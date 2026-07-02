package com.trinet.ambis.service.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.security.util.SecurityUtils;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import com.trinet.ambis.common.ApiBssPropertiesConstants;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BSSProcessTypeToSNFeatureMapping;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.SubmitStatusDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RealmWaitPeriodService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.dto.ClientConversionFailureEmailDto;
import com.trinet.ambis.service.email.dto.CompanyAndConfNumberDto;
import com.trinet.ambis.service.email.dto.StrategySubmissionFailureDto;
import com.trinet.ambis.service.email.dto.SubmissionEmailDto;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.service.email.impl.EmailGenServiceImpl;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.FundingBasePlan;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.SelectItem;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySubmitIssueReport;
import com.trinet.ambis.service.model.StrategySubmitIssueReport.Bdm;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.model.notification.NotificationMessage;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.model.notification.Recipient;
import com.trinet.ambis.service.unit.RealmWaitPeriodServiceImplTest;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.CommonUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author jshuali
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class EmailGenServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	EmailGenServiceImpl emailGenService;

	@Mock
	EmailGenServiceImpl emailGenServiceImplMock;

	@Mock
	CompanyService companyService;

	@Mock
	EmailNotificationService emailNotificationService;

	@Mock
	SubmitStatusService submitStatusService;

	@Mock
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	@Mock
	@Qualifier("realmWaitPeriodServiceImpl")
	RealmWaitPeriodService waitPeriodService;

	@Mock
	PsDao psDao;

	@Mock
	HrpDao hrpDao;

	@Mock
	SubmitStatusDao submitStatusDao;

	@Mock
	SchedMidYearFundingDao schedMidYearFundingDao;

	@Mock
	CommonDataDao commonDataDao;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	Configuration configuration;

	@Mock
	Template template;

	@Captor
	private ArgumentCaptor<NotificationRequestParam> notificationRequestParamCaptor;

	private static final String COMPANY_CODE = "TEST";
	private static final String STRATEGY_NAME = "STRATEGY NAME";
	private static final String EMPL_ID = "0000000123456";
	private static final String EMPL_FIRST_NAME = "FIRSTNAME";
	private static final String USER_ID = "TESTUSER";
	private static final String CONFIRMATION_NUMBER = "CONFIRMATION";
	private static final String PROSPECT_ID="PROSPECT_ID";
	private static final long REALM_YEAR_ID = 10;
	private static final String GRP1_WAIT_PERIOD_ID = "EX90";
	private Map<String, Integer> bdmCount = new HashMap<>();
	private static final int MONTHLY_BUDGET_FACTOR = 1;
    private MockedStatic<RandomStringUtils> mockStaticRandomStringUtils;
    private MockedStatic<CommonServiceHelper> mockStaticCommonServiceHelper;
    private MockedStatic<SecurityUtils> mockStaticSecurityUtils;
    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;
    private MockedStatic<CommonUtils> mockStaticCommonUtils;

    @Before
    public void setUp() {
        mockStaticRandomStringUtils = Mockito.mockStatic(RandomStringUtils.class);
        mockStaticCommonServiceHelper = Mockito.mockStatic(CommonServiceHelper.class);
        mockStaticSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
        mockStaticCommonUtils = Mockito.mockStatic(CommonUtils.class);
        bdmCount.put(COMPANY_CODE, 1);
        try {
            Configuration mockConfiguration = Mockito.mock(Configuration.class);
            Template mockTemplate = Mockito.mock(Template.class);
            when(mockConfiguration.getTemplate(anyString(), anyString())).thenReturn(mockTemplate);
            Mockito.doAnswer(invocation -> {
                StringWriter writer = invocation.getArgument(1);
                writer.write("Mock FreeMarker template output");
                return null;
            }).when(mockTemplate).process(any(), any(StringWriter.class));
            ReflectionTestUtils.setField(emailGenService, "configuration", mockConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    @After
    public void tearDown() {
        if (mockStaticRandomStringUtils != null) {
            mockStaticRandomStringUtils.close();
            mockStaticRandomStringUtils = null;
        }
        if (mockStaticCommonServiceHelper != null) {
            mockStaticCommonServiceHelper.close();
            mockStaticCommonServiceHelper = null;
        }
        if (mockStaticSecurityUtils != null) {
            mockStaticSecurityUtils.close();
            mockStaticSecurityUtils = null;
        }
        if (mockStaticBSSMessageConfig != null) {
            mockStaticBSSMessageConfig.close();
            mockStaticBSSMessageConfig = null;
        }
        if (mockStaticCommonUtils != null) {
            mockStaticCommonUtils.close();
            mockStaticCommonUtils = null;
        }
    }

    @Ignore
	@Test
	public void createSubmitEmail() throws IOException {
		Company company = prepareCompany();
		List<String> assignmentAddress = new ArrayList<>();
		assignmentAddress.add("assignment_email1@trinet.com");

		when(psDao.getEmployeeFirstName(USER_ID)).thenReturn(EMPL_FIRST_NAME);
		when(hrpDao.getBDMEmails(COMPANY_CODE))
				.thenReturn(new HashSet<>(Arrays.asList("bdm_email1@trinet.com", "bdm_email2@trinet.com")));
		when(hrpDao.getEmplEmail(COMPANY_CODE, USER_ID)).thenReturn("emp_email1@trinet.com");
		when(psDao.getAssignmentAddresses(company)).thenReturn(assignmentAddress);
		when(commonDataDao.getBsuppVolPlanTypes(REALM_YEAR_ID)).thenReturn(prepareBSuppVolPlanTypes());
		when(realmDataDao.getRealmFundingTypes(REALM_YEAR_ID)).thenReturn(prepareFundingTypes());
		when(waitPeriodService.getWaitPeriodDescr()).thenReturn( RealmWaitPeriodServiceImplTest.generateWaitPeriodMap() );
		when(hrpDao.getCovrgCdMap()).thenReturn(prepareCoverageCodes());
		when(configuration.getTemplate(Mockito.anyString(), Mockito.anyString())).thenReturn(template);

		// emailGenService.createSubmitEmail(dto, USER_ID, uniqueId, company,
		// SERVICE_ORDER_NUMBER, status, resubmitFlag);
	}

	@Test
	public void createSupportEmail_doNotSendToBSS() {
		ArgumentCaptor<NotificationRequestParam> notificationRequestParamArgCaptor = ArgumentCaptor
				.forClass(NotificationRequestParam.class);

		when(emailNotificationService.sendConfirmationEmail(notificationRequestParamArgCaptor.capture()))
				.thenReturn(null);

		SupportEmailDto benefitStrategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
				.companyCode(COMPANY_CODE)
				.confirmationNumber(CONFIRMATION_NUMBER)
				.userId(EMPL_ID)
				.sendToBSS(false)
				.build();
		emailGenService.createSupportEmail(benefitStrategySubmissionFailureDto);

		assertEquals(1,
				notificationRequestParamArgCaptor.getValue().getNotificationMessages().get(0).getRecipients().size());
	}

	@Test
	public void createSupportEmail_clientConversionFailure_sendToBSS() {
		ArgumentCaptor<NotificationRequestParam> notificationRequestParamArgCaptor = ArgumentCaptor
				.forClass(NotificationRequestParam.class);

		when(emailNotificationService.sendConfirmationEmail(notificationRequestParamArgCaptor.capture()))
				.thenReturn(null);

		SupportEmailDto benefitStrategySubmissionFailureDto = ClientConversionFailureEmailDto.builder()
				.companyCode(COMPANY_CODE)
				.userId(EMPL_ID)
				.sendToBSS(true)
				.build();
		emailGenService.createSupportEmail(benefitStrategySubmissionFailureDto);

		assertEquals(2,
				notificationRequestParamArgCaptor.getValue().getNotificationMessages().get(0).getRecipients().size());
	}

	@Test
	public void createSupportEmail_clientConversionFailure_doNotSendToBSS() {
		ArgumentCaptor<NotificationRequestParam> notificationRequestParamArgCaptor = ArgumentCaptor
				.forClass(NotificationRequestParam.class);

		when(emailNotificationService.sendConfirmationEmail(notificationRequestParamArgCaptor.capture()))
				.thenReturn(null);

		SupportEmailDto benefitStrategySubmissionFailureDto = ClientConversionFailureEmailDto.builder()
				.companyCode(COMPANY_CODE)
				.userId(EMPL_ID)
				.sendToBSS(false)
				.build();
		emailGenService.createSupportEmail(benefitStrategySubmissionFailureDto);

		assertEquals(1,
				notificationRequestParamArgCaptor.getValue().getNotificationMessages().get(0).getRecipients().size());
	}


	@Test(expected = IllegalStateException.class)
	public void createSupportEmail_confirmationNumberMissing() {
		SupportEmailDto benefitStrategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
				.companyCode(COMPANY_CODE)
				.userId(EMPL_ID)
				.sendToBSS(false)
				.build();
		emailGenService.createSupportEmail(benefitStrategySubmissionFailureDto);
	}

	@Test(expected = IllegalStateException.class)
	public void createSupportEmail_companyCodeMissing() {
		SupportEmailDto benefitStrategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
				.confirmationNumber(CONFIRMATION_NUMBER)
				.userId(EMPL_ID)
				.sendToBSS(false)
				.build();
		emailGenService.createSupportEmail(benefitStrategySubmissionFailureDto);
	}

	@Test
	public void createSupportEmail_sendToBSS() {
		ArgumentCaptor<NotificationRequestParam> notificationRequestParamArgCaptor = ArgumentCaptor
				.forClass(NotificationRequestParam.class);

		when(emailNotificationService.sendConfirmationEmail(notificationRequestParamArgCaptor.capture()))
				.thenReturn(null);

		SupportEmailDto benefitStrategySubmissionFailureDto = StrategySubmissionFailureDto.builder()
				.companyCode(COMPANY_CODE)
				.confirmationNumber(CONFIRMATION_NUMBER)
				.userId(EMPL_ID)
				.sendToBSS(true)
				.build();
		emailGenService.createSupportEmail(benefitStrategySubmissionFailureDto);

		assertEquals(2,
				notificationRequestParamArgCaptor.getValue().getNotificationMessages().get(0).getRecipients().size());
		assertEquals("BSS-Team@trinet.com", (notificationRequestParamArgCaptor.getValue().getNotificationMessages()
				.get(0).getRecipients().get(1).getId()));
	}

	@Test
	public void createDefaultEmail() {
		ArgumentCaptor<NotificationRequestParam> clientRequestParamArgCaptor = ArgumentCaptor
				.forClass(NotificationRequestParam.class);

		when(emailNotificationService.sendConfirmationEmail(clientRequestParamArgCaptor.capture())).thenReturn(null);

		emailGenService.createDefaultEmail(4, EMPL_ID);
	}

	@Test
	public void createPreLoadEmail() {
		ArgumentCaptor<NotificationRequestParam> clientRequestParamArgCaptor = ArgumentCaptor
				.forClass(NotificationRequestParam.class);

		when(emailNotificationService.sendConfirmationEmail(clientRequestParamArgCaptor.capture())).thenReturn(null);

		emailGenService.createPreLoadEmail(5, COMPANY_CODE, EMPL_ID);
	}

	@Test
	public void createUnsentStrategyEmailsNotification() {
		StrategySubmitIssueReport report = new StrategySubmitIssueReport();
		Bdm bdm = report.new Bdm();
		Bdm bdm1 = report.new Bdm();
		report.setSubmittedByBdm(true);
		report.setBdms(Arrays.asList(bdm, bdm1));

		when(strategyDataDao.getSubmittedStrategyIssueReportData()).thenReturn(Arrays.asList(report));

		emailGenService.generateSubmissionIssueReport(COMPANY_CODE, EMPL_ID);
	}

    @Ignore
	@Test
	public void resendConfirmationEmailTest1() throws IOException {
		Company company = prepareCompany();
		company.setRenewalCompany(false);
		SubmitPayload submitPayload = SubmitPayload.builder().payload("").build();
		submitPayload.setPayload("");
		SubmitStatus submitStatus = SubmitStatus.builder().submitPayload(submitPayload).userId(EMPL_ID)
				.confirmationNumber(CONFIRMATION_NUMBER).build();
		SchedMidYearFunding schedMidYrFunding = new SchedMidYearFunding();
		schedMidYrFunding.setServiceOrderNumber("1111");
		Map<String, Integer> bdmCounts = new HashMap<>();
		bdmCounts.put("G48", 2);
		bdmCounts.put("CQT", 4);
		StrategyData strategyData = prepareStrategyDataJson();
		strategyData.getStrategySummary().setTotalBudget(null);

		mockStatic(CommonServiceHelper.class);

		ArgumentCaptor<SubmitStatus> submitStatusCaptor = ArgumentCaptor.forClass(SubmitStatus.class);

		when(submitStatusDao.findByCompanyAndConfirmationNumber("TEST", CONFIRMATION_NUMBER)).thenReturn(submitStatus);
		when(schedMidYearFundingDao.getActiveSchedMidYearFundingByCompanyId(company.getId()))
				.thenReturn(schedMidYrFunding);
		when(CommonServiceHelper.jsonToObject("", StrategyData.class)).thenReturn(strategyData);

		/** Mocking for createSubmitEmail() **/
		List<String> newClientAddresses = new ArrayList<>();
		newClientAddresses.add("assignment_email1@trinet.com");

		when(psDao.getEmployeeFirstName(USER_ID)).thenReturn(EMPL_FIRST_NAME);
		when(hrpDao.getBDMEmails(COMPANY_CODE))
				.thenReturn(new HashSet<>(Arrays.asList("bdm_email1@trinet.com", "bdm_email2@trinet.com")));
		when(hrpDao.getEmplEmail(COMPANY_CODE, USER_ID)).thenReturn("emp_email1@trinet.com");
		when(psDao.getNewClientAddresses(company)).thenReturn(newClientAddresses);
		when(commonDataDao.getBsuppVolPlanTypes(REALM_YEAR_ID)).thenReturn(prepareBSuppVolPlanTypes());
		when(realmDataDao.getRealmFundingTypes(REALM_YEAR_ID)).thenReturn(prepareFundingTypes());
		when(waitPeriodService.getWaitPeriodDescr()).thenReturn( RealmWaitPeriodServiceImplTest.generateWaitPeriodMap() );
		when(hrpDao.getCovrgCdMap()).thenReturn(prepareCoverageCodes());
		when(configuration.getTemplate(Mockito.anyString(), Mockito.anyString())).thenReturn(template);
		/** Mocking for createSubmitEmail() ends **/

		when(hrpDao.getBDMCount(Arrays.asList(COMPANY_CODE))).thenReturn(bdmCounts);
		when(submitStatusService.createUpdateSubmitStatus(submitStatusCaptor.capture())).thenReturn(null);

	}


	@Test
	public void generateBssConfirmationStatementHtmlTest() throws IOException {
		Map<String,String> configs = new HashMap<>();
		configs.put( "CONFIRM_DISPLAY_ALL_PLANS", "true" );
		when( realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId( ArgumentMatchers.anyLong() ) ).thenReturn( configs );

		emailGenService.generateBssConfirmationStatementHtml( prepareStrategyData(), CONFIRMATION_NUMBER, prepareCompany(), EMPL_FIRST_NAME, COMPANY_CODE );
		verify( realmPlanYearRuleConfigService, times(2) ).getRulesAndConfigsByRealmPlanYearId( ArgumentMatchers.anyLong() );

	}

	@Test
	public void sendBssSubmissionFailureEmailForSingleClientsSubmitFeatureTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.SUBMIT.getBssProcessType());
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
				.thenReturn("BSS Submission Failure - Company %s - %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
				.thenReturn("BSS Submission Failure - Default for Quarter %s");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals(submissionEmailDto.getUserId(), notificationMessage.getEmployeeId());
		assertEquals("G48", notificationMessage.getCompanyId());
		assertEquals("BSS Submission Failure - Company G48 - G48 LLC", notificationMessage.getSubject());
		assertEquals(
				"An error was encountered while submitting to PeopleSoft for the selected Benefit Strategy.<BR><BR>Company - Confirmation Number<BR><BR>G48 - 1MPZEYY6S5SY<BR><BR>Feature: Submit<BR><BR>",
				notificationMessage.getHtmlMsg());
	}

	@Test
	public void sendBssSubmissionFailureEmailForSingleClientsRESubmitFeatureTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.RESUBMIT.getBssProcessType());
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
				.thenReturn("BSS Submission Failure - Company %s - %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
				.thenReturn("BSS Submission Failure - Default for Quarter %s");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals(submissionEmailDto.getUserId(), notificationMessage.getEmployeeId());
		assertEquals("G48", notificationMessage.getCompanyId());
		assertEquals("BSS Submission Failure - Company G48 - G48 LLC", notificationMessage.getSubject());
		assertEquals(
				"An error was encountered while submitting to PeopleSoft for the selected Benefit Strategy.<BR><BR>Company - Confirmation Number<BR><BR>G48 - 1MPZEYY6S5SY<BR><BR>Feature: Resubmit<BR><BR>",
				notificationMessage.getHtmlMsg());
	}

	@Test
	public void sendBssSubmissionFailureEmailForSingleClientsSubmitBandChangeFeatureTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.BANDCODE_RESUBMIT.getBssProcessType());
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
				.thenReturn("BSS Submission Failure - Company %s - %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
				.thenReturn("BSS Submission Failure - Default for Quarter %s");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals(submissionEmailDto.getUserId(), notificationMessage.getEmployeeId());
		assertEquals("G48", notificationMessage.getCompanyId());
		assertEquals("BSS Submission Failure - Company G48 - G48 LLC", notificationMessage.getSubject());
		assertEquals(
				"An error was encountered while submitting to PeopleSoft for the selected Benefit Strategy.<BR><BR>Company - Confirmation Number<BR><BR>G48 - 1MPZEYY6S5SY<BR><BR>Feature: Band Change Submit<BR><BR>",
				notificationMessage.getHtmlMsg());
	}

	@Test
	public void sendBssSubmissionFailureEmailForSingleClientsTermedDefaultFeatureTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.TERM_DEFAULT.getBssProcessType());
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
				.thenReturn("BSS Submission Failure - Company %s - %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
				.thenReturn("BSS Submission Failure - Default for Quarter %s");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals(submissionEmailDto.getUserId(), notificationMessage.getEmployeeId());
		assertEquals("G48", notificationMessage.getCompanyId());
		assertEquals("BSS Submission Failure - Company G48 - G48 LLC", notificationMessage.getSubject());
		assertEquals(
				"An error was encountered while submitting to PeopleSoft for the selected Benefit Strategy.<BR><BR>Company - Confirmation Number<BR><BR>G48 - 1MPZEYY6S5SY<BR><BR>Feature: Termed Default Submit<BR><BR>",
				notificationMessage.getHtmlMsg());
	}

	@Test
	public void sendBssSubmissionFailureEmailForSingleClientsDefaultFeatureTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.DEFAULT_SUBMIT.getBssProcessType());
		
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
		.thenReturn("BSS Submission Failure - Company %s - %s");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals(submissionEmailDto.getUserId(), notificationMessage.getEmployeeId());
		assertEquals("G48", notificationMessage.getCompanyId());
		assertEquals("BSS Submission Failure - Company G48 - G48 LLC", notificationMessage.getSubject());
		assertEquals(
				"An error was encountered while submitting to PeopleSoft for the selected Benefit Strategy.<BR><BR>Company - Confirmation Number<BR><BR>G48 - 1MPZEYY6S5SY<BR><BR>Feature: Default Submit<BR><BR>",
				notificationMessage.getHtmlMsg());
	}


	@Test
	public void createSyncFailureEmailTest() {
		// given
		String company = "G9P";
		UUID uid = UUID.fromString( "12345678-aaaa-bbbb-ffff-123456789abc" );

		when( BSSMessageConfig.getProperty( ApiBssPropertiesConstants.SUPPORT_ADDRESS_PROPERTY )).thenReturn( "bss-support@devEmail.com" );
		when( BSSMessageConfig.getProperty( ApiBssPropertiesConstants.REAL_TIME_SYNC_FAILURE_SUBJECT ))
				.thenReturn( "SYNC FAILURE SUBJECT %s" );

		// when
		emailGenService.createSyncFailureEmail( company, uid );

		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals( company, notificationMessage.getCompanyId() );
		assertEquals( String.format( "SYNC FAILURE SUBJECT %s", company ), notificationMessage.getSubject() );
		assertEquals(
				"An error was encountered while performing real-time sync for company G9P<BR><BR>Log identifier:<pre style=\"margin-top: 1px;\">12345678-aaaa-bbbb-ffff-123456789abc</pre>",
				notificationMessage.getHtmlMsg());
	}


	@Test
	public void createSyncFailureExceptionTest() {
		// given
		String company = "G9P";
		UUID uid = UUID.fromString( "12345678-aaaa-bbbb-ffff-123456789abc" );

		when( BSSMessageConfig.getProperty( ApiBssPropertiesConstants.SUPPORT_ADDRESS_PROPERTY )).thenThrow( BSSApplicationException.class );

		// when
		emailGenService.createSyncFailureEmail( company, uid );

		// then
		verify(CommonUtils.class);
		CommonUtils.logExceptions(any(), any(), anyString(), anyString());
	}


	@Test
	public void createSyncFailureEmailNoUIDTest() {
		// given
		String company = "G9P";

		when( BSSMessageConfig.getProperty( ApiBssPropertiesConstants.SUPPORT_ADDRESS_PROPERTY )).thenReturn( "bss-support@devEmail.com" );
		when( BSSMessageConfig.getProperty( ApiBssPropertiesConstants.REAL_TIME_SYNC_FAILURE_SUBJECT ))
				.thenReturn( "SYNC FAILURE SUBJECT %s" );

		// when
		emailGenService.createSyncFailureEmail( company, null );

		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals( company, notificationMessage.getCompanyId() );
		assertEquals( String.format( "SYNC FAILURE SUBJECT %s", company ), notificationMessage.getSubject() );
		assertEquals(
				"An error was encountered while performing real-time sync for company G9P<BR><BR>",
				notificationMessage.getHtmlMsg());
	}


	@Test
	public void sendBssSubmissionFailureEmailForMultipleClientsDefaultFeatureTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforMultipleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.DEFAULT_SUBMIT.getBssProcessType());
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
		.thenReturn("BSS Submission Failure - Default for Quarter %s");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		assertEquals(submissionEmailDto.getUserId(), notificationMessage.getEmployeeId());
		//assertEquals("G48", notificationMessage.getCompanyId());
		assertEquals("BSS Submission Failure - Default for Quarter 8Y", notificationMessage.getSubject());
		assertEquals(
				"An error was encountered while submitting to PeopleSoft for the selected Benefit Strategy.<BR><BR>Company - Confirmation Number<BR><BR>G48 - 1MPZEYY6S5SY<BR>G5Z - 1MPZEYY6S5SPY<BR><BR>Feature: Default Submit<BR><BR>",
				notificationMessage.getHtmlMsg());
	}


	@Test
	public void sendBssSubmissionFailureSendToBssTeamTrueTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.SUBMIT.getBssProcessType());
		submissionEmailDto.setSendToBssTeam(Boolean.TRUE);
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
				.thenReturn("BSS Submission Failure - Company %s - %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
				.thenReturn("BSS Submission Failure - Default for Quarter %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.BSS_SUBMISSION_FAILURE_TO_ADDRESS))
				.thenReturn("tndev@service-now.com");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.BSS_TEAM_TO_ADDRESS))
				.thenReturn("BSS-Team@trinet.com");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		List<Recipient> recipients = notificationMessage.getRecipients();
		assertNotNull(recipients);
		assertEquals(2, recipients.size());
		assertTrue(recipients.stream().filter(recipient -> recipient.getId().equalsIgnoreCase("BSS-Team@trinet.com"))
				.findFirst().isPresent());
	}
	

	@Test
	public void sendBssSubmissionFailureSendToBssTeamFalseTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setBssProcessType(BSSProcessTypeToSNFeatureMapping.SUBMIT.getBssProcessType());
		submissionEmailDto.setSendToBssTeam(Boolean.FALSE);
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
				.thenReturn("BSS Submission Failure - Company %s - %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
				.thenReturn("BSS Submission Failure - Default for Quarter %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.BSS_SUBMISSION_FAILURE_TO_ADDRESS))
				.thenReturn("tndev@service-now.com");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(emailNotificationService, times(1)).sendConfirmationEmail(notificationRequestParamCaptor.capture());
		NotificationRequestParam notificationRequestParam = notificationRequestParamCaptor.getValue();
		NotificationMessage notificationMessage = notificationRequestParam.getNotificationMessages().get(0);
		List<Recipient> recipients = notificationMessage.getRecipients();
		assertNotNull(recipients);
		assertEquals(1, recipients.size());
		assertFalse(recipients.stream().filter(recipient -> recipient.getId().equalsIgnoreCase("BSS-Team@trinet.com"))
				.findFirst().isPresent());
	}


	@Test
	public void sendBssSubmissionFailureEmailTryCatchTest() {
		// given
		SubmissionEmailDto submissionEmailDto = prepareSubmissionEmailDtoforSingleClients();
		submissionEmailDto.setCompanyAndConfNumberDtos(null);
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT))
				.thenReturn("BSS Submission Failure - Company %s - %s");
		when(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER))
				.thenReturn("BSS Submission Failure - Default for Quarter %s");
		// when
		emailGenService.sendBssSubmissionFailureEmail(submissionEmailDto);
		// then
		verify(CommonUtils.class);
		CommonUtils.logExceptions(any(), any(), anyString(), anyString());
	}


	private List<FundingType> prepareFundingTypes() {
		List<FundingType> fundingTypes = new ArrayList<>();
		FundingType fundingType = new FundingType();
		fundingType.setDefaultFunding(true);
		fundingType.setId(BSSApplicationConstants.BFPCT);
		fundingType.setDescription("Base Plan Percent");
		fundingTypes.add(fundingType);
		fundingType = new FundingType();
		fundingType.setDefaultFunding(false);
		fundingType.setId(BSSApplicationConstants.CFPCT);
		fundingType.setDescription("Base Plan Percent");
		fundingTypes.add(fundingType);
		fundingType = new FundingType();
		fundingType.setDefaultFunding(false);
		fundingType.setId("FLT");
		fundingType.setDescription("Flat");
		fundingTypes.add(fundingType);
		return fundingTypes;
	}

	private List<SelectItem> prepareBSuppVolPlanTypes() {
		List<SelectItem> selectItems = new ArrayList<>(3);
		SelectItem selectItem = new SelectItem();
		selectItem.setId("XX");
		selectItem.setDescription("All Plans");
		selectItems.add(selectItem);
		selectItem = new SelectItem();
		selectItem.setId("1D");
		selectItem.setDescription("Dental (Optional)");
		selectItems.add(selectItem);
		selectItem = new SelectItem();
		selectItem.setId("1V");
		selectItem.setDescription("Vision (Optional)");
		selectItems.add(selectItem);
		return selectItems;
	}

	private Company prepareCompany() {
		Company company = new Company();
		Realm realm = new Realm();
		realm.setPeoid("PAS");
		realm.setId(1L);
		realm.setBenExchange("TriNet III");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(31);
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());

		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setCloseDate(new Date());
		schedTbl.setExtensionEndDate(new Date());

		company.setId(0);
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(10);
		company.setRealm(realm);
		company.setRealmPlanYear(realmPlanYear);
		company.setQuater("Q2");
		company.setRenewalCompany(true);
		company.setPlanStartDate("01-JAN-2019");
		company.setPlanEndDate("31-DEC-2019");
		company.setSchedTbl(schedTbl);
		return company;
	}

	private StrategyData prepareStrategyDataJson() {
		StrategyData strategyDataJson = new StrategyData();
		StrategySummary summary = new StrategySummary();
		summary.setId(100L);
		summary.setSubmitDate(new Date());
		summary.setBudgetFactor(MONTHLY_BUDGET_FACTOR);
		summary.setTotalBudget(BigDecimal.valueOf(20000));
		summary.setName(STRATEGY_NAME);
		strategyDataJson.setStrategySummary(summary);
		strategyDataJson.setBenefitGroups(prepareBenefitGroups());
		return strategyDataJson;
	}

	private StrategyData prepareStrategyData() {
		final String BENEFIT_PLAN = "004S6L";
		StrategyData sd = new StrategyData();

		StrategySummary s1 = new StrategySummary();
		sd.setStrategySummary( s1 );
		s1.setId( 189333L );
		s1.setName( "Current Strategy" );
		s1.setType( "recommended" );
		s1.setSubmitted( true );
		s1.setSubmitDate( new Date() );
		s1.setEffectiveDate( java.sql.Date.valueOf( "2021-10-01" ) );
		s1.setEstimatedTotalCost( BigDecimal.ZERO );
		s1.setCurrentYearTotalCost( BigDecimal.ZERO );
		s1.setHeadcount( 28 );
		s1.setTotalBudget( BigDecimal.ZERO );
		s1.setBudgetFactor( 1 );
		s1.setCompanyId( "G9P" );
		s1.setAcaFplOpted( true );
		s1.setCostShareType( "DFLT" );

		StrategyBenefitGroup bg = new StrategyBenefitGroup();
		sd.setBenefitGroups( new ArrayList<>() );
		sd.getBenefitGroups().add( bg );
		bg.setId( 203998L );
		bg.setName( "All Employees" );
		bg.setType( "STD" );
		bg.setWaitingPeriod( "EX90" );
		bg.setStatus( "A" );
		bg.setBenefitProgram( "UV6" );
		bg.setCompanyId( 86101L );
		bg.setStrategyId( 189333L );
		bg.setHeadcount( 28 );
		bg.setDefaultGroup( true );

		BenefitOffer bo = new BenefitOffer();
		bg.setBenefitOffers( new ArrayList<>() );
		bg.getBenefitOffers().add( bo );
		BenefitOfferSummary bos = new BenefitOfferSummary();
		bo.setSummary( bos );
		bos.setType( "medical" );
		bos.setGroupId( 203998L );
		bos.setDescription( "10" );
		bos.setHeadcount( 0 );
		bos.setWaiverHeadcount( 5 );
		bos.setBaseFundingRequired( true );

		PlanCarrier pc = new PlanCarrier();
		bo.setPlanCarriers( new HashSet<>() );
		bo.getPlanCarriers().add( pc );
		pc.setId( 9 );
		pc.setName( "UHC Portfolio A" );
		pc.setMandatory( true );
		pc.setRegionalCarriers( new ArrayList<>() );

		PlanPackage pp = new PlanPackage();
		bo.setPlanPackage( pp );
		pp.setId( 0 );
		pp.setTemplateId( 0 );
		pp.setFundingModelId( 1493042L );
		pp.setCustomized( true );
		pp.setFundingBasePlan( BENEFIT_PLAN );
		pp.setStrategyId( 189333L );
		pp.setFundingType( "BFPCT" );
		FundingBasePlan fbp = new FundingBasePlan();
		fbp.setPlanCarrierId( 9L );
		fbp.setFundingBasePlan( BENEFIT_PLAN );
		pp.setFundingBasePlans( new ArrayList<>() );
		pp.getFundingBasePlans().add( fbp );
		pp.setPlanCarrierIds( new ArrayList<>() );
		pp.getPlanCarrierIds().add( 9L );
		pp.setBenefitPlans( new ArrayList<>() );
		pp.getBenefitPlans().add( BENEFIT_PLAN );
		pp.setCoverageLevelFunding( new HashMap<>() );
		pp.getCoverageLevelFunding().put( "employee", new BigDecimal( "75" ) );

		BenefitPlan bp = new BenefitPlan();
		bo.setBenefitPlans( new ArrayList<>() );
		bo.getBenefitPlans().add( bp );
		bp.setId( BENEFIT_PLAN );
		bp.setPlanCarrierId( 9L );
		bp.setName( "UHC Basic EPO" );
		bp.setPlanCategory( "DFLT" );
		bp.setContributions( new ArrayList<>() );
		PlanContribution cntb = new PlanContribution();
		bp.getContributions().add( cntb );
		cntb.setId( 1632746124L );
		cntb.setPlanSelectionId( 0 );
		cntb.setType( "employee" );
		cntb.setBenefitPlanId( BENEFIT_PLAN );
		cntb.setPlanCost( new BigDecimal( "930" ) );
		cntb.setEmployerPercent( new BigDecimal( "75" ) );
		cntb.setOverrideType( "PCT" );
		//
		cntb = new PlanContribution();
		bp.getContributions().add( cntb );
		cntb.setId( 1632746125L );
		cntb.setPlanSelectionId( 0 );
		cntb.setType( "employeePlusSpouse" );
		cntb.setBenefitPlanId( BENEFIT_PLAN );
		cntb.setPlanCost( new BigDecimal( "1999" ) );
		cntb.setEmployerPercent( new BigDecimal( "60" ) );
		cntb.setOverrideType( "PCT" );
		//
		cntb = new PlanContribution();
		bp.getContributions().add( cntb );
		cntb.setId( 1632746127L );
		cntb.setPlanSelectionId( 0 );
		cntb.setType( "employeePlusFamily" );
		cntb.setBenefitPlanId( BENEFIT_PLAN );
		cntb.setPlanCost( new BigDecimal( "2883" ) );
		cntb.setEmployerPercent( new BigDecimal( "44" ) );
		cntb.setOverrideType( "FLT" );
		//
		cntb = new PlanContribution();
		bp.getContributions().add( cntb );
		cntb.setId( 1632746126L );
		cntb.setPlanSelectionId( 0 );
		cntb.setType( "employeePlusChild" );
		cntb.setBenefitPlanId( BENEFIT_PLAN );
		cntb.setPlanCost( new BigDecimal( "1674" ) );
		cntb.setEmployerPercent( new BigDecimal( "72" ) );
		cntb.setOverrideType( "PCT" );

		return sd;
	}

	private List<StrategyBenefitGroup> prepareBenefitGroups() {
		List<StrategyBenefitGroup> benGrps = new ArrayList<>(2);
		StrategyBenefitGroup sbg = prepareBenefitGroup(GRP1_WAIT_PERIOD_ID);
		benGrps.add(sbg);
		return benGrps;
	}

	private StrategyBenefitGroup prepareBenefitGroup(String waitingPeriodId) {
		StrategyBenefitGroup sbg = new StrategyBenefitGroup();
		sbg.setWaitingPeriod(waitingPeriodId);
		sbg.setBenefitOffers(prepareBenefitOffers());
		return sbg;
	}

	private List<BenefitOffer> prepareBenefitOffers() {
		List<BenefitOffer> offers = new ArrayList<>();

		BenefitOffer benOffer = new BenefitOffer();
		benOffer.setSummary(prepareSummary(BSSApplicationConstants.MEDICAL));
		List<PlanContribution> contributions = new ArrayList<>();
		List<PlanContribution> contributions1 = new ArrayList<>();
		contributions.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE.getId()));
		contributions.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
		contributions.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
		contributions.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.CFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		benOffer.setBenefitPlans(Arrays.asList(prepareBenefitPlan("MPLANID1", "MedPlan1", contributions),
				prepareBenefitPlan("MPLANID2", "MedPlan2", contributions1)));
		benOffer.setPlanCarriers(
				new HashSet<>(Arrays.asList(preparePlanCarrier("Aetna"), preparePlanCarrier("Kaiser"))));
		Map<String, BigDecimal> coverageLevelFunding = prepareCvgLvlFunding();
		Map<String, BigDecimal> coverageLevelFundingFlatMax = prepareCvgLvlFundingFlatMax();
		benOffer.setPlanPackage(preparePlanPackage("MPLANID1", BSSApplicationConstants.CFPCT, BigDecimal.valueOf(400),
				coverageLevelFunding));
		benOffer.getPlanPackage().setCoverageLevelFundingFlatMax(coverageLevelFundingFlatMax);
		offers.add(benOffer);
		benOffer = new BenefitOffer();
		benOffer.setSummary(prepareSummary(BSSApplicationConstants.DENTAL));
		contributions.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE.getId()));
		contributions.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
		contributions.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
		contributions.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
		contributions1.add(prepareContribution(BSSApplicationConstants.BFPCT, BigDecimal.valueOf(80), null,
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		benOffer.setBenefitPlans(Arrays.asList(prepareBenefitPlan("DPLANID1", "DenPlan1", contributions),
				prepareBenefitPlan("DPLANID2", "DenPlan2", contributions1)));
		benOffer.setPlanCarriers(new HashSet<>(Arrays.asList(preparePlanCarrier("Gaurdian"))));
		coverageLevelFunding = prepareCvgLvlFunding();
		benOffer.setPlanPackage(
				preparePlanPackage("DPLANID2", BSSApplicationConstants.BFPCT, null, coverageLevelFunding));
		offers.add(benOffer);
		benOffer = new BenefitOffer();
		benOffer.setSummary(prepareSummary(BSSApplicationConstants.VISION));
		contributions.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE.getId()));
		contributions.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
		contributions.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
		contributions.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		contributions1.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE.getId()));
		contributions1.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()));
		contributions1.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()));
		contributions1.add(prepareContribution("FLT", BigDecimal.valueOf(80), BigDecimal.valueOf(800),
				BigDecimal.valueOf(1000), CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()));
		benOffer.setBenefitPlans(Arrays.asList(prepareBenefitPlan("VPLANID1", "VisPlan1", contributions),
				prepareBenefitPlan("VPLANID2", "VisPlan2", contributions1)));
		benOffer.setPlanCarriers(new HashSet<>(Arrays.asList(preparePlanCarrier("EyeMed"), preparePlanCarrier("VSP"))));
		coverageLevelFunding = prepareCvgLvlFunding();
		benOffer.setPlanPackage(preparePlanPackage("VPLANID1", "FLT", null, coverageLevelFunding));
		offers.add(benOffer);
		benOffer = new BenefitOffer();
		benOffer.setSummary(prepareSummary(BSSApplicationConstants.ADDITIONAL));
		benOffer.setBenefitPlans(Arrays.asList(prepareBenefitPlan("APLANID1", "AddPlan1", contributions),
				prepareBenefitPlan("APLANID2", "AddPlan2", contributions1)));
		benOffer.setAdditionalBenefitOffers(crateAdditionalBenefitOffers());
		offers.add(benOffer);
		return offers;
	}

	private BenefitOfferSummary prepareSummary(String type) {
		BenefitOfferSummary summary = new BenefitOfferSummary();
		summary.setType(type);
		return summary;
	}

	private BenefitPlan prepareBenefitPlan(String id, String planName, List<PlanContribution> contributions) {
		BenefitPlan benPlan = new BenefitPlan();
		benPlan.setId(id);
		benPlan.setName(planName);
		benPlan.setContributions(contributions);
		return benPlan;
	}

	private PlanCarrier preparePlanCarrier(String name) {
		PlanCarrier planCarrie = new PlanCarrier();
		planCarrie.setName(name);
		return planCarrie;
	}

	private PlanPackage preparePlanPackage(String fundingBaseBenefitPlan, String fundingType,
			BigDecimal waiverAllowance, Map<String, BigDecimal> coverageLevelFunding) {
		PlanPackage planPckg = new PlanPackage();
		planPckg.setEmployeePaid(false);
		planPckg.setFundingType(fundingType);
		FundingBasePlan fundingBasePlan = new FundingBasePlan();
		fundingBasePlan.setFundingBasePlan(fundingBaseBenefitPlan);
		planPckg.setFundingBasePlans(Arrays.asList(fundingBasePlan));
		planPckg.setCoverageLevelFunding(coverageLevelFunding);
		planPckg.setWaiverAllowance(waiverAllowance);
		return planPckg;
	}

	private PlanContribution prepareContribution(String overrideType, BigDecimal employerPercent,
			BigDecimal employerContribution, BigDecimal planCost, String type) {
		PlanContribution pc = new PlanContribution();
		pc.setOverrideType(overrideType);
		pc.setEmployerPercent(employerPercent);
		pc.setEmployerContribution(employerContribution);
		pc.setPlanCost(planCost);
		pc.setType(type);
		return pc;
	}

	private Map<String, String> prepareCoverageCodes() {
		Map<String, String> covgCodes = new HashMap<>();
		covgCodes.put("all", "All Levels");
		covgCodes.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), "Employee Only");
		covgCodes.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), "Employee + Spouse");
		covgCodes.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), "Employee + Child(ren)");
		covgCodes.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), "Family");
		return covgCodes;
	}

	private Map<String, BigDecimal> prepareCvgLvlFunding() {
		Map<String, BigDecimal> coverageLevelFunding = new HashMap<>();
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(100));
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(100));
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(100));
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(100));
		return coverageLevelFunding;
	}

	private Map<String, BigDecimal> prepareCvgLvlFundingFlatMax() {
		Map<String, BigDecimal> coverageLevelFundingFltMax = new HashMap<>();
		coverageLevelFundingFltMax.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(750.50));
		coverageLevelFundingFltMax.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(750.50));
		coverageLevelFundingFltMax.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(750.50));
		coverageLevelFundingFltMax.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(750.50));

		return coverageLevelFundingFltMax;
	}

	private List<AdditionalBenefitOffer> crateAdditionalBenefitOffers() {
		List<AdditionalBenefitOffer> adBenefitOffers = new ArrayList<>();
		AdditionalBenefitOffer abOffer = new AdditionalBenefitOffer();
		BenefitOfferSummary summary = new BenefitOfferSummary();
		summary.setType("DISABILITY");
		abOffer.setSummary(summary);
		AdditionalBenefitPlan adBenPlan = new AdditionalBenefitPlan();
		adBenPlan.setDescription("STD & LTD Premium");
		abOffer.setAdditionalBenefitPlans(Arrays.asList(adBenPlan));
		adBenefitOffers.add(abOffer);
		abOffer = new AdditionalBenefitOffer();
		summary = new BenefitOfferSummary();
		summary.setType("life");
		abOffer.setSummary(summary);
		adBenPlan = new AdditionalBenefitPlan();
		adBenPlan.setDescription("Life and AD and D");
		abOffer.setAdditionalBenefitPlans(Arrays.asList(adBenPlan));
		adBenefitOffers.add(abOffer);
		abOffer = new AdditionalBenefitOffer();
		summary = new BenefitOfferSummary();
		summary.setType("CMTR");
		abOffer.setSummary(summary);
		adBenefitOffers.add(abOffer);
		return adBenefitOffers;
	}

	private SubmissionEmailDto prepareSubmissionEmailDtoforSingleClients() {
		List<CompanyAndConfNumberDto> companyAndConfNumberDtos = new ArrayList<>();
		String confirmationNumber = "1MPZEYY6S5SY";
		String userId = "00001780307";
		Company company = new Company();
		company.setCode("G48");
		company.setName("G48 LLC");
		companyAndConfNumberDtos.add(CompanyAndConfNumberDto.builder().companyCode(company.getCode())
				.companyName(company.getName()).confirmationNumber(confirmationNumber).build());
		return SubmissionEmailDto.builder().companyAndConfNumberDtos(companyAndConfNumberDtos).userId(userId)
				.sendToBssTeam(false).isSingleClient(true).build();
	}
	
	private SubmissionEmailDto prepareSubmissionEmailDtoforMultipleClients() {
		List<CompanyAndConfNumberDto> companyAndConfNumberDtos = new ArrayList<>();
		String confirmationNumber = "1MPZEYY6S5SY";
		String userId = "00001780307";
		Company company = new Company();
		company.setCode("G48");
		company.setName("G48 LLC");
		companyAndConfNumberDtos.add(CompanyAndConfNumberDto.builder().companyCode(company.getCode())
				.companyName(company.getName()).confirmationNumber(confirmationNumber).build());
		String confirmationNumber1 = "1MPZEYY6S5SPY";
		company = new Company();
		company.setCode("G5Z");
		company.setName("G5Z LLC");
		String oeQuarter = "8Y";
		companyAndConfNumberDtos.add(CompanyAndConfNumberDto.builder().companyCode(company.getCode())
				.companyName(company.getName()).confirmationNumber(confirmationNumber1).build());
		
		return SubmissionEmailDto.builder().companyAndConfNumberDtos(companyAndConfNumberDtos).userId(userId)
				.sendToBssTeam(false).isSingleClient(false).oeQuarter(oeQuarter).build();
	}

}

