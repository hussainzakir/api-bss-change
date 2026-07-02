package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeDesc;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsReportDetails;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.PlanOfferingsRequestBuilder;
import com.trinet.ambis.service.impl.planofferings.PlanOfferingsServiceImpl;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RestApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlanOfferingsServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	PlanOfferingsServiceImpl planOfferingsService;

	@Mock
	RestApiClient restApiClient;

	@Mock
	PlanOfferingsRequestBuilder planOfferingsRequestBuilder;

	@Mock
	HttpServletRequest httpServletRequest;

	@Mock
	PlanOfferingsRequest planOfferingsRequest;
	
	private static final String EMPLID = "00002222287";
	private static final String ADMIN_COMPANY_CODE = "001";
	private static final String PERSON_ID = "00002222267";
	
	private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;
	
	@Before
	public void setUp() {
		bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);
		 ReflectionTestUtils.setField(planOfferingsService, "docGenUrl",
					"https://trinetqen1.hrpassport.com/api-docgen/v1/doc-gen/");
		when(BSSSecurityUtils.getAuthenticatedCompanyCode(httpServletRequest)).thenReturn(ADMIN_COMPANY_CODE);
		when(BSSSecurityUtils.getAuthenticatedEmplId(httpServletRequest)).thenReturn(EMPLID);
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(PERSON_ID);
	}

	@After
	public void tearDown() {
		bssSecurityUtilsMockedStatic.close();
	}

	@Test
	public void testGeneratePlanOfferingsReport() {
		PlanOfferingsReportDetails details = getPlanOfferingReportDetails();
		byte[] responseBytes = "reportBytes".getBytes();

		when(planOfferingsRequestBuilder.buildPlanOfferingsReportRequest(any(), any())).thenReturn(details);

		when(restApiClient.getReturnResponse(any(), eq(details), anyString(), eq(HttpMethod.POST)))
				.thenReturn(responseBytes);

		byte[] template = planOfferingsService.generatePlanOfferingsReport(planOfferingsRequest, httpServletRequest);
		assertNotNull(template);
		assertTrue(template.length > 0);
	}
	
	
	@Test(expected = BSSApplicationException.class)
	public void testGeneratePlanOfferingsReportWithNoPlans() {
		PlanOfferingsReportDetails details = getReportDetailsNoPlans();
		when(planOfferingsRequestBuilder.buildPlanOfferingsReportRequest(any(), any())).thenReturn(details);

		planOfferingsService.generatePlanOfferingsReport(planOfferingsRequest, httpServletRequest);
	}
	

	private PlanOfferingsReportDetails getPlanOfferingReportDetails() {
		List<AttributeDesc> attributeNames = new ArrayList<>();
		attributeNames.add(AttributeDesc.builder().name("Plan Name").build());
		
		PlanAppendix appendix = new PlanAppendix();
		appendix.setAttributeNames(attributeNames);
		appendix.setPlanAttributes(Collections.emptyList());
		appendix.setAdditionalGroupDetails(Collections.emptyList());
		appendix.setAdditionalGroupAttributeNames(Collections.emptyList());

		Map<String, PlanAppendix> planOfferings = new HashMap<>();
		planOfferings.put("plan", appendix);

		PlanOfferingsData data = new PlanOfferingsData();
		data.setPlanOfferings(planOfferings);

		PlanOfferingsReportDetails details = new PlanOfferingsReportDetails();
		details.setData(data);

		return details;
	}
	
	private PlanOfferingsReportDetails getReportDetailsNoPlans() {
		List<AttributeDesc> attributeNames = new ArrayList<>();
		attributeNames.add(AttributeDesc.builder().name("Plan Name").build());
		
		PlanAppendix appendix = new PlanAppendix();

		Map<String, PlanAppendix> planOfferings = new HashMap<>();
		planOfferings.put("plan", appendix);

		PlanOfferingsData data = new PlanOfferingsData();
		data.setPlanOfferings(planOfferings);

		PlanOfferingsReportDetails details = new PlanOfferingsReportDetails();
		details.setData(data);

		return details;
	}

}
