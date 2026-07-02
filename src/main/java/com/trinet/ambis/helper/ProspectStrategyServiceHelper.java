package com.trinet.ambis.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.StrategyDefaultPlan;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.StateBenefitPlan;

public class ProspectStrategyServiceHelper {
	
	private ProspectStrategyServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + ProspectStrategyServiceHelper.class.getName() + " can not be instantiated.");
	}
	
	public static List<PlanSelection> createAdditionalPlanSelections(long strategyId, long groupId,
			List<StrategyDefaultPlan> defaultPlans) {
		List<PlanSelection> additionalPlanSelections = new ArrayList<>();
		for (StrategyDefaultPlan defaultPlan : defaultPlans) {
			for (String planType : BSSApplicationConstants.ADDITIONAL_PLAN_TYPES_INCLUD_CMTR) {
				if (planType.equals(defaultPlan.getPlanType())) {
					BenefitPlan bp = new BenefitPlan();
					bp.setId(defaultPlan.getBaseBenefitPlan());
					bp.setPlanType(defaultPlan.getPlanType());
					bp.setPlanCarrierId(defaultPlan.getPortfolioId());
					PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId, groupId, bp,
							0L);
					additionalPlanSelections.add(planSelection);
				}
			}
		}
		return additionalPlanSelections;
	}
	
	public static List<PlanSelection> createDentalAndVisionPlanSelections(
			Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels, long strategyId, long groupId,
			Map<String, BenefitPlan> benefitPlanMap, List<String> planTypes) {
		List<PlanSelection> planSelections = new ArrayList<>();
		for (Map.Entry<String, Set<StateBenefitPlan>> entry : healthBenefitPlansMap.entrySet()) {
			for (StateBenefitPlan benefitPlan : entry.getValue()) {
				if (planTypes.contains(benefitPlan.getPlanType())) {
					BenefitPlan bp = new BenefitPlan();
					bp.setId(benefitPlan.getBenefitPlan());
					bp.setPlanType(benefitPlan.getPlanType());
					bp.setPlanCarrierId(benefitPlan.getPortfolioId());
					RenewalServiceHelper.addBlankContributions(bp, mapOfCoverageLevels.get(entry.getKey()));
					benefitPlanMap.put(bp.getId(), bp);
					PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId, groupId, bp,
							0L);
					planSelections.add(planSelection);
				}
			}
		}
		return planSelections;
	}
}
