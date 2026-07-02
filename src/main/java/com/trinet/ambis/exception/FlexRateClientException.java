package com.trinet.ambis.exception;

import org.springframework.http.HttpStatus;

/**
 * Non-retryable exception thrown by {@code FlexRateRestClient} when the FlexRate API
 * returns a response that must not be retried (e.g. 4xx, null response, unexpected status).
 *
 * <p>Resilience4j is configured to ignore this exception via
 * {@code resilience4j.retry.instances.flexRate.ignore-exceptions} so retries are never
 * triggered for these failure cases.</p>
 */
public class FlexRateClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    public FlexRateClientException(String message) {
        super(message);
        this.status = null;
    }

    public FlexRateClientException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

