package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.rest.controllers.dto.ProcessFailureEmailDto;
import com.trinet.ambis.service.dto.ProcessInfoDto;
import com.trinet.ambis.service.dto.QuarterChangeProcessInfoDTO;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.util.JsonConverterUtils;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.ProcessStatusDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PreLoadStrategiesStatusDto;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.model.bsscore.BssCoreProcessStatusResponse;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.CommonUtils;

/**
 * @author rvutukuri
 *
 */
@Service
public class ProcessStatusServiceImpl implements ProcessStatusService {
	private static final Logger log = LoggerFactory.getLogger(ProcessStatusServiceImpl.class);
	
	@Autowired
	ProcessStatusDao processStatusDao;

	@Autowired
	BssCoreServiceClient bssCoreServiceClient;
	@Autowired
	EmailGenService emailGenService;
	@Override
	public void saveAll(List<ProcessStatus> processStatusList) {
		for(ProcessStatus status: processStatusList) {
			notifyBssDevTeamOnProcessFailure(status, null);
		}
		processStatusDao.saveAllAndFlush(processStatusList);
	}

	@Override
	public boolean isStrategySummariesProcessed(String companyCode) {
		boolean processStatusFlag = false;
		ProcessStatus processStatus = processStatusDao.findStrateyCreateStatus(companyCode);
		if (null == processStatus) {
			processStatusFlag = true;
		}
		return processStatusFlag;
	}

	@Override
	public boolean isPreLoadProcessed(String quarter) {
		boolean processStatusFlag = false;
		ProcessStatus processStatus = processStatusDao.findPreLoadStatus(quarter);
		if (null == processStatus) {
			processStatusFlag = true;
		}
		return processStatusFlag;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ProcessStatus createStrategyProcess(Company company) {
		ProcessStatus ps = new ProcessStatus();
		ps.setProcessName(ProcessStatusEnum.STRATEGY_CREATE_PROCESS.getProcessName());
		ps.setUserId(company.getEmplId());
		ps.setProcessIdentifer(ProcessStatusEnum.STRATEGY_CREATE_PROCESS.getIdentifierName());
		ps.setProcessIdentiferValue(company.getCode());
		ps.setCreateTime(new Date());
		ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_INPROGRESS);
		ps = processStatusDao.saveAndFlush(ps);
		return ps;
	}

	@Override
	public ProcessStatus createPreLoadProcess(String quarter, String emplId) {
		ProcessStatus ps = new ProcessStatus();
		ps.setProcessName(ProcessStatusEnum.PRE_LOAD.getProcessName());
		ps.setUserId(emplId);
		ps.setProcessIdentifer(ProcessStatusEnum.PRE_LOAD.getIdentifierName());
		ps.setProcessIdentiferValue(quarter);
		ps.setCreateTime(new Date());
		ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_INPROGRESS);
		ps = processStatusDao.saveAndFlush(ps);
		return ps;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ProcessStatus updateProcessStatus(ProcessStatus ps) {
		notifyBssDevTeamOnProcessFailure(ps, null);
		ps = processStatusDao.saveAndFlush(ps);
		return ps;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void createSubmitProcess(String processName, String confirmationId, String emplId) {
		ProcessStatus ps = new ProcessStatus();
		ps.setProcessName(processName);
		ps.setUserId(emplId);
		ps.setProcessIdentifer(ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getIdentifierName());
		ps.setProcessIdentiferValue(confirmationId);
		ps.setCreateTime(new Date());
		ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_NEW);
		if(processName.equals(ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName())) {
			String confirmationNumber = CommonServiceHelper.randomAlphanumeric();
			ps.setProcessIdentiferValue(confirmationNumber);
			// In case of term default, storing company code in process data
			ps.setProcessData(confirmationId);
		}
		processStatusDao.saveAndFlush(ps);
	}

	@Override
	public void createStrategySyncProcess(String companyCode, String processData, String processName,
			String identifierName) {
		ProcessStatus ps = new ProcessStatus();
		ps.setProcessName(processName);
		ps.setProcessIdentifer(identifierName);
		ps.setProcessIdentiferValue(companyCode);
		ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_NEW);
		ps.setCreateTime(new Date());
		ps.setUserId("SYSTEM");
		ps.setProcessData(processData);
		ps.setEffDt(null);
		processStatusDao.saveAndFlush(ps);
	}

	@Override
	public ProcessStatus findNextToProcessSubmit() {
		Set<String> status = new HashSet<>(Arrays.asList(BSSApplicationConstants.PROCESS_STATUS_INPROGRESS,
				BSSApplicationConstants.PROCESS_STATUS_NEW));
		return processStatusDao.findTop1ByProcessNameInAndProcessStatusInOrderByCreateTimeAsc(
				BSSApplicationConstants.SUBMIT_PROCESS_NAMES, status);
	}

	@Override
	public List<ProcessStatus> findNextStrategySyncProcess(List<String> processNames) {
		return processStatusDao.findNextStrategySyncEvent(processNames);
	}

	@Override
	public List<ProcessStatus> findPendingSubmitProcessBy(String companyCode) {
		Set<String> status = new HashSet<>(Arrays.asList(BSSApplicationConstants.PROCESS_STATUS_NEW,
				BSSApplicationConstants.PROCESS_STATUS_INPROGRESS));
		return processStatusDao.findPendingSubmitProcessBy(companyCode, status,
				BSSApplicationConstants.SUBMIT_PROCESS_NAMES);
	}

	@Override
	public List<ProcessStatus> findPendingSubmitProcesses() {
		Set<String> status = new HashSet<>(Arrays.asList(BSSApplicationConstants.PROCESS_STATUS_NEW));
		return processStatusDao.findByProcessNameInAndProcessStatusInOrderByCreateTimeAsc(
				BSSApplicationConstants.SUBMIT_PROCESS_NAMES, status);
	}


	@Override
	public List<PreLoadStrategiesStatusDto> getPreLoadStrategiesStatuses() {
		String processName = ProcessStatusEnum.PRE_LOAD.getProcessName();
		List<PreLoadStrategiesStatusDto> listOfPreLoadStrategiesStatus = new ArrayList<>();
		PreLoadStrategiesStatusDto preLoadStrategiesStatusDto = null;
		Date payrollCutOffDate = new DateTime().minusDays(BSSApplicationConstants.PRELOAD_STATUS_LAST_30_DAYS).toDate();
		String date = CommonUtils.formatDateToString(payrollCutOffDate,
				BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY);
		List<ProcessStatus> listOfProcessStatus = processStatusDao.findLatestBy(
				CommonUtils.formatStringToDate(date, BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY), processName);
		for (ProcessStatus processStatus : listOfProcessStatus) {
			preLoadStrategiesStatusDto = PreLoadStrategiesStatusDto.builder()
					.preloadDate(CommonUtils.formatDateToString(processStatus.getCreateTime(),
							BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY))
					.status(processStatus.getProcessStatus()).type(processStatus.getProcessIdentifer())
					.userId(processStatus.getUserId()).value(processStatus.getProcessIdentiferValue()).build();

			listOfPreLoadStrategiesStatus.add(preLoadStrategiesStatusDto);
		}

		return listOfPreLoadStrategiesStatus;

	}
	
	public ProcessStatus findByConfirmationNumber(@Param("confirmationNumber") String confirmationNumber) {
		return processStatusDao.findByConfirmationNumber(confirmationNumber);
	}

	/**
	 * Updates process status for given list of ids in a separate transaction.
	 * 
	 * @param ids
	 * @param processStatus
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateProcessStatus(Set<Long> ids, ProcessStatus processStatus) {
		notifyBssDevTeamOnProcessFailure(processStatus, ids);
		processStatusDao.updateProcessStatus(ids, processStatus.getProcessStatus());
	}
	
	/**
	 * Returns new census hc sync events in FIFO order <br>
	 * Events are returned for a single company <br>
	 * 
	 * @return list of events for a company
	 */
	@Override
	public List<ProcessStatus> findNewCenusHcSyncEvent() {
		return processStatusDao.findNewCenusHcSyncEvent();
	}

	/**
	 * Returns new census hc sync events for given company <br>
	 * 
	 * @return list of events for a company
	 */
	@Override
	public List<ProcessStatus> findNewCenusHcSyncEvent(String companyCode) {
		return processStatusDao.findNewCenusHcSyncEvent(companyCode);
	}

	/**
	 * Returns in progress census hc sync events for a given company <br>
	 * 
	 * @return list of events for a company
	 */
	@Override
	public List<ProcessStatus> findInProgressCenusHcSyncEvent(String companyCode) {
		return processStatusDao.findInProgressCenusHcSyncEvent(companyCode);
	}

	@Override
	public ProcessStatus findLastRecordByCompanyAndEvent(long companyId, String event) {
		ProcessStatus returnStatus = null;
		List<ProcessStatus> processStatuses = processStatusDao.findByProcessNameAndProcessIdentiferValueOrderByCreateTimeDesc(event, String.valueOf(companyId));
		// Get the first record, else return null
		if (processStatuses != null && !processStatuses.isEmpty()) {
			returnStatus =  processStatuses.get(0);
		}
		return returnStatus;
	}
	
	@Override
	public String findStrategySyncProcessStatus(String companyCode) {
		String processStatusCode = "";
		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());
		List<ProcessStatus> processStatusList = processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.BAND_UPDATE_EVENT.getIdentifierName(), companyCode, processNames);
		processStatusList.addAll(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames));
		if (!CollectionUtils.isEmpty(processStatusList)) {
			Set<String> statuses = processStatusList.stream().map(ProcessStatus::getProcessStatus)
					.collect(Collectors.toSet());
			return Stream.of("N", "I", "F").filter(statuses::contains).findFirst().orElse("");
		}
		return processStatusCode;
	}

	@Override
	public String findPendingOrInProgressPSQuarterChangeProcessStatus(String companyCode, String newQuater) {
		String processStatusCode = "";
		List<String> processNames = Collections.singletonList(ProcessStatusEnum.QUARTER_CHANGE.getProcessName());
		List<ProcessStatus> processStatusList = processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		if (!CollectionUtils.isEmpty(processStatusList)) {
			Set<String> statuses = processStatusList.stream()
					.filter(ps -> {
						String processData = ps.getProcessData();
						if (processData == null) {
							return false;
						}
						try {
							QuarterChangeProcessInfoDTO processInfoDto = JsonConverterUtils.convertJsonToObject(
									processData, new TypeReference<QuarterChangeProcessInfoDTO>() {});
							return processInfoDto.getNewQuaterId().equals(newQuater);
						} catch (Exception e) {
							log.error("Error while converting process data to QuarterChangeProcessInfoDTO for process id={}", ps.getId(), e);
							return false;
						}
					})
					.map(ProcessStatus::getProcessStatus)
					.collect(Collectors.toSet());
			return Stream.of(BSSApplicationConstants.PROCESS_STATUS_NEW, BSSApplicationConstants.PROCESS_STATUS_INPROGRESS)
					.filter(statuses::contains).findFirst().orElse("");
		}
		return processStatusCode;
	}


	@Override
	public void createBandUpdateProcess(Long exchangeId, String companyCode, Long companyId) {
		ProcessInfoDto processInfoDto = ProcessInfoDto.builder().processName(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName())
				.exchangeId(exchangeId).oldCompanyId(companyId).build();

		createStrategySyncProcess(companyCode,
				JsonConverterUtils.convertObjectToJson(processInfoDto),
				ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.BAND_UPDATE_EVENT.getIdentifierName());
	}



	@Override
	public boolean findBssCoreProcessStatus(String companyCode) {
		try {
			BssCoreProcessStatusResponse response = bssCoreServiceClient.getProcessStatusesBy(companyCode);
			if (response == null || CollectionUtils.isEmpty(response.getProcessStatuses())) {
				// Fail-open: no processes found means bundles processes is not initialized.
                // Return true to skip the error in interceptor.
				return true;
			}
			boolean allCompleted = response.getProcessStatuses().stream()
					.allMatch(p -> "COMPLETED".equals(p.getStatus()));
			log.debug("BSS Core bundle process status for companyCode={}: allCompleted={}", companyCode, allCompleted);
			return allCompleted;
		} catch (HttpClientErrorException e) {
			log.error("BSS Core HTTP error for companyCode={}: {}", companyCode, e.getMessage());
			if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
				throw new BSSApplicationException(e, new BSSApplicationError(
						BSSErrorResponseCodes.BSS_CORE_AUTH_ERROR,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
						ProcessStatusServiceImpl.class.getName(),
						"BSS Core authorization failed. Please check service credentials and roles.",
						null, null));
			}
			throw e;
		}
	}


	private void notifyBssDevTeamOnProcessFailure(ProcessStatus processStatus, Set<Long> processStatusIds) {
		try {
			if (processStatus != null
					&& BSSApplicationConstants.PROCESS_STATUS_FAILED.equals(processStatus.getProcessStatus())) {

				SupportEmailDto dto = ProcessFailureEmailDto.builder().processName(processStatus.getProcessName())
						.companyCode(processStatus.getProcessIdentiferValue())
						.errorMessage(processStatus.getErrorMessage())
						.processStatusIds(processStatusIds != null ? processStatusIds : Set.of(processStatus.getId()))
						.userId(processStatus.getUserId()).sendToBSS(true).build();

				emailGenService.createSupportEmail(dto);
			}

		} catch (Exception e) {
			log.error("Failed to send process failure email for id={}", processStatus.getId(), e);
		}
	}

	@Override
	public List<ProcessStatus> findPendingQuarterChangeProcesses(String processIdentifierValue) {
		HashSet<String> processNames = new HashSet<>(Arrays.asList(ProcessStatusEnum.QUARTER_CHANGE.getProcessName()));
		Set<String> status = new HashSet<>(Arrays.asList(BSSApplicationConstants.PROCESS_STATUS_NEW));
		return processStatusDao.findByProcessNameInAndProcessStatusInAndProcessIdentiferValueOrderByCreateTimeAsc(
				processNames, status, processIdentifierValue);
	}

}
