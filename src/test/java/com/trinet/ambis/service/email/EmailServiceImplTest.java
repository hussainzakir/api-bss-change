package com.trinet.ambis.service.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.service.email.dto.ClientConversionRequestDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.EmailServiceHelper;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.DocManagementService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.dto.ClientConversionFailureEmailDto;
import com.trinet.ambis.service.email.impl.EmailServiceImpl;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.model.notification.Recipient;
import com.trinet.ambis.service.unit.ServiceUnitTest;


@RunWith(MockitoJUnitRunner.class)
public class EmailServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	EmailServiceImpl emailService;

	@Mock
	private EmailNotificationService emailNotificationService;

	@Mock
	private EmailAddressService emailAddressService;

	@Mock
	EmailGenService emailGenService;

	@Mock
	private DocManagementService docManagementService;

	@Mock
	private PersonService personService;

	@Mock
	private SubmitStatusService submitStatusService;

	@Mock
	PsDao psDao;

	@Mock
	private CompanyDao companyDao;

	private static final String USER_ID = "000022233390";
	private static final String COMP_CODE = "G48";
	private static final String CONFIRMATION_ID = "SADKJKAJDLJALDAD";
	private static final String SERVICE_ORDER_NUMBER = "12312323";
	private static final long REALM_PLYR_ID = 31;
	private static final long STRATEGY_ID = 1111;
	private static final StrategyData STRATEGY_DATA = prepareStrategyData();
	private static final String PAYLOAD = CommonServiceHelper.objectToJsonString(STRATEGY_DATA);
	private static final String HTML_EMAIL_CONTENT = "<HTML><BODY>Email Content</BODY></HTML>";
	private static final String RECIPIENT_NAME = "Sean";
	private static final Set<String> CLIENT_RECIPIENTS_EMAILS = new HashSet<>(Arrays.asList("sean@abc.com"));
	private static final Set<String> NON_CLIENT_RECIPIENTS_EMAILS = new HashSet<>(
			Arrays.asList("implementationteam@trinet.com"));

	private MockedStatic<EmailServiceHelper> mockStaticEmailServiceHelper;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		if (mockStaticEmailServiceHelper == null) {
			mockStaticEmailServiceHelper = Mockito.mockStatic(EmailServiceHelper.class);
		}

		Recipient clientRecipient = new Recipient();
		clientRecipient.setId("sean@abc.com");
		Recipient nonClientRecipient = new Recipient();
		nonClientRecipient.setId("implementationteam@trinet.com");

		List<Recipient> clientRecipients = new ArrayList<>(Arrays.asList(clientRecipient));
		List<Recipient> nonClientRecipients = new ArrayList<>(Arrays.asList(nonClientRecipient));

		when(EmailServiceHelper.prepareRecipients(CLIENT_RECIPIENTS_EMAILS)).thenReturn(clientRecipients);
		when(EmailServiceHelper.prepareRecipients(NON_CLIENT_RECIPIENTS_EMAILS))
				.thenReturn(nonClientRecipients);
	}

	@org.junit.After
	public void tearDown() {
		if (mockStaticEmailServiceHelper != null) {
			mockStaticEmailServiceHelper.close();
			mockStaticEmailServiceHelper = null;
		}
	}

	@Test
	public void uploadStatementAndSendConfirmation_testRecipientNameIsSetWhenAvailable() {
		Company company = new Company();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(RECIPIENT_NAME, submissionInfo.getEmailInfo().getRecipientName());
	}

	@Test
	public void uploadStatementAndSendConfirmation_testHtmlContentGenerationSuccess() {
		Company company = new Company();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
	}

	@Test
	public void uploadStatementAndSendConfirmation_testHtmlContentGenerationFails() {
		Company company = new Company();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenThrow(new RuntimeException());

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals("Email content should be null", null, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be ERROR", "ERROR",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		assertFalse("Client email send flag should be false", submissionInfo.getEmailInfo().isClientEmailSent());

		verify(docManagementService, times(0)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(0)).sendConfirmationEmail(any(NotificationRequestParam.class));
	}

	@Test
	public void uploadStatementAndSendConfirmation_testUploadSuccess() {
		Company company = new Company();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
	}

	@Test
	public void uploadStatementAndSendConfirmation_testUploadFail() {
		Company company = new Company();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(false);

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be ERROR", "ERROR",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
	}

	@Test
	public void uploadStatementAndSendConfirmation_testSendEmailSuccessfully() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
				.withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);
		ArgumentCaptor<NotificationRequestParam> notificationRequestParamArgCaptor = ArgumentCaptor
				.forClass(NotificationRequestParam.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);
		when(emailNotificationService.sendConfirmationEmail(notificationRequestParamArgCaptor.capture()))
				.thenReturn(new NotificationRequestParam());

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(1)).sendConfirmationEmail(clientRequest);
		verify(emailNotificationService, times(1)).sendConfirmationEmail(nonClientRequest);
		assertEquals("sean@abc.com", submissionInfo.getEmailInfo().getClientRecipients().get(0).getId());
		assertEquals(1, submissionInfo.getEmailInfo().getNonClientRecipients().size());
		assertTrue("isClientEmailSent flag should be true", submissionInfo.getEmailInfo().isClientEmailSent());
	}

	@Test
	public void uploadStatementAndSendConfirmation_testSendEmailFails() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
				.withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);
		when(emailNotificationService.sendConfirmationEmail(clientRequest)).thenThrow(new RuntimeException());

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(2)).sendConfirmationEmail(any(NotificationRequestParam.class));
		assertFalse("isClientEmailSent flag should be false", submissionInfo.getEmailInfo().isClientEmailSent());
	}
	
	@Test
	public void uploadStatementAndSendConfirmation_testNoClientEmailAvailable() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(true).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
				.withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.prepareRecipients(CLIENT_RECIPIENTS_EMAILS)).thenReturn(null);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(1)).sendConfirmationEmail(any(NotificationRequestParam.class));
		assertFalse("isClientEmailSent flag should be false", submissionInfo.getEmailInfo().isClientEmailSent());
	}

	@Test
	public void uploadStatementAndSendConfirmation_testUploadWhenResubmitAndStatusIsSuccessAndSendEmailTrue() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		boolean sendEmail = true;
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(sendEmail).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPostSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);

		// Validate test data.
		assertTrue(submissionInfo.isResubmit());
		assertTrue(submissionInfo.getEmailInfo().isSendClientEmail());
		assertEquals("SUCCESS", submissionInfo.getSubmitStatusInfo().getSubmitStatus());

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		// Email should be sent for internal colleauges.
		verify(emailNotificationService, times(2)).sendConfirmationEmail(any(NotificationRequestParam.class));
	}

	@Test
	public void uploadStatementAndSendConfirmation_testUploadWhenResubmitAndStatusIsSuccessAndSendEmailFalse() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		boolean sendEmail = false;
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(sendEmail).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPostSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
//		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
//				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
//		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
//				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);

		// Validate test data.
		assertTrue(submissionInfo.isResubmit());
		assertFalse(submissionInfo.getEmailInfo().isResendEmail());
		assertEquals("SUCCESS", submissionInfo.getSubmitStatusInfo().getSubmitStatus());

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(0)).sendConfirmationEmail(any(NotificationRequestParam.class));
	}

	@Test
	public void uploadStatementAndSendConfirmation_testUploadWhenResubmitAndStatusIsErrorAndSendEmailTrue() {
		Company company = new Company();
		boolean sendEmail = true;
		String SUBMIT_STATUS = "ERROR";
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(SUBMIT_STATUS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(sendEmail).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPostSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
//		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
//				.thenReturn(true);
//		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
//		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
//				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);

		// Validate test data.
		assertTrue(submissionInfo.isResubmit());
		assertEquals("ERROR", submissionInfo.getSubmitStatusInfo().getSubmitStatus());

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "ERROR",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(0)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(1)).sendConfirmationEmail(any(NotificationRequestParam.class));
	}

	@Test
	public void uploadStatementAndSendConfirmation_testEmailSentAndUploadStatusIsErrorAndSendEmailTrue() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		boolean sendEmail = true;
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(sendEmail).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPostSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(false);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
//		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
//				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);

		// Validate test data.
		assertTrue(submissionInfo.getEmailInfo().isSendClientEmail());

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "ERROR",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(1)).sendConfirmationEmail(any(NotificationRequestParam.class));
	}

	@Test
	public void uploadStatementAndSendConfirmation_testResubmitDoNotSendNonClientEmailIfTriNetComp() {
		Company company = new Company();
		company.setCode("001");
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put("001", 1);
		boolean sendEmail = true;
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(sendEmail).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().resubmit(true).withSubmitStatusInfo()
				.submitStatus(submitStatus).buildSubmissionInfo().buildPostSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailGenService.getAdminEmailCount("001")).thenReturn(bdmCount);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
//		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
//				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(1)).sendConfirmationEmail(any(NotificationRequestParam.class));
		verify(emailNotificationService, times(1)).sendConfirmationEmail(clientRequest);
	}

	/********
	 * 
	 * DEFAULT SUBMIT TEST CASES
	 */
	
	@Test
	public void uploadStatementAndSendConfirmation_testGetBDMCountsFromSubmissionInfoForDefaultSubmit() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		boolean sendEmail = true;
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(null).updateTime(null).sendEmail(sendEmail).build();

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().defaultSubmit(true).withEmailInfo()
				.bdmCounts(bdmCount).buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPostSubmit();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(company, submissionInfo))
				.thenReturn(clientRequest);
		when(EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo))
				.thenReturn(nonClientRequest);

		emailService.uploadStatementAndSendConfirmation(company, submissionInfo);

		assertEquals(HTML_EMAIL_CONTENT, submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody());
		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(anyString(), any(Company.class),
				anyString());
		verify(emailNotificationService, times(1)).sendConfirmationEmail(nonClientRequest);
		verify(emailNotificationService, times(1)).sendConfirmationEmail(clientRequest);
	}

	/********
	 * 
	 * RESEND EMAIL TEST CASES
	 */

	@Test
	public void resendConfirmationEmail_testResendEmailUploadWhenCurrentUploadStatusIsError() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		String currentStatementUploadStatus = "ERROR";
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(currentStatementUploadStatus).updateTime(null).sendEmail(true).build();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(submitStatusService.findByConfirmationNumber(company.getCode(), CONFIRMATION_ID)).thenReturn(submitStatus);
		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
				.thenReturn(true);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(any(Company.class), any(SubmissionInfo.class)))
				.thenReturn(clientRequest);
		when(
				EmailServiceHelper.createNonClientNotificationRequest(any(Company.class), any(SubmissionInfo.class)))
				.thenReturn(nonClientRequest);

		// validate test data
		assertEquals("ERROR", submitStatus.getStatementUploadStatus());
		assertFalse(submitStatus.getEmailSentStatus());

		emailService.resendConfirmationEmail(company, CONFIRMATION_ID);

		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submitStatus.getStatementUploadStatus());
		assertTrue("Email should be sent", submitStatus.getEmailSentStatus());
		verify(docManagementService, times(1)).uploadConfirmationStatement(HTML_EMAIL_CONTENT, company,
				CONFIRMATION_ID);
		verify(emailNotificationService, times(2)).sendConfirmationEmail(any(NotificationRequestParam.class));
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(submitStatus);
	}

	@Test
	public void uploadStatementAndSendConfirmation_testResendEmailUploadWhenCurrentUploadStatusIsSuccess() {
		Company company = new Company();
		company.setCode(COMP_CODE);
		String currentStatementUploadStatus = "SUCCESS";
		Map<String, Integer> bdmCount = new HashMap<>();
		bdmCount.put(COMP_CODE, 1);
		NotificationRequestParam clientRequest = new NotificationRequestParam();
		NotificationRequestParam nonClientRequest = new NotificationRequestParam();

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus(currentStatementUploadStatus).updateTime(null).sendEmail(true).build();

		ArgumentCaptor<StrategyData> strategyDataArgCaptor = ArgumentCaptor.forClass(StrategyData.class);

		when(submitStatusService.findByConfirmationNumber(company.getCode(), CONFIRMATION_ID)).thenReturn(submitStatus);
		when(personService.prepareNameForConfirmationEmail(USER_ID, company)).thenReturn(RECIPIENT_NAME);
		when(emailGenService.generateBssConfirmationStatementHtml(strategyDataArgCaptor.capture(), eq(CONFIRMATION_ID),
				eq(company), eq(RECIPIENT_NAME), eq(SERVICE_ORDER_NUMBER))).thenReturn(HTML_EMAIL_CONTENT);
//		when(docManagementService.uploadConfirmationStatement(HTML_EMAIL_CONTENT, company, CONFIRMATION_ID))
//				.thenReturn(true);
		when(emailGenService.getAdminEmailCount(COMP_CODE)).thenReturn(bdmCount);
		when(emailAddressService.getConfirmationStatementClientRecipients(company, USER_ID))
				.thenReturn(CLIENT_RECIPIENTS_EMAILS);
		when(emailAddressService.getConfirmationStatementNonClientRecipients(company))
				.thenReturn(NON_CLIENT_RECIPIENTS_EMAILS);
		when(EmailServiceHelper.createClientNotificationRequest(any(Company.class), any(SubmissionInfo.class)))
				.thenReturn(clientRequest);
		when(
				EmailServiceHelper.createNonClientNotificationRequest(any(Company.class), any(SubmissionInfo.class)))
				.thenReturn(nonClientRequest);

		// Validate test data.
		assertEquals("SUCCESS", submitStatus.getStatementUploadStatus());
		assertFalse(submitStatus.getEmailSentStatus());

		emailService.resendConfirmationEmail(company, CONFIRMATION_ID);

		assertEquals("Confirmation statement upload status should be SUCCESS", "SUCCESS",
				submitStatus.getStatementUploadStatus());
		assertTrue("Email should be sent", submitStatus.getEmailSentStatus());
		verify(docManagementService, times(0)).uploadConfirmationStatement(HTML_EMAIL_CONTENT, company,
				CONFIRMATION_ID);
		verify(emailNotificationService, times(2)).sendConfirmationEmail(any(NotificationRequestParam.class));
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(submitStatus);
	}

	@Test
	public void uploadStatementAndSendConfirmation_testResendEmailWithInvalidConfirmationId() {
		Company company = new Company();
		company.setCode(COMP_CODE);

		when(submitStatusService.findByConfirmationNumber(company.getCode(), CONFIRMATION_ID)).thenReturn(null);

		try {
			emailService.resendConfirmationEmail(company, CONFIRMATION_ID);
		} catch (Exception e) {
			assertEquals("java.lang.RuntimeException", e.getClass().getName());
			assertEquals(String.format("No submit record found for company %s and Confirmation number %s",
					company.getCode(), CONFIRMATION_ID), e.getMessage());
		}

	}

	@Test
	public void uploadStatementAndSendConfirmation_testResendEmailUpdateStatusEvenWhenExceptionOccurs() {
		Company company = new Company();
		company.setCode(COMP_CODE);

		SubmitPayload submitPayload = SubmitPayload.builder().payload(PAYLOAD).build();
		SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
				.status(BSSApplicationConstants.SUCCESS).submitError(null).submitPayload(submitPayload)
				.createTime(new Date()).userId(USER_ID).company(COMP_CODE).emailSentStatus(false)
				.confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
				.statementUploadStatus("ERROR").updateTime(null).sendEmail(true).build();

		when(submitStatusService.findByConfirmationNumber(company.getCode(), CONFIRMATION_ID)).thenReturn(submitStatus);
		when(personService.prepareNameForConfirmationEmail(anyString(), any(Company.class)))
				.thenThrow(new RuntimeException());

		emailService.resendConfirmationEmail(company, CONFIRMATION_ID);

		assertEquals("Confirmation statement upload status should be SUCCESS", "ERROR",
				submitStatus.getStatementUploadStatus());
		assertFalse("Email should be sent", submitStatus.getEmailSentStatus());
		verify(submitStatusService, times(1)).createUpdateSubmitStatus(submitStatus);
	}

	private static StrategyData prepareStrategyData() {
		StrategyData strategyData = new StrategyData();
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId(STRATEGY_ID);
		strategyData.setStrategySummary(strategySummary);
		return strategyData;
	}
}
