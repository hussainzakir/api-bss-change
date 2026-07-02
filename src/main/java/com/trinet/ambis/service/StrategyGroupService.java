/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.BenefitGroupStrategy;

/**
 * @author hliddle
 *
 */
@Service
public interface StrategyGroupService {
	
	public BenefitGroupStrategy findById(long id);

	public BenefitGroupStrategy findByStrategyIdAndGroupId(long strategyId, long groupId);

	public List<BenefitGroupStrategy> findByStrategyIdAndStatus(long strategyId, String status);		

	/**
	 * 
	 * @param id
	 * @return
	 */
	BenefitGroupStrategy getBenefitGroupStrategyBy(long id);

	/**
	 * 
	 * @param strategyId
	 * @param groupId
	 * @return
	 */
	BenefitGroupStrategy getBenefitGroupStrategy(long strategyId, long groupId);

	/**
	 * 
	 * @param strategyId
	 * @param status
	 * @return
	 */
	List<BenefitGroupStrategy> getBenefitGroupStrategy(long strategyId, String status);

	/**
	 * 
	 * @param benefitGroupStrategy
	 * @return
	 */
	BenefitGroupStrategy saveBenefitGroupStrategy(BenefitGroupStrategy benefitGroupStrategy);

	/**
	 * 
	 * @param benefitGroupStrategies
	 * @return
	 */
	List<BenefitGroupStrategy> saveBenefitGroupStrategies(List<BenefitGroupStrategy> benefitGroupStrategies);

	/**
	 * @param companyCode
	 * @return
	 */
	List<BenefitGroupStrategy> findBy(String companyCode);
	
	/**
	 * 
	 * @param id
	 * @param strategyId
	 * @return
	 */
	BenefitGroupStrategy getBenefitGroupStrategyBy(long id, long strategyId);
	
	/**
	 * Updates the status of BenefitGroupStrategy for strategyId and its groupIds
	 * @param strategyId
	 * @param benGroupIds
	 * @param status
	 */
	void updateBenefitGroupStrategyStatus(BenefitGroupStrategy benefitGroupStrategy, String status);

}
