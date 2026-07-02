package com.trinet.ambis.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.trinet.common.auth.TriNetAuthUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service client to call the BenBundles Plan Mapping API
 * from api-bs-hw-bss-benbundles service.
 */
@Slf4j
@Component
public class DefaultPlanMappingServiceClient {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${dcpApiUri}")
	private String dcpApiUri;

	@Value("${benbundles.plan-mapping.api.uri}")
	private String planMappingApiPath;

	@Value("${api.security.benefits.batch.scope}")
	private String benefitsBatchScope;

	@Value("${benefits.batch.client.id}")
	private String benefitsBatchClientId;

	@Value("${benefits.batch.client.secret}")
	private String benefitsBatchClientSecret;

	/**
	 * Get plan mapping by company code.
	 *
	 * @param companyCode the company code
	 * @param planMappingRequest the plan mapping request containing mappingRuleTemplateName,
	 *                           exchangeId, targetEffectiveDate, and cloneBenefitProgram
	 * @return list of PlanMappingResponse objects
	 */
	public List<PlanMappingResponse> getPlanMapping(String companyCode, PlanMappingRequest planMappingRequest) {
		log.info("Calling Plan Mapping API for company code: {}", companyCode);
		String apiUri = buildApiUri(companyCode);
		HttpHeaders headers = buildHeadersAndBasicAuthorization();
		HttpEntity<PlanMappingRequest> entity = new HttpEntity<>(planMappingRequest, headers);

		ParameterizedTypeReference<List<PlanMappingResponse>> typeReference =
				new ParameterizedTypeReference<>() {};

		ResponseEntity<List<PlanMappingResponse>> response = restTemplate.exchange(
				apiUri, HttpMethod.POST, entity, typeReference);

		return extractResponseBody(response);
	}

	private String buildApiUri(String companyCode) {
		return UriComponentsBuilder.fromHttpUrl(dcpApiUri)
			.path(planMappingApiPath)
			.buildAndExpand(companyCode)
			.toUriString();
	}

	private HttpHeaders buildHeadersAndBasicAuthorization() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		addBasicAuthorizationHeader(headers);
		return headers;
	}

	private void addBasicAuthorizationHeader(HttpHeaders headers) {
		headers.add("Authorization", TriNetAuthUtil.getAuthorization(benefitsBatchClientId, benefitsBatchClientSecret));
		headers.add("scope", benefitsBatchScope);
	}

	private List<PlanMappingResponse> extractResponseBody(ResponseEntity<List<PlanMappingResponse>> responseEntity) {
		return Optional.ofNullable(responseEntity)
			.filter(response -> response.getStatusCode() == HttpStatus.OK)
			.map(ResponseEntity::getBody)
			.orElse(Collections.emptyList());
	}

	/**
	 * PlanMappingRequest DTO
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PlanMappingRequest {
		private String cloneBenefitProgram;
		private String mappingRuleTemplateName;
		private String exchangeId;
		private String targetEffectiveDate;
		private Set<String> employeeIds;
		private String hqState;
		private String hqZip;
		private String naicsCode;
	}

	/**
	 * PlanMappingResponse DTO - Response from Plan Mapping API
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PlanMappingResponse {
		private List<EmployeeResponse> employeeResponseList;
	}

	/**
	 * EmployeeResponse DTO - Employee response from Plan Mapping API
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class EmployeeResponse {
		@JsonProperty("id")
		private String employeeId;
		private Map<String, PlanResponse> mappedPlans;
	}

	/**
	 * PlanResponse DTO - Mapped plan details from Plan Mapping API
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PlanResponse {
		private String planId;
		private String enrolledCvgCode;
		private Map<String, PlanCostResponse> cvgLevelCost;
	}

	/**
	 * PlanCostResponse DTO - Cost details per coverage level from Plan Mapping API
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PlanCostResponse {
		private java.math.BigDecimal eeRate;
		private java.math.BigDecimal erRate;
		private java.math.BigDecimal totalCost;
	}
}
