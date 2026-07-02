package com.trinet.ambis.service.model;

import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class AdditionalBenefitsCategoryOffer {
	@JsonUnwrapped
	private BenefitOfferSummary summary;
	boolean mandatory = false;
	private Set<AdditionalBenefitPlan> additionalBenefitPlans;

	public BenefitOfferSummary getSummary() {
		if (summary == null) {
			summary = new BenefitOfferSummary();
		}
		return summary;
	}

	public void setSummary(BenefitOfferSummary summary) {
		this.summary = summary;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * @return the additionalBenefitPlan
	 */
	public Set<AdditionalBenefitPlan> getAdditionalBenefitPlans() {
		if (additionalBenefitPlans == null) {
			additionalBenefitPlans = new TreeSet<>();
		}
		return additionalBenefitPlans;
	}

	/**
	 * @param additionalBenefitPlan
	 *            the additionalBenefitPlan to set
	 */
	public void setAdditionalBenefitPlans(Set<AdditionalBenefitPlan> additionalBenefitPlans) {
		this.additionalBenefitPlans = additionalBenefitPlans;
	}

}
