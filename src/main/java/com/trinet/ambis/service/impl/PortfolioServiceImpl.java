package com.trinet.ambis.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.HrisPlanAttributeService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@Service
public class PortfolioServiceImpl implements PortfolioService {

	
	@Autowired
	private PortfolioRuleDao portfolioRuleDao;

	@Autowired
	private StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	@Autowired
	private HrisPlanAttributeService hrisPlanAttributeService;

	@Autowired
	private StrategyDao strategyDao;

	@Override
	public Map<String, Set<PlanCarrier>> findPrimaryPlanCarriers(Company company) {
		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions(company);
		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioRuleDao.getPortfoliosByHqRegion(
				company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getZipCode(),
				company.getExclusiveMedPlan(), company.getPlanStartDate(), isPickChoose);
		BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());

		return planCarrierMap;
	}

	@Override
	public Set<PlanCarrier> getOmsPlanCarriersForCompanyAndPlanType(Company company, String benefitType) {
		List<Strategy> strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
		if (strategies.isEmpty()) {
			return new HashSet<>();
		}
		Map<Integer, PlanCarrier> planCarriers =
				strategies.stream()
						.map(strategy -> getOmsPlanCarriersForStrategyIdAndPlanType(strategy.getId(), benefitType))
						.flatMap(Set::stream)
						.collect(Collectors.toMap(PlanCarrier::getId, planCarrier -> planCarrier,
								(existing, replacement) -> existing));

		return new HashSet<>(planCarriers.values());
	}

	@Override
	public Set<PlanCarrier> getOmsPlanCarriersForStrategyIdAndPlanType(long strategyId, String benefitType) {
		List<PlanSelection> planSelections = strategyGroupPlanSelectDao.findByStrategyIdAndPlanType(strategyId, benefitType);
		Set<String> planIds = planSelections.stream()
				.map(PlanSelection::getBenefitPlan)
				.collect(Collectors.toSet());
		CompletableFuture<List<BenefitPlanCompare>> planAttributesCompletableFuture =
				hrisPlanAttributeService.getPlanAttributesByBenefitType(planIds, PlanTypesEnum.getName(benefitType));
		List<BenefitPlanCompare> planAttributes = planAttributesCompletableFuture.join();
		Map<Integer, PlanCarrier> planCarriers = planAttributes.stream()
				.filter(planAttribute -> planAttribute.getCarrier() != null)
				.collect(Collectors.toMap(BenefitPlanCompare::getCarrierId,
						planAttribute -> new PlanCarrier(planAttribute.getCarrierId(), planAttribute.getCarrier(), null),
						(existing, replacement) -> existing));

		return new HashSet<>(planCarriers.values());
	}
}
