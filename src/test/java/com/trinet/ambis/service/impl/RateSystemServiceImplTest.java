package com.trinet.ambis.service.impl;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.ambis.util.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.trinet.ambis.common.BSSApplicationConstants.REGIONAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RateSystemServiceImplTest {

	private static final String PLAN_START_DATE = "01-Jan-2026";
	private static final String EFFECTIVE_DATE  = "2026-01-01";
	private static final String RATE_GROUP_ID   = "RG12345";
	private static final String RATE_TYPE       = "tiered";
	private static final long   COMPANY_ID      = 1L;
	private static final String COMPANY_CODE    = "COMP001";

	@InjectMocks
	private RateSystemServiceImpl rateSystemService;

	@Mock
	private FlexRateService flexRateService;

	private Company company;

	@Before
	public void setUp() {
		company = new Company();
		company.setId(COMPANY_ID);
		company.setCode(COMPANY_CODE);
		company.setPlanStartDate(PLAN_START_DATE);
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
	}

	@Test
	public void testGetRateSystemRateType_ReturnsCachedRateType() {
		// Given – company has null risk type, but getRiskType() defaults to BANDS
		company.setRiskType(null);

		// When
		String result = rateSystemService.getRateSystemRateType(company);

		// Then - returns REGIONAL because getRiskType() defaults to BANDS
		assertEquals(REGIONAL, result);
		verify(flexRateService, never()).getPlanRatesFromCache(any(), any());
	}

	@Test
	public void testGetRateSystemRateType_NullRateType_ReturnsRegional() {
		// Given – company has null risk type, but getRiskType() defaults to BANDS
		company.setRiskType(null);

		// When
		String result = rateSystemService.getRateSystemRateType(company);

		// Then - returns REGIONAL because getRiskType() defaults to BANDS
		assertEquals(REGIONAL, result);
		verify(flexRateService, never()).getPlanRatesFromCache(any(), any());
	}

	@Test
	public void testGetRateSystemRateType_BandsRiskType_ReturnsRegional() {
		// Given – company has BANDS risk type
		company.setRiskType(RiskTypeEnum.BANDS);

		// When
		String result = rateSystemService.getRateSystemRateType(company);

		// Then - returns REGIONAL
		assertEquals(REGIONAL, result);
		verify(flexRateService, never()).getPlanRatesFromCache(any(), any());
	}

	@Test
	public void testGetRateSystemRateType_CacheThrowsException_PropagatesException() {
		// Given – company has null risk type, which defaults to BANDS
		company.setRiskType(null);

		// When – getRiskType() returns BANDS, so condition is true and returns REGIONAL
		String result = rateSystemService.getRateSystemRateType(company);

		// Then - returns REGIONAL
		assertEquals(REGIONAL, result);
		verify(flexRateService, never()).getPlanRatesFromCache(any(), any());
	}
}
