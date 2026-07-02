package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanMappingId;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.impl.EmployeeDataServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRateData;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.EmployeeStrategyPlanData;
import com.trinet.ambis.service.model.GroupFunding;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.StrategyGroupPlanRateData;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author clamorie
 */

@RunWith(MockitoJUnitRunner.class)
public class EmployeeDataServiceImplNewStrategiesTest extends ServiceUnitTest {

	@InjectMocks
	EmployeeDataServiceImpl employeeDataServiceImpl;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	RealmPlanYearRuleService realmPlanYearRuleService;

	@Mock
	PlanMappingDao planMappingDao;
	
	@Mock
	PortfolioRuleDao portfolioRuleDao;

	@Mock
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;

	@Mock
	StrategyGroupDataDao strategyGroupDataDao;
	
	@Mock
	EmployeeDataDao employeeDataDao;

	private static Long realmYearId = 9L;
	private static String headQuatersState = "CA";
	private static String zipCode = "00000";
	private static String exclMedPlan = "DFLT";
	private static String planStartDate = "01-APR-2020";
	private static final Long CURRENT_STRATEGY_ID = 6789L;
	private static final String EMPL_ID = "0000001";
	static List<Long> allStrategiesList = new ArrayList<>(Arrays.asList(CURRENT_STRATEGY_ID, 12345L, 54321L));
	static List<Long> futureStrategyList = new ArrayList<>(Arrays.asList(12345L, 54321L));
	static List<Long> currentStrategyList = new ArrayList<>(Arrays.asList(CURRENT_STRATEGY_ID));
	static List<Long> singleFutureStrategyList = new ArrayList<>(Arrays.asList(12345L));
	List<EmployeeStrategyData> empStrategyDataList = new ArrayList<>();
	MultiKeyMap strategyGroupPlantypeMap = new MultiKeyMap();
	MultiKeyMap strategyGroupPlanCostMap = new MultiKeyMap();
	MultiKeyMap strategyGroupPlanMapping = new MultiKeyMap();
	MultiKeyMap emplStrategyBenGroupMap = new MultiKeyMap();
	Map<String, PlanMapping> realmPlanMapping = new HashMap<>();
	Map<BenefitPlan, BenefitPlan> erEePlanMapping = new HashMap<>();
	Map<String, Map<String, String>> strategyPortfolioDefaultPlans = new HashMap<>();

    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMockedStatic;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @After
    public void tearDown() {
        benefitCategoriesHelperMockedStatic.close();
        commonServiceHelperMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Before
	public void setUp() {
        benefitCategoriesHelperMockedStatic = Mockito.mockStatic(BenefitCategoriesHelper.class);
        commonServiceHelperMockedStatic = Mockito.mockStatic(CommonServiceHelper.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
		Company company = prepareCompany();
		
		// setup a required mapping table
		setupStrategyPortfolioDefaultPlans();
		Mockito.when(realmDataDao.getPortfilioDefaultPlans(realmYearId.longValue()))
				.thenReturn(strategyPortfolioDefaultPlans);

		// setup a required mapping table
		setupHVRealmPlanMapping();
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
		Mockito.when(realmDataDao
		.getPortfilioDefaultPlans(realmYearId)).thenReturn(defaultPlanMap);

		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Mockito.when(portfolioRuleDao.getPortfoliosByHqRegion(
				realmYearId, headQuatersState, zipCode,
				exclMedPlan, planStartDate, false)).thenReturn(planCarrierMap);
		
		Set<String> primaryPlanCarriers = new HashSet<>();
		
		when(BenefitCategoriesHelper.getPlanCarriers(planCarrierMap)).thenReturn(primaryPlanCarriers);
		
		Set<String> outOfRegionPlans = new HashSet<>();
				
		when(CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao)).thenReturn(outOfRegionPlans);
		
		// setup a required mapping table
		setupErEEPlanMapping();
		Mockito.when(
				employerEmployeePlansMappingDao.getEmployerEmployeePlansMappingByRealmYearId(realmYearId.longValue()))
				.thenReturn(erEePlanMapping);
		
		Long groupId = 12345L;
		Long strategyId = 12345L;
		
		// need a not empty map
		strategyGroupPlanCostMap.put(strategyId, groupId, "10", "xxx", "C");

		// stubbing for find-pick-choose flag call
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( eq( company )) ).thenReturn( false );
	}

	/****************
	 * Testing setupEmplNewStrategies ***************** This procedure takes
	 * employee plans setup by getEmployeeStrategyPlanCostData which are the
	 * plans the employee has currently elected We are testing that each of the
	 * above plans will have a new plan for every new client strategy
	 **/

	@Test
	public void setupEmplNewStrategiesAndBenGroupTest() {
		Company company = prepareCompany();
		/*
		 * * GIVEN 1. An employee with only medical coverage 2. A client the
		 * offers medical ONLY 3. There are 2 strategies to be added which are
		 * in strategyList (used as a parameter below) 4. There are No new
		 * benefit groups for any employee and strategy
		 * 
		 * Testing the employee will get 2 additional strategies Also testing
		 * that the benefit group id is the employees original benefit group
		 * 
		 */
		
		// no employees are moving to a new benefit group for this test so
		// clear that map
		emplStrategyBenGroupMap.clear();
		
		// setup map so most of the logic in getMappedStrategyPlan
		// is not called
		setupStrategyGroupPlanCostMap(futureStrategyList); 

		// setup client offering medical only
		String[] planTypes = { "10" };
		setupClientStrategyBenGroupMap(currentStrategyList, planTypes); 

		// sets up employee with only health coverage
		String[] employeeIds = { EMPL_ID };
		setupHealthOnlyCoverageEmployees(employeeIds, currentStrategyList); 

		MultiKeyMap fundingTypeData = new MultiKeyMap();
		MultiKeyMap waiverAllowanceData = new MultiKeyMap();
		Map<Long,Map<String,GroupFunding>> strategyGroupMap = new HashMap<>();

		// when
		employeeDataServiceImpl.setupEmplNewStrategies(company, futureStrategyList,
				emplStrategyBenGroupMap, empStrategyDataList, strategyGroupPlanCostMap, strategyGroupPlantypeMap, fundingTypeData, waiverAllowanceData, strategyGroupMap);

		// then
		// get the employee
		EmployeeStrategyData anEmpl = empStrategyDataList.get(0); 

		// our one employee should have 3 strategies the original
		// and the 2 new ones
		assertEquals(3, anEmpl.getStrategyDetails().size()); 
		// 1st new strategy should be 12345
		assertEquals(12345L, anEmpl.getStrategyDetails().get(1).getStrategyId().longValue()); 
		// 2nd new strategy should be 54321
		assertEquals(54321L, anEmpl.getStrategyDetails().get(2).getStrategyId().longValue()); 
		// strategy should have the one medical plan
		assertEquals(1, anEmpl.getStrategyDetails().get(1).getBenefitPlans().size()); 
		assertFalse(anEmpl.getStrategyDetails().get(1).getBenefitPlans().get(0).getPlanName().isEmpty());

		/*
		 * * GIVEN 1. An employee with only medical coverage 2. A client the
		 * offers medical ONLY 3. There are two strategies to be added in
		 * futureStrategyList (used as a parameter below) 4. There is one new
		 * benefit group
		 * 
		 * Testing the employee will get 2 additional strategies Also testing
		 * that the benefit group id is the new one from the map
		 * 
		 */

		// reset strategy list with employee with only health coverage
		setupHealthOnlyCoverageEmployees(employeeIds, currentStrategyList); 

		emplStrategyBenGroupMap.clear();
		// adds employee to new benefit group for new strategies
		setupEmplStrategyBenGroup(); 

		// when
		employeeDataServiceImpl.setupEmplNewStrategies(company, futureStrategyList,
				emplStrategyBenGroupMap, empStrategyDataList, strategyGroupPlanCostMap, strategyGroupPlantypeMap, fundingTypeData, waiverAllowanceData, strategyGroupMap);

		// then
		anEmpl = empStrategyDataList.get(0);
		// our one employee should have 3 strategies the original and the 2 new ones
		assertEquals(3, anEmpl.getStrategyDetails().size()); 
		// 2nd strategy should a new benefit group id of 32058
		assertEquals(30258, anEmpl.getStrategyDetails().get(1).getGroupId().longValue()); 
		// strategy should have the one medical plan
		assertEquals(1, anEmpl.getStrategyDetails().get(1).getBenefitPlans().size()); 
		assertFalse(anEmpl.getStrategyDetails().get(1).getBenefitPlans().get(0).getPlanName().isEmpty());
	}

	@Test
	public void emplNewStrategiesVerifyGetMappedStrategyPlanSetupTest() {
		Company company = prepareCompany();
		/*
		 * GIVEN 1. An employee with medical dental and vision coverage 2. A
		 * client that offers medical and dental and vision 3. There are two
		 * strategies to be added in futureStrategyList (used as a parameter
		 * below)
		 * 
		 * Testing that the procedure getMappedStrategyPlan sets up 2 strategies
		 * Also check that 3 plans were added to each new strategy
		 * 
		 */

		String[] employeeIds = { EMPL_ID };

		setupStrategyGroupPlanNoPlanMapping();

		emplStrategyBenGroupMap.clear();
		String[] planTypes2 = { "10", "11", "14" };
		// setup client offering medical and dental and vision for all strategies
		setupClientStrategyBenGroupMap(allStrategiesList, planTypes2); 

		// setup employee with medical, dental and vision plans
		setupMDVCoverageEmployees(employeeIds, currentStrategyList); 
		
		// setup so that minimal logic in getMappedStrategyPlan is invoked
		setupStrategyGroupPlanCostMap(futureStrategyList);
		
		MultiKeyMap fundingTypeData = new MultiKeyMap();
		MultiKeyMap waiverAllowanceData = new MultiKeyMap();
		Map<Long,Map<String,GroupFunding>> strategyGroupMap = new HashMap<>();

		// when
		employeeDataServiceImpl.setupEmplNewStrategies(company, futureStrategyList,
				emplStrategyBenGroupMap, empStrategyDataList, strategyGroupPlanCostMap, strategyGroupPlantypeMap, fundingTypeData, waiverAllowanceData, strategyGroupMap);
		
		// employee has 3 strategies original and 2 additional
		assertEquals(3, empStrategyDataList.get(0).getStrategyDetails().size()); 
		
		// 1st employee 2nd strategy -  3 plans per strategy
		EmployeeStrategyPlanData empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(1); 
		assertEquals(3, empStrategy.getBenefitPlans().size());
		
		// 1st employee 3rd strategy -  3 plans per strategy
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(2); 
		assertEquals(3, empStrategy.getBenefitPlans().size()); 

		/*
		 * GIVEN 1. An employee No coverage 2. A client that offers medical and
		 * dental and vision 3. There are two strategies to be added in
		 * futureStrategyList (used as a parameter below)
		 * 
		 * Testing that the procedure getMappedStrategyPlan sets up 2 strategies
		 * Also check that no plans were added to each new strategy
		 * 
		 */
		
		// setup Eligible employee with no plans
		setupNoCoverageEmployees(employeeIds, currentStrategyList); 
		employeeDataServiceImpl.setupEmplNewStrategies(company, futureStrategyList,
				emplStrategyBenGroupMap, empStrategyDataList, strategyGroupPlanCostMap, strategyGroupPlantypeMap, fundingTypeData, waiverAllowanceData, strategyGroupMap);

		// then
		// employee has 3 strategies original and 2 additional
		assertEquals(3, empStrategyDataList.get(0).getStrategyDetails().size()); 

		// 1st employee 2nd strategy - employee has one waived plan
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(1); 
		assertEquals(1, empStrategy.getBenefitPlans().size());
		
		// 1st employee 3rd strategy - employee has one waived plan
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(2); 
		assertEquals(1, empStrategy.getBenefitPlans().size());
	}

	@Test
	public void emplNewStrategiesVerifyGetMappedStrategyPlanCallNullRtnTest() {
		Company company = prepareCompany();
		
		/*
		 * * GIVEN 1. An employee with Medical dental and vision coverage 2. A
		 * client the offers medical and dental ONLY in new strategy 3. There is
		 * one strategy to be added in futureStrategyList (used as a parameter
		 * below) 4. we are forcing mapping routine to returns nulls as part
		 * this test
		 * 
		 * Two plans should have been added to the new strategy medical and
		 * dental Placeholder plans should be setup since client offers medical
		 * dental Plans should have an "Unknown" mapping value
		 */

		EmployeeStrategyPlanData empStrategy;
		
		// make a strategyPlanCostMap with no strategies
		setupStrategyGroupPlanNoPlanMapping(); 

		// setup client offering medical and dental only
		strategyGroupPlantypeMap.clear();
		String[] planTypes2 = { "10", "11" };
		setupClientStrategyBenGroupMap(futureStrategyList, planTypes2); 
		// setup employee with Medical, Dental and Vision
		String[] employeeIds = { EMPL_ID };
		this.setupMDVCoverageEmployees(employeeIds, currentStrategyList); 
		
		MultiKeyMap fundingTypeData = new MultiKeyMap();
		MultiKeyMap waiverAllowanceData = new MultiKeyMap();
		Map<Long,Map<String,GroupFunding>> strategyGroupMap = new HashMap<>();

		// will force the mapping routine to return nulls
		strategyGroupPlanCostMap = null; 

		// when
		employeeDataServiceImpl.setupEmplNewStrategies(company, futureStrategyList,
				emplStrategyBenGroupMap, empStrategyDataList, strategyGroupPlanCostMap, strategyGroupPlantypeMap, fundingTypeData, waiverAllowanceData, strategyGroupMap);

		// then
		// 1st employee 1st strategy
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(0); 
		// employee's original strategy setup above should still have 3 plans
		// setup before the test
		assertEquals(3, empStrategy.getBenefitPlans().size());
		
		// 1st employee 2nd strategy
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(1); 
		// the added strategy should have 2 p1ans medical and dental but no
		// vision since vision no longer offered
		assertEquals(2, empStrategy.getBenefitPlans().size());
		// 1st plan should be medical
		assertEquals("10", empStrategy.getBenefitPlans().get(0).getPlanType()); 
		// elected coverage
		assertEquals("E", empStrategy.getBenefitPlans().get(0).getCoverageElect()); 
		// Unknown mapping
		assertEquals("Unknown", empStrategy.getBenefitPlans().get(0).getMappingFlag()); 
		// 2nd plan should be dental
		assertEquals("11", empStrategy.getBenefitPlans().get(1).getPlanType()); 
		// elected coverage
		assertEquals("E", empStrategy.getBenefitPlans().get(1).getCoverageElect()); 
		// Unknown mapping
		assertEquals("Unknown", empStrategy.getBenefitPlans().get(1).getMappingFlag()); 

		this.setupHealthOnlyCoverageEmployees(employeeIds, currentStrategyList);
		
		// setup client offering dental and vision only
		strategyGroupPlantypeMap.clear();
		String[] planTypes3 = { "11", "14" };
		setupClientStrategyBenGroupMap(futureStrategyList, planTypes3); 

		// when
		employeeDataServiceImpl.setupEmplNewStrategies(company, futureStrategyList,
				emplStrategyBenGroupMap, empStrategyDataList, strategyGroupPlanCostMap, strategyGroupPlantypeMap, fundingTypeData, waiverAllowanceData, strategyGroupMap);

		// then
		// 1st employee 1st strategy
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(0); 
		
		// employee's original strategy setup above should still have 1 plan
		// setup before test
		assertEquals(1, empStrategy.getBenefitPlans().size()); 
		// 1st employee 2nd strategy
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(1); 
		// the added strategy should no plans because the client no
		// longer offers medical
		assertEquals(0, empStrategy.getBenefitPlans().size()); 
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode( "6PR" );
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( realmYearId );
		company.setRealmPlanYearId( rpy.getId() );
		company.setRealmPlanYear( rpy );
		company.setHeadQuatersState(headQuatersState);
		company.setZipCode(zipCode);
		company.setExclusiveMedPlan( exclMedPlan );
		company.setPlanStartDate(planStartDate);
		company.setRenewalCompany(true);
		return company;
	}

	private void setupNoCoverageEmployees(String[] empIdArr, List<Long> strategyList) {
		// Basic employee data and a set of strategies which will have
		// plan and cost data
		EmployeeStrategyData empStrategyData; 
		// plan data for one strategy
		EmployeeStrategyPlanData empStrategyPlanData; 
		BenefitPlanRateData empPlanAndRates;

		empStrategyDataList.clear(); // remove all employees from data list
		for (int i = 0; i < empIdArr.length; i++) {
			// create object that holds all employee/strategy/plan data
			empStrategyData = new EmployeeStrategyData();
			empStrategyData.setEmplId(empIdArr[i]);
			empStrategyData.setEmplFirstName("John");
			empStrategyData.setEmplLastName("Smith" + empIdArr[i]);
			empStrategyData.setDeptId("9O12300");
			empStrategyData.setDeptName("Plumbers");
			empStrategyData.setLocationId("0000002X8N");
			empStrategyData.setLocationName("Plumbing & Pipe Technologie-HQ");
			
			// add this employee to the employee data set
			empStrategyDataList.add(empStrategyData); 

			for (Long strategy : strategyList) {
				// setup strategies in the strategy list NOTE: this employee
				// will have no plan data
				empStrategyPlanData = new EmployeeStrategyPlanData();
				empStrategyPlanData.setStrategyId(strategy);
				empStrategyPlanData.setGroupId(12345L);
				empStrategyPlanData.setGroupName("123435 Grp");
				empStrategyPlanData.setBenefitProgram("001LEN");
				// add to the map of the strategies for this employee
				empStrategyData.getStrategyDetails().add(empStrategyPlanData);
				empPlanAndRates = new BenefitPlanRateData("10", BigDecimal.ZERO, BigDecimal.valueOf(1000), "W", null, false, true);
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRates); 
			}
		}
	}

	private void setupHealthOnlyCoverageEmployees(String[] empIdArr, List<Long> strategyList) {
		setupNoCoverageEmployees(empIdArr, strategyList);
		BenefitPlanRateData empPlanAndRates;
		for (int i = 0; i < empIdArr.length; i++) {
			empPlanAndRates = new BenefitPlanRateData("002AOP", "10", "Aetna HNO 35 4 North NC", "1", CoverageCodesEnums.COV_EMPLOYEE.getName(),
					BigDecimal.valueOf(42.07), BigDecimal.valueOf(168.3), 1L, "E", null, false, true);
			EmployeeStrategyData empStrategyData = empStrategyDataList.get(i);
			for (EmployeeStrategyPlanData empStrategyPlanData : empStrategyData.getStrategyDetails()) {
				// add health plan to every strategy
				empStrategyPlanData.getBenefitPlans().clear();
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRates); 
			}
		}
	}

	private void setupMDVCoverageEmployees(String[] empIdArr, List<Long> strategyList) {
		setupNoCoverageEmployees(empIdArr, strategyList);
		EmployeeStrategyData empStrategyData;
		BenefitPlanRateData empPlanAndRatesHlth = new BenefitPlanRateData("002AOP", "10", "Aetna HNO 35 4 North NC",
				"1", CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(42.07), BigDecimal.valueOf(168.3), 1L, "E", null, false, true);
		BenefitPlanRateData empPlanAndRatesDental = new BenefitPlanRateData("002AD4", "11",
				"Guardian Dental EPO Optional", "1", CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(17.07), BigDecimal.valueOf(0), 1L, "E",
				null, false, true);
		BenefitPlanRateData empPlanAndRatesVision = new BenefitPlanRateData("002ACQ", "14", "EyeMed Optional Vision",
				"1", CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(5.89), BigDecimal.valueOf(0), 1L, "E", null, false, true);

		for (int i = 0; i < empIdArr.length; i++) {
			empStrategyData = empStrategyDataList.get(i);
			for (EmployeeStrategyPlanData empStrategyPlanData : empStrategyData.getStrategyDetails()) {
				empStrategyPlanData.getBenefitPlans().clear();
				// add health plan to strategy
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesHlth); 
				// add dental plan to strategy
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesDental); 
				// add vision plan to strategy
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesVision); 
			}
		}
	}

	private void setupStrategyGroupPlanCostMap(List<Long> strategyList) {
		strategyGroupPlanMapping.clear();
		Long groupId = 12345L;
		Long groupId2 = 30258L;
		StrategyGroupPlanRateData strategyGroupPlanRateData;
		// Array of strings for plan, planType, and mapReason
		String[] planType = { "10", "11", "14" };
		String[] benefitPlan = { "002AOP", "002AD4", "002ACQ" };

		for (Long strategyId : strategyList) {
			for (int i = 0; i < 3; i++) {
				strategyGroupPlanRateData = new StrategyGroupPlanRateData();
				strategyGroupPlanRateData.setStrategyId(strategyId);
				strategyGroupPlanRateData.setGroupId(groupId);
				strategyGroupPlanRateData.setPlanType(planType[i]);
				strategyGroupPlanRateData.setBenefitPlan(benefitPlan[i]);
				strategyGroupPlanRateData.setCoverageLevel("1");
				strategyGroupPlanRateData.setErContribPercent(BigDecimal.valueOf(80.00));
				strategyGroupPlanRateData.setErRate(BigDecimal.valueOf(168.3));
				strategyGroupPlanRateData.setEeRate(BigDecimal.valueOf(42.07));
				strategyGroupPlanRateData.setDescription("description");
				strategyGroupPlanCostMap.put(strategyId, groupId, planType[i], benefitPlan[i], "1",
						strategyGroupPlanRateData);
				strategyGroupPlanCostMap.put(strategyId, groupId2, planType[i], benefitPlan[i], "1",
						strategyGroupPlanRateData);
			}
		}
	}

	private void setupStrategyGroupPlanNoPlanMapping() {
		strategyGroupPlanCostMap.clear();
		Long groupId2 = 30258L;
		Long strategyId = 0L;
		StrategyGroupPlanRateData strategyGroupPlanRateData;

		strategyGroupPlanRateData = new StrategyGroupPlanRateData();
		strategyGroupPlanRateData.setStrategyId(strategyId);
		strategyGroupPlanRateData.setGroupId(groupId2);
		strategyGroupPlanRateData.setPlanType("10");
		strategyGroupPlanRateData.setBenefitPlan("XX");
		strategyGroupPlanRateData.setCoverageLevel("1");
		strategyGroupPlanRateData.setErContribPercent(BigDecimal.valueOf(80.00));
		strategyGroupPlanRateData.setErRate(BigDecimal.valueOf(168.3));
		strategyGroupPlanRateData.setEeRate(BigDecimal.valueOf(42.07));
		strategyGroupPlanRateData.setDescription("description");
		strategyGroupPlanCostMap.put(strategyId, groupId2, "10", "XX", "1", strategyGroupPlanRateData);

	}

	private void setupClientStrategyBenGroupMap(List<Long> strategies, String[] planTypes) {
		String planType;
		String genericPlanType;
		for (int i = 0; i < planTypes.length; i++) {
			planType = planTypes[i];
			genericPlanType = planType;
			if ("1D".equals(planType))
				genericPlanType = "11";
			if ("1V".equals(planType))
				genericPlanType = "14";
			for (Long strategy : strategies) {
				strategyGroupPlantypeMap.put(strategy, "001LEN", genericPlanType, planType);
			}

		}

	}

	private void setupHVRealmPlanMapping() {

		RealmPlanMappingId planMappingId = new RealmPlanMappingId();
		planMappingId.setPlan("002ACM");
		planMappingId.setRealmYearId(9L);
		PlanMapping planMapping = new PlanMapping();
		planMapping.setOldPortfolioId(15);
		planMapping.setPlanType("14");
		planMapping.setNewBenefitPlan("0038QI");
		planMapping.setNewPortfolioId(15);
		realmPlanMapping.put("002ACM", planMapping);

		planMappingId = new RealmPlanMappingId();
		planMappingId.setPlan("002AXW");
		planMappingId.setRealmYearId(9L);
		planMapping = new PlanMapping();
		planMapping.setOldPortfolioId(1);
		planMapping.setPlanType("10");
		planMapping.setNewBenefitPlan("0038P3");
		planMapping.setNewPortfolioId(1);
		realmPlanMapping.put("002AXW", planMapping);

	}

	private void setupErEEPlanMapping() {
		BenefitPlan employerPaidPlan = new BenefitPlan();
		BenefitPlan employeePaidPlan = new BenefitPlan();

		employerPaidPlan.setId("002ACU");
		employerPaidPlan.setPlanType("11");
		employeePaidPlan.setId("002AD4");
		employeePaidPlan.setPlanType("1D");

		erEePlanMapping.put(employerPaidPlan, employeePaidPlan);
		erEePlanMapping.put(employeePaidPlan, employerPaidPlan);

		BenefitPlan employerPaidPlan2 = new BenefitPlan();
		BenefitPlan employeePaidPlan2 = new BenefitPlan();

		employerPaidPlan2.setId("0038QI");
		employerPaidPlan2.setPlanType("14");
		employeePaidPlan2.setId("0038QL");
		employeePaidPlan2.setPlanType("1V");

		erEePlanMapping.put(employerPaidPlan2, employeePaidPlan2);
		erEePlanMapping.put(employeePaidPlan2, employerPaidPlan2);

	}

	private void setupStrategyPortfolioDefaultPlans() {
		Map<String, String> portfolioMap1 = new HashMap<>();
		Map<String, String> portfolioMap2 = new HashMap<>();
		Map<String, String> portfolioMap3 = new HashMap<>();

		portfolioMap1.put("1", "0038Q4");
		strategyPortfolioDefaultPlans.put("10", portfolioMap1);
		portfolioMap2.put("14", "002ACW");
		strategyPortfolioDefaultPlans.put("11", portfolioMap2);
		portfolioMap2.put("15", "0038QI");
		strategyPortfolioDefaultPlans.put("14", portfolioMap3);

	}

	private void setupEmplStrategyBenGroup() {
		Long strategyId = 12345L;
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setId(30258L);
		benefitGroup.setName("new group");
		benefitGroup.setBenefitProgram("NBPG");

		emplStrategyBenGroupMap.put(EMPL_ID, strategyId, benefitGroup);

	}

}