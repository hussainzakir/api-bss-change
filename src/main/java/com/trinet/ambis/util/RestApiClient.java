package com.trinet.ambis.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.rest.controllers.dto.planofferings.ReportDetails;
import com.trinet.ambis.service.dto.CmsLogoDto;
import com.trinet.exception.SecureRestServiceBrokerException;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RestApiClient {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private TokenUtils tokenUtils;
	
	public byte[] getReturnResponse(HttpServletRequest httpRequest, ReportDetails reportDetails, String url, HttpMethod httpMethod) {
		log.info("URI : {}, HTTP Method : {}", url, httpMethod);
		HttpEntity<?> entity = new HttpEntity<>(reportDetails, getHeader(httpRequest));
		ResponseEntity<byte[]> result = null;
		try {
	        long startTime = System.currentTimeMillis();
			result = restTemplate.exchange(url, httpMethod, entity, byte[].class);
			long estimatedTime = System.currentTimeMillis() - startTime;
			log.debug("restUrl <{}> estimatedTime <{}> ", url, estimatedTime);
			log.debug("result = {} " , result);
		} catch (SecureRestServiceBrokerException exception) {
			log.error("Exception - URL {}  message {} " , url,exception.getMessage());
		}
		
		return Objects.nonNull(result) ? result.getBody() : null;
	}
	
	private HttpHeaders getHeader(HttpServletRequest httpRequest) {
		return tokenUtils.getHeaders(MediaType.APPLICATION_JSON_VALUE).apply(httpRequest);
	}

	public CmsLogoDto fetchCarrierLogos(String url, String cmsLogosId){
		log.info("Fetching carrier logos from the CMS..{}", url);
		ResponseEntity<CmsLogoDto> result = null;
		try {
			long startTime = System.currentTimeMillis();
			HttpEntity<?> entity = new HttpEntity<>(tokenUtils.getCMSHeader());
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
					.queryParam("folder", cmsLogosId)
					.queryParam("include_metadata", true);
			result = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, CmsLogoDto.class);
			long estimatedTime = System.currentTimeMillis() - startTime;
			log.debug("restUrl <{}> estimatedTime <{}> ", url, estimatedTime);
			log.debug("result = {} " , result);
		}catch (Exception exception) {
			log.error("Exception - message {} " ,exception.getMessage());
		}
		return Optional.ofNullable(result).map(HttpEntity::getBody).orElse(null);
	}
}
