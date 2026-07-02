/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.Date;

import com.trinet.ambis.persistence.dao.hrp.impl.StrategyPreLoadDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.StrategyPreLoadService;

/**
 * @author rvutukuri
 *
 */
public class StrategyPreLoadServiceImpl implements StrategyPreLoadService {

	private StrategyPreLoadDaoImpl strategyPreLoadDao = new StrategyPreLoadDaoImpl();

	@Override
	public void preLoadBssStrategies(String peoId, String quarter, Long relamYearId, Date payrollCutOffDate,
			String emplid) {
		strategyPreLoadDao.createEntityManager();
		strategyPreLoadDao.preLoadBssStrategies(peoId, quarter, relamYearId, payrollCutOffDate, emplid);
	}

	@Override
	public void preLoadClientStrategies(Company company) {
		strategyPreLoadDao.createEntityManager();
		strategyPreLoadDao.preLoadClientStrategies(company);
	}
}
