package com.trinet.ambis.service.impl;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CapTypeEnum;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.*;
import com.trinet.ambis.persistence.dao.hrp.*;
import com.trinet.ambis.persistence.model.*;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse.BenefitType;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse.FundingCapDetails;
import com.trinet.ambis.service.*;
import com.trinet.ambis.service.model.*;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;
import com.trinet.ambis.util.*;
import com.trinet.ambis.client.DefaultPlanMappingServiceClient.PlanMappingResponse;
import com.trinet.domain.common.ReturnResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.trinet.ambis.common.BSSApplicationConstants.*;
import static com.trinet.ambis.enums.US.MASSACHUSETTS;
import static java.util.stream.Collectors.*;

@Service
public class ProspectStrategyServiceImpl implements ProspectStrategyService {
	private static final Logger logger = LoggerFactory.getLogger(ProspectStrategyServiceImpl.class);
	@Autowired
	StrategyDao strategyDao;
	@Autowired
	BenefitGroupService benefitGroupService;
	@Autowired
	StrategyGroupService strategyGroupService;
	@Autowired
	RealmDataDao realmDataDao;
	@Autowired
	StrategyFundingModelService strategyFundingModelService;
	@Autowired
	PlanRatesService planRatesService;
	@Autowired
	PortfolioService portfolioService;
	@Autowired
	BenefitPlanDao benefitPlanDao;
	@Autowired
	PlanSelectionService planSelectionService;
	@Autowired
	XbssRealmPlyrPlanDao realmPlyrPlanDao;
	@Autowired
	ContributionService contributionService;
	@Autowired
	RealmPlyrPlanService realmPlyrPlanService;
	@Autowired
	ProspectServiceRestClient prospectServiceRestClient;
	@Autowired
	ProspectCensusService prospectCensusService;
	@Autowired
	EmployeeStrategyGroupDao employeeStrategyGroupDao;
	@Autowired
	StrategyDefaultPlanDao strategyDefaultPlanDao;
	@Autowired
	ProspectDefaultPlanAssignmentService defaultPlanAssignmentService;
	@Autowired
	ProspectDefaultPlanMappingService prospectDefaultPlanMappingService;
	@Autowired
	CacheService cacheService;
	@Autowired
	BssCoreServiceClient bssCoreServiceClient;
	@Autowired
	BenefitPlanService benefitPlanService;
	@Autowired
	ProspectCompanyService prospectCompanyService;
	@Autowired
	BenefitsBundleService benefitsBundleService;
	@Autowired
	CompanyService companyService;
	@Autowired
	DefaultPlanMappingService planMappingAssignmentService;

    private static final String W2MA = "W2MA";

	@Override
	public StrategyData getProspectCurrentStrategy(String prospectId) {
		List<BenefitsDetailsResponse> benefitsDetailsResponse = getProspectBenefitDetails(prospectId);
		logger.info("benefitsDetailsResponse {}", benefitsDetailsResponse);
		StrategyData strategyData = new StrategyData();
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId(0L);
		strategySummary.setName(ProspectConstants.PROSPECT_STRATEGY_NAME);
		strategySummary.setType(ProspectConstants.PROSPECT);
		strategySummary.setEstimatedTotalCost(benefitsDetailsResponse.stream()
				.map(BenefitsDetailsResponse::getMonthlyTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
		strategySummary
				.setHeadcount(benefitsDetailsResponse.stream().mapToInt(BenefitsDetailsResponse::getHeadCount).sum());
		strategySummary.setTotalBudget(BigDecimal.ZERO);
		strategySummary.setBudgetFactor(1);
		strategySummary.setCompanyId(prospectId);
		strategyData.setStrategySummary(strategySummary);
		StrategyHsaFundingDto strategyHsaFundingDto = new StrategyHsaFundingDto();
		strategyHsaFundingDto.setOptionId(0);
		strategySummary.setProspectCurrentStrategy(true);
		strategyData.setStrategyHsaFunding(strategyHsaFundingDto);
		strategyData.setBenefitGroups(getStrategyBenefitGroup(benefitsDetailsResponse));
		return strategyData;
	}

	@Override
	public ModelCompareStrategyCost getProspectCurrentStrategyCosts(String prospectId) {
		List<BenefitsDetailsResponse> benefitsDetailsResponse = getProspectBenefitDetails(prospectId);
		logger.info("getProspectCurrentStrategyCosts -> benefitsDetailsResponse {}", benefitsDetailsResponse);
		return getStrategyPlanCosts(benefitsDetailsResponse);
	}

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void createDefaultTrinetStrategy(Company company, long selectedCarrier,
			List<PlanMappingResponse> planMappingResponse) {

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService
				.getMapForRealmPlanYear(company.getRealmPlanYear().getId());

		List<StrategyDefaultPlan> defaultPlans = strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
				BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL);

		// loading default mappings
		boolean isGaBundleEnabled = RulesAndConfigsUtils.isGaBundleEnabled(company.getRealmPlanYear().getId());
		boolean planMappingServiceEnabled = RulesAndConfigsUtils.isPlanMappingServiceEnabled(company.getRealmPlanYear().getId());
		boolean isOmsCompany = CompanyServiceHelper.isOMSExchange(company);
		CompletableFuture<Void> planMappingFuture = null;
		if (!isOmsCompany) {
			if(isGaBundleEnabled){
				planMappingAssignmentService.saveDefaultPlanMappings(company, planMappingResponse);
			}
			else if (planMappingServiceEnabled && company.isProspectCompany()) {
				planMappingFuture = CompletableFuture.runAsync(() -> planMappingAssignmentService.callPlanMappingService(company))
						.exceptionally(ex -> {
							throw new RuntimeException("Plan mapping service failed for company: " + company.getCode(), ex);
						});
			} else {
				prospectDefaultPlanMappingService.createCensusDefaultRegionalPlanMapping(company);
			}
		}

		Map<String, Boolean> benOfferExceptions = new HashMap<>();
		List<CarrierMinimumFunding> minFundings = benefitPlanService.getLowestCostPlanPerCarrier(company);
		
		Strategy prospectStrategy = null;
		if (company.isProspectCompany()) {
			prospectStrategy = StrategyServiceHelper.constructStrategyForProspect(company,
					ProspectConstants.PROSPECT_TN_STRATEGY_NAME);
		} else {
			prospectStrategy = StrategyServiceHelper.constructStrategyForProspect(company,
					BSSApplicationConstants.PROPOSED_STRATEGY_NAME);
			prospectStrategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		}
		
		prospectStrategy = strategyDao.save(prospectStrategy);

        // insert the employees into benefit groups
        List<ProspectCensusResponse> censusData = null;
        boolean hasMAEmployee = false;
        if (company.isProspectCompany()) {
            censusData = prospectCensusService.getProspectCensus(company.getCode());
        } else {
            censusData = bssCoreServiceClient.getCensusByCompanyCode(company.getCode());
        }
        
		if (company.isProspectCompany() && CompanyServiceHelper.isOMSExchange(company)) {
			BenExchngEnums benExchange = BenExchngEnums.getByBenExchange(company.getRealm().getBenExchange());
			ProspectInfoResponse prospectInfoResponse = prospectCompanyService
					.getProspectBasicDetails(company.getCode(), benExchange);
			if (prospectInfoResponse != null && prospectInfoResponse.getExpiryDate() == null) {
				prospectCompanyService.updateProspectExpiryDate(company.getCode(), benExchange.getExchangeId(),
						LocalDate.now().plusDays(BSSApplicationConstants.PROSPECT_EXPIRY_DAYS));
			}
		}

        boolean enableDefaultMAGroupCreation = AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled();

        // Create map of census data filtered by home state and K1 status
        Map<String, List<ProspectCensusResponse>> censusDataByGroup = censusData.stream()
                .collect(Collectors.groupingBy(census -> {
                    if (census.isK1()) {
                        return BSSApplicationConstants.K1_GROUP_TYPE;
                    } else if (enableDefaultMAGroupCreation && MASSACHUSETTS.getANSIabbreviation().equalsIgnoreCase(census.getState())) {
                        return W2MA;
                    } else {
                        return STD_GROUP_TYPE;
                    }
                }));

        if (enableDefaultMAGroupCreation) {
            hasMAEmployee = censusData.stream()
                    .anyMatch(census -> !census.isK1() && MASSACHUSETTS.getANSIabbreviation()
                            .equals(census.getState()));
        }

		// creating prospect Benefit Groups
        List<BenefitGroup> bgs = createProspectBenefitGroups(company, prospectStrategy,
                hasMAEmployee);

		Map<String, List<CoverageLevel>> mapOfCoverageLevels = realmDataDao
				.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, company.getRealmPlanYear().getId());

		// If this is OMS exchange and not a TIB prospect, remove medical coverage levels
		if (CompanyServiceHelper.isOMSExchange(company) && !CompanyServiceHelper.isTibProspect(company)) {
			mapOfCoverageLevels.remove(BSSApplicationConstants.MEDICAL);
			mapOfCoverageLevels.remove(BSSApplicationConstants.DENTAL);
			mapOfCoverageLevels.remove(BSSApplicationConstants.VISION);
		}

		// Rate for the plans selected by the company
		Map<String, List<BenefitPlanRate>> rates = planRatesService.getBenefitPlanRatesBy(company);

		// Get all benefitPlans by planTypes for realmYearId for this company's
		// applicable regions.
		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioService.findPrimaryPlanCarriers(company);

		// need to get selected medical carrier during sales flow:

		Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriersProspect(planCarrierMap,
				selectedCarrier);

		Map<String, Set<Long>> selectedPlancarriers = BenefitCategoriesHelper
				.getSelectedPlanCarriersProspect(planCarrierMap, selectedCarrier);

		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);

		if (isGaBundleEnabled) {
			updateBundleDetailsForBenBundle(company);
		}
		if(company.isProspectCompany() && company.isLargeDealProspect()){
			//update bundleId from bundle table to company table
			updateBundleIdInCompany(company);
		}

		// health plans
		Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap = benefitPlanDao
				.getAllPrimaryBenefitPlans(primaryPlanCarriers, company, outOfRegionPlans);

		// removing the plans of carriers that are not selected for medical.
		RenewalServiceHelper.updateMedicalPlans(selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE),
				healthBenefitPlansMap);


		Map<String, StateBenefitPlan> mandatoryPlans = RenewalServiceHelper.getAllMandatoryPlans(company,
				healthBenefitPlansMap, selectedPlancarriers);

		List<String> fplMedicalPlans = RenewalServiceHelper.getFplPlans(healthBenefitPlansMap, mandatoryPlans,
				selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE));

		for (BenefitGroup benefitGroup : bgs) {
			List<StrategyFundingModel> strategyFunding = new ArrayList<>();
			RenewalServiceHelper.createK1Funding(strategyFunding, prospectStrategy.getId(), benefitGroup.getId(),
					mapOfCoverageLevels, benOfferExceptions);
			strategyFundingModelService.saveAll(strategyFunding);
		}

		// getting funding details
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = realmDataDao
				.getStrategyFundingDetails(prospectStrategy.getId());

        // Set to collect all employee strategy groups for batch saving
        Set<EmployeeStrategyGroup> allEmployeeStrategyGroups = new HashSet<>();

		for (BenefitGroup benefitGroup : bgs) {

			Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();

			// plan selections for Health plans
			List<PlanSelection> healthPlanSelections = ProspectStrategyServiceHelper.createDentalAndVisionPlanSelections(
					healthBenefitPlansMap, mapOfCoverageLevels, prospectStrategy.getId(), benefitGroup.getId(),
					benefitPlanMap, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);

			// Plan selection for life and disability
			List<PlanSelection> addPlanSelections = ProspectStrategyServiceHelper
					.createAdditionalPlanSelections(prospectStrategy.getId(), benefitGroup.getId(), defaultPlans);

			// Saving all AD plan selections to DB
			planSelectionService.saveAll(addPlanSelections);

			// Saving all plan selections to DB
			healthPlanSelections = planSelectionService.saveAll(healthPlanSelections);

			Set<PlanSelection> planSelectionSet = new HashSet<>();
			planSelectionSet.addAll(healthPlanSelections);

			// Updating PPO Flag for all plan selections
			RenewalServiceHelper.updateWidelyAvailableFlagForPlanSelections(planSelectionSet, company, benefitPlanMap,
					benefitPlanDao);

			// creating contribution records for health plans
			createUpdateContribution(company, benefitPlanMap, healthPlanSelections, rates,
					groupFundingDetails.get(benefitGroup.getBenefitProgram()), plyrPlanMap, selectedPlancarriers,
					minFundings, fplMedicalPlans, prospectStrategy);

			// creating employee strategy groups
            createEmployeeStrategyGroups(censusDataByGroup, benefitGroup, allEmployeeStrategyGroups);
		}

        // Save all employee strategy groups at once
        employeeStrategyGroupDao.saveAll(allEmployeeStrategyGroups);

		Set<Long> altCarriers = new HashSet<>();
		if (MapUtils.isNotEmpty(selectedPlancarriers)
				&& CollectionUtils.isNotEmpty(selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE)))
			selectedPlancarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).forEach(carrier -> {
				if (!carrier.equals(selectedCarrier)) {
					altCarriers.add(carrier);
				}
			});
		
		// clearing the cache since the plan and rates object created from here impacts
		// benefit categories
		cacheService.invalidateOutofDateCache(company);
		
		if (planMappingFuture != null) {
			planMappingFuture.join();
		}

		// Strategy employees plan defaulting
		if (!isOmsCompany) {
			defaultPlanAssignmentService.insertStrategyDefaultAssignments(company, Set.of(selectedCarrier), altCarriers, prospectStrategy.getId());
		}
		else {
			boolean isNonTibOmsCompany = CompanyServiceHelper.isOMSExchange(company) && !CompanyServiceHelper.isTibProspect(company);
			boolean shouldInvokePlanMappingService = planMappingServiceEnabled && !isNonTibOmsCompany;
			if (shouldInvokePlanMappingService) {
				List<PlanMappingResponse> omsPlanMappingResponse = planMappingAssignmentService.callPlanMappingServiceForOms(
						company, Collections.emptySet());
				planMappingAssignmentService.createOmsEePlanAssignments(company, prospectStrategy.getId(), omsPlanMappingResponse);
			}
		}

	}

    private void createEmployeeStrategyGroups(Map<String, List<ProspectCensusResponse>> censusDataByGroup,
                                              BenefitGroup benefitGroup, Set<EmployeeStrategyGroup> allEmployeeStrategyGroups) {

        String benefitGroupType = benefitGroup.getType();
        String benefitGroupName = benefitGroup.getName();
        Long strategyGroupId = benefitGroup.getBenefitGroupStrategy().iterator().next().getId();

        // Determine which census group this benefit group should handle
        List<ProspectCensusResponse> targetCensusData = null;
        if (K1_GROUP_TYPE.equals(benefitGroupType)) {
            // K1 benefit group gets K1 employees
            targetCensusData = censusDataByGroup.getOrDefault(K1_GROUP_TYPE, new ArrayList<>());
        } else if (CLIENT_MA_GROUP_NAME.equals(benefitGroupName)) {
            // MA benefit group gets W2MA employees (W2 employees from Massachusetts)
            targetCensusData = censusDataByGroup.getOrDefault(W2MA, new ArrayList<>());
        } else if (STD_GROUP_TYPE.equals(benefitGroupType)) {
            // W2 benefit group gets remaining W2 employees (non-MA W2 employees)
            targetCensusData = censusDataByGroup.getOrDefault(STD_GROUP_TYPE, new ArrayList<>());
        } else {
            //Log error
            logger.error("No matching census data found for benefit group: {}", benefitGroupName);
        }

        // Create employee strategy groups for the target census data
        if (targetCensusData != null) {
            for (ProspectCensusResponse response : targetCensusData) {
                EmployeeStrategyGroup employeeStrategyGroup = new EmployeeStrategyGroup();
                employeeStrategyGroup.setEmplId(response.getEmployeeId());
                employeeStrategyGroup.setStrategyGroupId(strategyGroupId);
                allEmployeeStrategyGroups.add(employeeStrategyGroup);
            }
        }
    }

	private List<BenefitGroup> createProspectBenefitGroups(Company company, Strategy prospectStrategy,
                                                           boolean hasMAEmployee) {
		List<BenefitGroup> bgs = new ArrayList<>();
		// Creating W2 group
		BenefitGroup w2Bg = benefitGroupService.constructW2Group(company, true);
		bgs.add(w2Bg);

		if(company.isProspectCompany() || company.isK1Company()) {
			// Creating K1 Group
			BenefitGroup k1Bg = benefitGroupService.constructK1Group(company);
			bgs.add(k1Bg);
		}

        if (hasMAEmployee) {
            // Creating MA Group
            BenefitGroup maBg = benefitGroupService.constructMAGroup(company);
            bgs.add(maBg);
        }

		// NEED IMPLEMENTATION FOR MA groups
		bgs = benefitGroupService.saveAll(bgs);
		for (BenefitGroup benefitGroup : bgs) {
			BenefitGroupStrategy strategyBenefitGroup = null;
            boolean isNotDefaultGroup = K1_GROUP_TYPE.equals(benefitGroup.getType()) ||
                    CLIENT_MA_GROUP_NAME.equals(benefitGroup.getName());
            if (isNotDefaultGroup) {
				strategyBenefitGroup = BenefitGroupServiceHelper.constructStrategyBenefitGroup(benefitGroup,
						prospectStrategy, benefitGroup.getWaitingPeriod(), false);
			} else {
				strategyBenefitGroup = BenefitGroupServiceHelper.constructStrategyBenefitGroup(benefitGroup,
						prospectStrategy, benefitGroup.getWaitingPeriod(), true);
			}
			strategyGroupService.saveBenefitGroupStrategy(strategyBenefitGroup);
			benefitGroup.getBenefitGroupStrategy().add(strategyBenefitGroup);
		}
		return bgs;
	}

	private void createUpdateContribution(Company company, Map<String, BenefitPlan> benefitPlanMap,
			List<PlanSelection> planSelections, Map<String, List<BenefitPlanRate>> rates,
			Map<String, Map<String, Object>> groupFundingDetails, Map<String, XbssRealmPlyrPlan> plyrPlanMap,
			Map<String, Set<Long>> selectedPlancarriers, List<CarrierMinimumFunding> minFundings,
			List<String> fplMedicalPlans, Strategy rs) {
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();
		String fundingType = "";
		Map<String, Object> coverageLevelFunding = null;
		for (PlanSelection ps : planSelections) {
			BenefitPlan bp = benefitPlanMap.get(ps.getBenefitPlan());

			if (null != groupFundingDetails) {
				if (Constants.dentalPlanTypeList.contains(bp.getPlanType())) {
					coverageLevelFunding = groupFundingDetails.get(Constants.DENTAL_CODE);
				} else if (Constants.visionPlanTypeList.contains(bp.getPlanType())) {
					coverageLevelFunding = groupFundingDetails.get(Constants.VISION_CODE);
				} else {
					coverageLevelFunding = groupFundingDetails.get(Constants.MEDICAL_CODE);
				}
			}

			if (null != coverageLevelFunding && !coverageLevelFunding.isEmpty()) {
				if (null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE)) {
					fundingType = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
				}
				List<Contribution> contribList = RenewalServiceHelper.createUpdateContributionsByBaseFunding(
						bp, ps, rates, null, coverageLevelFunding, null, false);
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
	}

	private List<StrategyBenefitGroup> getStrategyBenefitGroup(List<BenefitsDetailsResponse> benefitsDetailsResponse) {
		return benefitsDetailsResponse.stream().map(benefitDetails -> {
			StrategyBenefitGroup strategyBenefitGroup = new StrategyBenefitGroup();
			strategyBenefitGroup.setId(benefitDetails.getGroupId());
			strategyBenefitGroup.setName(benefitDetails.getGroupName());
			// needs to be cleaned up
			strategyBenefitGroup.setType(benefitDetails.getGroupType());
			strategyBenefitGroup.setWaitingPeriod("NONE");
			strategyBenefitGroup.setStatus("A");
			strategyBenefitGroup.setBenefitProgram(null);
			strategyBenefitGroup.setStrategyId(ProspectConstants.PROSPECT_STRATEGY_ID);
			strategyBenefitGroup.setStrategyGroupId(benefitDetails.getGroupId());
			strategyBenefitGroup.setEstimatedTotalCost(benefitDetails.getMonthlyTotal());
			strategyBenefitGroup.setHeadcount(benefitDetails.getHeadCount());
			strategyBenefitGroup.setBenefitOffers(getBenefitOffers(benefitDetails));
			return strategyBenefitGroup;
		}).collect(Collectors.toList());
	}

	private List<BenefitOffer> getBenefitOffers(BenefitsDetailsResponse benefitDetails) {
		List<BenefitOffer> benOfferList = benefitDetails.getBenefitTypes().stream()
				.filter(benefitType -> !benefitType.getBenefitTypeCode().equalsIgnoreCase(Constants.LIFE_CODE)
						&& !benefitType.getBenefitTypeCode().equalsIgnoreCase(Constants.LTD_CODE)
						&& !benefitType.getBenefitTypeCode().equalsIgnoreCase(Constants.STD_CODE))
				.map(benefitType -> {
					BenefitOffer benefitOffer = new BenefitOffer();
					BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();
					benefitOfferSummary.setType(PlanTypesEnum.getName(benefitType.getBenefitTypeCode()));
					benefitOfferSummary.setGroupId(benefitDetails.getGroupId());
					benefitOfferSummary.setDescription(benefitType.getBenefitTypeCode());
					benefitOfferSummary.setEstimatedTotalCost(benefitType.getMonthlyTotal());
					benefitOffer.setSummary(benefitOfferSummary);
					benefitOffer.setPlanCarriers(getPlanCarriers(benefitType));
					benefitOffer.setPlanPackage(getPlanPackage(benefitType));

					return benefitOffer;
				}).collect(Collectors.toList());

		// AdditionalBenefit Total Amount (Life + DIsability (LTD + STD))
		BigDecimal additionalBenTotalCost = benefitDetails.getBenefitTypes().stream()
				.filter(benType -> benType.getBenefitTypeCode().equalsIgnoreCase(Constants.LIFE_CODE)
						|| benType.getBenefitTypeCode().equalsIgnoreCase(Constants.LTD_CODE)
						|| benType.getBenefitTypeCode().equalsIgnoreCase(Constants.STD_CODE))
				.map(benType -> benType.getMonthlyTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);

		// Disability Total Amount (LTD + STD)
		BigDecimal disabilityTotalCost = benefitDetails.getBenefitTypes().stream()
				.filter(benType -> benType.getBenefitTypeCode().equalsIgnoreCase(Constants.LTD_CODE)
						|| benType.getBenefitTypeCode().equalsIgnoreCase(Constants.STD_CODE))
				.map(benType -> benType.getMonthlyTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);

		BenefitType lifeObj = benefitDetails.getBenefitTypes().stream()
				.filter(benType -> benType.getBenefitTypeCode().equalsIgnoreCase(Constants.LIFE_CODE)).findAny()
				.orElse(null);

		List<BenefitType> disabalityObjs = benefitDetails.getBenefitTypes().stream()
				.filter(benType -> benType.getBenefitTypeCode().equalsIgnoreCase(Constants.LTD_CODE)
						|| benType.getBenefitTypeCode().equalsIgnoreCase(Constants.STD_CODE))
				.collect(Collectors.toList());

		if (lifeObj != null || disabalityObjs != null) {
			BenefitOffer benefitOffer = new BenefitOffer();
			BenefitOfferSummary benefitOfferSummary = new BenefitOfferSummary();

			benefitOfferSummary.setType(Constants.ADDITIONAL);
			benefitOfferSummary.setGroupId(benefitDetails.getGroupId());

			benefitOfferSummary.setHeadcount(benefitDetails.getHeadCount());
			benefitOfferSummary.setEstimatedTotalCost(additionalBenTotalCost);
			benefitOffer.setSummary(benefitOfferSummary);

			List<AdditionalBenefitOffer> additionalBenOfferList = new ArrayList<>();
			if (lifeObj != null) {
				AdditionalBenefitOffer addBenOffer = addLifeObject(benefitDetails, lifeObj);
				additionalBenOfferList.add(addBenOffer);
			}
			if (disabalityObjs != null) {
				AdditionalBenefitOffer addBenOffer = addDisabilityObjects(benefitDetails, disabalityObjs,
						disabilityTotalCost);
				additionalBenOfferList.add(addBenOffer);
			}
			benefitOffer.setAdditionalBenefitOffers(additionalBenOfferList);
			benOfferList.add(benefitOffer);
		}

		return benOfferList;
	}

	private AdditionalBenefitOffer addLifeObject(BenefitsDetailsResponse benefitDetails, BenefitType lifeObj) {
		AdditionalBenefitOffer addBenOffer = new AdditionalBenefitOffer();
		BenefitOfferSummary bnftOfferSummary = new BenefitOfferSummary();

		bnftOfferSummary.setType(PlanTypesEnum.getName(lifeObj.getBenefitTypeCode()));
		bnftOfferSummary.setGroupId(benefitDetails.getGroupId());
		bnftOfferSummary.setDescription(lifeObj.getBenefitTypeCode());
		bnftOfferSummary.setHeadcount(benefitDetails.getHeadCount());
		bnftOfferSummary.setEstimatedTotalCost(lifeObj.getMonthlyTotal());
		addBenOffer.setSummary(bnftOfferSummary);

		List<AdditionalBenefitPlan> additionalBenPlanList = new ArrayList<>();
		AdditionalBenefitPlan additionalBenPlan = new AdditionalBenefitPlan();

		additionalBenPlan.setPlanType(lifeObj.getBenefitTypeCode());
		additionalBenPlan.setPlanCost(lifeObj.getMonthlyTotal());
		additionalBenPlanList.add(additionalBenPlan);

		addBenOffer.setAdditionalBenefitPlans(additionalBenPlanList);
		return addBenOffer;
	}

	private AdditionalBenefitOffer addDisabilityObjects(BenefitsDetailsResponse benefitDetails,
			List<BenefitType> disabalityObjs, BigDecimal disabilityAmount) {
		AdditionalBenefitOffer addBenOffer = new AdditionalBenefitOffer();
		BenefitOfferSummary bnftOfferSummary = new BenefitOfferSummary();

		bnftOfferSummary.setType(Constants.DISABILITY);
		bnftOfferSummary.setGroupId(benefitDetails.getGroupId());
		bnftOfferSummary.setDescription(Constants.STD_AND_LTD);
		bnftOfferSummary.setHeadcount(benefitDetails.getHeadCount());
		bnftOfferSummary.setEstimatedTotalCost(disabilityAmount);
		addBenOffer.setSummary(bnftOfferSummary);

		List<AdditionalBenefitPlan> additionalBenPlanList = new ArrayList<>();

		for (BenefitType disObj : disabalityObjs) {
			AdditionalBenefitPlan additionalBenPlan = new AdditionalBenefitPlan();
			additionalBenPlan.setPlanType(disObj.getBenefitTypeCode());
			if (disObj.getBenefitTypeCode().equalsIgnoreCase(Constants.LTD_CODE))
				additionalBenPlan.setDescription(Constants.LTD);
			else
				additionalBenPlan.setDescription(Constants.STD);
			additionalBenPlan.setPlanCost(disObj.getMonthlyTotal());
			additionalBenPlanList.add(additionalBenPlan);
		}
		addBenOffer.setAdditionalBenefitPlans(additionalBenPlanList);
		return addBenOffer;
	}

	private Set<PlanCarrier> getPlanCarriers(BenefitsDetailsResponse.BenefitType benefitTypes) {
		AtomicInteger count = new AtomicInteger(1);
		return benefitTypes.getPlanCarriers().stream().map(carrier -> {
			PlanCarrier planCarrier = new PlanCarrier();
			planCarrier.setId(count.getAndIncrement());
			planCarrier.setName(carrier);
			return planCarrier;
		}).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private PlanPackage getPlanPackage(BenefitsDetailsResponse.BenefitType benefitType) {
		PlanPackage planPackage = new PlanPackage();
		planPackage.setFundingModelId(0L);
		planPackage.setName("renewal");
		planPackage.setCustomized(true);
		planPackage.setStrategyId(0L);
		if (null != benefitType.getFundingDetails()) {
			planPackage.setFundingType(benefitType.getFundingDetails().getFundingType());
			Map<String, BigDecimal> mapOfCovgLevelFunding = new HashMap<>();
			benefitType.getFundingDetails().getCvgCodeValues()
					.forEach((key, value) -> mapOfCovgLevelFunding.put(CoverageCodesEnums.valueOfId(key), value));
			planPackage.setCoverageLevelFunding(mapOfCovgLevelFunding);
			buildBasePlanLimits(planPackage, benefitType.getFundingDetails().getFundingCapDetails());
		}
		return planPackage;
	}

	private void buildBasePlanLimits(PlanPackage planPackage, FundingCapDetails fundingCapDetails) {
		if (Objects.isNull(fundingCapDetails) || MapUtils.isEmpty(fundingCapDetails.getCvgCodeValues())) {
			return;
		}
		Map<String, BigDecimal> transformedCvgCodeValues = fundingCapDetails.getCvgCodeValues().entrySet().stream()
				.collect(Collectors.toMap(entry -> CoverageCodesEnums.valueOfId(entry.getKey()), Entry::getValue));
		if (CapTypeEnum.DOLLAR.getCapType().equalsIgnoreCase(fundingCapDetails.getCapType())) {
			planPackage.setFundingBasePlan(BSSApplicationConstants.FLAT_MAX);
			planPackage.setCoverageLevelFundingFlatMax(transformedCvgCodeValues);
		} else {
			planPackage.setFundingBasePlan(fundingCapDetails.getCapPlanId());
			planPackage.setFundingBasePlanName(fundingCapDetails.getCapPlanName());
			planPackage.setCoverageLevelBasePlanLimits(transformedCvgCodeValues);
			FundingBasePlan fundingBasePlan = new FundingBasePlan();
			fundingBasePlan.setFundingBasePlan(fundingCapDetails.getCapPlanId());
			planPackage.setFundingBasePlans(List.of(fundingBasePlan));
		}
	}

	private List<BenefitsDetailsResponse> getProspectBenefitDetails(String companyCode) {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		queryParams.put(ProspectConstants.PROSPECT_ID_REQ_PARAM, List.of(companyCode));
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(ProspectConstants.BENEFITS_TYPES_PATH_PARAM, ProspectConstants.BENEFITS_TYPES_VALUES);

		ParameterizedTypeReference<ReturnResponse<List<BenefitsDetailsResponse>>> prospectDetailsBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<List<BenefitsDetailsResponse>> prospectApiGetRequest = ProspectApiRequest.<List<BenefitsDetailsResponse>>builder()
				.method(HttpMethod.GET)
				.uri(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_STRATEGY_URI))
				.queryParams(queryParams)
				.pathParams(pathParams)
				.parameterizedTypeReference(prospectDetailsBean).build();

		@SuppressWarnings("unchecked")
		List<BenefitsDetailsResponse> data = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		return data;
	}

	private ModelCompareStrategyCost getStrategyPlanCosts(List<BenefitsDetailsResponse> benefitsDetailsResponse) {

		ModelCompareStrategyCost modelCompareStrategyCost = new ModelCompareStrategyCost();
		modelCompareStrategyCost.setStrategyId(ProspectConstants.PROSPECT_STRATEGY_ID);
		modelCompareStrategyCost.setBenefitGroups(benefitsDetailsResponse.stream()
				.map(benefitDetails -> getBenefitGroup(benefitDetails)).collect(Collectors.toList()));

		modelCompareStrategyCost.setPlanTypeCosts(getPlanTypeCosts(benefitsDetailsResponse));

		return modelCompareStrategyCost;
	}

	private BenefitGroup getBenefitGroup(BenefitsDetailsResponse benefitDetails) {
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setId(benefitDetails.getGroupId());
		benefitGroup.setName(benefitDetails.getGroupName());
		return benefitGroup;
	}

	private List<ModelComparePlanTypeCost> getPlanTypeCosts(List<BenefitsDetailsResponse> benefitsDetailsResponse) {

		Map<String, BenefitsDetailsResponse.BenefitType> combinedCostData = benefitsDetailsResponse.stream()
				.map(BenefitsDetailsResponse::getBenefitTypes).flatMap(List::stream)
				.collect(groupingBy(benefitTypes -> benefitTypes.getBenefitTypeCode(),
						collectingAndThen(
								reducing((a, b) -> new BenefitsDetailsResponse.BenefitType(a.getBenefitTypeCode(),
										a.getMonthlyTotal().add(b.getMonthlyTotal()), null, null)),
								Optional::get)));

		return combinedCostData.values().stream().map(benefitTypes -> {
			ModelComparePlanTypeCost modelComparePlanTypeCost = new ModelComparePlanTypeCost();
			modelComparePlanTypeCost.setPlanType(PlanTypesEnum.getName(benefitTypes.getBenefitTypeCode()));
			modelComparePlanTypeCost.setCost(benefitTypes.getMonthlyTotal());
			modelComparePlanTypeCost.setOffered(true);
			return modelComparePlanTypeCost;
		}).collect(Collectors.toList());
	}


	private void updateBundleIdInCompany(Company company) {
		Bundle bundle = benefitsBundleService.getBundleByCompanyCode(company.getProspectId());
		if(bundle!=null){
			company.setBundleId(bundle.getId());
			companyService.createUpdateCompany(company);
		}else {
				throw new BSSBadDataException(String.format(
						"No bundle data found for Large deal prospect Id: %s", company.getProspectId()));
		}
	}

	private void updateBundleDetailsForBenBundle(Company company) {
		// If the bundleId is -1, then store it as NULL
		Long bundleId = company.getBundleId();
		if (bundleId != null && bundleId.longValue() < 0) {
			company.setBundleId(null);
		}
		// Handle bundle sequence independently
		updateBundleSequence(company);
		companyService.createUpdateCompany(company);
	}

	private void updateBundleSequence(Company company) {
		int currentBundleSeq = company.getBundleSeq();
		int maxBundleSeq = AppRulesAndConfigsUtils.getMaxBundleSeq();
		if (currentBundleSeq < maxBundleSeq) {
			company.setBundleSeq(currentBundleSeq + 1);
		} else if (currentBundleSeq == maxBundleSeq) {
			company.setBundleSeq(9);
		}
	}

}