package com.trinet.ambis.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.BenefitsPlanViewService;
import com.trinet.ambis.service.model.plancompare.BenPlanCompareResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.common.AppConfig;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.common.SecurityConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author schaudhari
 *
 */
@Service
@Slf4j
public class BenefitsPlanViewServiceImpl implements BenefitsPlanViewService {

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public CompletableFuture<List<BenefitPlanCompare>> getBenefitPlanAttributes(Set<String> planIds, Date effDate,
			String template, HttpServletRequest httpRequest) {
		if(CollectionUtils.isEmpty(planIds)) {
			return CompletableFuture.completedFuture(List.of());
		}
		ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>> myBean = new ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>>() {
		};
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		setBasicAuthorization(headers);
		BSSSecurityUtils.addImpersonationHeaders(httpRequest, headers);

		String benefitPlanViewUrl = prepareBenPlanViewServiceUri(effDate, planIds, template, httpRequest);

		StopWatch taskWatch = new StopWatch("PlanViewApiCall");
		taskWatch.start();
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<ReturnResponse<BenPlanCompareResponse>> resp = restTemplate.exchange(benefitPlanViewUrl,
				HttpMethod.GET, entity, myBean);
		ReturnResponse<BenPlanCompareResponse> currentPlansAttributes = resp.getBody();
		CompletableFuture<List<BenefitPlanCompare>> result = null;
		if (isApiCallSuccessful(currentPlansAttributes)) {
			log.info(String.format("Total plans :: %s", currentPlansAttributes.getData().getPlans().size()));
			result = CompletableFuture.completedFuture(currentPlansAttributes.getData().getPlans());
		} else {
			throw new RuntimeException("Error occured while getting plan data from plan view service.");
		}
		taskWatch.stop();
		log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
		return result == null ? CompletableFuture.completedFuture(List.of()) : result;
	}

	private String prepareBenPlanViewServiceUri(Date effectiveDate, Set<String> plansToCompare, String template,
			HttpServletRequest httpRequest) {
		String planViewServiceUri = AppConfig.getPlatformURL()
				+ BSSMessageConfig.getProperty(BSSURIConstants.PLAN_VIEW_API_URI);
		
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(SecurityConstants.COMPANY_ID, BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest));
		pathParams.put(SecurityConstants.EMPLOYEE_ID, BSSSecurityUtils.getAuthenticatedEmplId(httpRequest));

		String effectiveDateStr = CommonUtils.formatDateToString(effectiveDate,
				BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(planViewServiceUri)
				.queryParam("planIds", String.join(",", plansToCompare)).queryParam("effectiveDate", effectiveDateStr)
				.queryParam("template", template);

		return uriBuilder.buildAndExpand(pathParams).toString();
	}

	private void setBasicAuthorization(HttpHeaders headers) {
		String clientId = BSSMessageConfig.getProperty("benefits.batch.client.id");
		String clientSecret = BSSMessageConfig.getProperty("benefits.batch.client.secret");
		String scope = BSSMessageConfig.getProperty("api.security.benefits.batch.scope");

		String auth = clientId + ":" + clientSecret;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
		String authorization = new String(encodedAuth, StandardCharsets.UTF_8);
		authorization = "Basic " + authorization;

		headers.add("Authorization", authorization);
		headers.add("scope", scope);
	}

	private boolean isApiCallSuccessful(ReturnResponse<BenPlanCompareResponse> benPlanResponse) {
		return benPlanResponse != null && String.valueOf(HttpStatus.OK.value()).equals(benPlanResponse.getStatusCode())
				&& null != benPlanResponse.getData() && (benPlanResponse.getError() == null);
	}

}
