package com.trinet.ambis.service.impl;

import java.util.List;

import com.trinet.ambis.service.PsSubmitDataService;

public class DefaultSubmitTask implements Runnable {
	private PsSubmitDataService submitDataService = new PsSubmitDataServiceImpl();
	private List<String> companies;
	private boolean isSingleClientSubmit;
	private String userId;
	private long realmPlanYearId;

	public DefaultSubmitTask(List<String> companies, boolean isSingleClientSubmit, String userId, long realmPlanYearId) {
		this.companies = companies;
		this.isSingleClientSubmit = isSingleClientSubmit;
		this.userId = userId;
		this.realmPlanYearId=realmPlanYearId;
	}

	@Override
	public void run() {
		submitDataService.defaultSubmit(companies, isSingleClientSubmit, userId, realmPlanYearId);
	}

}
