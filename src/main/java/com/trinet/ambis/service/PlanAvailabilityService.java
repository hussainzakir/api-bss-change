package com.trinet.ambis.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;

@Service
public interface PlanAvailabilityService {

	/**
	 *
	 * @param planAvailableRequest
	 * @return
	 */
	CompletableFuture<List<PlanAvailableResponse>> getBenefitPlanAvailability(
			PlanAvailableRequest planAvailableRequest);
}