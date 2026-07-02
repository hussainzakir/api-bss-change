package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.service.model.HeadCountBenefitPlan;

/**
 * @author rvutukuri
 *
 */
public interface PortfolioHeadCountDataDao {


	/**
	 * 
	 */
	Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> getBenefitProgramHeadCounts(long strategyId,
			Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio);

	/**
	 * 
	 */
	Map<String, List<HeadCountBenefitPlan>> getHeadCountPlans(long strategyId);
	

	/**
	 * 
	 * @param company
	 * @param realmYearId
	 * @return
	 */
	Map<String, Map<String, Map<String, Long>>> getMirrorPlanHeadCounts(String company, long realmYearId);
	
	/**
	 * @param strategyId
	 * @return
	 */
	Map<String, List<HeadCountBenefitPlan>> getProspectHeadCountPlans(long strategyId);
	
	/**
	 * @param strategyId
	 * @param benefitGroupPlanTypePortfolio
	 * @return
	 */
	Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> getProspectBenefitProgramHeadCounts(long strategyId,
			Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio);

}
