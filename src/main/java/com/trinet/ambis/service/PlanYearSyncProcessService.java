package com.trinet.ambis.service;

import com.trinet.ambis.service.model.PlanYearRequest;

/**
 * @author mcshaik
 *
 */
public interface PlanYearSyncProcessService {

	/**
	 *
	 * @param PlanYearRequest
	 * @return
	 */
	void updatePlanYearSyncProcessStatus(final PlanYearRequest planYearRequest);
}
