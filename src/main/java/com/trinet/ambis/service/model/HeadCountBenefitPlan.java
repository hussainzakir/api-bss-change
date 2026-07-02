/**
 * 
 */
package com.trinet.ambis.service.model;
import java.util.HashMap;
import java.util.Map;

import com.trinet.ambis.enums.CoverageCodesEnums;

import lombok.Data;
/**
 * @author rvutukuri
 *
 */
@Data
public class HeadCountBenefitPlan {
    private String benefitPlanId;
    private String planType;
    private long planCarrierId;
    private Map<String, Long> coverageLevelHeadCounts = new HashMap<>();
    private Map<String, Long> hsaCoverageLevelHeadCounts = new HashMap<>();
    private HeadCountBenefitPlan(HeadCountBenefitPlanBuilder builder) {
        this.benefitPlanId = builder.benefitPlanId;
        this.planType = builder.planType;
        this.planCarrierId = builder.planCarrierId;
        if (builder.populateZeroCvgLvlHeadCounts) {
            this.coverageLevelHeadCounts = createNewCoverageHeadCountMap();
            this.hsaCoverageLevelHeadCounts = createNewCoverageHeadCountMap();
        }
    }
    public static class HeadCountBenefitPlanBuilder {
        String benefitPlanId;
        String planType;
        long planCarrierId;
        boolean populateZeroCvgLvlHeadCounts;
        Map<String, Long> coverageLevelHeadCounts = new HashMap<>();
        Map<String, Long> hsaCoverageLevelHeadCounts = new HashMap<>();        
        
        public HeadCountBenefitPlanBuilder() {
        }
        public HeadCountBenefitPlanBuilder benefitPlanId(String benefitPlanId) {
            this.benefitPlanId = benefitPlanId;
            return this;
        }
        public HeadCountBenefitPlanBuilder planType(String planType) {
            this.planType = planType;
            return this;
        }
        public HeadCountBenefitPlanBuilder planCarrierId(long planCarrierId) {
            this.planCarrierId = planCarrierId;
            return this;
        }
        public HeadCountBenefitPlanBuilder coverageLevelHeadCounts(Map<String, Long> coverageLevelHeadCounts) {
            this.coverageLevelHeadCounts =coverageLevelHeadCounts;
            return this;
        }
        public HeadCountBenefitPlanBuilder hsaCoverageLevelHeadCounts(Map<String, Long> hsaCoverageLevelHeadCounts) {
            this.hsaCoverageLevelHeadCounts = hsaCoverageLevelHeadCounts;
            return this;
        }
        public HeadCountBenefitPlanBuilder populateZeroCvgLvlHeadCounts(boolean populateZeroCvgLvlHeadCounts) {
            this.populateZeroCvgLvlHeadCounts = populateZeroCvgLvlHeadCounts;
            return this;
        }
        public HeadCountBenefitPlan build() {
            return new HeadCountBenefitPlan(this);
        }
    }
    private Map<String, Long> createNewCoverageHeadCountMap() {
        Map<String, Long> coverageHeadCountMap = new HashMap<>();
        coverageHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), 0L);
        coverageHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), 0L);
        coverageHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), 0L);
        coverageHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), 0L);
        return coverageHeadCountMap;
    }
}
