package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;


@RunWith(JUnit4.class)
public class PlanSelectionServiceHelperTest {

	private static final String STD_BENEFIT_GROUP = "STD_BENEFIT_GROUP";
	private static final String K1_BENEFIT_GROUP = "K1_BENEFIT_GROUP";
	private static final String GROUP_DENTAL_PLAN_1 = "GROUP_DENTAL_PLAN_1";
	private static final String OPTIONAL_DENTAL_PLAN_1 = "OPTIONAL_DENTAL_PLAN_1";
	private static final String GROUP_VISION_PLAN_1 = "GROUP_VISION_PLAN_1";
	private static final String OPTIONAL_VISION_PLAN_1 = "OPTIONAL_VISION_PLAN_1";
	@Test
	public void addDentalVisionPlansTest() {
		List<BenefitGroup> benefitGroupList = prepareBenefitGroupList();
		
		Map<String, Map<String, Map<String, BenefitPlan>>> bgHealthPlanMap = prepareBenefitGroupHealthPlanMap(benefitGroupList, false, false);
		
		assertEquals(0, bgHealthPlanMap.get(STD_BENEFIT_GROUP).size());
		assertEquals(0, bgHealthPlanMap.get(K1_BENEFIT_GROUP).size());
		PlanSelectionServiceHelper.addDentalVisionPlans(benefitGroupList, bgHealthPlanMap, prepareMapOfDentalVisionCovgCodes(), prepareDentalVisionPlanMap());
		assertEquals(2, bgHealthPlanMap.get(STD_BENEFIT_GROUP).size());
		assertEquals(1, bgHealthPlanMap.get(STD_BENEFIT_GROUP).get(BSSApplicationConstants.DENTAL_PLAN_TYPE).size());
		assertEquals(OPTIONAL_DENTAL_PLAN_1, bgHealthPlanMap.get(STD_BENEFIT_GROUP).get(BSSApplicationConstants.DENTAL_PLAN_TYPE).get(OPTIONAL_DENTAL_PLAN_1).getId());
		assertEquals(1, bgHealthPlanMap.get(STD_BENEFIT_GROUP).get(BSSApplicationConstants.VISION_PLAN_TYPE).size());
		assertEquals(OPTIONAL_VISION_PLAN_1, bgHealthPlanMap.get(STD_BENEFIT_GROUP).get(BSSApplicationConstants.VISION_PLAN_TYPE).get(OPTIONAL_VISION_PLAN_1).getId());
		
		assertEquals(2, bgHealthPlanMap.get(K1_BENEFIT_GROUP).size());
		assertEquals(1, bgHealthPlanMap.get(K1_BENEFIT_GROUP).get(BSSApplicationConstants.DENTAL_PLAN_TYPE).size());
		assertEquals(GROUP_DENTAL_PLAN_1, bgHealthPlanMap.get(K1_BENEFIT_GROUP).get(BSSApplicationConstants.DENTAL_PLAN_TYPE).get(GROUP_DENTAL_PLAN_1).getId());
		assertEquals(1, bgHealthPlanMap.get(K1_BENEFIT_GROUP).get(BSSApplicationConstants.VISION_PLAN_TYPE).size());
		assertEquals(GROUP_VISION_PLAN_1, bgHealthPlanMap.get(K1_BENEFIT_GROUP).get(BSSApplicationConstants.VISION_PLAN_TYPE).get(GROUP_VISION_PLAN_1).getId());
		
	}

	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = PlanSelectionServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}
	
	private XbssRealmPlyrPlan makePlyrObj(long id, long rpyId, String planType, String benefitPlan,
			long portId, String situs, String locator ) {
		XbssRealmPlyrPlan plyr = new XbssRealmPlyrPlan();
		plyr.setId( id );
		plyr.setRealmYearId( BigDecimal.valueOf( rpyId ) );
		plyr.setPlanType( planType );
		plyr.setBenefitPlan( benefitPlan );
		plyr.setPortfolioId( BigDecimal.valueOf( portId ) );
		plyr.setBandLocator( locator );
		plyr.setSitus(situs);
		return plyr;
	}
	
	
	private List<BenefitGroup> prepareBenefitGroupList() {
		List<BenefitGroup> returnList = new ArrayList<>();
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram(STD_BENEFIT_GROUP);
		benefitGroup.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		returnList.add(benefitGroup);

		benefitGroup = new BenefitGroup();
		benefitGroup.setBenefitProgram(K1_BENEFIT_GROUP);
		benefitGroup.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		returnList.add(benefitGroup);		
		
		return returnList;
	}
	
	private Map<String, Map<String, Map<String, BenefitPlan>>> prepareBenefitGroupHealthPlanMap (List<BenefitGroup> benefitGroupList, boolean includeDental, boolean includeVision) {
		Map<String, Map<String, Map<String, BenefitPlan>>> returnMap = new HashMap<>();
		
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setId("BENEFIT_PLAN");
		Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
		benefitPlanMap.put(benefitPlan.getId(), benefitPlan);
		Map<String, Map<String, BenefitPlan>> planTypePlanMap = new HashMap<>();
		if (includeDental) {
			planTypePlanMap.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, benefitPlanMap);
		}
		if (includeVision) {
			planTypePlanMap.put(BSSApplicationConstants.VISION_PLAN_TYPE, benefitPlanMap);
		}
		
		for (BenefitGroup benefitGroup : benefitGroupList) {
			String benefitProgram = benefitGroup.getBenefitProgram();
			returnMap.put(benefitProgram, new HashMap<>(planTypePlanMap));
		}
		return returnMap;
	}
	
	private Map<String, List<CoverageLevel>> prepareMapOfDentalVisionCovgCodes() {
		Map<String, List<CoverageLevel>> planCovgCode = new HashMap<>();
		List<CoverageLevel> covgCodes = new ArrayList<>();
		covgCodes.add(new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE.getCode(), CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.ZERO));
		covgCodes.add(new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode(), CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.ZERO));
		covgCodes.add(new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode(), CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.ZERO));
		covgCodes.add(new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode(), CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), BigDecimal.ZERO));
		planCovgCode.put(BSSApplicationConstants.DENTAL, covgCodes);
		planCovgCode.put(BSSApplicationConstants.VISION, covgCodes);
		return planCovgCode;
	}
	
	private Map<String, List<XbssRealmPlyrPlan>> prepareDentalVisionPlanMap() {
		Map<String, List<XbssRealmPlyrPlan>> dentalVisionPlanMap = new HashMap<>();
		XbssRealmPlyrPlan plan;
		plan = this.makePlyrObj(1, 1, BSSApplicationConstants.DENTAL_PLAN_TYPE, GROUP_DENTAL_PLAN_1, 0, "", "");
		dentalVisionPlanMap.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, Arrays.asList(plan));
		plan = this.makePlyrObj(2, 1, BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, OPTIONAL_DENTAL_PLAN_1, 0, "", "");
		dentalVisionPlanMap.put(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, Arrays.asList(plan));
		plan = this.makePlyrObj(3, 1, BSSApplicationConstants.VISION_PLAN_TYPE, GROUP_VISION_PLAN_1, 0, "", "");
		dentalVisionPlanMap.put(BSSApplicationConstants.VISION_PLAN_TYPE, Arrays.asList(plan));
		plan = this.makePlyrObj(4, 1, BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, OPTIONAL_VISION_PLAN_1, 0, "", "");
		dentalVisionPlanMap.put(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, Arrays.asList(plan));

		return dentalVisionPlanMap;
	}
}
