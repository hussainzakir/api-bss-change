package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.CommonData;
import com.trinet.ambis.service.model.CompanyRealmData;
public interface CompanyService {

	/**
	 * This method is for getting common data of a company
	 * 
	 * @param code
	 * @param emplid
	 * @param benExchange
	 * @param strategyAccessed
	 * @return
	 */
	CommonData getCompanyCommonData(String code, String emplid, BenExchngEnums benExchange, boolean strategyAccessed);

	/**
	 * This method is for getting company details based on company code.
	 * 
	 * @param code
	 * @return
	 */
	Company getCompanyDetails(String code);

	/**
	 * Get all the company ID values for a given company code and exchange
	 * 
	 * @param companyCode
	 * @param benExchange
	 * @return a list of company ID
	 */
	List<Long> getIdsByCodeAndExchange(String companyCode, BenExchngEnums benExchange);

	/**
	 * This method is for getting company details based on company code
	 * 
	 * @param code
	 * @param history
	 * @param benExchange TODO
	 * @return
	 */
	Company getCompanyDetails(String code, boolean history, String emplId, BenExchngEnums benExchange);

	/**
	 * This method is for getting company details.
	 * 
	 * @param companyId
	 * @param effdt
	 * @param currentRealmPlanYearId
	 * @return
	 */
	Company getCompanyDetailsEffdt(String companyId, Date effdt, long currentRealmPlanYearId);

	/**
	 * This method is for creating or updating a company.
	 * 
	 * @param company
	 * @return
	 */
	Company createUpdateCompany(Company company);

	/**
	 * This method is for getting all the companies for a realm year.
	 * 
	 * @param realmYearId
	 * @return
	 */
	List<Company> getCompaniesByReamYear(Long realmYearId);
	
	/**
	 * 
	 * @param companyCode
	 * @param effdt
	 * @param currentRealmPlanYearId
	 * @return
	 */
	Company getCompanyDetailsEffdtPlanRates(String companyCode, Date effdt, long currentRealmPlanYearId);
	
	
	/**
	 * This method returns company names from PS for given company codes.
	 * 
	 * @param companyCodes
	 * @return
	 */
	Map<String, String> findCompaniesNames(Set<String> companyCodes);
	
	/**
	 * This method returns a list of the passed in company's current and future (if
	 * available) plan year information
	 * 
	 * @param companyCode
	 * @param emplid
	 * @return
	 */
	List<CompanyRealmData> getCompanyPlanYearData(String companyCode, String emplid);
	

	/**
	 * Request refresh of company census and enrollment mapping data
	 * @param companyCode
	 * @param realmYearId
	 */
	void refreshCompanyCensus( String companyCode, long realmYearId );
	
	/**
	 * This method refreshes company census
	 * 
	 * @param companyCode
	 * @param realmYearId
	 */
	void refreshCompanyCensusSynchronously(String companyCode, long realmYearId);
	
	/**
	 * @param companyCode
	 * @return
	 */
	boolean isRenewalCompany(String companyCode);
	
	/**
	 * Checks if given company is termed company or not
	 *
	 * @return true if termed company<br>
	 *         false if not termed company
	 */
	boolean isTermedCompany(String companyCode);
	
	String getCompanyName(String companyCode);

	/**
	 * Creates or update a company for the given realm plan year
	 * 
	 * @param company
	 * @param companyCode
	 * @param realmPlanYearId
	 * @param authBroker
	 * @param bundleId
	 * @param riskType
	 * @param bssNaicsCode
	 * @return companyId
	 */
	long createUpdateCompany(Company company, String companyCode, long realmPlanYearId,
			String authBroker, Long bundleId, RiskTypeEnum riskType, Integer bssNaicsCode);

	/**
	 * Returns a map of company id as key and CompanyStrategyDetailsDto as value for
	 * the given company
	 * 
	 * @param companyCode
	 * @return
	 */
	Map<Long, CompanyStrategyDetailsDto> getCompanyStrategyDetails(String companyCode);

	/**
	 * @param code
	 * @return
	 */
	List<Company> getXbssCompaniesByCode(String code);
	
	/**
	 * @param companyCode
	 * @param realmPlanYearId
	 * @return
	 */
	Company findCompanyBy(String companyCode, long realmPlanYearId);

	/**
	 * Update the flag value for PLYR_CHANGE_SYNC_EXCUTED
	 * 
	 * @param companyCode
	 * @param benExchange
	 */
	void updatePlYrChangeSyncExecutedFlag(String companyCode, BenExchngEnums benExchange);
	
	/**
	 * Returns the people soft company details 	
	 * @param code
	 * @return
	 */
	Company getPsCompanyDetails(String code);
	
	/**
	 * Updates the prospect company code to client code
	 * @param companyId
	 * @param companyCode
	 */
	void updatePsCompanyCodeForProspect(long companyId, String companyCode);

	/**
	 * Updates the aleUpdated flag for a given company
	 *
	 * @param company
	 * @param status
	 */
	void updateAleUpdatedFlag(Company company, Integer status);

	/**
	 * Retrieves a {@link Company} entity based on the provided BSS company ID.
	 *
	 * @param bssCompanyId
	 * @return
	 */
	Company findByCompanyId(Long bssCompanyId);
	
	/**
	 * This method is for getting company details based on company code
	 * 
	 * @param code
	 * @param history
	 * @param benExchange
	 * @param fromPlanYearSync
	 * @return
	 */
	Company getCompanyDetails(String code, boolean history, String emplId, BenExchngEnums benExchange,
			boolean fromPlanYearSync);


	/**
	 * This method is for getting the latest company details based on company code
	 *
	 * @param companyCode
	 * @return
	 */
	Company getLatestCompany(String companyCode);

	/**
	 * initiate quarter change for a company.
	 * @param companyCode the company code
	 * @param quarter the new quarter value
	 * @param messageSeq the message sequence for tracking the change
	 *
	 * @return true if the quarter change insert occurred, false otherwise
	 */
	boolean initiateQuarterChange(String companyCode, String quarter, String messageSeq);

	/**
	 * Gets company details by company ID including plan year start, OMS offering,
	 * clone program, bundle sequence, and OE quarter.
	 *
	 * @param companyId the company ID
	 * @return CompanyDetailsDto containing the company details, or null if not found
	 */
	CompanyDetailsDto getCompanyDetailsById(Long companyId);
	/**
	 * Finds a company by its quarter and effective date.
	 * @param benefitStartDate
	 * @param quarter
	 * @return
	 */
	Company findCompanyByQuarterAndEffDate(Date benefitStartDate, String quarter, String companyIdentifier);

	/**
	 * Resets a company by deleting all strategies and company-level data in a
	 * single atomic transaction.
	 *
	 * @param companyId the BSS company ID
	 */
	void resetCompany(long companyId);

	/**
	 * Resets a company by deleting all strategies and company-level data in a
	 * single atomic transaction. Optionally deletes the company record itself.
	 *
	 * @param companyId the BSS company ID
	 * @param deleteCompanyRecord whether to delete the company row from XBSS_COMPANY.
	 *        When false (default), preserves prospect company safety.
	 *        When true, performs hard-delete of the company record (for quarter-change flows).
	 */
	void resetCompany(long companyId, boolean deleteCompanyRecord);

	/**
	 * Deletes company-level records (regions, groups, employees, scheduled
	 * mid-year funding, default plan assignments) for the given company.
	 * Can be called independently or as part of a broader reset operation.
	 *
	 * @param companyId the BSS company ID
	 */
	void deleteCompanyData(long companyId);

}
