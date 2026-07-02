/**
 * 
 */
package com.trinet.ambis.service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyData;

/**
 * This class facilitates the Submit and Resubmit requests to be process one at
 * a time in FIFO manner. It uses XBSS_PROCESS_STATUS table to store all the
 * requests to be processed.
 * 
 * @author schaudhari
 *
 */
public interface QueuedSubmitService {

	/**
	 * This method creates new record in XBSS_PROCESS_STATUS to be processed in FIFO
	 * manner.
	 *
	 * @param company
	 * @param strategyData
	 * @param processName
	 * @param sendEmail
	 */
	void createSubmitProcess(Company company, StrategyData strategyData, String processName, boolean sendEmail);

	/**
	 * This method creates new record in XBSS_PROCESS_STATUS to be processed in FIFO
	 * manner.
	 *
	 * @param company
	 * @param sendEmail
	 * @param userId
	 */
	void createAsyncDefaultSubmitProcess(final String companyCode, String userId);

	/**
	 * This method runs the submit for next record to be processed in queue for
	 * submit or resubmit in FIFO manner.
	 */
	void startSchedulerSubmitProcess();

	/**
	 * This method runs the submit for all the pending records in queue for submit
	 * or resubmit in FIFO manner.
	 */
	void startAsyncManualSubmitProcess();

}
