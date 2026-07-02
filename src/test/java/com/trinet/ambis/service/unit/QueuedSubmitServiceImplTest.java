package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.impl.QueuedSubmitServiceImpl;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.submit.SubmitService;

@RunWith(MockitoJUnitRunner.class)
public class QueuedSubmitServiceImplTest extends ServiceUnitTest {

	private static final long STRATEGY_ID = 1111;
	private static final long COMPANY_ID = 2222;
	private static final String COMPANY_CODE = "G48";
	private static final String EMPL_ID = "00006666656";
	private static final Long REALM_YR_ID = 43L;
	private static final Date STRATEGY_SUBMIT_DATE = new Date();

	@InjectMocks
	QueuedSubmitServiceImpl queuedSubmitService;

	@Mock
	SubmitStatusService submitStatusService;

	@Mock
	ProcessStatusService processStatusService;

	@Mock
	SubmitService submitService;

	@Mock
	StrategyDao strategyDao;

	@Test
	public void createSubmitProcess_submit() {
		Company company = createCompany();
		StrategyData dto = createStrategyData();
		String processName = "SUBMIT";
		boolean sendEmail = true;
		Strategy strategy = new Strategy();

		ArgumentCaptor<SubmitStatus> ssArgCaptor = ArgumentCaptor.forClass(SubmitStatus.class);
		ArgumentCaptor<String> processNameArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> confirmationIdArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> userIdArgCaptor = ArgumentCaptor.forClass(String.class);

		ArgumentCaptor<SubmissionInfo> submissionInfoArgCaptor = ArgumentCaptor.forClass(SubmissionInfo.class);
		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);

		when(strategyDao.findByIdAndCompanyIdAndStatus(STRATEGY_ID, COMPANY_ID, "A")).thenReturn(strategy);
		when(strategyDao.saveAndFlush(strategy)).thenReturn(null);
		when(submitStatusService.createUpdateSubmitStatus(ssArgCaptor.capture())).thenReturn(null);
		doNothing().when(processStatusService).createSubmitProcess(processNameArgCaptor.capture(),
				confirmationIdArgCaptor.capture(), userIdArgCaptor.capture());
		when(submitService.preSubmit(companyArgCaptor.capture(), submissionInfoArgCaptor.capture()))
				.thenReturn(CompletableFuture.completedFuture(null));

		queuedSubmitService.createSubmitProcess(company, dto, processName, sendEmail);

		verify(strategyDao, times(1)).findByIdAndCompanyIdAndStatus(STRATEGY_ID, COMPANY_ID, "A");
		verify(strategyDao, times(1)).saveAndFlush(strategy);
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(any(SubmitStatus.class));
		verify(processStatusService, times(1)).createSubmitProcess(anyString(), anyString(), anyString());
		verify(submitService, times(1)).preSubmit(any(Company.class), any(SubmissionInfo.class));

		assertEquals(STRATEGY_ID, ssArgCaptor.getValue().getStrategyId());
		assertEquals(processNameArgCaptor.getValue(), processName);
		assertEquals("UNPROCESSED", ssArgCaptor.getValue().getStatus());
		assertEquals(STRATEGY_SUBMIT_DATE, ssArgCaptor.getValue().getCreateTime());
		assertEquals(EMPL_ID, ssArgCaptor.getValue().getUserId());
		assertEquals(COMPANY_CODE, ssArgCaptor.getValue().getCompany());
		assertFalse(ssArgCaptor.getValue().getEmailSentStatus());
		assertEquals(confirmationIdArgCaptor.getValue(), ssArgCaptor.getValue().getConfirmationNumber());
		assertEquals(REALM_YR_ID, ssArgCaptor.getValue().getRealmYrId());
		assertEquals("TESTSERVICEORDER", ssArgCaptor.getValue().getServiceOrder());
		assertNull(ssArgCaptor.getValue().getStatementUploadStatus());
		assertNull(ssArgCaptor.getValue().getUpdateTime());
		assertTrue(ssArgCaptor.getValue().getSendEmail());
		assertEquals(EMPL_ID, userIdArgCaptor.getValue());
		assertEquals(company, companyArgCaptor.getValue());
		assertTrue(submissionInfoArgCaptor.getValue().isQueuedSubmit());
		assertEquals(COMPANY_CODE, submissionInfoArgCaptor.getValue().getCompanyCode());

	}

	@Test
	public void createSubmitProcess_resubmit() {
		Company company = createCompany();
		StrategyData dto = createStrategyData();
		String processName = "RESUBMIT";
		boolean sendEmail = true;
		Strategy strategy = new Strategy();

		ArgumentCaptor<SubmitStatus> ssArgCaptor = ArgumentCaptor.forClass(SubmitStatus.class);
		ArgumentCaptor<String> processNameArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> confirmationIdArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> userIdArgCaptor = ArgumentCaptor.forClass(String.class);

		when(strategyDao.findByIdAndCompanyIdAndStatus(STRATEGY_ID, COMPANY_ID, "A")).thenReturn(strategy);
		when(strategyDao.saveAndFlush(strategy)).thenReturn(null);
		when(submitStatusService.createUpdateSubmitStatus(ssArgCaptor.capture())).thenReturn(null);
		doNothing().when(processStatusService).createSubmitProcess(processNameArgCaptor.capture(),
				confirmationIdArgCaptor.capture(), userIdArgCaptor.capture());

		queuedSubmitService.createSubmitProcess(company, dto, processName, sendEmail);

		verify(strategyDao, times(1)).findByIdAndCompanyIdAndStatus(STRATEGY_ID, COMPANY_ID, "A");
		verify(strategyDao, times(1)).saveAndFlush(strategy);
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(any(SubmitStatus.class));
		verify(processStatusService, times(1)).createSubmitProcess(anyString(), anyString(), anyString());

		assertEquals(STRATEGY_ID, ssArgCaptor.getValue().getStrategyId());
		assertEquals(processNameArgCaptor.getValue(), processName);
		assertEquals("UNPROCESSED", ssArgCaptor.getValue().getStatus());
		assertEquals(STRATEGY_SUBMIT_DATE, ssArgCaptor.getValue().getCreateTime());
		assertEquals(EMPL_ID, ssArgCaptor.getValue().getUserId());
		assertEquals(COMPANY_CODE, ssArgCaptor.getValue().getCompany());
		assertFalse(ssArgCaptor.getValue().getEmailSentStatus());
		assertEquals(confirmationIdArgCaptor.getValue(), ssArgCaptor.getValue().getConfirmationNumber());
		assertEquals(REALM_YR_ID, ssArgCaptor.getValue().getRealmYrId());
		assertEquals("TESTSERVICEORDER", ssArgCaptor.getValue().getServiceOrder());
		assertNull(ssArgCaptor.getValue().getStatementUploadStatus());
		assertNull(ssArgCaptor.getValue().getUpdateTime());
		assertTrue(ssArgCaptor.getValue().getSendEmail());
		assertEquals(EMPL_ID, userIdArgCaptor.getValue());
	}

	private Company createCompany() {
		Company comp = new Company();
		comp.setCode(COMPANY_CODE);
		comp.setId(COMPANY_ID);
		comp.setEmplId(EMPL_ID);
		RealmPlanYear rpl = new RealmPlanYear();
		rpl.setId(REALM_YR_ID);
		comp.setRealmPlanYear(rpl);
		comp.setServiceOrderNumber("TESTSERVICEORDER");
		return comp;
	}

	private StrategyData createStrategyData() {
		StrategyData strategyData = new StrategyData();
		StrategySummary summary = new StrategySummary();
		summary.setId(STRATEGY_ID);
		summary.setSubmitDate(STRATEGY_SUBMIT_DATE);
		strategyData.setStrategySummary(summary);
		return strategyData;
	}
}
