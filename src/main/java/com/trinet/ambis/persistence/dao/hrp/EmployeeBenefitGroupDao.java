package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.EmployeeBenefitGroup;
import com.trinet.ambis.service.model.EmployeeCensusStrategyGroupDetails;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.model.StrategyGroupDetails;

public interface EmployeeBenefitGroupDao {

	public void updateEmployees(long groupId, List<String> employeeIds);

	Map<String, EmployeeBenefitGroup> getEmployeeBenefitGroup(Company company);

	Map<String, EmployeeBenefitGroup> getBenefitProgramDetails(Company company);

	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	public Map<String, EmployeeStrategyGroupDetails> getEmployeeDetailsByStrategy(long strategyId);

	/**
	 * Returns a {@code Map} of benefitProgram and StrategyGroup records from
	 * XBSS_COMPANY_STRATEGY_GROUP_VW for a company
	 * 
	 * @param company
	 */
	public Map<String, Set<StrategyGroupDetails>> getStrategyGroupDetailsForCompany(Company company);

	/**
	 * Deletes records from XBSS_EMPLOYEE_STRATEGY_GROUP for passed in employee
	 * ids
	 * 
	 * @param employees
	 *            {@code Set} of employee ids
	 */
	void deleteEmployeeStrategyGroups(Set<String> employees);

	/**
	 * Deletes records from XBSS_EMPLOYEE and XBSS_EMPLOYEE_STRATEGY_GROUP for
	 * passed in employee ids
	 * 
	 * @param employees
	 *            {@code Set} of employee ids
	 */
	void deleteEmployees(Set<String> employees);
	
	/**
	 * 
	 * @param company
	 */
	public void deleteEmployeeStrategyGroups(Company company);
	
	/**
	 * Returns a {@code Map} of employee and StrategyGroup records from
	 * XBSS_EMPLOYEE_STRATEGY_GROUP for all the companies for given companyCode
	 * 
	 * @param companyCode
	 */
	public Map<String, List<EmployeeCensusStrategyGroupDetails>> getEmployeeStrategyGroupDetails(String companyCode);
	
	/**
	 * Returns a {@code List} of StrategyGroup records from
	 * XBSS_STRATEGY_GROUP
	 * 
	 * @param companyCode
	 */
	public List<EmployeeCensusStrategyGroupDetails> getStartegyGroupByCompanyAndStrategy(String companyCode);

	/**
	 * 
	 * @param strategyId
	 * @param groupId
	 * @return
	 */
	List<String> getEmployeeDetailsByStrategyAndGroup(long strategyId, long groupId);

}
