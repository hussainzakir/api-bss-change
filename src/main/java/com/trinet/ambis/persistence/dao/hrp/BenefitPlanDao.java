package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EligiblePlanData;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;

@Repository
public interface BenefitPlanDao {
	/**
	 * This method returns map of plan type mapped to set of StateBenefitPlan
	 * excluding outOfRegionPlans for given plan year.
	 * 
	 * @param plansPortfoliosList
	 * @param company
	 * @param outOfRegionPlans
	 * @return
	 */
	Map<String, Set<StateBenefitPlan>> getAllPrimaryBenefitPlans(Set<String> plansPortfoliosList, Company company,
			Set<String> outOfRegionPlans);

	Set<String> getWidelyAvailablePlans(Set<String> plans, long realmPlanYearId);	
	
	Map<String, BenefitPlan> getRegionalBasePlanMapping(RealmPlanYear rpy);

	/**
	 * This method returns a list of regional plans that the client offers in the given strategy, group, realmPlanYearId and basePlanId
	 *
	 * @param strategyId
	 * @param groupId
	 * @param realmPlanYearId
	 * @param basePlanId
	 * @return
	 */
	List<String> getSelectedRegionalPlansForBasePlan(Long strategyId, Long groupId, Long realmPlanYearId, String basePlanId);

	/**
	 * Fetching the plans benefit plan id's for plan appendix report
	 *
	 * @param company
	 * @param strategyId
	 * @param regions
	 * @param mdPlanTypes
	 * @param visionPlanTypes
	 * @param filterSubregions
	 *
	 * @return List<String>
	 */
	List<PlanAppendixBenefitPlanData> getPlansForAppendix(Company company,String strategyId,List<String> regions, List<String> mdPlanTypes,  List<String> visionPlanTypes, boolean filterSubregions);

	/**
	 * Get plan offerings data based on realmYearId
	 * @param planOfferingsRequest
	 * @param realmYearId
	 * @param outOfRegionPlans
	 * @return
	 */
	List<PlanOfferingsBenefitPlanData> getBenefitsPlanOfferingsBy(PlanOfferingsRequest planOfferingsRequest, long realmYearId, Set<String> outOfRegionPlans, boolean isPickChoose);

	/**
	 * Get the plan selections for the passed in company
	 * @param planOfferingsRequest
	 * @param realmYearId
	 * @return
	 */
	Map<String, List<String>> getCompanyPlanSelectionsForPlanOfferingReport(PlanOfferingsRequest planOfferingsRequest,
																				   long realmYearId);
	
	/**
	 * Get all benefit plans and carriers by strategy group and plan type.
	 * @param strategyId
	 * @param groupId
	 * @param planType
	 * @param effectiveDate
	 * @param cvgTierCode
	 * @return
	 */
	Map<String, EligiblePlanData> getBenefitPlansAndCarriersBy(long strategyId, long groupId, String planType,
			Date effectiveDate, String cvgTierCode);

    /**
     * Get regional plans for a given state excluding outOfRegionPlans
     * @param state
     * @param realmYearId
     * @param outOfRegionPlans
     * @return Map of plan type mapped to map of portfolio id and plan id
     */
    Map<String, Map<Long, Set<String>>> getPortfolioPlansByPlanTypeForState(String state, long realmYearId,  Set<String> outOfRegionPlans);

	List<Object[]> getAllExchangeAndBundlesPlans(Company company, Set<String> plansPortfoliosIds, Set<String> outOfRegionPlans, Set<Long> bundleIds);
}
