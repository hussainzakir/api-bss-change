package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class StrategyGroupDetails {

	private long strategyGroupId;
	private long strategyId;
	private long groupId;
	private String benefitProgram;
	private String groupName;
	private String groupType;
	private String status;
	private boolean defaultGroup;
	private long headcount;
	private String waitingPeriod;
	private String eligConfig1;

}
