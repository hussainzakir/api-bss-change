package com.trinet.ambis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Company not found in PSoft DB")
public class CompanyNotFound extends RuntimeException {

    private static final long serialVersionUID = 1L;

}
