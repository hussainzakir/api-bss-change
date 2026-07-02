/**
 * 
 */
package com.trinet.ambis.service.impl;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.StrategyGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.PlanSelectionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.PlanSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.projections.PlanSelectionDetail;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;

/**
 * @author kpamulapati
 *
 */

@Service
public class PlanSelectionServiceImpl implements PlanSelectionService {

	@Autowired
	RealmPlyrPlanService realmPlyrPlanService;
	
	@Autowired
	PlanSelectionDao planSelectionDao;
	
	@Autowired
	BenefitPlanDao benefitPlanDao;
	
	@Autowired
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	@Autowired
	EmployeePlanAssignmentService employeePlanAssignmentService;

	@Autowired
	StrategyGroupService strategyGroupService;
	
	@Override
	public PlanSelection createUpdatePlanSelection(PlanSelection planSelection) {
		return planSelectionDao.saveAndFlush(planSelection);
	}

	public PlanSelection getPlanSelection(long strategyId, long groupId, String benefitPlan) {
		return planSelectionDao.getByStrategyIdAndGroupIdAndBenefitPlan(strategyId, groupId, benefitPlan);
	}

	@Override
	public List<PlanSelection> getPlansByStrategyIdGroupId(long strategyId, long groupId) {
		return strategyGroupPlanSelectDao.findByStrategyIdAndGroupId(strategyId, groupId);
	}

	@Override
	public List<PlanSelection> findByStrategyIdAndPlanType(long strategyId, String planType) {
		return strategyGroupPlanSelectDao.findByStrategyIdAndPlanType(strategyId, planType);
	}

	@Override
	public List<PlanSelection> findByStrategyIdAndPlanTypes(long strategyId, List<String> planTypes) {
		return strategyGroupPlanSelectDao.findByStrategyIdAndPlanTypeIn(strategyId, planTypes);
	}

	@Override
	public List<PlanSelection> getPlansByGroupId(long groupId) {
		return strategyGroupPlanSelectDao.findByGroupId(groupId);
	}
	
	@Override
	public void deleteAll(List<PlanSelection> list) {
		strategyGroupPlanSelectDao.deleteAllInBatch(list);	
	}
	
	@Override
	public List<PlanSelection> saveAll(List<PlanSelection> planSelectionList) {
		return planSelectionDao.saveAllAndFlush(planSelectionList);
	}

	@Override
	public void addRequiredDentalVisionPlans(Company company, List<BenefitGroup> benefitGroups,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels) {

		if (BenExchngEnums.TRINET_IV.getBenExchng().equals(company.getRealm().getBenExchange())) {
			List<String> planTypes = new ArrayList<>();
			planTypes.addAll(BSSApplicationConstants.DENTAL_PLAN_TYPES);
			planTypes.addAll(BSSApplicationConstants.VISION_PLAN_TYPES);
			Map<String, List<XbssRealmPlyrPlan>> planTypePlanMap = realmPlyrPlanService
					.getPlanTypePlanMapForRealmPlanYear(company.getRealmPlanYear().getId(), planTypes);
			PlanSelectionServiceHelper.addDentalVisionPlans(benefitGroups, bgsHealthPlansMap, mapOfCoverageLevels,
					planTypePlanMap);
		}
	}

	@Override
	public Map<String, List<PlanAppendixBenefitPlanData>> findAppendixReportBenefitPlansBy(Company company,
														   String strategyId,List<String> regions, List<String> planTypes, boolean filterSubregions) {
		List<String> mdPlanTypes = new ArrayList<>();
		List<String> visionPlanTypes = new ArrayList<>();
		if (planTypes.contains(BSSApplicationConstants.MEDICAL_PLAN_TYPE)) {
			mdPlanTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		} 
		if(planTypes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {	
			mdPlanTypes.addAll(BSSApplicationConstants.DENTAL_PLAN_TYPES);
		} 
	    if(planTypes.contains(BSSApplicationConstants.VISION_PLAN_TYPE)) {	
	    	visionPlanTypes.addAll(BSSApplicationConstants.VISION_PLAN_TYPES);
		}
		List<PlanAppendixBenefitPlanData> plansWithoutEnrollments = benefitPlanDao.getPlansForAppendix(company, strategyId,regions, mdPlanTypes, visionPlanTypes, filterSubregions);

		Map<String, PlanAppendixBenefitPlanData> uniquePlans = plansWithoutEnrollments.stream()
				.collect(Collectors.toMap(
						p -> p.getPlanType() + "|" + p.getDescription().trim().toUpperCase(),
						Function.identity(), (existing, duplicate) -> existing));
		return uniquePlans.values().stream()
				.collect(Collectors.groupingBy(PlanAppendixBenefitPlanData::getPlanType));

	}

	@Override
	public List<PlanSelectionDetail> findDistinctPlanTypeBy(Set<Long> strategyIds, Set<Long> groupIds) {
		return strategyGroupPlanSelectDao.findDistinctPlanTypeBy(strategyIds, groupIds);
	}
	
	@Override
	public List<PlanSelection> getPlansByStrategyId(long strategyId) {
		return strategyGroupPlanSelectDao.findByStrategyId(strategyId);
	}

	@Override
	public void syncOmsMedicalPlanSelections(long strategyId) {

		Map<String, Set<String>> enrolledBenefitPlans = getEnrolledBenefitPlans(strategyId);

		List<BenefitGroupStrategy> benefitGroupStrategies = strategyGroupService
				.findByStrategyIdAndStatus(strategyId, BSSApplicationConstants.STATUS_ACTIVE);

		List<PlanSelection> currentPlanSelections =
				findByStrategyIdAndPlanTypes(strategyId, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		Map<String, PlanSelection> currentPlanSelectionsMap = getPlanSelectionsMap(currentPlanSelections);

		List<PlanSelection> updatedPlanSelections = getUpdatedPlanSelections(strategyId, benefitGroupStrategies, enrolledBenefitPlans);
		Map<String, PlanSelection> updatedPlanSelectionsMap = getPlanSelectionsMap(updatedPlanSelections);

		List<PlanSelection> newPlanSelections = getMissingPlanSelections(updatedPlanSelectionsMap, currentPlanSelectionsMap);
		if (!newPlanSelections.isEmpty()) {
			saveAll(newPlanSelections);
		}

		List<PlanSelection> planSelectionsToRemove = getMissingPlanSelections(currentPlanSelectionsMap, updatedPlanSelectionsMap);
		if (!planSelectionsToRemove.isEmpty()) {
			deleteAll(planSelectionsToRemove);
		}

	}

	private Map<String, Set<String>> getEnrolledBenefitPlans(long strategyId) {
		List<EePlanAssignment> employeePlanAssignments = employeePlanAssignmentService
				.getEmployeePlanAssigmentBy(List.of(strategyId));
		return employeePlanAssignments.stream()
				.collect(Collectors.groupingBy(
						eePlanAssignment -> eePlanAssignment.getEePlanAssignmentPK().getBenefitType(),
						Collectors.mapping(EePlanAssignment::getBenefitPlan, Collectors.toSet())));
	}

	private Map<String, PlanSelection> getPlanSelectionsMap(List<PlanSelection> planSelections) {
		return planSelections.stream()
				.collect(Collectors.toMap(
						planSelection -> planSelection.getGroupId() + "_" + planSelection.getPlanType() + "_" + planSelection.getBenefitPlan(),
						planSelection -> planSelection));
	}

	private static List<PlanSelection> getUpdatedPlanSelections(long strategyId, List<BenefitGroupStrategy> benefitGroupStrategies, Map<String, Set<String>> enrolledBenefitPlans) {
		List<PlanSelection> updatedPlanSelections = new ArrayList<>();
		for (BenefitGroupStrategy benefitGroupStrategy : benefitGroupStrategies) {
			long groupId = benefitGroupStrategy.getGroupId();
			for (Map.Entry<String, Set<String>> entry : enrolledBenefitPlans.entrySet()) {
				String planType = entry.getKey();
				for (String benefitPlan : entry.getValue()) {
					PlanSelection planSelection = new PlanSelection();
					planSelection.setId(0L);
					planSelection.setStrategyId(strategyId);
					planSelection.setGroupId(groupId);
					planSelection.setPlanType(planType);
					planSelection.setBenefitPlan(benefitPlan);
					updatedPlanSelections.add(planSelection);
				}
			}
		}
		return updatedPlanSelections;
	}

	/**
	 * Get the planSelections that are in firstPlanSelectionsMap but not in secondPlanSelectionsMap
	 *
	 * @param firstPlanSelectionsMap
	 * @param secondPlanSelectionsMap
	 * @return
	 */
	private static List<PlanSelection> getMissingPlanSelections(Map<String, PlanSelection> firstPlanSelectionsMap,
			Map<String, PlanSelection> secondPlanSelectionsMap) {
		Set<String> missingKeys = firstPlanSelectionsMap.keySet().stream()
				.filter(key -> !secondPlanSelectionsMap.containsKey(key))
				.collect(Collectors.toSet());

		return missingKeys.stream()
				.map(firstPlanSelectionsMap::get)
				.collect(Collectors.toList());
	}

}
