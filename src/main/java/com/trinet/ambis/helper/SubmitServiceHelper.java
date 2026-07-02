/**
 * 
 */
package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.SubmitUtil;

/**
 * @author rvutukuri
 *
 */
public class SubmitServiceHelper {
	private static final Logger logger = LoggerFactory.getLogger(SubmitServiceHelper.class);
	
	private static final String WAIVER_ALLOWANCE = "waiverAllowance";
	private static final String DN_BASE_PCT = "dnBasePct";
	private static final String DN_BASE_COVRG = "dnBaseCovrg";
	private static final String VS_BASE_COVRG = "vsBaseCovrg";
	private static final String VS_BASE_PCT = "vsBasePct";
	private static final String MD_BASE_COVRG = "mdBaseCovrg";
	private static final String MD_BASE_PCT = "mdBasePct";
	
	private SubmitServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + SubmitServiceHelper.class.getName() + " cannot be instantiated.");
	}

	public static Map<String, Set<String>> getSelectedBenefitPlans(BenefitGroup group) {
		Map<String, Set<String>> planTypeSelectedPlans = new HashMap<>();
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		for (BenefitOffer benefitOffer : benefitOffers) {
			String planType = benefitOffer.getSummary().getType();
			if (isValidPlanType(planType)) {
				Set<String> selectedPlans = new HashSet<>();
				for (BenefitPlan bp : benefitOffer.getBenefitPlans()) {
					selectedPlans.add(bp.getId());
				}
				planTypeSelectedPlans.put(benefitOffer.getSummary().getDescription(), selectedPlans);
			}
		}
		return planTypeSelectedPlans;
	}
	
	private static boolean isValidPlanType(String planType) {
	    return BSSApplicationConstants.MEDICAL.equals(planType) ||
	           BSSApplicationConstants.DENTAL.equals(planType) ||
	           BSSApplicationConstants.VISION.equals(planType);
	}

	public static List<Contribution> getAllBenOffersContributions(BenefitGroup group) {
		List<Contribution> contributions = new ArrayList<>();
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		for (BenefitOffer benefitOffer : benefitOffers) {
			if (!benefitOffer.getSummary().getType().equals("additionalBenefit")) {
				for (BenefitPlan plan : benefitOffer.getBenefitPlans()) {
					if(null == plan.getPlanType()) {
						plan.setPlanType(benefitOffer.getSummary().getDescription());
					}
					for (PlanContribution planContribution : plan.getContributions()) {
						Contribution contribution = new Contribution();
						BigDecimal planCost = planContribution.getPlanCost();
						BigDecimal erContribution = planCost
								.multiply(planContribution.getEmployerPercent().divide(Constants.BigDecimal_100))
								.setScale(2, RoundingMode.HALF_UP);
						BigDecimal eeContribution = planCost.subtract(erContribution).setScale(2, RoundingMode.HALF_UP);
						contribution.setBenefitPlan(plan.getId());
						contribution.setBenefitPlanAssociation(plan);
						contribution.setOverrideType(planContribution.getOverrideType());
						contribution.setCoverageLevel(CoverageCodesEnums.codeFromId(planContribution.getType()));
						contribution.setEmployeeContribution(eeContribution);
						contribution.setEmployerContribution(erContribution);
						contribution.setEmployerPercent(planContribution.getEmployerPercent());
						contributions.add(contribution);
					}
				}
			}
		}
		return contributions;
	}

	/**
	 * 
	 * @param contributions
	 * @param fplPLansByRegion
	 * @param company
	 */
	public static void setFPLForLowCostPpoPlan(List<Contribution> contributions, Map<String, String> fplPLansByRegion,
			Company company, List<String> fplMedicalPlans) {
		logger.debug("Entering method : setFPLForLowCostPpoPlan");
		for (Map.Entry<String, String> entry : fplPLansByRegion.entrySet()) {
			String lowCostPpoPlan = entry.getValue();
			updateContributionsForPlan(contributions, company, lowCostPpoPlan);
		}
		if (CollectionUtils.isNotEmpty(fplMedicalPlans)) {
			for (String bp : fplMedicalPlans) {
				updateContributionsForPlan(contributions, company, bp);
			}
		}
		logger.debug("Exiting method : setFPLForLowCostPpoPlan");
	}

	private static void updateContributionsForPlan(List<Contribution> contributions, Company company,
			String lowCostPpoPlan) {
		for (Contribution contribution : contributions) {
			BigDecimal planCost = contribution.getEmployerContribution()
					.add(contribution.getEmployeeContribution());
			if (contribution.getBenefitPlanAssociation().getId().equals(lowCostPpoPlan)
					&& Constants.CVG_CODE_EMPLOYEE.equals(contribution.getCoverageLevel())
					&& company.getAleAmount().compareTo(contribution.getEmployeeContribution()) < 0) {
				contribution.setEmployeeContribution(company.getAleAmount());
				contribution.setEmployerContribution(planCost.subtract(company.getAleAmount()));
				contribution.setEmployerPercent(contribution.getEmployerContribution()
						.divide(planCost, 10, RoundingMode.CEILING).multiply(Constants.BigDecimal_100));
				contribution.setOverrideType(PlanOverrideServiceHelper.getFPLPlanOverrideType(contribution.getOverrideType()));
			}
		}
	}

	/**
	 * 
	 * @param benefitPlansByRegion
	 * @param benefitPlanContributions
	 * @return
	 */
	public static Map<String, String> findLowCostPpoPlanByRegion(Map<String, List<String>> benefitPlansByRegion,
			Map<String, List<Contribution>> benefitPlanContributions) {
		Map<String, String> fplPLansByRegion = new HashMap<>();
		if (MapUtils.isNotEmpty(benefitPlansByRegion)) {
			for (Map.Entry<String, List<String>> entry : benefitPlansByRegion.entrySet()) {
				BigDecimal lowestPlanCost = null;
				String region = entry.getKey();
				for (String bp : entry.getValue()) {
					List<Contribution> cbMap = benefitPlanContributions.get(bp);
					for (Contribution cb : cbMap) {
						if (BSSApplicationConstants.CVG_CODE_EMPLOYEE.equals(cb.getCoverageLevel())
								&& cb.getBenefitPlanAssociation().isPpoPlan()
								&& cb.getBenefitPlanAssociation().getId().equals(bp)) {
							if (null != fplPLansByRegion.get(region)) {
								BigDecimal planCost = cb.getEmployerContribution().add(cb.getEmployeeContribution());
								if (planCost.compareTo(lowestPlanCost) < 0) {
									fplPLansByRegion.put(region, cb.getBenefitPlanAssociation().getId());
									lowestPlanCost = planCost;
								}
							} else {
								fplPLansByRegion.put(region, cb.getBenefitPlanAssociation().getId());
								lowestPlanCost = cb.getEmployerContribution().add(cb.getEmployeeContribution());
							}
						}
					}
				}
			}
		}
		return fplPLansByRegion;
	}

	/**
	 * 
	 * @param contributions
	 * @return
	 */
	public static Map<String, List<Contribution>> createMapOfContributions(List<Contribution> contributions) {
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		for (Contribution cb : contributions) {
			if (null != benefitPlanContributions.get(cb.getBenefitPlanAssociation().getId())) {
				List<Contribution> cbList = benefitPlanContributions.get(cb.getBenefitPlanAssociation().getId());
				cbList.add(cb);
			} else {
				List<Contribution> cbList = new ArrayList<>();
				cbList.add(cb);
				benefitPlanContributions.put(cb.getBenefitPlanAssociation().getId(), cbList);
			}
		}
		return benefitPlanContributions;
	}

	/**
	 * 
	 * @param BenefitGroupStrategies
	 * @return
	 */
	public static List<BenefitGroup> updateBenefitGroupData(StrategyGroupService strategyGroupService,
			StrategyData strategy) {
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		Map<String, List<BenefitOffer>> benefitProgramOffers = new HashMap<>();
		for (StrategyBenefitGroup sbg : strategy.getBenefitGroups()) {
			benefitProgramOffers.put(sbg.getBenefitProgram(), sbg.getBenefitOffers());
		}
		List<BenefitGroupStrategy> benefitGroupStrategies = strategyGroupService
				.getBenefitGroupStrategy(strategy.getStrategySummary().getId(), BSSApplicationConstants.STATUS_ACTIVE);
		for (BenefitGroupStrategy bgs : benefitGroupStrategies) {
			BenefitGroup bg = bgs.getBenefitGroup();
			bg.setWaitingPeriod(bgs.getWaitingPeriod());
			bg.setHeadcount(bgs.getHeadcount());
			bg.setStatus(bgs.getStatus());
			bg.setDefaultGroup(bgs.isDefaultGroup());
			bg.setBenefitOffers(benefitProgramOffers.get(bg.getBenefitProgram()));
			benefitGroups.add(bg);
		}
		return benefitGroups;
	}

	/**
	 * 
	 * @param company
	 * @param benefitGroups
	 */
	public static void updateCompaniesBenefitProgram(Company company, List<BenefitGroup> benefitGroups) {
		for (BenefitGroup bg : benefitGroups) {
			if (bg.isDefaultGroup()) {
				company.setBenefitProgram(bg.getBenefitProgram());
				break;
			}
		}
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateMedicalFunding(Query query, PlanPackage planPkg) {
		String fundingType = planPkg.getFundingType() != null ? planPkg.getFundingType()
				: BSSApplicationConstants.EMPTY_SPACE;
		query.setParameter("mdFundingType", fundingType);
		String fundingBasePlan = planPkg.getFundingBasePlan() != null ? planPkg.getFundingBasePlan()
				: BSSApplicationConstants.EMPTY_SPACE;
		query.setParameter("mdBasePlan", fundingBasePlan);
		if( null == planPkg.getWaiverAllowance() ){
			query.setParameter(WAIVER_ALLOWANCE, BigDecimal.ZERO);
		} else {
			query.setParameter(WAIVER_ALLOWANCE, planPkg.getWaiverAllowance());
		}
		// adding flat max details
		updateFlatMaxMedicalFunding(query, planPkg.getCoverageLevelFundingFlatMax(), fundingBasePlan);

		if (StringUtils.isBlank(fundingType)) {
			updateEmptyMedicalFunding(query);
		} else if (BSSApplicationConstants.BFPCT.equals(planPkg.getFundingType())) {
			for (Map.Entry<String, BigDecimal> entry : planPkg.getCoverageLevelFunding().entrySet()) {
				String fundingBaseCoverage = entry.getKey() != null ? SubmitUtil.getCoverageCode(entry.getKey())
						: BSSApplicationConstants.EMPTY_SPACE;
				query.setParameter(MD_BASE_COVRG, fundingBaseCoverage);
				query.setParameter(MD_BASE_PCT, entry.getValue());
			}
			updateRegularMedicalFunding(query, null, fundingType);
		} else {
			updateRegularMedicalFunding(query, planPkg.getCoverageLevelFunding(), fundingType);
		}
	}

	/**
	 * 
	 * @param query
	 * @param coverageLevelFundingFlatMax
	 * @param fundingBasePlan
	 */
	public static void updateFlatMaxMedicalFunding(Query query, Map<String, BigDecimal> coverageLevelFundingFlatMax,
			String fundingBasePlan) {
		if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
			query.setParameter("mdFlatMaxEE", coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			query.setParameter("mdFlatMaxSp",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
			query.setParameter("mdFlatMaxDep",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
			query.setParameter("mdFlatMaxFam",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		} else {
			query.setParameter("mdFlatMaxEE", BigDecimal.ZERO);
			query.setParameter("mdFlatMaxSp", BigDecimal.ZERO);
			query.setParameter("mdFlatMaxDep", BigDecimal.ZERO);
			query.setParameter("mdFlatMaxFam", BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 * @param coverageFundingMap
	 */
	public static void updateRegularMedicalFunding(Query query, Map<String, BigDecimal> coverageLevelFunding,
			String fundingType) {
		if (null != coverageLevelFunding && !coverageLevelFunding.isEmpty()) {
			if (BSSApplicationConstants.BSUPP.equals(fundingType)) {
				updateBenSuppFunding(query, coverageLevelFunding);
				updateBenFunding(query, null);
			} else {
				updateBenFunding(query, coverageLevelFunding);
				updateBenSuppFunding(query, null);
			}
		} else {
			updateBenFunding(query, null);
			updateBenSuppFunding(query, null);
		}
		if (!BSSApplicationConstants.BFPCT.equals(fundingType)) {
			query.setParameter(MD_BASE_COVRG, BSSApplicationConstants.EMPTY_SPACE);
			query.setParameter(MD_BASE_PCT, BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 * @param coverageLevelFunding
	 */
	public static void updateBenFunding(Query query, Map<String, BigDecimal> coverageLevelFunding) {
		if (null != coverageLevelFunding && !coverageLevelFunding.isEmpty()) {
			query.setParameter("mdEe", coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			query.setParameter("mdSp", coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
			query.setParameter("mdDep", coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
			query.setParameter("mdFam", coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
			updateBenSuppFunding(query, null);
		} else {
			query.setParameter("mdEe", BigDecimal.ZERO);
			query.setParameter("mdSp", BigDecimal.ZERO);
			query.setParameter("mdDep", BigDecimal.ZERO);
			query.setParameter("mdFam", BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 * @param benSupCoverageLevelFunding
	 */
	public static void updateBenSuppFunding(Query query, Map<String, BigDecimal> benSupCoverageLevelFunding) {
		if (null != benSupCoverageLevelFunding && !benSupCoverageLevelFunding.isEmpty()) {
			query.setParameter("benSuppEE", benSupCoverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			query.setParameter("benSuppSp",
					benSupCoverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
			query.setParameter("benSuppDep",
					benSupCoverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
			query.setParameter("benSuppFam",
					benSupCoverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		} else {
			query.setParameter("benSuppEE", BigDecimal.ZERO);
			query.setParameter("benSuppSp", BigDecimal.ZERO);
			query.setParameter("benSuppDep", BigDecimal.ZERO);
			query.setParameter("benSuppFam", BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateEmptyMedicalFunding(Query query) {
		query.setParameter("mdFundingType", BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter("mdBasePlan", BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(MD_BASE_COVRG, BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(MD_BASE_PCT, BigDecimal.ZERO);
		query.setParameter(WAIVER_ALLOWANCE, BigDecimal.ZERO);

		// regular medical
		updateRegularMedicalFunding(query, null, BSSApplicationConstants.EMPTY_SPACE);

		// flat max medical
		updateFlatMaxMedicalFunding(query, null, null);

		// benefit sup medical
		updateBenSuppFunding(query, null);
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateDentalFunding(Query query, PlanPackage planPkg, boolean employeePaid) {
		String fundingType = planPkg.getFundingType() != null ? planPkg.getFundingType()
				: BSSApplicationConstants.EMPTY_SPACE;
		query.setParameter("dnFundingType", fundingType);
		String fundingBasePlan = planPkg.getFundingBasePlan() != null ? planPkg.getFundingBasePlan()
				: BSSApplicationConstants.EMPTY_SPACE;
		query.setParameter("dnBasePlan", fundingBasePlan);

		// FLAT MAX FUNDING
		updateFlatMaxDentalFunding(query, planPkg.getCoverageLevelFundingFlatMax(), fundingBasePlan);

		if (employeePaid) {
			updateEmptyDentalFunding(query, BSSApplicationConstants.Y, BSSApplicationConstants.N);
		} else {
			query.setParameter("dnNotOffered", BSSApplicationConstants.N);
			query.setParameter("odNotOffered", BSSApplicationConstants.Y);
			if (StringUtils.isBlank(fundingType)) {
				updateEmptyDentalFunding(query, BSSApplicationConstants.N, BSSApplicationConstants.Y);
			} else if (BSSApplicationConstants.BFPCT.equals(planPkg.getFundingType())) {
				Map<String, BigDecimal> coverageFundingMap = planPkg.getCoverageLevelFunding();
				for (Map.Entry<String, BigDecimal> entry : coverageFundingMap.entrySet()) {
					String fundingBaseCoverage = entry.getKey() != null ? SubmitUtil.getCoverageCode(entry.getKey())
							: BSSApplicationConstants.EMPTY_SPACE;
					query.setParameter(DN_BASE_COVRG, fundingBaseCoverage);
					query.setParameter(DN_BASE_PCT, entry.getValue());
				}
				updateRegularDentalFunding(query, null, fundingType);
			} else {
				updateRegularDentalFunding(query, planPkg.getCoverageLevelFunding(), fundingType);
			}
		}
	}

	/**
	 * 
	 * @param query
	 * @param coverageLevelFundingFlatMax
	 * @param fundingBasePlan
	 */
	public static void updateFlatMaxDentalFunding(Query query, Map<String, BigDecimal> coverageLevelFundingFlatMax,
			String fundingBasePlan) {
		if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
			query.setParameter("dnFlatMaxEE", coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			query.setParameter("dnFlatMaxSp",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
			query.setParameter("dnFlatMaxDep",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
			query.setParameter("dnFlatMaxFam",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		} else {
			query.setParameter("dnFlatMaxEE", BigDecimal.ZERO);
			query.setParameter("dnFlatMaxSp", BigDecimal.ZERO);
			query.setParameter("dnFlatMaxDep", BigDecimal.ZERO);
			query.setParameter("dnFlatMaxFam", BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 * @param coverageFundingMap
	 */
	public static void updateRegularDentalFunding(Query query, Map<String, BigDecimal> coverageFundingMap,
			String fundingType) {
		if (null != coverageFundingMap && !coverageFundingMap.isEmpty()) {
			query.setParameter("dnEe", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			query.setParameter("dnSp", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
			query.setParameter("dnDep", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
			query.setParameter("dnFam", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		} else {
			query.setParameter("dnEe", BigDecimal.ZERO);
			query.setParameter("dnSp", BigDecimal.ZERO);
			query.setParameter("dnDep", BigDecimal.ZERO);
			query.setParameter("dnFam", BigDecimal.ZERO);
		}
		if (!BSSApplicationConstants.BFPCT.equals(fundingType)) {
			query.setParameter(DN_BASE_COVRG, BSSApplicationConstants.EMPTY_SPACE);
			query.setParameter(DN_BASE_PCT, BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateEmptyDentalFunding(Query query, String dentalNotOffered, String volDentalNotOffered) {
		query.setParameter("dnNotOffered", dentalNotOffered);
		query.setParameter("odNotOffered", volDentalNotOffered);
		query.setParameter("dnFundingType", BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter("dnBasePlan", BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(DN_BASE_COVRG, BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(DN_BASE_PCT, BigDecimal.ZERO);

		// regular funding
		updateRegularDentalFunding(query, null, BSSApplicationConstants.EMPTY_SPACE);
		// flat max funding
		updateFlatMaxDentalFunding(query, null, null);
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateVisionFunding(Query query, PlanPackage planPkg, boolean employeePaid) {
		String fundingType = planPkg.getFundingType() != null ? planPkg.getFundingType()
				: BSSApplicationConstants.EMPTY_SPACE;
		query.setParameter("vsFundingType", fundingType);
		String fundingBasePlan = planPkg.getFundingBasePlan() != null ? planPkg.getFundingBasePlan()
				: BSSApplicationConstants.EMPTY_SPACE;
		query.setParameter("vsBasePlan", fundingBasePlan);

		// FLAT MAX FUNDING
		updateFlatMaxVisionFunding(query, planPkg.getCoverageLevelFundingFlatMax(), fundingBasePlan);
		if (employeePaid) {
			updateEmptyVisionFunding(query, BSSApplicationConstants.Y, BSSApplicationConstants.N);
		} else {
			query.setParameter("vsNotOffered", BSSApplicationConstants.N);
			query.setParameter("ovNotOffered", BSSApplicationConstants.Y);
			if (StringUtils.isBlank(fundingType)) {
				updateEmptyVisionFunding(query, BSSApplicationConstants.N, BSSApplicationConstants.Y);
			} else if (BSSApplicationConstants.BFPCT.equals(planPkg.getFundingType())) {
				Map<String, BigDecimal> coverageFundingMap = planPkg.getCoverageLevelFunding();
				for (Map.Entry<String, BigDecimal> entry : coverageFundingMap.entrySet()) {
					String fundingBaseCoverage = entry.getKey() != null ? SubmitUtil.getCoverageCode(entry.getKey())
							: BSSApplicationConstants.EMPTY_SPACE;
					query.setParameter(VS_BASE_COVRG, fundingBaseCoverage);
					query.setParameter(VS_BASE_PCT, entry.getValue());
				}
				updateRegularVisionFunding(query, null, fundingType);
			} else {
				updateRegularVisionFunding(query, planPkg.getCoverageLevelFunding(), fundingType);
			}
		}
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateFlatMaxVisionFunding(Query query, Map<String, BigDecimal> coverageLevelFundingFlatMax,
			String fundingBasePlan) {
		if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
			query.setParameter("vsFlatMaxEE", coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			query.setParameter("vsFlatMaxSp",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
			query.setParameter("vsFlatMaxDep",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
			query.setParameter("vsFlatMaxFam",
					coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		} else {
			query.setParameter("vsFlatMaxEE", BigDecimal.ZERO);
			query.setParameter("vsFlatMaxSp", BigDecimal.ZERO);
			query.setParameter("vsFlatMaxDep", BigDecimal.ZERO);
			query.setParameter("vsFlatMaxFam", BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateRegularVisionFunding(Query query, Map<String, BigDecimal> coverageFundingMap,
			String fundingType) {
		if (null != coverageFundingMap && !coverageFundingMap.isEmpty()) {
			query.setParameter("vsEe", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			query.setParameter("vsSp", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
			query.setParameter("vsDep", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
			query.setParameter("vsFam", coverageFundingMap.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		} else {
			query.setParameter("vsEe", BigDecimal.ZERO);
			query.setParameter("vsSp", BigDecimal.ZERO);
			query.setParameter("vsDep", BigDecimal.ZERO);
			query.setParameter("vsFam", BigDecimal.ZERO);
		}
		if (!BSSApplicationConstants.BFPCT.equals(fundingType)) {
			query.setParameter(VS_BASE_COVRG, BSSApplicationConstants.EMPTY_SPACE);
			query.setParameter(VS_BASE_PCT, BigDecimal.ZERO);
		}
	}

	/**
	 * 
	 * @param query
	 */
	public static void updateEmptyVisionFunding(Query query, String vsNotOffered, String ovNotOffered) {
		query.setParameter("vsNotOffered", vsNotOffered);
		query.setParameter("ovNotOffered", ovNotOffered);
		query.setParameter("vsFundingType", BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter("vsBasePlan", BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(VS_BASE_COVRG, BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(VS_BASE_PCT, BigDecimal.ZERO);

		// regular funding
		updateRegularVisionFunding(query, null, BSSApplicationConstants.EMPTY_SPACE);
		// flat max funding
		updateFlatMaxVisionFunding(query, null, null);
	}

	/**
	 * 
	 * @param query
	 * @param benefitOffer
	 * @param sdNotOffered
	 * @param ldNotOffered
	 * @param cmNotOffered
	 * @param lfNotOffered
	 */
	public static void updateAdditionalFunding(Query query, BenefitOffer benefitOffer) {
		boolean lfNotOffered = true;
		boolean cmNotOffered = true;
		boolean disabilityNotOffered = true;
		List<AdditionalBenefitOffer> additionalBenefits = null;
		if (null != benefitOffer) {
			// Get plans from option ID
			additionalBenefits = new ArrayList<>(benefitOffer.getAdditionalBenefitOffers());
			for (AdditionalBenefitOffer abDTOImpl : additionalBenefits) {
				BenefitOfferSummary summary1 = abDTOImpl.getSummary();
				if (summary1.getType().equals("DISABILITY")) {
					disabilityNotOffered = false;
					updateDisabilityOffered(abDTOImpl, query);
				} else if (summary1.getType().equals("CMTR")) {
					query.setParameter("cmNotOffered", BSSApplicationConstants.N);
					cmNotOffered = false;
				} else if (summary1.getType().equals("LIFE")) {
					query.setParameter("lfNotOffered", BSSApplicationConstants.N);
					lfNotOffered = false;
				}
			}
		}
		if (disabilityNotOffered) {
			setDisabilityNotOffered(true, true, query);
		}

		if (cmNotOffered) {
			query.setParameter("cmNotOffered", BSSApplicationConstants.Y);
		}
		if (lfNotOffered) {
			query.setParameter("lfNotOffered", BSSApplicationConstants.Y);
		}
	}

	public static void updateDisabilityOffered(AdditionalBenefitOffer abDTOImpl, Query query) {
		boolean sdNotOffered = true;
		boolean ldNotOffered = true;
		// Get option plans
		List<AdditionalBenefitPlan> planOptions = abDTOImpl.getAdditionalBenefitPlans();
		for (AdditionalBenefitPlan option : planOptions) {
			List<DisabilityBenefitOptionPlans> plans = option.getOptionPlans();
			for (DisabilityBenefitOptionPlans plan : plans) {
				if (plan.getPlanType().equals(Constants.STD_CODE)) {
					String offerEmployeePaidStd = plan.isEmployeePaid() ? BSSApplicationConstants.Y
							: BSSApplicationConstants.N;
					query.setParameter("offerEESTD", offerEmployeePaidStd);
					query.setParameter("sdNotOffered", BSSApplicationConstants.N);
					sdNotOffered = false;
				} else if (plan.getPlanType().equals(Constants.LTD_CODE)) {
					String offerEmployeePaidLtd = plan.isEmployeePaid() ? BSSApplicationConstants.Y
							: BSSApplicationConstants.N;
					query.setParameter("offerEELTD", offerEmployeePaidLtd);
					query.setParameter("ldNotOffered", BSSApplicationConstants.N);
					ldNotOffered = false;
				}
			}
		}
		setDisabilityNotOffered(sdNotOffered, ldNotOffered, query);

	}

	public static void setDisabilityNotOffered(boolean sdNotOffered, boolean ldNotOffered, Query query) {
		if (sdNotOffered) {
			query.setParameter("sdNotOffered", BSSApplicationConstants.Y);
			query.setParameter("offerEESTD", BSSApplicationConstants.N);
		}
		if (ldNotOffered) {
			query.setParameter("ldNotOffered", BSSApplicationConstants.Y);
			query.setParameter("offerEELTD", BSSApplicationConstants.N);
		}
	}

	public static String generateConfirmationStmtPdfName(String confirmationId) {
		return BSSApplicationConstants.CONFIRMATION_STMT_DOC_TYPE.concat("_").concat(confirmationId);
	}
}
