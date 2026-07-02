package com.trinet.ambis.rest.controllers.dto.outputs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeCostSummary {

	private EmployeeDetails employee;
	private BenefitTypeTotal currentPlan;
	private BenefitTypeTotal triNetPlan;
	private int costDiff;
	private boolean pageBreak;
	private boolean wordWrap;

}
