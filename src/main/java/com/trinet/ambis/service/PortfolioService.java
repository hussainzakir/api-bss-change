package com.trinet.ambis.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.PlanCarrier;

@Service
public interface PortfolioService {

	Map<String, Set<PlanCarrier>> findPrimaryPlanCarriers(Company company);

	/**
	 * This method will return plan carriers for the given company and benefit type
	 *
	 * @param company
	 * @return
	 */
	Set<PlanCarrier> getOmsPlanCarriersForCompanyAndPlanType(Company company, String benefitType);

	/**
	 * This method will return plan carriers for the given strategy id and benefit type
	 *
	 * @param strategyId
	 * @param benefitType
	 * @return
	 */
	Set<PlanCarrier> getOmsPlanCarriersForStrategyIdAndPlanType(long strategyId, String benefitType);
}
