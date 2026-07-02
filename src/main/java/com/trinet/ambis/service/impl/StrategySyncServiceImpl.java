package com.trinet.ambis.service.impl;

import com.trinet.ambis.service.DefaultPlanMappingService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.service.BenefitPlanService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CacheObjectLevelEnum;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.ProspectStrategyServiceHelper;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.helper.SubmitServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDefaultPlanDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Company.ProcessInfo;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyDefaultPlan;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.persistence.projections.StrategySitusDetail;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.ProspectDefaultPlanAssignmentService;
import com.trinet.ambis.service.ProspectDefaultPlanMappingService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.service.model.GroupRuleDto;
import com.trinet.ambis.service.model.GroupRuleDto.PlanTypeRule;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author rvutukuri
 *
 */
@Service
public class StrategySyncServiceImpl implements StrategySyncService {

	@Autowired
	CacheService cacheService;
	@Autowired
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;
	@Autowired
	RealmPlanYearRuleService realmPlanYearRuleService;
	@Autowired
	BenefitPlanDao benefitPlanDao;
	@Autowired
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;
	@Autowired
	RealmDataDao realmDataDao;
	@Autowired
	PortfolioRuleDao portfolioRuleDao;
	@Autowired
	EmployeeSelectionDao employeeSelectionDao;
	@Autowired
	StrategyDataDao strategyDataDao;
	@Autowired
	StrategyDao strategyDao;
	@Autowired
	RenewalDataDao renewalDataDao;
	@Autowired
	StrategyGroupDataDao strategyGroupDataDao;
	@Autowired
	PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	@Autowired
	StrategyGroupService strategyGroupService;
	@Autowired
	ContributionService contributionService;
	@Autowired
	MandatoryRegionDao mandatoryRegionDao;
	@Autowired
	PsCompanyDao psCompanyDao;
	@Autowired
	RealmPlyrPlanService realmPlyrPlanService;
	@Autowired
	PlanSelectionService planSelectionService;
	@Autowired
	PlanRatesService planRatesService;
	@Autowired
	XbssRealmPlyrPlanDao realmPlyrPlanDao;
	@Autowired
	CompanyBandCodesDao companyBandCodesDao;
	@Autowired
	BenefitGroupService benefitGroupService;
	@Autowired
	CompanyDataDao companyDataDao;
	@Autowired
	CompanyDao companyDao;
	@Autowired
	CompanyService companyService;
	@Autowired
	BenefitOfferExceptionService benOfferExceptionService;
	@Autowired
	MinFundExceptionService minFundExceptionService; 
	@Autowired
	RealmPlanYearService realmPlanYearService;
	@Autowired
	EmployeeDataService employeeDataService;
	@Autowired
	PlanMappingDao planMappingDao;
	@Autowired
	HeadCountService headCountService;
	@Autowired
	GroupRuleService groupRuleService;
	@Autowired
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;
	@Autowired
	StrategyServiceImpl strategyService;
	@Autowired
	EmployeePlanAssignmentService emplPlanAssignmentService;
	@Autowired
	HrpDao hrpDao;
	@Autowired
	EePlanAssignmentDao eePlanAssignmentDao;
	@Autowired
	StrategyFundingModelService strategyFundingModelService;
	@Autowired
	StrategyDefaultPlanDao strategyDefaultPlanDao;
	@Autowired
	PortfolioService portfolioService;
	@Autowired
	ProspectDefaultPlanAssignmentService prospectDefaultPlanAssignmentService;
	@Autowired
	EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;
	@Autowired
	DefaultPlanMappingService planMappingAssignmentService;
	@Autowired
	ProspectDefaultPlanMappingService prospectDefaultPlanMappingService;
	@Autowired
	BenefitPlanService benefitPlanService;

	

	private static final Logger logger = LoggerFactory.getLogger(StrategySyncServiceImpl.class);

	@Override
	public void syncStrategyHistoryData(Company company, Long strategyId) {
		// to be defined
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void syncStrategyData(Company company, Long strategyId) {
		performStrategySync(company, strategyId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void syncStrategiesForCompany(String companyCode, BenExchngEnums exchange,
			ProcessInfo processInfo) {
		boolean isPlanYearChanged = isPlanYearChange(processInfo);
		boolean isBandUpdated = isBandUpdate(processInfo);
        if(isPlanYearChanged || isBandUpdated) {
            evictCacheBeforeRetrievingCompanyDetails(companyCode);
        }
		Company company = companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true);
		company.setProcessInfo(processInfo);
		company.setBandCodeUpdated(isBandUpdated);
		company.setPlanYearChanged(isPlanYearChanged);
		List<Strategy> strategyList = strategyDao.findByCompanyId(company.getId());
		if (strategyList != null && !strategyList.isEmpty() && !isPlanYearChanged) {
			syncDissimilarSitusStrategies(company, strategyList);
		}
		performStrategySync(company, null);
	}

    private void evictCacheBeforeRetrievingCompanyDetails(String companyCode) {
        cacheService.invalidateCache(CacheObjectTypeEnum.ALL.getObjectType(), CacheObjectLevelEnum.COMPANY.getObjectLevel(), companyCode);
    }

    private void performStrategySync(Company company, Long strategyId) {
		if (company.isRenewalCompany() && !company.isPlanYearChanged()) {
	        employeeDataService.employeeDataSync(company);
	    }
	    Long planYearId = company.getRealmPlanYearId();
	    List<Long> planYears = Collections.singletonList(planYearId);
	    
	    Map<String, BenefitPlan> benefitPlans = employeeSelectionDao.getRealmPlanYearBenefitPlans(planYears);
	    Map<String, List<BenefitPlanRate>> planRates = planRatesService.getBenefitPlanRatesBy(company);
	    Map<String, XbssRealmPlyrPlan> plyrPlans = realmPlyrPlanService.getMapForRealmPlanYear(planYearId);
	    Map<String, Map<String, String>> defaultPlans = realmDataDao.getPortfilioDefaultPlans(planYearId);
	    Map<String, Boolean> offerExceptions = benOfferExceptionService.findApplicableBy(company);
	    updateRateGroupId(company, planRates);
		Map<String, String> currentFuturePlans = new HashMap<>();
		List<StrategyDefaultPlan> lifeDisabilityPlans = new ArrayList<>();

	    if (company.isPlanYearChanged()) {
	    	//updating the new company id for strategy 
	        updateStrategiesAndBenefitGroupsForPlanYearChange(company);
	        currentFuturePlans = getCurrentAndFutureBenefitsPlansMap(company);
	        lifeDisabilityPlans = getDefaultLifeAndDisabilityPlansForPlanYearChange(company);
	    }

	    if (strategyId != null) {
	        Strategy strategy = strategyDao.findById(strategyId.longValue());
	        syncStrategy(company, strategy, defaultPlans, planRates, benefitPlans, plyrPlans, offerExceptions, null, null);
	        strategy.setUpdateTime(new Date());
	        strategyDao.save(strategy);
	    } else {
	        List<Strategy> strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
	        for (Strategy strategy : strategies) {
	            if (company.isAcaLargeEmplrStatusUpdated()) {
	                strategy.setAcaFplOpted(BSSApplicationConstants.ACA_FPL_OPTED_IN);
	            }
	            syncStrategy(company, strategy, defaultPlans, planRates, benefitPlans, plyrPlans, offerExceptions, currentFuturePlans, lifeDisabilityPlans);
	            strategy.setUpdateTime(new Date());
	            strategyDao.save(strategy);
	        }
	        
	        if (company.isPlanYearChanged() || company.isBandCodeUpdated() || company.isAcaLargeEmplrStatusUpdated()) {
	            resetStrategyCache(strategies);
                cacheService.invalidateOutofDateCache(company);
	        }

			updateBandCode(company);
	        updateRegions(company);

	        if (company.isAcaLargeEmplrStatusUpdated()) {
	            companyDataDao.updateAcaLargeEmplr(company.getId(), company.isEligAle());
	        }
	    }
		if (company.isPlanYearChanged()) {
			deleteOldQuarterCompanyRecord(company);
		}
	}


	private void updateStrategiesAndBenefitGroupsForPlanYearChange(Company company) {
		deleteExistingStrategiesForNewCompanyId(company);
		updateStrategiesWithNewCompanyId(company);
		updateBenefitGroupsWithNewCompanyId(company);
		if (!company.isRenewalCompany()) {
			createEeDefaultAssignmentForNewCompany(company.getProcessInfo().getOldCompanyId(), company);
		}
		updateCompanyDetailsForPlanYearSync(company);
	}


	private void deleteOldQuarterCompanyRecord(Company company) {
		Long oldCompanyId = Optional.ofNullable(company)
				.map(Company::getProcessInfo)
				.map(ProcessInfo::getOldCompanyId)
				.orElse(null);
		if (company != null && Objects.equals(oldCompanyId, company.getId())) {
			throw new IllegalStateException(String.format("Old and new company ids should not be the same %s", oldCompanyId));
		}
		logger.info("deleteOldQuarterCompanyRecord() - resetting old quarter company for companyId: {} with hard-delete enabled", oldCompanyId);
		companyService.resetCompany(oldCompanyId, true);
		logger.info("deleteOldQuarterCompanyRecord() - completed cleanup for companyId: {}", oldCompanyId);
	}

	private boolean isPlanYearChange(ProcessInfo processInfo) {
		return processInfo != null
				&& ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName().equals(processInfo.getProcessName());
	}
	
	private boolean isBandUpdate(ProcessInfo processInfo) {
		return processInfo != null && ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName()
				.equals(processInfo.getProcessName());

	}

	private void deleteExistingStrategiesForNewCompanyId(Company company) {
		List<Strategy> newStrategies = strategyDao.findByCompanyId(company.getId());
		if(newStrategies != null && !newStrategies.isEmpty()) {
			Set<Long> strategyIds = newStrategies.stream().map(Strategy::getId).collect(Collectors.toSet());
			strategyService.deleteExistingStrategies(strategyIds);
		}
	}

	private void updateStrategiesWithNewCompanyId(Company company) {
		List<Strategy> existingStrategies = strategyDao
				.findByCompanyId(company.getProcessInfo().getOldCompanyId());
		if (existingStrategies != null && !existingStrategies.isEmpty()) {
			existingStrategies.forEach(strategy -> strategy.setCompanyId(company.getId()));
			strategyDao.saveAllAndFlush(existingStrategies);
		}
	}

	private void updateBenefitGroupsWithNewCompanyId(Company company) {
		List<BenefitGroup> benefitGroups = benefitGroupService
				.findByCompanyId(company.getProcessInfo().getOldCompanyId());
		if (benefitGroups != null && !benefitGroups.isEmpty()) {
			benefitGroups.forEach(benefitGroup -> benefitGroup.setCompanyId(company.getId()));
			benefitGroupService.saveAll(benefitGroups);
		}
	}
	
	private void createEeDefaultAssignmentForNewCompany(Long oldCompanyId, Company company) {
		emplDefaultPlanAssignmentService
				.deleteEmplDefaultPlanAssignments(oldCompanyId);

		boolean planMappingServiceEnabled = RulesAndConfigsUtils.isPlanMappingServiceEnabled(company.getRealmPlanYear().getId());
		if (planMappingServiceEnabled && company.isProspectCompany()) {
			planMappingAssignmentService.callPlanMappingService(company);
		} else {
			prospectDefaultPlanMappingService.createCensusDefaultRegionalPlanMapping(company);
		}
	}
	
	private void updateCompanyDetailsForPlanYearSync(Company company) {
		Company oldCompany = Optional
				.ofNullable(companyService.findByCompanyId(company.getProcessInfo().getOldCompanyId())).orElse(null);
		if (oldCompany != null) {
			// Copy relevant fields from oldCompany to company
			company.setHeadcount(oldCompany.getHeadcount());
			company.setCurrentYearTotalCost(oldCompany.getCurrentYearTotalCost());
			company.setPercentChange(oldCompany.getPercentChange());
			company.setTotalBenefitGroups(oldCompany.getTotalBenefitGroups());
			company.setTotalEmployees(oldCompany.getTotalEmployees());
			company.setAcaLargeEmplr(oldCompany.isAcaLargeEmplr());
			company.setProspectId(oldCompany.getProspectId());
			company.setAuthBroker(oldCompany.getAuthBroker());
			company.setBundleId(oldCompany.getBundleId());
			company.setPlYrChangeSyncExcuted(1);
			company.setStrategyAccessed(oldCompany.getStrategyAccessed());
			company.setAleUpdated(oldCompany.getAleUpdated());
			company.setOmsOffering(oldCompany.getOmsOffering());

			companyDao.saveAndFlush(company);
		}
	}
	
	private Map<String, String> getCurrentAndFutureBenefitsPlansMap(Company company) {
		RealmPlanYear oldPlanYear = realmPlanYearService
				.getRealmPlanYearById(company.getProcessInfo().getOldRealmPlanYear());
		RealmPlanYear newPlanYear = company.getRealmPlanYear();
		return hrpDao.getCurrentFutureBenefitPlansMap(oldPlanYear.getId(),
				oldPlanYear.getPlanYearEnd(), newPlanYear.getId(), newPlanYear.getPlanYearEnd(), false);
	}
	
	private List<StrategyDefaultPlan> getDefaultLifeAndDisabilityPlansForPlanYearChange(Company company) {
		return strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
				BSSApplicationConstants.ADDITIONAL_PLAN_TYPES_INCLUD_CMTR);
	}
	
	private void resetStrategyCache(List<Strategy> strategies) {
		strategies.stream()
				.forEach(strategy -> cacheService.invalidateCache(
						CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE.getObjectType(),
						CacheObjectLevelEnum.STRATEGY.getObjectLevel(), String.valueOf(strategy.getId())));
	}

	private void updateRegions(Company company) {
		if (company.isRegionsUpdated()) {
			Set<String> allRegions = new HashSet<>();
			allRegions.addAll(company.getCompanyRegions());
			allRegions.addAll(company.getFundingRegions());
			allRegions.addAll(company.getEmployeeRegions());
			companyDataDao.insertUpdateCompanyRegions(company.getId(), allRegions);
			company.setRegionsUpdated(false);
		}
	}

	private void updateBandCode(Company company) {
		if (!company.isBandCodeUpdated()) {
			return;
		}
		if (!company.isProspectCompany()) {
			List<CompanyBandCodes> bandCodes = CompanyServiceHelper.getBssBandCodeList(company.getId(),
					company.getBandCodes());
			companyBandCodesDao.insertUpdateCompanyBandCodes(company.getId(), bandCodes);
		}
		company.setBandCodeUpdated(false);
	}


	private void syncStrategy(Company company, Strategy strategy, Map<String, Map<String, String>> defaultPlanMap,
			Map<String, List<BenefitPlanRate>> planRates, Map<String, BenefitPlan> mapOfBenefitPlans,
			Map<String, XbssRealmPlyrPlan> plyrPlanMap, Map<String, Boolean> benOfferExceptions,
			Map<String, String> benefitPlansMap, List<StrategyDefaultPlan> lifeAndDisabilityDefaultPlans) {
		// updating strategy plans
		if (company.isPlanYearChanged()) {
			updateStrategyPlansforPlanYearOrSitusChanges(company, strategy, benOfferExceptions, false);
			if (benefitPlansMap != null) {
				updateEePlanAssignmentWithNewBenefitPlans(benefitPlansMap, strategy, company);
				updateStrategyLimitPlanWithNewBenefitPlans(benefitPlansMap, strategy);
			}
			if (CollectionUtils.isNotEmpty(lifeAndDisabilityDefaultPlans)) {
				updateStrategyAdditionalPlans(strategy, lifeAndDisabilityDefaultPlans);
			}
		}
		// getting BSS contributions
		List<Object[]> results = null;
		Set<MinFundExceptionDto> minFundExceptions = minFundExceptionService.findActiveByCompanyCodeAndQuarter(company);
		boolean isMinFundExceptionUpdated = isMinFundExceptionUpdated(minFundExceptions, strategy);

        if (company.isPlanYearChanged() || company.isBandCodeUpdated() || isMinFundExceptionUpdated || (company.isAcaLargeEmplrStatusUpdated()) || company.isRatesUpdated()) {
			results = strategyDataDao.getPlanContributionsByStrategyId(company, strategy.getId(), false);
			List<BenefitGroupStrategy> benefitGroupStrategies = strategyGroupService
					.getBenefitGroupStrategy(strategy.getId(), BSSApplicationConstants.STATUS_ACTIVE);
			Map<String, Map<String, Map<String, BenefitPlan>>> getBenefitPlanContributions = StrategyServiceHelper
					.getBenefitPlanContributionsByStrategyId(results);
			updateContributionsForBandCodeChanges(getBenefitPlanContributions, company, planRates, plyrPlanMap,
					strategy, defaultPlanMap, benefitGroupStrategies, benOfferExceptions, strategy.getAcaFplOpted());
		}
		Map<String, Map<String, List<Contribution>>> bssContributionsMap = null;
		// updating strategy HeadCounts
		results = strategyDataDao.getPlanContributionsByStrategyId(company, strategy.getId(), false);
		bssContributionsMap = StrategyServiceHelper.getPlanContributionsByStrategyId(results);
		if (company.isRenewalCompany() || company.isProspectCompany() || company.isProspectConvertedClient()) {
			updateStrategyHeadCounts(company, strategy.getId(), mapOfBenefitPlans, bssContributionsMap);
		}

	}
	
	private void updateEePlanAssignmentWithNewBenefitPlans(Map<String, String> newBenefitPlansMap, Strategy strategy,
			Company company) {
		List<EePlanAssignment> emplPlanAssignments = emplPlanAssignmentService
				.getEmployeePlanAssigmentBy(Arrays.asList(strategy.getId()));
		if (CollectionUtils.isNotEmpty(emplPlanAssignments)) {
			List<EePlanAssignment> eePlanAssignmentsToDefault = new ArrayList<>();
			emplPlanAssignments.forEach(emplPlanAssignment -> {
				String benefitPlan = emplPlanAssignment.getBenefitPlan();
				if (newBenefitPlansMap.containsKey(benefitPlan)) {
					emplPlanAssignment.setBenefitPlan(newBenefitPlansMap.get(benefitPlan));
				} else {
					eePlanAssignmentsToDefault.add(emplPlanAssignment);
				}
			});
			eePlanAssignmentDao.saveAllAndFlush(emplPlanAssignments);

			if (CollectionUtils.isNotEmpty(eePlanAssignmentsToDefault)) {
				assignDefaultPlans(eePlanAssignmentsToDefault, company, strategy.getId());
			}
		}
	}
	
	private void assignDefaultPlans(List<EePlanAssignment> eeAssignments, Company company, Long strategyId) {
		Map<String, Set<String>> empIdsByPlanType = eeAssignments.stream()
				.collect(Collectors.groupingBy(e -> e.getEePlanAssignmentPK().getBenefitType(),
						Collectors.mapping(e -> e.getEePlanAssignmentPK().getEmplId(), Collectors.toSet())));

		for (Map.Entry<String, Set<String>> entry : empIdsByPlanType.entrySet()) {
			String benefitType = entry.getKey();
			Set<String> empIds = entry.getValue();
			Map<Long, Boolean> medicalPortfolios = new HashMap<>();

			if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(benefitType)) {
				medicalPortfolios = portfolioRuleDao.getMedicalPortfoliosBy(strategyId, company.getRealmPlanYearId(),
						company.getHeadQuatersState());
			}
			if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(benefitType)) {
				benefitType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
			}
			if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(benefitType)) {
				benefitType = BSSApplicationConstants.VISION_PLAN_TYPE;
			}
			prospectDefaultPlanAssignmentService.assignDefaultPlanBy(empIds, strategyId, medicalPortfolios,
					Set.of(benefitType));
		}
	}

	private void updateStrategyLimitPlanWithNewBenefitPlans(Map<String, String> newBenefitPlansMap, Strategy strategy) {
		List<StrategyFundingModel> sfmList = strategyFundingModelService
				.getStrategyFundingModelByStrategyId(strategy.getId());

		sfmList.forEach(strategyFundingModel -> {
			String benefitPlan = strategyFundingModel.getBaseBenefitPlan();
			if(benefitPlan != null && !benefitPlan.equals(BSSApplicationConstants.FLAT_MAX)) {
				if (newBenefitPlansMap.containsKey(benefitPlan)) {
					strategyFundingModel.setBaseBenefitPlan(newBenefitPlansMap.get(benefitPlan));
				} else {
					strategyFundingModel.setBaseBenefitPlan(null);
				}
			}
		});

		strategyFundingModelService.saveAll(sfmList);
	}
	
	private void updateStrategyAdditionalPlans(Strategy strategy, List<StrategyDefaultPlan> lifeDisabilityDefaultPlans) {
		List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(strategy.getId(),
				BSSApplicationConstants.STATUS_ACTIVE);
		for (BenefitGroup benefitGroup : benefitGroups) {
			List<PlanSelection> addPlanSelections = ProspectStrategyServiceHelper
					.createAdditionalPlanSelections(strategy.getId(), benefitGroup.getId(), lifeDisabilityDefaultPlans);
			planSelectionService.saveAll(addPlanSelections);
		}
	}

	private void updateContributionsForBandCodeChanges(
			Map<String, Map<String, Map<String, BenefitPlan>>> getBenefitPlanContributions, Company company,
			Map<String, List<BenefitPlanRate>> planRates, Map<String, XbssRealmPlyrPlan> plyrPlanMap, Strategy strategy,
			Map<String, Map<String, String>> defaultPlanMap, List<BenefitGroupStrategy> benefitGroupStrategies,
			Map<String, Boolean> benOfferExceptions, int acaFplOpted) {
		String fundingType = "";
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = realmDataDao
				.getStrategyFundingDetails(strategy.getId());

		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		Map<String, Set<PlanCarrier>> portfolioMap = portfolioRuleDao.getStrategyPortfolios(strategy.getId(),
				company.getRealmPlanYearId(), defaultPlanMap, company.getHeadQuatersState(), isPickChoose );

		// Set the carrierId -> minimum funding map to benefitOffers so that UI can use
		// it for min funding calculation.
		List<CarrierMinimumFunding> minFundings = benefitPlanService.getLowestCostPlanPerCarrier(company);

		// Get exclusion plan list to use lowest cost client selected plan not mandated
		// plans
		// unless client HQ is in mandated State.
		List<String> mandatoryPlansToExclude = realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(
				company.getHeadQuatersState(), BigDecimal.valueOf(company.getRealmPlanYearId()));

		for (String benefitProgram : getBenefitPlanContributions.keySet()) {
			List<String> fplMedicalPlans = new ArrayList<>();
			List<Contribution> updatedContributions = new ArrayList<>();
			Map<String, Map<String, Object>> planTypeFundingDetails = null;
			String groupType = null;
			for (BenefitGroupStrategy bgs : benefitGroupStrategies) {
				if (benefitProgram.equals(bgs.getBenefitGroup().getBenefitProgram())) {
					groupType = bgs.getBenefitGroup().getType();
				}
			}
			if (null != groupFundingDetails && null != groupFundingDetails.get(benefitProgram)) {
				planTypeFundingDetails = groupFundingDetails.get(benefitProgram);
				RenewalServiceHelper.updateFundingDetailsForBasePlan(planTypeFundingDetails, planRates, company, null,
						realmDataDao, null, benOfferExceptions);
			}
			for (String planType : getBenefitPlanContributions.get(benefitProgram).keySet()) {
				Map<String, Object> coverageLevelFunding = null;
				if(null != planTypeFundingDetails) {
					coverageLevelFunding = planTypeFundingDetails.get(planType);
				}
				for (String benefitPlan : getBenefitPlanContributions.get(benefitProgram).get(planType).keySet()) {
					List<Contribution> planContributions = new ArrayList<>();
					BenefitPlan benefitPlanNew = getBenefitPlanContributions.get(benefitProgram).get(planType)
							.get(benefitPlan);
					PlanSelection planSelection = new PlanSelection();
					planSelection.setId(benefitPlanNew.getPlanSelectionId());
					planSelection.setBenefitPlan(benefitPlan);
					if (company.isRenewalCompany()) {
						boolean costSharePlanOverrride = false;
						if (!StrategyTypesEnums.F_S.getValue().equals(strategy.getCostShareType())) {
							costSharePlanOverrride = RenewalServiceHelper.validateCostSharePlanFunding(benefitPlanNew);
						}
						if ((StrategyTypesEnums.F_S.getValue().equals(strategy.getCostShareType())
								|| BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType) || costSharePlanOverrride)
								&& null != coverageLevelFunding && !coverageLevelFunding.isEmpty()) {
							if (null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE)) {
								fundingType = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
							}
							planContributions = RenewalServiceHelper.createUpdateContributionsByBaseFunding(
									benefitPlanNew, planSelection, planRates, null, coverageLevelFunding, null,
									true);
						} else {
							planContributions = RenewalServiceHelper.updateContributionsByPercentIncrease(
									benefitPlanNew, planSelection, StrategyTypesEnums.getCodeByValue(strategy.getCostShareType()), planRates);
						}
					} else {
						if (MapUtils.isNotEmpty(coverageLevelFunding)) {
							planContributions = RenewalServiceHelper.createUpdateContributionsByBaseFunding(
									benefitPlanNew, planSelection, planRates, null, coverageLevelFunding, null,
									true);

							// updating the new company flow head
							for (PlanContribution pc : benefitPlanNew.getContributions()) {
								for (Contribution c : planContributions) {
									if (CoverageCodesEnums.valueOfCode(pc.getType()).equals(c.getCoverageLevel())) {
										c.setHeadCount(pc.getHeadcount());
									}
								}
							}
						}
					}
					if (BSSApplicationConstants.FPL.equals(benefitPlanNew.getPlanCategory())) {
						fplMedicalPlans.add(benefitPlanNew.getId());
					}
					updatedContributions.addAll(planContributions);
					benefitPlanContributions.put(benefitPlanNew.getId(), planContributions);
				}
			}

			Map<String, List<String>> selectedPlansByRegion = null;
			if (null != benefitPlanContributions && !benefitPlanContributions.isEmpty()) {
				selectedPlansByRegion = realmDataDao.getSelectedPlansByRegion(company.getRealmPlanYearId(),
						benefitPlanContributions.keySet());
			}
			Map<String, Set<Long>> medicalPlanCarriers = new HashMap<>();
			medicalPlanCarriers.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, new HashSet<>());
			if (MapUtils.isNotEmpty(portfolioMap) && null != portfolioMap.get(BSSApplicationConstants.MEDICAL)) {
				for (PlanCarrier planCarrier : portfolioMap.get(BSSApplicationConstants.MEDICAL)) {
					medicalPlanCarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
							.add(Long.valueOf(planCarrier.getId()));
				}
			}

			// finding the minimum funding
			Map<String, BigDecimal> minimumFundingMap = RenewalServiceHelper.getMinimumFunding(updatedContributions,
					mandatoryPlansToExclude, company, medicalPlanCarriers, minFundings);
			Map<String, List<String>> planRegions = RenewalServiceHelper.preparePlanToRegionMap(selectedPlansByRegion);
			// updating the plan contributions to set it to minimum funding.
			RenewalServiceHelper.updateContributionsForMinimumFunding(minimumFundingMap, updatedContributions, company,
					planRegions, benefitPlanContributions, fundingType, planTypeFundingDetails);
			

			// Applying FPL for ALE clients who did not OPT out.
			if (RenewalServiceHelper.isFplApplicable(company, acaFplOpted)) {
				Map<String, String> fplPLansByRegion = SubmitServiceHelper
						.findLowCostPpoPlanByRegion(selectedPlansByRegion, benefitPlanContributions);
				SubmitServiceHelper.setFPLForLowCostPpoPlan(updatedContributions, fplPLansByRegion, company,
						fplMedicalPlans);
			}			

			// saving all the updated contributions
			contributionService.saveAll(updatedContributions);
		}

	}

	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param realmPlanMapping
	 * @param eeErPlanMapping
	 * @param mapOfBenefitPlans
	 * @param isVendorMappingEnabled
	 * @param bssContributionsMap
	 */
	private void updateStrategyHeadCounts(Company company, Long strategyId,
			Map<String, BenefitPlan> mapOfBenefitPlans,
			Map<String, Map<String, List<Contribution>>> bssContributionsMap) {
		Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> benefitProgramHeadCountMap;
		Map<String, Map<String, String>> defaultPlans = strategyDataDao
				.getStrategyDefaultPlans(company.getRealmPlanYearId(), strategyId);
		benefitProgramHeadCountMap = getVendorMappedHeadCountMap(company, strategyId);
		// setting head counts to zero
		strategyGroupDataDao.resetStrategyPlanSelectHeadcounts(strategyId);
		strategyGroupDataDao.resetStrategyContributionHeadcounts(strategyId);
		Map<Long, Long> strategyGroupHeadCounts = headCountService.getStrategyBenefitGroupHeadCount(company,
				strategyId);
		List<BenefitGroupStrategy> bgs = strategyGroupService.getBenefitGroupStrategy(strategyId,
				BSSApplicationConstants.STATUS_ACTIVE);
		updateBenefitGroupStrategyHeadCount(strategyGroupHeadCounts, bgs);
		List<Contribution> updatedContributions = new ArrayList<>();
		for (Map.Entry<String, Map<String, Map<String, CoverageLevelHeadCount>>> entry : benefitProgramHeadCountMap
				.entrySet()) {
			Map<String, List<Contribution>> benefitPlanContributions = bssContributionsMap.get(entry.getKey());
			Map<String, Map<String, CoverageLevelHeadCount>> benefitPlanHeadcounts = entry.getValue();
			List<String> headCountsToDefault = new ArrayList<>();
			for (String bph : benefitPlanHeadcounts.keySet()) {
				if (!benefitPlanContributions.keySet().contains(bph)) {
					headCountsToDefault.add(bph);
				}
			}
			updateContributionsWithHeadCounts(updatedContributions, benefitPlanContributions, benefitPlanHeadcounts);
			// setting the head count to default plan.
			for (String toDefault : headCountsToDefault) {
				BenefitPlan toDefaultBenefitPlan = mapOfBenefitPlans.get(toDefault);
				if (null != toDefaultBenefitPlan) {
					Map<String, String> defaultplans = defaultPlans.get(toDefaultBenefitPlan.getPlanType());
					updateContributionsHeadCountForDefaultPlans(updatedContributions, benefitPlanContributions,
							benefitPlanHeadcounts, toDefault, toDefaultBenefitPlan, defaultplans);
				} else {
					logger.error(
							"Following benefit plan is missing in the selected benefit plan list for the company which has head counts : {}",
							toDefault);
				}
			}
		}
		contributionService.saveAll(updatedContributions);
	}
	
	private Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> getVendorMappedHeadCountMap(Company company,
			Long strategyId) {
		Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> benefitProgramHeadCountMap;
		Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio = strategyGroupDataDao
				.getStrategyPortfoliosByPlanType(strategyId);
		if (company.isProspectCompany() || company.isProspectConvertedOnboardingClient()) {
			benefitProgramHeadCountMap = portfolioHeadCountDataDao.getProspectBenefitProgramHeadCounts(strategyId,
					benefitGroupPlanTypePortfolio);
		} else {
			benefitProgramHeadCountMap = portfolioHeadCountDataDao.getBenefitProgramHeadCounts(strategyId,
					benefitGroupPlanTypePortfolio);
		}
		return benefitProgramHeadCountMap;
	}
	
	

	private void updateContributionsHeadCountForDefaultPlans(List<Contribution> updatedContributions,
			Map<String, List<Contribution>> benefitPlanContributions,
			Map<String, Map<String, CoverageLevelHeadCount>> benefitPlanHeadcounts, String toDefault,
			BenefitPlan toDefaultBenefitPlan, Map<String, String> defaultplans) {
		if (null != defaultplans && !defaultplans.isEmpty()) {
			String defaultPlan = (String) defaultplans.values().toArray()[0];
			Map<String, CoverageLevelHeadCount> hc = benefitPlanHeadcounts.get(toDefault);
			for (Contribution c : benefitPlanContributions.get(defaultPlan)) {
				if (null != hc.get(c.getCoverageLevel())) {
					long aggHeadCount = c.getHeadCount();
					aggHeadCount = aggHeadCount + hc.get(c.getCoverageLevel()).getHeadCount();
					long aggHsaHeadCount = c.getHsaHeadCount();
					aggHsaHeadCount = aggHsaHeadCount + hc.get(c.getCoverageLevel()).getHsaHeadCount();
					c.setHeadCount(aggHeadCount);
					if (toDefaultBenefitPlan.isHighDeductible()) {
						c.setHsaHeadCount(aggHsaHeadCount);
					}
					updatedContributions.add(c);
				}
			}
		} else {
			logger.error("There is no default plan found to assigne headcounts : {}", toDefault);
		}
	}

	private void updateBenefitGroupStrategyHeadCount(Map<Long, Long> strategyGroupHeadCounts,
			List<BenefitGroupStrategy> bgs) {
		for (BenefitGroupStrategy benefitGroupStrategy : bgs) {
			if (null != strategyGroupHeadCounts.get(benefitGroupStrategy.getId())) {
				benefitGroupStrategy.setHeadcount(strategyGroupHeadCounts.get(benefitGroupStrategy.getId()));
			} else {
				benefitGroupStrategy.setHeadcount(0);
			}
			strategyGroupService.saveBenefitGroupStrategy(benefitGroupStrategy);
		}
	}
	
	private void updateContributionsWithHeadCounts(List<Contribution> updatedContributions,
			Map<String, List<Contribution>> benefitPlanContributions,
			Map<String, Map<String, CoverageLevelHeadCount>> benefitPlanHeadcounts) {
		for (Entry<String, List<Contribution>> benefitPlanEntry : benefitPlanContributions.entrySet()) {
			Map<String, CoverageLevelHeadCount> hc = benefitPlanHeadcounts.get(benefitPlanEntry.getKey());
			if (null != hc) {
				for (Contribution c : benefitPlanEntry.getValue()) {
					if (null != hc.get(c.getCoverageLevel())) {
						c.setHeadCount(hc.get(c.getCoverageLevel()).getHeadCount());
						c.setHsaHeadCount(hc.get(c.getCoverageLevel()).getHsaHeadCount());
						updatedContributions.add(c);
					}
				}
			}

		}
	}


	/**
	 * 
	 * @param company
	 * @param strategy
	 */
	private void updateStrategyPlansforPlanYearOrSitusChanges(Company company, Strategy strategy,
			Map<String, Boolean> benOfferExceptions, boolean isSitusChange) {
		long strategyId = strategy.getId();
		List<String> portfolios = strategyGroupDataDao.getMedStrategyPortfolios(strategyId);
		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company,
				new HashSet<>(portfolios), realmDataDao);
		Map<String, Map<String, Set<Long>>> selectedPlancarriers = strategyGroupDataDao
				.getStrategyPortfoliosByPlanType(strategyId);

		Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap = new HashMap<>();
		Map<Long, List<String>> existingDentalAndVisionPlanTypesByGroup = new HashMap<>();
		if (!isSitusChange) {
			healthBenefitPlansMap = getHealthBenefitPlansMap(company);
			existingDentalAndVisionPlanTypesByGroup = getExistingDenAndVisPlanTypesByGroup(strategy.getId());
		}
		deletePlanSelectionsAndContributionsForStrategy(strategy.getId(), isSitusChange);
		List<CarrierMinimumFunding> minFundings = benefitPlanService.getLowestCostPlanPerCarrier(company);
		List<String> strategyMissingPlans = strategyGroupDataDao.getStrategyPortfolioMissingPlans(strategyId, company,
				new HashSet<>(portfolios), outOfRegionPlans);
		List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(strategyId,
				BSSApplicationConstants.STATUS_ACTIVE);
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = realmDataDao
				.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, company.getRealmPlanYear().getId());
		Map<String, List<BenefitPlanRate>> rates = planRatesService.getBenefitPlanRatesBy(company);
		Map<String, XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService
				.getMapForRealmPlanYear(company.getRealmPlanYear().getId());
		List<GroupRuleDto> groupRuleDtoList = groupRuleService.getApplicableGroups(company, false);
		for (BenefitGroup bg : benefitGroups) {
			Map<String, PlanTypeRule> planTypeExceptionMap = BenefitGroupServiceHelper
					.getBenefitGroupPlanTypeExceptions(bg, groupRuleDtoList);
			Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
			List<PlanSelection> planSelections = new ArrayList<>();
			String benefitProgram = bg.getBenefitProgram();
			Map<String, Map<String, Map<String, Object>>> groupFundingDetails = realmDataDao
					.getStrategyFundingDetails(strategy.getId());
			Map<String, Map<String, Object>> fundingDetailsMap = null;
			List<StrategyFundingModel> strategyFunding = null;
			if (null != groupFundingDetails && null != groupFundingDetails.get(benefitProgram)
					&& (!BSSApplicationConstants.K1_GROUP_TYPE.equals(bg.getType())
							|| !planTypeExceptionMap.isEmpty())) {
				fundingDetailsMap = groupFundingDetails.get(benefitProgram);
				RenewalServiceHelper.updateFundingDetailsForBasePlan(fundingDetailsMap, rates, company, null,
						realmDataDao, null, benOfferExceptions);

			} else if (BSSApplicationConstants.K1_GROUP_TYPE.equals(bg.getType())) {
				strategyFunding = new ArrayList<>();
				fundingDetailsMap = RenewalServiceHelper.createK1Funding(strategyFunding, strategyId, 1L,
						mapOfCoverageLevels, benOfferExceptions);
			}
			if (CollectionUtils.isNotEmpty(strategyMissingPlans)) {
				for (String benefitPlan : strategyMissingPlans) {
					XbssRealmPlyrPlan planYearPlan = plyrPlanMap.get(benefitPlan);
					BenefitPlan bp = StrategyServiceHelper.populateBenefitPlan(benefitPlan,
							BSSApplicationConstants.MEDICAL_PLAN_TYPE, 0L, planYearPlan.isWidelyAvailable(),
							planYearPlan.getPlanCategory(), null);
					RenewalServiceHelper.addBlankContributions(bp,
							mapOfCoverageLevels.get(BSSApplicationConstants.MEDICAL));
					benefitPlanMap.put(benefitPlan, bp);
					PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId, bg.getId(),
							bp, 0L);
					planSelections.add(planSelection);
				}
			}

			if(!isSitusChange) {
				List<PlanSelection> dentalVisionPlanSelections = ProspectStrategyServiceHelper.createDentalAndVisionPlanSelections(
						healthBenefitPlansMap, mapOfCoverageLevels, strategyId, bg.getId(), benefitPlanMap,
						existingDentalAndVisionPlanTypesByGroup.get(bg.getId()));
				if (!dentalVisionPlanSelections.isEmpty()) {
					planSelections.addAll(dentalVisionPlanSelections);
				}
			}

			// Plan selections are not available for M/D/V for OMS and may also be
			// unavailable for TN XI when the user selects 'Do Not Offer' for D/V.
			if (CollectionUtils.isNotEmpty(planSelections)) {
				planSelections = planSelectionService.saveAll(planSelections);
				createContributionForNewPlans(company, benefitPlanMap, planSelections, rates, fundingDetailsMap,
						bg.getType(), null, plyrPlanMap, selectedPlancarriers.get(bg.getBenefitProgram()), minFundings,
						strategy.getAcaFplOpted());
			}
		}

	}
	
	private Map<String, Set<StateBenefitPlan>> getHealthBenefitPlansMap(Company company) {
		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioService.findPrimaryPlanCarriers(company);
		Set<String> primaryPlanCarriers = new HashSet<>();
		Stream.of(BSSApplicationConstants.DENTAL, BSSApplicationConstants.VISION).map(planCarrierMap::get)
				.filter(Objects::nonNull).flatMap(Set::stream).map(pc -> String.valueOf(pc.getId()))
				.forEach(primaryPlanCarriers::add);

		return benefitPlanDao.getAllPrimaryBenefitPlans(primaryPlanCarriers, company, new HashSet<>());
	}

	private Map<Long, List<String>> getExistingDenAndVisPlanTypesByGroup(long strategyId) {
		List<PlanSelection> existingPlanSelections = planSelectionService.getPlansByStrategyId(strategyId);
		return existingPlanSelections.stream()
				.filter(planSelection -> BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planSelection.getPlanType())
						|| BSSApplicationConstants.VISION_PLAN_TYPES.contains(planSelection.getPlanType()))
				.collect(Collectors.groupingBy(PlanSelection::getGroupId, Collectors.mapping(PlanSelection::getPlanType,
						Collectors.collectingAndThen(Collectors.toSet(), ArrayList::new))));
	}

	private void deletePlanSelectionsAndContributionsForStrategy(long strategyId, boolean isSitusChange) {
		if (isSitusChange) {
			strategyDataDao.deleteAllPlanContributionsBy(Collections.singleton(strategyId),
					BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			strategyDataDao.deleteAllPlanSelectionsBy(Collections.singleton(strategyId),
					BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		} else {
			strategyDataDao.deleteAllPlanContributionsByStrategy(Collections.singleton(strategyId));
			strategyDataDao.deleteAllPlanSelectionsByStrategy(Collections.singleton(strategyId));
		}
	}

	private void createContributionForNewPlans(Company company, Map<String, BenefitPlan> benefitPlanMap,
			List<PlanSelection> planSelections, Map<String, List<BenefitPlanRate>> rates,
			Map<String, Map<String, Object>> groupFundingDetails, String groupType,
			Map<String, Map<String, String>> planOverrides, Map<String, XbssRealmPlyrPlan> plyrPlanMap,
			Map<String, Set<Long>> selectedPlancarriers, List<CarrierMinimumFunding> minFundings, int acaFplOpted) {
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();
		String fundingType = "";
		List<String> fplMedicalPlans = new ArrayList<>();
		for (PlanSelection ps : planSelections) {
			BenefitPlan bp = benefitPlanMap.get(ps.getBenefitPlan());
			Map<String, Object> coverageLevelFunding = getCoverageLevelFunding(groupFundingDetails, bp.getPlanType());
			if (null != coverageLevelFunding && !coverageLevelFunding.isEmpty()) {
				if (null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE)) {
					fundingType = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
				}
				List<Contribution> contribList = RenewalServiceHelper.createUpdateContributionsByBaseFunding(
						bp, ps, rates, null, coverageLevelFunding, planOverrides, false);
				contributions.addAll(contribList);
			} else {
				List<Contribution> contribList = new ArrayList<>();
				RenewalServiceHelper.constructContributionsByPercentIncrease(bp, ps, null, rates, null,
						contribList, planOverrides);
				contributions.addAll(contribList);
			}
			if (Constants.MEDICAL_CODE.equals(bp.getPlanType())) {
				benefitPlanContributions.put(bp.getId(), contributions);
				if (BSSApplicationConstants.FPL.equals(bp.getPlanCategory())) {
					fplMedicalPlans.add(bp.getId());
				}
			}
			
		}
		Map<String, List<String>> selectedPlansByRegion = getSelectedPlansByRegion(company, benefitPlanContributions);

		List<String> mandatoryPlansToExclude = realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(
				company.getHeadQuatersState(), BigDecimal.valueOf(company.getRealmPlanYearId()));
		// finding the minimum funding
		Map<String, BigDecimal> minimumFundingMap = RenewalServiceHelper.getMinimumFunding(contributions, mandatoryPlansToExclude, company,
				selectedPlancarriers, minFundings);

		Map<String, List<String>> planRegions = null;
		if (null != selectedPlansByRegion) {
			planRegions = RenewalServiceHelper.preparePlanToRegionMap(selectedPlansByRegion);
		}
		// updating the plan contributions to set it to minimum funding.
		RenewalServiceHelper.updateContributionsForMinimumFunding(minimumFundingMap, contributions, company,
				planRegions, benefitPlanContributions, fundingType, groupFundingDetails);
		// Applying FPL for ALE clients who did not OPT out.
		if (RenewalServiceHelper.isFplApplicable(company, acaFplOpted)) {
			Map<String, String> fplPLansByRegion = SubmitServiceHelper.findLowCostPpoPlanByRegion(selectedPlansByRegion,
					benefitPlanContributions);
			SubmitServiceHelper.setFPLForLowCostPpoPlan(contributions, fplPLansByRegion, company, fplMedicalPlans);
		}

		// saving contribution data.
		contributionService.saveAll(contributions);
	}

	private Map<String, List<String>> getSelectedPlansByRegion(Company company,
			Map<String, List<Contribution>> benefitPlanContributions) {
		Map<String, List<String>> selectedPlansByRegion = null;
		if (null != benefitPlanContributions && !benefitPlanContributions.isEmpty()) {
			selectedPlansByRegion = realmDataDao.getRegionForSelectedPlans(benefitPlanContributions.keySet(),
					company.getRealmPlanYearId());
		}
		return selectedPlansByRegion;
	}

	private Map<String, Object> getCoverageLevelFunding(Map<String, Map<String, Object>> groupFundingDetails,
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

	private boolean isMinFundExceptionUpdated(Set<MinFundExceptionDto> minFundExceptions, Strategy strategy) {
		boolean result = false;
		for (MinFundExceptionDto minFundExceptionDto : minFundExceptions) {
			if (strategy.getUpdateTime().compareTo(minFundExceptionDto.getCreateTime()) < 0) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	private void syncDissimilarSitusStrategies(Company company, List<Strategy> strategyList) {
		List<Long> strategyIds = strategyList.stream().map(Strategy::getId).collect(Collectors.toList());

		List<StrategySitusDetail> strategySitusDetailList = strategyGroupPlanSelectDao.getStrategiesSitus(strategyIds,
				company.getRealmPlanYearId());

		Set<Long> dissimilarSitusStrategyIds = strategySitusDetailList.stream()
				.filter(detail -> Boolean.compare(company.isTexasSitus(), detail.getIsTexasSitus()) != 0)
				.map(StrategySitusDetail::getStrategyId).collect(Collectors.toSet());

		if (!dissimilarSitusStrategyIds.isEmpty()) {
			logger.info("Process strategies for situs change for ids : {}", dissimilarSitusStrategyIds);
			updateSitusStrategies(company, strategyList, dissimilarSitusStrategyIds);
		}
	}

	private void updateSitusStrategies(Company company, List<Strategy> strategyList,
			Set<Long> dissimilarSitusStrategyIds) {
		RealmPlanYear realmPlanYear = company.getRealmPlanYear();
		Map<String, String> currentFuturePlansMap = hrpDao.getCurrentFutureBenefitPlansMap(realmPlanYear.getId(),
				realmPlanYear.getPlanYearEnd(), realmPlanYear.getId(), realmPlanYear.getPlanYearEnd(), true);

		Map<String, Boolean> offerExceptions = benOfferExceptionService.findApplicableBy(company);

		if (!company.isRenewalCompany()) {
			createEeDefaultAssignmentForNewCompany(company.getId(), company);
		}

		strategyList.stream().filter(strategy -> dissimilarSitusStrategyIds.contains(strategy.getId()))
				.forEach(strategy -> {
					updateStrategyPlansforPlanYearOrSitusChanges(company, strategy, offerExceptions, true);
					if (currentFuturePlansMap != null) {
						updateEePlanAssignmentWithNewBenefitPlans(currentFuturePlansMap, strategy, company);
						updateStrategyLimitPlanWithNewBenefitPlans(currentFuturePlansMap, strategy);
					}
				});
	}

	private void updateRateGroupId(Company company, Map<String, List<BenefitPlanRate>> planRates) {
		RiskTypeEnum riskType = company.getRiskType();
		String currentRateGroupId = company.getRateGroupId();
		if (riskType == RiskTypeEnum.BANDS && currentRateGroupId != null) {
			updateCompanyRateGroupId(company, null);
			return;
		}
		if (riskType == RiskTypeEnum.DIFFERENTIALS) {
			String newRateGroupId = planRates.values().stream()
					.filter(list -> list != null && !list.isEmpty())
					.map(list -> list.get(0))
					.filter(Objects::nonNull)
					.map(BenefitPlanRate::getRateGroupId)
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
			if (newRateGroupId == null) {
				throw new IllegalStateException("RateGroupId is not found for DIFFERENTIALS risk type for company: "+ company.getCode());
			}
			if (!newRateGroupId.equals(currentRateGroupId)) {
				updateCompanyRateGroupId(company, newRateGroupId);
			}
		}
	}

	private void updateCompanyRateGroupId(Company company, String newRateGroupId) {
		company.setRateGroupId(newRateGroupId);
		companyDao.saveAndFlush(company);
	}
}