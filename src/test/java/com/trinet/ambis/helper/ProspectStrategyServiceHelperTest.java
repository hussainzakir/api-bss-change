package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.StrategyDefaultPlan;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.Constants;

@RunWith(JUnit4.class)
public class ProspectStrategyServiceHelperTest {

	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = ProspectStrategyServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * Test to create additional plan selections for life and disability
	 */
	@Test
	public void createAddtionalPlanSelectionsTest() {

		List<PlanSelection> addtionalPlanSelections = ProspectStrategyServiceHelper.createAdditionalPlanSelections(1L,
				2L, prepareStrategyDefaultPlans());
		assertEquals(1, addtionalPlanSelections.size());
	}
	
	/**
	 * Test to create Health plan selections
	 */
	@Test
	public void createHealthPlanSelectionsTest() {

		Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap = prepareHealthBenefitPlansMap();
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = prepareCoverageLevelsMap();

		List<PlanSelection> addtionalPlanSelections = ProspectStrategyServiceHelper.createDentalAndVisionPlanSelections(
				healthBenefitPlansMap, mapOfCoverageLevels, 1L, 2L, new HashMap<>(),
				Arrays.asList(BSSApplicationConstants.DENTAL_PLAN_TYPE, BSSApplicationConstants.VISION_PLAN_TYPE));
		assertEquals(2, addtionalPlanSelections.size());
	}

	/**
	 * Test to create Health plan selections for voluntary plans
	 */
	@Test
	public void createHealthPlanSelectionsForVoluntaryPlansTest() {

		Map<String, Set<StateBenefitPlan>> healthBenefitPlansMap = prepareHealthBenefitPlansMap();
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = prepareCoverageLevelsMap();

		List<PlanSelection> addtionalPlanSelections = ProspectStrategyServiceHelper.createDentalAndVisionPlanSelections(
				healthBenefitPlansMap, mapOfCoverageLevels, 1L, 2L, new HashMap<>(),
				Arrays.asList(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE,
						BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE));
		assertEquals(2, addtionalPlanSelections.size());
	}

	private Map<String, Set<StateBenefitPlan>> prepareHealthBenefitPlansMap() {
		Map<String, Set<StateBenefitPlan>> map = new HashMap<>();
		Set<StateBenefitPlan> medicalPlans = new HashSet<>();
		Set<StateBenefitPlan> dentalPlans = new HashSet<>();
		Set<StateBenefitPlan> VisonPlans = new HashSet<>();
		medicalPlans.add(prepareStateBenefitPlan(BSSApplicationConstants.FPL, "FPL_PLAN1", 1111,
				BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		medicalPlans.add(prepareStateBenefitPlan(BSSApplicationConstants.FPL, "FPL_PLAN2", 1111,
				BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		dentalPlans.add(prepareStateBenefitPlan(BSSApplicationConstants.FPL, "FPL_PLAN3", 2222,
				BSSApplicationConstants.DENTAL_PLAN_TYPE));
		dentalPlans.add(prepareStateBenefitPlan(BSSApplicationConstants.MND, "MND_PLAN1", 3333,
				BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE));
		VisonPlans.add(prepareStateBenefitPlan(BSSApplicationConstants.MND, "MND_PLAN2", 3333,
				BSSApplicationConstants.VISION_PLAN_TYPE));
		VisonPlans.add(prepareStateBenefitPlan(BSSApplicationConstants.MND, "MND_PLAN3", 4444,
				BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE));
		map.put(BSSApplicationConstants.MEDICAL, medicalPlans);
		map.put(BSSApplicationConstants.DENTAL, dentalPlans);
		map.put(BSSApplicationConstants.VISION, VisonPlans);
		return map;
	}

	private StateBenefitPlan prepareStateBenefitPlan(String category, String benPlan, long portfolioId,
			String planType) {
		StateBenefitPlan sbp = new StateBenefitPlan();
		sbp.setPlanCategory(category);
		sbp.setBenefitPlan(benPlan);
		sbp.setPortfolioId(portfolioId);
		sbp.setPlanType(planType);
		return sbp;
	}

	private Map<String, List<CoverageLevel>> prepareCoverageLevelsMap() {
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<String, List<CoverageLevel>>();
		List<CoverageLevel> coverageLevels = new ArrayList<CoverageLevel>();
		CoverageLevel coverageLevel0 = new CoverageLevel(CoverageCodesEnums.COV_ALL);
		CoverageLevel coverageLevel1 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE);
		CoverageLevel coverageLevel2 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE);
		CoverageLevel coverageLevel3 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD);
		CoverageLevel coverageLevel4 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY);
		coverageLevels
				.addAll(Arrays.asList(coverageLevel0, coverageLevel1, coverageLevel2, coverageLevel3, coverageLevel4));

		mapOfCoverageLevels.put(Constants.MEDICAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.DENTAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.VISION, coverageLevels);
		return mapOfCoverageLevels;
	}
	
	private List<StrategyDefaultPlan> prepareStrategyDefaultPlans() {
		List<StrategyDefaultPlan> results = new ArrayList<>();
		StrategyDefaultPlan defaultPlan = new StrategyDefaultPlan();
		defaultPlan.setPlanType(BSSApplicationConstants.LIFE_CODE);
		results.add(defaultPlan);
		return results;
	}

}
