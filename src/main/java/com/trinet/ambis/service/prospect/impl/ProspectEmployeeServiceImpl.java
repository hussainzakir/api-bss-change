package com.trinet.ambis.service.prospect.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.authorization.BenefitsBatchAuthorization;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.exception.ProspectApiCallException;
import com.trinet.ambis.service.prospect.response.ApiRes;
import com.trinet.ambis.service.prospect.response.CensusRes;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProspectEmployeeServiceImpl implements ProspectEmployeeService {

	private final RestTemplate restTemplate;
	
	private final ProspectServiceRestClient prospectServiceRestClient;

	@Override
	public Optional<List<EmployeePlansRes>> getEmployeePlans(String prospectId) {
		Optional<List<EmployeePlansRes>> result;
		StopWatch taskWatch = new StopWatch(ProspectURIConstants.PROPERTY_EMPLOYEE_PLAN_ASSIGNMENT_API_URI);
		taskWatch.start();
		ResponseEntity<ApiRes<List<EmployeePlansRes>>> apiResponse = restTemplate.exchange(buildUri(prospectId),
				HttpMethod.GET, new HttpEntity<>(buildHeaders()), getResponseType());
		if (isApiCallSuccessful(apiResponse)) {
			result = Optional.of(apiResponse.getBody().getData());
		} else {
			throw new ProspectApiCallException("Error occured while getting prospect's employee plan assignment.");
		}
		taskWatch.stop();
		log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CensusRes> getEmployees(String prospectId) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put("prospectId", List.of(prospectId));
		ParameterizedTypeReference<ReturnResponse<List<CensusRes>>> benefitGroupAssignmentBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<List<CensusRes>> prospectApiGetRequest = ProspectApiRequest.<List<CensusRes>>builder()
				.method(HttpMethod.GET)
				.uri(ProspectURIConstants.PROSPECT_CENSUS_URI).queryParams(map)
				.parameterizedTypeReference(benefitGroupAssignmentBean).build();
		List<CensusRes> response = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		if (response != null) {
			return response;
		} else {
			return Collections.emptyList();
		}
	}
	
	private ParameterizedTypeReference<ApiRes<List<EmployeePlansRes>>> getResponseType() {
		return new ParameterizedTypeReference<ApiRes<List<EmployeePlansRes>>>() {
		};
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		BenefitsBatchAuthorization.addAuthHeaders(headers);
		return headers;
	}

	private static String buildUri(String prospectId) {
		String prospectStrategyServiceUri = BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI)
				+ BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_EMPLOYEE_PLAN_ASSIGNMENT_API_URI);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(prospectStrategyServiceUri);
		return uriBuilder.queryParam(ProspectConstants.PROSPECT_ID_REQ_PARAM, prospectId).buildAndExpand().toString();
	}

	private boolean isApiCallSuccessful(ResponseEntity<ApiRes<List<EmployeePlansRes>>> apiResponse) {
		ApiRes<List<EmployeePlansRes>> apiResponseBody = apiResponse.getBody();
		return (HttpStatus.OK == apiResponse.getStatusCode()) && apiResponseBody != null
				&& apiResponseBody.getError() == null;
	}

}