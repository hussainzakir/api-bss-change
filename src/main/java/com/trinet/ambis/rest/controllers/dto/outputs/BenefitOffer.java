package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BenefitOffer {

	private String benefitType;
	private String fundingBasePlan;
	private String fundingType;
	private FundingDetail employeePlanValue;
	private FundingDetail employeeSpousePlanValue;
	private FundingDetail employeeChildrenPlanValue;
	private FundingDetail familyPlanValue;
	private boolean planLevelFundingOverride;
}
