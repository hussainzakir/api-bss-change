package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BplURIConstants;
import com.trinet.ambis.service.BplService;
import com.trinet.ambis.service.model.BplApiRequest;
import com.trinet.ambis.service.model.plancompare.BenPlanCompareResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.BplServiceRestClient;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.common.SecurityConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BplServiceImpl implements BplService {

    @Autowired
    private BplServiceRestClient bplServiceRestClient;

    @Override
    public CompletableFuture<List<BenefitPlanCompare>> getBPLAttributes(Set<String> planIds, Date effDate,
    		String template, HttpServletRequest httpRequest) {
    	log.info("Calling BPL service to get plan attributes");
    	if (CollectionUtils.isEmpty(planIds)) {
    		return CompletableFuture.completedFuture(List.of());
    	}

    	var queryParams = prepareQueryParams(planIds, effDate, template);
    	var pathParams = preparePathParams(httpRequest);

    	var myBean = new ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>>() {};
    	var bplApiRequest = BplApiRequest.builder().method(HttpMethod.GET)
    			.uri(BplURIConstants.BPL_INFO_URI).queryParams(queryParams).pathParams(pathParams)
    			.parameterizedTypeReference(myBean).build();

    	return CompletableFuture
    			.supplyAsync(() -> bplServiceRestClient.prepareRequestAndCallEndPoint(bplApiRequest, myBean))
    			.thenApply(response -> Optional.ofNullable(response)
    					.filter(this::isResponseValid)
    					.map(r -> r.getData().getPlans())
    					.orElseThrow(() -> new RuntimeException("Failed to fetch valid plan data")))
    			.exceptionally(ex -> {
    				log.error("Error during BPL API call: {}", ex.getMessage(), ex);
    				return List.of();
    			});
    }

    private MultiValueMap<String, String> prepareQueryParams(Set<String> planIds, Date effDate, String template) {
    	return new LinkedMultiValueMap<>(Map.of(
    			"planIds", List.of(String.join(",", planIds)),
    			"effectiveDate", List.of(CommonUtils.formatDateToString(effDate, BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD)),
    			"template", List.of(template),
    			"showEmptyTemplate", List.of(String.valueOf(true))
    			));
    }

    private Map<String, String> preparePathParams(HttpServletRequest httpRequest) {
        return Map.of(
            SecurityConstants.COMPANY_ID, BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest),
            SecurityConstants.EMPLOYEE_ID, BSSSecurityUtils.getAuthenticatedEmplId(httpRequest)
        );
    }

    private boolean isResponseValid(ReturnResponse<BenPlanCompareResponse> response) {
        return response != null && ( HttpStatus.OK.value() == Integer.parseInt(response.getStatusCode()) 
            && response.getData() != null 
            && response.getError() == null);
    }
}
