/**
 * 
 */

/**
 * 
 */
package com.trinet.ambis.service.impl;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.StrategyPreLoadService;

/**
 * @author rvutukuri
 *
 */
public class StrategyPreLoadClientTask implements Runnable {

	private StrategyPreLoadService strategyPreLoadService = new StrategyPreLoadServiceImpl();
	private Company company;

	public StrategyPreLoadClientTask(Company company) {
		this.company = company;

	}

	@Override
	public void run() {
		strategyPreLoadService.preLoadClientStrategies(company);
	}

	public StrategyPreLoadService getStrategyPreLoadService() {
		return strategyPreLoadService;
	}

	public void setStrategyPreLoadService(StrategyPreLoadService strategyPreLoadService) {
		this.strategyPreLoadService = strategyPreLoadService;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

}
