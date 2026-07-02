package com.trinet.ambis.enums;

/**
 * @author schaudhari
 */
public enum ExceptionTypeEnum {

	MIN_FUND("MINIMUM_FUNDING");

	private final String exceptionType;

	public String getExceptionType() {
		return exceptionType;
	}

	private ExceptionTypeEnum(String exceptionType) {
		this.exceptionType = exceptionType;
	}

}
