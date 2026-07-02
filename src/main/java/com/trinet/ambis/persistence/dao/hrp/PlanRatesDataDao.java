package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.StateBenefitPlan;

@Repository
@Transactional(readOnly = true)
public interface PlanRatesDataDao {

	/**
	 * Returns a map of benefitPlan and StateBenefitPlan for the passed in plan
	 * types that were available for the realmPlanYearId and passed in
	 * portfolios. This will exclude any plans passed in the outOfRegionPlans
	 * parameter.
	 * 
	 * @param realmPlanYearId
	 * @param prevRealmPlanYearId
	 * @param portfolios
	 * @param company
	 * @param outOfRegionPlans
	 * @param planTypes
	 * @return
	 */
	Map<String, StateBenefitPlan> getBenefitPlans(long realmPlanYearId, Set<String> portfolios, Company company,
			Set<String> outOfRegionPlans, Set<String> planTypes);

	/**
	 * Returns a map of benefitPlan and the states it is available in for the
	 * passed in realm plan year ids
	 * 
	 * @param realmPlanYearId
	 * @param prevRealmPlanYearId
	 * @return
	 */
	Map<String, Set<String>> getBenefitPlanStates(long realmPlanYearId, long prevRealmPlanYearId);
	
	/**
	 * This method returns coverage level rates of all the plans for given company.
	 * 
	 * @param company
	 * @return
	 */
	public Map<String, List<BenefitPlanRate>> getBenefitPlanRatesBy(Company company);
}
