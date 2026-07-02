package com.trinet.ambis.service.impl;

import java.util.List;

import com.trinet.ambis.aop.BSSCacheable;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.service.HrisPlanAvailabilityService;
import com.trinet.ambis.service.model.planAvailability.*;
import com.trinet.ambis.util.HrisPlanServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import static com.trinet.ambis.common.BSSApplicationConstants.TTL_FOR_OMS_BENEFIT_PLAN_RATES;

@Service
@Slf4j
public class HrisPlanAvailabilityServiceImpl implements HrisPlanAvailabilityService {

    @Autowired
    private HrisPlanServiceRestClient hrisPlanServiceRestClient;

    @BSSCacheable(objectType = CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, ttl = TTL_FOR_OMS_BENEFIT_PLAN_RATES)
    public List<HrisPlanResponse> getBenefitPlanAvailability(HrisPlanRequest request, @CacheKey String cacheKey) {
        if (request.getBenefitsType() == null || !BSSApplicationConstants.PRIMARY_PLAN_TYPE_NAMES.contains(request.getBenefitsType())) {
            return List.of();
        }
        List<HrisPlanResponse> emptyPlanList = List.of();
        var myBean = new ParameterizedTypeReference<List<HrisPlanResponse>>() {};

        List<HrisPlanResponse> plans = hrisPlanServiceRestClient.getBenefitPlanAvailability(request, myBean);
        return plans != null ? plans : emptyPlanList;
    }
}

