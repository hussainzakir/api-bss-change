package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.FlexRateConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.exception.FlexRateClientException;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.domain.common.ReturnResponse;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class FlexRateRestClientTest {

    @InjectMocks
    private FlexRateRestClient flexRateRestClient;

    @Mock
    private RestTemplate restTemplate;

    private MockedStatic<BSSMessageConfig> bssMessageConfigMock;

    private static final String FLEX_RATE_ENGINE_API_URL = "http://localhost:8081/api-bs-hw-rates-flexrate/v1";

    @Before
    public void setUp() throws IOException {
        RetryRegistry registry = buildRetryRegistryFromProperties();
        ReflectionTestUtils.setField(flexRateRestClient, "retryRegistry", registry);
        flexRateRestClient.init();
        bssMessageConfigMock = mockStatic(BSSMessageConfig.class);
        when(BSSMessageConfig.getProperty(FlexRateConstants.FLEX_RATE_ENGINE_BASE_URI))
                .thenReturn(FLEX_RATE_ENGINE_API_URL);
    }

    /**
     * Builds a {@link RetryRegistry} whose parameters are read directly from
     * {@code api-bss.properties} so this test stays in sync with the production
     * configuration automatically.
     *
     * <p>The flexRate instance inherits {@code max-attempts} and {@code wait-duration}
     * from the {@code default} base-config when those keys are not overridden at the
     * instance level — the same resolution Spring Boot auto-configuration performs.</p>
     */
    private RetryRegistry buildRetryRegistryFromProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("api-bss.properties")) {
            if (is == null) {
                throw new IllegalStateException("api-bss.properties not found on test classpath");
            }
            props.load(is);
        }

        // Resolve max-attempts: instance override → default config fallback
        int maxAttempts = Integer.parseInt(props.getProperty(
                "resilience4j.retry.instances.flexRate.max-attempts",
                props.getProperty("resilience4j.retry.configs.default.max-attempts", "3")));

        // Resolve wait-duration: instance override → default config fallback
        // Accepted formats from Spring Boot: "1s", "500ms", "1000" (ms)
        String waitDurationStr = props.getProperty(
                "resilience4j.retry.instances.flexRate.wait-duration",
                props.getProperty("resilience4j.retry.configs.default.wait-duration", "1s"));
        Duration waitDuration = parseDuration(waitDurationStr);

        return RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(waitDuration)
                .retryExceptions(
                        HttpServerErrorException.class,
                        HttpClientErrorException.class,
                        ResourceAccessException.class,
                        java.io.IOException.class)
                .ignoreExceptions(FlexRateClientException.class)
                .build());
    }

    /** Parses Spring Boot duration strings: {@code "1s"}, {@code "500ms"}, or plain millis. */
    private static Duration parseDuration(String value) {
        String v = value.trim();
        if (v.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(v.substring(0, v.length() - 2).trim()));
        } else if (v.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(v.substring(0, v.length() - 1).trim()));
        }
        return Duration.ofMillis(Long.parseLong(v));
    }

    @After
    public void tearDown() {
        bssMessageConfigMock.close();
    }

    @Test
    public void testGetPlanRatesByCompanyCode() {
        String companyCode = "ABC";
        String effectiveDate = "2024-01-01";
        FlexRateResponse expectedResponse = new FlexRateResponse();
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        ResponseEntity<FlexRateResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(responseEntity);

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode(companyCode, effectiveDate, null, typeRef);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(typeRef)
        );
        assertEquals("http://localhost:8081/api-bs-hw-rates-flexrate/v1/plan-rates?companyCode=ABC&effectiveDate=2024-01-01&benefitTypes=medical,dental,vision&bundleId",
                urlCaptor.getValue());
        assertEquals(expectedResponse, result);
    }

    @Test
    public void testGetPlanRatesByCompanyCodeWithBundleId() {
        String companyCode = "ABC";
        String effectiveDate = "2024-01-01";
        String bundleId = "1";
        FlexRateResponse expectedResponse = new FlexRateResponse();
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        ResponseEntity<FlexRateResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(responseEntity);

        FlexRateResponse result = flexRateRestClient.getPlanRatesByCompanyCode(companyCode, effectiveDate, bundleId, typeRef);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(typeRef)
        );
        assertEquals("http://localhost:8081/api-bs-hw-rates-flexrate/v1/plan-rates?companyCode=ABC&effectiveDate=2024-01-01&benefitTypes=medical,dental,vision&bundleId=1",
                urlCaptor.getValue());
        assertEquals(expectedResponse, result);
    }

    @Test
    public void testGetPlanRatesByProposalId() {
        String proposalId = "123";
        String effectiveDate = "2024-01-01";
        FlexRateResponse expectedResponse = new FlexRateResponse();
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        ResponseEntity<FlexRateResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(responseEntity);

        FlexRateResponse result = flexRateRestClient.getPlanRatesByProposalId(proposalId, effectiveDate, null, typeRef);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(typeRef)
        );
        assertEquals("http://localhost:8081/api-bs-hw-rates-flexrate/v1/plan-rates?proposalId=123&effectiveDate=2024-01-01&benefitTypes=medical,dental,vision&bundleId",
                urlCaptor.getValue());
        assertEquals(expectedResponse, result);
    }

    @Test
    public void testGetPlanRatesByProposalIdWithBundleId() {
        String proposalId = "123";
        String effectiveDate = "2024-01-01";
        String bundleId = "2";
        FlexRateResponse expectedResponse = new FlexRateResponse();
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        ResponseEntity<FlexRateResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(responseEntity);

        FlexRateResponse result = flexRateRestClient.getPlanRatesByProposalId(proposalId, effectiveDate, bundleId, typeRef);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(typeRef)
        );
        assertEquals("http://localhost:8081/api-bs-hw-rates-flexrate/v1/plan-rates?proposalId=123&effectiveDate=2024-01-01&benefitTypes=medical,dental,vision&bundleId=2",
                urlCaptor.getValue());
        assertEquals(expectedResponse, result);
    }

    @Test(expected = RuntimeException.class)
    public void testGetPlanRatesByCompanyCodeException() {
        String companyCode = "ABC";
        String effectiveDate = "2024-01-01";
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        ResponseEntity<FlexRateResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(responseEntity);

        flexRateRestClient.getPlanRatesByCompanyCode(companyCode, effectiveDate, null, typeRef);
    }

    @Test(expected = RuntimeException.class)
    public void testGetPlanRatesByProposalIdException() {
        String proposalId = "123";
        String effectiveDate = "2024-01-01";
        ParameterizedTypeReference<FlexRateResponse> typeRef = new ParameterizedTypeReference<FlexRateResponse>() {};

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(typeRef)))
                .thenReturn(null);

        flexRateRestClient.getPlanRatesByProposalId(proposalId, effectiveDate, null, typeRef);
    }

  @Test
  public void testGetClientsWithRates() {
    String quarter = "Q1";
    String planYearStartDate = "2026-01-01";
    List<String> expectedResponse = Arrays.asList("T01", "T02");

    ReturnResponse<List<String>> returnResponse = new ReturnResponse<>();
    returnResponse.setData(expectedResponse);
    ResponseEntity<ReturnResponse<List<String>>> responseEntity = new ResponseEntity<>(returnResponse, HttpStatus.OK);
    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<ReturnResponse<List<String>>>>any()))
        .thenReturn(responseEntity);

    List<String> result = flexRateRestClient.getClientsWithRates(quarter, planYearStartDate);

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(restTemplate).exchange(
        urlCaptor.capture(),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        org.mockito.ArgumentMatchers.<ParameterizedTypeReference<ReturnResponse<List<String>>>>any());
    assertEquals(
        "http://localhost:8081/api-bs-hw-rates-flexrate/v1/clients/with-rates?quarter=Q1&planYearStartDate=2026-01-01",
        urlCaptor.getValue());
    assertEquals(expectedResponse, result);
  }
}
