package com.trinet.ambis.service.model;

import com.trinet.ambis.exception.BSSBadDataException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for FlexRateResponseMapper pay-in rate methods
 */
@RunWith(MockitoJUnitRunner.class)
public class FlexRateMapperTest {

    @Test
    public void testGetPayInRatesByBenefitPlanId_WithValidResponse() {
        FlexRateResponse response = buildFlexRateResponse();
        
        Map<String, List<PayInRateInfo>> result = FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
        
        // Verify we have entries for both regional and DP regional plans
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("00NPQ6")); // regionalPlanId
        assertTrue(result.containsKey("00NPQ7")); // dpRegionalPlanId
        
        // Verify regional plan rates (tiers 1, 2, C, 4)
        List<PayInRateInfo> regionalRates = result.get("00NPQ6");
        assertNotNull(regionalRates);
        assertEquals(4, regionalRates.size());
        
        // Check tier 1
        PayInRateInfo tier1 = findRateByTier(regionalRates, "1");
        assertNotNull(tier1);
        assertEquals("10", tier1.getPlanType());
        assertEquals("1", tier1.getCoverageLevel());
        assertEquals(50.0, tier1.getPayInRate(), 0.01);
        
        // Check tier 2
        PayInRateInfo tier2 = findRateByTier(regionalRates, "2");
        assertNotNull(tier2);
        assertEquals("10", tier2.getPlanType());
        assertEquals("2", tier2.getCoverageLevel());
        assertEquals(100.0, tier2.getPayInRate(), 0.01);
        
        // Check tier C
        PayInRateInfo tierC = findRateByTier(regionalRates, "C");
        assertNotNull(tierC);
        assertEquals("10", tierC.getPlanType());
        assertEquals("C", tierC.getCoverageLevel());
        assertEquals(125.0, tierC.getPayInRate(), 0.01);
        
        // Check tier 4
        PayInRateInfo tier4 = findRateByTier(regionalRates, "4");
        assertNotNull(tier4);
        assertEquals("10", tier4.getPlanType());
        assertEquals("4", tier4.getCoverageLevel());
        assertEquals(150.0, tier4.getPayInRate(), 0.01);
        
        // Verify DP regional plan rates (tiers 5, 6, 7, 8)
        List<PayInRateInfo> dpRates = result.get("00NPQ7");
        assertNotNull(dpRates);
        assertEquals(4, dpRates.size());
        
        // Check tier 5
        PayInRateInfo tier5 = findRateByTier(dpRates, "5");
        assertNotNull(tier5);
        assertEquals("15", tier5.getPlanType());
        assertEquals("5", tier5.getCoverageLevel());
        assertEquals(60.0, tier5.getPayInRate(), 0.01);
        
        // Check tier 6
        PayInRateInfo tier6 = findRateByTier(dpRates, "6");
        assertNotNull(tier6);
        assertEquals("15", tier6.getPlanType());
        assertEquals("6", tier6.getCoverageLevel());
        assertEquals(120.0, tier6.getPayInRate(), 0.01);
        
        // Check tier 7
        PayInRateInfo tier7 = findRateByTier(dpRates, "7");
        assertNotNull(tier7);
        assertEquals("15", tier7.getPlanType());
        assertEquals("7", tier7.getCoverageLevel());
        assertEquals(140.0, tier7.getPayInRate(), 0.01);
        
        // Check tier 8
        PayInRateInfo tier8 = findRateByTier(dpRates, "8");
        assertNotNull(tier8);
        assertEquals("15", tier8.getPlanType());
        assertEquals("8", tier8.getCoverageLevel());
        assertEquals(180.0, tier8.getPayInRate(), 0.01);
    }

    @Test
    public void testGetPayInRatesByBenefitPlanId_WithMultiplePlans() {
        FlexRateResponse response = buildFlexRateResponseWithMultiplePlans();
        
        Map<String, List<PayInRateInfo>> result = 
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
        
        // Should return rates for all plans
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("00NPQ6"));
        assertTrue(result.containsKey("00NPQ7"));
        assertTrue(result.containsKey("00NPQ9"));
    }

    @Test
    public void testGetPayInRatesByBenefitPlanId_NullResponse() {
        Map<String, List<PayInRateInfo>> result = 
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(null, "123");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetPayInRatesByBenefitPlanId_EmptyPlansByBenefitType() {
        FlexRateResponse response = new FlexRateResponse();
        response.setPlansByBenefitType(null);
        
        Map<String, List<PayInRateInfo>> result = 
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = BSSBadDataException.class)
    public void testGetPayInRatesByBenefitPlanId_MissingRateDetails() {
        FlexRateResponse response = new FlexRateResponse();
        
        PlanRate planRate = PlanRate.builder()
                .regionalPlanId("00NPQ6")
                .rateDetails(null) // Missing rate details
                .build();
        
        FlexRateResponse.PlansByPlanType plansByPlanType = new FlexRateResponse.PlansByPlanType();
        plansByPlanType.setPlanType("10");
        plansByPlanType.setPlans(List.of(planRate));
        
        FlexRateResponse.PlanByBenefitType planByBenefitType = new FlexRateResponse.PlanByBenefitType();
        planByBenefitType.setBenefitType("medical");
        planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));
        
        response.setPlansByBenefitType(List.of(planByBenefitType));
        
        FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
    }

    @Test
    public void testGetPayInRatesByBenefitPlanId_OnlyRegionalPlan() {
        FlexRateResponse response = new FlexRateResponse();
        
        // Create plan with only regional plan ID (no DP)
        PlanRate planRate = PlanRate.builder()
                .planId("PLAN123")
                .regionalPlanId("00NPQ6")
                .dpRegionalPlanId(null) // No DP regional plan
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        buildRate("1", 150.0, 50.0),
                                        buildRate("2", 300.0, 100.0),
                                        buildRate("C", 400.0, 125.0),
                                        buildRate("4", 500.0, 150.0)
                                ))
                                .build()
                )
                .build();
        
        FlexRateResponse.PlansByPlanType plansByPlanType = new FlexRateResponse.PlansByPlanType();
        plansByPlanType.setPlanType("10");
        plansByPlanType.setPlans(List.of(planRate));
        
        FlexRateResponse.PlanByBenefitType planByBenefitType = new FlexRateResponse.PlanByBenefitType();
        planByBenefitType.setBenefitType("medical");
        planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));
        
        response.setPlansByBenefitType(List.of(planByBenefitType));
        
        Map<String, List<PayInRateInfo>> result = 
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
        
        // Should only have regional plan, not DP
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("00NPQ6"));
        assertFalse(result.containsKey("00NPQ7"));
        assertEquals(4, result.get("00NPQ6").size());
    }

    @Test
    public void testGetPayInRatesByBenefitPlanId_OnlyDPRegionalPlan() {
        FlexRateResponse response = new FlexRateResponse();
        
        // Create plan with only DP regional plan ID
        PlanRate planRate = PlanRate.builder()
                .planId("PLAN123")
                .regionalPlanId(null) // No regular regional plan
                .dpRegionalPlanId("00NPQ7")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        buildRate("5", 180.0, 60.0),
                                        buildRate("6", 360.0, 120.0),
                                        buildRate("7", 420.0, 140.0),
                                        buildRate("8", 540.0, 180.0)
                                ))
                                .build()
                )
                .build();
        
        FlexRateResponse.PlansByPlanType plansByPlanType = new FlexRateResponse.PlansByPlanType();
        plansByPlanType.setDpPlanType("15");
        plansByPlanType.setPlans(List.of(planRate));
        
        FlexRateResponse.PlanByBenefitType planByBenefitType = new FlexRateResponse.PlanByBenefitType();
        planByBenefitType.setBenefitType("medical");
        planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));
        
        response.setPlansByBenefitType(List.of(planByBenefitType));
        
        Map<String, List<PayInRateInfo>> result = 
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
        
        // Should only have DP plan, not regular regional
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("00NPQ7"));
        assertFalse(result.containsKey("00NPQ6"));
        assertEquals(4, result.get("00NPQ7").size());
    }

    @Test
    public void testGetPayInRatesByBenefitPlanId_AllTiersPresent() {
        FlexRateResponse response = buildFlexRateResponse();
        
        Map<String, List<PayInRateInfo>> result = 
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
        
        // Verify all required tiers are present for regional plan
        List<PayInRateInfo> regionalRates = result.get("00NPQ6");
        assertNotNull(findRateByTier(regionalRates, "1"));
        assertNotNull(findRateByTier(regionalRates, "2"));
        assertNotNull(findRateByTier(regionalRates, "C"));
        assertNotNull(findRateByTier(regionalRates, "4"));
        
        // Verify all required tiers are present for DP plan
        List<PayInRateInfo> dpRates = result.get("00NPQ7");
        assertNotNull(findRateByTier(dpRates, "5"));
        assertNotNull(findRateByTier(dpRates, "6"));
        assertNotNull(findRateByTier(dpRates, "7"));
        assertNotNull(findRateByTier(dpRates, "8"));
    }

    @Test
    public void testGetPayInRatesByBenefitPlanId_NonNullRates() {
        FlexRateResponse response = buildFlexRateResponse();
        
        Map<String, List<PayInRateInfo>> result = 
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123");
        
        // Verify all rates are non-null and have valid values
        for (List<PayInRateInfo> rateList : result.values()) {
            for (PayInRateInfo rate : rateList) {
                assertNotNull(rate);
                assertNotNull(rate.getPlanType());
                assertNotNull(rate.getCoverageLevel());
                assertTrue(rate.getPayInRate() >= 0);
            }
        }
    }

    @Test
    public void testExtractRates_MISSING_TIER_raisesException() {
        FlexRateResponse response = buildFlexRateResponse_missingTier();
        assertThrows(BSSBadDataException.class, () ->
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123")
        );
    }

    @Test
    public void testExtractRates_NULL_TIER_raisesException() {
        FlexRateResponse response = buildFlexRateResponse_nullTier();
        assertThrows(BSSBadDataException.class, () ->
            FlexRateResponseMapper.getPayInRatesByBenefitPlanId(response, "123")
        );
    }

    // Helper methods

    private PayInRateInfo findRateByTier(List<PayInRateInfo> rates, String tier) {
        return rates.stream()
                .filter(r -> tier.equals(r.getCoverageLevel()))
                .findFirst()
                .orElse(null);
    }

    private PlanRate.RateDetails.Rate buildRate(String tierCode, Double retailRate, Double payInRate) {
        return PlanRate.RateDetails.Rate.builder()
                .tierCode(tierCode)
                .retailRate(retailRate)
                .payInRate(payInRate)
                .build();
    }

    private FlexRateResponse buildFlexRateResponse() {
        FlexRateResponse response = new FlexRateResponse();
        response.setRateGroupId("RG12345");

        // Create plan with both regional and DP regional rates
        PlanRate planRate = PlanRate.builder()
                .planId("8acba25e99dbf5760199e3ca33e20082")
                .regionalPlanId("00NPQ6")
                .dpRegionalPlanId("00NPQ7")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        // Regional tiers (1, 2, C, 4)
                                        buildRate("1", 150.0, 50.0),
                                        buildRate("2", 300.0, 100.0),
                                        buildRate("C", 400.0, 125.0),
                                        buildRate("4", 500.0, 150.0),
                                        // DP tiers (5, 6, 7, 8)
                                        buildRate("5", 180.0, 60.0),
                                        buildRate("6", 360.0, 120.0),
                                        buildRate("7", 420.0, 140.0),
                                        buildRate("8", 540.0, 180.0)
                                ))
                                .build()
                )
                .build();

        FlexRateResponse.PlansByPlanType plansByPlanType = new FlexRateResponse.PlansByPlanType();
        plansByPlanType.setPlanType("10");
        plansByPlanType.setDpPlanType("15");
        plansByPlanType.setPlans(List.of(planRate));

        FlexRateResponse.PlanByBenefitType planByBenefitType = new FlexRateResponse.PlanByBenefitType();
        planByBenefitType.setBenefitType("medical");
        planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));

        response.setPlansByBenefitType(List.of(planByBenefitType));

        return response;
    }

    private FlexRateResponse buildFlexRateResponseWithMultiplePlans() {
        FlexRateResponse response = new FlexRateResponse();
        response.setRateGroupId("RG12345");

        List<PlanRate> plans = new ArrayList<>();

        // First plan
        plans.add(PlanRate.builder()
                .planId("8acba25e99dbf5760199e3ca33e20082")
                .regionalPlanId("00NPQ6")
                .dpRegionalPlanId("00NPQ7")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        buildRate("1", 150.0, 50.0),
                                        buildRate("2", 300.0, 100.0),
                                        buildRate("C", 400.0, 125.0),
                                        buildRate("4", 500.0, 150.0),
                                        buildRate("5", 180.0, 60.0),
                                        buildRate("6", 360.0, 120.0),
                                        buildRate("7", 420.0, 140.0),
                                        buildRate("8", 540.0, 180.0)
                                ))
                                .build()
                )
                .build());

        // Second plan
        plans.add(PlanRate.builder()
                .planId("ANOTHER_PLAN_ID")
                .regionalPlanId("00NPQ9")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        buildRate("1", 200.0, 75.0),
                                        buildRate("2", 350.0, 125.0),
                                        buildRate("C", 400.0, 125.0),
                                        buildRate("4", 500.0, 150.0),
                                        buildRate("5", 180.0, 60.0),
                                        buildRate("6", 360.0, 120.0),
                                        buildRate("7", 420.0, 140.0),
                                        buildRate("8", 540.0, 180.0)
                                ))
                                .build()
                )
                .build());

        FlexRateResponse.PlansByPlanType plansByPlanType = new FlexRateResponse.PlansByPlanType();
        plansByPlanType.setPlanType("10");
        plansByPlanType.setDpPlanType("15");
        plansByPlanType.setPlans(plans);

        FlexRateResponse.PlanByBenefitType planByBenefitType = new FlexRateResponse.PlanByBenefitType();
        planByBenefitType.setBenefitType("medical");
        planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));

        response.setPlansByBenefitType(List.of(planByBenefitType));

        return response;
    }

    private FlexRateResponse buildFlexRateResponse_missingTier() {
        FlexRateResponse response = new FlexRateResponse();
        response.setRateGroupId("RG12345");

        // Create plan with only regional plan (no DP)
        PlanRate planRate = PlanRate.builder()
                .planId("8acba25e99dbf5760199e3ca33e20082")
                .regionalPlanId("00NPQ6")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        // Regional tiers (1, 2, C, 4)
                                        buildRate("1", 150.0, 50.0),
                                        // Missing tier 2
                                        buildRate("C", 400.0, 125.0),
                                        buildRate("4", 500.0, 150.0)
                                ))
                                .build()
                )
                .build();

        FlexRateResponse.PlansByPlanType plansByPlanType = new FlexRateResponse.PlansByPlanType();
        plansByPlanType.setPlanType("10");
        plansByPlanType.setPlans(List.of(planRate));

        FlexRateResponse.PlanByBenefitType planByBenefitType = new FlexRateResponse.PlanByBenefitType();
        planByBenefitType.setBenefitType("medical");
        planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));

        response.setPlansByBenefitType(List.of(planByBenefitType));

        return response;
    }

    private FlexRateResponse buildFlexRateResponse_nullTier() {
        FlexRateResponse response = new FlexRateResponse();
        response.setRateGroupId("RG12345");

        // Create plan with only regional plan (no DP)
        PlanRate planRate = PlanRate.builder()
                .planId("8acba25e99dbf5760199e3ca33e20082")
                .regionalPlanId("00NPQ6")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        // Regional tiers (1, 2, C, 4)
                                        buildRate("1", 150.0, 50.0),
                                        buildRate("2", 300.0, null), // Null payInRate
                                        buildRate("C", 400.0, 125.0),
                                        buildRate("4", 500.0, 150.0)
                                ))
                                .build()
                )
                .build();

        FlexRateResponse.PlansByPlanType plansByPlanType = new FlexRateResponse.PlansByPlanType();
        plansByPlanType.setPlanType("10");
        plansByPlanType.setPlans(List.of(planRate));

        FlexRateResponse.PlanByBenefitType planByBenefitType = new FlexRateResponse.PlanByBenefitType();
        planByBenefitType.setBenefitType("medical");
        planByBenefitType.setPlansByPlanType(List.of(plansByPlanType));

        response.setPlansByBenefitType(List.of(planByBenefitType));

        return response;
    }
}
