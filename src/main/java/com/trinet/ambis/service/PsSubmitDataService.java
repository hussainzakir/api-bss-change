package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyData;

public interface PsSubmitDataService {
	/**
	 * @param company
	 * @param stategy
	 * @param userId
	 * @param emailFlag
	 * @param uniqueTrxId
	 * @param resubmitFlag
	 */
	public void submitData(Company company, StrategyData stategy, String userId, boolean emailFlag, String uniqueTrxId,
			boolean resubmitFlag);

	/**
	 * This method is for submitting all the un submitted clients for an
	 * exchange/PEO
	 * 
	 * @param companies
	 * @param isSingleClientSubmit
	 * @param userId
	 */
	public void defaultSubmit(List<String> companies, boolean isSingleClientSubmit, String userId, long realmPlanYearId);

}
