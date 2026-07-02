package com.trinet.ambis.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class FlexRateResponse implements Serializable {
    private String rateGroupId;
    private String rateType;
    private List<PlanByBenefitType> plansByBenefitType;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanByBenefitType implements Serializable {
        private String benefitType;
        private List<PlansByPlanType> plansByPlanType;
    }

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlansByPlanType implements Serializable {
        private String planType;
        private String dpPlanType;
        private List<PlanRate> plans;
    }
}
