package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.trinet.ambis.service.dto.QuarterChangeProcessInfoDTO;
import com.trinet.ambis.service.email.EmailGenService;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDao;
import com.trinet.ambis.persistence.dao.hrp.ProcessStatusDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PreLoadStrategiesStatusDto;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.EmployeeBenefitGroupService;
import com.trinet.ambis.service.impl.ProcessStatusServiceImpl;
import com.trinet.ambis.service.model.bsscore.BssCoreProcessStatus;
import com.trinet.ambis.service.model.bsscore.BssCoreProcessStatusResponse;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Utils;
import com.trinet.ambis.util.JsonConverterUtils;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import static org.mockito.ArgumentMatchers.any;

@RunWith(JUnit4.class)
public class ProcessStatusServiceImplTest {

	@InjectMocks
	ProcessStatusServiceImpl processStatusService;

	@Mock
	ProcessStatusDao processStatusDao;

	@Mock
	private EmailGenService emailGenService;

	@Mock
	EmployeeDao employeeDao;

	@Mock
	EmployeeBenefitGroupService employeeBenefitGroupService;

	@Captor
	ArgumentCaptor<ProcessStatus> processStatusCaptor;
	
	@Mock
	AppRulesConfigService appRulesConfigService;

	@Mock
	BssCoreServiceClient bssCoreServiceClient;

	private static final String COMPANY_CODE = "COMPANY_CODE";
	private static final String QUARTER = "QUARTER";
	private static final String EMPLOYEE_ID = "EMPLOYEE_ID";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
	}

	@Test
	public void isStrategySummariesProcessed() {
		String companyCode = COMPANY_CODE;
		ProcessStatus processStatus = null;

		/*
		 * Test when ProcessStatus is null
		 */
		when(processStatusDao.findStrateyCreateStatus(companyCode)).thenReturn(processStatus);

		boolean result = processStatusService.isStrategySummariesProcessed(companyCode);

		verify(processStatusDao, times(1)).findStrateyCreateStatus(companyCode);
		assertEquals(true, result);

		/*
		 * Test when ProcessStatus is not null
		 */
		processStatus = new ProcessStatus();
		when(processStatusDao.findStrateyCreateStatus(companyCode)).thenReturn(processStatus);

		result = processStatusService.isStrategySummariesProcessed(companyCode);

		verify(processStatusDao, times(2)).findStrateyCreateStatus(companyCode);
		assertEquals(false, result);
	}

	@Test
	public void isPreLoadProcessed() {
		String quarter = QUARTER;
		ProcessStatus processStatus = null;

		/*
		 * Test when ProcessStatus is null
		 */
		when(processStatusDao.findPreLoadStatus(quarter)).thenReturn(processStatus);

		boolean result = processStatusService.isPreLoadProcessed(quarter);

		verify(processStatusDao, times(1)).findPreLoadStatus(quarter);
		assertEquals(true, result);

		/*
		 * Test when ProcessStatus is not null
		 */
		processStatus = new ProcessStatus();
		when(processStatusDao.findPreLoadStatus(quarter)).thenReturn(processStatus);

		result = processStatusService.isPreLoadProcessed(quarter);

		verify(processStatusDao, times(2)).findPreLoadStatus(quarter);
		assertEquals(false, result);
	}

	@Test
	public void createStrategyProcess() {
		Company company = new Company();
		company.setEmplId(EMPLOYEE_ID);
		company.setCode(COMPANY_CODE);
		ProcessStatus expectedResult = new ProcessStatus();
		expectedResult.setProcessName("STRATEGY_CREATE");
		expectedResult.setUserId(company.getEmplId());
		expectedResult.setProcessIdentifer(COMPANY_CODE);
		expectedResult.setProcessIdentiferValue(company.getCode());
		expectedResult.setCreateTime(new Date());
		expectedResult.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_INPROGRESS);
		expectedResult.setEffDt(null);

		when(processStatusDao.saveAndFlush(processStatusCaptor.capture())).thenReturn(null);

		processStatusService.createStrategyProcess(company);

		verify(processStatusDao, times(1)).saveAndFlush(Mockito.any(ProcessStatus.class));
		assertEquals(expectedResult.getProcessName(), processStatusCaptor.getValue().getProcessName());
		assertEquals(expectedResult.getUserId(), processStatusCaptor.getValue().getUserId());
		assertEquals(expectedResult.getProcessIdentifer(), processStatusCaptor.getValue().getProcessIdentifer());
		assertEquals(expectedResult.getProcessIdentiferValue(),
				processStatusCaptor.getValue().getProcessIdentiferValue());
		assertEquals(Utils.convertDateToString(expectedResult.getCreateTime()),
				Utils.convertDateToString(processStatusCaptor.getValue().getCreateTime()));
		assertEquals(expectedResult.getProcessStatus(), processStatusCaptor.getValue().getProcessStatus());
		assertEquals(expectedResult.getEffDt(), processStatusCaptor.getValue().getEffDt());
	}

	@Test
	public void createPreLoadProcess() {
		String quarter = QUARTER;
		String emplId = EMPLOYEE_ID;

		ProcessStatus expectedResult = new ProcessStatus();
		expectedResult.setProcessName("PRE_LOAD");
		expectedResult.setUserId(emplId);
		expectedResult.setProcessIdentifer(quarter);
		expectedResult.setProcessIdentiferValue(quarter);
		expectedResult.setCreateTime(new Date());
		expectedResult.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_INPROGRESS);
		expectedResult.setEffDt(null);

		when(processStatusDao.saveAndFlush(processStatusCaptor.capture())).thenReturn(null);

		processStatusService.createPreLoadProcess(quarter, emplId);

		verify(processStatusDao, times(1)).saveAndFlush(Mockito.any(ProcessStatus.class));
		assertEquals(expectedResult.getProcessName(), processStatusCaptor.getValue().getProcessName());
		assertEquals(expectedResult.getUserId(), processStatusCaptor.getValue().getUserId());
		assertEquals(expectedResult.getProcessIdentifer(), processStatusCaptor.getValue().getProcessIdentifer());
		assertEquals(expectedResult.getProcessIdentiferValue(),
				processStatusCaptor.getValue().getProcessIdentiferValue());
		assertEquals(Utils.convertDateToString(expectedResult.getCreateTime()),
				Utils.convertDateToString(processStatusCaptor.getValue().getCreateTime()));
		assertEquals(expectedResult.getProcessStatus(), processStatusCaptor.getValue().getProcessStatus());
		assertEquals(expectedResult.getEffDt(), processStatusCaptor.getValue().getEffDt());
	}

	@Test
	public void updateProcessStatus() {

		ProcessStatus processStatus = new ProcessStatus();

		when(processStatusDao.saveAndFlush(processStatusCaptor.capture())).thenReturn(null);

		processStatusService.updateProcessStatus(processStatus);

		verify(processStatusDao, times(1)).saveAndFlush(Mockito.any(ProcessStatus.class));
	}

	@Test
	public void updateProcessStatus_WhenFailed_ShouldSendEmail() {
		// given
		Set<Long> ids = Set.of(1L, 2L);

		ProcessStatus ps = new ProcessStatus();
		ps.setProcessStatus("F");
		ps.setProcessName("TEST_PROCESS");
		ps.setProcessIdentiferValue("COMP123");
		ps.setErrorMessage("Something failed");
		ps.setId(100L);

		// when
		processStatusService.updateProcessStatus(ids, ps);

		// then
		verify(processStatusDao, times(1))
				.updateProcessStatus(ids, "F");

		verify(emailGenService, times(1))
				.createSupportEmail(any());
	}

	@Test
	public void updateProcessStatus_WhenProcessed_ShouldNotSendEmail() {
		// given
		Set<Long> ids = Set.of(1L, 2L);

		ProcessStatus ps = new ProcessStatus();
		ps.setProcessStatus("P");

		// when
		processStatusService.updateProcessStatus(ids, ps);

		// then
		verify(processStatusDao, times(1))
				.updateProcessStatus(ids, "P");

		verify(emailGenService, times(0))
				.createSupportEmail(any());
	}



	@Test
	public void createSubmitProcess() {
		ArgumentCaptor<ProcessStatus> processStatusArgCaptor = ArgumentCaptor.forClass(ProcessStatus.class);

		processStatusService.createSubmitProcess("SUBMIT", "jahskdhkdhkahd", "0000022222283");

		verify(processStatusDao, times(1)).saveAndFlush(processStatusArgCaptor.capture());
		assertEquals("SUBMIT", processStatusArgCaptor.getValue().getProcessName());
		assertEquals("0000022222283", processStatusArgCaptor.getValue().getUserId());
		assertEquals("CONF_NUMBER", processStatusArgCaptor.getValue().getProcessIdentifer());
		assertEquals("jahskdhkdhkahd", processStatusArgCaptor.getValue().getProcessIdentiferValue());
		assertEquals("N", processStatusArgCaptor.getValue().getProcessStatus());
	}

	@Test
	public void findNextToProcessSubmit() {
		ArgumentCaptor<Set<String>> processNameArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Set<String>> statusArgCaptor = ArgumentCaptor.forClass(Set.class);
		ProcessStatus processStatus = new ProcessStatus();

		when(processStatusDao.findTop1ByProcessNameInAndProcessStatusInOrderByCreateTimeAsc(
				processNameArgCaptor.capture(), statusArgCaptor.capture())).thenReturn(processStatus);

		processStatusService.findNextToProcessSubmit();

		assertEquals(4, processNameArgCaptor.getValue().size());
		assertEquals(2, statusArgCaptor.getValue().size());
		assertTrue(statusArgCaptor.getValue().contains("N"));
		assertTrue(statusArgCaptor.getValue().contains("I"));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.SUBMIT_PROCESS.getProcessName()));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.RESUBMIT_PROCESS.getProcessName()));
		assertTrue(processNameArgCaptor.getValue().contains(BSSApplicationConstants.TERMED_CLIENT_DEFAULT_SUBMIT_PROCESS));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.BAND_CODE_RESUBMIT_PROCESS.getProcessName()));
	}

	@Test
	public void findPendingSubmitProcessBy() {
		ArgumentCaptor<String> compArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Set<String>> processNameArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Set<String>> statusArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(processStatusDao.findPendingSubmitProcessBy(compArgCaptor.capture(), statusArgCaptor.capture(),
				processNameArgCaptor.capture())).thenReturn(Collections.emptyList());

		processStatusService.findPendingSubmitProcessBy("G48");

		assertEquals("G48", compArgCaptor.getValue());
		assertEquals(4, processNameArgCaptor.getValue().size());
		assertEquals(2, statusArgCaptor.getValue().size());

		assertTrue(statusArgCaptor.getValue().contains("N"));
		assertTrue(statusArgCaptor.getValue().contains("I"));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.SUBMIT_PROCESS.getProcessName()));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.RESUBMIT_PROCESS.getProcessName()));
		assertTrue(processNameArgCaptor.getValue().contains(BSSApplicationConstants.TERMED_CLIENT_DEFAULT_SUBMIT_PROCESS));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.BAND_CODE_RESUBMIT_PROCESS.getProcessName()));
	}

	@Test
	public void findPendingSubmitProcesses() {
		ArgumentCaptor<Set<String>> processNameArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Set<String>> statusArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(processStatusDao.findByProcessNameInAndProcessStatusInOrderByCreateTimeAsc(processNameArgCaptor.capture(),
				statusArgCaptor.capture())).thenReturn(Collections.emptyList());

		processStatusService.findPendingSubmitProcesses();

		assertEquals(4, processNameArgCaptor.getValue().size());
		assertEquals(1, statusArgCaptor.getValue().size());
		assertTrue(statusArgCaptor.getValue().contains("N"));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.SUBMIT_PROCESS.getProcessName()));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.RESUBMIT_PROCESS.getProcessName()));
		assertTrue(processNameArgCaptor.getValue().contains(BSSApplicationConstants.TERMED_CLIENT_DEFAULT_SUBMIT_PROCESS));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.BAND_CODE_RESUBMIT_PROCESS.getProcessName()));
	}

	@Test
	public void findPendingQuarterChangeProcesses() {
		ArgumentCaptor<Set<String>> processNameArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Set<String>> statusArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(processStatusDao.findByProcessNameInAndProcessStatusInAndProcessIdentiferValueOrderByCreateTimeAsc(processNameArgCaptor.capture(),
				statusArgCaptor.capture(), eq("CODE"))).thenReturn(Collections.emptyList());

		processStatusService.findPendingQuarterChangeProcesses("CODE");

		assertEquals(1, processNameArgCaptor.getValue().size());
		assertEquals(1, statusArgCaptor.getValue().size());
		assertTrue(statusArgCaptor.getValue().contains("N"));
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.QUARTER_CHANGE.getProcessName()));
	}

	@Test
	public void findPreLoadStrategiesStatus() {
		String processName = ProcessStatusEnum.PRE_LOAD.getProcessName();
		List<ProcessStatus> listOfStatus=new ArrayList<>();
		prepareProcessStatus(listOfStatus);
		 Date payrollCutOffDate = new DateTime().minusDays(BSSApplicationConstants.PRELOAD_STATUS_LAST_30_DAYS).toDate();
        String date = CommonUtils.formatDateToString(payrollCutOffDate,BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY);
        when( processStatusDao.findLatestBy(
				CommonUtils.formatStringToDate(date, BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY), processName)).thenReturn(listOfStatus);
        List<PreLoadStrategiesStatusDto> preLoadStrategiesStatuses=processStatusService.getPreLoadStrategiesStatuses();
		assertEquals(1, preLoadStrategiesStatuses.size());
		verify(processStatusDao, times(1)).findLatestBy(CommonUtils.formatStringToDate(date,
				  BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY),
				  processName);
	}

	@Test
	public void findByConfirmationNumberTest() {
		// given
		ProcessStatus processStatus = prepareProcessStatus();
		when(processStatusDao.findByConfirmationNumber(processStatus.getProcessIdentiferValue()))
				.thenReturn(processStatus);
		// when
		ProcessStatus actualProcessStatus = processStatusService
				.findByConfirmationNumber(processStatus.getProcessIdentiferValue());
		// then
		assertNotNull(actualProcessStatus);
		assertEquals(processStatus.getId(), actualProcessStatus.getId());
		assertEquals(processStatus.getProcessName(), actualProcessStatus.getProcessName());
		assertEquals(processStatus.getProcessIdentifer(), actualProcessStatus.getProcessIdentifer());
		assertEquals(processStatus.getProcessIdentiferValue(), actualProcessStatus.getProcessIdentiferValue());
		assertEquals(processStatus.getProcessStatus(), actualProcessStatus.getProcessStatus());
		assertEquals(processStatus.getUserId(), actualProcessStatus.getUserId());
		verify(processStatusDao, times(1)).findByConfirmationNumber(processStatus.getProcessIdentiferValue());
	}

	@Test
	public void findNewCenusHcSyncEventTest() throws ParseException {
		// given
		List<ProcessStatus> expected = prepareCensusHcSyncProcessStatus();
		when(processStatusDao.findNewCenusHcSyncEvent()).thenReturn(expected);
		// when
		List<ProcessStatus> actual = processStatusService.findNewCenusHcSyncEvent();
		// then
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals(expected, actual);
		verify(processStatusDao, times(1)).findNewCenusHcSyncEvent();
	}

	@Test
	public void findNewCenusHcSyncEventForACompanyTest() throws ParseException {
		// given
		String companyCode = "G48";
		List<ProcessStatus> expected = prepareCensusHcSyncProcessStatus();
		when(processStatusDao.findNewCenusHcSyncEvent(companyCode)).thenReturn(expected);
		// when
		List<ProcessStatus> actual = processStatusService.findNewCenusHcSyncEvent(companyCode);
		// then
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals(expected, actual);
		verify(processStatusDao, times(1)).findNewCenusHcSyncEvent(companyCode);
	}
	
	@Test
	public void findInProgressCenusHcSyncEventForACompanyTest() throws ParseException {
		// given
		String companyCode = "G48";
		List<ProcessStatus> expected = prepareCensusHcSyncProcessStatus();
		when(processStatusDao.findInProgressCenusHcSyncEvent(companyCode)).thenReturn(expected);
		// when
		List<ProcessStatus> actual = processStatusService.findInProgressCenusHcSyncEvent(companyCode);
		// then
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals(expected, actual);
		verify(processStatusDao, times(1)).findInProgressCenusHcSyncEvent(companyCode);
	}
	
	@Test
	public void updateProcessStatusProcessedTest() {
		// given
		Set<Long> ids = Set.of(1L, 2L);
		String processStatus = "P";
		// when
		processStatusDao.updateProcessStatus(ids, processStatus);
		// then
		verify(processStatusDao, times(1)).updateProcessStatus(ids, processStatus);
	}

	@Test
	public void updateProcessStatusFailedTest() {
		// given
		Set<Long> ids = Set.of(1L, 2L);
		String processStatus = "F";
		// when
		processStatusDao.updateProcessStatus(ids, processStatus);
		// then
		verify(processStatusDao, times(1)).updateProcessStatus(ids, processStatus);
	}

	@Test
	public void findLastRecordByCompanyAndEventTest() throws ParseException {
		// given
		long companyId = 1234;
		List<ProcessStatus> processStatuses = prepareBandCodeProcessStatus();
		when(processStatusDao.findByProcessNameAndProcessIdentiferValueOrderByCreateTimeDesc(
				ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(), String.valueOf(companyId)))
				.thenReturn(processStatuses);

		// when
		ProcessStatus result = processStatusService.findLastRecordByCompanyAndEvent(companyId,
				ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());

		// then
		verify(processStatusDao, times(1)).findByProcessNameAndProcessIdentiferValueOrderByCreateTimeDesc(
				ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(), String.valueOf(companyId));
		assertEquals(1, result.getId());

	}

	@Test
	public void findLastRecordByCompanyAndEventNoRowsTest() throws ParseException {
		// given
		long companyId = 1234;
		when(processStatusDao.findByProcessNameAndProcessIdentiferValueOrderByCreateTimeDesc(
				ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(), String.valueOf(companyId))).thenReturn(null);

		// when
		ProcessStatus result = processStatusService.findLastRecordByCompanyAndEvent(companyId,
				ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());

		// then
		verify(processStatusDao, times(1)).findByProcessNameAndProcessIdentiferValueOrderByCreateTimeDesc(
				ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(), String.valueOf(companyId));
		assertNull(result);

	}

	
	@Test
	public void findBandUpdateProcessStatusA() {
		long companyId = 1111;
		String companyCode = "test";
		List<ProcessStatus> processStatusInProgress = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer(ProcessStatusEnum.BAND_UPDATE_EVENT.getIdentifierName());
		processStatus.setProcessName(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
		processStatus.setProcessIdentiferValue("1111");
		processStatus.setProcessStatus("I");
		processStatus.setUserId("1");
		processStatusInProgress.add(processStatus);
		
		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(processStatus.getProcessIdentifer(),
				companyCode, processNames)).thenReturn(processStatusInProgress);

		String code = processStatusService.findStrategySyncProcessStatus( companyCode);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				processStatus.getProcessIdentifer(), companyCode, processNames);
		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertEquals("I", code);
	}	
	
	@Test
	public void findBandUpdateProcessStatusB() {
		long companyId = 1111;
		String companyCode = "test";
		List<ProcessStatus> processStatusInProgress = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer(ProcessStatusEnum.BAND_UPDATE_EVENT.getIdentifierName());
		processStatus.setProcessName(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
		processStatus.setProcessIdentiferValue("1111");
		processStatus.setProcessStatus("P");
		processStatus.setUserId("1");
		processStatusInProgress.add(processStatus);

		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(processStatus.getProcessIdentifer(),
				String.valueOf(companyId), processNames)).thenReturn(processStatusInProgress);

		String statusCode = processStatusService.findStrategySyncProcessStatus(companyCode);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				processStatus.getProcessIdentifer(), companyCode, processNames);
		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertNotNull(statusCode);
		assertEquals("", statusCode);
	}
	
	@Test
	public void findBandUpdateProcessStatusWithFailed() {
		long companyId = 1111;
		String companyCode = "test";
		List<ProcessStatus> processStatusInProgress = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer(ProcessStatusEnum.BAND_UPDATE_EVENT.getIdentifierName());
		processStatus.setProcessName(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
		processStatus.setProcessIdentiferValue("1111");
		processStatus.setProcessStatus("F");
		processStatus.setUserId("1");
		processStatusInProgress.add(processStatus);

		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(processStatus.getProcessIdentifer(),
				companyCode, processNames)).thenReturn(processStatusInProgress);

		String code = processStatusService.findStrategySyncProcessStatus(companyCode);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				processStatus.getProcessIdentifer(), companyCode, processNames);
		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertEquals("F", code);
	}

	@Test
	public void findStrategySyncProcessStatusWithFailed() {
		long companyId = 1111;
		String companyCode = "test";
		List<ProcessStatus> processStatusInProgress = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName());
		processStatus.setProcessName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName());
		processStatus.setProcessIdentiferValue("1111");
		processStatus.setProcessStatus("F");
		processStatus.setUserId("1");
		processStatusInProgress.add(processStatus);

		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(processStatus.getProcessIdentifer(),
				companyCode, processNames)).thenReturn(processStatusInProgress);

		String code = processStatusService.findStrategySyncProcessStatus(companyCode);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				processStatus.getProcessIdentifer(), companyCode, processNames);
		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertEquals("F", code);
	}

	@Test
	public void findStrategySyncProcessStatusWithInProgress() {
		long companyId = 1111;
		String companyCode = "test";
		List<ProcessStatus> processStatusInProgress = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName());
		processStatus.setProcessName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName());
		processStatus.setProcessIdentiferValue("1111");
		processStatus.setProcessStatus("I");
		processStatus.setUserId("1");
		processStatusInProgress.add(processStatus);

		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(processStatus.getProcessIdentifer(),
				companyCode, processNames)).thenReturn(processStatusInProgress);

		String code = processStatusService.findStrategySyncProcessStatus(companyCode);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				processStatus.getProcessIdentifer(), companyCode, processNames);
		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertEquals("I", code);
	}
	
	@Test
	public void findStrategySyncProcessStatusWithProcess() {
		long companyId = 1111;
		String companyCode = "test";
		List<ProcessStatus> processStatusInProgress = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName());
		processStatus.setProcessName(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName());
		processStatus.setProcessIdentiferValue("1111");
		processStatus.setProcessStatus("P");
		processStatus.setUserId("1");
		processStatusInProgress.add(processStatus);

		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(processStatus.getProcessIdentifer(),
				String.valueOf(companyId), processNames)).thenReturn(processStatusInProgress);

		String code = processStatusService.findStrategySyncProcessStatus(companyCode);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				processStatus.getProcessIdentifer(), companyCode, processNames);
		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertEquals("", code);
	}

	@Test
	public void findStrategySyncProcessStatusWithQuarterChange() {
		long companyId = 1111;
		String companyCode = "test";
		List<ProcessStatus> processStatusNew = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer(ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName());
		processStatus.setProcessName(ProcessStatusEnum.QUARTER_CHANGE.getProcessName());
		processStatus.setProcessIdentiferValue("1111");
		processStatus.setProcessStatus("N");
		processStatus.setUserId("1");
		processStatusNew.add(processStatus);

		List<String> processNames = Arrays.asList(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(),
				ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(),
				ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(),
				companyCode, processNames)).thenReturn(processStatusNew);

		String code = processStatusService.findStrategySyncProcessStatus(companyCode);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.BAND_UPDATE_EVENT.getIdentifierName(), companyCode, processNames);
		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				processStatus.getProcessIdentifer(), companyCode, processNames);
		assertEquals("N", code);
	}
	

	@Test
	public void createStrategySyncPlyrChangeTest() {
		String companyCode = "123456";
		String processData = "{ \"oldPlanYearId\" : 78, \"newPlanYearId\" : 80 \"exchangeId\": 3 }";
		String processName = ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName();
		String identifierName = ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getIdentifierName();
		ArgumentCaptor<ProcessStatus> processStatusArgCaptor = ArgumentCaptor.forClass(ProcessStatus.class);
		processStatusService.createStrategySyncProcess(companyCode, processData, processName, identifierName);

		verify(processStatusDao, times(1)).saveAndFlush(processStatusArgCaptor.capture());
		ProcessStatus ps = processStatusArgCaptor.getValue();
		
		assertEquals(ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE.getProcessName(), ps.getProcessName());
		assertEquals("PROSPECT_ID", ps.getProcessIdentifer());
		assertEquals(companyCode, ps.getProcessIdentiferValue());
		assertEquals("N", ps.getProcessStatus());
		assertNotNull(ps.getCreateTime());
		assertEquals("SYSTEM", ps.getUserId());
		assertNull(ps.getEffDt());
	}

	@Test
	public void createBandUpdateProcessTest() {
		String companyCode = "123456";
		ArgumentCaptor<ProcessStatus> processStatusArgCaptor = ArgumentCaptor.forClass(ProcessStatus.class);
		processStatusService.createBandUpdateProcess(3L, companyCode, 123L);
		verify(processStatusDao, times(1)).saveAndFlush(processStatusArgCaptor.capture());
		ProcessStatus ps = processStatusArgCaptor.getValue();

		assertEquals(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName(), ps.getProcessName());
		assertEquals("PROSPECT_ID", ps.getProcessIdentifer());
		assertEquals(companyCode, ps.getProcessIdentiferValue());
		assertEquals("N", ps.getProcessStatus());
		assertNotNull(ps.getCreateTime());
		assertEquals("SYSTEM", ps.getUserId());
		assertNull(ps.getEffDt());
	}

	@Test
	public void findPendingOrInProgressPSQuarterChangeProcessStatus() {
		String companyCode = "G48";
		String newQuarter = "Q3";
		List<String> processNames = Collections.singletonList(ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setProcessIdentifer(ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName());
		processStatus.setProcessStatus("N");
		processStatus.setProcessData(JsonConverterUtils.convertObjectToJson(
				QuarterChangeProcessInfoDTO.builder().newQuaterId(newQuarter).build()));

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames))
				.thenReturn(Collections.singletonList(processStatus));

		String code = processStatusService.findPendingOrInProgressPSQuarterChangeProcessStatus(companyCode, newQuarter);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertEquals("N", code);
	}

	@Test
	public void findPendingOrInProgressPSQuarterChangeProcessStatusWithNoMatchingQuarter() {
		String companyCode = "G48";
		String newQuarter = "Q3";
		List<String> processNames = Collections.singletonList(ProcessStatusEnum.QUARTER_CHANGE.getProcessName());

		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setProcessIdentifer(ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName());
		processStatus.setProcessStatus("N");
		processStatus.setProcessData(JsonConverterUtils.convertObjectToJson(
				QuarterChangeProcessInfoDTO.builder().newQuaterId("Q4").build()));

		when(processStatusDao.findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames))
				.thenReturn(Collections.singletonList(processStatus));

		String code = processStatusService.findPendingOrInProgressPSQuarterChangeProcessStatus(companyCode, newQuarter);

		verify(processStatusDao, times(1)).findNewOrInProgessOrFailedStrategySyncEvents(
				ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName(), companyCode, processNames);
		assertEquals("", code);
	}

	/**
	 * Verifies that findPendingQuarterChangeProcessBy delegates to the DAO with:
	 *   - the supplied companyCode
	 *   - status set containing only "N" (PROCESS_STATUS_NEW)
	 *   - processNames set containing only "QUARTER_CHANGE"
	 */
	@Test
	public void findPendingQuarterChangeProcessesTest() {
		// given
		ArgumentCaptor<String> compArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Set<String>> statusArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<Set<String>> processNameArgCaptor = ArgumentCaptor.forClass(Set.class);

		when(processStatusDao.findByProcessNameInAndProcessStatusInAndProcessIdentiferValueOrderByCreateTimeAsc(
				processNameArgCaptor.capture(), statusArgCaptor.capture(), compArgCaptor.capture()))
				.thenReturn(Collections.emptyList());

		// when
		processStatusService.findPendingQuarterChangeProcesses("G48");

		// then
		assertEquals("G48", compArgCaptor.getValue());
		assertEquals(1, statusArgCaptor.getValue().size());
		assertTrue(statusArgCaptor.getValue().contains(BSSApplicationConstants.PROCESS_STATUS_NEW));
		assertEquals(1, processNameArgCaptor.getValue().size());
		assertTrue(processNameArgCaptor.getValue().contains(ProcessStatusEnum.QUARTER_CHANGE.getProcessName()));
	}

	private void prepareProcessStatus(List<ProcessStatus> listOfStatus) {
		ProcessStatus processStatus=new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer("Q1");
		processStatus.setProcessName("PRE_LOAD");
		processStatus.setProcessIdentiferValue("A");
		processStatus.setUserId("1");
		listOfStatus.add(processStatus);
		
	}

	private ProcessStatus prepareProcessStatus() {
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setId(1155582);
		processStatus.setProcessName("SUBMIT");
		processStatus.setProcessIdentifer("CONF_NUMBER");
		processStatus.setProcessIdentiferValue("KTYOWKAADKL9");
		processStatus.setProcessStatus("P");
		processStatus.setUserId("00002145698");
		return processStatus;
	}
	
	private List<ProcessStatus> prepareCensusHcSyncProcessStatus() throws ParseException {
		List<ProcessStatus> events = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer("COMPANY_CODE");
		processStatus.setProcessName("CENSUS_HC_SYNC");
		processStatus.setProcessIdentiferValue("6EK");
		processStatus.setProcessStatus("N");
		processStatus.setUserId("1");
		processStatus.setEffDt(DateUtils.parseDate("01-08-2022", "dd-mm-yyyy"));
		events.add(processStatus);
		ProcessStatus processStatus1 = new ProcessStatus();
		processStatus1.setCreateTime(new Date());
		processStatus1.setId(1);
		processStatus1.setProcessIdentifer("COMPANY_CODE");
		processStatus1.setProcessName("CENSUS_HC_SYNC");
		processStatus1.setProcessIdentiferValue("6EK");
		processStatus1.setProcessStatus("N");
		processStatus1.setUserId("2");
		processStatus.setEffDt(DateUtils.parseDate("01-08-2022", "dd-mm-yyyy"));
		events.add(processStatus1);
		return events;
	}


	private List<ProcessStatus> prepareBandCodeProcessStatus() throws ParseException {
		List<ProcessStatus> events = new ArrayList<>();
		ProcessStatus processStatus = new ProcessStatus();
		processStatus.setCreateTime(new Date());
		processStatus.setId(1);
		processStatus.setProcessIdentifer("COMPANY_ID");
		processStatus.setProcessName(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
		processStatus.setProcessIdentiferValue("1234");
		processStatus.setProcessStatus("N");
		processStatus.setUserId("1");
		processStatus.setEffDt(DateUtils.parseDate("01-09-2022", "dd-mm-yyyy"));
		events.add(processStatus);
		ProcessStatus processStatus1 = new ProcessStatus();
		processStatus1.setCreateTime(new Date());
		processStatus1.setId(2);
		processStatus1.setProcessIdentifer("COMPANY_ID");
		processStatus1.setProcessName(ProcessStatusEnum.BAND_UPDATE_EVENT.getProcessName());
		processStatus1.setProcessIdentiferValue("1234");
		processStatus1.setProcessStatus("N");
		processStatus1.setUserId("2");
		processStatus.setEffDt(DateUtils.parseDate("01-08-2022", "dd-mm-yyyy"));
		events.add(processStatus1);
		return events;
	}

	@Test
	public void findPendingQuarterChangeProcesses_returnsRecords() {
		// given
		String companyCode = "G48";
		List<ProcessStatus> expected = new ArrayList<>();
		ProcessStatus ps = new ProcessStatus();
		ps.setId(1);
		ps.setProcessName(ProcessStatusEnum.QUARTER_CHANGE.getProcessName());
		ps.setProcessIdentifer(ProcessStatusEnum.QUARTER_CHANGE.getIdentifierName());
		ps.setProcessIdentiferValue(companyCode);
		ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_NEW);
		ps.setUserId("T2_CLIENTOPTION_MSG");
		expected.add(ps);

		when(processStatusDao.findByProcessNameInAndProcessStatusInAndProcessIdentiferValueOrderByCreateTimeAsc(
				Mockito.anySet(),
				Mockito.anySet(),
				Mockito.eq(companyCode)
		)).thenReturn(expected);

		// when
		List<ProcessStatus> actual = processStatusService.findPendingQuarterChangeProcesses(companyCode);

		// then
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(ProcessStatusEnum.QUARTER_CHANGE.getProcessName(), actual.get(0).getProcessName());
		verify(processStatusDao, times(1)).findByProcessNameInAndProcessStatusInAndProcessIdentiferValueOrderByCreateTimeAsc(
				Mockito.anySet(), Mockito.anySet(), Mockito.eq(companyCode));
	}

	@Test
	public void findPendingQuarterChangeProcesses_returnsEmptyWhenNoneFound() {
		// given
		String companyCode = "G48";
		when(processStatusDao.findByProcessNameInAndProcessStatusInAndProcessIdentiferValueOrderByCreateTimeAsc(
				Mockito.anySet(),
				Mockito.anySet(),
				Mockito.eq(companyCode)
		)).thenReturn(new ArrayList<>());

		// when
		List<ProcessStatus> actual = processStatusService.findPendingQuarterChangeProcesses(companyCode);

		// then
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
	}

	// ── findBssCoreProcessStatus tests ───────────────────────────────────────

	@Test
	public void findBssCoreProcessStatus_allCompleted_returnsTrue() {
		BssCoreProcessStatus completed = BssCoreProcessStatus.builder()
				.processName("BUNDLE_STRATEGY_SYNC").status("COMPLETED").finishedAt("2026-03-30T09:12:45Z").build();
		BssCoreProcessStatusResponse response = BssCoreProcessStatusResponse.builder()
				.companyCode(COMPANY_CODE).processStatuses(List.of(completed)).build();
		when(bssCoreServiceClient.getProcessStatusesBy(COMPANY_CODE)).thenReturn(response);

		boolean result = processStatusService.findBssCoreProcessStatus(COMPANY_CODE);

		assertTrue(result);
	}

	@Test
	public void findBssCoreProcessStatus_anyNotCompleted_returnsFalse() {
		BssCoreProcessStatus completed = BssCoreProcessStatus.builder()
				.processName("BUNDLE_STRATEGY_SYNC").status("COMPLETED").finishedAt("2026-03-30T09:12:45Z").build();
		BssCoreProcessStatus inProgress = BssCoreProcessStatus.builder()
				.processName("BUNDLE_STRATEGY_SYNC").status("IN_PROGRESS").finishedAt(null).build();
		BssCoreProcessStatusResponse response = BssCoreProcessStatusResponse.builder()
				.companyCode(COMPANY_CODE).processStatuses(List.of(completed, inProgress)).build();
		when(bssCoreServiceClient.getProcessStatusesBy(COMPANY_CODE)).thenReturn(response);

		boolean result = processStatusService.findBssCoreProcessStatus(COMPANY_CODE);

		assertFalse(result);
	}

	@Test
	public void findBssCoreProcessStatus_emptyList_returnsTrue() {
		BssCoreProcessStatusResponse response = BssCoreProcessStatusResponse.builder()
				.companyCode(COMPANY_CODE).processStatuses(new ArrayList<>()).build();
		when(bssCoreServiceClient.getProcessStatusesBy(COMPANY_CODE)).thenReturn(response);

		boolean result = processStatusService.findBssCoreProcessStatus(COMPANY_CODE);

		assertTrue(result);
	}

	@Test
	public void findBssCoreProcessStatus_nullResponse_returnsTrue() {
		when(bssCoreServiceClient.getProcessStatusesBy(COMPANY_CODE)).thenReturn(null);

		boolean result = processStatusService.findBssCoreProcessStatus(COMPANY_CODE);

		assertTrue(result);
	}

	@Test
	public void findBssCoreProcessStatus_404NotFound_rethrowsHttpClientErrorException() {
		HttpClientErrorException notFound = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
				org.springframework.http.HttpHeaders.EMPTY, null, null);
		when(bssCoreServiceClient.getProcessStatusesBy(COMPANY_CODE)).thenThrow(notFound);

		try {
			processStatusService.findBssCoreProcessStatus(COMPANY_CODE);
			org.junit.Assert.fail("Expected HttpClientErrorException");
		} catch (HttpClientErrorException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
		}
	}

	@Test
	public void findBssCoreProcessStatus_403Forbidden_throwsBSSApplicationException() {
		when(bssCoreServiceClient.getProcessStatusesBy(COMPANY_CODE))
				.thenThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Forbidden",
						org.springframework.http.HttpHeaders.EMPTY, null, null));

		try {
			processStatusService.findBssCoreProcessStatus(COMPANY_CODE);
			org.junit.Assert.fail("Expected BSSApplicationException");
		} catch (BSSApplicationException e) {
			assertEquals(BSSErrorResponseCodes.BSS_CORE_AUTH_ERROR, e.getBssError().getCode());
		}
	}

	@Test
	public void findBssCoreProcessStatus_401Unauthorized_throwsBSSApplicationException() {
		when(bssCoreServiceClient.getProcessStatusesBy(COMPANY_CODE))
				.thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized",
						org.springframework.http.HttpHeaders.EMPTY, null, null));

		try {
			processStatusService.findBssCoreProcessStatus(COMPANY_CODE);
			org.junit.Assert.fail("Expected BSSApplicationException");
		} catch (BSSApplicationException e) {
			assertEquals(BSSErrorResponseCodes.BSS_CORE_AUTH_ERROR, e.getBssError().getCode());
		}
	}

}