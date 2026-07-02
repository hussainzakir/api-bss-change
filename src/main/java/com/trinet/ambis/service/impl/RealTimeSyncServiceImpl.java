package com.trinet.ambis.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.RealTimeSyncServiceStatusEnum;
import com.trinet.ambis.persistence.dao.hrp.RealTimeSyncDao;
import com.trinet.ambis.persistence.dao.hrp.dto.CensusHcSyncEventDto;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.RealTimeSyncService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RealTimeSyncServiceImpl implements RealTimeSyncService {

	@Autowired
	private RealTimeSyncDao realTimeSyncDao;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private StrategySyncService strategySyncService;

	@Value("${RealTimeSyncWaitTime}")
	private long realTimeSyncWaitTime;

	@Autowired
	private EmailGenService emailGenService;

	@Autowired
	private Executor executor;

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	private static final String REAL_TIME_SYNC_LOGGER_MESSAGE = "Real Time Sync Process Status is %s for event %s";

	/**
	 * Sync method to sync company census and strategy sync based on census hc sync
	 * events
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public RealTimeSyncServiceStatusEnum eventDrivenSync(Optional<String> companyCode) {
		return eventDrivenSyncProcess(companyCode);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public void onDemandSync(Company company) {
		boolean isRealTimeSyncEnabled = AppRulesAndConfigsUtils.isEventDrivenSyncEnabled();
		boolean isSyncFinished = false;
		boolean isBandOrAleUpdateSyncRequired = company.isBandCodeUpdated() || company.isAcaLargeEmplrStatusUpdated();
		if (isRealTimeSyncEnabled) {
			while (!isSyncFinished) {
				List<ProcessStatus> inProgressEvents = processStatusService
						.findInProgressCenusHcSyncEvent(company.getCode());
				if (!CollectionUtils.sizeIsEmpty(inProgressEvents)) {
					LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(realTimeSyncWaitTime));
					isBandOrAleUpdateSyncRequired = false;
					continue;
				}
				RealTimeSyncServiceStatusEnum realTimeSyncServiceStatusEnum = eventDrivenSyncProcess(
						Optional.ofNullable(company.getCode()));
				if (RealTimeSyncServiceStatusEnum.STATUS_PROCESSED == realTimeSyncServiceStatusEnum
						|| RealTimeSyncServiceStatusEnum.STATUS_FAILED == realTimeSyncServiceStatusEnum) {
					isBandOrAleUpdateSyncRequired = false;
				}
				isSyncFinished = true;
			}
		}
		if (isBandOrAleUpdateSyncRequired || !isRealTimeSyncEnabled) {
			syncAll(company.getCode());
		}
	}

	/**
	 * Created this method to solve @Transactional sonar qube issue. Refer
	 * BNFT-42115.
	 **/
	private RealTimeSyncServiceStatusEnum eventDrivenSyncProcess(Optional<String> companyCode) {
		// get census hc sync event
		Optional<CensusHcSyncEventDto> censusHqSyncEventDtoOpt = realTimeSyncDao
				.findNewCensusHcSyncEventAndUpdateToInProgress(companyCode);
		if (censusHqSyncEventDtoOpt.isPresent()) {
			CensusHcSyncEventDto censusHqSyncEventDto = censusHqSyncEventDtoOpt.get();
			Set<Long> processStatusIds = censusHqSyncEventDto.getProcessStatusIds();
			try {
				censusHqSyncEventDto
						.setTermedCompany(companyService.isTermedCompany(censusHqSyncEventDto.getCompanyCode()));
				log.error(String.format(REAL_TIME_SYNC_LOGGER_MESSAGE, "in progress", censusHqSyncEventDto));
				if (!censusHqSyncEventDto.isTermedCompany()) {
					syncAll(censusHqSyncEventDto.getCompanyCode());
				}
				uppdateProcessStatus(BSSApplicationConstants.PROCESS_STATUS_PROCESSED, censusHqSyncEventDto.getCompanyCode(), processStatusIds);
				log.error(String.format(REAL_TIME_SYNC_LOGGER_MESSAGE, "processed", censusHqSyncEventDto));
			} catch (Exception ex) {
				UUID logEyeCatcher = UUID.randomUUID();
				CompletableFuture.runAsync(() -> emailGenService
						.createSyncFailureEmail(censusHqSyncEventDto.getCompanyCode(), logEyeCatcher), executor);

				uppdateProcessStatus(BSSApplicationConstants.PROCESS_STATUS_FAILED, censusHqSyncEventDto.getCompanyCode(), processStatusIds);
				log.error("Log identifier: {}", logEyeCatcher.toString());
				log.error(String.format(REAL_TIME_SYNC_LOGGER_MESSAGE, "failed", censusHqSyncEventDto));
				CommonUtils.logExceptions(ex, log, censusHqSyncEventDto.getCompanyCode(), "");
				return RealTimeSyncServiceStatusEnum.STATUS_FAILED;
			}
			return RealTimeSyncServiceStatusEnum.STATUS_PROCESSED;
		}
		return RealTimeSyncServiceStatusEnum.STATUS_NORECORDS;
	}

	private void uppdateProcessStatus(String processStatusProcessed,  String companyCode, Set<Long> processStatusIds) {
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setProcessStatus(processStatusProcessed);
		processStatus.setProcessIdentifer(companyCode);
		processStatus.setProcessName("CENSUS_HC_SYNC");

		// update process status as processed
		processStatusService.updateProcessStatus(processStatusIds,
				processStatus);
	}

	private void syncAll(String companyCode) {
		log.error("syncAll started for companyCode = " + companyCode);
		Company companyDetails = companyService.getCompanyDetails(companyCode);
		// sync company census
		companyService.refreshCompanyCensusSynchronously(companyDetails.getCode(), companyDetails.getRealmPlanYearId());
		// sync strategies
		strategySyncService.syncStrategyData(companyDetails, null);
		log.error("syncAll completed for companyCode = " + companyCode);
	}

}
