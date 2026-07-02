package com.trinet.ambis.service.prospect.impl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.prospect.SfdcClientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SfdcClientServiceImpl implements SfdcClientService {

	private final RestTemplate restTemplate;

	@Override
	public void sendProposal(MultiValueMap<String, Object> bodyMap) {
		HttpEntity<MultiValueMap<String, Object>> entity = prepareRequestEntity(bodyMap);
		String hipServiceUrl = buildHIPUri();
		long startTime = System.currentTimeMillis();
		log.info("SfdcClientServiceImpl::MultiValueMap Start call to url: {} and timestamp: {}", hipServiceUrl,
				startTime);
		restTemplate.postForObject(hipServiceUrl, entity, String.class);
		log.info("SfdcClientServiceImpl::MultiValueMap End call to url: {} and time taken in ms: {}", hipServiceUrl,
				System.currentTimeMillis() - startTime);
	}

	private HttpEntity<MultiValueMap<String, Object>> prepareRequestEntity(MultiValueMap<String, Object> bodyMap) {
		return new HttpEntity<>(bodyMap, buildHeaders());
	}

	private HttpHeaders buildHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		return headers;
	}

	private String buildHIPUri() {
		return UriComponentsBuilder.fromHttpUrl(BSSMessageConfig.getProperty("prospect.HIP.proposal.submit.url"))
				.buildAndExpand().toString();
	}

}
