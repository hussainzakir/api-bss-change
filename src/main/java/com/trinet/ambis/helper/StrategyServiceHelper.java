package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.enums.CacheObjectTypeEnum;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.FundingBasePlan;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Constants;

/**
 * @author rvutukuri
 *
 */
public class StrategyServiceHelper {
	private static final Logger logger = LoggerFactory.getLogger(StrategyServiceHelper.class);
	
	private static final String LOGGER_ENTERING_METHOD = "Entering method : constructContribution";

	private StrategyServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + StrategyServiceHelper.class.getName() + " can not be instantiated.");
	}

	/**
	 * This method is for constructing Strategy object for a renewal company.
	 * 
	 * @param company
	 * @param strategyName
	 * @return
	 */
	public static Strategy constructStrategyForRenewals(Company company, String strategyName, String costShareType, String status, int acaFplOpted) {
		logger.debug("Entering method : constructStrategyForRenewals");
		Strategy strategy = new Strategy();
		strategy.setName(strategyName);
		if (strategyName.contains(StrategyTypesEnums.F_S.getName())) {
			strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		} else {
			strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_CUSTOM_RECOMMENDED);
		}
		strategy.setCostShareType(costShareType);
		strategy.setComments("");
		strategy.setSubmitted(false);
		strategy.setSubmitDate(null);
		strategy.setEstimatedTotalCost(BigDecimal.ZERO);
		strategy.setCurrentYearTotalCost(BigDecimal.ZERO);
		strategy.setCompanyId(company.getId());
		strategy.setTotalBudget(BigDecimal.ZERO);
		strategy.setHeadCount(0L);
		strategy.setBudgetFactor(1);
		strategy.setStatus(status);
		strategy.setAcaFplOpted(acaFplOpted);
		logger.debug("Exiting method : constructStrategyForRenewals");
		return strategy;

	}

	/**
	 * This method is for constructing current strategy.
	 * 
	 * @param company
	 * @param strategyName
	 * @return
	 */
	public static Strategy constructStrategyForCurrent(Company company, String strategyName) {
		logger.debug("Entering method : constructStrategyForCurrent");
		Strategy strategy = new Strategy();
		strategy.setName(strategyName);
		strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		strategy.setComments("");
		strategy.setSubmitted(true);
		strategy.setSubmitDate(null);
		strategy.setEstimatedTotalCost(BigDecimal.ZERO);
		strategy.setCurrentYearTotalCost(BigDecimal.ZERO);
		strategy.setCompanyId(company.getId());
		strategy.setTotalBudget(BigDecimal.ZERO);
		strategy.setHeadCount(0L);
		strategy.setBudgetFactor(1);
		strategy.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		logger.debug("Exiting method : constructStrategyForCurrent");
		return strategy;

	}
	/**
	 * 
	 * @param company
	 * @param strategyName
	 * @return
	 */
	public static Strategy constructStrategyForProspect(Company company, String strategyName) {
		logger.debug("Entering method : constructStrategyForProspect");
		Strategy strategy = new Strategy();
		strategy.setName(strategyName);
		strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		strategy.setComments("");
		strategy.setSubmitted(false);
		strategy.setSubmitDate(null);
		strategy.setEstimatedTotalCost(BigDecimal.ZERO);
		strategy.setCurrentYearTotalCost(BigDecimal.ZERO);
		strategy.setCompanyId(company.getId());
		strategy.setTotalBudget(BigDecimal.ZERO);
		strategy.setHeadCount(0L);
		strategy.setBudgetFactor(1);
		strategy.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
		strategy.setCostShareType(StrategyTypesEnums.F_S.getValue());
		logger.debug("Exiting method : constructStrategyForProspect");
		return strategy;

	}

	/**
	 * This method is for constructing PlanSelection object.
	 * 
	 * @param strategyId
	 * @param benefitGroupId
	 * @param bp
	 * @param headCount
	 * @return
	 */
	public static PlanSelection constructPlanSelection(long strategyId, long benefitGroupId, BenefitPlan bp,
			long headCount) {
		logger.debug("Entering method : constructPlanSelection");
		PlanSelection planSelection = new PlanSelection();
		planSelection.setId(0L);
		planSelection.setBenefitPlan(bp.getId());
		planSelection.setStrategyId(strategyId);
		planSelection.setGroupId(benefitGroupId);
		planSelection.setPlanType(bp.getPlanType());
		planSelection.setHeadCount(headCount);
		planSelection.setPpoPlan(bp.isPpoPlan());
		planSelection.setHighDeductiblePlan(bp.isHighDeductible());
		logger.debug("Exiting method : constructPlanSelection");
		return planSelection;
	}

	/**
	 * This method is for constructing ADPlanSelection object.
	 * 
	 * @param strategyId
	 * @param benefitGroupId
	 * @param planId
	 * @param planType
	 * @param headCount
	 * @return
	 */
	public static PlanSelection constructADPlanSelection(long strategyId, long benefitGroupId, String planId,
			String planType, long headCount) {
		logger.debug("Entering method : constructADPlanSelection");
		PlanSelection planSelection = new PlanSelection();
		planSelection.setId(0L);
		planSelection.setBenefitPlan(planId);
		planSelection.setStrategyId(strategyId);
		planSelection.setGroupId(benefitGroupId);
		planSelection.setPlanType(planType);
		planSelection.setHeadCount(headCount);
		logger.debug("Exiting method : constructADPlanSelection");
		return planSelection;
	}

	/**
	 * This method is for constructing Contribution object.
	 * 
	 * @param coverageCode
	 * @param headCount
	 * @param planSelectionId
	 * @param newEmployerContribution
	 * @param newEmployeeContribution
	 * @param newEmployerPercent
	 * @return
	 */
	public static Contribution constructContribution(String coverageCode, int headCount, int hsaHeadCount, Long planSelectionId,
			BigDecimal newEmployerContribution, BigDecimal newEmployeeContribution, BigDecimal newEmployerPercent) {
		logger.debug(LOGGER_ENTERING_METHOD);
		Contribution contribution = new Contribution();
		if (null != planSelectionId) {
			contribution.setPlanSelectionId(planSelectionId);
		}
		contribution.setHeadCount(headCount);
		contribution.setHsaHeadCount(hsaHeadCount);
		contribution.setEmployerContribution(newEmployerContribution);
		contribution.setEmployeeContribution(newEmployeeContribution);
		contribution.setEmployerPercent(newEmployerPercent);
		contribution.setCoverageLevel(coverageCode);
		logger.debug(LOGGER_ENTERING_METHOD);
		return contribution;
	}

	/**
	 * 
	 * @param pc
	 * @return
	 */
	public static Contribution constructContribution(PlanContribution pc) {
		logger.debug(LOGGER_ENTERING_METHOD);
		Contribution contribution = new Contribution();
		contribution.setId(pc.getId());
		contribution.setHeadCount(pc.getHeadcount());
		contribution.setHsaHeadCount(pc.getHsaHeadcount());
		contribution.setEmployerContribution(pc.getEmployerContribution());
		contribution.setEmployeeContribution(pc.getEmployeeContribution());
		contribution.setEmployerPercent(pc.getEmployerPercent());
		contribution.setCoverageLevel(CoverageCodesEnums.valueOfCode(pc.getType()));
		contribution.setOverrideType(pc.getOverrideType());
		logger.debug(LOGGER_ENTERING_METHOD);
		return contribution;
	}

	/**
	 * This method is for constructing benefit Plan.
	 * 
	 * @param benefitPlanId
	 * @param planType
	 * @param vendorId
	 * @return
	 */
	public static BenefitPlan constructBenefitPlan(String benefitPlanId, String planType, String vendorId) {
		BenefitPlan bp = new BenefitPlan();
		bp.setId(benefitPlanId);
		bp.setPlanType(planType);
		bp.setVendorId(vendorId);
		return bp;
	}

	/**
	 * This method is for constructing company object for a renewal company.
	 * 
	 * @param company
	 * @return
	 */
	public static Company constructXbssCompany(Company company, Long realmPlanYearId) {
		logger.debug("Entering method : constructXbssCompany");
		Company lastPlanYearCompany = new Company();
		lastPlanYearCompany.setCode(company.getCode());
		lastPlanYearCompany.setRealmPlanYearId(realmPlanYearId);
		lastPlanYearCompany.setDescription(company.getDescription());
		lastPlanYearCompany.setName(company.getName());
		logger.debug("Exiting method : constructXbssCompany");
		return lastPlanYearCompany;
	}

	/**
	 * 
	 * @param disabilityOptionsMap
	 * @return
	 */
	public static Map<String, List<String>> constructOptionPlansMap(
			Map<String, AdditionalBenefitPlan> disabilityOptionsMap) {
		Map<String, List<String>> optionBenefitPlanList = new HashMap<>();
		for (Map.Entry<String, AdditionalBenefitPlan> entry : disabilityOptionsMap.entrySet()) {
			List<String> planIds = new ArrayList<>();
			for (DisabilityBenefitOptionPlans dbop : entry.getValue().getOptionPlans()) {
				planIds.add(dbop.getId());
			}
			optionBenefitPlanList.put(entry.getKey(), planIds);
		}
		return optionBenefitPlanList;
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public static void createUpdateContribution(Company company, BenefitPlan benefitPlan, PlanSelection planSelection,
			List<Contribution> contribList) {
		List<PlanContribution> planContributions = benefitPlan.getContributions();
		for (PlanContribution planContribution : planContributions) {
			String coverageLevel = planContribution.getType();
			int headCount = planContribution.getHeadcount();
			int hsaHeadcount = planContribution.getHsaHeadcount();
			String coverageCode = CoverageCodesEnums.valueOfCode(coverageLevel);
			BigDecimal employerPercent = planContribution.getEmployerPercent();
			BigDecimal employerContribution = planContribution.getEmployerContribution() != null
					? planContribution.getEmployerContribution() : BigDecimal.ZERO;
			BigDecimal employeeContribution = planContribution.getEmployeeContribution() != null
					? planContribution.getEmployeeContribution() : BigDecimal.ZERO;
			Contribution contribution = constructContribution(coverageCode, headCount, hsaHeadcount, planSelection.getId(),
					employerContribution, employeeContribution, employerPercent);
			contribution.setOverrideType(planContribution.getOverrideType());
			contribList.add(contribution);
		}
	}

	public static StrategyBenefitGroup getBenefitGroupByCompare(List<StrategyBenefitGroup> dtoGroups,
			BenefitGroup benefitGroup) {
		StrategyBenefitGroup matchedBenefitGroup = null;
		for (StrategyBenefitGroup dtoGroup : dtoGroups) {
			if (null != dtoGroup.getBenefitProgram()) {
				if (dtoGroup.getName().equals(benefitGroup.getName())
						&& dtoGroup.getType().equals(benefitGroup.getType())
						&& dtoGroup.getBenefitProgram().equals(benefitGroup.getBenefitProgram())) {
					matchedBenefitGroup = dtoGroup;
				}
			} else {
				if (dtoGroup.getName().equals(benefitGroup.getName())
						&& dtoGroup.getType().equals(benefitGroup.getType())) {
					matchedBenefitGroup = dtoGroup;
				}
			}
		}
		return matchedBenefitGroup;
	}

	public static StrategyBenefitGroup getBenefitGroupById(List<StrategyBenefitGroup> dtoGroups, long id) {
		for (StrategyBenefitGroup dtoGroup : dtoGroups) {
			if (dtoGroup.getId() == id) {
				return dtoGroup;
			}
		}
		return null;
	}

	public static PlanSelection getPlanSelection(List<PlanSelection> planSelections, String plan) {
		for (PlanSelection ps : planSelections) {
			if (ps.getBenefitPlan().equals(plan)) {
				return ps;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param strategyId
	 * @param benefitGroup
	 * @return
	 */
	public static StrategyBenefitGroup constructBenefitGroup(long strategyId,
			BenefitGroupStrategy benefitGroupStrategy) {
		StrategyBenefitGroup strategyBenefitGroup = new StrategyBenefitGroup();
		strategyBenefitGroup.setStrategyId(strategyId);
		strategyBenefitGroup.setBenefitProgram(benefitGroupStrategy.getBenefitGroup().getBenefitProgram());
		strategyBenefitGroup.setCompanyId(benefitGroupStrategy.getBenefitGroup().getCompanyId());
		strategyBenefitGroup.setDefaultGroup(benefitGroupStrategy.isDefaultGroup());
		strategyBenefitGroup.setEstimatedTotalCost(benefitGroupStrategy.getBenefitGroup().getEstimatedTotalCost());
		strategyBenefitGroup.setHeadcount(benefitGroupStrategy.getHeadcount());
		strategyBenefitGroup.setId(benefitGroupStrategy.getBenefitGroup().getId());
		strategyBenefitGroup.setName(benefitGroupStrategy.getBenefitGroup().getName());
		strategyBenefitGroup.setPercentChange(benefitGroupStrategy.getBenefitGroup().getPercentChange());
		strategyBenefitGroup.setType(benefitGroupStrategy.getBenefitGroup().getType());
		strategyBenefitGroup.setWaitingPeriod(benefitGroupStrategy.getWaitingPeriod());
		strategyBenefitGroup.setStatus(benefitGroupStrategy.getStatus());
		strategyBenefitGroup.setStrategyGroupId(benefitGroupStrategy.getId());
		strategyBenefitGroup.setState(benefitGroupStrategy.getBenefitGroup().getState());
		return strategyBenefitGroup;
	}

	public static List<Long> getPlanSelectionIds(Long groupId,
			Map<String, Map<Long, List<PlanSelection>>> benefitOfferPlans) {
		List<Long> planIdList = new ArrayList<>();
		for (Map.Entry<String, Map<Long, List<PlanSelection>>> entry : benefitOfferPlans.entrySet()) {
			Map<Long, List<PlanSelection>> planMap = entry.getValue();
			List<PlanSelection> list = planMap.get(groupId);
			if (null != list && !list.isEmpty()) {
				for (PlanSelection ps : list) {
					planIdList.add(ps.getId());
				}
			}
		}
		return planIdList;
	}

	/**
	 * This method returns Set of company & employee locations for the given company
	 * + mandatory locations for given realmYearId .
	 * 
	 * @param company
	 * @return
	 */
	public static Set<String> getLocations(Company company) {
		Set<String> regions = new HashSet<>();
		regions.addAll(company.getCompanyRegions());
		regions.addAll(company.getFundingRegions());
		regions.addAll(company.getEmployeeRegions());
		return regions;
	}

	/**
	 * This method is for getting regions for benefit offer rules.
	 * 
	 * @param company
	 * @return
	 */
	public static Set<String> getHqStateCity(Company company) {
		Set<String> locations = new HashSet<>();
		locations.add(company.getHeadQuatersState());
		locations.add(company.getHeadQuatersCity());
		return locations;
	}

	/**
	 * 
	 * @param planSelections
	 * @param benefitPlansStatesMap
	 */
	public static void updatePlanSelectionRegions(List<PlanSelection> planSelections,
			Map<String, List<String>> benefitPlansStatesMap) {
		for (PlanSelection planSelection : planSelections) {
			String benefitPlan = planSelection.getBenefitPlan();
			if (benefitPlansStatesMap.containsKey(benefitPlan)) {
				planSelection.setListOfStates(benefitPlansStatesMap.get(benefitPlan));
			} else {
				List<String> listOfStates = new ArrayList<>();
				listOfStates.add(Constants.ALL_STATES);
				planSelection.setListOfStates(listOfStates);
			}
		}
	}

	/**
	 * 
	 * @param stateBenefitPlans
	 * @param benefitPlansStatesMap
	 */
	public static void updateBenfitPlanRegions(Set<StateBenefitPlan> stateBenefitPlans,
			Map<String, List<String>> benefitPlansStatesMap) {
		for (StateBenefitPlan stateBenefitPlan : stateBenefitPlans) {
			String benefitPlan = stateBenefitPlan.getBenefitPlan();
			if (benefitPlansStatesMap.containsKey(benefitPlan)) {
				stateBenefitPlan.setOfferedStates(benefitPlansStatesMap.get(benefitPlan));
			} else {
				List<String> listOfStates = new ArrayList<>();
				listOfStates.add(Constants.ALL_STATES);
				stateBenefitPlan.setOfferedStates(listOfStates);
			}
		}
	}

	/**
	 * This method is for calculating estimated cost for strategy by benefit
	 * offer.
	 * 
	 * @param strategyDataList
	 * @return
	 */
	public static Map<Long, List<StrategyEstimate>> calcStrategyEstimate(List<StrategyData> strategyDataList, Company company) {
		MultiKeyMap summaryWorkingMap = new MultiKeyMap();
		BigDecimal totalContribution = null;
		BigDecimal totalBsuppContribution = null;
		boolean offersHdhpPlan = false;
		BigDecimal totalHsaContribution = null;
		String planType = null;
		Map<String, BigDecimal> bsuppCoverageLevelFunding = null;
		boolean offersBsupp = false;
		if (strategyDataList != null) {
			for (StrategyData strategyData : strategyDataList) {
				long strategyId = strategyData.getStrategySummary().getId();

				// Determine if there is an HSA Contribution
				StrategyHsaFundingDto strategyHsaFunding = strategyData.getStrategyHsaFunding();
				
				if (strategyData.getBenefitGroups() != null) {
					for (StrategyBenefitGroup benefitGroup : strategyData.getBenefitGroups()) {
						long groupId = benefitGroup.getId();
						int hsaEmployeeHeadcount = 0;
						int hsaFamilyHeadcount = 0;
						if (benefitGroup.getBenefitOffers() != null) {
							for (BenefitOffer benefitOffer : benefitGroup.getBenefitOffers()) {
								
								// benefitPlans is where Medical/Dental/Vision
								// are stored
								if (benefitOffer.getBenefitPlans() != null && !benefitOffer.getBenefitPlans().isEmpty()) {
									
									bsuppCoverageLevelFunding = new HashMap<>();
									offersBsupp = false;
									offersHdhpPlan = false;
									
									// Determine if there is a BSUPP and the amount(s)
									if (benefitOffer.getPlanPackage() != null && BSSApplicationConstants.BSUPP.equals(benefitOffer.getPlanPackage().getFundingType())) {
										offersBsupp = true;
										if (ExcessOptionEnum.OTHER.getType() != benefitOffer.getPlanPackage().getBsuppExcessOption().intValue()) {
											bsuppCoverageLevelFunding = benefitOffer.getPlanPackage().getCoverageLevelFunding();
										}
									}

									planType = benefitOffer.getSummary().getDescription();
									// Set the totalContribution to 0
									totalContribution = new BigDecimal(0);
									totalBsuppContribution = new BigDecimal(0);
									for (BenefitPlan benefitPlan : benefitOffer.getBenefitPlans()) {
										if (benefitPlan.isHighDeductible()) {
											offersHdhpPlan = true;
										}
										if (benefitPlan.getContributions() != null) {
											for (PlanContribution contribution : benefitPlan.getContributions()) {
												if (benefitPlan.isHighDeductible()) {
													if (contribution.getType().equals(CoverageCodesEnums.COV_EMPLOYEE.getId())) {
														hsaEmployeeHeadcount = hsaEmployeeHeadcount + contribution.getHsaHeadcount();
													}
													else {
														hsaFamilyHeadcount = hsaFamilyHeadcount + contribution.getHsaHeadcount();
													}
												}
												
												if (contribution.getPlanCost() != null) {
													if (contribution.getEmployerPercent() != null) {
														BigDecimal contributionAmount = contribution.getPlanCost()
																.multiply(contribution.getEmployerPercent())
																.divide(new BigDecimal(100))
																.setScale(2, RoundingMode.HALF_UP);
														totalContribution = totalContribution.add(contributionAmount
																.multiply(new BigDecimal(contribution.getHeadcount())));
														if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)
																&& !bsuppCoverageLevelFunding.isEmpty()
																&& bsuppCoverageLevelFunding.get(contribution.getType())
																		.compareTo(contributionAmount) > 0) {
															BigDecimal subtotalBsuppContribution = bsuppCoverageLevelFunding
																	.get(contribution.getType())
																	.subtract(contributionAmount);
															totalBsuppContribution = totalBsuppContribution.add(
																	subtotalBsuppContribution.multiply(new BigDecimal(
																			contribution.getHeadcount())));
														}

													} else if (contribution.getEmployerContribution() != null) {
														totalContribution = totalContribution
																.add(contribution.getEmployerContribution().multiply(
																		new BigDecimal(contribution.getHeadcount())));
													}
												}
												//calculating the mirror plan estimates
												if (contribution.getMirrorHeadCount() != 0) {
													BigDecimal erContribution = contribution.getPlanCost()
															.subtract(company.getAleAmount());
													BigDecimal fPLContribution = erContribution.multiply(
															new BigDecimal(contribution.getMirrorHeadCount()));
													totalContribution = totalContribution.add(fPLContribution);
												}
											}
										}
									}
									summaryWorkingMap.put(strategyId, groupId, planType, null, totalContribution);
									if (offersBsupp) {
										summaryWorkingMap.put(strategyId, groupId, planType,
												BSSApplicationConstants.BSUPP, totalBsuppContribution);
									}

									totalHsaContribution = new BigDecimal(0);
									if (offersHdhpPlan) {
										if (strategyHsaFunding != null && strategyHsaFunding.getOptionId() > 0) {
											// If contributes, multiply out the estimate
											// If not, set the values to 0
											if (BSSApplicationConstants.HSA_MONTHLY.equals(strategyHsaFunding.getContributionFrequency())) {
												totalHsaContribution = totalHsaContribution
														.add(strategyHsaFunding.getMonthlyEeAmount()
																.multiply(new BigDecimal(hsaEmployeeHeadcount * 12)));
												totalHsaContribution = totalHsaContribution
														.add(strategyHsaFunding.getMonthlyFamilyAmount()
																.multiply(new BigDecimal(hsaFamilyHeadcount * 12)));
											}
											if (BSSApplicationConstants.HSA_QUARTERLY.equals(strategyHsaFunding.getLumpSumFrequency())) {
												totalHsaContribution = totalHsaContribution
														.add(strategyHsaFunding.getQuarterlyEeAmount()
																.multiply(new BigDecimal(hsaEmployeeHeadcount * 4)));
												totalHsaContribution = totalHsaContribution
														.add(strategyHsaFunding.getQuarterlyFamilyAmount()
																.multiply(new BigDecimal(hsaFamilyHeadcount * 4)));
											} else if (BSSApplicationConstants.HSA_ANNUAL.equals(strategyHsaFunding.getLumpSumFrequency())) {
												totalHsaContribution = totalHsaContribution
														.add(strategyHsaFunding.getAnnualEeAmount()
																.multiply(new BigDecimal(hsaEmployeeHeadcount)));
												totalHsaContribution = totalHsaContribution
														.add(strategyHsaFunding.getAnnualFamilyAmount()
																.multiply(new BigDecimal(hsaFamilyHeadcount)));
											}
										}
										summaryWorkingMap.put(strategyId, groupId, planType,
												BSSApplicationConstants.HSA, totalHsaContribution.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));
									}
								}
								
								// Waiver Allowance is stored in the planPackage
								if (benefitOffer.getPlanPackage() != null && benefitOffer.getPlanPackage().getWaiverAllowance() != null) {
									long waiverHeadcount = benefitOffer.getSummary().getWaiverHeadcount();
									planType = benefitOffer.getSummary().getDescription();
									BigDecimal waiverAllowance = benefitOffer.getPlanPackage().getWaiverAllowance().multiply(BigDecimal.valueOf(waiverHeadcount));
									summaryWorkingMap.put(strategyId, groupId, planType, BSSApplicationConstants.WAIVER_ALLOWANCE_PLAN_SUB_TYPE, waiverAllowance);
								}						

								// additionalBenefitOffers is where Supplemental
								// plans are stored
								// These are handled differently because the
								// hierarchy is different
								if (benefitOffer.getAdditionalBenefitOffers() != null) {
									for (AdditionalBenefitOffer additionalBenefitOffer : benefitOffer
											.getAdditionalBenefitOffers()) {
										if (additionalBenefitOffer.getAdditionalBenefitPlans() != null) {
											for (AdditionalBenefitPlan additionalBenefitPlan : additionalBenefitOffer
													.getAdditionalBenefitPlans()) {
												// These are disability plans
												// (LTD, STD)
												if (additionalBenefitPlan.getOptionPlans() != null) {
													totalContribution = new BigDecimal(0);
													totalContribution = totalContribution
															.add(additionalBenefitPlan.getMonthlyTotalCost());
													summaryWorkingMap.put(strategyId, groupId, "30", null,
															totalContribution);
													summaryWorkingMap.put(strategyId, groupId, "31", null,
															BigDecimal.ZERO);
												}
												// These are LIFE and COMMUTER
												// plans
												else {
													if (additionalBenefitPlan.getPlanCost() != null) {
														planType = additionalBenefitPlan.getPlanType();
														if (summaryWorkingMap.containsKey(strategyId, groupId, planType,
																null)) {
															totalContribution = (BigDecimal) summaryWorkingMap
																	.get(strategyId, groupId, planType, null);
														} else {
															totalContribution = new BigDecimal(0);
														}
														totalContribution = totalContribution
																.add(additionalBenefitPlan.getPlanCost()
																		.multiply(new BigDecimal(additionalBenefitOffer
																				.getSummary().getHeadcount())));
														summaryWorkingMap.put(strategyId, groupId, planType, null,
																totalContribution);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		// Create the return map of StrategyEstimate objects
		Map<Long, List<StrategyEstimate>> strategyEstimateMap = new HashMap<>();
		MapIterator it = summaryWorkingMap.mapIterator();
		while (it.hasNext()) {
			it.next();
			MultiKey mk = (MultiKey) it.getKey();
			StrategyEstimate strategyEstimate = new StrategyEstimate();
			Long strategyId = (Long) mk.getKey(0);
			strategyEstimate.setStrategyId(strategyId);
			strategyEstimate.setGroupId(((Long) mk.getKey(1)).longValue());
			strategyEstimate.setPlanType(mk.getKey(2).toString());
			strategyEstimate.setPlanSubType((String) mk.getKey(3));
			strategyEstimate.setEstimate((BigDecimal) it.getValue());
			List<StrategyEstimate> currentList = null;
			if (strategyEstimateMap.containsKey(strategyId)) {
				currentList = strategyEstimateMap.get(strategyId);
			} else {
				currentList = new ArrayList<>();
			}
			currentList.add(strategyEstimate);
			strategyEstimateMap.put(strategyId, currentList);
		}
		return strategyEstimateMap;
	}

	/**
	 * 
	 * @param strategyGrpHeadCounts
	 * @return
	 */
	public static Map<String, Integer> prepareCoverageLevelHeadCounts(
			List<StrategyGroupHeadCount> strategyGrpHeadCounts) {
		Map<String, Integer> map = new HashMap<>();
		for (StrategyGroupHeadCount strategyGroupHeadCount : strategyGrpHeadCounts) {
			String covgName = CoverageCodesEnums.valueOfId(strategyGroupHeadCount.getId().getCovrgCd());
			map.put(covgName, ((Long) strategyGroupHeadCount.getHeadcount()).intValue());
		}
		return map;
	}

	/**
	 * 
	 * @param strategyGroupPlansSelections
	 * @return
	 */
	public static List<String> getBenefitPlanList(
			Map<Long, Map<String, Map<Long, List<PlanSelection>>>> strategyGroupPlansSelections) {
		Set<String> medicalPlanSet = new HashSet<>();
		for (Map.Entry<Long, Map<String, Map<Long, List<PlanSelection>>>> entry: strategyGroupPlansSelections.entrySet()) {
			Map<Long, List<PlanSelection>> medicalPlanSelections = entry.getValue()
					.get(BSSApplicationConstants.MEDICAL);
			if (null != medicalPlanSelections && !medicalPlanSelections.isEmpty()) {
				for (Map.Entry<Long, List<PlanSelection>> planSelectionEntry : medicalPlanSelections.entrySet()) {
					for (PlanSelection ps : planSelectionEntry.getValue()) {
						medicalPlanSet.add(ps.getBenefitPlan());
					}
				}
			}
		}
		List<String> medicalPlans = new ArrayList<>();
		if (!medicalPlanSet.isEmpty()) {
			medicalPlans.addAll(medicalPlanSet);
		}
		return medicalPlans;
	}
	
	/**
	 * This method decides whether the strategy can be deleted or not. 
	 * 
	 * Strategy cann't be deleted if
	 * 1. The person who did not create the strategy
 	 * 2. Default strategy
	 * 3. History strategy
	 * 4. Strategy in processing or unprocessed status 
	 * 
	 * @param strategy
	 * @param submitPendingStrategyIds
	 * @param history
	 * @return
	 */
	public static boolean isStrategyDeletable(Strategy strategy, Set<Long> submitPendingStrategyIds, boolean history) {
		return (BSSSecurityUtils.getAuthenticatedPersonId().equals(strategy.getCreatedBy())
				&& BSSApplicationConstants.STRATEGY_TYPE_CUSTOM.equals(strategy.getType())
				&& !strategy.isSubmitted()
				&& !history
				&& !submitPendingStrategyIds.contains(strategy.getId()));
	}
	
	/**
	 * This method decides whether the strategy name can be edited or not. 
	 * 
	 * Strategy name cannot be edited if
	 * 1. The person who did not create the strategy
 	 * 2. Default strategy
	 * 3. History strategy
	 * 
	 * @param strategy
	 * @param history
	 * @return
	 */
	public static boolean isStrategyNameEditable(Strategy strategy, boolean history) {
		return (BSSSecurityUtils.getAuthenticatedPersonId().equals(strategy.getCreatedBy())
				&& BSSApplicationConstants.STRATEGY_TYPE_CUSTOM.equals(strategy.getType())
				&& !strategy.isSubmitted()
				&& !history);
	}	
	
	/**
	 * 
	 * @param results
	 * @return
	 */
	public static Map<String, Map<String, List<Contribution>>> getPlanContributionsByStrategyId(
			List<Object[]> results) {
		Map<String, Map<String, List<Contribution>>> contributionsMap = new HashMap<>();

		for (Object[] r : results) {
			String benefitProgram = (String) r[11];
			Contribution contribution = new Contribution();
			long id = ((BigDecimal) r[0]).longValue();
			contribution.setId(id);
			long planSelectionId = ((BigDecimal) r[1]).longValue();
			contribution.setPlanSelectionId(planSelectionId);
			String coverageLevel = (String) r[2];
			contribution.setCoverageLevel(coverageLevel);
			BigDecimal erContribPct = ((BigDecimal) r[3]);
			contribution.setEmployerPercent(erContribPct);
			long headCount = ((BigDecimal) r[4]).longValue();
			contribution.setHeadCount(headCount);
			BigDecimal bnEmplRate = ((BigDecimal) r[5]);
			contribution.setEmployeeContribution(bnEmplRate);
			BigDecimal bnEmplrRate = ((BigDecimal) r[6]);
			contribution.setEmployerContribution(bnEmplrRate);
			long planCarrier = ((BigDecimal) r[12]).longValue();
			contribution.setPlanCarrier(planCarrier);
			String overrideType = (String) r[13];
			contribution.setOverrideType(overrideType);
			String benefitPlan = (String) r[8];
			if (null != contributionsMap.get(benefitProgram)) {
				Map<String, List<Contribution>> benefitPlanContributions = contributionsMap.get(benefitProgram);
				if (null != benefitPlanContributions.get(benefitPlan)) {
					List<Contribution> cbs = benefitPlanContributions.get(benefitPlan);
					cbs.add(contribution);
				} else {
					List<Contribution> cbs = new ArrayList<>();
					cbs.add(contribution);
					benefitPlanContributions.put(benefitPlan, cbs);
				}
			} else {
				Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
				List<Contribution> cbs = new ArrayList<>();
				cbs.add(contribution);
				benefitPlanContributions.put(benefitPlan, cbs);

				contributionsMap.put(benefitProgram, benefitPlanContributions);
			}
		}
		return contributionsMap;
	}
	
	/**
	 * 
	 * @param results
	 * @return
	 */
	public static Map<String, Map<String, Map<String, BenefitPlan>>> getBenefitPlanContributionsByStrategyId(
			List<Object[]> results) {
		Map<String, Map<String, Map<String, BenefitPlan>>> benefitPlanContributionsByBenProg = new HashMap<>();
		for (Object[] r : results) {
			String benefitProgram = (String) r[11];
			String planType = (String) r[9];
			PlanContribution contribution = new PlanContribution();
			long id = ((BigDecimal) r[0]).longValue();
			contribution.setId(id);
			long planSelectionId = ((BigDecimal) r[1]).longValue();
			contribution.setPlanSelectionId(planSelectionId);
			String coverageLevel = (String) r[2];
			contribution.setType(CoverageCodesEnums.valueOfId(coverageLevel));
			BigDecimal erContribPct = ((BigDecimal) r[3]);
			contribution.setEmployerPercent(erContribPct);
			Long headCount = ((BigDecimal) r[4]).longValue();
			contribution.setHeadcount(headCount.intValue());
			BigDecimal bnEmplRate = ((BigDecimal) r[5]);
			contribution.setEmployeeContribution(bnEmplRate);
			BigDecimal bnEmplrRate = ((BigDecimal) r[6]);
			contribution.setEmployerContribution(bnEmplrRate);
			String overrideType = (String) r[13];
			contribution.setOverrideType(overrideType);
			String benefitPlanId = (String) r[8];
			contribution.setBenefitPlanId(benefitPlanId);
			BigDecimal widelyAvailableFlag = ((BigDecimal) r[14]);
			boolean isWidelyAvailablePlan = false;
			if (widelyAvailableFlag.compareTo(BigDecimal.ONE) == 0) {
				isWidelyAvailablePlan = true;
			}
			String planCategory = (String) r[15];

			if (null != benefitPlanContributionsByBenProg.get(benefitProgram)) {
				Map<String, Map<String, BenefitPlan>> planTypeBenefitPlanMap = benefitPlanContributionsByBenProg
						.get(benefitProgram);
				if (null != planTypeBenefitPlanMap.get(planType)) {
					Map<String, BenefitPlan> benefitPlanMap = planTypeBenefitPlanMap.get(planType);
					if (null != benefitPlanMap.get(benefitPlanId)) {
						BenefitPlan bp = benefitPlanMap.get(benefitPlanId);
						bp.getContributions().add(contribution);
						planTypeBenefitPlanMap.put(planType, benefitPlanMap);
					} else {
						BenefitPlan bp = populateBenefitPlan(benefitPlanId, planType, planSelectionId,
								isWidelyAvailablePlan, planCategory, contribution);
						benefitPlanMap.put(benefitPlanId, bp);
						planTypeBenefitPlanMap.put(planType, benefitPlanMap);
					}
				} else {
					BenefitPlan bp = populateBenefitPlan(benefitPlanId, planType, planSelectionId,
							isWidelyAvailablePlan, planCategory, contribution);
					Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
					benefitPlanMap.put(benefitPlanId, bp);
					planTypeBenefitPlanMap.put(planType, benefitPlanMap);
				}

			} else {
				Map<String, Map<String, BenefitPlan>> planTypeBenefitPlanMap = new HashMap<>();
				BenefitPlan bp = populateBenefitPlan(benefitPlanId, planType, planSelectionId, isWidelyAvailablePlan,
						planCategory, contribution);
				Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
				benefitPlanMap.put(benefitPlanId, bp);
				planTypeBenefitPlanMap.put(planType, benefitPlanMap);
				benefitPlanContributionsByBenProg.put(benefitProgram, planTypeBenefitPlanMap);
			}
		}
		return benefitPlanContributionsByBenProg;
	}
	
	/**
	 * 
	 * @param benefitPlanId
	 * @param planType
	 * @param planSelectionId
	 * @param isWidelyAvailablePlan
	 * @param planCategory
	 * @param contribution
	 * @return
	 */
	public static BenefitPlan populateBenefitPlan(String benefitPlanId, String planType, long planSelectionId,
			boolean isWidelyAvailablePlan, String planCategory, PlanContribution contribution) {
		BenefitPlan bp = new BenefitPlan();
		bp.setId(benefitPlanId);
		bp.setPlanType(planType);
		bp.setPlanSelectionId(planSelectionId);
		bp.setPpoPlan(isWidelyAvailablePlan);
		bp.setWidelyAvailablePlan(isWidelyAvailablePlan);
		bp.setPlanCategory(planCategory);
		bp.getContributions().add(contribution);
		return bp;
	}
	
	
	
	public static Map<String, PlanPackage> constructPlanPackages(Map<String, Map<String, Object>> groupFundingDetails,
			Company company, Map<String, List<BenefitPlanRate>> planRates, Map<String, XbssRealmPlyrPlan> plyrPlanMap,
			RealmDataDao realmDataDao, long strategyId, StrategyFundingDataDao strategyFundingDataDao) {
		Map<String, PlanPackage> planPackageMap = new HashMap<>();
		Map<String, Boolean> benOfferExceptions = new HashMap<>();
		if (MapUtils.isNotEmpty(groupFundingDetails)) {
			RenewalServiceHelper.updateFundingDetailsForBasePlan(groupFundingDetails, planRates, company, null,
					realmDataDao, null, benOfferExceptions);
			for (Map.Entry<String, Map<String, Object>> entry: groupFundingDetails.entrySet()) {
				String planType = entry.getKey();
				Map<String, Object> planTypeFunding = entry.getValue();
				String fundingType = (String) planTypeFunding.get(BSSApplicationConstants.FUNDING_TYPE);
				BigDecimal waiverAllowance = (BigDecimal) planTypeFunding.get(BSSApplicationConstants.WAIVER_ALLOWANCE);
				BigDecimal bsuppExcessOption = (BigDecimal) planTypeFunding
						.get(BSSApplicationConstants.BSUPP_EXCESS_OPTION);
				String primaryPlanType = (String) planTypeFunding.get(BSSApplicationConstants.PRIMARY_PLAN_TYPE);
				String fundingBasePlan = (String) planTypeFunding.get(BSSApplicationConstants.FUNDING_BASE_PLAN);
				String limitCoverageLevel = (String) planTypeFunding.get(BSSApplicationConstants.FUNDING_BASE_CVG);
				BigDecimal limitPct = (BigDecimal) planTypeFunding.get(BSSApplicationConstants.FUNDING_BASE_PCT);
				BigDecimal fundingModelId = (BigDecimal) planTypeFunding.get(BSSApplicationConstants.FUNDING_MODEL_ID);
				String pkgType = (String) planTypeFunding.get(BSSApplicationConstants.FUNDING_PKG_TYPE);
				BigDecimal customizedValue = (BigDecimal) planTypeFunding.get(BSSApplicationConstants.CUSTOMIZED);

				String planTypeDesc = getPlanTypeDesc(planType);

				PlanPackage pkg = new PlanPackage();
				pkg.setFundingModelId(fundingModelId.longValue());
				pkg.setFundingBasePlan(fundingBasePlan);
				pkg.setPlanType(planType);
				pkg.setStrategyId(strategyId);
				pkg.setFundingType(fundingType);
				pkg.setWaiverAllowance(waiverAllowance);
				pkg.setBsuppExcessOption(bsuppExcessOption);
				pkg.setCustomized(customizedValue.compareTo(BigDecimal.ZERO) > 0);
				if (Constants.voluntaryPlanTypeList.contains(primaryPlanType)) {
					pkg.setEmployeePaid(true);
				}
				updateFundingBasePlan(plyrPlanMap, pkg);
				setPakageType(company, pkgType, pkg);

				if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)
						&& BSSApplicationConstants.BSUPP.equals(pkg.getFundingType())) {
					List<String> bsuppSelectedVolPlanTypes = strategyFundingDataDao
							.getBsuppStrategyFundVolPlanTypes(pkg.getFundingModelId());
					pkg.setBsuppSelectedVolPlanTypes(bsuppSelectedVolPlanTypes);
				}
				// Base Funding Details
				updateCoverageLevelFunding(planTypeFunding, fundingType, limitCoverageLevel, limitPct, pkg);
				updateCoverageLevelFlatMaxOrBasePlan(planTypeFunding, fundingType, fundingBasePlan, pkg);
				planPackageMap.put(planTypeDesc, pkg);
			}
		}
		return planPackageMap;
	}
	
	private static String getPlanTypeDesc(String planType) {
		String planTypeDesc = null;
		if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)) {
			planTypeDesc = BSSApplicationConstants.MEDICAL;
		} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
			planTypeDesc = BSSApplicationConstants.DENTAL;
		} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planType)) {
			planTypeDesc = BSSApplicationConstants.VISION;
		}
		return planTypeDesc;
	}

	private static void updateFundingBasePlan(Map<String, XbssRealmPlyrPlan> plyrPlanMap, PlanPackage pkg) {
		if (null != pkg.getFundingBasePlan()
				&& !BSSApplicationConstants.FLAT_MAX.equals(pkg.getFundingBasePlan())) {
			FundingBasePlan fBasePlan = new FundingBasePlan();
			fBasePlan.setFundingBasePlan(pkg.getFundingBasePlan());
			BigDecimal planCarrierId = plyrPlanMap.get(pkg.getFundingBasePlan()).getPortfolioId();
			if (null != planCarrierId) {
				fBasePlan.setPlanCarrierId(planCarrierId.longValue());
			}
			pkg.getFundingBasePlans().add(fBasePlan);
			pkg.getBenefitPlans().add(pkg.getFundingBasePlan());
			pkg.getPlanCarrierIds().add(fBasePlan.getPlanCarrierId());
		}
	}
	
	private static void updateCoverageLevelFunding(Map<String, Object> planTypeFunding, String fundingType,
			String limitCoverageLevel, BigDecimal limitPct, PlanPackage pkg) {
		if (BSSApplicationConstants.BFPCT.equals(fundingType)) {
			if (null != limitCoverageLevel) {
				pkg.getCoverageLevelFunding().put(limitCoverageLevel, limitPct);
			} else {
				pkg.getCoverageLevelFunding().put(BSSApplicationConstants.CVG_CODE_ALL,
						(BigDecimal) planTypeFunding.get(CoverageCodesEnums.COV_EMPLOYEE.getId()));
			}
		} else {
			for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
				pkg.getCoverageLevelFunding().put(coverageLevel,
						(BigDecimal) planTypeFunding.get(coverageLevel));
			}
		}
	}

	private static void updateCoverageLevelFlatMaxOrBasePlan(Map<String, Object> planTypeFunding, String fundingType,
			String fundingBasePlan, PlanPackage pkg) {
		if (null != fundingBasePlan) {
			if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
				for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
					pkg.getCoverageLevelFundingFlatMax().put(coverageLevel,
							(BigDecimal) planTypeFunding.get(coverageLevel + "LIMIT"));
				}
			} else {
				updateCvgLevelBasePlanLimits(planTypeFunding, fundingType, pkg);
			}
		}
	}

	private static void updateCvgLevelBasePlanLimits(Map<String, Object> planTypeFunding, String fundingType,
			PlanPackage pkg) {
		if (BSSApplicationConstants.BFPCT.equals(fundingType)) {
			for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
				pkg.getCoverageLevelBasePlanLimits().put(coverageLevel,
						(BigDecimal) planTypeFunding.get(coverageLevel));
			}
		} else {
			for (String coverageLevel : CoverageCodesEnums.coverageLevels()) {
				pkg.getCoverageLevelBasePlanLimits().put(coverageLevel,
						(BigDecimal) planTypeFunding.get(coverageLevel + "LIMIT"));
			}
		}
	}

	private static void setPakageType(Company company, String pkgType, PlanPackage pkg) {
		long pkgTypeId = 0L;
		if (!company.isRenewalCompany()) {
			if (BSSApplicationConstants.TOP_QUALITY_NAME.equalsIgnoreCase(pkgType)) {
				pkgTypeId = 3L;
			} else if (BSSApplicationConstants.CONSERVATIVE_PACKAGE_NAME.equalsIgnoreCase(pkgType)) {
				pkgTypeId = 1L;
			} else if (BSSApplicationConstants.BALANCED_PACKAGE_NAME.equalsIgnoreCase(pkgType)) {
				pkgTypeId = 2L;
			}
		}
		pkg.setId(pkgTypeId);
		pkg.setTemplateId(pkgTypeId);
		pkg.setName(pkgType);
	}
	
	/**
	 * Checks if given strategy id belong to prospect or not
	 * 
	 * @param strategyId
	 * @return true if strategy id 0 </br>
	 *         false if strategy id is not 0
	 */
	public static boolean isProspectStrategy(long strategyId) {
		return ProspectConstants.PROSPECT_STRATEGY_ID == strategyId;
	}
}
