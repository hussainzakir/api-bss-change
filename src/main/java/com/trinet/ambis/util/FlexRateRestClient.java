package com.trinet.ambis.util;

import com.trinet.ambis.authorization.BenefitsBatchAuthorization;
import com.trinet.ambis.common.FlexRateConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.exception.FlexRateClientException;
import com.trinet.domain.common.ReturnResponse;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

@Component
public class FlexRateRestClient {

    private static final String FLEX_RATE_RETRY_INSTANCE = "flexRate";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RetryRegistry retryRegistry;

    private Retry flexRateRetry;

    @PostConstruct
    public void init() {
        this.flexRateRetry = retryRegistry.retry(FLEX_RATE_RETRY_INSTANCE);
    }

    /**
     * Call the plan-rates endpoint with companyCode and a valid effectiveDate (YYYY-MM-DD).
     */
    public <T> T getPlanRatesByCompanyCode(
            String companyCode,
            String effectiveDate,
            String bundleId,
            ParameterizedTypeReference<T> typeReference
    ) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("companyCode", companyCode);
        queryParams.add("effectiveDate", effectiveDate);
        queryParams.add("benefitTypes", String.join(",", com.trinet.ambis.common.BSSApplicationConstants.PRIMARY_PLAN_TYPE_NAMES));
        queryParams.add("bundleId", bundleId);
        // URI and headers are resolved once here, before the retry lambda, to avoid
        // repeated BSSMessageConfig lookups and header construction on every attempt.
        String apiUri = buildApiUri(FlexRateConstants.FLEX_RATE_PLAN_RATES_URI, queryParams);
        HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
        return flexRateRetry.executeSupplier(() -> callApiEndPoint(apiUri, entity, typeReference));
    }

    /**
     * Call the plan-rates endpoint with proposalId and a valid effectiveDate (YYYY-MM-DD).
     */
    public <T> T getPlanRatesByProposalId(
            String proposalId,
            String effectiveDate,
            String bundleId,
            ParameterizedTypeReference<T> typeReference
    ) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("proposalId", proposalId);
        queryParams.add("effectiveDate", effectiveDate);
        queryParams.add("benefitTypes", String.join(",", com.trinet.ambis.common.BSSApplicationConstants.PRIMARY_PLAN_TYPE_NAMES));
        queryParams.add("bundleId", bundleId);
        // URI and headers are resolved once here, before the retry lambda, to avoid
        // repeated BSSMessageConfig lookups and header construction on every attempt.
        String apiUri = buildApiUri(FlexRateConstants.FLEX_RATE_PLAN_RATES_URI, queryParams);
        HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
        return flexRateRetry.executeSupplier(() -> callApiEndPoint(apiUri, entity, typeReference));
    }

    /**
     * Call the clients-with-rates endpoint and return company codes for the requested quarter and plan year start.
     */
    public List<String> getClientsWithRates(String quarter, String planYearStartDate) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("quarter", quarter);
        queryParams.add("planYearStartDate", planYearStartDate);
        String apiUri = buildApiUri(FlexRateConstants.FLEX_RATE_CLIENTS_WITH_RATES_URI, queryParams);
        HttpEntity<String> entity = new HttpEntity<>(buildHeaders());
        var typeReference = new ParameterizedTypeReference<ReturnResponse<List<String>>>() {};
        ReturnResponse<List<String>> response =
                flexRateRetry.executeSupplier(() -> callApiEndPoint(apiUri, entity, typeReference));
        return response.getData();
    }

    private String buildApiUri(String path, MultiValueMap<String, String> queryParams) {
        return UriComponentsBuilder
                .fromHttpUrl(BSSMessageConfig.getProperty(FlexRateConstants.FLEX_RATE_ENGINE_BASE_URI))
                .path(path)
                .queryParams(Optional.ofNullable(queryParams).orElse(new LinkedMultiValueMap<>()))
                .toUriString();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        BenefitsBatchAuthorization.addAuthHeaders(headers);
        return headers;
    }

    private <T> T callApiEndPoint(String apiUri, HttpEntity<?> entity,
                                   ParameterizedTypeReference<T> typeReference) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(apiUri, HttpMethod.GET, entity, typeReference);
            return extractResponseBody(response);
        } catch (HttpClientErrorException e) {
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            // 401/403 are retryable (e.g. expired token) — rethrow so Resilience4j retries.
            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                throw e;
            }
            // Other 4xx: non-retryable — fail immediately.
            // FlexRateClientException is listed under ignore-exceptions in the retry config.
            throw new FlexRateClientException(
                    String.format("FlexRate API returned non-retryable status: %s", status), status);
        } catch (HttpServerErrorException e) {
            HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
            // 500 Internal Server Error: non-retryable — the remote service reported an
            // application-level error that a retry is unlikely to fix.
            if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
                throw new FlexRateClientException(
                        String.format("FlexRate API returned non-retryable status: %s", status), status);
            }
            // Other 5xx (502, 503, 504 …): transient gateway/upstream errors — retryable.
            // Rethrow so Resilience4j retries via retry-exceptions config.
            throw e;
        }
    }

    private <T> T extractResponseBody(ResponseEntity<T> responseEntity) {
        if (responseEntity == null) {
            // RestTemplate should never return null, but guard defensively.
            // FlexRateClientException is non-retryable — Resilience4j will not retry this.
            throw new FlexRateClientException("FlexRate API returned a null response.");
        }

        HttpStatus statusCode = HttpStatus.valueOf(responseEntity.getStatusCode().value());

        // Happy path: any 2xx status is accepted.
        if (statusCode.is2xxSuccessful()) {
            return responseEntity.getBody();
        }

        // In production, DefaultResponseErrorHandler throws before exchange() returns for
        // non-2xx, so the branches below are reached only when the error handler is bypassed
        // (e.g. mocked RestTemplate in tests or a custom no-op ResponseErrorHandler).
        // We must throw the same exception types that callApiEndPoint's catch blocks expect,
        // so that the retryable/non-retryable classification is applied consistently.

        // 401 Unauthorized / 403 Forbidden: retryable (e.g. expired token).
        // Throw HttpClientErrorException so Resilience4j retries via retry-exceptions config.
        if (statusCode == HttpStatus.UNAUTHORIZED || statusCode == HttpStatus.FORBIDDEN) {
            throw HttpClientErrorException.create(
                    statusCode, statusCode.getReasonPhrase(), HttpHeaders.EMPTY, null, null);
        }

        // 500 Internal Server Error: non-retryable.
        // FlexRateClientException is listed under ignore-exceptions in the retry config.
        if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new FlexRateClientException(
                    String.format("FlexRate API returned non-retryable status: %s", statusCode), statusCode);
        }

        // Other 5xx (502, 503, 504 …): transient gateway/upstream errors — retryable.
        // Throw HttpServerErrorException so Resilience4j retries via retry-exceptions config.
        if (statusCode.is5xxServerError()) {
            throw HttpServerErrorException.create(
                    statusCode, statusCode.getReasonPhrase(), HttpHeaders.EMPTY, null, null);
        }

        // All remaining non-2xx (4xx except 401/403, 3xx, etc.): non-retryable.
        throw new FlexRateClientException(
                String.format("FlexRate API returned non-retryable status: %s", statusCode), statusCode);
    }
}
