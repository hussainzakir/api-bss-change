package com.trinet.ambis.service.unit;

import static com.trinet.ambis.common.BSSApplicationConstants.DENTAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.MEDICAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.STATUS_ACTIVE;
import static com.trinet.ambis.common.BSSApplicationConstants.STRATEGY_TYPE_CUSTOM_RECOMMENDED;
import static com.trinet.ambis.common.BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED;
import static com.trinet.ambis.common.BSSApplicationConstants.VISION_PLAN_TYPE;
import static com.trinet.ambis.enums.BenExchngEnums.TRINET_III;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.service.impl.DefaultPlanMappingServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDefaultPlanDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyDefaultPlan;
import com.trinet.ambis.persistence.projections.StrategySitusDetail;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
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
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.impl.StrategyServiceImpl;
import com.trinet.ambis.service.impl.StrategySyncServiceImpl;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class StrategySyncServiceImplTest extends ServiceUnitTest {

	@Before
	public void setUpRulesAndConfigService() {
		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigService);
		Mockito.lenient().when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong())).thenReturn(new HashMap<>());
	}

	@InjectMocks
	StrategySyncServiceImpl strategySyncService;


	@Mock
	BenefitOfferExceptionService benefitOfferExceptionService;

	@Mock
	CacheService cacheService;

	@Mock
	CompanyService companyService;

	@Mock
	EmployeeSelectionDao employeeSelectionDao;

	@Mock
	PlanRatesService planRatesService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	@Mock
	RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	StrategyDao strategyDao;

	@Mock
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;
	
	@Mock
	StrategyServiceImpl strategyService;
	
	@Mock
	BenefitGroupService benefitGroupService;
	
	@Mock
	RealmPlanYearService realmPlanYearService;
	
	@Mock
	HrpDao hrpDao;
	@Mock
	StrategyDefaultPlanDao strategyDefaultPlanDao;
	@Mock
	EmployeePlanAssignmentService emplPlanAssignmentService;
	@Mock
	EePlanAssignmentDao eePlanAssignmentDao;
	@Mock
	StrategyFundingModelService strategyFundingModelService;
	@Mock
	StrategyGroupDataDao strategyGroupDataDao;
	@Mock
	PlanSelectionService planSelectionService;
	@Mock
	BenefitPlanService benefitPlanService;
	@Mock
	MinFundExceptionService minFundExceptionService; 
	@Mock
	StrategyDataDao strategyDataDao;
	@Mock
	StrategyGroupService strategyGroupService;
	@Mock
	PortfolioRuleDao portfolioRuleDao;
	@Mock
	XbssRealmPlyrPlanDao realmPlyrPlanDao;
	@Mock
	PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	@Mock
	HeadCountService headCountService;
	@Mock
	ContributionService contributionService;
	@Mock
	CompanyDao companyDao;
	@Mock
	PortfolioService portfolioService;
	@Mock
	BenefitPlanDao benefitPlanDao;
	@Mock
	GroupRuleService groupRuleService;
	@Mock
	ProspectDefaultPlanAssignmentService prospectDefaultPlanAssignmentService;
	@Mock
	EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;
	@Mock
	ProspectDefaultPlanMappingService prospectDefaultPlanMappingService;
	@Mock
    DefaultPlanMappingServiceImpl defaultPlanMappingService;

	private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

	private static final long COMPANY_ID = 123456;
	private static final String COMPANY_CODE = "a1b2c3d4";
	private static final String PF_CLIENT = "99AB";
	private static final String BENEFITS_START_DATE_STR = "01-Jan-2019";
	private static final String HQ_STATE = "CA";

	private static final long REALM_YEAR_ID = 11;
	private static final String PLAN_YEAR_START_STR = "2019-01-01";
	private static final String PLAN_YEAR_END_STR = "2019-12-31";

	private static final long STRATEGY_ID1 = 321654;
	private static final long STRATEGY_ID2 = 321655;

	@Before
	public void setup() {
		rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
	}

	@After
	public void tearDown() {
		if (rulesAndConfigsUtilsMockedStatic != null) {
			rulesAndConfigsUtilsMockedStatic.close();
		}
	}

	// Strategy update
	@Test
	public void syncStrategiesForCompanyTest() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		List<Long> strategyIdsList = List.of(STRATEGY_ID1,STRATEGY_ID2);
		Set<Long> deleteStrategyIdsSet = Set.of(STRATEGY_ID2);

		Company company =  prepareCompany() ;
		Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();
		when( planRatesService.getBenefitPlanRatesBy( Mockito.any(Company.class))).thenReturn(benefitPlanRates);
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when( strategyDao.findByCompanyId( COMPANY_ID ) ).thenReturn( prepareStrategies() );
		when(strategyGroupPlanSelectDao.getStrategiesSitus(strategyIdsList, REALM_YEAR_ID)).thenReturn(prepareStrategySitusDetails());
		when(hrpDao.getCurrentFutureBenefitPlansMap(anyLong(), any(), anyLong(), any(), anyBoolean())).thenReturn(new HashMap<>());
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(anyList()))
				.thenReturn(prepareEmployeePlanAssignments());
		rulesAndConfigsUtilsMockedStatic.when(() -> RulesAndConfigsUtils.isPlanMappingServiceEnabled(anyLong())).thenReturn(false);
		
		strategySyncService.syncStrategiesForCompany( companyCode, exchange, null );

		verify(companyService, times(1)).getCompanyDetails(companyCode, false, "EMPLID", exchange, true);
		verify( strategyDao, times(1) ).findByCompanyId( COMPANY_ID );
		verify( strategyDao, times(1) ).findByCompanyIdAndStatus( COMPANY_ID, STATUS_ACTIVE );
		verify( employeeSelectionDao, times(1) ).getRealmPlanYearBenefitPlans( any() );
		verify( planRatesService, times(2) ).getBenefitPlanRatesBy( any() );
		verify( realmPlyrPlanService, times(2) ).getMapForRealmPlanYear( REALM_YEAR_ID );
		verify( realmDataDao, times(1) ).getPortfilioDefaultPlans( REALM_YEAR_ID );
		verify( benefitOfferExceptionService, times(2) ).findApplicableBy( any() );
		verify( strategyGroupPlanSelectDao, times(1) ).getStrategiesSitus(strategyIdsList, REALM_YEAR_ID);
		verify( strategyService, times(0) ).resetStrategiesBy(companyCode, COMPANY_ID, REALM_YEAR_ID, deleteStrategyIdsSet);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(123456L);
		verify(prospectDefaultPlanMappingService, times(1)).createCensusDefaultRegionalPlanMapping(company);

		verify(companyDao, times(0)).saveAndFlush(company);
	}

	@Test
	public void syncStrategiesForCompanyTestWithPlanMapping() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		List<Long> strategyIdsList = List.of(STRATEGY_ID1,STRATEGY_ID2);
		Set<Long> deleteStrategyIdsSet = Set.of(STRATEGY_ID2);

		Company company =  prepareCompany() ;
		Map<String, List<BenefitPlanRate>> benefitPlanRates = mockPlanRates();
		when( planRatesService.getBenefitPlanRatesBy( Mockito.any(Company.class))).thenReturn(benefitPlanRates);
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when( strategyDao.findByCompanyId( COMPANY_ID ) ).thenReturn( prepareStrategies() );
		when(strategyGroupPlanSelectDao.getStrategiesSitus(strategyIdsList, REALM_YEAR_ID)).thenReturn(prepareStrategySitusDetails());
		when(hrpDao.getCurrentFutureBenefitPlansMap(anyLong(), any(), anyLong(), any(), anyBoolean())).thenReturn(new HashMap<>());
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(anyList()))
				.thenReturn(prepareEmployeePlanAssignments());
		rulesAndConfigsUtilsMockedStatic.when(() -> RulesAndConfigsUtils.isPlanMappingServiceEnabled(anyLong())).thenReturn(true);
		doNothing().when(defaultPlanMappingService).callPlanMappingService(any(Company.class));

		strategySyncService.syncStrategiesForCompany( companyCode, exchange, null );

		verify(companyService, times(1)).getCompanyDetails(companyCode, false, "EMPLID", exchange, true);
		verify( strategyDao, times(1) ).findByCompanyId( COMPANY_ID );
		verify( strategyDao, times(1) ).findByCompanyIdAndStatus( COMPANY_ID, STATUS_ACTIVE );
		verify( employeeSelectionDao, times(1) ).getRealmPlanYearBenefitPlans( any() );
		verify( planRatesService, times(2) ).getBenefitPlanRatesBy( any() );
		verify( realmPlyrPlanService, times(2) ).getMapForRealmPlanYear( REALM_YEAR_ID );
		verify( realmDataDao, times(1) ).getPortfilioDefaultPlans( REALM_YEAR_ID );
		verify( benefitOfferExceptionService, times(2) ).findApplicableBy( any() );
		verify( strategyGroupPlanSelectDao, times(1) ).getStrategiesSitus(strategyIdsList, REALM_YEAR_ID);
		verify( strategyService, times(0) ).resetStrategiesBy(companyCode, COMPANY_ID, REALM_YEAR_ID, deleteStrategyIdsSet);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(123456L);
		verify(defaultPlanMappingService, times(1)).callPlanMappingService(company);

		verify(companyDao, times(0)).saveAndFlush(company);
	}

	@Test
	public void syncStrategiesForCompanyTestForPlanYearChange() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		List<Long> strategyIdsList = List.of(STRATEGY_ID1, STRATEGY_ID2);
		Set<Long> deleteStrategyIdsSet = Set.of(STRATEGY_ID2);
		
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(Long.valueOf(1), true);
		existingMedStrategyPortfolioMap.put(Long.valueOf(2), false);

		Company company = prepareCompany();
		Company.ProcessInfo processInfo = Company.ProcessInfo.builder()
				.oldCompanyId(1234L).oldRealmPlanYear(78L)
				.processName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName())
				.build();
		company.setPlanYearChanged(true);
		RealmPlanYearRuleConfigService mockService = mock(RealmPlanYearRuleConfigService.class);
		Map<String, Set<PlanCarrier>> planCarrierMap = getPlanCarrierMap(mockService);
		List<String> missingPlans = Arrays.asList("MEDPLAN1", "DENPLAN1");
		Map<String, List<CoverageLevel>> coverageLevelsMap = prepareCoverageLevelsMap();

		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when(companyService.findByCompanyId(1234L)).thenReturn(company);
		when(strategyDao.findByCompanyId(COMPANY_ID)).thenReturn(prepareStrategies());
		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, STATUS_ACTIVE)).thenReturn(prepareStrategies());
		when(hrpDao.getCurrentFutureBenefitPlansMap(anyLong(), any(), anyLong(), any(), anyBoolean())).thenReturn(new HashMap<>());
		when(realmPlanYearService.getRealmPlanYearById(anyLong())).thenReturn(prepareRPY("Q1"));
		when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
		when(strategyGroupDataDao.getStrategyPortfolioMissingPlans(anyLong(), any(), any(), any()))
				.thenReturn(missingPlans);
		when(realmPlyrPlanService.getMapForRealmPlanYear(anyLong())).thenReturn(prepareRealmPlanMap());
		//when(companyDao.updatePlYrChangeSyncExcuted(company.getId(), 1)).thenReturn(1);
		when(benefitGroupService.getBenefitGroupByStrategy(anyLong(), eq("A"))).thenReturn(prepareBenefitGroups());
		when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
				BSSApplicationConstants.ADDITIONAL_PLAN_TYPES_INCLUD_CMTR)).thenReturn(prepareStrategyDefaultPlans());
		when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
				company.getRealmPlanYearId())).thenReturn(coverageLevelsMap);
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(anyList()))
				.thenReturn(prepareEmployeePlanAssignments());
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID1, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);
		when(portfolioRuleDao.getMedicalPortfoliosBy(STRATEGY_ID2, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(existingMedStrategyPortfolioMap);

		strategySyncService.syncStrategiesForCompany(companyCode, exchange, processInfo);

		verify(companyService, times(1)).getCompanyDetails(companyCode, false, "EMPLID", exchange, true);
		verify(strategyDao, times(2)).findByCompanyId(COMPANY_ID);
		verify(strategyDao, times(1)).findByCompanyIdAndStatus(COMPANY_ID, STATUS_ACTIVE);
		verify(employeeSelectionDao, times(1)).getRealmPlanYearBenefitPlans(any());
		verify(planRatesService, times(3)).getBenefitPlanRatesBy(any());
		verify(realmPlyrPlanService, times(3)).getMapForRealmPlanYear(REALM_YEAR_ID);
		verify(realmDataDao, times(1)).getPortfilioDefaultPlans(REALM_YEAR_ID);
		verify(benefitOfferExceptionService, times(1)).findApplicableBy(any());
		verify(strategyGroupPlanSelectDao, times(0)).getStrategiesSitus(strategyIdsList, REALM_YEAR_ID);
		verify(strategyService, times(0)).resetStrategiesBy(companyCode, COMPANY_ID, REALM_YEAR_ID,
				deleteStrategyIdsSet);
		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of("00emp2", "00emp1"), 321654L,
				existingMedStrategyPortfolioMap, Set.of(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanBy(Set.of("00emp2", "00emp1"), 321655L,
				existingMedStrategyPortfolioMap, Set.of(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(1234L);
		verify(prospectDefaultPlanMappingService, times(1)).createCensusDefaultRegionalPlanMapping(company);
		verify(companyService, times(1)).resetCompany(1234L, true);
	}

	@Test
	public void syncStrategiesForCompany_planYearChange_resetsOldCompanyOnlyOnce_whenNoActiveStrategies() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;

		Company company = prepareCompany();
		Company.ProcessInfo processInfo = Company.ProcessInfo.builder()
				.oldCompanyId(1234L).oldRealmPlanYear(78L)
				.processName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName())
				.build();

		mockPlanYearChangeFlow(companyCode, exchange, company, processInfo);
		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, STATUS_ACTIVE)).thenReturn(Collections.emptyList());

		strategySyncService.syncStrategiesForCompany(companyCode, exchange, processInfo);

		verify(companyService, times(1)).resetCompany(1234L, true);
	}

	@Test
	public void syncStrategiesForCompany_planYearChange_throwsException_whenOldAndNewCompanyIdsMatch() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;

		Company company = prepareCompany();
		Company.ProcessInfo processInfo = Company.ProcessInfo.builder()
				.oldCompanyId(COMPANY_ID).oldRealmPlanYear(78L)
				.processName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName())
				.build();

		mockPlanYearChangeFlow(companyCode, exchange, company, processInfo);
		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, STATUS_ACTIVE)).thenReturn(Collections.emptyList());

		try {
			strategySyncService.syncStrategiesForCompany(companyCode, exchange, processInfo);
			fail("Expected IllegalStateException to be thrown");
		} catch (IllegalStateException ex) {
			assertEquals("Old and new company ids should not be the same 123456", ex.getMessage());
		}

		verify(companyService, never()).resetCompany(anyLong(), anyBoolean());
	}

	@Test
	public void syncStrategiesForCompany_setsRateGroupIdToNull_whenRiskTypeIsBandsAndRateGroupIdIsNotNull() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		Company company = prepareCompany();
		company.setRiskType(RiskTypeEnum.BANDS);
		company.setRateGroupId("RGID-OLD");
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when(strategyDao.findByCompanyId(company.getId())).thenReturn(prepareStrategies());
		when(strategyDao.findByCompanyIdAndStatus(company.getId(), STATUS_ACTIVE)).thenReturn(prepareStrategies());
		when(planRatesService.getBenefitPlanRatesBy(any(Company.class))).thenReturn(mockPlanRates());
		when(realmPlyrPlanService.getMapForRealmPlanYear(anyLong())).thenReturn(prepareRealmPlanMap());
		when(employeeSelectionDao.getRealmPlanYearBenefitPlans(any())).thenReturn(Collections.emptyMap());
		when(realmDataDao.getPortfilioDefaultPlans(anyLong())).thenReturn(new HashMap<>());
		when(benefitOfferExceptionService.findApplicableBy(any())).thenReturn(Collections.emptyMap());
		strategySyncService.syncStrategiesForCompany(companyCode, exchange, null);
		verify(companyDao, times(1)).saveAndFlush(company);
		assertNull(company.getRateGroupId());
	}

	@Test
	public void syncStrategiesForCompany_doesNothing_whenRiskTypeIsBandsAndRateGroupIdIsNull() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		Company company = prepareCompany();
		company.setRiskType(RiskTypeEnum.BANDS);
		company.setRateGroupId(null);
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when(strategyDao.findByCompanyId(company.getId())).thenReturn(prepareStrategies());
		when(strategyDao.findByCompanyIdAndStatus(company.getId(), STATUS_ACTIVE)).thenReturn(prepareStrategies());
		when(planRatesService.getBenefitPlanRatesBy(any(Company.class))).thenReturn(mockPlanRates());
		when(realmPlyrPlanService.getMapForRealmPlanYear(anyLong())).thenReturn(prepareRealmPlanMap());
		when(employeeSelectionDao.getRealmPlanYearBenefitPlans(any())).thenReturn(Collections.emptyMap());
		when(realmDataDao.getPortfilioDefaultPlans(anyLong())).thenReturn(new HashMap<>());
		when(benefitOfferExceptionService.findApplicableBy(any())).thenReturn(Collections.emptyMap());
		strategySyncService.syncStrategiesForCompany(companyCode, exchange, null);
		verify(companyDao, times(0)).saveAndFlush(company);
		assertNull(company.getRateGroupId());
	}

	@Test
	public void syncStrategiesForCompany_setsNewRateGroupId_whenRiskTypeIsDifferentialsAndDifferentId() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		Company company = prepareCompany();
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setRateGroupId("OLD-RGID");
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when(strategyDao.findByCompanyId(company.getId())).thenReturn(prepareStrategies());
		when(strategyDao.findByCompanyIdAndStatus(company.getId(), STATUS_ACTIVE)).thenReturn(prepareStrategies());
		when(planRatesService.getBenefitPlanRatesBy(any(Company.class))).thenReturn(mockPlanRates());
		when(realmPlyrPlanService.getMapForRealmPlanYear(anyLong())).thenReturn(prepareRealmPlanMap());
		when(employeeSelectionDao.getRealmPlanYearBenefitPlans(any())).thenReturn(Collections.emptyMap());
		when(realmDataDao.getPortfilioDefaultPlans(anyLong())).thenReturn(new HashMap<>());
		when(benefitOfferExceptionService.findApplicableBy(any())).thenReturn(Collections.emptyMap());
		strategySyncService.syncStrategiesForCompany(companyCode, exchange, null);
		verify(companyDao, times(1)).saveAndFlush(company);
		assertEquals("RGID-1", company.getRateGroupId());
	}

	@Test
	public void syncStrategiesForCompany_doesNothing_whenRiskTypeIsDifferentialsAndSameId() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		Company company = prepareCompany();
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setRateGroupId("RGID-1");
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when(strategyDao.findByCompanyId(company.getId())).thenReturn(prepareStrategies());
		when(strategyDao.findByCompanyIdAndStatus(company.getId(), STATUS_ACTIVE)).thenReturn(prepareStrategies());
		when(planRatesService.getBenefitPlanRatesBy(any(Company.class))).thenReturn(mockPlanRates());
		when(realmPlyrPlanService.getMapForRealmPlanYear(anyLong())).thenReturn(prepareRealmPlanMap());
		when(employeeSelectionDao.getRealmPlanYearBenefitPlans(any())).thenReturn(Collections.emptyMap());
		when(realmDataDao.getPortfilioDefaultPlans(anyLong())).thenReturn(new HashMap<>());
		when(benefitOfferExceptionService.findApplicableBy(any())).thenReturn(Collections.emptyMap());
		strategySyncService.syncStrategiesForCompany(companyCode, exchange, null);
		verify(companyDao, times(0)).saveAndFlush(company);
		assertEquals("RGID-1", company.getRateGroupId());
	}

	@Test(expected = IllegalStateException.class)
	public void syncStrategiesForCompany_throwsException_whenRiskTypeIsDifferentialsAndNoRateGroupId() {
		String companyCode = "a1b2c3d4";
		BenExchngEnums exchange = TRINET_III;
		Company company = prepareCompany();
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setRateGroupId("OLD-RGID");
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when(strategyDao.findByCompanyId(company.getId())).thenReturn(prepareStrategies());
		when(planRatesService.getBenefitPlanRatesBy(any(Company.class))).thenReturn(new HashMap<>()); // No rates
		when(realmPlyrPlanService.getMapForRealmPlanYear(anyLong())).thenReturn(prepareRealmPlanMap());
		when(employeeSelectionDao.getRealmPlanYearBenefitPlans(any())).thenReturn(Collections.emptyMap());
		when(realmDataDao.getPortfilioDefaultPlans(anyLong())).thenReturn(new HashMap<>());
		when(benefitOfferExceptionService.findApplicableBy(any())).thenReturn(Collections.emptyMap());
		strategySyncService.syncStrategiesForCompany(companyCode, exchange, null);
	}

	private Map<String, Set<PlanCarrier>> getPlanCarrierMap(RealmPlanYearRuleConfigService mockService) {
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Set<PlanCarrier> planCarriers = new HashSet<>();
		planCarrierMap.put(BSSApplicationConstants.MEDICAL, planCarriers);
		planCarrierMap.put(BSSApplicationConstants.DENTAL, planCarriers);
		planCarrierMap.put(BSSApplicationConstants.VISION, planCarriers);
		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(mockService);
		return planCarrierMap;
	}

	private void mockPlanYearChangeFlow(String companyCode, BenExchngEnums exchange, Company company,
			Company.ProcessInfo processInfo) {
		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigService);
		when(companyService.getCompanyDetails(companyCode, false, "EMPLID", exchange, true)).thenReturn(company);
		when(companyService.findByCompanyId(processInfo.getOldCompanyId())).thenReturn(null);
		when(strategyDao.findByCompanyId(anyLong())).thenReturn(Collections.emptyList());
		when(strategyDao.findByCompanyIdAndStatus(COMPANY_ID, STATUS_ACTIVE)).thenReturn(Collections.emptyList());
		when(employeeSelectionDao.getRealmPlanYearBenefitPlans(any())).thenReturn(Collections.emptyMap());
		when(planRatesService.getBenefitPlanRatesBy(any(Company.class))).thenReturn(Collections.emptyMap());
		when(realmPlyrPlanService.getMapForRealmPlanYear(anyLong())).thenReturn(Collections.emptyMap());
		when(realmDataDao.getPortfilioDefaultPlans(anyLong())).thenReturn(Collections.emptyMap());
		when(benefitOfferExceptionService.findApplicableBy(any())).thenReturn(Collections.emptyMap());
		when(realmPlanYearService.getRealmPlanYearById(anyLong())).thenReturn(prepareRPY("Q1"));
		when(hrpDao.getCurrentFutureBenefitPlansMap(anyLong(), any(), anyLong(), any(), anyBoolean()))
				.thenReturn(Collections.emptyMap());
		when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(any(), any())).thenReturn(Collections.emptyList());
		when(benefitGroupService.findByCompanyId(anyLong())).thenReturn(Collections.emptyList());
	}

	private Company prepareCompany() {
		Company comp = new Company();
		comp.setId( COMPANY_ID );
		comp.setCode( COMPANY_CODE );
		comp.setRenewalCompany( false );
		comp.setPfClient( PF_CLIENT );
		comp.setPlanStartDate( BENEFITS_START_DATE_STR );
		comp.setHeadQuatersState( HQ_STATE );
		comp.setProspectCompany( true );
		comp.setTexasSitus(true);
		comp.setRealmPlanYear( prepareRealmPlanYear() );
		comp.setRealmPlanYearId( comp.getRealmPlanYear().getId() );
 
		Realm realm = new Realm();
		realm.setId( TRINET_III.getId() );
		realm.setBenExchange( TRINET_III.getBenExchng() );
		comp.setRealm( realm );

		Set<String> companyRegions = new HashSet<>();
		companyRegions.add( HQ_STATE );
		companyRegions.add("MA");
		comp.setCompanyRegions(companyRegions);

		List<String> employeeRegions = new ArrayList<>();
		employeeRegions.add( HQ_STATE );
		employeeRegions.add("MA");
		employeeRegions.add("CT");
		comp.setEmployeeRegions(employeeRegions);
		
		Set<String> fundingRegions = new HashSet<>();
		fundingRegions.add( HQ_STATE );
		fundingRegions.add("MA");
		comp.setFundingRegions(fundingRegions);
		
		comp.setMinFundings(prepareMinFundings());

		return comp;
	}

	private Map<String,String> prepareRuleConfigs() {
		Map<String,String> map = new HashMap<>();
		map.put( "VENDOR_MAPPING", "true" );
		return map;
	}

	private RealmPlanYear prepareRealmPlanYear() {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( REALM_YEAR_ID );
		rpy.setRealmId( TRINET_III.getId() );
		rpy.setPlanYearStart( java.sql.Date.valueOf( PLAN_YEAR_START_STR ) );
		rpy.setPlanYearEnd( java.sql.Date.valueOf( PLAN_YEAR_END_STR ) );
		return rpy;
	}

	private List<Strategy> prepareStrategies() {
		List<Strategy> list = new ArrayList<>();

		Strategy strategy = new Strategy();
		strategy.setId( STRATEGY_ID1 );
		strategy.setType( STRATEGY_TYPE_RECOMMENDED );
		list.add( strategy );

		strategy = new Strategy();
		strategy.setId( STRATEGY_ID2 );
		strategy.setType( STRATEGY_TYPE_CUSTOM_RECOMMENDED );
		list.add( strategy );

		return list;
	}
	
	private List<StrategySitusDetail> prepareStrategySitusDetails() {

		List<StrategySitusDetail> strategySitusDetailList = new ArrayList<>();
		strategySitusDetailList.add(new StrategySitusDetail() {
			@Override
			public long getStrategyId() {
				return STRATEGY_ID1;
			}

			@Override
			public boolean getIsTexasSitus() {
				return true;
			}

		});
		strategySitusDetailList.add(new StrategySitusDetail() {
			@Override
			public long getStrategyId() {
				return STRATEGY_ID2;
			}

			@Override
			public boolean getIsTexasSitus() {
				return false;
			}

		});

		return strategySitusDetailList;
	}
	
	private List<StrategySitusDetail> prepareStrategySitusDetails1() {

		List<StrategySitusDetail> strategySitusDetailList = new ArrayList<>();
		strategySitusDetailList.add(new StrategySitusDetail() {
			@Override
			public long getStrategyId() {
				return STRATEGY_ID1;
			}

			@Override
			public boolean getIsTexasSitus() {
				return true;
			}

		});
		strategySitusDetailList.add(new StrategySitusDetail() {
			@Override
			public long getStrategyId() {
				return STRATEGY_ID2;
			}

			@Override
			public boolean getIsTexasSitus() {
				return true;
			}

		});

		return strategySitusDetailList;
	}
	
	private RealmPlanYear prepareRPY(String quarter) {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(74);
		rpy.setRealmId(3);
		rpy.setOeQuarter(quarter);
		rpy.setPlanYearStart(java.sql.Date.valueOf("2024-10-01"));
		rpy.setPlanYearEnd(java.sql.Date.valueOf("2025-09-30"));
		rpy.setMinFunding(70);
		rpy.setMbgNew(true);
		rpy.setMbgRenewal(true);
		rpy.setAleAmount(new BigDecimal("300"));
		rpy.setAvgSalary(new BigDecimal("100000"));
		rpy.setAcaFplOpt(0);
		rpy.setMicrositeUrl("https://www.trinet.com/oe/trinet-iii-q4-bss");
		rpy.setK1Flag(true);
		rpy.setCloneProgram("098");
		return rpy;
	}
	
	private List<StrategyDefaultPlan> prepareStrategyDefaultPlans() {
		List<StrategyDefaultPlan> results = new ArrayList<>();
		StrategyDefaultPlan defaultPlan = new StrategyDefaultPlan();
		defaultPlan.setPlanType(BSSApplicationConstants.LIFE_CODE);
		results.add(defaultPlan);
		return results;
	}
	
	private List<BenefitGroup> prepareBenefitGroups() {
		List<BenefitGroup> results = new ArrayList<>();
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setId(2L);
		results.add(benefitGroup);
		return results;
	}
	
	private Map<String, XbssRealmPlyrPlan> prepareRealmPlanMap() {
		Map<String, XbssRealmPlyrPlan> rpps = new HashMap<>();

		XbssRealmPlyrPlan rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("MEDPLAN1");
		rpp.setPlanType(MEDICAL_PLAN_TYPE);
		rpps.put("MEDPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("MEDPLAN2");
		rpp.setPlanType(MEDICAL_PLAN_TYPE);
		rpps.put("MEDPLAN2", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("REGMEDPLAN1");
		rpp.setPlanType(MEDICAL_PLAN_TYPE);
		rpps.put("REGMEDPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("VISPLAN1");
		rpp.setPlanType(VISION_PLAN_TYPE);
		rpps.put("VISPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("VISPLAN2");
		rpp.setPlanType(VISION_PLAN_TYPE);
		rpps.put("VISPLAN2", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("VISPLAN3");
		rpp.setPlanType(VISION_PLAN_TYPE);
		rpps.put("VISPLAN3", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("DENPLAN1");
		rpp.setPlanType(DENTAL_PLAN_TYPE);
		rpps.put("DENPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("DENPLAN2");
		rpp.setPlanType(DENTAL_PLAN_TYPE);
		rpps.put("DENPLAN2", rpp);

		return rpps;
	}

	private Map<String, List<CoverageLevel>> prepareCoverageLevelsMap() {
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
		List<CoverageLevel> coverageLevels = new ArrayList<>();
		CoverageLevel coverageLevel1 = new CoverageLevel(CoverageCodesEnums.COV_ALL);
		CoverageLevel coverageLevel2 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE);
		CoverageLevel coverageLevel3 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD);
		CoverageLevel coverageLevel4 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY);
		coverageLevels.addAll(Arrays.asList(coverageLevel1, coverageLevel2, coverageLevel3, coverageLevel4));

		mapOfCoverageLevels.put(Constants.MEDICAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.DENTAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.VISION, coverageLevels);
		return mapOfCoverageLevels;
	}

	private Set<MinimumFunding> prepareMinFundings() {
		Set<MinimumFunding> minFundings = new HashSet<>();
		MinimumFunding minFund = new MinimumFunding("medical", "FLT", BigDecimal.valueOf(1120), true);
		minFundings.add(minFund);
		minFund = new MinimumFunding("dental", "PCT", BigDecimal.valueOf(80), false);
		minFundings.add(minFund);
		minFund = new MinimumFunding("vision", "PCT", BigDecimal.valueOf(60), false);
		minFundings.add(minFund);
		return minFundings;
	}
	
	private List<EePlanAssignment> prepareEmployeePlanAssignments() {
		List<EePlanAssignment> employeePlanAssignments = new ArrayList<>();
		EePlanAssignment eePlanAssignment = new EePlanAssignment();
		EePlanAssignmentPK eePlanAssignmentPK = new EePlanAssignmentPK();
		eePlanAssignmentPK.setBenefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		eePlanAssignmentPK.setEmplId("00emp1");
		eePlanAssignment.setEePlanAssignmentPK(eePlanAssignmentPK);
		eePlanAssignment.setBenefitPlan("PLAN_1");
		employeePlanAssignments.add(eePlanAssignment);

		eePlanAssignment = new EePlanAssignment();
		eePlanAssignmentPK = new EePlanAssignmentPK();
		eePlanAssignmentPK.setBenefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		eePlanAssignmentPK.setEmplId("00emp2");
		eePlanAssignment.setEePlanAssignmentPK(eePlanAssignmentPK);
		eePlanAssignment.setBenefitPlan("PLAN_2");
		employeePlanAssignments.add(eePlanAssignment);
		return employeePlanAssignments;
	}

	private static Map<String, List<BenefitPlanRate>> mockPlanRates() {
		Map<String, List<BenefitPlanRate>> benefitPlanRates = new HashMap<>();

		List<BenefitPlanRate> rateList = new ArrayList<>();
		String dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"1\",\"employerCost\":100,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null,\"rateGroupId\":\"RGID-1\"}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"2\",\"employerCost\":200,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null,\"rateGroupId\":\"RGID-1\"}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"C\",\"employerCost\":300,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null,\"rateGroupId\":\"RGID-1\"}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		dataStream = "{\"benefitPlan\":\"0013HJ\",\"planType\":\"10\",\"benefitProgram\":null,\"effDt\":null,\"coverageCode\":\"4\",\"employerCost\":400,\"bandCode\":\"N\",\"optionId\":null,\"costId\":null,\"rateGroupId\":\"RGID-1\"}";
		rateList.add(CommonServiceHelper.jsonToObject(dataStream, BenefitPlanRate.class));
		benefitPlanRates.put("0013HJ", rateList);
		return benefitPlanRates;
	}
}
