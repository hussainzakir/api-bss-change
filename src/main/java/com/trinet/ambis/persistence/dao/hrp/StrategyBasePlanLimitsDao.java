/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyFundingBasePlanLimits;
import com.trinet.ambis.persistence.model.StrategyFundingDetailId;

/**
 * @author rvutukuri
 *
 */
@Repository
@Transactional(readOnly = true)
public interface StrategyBasePlanLimitsDao
		extends JpaRepository<StrategyFundingBasePlanLimits, StrategyFundingDetailId> {

}
