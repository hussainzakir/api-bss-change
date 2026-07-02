package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.StrategyFundingDetail;
import com.trinet.ambis.persistence.model.StrategyFundingDetailId;


/**
 * 
 * @author akaparaboyna
 *
 */

@Repository
@Transactional(readOnly = true)
public interface StrategyFundingDetailDao extends JpaRepository<StrategyFundingDetail, StrategyFundingDetailId> {
	
	List<StrategyFundingDetail> findBySfDetailIdStrategyFundingId(long id);
	
	
}
