package com.trinet.ambis.service.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.helper.EmailServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.email.impl.AleServiceImpl;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;

@RunWith(MockitoJUnitRunner.class)
public class AleServiceImplTest {

    @InjectMocks
    private AleServiceImpl aleService;

    @Mock
    private CompanyService companyService;

    @Mock
    private PersonService personService;

    @Mock
    private EmailNotificationService emailNotificationService;

    private static final Long VALID_COMPANY_ID = 123L;
    private static final String VALID_COMPANY_CODE = "TestCompany";
    private static final String USER_ID = "000022233390";
    private static final String CONFIRMATION_ID = "SADKJKAJDLJALDAD";
    private static final String SERVICE_ORDER_NUMBER = "12312323";
    private static final long REALM_PLYR_ID = 31;
    private static final long STRATEGY_ID = 1111;
    private static final String CS_AUTH_EMAIL = "customerSubmitter@abc.com";
    private static final String COMP_HRM_EMAIL = "companyHrManager@trinet.com";

    private Company mockCompany;

    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;
    private MockedStatic<EmailServiceHelper> mockStaticEmailServiceHelper;

    @Before
    public void setUp() throws Exception {
        mockCompany = new Company();
        mockCompany.setId(VALID_COMPANY_ID);
        mockCompany.setCode(VALID_COMPANY_CODE);
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("aleNotificationSubject")).thenReturn("Test Subject");
        mockStaticEmailServiceHelper = Mockito.mockStatic(EmailServiceHelper.class);
   };

    @After
    public void tearDown() {
        if (mockStaticBSSMessageConfig != null) {
            mockStaticBSSMessageConfig.close();
            mockStaticBSSMessageConfig = null;
        }
        if (mockStaticEmailServiceHelper != null) {
            mockStaticEmailServiceHelper.close();
            mockStaticEmailServiceHelper = null;
        }
    }
    /**
     * Given: Company and SubmissionInfo data
     * When: sendConfirmationEmail method is called and aleNotificationRequest is created with recipients
     * Then: It should call emailNotificationService.sendConfirmationEmail with aleNotificationRequest to send an email for given recipients
     */
    @Test
    public void sendConfirmationEmail_testSendEmailSuccessfully() {
        Company company = new Company();
        company.setCode(VALID_COMPANY_CODE);
        NotificationRequestParam aleNotificationRequest = new NotificationRequestParam();

        SubmitPayload submitPayload = new SubmitPayload();
        SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
                .status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
                .createTime(new Date()).userId(USER_ID).company(VALID_COMPANY_CODE).emailSentStatus(false)
                .confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
                .statementUploadStatus(null).updateTime(null).sendEmail(true).build();

        SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
                .withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

        ArgumentCaptor<NotificationRequestParam> notificationRequestParamArgCaptor = ArgumentCaptor
                .forClass(NotificationRequestParam.class);

        when(personService.getCSAuthEmail(USER_ID)).thenReturn(CS_AUTH_EMAIL);
        when(personService.getCompanyHrmEmail(company.getCode())).thenReturn(COMP_HRM_EMAIL);
        when(EmailServiceHelper.createAleNotificationRequest(company, submissionInfo))
                .thenReturn(aleNotificationRequest);
        when(emailNotificationService.sendConfirmationEmail(notificationRequestParamArgCaptor.capture()))
                .thenReturn(new NotificationRequestParam());

        aleService.sendConfirmationEmail(company, submissionInfo);

        assertTrue(submissionInfo.getEmailInfo().isAleEmailSent());
        assertEquals(CS_AUTH_EMAIL, submissionInfo.getEmailInfo().getClientRecipients().stream().filter(recipient -> recipient.getType().equals("to")).findFirst().get().getId());
        assertEquals(COMP_HRM_EMAIL, submissionInfo.getEmailInfo().getClientRecipients().stream().filter(recipient -> recipient.getType().equals("bcc")).findFirst().get().getId());
    }

    /**
     * Given: Company and SubmissionInfo data
     * When: sendConfirmationEmail method is called and aleNotificationRequest creation is failed
     * Then: emailNotificationService.sendConfirmationEmail should throw an exception
     */
    @Test(expected = RestClientException.class)
    public void sendConfirmationEmail_testSendEmailFailure() {
        Company company = new Company();
        company.setCode(VALID_COMPANY_CODE);

        SubmitPayload submitPayload = new SubmitPayload();
        SubmitStatus submitStatus = SubmitStatus.builder().strategyId(STRATEGY_ID)
                .status(BSSApplicationConstants.PROCESSING).submitError(null).submitPayload(submitPayload)
                .createTime(new Date()).userId(USER_ID).company(VALID_COMPANY_CODE).emailSentStatus(false)
                .confirmationNumber(CONFIRMATION_ID).realmYrId(REALM_PLYR_ID).serviceOrder(SERVICE_ORDER_NUMBER)
                .statementUploadStatus(null).updateTime(null).sendEmail(true).build();

        SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().buildEmailInfo()
                .withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo().buildPreSubmit();

        when(personService.getCSAuthEmail(USER_ID)).thenReturn(CS_AUTH_EMAIL);
        when(personService.getCompanyHrmEmail(company.getCode())).thenReturn(COMP_HRM_EMAIL);
        when(EmailServiceHelper.createAleNotificationRequest(company, submissionInfo))
                .thenReturn(null);
        when(emailNotificationService.sendConfirmationEmail(Mockito.any()))
                .thenThrow(RestClientException.class);

        aleService.sendConfirmationEmail(company, submissionInfo);

        assertFalse(submissionInfo.getEmailInfo().isAleEmailSent());
    }

    /**
     * Given: Valid company ID and matching company code
     * When: updateAleChangeStatus is called
     * Then: It should call companyService.updateAleStatus with status 1
     */
    @Test
    public void shouldUpdateAleStatus_WhenValidInputsProvided() {
        // Given
        when(companyService.findByCompanyId(VALID_COMPANY_ID)).thenReturn(mockCompany);

        // When
        aleService.updateAleChangeStatus(VALID_COMPANY_ID, VALID_COMPANY_CODE);

        // Then
        verify(companyService, times(1)).updateAleUpdatedFlag(mockCompany, 1);
    }

    /**
     * Given: Valid company ID but mismatched company code
     * When: updateAleChangeStatus is called
     * Then: It should throw IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WhenCompanyCodeDoesNotMatch() {
        // Given
        when(companyService.findByCompanyId(VALID_COMPANY_ID)).thenReturn(mockCompany);
        String wrongCode = "XYZ999";

        // When
        aleService.updateAleChangeStatus(VALID_COMPANY_ID, wrongCode);

        // Then: exception is expected
    }

    /**
     * Given: Invalid company ID (no company found)
     * When: updateAleChangeStatus is called
     * Then: It should throw IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_WhenCompanyNotFound() {
        // Given
        when(companyService.findByCompanyId(VALID_COMPANY_ID)).thenReturn(null);

        // When
        aleService.updateAleChangeStatus(VALID_COMPANY_ID, VALID_COMPANY_CODE);

        // Then: exception is expected
    }

}
