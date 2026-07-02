package com.trinet.ambis.configuration;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trinet.ambis.service.RealTimeSyncService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
public class RealTimeSyncSchedulerConfig {

	private final RealTimeSyncService realTimeSyncService;

	private static final String REAL_TIME_SYNC_SCHEDULER_LOGGER_MESSAGE = "Real Time Sync Scheduler %s";

	@Scheduled(fixedDelayString = "#{${RealTimeSyncInterval} * 1000L}")
	public void run() {
		/*
		 * Added try-catch block to catch any unexpected exceptions and move on to next
		 * invocation of the scheduler.
		 */
		try {
			if (!AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()) {
				return;
			}
			realTimeSyncService.eventDrivenSync(Optional.empty());
		} catch (Exception ex) {
			log.error(String.format(REAL_TIME_SYNC_SCHEDULER_LOGGER_MESSAGE, "exception occured."));
			CommonUtils.logExceptions(ex, log, "001", "");
		}
	}

}
