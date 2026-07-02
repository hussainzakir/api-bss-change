package com.trinet.ambis.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * @author schaudhari
 * 
 *         This class will run a scheduler which every 10 seconds to process the
 *         submit/resubmit which is next in the queue (in XBSS_PROCESS_STATUS
 *         table).
 */
@Profile("!test")
@Component
@EnableSchedulerLock(defaultLockAtMostFor = "PT32M", defaultLockAtLeastFor = "PT2S")
public class SubmitScheduler {

	private static final long FIXED_DELAY = 10000L;

	@Autowired
	private QueuedSubmitService queuedSubmitService;

	@Autowired
	AppRulesConfigService appRulesConfigService;

	@Scheduled(fixedDelay = FIXED_DELAY)
	@SchedulerLock(name = "BSS_TASK_SCHEDULER_SUBMIT_PROCESS")
	public void runSubmitProcess() {
		if (!AppRulesAndConfigsUtils.isSubmitQueuingEnabled()) {
			return;
		}
		LockAssert.assertLocked();
		queuedSubmitService.startSchedulerSubmitProcess();
	}

}
