package com.trinet.ambis.service;

import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyGroupDetails;

public interface EmployeeBenefitGroupService {

	/**
	 * Inserts records into XBSS_EMPLOYEE_STRATEGY_GROUP for passed in employee
	 * ids and StrategyGroupDetails
	 * 
	 * @param employeeStrategyGroups
	 */
	void insertNewEmployeeStrategyGroups(Map<String, Set<StrategyGroupDetails>> employeeStrategyGroups);

	/**
	 * 
	 * @param company
	 * @param benefitProgramStrategyGroupId
	 * @param strategyId
	 */
	void loadStrategyEmployeeData(Company company, Map<String, Long> benefitProgramStrategyGroupId, long strategyId);

}
