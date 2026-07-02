package com.trinet.ambis.exception;

public class BssSecurityException extends RuntimeException {
	private static final long serialVersionUID = 8248786721802748521L;

	public BssSecurityException() {
		super();
	}

	public BssSecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public BssSecurityException(String message) {
		super(message);
	}

	public BssSecurityException(Throwable cause) {
		super(cause);
	}

}
