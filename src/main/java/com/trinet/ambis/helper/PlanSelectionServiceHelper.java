/**
 * 
 */
package com.trinet.ambis.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;

/**
 * @author rvutukuri
 *
 */
public class PlanSelectionServiceHelper {
	
	private PlanSelectionServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + PlanSelectionServiceHelper.class.getName() + " can not be instantiated.");
	}

	public static void addDentalVisionPlans(List<BenefitGroup> benefitGroups,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels,
			Map<String, List<XbssRealmPlyrPlan>> planTypePlanMap) {

		for (BenefitGroup benefitGroup : benefitGroups) {
			String benefitProgram = benefitGroup.getBenefitProgram();
			String groupType = benefitGroup.getType();
			Map<String, BenefitPlan> plansToAddMap = null;

			if (MapUtils.isNotEmpty(bgsHealthPlansMap) && bgsHealthPlansMap.containsKey(benefitProgram)) {

				Map<String, Map<String, BenefitPlan>> bpPlanTypePlans = bgsHealthPlansMap.get(benefitProgram);

				// If they aren't offering dental, add it.
				if (MapUtils.isEmpty(bpPlanTypePlans.get(BSSApplicationConstants.DENTAL_PLAN_TYPE))) {

					// If K1 group, add the group plans.
					// Otherwise, add the voluntary plans
					plansToAddMap = getPlansToAddMap(BSSApplicationConstants.DENTAL,
							BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType), planTypePlanMap,
							mapOfCoverageLevels);

					if (MapUtils.isNotEmpty(plansToAddMap)) {
						bpPlanTypePlans.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, plansToAddMap);
					}
				}
				
				// If they aren't offering vision, add it.
				if (MapUtils.isEmpty(bpPlanTypePlans.get(BSSApplicationConstants.VISION_PLAN_TYPE))) {

					// If K1 group, add the group plans.
					// Otherwise, add the voluntary plans
					plansToAddMap = getPlansToAddMap(BSSApplicationConstants.VISION,
							BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType), planTypePlanMap,
							mapOfCoverageLevels);

					if (MapUtils.isNotEmpty(plansToAddMap)) {
						bpPlanTypePlans.put(BSSApplicationConstants.VISION_PLAN_TYPE, plansToAddMap);
					}
				}
			}
		}
	}
	
	private static Map<String, BenefitPlan> getPlansToAddMap(String basePlanType, boolean addGroupPlans, 
			Map<String, List<XbssRealmPlyrPlan>> planTypePlanMap,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels) {
		
		String planTypeToAdd = null;
		
		if (BSSApplicationConstants.DENTAL.equals(basePlanType)) {
			if (addGroupPlans) {
				planTypeToAdd = BSSApplicationConstants.DENTAL_PLAN_TYPE;
			}
			else {
				planTypeToAdd = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
			}
		}
		else if (BSSApplicationConstants.VISION.equals(basePlanType)) {
			if (addGroupPlans) {
				planTypeToAdd = BSSApplicationConstants.VISION_PLAN_TYPE;
			}
			else {
				planTypeToAdd = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;
			}
			
		}

		Map<String, BenefitPlan> returnMap = new HashMap<>();
		if (planTypePlanMap.containsKey(planTypeToAdd)) {
			List<XbssRealmPlyrPlan> planList = planTypePlanMap.get(planTypeToAdd);
			for (XbssRealmPlyrPlan planYearPlan : planList) {
				BenefitPlan bp = new BenefitPlan();
				bp.setId(planYearPlan.getBenefitPlan());
				bp.setPlanType(planYearPlan.getPlanType());
				RenewalServiceHelper.addBlankContributions(bp, mapOfCoverageLevels.get(basePlanType));
				returnMap.put(bp.getId(), bp);
			}
		}
		return returnMap;
	}

}
