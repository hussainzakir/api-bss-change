/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;

import com.trinet.ambis.persistence.model.Company;

/**
 * @author rvutukuri
 *
 */
public interface StrategyPreLoadDao {

	/**
	 * 
	 * @param peodId
	 * @param quarter
	 * @param relamYearId
	 * @param payrollCutOffDate
	 */
	void preLoadBssStrategies(String peodId, String quarter, Long relamYearId, Date payrollCutOffDate, String emplid);

	/**
	 * 
	 * @param company
	 * @param emplid
	 */
	void preLoadClientStrategies(Company company);

}
