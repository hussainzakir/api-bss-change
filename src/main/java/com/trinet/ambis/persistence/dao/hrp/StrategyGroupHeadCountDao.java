package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCountId;

/**
 * @author schaudhari
 *
 */
@Repository
@Transactional(readOnly = true)
public interface StrategyGroupHeadCountDao extends JpaRepository<StrategyGroupHeadCount, StrategyGroupHeadCountId> {

	/**
	 * This method returns list of all StrategyGroupHeadCount entities for given
	 * single strategyGroupId.
	 * 
	 * @param strategyGroupId
	 * @return List<StrategyGroupHeadCount>
	 */
	List<StrategyGroupHeadCount> findByIdStrategyGroupId(long strategyGroupId);
}
