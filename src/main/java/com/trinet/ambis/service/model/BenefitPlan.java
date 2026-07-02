package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BenefitPlan implements java.io.Serializable, Comparable<BenefitPlan> {

	private static final long serialVersionUID = 1L;
	String id;
	String planType;
	Long planCarrierId;
	String name;
	BigDecimal estimatedTotalCost;
	BigDecimal annualCap;
	boolean employeePaid;
	boolean highDeductible;
	boolean premium;
	boolean mandatory;
	boolean restrictedState;
	boolean nationalPlan;
	String optionalPlans;
	boolean ppoPlan;
	boolean widelyAvailablePlan;
	boolean mandatoryExcluded;
	String planCategory;
	Long strategyId;
	@JsonIgnore
	private long headCount;
	@JsonIgnore
	private String vendorId;
	private long planSelectionId;

	private Set<String> crossRefPlans;
	private List<String> offeredStates;
	private List<PlanContribution> contributions = new ArrayList<>();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BenefitPlan other = (BenefitPlan) obj;
		if (id == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!id.equals(other.getId())) {
			return false;
		}

		return true;
	}

	@Override
	public int compareTo(BenefitPlan obj) {
		if (this == obj)
			return 0;
		if (id.equals(obj.getId()))
			return 0;
		return -1;

	}

}
