/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.StrategyFundingModelDao;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.service.StrategyFundingModelService;

/**
 * @author rvutukuri
 *
 */
@Service
public class StrategyFundingModelServiceImpl implements StrategyFundingModelService {

	@Autowired
	StrategyFundingModelDao strategyFundingModelDao;

	@Override
	public void createUpdateFunding(StrategyFundingModel strategyFundingModel) {
		strategyFundingModelDao.saveAndFlush(strategyFundingModel);
	}

	public void setStrategyFundingModelDao(StrategyFundingModelDao strategyFundingModelDao) {
		this.strategyFundingModelDao = strategyFundingModelDao;
	}

	@Override
	public List<StrategyFundingModel> getStrategyFundingModelByStrategyIdAndGroupId(long strategyId, long groupId) {
		return strategyFundingModelDao.findByStrategyIdAndGroupId(strategyId, groupId);
	}

	@Override
	public List<StrategyFundingModel> saveAll(List<StrategyFundingModel> list) {
		return strategyFundingModelDao.saveAll(list);
	}

	@Override
	public void deleteAll(List<StrategyFundingModel> list) {
		strategyFundingModelDao.deleteAllInBatch(list);
	}

	@Override
	public void deleteStrategyFundingModelByStrategyIdAndGroupId(long strategyId, long groupId) {
		List<StrategyFundingModel> sfm = strategyFundingModelDao.findByStrategyIdAndGroupId(strategyId, groupId);
		strategyFundingModelDao.deleteAllInBatch(sfm);
	}
	
	@Override
	public List<StrategyFundingModel> getStrategyFundingModelByStrategyId(long strategyId) {
		return strategyFundingModelDao.findByStrategyId(strategyId);
	}
}
