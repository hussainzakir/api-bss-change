package com.trinet.ambis.service.model;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for mapping BenefitPlanRate entries to PlanRate objects.
 */
public final class BenefitPlanRateMapper {

    private BenefitPlanRateMapper() {
        throw new IllegalStateException(
                "Utility class " + BenefitPlanRateMapper.class.getName() + " can not be instantiated.");
    }

    /**
     * Converts a list of BenefitPlanRate entries into a single PlanRate.
     *
     * @param benefitPlanRates List of BenefitPlanRate entries
     * @return PlanRate object
     * @throws IllegalArgumentException if input is null, empty, or contains inconsistent plan IDs
     */
    public static PlanRate toPlanRate(List<BenefitPlanRate> benefitPlanRates) {
        validateBenefitPlanRates(benefitPlanRates);

        List<PlanRate.RateDetails.Rate> rates = benefitPlanRates.stream()
                .map(BenefitPlanRateMapper::mapToRate)
                .collect(Collectors.toUnmodifiableList());

        PlanRate.RateDetails rateDetails = PlanRate.RateDetails.builder()
                .rateType(RateTypeEnum.TIERED.getCode())
                .rates(rates)
                .build();

        BenefitPlanRate firstRate = benefitPlanRates.get(0);
        return PlanRate.builder()
                .planType(firstRate.getPlanType())
                .dpPlanType(null)
                .planId(firstRate.getBenefitPlan())
                .regionalPlanId(null)
                .dpRegionalPlanId(null)
                .planId(firstRate.getBenefitPlan())
                .rateDetails(rateDetails)
                .build();
    }

    /**
     * Maps a single BenefitPlanRate to a PlanRate.RateDetails.Rate.
     *
     * @param rate BenefitPlanRate entry
     * @return Converted Rate object
     */
    private static PlanRate.RateDetails.Rate mapToRate(BenefitPlanRate rate) {
        String tierCode = CoverageCodesEnums.valueOfId(rate.getCoverageCode());
        double employerRate = Optional.ofNullable(rate.getEmployerCost())
                .map(BigDecimal::doubleValue)
                .orElseThrow(() -> new IllegalArgumentException("Employer cost cannot be null"));

        return PlanRate.RateDetails.Rate.builder()
                .tierCode(tierCode)
                .retailRate(employerRate)
                .build();
    }

    /**
     * Validates that the list is not null, not empty, and all entries share the same benefitPlan ID.
     *
     * @param rates List of BenefitPlanRate entries
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateBenefitPlanRates(List<BenefitPlanRate> rates) {
        if (rates == null || rates.isEmpty()) {
            throw new IllegalArgumentException("BenefitPlanRate list cannot be null or empty.");
        }

        String expectedPlanId = rates.get(0).getBenefitPlan();
        if (expectedPlanId == null || expectedPlanId.isBlank()) {
            throw new IllegalArgumentException("BenefitPlan ID cannot be null or blank.");
        }

        boolean allMatch = rates.stream()
                .allMatch(rate -> expectedPlanId.equals(rate.getBenefitPlan()));

        if (!allMatch) {
            throw new IllegalArgumentException("All BenefitPlanRate entries must have the same benefitPlan ID.");
        }
    }
}


