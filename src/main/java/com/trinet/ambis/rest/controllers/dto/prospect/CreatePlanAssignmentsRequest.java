package com.trinet.ambis.rest.controllers.dto.prospect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlanAssignmentsRequest {

	private String employeeId;
	private String benefitPlanId;
	private String benefitType;
	private String coverageCode;
	private long portfolioId;

}
