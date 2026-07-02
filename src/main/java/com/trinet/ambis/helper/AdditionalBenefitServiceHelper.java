/**
 * 
 */
package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.output.AdditionalBenefitPlanDto;

/**
 * @author rvutukuri
 *
 */
public class AdditionalBenefitServiceHelper {

	private static final Logger logger = LoggerFactory.getLogger(AdditionalBenefitServiceHelper.class);
	
	private AdditionalBenefitServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + AdditionalBenefitServiceHelper.class.getName() + " can not be instantiated.");
	}
	
	/**
	 * 
	 * @param adbAllStatePlansMap
	 * @return
	 */
	public static Map<String, Set<String>> getADBPlanListMapByType(
			Map<String, Set<StateBenefitPlan>> adbAllStatePlansMap) {
		Map<String, Set<String>> adbPlanListMapByType = new HashMap<>();
		for (Map.Entry<String, Set<StateBenefitPlan>> entry : adbAllStatePlansMap.entrySet()) {
		    String planType = entry.getKey();
			Set<String> plans = new HashSet<>();
			for (StateBenefitPlan sbp : adbAllStatePlansMap.get(planType)) {
				plans.add(sbp.getBenefitPlan());
			}
			adbPlanListMapByType.put(planType, plans);
		}
		return adbPlanListMapByType;
	}
	/**
	 * Organize the argument <code>adPlanSelections</code> by plan type and benefit plan
	 * @param adPlanSelections
	 * @return a map of PlanTypes to List of Benefit Plans
	 */
	public static Map<String, Set<String>> getADBPlanListMapByType(List<PlanSelection> adPlanSelections) {
		Map<String, Set<String>> adbPlanListMapByType = new HashMap<>();
		for (PlanSelection plan : adPlanSelections) {
			if (null != adbPlanListMapByType.get(plan.getPlanType())) {
				Set<String> plans = adbPlanListMapByType.get(plan.getPlanType());
				plans.add(plan.getBenefitPlan());
			} else {
				Set<String> plans = new TreeSet<>();
				plans.add(plan.getBenefitPlan());
				adbPlanListMapByType.put(plan.getPlanType(), plans);
			}
		}
		return adbPlanListMapByType;
	}

	/**
	 * Organize the argument <code>adPlanSelections</code> by plan type and benefit plan
	 * @param adPlanSelections
	 * @return a map of PlanTypes to List of Benefit Plans
	 */
	public static Map<String, Set<String>> getAdditionalBenefitPlanListMapByType(List<AdditionalBenefitPlanDto> adPlanSelections) {
		Map<String, Set<String>> adbPlanListMapByType = new HashMap<>();
		for (AdditionalBenefitPlanDto plan : adPlanSelections) {
			if (null != adbPlanListMapByType.get(plan.getPlanType())) {
				Set<String> plans = adbPlanListMapByType.get(plan.getPlanType());
				plans.add(plan.getBenefitPlan());
			} else {
				Set<String> plans = new TreeSet<>();
				plans.add(plan.getBenefitPlan());
				adbPlanListMapByType.put(plan.getPlanType(), plans);
			}
		}
		return adbPlanListMapByType;
	}
	
	/**
	 * Calculate the total cost of the options and set it to the plan.
	 * 
	 * @param planCostMap
	 * @param isRenewalCompany
	 * @param activeEligibleEECount
	 * @param additionalBenPlan
	 */
	public static void populatePlanOptionsCost(Map<String, BigDecimal> planCostMap,
			ActiveEligibleEECount activeEligibleEECount, AdditionalBenefitPlan additionalBenPlan, Company company) {
		BigDecimal primarySTDPlanCost = new BigDecimal(0);
		BigDecimal secondarySTDPlanCost = new BigDecimal(0);
		BigDecimal ltdPlanCost = new BigDecimal(0);
		BigDecimal primaryHC = (null == activeEligibleEECount) ? BigDecimal.ZERO
				: BigDecimal.valueOf(activeEligibleEECount.getPrimaryHeadCount());
		BigDecimal secondaryHC = (null == activeEligibleEECount) ? BigDecimal.ZERO
				: BigDecimal.valueOf(activeEligibleEECount.getSecondaryHeadCount());
		Boolean bundledOptionHasSdiPlan = false;
		for (DisabilityBenefitOptionPlans plan : additionalBenPlan.getOptionPlans()) {
			BigDecimal planCost = planCostMap.get(plan.getId());
			plan.setPlanCost(planCost);
			 logger.info("OPTION PLAN : {} \t PLAN TYPE : {} \t PLAN COST : {}", 
	                 plan.getId(), plan.getPlanType(), plan.getPlanCost());
			if (null == plan.getPlanCost() || plan.isEmployeePaid()) {
				continue;
			}
			if (BSSApplicationConstants.STD_CODE.equals(plan.getPlanType()) && plan.isPrimaryPlan()) {
				primarySTDPlanCost = planCost;
			} else if (BSSApplicationConstants.STD_CODE.equals(plan.getPlanType())) {
				secondarySTDPlanCost = planCost;
			} else {
				ltdPlanCost = planCost;
			}
			if (plan.isSdiPlan()) {
				bundledOptionHasSdiPlan = true;
			}
		}

		boolean isRenewalOrProspectCompany = company.isRenewalCompany() || company.isProspectCompany()
				|| company.isProspectConvertedClient() ? Boolean.TRUE : Boolean.FALSE;
		
		BigDecimal totalOptionCost = calculateTotalOptionCost(isRenewalOrProspectCompany, primarySTDPlanCost,
				secondarySTDPlanCost, ltdPlanCost, primaryHC, secondaryHC, bundledOptionHasSdiPlan);
		
		additionalBenPlan.setPlanCost(totalOptionCost);
		
		BigDecimal monthlyTotalCost = calculateMonthlyTotalCost(activeEligibleEECount, totalOptionCost, primaryHC, secondaryHC);
		additionalBenPlan.setMonthlyTotalCost(monthlyTotalCost);
	}

	private static BigDecimal calculateTotalOptionCost(boolean isRenewalOrProspectCompany,
			BigDecimal primarySTDPlanCost, BigDecimal secondarySTDPlanCost, BigDecimal ltdPlanCost,
			BigDecimal primaryHC, BigDecimal secondaryHC, Boolean bundledOptionHasSdiPlan) {
		BigDecimal totalOptionCost;
		if (Boolean.FALSE.equals(bundledOptionHasSdiPlan)) {
			if (secondarySTDPlanCost.compareTo(BigDecimal.ZERO) == 0) {
				secondarySTDPlanCost = primarySTDPlanCost;
			}
			if (primarySTDPlanCost.compareTo(BigDecimal.ZERO) == 0) {
				primarySTDPlanCost = secondarySTDPlanCost;
			}
		}

		if ((isRenewalOrProspectCompany)
				&& (primaryHC.compareTo(BigDecimal.ZERO) > 0 || secondaryHC.compareTo(BigDecimal.ZERO) > 0)) {
			totalOptionCost = ((((primarySTDPlanCost.multiply(primaryHC))
					.add((secondarySTDPlanCost.multiply(secondaryHC)))).divide(primaryHC.add(secondaryHC), 4,
							RoundingMode.HALF_UP)).add(ltdPlanCost));
		} else {
			totalOptionCost = primarySTDPlanCost.add(ltdPlanCost);
		}
		return totalOptionCost;
	}

	private static BigDecimal calculateMonthlyTotalCost(ActiveEligibleEECount activeEligibleEECount,
			BigDecimal totalOptionCost, BigDecimal primaryHC, BigDecimal secondaryHC) {
		BigDecimal monthlyTotalCost;
		BigDecimal totalHC;
		if (null != activeEligibleEECount && activeEligibleEECount.getTotalHeadCount() > 0) {
			totalHC = BigDecimal.valueOf(activeEligibleEECount.getTotalHeadCount());
		} else {
			totalHC = primaryHC.add(secondaryHC);
		}
		monthlyTotalCost = totalOptionCost.multiply(totalHC).setScale(2, RoundingMode.HALF_UP);
		return monthlyTotalCost;
	}

	public static void addCommuterBenefitPlan(Map<String, Map<String, Map<String, BenefitPlan>>> benefitGroupADPlansMap,
			XbssRealmPlyrPlan commuterPlan, List<BenefitGroup> benefitGroups) {
		
		Map<String, BenefitPlan> benefitPlanMap = createCommuterBenefitPlanMap(commuterPlan);
		for (BenefitGroup benefitGroup : benefitGroups) {
			if (!BSSApplicationConstants.K1_GROUP_TYPE.equals(benefitGroup.getType())) {
				String benefitProgram = benefitGroup.getBenefitProgram();
				if (benefitGroupADPlansMap.containsKey(benefitProgram)) {
					addBenefitPlanIfCommuterAbsent(benefitGroupADPlansMap, commuterPlan, benefitPlanMap, benefitProgram);
				} else {
					Map<String, Map<String, BenefitPlan>> benefitGroupPlansMap = new HashMap<>();
					benefitGroupPlansMap.put(commuterPlan.getPlanType(), benefitPlanMap);
					benefitGroupADPlansMap.put(benefitProgram, benefitGroupPlansMap);
				}
			}
		}
	}

	private static Map<String, BenefitPlan> createCommuterBenefitPlanMap(XbssRealmPlyrPlan commuterPlan) {
		BenefitPlan commuterBenefitPlan = new BenefitPlan();
		commuterBenefitPlan.setId(commuterPlan.getBenefitPlan());
		commuterBenefitPlan.setPlanType(commuterPlan.getPlanType());

		Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
		benefitPlanMap.put(commuterBenefitPlan.getId(), commuterBenefitPlan);
		return benefitPlanMap;
	}
	
	private static void addBenefitPlanIfCommuterAbsent(
			Map<String, Map<String, Map<String, BenefitPlan>>> benefitGroupADPlansMap, XbssRealmPlyrPlan commuterPlan,
			Map<String, BenefitPlan> benefitPlanMap, String benefitProgram) {
		boolean foundCommuter = false;
		Map<String, Map<String, BenefitPlan>> benefitGroupPlansMap = benefitGroupADPlansMap
				.get(benefitProgram);

		for (Entry<String, Map<String, BenefitPlan>> adPlans : benefitGroupPlansMap.entrySet()) {
			if (PlanTypesEnum.CMTR.getCode().equals(adPlans.getKey())) {
				foundCommuter = true;
				break;
			}
		}
		if (!foundCommuter) {
			benefitGroupPlansMap.put(commuterPlan.getPlanType(), benefitPlanMap);
		}
	}

}
