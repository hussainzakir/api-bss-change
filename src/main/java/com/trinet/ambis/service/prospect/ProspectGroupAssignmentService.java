package com.trinet.ambis.service.prospect;

import java.util.Set;

import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeData;

public interface ProspectGroupAssignmentService {

	/** Returns the employee group assignments
	 * @param prospectId
	 * @return Set<EmployeeData>
	 */
	public Set<EmployeeData> getEmployeeGroupAssignments(String prospectId);
	
	/**
	 * Updates groups to employees assignment
	 *
	 * @param strategyId
	 * @param employeeAssignmentData
	 */
	void updateEmployeeGroupAssignment(long strategyId, EmployeeAssignmentData employeeAssignmentData);

}