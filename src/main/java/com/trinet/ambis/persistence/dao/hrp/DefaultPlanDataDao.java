/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;

/**
 * 
 */
public interface DefaultPlanDataDao {

	/**
	 *
	 * @param company
	 * @return
	 */
	Map<String, Map<String, Long>> getRegionalDefaultPlansByPlanType(Company company);

	/**
	 * This method is for inserting strategy default assignments
	 * 
	 * @param company
	 * @param primaryPortfolioIds
	 * @param altPortfolioIds
	 * @param strategyId
	 */
	void insertStrategyDefaultAssignmentsBy(Company company, Set<Long> primaryPortfolioIds,
			Set<Long> altPortfolioIds, long strategyId);

	/**
	 * 
	 * @param fromStrategyId
	 * @param toStrategyId
	 */
	void copyStrategyAssignments(long fromStrategyId, long toStrategyId);

	/**
	 * This method assigns the default plans to given employees per given strategy. 
	 * The portfolio id for medical will be selected
	 * amongst the selected portfolio list in the strategy. Also, ee or er paid
	 * version of dental and vision will be selected based on what is offered in the
	 * strategy and group that employee belongs to
	 * 
	 * @param emplIds
	 * @param strategyId
	 * @param primaryPortfolioIds
	 * @param alternatePortfolioIds
	 * @param benTypes
	 */
	void insertStrategyDefaultAssignmentsBy(Set<String> emplIds, long strategyId, List<Long> primaryPortfolioIds,
			List<Long> alternatePortfolioIds, Set<String> benTypes);
	
	/**
	 * @param strategyId
	 * @param groupId
	 * @param primaryMedPortfolio
	 * @param alternateMedPortfolio
	 * @param benTypes
	 */
	void insertStrategyDefaultAssignmentsBy(Set<Long> strategyId, Set<Long> groupId, List<Long> primaryMedPortfolio,
			List<Long> alternateMedPortfolio, Set<String> benTypes);

	/**
	 * 
	 * @param region
	 * @param strategyId
	 * @param realmPlanYearId
	 * @param benefitType
	 * @param employeeId
	 * @return
	 */
	List<String> getEligibleRegionalPlans(String region, long strategyId, long realmPlanYearId, String benefitType,
			String employeeId);

	Map<String, Set<String>> getMissingEmplPlanAssignmentsBy(Set<String> emplIds, long strategyId, long companyId);
}
