
package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.dao.hrp.StrategyGroupHeadCountDao;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.service.StrategyGroupHeadCountService;

/**
 * @author hliddle
 *
 */
@Service
public class StrategyGroupHeadCountServiceImpl implements StrategyGroupHeadCountService {

	@Autowired
	StrategyGroupHeadCountDao strategyGroupHeadCountDao;

	@Override
	public List<StrategyGroupHeadCount> findStrategyGroupHeadCountBy(long strategyGroupId) {
		return strategyGroupHeadCountDao.findByIdStrategyGroupId(strategyGroupId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public StrategyGroupHeadCount saveStrategyGroupHeadCount(StrategyGroupHeadCount sghc) {
		return strategyGroupHeadCountDao.save(sghc);
	}

}
