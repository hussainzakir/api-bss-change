package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class AdditionalBenefitOfferPlan implements Comparable<AdditionalBenefitOfferPlan> {

	private String type;
	private String benefitPlan;

	@Override
	public int compareTo(AdditionalBenefitOfferPlan o) {
		return -o.getType().compareTo(getType());
	}
}
