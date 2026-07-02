package com.trinet.ambis.util;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.common.BplURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.model.BplApiRequest;
import com.trinet.common.auth.TriNetAuthUtil;

@Component
public class BplServiceRestClient {

    @Autowired
    private RestTemplate restTemplate;

    public <T> T prepareRequestAndCallEndPoint(BplApiRequest request, ParameterizedTypeReference<T> typeReference) {
        String apiUri = buildApiUri(request);
        HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
        return callApiEndPoint(apiUri, entity, typeReference);
    }

    private String buildApiUri(BplApiRequest request) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(BSSMessageConfig.getProperty(BplURIConstants.BPL_API_URI))
                .path(request.getUri())
                .queryParams(Optional.ofNullable(request.getQueryParams()).orElse(new LinkedMultiValueMap<>()));

        return uriBuilder.buildAndExpand(Optional.ofNullable(request.getPathParams()).orElse(Collections.emptyMap())).toString();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", TriNetAuthUtil.getAuthorization(
                BSSMessageConfig.getProperty("benefits.batch.client.id"),
                BSSMessageConfig.getProperty("benefits.batch.client.secret")));
        headers.add("scope", BSSMessageConfig.getProperty("api.security.benefits.batch.scope"));
        return headers;
    }

    private <T> T callApiEndPoint(String apiUri, HttpEntity<String> entity, ParameterizedTypeReference<T> typeReference) {
        ResponseEntity<T> response = restTemplate.exchange(apiUri, HttpMethod.GET, entity, typeReference);
        return extractResponseBody(response);
    }

    private <T> T extractResponseBody(ResponseEntity<T> responseEntity) {
        return Optional.ofNullable(responseEntity)
                .filter(response -> response.getStatusCode() == HttpStatus.OK)
                .map(ResponseEntity::getBody)
                .orElseThrow(() -> new RuntimeException("Failed to get valid response from API."));
    }
}
