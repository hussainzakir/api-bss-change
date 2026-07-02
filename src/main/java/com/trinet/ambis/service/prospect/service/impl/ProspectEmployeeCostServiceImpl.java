package com.trinet.ambis.service.prospect.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ProspectEmployeeCostServiceImpl implements ProspectEmployeeCostService {

    @Autowired
    private ProspectServiceRestClient prospectServiceRestClient;

    @Override
	public Optional<List<EmployeeCostRes>> getProspectEmployeeCostByType(String prospectId, List<String> benefitTypes) {

		Supplier<Optional<List<EmployeeCostRes>>> makeGetApiCall = callProspectApiforEmployeeCost(prospectId,
				benefitTypes);
		return makeGetApiCall.get();
	}

	private Supplier<Optional<List<EmployeeCostRes>>> callProspectApiforEmployeeCost(String prospectId,
			List<String> benefitTypes) {
		return () -> {
			List<EmployeeCostRes> reponseData = getEmployeeCostResponse(prospectId, benefitTypes);
			return Optional.ofNullable(reponseData);
		};
	}

    @SuppressWarnings("unchecked")
	public List<EmployeeCostRes> getEmployeeCostResponse(String prospectId, List<String> benefitTypes) {
        if (benefitTypes.isEmpty()) {
            return Collections.emptyList();
        }

        StopWatch taskWatch = new StopWatch(ProspectURIConstants.PROSPECT_EMPLOYEE_COSTS_BY_TYPES_URI);
        taskWatch.start();
        MultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
        queryParam.put("prospectId", List.of(prospectId));
        ParameterizedTypeReference<ReturnResponse<List<EmployeeCostRes>>> benefitGroupAssignmentBean = new ParameterizedTypeReference<ReturnResponse<List<EmployeeCostRes>>>() {};
        Map<String, String> pathParams = new HashMap<>();
        String benefitTypesParam = benefitTypes.stream().reduce(((s1, s2) -> s1 + "," + s2 )).orElse("");
        pathParams.put(ProspectConstants.BENEFITS_TYPES_PATH_PARAM, benefitTypesParam);
        ProspectApiRequest<List<EmployeeCostRes>> prospectApiGetRequest = ProspectApiRequest.<List<EmployeeCostRes>>builder()
                .method(HttpMethod.GET)
                .uri(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_EMPLOYEE_COSTS_BY_TYPES_URI)).queryParams(queryParam)
                .pathParams(pathParams)
                .parameterizedTypeReference(benefitGroupAssignmentBean).build();
        Object response = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
        taskWatch.stop();
        log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
        if (response != null) {
            return (List<EmployeeCostRes>) response;
        } else {
            return Collections.emptyList();
        }
    }

}
