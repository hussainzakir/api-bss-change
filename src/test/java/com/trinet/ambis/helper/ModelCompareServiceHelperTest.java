package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.enums.StrategyTypesEnums;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.unit.ServiceUnitTest;

/**
 * @author hliddle
 *
 */

@RunWith(JUnit4.class)
@WebAppConfiguration
public class ModelCompareServiceHelperTest extends ServiceUnitTest {
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}	

	@Test
	public void splitStrategyList() {

		String strategyListString = null;

		Map<Long, List<Long>> actualResult;

		/*
		 * Test when the string is null
		 */
		actualResult = ModelCompareServiceHelper.splitStrategyList(strategyListString);
		assertEquals(0, actualResult.size());

		/*
		 * Test when the string is empty
		 */
		strategyListString = "";
		actualResult = ModelCompareServiceHelper.splitStrategyList(strategyListString);
		assertEquals(0, actualResult.size());

		/*
		 * Test when the string has 3 strategies
		 */
		strategyListString = "1,2,3";
		actualResult = ModelCompareServiceHelper.splitStrategyList(strategyListString);
		assertEquals(1, actualResult.size());
		assertEquals(1, actualResult.keySet().iterator().next().longValue());
		assertEquals(2, actualResult.get(1L).get(0).longValue());
	}
	
	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = ModelCompareServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void getStrategyDisplayName() {

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearStart(java.sql.Date.valueOf("2021-01-01"));
		realmPlanYear.setPlanYearEnd(java.sql.Date.valueOf("2021-12-31"));

		String actualResult;

		/*
		 * Test history
		 */
		actualResult = ModelCompareServiceHelper.getStrategyDisplayName("Submitted Strategy Name", realmPlanYear, true, false);
		assertEquals("Current Strategy 01/01/2021 - 12/31/2021", actualResult);

		/*
		 * Test default strategy
		 */
		actualResult = ModelCompareServiceHelper.getStrategyDisplayName(StrategyTypesEnums.F_S.getName(), realmPlanYear,
				false, false);
		assertEquals("Current Strategy 01/01/2021 - 12/31/2021 (Updated Rates)", actualResult);

		/*
		 * Test strategy with custom name
		 */
		actualResult = ModelCompareServiceHelper.getStrategyDisplayName("Custom Strategy", realmPlanYear,
				false, false);
		assertEquals("Custom Strategy", actualResult);

		/*
		 * Test prospect default strategy
		 */
		actualResult = ModelCompareServiceHelper.getStrategyDisplayName("Current Strategy", realmPlanYear, false, true);
		assertEquals("Current Strategy", actualResult);

		/*
		 * Test prospect strategy with custom name
		 */
		actualResult = ModelCompareServiceHelper.getStrategyDisplayName("Custom Strategy", realmPlanYear,
				false, true);
		assertEquals("Custom Strategy", actualResult);

	}

}
