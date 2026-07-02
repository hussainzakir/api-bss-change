/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.repository.query.Param;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.projections.PlanSelectionDetail;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;

/**
 * @author kpamulapati
 *
 */
public interface PlanSelectionService {

	public PlanSelection createUpdatePlanSelection(PlanSelection planSelection);

	public PlanSelection getPlanSelection(long strategyId, long groupId, String peoplesoftPlan);

	List<PlanSelection> getPlansByStrategyIdGroupId(long strategyId, long groupId);

	void deleteAll(List<PlanSelection> list);

	List<PlanSelection> saveAll(List<PlanSelection> planSelectionList);

	List<PlanSelection> getPlansByGroupId(long groupId);

	List<PlanSelection> findByStrategyIdAndPlanType(long strategyId, String planType);

	List<PlanSelection> findByStrategyIdAndPlanTypes(long strategyId, List<String> planType);

	/**
	 * Return plans by plan type offered by the strategy associated with the regions.
	 * If filterSubregions is true, returns the plans offered in the regions or sub-regions
	 *
	 * @param company
	 * @param strategyId
	 * @param planTypes
	 * @param filterSubregions
	 * @return
	 */
	Map<String, List<PlanAppendixBenefitPlanData>> findAppendixReportBenefitPlansBy(Company company, String strategyId, List<String> regions, List<String> planTypes, boolean filterSubregions);

	/**
	 * Adds optional (non-K1 programs) or group (K1 programs) dental and vision
	 * plans to each of a client's benefit programs if they are TN IV and not
	 * currently offering the benefit.
	 *
	 * @param company
	 * @param benefitGroups
	 * @param bgsHealthPlansMap
	 * @param mapOfCoverageLevels
	 */
	void addRequiredDentalVisionPlans(Company company, List<BenefitGroup> benefitGroups,
									  Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
									  Map<String, List<CoverageLevel>> mapOfCoverageLevels);

	/**
	 * Returns distinct plan type by strategy and group
	 *
	 * @param strategyIds
	 * @param groupIds
	 * @return
	 */
	List<PlanSelectionDetail> findDistinctPlanTypeBy(@Param("strategyIds") Set<Long> strategyIds,
													 @Param("groupIds") Set<Long> groupIds);

	/**
	 * This method will return plan selections for the given strategy id
	 *
	 * @param strategyId
	 * @return
	 */
	List<PlanSelection> getPlansByStrategyId(long strategyId);

	/**
	 * This method will return add/remove OMS Medical plan selections for the given strategy id
	 * based on enrollment in the plans in XBSS_EMPLOYEE_PLAN_ASSIGNMENT table
	 *
	 * @param strategyId
	 * @return
	 */
	void syncOmsMedicalPlanSelections(long strategyId);

}