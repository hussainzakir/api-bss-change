package com.trinet.ambis.service.impl;

import java.util.List;

import com.trinet.ambis.persistence.dao.ps.impl.PsSubmitDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.PsSubmitDataService;
import com.trinet.ambis.service.model.StrategyData;

public class PsSubmitDataServiceImpl implements PsSubmitDataService {

	private PsSubmitDataDaoImpl psSubmitDataDao = new PsSubmitDataDaoImpl();

	@Override
	public void submitData(Company company, StrategyData strategy, String userId, boolean emailFlag, String uniqueTrxId,
			boolean resubmitFlag) {
		psSubmitDataDao.createEntityManager();
		psSubmitDataDao.submitData(company, strategy, userId, emailFlag, uniqueTrxId, resubmitFlag, false, null);
	}

	@Override
	public void defaultSubmit(List<String> companies, boolean isSingleClientSubmit, String userId, long realmPlanYearId) {
		psSubmitDataDao.createEntityManager();
		psSubmitDataDao.defaultSubmit(companies, isSingleClientSubmit, userId, realmPlanYearId);
	}

}
