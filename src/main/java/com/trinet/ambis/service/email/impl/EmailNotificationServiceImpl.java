package com.trinet.ambis.service.email.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.email.EmailNotificationService;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.common.notification.util.NotificationUtils;

/**
 * @author rvutukuri
 *
 */
@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	public NotificationRequestParam sendConfirmationEmail(NotificationRequestParam notificationRequestParam) {
		String apiurl = buildUrl();
		LOGGER.info("Notification URL : {}", apiurl);
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.YEAR, 1);
		String request = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			request = mapper.writeValueAsString(notificationRequestParam);
			HttpEntity<String> requestEntity = createHttpEntity(request);
			LOGGER.info("Email request body : {}", requestEntity.getBody());
			ResponseEntity<Object> response = restTemplate.exchange(apiurl, HttpMethod.POST, requestEntity,
					Object.class);
			if (response != null && response.getStatusCode() != null
					&& (response.getStatusCode().value() == BSSApplicationConstants.REDIRECT_NOTIFICATION_URL_ERROR_CODE
							|| response.getStatusCode()
									.value() == BSSApplicationConstants.SERVER_ERROR_NOTIFICATION_URL_ERROR_CODE)) {
				LOGGER.error("Error occurred while sending email. Status Code: {}", response.getStatusCode());
				throw new RuntimeException(
						String.format("Error occurred while sending email. Status Code: %s", response.getStatusCode()));
			}
		} catch (Exception ex) {
			throw new RuntimeException(String.format("Error occured while calling apiurl::%s ", apiurl), ex);
		}
		return notificationRequestParam;
	}

	/**
	 * Creating HTTP request Entity
	 * 
	 * @param reqBody
	 * @return
	 */
	private HttpEntity<String> createHttpEntity(String reqBody) {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Content-Type", "application/json");
		requestHeaders.add("connection", "keep-alive");
		requestHeaders.add("Accept", "application/json");
		NotificationUtils.addAuthorizationToken(requestHeaders);
		return new HttpEntity<>(reqBody, requestHeaders);
	}

	/**
	 * This method is for constructing the email service end point.
	 * 
	 * @return
	 */
	private String buildUrl() {
		String endPointUrl = BSSMessageConfig.getProperty(BSSURIConstants.API_NOTIFICATIONS_ENDPOINT_URI);
		String event = BSSMessageConfig.getProperty(BSSURIConstants.NOTIFICATIONS_EVENT);
		String eventType = BSSMessageConfig.getProperty(BSSURIConstants.NOTIFICATIONS_EVENT_TYPE);
		return endPointUrl + eventType + event;
	}

}
