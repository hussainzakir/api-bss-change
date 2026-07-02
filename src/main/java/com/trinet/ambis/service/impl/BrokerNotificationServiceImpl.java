package com.trinet.ambis.service.impl;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSURIConstants;
import com.trinet.ambis.configuration.AuthenticationProperties;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.SchedTblDao;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.rest.controllers.dto.BrokerNotificationFailEmailDto;
import com.trinet.ambis.service.BrokerNotificationService;
import org.springframework.http.MediaType;
import java.util.Collections;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.model.BrokerNotificationDto;
import com.trinet.ambis.service.model.SchedTblDto;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.common.DateUtils;
import com.trinet.security.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.common.AppConfig;
import com.trinet.ambis.configuration.BSSMessageConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Objects;

@Service
public class BrokerNotificationServiceImpl implements BrokerNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(BrokerNotificationServiceImpl.class);
    private static final String INTERNAL_OPEN_DATE_Q4_8Y = "2026-06-02";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    EmailGenService emailGenService;

    @Autowired
    private AuthenticationProperties authProperties;

    @Autowired
    private SchedTblDao schedTblDao;

    @Override
    public void validateAndSendBrokerNotification(HttpServletRequest request, SchedTblDto schedTblDto) {
        try {

            String company = schedTblDto.getSched().getCompany();
            if (Constants.DEFAULT_COMPANY_CODE.equals(company)) {
                Long realmYearId = schedTblDto.getSched().getRealmYearId();
                Date newInternalOpenDate = schedTblDto.getInternalOpenDate();
                SchedTbl existingSchedule = schedTblDao.getSecheduleDates(company, realmYearId);
                Date existingInternalOpenDate = null;
                if(existingSchedule != null){
                    existingInternalOpenDate = existingSchedule.getInternalOpenDate();

                }
                if (Objects.isNull(existingSchedule) || (Objects.nonNull(existingInternalOpenDate)
                        && Objects.nonNull(newInternalOpenDate)
                        && DateUtils.compareDate(existingInternalOpenDate, newInternalOpenDate) != 0)) {
                    sendBrokerNotificationForSchedule(request, schedTblDto);
                }
            }
        } catch (Exception e) {
            logger.error("Error validating and sending broker notification for schedule", e);
        }
    }

    private void sendBrokerNotificationForSchedule(HttpServletRequest request, SchedTblDto schedTblDto) {
        try {
            BenExchngEnums benExchange = BenExchngEnums.getByQuarter(schedTblDto.getOeQuarter());
            String exchange = benExchange.getBenExchng();
            String quarter = schedTblDto.getOeQuarter();
            String internalOpenDate;

            // Set internalOpenDate to 2026-06-02 if quarter is Q4 or 8Y. We will remove the code once the internal open period starts for Q4 and 8Y in 2026.
            if ("Q4".equalsIgnoreCase(quarter) || "8Y".equalsIgnoreCase(quarter)) {
                internalOpenDate = INTERNAL_OPEN_DATE_Q4_8Y;
            } else {
                internalOpenDate = CommonUtils.formatDateToString(schedTblDto.getInternalOpenDate(),
                        BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
            }

            if (Objects.nonNull(exchange) && Objects.nonNull(quarter)) {
                BrokerNotificationDto brokerNotificationDto = new BrokerNotificationDto(
                        exchange,
                        quarter,
                        internalOpenDate);

                sendBrokerNotification(request, brokerNotificationDto);
            }
        } catch (Exception e) {
            logger.error("Error sending broker notification for schedule", e);
        }
    }

    private void sendBrokerNotification(HttpServletRequest request,BrokerNotificationDto brokerNotificationDto) {
        try {
            String apiUrl = buildUrl();
            HttpEntity<String> requestEntity = createHttpEntity(request, brokerNotificationDto);

            restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        } catch (Exception e) {
            logger.error("Failed to send broker notification for exchange: {}, quarter: {}. Error: {}",
                    brokerNotificationDto.getExchange(),
                    brokerNotificationDto.getQuarter(),
                    e.getMessage(), e);
            sendEmailToBSSDevTeam(brokerNotificationDto);
        }
    }

    private String buildUrl() {
        String baseUrl = AppConfig.getProfileServiceURL();
        String endPointUrl = BSSMessageConfig.getProperty(BSSURIConstants.BROKER_NOTIFICATION_API_URI);
        return baseUrl + endPointUrl;
    }

    private HttpEntity<String> createHttpEntity(HttpServletRequest request, BrokerNotificationDto brokerNotificationDto) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("token", SecurityUtils.parseAuthenticationToken(request));
        headers.add("Authorization", authProperties.getClientId());
        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(brokerNotificationDto);
        return new HttpEntity<>(requestBody, headers);
    }

    private void sendEmailToBSSDevTeam(BrokerNotificationDto request) {
        try {
            BrokerNotificationFailEmailDto emailDto = BrokerNotificationFailEmailDto.builder()
                    .exchange(request.getExchange())
                    .quarter(request.getQuarter())
                    .internalOpenDate(request.getInternalOpenDate())
                    .sendToBSS(true)
                    .build();
            emailGenService.createSupportEmail(emailDto);
        } catch (Exception e) {
            logger.error("Failed to send failure notification email", e);
        }
    }
}