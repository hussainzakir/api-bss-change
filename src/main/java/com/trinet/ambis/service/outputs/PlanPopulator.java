package com.trinet.ambis.service.outputs;

public interface PlanPopulator {
	
	Populator populateCurrentPlan();
	
	Populator populateCurrentPlanRate();
	
	Populator populateCurrentPlanHeadCount();
	
	Populator populateTrinetPlan();
	
	Populator populateTrinetPlanRate();
	
}
