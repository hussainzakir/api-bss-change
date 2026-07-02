package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;

public interface EmployeeDataDao {
	/**
	 * This method is for getting the employee details from PS.
	 * 
	 * @param company
	 * @param isVendorMappingOn
	 * @return
	 */
	 Map<String, Employee> getEmployeesByCompany(Company company);

	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	Set<Employee> getEmployeeGroupDetailsByStrategy(long strategyId);

	/**
	 * @param company
	 * @param currentStrategyId
	 * @return
	 */
	List<Object[]> getEmployeeStrategyPlanData(Company company, Long currentStrategyId);

	/**
	 * @param company
	 * @param currentStrategyId
	 * @param effDate
	 * @return
	 */
	List<Object[]> getEmployeeCensusStrategyPlanData(Company company, Long currentStrategyId, Date effDate);


	/**
	 * This method will return the map of all the employees, plan type and their current, realm plan
	 * mapping plan and alternate plan (vendor switching option) for the given realm/plan-year
	 * 
	 * @param companyCode
	 * @param realmYearId
	 * @return
	 */
	MultiKeyMap getEmpPlanMapping(String companyCode, long realmYearId);
	

	/**
	 * This method will return the map of all employees enrolled in a mirror plan, plan type and the FPL cost.
	 * This is only retrieved for TN III clients as they are the only exchange using mirror plans.
	 * 
	 * @param company
	 * @return
	 */
	MultiKeyMap getMirrorPlanEnrolledEmployees(Company company);
	
	/**
	 * @param company
	 * @return
	 */
	List<String> getEmployeesBy(String companyCode);

}
