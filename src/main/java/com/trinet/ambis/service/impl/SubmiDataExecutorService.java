package com.trinet.ambis.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.util.CommonUtils;

public class SubmiDataExecutorService {
	private Company company;
	private StrategyData strategy;
	private String userId;
	private boolean emailFlag;
	private String uniqueTrxId;
	private boolean resubmitFlag;
	
	private static final Logger logger = LoggerFactory.getLogger(SubmiDataExecutorService.class);

	public SubmiDataExecutorService(Company company, StrategyData strategy, String userId, boolean emailFlag,
			String uniqueTrxId, boolean resubmitFlag) {
		this.company = company;
		this.strategy = strategy;
		this.userId = userId;
		this.emailFlag = emailFlag;
		this.uniqueTrxId = uniqueTrxId;
		this.resubmitFlag = resubmitFlag;
	}

	public void start() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		try {
			Thread t = new Thread(new SubmitDataTask(company, strategy, userId, emailFlag, uniqueTrxId,
					resubmitFlag));
			t.start();
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		} finally {
			executorService.shutdown();
		}
	}
}
