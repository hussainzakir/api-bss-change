package com.trinet.ambis.service.outputs;

public interface PlanCompareBuilder {

	Populator buildPlanAttributeLabels();
	
	Populator buildCurrentAndTrinetPlanAttributeValues();
	
	Populator buildCurrentPlanRate();
	
	Populator buildCurrentPlanHeadCount();
	
	Populator buildTrinetPlanRate();
	
	Populator buildTrinetPlanHeadCount();

	Populator buildPlanComparisonPagination();
}
