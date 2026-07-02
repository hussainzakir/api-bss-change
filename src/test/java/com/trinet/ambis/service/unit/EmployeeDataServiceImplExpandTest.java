package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.RealmPlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanMappingId;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.impl.EmployeeDataServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRateData;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.EmployeeStrategyPlanData;
import com.trinet.ambis.service.model.StrategyGroupPlanRateData;

/**
 * @author clamorie
 */

@RunWith(MockitoJUnitRunner.class)
public class EmployeeDataServiceImplExpandTest extends ServiceUnitTest {

	@InjectMocks
	EmployeeDataServiceImpl employeeDataServiceImpl;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;

	@Mock
	BenefitOfferExceptionService benOfferExceptionService;

	String companyCode = "SCL";
	String planStart = "04-APR-2018";
	Long realmYearId = 9L;
	Long currentStrategyId = 6789L;
	List<Long> allStrategiesList = new ArrayList<>(Arrays.asList(6789L, 12345L, 54321L));
	List<Long> futureStrategiesList = new ArrayList<>(Arrays.asList(12345L, 54321L));
	List<Long> currentStrategyList = new ArrayList<>(Arrays.asList(6789L));
	List<EmployeeStrategyData> empStrategyDataList = new ArrayList<>();
	MultiKeyMap strategyGroupPlantypeMap = new MultiKeyMap();
	MultiKeyMap strategyGroupPlanCostMap = new MultiKeyMap();
	MultiKeyMap strategyGroupPlanMapping = new MultiKeyMap();
	MultiKeyMap emplStrategyBenGroupMap = new MultiKeyMap();
	Map<String, RealmPlanMapping> realmPlanMapping = new HashMap<>();
	Map<BenefitPlan, BenefitPlan> erEePlanMapping = new HashMap<>();
	Map<String, Map<String, String>> strategyPortfolioDefaultPlans = new HashMap<>();

	StrategyGroupPlanRateData strategyGrpPlnRate12345 = new StrategyGroupPlanRateData(); // dummy plan rate data for ben
																							// group 12345 plan 002AOP
	StrategyGroupPlanRateData strategyGrpPlnRate30258 = new StrategyGroupPlanRateData(); // dummy plan rate data for ben
																							// group 30258 plan 002AOP

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		// setup a required mapping table
		setupStrategyPortfolioDefaultPlans();

		// setup a required mapping table
		setupHVRealmPlanMapping();

		// setup a required mapping table
		setupErEEPlanMapping();

	}

	/*
	 * The procedure expandEmployeeStrategyPlans orders the plans in the
	 * employee/strategy plan list as medical,dental then vision It also adds plans
	 * to employees who are missing medical, dental and/or vision coverage If an
	 * employee has no coverage but the client offers that plan type, the procedure
	 * will add a waived plan All employees are in a list empStrategyDataList so the
	 * tests check the employee(s) in this list
	 */

	@Test
	public void expandEmployeeStrategyPlansTestAddMedOptDntlVisionToNoCvgEE() {
		/*
		 * GIVEN 1. A no coverage employee 2. A client the offers medical, optional
		 * dental and optional vision Testing that medical, optional dental and optional
		 * vision plans are added to this employee and coverage is waived Also testing
		 * that plans are sorted medical, dental then vision
		 **/
		EmployeeStrategyPlanData empStrategy;

		empStrategyDataList.clear();
		String[] employeeIds = { "0000001" };
		setupNoCoverageEmployees(employeeIds, currentStrategyList);

		strategyGroupPlantypeMap.clear();
		String[] planTypes = { "10", "1D", "1V" };
		setupClientStrategyBenGroupMap(currentStrategyList, planTypes);

		// when
		employeeDataServiceImpl.expandEmployeeStrategyPlans(empStrategyDataList, strategyGroupPlantypeMap);

		// then
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(0); // 1st employee 1st strategy
		assertEquals(3, empStrategy.getBenefitPlans().size()); // three plans should have been added to the no coverage
																// employee
		assertEquals("10", empStrategy.getBenefitPlans().get(0).getPlanType()); // 1st added plan should be medical
		assertEquals("N", empStrategy.getBenefitPlans().get(0).getCoverageElect()); // coverage should be waived
		assertEquals("1D", empStrategy.getBenefitPlans().get(1).getPlanType()); // 2nd added plan should be optional
																				// dental
		assertEquals("N", empStrategy.getBenefitPlans().get(1).getCoverageElect()); // coverage should be waived
		assertEquals("1V", empStrategy.getBenefitPlans().get(2).getPlanType()); // 3rd added plan should be optional
																				// vision
		assertEquals("N", empStrategy.getBenefitPlans().get(2).getCoverageElect()); // coverage should be waived

	}

	@Test
	public void expandEmployeeStrategyPlansTestAddMDVToNoCvgEE() {

		/*
		 * GIVEN 1. A no coverage employee 2. A client the offers medical, regular
		 * dental and regular vision Testing that the added plans are medical, regular
		 * dental and regular vision, plans have waived coverage and are in correct
		 * M/D/V order
		 **/

		EmployeeStrategyPlanData empStrategy;
		empStrategyDataList.clear();
		String[] employeeIds = { "0000001" };
		setupNoCoverageEmployees(employeeIds, currentStrategyList);

		strategyGroupPlantypeMap.clear();
		String[] planTypes = { "10", "11", "14" };
		setupClientStrategyBenGroupMap(currentStrategyList, planTypes);

		// when
		employeeDataServiceImpl.expandEmployeeStrategyPlans(empStrategyDataList, strategyGroupPlantypeMap);

		// then
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(0); // 1st employee 1st strategy
		assertEquals(3, empStrategy.getBenefitPlans().size()); // three plans should have been added to this no coverage
																// employee
		assertEquals("10", empStrategy.getBenefitPlans().get(0).getPlanType()); // 1st added plan should be medical
		assertEquals("N", empStrategy.getBenefitPlans().get(0).getCoverageElect()); // coverage should be waived
		assertEquals("11", empStrategy.getBenefitPlans().get(1).getPlanType()); // 2nd added plan should be regular
																				// dental
		assertEquals("N", empStrategy.getBenefitPlans().get(1).getCoverageElect()); // coverage should be waived
		assertEquals("14", empStrategy.getBenefitPlans().get(2).getPlanType()); // 3rd added plan should be regular
																				// vision
		assertEquals("N", empStrategy.getBenefitPlans().get(2).getCoverageElect()); // coverage should be waived
	}

	@Test
	public void expandEmployeeStrategyPlansTestAddDentalToMedOnlyEE() {
		/*
		 * GIVEN 1. An employee with only medical coverage 2. A client the offers
		 * medical and regular dental 3. Client does NOT offer vision Testing that the
		 * regular dental plan is the only added plan (ie there will be 2 plans the
		 * original medical and the added dental, since there is no vision offered)
		 **/
		EmployeeStrategyPlanData empStrategy;
		empStrategyDataList.clear();
		String[] employeeIds = { "0000001" };
		setupHealthOnlyCoverageEmployees(employeeIds, currentStrategyList);

		strategyGroupPlantypeMap.clear();
		String[] planTypes = { "10", "11" };
		setupClientStrategyBenGroupMap(currentStrategyList, planTypes); // setup a client who offers medical and regular
																		// dental but NOT vision

		// when
		employeeDataServiceImpl.expandEmployeeStrategyPlans(empStrategyDataList, strategyGroupPlantypeMap);

		// then
		empStrategy = empStrategyDataList.get(0).getStrategyDetails().get(0); // 1st employee 1st strategy
		assertEquals(3, empStrategy.getBenefitPlans().size());
		assertEquals("11", empStrategy.getBenefitPlans().get(1).getPlanType()); // added plan should be dental
		assertEquals("N", empStrategy.getBenefitPlans().get(1).getCoverageElect()); // coverage should be waived

		assertEquals("E", empStrategy.getBenefitPlans().get(0).getCoverageElect()); // ee's medical coverage should
																					// remained elected

	}

	@Test
	public void expandEmployeeStrategyPlansTestNoAddCheckOrder() {
		/*
		 * GIVEN 1. An employee with medical dental and vision coverage 2. A client the
		 * offers medical, regular dental and regular vision Testing that an employee
		 * with medical, dental and vision plans won't get any additional plans Also
		 * testing the order of the plans in the benefit plan list
		 **/

		empStrategyDataList.clear();
		String[] employeeIds = { "0000001" };
		EmployeeStrategyData empData;
		setupMDVCoverageEmployees(employeeIds, currentStrategyList); // setup employees with dental, vision and medical
																		// coverage

		strategyGroupPlantypeMap.clear();
		String[] planTypes = { "10", "11", "14" };
		setupClientStrategyBenGroupMap(currentStrategyList, planTypes); // setup a client who offers medical, dental and
																		// vision

		// when
		employeeDataServiceImpl.expandEmployeeStrategyPlans(empStrategyDataList, strategyGroupPlantypeMap);

		// then
		empData = empStrategyDataList.get(0);
		assertEquals(3, empData.getStrategyDetails().get(0).getBenefitPlans().size()); // verify no plans have been
																						// added to this employee's
																						// strategy

		// also testing that the plans are ordered as medical, dental then vision
		assertEquals("10", empData.getStrategyDetails().get(0).getBenefitPlans().get(0).getPlanType()); // 1st plan
																										// should be
																										// medical
		assertEquals("11", empData.getStrategyDetails().get(0).getBenefitPlans().get(1).getPlanType()); // 2nd plan
																										// should be
																										// dental
		assertEquals("14", empData.getStrategyDetails().get(0).getBenefitPlans().get(2).getPlanType()); // 3rd plan
																										// should be
																										// vision

	}

	@Test
	public void expandEmployeeStrategyPlansTestNoAddOptional() {
		/*
		 * GIVEN 1. An employee with medial dental and vision coverage 2. A client the
		 * offers medical, regular dental and regular vision Testing that an employee
		 * with medical, dental and vision plans won't get any additional plans (ie.
		 * there will be 3 plans the original medical, dental and vision plans)
		 **/

		empStrategyDataList.clear();
		String[] employeeIds = { "0000001" };
		EmployeeStrategyData empData;
		setupMedOptionalDVCoverageEmployees(employeeIds, currentStrategyList);

		strategyGroupPlantypeMap.clear();
		String[] planTypes = { "10", "1D", "1V" };
		setupClientStrategyBenGroupMap(currentStrategyList, planTypes); // setup a client who offers medical, dental and
																		// vision

		// when
		employeeDataServiceImpl.expandEmployeeStrategyPlans(empStrategyDataList, strategyGroupPlantypeMap);

		// then
		empData = empStrategyDataList.get(0);
		assertEquals(3, empData.getStrategyDetails().get(0).getBenefitPlans().size()); // verify no plans have been
																						// added to this employee's
																						// strategy

	}

	public void setupNoCoverageEmployees(String[] empIdArr, List<Long> strategyList) {
		EmployeeStrategyData empStrategyData; // Basic employee data and a set of strategies which will have plan and
												// cost data
		EmployeeStrategyPlanData empStrategyPlanData; // plan data for one strategy

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

			empStrategyDataList.add(empStrategyData); // add this employee to
														// the employee data set

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
			}
		}
	}

	public void setupHealthOnlyCoverageEmployees(String[] empIdArr, List<Long> strategyList) {
		setupNoCoverageEmployees(empIdArr, strategyList);
		BenefitPlanRateData empPlanAndRates;
		for (int i = 0; i < empIdArr.length; i++) {
			empPlanAndRates = new BenefitPlanRateData("002AOP", "10", "Aetna HNO 35 4 North NC", "1",
					CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(42.07), BigDecimal.valueOf(168.3), 1L,
					"E", null, false, true);
			EmployeeStrategyData empStrategyData = empStrategyDataList.get(i);
			for (EmployeeStrategyPlanData empStrategyPlanData : empStrategyData.getStrategyDetails()) {
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRates); // add health plan to every strategy
			}
		}
	}

	public void setupMDVCoverageEmployees(String[] empIdArr, List<Long> strategyList) {
		setupNoCoverageEmployees(empIdArr, strategyList);
		EmployeeStrategyData empStrategyData;
		BenefitPlanRateData empPlanAndRatesHlth = new BenefitPlanRateData("002AOP", "10", "Aetna HNO 35 4 North NC",
				"1", CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(42.07), BigDecimal.valueOf(168.3),
				1L, "E", null, false, true);
		BenefitPlanRateData empPlanAndRatesDental = new BenefitPlanRateData("002AD4", "11", "Guardian Dental", "1",
				CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(17.07), BigDecimal.valueOf(0), 1L, "E",
				null, false, true);
		BenefitPlanRateData empPlanAndRatesVision = new BenefitPlanRateData("002ACQ", "14", "EyeMedVision", "1",
				CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(5.89), BigDecimal.valueOf(0), 1L, "E",
				null, false, true);

		for (int i = 0; i < empIdArr.length; i++) {
			empStrategyData = empStrategyDataList.get(i);
			for (EmployeeStrategyPlanData empStrategyPlanData : empStrategyData.getStrategyDetails()) {
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesDental); // add dental plan to strategy
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesVision); // add vision plan to strategy
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesHlth); // add health plan to strategy

			}
		}
	}

	public void setupMedOptionalDVCoverageEmployees(String[] empIdArr, List<Long> strategyList) {
		setupNoCoverageEmployees(empIdArr, strategyList);
		EmployeeStrategyData empStrategyData;
		BenefitPlanRateData empPlanAndRatesHlth = new BenefitPlanRateData("002AOP", "10", "Aetna HNO 35 4 North NC",
				"1", CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(42.07), BigDecimal.valueOf(168.3),
				1L, "E", null, false, true);
		BenefitPlanRateData empPlanAndRatesDental = new BenefitPlanRateData("002AD4", "1D",
				"Guardian Dental EPO Optional", "1", CoverageCodesEnums.COV_EMPLOYEE.getName(),
				BigDecimal.valueOf(17.07), BigDecimal.valueOf(0), 1L, "E", null, false, true);
		BenefitPlanRateData empPlanAndRatesVision = new BenefitPlanRateData("002ACQ", "1V", "EyeMed Vision Optional",
				"1", CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(5.89), BigDecimal.valueOf(0), 1L,
				"E", null, false, true);

		for (int i = 0; i < empIdArr.length; i++) {
			empStrategyData = empStrategyDataList.get(i);
			for (EmployeeStrategyPlanData empStrategyPlanData : empStrategyData.getStrategyDetails()) {
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesHlth); // add health plan to strategy
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesDental); // add dental plan to strategy
				empStrategyPlanData.getBenefitPlans().add(empPlanAndRatesVision); // add vision plan to strategy
			}
		}
	}

	public void setupClientStrategyBenGroupMap(List<Long> strategies, String[] planTypes) {
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

	public void setupStrategyGroupPlanCostMapFor002AOPPlan(int[] strategies, int[] benGroups) {
		// sets up plan costs for the passed in strategies and benefit groups
		strategyGroupPlanCostMap.clear();
		StrategyGroupPlanRateData strategyGroupPlanRateData;
		Long strategyId;
		Long groupId;
		for (int i = 0; i < strategies.length; i++) {
			strategyId = Long.valueOf(strategies[i]);
			for (int j = 0; j < benGroups.length; j++) {
				groupId = Long.valueOf(benGroups[j]);
				String planType = "10";
				String benefitPlan = "002AOP";
				strategyGroupPlanRateData = new StrategyGroupPlanRateData();
				strategyGroupPlanRateData.setStrategyId(strategyId);
				strategyGroupPlanRateData.setGroupId(groupId);
				strategyGroupPlanRateData.setPlanType("10");
				strategyGroupPlanRateData.setBenefitPlan("002AOP");
				strategyGroupPlanRateData.setCoverageLevel("1");
				strategyGroupPlanRateData.setErContribPercent(BigDecimal.valueOf(80.00));
				strategyGroupPlanRateData.setErRate(BigDecimal.valueOf(168.3));
				strategyGroupPlanRateData.setEeRate(BigDecimal.valueOf(42.07));
				strategyGroupPlanRateData.setDescription("Aetna HNO 35 4 North NC");
				strategyGroupPlanCostMap.put(strategyId, groupId, planType, benefitPlan, "1",
						strategyGroupPlanRateData);
			}
		}

	}

	public void setupHVRealmPlanMapping() {

		RealmPlanMappingId planMappingId = new RealmPlanMappingId();
		planMappingId.setPlan("002ACM");
		planMappingId.setRealmYearId(9L);
		RealmPlanMapping planMapping = new RealmPlanMapping();
		planMapping.setPlanMappingId(planMappingId);
		planMapping.setOldPortfolioId(15);
		planMapping.setOldPlanDesc("EyeMed Group Vision");
		planMapping.setPlanType("14");
		planMapping.setNewPlan("0038QI");
		planMapping.setNewPortfolioId(15);
		planMapping.setNewPlanDesc("Aetna EyeMed Vision Group");
		realmPlanMapping.put("002ACM", planMapping);

		planMappingId = new RealmPlanMappingId();
		planMappingId.setPlan("002AXW");
		planMappingId.setRealmYearId(9L);
		planMapping = new RealmPlanMapping();
		planMapping.setPlanMappingId(planMappingId);
		planMapping.setOldPortfolioId(1);
		planMapping.setOldPlanDesc("Aetna ACO 6350 CO");
		planMapping.setPlanType("10");
		planMapping.setNewPlan("0038P3");
		planMapping.setNewPortfolioId(1);
		planMapping.setNewPlanDesc("Aetna ACO 6500 CO");
		realmPlanMapping.put("002AXW", planMapping);

	}

	public void setupErEEPlanMapping() {
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

	public void setupStrategyPortfolioDefaultPlans() {
		Map<String, String> portfolioMap1 = new HashMap<>();
		Map<String, String> portfolioMap2 = new HashMap<>();
		Map<String, String> portfolioMap3 = new HashMap<>();

		portfolioMap1.put("1", "0038Q4");
		strategyPortfolioDefaultPlans.put("11", portfolioMap1);
		portfolioMap2.put("14", "002ACW");
		strategyPortfolioDefaultPlans.put("11", portfolioMap2);
		portfolioMap2.put("15", "0038QI");
		strategyPortfolioDefaultPlans.put("14", portfolioMap3);

	}

	public void setupEmplStrategyBenGroup() {
		String emplId = "0000001";
		Long strategyId = 12345L;
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setId(30258L);
		benefitGroup.setName("new group");
		benefitGroup.setBenefitProgram("NBPG");

		emplStrategyBenGroupMap.put(emplId, strategyId, benefitGroup);

	}

}