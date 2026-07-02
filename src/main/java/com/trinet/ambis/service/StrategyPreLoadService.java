/**
 * 
 */
package com.trinet.ambis.service;

import java.util.Date;

import com.trinet.ambis.persistence.model.Company;

/**
 * @author rvutukuri
 *
 */
public interface StrategyPreLoadService {

	/**
	 * 
	 * @param company
	 */
	void preLoadBssStrategies(String peoId, String quarter, Long relamYearId, Date payrollCutOffDate, String emplid);

	/**
	 * 
	 * @param company
	 * @param emplid
	 */
	void preLoadClientStrategies(Company company);
}
