package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.ProcessStatus;

/**
 * @author rvutukuri
 *
 */
@Repository
@Transactional(readOnly = true)
public interface ProcessStatusDao extends JpaRepository<ProcessStatus, Long> {

	/**
	 * 
	 * @param companyCode
	 * @return
	 */
	@Query("Select ps from ProcessStatus ps where ps.processName='STRATEGY_CREATE' and ps.processIdentifer='COMPANY_CODE' and ps.processStatus = 'I' and ps.processIdentiferValue=?1 and ps.createTime > trunc(sysdate-1)")

	ProcessStatus findStrateyCreateStatus(String processIdentiferValue);

	/**
	 * 
	 * @param companyCode
	 * @return
	 */
	@Query("Select ps from ProcessStatus ps where ps.processName='PRE_LOAD' and ps.processIdentifer='QUARTER' and ps.processStatus = 'I' and ps.processIdentiferValue=?1 and ps.createTime > trunc(sysdate-1)")
	ProcessStatus findPreLoadStatus(String processIdentiferValue);

	/**
	 * @param processName
	 * @param status
	 * @return
	 */
	ProcessStatus findTop1ByProcessNameInAndProcessStatusInOrderByCreateTimeAsc(Set<String> processName,
			Set<String> status);
	
	/**
	 * @param processName
	 * @param status
	 * @return
	 */
	List<ProcessStatus> findByProcessNameInAndProcessStatusInOrderByCreateTimeAsc(Set<String> processName,
			Set<String> status);
	/**
	 * @param processName
	 * @param status
	 * @param processIdentifierValue
	 * @return
	 */
	List<ProcessStatus> findByProcessNameInAndProcessStatusInAndProcessIdentiferValueOrderByCreateTimeAsc(Set<String> processName,
	                                                                                                      Set<String> status, String processIdentifierValue);
	/**
	 * This method returns all the ProcessStatus for given companyCode, statuses and processNames
	 * 
	 * @param companyCode
	 * @param status
	 * @param processNames
	 * @return
	 */
	@Query("SELECT ps FROM ProcessStatus ps WHERE ps.processIdentiferValue IN ("
			+ " SELECT ss.confirmationNumber FROM SubmitStatus ss WHERE ss.company = :companyCode )"
			+ "	AND ps.processStatus IN (:status) AND ps.processName IN (:processNames) ")
	List<ProcessStatus> findPendingSubmitProcessBy(@Param("companyCode") String companyCode,
			@Param("status") Set<String> status, @Param("processNames") Set<String> processNames);

	@Query("select ps from ProcessStatus ps where ps.processName= :processName and ps.createTime > :dateAfter ")
	List<ProcessStatus> findLatestBy(@Param("dateAfter") java.util.Date dateAfter,
			@Param("processName") String processName);
	
	@Query("SELECT ps FROM ProcessStatus ps WHERE ps.processIdentiferValue = :confirmationNumber")
	ProcessStatus findByConfirmationNumber(@Param("confirmationNumber") String confirmationNumber);
	
	/**
	 * Returns new census hc sync events in FIFO order <br>
	 * Events are returned for a single company <br>
	 * FOR UPDATE is used in the below query to lock the selected rows <br>
	 * SKIP RECORD is used in the below query to skip the locked records <br>
	 * (since real time sync scheduler running on multiple instances
	 * 
	 * @return list of events for a company
	 */
	@Query(value = "select * from  xbss_process_status ps where ps.P_IDENTIFER_VALUE = ( select P_IDENTIFER_VALUE from (select ps1.P_IDENTIFER_VALUE from xbss_process_status ps1 where ps1.p_status_code = 'N' and ps1.p_name = 'CENSUS_HC_SYNC' and ps1.p_identifer = 'COMPANY_CODE' and ps1.effDt <= sysdate order by ps1.create_time asc) where rownum=1)  and ps.p_status_code = 'N' and ps.p_name = 'CENSUS_HC_SYNC' and ps.p_identifer = 'COMPANY_CODE' and ps.effDt <= sysdate FOR UPDATE SKIP LOCKED", nativeQuery = true)
	List<ProcessStatus> findNewCenusHcSyncEvent();

	/**
	 * Scans process-status table for the next PROSPECT_BAND_UPDATE_EVENT waiting to be processed for
	 * a prospect that does not already have events in-progress.
	 * 
	 * @return list of all "New" events for a company
	 */
	@Query(value =
			"WITH NEXT_COMPANY_TO_PROCESS AS ( "
					+ "SELECT NI.P_IDENTIFER_VALUE "
					+ "  FROM XBSS_PROCESS_STATUS NI "
					+ " WHERE NI.P_NAME IN (:processNames) "
					+ "   AND NI.P_IDENTIFER = 'PROSPECT_ID' "
					+ "   AND NI.P_STATUS_CODE = 'N' "
					+ "   AND NOT EXISTS ( "
					+ "       SELECT 'X' "
					+ "         FROM XBSS_PROCESS_STATUS S1 "
					+ "        WHERE S1.P_NAME = NI.P_NAME "
					+ "          AND S1.P_IDENTIFER = NI.P_IDENTIFER "
					+ "          AND S1.P_IDENTIFER_VALUE = NI.P_IDENTIFER_VALUE "
					+ "          AND S1.P_STATUS_CODE = 'I' ) "
					+ "ORDER BY NI.CREATE_TIME ASC "
					+ "FETCH FIRST 1 ROWS ONLY "
					+ ") "
					+ "SELECT ST.* "
					+ "  FROM XBSS_PROCESS_STATUS ST "
					+ " WHERE ST.P_IDENTIFER_VALUE = ( "
					+ "       SELECT P_IDENTIFER_VALUE "
					+ "         FROM NEXT_COMPANY_TO_PROCESS ) "
					+ "   AND ST.P_NAME IN (:processNames) "
					+ "   AND ST.P_STATUS_CODE = 'N' "
					+ "ORDER BY ST.CREATE_TIME DESC "
					+ "FOR UPDATE", nativeQuery = true)
	List<ProcessStatus> findNextStrategySyncEvent(@Param("processNames") List<String> processNames);

	/**
	 * Updates process status for given list of ids.
	 * 
	 * @param ids
	 * @param processStatus
	 */
	@Modifying
	@Query("update ProcessStatus ps set ps.processStatus = :processStatus WHERE ps.id in (:ids)")
	void updateProcessStatus(@Param("ids") Set<Long> ids, @Param("processStatus") String processStatus);
	
	/**
	 * Returns new census hc sync events for a given company <br>
	 * 
	 * @return list of events for a company
	 */
	@Query(value = "select * from xbss_process_status ps where ps.p_identifer_value =:companyCode and ps.p_status_code = 'N' and ps.p_name = 'CENSUS_HC_SYNC' and ps.p_identifer = 'COMPANY_CODE' and ps.effDt <= sysdate FOR UPDATE SKIP LOCKED order by ps.create_time asc", nativeQuery = true)
	List<ProcessStatus> findNewCenusHcSyncEvent(@Param("companyCode") String companyCode);
	
	/**
	 * Returns in progress census hc sync events for a given company <br>
	 * 
	 * @return list of events for a company
	 */
	@Query(value = "select * from xbss_process_status ps where ps.p_identifer_value =:companyCode and ps.p_status_code = 'I' and ps.p_name = 'CENSUS_HC_SYNC' and ps.p_identifer = 'COMPANY_CODE' and ps.effDt <= sysdate order by ps.create_time asc", nativeQuery = true)
	List<ProcessStatus> findInProgressCenusHcSyncEvent(@Param("companyCode") String companyCode);
	
	/**
	 * Retrieve process statuses in I, N, F statuses list based on provided process names, identifier, identifier value
	 * 
	 * @param processIdentifier
	 * @param processIdentifierValue
	 * @param processNames
	 * @return
	 */
	@Query("SELECT ps FROM ProcessStatus ps "
			+ " WHERE ps.processName IN (:processNames) AND ps.processIdentifer= :processIdentifier "
			+ " AND ps.processIdentiferValue= :processIdentifierValue "
			+ " AND ( ps.processStatus IN ( 'I','N') "
			+ " 	OR ( ps.processStatus = 'F' AND ps.createTime = "
			+ "       	(SELECT MAX(ps2.createTime) FROM ProcessStatus ps2 "
			+ "       		WHERE ps2.processName IN ( :processNames)"
			+ "       		AND ps2.processIdentifer = :processIdentifier "
			+ "      		 AND ps2.processIdentiferValue = :processIdentifierValue )))")
	List<ProcessStatus> findNewOrInProgessOrFailedStrategySyncEvents(
			@Param("processIdentifier") String processIdentifier,
			@Param("processIdentifierValue") String processIdentifierValue,
			@Param("processNames") List<String> processNames);


	/**
	 * @param processName
	 * @param processIdentiferValue - companyId
	 * @return List of process status records in descending order by create time
	 */
	List<ProcessStatus> findByProcessNameAndProcessIdentiferValueOrderByCreateTimeDesc(String processName,
																				  String processIdentiferValue);

	
}
