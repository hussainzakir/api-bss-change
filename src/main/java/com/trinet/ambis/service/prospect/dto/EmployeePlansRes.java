package com.trinet.ambis.service.prospect.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePlansRes {

	private String employeeId;

	private List<BenefitPlan> benefitPlans;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class BenefitPlan {

		@JsonProperty("benefitType")
		private String benefitTypeCode;

		@JsonProperty("benefitPlan")
		private String benefitPlanId;
		
		@JsonProperty("coverageCode")
		private String coverageCode;
		
		@JsonProperty("bplPlanId")
		private String bplPlanId;

	}

}