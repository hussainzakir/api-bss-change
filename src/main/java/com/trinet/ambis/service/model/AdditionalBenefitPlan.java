/**
 * 
 */
package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author rvutukuri
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalBenefitPlan implements java.io.Serializable, Comparable<AdditionalBenefitPlan> {
	private static final long serialVersionUID = 1L;

	private String id;
	private String description;
	private String region;
	private BigDecimal planCost;
	@JsonInclude(JsonInclude.Include.NON_NULL) 
	private BigDecimal monthlyTotalCost;
	private BigDecimal annualCap;
	private String planType;
	private boolean standAlone;
	@JsonIgnore
	private boolean employeePaidOption;
	@JsonIgnore
	private boolean taxFreeOption;
	@JsonInclude(Include.NON_EMPTY)
	private List<DisabilityBenefitOptionPlans> optionPlans;
	private String offeredGroupType;
	private Long displaySeq;

	public AdditionalBenefitPlan() {
		super();
	}
	
	public AdditionalBenefitPlan(AdditionalBenefitPlan original) {
		this.id = original.id;
		this.description = original.description;
		this.region = original.region;
		this.planCost = original.planCost;
		this.monthlyTotalCost = original.monthlyTotalCost;
		this.annualCap = original.annualCap;
		this.planType = original.planType;
		this.standAlone = original.standAlone;
		this.employeePaidOption = original.employeePaidOption;
		this.taxFreeOption = original.taxFreeOption;
		this.offeredGroupType = original.offeredGroupType;
		this.displaySeq = original.displaySeq;
		
		if(null != original.getOptionPlans()) {
			List<DisabilityBenefitOptionPlans> optionPlan = new ArrayList<>(original.getOptionPlans().size());
			for (DisabilityBenefitOptionPlans disabilityBenefitOptionPlans : original.getOptionPlans()) {
				optionPlan.add(new DisabilityBenefitOptionPlans(disabilityBenefitOptionPlans));
			}
			this.setOptionPlans(optionPlan);
		}
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @param region
	 *            the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/**
	 * @return the optionPlans
	 */
	public List<DisabilityBenefitOptionPlans> getOptionPlans() {
		return optionPlans;
	}

	/**
	 * @param optionPlans
	 *            the optionPlans to set
	 */
	public void setOptionPlans(List<DisabilityBenefitOptionPlans> optionPlans) {
		this.optionPlans = optionPlans;
	}

	/**
	 * @return the planType
	 */
	public String getPlanType() {
		return planType;
	}

	/**
	 * @param planType
	 *            the planType to set
	 */
	public void setPlanType(String planType) {
		this.planType = planType;
	}

	/**
	 * @return the planCost
	 */
	public BigDecimal getPlanCost() {
		return planCost;
	}

	/**
	 * @param planCost
	 *            the planCost to set
	 */
	public void setPlanCost(BigDecimal planCost) {
		this.planCost = planCost;
	}
	
	public BigDecimal getMonthlyTotalCost() {
		return monthlyTotalCost;
	}

	public void setMonthlyTotalCost(BigDecimal monthlyTotalCost) {
		this.monthlyTotalCost = monthlyTotalCost;
	}

	/**
	 * @return the annualCap
	 */
	public BigDecimal getAnnualCap() {
		return annualCap;
	}

	/**
	 * @param annualCap
	 *            the annualCap to set
	 */
	public void setAnnualCap(BigDecimal annualCap) {
		this.annualCap = annualCap;
	}

	/**
	 * @return the standAlone
	 */
	public boolean isStandAlone() {
		return standAlone;
	}

	/**
	 * @param standAlone
	 *            the standAlone to set
	 */
	public void setStandAlone(boolean standAlone) {
		this.standAlone = standAlone;
	}

	/**
	 * @return the employeePaidOption
	 */
	@JsonIgnore
	public boolean isEmployeePaidOption() {
		return employeePaidOption;
	}

	/**
	 * @param employeePaidOption
	 *            the employeePaidOption to set
	 */
	public void setEmployeePaidOption(boolean employeePaidOption) {
		this.employeePaidOption = employeePaidOption;
	}

	/**
	 * @return the taxFreeOption
	 */
	public boolean isTaxFreeOption() {
		return taxFreeOption;
	}

	/**
	 * @param taxFreeOption
	 *            the taxFreeOption to set
	 */
	public void setTaxFreeOption(boolean taxFreeOption) {
		this.taxFreeOption = taxFreeOption;
	}
	
	public String getOfferedGroupType() {
		return offeredGroupType;
	}

	public void setOfferedGroupType(String offeredGroupType) {
		this.offeredGroupType = offeredGroupType;
	}

	

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
		AdditionalBenefitPlan other = (AdditionalBenefitPlan) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(AdditionalBenefitPlan obj) {
		if (this == obj)
			return 0;
		if (id.equals(obj.getId()))
			return 0;
		return -1;

	}

	@Override
	public String toString() {
		return "AdditionalBenefitPlan [id=" + id + ", description=" + description + ", region=" + region + ", planCost="
				+ planCost + ", monthlyTotalCost=" + monthlyTotalCost + ", annualCap=" + annualCap + ", planType="
				+ planType + ", standAlone=" + standAlone + ", employeePaidOption=" + employeePaidOption
				+ ", taxFreeOption=" + taxFreeOption + ", optionPlans=" + optionPlans + ", offeredGroupType="
				+ offeredGroupType + ", displaySeq=" + displaySeq + "]";
	}


	public Long getDisplaySeq() {
		return displaySeq;
	}

	public void setDisplaySeq( Long displaySeq ) {
		this.displaySeq = displaySeq;
	}

}
