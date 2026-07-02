package com.trinet.ambis.persistence.dao.ps;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.model.Company;

@Repository
public interface PsDao {

	/**
	 * @param emplId
	 * @return
	 */
	String getEmployeeFirstName(String emplId);
	
	/**
	 * @param emplId
	 * @return
	 */
	String getEmployeeLastName(String emplId);
	
	/**
	 * @param emplIds
	 * @return
	 */
	String getEmployeeFullName(String emplId);
	
	/**
	 * @param emplIds
	 * @return
	 */
	Map<String, String> getEmployeesFullName(Set<String> emplIds);

	/**
	 * 
	 * @param company
	 * @return
	 */
	List<String> getAssignmentAddresses(Company company);

	/**
	 * 
	 * @return
	 */
	String getDatabase();

	/**
	 * 
	 * @param quarter
	 * @param relamYearId
	 * @param payrollCutOffDate
	 * @return
	 */
	List<String> getUnsubmittedClients(String quarter, Long relamYearId, Date payrollCutOffDate, String termStatus);

	/**
	 * 
	 * @param code
	 * @return
	 */
	List<String> getNewClientAddresses(Company company);
	
	/**
	 * 
	 * @param peoId
	 * @param quarter
	 * @param realmYearId
	 * @param payrollCutOffDate
	 * @return
	 */
	List<String> getPreLoadClients(String peoId, String quarter, Long realmYearId, Date payrollCutOffDate);
	

	/**
	 * @param effDate
	 * @return
	 */
	Map<String, BigDecimal> getHsaMaximumsByEffDate(Date effDate);
	
}