package com.trinet.ambis.service.impl;

import static com.trinet.ambis.common.BSSApplicationConstants.PROCESS_STATUS_FAILED;
import static com.trinet.ambis.common.BSSApplicationConstants.PROCESS_STATUS_INPROGRESS;
import static com.trinet.ambis.common.BSSApplicationConstants.PROCESS_STATUS_PROCESSED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.QueuedStrategySyncService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.dto.ProcessInfoDto;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.JsonConverterUtils;

@Service
public class QueuedStrategySyncServiceImpl implements QueuedStrategySyncService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueuedStrategySyncServiceImpl.class);

	@Autowired
	private CompanyDao companyDao;

	@Autowired
	private Executor executor;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	private StrategySyncService strategySyncService;

	@Override
	public void startScheduledStrategySyncProcess() {
		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName());
		List<ProcessStatus> processStatus = processStatusService.findNextStrategySyncProcess(processNames);
		
		if (processStatus == null || processStatus.isEmpty()) {
			LOGGER.info("No process status to process.");
			return;
		}
		startStrategySyncProcess(processStatus);
	}

	private void startStrategySyncProcess(List<ProcessStatus> processStatus) {
		List<ProcessStatus> filteredEvents = filterProcessStatus(processStatus);
		setInProgressStatus(filteredEvents);
		ProcessStatus oneProcess = filteredEvents.get(0);
		try {
			Future<Void> futureProcess = startAsyncStrategySyncProcess(oneProcess);
			futureProcess.get(10, TimeUnit.MINUTES);
			final String companyCode = oneProcess.getProcessIdentiferValue();
			List<ProcessStatus> pendingQuarterChangeEvents =
					processStatusService.findPendingQuarterChangeProcesses(companyCode);
			if (pendingQuarterChangeEvents != null && !pendingQuarterChangeEvents.isEmpty()) {
				filteredEvents.addAll(pendingQuarterChangeEvents);
			}
			setProcessedStatus(filteredEvents);
		} catch (TimeoutException toe) {
			String message = String.format("Prospect strategy sync for company ID %s timed out", oneProcess.getProcessIdentiferValue());
			LOGGER.error(message, toe);
			setFailedStatus(filteredEvents, "Timed out");
		} catch (BSSApplicationException be) {
			String message = String.format("BSS Application error occured on prospect strategy sync for company ID %s", oneProcess.getProcessIdentiferValue());
			LOGGER.error(message, be);
			setFailedStatus(filteredEvents, be.getMessage());
		} catch (InterruptedException intEx) {
			String message = String.format("Thread interruption occured on prospect strategy sync for company ID %s", oneProcess.getProcessIdentiferValue());
			LOGGER.error(message, intEx);
			setFailedStatus(filteredEvents, intEx.getMessage());
			Thread.currentThread().interrupt();
		} catch (ExecutionException exEx) {
			String message = String.format("Execution exception occured on prospect strategy sync for company ID %s", oneProcess.getProcessIdentiferValue());
			LOGGER.error(message, exEx);
			setFailedStatus(filteredEvents, exEx.getCause().getMessage());
		} catch (Exception e) {
			String message = String.format("Unknown exception occured on prospect strategy sync for company ID %s", oneProcess.getProcessIdentiferValue());
			LOGGER.error(message, e);
			setFailedStatus(filteredEvents, e.getMessage());
		}
	}

	private List<ProcessStatus> filterProcessStatus(List<ProcessStatus> processStatus) {
		Optional<ProcessStatus> latestEvent = processStatus.stream()
				.max((e1, e2) -> e1.getCreateTime().compareTo(e2.getCreateTime()));
		return processStatus.stream().filter(e -> e.getProcessName().equals(latestEvent.get().getProcessName()))
				.collect(Collectors.toList());
	}

	private Future<Void> startAsyncStrategySyncProcess(ProcessStatus processStatus) {
		String processData = processStatus.getProcessData();
		final String companyCode = processStatus.getProcessIdentiferValue();
		ProcessInfoDto processInfoDto = JsonConverterUtils.convertJsonToObject(processData,
				new TypeReference<ProcessInfoDto>() {
				});

		final Company.ProcessInfo processInfo = Company.ProcessInfo.builder()
				.oldCompanyId(processInfoDto.getOldCompanyId()).oldRealmPlanYear(processInfoDto.getOldRealmPlanYear())
				.processName(processInfoDto.getProcessName()).build();

		final BenExchngEnums exchange = processInfoDto.getExchangeId() != null
				? BenExchngEnums.getById(processInfoDto.getExchangeId())
				: null;

		return CompletableFuture.runAsync(
				() -> strategySyncService.syncStrategiesForCompany(companyCode, exchange, processInfo), executor);
	}
	
	private void setProcessedStatus(List<ProcessStatus> processStatus) {
		setStatus(processStatus, PROCESS_STATUS_PROCESSED, null);
	}

	private void setInProgressStatus(List<ProcessStatus> processStatus) {
		setStatus(processStatus, PROCESS_STATUS_INPROGRESS, null);
	}

	private void setFailedStatus(List<ProcessStatus> processStatus, String errorMessage) {
		setStatus(processStatus, PROCESS_STATUS_FAILED, errorMessage);
	}

	private void setStatus(List<ProcessStatus> processStatus, String status, String message) {
		processStatus.stream().forEach(st -> {
			st.setProcessStatus(status);
			st.setErrorMessage(message);
		});
		processStatusService.saveAll(processStatus);
	}

}