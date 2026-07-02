package com.trinet.ambis.exception;

import com.trinet.exception.TriNetRuntimeBaseException;

public class GatewaySecurityException extends TriNetRuntimeBaseException {
	private static final long serialVersionUID = 1L;

	public GatewaySecurityException(Throwable cause) {
		super(cause);
	}

	public GatewaySecurityException(String message) {
		super(message);
	}

	public GatewaySecurityException(String message, String errorCode) {
		super(message, errorCode);
	}

	public GatewaySecurityException(String message, Throwable throwable) {
		super(message, throwable);
	}

}