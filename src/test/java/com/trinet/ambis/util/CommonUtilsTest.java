package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.CompanyNotFound;
import com.trinet.common.DateUtils;
import com.trinet.exception.TriNetParseException;

@RunWith(JUnit4.class)
public class CommonUtilsTest {


    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);	

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void logExceptions() {
		
		String companyCode = "TEST";
		String emplId = "0000000123456";
		
		BSSApplicationException bssApplicationException;
		CompanyNotFound companyNotFound = new CompanyNotFound();

		/*
		 * Test with BSSApplicationException - null Cause
		 */
		bssApplicationException = new BSSApplicationException();
		CommonUtils.logExceptions(bssApplicationException, logger, companyCode, emplId);

		/*
		 * Test with BSSApplicationException - not null Cause
		 */
		Throwable throwable = new Throwable("TESTING ERROR");
		BSSApplicationError  error = new BSSApplicationError("ERROR MESSAGE");
		
		bssApplicationException = new BSSApplicationException(throwable, error);
		CommonUtils.logExceptions(bssApplicationException, logger, companyCode, emplId);

		/*
		 * Test with CompanyNotFound exception
		 */
		CommonUtils.logExceptions(companyNotFound, logger, companyCode, emplId);
	}

	@Test
	public void nullStringToDateTest() {
		String dateString = null;
		Date endDate = CommonUtils.formatStringToDate( dateString, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY );
		assertTrue( endDate == null );
	}

	@Test
	public void stringToDateTest() {
		String dateString = "01-JAN-2020";
		Date expectedDate = java.sql.Date.valueOf( "2020-01-01" );
		Date endDate = CommonUtils.formatStringToDate( dateString, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY );
		assertTrue( endDate.equals( expectedDate ) );
	}

	@Test
	public void nullDateToStringTest() {
		Date startDate = null;
		String formattedDate = CommonUtils.formatDateToString( startDate, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY );
		assertTrue( "".equalsIgnoreCase( formattedDate ) );
	}

	@Test
	public void dateToStringTest() {
		Date startDate = java.sql.Date.valueOf( "2009-07-03" );
		String expectedString = "03-JUL-2009";
		String formattedDate = CommonUtils.formatDateToString( startDate, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY );
		assertTrue( formattedDate.equalsIgnoreCase( expectedString ) );
	}

	@Test
	public void getBucketedList() {
		List<String> input = null;
		Collection<List<String>> result = CommonUtils.getBucketedList(input, 3);
		
		assertEquals(0, result.size());
		
		input = Collections.emptyList();
		result = CommonUtils.getBucketedList(input, 3);
		
		assertEquals(0, result.size());
		
		input = Arrays.asList("Str1","Str2","Str3","Str4","Str5","Str6","Str7");
		result = CommonUtils.getBucketedList(input, 3);
		
		assertEquals(3, result.size());
		for (List<String> list : result) {
			if(list.contains("Str1")) {
				assertEquals(Arrays.asList("Str1","Str2","Str3"), list);
			}
			if(list.contains("Str4")) {
				assertEquals(Arrays.asList("Str4","Str5","Str6"), list);
			}
			if(list.contains("Str7")) {
				assertEquals(Arrays.asList("Str7"), list);
			}
		} 
	}
	
	// choose lesser date -- both args null
	@Test
	public void chooseLesserDateTest1() throws TriNetParseException {
		Date firstDate = null;
		Date secondDate = null;

		Date endDate = CommonUtils.chooseLesserDate( firstDate, secondDate );
		assertTrue( endDate == null );
	}

	// choose lesser date -- first arg null
	@Test
	public void chooseLesserDateTest2() throws TriNetParseException {
		Date firstDate = null;
		Date secondDate = DateUtils.getDateFromString("2020-01-03");

		Date endDate = CommonUtils.chooseLesserDate( firstDate, secondDate );
		assertTrue( endDate.equals(  secondDate ) );
	}

	// choose lesser date -- second arg null
	@Test
	public void chooseLesserDateTest3() throws TriNetParseException {
		Date firstDate = DateUtils.getDateFromString("2020-01-03");
		Date secondDate = null;

		Date endDate = CommonUtils.chooseLesserDate( firstDate, secondDate );
		assertTrue( endDate.equals( firstDate ) );
	}

	// choose lesser date - first date wins
	@Test
	public void chooseLesserDateTest4() throws TriNetParseException {
		Date firstDate = DateUtils.getDateFromString("2020-01-03");
		Date secondDate = DateUtils.getDateFromString("2020-04-01");

		Date endDate = CommonUtils.chooseLesserDate( firstDate, secondDate );
		assertTrue( endDate.equals( firstDate ) );
	}

	// choose lesser date - second date wins
	@Test
	public void chooseLesserDateTest5() throws TriNetParseException {
		Date firstDate = DateUtils.getDateFromString("2020-01-03");
		Date secondDate = DateUtils.getDateFromString("1990-09-24");

		Date endDate = CommonUtils.chooseLesserDate( firstDate, secondDate );
		assertTrue( endDate.equals( secondDate ) );
	}

	// choose lesser date - args equal
	@Test
	public void chooseLesserDateTest6() throws TriNetParseException {
		Date firstDate = DateUtils.getDateFromString("1990-09-24");
		Date secondDate = DateUtils.getDateFromString("1990-09-24");

		Date endDate = CommonUtils.chooseLesserDate( firstDate, secondDate );
		assertTrue( endDate.equals( firstDate ) && endDate.equals( secondDate ) );
	}


	// choose date -- both args null
	@Test
	public void chooseGreaterDateTest1() throws TriNetParseException {
		Date dateToTest = null;
		Date minimumDate = null;
		
		Date endDate = CommonUtils.chooseGreaterDate( dateToTest, minimumDate );
		assertTrue( endDate == null );
	}
	
	// choose date -- first arg null
	@Test
	public void chooseGreaterDateTest2() throws TriNetParseException {
		Date dateToTest = null;
		Date minimumDate = DateUtils.getDateFromString("2020-01-03");
		
		Date endDate = CommonUtils.chooseGreaterDate( dateToTest, minimumDate );
		assertTrue( endDate.equals(  minimumDate ) );
	}
	
	// choose date -- second arg null
	@Test
	public void chooseGreaterDateTest3() throws TriNetParseException {
		Date dateToTest = DateUtils.getDateFromString("2020-01-03");
		Date minimumDate = null;
		
		Date endDate = CommonUtils.chooseGreaterDate( dateToTest, minimumDate );
		assertTrue( endDate.equals( dateToTest ) );
	}
	
	// choose date against minimum - first date wins
	@Test
	public void chooseGreaterDateTest4() throws TriNetParseException {
		Date dateToTest = DateUtils.getDateFromString("2020-01-03");
		Date minimumDate = DateUtils.getDateFromString("2020-04-01");
		
		Date endDate = CommonUtils.chooseGreaterDate( dateToTest, minimumDate );
		assertTrue( endDate.equals( minimumDate ) );
	}
	
	// choose date against minimum - second date wins
	@Test
	public void chooseGreaterDateTest5() throws TriNetParseException {
		Date dateToTest = DateUtils.getDateFromString("2020-01-03");
		Date minimumDate = DateUtils.getDateFromString("1990-09-24");
		
		Date endDate = CommonUtils.chooseGreaterDate( dateToTest, minimumDate );
		assertTrue( endDate.equals( dateToTest ) );
	}
	
	// choose date against minimum - args equal
	@Test
	public void chooseGreaterDateTest6() throws TriNetParseException {
		Date dateToTest = DateUtils.getDateFromString("1990-09-24");
		Date minimumDate = DateUtils.getDateFromString("1990-09-24");
		
		Date endDate = CommonUtils.chooseGreaterDate( dateToTest, minimumDate );
		assertTrue( endDate.equals( dateToTest ) );
	}
	
	
	// dateToVerify is between start and end date.
	@Test
	public void checkIfDateIsInRangeInclusive_test1() throws TriNetParseException {
		Date dateToVerify = DateUtils.getDateFromString("2020-01-02");
		Date startDate = DateUtils.getDateFromString("2020-01-01");
		Date endDate = DateUtils.getDateFromString("2020-01-04");
		boolean result = CommonUtils.checkIfDateIsInRangeInclusive(dateToVerify, startDate, endDate);
		assertTrue(result);
	}
	
	// dateToVerify is equals to start date.
	@Test
	public void checkIfDateIsInRangeInclusive_test2() throws TriNetParseException {
		Date dateToVerify = DateUtils.getDateFromString("2020-01-02");
		Date startDate = DateUtils.getDateFromString("2020-01-02");
		Date endDate = DateUtils.getDateFromString("2020-01-04");
		boolean result = CommonUtils.checkIfDateIsInRangeInclusive(dateToVerify, startDate, endDate);
		assertTrue(result);
	}
	
	// dateToVerify is equals to end date.
	@Test
	public void checkIfDateIsInRangeInclusive_test3() throws TriNetParseException {
		Date dateToVerify = DateUtils.getDateFromString("2020-01-04");
		Date startDate = DateUtils.getDateFromString("2019-01-02");
		Date endDate = DateUtils.getDateFromString("2020-01-04");
		boolean result = CommonUtils.checkIfDateIsInRangeInclusive(dateToVerify, startDate, endDate);
		assertTrue(result);
	}
	
	// dateToVerify is greater than start and end date
	@Test
	public void checkIfDateIsInRangeInclusive_test4() throws TriNetParseException {
		Date dateToVerify = DateUtils.getDateFromString("2020-01-09");
		Date startDate = DateUtils.getDateFromString("2020-01-05");
		Date endDate = DateUtils.getDateFromString("2020-01-07");
		boolean result = CommonUtils.checkIfDateIsInRangeInclusive(dateToVerify, startDate, endDate);
		assertFalse(result);
	}
	
	// dateToVerify is less than start and end date
	@Test
	public void checkIfDateIsInRangeInclusive_test5() throws TriNetParseException {
		Date dateToVerify = DateUtils.getDateFromString("2020-01-02");
		Date startDate = DateUtils.getDateFromString("2020-01-05");
		Date endDate = DateUtils.getDateFromString("2020-01-07");
		boolean result = CommonUtils.checkIfDateIsInRangeInclusive(dateToVerify, startDate, endDate);
		assertFalse(result);
	}
	
	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = CommonUtils.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}
}
