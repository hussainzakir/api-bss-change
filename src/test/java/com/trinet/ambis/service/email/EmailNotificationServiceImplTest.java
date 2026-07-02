package com.trinet.ambis.service.email;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.trinet.ambis.configuration.BSSMessageConfig;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.service.email.impl.EmailNotificationServiceImpl;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.common.notification.util.NotificationUtils;

/**
 * @author schaudhari
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	EmailNotificationServiceImpl emailNotificationService;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	ResponseEntity<Object> response;

	private MockedStatic<NotificationUtils> mockStaticNotificationUtils;
    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;

	@Before
	public void setUp() {
		if (mockStaticNotificationUtils == null) {
			mockStaticNotificationUtils = Mockito.mockStatic(NotificationUtils.class);
		}
        if (mockStaticBSSMessageConfig == null) {
            mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
        }
	}

	@org.junit.After
	public void tearDown() {
		if (mockStaticNotificationUtils != null) {
			mockStaticNotificationUtils.close();
			mockStaticNotificationUtils = null;
		}
        if (mockStaticBSSMessageConfig != null) {
            mockStaticBSSMessageConfig.close();
            mockStaticBSSMessageConfig = null;
        }
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RuntimeException.class)
	public void sendConfirmationEmailWhenResponse500() {
		NotificationRequestParam notificationRequestParam = new NotificationRequestParam();

		when(restTemplate.exchange(Mockito.any(String.class), Mockito.any(HttpMethod.class),
				Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

		emailNotificationService.sendConfirmationEmail(notificationRequestParam);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RuntimeException.class)
	public void sendConfirmationEmailWhenResponse302() {
		NotificationRequestParam notificationRequestParam = new NotificationRequestParam();

		when(restTemplate.exchange(Mockito.any(String.class), Mockito.any(HttpMethod.class),
				Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.FOUND);

		emailNotificationService.sendConfirmationEmail(notificationRequestParam);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RuntimeException.class)
	public void sendConfirmationEmailWhenException() {
		NotificationRequestParam notificationRequestParam = new NotificationRequestParam();

		when(restTemplate.exchange(Mockito.any(String.class), Mockito.any(HttpMethod.class),
				Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenThrow(RuntimeException.class);

		emailNotificationService.sendConfirmationEmail(notificationRequestParam);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void sendConfirmationEmailWhen200() {
		NotificationRequestParam notificationRequestParam = new NotificationRequestParam();

		ArgumentCaptor<String> apiurlArgCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<HttpMethod> httpMethodArgCaptor = ArgumentCaptor.forClass(HttpMethod.class);
		ArgumentCaptor<HttpEntity> httpEntityArgCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		ArgumentCaptor<Class> objArgCaptor = ArgumentCaptor.forClass(Class.class);

		when(restTemplate.exchange(apiurlArgCaptor.capture(), httpMethodArgCaptor.capture(),
				httpEntityArgCaptor.capture(), objArgCaptor.capture())).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.OK);

		NotificationRequestParam actual = emailNotificationService.sendConfirmationEmail(notificationRequestParam);

		assertEquals(notificationRequestParam, actual);
		assertEquals(HttpMethod.POST, httpMethodArgCaptor.getValue());
		assertEquals("application/json", httpEntityArgCaptor.getValue().getHeaders().get("Content-Type").get(0));
		assertEquals("keep-alive", httpEntityArgCaptor.getValue().getHeaders().get("connection").get(0));
		assertEquals("application/json", httpEntityArgCaptor.getValue().getHeaders().get("Accept").get(0));
		assertEquals(Object.class, objArgCaptor.getValue());
	}

}