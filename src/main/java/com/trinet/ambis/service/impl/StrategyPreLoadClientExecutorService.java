
package com.trinet.ambis.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.util.CommonUtils;

/**
 * @author rvutukuri
 *
 */
public class StrategyPreLoadClientExecutorService {

	private Company company;

	private static final Logger logger = LoggerFactory.getLogger(StrategyPreLoadExecutorService.class);

	public StrategyPreLoadClientExecutorService(Company company) {
		this.company = company;

	}

	public void start() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		try {
			Thread t = new Thread(new StrategyPreLoadClientTask(company));
			t.start();
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		} finally {
			executorService.shutdown();
		}
	}
}
