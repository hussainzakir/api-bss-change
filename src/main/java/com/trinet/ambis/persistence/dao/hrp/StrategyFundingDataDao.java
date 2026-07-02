package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.ModelCompareStrategy;

@Repository
public interface StrategyFundingDataDao {

	/**
	 * 
	 * @param fundingModel
	 * @return
	 */
	List<String> getBsuppStrategyFundVolPlanTypes(long fundingModel);
	
	/**
	 * @param strategyIds
	 * @param company
	 * @param includeGroupExcessOptions
	 * @return
	 */
	Map<Long, ModelCompareStrategy> getFundingDetailsByStrategyId(List<Long> strategyIds, Company company, boolean includeGroupExcessOptions, Date effDate);

	/**
	 * This method returns a map of benefitProgram and planTypes that are setup with EEC funding for the passed in company and realm plan year 
	 * 
	 * @param companyCode
	 * @param realmPlanYearId
	 * @return
	 */
	Map<String, Set<String>> getEecFunding(String companyCode, long realmPlanYearId);
	
	/**
	 * Retrieves a map of plan-level overrides for the given strategy ID.
	 * 
	 * @param strategyId
	 * @return
	 */
	Map<Long, Set<String>> getPlanLevelOverrides(long strategyId);
}
