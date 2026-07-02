package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.BplURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.model.BplApiRequest;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenPlanCompareResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.common.SecurityConstants;

@RunWith(MockitoJUnitRunner.class)
public class BplServiceRestClientTest extends ServiceUnitTest {

	@InjectMocks
	private BplServiceRestClient bplServiceRestClient;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private HttpServletRequest httpRequest;

	private static final String BPL_API_URL = "http://localhost:8085/api-hw-benplanlib-plan/v2";

    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMock;
    private MockedStatic<BSSMessageConfig> bssMessageConfigMock;

    @Before
    public void setUp() {
        bssSecurityUtilsMock = mockStatic(BSSSecurityUtils.class);
        bssMessageConfigMock = mockStatic(BSSMessageConfig.class);
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMock.close();
        bssMessageConfigMock.close();
    }

	@Test
	public void testPrepareRequestAndCallEndPoint_Success() {
		// given
		MultiValueMap<String, String> queryParams = createQueryParams();
		Map<String, String> pathParams = createPathParams();
		
		// when
        bssMessageConfigMock.when(() -> BSSMessageConfig.getProperty(BplURIConstants.BPL_API_URI))
                .thenReturn(BPL_API_URL);
        ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>> typeReference = new ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>>() {
		};
		ResponseEntity<ReturnResponse<BenPlanCompareResponse>> apiResponse = populateResponseEntity();

		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(apiResponse);

		BplApiRequest bplApiRequest = createBplApiRequest(queryParams, pathParams,
				typeReference);

		// then
		ReturnResponse<BenPlanCompareResponse> response = bplServiceRestClient
				.prepareRequestAndCallEndPoint(bplApiRequest, typeReference);

		// Assertions
		assertNotNull(response);
		BenPlanCompareResponse data = response.getData();
		assertNotNull(data);

		List<BenefitPlanCompare> plans = data.getPlans();
		assertEquals(1, plans.size());

		BenefitPlanCompare plan = plans.get(0);
		assertEquals("Medical", plan.getBenefitType());
		assertEquals("003GUE", plan.getPlanId());
		assertEquals("Blue Shield of California", plan.getCarrier());

		List<PlanCompareTemplate> templates = plan.getTemplate();
		assertEquals(2, templates.size());
		assertEquals("Deductible", templates.get(0).getName());
		assertEquals("Out-of-Pocket Max", templates.get(1).getName());
	}

	@Test(expected = RuntimeException.class)
	public void testPrepareRequestAndCallEndPoint_Failure_404() {
		// given
		MultiValueMap<String, String> queryParams = createQueryParams();
		Map<String, String> pathParams = createPathParams();
		
		// when
		when(BSSMessageConfig.getProperty(BplURIConstants.BPL_API_URI)).thenReturn(BPL_API_URL);
		ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>> typeReference = new ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>>() {
		};

		ResponseEntity<ReturnResponse<BenPlanCompareResponse>> apiResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);

		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(apiResponse);

		BplApiRequest bplApiRequest = createBplApiRequest(queryParams, pathParams,
				typeReference);

		// then
		bplServiceRestClient.prepareRequestAndCallEndPoint(bplApiRequest, typeReference);
	}

	@Test
	public void testPrepareRequestAndCallEndPoint_EmptyResponse() {
		// given
		MultiValueMap<String, String> queryParams = createQueryParams();
		Map<String, String> pathParams = createPathParams();
		
		// when
        bssSecurityUtilsMock.when(() ->BSSMessageConfig.getProperty(BplURIConstants.BPL_API_URI)).thenReturn(BPL_API_URL);
		ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>> typeReference = new ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>>() {
		};

		ReturnResponse<BenPlanCompareResponse> emptyResponse = new ReturnResponse<>();
		ResponseEntity<ReturnResponse<BenPlanCompareResponse>> apiResponse = new ResponseEntity<>(emptyResponse,
				HttpStatus.OK);

		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				Mockito.eq(typeReference))).thenReturn(apiResponse);

		BplApiRequest bplApiRequest = createBplApiRequest(queryParams, pathParams,
				typeReference);

		// then
		ReturnResponse<BenPlanCompareResponse> response = bplServiceRestClient
				.prepareRequestAndCallEndPoint(bplApiRequest, typeReference);

		// Assertions
		assertNotNull(response);
		assertNull(response.getData());
	}

	private MultiValueMap<String, String> createQueryParams() {
		return new LinkedMultiValueMap<>(Map.of("planIds", List.of("003GUE,006IGO,002IL9,003GUM,006IGM"),
				"effectiveDate", List.of("2025-01-01"), "template", List.of("bss_export_template")));
	}

	private Map<String, String> createPathParams() {
		return Map.of(SecurityConstants.COMPANY_ID, "001", SecurityConstants.EMPLOYEE_ID, "101110121");
	}

	private BplApiRequest createBplApiRequest(MultiValueMap<String, String> queryParams, Map<String, String> pathParams,
			ParameterizedTypeReference<ReturnResponse<BenPlanCompareResponse>> typeReference) {
		return BplApiRequest.builder().method(HttpMethod.GET).uri(BplURIConstants.BPL_INFO_URI)
				.queryParams(queryParams).pathParams(pathParams).parameterizedTypeReference(typeReference).build();
	}

	private ResponseEntity<ReturnResponse<BenPlanCompareResponse>> populateResponseEntity() {
		ReturnResponse<BenPlanCompareResponse> returnResponse = new ReturnResponse<BenPlanCompareResponse>();

		Attribute deductibleSingle = Attribute.builder()
				.id(6)
				.displayName("Single")
				.name("Single Deductible")
				.value("$3,500")
				.dataType("CURRENCY")
				.transformedValue("3500")
				.build();

		Attribute deductibleFamily = Attribute.builder()
				.id(7)
				.displayName("Family")
				.name("Family Deductible")
				.value("$7,000")
				.dataType("CURRENCY")
				.transformedValue("7000")
				.build();

		Attribute single = Attribute.builder()
				.id(10)
				.displayName("Single")
				.name("Single Out-of-Pocket Max")
				.value("10%")
				.dataType("PERCENTAGE")
				.transformedValue("10")
				.build();

		PlanCompareTemplate deductibleTemplate = PlanCompareTemplate.builder()
				.type("Category")
				.name("Deductible")
				.displayOrder(2)
				.children(Arrays.asList(deductibleSingle, deductibleFamily))
				.build();

		PlanCompareTemplate outOfPocket = PlanCompareTemplate.builder()
				.type("Category")
				.name("Out-of-Pocket Max")
				.displayOrder(10)
				.children(Arrays.asList(single)).build();

		BenefitPlanCompare plan = BenefitPlanCompare.builder()
				.planId("003GUE").name("BS-CA HDHP 3500 CA North")
				.benefitType("Medical")
				.carrier("Blue Shield of California")
				.carrierLogoUrl(null)
				.template(Arrays.asList(deductibleTemplate, outOfPocket)).build();
		BenPlanCompareResponse benPlanCompareResponse = new BenPlanCompareResponse();
		benPlanCompareResponse.setPlans(Arrays.asList(plan));

		returnResponse.setData(benPlanCompareResponse);
		ResponseEntity<ReturnResponse<BenPlanCompareResponse>> resp = new ResponseEntity<ReturnResponse<BenPlanCompareResponse>>(
				returnResponse, HttpStatus.OK);
		return resp;
	}

}
