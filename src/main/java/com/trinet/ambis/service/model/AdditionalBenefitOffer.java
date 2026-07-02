package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalBenefitOffer implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonUnwrapped
	private BenefitOfferSummary summary;
	private List<AdditionalBenefitPlan> additionalBenefitPlans = new ArrayList<>();

	/**
	 * @return the summary
	 */
	public BenefitOfferSummary getSummary() {
		return summary;
	}

	/**
	 * @param summary
	 *            the summary to set
	 */
	public void setSummary(BenefitOfferSummary summary) {
		this.summary = summary;
	}
	/**
	 * @return the additionalBenefitPlans
	 */
	public List<AdditionalBenefitPlan> getAdditionalBenefitPlans() {
		return additionalBenefitPlans;
	}

	/**
	 * @param additionalBenefitPlans
	 *            the additionalBenefitPlans to set
	 */
	public void setAdditionalBenefitPlans(List<AdditionalBenefitPlan> additionalBenefitPlans) {
		this.additionalBenefitPlans = additionalBenefitPlans;
	}

}
