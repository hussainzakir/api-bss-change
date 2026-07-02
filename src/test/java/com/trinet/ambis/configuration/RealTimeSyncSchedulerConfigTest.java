package com.trinet.ambis.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.RealTimeSyncService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class RealTimeSyncSchedulerConfigTest extends ServiceUnitTest {

	@InjectMocks
	private RealTimeSyncSchedulerConfig realTimeSyncSchedulerConfig;

	@Mock
	private RealTimeSyncService realTimeSyncService;

	private static final String EXCEPTION_RUN_METHOD = "Exception occurred in run method";

	private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
	private MockedStatic<CommonUtils> mockStaticCommonUtils;

	@Before
	public void setUp() {
		mockStaticAppRulesAndConfigsUtils = mockStatic(AppRulesAndConfigsUtils.class);
		mockStaticCommonUtils = mockStatic(CommonUtils.class);
	}

	@Test
	public void schedulerEnabledTest() throws ParseException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		// when
		realTimeSyncSchedulerConfig.run();
		// then
		verify(realTimeSyncService, times(1)).eventDrivenSync(Optional.empty());
	}

	@Test
	public void schedulerDisabledTest() throws ParseException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.FALSE);
		// when
		realTimeSyncSchedulerConfig.run();
		// then
		verify(realTimeSyncService, times(0)).eventDrivenSync(Optional.empty());
	}

	@Test
	public void schedulerExceptionTest() throws ParseException {
		// given
		when(AppRulesAndConfigsUtils.isEventDrivenSyncEnabled()).thenReturn(Boolean.TRUE);
		doThrow(new RuntimeException(EXCEPTION_RUN_METHOD)).when(realTimeSyncService).eventDrivenSync(Optional.empty());
		// when
		realTimeSyncSchedulerConfig.run();
		// then
		verify(realTimeSyncService, times(1)).eventDrivenSync(Optional.empty());
		verify(CommonUtils.class, times(1));
		CommonUtils.logExceptions(any(), any(), any(), any());
	}

	@After
	public void tearDown() {
		if (mockStaticAppRulesAndConfigsUtils != null) {
			mockStaticAppRulesAndConfigsUtils.close();
		}
		if (mockStaticCommonUtils != null) {
			mockStaticCommonUtils.close();
		}
	}

}
