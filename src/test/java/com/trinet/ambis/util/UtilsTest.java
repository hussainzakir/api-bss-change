package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class UtilsTest {

	String dateString = "15-Jan-2019";
	String formatString = "dd-MMM-yyyy";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void converStringToDate() {

		Date actualResult;
		Calendar calendar = Calendar.getInstance();

		actualResult = Utils.convertStringToDate(dateString, formatString);
		calendar.setTime(actualResult);
		assertEquals(0, calendar.get(Calendar.MONTH));
		assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH));
		assertEquals(2019, calendar.get(Calendar.YEAR));

		actualResult = Utils.convertStringToDate("99-999-9999", formatString);
		assertNull(actualResult);
	}

	@Test
	public void convertStringToLocalDate() {

		LocalDate actualResult;

		actualResult = Utils.convertStringToLocalDate(dateString, formatString);
		assertEquals(1, actualResult.getMonthValue());
		assertEquals(15, actualResult.getDayOfMonth());
		assertEquals(2019, actualResult.getYear());

		actualResult = Utils.convertStringToLocalDate("99-999-9999", formatString);
		assertNull(actualResult);
	}

	@Test
	public void convertDateToString() throws ParseException {

		Date date = new SimpleDateFormat(formatString, Locale.ENGLISH).parse(dateString);
		String actualResult;

		actualResult = Utils.convertDateToString(date, formatString);
		assertEquals(dateString, actualResult);
	}

	@Test
	public void convertDateToStringNoFormatter() throws ParseException {

		Date date = new SimpleDateFormat(formatString, Locale.ENGLISH).parse(dateString);
		String actualResult;

		actualResult = Utils.convertDateToString(date);
		assertEquals(dateString, actualResult);
	}

	@Test
	public void convertDateToEmailString() throws ParseException {

		Date date = new SimpleDateFormat(formatString, Locale.ENGLISH).parse(dateString);
		String actualResult;

		actualResult = Utils.convertDateToEmailString(date);
		assertEquals("January 15, 2019", actualResult);
	}

	@Test
	public void isPremium() {

		boolean actualResult;

		actualResult = Utils.isPremium("000SR3");
		assertTrue(actualResult);

		actualResult = Utils.isPremium("INVALID");
		assertFalse(actualResult);
	}

	@Test
	public void addWeekDaysToDate() throws ParseException {

		Date date = new SimpleDateFormat(formatString, Locale.ENGLISH).parse(dateString);
		Date actualResult;
		Calendar calendar = Calendar.getInstance();

		/*
		 * Should return 24-JAN-2019
		 */
		actualResult = Utils.addWeekDaysToDate(date, 7);
		calendar.setTime(actualResult);
		assertEquals(0, calendar.get(Calendar.MONTH));
		assertEquals(24, calendar.get(Calendar.DAY_OF_MONTH));
		assertEquals(2019, calendar.get(Calendar.YEAR));

		/*
		 * Should return 4-FEB-2019
		 */
		actualResult = Utils.addWeekDaysToDate(date, 14);
		calendar.setTime(actualResult);
		assertEquals(1, calendar.get(Calendar.MONTH));
		assertEquals(4, calendar.get(Calendar.DAY_OF_MONTH));
		assertEquals(2019, calendar.get(Calendar.YEAR));

		/*
		 * Should return 9-JUN-2020
		 */
		actualResult = Utils.addWeekDaysToDate(date, 365);
		calendar.setTime(actualResult);
		assertEquals(5, calendar.get(Calendar.MONTH));
		assertEquals(9, calendar.get(Calendar.DAY_OF_MONTH));
		assertEquals(2020, calendar.get(Calendar.YEAR));
	}
	
	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = Utils.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}

}
