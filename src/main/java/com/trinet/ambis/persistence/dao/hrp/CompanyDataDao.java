/**

 *
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest.ExchangeDates;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import com.trinet.ambis.service.model.CompanyRealmData;

/**
 * @author rvutukuri
 *
 */
@Repository
public interface CompanyDataDao {

	/**
	 * 
	 * @param companyId
	 * @return
	 */
	Set<String> getRegionsByCompanyId(Long companyId);

	/**
	 * 
	 * @param companyId
	 * @param regions
	 * @return
	 */
	int insertUpdateCompanyRegions(Long companyId, Set<String> regions);
	
	/**
	 * Returns a list of the passed in company's current and future (if available)
	 * realm plan year information
	 * 
	 * @param companyCode
	 * @param isRenewalCompany
	 * @return
	 */
	List<CompanyRealmData> getAvailableCompanyRealms(String companyCode, boolean isRenewalCompany);
	
	/**
	 * Updates ACA_LARGE_EMPLR column for a given company
	 * 
	 * @param company   id
	 * @param aca large emplr boolean value to be updated
	 * 		  0 -> aca large emplr is false (blank/null/N in peoplesoft)
	 * 		  1 -> aca large emplr is true (Y in peoplesoft)
	 * @return
	 */
	int updateAcaLargeEmplr(Long companyId, boolean acaLargeEmplr);

	/**
	 * Returns a map of company id as key and CompanyStrategyDetailsDto as value for
	 * the given company
	 * 
	 * @param companyCode
	 * @return
	 */
	Map<Long, CompanyStrategyDetailsDto> getCompanyStrategyDetails(String companyCode);

	List<BundleSelectionDetailsDto> getBundleSelectionDetails(String companyCode, List<ExchangeDates> exchangeDatePairs);

	/**
	 * Gets company details by company ID including plan year start, OMS offering,
	 * clone program, bundle sequence, and OE quarter.
	 *
	 * @param companyId the company ID
	 * @return CompanyDetailsDto containing the company details, or null if not found
	 */
	CompanyDetailsDto getCompanyDetailsById(Long companyId);

}
