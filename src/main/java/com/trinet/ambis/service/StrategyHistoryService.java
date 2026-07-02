/**
 * 
 */
package com.trinet.ambis.service;

import java.util.Map;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlan;

/**
 * @author rvutukuri
 *
 */
public interface StrategyHistoryService {

	/**
	 * This method is for creating the current strategy for peoplesoft Data.
	 * 
	 * @param companyCode
	 */
	void createHistoryStrategyFromPS(Company company,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap);

}
