package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.trinet.ambis.service.BenefitPlanService;
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
import com.trinet.ambis.common.BSSRateType;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.helper.AdditionalBenefitServiceHelper;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.PlanOverrideServiceHelper;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.helper.SubmitServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.HeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDefaultPlanDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDetailDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.BenefitPlanDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.persistence.sp.GetNextEligRulesId;
import com.trinet.ambis.persistence.sp.NextBenProgram;
import com.trinet.ambis.persistence.sp.NextRateTblID;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitClassService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.EmployerEmployeePlansMappingService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyFundingDetailService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyHsaFundingService;
import com.trinet.ambis.service.StrategyRenewalService;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.GroupRuleDto;
import com.trinet.ambis.service.model.GroupRuleDto.PlanTypeRule;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author rvutukuri
 *
 */
@Service
public class StrategyRenewalServiceImpl implements StrategyRenewalService {
	private static final Logger logger = LoggerFactory.getLogger(StrategyRenewalServiceImpl.class);

	@Autowired
	BenefitClassService benefitClassService;
	@Autowired
	BenefitGroupService benefitGroupService;
	@Autowired
	HeadCountService headCountService;
	@Autowired
	StrategyGroupService strategyGroupService;
	@Autowired
	CompanyService companyService;
	@Autowired
	ContributionService contributionService;
	@Autowired
	EmployeeDataService employeeDataService;
	@Autowired
	EmployerEmployeePlansMappingService employerEmployeePlansMappingService;
	@Autowired
	PlanSelectionService planSelectionService;
	@Autowired
	RealmPlanYearRuleService realmPlanYearRuleService;
	@Autowired
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;
	@Autowired
	RealmPlyrPlanService realmPlyrPlanService;
	@Autowired
	StrategyFundingModelService strategyFundingModelService;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	PlanRatesService planRatesService;
	@Autowired
	BenefitOfferExceptionService benOfferExceptionService;
	@Autowired
	BenefitPlanDao benefitPlanDao;
	@Autowired
	BenefitPlanDataDao benefitPlanDataDao;
	@Autowired
	CompanyDao companyDao;
	@Autowired
	EmployeeSelectionDao employeeSelectionDao;
	@Autowired
	GroupRuleService groupRuleService;
	@Autowired
	GetNextEligRulesId spGetNextEligRulesId;
	@Autowired
	MandatoryRegionDao mandatoryRegionDao;
	@Autowired
	NextBenProgram nextBenProgram;
	@Autowired
	NextRateTblID nextRateTblID;
	@Autowired
	PsCompanyDao psCompanyDao;
	@Autowired
	RealmDataDao realmDataDao;
	@Autowired
	PlanMappingDao planMappingDao;
	@Autowired
	RealmPlanYearDao realmPlanYearDao;
	@Autowired
	RenewalDataDao renewalDataDao;
	@Autowired
	StrategyDao strategyDao;
	@Autowired
	StrategyDataDao strategyDataDao;
	@Autowired
	StrategyHsaFundingService strategyHsaFundingService;
	@Autowired
	XbssRealmPlyrPlanDao realmPlyrPlanDao;
	@Autowired
	DisabilityOptionService disabilityOptionService;
	@Autowired
	CommonDataDao commonDataDao;
	@Autowired
	HeadCountDao headCountDao;
	@Autowired
	StrategyFundingDetailDao strategyFundingDetailDao;
	@Autowired
	StrategyFundingDetailService strategyFundingDetailService;
	@Autowired
	CacheService cacheService;
	@Autowired
	BenefitPlanService benefitPlanService;
	@Autowired
	StrategyDefaultPlanDao strategyDefaultPlanDao;
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public Map<String, BenefitGroup> createFutureStrategies(Company company, boolean isMigratedCompany,
			RealmPlanYear realmPlanYear, boolean isDefaultSubmit,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap, boolean isPreload) {
		logger.debug("Entering method : createFutureStrategiesForMigratedBSSCompany");

		applyBenefitOfferException(company, bgsHealthPlansMap, bgsADPlansMap);

		String benefitProgram = null;
		Map<String, BenefitGroup> mapOfBenefitProgram = new HashMap<>();
		// getting the previous realm year data.
		RealmPlanYear previousYearRealm = realmPlanYearDao.findPreviousRealmPlanYearByRealmIdAndOeQuarter(
				company.getRealmPlanYearId(), company.getRealm().getId(), company.getQuater());

		List<BenefitGroup> benefitGroups = renewalDataDao.getBenefitPrograms(company.getPfClient(),
				previousYearRealm.getPlanYearEnd());

		// Add commuter benefit plans to TN IV clients
		addCommuterPlansIfApplicable(company, bgsADPlansMap, benefitGroups);

		// creating last year company for handling Plan rates - Need to remove
		// this after code clean up in plan rates.
		Company previousYearCompany = companyDao.findByCodeAndRealmPlanYearId(company.getCode(),
				previousYearRealm.getId());
		if (isMigratedCompany && null == previousYearCompany) {
			previousYearCompany = StrategyServiceHelper.constructXbssCompany(company, previousYearRealm.getId());
			companyService.createUpdateCompany(previousYearCompany);
		}
		
		// creating the company.
		company.setUpdateTime(new Date());
		company.setBundleId(previousYearCompany.getBundleId());
		company.setProspectId(previousYearCompany.getProspectId());
		company.setOmsOffering(previousYearCompany.getOmsOffering());
		companyService.createUpdateCompany(company);
		
		company.setAcaLargeEmplrStatusUpdated(previousYearCompany.isAcaLargeEmplr() ^ company.isAcaLargeEmplr());
		
		// getting funding details
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = realmDataDao
				.getRenewalFundingDetailsBSS(company.getCode(), previousYearRealm.getId());

		// group head-count details
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = headCountDao
				.getPlanCoverageLevelHeadCountByGroup(company.getCode(), company.getRealmPlanYearId(), Boolean.TRUE);

		Map<String, String> erEeMapping = employerEmployeePlansMappingService
				.getEeAndErPlanMapping(previousYearRealm.getId());

		Map<String, List<CoverageLevel>> mapOfCoverageLevels = realmDataDao
				.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, company.getRealmPlanYear().getId());

		RenewalServiceHelper.updateGroupDVPlansBsupp(groupFundingDetails, bgsHealthPlansMap, mapOfCoverageLevels,
				erEeMapping);

		// getting realm rule configurations
		Map<String, String> realmRuleConfigurations = realmPlanYearRuleConfigService
				.getRulesAndConfigsByRealmPlanYearId(company.getRealmPlanYearId());
		realmRuleConfigurations.putAll(
				realmPlanYearRuleConfigService.getPsConfigsByDate(company.getRealmPlanYear().getPlanYearStart()));

		// getting the current submitted strategy from BSS.
		List<Strategy> strategies = strategyDao.findByCompanyIdAndSubmitted(previousYearCompany.getId(), true);
		Strategy previousSubmittedStrategy = new Strategy();
		if (null != strategies && !strategies.isEmpty()) {
			previousSubmittedStrategy = strategies.get(0);
		}
		
		int acaFplOpted = previousSubmittedStrategy.getAcaFplOpted();
		
		if (company.isAcaLargeEmplrStatusUpdated()
				|| BenExchngEnums.TRINET_III.getBenExchng().equals(company.getRealm().getBenExchange())) {
			acaFplOpted = BSSApplicationConstants.ACA_FPL_OPTED_IN;
		}
		
		List<Strategy> renewalStrategies = RenewalServiceHelper.constructRenewalStrategies(company, isDefaultSubmit,
				realmRuleConfigurations, isPreload, acaFplOpted);

		renewalStrategies = strategyDao.saveAll(renewalStrategies);

		strategyHsaFundingService.createFutureStrategyHsaFunding(renewalStrategies, company, realmRuleConfigurations);

		// this is used to get the head counts for groups based on the employee
		// data.
		Map<String, Integer> groupHeadCountMap = headCountService.getEmployeeHeadcountByBenefitGroup(company,
				company.getRealmPlanYear().getId(), company.getRealmPlanYear().getPlanYearStart());

		// getting EligRuleId's for the company
		Map<String, String> eligRuleIdMap = renewalDataDao.getEligRuleIdsByClient(company.getPfClient(),
				previousYearRealm.getPlanYearEnd());

		// getting WaitPeriods for the company
		Map<String, String> waitPeriodMap = renewalDataDao.getWaitPeriodByClient(company.getPfClient(),
				company.getCode(), previousYearRealm.getPlanYearEnd());

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService
				.getMapForRealmPlanYear(company.getRealmPlanYear().getId());

		// Set the carrierId -> minimum funding map to benefitOffers so that UI can use
		// it for min funding calculation.
		List<CarrierMinimumFunding> minFundings =  benefitPlanService.getLowestCostPlanPerCarrier(company);

		// Get all benefitPlans by planTypes for realmYearId for this company's
		// applicable regions.
		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioService.findPrimaryPlanCarriers(company);
		Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);

		Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap = benefitPlanDao
				.getAllPrimaryBenefitPlans(primaryPlanCarriers, company, outOfRegionPlans);

		// need use this for finding the mapped plans for missing plans.
		Map<String, PlanMapping> realmPlanMapping = planMappingDao.getPlanMappings(company, outOfRegionPlans);

		Map<String, PlanMapping> primaryPlanMapping = planMappingDao.getPrimaryPlanMappings(company, outOfRegionPlans);

		Map<String, Map<String, Set<String>>> autoSelectPlans = realmDataDao
				.getAutoSelectPlansByRealmIdAndPlanTypes(company.getRealmPlanYearId(), company,
						outOfRegionPlans);

		Map<String, Map<String, Map<String, String>>> groupPlanOverrideMap = strategyDataDao
				.getOverridesByBenefitGroup(company.getCode(), previousYearRealm.getId(), false);

		setupOverrides(groupFundingDetails, groupPlanOverrideMap);

		List<String> bcAdPlans = realmDataDao.getADBenefitPlans(company);

		benefitGroupService.addMandatoryBenefitGroups(company, benefitGroups, waitPeriodMap);

		Map<String, Map<String, BenefitPlan>> k1AdditionalPlans = getK1AdditionalPlans(previousYearCompany,
				bgsADPlansMap);

		String k1RateTableId = null;

		for (BenefitGroup bg : benefitGroups) {
			benefitProgram = bg.getBenefitProgram();
			Map<String, String> rateTableIds = getRateTableIdsBy(benefitProgram, previousYearRealm);

			// if this is a K1 benefit group, save the MEDICAL rate ID for additional
			// validation later
			if (BSSApplicationConstants.K1_GROUP_TYPE.equals(bg.getType())) {
				k1RateTableId = rateTableIds.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			}

			// generating the eligRuleId if not available.
			String eligRuleId = eligRuleIdMap.get(benefitProgram);
			if (null == eligRuleId) {
				eligRuleId = spGetNextEligRulesId.execute();
			}

			// populating waitPeriod, EligeRuleId, rateTableId & HeadCounts.
			RenewalServiceHelper.constructBenefitGroupForRenewalCompany(bg, company, groupHeadCountMap, rateTableIds,
					eligRuleId, waitPeriodMap);
		}

		// validating the K1 rateTable id and updating it if it is same as
		// regular group rate table Id.
		validateAndUpdateK1RateTable(benefitGroups, k1RateTableId);

		// Assign class code to benefit groups
		benefitGroups = benefitClassService.generateAllClassCodes(company, benefitGroups);

		// creating Benefit Groups.
		benefitGroups = benefitGroupService.saveAll(benefitGroups);

		// Constructing BenefitGroupStrategy and saving them.
		List<BenefitGroupStrategy> benefitGroupStrategies = RenewalServiceHelper.constructStrategyBenefitGroups(
				renewalStrategies, benefitGroups, waitPeriodMap, groupHeadCountMap, company,
				BSSApplicationConstants.STATUS_ACTIVE);
		benefitGroupStrategies = strategyGroupService.saveBenefitGroupStrategies(benefitGroupStrategies);

		// getting the strategyGroup mapping by benefit program
		Map<String, Set<Long>> strategyGroupBenefitProgramMap = RenewalServiceHelper
				.getStrategyGroupByBenefitProgram(benefitGroupStrategies);

		planSelectionService.addRequiredDentalVisionPlans(company, benefitGroups, bgsHealthPlansMap,
				mapOfCoverageLevels);

		// getting all distinct health plans
		Map<String, Map<String, BenefitPlan>> bgAllHealthPlansMap = RenewalServiceHelper
				.getHealthPlansForAllBenefitGroups(bgsHealthPlansMap, erEeMapping, mapOfCoverageLevels);

		// determining all the selected plan carriers along with mandatory
		// carriers
		Map<String, Set<Long>> selectedPlancarriers = RenewalServiceHelper.getSelectedPlanCarriers(bgAllHealthPlansMap,
				allBenefitStatePlansMap, planCarrierMap, primaryPlanMapping);

		// adding missing child carriers for the parent carrier
		RenewalServiceHelper.addMissingChildCarriers(planCarrierMap, selectedPlancarriers);

		// getting mandatory plans based on current plans.
		Map<String, StateBenefitPlan> mandatoryPlans = RenewalServiceHelper.getAllMandatoryPlans(company,
				allBenefitStatePlansMap, selectedPlancarriers);

		List<String> fplMedicalPlans = RenewalServiceHelper.getFplPlans(allBenefitStatePlansMap, mandatoryPlans,
				selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE));

		// adding the non selected plans for selected carriers.
		if (!CompanyServiceHelper.isBundledCompany(company)) {
			boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions(company);
			if (!isPickChoose) {
				RenewalServiceHelper.addMandatoryPortfolioPlans(mandatoryPlans, bgAllHealthPlansMap,
						allBenefitStatePlansMap, selectedPlancarriers);
			}
		}

		// removing the plans of carriers that are not selected for medical.
		RenewalServiceHelper.updateMedicalPlans(selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE),
				allBenefitStatePlansMap);

		// getting all medical plans.
		List<String> medPlans = RenewalServiceHelper.getBCPlansByType(allBenefitStatePlansMap, Constants.MEDICAL);

		// Rate for the plans selected by the company
		Map<String, List<BenefitPlanRate>> rates = planRatesService.getBenefitPlanRatesBy(company);

		List<GroupRuleDto> groupRuleDtoList = groupRuleService.getApplicableGroups(company, false);

		Map<String, Boolean> benOfferExceptions = benOfferExceptionService.findApplicableBy(company);
		for (Strategy rs : renewalStrategies) {
			groupFundingDetails = realmDataDao.getRenewalFundingDetailsBSS(company.getCode(),
					previousYearRealm.getId());

			// creating the future & custom strategies data
			mapOfBenefitProgram = createStrategiesRenewalCompany(benefitGroups, groupFundingDetails, company,
					rs.getId(), rates, mapOfCoverageLevels, bgAllHealthPlansMap, bgsHealthPlansMap, autoSelectPlans,
					medPlans, StrategyTypesEnums.getCodeByValue(rs.getCostShareType()), bgsADPlansMap, bcAdPlans,
					mandatoryPlans, groupPlanOverrideMap, plyrPlanMap, selectedPlancarriers, minFundings,
					previousYearRealm, fplMedicalPlans, groupRuleDtoList, benOfferExceptions, k1AdditionalPlans,
					realmPlanMapping, primaryPlanMapping, groupCovrgHeadCountMap, rs);

		}
		
		// clearing the cache since the plan and rates object created from here impacts
		// benefit categories
		cacheService.invalidateOutofDateCache(company);
		
		// setting data into employee table.
		if (company.getRealmPlanYear().isMbgRenewal()) {
			insertEmployeeData(company, mapOfBenefitProgram, strategyGroupBenefitProgramMap);
		}
		return mapOfBenefitProgram;
	}

	private void addCommuterPlansIfApplicable(Company company,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap, List<BenefitGroup> benefitGroups) {
		if (BenExchngEnums.TRINET_IV.getBenExchng().equals(company.getRealm().getBenExchange())) {
			XbssRealmPlyrPlan commuterPlan = realmPlyrPlanService
					.getCommuterPlanForRealmPlanYear(company.getRealmPlanYearId());
			if (commuterPlan != null) {
				AdditionalBenefitServiceHelper.addCommuterBenefitPlan(bgsADPlansMap, commuterPlan, benefitGroups);
			}
		}
	}
	
	private Map<String, String> getRateTableIdsBy(String benefitProgram, RealmPlanYear previousYearRealm) {
		Map<String, String> rateTableIds = renewalDataDao.getRateTableIds(benefitProgram,
				previousYearRealm.getPlanYearEnd());

		// make sure each rate ID is used for only one rate type
		// if duplicates are found, they are removed here and new ones will be assigned
		// below
		for (Map.Entry<String, String> entry : rateTableIds.entrySet()) {
			for (Map.Entry<String, String> other : rateTableIds.entrySet()) {
				if (other != entry && other.getValue().equals(entry.getValue())) {
					other.setValue(BSSApplicationConstants.EMPTY_SPACE);
				}
			}
		}

		// make sure each rateType has a valid RATE_TBL_ID and assign one if it doesn't
		for (BSSRateType rateType : BSSRateType.getHealthRateSet()) {
			String rateTblId = rateTableIds.get(rateType.rateIdType());
			if (!BSSApplicationConstants.EMPTY_SPACE.equals(rateTblId)
					&& realmDataDao.validateRateTableId(rateTblId, benefitProgram)) {
				// this rate table ID is valid
			} else {
				// assign a new rate table ID for this rate type key
				rateTblId = nextRateTblID.execute();
				rateTableIds.put(rateType.rateIdType(), rateTblId);
			}
		}
		return rateTableIds;
	}
	
	private void validateAndUpdateK1RateTable(List<BenefitGroup> benefitGroups, String k1RateTableId) {
		if (null != k1RateTableId) {
			boolean k1HasSameRateTableId = RenewalServiceHelper.validateK1RateTableId(benefitGroups, k1RateTableId);
			if (k1HasSameRateTableId) {
				Map<String, String> updatedRateTableIds = benefitGroupService.generateRateTableId();
				RenewalServiceHelper.updateK1RateTableId(benefitGroups, updatedRateTableIds);
			}
		}
	}

	/**
	 * @param benefitGroups
	 * @param groupFundingDetails
	 * @param company
	 * @param strategyId
	 * @param rates
	 * @param realmPlanMapping
	 * @param mapOfCoverageLevels
	 * @param bgAllHealthPlansMap
	 * @param bgsHealthPlansMap
	 * @param autoSelectPlans
	 * @param medPlans
	 * @param costShare
	 * @param bgsADPlansMap
	 * @param bcAdPlans
	 * @param mandatoryPlans
	 * @param groupPlanOverrrideMap
	 * @param plyrPlanMap
	 * @param selectedPlancarriers
	 * @param minFundings
	 * @param previousYearRealm
	 * @param fplMedicalPlans
	 * @param groupRuleDtoList
	 * @return
	 */
	private Map<String, BenefitGroup> createStrategiesRenewalCompany(List<BenefitGroup> benefitGroups,
			Map<String, Map<String, Map<String, Object>>> groupFundingDetails, Company company, Long strategyId,
			Map<String, List<BenefitPlanRate>> rates, Map<String, List<CoverageLevel>> mapOfCoverageLevels,
			Map<String, Map<String, BenefitPlan>> bgAllHealthPlansMap,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, Map<String, Set<String>>> autoSelectPlans, List<String> medPlans, BigDecimal costShare,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap, List<String> bcAdPlans,
			Map<String, StateBenefitPlan> mandatoryPlans,
			Map<String, Map<String, Map<String, String>>> groupPlanOverrideMap,
			Map<String, XbssRealmPlyrPlan> plyrPlanMap, Map<String, Set<Long>> selectedPlancarriers,
			List<CarrierMinimumFunding> minFundings, RealmPlanYear previousYearRealm, List<String> fplMedicalPlans,
			List<GroupRuleDto> groupRuleDtoList, Map<String, Boolean> benOfferExceptions,
			Map<String, Map<String, BenefitPlan>> k1AdditionalPlans, Map<String, PlanMapping> realmPlanMapping,
			Map<String, PlanMapping> primaryPlanMapping,
			Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap, Strategy rs) {

		Map<String, BenefitGroup> mapOfBenefitProgram = new HashMap<>();

		// Creating Plan selections and contributions for the Strategy & Benefit
		// Group.
		for (BenefitGroup bg : benefitGroups) {
			String benefitProgram = bg.getBenefitProgram();
			Long benefitGroupId = bg.getId();
			logger.info("Created BenefitGroupId : {}", benefitGroupId);
			// creating funding for a strategy.
			Map<String, Map<String, Object>> fundingDetailsMap = null;
			List<StrategyFundingModel> strategyFunding = null;
			Map<String, PlanTypeRule> planTypeExceptionMap = BenefitGroupServiceHelper
					.getBenefitGroupPlanTypeExceptions(bg, groupRuleDtoList);
			if (null != groupFundingDetails && null != groupFundingDetails.get(benefitProgram)
					&& (!BSSApplicationConstants.K1_GROUP_TYPE.equals(bg.getType())
							|| !planTypeExceptionMap.isEmpty())) {
				fundingDetailsMap = groupFundingDetails.get(benefitProgram);
				strategyFunding = new ArrayList<>();
				List<String> supportedVoluntaryPlanTypes = commonDataDao
						.getBsuppVolBenPlanTypes(company.getRealm().getId());
				List<String> bsuppPlanTypes = renewalDataDao.getBsuppVoluntaryPlanTypes(benefitProgram,
						previousYearRealm.getPlanYearEnd(), false, supportedVoluntaryPlanTypes);
				RenewalServiceHelper.updateFundingDetailsForBasePlan(fundingDetailsMap, rates, company,
						primaryPlanMapping, realmDataDao, medPlans, benOfferExceptions);
				RenewalServiceHelper.constructRenewalStrategyFundingDetails(strategyId, bg, strategyFunding,
						fundingDetailsMap, primaryPlanMapping, false, bsuppPlanTypes, medPlans,
						planTypeExceptionMap, mapOfCoverageLevels, benOfferExceptions);
				strategyFundingModelService.saveAll(strategyFunding);
			} else if (BSSApplicationConstants.K1_GROUP_TYPE.equals(bg.getType())) {
				strategyFunding = new ArrayList<>();
				fundingDetailsMap = RenewalServiceHelper.createK1Funding(strategyFunding, strategyId, benefitGroupId,
						mapOfCoverageLevels, benOfferExceptions);
				strategyFundingModelService.saveAll(strategyFunding);
			}
			// creating plan selection & contributions for Medical, Dental &
			// Vision:
			createHPSelectionsForBenefitGroup(company, strategyId, benefitGroupId, realmPlanMapping, rates,
					bgAllHealthPlansMap, bgsHealthPlansMap.get(benefitProgram), mapOfCoverageLevels, autoSelectPlans,
					medPlans, fundingDetailsMap, costShare, mandatoryPlans, bg.getType(),
					groupPlanOverrideMap.get(bg.getBenefitProgram()), plyrPlanMap, selectedPlancarriers, minFundings,
					fplMedicalPlans, groupCovrgHeadCountMap.get(benefitProgram), primaryPlanMapping, rs);

			// Creating Additional Benefit plans for the Strategy
			createADPlanSelectionsForBenefitGroup(strategyId, bg, realmPlanMapping, bgsADPlansMap, bcAdPlans,
					k1AdditionalPlans, company);
			// benefitGroupStrategies has the strategy group id info use this
			// here.
			// for MGB head counts
			if (!mapOfBenefitProgram.containsKey(bg.getBenefitProgram())) {
				mapOfBenefitProgram.put(bg.getBenefitProgram(), bg);
			}
		}
		logger.debug("Exiting method : createFutureStrategiesForMigratedBSSCompany");
		return mapOfBenefitProgram;
	}

	/**
	 * This methods performs the initial insert of employees and employee strategy
	 * groups.
	 * 
	 * @param company
	 * @param newBenefitProgramMap
	 * @param strategyGroupBenefitProgramMap
	 */
	private void insertEmployeeData(Company company, Map<String, BenefitGroup> newBenefitProgramMap,
			Map<String, Set<Long>> strategyGroupBenefitProgramMap) {
		employeeDataService.loadEmployeeData(company, newBenefitProgramMap, strategyGroupBenefitProgramMap);
	}

	
	private void createADPlanSelectionsForBenefitGroup(Long strategyId, BenefitGroup bg,
			Map<String, PlanMapping> realmPlanMapping, Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap,
			List<String> bcAdPlans, Map<String, Map<String, BenefitPlan>> k1AdditionalPlans, Company company) {

		List<PlanSelection> planSelections = new ArrayList<>();
		Map<String, Map<String, BenefitPlan>> adPlansMap = bgsADPlansMap.get(bg.getBenefitProgram());

		// Use default plans if system-created K1 group has no AD plans
		if (bg.isSystemCreated() && (adPlansMap == null || adPlansMap.isEmpty())
				&& BSSApplicationConstants.K1_GROUP_TYPE.equals(bg.getType())) {
			adPlansMap = k1AdditionalPlans;
		}

		if (adPlansMap == null || adPlansMap.isEmpty()) {
			return;
		}

		//add default life plan only for realm plan years greater than 86.
		RealmPlanYear companyRealmPlanYear = company.getRealmPlanYear();
		if (companyRealmPlanYear != null && companyRealmPlanYear.getId() > 86L) {
			addDefaultLifePlanIfMissing(adPlansMap, company, realmPlanMapping, strategyId, bg, planSelections);
		}

		boolean stdPlansAvailable = false;
		boolean ltdPlansAvailable = false;
		List<String> disabilityPlans = new ArrayList<>();

		for (Map<String, BenefitPlan> offerPlanMap : adPlansMap.values()) {
			for (BenefitPlan bp : offerPlanMap.values()) {
				updateBenefitPlanIdIfMapped(bp, realmPlanMapping);

				if (BSSApplicationConstants.STD_CODE.equals(bp.getPlanType()))
					stdPlansAvailable = true;
				if (BSSApplicationConstants.LTD_CODE.equals(bp.getPlanType()))
					ltdPlansAvailable = true;

				if (BSSApplicationConstants.DISABILITY_PLAN_TYPES.contains(bp.getPlanType())) {
					disabilityPlans.add(bp.getId());
				} else {
					planSelections.add(StrategyServiceHelper.constructADPlanSelection(strategyId, bg.getId(),
							bp.getId(), bp.getPlanType(), 0));
				}
			}
		}

		// Handle disability plans
		if (!disabilityPlans.isEmpty()) {
			boolean isStandAlone = !(stdPlansAvailable && ltdPlansAvailable);
			AdditionalBenefitPlan adPlan = disabilityOptionService.getDisabilityOptionByPlans(disabilityPlans, company,
					isStandAlone);

			if (adPlan != null && CollectionUtils.isNotEmpty(adPlan.getOptionPlans())) {
				adPlan.getOptionPlans().forEach(dop -> planSelections.add(StrategyServiceHelper
						.constructADPlanSelection(strategyId, bg.getId(), dop.getId(), dop.getPlanType(), 0)));
			}
		}

		planSelectionService.saveAll(planSelections);
	}

	private void updateBenefitPlanIdIfMapped(BenefitPlan bp, Map<String, PlanMapping> realmPlanMapping) {
		if (realmPlanMapping != null && realmPlanMapping.containsKey(bp.getId())) {
			PlanMapping mappedPlan = realmPlanMapping.get(bp.getId());
			if (!mappedPlan.getNewBenefitPlans().isEmpty()) {
				bp.setId(mappedPlan.getNewBenefitPlans().get(0));
			}
		}
	}

	/**
	 * Checks whether a Life plan (type 23) is already present in {@code adPlansMap}.
	 * If not, looks up the configured default Life plan for the company's quarter
	 * whose EFFDT/ENDDT window covers the realm plan-year start date, applies any
	 * realm plan mapping, and adds a plan-selection entry directly to
	 * {@code planSelections}.
	 */
	private void addDefaultLifePlanIfMissing(Map<String, Map<String, BenefitPlan>> adPlansMap,
			Company company, Map<String, PlanMapping> realmPlanMapping,
			Long strategyId, BenefitGroup bg, List<PlanSelection> planSelections) {

		boolean hasLifePlan = adPlansMap.values().stream()
				.flatMap(offerPlanMap -> offerPlanMap.values().stream())
				.anyMatch(bp -> BSSApplicationConstants.LIFE_CODE.equals(bp.getPlanType()));

		if (!hasLifePlan) {
			strategyDefaultPlanDao
					.findBy(company.getQuater(),
							Collections.singletonList(BSSApplicationConstants.LIFE_CODE),
							company.getRealmPlanYear().getPlanYearStart())
					.stream().findFirst().ifPresent(defaultPlan -> {
						BenefitPlan defaultBp = new BenefitPlan();
						defaultBp.setId(defaultPlan.getBaseBenefitPlan());
						defaultBp.setPlanType(defaultPlan.getPlanType());
						defaultBp.setPlanCarrierId(defaultPlan.getPortfolioId());
						updateBenefitPlanIdIfMapped(defaultBp, realmPlanMapping);
						planSelections.add(StrategyServiceHelper.constructADPlanSelection(strategyId,
								bg.getId(), defaultBp.getId(), defaultBp.getPlanType(), 0));
						logger.info("Inserted default Life plan [{}] directly for BenefitGroup [{}]",
								defaultBp.getId(), bg.getBenefitProgram());
					});
		}
	}

	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param benefitGroupId
	 * @param realmPlanMapping
	 * @param rates
	 * @param bgAllPlansMap
	 * @param hpPlansMap
	 * @param mapOfCoverageLevels
	 * @param autoSelectPlans
	 * @param medPlans
	 * @param groupFundingDetails
	 * @param costShare
	 * @param mandatoryPlans
	 * @param groupType
	 * @param planOverrides
	 * @param hmoPpoFlags
	 * @param selectedPlancarriers
	 * @param minFundings
	 */
	private void createHPSelectionsForBenefitGroup(Company company, Long strategyId, Long benefitGroupId,
			Map<String, PlanMapping> realmPlanMapping, Map<String, List<BenefitPlanRate>> rates,
			Map<String, Map<String, BenefitPlan>> bgAllPlansMap, Map<String, Map<String, BenefitPlan>> hpPlansMap,
			Map<String, List<CoverageLevel>> mapOfCoverageLevels, Map<String, Map<String, Set<String>>> autoSelectPlans,
			List<String> medPlans, Map<String, Map<String, Object>> groupFundingDetails, BigDecimal costShare,
			Map<String, StateBenefitPlan> mandatoryPlans, String groupType,
			Map<String, Map<String, String>> planOverrides, Map<String, XbssRealmPlyrPlan> plyrPlanMap,
			Map<String, Set<Long>> selectedPlancarriers, List<CarrierMinimumFunding> minFundings,
			List<String> fplMedicalPlans, Map<String, List<PlanCoverageLevelHeadCount>> planCoverageLevelHeadCounts,
			Map<String, PlanMapping> primaryPlanMapping, Strategy rs) {
		Set<String> selectedPlanTypes = new HashSet<>();
		Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
		List<PlanSelection> planSelections = new ArrayList<>();
		String dentalNonEligPlanType = BSSApplicationConstants.EMPTY_SPACE;
		String visionNonEligPlanType = BSSApplicationConstants.EMPTY_SPACE;

		Set<String> currentBenefitPlans = RenewalServiceHelper.getCompanyPreviousPlanYearHealthPlans(hpPlansMap, primaryPlanMapping);
		
		for (String offerType : bgAllPlansMap.keySet()) {
			String offerTypeDesc = PlanTypesEnum.getName(offerType);
			Map<String, BenefitPlan> offerTypePlanMap = null;
			if (MapUtils.isNotEmpty(hpPlansMap)) {
				offerTypePlanMap = hpPlansMap.get(offerType);
			}
			for (String benefitPlanId : bgAllPlansMap.get(offerType).keySet()) {
				BenefitPlan bp = new BenefitPlan();
				if (null != offerTypePlanMap && null != offerTypePlanMap.get(benefitPlanId)) {
					BeanUtils.copyProperties(offerTypePlanMap.get(benefitPlanId), bp);
				} else {
					if (MapUtils.isNotEmpty(offerTypePlanMap)) {
						if (bgAllPlansMap.get(offerType).get(benefitPlanId).getPlanType()
								.equals(offerTypePlanMap.get(offerTypePlanMap.keySet().toArray()[0]).getPlanType())) {
							BeanUtils.copyProperties(bgAllPlansMap.get(offerType).get(benefitPlanId), bp);
							RenewalServiceHelper.addBlankContributions(bp, mapOfCoverageLevels.get(offerTypeDesc));
						} else {
							continue;
						}
					} else {
						BenefitPlan nonEligBp = bgAllPlansMap.get(offerType).get(benefitPlanId);
						if (BSSApplicationConstants.EMPTY_SPACE.equals(dentalNonEligPlanType)
								&& BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(nonEligBp.getPlanType())) {
							if (BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType)) {
								dentalNonEligPlanType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
							} else {
								dentalNonEligPlanType = nonEligBp.getPlanType();
							}
						}
						if (BSSApplicationConstants.EMPTY_SPACE.equals(visionNonEligPlanType)
								&& BSSApplicationConstants.VISION_PLAN_TYPES.contains(nonEligBp.getPlanType())) {
							if (BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType)) {
								visionNonEligPlanType = BSSApplicationConstants.VISION_PLAN_TYPE;
							} else {
								visionNonEligPlanType = nonEligBp.getPlanType();
							}
						}
						if (dentalNonEligPlanType.equals(nonEligBp.getPlanType())
								|| visionNonEligPlanType.equals(nonEligBp.getPlanType())
								|| BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(nonEligBp.getPlanType())) {
							BeanUtils.copyProperties(bgAllPlansMap.get(offerType).get(benefitPlanId), bp);
							RenewalServiceHelper.addBlankContributions(bp, mapOfCoverageLevels.get(offerTypeDesc));
						} else {
							continue;
						}
					}
				}
				// using the one to many mapping to add all the mapped plans into the strategy
				if (null != realmPlanMapping && null != realmPlanMapping.get(benefitPlanId)) {
					PlanMapping vpm = realmPlanMapping.get(benefitPlanId);
					List<String> mappedBenefitPlans = vpm.getNewBenefitPlans();
					for (String benefitPlan : mappedBenefitPlans) {
						BenefitPlan mappedBenefitPlan = new BenefitPlan();
						BeanUtils.copyProperties(bp, mappedBenefitPlan);
						mappedBenefitPlan.setId(benefitPlan);
						mappedBenefitPlan.setPlanCarrierId(vpm.getNewPortfolioId());
						if (null == benefitPlanMap.get(mappedBenefitPlan.getId())
								&& (!bgAllPlansMap.get(offerType).containsKey(mappedBenefitPlan.getId())
										|| mappedBenefitPlan.getId().equals(bp.getId()))) {
							if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(mappedBenefitPlan.getPlanType())) {
								if (medPlans.contains(mappedBenefitPlan.getId())) {
									benefitPlanMap.put(mappedBenefitPlan.getId(), mappedBenefitPlan);
									selectedPlanTypes.add(mappedBenefitPlan.getPlanType());
									PlanSelection planSelection = StrategyServiceHelper
											.constructPlanSelection(strategyId, benefitGroupId, mappedBenefitPlan, 0L);
									planSelections.add(planSelection);
								}
							} else {
								benefitPlanMap.put(mappedBenefitPlan.getId(), mappedBenefitPlan);
								selectedPlanTypes.add(mappedBenefitPlan.getPlanType());
								PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId,
										benefitGroupId, mappedBenefitPlan, 0L);
								planSelections.add(planSelection);
							}
						}
					}
				} else {
					if (null == benefitPlanMap.get(bp.getId())) {
						if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(bp.getPlanType())) {
							if (medPlans.contains(bp.getId())) {
								benefitPlanMap.put(bp.getId(), bp);
								selectedPlanTypes.add(bp.getPlanType());
								PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId,
										benefitGroupId, bp, 0L);
								planSelections.add(planSelection);
							}
						} else {
							benefitPlanMap.put(bp.getId(), bp);
							selectedPlanTypes.add(bp.getPlanType());
							PlanSelection planSelection = StrategyServiceHelper.constructPlanSelection(strategyId,
									benefitGroupId, bp, 0L);
							planSelections.add(planSelection);
						}
					}
				}
			}
		}
		// Adding the crossref plans to the plan selection for the group.
		RenewalServiceHelper.addPlanSelectionsForAutoSelectPlans(planSelections, autoSelectPlans, mapOfCoverageLevels,
				benefitPlanMap, strategyId, benefitGroupId);

		// Adding the mandatory plans to the plan selection for the group.
		if (null != mandatoryPlans && !mandatoryPlans.isEmpty()) {
			Map<String, String> planVendorMap = realmDataDao.getPlanVendors(mandatoryPlans.keySet(),
					company.getRealmPlanYearId());
			RenewalServiceHelper.addPlanSelectionsForMandatoryPlans(planSelections, mandatoryPlans, mapOfCoverageLevels,
					benefitPlanMap, strategyId, benefitGroupId, planVendorMap, selectedPlanTypes);
		}
		// Saving all plan selections to DB
		planSelections = planSelectionService.saveAll(planSelections);

		Set<PlanSelection> planSelectionSet = new HashSet<>();
		planSelectionSet.addAll(planSelections);
		// Updating PPO Flag for all plan selections
		RenewalServiceHelper.updateWidelyAvailableFlagForPlanSelections(planSelectionSet, company, benefitPlanMap,
				benefitPlanDao);
		
		// Based on the updated plan selection, update group level funding as necessary
		// to meet minimum funding
		updateGroupFundingForMinimumContribution(company, strategyId, benefitGroupId, groupFundingDetails, planSelections, rates,
				selectedPlancarriers, minFundings);

		// Creating all contributions for all plan selections and saving to
		// DB
		createUpdateContribution(company, benefitPlanMap, planSelections, rates, groupFundingDetails, costShare,
				groupType, planOverrides, plyrPlanMap, selectedPlancarriers, minFundings, fplMedicalPlans,
				planCoverageLevelHeadCounts, strategyId, benefitGroupId, currentBenefitPlans, rs);
	}

	/**
	 * 
	 * @param company
	 * @param benefitPlanMap
	 * @param planSelections
	 * @param rates
	 * @param groupFundingDetails
	 * @param costShare
	 * @param groupType
	 * @param planOverrides
	 * @param hmoPpoFlags
	 * @param selectedPlancarriers
	 * @param minFundings
	 */
	private void createUpdateContribution(Company company, Map<String, BenefitPlan> benefitPlanMap,
			List<PlanSelection> planSelections, Map<String, List<BenefitPlanRate>> rates,
			Map<String, Map<String, Object>> groupFundingDetails, BigDecimal costShare, String groupType,
			Map<String, Map<String, String>> planOverrides, Map<String, XbssRealmPlyrPlan> plyrPlanMap,
			Map<String, Set<Long>> selectedPlancarriers, List<CarrierMinimumFunding> minFundings,
			List<String> fplMedicalPlans, Map<String, List<PlanCoverageLevelHeadCount>> planCoverageLevelHeadCounts,
			Long strategyId, Long benefitGroupId, Set<String> currentBenefitPlans, Strategy rs) {
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();
		String fundingType = "";
		Map<String, Object> coverageLevelFunding = null;
		for (PlanSelection ps : planSelections) {
			BenefitPlan bp = benefitPlanMap.get(ps.getBenefitPlan());
			List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
			if (null != groupFundingDetails) {
				if (Constants.dentalPlanTypeList.contains(bp.getPlanType())) {
					coverageLevelFunding = groupFundingDetails.get(Constants.DENTAL_CODE);
				} else if (Constants.visionPlanTypeList.contains(bp.getPlanType())) {
					coverageLevelFunding = groupFundingDetails.get(Constants.VISION_CODE);
				} else {
					coverageLevelFunding = groupFundingDetails.get(Constants.MEDICAL_CODE);
				}
			}
			if (null != planCoverageLevelHeadCounts && null != planCoverageLevelHeadCounts.get(bp.getId())) {
				headCountList.addAll(planCoverageLevelHeadCounts.get(bp.getId()));
			}

			if ((null == costShare || BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType)
					|| !currentBenefitPlans.contains(bp.getId()))
					&& null != coverageLevelFunding && !coverageLevelFunding.isEmpty()) {
				if (null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE)) {
					fundingType = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
				}
				List<Contribution> contribList = RenewalServiceHelper.createUpdateContributionsByBaseFunding(
						bp, ps, rates, headCountList, coverageLevelFunding,
							((costShare == null) ? planOverrides : null),
							false);
				contributions.addAll(contribList);
				if (Constants.MEDICAL_CODE.equals(bp.getPlanType())) {
					benefitPlanContributions.put(bp.getId(), contribList);
				}
			} else {

				List<Contribution> contribList = new ArrayList<>();
				RenewalServiceHelper.constructContributionsByPercentIncrease(bp, ps, costShare, rates,
						headCountList, contribList, planOverrides);
				contributions.addAll(contribList);
				if (Constants.MEDICAL_CODE.equals(bp.getPlanType())) {
					benefitPlanContributions.put(bp.getId(), contribList);
				}
			}
		}

		Map<String, List<String>> selectedPlansByRegion = null;
		if (null != benefitPlanContributions && !benefitPlanContributions.isEmpty()) {
			selectedPlansByRegion = realmDataDao.getSelectedPlansByRegion(company.getRealmPlanYearId(),
					benefitPlanContributions.keySet());
		}
		List<String> mandatoryPlansToExclude = realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(
				company.getHeadQuatersState(), BigDecimal.valueOf(company.getRealmPlanYearId()));

		// finding the minimum funding
		Map<String, BigDecimal> minimumFundingMap = RenewalServiceHelper.getMinimumFunding(contributions,
				mandatoryPlansToExclude, company, selectedPlancarriers, minFundings);

		Map<String, List<String>> planRegions = null;
		if (null != selectedPlansByRegion) {
			planRegions = RenewalServiceHelper.preparePlanToRegionMap(selectedPlansByRegion);
		}
		// updating the plan contributions to set it to minimum funding.
		RenewalServiceHelper.updateContributionsForMinimumFunding(minimumFundingMap, contributions, company,
				planRegions, benefitPlanContributions, fundingType, groupFundingDetails);

		// Applying FPL for ALE clients who did not OPT out.
		if (RenewalServiceHelper.isFplApplicable(company, rs.getAcaFplOpted())) {
			Map<String, String> fplPLansByRegion = SubmitServiceHelper.findLowCostPpoPlanByRegion(selectedPlansByRegion,
					benefitPlanContributions);
			SubmitServiceHelper.setFPLForLowCostPpoPlan(contributions, fplPLansByRegion, company, fplMedicalPlans);
		}
		
		// saving contribution data.
		contributionService.saveAll(contributions);

		// calculating estimates
		try {
			// calculating estimates & saving to DB
			Map<Long, List<StrategyEstimate>> strategyEstimateMap = RenewalServiceHelper
					.calculateStrategyGroupEstimates(strategyId, benefitGroupId, contributions, groupFundingDetails);
			strategyDataDao.deleteStrategyEstimateByStrategyGroup(strategyId, benefitGroupId);
			strategyDataDao.insertStrategyEstimate(strategyEstimateMap);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, company.getCode(), null);
		}
	}

	private void setupOverrides(Map<String, Map<String, Map<String, Object>>> groupFundingDetails,
			Map<String, Map<String, Map<String, String>>> groupPlanOverrideMap) {

		for (Entry<String, Map<String, Map<String, String>>> groupPlanOverrideMapEntry : groupPlanOverrideMap
				.entrySet()) {
			String updatedOverrideValue = null;
			String benefitProgram = groupPlanOverrideMapEntry.getKey();
			if (groupFundingDetails.containsKey(benefitProgram)) {
				updatedOverrideValue = BSSApplicationConstants.PLAN_OVERRIDE_BASE;
			} else {
				updatedOverrideValue = BSSApplicationConstants.PLAN_OVERRIDE_PCT;
			}
			for (Entry<String, Map<String, String>> planOverrideMapEntry : groupPlanOverrideMapEntry.getValue()
					.entrySet()) {
				for (Entry<String, String> overrideMap : planOverrideMapEntry.getValue().entrySet()) {
					String overrideValue = PlanOverrideServiceHelper.getRenewalPlanOverrideType(overrideMap.getValue(),
							updatedOverrideValue);
					overrideMap.setValue(overrideValue);
				}
			}
		}
	}

	private void applyBenefitOfferException(Company company,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap) {
		Map<String, Map<String, Map<String, BenefitPlan>>> futureBgsHealthPlansMap = new HashMap<>(bgsHealthPlansMap);
		futureBgsHealthPlansMap.forEach(
				(key, val) -> benOfferExceptionService.applyException(company, futureBgsHealthPlansMap.get(key)));

		Map<String, Map<String, Map<String, BenefitPlan>>> futureBgsADPlansMap = new HashMap<>(bgsADPlansMap);
		futureBgsADPlansMap
				.forEach((key, val) -> benOfferExceptionService.applyException(company, futureBgsADPlansMap.get(key)));
	}

	private Map<String, Map<String, BenefitPlan>> getK1AdditionalPlans(Company company,
			Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap) {

		Set<AdditionalBenefitPlan> allDisabilityPlans = disabilityOptionService
				.getDisabilityOptionsByRealmPlanYear(company);
		Map<String, Map<String, BenefitPlan>> defaultProgramADPlansMap = bgsADPlansMap.get(company.getBenefitProgram());
		Map<String, Map<String, BenefitPlan>> k1AdditionalPlans = new HashMap<>();
		if (defaultProgramADPlansMap != null) {
			for (Entry<String, Map<String, BenefitPlan>> entry : defaultProgramADPlansMap.entrySet()) {
				String planType = entry.getKey();

				if (BSSApplicationConstants.LIFE_CODE.equals(planType)) {
					k1AdditionalPlans.put(planType, entry.getValue());
				} else {

					Map<String, BenefitPlan> k1DisabilityPlanMap = new HashMap<>();
					for (String adPlanId : entry.getValue().keySet()) {
						for (AdditionalBenefitPlan disabilityPlan : allDisabilityPlans) {
							if (BSSApplicationConstants.K1_GROUP_TYPE.equals(disabilityPlan.getOfferedGroupType())
									|| BSSApplicationConstants.DISABILITY_OFFERED_GROUP_TYPE_ALL
											.equals(disabilityPlan.getOfferedGroupType())) {
								for (DisabilityBenefitOptionPlans optionPlan : disabilityPlan.getOptionPlans()) {
									if (adPlanId.equals(optionPlan.getId())) {
										k1DisabilityPlanMap.put(adPlanId, entry.getValue().get(adPlanId));
									}
								}
							}
						}
					}
					if (!k1DisabilityPlanMap.isEmpty()) {
						k1AdditionalPlans.put(planType, k1DisabilityPlanMap);
					}
				}
			}
		}
		return k1AdditionalPlans;
	}
	
	private void updateGroupFundingForMinimumContribution(Company company, Long strategyId, Long benefitGroupId,
			Map<String, Map<String, Object>> groupFundingDetails, List<PlanSelection> planSelections,
			Map<String, List<BenefitPlanRate>> rates, Map<String, Set<Long>> selectedPlancarriers,
			List<CarrierMinimumFunding> minFundings) {

		if (null != groupFundingDetails) {
			List<String> mandatoryPlansToExclude = realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(
					company.getHeadQuatersState(), BigDecimal.valueOf(company.getRealmPlanYearId()));

			// finding the minimum funding
			Map<String, BigDecimal> minimumFundingMap = RenewalServiceHelper.getMinimumFunding(planSelections, rates,
					mandatoryPlansToExclude, company, selectedPlancarriers, minFundings);

			for (Entry<String, BigDecimal> minimumFundingEntry : minimumFundingMap.entrySet()) {
				if (groupFundingDetails.containsKey(minimumFundingEntry.getKey())) {
					Map<String, Object> fundingDetails = groupFundingDetails.get(minimumFundingEntry.getKey());
					String fundingType = fundingDetails.get(BSSApplicationConstants.FUNDING_TYPE).toString();
					if (fundingType.equals(BSSApplicationConstants.FLAT)
							|| fundingType.equals(BSSApplicationConstants.BSUPP)) {
						updateGroupFundingDetail(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE.getId(), fundingDetails,
								minimumFundingEntry.getValue());
						updateGroupFundingDetail(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), fundingDetails,
								minimumFundingEntry.getValue());
						updateGroupFundingDetail(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), fundingDetails,
								minimumFundingEntry.getValue());
						updateGroupFundingDetail(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), fundingDetails,
								minimumFundingEntry.getValue());
					} else if (fundingType.equals(BSSApplicationConstants.BFPCT)
							|| fundingType.equals(BSSApplicationConstants.CFPCT)) {
						updateGroupFundingFlatMax(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE.getId(), fundingDetails,
								minimumFundingEntry.getValue());
						updateGroupFundingFlatMax(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), fundingDetails,
								minimumFundingEntry.getValue());
						updateGroupFundingFlatMax(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), fundingDetails,
								minimumFundingEntry.getValue());
						updateGroupFundingFlatMax(strategyId, benefitGroupId, minimumFundingEntry.getKey(),
								CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), fundingDetails,
								minimumFundingEntry.getValue());
					}
				}
			}
		}
	}

	private void updateGroupFundingDetail(Long strategyId, Long benefitGroupId, String planType, String coverageLevel,
			Map<String, Object> fundingDetails, BigDecimal minimumFunding) {
		if (((BigDecimal) fundingDetails.get(coverageLevel)).compareTo(minimumFunding) < 0) {
			fundingDetails.put(coverageLevel, minimumFunding);
			strategyFundingDetailService.updateStrategyFundingDetail(strategyId, benefitGroupId, planType, coverageLevel,
					minimumFunding);
		}
	}

	private void updateGroupFundingFlatMax(Long strategyId, Long benefitGroupId, String planType, String coverageLevel,
			Map<String, Object> fundingDetails, BigDecimal minimumFunding) {
		if (fundingDetails.containsKey(BSSApplicationConstants.FUNDING_BASE_PLAN)
				&& BSSApplicationConstants.FLAT_MAX
						.equals(fundingDetails.get(BSSApplicationConstants.FUNDING_BASE_PLAN))
				&& ((BigDecimal) fundingDetails.get(coverageLevel + BSSApplicationConstants.LIMIT))
						.compareTo(minimumFunding) < 0) {
			fundingDetails.put(coverageLevel + BSSApplicationConstants.LIMIT, minimumFunding);
			strategyFundingDetailService.updateStrategyFundingFlatMax(strategyId, benefitGroupId, planType,
					coverageLevel, minimumFunding);
		}
	}

}