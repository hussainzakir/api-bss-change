package com.trinet.ambis.common;

import org.springframework.http.HttpStatus;

/**
 * @author schaudhari
 *
 */
public class BSSHttpStatusConstants {

	private BSSHttpStatusConstants() {
	}

	public static final int INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR.value();
	public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
	public static final int FORBIDDEN = HttpStatus.FORBIDDEN.value();
	public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
	public static final int OK = HttpStatus.OK.value();
	public static final HttpStatus OK_HTTP_STATUS = HttpStatus.OK;
	public static final String OK_HTTP_STATUS_NAME = HttpStatus.OK.name();

}
