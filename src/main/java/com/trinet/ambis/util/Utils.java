package com.trinet.ambis.util;

import static com.trinet.ambis.util.Constants.premiumPlanTypeList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSApplicationConstants;

public class Utils {
	
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);
	
	private Utils() {
		throw new IllegalStateException(
				"Utility class " + Utils.class.getName() + " can not be instantiated.");
	}
	
	public static Date convertStringToDate(String dateStr, String formatStr) {
		Date date = null;
		try {
			date = new SimpleDateFormat(formatStr, Locale.ENGLISH).parse(dateStr);
		} catch (ParseException e) {
			CommonUtils.logExceptions(e, logger, "", "");
		}
		return date;
	}

	public static LocalDate convertStringToLocalDate(String dateStr, String formatStr) {
		LocalDate date = null;
		try {
			date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(formatStr));
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		}
		return date;
	}

	public static String convertDateToString(Date date, String formatStr) {
		SimpleDateFormat df = new SimpleDateFormat(formatStr);
		String dateStr = df.format(date);
		logger.info("**** DATE FORMAT : {}", dateStr);
		return dateStr;
	}
	
	public static String convertDateToString(Date date) {
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
		String dateStr = df.format(date);
		logger.info("**** DATE FORMAT : {}", dateStr);
		return dateStr;
	}
	
	public static String convertDateToEmailString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT_EMAIL);
        String dateStr = df.format(date);
        logger.info("**** DATE FORMAT EMAIL : {}", dateStr);
        return dateStr;
    }

    public static boolean isPremium(String planId) {

        return premiumPlanTypeList.contains(planId) ;
    }
    
    /**
     * This method adds given number of working days to given date.
     * @param date
     * @param daysToAdd
     * @return Date
     */
    public static Date addWeekDaysToDate(Date date, int daysToAdd) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		for (int i = 1; i <= daysToAdd; i++) {
			// Adding 1 day to calendar.
			c.add(Calendar.DAY_OF_MONTH, 1);
			/*
			 * calendar.get(Calendar.DAY_OF_WEEK) = 1 (Sunday)
			 * calendar.get(Calendar.DAY_OF_WEEK) = 7 (Saturday)
			 * 
			 * If day is either Sunday or Saturday then it is non working day so Increasing
			 * the limit to compensate working days.
			 */
			if (c.get(Calendar.DAY_OF_WEEK) == 1 || c.get(Calendar.DAY_OF_WEEK) == 7) {
				daysToAdd++;
			}
		}
		return c.getTime();
	}
    
    /**
	 * This method takes in a planTypeCode (i.e. 10, 11, 1D) and returns the
	 * generic planType name (i.e. medical, dental, vision) If the passed in
	 * code is not found, the empty string is returned
	 * 
	 * @param planTypeCode
	 * @return String
	 */
	public static String getGenericPlanType(String planTypeCode) {

		String genericPlanType = "";
		if (BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains(planTypeCode)) {
			genericPlanType = BSSApplicationConstants.MEDICAL;
		} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planTypeCode)) {
			genericPlanType = BSSApplicationConstants.DENTAL;
		} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planTypeCode)) {
			genericPlanType = BSSApplicationConstants.VISION;
		} else if (BSSApplicationConstants.ADDITIONAL_PLAN_TYPES_INCLUD_CMTR.contains(planTypeCode)) {
			genericPlanType = BSSApplicationConstants.ADDITIONAL;
		}
		return genericPlanType;
	}

    
	/**
	 * This method takes in a planTypeCode (i.e. 10, 11, 1D) and returns the
	 * generic planTypeCode (i.e. 10, 11) If the passed in code is not found,
	 * the empty string is returned. This should only be used for medical,
	 * dental and vision
	 * 
	 * @param planTypeCode
	 * @return String
	 */
	public static String getGenericPlanTypeCode(String planTypeCode) {

		String genericPlanTypeCode = "";
		if (BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains(planTypeCode)) {
			genericPlanTypeCode = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planTypeCode)) {
			genericPlanTypeCode = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planTypeCode)) {
			genericPlanTypeCode = BSSApplicationConstants.VISION_PLAN_TYPE;
		} else if (BSSApplicationConstants.LIFE_CODE.equals(planTypeCode)) {
			genericPlanTypeCode = BSSApplicationConstants.LIFE_CODE;
		} else if (BSSApplicationConstants.STD_CODE.equals(planTypeCode)) {
			genericPlanTypeCode = BSSApplicationConstants.STD_CODE;
		} else if (BSSApplicationConstants.LTD_CODE.equals(planTypeCode)) {
			genericPlanTypeCode = BSSApplicationConstants.LTD_CODE;
		}
		return genericPlanTypeCode;
	}
	
	/**
	 * This method takes effDate and source format and target format
	 * and return converted date format
	 * @param effDate
	 * @param sourceFormat
	 * @param targetFormat
	 * @return
	 */
	public static Supplier<String> convertDateFormat(String effDate, String sourceFormat, String targetFormat) {
		return () -> {
			DateFormat originalFormat = new SimpleDateFormat(sourceFormat, Locale.ENGLISH);
			DateFormat target = new SimpleDateFormat(targetFormat);
			Date date = null;
			try {
				date = originalFormat.parse(effDate);
			} catch (ParseException e) {
				CommonUtils.logExceptions(e, logger, "", "");
			}
			return target.format(date);
		};
	}
    
}
