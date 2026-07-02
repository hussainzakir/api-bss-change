package com.trinet.ambis.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.util.BSSSecurityUtils;

@ControllerAdvice
public class GenericExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionHandler.class);

	@org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleException(Exception exception, HttpServletRequest request) throws IOException {
		BSSApplicationError appError = new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
				BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, "",
				"Exception occurred while processing the request.", null, null);
		if (null != exception) {
			try {
				appError.setMessage(ExceptionUtils.getRootCauseStackTrace(exception)[0]);
				appError.setSource(ExceptionUtils.getRootCauseStackTrace(exception)[1]);
				appError.setExceptionStackTrace(ExceptionUtils.getStackTrace(exception));
				if (exception.getMessage() != null
						&& exception.getMessage().contains(BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX)) {
					appError.setCustomMessage(
							exception.getMessage().substring(exception.getMessage().lastIndexOf(':') + 1));
				}
			} catch (Exception e) {
				LOG.error("Exception occurred while generating error response: ", e);
			}
		}
		appError.setUrl(request.getRequestURL().toString());
		try {
			appError.setEmplId(BSSSecurityUtils.getAuthenticatedPersonId());
		} catch (Exception e) {
			LOG.info("Authenticated person id is not available because the request is from system account");
		}
		ObjectMapper mapper = new ObjectMapper();
		String errorToLog = mapper.writeValueAsString(appError);
		LOG.error("BSS_ERROR: {}", errorToLog);
		appError.setQuery("");
		appError.setQueryParams(null);
		return new ResponseEntity<>(appError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}