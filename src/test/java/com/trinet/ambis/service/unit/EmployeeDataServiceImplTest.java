package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupTransactionDao;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.persistence.model.EmployeeStrategyGroupTransaction;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.ModelCompareEmployeeDataService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.EmployeeDataServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.EmpBenPlanMapping;
import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeBenefitGroup;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.EmployeeSourceData;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.EmployeeStrategyPlanData;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.StrategyGroupPlanRateData;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.ProspectGroupAssignmentService;
import com.trinet.ambis.service.prospect.response.CensusRes;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeDataServiceImplTest extends ServiceUnitTest {

	@Qualifier("employeeDataServiceImpl")
	@InjectMocks
	EmployeeDataServiceImpl employeeDataService;

	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Mock
	EmployeeDataDao employeeDataDao;

	@Mock
	EmployeeDao employeeDao;

	@Mock
	EmployeeBenefitGroupService employeeBenefitGroupService;

	@Mock
	EmployeeStrategyGroupTransactionDao employeeStrategyGroupTransactionDao;

	@Mock
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;
	
	@Mock
	PlanMappingDao planMappingDao;

	@Mock
	PortfolioRuleDao portfolioRuleDao;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	RealmPlanYearRuleService realmPlanYearRuleService;

	@Mock
	RealmPlanYearService realmPlanYearService;
	
	@Mock
	StrategyDao strategyDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	StrategyFundingDataDao strategyFundingDataDao;

	@Mock
	XbssRealmPlyrPlanDao xbssRealmPlyrPlanDao;
	
	@Mock
	ProspectGroupAssignmentService prospectGroupAssignmentService;
	
	@Mock
	ProspectEmployeeService prospectEmployeeService;

	@Mock
    ModelCompareEmployeeDataService modelCompareEmployeeDataService;

	@Mock
	StrategyService strategyService;

	@Mock
	BssCoreServiceClient bssCoreServiceClient;

	private static final String EMP_ID_1 = "employeeId1";
	private static final String EMP_ID_2 = "employeeId2";
	private static final String EMP_ID_3 = "employeeId3";
	private static final String EMP_ID_4 = "employeeId4";
	private static final String EMP_ID_5 = "employeeId5";
	private static final String EMP_ID_6 = "employeeId6";
	private static final String EMP_ID_7 = "employeeId7";
	private static final String BEN_PROG_1 = "benProg1";
	private static final String BEN_PROG_2 = "benProg2";
	private static final String BEN_PROG_3 = "benProg3";
	private static final String BEN_PROG_4 = "benProg4";
	private static final String BEN_PROG_5 = "benProg5";
	private static final String BEN_PROG_6 = "benProg6";
    private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;

    @Before
    public void setUp() {
        mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        if (mockStaticRulesAndConfigsUtils != null) {
            mockStaticRulesAndConfigsUtils.close();
        }
    }
	
	@Test
	public void getEmployeesDataClient() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(10);
		Company company = new Company();
		company.setCode("G48");
		company.setRealmPlanYearId(10);
		company.setRealmPlanYear(realmPlanYear);

		long strategyId = 1;

		Map<String, Employee> employeeDetails = new HashMap<String, Employee>();
		employeeDetails.put(EMP_ID_1, prepareEmployee(EMP_ID_1, "emp name 1", "dept1", "job title 1", "location 1"));
		employeeDetails.put(EMP_ID_2, prepareEmployee(EMP_ID_2, "emp name 2", "dept2", "job title 2", "location 2"));
		
		Set<Employee> bssEmployees = new HashSet<Employee>();
		bssEmployees.add(prepareEmployee(EMP_ID_1, 1111, "grp 1", "ben prog 1", 11111));
		bssEmployees.add(prepareEmployee(EMP_ID_2, 2222, "grp 2", "ben prog 2", 22222));
		bssEmployees.add(prepareEmployee(EMP_ID_3, 3333, "grp 3", "ben prog 3", 33333));

		when(employeeDataDao.getEmployeesByCompany(company)).thenReturn(employeeDetails);
		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId)).thenReturn(bssEmployees);
		Set<EmployeeData> result = employeeDataService.getEmployeesData(company, strategyId);

		verify(employeeDataDao, times(1)).getEmployeesByCompany(company);
		verify(employeeDataDao, times(1)).getEmployeeGroupDetailsByStrategy(strategyId);
		assertEquals(2, result.size());
		for (EmployeeData employeeData : result) {
			assertEmployee(employeeData);
		}
	}
	
	@Test
	public void getEmployeesDataProspectCensus() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(10);
		Company company = new Company();
		company.setCode("PROSPECT-COMPANY-CODE");
		company.setRealmPlanYearId(10);
		company.setRealmPlanYear(realmPlanYear);
		company.setProspectCompany(true);
		long strategyId = 0;

		Set<EmployeeData> prospectEmployees = new HashSet<>();
		prospectEmployees.add(prepareEmployeeData1(EMP_ID_1, "John Doe", "grp 1", 1));
		prospectEmployees.add(prepareEmployeeData1(EMP_ID_2, "John Doe1", "grp 1", 1));

		when(prospectGroupAssignmentService.getEmployeeGroupAssignments(company.getCode()))
				.thenReturn(prospectEmployees);
		Set<EmployeeData> result = employeeDataService.getEmployeesData(company, strategyId);
		verify(prospectGroupAssignmentService, times(1)).getEmployeeGroupAssignments(company.getCode());
		assertEquals(2, result.size());
		assertTrue(CollectionUtils.isEqualCollection(prospectEmployees, result));
	}
	
	@Test
	public void getEmplDataProsTnStrategy() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(10);
		Company company = new Company();
		company.setCode("PROSPECT-COMPANY-CODE");
		company.setRealmPlanYearId(10);
		company.setRealmPlanYear(realmPlanYear);
		company.setProspectCompany(true);
		long strategyId = 1;

		Set<Employee> employees = new HashSet<Employee>();
		employees.add(prepareEmployee(EMP_ID_1, "grp 1", 1111, 1));
		employees.add(prepareEmployee(EMP_ID_2, "grp 2", 2222, 1));
		employees.add(prepareEmployee(EMP_ID_3, "grp 3", 3333, 1));

		Set<EmployeeData> prospectEmployees = new HashSet<>();
		prospectEmployees.add(prepareEmployeeData(EMP_ID_1, "John Doe", "grp 1", 1111, 1));
		prospectEmployees.add(prepareEmployeeData(EMP_ID_2, "Katty Scott", "grp 2", 2222, 1));
		prospectEmployees.add(prepareEmployeeData(EMP_ID_3, "John S", "grp 3", 3333, 1));

		List<CensusRes> censusResponse = prepareCensusResponse();

		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId)).thenReturn(employees);
		when(prospectEmployeeService.getEmployees(company.getCode())).thenReturn(censusResponse);
		Set<EmployeeData> result = employeeDataService.getEmployeesData(company, strategyId);
		verify(employeeDataDao, times(1)).getEmployeeGroupDetailsByStrategy(strategyId);
		verify(prospectEmployeeService, times(1)).getEmployees(company.getCode());
		assertEquals(3, result.size());
		assertTrue(CollectionUtils.isEqualCollection(prospectEmployees, result));
		for (EmployeeData employeeData : result) {
			if(EMP_ID_1.equals(employeeData.getEmplId())) {
				assertEquals("grp 1", employeeData.getBenefitGroupName());
				assertEquals("John Doe", employeeData.getEmplName());
			}
			if(EMP_ID_2.equals(employeeData.getEmplId())) {
				assertEquals("grp 2", employeeData.getBenefitGroupName());
				assertEquals("Katty Scott", employeeData.getEmplName());
			}
			if(EMP_ID_3.equals(employeeData.getEmplId())) {
				assertEquals("grp 3", employeeData.getBenefitGroupName());
				assertEquals("John S", employeeData.getEmplName());
			}
		}
	}


	@Test
	public void getEmplDataNewClient() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(10);
		Company company = new Company();
		company.setCode("NEW-CLIENT-COMPANY-CODE");
		company.setRealmPlanYearId(10);
		company.setProspectConvertedClient(true);
		company.setProspectConvertedOnboardingClient(true);
		company.setRenewalCompany(false);
		company.setRealmPlanYear(realmPlanYear);
		company.setProspectCompany(false);
		long strategyId = 1;

		Set<Employee> employees = new HashSet<Employee>();
		employees.add(prepareEmployee(EMP_ID_1, "grp 1", 1111, 1));
		employees.add(prepareEmployee(EMP_ID_2, "grp 2", 2222, 1));
		employees.add(prepareEmployee(EMP_ID_3, "grp 3", 3333, 1));

		Map<String, Employee> employeeDetails = new HashMap<String, Employee>();
		employeeDetails.put(EMP_ID_1, prepareNewClientEmployee(EMP_ID_1, "emp name 1",null, null, null, true));
		employeeDetails.put(EMP_ID_2, prepareNewClientEmployee(EMP_ID_2, "emp name 2",null, null, null,false));
		employeeDetails.put(EMP_ID_3, prepareNewClientEmployee(EMP_ID_3, "emp name 3",null, null, null, false));


		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId)).thenReturn(employees);
		when(bssCoreServiceClient.getCensusByCode(company.getCode())).thenReturn(employeeDetails);
		Set<EmployeeData> result = employeeDataService.getEmployeesData(company, strategyId);
		verify(employeeDataDao, times(1)).getEmployeeGroupDetailsByStrategy(strategyId);
		verify(bssCoreServiceClient, times(1)).getCensusByCode(company.getCode());
		assertEquals(3, result.size());
		for (EmployeeData employeeData : result) {
			if(EMP_ID_1.equals(employeeData.getEmplId())) {
				assertEquals("grp 1", employeeData.getBenefitGroupName());
				assertEquals("emp name 1", employeeData.getEmplName());
                assertTrue(employeeData.isK1());
			}
			if(EMP_ID_2.equals(employeeData.getEmplId())) {
				assertEquals("grp 2", employeeData.getBenefitGroupName());
				assertEquals("emp name 2", employeeData.getEmplName());
                assertFalse(employeeData.isK1());
			}
			if(EMP_ID_3.equals(employeeData.getEmplId())) {
				assertEquals("grp 3", employeeData.getBenefitGroupName());
				assertEquals("emp name 3", employeeData.getEmplName());
                assertFalse(employeeData.isK1());
			}
		}
	}

	@Test
	public void getEmployeeStrategiesPlanCostDataTest() {
		Company company = new Company();
		company.setId( 5L );
		company.setCode("VVC");
		company.setRenewalCompany(true);
		Realm realm = new Realm();
		realm.setBenExchange( BenExchngEnums.TRINET_III.getBenExchng() );
		company.setRealm( realm );
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(50);
		realmPlanYear.setPlanYearEnd(new Date());
		company.setRealmPlanYearId( realmPlanYear.getId() );
		company.setRealmPlanYear(realmPlanYear);

		RealmPlanYear prevRPY = new RealmPlanYear();
		prevRPY.setId(40);
		long oneYear = 365L * 24 * 60 * 60 * 1000;
		prevRPY.setPlanYearEnd(new Date( realmPlanYear.getPlanYearEnd().getTime() - oneYear ));

		Strategy strategy = new Strategy();
		strategy.setCompanyId(5L);

		long oldStrategyId = 172304;
		long newStrategyId = 223220;
		List<Long> strategyList = new ArrayList<>();
		strategyList.add( newStrategyId );

		when( strategyDataDao.getEmplStrategyBenGroup( company.getId() )).thenReturn( prepareEmplBenGroupChangeMap() );
		when( employeeDataDao.getMirrorPlanEnrolledEmployees( company )).thenReturn( new MultiKeyMap() );
		when( strategyDataDao.getGroupStrategyPlanCost( eq(company), anyList() ) ).thenReturn( prepareStrategyGroupPlanCostMap() );
		when( strategyFundingDataDao.getFundingDetailsByStrategyId( anyList(), eq(company), eq(true), Mockito.any(Date.class) )).thenReturn( prepareFundingDetailMap() );
		when( strategyDataDao.getStrategyProgramPlantypeOfferings( anyList(), anyList() ) ).thenReturn( prepPlanTypeOfferingMap() );
		when( RulesAndConfigsUtils.isVendorMappingOn( anyLong() )).thenReturn( true );
		when(realmPlanYearService.getPreviousRealmPlanYear(company.getCode(), company.getRealmPlanYearId())).thenReturn(prevRPY);
		when( employeeDataDao.getEmployeeCensusStrategyPlanData( company, oldStrategyId, realmPlanYear.getPlanYearEnd() ) ).thenReturn( prepareStrategyList() );
		when( realmPlanYearService.getPreviousRealmPlanYear( company.getRealmPlanYear() ) ).thenReturn( prevRPY );
		when( xbssRealmPlyrPlanDao.findByRealmYearIdInAndPlanTypeInOrderByRealmYearId( anySet(), anyList() ) ).thenReturn( prepareRealmPlyrPlans() );
		Map<String,Map<String,String>> defaultPlanMap = new HashMap<>();
		when( realmDataDao.getPortfilioDefaultPlans( company.getRealmPlanYearId() )).thenReturn( defaultPlanMap );
		when( portfolioRuleDao.getPortfoliosByHqRegion( company.getRealmPlanYearId(), company.getHeadQuatersState(),
				company.getZipCode(), company.getExclusiveMedPlan(), company.getPlanStartDate(), false ) ).thenReturn( preparePlanCarrierMap() );
		Set<String> outOfRegionPlans = prepareOutOfRegionPlans();
		when( realmDataDao.getBSOutOfRegionPlans( company )).thenReturn( outOfRegionPlans );
		when( planMappingDao.getPrimaryPlanMappings(company, outOfRegionPlans) ).thenReturn( preparePrimaryPlanMappings() );
		when( employerEmployeePlansMappingDao.getEmployerEmployeePlansMappingByRealmYearId( realmPlanYear.getId() )).thenReturn( prepareErEePlanMapping() );
		when( employeeDataDao.getEmpPlanMapping( company.getCode(), realmPlanYear.getId() )).thenReturn( prepareEmpBenPlanMappings() );
		when( employeeBenefitGroupDao.getBenefitProgramDetails(company) ).thenReturn( prepareBenefitGroupMap() );
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( eq( company ) )).thenReturn( false );

		List<EmployeeStrategyData> result = employeeDataService.getEmployeeStrategiesPlanCostData( company, oldStrategyId, strategyList );
		
		// Test group names applied to their corresponding plan/year strategies
		EmployeeStrategyData george = findMyTestCase( "00001797470", result );
		EmployeeStrategyPlanData lastYears = george.getStrategyDetails().get(0);
		EmployeeStrategyPlanData thisYears = george.getStrategyDetails().get(1);
		assertEquals( "Ocean Consultants", lastYears.getGroupName() );
		assertEquals( "UNSUBMITTED CHANGE", thisYears.getGroupName() );
		
		// Test employee assigned to a different group in the new strategy
		EmployeeStrategyData amalea = findMyTestCase( "00002627852", result );
		lastYears = amalea.getStrategyDetails().get(0);
		thisYears = amalea.getStrategyDetails().get(1);
		assertEquals( "Ocean Consultants", lastYears.getGroupName() );
		assertEquals( "Ocean Partners New Improved", thisYears.getGroupName() );

	}

	@Test
	public void getEmployeeStrategiesPlanCostDataForProspectTest() {
		Company company = new Company();
		company.setProspectCompany(true);
		long currentStrategyId = ProspectConstants.PROSPECT_STRATEGY_ID;
		long newStrategyId = 223220;
		List<Long> strategyList = new ArrayList<>();
		strategyList.add( newStrategyId );

		when(modelCompareEmployeeDataService.getEmployeeStrategiesPlanCostData(company, currentStrategyId,
				strategyList)).thenReturn(new ArrayList<>());

		employeeDataService.getEmployeeStrategiesPlanCostData( company, currentStrategyId, strategyList );

		verify(modelCompareEmployeeDataService, times(1)).getEmployeeStrategiesPlanCostData(company, currentStrategyId,
				strategyList);

	}

	private EmployeeStrategyData findMyTestCase( String emplid, List<EmployeeStrategyData> testCases ) {
		for( EmployeeStrategyData d : testCases ) {
			if( emplid.equals( d.getEmplId() ) ) {
				return d;
			}
		}
		return null;
	}

	/* For Client Company */
	@Test
	@SuppressWarnings("unchecked")
	public void updateEmployeeAssignment() {
		Company company = new Company();
		company.setCode("G48");
		long strategyId = 1000L;
		EmployeeAssignmentData employeeAssignmentData = prepareEmployeeAssignmentData();
		ArgumentCaptor<List<EmployeeStrategyGroupTransaction>> empsArgCaptor = ArgumentCaptor.forClass(List.class);
		when(employeeStrategyGroupTransactionDao.saveAll(any())).thenAnswer(test -> test.getArguments()[0]);
		
		employeeDataService.updateEmployeeAssignment(company, employeeAssignmentData, strategyId);

		verify(employeeStrategyGroupTransactionDao, times(1)).saveAll(empsArgCaptor.capture());
		List<EmployeeStrategyGroupTransaction> saveArg = empsArgCaptor.getValue();
		assertEquals(4, saveArg.size());
		assertTrue(saveArg.stream().filter(txn -> "00001113666".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "00001415472".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "00001520518".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "00001523259".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "1234".equals(txn.getEmplid())).findFirst().isEmpty());
		verify(prospectGroupAssignmentService, times(0)).updateEmployeeGroupAssignment(anyLong(), any());
		verify(strategyService, times(1)).createOmsStrategyEstimate(company, Set.of(strategyId));
	}

	/* For Prospect Trinet Strategy where strategyId is non 0 */
	@Test
	public void updateEmployeeAssignment1() {
		Company company = new Company();
		company.setCode("a1b2c3");
		long strategyId = 1000L;
		EmployeeAssignmentData employeeAssignmentData = prepareEmployeeAssignmentData();
		ArgumentCaptor<List<EmployeeStrategyGroupTransaction>> empsArgCaptor = ArgumentCaptor.forClass(List.class);
		when(employeeStrategyGroupTransactionDao.saveAll(any())).thenAnswer(test -> test.getArguments()[0]);
		
		employeeDataService.updateEmployeeAssignment(company, employeeAssignmentData, strategyId);

		verify(employeeStrategyGroupTransactionDao, times(1)).saveAll(empsArgCaptor.capture());
		List<EmployeeStrategyGroupTransaction> saveArg = empsArgCaptor.getValue();
		assertEquals(4, saveArg.size());
		assertTrue(saveArg.stream().filter(txn -> "00001113666".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "00001415472".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "00001520518".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "00001523259".equals(txn.getEmplid())).findFirst().isPresent());
		assertTrue(saveArg.stream().filter(txn -> "1234".equals(txn.getEmplid())).findFirst().isEmpty());
		verify(strategyService, times(1)).createOmsStrategyEstimate(company, Set.of(strategyId));
		verify(prospectGroupAssignmentService, times(0)).updateEmployeeGroupAssignment(anyLong(), any());
	}

	/* For Prospect Current Strategy where strategyId is 0 */
	@Test
	public void updateEmployeeAssignment2() {
		Company company = new Company();
		company.setCode("a1b2c3");
		company.setProspectCompany(true);
		long strategyId = ProspectConstants.PROSPECT_STRATEGY_ID;
		EmployeeAssignmentData employeeAssignmentData = prepareEmployeeAssignmentData();

		doNothing().when(prospectGroupAssignmentService).updateEmployeeGroupAssignment(strategyId,
				employeeAssignmentData);

		employeeDataService.updateEmployeeAssignment(company, employeeAssignmentData, strategyId);

		verify(prospectGroupAssignmentService, times(1)).updateEmployeeGroupAssignment(strategyId,
				employeeAssignmentData);
		verify(strategyService, times(0)).createOmsStrategyEstimate(company, Set.of(strategyId));
	}

	private void assertEmployee(EmployeeData employeeData) {
		Map<String, Employee> expected = new HashMap<String, Employee>();
		expected.put(EMP_ID_1, prepareExpectedEmployee(EMP_ID_1, "dept1", "emp name 1", "job title 1", "location 1",
				1111, "grp 1", "ben prog 1", 11111));
		expected.put(EMP_ID_2, prepareExpectedEmployee(EMP_ID_2, "dept2", "emp name 2", "job title 2", "location 2",
				2222, "grp 2", "ben prog 2", 22222));
		assertEquals(expected.get(employeeData.getEmplId()).getEmplName(), employeeData.getEmplName());
		assertEquals(expected.get(employeeData.getEmplId()).getLocation(), employeeData.getLocation());
		assertEquals(expected.get(employeeData.getEmplId()).getJobTitle(), employeeData.getJobTitle());
		assertEquals(expected.get(employeeData.getEmplId()).getDepartment(), employeeData.getDepartment());
		assertEquals(expected.get(employeeData.getEmplId()).getEmplRcd(), employeeData.getEmplRcd());
		assertEquals(expected.get(employeeData.getEmplId()).getBenefitGroupName(),
				employeeData.getBenefitGroupName());
		assertEquals(expected.get(employeeData.getEmplId()).getBenefitGroupId(), employeeData.getBenefitGroupId());
		assertEquals(expected.get(employeeData.getEmplId()).getBenefitProgram(), employeeData.getBenefitProgram());
	}

	private Employee prepareExpectedEmployee(String empId, String department, String empName, String jobTitle,
			String location, long grpId, String grpName, String benProg, long rcd) {
		Employee emp = new Employee();
		emp.setEmplId(empId);
		emp.setEmplName(empName);
		emp.setDepartment(department);
		emp.setLocation(location);
		emp.setJobTitle(jobTitle);
		emp.setBenefitGroupId(grpId);
		emp.setBenefitGroupName(grpName);
		emp.setBenefitProgram(benProg);
		emp.setEmplRcd(rcd);
		return emp;
	}

	private BenefitGroup prepareNewBenefitProgram(String benProg) {
		BenefitGroup bg = new BenefitGroup();
		bg.setBenefitProgram(benProg);
		return bg;
	}

	private Employee prepareEmployee(String emplId, String benefitProgram) {
		Employee emp = new Employee();
		emp.setEmplId(emplId);
		emp.setBenefitProgram(benefitProgram);
		return emp;
	}

	private void assertEmployee(Set<Employee> value) {
		for (Employee emp : value) {
			assertEquals(null, emp.getEmplId());
			assertEquals(BEN_PROG_1, emp.getBenefitProgram());
			assertEquals(BEN_PROG_1, emp.getBenefitProgram());
			assertEquals(1111, emp.getBenefitGroupId());
			assertEquals("BenGrp1", emp.getBenefitGroupName());
			assertEquals("ElegConfig1", emp.getEligConfig1());
		}
	}

	private Employee prepareEmployee(String empId1, String empName, String department, String jobTitle,
			String location) {
		Employee emp = new Employee();
		emp.setEmplId(empId1);
		emp.setEmplName(empName);
		emp.setDepartment(department);
		emp.setLocation(location);
		emp.setJobTitle(jobTitle);
		return emp;
	}

	private Employee prepareEmployee(String empId1, int grpId, String grpName, String benProg, long rcd) {
		Employee emp = new Employee();
		emp.setEmplId(empId1);
		emp.setBenefitGroupId(grpId);
		emp.setBenefitGroupName(grpName);
		emp.setBenefitProgram(benProg);
		emp.setUpdatedBenefitProgram(benProg);
		emp.setEmplRcd(rcd);
		return emp;
	}

	private MultiKeyMap prepareStrategyGroupPlanCostMap() {
		MultiKeyMap map = new MultiKeyMap();

		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"1\", \"erContribPercent\":100, \"erRate\":768, \"eeRate\":0}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"2\", \"erContribPercent\":43.2919955, \"erRate\":768, \"eeRate\":1006}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"C\", \"erContribPercent\":49.0421456, \"erRate\":768, \"eeRate\":798}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"4\", \"erContribPercent\":33.01805675, \"erRate\":768, \"eeRate\":1558}" );

		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"1\", \"erContribPercent\":100, \"erRate\":70.65, \"eeRate\":0}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"2\", \"erContribPercent\":48.7712274, \"erRate\":70.65, \"eeRate\":74.21}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"C\", \"erContribPercent\":48.79480628, \"erRate\":70.65, \"eeRate\":74.14}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"4\", \"erContribPercent\":32.25880097, \"erRate\":70.65, \"eeRate\":148.36}" );

		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"1\", \"erContribPercent\":100, \"erRate\":10.04, \"eeRate\":0}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"2\", \"erContribPercent\":50.02491281, \"erRate\":10.04, \"eeRate\":10.03}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"C\", \"erContribPercent\":46.74115457, \"erRate\":10.04, \"eeRate\":11.44}" );
		createPlanCostEntry( map, "{\"strategyId\":172304, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"4\", \"erContribPercent\":29.24555783, \"erRate\":10.04, \"eeRate\":24.29}" );

		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"1\", \"erContribPercent\":100, \"erRate\":857, \"eeRate\":0}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"2\", \"erContribPercent\":43.28282829, \"erRate\":857, \"eeRate\":1123}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"C\", \"erContribPercent\":49.02745996, \"erRate\":857, \"eeRate\":891}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"10\", \"benefitPlan\":\"000OG9\", \"description\":\"BS-CA PPO 300 CA South\", \"coverageLevel\":\"4\", \"erContribPercent\":33.01232666, \"erRate\":857, \"eeRate\":1739}" );

		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"1\", \"erContribPercent\":100, \"erRate\":73.78, \"eeRate\":0}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"2\", \"erContribPercent\":48.77371588, \"erRate\":73.78, \"eeRate\":77.49}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"C\", \"erContribPercent\":48.79952378, \"erRate\":73.78, \"eeRate\":77.41}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"11\", \"benefitPlan\":\"0065PF\", \"description\":\"Aetna Dental 0\", \"coverageLevel\":\"4\", \"erContribPercent\":32.26060342, \"erRate\":73.78, \"eeRate\":154.92}" );

		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"1\", \"erContribPercent\":100, \"erRate\":10.2, \"eeRate\":0}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"2\", \"erContribPercent\":49.97550221, \"erRate\":10.2, \"eeRate\":10.21}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"C\", \"erContribPercent\":46.7246908, \"erRate\":10.2, \"eeRate\":11.63}" );
		createPlanCostEntry( map, "{\"strategyId\":223220, \"groupId\":241114, \"planType\":\"14\", \"benefitPlan\":\"002M2Y\", \"description\":\"VSP Vision Plus\", \"coverageLevel\":\"4\", \"erContribPercent\":29.22636104, \"erRate\":10.2, \"eeRate\":24.7}" );

		return map;
	}

	private void createPlanCostEntry( MultiKeyMap map, String json ) {
		StrategyGroupPlanRateData data = CommonServiceHelper.jsonToObject( json, StrategyGroupPlanRateData.class );
		long strategyId = data.getStrategyId();
		long groupId = data.getGroupId();
		String planType = data.getPlanType();
		String benefitPlan = data.getBenefitPlan();
		String covrgLvl = data.getCoverageLevel();
		map.put( strategyId, groupId, planType, benefitPlan, covrgLvl, data);
	}

	private Map<Long,ModelCompareStrategy> prepareFundingDetailMap() {
		Map<Long,ModelCompareStrategy> map = new HashMap<>();
		createStrategyEntry( map, "{\"id\":172304,\"name\":\"Current Strategy\",\"activeStrategy\":false,\"groupFundingList\":[{\"id\":188157,\"benefitProgram\":\"001VF6\",\"name\":\"Ocean Junior Employees\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"vision\":{\"type\":\"vision\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":\"FLTMAX\",\"waiverAllowance\":0,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":70},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":[{\"id\":\"employee\",\"name\":\"Employee Only\",\"contribution\":238.7},{\"id\":\"employeePlusSpouse\",\"name\":\"Employee + Spouse\",\"contribution\":238.7},{\"id\":\"employeePlusChild\",\"name\":\"Employee + Child(ren)\",\"contribution\":238.7},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":238.7}],\"fundingBasePlanLimits\":null,\"excessOption\":null},\"dental\":{\"type\":\"dental\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}},{\"id\":188156,\"benefitProgram\":\"001VF5\",\"name\":\"Ocean Consultants\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"vision\":{\"type\":\"vision\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":0,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"dental\":{\"type\":\"dental\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}},{\"id\":188154,\"benefitProgram\":\"001P65\",\"name\":\"Ocean Partners\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"vision\":{\"type\":\"vision\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":100},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":100},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":100}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":0,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":100},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":100},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":100}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"dental\":{\"type\":\"dental\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":100},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":100},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":100}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}},{\"id\":188155,\"benefitProgram\":\"001P8F\",\"name\":\"Ocean Temp Staff\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":\"FLTMAX\",\"waiverAllowance\":0,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":70},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":70},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":70},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":70}],\"coverageLevelFundingFlatMax\":[{\"id\":\"employee\",\"name\":\"Employee Only\",\"contribution\":238.7},{\"id\":\"employeePlusSpouse\",\"name\":\"Employee + Spouse\",\"contribution\":238.7},{\"id\":\"employeePlusChild\",\"name\":\"Employee + Child(ren)\",\"contribution\":238.7},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":238.7}],\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}}]}" );
		createStrategyEntry( map, "{\"id\":223220,\"name\":\"Brand New Benefits Strategy\",\"activeStrategy\":false,\"groupFundingList\":[{\"id\":241114,\"benefitProgram\":\"001VF5\",\"name\":\"UNSUBMITTED CHANGE\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"vision\":{\"type\":\"vision\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":0,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"dental\":{\"type\":\"dental\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}},{\"id\":241115,\"benefitProgram\":\"001VF6\",\"name\":\"Ocean Junior Employees\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"vision\":{\"type\":\"vision\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":\"FLTMAX\",\"waiverAllowance\":0,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":70},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":[{\"id\":\"employee\",\"name\":\"Employee Only\",\"contribution\":238.7},{\"id\":\"employeePlusSpouse\",\"name\":\"Employee + Spouse\",\"contribution\":238.7},{\"id\":\"employeePlusChild\",\"name\":\"Employee + Child(ren)\",\"contribution\":238.7},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":238.7}],\"fundingBasePlanLimits\":null,\"excessOption\":null},\"dental\":{\"type\":\"dental\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}},{\"id\":241112,\"benefitProgram\":\"001P65\",\"name\":\"Ocean Partners New Improved\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"vision\":{\"type\":\"vision\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":100},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":100},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":100}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":100},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":100},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":100}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null},\"dental\":{\"type\":\"dental\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":null,\"waiverAllowance\":null,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":100},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":100},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":100},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":100}],\"coverageLevelFundingFlatMax\":null,\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}},{\"id\":241113,\"benefitProgram\":\"001P8F\",\"name\":\"Ocean Temp Staff\",\"waitTime\":\"Date of Hire (DOH)\",\"offerTypeFunding\":{\"medical\":{\"type\":\"medical\",\"offered\":true,\"employeePaid\":false,\"fundingType\":\"CFPCT\",\"benefitPlanDesc\":null,\"baseFundPlan\":\"FLTMAX\",\"waiverAllowance\":0,\"coverageLevels\":[{\"id\":\"employee\",\"name\":\"Employee\",\"contribution\":70},{\"id\":\"employeePlusSpouse\",\"name\":\"Spouse\",\"contribution\":0},{\"id\":\"employeePlusChild\",\"name\":\"Child(ren)\",\"contribution\":0},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":0}],\"coverageLevelFundingFlatMax\":[{\"id\":\"employee\",\"name\":\"Employee Only\",\"contribution\":238.7},{\"id\":\"employeePlusSpouse\",\"name\":\"Employee + Spouse\",\"contribution\":238.7},{\"id\":\"employeePlusChild\",\"name\":\"Employee + Child(ren)\",\"contribution\":238.7},{\"id\":\"employeePlusFamily\",\"name\":\"Family\",\"contribution\":238.7}],\"fundingBasePlanLimits\":null,\"excessOption\":null}},\"hsaFunding\":{\"optionId\":null,\"lumpSumFrequency\":null,\"annualEeAmount\":null,\"annualFamilyAmount\":null,\"annualMonth\":null,\"quarterlyEeAmount\":null,\"quarterlyFamilyAmount\":null,\"contributionFrequency\":null,\"monthlyEeAmount\":null,\"monthlyFamilyAmount\":null,\"quarters\":{}}}]}" );
		return map;
	}

	private void createStrategyEntry( Map<Long,ModelCompareStrategy> map, String json ) {
		ModelCompareStrategy strategy = CommonServiceHelper.jsonToObject( json, ModelCompareStrategy.class );
		map.put( strategy.getId(), strategy );
	}

	private MultiKeyMap prepPlanTypeOfferingMap() {
		MultiKeyMap map = new MultiKeyMap();
		map.put( 172304L, "001P65", "10", "10" );
		map.put( 172304L, "001P65", "11", "11" );
		map.put( 172304L, "001P65", "14", "14" );
		map.put( 172304L, "001P8F", "10", "10" );
		map.put( 172304L, "001P8F", "11", "1D" );
		map.put( 172304L, "001P8F", "14", "1V" );
		map.put( 172304L, "001VF5", "10", "10" );
		map.put( 172304L, "001VF5", "11", "11" );
		map.put( 172304L, "001VF5", "14", "14" );
		map.put( 172304L, "001VF6", "10", "10" );
		map.put( 172304L, "001VF6", "11", "11" );
		map.put( 172304L, "001VF6", "14", "14" );
		map.put( 223220L, "001P65", "10", "10" );
		map.put( 223220L, "001P65", "11", "11" );
		map.put( 223220L, "001P65", "14", "14" );
		map.put( 223220L, "001P8F", "10", "10" );
		map.put( 223220L, "001P8F", "11", "1D" );
		map.put( 223220L, "001P8F", "14", "1V" );
		map.put( 223220L, "001VF5", "10", "10" );
		map.put( 223220L, "001VF5", "11", "11" );
		map.put( 223220L, "001VF5", "14", "14" );
		map.put( 223220L, "001VF6", "10", "10" );
		map.put( 223220L, "001VF6", "11", "11" );
		map.put( 223220L, "001VF6", "14", "14" );
		return map;
	}

	private List<Object[]> prepareStrategyList() {
		
		List<Object[]> strategyResult = new ArrayList<>();
		strategyResult.add( createEmplStrategyEntry( "00001797470", "George", "",  "Marchese",  "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "10", "000OG9", "BS-CA PPO 300 CA South", "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00001797470", "George", "",  "Marchese",  "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "11", "0065PF", "Aetna Dental 0",         "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00001797470", "George", "",  "Marchese",  "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "14", "002M2Y", "VSP Vision Plus",        "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00002223903", "Aime",   "F", "Richey  ",  "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001P65", "10", "000OG9", "BS-CA PPO 300 CA South", "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00002223903", "Aime",   "F", "Richey  ",  "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001P65", "11", "0065PG", "Aetna Dental 50",        "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00002223903", "Aime",   "F", "Richey  ",  "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001P65", "14", "005AJI", "Aetna EyeMed Plus",      "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00002627852", "Amalea", "S", "Busman   ", "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "10", "000OG8", "BS-CA PPO 300 CA North", "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00002627852", "Amalea", "S", "Busman   ", "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "11", "0065PZ", "Delta Dental 0",         "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00002627852", "Amalea", "S", "Busman   ", "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "14", "005AJI", "Aetna EyeMed Plus",      "1", "E" ));
		strategyResult.add( createEmplStrategyEntry( "00010111517", "Glynda", " ", "Gerrish  ", "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "10", "Waive", "",                        "0", "W" ));
		strategyResult.add( createEmplStrategyEntry( "00010111517", "Glynda", " ", "Gerrish  ", "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "11", "Waive", "",                        "0", "W" ));
		strategyResult.add( createEmplStrategyEntry( "00010111517", "Glynda", " ", "Gerrish  ", "9PZ1UADEPT", "Mktg Dept", "W00000225R", "Race Locn", "001VF5", "14", "Waive", "",                        "0", "W" ));
		return strategyResult;
	}

	private Object[] createEmplStrategyEntry( String emplid, String firstName, String middleName, String lastName,
			String deptid, String deptDescr, String location, String locDescr, String benefitProgram, String planType,
			String benefitPlan, String planName, String covrgCd, String coverageElect ) {
		Object[] r = new Object[14];
		r[0] = emplid;
		r[1] = firstName;
		r[2] = middleName;
		r[3] = lastName;
		r[4] = deptid;
		r[5] = deptDescr;
		r[6] = location;
		r[7] = locDescr;
		r[8] = benefitProgram;
		r[9] = planType;
		r[10] = benefitPlan;
		r[11] = planName;
		r[12] = covrgCd;
		r[13] = coverageElect;
		return r;
	}

	private Map<String,Set<PlanCarrier>> preparePlanCarrierMap() {
		Map<String,Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Set<PlanCarrier> set = new HashSet<>();
		createPlanCarrierSetEntry( set, "{\"id\":28,\"name\":\"Kaiser CO\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":[\"1\"],\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":27,\"name\":\"Kaiser GA\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":[\"1\"],\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":25,\"name\":\"Kaiser WA\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":[\"1\"],\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":24,\"name\":\"Kaiser MD\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":[\"1\"],\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":19,\"name\":\"UHC HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":18,\"name\":\"Kaiser HI\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":13,\"name\":\"Tufts\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":[\"1\"],\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":11,\"name\":\"Blue Shield of CA\",\"mandatory\":false,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":[\"1\"],\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":2,\"name\":\"Kaiser\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":1,\"name\":\"Aetna\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		planCarrierMap.put( "medical", set );

		set = new HashSet<>();
		createPlanCarrierSetEntry( set, "{\"id\":16,\"name\":\"Delta\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":14,\"name\":\"Guardian\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":3,\"name\":\"Metlife\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":1,\"name\":\"Aetna\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		planCarrierMap.put( "dental", set );

		set = new HashSet<>();
		createPlanCarrierSetEntry( set, "{\"id\":15,\"name\":\"EyeMed\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		createPlanCarrierSetEntry( set, "{\"id\":6,\"name\":\"VSP\",\"mandatory\":true,\"restricted\":false,\"employeePaid\":false,\"defaultPlan\":null,\"parentId\":null,\"regionalCarriers\":[]}" );
		planCarrierMap.put( "vision", set );

		return planCarrierMap;
	}

	private void createPlanCarrierSetEntry( Set<PlanCarrier> set, String json ) {
		PlanCarrier pc = CommonServiceHelper.jsonToObject( json, PlanCarrier.class );
		set.add( pc );
	}

	private Set<String> prepareOutOfRegionPlans() {
		return new HashSet<>( Arrays.asList( "006RYC","006RYA","0065P2","000OG6","0024CJ","006RYE","003UB3","000OGE","000OGA","0033OT","000OL3","000OGI","006RVH","000OL7","003UI1","0065OF","006RVP","000OLB","006RY4","0033LB","006RVN","006RVL","006RVJ","000OLF","006RY8","0024BZ","006RVR","006RY6" ) );
	}

	private Map<String,PlanMapping> preparePrimaryPlanMappings() {
		Map<String,PlanMapping> map = new HashMap<>();
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0033JO\",\"oldPortfolioId\":1,\"newPortfolioId\":1,\"newBenefitPlan\":\"0033JN\",\"newBenefitPlans\":[\"0033JN\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0024NR\",\"oldPortfolioId\":26,\"newPortfolioId\":13,\"newBenefitPlan\":\"0024O9\",\"newBenefitPlans\":[\"0024O9\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"003UW5\",\"oldPortfolioId\":1,\"newPortfolioId\":1,\"newBenefitPlan\":\"003UW3\",\"newBenefitPlans\":[\"003UW3\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"003UAO\",\"oldPortfolioId\":26,\"newPortfolioId\":13,\"newBenefitPlan\":\"000OGY\",\"newBenefitPlans\":[\"000OGY\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0024CB\",\"oldPortfolioId\":26,\"newPortfolioId\":13,\"newBenefitPlan\":\"000OGZ\",\"newBenefitPlans\":[\"000OGZ\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"003UZK\",\"oldPortfolioId\":1,\"newPortfolioId\":1,\"newBenefitPlan\":\"003UZI\",\"newBenefitPlans\":[\"003UZI\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0033QO\",\"oldPortfolioId\":1,\"newPortfolioId\":1,\"newBenefitPlan\":\"0033QN\",\"newBenefitPlans\":[\"0033QN\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0024NT\",\"oldPortfolioId\":26,\"newPortfolioId\":13,\"newBenefitPlan\":\"005AS6\",\"newBenefitPlans\":[\"005AS6\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0024O6\",\"oldPortfolioId\":26,\"newPortfolioId\":13,\"newBenefitPlan\":\"0014U8\",\"newBenefitPlans\":[\"0014U8\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0024NU\",\"oldPortfolioId\":26,\"newPortfolioId\":13,\"newBenefitPlan\":\"0014U8\",\"newBenefitPlans\":[\"0014U8\"],\"planType\":\"10\"}" );
		createPlanMappingEntry( map, "{\"oldBenefitPlan\":\"0024NV\",\"oldPortfolioId\":26,\"newPortfolioId\":13,\"newBenefitPlan\":\"0014U8\",\"newBenefitPlans\":[\"0014U8\"],\"planType\":\"10\"}" );
		return map;
	}

	private void createPlanMappingEntry( Map<String,PlanMapping> map, String json ) {
		PlanMapping pm = CommonServiceHelper.jsonToObject( json, PlanMapping.class );
		map.put( pm.getOldBenefitPlan(), pm );
	}

	private Map<BenefitPlan,BenefitPlan> prepareErEePlanMapping() {
		Map<BenefitPlan,BenefitPlan> map = new HashMap<>();
		createPlanMapEntry( map, "11", "003V7W", "1D", "003V7Y" );
		createPlanMapEntry( map, "11", "003V80", "1D", "003V82" );
		createPlanMapEntry( map, "11", "0065PX", "1D", "0065LS" );
		createPlanMapEntry( map, "11", "0065Q9", "1D", "0065M4" );
		createPlanMapEntry( map, "11", "0065PY", "1D", "0065LT" );
		createPlanMapEntry( map, "11", "0065PV", "1D", "0065LQ" );
		createPlanMapEntry( map, "11", "0065Q7", "1D", "0065M2" );
		createPlanMapEntry( map, "11", "0065PW", "1D", "0065LR" );
		createPlanMapEntry( map, "11", "0065Q8", "1D", "0065M3" );
		createPlanMapEntry( map, "11", "0065PT", "1D", "0065LO" );
		createPlanMapEntry( map, "11", "0065Q5", "1D", "0065M0" );
		createPlanMapEntry( map, "11", "0065PU", "1D", "0065LP" );
		createPlanMapEntry( map, "11", "0065Q6", "1D", "0065M1" );
		createPlanMapEntry( map, "11", "0065PR", "1D", "0065LM" );
		createPlanMapEntry( map, "11", "0065Q3", "1D", "0065LY" );
		createPlanMapEntry( map, "11", "0065PS", "1D", "0065LN" );
		createPlanMapEntry( map, "11", "0065Q4", "1D", "0065LZ" );
		createPlanMapEntry( map, "11", "0065QA", "1D", "0065M5" );
		createPlanMapEntry( map, "11", "0065QB", "1D", "0065M6" );
		createPlanMapEntry( map, "11", "0065PZ", "1D", "0065LU" );
		createPlanMapEntry( map, "11", "0065QG", "1D", "0065MB" );
		createPlanMapEntry( map, "11", "0065QH", "1D", "0065MC" );
		createPlanMapEntry( map, "11", "0065QE", "1D", "0065M9" );
		createPlanMapEntry( map, "11", "0065QF", "1D", "0065MA" );
		createPlanMapEntry( map, "11", "0065QC", "1D", "0065M7" );
		createPlanMapEntry( map, "11", "0065QD", "1D", "0065M8" );
		createPlanMapEntry( map, "11", "00674L", "1D", "00674N" );
		createPlanMapEntry( map, "11", "0065PH", "1D", "0065LC" );
		createPlanMapEntry( map, "11", "0065PI", "1D", "0065LD" );
		createPlanMapEntry( map, "11", "0065PF", "1D", "0065LA" );
		createPlanMapEntry( map, "11", "0065PG", "1D", "0065LB" );
		createPlanMapEntry( map, "11", "0065PP", "1D", "0065LK" );
		createPlanMapEntry( map, "11", "0065Q1", "1D", "0065LW" );
		createPlanMapEntry( map, "11", "0065PQ", "1D", "0065LL" );
		createPlanMapEntry( map, "11", "0065Q2", "1D", "0065LX" );
		createPlanMapEntry( map, "11", "0065PN", "1D", "0065LI" );
		createPlanMapEntry( map, "11", "0065PO", "1D", "0065LJ" );
		createPlanMapEntry( map, "11", "0065Q0", "1D", "0065LV" );
		createPlanMapEntry( map, "11", "0065PL", "1D", "0065LG" );
		createPlanMapEntry( map, "11", "0065PM", "1D", "0065LH" );
		createPlanMapEntry( map, "11", "0065PJ", "1D", "0065LE" );
		createPlanMapEntry( map, "11", "0065PK", "1D", "0065LF" );
		createPlanMapEntry( map, "11", "0065J2", "1D", "0065MM" );
		createPlanMapEntry( map, "11", "0065J3", "1D", "0065MN" );
		createPlanMapEntry( map, "11", "0065J0", "1D", "0065MK" );
		createPlanMapEntry( map, "11", "0065J1", "1D", "0065ML" );
		createPlanMapEntry( map, "11", "0065IY", "1D", "0065MI" );
		createPlanMapEntry( map, "11", "0065IZ", "1D", "0065MJ" );
		createPlanMapEntry( map, "11", "0065IW", "1D", "0065MG" );
		createPlanMapEntry( map, "11", "0065J8", "1D", "0065MS" );
		createPlanMapEntry( map, "11", "0065IX", "1D", "0065MH" );
		createPlanMapEntry( map, "11", "0065J9", "1D", "0065MT" );
		createPlanMapEntry( map, "11", "0065IU", "1D", "0065ME" );
		createPlanMapEntry( map, "11", "0065J6", "1D", "0065MQ" );
		createPlanMapEntry( map, "11", "0065IV", "1D", "0065MF" );
		createPlanMapEntry( map, "11", "0065J7", "1D", "0065MR" );
		createPlanMapEntry( map, "11", "0065J4", "1D", "0065MO" );
		createPlanMapEntry( map, "11", "0065IT", "1D", "0065MD" );
		createPlanMapEntry( map, "11", "0065J5", "1D", "0065MP" );
		createPlanMapEntry( map, "11", "0065JH", "1D", "0065N1" );
		createPlanMapEntry( map, "11", "0065JF", "1D", "0065MZ" );
		createPlanMapEntry( map, "11", "0065JG", "1D", "0065N0" );
		createPlanMapEntry( map, "11", "0065JD", "1D", "0065MX" );
		createPlanMapEntry( map, "11", "0065JE", "1D", "0065MY" );
		createPlanMapEntry( map, "14", "002M2Y", "1V", "002M3F" );
		createPlanMapEntry( map, "14", "005AJE", "1V", "005AKH" );
		createPlanMapEntry( map, "14", "005AJI", "1V", "005AKL" );
		createPlanMapEntry( map, "14", "002M2W", "1V", "002M3D" );
		return map;
	}

	private void createPlanMapEntry( Map<BenefitPlan,BenefitPlan> map, String planType1, String benefitPlan1,
			String planType2, String benefitPlan2 ) {
		BenefitPlan thing1 = new BenefitPlan();
		BenefitPlan thing2 = new BenefitPlan();
		thing1.setPlanType( planType1 );
		thing1.setId( benefitPlan1 );
		thing2.setPlanType( planType2 );
		thing2.setId( benefitPlan2 );
		map.put( thing1, thing2 );
		map.put( thing2, thing1 );
	}

	private MultiKeyMap prepareEmpBenPlanMappings() {
		MultiKeyMap map = new MultiKeyMap();
		createEmpBenPlanMapping( map, "00001797470", "10", "10", "000OG9", "000OG9", Arrays.asList("001IWL") );
		createEmpBenPlanMapping( map, "00001797470", "11", "11", "0065PF", "0065PF", Arrays.asList("0065LA") );
		createEmpBenPlanMapping( map, "00001797470", "14", "14", "002M2Y", "002M2Y", Arrays.asList("002M3F") );
		createEmpBenPlanMapping( map, "00002223903", "10", "10", "000OG9", "000OG9", Arrays.asList("001IWL") );
		createEmpBenPlanMapping( map, "00002223903", "14", "14", "005AJI", "005AJI", Arrays.asList("005AKL") );
		createEmpBenPlanMapping( map, "00002223903", "11", "11", "0065PG", "0065PG", Arrays.asList("0065LB") );
		createEmpBenPlanMapping( map, "00002627852", "11", "11", "0065PZ", "0065PZ", Arrays.asList("0065LU") );
		createEmpBenPlanMapping( map, "00002627852", "14", "14", "005AJI", "005AJI", Arrays.asList("005AKL") );
		createEmpBenPlanMapping( map, "00002627852", "10", "10", "000OG8", "000OG8", Arrays.asList("001IWE") );
		createEmpBenPlanMapping( map, "00010111517", "10", "10", "Waive",  "",       Arrays.asList("") );
		createEmpBenPlanMapping( map, "00010111517", "11", "11", "Waive",  "",       Arrays.asList("") );
		createEmpBenPlanMapping( map, "00010111517", "14", "14", "Waive",  "",       Arrays.asList("") );
		return map;
	}

	private void createEmpBenPlanMapping( MultiKeyMap map, String emplidKey, String planTypeKey,
			String planType, String curBenPlan, String nextBenPlan, List<String> nextAltBenPlans ) {
		EmpBenPlanMapping e = new EmpBenPlanMapping( planType, curBenPlan, nextBenPlan, nextAltBenPlans, "");
		map.put( emplidKey, planTypeKey, e);
	}

	private List<XbssRealmPlyrPlan> prepareRealmPlyrPlans() {
		List<XbssRealmPlyrPlan> list = new ArrayList<>();
		list.add( createPlyrPlan( "10", "000OG9", 11 ));
		list.add( createPlyrPlan( "10", "001IWL", 5 ));
		list.add( createPlyrPlan( "10", "000OG8", 11 ));
		list.add( createPlyrPlan( "10", "001IWE", 5 ));
		list.add( createPlyrPlan( "11", "0065PF", 1 ));
		list.add( createPlyrPlan( "11", "0065LA", 1 ));
		list.add( createPlyrPlan( "11", "0065PG", 1 ));
		list.add( createPlyrPlan( "11", "0065LB", 1 ));
		list.add( createPlyrPlan( "11", "0065PZ", 16 ));
		list.add( createPlyrPlan( "11", "0065LU", 16 ));
		list.add( createPlyrPlan( "14", "002M2Y", 6 ));
		list.add( createPlyrPlan( "14", "002M3F", 6 ));
		list.add( createPlyrPlan( "14", "005AJI", 15 ));
		list.add( createPlyrPlan( "14", "005AKL", 15 ));
		return list;
	}

	private XbssRealmPlyrPlan createPlyrPlan( String planType, String benefitPlan, int portfolioId ) {
		XbssRealmPlyrPlan plan = new XbssRealmPlyrPlan();
		plan.setPlanType( planType );
		plan.setBenefitPlan( benefitPlan );
		plan.setPortfolioId( new BigDecimal( portfolioId ) );
		return plan;
	}

	private MultiKeyMap prepareEmplBenGroupChangeMap() {
		MultiKeyMap map = new MultiKeyMap();
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setId( 241112L );
		benefitGroup.setName( "Ocean Partners New Improved" );
		benefitGroup.setBenefitProgram( "001P65" );
		map.put( "00002627852", 223220L, benefitGroup );
		return map;
	}

	private Map<String,EmployeeBenefitGroup> prepareBenefitGroupMap() {
		Map<String,EmployeeBenefitGroup> map = new HashMap<>();
		createBenefitGroupEntry( map, 241114, "UNSUBMITTED CHANGE", "AB01", "001VF5" );
		createBenefitGroupEntry( map, 241112, "Ocean Partners New Improved", "AB02", "001P65" );
		return map;
	}

	private void createBenefitGroupEntry( Map<String,EmployeeBenefitGroup> map, long benefitGroupId,
			String benefitGroupName, String eligConfig1, String benProg ) {
		EmployeeBenefitGroup bg = new EmployeeBenefitGroup();
		bg.setBenefitGroupId(benefitGroupId);
		bg.setBenefitGroupName(benefitGroupName);
		bg.setEligConfig1(eligConfig1);
		bg.setBenefitProgram(benProg);
		map.put( bg.getBenefitProgram(), bg );
	}
	
	private Employee prepareEmployee(String empId, String groupName, int groupId, long strategyGroupId) {
		Employee employee = new Employee();
		employee.setEmplId(empId);
		employee.setBenefitGroupName(groupName);
		employee.setBenefitGroupId(groupId);
		employee.setStrategyGroupId(strategyGroupId);
		return employee;
	}

	private EmployeeData prepareEmployeeData(String empId, String empName, String groupName, int groupId,
			long strategyId) {
		EmployeeData employee = new EmployeeData();
		employee.setEmplId(empId);
		employee.setEmplName(empName);
		employee.setBenefitGroupName(groupName);
		employee.setBenefitGroupId(groupId);
		employee.setStrategyGroupId(strategyId);
		return employee;
	}

	private EmployeeData prepareEmployeeData1(String empId, String empName, String groupName, int groupId) {
		EmployeeData emp = new EmployeeData();
		emp.setEmplId(empId);
		emp.setBenefitGroupId(groupId);
		emp.setBenefitGroupName(groupName);
		emp.setEmplName(empName);
		emp.setStrategyGroupId(1);
		return emp;
	}

	private List<CensusRes> prepareCensusResponse() {
		return List.of(
				CensusRes.builder().employeeId(EMP_ID_1).employeeName("John Doe").age(22).homeState("CA").gender("M")
						.k1(true).annualWages(4000.0).homePostalCode("28262").medicalTier("EE").dentalTier("EC")
						.visionTier("EF").build(),
				CensusRes.builder().employeeId(EMP_ID_2).employeeName("Katty Scott").age(29).homeState("TX").gender("F")
						.k1(false).annualWages(4500.0).homePostalCode("07305").medicalTier("EF").dentalTier("EE")
						.visionTier("WE").build(),
				CensusRes.builder().employeeId(EMP_ID_3).employeeName("John S").age(29).homeState("TX").gender("F")
						.k1(false).annualWages(4500.0).homePostalCode("07305").medicalTier("EF").dentalTier("EE")
						.visionTier("WE").build());
	}

	private EmployeeAssignmentData prepareEmployeeAssignmentData() {
		EmployeeAssignmentData employeeAssignmentData = new EmployeeAssignmentData();
		List<EmployeeSourceData> employeesList = new ArrayList<>();
		EmployeeSourceData esd = new EmployeeSourceData();
		esd.setEmployees(Arrays.asList("00001113666", "00001415472"));
		esd.setSourceStrategyGroupId(1111L);
		employeesList.add(esd);
		esd = new EmployeeSourceData();
		esd.setEmployees(Arrays.asList("00001520518", "00001523259"));
		esd.setSourceStrategyGroupId(2222L);
		employeesList.add(esd);
		employeeAssignmentData.setEmployeesList(employeesList);
		employeeAssignmentData.setDestinationStrategyGroupId(3333L);
		return employeeAssignmentData;
	}

    private Employee prepareNewClientEmployee(String empId1, String empName, String department, String jobTitle,
                                     String location, boolean k1) {
        Employee emp = new Employee();
        emp.setEmplId(empId1);
        emp.setEmplName(empName);
        emp.setDepartment(department);
        emp.setLocation(location);
        emp.setJobTitle(jobTitle);
        emp.setK1(k1);
        return emp;
    }
	@Test
	public void testEmployeeDataSync_ProspectCompany_NoProcessing() {
		// Arrange
		Company company = new Company();
		company.setProspectCompany(true);

		// Act
		employeeDataService.employeeDataSync(company);

		// Assert - verify no DAO methods are called
		verify(employeeDataDao, times(0)).getEmployeesByCompany(any());
		verify(employeeBenefitGroupDao, times(0)).deleteEmployeeStrategyGroups(any(Company.class));
		verify(employeeBenefitGroupService, times(0)).insertNewEmployeeStrategyGroups(any());
	}

	@Test(expected = com.trinet.ambis.exception.BSSApplicationException.class)
	public void testEmployeeDataSync_InactiveBenefitProgram_ThrowsException() {
		// Arrange
		Company company = new Company();
		company.setProspectCompany(false);
		company.setCode("G48");
		company.setId(12345L);

		// Create employee with benefit program
		Employee employee = new Employee();
		employee.setEmplId(EMP_ID_1);
		employee.setBenefitProgram("INACTIVE_PROGRAM");

		Map<String, Employee> psEmployees = new HashMap<>();
		psEmployees.put(EMP_ID_1, employee);

		// Create empty strategy groups map (inactive/non-existent benefit program)
		Map<String, Set<com.trinet.ambis.service.model.StrategyGroupDetails>> benefitProgramStrategyGroups = new HashMap<>();

		when(employeeDataDao.getEmployeesByCompany(company)).thenReturn(psEmployees);
		doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(company);
		when(employeeBenefitGroupDao.getStrategyGroupDetailsForCompany(company))
				.thenReturn(benefitProgramStrategyGroups);

		// Act - should throw BSSApplicationException
		employeeDataService.employeeDataSync(company);
	}

	@Test(expected = com.trinet.ambis.exception.BSSApplicationException.class)
	public void testEmployeeDataSync_NullStrategyGroups_ThrowsException() {
		// Arrange
		Company company = new Company();
		company.setProspectCompany(false);
		company.setCode("G48");
		company.setId(12345L);

		// Create employee with benefit program
		Employee employee = new Employee();
		employee.setEmplId(EMP_ID_1);
		employee.setBenefitProgram(BEN_PROG_1);

		Map<String, Employee> psEmployees = new HashMap<>();
		psEmployees.put(EMP_ID_1, employee);

		// Create strategy groups map with null value for benefit program
		Map<String, Set<com.trinet.ambis.service.model.StrategyGroupDetails>> benefitProgramStrategyGroups = new HashMap<>();
		benefitProgramStrategyGroups.put(BEN_PROG_1, null);

		when(employeeDataDao.getEmployeesByCompany(company)).thenReturn(psEmployees);
		doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(company);
		when(employeeBenefitGroupDao.getStrategyGroupDetailsForCompany(company))
				.thenReturn(benefitProgramStrategyGroups);

		// Act - should throw BSSApplicationException
		employeeDataService.employeeDataSync(company);
	}

	@Test(expected = com.trinet.ambis.exception.BSSApplicationException.class)
	public void testEmployeeDataSync_EmptyStrategyGroupsSet_ThrowsException() {
		// Arrange
		Company company = new Company();
		company.setProspectCompany(false);
		company.setCode("G48");
		company.setId(12345L);

		// Create employee with benefit program
		Employee employee = new Employee();
		employee.setEmplId(EMP_ID_2);
		employee.setBenefitProgram(BEN_PROG_2);

		Map<String, Employee> psEmployees = new HashMap<>();
		psEmployees.put(EMP_ID_2, employee);

		// Create strategy groups map with empty set for benefit program
		Map<String, Set<com.trinet.ambis.service.model.StrategyGroupDetails>> benefitProgramStrategyGroups = new HashMap<>();
		benefitProgramStrategyGroups.put(BEN_PROG_2, new HashSet<>());

		when(employeeDataDao.getEmployeesByCompany(company)).thenReturn(psEmployees);
		doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(company);
		when(employeeBenefitGroupDao.getStrategyGroupDetailsForCompany(company))
				.thenReturn(benefitProgramStrategyGroups);

		// Act - should throw BSSApplicationException
		employeeDataService.employeeDataSync(company);
	}

	@Test
	public void testEmployeeDataSync_ValidData_Success() {
		// Arrange
		Company company = new Company();
		company.setProspectCompany(false);
		company.setCode("G48");
		company.setId(12345L);

		// Create employees with benefit programs
		Employee employee1 = new Employee();
		employee1.setEmplId(EMP_ID_1);
		employee1.setBenefitProgram(BEN_PROG_1);

		Employee employee2 = new Employee();
		employee2.setEmplId(EMP_ID_2);
		employee2.setBenefitProgram(BEN_PROG_2);

		Map<String, Employee> psEmployees = new HashMap<>();
		psEmployees.put(EMP_ID_1, employee1);
		psEmployees.put(EMP_ID_2, employee2);

		// Create strategy groups with valid data
		com.trinet.ambis.service.model.StrategyGroupDetails strategyGroupDetails1 = new com.trinet.ambis.service.model.StrategyGroupDetails();
		strategyGroupDetails1.setStrategyGroupId(1111L);
		strategyGroupDetails1.setBenefitProgram(BEN_PROG_1);
		strategyGroupDetails1.setEligConfig1("EligConfig1");

		Set<com.trinet.ambis.service.model.StrategyGroupDetails> strategyGroups1 = new HashSet<>();
		strategyGroups1.add(strategyGroupDetails1);

		com.trinet.ambis.service.model.StrategyGroupDetails strategyGroupDetails2 = new com.trinet.ambis.service.model.StrategyGroupDetails();
		strategyGroupDetails2.setStrategyGroupId(2222L);
		strategyGroupDetails2.setBenefitProgram(BEN_PROG_2);
		strategyGroupDetails2.setEligConfig1("EligConfig2");

		Set<com.trinet.ambis.service.model.StrategyGroupDetails> strategyGroups2 = new HashSet<>();
		strategyGroups2.add(strategyGroupDetails2);

		Map<String, Set<com.trinet.ambis.service.model.StrategyGroupDetails>> benefitProgramStrategyGroups = new HashMap<>();
		benefitProgramStrategyGroups.put(BEN_PROG_1, strategyGroups1);
		benefitProgramStrategyGroups.put(BEN_PROG_2, strategyGroups2);

		when(employeeDataDao.getEmployeesByCompany(company)).thenReturn(psEmployees);
		doNothing().when(employeeBenefitGroupDao).deleteEmployeeStrategyGroups(company);
		when(employeeBenefitGroupDao.getStrategyGroupDetailsForCompany(company))
				.thenReturn(benefitProgramStrategyGroups);
		doNothing().when(employeeBenefitGroupService).insertNewEmployeeStrategyGroups(any());

		// Act
		employeeDataService.employeeDataSync(company);

		// Assert
		verify(employeeDataDao, times(1)).getEmployeesByCompany(company);
		verify(employeeBenefitGroupDao, times(1)).deleteEmployeeStrategyGroups(company);
		verify(employeeBenefitGroupDao, times(1)).getStrategyGroupDetailsForCompany(company);

		ArgumentCaptor<Map<String, Set<com.trinet.ambis.service.model.StrategyGroupDetails>>> mapCaptor = ArgumentCaptor
				.forClass(Map.class);
		verify(employeeBenefitGroupService, times(1)).insertNewEmployeeStrategyGroups(mapCaptor.capture());

		Map<String, Set<com.trinet.ambis.service.model.StrategyGroupDetails>> capturedMap = mapCaptor.getValue();
		assertEquals(2, capturedMap.size());
		assertTrue(capturedMap.containsKey(EMP_ID_1));
		assertTrue(capturedMap.containsKey(EMP_ID_2));
		assertEquals("EligConfig1", employee1.getEligConfig1());
		assertEquals("EligConfig2", employee2.getEligConfig1());
	}
}