package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.service.HrisPlanAttributeService;
import com.trinet.ambis.service.model.HrisPlanAttributeRequest;
import com.trinet.ambis.util.HrisPlanServiceRestClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HrisPlanAttributeServiceImpl implements HrisPlanAttributeService {

    @Autowired
    private HrisPlanServiceRestClient hrisPlanServiceRestClient;

    private static final int PLAN_IDS_PARTITION_SIZE = 50;
    
    @Override
    public CompletableFuture<List<BenefitPlanCompare>> getPlanAttributes(Set<String> planIds) {
        return getPlanAttributesByBenefitType(planIds, BSSApplicationConstants.MEDICAL);
    }

    @Override
    public CompletableFuture<List<BenefitPlanCompare>> getPlanAttributesByBenefitType(Set<String> planIds,
            String benefitType) {
        List<String> planIdsList = new ArrayList<>(planIds);
        log.info("Calling HRIS service to get plan attributes");
        if (CollectionUtils.isEmpty(planIdsList)) {
            return CompletableFuture.completedFuture(List.of());
        }
        var myBean = new ParameterizedTypeReference<List<BenefitPlanCompare>>() {};

        List<BenefitPlanCompare> emptyPlanList = List.of();
        List<List<String>> planIdsPartitions = Lists.partition(planIdsList, PLAN_IDS_PARTITION_SIZE);
        
        // Create a list of CompletableFutures for each chunk
		List<CompletableFuture<List<BenefitPlanCompare>>> hrisPlanAttributesFutures = planIdsPartitions.stream()
				.map(planIdsInChunk -> {
					var hrisApiRequest = HrisPlanAttributeRequest.builder().planIds(planIdsInChunk)
							.benefitType(benefitType).build();
					return CompletableFuture
							.supplyAsync(() -> hrisPlanServiceRestClient.getPlanAttributes(hrisApiRequest, myBean))
							.thenApply(plans -> plans != null ? plans : emptyPlanList).exceptionally(ex -> {
								log.error("Error during HRIS API call: {}", ex.getMessage(), ex);
								return emptyPlanList;
							});
				}).collect(Collectors.toList());

        // Combine all futures into one list
        return CompletableFuture.allOf(hrisPlanAttributesFutures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> hrisPlanAttributesFutures.stream()
                        .flatMap(future -> future.join().stream())
                        .collect(Collectors.toList()));
    }
}
