package com.trinet.ambis.service;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;

/**
 * @author rvutukuri
 *
 */
public interface StrategySyncService {

	/**
	 * 
	 * @param company
	 * @param strategyId
	 */
	void syncStrategyData(Company company, Long strategyId);

	/**
	 * 
	 * @param company
	 * @param strategyId
	 */
	void syncStrategyHistoryData(Company company, Long strategyId);

	/**
	 * Recalculate strategies, for example after updating the band codes for a Prospect
	 *
	 * @param companyCode
	 * @param exchange
	 * @param processInfo
	 */
	void syncStrategiesForCompany(String companyCode, BenExchngEnums exchange, Company.ProcessInfo processInfo);

}
