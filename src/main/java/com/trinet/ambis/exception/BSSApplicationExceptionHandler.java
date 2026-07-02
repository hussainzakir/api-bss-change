package com.trinet.ambis.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.common.BSSApplicationConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.util.BSSSecurityUtils;

@ControllerAdvice
public class BSSApplicationExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger bssLogger = LoggerFactory.getLogger(BSSApplicationExceptionHandler.class);

	@ExceptionHandler(BSSApplicationException.class)
	public ResponseEntity<Object> handleBSSApplicationException(BSSApplicationException bssApplicationException,
			HttpServletRequest request) throws IOException {
		BSSApplicationError appError = bssApplicationException.getBssError();
		if (null != bssApplicationException.getCause()) {
			appError.setMessage(bssApplicationException.getCause().toString());
			try {
				appError.setSource(bssApplicationException.getCause().getStackTrace()[0].getClassName());
			} catch (Exception e) {
				appError.setSource("UNKNOWN");
			}
			appError.setExceptionStackTrace(ExceptionUtils.getStackTrace(bssApplicationException.getCause()));
		}
		appError.setUrl(request.getRequestURL().toString());
		try {
			if (BSSSecurityUtils.checkSystemAccount()) {
				appError.setEmplId(BSSApplicationConstants.SYSTEM_ACCOUNT);
			} else {
				appError.setEmplId(BSSSecurityUtils.getAuthenticatedPersonId());
			}
		} catch (Exception e) {
			bssLogger.error("Error while fetching emplId: {}", e.getMessage());
		}
		ObjectMapper mapper = new ObjectMapper();
		String errorToLog = mapper.writeValueAsString(appError);
		bssLogger.error("BSS_ERROR: {}", errorToLog);
		// not required in the JSON response set to UI
		appError.setQuery("");
		appError.setQueryParams(null);
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		if (appError.getStatus() > 0) {
			try {
				httpStatus = HttpStatus.valueOf(appError.getStatus());
			} catch (Exception e) {
			}
		}
		return new ResponseEntity<>(appError, new HttpHeaders(), httpStatus);
	}
	
	@ExceptionHandler(BssSecurityException.class)
	public ResponseEntity<String> handleBssSecurityException(BssSecurityException bssSecurityException,
			HttpServletRequest request) throws IOException {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(bssSecurityException.getMessage());
	}

}