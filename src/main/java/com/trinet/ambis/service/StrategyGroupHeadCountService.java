/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;

/**
 * @author hliddle
 *
 */
@Service
public interface StrategyGroupHeadCountService {

	/**
	 * This method returns all StrategyGroupHeadCount entities for given single
	 * strategyGroupId.
	 * 
	 * @param strategyGroupId
	 * @return List<StrategyGroupHeadCount>
	 */
	List<StrategyGroupHeadCount> findStrategyGroupHeadCountBy(long strategyGroupId);

	/**
	 * This method saves the given StrategyGroupHeadCount object.
	 * 
	 * @param sghc
	 * @return StrategyGroupHeadCount
	 */
	StrategyGroupHeadCount saveStrategyGroupHeadCount(StrategyGroupHeadCount sghc);
}
