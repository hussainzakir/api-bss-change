package com.trinet.ambis.service.model.prospect;

import java.util.Map;

import com.trinet.domain.common.ReturnResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class ProspectApiRequest<T> {

	String uri;

	Map<String, ?> pathParams;

	MultiValueMap<String, String> queryParams;

	HttpMethod method;

	Object requestBody;

	ParameterizedTypeReference<ReturnResponse<T>> parameterizedTypeReference;

}
