package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.service.BenefitClassService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.ExchangeService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyHistoryService;
import com.trinet.ambis.service.StrategyHsaFundingService;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.GroupRuleDto;
import com.trinet.ambis.service.model.GroupRuleDto.PlanTypeRule;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author rvutukuri
 *
 */
@Service
public class StrategyHistoryServiceImpl implements StrategyHistoryService {
	private static final Logger logger = LoggerFactory.getLogger(StrategyHistoryServiceImpl.class);

	@Autowired
	RenewalDataDao renewalDataDao;
	@Autowired
	RealmDataDao realmDataDao;
	@Autowired
	BenefitGroupService benefitGroupService;
	@Autowired
	GroupRuleService groupRuleService;
	@Autowired
	RealmPlanYearRuleService realmPlanYearRuleService;
	@Autowired
	HeadCountService headCountService;
	@Autowired
	StrategyGroupService strategyGroupService;
	@Autowired
	BenefitClassService benefitClassService;
	@Autowired
	CompanyService companyService;
	@Autowired
	StrategyDao strategyDao;
	@Autowired
	StrategyDataDao strategyDataDao;
	@Autowired
	PlanSelectionService planSelectionService;
	@Autowired
	ContributionService contributionService;
	@Autowired
	BenefitOfferExceptionService benOfferExceptionService;
	@Autowired
	PortfolioRuleDao portfolioRuleDao;
	@Autowired
	StrategyHsaFundingService strategyHsaFundingService;
	@Autowired
	StrategyFundingModelService strategyFundingModelService;
	@Autowired
	EmployeeSelectionDao employeeSelectionDao;
	@Autowired
	MandatoryRegionDao mandatoryRegionDao;
	@Autowired
	BenefitPlanDao benefitPlanDao;
	@Autowired
	CommonDataDao commonDataDao;
	@Autowired
	ExchangeService exchangeService;

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void createHistoryStrategyFromPS(Company company,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap) {
		logger.info("Entering Method : createHistoryStrategyFromPS");
		// getting previous year BSS company.
		String companyCode = company.getCode();
		companyService.createUpdateCompany(company);
		RealmPlanYear realmPlanYear = company.getRealmPlanYear();
		Date planYearEndDate = realmPlanYear.getPlanYearEnd();

		// getting benefit program details form peoplesoft.
		List<BenefitGroup> psBenefitGroups = renewalDataDao.getBenefitPrograms(company.getPfClient(), planYearEndDate);

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = headCountService
				.getHeadCountByGroupAndPlan(company, realmPlanYear.getId(), planYearEndDate, false);

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupAdditionalHeadCountMap = renewalDataDao
				.getAdditionalPlansHeadCountByGroup(companyCode, planYearEndDate);

		Map<String, Integer> groupHeadCountMap = headCountService.getEmployeeHeadcountByBenefitGroup(company,
				realmPlanYear.getId(), planYearEndDate);

		// getting EligRuleId's for the company
		Map<String, String> eligRuleIdMap = renewalDataDao.getEligRuleIdsByClient(company.getPfClient(),
				planYearEndDate);

		// getting WaitPeriods for the company
		Map<String, String> waitPeriodMap = renewalDataDao.getWaitPeriodByClient(company.getPfClient(), companyCode,
				planYearEndDate);

		// getting the funding details from peoplesoft always.
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = renewalDataDao
				.getRenewalFundingDetails(company.getCode(), planYearEndDate);

		Map<String, List<CoverageLevel>> mapOfCoverageLevels = realmDataDao
				.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, company.getRealmPlanYear().getId());

		List<String> medPlans = getMedicalPlans(company);

		Map<String, Map<String, Map<String, String>>> groupPlanOverrrideMap = strategyDataDao
				.getOverridesByBenefitGroup(company.getCode(), company.getRealmPlanYear().getId(), true);

		// getting the current submitted strategy from BSS.
		Strategy strategy = getOrCreateStrategy(company);
		
		Long strategyId = strategy.getId();
		List<Strategy> historyStrategies = new ArrayList<>();
		historyStrategies.add(strategy);

		// Getting the current HSA information from PS
		StrategyHsaFundingDto currentHsaFunding = renewalDataDao.getPsHsaFundingDetails(companyCode, planYearEndDate);
		if (currentHsaFunding != null) {
			StrategyHsaFundingDto strategyHsaFundingDto = new StrategyHsaFundingDto(currentHsaFunding, strategyId);
			strategyHsaFundingService.save(strategyHsaFundingDto);
		}

		List<BenefitGroup> benefitGroupsFinal = processBenefitGroups(company, psBenefitGroups, groupHeadCountMap,
				eligRuleIdMap, waitPeriodMap, strategyId);

		setupBenefitGroupStrategies(company, groupHeadCountMap, waitPeriodMap, strategyId, historyStrategies,
				benefitGroupsFinal);

		List<GroupRuleDto> groupRuleDtoList = groupRuleService
				.findByDate(company.getRealmPlanYear().getPlanYearStart());
		Map<String, Boolean> benOfferExceptions = benOfferExceptionService.findApplicableBy(company);

		for (BenefitGroup psBg : benefitGroupsFinal) {
			if (!"D".equals(psBg.getStatus())) {
				String benefitProgram = psBg.getBenefitProgram();
				Long benefitGroupId = psBg.getId();
				Map<String, PlanTypeRule> planTypeExceptionMap = BenefitGroupServiceHelper
						.getBenefitGroupPlanTypeExceptions(psBg, groupRuleDtoList);
				// creating funding for a strategy.
				Map<String, Map<String, Object>> fundingDetailsMap = null;
				Map<String, Map<String, Object>> benefitGroupFunding = null;
				if (null != groupFundingDetails) {
					benefitGroupFunding = groupFundingDetails.get(benefitProgram);
				}
				if (null != benefitGroupFunding) {
					fundingDetailsMap = groupFundingDetails.get(benefitProgram);
					List<String> supportedVoluntaryPlanTypes = commonDataDao
							.getBsuppVolBenPlanTypes(company.getRealm().getId());
					List<String> bsuppPlanTypes = renewalDataDao.getBsuppVoluntaryPlanTypes(benefitProgram,
							planYearEndDate, true, supportedVoluntaryPlanTypes);
					List<StrategyFundingModel> strategyFunding = new ArrayList<>();
					RenewalServiceHelper.constructRenewalStrategyFundingDetails(strategyId, psBg, strategyFunding,
							fundingDetailsMap, null, true, bsuppPlanTypes, medPlans, planTypeExceptionMap,
							mapOfCoverageLevels, benOfferExceptions);
					strategyFundingModelService.deleteStrategyFundingModelByStrategyIdAndGroupId(strategyId,
							benefitGroupId);
					strategyFundingModelService.saveAll(strategyFunding);
				} else if ("K1".equals(psBg.getType())) {
					List<StrategyFundingModel> strategyFunding = new ArrayList<>();
					// setting K1 Funding for the K1 Group.
					benefitGroupFunding = RenewalServiceHelper.createK1Funding(strategyFunding, strategyId,
							benefitGroupId, mapOfCoverageLevels, benOfferExceptions);

					// saving the funding to BSS table
					strategyFundingModelService.deleteStrategyFundingModelByStrategyIdAndGroupId(strategyId,
							benefitGroupId);
					strategyFundingModelService.saveAll(strategyFunding);
				}
				// deleting plans selections from the current strategy.
				strategyDataDao.deleteAllPlanContributionsByBenefitgroupAndStrategy(benefitGroupId, strategyId);
				strategyDataDao.deleteAllPlanSelectionsByBenefitgroupAndStrategy(benefitGroupId, strategyId);

				// Updating plan selections & Contributions for MEDICAL, DENTAL,
				// VISION
				createHPSelectionsForBenefitGroup(benefitProgram, strategyId, benefitGroupId, groupCovrgHeadCountMap,
						bgsHealthPlansMap.get(benefitProgram), groupPlanOverrrideMap.get(psBg.getBenefitProgram()),
						psBg.getType(), benefitGroupFunding, company);

				// Updating plan selections for AD benefit offers.
				createADPlanSelectionsForBenefitGroup(benefitProgram, strategyId, benefitGroupId,
						groupAdditionalHeadCountMap, bgsADPlansMap.get(benefitProgram));
			}
		}
		logger.info("Exiting Method : createHistoryStrategyFromPS");
	}

	private void setupBenefitGroupStrategies(Company company, Map<String, Integer> groupHeadCountMap,
			Map<String, String> waitPeriodMap, Long strategyId, List<Strategy> historyStrategies,
			List<BenefitGroup> benefitGroupsFinal) {
		List<BenefitGroupStrategy> benefitGroupStrategies = strategyGroupService.getBenefitGroupStrategy(strategyId,
				BSSApplicationConstants.STATUS_ACTIVE);
		if (null == benefitGroupStrategies || benefitGroupStrategies.isEmpty()) {
			// Constructing BenefitGroupStrategy and saving them.
			benefitGroupStrategies = RenewalServiceHelper.constructStrategyBenefitGroups(historyStrategies,
					benefitGroupsFinal, waitPeriodMap, groupHeadCountMap, company, null);
			strategyGroupService.saveBenefitGroupStrategies(benefitGroupStrategies);
		} else {
			updateExistingBenefitGroupStrategies(company, groupHeadCountMap, waitPeriodMap, historyStrategies,
					benefitGroupsFinal, benefitGroupStrategies);
		}
	}

	private void updateExistingBenefitGroupStrategies(Company company, Map<String, Integer> groupHeadCountMap,
			Map<String, String> waitPeriodMap, List<Strategy> historyStrategies, List<BenefitGroup> benefitGroupsFinal,
			List<BenefitGroupStrategy> benefitGroupStrategies) {
		List<BenefitGroup> newPsBenefitGroups = new ArrayList<>();
		for (BenefitGroup bg : benefitGroupsFinal) {
			boolean bgExists = false;
			for (BenefitGroupStrategy bgs : benefitGroupStrategies) {
				if (bgs.getBenefitGroup().getId() == bg.getId()) {
					bgExists = true;
					bgs.setStatus(bg.getStatus());
					bgs.setHeadcount(bg.getHeadcount());
					if( bg.getWaitingPeriod() != null ) {
						bgs.setWaitingPeriod( bg.getWaitingPeriod() );
					}
				}
			}
			if (!bgExists) {
				newPsBenefitGroups.add(bg);
			}
		}
		if (CollectionUtils.isNotEmpty(newPsBenefitGroups)) {
			List<BenefitGroupStrategy> newBenefitGroupStrategies = RenewalServiceHelper
					.constructStrategyBenefitGroups(historyStrategies, newPsBenefitGroups, waitPeriodMap,
							groupHeadCountMap, company, null);
			benefitGroupStrategies.addAll(newBenefitGroupStrategies);

		}
		strategyGroupService.saveBenefitGroupStrategies(benefitGroupStrategies);
	}

	private List<BenefitGroup> processBenefitGroups(Company company, List<BenefitGroup> psBenefitGroups,
			Map<String, Integer> groupHeadCountMap, Map<String, String> eligRuleIdMap,
			Map<String, String> waitPeriodMap, Long strategyId) {
		// getting the current benefit groups in BSS.
		List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(strategyId,
				BSSApplicationConstants.STATUS_ACTIVE);

		Map<String, BenefitGroup> bssBenefitGroupsMap = new HashMap<>();
		Map<String, BenefitGroup> psBenefitGroupsMap = new HashMap<>();
		for (BenefitGroup psBg : psBenefitGroups) {
			psBenefitGroupsMap.put(psBg.getBenefitProgram(), psBg);
		}

		for (BenefitGroup bssBg : benefitGroups) {
			if (!psBenefitGroupsMap.keySet().contains(bssBg.getBenefitProgram())) {
				bssBg.setStatus("D");
			}
			if (groupHeadCountMap != null && groupHeadCountMap.get(bssBg.getBenefitProgram()) != null) {
				bssBg.setHeadcount(groupHeadCountMap.get(bssBg.getBenefitProgram()));
			} else {
				bssBg.setHeadcount(0);
			}
			bssBenefitGroupsMap.put(bssBg.getBenefitProgram(), bssBg);
		}

		for (BenefitGroup psBg : psBenefitGroups) {
			if( bssBenefitGroupsMap.containsKey( psBg.getBenefitProgram() )) {
				// If found, update the bss benefit group type, state and name with the PS value
				BenefitGroup bssBenefitGroup = bssBenefitGroupsMap.get(psBg.getBenefitProgram());
				bssBenefitGroup.setName(psBg.getName());
				bssBenefitGroup.setType(psBg.getType());
				bssBenefitGroup.setState(psBg.getState());
				bssBenefitGroup.setWaitingPeriod(waitPeriodMap.get(psBg.getBenefitProgram()));
			} else {
				String benefitProgram = psBg.getBenefitProgram();
				String eligRuleId = eligRuleIdMap.get(benefitProgram);
				// constructing benefit group object
				RenewalServiceHelper.constructBenefitGroupForRenewalCompany(psBg, company, groupHeadCountMap, null,
						eligRuleId, waitPeriodMap);
				bssBenefitGroupsMap.put(psBg.getBenefitProgram(), psBg);
			}
		}
		List<BenefitGroup> benefitGroupsFinal = new ArrayList<>();
		benefitGroupsFinal.addAll(bssBenefitGroupsMap.values());

		// assigning class codes
		benefitGroupsFinal = benefitClassService.generateAllClassCodes(company, benefitGroupsFinal);
		// Updating groups in BSS
		benefitGroupsFinal = benefitGroupService.saveAll(benefitGroupsFinal);
		return benefitGroupsFinal;
	}

	private Strategy getOrCreateStrategy(Company company) {
		Strategy strategy = null;

		List<Strategy> strategies = strategyDao.findByCompanyIdAndSubmitted(company.getId(), true);
		if (null != strategies && !strategies.isEmpty()) {
			strategy = strategies.get(0);
			strategy.setBudgetFactor(1);
		} else {
			// creating the strategy if it doesn't exist in BSS.
			strategy = StrategyServiceHelper.constructStrategyForCurrent(company, StrategyTypesEnums.F_S.getName());
		}
		
		strategy = strategyDao.saveAndFlush(strategy);
		
		return strategy;
	}

	private List<String> getMedicalPlans(Company company) {
		List<String> medPlans = null;
		if (exchangeService.isMedicalOffered(company.getRealmPlanYearId())) {
			// updated the logic based on passport changes
			String state = company.getHeadQuatersState();
			Map<String, Map<String, String>> defaultPlanMap = realmDataDao
					.getPortfilioDefaultPlans(company.getRealmPlanYear().getId());

			// Get all portfolios by Region for realmYearId
			boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
			Map<String, Set<PlanCarrier>> planCarrierMap = portfolioRuleDao.getPortfoliosByHqRegion(
					company.getRealmPlanYearId(), state, company.getZipCode(), company.getExclusiveMedPlan(),
					company.getPlanStartDate(), isPickChoose );
			BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());

			Set<String> plansPortfoliosList = new HashSet<>();
			Set<PlanCarrier> primaryPlanCarrierList = new HashSet<>();
			if (planCarrierMap.get(Constants.MEDICAL) != null) {
				primaryPlanCarrierList.addAll(planCarrierMap.get(Constants.MEDICAL));
				for (PlanCarrier pc : planCarrierMap.get(Constants.MEDICAL)) {
					plansPortfoliosList.add(String.valueOf(pc.getId()));
				}
			}
			Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
			Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company,
					primaryPlanCarriers, realmDataDao);

			Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap = benefitPlanDao
					.getAllPrimaryBenefitPlans(plansPortfoliosList, company, outOfRegionPlans);

			medPlans = RenewalServiceHelper.getBCPlansByType(allBenefitStatePlansMap, Constants.MEDICAL);
		}
		return medPlans;
	}

	private void createHPSelectionsForBenefitGroup(String benefitProgram, Long strategyId, Long benefitGroupId,
			Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap,
			Map<String, Map<String, BenefitPlan>> hpPlansMap, Map<String, Map<String, String>> planOverrides,
			String groupType, Map<String, Map<String, Object>> offerTypeFunding, Company company) {
		logger.info("Entering Method : createHPSelectionsForBenefitGroup");
		Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
		List<PlanSelection> planSelections = createPlanSelections(strategyId, benefitGroupId, hpPlansMap,
				benefitPlanMap);
		// Saving all plan selections to DB
		planSelections = planSelectionService.saveAll(planSelections);
		// Creating all contributions for all plan selections and saving to
		// DB
		List<Contribution> contributions = new ArrayList<>();
		List<PlanCoverageLevelHeadCount> headCountList = null;
		Map<String, List<PlanCoverageLevelHeadCount>> planCovrgHeadCountMap = null;
		if (groupCovrgHeadCountMap != null) {
			planCovrgHeadCountMap = groupCovrgHeadCountMap.get(benefitProgram);
		}
		for (PlanSelection ps : planSelections) {
			BenefitPlan bp = benefitPlanMap.get(ps.getBenefitPlan());
			headCountList = new ArrayList<>();
			if (null != planCovrgHeadCountMap && null != planCovrgHeadCountMap.get(bp.getId())) {
				headCountList.addAll(planCovrgHeadCountMap.get(bp.getId()));
			}
			// always creating the contributions based on plan level funding.
			contributions.addAll(RenewalServiceHelper.constructHistoryContributions(bp, ps, headCountList,
					planOverrides, groupType, offerTypeFunding));
		}
		// saving contribution data.
		contributionService.saveAll(contributions);

		// calculating estimates & saving to DB
		try {
			Map<Long, List<StrategyEstimate>> strategyEstimateMap = RenewalServiceHelper
					.calculateStrategyGroupEstimates(strategyId, benefitGroupId, contributions, offerTypeFunding);
			strategyDataDao.deleteStrategyEstimateByStrategyGroup(strategyId, benefitGroupId);
			strategyDataDao.insertStrategyEstimate(strategyEstimateMap);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, company.getCode(), null);
		}

		logger.info("Exiting Method : createHPSelectionsForBenefitGroup");
	}

	private List<PlanSelection> createPlanSelections(Long strategyId, Long benefitGroupId,
			Map<String, Map<String, BenefitPlan>> hpPlansMap, Map<String, BenefitPlan> benefitPlanMap) {
		List<PlanSelection> planSelections = new ArrayList<>();
		if (MapUtils.isNotEmpty(hpPlansMap)) {
			for ( Map.Entry<String,Map<String,BenefitPlan>> primaryOfferEntry : hpPlansMap.entrySet() ) {
				Map<String, BenefitPlan> offerPlanMap = primaryOfferEntry.getValue();
				for ( Map.Entry<String,BenefitPlan> benefitPlanEntry : offerPlanMap.entrySet() ) {
					BenefitPlan bp = new BenefitPlan();
					Long headCount = 0L;
					if (null != benefitPlanEntry.getValue() ) {
						BeanUtils.copyProperties( benefitPlanEntry.getValue(), bp);
					} else {
						continue;
					}
					if (null == benefitPlanMap.get(bp.getId())) {
						benefitPlanMap.put(bp.getId(), bp);
						PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId,
								benefitGroupId, bp, headCount);
						planSelections.add(planSelection);
					}
				}
			}
		}
		return planSelections;
	}

	private void createADPlanSelectionsForBenefitGroup(String benefitProgram, Long strategyId, Long benefitGroupId,
			Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupAdditionalHeadCountMap,
			Map<String, Map<String, BenefitPlan>> adPlansMap) {
		logger.info("Entering Method : createADPlanSelectionsForBenefitGroup");
		List<PlanSelection> planSelections = new ArrayList<>();
		Map<String, List<PlanCoverageLevelHeadCount>> additionalPlanHeadCountMap = null;
		// getting plan head count map for the current benefitProgram.
		if (groupAdditionalHeadCountMap != null) {
			additionalPlanHeadCountMap = groupAdditionalHeadCountMap.get(benefitProgram);
		}
		if (null != adPlansMap && !adPlansMap.isEmpty()) {
			createADPlanSelections(strategyId, benefitGroupId, adPlansMap, planSelections, additionalPlanHeadCountMap);
		}
		// Creating Plan selections for the Benefit Group.
		planSelectionService.saveAll(planSelections);
		logger.info("Exiting Method : createADPlanSelectionsForBenefitGroup");
	}

	private void createADPlanSelections(Long strategyId, Long benefitGroupId,
			Map<String, Map<String, BenefitPlan>> adPlansMap, List<PlanSelection> planSelections,
			Map<String, List<PlanCoverageLevelHeadCount>> additionalPlanHeadCountMap) {
		for ( Map.Entry<String,Map<String,BenefitPlan>> adPlanEntry : adPlansMap.entrySet() ) {
			Map<String, BenefitPlan> offerPlanMap = adPlanEntry.getValue();
			for ( Map.Entry<String,BenefitPlan> offerEntry : offerPlanMap.entrySet() ) {
				BenefitPlan bp = new BenefitPlan();
				long headCount = 0;
				if( null != offerEntry.getValue() ) {
					BeanUtils.copyProperties( offerEntry.getValue(), bp );
				} else {
					continue;
				}
				if (additionalPlanHeadCountMap != null) {
					List<PlanCoverageLevelHeadCount> headCountsList = additionalPlanHeadCountMap.get( offerEntry.getKey() );
					if (headCountsList != null) {
						headCount = RenewalServiceHelper.getHeadCount(headCountsList, offerEntry.getKey() );
					}
				}
				PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId,
						benefitGroupId, bp, headCount);
				planSelections.add(planSelection);
			}
		}
	}

}
