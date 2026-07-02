package com.trinet.ambis.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class RangeUtilsTest extends ServiceUnitTest {

	/**
	 * given valid valueStr, min as null, max as null <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest1() {
		// given
		// data
		String valueStr = "$1,000";
		BigDecimal min = null;
		BigDecimal max = null;
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr equal to min , max as null <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest2() {
		// given
		// data
		String valueStr = "$1,000";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = null;
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr greater than min , max as null <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest3() {
		// given
		// data
		String valueStr = "$1,001";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = null;
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr lesser than min , max as null <br>
	 * when isInRange method is called<br>
	 * then return false
	 * 
	 */
	@Test
	public void isInRangeTest4() {
		// given
		// data
		String valueStr = "$999";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = null;
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertFalse(actualResult);
	}

	/**
	 * given valid valueStr, valueStr equal to max , min as null <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest5() {
		// given
		// data
		String valueStr = "$1,000";
		BigDecimal max = new BigDecimal("1000");
		BigDecimal min = null;
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr greater than max , min as null <br>
	 * when isInRange method is called<br>
	 * then return false
	 * 
	 */
	@Test
	public void isInRangeTest6() {
		// given
		// data
		String valueStr = "$1,001";
		BigDecimal max = new BigDecimal("1000");
		BigDecimal min = null;
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertFalse(actualResult);
	}

	/**
	 * given valid valueStr, valueStr lesser than max , min as null <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest7() {
		// given
		// data
		String valueStr = "$999";
		BigDecimal max = new BigDecimal("1000");
		BigDecimal min = null;
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr equal to min , valueStr lesser than max <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest8() {
		// given
		// data
		String valueStr = "$1,000";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr greater than min , valueStr lesser than max
	 * <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest9() {
		// given
		// data
		String valueStr = "$1,001";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr lesser than min , valueStr lesser than max
	 * <br>
	 * when isInRange method is called<br>
	 * then return false
	 * 
	 */
	@Test
	public void isInRangeTest10() {
		// given
		// data
		String valueStr = "$999";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertFalse(actualResult);
	}

	/**
	 * given valid valueStr, valueStr equal to max , valueStr greater than min <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest11() {
		// given
		// data
		String valueStr = "$2,000";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given valid valueStr, valueStr greater than max , valueStr greater than min
	 * <br>
	 * when isInRange method is called<br>
	 * then return false
	 * 
	 */
	@Test
	public void isInRangeTest12() {
		// given
		// data
		String valueStr = "$2,001";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertFalse(actualResult);
	}

	/**
	 * given valid valueStr, valueStr lesser than max , valueStr greater than min
	 * <br>
	 * when isInRange method is called<br>
	 * then return true
	 * 
	 */
	@Test
	public void isInRangeTest13() {
		// given
		// data
		String valueStr = "$1999";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertTrue(actualResult);
	}

	/**
	 * given invalid valueStr <br>
	 * when isInRange method is called<br>
	 * then throw exception
	 * 
	 */
	@Test
	public void isInRangeTest14() {
		// given
		// data
		String valueStr = "";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		RangeUtils.isInRange(valueStr, min, max);
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertFalse(actualResult);
	}

	/**
	 * given invalid valueStr <br>
	 * when isInRange method is called<br>
	 * then return false
	 * 
	 */
	@Test
	public void isInRangeTest15() {
		// given
		// data
		String valueStr = "-1";
		BigDecimal min = new BigDecimal("1000");
		BigDecimal max = new BigDecimal("2000");
		// when
		boolean actualResult = RangeUtils.isInRange(valueStr, min, max);
		// then
		// assertions
		assertFalse(actualResult);

	}

}
