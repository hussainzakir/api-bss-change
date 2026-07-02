/**
 * 
 */
package com.trinet.ambis.service;

import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;

/**
 * 
 */
public interface ProspectDefaultPlanAssignmentService {

	/**
	 * This method assigns the default plan for selected porfolio by employee
	 * regions for entire census
	 * 
	 * @param company
	 * @param portfolioIds
	 * @param strategyId
	 */
	void insertStrategyDefaultAssignments(Company company, Set<Long> primaryPortfolioIds,
			Set<Long> altPortfolioIds, long strategyId);

	/**
	 * This method first deletes and then assigns the regional default plans to
	 * given employee ids per strategy based on the selected carrier in the strategy
	 * for medical and EE or ER offered type for dental and vision
	 * 
	 * @param emplIds
	 * @param strategyId
	 * @param existingMedStrategyPortfolioMap
	 * @param benTypes
	 */
	void assignDefaultPlanBy(Set<String> emplIds, long strategyId,
			Map<Long, Boolean> existingMedStrategyPortfolioMap, Set<String> benTypes);
	
	/**
	 * This method first deletes and then assigns the regional default plans to
	 * all the employees in given strategies and groups based on the selected carrier in the strategy
	 * for medical and EE or ER offered type for dental and vision.
	 * 
	 * @param strategyIds
	 * @param groupIds
	 * @param existingMedStrategyPortfolioMap
	 * @param benTypes
	 */
	void assignDefaultPlanBy(Set<Long> strategyIds, Set<Long> groupIds,
			Map<Long, Boolean> existingMedStrategyPortfolioMap, Set<String> benTypes);
	
	
	/**
	 * This method assigns the default plans to all the given employees that doesn't have default plan assigned.
	 * This method is useful for assigning the plans to employees that are transitioning from Waived covg code to other.
	 * 
	 * @param noLocChangeEmployeeIds
	 * @param strategyId
	 * @param companyId
	 * @param existingMedStrategyPortfolioMap
	 */
	void assignDefaultPlanForMissingEmployees(Set<String> noLocChangeEmployeeIds, long strategyId, long companyId,
			Map<Long, Boolean> existingMedStrategyPortfolioMap, boolean isTibCompany);
	
}