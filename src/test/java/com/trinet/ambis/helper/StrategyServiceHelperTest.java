package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCountId;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class StrategyServiceHelperTest extends ServiceUnitTest {

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	MandatoryRegionDao mandatoryRegionDao;
	
	@Mock
	StrategyFundingDataDao strategyFundingDataDao;
	
	private static final String PERSON_ID = "00002222267";
	
	/*
	 * @Rule public PowerMockRule rule = new PowerMockRule();
	 */

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;

	@Before
	public void setUp() {
        mockStaticBSSSecurityUtils = org.mockito.Mockito.mockStatic(BSSSecurityUtils.class);
        mockStaticAppRulesAndConfigsUtils = org.mockito.Mockito.mockStatic(AppRulesAndConfigsUtils.class);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
            mockStaticAppRulesAndConfigsUtils = null;
        }
	}

	@Test
	public void constructStrategyForRenewals() {
		Company company = prepareCompany();
		String strategyName = StrategyTypesEnums.F_S.getName();
		String costShareType = "DFLT";
		 int acaFplOpted  =1;
		Strategy actualResult = StrategyServiceHelper.constructStrategyForRenewals(company, strategyName,
				costShareType, BSSApplicationConstants.STATUS_ACTIVE, acaFplOpted);

		assertEquals(StrategyTypesEnums.F_S.getName(), actualResult.getName());
		assertEquals(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED, actualResult.getType());
		assertEquals(costShareType, actualResult.getCostShareType());
		assertEquals("", actualResult.getComments());
		assertEquals(false, actualResult.isSubmitted());
		assertNull(actualResult.getSubmitDate());
		assertEquals(BigDecimal.ZERO, actualResult.getEstimatedTotalCost());
		assertEquals(BigDecimal.ZERO, actualResult.getCurrentYearTotalCost());
		assertEquals(1111, actualResult.getCompanyId());
		assertEquals(BigDecimal.ZERO, actualResult.getTotalBudget());
		assertEquals(0, actualResult.getHeadCount().intValue());
		assertEquals(1, actualResult.getBudgetFactor());

		strategyName = "Employees Pay Cost Changes";
		costShareType = "EEPC";

		actualResult = StrategyServiceHelper.constructStrategyForRenewals(company, strategyName, costShareType, BSSApplicationConstants.STATUS_ACTIVE, acaFplOpted);

		assertEquals(strategyName, actualResult.getName());
		assertEquals(BSSApplicationConstants.STRATEGY_TYPE_CUSTOM_RECOMMENDED, actualResult.getType());
		assertEquals(costShareType, actualResult.getCostShareType());
		assertEquals("", actualResult.getComments());
		assertEquals(false, actualResult.isSubmitted());
		assertNull(actualResult.getSubmitDate());
		assertEquals(BigDecimal.ZERO, actualResult.getEstimatedTotalCost());
		assertEquals(BigDecimal.ZERO, actualResult.getCurrentYearTotalCost());
		assertEquals(1111, actualResult.getCompanyId());
		assertEquals(BigDecimal.ZERO, actualResult.getTotalBudget());
		assertEquals(0, actualResult.getHeadCount().intValue());
		assertEquals(1, actualResult.getBudgetFactor());
	}

	@Test
	public void constructStrategyForCurrent() {
		Company company = prepareCompany();
		String strategyName = "Future Strategy Solution";

		Strategy actualResult = StrategyServiceHelper.constructStrategyForCurrent(company, strategyName);

		assertEquals("Future Strategy Solution", actualResult.getName());
		assertEquals(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED, actualResult.getType());
		assertEquals(null, actualResult.getCostShareType());
		assertEquals("", actualResult.getComments());
		assertEquals(true, actualResult.isSubmitted());
		assertNull(actualResult.getSubmitDate());
		assertEquals(BigDecimal.ZERO, actualResult.getEstimatedTotalCost());
		assertEquals(BigDecimal.ZERO, actualResult.getCurrentYearTotalCost());
		assertEquals(1111, actualResult.getCompanyId());
		assertEquals(BigDecimal.ZERO, actualResult.getTotalBudget());
		assertEquals(0, actualResult.getHeadCount().intValue());
		assertEquals(1, actualResult.getBudgetFactor());
	}

	@Test
	public void constructPlanSelection() {
		long strategyId = 1111;
		long benefitGroupId = 2222;
		BenefitPlan bp = new BenefitPlan();
		bp.setId("bp1");
		bp.setPlanType("10");
		bp.setPpoPlan(true);
		long headCount = 4;

		PlanSelection actualResult = StrategyServiceHelper.constructPlanSelection(strategyId, benefitGroupId, bp,
				headCount);

		assertEquals(0L, actualResult.getId());
		assertEquals("bp1", actualResult.getBenefitPlan());
		assertEquals(strategyId, actualResult.getStrategyId());
		assertEquals(benefitGroupId, actualResult.getGroupId());
		assertEquals("10", actualResult.getPlanType());
		assertEquals(headCount, actualResult.getHeadCount());
		assertEquals(true, actualResult.isPpoPlan());
	}

	@Test
	public void constructADPlanSelection() {
		long strategyId = 1111;
		long benefitGroupId = 2222;
		String planId = "AAAA";
		String planType = "10";
		long headCount = 5;

		PlanSelection actualResult = StrategyServiceHelper.constructADPlanSelection(strategyId, benefitGroupId, planId,
				planType, headCount);

		assertEquals(0L, actualResult.getId());
		assertEquals(planId, actualResult.getBenefitPlan());
		assertEquals(strategyId, actualResult.getStrategyId());
		assertEquals(benefitGroupId, actualResult.getGroupId());
		assertEquals(planType, actualResult.getPlanType());
		assertEquals(headCount, actualResult.getHeadCount());
	}

	@Test
	public void constructContribution() {
		String coverageCode = "C";
		int headCount = 5;
		int hsaHeadCount = 5;
		long planSelectionId = 1111L;
		BigDecimal newEmployerContribution = BigDecimal.valueOf(100);
		BigDecimal newEmployeeContribution = BigDecimal.valueOf(200);
		BigDecimal newEmployerPercent = BigDecimal.valueOf(75);

		Contribution actualResult = StrategyServiceHelper.constructContribution(coverageCode, headCount, hsaHeadCount,
				planSelectionId, newEmployerContribution, newEmployeeContribution, newEmployerPercent);

		assertEquals(planSelectionId, actualResult.getPlanSelectionId());
		assertEquals(headCount, actualResult.getHeadCount());
		assertEquals(newEmployerContribution, actualResult.getEmployerContribution());
		assertEquals(newEmployeeContribution, actualResult.getEmployeeContribution());
		assertEquals(newEmployerPercent, actualResult.getEmployerPercent());
		assertEquals(coverageCode, actualResult.getCoverageLevel());
	}
	
	@Test
	public void constructContributionFromPlanContribution() {
		int headCount = 5;
		Long planSelectionId = 1111L;
		BigDecimal employerContribution = BigDecimal.valueOf(100);
		BigDecimal employeeContribution = BigDecimal.valueOf(200);
		BigDecimal employerPercent = BigDecimal.valueOf(75);

		PlanContribution pc = new PlanContribution();
		pc.setId(planSelectionId);
		pc.setHeadcount(headCount);
		pc.setEmployerContribution(employerContribution);
		pc.setEmployeeContribution(employeeContribution);
		pc.setEmployerPercent(employerPercent);
		pc.setType(CoverageCodesEnums.COV_EMPLOYEE.getId());
		pc.setOverrideType(null);

		Contribution actualResult = StrategyServiceHelper.constructContribution(pc);

		//assertEquals(pc.getId(), Long.valueOf(actualResult.getId()));
		assertEquals(pc.getHeadcount(), actualResult.getHeadCount());
		assertEquals(pc.getHsaHeadcount(), actualResult.getHsaHeadCount());
		assertEquals(pc.getEmployerContribution(), actualResult.getEmployerContribution());
		assertEquals(pc.getEmployeeContribution(), actualResult.getEmployeeContribution());
		assertEquals(pc.getEmployerPercent(), actualResult.getEmployerPercent());
		assertEquals(CoverageCodesEnums.COV_EMPLOYEE.getCode(), actualResult.getCoverageLevel());
		assertEquals(pc.getOverrideType(), actualResult.getOverrideType());
	}	

	@Test
	public void constructBenefitPlan() {
		String benefitPlanId = "AAAA";
		String planType = "10";
		String vendorId = "BBBB";

		BenefitPlan actualResult = StrategyServiceHelper.constructBenefitPlan(benefitPlanId, planType, vendorId);

		assertEquals(benefitPlanId, actualResult.getId());
		assertEquals(planType, actualResult.getPlanType());
		assertEquals(vendorId, actualResult.getVendorId());
	}

	@Test
	public void constructXbssCompany() {
		Company company = prepareCompany();
		Long realmPlanYearId = 21L;

		Company actualResult = StrategyServiceHelper.constructXbssCompany(company, realmPlanYearId);

		assertEquals("G48", actualResult.getCode());
		assertEquals(realmPlanYearId, Long.valueOf(actualResult.getRealmPlanYearId()));
		assertEquals("Company G48", actualResult.getDescription());
		assertEquals("Trinet Inc.", actualResult.getName());
	}

	@Test
	public void constructOptionPlansMap() {
		Map<String, AdditionalBenefitPlan> disabilityOptionsMap = new HashMap<>();
		AdditionalBenefitPlan optionPlan = new AdditionalBenefitPlan();
		List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<>();
		DisabilityBenefitOptionPlans dbop = new DisabilityBenefitOptionPlans();
		dbop.setId("dbop1");
		optionPlans.add(dbop);
		dbop = new DisabilityBenefitOptionPlans();
		dbop.setId("dbop2");
		optionPlans.add(dbop);
		optionPlan.setOptionPlans(optionPlans);
		disabilityOptionsMap.put("option1", optionPlan);

		Map<String, List<String>> actualResult = StrategyServiceHelper.constructOptionPlansMap(disabilityOptionsMap);

		assertEquals(1, actualResult.size());
		assertEquals(2, actualResult.get("option1").size());
		assertTrue(Arrays.asList("dbop1", "dbop2").contains(actualResult.get("option1").get(0)));
		assertTrue(Arrays.asList("dbop1", "dbop2").contains(actualResult.get("option1").get(1)));
	}

	@Test
	public void createUpdateContribution() {
		Company company = prepareCompany();
		BenefitPlan benefitPlan = prepareBenefitPlan();
		PlanSelection planSelection = new PlanSelection();
		List<Contribution> contribList = new ArrayList<>();

		StrategyServiceHelper.createUpdateContribution(company, benefitPlan, planSelection, contribList);

		assertEquals(1, contribList.size());
		assertEquals(0, (int) contribList.get(0).getPlanSelectionId());
		assertEquals(5, contribList.get(0).getHeadCount());
		assertEquals(BigDecimal.valueOf(90), contribList.get(0).getEmployerContribution());
		assertEquals(BigDecimal.valueOf(10), contribList.get(0).getEmployeeContribution());
		assertEquals(BigDecimal.valueOf(75), contribList.get(0).getEmployerPercent());
		assertEquals("1", contribList.get(0).getCoverageLevel());
		assertEquals("BFPCT", contribList.get(0).getOverrideType());
	}

	@Test
	public void getBenefitGroupByNameType() {
		List<StrategyBenefitGroup> dtoGroups = prepareStrategyBenefitGroupList();
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setName("STD");
		benefitGroup.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		benefitGroup.setBenefitProgram("ABC123");
		StrategyBenefitGroup actualResult = StrategyServiceHelper.getBenefitGroupByCompare(dtoGroups, benefitGroup);

		assertEquals("STD", actualResult.getName());
		assertEquals(BSSApplicationConstants.STD_GROUP_TYPE, actualResult.getType());
		assertEquals("ABC123", actualResult.getBenefitProgram());

		dtoGroups = Collections.emptyList();

		actualResult = StrategyServiceHelper.getBenefitGroupByCompare(dtoGroups, benefitGroup);

		assertNull(actualResult);
	}

	@Test
	public void getBenefitGroupById() {
		List<StrategyBenefitGroup> dtoGroups = prepareStrategyBenefitGroupList();
		long id = 1111;

		StrategyBenefitGroup actualResult = StrategyServiceHelper.getBenefitGroupById(dtoGroups, id);

		assertEquals("K1", actualResult.getName());
		assertEquals(BSSApplicationConstants.K1_GROUP_TYPE, actualResult.getType());

		dtoGroups = Collections.emptyList();

		actualResult = StrategyServiceHelper.getBenefitGroupById(dtoGroups, id);

		assertNull(actualResult);
	}

	@Test
	public void getPlanSelection() {
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection ps = new PlanSelection();
		ps.setBenefitPlan("AAAA");
		planSelections.add(ps);
		ps = new PlanSelection();
		ps.setBenefitPlan("BBBB");
		planSelections.add(ps);
		String plan = "BBBB";

		PlanSelection actualResult = StrategyServiceHelper.getPlanSelection(planSelections, plan);

		assertEquals("BBBB", actualResult.getBenefitPlan());

		planSelections = Collections.emptyList();

		actualResult = StrategyServiceHelper.getPlanSelection(planSelections, plan);

		assertNull(actualResult);
	}

	@Test
	public void constructBenefitGroup() {
		long strategyId = 1111;
		BenefitGroupStrategy benefitGroupStrategy = prepareBenGrpStrategy();

		StrategyBenefitGroup actualResult = StrategyServiceHelper.constructBenefitGroup(strategyId,
				benefitGroupStrategy);

		assertEquals(strategyId, actualResult.getStrategyId());
		assertEquals("AAAA", actualResult.getBenefitProgram());
		assertEquals(3333, actualResult.getCompanyId());
		assertEquals(true, actualResult.isDefaultGroup());
		assertEquals(BigDecimal.valueOf(450), actualResult.getEstimatedTotalCost());
		assertEquals(5, actualResult.getHeadcount());
		assertEquals(4444, actualResult.getId());
		assertEquals("K1", actualResult.getName());
		assertEquals(BigDecimal.valueOf(5), actualResult.getPercentChange());
		assertEquals(BSSApplicationConstants.K1_GROUP_TYPE, actualResult.getType());
		assertEquals("FDOH", actualResult.getWaitingPeriod());
		assertEquals("A", actualResult.getStatus());
		assertEquals(2222, actualResult.getStrategyGroupId());
	}

	@Test
	public void getPlanSelectionIds() {
		Long groupId = 6666L;
		Map<String, Map<Long, List<PlanSelection>>> benefitOfferPlans = new HashMap<>();
		Map<Long, List<PlanSelection>> grpPlans = new HashMap<>();
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection ps = new PlanSelection();
		ps.setId(1111);
		planSelections.add(ps);
		ps.setId(2222);
		planSelections.add(ps);
		grpPlans.put(5555L, planSelections);
		planSelections = new ArrayList<>();
		ps = new PlanSelection();
		ps.setId(3333);
		planSelections.add(ps);
		ps.setId(4444);
		planSelections.add(ps);
		grpPlans.put(6666L, planSelections);
		benefitOfferPlans.put("10", grpPlans);

		List<Long> actualResult = StrategyServiceHelper.getPlanSelectionIds(groupId, benefitOfferPlans);

		assertEquals(2, actualResult.size());
		assertTrue(Arrays.asList(3333L, 4444L).contains(actualResult.get(0)));
		assertTrue(Arrays.asList(3333L, 4444L).contains(actualResult.get(1)));
	}

	@Test
	public void getLocations() {
		Company company = prepareCompany();
		Set<String> companyRegions = new HashSet<>(Arrays.asList("CA", "MA"));
		company.setCompanyRegions(companyRegions);

		Set<String> fundingRegions = new HashSet<>(Arrays.asList("ZZ"));
		company.setFundingRegions(fundingRegions);
		
		List<String> employeeRegions = Arrays.asList("NC", "SC");
		company.setEmployeeRegions(employeeRegions);

		Set<String> actualResult = StrategyServiceHelper.getLocations(company);

		assertEquals(5, actualResult.size());
		assertTrue(actualResult.containsAll(Arrays.asList("NC", "SC", "CA", "MA", "ZZ")));
	}

	@Test
	public void getHqStateCity() {
		Company company = prepareCompany();

		Set<String> actualResult = StrategyServiceHelper.getHqStateCity(company);

		assertEquals(2, actualResult.size());
		assertTrue(actualResult.containsAll(Arrays.asList("MA", "Boston")));
	}

	@Test
	public void updatePlanSelectionRegions() {
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection ps = new PlanSelection();
		ps.setBenefitPlan("AAAA");
		planSelections.add(ps);
		ps = new PlanSelection();
		ps.setBenefitPlan("BBBB");
		planSelections.add(ps);

		Map<String, List<String>> benefitPlansStatesMap = new HashMap<>();
		benefitPlansStatesMap.put("BBBB", Arrays.asList("NJ", "NY"));

		StrategyServiceHelper.updatePlanSelectionRegions(planSelections, benefitPlansStatesMap);

		for (PlanSelection planSelection : planSelections) {
			if ("AAAA".equals(planSelection.getBenefitPlan())) {
				assertEquals(1, planSelection.getListOfStates().size());
				assertTrue(Arrays.asList("All").containsAll(planSelection.getListOfStates()));
			} else if ("BBBB".equals(planSelection.getBenefitPlan())) {
				assertEquals(2, planSelection.getListOfStates().size());
				assertTrue(Arrays.asList("NJ", "NY").containsAll(planSelection.getListOfStates()));
			}
		}
	}

	@Test
	public void updateBenfitPlanRegions() {
		Set<StateBenefitPlan> stateBenefitPlans = new HashSet<>();
		StateBenefitPlan sbp = new StateBenefitPlan();
		sbp.setBenefitPlan("AAAA");
		stateBenefitPlans.add(sbp);
		sbp = new StateBenefitPlan();
		sbp.setBenefitPlan("BBBB");
		stateBenefitPlans.add(sbp);

		Map<String, List<String>> benefitPlansStatesMap = new HashMap<>();
		benefitPlansStatesMap.put("AAAA", Arrays.asList("NY", "NY"));

		StrategyServiceHelper.updateBenfitPlanRegions(stateBenefitPlans, benefitPlansStatesMap);

		for (StateBenefitPlan stateBenPlan : stateBenefitPlans) {
			if ("AAAA".equals(stateBenPlan.getBenefitPlan())) {
				assertEquals(2, stateBenPlan.getOfferedStates().size());
				assertTrue(Arrays.asList("NJ", "NY").containsAll(stateBenPlan.getOfferedStates()));
			} else if ("BBBB".equals(stateBenPlan.getBenefitPlan())) {
				assertEquals(1, stateBenPlan.getOfferedStates().size());
				assertTrue(Arrays.asList("All").containsAll(stateBenPlan.getOfferedStates()));
			}
		}
	}

	@Test
	public void calcStrategyEstimate() {
		
		List<StrategyData> strategyDataList = prepareStrategyDataListForCalcEstimate();
		Company comapny =  prepareCompany();
		Map<Long, List<StrategyEstimate>> actualResult = StrategyServiceHelper.calcStrategyEstimate(strategyDataList,comapny);

		assertEquals(1, actualResult.size());
		assertEquals(7, actualResult.get(1111L).size());
		assertEquals(11111L, actualResult.get(1111L).get(0).getGroupId());
		
//		assertEquals(BSSApplicationConstants.MEDICAL_PLAN_TYPE, actualResult.get(1111L).get(0).getPlanType());
//		assertEquals(null, actualResult.get(1111L).get(0).getPlanSubType());
//		assertEquals(new BigDecimal("4400.00"), actualResult.get(1111L).get(0).getEstimate());
//		
//		assertEquals(BSSApplicationConstants.MEDICAL_PLAN_TYPE, actualResult.get(1111L).get(1).getPlanType());
//		assertEquals(BSSApplicationConstants.HSA, actualResult.get(1111L).get(1).getPlanSubType());
//		assertEquals(new BigDecimal("2000.00"), actualResult.get(1111L).get(1).getEstimate());
//		
//		assertEquals(BSSApplicationConstants.LTD_CODE, actualResult.get(1111L).get(2).getPlanType());
//		assertEquals(null, actualResult.get(1111L).get(2).getPlanSubType());
//		assertEquals(new BigDecimal("800"), actualResult.get(1111L).get(2).getEstimate());
//		
//		assertEquals(BSSApplicationConstants.MEDICAL_PLAN_TYPE, actualResult.get(1111L).get(3).getPlanType());
//		assertEquals(BSSApplicationConstants.BSUPP, actualResult.get(1111L).get(3).getPlanSubType());
//		assertEquals(new BigDecimal("4800.00"), actualResult.get(1111L).get(3).getEstimate());
//		
//		assertEquals(BSSApplicationConstants.LIFE_CODE, actualResult.get(1111L).get(4).getPlanType());
//		assertEquals(null, actualResult.get(1111L).get(4).getPlanSubType());
//		assertEquals(new BigDecimal("1000"), actualResult.get(1111L).get(4).getEstimate());
//
//		assertEquals(BSSApplicationConstants.MEDICAL_PLAN_TYPE, actualResult.get(1111L).get(5).getPlanType());
//		assertEquals(BSSApplicationConstants.WAIVER_ALLOWANCE_PLAN_SUB_TYPE, actualResult.get(1111L).get(5).getPlanSubType());
//		assertEquals(new BigDecimal("10000"), actualResult.get(1111L).get(5).getEstimate());
//
//		assertEquals(BSSApplicationConstants.MEDICAL_PLAN_TYPE, actualResult.get(1111L).get(0).getPlanType());
	}

//	@Test
//	public void isSyncRequired() {
//		// bssCount == psCount and syncStatus = true THEN result true
//		Map<String, Integer> groupHeadCountMap = new HashMap<String, Integer>();
//		groupHeadCountMap.put("K1", 2);
//		groupHeadCountMap.put("Staff", 2);
//		Map<String, Integer> groupHeadCountMapBSS = new HashMap<String, Integer>();
//		groupHeadCountMapBSS.put("K1", 2);
//		groupHeadCountMapBSS.put("Staff", 2);
//		boolean syncStatus = true;
//
//		boolean actualResult = StrategyServiceHelper.isSyncRequired(groupHeadCountMap, groupHeadCountMapBSS,
//				syncStatus);
//
//		assertTrue(actualResult);
//
//		// bssCount == psCount and syncStatus = false THEN result true
//		groupHeadCountMap = new HashMap<String, Integer>();
//		groupHeadCountMap.put("K1", 2);
//		groupHeadCountMap.put("Staff", 2);
//		syncStatus = false;
//
//		actualResult = StrategyServiceHelper.isSyncRequired(groupHeadCountMap, groupHeadCountMapBSS, syncStatus);
//
//		assertFalse(actualResult);
//
//		// bssCount != psCount and syncStatus = true THEN result true
//		groupHeadCountMap = new HashMap<String, Integer>();
//		groupHeadCountMap.put("K1", 1);
//		groupHeadCountMap.put("Staff", 1);
//
//		actualResult = StrategyServiceHelper.isSyncRequired(groupHeadCountMap, groupHeadCountMapBSS, syncStatus);
//
//		assertTrue(actualResult);
//
//		// bssCount != psCount and syncStatus = false THEN result true
//		groupHeadCountMap = new HashMap<String, Integer>();
//		groupHeadCountMap.put("K1", 1);
//		groupHeadCountMap.put("Staff", 1);
//		syncStatus = false;
//
//		actualResult = StrategyServiceHelper.isSyncRequired(groupHeadCountMap, groupHeadCountMapBSS, syncStatus);
//
//		assertTrue(actualResult);
//	}

	@Test
	public void prepareCoverageLevelHeadCounts() {
		List<StrategyGroupHeadCount> strategyGrpHeadCounts = new ArrayList<>();
		StrategyGroupHeadCount sghc = new StrategyGroupHeadCount();
		StrategyGroupHeadCountId id = new StrategyGroupHeadCountId();
		id.setCovrgCd("1");
		sghc.setId(id);
		sghc.setHeadcount(2);
		strategyGrpHeadCounts.add(sghc);
		sghc = new StrategyGroupHeadCount();
		id = new StrategyGroupHeadCountId();
		id.setCovrgCd("C");
		sghc.setId(id);
		sghc.setHeadcount(4);
		strategyGrpHeadCounts.add(sghc);

		Map<String, Integer> actualResult = StrategyServiceHelper.prepareCoverageLevelHeadCounts(strategyGrpHeadCounts);

		assertEquals(2, actualResult.size());
		assertEquals(Integer.valueOf(2), actualResult.get("employee"));
		assertEquals(Integer.valueOf(4), actualResult.get("employeePlusChild"));
	}

	@Test
	public void getBenefitPlanList() {
		Map<Long, Map<String, Map<Long, List<PlanSelection>>>> strategyGroupPlansSelections = new HashMap<>();
		Map<String, Map<Long, List<PlanSelection>>> medicalPlanSelections = new HashMap<>();
		Map<Long, List<PlanSelection>> grpPlanSelections = new HashMap<>();
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection planSelection = new PlanSelection();
		planSelection.setBenefitPlan("AA1111");
		planSelections.add(planSelection);
		grpPlanSelections.put(111111L, planSelections);
		medicalPlanSelections.put(BSSApplicationConstants.MEDICAL, grpPlanSelections);

		grpPlanSelections = new HashMap<>();
		planSelections = new ArrayList<>();
		planSelection = new PlanSelection();
		planSelection.setBenefitPlan("BB1111");
		planSelections.add(planSelection);
		grpPlanSelections.put(22222L, planSelections);
		medicalPlanSelections.put(BSSApplicationConstants.DENTAL, grpPlanSelections);

		strategyGroupPlansSelections.put(1111L, medicalPlanSelections);

		planSelections = new ArrayList<>();
		medicalPlanSelections = new HashMap<>();
		planSelection = new PlanSelection();
		planSelection.setBenefitPlan("CC1111");
		planSelections.add(planSelection);
		medicalPlanSelections.put(BSSApplicationConstants.MEDICAL, new HashMap<Long, List<PlanSelection>>());

		strategyGroupPlansSelections.put(2222L, medicalPlanSelections);

		planSelections = new ArrayList<>();
		medicalPlanSelections = new HashMap<>();
		planSelection = new PlanSelection();
		planSelection.setBenefitPlan("DD1111");
		planSelections.add(planSelection);
		medicalPlanSelections.put(BSSApplicationConstants.MEDICAL, null);

		strategyGroupPlansSelections.put(3333L, medicalPlanSelections);

		List<String> actualResult = StrategyServiceHelper.getBenefitPlanList(strategyGroupPlansSelections);

		assertEquals(1, actualResult.size());
	}
	
	// When logged in person is different than CREATEDBY then return false
	@Test
	public void isStrategyDeletable1() {
		Strategy strategy = prepareStrategy(1111L, false, "00002222268", BSSApplicationConstants.STRATEGY_TYPE_CUSTOM);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
		Set<Long> pendingStrategyIds = Collections.emptySet();
		
		boolean result = StrategyServiceHelper.isStrategyDeletable(strategy, pendingStrategyIds, false);

		assertFalse(result);
	}
	
	// When strategy type is different than custom or current then return false
	@Test
	public void isStrategyDeletable2() {
		Strategy strategy = prepareStrategy(1111L, false, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
		Set<Long> pendingStrategyIds = Collections.emptySet();

		boolean result = StrategyServiceHelper.isStrategyDeletable(strategy, pendingStrategyIds, false);

		assertFalse(result);
	}
	
	// When strategy is history then return false
	@Test
	public void isStrategyDeletable3() {
		Strategy strategy = prepareStrategy(1111L, false, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_CUSTOM);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
		Set<Long> pendingStrategyIds = Collections.emptySet();
		
		boolean result = StrategyServiceHelper.isStrategyDeletable(strategy, pendingStrategyIds, true);

		assertFalse(result);
	}
	
	// When strategy submit is pending then return false
	@Test
	public void isStrategyDeletableSubmitted() {
		Strategy strategy = prepareStrategy(1111L, true, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
		Set<Long> pendingStrategyIds = new HashSet<>(Arrays.asList(1111L));

		boolean result = StrategyServiceHelper.isStrategyDeletable(strategy, pendingStrategyIds, false);

		assertFalse(result);
	}
	
	// When strategy is deletable then return true
	@Test
	public void isStrategyDeletableCustom() {
		Strategy strategy = prepareStrategy(1111L, false, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_CUSTOM);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
		Set<Long> pendingStrategyIds = new HashSet<>(Arrays.asList(2222L));

		boolean result = StrategyServiceHelper.isStrategyDeletable(strategy, pendingStrategyIds, false);

		assertTrue(result);
	}

	// When logged in person is different than CREATEDBY then return false
	@Test
	public void isStrategyNameEditable1() {
		Strategy strategy = prepareStrategy(1111L, false, "00002222268", BSSApplicationConstants.STRATEGY_TYPE_CUSTOM);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
		
		boolean result = StrategyServiceHelper.isStrategyNameEditable(strategy, false);

		assertFalse(result);
	}
	
	// When strategy type is different than custom or current then return false
	@Test
	public void isStrategyNameEditable2() {
		Strategy strategy = prepareStrategy(1111L, false, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);

		boolean result = StrategyServiceHelper.isStrategyNameEditable(strategy, false);

		assertFalse(result);
	}
	
	// When strategy is history then return false
	@Test
	public void isStrategyNameEditable3() {
		Strategy strategy = prepareStrategy(1111L, false, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_CUSTOM);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
		
		boolean result = StrategyServiceHelper.isStrategyNameEditable(strategy, true);

		assertFalse(result);
	}
	
	// When strategy submit is pending then return false 
	public void isStrategyNameEditableSubmitted() {
		Strategy strategy = prepareStrategy(1111L, true, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);

		boolean result = StrategyServiceHelper.isStrategyNameEditable(strategy, false);

		assertFalse(result);
	}
	
	// When strategy is editable then return true
	@Test
	public void isStrategyNameEditableCustom() {
		Strategy strategy = prepareStrategy(1111L, false, PERSON_ID, BSSApplicationConstants.STRATEGY_TYPE_CUSTOM);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);

		boolean result = StrategyServiceHelper.isStrategyNameEditable(strategy, false);

		assertTrue(result);
	}
	
	@Test
	public void getLocationsTest() {
		
		Company company = new Company();
		company.setCompanyRegions(new HashSet<>(Arrays.asList("AL", "AR")));
		company.setFundingRegions(new HashSet<>(Arrays.asList("MI", "MO")));
		company.setEmployeeRegions(Arrays.asList("WI", "WY"));

		Set<String> result = StrategyServiceHelper.getLocations(company);
		assertEquals(6, result.size());
	}
	
	@Test
	public void getPlanContributionsByStrategyId() {

		List<Object[]> dataResults = preparePlanContributionsByStrategyId();
		Map<String, Map<String, List<Contribution>>> result = StrategyServiceHelper
				.getPlanContributionsByStrategyId(dataResults);
		assertEquals(1, result.size());
		assertEquals(1, result.get("BENEFIT_PROGRAM").size());
		assertEquals(4, result.get("BENEFIT_PROGRAM").get("BENEFIT_PLAN").size());
	}
	
	@Test
	public void getBenefitPlanContributionsByStrategyId() {

		List<Object[]> dataResults = preparePlanContributionsByStrategyId();
		Map<String, Map<String, Map<String, BenefitPlan>>> result = StrategyServiceHelper
				.getBenefitPlanContributionsByStrategyId(dataResults);
		assertEquals(1, result.size());
		assertEquals(1, result.get("BENEFIT_PROGRAM").size());
		assertEquals(1, result.get("BENEFIT_PROGRAM").get("10").size());
		assertEquals("BENEFIT_PLAN", result.get("BENEFIT_PROGRAM").get("10").get("BENEFIT_PLAN").getId());
	}
	
	@Test
	public void constructPlanPackages() {
		Map<String, PlanPackage> result = StrategyServiceHelper.constructPlanPackages(
				prepareGroupFundingDetails(), prepareCompany(), preparePlanRateMap(), preparePlyrMapping(),
				realmDataDao, 1L, strategyFundingDataDao);
		assertEquals(1, result.size());
	}
    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<?> constructor = StrategyServiceHelper.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            org.junit.Assert.fail("Expected IllegalStateException");
        } catch (InvocationTargetException e) {
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }
	/**
	 * given 0 strategy id</br>
	 * when isProspectStrategy is called</br>
	 * then return true
	 **/
	@Test
	public void isProspectStrategyTest1() {
		// given
		// data
		long strategyId = 0;
		// when
		boolean actualResult = StrategyServiceHelper.isProspectStrategy(strategyId);
		// then
		// assertions
		assertTrue(actualResult);
	}
	
	/**
	 * given non zero strategy id</br>
	 * when isProspectStrategy is called</br>
	 * then return false
	 **/
	@Test
	public void isProspectStrategyTest2() {
		// given
		// data
		long strategyId = 1;
		// when
		boolean actualResult = StrategyServiceHelper.isProspectStrategy(strategyId);
		// then
		// assertions
		assertFalse(actualResult);
	}
	
	private Company prepareCompany() {
		Company company = new Company();
		company.setCode("G48");
		company.setRealmPlanYearId(21);
		company.setDescription("Company G48");
		company.setName("Trinet Inc.");
		company.setId(1111);
		company.setPlanStartDate("01-JAN-2018");
		company.setPlanEndDate("31-DEC-2018");
		company.setHeadQuatersState("MA");
		company.setHeadQuatersCity("Boston");
		company.setAleAmount(new BigDecimal(99.75));
		return company;
	}

	private BenefitPlan prepareBenefitPlan() {
		BenefitPlan benefitPlan = new BenefitPlan();
		List<PlanContribution> contributions = new ArrayList<>();
		PlanContribution pc = new PlanContribution();
		pc.setType("employee");
		pc.setHeadcount(5);
		pc.setEmployerPercent(BigDecimal.valueOf(75));
		pc.setEmployerContribution(BigDecimal.valueOf(90));
		pc.setEmployeeContribution(BigDecimal.valueOf(10));
		pc.setOverrideType("BFPCT");
		contributions.add(pc);
		benefitPlan.setContributions(contributions);
		return benefitPlan;
	}

	private List<StrategyBenefitGroup> prepareStrategyBenefitGroupList() {
		List<StrategyBenefitGroup> list = new ArrayList<>();
		StrategyBenefitGroup sbg = new StrategyBenefitGroup();
		sbg.setId(1111);
		sbg.setName("K1");
		sbg.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		sbg.setBenefitProgram("ABC123");
		list.add(sbg);
		sbg = new StrategyBenefitGroup();
		sbg.setId(2222);
		sbg.setName("STD");
		sbg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		sbg.setBenefitProgram("ABC123");
		list.add(sbg);
		return list;
	}

	private BenefitGroupStrategy prepareBenGrpStrategy() {
		BenefitGroupStrategy benefitGroupStrategy = new BenefitGroupStrategy();
		benefitGroupStrategy.setDefaultGroup(true);
		benefitGroupStrategy.setHeadcount(5);
		benefitGroupStrategy.setWaitingPeriod("FDOH");
		benefitGroupStrategy.setStatus("A");
		benefitGroupStrategy.setId(2222);
		BenefitGroup bg = new BenefitGroup();
		bg.setBenefitProgram("AAAA");
		bg.setCompanyId(3333);
		bg.setEstimatedTotalCost(BigDecimal.valueOf(450));
		bg.setId(4444);
		bg.setName("K1");
		bg.setPercentChange(BigDecimal.valueOf(5));
		bg.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		benefitGroupStrategy.setBenefitGroup(bg);
		return benefitGroupStrategy;
	}

	private List<StrategyData> prepareStrategyDataListForCalcEstimate() {

		List<StrategyData> strategyDataList = new ArrayList<>();
		StrategyData strategyData = new StrategyData();
		StrategySummary summary = new StrategySummary();
		summary.setId(1111L);
		summary.setType("recommended");
		summary.setEffectiveDate(new Date());
		summary.setEndDate(new Date());
		summary.setComments("Comment");
		summary.setPercentChange(BigDecimal.valueOf(10));
		summary.setTotalEmployees(2);
		summary.setCompanyId("1111");
		summary.setCostShareType("Shared");
		summary.setMedicalCarriers(Arrays.asList("Aetna"));
		summary.setDentalCarriers(Arrays.asList("11"));
		summary.setVisionCarriers(Arrays.asList("17"));
		summary.setSubmitStatus("SUCCESS");
		strategyData.setStrategySummary(summary);
		strategyDataList.add(strategyData);
		List<StrategyBenefitGroup> benefitGroups = new ArrayList<>();
		strategyData.setBenefitGroups(benefitGroups);
		
		StrategyHsaFundingDto strategyHsaFundingDto = new StrategyHsaFundingDto();
		strategyHsaFundingDto.setStrategyId(1111);
		strategyHsaFundingDto.setOptionId(7);
		strategyHsaFundingDto.setMonthlyEeAmount(BigDecimal.valueOf(200));
		strategyHsaFundingDto.setMonthlyFamilyAmount(BigDecimal.valueOf(200));
		strategyHsaFundingDto.setContributionFrequency("M");
		strategyHsaFundingDto.setAnnualEeAmount(BigDecimal.valueOf(1200));
		strategyHsaFundingDto.setAnnualFamilyAmount(BigDecimal.valueOf(2400));
		strategyHsaFundingDto.setAnnualMonth(1);
		strategyHsaFundingDto.setLumpSumFrequency("A");
		strategyData.setStrategyHsaFunding(strategyHsaFundingDto);
		
		StrategyBenefitGroup benGrp = new StrategyBenefitGroup();
		benGrp.setId(11111L);
		benefitGroups.add(benGrp);
		List<BenefitOffer> benefitOffers = new ArrayList<>();
		benGrp.setBenefitOffers(benefitOffers);
		BenefitOffer benOffer = new BenefitOffer();
		BenefitOfferSummary benOfferSummary = new BenefitOfferSummary();
		benOfferSummary.setDescription(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		benOfferSummary.setWaiverHeadcount(10L);
		benOffer.setSummary(benOfferSummary);
		List<BenefitPlan> benPlans = new ArrayList<>();
		BenefitPlan benPlan = new BenefitPlan();
		List<PlanContribution> planContributions = new ArrayList<>();
		PlanContribution planContribution = new PlanContribution();
		planContribution.setType(CoverageCodesEnums.COV_EMPLOYEE.getId());
		planContribution.setPlanCost(BigDecimal.valueOf(500));
		planContribution.setEmployerPercent(BigDecimal.valueOf(80));
		planContribution.setHeadcount(8);
		planContribution.setHsaHeadcount(4);
		planContributions.add(planContribution);

		planContribution = new PlanContribution();
		planContribution.setType(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId());
		planContribution.setPlanCost(BigDecimal.valueOf(400));
		planContribution.setEmployerPercent(null);
		planContribution.setEmployerContribution(BigDecimal.valueOf(300));
		planContribution.setHeadcount(4);
		planContribution.setHsaHeadcount(2);
		planContributions.add(planContribution);

		benPlan.setHighDeductible(true);
		benPlan.setContributions(planContributions);
		benPlans.add(benPlan);
		benOffer.setBenefitPlans(benPlans);
		benefitOffers.add(benOffer);

		PlanPackage planPackage = new PlanPackage();
		planPackage.setFundingType(BSSApplicationConstants.BSUPP);
		planPackage.setBsuppExcessOption(BigDecimal.valueOf(ExcessOptionEnum.FORFEIT.getType()));
		planPackage.setWaiverAllowance(BigDecimal.valueOf(1000));
		
		Map<String, BigDecimal> bsuppCoverageLevelFunding = new HashMap<>(); 
		bsuppCoverageLevelFunding.put((CoverageCodesEnums.COV_EMPLOYEE.getId()), BigDecimal.valueOf(1000));
		bsuppCoverageLevelFunding.put((CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()), BigDecimal.valueOf(2000));
		bsuppCoverageLevelFunding.put((CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()), BigDecimal.valueOf(3000));
		bsuppCoverageLevelFunding.put((CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()), BigDecimal.valueOf(4000));
		
		planPackage.setCoverageLevelFunding(bsuppCoverageLevelFunding);
		benOffer.setPlanPackage(planPackage);
		
		List<AdditionalBenefitOffer> additionalBenefitOfferList = new ArrayList<>();
		AdditionalBenefitOffer additionalBenefitOffer = new AdditionalBenefitOffer();
		
		AdditionalBenefitPlan ltdAdditionalBenefitPlan = new AdditionalBenefitPlan();
		ltdAdditionalBenefitPlan.setMonthlyTotalCost(BigDecimal.ONE);
		DisabilityBenefitOptionPlans ltdOptionPlan1 = new DisabilityBenefitOptionPlans();
		ltdOptionPlan1.setPlanType(BSSApplicationConstants.LTD_CODE);
		ltdOptionPlan1.setPlanCost(BigDecimal.valueOf(30));
		ltdOptionPlan1.setPlanHeadCount(20L);
		
		DisabilityBenefitOptionPlans ltdOptionPlan2 = new DisabilityBenefitOptionPlans();
		ltdOptionPlan2.setPlanType(BSSApplicationConstants.LTD_CODE);
		ltdOptionPlan2.setPlanCost(BigDecimal.valueOf(20));
		ltdOptionPlan2.setPlanHeadCount(10L);
		
		ltdAdditionalBenefitPlan.setId("ADDITIONAL PLAN");
		ltdAdditionalBenefitPlan.setOptionPlans(Arrays.asList(ltdOptionPlan1, ltdOptionPlan2));
		

		AdditionalBenefitPlan lifeAdditionalBenefitPlan1 = new AdditionalBenefitPlan();
		lifeAdditionalBenefitPlan1.setPlanCost(BigDecimal.valueOf(20));
		lifeAdditionalBenefitPlan1.setPlanType(BSSApplicationConstants.LIFE_CODE);
		
		AdditionalBenefitPlan lifeAdditionalBenefitPlan2 = new AdditionalBenefitPlan();
		lifeAdditionalBenefitPlan2.setPlanCost(BigDecimal.valueOf(30));
		lifeAdditionalBenefitPlan2.setPlanType(BSSApplicationConstants.LIFE_CODE);
		
		BenefitOfferSummary additionalBenefitSummary = new BenefitOfferSummary();
		additionalBenefitSummary.setHeadcount(20L);
		
		
		additionalBenefitOffer.setAdditionalBenefitPlans(Arrays.asList(ltdAdditionalBenefitPlan, lifeAdditionalBenefitPlan1, lifeAdditionalBenefitPlan2));
		additionalBenefitOffer.setSummary(additionalBenefitSummary);
		additionalBenefitOfferList.add(additionalBenefitOffer);
		benOffer.setAdditionalBenefitOffers(additionalBenefitOfferList);

		return strategyDataList;

	}

	private Strategy prepareStrategy(long id, boolean submitted, String createdBy, String type) {
		Strategy strategy = new Strategy();
		strategy.setId(id);
		strategy.setSubmitted(submitted);
		strategy.setCreatedBy(createdBy);
		strategy.setType(type);
		return strategy;
	}
	
	private List<Object[]> preparePlanContributionsByStrategyId() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[16];
		r[0] = new BigDecimal(4298992);
		r[1] = new BigDecimal(1370898);
		r[2] = "1";
		r[3] = new BigDecimal(49.99910495318905);
		r[4] = new BigDecimal(1);
		r[5] = new BigDecimal(279.32);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "BENEFIT_PLAN";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "DFLT";
		results.add(r);
		r = new Object[16];
		r[0] = new BigDecimal(4298993);
		r[1] = new BigDecimal(1370898);
		r[2] = "2";
		r[3] = new BigDecimal(24.99932872090005);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(837.96);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "BENEFIT_PLAN";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "FPL";
		results.add(r);
		r = new Object[16];
		r[0] = new BigDecimal(4298994);
		r[1] = new BigDecimal(1370898);
		r[2] = "C";
		r[3] = new BigDecimal(27.77683854606931);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(726.24);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "BENEFIT_PLAN";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "DFLT";
		results.add(r);
		r = new Object[16];
		r[0] = new BigDecimal(4298995);
		r[1] = new BigDecimal(1370898);
		r[2] = "4";
		r[3] = new BigDecimal(16.66626887045766);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(1396.59);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "BENEFIT_PLAN";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "DFLT";
		results.add(r);
		return results;
	}
	
	public Map<String, Map<String, Object>> prepareGroupFundingDetails() {
		Map<String, Map<String, Object>> planTypeFunding = new HashMap<>();
		Map<String, Object> funding = new HashMap<>();
		funding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BSUPP);
		funding.put(BSSApplicationConstants.FUNDING_MODEL_ID, BigDecimal.ONE);
		funding.put(BSSApplicationConstants.CUSTOMIZED, BigDecimal.ONE);
		planTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, funding);
		return planTypeFunding;
	}
	
	private Map<String, List<BenefitPlanRate>> preparePlanRateMap() {
		String benefitPlan;
		Map<String, List<BenefitPlanRate>> planRateMap = new HashMap<String, List<BenefitPlanRate>>();

		// Plan with regular band code
		benefitPlan = "BENEFIT_PLAN";
		List<BenefitPlanRate> planRateList = new ArrayList<BenefitPlanRate>();
		BenefitPlanRate planRate = new BenefitPlanRate();
		planRate.setBenefitPlan(benefitPlan);
		planRate.setBandCode("10");
		planRate.setCoverageCode("1");
		planRate.setEmployerCost(BigDecimal.valueOf(1000));
		planRateList.add(planRate);
		planRateMap.put(benefitPlan, planRateList);

		// Plan with N band code
		planRateList = new ArrayList<BenefitPlanRate>();
		benefitPlan = "MEDPLAN-N";
		planRate = new BenefitPlanRate();
		planRate.setBenefitPlan(benefitPlan);
		planRate.setBandCode("N");
		planRate.setCoverageCode("1");
		planRate.setEmployerCost(BigDecimal.valueOf(5000));
		planRateList.add(planRate);
		planRateMap.put(benefitPlan, planRateList);

		return planRateMap;
	}
	
	private Map<String, XbssRealmPlyrPlan> preparePlyrMapping() {
		Map<String, XbssRealmPlyrPlan> map = new HashMap<>();
		this.makePlyrObj(map, 19401, 24, "10", "TS0TF8", 9, "", "");
		this.makePlyrObj(map, 19402, 24, "10", "TS11LH", 9, "", "1");
		this.makePlyrObj(map, 19403, 24, "10", "TS13HF", 2, "", "2");
		this.makePlyrObj(map, 19404, 24, "10", "TS13HG", 2, "", "3");
		this.makePlyrObj(map, 19405, 24, "10", "TS13HH", 2, "", "4");
		this.makePlyrObj(map, 19406, 24, "10", "TS13HI", 2, "", "5");
		this.makePlyrObj(map, 19407, 24, "10", "TS13HJ", 2, "", "6");
		this.makePlyrObj(map, 19408, 24, "10", "TS13HK", 2, "", "7");
		this.makePlyrObj(map, 19409, 24, "10", "TS1998", 9, "", "8");
		this.makePlyrObj(map, 19410, 24, "10", "TS1EKS", 9, "", "9");
		this.makePlyrObj(map, 19411, 24, "10", "TS1EKU", 9, "", "A");
		this.makePlyrObj(map, 19412, 24, "10", "TS1EKV", 9, "", "B");
		this.makePlyrObj(map, 19413, 24, "10", "TS1EKW", 9, "", "C");
		this.makePlyrObj(map, 19414, 24, "10", "TS1EKX", 9, "", "D");
		this.makePlyrObj(map, 19415, 24, "10", "TS1EKY", 9, "", "E");
		this.makePlyrObj(map, 19416, 24, "10", "TS1EL0", 9, "", "F");
		this.makePlyrObj(map, 19417, 24, "10", "TS3GIB", 9, "", "G");
		this.makePlyrObj(map, 19418, 24, "10", "TS4S6K", 9, "", "H");
		this.makePlyrObj(map, 19419, 24, "10", "TS4S6L", 9, "", "I");
		this.makePlyrObj(map, 19420, 24, "10", "TS4S6M", 9, "", "J");
		this.makePlyrObj(map, 19420, 24, "10", "TS4S6N", 9, "", "K");
		this.makePlyrObj(map, 19420, 24, "10", "TS4NUL", 9, "", null);
		this.makePlyrObj(map, 19436, 24, "11", "TS2J1T", 16, "", "");
		this.makePlyrObj(map, 19471, 24, "14", "TS4S84", 15, "", "");
		this.makePlyrObj(map, 19472, 24, "1D", "TS0TFY", 3, "", "");
		this.makePlyrObj(map, 19498, 24, "1V", "TS4S8W", 15, "", "");
		this.makePlyrObj(map, 19499, 24, "23", "TS0SRO", 3, "", "");
		this.makePlyrObj(map, 19507, 24, "30", "TS0SRS", 3, "", "");
		this.makePlyrObj(map, 19511, 24, "31", "TS2J41", 3, "", "");
		return map;
	}

	private void makePlyrObj(Map<String, XbssRealmPlyrPlan> plyrMap, long id, long rpyId, String planType,
			String benefitPlan, long portId, String situs, String locator) {
		XbssRealmPlyrPlan plyr = new XbssRealmPlyrPlan();
		plyr.setId(id);
		plyr.setRealmYearId(BigDecimal.valueOf(rpyId));
		plyr.setPlanType(planType);
		plyr.setBenefitPlan(benefitPlan);
		plyr.setPortfolioId(BigDecimal.valueOf(portId));
		plyr.setBandLocator(locator);

		plyrMap.put(plyr.getBenefitPlan(), plyr);
	}	
}
