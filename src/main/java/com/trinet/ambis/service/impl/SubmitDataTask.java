package com.trinet.ambis.service.impl;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.PsSubmitDataService;
import com.trinet.ambis.service.model.StrategyData;

import lombok.ToString;

@ToString
public class SubmitDataTask implements Runnable {

	private PsSubmitDataService submitDataService = new PsSubmitDataServiceImpl();

	private Company company;
	private StrategyData strategy;
	private String userId;
	private boolean emailFlag;
	private String uniqueTrxId;
	private boolean resubmitFlag;

	public SubmitDataTask(Company company, StrategyData strategy, String userId, boolean emailFlag, String uniqueTrxId,
			boolean resubmitFlag) {
		this.company = company;
		this.strategy = strategy;
		this.userId = userId;
		this.emailFlag = emailFlag;
		this.uniqueTrxId = uniqueTrxId;
		this.resubmitFlag = resubmitFlag;
	}

	@Override
	public void run() {
		submitDataService.submitData(company, strategy, userId, emailFlag, uniqueTrxId, resubmitFlag);
	}

	public boolean isEmailFlag() {
		return emailFlag;
	}

	public void setEmailFlag(boolean emailFlag) {
		this.emailFlag = emailFlag;
	}

	public Company getCompny() {
		return this.company;
	}

	public void setComapny(Company company) {
		this.company = company;
	}

	public StrategyData getStrategy() {
		return this.strategy;
	}

	public void setStrategy(StrategyData strategy) {
		this.strategy = strategy;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUniqueTrxId() {
		return uniqueTrxId;
	}

	public void setUniqueTrxId(String uniqueTrxId) {
		this.uniqueTrxId = uniqueTrxId;
	}

}
