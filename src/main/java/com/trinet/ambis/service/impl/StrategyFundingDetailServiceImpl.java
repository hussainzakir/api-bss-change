package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDetailDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDetailDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingFlatMaxDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingFlatMaxDataDao;
import com.trinet.ambis.persistence.model.StrategyFundingDetail;
import com.trinet.ambis.persistence.model.StrategyFundingFlatMax;
import com.trinet.ambis.service.StrategyFundingDetailService;

/**
 * 
 * @author akaparaboyna
 *
 */

@Service
public class StrategyFundingDetailServiceImpl implements StrategyFundingDetailService {

	@Autowired
	StrategyFundingDetailDao strategyFundingDetailDao;

	@Autowired
	StrategyFundingFlatMaxDao strategyFundingFlatMaxDao;

	@Autowired
	StrategyFundingDetailDataDao strategyFundingDetailDataDao;

	@Autowired
	StrategyFundingFlatMaxDataDao strategyFundingFlatMaxDataDao;

	public void setStrategyFundingDetailDao(StrategyFundingDetailDao strategyFundingDetailDao) {
		this.strategyFundingDetailDao = strategyFundingDetailDao;
	}

	public void setStrategyFundingFlatMaxDao(StrategyFundingFlatMaxDao strategyFundingFlatMaxDao) {
		this.strategyFundingFlatMaxDao = strategyFundingFlatMaxDao;
	}

	@Override
	public List<StrategyFundingDetail> getStrategyFundingDetailByStrategyFundingId(long strategyFundingId) {
		return strategyFundingDetailDao.findBySfDetailIdStrategyFundingId(strategyFundingId);
	}

	@Override
	public void deleteAll(List<StrategyFundingDetail> list) {
		strategyFundingDetailDao.deleteAllInBatch(list);
	}

	@Override
	public List<StrategyFundingFlatMax> getStrategyFundingFlatMaxByStrategyFundingId(long strategyFundingId) {
		return strategyFundingFlatMaxDao.findBySfDetailIdStrategyFundingId(strategyFundingId);
	}
	
	@Override
	public void updateStrategyFundingDetail(long strategyId, long groupId, String planType, String coverageLevel, BigDecimal contribution) {
		strategyFundingDetailDataDao.updateStrategyFundingDetail(strategyId, groupId, planType, coverageLevel, contribution);
	}
	
	@Override
	public void updateStrategyFundingFlatMax(long strategyId, long groupId, String planType, String coverageLevel, BigDecimal contribution) {
		strategyFundingFlatMaxDataDao.updateStrategyFundingFlatMax(strategyId, groupId, planType, coverageLevel, contribution);
	}
	
}
