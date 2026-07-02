package com.trinet.ambis.service.model;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class BplApiRequest {

	String uri;

	Map<String, ?> pathParams;

	MultiValueMap<String, String> queryParams;

	HttpMethod method;

	ParameterizedTypeReference<?> parameterizedTypeReference;

}
