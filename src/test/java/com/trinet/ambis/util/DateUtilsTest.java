package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.LocalDate;

@RunWith(JUnit4.class)
public class DateUtilsTest {

	/**
	 * when getCurrentDate() method is called </br>
	 * then return the string in yyyy_MM_dd format
	 * 
	 */
	@Test
	public void getCurrentDateTest() {
		// when
		String actualResult = DateUtils.getCurrentDate();
		// then
		// assertions
		assertNotNull(actualResult);
	}

	@Test
	public void calculateAgeUntilDate() {
		// when - The age calculation date is the same as dob
		int actualResult = DateUtils.calculateAgeUntilDate(LocalDate.of(2000, 1, 1),
				LocalDate.of(2020, 1, 1));
		// then
		// assertions
		assertEquals(20, actualResult);

		// when - The age calculation date is before dob
		actualResult = DateUtils.calculateAgeUntilDate(LocalDate.of(2025, 1, 1),
				LocalDate.of(2020, 1, 1));
		// then
		// assertions
		assertEquals(-1, actualResult);

		// when - The age calculation date is after dob
		actualResult = DateUtils.calculateAgeUntilDate(LocalDate.of(2000, 12, 31),
				LocalDate.of(2020, 1, 1));
		// then
		// assertions
		assertEquals(19, actualResult);
	}

	@Test
	public void testCreateDate_ValidBasicIso() {
		assertEquals("08-Sep-2025", DateUtils.createDate("20250908"));
		assertEquals("29-Feb-2020", DateUtils.createDate("20200229"));
		assertEquals("31-Dec-2023", DateUtils.createDate("20231231"));
	}

	@Test(expected = java.time.format.DateTimeParseException.class)
	public void testCreateDate_InvalidFormat() {
		DateUtils.createDate("2025-09-08"); // Not BASIC_ISO_DATE
	}

	@Test(expected = java.time.format.DateTimeParseException.class)
	public void testCreateDate_CompletelyInvalid() {
		DateUtils.createDate("invalid");
	}

	@Test
	public void testExtractYear_Valid() {
		assertEquals("2025", DateUtils.extractYear("08-Sep-2025"));
		assertEquals("2020", DateUtils.extractYear("29-Feb-2020"));
		assertEquals("2023", DateUtils.extractYear("31-Dec-2023"));
	}

	@Test
	public void testExtractYear_InvalidFormat() {
		assertEquals(" ", DateUtils.extractYear("2025-09-08")); // Not dd-MMM-yyyy
		assertEquals(" ", DateUtils.extractYear("invalid"));
		assertEquals(" ", DateUtils.extractYear(""));
	}

    @Test
    public void testIsIsoDate_Valid() {
        assertTrue(DateUtils.isIsoDate("2024-06-01"));
        assertTrue(DateUtils.isIsoDate("2000-01-01"));
        assertTrue(DateUtils.isIsoDate("2000-02-29"));
    }

    @Test
    public void testIsIsoDate_Invalid() {
        assertFalse(DateUtils.isIsoDate("2001-02-29"));
        assertFalse(DateUtils.isIsoDate("20240601"));
        assertFalse(DateUtils.isIsoDate("01-06-2024"));
        assertFalse(DateUtils.isIsoDate("invalid"));
        assertFalse(DateUtils.isIsoDate(""));
        assertFalse(DateUtils.isIsoDate(null));
    }
}
