package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.ModelCompareGroupHeadcount;
import com.trinet.ambis.service.model.ModelComparePlanTypeCost;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.service.model.StrategyBenefitPlanHeadCount;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.service.model.StrategySubmitIssueReport;
import org.apache.commons.collections.map.MultiKeyMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface StrategyDataDao {
	int deleteAllPlanContributionsByBenefitgroupAndStrategy(long groupId, long strategyId);
	int deleteAllPlanSelectionsByBenefitgroupAndStrategy(long groupId, long strategyId);
	int deleteStrategyFundingsByBenefitgroupAndStrategy(long groupId, long strategyId);
	Map<String, BigDecimal> getAdditionalBenefitPlanEstCost(long planYearId);
	Map<String, PlanTypeDescription> getPlanTypeDescriptions(long planYearId);
	int getStrategiesHistoryCount(String companyCode, long realmYrId);
	int updateStrategySubmitFlag(long companyId);
	List<Strategy> getHistoryStrategies(String companyCode, long id);
	Strategy getCurrentStrategy(String companyCode, long id);
	List<Strategy> getFutureStrategies(String companyCode, long id);
	Map<String, PlanPackage> getPlanPackagesByStrategyIdAndBenefitGroupId(long strategyId, long benefitGroupId, long planYearId, boolean renewals);
	Set<String> getRealmPlanTypes(long realmYearId);
	Map<Long, List<PlanContribution>> getByPlanSelectionId(List<Long> ids, Map<String, List<BenefitPlanRate>> planRates,
			boolean contributionRequired);
	int deleteAllPlanSelectionsByStrategy(Set<Long> strategyIds);
	int deleteAllPlanSelectionsBy(Set<Long> strategyIds, String planType);
	int deleteAllPlanContributionsBy(Set<Long> strategyIds, String planType);
	int deleteAllPlanContributionsByStrategy(Set<Long> strategyIds);
	void deleteStrategyFundDetailByStrategy(Set<Long> strategyIds);
	int deleteStrategyFundModelByStrategy(Set<Long> strategyIds);
	void deleteStrategyById(Set<Long> strategyIds);
	void deleteEmployees(String companyCode, long realmYearId);
	void deleteGroupByCompanyId(long companyId);
    void deleteGroupCovHeadCount(long companyId);
    void deleteGroupRate(long companyId);

    void deleteGroupByIds(Set<Long> groupIds);

    void deleteGroupCovHeadCountByGroupIds(Set<Long> groupIds);

    void deleteGroupRateByGroupIds(Set<Long> groupIds);

    /**
	 * Deletes all records from XBSS_STRATEGY_GROUP for the given companyId
	 * 
	 * @param strategyIds
	 */
	void deleteStrategyGroup(Set<Long> strategyIds);

	/**
	 * Deletes all records from XBSS_EMPLOYEE_STRATEGY_GROUP for the given
	 * companyId
	 * 
	 * @param strategyIds
	 */
	void deleteEmployeeStrategyGroup(Set<Long> strategyIds);

	/**
	 * Deletes all records from XBSS_STRATEGY_GROUP_COV_HC for the given
	 * companyId
	 * 
	 * @param strategyIds
	 */
	void deleteStrategyGroupCovHeadCount(Set<Long> strategyIds);
	
	/**
	 * Returns a map of benefitProgram, benefitPlans and coverage level override types
	 * If history is true, do not map the plans to the next plan in realm plan mappings.
	 * Otherwise, map plans that are new and only mapped from one plan to the new plan
	 * 
	 * @param companyCode
	 * @param realmPlanYearId
	 * @param history
	 * @return
	 */
	Map<String, Map<String, Map<String, String>>> getOverridesByBenefitGroup(String companyCode, Long realmPlanYearId, boolean history);

	/**
	 * 
	 * @param companyCode
	 * @param realmPlanYearId
	 * @param effDate
	 * @return
	 */
	Map<Long, Map<String, Map<Long, List<PlanSelection>>>> getPlansSelectionsByCompany(String companyCode,
			Long realmPlanYearId, String effDate);

	/**
	 * Deletes the records from XBSS_STRATEGY_ESTIMATE for the given strategy ids
	 *
	 * @param strategyIds
	 */
	void deleteStrategyEstimateList(Set<Long> strategyIds);

	/**
	 * Deletes the records from XBSS_STRATEGY_ESTIMATE for the given strategy ids and plan types
	 *
	 * @param strategyIds
	 * @param planTypes
	 *
	 */
	int deleteStrategyEstimateForPlanTypes(Set<Long> strategyIds, List<String> planTypes);

	/**
	 * Insert the Medical (planType 10) records into XBSS_STRATEGY_ESTIMATE for the given strategy ids
	 * based on the data in XBSS_EE_PLAN_ASSIGNMENT
	 *
	 * @param strategyIds
	 * @param planTypes
	 *
	 */
	int insertStrategyEstimateForOmsPlanTypes(Set<Long> strategyIds, List<String> planTypes);

	/**
	 * 
	 * @param strategyEstimateMap
	 */
	void insertStrategyEstimate(Map<Long, List<StrategyEstimate>> strategyEstimateMap);

	/**
	 * 
	 * @param companyId
	 * @return
	 */
	List<ModelCompareStrategy> getModelCompareStrategies(long companyId);

	/**
	 * 
	 * @param strategyIdList
	 * @return
	 */
	Map<Long, List<ModelComparePlanTypeCost>> getStrategiesCost(List<Long> strategyIdList);

	/**
	 * 
	 * @param strategyIdList
	 * @return
	 */
	Map<Long, List<BenefitGroup>> getGroupsByStrategy(List<Long> strategyIdList);

	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	Map<String, List<String>> getOfferedPlanTypesByStrategy(String strategyId);
	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param fetchAllRecords
	 * @return
	 */
	 List<Object[]> getPlanContributionsByStrategyId(Company company, long strategyId, boolean fetchAllRecords);
	/**
	 * 
	 * @param realmPlanYearId
	 * @param strategyId
	 * @return
	 */
	Map<String, Map<String, String>> getStrategyDefaultPlans(long realmPlanYearId, long strategyId);
	
	/**
	 * @param strategyId
	 * @return
	 */
	Map<Long, Long> getStrategyBenefitGroupHeadCountsFromCensus(Long strategyId);

	/**
	 * 
	 * @param companyId
	 * @return
	 */
	int getSubmittedStrategiesCount(long companyId);
	
	/**
	 * @param companyId
	 * @return
	 */
	MultiKeyMap getEmplStrategyBenGroup(Long companyId);
	/**
	 * 
	 * @param company
	 * @param strategyList
	 * @return
	 */
	MultiKeyMap getGroupStrategyPlanCost(Company company, List<Long> strategyList);
	/**
	 *
	 * @param strategyIds
	 * @param planTypes
	 * @return List<StrategyGroupEmployeePlanRateData>
	 */
	Optional<List<StrategyGroupEmployeePlanRateData>> getOmsStrategyGroupPlanCostByPlanType(List<Long> strategyIds, List<String> planTypes);
	/**
	 * 
	 * @param company
	 * @param strategyList
	 * @param planTypes
	 * @return List<StrategyGroupEmployeePlanRateData>
	 */
	Optional<List<StrategyGroupEmployeePlanRateData>> getStrategyGroupPlanCostByPlanType(Company company, List<Long> strategyList, List<String> planTypes);
	/**
	 * 
	 * @param allStrategyList
	 * @param planTypes
	 * @return
	 */
	MultiKeyMap getStrategyProgramPlantypeOfferings(List<Long> allStrategyList, List<String> planTypes);
	
	/**
	 * Returns a LinkedList of ModelCompareBenefitPlanHeadCount objects
	 * 
	 * @param strategyIdList {@code a List<Long>}
	 * @param effDate {@code a String representing the effective date}
	 * 
	 * @return {@code List<ModelCompareBenefitPlanHeadCount>}
	 */
	List<StrategyBenefitPlanHeadCount> getHeadcountByPlanStrategyCoverage(List<Long> strategyIdList, String effDate);
		
	/**
	 * 
	 * @param strategyList
	 * @return {@code List<ModelCompareGroupHeadcount>}
	 */
	List<ModelCompareGroupHeadcount> getStrategyGroupHeadcountCost(List<Long> strategyList);

	/**
	 * 
	 * @param strategyIds
	 */
	void deleteStrategyFlatMaxByStrategy(Set<Long> strategyIds);

	/**
	 * Returns the strategy's additional plan offering planName(s) by group and planType
	 *
	 * @param strategyId
	 * @param effDate
	 * @return Map of groupId with a list of AdditionalBenefitPlan objects offered for the group
	 */
	Map<Long, List<AdditionalBenefitPlan>> getAdditionalBenefitPlansForStrategy(Long strategyId, String effDate);


	/**
	 * Returns the strategy's additional plan offering planName(s) by group and planType
	 *
	 * @param strategyId
	 * @param effDate
	 * @param realmPlanYearId
	 * @return Map of groupId with a list of DisabilityBenefitOptionPlans objects offered for the strategy
	 * and group. This includes the SDI information.
	 */
	Map<Long, List<DisabilityBenefitOptionPlans>> getAdditionalBenefitPlansForStrategyWithSdiInfo(Long strategyId, String effDate, long realmPlanYearId);


	/**
	 * Returns a list of {@code UnsentStrategySubmitEmailReport} objects which
	 * contains data regarding successfully submitted strategies where the
	 * client was not sent a confirmation email.
	 * 
	 * @return
	 */
	List<StrategySubmitIssueReport> getSubmittedStrategyIssueReportData();
	
	/**
	 * inactivates all the unsubmitted strategies that don't have the benefit programs in sync
	 * with the submitted strategy
	 * @param realmPlanYear
	 */
	void inactivateUnSubmittedStrategiesByPlanYear(long realmPlanYear);
	
	/**
	 * inactivates all the unsubmitted strategies that don't have the benefit programs in sync
	 * with the submitted strategy for a company
	 * 
	 */
	void inactivateUnSubmittedStrategiesByCompany(Company company);
	/**
	 * 
	 * @param strategyId
	 * @param groupId
	 */
	void deleteStrategyEstimateByStrategyGroup(long strategyId, long groupId);
	
	Map<Long, Map<String, List<String>>> getStrategyBenPlans(List<Long> strategyIds);
	
	/**
	 * 
	 * @param trinetStrategyId
	 * @return
	 */
	List<Object[]> getBenefitCostSummary(String trinetStrategyId);

	/**
	 * Deletes records from xbss_ee_plan_assignment for the given strategy ids
	 * 
	 * @param strategyIds
	 * @return no of records deleted
	 */
	int deleteEePlanAssignmentsByStrategyIds(Set<Long> strategyIds);

	/**
	 * Deletes records from xbss_ee_default_plan_assignment for the given company id
	 * 
	 * @param companyId
	 * @return no of records deleted
	 */
	int deleteEeDefaultPlanAssignmentsByCompanyId(long companyId);
	
	List<Object[]> getStrategyHsaFunding(List<Long> strategyIdList);
	
	List<Object[]> getStrategyFundingByStrategy(List<Long> strategyIdList);
	/**
	 * get primaryCarrierName based on company realmPlanYearId, realmPlanYearStartDate and trinetStragegyId
	 * 
	 * @param company
	 * @param tnStrategyId
	 * @return primaryCarruerName string
	 */
	String getPrimaryCarrierName(Company company, String tnStrategyId);

	/**
	 * get the M/D/V monthly EE and ER cost for a strategy id by plan type
	 *
	 * @param strategyId
	 * @return List<Object[]>
	 */
	List<Object[]> getHealthCostsByPlanType(Long strategyId);

	/**
	 * get the Additional Benefits monthly EE and ER cost for a strategy id by plan type
	 *
	 * @param strategyId
	 * @return List<Object[]>
	 */
	List<Object[]> getAdditionalBenefitCostsByPlanType(Long strategyId);

	/**
	 * get the strategy estimate for strategyId and groupId by plan type
	 *
	 * @param strategyId
	 * @param groupId
	 * @return Map<String, BigDecimal>
	 */
	Map<String, BigDecimal> getStrategyGroupEstimateByPlanType(long strategyId, long groupId);
}
