package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.enums.US;
import com.trinet.ambis.client.DefaultPlanMappingServiceClient;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.service.*;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.trinet.ambis.persistence.model.EmployeeStrategyGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDefaultPlanDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse.BenefitType;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse.FundingDetails;
import com.trinet.ambis.service.impl.ProspectStrategyServiceImpl;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProspectStrategyServiceImplTest extends ServiceUnitTest {

    @InjectMocks
    ProspectStrategyServiceImpl prospectStrategyService;

    @Mock
    ProspectServiceRestClient prospectServiceRestClient;

    @Mock
    RealmPlyrPlanService realmPlyrPlanService;

    @Mock
    StrategyDefaultPlanDao strategyDefaultPlanDao;

    @Mock
    ProspectDefaultPlanMappingService prospectDefaultPlanMappingService;

    @Mock
    PsCompanyDao psCompanyDao;

    @Mock
    StrategyDao strategyDao;

    @Mock
    ProspectDefaultPlanAssignmentService defaultPlanAssignmentService;

    @Mock
    BenefitGroupService benefitGroupService;

    @Mock
    RealmDataDao realmDataDao;

    @Mock
    StrategyFundingModelService strategyFundingModelService;

    @Mock
    PlanRatesService planRatesService;

    @Mock
    PortfolioService portfolioService;

    @Mock
    BenefitPlanDao benefitPlanDao;

    @Mock
    PlanSelectionService planSelectionService;

    @Mock
    EmployeeStrategyGroupDao employeeStrategyGroupDao;

    @Mock
    StrategyGroupService strategyGroupService;

    @Mock
    ProspectCensusService prospectCensusService;

    @Mock
    XbssRealmPlyrPlanDao realmPlyrPlanDao;

    @Mock
    ContributionService contributionService;

    @Mock
    CacheService cacheService;

    @Mock
    BenefitPlanService benefitPlanService;

    @Mock
    private ProspectCompanyService prospectCompanyService;

    @Mock
    CompanyService companyService;

    @Mock
    BenefitsBundleService benefitsBundleService;

    @Mock
    DefaultPlanMappingService planMappingAssignmentService;

    @Captor
    ArgumentCaptor<Strategy> strategyArgumentCaptor;

    @Captor
    ArgumentCaptor<List<BenefitGroup>> benefitGroupArgumentCaptor;

    @Captor
    ArgumentCaptor<Set<EmployeeStrategyGroup>>  employeeStrategyGroupArgumentCaptor;

    private static final String COMPANY_CODE = "PROSPECT-COMPANY";
    private static final long REALM_PLYR_ID = 11;
    private MockedStatic<BSSMessageConfig> bssMessageConfigMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;
    private MockedStatic<RenewalServiceHelper> renewalServiceHelperMockedStatic;
    private MockedStatic<CompanyServiceHelper> companyServiceHelperMockedStatic;
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;

    private BenefitGroupStrategy benefitGroupStrategy = CommonServiceHelper.jsonToObject(benefitGroupStrategyJson(),
            BenefitGroupStrategy.class);

    @Before
    public void setUp() throws Exception {
        bssMessageConfigMockedStatic = Mockito.mockStatic(BSSMessageConfig.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
        renewalServiceHelperMockedStatic = Mockito.mockStatic(RenewalServiceHelper.class);
        companyServiceHelperMockedStatic = Mockito.mockStatic(CompanyServiceHelper.class);
        appRulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(AppRulesAndConfigsUtils.class);

        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
                .thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_STRATEGY_URI))
                .thenReturn("/benefits-details/{benefitTypes}");
    }

    @After
    public void tearDown() {
        bssMessageConfigMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
        renewalServiceHelperMockedStatic.close();
        companyServiceHelperMockedStatic.close();
        appRulesAndConfigsUtilsMockedStatic.close();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getProspectCurrentStrategyTest() {
        List<BenefitsDetailsResponse> response = prepareBenefitsDetailsRes();
        ArgumentCaptor<ProspectApiRequest> apiRequestArgCaptor = ArgumentCaptor
                .forClass(ProspectApiRequest.class);
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiRequestArgCaptor.capture()))
                .thenReturn(response);
        StrategyData actualResult = prospectStrategyService.getProspectCurrentStrategy("P1");
        assertNotNull(actualResult);
        assertNotNull(actualResult.getStrategySummary());
        assertEquals(Long.valueOf(0), actualResult.getStrategySummary().getId());
        assertEquals("Prospect Current Strategy", actualResult.getStrategySummary().getName());
        assertEquals("prospect", actualResult.getStrategySummary().getType());
        assertEquals(BigDecimal.valueOf(16071.83), actualResult.getStrategySummary().getEstimatedTotalCost());
        assertEquals(9, actualResult.getStrategySummary().getHeadcount());
        assertEquals(BigDecimal.ZERO, actualResult.getStrategySummary().getTotalBudget());
        assertEquals(1, actualResult.getStrategySummary().getBudgetFactor());
        assertEquals("P1", actualResult.getStrategySummary().getCompanyId());
        assertNotNull(actualResult.getStrategyHsaFunding());
        assertEquals(0L, actualResult.getStrategyHsaFunding().getStrategyId());
        assertEquals(Integer.valueOf(0), actualResult.getStrategyHsaFunding().getOptionId());
        assertFalse(actualResult.getStrategyHsaFunding().isCustomLevel());
        List<StrategyBenefitGroup> benefitGroups = actualResult.getBenefitGroups();
        assertBenefitGroupOne(benefitGroups);
        assertBenefitGroupTwo(benefitGroups);

        verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(apiRequestArgCaptor.getValue());
        assertEquals(HttpMethod.GET, apiRequestArgCaptor.getValue().getMethod());
        assertEquals("10,11,14,23,30,31", apiRequestArgCaptor.getValue().getPathParams().get("benefitTypes"));
        assertEquals(List.of("P1"), apiRequestArgCaptor.getValue().getQueryParams().get("prospectId"));
    }

    @Test(expected = RuntimeException.class)
    public void getProspectCurrentStrategyTest1() {
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any()))
                .thenReturn(null);
        prospectStrategyService.getProspectCurrentStrategy("P1");
        verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any());
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getProspectCurrentStrategyCostsTest() {
        List<BenefitsDetailsResponse> response = prepareBenefitsDetailsRes();
        ArgumentCaptor<ProspectApiRequest> apiRequestArgCaptor = ArgumentCaptor
                .forClass(ProspectApiRequest.class);
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiRequestArgCaptor.capture()))
                .thenReturn(response);
        ModelCompareStrategyCost actualResult = prospectStrategyService.getProspectCurrentStrategyCosts("P1");
        assertNotNull(actualResult);
        assertEquals(0, actualResult.getStrategyId());
        assertEquals(2, actualResult.getBenefitGroups().size());
        assertEquals("group1", actualResult.getBenefitGroups().stream()
                .filter(a -> Objects.equals(a.getId(), 1L))
                .collect(Collectors.toList()).get(0).getName());
        assertEquals("group2", actualResult.getBenefitGroups().stream()
                .filter(a -> Objects.equals(a.getId(), 2L))
                .collect(Collectors.toList()).get(0).getName());

        assertEquals(6, actualResult.getPlanTypeCosts().size());
        assertEquals(BigDecimal.valueOf(11101.46), actualResult.getPlanTypeCosts().stream()
                .filter(a -> Objects.equals(a.getPlanType(), "medical"))
                .collect(Collectors.toList()).get(0).getCost());
        assertEquals(BigDecimal.valueOf(1495.52), actualResult.getPlanTypeCosts().stream()
                .filter(a -> Objects.equals(a.getPlanType(), "dental"))
                .collect(Collectors.toList()).get(0).getCost());
        assertEquals(BigDecimal.valueOf(3474.85), actualResult.getPlanTypeCosts().stream()
                .filter(a -> Objects.equals(a.getPlanType(), "vision"))
                .collect(Collectors.toList()).get(0).getCost());
    }

    @Test
    public void createDefaultTrinetStrategyTest() {
        //given
        long selectedCarrier = 1L;
        Company company = prepareCompany();
        company.setLargeDealProspect(1);
        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Bundle bundle = prepareBundle();

        //method mocks
        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(), BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(new ArrayList<>());
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(new ArrayList<>());
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        rulesAndConfigsUtilsMockedStatic
                .when(() -> RulesAndConfigsUtils.isPlanMappingServiceEnabled(company.getRealmPlanYear().getId()))
                .thenReturn(true);
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap()))
                .thenReturn(new HashMap<>());
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);
        when(benefitsBundleService.getBundleByCompanyCode(COMPANY_CODE)).thenReturn(bundle);
        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
        }

        doNothing().when(planMappingAssignmentService).callPlanMappingService(any(Company.class));

        // when
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        // verify
        verify(strategyDao).save(strategyArgumentCaptor.capture());

        Strategy actualResult = strategyArgumentCaptor.getValue();
        assertEquals(ProspectConstants.PROSPECT_TN_STRATEGY_NAME, actualResult.getName());
        assertEquals(company.getBundleId(), bundle.getId());
        verify(companyService, times(1)).createUpdateCompany(company);
        verify(planMappingAssignmentService, times(0)).saveDefaultPlanMappings(any(Company.class), anyList());
        verify(planMappingAssignmentService, times(1))
                .callPlanMappingService(company);
    }

    @Test
    public void createDefaultTrinetStrategy_whenBenBundleEnabled_usesBundleFlow() {
        long selectedCarrier = 1L;
        Company company = prepareCompany();
        company.setLargeDealProspect(1);
        company.setBundleId(-1L);
        company.setBundleSeq(2);
        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Bundle bundle = prepareBundle();
        DefaultPlanMappingServiceClient.PlanResponse mappedPlan =
                new DefaultPlanMappingServiceClient.PlanResponse("MED1", "EE", null);
        DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse =
                new DefaultPlanMappingServiceClient.EmployeeResponse("E1", Map.of("medical", mappedPlan));
        List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse =
                List.of(new DefaultPlanMappingServiceClient.PlanMappingResponse(List.of(employeeResponse)));

        rulesAndConfigsUtilsMockedStatic
                .when(() -> RulesAndConfigsUtils.isGaBundleEnabled(company.getRealmPlanYear().getId()))
                .thenReturn(true);
        appRulesAndConfigsUtilsMockedStatic.when(AppRulesAndConfigsUtils::getMaxBundleSeq).thenReturn(3);

        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
                BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(new ArrayList<>());
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);
        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(new ArrayList<>());
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap()))
                .thenReturn(new HashMap<>());
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);
        when(benefitsBundleService.getBundleByCompanyCode(COMPANY_CODE)).thenReturn(bundle);

        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic =
                mockStatic(BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic
                    .when(() -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(), anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
            prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, planMappingResponse);
        }

        // BEN_BUNDLE_ENABLED branch should bypass legacy non-OMS mapping calls.
        verify(planMappingAssignmentService, times(1)).saveDefaultPlanMappings(company, planMappingResponse);
        verify(planMappingAssignmentService, times(0)).callPlanMappingService(company);
        verify(prospectDefaultPlanMappingService, times(0)).createCensusDefaultRegionalPlanMapping(company);

        // Two independent if blocks execute: bundle details update + legacy large-deal bundle sync.
        verify(companyService, times(2)).createUpdateCompany(company);
        verify(benefitsBundleService, times(1)).getBundleByCompanyCode(COMPANY_CODE);
        assertEquals(3, company.getBundleSeq());
        assertEquals(bundle.getId(), company.getBundleId());
    }

      @Test
      public void createDefaultTrinetStrategy_whenPlanMappingEnabledForTibOms_doesNotUseEarlyPlanMappingService() {
        long selectedCarrier = 1L;
        Company company = prepareOmsCompany();
        company.getRealm().setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
        company.setOmsOffering("OM_TD_TV_TLD");
        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();

        rulesAndConfigsUtilsMockedStatic
                .when(() -> RulesAndConfigsUtils.isPlanMappingServiceEnabled(company.getRealmPlanYear().getId()))
                .thenReturn(true);
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isOMSExchange(company)).thenReturn(true);
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isTibProspect(company)).thenReturn(true);

        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(), BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL))
                .thenReturn(new ArrayList<>());
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);
        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(new ArrayList<>());
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap()))
                .thenReturn(new HashMap<>());
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);

        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
        }

         prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);

              verify(planMappingAssignmentService, times(0))
                  .callPlanMappingService(company);
          verify(prospectDefaultPlanMappingService, times(0)).createCensusDefaultRegionalPlanMapping(company);
    }

          @Test
          public void createDefaultTrinetStrategy_whenPlanMappingServiceWouldFailForOms_doesNotInvokeIt() {
        long selectedCarrier = 1L;
        Company company = prepareOmsCompany();
        company.getRealm().setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
        company.setOmsOffering("OM_TD_TV_TLD");
        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();

        rulesAndConfigsUtilsMockedStatic
                .when(() -> RulesAndConfigsUtils.isPlanMappingServiceEnabled(company.getRealmPlanYear().getId()))
                .thenReturn(true);
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isOMSExchange(company)).thenReturn(true);
        companyServiceHelperMockedStatic.when(() -> CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId()))
                .thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(), BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL))
                .thenReturn(new ArrayList<>());
        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(new ArrayList<>());
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap()))
                .thenReturn(new HashMap<>());
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);

            try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());

            prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        }

            verify(planMappingAssignmentService, times(0)).callPlanMappingService(company);
    }

    @Test
    public void createDefaultTrinetStrategyMACensusNonK1Test() {
        //given
        long selectedCarrier = 1L;
        Company company = prepareCompany();
        company.setLargeDealProspect(0);
        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        BenefitGroup maBenefitGroup = prepareMABenefitGroup();
        BenefitGroup K1BenefitGroup = prepareK1BenefitGroup();

        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();

        List<EmployeeStrategyGroup> employeeStrategyGroups = prepareEmployeeStrategyGroup();
        List<ProspectCensusResponse> census = List.of(
                ProspectCensusResponse.builder().employeeId("E01").employeeName("John").state("MA").gender("M").k1(false).zip("01074")
                        .salary(BigDecimal.valueOf(6000)).dob("1989-01-01").build(),
                ProspectCensusResponse.builder().employeeId("E02").employeeName("katty Scott").state("TX").gender("F").k1(false).zip("77350").build(),
                ProspectCensusResponse.builder().employeeId("E03").employeeName("Emma").state("TX").gender("F").k1(true).zip("76939").build(),
                ProspectCensusResponse.builder().employeeId("E04").employeeName("Brandon").state("MA").gender("M").k1(true).zip("01001").build()
        );


        //method mocks
        when(realmPlyrPlanService.getMapForRealmPlanYear(
                company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
                BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(
                new ArrayList<>());
        doNothing().when(prospectDefaultPlanMappingService)
                .createCensusDefaultRegionalPlanMapping(company);

        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(benefitGroupService.constructK1Group(company)).thenReturn(K1BenefitGroup);
        when(benefitGroupService.constructMAGroup(company)).thenReturn(maBenefitGroup);
        when(strategyGroupService.saveBenefitGroupStrategy(any())).thenReturn(null);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(census);
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(true);

        when(employeeStrategyGroupDao.saveAll(any())).thenReturn(employeeStrategyGroups);

        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
        }

        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(),
                anyMap()))
                .thenReturn(new HashMap<>());
        when(benefitGroupService.saveAll(anyList())).thenReturn(
                new ArrayList<>(List.of(benefitGroup, maBenefitGroup, K1BenefitGroup)));

        // when
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);

        // verify
        verify(strategyDao).save(strategyArgumentCaptor.capture());
        verify(benefitGroupService).saveAll(benefitGroupArgumentCaptor.capture());
        verify(employeeStrategyGroupDao, times(1)).saveAll(employeeStrategyGroupArgumentCaptor.capture());

        Strategy actualResult = strategyArgumentCaptor.getValue();
        assertEquals(ProspectConstants.PROSPECT_TN_STRATEGY_NAME, actualResult.getName());

        List<BenefitGroup> benefitGroupArgumentCaptorValue= benefitGroupArgumentCaptor.getValue();
        assertEquals(3,benefitGroupArgumentCaptorValue.size());

        Set<EmployeeStrategyGroup> employeeStrategyGroupArgumentCaptorValue= employeeStrategyGroupArgumentCaptor.getValue();
        assertEquals(4,employeeStrategyGroupArgumentCaptorValue.size());
        long maW2Count = employeeStrategyGroupArgumentCaptorValue.stream().filter(g -> g.getEmplId().equals("E01"))
                .count();
        long defaultW2Count = employeeStrategyGroupArgumentCaptorValue.stream().filter(g -> g.getEmplId().equals("E02"))
                .count();
        long k1Count = employeeStrategyGroupArgumentCaptorValue.stream().filter(g -> g.getEmplId().matches("E0[34]"))
                .count();

        assertEquals(1, maW2Count);
        assertEquals(1, defaultW2Count);
        assertEquals(2, k1Count);

        //bundle id value not null
        company.setBundleId(1L);
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        assertNotNull(company.getBundleId());
    }

    @Test
    public void createDefaultTrinetStrategyWithK1CompanyMACensusTest() {
        //given
        long selectedCarrier = 1L;
        Company company = prepareCompany();

        company.setK1Company(true);

        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        BenefitGroup maBenefitGroup = prepareMABenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        List<CarrierMinimumFunding> minFundings = new ArrayList<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap = new HashMap<>();

        List<ProspectCensusResponse> census = prepareProspectEmployee();

        census.get(0).setState(US.MASSACHUSETTS.getANSIabbreviation());
        census.get(0).setZip("01074");

        //method mocks
        when(realmPlyrPlanService.getMapForRealmPlanYear(
                company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
                BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(
                new ArrayList<>());
        doNothing().when(prospectDefaultPlanMappingService)
                .createCensusDefaultRegionalPlanMapping(company);
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

        when(benefitGroupService.constructK1Group(company)).thenReturn(null);
        //when(benefitGroupService.constructMAGroup(company)).thenReturn(maBenefitGroup);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(census);
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(true);
        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
        }
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(),
                anyMap()))
                .thenReturn(new HashMap<>());

        // when
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        // verify
        verify(strategyDao).save(strategyArgumentCaptor.capture());

        Strategy actualResult = strategyArgumentCaptor.getValue();
        assertEquals(ProspectConstants.PROSPECT_TN_STRATEGY_NAME, actualResult.getName());

        //bundle id value not null
        company.setBundleId(1L);
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        assertNotNull(company.getBundleId());
    }

    @Test
    public void createDefaultTrinetStrategyWithK1CompanyNonMACensusTest() {
        //given
        long selectedCarrier = 1L;
        Company company = prepareCompany();

        company.setK1Company(true);

        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        BenefitGroup maBenefitGroup = prepareMABenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        List<CarrierMinimumFunding> minFundings = new ArrayList<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap = new HashMap<>();

        List<ProspectCensusResponse> census = prepareProspectEmployee();

        //method mocks
        when(realmPlyrPlanService.getMapForRealmPlanYear(
                company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
                BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(
                new ArrayList<>());
        doNothing().when(prospectDefaultPlanMappingService)
                .createCensusDefaultRegionalPlanMapping(company);
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(benefitGroupService.constructK1Group(company)).thenReturn(null);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(census);
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(true);
        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
        }
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(),
                anyMap()))
                .thenReturn(new HashMap<>());

        // when
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        // verify
        verify(strategyDao).save(strategyArgumentCaptor.capture());

        Strategy actualResult = strategyArgumentCaptor.getValue();
        assertEquals(ProspectConstants.PROSPECT_TN_STRATEGY_NAME, actualResult.getName());

        //bundle id value not null
        company.setBundleId(1L);
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        assertNotNull(company.getBundleId());
    }

    @Test
    public void createDefaultTrinetStrategyOMSNotTibTest() {
        long selectedCarrier = 1L;
        Company company = prepareOmsCompany();
        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        List<CarrierMinimumFunding> minFundings = new ArrayList<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap = new HashMap<>();
        ProspectInfoResponse prospectInfoResponse = prepareProspectCompany();

        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(), BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(new ArrayList<>());
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(benefitGroupService.constructK1Group(company)).thenReturn(null);
        when(benefitGroupService.saveAll(anyList())).thenReturn(List.of(benefitGroup));
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(CompanyServiceHelper.isOMSExchange(company)).thenReturn(true);
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(strategyFundingModelService.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(planSelectionService.saveAll(anyList())).thenReturn(new ArrayList<>());
        when(strategyGroupService.saveBenefitGroupStrategy(any())).thenReturn(null);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(new ArrayList<>());
        when(realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(company.getHeadQuatersState(),
                BigDecimal.valueOf(company.getRealmPlanYearId()))).thenReturn(new ArrayList<>());
        doNothing().when(contributionService).saveAll(any());
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap()))
                .thenReturn(new HashMap<>());
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);
        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
        }

        // when
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        // verify
        verify(strategyDao).save(strategyArgumentCaptor.capture());

        Strategy actualResult = strategyArgumentCaptor.getValue();
        assertEquals(ProspectConstants.PROSPECT_TN_STRATEGY_NAME, actualResult.getName());
        verify(RenewalServiceHelper.class, Mockito.times(1));
        RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap());

    }

	@Test
	public void createDefaultOMSTrinetStrategyWithExpiryDate() {
		// given
		long selectedCarrier = 1L;
		Company company = prepareOmsCompany();
		Strategy strategy = prepareStrategy();
		Strategy prospectStrategy = prepareTNStrategy();
		BenefitGroup benefitGroup = prepareBenefitGroup();
		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
		Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		ProspectInfoResponse prospectInfoResponse = prepareProspectCompany();

		company.getRealm().setBenExchange("TriNet OMS");

		when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
		when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(),
				BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(new ArrayList<>());
		when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

		when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
		when(benefitGroupService.constructK1Group(company)).thenReturn(null);
		when(benefitGroupService.saveAll(anyList())).thenReturn(List.of(benefitGroup));
		when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
				company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
		when(CompanyServiceHelper.isOMSExchange(company)).thenReturn(true);
		when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);
		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
		when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
		when(strategyFundingModelService.saveAll(anyList())).thenReturn(new ArrayList<>());
		when(planSelectionService.saveAll(anyList())).thenReturn(new ArrayList<>());
		when(strategyGroupService.saveBenefitGroupStrategy(any())).thenReturn(null);
		when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(new ArrayList<>());
		when(realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(company.getHeadQuatersState(),
				BigDecimal.valueOf(company.getRealmPlanYearId()))).thenReturn(new ArrayList<>());
		doNothing().when(contributionService).saveAll(any());
		when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
		when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap()))
				.thenReturn(new HashMap<>());
		when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);
		when(prospectCompanyService.getProspectBasicDetails(any(), any())).thenReturn(prospectInfoResponse);
		doNothing().when(prospectCompanyService).updateProspectExpiryDate(anyString(), anyString(), any());

		try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
				BenefitGroupServiceHelper.class)) {
			benefitGroupServiceHelperMockedStatic.when(() -> BenefitGroupServiceHelper
					.constructStrategyBenefitGroup(any(), any(), anyString(), anyBoolean()))
					.thenReturn(constructStrategyBenefitGroup());
		}

		// when
		prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
		// verify
		verify(strategyDao).save(strategyArgumentCaptor.capture());

		Strategy actualResult = strategyArgumentCaptor.getValue();
		assertEquals(ProspectConstants.PROSPECT_TN_STRATEGY_NAME, actualResult.getName());
		RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap());
	}

    @Test(expected = BSSBadDataException.class)
    public void createDefaultTrinetStrategyTest_whenNoBundleDataFoundForLargeDealProspect() {
        //given
        long selectedCarrier = 1L;
        Company company = prepareCompany();
        company.setLargeDealProspect(1);
        Strategy strategy = prepareStrategy();
        Strategy prospectStrategy = prepareTNStrategy();
        BenefitGroup benefitGroup = prepareBenefitGroup();
        Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
        Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        //Bundle bundle = prepareBundle();

        //method mocks
        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(plyrPlanMap);
        when(strategyDefaultPlanDao.findByQuarterAndPlanTypeIn(company.getQuater(), BSSApplicationConstants.ALL_PLAN_TYPES_INCLUD_ADDITIONAL)).thenReturn(new ArrayList<>());
        when(strategyDao.save(strategy)).thenReturn(prospectStrategy);

        when(benefitGroupService.constructW2Group(company, true)).thenReturn(benefitGroup);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYear().getId())).thenReturn(mapOfCoverageLevels);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(rates);
        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(new ArrayList<>());
        when(RulesAndConfigsUtils.getMinFundingType(anyLong())).thenReturn("DEFAULT");
        when(RenewalServiceHelper.createK1Funding(anyList(), anyLong(), anyLong(), anyMap(), anyMap()))
                .thenReturn(new HashMap<>());
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);
        when(benefitsBundleService.getBundleByCompanyCode(COMPANY_CODE)).thenReturn(null);
        try (MockedStatic<BenefitGroupServiceHelper> benefitGroupServiceHelperMockedStatic = mockStatic(
                BenefitGroupServiceHelper.class)) {
            benefitGroupServiceHelperMockedStatic.when(
                            () -> BenefitGroupServiceHelper.constructStrategyBenefitGroup(any(), any(),
                                    anyString(), anyBoolean()))
                    .thenReturn(constructStrategyBenefitGroup());
        }

        // when
        prospectStrategyService.createDefaultTrinetStrategy(company, selectedCarrier, null);
        // verify
        verify(strategyDao).save(strategyArgumentCaptor.capture());

        Strategy actualResult = strategyArgumentCaptor.getValue();
        assertEquals(ProspectConstants.PROSPECT_TN_STRATEGY_NAME, actualResult.getName());
        verify(companyService, times(0)).createUpdateCompany(company);
    }

    private List<BenefitsDetailsResponse> prepareBenefitsDetailsRes() {
        return List.of(
                BenefitsDetailsResponse.builder().groupId(1).groupName("group1").groupType("STD")
                        .monthlyTotal(BigDecimal.valueOf(1437.86)).headCount(4)
                        .benefitTypes(List.of(
                                BenefitType
                                        .builder().benefitTypeCode("10").monthlyTotal(BigDecimal.valueOf(1001.34))
                                        .planCarriers(List.of("Blue Shield CA"))
                                        .fundingDetails(FundingDetails
                                                .builder().fundingType("PCT")
                                                .cvgCodeValues(Map.of("1", BigDecimal.valueOf(100.10), "2",
                                                        BigDecimal.valueOf(100.90), "C", BigDecimal.valueOf(101.09),
                                                        "4", BigDecimal.valueOf(158.10)))
                                                .build())
                                        .build(),
                                BenefitType.builder().benefitTypeCode("11").monthlyTotal(BigDecimal.valueOf(207.54))
                                        .planCarriers(List.of("Aetna", "Guardian"))
                                        .fundingDetails(FundingDetails.builder().fundingType("PCT")
                                                .cvgCodeValues(Map.of("1", BigDecimal.valueOf(210.10), "2",
                                                        BigDecimal.valueOf(200.00), "C", BigDecimal.valueOf(555.05),
                                                        "4", BigDecimal.valueOf(103.20)))
                                                .build())
                                        .build(),
                                BenefitType.builder().benefitTypeCode("14").monthlyTotal(BigDecimal.valueOf(228.98))
                                        .planCarriers(List.of("VSP", "EyeMed"))
                                        .fundingDetails(FundingDetails.builder().fundingType("PCT")
                                                .cvgCodeValues(Map.of("1", BigDecimal.valueOf(72.20), "2",
                                                        BigDecimal.valueOf(80.25), "C", BigDecimal.valueOf(103.20), "4",
                                                        BigDecimal.valueOf(110.00)))
                                                .build())
                                        .build(),
                                BenefitType.builder().benefitTypeCode("23").monthlyTotal(BigDecimal.valueOf(350.21))
                                        .planCarriers(null).fundingDetails(null).build(),
                                BenefitType.builder().benefitTypeCode("30").monthlyTotal(BigDecimal.valueOf(210.84))
                                        .planCarriers(null).fundingDetails(null).build(),
                                BenefitType.builder().benefitTypeCode("31").monthlyTotal(BigDecimal.valueOf(98.56))
                                        .planCarriers(null).fundingDetails(null).build()))
                        .build(),
                BenefitsDetailsResponse.builder().groupId(2).groupName("group2").groupType("K1")
                        .monthlyTotal(BigDecimal.valueOf(14633.97)).headCount(5)
                        .benefitTypes(List.of(
                                BenefitType.builder().benefitTypeCode("10").monthlyTotal(BigDecimal.valueOf(10100.12))
                                        .planCarriers(List.of("Blue Shield CA", "Kaiser"))
                                        .fundingDetails(FundingDetails.builder().fundingType("FLT")
                                                .cvgCodeValues(Map.of("1", BigDecimal.valueOf(1370), "2",
                                                        BigDecimal.valueOf(1380), "C", BigDecimal.valueOf(1390), "4",
                                                        BigDecimal.valueOf(1397)))
                                                .build())
                                        .build(),
                                BenefitType.builder().benefitTypeCode("11").monthlyTotal(BigDecimal.valueOf(1287.98))
                                        .planCarriers(List.of("Aetna", "Guardian"))
                                        .fundingDetails(FundingDetails.builder().fundingType("FLT")
                                                .cvgCodeValues(Map.of("1", BigDecimal.valueOf(1371), "2",
                                                        BigDecimal.valueOf(1381), "C", BigDecimal.valueOf(1391), "4",
                                                        BigDecimal.valueOf(1398)))
                                                .build())
                                        .build(),
                                BenefitType.builder().benefitTypeCode("14").monthlyTotal(BigDecimal.valueOf(3245.87))
                                        .planCarriers(List.of("VSP", "EyeMed"))
                                        .fundingDetails(FundingDetails.builder().fundingType("FLT")
                                                .cvgCodeValues(Map.of("1", BigDecimal.valueOf(1372), "2",
                                                        BigDecimal.valueOf(1382), "C", BigDecimal.valueOf(1392), "4",
                                                        BigDecimal.valueOf(1399)))
                                                .build())
                                        .build(),
                                BenefitType.builder().benefitTypeCode("23").monthlyTotal(BigDecimal.valueOf(350.21))
                                        .planCarriers(null).fundingDetails(null).build(),
                                BenefitType.builder().benefitTypeCode("30").monthlyTotal(BigDecimal.valueOf(210.84))
                                        .planCarriers(null).fundingDetails(null).build(),
                                BenefitType.builder().benefitTypeCode("31").monthlyTotal(BigDecimal.valueOf(98.56))
                                        .planCarriers(null).fundingDetails(null).build()))
                        .build());
    }

    private void assertBenefitGroupOne(List<StrategyBenefitGroup> benefitGroups) {
        assertEquals(1, benefitGroups.get(0).getId());
        assertEquals("group1", benefitGroups.get(0).getName());
        assertEquals("STD", benefitGroups.get(0).getType());
        assertEquals("NONE", benefitGroups.get(0).getWaitingPeriod());
        assertEquals("A", benefitGroups.get(0).getStatus());
        assertNull(benefitGroups.get(0).getBenefitProgram());
        assertEquals(0L, benefitGroups.get(0).getStrategyId());
        assertEquals(1, benefitGroups.get(0).getStrategyGroupId());
        assertEquals(BigDecimal.valueOf(1437.86), benefitGroups.get(0).getEstimatedTotalCost());
        assertEquals(4, benefitGroups.get(0).getHeadcount());

        List<BenefitOffer> benefitOffers = benefitGroups.get(0).getBenefitOffers();
        assertEquals("medical", benefitOffers.get(0).getSummary().getType());
        assertEquals(1, benefitOffers.get(0).getSummary().getGroupId());
        assertEquals("10", benefitOffers.get(0).getSummary().getDescription());
        assertEquals(BigDecimal.valueOf(1001.34), benefitOffers.get(0).getSummary().getEstimatedTotalCost());
        assertFalse(benefitOffers.get(0).getSummary().isBaseFundingRequired());

        for (PlanCarrier planCarrier : benefitOffers.get(0).getPlanCarriers()) {
            assertEquals(1, planCarrier.getId());
            assertEquals("Blue Shield CA", planCarrier.getName());
        }

        assertEquals(0, benefitOffers.get(0).getPlanPackage().getFundingModelId());
        assertEquals("renewal", benefitOffers.get(0).getPlanPackage().getName());
        assertTrue(benefitOffers.get(0).getPlanPackage().isCustomized());
        assertEquals(Long.valueOf(0), benefitOffers.get(0).getPlanPackage().getStrategyId());
        assertEquals("PCT", benefitOffers.get(0).getPlanPackage().getFundingType());
        assertEquals(BigDecimal.valueOf(101.09),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusChild"));
        assertEquals(BigDecimal.valueOf(100.90),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusSpouse"));
        assertEquals(BigDecimal.valueOf(100.10),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employee"));
        assertEquals(BigDecimal.valueOf(158.10),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusFamily"));

        assertEquals("dental", benefitOffers.get(1).getSummary().getType());
        assertEquals(1, benefitOffers.get(1).getSummary().getGroupId());
        assertEquals("11", benefitOffers.get(1).getSummary().getDescription());
        assertEquals(BigDecimal.valueOf(207.54), benefitOffers.get(1).getSummary().getEstimatedTotalCost());
        assertFalse(benefitOffers.get(1).getSummary().isBaseFundingRequired());
        assertEquals(0, benefitOffers.get(1).getPlanPackage().getFundingModelId());
        assertEquals("renewal", benefitOffers.get(1).getPlanPackage().getName());
        assertTrue(benefitOffers.get(1).getPlanPackage().isCustomized());
        assertEquals(Long.valueOf(0), benefitOffers.get(1).getPlanPackage().getStrategyId());
        assertEquals("PCT", benefitOffers.get(1).getPlanPackage().getFundingType());
        assertEquals(BigDecimal.valueOf(555.05),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employeePlusChild"));
        assertEquals(BigDecimal.valueOf(200.00),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employeePlusSpouse"));
        assertEquals(BigDecimal.valueOf(210.10),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employee"));
        assertEquals(BigDecimal.valueOf(103.20),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employeePlusFamily"));

        assertEquals("vision", benefitOffers.get(2).getSummary().getType());
        assertEquals(1, benefitOffers.get(2).getSummary().getGroupId());
        assertEquals("14", benefitOffers.get(2).getSummary().getDescription());
        assertEquals(BigDecimal.valueOf(228.98), benefitOffers.get(2).getSummary().getEstimatedTotalCost());
        assertFalse(benefitOffers.get(2).getSummary().isBaseFundingRequired());
        assertEquals("PCT", benefitOffers.get(2).getPlanPackage().getFundingType());
        assertEquals(0, benefitOffers.get(2).getPlanPackage().getFundingModelId());
        assertEquals("renewal", benefitOffers.get(2).getPlanPackage().getName());
        assertTrue(benefitOffers.get(2).getPlanPackage().isCustomized());
        assertEquals(Long.valueOf(0), benefitOffers.get(2).getPlanPackage().getStrategyId());
        assertEquals(BigDecimal.valueOf(103.20),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employeePlusChild"));
        assertEquals(BigDecimal.valueOf(80.25),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employeePlusSpouse"));
        assertEquals(BigDecimal.valueOf(72.20),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employee"));
        assertEquals(BigDecimal.valueOf(110.00),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employeePlusFamily"));

        assertEquals(1L, benefitOffers.get(3).getSummary().getGroupId());
        assertEquals(4, benefitOffers.get(3).getSummary().getHeadcount());

        assertEquals("additionalBenefit", benefitOffers.get(3).getSummary().getType());
        assertEquals(BigDecimal.valueOf(659.61), benefitOffers.get(3).getSummary().getEstimatedTotalCost());

        assertEquals(2, benefitOffers.get(3).getAdditionalBenefitOffers().size());
        assertEquals(1, benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getSummary().getGroupId());

        assertEquals("LIFE", benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getSummary().getType());
        assertEquals(1L, benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getSummary().getGroupId());
        assertEquals("23", benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getSummary().getDescription());
        assertEquals(4, benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getSummary().getHeadcount());
        assertEquals(BigDecimal.valueOf(350.21), benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getSummary().getEstimatedTotalCost());

        assertEquals(1, benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getAdditionalBenefitPlans().size());
        assertEquals("23", benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getAdditionalBenefitPlans().get(0).getPlanType());
        assertEquals(BigDecimal.valueOf(350.21), benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getAdditionalBenefitPlans().get(0).getPlanCost());

        assertEquals(2, benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getAdditionalBenefitPlans().size());
        assertEquals("30", benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getAdditionalBenefitPlans().get(0).getPlanType());
        assertEquals("31", benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getAdditionalBenefitPlans().get(1).getPlanType());
        assertEquals(BigDecimal.valueOf(350.21), benefitOffers.get(3).getAdditionalBenefitOffers().get(0).getAdditionalBenefitPlans().get(0).getPlanCost());

        assertEquals("Short & Long Term Disability Plan Options", benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getSummary().getDescription());
        assertEquals(BigDecimal.valueOf(309.40).setScale(2), benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getSummary().getEstimatedTotalCost());
        assertEquals(1L, benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getSummary().getGroupId());
        assertEquals(4, benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getSummary().getHeadcount());
        assertEquals("DISABILITY", benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getSummary().getType());

        assertEquals(2, benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getAdditionalBenefitPlans().size());
        assertEquals("30", benefitOffers.get(3).getAdditionalBenefitOffers().get(1).getAdditionalBenefitPlans().get(0).getPlanType());
    }

    private void assertBenefitGroupTwo(List<StrategyBenefitGroup> benefitGroups) {
        assertEquals(2, benefitGroups.get(1).getId());
        assertEquals("group2", benefitGroups.get(1).getName());
        assertEquals("K1", benefitGroups.get(1).getType());
        assertNull(benefitGroups.get(1).getBenefitProgram());
        assertEquals(0L, benefitGroups.get(1).getStrategyId());
        assertEquals(2, benefitGroups.get(1).getStrategyGroupId());
        assertEquals(BigDecimal.valueOf(14633.97), benefitGroups.get(1).getEstimatedTotalCost());
        assertEquals(5, benefitGroups.get(1).getHeadcount());

        List<BenefitOffer> benefitOffers = benefitGroups.get(1).getBenefitOffers();
        assertEquals("medical", benefitOffers.get(0).getSummary().getType());
        assertEquals(2, benefitOffers.get(0).getSummary().getGroupId());
        assertEquals("10", benefitOffers.get(0).getSummary().getDescription());
        assertEquals(BigDecimal.valueOf(10100.12), benefitOffers.get(0).getSummary().getEstimatedTotalCost());
        assertFalse(benefitOffers.get(0).getSummary().isBaseFundingRequired());
        assertEquals(0, benefitOffers.get(0).getPlanPackage().getFundingModelId());
        assertEquals("renewal", benefitOffers.get(0).getPlanPackage().getName());
        assertTrue(benefitOffers.get(0).getPlanPackage().isCustomized());
        assertEquals(Long.valueOf(0), benefitOffers.get(0).getPlanPackage().getStrategyId());
        assertEquals("FLT", benefitOffers.get(0).getPlanPackage().getFundingType());
        assertEquals(BigDecimal.valueOf(1390),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusChild"));
        assertEquals(BigDecimal.valueOf(1380),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusSpouse"));
        assertEquals(BigDecimal.valueOf(1370),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employee"));
        assertEquals(BigDecimal.valueOf(1397),
                benefitOffers.get(0).getPlanPackage().getCoverageLevelFunding().get("employeePlusFamily"));

        assertEquals("dental", benefitOffers.get(1).getSummary().getType());
        assertEquals(2, benefitOffers.get(1).getSummary().getGroupId());
        assertEquals("11", benefitOffers.get(1).getSummary().getDescription());
        assertEquals(BigDecimal.valueOf(1287.98), benefitOffers.get(1).getSummary().getEstimatedTotalCost());
        assertFalse(benefitOffers.get(1).getSummary().isBaseFundingRequired());
        assertEquals(0, benefitOffers.get(1).getPlanPackage().getFundingModelId());
        assertEquals("renewal", benefitOffers.get(1).getPlanPackage().getName());
        assertTrue(benefitOffers.get(1).getPlanPackage().isCustomized());
        assertEquals(Long.valueOf(0), benefitOffers.get(1).getPlanPackage().getStrategyId());
        assertEquals("FLT", benefitOffers.get(1).getPlanPackage().getFundingType());
        assertEquals(BigDecimal.valueOf(1391),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employeePlusChild"));
        assertEquals(BigDecimal.valueOf(1381),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employeePlusSpouse"));
        assertEquals(BigDecimal.valueOf(1371),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employee"));
        assertEquals(BigDecimal.valueOf(1398),
                benefitOffers.get(1).getPlanPackage().getCoverageLevelFunding().get("employeePlusFamily"));

        assertEquals("vision", benefitOffers.get(2).getSummary().getType());
        assertEquals(2, benefitOffers.get(2).getSummary().getGroupId());
        assertEquals("14", benefitOffers.get(2).getSummary().getDescription());
        assertEquals(BigDecimal.valueOf(3245.87), benefitOffers.get(2).getSummary().getEstimatedTotalCost());
        assertFalse(benefitOffers.get(2).getSummary().isBaseFundingRequired());
        assertEquals(0, benefitOffers.get(2).getPlanPackage().getFundingModelId());
        assertEquals("renewal", benefitOffers.get(2).getPlanPackage().getName());
        assertTrue(benefitOffers.get(2).getPlanPackage().isCustomized());
        assertEquals(Long.valueOf(0), benefitOffers.get(2).getPlanPackage().getStrategyId());
        assertEquals("FLT", benefitOffers.get(2).getPlanPackage().getFundingType());
        assertEquals(BigDecimal.valueOf(1392),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employeePlusChild"));
        assertEquals(BigDecimal.valueOf(1382),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employeePlusSpouse"));
        assertEquals(BigDecimal.valueOf(1372),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employee"));
        assertEquals(BigDecimal.valueOf(1399),
                benefitOffers.get(2).getPlanPackage().getCoverageLevelFunding().get("employeePlusFamily"));

    }

    private Company prepareCompany() {
        Company comp = new Company();
        comp.setId(2222);
        comp.setCode(COMPANY_CODE);
        comp.setRenewalCompany(false);
        comp.setPfClient("PFCLIENT");
        comp.setPlanStartDate("01-Jan-2019");
        comp.setHeadQuatersState("CA");
        comp.setProspectCompany(true);
        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setPlanYearEnd(java.sql.Date.valueOf("2018-12-31"));
        realmPlanYear.setPlanYearStart(java.sql.Date.valueOf("2019-01-01"));
        realmPlanYear.setId(REALM_PLYR_ID);
        comp.setRealmPlanYear(realmPlanYear);
        comp.setRealmPlanYearId(REALM_PLYR_ID);
        comp.setQuater("Q3");
        Set<String> companyRegions = new HashSet<>();
        companyRegions.add("MA");
        comp.setCompanyRegions(companyRegions);
        List<String> employeeRegions = new ArrayList<>();
        employeeRegions.add("CT");
        comp.setEmployeeRegions(employeeRegions);
        Realm realm = new Realm();
        realm.setBenExchange(BenExchngEnums.TRINET_III.getExchangeId());
        comp.setRealm(realm);
        comp.setProspectId(COMPANY_CODE);
        return comp;
    }

    private Company prepareOmsCompany() {
        Company comp = new Company();
        comp.setId(2222);
        comp.setCode(COMPANY_CODE);
        comp.setRenewalCompany(false);
        comp.setPfClient("PFCLIENT");
        comp.setPlanStartDate("01-Jan-2019");
        comp.setHeadQuatersState("CA");
        comp.setProspectCompany(true);
        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setPlanYearEnd(java.sql.Date.valueOf("2018-12-31"));
        realmPlanYear.setPlanYearStart(java.sql.Date.valueOf("2019-01-01"));
        realmPlanYear.setId(REALM_PLYR_ID);
        comp.setRealmPlanYear(realmPlanYear);
        comp.setRealmPlanYearId(REALM_PLYR_ID);
        comp.setQuater("Q3");
        Set<String> companyRegions = new HashSet<>();
        companyRegions.add("MA");
        comp.setCompanyRegions(companyRegions);
        List<String> employeeRegions = new ArrayList<>();
        employeeRegions.add("CT");
        comp.setEmployeeRegions(employeeRegions);
        Realm realm = new Realm();
        realm.setBenExchange(BenExchngEnums.TRINET_OMS.getExchangeId());
        comp.setRealm(realm);
        return comp;
    }

    private Strategy prepareStrategy() {
        Strategy strategy = new Strategy();
        strategy.setName(ProspectConstants.PROSPECT_TN_STRATEGY_NAME);
        strategy.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
        strategy.setComments("");
        strategy.setSubmitted(false);
        strategy.setSubmitDate(null);
        strategy.setEstimatedTotalCost(BigDecimal.ZERO);
        strategy.setCurrentYearTotalCost(BigDecimal.ZERO);
        strategy.setCompanyId(2222);
        strategy.setTotalBudget(BigDecimal.ZERO);
        strategy.setHeadCount(0L);
        strategy.setBudgetFactor(1);
        strategy.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
        strategy.setCostShareType("DFLT");
        return strategy;
    }

    private Strategy prepareTNStrategy() {
        Strategy strategy = prepareStrategy();
        strategy.setId(2222L);
        return strategy;
    }

    private BenefitGroup prepareBenefitGroup() {
        BenefitGroup benefitGroup = new BenefitGroup();
        benefitGroup.setId(1L);
        benefitGroup.setType("STD");
        benefitGroup.setName("ProspectBenGroup");
        benefitGroupStrategy.setWaitingPeriod("NONE");
        benefitGroup.setBenefitGroupStrategy(new HashSet<>(Set.of(benefitGroupStrategy)));
        return benefitGroup;
    }

    private BenefitGroup prepareMABenefitGroup() {
        BenefitGroup benefitGroup = new BenefitGroup();
        benefitGroup.setId(1L);
        benefitGroup.setType("STD");
        benefitGroup.setName("W2 MA");
        benefitGroupStrategy.setWaitingPeriod("NONE");
        benefitGroup.setBenefitGroupStrategy(new HashSet<>(Set.of(benefitGroupStrategy)));
        return benefitGroup;
    }

    private List<ProspectCensusResponse> prepareProspectEmployee() {
        return List.of(
                ProspectCensusResponse.builder().employeeName("John").state("CA").gender("M")
                        .k1(true)
                        .salary(BigDecimal.valueOf(6000)).zip("28262").dob("1989-01-01").build(),
                ProspectCensusResponse.builder().employeeName("katty Scott").state("TX").gender("F")
                        .k1(false)
                        .salary(BigDecimal.valueOf(4500)).zip("07305").dob("1973-09-28").build(),
                ProspectCensusResponse.builder().employeeName("Invalid Zip").state("TX").gender("F")
                        .k1(false)
                        .salary(BigDecimal.valueOf(5000)).zip("NOZIP").dob("2000-03-31").build());
    }

    private String benefitGroupStrategyJson() {
        return "{\"id\":2001,\"headcount\":10,\"groupId\":110001,\"strategyId\":987654,\"waitingPeriod\":\"NONE\",\"defaultGroup\":true,\"status\":\"A\",\"benefitGroup\":null,\"strategy\":null}";
    }

    public BenefitGroupStrategy constructStrategyBenefitGroup() {
        BenefitGroupStrategy bsg = new BenefitGroupStrategy();
        bsg.setGroupId(110001);
        bsg.setBenefitGroup(null);
        bsg.setStrategyId(987654);
        bsg.setStrategy(benefitGroupStrategy.getStrategy());
        bsg.setDefaultGroup(true);
        bsg.setWaitingPeriod("NONE");
        bsg.setHeadcount(0);
        bsg.setStatus(BSSApplicationConstants.STATUS_ACTIVE);
        return bsg;
    }

    @Test
    public void getProspectCurrentStrategy_EmptyInput_ReturnsNullOrDefault() {
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any()))
                .thenReturn(null);
        assertThrows(RuntimeException.class, () -> prospectStrategyService.getProspectCurrentStrategy(""));
    }

    @Test
    public void createDefaultTrinetStrategy_NullCompany_ThrowsException() {
        assertThrows(RuntimeException.class, () -> prospectStrategyService.createDefaultTrinetStrategy(null, 1L, null));
    }

    @Test
    public void createDefaultTrinetStrategy_InvalidCompanyFields_HandlesGracefully() {
        Company company = new Company(); // missing required fields
        assertThrows(Exception.class, () -> prospectStrategyService.createDefaultTrinetStrategy(company, 1L, null));
    }

    @Test
    public void getProspectCurrentStrategyCosts_EmptyInput_ReturnsNullOrDefault() {
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any()))
                .thenReturn(null);
        assertThrows(RuntimeException.class, () -> prospectStrategyService.getProspectCurrentStrategyCosts(""));
    }

    @Test
    public void getProspectCurrentStrategy_DaoThrowsException_Propagates() {
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any()))
                .thenThrow(new RuntimeException("DAO error"));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> prospectStrategyService.getProspectCurrentStrategy("P1"));
        assertEquals("DAO error", ex.getMessage());
    }

    @Test
    public void updateBundleDetailsForBenBundle_negativeBundleIdAndSeqBelowMax() throws Exception {
        Company company = prepareCompany();
        company.setBundleId(-1L);
        company.setBundleSeq(2);
        appRulesAndConfigsUtilsMockedStatic.when(AppRulesAndConfigsUtils::getMaxBundleSeq).thenReturn(3);

        Method method = ProspectStrategyServiceImpl.class
                .getDeclaredMethod("updateBundleDetailsForBenBundle", Company.class);
        method.setAccessible(true);
        method.invoke(prospectStrategyService, company);

        assertNull(company.getBundleId());
        assertEquals(3, company.getBundleSeq());
        verify(companyService, times(1)).createUpdateCompany(company);
    }

    @Test
    public void updateBundleDetailsForBenBundle_seqEqualsMaxSetsNine() throws Exception {
        Company company = prepareCompany();
        company.setBundleId(101L);
        company.setBundleSeq(3);
        appRulesAndConfigsUtilsMockedStatic.when(AppRulesAndConfigsUtils::getMaxBundleSeq).thenReturn(3);

        Method method = ProspectStrategyServiceImpl.class
                .getDeclaredMethod("updateBundleDetailsForBenBundle", Company.class);
        method.setAccessible(true);
        method.invoke(prospectStrategyService, company);

        assertEquals(Long.valueOf(101L), company.getBundleId());
        assertEquals(9, company.getBundleSeq());
        verify(companyService, times(1)).createUpdateCompany(company);
    }

    private List<EmployeeStrategyGroup> prepareEmployeeStrategyGroup() {

        EmployeeStrategyGroup esg1 = new EmployeeStrategyGroup();//w2 ma
        esg1.setEmplId("E01");
        esg1.setStrategyGroupId(8020);
        EmployeeStrategyGroup esg2 = new EmployeeStrategyGroup(); //w2
        esg2.setEmplId("E02");
        esg2.setStrategyGroupId(8021);
        EmployeeStrategyGroup esg3 = new EmployeeStrategyGroup();//k1
        esg3.setEmplId("E03");
        esg3.setStrategyGroupId(8022);
        EmployeeStrategyGroup esg4 = new EmployeeStrategyGroup();
        esg4.setEmplId("E04");
        esg4.setStrategyGroupId(8022);
        return List.of(esg1,esg2, esg3, esg4);
    }

    private BenefitGroup prepareK1BenefitGroup() {
        BenefitGroup benefitGroup = new BenefitGroup();
        benefitGroup.setId(1L);
        benefitGroup.setType("K1");
        benefitGroup.setName("K1");
        benefitGroupStrategy.setWaitingPeriod("NONE");
        benefitGroup.setBenefitGroupStrategy(new HashSet<>(Set.of(benefitGroupStrategy)));
        return benefitGroup;
    }

    private ProspectInfoResponse prepareProspectCompany() {
        ProspectInfoResponse prospectInfoResponse = new ProspectInfoResponse();
        prospectInfoResponse.setZipCode("28277");
        prospectInfoResponse.setHqState("NC");
        prospectInfoResponse.setBenStartDate("2024-04-01");
        return prospectInfoResponse;
    }

    private Bundle prepareBundle() {
        return Bundle.builder()
                .id(101L)
                .effectiveDate(LocalDate.parse("2025-12-31"))
                .endDate(LocalDate.parse("2026-12-31"))
                .name("PROSPECT_BUNDLE")
                .companyCode(COMPANY_CODE)
                .build();
    }
}