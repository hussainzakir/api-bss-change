/**
 * 
 */
package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.MappedHeadCount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;

public interface HeadCountService {

	/**
	 * 
	 * @param company
	 * @param realmPlanYearId
	 * @param effDate
	 * @return
	 */
	Map<String, Integer> getEmployeeHeadcountByBenefitGroup(Company company, Long realmPlanYearId, Date effDate);

	/**
	 * 
	 * @param company
	 * @param realmPlanYearId
	 * @param effDate
	 * @param getMappedPlans
	 * @return
	 */
	Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getHeadCountByGroupAndPlan(Company company,
			Long realmPlanYearId, Date effDate, boolean getMappedPlans);
	/**
	 * 
	 * @param company
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, Map<String, Map<String, Long>>> getMirrorPlanHeadCounts(Company company, long realmPlanYearId);
	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param realmPlanYearId
	 * @param history
	 * @return
	 */
	Map<String, Long> getWaiverHeadCountByBenefitProgram(Company company, long strategyId, long realmPlanYearId,
			boolean history);

	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param history
	 * @return
	 */
	Map<String, Integer> getPrimaryHeadCountByBenefitProgram(Company company, long strategyId, boolean history);
	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @return
	 */
	Map<Long, Long> getStrategyBenefitGroupHeadCount(Company company, long strategyId);
	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param realmPlanYear
	 * @param history
	 * @return
	 */
	Map<String, ActiveEligibleEECount> getEligibleEmployeeCount(Company company, long strategyId,
			RealmPlanYear realmPlanYear, boolean history);
	/**
	 * 
	 * @param companyCode
	 * @param realmPlanYearId
	 * @return
	 */
	List<MappedHeadCount> getMappedHeadCounts(String companyCode, long realmPlanYearId);
}
