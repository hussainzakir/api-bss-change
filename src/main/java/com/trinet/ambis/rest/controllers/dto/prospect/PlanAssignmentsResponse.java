package com.trinet.ambis.rest.controllers.dto.prospect;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanAssignmentsResponse {

	private String employeeId;

	private String employeeName;

	private String homeState;

	private String homeZipCode;

	private String medicalCvgCd;

	private String dentalCvgCd;

	private String visionCvgCd;

	private List<PlanAssignmentItem> planAssignment;
	private List<PlanAssignmentItem> prospectPlanAssignment;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PlanAssignmentItem {
		private String benefitPlanId;
		private String bplPlanId;
		private String benefitType;
		private String benefitPlanName;
		private BigDecimal totalCost;
	}
}
