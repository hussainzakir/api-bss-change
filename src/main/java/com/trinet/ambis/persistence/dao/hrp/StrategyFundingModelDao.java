package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyFundingModel;

/**
 * @author rvutukuri
 *
 */
@Repository
@Transactional(readOnly = true)
public interface StrategyFundingModelDao extends JpaRepository<StrategyFundingModel, Long> {

	/**
	 * This method is for finding the funding model by strategy id & group id
	 * 
	 * @param strategyId
	 * @param groupId
	 * @return
	 */
	List<StrategyFundingModel> findByStrategyIdAndGroupId(long strategyId, long groupId);
	
	/**
	 * This method removes StrategyFundingModel for given strategyIds and plan type code.
	 * 
	 * @param strategyIds
	 * @param planTypeCodes
	 */
	void deleteByStrategyIdInAndPlanTypeIn(Set<Long> strategyIds, Set<String> planTypeCodes);
	
	/**
	 * This method is for finding the funding model by strategy id
	 * 
	 * @param strategyId
	 * @return
	 */
	List<StrategyFundingModel> findByStrategyId(long strategyId);
}
