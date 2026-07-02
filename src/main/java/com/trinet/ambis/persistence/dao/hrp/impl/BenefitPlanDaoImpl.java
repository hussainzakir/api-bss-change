package com.trinet.ambis.persistence.dao.hrp.impl;

import static com.trinet.ambis.common.BSSApplicationConstants.REGIONAL;
import static com.trinet.ambis.common.BSSQueryConstants.FILTER_SUB_REGIONS;
import static com.trinet.ambis.common.BSSQueryConstants.RATES_FLAG;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.annotations.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.aop.BSSCacheable;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.common.PlanOfferingReportConstants;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EligiblePlanData;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.Carrier;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;

public class BenefitPlanDaoImpl implements BenefitPlanDao {

    private static final Logger logger = LoggerFactory.getLogger(BenefitPlanDaoImpl.class);

    private static final String ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS = "ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS";
    private static final String ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS_V2 = "ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS_V2";
    private static final String GET_BENEFIT_PLANS_BY_STRATEGY_GROUP = "GET_BENEFIT_PLANS_BY_STRATEGY_GROUP";
    private static final String GET_REGIONAL_PLANS_FOR_STATE = "GET_REGIONAL_PLANS_FOR_STATE";

    @PersistenceContext(unitName = "bis-hrp")
    EntityManager hrpEm;

    @Override
    @BSSCacheable(objectType = CacheObjectTypeEnum.BEN_PLANS_OBJECT_TYPE)
    public Map<String, Set<StateBenefitPlan>> getAllPrimaryBenefitPlans(Set<String> portfolios,
                                                                        @CacheKey(value = "id") Company company, Set<String> outOfRegionPlans) {

        logger.info("$$$$$$$$$$$$$$$$$$$ getting benefit plans for company regions from DB $$$$$$$$$$$$$$");
        int returnOnlyBundlePlans = company.getBundleId() != null ? 1 : 0;
        Set<Long> bundleIds = company.getBundleId() != null ? Set.of(company.getBundleId()) : null;
        Query query = getAllPrimaryPlansForExchangeOrBundleQuery(portfolios, company, bundleIds, outOfRegionPlans, returnOnlyBundlePlans);
        return extractResult(query, ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS, company.getRealmPlanYearId());
    }

    @Override
    public Set<String> getWidelyAvailablePlans(Set<String> plans, long realmPlanYearId) {
        if (plans == null || CollectionUtils.isEmpty(plans)) {
            String sqlName = "GET_WIDELY_AVAILABLE_PLANS";
            Query query = hrpEm.createNamedQuery(sqlName);
            query.setHint(QueryHints.FETCH_SIZE, BSSQueryConstants.HIBERNATE_FETCH_SIZE_250);
            query.setParameter(BSSQueryConstants.PLAN_TYPE, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
            query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
            return new HashSet<>(DaoUtils.getResultStringList(query, sqlName));
        }

        // Batch into chunks of 999 to avoid Oracle ORA-01795 (max 1000 expressions in IN list)
        Set<String> benefitPlans = new HashSet<>();
        String sqlName = "GET_WIDELY_AVAILABLE_PLANS_IN_LIST";
        List<String> planList = new ArrayList<>(plans);
        int batchSize = 999;
        for (int i = 0; i < planList.size(); i += batchSize) {
            List<String> batch = planList.subList(i, Math.min(i + batchSize, planList.size()));
            Query query = hrpEm.createNamedQuery(sqlName);
            query.setHint(QueryHints.FETCH_SIZE, BSSQueryConstants.HIBERNATE_FETCH_SIZE_1000);
            query.setParameter("benefitPlanList", batch);
            query.setParameter(BSSQueryConstants.PLAN_TYPE, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
            query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
            benefitPlans.addAll(DaoUtils.getResultStringList(query, sqlName));
        }
        return benefitPlans;
    }

    @Override
    public Map<String, BenefitPlan> getRegionalBasePlanMapping(RealmPlanYear rpy) {
        Query query = hrpEm.createNamedQuery("regionalBasePlanMappings");
        query.setHint(QueryHints.FETCH_SIZE, BSSQueryConstants.HIBERNATE_FETCH_SIZE_1000);
        query.setParameter(BSSQueryConstants.OE_QUARTER, rpy.getOeQuarter());
        query.setParameter(BSSQueryConstants.REALM_ID, rpy.getRealmId());
        query.setParameter(BSSQueryConstants.EFF_DT, rpy.getPlanYearEnd());
        List<Object[]> results = DaoUtils.getResultList(query, "regionalBasePlanMappings");
        Map<String, BenefitPlan> result = new HashMap<>(results.size());
        for (Object[] r : results) {
            String regionalPlanId = (String) r[0];
            String basePlanId = (String) r[1];
            String basePlanDescr = (String) r[2];
            BenefitPlan benPlan = new BenefitPlan(basePlanId, basePlanDescr);
            result.put(regionalPlanId, benPlan);
        }
        return result;
    }

    @Override
    public List<String> getSelectedRegionalPlansForBasePlan(Long strategyId, Long groupId, Long realmPlanYearId, String basePlanId) {
        Query query = hrpEm.createNamedQuery("SELECTED_REGIONAL_PLANS_FOR_BASE_PLAN_ID");
        query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
        query.setParameter(BSSQueryConstants.GROUP_ID, groupId);
        query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
        query.setParameter("basePlanId", basePlanId);
        return DaoUtils.getResultStringList(query, "SELECTED_REGIONAL_PLANS_FOR_BASE_PLAN_ID");
    }

    @Override
    public List<PlanAppendixBenefitPlanData> getPlansForAppendix(Company company, String strategyId,
                                                                 List<String> regions, List<String> mdPlanTypes, List<String> visionPlanTypes, boolean filterSubregions) {

        Query query = hrpEm.createNamedQuery("APPENDIX_BENEFITPLANS_BY_STRATEGY_ID_AND_PLANTYPE");
        query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
        query.setParameter(BSSQueryConstants.MD_PLAN_TYPES, mdPlanTypes);
        query.setParameter(BSSQueryConstants.VISION_PLAN_TYPES, visionPlanTypes);
        query.setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearEnd());
        query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYearId());
        query.setParameter(BSSQueryConstants.REGIONS, regions);
        query.setParameter(FILTER_SUB_REGIONS, filterSubregions ? 1 : 0);
        query.setParameter(RATES_FLAG, company.getRateType());
        List<Object[]> results = DaoUtils.getResultList(query, "APPENDIX_BENEFITPLANS_BY_STRATEGY_ID_AND_PLANTYPE");
        List<PlanAppendixBenefitPlanData> benefitsPlanDescList = new ArrayList<>();
        for (Object[] result : results) {
            PlanAppendixBenefitPlanData benefitPlanData = new PlanAppendixBenefitPlanData();
            benefitPlanData.setPlanType((String) result[0]);
            benefitPlanData.setBenefitPlan((String) result[1]);
            benefitPlanData.setDescription((String) result[2]);
            benefitPlanData.setRegion((String) result[3]);
            benefitsPlanDescList.add(benefitPlanData);
        }

        return benefitsPlanDescList;
    }

    @Override
    public Map<String, List<String>> getCompanyPlanSelectionsForPlanOfferingReport(PlanOfferingsRequest planOfferingsRequest,
                                                                                   long realmYearId) {
        Query query = hrpEm
                .createNamedQuery(PlanOfferingReportConstants.COMPANY_PLAN_SELECTION_FOR_PLAN_OFFERING_REPORT);
        query.setParameter(BSSQueryConstants.COMPANY_CODE, planOfferingsRequest.getCompanyCode());
        query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmYearId);
        query.setParameter(BSSQueryConstants.PLAN_TYPES, planOfferingsRequest.getBenefitTypes());
        List<Object[]> results = DaoUtils.getResultList(query,
                PlanOfferingReportConstants.COMPANY_PLAN_SELECTION_FOR_PLAN_OFFERING_REPORT);

        return results.stream()
                .collect(Collectors.groupingBy(
                        result -> ((String) result[0]),
                        Collectors.mapping(
                                result -> ((String) result[1]),
                                Collectors.toList()
                        )
                ));
    }

    public void setHrpEm(EntityManager hrpEm) {
        this.hrpEm = hrpEm;
    }

    @Override
    public List<PlanOfferingsBenefitPlanData> getBenefitsPlanOfferingsBy(PlanOfferingsRequest planOfferingsRequest,
                                                                         long realmYearId, Set<String> outOfRegionPlans, boolean isPickChoose) {
        if (PlanOfferingReportConstants.REPORT_CODE_EXCHANGE.equalsIgnoreCase(planOfferingsRequest.getReportCode())) {
            return getApplicablePlansForExchange(planOfferingsRequest, realmYearId, outOfRegionPlans);
        } else if (PlanOfferingReportConstants.REPORT_CODE_WSE.equalsIgnoreCase(planOfferingsRequest.getReportCode())) {
            return getApplicablePlansForWse(planOfferingsRequest, realmYearId, outOfRegionPlans, isPickChoose);
        } else {
            return getApplicablePlansForCompany(planOfferingsRequest, realmYearId, outOfRegionPlans, isPickChoose);
        }
    }

    @Override
    public Map<String, EligiblePlanData> getBenefitPlansAndCarriersBy(long strategyId, long groupId, String planType,
                                                                      Date effectiveDate, String cvgTierCode) {
        Map<String, EligiblePlanData> eligiblePlansMap = new HashMap<>();
        Query q = hrpEm.createNamedQuery(GET_BENEFIT_PLANS_BY_STRATEGY_GROUP);
        q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
        q.setParameter(BSSQueryConstants.GROUP_ID, groupId);
        q.setParameter(BSSQueryConstants.PLAN_TYPE, planType);
        q.setParameter(BSSQueryConstants.EFF_DATE, effectiveDate);
        q.setParameter(BSSQueryConstants.CVG_TIER_CODE, cvgTierCode);

        List<Object[]> results = DaoUtils.getResultList(q, GET_BENEFIT_PLANS_BY_STRATEGY_GROUP);
        if (CollectionUtils.isNotEmpty(results)) {
            for (Object[] result : results) {
                EligiblePlanData eligiblePlanData = new EligiblePlanData();
                String planId = (String) result[0];
                BigDecimal emplrCost = result[2] != null ? (BigDecimal) result[2] : BigDecimal.ZERO;
                BigDecimal empCost = result[3] != null ? (BigDecimal) result[3] : BigDecimal.ZERO;

                eligiblePlanData.setPlanId(planId);
                eligiblePlanData.setCarrier((String) result[1]);
                eligiblePlanData.setPlanCost(emplrCost.add(empCost));

                eligiblePlansMap.put(planId, eligiblePlanData);
            }
        }
        return eligiblePlansMap;
    }

    @Override
    public Map<String, Map<Long, Set<String>>> getPortfolioPlansByPlanTypeForState(String state, long realmYearId, Set<String> outOfRegionPlans) {
        Map<String, Map<Long, Set<String>>> portfolioPlansByPlanType = new HashMap<>();
        Query q = hrpEm.createNamedQuery(GET_REGIONAL_PLANS_FOR_STATE);
        q.setParameter(BSSQueryConstants.STATE, state);
        q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmYearId);
        if (CollectionUtils.isEmpty(outOfRegionPlans)) {
            // Need to pass a not-null array or Oracle throws an exception for empty list
            outOfRegionPlans = new HashSet<>(1);
            outOfRegionPlans.add(BSSApplicationConstants.PLANTOEXCLUDE);
        }
        q.setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, outOfRegionPlans);
        q.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);

        List<Object[]> results = DaoUtils.getResultList(q, GET_REGIONAL_PLANS_FOR_STATE);
        if (CollectionUtils.isNotEmpty(results)) {
            for (Object[] result : results) {
                String planType = (String) result[0];
                Long portfolioId = ((BigDecimal) result[1]).longValue();
                String planId = (String) result[2];
                Map<Long, String> planPortfolios = new HashMap<>();
                planPortfolios.put(portfolioId, planId);
                if (portfolioPlansByPlanType.containsKey(planType)) {
                    Map<Long, Set<String>> portfolioPlanMap = portfolioPlansByPlanType.get(planType);
                    if (portfolioPlanMap.containsKey(portfolioId)) {
                        Set<String> planSet = portfolioPlanMap.get(portfolioId);
                        planSet.add(planId);
                        portfolioPlanMap.put(portfolioId, planSet);
                    } else {
                        Set<String> planSet = new HashSet<>();
                        planSet.add(planId);
                        portfolioPlanMap.put(portfolioId, planSet);
                    }
                    portfolioPlansByPlanType.put(planType, portfolioPlanMap);
                } else {
                    Map<Long, Set<String>> portfolioPlanMap = new HashMap<>();
                    Set<String> planSet = new HashSet<>();
                    planSet.add(planId);
                    portfolioPlanMap.put(portfolioId, planSet);
                    portfolioPlansByPlanType.put(planType, portfolioPlanMap);
                }
            }
        }
        return portfolioPlansByPlanType;
    }

    @Override
    public List<Object[]> getAllExchangeAndBundlesPlans(Company company, Set<String> plansPortfoliosIds, Set<String> outOfRegionPlans, Set<Long> bundleIds) {
        List<Object[]> allRows = new ArrayList<>();
        boolean staticBundlesEnabled = RulesAndConfigsUtils.isGaBundleEnabled(company.getRealmPlanYear().getId());
        Query bundleQuery = null;
        Query exchangeQuery = null;
        if (company.getBundleId() != null) {
            // Get company specific bundle plans
            bundleIds = new HashSet<>();
            bundleIds.add(company.getBundleId());
            bundleQuery = getAllPrimaryPlansForExchangeOrBundleQuery(plansPortfoliosIds, company, bundleIds, outOfRegionPlans, 1);
        }  else {
            // Get all GA bundle plans
            if(staticBundlesEnabled) {
                bundleQuery = getAllPrimaryPlansForExchangeOrBundleQuery(plansPortfoliosIds, company, bundleIds, outOfRegionPlans, 1);
            }
            // Get only exchange specific plans
            exchangeQuery = getAllPrimaryPlansForExchangeOrBundleQuery(plansPortfoliosIds, company, null, outOfRegionPlans, 0);
        }
        String queryName = AppRulesAndConfigsUtils.isBundleV2Enabled()
            ? ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS_V2
            : ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS;        
        if(bundleQuery != null) {
            allRows.addAll(DaoUtils.getResultList(bundleQuery, queryName));
        }
        if(exchangeQuery != null) {
            allRows.addAll(DaoUtils.getResultList(exchangeQuery, queryName));
        }
        return allRows;
    }

    private @NonNull Query getAllPrimaryPlansForExchangeOrBundleQuery(Set<String> portfolios, Company company, Set<Long> bundleIds, Set<String> outOfRegionPlans, int returnOnlyBundlePlans) {
        if (CollectionUtils.isEmpty(outOfRegionPlans)) {
            // Need to pass some random string as a benPlan since oracle IN clause throws
            // exception for empty list.
            outOfRegionPlans = new HashSet<>(1);
            outOfRegionPlans.add(BSSApplicationConstants.PLANTOEXCLUDE);
        }
        bundleIds = CollectionUtils.isEmpty(bundleIds) ? Set.of() : bundleIds;
        String queryName = AppRulesAndConfigsUtils.isBundleV2Enabled()
            ? ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS_V2
            : ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS;             

        Query query = hrpEm.createNamedQuery(queryName);//
        query.setParameter(BSSQueryConstants.BUNDLE_IDS, bundleIds);
        query.setParameter(BSSQueryConstants.EFF_DT, company.getRealmPlanYear().getPlanYearStart());
        query.setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, returnOnlyBundlePlans);

        query.setHint(QueryHints.FETCH_SIZE, BSSQueryConstants.HIBERNATE_FETCH_SIZE_500);
        if (company.isTexasSitus()) {
            query.setParameter(BSSQueryConstants.SITUS, "TX");
        } else {
            query.setParameter(BSSQueryConstants.SITUS, "FL");
        }
        query.setParameter(BSSQueryConstants.PLAN_YEAR_ID, company.getRealmPlanYearId());
        query.setParameter(BSSQueryConstants.PORTFOLIOS, portfolios);
        query.setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, outOfRegionPlans);
        query.setParameter(BSSQueryConstants.OE_QUARTER, company.getRealmPlanYear().getOeQuarter());
        return query;
    }

    private List<PlanOfferingsBenefitPlanData> getApplicablePlansForWse(PlanOfferingsRequest planOfferingsRequest,
                                                                        long realmYearId, Set<String> outOfRegionPlans, boolean isPickChoose) {
        Query query = hrpEm
                .createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_WSE_GET_APPLICABLE_PLANS);
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_REALM_PLAN_YEAR_ID, realmYearId);
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_PICK_CHOOSE_FLAG, isPickChoose ? 1 : 0);
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_HQ_REGION, planOfferingsRequest.getHqState());
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_PRIM_PORTFOLIO,
                planOfferingsRequest.getCarriers().stream().map(Carrier::getId).collect(Collectors.toList()));
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_WSE_STATE, planOfferingsRequest.getState());
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_OUT_OF_REGION_PLANS,
                Objects.nonNull(outOfRegionPlans) ? outOfRegionPlans : Set.of(BSSApplicationConstants.DUMMY));
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_PLAN_TYPES,
                planOfferingsRequest.getBenefitTypes());
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_REALM_PLAN_YEAR_EFFDT,
                Utils.convertDateFormat(planOfferingsRequest.getPlanYearStartDate(),
                        BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY,
                        BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY).get());
        query.setParameter(PlanOfferingReportConstants.QUERY_PARAMETER_RETURN_ALL_SITUS,
                StringUtils.isNotEmpty(planOfferingsRequest.getCompanyCode()) ? 1 : 0);

        List<Object[]> results = DaoUtils.getResultList(query,
                PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_WSE_GET_APPLICABLE_PLANS);
        return convertToPlanOfferingsBenefitPlanData(results);
    }

    private List<PlanOfferingsBenefitPlanData> getApplicablePlansForExchange(PlanOfferingsRequest planOfferingsRequest,
                                                                             long realmYearId, Set<String> outOfRegionPlans) {
        String queryName = AppRulesAndConfigsUtils.isBundleV2Enabled()
                ? PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS_V2
                : PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS;
        Query query = hrpEm.createNamedQuery(queryName);
        query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmYearId);
        query.setParameter(BSSQueryConstants.PORTFOLIO_IDS,
                planOfferingsRequest.getCarriers().stream().map(Carrier::getId).collect(Collectors.toList()));
        query.setParameter(BSSQueryConstants.REGIONS, planOfferingsRequest.getRegions());
        outOfRegionPlans = Objects.nonNull(outOfRegionPlans) ? outOfRegionPlans : Set.of(BSSApplicationConstants.DUMMY);
        query.setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, outOfRegionPlans);
        query.setParameter(BSSQueryConstants.PLAN_TYPE, planOfferingsRequest.getBenefitTypes().get(0));
        query.setParameter(BSSQueryConstants.BUNDLE_ID,
                Objects.nonNull(planOfferingsRequest.getBundleId()) ? planOfferingsRequest.getBundleId()
                        : BSSQueryConstants.ORACLE_NULL);
        query.setParameter(BSSQueryConstants.OE_QUARTER, planOfferingsRequest.getQuarter());
        List<Object[]> results = DaoUtils.getResultList(query,
                PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS);
        return convertToPlanOfferingsBenefitPlanData2(results);
    }

    private List<PlanOfferingsBenefitPlanData> getApplicablePlansForCompany(PlanOfferingsRequest planOfferingsRequest,
                                                                            long realmYearId, Set<String> outOfRegionPlans, boolean isPickChoose) {
        Query query = hrpEm
                .createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_COMPANY_GET_APPLICABLE_PLANS);
        query.setParameter(BSSQueryConstants.PLAN_OFFER_REALM_YEAR_ID, realmYearId);
        query.setParameter(BSSQueryConstants.PICK_CHOOSE_FLAG, isPickChoose ? 1 : 0);
        query.setParameter(BSSQueryConstants.HQ_REGION, planOfferingsRequest.getHqState());
        query.setParameter(BSSQueryConstants.PRIM_PORTFOLIO,
                planOfferingsRequest.getCarriers().stream().map(Carrier::getId).collect(Collectors.toList()));
        query.setParameter(BSSQueryConstants.REGIONS, planOfferingsRequest.getRegions());
        outOfRegionPlans = Objects.nonNull(outOfRegionPlans) ? outOfRegionPlans : Set.of(BSSApplicationConstants.DUMMY);
        query.setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, outOfRegionPlans);
        query.setParameter(BSSQueryConstants.PLAN_TYPES, planOfferingsRequest.getBenefitTypes());
        List<Object[]> results = DaoUtils.getResultList(query,
                PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_COMPANY_GET_APPLICABLE_PLANS);
        return convertToPlanOfferingsBenefitPlanData2(results);
    }

    private List<PlanOfferingsBenefitPlanData> convertToPlanOfferingsBenefitPlanData(List<Object[]> results) {
        return results.stream()
                .map(result -> PlanOfferingsBenefitPlanData.builder().planType((String) result[0])
                        .benefitPlan((String) result[1]).description((String) result[2]).build())
                .collect(Collectors.toList());
    }

    private List<PlanOfferingsBenefitPlanData> convertToPlanOfferingsBenefitPlanData2(List<Object[]> results) {
        return results.stream()
                .map(result -> PlanOfferingsBenefitPlanData.builder().planType((String) result[0])
                        .benefitPlan((String) result[2]).description((String) result[3]).build())
                .collect(Collectors.toList());
    }

    private Map<String, Set<StateBenefitPlan>> extractResult(Query q, String queryName, long planYearId) {
        Map<String, List<String>> autoPlanMap = getAutoSelectPlansByRealmId(planYearId);
        Map<String, Set<StateBenefitPlan>> map = new HashMap<>();
        List<Object[]> results = DaoUtils.getResultList(q, queryName);
        for (Object[] r : results) {
            StateBenefitPlan plan = new StateBenefitPlan();
            String benefitPlan = (String) r[0];
            plan.setBenefitPlan(benefitPlan);
            plan.setDescription((String) r[1]);
            plan.setPlanType((String) r[2]);
            plan.setVendorId((String) r[3]);
            List<String> autoPlanList = autoPlanMap.get(benefitPlan);
            if (autoPlanList != null)
                plan.setCrossRefPlanId(StringUtils.join(autoPlanList, ","));
            else {
                plan.setCrossRefPlanId("");
            }
            plan.setPortfolioId(((BigDecimal) r[5]).longValue());
            plan.setRealmYearId(((BigDecimal) r[6]).intValue());

            String situsValue = (String) r[8];
            plan = setTexasSitusValue(situsValue, plan);
            String planType = plan.getPlanType();
            String planCategory = (String) r[9];
            plan.setPlanCategory(planCategory);

            boolean isMandatory = false;
            BigDecimal mandatoryValue = (BigDecimal) r[7];
            if (mandatoryValue != null) {
                isMandatory = mandatoryValue.compareTo(BigDecimal.ZERO) > 0 ? Boolean.TRUE : Boolean.FALSE;
            }
            if (BSSApplicationConstants.MND.equals(planCategory)) {
                isMandatory = true;
            }
            plan.setMandatory(isMandatory);

            if (Constants.medicalPlanTypeList.contains(planType)) {
                addPlanToMap(plan, map, Constants.MEDICAL);
            } else if (Constants.dentalPlanTypeList.contains(planType)) {
                addPlanToMap(plan, map, Constants.DENTAL);
            } else if (Constants.visionPlanTypeList.contains(planType)) {
                addPlanToMap(plan, map, Constants.VISION);
            }
        }
        return map;
    }

    private Map<String, List<String>> getAutoSelectPlansByRealmId(long planYearId) {
        Query q = hrpEm.createNamedQuery("autoSelectPlansByRealm");
        q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYearId);
        List<Object[]> results = DaoUtils.getResultList(q, "autoSelectPlansByRealm");
        Map<String, List<String>> autoSelectMap = new HashMap<>();
        for (Object[] r : results) {
            String groupName = (String) r[1];
            String benefitPlan = (String) r[0];

            List<String> planList = autoSelectMap.get(groupName);
            if (planList != null) {
                planList.add(benefitPlan);
            } else {
                List<String> plList = new ArrayList<>();
                plList.add(benefitPlan);
                autoSelectMap.put(groupName, plList);
            }
        }

        Map<String, List<String>> autoPlanMap = new HashMap<>();
        for (Object[] r : results) {
            String groupName = (String) r[1];
            String benefitPlan = (String) r[0];
            List<String> selectMap = autoSelectMap.get(groupName);
            List<String> finalMap = new ArrayList<>();
            finalMap.addAll(selectMap);
            finalMap.remove(benefitPlan);
            autoPlanMap.put(benefitPlan, finalMap);

        }
        return autoPlanMap;
    }

    private StateBenefitPlan setTexasSitusValue(String situsValue, StateBenefitPlan plan) {
        boolean isTexasSitusPlan = false;
        if (situsValue != null && situsValue.equalsIgnoreCase(Constants.TEXAS_SITUS_STATE)) {
            isTexasSitusPlan = true;
            plan.setTexasSitus(isTexasSitusPlan);
        }
        return plan;
    }

    private void addPlanToMap(StateBenefitPlan plan, Map<String, Set<StateBenefitPlan>> map, String planType) {

        Set<StateBenefitPlan> planTypeStateBenPlans = null;
        if (map.containsKey(planType)) {
            planTypeStateBenPlans = map.get(planType);
            planTypeStateBenPlans.add(plan);
            map.put(planType, planTypeStateBenPlans);
        } else {
            planTypeStateBenPlans = new HashSet<>();
            planTypeStateBenPlans.add(plan);
            map.put(planType, planTypeStateBenPlans);
        }
    }

}