package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.service.BenefitPlanService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.HeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDefaultPlanDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.StrategyDefaultPlan;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.sp.GetNextEligRulesId;
import com.trinet.ambis.persistence.sp.NextRateTblID;
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
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyFundingDetailService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyHsaFundingService;
import com.trinet.ambis.service.impl.StrategyRenewalServiceImpl;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class StrategyRenewalServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	StrategyRenewalServiceImpl strategyRenewalService;

	@Mock
	RenewalDataDao renewalDataDao;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	PlanMappingDao planMappingDao;

	@Mock
	BenefitGroupService benefitGroupService;

	@Mock
	StrategyGroupService benefitGroupStrategyService;

	@Mock
	BenefitClassService benefitClassService;

	@Mock
	CompanyService companyService;

	@Mock
	StrategyDao strategyDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	PlanSelectionService planSelectionService;

	@Mock
	RealmPlanYearDao realmPlanYearDao;

	@Mock
	CompanyDao companyDao;

	@Mock
	EmployeeSelectionDao employeeSelectionDao;

	@Mock
	EmployerEmployeePlansMappingService employerEmployeePlansMappingService;

	@Mock
	PortfolioRuleDao portfolioRuleDao;

	@Mock
	BenefitPlanDao benefitPlanDao;

	@Mock
	NextRateTblID nextRateTblID;

	@Mock
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	@Mock
	BenefitOfferExceptionService benOfferExceptionService;

	@Mock
	HeadCountDao headCountDao;

	@Mock
	StrategyHsaFundingService strategyHsaFundingService;

	@Mock
	HeadCountService headCountService;

	@Mock
	RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	PortfolioService portfolioService;

	@Mock
	DisabilityOptionService disabilityOptionService;

	@Mock
	GetNextEligRulesId spGetNextEligRulesId;

	@Mock
	PlanRatesService planRatesService;

	@Mock
	GroupRuleService groupRuleService;

	@Mock
	StrategyFundingModelService strategyFundingModelService;

	@Mock
	XbssRealmPlyrPlanDao realmPlyrPlanDao;

	@Mock
	ContributionService contributionService;

	@Mock
	EmployeeDataService employeeDataService;

	@Mock
	CommonDataDao commonDataDao;

	@Mock
	StrategyFundingDetailService strategyFundingDetailService;
	
	@Mock
	CacheService cacheService;

	@Mock
	BenefitPlanService benefitPlanService;

	@Mock
	StrategyDefaultPlanDao strategyDefaultPlanDao;

	private static final String COMPANY_CODE = "G48";
	private static final long REALM_PLAN_YR_ID = 1111L;
	private static final long REALM_ID = 11;
	private static final String QUARTER = "Q4";
	private static final String PF_CLIENT = "PfClient";
	private static final String HQ_STATE = "MA";
	private static final Date PLYR_START_DATE = new Date();
	private static final long PREV_REALM_PLAN_YR_ID = 2222L;
	private static final Date PREV_PLAN_YR_END = new Date();
	private static final String EXCL_MED_PLAN = "DFLT";
	private static final String MED_BEN_PLAN_ID = "MED1234";
	private static final String MED_BEN_PLAN_ID1 = "MED1111";
	private static final String MED_BEN_PLAN_ID2 = "MED2222";
	private static final String DEN_BEN_PLAN_ID = "DEN1234";
	private static final String VIS_BEN_PLAN_ID = "VIS1234";
	private static final String AD_BEN_PLAN_ID_1111 = "1111";
	private static final String AD_BEN_PLAN_ID_2222 = "2222";
	private static final String NEW_PLAN_1 = "newPlan1";
	private static final String NEW_PLAN_2 = "newPlan2";
	private static final String NEW_ELIG_RULE = "ELIG";
	private static final String LIFE_PLAN_ID = "LIFE_PLAN_001";

	private static final Long STRATEGY_ID = 1111L;
	private static final long BEN_GROUP_ID_K1 = 2222L;
	private static final String BEN_PROG_K1 = "benProg1";
	private static final long BEN_GROUP_ID_STD = 3333L;
	private static final String BEN_PROG_STD = "benProgStd";

    private MockedStatic<RenewalServiceHelper> renewalServiceHelperMock;
    private MockedStatic<StrategyServiceHelper> strategyServiceHelperMock;
    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMock;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMock;

    @Before
    public void setUp() {
        renewalServiceHelperMock = Mockito.mockStatic(RenewalServiceHelper.class);
        strategyServiceHelperMock = Mockito.mockStatic(StrategyServiceHelper.class);
        benefitCategoriesHelperMock = Mockito.mockStatic(BenefitCategoriesHelper.class);
        commonServiceHelperMock = Mockito.mockStatic(CommonServiceHelper.class);
    }

    @After
    public void tearDown() {
        renewalServiceHelperMock.close();
        strategyServiceHelperMock.close();
        benefitCategoriesHelperMock.close();
        commonServiceHelperMock.close();
    }
	@Test
	public void createFutureStrategies() {
		Company company = prepareCompany();
		boolean isMigratedCompany = true;
		RealmPlanYear realmPlanYear = prepareRealmPlanYear();
		boolean isDefaultSubmit = true;
		boolean isPreload = true;
		
		List<Strategy> strategies = prepareRenewalStrategies();
		List<Strategy> renewalStrategies = prepareRenewalStrategies();
		List<BenefitGroup> benefitGroups = prepareBenefitPrograms();
		Map<String, Integer> groupHeadCountMap = prepareGroupHeadCountMap();
		Company previousYearCompany =  preparePriorCompany();
		Strategy previousSubmittedStrategy = strategies.get(0);
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = prepareBgsHealthPlanMap();
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap = prepareBgsADPlansMap();
		List<BenefitGroupStrategy> benefitGroupStrategies = prepareBenGroupStrategies();
		Map<String, Set<Long>> strategyGroupBenefitProgramMap = prepareStrategyGroupBenefitProgramMap();
		Map<String, Map<String, BenefitPlan>> bgAllHealthPlansMap = prepareBGAllHealthPlansMap();
		Map<String, Set<Long>> selectedPlancarriers = prepareSelectedPlancarriers();
		Map<String, StateBenefitPlan> mandatoryPlans = prepareMandatoryPlans();
		List<PlanSelection> planSelections = preparePlanSelections();
		Map<String, String> rateTblIds = prepareRateIds();
		List<String> medPlans = prepareMedPlans();
		Map<String, List<BenefitPlanRate>> rates = prepareRates();
		List<String> mandatoryPlansToExclude = prepareMandatoryPlansToExclude();
		Map<String, BigDecimal> minimumFunding = prepareMinimumFundingMap();
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = prepareGroupFundingDetails();
		List<CarrierMinimumFunding> minFundings = null;
		Map<String, String> waitPeriodMap = new HashMap<>();
		Map<String, Map<String, Map<String, String>>> planOverrideMap = new HashMap<>();
		Set<String> locations = new HashSet<>();
		Map<String, String> erEeMapping = new HashMap<>();
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Set<String> primaryPlanCarriers = new HashSet<>();
		Map<String, Set<Long>> mandatoryPortfoliosMap = new HashMap<>();
		Set<String> medicalPlanCarriers = new HashSet<>();
		Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap = new HashMap<>();
		List<String> bcAdPlans = new ArrayList<>();
		Map<String, String> realmRuleConfigurations = new HashMap<>();
		realmRuleConfigurations.put("COST_SHARE_STRATEGIES", "true");
		Map<String, PlanMapping> realmPlanMapping = prepareRealmPlanMapping();
		when(realmPlanYearDao.findPreviousRealmPlanYearByRealmIdAndOeQuarter(REALM_PLAN_YR_ID, REALM_ID, QUARTER))
				.thenReturn(preparePrevRealmPlYr());
		when(renewalDataDao.getBenefitPrograms(PF_CLIENT, PREV_PLAN_YR_END)).thenReturn(prepareBenefitPrograms());

		when(companyDao.findByCodeAndRealmPlanYearId(COMPANY_CODE, PREV_REALM_PLAN_YR_ID))
				.thenReturn(previousYearCompany);

		when(spGetNextEligRulesId.execute()).thenReturn(NEW_ELIG_RULE);

		when(companyService.createUpdateCompany(company)).thenReturn(company);
		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigService);
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(company.getRealmPlanYearId()))
				.thenReturn(realmRuleConfigurations);
		
		when(strategyDao.findByCompanyIdAndSubmitted(previousYearCompany.getId(), true))
		.thenReturn(strategies);
		
		when(RenewalServiceHelper.constructRenewalStrategies(company, isDefaultSubmit,
				realmRuleConfigurations, isPreload, previousSubmittedStrategy.getAcaFplOpted())).thenReturn(renewalStrategies);
		when(strategyDao.saveAll(renewalStrategies)).thenReturn(renewalStrategies);
		Set<String> outOfRegionPlans = new HashSet<>();
		when(CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers, realmDataDao))
				.thenReturn(outOfRegionPlans);
		Mockito.when(planMappingDao.getPrimaryPlanMappings(company, outOfRegionPlans))
				.thenReturn(prepareRealmPlanMapping());
		when(renewalDataDao.getEligRuleIdsByClient(PF_CLIENT, PREV_PLAN_YR_END))
				.thenReturn(new HashMap<String, String>());
		when(renewalDataDao.getWaitPeriodByClient(PF_CLIENT, COMPANY_CODE, PREV_PLAN_YR_END)).thenReturn(waitPeriodMap);
		when(strategyDataDao.getOverridesByBenefitGroup(COMPANY_CODE, PREV_REALM_PLAN_YR_ID, false))
				.thenReturn(planOverrideMap);
		when(StrategyServiceHelper.getLocations(company)).thenReturn(locations);
		when(employerEmployeePlansMappingService.getEeAndErPlanMapping(PREV_REALM_PLAN_YR_ID)).thenReturn(erEeMapping);
		when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, REALM_PLAN_YR_ID))
				.thenReturn(mapOfCoverageLevels);
		when(BenefitCategoriesHelper.getPlanCarriers(planCarrierMap)).thenReturn(primaryPlanCarriers);
		when(BenefitCategoriesHelper.getMedicalPlanCarriers(planCarrierMap))
				.thenReturn(medicalPlanCarriers);
		when(BenefitCategoriesHelper.getMandatoryPlanCarriers(planCarrierMap))
				.thenReturn(mandatoryPortfoliosMap);
		when(benefitPlanDao.getAllPrimaryBenefitPlans(primaryPlanCarriers, company, new HashSet<String>()))
				.thenReturn(allBenefitStatePlansMap);
		when(realmDataDao.getADBenefitPlans(company)).thenReturn(bcAdPlans);
		when(renewalDataDao.getRateTableIds(BEN_PROG_K1, PREV_PLAN_YR_END)).thenReturn(rateTblIds);
		when(renewalDataDao.getRateTableIds(BEN_PROG_STD, PREV_PLAN_YR_END)).thenReturn(rateTblIds);
		when(benefitClassService.generateAllClassCodes(Mockito.any(Company.class), Mockito.anyList()))
				.thenReturn(benefitGroups);
		when(realmDataDao.validateRateTableId("rateTbId10", BEN_PROG_K1)).thenReturn(false);
		when(benefitGroupService.saveAll(benefitGroups)).thenReturn(benefitGroups);
		when(nextRateTblID.execute()).thenReturn(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		when(realmDataDao.getADBenefitPlans(company)).thenReturn(prepareBcAdPlans());
		when(RenewalServiceHelper.constructStrategyBenefitGroups(renewalStrategies, benefitGroups,
				waitPeriodMap, groupHeadCountMap, company, null)).thenReturn(benefitGroupStrategies);
		when(benefitGroupStrategyService.saveBenefitGroupStrategies(benefitGroupStrategies))
				.thenReturn(benefitGroupStrategies);
		when(RenewalServiceHelper.validateK1RateTableId(Mockito.anyList(), Mockito.anyString()))
				.thenReturn(true);
		when(RenewalServiceHelper.getStrategyGroupByBenefitProgram(benefitGroupStrategies))
				.thenReturn(strategyGroupBenefitProgramMap);
		when(RenewalServiceHelper.getHealthPlansForAllBenefitGroups(bgsHealthPlansMap, erEeMapping,
				mapOfCoverageLevels)).thenReturn(bgAllHealthPlansMap);
		when(RenewalServiceHelper.getSelectedPlanCarriers(bgAllHealthPlansMap, allBenefitStatePlansMap,
				planCarrierMap, realmPlanMapping)).thenReturn(selectedPlancarriers);
		when(RenewalServiceHelper.getAllMandatoryPlans(company, allBenefitStatePlansMap, selectedPlancarriers))
				.thenReturn(mandatoryPlans);

		when(realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(company.getHeadQuatersState(),
				BigDecimal.valueOf(company.getRealmPlanYearId()))).thenReturn(mandatoryPlansToExclude);

		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
		when(RenewalServiceHelper.getMinimumFunding(planSelections, rates, mandatoryPlansToExclude,
				company, selectedPlancarriers, minFundings)).thenReturn(minimumFunding);
		when(realmDataDao.getRenewalFundingDetailsBSS(company.getCode(), PREV_REALM_PLAN_YR_ID))
				.thenReturn(groupFundingDetails);

		when(RenewalServiceHelper.getBCPlansByType(allBenefitStatePlansMap, Constants.MEDICAL))
				.thenReturn(medPlans);
		when(StrategyServiceHelper.constructPlanSelection(Mockito.any(Long.class), Mockito.any(Long.class),
				Mockito.any(BenefitPlan.class), Mockito.any(Long.class))).thenReturn(preparePlanSelection());
		when(planSelectionService.saveAll(Mockito.any(List.class))).thenReturn(planSelections);

		Map<String, BenefitGroup> actualResult = strategyRenewalService.createFutureStrategies(company,
				isMigratedCompany, realmPlanYear, isDefaultSubmit, bgsHealthPlansMap, bgsADPlansMap, isPreload);

		assertEquals(2, actualResult.size());
		assertEquals(BEN_PROG_K1, actualResult.get(BEN_PROG_K1).getBenefitProgram());
		assertEquals(BEN_PROG_STD, actualResult.get(BEN_PROG_STD).getBenefitProgram());

		// addDefaultLifePlanIfMissing – branch 1: Life plan (23) already present in
		// adPlansMap → DAO must never be consulted
		Mockito.verify(strategyDefaultPlanDao, Mockito.never())
				.findBy(Mockito.anyString(), Mockito.anyList(),
						Mockito.any(Date.class));

		// addDefaultLifePlanIfMissing – branch 2: Life plan absent, default found →
		// constructADPlanSelection called with the default Life plan (once per group)
		Map<String, Map<String, Map<String, BenefitPlan>>> adPlansWithoutLife = prepareBgsADPlansMapWithoutLifePlan();
		when(strategyDefaultPlanDao.findBy(QUARTER,
				Collections.singletonList(BSSApplicationConstants.LIFE_CODE), PLYR_START_DATE))
				.thenReturn(Collections.singletonList(prepareDefaultLifePlan()));

		strategyRenewalService.createFutureStrategies(company, isMigratedCompany, realmPlanYear,
				isDefaultSubmit, bgsHealthPlansMap, adPlansWithoutLife, isPreload);

		strategyServiceHelperMock.verify(
				() -> StrategyServiceHelper.constructADPlanSelection(
						Mockito.eq(STRATEGY_ID), Mockito.anyLong(),
						Mockito.eq(LIFE_PLAN_ID), Mockito.eq(BSSApplicationConstants.LIFE_CODE), Mockito.eq(0L)),
				Mockito.times(2)); // once per benefit group (K1 + STD)

		// addDefaultLifePlanIfMissing – branch 3: Life plan absent, no default → nothing
		// extra added (constructADPlanSelection not called again with LIFE_PLAN_ID)
		when(strategyDefaultPlanDao.findBy(QUARTER,
				Collections.singletonList(BSSApplicationConstants.LIFE_CODE), PLYR_START_DATE))
				.thenReturn(Collections.emptyList());

		strategyRenewalService.createFutureStrategies(company, isMigratedCompany, realmPlanYear,
				isDefaultSubmit, bgsHealthPlansMap, adPlansWithoutLife, isPreload);

		// still times(2) — no additional calls from the third invocation
		strategyServiceHelperMock.verify(
				() -> StrategyServiceHelper.constructADPlanSelection(
						Mockito.eq(STRATEGY_ID), Mockito.anyLong(),
						Mockito.eq(LIFE_PLAN_ID), Mockito.eq(BSSApplicationConstants.LIFE_CODE), Mockito.eq(0L)),
				Mockito.times(2));

		// addDefaultLifePlanIfMissing – check if realm plan year <= 86,
		// skip default life insertion even when Life is missing and a default exists.
		RealmPlanYear lowRealmPlanYear = prepareRealmPlanYear();
		lowRealmPlanYear.setId(86L);
		company.setRealmPlanYear(lowRealmPlanYear);

		strategyRenewalService.createFutureStrategies(company, isMigratedCompany, realmPlanYear,
				isDefaultSubmit, bgsHealthPlansMap, adPlansWithoutLife, isPreload);

		// No extra LIFE default insertions beyond the first eligible (>86) invocation.
		strategyServiceHelperMock.verify(
				() -> StrategyServiceHelper.constructADPlanSelection(
						Mockito.eq(STRATEGY_ID), Mockito.anyLong(),
						Mockito.eq(LIFE_PLAN_ID), Mockito.eq(BSSApplicationConstants.LIFE_CODE), Mockito.eq(0L)),
				Mockito.times(2));

		// <=86 should not add any new DAO calls.
		// Total remains 4 (2 groups x 2 earlier missing-life runs).
		Mockito.verify(strategyDefaultPlanDao, Mockito.times(4))
				.findBy(QUARTER, Collections.singletonList(BSSApplicationConstants.LIFE_CODE), PLYR_START_DATE);

		assertNotNull(company.getBundleId());
	}

	private Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> prepareGroupCovrgHeadCountMap() {
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> map = new HashMap<>();
		Map<String, List<PlanCoverageLevelHeadCount>> covgLvlHeadCountMap = new HashMap<>();
		List<PlanCoverageLevelHeadCount> covgLvlHeadCounts = new ArrayList<>();
		PlanCoverageLevelHeadCount covgLvlHeadCount = new PlanCoverageLevelHeadCount();
		covgLvlHeadCounts.add(covgLvlHeadCount);
		covgLvlHeadCountMap.put(MED_BEN_PLAN_ID1, covgLvlHeadCounts);
		map.put(BEN_PROG_K1, covgLvlHeadCountMap);
		return map;
	}

	private List<String> prepareBcAdPlans() {
		List<String> bcAdPlans = new ArrayList<>();
		bcAdPlans.add(AD_BEN_PLAN_ID_1111);
		bcAdPlans.add(AD_BEN_PLAN_ID_2222);
		return bcAdPlans;
	}

	private List<PlanSelection> preparePlanSelections() {
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection ps = new PlanSelection();
		ps.setBenefitPlan(MED_BEN_PLAN_ID);
		planSelections.add(ps);
		ps = new PlanSelection();
		ps.setBenefitPlan(DEN_BEN_PLAN_ID);
		planSelections.add(ps);
		ps = new PlanSelection();
		ps.setBenefitPlan(VIS_BEN_PLAN_ID);
		planSelections.add(ps);
		return planSelections;
	}

	private PlanSelection preparePlanSelection() {
		PlanSelection planSelection = new PlanSelection();
		planSelection.setBenefitPlan(AD_BEN_PLAN_ID_1111);
		planSelection.setHeadCount(2);
		return planSelection;
	}

	private List<String> prepareMedPlans() {
		List<String> plans = new ArrayList<>();
		plans.add(MED_BEN_PLAN_ID);
		return plans;
	}

	private Map<String, StateBenefitPlan> prepareMandatoryPlans() {
		return new HashMap<>();
	}

	private List<String> prepareMandatoryPlansToExclude() {
		return Arrays.asList("MED_BEN_PLAN_ID2");
	}

	private Map<String, Set<Long>> prepareSelectedPlancarriers() {
		Map<String, Set<Long>> map = new HashMap<>();
		map.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, Collections.<Long>emptySet());
		return map;
	}

	private Map<String, Map<String, BenefitPlan>> prepareBGAllHealthPlansMap() {
		Map<String, Map<String, BenefitPlan>> map = new HashMap<>();
		Map<String, BenefitPlan> benPlans = new HashMap<>();
		BenefitPlan benPlan = new BenefitPlan();
		benPlan.setPlanType(BSSApplicationConstants.MEDICAL);
		benPlan.setId(MED_BEN_PLAN_ID);
		benPlans.put(MED_BEN_PLAN_ID, benPlan);
		benPlan = new BenefitPlan();
		benPlan.setPlanType(BSSApplicationConstants.MEDICAL);
		benPlan.setId(MED_BEN_PLAN_ID1);
		benPlans.put(MED_BEN_PLAN_ID1, benPlan);
		map.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, benPlans);
		benPlans = new HashMap<>();
		benPlan = new BenefitPlan();
		benPlan.setPlanType(BSSApplicationConstants.DENTAL);
		benPlan.setId(DEN_BEN_PLAN_ID);
		benPlans.put(DEN_BEN_PLAN_ID, benPlan);
		map.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, benPlans);
		benPlans = new HashMap<>();
		benPlan = new BenefitPlan();
		benPlan.setPlanType(BSSApplicationConstants.VISION);
		benPlan.setId(VIS_BEN_PLAN_ID);
		benPlans.put(VIS_BEN_PLAN_ID, benPlan);
		map.put(BSSApplicationConstants.VISION_PLAN_TYPE, benPlans);
		return map;
	}

	private Company prepareCompany() {
		Company company = new Company();
		Realm realm = new Realm();
		realm.setId(REALM_ID);
		company.setRealm(realm);
		company.setRealmPlanYearId(REALM_PLAN_YR_ID);
		company.setQuater(QUARTER);
		company.setCode(COMPANY_CODE);
		company.setPfClient(PF_CLIENT);
		company.setExclusiveMedPlan(EXCL_MED_PLAN);
		company.setRealmPlanYear(prepareRealmPlanYear());
		company.setHeadQuatersState(HQ_STATE);	
		return company;
	}

	private Company preparePriorCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setPfClient(PF_CLIENT);
		company.setQuater(QUARTER);
		company.setExclusiveMedPlan(EXCL_MED_PLAN);
		Realm realm = new Realm();
		realm.setId(REALM_ID);
		company.setRealm(realm);
		company.setRealmPlanYear(preparePrevRealmPlYr());
		company.setRealmPlanYearId(company.getRealmPlanYear().getId());
		company.setBundleId(STRATEGY_ID);	
		company.setProspectId(PF_CLIENT);
		return company;
	}

	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(REALM_PLAN_YR_ID);
		rpy.setPlanYearStart(PLYR_START_DATE);
		rpy.setMbgRenewal(true);
		return rpy;
	}

	private Map<String, Map<String, Map<String, BenefitPlan>>> prepareBgsHealthPlanMap() {
		Map<String, Map<String, Map<String, BenefitPlan>>> map = new HashMap<>();
		Map<String, Map<String, BenefitPlan>> benPlanMap = new HashMap<>();
		Map<String, BenefitPlan> benPlans = new HashMap<>();
		BenefitPlan benPlan = new BenefitPlan();
		benPlan.setPlanType(BSSApplicationConstants.MEDICAL);
		benPlans.put("key", benPlan);
		benPlanMap.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, benPlans);
		benPlans = new HashMap<>();
		benPlan = new BenefitPlan();
		benPlan.setPlanType(BSSApplicationConstants.DENTAL);
		benPlans.put("key", benPlan);
		benPlanMap.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, benPlans);
		benPlans = new HashMap<>();
		benPlan = new BenefitPlan();
		benPlan.setPlanType(BSSApplicationConstants.VISION);
		benPlans.put("key", benPlan);
		benPlanMap.put(BSSApplicationConstants.VISION_PLAN_TYPE, benPlans);

		map.put(BEN_PROG_K1, benPlanMap);
		map.put(BEN_PROG_STD, benPlanMap);
		return map;
	}

	private Map<String, Map<String, Map<String, BenefitPlan>>> prepareBgsADPlansMap() {
		Map<String, Map<String, Map<String, BenefitPlan>>> map = new HashMap<>();
		Map<String, Map<String, BenefitPlan>> adMap = new HashMap<>();
		Map<String, BenefitPlan> adBenPlans = new HashMap<>();

		BenefitPlan benPlan = new BenefitPlan();
		benPlan.setId(AD_BEN_PLAN_ID_1111);
		benPlan.setPlanType("30");
		adBenPlans.put(AD_BEN_PLAN_ID_1111, benPlan);
		adMap.put("30", adBenPlans);

		benPlan = new BenefitPlan();
		benPlan.setId(AD_BEN_PLAN_ID_2222);
		benPlan.setPlanType("31");
		adBenPlans.put(AD_BEN_PLAN_ID_2222, benPlan);
		adMap.put("31", adBenPlans);

		benPlan = new BenefitPlan();
		benPlan.setId(AD_BEN_PLAN_ID_2222);
		benPlan.setPlanType("23");
		adBenPlans.put(AD_BEN_PLAN_ID_2222, benPlan);
		adMap.put("23", adBenPlans);

		map.put(BEN_PROG_K1, adMap);
		map.put(BEN_PROG_STD, adMap);
		return map;
	}

	private RealmPlanYear preparePrevRealmPlYr() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(PREV_REALM_PLAN_YR_ID);
		realmPlanYear.setPlanYearEnd(PREV_PLAN_YR_END);
		return realmPlanYear;
	}

	private List<BenefitGroup> prepareBenefitPrograms() {
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		BenefitGroup bg = prepareK1BenefitGroup();
		benefitGroups.add(bg);
		bg = prepareStdBenefitGroup();
		benefitGroups.add(bg);
		return benefitGroups;
	}
	
	private static AdditionalBenefitPlan prepareAdditionalBenefitPlan() {
		AdditionalBenefitPlan benPlan = new AdditionalBenefitPlan();
		benPlan.setId("1111");
		List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<DisabilityBenefitOptionPlans>();
		DisabilityBenefitOptionPlans plan = new DisabilityBenefitOptionPlans();
		plan.setPlanType("30");
		plan.setId("STD1");
		optionPlans.add(plan);
		plan = new DisabilityBenefitOptionPlans();
		plan.setPlanType("31");
		plan.setId("LTD1");
		optionPlans.add(plan);
		benPlan.setOptionPlans(optionPlans);
		benPlan.setStandAlone(false);

		return benPlan;
	}

	private BenefitGroup prepareK1BenefitGroup() {
		BenefitGroup bg = new BenefitGroup();
		bg.setId(BEN_GROUP_ID_K1);
		bg.setBenefitProgram(BEN_PROG_K1);
		bg.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		return bg;
	}

	private BenefitGroup prepareStdBenefitGroup() {
		BenefitGroup bg = new BenefitGroup();
		bg.setId(BEN_GROUP_ID_STD);
		bg.setBenefitProgram(BEN_PROG_STD);
		bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		return bg;
	}

	private List<Strategy> prepareRenewalStrategies() {
		List<Strategy> strategies = new ArrayList<>();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setCostShareType("DFLT");
		strategies.add(strategy);
		return strategies;
	}

	private Map<String, Integer> prepareGroupHeadCountMap() {
		Map<String, Integer> map = new HashMap<>();
		map.put("Grp1", 5);
		return map;
	}

	private Map<String, PlanMapping> prepareRealmPlanMapping() {
		Map<String, PlanMapping> map = new HashMap<>();
		PlanMapping rpm = new PlanMapping();
		rpm.setNewBenefitPlan(NEW_PLAN_1);
		map.put(AD_BEN_PLAN_ID_1111, rpm);
		rpm = new PlanMapping();
		rpm.setNewBenefitPlan(NEW_PLAN_2);
		map.put(AD_BEN_PLAN_ID_2222, rpm);
		rpm = new PlanMapping();
		rpm.setNewBenefitPlan(MED_BEN_PLAN_ID);
		map.put(MED_BEN_PLAN_ID, rpm);
		rpm = new PlanMapping();
		rpm.setNewBenefitPlan(MED_BEN_PLAN_ID2);
		map.put(MED_BEN_PLAN_ID1, rpm);
		rpm = new PlanMapping();
		rpm.setNewBenefitPlan(VIS_BEN_PLAN_ID);
		map.put(VIS_BEN_PLAN_ID, rpm);
		return map;
	}

	private Map<String, String> prepareRateIds() {
		Map<String, String> rateIds = new HashMap<>();
		rateIds.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, "rateTbId10");
		rateIds.put("15", "rateTbId15");
		rateIds.put("OTHER", "rateTbIdOther");
		return rateIds;
	}

	private List<BenefitGroupStrategy> prepareBenGroupStrategies() {
		List<BenefitGroupStrategy> list = new ArrayList<>();
		return list;
	}

	private Map<String, Set<Long>> prepareStrategyGroupBenefitProgramMap() {
		return new HashMap<>();
	}

	private Map<String, List<BenefitPlanRate>> prepareRates() {
		return new HashMap<String, List<BenefitPlanRate>>();
	}

	private Map<String, BigDecimal> prepareMinimumFundingMap() {
		Map<String, BigDecimal> minimumFundingMap = new HashMap<>();
		minimumFundingMap.put(PlanTypesEnum.MEDICAL.getCode(), BigDecimal.valueOf(400));
		minimumFundingMap.put(PlanTypesEnum.DENTAL.getCode(), BigDecimal.valueOf(40));
		minimumFundingMap.put(PlanTypesEnum.VISION.getCode(), BigDecimal.valueOf(20));
		return minimumFundingMap;
	}

	private Map<String, Map<String, Map<String, Object>>> prepareGroupFundingDetails() {
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = new HashMap<>();
		Map<String, Map<String, Object>> planTypeFundingDetails = new HashMap<>();

		// Medical funding FLAT
		Map<String, Object> fundingDetails = new HashMap<>();
		fundingDetails.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.FLAT);
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(200));
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(300));
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(250));
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(450));
		planTypeFundingDetails.put(PlanTypesEnum.MEDICAL.getCode(), fundingDetails);

		// Dental funding CFPCT
		fundingDetails = new HashMap<>();
		fundingDetails.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.CFPCT);
		fundingDetails.put(BSSApplicationConstants.FUNDING_BASE_PLAN, BSSApplicationConstants.FLAT_MAX);
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE.getId() + BSSApplicationConstants.LIMIT,
				BigDecimal.valueOf(20));
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() + BSSApplicationConstants.LIMIT,
				BigDecimal.valueOf(30));
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() + BSSApplicationConstants.LIMIT,
				BigDecimal.valueOf(35));
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() + BSSApplicationConstants.LIMIT,
				BigDecimal.valueOf(45));
		planTypeFundingDetails.put(PlanTypesEnum.DENTAL.getCode(), fundingDetails);

		groupFundingDetails.put(BEN_PROG_STD, planTypeFundingDetails);
		return groupFundingDetails;
	}

	private Map<String, Map<String, Map<String, BenefitPlan>>> prepareBgsADPlansMapWithoutLifePlan() {
		Map<String, Map<String, Map<String, BenefitPlan>>> map = new HashMap<>();
		Map<String, Map<String, BenefitPlan>> adMap = new HashMap<>();

		Map<String, BenefitPlan> stdPlans = new HashMap<>();
		BenefitPlan std = new BenefitPlan();
		std.setId(AD_BEN_PLAN_ID_1111);
		std.setPlanType(BSSApplicationConstants.STD_CODE);
		stdPlans.put(AD_BEN_PLAN_ID_1111, std);
		adMap.put(BSSApplicationConstants.STD_CODE, stdPlans);

		Map<String, BenefitPlan> ltdPlans = new HashMap<>();
		BenefitPlan ltd = new BenefitPlan();
		ltd.setId(AD_BEN_PLAN_ID_2222);
		ltd.setPlanType(BSSApplicationConstants.LTD_CODE);
		ltdPlans.put(AD_BEN_PLAN_ID_2222, ltd);
		adMap.put(BSSApplicationConstants.LTD_CODE, ltdPlans);

		map.put(BEN_PROG_K1, adMap);
		map.put(BEN_PROG_STD, adMap);
		return map;
	}

	private StrategyDefaultPlan prepareDefaultLifePlan() {
		StrategyDefaultPlan plan = new StrategyDefaultPlan();
		plan.setBaseBenefitPlan(LIFE_PLAN_ID);
		plan.setPlanType(BSSApplicationConstants.LIFE_CODE);
		plan.setPortfolioId(100L);
		return plan;
	}
}


