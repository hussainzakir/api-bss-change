package com.trinet.ambis.service.model;

import lombok.Data;

import java.util.List;

@Data
public class EmpBenPlanMapping {
	String curBenPlan;
	String nextBenPlan;
	List<String> altBenPlans;
	String planType;
	String curBenProgram;

	public EmpBenPlanMapping(String planType, String curBenPlan, String nextBenPlan, List<String> altBenPlans,
			String curBenProgram) {
		super();
		this.curBenPlan = curBenPlan;
		this.nextBenPlan = nextBenPlan;
		this.altBenPlans = altBenPlans;
		this.planType = planType;
		this.curBenProgram = curBenProgram;
	}
}
