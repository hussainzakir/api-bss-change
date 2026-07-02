
package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.BenefitStrategyGroupDao;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.service.StrategyGroupService;

/**
 * @author hliddle
 *
 */
@Service
public class StrategyGroupServiceImpl implements StrategyGroupService {

	@Autowired
	BenefitStrategyGroupDao benefitStrategyGroupDao;

	@Override
	public BenefitGroupStrategy findById(long id) {
		return benefitStrategyGroupDao.findById(id);
	}

	@Override
	public BenefitGroupStrategy findByStrategyIdAndGroupId(long strategyId, long groupId) {
		return benefitStrategyGroupDao.findByStrategyIdAndGroupId(strategyId, groupId);
	}

	@Override
	public List<BenefitGroupStrategy> findByStrategyIdAndStatus(long strategyId, String status) {
		return benefitStrategyGroupDao.findByStrategyIdAndStatus(strategyId, status);
	}

	@Override
	public BenefitGroupStrategy getBenefitGroupStrategyBy(long id) {
		return benefitStrategyGroupDao.findById(id);
	}

	@Override
	public BenefitGroupStrategy getBenefitGroupStrategy(long strategyId, long groupId) {
		return benefitStrategyGroupDao.findByStrategyIdAndGroupId(strategyId, groupId);
	}

	@Override
	public List<BenefitGroupStrategy> getBenefitGroupStrategy(long strategyId, String status) {
		return benefitStrategyGroupDao.findByStrategyIdAndStatus(strategyId, status);
	}

	@Override
	public BenefitGroupStrategy saveBenefitGroupStrategy(BenefitGroupStrategy benefitGroupStrategy) {
		return benefitStrategyGroupDao.save(benefitGroupStrategy);
	}

	@Override
	public List<BenefitGroupStrategy> saveBenefitGroupStrategies(List<BenefitGroupStrategy> benefitGroupStrategies) {
		return benefitStrategyGroupDao.saveAll(benefitGroupStrategies);
	}

	@Override
	public List<BenefitGroupStrategy> findBy(String companyCode) {
		return benefitStrategyGroupDao.findBy(companyCode);
	}

	@Override
	public BenefitGroupStrategy getBenefitGroupStrategyBy(long id, long strategyId) {
		return benefitStrategyGroupDao.findByIdAndStrategyId(id, strategyId);
	}

	@Override
	public void updateBenefitGroupStrategyStatus(BenefitGroupStrategy benefitGroupStrategy, String status) {
		if (benefitGroupStrategy != null) {
			benefitGroupStrategy.setStatus(status);
			benefitStrategyGroupDao.saveAndFlush(benefitGroupStrategy);
		}
	}
}
