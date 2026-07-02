package com.trinet.ambis.service.email;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.notification.NotificationRequestParam;

@Service
public interface EmailNotificationService {

	/**
	 * This method is for sending confirmation email.
	 * 
	 * @param param
	 * @return
	 */
	public NotificationRequestParam sendConfirmationEmail(NotificationRequestParam param);

}
