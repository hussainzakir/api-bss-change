package com.trinet.ambis.helper;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.RelationEnum;
import com.trinet.ambis.service.model.PlanRateDto;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;
import com.trinet.ambis.util.DateUtils;
import com.trinet.ambis.util.Utils;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class RateServiceHelper {

	private RateServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + RateServiceHelper.class.getName() + " can not be instantiated.");
	}

	/**
	 * This method returns the plan cost based on the plan rate and coverage level.
	 *
	 * @param planRate
	 * 		   The PlanRateDto object containing plan rate information.
	 * @param covgLevelCode
	 * 		  The coverage level code.
	 * @param employee
	 * 		  The ProspectCensusResponse object containing employee information.
	 */
	public static BigDecimal getPlanCost(PlanRateDto planRate, String covgLevelCode, ProspectCensusResponse employee,
										 LocalDate benefitStartDate) {
		boolean is4Tiers = is4Tiers(planRate);
		return is4Tiers ? get4TierPlanCost(planRate, covgLevelCode)
				: getAgeBandedPlanCost(planRate, getAge(employee.getDob(), benefitStartDate), covgLevelCode, employee.getDependents(), benefitStartDate);
	}

	/**
	 * This method checks if the plan rate is 4-tiered.
	 *
	 * @param planRate
	 * 		   The PlanRateDto object containing plan rate information.
	 * @return true if the plan rate is 4-tiered, false otherwise.
	 */
	public static boolean is4Tiers(PlanRateDto planRate) {
		return RateTypeEnum.FOUR_TIER.getCode().equals(planRate.getRateTypeCode());
	}

	/**
	 * This method calculates the employer contribution based on the coverage level plan cost, group funding,
	 * plan rate, benefit type code, coverage level code, and employee age.
	 *
	 * @param cvgLvlPlanCost
	 * 		   The coverage level plan cost.
	 * @param groupFunding
	 * 		  The group funding map containing funding details.
	 * @param planRate
	 * 		  The PlanRateDto object containing plan rate information.
	 * @param benefitTypeCode
	 * 		  The benefit type code.
	 * @param covgLevelCode
	 * 		  The coverage level code.
	 * @param is4Tiers
	 * 		  Indicates if the plan is 4-tiered.
	 * @param eeAge
	 * 		  The employee's age.
	 */
	public static BigDecimal getEmployerContribution(BigDecimal cvgLvlPlanCost, Map<String, Map<String, Object>> groupFunding,
													 PlanRateDto planRate, String benefitTypeCode, String covgLevelCode, boolean is4Tiers, int eeAge) {
		Map<String, Object> fundingDetails = groupFunding.get(benefitTypeCode);
		BigDecimal planAssignmentFundingValue = getFundingValue(fundingDetails, covgLevelCode);

		BigDecimal employerContribution;
		if (fundingDetails.get(BSSApplicationConstants.FUNDING_TYPE).equals(BSSApplicationConstants.CFPCT)) {
			if (isEmployeeOnlyCoverageLevel(covgLevelCode)) {
				employerContribution = applyFunding(cvgLvlPlanCost, planAssignmentFundingValue);
			} else {
				BigDecimal eeFundingValue = getFundingValue(fundingDetails, CoverageCodesEnums.COV_EMPLOYEE.getCode());
				String eeCvgLvlCode = CoverageCodesEnums.COV_EMPLOYEE.getCode();
				BigDecimal employeeLevelCost = is4Tiers ? get4TierPlanCost(planRate, eeCvgLvlCode)
						: getAgeBandedPlanCost(planRate, eeAge, eeCvgLvlCode, null, null);
				employerContribution = calculateCfpctERContribution(cvgLvlPlanCost, employeeLevelCost, eeFundingValue,
						planAssignmentFundingValue);
			}
			return employerContribution;
//			return applyCapOnEmployerContribution(employerContribution, covgLevelCode,
//					groupFunding.get(benefitTypeCode).getCvgFundLimitDetails());
		} else if (fundingDetails.get(BSSApplicationConstants.FUNDING_TYPE).equals(BSSApplicationConstants.FLAT)) {
			return calculateWhenFundingTypeFLT(planAssignmentFundingValue, cvgLvlPlanCost);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * This method checks if the given BigDecimal value is not null and not zero.
	 *
	 * @param cost
	 * 		   The BigDecimal value to check.
	 * @return true if the value is not null and not zero, false otherwise.
	 */
	public static boolean isValueNotNullAndNotZero(BigDecimal cost) {
		return null != cost && !BigDecimal.ZERO.equals(cost);
	}

	private static BigDecimal get4TierPlanCost(PlanRateDto planRate, String covgLevelCode) {
		return planRate.getTieredCost().getOrDefault(covgLevelCode, BigDecimal.ZERO);
	}

	private static BigDecimal getAgeBandedPlanCost(PlanRateDto planRate, int eeAge, String covgLevelCode,
									List<ProspectCensusResponse.Dependents> eeDependents, LocalDate benefitStartDate) {

		BigDecimal employeePlanCost = planRate.getTieredCost().getOrDefault(getAge(eeAge), BigDecimal.ZERO);

		BigDecimal allDependentPlanCost = BigDecimal.ZERO;

		if (CollectionUtils.isNotEmpty(eeDependents)) {

			List<Integer> allDependentsAge = validateDependentDetails(eeDependents, planRate.getCarrierAgeLimit(), covgLevelCode, benefitStartDate);
			allDependentPlanCost = allDependentsAge.stream()
					.filter(dependentAge -> dependentAge >= 0)
					.map(dependentAge -> planRate.getTieredCost().getOrDefault(getAge(dependentAge), BigDecimal.ZERO))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
		}

		return employeePlanCost.add(allDependentPlanCost);
	}

	private static String getAge(int employeeAge) {
		return String.valueOf((employeeAge <= 14) ? 0 : Math.min(employeeAge, 64));
	}

	private static List<Integer> validateDependentDetails(List<ProspectCensusResponse.Dependents> eeDependents,
														  Integer carrierAgeLimit,
														  String covgLevelCode, LocalDate benefitStartDate) {
		List<ProspectCensusResponse.Dependents> spousesOrPartners = new ArrayList<>();
		List<ProspectCensusResponse.Dependents> children = new ArrayList<>();

		for (ProspectCensusResponse.Dependents dependent : eeDependents) {
			if (dependent.isCovgElection() && dependent.isIncludeInCost()) {
				String relation = dependent.getRelation();
				if (RelationEnum.SPOUSE.getCode().equals(relation) ||
						RelationEnum.DOMESTIC_PARTNER.getCode().equals(relation)) {
					spousesOrPartners.add(dependent);
				} else if (RelationEnum.CHILD.getCode().equals(relation)) {
					children.add(dependent);
				}
			}
		}

		List<Integer> ageOfDependentsToBeIncludedInCost = new ArrayList<>();

		CoverageCodesEnums coverageCode = CoverageCodesEnums.getByCoverageCode(covgLevelCode);

		switch (coverageCode) {
			case COV_EMPLOYEE_PLUS_SPOUSE:
				ageOfDependentsToBeIncludedInCost.addAll(getAgeOfPartnerToBeIncludedInCost(spousesOrPartners, benefitStartDate));
				break;
			case COV_EMPLOYEE_PLUS_CHILD:
				ageOfDependentsToBeIncludedInCost.addAll(getAgeOfChildrenToBeIncludedInCost(children, carrierAgeLimit, benefitStartDate));
				break;
			case COV_EMPLOYEE_FAMILY:
				ageOfDependentsToBeIncludedInCost.addAll(getAgeOfPartnerToBeIncludedInCost(spousesOrPartners, benefitStartDate));
				ageOfDependentsToBeIncludedInCost.addAll(getAgeOfChildrenToBeIncludedInCost(children, carrierAgeLimit, benefitStartDate));
				break;
			default:
				// unknown level
				break;
		}

		return ageOfDependentsToBeIncludedInCost;
	}

	private static List<Integer> getAgeOfPartnerToBeIncludedInCost(
			List<ProspectCensusResponse.Dependents> listOfPartners, LocalDate benefitStartDate) {
		return listOfPartners.stream()
				.filter(dependent -> RelationEnum.SPOUSE.getCode().equals(dependent.getRelation())
						|| RelationEnum.DOMESTIC_PARTNER.getCode().equals(dependent.getRelation()))
				.map(dependent -> getAge(dependent.getDob(), benefitStartDate)).collect(Collectors.toList());
	}

	private static List<Integer> getAgeOfChildrenToBeIncludedInCost(
			List<ProspectCensusResponse.Dependents> listOfChildren, Integer carrierAgeLimit, LocalDate benefitStartDate) {

		List<Integer> ageOfChildrenToBeIncludedInCost = new ArrayList<>();

		List<ProspectCensusResponse.Dependents> listOfDependentChildrenSortedByAge = listOfChildren.stream()
				.sorted(comparing((ProspectCensusResponse.Dependents child) -> getAge(child.getDob(), benefitStartDate)).reversed())
				.collect(Collectors.toList());

		if (carrierAgeLimit != null) {
			// Separate lists for children above and under/equal to the carrier age limit
			List<Integer> aboveAgeLimit = new ArrayList<>();
			List<Integer> underOrEqualAgeLimit = new ArrayList<>();

			for (ProspectCensusResponse.Dependents dependent : listOfDependentChildrenSortedByAge) {
				int dependentAge = getAge(dependent.getDob(), benefitStartDate);

				if (dependentAge > carrierAgeLimit) {
					// Always include children above the carrier age limit
					aboveAgeLimit.add(dependentAge);
				} else if (underOrEqualAgeLimit.size() < 3) {
					// Include up to 3 children who are under or equal to the age limit
					underOrEqualAgeLimit.add(dependentAge);
				}
			}

			ageOfChildrenToBeIncludedInCost.addAll(aboveAgeLimit);
			ageOfChildrenToBeIncludedInCost.addAll(underOrEqualAgeLimit);
		} else {
			// If no age limit is defined, include the top 3 oldest children
			ageOfChildrenToBeIncludedInCost = listOfDependentChildrenSortedByAge.stream()
					.limit(3)
					.map(dep -> getAge(dep.getDob(), benefitStartDate))
					.collect(Collectors.toList());
		}

		return ageOfChildrenToBeIncludedInCost;
	}

	private static BigDecimal getFundingValue(Map<String, Object> fundingDetails, String covgLevel) {
		String fundingCoverageName = CoverageCodesEnums.valueOfId(covgLevel);
		return (BigDecimal) fundingDetails.get(fundingCoverageName);
	}

	private static boolean isEmployeeOnlyCoverageLevel(String covgLevelCode) {
		return (covgLevelCode).equals(CoverageCodesEnums.COV_EMPLOYEE.getCode());
	}

	private static BigDecimal calculateWhenFundingTypeFLT(BigDecimal fundingValue, BigDecimal coverageCost)
	{
		return isValueNotNullAndNotZero(fundingValue) ? fundingValue.min(coverageCost) : BigDecimal.ZERO;
	}

	private static BigDecimal applyFunding(BigDecimal planCost, BigDecimal fundingValue) {
		if (isValueNotNullAndNotZero(planCost) && isValueNotNullAndNotZero(fundingValue)) {
			return planCost.multiply(fundingValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		}
		return BigDecimal.ZERO;
	}

	private static BigDecimal calculateCfpctERContribution(BigDecimal cvgLvlPlanCost,BigDecimal employeeLevelCost,
															  BigDecimal eeFundingValue, BigDecimal planAssignmentFundingValue) {

		// Calculate employee ER contribution
		BigDecimal employeeLevelErContribution = applyFunding(employeeLevelCost, eeFundingValue);

		// For Spouse/Children/Family Only
		BigDecimal dependentPortionPlanCost = isValueNotNullAndNotZero(cvgLvlPlanCost)
				? cvgLvlPlanCost.subtract(employeeLevelCost)
				: BigDecimal.ZERO;

		// Calculate dependent ER contribution
		BigDecimal dependentPortionErContribution = applyFunding(dependentPortionPlanCost,
				planAssignmentFundingValue);

		return dependentPortionErContribution.add(employeeLevelErContribution);
	}

	public static int getAge(String dob, LocalDate benefitStartDate) {
		return DateUtils.calculateAgeUntilDate(Utils.convertStringToLocalDate(dob, BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD), benefitStartDate);
	}

//
//	private BigDecimal applyCapOnEmployerContribution(BigDecimal employerContribution, String covgLevelCode,
//			Map<String, BigDecimal> cvgFundLimitDetails) {
//
//		if (MapUtils.isNotEmpty(cvgFundLimitDetails) && Objects.nonNull(cvgFundLimitDetails.get(covgLevelCode))) {
//			BigDecimal covgLevelCapValue = cvgFundLimitDetails.get(covgLevelCode);
//			employerContribution = covgLevelCapValue.compareTo(employerContribution) <= 0 ? covgLevelCapValue
//					: employerContribution;
//		}
//
//		return employerContribution;
//	}
//

}
