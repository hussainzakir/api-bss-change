package com.trinet.ambis.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RangeUtils {

	private RangeUtils() {
	}

	private static BiPredicate<BigDecimal, BigDecimal> isValueGreater = (value, min) -> value.compareTo(min) >= 0;

	private static BiPredicate<BigDecimal, BigDecimal> isValueLesser = (value, max) -> value.compareTo(max) <= 0;

	/**
	 * Checks given value is in range
	 * 
	 * @param valueStr
	 * @param min
	 * @param max
	 * @return <true> when min is null and max is null </br>
	 *         <true> when min is provided and max is provided and value is between
	 *         min and amx </br>
	 *         <true> when min is provided and max is not provided and value is
	 *         greater than or equal to min value</br>
	 *         <true> when min is not provided and max is provided and value is
	 *         lesser than or equal to max value</br>
	 *         else <false>
	 */
	public static boolean isInRange(String valueStr, BigDecimal min, BigDecimal max) {
		Optional<BigDecimal> valueOpt = stripDollar(valueStr);
		if (!valueOpt.isPresent()) {
			return Boolean.FALSE;
		}
		BigDecimal value = valueOpt.get();
		if (Objects.isNull(min) && Objects.isNull(max)) {
			return Boolean.TRUE;
		}
		if (Objects.nonNull(min) && Objects.nonNull(max)
				&& (isValueGreater.test(value, min) && isValueLesser.test(value, max))) {
			return Boolean.TRUE;
		}
		if (Objects.nonNull(min) && Objects.isNull(max) && isValueGreater.test(value, min)) {
			return Boolean.TRUE;
		}
		if (Objects.isNull(min) && Objects.nonNull(max) && isValueLesser.test(value, max)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private static Optional<BigDecimal> stripDollar(String value) {
		try {
			Number number = NumberFormat.getCurrencyInstance(Locale.US).parse(value);
			return Optional.of(new BigDecimal(number.toString()));
		} catch (ParseException e) {
			log.error(e, e);
			log.error(
					String.format("Plan attribute value is invalid, value = %s , Should be in $####.## format", value));
			return Optional.empty();
		}
	}

}
