package com.trinet.ambis.persistence.dao.hrp;

import java.math.BigDecimal;

import org.springframework.stereotype.Repository;

@Repository
public interface StrategyFundingFlatMaxDataDao {
	
	void updateStrategyFundingFlatMax(long strategyId, long groupId, String planType, String coverageLevel, BigDecimal contribution);
	
	
}
