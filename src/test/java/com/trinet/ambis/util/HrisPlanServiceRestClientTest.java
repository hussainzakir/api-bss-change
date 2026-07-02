package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.HrisURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.model.HrisPlanAttributeRequest;
import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.service.unit.ServiceUnitTest;


@RunWith(MockitoJUnitRunner.class)
public class HrisPlanServiceRestClientTest extends ServiceUnitTest {

    @InjectMocks
    private HrisPlanServiceRestClient hrisPlanServiceRestClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest httpRequest;

    private static final String HRIS_PLAN_API_URI = "http://localhost:8085/tib-integration";
    // Java
    private MockedStatic<BSSMessageConfig> bssMessageConfigMock;

    @Before
    public void setUp() {
        bssMessageConfigMock = mockStatic(BSSMessageConfig.class);
    }

    @After
    public void tearDown() {
        bssMessageConfigMock.close();
    }

    @Test
    public void testGetPlanAttributes_Success() {
        // Given
        List<String> validPlanIds = List.of("784527", "778533");
        HrisPlanAttributeRequest request = HrisPlanAttributeRequest.builder()
                .planIds(validPlanIds)
                .benefitType("10")
                .build();
        ResponseEntity<List<BenefitPlanCompare>> expectedResponse = populateResponseEntityBenefitPlanCompareList();
        ParameterizedTypeReference<List<BenefitPlanCompare>> typeReference = new ParameterizedTypeReference<List<BenefitPlanCompare>>() {};

        // Mock
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.GET), any(HttpEntity.class),
                Mockito.eq(typeReference))).thenReturn(expectedResponse);

        // When
        when(BSSMessageConfig.getProperty(HrisURIConstants.HRIS_PLAN_API_URI)).thenReturn(HRIS_PLAN_API_URI);
		List<BenefitPlanCompare> plans = hrisPlanServiceRestClient.getPlanAttributes(request, typeReference);

        // Then
        assertNotNull(plans);
        assertEquals(2, plans.size());
        BenefitPlanCompare plan1 = plans.get(0);
        assertEquals("10", plan1.getBenefitType());
        assertEquals("784527", plan1.getPlanId());
        assertEquals("Carrier A", plan1.getCarrier());
        assertEquals("[2025] Gold Plan $1250/$35/20%/$8600", plan1.getName());
        BenefitPlanCompare plan2 = plans.get(1);
        assertEquals("10", plan2.getBenefitType());
        assertEquals("778533", plan2.getPlanId());
        assertEquals("Carrier A", plan2.getCarrier());
        assertEquals("[2025] Prem Plan", plan2.getName());
        List<PlanCompareTemplate> templates = plan1.getTemplate();
        assertEquals(1, templates.size());
        assertEquals("medical_ouput", templates.get(0).getName());
        assertEquals("Category", templates.get(0).getType());
        assertEquals("Deductible", templates.get(0).getChildren().get(0).getName());
        assertEquals("Single Deductible", templates.get(0).getChildren().get(0).getChildren().get(0).getName());
    }

    @Test
    public void testGetBenefitPlanAvailability_Success() throws ParseException {
        // Given
        HrisPlanRequest request = HrisPlanRequest.builder()
                .benefitsType("medical")
                .hqState("CA")
                .hqZipCode("94107")
                .effDate("2025-04-01")
                .emplLocDetails(Arrays.asList(
                        HrisPlanRequest.LocationDetails.builder()
                                .homeState("MA")
                                .homeZipCodes(Arrays.asList("02108", "02110"))
                                .build()
                ))
                .build();
        ResponseEntity<List<HrisPlanResponse>> expectedResponse = populateResponseEntityHrisPlanResponseList();
        ParameterizedTypeReference<List<HrisPlanResponse>> typeReference = new ParameterizedTypeReference<List<HrisPlanResponse>>() {};

        // Mock
        when(restTemplate.exchange(anyString(), Mockito.eq(HttpMethod.POST), any(HttpEntity.class),
                Mockito.eq(typeReference))).thenReturn(expectedResponse);

        // When
        when(BSSMessageConfig.getProperty(HrisURIConstants.HRIS_PLAN_API_URI)).thenReturn(HRIS_PLAN_API_URI);
        List<HrisPlanResponse> plans = hrisPlanServiceRestClient.getBenefitPlanAvailability(request, typeReference);

        // Then
        assertNotNull(plans);
        assertEquals(1, plans.size());
        HrisPlanResponse plan1 = plans.get(0);
        assertEquals(12343, plan1.getPlanId());
        assertEquals("Some medical plan 1", plan1.getPlanName());
        assertEquals(1, plan1.getCarrierId());
        assertEquals("4tier", plan1.getRateDetails().getRateType());
        assertEquals(Arrays.asList("12434"), plan1.getRateDetails().getRatesByZip().get(0).getZips());
        assertEquals("1", plan1.getRateDetails().getRatesByZip().get(0).getRates().get(0).getTierCode());
        assertEquals(234.43, plan1.getRateDetails().getRatesByZip().get(0).getRates().get(0).getRate(), 0.01);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetPlanAttributes_InvalidPlanIds() {
        // Given
        List<String> invalidPlanIds = List.of("784527", "invalidId");
        HrisPlanAttributeRequest request = HrisPlanAttributeRequest.builder()
                .planIds(invalidPlanIds)
                .build();
        ParameterizedTypeReference<List<BenefitPlanCompare>> typeReference = new ParameterizedTypeReference<List<BenefitPlanCompare>>() {};

        // When
		hrisPlanServiceRestClient.getPlanAttributes(request, typeReference);
    }

    @Test(expected = RuntimeException.class)
    public void testGetPlanAttributes_EndPointFailure404() {
        // Given
        List<String> validPlanIds = List.of("784527", "778533");
        HrisPlanAttributeRequest request = HrisPlanAttributeRequest.builder()
                .planIds(validPlanIds)
                .build();
        ParameterizedTypeReference<List<BenefitPlanCompare>> typeReference = new ParameterizedTypeReference<List<BenefitPlanCompare>>() {};

        // When
		hrisPlanServiceRestClient.getPlanAttributes(request, typeReference);
    }

    @Test(expected = RuntimeException.class)
    public void testGetBenefitPlanAvailability_EndPointFailure404() throws ParseException {
        // Given
        HrisPlanRequest request = HrisPlanRequest.builder()
                .benefitsType("medical")
                .hqState("CA")
                .hqZipCode("94107")
                .effDate("2025-04-01")
                .emplLocDetails(Arrays.asList(
                        HrisPlanRequest.LocationDetails.builder()
                                .homeState("MA")
                                .homeZipCodes(Arrays.asList("02108", "02110"))
                                .build()
                ))
                .build();
        ParameterizedTypeReference<List<HrisPlanResponse>> typeReference = new ParameterizedTypeReference<List<HrisPlanResponse>>() {};

        // When
        hrisPlanServiceRestClient.getBenefitPlanAvailability(request, typeReference);
    }

    private ResponseEntity<List<BenefitPlanCompare>> populateResponseEntityBenefitPlanCompareList() {
        Attribute attributeSingleDeductible = Attribute.builder()
                .type("Attribute")
                .name("Single Deductible")
                .displayName("Single")
                .value("$1,250.00")
                .id(6)
                .build();

        Attribute attributeFamilyDeductible = Attribute.builder()
                .type("Attribute")
                .name("Family Deductible")
                .displayName("Family")
                .value( "$2,500.00")
                .id(7)
                .build();

        Attribute attributeDeductible = Attribute.builder()
                .type("Category")
                .name("Deductible")
                .displayOrder(2)
                .children(List.of(attributeSingleDeductible, attributeFamilyDeductible))
                .build();

        PlanCompareTemplate template = PlanCompareTemplate.builder()
                .type("Category")
                .name("medical_ouput")
                .displayOrder(1)
                .children(List.of(attributeDeductible))
                .build();

        BenefitPlanCompare planCompare1 = BenefitPlanCompare.builder()
                .planId("784527")
                .name("[2025] Gold Plan $1250/$35/20%/$8600")
                .benefitType("10")
                .carrier("Carrier A")
                .carrierLogoUrl("http://test/logoA.png")
                .template(List.of(template))
                .build();

        BenefitPlanCompare planCompare2 = BenefitPlanCompare.builder()
                .planId("778533")
                .name("[2025] Prem Plan")
                .benefitType("10")
                .carrier("Carrier A")
                .carrierLogoUrl("http://test/logoA.png")
                .template(List.of(template))
                .build();

        List<BenefitPlanCompare> expectedResponse = List.of(planCompare1, planCompare2);

        return new ResponseEntity<List<BenefitPlanCompare>>(expectedResponse, HttpStatus.OK);
    }

    private ResponseEntity<List<HrisPlanResponse>> populateResponseEntityHrisPlanResponseList() {
        return new ResponseEntity<List<HrisPlanResponse>>(List.of(
                HrisPlanResponse.builder()
                        .planId(12343)
                        .planName("Some medical plan 1")
                        .carrierId(1)
                        .carrierName("Carrier A")
                        .rateDetails(HrisPlanResponse.RateDetails.builder()
                                .rateType("4tier")
                                .ratesByZip(Arrays.asList(
                                        HrisPlanResponse.RateDetails.RatesByZip.builder()
                                                .zips(Arrays.asList("12434"))
                                                .rates(Arrays.asList(
                                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                                .tierCode("1")
                                                                .rate(234.43)
                                                                .build(),
                                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                                .tierCode("2")
                                                                .rate(234.43)
                                                                .build(),
                                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                                .tierCode("C")
                                                                .rate(234.43)
                                                                .build(),
                                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                                .tierCode("4")
                                                                .rate(234.43)
                                                                .build()
                                                ))
                                                .build()
                                ))
                                .build())
                        .build()
        ), HttpStatus.OK);
    }
}
