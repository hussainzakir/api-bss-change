package com.trinet.ambis.persistence.dao.ps;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.CarrierMinimumFunding;

public interface PsCompanyDao {

	/**
	 * 
	 * @param code
	 * @return
	 * @throws BSSApplicationException
	 */
	public Company getBasicCompanyDetails(String code) throws BSSApplicationException;

	/**
	 * 
	 * @param company
	 * @param effdt
	 * @return
	 * @throws BSSApplicationException
	 */
	public Company getCompanyDetailsByEffdt(Company company, Date effdt) throws BSSApplicationException;

	/**
	 * Spawn an asyncronous process to refresh the company census and enrollment mapping data.
	 * @param companyCode
	 * @param realmYearId
	 */
	public void refreshCompanyCensus( String companyCode, long realmYearId );

	/**
	 * Get the oldest created date for a company and year from the BSS census data
	 * @param companyCode
	 * @param realmYearId
	 * @return The oldest CREATED_DT timestamp on record for the company
	 */
	public Timestamp getCompanyCensusCreateDt( String companyCode, long realmYearId );

	/**
	 * 
	 * @param personid
	 * @param company
	 * @return
	 */
	boolean isBDMUser(String personid, String company);

	/**
	 * 
	 * @param company
	 * @return
	 */
	int getCompanyActualHeadCount(String company);

	/**
	 * 
	 * @param company
	 * @return
	 */
	public boolean isPayConfirm(String company);

	/**
	 * 
	 * @param personid
	 * @return
	 */
	boolean isBMGUser(String personid);

	/**
	 * 
	 * @param emplId
	 * @return
	 */
	public boolean isCSAUser(String emplId, long realmId);

	/**
	 * 
	 * @param emplid
	 * @param companyCode
	 * @return
	 */
	public boolean isActiveWithCompany(String emplid, String companyCode);

	/**
	 * 
	 * @param emplid
	 * @return
	 */
	public boolean isActiveColleague(String emplid);
	
	/**
	 * This method is to check if the USER has the TMT ROLE
	 * @param personid
	 * @return
	 */
	public boolean isTMTUser(String personid);
	
	/**
	 * This method is to check if the future Bands are available for the New Company.
	 * @param company
	 * @param effDate
	 * @return
	 */
	boolean isNewBandsAvailable(Company company, Date effDate);
	
	/**
	 * This method returns lowest cost plan amount for medical, dental and vision plans with their plan carrier ids.
	 * 
	 * @param company
	 * @return
	 */
	List<CarrierMinimumFunding> getLowestCostPlanPerPlanCarrier(Company company);
	
	/**
	 * 
	 * @param personid
	 * @return
	 */
	boolean isBenCorpAdUser(String personid);
	
	/**
	 * This method returns map of company code mapped to company name for given
	 * company codes.
	 * 
	 * @param companyCodes
	 * @return
	 */
	Map<String, String> findCompaniesNames(Set<String> companyCodes);
	
	/**
	 * This method is to check if the USER has the BROKER ROLE
	 * @param personid
	 * @return
	 */
	boolean isBenAdvisorUser(String personid, String company);

	/**
	 * Checks if given company is termed company or not
	 *
	 * @return true if termed company<br>
	 *         false if not termed company
	 */
	boolean isTermedCompany(String companyCode);
	
	boolean isTexasSitus(String companyCode, Date effDate);
	
}
