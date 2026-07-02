package com.trinet.ambis.service;

import java.util.List;

public interface EmployeeStrategyGroupService {

	/**
	 * Delete records for the employees in the employeeIds List
	 * and associated with passed in company code
	 *
	 * @param employeeIds
	 * @param companyCode
	 */
	void deleteEmployeeStrategyGroups(List<String> employeeIds, String companyCode);
	
	/**
	 * Update the strategy group for employees in the k1StrategyGroupIds list to the defaultStrategyGroupId
	 *
	 * @param k1StrategyGroupIds
	 * @param defaultStrategyGroupId
	 */
	void updateEmployeesToDefaultStrategyGroup(List<Long> k1StrategyGroupIds, Long defaultStrategyGroupId);

}
