package com.trinet.ambis.service.prospect.impl;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.service.prospect.ProspectPlanService;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

@Service
public class ProspectPlanServiceImpl implements ProspectPlanService {

	@Autowired
	ProspectServiceRestClient prospectServiceRestClient;

	@SuppressWarnings("unchecked")
	@Override
	public List<BenefitPlansRes> getBenefitPlansBy(String prospectId, String rateType, boolean includeWithRates) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put(ProspectConstants.PROSPECT_ID_REQ_PARAM, List.of(prospectId));
		map.put(ProspectConstants.RATE_TYPE_PARAM, List.of(rateType));
		map.put(ProspectConstants.INCLUDE_RATES_PARAM, List.of(String.valueOf(includeWithRates)));
		ParameterizedTypeReference<ReturnResponse<Object>> getBenefitPlansBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest prospectApiGetRequest = ProspectApiRequest.builder().method(HttpMethod.GET)
				.uri(ProspectURIConstants.BENEFIT_PLANS).queryParams(map)
				.parameterizedTypeReference(getBenefitPlansBean).build();
		Object response = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		if (Objects.nonNull(response)) {
			return (List<BenefitPlansRes>) response;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<BenefitPlansRes> getBenefitPlansBy(String prospectId) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put(ProspectConstants.PROSPECT_ID_REQ_PARAM, List.of(prospectId));
		ParameterizedTypeReference<ReturnResponse<Object>> getBenefitPlansBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest prospectApiGetRequest = ProspectApiRequest.builder().method(HttpMethod.GET)
				.uri(ProspectURIConstants.BENEFIT_PLANS).queryParams(map)
				.parameterizedTypeReference(getBenefitPlansBean).build();
		Object response = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		if (Objects.nonNull(response) && !ObjectUtils.isEmpty(response)) {
			Gson gson = new Gson();
			Type benefitPlansRes = new TypeToken<List<BenefitPlansRes>>() {}.getType();
			return gson.fromJson(gson.toJson(response), benefitPlansRes);
		} else {
			return Collections.emptyList();
		}
	}


}
