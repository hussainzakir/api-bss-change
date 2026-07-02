package com.trinet.ambis.util;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.HrisURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.helper.CommonServiceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trinet.ambis.service.model.HrisPlanAttributeRequest;
import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class HrisPlanServiceRestClient {

    @Autowired
    private RestTemplate restTemplate;

    public <T> T getBenefitPlanAvailability(HrisPlanRequest request, ParameterizedTypeReference<T> typeReference){
        String requestBody = CommonServiceHelper.objectToJsonString(request);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, buildHeaders());
        String apiUri = buildApiUri(HrisURIConstants.HRIS_AVAILABLE_PLAN_URI, new LinkedMultiValueMap<>());
        return callApiEndPoint(apiUri, HttpMethod.POST, entity, typeReference);
    }

	public <T> T getPlanAttributes(HrisPlanAttributeRequest request, ParameterizedTypeReference<T> typeReference) {
				validatePlanIds(request.getPlanIds());

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("planIds", String.join(",", request.getPlanIds()));
        queryParams.add("benefitsType", request.getBenefitType());
		queryParams.add("responseVersion", BSSApplicationConstants.VERSION_V2);

        String apiUri = buildApiUri(HrisURIConstants.HRIS_PLAN_ATTRIBUTES_URI, queryParams);

        HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
        return callApiEndPoint(apiUri, HttpMethod.GET, entity, typeReference);
    }

    private void validatePlanIds(List<String> planIds) {
        List<String> invalidIds = planIds.stream()
                .filter(planId -> !planId.matches("\\d+"))
                .collect(Collectors.toList());

        if (!invalidIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid plan IDs: " + String.join(", ", invalidIds));
        }
    }

    private String buildApiUri(String path, MultiValueMap<String, String> queryParams) {
        return UriComponentsBuilder.fromHttpUrl(BSSMessageConfig.getProperty(HrisURIConstants.HRIS_PLAN_API_URI))
                .path(path)
                .queryParams(Optional.ofNullable(queryParams).orElse(new LinkedMultiValueMap<>()))
                .toUriString();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // No authorization is needed
        return headers;
    }

    private <T> T callApiEndPoint(String apiUri,  HttpMethod method, HttpEntity<?> entity, ParameterizedTypeReference<T> typeReference) {
        ResponseEntity<T> response = restTemplate.exchange(apiUri, method, entity, typeReference);
        return extractResponseBody(response);
    }

    private <T> T extractResponseBody(ResponseEntity<T> responseEntity) {
        return Optional.ofNullable(responseEntity)
                .filter(response -> response.getStatusCode() == HttpStatus.OK)
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new RuntimeException("Failed to get valid response from API."));
    }
}
