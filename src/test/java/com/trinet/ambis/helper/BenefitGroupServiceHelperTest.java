package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;

/**
 * @author schaudhari
 *
 */

@RunWith(JUnit4.class)


public class BenefitGroupServiceHelperTest {

	@Test
	public void prepareStrategyGroupHeadCountObj() {
		Map<String, Integer> covrgLvlHeadCounts = new HashMap<String, Integer>();
		covrgLvlHeadCounts.put("employee", 2);
		covrgLvlHeadCounts.put("employeePlusSpouse", 4);
		covrgLvlHeadCounts.put("employeePlusChild", 1);
		covrgLvlHeadCounts.put("employeePlusFamily", 0);
		long bsgId = 1111;

		List<StrategyGroupHeadCount> actualResult = BenefitGroupServiceHelper
				.prepareStrategyGroupHeadCountObj(covrgLvlHeadCounts, bsgId);

		assertEquals(4, actualResult.size());
		for (StrategyGroupHeadCount strategyGroupHeadCount : actualResult) {
			assertEquals(1111, strategyGroupHeadCount.getId().getStrategyGroupId());
			if ("1".equals(strategyGroupHeadCount.getId().getCovrgCd())) {
				assertEquals(2, strategyGroupHeadCount.getHeadcount());
			} else if ("2".equals(strategyGroupHeadCount.getId().getCovrgCd())) {
				assertEquals(4, strategyGroupHeadCount.getHeadcount());
			} else if ("C".equals(strategyGroupHeadCount.getId().getCovrgCd())) {
				assertEquals(1, strategyGroupHeadCount.getHeadcount());
			} else if ("4".equals(strategyGroupHeadCount.getId().getCovrgCd())) {
				assertEquals(0, strategyGroupHeadCount.getHeadcount());
			}
		}
	}
	
	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = BenefitGroupServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}
}
