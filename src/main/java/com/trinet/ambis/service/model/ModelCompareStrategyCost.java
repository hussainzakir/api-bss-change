package com.trinet.ambis.service.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.trinet.ambis.persistence.model.BenefitGroup;

/**
 * @author hliddle
 */

public class ModelCompareStrategyCost {

	private long strategyId;

	private List<ModelComparePlanTypeCost> planTypeCosts = new ArrayList<>();

	@JsonInclude(Include.NON_EMPTY)
	private List<BenefitGroup> benefitGroups = new ArrayList<>();

	/**
	 * @return the strategyId
	 */
	public long getStrategyId() {
		return strategyId;
	}

	/**
	 * @param strategyId
	 *            the strategyId to set
	 */
	public void setStrategyId(long strategyId) {
		this.strategyId = strategyId;
	}

	public List<ModelComparePlanTypeCost> getPlanTypeCosts() {
		return planTypeCosts;
	}

	public void setPlanTypeCosts(List<ModelComparePlanTypeCost> planTypeCosts) {
		this.planTypeCosts = planTypeCosts;
	}

	public List<BenefitGroup> getBenefitGroups() {
		return benefitGroups;
	}

	public void setBenefitGroups(List<BenefitGroup> benefitGroups) {
		this.benefitGroups = benefitGroups;
	}

}