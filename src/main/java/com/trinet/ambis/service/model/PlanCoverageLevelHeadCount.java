package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class PlanCoverageLevelHeadCount {
	private String groupName;
	private String planType;
	private String benefitPlan;
	private String covrgCode;
	private int headCount;
	private int hsaHeadCount;
}
