package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.Lists;
import com.trinet.ambis.authorization.BenefitsBatchAuthorization;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.PlanAvailabilityURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.common.AppConfig;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.common.SecurityConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author schaudhari
 *
 */
@Service
@Slf4j
public class PlanAvailabilityServiceImpl implements PlanAvailabilityService {

	@Autowired
	private RestTemplate restTemplate;

	@SuppressWarnings("unchecked")
	@Override
	public CompletableFuture<List<PlanAvailableResponse>> getBenefitPlanAvailability(
			PlanAvailableRequest planAvailableRequest) {

		List<PlanAvailableResponse> result = Lists.newArrayList();

		// Call the API in chunks of 50 and then merge the results
		List<PlanAvailableRequest.Location> locationList = planAvailableRequest.getLocations();
		List<List<PlanAvailableRequest.Location>> locationPartitions = Lists.partition(locationList, 50);

		StopWatch taskWatch = new StopWatch("TotalCall");
		taskWatch.start();
		List<PlanAvailableRequest> planAvailableRequestPartitions = new ArrayList<>();
		for (List<PlanAvailableRequest.Location> locationPartition : locationPartitions) {
			PlanAvailableRequest requestPartition = PlanAvailableRequest.builder()
					.cloneBenefitProgram(planAvailableRequest.getCloneBenefitProgram())
					.effectiveDate(planAvailableRequest.getEffectiveDate()).plans(planAvailableRequest.getPlans())
					.build();
			requestPartition.setLocations(locationPartition);
			planAvailableRequestPartitions.add(requestPartition);
		}
		List<CompletableFuture<List<PlanAvailableResponse>>> planAvailabilityFutures = planAvailableRequestPartitions.stream()
				.map(request -> CompletableFuture.supplyAsync(() -> getBenefitPlanAvailabilityChunk(request)))
				.collect(Collectors.toList());

		CompletableFuture<List<PlanAvailableResponse>>[] futuresArray = planAvailabilityFutures.toArray(new CompletableFuture[0]);

		CompletableFuture<Void> planAvailabilityFuturesAllOf = CompletableFuture.allOf(futuresArray);

		CompletableFuture<List<List<PlanAvailableResponse>>> combinedResponsesFuture = planAvailabilityFuturesAllOf
				.thenApply(v -> planAvailabilityFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

		try {
			log.info("getBenefitPlanAvailability: Fetching results from all futures");
			List<List<PlanAvailableResponse>> combinedResponse = combinedResponsesFuture.get();
			combinedResponse.stream().forEach(result::addAll);
		} catch (InterruptedException | ExecutionException e) {
			log.error("Exception occurred while getting plan availability from plan availability service: ", e);
			Thread.currentThread().interrupt();
			throw new BSSApplicationException(e,
					new BSSApplicationError("Exception occurred while fetching results from all futures."));
		}

		taskWatch.stop();
		log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
		return CompletableFuture.completedFuture(result);
	}

	private List<PlanAvailableResponse>  getBenefitPlanAvailabilityChunk(PlanAvailableRequest chunkRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		BenefitsBatchAuthorization.addAuthHeaders(headers);

		String planAvailableUrl = preparePlanAvailableServiceUri();
		String requestBody = CommonServiceHelper.objectToJsonString(chunkRequest);

		StopWatch taskWatch = new StopWatch("PlanAvailableApiCall");
		taskWatch.start();
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		ResponseEntity<ReturnResponse<List<PlanAvailableResponse>>> resp = restTemplate.exchange(planAvailableUrl,
				HttpMethod.POST, entity, getResponseType());

		ReturnResponse<List<PlanAvailableResponse>> planAvailableList = resp.getBody();
		List<PlanAvailableResponse> result = null;
		if (isApiCallSuccessful(planAvailableList)) {
			log.info(String.format("Total plans :: %s", planAvailableList.getData().size()));
			result = planAvailableList.getData();
		} else {
			throw new BSSApplicationException("Exception occurred while getting plan availability from plan availability service.");
		}
		taskWatch.stop();
		log.info(String.format("%s finished in :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));
		return result ;

	}

	private ParameterizedTypeReference<ReturnResponse<List<PlanAvailableResponse>>> getResponseType() {
		return new ParameterizedTypeReference<ReturnResponse<List<PlanAvailableResponse>>>() {
		};
	}

	private String preparePlanAvailableServiceUri() {
		String planAvailableServiceUri = BSSMessageConfig.getProperty(PlanAvailabilityURIConstants.PLAN_AVAILABILITY_API_URI);

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(SecurityConstants.COMPANY_ID, BSSApplicationConstants.TRINET_COMPANY_ID);
		pathParams.put(SecurityConstants.EMPLOYEE_ID, BSSApplicationConstants.TRINET_EMPL_ID);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(planAvailableServiceUri);

		return uriBuilder.buildAndExpand(pathParams).toString();
	}

	private boolean isApiCallSuccessful(ReturnResponse<List<PlanAvailableResponse>> planAvailableResponse) {
		return planAvailableResponse != null
				&& String.valueOf(HttpStatus.OK.value()).equals(planAvailableResponse.getStatusCode())
				&& null != planAvailableResponse.getData() && (planAvailableResponse.getError() == null);
	}

}