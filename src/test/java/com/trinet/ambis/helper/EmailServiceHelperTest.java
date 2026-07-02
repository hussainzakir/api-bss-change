package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.model.notification.Recipient;

import freemarker.template.Configuration;
import freemarker.template.Template;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceHelperTest {

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private Template mockTemplate;

    private static final String VALID_COMPANY_CODE = "TestCompany";
    private static final String USER_ID = "000022233390";
    private static final String CONFIRMATION_ID = "SADKJKAJDLJALDAD";
    private static final String SERVICE_ORDER_NUMBER = "12312323";
    private static final long REALM_PLYR_ID = 31;
    private static final long STRATEGY_ID = 1111;
    private static final String MAIL_BODY = "<HTML> BODY </HTML>";

    private MockedStatic<BSSMessageConfig> bssMessageConfigMockedStatic;

    @Before
    public void setUp() throws Exception {
        bssMessageConfigMockedStatic = org.mockito.Mockito.mockStatic(BSSMessageConfig.class);

        EmailServiceHelper.setConfiguration(mockConfiguration);
        when(mockConfiguration.getTemplate(anyString(), eq(StandardCharsets.UTF_8.name())))
                .thenReturn(mockTemplate);

        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty("aleNotificationSubject")).thenReturn("Test Subject 1");
        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty("nonClientConfirmationSubject")).thenReturn("Test Subject 2");
        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty("notAleNotificationSubject")).thenReturn("Test Subject 3");
        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty("clientConfirmationSubject")).thenReturn("Test Subject 4");
        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty("external.email.content.baseUrl")).thenReturn("https://test-url/email/content");
        bssMessageConfigMockedStatic.when(() -> BSSMessageConfig.getProperty("external.email.content.assetsUrl")).thenReturn("https://test-url/email/assets");
    }

    @After
    public void tearDown() {
        bssMessageConfigMockedStatic.close();
    }

    @Test
    public void testGenerateAleNotificationEmail_Success() throws Exception {
        doAnswer(invocation -> {
            Map<String, Object> params = invocation.getArgument(0);
            StringWriter writer = invocation.getArgument(1);

            writer.write("Email with base URL: " + params.get("emailContentAssetsUrl"));
            return MAIL_BODY;
        }).when(mockTemplate).process(anyMap(), any(StringWriter.class));

        Company company = new Company();
        company.setCode(VALID_COMPANY_CODE);
        company.setEligAle(true);
        company.setRealmPlanYear(new RealmPlanYear());

        SubmitPayload submitPayload = new SubmitPayload();
        SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
                .status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
                .createTime(new Date()).userId(USER_ID).company(VALID_COMPANY_CODE).emailSentStatus(false)
                .confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
                .statementUploadStatus(null).updateTime(null).sendEmail(true).build();

        SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
                .withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

        NotificationRequestParam aleNotificationRequest = EmailServiceHelper.createAleNotificationRequest(company, submissionInfo);

        assertEquals("email", aleNotificationRequest.getDeliveryChannel().getChannel());
    }
    
    @Test
    public void testGenerateNonEligAleNotificationEmail_Success() throws Exception {
        doAnswer(invocation -> {
            Map<String, Object> params = invocation.getArgument(0);
            StringWriter writer = invocation.getArgument(1);

            writer.write("Email with base URL: " + params.get("emailContentAssetsUrl"));
            return MAIL_BODY;
        }).when(mockTemplate).process(anyMap(), any(StringWriter.class));

        Company company = new Company();
        company.setCode(VALID_COMPANY_CODE);
        company.setEligAle(false);

        SubmitPayload submitPayload = new SubmitPayload();
        SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
                .status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
                .createTime(new Date()).userId(USER_ID).company(VALID_COMPANY_CODE).emailSentStatus(false)
                .confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
                .statementUploadStatus(null).updateTime(null).sendEmail(true).build();

        SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
                .withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

        NotificationRequestParam aleNotificationRequest = EmailServiceHelper.createAleNotificationRequest(company, submissionInfo);

        assertEquals("email", aleNotificationRequest.getDeliveryChannel().getChannel());
    }
    
    @Test
    public void testCreateClientNotificationEmail_Success() throws Exception {
        doAnswer(invocation -> {
            Map<String, Object> params = invocation.getArgument(0);
            StringWriter writer = invocation.getArgument(1);

            writer.write("Email with base URL: " + params.get("emailContentBaseUrl"));
            return MAIL_BODY;
        }).when(mockTemplate).process(anyMap(), any(StringWriter.class));

        Company company = new Company();
        company.setCode(VALID_COMPANY_CODE);
        company.setEligAle(true);

        SubmitPayload submitPayload = new SubmitPayload();
        SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
                .status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
                .createTime(new Date()).userId(USER_ID).company(VALID_COMPANY_CODE).emailSentStatus(false)
                .confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
                .statementUploadStatus(null).updateTime(null).sendEmail(true).build();

        SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
                .withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

        NotificationRequestParam clientNotificationRequest = EmailServiceHelper.createClientNotificationRequest(company, submissionInfo);

        assertEquals("email", clientNotificationRequest.getDeliveryChannel().getChannel());
    }

    @Test
    public void testCreateNonClientNotificationEmail_Success() throws Exception {

        Company company = new Company();
        company.setCode(VALID_COMPANY_CODE);
        company.setEligAle(true);

        SubmitPayload submitPayload = new SubmitPayload();
        SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
                .status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
                .createTime(new Date()).userId(USER_ID).company(VALID_COMPANY_CODE).emailSentStatus(false)
                .confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
                .statementUploadStatus(null).updateTime(null).sendEmail(true).build();

        SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
                .withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

        NotificationRequestParam nonClientNotificationRequest = EmailServiceHelper.createNonClientNotificationRequest(company, submissionInfo);

        assertEquals("email", nonClientNotificationRequest.getDeliveryChannel().getChannel());
    }

    @Test
    public void testPrepareRecipients() {
        Set<String> emailSet = Set.of("email1@test.com", "email2@test.com");
        List<Recipient> recipientList = EmailServiceHelper.prepareRecipients(emailSet);
        assertEquals(2, recipientList.size());
    }

    @Test
    public void testPrepareRecipients_Null() {
        Set<String> emailSet = Set.of();
        List<Recipient> recipientList = EmailServiceHelper.prepareRecipients(emailSet);
        assertTrue(recipientList.isEmpty());
    }
}
