/**
 * 
 */
package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyFundBsuppPlanTypeId;
import com.trinet.ambis.persistence.model.StrategyFundBsuppPlanTypes;
import com.trinet.ambis.persistence.model.StrategyFundingBasePlanLimits;
import com.trinet.ambis.persistence.model.StrategyFundingDetail;
import com.trinet.ambis.persistence.model.StrategyFundingDetailId;
import com.trinet.ambis.persistence.model.StrategyFundingFlatMax;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.model.BenefitGroupRateMapper;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.GroupRuleDto.PlanTypeRule;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.RegionalMinimumFunding;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author rvutukuri
 *
 */
public class RenewalServiceHelper {
	private static final Logger logger = LoggerFactory.getLogger(RenewalServiceHelper.class);
	
	private RenewalServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + RenewalServiceHelper.class.getName() + " can not be instantiated.");
	}

	/**
	 * This method for getting the plan level head counts from a List.
	 * 
	 * @param list
	 * @param plan
	 * @return coverage level head counts for a BenefitPlan
	 */
	public static long getHeadCount(List<PlanCoverageLevelHeadCount> list, String plan) {
		logger.debug("Entering method : getHeadCount");
		for (PlanCoverageLevelHeadCount headCount : list) {
			if (headCount.getBenefitPlan().equals(plan)) {
				return headCount.getHeadCount();
			}
		}
		return 0;
	}

	/**
	 * This method is for getting head count for a coverage id.
	 * 
	 * @param headCountList
	 * @param covrgId
	 * @return coverage level head count.
	 */
	public static int getCovrgHeadCount(List<PlanCoverageLevelHeadCount> headCountList, String covrgId) {
		int count = 0;
 		PlanCoverageLevelHeadCount planCoverageLevelHeadCount = headCountList.stream().filter(item -> item.getCovrgCode().equals(covrgId)).findFirst().orElse(null);
 		count = planCoverageLevelHeadCount == null ? 0 : planCoverageLevelHeadCount.getHeadCount();
		return count;
	}

	/**
	 * This method is for getting HSA head count for a coverage id.
	 * 
	 * @param headCountList
	 * @param covrgId
	 * @return coverage level head count.
	 */
	public static int getCovrgHsaHeadCount(List<PlanCoverageLevelHeadCount> headCountList, String covrgId) {
		int count = 0;
 		PlanCoverageLevelHeadCount planCoverageLevelHeadCount = headCountList.stream().filter(item -> item.getCovrgCode().equals(covrgId)).findFirst().orElse(null);
 		count = planCoverageLevelHeadCount == null ? 0 : planCoverageLevelHeadCount.getHsaHeadCount();
		return count;
	}
	
	/**
	 * 
	 * @param mirroHeadCountCvgLevel
	 * @param covrgId
	 * @return
	 */
	public static int getMirrorPlanCvgLevelHeadCount(Map<String, Long> mirroHeadCountCvgLevel, String covrgId) {
		int count = 0;
		if (null != mirroHeadCountCvgLevel.get(covrgId)) {
			count = mirroHeadCountCvgLevel.get(covrgId).intValue();
		}
		return count;
	}

	/**
	 * This method is for constructing benefit group for a renewal company.
	 * 
	 * @param bg
	 * @param company
	 * @param groupHeadCountMap
	 * @param rateTableIds
	 * @param eligRuleId
	 * @param waitPeriodMap
	 */
	public static void constructBenefitGroupForRenewalCompany(BenefitGroup bg, Company company,
			Map<String, Integer> groupHeadCountMap, Map<String, String> rateTableIds, String eligRuleId,
			Map<String, String> waitPeriodMap) {
		logger.debug("Entering method : constructBenefitGroupForRenewal");
		String benefitProgram = bg.getBenefitProgram();
		String waitPeriod = waitPeriodMap.get(benefitProgram);
		if (groupHeadCountMap != null && groupHeadCountMap.get(benefitProgram) != null) {
			bg.setHeadcount(groupHeadCountMap.get(benefitProgram));
		} else {
			bg.setHeadcount(0);
		}
		bg.setWaitingPeriod(waitPeriod);
		bg.setEligRuleId(eligRuleId);
		bg.setCompanyId(company.getId());
		if (null == bg.getType()) {
			bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		}
		bg.setDefaultGroup(benefitProgram.equals(company.getBenefitProgram()));
		bg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		if (null != rateTableIds) {
			bg.getGroupRate().addAll(BenefitGroupRateMapper.convertMapToGroupRate(rateTableIds, bg));

		}
		logger.debug("Exiting method : constructBenefitGroupForRenewal");
	}
	
	/**
	 * This method is for calculating the funding based on Base funding.
	 * @param benefitPlan
	 * @param planSelection
	 * @param rates
	 * @param headCountList
	 * @param coverageLevelFunding
	 * @param planOverrides
	 * @return
	 */
	public static List<Contribution> createUpdateContributionsByBaseFunding(BenefitPlan benefitPlan,
			PlanSelection planSelection, Map<String, List<BenefitPlanRate>> rates,
			List<PlanCoverageLevelHeadCount> headCountList, Map<String, Object> coverageLevelFunding,
			Map<String, Map<String, String>> planOverrides,
			boolean isBandCodeUpdated) {
		logger.debug("Entering method : constructContributions");
		List<Contribution> contributions = new ArrayList<>();
		List<PlanContribution> planContributions = benefitPlan.getContributions();
		BigDecimal employeeCvgContribution = null;
		BigDecimal employeeCvgPlanCost = null;
		String limitCoverageLevel = null;
		BigDecimal limitCoverageCost = null;
		String limitPlan = null;
		for (PlanContribution planContribution : planContributions) {
			boolean isContributionCreated = false;
			String coverageLevel = planContribution.getType();
			String coverageCode = CoverageCodesEnums.valueOfCode(coverageLevel);
			int headCount = getHeadCount(headCountList, planContribution, coverageCode);
			int hsaHeadCount = getHsaHeadCount(headCountList, planContribution, coverageCode);
			
			String fundingType = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
			limitPlan = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN);
			limitCoverageLevel = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG);
			BigDecimal limitPct = (BigDecimal) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_PCT);
			String primaryPlanType = (String) coverageLevelFunding.get(BSSApplicationConstants.PRIMARY_PLAN_TYPE);

			BigDecimal employerPercent = planContribution.getEmployerPercent() != null
					? planContribution.getEmployerPercent()
					: BigDecimal.ZERO;
			BigDecimal employerContribution = planContribution.getEmployerContribution() != null
					? planContribution.getEmployerContribution()
					: BigDecimal.ZERO;
			BigDecimal employerContributionBase = planContribution.getEmployerContribution() != null
							? planContribution.getEmployerContribution()
							: BigDecimal.ZERO;
			BigDecimal employeeContribution = planContribution.getEmployeeContribution() != null
					? planContribution.getEmployeeContribution()
					: BigDecimal.ZERO;
			String overrideType = determineOverrideType(benefitPlan, planOverrides, isBandCodeUpdated, planContribution,
					coverageCode, fundingType);
			Long planSelectionId = null != planSelection ? planSelection.getId() : null ;
			
			logger.info("BenefitPlan Plan Type: {}", benefitPlan.getPlanType());
			logger.info("BenefitPlan Id: {}", benefitPlan.getId());
			logger.info("BenefitPlan planSelectionId: {}", planSelectionId);
			logger.info("BenefitPlan fundingType: {}", fundingType);
			logger.info("BenefitPlan limitPlan: {}", limitPlan);
			logger.info("BenefitPlan old employerContribution: {}", employerContribution);
			logger.info("BenefitPlan old employeeContribution: {}", employeeContribution);
			logger.info("BenefitPlan old employerPercent: {}", employerPercent);
			logger.info("BenefitPlan overrideType: {}", overrideType);
			logger.info("BenefitPlan limitCoverageLevel: {}", limitCoverageLevel);

			List<BenefitPlanRate> planRates = rates.get(benefitPlan.getId());
			if (planRates == null || planRates.isEmpty()) {
				logger.info("No Plan rate found for the Benefit Plan: {}", benefitPlan.getId());
				continue;
			}

			for (BenefitPlanRate planRate : planRates) {
				String coverageId = CoverageCodesEnums.valueOfId(planRate.getCoverageCode());
				Contribution contribution = null;
				if (coverageId.equals(planContribution.getType())) {
					isContributionCreated = true;
					BigDecimal newPlanCost = planRate.getEmployerCost();
					logger.info("BenefitPlan newPlanCost: {}", newPlanCost);
					logger.info("BenefitPlan bandcode: {}", planRate.getBandCode());
					if (BSSApplicationConstants.VOLUNTARY_PLAN_TYPES.contains(benefitPlan.getPlanType())) {
						employerContribution = BigDecimal.ZERO;
					} else if (!benefitPlan.getPlanType().equals(primaryPlanType)) {
						employerContribution = newPlanCost.multiply(employerPercent).divide(Constants.BigDecimal_100)
								.setScale(2, RoundingMode.HALF_UP);
					} else if (((BSSApplicationConstants.BFPCT.equals(fundingType) && StringUtils.isNotBlank(limitPlan)
							&& !BSSApplicationConstants.FLAT_MAX.equals(limitPlan))
							|| BSSApplicationConstants.FLAT.equals(fundingType)
							|| BSSApplicationConstants.BSUPP.equals(fundingType))
							&& BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(overrideType)) {
						employerContribution = (BigDecimal) coverageLevelFunding.get(coverageLevel);
						if (newPlanCost.compareTo(employerContribution) < 0) {
							employerContribution = newPlanCost;
						}
					} else if (!BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(overrideType)) {
						if (BSSApplicationConstants.PLAN_OVERRIDE_FLTEE.equals(overrideType)) {
							employerContribution = calculateEecEmployerContribution(coverageLevelFunding, coverageLevel,
									newPlanCost);
						} else if (null == employerPercent || 0L == employerPercent.longValue()) {
							employerContribution = BigDecimal.ZERO;
						} else {
							if (BSSApplicationConstants.PLAN_OVERRIDE_PCT.equals(overrideType)) {
								employerContribution = newPlanCost.multiply(employerPercent)
										.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
							} else {
								if (newPlanCost.compareTo(employerContribution) < 0) {
									employerContribution = newPlanCost;
								}
							}
						}
					} else if (BSSApplicationConstants.BFPCT.equals(fundingType)
							&& BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(overrideType)
							&& BSSApplicationConstants.FLAT_MAX.equals(limitPlan)) {
						employerPercent = (BigDecimal) coverageLevelFunding.get(coverageLevel);
						employerContribution = newPlanCost.multiply(employerPercent).divide(Constants.BigDecimal_100)
								.setScale(2, RoundingMode.HALF_UP);
						BigDecimal employeeCvgLimit = (BigDecimal) coverageLevelFunding
								.get(coverageLevel + BSSApplicationConstants.LIMIT);
						if (null != employeeCvgLimit && employeeCvgLimit.compareTo(employerContribution) < 0) {
							employerContribution = employeeCvgLimit;
						} else {
							employerContributionBase = employerContribution;
						}
					} else if (BSSApplicationConstants.BFPCT.equals(fundingType)
							&& BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(overrideType)) {
						if (null != limitCoverageLevel) {
							if (limitCoverageLevel.equals(coverageId)) {
								limitCoverageCost = newPlanCost.multiply(limitPct).divide(Constants.BigDecimal_100)
										.setScale(2, RoundingMode.HALF_UP);
							}
						} else {
							employerPercent = (BigDecimal) coverageLevelFunding.get(coverageLevel);
							employerContribution = newPlanCost.multiply(employerPercent)
									.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
						}
					} else {
						employerPercent = (BigDecimal) coverageLevelFunding.get(coverageLevel);
						employerContribution = newPlanCost.multiply(employerPercent).divide(Constants.BigDecimal_100)
								.setScale(2, RoundingMode.HALF_UP);
						BigDecimal employeeCvgLimit = (BigDecimal) coverageLevelFunding.get(coverageLevel + BSSApplicationConstants.LIMIT);
						if (null != employeeCvgLimit && employeeCvgLimit.compareTo(employerContribution) < 0) {
							employerContributionBase = employerContribution;
							employerContribution = employeeCvgLimit;
						}else {
							employerContributionBase = employerContribution;
						}
					}
					employeeContribution = newPlanCost.subtract(employerContribution);
					employerPercent = employerContribution.divide(newPlanCost, 10, RoundingMode.CEILING)
							.multiply(Constants.BigDecimal_100);

					// calculating the employee base coverage level contribution when the override
					// type is not BASE.
					if (CoverageCodesEnums.COV_EMPLOYEE.getCode().equals(coverageCode)) {
						employeeCvgPlanCost = newPlanCost;
						if (BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(overrideType)) {
							BigDecimal employeeCvgLimit = (BigDecimal) coverageLevelFunding.get(coverageLevel + BSSApplicationConstants.LIMIT);
							if(null != employeeCvgLimit) {
								employeeCvgContribution = employerContributionBase;
							}else {
								employeeCvgContribution = employerContribution;
							}
						} else {
							if (((BSSApplicationConstants.BFPCT.equals(fundingType) && StringUtils.isNotBlank(limitPlan)
									&& !BSSApplicationConstants.FLAT_MAX.equals(limitPlan))
									|| BSSApplicationConstants.FLAT.equals(fundingType)
									|| BSSApplicationConstants.BSUPP.equals(fundingType))) {
								employeeCvgContribution = (BigDecimal) coverageLevelFunding.get(coverageLevel);
								if (newPlanCost.compareTo(employeeCvgContribution) < 0) {
									employeeCvgContribution = newPlanCost;
								}
							} else if (BSSApplicationConstants.BFPCT.equals(fundingType)) {
								if(CoverageCodesEnums.COV_EMPLOYEE.getId().equals(limitCoverageLevel)) {
									employeeCvgContribution = newPlanCost.multiply(limitPct)
											.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
								}
							} else {
								BigDecimal employerBasePercent = (BigDecimal) coverageLevelFunding.get(coverageLevel);
								employeeCvgContribution = newPlanCost.multiply(employerBasePercent)
										.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
							}
						}
					}
					logger.info("BenefitPlan new employerContribution: {}", employerContribution);
					logger.info("BenefitPlan new employeeContribution: {}", employeeContribution);
					logger.info("BenefitPlan new employerPercent: {}", employerPercent);

					contribution = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount, planSelectionId,
							employerContribution, employeeContribution, employerPercent);
					if (isBandCodeUpdated) {
						contribution.setId(planContribution.getId());
						contribution.setBenefitPlan(planContribution.getBenefitPlanId());
					}
					contribution.setBenefitPlanAssociation(benefitPlan);
					contribution.setOverrideType(overrideType);
					contributions.add(contribution);
				}
			}
			if (!isContributionCreated && !isBandCodeUpdated) {
				Contribution contribution = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount,
						planSelectionId, employerContribution, employeeContribution, employerPercent);
				contribution.setBenefitPlanAssociation(benefitPlan);
				contribution.setOverrideType(overrideType);
				contributions.add(contribution);
			}
		}
		if (null != employeeCvgContribution && BigDecimal.ZERO.compareTo(employeeCvgContribution) < 0) {
			updateContributionByEmployeeContributions(contributions, employeeCvgPlanCost, employeeCvgContribution,
					coverageLevelFunding);
		}
		if (null != limitCoverageCost && BigDecimal.ZERO.compareTo(limitCoverageCost) < 0) {
			updateContributionByLimitContributions(contributions, limitCoverageCost);
		}
		logger.debug("Exiting method : constructContributions");
		return contributions;
	}

	private static String determineOverrideType(BenefitPlan benefitPlan, Map<String, Map<String, String>> planOverrides,
			boolean isBandCodeUpdated, PlanContribution planContribution, String coverageCode, String fundingType) {
		String overrideType = BSSApplicationConstants.PLAN_OVERRIDE_BASE;
		if (isBandCodeUpdated) {
			overrideType = PlanOverrideServiceHelper.getRenewalPlanOverrideType(planContribution.getOverrideType());
		} else if (BSSApplicationConstants.EEC.equals(fundingType)) {
			overrideType = BSSApplicationConstants.PLAN_OVERRIDE_FLTEE;
		} else {
			Map<String, String> coverageOverrides = null;
			if (null != planOverrides) {
				coverageOverrides = planOverrides.get(benefitPlan.getId());
				if (null != coverageOverrides && null != coverageOverrides.get(coverageCode)) {
					overrideType = coverageOverrides.get(coverageCode);
				}
			}
		}
		return overrideType;
	}

	private static int getHsaHeadCount(List<PlanCoverageLevelHeadCount> headCountList,
			PlanContribution planContribution, String coverageCode) {
		int hsaHeadCount = 0;
		if (null != headCountList && !headCountList.isEmpty()) {
			hsaHeadCount = getCovrgHsaHeadCount(headCountList, coverageCode);
		} else {
			hsaHeadCount = planContribution.getHsaHeadcount();
		}
		return hsaHeadCount;
	}

	private static int getHeadCount(List<PlanCoverageLevelHeadCount> headCountList, PlanContribution planContribution,
			String coverageCode) {
		int headCount = 0;
		// need head count for the mapped plan.
		if (null != headCountList && !headCountList.isEmpty()) {
			headCount = getCovrgHeadCount(headCountList, coverageCode);
		} else {
			headCount = planContribution.getHeadcount();
		}
		return headCount;
	}

	/**
	 * This method is for constructing contributions on shared cost by
	 * EMP/EMPLYR
	 *
	 * @param benefitPlan
	 * @param planSelection
	 * @param employerPercentIncrease
	 * @param rates
	 * @param headCountList
	 * @param contribList
	 */
	public static void constructContributionsByPercentIncrease(BenefitPlan benefitPlan,
			PlanSelection planSelection, BigDecimal employerPercentIncrease, Map<String, List<BenefitPlanRate>> rates,
			List<PlanCoverageLevelHeadCount> headCountList, List<Contribution> contribList,
			Map<String, Map<String, String>> planOverrides) {
		List<PlanContribution> planContributions = benefitPlan.getContributions();
		for (PlanContribution planContribution : planContributions) {
			boolean isContributionCreated = false;
			String planLevelOverride = getPlanLevelOverride(benefitPlan);
			String coverageLevel = planContribution.getType();
			String coverageCode = CoverageCodesEnums.valueOfCode(coverageLevel);
			int headCount = getHeadCount(headCountList, planContribution, coverageCode);
			int hsaHeadCount = getHsaHeadCount(headCountList, planContribution, coverageCode);

			BigDecimal employerPercent = planContribution.getEmployerPercent() != null
					? planContribution.getEmployerPercent() : BigDecimal.ZERO;
			BigDecimal employerContribution = planContribution.getEmployerContribution() != null
					? planContribution.getEmployerContribution() : BigDecimal.ZERO;
			BigDecimal employeeContribution = planContribution.getEmployeeContribution() != null
					? planContribution.getEmployeeContribution() : BigDecimal.ZERO;
			Long planSelectionId = null;
			if (null != planSelection) {
				planSelectionId = planSelection.getId();
			}
			logger.info("GETTING RATES FOR PLAN : {}", benefitPlan.getId());
			List<BenefitPlanRate> planRates = rates.get(benefitPlan.getId());

			if (planRates == null || planRates.isEmpty()) {
				logger.info("**** NO PLAN RATES FOUND FOR BENEFIT PLAN  : {}", benefitPlan.getId());
				continue;
			}
			for (BenefitPlanRate planRate : planRates) {
				String coverageId = CoverageCodesEnums.valueOfId(planRate.getCoverageCode());
				Contribution contribution = null;
				if (coverageId.equals(planContribution.getType())) {
					isContributionCreated = true;
					BigDecimal newPlanCost = planRate.getEmployerCost();
					BigDecimal planCost = employerContribution.add(employeeContribution);
					BigDecimal planCostDiff = newPlanCost.subtract(planCost);
					if (BSSApplicationConstants.VOLUNTARY_PLAN_TYPES.contains(benefitPlan.getPlanType())) {
						employeeContribution = newPlanCost;
						employerContribution = BigDecimal.ZERO;
					} else {
						// Company Pays 100% of the Cost Changes
						if (Constants.BigDecimal_100.equals(employerPercentIncrease)) {
							if (planCostDiff.compareTo(BigDecimal.ZERO) > 0) {
								employerContribution = employerContribution.add(planCostDiff);
								employeeContribution = newPlanCost.subtract(employerContribution);
							} else {
								BigDecimal employerPortion = employerContribution.add(planCostDiff);

								if (employerPortion.compareTo(BigDecimal.ZERO) < 0) {
									employerContribution = BigDecimal.ZERO;
									employeeContribution = newPlanCost;
								} else {
									employerContribution = employerPortion;
									employeeContribution = newPlanCost.subtract(employerContribution);
								}
							}
							// Share Cost Changes between employee and employer
						} else if (Constants.BigDecimal_50.equals(employerPercentIncrease)) {
							if (planCostDiff.compareTo(BigDecimal.ZERO) > 0) {
								employerContribution = employerContribution.add(planCostDiff.divide(new BigDecimal(2)));
								employeeContribution = newPlanCost.subtract(employerContribution);
							} else {
								BigDecimal fiftyPercent = planCostDiff.divide(new BigDecimal(2));
								BigDecimal employerPortion = employerContribution.add(fiftyPercent);
								BigDecimal employeePortion = newPlanCost.subtract(employerPortion);

								if (employerPortion.compareTo(BigDecimal.ZERO) < 0) {
									employerContribution = BigDecimal.ZERO;
									employeeContribution = newPlanCost;
								} else if (employeePortion.compareTo(BigDecimal.ZERO) < 0) {
									employeeContribution = BigDecimal.ZERO;
									employerContribution = newPlanCost;
								} else {
									employerContribution = employerPortion;
									employeeContribution = employeePortion;
								}
							}
							// Employees Pay the Cost Changes
						} else if (BigDecimal.ZERO.equals(employerPercentIncrease)) {
							if (planCostDiff.compareTo(BigDecimal.ZERO) > 0) {
								employeeContribution = employeeContribution.add(planCostDiff);
								employerContribution = newPlanCost.subtract(employeeContribution);
							} else {
								BigDecimal employeePortion = employeeContribution.add(planCostDiff);

								if (employeePortion.compareTo(BigDecimal.ZERO) < 0) {
									employeeContribution = BigDecimal.ZERO;
									employerContribution = newPlanCost;
								} else {
									employeeContribution = employeePortion;
									employerContribution = newPlanCost.subtract(employeeContribution);
								}
							}
						} else {
							if (null != planOverrides && null != planOverrides.get(benefitPlan.getId())
									&& !planOverrides.get(benefitPlan.getId()).isEmpty()) {
								Map<String, String> coverageOverrides = planOverrides.get(benefitPlan.getId());
								planLevelOverride = coverageOverrides.get(coverageCode);
							} else {
								planLevelOverride = BSSApplicationConstants.PLAN_OVERRIDE_PCT;
							}
							if (BSSApplicationConstants.PLAN_OVERRIDE_PCT.equals(planLevelOverride)) {
								employerContribution = newPlanCost.multiply(employerPercent)
										.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
								employeeContribution = newPlanCost.subtract(employerContribution).setScale(2,
										RoundingMode.HALF_UP);

							} else if (BSSApplicationConstants.PLAN_OVERRIDE_FLTEE.equals(planLevelOverride)) {
								if (planCostDiff.compareTo(BigDecimal.ZERO) > 0) {
									employerContribution = employerContribution.add(planCostDiff);
									employeeContribution = newPlanCost.subtract(employerContribution);
								} else {
									BigDecimal employerPortion = employerContribution.add(planCostDiff);
									if (employerPortion.compareTo(BigDecimal.ZERO) < 0) {
										employerContribution = BigDecimal.ZERO;
										employeeContribution = newPlanCost;
									} else {
										employerContribution = employerPortion;
										employeeContribution = newPlanCost.subtract(employerContribution);
									}
								}
							} else {
								if (newPlanCost.compareTo(employerContribution) < 0) {
									employerContribution = newPlanCost;
								}
								employeeContribution = newPlanCost.subtract(employerContribution);
							}
						}
					}
					if (!BigDecimal.ZERO.equals(newPlanCost)) {
						employerPercent = employerContribution.divide(newPlanCost, 10, RoundingMode.CEILING)
								.multiply(Constants.BigDecimal_100);
					} else {
						employerPercent = BigDecimal.ZERO;

					}
					contribution = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount, planSelectionId,
							employerContribution, employeeContribution, employerPercent);
					contribution.setBenefitPlanAssociation(benefitPlan);
					contribution.setOverrideType(planLevelOverride);
					contribList.add(contribution);
				}
			}
			if (!isContributionCreated) {
				Contribution contribution = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount,
						planSelectionId, employerContribution, employeeContribution, employerPercent);
				contribution.setBenefitPlanAssociation(benefitPlan);
				if (BSSApplicationConstants.VOLUNTARY_PLAN_TYPES.contains(benefitPlan.getPlanType())) {
					contribution.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
				} else {
					contribution.setOverrideType("FLT");
				}
				contribList.add(contribution);
			}
		}
	}

	private static String getPlanLevelOverride(BenefitPlan benefitPlan) {
		String planLevelOverride = null;
		if (BSSApplicationConstants.VOLUNTARY_PLAN_TYPES.contains(benefitPlan.getPlanType())) {
			planLevelOverride = BSSApplicationConstants.PLAN_OVERRIDE_BASE;
		} else {
			planLevelOverride = BSSApplicationConstants.PLAN_OVERRIDE_FLT;
		}
		return planLevelOverride;
	}

	/**
	 * 
	 * This method is to find the minimum funding for the BenefiProgram using contributions
	 * 
	 * @param contributions
	 * @param mandatoryPlansToExclude
	 * @param company
	 * @param selectedPlanCarriers
	 * @param minFundings List of minimum fundings, may be null.
	 * @return Returns a map of plan type and minimum funding amount
	 */
	public static Map<String, BigDecimal> getMinimumFunding(List<Contribution> contributions,
			List<String> mandatoryPlansToExclude, Company company,
			Map<String, Set<Long>> selectedPlanCarriers, List<CarrierMinimumFunding> minFundings) {
		String minFundingType = RulesAndConfigsUtils.getMinFundingType(company.getRealmPlanYearId());
		if (BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE.equals(minFundingType)) {
			return getMinFundingForDefaultType(contributions, mandatoryPlansToExclude, company);
		} else if (BSSApplicationConstants.HQ_MIN_FUNDING_TYPE.equals(minFundingType)) {
			return getMinFundingForHQType(company, selectedPlanCarriers, minFundings);
		} else {
			return new HashMap<>();
		}
	}
	
	/**
	 * 
	 * This method is to find the minimum funding for the BenefiProgram using plan selection and rates
	 * 
	 * @param planSelections
	 * @param rates
	 * @param mandatoryPlansToExclude
	 * @param company
	 * @param selectedPlanCarriers
	 * @param minFundings
	 * @return Returns a map of plan type and minimum funding amount
	 */
	public static Map<String, BigDecimal> getMinimumFunding(List<PlanSelection> planSelections,
			Map<String, List<BenefitPlanRate>> rates, List<String> mandatoryPlansToExclude, Company company,
			Map<String, Set<Long>> selectedPlanCarriers, List<CarrierMinimumFunding> minFundings) {
		String minFundingType = RulesAndConfigsUtils.getMinFundingType(company.getRealmPlanYearId());
		if (BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE.equals(minFundingType)) {
			return getMinFundingForDefaultType(planSelections, rates, mandatoryPlansToExclude, company);
		} else if (BSSApplicationConstants.HQ_MIN_FUNDING_TYPE.equals(minFundingType)) {
			return getMinFundingForHQType(company, selectedPlanCarriers, minFundings);
		} else {
			return new HashMap<>();
		}
	}
	

	/**
	 * This method is to set the contributions to Minimum Funding.
	 * 
	 * @param minimumFundingMap
	 * @param contributions
	 */
	public static void updateContributionsForMinimumFunding(Map<String, BigDecimal> minimumFundingMap,
			List<Contribution> contributions, Company company, Map<String, List<String>> planRegions,
			Map<String, List<Contribution>> benefitPlanContributions, String fundingType,
			Map<String, Map<String, Object>> groupFundingDetails) {
		String minFundingType = RulesAndConfigsUtils.getMinFundingType(company.getRealmPlanYearId());
		Map<String, BigDecimal> empCvgCostByPlan = prepareMapOfEmpCvgCostByPlan(contributions);
		Map<String, BigDecimal> empCvgEmployerCostByPlan = prepareMapOfEmployerCvgCostByPlan(contributions);
		for (Contribution cb : contributions) {
			BigDecimal planCost = cb.getEmployerContribution().add(cb.getEmployeeContribution());
			logger.info("plan cost is {}", planCost);
			if (planCost.intValue() != 0) {
				if(BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE.equals(minFundingType)) {
					updateContributionsForDefaultMinFunding(minimumFundingMap, cb, planCost);
				} else if (BSSApplicationConstants.HQ_MIN_FUNDING_TYPE.equals(minFundingType)) {
					updateContributionsForHQMinFunding(minimumFundingMap, company, cb, planCost, empCvgCostByPlan);
				}
				// setting the plan level minimum funding
				if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())) {
					BigDecimal emplCvgRegionalMinFunding = calculateEmplCvgRegionalMinFunding(company, planRegions,
							benefitPlanContributions, cb);
					if (null != emplCvgRegionalMinFunding) {
						adjustMedicalPlanContributions(fundingType, groupFundingDetails, empCvgEmployerCostByPlan, cb, planCost,
								emplCvgRegionalMinFunding);
					}
				}
			}
		}
	}

	private static BigDecimal calculateEmplCvgRegionalMinFunding(Company company, Map<String, List<String>> planRegions,
			Map<String, List<Contribution>> benefitPlanContributions, Contribution cb) {
		BigDecimal emplCvgRegionalMinFunding = null;

		List<Contribution> contribList = benefitPlanContributions
				.get(cb.getBenefitPlanAssociation().getId());
		for (Contribution cb1 : contribList) {
			List<String> regions = planRegions.get(cb1.getBenefitPlanAssociation().getId());
			if (null != regions && regions.size() == 1
					&& BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(cb1.getCoverageLevel())) {
				BigDecimal planCost1 = cb1.getEmployerContribution().add(cb1.getEmployeeContribution());
				for (RegionalMinimumFunding regMinFunding : company.getRegionalMinimumFundings()) {
					if (null != regions.get(0) && regions.get(0).equals(regMinFunding.getRegion())) {
						BigDecimal minFundingPct = regMinFunding.getFundingPct();
						emplCvgRegionalMinFunding = planCost1.divide(Constants.BigDecimal_100, 10, RoundingMode.CEILING)
								.multiply(minFundingPct).setScale(2, RoundingMode.HALF_UP);
					}
				}
			}
		}
		return emplCvgRegionalMinFunding;
	}

	private static void adjustMedicalPlanContributions(String fundingType,
			Map<String, Map<String, Object>> groupFundingDetails, Map<String, BigDecimal> empCvgEmployerCostByPlan,
			Contribution cb, BigDecimal planCost, BigDecimal emplCvgRegionalMinFunding) {
		if (BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(cb.getCoverageLevel())) {
			cb.setEmployerContribution(emplCvgRegionalMinFunding);
			cb.setEmployeeContribution(planCost.subtract(emplCvgRegionalMinFunding));
			cb.setEmployerPercent(cb.getEmployerContribution()
					.divide(planCost, 10, RoundingMode.CEILING).multiply(Constants.BigDecimal_100));
		} else {
			if (BSSApplicationConstants.CFPCT.equals(fundingType)) {
				adjustContributionForCFPCTFundingType(groupFundingDetails, empCvgEmployerCostByPlan, cb, planCost,
						emplCvgRegionalMinFunding);
			} 
			if (cb.getEmployerContribution().compareTo(emplCvgRegionalMinFunding) < 0) {
				cb.setEmployerContribution(emplCvgRegionalMinFunding);
				cb.setEmployeeContribution(planCost.subtract(emplCvgRegionalMinFunding));
			}
			cb.setEmployerPercent(cb.getEmployerContribution()
					.divide(planCost, 10, RoundingMode.CEILING).multiply(Constants.BigDecimal_100));
		}
	}

	private static void adjustContributionForCFPCTFundingType(Map<String, Map<String, Object>> groupFundingDetails,
			Map<String, BigDecimal> empCvgEmployerCostByPlan, Contribution cb, BigDecimal planCost,
			BigDecimal emplCvgRegionalMinFunding) {
		BigDecimal erContributionEmployeeCvg;
		BigDecimal oldEmployeeCvgContribution = empCvgEmployerCostByPlan.get(cb.getBenefitPlanAssociation().getId());
		erContributionEmployeeCvg = emplCvgRegionalMinFunding;
		BigDecimal otheCoverageErCost = cb.getEmployerContribution()
				.subtract(oldEmployeeCvgContribution);
		if (otheCoverageErCost.compareTo(BigDecimal.ONE) < 0) {
			cb.setEmployerContribution(erContributionEmployeeCvg);
		} else {
			BigDecimal cvgLvlLimitAmount = null;
			if (null != groupFundingDetails) {
				String covgLevelId = CoverageCodesEnums.valueOfId(cb.getCoverageLevel());
				Map<String, Object> coverageLevelFunding = groupFundingDetails.get(Constants.MEDICAL_CODE);
				if (coverageLevelFunding != null && coverageLevelFunding
						.get(covgLevelId + BSSApplicationConstants.LIMIT) != null) {
					cvgLvlLimitAmount = (BigDecimal) coverageLevelFunding
							.get(covgLevelId + BSSApplicationConstants.LIMIT);
				}
			}
			BigDecimal totalContr = otheCoverageErCost.add(erContributionEmployeeCvg);
			// Apply the limit cap when the contribution after adding the regional min
			// funding is greater than limit amount
			if (cvgLvlLimitAmount != null && BigDecimal.ZERO.compareTo(cvgLvlLimitAmount) < 0
					&& totalContr.compareTo(cvgLvlLimitAmount) > 0) {
				totalContr = cvgLvlLimitAmount;
			}
			cb.setEmployerContribution(totalContr);
		}
		cb.setEmployeeContribution(planCost.subtract(cb.getEmployerContribution()));
	}

	public static Map<String, List<String>> preparePlanToRegionMap(Map<String, List<String>> selectedPlansByRegion) {
		Map<String, List<String>> planRegion = new HashMap<>();
		for (Map.Entry<String, List<String>> entry: selectedPlansByRegion.entrySet()) {
			for (String planId : entry.getValue()) {
				if (planRegion.containsKey(planId)) {
					planRegion.get(planId).add(entry.getKey());
				} else {
					List<String> regions = new ArrayList<>();
					regions.add(entry.getKey());
					planRegion.put(planId, regions);
				}
			}
		}
		return planRegion;
	}

	public static Map<String, Map<String, BenefitPlan>> getHealthPlansForAllBenefitGroups(
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap, Map<String, String> erEeMapping,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels) {
		Map<String, Map<String, BenefitPlan>> bgAllPlansMap = new HashMap<>();
		Map<String, BenefitPlan> medicalPlans = new HashMap<>();
		Map<String, BenefitPlan> dentalPlans = new HashMap<>();
		Map<String, BenefitPlan> visionPlans = new HashMap<>();
		Map<String, BenefitPlan> missingDentalPlans = new HashMap<>();
		Map<String, BenefitPlan> missingVisionPlans = new HashMap<>();

		// this is required for mapping the plan types
		Map<String, String> voluntaryGroupPlanTypeMapping = new HashMap<>();
		voluntaryGroupPlanTypeMapping.put(BSSApplicationConstants.DENTAL_PLAN_TYPE,
				BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		voluntaryGroupPlanTypeMapping.put(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE,
				BSSApplicationConstants.DENTAL_PLAN_TYPE);
		voluntaryGroupPlanTypeMapping.put(BSSApplicationConstants.VISION_PLAN_TYPE,
				BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
		voluntaryGroupPlanTypeMapping.put(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE,
				BSSApplicationConstants.VISION_PLAN_TYPE);

		for (Map.Entry<String, Map<String, Map<String, BenefitPlan>>> entry : bgsHealthPlansMap.entrySet()) {
			Map<String, Map<String, BenefitPlan>> hpPlansMap = entry.getValue();
			
			//process Medical plans
			processBenefitPlans(medicalPlans,hpPlansMap,BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			//process Dental plans
			processBenefitPlans(dentalPlans, hpPlansMap,BSSApplicationConstants.DENTAL_PLAN_TYPE);
			//process Vision plans
			processBenefitPlans(visionPlans, hpPlansMap,BSSApplicationConstants.VISION_PLAN_TYPE);
		}

		for (Map.Entry<String, BenefitPlan> entry : dentalPlans.entrySet()) {
			BenefitPlan dentalPlan = entry.getValue();
			if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(dentalPlan.getPlanType())) {
				String alternatePlan = erEeMapping.get(entry.getKey());
				if (null != alternatePlan && null == dentalPlans.get(alternatePlan)) {
					BenefitPlan bp = new BenefitPlan();
					BeanUtils.copyProperties(dentalPlan, bp);
					bp.setId(alternatePlan);
					RenewalServiceHelper.addBlankContributions(bp, mapOfCoverageLevels.get(BSSApplicationConstants.DENTAL));
					bp.setPlanType(voluntaryGroupPlanTypeMapping.get(bp.getPlanType()));
					missingDentalPlans.put(alternatePlan, bp);
				}
			}
		}
		for (Map.Entry<String, BenefitPlan> entry : visionPlans.entrySet()) {
			BenefitPlan visionPlan = entry.getValue();
			if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(visionPlan.getPlanType())) {
				String alternatePlan = erEeMapping.get(entry.getKey());
				if (null != alternatePlan && null == visionPlans.get(alternatePlan)) {
					BenefitPlan bp = new BenefitPlan();
					BeanUtils.copyProperties(visionPlan, bp);
					bp.setId(alternatePlan);
					RenewalServiceHelper.addBlankContributions(bp, mapOfCoverageLevels.get(BSSApplicationConstants.VISION));
					bp.setPlanType(voluntaryGroupPlanTypeMapping.get(bp.getPlanType()));
					missingVisionPlans.put(alternatePlan, bp);
				}
			}
		}

		bgAllPlansMap.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, medicalPlans);
		dentalPlans.putAll(missingDentalPlans);
		bgAllPlansMap.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, dentalPlans);
		visionPlans.putAll(missingVisionPlans);
		bgAllPlansMap.put(BSSApplicationConstants.VISION_PLAN_TYPE, visionPlans);
		return bgAllPlansMap;
	}
	
	private static void processBenefitPlans(Map<String, BenefitPlan> benefitPlans,
			Map<String, Map<String, BenefitPlan>> hpPlansMap, String planType) {
		Map<String, BenefitPlan> benefitPlanMap = hpPlansMap.get(planType);
		if (null != benefitPlanMap && !benefitPlanMap.isEmpty()) {
			for (BenefitPlan bp : benefitPlanMap.values()) {
				if (null == benefitPlans.get(bp.getId())) {
					benefitPlans.put(bp.getId(), bp);
				}
			}
		}
	}

	/**
	 * 
	 * @param hBp
	 * @param coverageCodes
	 */
	public static void addBlankContributions(BenefitPlan hBp, List<CoverageLevel> coverageCodes) {
		logger.debug("Entering method : addBlankContributions");
		hBp.setContributions(new ArrayList<>());
		for (CoverageLevel coverageLevel : coverageCodes) {
			if (!"all".equalsIgnoreCase(coverageLevel.getId())) {
				PlanContribution pc = new PlanContribution();
				pc.setBenefitPlanId(hBp.getId());
				pc.setType(coverageLevel.getId());
				pc.setHeadcount(0);
				hBp.getContributions().add(pc);
			}
		}
		logger.debug("Exiting method : addBlankContributions");
	}

	/**
	 * This method is for adding the auto selected plans to the plan selection
	 * List.
	 * 
	 * @param planSelections
	 * @param autoSelectPlans
	 * @param dentalCoverageCodes
	 * @param benefitPlanMap
	 * @param strategyId
	 * @param benefitGroupId
	 */
	public static void addPlanSelectionsForAutoSelectPlans(List<PlanSelection> planSelections,
			Map<String, Map<String, Set<String>>> autoSelectPlans, Map<String, List<CoverageLevel>> mapOfCoverageLevels,
			Map<String, BenefitPlan> benefitPlanMap, Long strategyId, Long benefitGroupId) {
		Set<BenefitPlan> crossBenefitPlans = new java.util.HashSet<>();
		for (Map.Entry<String, BenefitPlan> entry : benefitPlanMap.entrySet()) {
		    BenefitPlan bp = entry.getValue();
			Map<String, Set<String>> autoSelectPlansByType = null;
			List<CoverageLevel> coverageCodes = null;
			if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(bp.getPlanType())) {
				autoSelectPlansByType = autoSelectPlans.get(BSSApplicationConstants.DENTAL);
				coverageCodes = mapOfCoverageLevels.get(BSSApplicationConstants.DENTAL);
			} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(bp.getPlanType())) {
				autoSelectPlansByType = autoSelectPlans.get(BSSApplicationConstants.VISION);
				coverageCodes = mapOfCoverageLevels.get(BSSApplicationConstants.VISION);
			} else if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(bp.getPlanType())) {
				autoSelectPlansByType = autoSelectPlans.get(BSSApplicationConstants.MEDICAL);
				coverageCodes = mapOfCoverageLevels.get(BSSApplicationConstants.MEDICAL);
			}
			if (null != autoSelectPlansByType) {
				Set<String> crossRefPlans = autoSelectPlansByType.get(bp.getId());
				if (crossRefPlans != null) {
					addCrossBenefitPlans(benefitPlanMap, crossBenefitPlans, bp, coverageCodes, crossRefPlans);
				}
			}
		}
		if (null != crossBenefitPlans && !crossBenefitPlans.isEmpty()) {
			addPlanSelectionsWithCrossBenefitPlans(planSelections, benefitPlanMap, strategyId, benefitGroupId,
					crossBenefitPlans);
		}
	}

	private static void addPlanSelectionsWithCrossBenefitPlans(List<PlanSelection> planSelections,
			Map<String, BenefitPlan> benefitPlanMap, Long strategyId, Long benefitGroupId,
			Set<BenefitPlan> crossBenefitPlans) {
		for (BenefitPlan cBp : crossBenefitPlans) {
			logger.debug("Cross Benefit Plan added: {}", cBp.getId());
			logger.debug("Cross Plan Type : {}", cBp.getPlanType());
			if (null == benefitPlanMap.get(cBp.getId())) {
				benefitPlanMap.put(cBp.getId(), cBp);
				planSelections
						.add(StrategyServiceHelper.constructPlanSelection(strategyId, benefitGroupId, cBp, 0));
			}
		}
	}

	private static void addCrossBenefitPlans(Map<String, BenefitPlan> benefitPlanMap,
			Set<BenefitPlan> crossBenefitPlans, BenefitPlan bp, List<CoverageLevel> coverageCodes,
			Set<String> crossRefPlans) {
		for (String crossRefPlan : crossRefPlans) {
			if (null == benefitPlanMap.get(crossRefPlan)) {
				BenefitPlan crossBenefitPlan = StrategyServiceHelper.constructBenefitPlan(crossRefPlan,
						bp.getPlanType(), bp.getVendorId());
				RenewalServiceHelper.addBlankContributions(crossBenefitPlan, coverageCodes);
				crossBenefitPlans.add(crossBenefitPlan);
			}
		}
	}

	/**
	 * This method is for construct Renewal Strategy FundingDetails
	 * 
	 * @param strategyId
	 * @param benefitGroup
	 * @param strategyFunding
	 * @param psFundingDetails
	 * @param realmPlanMapping
	 * @param isHistory
	 * @param bsuppPlanTypes
	 * @param medPlans
	 * @param planTypeExceptionMap
	 * @param mapOfCoverageLevels
	 * @param benOfferExceptions
	 */
	public static void constructRenewalStrategyFundingDetails(long strategyId, BenefitGroup benefitGroup,
			List<StrategyFundingModel> strategyFunding, Map<String, Map<String, Object>> psFundingDetails,
			Map<String, PlanMapping> realmPlanMapping, boolean isHistory,
			List<String> bsuppPlanTypes, List<String> medPlans, Map<String, PlanTypeRule> planTypeExceptionMap,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels, Map<String, Boolean> benOfferExceptions) {
		for (String planType : psFundingDetails.keySet()) {
			if (!isBenOfferExceptionAvailable(planType, benOfferExceptions)) {
				if (!isHistory && BSSApplicationConstants.K1_GROUP_TYPE.equals(benefitGroup.getType())
						&& !planTypeExceptionMap.containsKey(planType)) {
					List<CoverageLevel> coverageLevelList = mapOfCoverageLevels.get(PlanTypesEnum.getName(planType));
					psFundingDetails.putAll(createK1FundingForPlanType(strategyFunding, strategyId,
							benefitGroup.getId(), planType, coverageLevelList, psFundingDetails));
				} else {
					constructRenewalStrategyFundingDetailsForPlanType(strategyId, benefitGroup, planType,
							strategyFunding, psFundingDetails, realmPlanMapping, isHistory, bsuppPlanTypes,
							medPlans);
				}
			}
		}
	}
	
	public static void constructRenewalStrategyFundingDetailsForPlanType(long strategyId, BenefitGroup benefitGroup,
			String planType, List<StrategyFundingModel> strategyFunding,
			Map<String, Map<String, Object>> psFundingDetails, Map<String, PlanMapping> realmPlanMapping,
			boolean isHistory, List<String> bsuppPlanTypes, List<String> medPlans) {

		StrategyFundingModel strategyFundingModel = new StrategyFundingModel();
		Map<String, Object> coverageLevelFunding = psFundingDetails.get(planType);
		strategyFundingModel.setFundingType((String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE));

		if (!isHistory && BSSApplicationConstants.EEC.equals(strategyFundingModel.getFundingType())) {
			return;
		}
		String fundingBasePlan = getAndUpdateFundingBasePlan(planType, realmPlanMapping, medPlans, strategyFundingModel,
				coverageLevelFunding);
		strategyFundingModel
				.setWaiverAllowance((BigDecimal) coverageLevelFunding.get(BSSApplicationConstants.WAIVER_ALLOWANCE));

		updateBsuppExcessOption(bsuppPlanTypes, strategyFundingModel, coverageLevelFunding);
		strategyFundingModel.setStrategyId(strategyId);
		strategyFundingModel.setGroupId(benefitGroup.getId());
		strategyFundingModel.setName("renewal");
		strategyFundingModel.setCustomized(true);
		strategyFundingModel.setPlanType(planType);
		
		Set<StrategyFundingDetail> fundingDetails = new java.util.HashSet<>();
		Set<StrategyFundingFlatMax> fundingFlatMaxDetails = new java.util.HashSet<>();
		Set<StrategyFundingBasePlanLimits> fundingBasePlanLimits = new java.util.HashSet<>();

		// BSUPP funding data
		updateFundingBsuppPlanTypes(bsuppPlanTypes, strategyFundingModel);
		
		boolean isValidFunding = validateAndUpdateFundingDetails(strategyFundingModel, coverageLevelFunding, fundingDetails);

		boolean isValidFundingFlatMax = false;
		if (null != fundingBasePlan) {
			if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
				isValidFundingFlatMax = validateAndAddFundingFlatMaxDetails(strategyFundingModel, coverageLevelFunding,
						fundingFlatMaxDetails, isValidFundingFlatMax);
			} else {
				validateAndAddFundingBasePlanLimits(strategyFundingModel, coverageLevelFunding, fundingBasePlanLimits);
			}
		} 


		if (isValidFunding || isValidFundingFlatMax) {
			strategyFunding.add(strategyFundingModel);
		}
	}

	private static void updateFundingBsuppPlanTypes(List<String> bsuppPlanTypes,
			StrategyFundingModel strategyFundingModel) {
		if (BSSApplicationConstants.BSUPP.equals(strategyFundingModel.getFundingType())
				&& CollectionUtils.isNotEmpty(bsuppPlanTypes) && strategyFundingModel.getBsuppExcessOption() != null
				&& ExcessOptionEnum.CASH.getType() != strategyFundingModel.getBsuppExcessOption().intValue()) {
			Set<StrategyFundBsuppPlanTypes> fundingBsuppPlanTypes = new java.util.HashSet<>();
			for (String pTypes : bsuppPlanTypes) {
				StrategyFundBsuppPlanTypes sfbpt = new StrategyFundBsuppPlanTypes();
				StrategyFundBsuppPlanTypeId strategyFundBsuppPlanTypeId = new StrategyFundBsuppPlanTypeId();
				strategyFundBsuppPlanTypeId.setPlanType(pTypes);
				sfbpt.setStrategyFundBsuppPlanTypeId(strategyFundBsuppPlanTypeId);
				sfbpt.setStrategyFundingModel(strategyFundingModel);
				fundingBsuppPlanTypes.add(sfbpt);
			}
			strategyFundingModel.getFundingBsuppPlanTypes().addAll(fundingBsuppPlanTypes);
		}
	}

	private static boolean validateAndUpdateFundingDetails(StrategyFundingModel strategyFundingModel,
			Map<String, Object> coverageLevelFunding, Set<StrategyFundingDetail> fundingDetails) {
		boolean isValidFunding = false;
		
		getFundingDetails(strategyFundingModel, coverageLevelFunding, fundingDetails);
		
		if (!fundingDetails.isEmpty()) {
			for (StrategyFundingDetail sfd : fundingDetails) {
				if (null != sfd.getContribution() && sfd.getContribution().compareTo(BigDecimal.ZERO) > 0
						|| (BSSApplicationConstants.EEC.equals(sfd.getStrategyFundingModel().getFundingType()))) {
					isValidFunding = true;
					break;
				}
			}
			if (isValidFunding) {
				strategyFundingModel.setFundingDetails(fundingDetails);
			}
		}
		return isValidFunding;
	}

	private static void getFundingDetails(StrategyFundingModel strategyFundingModel,
			Map<String, Object> coverageLevelFunding, Set<StrategyFundingDetail> fundingDetails) {
		if (BSSApplicationConstants.BFPCT.equals(strategyFundingModel.getFundingType())) {
			StrategyFundingDetail esfd = new StrategyFundingDetail();
			StrategyFundingDetailId esfdId = new StrategyFundingDetailId();
			if (null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG)) {
				esfdId.setCoverageId((String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG));
				esfd.setSfDetailId(esfdId);
				esfd.setContribution((BigDecimal) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_PCT));
			} else {
				esfdId.setCoverageId(BSSApplicationConstants.CVG_CODE_ALL);
				esfd.setSfDetailId(esfdId);
				esfd.setContribution((BigDecimal) coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			}
			esfd.setStrategyFundingModel(strategyFundingModel);
			fundingDetails.add(esfd);
		} else {
			for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
				StrategyFundingDetail esfd = new StrategyFundingDetail();
				StrategyFundingDetailId esfdId = new StrategyFundingDetailId();
				esfdId.setCoverageId(coverageLevel);
				esfd.setSfDetailId(esfdId);
				esfd.setContribution((BigDecimal) coverageLevelFunding.get(coverageLevel));
				esfd.setStrategyFundingModel(strategyFundingModel);
				fundingDetails.add(esfd);
			}
		}
	}

	private static boolean validateAndAddFundingFlatMaxDetails(StrategyFundingModel strategyFundingModel,
			Map<String, Object> coverageLevelFunding, Set<StrategyFundingFlatMax> fundingFlatMaxDetails,
			boolean isValidFundingFlatMax) {
		for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
			StrategyFundingFlatMax esfdMax = new StrategyFundingFlatMax();
			StrategyFundingDetailId esfdIdMax = new StrategyFundingDetailId();
			esfdIdMax.setCoverageId(coverageLevel);
			esfdMax.setSfDetailId(esfdIdMax);
			esfdMax.setContribution(
					(BigDecimal) coverageLevelFunding.get(coverageLevel + BSSApplicationConstants.LIMIT));
			esfdMax.setStrategyFundingModel(strategyFundingModel);
			fundingFlatMaxDetails.add(esfdMax);
		}
		
		// getting flat max details.
		if (!fundingFlatMaxDetails.isEmpty()) {
			for (StrategyFundingFlatMax sfd : fundingFlatMaxDetails) {
				if (sfd.getContribution().compareTo(BigDecimal.ZERO) > 0) {
					isValidFundingFlatMax = true;
					break;
				}
			}
			if (isValidFundingFlatMax) {
				strategyFundingModel.setBaseBenefitPlan(BSSApplicationConstants.FLAT_MAX);
				strategyFundingModel.setFundingFlatMax(fundingFlatMaxDetails);
			}
		}
		return isValidFundingFlatMax;
	}

	private static void validateAndAddFundingBasePlanLimits(StrategyFundingModel strategyFundingModel,
			Map<String, Object> coverageLevelFunding, Set<StrategyFundingBasePlanLimits> fundingBasePlanLimits) {
		if (BSSApplicationConstants.BFPCT.equals(strategyFundingModel.getFundingType())) {
			for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
				StrategyFundingBasePlanLimits esfbpLimits = new StrategyFundingBasePlanLimits();
				StrategyFundingDetailId esfdIdLimit = new StrategyFundingDetailId();
				esfdIdLimit.setCoverageId(coverageLevel);
				esfbpLimits.setSfDetailId(esfdIdLimit);
				esfbpLimits.setContribution((BigDecimal) coverageLevelFunding.get(coverageLevel));
				esfbpLimits.setStrategyFundingModel(strategyFundingModel);
				fundingBasePlanLimits.add(esfbpLimits);
			}
		} else {
			for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
				StrategyFundingBasePlanLimits esfbpLimits = new StrategyFundingBasePlanLimits();
				StrategyFundingDetailId esfdIdLimit = new StrategyFundingDetailId();
				esfdIdLimit.setCoverageId(coverageLevel);
				esfbpLimits.setSfDetailId(esfdIdLimit);
				esfbpLimits.setContribution(
						(BigDecimal) coverageLevelFunding.get(coverageLevel + BSSApplicationConstants.LIMIT));
				esfbpLimits.setStrategyFundingModel(strategyFundingModel);
				fundingBasePlanLimits.add(esfbpLimits);
			}
		}
		
		boolean isValidBasePlanLimits = false;
		// getting flat max details.
		if (!fundingBasePlanLimits.isEmpty()) {
			for (StrategyFundingBasePlanLimits sfd : fundingBasePlanLimits) {
				if (sfd.getContribution() != null && sfd.getContribution().compareTo(BigDecimal.ZERO) > 0) {
					isValidBasePlanLimits = true;
					break;
				}
			}
			if (isValidBasePlanLimits) {
				strategyFundingModel.setFundingBasePlanLimits(fundingBasePlanLimits);
			}
		}
	}

	private static void updateBsuppExcessOption(List<String> bsuppPlanTypes, StrategyFundingModel strategyFundingModel,
			Map<String, Object> coverageLevelFunding) {
		if (BSSApplicationConstants.BSUPP.equals(strategyFundingModel.getFundingType())) {
			BigDecimal bSuppExcessOption = (BigDecimal) coverageLevelFunding.get(BSSApplicationConstants.BSUPP_EXCESS_OPTION);
			if (!bSuppExcessOption.equals(BigDecimal.valueOf(ExcessOptionEnum.CASH.getType()))) {
				bSuppExcessOption = bsuppPlanTypes.isEmpty() ? BigDecimal.valueOf(ExcessOptionEnum.OTHER.getType())
						: BigDecimal.valueOf(ExcessOptionEnum.FORFEIT.getType());
			}
			strategyFundingModel.setBsuppExcessOption(bSuppExcessOption);
		}
	}

	private static String getAndUpdateFundingBasePlan(String planType, Map<String, PlanMapping> realmPlanMapping,
			List<String> medPlans, StrategyFundingModel strategyFundingModel,
			Map<String, Object> coverageLevelFunding) {
		String fundingBasePlan = null;
		if(null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)) {
			fundingBasePlan = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN);
		}
		if (!BSSApplicationConstants.FLAT.equals(strategyFundingModel.getFundingType())) {
			if (null != realmPlanMapping && null != realmPlanMapping.get(fundingBasePlan)) {
				fundingBasePlan = realmPlanMapping.get(fundingBasePlan).getNewBenefitPlan();
			}
			if (!BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)) {
				strategyFundingModel.setBaseBenefitPlan(fundingBasePlan);
			} else {
				if (CollectionUtils.isNotEmpty(medPlans) && medPlans.contains(fundingBasePlan)) {
					strategyFundingModel.setBaseBenefitPlan(fundingBasePlan);
				}
			}
		}
		return fundingBasePlan;
	}

	/**
	 * This method is for getting all the medical benefit plans from the plan
	 * selection.
	 * 
	 * @param planSelections
	 * @return
	 */
	public static Set<String> getPlanSeclectionMedicalPlans(Set<PlanSelection> planSelections) {
		Set<String> planSelectionPlans = new HashSet<>();
		for (PlanSelection ps : planSelections) {
			if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(ps.getPlanType())) {
				planSelectionPlans.add(ps.getBenefitPlan());
			}
		}
		return planSelectionPlans;
	}

	/**
	 * This method is for updating the PPO flag in all PS objects.
	 * 
	 * @param planSelections
	 * @param ppoPlanMap
	 * @param benefitPlanMap
	 */
	public static void updatePPOForPlansSelections(Set<PlanSelection> planSelections, Set<String> widelyAvailablePlanSet,
			Map<String, BenefitPlan> benefitPlanMap) {
		for (PlanSelection ps : planSelections) {
			if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(ps.getPlanType()) && widelyAvailablePlanSet.contains(ps.getBenefitPlan())) {
				if (null != benefitPlanMap.get(ps.getBenefitPlan())) {
					benefitPlanMap.get(ps.getBenefitPlan()).setPpoPlan(true);
					benefitPlanMap.get(ps.getBenefitPlan()).setWidelyAvailablePlan(true);
				}
				ps.setPpoPlan(true);
			} else {
				ps.setPpoPlan(false);
			}
		}
	}

	/**
	 * This method is for checking if the company has opted out FPL.
	 * 
	 * @param company
	 * @return
	 */
	public static boolean isFplApplicable(Company company, int acaFPLOpted) {
		boolean isFplApplicable = false;
		if (company.isEligAle() && BSSApplicationConstants.ACA_FPL_OPTED_IN == acaFPLOpted) {
			isFplApplicable = true;
		}
		return isFplApplicable;
	}

	/**
	 * This method is for updating the contributions of other coverage levels
	 * based on employee coverage.
	 * 
	 * @param contributions
	 * @param employeeCvgPlanCost
	 * @param employeeCvgContribution
	 */
	public static void updateContributionByEmployeeContributions(List<Contribution> contributions,
			BigDecimal employeeCvgPlanCost, BigDecimal employeeCvgContribution,
			Map<String, Object> coverageLevelFunding) {
		String fundingType = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
		if (BSSApplicationConstants.CFPCT.equals(fundingType)) {
			updateContributionForCFPCT(contributions, employeeCvgPlanCost, employeeCvgContribution,
					coverageLevelFunding);
		} else {
			updateContributionForOtherTypes(contributions, employeeCvgContribution);
		}
	}
	
	private static void updateContributionForCFPCT(List<Contribution> contributions, BigDecimal employeeCvgPlanCost,
			BigDecimal employeeCvgContribution, Map<String, Object> coverageLevelFunding) {
		for (Contribution contribution : contributions) {
			if (!CoverageCodesEnums.COV_EMPLOYEE.getCode().equals(contribution.getCoverageLevel())
					&& BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(contribution.getOverrideType())) {
				BigDecimal planCost = contribution.getEmployerContribution()
						.add(contribution.getEmployeeContribution());
				BigDecimal cvgPlanCost = planCost.subtract(employeeCvgPlanCost);
				BigDecimal cvgEmployeerCost = null;
				BigDecimal cvgEmployeerPct = (BigDecimal) coverageLevelFunding
						.get(CoverageCodesEnums.valueOfId(contribution.getCoverageLevel()));
				if (BigDecimal.ZERO.compareTo(cvgEmployeerPct) < 0) {
					cvgEmployeerCost = cvgPlanCost.multiply(cvgEmployeerPct).divide(Constants.BigDecimal_100)
							.setScale(2, RoundingMode.HALF_UP);
				} else {
					cvgEmployeerCost = BigDecimal.ZERO;
				}
				contribution.setEmployerContribution(employeeCvgContribution.add(cvgEmployeerCost));
				String coverageLevel = CoverageCodesEnums.valueOfId(contribution.getCoverageLevel());
				BigDecimal employeeCvgLimit = (BigDecimal) coverageLevelFunding.get(coverageLevel + BSSApplicationConstants.LIMIT);
				if (null != employeeCvgLimit && employeeCvgLimit.compareTo(contribution.getEmployerContribution()) < 0) {
					contribution.setEmployerContribution(employeeCvgLimit);
				}
				contribution.setEmployeeContribution(planCost.subtract(contribution.getEmployerContribution()));
				BigDecimal employerPercent = contribution.getEmployerContribution()
						.divide(planCost, 10, RoundingMode.CEILING).multiply(Constants.BigDecimal_100);
				contribution.setEmployerPercent(employerPercent);
			}
		}
	}

	private static void updateContributionForOtherTypes(List<Contribution> contributions,
			BigDecimal employeeCvgContribution) {
		for (Contribution contribution : contributions) {
			if (!CoverageCodesEnums.COV_EMPLOYEE.getCode().equals(contribution.getCoverageLevel())
					&& BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(contribution.getOverrideType())) {
				BigDecimal planCost = contribution.getEmployerContribution()
						.add(contribution.getEmployeeContribution());
				if (contribution.getEmployerContribution().compareTo(employeeCvgContribution) < 0) {
					contribution.setEmployerContribution(employeeCvgContribution);
					contribution.setEmployeeContribution(planCost.subtract(contribution.getEmployerContribution()));
					BigDecimal employerPercent = contribution.getEmployerContribution()
							.divide(planCost, 10, RoundingMode.CEILING).multiply(Constants.BigDecimal_100);
					contribution.setEmployerPercent(employerPercent);
				}
			}
		}
	}

	/**
	 * This method is for updating the contribution based on a limit coverage
	 * level.
	 * 
	 * @param contributions
	 * @param limitCvgPlanCost
	 */
	public static void updateContributionByLimitContributions(List<Contribution> contributions,
			BigDecimal limitCvgPlanCost) {
		for (Contribution contribution : contributions) {
			if (BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(contribution.getOverrideType())) {
				BigDecimal planCost = contribution.getEmployerContribution()
						.add(contribution.getEmployeeContribution());
				if (planCost.compareTo(limitCvgPlanCost) < 0) {
					contribution.setEmployerContribution(planCost);
				} else {
					contribution.setEmployerContribution(limitCvgPlanCost);
				}
				contribution.setEmployeeContribution(planCost.subtract(contribution.getEmployerContribution()));
				BigDecimal employerPercent = contribution.getEmployerContribution()
						.divide(planCost, 10, RoundingMode.CEILING).multiply(Constants.BigDecimal_100);
				contribution.setEmployerPercent(employerPercent);
			}
		}
	}

	/**
	 * This method is for setting funding for K1 Benefit group.
	 * 
	 * @param strategyFunding
	 * @param strategyId
	 * @param benefitGroupId
	 * @param mapOfCoverageLevels
	 */
	public static Map<String, Map<String, Object>> createK1Funding(List<StrategyFundingModel> strategyFunding,
			long strategyId, long benefitGroupId, Map<String, List<CoverageLevel>> mapOfCoverageLevels,
			Map<String, Boolean> benOfferExceptions) {

		Map<String, Map<String, Object>> k1FundingDetailsMap = new HashMap<>();
		for (Map.Entry<String, List<CoverageLevel>> entry : mapOfCoverageLevels.entrySet()) {
			if (!isBenOfferExceptionAvailable(entry.getKey(), benOfferExceptions)) {
				List<CoverageLevel> coverageLevelList = entry.getValue();
				createK1FundingForPlanType(strategyFunding, strategyId, benefitGroupId, PlanTypesEnum.getCode(entry.getKey()),
						coverageLevelList, k1FundingDetailsMap);
			}
		}
		return k1FundingDetailsMap;

	}
	/**
	 * This method is for setting funding for K1 for a plan type.
	 * 
	 * @param strategyFunding
	 * @param strategyId
	 * @param benefitGroupId
	 * @param mapOfCoverageLevels
	 */
	public static Map<String, Map<String, Object>> createK1FundingForPlanType(
			List<StrategyFundingModel> strategyFunding, long strategyId, long benefitGroupId, String planType,
			List<CoverageLevel> coverageLevelList, Map<String, Map<String, Object>> fundingDetailsMap) {

		StrategyFundingModel strategyFundingModel = new StrategyFundingModel();
		strategyFundingModel.setFundingType(BSSApplicationConstants.CFPCT);
		strategyFundingModel.setStrategyId(strategyId);
		strategyFundingModel.setGroupId(benefitGroupId);
		strategyFundingModel.setName("renewal");
		strategyFundingModel.setCustomized(true);
		strategyFundingModel.setPlanType(planType);
		Map<String, Object> coveragelevelfunding = new HashMap<>();
		coveragelevelfunding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.CFPCT);
		coveragelevelfunding.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, strategyFundingModel.getPlanType());
		Set<StrategyFundingDetail> fundingDetails = new java.util.HashSet<>();
		for (CoverageLevel cvl : coverageLevelList) {
			if (!BSSApplicationConstants.CVG_CODE_ALL.equals(cvl.getId())) {
				StrategyFundingDetail esfd = new StrategyFundingDetail();
				StrategyFundingDetailId esfdId = new StrategyFundingDetailId();
				esfdId.setCoverageId(cvl.getId());
				esfd.setSfDetailId(esfdId);
				esfd.setContribution(Constants.BigDecimal_100);
				coveragelevelfunding.put(cvl.getId(), Constants.BigDecimal_100);
				esfd.setStrategyFundingModel(strategyFundingModel);
				fundingDetails.add(esfd);
			}
		}
		fundingDetailsMap.put(strategyFundingModel.getPlanType(), coveragelevelfunding);
		if (!fundingDetails.isEmpty()) {
			strategyFundingModel.setFundingDetails(fundingDetails);
			strategyFunding.add(strategyFundingModel);
		}
		return fundingDetailsMap;
	}

	/**
	 * 
	 * @param benefitGroups
	 * @param k1RateTableID
	 * @return
	 */
	public static boolean validateK1RateTableId(List<BenefitGroup> benefitGroups, String k1RateTableId) {
		boolean returnValue = false;
		for (BenefitGroup bg : benefitGroups) {
			if (!"K1".equals(bg.getType())) {
				Set<GroupRate> groupRate = bg.getGroupRate();
				for (GroupRate gr : groupRate) {
					if (gr.getRateIdType().equals(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
							&& gr.getId().getRateTblId().equals(k1RateTableId)) {
						returnValue = true;
						break;
					}
				}
			}
		}
		return returnValue;
	}

	/**
	 * 
	 * @param benefitGroups
	 * @param k1RateTableId
	 * @return
	 */
	public static boolean updateK1RateTableId(List<BenefitGroup> benefitGroups,
			Map<String, String> updatedRateTableIds) {
		boolean returnValue = false;
		for (BenefitGroup bg : benefitGroups) {
			if ("K1".equals(bg.getType())) {
				if (null != updatedRateTableIds) {
					bg.setGroupRate(new HashSet<>());
					bg.getGroupRate().addAll(BenefitGroupRateMapper.convertMapToGroupRate(updatedRateTableIds, bg));
				}
				returnValue = true;
			}
		}
		return returnValue;
	}

	/**
	 * This method is for calculating contributions for funding type BFPCT.
	 * 
	 * @param groupFundingDetails
	 * @param rates
	 * @param company
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public static void updateFundingDetailsForBasePlan(Map<String, Map<String, Object>> groupFundingDetails,
			Map<String, List<BenefitPlanRate>> rates, Company company, Map<String, PlanMapping> realmPlanMapping,
			RealmDataDao realmDataDao, List<String> medPlans,
			Map<String, Boolean> benOfferExceptions) {
		Map<String, String> limitPlanTypeMap = new HashMap<>();
		Set<String> limitPlans = new HashSet<>();
		boolean isMedicalExceptionAvailable = isBenOfferExceptionAvailable(PlanTypesEnum.MEDICAL.getCode(),
				benOfferExceptions);
		if (!isMedicalExceptionAvailable) {
			String medicalPlan = null;
			Map<String, Object> medicalFunding = groupFundingDetails.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			if (null != medicalFunding && null != medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)
					&& !BSSApplicationConstants.FLAT_MAX
							.equals(medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN))) {
				medicalPlan = (String) medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN);
				if (null != medicalPlan) {
					if (null != realmPlanMapping && null != realmPlanMapping.get(medicalPlan)) {
						String mappedPlan = realmPlanMapping.get(medicalPlan).getNewBenefitPlan();
						limitPlans.add(mappedPlan);
						medicalPlan = mappedPlan;
					}
					if (null != medPlans) {
						if (medPlans.contains(medicalPlan)) {
							limitPlans.add(medicalPlan);
							limitPlanTypeMap.put(medicalPlan, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
						}
					} else {
						limitPlans.add(medicalPlan);
						limitPlanTypeMap.put(medicalPlan, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
					}
				}
			} else if (null != medicalFunding
					&& (null == medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)
							|| BSSApplicationConstants.FLAT_MAX
									.equals(medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)))
					&& null != medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG)) {
				String limitCvgLevel = (String) medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG);
				BigDecimal basePct = (BigDecimal) medicalFunding.get(BSSApplicationConstants.FUNDING_BASE_PCT);
				if (null != limitCvgLevel && "all".equals(limitCvgLevel)) {
					medicalFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), basePct);
					medicalFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), basePct);
					medicalFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), basePct);
					medicalFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), basePct);
					medicalFunding.remove(BSSApplicationConstants.FUNDING_BASE_CVG);
					medicalFunding.remove(BSSApplicationConstants.FUNDING_BASE_PCT);
				}
			}
		}

		boolean isDentalExceptionAvailable = isBenOfferExceptionAvailable(PlanTypesEnum.DENTAL.getCode(),
				benOfferExceptions);
		if (!isDentalExceptionAvailable) {
			String dentalPlan = null;
			Map<String, Object> dentalFunding = groupFundingDetails.get(BSSApplicationConstants.DENTAL_PLAN_TYPE);
			if (null != dentalFunding && null != dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)
					&& !BSSApplicationConstants.FLAT_MAX
							.equals(dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN))) {
				dentalPlan = (String) dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN);
				if (null != dentalPlan) {
					if (null != realmPlanMapping && null != realmPlanMapping.get(dentalPlan)) {
						String dMappedPlan = realmPlanMapping.get(dentalPlan).getNewBenefitPlan();
						limitPlans.add(dMappedPlan);
						dentalPlan = dMappedPlan;
					} else {
						limitPlans.add(dentalPlan);
					}
					limitPlanTypeMap.put(dentalPlan, BSSApplicationConstants.DENTAL_PLAN_TYPE);
				}
			} else if (null != dentalFunding
					&& (null == dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)
							|| BSSApplicationConstants.FLAT_MAX
									.equals(dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)))
					&& null != dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG)) {
				String limitCvgLevel = (String) dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG);
				BigDecimal basePct = (BigDecimal) dentalFunding.get(BSSApplicationConstants.FUNDING_BASE_PCT);
				if (null != limitCvgLevel && "all".equals(limitCvgLevel)) {
					dentalFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), basePct);
					dentalFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), basePct);
					dentalFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), basePct);
					dentalFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), basePct);
					dentalFunding.remove(BSSApplicationConstants.FUNDING_BASE_CVG);
					dentalFunding.remove(BSSApplicationConstants.FUNDING_BASE_PCT);
				}
			}
		}

		boolean isVisionExceptionAvailable = isBenOfferExceptionAvailable(PlanTypesEnum.VISION.getCode(),
				benOfferExceptions);
		if (!isVisionExceptionAvailable) {
			String visionPlan = null;
			Map<String, Object> visionFunding = groupFundingDetails.get(BSSApplicationConstants.VISION_PLAN_TYPE);
			if (null != visionFunding && null != visionFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)
					&& !BSSApplicationConstants.FLAT_MAX
							.equals(visionFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN))) {
				visionPlan = (String) groupFundingDetails.get(BSSApplicationConstants.VISION_PLAN_TYPE)
						.get(BSSApplicationConstants.FUNDING_BASE_PLAN);
				if (null != visionPlan) {
					if (null != realmPlanMapping && null != realmPlanMapping.get(visionPlan)) {
						String vMappedPlan = realmPlanMapping.get(visionPlan).getNewBenefitPlan();
						limitPlans.add(vMappedPlan);
						visionPlan = vMappedPlan;
					} else {
						limitPlans.add(visionPlan);
					}
					limitPlanTypeMap.put(visionPlan, BSSApplicationConstants.VISION_PLAN_TYPE);
				}
			} else if (null != visionFunding
					&& (null == visionFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)
							|| BSSApplicationConstants.FLAT_MAX
									.equals(visionFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN)))
					&& null != visionFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG)) {
				String limitCvgLevel = (String) visionFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG);
				BigDecimal basePct = (BigDecimal) visionFunding.get(BSSApplicationConstants.FUNDING_BASE_PCT);
				if (null != limitCvgLevel && "all".equals(limitCvgLevel)) {
					visionFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), basePct);
					visionFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), basePct);
					visionFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), basePct);
					visionFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), basePct);
					visionFunding.remove(BSSApplicationConstants.FUNDING_BASE_CVG);
					visionFunding.remove(BSSApplicationConstants.FUNDING_BASE_PCT);
				}
			}
		}
		if (!limitPlans.isEmpty()) {
			Map<String, String> limitPlanVendorIdMap = realmDataDao.getPlanVendors(limitPlans,
					company.getRealmPlanYearId());
			for (String limitPlan : limitPlanVendorIdMap.keySet()) {
				List<BenefitPlanRate> planRates = rates.get(limitPlan);
				if (CollectionUtils.isEmpty(planRates)) {
					logger.info("No Plan rate found for the Benefit Plan : {}", limitPlan);
					continue;
				}
				String limitCoverageLevel = null;
				BigDecimal limitPercentage = null;
				BigDecimal limitBaseFunding = null;
				String limitCoverageLevelCFPCT = null;
				BigDecimal employeeCoverageCost = new BigDecimal(0);
				BigDecimal employeeCoverageErCost = new BigDecimal(0);
				for (String planType : groupFundingDetails.keySet()) {
					if (planType.equals(limitPlanTypeMap.get(limitPlan))) {
						Map<String, Object> coverageLevelFunding = groupFundingDetails.get(planType);
						if (BSSApplicationConstants.BFPCT
								.equals(coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE))) {
							limitCoverageLevel = (String) groupFundingDetails.get(planType)
									.get(BSSApplicationConstants.FUNDING_BASE_CVG);
							limitPercentage = (BigDecimal) groupFundingDetails.get(planType)
									.get(BSSApplicationConstants.FUNDING_BASE_PCT);
						}
						Map<String, BigDecimal> otherCoverageLevel = new HashMap<>();
						for (BenefitPlanRate planRate : planRates) {
							String coverageId = CoverageCodesEnums.valueOfId(planRate.getCoverageCode());

							if (StringUtils.isNotBlank(limitCoverageLevel)) {
								if ("all".equals(limitCoverageLevel)) {
									if (CoverageCodesEnums.COV_EMPLOYEE.getId().equals(coverageId)) {
										BigDecimal newPlanCost = planRate.getEmployerCost();
										BigDecimal employerContribution = newPlanCost.multiply(limitPercentage)
												.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
										coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(),
												employerContribution);
									} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId().equals(coverageId)) {
										BigDecimal newPlanCost = planRate.getEmployerCost();
										BigDecimal employerContribution = newPlanCost.multiply(limitPercentage)
												.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
										coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
												employerContribution);
									} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId().equals(coverageId)) {
										BigDecimal newPlanCost = planRate.getEmployerCost();
										BigDecimal employerContribution = newPlanCost.multiply(limitPercentage)
												.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
										coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
												employerContribution);
									} else if (CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId().equals(coverageId)) {
										BigDecimal newPlanCost = planRate.getEmployerCost();
										BigDecimal employerContribution = newPlanCost.multiply(limitPercentage)
												.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
										coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(),
												employerContribution);
									}
								} else {
									if (limitCoverageLevel.equals(coverageId)) {
										BigDecimal newPlanCost = planRate.getEmployerCost();
										BigDecimal employerContribution = newPlanCost.multiply(limitPercentage)
												.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
										coverageLevelFunding.put(limitCoverageLevel, employerContribution);
									} else {
										if ((CoverageCodesEnums.COV_EMPLOYEE.getId().equals(coverageId)) ||
												CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId().equals(coverageId) ||
												CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId().equals(coverageId) ||
												CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId().equals(coverageId)) {
											BigDecimal newPlanCost = planRate.getEmployerCost();
											otherCoverageLevel.put(coverageId, newPlanCost);
										}
									}
								}
							} else {
								if (CoverageCodesEnums.COV_EMPLOYEE.getId().equals(coverageId)) {
									BigDecimal newPlanCost = planRate.getEmployerCost();
									employeeCoverageCost = planRate.getEmployerCost();
									BigDecimal employerContribution = newPlanCost
											.multiply((BigDecimal) coverageLevelFunding
													.get(CoverageCodesEnums.COV_EMPLOYEE.getId()))
											.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
									employeeCoverageErCost = employerContribution;
									coverageLevelFunding.put("employeeLIMIT", employerContribution);
									if (0L == employerContribution.longValue()) {
										otherCoverageLevel.put(coverageId, newPlanCost);
									} else {
										if (null == limitBaseFunding || limitBaseFunding.compareTo(employerContribution) < 0) {
											limitBaseFunding = employerContribution;
											limitCoverageLevelCFPCT = coverageId;
										} 
									}
								} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId().equals(coverageId)) {
									BigDecimal newPlanCost = planRate.getEmployerCost().subtract(employeeCoverageCost);
									BigDecimal employerContribution = newPlanCost
											.multiply((BigDecimal) coverageLevelFunding
													.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()))
											.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
									employerContribution = employerContribution.add(employeeCoverageErCost);
									coverageLevelFunding.put("employeePlusSpouseLIMIT", employerContribution);
									if (0L == employerContribution.longValue()) {
										otherCoverageLevel.put(coverageId, newPlanCost);
									} else {
										if (null == limitBaseFunding || limitBaseFunding.compareTo(employerContribution) < 0) {
											limitBaseFunding = employerContribution;
											limitCoverageLevelCFPCT = coverageId;
										} 
									}
								} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId().equals(coverageId)) {
									BigDecimal newPlanCost = planRate.getEmployerCost().subtract(employeeCoverageCost);
									BigDecimal employerContribution = newPlanCost
											.multiply((BigDecimal) coverageLevelFunding
													.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()))
											.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
									employerContribution = employerContribution.add(employeeCoverageErCost);
									coverageLevelFunding.put("employeePlusChildLIMIT", employerContribution);
									if (0L == employerContribution.longValue()) {
										otherCoverageLevel.put(coverageId, newPlanCost);
									} else {
										if (null == limitBaseFunding || limitBaseFunding.compareTo(employerContribution) < 0) {
											limitBaseFunding = employerContribution;
											limitCoverageLevelCFPCT = coverageId;
										} 
									}
								} else if (CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId().equals(coverageId)) {
									BigDecimal newPlanCost = planRate.getEmployerCost().subtract(employeeCoverageCost);
									BigDecimal employerContribution = newPlanCost
											.multiply((BigDecimal) coverageLevelFunding
													.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()))
											.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
									employerContribution = employerContribution.add(employeeCoverageErCost);
									coverageLevelFunding.put("employeePlusFamilyLIMIT", employerContribution);
									if (0L == employerContribution.longValue()) {
										otherCoverageLevel.put(coverageId, newPlanCost);
									} else {
										if (null == limitBaseFunding || limitBaseFunding.compareTo(employerContribution) < 0) {
											limitBaseFunding = employerContribution;
											limitCoverageLevelCFPCT = coverageId;
										} 
									}
								}
							}
						}
						if (!otherCoverageLevel.isEmpty()) {
							for (String coverageLevel : otherCoverageLevel.keySet()) {
								BigDecimal baseFunding = null;
								if (null != limitCoverageLevelCFPCT) {
									baseFunding = (BigDecimal) coverageLevelFunding
											.get(limitCoverageLevelCFPCT + BSSApplicationConstants.LIMIT);
									if (baseFunding.compareTo(otherCoverageLevel.get(coverageLevel)) < 0) {
										coverageLevelFunding.put(coverageLevel + BSSApplicationConstants.LIMIT,
												baseFunding);
									} else {
										coverageLevelFunding.put(coverageLevel + BSSApplicationConstants.LIMIT,
												otherCoverageLevel.get(coverageLevel));
									}
								} else {
									baseFunding = (BigDecimal) coverageLevelFunding.get(limitCoverageLevel);
									coverageLevelFunding.put(coverageLevel, baseFunding);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This method is for updating the widelyAvailablePlan (formerly ppoPlan) flag for all Plans selections.
	 * 
	 * @param planSelections
	 * @param company
	 * @param benefitPlanMap
	 */
	public static void updateWidelyAvailableFlagForPlanSelections(Set<PlanSelection> planSelections, Company company,
			Map<String, BenefitPlan> benefitPlanMap, BenefitPlanDao benefitPlanDao) {
		Set<String> planSelectionPlans = RenewalServiceHelper.getPlanSeclectionMedicalPlans(planSelections);
		if (!planSelectionPlans.isEmpty()) {
			Set<String> widelyAvailablePlanSet = benefitPlanDao.getWidelyAvailablePlans(planSelectionPlans, company.getRealmPlanYearId());
			RenewalServiceHelper.updatePPOForPlansSelections(planSelections, widelyAvailablePlanSet, benefitPlanMap);
		}
	}

	/**
	 * This method is for creating the contributions for History strategy.
	 * 
	 * @param company
	 * @param benefitPlan
	 * @param planSelection
	 * @param headCountList
	 * @return
	 */
	public static List<Contribution> constructHistoryContributions(BenefitPlan benefitPlan,
			PlanSelection planSelection, List<PlanCoverageLevelHeadCount> headCountList,
			Map<String, Map<String, String>> planOverrides, String groupType,
			Map<String, Map<String, Object>> offerTypeFunding) {
		logger.debug("Entering method : constructHistoryContributions");
		List<Contribution> contributions = new ArrayList<>();
		List<PlanContribution> planContributions = benefitPlan.getContributions();
		for (PlanContribution planContribution : planContributions) {
			String coverageLevel = planContribution.getType();
			String coverageCode = CoverageCodesEnums.valueOfCode(coverageLevel);
			
			int headCount = getHeadCount(headCountList, planContribution, coverageCode);
			int hsaHeadCount = getHsaHeadCount(headCountList, planContribution, coverageCode);
			
			Map<String, Object> coverageLevelFunding = null;
			if (null != offerTypeFunding && !offerTypeFunding.isEmpty()) {
				coverageLevelFunding = offerTypeFunding.get(benefitPlan.getPlanType());
			}
			String overrideType = determineOverrideTypeForHistoryContributions(benefitPlan, planOverrides, groupType,
					coverageCode, coverageLevelFunding);

			BigDecimal employerPercent = planContribution.getEmployerPercent() != null
					? planContribution.getEmployerPercent()
					: BigDecimal.ZERO;
			BigDecimal employerContribution = planContribution.getEmployerContribution() != null
					? planContribution.getEmployerContribution()
					: BigDecimal.ZERO;
			BigDecimal employeeContribution = planContribution.getEmployeeContribution() != null
					? planContribution.getEmployeeContribution()
					: BigDecimal.ZERO;
			Long planSelectionId = planSelection.getId();
			logger.info("benefitPlan Plan Type : {}", benefitPlan.getPlanType());
			logger.info("benefitPlan Id : {}", benefitPlan.getId());
			logger.info("planSelectionId : {}", planSelectionId);
			logger.info("PS employerContribution : {}", employerContribution);
			logger.info("PS employeeContribution : {}", employeeContribution);
			logger.info("PS employerPercent : {}", employerPercent);

			Contribution contribution = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount,
					planSelectionId, employerContribution, employeeContribution, employerPercent);
			contribution.setOverrideType(overrideType);
			contribution.setBenefitPlanAssociation(benefitPlan);
			contributions.add(contribution);
		}
		logger.debug("Exiting method : constructHistoryContributions");
		return contributions;
	}

	private static String determineOverrideTypeForHistoryContributions(BenefitPlan benefitPlan,
			Map<String, Map<String, String>> planOverrides, String groupType, String coverageCode,
			Map<String, Object> coverageLevelFunding) {
		String overrideType = null;
		if (null != planOverrides && null != planOverrides.get(benefitPlan.getId())
				&& !planOverrides.get(benefitPlan.getId()).isEmpty()) {
			Map<String, String> overrideMap = planOverrides.get(benefitPlan.getId());
			overrideType = overrideMap.get(coverageCode);
		} else {
			if (null != coverageLevelFunding && BSSApplicationConstants.EEC
					.equals(coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE))) {
				overrideType = BSSApplicationConstants.PLAN_OVERRIDE_FLTEE;
			}
			else if (BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType) || null != coverageLevelFunding) {
				overrideType = BSSApplicationConstants.PLAN_OVERRIDE_BASE;
			} else {
				overrideType = BSSApplicationConstants.PLAN_OVERRIDE_PCT;
			}
		}
		return overrideType;
	}

	/**
	 * This method is for updating contributions on shared cost by EMP/EMPLYR
	 * 
	 * @param company
	 * @param benefitPlan
	 * @param planSelection
	 * @param employerPercentIncrease
	 * @param rates
	 * @return
	 */
	public static List<Contribution> updateContributionsByPercentIncrease(BenefitPlan benefitPlan,
			PlanSelection planSelection, BigDecimal employerPercentIncrease, Map<String, List<BenefitPlanRate>> rates) {
		List<Contribution> contribList = new ArrayList<>();
		List<PlanContribution> planContributions = benefitPlan.getContributions();
		for (PlanContribution planContribution : planContributions) {
			boolean isContributionCreated = false;
			String coverageLevel = planContribution.getType();
			String coverageCode = CoverageCodesEnums.valueOfCode(coverageLevel);
			int headCount = planContribution.getHeadcount();
			int hsaHeadCount = planContribution.getHsaHeadcount();
			BigDecimal employerPercent = planContribution.getEmployerPercent() != null
					? planContribution.getEmployerPercent() : BigDecimal.ZERO;
			BigDecimal employerContribution = planContribution.getEmployerContribution() != null
					? planContribution.getEmployerContribution() : BigDecimal.ZERO;
			BigDecimal employeeContribution = planContribution.getEmployeeContribution() != null
					? planContribution.getEmployeeContribution() : BigDecimal.ZERO;
			String overrideType = planContribution.getOverrideType();
			Long planSelectionId = planSelection.getId();
			logger.info("GETTING RATES FOR PLAN : {}", benefitPlan.getId());
			List<BenefitPlanRate> planRates = rates.get(benefitPlan.getId());

			if (planRates == null || planRates.isEmpty()) {
				logger.info("**** NO PLAN RATES FOUND FOR BENEFIT PLAN  : {}", benefitPlan.getId());
				continue;
			}
			for (BenefitPlanRate planRate : planRates) {
				String coverageId = CoverageCodesEnums.valueOfId(planRate.getCoverageCode());
				Contribution contribution = null;
				if (coverageId.equals(planContribution.getType())) {
					isContributionCreated = true;
					BigDecimal newPlanCost = planRate.getEmployerCost();
					BigDecimal planCost = employerContribution.add(employeeContribution);
					BigDecimal planCostDiff = newPlanCost.subtract(planCost);
					if (BSSApplicationConstants.VOLUNTARY_PLAN_TYPES.contains(benefitPlan.getPlanType())) {
						employeeContribution = newPlanCost;
						employerContribution = BigDecimal.ZERO;
					} else {
						// Company Pays 100% of the Cost Changes
						if (Constants.BigDecimal_100.equals(employerPercentIncrease) && "FLT".equals(overrideType)) {
							if (planCostDiff.compareTo(BigDecimal.ZERO) >= 0) {
								employerContribution = employerContribution.add(planCostDiff);
								employeeContribution = newPlanCost.subtract(employerContribution);
							} else {
								BigDecimal employerPortion = employerContribution.add(planCostDiff);

								if (employerPortion.compareTo(BigDecimal.ZERO) < 0) {
									employerContribution = BigDecimal.ZERO;
									employeeContribution = newPlanCost;
								} else {
									employerContribution = employerPortion;
									employeeContribution = newPlanCost.subtract(employerContribution);
								}
							}
							// Share Cost Changes between employee and employer
						} else if (Constants.BigDecimal_50.equals(employerPercentIncrease)
								&& "FLT".equals(overrideType)) {
							if (planCostDiff.compareTo(BigDecimal.ZERO) >= 0) {
								employerContribution = employerContribution.add(planCostDiff.divide(new BigDecimal(2)));
								employeeContribution = newPlanCost.subtract(employerContribution);
							} else {
								BigDecimal fiftyPercent = planCostDiff.divide(new BigDecimal(2));
								BigDecimal employerPortion = employerContribution.add(fiftyPercent);
								BigDecimal employeePortion = newPlanCost.subtract(employerPortion);

								if (employerPortion.compareTo(BigDecimal.ZERO) < 0) {
									employerContribution = BigDecimal.ZERO;
									employeeContribution = newPlanCost;
								} else if (employeePortion.compareTo(BigDecimal.ZERO) < 0) {
									employeeContribution = BigDecimal.ZERO;
									employerContribution = newPlanCost;
								} else {
									employerContribution = employerPortion;
									employeeContribution = employeePortion;
								}
							}
							// Employees Pay the Cost Changes
						} else if (BigDecimal.ZERO.equals(employerPercentIncrease) && "FLT".equals(overrideType)) {
							if (planCostDiff.compareTo(BigDecimal.ZERO) >= 0) {
								employeeContribution = employeeContribution.add(planCostDiff);
								employerContribution = newPlanCost.subtract(employeeContribution);
							} else {
								BigDecimal employeePortion = employeeContribution.add(planCostDiff);

								if (employeePortion.compareTo(BigDecimal.ZERO) < 0) {
									employeeContribution = BigDecimal.ZERO;
									employerContribution = newPlanCost;
								} else {
									employeeContribution = employeePortion;
									employerContribution = newPlanCost.subtract(employeeContribution);
								}
							}
						} else {
							employerContribution = newPlanCost.multiply(employerPercent)
									.divide(Constants.BigDecimal_100).setScale(2, RoundingMode.HALF_UP);
							employeeContribution = newPlanCost.subtract(employerContribution).setScale(2,
									RoundingMode.HALF_UP);
						}
					}
					if (!BigDecimal.ZERO.equals(newPlanCost)) {
						employerPercent = employerContribution.divide(newPlanCost, 10, RoundingMode.CEILING)
								.multiply(Constants.BigDecimal_100);
					} else {
						employerPercent = BigDecimal.ZERO;
					}
					contribution = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount, planSelectionId,
							employerContribution, employeeContribution, employerPercent);
					contribution.setId(planContribution.getId());
					contribution.setBenefitPlan(planContribution.getBenefitPlanId());
					contribution.setBenefitPlanAssociation(benefitPlan);
					contribution.setOverrideType(planContribution.getOverrideType());
					contribList.add(contribution);
				}
			}
			if (!isContributionCreated) {
				Contribution contribution = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount,
						planSelectionId, employerContribution, employeeContribution, employerPercent);
				contribution.setId(planContribution.getId());
				contribList.add(contribution);
			}
		}
		return contribList;
	}

	/**
	 * This method is for getting mandatory plans
	 * 
	 * @param allBenefitStatePlansMap
	 * @param mandatoryPortfolios
	 * @return
	 */
	public static Map<String, StateBenefitPlan> getAllMandatoryPlans(Company company,
			Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap,
			Map<String, Set<Long>> mandatoryPortfoliosMap) {
		Map<String, StateBenefitPlan> mandatoryPlans = new HashMap<>();
		for (Map.Entry<String, Set<StateBenefitPlan>> entry : allBenefitStatePlansMap.entrySet()) {
			String planType = entry.getKey();
	        Set<StateBenefitPlan> stateBenefitPlans = entry.getValue();
			Set<Long> mandatoryPortfolios = mandatoryPortfoliosMap.get(PlanTypesEnum.getCode(planType));
			if (null != stateBenefitPlans) {
				addMandatoryPlans(company, mandatoryPlans, planType, stateBenefitPlans, mandatoryPortfolios);
			}
		}

		return mandatoryPlans;
	}

	private static void addMandatoryPlans(Company company, Map<String, StateBenefitPlan> mandatoryPlans,
			String planType, Set<StateBenefitPlan> stateBenefitPlans, Set<Long> mandatoryPortfolios) {
		for (StateBenefitPlan sbp : stateBenefitPlans) {
			if (null != mandatoryPortfolios && mandatoryPortfolios.contains(sbp.getPortfolioId())
					&& sbp.isMandatory() && null == mandatoryPlans.get(sbp.getBenefitPlan())) {
				mandatoryPlans.put(sbp.getBenefitPlan(), sbp);
			}
		}
		// adding BS-CA plans to the strategy for ACD for plan year 37 - need to remove
		// this code after the renewals.
		if (company.getRealmPlanYear().getId() == 36L
				&& BSSApplicationConstants.MEDICAL.equals(planType)) {
			for (StateBenefitPlan sbp : stateBenefitPlans) {
				if (sbp.getPortfolioId() == 11 && null == mandatoryPlans.get(sbp.getBenefitPlan())) {
					mandatoryPlans.put(sbp.getBenefitPlan(), sbp);
				}
			}
		}
	}

	/**
	 * 
	 * @param planSelections
	 * @param mandatroyPlans
	 * @param mapOfCoverageLevels
	 * @param benefitPlanMap
	 * @param strategyId
	 * @param benefitGroupId
	 */
	public static void addPlanSelectionsForMandatoryPlans(List<PlanSelection> planSelections,
			Map<String, StateBenefitPlan> mandatoryPlans, Map<String, List<CoverageLevel>> mapOfCoverageLevels,
			Map<String, BenefitPlan> benefitPlanMap, Long strategyId, Long benefitGroupId,
			Map<String, String> planVendorMap, Set<String> selectedPlanTypes) {
		Set<BenefitPlan> crossBenefitPlans = new java.util.HashSet<>();
		for (Map.Entry<String, StateBenefitPlan> entry : mandatoryPlans.entrySet()) {
			StateBenefitPlan sbp = entry.getValue();
			if (selectedPlanTypes.contains(sbp.getPlanType())) {
				List<CoverageLevel> coverageCodes = null;
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(sbp.getPlanType())) {
					coverageCodes = mapOfCoverageLevels.get(BSSApplicationConstants.DENTAL);
				} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(sbp.getPlanType())) {
					coverageCodes = mapOfCoverageLevels.get(BSSApplicationConstants.VISION);
				} else if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(sbp.getPlanType())) {
					coverageCodes = mapOfCoverageLevels.get(BSSApplicationConstants.MEDICAL);
				}
				BenefitPlan crossBenefitPlan = StrategyServiceHelper.constructBenefitPlan(sbp.getBenefitPlan(),
						sbp.getPlanType(), sbp.getVendorId());
				crossBenefitPlan.setVendorId(planVendorMap.get(sbp.getBenefitPlan()));
				RenewalServiceHelper.addBlankContributions(crossBenefitPlan, coverageCodes);
				crossBenefitPlans.add(crossBenefitPlan);
			}
		}
		if (!crossBenefitPlans.isEmpty()) {
			addPlanSelectionsWithCrossBenefitPlans(planSelections, benefitPlanMap, strategyId, benefitGroupId,
					crossBenefitPlans);
		}
	}

	/**
	 * 
	 * @param allBenefitStatePlansMap
	 * @param planType
	 * @return
	 */
	public static List<String> getBCPlansByType(Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap,
			String planType) {
		Set<StateBenefitPlan> planTypePlans = allBenefitStatePlansMap.get(planType);
		List<String> planStrings = new ArrayList<>();
		if (planTypePlans != null && !planTypePlans.isEmpty()) {
			for (StateBenefitPlan sbp : planTypePlans) {
				planStrings.add(sbp.getBenefitPlan());
			}
		}
		return planStrings;
	}

	/**
	 * 
	 * @param bp
	 * @return
	 */
	public static boolean validateCostSharePlanFunding(BenefitPlan bp) {
		boolean baseOverride = false;
		List<PlanContribution> planContributions = bp.getContributions();
		for (PlanContribution planContribution : planContributions) {
			if (null != planContribution.getOverrideType() && BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(planContribution.getOverrideType())) {
				baseOverride = true;
			}
		}
		return baseOverride;
	}

	/**
	 * 
	 * @param planCarrierMap
	 * @param allBenefitStatePlansMap
	 */
	public static void updateMedicalPlans(Set<Long> medicalSelectedPlanCarriers,
			Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap) {
		if (null != medicalSelectedPlanCarriers && !medicalSelectedPlanCarriers.isEmpty()) {
			Set<StateBenefitPlan> lSbp = allBenefitStatePlansMap.get(BSSApplicationConstants.MEDICAL);
			Set<StateBenefitPlan> updatedMedicalPlans = new HashSet<>();
			for (StateBenefitPlan sbp : lSbp) {
				if (medicalSelectedPlanCarriers.contains(sbp.getPortfolioId())) {
					updatedMedicalPlans.add(sbp);
				}
			}
			allBenefitStatePlansMap.put(BSSApplicationConstants.MEDICAL, updatedMedicalPlans);
		}
	}

	/**
	 * 
	 * @param bgAllHealthPlansMap
	 * @param allBenefitStatePlansMap
	 * @param planCarrierMap
	 * @param realmPlanMapping
	 * @return
	 */
	public static Map<String, Set<Long>> getSelectedPlanCarriers(
			Map<String, Map<String, BenefitPlan>> bgAllHealthPlansMap,
			Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap,
			Map<String, Set<PlanCarrier>> planCarrierMap,
			Map<String, PlanMapping> realmPlanMapping) {
		Map<String, Set<Long>> selectedPlancarriers = new HashMap<>();
		Map<String, Set<Long>> mandatoryPortfoliosMap = BenefitCategoriesHelper.getMandatoryPlanCarriers(planCarrierMap);
		Set<String> medicalPlanCarriers = BenefitCategoriesHelper.getMedicalPlanCarriers(planCarrierMap);
		Map<Long, Set<Long>> parentChildCarrierIds = getParentChildCarriers(planCarrierMap);
		for (Map.Entry<String, Map<String, BenefitPlan>> bgAllHealthPlansMapEntry : bgAllHealthPlansMap.entrySet()) {
			String planType = bgAllHealthPlansMapEntry.getKey();
			Set<Long> mandatoryCarriers = mandatoryPortfoliosMap.get(PlanTypesEnum.getName(planType));
			Map<String, BenefitPlan> bps = bgAllHealthPlansMapEntry.getValue();
			Set<StateBenefitPlan> spbs = allBenefitStatePlansMap.get(PlanTypesEnum.getName(planType));
			Set<Long> planCarrierIds = getPlanCarrierIds(realmPlanMapping, medicalPlanCarriers, bps, spbs);
			// planCarrierIds will contain the primary plan carrier. Remove all the child
			// carriers whose parent is not present in the planCarrierIds
			if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)) {
				for (Map.Entry<Long, Set<Long>> entry : parentChildCarrierIds.entrySet()) {
					if (!planCarrierIds.contains(entry.getKey())) {
						planCarrierIds.removeAll(entry.getValue());
					}
				}
			}
			if (null != mandatoryCarriers && !mandatoryCarriers.isEmpty()) {
				planCarrierIds.addAll(mandatoryCarriers);
			}
			selectedPlancarriers.put(planType, planCarrierIds);
		}
		return selectedPlancarriers;
	}

	private static Set<Long> getPlanCarrierIds(Map<String, PlanMapping> realmPlanMapping,
			Set<String> medicalPlanCarriers, Map<String, BenefitPlan> bps, Set<StateBenefitPlan> spbs) {
		Set<Long> planCarrierIds = new HashSet<>();
		for (String bpId : bps.keySet()) {
			PlanMapping rp = realmPlanMapping.get(bpId);
			String updateBenefitPlan = getUpdateBenefitPlan(bpId, rp);
			for (StateBenefitPlan sbp : spbs) {
				if (updateBenefitPlan.equals(sbp.getBenefitPlan())) {
					addPlanCarrierIds(medicalPlanCarriers, planCarrierIds, sbp);
				}
			}
		}
		return planCarrierIds;
	}
	
	private static String getUpdateBenefitPlan(String bpId, PlanMapping rp) {
		return null != rp ? rp.getNewBenefitPlan() : bpId;
	}

	private static void addPlanCarrierIds(Set<String> medicalPlanCarriers, Set<Long> planCarrierIds,
			StateBenefitPlan sbp) {
		if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(sbp.getPlanType())) {
			if (medicalPlanCarriers.contains(Long.toString(sbp.getPortfolioId()))) {
				planCarrierIds.add(sbp.getPortfolioId());
			}
		} else {
			planCarrierIds.add(sbp.getPortfolioId());
		}
	}

	/**
	 * 
	 * @param mandatoryPlans
	 * @param bgAllHealthPlansMap
	 * @param allBenefitStatePlansMap
	 * @param selectedPlancarriers
	 */
	public static void addMandatoryPortfolioPlans(Map<String, StateBenefitPlan> mandatoryPlans,
			Map<String, Map<String, BenefitPlan>> bgAllHealthPlansMap,
			Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap, Map<String, Set<Long>> selectedPlancarriers) {
		for (Map.Entry<String, Set<Long>> entry: selectedPlancarriers.entrySet()) {
			String planType = entry.getKey();
			Set<Long> offerTypeCarriers = entry.getValue();
			Map<String, BenefitPlan> offerTypePlanMap = bgAllHealthPlansMap.get(planType);
			String planTypeConverted = null;
			if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)) {
				planTypeConverted = BSSApplicationConstants.MEDICAL;
			} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planType)) {
				planTypeConverted = BSSApplicationConstants.VISION;
			} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
				planTypeConverted = BSSApplicationConstants.DENTAL;
			}
			for (StateBenefitPlan sbp1 : allBenefitStatePlansMap.get(planTypeConverted)) {
				if (!offerTypePlanMap.keySet().contains(sbp1.getBenefitPlan())
						&& offerTypeCarriers.contains(sbp1.getPortfolioId())) {
					mandatoryPlans.put(sbp1.getBenefitPlan(), sbp1);
				}
			}
		}
	}

	/**
	 * 
	 * @param company
	 * @param isDefaultSubmit
	 * @return
	 */
	public static List<Strategy> constructRenewalStrategies(Company company, boolean isDefaultSubmit,
			Map<String, String> realmRuleConfigurations, boolean isPreload, int acaFplOpted) {
		List<Strategy> renewalStrategies = new ArrayList<>();
		Strategy strategy = StrategyServiceHelper.constructStrategyForRenewals(company,
				StrategyTypesEnums.F_S.getName(), StrategyTypesEnums.F_S.getValue(),
				BSSApplicationConstants.STATUS_ACTIVE, acaFplOpted);
		strategy.setCreatedBy(getStrategyCreatedBy(isDefaultSubmit, isPreload));
		renewalStrategies.add(strategy);
		String isCostShareEnabled = realmRuleConfigurations.get("COST_SHARE_STRATEGIES");
		if ("true".equals(isCostShareEnabled) && !isDefaultSubmit) {
			// creating employee shares the cost changes strategy
			Strategy strategyEEPC = StrategyServiceHelper.constructStrategyForRenewals(company,
					StrategyTypesEnums.EE_PC.getName(), StrategyTypesEnums.EE_PC.getValue(),
					BSSApplicationConstants.STATUS_ACTIVE, acaFplOpted);
			renewalStrategies.add(strategyEEPC);

			// creating employer shares the cost changes strategy
			Strategy strategyERPC = StrategyServiceHelper.constructStrategyForRenewals(company,
					StrategyTypesEnums.ER_PC.getName(), StrategyTypesEnums.ER_PC.getValue(),
					BSSApplicationConstants.STATUS_ACTIVE, acaFplOpted);

			renewalStrategies.add(strategyERPC);

			// creating employer/employee shares the cost changes strategy
			Strategy strategySPC = StrategyServiceHelper.constructStrategyForRenewals(company,
					StrategyTypesEnums.S_PC.getName(), StrategyTypesEnums.S_PC.getValue(),
					BSSApplicationConstants.STATUS_ACTIVE, acaFplOpted);
			renewalStrategies.add(strategySPC);
		}
		return renewalStrategies;
	}

	/**
	 * 
	 * @param renewalStrategies
	 * @param benefitGroups
	 * @param waitPeriodMap
	 * @param groupHeadCountMap
	 * @param company
	 * @return
	 */
	public static List<BenefitGroupStrategy> constructStrategyBenefitGroups(List<Strategy> renewalStrategies,
			List<BenefitGroup> benefitGroups, Map<String, String> waitPeriodMap, Map<String, Integer> groupHeadCountMap,
			Company company, String status) {
		List<BenefitGroupStrategy> benefitGroupStrategies = new ArrayList<>();
		for (Strategy sg : renewalStrategies) {
			for (BenefitGroup bg : benefitGroups) {
				if( "D".equals( bg.getStatus() ) ) {
					// skip deleted strategy
				} else {
					BenefitGroupStrategy bs = constructStrategyBenefitGroup(waitPeriodMap, groupHeadCountMap, company,
							status, sg, bg);
					benefitGroupStrategies.add(bs);
				}
			}
		}
		return benefitGroupStrategies;
	}

	private static BenefitGroupStrategy constructStrategyBenefitGroup(Map<String, String> waitPeriodMap,
			Map<String, Integer> groupHeadCountMap, Company company, String status, Strategy sg, BenefitGroup bg) {
		String benefitProgram = bg.getBenefitProgram();
		String waitPeriod = waitPeriodMap.get(benefitProgram);
		BenefitGroupStrategy bs = new BenefitGroupStrategy();
		bs.setBenefitGroup(bg);
		bs.setDefaultGroup(benefitProgram.equals(company.getBenefitProgram()));
		bs.setWaitingPeriod(waitPeriod);
		if (null != status) {
			bs.setStatus(status);
		} else {
			bs.setStatus(bg.getStatus());
		}
		bs.setGroupId(bg.getId());
		if (groupHeadCountMap != null && groupHeadCountMap.get(benefitProgram) != null) {
			bs.setHeadcount(groupHeadCountMap.get(benefitProgram));
		} else {
			bs.setHeadcount(0);
		}
		bs.setStrategy(sg);
		bs.setStrategyId(sg.getId());
		return bs;
	}

	/**
	 * 
	 * @param benefitGroupStrategies
	 * @return
	 */
	public static Map<String, Set<Long>> getStrategyGroupByBenefitProgram(
			List<BenefitGroupStrategy> benefitGroupStrategies) {
		Map<String, Set<Long>> strategyGroupBenefitProgramMap = new HashMap<>();
		for (BenefitGroupStrategy bgs : benefitGroupStrategies) {
			String bp = bgs.getBenefitGroup().getBenefitProgram();
			if (null != strategyGroupBenefitProgramMap.get(bp)) {
				Set<Long> strategyGroupIds = strategyGroupBenefitProgramMap.get(bp);
				strategyGroupIds.add(bgs.getId());
			} else {
				Set<Long> strategyGroupIds = new HashSet<>();
				strategyGroupIds.add(bgs.getId());
				strategyGroupBenefitProgramMap.put(bp, strategyGroupIds);
			}
		}
		return strategyGroupBenefitProgramMap;
	}

	private static BigDecimal getMinFundingFor(List<CarrierMinimumFunding> lowestCostPlanPerCarrier, Set<Long> selectedPlanCarriers, String planTypeCode,
			Company company) {
		BigDecimal minFunding = getStartingMinimumFundingAmount(lowestCostPlanPerCarrier, selectedPlanCarriers);
		MinimumFunding minFund = CommonServiceHelper.extractMinFundingDetails(PlanTypesEnum.getName(planTypeCode), company);
		if (MinFundExceptionService.FLAT.equals(minFund.getMinFundType())) {
			return minFund.getMinFundValue().setScale(2, RoundingMode.HALF_UP);
		}
		for (CarrierMinimumFunding carrierMinimumFunding : lowestCostPlanPerCarrier) {
			if (planTypeCode.equals(carrierMinimumFunding.getPlanType())) {
				if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planTypeCode)) {
					if (selectedPlanCarriers.contains(carrierMinimumFunding.getCarrierId())) {
						minFunding = minFunding.min(carrierMinimumFunding.getMinimumFundingAmt());
					}
				} else {
					minFunding = carrierMinimumFunding.getMinimumFundingAmt();
				}
			}
		}
		BigDecimal minFundingPercent = minFund.getMinFundValue();
		minFundingPercent = minFundingPercent.divide(new BigDecimal(100));
		return minFunding.multiply(minFundingPercent).setScale(2, RoundingMode.HALF_UP);
		
	}
	
	private static Map<String, BigDecimal> getMinFundingForDefaultType(List<Contribution> contributions, List<String> mandatoryPlansToExclude,
			Company company) {
		BigDecimal medicalLowCostPlan = null;
		BigDecimal dentalLowCostPlan = null;
		BigDecimal visionLowCostPlan = null;
		for (Contribution cb : contributions) {
			BigDecimal planCost = cb.getEmployeeContribution().add(cb.getEmployerContribution());
			String planId = cb.getBenefitPlanAssociation().getId();
			// Exclude the plans that are made mandatory by insurance services
			// unless the client is HQ in the state.
			if (!mandatoryPlansToExclude.contains(planId)) {
				if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
						&& BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(cb.getCoverageLevel())) {
					medicalLowCostPlan = updateLowCostPlan(medicalLowCostPlan, planCost);
				} else if (BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
						&& BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(cb.getCoverageLevel())) {
					dentalLowCostPlan = updateLowCostPlan(dentalLowCostPlan, planCost);
				} else if (BSSApplicationConstants.VISION_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
						&& BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(cb.getCoverageLevel())) {
					visionLowCostPlan = updateLowCostPlan(visionLowCostPlan, planCost);
				}
			}
		}

		return prepareMinFundingMap(medicalLowCostPlan, dentalLowCostPlan,
				visionLowCostPlan, company);
	}
	
	private static Map<String, BigDecimal> getMinFundingForDefaultType(List<PlanSelection> planSelections,
			Map<String, List<BenefitPlanRate>> rates, List<String> mandatoryPlansToExclude, Company company) {
		BigDecimal medicalLowCostPlan = null;
		BigDecimal dentalLowCostPlan = null;
		BigDecimal visionLowCostPlan = null;

		for (PlanSelection ps : planSelections) {

			// Exclude the plans that are made mandatory by insurance services
			if (!mandatoryPlansToExclude.contains(ps.getBenefitPlan())) {
				BigDecimal planCost = getPlanCostForEmployeeCvg(rates, ps);
				if (planCost != null) {
					switch (ps.getPlanType()) {
						case BSSApplicationConstants.MEDICAL_PLAN_TYPE:
							medicalLowCostPlan = updateLowCostPlan(medicalLowCostPlan, planCost);
							break;
						case BSSApplicationConstants.DENTAL_PLAN_TYPE:
							dentalLowCostPlan = updateLowCostPlan(dentalLowCostPlan, planCost);
							break;
						case BSSApplicationConstants.VISION_PLAN_TYPE:
							visionLowCostPlan = updateLowCostPlan(visionLowCostPlan, planCost);
							break;
						default:
							break;
						}
				}
			}

		}

		return prepareMinFundingMap(medicalLowCostPlan, dentalLowCostPlan, visionLowCostPlan, company);
	}

	private static BigDecimal getPlanCostForEmployeeCvg(Map<String, List<BenefitPlanRate>> rates, PlanSelection ps) {
		BigDecimal planCost = null;
		List<BenefitPlanRate> planRates = rates.get(ps.getBenefitPlan());
		for (BenefitPlanRate planRate : planRates) {
			if (BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(planRate.getCoverageCode())) {
				planCost = planRate.getEmployerCost();
			}
		}
		return planCost;
	}
	
	private static BigDecimal updateLowCostPlan(BigDecimal currentLowCostPlan, BigDecimal newPlanCost) {
	    if (currentLowCostPlan == null || (newPlanCost.compareTo(BigDecimal.ZERO) > 0 && newPlanCost.compareTo(currentLowCostPlan) < 0)) {
	        return newPlanCost;
	    }
	    return currentLowCostPlan;
	}

	private static Map<String, BigDecimal> prepareMinFundingMap(BigDecimal medicalLowCostPlan,
			BigDecimal dentalLowCostPlan, BigDecimal visionLowCostPlan, Company company) {
		Map<String, BigDecimal> minimumFundingMap = new HashMap<>();

		addMinFundingValueToMap(BSSApplicationConstants.MEDICAL_PLAN_TYPE, medicalLowCostPlan, minimumFundingMap,
				company);
		addMinFundingValueToMap(BSSApplicationConstants.DENTAL_PLAN_TYPE, dentalLowCostPlan, minimumFundingMap,
				company);
		addMinFundingValueToMap(BSSApplicationConstants.VISION_PLAN_TYPE, visionLowCostPlan, minimumFundingMap,
				company);
		return minimumFundingMap;
	}
	
	private static void addMinFundingValueToMap(String planTypeCode, BigDecimal lowCostPlan,
			Map<String, BigDecimal> minimumFundingMap, Company company) {
		MinimumFunding minFunding = CommonServiceHelper.extractMinFundingDetails(PlanTypesEnum.getName(planTypeCode),
				company);
		BigDecimal minFundingValue = null;
		if (MinFundExceptionService.FLAT.equals(minFunding.getMinFundType())) {
			minFundingValue = minFunding.getMinFundValue().setScale(2, RoundingMode.HALF_UP);
		} else {
			BigDecimal minFundingPercent = minFunding.getMinFundValue();
			minFundingPercent = minFundingPercent.divide(new BigDecimal(100));
			if (null != lowCostPlan) {
				minFundingValue = lowCostPlan.multiply(minFundingPercent).setScale(2, RoundingMode.HALF_UP);
			}
		}
		minimumFundingMap.put(planTypeCode, minFundingValue);
	}

	private static Map<String, BigDecimal> getMinFundingForHQType(Company company,
			Map<String, Set<Long>> selectedPlanCarriers, List<CarrierMinimumFunding> minFundings) {
		Map<String, BigDecimal> minimumFundingMap = new HashMap<>();
		BigDecimal medMinFunding = getMinFundingFor(minFundings,
				selectedPlanCarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE),
				BSSApplicationConstants.MEDICAL_PLAN_TYPE, company);
		BigDecimal visionMinFunding = getMinFundingFor(minFundings, Collections.emptySet(),
				BSSApplicationConstants.VISION_PLAN_TYPE, company);
		BigDecimal dentalMinFunding = getMinFundingFor(minFundings, Collections.emptySet(),
				BSSApplicationConstants.DENTAL_PLAN_TYPE, company);

		minimumFundingMap.put(Constants.MEDICAL_CODE, medMinFunding);
		minimumFundingMap.put(Constants.DENTAL_CODE, dentalMinFunding);
		minimumFundingMap.put(Constants.VISION_CODE, visionMinFunding);

		return minimumFundingMap;
	}

	private static void updateContributionsForDefaultMinFunding(Map<String, BigDecimal> minimumFundingMap,
			Contribution cb, BigDecimal planCost) {
		BigDecimal medicalMinFunding = minimumFundingMap.get(Constants.MEDICAL_CODE);
		BigDecimal dentalMinFunding = minimumFundingMap.get(Constants.DENTAL_CODE);
		BigDecimal visionMinFunding = minimumFundingMap.get(Constants.VISION_CODE);
		logger.info("Minimum Funding for Medical : {}", medicalMinFunding);
		logger.info("Minimum Funding for dental  : {}", dentalMinFunding);
		logger.info("Minimum Funding for vision  : {}", visionMinFunding);

		if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())) {
			medicalMinFunding = Collections.min(Arrays.asList(medicalMinFunding, planCost));
		} else if (BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())) {
			dentalMinFunding = Collections.min(Arrays.asList(dentalMinFunding, planCost));
		} else if (BSSApplicationConstants.VISION_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())) {
			visionMinFunding = Collections.min(Arrays.asList(visionMinFunding, planCost));
		}

		if (null != medicalMinFunding
				&& BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
				&& cb.getEmployerContribution().compareTo(medicalMinFunding) < 0) {
			logger.info("Medical BenefitPlan not meeting MIN Funding : {}", cb.getBenefitPlanAssociation().getId());
			cb.setEmployerContribution(medicalMinFunding);
			cb.setEmployeeContribution(planCost.subtract(medicalMinFunding));
			cb.setEmployerPercent(cb.getEmployerContribution().divide(planCost, 10, RoundingMode.CEILING)
					.multiply(Constants.BigDecimal_100));
			cb.setOverrideType(PlanOverrideServiceHelper.getMNFPlanOverrideType(cb.getOverrideType()));
		} else if (null != dentalMinFunding
				&& BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
				&& cb.getEmployerContribution().compareTo(dentalMinFunding) < 0) {
			logger.info("Dental BenefitPlan not meeting MIN Funding : {}", cb.getBenefitPlanAssociation().getId());
			cb.setEmployerContribution(dentalMinFunding);
			cb.setEmployeeContribution(planCost.subtract(dentalMinFunding));
			cb.setEmployerPercent(cb.getEmployerContribution().divide(planCost, 10, RoundingMode.CEILING)
					.multiply(Constants.BigDecimal_100));
			cb.setOverrideType(PlanOverrideServiceHelper.getMNFPlanOverrideType(cb.getOverrideType()));
		} else if (null != visionMinFunding
				&& BSSApplicationConstants.VISION_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
				&& cb.getEmployerContribution().compareTo(visionMinFunding) < 0) {
			logger.info("Vision BenefitPlan not meeting MIN Funding : {}", cb.getBenefitPlanAssociation().getId());
			cb.setEmployerContribution(visionMinFunding);
			cb.setEmployeeContribution(planCost.subtract(visionMinFunding));
			cb.setEmployerPercent(cb.getEmployerContribution().divide(planCost, 10, RoundingMode.CEILING)
					.multiply(Constants.BigDecimal_100));
			cb.setOverrideType(PlanOverrideServiceHelper.getMNFPlanOverrideType(cb.getOverrideType()));
		}
	}
	
	private static void updateContributionsForHQMinFunding(Map<String, BigDecimal> minimumFundingMap,
			Company company, Contribution cb, BigDecimal planCost, Map<String, BigDecimal> empLvlCostPerPlan) {
		BigDecimal medicalMinFunding = minimumFundingMap.get(Constants.MEDICAL_CODE);
		BigDecimal dentalMinFunding = minimumFundingMap.get(Constants.DENTAL_CODE);
		BigDecimal visionMinFunding = minimumFundingMap.get(Constants.VISION_CODE);
		
		BigDecimal empOnlyMinFundingPlanCost = findEmpOnlyMinFundingPlanCost(company, cb, empLvlCostPerPlan);
		
		if(BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())) {
			medicalMinFunding = Collections.min(Arrays.asList(medicalMinFunding, empOnlyMinFundingPlanCost, planCost));
		} else if(BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())) {
			dentalMinFunding = Collections.min(Arrays.asList(dentalMinFunding, empOnlyMinFundingPlanCost, planCost));
		} else if(BSSApplicationConstants.VISION_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())) {
			visionMinFunding = Collections.min(Arrays.asList(visionMinFunding, empOnlyMinFundingPlanCost, planCost));
		}

		if (null != medicalMinFunding
				&& BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
				&& cb.getEmployerContribution().compareTo(medicalMinFunding) < 0) {
			logger.info("Medical BenefitPlan not meeting HQ MIN Funding : {} ",
					cb.getBenefitPlanAssociation().getId());
			cb.setEmployerContribution(medicalMinFunding);
			cb.setEmployeeContribution(planCost.subtract(cb.getEmployerContribution()));
			cb.setEmployerPercent(cb.getEmployerContribution().divide(planCost, 10, RoundingMode.CEILING)
					.multiply(Constants.BigDecimal_100));
			cb.setOverrideType(PlanOverrideServiceHelper.getMNFPlanOverrideType(cb.getOverrideType()));
		} else if (null != dentalMinFunding
				&& BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
				&& cb.getEmployerContribution().compareTo(dentalMinFunding) < 0) {
			logger.info(
					"Dental BenefitPlan not meeting HQ MIN Funding : {} ", cb.getBenefitPlanAssociation().getId());
			cb.setEmployerContribution(dentalMinFunding);
			cb.setEmployeeContribution(planCost.subtract(cb.getEmployerContribution()));
			cb.setEmployerPercent(cb.getEmployerContribution().divide(planCost, 10, RoundingMode.CEILING)
					.multiply(Constants.BigDecimal_100));
			cb.setOverrideType(PlanOverrideServiceHelper.getMNFPlanOverrideType(cb.getOverrideType()));
		} else if (null != visionMinFunding
				&& BSSApplicationConstants.VISION_PLAN_TYPE.equals(cb.getBenefitPlanAssociation().getPlanType())
				&& cb.getEmployerContribution().compareTo(visionMinFunding) < 0) {
			logger.info(
					"Vision BenefitPlan not meeting HQ MIN Funding : {} ", cb.getBenefitPlanAssociation().getId());
			cb.setEmployerContribution(visionMinFunding);
			cb.setEmployeeContribution(planCost.subtract(cb.getEmployerContribution()));
			cb.setEmployerPercent(cb.getEmployerContribution().divide(planCost, 10, RoundingMode.CEILING)
					.multiply(Constants.BigDecimal_100));
			cb.setOverrideType(PlanOverrideServiceHelper.getMNFPlanOverrideType(cb.getOverrideType()));
		}
	}
	
	private static BigDecimal findEmpOnlyMinFundingPlanCost(Company company, Contribution cb,
			Map<String, BigDecimal> empLvlCostPerPlan) {
		BigDecimal empOnlyMinFundingPlanCost = null;
		String planTypeCode = cb.getBenefitPlanAssociation().getPlanType();
		MinimumFunding minFunding = CommonServiceHelper.extractMinFundingDetails(PlanTypesEnum.getName(planTypeCode), company);
		logger.info("Minimum Funding for {} : {}", PlanTypesEnum.getName(planTypeCode), minFunding);
		if (MinFundExceptionService.FLAT.equals(minFunding.getMinFundType())) {
			empOnlyMinFundingPlanCost = minFunding.getMinFundValue();
		} else {
			BigDecimal minFundingPercent = minFunding.getMinFundValue();
			minFundingPercent = minFundingPercent.divide(new BigDecimal(100));
			BigDecimal empLvlPlanCost = empLvlCostPerPlan.get(cb.getBenefitPlanAssociation().getId());
			empOnlyMinFundingPlanCost = empLvlPlanCost.multiply(minFundingPercent);
		}
		return empOnlyMinFundingPlanCost.setScale(2, RoundingMode.HALF_UP);
	}

	private static Map<String, BigDecimal> prepareMapOfEmpCvgCostByPlan(List<Contribution> contributions) {
		Map<String, BigDecimal> empLvlEmpContPerPlan = new HashMap<>();
		for (Contribution contribution : contributions) {
			if (BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(contribution.getCoverageLevel())) {
				empLvlEmpContPerPlan.put(contribution.getBenefitPlanAssociation().getId(),
						contribution.getEmployeeContribution().add(contribution.getEmployerContribution()));
			}
		}
		return empLvlEmpContPerPlan;
	}
	
	private static Map<String, BigDecimal> prepareMapOfEmployerCvgCostByPlan(List<Contribution> contributions) {
		Map<String, BigDecimal> empLvlEmployerContPerPlan = new HashMap<>();
		for (Contribution contribution : contributions) {
			if (BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(contribution.getCoverageLevel())) {
				empLvlEmployerContPerPlan.put(contribution.getBenefitPlanAssociation().getId(),
						contribution.getEmployerContribution());
			}
		}
		return empLvlEmployerContPerPlan;
	}
	
	private static String getStrategyCreatedBy(boolean isDefaultSubmit, boolean isPreload) {
		String createdBy;
		if (isDefaultSubmit) {
			createdBy = BSSApplicationConstants.DEFAULT_SUBMIT;
		} else if (isPreload) {
			createdBy = BSSApplicationConstants.CREATED_BY_PRELOAD;
		} else {
			createdBy = BSSSecurityUtils.getAuthenticatedPersonId();
		}
		return createdBy;
	}
	
	/**
	 * 
	 * @param groupFundingDetails
	 * @param bgsHealthPlansMap
	 * @param mapOfCoverageLevels
	 * @param erEeMapping
	 */
	public static void updateGroupDVPlansBsupp(Map<String, Map<String, Map<String, Object>>> groupFundingDetails,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels, Map<String, String> erEeMapping) {
		Set<String> bsuppBenefitPrograms = new HashSet<>();
		for (String bProgram : groupFundingDetails.keySet()) {
			String fundingType = null;
			if (null != groupFundingDetails.get(bProgram)
					&& null != groupFundingDetails.get(bProgram).get(BSSApplicationConstants.MEDICAL_PLAN_TYPE)) {
				fundingType = (String) groupFundingDetails.get(bProgram).get(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
						.get(BSSApplicationConstants.FUNDING_TYPE);
			}
			if (BSSApplicationConstants.BSUPP.equals(fundingType)) {
				bsuppBenefitPrograms.add(bProgram);
			}
		}
		for (String bProgram : bsuppBenefitPrograms) {
			if (MapUtils.isNotEmpty(bgsHealthPlansMap) && null != bgsHealthPlansMap.get(bProgram)) {
				Map<String, Map<String, BenefitPlan>> bpPlanTypePlans = bgsHealthPlansMap.get(bProgram);
				Map<String, BenefitPlan> dentalPlanMap = bpPlanTypePlans.get(BSSApplicationConstants.DENTAL_PLAN_TYPE);
				//process Dental plan map
				processPlanMap(dentalPlanMap, Constants.DENTAL, erEeMapping,mapOfCoverageLevels,BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, BSSApplicationConstants.DENTAL_PLAN_TYPE);
				Map<String, BenefitPlan> visionPlanMap = bpPlanTypePlans.get(BSSApplicationConstants.VISION_PLAN_TYPE);
				//process Vision plan map
				processPlanMap(visionPlanMap, Constants.VISION, erEeMapping,mapOfCoverageLevels,BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, BSSApplicationConstants.VISION_PLAN_TYPE);
				
			}
		}
	}
	
	private static void processPlanMap(Map<String, BenefitPlan> planMap, String planType,
			Map<String, String> erEeMapping, Map<String, List<CoverageLevel>> mapOfCoverageLevels,
			String voluntaryPlanType, String planTypeCd) {

		if (MapUtils.isNotEmpty(planMap)) {
			Map<String, BenefitPlan> voluntaryPlans = new HashMap<>();
			List<String> groupPlanIds = new ArrayList<>();

			for (Map.Entry<String, BenefitPlan> entry : planMap.entrySet()) {
				BenefitPlan bp = entry.getValue();
				if (planTypeCd.equals(bp.getPlanType())) {
					bp.setId(erEeMapping.get(bp.getId()));
					bp.setPlanType(voluntaryPlanType);
					groupPlanIds.add(entry.getKey());
					RenewalServiceHelper.addBlankContributions(bp, mapOfCoverageLevels.get(planType));
					voluntaryPlans.put(bp.getId(), bp);
				}
			}

			if (CollectionUtils.isNotEmpty(groupPlanIds)) {
				planMap.clear();
			}

			if (MapUtils.isNotEmpty(voluntaryPlans)) {
				planMap.putAll(voluntaryPlans);
			}
		}
	}
	
	/**
	 * 
	 * @param allBenefitStatePlansMap
	 * @param mandatoryPlans
	 * @param selectedMedicalCarriers
	 * @return
	 */
	public static List<String> getFplPlans(Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap,
			Map<String, StateBenefitPlan> mandatoryPlans, Set<Long> selectedMedicalCarriers) {
		Set<StateBenefitPlan> medicalPlans = allBenefitStatePlansMap.get(BSSApplicationConstants.MEDICAL);
		List<String> fplMedicalPlans = new ArrayList<>();
		
		if (CollectionUtils.isEmpty(medicalPlans)) {
			return fplMedicalPlans;
		}
		for (StateBenefitPlan sb : medicalPlans) {
			if (null != selectedMedicalCarriers && selectedMedicalCarriers.contains(sb.getPortfolioId())) {
				if (BSSApplicationConstants.FPL.equals(sb.getPlanCategory())) {
					fplMedicalPlans.add(sb.getBenefitPlan());
					if (!mandatoryPlans.containsKey(sb.getBenefitPlan())) {
						mandatoryPlans.put(sb.getBenefitPlan(), sb);
					}
				} else if (BSSApplicationConstants.MND.equals(sb.getPlanCategory())
						&& !mandatoryPlans.containsKey(sb.getBenefitPlan())) {
					mandatoryPlans.put(sb.getBenefitPlan(), sb);
				}
			}
		}
		return fplMedicalPlans;
	}
	
	private static BigDecimal calculateEecEmployerContribution(Map<String, Object> coverageLevelFunding, String coverageLevel, BigDecimal planCost) {
		BigDecimal erLimit = (BigDecimal) coverageLevelFunding.get(coverageLevel + BSSApplicationConstants.LIMIT);
		BigDecimal eeMinimum = (BigDecimal) coverageLevelFunding.get(coverageLevel);
		BigDecimal erContribution = planCost.subtract(eeMinimum);
		
		if (erLimit != null && erLimit.compareTo(erContribution) < 0) {
			erContribution = erLimit; 
		}

		if (erContribution.compareTo(BigDecimal.ZERO) < 0) {
			erContribution = BigDecimal.ZERO;
		}
		
		return erContribution;
	}
	
	/**
	 * 
	 * @param planCarrierMap
	 * @param selectedPlancarriers
	 */
	public static void addMissingChildCarriers(Map<String, Set<PlanCarrier>> planCarrierMap,
			Map<String, Set<Long>> selectedPlancarriers) {
		Set<PlanCarrier> medicalCarriers = planCarrierMap.get(BSSApplicationConstants.MEDICAL);
		Map<Long, Set<Long>> planCarriersByParentPortfolio = new HashMap<>();
		if (CollectionUtils.isNotEmpty(medicalCarriers)) {
			for (PlanCarrier pc : medicalCarriers) {
				if (null != pc.getParentId()) {
					pc.getParentId().stream().filter(Objects::nonNull).map(Long::valueOf)
							.forEach(parentId -> planCarriersByParentPortfolio
									.computeIfAbsent(parentId, k -> new HashSet<>()).add((long) pc.getId()));
				}
			}
			Set<Long> selectedMedicalCarriers = selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			Set<Long> missingChildCarriers = getMissingChildCarriers(planCarriersByParentPortfolio,
					selectedMedicalCarriers);
			selectedMedicalCarriers.addAll(missingChildCarriers);
		}
	}

	private static Set<Long> getMissingChildCarriers(Map<Long, Set<Long>> planCarriersByParentPortfolio,
			Set<Long> selectedMedicalCarriers) {
		Set<Long> missingChildCarriers = new HashSet<>();
		for (Long sc : selectedMedicalCarriers) {
			if (null != planCarriersByParentPortfolio.get(sc)) {
				missingChildCarriers.addAll(planCarriersByParentPortfolio.get(sc));
			}
		}
		return missingChildCarriers;
	}
	
	public static boolean isBenOfferExceptionAvailable(String planType, Map<String, Boolean> benOfferExceptions) {
		boolean exceptionAvailable = false;
		if(PlanTypesEnum.MEDICAL.getName().equals(planType)) {
			planType = PlanTypesEnum.MEDICAL.getCode();
		} else if(PlanTypesEnum.DENTAL.getName().equals(planType)) {
			planType = PlanTypesEnum.DENTAL.getCode();
		} else if(PlanTypesEnum.VISION.getName().equals(planType)) {
			planType = PlanTypesEnum.VISION.getCode();
		}
		if (null != benOfferExceptions.get(planType) && benOfferExceptions.get(planType)) {
			exceptionAvailable = true;
		}
		return exceptionAvailable;
	}

	public static Map<Long, Set<Long>> getParentChildCarriers(Map<String, Set<PlanCarrier>> planCarrierMap) {
		Map<Long, Set<Long>> parentChildCarrierIds = new HashMap<>();
		Set<PlanCarrier> medicalPlanCarriers = planCarrierMap.get(BSSApplicationConstants.MEDICAL);
		if (medicalPlanCarriers != null) {
			for (PlanCarrier planCarrier : medicalPlanCarriers) {
				if (null != planCarrier.getParentId()) {
					planCarrier.getParentId().stream().filter(Objects::nonNull).map(Long::valueOf)
							.forEach(parentId -> parentChildCarrierIds.computeIfAbsent(parentId, k -> new HashSet<>())
									.add((long) planCarrier.getId()));
				}
			}
		}
		return parentChildCarrierIds;
	}
	
	/**
	 * This method is for calculating the strategy estimates during strategy pre load.
	 * Waver allowance and HSA are not part of this.
	 * @param strategyId
	 * @param benefitGroupId
	 * @param contributions
	 * @param groupFundingDetails
	 * @return
	 */
	public static Map<Long, List<StrategyEstimate>> calculateStrategyGroupEstimates(Long strategyId,
			Long benefitGroupId, List<Contribution> contributions,
			Map<String, Map<String, Object>> groupFundingDetails) {
		// calculate estimates:
		Map<String, BigDecimal> groupTotalContribution = new HashMap<>();
		for (Contribution contribution : contributions) {
			String planType = contribution.getBenefitPlanAssociation().getPlanType();
			BigDecimal totalContribution = groupTotalContribution.get(planType);
			if (null == totalContribution) {
				totalContribution = BigDecimal.ZERO;
			}
			
			Map<String, Object> coverageLevelFunding = getCoverageLevelFunding(groupFundingDetails, planType);
			String planTypeFunding = getPlanTypeFunding(coverageLevelFunding);
			int excessOption = getExcessOption(coverageLevelFunding, planTypeFunding);
			
			BigDecimal planCost = contribution.getEmployerContribution().add(contribution.getEmployeeContribution());
			if (planCost != null) {
				if (contribution.getEmployerPercent() != null) {
					BigDecimal contributionAmount = planCost.multiply(contribution.getEmployerPercent())
							.divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
					totalContribution = totalContribution
							.add(contributionAmount.multiply(new BigDecimal(contribution.getHeadCount())));
					// BSUPP calculation
					if (BSSApplicationConstants.BSUPP.equals(planTypeFunding)
							&& ExcessOptionEnum.OTHER.getType() != excessOption) {

						BigDecimal totalBsuppContribution = calculateTotalBsuppContribution(groupTotalContribution,
								contribution, coverageLevelFunding, contributionAmount);
						groupTotalContribution.put(BSSApplicationConstants.BSUPP, totalBsuppContribution);
					}

				} else if (contribution.getEmployerContribution() != null) {
					totalContribution = totalContribution.add(contribution.getEmployerContribution()
							.multiply(new BigDecimal(contribution.getHeadCount())));
				}
				groupTotalContribution.put(planType, totalContribution);
			}
		}
		return createStreategyEstimatesMap(groupTotalContribution, strategyId, benefitGroupId);
	}

	private static int getExcessOption(Map<String, Object> coverageLevelFunding, String planTypeFunding) {
		int excessOption = 0;
		
		if (null != coverageLevelFunding
				&& null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE) && BSSApplicationConstants.BSUPP.equals(planTypeFunding)) {
				excessOption = ((BigDecimal) coverageLevelFunding.get(BSSApplicationConstants.BSUPP_EXCESS_OPTION))
						.intValue();
		}
		return excessOption;
	}

	private static String getPlanTypeFunding(Map<String, Object> coverageLevelFunding) {
		String planTypeFunding = null;
		if (null != coverageLevelFunding
				&& null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE)) {
			planTypeFunding = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
		}
		return planTypeFunding;
	}

	private static BigDecimal calculateTotalBsuppContribution(Map<String, BigDecimal> groupTotalContribution,
			Contribution contribution, Map<String, Object> coverageLevelFunding, BigDecimal contributionAmount) {
		BigDecimal totalBsuppContribution = groupTotalContribution.get(BSSApplicationConstants.BSUPP);
		if (null == totalBsuppContribution) {
			totalBsuppContribution = BigDecimal.ZERO;
		}
		BigDecimal subtotalBsuppContribution = ((BigDecimal) coverageLevelFunding
				.get(CoverageCodesEnums.valueOfId(contribution.getCoverageLevel())))
						.subtract(contributionAmount);
		totalBsuppContribution = totalBsuppContribution
				.add(subtotalBsuppContribution.multiply(new BigDecimal(contribution.getHeadCount())));
		return totalBsuppContribution;
	}

	private static Map<String, Object> getCoverageLevelFunding(Map<String, Map<String, Object>> groupFundingDetails,
			String planType) {
		Map<String, Object> coverageLevelFunding = null;
		if (null != groupFundingDetails) {
			if (Constants.dentalPlanTypeList.contains(planType)) {
				coverageLevelFunding = groupFundingDetails.get(Constants.DENTAL_CODE);
			} else if (Constants.visionPlanTypeList.contains(planType)) {
				coverageLevelFunding = groupFundingDetails.get(Constants.VISION_CODE);
			} else {
				coverageLevelFunding = groupFundingDetails.get(Constants.MEDICAL_CODE);
			}
		}
		return coverageLevelFunding;
	}
	
	/**
	 * This method is for creating the estimates object.
	 * @param groupTotalContribution
	 * @param strategyId
	 * @param benefitGroupId
	 * @return
	 */
	public static Map<Long, List<StrategyEstimate>> createStreategyEstimatesMap(
			Map<String, BigDecimal> groupTotalContribution, long strategyId, long benefitGroupId) {
		Map<Long, List<StrategyEstimate>> strategyEstimateMap = new HashMap<>();
		List<StrategyEstimate> currentList = new ArrayList<>();
		for (Map.Entry<String, BigDecimal> entry: groupTotalContribution.entrySet()) {
			String planType = entry.getKey();
			StrategyEstimate strategyEstimate = new StrategyEstimate();
			strategyEstimate.setStrategyId(strategyId);
			strategyEstimate.setGroupId(benefitGroupId);
			if (BSSApplicationConstants.BSUPP.equals(planType)) {
				strategyEstimate.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
				strategyEstimate.setPlanSubType(planType);
			} else {
				strategyEstimate.setPlanType(planType);
			}
			strategyEstimate.setEstimate(entry.getValue());
			currentList.add(strategyEstimate);
		}
		strategyEstimateMap.put(strategyId, currentList);
		// Save the new data in the database
		return strategyEstimateMap;
	}
	
	/**
	 * This method gets a set of all of the plans the company currently offers
	 * 
	 * @param healthPlansMap
	 * @return
	 */
	public static Set<String> getCompanyPreviousPlanYearHealthPlans(
			Map<String, Map<String, BenefitPlan>> healthPlansMap,
			Map<String, PlanMapping> primaryPlanMapping) {

		Set<String> planSet = new HashSet<>();

		if (healthPlansMap != null) {
			for (Entry<String, Map<String, BenefitPlan>> planEntry : healthPlansMap.entrySet()) {
				Map<String, BenefitPlan> planMap = planEntry.getValue();
				for (String planId : planMap.keySet()) {
					planSet.add(planId);
				}
			}
		}
		if (primaryPlanMapping != null) {
			for (Entry<String, PlanMapping> planEntry : primaryPlanMapping.entrySet()) {
				PlanMapping planMap = planEntry.getValue();
				for (String planId : planMap.getNewBenefitPlans()) {
					planSet.add(planId);
				}
			}
		}
		return planSet;
	}

    /**
	 * Returns the minimum funding amount starting value.
	 * 
	 * If none of the selected carriers are included in the
	 * lowestCostPlanPerCarrier, the client must change carriers so return 0
	 * 
	 * @param lowestCostPlanPerCarrier
	 * @param selectedPlanCarriers
	 * @return
	 */
	private static BigDecimal getStartingMinimumFundingAmount(List<CarrierMinimumFunding> lowestCostPlanPerCarrier, Set<Long> selectedPlanCarriers) {
		
		BigDecimal minFunding = BigDecimal.valueOf(Long.MAX_VALUE);
		
		boolean foundCarrier = false;
		for (CarrierMinimumFunding carrierMinimumFunding : lowestCostPlanPerCarrier) {
			if (selectedPlanCarriers.contains(carrierMinimumFunding.getCarrierId())) {
				foundCarrier = true;
			}
		}
		if (!foundCarrier) {
			minFunding = BigDecimal.ZERO;
		}		
		return minFunding;
	}
	
}