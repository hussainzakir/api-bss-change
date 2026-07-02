package com.trinet.ambis.service.unit;

import static com.trinet.ambis.enums.BenExchngEnums.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;

import com.trinet.ambis.enums.OmsOfferingEnum;
import com.trinet.ambis.enums.US;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import org.junit.After;
import org.junit.Before;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.slf4j.Logger;

import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.TibRateService;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.trinet.ambis.service.ProspectCompanyService;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyDetailsDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.EmployeeStrategyGroupService;
import com.trinet.ambis.service.ProspectDefaultPlanAssignmentService;
import com.trinet.ambis.service.ProspectDefaultPlanMappingService;
import com.trinet.ambis.service.DefaultPlanMappingService;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.ProspectStrategySyncServiceImpl;
import com.trinet.ambis.service.model.EmployeeCensusStrategyGroupDetails;
import com.trinet.ambis.service.model.ProspectStrategySyncData;
import com.trinet.ambis.service.model.ProspectStrategySyncData.EnrolledCvgCode;
import com.trinet.ambis.service.model.StrategyGroupDetails;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.model.GroupData;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProspectStrategySyncServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	ProspectStrategySyncServiceImpl prospectStrategySyncService;

	@Mock
	EmployeeBenefitGroupService employeeBenefitGroupService;

	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Mock
	EmployeeStrategyGroupService employeeStrategyGroupService;

	@Mock
	EmployeePlanAssignmentService employeePlanAssignmentService;

	@Mock
	EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;

	@Mock
	CompanyService companyService;

	@Mock
	ProspectCompanyService prospectCompanyService;

	@Mock
	PortfolioRuleDao portfolioRuleDao;

	@Mock
	StrategyService strategyService;

	@Captor
	ArgumentCaptor<String> companyCodeCaptor;

	@Captor
	ArgumentCaptor<Long> companyIdCaptor;

	@Captor
	ArgumentCaptor<Long> realmPlanYearIdCaptor;

	@Captor
	ArgumentCaptor<Set<Long>> strategyIdsCaptor;

	@Mock
	ProspectDefaultPlanMappingService prospectDefaultPlanMappingService;

	@Mock
	ProspectDefaultPlanAssignmentService prospectDefaultPlanAssignmentService;

	@Mock
	RealmPlanYearService realmPlanYearService;
	
	@Mock
	CacheService cacheService;

	@Mock
	TibRateService tibRateService;

	@Mock
	EePlanAssignmentDao eePlanAssignmentDao;

	@Mock
	PlanSelectionService planSelectionService;

    @Mock
    BenefitGroupService benefitGroupService;

    @Mock
    DefaultPlanMappingService planMappingAssignmentService;

	private static Logger logger;

    private MockedStatic<ProspectStrategySyncServiceImpl> mockStaticProspectStrategySyncServiceImpl;
    private MockedStatic<ReflectionTestUtils> mockStaticReflectionTestUtils;
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        mockStaticProspectStrategySyncServiceImpl = Mockito.mockStatic(ProspectStrategySyncServiceImpl.class);
        mockStaticReflectionTestUtils = Mockito.mockStatic(ReflectionTestUtils.class);
        logger = Mockito.mock(Logger.class);
        ReflectionTestUtils.setField(prospectStrategySyncService, "logger", logger);
        appRulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
        rulesAndConfigsUtilsMockedStatic.when(() -> RulesAndConfigsUtils.isPlanMappingServiceEnabled(anyLong()))
                .thenReturn(false);
    }

    @After
    public void tearDown() {
        if (mockStaticProspectStrategySyncServiceImpl != null) {
            mockStaticProspectStrategySyncServiceImpl.close();
        }
        if (mockStaticReflectionTestUtils != null) {
            mockStaticReflectionTestUtils.close();
        }
        appRulesAndConfigsUtilsMockedStatic.close();
        if (rulesAndConfigsUtilsMockedStatic != null) {
            rulesAndConfigsUtilsMockedStatic.close();
        }
    }

	@Test
	public void handleChangeEventTest1() {
		ArgumentCaptor<Set> emlIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Long> strategyIdArgCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Map> portfolioIdsArgCaptor = ArgumentCaptor.forClass(Map.class);
		
		// given
		// data
		Realm realm = new Realm();
		realm.setBenExchange(TRINET_II.getBenExchng());
		String companyCode = "a1b2c3";
		Company company1 = new Company();
		company1.setCode(companyCode);
		company1.setId(1234);
		company1.setRealmPlanYearId(70);
		company1.setRealm(realm);
		RealmPlanYear rpy1 = new RealmPlanYear();
		rpy1.setId(70L);
		company1.setRealmPlanYear(rpy1);
		Company company2 = new Company();
		company2.setCode(companyCode);
		company2.setId(2345);
		company2.setRealmPlanYearId(74);
		company2.setRealm(realm);
		RealmPlanYear rpy2 = new RealmPlanYear();
		rpy2.setId(74L);
		company2.setRealmPlanYear(rpy2);

		List<ProspectStrategySyncData> prospectDataRequest = prepareStrategySyncData();
		Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails = prepareEmployeeCensusStrategyGroupData();
		List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = prepareStrategyGroupDetails();
		Set<String> employeeIdsToDeleteStrategyGroup = Set.of("0000000123456", "0000000123457");
		Set<String> employeeIds = Set.of("0000000123456", "0000000123457", "0000000123458");
		Map<String, Set<StrategyGroupDetails>> strategyGroupDetailsToBeSaved = prepareEmployeeStrategyGroups();
		// method mocks
		when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(companyCode))
				.thenReturn(employeeStrategyGroupDetails);
		when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode))
				.thenReturn(strategyGroupDetails);
		doNothing().when(employeeBenefitGroupService).insertNewEmployeeStrategyGroups(strategyGroupDetailsToBeSaved);
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(employeeIds);
		doNothing().when(prospectDefaultPlanMappingService).createCensusDefaultRegionalPlanMapping(any(Company.class),
				anyList());
		when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(Arrays.asList(company1, company2));
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70)).thenReturn(company1);

		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 74)).thenReturn(company2);
		when(strategyService.getAllStrategies(company1.getId())).thenReturn(createStrategies(1111L, 2222L));
		when(strategyService.getAllStrategies(company2.getId())).thenReturn(createStrategies(3333L, 4444L));
		when(portfolioRuleDao.getMedicalPortfoliosBy(1111L, company1.getRealmPlanYearId(),
				company1.getHeadQuatersState())).thenReturn(prepareMedPortfolio(1, 2));
		when(portfolioRuleDao.getMedicalPortfoliosBy(2222L, company1.getRealmPlanYearId(),
				company1.getHeadQuatersState())).thenReturn(prepareMedPortfolio(3, 4));
		when(portfolioRuleDao.getMedicalPortfoliosBy(3333L, company2.getRealmPlanYearId(),
				company2.getHeadQuatersState())).thenReturn(prepareMedPortfolio(5, 6));
		when(portfolioRuleDao.getMedicalPortfoliosBy(4444L, company2.getRealmPlanYearId(),
				company2.getHeadQuatersState())).thenReturn(prepareMedPortfolio(7, 8));
		// when
		prospectStrategySyncService.handleCensusChangeEvent(prospectDataRequest, companyCode);
		// then
		// verify
		verify(employeeBenefitGroupDao, times(1)).getEmployeeStrategyGroupDetails(companyCode);
		verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDeleteStrategyGroup);
		verify(employeeBenefitGroupService, times(1)).insertNewEmployeeStrategyGroups(strategyGroupDetailsToBeSaved);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(employeeIds);
		verify(prospectDefaultPlanMappingService, times(2)).createCensusDefaultRegionalPlanMapping(any(Company.class), anyList());
		verify(prospectDefaultPlanAssignmentService, times(4)).assignDefaultPlanBy(
				emlIdsArgCaptor.capture(), strategyIdArgCaptor.capture(), portfolioIdsArgCaptor.capture(), anySet());
		verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
		verify(tibRateService, times(0)).saveRatesPerEmployeeIds(any(Company.class), anyLong(), anySet());
		assertTrue(emlIdsArgCaptor.getAllValues().get(0).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(1).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(2).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(3).containsAll(List.of("0000000123456")));
		
		assertEquals(1111L, strategyIdArgCaptor.getAllValues().get(0).longValue());
		assertEquals(2222L, strategyIdArgCaptor.getAllValues().get(1).longValue());
		assertEquals(3333L, strategyIdArgCaptor.getAllValues().get(2).longValue());
		assertEquals(4444L, strategyIdArgCaptor.getAllValues().get(3).longValue());

		assertTrue(portfolioIdsArgCaptor.getAllValues().get(0).keySet().containsAll(List.of(1L, 2L)));
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(1).keySet().containsAll(List.of(3L, 4L)));
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(2).keySet().containsAll(List.of(5L, 6L)));
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(3).keySet().containsAll(List.of(7L, 8L)));
        verify(cacheService, times(1)).invalidateStrategyDataCache(company1);
        verify(cacheService, times(1)).invalidateStrategyDataCache(company2);
	}

	@Test
	public void handleChangeEventTest5() {
		ArgumentCaptor<Set> emlIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Long> strategyIdArgCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Map> portfolioIdsArgCaptor = ArgumentCaptor.forClass(Map.class);
		ArgumentCaptor<List> eePlanAssignments = ArgumentCaptor.forClass(List.class);

		// given
		// data
		Realm realm = new Realm();
		realm.setBenExchange(TRINET_OMS.getBenExchng());
		String companyCode = "a1b2c3";
		Company company1 = new Company();
		company1.setCode(companyCode);
		company1.setId(1234);
		company1.setRealmPlanYearId(70);
		company1.setRealm(realm);
		company1.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
		RealmPlanYear rpy1 = new RealmPlanYear();
		rpy1.setId(70L);
		company1.setRealmPlanYear(rpy1);
		Company company2 = new Company();
		company2.setCode(companyCode);
		company2.setId(2345);
		company2.setRealmPlanYearId(74);
		company2.setRealm(realm);
		company2.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
		RealmPlanYear rpy2 = new RealmPlanYear();
		rpy2.setId(74L);
		company2.setRealmPlanYear(rpy2);

		List<ProspectStrategySyncData> prospectDataRequest = prepareStrategySyncData();
		Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails = prepareEmployeeCensusStrategyGroupData();
		List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = prepareStrategyGroupDetails();
		Set<String> employeeIdsToDeleteStrategyGroup = Set.of("0000000123456", "0000000123457");
		Set<String> employeeIds = Set.of("0000000123456", "0000000123457", "0000000123458");
		Map<String, Set<StrategyGroupDetails>> strategyGroupDetailsToBeSaved = prepareEmployeeStrategyGroups();
		// method mocks
		when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(companyCode))
				.thenReturn(employeeStrategyGroupDetails);
		when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode))
				.thenReturn(strategyGroupDetails);
		doNothing().when(employeeBenefitGroupService).insertNewEmployeeStrategyGroups(strategyGroupDetailsToBeSaved);
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(employeeIds);
		doNothing().when(prospectDefaultPlanMappingService).createCensusDefaultRegionalPlanMapping(any(Company.class),
				anyList());
		when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(Arrays.asList(company1, company2));
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70)).thenReturn(company1);

		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 74)).thenReturn(company2);
		when(strategyService.getAllStrategies(company1.getId())).thenReturn(createStrategies(1111L, 2222L));
		when(strategyService.getAllStrategies(company2.getId())).thenReturn(createStrategies(3333L, 4444L));
		when(portfolioRuleDao.getMedicalPortfoliosBy(1111L, company1.getRealmPlanYearId(),
				company1.getHeadQuatersState())).thenReturn(prepareMedPortfolio(1, 2));
		when(portfolioRuleDao.getMedicalPortfoliosBy(2222L, company1.getRealmPlanYearId(),
				company1.getHeadQuatersState())).thenReturn(prepareMedPortfolio(3, 4));
		when(portfolioRuleDao.getMedicalPortfoliosBy(3333L, company2.getRealmPlanYearId(),
				company2.getHeadQuatersState())).thenReturn(prepareMedPortfolio(5, 6));
		when(portfolioRuleDao.getMedicalPortfoliosBy(4444L, company2.getRealmPlanYearId(),
				company2.getHeadQuatersState())).thenReturn(prepareMedPortfolio(7, 8));
		when(employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(1111L))).thenReturn(prepareEmployeePlanAssignments());
		when(eePlanAssignmentDao.saveAll(anyList())).thenReturn(Collections.emptyList());
		// when
		prospectStrategySyncService.handleCensusChangeEvent(prospectDataRequest, companyCode);
		// then
		// verify
		verify(employeeBenefitGroupDao, times(1)).getEmployeeStrategyGroupDetails(companyCode);
		verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDeleteStrategyGroup);
		verify(employeeBenefitGroupService, times(1)).insertNewEmployeeStrategyGroups(strategyGroupDetailsToBeSaved);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(employeeIds);
		verify(prospectDefaultPlanMappingService, times(2)).createCensusDefaultRegionalPlanMapping(any(Company.class), anyList());
		verify(prospectDefaultPlanAssignmentService, times(4)).assignDefaultPlanBy(
				emlIdsArgCaptor.capture(), strategyIdArgCaptor.capture(), portfolioIdsArgCaptor.capture(), anySet());
		verify(eePlanAssignmentDao, times(1)).saveAll(eePlanAssignments.capture());
        verify(eePlanAssignmentDao, times(1)).deleteAll(eePlanAssignments.capture());
		verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
		verify(tibRateService, times(4)).saveRatesPerEmployeeIds(any(Company.class), anyLong(), anySet());
		assertTrue(emlIdsArgCaptor.getAllValues().get(0).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(1).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(2).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(3).containsAll(List.of("0000000123456")));

		assertEquals(1, eePlanAssignments.getAllValues().get(0).size());
		assertEquals(1, eePlanAssignments.getAllValues().get(1).size());
	}

    @Test
    public void rateSyncOnCensusDependentChangeTest() {
        // given
        String companyCode = "a1b2c3";
        Realm realm = new Realm();
        realm.setBenExchange(TRINET_OMS.getBenExchng());

        Company company1 = new Company();
        company1.setId(1234L);
        company1.setRealmPlanYearId(70L);
        company1.setCode(companyCode);
        company1.setRealm(realm);
        company1.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());

        Company company2 = new Company();
        company2.setId(2345L);
        company2.setRealmPlanYearId(74L);
        company2.setCode(companyCode);
        company2.setRealm(realm);
        company2.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());

        List<Company> bssCompanies = List.of(company1, company2);

        Strategy strategy1 = new Strategy();
        strategy1.setId(1111L);

        Strategy strategy2 = new Strategy();
        strategy2.setId(2222L);

        List<Strategy> strategies = List.of(strategy1, strategy2);

        // method mocks
        when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(bssCompanies);
        when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70L)).thenReturn(company1);
        when(prospectCompanyService.getProspectCompanyDetails(companyCode, 74L)).thenReturn(company2);
        when(strategyService.getAllStrategies(1234L)).thenReturn(strategies);
        when(strategyService.getAllStrategies(2345L)).thenReturn(strategies);
        doNothing().when(tibRateService).saveRatesPerEmployee(any(Company.class), anyLong(), anyList());

        // when
        prospectStrategySyncService.rateSyncOnCensusDependentChange(prepareProspectCensus(), companyCode);

        // then
        verify(companyService, times(1)).getXbssCompaniesByCode(companyCode);
        verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 70L);
        verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 74L);
        verify(strategyService, times(1)).getAllStrategies(1234L);
        verify(strategyService, times(1)).getAllStrategies(2345L);
        verify(tibRateService, times(4)).saveRatesPerEmployee(any(Company.class), anyLong(), anyList());
    }

    @Test
    public void rateSyncOnCensusDependentChange_NotTibProspect1() {
        // given
        String companyCode = "testCompanyCode";
        List<String> employeeIds = List.of("0000000123456", "0000000123457");
        Set<String> employeeIdSet = new HashSet<>(employeeIds);

        Realm realm = new Realm();
        realm.setBenExchange(TRINET_III.getBenExchng());


        Company company1 = new Company();
        company1.setId(1234L);
        company1.setRealmPlanYearId(70L);
        company1.setCode(companyCode);
        company1.setRealm(realm);
        company1.setAuthBroker("NOT_TIB");

        Company company2 = new Company();
        company2.setId(2345L);
        company2.setRealmPlanYearId(74L);
        company2.setCode(companyCode);
        company2.setRealm(realm);
        company2.setAuthBroker("NOT_TIB");

        List<Company> bssCompanies = List.of(company1, company2);

        Strategy strategy1 = new Strategy();
        strategy1.setId(1111L);

        Strategy strategy2 = new Strategy();
        strategy2.setId(2222L);

        List<Strategy> strategies = List.of(strategy1, strategy2);

        // method mocks
        when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(bssCompanies);
        when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70L)).thenReturn(company1);
        when(prospectCompanyService.getProspectCompanyDetails(companyCode, 74L)).thenReturn(company2);

        // when
        prospectStrategySyncService.rateSyncOnCensusDependentChange(prepareProspectCensus(), companyCode);

        // then
        verify(companyService, times(1)).getXbssCompaniesByCode(companyCode);
        verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 70L);
        verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 74L);
    }

	private Map<Long, Boolean> prepareMedPortfolio(long portfolioId1, long portfolioId2) {
		Map<Long, Boolean> portfolio = new HashMap<>();
		portfolio.put(portfolioId1, Boolean.TRUE);
		portfolio.put(portfolioId2, Boolean.FALSE);
		return portfolio;
	}

	private RealmPlanYear prepareRealmPlanYear(long rpyId, long realmId) {
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(rpyId);
		rpy.setRealmId(realmId);
		return rpy;
	}

	private List<Strategy> createStrategies(long strategyId1, long strategyId2) {
		Strategy s1 = new Strategy();
		s1.setId(strategyId1);
		Strategy s2 = new Strategy();
		s2.setId(strategyId2);
		return Arrays.asList(s1, s2);
	}

	@Test(expected = RuntimeException.class)
	public void handleChangeEventTest2() {
		// given
		// data
		String companyCode = "a1b2c3";
		List<ProspectStrategySyncData> strategySyncData = prepareStrategySyncData();
		// method mocks
		doThrow(new RuntimeException("Exception occurred while fetching the employee strategy group details"))
				.when(employeeBenefitGroupDao).getEmployeeStrategyGroupDetails(companyCode);
		// when
		prospectStrategySyncService.handleCensusChangeEvent(strategySyncData, companyCode);
		// then
		// verify
		verify(employeeBenefitGroupDao, times(1)).getEmployeeStrategyGroupDetails(companyCode);
		verify(employeeBenefitGroupDao, times(0)).deleteEmployeeStrategyGroups(anySet());
		verify(employeeBenefitGroupService, times(0)).insertNewEmployeeStrategyGroups(any());
	}

	@Test(expected = RuntimeException.class)
	public void handleChangeEventTest3() {
		// given
		// data
		String companyCode = "a1b2c3";
		List<ProspectStrategySyncData> strategySyncData = prepareStrategySyncData();
		Set<String> employeeIds = Set.of("0000000123456", "0000000123457");
		Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeCensusStrategyGroup = prepareEmployeeCensusStrategyGroupData();
		List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = prepareStrategyGroupDetails();
		// method mocks
		when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(companyCode))
				.thenReturn(employeeCensusStrategyGroup);
		when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode)).thenReturn(strategyGroupDetails);
		doThrow(new RuntimeException("Exception occurred while deleting the employee strategy group details"))
				.when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(employeeIds);
		// when
		prospectStrategySyncService.handleCensusChangeEvent(strategySyncData, companyCode);
		// then
		// verify
		verify(employeeBenefitGroupDao, times(1)).getEmployeeStrategyGroupDetails(companyCode);
		verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(employeeIds);
		verify(employeeBenefitGroupService, times(0)).insertNewEmployeeStrategyGroups(any());
	}

	@Test(expected = RuntimeException.class)
	public void handleChangeEventTest4() {
		// given
		// data
		String companyCode = "a1b2c3";
		List<ProspectStrategySyncData> strategySyncData = prepareStrategySyncData();
		Set<String> employeeIds = Set.of("0000000123456", "0000000123457");
		Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeCensusStrategyGroup = prepareEmployeeCensusStrategyGroupData();
		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroups = prepareEmployeeStrategyGroups();
		List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = prepareStrategyGroupDetails();
		// method mocks
		when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(companyCode))
				.thenReturn(employeeCensusStrategyGroup);
		when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode)).thenReturn(strategyGroupDetails);
		doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(employeeIds);
		doThrow(new RuntimeException("Exception occurred while saving the employee strategy group details"))
				.when(employeeBenefitGroupService).insertNewEmployeeStrategyGroups(employeeStrategyGroups);
		// when
		prospectStrategySyncService.handleCensusChangeEvent(strategySyncData, companyCode);
		// then
		// verify
		verify(employeeBenefitGroupDao, times(1)).getEmployeeStrategyGroupDetails(companyCode);
		verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(employeeIds);
		verify(employeeBenefitGroupService, times(1)).insertNewEmployeeStrategyGroups(employeeStrategyGroups);
	}

	@Test
	public void syncProspectStrategyDataTest1() {
		// given
		// data
		String companyCode = "a1b2c3";
		List<String> employeeIdsToDelete = prepareEmployeeIdsToDelete();
		doNothing().when(employeeStrategyGroupService).deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignment(employeeIdsToDelete, companyCode);
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignment(employeeIdsToDelete,
				companyCode);

		// when
		prospectStrategySyncService.handleCensusDeleteEvent(employeeIdsToDelete, companyCode);
		// then
		// verify
		verify(employeeStrategyGroupService, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignment(employeeIdsToDelete, companyCode);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignment(employeeIdsToDelete,
				companyCode);
		verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
		verify(planSelectionService, times(0)).syncOmsMedicalPlanSelections(anyLong());
	}

	@Test(expected = RuntimeException.class)
	public void syncProspectStrategyDataTest2() {
		// given
		// data
		String companyCode = "a1b2c3";
		List<String> employeeIdsToDelete = prepareEmployeeIdsToDelete();
		doThrow(new RuntimeException("Exception occurred while deleting")).when(employeeStrategyGroupService)
				.deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
//		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignment(employeeIdsToDelete, companyCode);
//		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignment(employeeIdsToDelete,
//				companyCode);

		// when
		prospectStrategySyncService.handleCensusDeleteEvent(employeeIdsToDelete, companyCode);
		// then
		// verify
		verify(employeeStrategyGroupService, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
		verify(employeePlanAssignmentService, times(0)).deleteEmployeePlanAssignment(anyList(), any());
		verify(emplDefaultPlanAssignmentService, times(0)).deleteEmplDefaultPlanAssignment(anyList(), any());
	}

	@Test
	public void syncProspectStrategyDataTest3() {
		// given
		// data
		String companyCode = "a1b2c3";
		Realm realm = new Realm();
		realm.setBenExchange(TRINET_OMS.getBenExchng());
		Company company1 = new Company();
		company1.setCode(companyCode);
		company1.setId(1234);
		company1.setRealmPlanYearId(70);
		company1.setRealm(realm);
		company1.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
		List<String> employeeIdsToDelete = prepareEmployeeIdsToDelete();
		doNothing().when(employeeStrategyGroupService).deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignment(employeeIdsToDelete, companyCode);
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignment(employeeIdsToDelete,
				companyCode);
		doNothing().when(strategyService).createOmsStrategyEstimate(any(), any());
		when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(List.of(company1));
		when(strategyService.getAllStrategies(company1.getId())).thenReturn(createStrategies(1111L, 2222L));
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70)).thenReturn(company1);

		// when
		prospectStrategySyncService.handleCensusDeleteEvent(employeeIdsToDelete, companyCode);
		// then
		// verify
		verify(employeeStrategyGroupService, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignment(employeeIdsToDelete, companyCode);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignment(employeeIdsToDelete,
				companyCode);
		verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
		verify(planSelectionService, times(2)).syncOmsMedicalPlanSelections(anyLong());
		verify(strategyService, times(1)).createOmsStrategyEstimate(company1, Set.of(1111L, 2222L));
        verify(cacheService, times(0)).invalidateStrategyDataCache(company1);

	}

	@Test
	public void syncProspectStrategyDataTest4() {
		String companyCode = "2TMF";
        Company company = new Company();
        company.setCode(companyCode);
		List<String> employeeIdsToDelete = List.of("0000000123456");
		doNothing().when(employeeStrategyGroupService).deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignment(employeeIdsToDelete, companyCode);
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignment(employeeIdsToDelete,
				companyCode);
        when(companyService.getCompanyDetails(companyCode)).thenReturn(company);

		prospectStrategySyncService.handleCensusDeleteEvent(employeeIdsToDelete, companyCode);
	
		verify(employeeStrategyGroupService, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDelete, companyCode);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignment(employeeIdsToDelete, companyCode);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignment(employeeIdsToDelete,
				companyCode);
		verify(cacheService, times(0)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
		verify(planSelectionService, times(0)).syncOmsMedicalPlanSelections(anyLong());
        verify(cacheService, times(1)).invalidateStrategyDataCache(company);
	}
	
	@Test
	public void syncProspectStrategyDataForAddEvent() {
		ArgumentCaptor<Set> emlIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Long> strategyIdArgCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Map> portfolioIdsArgCaptor = ArgumentCaptor.forClass(Map.class);
		
		// given
		// data
		Realm realm = new Realm();
		realm.setBenExchange(TRINET_II.getBenExchng());
		String companyCode = "a1b2c3";
		Company company1 = new Company();
		company1.setCode(companyCode);
		company1.setId(1234);
		company1.setRealmPlanYearId(70);
		company1.setRealm(realm);
		RealmPlanYear rpy1 = new RealmPlanYear();
		rpy1.setId(70L);
		company1.setRealmPlanYear(rpy1);
		Company company2 = new Company();
		company2.setCode(companyCode);
		company2.setId(2345);
		company2.setRealmPlanYearId(74);
		company2.setRealm(realm);
		RealmPlanYear rpy2 = new RealmPlanYear();
		rpy2.setId(74L);
		company2.setRealmPlanYear(rpy2);
		Set<String> employeeIds = Set.of("0000000123456", "0000000123457", "0000000123458");
	
		Company company3 = new Company();
		company3.setCode(companyCode);
		company3.setId(23456);
		company3.setRealmPlanYearId(80);
		company3.setRealm(realm);
		RealmPlanYear rpy3 = new RealmPlanYear();
		rpy3.setId(80L);
		company3.setRealmPlanYear(rpy3);

		List<ProspectStrategySyncData> strategySyncData = prepareStrategySyncData();
		List<EmployeeCensusStrategyGroupDetails> existingStrategyGroupsForCompanyCodeList = prepareStartegyGroupByCompanyAndStrategy();
		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetailsToSave = prepareEmployeeStrategyGroups1();
		// method mocks
		when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode))
				.thenReturn(existingStrategyGroupsForCompanyCodeList);
		doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(employeeIds);
		doNothing().when(employeeBenefitGroupService).insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(employeeIds);
		doNothing().when(prospectDefaultPlanMappingService).createCensusDefaultRegionalPlanMapping(any(Company.class),
				anyList());
		when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(Arrays.asList(company1, company2, company3));
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70)).thenReturn(company1);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 74)).thenReturn(company2);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 80)).thenReturn(company3);
		when(strategyService.getAllStrategies(company1.getId())).thenReturn(createStrategies(1111L, 2222L));
		when(strategyService.getAllStrategies(company2.getId())).thenReturn(createStrategies(3333L, 4444L));
		when(strategyService.getAllStrategies(company3.getId())).thenReturn(List.of());
		when(portfolioRuleDao.getMedicalPortfoliosBy(1111L, company1.getRealmPlanYearId(),
				company1.getHeadQuatersState())).thenReturn(prepareMedPortfolio(1, 2));
		when(portfolioRuleDao.getMedicalPortfoliosBy(2222L, company1.getRealmPlanYearId(),
				company1.getHeadQuatersState())).thenReturn(prepareMedPortfolio(3, 4));
		when(portfolioRuleDao.getMedicalPortfoliosBy(3333L, company2.getRealmPlanYearId(),
				company2.getHeadQuatersState())).thenReturn(prepareMedPortfolio(5, 6));
		when(portfolioRuleDao.getMedicalPortfoliosBy(4444L, company2.getRealmPlanYearId(),
				company2.getHeadQuatersState())).thenReturn(prepareMedPortfolio(7, 8));
		// when
		prospectStrategySyncService.handleCensusAddEvent(strategySyncData, companyCode);
		// then
		// verify
		verify(employeeBenefitGroupDao, times(1)).getStartegyGroupByCompanyAndStrategy(companyCode);
		verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(Set.of("0000000123456", "0000000123457", "0000000123458"));
		verify(employeeBenefitGroupService, times(1)).insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(employeeIds);
		verify(prospectDefaultPlanMappingService, times(2)).createCensusDefaultRegionalPlanMapping(any(Company.class), anyList());
		verify(prospectDefaultPlanAssignmentService, times(4)).assignDefaultPlanBy(
				emlIdsArgCaptor.capture(), strategyIdArgCaptor.capture(), portfolioIdsArgCaptor.capture(), anySet());
		verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
		assertTrue(emlIdsArgCaptor.getAllValues().get(0).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(1).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(2).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(3).containsAll(List.of("0000000123456")));
		
		assertEquals(1111L, strategyIdArgCaptor.getAllValues().get(0).longValue());
		assertEquals(2222L, strategyIdArgCaptor.getAllValues().get(1).longValue());
		assertEquals(3333L, strategyIdArgCaptor.getAllValues().get(2).longValue());
		assertEquals(4444L, strategyIdArgCaptor.getAllValues().get(3).longValue());

		assertTrue(portfolioIdsArgCaptor.getAllValues().get(0).keySet().containsAll(List.of(1L, 2L)));
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(1).keySet().containsAll(List.of(3L, 4L)));
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(2).keySet().containsAll(List.of(5L, 6L)));
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(3).keySet().containsAll(List.of(7L, 8L)));
	}

	@Test(expected = RuntimeException.class)
	public void syncProspectStrategyDataForAddEvent2() {
		// given
		// data
		String companyCode = "a1b2c3";
		List<ProspectStrategySyncData> strategySyncData = prepareStrategySyncData();
		// method mocks
		doThrow(new RuntimeException("Exception occurred while fetching the employee strategy group details"))
				.when(employeeBenefitGroupDao).getStartegyGroupByCompanyAndStrategy(companyCode);
		// when
		prospectStrategySyncService.handleCensusAddEvent(strategySyncData, companyCode);
		// then
		// verify
		verify(employeeBenefitGroupDao, times(1)).getStartegyGroupByCompanyAndStrategy(companyCode);
		verify(employeeBenefitGroupService, times(0)).insertNewEmployeeStrategyGroups(any());
	}

	@Test
	public void handleCensusAddEventTest3() {
		ArgumentCaptor<Set> emlIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Long> strategyIdArgCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Map> portfolioIdsArgCaptor = ArgumentCaptor.forClass(Map.class);
		String companyCode = "2TMF";
		Company company = prepareClientCompany();
		Realm realm = new Realm();
		realm.setBenExchange(TRINET_III.getBenExchng());
		company.setRealm(realm);
		Set<String> employeeIds = Set.of("0000000123456");
		List<ProspectStrategySyncData> strategySyncData = List.of(ProspectStrategySyncData.builder()
				.employeeId("0000000123456").k1(true).homeState("TX").homePostalCode("73301")
				.enrolledCvgCodes(List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
						EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
						EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
				.build());
		List<EmployeeCensusStrategyGroupDetails> existingStrategyGroupsForCompanyCodeList = prepareStartegyGroupByCompanyAndStrategy();
		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetailsToSave = prepareClientEmployeeStrategyGroups();

		when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode))
				.thenReturn(existingStrategyGroupsForCompanyCodeList);
		doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(employeeIds);
		doNothing().when(employeeBenefitGroupService)
				.insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(employeeIds);
		doNothing().when(prospectDefaultPlanMappingService).createCensusDefaultRegionalPlanMapping(any(Company.class),
				anyList());
		when(companyService.getCompanyDetails(companyCode)).thenReturn(company);
		when(strategyService.getAllStrategies(company.getId())).thenReturn(createStrategies(1111L, 2222L));
		when(portfolioRuleDao.getMedicalPortfoliosBy(1111L, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(prepareMedPortfolio(1, 2));
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(false);
		when(portfolioRuleDao.getMedicalPortfoliosBy(2222L, company.getRealmPlanYearId(),
				company.getHeadQuatersState())).thenReturn(prepareMedPortfolio(3, 4));

		prospectStrategySyncService.handleCensusAddEvent(strategySyncData, companyCode);

		verify(employeeBenefitGroupDao, times(1)).getStartegyGroupByCompanyAndStrategy(companyCode);
		verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(Set.of("0000000123456"));
		verify(employeeBenefitGroupService, times(1))
				.insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(employeeIds);
		verify(prospectDefaultPlanMappingService, times(1)).createCensusDefaultRegionalPlanMapping(any(Company.class),
				anyList());
		verify(prospectDefaultPlanAssignmentService, times(0)).assignDefaultPlanBy(anySet(), anyLong(), anyMap(),
				anySet());
		verify(prospectDefaultPlanAssignmentService, times(2)).assignDefaultPlanForMissingEmployees(
				emlIdsArgCaptor.capture(), strategyIdArgCaptor.capture(), anyLong(), portfolioIdsArgCaptor.capture(),
				anyBoolean());
		verify(employeePlanAssignmentService, times(1)).updateEePlanAssignmentCvgCode(emlIdsArgCaptor.capture());
		verify(tibRateService, times(0)).saveRatesPerEmployeeIds(any(), anyLong(), anySet());
		verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
		verify(prospectCompanyService, times(0)).getProspectCompanyDetails(anyString(), anyLong());
		verify(companyService, times(0)).getXbssCompaniesByCode(anyString());
		assertTrue(emlIdsArgCaptor.getAllValues().get(0).containsAll(List.of("0000000123456")));
		assertTrue(emlIdsArgCaptor.getAllValues().get(1).containsAll(List.of("0000000123456")));
		assertEquals(1111L, strategyIdArgCaptor.getAllValues().get(0).longValue());
		assertEquals(2222L, strategyIdArgCaptor.getAllValues().get(1).longValue());
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(0).keySet().containsAll(List.of(1L, 2L)));
		assertTrue(portfolioIdsArgCaptor.getAllValues().get(1).keySet().containsAll(List.of(3L, 4L)));
        verify(benefitGroupService, times(0)).addGroup(any(), any(GroupData.class), anyLong());

	}

    @Test
    public void handleCensusAddEventTest4() {
        ArgumentCaptor<Set> emlIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<Long> strategyIdArgCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map> portfolioIdsArgCaptor = ArgumentCaptor.forClass(Map.class);
        Realm realm = new Realm();
        realm.setBenExchange(TRINET_II.getBenExchng());
        String companyCode = "ProspectTest";
        Company company = new Company();
        company.setCode(companyCode);
        company.setId(1234);
        company.setRealmPlanYearId(70);
        company.setRealm(realm);
        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setId(70L);
        company.setRealmPlanYear(realmPlanYear);
        Set<String> employeeIds = Set.of("000-E3", "000-E2", "000-E1");
        List<ProspectStrategySyncData> prospectStrategySyncData = createProspectStrategySyncDataForMA();
        List<Strategy> strategy = createStrategyForMA();
        Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetailsToSave = prepareClientEmployeeStrategyGroupsForMA();
        List<EmployeeCensusStrategyGroupDetails> existingStrategyGroupsForCompanyCodeList = prepareStartegyGroupByCompanyAndStrategy();
        List<EmployeeCensusStrategyGroupDetails> refreshedStrategyGroups = List.of(
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93140).groupType("STD")
                        .groupDesc(BSSApplicationConstants.CLIENT_MA_GROUP_NAME).build(),
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93141).groupType("K1")
                        .build(),
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93142).groupType("STD")
                        .build());

        when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode))
                .thenReturn(existingStrategyGroupsForCompanyCodeList).thenReturn(refreshedStrategyGroups);
        when(benefitGroupService.addGroup(any(), any(GroupData.class), anyLong())).thenReturn(123L);
        when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(Arrays.asList(company));
        when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70)).thenReturn(company);
        doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(employeeIds);
        doNothing().when(employeeBenefitGroupService)
                .insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
        doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(employeeIds);
        doNothing().when(prospectDefaultPlanMappingService).createCensusDefaultRegionalPlanMapping(any(Company.class),
                anyList());
        when(strategyService.getAllStrategies(company.getId())).thenReturn(strategy);
        when(portfolioRuleDao.getMedicalPortfoliosBy(78981L, company.getRealmPlanYearId(),
                company.getHeadQuatersState())).thenReturn(prepareMedPortfolio(1, 2));
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(true);

        prospectStrategySyncService.handleCensusAddEvent(prospectStrategySyncData, companyCode);

        verify(employeeBenefitGroupDao, times(2)).getStartegyGroupByCompanyAndStrategy(companyCode);
        verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(Set.of("000-E3", "000-E2", "000-E1"));
        verify(employeeBenefitGroupService, times(1))
                .insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
        verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(employeeIds);
        verify(prospectDefaultPlanMappingService, times(1)).createCensusDefaultRegionalPlanMapping(any(Company.class),
                anyList());
        verify(prospectDefaultPlanAssignmentService, times(0)).assignDefaultPlanBy(anySet(), anyLong(), anyMap(),
                anySet());
        verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanForMissingEmployees(
                emlIdsArgCaptor.capture(), strategyIdArgCaptor.capture(), anyLong(), portfolioIdsArgCaptor.capture(),
                anyBoolean());
        verify(employeePlanAssignmentService, times(1)).updateEePlanAssignmentCvgCode(emlIdsArgCaptor.capture());
        verify(tibRateService, times(0)).saveRatesPerEmployeeIds(any(), anyLong(), anySet());
        verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
        verify(prospectCompanyService, times(1)).getProspectCompanyDetails(anyString(), anyLong());
        verify(companyService, times(1)).getXbssCompaniesByCode(anyString());
        verify(benefitGroupService, times(1)).addGroup(any(), any(GroupData.class), anyLong());
        assertTrue(emlIdsArgCaptor.getAllValues().get(0).containsAll(List.of("000-E3", "000-E2", "000-E1")));
        assertTrue(emlIdsArgCaptor.getAllValues().get(1).containsAll(List.of("000-E3", "000-E2", "000-E1")));
        assertEquals(78981L, strategyIdArgCaptor.getAllValues().get(0).longValue());
        assertTrue(portfolioIdsArgCaptor.getAllValues().get(0).keySet().containsAll(List.of(1L, 2L)));

    }

    @Test
    public void handleCensusAddEventTest5() {
        ArgumentCaptor<Set> emlIdsArgCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<Long> strategyIdArgCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map> portfolioIdsArgCaptor = ArgumentCaptor.forClass(Map.class);
        Realm realm = new Realm();
        realm.setBenExchange(TRINET_II.getBenExchng());
        String companyCode = "ProspectTest";
        Company company = new Company();
        company.setCode(companyCode);
        company.setId(1234);
        company.setRealmPlanYearId(70);
        company.setRealm(realm);
        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setId(70L);
        company.setRealmPlanYear(realmPlanYear);
        Set<String> employeeIds = Set.of("000-E1");
        List<ProspectStrategySyncData> prospectStrategySyncData = List.of(ProspectStrategySyncData.builder()
                .employeeId("000-E1").k1(true).homeState("MA").homePostalCode("00123")
                .enrolledCvgCodes(List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
                        EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
                        EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
                .build());
        List<Strategy> strategy = createStrategyForMA();

        Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetailsToSave = new HashMap<>();
        StrategyGroupDetails grpK1 = new StrategyGroupDetails();
        grpK1.setStrategyGroupId(93141);
        employeeStrategyGroupDetailsToSave.put("000-E1", Set.of(grpK1));

        List<EmployeeCensusStrategyGroupDetails> existingStrategyGroupsForCompanyCodeList = prepareStartegyGroupByCompanyAndStrategy();

        when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode))
                .thenReturn(existingStrategyGroupsForCompanyCodeList);
        when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(Arrays.asList(company));
        when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70)).thenReturn(company);
        doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(employeeIds);
        doNothing().when(employeeBenefitGroupService)
                .insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
        doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(employeeIds);
        doNothing().when(prospectDefaultPlanMappingService).createCensusDefaultRegionalPlanMapping(any(Company.class),
                anyList());
        when(strategyService.getAllStrategies(company.getId())).thenReturn(strategy);
        when(portfolioRuleDao.getMedicalPortfoliosBy(78981L, company.getRealmPlanYearId(),
                company.getHeadQuatersState())).thenReturn(prepareMedPortfolio(1, 2));
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(true);

        prospectStrategySyncService.handleCensusAddEvent(prospectStrategySyncData, companyCode);

        verify(employeeBenefitGroupDao, times(1)).getStartegyGroupByCompanyAndStrategy(companyCode);
        verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(Set.of("000-E1"));
        verify(employeeBenefitGroupService, times(1))
                .insertNewEmployeeStrategyGroups(employeeStrategyGroupDetailsToSave);
        verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(employeeIds);
        verify(prospectDefaultPlanMappingService, times(1)).createCensusDefaultRegionalPlanMapping(any(Company.class),
                anyList());
        verify(prospectDefaultPlanAssignmentService, times(0)).assignDefaultPlanBy(anySet(), anyLong(), anyMap(),
                anySet());
        verify(prospectDefaultPlanAssignmentService, times(1)).assignDefaultPlanForMissingEmployees(
                emlIdsArgCaptor.capture(), strategyIdArgCaptor.capture(), anyLong(), portfolioIdsArgCaptor.capture(),
                anyBoolean());
        verify(employeePlanAssignmentService, times(1)).updateEePlanAssignmentCvgCode(emlIdsArgCaptor.capture());
        verify(tibRateService, times(0)).saveRatesPerEmployeeIds(any(), anyLong(), anySet());
        verify(cacheService, times(1)).invalidateCache("OMS-BENEFIT-PLAN-RATES", "COMPANY", companyCode);
        verify(prospectCompanyService, times(1)).getProspectCompanyDetails(anyString(), anyLong());
        verify(companyService, times(1)).getXbssCompaniesByCode(anyString());
        verify(benefitGroupService, times(0)).addGroup(any(), any(GroupData.class), anyLong());
        assertTrue(emlIdsArgCaptor.getAllValues().get(0).containsAll(List.of("000-E1")));
        assertTrue(emlIdsArgCaptor.getAllValues().get(1).containsAll(List.of("000-E1")));
        assertEquals(78981L, strategyIdArgCaptor.getAllValues().get(0).longValue());
        assertTrue(portfolioIdsArgCaptor.getAllValues().get(0).keySet().containsAll(List.of(1L, 2L)));

    }

    @Test
    public void handleChangeEventTestWhenHomeStateChangedToMA() {
        ArgumentCaptor<Long> strategyGroupIDArgCaptor = ArgumentCaptor.forClass(Long.class);
        // given
        // data
        Realm realm = new Realm();
        realm.setBenExchange(TRINET_OMS.getBenExchng());
        String companyCode = "a1b2c3";
        Company company1 = new Company();
        company1.setCode(companyCode);
        company1.setId(1234);
        company1.setRealmPlanYearId(70);
        company1.setRealm(realm);
        company1.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
        Company company2 = new Company();
        company2.setCode(companyCode);
        company2.setId(2345);
        company2.setRealmPlanYearId(74);
        company2.setRealm(realm);
        company2.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
        Set<String> employeeIdsToDeleteStrategyGroup = Set.of("0000000123458", "0000000123457", "0000000123456");

        List<ProspectStrategySyncData> prospectDataRequest = prepareStrategySyncDataForHomeStateChange();
        Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails = prepareEmployeeCensusStrategyGroupDataForHomeStateChange();
        List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = prepareStrategyGroupDetailsForHomeStateChange();
        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(true);
        when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(companyCode))
                .thenReturn(employeeStrategyGroupDetails);

        doReturn(strategyGroupDetails).when(employeeBenefitGroupDao).getStartegyGroupByCompanyAndStrategy(companyCode);

        prospectStrategySyncService.handleCensusChangeEvent(prospectDataRequest, companyCode);

        verify(employeeBenefitGroupDao, times(1)).getEmployeeStrategyGroupDetails(companyCode);
        verify(employeeBenefitGroupDao, times(1)).getStartegyGroupByCompanyAndStrategy(companyCode);
        verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDeleteStrategyGroup);

    }

    @Test
    public void handleChangeEventTestWhenHomeStateChangedToMAWhenMAGroupNotPresent() {
        ArgumentCaptor<Long> strategyGroupIDArgCaptor = ArgumentCaptor.forClass(Long.class);
        // given
        // data
        Realm realm = new Realm();
        realm.setBenExchange(TRINET_OMS.getBenExchng());
        String companyCode = "a1b2c3";
        Company company1 = new Company();
        company1.setCode(companyCode);
        company1.setId(1234);
        company1.setRealmPlanYearId(70);
        company1.setRealm(realm);
        company1.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
        Company company2 = new Company();
        company2.setCode(companyCode);
        company2.setId(2345);
        company2.setRealmPlanYearId(74);
        company2.setRealm(realm);
        company2.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
        Set<String> employeeIdsToDeleteStrategyGroup = Set.of("0000000123458");

        List<ProspectStrategySyncData> prospectDataRequest = Collections.singletonList(prepareStrategySyncDataForHomeStateChange().get(2));
        Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails = prepareEmployeeCensusStrategyGroupDataForHomeStateChange();
        List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = prepareStrategyGroupDetailsForHomeStateChange().stream()
                .filter(strategyGroupDetail-> !US.MASSACHUSETTS.getANSIabbreviation().equals(strategyGroupDetail.getGroupDesc())).collect(Collectors.toList());

        when(AppRulesAndConfigsUtils.isProspectDefaultMAGroupCreationEnabled()).thenReturn(true);
        when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(companyCode))
                .thenReturn(employeeStrategyGroupDetails);

        doReturn(strategyGroupDetails).when(employeeBenefitGroupDao).getStartegyGroupByCompanyAndStrategy(companyCode);

        prospectStrategySyncService.handleCensusChangeEvent(prospectDataRequest, companyCode);

        verify(employeeBenefitGroupDao, times(1)).getEmployeeStrategyGroupDetails(companyCode);
        verify(employeeBenefitGroupDao, times(1)).getStartegyGroupByCompanyAndStrategy(companyCode);
        verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(employeeIdsToDeleteStrategyGroup);

    }
	
	/**
	 * given company code</br>
	 * when strategySyncOnHQLocationChange method is called</br>
	 * then reset the strategies for the companies
	 **/
	@Test
	public void strategySyncOnHQLocationChangeTest1() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		Map<Long, CompanyStrategyDetailsDto> companyStrategyDetailsDtos = prepareCompanyStrategyDetails1();
		// method mocks
		when(companyService.getCompanyStrategyDetails(companyCode)).thenReturn(companyStrategyDetailsDtos);
		// when
		prospectStrategySyncService.strategySyncOnHQLocationChange(companyCode);
		// then
		// verify
		verify(strategyService, times(0)).resetStrategiesBy(companyCodeCaptor.capture(), companyIdCaptor.capture(),
				realmPlanYearIdCaptor.capture(), strategyIdsCaptor.capture());
		verify(cacheService, times(1)).invalidateCache("ALL", "COMPANY", companyCode);
	}

	/**
	 * given company code and strategies are not present</br>
	 * when strategySyncOnHQLocationChange method is called</br>
	 * then do nothing
	 **/
	@Test
	public void strategySyncOnHQLocationChangeTest2() {
		// given
		// data
		String companyCode = "B5NP1PC1";
		Realm realm = new Realm();
		realm.setBenExchange(TRINET_II.getBenExchng());
		Company company = new Company();
		company.setCode(companyCode);
		company.setId(129059);
		company.setRealmPlanYearId(64);
		company.setRealm(realm);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 64)).thenReturn(company);
		Map<Long, CompanyStrategyDetailsDto> companyStrategyDetailsDtos = prepareCompanyStrategyDetails();
		// method mocks
		when(companyService.getCompanyStrategyDetails(companyCode)).thenReturn(companyStrategyDetailsDtos);
		doNothing().when(strategyService).resetStrategiesBy(companyCodeCaptor.capture(), companyIdCaptor.capture(),
				realmPlanYearIdCaptor.capture(), strategyIdsCaptor.capture());
		// when
		prospectStrategySyncService.strategySyncOnHQLocationChange(companyCode);
		// then
		// verify
		verify(strategyService, times(1)).resetStrategiesBy(companyCodeCaptor.capture(), companyIdCaptor.capture(),
				realmPlanYearIdCaptor.capture(), strategyIdsCaptor.capture());
	}

	@Test
	public void rateSyncOnDependentChangeTest() {
		// given
		String companyCode = "a1b2c3";
		List<String> employeeIds = List.of("0000000123456", "0000000123457");
		Set<String> employeeIdSet = new HashSet<>(employeeIds);

		Realm realm = new Realm();
		realm.setBenExchange(TRINET_OMS.getBenExchng());

		Company company1 = new Company();
		company1.setId(1234L);
		company1.setRealmPlanYearId(70L);
		company1.setCode(companyCode);
		company1.setRealm(realm);
		company1.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());

		Company company2 = new Company();
		company2.setId(2345L);
		company2.setRealmPlanYearId(74L);
		company2.setCode(companyCode);
		company2.setRealm(realm);
		company2.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());

		List<Company> bssCompanies = List.of(company1, company2);

		Strategy strategy1 = new Strategy();
		strategy1.setId(1111L);

		Strategy strategy2 = new Strategy();
		strategy2.setId(2222L);

		List<Strategy> strategies = List.of(strategy1, strategy2);

		// method mocks
		when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(bssCompanies);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70L)).thenReturn(company1);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 74L)).thenReturn(company2);
		when(strategyService.getAllStrategies(1234L)).thenReturn(strategies);
		when(strategyService.getAllStrategies(2345L)).thenReturn(strategies);
		doNothing().when(tibRateService).saveRatesPerEmployeeIds(any(Company.class), anyLong(), anySet());

		// when
		prospectStrategySyncService.rateSyncOnDependentChange(employeeIds, companyCode);

		// then
		verify(companyService, times(1)).getXbssCompaniesByCode(companyCode);
		verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 70L);
		verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 74L);
		verify(strategyService, times(1)).getAllStrategies(1234L);
		verify(strategyService, times(1)).getAllStrategies(2345L);
		verify(tibRateService, times(4)).saveRatesPerEmployeeIds(any(Company.class), anyLong(), eq(employeeIdSet));
	}

	private List<ProspectStrategySyncData> prepareStrategySyncData() {
		return List.of(ProspectStrategySyncData.builder().employeeId("0000000123456").k1(false).isLocationChanged(true)
				.enrolledCvgCodes(List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
						EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
						EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
				.build(),
				ProspectStrategySyncData.builder().employeeId("0000000123457").k1(true).isLocationChanged(false)
						.enrolledCvgCodes(
								List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
										EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("W").build(),
										EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
						.build(),
				ProspectStrategySyncData.builder().employeeId("0000000123458").k1(false).isLocationChanged(false)
						.enrolledCvgCodes(
								List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
										EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
										EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
						.build());
	}
	
	@Test
	public void rateSyncOnDependentChange_NotTibProspect() {
		// given
		String companyCode = "testCompanyCode";
		List<String> employeeIds = List.of("0000000123456", "0000000123457");
		Set<String> employeeIdSet = new HashSet<>(employeeIds);

		Realm realm = new Realm();
		realm.setBenExchange(TRINET_III.getBenExchng());


		Company company1 = new Company();
		company1.setId(1234L);
		company1.setRealmPlanYearId(70L);
		company1.setCode(companyCode);
		company1.setRealm(realm);
		company1.setAuthBroker("NOT_TIB");

		Company company2 = new Company();
		company2.setId(2345L);
		company2.setRealmPlanYearId(74L);
		company2.setCode(companyCode);
		company2.setRealm(realm);
		company2.setAuthBroker("NOT_TIB");

		List<Company> bssCompanies = List.of(company1, company2);

		Strategy strategy1 = new Strategy();
		strategy1.setId(1111L);

		Strategy strategy2 = new Strategy();
		strategy2.setId(2222L);

		List<Strategy> strategies = List.of(strategy1, strategy2);

		// method mocks
		when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(bssCompanies);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70L)).thenReturn(company1);
		when(prospectCompanyService.getProspectCompanyDetails(companyCode, 74L)).thenReturn(company2);

		// when
		prospectStrategySyncService.rateSyncOnDependentChange(employeeIds, companyCode);

		// then
		verify(companyService, times(1)).getXbssCompaniesByCode(companyCode);
		verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 70L);
		verify(prospectCompanyService, times(1)).getProspectCompanyDetails(companyCode, 74L);
//		verify(logger, times(2)).error(eq("TIB Rate Sync is not applicable for company: {}"), eq("testCompanyCode"));
	}

	private Map<String, Set<StrategyGroupDetails>> prepareEmployeeStrategyGroups() {
		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetails = new HashMap<>();
		Set<StrategyGroupDetails> strategyGroupDetails = new HashSet<>();
		StrategyGroupDetails grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93143);
		strategyGroupDetails.add(grpDetails);
		grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93144);
		strategyGroupDetails.add(grpDetails);
		employeeStrategyGroupDetails.put("0000000123456", strategyGroupDetails);
		strategyGroupDetails = new HashSet<>();
		grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93141);
		strategyGroupDetails.add(grpDetails);
		grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93142);
		strategyGroupDetails.add(grpDetails);
		employeeStrategyGroupDetails.put("0000000123457", strategyGroupDetails);
		return employeeStrategyGroupDetails;
	}

	private Map<String, Set<StrategyGroupDetails>> prepareEmployeeStrategyGroups1() {
		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetails = new HashMap<>();
		Set<StrategyGroupDetails> strategyGroupDetails = new HashSet<>();
		StrategyGroupDetails grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93142);
		strategyGroupDetails.add(grpDetails);
		employeeStrategyGroupDetails.put("0000000123456", strategyGroupDetails);
		strategyGroupDetails = new HashSet<>();
		grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93141);
		strategyGroupDetails.add(grpDetails);
		employeeStrategyGroupDetails.put("0000000123457", strategyGroupDetails);
		strategyGroupDetails = new HashSet<>();
		grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93142);
		strategyGroupDetails.add(grpDetails);
		employeeStrategyGroupDetails.put("0000000123458", strategyGroupDetails);

		return employeeStrategyGroupDetails;
	}

	private Map<String, List<EmployeeCensusStrategyGroupDetails>> prepareEmployeeCensusStrategyGroupData() {
		Map<String, List<EmployeeCensusStrategyGroupDetails>> strategyGroupMap = new HashMap<>();
		strategyGroupMap.put("0000000123456",
				List.of(EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123456").strategyId(78981)
						.strategyGroupId(93141).groupType("K1").build(),
						EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123456").strategyId(78982)
								.strategyGroupId(93142).groupType("K1").build()));
		strategyGroupMap.put("0000000123457",
				List.of(EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123457").strategyId(78981)
						.strategyGroupId(93143).groupType("STD").build(),
						EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123457").strategyId(78982)
								.strategyGroupId(93144).groupType("STD").build()));
		strategyGroupMap.put("0000000123458",
				List.of(EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123458").strategyId(78981)
						.strategyGroupId(93143).groupType("STD").build(),
						EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123458").strategyId(78982)
								.strategyGroupId(93144).groupType("STD").build()));
		return strategyGroupMap;
	}
	
	private List<EmployeeCensusStrategyGroupDetails> prepareStrategyGroupDetails() {
		List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = new ArrayList<>();
		strategyGroupDetails.addAll(List.of(
				EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93141).groupType("K1")
						.build(),
				EmployeeCensusStrategyGroupDetails.builder().strategyId(78982).strategyGroupId(93142).groupType("K1")
						.build(),
				EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93143).groupType("STD")
						.build(),
				EmployeeCensusStrategyGroupDetails.builder().strategyId(78982).strategyGroupId(93144).groupType("STD")
						.build()));
		return strategyGroupDetails;
	}

	private List<String> prepareEmployeeIdsToDelete() {
		return List.of("Emp1", "Emp2");
	}

	private List<EmployeeCensusStrategyGroupDetails> prepareStartegyGroupByCompanyAndStrategy() {
		return List.of(
				EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93141).groupType("K1")
						.build(),
				EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93142).groupType("STD")
						.build());
	}

	private Map<Long, CompanyStrategyDetailsDto> prepareCompanyStrategyDetails() {
		return Map.of(129059L, CompanyStrategyDetailsDto.builder().companyId(129059L).allStrategyIds(Set.of(1L, 2L, 3L))
				.realmPlanYearId(64).build());
	}

	private Map<Long, CompanyStrategyDetailsDto> prepareCompanyStrategyDetails1() {
		return Map.of(129059L, CompanyStrategyDetailsDto.builder().companyId(129059L).allStrategyIds(null)
				.realmPlanYearId(64).build());
	}

	private List<EePlanAssignment> prepareEmployeePlanAssignments() {
		return List.of(
				EePlanAssignment.builder()
						.eePlanAssignmentPK(
								EePlanAssignmentPK.builder()
										.strategyId(1111)
										.emplId("0000000123456")
										.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
										.build()
						)
						.benefitPlan("123456")
						.covrgCD("2")
						.build(),
				EePlanAssignment.builder()
						.eePlanAssignmentPK(
								EePlanAssignmentPK.builder()
										.strategyId(1111)
										.emplId("0000000123457")
										.benefitType(BSSApplicationConstants.DENTAL_PLAN_TYPE)
										.build()
						)
						.benefitPlan("123456")
						.covrgCD("1")
						.build(),
				EePlanAssignment.builder()
						.eePlanAssignmentPK(
								EePlanAssignmentPK.builder()
										.strategyId(1111)
										.emplId("0000000123458")
										.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
										.build()
						)
						.benefitPlan("123456")
						.covrgCD("1")
						.build()
		);
	}
	
	private Map<String, Set<StrategyGroupDetails>> prepareClientEmployeeStrategyGroups() {
		Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetails = new HashMap<>();
		Set<StrategyGroupDetails> strategyGroupDetails = new HashSet<>();
		StrategyGroupDetails grpDetails = new StrategyGroupDetails();
		grpDetails.setStrategyGroupId(93142);
		strategyGroupDetails.add(grpDetails);
		employeeStrategyGroupDetails.put("0000000123456", strategyGroupDetails);
		return employeeStrategyGroupDetails;
	}
	
	private Company prepareClientCompany() {
		Company cmp = new Company();
		cmp.setCode("2TMF");
		cmp.setName("Trinet Group");
		cmp.setPlanStartDate("10-JAN-2018");
		cmp.setQuater("Q1");
		cmp.setId(16898);
		cmp.setHeadQuatersState("FL");
		cmp.setBenefitStartDate("10-JAN-2018");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70L);
		cmp.setRealmPlanYear(realmPlanYear);
		cmp.setRealmPlanYearId(70L);
		return cmp;
	}

    private List<ProspectCensusResponse> prepareProspectCensus() {
        return List.of(
                ProspectCensusResponse.builder().employeeId("0000000123456").employeeName("John").state("CA")
                        .gender("M").k1(true).salary(BigDecimal.valueOf(6000)).zip("90210").dob("2000-01-01")
                        .dependents(List.of(
                                ProspectCensusResponse.Dependents.builder().dob("2001-09-28").relation("SP")
                                        .covgElection(true).includeInCost(true).build(),
                                ProspectCensusResponse.Dependents.builder().dob("2022-03-31").relation("CH")
                                        .covgElection(true).includeInCost(true).build()))
                        .build(),
                ProspectCensusResponse.builder().employeeId("0000000123457").employeeName("katty Scott").state("TX")
                        .gender("F").k1(false).salary(BigDecimal.valueOf(4500)).zip("77001").dob("1975-09-28")
                        .dependents(List.of(
                                ProspectCensusResponse.Dependents.builder().dob("2001-09-28").relation("SP")
                                        .covgElection(false).includeInCost(true).build(),
                                ProspectCensusResponse.Dependents.builder().dob("2022-03-31").relation("CH")
                                        .covgElection(true).includeInCost(false).build()))
                        .build());

    }

    private List<ProspectStrategySyncData> createProspectStrategySyncDataForMA() {

        return List.of(ProspectStrategySyncData.builder().employeeId("000-E1").k1(false).homeState("MA")
                        .homePostalCode("00123")
                        .enrolledCvgCodes(List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
                                EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
                                EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
                        .build(),
                ProspectStrategySyncData.builder().employeeId("000-E2").k1(true).homeState("CA").homePostalCode("95123")
                        .enrolledCvgCodes(
                                List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
                                        EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
                                        EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
                        .build(),
                ProspectStrategySyncData.builder().employeeId("000-E3").k1(false).homeState("CA")
                        .homePostalCode("95123")
                        .enrolledCvgCodes(
                                List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
                                        EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
                                        EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build()))
                        .build());
    }

    private Map<String, Set<StrategyGroupDetails>> prepareClientEmployeeStrategyGroupsForMA() {
        Map<String, Set<StrategyGroupDetails>> employeeStrategyGroupDetails = new HashMap<>();
        StrategyGroupDetails grpMA = new StrategyGroupDetails();
        grpMA.setStrategyGroupId(93140);
        StrategyGroupDetails grpK1 = new StrategyGroupDetails();
        grpK1.setStrategyGroupId(93141);
        StrategyGroupDetails grpSTD = new StrategyGroupDetails();
        grpSTD.setStrategyGroupId(93142);
        employeeStrategyGroupDetails.put("000-E1", Set.of(grpMA));
        employeeStrategyGroupDetails.put("000-E2", Set.of(grpK1));
        employeeStrategyGroupDetails.put("000-E3", Set.of(grpSTD));

        return employeeStrategyGroupDetails;
    }

    private List<Strategy> createStrategyForMA()
    {
        BenefitGroupStrategy bgs1 = new BenefitGroupStrategy();
        bgs1.setGroupId(2);
        bgs1.setDefaultGroup(true);
        bgs1.setWaitingPeriod("none");
        Strategy strategy = new Strategy();
        strategy.setId(78981L);
        strategy.setBenefitGroupStrategy(Set.of(bgs1));
        return List.of(strategy);
    }
    private List<ProspectStrategySyncData> prepareStrategySyncDataForHomeStateChange() {
        return List.of(ProspectStrategySyncData.builder().employeeId("0000000123456").k1(true).homeState("NY").isLocationChanged(true)
                        .enrolledCvgCodes(List.of(EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("1").build(),
                                EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("2").build(),
                                EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("C").build(),
                                EnrolledCvgCode.builder().benefitType("10").desiredCvgCode("4").build()))
                        .build(),
                ProspectStrategySyncData.builder().employeeId("0000000123457").k1(false).homeState("CA").isLocationChanged(false)
                        .enrolledCvgCodes(
                                List.of(EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("1").build(),
                                        EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("2").build(),
                                        EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("C").build(),
                                        EnrolledCvgCode.builder().benefitType("11").desiredCvgCode("4").build()))

                        .build(),
                ProspectStrategySyncData.builder().employeeId("0000000123458").k1(false).homeState("MA").isLocationChanged(false)
                        .enrolledCvgCodes(
                                List.of(EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("1").build(),
                                        EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("2").build(),
                                        EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("C").build(),
                                        EnrolledCvgCode.builder().benefitType("14").desiredCvgCode("4").build()))
                        .build());
    }

    private Map<String, List<EmployeeCensusStrategyGroupDetails>> prepareEmployeeCensusStrategyGroupDataForHomeStateChange() {
        Map<String, List<EmployeeCensusStrategyGroupDetails>> strategyGroupMap = new HashMap<>();
        strategyGroupMap.put("0000000123456",
                List.of(EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123456").strategyId(78981)
                                .strategyGroupId(93141).groupType("STD").groupDesc("W2").build(),
                        EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123456").strategyId(78982)
                                .strategyGroupId(93142).groupType("STD").groupDesc("W2").build()));
        strategyGroupMap.put("0000000123457",
                List.of(EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123457").strategyId(78981)
                        .strategyGroupId(93145).groupType("STD").groupDesc("W2 MA").build()));
        strategyGroupMap.put("0000000123458",
                List.of(EmployeeCensusStrategyGroupDetails.builder().emplId("0000000123458").strategyId(78981)
                        .strategyGroupId(93143).groupType("STD").groupDesc("W2").build()));
        return strategyGroupMap;
    }

    private List<EmployeeCensusStrategyGroupDetails> prepareStrategyGroupDetailsForHomeStateChange() {
        List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = new ArrayList<>();
        strategyGroupDetails.addAll(List.of(
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93141).groupType("K1").groupDesc("K1")
                        .build(),
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93143).groupType("STD").groupDesc("W2 MA")
                        .build(),
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78981).strategyGroupId(93145).groupType("STD").groupDesc("W2")
                        .build(),
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78982).strategyGroupId(93142).groupType("K1").groupDesc("STD")
                        .build(),
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78982).strategyGroupId(93144).groupType("STD").groupDesc("W2")
                        .build(),
                EmployeeCensusStrategyGroupDetails.builder().strategyId(78982).strategyGroupId(93146).groupType("STD").groupDesc("W2 MA")
                        .build()));
        return strategyGroupDetails;
    }

/**
 * given company code and company is null</br>
 * when strategySyncOnHQLocationChange method is called</br>
 * then do not reset strategies
 **/
@Test
public void strategySyncOnHQLocationChangeTest3() {
    // given
    String companyCode = "B5NP1PC1";
    Map<Long, CompanyStrategyDetailsDto> companyStrategyDetailsDtos = prepareCompanyStrategyDetails();
    // method mocks
    when(companyService.getCompanyStrategyDetails(companyCode)).thenReturn(companyStrategyDetailsDtos);
    when(prospectCompanyService.getProspectCompanyDetails(companyCode, 64)).thenReturn(null);
    // when
    prospectStrategySyncService.strategySyncOnHQLocationChange(companyCode);
    // then
    // verify
    verify(strategyService, times(0)).resetStrategiesBy(anyString(), anyLong(), anyLong(), anySet());
    verify(cacheService, times(1)).invalidateCache("ALL", "COMPANY", companyCode);
}

/**
 * given company code and company is TNXI exchange</br>
 * when strategySyncOnHQLocationChange method is called</br>
 * then do not reset strategies
 **/
@Test
public void strategySyncOnHQLocationChangeTest4() {
    // given
    String companyCode = "B5NP1PC1";
    Realm realm = new Realm();
    realm.setBenExchange(TRINET_XI.getBenExchng());
    Company company = new Company();
    company.setCode(companyCode);
    company.setId(129059);
    company.setRealmPlanYearId(64);
    company.setRealm(realm);
    Map<Long, CompanyStrategyDetailsDto> companyStrategyDetailsDtos = prepareCompanyStrategyDetails();
    // method mocks
    when(companyService.getCompanyStrategyDetails(companyCode)).thenReturn(companyStrategyDetailsDtos);
    when(prospectCompanyService.getProspectCompanyDetails(companyCode, 64)).thenReturn(company);

    // when
    prospectStrategySyncService.strategySyncOnHQLocationChange(companyCode);
    // then
    // verify
    verify(strategyService, times(0)).resetStrategiesBy(anyString(), anyLong(), anyLong(), anySet());
    verify(cacheService, times(1)).invalidateCache("ALL", "COMPANY", companyCode);
}

/**
 * given plan mapping service is enabled</br>
 * when handleCensusChangeEvent is called</br>
 * then planMappingAssignmentService is called with employeeIds and prospectDefaultPlanMappingService is not called
 **/
@Test
public void handleCensusChangeEvent_planMappingServiceEnabled_callsPlanMappingService() {
    // given
    rulesAndConfigsUtilsMockedStatic.when(() -> RulesAndConfigsUtils.isPlanMappingServiceEnabled(anyLong()))
            .thenReturn(true);
    Realm realm = new Realm();
    realm.setBenExchange(TRINET_II.getBenExchng());
    String companyCode = "a1b2c3";
    Company company1 = new Company();
    company1.setCode(companyCode);
    company1.setId(1234);
    company1.setRealmPlanYearId(70);
    company1.setRealm(realm);
	company1.setProspectCompany(true);

    RealmPlanYear realmPlanYear = new RealmPlanYear();
    realmPlanYear.setId(70L);
    company1.setRealmPlanYear(realmPlanYear);

    List<ProspectStrategySyncData> prospectDataRequest = prepareStrategySyncData();
    Map<String, List<EmployeeCensusStrategyGroupDetails>> employeeStrategyGroupDetails = prepareEmployeeCensusStrategyGroupData();
    List<EmployeeCensusStrategyGroupDetails> strategyGroupDetails = prepareStrategyGroupDetails();

    // method mocks
    when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(companyCode))
            .thenReturn(employeeStrategyGroupDetails);
    when(employeeBenefitGroupDao.getStartegyGroupByCompanyAndStrategy(companyCode))
            .thenReturn(strategyGroupDetails);
    when(companyService.getXbssCompaniesByCode(companyCode)).thenReturn(List.of(company1));
    when(prospectCompanyService.getProspectCompanyDetails(companyCode, 70)).thenReturn(company1);

    Strategy strategy = new Strategy();
    strategy.setId(1L);
    when(strategyService.getAllStrategies(1234L)).thenReturn(List.of(strategy));

    when(cacheService.invalidateCache(anyString(), anyString(), anyString())).thenReturn(true);
    doNothing().when(employeeBenefitGroupService).insertNewEmployeeStrategyGroups(any());
    doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(any(Set.class));

    // when
    prospectStrategySyncService.handleCensusChangeEvent(prospectDataRequest, companyCode);

    // then
    verify(planMappingAssignmentService, atLeastOnce())
            .callPlanMappingService(any(Company.class), anySet());
    verify(prospectDefaultPlanMappingService, never())
            .createCensusDefaultRegionalPlanMapping(any(Company.class), anyList());
}

}