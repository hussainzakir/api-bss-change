package com.trinet.ambis.rest.controllers.dto.planofferings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanOfferingsBenefitPlanData {

	private String planType;
	private String benefitPlan;
	private String description;

}
