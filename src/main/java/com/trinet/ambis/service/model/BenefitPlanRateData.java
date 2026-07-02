package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;

import lombok.Data;

@Data
public class BenefitPlanRateData {
	private String planId;
	private String planType;
	private String planName;
	private String coverageLevel;
	private String coverageLevelName;
	private String coverageElect;
	private BigDecimal employeeContribution;
	private BigDecimal employerContribution;
	private Long portfolioId;
	private String mappingFlag;
	private boolean mirrorPlanFlag;
	private boolean offered;

	/**
	 * This constructor exists for JUnit test classes that reconstruct objects from json strings
	 */
	public BenefitPlanRateData() {
		super();
	}

	public BenefitPlanRateData(String benefitPlan, String planType, String planName, String coverageLevel,
			String coverageLevelName, BigDecimal eeContribution, BigDecimal erContribution, Long currentPortfolioId,
			String coverageElect, String mappingFlag, boolean mirrorPlanFlag, boolean offered) {
		this.planId = benefitPlan;
		this.planType = planType;
		this.planName = planName;
		this.coverageLevel = coverageLevel;
		this.employeeContribution = eeContribution;
		this.employerContribution = erContribution;
		this.portfolioId = currentPortfolioId;
		this.coverageElect = coverageElect;
		this.mappingFlag = mappingFlag;
		this.mirrorPlanFlag = mirrorPlanFlag;
		this.coverageLevelName = coverageLevelName;
		this.offered = offered;
	}

	public BenefitPlanRateData(String planType, BigDecimal eeContribution, BigDecimal erContribution,
			String coverageElect, String mappingFlag, boolean mirrorPlanFlag, boolean offered) {
		this.planType = planType;
		this.employeeContribution = eeContribution;
		this.employerContribution = erContribution;
		this.coverageElect = coverageElect;
		this.mappingFlag = mappingFlag;
		this.mirrorPlanFlag = mirrorPlanFlag;
		this.offered = offered;
	}


	public String getBasePlanTypeName() {
		String basePlanType = null;
		if (BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE.equals(this.planType)) {
			basePlanType = BSSApplicationConstants.VISION_PLAN_TYPE;
		} else if (BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE.equals(planType)) {
			basePlanType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		} else {
			basePlanType = this.planType;
		}

		return PlanTypesEnum.getName(basePlanType);
	}

}