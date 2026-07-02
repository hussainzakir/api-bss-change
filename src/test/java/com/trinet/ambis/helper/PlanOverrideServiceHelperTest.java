package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.service.unit.ServiceUnitTest;

/**
 * @author hliddle
 *
 */
@RunWith(JUnit4.class)
@WebAppConfiguration
public class PlanOverrideServiceHelperTest extends ServiceUnitTest {
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getRenewalPlanOverrideType() {

		String actualResult;

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FLTEE, BSSApplicationConstants.PLAN_OVERRIDE_PCT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FLTEE, actualResult);

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FPL, BSSApplicationConstants.PLAN_OVERRIDE_FLT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FLT, actualResult);

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FPL_FLT, BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FLT, actualResult);

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FPL_PCT, BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_PCT, actualResult);

	}

	@Test
	public void getRenewalPlanOverrideTypeNoDefault() {

		String actualResult;

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FLTEE);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FLTEE, actualResult);

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FPL);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_BASE, actualResult);

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FPL_FLT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FLT, actualResult);

		actualResult = PlanOverrideServiceHelper.getRenewalPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FPL_PCT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_PCT, actualResult);

	}	

	@Test
	public void getMNFPlanOverrideType() {

		String actualResult;

		actualResult = PlanOverrideServiceHelper.getMNFPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FPL);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_MNF, actualResult);

		actualResult = PlanOverrideServiceHelper.getMNFPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FLT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_MNF_FLT, actualResult);

		actualResult = PlanOverrideServiceHelper.getMNFPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_PCT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_MNF_PCT, actualResult);

	}

	@Test
	public void getFPLPlanOverrideType() {

		String actualResult;

		actualResult = PlanOverrideServiceHelper.getFPLPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_MNF);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FPL, actualResult);

		actualResult = PlanOverrideServiceHelper.getFPLPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_FLT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FPL_FLT, actualResult);

		actualResult = PlanOverrideServiceHelper.getFPLPlanOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_PCT);
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FPL_PCT, actualResult);

	}
	
	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = PlanOverrideServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}

}