package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.dto.ProcessInfoDto;
import com.trinet.ambis.service.impl.QueuedStrategySyncServiceImpl;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.JsonConverterUtils;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class QueuedStrategySyncServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	QueuedStrategySyncServiceImpl queuedProspectStrategySyncService;

	@Mock
	private CompanyDao companyDao;

	@Spy
	Executor executor = java.util.concurrent.Executors.newCachedThreadPool();

	@Mock
	private ProcessStatusService processStatusService;

	@Mock
	private StrategySyncService strategySyncService;


	private long id = 101;
	private final long companyId = 123456;
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;

	@Before
	public void setUp() throws Exception {
        appRulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
	}

    @After
    public void tearDown() {
        appRulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void scheduleStrategySyncEmptyTest() {
		when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(new ArrayList<>());
		queuedProspectStrategySyncService.startScheduledStrategySyncProcess();
		assertTrue(true);
	}

	@Test
	public void scheduleStrategySyncValidTest() {

        List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
        when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);

        assertEquals("N", processStatusRows.get(0).getProcessStatus());

        queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

        assertEquals("P", processStatusRows.get(0).getProcessStatus());
	}

	@Test
	public void scheduleStrategySyncErrorTest() {
        when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(Boolean.TRUE);
        List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
        when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);
        doThrow(new BSSApplicationException("testError")).when(strategySyncService).syncStrategiesForCompany(any(), any(), any());

        assertEquals("N", processStatusRows.get(0).getProcessStatus());

        queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

        assertEquals("F", processStatusRows.get(0).getProcessStatus());
	}
	
    public void startScheduledStrategySyncProcessErrorTest2() {
        when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(Boolean.FALSE);
        List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
        when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);
        when(companyDao.getCompanyCodeAndRealm(companyId)).thenReturn(prepareCompanyCodeAndRealm());
        doThrow(new BSSApplicationException("testError")).when(strategySyncService).syncStrategiesForCompany(any(), any(), any());

        assertEquals("N", processStatusRows.get(0).getProcessStatus());

        queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

        assertEquals("F", processStatusRows.get(0).getProcessStatus());
    }


    @Test
    public void startScheduledStrategySyncProcessWithProspectBandUpdateEvent() {
        List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
        when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(Boolean.TRUE);
        when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);

        assertEquals("N", processStatusRows.get(0).getProcessStatus());

        queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

        assertEquals("P", processStatusRows.get(0).getProcessStatus());
    }

    @Test
    public void startScheduledStrategySyncProcessWithProspectBandUpdateEventTest2() {
        List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
        when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(Boolean.FALSE);
        when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);

        assertEquals("N", processStatusRows.get(0).getProcessStatus());

        queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

        assertEquals("P", processStatusRows.get(0).getProcessStatus());
    }

    @Test
    public void startScheduledStrategySyncProcessWithProspectStrategySyncPlyrChange() {
        when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(Boolean.TRUE);
        List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName());
        when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);

        assertEquals("N", processStatusRows.get(0).getProcessStatus());

        queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

        assertEquals("P", processStatusRows.get(0).getProcessStatus());
    }

    @Test
    public void startScheduledStrategySyncProcessWithMixedEvents() {
        List<ProcessStatus> processStatusRows = new ArrayList<>();
        when(AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled()).thenReturn(Boolean.TRUE);
        processStatusRows.addAll(prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName()));
        processStatusRows.addAll(prepareStrategySyncProcesses(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName()));
        when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);

        assertEquals("N", processStatusRows.get(0).getProcessStatus());
        assertEquals("N", processStatusRows.get(1).getProcessStatus());

        queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

        assertEquals("N", processStatusRows.get(0).getProcessStatus());
        assertEquals("P", processStatusRows.get(1).getProcessStatus());
    }


    private List<ProcessStatus> prepareStrategySyncProcesses(String eventType) {
        List<ProcessStatus> list = new ArrayList<>();
        list.add(createProcessStatus(id++, companyId, "USERID", eventType));
        return list;
    }

	private ProcessStatus createProcessStatus(long id, long companyId, String userid, String eventType) {
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setId(id);
		processStatus.setProcessName(eventType);
		processStatus.setProcessIdentifer("PROSPECT_ID");
		processStatus.setProcessIdentiferValue(Long.toString(companyId));
		processStatus.setProcessStatus("N");
		processStatus.setErrorMessage(null);
		Date now = new Date();
		processStatus.setCreateTime(now);
		processStatus.setUserId(userid);
		processStatus.setEffDt(now);
		ProcessInfoDto processDataPlanYrDto = ProcessInfoDto.builder().exchangeId(3L)
				.oldCompanyId(companyId).oldRealmPlanYear(83L)
				.processName(eventType)
				.build();
		processStatus.setProcessData(JsonConverterUtils.convertObjectToJson(processDataPlanYrDto));
		if(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName().equalsIgnoreCase(eventType)){
            processStatus.setCreateTime(CommonUtils.formatStringToDate("2001-04-01T12:08:56.240-0700", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        }else{
            processStatus.setCreateTime(CommonUtils.formatStringToDate("2025-04-01T12:08:56.235-0700", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        }
		processStatus.setProcessName(eventType);
		return processStatus;
	}

	private List<Object[]> prepareCompanyCodeAndRealm() {
		Object[] row = { "LITTLE", new BigDecimal("54"), BigDecimal.ONE };
		List<Object[]> list = new ArrayList<>();
		list.add(row);
		return list;
	}

	/**
	 * After a successful strategy sync, any QUARTER_CHANGE records in
	 * status "N" for the same company must be appended to filteredEvents and
	 * therefore transitioned to status "P" by setProcessedStatus.
	 */
	@Test
	public void startStrategySyncAlsoMarksQUARTER_CHANGEEventsAsProcessedTest() {
		// given
		List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
		when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);

		ProcessStatus psQuarterChange = new ProcessStatus();
		psQuarterChange.setId(999L);
		psQuarterChange.setProcessName(ProcessStatusEnum.QUARTER_CHANGE.getProcessName());
		psQuarterChange.setProcessIdentifer(ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName());
		psQuarterChange.setProcessIdentiferValue(Long.toString(companyId));
		psQuarterChange.setProcessStatus("N");
		psQuarterChange.setCreateTime(new Date());

		when(processStatusService.findPendingQuarterChangeProcesses(Long.toString(companyId)))
				.thenReturn(Collections.singletonList(psQuarterChange));

		assertEquals("N", processStatusRows.get(0).getProcessStatus());
		assertEquals("N", psQuarterChange.getProcessStatus());

		// when
		queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

		// then – both original sync event and QUARTER_CHANGE are marked processed
		assertEquals("P", processStatusRows.get(0).getProcessStatus());
		assertEquals("P", psQuarterChange.getProcessStatus());
		verify(processStatusService).findPendingQuarterChangeProcesses(Long.toString(companyId));
	}

	/**
	 * When no QUARTER_CHANGE records are pending, the original
	 * sync events are still marked "P" and findPendingPsQuaterChangeProcessBy is
	 * still invoked.
	 */
	@Test
	public void startStrategySyncWithNoPendingQuarterChangeEventsTest() {
		List<ProcessStatus> processStatusRows = prepareStrategySyncProcesses(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
		when(processStatusService.findNextStrategySyncProcess(anyList())).thenReturn(processStatusRows);
		when(processStatusService.findPendingQuarterChangeProcesses(anyString()))
				.thenReturn(Collections.emptyList());

		assertEquals("N", processStatusRows.get(0).getProcessStatus());
		queuedProspectStrategySyncService.startScheduledStrategySyncProcess();

		assertEquals("P", processStatusRows.get(0).getProcessStatus());
		verify(processStatusService).findPendingQuarterChangeProcesses(anyString());
	}
}