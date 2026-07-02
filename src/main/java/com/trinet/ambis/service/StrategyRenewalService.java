/**
 * 
 */
package com.trinet.ambis.service;

import java.util.Map;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.BenefitPlan;

/**
 * @author rvutukuri
 *
 */
public interface StrategyRenewalService {
	/**
	 * This method is for creating future strategies for a company which is not
	 * present in BSS for the previous plan year.
	 * 
	 * @param company
	 * @param history
	 * @param realmPlanYear
	 * @param isDefaultSubmit
	 * @param bgsHealthPlansMap
	 * @param bgsADPlansMap
	 * @param isPreload
	 * @return
	 */
	Map<String, BenefitGroup> createFutureStrategies(Company company, boolean history, RealmPlanYear realmPlanYear,
			boolean isDefaultSubmit, Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap, boolean isPreload);

}
