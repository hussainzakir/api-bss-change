package com.trinet.ambis.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;


@Service
public interface HrisPlanAttributeService {

    /**
     * Get all the HrisPlanAttributes for given planIds
     *
     * @param planIds
     * @return
     */
    public CompletableFuture<List<BenefitPlanCompare>> getPlanAttributes(Set<String> planIds);

    
    /**
     * Get all the HrisPlanAttributes for given planIds filtered by a specific benefit type.
     *
     * @param planIds the set of plan IDs to fetch attributes for
     * @param benefitType the benefit type to filter the plan attributes (e.g., "Medical", "Dental", "Vision")
     * @return a CompletableFuture containing a list of BenefitPlanCompare objects matching the criteria
     */
    public CompletableFuture<List<BenefitPlanCompare>> getPlanAttributesByBenefitType(Set<String> planIds,
            String benefitType);
}
