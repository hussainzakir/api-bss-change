/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.StrategyFundingModel;

/**
 * @author rvutukuri
 *
 */
@Service
public interface StrategyFundingModelService {
	/**
	 * 
	 * @param strategyFundingModel
	 */
	public void createUpdateFunding(StrategyFundingModel strategyFundingModel);

	/**
	 * 
	 * @param strategyId
	 * @param groupId
	 * @return
	 */
	public List<StrategyFundingModel> getStrategyFundingModelByStrategyIdAndGroupId(long strategyId, long groupId);

	/**
	 * 
	 * @param list
	 * @return
	 */
	public List<StrategyFundingModel> saveAll(List<StrategyFundingModel> list);

	/**
	 * 
	 * @param list
	 */
	public void deleteAll(List<StrategyFundingModel> list);

	/**
	 * 
	 * @param strategyId
	 * @param groupId
	 */
	void deleteStrategyFundingModelByStrategyIdAndGroupId(long strategyId, long groupId);
	
	/**
	 * This method will return  strategy funding models for the given strategy id.
	 * 
	 * @param strategyId
	 * @return
	 */
	List<StrategyFundingModel> getStrategyFundingModelByStrategyId(long strategyId);
}
