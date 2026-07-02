package com.trinet.ambis.configuration;

import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class SubmitSchedulerTest extends ServiceUnitTest {
	@Spy
	private SubmitScheduler submitScheduler;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Ignore
	@Test
	public void jobRuns() {
		Awaitility.await().atMost(20000L, TimeUnit.MILLISECONDS)
				.untilAsserted(() -> verify(submitScheduler, Mockito.atLeastOnce()).runSubmitProcess());
	}
}
