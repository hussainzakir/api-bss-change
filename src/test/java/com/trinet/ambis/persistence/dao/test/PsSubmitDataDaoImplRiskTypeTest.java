package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.persistence.dao.ps.impl.BnRateDataForInsert;

import org.junit.Test;

/**
 * Unit tests for BnRateDataForInsert model and risk type functionality.
 * Tests that fields like payInRate and t2ProvCovrgRate can be set and retrieved correctly.
 */
public class PsSubmitDataDaoImplRiskTypeTest {

	@Test
	public void testT2ProvCovrgRate_IsSetCorrectly() {
		// Verify that t2ProvCovrgRate field is populated correctly
		BnRateDataForInsert rateData = new BnRateDataForInsert();
		BigDecimal testRate = BigDecimal.valueOf(150.50);
		rateData.setT2ProvCovrgRate(testRate);
		assertEquals(testRate, rateData.getT2ProvCovrgRate());
	}

	@Test
	public void testBnRateDataForInsert_AllFieldsSetCorrectly() {
		// Comprehensive test of BnRateDataForInsert model
		BnRateDataForInsert rateData = new BnRateDataForInsert();
		
		rateData.setRateTblId("RATE123");
		rateData.setBnRateKey01("KEY01");
		rateData.setBnRateKey02("KEY02");
		rateData.setBnEmplRate(BigDecimal.valueOf(50.00));
		rateData.setBnEmplrRate(BigDecimal.valueOf(150.00));
		rateData.setT2ProvCovrgRate(BigDecimal.valueOf(75.00));
		rateData.setPfClient("CLIENT1");
		rateData.setPlanType("20");
		rateData.setBandCode("2");

		assertEquals("RATE123", rateData.getRateTblId());
		assertEquals("KEY01", rateData.getBnRateKey01());
		assertEquals("KEY02", rateData.getBnRateKey02());
		assertEquals(BigDecimal.valueOf(50.00), rateData.getBnEmplRate());
		assertEquals(BigDecimal.valueOf(150.00), rateData.getBnEmplrRate());
		assertEquals(BigDecimal.valueOf(75.00), rateData.getT2ProvCovrgRate());
		assertEquals("CLIENT1", rateData.getPfClient());
		assertEquals("20", rateData.getPlanType());
		assertEquals("2", rateData.getBandCode());
	}

	@Test
	public void testRiskTypeEnum_Values() {
		// Verify RiskTypeEnum has expected values
		assertEquals("BANDS", RiskTypeEnum.BANDS.name());
		assertEquals("DIFFERENTIALS", RiskTypeEnum.DIFFERENTIALS.name());
	}
}
