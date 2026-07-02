package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.trinet.ambis.enums.CacheObjectTypeEnum;


@RunWith(JUnit4.class)


public class CacheKeyGeneratorTest {
	
	private static final String UNIQUE_ID = "1111";

	@Test
	public void constructCacheKeyForRenewalsPlanRates() {
		String actual = CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE, UNIQUE_ID);
		assertEquals("BSS:PLAN-RATES:1111", actual);
	}
	
	@Test
	public void constructCacheKeyForRenewalsBenPlans() {
		String actual = CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE, UNIQUE_ID);
		assertEquals("BSS:BENEFIT-PLANS:1111", actual);
	}

	@Test
	public void constructCacheKeyForOmsBenPlans() {
		String actual = CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.OMS_BENEFIT_PLAN_RATES, UNIQUE_ID);
		assertEquals("BSS:OMS-BENEFIT-PLAN-RATES:1111", actual);
	}
	
	@Test
	public void constructCacheKeyForAll() {
		String actual = CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.ALL, UNIQUE_ID);
		assertEquals("BSS:1111", actual);
	}

	@Test
	public void constructCacheKeyForStrategyData() {
		String actual = CacheKeyGenerator.generateCacheKey(CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE, UNIQUE_ID);
		assertEquals("BSS:STRATEGY_DATA:1111", actual);
	}
	
}
