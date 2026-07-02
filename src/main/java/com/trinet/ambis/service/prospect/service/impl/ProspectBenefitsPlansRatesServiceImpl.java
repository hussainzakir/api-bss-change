package com.trinet.ambis.service.prospect.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.authorization.BenefitsBatchAuthorization;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.PlanCompareHelper;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.TierRate;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;
import com.trinet.ambis.service.prospect.exception.ProspectApiCallException;
import com.trinet.ambis.service.prospect.response.ApiRes;
import com.trinet.ambis.service.prospect.response.BenefitsPlansRatesRes;
import com.trinet.ambis.service.prospect.service.ProspectBenefitsPlansRatesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProspectBenefitsPlansRatesServiceImpl implements ProspectBenefitsPlansRatesService {

	private final RestTemplate restTemplate;

	@Override
	public Map<String, RateDetail> getBenefitsPlansRateDetails(List<String> benefitsPlanIds, String prospectId) {
		Optional<List<BenefitsPlansRatesRes>> benefitsPlansRatesResOpt = getBenefitsPlansRates(benefitsPlanIds,
				prospectId);
		if (benefitsPlansRatesResOpt.isPresent()) {
			List<BenefitsPlansRatesRes> benefitsPlansRatesRes = benefitsPlansRatesResOpt.get();
			return benefitsPlansRatesRes.stream()
					.collect(Collectors.toMap(BenefitsPlansRatesRes::getBenefitPlanId,
							res -> RateDetail.builder().rateType(getRateType(res)).regionCode(getAllRegionCode())
									.tierRates(getRates(res)).build()));
		}
		return Collections.emptyMap();
	}

	private Optional<List<BenefitsPlansRatesRes>> getBenefitsPlansRates(List<String> benefitsPlanIds,
			String prospectId) {
		List<String> tmpPlanIds = new ArrayList<>(benefitsPlanIds);
		tmpPlanIds.removeAll(Arrays.asList(PlanCompareHelper.MED_NO_PLAN_ID, PlanCompareHelper.DEN_NO_PLAN_ID,
				PlanCompareHelper.VIS_NO_PLAN_ID));
		if (CollectionUtils.isNotEmpty(tmpPlanIds)) {
			Optional<List<BenefitsPlansRatesRes>> result;
			StopWatch taskWatch = new StopWatch(ProspectURIConstants.PROPERTY_PROSPECT_BENEFITS_PLANS_RATES_API_URI);
			taskWatch.start();
			log.info(String.format("%s started", taskWatch.getId()));
			ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> apiResponse = restTemplate.exchange(
					buildUri(tmpPlanIds, prospectId), HttpMethod.GET, new HttpEntity<>(buildHeaders()),
					getResponseType());
			if (isApiCallSuccessful(apiResponse)) {
				result = Optional.ofNullable(apiResponse.getBody().getData());
			} else {
				throw new ProspectApiCallException("Error occured while getting prospect's benefits plans rates.");
			}
			taskWatch.stop();
			log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
			return result;
		} else {
			return Optional.empty();
		}
	}

	private ParameterizedTypeReference<ApiRes<List<BenefitsPlansRatesRes>>> getResponseType() {
		return new ParameterizedTypeReference<ApiRes<List<BenefitsPlansRatesRes>>>() {
		};
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		BenefitsBatchAuthorization.addAuthHeaders(headers);
		return headers;
	}

	private static String buildUri(List<String> benefitsPlanIds, String prospectId) {
		String prospectStrategyServiceUri = BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)
				+ BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_PROSPECT_BENEFITS_PLANS_RATES_API_URI);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(prospectStrategyServiceUri);
		return uriBuilder.queryParam(ProspectConstants.PROSPECT_ID_REQ_PARAM, prospectId)
				.buildAndExpand(StringUtils.join(benefitsPlanIds, ',')).toString();
	}

	private boolean isApiCallSuccessful(ResponseEntity<ApiRes<List<BenefitsPlansRatesRes>>> apiResponse) {
		ApiRes<List<BenefitsPlansRatesRes>> apiResponseBody = apiResponse.getBody();
		return (HttpStatus.OK == apiResponse.getStatusCode()) && apiResponseBody != null
				&& (apiResponseBody.getError() == null) && CollectionUtils.isNotEmpty(apiResponseBody.getData());
	}

	private String getRateType(BenefitsPlansRatesRes benefitsPlansRatesRes) {
		return CollectionUtils.isEmpty(benefitsPlansRatesRes.getAgeBandedRates()) ? RateTypeEnum.FOUR_TIER.getType()
				: RateTypeEnum.AGE_BANDED.getType();
	}

	private List<String> getAllRegionCode() {
		return List.of("ALL");
	}

	private List<TierRate> getRates(BenefitsPlansRatesRes benefitsPlansRatesRes) {
		if (CollectionUtils.isEmpty(benefitsPlansRatesRes.getAgeBandedRates())
				&& CollectionUtils.isEmpty(benefitsPlansRatesRes.getTierRates())) {
			return Collections.emptyList();
		}
		return CollectionUtils.isEmpty(benefitsPlansRatesRes.getAgeBandedRates())
				? benefitsPlansRatesRes.getTierRates().stream()
						.map(rateDetails -> TierRate.builder()
								.cvgTierCode(CoverageCodesEnums.valueOfId(rateDetails.getCvgTierCode()))
								.cost(rateDetails.getCost()).build())
						.collect(Collectors.toList())
				: benefitsPlansRatesRes
						.getAgeBandedRates().stream().map(rateDetails -> TierRate.builder()
								.cvgTierCode(rateDetails.getAgeBandCode()).cost(rateDetails.getCost()).build())
						.collect(Collectors.toList());
	}

}
