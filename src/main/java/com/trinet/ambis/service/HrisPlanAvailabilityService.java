package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.aop.CacheKey;
import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;

@Service
public interface HrisPlanAvailabilityService {

    /**
     * @param hrisPlanRequest
     * @return
     */
    List<HrisPlanResponse> getBenefitPlanAvailability(
            HrisPlanRequest hrisPlanRequest, @CacheKey String cacheKey);
}
