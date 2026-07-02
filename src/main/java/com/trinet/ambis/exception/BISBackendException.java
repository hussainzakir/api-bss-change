package com.trinet.ambis.exception;

public class BISBackendException extends RuntimeException {

	private static final long serialVersionUID = 2476322297626082671L;

	public BISBackendException() {
		
	}

	public BISBackendException(String message) {
		super(message);
		
	}

	public BISBackendException(Throwable cause) {
		super(cause);
		
	}

	public BISBackendException(String message, Throwable cause) {
		super(message, cause);
		
	}

}
