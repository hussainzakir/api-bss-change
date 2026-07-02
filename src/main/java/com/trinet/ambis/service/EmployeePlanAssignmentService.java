package com.trinet.ambis.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.EePlanAssignment;

@Service
public interface EmployeePlanAssignmentService {

	/**
	 * Get all EePlanAssignment for given strategy ids 
	 * 
	 * @param strategyIds
	 * @return
	 */
	public List<EePlanAssignment> getEmployeePlanAssigmentBy(List<Long> strategyIds);

	/**
	 * Delete records for the employees in the employeeIds List
	 * and associated with passed in company code
	 *
	 * @param employeeIds
	 * @param companyCode
	 */
	void deleteEmployeePlanAssignment(List<String> employeeIds, String companyCode);

	/**
	 * Delete records for the employees in the employeeIds List
	 * associated with passed in strategy and benefit types
	 *
	 * @param employeeIds
	 * @param strategyId
	 * @param benTypes
	 */
	void deleteEmployeePlanAssignmentForBenTypes(Set<String> employeeIds, Long strategyId, Set<String> benTypes);
	
	/**
	 * This method copies plan assignments from source to new strategy 
	 * 
	 * @param sourceStrategyId
	 * @param targetStrategyId
	 */
	void copyEePlanAssignmentsFor(long sourceStrategyId, long targetStrategyId);
	
	/**
	 *  This method updates the coverage code in xbss_ee_plan_assignment if there is a change to coverage code.
	 *  It copies the coverage code from xbss_ee_default_plan_assignment since the coverage code there is already updated. 
	 */
	void updateEePlanAssignmentCvgCode(Set<String> emplIds);
	
	/**
	 * This method copies plan assignments from source to new strategy 
	 * 
	 * @param sourceStrategyId
	 * @param targetStrategyId
	 * @param benType
	 */
	void copyEePlanAssignmentsFor(long sourceStrategyId, long targetStrategyId, String benType);

	void saveEePlanAssignments(List<EePlanAssignment> eePlanAssignments);

}
