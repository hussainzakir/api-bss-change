package com.trinet.ambis.service;

import com.trinet.ambis.client.DefaultPlanMappingServiceClient.PlanMappingResponse;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.model.StrategyData;
import java.util.List;

public interface ProspectStrategyService {

	/**
	 * This method returns the current strategy for the prospect
	 * 
	 * @param prospectId
	 * @return
	 */
	StrategyData getProspectCurrentStrategy(String prospectId);

	/**
	 * This method creates the Trinet Strategy for Prospect Company.
	 * 
	 * @param company
	 */
	void createDefaultTrinetStrategy(Company company, long selectedCarrier, List<PlanMappingResponse> planMappingResponse);

	ModelCompareStrategyCost getProspectCurrentStrategyCosts(String prospectId);

}