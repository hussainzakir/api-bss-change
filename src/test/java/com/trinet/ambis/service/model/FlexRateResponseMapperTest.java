package com.trinet.ambis.service.model;

import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.service.model.FlexRateResponse.PlanByBenefitType;
import com.trinet.ambis.service.model.FlexRateResponse.PlansByPlanType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FlexRateResponseMapperTest {

    @Test
    public void testToBenefitPlanRates() {

        List<BenefitPlanRate> result = FlexRateResponseMapper.toBenefitPlanRates(prepareFlexRateResponse(), "123");

        assertEquals(12, result.size());

        BenefitPlanRate bpr1 = result.get(0);
        assertEquals("00NPQ6", bpr1.getBenefitPlan());
        assertEquals("10", bpr1.getPlanType());
        assertEquals("1", bpr1.getCoverageCode());
        assertEquals(0, bpr1.getEmployerCost().compareTo(BigDecimal.valueOf(150.0)));
		assertEquals("RG12345", bpr1.getRateGroupId());

        BenefitPlanRate bpr2 = result.get(1);
        assertEquals("00NPQ6", bpr2.getBenefitPlan());
        assertEquals("10", bpr2.getPlanType());
        assertEquals("2", bpr2.getCoverageCode());
        assertEquals(0, bpr2.getEmployerCost().compareTo(BigDecimal.valueOf(300.0)));
		assertEquals("RG12345", bpr2.getRateGroupId());

        BenefitPlanRate bpr3 = result.get(2);
        assertEquals("00NPQ6", bpr3.getBenefitPlan());
        assertEquals("10", bpr3.getPlanType());
        assertEquals("C", bpr3.getCoverageCode());
        assertEquals(0, bpr3.getEmployerCost().compareTo(BigDecimal.valueOf(400.0)));
		assertEquals("RG12345", bpr3.getRateGroupId());

        BenefitPlanRate bpr4 = result.get(3);
        assertEquals("00NPQ6", bpr4.getBenefitPlan());
        assertEquals("10", bpr4.getPlanType());
        assertEquals("4", bpr4.getCoverageCode());
        assertEquals(0, bpr4.getEmployerCost().compareTo(BigDecimal.valueOf(500.0)));
		assertEquals("RG12345", bpr4.getRateGroupId());

        BenefitPlanRate bpr5 = result.get(4);
        assertEquals("00NPQ9", bpr5.getBenefitPlan());
        assertEquals("11", bpr5.getPlanType());
        assertEquals("1", bpr5.getCoverageCode());
        assertEquals(0, bpr5.getEmployerCost().compareTo(BigDecimal.valueOf(50.0)));
		assertEquals("RG12345", bpr5.getRateGroupId());

        BenefitPlanRate bpr6 = result.get(5);
        assertEquals("00NPQ9", bpr6.getBenefitPlan());
        assertEquals("11", bpr6.getPlanType());
        assertEquals("2", bpr6.getCoverageCode());
        assertEquals(0, bpr6.getEmployerCost().compareTo(BigDecimal.valueOf(100.0)));
		assertEquals("RG12345", bpr6.getRateGroupId());

        BenefitPlanRate bpr7 = result.get(8);
        assertEquals("00NPQ1", bpr7.getBenefitPlan());
        assertEquals("1D", bpr7.getPlanType());
        assertEquals("1", bpr7.getCoverageCode());
        assertEquals(0, bpr7.getEmployerCost().compareTo(BigDecimal.valueOf(60.0)));
		assertEquals("RG12345", bpr7.getRateGroupId());
    }

	@Test
	public void testToBenefitPlanRates_PlanTypeFallbackToParent() {
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId("RG12345");

		// planRate has planType = null, but parent PlansByPlanType has planType.
		PlanRate planRate = PlanRate.builder()
				.planType(null)
				.regionalPlanId("00NPQ6")
				.rateDetails(
						PlanRate.RateDetails.builder()
								.rateType("tiered")
								.rates(List.of(
										buildBenPlanRate("1", BigDecimal.valueOf(150.0)),
										buildBenPlanRate("2", BigDecimal.valueOf(150.0)),
										buildBenPlanRate("C", BigDecimal.valueOf(150.0)),
										buildBenPlanRate("4", BigDecimal.valueOf(150.0))
										))
								.build()
				)
				.build();

		PlansByPlanType plansByPlanType = new PlansByPlanType();
		plansByPlanType.setPlanType("10");
		plansByPlanType.setPlans(List.of(planRate));

		PlanByBenefitType planByBenefitType = new PlanByBenefitType();
		planByBenefitType.setBenefitType("medical");
		planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));

		response.setPlansByBenefitType(List.of(planByBenefitType));

		List<BenefitPlanRate> result = FlexRateResponseMapper.toBenefitPlanRates(response, "123");
		assertEquals(4, result.size());
		assertEquals("10", result.get(0).getPlanType());
	}

    @Test
    public void testToBenefitPlanRates_NullResponse() {
        List<BenefitPlanRate> result = FlexRateResponseMapper.toBenefitPlanRates(null, "123");
        assertEquals(0, result.size());
    }

    @Test
    public void testToBenefitPlanRates_EmptyPlans() {
        FlexRateResponse response = new FlexRateResponse();
        response.setPlansByBenefitType(null);

        List<BenefitPlanRate> result = FlexRateResponseMapper.toBenefitPlanRates(response, "123");
        assertEquals(0, result.size());
    }

	@Test
	public void testToBenefitPlanRatesByPlanId() {
		FlexRateResponse response = prepareFlexRateResponse();
		Map<String, List<BenefitPlanRate>> result = FlexRateResponseMapper.toBenefitPlanRatesByPlanId(response, "123");

		assertEquals(3, result.size());
		assertEquals(4, result.get("00NPQ6").size());
		assertEquals(4, result.get("00NPQ9").size());
		assertEquals(4, result.get("00NPQ1").size());

		BenefitPlanRate bpr = result.get("00NPQ1").get(0);
		assertEquals("1D", bpr.getPlanType());
		assertEquals("1", bpr.getCoverageCode());
		assertEquals(0, bpr.getEmployerCost().compareTo(BigDecimal.valueOf(60.0)));
		assertEquals("RG12345", bpr.getRateGroupId());
	}

	@Test
	public void testToBenefitPlanRatesByPlanId_NullResponse() {
		Map<String, List<BenefitPlanRate>> result = FlexRateResponseMapper.toBenefitPlanRatesByPlanId(null, "123");
		assertEquals(0, result.size());
	}

	@Test
	public void testToBenefitPlanRatesByPlanId_EmptyPlans() {
		FlexRateResponse response = new FlexRateResponse();
		response.setPlansByBenefitType(null);
		
		Map<String, List<BenefitPlanRate>> result = FlexRateResponseMapper.toBenefitPlanRatesByPlanId(response, "123");
		assertEquals(0, result.size());
	}

    @Test
    public void testPlanRateToRates_NULL_TIER_raisesException() {
		FlexRateResponse response = prepareFlexRateResponse_nullTier();
		BSSBadDataException ex = assertThrows(BSSBadDataException.class, () ->
			FlexRateResponseMapper.toBenefitPlanRatesByPlanId(response, "123")
		);
		assertTrue(ex.getMessage().contains("Null retailRate"));
		assertTrue(ex.getMessage().contains("00NPQ6"));
		assertTrue(ex.getMessage().contains("companyCode=123"));
    }

	@Test
	public void testPlanRateToRates_MISSING_TIER_raisesException() {
		FlexRateResponse response = prepareFlexRateResponse_missingTier();
		BSSBadDataException ex = assertThrows(BSSBadDataException.class, () ->
			FlexRateResponseMapper.toBenefitPlanRatesByPlanId(response, "123")
		);
		assertTrue(ex.getMessage().contains("Missing retailRate"));
		assertTrue(ex.getMessage().contains("00NPQ6"));
		assertTrue(ex.getMessage().contains("companyCode=123"));
	}

    private FlexRateResponse prepareFlexRateResponse() {
        FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId("RG12345");

        // Prepare PlanRate for medical
        PlanRate planRate10 = PlanRate.builder()
                .planType("10")
                .dpPlanType("15")
                .planId("8acba25e99dbf5760199e3ca33e20082")
                .regionalPlanId("00NPQ6")
                .dpRegionalPlanId("00NPQ7")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        buildBenPlanRate("1", BigDecimal.valueOf(150.0)),
                                        buildBenPlanRate("2", BigDecimal.valueOf(300.0)),
                                        buildBenPlanRate("C", BigDecimal.valueOf(400.0)),
                                        buildBenPlanRate("4", BigDecimal.valueOf(500.0))))
                                .build()
                ).build();

        PlansByPlanType plansByPlanTypeMedical = new PlansByPlanType();
        plansByPlanTypeMedical.setPlanType("10");
        plansByPlanTypeMedical.setPlans(List.of(planRate10));

        PlanByBenefitType planByBenefitTypeMedical = new PlanByBenefitType();
        planByBenefitTypeMedical.setBenefitType("medical");
        planByBenefitTypeMedical.setPlansByPlanType(List.of(plansByPlanTypeMedical));

        // Prepare PlanRate for dental
        PlanRate planRate11 = PlanRate.builder()
                .planType("11")
                .dpPlanType("16")
                .planId("9acba25e99dbf5760199e3ca33e20083")
                .regionalPlanId("00NPQ9")
                .dpRegionalPlanId("00NPQ9")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        buildBenPlanRate("1", BigDecimal.valueOf(50.0)),
                                        buildBenPlanRate("2", BigDecimal.valueOf(100.0)),
                                        buildBenPlanRate("C", BigDecimal.valueOf(50.0)),
                                        buildBenPlanRate("4", BigDecimal.valueOf(100.0))))
                                .build()
                ).build();
        PlanRate planRate1D = PlanRate.builder()
                .planType("1D")
                .dpPlanType("1E")
                .planId("1acba25e99dbf5760199e3ca33e20083")
                .regionalPlanId("00NPQ1")
                .dpRegionalPlanId("00NPQ1")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
								.rates(List.of(
										buildBenPlanRate("1", BigDecimal.valueOf(60.0)),
										buildBenPlanRate("2", BigDecimal.valueOf(100.0)),
										buildBenPlanRate("C", BigDecimal.valueOf(50.0)),
										buildBenPlanRate("4", BigDecimal.valueOf(100.0))))
                                .build()
                ).build();

        PlansByPlanType plansByPlanTypeDental11 = new PlansByPlanType();
        plansByPlanTypeDental11.setPlanType("11");
        plansByPlanTypeDental11.setPlans(List.of(planRate11));

        PlansByPlanType plansByPlanTypeDental1D = new PlansByPlanType();
        plansByPlanTypeDental1D.setPlanType("1D");
        plansByPlanTypeDental1D.setPlans(List.of(planRate1D));

        PlanByBenefitType planByBenefitTypeDental = new PlanByBenefitType();
        planByBenefitTypeDental.setBenefitType("dental");
        planByBenefitTypeDental.setPlansByPlanType(List.of(plansByPlanTypeDental11, plansByPlanTypeDental1D));

        response.setPlansByBenefitType(List.of(planByBenefitTypeMedical, planByBenefitTypeDental));

        return response;
    }

	private FlexRateResponse prepareFlexRateResponse_nullTier() {
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId("RG12345");

		// Prepare PlanRate for medical
		PlanRate planRate10 = PlanRate.builder()
				.planType("10")
				.dpPlanType("15")
				.planId("8acba25e99dbf5760199e3ca33e20082")
				.regionalPlanId("00NPQ6")
				.dpRegionalPlanId("00NPQ7")
				.rateDetails(
						PlanRate.RateDetails.builder()
							.rateType("tiered")
							.rates(List.of(
								buildBenPlanRateNull("1"),
								buildBenPlanRate("2", BigDecimal.ZERO),
								buildBenPlanRate("C", BigDecimal.ZERO),
								buildBenPlanRate("4", BigDecimal.ZERO)))
							.build()
				)
				.build();

		PlansByPlanType plansByPlanTypeMedical = new PlansByPlanType();
		plansByPlanTypeMedical.setPlanType("10");
		plansByPlanTypeMedical.setPlans(List.of(planRate10));

		PlanByBenefitType planByBenefitTypeMedical = new PlanByBenefitType();
		planByBenefitTypeMedical.setBenefitType("medical");
		planByBenefitTypeMedical.setPlansByPlanType(List.of(plansByPlanTypeMedical));

		response.setPlansByBenefitType(List.of(planByBenefitTypeMedical));

		return response;
	}

	private FlexRateResponse prepareFlexRateResponse_missingTier() {
		FlexRateResponse response = new FlexRateResponse();
		response.setRateGroupId("RG12345");

		// Prepare PlanRate for medical
		PlanRate planRate10 = PlanRate.builder()
				.planType("10")
				.dpPlanType("15")
				.planId("8acba25e99dbf5760199e3ca33e20082")
				.regionalPlanId("00NPQ6")
				.dpRegionalPlanId("00NPQ7")
				.rateDetails(
						PlanRate.RateDetails.builder()
								.rateType("tiered")
								.rates(List.of(
										buildBenPlanRate("1", BigDecimal.valueOf(150.0)),
										// Missing tier "2"
										buildBenPlanRate("C", BigDecimal.valueOf(1200.0)),
										buildBenPlanRate("4", BigDecimal.valueOf(40))))
								.build()
				).build();

		PlansByPlanType plansByPlanTypeMedical = new PlansByPlanType();
		plansByPlanTypeMedical.setPlanType("10");
		plansByPlanTypeMedical.setPlans(List.of(planRate10));

		PlanByBenefitType planByBenefitTypeMedical = new PlanByBenefitType();
		planByBenefitTypeMedical.setBenefitType("medical");
		planByBenefitTypeMedical.setPlansByPlanType(List.of(plansByPlanTypeMedical));

		response.setPlansByBenefitType(List.of(planByBenefitTypeMedical));

		return response;
	}

    private PlanRate.RateDetails.Rate buildBenPlanRate(
            String tierCode,
            BigDecimal rate
    ) {
        return PlanRate.RateDetails.Rate.builder()
                .tierCode(tierCode)
                .retailRate(rate.doubleValue())
                .build();
    }

	private PlanRate.RateDetails.Rate buildBenPlanRateNull(String tierCode) {
		return PlanRate.RateDetails.Rate.builder()
				.tierCode(tierCode)
				.retailRate(null)
				.build();
	}

}
