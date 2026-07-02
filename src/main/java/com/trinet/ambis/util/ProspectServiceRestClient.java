package com.trinet.ambis.util;

import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.common.auth.TriNetAuthUtil;
import com.trinet.domain.common.ReturnResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Optional;

@Component
public class ProspectServiceRestClient {

	private static final Logger logger = LoggerFactory.getLogger(ProspectServiceRestClient.class);

	@Autowired
	private RestTemplate restTemplate;

	@SuppressWarnings("unchecked")
	public <T> T prepareRequestAndCallEndPoint(ProspectApiRequest<T> request) {
		String apiUri = buildApiUri(request);
		HttpHeaders headers = buildHeaders();
		Object body = null;

		if (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PUT) {
			body = request.getRequestBody();
		}
		logger.info("Calling Prospect API at: {} with method: {}", apiUri, request.getMethod());

		ResponseEntity<ReturnResponse<T>> response = callApi(apiUri, request.getMethod(), headers, body, request.getParameterizedTypeReference());
		if (request.getMethod() == HttpMethod.PUT || request.getMethod() == HttpMethod.DELETE) {
			return (T) response;
		} else {
			ReturnResponse<T> tReturnResponse = extractResponseBody(response);
			return extractData(tReturnResponse);
		}
	}

	private <T> String buildApiUri(ProspectApiRequest<T> request) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
				.path(request.getUri())
				.queryParams(Optional.ofNullable(request.getQueryParams()).orElse(new LinkedMultiValueMap<>()));

		return uriBuilder.buildAndExpand(
				Optional.ofNullable(request.getPathParams()).orElse(Collections.emptyMap())
		).toString();
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.add("Authorization", getSystemAccountAuthorization());
		headers.add("scope", BSSMessageConfig.getProperty("api.security.benefits.batch.scope"));
		return headers;
	}

	private <T> ResponseEntity<ReturnResponse<T>> callApi(String uri, HttpMethod method, HttpHeaders headers, Object body,
														  ParameterizedTypeReference<ReturnResponse<T>> typeRef) {
		HttpEntity<Object> entity = new HttpEntity<>(body, headers);
		return restTemplate.exchange(uri, method, entity, typeRef);
	}

	private <T> ReturnResponse<T> extractResponseBody(ResponseEntity<ReturnResponse<T>> responseEntity) {
		return Optional.ofNullable(responseEntity)
				.filter(response -> response.getStatusCode() == HttpStatus.OK)
				.map(ResponseEntity::getBody)
				.orElse(new ReturnResponse<>());
	}

	private <T> T extractData(ReturnResponse<T> response) {
		return Optional.ofNullable(response)
				.map(ReturnResponse::getData)
				.orElse(null);
	}

	private String getSystemAccountAuthorization() {
		return TriNetAuthUtil.getAuthorization(BSSMessageConfig.getProperty("benefits.batch.client.id"),
				BSSMessageConfig.getProperty("benefits.batch.client.secret"));
	}

}
