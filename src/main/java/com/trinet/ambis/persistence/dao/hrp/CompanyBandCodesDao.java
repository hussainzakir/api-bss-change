/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import com.trinet.ambis.service.model.CompanyBandCodes;
import java.util.Date;

/**
 * @author rvutukuri
 *
 */

public interface CompanyBandCodesDao {
	/**
	 * 
	 * @param companyId
	 * @return
	 */
	List<CompanyBandCodes> getBandCodesByCompanyId(Long companyId);

	/**
	 * 
	 * @param companyId
	 * @param companyBandCodes
	 * @return
	 */
	int insertUpdateCompanyBandCodes(Long companyId, List<CompanyBandCodes> companyBandCodes);
	
	/**
	 * @param companyId
	 * @param benStartDt
	 * @return
	 */
	List<CompanyBandCodes> getBandCodesByCompanyIdAndEffDate(long companyId, String benStartDt);
		
	/*
	 * This method returns the effective date for band codes that are currently in effect for given exchange.
	 * Caution - Only applicable to prospect companies
	 * 
	 * @param prospectId
	 * @param realmId
	 * @return
	 */
	Date getProspectBandEffectiveDate(String prospectId, long realmId);
	
	/**
	 * This method returns the effective date for band codes that are currently in effect for given exchange or returns null if the effective date is not found.
	 * 
	 * @param prospectId
	 * @param realmId
	 * @return
	 */
	Date getProspectBandEffDate(String prospectId, long realmId);

}
