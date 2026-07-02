package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;

/**
 * @author schaudhari
 *
 */
@Service
public interface EmplDefaultPlanAssignmentService {

	/**
	 * This method returns all employees with default plan assignment for medical,
	 * dental and vision plans for given companyId and medicalPortfolioId
	 * 
	 * There is only single choice for dental and vision portfolio hence no
	 * portfolio id is required for dental and vision search.
	 * 
	 * @param companyId
	 * @param medicalPortfolioId
	 * @return
	 */
	public Map<String, Map<String, String>> findAllBy(long companyId, int medicalPortfolioId);

	/**
	 * Saves the default plans assignments for employees
	 * 
	 * @param dtos
	 */
	public void saveAll(List<EmplDefaultPlanAssignmentDto> dtos);

	/**
	 * Delete records for the employees in the employeeIds List and associated with
	 * passed in company code
	 *
	 * @param employeeIds
	 * @param companyCode
	 */
	void deleteEmplDefaultPlanAssignment(List<String> employeeIds, String companyCode);

	/**
	 * Delete records for the employees based on the employee ids
	 * 
	 * @param employeeIds
	 */
	void deleteEmplDefaultPlanAssignments(Set<String> employeeIds);
	
	/**
	 * Delete records for the employees default plan assignments for a given company id
	 * 
	 * @param companyId
	 * @return
	 */
	void deleteEmplDefaultPlanAssignments(long companyId);

}