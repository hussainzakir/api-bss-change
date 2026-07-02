package com.trinet.ambis.service;

import java.util.List;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PreLoadStrategiesStatusDto;
import com.trinet.ambis.persistence.model.ProcessStatus;

/**
 * @author rvutukuri
 *
 */
public interface ProcessStatusService {

	/**
	 * Save all entities.  Passes directly to dao.saveAndFlush
	 * 
	 * @param processStatus
	 * @return
	 */
	void saveAll(List<ProcessStatus> processStatus);

	/**
	 * 
	 * @param company
	 * @return
	 */
	boolean isStrategySummariesProcessed(String companyCode);

	/**
	 * 
	 * @param company
	 */
	ProcessStatus createStrategyProcess(Company company);

	/**
	 * 
	 * @param company
	 */
	ProcessStatus updateProcessStatus(ProcessStatus ps);
	/**
	 * 
	 * @param quarter
	 * @return
	 */
	boolean isPreLoadProcessed(String quarter);

	/**
	 * 
	 * @param quarter
	 * @param emplId
	 * @return
	 */
	ProcessStatus createPreLoadProcess(String quarter, String emplId);
	
	/**
	 * This method creates a new process for strategy submit and resubmit request.
	 * 
	 * @param processName
	 * @param confirmationNumber
	 * @param emplId
	 */
	void createSubmitProcess(String processName, String confirmationId, String emplId);

	/**
	 * Insert a transaction to PROCESS_STATUS table to plan year sync and band
	 * update events.
	 * 
	 * @param companyCode
	 * @param processData
	 * @param processName
	 * @param identifierName
	 */
	void createStrategySyncProcess(String companyCode, String processData, String processName, String identifierName);

	/**
	 * This method returns the next Submit/Resubmit process in the queue from XBSS_PROCESS_STATUS table
	 * to be processed.
	 * 
	 * @return {@link ProcessStatus}
	 */
	ProcessStatus findNextToProcessSubmit();

	/**
	 * This method returns the next prospect strategy sync process in the queue from
	 * XBSS_PROCESS_STATUS table.
	 * 
	 * @param processNames
	 * @return {@link ProcessStatus}
	 */
	List<ProcessStatus> findNextStrategySyncProcess(List<String> processNames);
	
	/**
	 * This method returns all the Submit/Resubmit ProcessStatus for given company which are in I (In-process) or 
	 * N (new) status.
	 * 
	 * @param companyCode
	 * @return List<ProcessStatus>
	 */
	List<ProcessStatus> findPendingSubmitProcessBy(String companyCode);
	
	/**
	 * This method returns all the Submit/Resubmit ProcessStatus for given company which are in  
	 * N (new) status.
	 * 
	 * @return List<ProcessStatus>
	 */
	List<ProcessStatus> findPendingSubmitProcesses();


	List<PreLoadStrategiesStatusDto> getPreLoadStrategiesStatuses();

	ProcessStatus findByConfirmationNumber(String confirmationNumber);
	
	/**
	 * Updates process status for given list of ids.
	 * 
	 * @param ids
	 * @param processStatus
	 */
	void updateProcessStatus(Set<Long> ids, ProcessStatus processStatus);

	/**
	 * Returns new census hc sync events in FIFO order <br>
	 * Events are returned for a single company <br>
	 * 
	 * @return list of events for a company
	 */
	List<ProcessStatus> findNewCenusHcSyncEvent();
	
	/**
	 * Returns new census hc sync events for given company <br>
	 * 
	 * @return list of events for a company
	 */
	List<ProcessStatus> findNewCenusHcSyncEvent(String companyCode);

	/**
	 * Returns in progress census hc sync events for given company <br>
	 * 
	 * @return list of events for a company
	 */
	List<ProcessStatus> findInProgressCenusHcSyncEvent(String companyCode);

	/**
	 * @param companyId
	 * @param event
	 * @return The first record in the list of process status records in descending order by create time
	 */
	ProcessStatus findLastRecordByCompanyAndEvent(long companyId, String event);
	
	/**
	 * 
	 * @param companyCode
	 * @return
	 */
	String findStrategySyncProcessStatus(String companyCode);

	/**
	 *
	 * @param companyCode
	 * @param newQuater
	 * @return
	 */
	String findPendingOrInProgressPSQuarterChangeProcessStatus(String companyCode, String newQuater);


	void createBandUpdateProcess(Long exchangeId, String companyCode, Long companyId);

	/**
	 * Finds pending QUARTER_CHANGE records with status 'N' for the given process identifier value (company code).
	 * Uses a direct query by processName, status, and identifierValue — suitable for QUARTER_CHANGE records
	 * whose identifier is COMPANY_CODE (not a SubmitStatus confirmation number).
	 *
	 * @param processIdentifierValue the company code to search for
	 * @return list of ProcessStatus records matching the criteria, ordered by create time ascending
	 */
	List<ProcessStatus> findPendingQuarterChangeProcesses(String processIdentifierValue);

	/**
	 * Calls BSS Core service to check if all bundle selection processes for the given
	 * company are COMPLETED.
	 *
	 * @param companyCode the company code to query
	 * @return {@code true} when all process records are COMPLETED (safe to proceed);
	 *         {@code false} when any record is not COMPLETED (gating should block);
	 *         defaults to {@code true} (fail-open) if BSS Core is unavailable
	 */
	boolean findBssCoreProcessStatus(String companyCode);
}
