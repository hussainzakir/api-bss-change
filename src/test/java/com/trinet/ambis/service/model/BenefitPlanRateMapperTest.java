package com.trinet.ambis.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BenefitPlanRateMapperTest {

    @Test
    public void testToPlanRate() {
        BenefitPlanRate planRate1 = prepareBenPlanRate(
                "BANDCODE1",
                "MEDPLAN1",
                "medical",
                "1",
                BigDecimal.valueOf(200)
        );
        BenefitPlanRate planRate2 = prepareBenPlanRate(
                "BANDCODE1",
                "MEDPLAN1",
                "medical",
                "2",
                BigDecimal.valueOf(300)
        );
        BenefitPlanRate planRate3 = prepareBenPlanRate(
                "BANDCODE1",
                "MEDPLAN1",
                "medical",
                "C",
                BigDecimal.valueOf(400)
        );
        BenefitPlanRate planRate4 = prepareBenPlanRate(
                "BANDCODE1",
                "MEDPLAN1",
                "medical",
                "4",
                BigDecimal.valueOf(500)
        );

        PlanRate expectedPlanRate = PlanRate.builder()
                .planType("medical")
                .planId("MEDPLAN1")
                .rateDetails(
                        PlanRate.RateDetails.builder()
                                .rateType("tiered")
                                .rates(List.of(
                                        PlanRate.RateDetails.Rate.builder()
                                                .tierCode("employee").retailRate(200.0).build(),
                                        PlanRate.RateDetails.Rate.builder()
                                                .tierCode("employeePlusSpouse").retailRate(300.0).build(),
                                        PlanRate.RateDetails.Rate.builder()
                                                .tierCode("employeePlusChild").retailRate(400.0).build(),
                                        PlanRate.RateDetails.Rate.builder()
                                                .tierCode("employeePlusFamily").retailRate(500.0).build()
                                ))
                                .build()
                )
                .build();

        PlanRate planRate = BenefitPlanRateMapper.toPlanRate(Arrays.asList(
                planRate1, planRate2, planRate3, planRate4
        ));

        // Assertions
        assertEquals(expectedPlanRate, planRate);
    }

    @Test
    public void testToPlanRateNullOrEmptyException() {
        try {
            BenefitPlanRateMapper.toPlanRate(null);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("BenefitPlanRate list cannot be null or empty.", e.getMessage());
        }
        try {
            BenefitPlanRateMapper.toPlanRate(Collections.emptyList());
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("BenefitPlanRate list cannot be null or empty.", e.getMessage());
        }
    }

    @Test
    public void testToPlanRateNullPlanIdException() {
        try {
            BenefitPlanRateMapper.toPlanRate(Arrays.asList(
                    prepareBenPlanRate(
                            "BANDCODE1",
                            null,
                            "medical",
                            "1",
                            BigDecimal.valueOf(200)
                    )
            ));
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("BenefitPlan ID cannot be null or blank.", e.getMessage());
        }
    }

    @Test
    public void testToPlanRateInconsistentPlanIdsException() {
        BenefitPlanRate planRate1 = prepareBenPlanRate(
                "BANDCODE1",
                "MEDPLAN1",
                "medical",
                "1",
                BigDecimal.valueOf(200)
        );
        BenefitPlanRate planRate2 = prepareBenPlanRate(
                "BANDCODE1",
                "MEDPLAN2",
                "medical",
                "2",
                BigDecimal.valueOf(300)
        );

        try {
            BenefitPlanRateMapper.toPlanRate(Arrays.asList(planRate1, planRate2));
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("All BenefitPlanRate entries must have the same benefitPlan ID.", e.getMessage());
        }
    }

    @Test
    public void testToPlanRateNullEmployerCostException() {
        BenefitPlanRate planRate = prepareBenPlanRate(
                "BANDCODE1",
                "MEDPLAN1",
                "medical",
                "1",
                null
        );;

        try {
            BenefitPlanRateMapper.toPlanRate(Arrays.asList(planRate));
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Employer cost cannot be null", e.getMessage());
        }
    }

    private BenefitPlanRate prepareBenPlanRate(
            String bandCode, String benPlan, String planType, String coverageCode, BigDecimal cost) {
        BenefitPlanRate planRate = new BenefitPlanRate();
        planRate.setBandCode(bandCode);
        planRate.setBenefitPlan(benPlan);
        planRate.setCostId(BigDecimal.valueOf(1));
        planRate.setCoverageCode(coverageCode);
        planRate.setEmployerCost(cost);
        planRate.setOptionId(BigDecimal.valueOf(1));
        java.util.Date effDt = new Date();
        planRate.setEffDt(effDt);
        planRate.setPlanType(planType);
        return planRate;
    }
}