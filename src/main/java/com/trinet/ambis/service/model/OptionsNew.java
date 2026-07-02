package com.trinet.ambis.service.model;

import java.util.List;

import lombok.Data;

@Data
public class OptionsNew implements Comparable<OptionsNew> {
	private String id;
	private String name;
	private List<BenefitOfferPackage> benefitOfferPackages;
	private List<AdditionalBenefitOfferPlan> additionalBenefitOfferPlans;

	@Override
	public int compareTo(OptionsNew o) {
		return -o.getId().compareTo(getId());
	}
}
