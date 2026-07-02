package com.trinet.ambis.service.model;

import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.service.model.FlexRateResponse.PlanByBenefitType;
import com.trinet.ambis.service.model.FlexRateResponse.PlansByPlanType;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FlexRateResponseMapper {

    private static final Logger logger = LoggerFactory.getLogger(FlexRateResponseMapper.class);

    // Coverage level codes for regular regional plans
    private static final Set<String> REGIONAL_TIERS = new HashSet<>(Arrays.asList(
        CoverageCodesEnums.COV_EMPLOYEE.getCode(),
        CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode(),
        CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode(),
        CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode()
    ));

    // Coverage level codes for DP (Domestic Partner) regional plans  
    private static final Set<String> DP_REGIONAL_TIERS = new HashSet<>(Arrays.asList(
        CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP.getCode(),
        CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP_CHILD.getCode(),
        CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP_ADULT_CHILD.getCode(),
        CoverageCodesEnums.COV_EMPLOYEE_PLUS_TWO_DP_ADULT.getCode()
    ));

    public static List<BenefitPlanRate> toBenefitPlanRates(FlexRateResponse response, String companyCode) {
        if (response == null || response.getPlansByBenefitType() == null) {
            return Collections.emptyList();
        }
        String rateGroupId = response.getRateGroupId();
        return response.getPlansByBenefitType().stream()
                .filter(Objects::nonNull)
                .flatMap(benefitType -> planByBenefitTypeToRates(benefitType, companyCode, rateGroupId))
                .collect(Collectors.toList());
    }

	public static Map<String, List<BenefitPlanRate>> toBenefitPlanRatesByPlanId(FlexRateResponse response, String companyCode) {
        return toBenefitPlanRates(response, companyCode).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(BenefitPlanRate::getBenefitPlan));
    }

    /**
     * Retrieves pay-in rates by benefit plan ID from a FlexRateResponse.
     *
     * @param flexRateResponse The FlexRateResponse containing plan rate data
     * @param companyCode The company code for logging and validation
     * @return Map where keys are regionalPlanId or dpRegionalPlanId, and values are lists of PayInRateInfo
     *         containing pay-in rates for each coverage level/tier
     */
    public static Map<String, List<PayInRateInfo>> getPayInRatesByBenefitPlanId(FlexRateResponse flexRateResponse, String companyCode) {
        if (flexRateResponse == null || flexRateResponse.getPlansByBenefitType() == null) {
            return Collections.emptyMap();
        }
        return flexRateResponse.getPlansByBenefitType().stream()
                .filter(Objects::nonNull)
                .filter(benefitType -> benefitType.getPlansByPlanType() != null)
                .flatMap(benefitType -> benefitType.getPlansByPlanType().stream()
                        .filter(Objects::nonNull)
                        .filter(plansByPlanType -> plansByPlanType.getPlans() != null)
                        .flatMap(plansByPlanType -> plansByPlanType.getPlans().stream()
                                .filter(Objects::nonNull)
                                .flatMap(plan -> extractPayInRatesFromPlan(plan, plansByPlanType, companyCode))))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.flatMapping(entry -> entry.getValue().stream(), Collectors.toList())
                ));
    }

    private static Stream<BenefitPlanRate> planByBenefitTypeToRates(PlanByBenefitType benefitType, String companyCode, String rateGroupId) {
        if (benefitType.getPlansByPlanType() == null) return Stream.empty();
        return benefitType.getPlansByPlanType().stream()
                .filter(Objects::nonNull)
                .flatMap(planType -> plansByPlanTypeToRates(planType, companyCode, rateGroupId));
    }

    private static Stream<BenefitPlanRate> plansByPlanTypeToRates(PlansByPlanType plansByPlanType, String companyCode, String rateGroupId) {
        if (plansByPlanType.getPlans() == null || plansByPlanType.getPlans().isEmpty()) {
            throw new BSSBadDataException(String.format("No rates returned by flex rate for plans of plan type: %s", plansByPlanType.getPlanType()));
        }
        return plansByPlanType.getPlans().stream()
            .filter(Objects::nonNull)
            .flatMap(planRate -> planRateToRates(planRate, companyCode, plansByPlanType.getPlanType(), rateGroupId));
    }

    private static Stream<BenefitPlanRate> planRateToRates(PlanRate planRate, String companyCode, String planType, String rateGroupId) {
        if (planRate.getRateDetails() == null || CollectionUtils.isEmpty(planRate.getRateDetails().getRates())) {
            throw new BSSBadDataException(String.format("No Rate details or rates returned by flex rate for plan: %s", planRate.getRegionalPlanId()));
        }
        List<PlanRate.RateDetails.Rate> ratesList = planRate.getRateDetails().getRates().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        for (String tier : REGIONAL_TIERS) {
            Optional<PlanRate.RateDetails.Rate> rateOpt = ratesList.stream()
                .filter(r -> tier.equals(r.getTierCode()))
                .findFirst();
            String coverageLevel = CoverageCodesEnums.valueOfName(tier);
            if (rateOpt.isEmpty()) {
                logger.error("event=FlexRateValidationFailed mapperMethod=planRateToRates companyCode={} plan={} coverageLevel={} reason=MISSING_TIER", companyCode, planRate.getRegionalPlanId(), coverageLevel);
                throw new BSSBadDataException(String.format(
                    "Missing retailRate for companyCode=%s, planId=%s, coverageLevel=%s", companyCode, planRate.getRegionalPlanId(), coverageLevel));
            } else if (rateOpt.get().getRetailRate() == null) {
                logger.error("event=FlexRateValidationFailed mapperMethod=planRateToRates companyCode={} plan={} coverageLevel={} reason=NULL_TIER", companyCode, planRate.getRegionalPlanId(), coverageLevel);
                throw new BSSBadDataException(String.format(
                    "Null retailRate for companyCode=%s, planId=%s, coverageLevel=%s", companyCode, planRate.getRegionalPlanId(), coverageLevel));
            }
        }

        return ratesList.stream()
            .map(rate -> {
                BenefitPlanRate bpr = new BenefitPlanRate();
                bpr.setBenefitPlan(planRate.getRegionalPlanId());
                bpr.setPlanType(planType);
                bpr.setCoverageCode(rate.getTierCode());
                bpr.setEmployerCost(BigDecimal.valueOf(rate.getRetailRate()));
                bpr.setRateGroupId(rateGroupId);
                return bpr;
            });
    }

    /**
     * Extracts pay-in rate entries from a single PlanRate.
     * Creates entries for both regionalPlanId and dpRegionalPlanId with their respective coverage levels.
     */
    private static Stream<Map.Entry<String, List<PayInRateInfo>>> extractPayInRatesFromPlan(
            PlanRate plan, PlansByPlanType plansByPlanType, String companyCode) {
        if (plan.getRateDetails() == null || CollectionUtils.isEmpty(plan.getRateDetails().getRates())) {
            throw new BSSBadDataException(
                    String.format("No rate details or rates returned for plan: %s", 
                                 plan.getRegionalPlanId() != null ? plan.getRegionalPlanId() : plan.getPlanId()));
        }
        List<Map.Entry<String, List<PayInRateInfo>>> entries = new ArrayList<>();
        // Extract rates for regional plan (tiers 1, 2, C, 4)
        if (plan.getRegionalPlanId() != null) {
            extractRates(plan, companyCode, plansByPlanType.getPlanType(), plan.getRegionalPlanId(),
                        REGIONAL_TIERS)
                    .ifPresent(entries::add);
        }
        // Extract rates for DP regional plan (tiers 5, 6, 7, 8)
        if (plan.getDpRegionalPlanId() != null) {
            extractRates(plan, companyCode, plansByPlanType.getDpPlanType(), plan.getDpRegionalPlanId(),
                        DP_REGIONAL_TIERS)
                    .ifPresent(entries::add);
        }

        return entries.stream();
    }

    /**
     * Helper method to extract rates for a specific plan ID and tier set.
     * 
     * @param plan The PlanRate containing rate details
     * @param planType The plan type to use in the PayInRateInfo
     * @param planId The plan ID to use as the map key
     * @param tierSet The set of tier codes to filter by
     * @return Optional containing a map entry with planId and list of PayInRateInfo, or empty if no rates found
     */
    private static Optional<Map.Entry<String, List<PayInRateInfo>>> extractRates(
            PlanRate plan, String companyCode, String planType, String planId, Set<String> tierSet) {
        List<PlanRate.RateDetails.Rate> ratesList = plan.getRateDetails().getRates().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (String tier : tierSet) {
            // Find the rate for this tier
            Optional<PlanRate.RateDetails.Rate> rateOpt = ratesList.stream()
                    .filter(r -> tier.equals(r.getTierCode()))
                    .findFirst();
            String coverageLevel = CoverageCodesEnums.valueOfName(tier);
            if (rateOpt.isEmpty()) {
                logger.error("event=FlexRateValidationFailed mapperMethod=extractRates companyCode={} plan={} coverageLevel={} reason=MISSING_TIER", companyCode, planId, coverageLevel);
                throw new BSSBadDataException(String.format(
                    "Missing payInRate for companyCode=%s, planId=%s, coverageLevel=%s", companyCode, planId, coverageLevel));
            } else if (rateOpt.get().getPayInRate() == null) {
                logger.error("event=FlexRateValidationFailed mapperMethod=extractRates companyCode={} plan={} coverageLevel={} reason=NULL_TIER", companyCode, planId, coverageLevel);
                throw new BSSBadDataException(String.format(
                    "Null payInRate for companyCode=%s, planId=%s, coverageLevel=%s", companyCode, planId, coverageLevel));
            }
        }
        List<PayInRateInfo> rates = ratesList.stream()
                .filter(rate -> tierSet.contains(rate.getTierCode()))
                .map(rate -> PayInRateInfo.builder()
                        .planType(planType)
                        .coverageLevel(rate.getTierCode())
                        .payInRate(rate.getPayInRate())
                        .build())
                .collect(Collectors.toList());
        if (!rates.isEmpty()) {
            return Optional.of(new AbstractMap.SimpleEntry<>(planId, rates));
        } else {
            return Optional.empty();
        }
    }

    private FlexRateResponseMapper() {}
}