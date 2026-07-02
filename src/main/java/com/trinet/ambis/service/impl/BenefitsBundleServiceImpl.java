package com.trinet.ambis.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.trinet.ambis.rest.controllers.dto.BundlePlanResponse;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.BenefitsBundleDao;
import com.trinet.ambis.persistence.dao.hrp.CompanyDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.BundlePlansDto;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.BenefitsBundleDto;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.bundle.BundleDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BenefitsBundleServiceImpl implements BenefitsBundleService {
	private static final Logger LOGGER = LoggerFactory.getLogger(BenefitsBundleServiceImpl.class);

    private static final boolean IS_HISTORY = false;
    private static final int PLAN_ID_INDEX = 0;
    private static final int PLAN_TYPE_INDEX = 2;
    private static final int PORTFOLIO_ID_INDEX = 5;
    private static final int BUNDLE_ID_INDEX = 10;

    private final PortfolioService portfolioService;
    private final CompanyService companyService;
    private final PlanRatesService planRatesService;
    private final BenefitsBundleDao benefitsBundleDao;
    private final BenefitPlanDao benefitPlanDao;
    private final RealmDataDao realmDataDao;
    private final CompanyDataDao companyDataDao;

    public BenefitsBundleServiceImpl(PortfolioService portfolioService,
                                     @Lazy CompanyService companyService,
                                     @Lazy PlanRatesService planRatesService,
                                     BenefitsBundleDao benefitsBundleDao,
                                     BenefitPlanDao benefitPlanDao,
                                     RealmDataDao realmDataDao,
                                     CompanyDataDao companyDataDao) {
        this.portfolioService = portfolioService;
        this.companyService = companyService;
        this.planRatesService = planRatesService;
        this.benefitsBundleDao = benefitsBundleDao;
        this.benefitPlanDao = benefitPlanDao;
        this.realmDataDao = realmDataDao;
        this.companyDataDao = companyDataDao;
    }

    @Override
    @Transactional
    public List<BenefitsBundleDto> getBundleDetails(String oeQuarter, String effectiveDate) {
        LocalDate effDate;
        try {
            effDate = LocalDate.parse(effectiveDate,
                    DateTimeFormatter.ofPattern(BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected: "
                    + BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD, e);
        }
        List<Bundle> bundles = AppRulesAndConfigsUtils.isBundleV2Enabled()
                ? benefitsBundleDao.findAllByOeQuarterAndEffectiveDateV2(oeQuarter, effDate)
                : benefitsBundleDao.findAllByOeQuarterAndEffectiveDate(oeQuarter, effDate);
        return bundles.stream()
                .filter(Objects::nonNull)
                .map(bundle -> {
                    List<String> quarters = Optional.ofNullable(bundle.getBundlePlans())
                            .orElseGet(Collections::emptyList)
                            .stream()
                            .filter(bp -> oeQuarter.equals(bp.getId().getOeQuarter())
                                    && effDate.equals(bundle.getEffectiveDate()))
                            .map(bundlePlan-> bundlePlan.getId().getOeQuarter())
                            .distinct()
                            .collect(Collectors.toList());

                    return BenefitsBundleDto.builder()
                            .quarter(String.join(",", quarters))
                            .bundles(List.of(BenefitsBundleDto.BundleDetails.builder()
                                    .id(bundle.getId())
                                    .name(bundle.getName())
                                    .build()))
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
	public Bundle getBundleById(long id) {
		return benefitsBundleDao.findById(id);
	}

    @Override
    public String getCustomBundleCreatedStatus(String companyCode, String type) {
        return benefitsBundleDao.findByCompanyCodeAndType(companyCode, type) != null ? BSSApplicationConstants.YES : BSSApplicationConstants.NO;
    }

    @Override
    public Bundle getBundleByCompanyCode(String companyCode) {
        return benefitsBundleDao.findByCompanyCode(companyCode);
    }

    @Override
    public void save(Bundle bundle) {
        benefitsBundleDao.save(bundle);
    }

    @Override
    public BundlePlanResponse getBundlesByEffectiveDateAndQuarter(LocalDate effectiveDate, String quarter) {
        List<Object[]> rows = AppRulesAndConfigsUtils.isBundleV2Enabled()
                ? benefitsBundleDao.findByEffectiveDateAndQuarterV2(effectiveDate, quarter)
                : benefitsBundleDao.findByEffectiveDateAndQuarter(effectiveDate, quarter);

        // Group rows by bundleId to build the response
        Map<Long, BundlePlanResponse.BundleDto> bundleMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Long bundleId = ((Number) row[0]).longValue();
            String bundleName = (String) row[1];
            String bundleType = (String) row[2];
            String regionalPlanId = (String) row[3];

            BundlePlanResponse.BundleDto bundleDto = bundleMap.computeIfAbsent(bundleId, id ->
                    BundlePlanResponse.BundleDto.builder()
                            .id(id)
                            .name(bundleName)
                            .type(bundleType)
                            .benefitPlanIds(new ArrayList<>())
                            .build()
            );

            if (regionalPlanId == null) {
                LOGGER.warn("No regional plan mapping found for bundleId={}, quarter={}, effectiveDate={}",
                        bundleId, quarter, effectiveDate);
                continue;
            }

            bundleDto.getBenefitPlanIds().add(regionalPlanId);
        }

        return BundlePlanResponse.builder()
                .bundles(new ArrayList<>(bundleMap.values()))
                .build();
    }

    @Override
    public List<BundlePlansDto> getBundleAndExchangePlans(String companyCode, String exchangeId, Set<Long> bundleIds) {
        Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
                "SYSTEM", BenExchngEnums.getByExchangeId(exchangeId));

        Map<String, Set<PlanCarrier>> mapOfprimaryPlanCarriers = portfolioService.findPrimaryPlanCarriers(company);
        Set<String> plansPortfolios = BenefitCategoriesHelper.getPlanCarriers(mapOfprimaryPlanCarriers);
        Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, plansPortfolios,
                realmDataDao);

        List<Object[]> allRows = benefitPlanDao.getAllExchangeAndBundlesPlans(company, plansPortfolios, outOfRegionPlans, bundleIds);

        Map<Integer, Set<Integer>> childToPrimaryCarriers = Collections.emptyMap();
        if(!BenExchngEnums.TRINET_XI.getBenExchng().equals(company.getRealm().getBenExchange())) {
            Map<Integer, Set<Integer>> primaryMedCarrierToChildren =
                    buildPrimaryToChildCarrierAssociation(mapOfprimaryPlanCarriers.get(BSSApplicationConstants.MEDICAL));
            childToPrimaryCarriers = buildChildToPrimaryCarrierAssociation(primaryMedCarrierToChildren);
        }

        Map<String, List<BenefitPlanRate>> rates = planRatesService.getBenefitPlanRatesBy(company,false);

        return buildPlanMappingBundleDtos(allRows, childToPrimaryCarriers, rates);
    }

    private Map<Integer, Set<Integer>> buildPrimaryToChildCarrierAssociation(Set<PlanCarrier> mapOfprimaryPlanCarriers) {
        if (mapOfprimaryPlanCarriers == null || mapOfprimaryPlanCarriers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, Set<Integer>> primaryCarrierToChildren = new LinkedHashMap<>();

        // Create map of primary carrier -> children, and include primary itself in its own set.
        for (PlanCarrier planCarrier : mapOfprimaryPlanCarriers) {
            if (planCarrier.isPrimaryCarrier()) {
                primaryCarrierToChildren
                        .computeIfAbsent(planCarrier.getId(), key -> new LinkedHashSet<>())
                        .add(planCarrier.getId());
            }
        }

        for (PlanCarrier planCarrier : mapOfprimaryPlanCarriers) {
            if (planCarrier.isPrimaryCarrier()) {
                continue;
            }

            List<String> parentIds = planCarrier.getParentId();
            if (parentIds == null || parentIds.isEmpty()) {
                for (Set<Integer> childCarrierIds : primaryCarrierToChildren.values()) {
                    childCarrierIds.add(planCarrier.getId());
                }
                continue;
            }

            for (String parentIdValue : parentIds) {
                Integer parentId = Integer.valueOf(parentIdValue);
                if (parentId != null && primaryCarrierToChildren.containsKey(parentId)) {
                    primaryCarrierToChildren.get(parentId).add(planCarrier.getId());
                }
            }
        }

        return primaryCarrierToChildren;
    }

    private Map<Integer, Set<Integer>> buildChildToPrimaryCarrierAssociation(Map<Integer, Set<Integer>> primaryCarrierToChildren) {
        if (primaryCarrierToChildren == null || primaryCarrierToChildren.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Integer, Set<Integer>> childToPrimaryCarriers = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : primaryCarrierToChildren.entrySet()) {
            Integer primaryCarrierId = entry.getKey();
            for (Integer childCarrierId : entry.getValue()) {
                childToPrimaryCarriers
                        .computeIfAbsent(childCarrierId, key -> new LinkedHashSet<>())
                        .add(primaryCarrierId);
            }
        }
        return childToPrimaryCarriers;
    }

    private List<BundlePlansDto> buildPlanMappingBundleDtos(List<Object[]> allRows,
                                                            Map<Integer, Set<Integer>> childToPrimaryCarriers,
                                                            Map<String, List<BenefitPlanRate>> rates) {
        if (allRows == null || allRows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Map<String, BundlePlansAccumulator>> bundlesByBenefitType = new LinkedHashMap<>();

        for (Object[] row : allRows) {
            processPlanRow(row, childToPrimaryCarriers, bundlesByBenefitType);
        }

        return convertBundlesAccumulatorToDto(bundlesByBenefitType, rates);
    }

    /**
     * Processes a single plan row and accumulates plans by benefit type and carrier grouping.
     */
    private void processPlanRow(Object[] row, Map<Integer, Set<Integer>> childToPrimaryCarriers,
                                Map<String, Map<String, BundlePlansAccumulator>> bundlesByBenefitType) {
        String planId = row[PLAN_ID_INDEX] == null ? null : row[PLAN_ID_INDEX].toString();
        String benefitType = row[PLAN_TYPE_INDEX] == null ? null : row[PLAN_TYPE_INDEX].toString();
        Integer portfolioId = toInteger(row[PORTFOLIO_ID_INDEX]);
        Integer benefitBundleId = toInteger(row[BUNDLE_ID_INDEX]);

        // Validate required fields
        validatePlanRow(planId, benefitType, portfolioId);

        String normalizedBenefitType = normalizeBenefitType(benefitType);

        // Get primary carrier IDs for this plan
        Set<Integer> primaryCarrierIds = resolvePrimaryCarriers(benefitType, portfolioId, childToPrimaryCarriers);

        // Accumulate plan for each carrier
        for (Integer primaryCarrierId : primaryCarrierIds) {
            if(!isMedicalPlanType(benefitType)) {
                primaryCarrierId = null;
            }
            benefitBundleId = benefitBundleId == null ? -1 : benefitBundleId;
            accumulatePlan(bundlesByBenefitType, normalizedBenefitType, benefitBundleId, primaryCarrierId, planId);
        }
    }

    private boolean isMedicalPlanType(String benefitType) {
        return PlanTypesEnum.MEDICAL.getCode().equals(benefitType)
                || PlanTypesEnum.MEDICAL.getName().equalsIgnoreCase(benefitType);
    }

    private String normalizeBenefitType(String benefitType) {
        if (benefitType == null) {
            return null;
        }
        return Arrays.stream(PlanTypesEnum.values())
                .filter(value -> value.getCode().equals(benefitType)
                        || value.getName().equalsIgnoreCase(benefitType))
                .map(PlanTypesEnum::getCode)
                .findFirst()
                .orElse(benefitType);
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    /**
     * Validates that required plan row fields are non-null.
     */
    private void validatePlanRow(String planId, String benefitType, Integer portfolioId) {
        if (planId == null || benefitType == null || portfolioId == null) {
            throw new BSSBadDataException(String.format(
                    "Invalid plan row - planId: [%s], benefitType: [%s], portfolioId: [%s]",
                    planId, benefitType, portfolioId));
        }
    }

    /**
     * Resolves primary carrier IDs for a plan based on benefit type and portfolio.
     * Medical plans are mapped through childToPrimaryCarriers; others default to single null carrier.
     */
    private Set<Integer> resolvePrimaryCarriers(String benefitType, Integer portfolioId,
                                                Map<Integer, Set<Integer>> childToPrimaryCarriers) {
        if (isMedicalPlanType(benefitType)) {
            Set<Integer> carriers = childToPrimaryCarriers.get(portfolioId);
            if (carriers == null || carriers.isEmpty()) {
                return Collections.singleton(portfolioId);
            }
            return carriers;
        }
        // Non-medical plans use a single null carrier
        return Collections.singleton(null);
    }

    /**
     * Accumulates a plan into the bundle structure under benefit type and carrier grouping.
     */
    private void accumulatePlan(Map<String, Map<String, BundlePlansAccumulator>> bundlesByBenefitType,
                               String benefitType, Integer bundleId, Integer carrierId, String planId) {
        String groupKey = createGroupKey(bundleId, carrierId);
        BundlePlansAccumulator bucket = bundlesByBenefitType
                .computeIfAbsent(benefitType, k -> new LinkedHashMap<>())
                .computeIfAbsent(groupKey, k -> new BundlePlansAccumulator(bundleId, carrierId));
        bucket.planIds.add(planId);
    }

    /**
     * Creates a unique grouping key combining bundle ID and carrier ID.
     */
    private String createGroupKey(Integer bundleId, Integer carrierId) {
        return bundleId + "|" + carrierId;
    }

    /**
     * Converts accumulated bundles structure into final DTO format.
     */
    private List<BundlePlansDto> convertBundlesAccumulatorToDto(
            Map<String, Map<String, BundlePlansAccumulator>> bundlesByBenefitType,
            Map<String, List<BenefitPlanRate>> rates) {
        List<BundlePlansDto> result = new ArrayList<>(bundlesByBenefitType.size());

        for (Map.Entry<String, Map<String, BundlePlansAccumulator>> entry : bundlesByBenefitType.entrySet()) {
            List<BundleDetail> bundleDetails = entry.getValue().values().stream()
                    .map(accumulator -> convertAccumulatorToDetail(accumulator, rates))
                    .collect(Collectors.toList());

            result.add(BundlePlansDto.builder()
                    .benefitType(entry.getKey())
                    .bundleDetails(bundleDetails)
                    .build());
        }

        return result;
    }

    /**
     * Converts a single BundlePlansAccumulator to BundleDetail DTO,
     * enriching each plan with coverage-level cost rates in O(1) per plan lookup.
     */
    private BundleDetail convertAccumulatorToDetail(BundlePlansAccumulator accumulator,
                                                    Map<String, List<BenefitPlanRate>> rates) {
        List<BundleDetail.BundlePlanDetail> bundlePlanDetails = accumulator.planIds.stream()
                .map(planId -> BundleDetail.BundlePlanDetail.builder()
                        .planId(planId)
                        .cvgLevelCost(toPlanCostRequests(planId, rates))
                        .build())
                .collect(Collectors.toList());

        return BundleDetail.builder()
                .benefitBundleId(accumulator.benefitBundleId)
                .primaryCarrierId(accumulator.primaryCarrierId)
                .bundlePlanDetails(bundlePlanDetails)
                .build();
    }

    /**
     * Looks up rates for a planId and maps each BenefitPlanRate to a PlanCostRequest.
     * Uses the rates map directly for O(1) lookup by planId.
     */
    private List<BundleDetail.PlanCostRequest> toPlanCostRequests(String planId,
                                                                   Map<String, List<BenefitPlanRate>> rates) {
        List<BenefitPlanRate> planRates = rates.get(planId);
        if (planRates == null || planRates.isEmpty()) {
            return Collections.emptyList();
        }
        return planRates.stream()
                .map(rate -> BundleDetail.PlanCostRequest.builder()
                        .covrgCd(rate.getCoverageCode())
                        .totalCost(rate.getEmployerCost())
                        .build())
                .collect(Collectors.toList());
    }

    private static class BundlePlansAccumulator {
        private final Integer benefitBundleId;
        private final Integer primaryCarrierId;
        private final Set<String> planIds = new LinkedHashSet<>();

        private BundlePlansAccumulator(Integer benefitBundleId, Integer primaryCarrierId) {
            this.benefitBundleId = benefitBundleId;
            this.primaryCarrierId = primaryCarrierId;
        }
    }

    @Override
    public List<BundleSelectionDetailsDto> getBundleSelectionDetails(BundleSelectionDetailsRequest request) {
        List<BundleSelectionDetailsDto> bundleSelectionDetailsList = companyDataDao.getBundleSelectionDetails(
                request.getCompanyCode(), request.getExchangeDatePairs());

        if (CollectionUtils.isEmpty(bundleSelectionDetailsList)) {
            return Collections.emptyList();
        }

        return bundleSelectionDetailsList;
    }
}

