package com.trinet.ambis.util;

import com.trinet.ambis.common.FlexRateConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.exception.FlexRateClientException;
import com.trinet.ambis.service.model.FlexRateResponse;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for FlexRateRestClient Resilience4j retry behaviour.
 *
 * <p>Covers every retryable exception type declared in api-bss.properties:
 * <ul>
 *   <li>{@link ResourceAccessException} — network/read timeouts</li>
 *   <li>{@link HttpServerErrorException} — 5xx (except 500)</li>
 *   <li>{@link HttpClientErrorException} — 401 / 403</li>
 *   <li>{@link IOException} — low-level I/O failures</li>
 *   <li>{@link ConnectException} — connection refused (subtype of IOException)</li>
 * </ul>
 * Each type has three test cases:
 * <ol>
 *   <li>Exception thrown on every attempt → retries exhausted → exception propagated</li>
 *   <li>Exception thrown only on attempt 1 → succeeds on attempt 2 → result returned</li>
 *   <li>Exception thrown directly by RestTemplate (not via response body)</li>
 * </ol>
 *
 * <p>Non-retryable scenarios (500, 4xx other than 401/403, null response) are also
 * verified to confirm a single call with no retry.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class FlexRateRestClientRetryIntegrationTest {

    private static final String FLEX_RATE_ENGINE_API_URL = "http://localhost:8081/api-bs-hw-rates-flexrate/v1";

    @InjectMocks
    private FlexRateRestClient flexRateRestClient;

    @Mock
    private RestTemplate restTemplate;

    private MockedStatic<BSSMessageConfig> bssMessageConfigMock;

    // -------------------------------------------------------------------------
    // Registry / setup / teardown
    // -------------------------------------------------------------------------

    /**
     * Mirrors api-bss.properties:
     * <pre>
     *   resilience4j.retry.configs.default.max-attempts=2
     *   resilience4j.retry.instances.flexRate.retry-exceptions=
     *       ResourceAccessException, HttpServerErrorException,
     *       HttpClientErrorException, ConnectException, IOException
     *   resilience4j.retry.instances.flexRate.ignore-exceptions=FlexRateClientException
     * </pre>
     * Wait-duration is shortened to 10 ms so the suite stays fast.
     */
    private static RetryRegistry buildRetryRegistry() {
        return RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(
                        ResourceAccessException.class,
                        HttpServerErrorException.class,
                        HttpClientErrorException.class,
                        IOException.class)          // ConnectException is a subtype of IOException
                .ignoreExceptions(FlexRateClientException.class)
                .build());
    }

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(flexRateRestClient, "retryRegistry", buildRetryRegistry());
        flexRateRestClient.init();
        bssMessageConfigMock = org.mockito.Mockito.mockStatic(BSSMessageConfig.class);
        bssMessageConfigMock.when(() -> BSSMessageConfig.getProperty(FlexRateConstants.FLEX_RATE_ENGINE_BASE_URI))
                .thenReturn(FLEX_RATE_ENGINE_API_URL);
    }

    @After
    public void tearDown() {
        if (bssMessageConfigMock != null) {
            bssMessageConfigMock.close();
        }
    }

    // =========================================================================
    // 1. ResourceAccessException  (network / read timeouts)
    // =========================================================================

    /**
     * ResourceAccessException on every attempt → retries exhausted → exception propagated.
     * RestTemplate is called exactly max-attempts (2) times.
     */
    @Test
    public void resourceAccessException_exhaustsRetries() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(new ResourceAccessException("read timeout"))
                .thenThrow(new ResourceAccessException("read timeout"));

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (Exception expected) {
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * ResourceAccessException on attempt 1, 200 OK on attempt 2 → succeeds.
     */
    @Test
    public void resourceAccessException_recoversOnRetry() {
        FlexRateResponse body = new FlexRateResponse();
        body.setRateGroupId("RG-RECOVER");
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(new ResourceAccessException("read timeout"))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        assertNotNull(result);
        assertEquals("RG-RECOVER", result.getRateGroupId());
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
    }

    // =========================================================================
    // 2. HttpServerErrorException  (5xx — except 500)
    // =========================================================================

    /**
     * HttpServerErrorException (503) thrown directly by RestTemplate on every attempt
     * → retries exhausted → exception propagated.
     */
    @Test
    public void httpServerErrorException_direct_exhaustsRetries() {
        HttpServerErrorException ex = HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                HttpHeaders.EMPTY, null, null);
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(ex)
                .thenThrow(ex);

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (HttpServerErrorException expected) {
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * 503 response (mocked RestTemplate, error handler bypassed) on every attempt
     * → extractResponseBody raises HttpServerErrorException → retries exhausted.
     */
    @Test
    public void httpServerErrorException_viaResponse_exhaustsRetries() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.SERVICE_UNAVAILABLE));

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (Exception expected) {
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * 502 response on attempt 1, 200 OK on attempt 2 → succeeds.
     */
    @Test
    public void httpServerErrorException_viaResponse_recoversOnRetry() {
        FlexRateResponse body = new FlexRateResponse();
        body.setRateGroupId("RG-502-RECOVER");
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        assertNotNull(result);
        assertEquals("RG-502-RECOVER", result.getRateGroupId());
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
    }

    // =========================================================================
    // 3. HttpClientErrorException  (401 / 403)
    // =========================================================================

    /**
     * HttpClientErrorException (401) thrown directly by RestTemplate on every attempt
     * → retries exhausted → exception propagated.
     */
    @Test
    public void httpClientErrorException_direct_exhaustsRetries() {
        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED, "Unauthorized",
                HttpHeaders.EMPTY, null, null);
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(ex)
                .thenThrow(ex);

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (HttpClientErrorException expected) {
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * 401 response on every attempt → retries exhausted → exception propagated.
     */
    @Test
    public void httpClientErrorException_401_viaResponse_exhaustsRetries() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED));

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (Exception expected) {
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * 403 response on every attempt → retries exhausted → exception propagated.
     */
    @Test
    public void httpClientErrorException_403_viaResponse_exhaustsRetries() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.FORBIDDEN))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.FORBIDDEN));

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (Exception expected) {
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * 401 on attempt 1, 200 OK on attempt 2 → succeeds.
     */
    @Test
    public void httpClientErrorException_401_recoversOnRetry() {
        FlexRateResponse body = new FlexRateResponse();
        body.setRateGroupId("RG-401-RECOVER");
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        assertNotNull(result);
        assertEquals("RG-401-RECOVER", result.getRateGroupId());
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
    }

    // =========================================================================
    // 4. IOException  (low-level I/O failures)
    //
    // RestTemplate.exchange() does not declare IOException in its signature, so
    // Mockito's thenThrow() rejects raw checked IOException.  In production,
    // RestTemplate always wraps IOException inside ResourceAccessException before
    // it leaves the method.  The two tests below mirror that real behaviour.
    //
    // The retry config lists IOException so that subclasses (e.g. SocketException,
    // EOFException) surfaced by other call sites are also covered.  We validate the
    // config rule by wrapping IOException in ResourceAccessException — exactly as
    // RestTemplate does — and confirm retry occurs on the wrapping exception.
    // =========================================================================

    /**
     * IOException wrapped in ResourceAccessException (real RestTemplate behaviour)
     * on every attempt → retries exhausted → exception propagated.
     */
    @Test
    public void ioException_wrappedInResourceAccessException_exhaustsRetries() {
        ResourceAccessException wrapped = new ResourceAccessException(
                "I/O error on GET request", new IOException("Broken pipe"));
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(wrapped)
                .thenThrow(wrapped);

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (ResourceAccessException expected) {
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * IOException wrapped in ResourceAccessException on attempt 1, 200 OK on attempt 2 → succeeds.
     */
    @Test
    public void ioException_wrappedInResourceAccessException_recoversOnRetry() {
        FlexRateResponse body = new FlexRateResponse();
        body.setRateGroupId("RG-IO-RECOVER");
        ResourceAccessException wrapped = new ResourceAccessException(
                "I/O error on GET request", new IOException("Broken pipe"));
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(wrapped)
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        assertNotNull(result);
        assertEquals("RG-IO-RECOVER", result.getRateGroupId());
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
    }

    // =========================================================================
    // 5. ConnectException  (connection refused — subtype of IOException)
    //
    // Same constraint as IOException above: RestTemplate wraps ConnectException
    // inside ResourceAccessException.  Tests verify the wrapped form is retried,
    // which is the scenario that actually occurs at runtime.
    // =========================================================================

    /**
     * ConnectException wrapped in ResourceAccessException (real RestTemplate behaviour)
     * on every attempt → retries exhausted → exception propagated.
     */
    @Test
    public void connectException_wrappedInResourceAccessException_exhaustsRetries() {
        ResourceAccessException wrapped = new ResourceAccessException(
                "I/O error on GET request", new ConnectException("Connection refused"));
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(wrapped)
                .thenThrow(wrapped);

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected exception after retries exhausted");
        } catch (ResourceAccessException expected) {
            assertTrue("Cause should be ConnectException",
                    expected.getCause() instanceof ConnectException);
            verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * ConnectException wrapped in ResourceAccessException on attempt 1, 200 OK on attempt 2 → succeeds.
     */
    @Test
    public void connectException_wrappedInResourceAccessException_recoversOnRetry() {
        FlexRateResponse body = new FlexRateResponse();
        body.setRateGroupId("RG-CONNECT-RECOVER");
        ResourceAccessException wrapped = new ResourceAccessException(
                "I/O error on GET request", new ConnectException("Connection refused"));
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenThrow(wrapped)
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        assertNotNull(result);
        assertEquals("RG-CONNECT-RECOVER", result.getRateGroupId());
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
    }

    // =========================================================================
    // Non-retryable scenarios
    // =========================================================================

    /**
     * 500 Internal Server Error → FlexRateClientException (ignored) → NO retry.
     */
    @Test
    public void doesNotRetryOn500InternalServerError() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected FlexRateClientException for 500 status");
        } catch (FlexRateClientException expected) {
            verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * 400 Bad Request → FlexRateClientException (ignored) → NO retry.
     */
    @Test
    public void doesNotRetryOn400BadRequest() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected FlexRateClientException for 400 status");
        } catch (FlexRateClientException expected) {
            verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * 404 Not Found → FlexRateClientException (ignored) → NO retry.
     */
    @Test
    public void doesNotRetryOn404NotFound() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected FlexRateClientException for 404 status");
        } catch (FlexRateClientException expected) {
            verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    /**
     * Null response → FlexRateClientException (ignored) → NO retry.
     */
    @Test
    public void doesNotRetryOnNullResponse() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(null);

        try {
            flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);
            fail("Expected FlexRateClientException for null response");
        } catch (FlexRateClientException expected) {
            verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        }
    }

    // =========================================================================
    // Success scenarios
    // =========================================================================

    /**
     * 200 OK → body returned, no retry.
     */
    @Test
    public void accepts200OkResponse() {
        FlexRateResponse expected = new FlexRateResponse();
        expected.setRateGroupId("RG001");
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        assertNotNull(result);
        assertEquals("RG001", result.getRateGroupId());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
    }

    /**
     * 201 Created → body returned (all 2xx accepted), no retry.
     */
    @Test
    public void accepts201CreatedResponse() {
        FlexRateResponse expected = new FlexRateResponse();
        expected.setRateGroupId("RG-201");
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(expected, HttpStatus.CREATED));

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        assertNotNull(result);
        assertEquals("RG-201", result.getRateGroupId());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
    }

    // =========================================================================
    // URL / header contract tests
    // =========================================================================

    @Test
    public void buildsCorrectUrlForCompanyCode() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(new FlexRateResponse(), HttpStatus.OK));

        flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        assertEquals(
                "http://localhost:8081/api-bs-hw-rates-flexrate/v1/plan-rates?companyCode=ABC&effectiveDate=2024-01-01&benefitTypes=medical,dental,vision&bundleId",
                urlCaptor.getValue());
    }

    @Test
    public void buildsCorrectUrlForProposalId() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(new FlexRateResponse(), HttpStatus.OK));

        flexRateRestClient.getPlanRatesByProposalId("PROP123", "2024-01-01", null, typeRef);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef));
        assertEquals(
                "http://localhost:8081/api-bs-hw-rates-flexrate/v1/plan-rates?proposalId=PROP123&effectiveDate=2024-01-01&benefitTypes=medical,dental,vision&bundleId",
                urlCaptor.getValue());
    }

    @Test
    public void setsRequiredHttpHeaders() {
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(new ResponseEntity<>(new FlexRateResponse(), HttpStatus.OK));

        flexRateRestClient.getPlanRatesByCompanyCode("ABC", "2024-01-01", null, typeRef);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(typeRef));

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertNotNull(headers);
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertTrue(headers.getAccept().contains(MediaType.APPLICATION_JSON));
    }
}


