package com.trinet.ambis.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BenExchngEnumsTest {

	@Test
	public void getAllQuartersTest() {
		// when
		Set<String> quarters = BenExchngEnums.getAllQuarters();
		// then
		assertNotNull(quarters);
		assertEquals(11, quarters.size());
		assertTrue(quarters.containsAll(getAllQuarters()));
	}

	@Test
	public void isValidQuarterTrueForACQuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("AC");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForSMQuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("SM");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForSYQuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("SY");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForQ1QuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("Q1");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForQ2QuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("Q2");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForQ3QuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("Q3");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForQ4QuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("Q4");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForQ5QuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("Q5");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueFor8YQuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("8Y");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterTrueForALQuarterTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("AL");
		// then
		assertTrue(isValidQuarter);
	}

	@Test
	public void isValidQuarterFalseTest() {
		// when
		boolean isValidQuarter = BenExchngEnums.isValidQuarter("ABC");
		// then
		assertFalse(isValidQuarter);
	}

	@Test
	public void getByIdTrinetITest() {
		// when
		BenExchngEnums actualResult = BenExchngEnums.getById(5L);
		// then
		assertNotNull(actualResult);
		assertEquals(5L, actualResult.getId());
		assertEquals("TriNet I", actualResult.getBenExchng());
		assertEquals("ACD", actualResult.getProduct());
		assertEquals(Set.of("AC"), actualResult.getQuarters());
		assertEquals("TNI", actualResult.getExchangeId());
		assertEquals("Exchange I", actualResult.getExchangeName());
	}

	@Test
	public void getByIdTrinetIITest() {
		// when
		BenExchngEnums actualResult = BenExchngEnums.getById(4L);
		// then
		assertNotNull(actualResult);
		assertEquals(4L, actualResult.getId());
		assertEquals("TriNet II", actualResult.getBenExchng());
		assertEquals("SOI", actualResult.getProduct());
		assertEquals(Set.of("SM", "SY"), actualResult.getQuarters());
		assertEquals("TNII", actualResult.getExchangeId());
		assertEquals("Exchange II", actualResult.getExchangeName());
	}

	@Test
	public void getByIdTrinetIIITest() {
		// when
		BenExchngEnums actualResult = BenExchngEnums.getById(3L);
		// then
		assertNotNull(actualResult);
		assertEquals(3L, actualResult.getId());
		assertEquals("TriNet III", actualResult.getBenExchng());
		assertEquals("PAS", actualResult.getProduct());
		assertEquals(Set.of("Q1", "Q2", "Q3", "Q4", "Q5"), actualResult.getQuarters());
		assertEquals("TNIII", actualResult.getExchangeId());
		assertEquals("Exchange III", actualResult.getExchangeName());
	}

	@Test
	public void getByIdTrinetIVTest() {
		// when
		BenExchngEnums actualResult = BenExchngEnums.getById(1L);
		// then
		assertNotNull(actualResult);
		assertEquals(1L, actualResult.getId());
		assertEquals("TriNet IV", actualResult.getBenExchng());
		assertEquals("AMB", actualResult.getProduct());
		assertEquals(Set.of("8Y"), actualResult.getQuarters());
		assertEquals("TNIV", actualResult.getExchangeId());
		assertEquals("Exchange IV", actualResult.getExchangeName());
	}

	@Test
	public void getByIdTrinetXITest() {
		// when
		BenExchngEnums actualResult = BenExchngEnums.getById(2L);
		// then
		assertNotNull(actualResult);
		assertEquals(2L, actualResult.getId());
		assertEquals("TriNet XI", actualResult.getBenExchng());
		assertEquals("ALP", actualResult.getProduct());
		assertEquals(Set.of("AL"), actualResult.getQuarters());
		assertEquals("TNXI", actualResult.getExchangeId());
		assertEquals("Exchange XI", actualResult.getExchangeName());
	}
	
	@Test
	public void getByBenExchangeTest() {
		// when
		BenExchngEnums exchangeTNI = BenExchngEnums.getByBenExchange("TriNet I");
		// then
		assertEquals(BenExchngEnums.TRINET_I, exchangeTNI);

		// when
		BenExchngEnums exchangeTNII = BenExchngEnums.getByBenExchange("TriNet II");
		// then
		assertEquals(BenExchngEnums.TRINET_II, exchangeTNII);

		// when
		BenExchngEnums exchangeTNIII = BenExchngEnums.getByBenExchange("TriNet III");
		// then
		assertEquals(BenExchngEnums.TRINET_III, exchangeTNIII);

		// when
		BenExchngEnums exchangeIV = BenExchngEnums.getByBenExchange("TriNet IV");
		// then
		assertEquals(BenExchngEnums.TRINET_IV, exchangeIV);

		// when
		BenExchngEnums exchangeXI = BenExchngEnums.getByBenExchange("TriNet XI");
		// then
		assertEquals(BenExchngEnums.TRINET_XI, exchangeXI);
	}

	private Set<String> getAllQuarters() {
		return Set.of("AC", "SM", "SY", "Q1", "Q2", "Q3", "Q4", "Q5", "8Y", "AL");
	}

}
