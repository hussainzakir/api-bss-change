/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyHsaFunding;

/**
 * @author hliddle
 *
 */
/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface StrategyHsaFundingDao extends JpaRepository<StrategyHsaFunding, Long> {
	
	/**
	 * 
	 * @param strategyId
	 * @return
	 */
	StrategyHsaFunding findByStrategyId(long strategyId);
	
	/**
	 * @param strategyIds
	 */
	void deleteByStrategyIdIn(Set<Long> strategyIds);
	
}