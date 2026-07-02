package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsReportDetails;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.impl.planofferings.PlanOfferingsRequestBuilderImpl;
import com.trinet.ambis.service.planofferings.PlanOfferingsReportDataService;
import com.trinet.ambis.test.config.TestHelper;

@RunWith(MockitoJUnitRunner.class)
public class PlanOfferingsRequestBuilderImplTest extends ServiceUnitTest {
	
	@InjectMocks
	private PlanOfferingsRequestBuilderImpl planOfferingsRequestBuilderImpl;
	
	@Mock
	private PlanOfferingsReportDataService planOfferingsReportDataService;
	
	@Mock
	private HttpServletRequest httpRequest;
	
	@Test
	public void prepareBssReportRequestTest() {
		PlanOfferingsReportDetails reportDetails = new PlanOfferingsReportDetails();
		reportDetails.setCmsType("cms-content");
		PlanOfferingsRequest planOfferingsRequest = new PlanOfferingsRequest();
		planOfferingsRequest.setBenefitTypes(List.of("med","den","vis"));
		planOfferingsRequest.setQuarter("Q1");
		PlanOfferingsData planOfferingsData = new PlanOfferingsData();
		planOfferingsData.setPlanOfferings(TestHelper.readPlanComparisonRequest(
				"/Outputs-templates/Templates/planofferings.json", new TypeReference<Map<String, PlanAppendix>>() {
				}).get());
		when(planOfferingsReportDataService.preparePlanOfferingsData(planOfferingsRequest, httpRequest))
				.thenReturn(planOfferingsData);
		PlanOfferingsReportDetails planOfferingsReportDetails = planOfferingsRequestBuilderImpl
				.buildPlanOfferingsReportRequest(planOfferingsRequest, httpRequest);

		//assert
		assertNotNull(planOfferingsReportDetails);
		assertNotNull(planOfferingsReportDetails.getData().getPlanOfferings());
		assertEquals("Aetna Dental Value Vol", planOfferingsReportDetails.getData().getPlanOfferings().get("11")
				.getPlanAttributes().get(0).getPlanName());
		assertEquals("VSP Vision Plus Vol", planOfferingsReportDetails.getData().getPlanOfferings().get("1V")
				.getPlanAttributes().get(0).getPlanName());
		assertEquals("AETNA PPO 5000 Midwest", planOfferingsReportDetails.getData().getPlanOfferings().get("10")
						.getPlanAttributes().get(0).getPlanName());

	}

}
