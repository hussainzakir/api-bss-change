package com.trinet.ambis.service;


import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface TibRateService {

	/**
	 * calculate and save ER and EE costs for each employee plan
	 *
	 * @param company
	 * @param strategyId
	 */
	void saveRatesPerStrategy(Company company, long strategyId);

	/**
	 * Calculate the EE and ER cost for given prospect and given employees
	 *
	 * @param company
	 * @param strategyId
	 * @param censusData
	 */
	void saveRatesPerEmployee(Company company, long strategyId, List<ProspectCensusResponse> censusData);

	/**
	 * Calculate the EE and ER cost for given prospect and given employees
	 *
	 * @param company
	 * @param strategyId
	 * @param employeeIds
	 */
	void saveRatesPerEmployeeIds(Company company, long strategyId, Set<String> employeeIds);

	/**
	 * Calculate the EE and ER cost for given prospect and given employees
	 *
	 * @param company
	 * @param strategyId
	 * @param employeeIds
	 * @param benefitTypes
	 */
	void saveRatesPerEmployeeIds(Company company, long strategyId, Set<String> employeeIds, List<String> benefitTypes);

	/**
	 * calculate and return the total rate for the employee, plan and coverage level
	 *
	 * @param company
	 * @param employeeId
	 * @param planId
	 * @param covgLevelCode
	 */
	BigDecimal getRateForEmployee(Company company, String employeeId, String planId, String covgLevelCode, String benefitType);

}