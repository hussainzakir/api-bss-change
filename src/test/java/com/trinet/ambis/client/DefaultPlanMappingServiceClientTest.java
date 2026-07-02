package com.trinet.ambis.client;

import com.trinet.common.auth.TriNetAuthUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPlanMappingServiceClientTest {

    @InjectMocks
    private DefaultPlanMappingServiceClient planMappingServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<DefaultPlanMappingServiceClient.PlanMappingRequest>> requestCaptor;

    private MockedStatic<TriNetAuthUtil> mockTriNetAuthUtil;

    private static final String COMPANY_CODE    = "COMP1";
    private static final String DCP_API_URI     = "http://localhost:8080/benbundles";
    private static final String MAPPING_PATH    = "/plan-mapping/{companycode}";
    private static final String MOCK_AUTH_TOKEN = "Basic dGVzdDp0ZXN0";
    private static final String MOCK_SCOPE      = "benbundles.scope";

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(planMappingServiceClient, "dcpApiUri",             DCP_API_URI);
        ReflectionTestUtils.setField(planMappingServiceClient, "planMappingApiPath",    MAPPING_PATH);
        ReflectionTestUtils.setField(planMappingServiceClient, "benefitsBatchScope",    MOCK_SCOPE);
        ReflectionTestUtils.setField(planMappingServiceClient, "benefitsBatchClientId", "clientId");
        ReflectionTestUtils.setField(planMappingServiceClient, "benefitsBatchClientSecret", "clientSecret");

        mockTriNetAuthUtil = Mockito.mockStatic(TriNetAuthUtil.class);
        mockTriNetAuthUtil.when(() -> TriNetAuthUtil.getAuthorization(anyString(), anyString()))
                .thenReturn(MOCK_AUTH_TOKEN);
    }

    @After
    public void tearDown() {
        mockTriNetAuthUtil.close();
    }

    // ── getPlanMapping – success ───────────────────────────────────────────────

    @Test
    public void getPlanMapping_successfulResponse_returnsMappedList() {
        DefaultPlanMappingServiceClient.PlanMappingResponse mockResponse = buildPlanMappingResponse("E1", "MED1");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(mockResponse)));

        List<DefaultPlanMappingServiceClient.PlanMappingResponse> result =
                planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("E1", result.get(0).getEmployeeResponseList().get(0).getEmployeeId());
        assertEquals("MED1", result.get(0).getEmployeeResponseList().get(0).getMappedPlans().get("medical").getPlanId());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class));
    }

    @Test(expected = HttpClientErrorException.class)
    public void getPlanMapping_nonOkStatus_throwsHttpStatusCodeException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
                        HttpHeaders.EMPTY, null, null));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());
    }

    @Test(expected = HttpServerErrorException.class)
    public void getPlanMapping_serverError_throwsHttpServerErrorException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                        HttpHeaders.EMPTY, null, null));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());
    }

    @Test
    public void getPlanMapping_nullResponseBody_returnsEmptyList() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(null));

        List<DefaultPlanMappingServiceClient.PlanMappingResponse> result =
                planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getPlanMapping – request construction ─────────────────────────────────

    @Test
    public void getPlanMapping_buildsCorrectUri() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.POST), any(HttpEntity.class),
                any(ParameterizedTypeReference.class));

        assertTrue(uriCaptor.getValue().contains(COMPANY_CODE));
        assertTrue(uriCaptor.getValue().startsWith(DCP_API_URI));
    }

    @Test
    public void getPlanMapping_setsAuthorizationAndScopeHeaders() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), requestCaptor.capture(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        HttpEntity<DefaultPlanMappingServiceClient.PlanMappingRequest> capturedEntity = requestCaptor.getValue();
        assertEquals(MOCK_AUTH_TOKEN, capturedEntity.getHeaders().getFirst("Authorization"));
        assertEquals(MOCK_SCOPE,      capturedEntity.getHeaders().getFirst("scope"));
    }

    @Test
    public void getPlanMapping_sendsRequestBodyWithCorrectFields() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), requestCaptor.capture(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        DefaultPlanMappingServiceClient.PlanMappingRequest request = buildRequest();
        planMappingServiceClient.getPlanMapping(COMPANY_CODE, request);

        DefaultPlanMappingServiceClient.PlanMappingRequest sentBody = requestCaptor.getValue().getBody();
        assertNotNull(sentBody);
        assertEquals("102", sentBody.getCloneBenefitProgram());
        assertEquals("PROSPECT_DEFAULT_PLAN_MAPPING", sentBody.getMappingRuleTemplateName());
        assertEquals("Q1",         sentBody.getExchangeId());
        assertEquals("2025-01-01", sentBody.getTargetEffectiveDate());
        assertEquals(Set.of("E1", "E2"), sentBody.getEmployeeIds());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private DefaultPlanMappingServiceClient.PlanMappingRequest buildRequest() {
        return new DefaultPlanMappingServiceClient.PlanMappingRequest(
                "102", "PROSPECT_DEFAULT_PLAN_MAPPING", "Q1", "2025-01-01", Set.of("E1", "E2"), "CA", "94043", "541512");
    }

    private DefaultPlanMappingServiceClient.PlanMappingResponse buildPlanMappingResponse(String employeeId, String planId) {
        DefaultPlanMappingServiceClient.PlanResponse planResponse = new DefaultPlanMappingServiceClient.PlanResponse(planId, null, null);
        DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse =
                new DefaultPlanMappingServiceClient.EmployeeResponse(employeeId, Map.of("medical", planResponse));
        return new DefaultPlanMappingServiceClient.PlanMappingResponse(List.of(employeeResponse));
    }
}

