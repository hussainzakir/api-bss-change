package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.StateBenefitPlan;


@RunWith(JUnit4.class)


public class AdditionalBenefitServiceHelperTest {
	
	private static final String STD_PLAN_ID1 = "STDID1";
	private static final String STD_PRIMARY_PLAN_ID2 = "STDPRID1";
	private static final String LTD_PLAN_ID1 = "LTDID1";
	private static final String DENTAL_PLAN_1 = "dentalPlan1";
	private static final String MEDICAL_PLAN_1 = "medPlan1";
	private static final String MEDICAL_PLAN_2 = "medPlan2";
	private static final String K1_GROUP_1 = "K1_GROUP1";
	private static final String K1_GROUP_2 = "K1_GROUP2";	
	private static final String STD_GROUP_1 = "STD_GROUP1";
	private static final String STD_GROUP_2 = "STD_GROUP2";	
	private static final String STD_GROUP_3 = "STD_GROUP3";	

	@Test
	public void getADBPlanListMapByType() {
		Map<String, Set<StateBenefitPlan>> adbAllStatePlansMap = new HashMap<>();
		Set<StateBenefitPlan> stateBenefitPlans = new HashSet<>();
		StateBenefitPlan stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan(MEDICAL_PLAN_1);
		stateBenefitPlans.add(stateBenefitPlan);
		stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan(MEDICAL_PLAN_2);
		stateBenefitPlans.add(stateBenefitPlan);
		adbAllStatePlansMap.put(PlanTypesEnum.MEDICAL.getCode(), stateBenefitPlans);

		stateBenefitPlans = new HashSet<>();
		stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan(DENTAL_PLAN_1);
		stateBenefitPlans.add(stateBenefitPlan);
		adbAllStatePlansMap.put(PlanTypesEnum.DENTAL.getCode(), stateBenefitPlans);

		Map<String, Set<String>> actualResult = AdditionalBenefitServiceHelper
				.getADBPlanListMapByType(adbAllStatePlansMap);

		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get(PlanTypesEnum.MEDICAL.getCode()).size());
		assertTrue(Arrays.asList(MEDICAL_PLAN_1, MEDICAL_PLAN_2).containsAll(actualResult.get(PlanTypesEnum.MEDICAL.getCode())));
		assertEquals(1, actualResult.get(PlanTypesEnum.DENTAL.getCode()).size());
		assertTrue(Arrays.asList(DENTAL_PLAN_1).containsAll(actualResult.get(PlanTypesEnum.DENTAL.getCode())));
	}

	@Test
	public void getADBPlanListMapByType1() {
		List<PlanSelection> adPlanSelections = new ArrayList<>();
		PlanSelection ps = new PlanSelection();
		ps.setPlanType(PlanTypesEnum.MEDICAL.getCode());
		ps.setBenefitPlan(MEDICAL_PLAN_1);
		adPlanSelections.add(ps);
		ps = new PlanSelection();
		ps.setPlanType(PlanTypesEnum.MEDICAL.getCode());
		ps.setBenefitPlan(MEDICAL_PLAN_2);
		adPlanSelections.add(ps);
		ps = new PlanSelection();
		ps.setPlanType(PlanTypesEnum.DENTAL.getCode());
		ps.setBenefitPlan(DENTAL_PLAN_1);
		adPlanSelections.add(ps);
		
		Map<String, Set<String>> actualResult = AdditionalBenefitServiceHelper
				.getADBPlanListMapByType(adPlanSelections);
		
		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get(PlanTypesEnum.MEDICAL.getCode()).size());
		assertTrue(Arrays.asList(MEDICAL_PLAN_1, MEDICAL_PLAN_2).containsAll(actualResult.get(PlanTypesEnum.MEDICAL.getCode())));
		assertEquals(1, actualResult.get(PlanTypesEnum.DENTAL.getCode()).size());
		assertTrue(Arrays.asList(DENTAL_PLAN_1).containsAll(actualResult.get(PlanTypesEnum.DENTAL.getCode())));
	}
	
	/*
	 * When company is not renewal company, then 
	 * totalOptionCost = primarySTDPlanCost + ltdPlanCost monthlyTotalCost = (primary HC + Secondary HC) * totalOptionCost
	 * 
	 * i.e. 
	 * totalOptionCost = 4.5 + 3.5 = 8 monthlyTotalCost = (2 + 4) * 8 = 48
	 */
	@Test
	public void populatePlanOptionsCostTest1() {
		Map<String, BigDecimal> planCostMap = preparePlanCostMap(BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.5),
				BigDecimal.valueOf(3.5));
		Company company = new Company();
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setPrimaryHeadCount(2);
		activeEligibleEECount.setSecondaryHeadCount(4);
		activeEligibleEECount.setTotalHeadCount(0);
		AdditionalBenefitPlan additionalBenPlan = prepareAdditionalBenPlan(false, false);

		AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeEligibleEECount, additionalBenPlan,
				company);

		assertEquals("48.00", additionalBenPlan.getMonthlyTotalCost().toString());
	}

	/*
	 * When company is renewal company, then 
	 * totalStdCost = [(primarySTDPlanCost * primaryHC) + (secondarySTDPlanCost * secondaryHC)] / (primary HC + Secondary HC)
	 * totalOptionCost = (totalStdCost + ltdPlanCost) 
	 * monthlyTotalCost = (primary HC + Secondary HC) * totalOptionCost
	 * i.e.
	 * totalStdCost = (((4.5 * 2) + (2.5 * 2)) / (2 + 4)) = 3.2 (Round half up 3.16)
	 * totalOptionCost = 3.5 + 3.2 = 6.70
	 * monthlyTotalCost = (2 + 4) * 6.70 = 40.20
	 */
	@Test
	public void populatePlanOptionsCostTest2() {
		Map<String, BigDecimal> planCostMap = preparePlanCostMap(BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.5),
				BigDecimal.valueOf(3.5));

		Company company = new Company();
		company.setRenewalCompany(true);
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setPrimaryHeadCount(2);
		activeEligibleEECount.setSecondaryHeadCount(4);
		activeEligibleEECount.setTotalHeadCount(0);
		AdditionalBenefitPlan additionalBenPlan = prepareAdditionalBenPlan(false, false);

		AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeEligibleEECount, additionalBenPlan,
				company);

		assertEquals("40.00", additionalBenPlan.getMonthlyTotalCost().toString());
	}
	
	
	
	/*
	 * When company is not renewal company and STD is employee paid, then 
	 * totalOptionCost = ltdPlanCost 
	 * monthlyTotalCost = (primary HC + Secondary HC) * totalOptionCost
	 * 
	 * i.e. 
	 * totalOptionCost = 3.5
	 * monthlyTotalCost = (2 + 4) * 3.5 = 21
	 */
	@Test
	public void populatePlanOptionsCostTest3() {
		Map<String, BigDecimal> planCostMap = preparePlanCostMap(BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.5),
				BigDecimal.valueOf(3.5));
		Company company = new Company();
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setPrimaryHeadCount(2);
		activeEligibleEECount.setSecondaryHeadCount(4);
		activeEligibleEECount.setTotalHeadCount(0);
		AdditionalBenefitPlan additionalBenPlan = prepareAdditionalBenPlan(true, false);

		AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeEligibleEECount, additionalBenPlan,
				company);

		assertEquals("21.00", additionalBenPlan.getMonthlyTotalCost().toString());
	}
	
	/*
	 * When company is not renewal company and LTD is employee paid
	 * and totalHC is available, then 
	 * totalOptionCost = ltdPlanCost 
	 * monthlyTotalCost = (primary HC + Secondary HC) * totalOptionCost
	 * 
	 * i.e. 
	 * totalOptionCost = 4.5
	 * monthlyTotalCost = 7 * 4.5 = 27
	 */
	@Test
	public void populatePlanOptionsCostTest4() {
		Map<String, BigDecimal> planCostMap = preparePlanCostMap(BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.5),
				BigDecimal.valueOf(3.5));
		Company company = new Company();
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setPrimaryHeadCount(2);
		activeEligibleEECount.setSecondaryHeadCount(0);
		activeEligibleEECount.setTotalHeadCount(7);
		AdditionalBenefitPlan additionalBenPlan = prepareAdditionalBenPlan(false, true);

		AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeEligibleEECount, additionalBenPlan,
				company);

		assertEquals("31.50", additionalBenPlan.getMonthlyTotalCost().toString());
	}
	
	/*
	 * When company is prospect company, then 
	 * totalStdCost = [(primarySTDPlanCost * primaryHC) + (secondarySTDPlanCost * secondaryHC)] / (primary HC + Secondary HC)
	 * totalOptionCost = (totalStdCost + ltdPlanCost) 
	 * monthlyTotalCost = (primary HC + Secondary HC) * totalOptionCost
	 * i.e.
	 * totalStdCost = (((4.5 * 2) + (2.5 * 2)) / (2 + 4)) = 3.2 (Round half up 3.16)
	 * totalOptionCost = 3.5 + 3.2 = 6.70
	 * monthlyTotalCost = (2 + 4) * 6.70 = 40.20
	 */
	@Test
	public void populatePlanOptionsCostTest5() {
		Map<String, BigDecimal> planCostMap = preparePlanCostMap(BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.5),
				BigDecimal.valueOf(3.5));

		Company company = new Company();
		company.setProspectCompany(true);
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setPrimaryHeadCount(2);
		activeEligibleEECount.setSecondaryHeadCount(4);
		activeEligibleEECount.setTotalHeadCount(0);
		AdditionalBenefitPlan additionalBenPlan = prepareAdditionalBenPlan(false, false);

		AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeEligibleEECount, additionalBenPlan,
				company);

		assertEquals("40.00", additionalBenPlan.getMonthlyTotalCost().toString());
	}
	
	/*
	 * When company is prospect converted company, then 
	 * totalStdCost = [(primarySTDPlanCost * primaryHC) + (secondarySTDPlanCost * secondaryHC)] / (primary HC + Secondary HC)
	 * totalOptionCost = (totalStdCost + ltdPlanCost) 
	 * monthlyTotalCost = (primary HC + Secondary HC) * totalOptionCost
	 * i.e.
	 * totalStdCost = (((4.5 * 2) + (2.5 * 2)) / (2 + 4)) = 3.2 (Round half up 3.16)
	 * totalOptionCost = 3.5 + 3.2 = 6.70
	 * monthlyTotalCost = (2 + 4) * 6.70 = 40.20
	 */
	@Test
	public void populatePlanOptionsCostTest6() {
		Map<String, BigDecimal> planCostMap = preparePlanCostMap(BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.5),
				BigDecimal.valueOf(3.5));

		Company company = new Company();
		company.setProspectConvertedClient(true);
		ActiveEligibleEECount activeEligibleEECount = new ActiveEligibleEECount();
		activeEligibleEECount.setPrimaryHeadCount(2);
		activeEligibleEECount.setSecondaryHeadCount(4);
		activeEligibleEECount.setTotalHeadCount(0);
		AdditionalBenefitPlan additionalBenPlan = prepareAdditionalBenPlan(false, false);

		AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeEligibleEECount, additionalBenPlan,
				company);

		assertEquals("40.00", additionalBenPlan.getMonthlyTotalCost().toString());
	}

	@Test
	public void addCommuterBenefitPlanTest() {
		XbssRealmPlyrPlan commuterPlan = prepareCommuterPlan();
		Map<String, Map<String, Map<String, BenefitPlan>>> benefitGroupADPlansMap = prepareBenefitGroupADPlansMap();
		List<BenefitGroup> benefitGroups = prepareBenefitsGroupsList();
		AdditionalBenefitServiceHelper.addCommuterBenefitPlan(benefitGroupADPlansMap, commuterPlan, benefitGroups);
		
		assertEquals(4, benefitGroupADPlansMap.size());
		assertEquals(false, benefitGroupADPlansMap.containsKey(K1_GROUP_1));
		assertEquals(2, benefitGroupADPlansMap.get(K1_GROUP_2).size());
		assertEquals(3, benefitGroupADPlansMap.get(STD_GROUP_1).size());
		assertEquals(3, benefitGroupADPlansMap.get(STD_GROUP_2).size());
		assertEquals(1, benefitGroupADPlansMap.get(STD_GROUP_3).size());
		
	}

	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = AdditionalBenefitServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}
	
	private Map<String, BigDecimal> preparePlanCostMap(BigDecimal primaryStdCost, BigDecimal secondayStdCost,
			BigDecimal ltdCost) {
		Map<String, BigDecimal> planCosts = new HashMap<>();
		planCosts.put(STD_PRIMARY_PLAN_ID2, primaryStdCost);
		planCosts.put(STD_PLAN_ID1, secondayStdCost);
		planCosts.put(LTD_PLAN_ID1, ltdCost);
		return planCosts;
	}

	private AdditionalBenefitPlan prepareAdditionalBenPlan(boolean isStdEmpPaid, boolean isLtdEmpPaid) {
		AdditionalBenefitPlan plan = new AdditionalBenefitPlan();
		List<DisabilityBenefitOptionPlans> optionPlans = new ArrayList<>();
		DisabilityBenefitOptionPlans optionPlan = new DisabilityBenefitOptionPlans();
		optionPlan.setPlanType(PlanTypesEnum.STD.getCode());
		optionPlan.setPrimaryPlan(true);
		optionPlan.setId(STD_PRIMARY_PLAN_ID2);
		optionPlan.setEmployeePaid(isStdEmpPaid);
		optionPlans.add(optionPlan);

		optionPlan = new DisabilityBenefitOptionPlans();
		optionPlan.setPlanType(PlanTypesEnum.STD.getCode());
		optionPlan.setPrimaryPlan(false);
		optionPlan.setId(STD_PLAN_ID1);
		optionPlan.setEmployeePaid(isStdEmpPaid);
		optionPlans.add(optionPlan);

		optionPlan = new DisabilityBenefitOptionPlans();
		optionPlan.setPlanType(PlanTypesEnum.LTD.getCode());
		optionPlan.setPrimaryPlan(false);
		optionPlan.setId(LTD_PLAN_ID1);
		optionPlan.setEmployeePaid(isLtdEmpPaid);
		plan.setOptionPlans(optionPlans);
		optionPlans.add(optionPlan);

		plan.setOptionPlans(optionPlans);
		return plan;
	}
	
	private XbssRealmPlyrPlan prepareCommuterPlan() {
		XbssRealmPlyrPlan commuterPlan = new XbssRealmPlyrPlan();
		commuterPlan.setBandLocator("BAND_LOCATOR");
		commuterPlan.setBenefitPlan("COMMUTER_PLAN");
		commuterPlan.setHighDeductible(false);
		commuterPlan.setId(1);
		commuterPlan.setPlanType(PlanTypesEnum.CMTR.getCode());
		commuterPlan.setPortfolioId(BigDecimal.ONE);
		commuterPlan.setRealmYearId(BigDecimal.TEN);
		commuterPlan.setSitus("SITUS");
		return commuterPlan;
	}
	
	private Map<String, Map<String, Map<String, BenefitPlan>>> prepareBenefitGroupADPlansMap() {
		Map<String, Map<String, Map<String, BenefitPlan>>> returnMap = new HashMap<>();
		
		Map<String, BenefitPlan> planMap = new HashMap<>();
		planMap.put("PLAN1", new BenefitPlan());
		
		Map<String, Map<String, BenefitPlan>> planTypeMap = new HashMap<>();
		planTypeMap.put(PlanTypesEnum.LIFE.getCode(), planMap);
		planTypeMap.put(PlanTypesEnum.STD.getCode(), planMap);
		returnMap.put(K1_GROUP_2, planTypeMap);
		
		planTypeMap = new HashMap<>();
		planTypeMap.put(PlanTypesEnum.LIFE.getCode(), planMap);
		planTypeMap.put(PlanTypesEnum.STD.getCode(), planMap);
		planTypeMap.put(PlanTypesEnum.CMTR.getCode(), planMap);
		returnMap.put(STD_GROUP_1, planTypeMap);
		
		planTypeMap = new HashMap<>();
		planTypeMap.put(PlanTypesEnum.LIFE.getCode(), planMap);
		planTypeMap.put(PlanTypesEnum.STD.getCode(), planMap);
		returnMap.put(STD_GROUP_2, planTypeMap);
		
		return returnMap;
	}
	

	private List<BenefitGroup> prepareBenefitsGroupsList() {
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram(K1_GROUP_1);
		benefitGroup.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		benefitGroups.add(benefitGroup);
		
		benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram(K1_GROUP_2);
		benefitGroup.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		benefitGroups.add(benefitGroup);
		
		benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram(STD_GROUP_1);
		benefitGroup.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		benefitGroups.add(benefitGroup);
		
		benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram(STD_GROUP_2);
		benefitGroup.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		benefitGroups.add(benefitGroup);
		
		benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram(STD_GROUP_3);
		benefitGroup.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		benefitGroups.add(benefitGroup);
		
		return benefitGroups;
	}
}
