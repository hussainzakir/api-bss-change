package com.trinet.ambis.service.model.plancompare;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BenefitPlanCompare {

	private String planId;
	private String name;
	private String planName;
	private String benefitType;
	private int carrierId;
	private String carrier;
	private String carrierLogoUrl;
	private List<PlanCompareTemplate> template;

}
