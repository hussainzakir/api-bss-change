package com.trinet.ambis.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.Set;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.trinet.common.auth.TriNetAuthUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RunWith(MockitoJUnitRunner.class)
public class PlanMappingServiceClientIT {

    private static final int WIREMOCK_PORT       = 9876;
    private static final String BASE_URL         = "http://localhost:" + WIREMOCK_PORT;
    private static final String MAPPING_PATH     = "/plan-mapping/{companycode}";
    private static final String COMPANY_CODE     = "COMP1";
    private static final String MOCK_AUTH_TOKEN  = "Basic dGVzdDp0ZXN0";
    private static final String MOCK_SCOPE       = "benbundles.scope";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WIREMOCK_PORT);

    private DefaultPlanMappingServiceClient planMappingServiceClient;
    private MockedStatic<TriNetAuthUtil> mockTriNetAuthUtil;

    private static final String RESPONSE_ONE_EMPLOYEE =
            "[{\"employeeResponseList\":[{\"id\":\"E1\",\"mappedPlans\":{\"medical\":{\"planId\":\"MED1\"},\"dental\":{\"planId\":\"DEN1\"}}}]}]";

    private static final String RESPONSE_TWO_EMPLOYEES =
            "[{\"employeeResponseList\":[{\"id\":\"E1\",\"mappedPlans\":{\"medical\":{\"planId\":\"MED1\"}}},{\"id\":\"E2\",\"mappedPlans\":{\"dental\":{\"planId\":\"DEN2\"}}}]}]";

    @Before
    public void setUp() {
        planMappingServiceClient = new DefaultPlanMappingServiceClient();
        ReflectionTestUtils.setField(planMappingServiceClient, "restTemplate",           new RestTemplate());
        ReflectionTestUtils.setField(planMappingServiceClient, "dcpApiUri",              BASE_URL);
        ReflectionTestUtils.setField(planMappingServiceClient, "planMappingApiPath",     MAPPING_PATH);
        ReflectionTestUtils.setField(planMappingServiceClient, "benefitsBatchScope",     MOCK_SCOPE);
        ReflectionTestUtils.setField(planMappingServiceClient, "benefitsBatchClientId",  "clientId");
        ReflectionTestUtils.setField(planMappingServiceClient, "benefitsBatchClientSecret", "clientSecret");

        mockTriNetAuthUtil = Mockito.mockStatic(TriNetAuthUtil.class);
        mockTriNetAuthUtil.when(() -> TriNetAuthUtil.getAuthorization(anyString(), anyString()))
                .thenReturn(MOCK_AUTH_TOKEN);
    }

    @After
    public void tearDown() {
        mockTriNetAuthUtil.close();
    }

    // ── 200 OK – happy path ────────────────────────────────────────────────────

    @Test
    public void getPlanMapping_200Ok_returnsMappedPlans() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(RESPONSE_ONE_EMPLOYEE)));

        List<DefaultPlanMappingServiceClient.PlanMappingResponse> result =
                planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        assertNotNull(result);
        assertEquals(1, result.size());

        DefaultPlanMappingServiceClient.EmployeeResponse employee = result.get(0).getEmployeeResponseList().get(0);
        assertEquals("E1",   employee.getEmployeeId());
        assertEquals("MED1", employee.getMappedPlans().get("medical").getPlanId());
        assertEquals("DEN1", employee.getMappedPlans().get("dental").getPlanId());
    }

    @Test
    public void getPlanMapping_200Ok_multipleEmployees_returnsAll() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(RESPONSE_TWO_EMPLOYEES)));

        List<DefaultPlanMappingServiceClient.PlanMappingResponse> result =
                planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        assertEquals(2, result.get(0).getEmployeeResponseList().size());
    }

    @Test
    public void getPlanMapping_200Ok_emptyArray_returnsEmptyList() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        List<DefaultPlanMappingServiceClient.PlanMappingResponse> result =
                planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── non-2xx responses ──────────────────────────────────────────────────────

    @Test(expected = HttpClientErrorException.NotFound.class)
    public void getPlanMapping_404NotFound_throwsHttpClientErrorException() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse().withStatus(404)));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());
    }

    @Test(expected = HttpServerErrorException.InternalServerError.class)
    public void getPlanMapping_500ServerError_throwsHttpServerErrorException() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse().withStatus(500)));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());
    }

    // ── request verification ───────────────────────────────────────────────────

    @Test
    public void getPlanMapping_sendsAuthorizationAndScopeHeaders() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        verify(postRequestedFor(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .withHeader("Authorization", equalTo(MOCK_AUTH_TOKEN))
                .withHeader("scope",         equalTo(MOCK_SCOPE)));
    }

    @Test
    public void getPlanMapping_sendsCorrectContentTypeHeader() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        verify(postRequestedFor(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withHeader(HttpHeaders.ACCEPT,       equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    public void getPlanMapping_buildsCorrectUrlWithCompanyCode() {
        String anotherCompanyCode = "OTHER";
        stubFor(post(urlPathEqualTo("/plan-mapping/" + anotherCompanyCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        planMappingServiceClient.getPlanMapping(anotherCompanyCode, buildRequest());

        verify(postRequestedFor(urlPathEqualTo("/plan-mapping/" + anotherCompanyCode)));

    }

    @Test
    public void getPlanMapping_sendsRequestBodyFields() {
        stubFor(post(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        planMappingServiceClient.getPlanMapping(COMPANY_CODE, buildRequest());

        verify(postRequestedFor(urlPathEqualTo("/plan-mapping/" + COMPANY_CODE))
                .withRequestBody(matchingJsonPath("$.cloneBenefitProgram", equalTo("102")))
                .withRequestBody(matchingJsonPath("$.mappingRuleTemplateName", equalTo("PROSPECT_DEFAULT_PLAN_MAPPING")))
                .withRequestBody(matchingJsonPath("$.exchangeId", equalTo("Q1")))
                .withRequestBody(matchingJsonPath("$.targetEffectiveDate", equalTo("2025-01-01")))
                .withRequestBody(matchingJsonPath("$.employeeIds[0]")));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private DefaultPlanMappingServiceClient.PlanMappingRequest buildRequest() {
        return new DefaultPlanMappingServiceClient.PlanMappingRequest(
                "102", "PROSPECT_DEFAULT_PLAN_MAPPING", "Q1", "2025-01-01", Set.of("E1", "E2"),
                "CA", "94043", "541512");
    }
}


