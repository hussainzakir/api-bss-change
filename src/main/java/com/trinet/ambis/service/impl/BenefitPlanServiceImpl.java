/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.PlanTypePortfolio;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;

/**
 * @author schaudhari
 *
 */
@Service
public class BenefitPlanServiceImpl implements BenefitPlanService {
	private static final Logger LOGGER = LoggerFactory.getLogger(BenefitPlanServiceImpl.class);

	@Autowired
	RealmDataDao realmDataDao;

	@Autowired
	PortfolioRuleDao portfolioRuleDao;

	@Autowired
	BenefitPlanDao benefitPlanDao;

	@Autowired
	PortfolioService portfolioService;

    @Autowired
    PsCompanyDao psCompanyDao;

    @Autowired
    PlanRatesService planRatesService;

    @Autowired
    RealmPlyrPlanService realmPlyrPlanService;

	@Override
	public Set<String> getAllPrimaryBenefitPlansForPlanRates(Company company) {
		Map<String, Set<PlanCarrier>> mapOfprimaryPlanCarriers = portfolioService.findPrimaryPlanCarriers(company);
		Set<String> plansPortfolios = BenefitCategoriesHelper.getPlanCarriers(mapOfprimaryPlanCarriers);

		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, plansPortfolios,
				realmDataDao);

		Map<String, Set<StateBenefitPlan>> benefitPlansByPlanTypes = benefitPlanDao
				.getAllPrimaryBenefitPlans(plansPortfolios, company, outOfRegionPlans);
		LOGGER.info("LIST OF STATE PLANS BY PLAN TYPE : {}", benefitPlansByPlanTypes.size());

		return BenefitCategoriesHelper.getAllBenefitPlans(benefitPlansByPlanTypes);
	}

	@Override
	public Map<String, BenefitPlan> getRegionalBasePlanMapping(RealmPlanYear rpy) {
		return benefitPlanDao.getRegionalBasePlanMapping(rpy);
	}


    @Override
    public List<CarrierMinimumFunding> getLowestCostPlanPerCarrier(Company company) {
        String minFundingType = RulesAndConfigsUtils.getMinFundingType(company.getRealmPlanYearId());
        if(minFundingType == null || !(minFundingType.equals(BSSApplicationConstants.HQ_MIN_FUNDING_TYPE))) {
            return null;
        }
        if (AppRulesAndConfigsUtils.isLowestCostPlanPerCarrierV2Enabled())
        {
            return getLowestCostPlanPerCarrierPlanType(company);
        }
        return psCompanyDao.getLowestCostPlanPerPlanCarrier(company);
    }

    private Set<String> extractValidBenefitPlanIds(Map<String, Map<Long, Set<String>>> portfolioPlansByPlanType) {
        return portfolioPlansByPlanType.values().stream()
                .flatMap(portfolioMap -> portfolioMap.values().stream())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Map<String, List<BenefitPlanRate>> filterPlanRates(Map<String, List<BenefitPlanRate>> allRates, Set<String> validIds) {
        return allRates.entrySet().stream()
                .filter(entry -> validIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, List<PlanTypePortfolio>> buildPlanIdToPortfolioMap(Map<String, Map<Long, Set<String>>> portfolioPlansByPlanType) {
        Map<String, List<PlanTypePortfolio>> reverseMap = new HashMap<>();
        for (Map.Entry<String, Map<Long, Set<String>>> planTypeEntry : portfolioPlansByPlanType.entrySet()) {
            String planType = planTypeEntry.getKey();
            for (Map.Entry<Long, Set<String>> portfolioEntry : planTypeEntry.getValue().entrySet()) {
                Long portfolioId = portfolioEntry.getKey();
                for (String planId : portfolioEntry.getValue()) {
                    reverseMap.computeIfAbsent(planId, k -> new ArrayList<>())
                            .add(new PlanTypePortfolio(portfolioId, planType));
                }
            }
        }
        return reverseMap;
    }

    private BigDecimal getEmployeeOnlyRate(List<BenefitPlanRate> rateList) {
        Map<String, BigDecimal> planCostMap = StrategyUtils.getPlanCost(rateList);

        return planCostMap.get(CoverageCodesEnums.COV_EMPLOYEE.getId());
    }

    private List<CarrierMinimumFunding> getLowestCostPlanPerCarrierPlanType(Company company) {
        String state = company.getHeadQuatersState();
        long realmYearId = company.getRealmPlanYearId();

        Map<String, Set<PlanCarrier>> primaryPlanCarriers = portfolioService.findPrimaryPlanCarriers(company);
        Set<String> plansPortfolios = BenefitCategoriesHelper.getPlanCarriers(primaryPlanCarriers);
        Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, plansPortfolios, realmDataDao);

        Map<String, Map<Long, Set<String>>> portfolioPlansByPlanType =
                benefitPlanDao.getPortfolioPlansByPlanTypeForState(state, realmYearId, outOfRegionPlans);

        Map<String, List<BenefitPlanRate>> allPlanRates = planRatesService.getBenefitPlanRatesBy(company);

        Set<String> validBenefitPlanIds = extractValidBenefitPlanIds(portfolioPlansByPlanType);
        Map<String, List<BenefitPlanRate>> filteredPlanRates = filterPlanRates(allPlanRates, validBenefitPlanIds);
        Map<String, List<PlanTypePortfolio>> planIdToPortfolioMap = buildPlanIdToPortfolioMap(portfolioPlansByPlanType);

        List<CarrierMinimumFunding> fundingCandidates = new ArrayList<>();

        for (Map.Entry<String, List<BenefitPlanRate>> entry : filteredPlanRates.entrySet()) {
            String benefitPlanId = entry.getKey();
            List<BenefitPlanRate> rateList = entry.getValue();

            BigDecimal employeeOnlyRate = getEmployeeOnlyRate(rateList);
            if (employeeOnlyRate == null) continue;

            List<PlanTypePortfolio> mappings = planIdToPortfolioMap.getOrDefault(benefitPlanId, Collections.emptyList());
            for (PlanTypePortfolio mapping : mappings) {
                fundingCandidates.add(new CarrierMinimumFunding(mapping.getPortfolioId(), mapping.getPlanType(), employeeOnlyRate));
            }
        }

        List<CarrierMinimumFunding> finalCandidates = new ArrayList<>(fundingCandidates.stream()
                .collect(Collectors.toMap(
                        f -> f.getCarrierId() + "|" + f.getPlanType(),
                        Function.identity(),
                        BinaryOperator.minBy(Comparator.comparing(CarrierMinimumFunding::getMinimumFundingAmt))
                ))
                .values());


        List<CarrierMinimumFunding> medicalCarrierMinimums = finalCandidates.stream()
                .filter(f -> f.getPlanType().equals(BSSApplicationConstants.MEDICAL_PLAN_TYPE))
                .collect(Collectors.toList());

        List<CarrierMinimumFunding> dentalVisionCarrierMinimums = new ArrayList<>(
                finalCandidates.stream()
                        .filter(f -> f.getPlanType().equals(BSSApplicationConstants.DENTAL_PLAN_TYPE) ||
                                f.getPlanType().equals(BSSApplicationConstants.VISION_PLAN_TYPE))
                        .collect(Collectors.toMap(
                                CarrierMinimumFunding::getPlanType,
                                Function.identity(),
                                BinaryOperator.minBy(Comparator.comparing(CarrierMinimumFunding::getMinimumFundingAmt))
                        ))
                        .values()
        );

        List<CarrierMinimumFunding> result = new ArrayList<>();
        result.addAll(medicalCarrierMinimums);
        result.addAll(dentalVisionCarrierMinimums);

        return result;

    }

}