package com.trinet.ambis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidOmsOfferingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidOmsOfferingException(String message) {
    super(message);
  }

}
