package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class EmployeeStrategyGroupDetails {

	private String emplId;
	private long emplRcd;
	private long strategyId;
	private String currentBenefitProgram;
	private String currentEligConfig1;
	private long futureStrategyGroupId;
	private long futureGroupId;
	private String futureBenefitProgram;
	private String futureGroupName;
	private String futureEligConfig1;

}
