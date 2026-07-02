package com.trinet.ambis.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.common.BssCoreURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.service.model.bsscore.BssCoreProcessStatusResponse;
import com.trinet.ambis.service.model.bsscore.CompanyResponse;
import com.trinet.ambis.service.model.bsscore.GraphQLRequest;
import com.trinet.ambis.service.model.bsscore.GraphQLResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.common.auth.TriNetAuthUtil;

import lombok.extern.log4j.Log4j2;
import lombok.val;

@Component
@Log4j2
public class BssCoreServiceClient {

	@Autowired
	private RestTemplate restTemplate;

	public Map<String, Employee> getCensusByCode(String companyCode) {
		String query = "query($code: String!) {companyByCode(code: $code) { census { prospectEmployeeId, lastName, firstName, k1} }}";
		Map<String, Object> variables = Map.of("code", companyCode);
		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};
		GraphQLRequest request = GraphQLRequest.builder().query(query).variables(variables).build();

		CompanyResponse companyResponse = prepareRequestAndCallEndPoint(request, typeReference);
		return buildEmployees(companyResponse);
	}

	public boolean getAleStatus(long companyId) {
		String query = "query($peoCompanyId: Int!) {companyByPeoCompanyId(peoCompanyId: $peoCompanyId) { aleStatus { ale, companyId } } }";
		Map<String, Object> variables = Map.of("peoCompanyId", companyId);
		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};
		GraphQLRequest request = GraphQLRequest.builder().query(query).variables(variables).build();
		CompanyResponse response = prepareRequestAndCallEndPoint(request, typeReference);
		return getAleStatus(response);
	}

	public List<ProspectCensusResponse> getCensusByCompanyCode(String companyCode) {
		String query = "query($code: String!) {companyByCode(code: $code) { census { prospectEmployeeId, lastName, firstName, annualWages, homeState, homePostalCode, medicalTier, dentalTier, visionTier } }}";
		Map<String, Object> variables = Map.of("code", companyCode);
		ParameterizedTypeReference<GraphQLResponse<CompanyResponse>> typeReference = new ParameterizedTypeReference<>() {
		};
		GraphQLRequest request = GraphQLRequest.builder().query(query).variables(variables).build();

		CompanyResponse companyResponse = prepareRequestAndCallEndPoint(request, typeReference);
		return buildCensus(companyResponse);
	}

    /**
     * Fetches bundle selection process statuses for the given company from BSS Core.
     * Throws {@link org.springframework.web.client.HttpClientErrorException} on HTTP errors
     * (e.g. 401/403) so callers can handle auth failures explicitly.
     *
     * @param companyCode the company code to query
     * @return {@link BssCoreProcessStatusResponse}
     */
    public BssCoreProcessStatusResponse getProcessStatusesBy(String companyCode) {
        String url = BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_REST_API_URI)
                + BssCoreURIConstants.PROCESS_STATUS_BY_COMPANY_PATH.replace("{companyCode}", companyCode);
        HttpEntity<?> entity = new HttpEntity<>(buildHeadersAndBasicAuthorization());
        log.debug("Fetching BSS Core process statuses for companyCode={} url={}", companyCode, url);
        ResponseEntity<BssCoreProcessStatusResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, BssCoreProcessStatusResponse.class);
        return response.getBody();
    }

    /**
     * Prepares the GraphQL request and calls the BSS Core API endpoint.
     *
     * @param request       The GraphQL request to be sent.
     * @param typeReference The type reference for the expected response.
     * @param <T>           The type of the data in the response.
     * @return The data extracted from the GraphQL response.
     */
    public <T> T prepareRequestAndCallEndPoint(GraphQLRequest request,
            ParameterizedTypeReference<GraphQLResponse<T>> typeReference) {
        HttpEntity<GraphQLRequest> entity = new HttpEntity<>(request,
                buildHeadersAndBasicAuthorization());
        String apiUri = buildApiUri(new LinkedMultiValueMap<>());
        ResponseEntity<GraphQLResponse<T>> response = callApi(apiUri, HttpMethod.POST,
                entity.getHeaders(), entity.getBody(), typeReference);
        GraphQLResponse<T> tReturnResponse = extractResponseBody(response);
        return extractData(tReturnResponse);
    }

	private <T> ResponseEntity<GraphQLResponse<T>> callApi(String uri, HttpMethod method, HttpHeaders headers,
			Object body, ParameterizedTypeReference<GraphQLResponse<T>> typeRef) {
		HttpEntity<Object> entity = new HttpEntity<>(body, headers);
		return restTemplate.exchange(uri, method, entity, typeRef);
	}

	private String buildApiUri(MultiValueMap<String, String> queryParams) {
		return UriComponentsBuilder.fromHttpUrl(BSSMessageConfig.getProperty(BssCoreURIConstants.BSS_CORE_API_URI))
				.queryParams(Optional.ofNullable(queryParams).orElse(new LinkedMultiValueMap<>())).toUriString();
	}

	private HttpHeaders buildHeadersAndBasicAuthorization() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		addBasicAuthorizationHeader(headers);
		return headers;

	}

	private void addBasicAuthorizationHeader(HttpHeaders headers) {
		headers.add("Authorization", getSystemAccountAuthorization());
		headers.add("scope", BSSMessageConfig.getProperty("api.security.benefits.batch.scope"));
	}

	private String getSystemAccountAuthorization() {
		return TriNetAuthUtil.getAuthorization(BSSMessageConfig.getProperty("benefits.batch.client.id"),
				BSSMessageConfig.getProperty("benefits.batch.client.secret"));
	}

	private Map<String, Employee> buildEmployees(CompanyResponse companyResponse) {
		Map<String, Employee> bssCoreEmployees = new HashMap<>();
		if (Objects.isNull(companyResponse) || Objects.isNull(companyResponse.getCompanyByCode())
				|| Objects.isNull(companyResponse.getCompanyByCode().getCensus())) {
			return bssCoreEmployees;
		}
		for (val census : companyResponse.getCompanyByCode().getCensus()) {
			Employee employee = new Employee();
			employee.setEmplId(census.getProspectEmployeeId());
			employee.setEmplName(census.getFirstName() + " " + census.getLastName());
            employee.setK1(census.isK1());
			bssCoreEmployees.put(census.getProspectEmployeeId(), employee);
		}
		return bssCoreEmployees;
	}

	private boolean getAleStatus(CompanyResponse companyResponse) {
		if (Objects.isNull(companyResponse) || Objects.isNull(companyResponse.getCompanyByPeoCompanyId())
				|| Objects.isNull(companyResponse.getCompanyByPeoCompanyId().getAleStatus())) {
			return false;
		}
		return companyResponse.getCompanyByPeoCompanyId().getAleStatus().isAle();
	}

	private List<ProspectCensusResponse> buildCensus(CompanyResponse companyResponse) {
		List<ProspectCensusResponse> censusData = new ArrayList<>();
		if (Objects.isNull(companyResponse) || Objects.isNull(companyResponse.getCompanyByCode())
				|| Objects.isNull(companyResponse.getCompanyByCode().getCensus())) {
			return censusData;
		}
		for (val census : companyResponse.getCompanyByCode().getCensus()) {
			ProspectCensusResponse employee = new ProspectCensusResponse();
			employee.setEmployeeId(census.getProspectEmployeeId());
			employee.setEmployeeName(census.getFirstName() + " " + census.getLastName());
            employee.setFirstName(census.getFirstName());
            employee.setLastName(census.getLastName());
			employee.setSalary(census.getAnnualWages());
			employee.setState(census.getHomeState());
			employee.setZip(census.getHomePostalCode());
			employee.setMedicalTier(census.getMedicalTier());
			employee.setDentalTier(census.getDentalTier());
			employee.setVisionTier(census.getVisionTier());
			censusData.add(employee);
		}
		return censusData;
	}

	private <T> GraphQLResponse<T> extractResponseBody(ResponseEntity<GraphQLResponse<T>> responseEntity) {
		return Optional.ofNullable(responseEntity).filter(response -> response.getStatusCode() == HttpStatus.OK)
				.map(ResponseEntity::getBody).orElse(new GraphQLResponse<>());
	}

	private <T> T extractData(GraphQLResponse<T> response) {
		return Optional.ofNullable(response).map(GraphQLResponse::getData).orElse(null);
	}
}