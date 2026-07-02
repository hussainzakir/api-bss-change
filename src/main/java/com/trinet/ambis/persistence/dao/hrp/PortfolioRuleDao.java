package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.dao.hrp.dto.CompanyStrategyPortfolioDetailsDto;
import com.trinet.ambis.service.model.PlanCarrier;

public interface PortfolioRuleDao {
	/**
	 * 
	 * @param realmYearId
	 * @param state
	 * @param zipCode
	 * @param effdt
	 * @return
	 */
	Map<String, Set<PlanCarrier>> getPortfoliosByHqRegion(long realmYearId, String state, String zipCode,
			String exclusiveMedPlan, String effdt, boolean isPickAndChoose );
	/**
	 * 
	 * @param strategyId
	 * @param planYearId
	 * @param defaultPlanMap
	 * @param region
	 * @return
	 */
	Map<String, Set<PlanCarrier>> getStrategyPortfolios(long strategyId, long planYearId,
			Map<String, Map<String, String>> defaultPlanMap, String region, boolean isPickChoose );
	
	/**
	 * Gets portfolio ids, strategy ids, realm plan year id, realm id for the given
	 * company ids
	 * 
	 * @param companyIds
	 * @return map of company id and its CompanyStrategyPortfolioDetails
	 */
	Map<Long, CompanyStrategyPortfolioDetailsDto> getCompanyStrategyPortfolioIds(List<Long> companyIds);
	
	/**
	 * @param strategyId
	 * @param planYearId
	 * @param region
	 * @return
	 */
	Map<Long, Boolean> getMedicalPortfoliosBy(long strategyId, long planYearId, String region);
	
}

