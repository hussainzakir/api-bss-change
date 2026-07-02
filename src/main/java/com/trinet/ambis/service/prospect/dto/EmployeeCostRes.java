package com.trinet.ambis.service.prospect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCostRes {

    private String benefitTypeCode;

    private List<EmployeePlanContribution> employeePlanContribution;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmployeePlanContribution {

        private String employeeId;

        private String firstName;

        private String lastName;

        private String state;

        private String covgLevel;

        private int groupId;

        private String groupName;

        private PlanContribution planContribution;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlanContribution {

        private int benefitPlanId;
        
        private String bplPlanId;

        private String benefitPlanName;

        private BigDecimal eeCost;

        private BigDecimal erCost;

        private BigDecimal totalCost;

    }

}
