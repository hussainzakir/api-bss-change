package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.service.model.output.AdditionalBenefitPlanDto;

/**
 * @author rvutukuri
 *
 */
@Repository 
public interface StrategyGroupDataDao {
	/**
	 * 
	 * @param strategyId
	 */
	void resetStrategyContributionHeadcounts(Long strategyId);

	/**
	 * 
	 * @param strategyId
	 */
	void resetStrategyPlanSelectHeadcounts(Long strategyId);

	/**
	 * 
	 * @param company
	 * @param strategyId
	 */
	void updateStrategyGroupStatus(Company company, Long strategyId);

	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	List<String> getMedStrategyPortfolios(long strategyId);
	
	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param history
	 * @return
	 */
	Map<String, Long> getStrategyWaiverHeadCount(Company company, long strategyId, boolean history);


	/**
	 * This method is for getting the portfolio FSA plans
	 * 
	 * @param strategyPortfolios
	 * @return
	 */
	List<String> getPortfolioFsaPlans(Company company, List<String> strategyPortfolios);

	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	Map<String, Map<String, Set<Long>>> getStrategyPortfoliosByPlanType(long strategyId);

	/**
	 * Scan the strategy for portfolios linked to an exclusive carrier and return those exclusive
	 * carriers.
	 *
	 * @param strategyId
	 * @param id
	 * @return a List of exclusive carrier codes.  If the List contains more than one exclusive
	 * carrier, you may have an invalid strategy.
	 */
	List<String> getExclMedPlanPortfolio( long strategyId, long realmPlanYearId);
	

	/**
	 * Returns list of all PlanSelection objects for given strategyId and
	 * groupId along with the highDeductiblePlan being set
	 * 
	 * @param strategyId
	 * @param groupId
	 * @param realmPlanYearId
	 * return List<PlanSelection>
	 */
	public List<PlanSelection> getPlanSelections(long strategyId, long groupId, long realmPlanYearId);

	/**
	 * 
	 * @param strategyId
	 * @param company
	 * @param regions
	 * @param portfolios
	 * @param outOfRegionPlans
	 * @return
	 */
	List<String> getStrategyOutOfLocationPlans(long strategyId, Company company, Set<String> regions,
			Set<String> portfolios, Set<String> outOfRegionPlans);

	/**
	 * @param strategyId
	 * @param company
	 * @param portfolios
	 * @param outOfRegionPlans
	 * @return
	 */
	List<String> getStrategyPortfolioMissingPlans(long strategyId, Company company, Set<String> portfolios,
			Set<String> outOfRegionPlans);
	
	/**
	 * 
	 * @param strategyId
	 * @param benefitPlans
	 */
	void deleteStrategyOutOfLocationPlans(long strategyId,List<String> benefitPlans);

	/**
	 * @param strategyId
	 * @param company
	 * @param portfolios
	 * @param outOfRegionPlans
	 * @return
	 */
	List<String> getStrategyAutoSelectPlans(long strategyId, Company company,
			Set<String> portfolios, Set<String> outOfRegionPlans);

	/**
	 * 
	 * @param strategyId
	 * @param realmYearId
	 * @return
	 */
	Map<String, List<AdditionalBenefitPlanDto>> getAdditionalBenPlanSelections(long strategyId, long realmYearId);

}
