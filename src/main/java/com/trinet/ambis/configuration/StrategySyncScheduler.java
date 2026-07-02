package com.trinet.ambis.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.QueuedStrategySyncService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * This class will run a scheduler to handle queued prospect band change/strategy sync requests.
 * Every 2 seconds, test the process-status table for queued band updates and launch a strategy
 * sync if a company is waiting to be processed.
 */
@Profile("!test")
@Component
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M30S")
public class StrategySyncScheduler {

	private static final long FIXED_DELAY = 2000L;

	@Autowired
	private QueuedStrategySyncService service;

	@Autowired
	AppRulesConfigService appRulesConfigService;

	@Scheduled(fixedDelay = FIXED_DELAY)
	@SchedulerLock(name = "BAND_UPDATE_AND_PLYR_CHANGE_EVENT", lockAtMostFor = "PT10M30S", lockAtLeastFor = "PT2S")
	public void runStrategySyncProcess() {
		if (!AppRulesAndConfigsUtils.isProspectStrategyQueuingEnabled()) {
			return;
		}
		LockAssert.assertLocked();
		service.startScheduledStrategySyncProcess();
	}

}
