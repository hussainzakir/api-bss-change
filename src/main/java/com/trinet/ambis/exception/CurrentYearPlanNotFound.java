package com.trinet.ambis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No plans found for current year")
public class CurrentYearPlanNotFound extends RuntimeException {

    private static final long serialVersionUID = 1L;

}
