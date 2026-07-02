package com.trinet.ambis.persistence.dao.hrp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.dto.CensusHcSyncEventDto;
import com.trinet.ambis.persistence.dao.hrp.impl.RealTimeSyncDaoImpl;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class RealTimeSyncDaoImplTest extends ServiceUnitTest {

	@InjectMocks
	private RealTimeSyncDaoImpl realTimeSyncDao;

	@Mock
	private ProcessStatusService processStatusService;

	private static final String PROCESS_STATUS_INPROGRESS = "I";

	@Test
	public void findNewEventsTest() throws ParseException {
		// given
		List<ProcessStatus> events = prepareCensusHcSyncProcessStatus();
		Set<Long> processStatusIds = events.stream().map(ProcessStatus::getId).collect(Collectors.toSet());
		when(processStatusService.findNewCenusHcSyncEvent()).thenReturn(events);
		// when
		Optional<CensusHcSyncEventDto> actualOpt = realTimeSyncDao
				.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		CensusHcSyncEventDto actual = actualOpt.get();
		// then
		assertNotNull(actualOpt);
		assertTrue(actualOpt.isPresent());
		assertNotNull(actual);
		assertEquals(events.get(0).getProcessIdentiferValue(), actual.getCompanyCode());
		assertEquals(processStatusIds.size(), actual.getProcessStatusIds().size());
		assertEquals(processStatusIds, actual.getProcessStatusIds());
		assertEquals(PROCESS_STATUS_INPROGRESS, events.get(0).getProcessStatus());
		assertEquals(PROCESS_STATUS_INPROGRESS, events.get(1).getProcessStatus());
		verify(processStatusService, times(1)).findNewCenusHcSyncEvent();
	}

	@Test
	public void findNoNewEventsTest() throws ParseException {
		// given
		when(processStatusService.findNewCenusHcSyncEvent()).thenReturn(Collections.emptyList());
		// when
		Optional<CensusHcSyncEventDto> actualOpt = realTimeSyncDao
				.findNewCensusHcSyncEventAndUpdateToInProgress(Optional.empty());
		// then
		assertNotNull(actualOpt);
		assertFalse(actualOpt.isPresent());
	}

	@Test
	public void findNewEventsForACompanyTest() throws ParseException {
		// given
		Optional<String> companyCode = Optional.of("6EK");
		List<ProcessStatus> events = prepareCensusHcSyncProcessStatus();
		Set<Long> processStatusIds = events.stream().map(ProcessStatus::getId).collect(Collectors.toSet());
		when(processStatusService.findNewCenusHcSyncEvent(companyCode.get())).thenReturn(events);
		// when
		Optional<CensusHcSyncEventDto> actualOpt = realTimeSyncDao
				.findNewCensusHcSyncEventAndUpdateToInProgress(companyCode);
		CensusHcSyncEventDto actual = actualOpt.get();
		// then
		assertNotNull(actualOpt);
		assertTrue(actualOpt.isPresent());
		assertNotNull(actual);
		assertEquals(events.get(0).getProcessIdentiferValue(), actual.getCompanyCode());
		assertEquals(processStatusIds.size(), actual.getProcessStatusIds().size());
		assertEquals(processStatusIds, actual.getProcessStatusIds());
		assertEquals(PROCESS_STATUS_INPROGRESS, events.get(0).getProcessStatus());
		assertEquals(PROCESS_STATUS_INPROGRESS, events.get(1).getProcessStatus());
		verify(processStatusService, times(1)).findNewCenusHcSyncEvent(companyCode.get());
	}

	@Test
	public void findNoNewEventsForACompanyTest() throws ParseException {
		// given
		Optional<String> companyCode = Optional.of("6EK");
		when(processStatusService.findNewCenusHcSyncEvent(companyCode.get())).thenReturn(Collections.emptyList());
		// when
		Optional<CensusHcSyncEventDto> actualOpt = realTimeSyncDao
				.findNewCensusHcSyncEventAndUpdateToInProgress(companyCode);
		// then
		assertNotNull(actualOpt);
		assertFalse(actualOpt.isPresent());
	}

	private List<ProcessStatus> prepareCensusHcSyncProcessStatus() throws ParseException {
		List<ProcessStatus> events = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer("COMPANY_CODE");
		processStatus.setProcessName("CENSUS_HC_SYNC");
		processStatus.setProcessIdentiferValue("6EK");
		processStatus.setProcessStatus("I");
		processStatus.setUserId("1");
		processStatus.setEffDt(DateUtils.parseDate("01-08-2022", "dd-mm-yyyy"));
		events.add(processStatus);
		ProcessStatus processStatus1 = new ProcessStatus();
		processStatus1.setCreateTime(new Date());
		processStatus1.setId(1);
		processStatus1.setProcessIdentifer("COMPANY_CODE");
		processStatus1.setProcessName("CENSUS_HC_SYNC");
		processStatus1.setProcessIdentiferValue("6EK");
		processStatus1.setProcessStatus("I");
		processStatus1.setUserId("2");
		processStatus.setEffDt(DateUtils.parseDate("01-08-2022", "dd-mm-yyyy"));
		events.add(processStatus1);
		return events;
	}

}
