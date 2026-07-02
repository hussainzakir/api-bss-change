package com.trinet.ambis.service.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class EmployeeStrategyPlanData {

	private Long strategyId;
	private Long groupId;
	private String groupName;
	private String benefitProgram;
	private String fundingType;
	List<BenefitPlanRateData> benefitPlans = new ArrayList<>();

}