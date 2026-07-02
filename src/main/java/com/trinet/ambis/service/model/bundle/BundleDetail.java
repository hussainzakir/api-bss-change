package com.trinet.ambis.service.model.bundle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleDetail {
    private Integer benefitBundleId;
    private Integer primaryCarrierId;
    private List<BundlePlanDetail> bundlePlanDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BundlePlanDetail {
        private String planId;
        private List<PlanCostRequest> cvgLevelCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanCostRequest {
        private String covrgCd;
        private BigDecimal totalCost;
    }
}
