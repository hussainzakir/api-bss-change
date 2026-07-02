package com.trinet.ambis.service.prospect;

import java.util.List;
import java.util.Optional;

import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.response.CensusRes;

public interface ProspectEmployeeService {

	public Optional<List<EmployeePlansRes>> getEmployeePlans(String prospectId);

	/**
	 * Returns a list of employees based on the prospect id
	 * 
	 * @param prospectId
	 * @return List<CensusRes>
	 */
	public List<CensusRes> getEmployees(String prospectId);

}