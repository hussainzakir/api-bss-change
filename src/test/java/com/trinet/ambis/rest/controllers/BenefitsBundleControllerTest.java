package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.rest.controllers.dto.BundlePlanResponse;
import com.trinet.ambis.rest.controllers.dto.BundlePlansDto;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.model.BenefitsBundleDto;

import java.time.LocalDate;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import com.trinet.ambis.service.model.bundle.BundleDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class BenefitsBundleControllerTest {

    @InjectMocks
    private BenefitsBundleController benefitsBundleController;

    @Mock
    private BenefitsBundleService benefitsBundleService;

    private MockMvc mockMvc;

    private static final String URI = URIConstants.VERSION_AND_ROOT + URIConstants.BUNDLE_BY_QUARTER;
    private static final String BUNDLE_PLANS_URI = URIConstants.VERSION_AND_ROOT + URIConstants.BUNDLE_PLANS;
    private static final String BUNDLE_SELECTION_DETAILS_URI = URIConstants.VERSION_AND_ROOT + URIConstants.BUNDLE_SELECTION_DETAILS;
    private static final String OE_QUARTER = "Q3";
    private static final String EFFECTIVE_DATE = "01-JULY-2025";
    private static final String COMPANY_CODE = "ABC123";
    private static final String EXCHANGE_ID = "EXG001";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BUNDLES_PLANS_URI = URIConstants.VERSION_AND_ROOT + URIConstants.BUNDLES_PLANS;
    private static final String QUARTER = "Q3";
    private static final LocalDate EFFECTIVE_LOCAL_DATE = LocalDate.of(2025, 7, 1);
    private static final String EFFECTIVE_DATE_ISO = "2025-07-01";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(benefitsBundleController).build();
    }

    /**
     * Given: A valid oeQuarter and effectiveDate, and the service returns a list of bundles
     * When: A GET request is made to the endpoint with valid parameters
     * Then: The response status is 200 OK, and the response contains the expected bundle details
     */
    @Test
    public void testGetBundlesDetails_Success() throws Exception {
        // Given
        List<BenefitsBundleDto> mockResponse = prepareMockBundleDtoList();
        when(benefitsBundleService.getBundleDetails(OE_QUARTER, EFFECTIVE_DATE)).thenReturn(mockResponse);

        // When
        MockHttpServletResponse response = performGetRequest(OE_QUARTER, EFFECTIVE_DATE);

        // Then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("Bundle Name 1"));
    }

    /**
     * Given: A valid oeQuarter and effectiveDate, and the service returns an empty list
     * When: A GET request is made to the endpoint with valid parameters
     * Then: The response status is 200 OK, and the response contains an empty JSON array
     */
    @Test
    public void testGetBundlesDetails_EmptyResult() throws Exception {
        // Given
        when(benefitsBundleService.getBundleDetails(OE_QUARTER, EFFECTIVE_DATE)).thenReturn(new ArrayList<>());

        // When
        MockHttpServletResponse response = performGetRequest(OE_QUARTER, EFFECTIVE_DATE);

        // Then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("[]", response.getContentAsString());
    }

    /**
     * Given: A valid oeQuarter and effectiveDate, and the service returns null
     * When: A GET request is made to the endpoint with valid parameters
     * Then: The response status is 200 OK, and the response body is empty
     */
    @Test
    public void testGetBundlesDetails_NullServiceResponse() throws Exception {
        // Given
        when(benefitsBundleService.getBundleDetails(OE_QUARTER, EFFECTIVE_DATE)).thenReturn(null);

        // When
        MockHttpServletResponse response = performGetRequest(OE_QUARTER, EFFECTIVE_DATE);

        // Then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }

    /**
     * Given: The oeQuarter parameter is missing
     * When: A GET request is made without the oeQuarter parameter
     * Then: The response status is 400 Bad Request
     */
    @Test
    public void testGetBundlesDetails_MissingOeQuarter() throws Exception {
        // When
        MockHttpServletResponse response = performGetRequest(null, EFFECTIVE_DATE);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    /**
     * Given: The effectiveDate parameter is missing
     * When: A GET request is made without the effectiveDate parameter
     * Then: The response status is 400 Bad Request
     */
    @Test
    public void testGetBundlesDetails_MissingEffectiveDate() throws Exception {
        // When
        MockHttpServletResponse response = performGetRequest(OE_QUARTER, null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    /**
     * Given: Both oeQuarter and effectiveDate parameters are missing
     * When: A GET request is made without any parameters
     * Then: The response status is 400 Bad Request
     */
    @Test
    public void testGetBundlesDetails_MissingBothParameters() throws Exception {
        // When
        MockHttpServletResponse response = performGetRequest(null, null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    /**
     * Given: Valid effectiveDate and quarter, service returns a BundlePlanResponse with bundles
     * When: GET request is made to /bundles/plans
     * Then: 200 OK with bundle names and plan IDs in the response body
     */
    @Test
    public void testGetBundlesPlans_Success() throws Exception {
        // given
        BundlePlanResponse mockResponse = prepareMockBundlePlanResponse();
        when(benefitsBundleService.getBundlesByEffectiveDateAndQuarter(EFFECTIVE_LOCAL_DATE, QUARTER))
                .thenReturn(mockResponse);

        // when
        MockHttpServletResponse response = performGetBundlePlansRequest(EFFECTIVE_DATE_ISO, QUARTER);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("Bundle A"));
        assertTrue(response.getContentAsString().contains("00A111"));
        assertTrue(response.getContentAsString().contains("00A112"));
    }

    /**
     * Given: Valid effectiveDate and quarter, service returns a response with empty bundles list
     * When: GET request is made to /bundles/plans
     * Then: 200 OK with an empty bundles array
     */
    @Test
    public void testGetBundlesPlans_EmptyBundleList() throws Exception {
        // given
        BundlePlanResponse emptyResponse = BundlePlanResponse.builder().bundles(new ArrayList<>()).build();
        when(benefitsBundleService.getBundlesByEffectiveDateAndQuarter(EFFECTIVE_LOCAL_DATE, QUARTER))
                .thenReturn(emptyResponse);

        // when
        MockHttpServletResponse response = performGetBundlePlansRequest(EFFECTIVE_DATE_ISO, QUARTER);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"bundles\":[]"));
    }

    /**
     * Given: effectiveDate parameter is missing
     * When: GET request is made to /bundles/plans without effectiveDate
     * Then: 400 Bad Request
     */
    @Test
    public void testGetBundlesPlans_MissingEffectiveDate() throws Exception {
        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(BUNDLES_PLANS_URI, "001", "00002222256")
                .param("quarter", QUARTER)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    /**
     * Given: quarter parameter is missing
     * When: GET request is made to /bundles/plans without quarter
     * Then: 400 Bad Request
     */
    @Test
    public void testGetBundlesPlans_MissingQuarter() throws Exception {
        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(BUNDLES_PLANS_URI, "001", "00002222256")
                .param("effectiveDate", EFFECTIVE_DATE_ISO)
                .accept(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    /**
     * Given: effectiveDate is in an invalid format (not ISO date)
     * When: GET request is made to /bundles/plans
     * Then: 400 Bad Request
     */
    @Test
    public void testGetBundlesPlans_InvalidDateFormat() throws Exception {
        // when
        MockHttpServletResponse response = performGetBundlePlansRequest("01-JULY-2025", QUARTER);

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    /**
     * Given: Valid inputs and service returns multiple bundles each with multiple plans
     * When: GET request is made to /bundles/plans
     * Then: 200 OK and response contains all bundle names
     */
    @Test
    public void testGetBundlesPlans_MultipleBundles() throws Exception {
        // given
        BundlePlanResponse mockResponse = BundlePlanResponse.builder()
                .bundles(List.of(
                        BundlePlanResponse.BundleDto.builder()
                                .id(1L).name("Bundle A").type("Medical")
                                .benefitPlanIds(List.of("REGIONAL-PLAN-001"))
                                .build(),
                        BundlePlanResponse.BundleDto.builder()
                                .id(2L).name("Bundle B").type("Dental")
                                .benefitPlanIds(List.of("REGIONAL-PLAN-002", "REGIONAL-PLAN-003"))
                                .build()
                ))
                .build();
        when(benefitsBundleService.getBundlesByEffectiveDateAndQuarter(EFFECTIVE_LOCAL_DATE, QUARTER))
                .thenReturn(mockResponse);

        // when
        MockHttpServletResponse response = performGetBundlePlansRequest(EFFECTIVE_DATE_ISO, QUARTER);

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("Bundle A"));
        assertTrue(response.getContentAsString().contains("Bundle B"));
        assertTrue(response.getContentAsString().contains("REGIONAL-PLAN-002"));
        assertTrue(response.getContentAsString().contains("REGIONAL-PLAN-003"));
    }

    private MockHttpServletResponse performGetBundlePlansRequest(String effectiveDate, String quarter) throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(BUNDLES_PLANS_URI, "001", "00002222256")
                .param("effectiveDate", effectiveDate)
                .param("quarter", quarter)
                .accept(MediaType.APPLICATION_JSON);
        return mockMvc.perform(requestBuilder).andReturn().getResponse();
    }

    private BundlePlanResponse prepareMockBundlePlanResponse() {
        BundlePlanResponse.BundleDto bundleDto = BundlePlanResponse.BundleDto.builder()
                .id(1L)
                .name("Bundle A")
                .type("Medical")
                .benefitPlanIds(List.of("00A111", "00A112"))
                .build();
        return BundlePlanResponse.builder()
                .bundles(List.of(bundleDto))
                .build();
    }

    @Test
    public void testGetBundlePlanExchangePlans_Success() throws Exception {
        // Given
        List<BundlePlansDto> mockResponse = List.of(
                BundlePlansDto.builder()
                        .benefitType("10")
                        .bundleDetails(List.of(BundleDetail.builder()
                                .benefitBundleId(101)
                                .primaryCarrierId(2001)
                                .bundlePlanDetails(List.of(
                                        BundleDetail.BundlePlanDetail.builder()
                                                .planId("PLAN_A")
                                                .cvgLevelCost(List.of(
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("1").totalCost(new java.math.BigDecimal("100.00")).build(),
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("2").totalCost(new java.math.BigDecimal("200.00")).build(),
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("C").totalCost(new java.math.BigDecimal("250.00")).build(),
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("4").totalCost(new java.math.BigDecimal("300.00")).build()
                                                ))
                                                .build(),
                                        BundleDetail.BundlePlanDetail.builder()
                                                .planId("PLAN_B")
                                                .cvgLevelCost(List.of(
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("1").totalCost(new java.math.BigDecimal("150.00")).build(),
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("2").totalCost(new java.math.BigDecimal("250.00")).build(),
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("C").totalCost(new java.math.BigDecimal("275.00")).build(),
                                                        BundleDetail.PlanCostRequest.builder().covrgCd("4").totalCost(new java.math.BigDecimal("350.00")).build()
                                                ))
                                                .build()
                                ))
                                .build()))
                        .build());
        Set<Long> bundleIds = Set.of(101L, 102L);
        when(benefitsBundleService.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, bundleIds)).thenReturn(mockResponse);

        // When
        MockHttpServletResponse response = performBundlePlansGetRequest(EXCHANGE_ID, "101", "102");

        // Then
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        JsonNode root = objectMapper.readTree(response.getContentAsString());
        assertTrue(root.isArray());
        assertEquals(1, root.size());

        JsonNode benefitTypeResponse = root.get(0);
        assertEquals("10", benefitTypeResponse.get("benefitType").asText());

        JsonNode bundleDetails = benefitTypeResponse.get("bundleDetails");
        assertTrue(bundleDetails.isArray());
        assertEquals(1, bundleDetails.size());

        JsonNode bundleDetail = bundleDetails.get(0);
        assertEquals(101, bundleDetail.get("benefitBundleId").asInt());
        assertEquals(2001, bundleDetail.get("primaryCarrierId").asInt());

        JsonNode bundlePlanDetails = bundleDetail.get("bundlePlanDetails");
        assertTrue(bundlePlanDetails.isArray());
        assertEquals(2, bundlePlanDetails.size());

        JsonNode planA = findByPlanId(bundlePlanDetails, "PLAN_A");
        assertEquals("PLAN_A", planA.get("planId").asText());
        assertCost(planA.get("cvgLevelCost"), "1", "100.00");
        assertCost(planA.get("cvgLevelCost"), "2", "200.00");
        assertCost(planA.get("cvgLevelCost"), "C", "250.00");
        assertCost(planA.get("cvgLevelCost"), "4", "300.00");

        JsonNode planB = findByPlanId(bundlePlanDetails, "PLAN_B");
        assertEquals("PLAN_B", planB.get("planId").asText());
        assertCost(planB.get("cvgLevelCost"), "1", "150.00");
        assertCost(planB.get("cvgLevelCost"), "2", "250.00");
        assertCost(planB.get("cvgLevelCost"), "C", "275.00");
        assertCost(planB.get("cvgLevelCost"), "4", "350.00");

        verify(benefitsBundleService).getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, bundleIds);
    }

    @Test
    public void testGetBundlePlanExchangePlans_WithoutBundleIds_Success() throws Exception {
        // Given
        when(benefitsBundleService.getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null)).thenReturn(new ArrayList<>());

        // When
        MockHttpServletResponse response = performBundlePlansGetRequest(EXCHANGE_ID);

        // Then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("[]", response.getContentAsString());
        verify(benefitsBundleService).getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);
    }

    @Test
    public void testGetBundlePlanExchangePlans_MissingExchangeId() throws Exception {
        // When
        MockHttpServletResponse response = performBundlePlansGetRequest(null, "101");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        verify(benefitsBundleService, never()).getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, Set.of(101L));
    }

    @Test
    public void testGetBundlePlanExchangePlans_InvalidBundleId() throws Exception {
        // When
        MockHttpServletResponse response = performBundlePlansGetRequest(EXCHANGE_ID, "not-a-number");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        verify(benefitsBundleService, never()).getBundleAndExchangePlans(COMPANY_CODE, EXCHANGE_ID, null);
    }

    /**
     * GIVEN a valid request with company code and exchange date pairs,
     * WHEN getBundleSelectionDetails is called,
     * THEN the controller should return a list of BundleSelectionDetailsDto
     */
    @Test
    public void getBundleSelectionDetails_Success() throws Exception {
        BundleSelectionDetailsRequest request = BundleSelectionDetailsRequest.builder()
                .companyCode("001Ea00001LXQzEIAX")
                .exchangeDatePairs(List.of(
                        BundleSelectionDetailsRequest.ExchangeDates.builder()
                                .exchange("TriNet II").effectiveDate("2026-03-01").build()
                ))
                .build();

        List<BundleSelectionDetailsDto> expectedList = new ArrayList<>();
        expectedList.add(BundleSelectionDetailsDto.builder()
                .companyId(192557L).exchangeId("TN-II").build());

        when(benefitsBundleService.getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class)))
                .thenReturn(expectedList);

        MockHttpServletResponse response = performBundleSelectionDetailsPostRequest(request);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        JsonNode root = objectMapper.readTree(response.getContentAsString());
        assertTrue(root.isArray());
        assertEquals(1, root.size());
        assertEquals(192557L, root.get(0).get("companyId").asLong());
        assertEquals("TN-II", root.get(0).get("exchangeId").asText());
        verify(benefitsBundleService).getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class));
    }

    /**
     * GIVEN a valid request with multiple exchange date pairs,
     * WHEN getBundleSelectionDetails is called,
     * THEN the controller should return all matching records
     */
    @Test
    public void getBundleSelectionDetails_MultipleRecords() throws Exception {
        BundleSelectionDetailsRequest request = BundleSelectionDetailsRequest.builder()
                .companyCode("G48")
                .exchangeDatePairs(List.of(
                        BundleSelectionDetailsRequest.ExchangeDates.builder()
                                .exchange("TriNet III").effectiveDate("2026-01-01").build(),
                        BundleSelectionDetailsRequest.ExchangeDates.builder()
                                .exchange("TriNet II").effectiveDate("2026-01-01").build()
                ))
                .build();

        List<BundleSelectionDetailsDto> expectedList = new ArrayList<>();
        expectedList.add(BundleSelectionDetailsDto.builder()
                .companyId(100001L).exchangeId("TNIII").build());
        expectedList.add(BundleSelectionDetailsDto.builder()
                .companyId(100002L).exchangeId("TNII").build());

        when(benefitsBundleService.getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class)))
                .thenReturn(expectedList);

        MockHttpServletResponse response = performBundleSelectionDetailsPostRequest(request);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        JsonNode root = objectMapper.readTree(response.getContentAsString());
        assertTrue(root.isArray());
        assertEquals(2, root.size());
        assertEquals("TNIII", root.get(0).get("exchangeId").asText());
        assertEquals("TNII", root.get(1).get("exchangeId").asText());
        verify(benefitsBundleService).getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class));
    }

    /**
     * GIVEN a valid request with no matching bundles,
     * WHEN getBundleSelectionDetails is called,
     * THEN the controller should return an empty list
     */
    @Test
    public void getBundleSelectionDetails_EmptyResult() throws Exception {
        BundleSelectionDetailsRequest request = BundleSelectionDetailsRequest.builder()
                .companyCode("NONEXISTENT")
                .exchangeDatePairs(List.of(
                        BundleSelectionDetailsRequest.ExchangeDates.builder()
                                .exchange("TriNet I").effectiveDate("2026-03-01").build()
                ))
                .build();

        when(benefitsBundleService.getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class)))
                .thenReturn(new ArrayList<>());

        MockHttpServletResponse response = performBundleSelectionDetailsPostRequest(request);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("[]", response.getContentAsString());
        verify(benefitsBundleService).getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class));
    }


    /**
     * GIVEN valid bundle selection details with null exchangeId,
     * WHEN getBundleSelectionDetails is called,
     * THEN the controller should return records with null exchangeId
     */
    @Test
    public void getBundleSelectionDetails_NullExchangeId() throws Exception {
        BundleSelectionDetailsRequest request = BundleSelectionDetailsRequest.builder()
                .companyCode("G48")
                .exchangeDatePairs(List.of(
                        BundleSelectionDetailsRequest.ExchangeDates.builder()
                                .exchange("TriNet III").effectiveDate("2026-03-01").build()
                ))
                .build();

        List<BundleSelectionDetailsDto> expectedList = new ArrayList<>();
        expectedList.add(BundleSelectionDetailsDto.builder()
                .companyId(100001L).exchangeId(null).build());

        when(benefitsBundleService.getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class)))
                .thenReturn(expectedList);

        MockHttpServletResponse response = performBundleSelectionDetailsPostRequest(request);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        JsonNode root = objectMapper.readTree(response.getContentAsString());
        assertEquals(1, root.size());
        assertTrue(root.get(0).get("exchangeId").isNull());
        verify(benefitsBundleService).getBundleSelectionDetails(org.mockito.ArgumentMatchers.any(BundleSelectionDetailsRequest.class));
    }

    private MockHttpServletResponse performGetRequest(String oeQuarter, String effectiveDate) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get(URI, "001", "00002222256").accept(MediaType.APPLICATION_JSON);

        if (oeQuarter != null) {
            requestBuilder.param("oeQuarter", oeQuarter);
        }
        if (effectiveDate != null) {
            requestBuilder.param("effectiveDate", effectiveDate);
        }

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        return result.getResponse();
    }

    private MockHttpServletResponse performBundlePlansGetRequest(String exchangeId, String... bundleIds) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get(BUNDLE_PLANS_URI, "001", "00002222256", COMPANY_CODE)
                        .accept(MediaType.APPLICATION_JSON);

        if (exchangeId != null) {
            requestBuilder.param("exchangeId", exchangeId);
        }
        if (bundleIds != null && bundleIds.length > 0) {
            requestBuilder.param("bundleIds", bundleIds);
        }

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        return result.getResponse();
    }

    private MockHttpServletResponse performBundleSelectionDetailsPostRequest(BundleSelectionDetailsRequest request) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(request);
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post(BUNDLE_SELECTION_DETAILS_URI, "001", "00002222256", request.getCompanyCode())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
                        .accept(MediaType.APPLICATION_JSON);


        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        return result.getResponse();
    }

    private List<BenefitsBundleDto> prepareMockBundleDtoList() {
        List<BenefitsBundleDto> bundleDtoList = new ArrayList<>();
        BenefitsBundleDto.BundleDetails details1 = BenefitsBundleDto.BundleDetails.builder()
                .id(1L)
                .name("Bundle Name 1")
                .build();
        BenefitsBundleDto bundle1 = BenefitsBundleDto.builder()
                .quarter(OE_QUARTER)
                .bundles(List.of(details1))
                .build();
        bundleDtoList.add(bundle1);

        BenefitsBundleDto.BundleDetails details2 = BenefitsBundleDto.BundleDetails.builder()
                .id(2L)
                .name("Bundle Name 2")
                .build();
        BenefitsBundleDto bundle2 = BenefitsBundleDto.builder()
                .quarter(OE_QUARTER)
                .bundles(List.of(details2))
                .build();
        bundleDtoList.add(bundle2);

        return bundleDtoList;
    }

    private JsonNode findByPlanId(JsonNode plans, String planId) {
        for (JsonNode plan : plans) {
            if (planId.equals(plan.get("planId").asText())) {
                return plan;
            }
        }
        throw new AssertionError("Plan not found: " + planId);
    }

    private void assertCost(JsonNode cvgLevelCost, String coverageCode, String expectedCost) {
        for (JsonNode cost : cvgLevelCost) {
            if (coverageCode.equals(cost.get("covrgCd").asText())) {
                assertEquals(0, new BigDecimal(expectedCost).compareTo(cost.get("totalCost").decimalValue()));
                return;
            }
        }
        throw new AssertionError("Coverage code not found: " + coverageCode);
    }
}