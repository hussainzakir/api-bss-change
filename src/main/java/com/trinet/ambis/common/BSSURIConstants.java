/**
 * 
 */
package com.trinet.ambis.common;

/**
 * @author rvutukuri
 *
 */
public class BSSURIConstants {

	private BSSURIConstants() {
		throw new IllegalStateException(
				"Constants class " + BSSURIConstants.class.getName() + " can not be instantiated.");
	}

	// Notifications Services URL
	public static final String API_NOTIFICATIONS_ENDPOINT_URI = "notificationsServiceUrl";
	public static final String NOTIFICATIONS_EVENT_TYPE = "notificationEventType";
	public static final String NOTIFICATIONS_EVENT = "notificationEvent";
	public static final String GET_BENEFIT_PROFILE = "trinet.benefit.profile";
	public static final String UPLOAD_CONFIRMATION_STATEMENT = "uploadDocumentUrl";
	public static final String CONFIRMATION_STATEMENT_DOC_URI = "confirmationStmtDocMgmtUrl";
	public static final String PLAN_VIEW_API_URI = "planViewApiUri";
	public static final String BROKER_NOTIFICATION_API_URI = "brokerNotificationApiUri";
}
