package com.trinet.ambis.persistence.dao.hrp;

import java.math.BigDecimal;

import org.springframework.stereotype.Repository;

@Repository
public interface StrategyFundingDetailDataDao {
	
	void updateStrategyFundingDetail(long strategyId, long groupId, String planType, String coverageLevel, BigDecimal contribution);
	
	
}
