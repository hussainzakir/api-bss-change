package com.trinet.ambis.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCensusStrategyGroupDetails {

	private String emplId;
	private long strategyId;
	private long strategyGroupId;
	private String benefitProgram;
	private String groupType;
	private String groupDesc;
}