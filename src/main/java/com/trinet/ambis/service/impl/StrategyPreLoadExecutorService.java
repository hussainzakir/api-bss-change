/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.util.CommonUtils;

/**
 * @author rvutukuri
 *
 */
public class StrategyPreLoadExecutorService {

	private String peoId;
	private String quarter;
	private Long relamYearId;
	private Date payrollCutOffDate;
	private String emplid;

	private static final Logger logger = LoggerFactory.getLogger(StrategyPreLoadExecutorService.class);

	public StrategyPreLoadExecutorService(String peoId, String quarter, Long relamYearId, Date payrollCutOffDate,
			String emplid) {
		this.peoId = peoId;
		this.quarter = quarter;
		this.relamYearId = relamYearId;
		this.payrollCutOffDate = payrollCutOffDate;
		this.emplid = emplid;
	}

	public void start() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		try {
			Thread t = new Thread(new StrategyPreLoadTask(peoId, quarter, relamYearId, payrollCutOffDate, emplid));
			t.start();
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		} finally {
			executorService.shutdown();
		}
	}
}
