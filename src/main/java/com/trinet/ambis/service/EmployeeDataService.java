package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.EmployeeStrategyData;

@Service
public interface EmployeeDataService {
	/**
	 * 
	 * @param company
	 * @param mapOfBenefitProgram
	 * @param strategyGroupBenefitProgramMap
	 * @return
	 */
	boolean loadEmployeeData(Company company, Map<String, BenefitGroup> mapOfBenefitProgram,
			Map<String, Set<Long>> strategyGroupBenefitProgramMap);

	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @return
	 */
	Set<EmployeeData> getEmployeesData(Company company, long strategyId);

	/**
	 * @param company
	 * @param employeeAssignmentData
	 * @param strategyId
	 */
	void updateEmployeeAssignment(Company company, EmployeeAssignmentData employeeAssignmentData, long strategyId);

	/**
	 * 
	 * @param company
	 * @param currentStrategyId
	 * @param strategyList
	 * @return
	 */
	List<EmployeeStrategyData> getEmployeeStrategiesPlanCostData(Company company, Long currentStrategyId,
			List<Long> strategyList);

	/**
	 * 
	 * @param company
	 */
	void employeeDataSync(Company company);
}
