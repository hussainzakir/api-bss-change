package com.trinet.ambis.service.unit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import com.trinet.ambis.enums.BenExchngEnums;
import org.springframework.http.MediaType;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.configuration.AuthenticationProperties;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.dao.hrp.SchedTblDao;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.persistence.model.SchedTblId;
import com.trinet.ambis.rest.controllers.dto.BrokerNotificationFailEmailDto;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.impl.BrokerNotificationServiceImpl;
import com.trinet.ambis.service.model.BrokerNotificationDto;
import com.trinet.ambis.service.model.SchedTblDto;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;
import com.trinet.common.AppConfig;
import com.trinet.security.util.SecurityUtils;
import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class BrokerNotificationServiceImplTest extends ServiceUnitTest {

    @InjectMocks
    private BrokerNotificationServiceImpl brokerNotificationService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmailGenService emailGenService;

    @Mock
    private AuthenticationProperties authProperties;

    @Mock
    private SchedTblDao schedTblDao;

    private MockedStatic<AppConfig> mockStaticAppConfig;
    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;
    private MockedStatic<SecurityUtils> mockStaticSecurityUtils;

    private MockHttpServletRequest request;
    private BrokerNotificationDto brokerNotificationDto;

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final String ENDPOINT_URL = "/broker/notification";
    private static final String EXCHANGE = "SHOP";
    private static final String QUARTER = "Q1";
    private static final String INTERNAL_OPEN_DATE = "2026-01-01";
    private static final String AUTH_TOKEN = "test-auth-token";
    private static final String CLIENT_ID = "test-client-id";
    private static final String DEFAULT_COMPANY_CODE = "DEFAULT";
    private static final Long REALM_YEAR_ID = 3L;

    @Before
    public void setUp() {
        mockStaticAppConfig = Mockito.mockStatic(AppConfig.class);
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
        mockStaticSecurityUtils = Mockito.mockStatic(SecurityUtils.class);

        mockStaticAppConfig.when(AppConfig::getProfileServiceURL).thenReturn(BASE_URL);
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("brokerNotificationApiUri"))
                .thenReturn(ENDPOINT_URL);
        mockStaticSecurityUtils.when(() -> SecurityUtils.parseAuthenticationToken(any(HttpServletRequest.class)))
                .thenReturn(AUTH_TOKEN);

        when(authProperties.getClientId()).thenReturn(CLIENT_ID);

        request = new MockHttpServletRequest();
        brokerNotificationDto = new BrokerNotificationDto(EXCHANGE, QUARTER, INTERNAL_OPEN_DATE);
    }

    @After
    public void tearDown() {
        if (mockStaticAppConfig != null) {
            mockStaticAppConfig.close();
        }
        if (mockStaticBSSMessageConfig != null) {
            mockStaticBSSMessageConfig.close();
        }
        if (mockStaticSecurityUtils != null) {
            mockStaticSecurityUtils.close();
        }
    }

    @Test
    public void validateAndSendBrokerNotificationTest() throws Exception {
        // Given
        Date oldDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        Date newDate = Utils.convertStringToDate("15-JAN-2026", Constants.DATE_FORMAT);

        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", newDate);
        SchedTbl existingSchedule = prepareExistingSchedTbl(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, oldDate);

        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(existingSchedule);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(successResponse);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        verify(schedTblDao, times(1)).getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void validateAndSendBrokerNotificationShouldNotSendNotificationTest() throws Exception {
        // Given
        Date sameDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);

        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", sameDate);
        SchedTbl existingSchedule = prepareExistingSchedTbl(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, sameDate);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(existingSchedule);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        verify(schedTblDao, times(1)).getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID);
        verify(restTemplate, times(0)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void validateAndSendBrokerNotificationServiceExceptionTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-FEB-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", internalOpenDate);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RestClientException("Notification service error"));

        doNothing().when(emailGenService).createSupportEmail(any(BrokerNotificationFailEmailDto.class));

        // When - should not throw exception
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
        verify(emailGenService, times(1)).createSupportEmail(any(BrokerNotificationFailEmailDto.class));
    }

    @Test
    public void validateAndSendBrokerNotificationNonDefaultCompanyShouldNotSendTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto("G48", REALM_YEAR_ID, "8Y", internalOpenDate);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then - should not call DAO or send notification for non-DEFAULT companies
        verify(schedTblDao, times(0)).getSecheduleDates(anyString(), any(Long.class));
        verify(restTemplate, times(0)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void validateAndSendBrokerNotificationNewScheduleShouldSendTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", internalOpenDate);

        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(successResponse);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        verify(schedTblDao, times(1)).getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void validateAndSendBrokerNotificationVerifyCorrectHeadersTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", internalOpenDate);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)))
                .thenReturn(successResponse);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        HttpEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity.getHeaders());
        assertEquals(MediaType.APPLICATION_JSON, capturedEntity.getHeaders().getContentType());
        assertEquals(MediaType.APPLICATION_JSON, capturedEntity.getHeaders().getAccept().get(0));
        assertEquals(AUTH_TOKEN, capturedEntity.getHeaders().getFirst("token"));
        assertEquals(CLIENT_ID, capturedEntity.getHeaders().getFirst("Authorization"));
    }

    @Test
    public void validateAndSendBrokerNotificationVerifyCorrectUrlTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", internalOpenDate);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                urlCaptor.capture(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(successResponse);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        String capturedUrl = urlCaptor.getValue();
        assertEquals(BASE_URL + ENDPOINT_URL, capturedUrl);
    }

    @Test
    public void validateAndSendBrokerNotificationVerifyEmailDtoValuesTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", internalOpenDate);

        ArgumentCaptor<BrokerNotificationFailEmailDto> emailDtoCaptor =
                ArgumentCaptor.forClass(BrokerNotificationFailEmailDto.class);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RestClientException("Test exception"));

        doNothing().when(emailGenService).createSupportEmail(emailDtoCaptor.capture());

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        BrokerNotificationFailEmailDto capturedDto = emailDtoCaptor.getValue();
        assertNotNull(capturedDto);
        assertEquals(BenExchngEnums.getByQuarter("8Y").getBenExchng(), capturedDto.getExchange());
        assertEquals("8Y", capturedDto.getQuarter());
        assertTrue(capturedDto.isSendToBSS());
    }

    @Test
    public void validateAndSendBrokerNotificationEmailServiceFailureTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", internalOpenDate);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        doThrow(new RuntimeException("Email service error"))
                .when(emailGenService).createSupportEmail(any(BrokerNotificationFailEmailDto.class));

        // When - should not throw exception even when email service fails
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        verify(emailGenService, times(1)).createSupportEmail(any(BrokerNotificationFailEmailDto.class));
    }

    @Test
    public void validateAndSendBrokerNotificationSuccess201ResponseTest() throws Exception {
        // Given
        Date internalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", internalOpenDate);

        ResponseEntity<String> createdResponse = new ResponseEntity<>("Created", HttpStatus.CREATED);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(createdResponse);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
        verify(emailGenService, times(0)).createSupportEmail(any(BrokerNotificationFailEmailDto.class));
    }

    @Test
    public void validateAndSendBrokerNotificationWithNullExistingInternalOpenDateTest() throws Exception {
        // Given
        Date newInternalOpenDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", newInternalOpenDate);

        SchedTbl existingSchedule = prepareExistingSchedTbl(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, null);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(existingSchedule);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then - should NOT send notification when existing internal open date is null but existing schedule exists
        verify(schedTblDao, times(1)).getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID);
        verify(restTemplate, times(0)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void validateAndSendBrokerNotificationWithNullNewInternalOpenDateTest() throws Exception {
        // Given
        Date existingDate = Utils.convertStringToDate("01-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y", null);

        SchedTbl existingSchedule = prepareExistingSchedTbl(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, existingDate);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(existingSchedule);

        // When
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto);

        // Then - should not send notification when new internal open date is null
        verify(schedTblDao, times(1)).getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID);
        verify(restTemplate, times(0)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void sendBrokerNotificationForScheduleForQ4() throws Exception {
        // Given
        Date newDate = Utils.convertStringToDate("15-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDtoQ4 = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "Q4",
                newDate);
        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(successResponse);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDtoQ4);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(),
                eq(String.class));
        String bodyQ4 = (String) entityCaptor.getValue().getBody();
        assertTrue(bodyQ4.contains("\"internalOpenDate\":\"2026-06-02\""));
    }

    @Test
    public void sendBrokerNotificationForScheduleFor8Y() throws Exception {
        // Given
        Date newDate = Utils.convertStringToDate("15-JAN-2026", Constants.DATE_FORMAT);
        SchedTblDto schedTblDto8Y = prepareSchedTblDto(DEFAULT_COMPANY_CODE, REALM_YEAR_ID, "8Y",
                newDate);
        ResponseEntity<String> successResponse = new ResponseEntity<>("Success", HttpStatus.OK);

        when(schedTblDao.getSecheduleDates(DEFAULT_COMPANY_CODE, REALM_YEAR_ID)).thenReturn(null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(successResponse);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        Mockito.reset(restTemplate); // Reset to capture again
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(successResponse);
        brokerNotificationService.validateAndSendBrokerNotification(request, schedTblDto8Y);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(),
                eq(String.class));
        String body8Y = (String) entityCaptor.getValue().getBody();
        assertTrue(body8Y.contains("\"internalOpenDate\":\"2026-06-02\""));
    }

    private SchedTblDto prepareSchedTblDto(String companyCode, Long realmYearId, String quarter, Date internalOpenDate) {
        SchedTblDto schedTblDto = new SchedTblDto();
        SchedTblId schedTblId = new SchedTblId();
        schedTblId.setCompany(companyCode);
        schedTblId.setRealmYearId(realmYearId);
        schedTblDto.setSched(schedTblId);
        schedTblDto.setOeQuarter(quarter);
        schedTblDto.setInternalOpenDate(internalOpenDate);
        return schedTblDto;
    }

    private SchedTbl prepareExistingSchedTbl(String companyCode, Long realmYearId, Date internalOpenDate) {
        SchedTbl schedTbl = new SchedTbl();
        SchedTblId schedTblId = new SchedTblId();
        schedTblId.setCompany(companyCode);
        schedTblId.setRealmYearId(realmYearId);
        schedTbl.setSched(schedTblId);
        schedTbl.setInternalOpenDate(internalOpenDate);
        return schedTbl;
    }
}

