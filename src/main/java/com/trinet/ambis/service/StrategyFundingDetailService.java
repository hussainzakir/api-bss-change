package com.trinet.ambis.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.StrategyFundingDetail;
import com.trinet.ambis.persistence.model.StrategyFundingFlatMax;

@Service
public interface StrategyFundingDetailService {
	/**
	 * 
	 * @param strategyFundingId
	 * @return
	 */
	List<StrategyFundingDetail> getStrategyFundingDetailByStrategyFundingId(long strategyFundingId);

	/**
	 * 
	 * @param list
	 */
	void deleteAll(List<StrategyFundingDetail> list);

	/**
	 * 
	 * @param strategyFundingId
	 * @return
	 */
	List<StrategyFundingFlatMax> getStrategyFundingFlatMaxByStrategyFundingId(long strategyFundingId);
	
	/**
	 * 
	 */
	void updateStrategyFundingDetail(long strategyId, long groupId, String planType, String coverageLevel, BigDecimal contribution);
	
	void updateStrategyFundingFlatMax(long strategyId, long groupId, String planType, String coverageLevel, BigDecimal contribution) ;

}
