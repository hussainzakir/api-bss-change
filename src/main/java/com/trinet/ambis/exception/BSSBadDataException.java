package com.trinet.ambis.exception;

/**
 * Exception for invalid or bad data scenarios in BSS application.
 * This exception should be thrown when data validation fails or
 * when required data is missing or invalid.
 */
public class BSSBadDataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BSSBadDataException() {
		super();
	}
	
	public BSSBadDataException(String message) {
		super(message);
	}
}