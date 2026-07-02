package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.trinet.ambis.configuration.BSSMessageConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
import org.springframework.http.HttpStatus;

import com.trinet.ambis.service.impl.BplServiceImpl;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenPlanCompareResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.service.prospect.enums.BenefitTypeEnum;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.BplServiceRestClient;
import com.trinet.domain.common.ReturnResponse;


@RunWith(MockitoJUnitRunner.class)
public class BplServiceTest extends ServiceUnitTest {

	@InjectMocks
	private BplServiceImpl bplService;

	@Mock
	private BplServiceRestClient bplServiceRestClient;

	@Mock
	private HttpServletRequest httpRequest;

	private static String TEMPLATE_TYPE = "bss_export_template";

	private Date effectiveDate;

    private MockedStatic<BSSMessageConfig> bssMessageConfigMock;
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMock;

    @Before
    public void setUp() {
        bssMessageConfigMock = Mockito.mockStatic(BSSMessageConfig.class);
        bssSecurityUtilsMock = Mockito.mockStatic(BSSSecurityUtils.class);
        effectiveDate = new Date();
    }

    @After
    public void tearDown() {
        bssMessageConfigMock.close();
        bssSecurityUtilsMock.close();
    }

	/**
	 * given planId's and effective date
	 * when getBPLAttributes method is called
	 * then return the CompletableFuture of Benefit Plan Compare details
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 **/
	@Test
	public void getBPLAttributesTest1() throws InterruptedException, ExecutionException {
		// Given
		String planIds = "003GUE,006IGO,002IL9,003GUM,006IGM";
		Set<String> planIdsSet = Set.of(planIds.split(","));

		// Mock
		Mockito.when(BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest)).thenReturn("001");
		Mockito.when(BSSSecurityUtils.getAuthenticatedEmplId(httpRequest)).thenReturn("101110121");
		Mockito.when(bplServiceRestClient.prepareRequestAndCallEndPoint(any(), any())).thenReturn(preparePlanResponse());

		// When
		CompletableFuture<List<BenefitPlanCompare>> bplAttributes = bplService.getBPLAttributes(planIdsSet, effectiveDate, TEMPLATE_TYPE, httpRequest);

		// Then
		verifyBPLAttributes(bplAttributes);
		verify(bplServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any(), any());
	}

	/**
	 * given empty planId's and effective date
	 * when getBPLAttributes method is called
	 * then return the empty CompletableFuture of Benefit Plan Compare details
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 **/
	@Test
	public void getBPLAttributesInvalidEmptyPlanIds() throws ExecutionException, InterruptedException {
		// Given
		Set<String> planIdsSet = Collections.emptySet();

		// When
		CompletableFuture<List<BenefitPlanCompare>> bplAttributes = bplService.getBPLAttributes(planIdsSet, effectiveDate, TEMPLATE_TYPE, httpRequest);

		// Then
		List<BenefitPlanCompare> attributeList = bplAttributes.get();
		assertEquals(0, attributeList.size());
	}

	/**
	 * given planId's and effective date
	 * when getBPLAttributes method is called
	 * then return the empty CompletableFuture of Benefit Plan Compare details with error status
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 **/
	@Test
	public void getBPLAttributesInvalidResponse() throws ExecutionException, InterruptedException {
		// Given
		String planIds = "003GUE,006IGO,002IL9,003GUM,006IGM";
		Set<String> planIdsSet = Set.of(planIds.split(","));

		// Mock
		Mockito.when(BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest)).thenReturn("001");
		Mockito.when(BSSSecurityUtils.getAuthenticatedEmplId(httpRequest)).thenReturn("101110121");

		ReturnResponse<BenPlanCompareResponse> response = new ReturnResponse<>();
		response.setStatusCode("500");

		Mockito.when(bplServiceRestClient.prepareRequestAndCallEndPoint(any(), any())).thenReturn(response);

		// When
		CompletableFuture<List<BenefitPlanCompare>> bplAttributes = bplService.getBPLAttributes(planIdsSet, effectiveDate, TEMPLATE_TYPE, httpRequest);

		// Then
		List<BenefitPlanCompare> attributeList = bplAttributes.get();
		assertEquals(0, attributeList.size());

		// Verify
		verify(bplServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any(), any());
	}

	/**
	 * given planId's and effective date
	 * when getBPLAttributes method is called
	 * then return the RuntimeException with API call failed message
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 **/
	@Test
	public void getBPLAttributesExceptionTest() throws ExecutionException, InterruptedException {
		// Given
		String planIds = "003GUE,006IGO,002IL9,003GUM,006IGM";
		Set<String> planIdsSet = Set.of(planIds.split(","));

		// Mock
		Mockito.when(BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest)).thenReturn("001");
		Mockito.when(BSSSecurityUtils.getAuthenticatedEmplId(httpRequest)).thenReturn("101110121");

		Mockito.when(bplServiceRestClient.prepareRequestAndCallEndPoint(any(), any()))
		.thenThrow(new RuntimeException("API call failed"));

		// When
		CompletableFuture<List<BenefitPlanCompare>> bplAttributes = bplService.getBPLAttributes(planIdsSet, effectiveDate, TEMPLATE_TYPE, httpRequest);

		// Then
		List<BenefitPlanCompare> attributeList = bplAttributes.get();
		assertEquals(0, attributeList.size());

		// Verify
		verify(bplServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any(), any());
	}

	private void verifyBPLAttributes(CompletableFuture<List<BenefitPlanCompare>> bplAttributes) throws ExecutionException, InterruptedException {
		List<BenefitPlanCompare> attributeList = bplAttributes.get();
		assertEquals(6, attributeList.size());
		verifyBenefitPlanCompare(attributeList.get(0), "Medical", "003GUE", "Blue Shield of California", "BS-CA HDHP 3500 CA North");
		verifyBenefitPlanCompare(attributeList.get(1), "Medical", "003GUM", "Kaiser Permanente", "Kaiser HMO/HDHP 3500 CA North");
		verifyBenefitPlanCompare(attributeList.get(2), "Dental", "006IGO", "Delta Dental", "Delta Dental 100");
		verifyBenefitPlanCompare(attributeList.get(3), "Dental", "006IGM", "Delta Dental", "Delta Dental 0");
		verifyBenefitPlanCompare(attributeList.get(4), "Vision", "002IL9", "Vision Service Plan (VSP)", "VSP Vision Plus");
		verifyBenefitPlanCompare(attributeList.get(5), "Vision", "004S5I", "Aetna", "Aetna EyeMed");

	}

	private void verifyBenefitPlanCompare(BenefitPlanCompare planCompare, String expectedBenefitType, String expectedPlanId,
			String expectedCarrier, String expectedName) {
		assertEquals(expectedBenefitType, planCompare.getBenefitType());
		assertEquals(expectedPlanId, planCompare.getPlanId());
		assertEquals(expectedCarrier, planCompare.getCarrier());
		assertEquals(expectedName, planCompare.getName());

		List<PlanCompareTemplate> template = planCompare.getTemplate();
		assertEquals(1, template.size());
		assertEquals("attr1", template.get(0).getName());

		List<Attribute> children = template.get(0).getChildren();
		assertEquals(1, children.size());
		assertEquals("Attribute 1", children.get(0).getName());
	}


	private ReturnResponse<BenPlanCompareResponse> preparePlanResponse() {
		List<BenefitPlanCompare> plansList = Arrays.asList(
				preparePlanCompare("003GUE", "BS-CA HDHP 3500 CA North", "Blue Shield of California", BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc()),
				preparePlanCompare("003GUM", "Kaiser HMO/HDHP 3500 CA North", "Kaiser Permanente",  BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc()),
				preparePlanCompare("006IGO", "Delta Dental 100", "Delta Dental",  BenefitTypeEnum.DENTAL.getBcrBenTypeDesc()),
				preparePlanCompare("006IGM", "Delta Dental 0", "Delta Dental",  BenefitTypeEnum.DENTAL.getBcrBenTypeDesc()),
				preparePlanCompare("002IL9", "VSP Vision Plus", "Vision Service Plan (VSP)",  BenefitTypeEnum.VISION.getBcrBenTypeDesc()),
				preparePlanCompare("004S5I", "Aetna EyeMed", "Aetna",  BenefitTypeEnum.VISION.getBcrBenTypeDesc())
				);

		BenPlanCompareResponse benPlanCompareResponse = new BenPlanCompareResponse();
		benPlanCompareResponse.setPlans(plansList);

		ReturnResponse<BenPlanCompareResponse> response = new ReturnResponse<>();
		response.setData(benPlanCompareResponse);
		response.setStatusCode(String.valueOf(HttpStatus.OK.value()));

		return response;
	}

	private BenefitPlanCompare preparePlanCompare(String planId, String planName, String carrier, String planType) {
		return BenefitPlanCompare.builder()
				.planId(planId)
				.name(planName)
				.carrier(carrier)
				.benefitType(planType)
				.template(prepareAttrPlanTemplate())
				.build();
	}

	private List<PlanCompareTemplate> prepareAttrPlanTemplate() {
		Attribute attribute = Attribute.builder()
				.name("Attribute 1")
				.displayName("Attribute 1")
				.value("Value")
				.build();

		PlanCompareTemplate template = PlanCompareTemplate.builder()
				.name("attr1")
				.children(List.of(attribute))
				.build();

		return List.of(template);
	}

}