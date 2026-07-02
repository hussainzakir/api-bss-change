/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyFundingDetailId;
import com.trinet.ambis.persistence.model.StrategyFundingFlatMax;

/**
 * @author rvutukuri
 *
 */

@Repository
@Transactional(readOnly = true)
public interface StrategyFundingFlatMaxDao extends JpaRepository<StrategyFundingFlatMax, StrategyFundingDetailId> {
	List<StrategyFundingFlatMax> findBySfDetailIdStrategyFundingId(long id);
}