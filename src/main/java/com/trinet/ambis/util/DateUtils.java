package com.trinet.ambis.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class DateUtils {

	private DateUtils() {
	}

	public static String getCurrentDate() {
		return LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy_MM_dd"));
	}

	/**
	 * This method calculates age until given date (ageCalculationDate) using dob
	 *
	 * @param dob
	 * @param ageCalculationDate
	 * @return
	 */
	public static int calculateAgeUntilDate(LocalDate dob, LocalDate ageCalculationDate) {
		Period between = Period.between(dob, ageCalculationDate);
		return between.isNegative() ? -1 : between.getYears();
	}

	public static String extractYear(String dateStr) {
		try {
			// Define the formatter for the given format
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
			LocalDate date = LocalDate.parse(dateStr, formatter);
			return String.valueOf(date.getYear());
		} catch (Exception e) {
			return " ";
		}
	}
	
	public static String createDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
		LocalDate localdate = LocalDate.parse(date, formatter);
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		return localdate.format(timeFormatter);
	}
	
	public static String getSystemDate() {
		return LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
	}

    public static boolean isIsoDate(String date) {
        if (date == null) return false;
        try {
            // YYYY-MM-DD
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
