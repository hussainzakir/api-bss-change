package com.trinet.ambis.service.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.util.CommonUtils;

public class DefaultSubmitExecutorService {
	private List<String> companies;
	private String userId;
	private boolean isSingleClientSubmit;
	private long realmPlanYearId;

	private static final Logger logger = LoggerFactory.getLogger(DefaultSubmitExecutorService.class);

	public DefaultSubmitExecutorService(List<String> companies, boolean isSingleClientSubmit, String userId, long realmPlanYearId) {
		this.companies = companies;
		this.isSingleClientSubmit = isSingleClientSubmit;
		this.userId = userId;
		this.realmPlanYearId=realmPlanYearId;
	}

	public void start() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		try {
			Thread t = new Thread(new DefaultSubmitTask(companies, isSingleClientSubmit, userId, realmPlanYearId));
			t.start();
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		} finally {
			executorService.shutdown();
		}

	}
}
