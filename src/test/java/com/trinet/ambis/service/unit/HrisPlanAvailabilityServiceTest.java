package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.service.impl.HrisPlanAvailabilityServiceImpl;
import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.util.HrisPlanServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class HrisPlanAvailabilityServiceTest extends ServiceUnitTest {

	@InjectMocks
	private HrisPlanAvailabilityServiceImpl hrisPlanAvailabilityService;

	@Mock
	private HrisPlanServiceRestClient hrisPlanServiceRestClient;

	private final String COMPANY_CODE = "1111";

	/**
	 * given HrisPlanRequest when getBenefitPlanAvailability method is called then
	 * return the CompletableFuture of list of HrisPlanResponse
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws ParseException
	 **/
	@Test
	public void getBenefitPlanAvailabilityTest() throws InterruptedException, ExecutionException, ParseException {
		// Given
		HrisPlanRequest request = HrisPlanRequest.builder().benefitsType("medical").hqState("CA").hqZipCode("94107")
				.effDate("2025-04-01").emplLocDetails(Arrays.asList(HrisPlanRequest.LocationDetails.builder()
						.homeState("MA").homeZipCodes(Arrays.asList("02108", "02110")).build()))
				.build();

		// Mock
		Mockito.when(hrisPlanServiceRestClient.getBenefitPlanAvailability(any(), any()))
				.thenReturn(populateHrisPlanResponseList());

		// When
		List<HrisPlanResponse> plans = hrisPlanAvailabilityService.getBenefitPlanAvailability(request,
				COMPANY_CODE + ":" + BSSApplicationConstants.MEDICAL);

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

	/**
	 * given HrisPlanRequest when getBenefitPlanAvailability method is called then
	 * return the CompletableFuture of list of HrisPlanResponse
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 **/
	@Test
	public void getBenefitPlanAvailabilityInvalidRequest() throws ExecutionException, InterruptedException {
		// Given
		HrisPlanRequest request = HrisPlanRequest.builder().build();

		// When
		List<HrisPlanResponse> plans = hrisPlanAvailabilityService.getBenefitPlanAvailability(request, COMPANY_CODE);

		// Then
		assertEquals(0, plans.size());
	}

	/**
	 * given HrisPlanRequest when getBenefitPlanAvailability method is called then
	 * return the CompletableFuture of list of HrisPlanResponse
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 **/
	@Test
	public void getBenefitPlanAvailabilityInvalidResponse() throws ExecutionException, InterruptedException {
		// Given
		HrisPlanRequest request = HrisPlanRequest.builder().benefitsType("medical").hqState("CA").build();

		// Mock
		Mockito.when(hrisPlanServiceRestClient.getBenefitPlanAvailability(any(), any())).thenReturn(null);

		// When
		List<HrisPlanResponse> plans = hrisPlanAvailabilityService.getBenefitPlanAvailability(request, COMPANY_CODE);

		// Then
		assertEquals(0, plans.size());

		// Verify
		verify(hrisPlanServiceRestClient, times(1)).getBenefitPlanAvailability(any(), any());
	}

	/**
	 * given HrisPlanRequest when getBenefitPlanAvailability method is called then
	 * return the CompletableFuture of list of HrisPlanResponse
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 **/
	@Test
	public void getBenefitPlanAvailabilityNotAllowedBenefitsType() throws ExecutionException, InterruptedException {
		// Given
		HrisPlanRequest request = HrisPlanRequest.builder().benefitsType("ltd").hqState("CA").build();

		// When
		List<HrisPlanResponse> plans = hrisPlanAvailabilityService.getBenefitPlanAvailability(request, COMPANY_CODE);

		// Then
		assertEquals(0, plans.size());

		// Verify
		verify(hrisPlanServiceRestClient, times(0)).getBenefitPlanAvailability(any(), any());
	}

	/**
	 * given HrisPlanRequest when getBenefitPlanAvailability method is called then
	 * return the CompletableFuture of list of HrisPlanResponse
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 **/
	@Test
	public void getBenefitPlanAvailabilityExceptionTest() throws ExecutionException, InterruptedException {
		// Given
		HrisPlanRequest request = HrisPlanRequest.builder().benefitsType("medical").hqState("CA").build();

		// When
		List<HrisPlanResponse> plans = hrisPlanAvailabilityService.getBenefitPlanAvailability(request, COMPANY_CODE);

		// Then
		assertEquals(0, plans.size());

		// Verify
		verify(hrisPlanServiceRestClient, times(1)).getBenefitPlanAvailability(any(), any());
	}

	private List<HrisPlanResponse> populateHrisPlanResponseList() {
		return List
				.of(HrisPlanResponse.builder().planId(12343).planName("Some medical plan 1").carrierId(1)
						.carrierName("Carrier A")
						.rateDetails(
								HrisPlanResponse.RateDetails.builder().rateType("4tier")
										.ratesByZip(Arrays.asList(HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(Arrays.asList("12434"))
												.rates(Arrays.asList(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("1").rate(234.43).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("2").rate(234.43).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("C").rate(234.43).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("4").rate(234.43).build()))
												.build()))
										.build())
						.build());
	}
}
