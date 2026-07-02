package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.ModelCompareExportHelper;
import com.trinet.ambis.helper.ModelCompareServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyHsaFundingDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyHsaFunding;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.ModelCompareService;
import com.trinet.ambis.service.ProspectStrategyService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitOfferFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.GroupFunding;
import com.trinet.ambis.service.model.ModelCompareGroupHeadcount;
import com.trinet.ambis.service.model.ModelComparePlanTypeCost;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.model.ModelCompareStrategyHsaFunding;
import com.trinet.ambis.service.model.StrategyBenefitPlanHeadCount;
import com.trinet.ambis.service.model.StrategyCoverageLevelHeadcount;
import com.trinet.ambis.service.model.StrategyGroupDetails;
import com.trinet.ambis.util.Constants;

@Service
public class ModelCompareServiceImpl implements ModelCompareService {

	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Autowired
	CompanyService companyService;

	@Autowired
	EmployeeDataService employeeDataService;

	@Autowired
	DisabilityOptionService disabilityOptionService;

	@Autowired
	StrategyDao strategyDao;

	@Autowired
	StrategyDataDao strategyDataDao;

	@Autowired
	StrategyFundingDataDao strategyFundingDataDao;

	@Autowired
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	@Autowired
	StrategyHsaFundingDao strategyHsaFundingDao;

	@Autowired
	RealmDataDao realmDataDao;

	@Autowired
	HeadCountService headCountService;

	@Autowired
	ProspectStrategyService prospectStrategyService;
	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	private static final Logger logger = LoggerFactory.getLogger(ModelCompareServiceImpl.class);

	@Override
	public List<ModelCompareStrategy> getMCStrategies(Company company, boolean isCallfromExport) {
		List<ModelCompareStrategy> strategies = Lists.newArrayList();
		Map<Long, List<StrategyGroupDetails>> strategyGroupsMap = new HashMap<>();
		logger.info("Company Code: {}", company.getCode());
		if (company.isProspectCompany()) {
			ModelCompareStrategy prospectStrategy = new ModelCompareStrategy();
			prospectStrategy.setHistory(true);
			prospectStrategy.setId(ProspectConstants.PROSPECT_STRATEGY_ID);
			prospectStrategy.setName(ProspectConstants.PROSPECT_STRATEGY_NAME);
			prospectStrategy.setActiveStrategy(true);
			strategies.add(prospectStrategy);
		}
		List<ModelCompareStrategy> strategyList = strategyDataDao.getModelCompareStrategies(company.getId());
		
		if(!isCallfromExport) {
			strategyGroupsMap = populateStrategyGroupMap(company);
		}
		
		RealmPlanYear prevRealmYear = null;
		for (ModelCompareStrategy mcs : strategyList) {
			if (mcs.isHistory() && prevRealmYear == null && !company.isProspectCompany()) {
				prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getCode(),
						company.getRealmPlanYearId());
			}
			mcs.setName(ModelCompareServiceHelper.getStrategyDisplayName(mcs.getName(), 
					mcs.isHistory() ? prevRealmYear : company.getRealmPlanYear(), mcs.isHistory(),
					company.isProspectCompany()));
			if (company.isProspectCompany()) {
				mcs.setActiveStrategy(false);
			}
			
			if(!strategyGroupsMap.isEmpty()) {
				List<StrategyGroupDetails> strategyGroups = strategyGroupsMap.get(mcs.getId());
				mcs.setGroups(strategyGroups);
			}
			 
			strategies.add(mcs);
		}

		return strategies;
	}

	private Map<Long, List<StrategyGroupDetails>> populateStrategyGroupMap(Company company) {
		Map<String, Set<StrategyGroupDetails>> benefitProgramStrategyGroups = employeeBenefitGroupDao
				.getStrategyGroupDetailsForCompany(company);
		Map<Long, List<StrategyGroupDetails>> strategyGroupsMap = new HashMap<>();
		benefitProgramStrategyGroups.values().stream()
				.flatMap(Set::stream)
				.filter(group -> StringUtils.isNotEmpty(group.getStatus()) && group.getStatus().equalsIgnoreCase(BSSApplicationConstants.STATUS_ACTIVE))
				.map(this::mapToStrategyGroupDetails)
				.forEach(group -> strategyGroupsMap.computeIfAbsent(group.getStrategyId(), k -> new ArrayList<>()).add(group));

		return strategyGroupsMap;
	}

	private StrategyGroupDetails mapToStrategyGroupDetails(StrategyGroupDetails group) {
		StrategyGroupDetails strategyGroup = new StrategyGroupDetails();
		strategyGroup.setStrategyId(group.getStrategyId());
		strategyGroup.setGroupId(group.getGroupId());
		strategyGroup.setGroupName(group.getGroupName());
		strategyGroup.setDefaultGroup(group.isDefaultGroup());
		return strategyGroup;
	}

	@Transactional
	@Override
	public List<ModelCompareStrategyCost> getMCSelectedStrategyCosts(List<Long> strategyIds, Company company) {
		List<ModelCompareStrategyCost> returnList = new ArrayList<>();
		Map<Long, List<ModelComparePlanTypeCost>> strategyPlanTypeCosts = new HashMap<>();
		Map<Long, List<BenefitGroup>> strategyGroups = new HashMap<>();
		// If this is a prospect client, get the strategy information from the prospect db
		// Remove strategy 0 from the list of strategies for the remainder of the processing
		if (company.isProspectCompany()) {
			strategyIds.removeIf(strategyId -> strategyId.equals(0L));
			
			ModelCompareStrategyCost prospectStrategyCost = prospectStrategyService
					.getProspectCurrentStrategyCosts(company.getCode());
			strategyPlanTypeCosts.put(ProspectConstants.PROSPECT_STRATEGY_ID, prospectStrategyCost.getPlanTypeCosts());
			strategyGroups.put(ProspectConstants.PROSPECT_STRATEGY_ID, prospectStrategyCost.getBenefitGroups());
		}
		strategyPlanTypeCosts.putAll(strategyDataDao.getStrategiesCost(strategyIds));
		logger.info("strategiesPlanCostByGroupList: {}", strategyPlanTypeCosts);

		Map<Long, List<ModelComparePlanTypeCost>> finalStrategyPlanTypeCost = new HashMap<>();

		strategyGroups.putAll(strategyDataDao.getGroupsByStrategy(strategyIds));
		logger.info("strategyGroups: {}", strategyGroups);

		Set<String> regions = StrategyServiceHelper.getHqStateCity(company);
		List<String> offeredBenefits = realmDataDao.getSelectedBenefitsExceptVoluntary(company.getRealmPlanYearId(),
				regions);

		boolean includeBsupp = false;
		boolean includeHsa = false;
		// If any of the strategies have a BSUPP record, include it for all strategies.
		// If any of the strategies have an HSA record, include it for all strategies.
		for (List<ModelComparePlanTypeCost> mcPlanTypeCostList : strategyPlanTypeCosts.values()) {
			for (ModelComparePlanTypeCost mcPlanTypeCost : mcPlanTypeCostList) {
				if (BSSApplicationConstants.BSUPP.equals(mcPlanTypeCost.getPlanType())) {
					includeBsupp = true;
				} else if (BSSApplicationConstants.HSA.equals(mcPlanTypeCost.getPlanType())) {
					includeHsa = true;
				}
			}
		}

		for (Entry<Long, List<ModelComparePlanTypeCost>> strategyNode : strategyPlanTypeCosts.entrySet()) {
			Long strategyId = strategyNode.getKey();
			for (String planFundingType : BSSApplicationConstants.MODEL_COMPARE_CONTRIBUTION_TYPES) {
				boolean planFound = false;
				if (finalStrategyPlanTypeCost.get(strategyId) == null) {
					finalStrategyPlanTypeCost.put(strategyId, new ArrayList<>());
				}
				// Add plan type as is if estimates are present
				for (ModelComparePlanTypeCost planTypeCost : strategyNode.getValue()) {
					if (planFundingType.equals(planTypeCost.getPlanType())) {
						finalStrategyPlanTypeCost.get(strategyId).add(planTypeCost);
						planFound = true;
						break;
					}
				}
				// Add other planTypes with cost set to 0 for which estimates are not available.
				// add CMTR only if it is offered
				// Do not add HSA, it should only be included if they offer it or are eligible
				if (!planFound && ((!planFundingType.equals(BSSApplicationConstants.CMTR)
						&& !planFundingType.equals(BSSApplicationConstants.BSUPP)
						&& !planFundingType.equals(BSSApplicationConstants.HSA))
						|| (planFundingType.equals(BSSApplicationConstants.CMTR)
								&& offeredBenefits.contains(planFundingType))
						|| (planFundingType.equals(BSSApplicationConstants.BSUPP) && includeBsupp)
						|| (planFundingType.equals(BSSApplicationConstants.HSA) && includeHsa))) {
					ModelComparePlanTypeCost mcPlanTypeCost = new ModelComparePlanTypeCost();
					mcPlanTypeCost.setPlanType(planFundingType);
					if (strategyId.equals(ProspectConstants.PROSPECT_STRATEGY_ID)) {
						mcPlanTypeCost.setCost(null);
						mcPlanTypeCost.setOffered(false);
					} else {
						mcPlanTypeCost.setCost(BigDecimal.ZERO);
						mcPlanTypeCost.setOffered(offeredBenefits.contains(planFundingType));
					}
					finalStrategyPlanTypeCost.get(strategyId).add(mcPlanTypeCost);
				}
			}

			ModelCompareStrategyCost sc = new ModelCompareStrategyCost();
			sc.setStrategyId(strategyId);
			sc.setPlanTypeCosts(finalStrategyPlanTypeCost.get(strategyId));
			if (strategyGroups.containsKey(strategyId)) {
				sc.setBenefitGroups(strategyGroups.get(strategyId));
			}
			returnList.add(sc);
		}

		return returnList;
	}

	@Override
	public ModelCompareStrategy getMCStrategyGroupFunding(long strategyId, Company company) {

		Strategy strategyData = strategyDao.findById(strategyId);

		boolean isProspect = !CompanyServiceHelper.isClientCompanyPattern( company.getCode() );

		// Get Medical Dental Vision details
		ModelCompareStrategy strategy = getStrategyForCompany(strategyId, company, strategyData, isProspect);
		strategy.setHasFundingOverrides(Optional.ofNullable(strategyFundingDataDao.getPlanLevelOverrides(strategyId))
				.map(map -> !map.isEmpty()).orElse(false));
		
		ModelCompareStrategyHsaFunding modelCompareStrategyHsaFunding = null;

		StrategyHsaFunding strategyHsaFuding = strategyHsaFundingDao.findByStrategyId(strategyId);

		if (strategyHsaFuding != null) {
			modelCompareStrategyHsaFunding = new ModelCompareStrategyHsaFunding(strategyHsaFuding);
		}

		// Get Additional Benefit information
		Map<Long, List<AdditionalBenefitPlan>> strategyGroupAdditionalOfferingMap = strategyDataDao
				.getAdditionalBenefitPlansForStrategy(strategyId, company.getPlanStartDate());

		for (GroupFunding groupFunding : strategy.getGroupFundingList()) {
			groupFunding.setHsaFunding(modelCompareStrategyHsaFunding);
			if (strategyGroupAdditionalOfferingMap.containsKey(groupFunding.getId())) {
				processAdditionalBenefitPlansForGroup(company, strategyGroupAdditionalOfferingMap, groupFunding);
			}
		}

		List<PlanSelection> planSelection = strategyGroupPlanSelectDao
				.findDistinctGroupIdPlanTypeByStrategyId(strategyId);
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = realmDataDao.getCoverageCodesDescByPlanTypes(
				BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER, company.getRealmPlanYear().getId());
		setMissingOfferTypeFunding(strategy, planSelection, mapOfCoverageLevels);

		return strategy;
	}
	
	private ModelCompareStrategy getStrategyForCompany(long strategyId, Company company, Strategy strategyData,
			boolean isProspect) {
		ModelCompareStrategy strategy;
		if (company.getId() == strategyData.getCompanyId()) {
			Map<Long, ModelCompareStrategy> strategyMap = strategyFundingDataDao
					.getFundingDetailsByStrategyId(Arrays.asList(strategyId), company, false, company.getRealmPlanYear().getPlanYearEnd());
			strategy = strategyMap.get(strategyId);
			strategy.setName(ModelCompareServiceHelper.getStrategyDisplayName(strategy.getName(),
					company.getRealmPlanYear(), false, isProspect));
		} else {
			RealmPlanYear prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getCode(),
					company.getRealmPlanYearId());
			Map<Long, ModelCompareStrategy> strategyMap = strategyFundingDataDao
					.getFundingDetailsByStrategyId(Arrays.asList(strategyId), company, false, prevRealmYear.getPlanYearEnd());
			strategy = strategyMap.get(strategyId);
			strategy.setName(ModelCompareServiceHelper.getStrategyDisplayName(strategy.getName(), prevRealmYear, true, isProspect));
		}
		return strategy;
	}

	private void processAdditionalBenefitPlansForGroup(Company company,
			Map<Long, List<AdditionalBenefitPlan>> strategyGroupAdditionalOfferingMap, GroupFunding groupFunding) {
		boolean isStdPlanAvailable = false;
		boolean isLtdPlanAvailable = false;
		boolean isStandAlone = false;
		List<String> stdLtdPlanList = new ArrayList<>();

		List<AdditionalBenefitPlan> additionalBenefitPlanList = strategyGroupAdditionalOfferingMap
				.get(groupFunding.getId());
		for (AdditionalBenefitPlan additionalBenefitPlan : additionalBenefitPlanList) {

			String planType = additionalBenefitPlan.getPlanType();
			BenefitOfferFunding benefitOfferFunding = new BenefitOfferFunding();
			benefitOfferFunding.setType(PlanTypesEnum.getName(planType));

			if (Constants.LIFE_CMTR_PLANS.contains(planType)) {
				benefitOfferFunding.setBenefitPlanDesc(additionalBenefitPlan.getDescription());
				groupFunding.getOfferTypeFunding().put(PlanTypesEnum.getName(planType), benefitOfferFunding);
			} else {
				if (BSSApplicationConstants.STD_CODE.equals(planType)) {
					isStdPlanAvailable = true;
				}
				if (BSSApplicationConstants.LTD_CODE.equals(planType)) {
					isLtdPlanAvailable = true;
				}
				stdLtdPlanList.add(additionalBenefitPlan.getId());
			}

		}
		if (isStdPlanAvailable && isLtdPlanAvailable) {
			isStandAlone = false;
		} else {
			isStandAlone = true;
		}
		AdditionalBenefitPlan adPlan = disabilityOptionService.getDisabilityOptionByPlans(stdLtdPlanList,
				company, isStandAlone);
		if (adPlan != null) {
			BenefitOfferFunding benefitOfferFunding = new BenefitOfferFunding();
			benefitOfferFunding.setType(BSSApplicationConstants.DISABILITY);
			benefitOfferFunding.setBenefitPlanDesc(adPlan.getDescription());
			groupFunding.getOfferTypeFunding().put(BSSApplicationConstants.DISABILITY, benefitOfferFunding);
		}
	}

	/**
	 * This method adds, the Plan Type for which funding is not available, to the
	 * group and sets an offered flag to identify whether the Plan Type is offered
	 * by company or not. Also it sets the converageLevels for plan type.
	 * 
	 * @param strategy
	 * @param planSelections
	 * @param mapOfCoverageLevels
	 */
	private void setMissingOfferTypeFunding(ModelCompareStrategy strategy, List<PlanSelection> planSelections,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels) {
		Map<Long, Set<String>> grpPlanTypeMap = new HashMap<>();
		for (PlanSelection planSelection : planSelections) {
			if (grpPlanTypeMap.containsKey(planSelection.getGroupId())) {
				grpPlanTypeMap.get(planSelection.getGroupId()).add(planSelection.getPlanType());
			} else {
				Set<String> planTypes = new HashSet<>();
				planTypes.add(planSelection.getPlanType());
				grpPlanTypeMap.put(planSelection.getGroupId(), planTypes);
			}
		}
		for (GroupFunding groupFunding : strategy.getGroupFundingList()) {
			if (!groupFunding.getOfferTypeFunding().containsKey(BSSApplicationConstants.MEDICAL)) {
				setOfferTypeAndCoverageLevels(mapOfCoverageLevels, grpPlanTypeMap, groupFunding,
						BSSApplicationConstants.MEDICAL);
			}
			if (!groupFunding.getOfferTypeFunding().containsKey(BSSApplicationConstants.VISION)) {
				setOfferTypeAndCoverageLevels(mapOfCoverageLevels, grpPlanTypeMap, groupFunding,
						BSSApplicationConstants.VISION);
			}
			if (!groupFunding.getOfferTypeFunding().containsKey(BSSApplicationConstants.DENTAL)) {
				setOfferTypeAndCoverageLevels(mapOfCoverageLevels, grpPlanTypeMap, groupFunding,
						BSSApplicationConstants.DENTAL);
			}
		}
	}

	private void setOfferTypeAndCoverageLevels(Map<String, List<CoverageLevel>> mapOfCoverageLevels,
			Map<Long, Set<String>> grpPlanTypeMap, GroupFunding groupFunding, String planType) {
		boolean offered = false;
		boolean employeePaid = false;
		Set<String> grpPlanTypes = grpPlanTypeMap.get(groupFunding.getId());
		if (CollectionUtils.isNotEmpty(grpPlanTypes)) {
			if (BSSApplicationConstants.MEDICAL.equals(planType)) {
				offered = grpPlanTypes.contains(BSSApplicationConstants.MEDICAL_PLAN_TYPES.get(0));
			} else if (BSSApplicationConstants.DENTAL.equals(planType)) {
				offered = grpPlanTypes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPES.get(0))
						|| grpPlanTypes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPES.get(1));
				employeePaid = grpPlanTypes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPES.get(1));
			} else if (BSSApplicationConstants.VISION.equals(planType)) {
				offered = grpPlanTypes.contains(BSSApplicationConstants.VISION_PLAN_TYPES.get(0))
						|| grpPlanTypes.contains(BSSApplicationConstants.VISION_PLAN_TYPES.get(1));
				employeePaid = grpPlanTypes.contains(BSSApplicationConstants.VISION_PLAN_TYPES.get(1));
			}
		}

		BenefitOfferFunding bof = new BenefitOfferFunding();
		bof.setType(planType);
		bof.setOffered(offered);
		bof.setEmployeePaid(employeePaid);
		List<CoverageLevel> coverageLevels = new ArrayList<>();
		for (CoverageLevel coverageLevel : mapOfCoverageLevels.get(planType)) {
			if (!BSSApplicationConstants.CVG_CODE_ALL.equals(coverageLevel.getId())) {
				coverageLevels.add(coverageLevel);
			}
		}
		bof.setCoverageLevels(coverageLevels);
		groupFunding.getOfferTypeFunding().put(planType, bof);
	}

	@Override
	public List<StrategyBenefitPlanHeadCount> getMCPlanStrategyCoverageHeadcount(List<Long> strategyIds,
			Company company) {
		List<StrategyBenefitPlanHeadCount> workingList = strategyDataDao.getHeadcountByPlanStrategyCoverage(strategyIds,
				company.getPlanStartDate());
		List<StrategyBenefitPlanHeadCount> returnList = new LinkedList<>();
		Map<String, Long> emptyCoverageHeadcount = new LinkedHashMap<>();
		emptyCoverageHeadcount.put(CoverageCodesEnums.COV_EMPLOYEE.getName(), null);
		emptyCoverageHeadcount.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getName(), null);
		emptyCoverageHeadcount.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getName(), null);
		emptyCoverageHeadcount.put(CoverageCodesEnums.COV_FAMILY.getName(), null);

		// Make sure each plan has all selected strategies included. If not, add
		// one and set it to not offered.
		for (StrategyBenefitPlanHeadCount mcBenefitPlanHeadcount : workingList) {
			StrategyBenefitPlanHeadCount localMCPlanHeadcount = new StrategyBenefitPlanHeadCount(
					mcBenefitPlanHeadcount.getBenefitPlan(), mcBenefitPlanHeadcount.getPlanType(),
					mcBenefitPlanHeadcount.getBasePlanType(), mcBenefitPlanHeadcount.getBenefitPlanDescr());
			for (Long strategyId : strategyIds) {
				boolean foundStrategy = false;
				for (StrategyCoverageLevelHeadcount coverageLevelHeadcount : mcBenefitPlanHeadcount
						.getStrategyCoverageLevelHeadcount()) {
					if (strategyId.equals(coverageLevelHeadcount.getStrategyId())) {
						foundStrategy = true;
						localMCPlanHeadcount.getStrategyCoverageLevelHeadcount().add(coverageLevelHeadcount);
						break;
					}
				}
				// Add the empty record
				if (!foundStrategy) {
					StrategyCoverageLevelHeadcount coverageLevelHeadcount = new StrategyCoverageLevelHeadcount();
					coverageLevelHeadcount.setStrategyId(strategyId);
					coverageLevelHeadcount.setOffered(false);
					coverageLevelHeadcount.setCoverageHeadcount(emptyCoverageHeadcount);
					localMCPlanHeadcount.getStrategyCoverageLevelHeadcount().add(coverageLevelHeadcount);
				}
			}
			returnList.add(localMCPlanHeadcount);
		}
		return returnList;
	}

	@Override
	public List<ModelCompareGroupHeadcount> getMCStrategyHeadcountCostByGroup(List<Long> selectedStrategyIDs,
			Company company) {

		Long historyStrategyId = selectedStrategyIDs.get(0);
		RealmPlanYear prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear());

		Map<Long, Map<String, ActiveEligibleEECount>> strategyGroupHeadcountMap = new HashMap<>();
		Map<Long, Map<String, Integer>> strategyGroupEnrolledHeadcountMap = new HashMap<>();
		for (Long strategyId : selectedStrategyIDs) {
			boolean history = strategyId.equals(historyStrategyId);

			// Get eligible employee headcount by benefit program
			Map<String, ActiveEligibleEECount> eligibleEmplCount = headCountService.getEligibleEmployeeCount(company,
					strategyId, prevRealmYear, history);

			strategyGroupHeadcountMap.put(strategyId, eligibleEmplCount);

			// Get enrolled employee headcount by benefit program
			Map<String, Integer> enrolledEmplCount = headCountService.getPrimaryHeadCountByBenefitProgram(company,
					strategyId, history);
			strategyGroupEnrolledHeadcountMap.put(strategyId, enrolledEmplCount);
		}

		List<ModelCompareGroupHeadcount> mcGroupHeadcountList = strategyDataDao
				.getStrategyGroupHeadcountCost(selectedStrategyIDs);
		List<String> planFundingTypeList = new ArrayList<>(BSSApplicationConstants.MODEL_COMPARE_CONTRIBUTION_TYPES);
		planFundingTypeList.add(BSSApplicationConstants.PRIMARY);
		planFundingTypeList.add(BSSApplicationConstants.ADDITIONAL);
		planFundingTypeList.add("total");
		for (ModelCompareGroupHeadcount mcGroupHeadcount : mcGroupHeadcountList) {
			String benefitProgram = mcGroupHeadcount.getBenefitProgram();
			Map<Long, LinkedList<ModelComparePlanTypeCost>> finalStrategyHeadcount = new LinkedHashMap<>();
			for (Long strategyId : selectedStrategyIDs) {
				if (mcGroupHeadcount.getStrategyHeadcountMap().containsKey(strategyId)) {
					finalStrategyHeadcount.put(strategyId, mcGroupHeadcount.getStrategyHeadcountMap().get(strategyId));
				} else {
					finalStrategyHeadcount.put(strategyId, new LinkedList<ModelComparePlanTypeCost>());
				}
			}
			mcGroupHeadcount.setStrategyHeadcountMap(finalStrategyHeadcount);
			for (Entry<Long, LinkedList<ModelComparePlanTypeCost>> strategyNode : mcGroupHeadcount
					.getStrategyHeadcountMap().entrySet()) {
				Long strategyId = strategyNode.getKey();
				Long groupHeadcount = getGroupHeadCount(strategyGroupHeadcountMap, benefitProgram, strategyId);
				Long enrolledHeadcount = getEnrolledHeadCount(strategyGroupEnrolledHeadcountMap, benefitProgram,
						strategyId);

				LinkedList<ModelComparePlanTypeCost> finalPlanTypeCostList = processFinalPlanTypeCostList(planFundingTypeList, strategyNode, groupHeadcount,
						enrolledHeadcount);
				mcGroupHeadcount.getStrategyHeadcountMap().get(strategyId).clear();
				mcGroupHeadcount.getStrategyHeadcountMap().get(strategyId).addAll(finalPlanTypeCostList);

			}
		}
		return mcGroupHeadcountList;
	}

	private Long getGroupHeadCount(Map<Long, Map<String, ActiveEligibleEECount>> strategyGroupHeadcountMap,
			String benefitProgram, Long strategyId) {
		Long groupHeadcount = 0L;

		// Get additional benefit head count
		ActiveEligibleEECount eligibleEECount = strategyGroupHeadcountMap.get(strategyId).get(benefitProgram);
		if (null != eligibleEECount) {
			groupHeadcount = eligibleEECount.getTotalHeadCount() > 0
					? Long.valueOf(eligibleEECount.getTotalHeadCount())
					: Long.valueOf(eligibleEECount.getPrimaryHeadCount())
							+ eligibleEECount.getSecondaryHeadCount();
		}
		return groupHeadcount;
	}
	
	private Long getEnrolledHeadCount(Map<Long, Map<String, Integer>> strategyGroupEnrolledHeadcountMap,
			String benefitProgram, Long strategyId) {
		Long enrolledHeadcount = 0L;
		if (null != strategyGroupEnrolledHeadcountMap.get(strategyId).get(benefitProgram)) {
			enrolledHeadcount = Long
					.valueOf(strategyGroupEnrolledHeadcountMap.get(strategyId).get(benefitProgram));
		}
		return enrolledHeadcount;
	}

	private LinkedList<ModelComparePlanTypeCost> processFinalPlanTypeCostList(List<String> planFundingTypeList,
			Entry<Long, LinkedList<ModelComparePlanTypeCost>> strategyNode,
		 Long groupHeadcount, Long enrolledHeadcount) {
		LinkedList<ModelComparePlanTypeCost> finalPlanTypeCostList = new LinkedList<>();
		for (String planFundingType : planFundingTypeList) {
			boolean planFound = false;
			// Add the existing in order
			for (ModelComparePlanTypeCost planTypeCost : strategyNode.getValue()) {
				if (planFundingType.equals(planTypeCost.getPlanType())) {

					// If this is the "primary" plan type, set the headcount to the enrolled
					// headcount
					// If this is a disability or "total" plan type, set the headcount to the
					// eligible headcount
					if (BSSApplicationConstants.PRIMARY.equals(planFundingType)) {
						planTypeCost.setHeadcount(enrolledHeadcount);
					} else if (!BSSApplicationConstants.PRIMARY_PLAN_TYPE_NAMES.contains(planFundingType)) {
						planTypeCost.setHeadcount(groupHeadcount);
					}
					finalPlanTypeCostList.add(planTypeCost);
					planFound = true;
					break;
				}
			}
			// Add the plan type when not found
			if (!planFound) {
				ModelComparePlanTypeCost mcPlanTypeCost = new ModelComparePlanTypeCost();
				mcPlanTypeCost.setPlanType(planFundingType);
				mcPlanTypeCost.setCost(BigDecimal.ZERO);
				mcPlanTypeCost.setOffered(false);
				mcPlanTypeCost.setHeadcount(0L);
				finalPlanTypeCostList.add(mcPlanTypeCost);
			}
		}
		return finalPlanTypeCostList;
	}

	@Transactional
	@Override
	public Workbook getModelCompareExcelWorkbook(Company company, Long currentStrategy, List<Long> strategyList) {

		List<ModelCompareStrategy> strategies = getMCStrategies(company,true);

		// Create a map of strategy id and name
		Map<Long, String> strategyMap = new LinkedHashMap<>();
		// Put the currentStrategy in the map first
		for (ModelCompareStrategy strategy : strategies) {
			if (strategy.getId() == currentStrategy) {
				strategyMap.put(strategy.getId(), strategy.getName());
			}
		}

		// Put the strategyList in the map in order
		for (Long strategyId : strategyList) {
			for (ModelCompareStrategy strategy : strategies) {
				if (strategy.getId() == strategyId) {
					strategyMap.put(strategy.getId(), strategy.getName());
				}
			}
		}

		Workbook workbook = new XSSFWorkbook();
		List<ModelCompareStrategyCost> mcStrategyCostList = getMCSelectedStrategyCosts(
				new LinkedList<>(strategyMap.keySet()), company);

		ModelCompareExportHelper.constructCompanyStrategyWorkbook(company, strategyMap, mcStrategyCostList, workbook);
		if (!company.isBenAdvisorUser())
			getEmployeeStrategiesPlanCostWorkbook(company, currentStrategy, strategyList, strategyMap, workbook);

		return workbook;

	}

	public void getEmployeeStrategiesPlanCostWorkbook(Company company, Long currentStrategy, List<Long> strategyList,
			Map<Long, String> strategyMap, Workbook workbook) {

		List<EmployeeStrategyData> employeeStrategyDataList = employeeDataService
				.getEmployeeStrategiesPlanCostData(company, currentStrategy, strategyList);

		ModelCompareExportHelper.constructEmployeeStrategiesPlanCostWorkbook(strategyMap, employeeStrategyDataList,
				workbook);
	}

}
