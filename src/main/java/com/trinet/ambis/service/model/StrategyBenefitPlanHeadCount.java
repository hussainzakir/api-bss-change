/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

/**
 * @author rvutukuri
 *
 */
@Data
public class StrategyBenefitPlanHeadCount {

	private String benefitPlan;
	private String planType;
	private String basePlanType;
	private String benefitPlanDescr;
	private List<StrategyCoverageLevelHeadcount> strategyCoverageLevelHeadcount;

	public StrategyBenefitPlanHeadCount() {
		super();
	}

	public StrategyBenefitPlanHeadCount(String benefitPlan, String planType, String basePlanType,
			String benefitPlanDescr) {
		this.benefitPlan = benefitPlan;
		this.planType = planType;
		this.basePlanType = basePlanType;
		this.benefitPlanDescr = benefitPlanDescr;
		this.strategyCoverageLevelHeadcount = new LinkedList<>();
	}

}
