package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.OLPProcessStatus;

public interface HrpDao {

	/**
	 * Returns the passed in employee's email address.
	 * 
	 * @param companyCode
	 * @param emplId
	 * @return
	 */
	public String getEmplEmail(String companyCode, String emplId);

	/**
	 * Returns a set of client BDM email addresses.
	 * 
	 * @param companyCode
	 * @return
	 */
	public Set<String> getBDMEmails(String companyCode);

	/**
	 * Returns a set of email addresses for employees of the passed in company
	 * with the passed in role
	 * 
	 * @param companyCode
	 * @param role
	 * @return
	 */
	public Set<String> getRoleEmails(String companyCode, String role);

	/**
	 * Returns the map of company and count of an active company employees setup
	 * as a BDM with an email
	 * 
	 * @param companies
	 * @return
	 */
	public Map<String, Integer> getBDMCount(List<String> companies);
	
	/**
	 * Returns the map of company and count of an active company employees setup
	 * as a BEN_CORP_AD with an email
	 * 
	 * @param companies
	 * @return
	 */
	public Map<String, Integer> getBenCorpAdminCount(List<String> companies);
	
	/**
	 * Returns the map of company and count of an active company employees setup
	 * with the passed in role with an email
	 * 
	 * @param companies
	 * @param role
	 * @return
	 */
	public Map<String, Integer> getRoleEmailCount(List<String> companies, String role);

	public Map<String, String> getCovrgCdMap();

	public void refreshPlanView();

	/**
	 * This method returns all roles which are allowed the access given TriNet
	 * Gateway application.
	 * 
	 * @param appkey
	 * @return
	 */
	Set<String> getGatewayAppAccessibleRolesBy(String appKey);

	/**
	 * This method returns the maximum status for given company.
	 * 
	 * @param company
	 * @return
	 */
	int getOLPStatus(Company company);
	
	/**
	 * This method returns OLPProcess status for given company.
	 * 
	 * @param company
	 * @return OLPProcessStatus
	 */
	OLPProcessStatus getOlpHiringCompletedStatus(Company company);
	
	/**
	 * This method returns ZIP codes and states for given ZIP codes
	 * @param zipCodes
	 * @return
	 */
	Map<String,String> getZipCodesAndStatesBy(List<String> zipCodes);
	
	/**
	 * This method returns the mapping of old plan year plan to new plan year plan for the given old and new plan year based on the provided effective dates for each plan year
	 * 
	 * @param oldPlanYear
	 * @param oldPlanYearEffDt
	 * @param newPlanYear
	 * @param newPlanYearEffDt
	 * @param applySitus
	 * @return
	 */
	Map<String, String> getCurrentFutureBenefitPlansMap(Long oldPlanYear, Date oldPlanYearEffDt, Long newPlanYear,
			Date newPlanYearEffDt, boolean applySitus);

	/**
	 * Deletes all strategies and strategy-related child records for the given company
	 * using an Oracle PL/SQL cursor loop in a single DB round-trip.
	 *
	 * @param companyId the BSS company ID
	 */
	void deleteStrategiesByCompanyId(long companyId);

	/**
	 * Deletes remaining company-level records (groups, employees, regions,
	 * scheduled mid-year funding, default plan assignments) for the given company.
	 * Intended to be called after deleteStrategiesByCompanyId in the same transaction.
	 *
	 * @param companyId the BSS company ID
	 */
	void deleteCompanyDataByCompanyId(long companyId);

	/**
	 * Deletes the company row from XBSS_COMPANY for the given company ID.
	 *
	 * @param companyId the BSS company ID
	 */
	void deleteCompanyByCompanyId(long companyId);

}