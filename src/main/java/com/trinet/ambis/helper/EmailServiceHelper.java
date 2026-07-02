package com.trinet.ambis.helper;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.model.notification.DeliveryChannel;
import com.trinet.ambis.service.model.notification.NotificationMessage;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.model.notification.Recipient;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class EmailServiceHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceHelper.class);

	private static Configuration configuration;

	private static final String EMAIL_CONFIRMATION_BDM_FTL = "emailConfirmationBdm.ftl";
	private static final String ALE_NOTIFICATION_FTL = "aleNotification.ftl";
	private static final String EMAIL_CONTENT_BASE_URL = "external.email.content.baseUrl";
	private static final String EMAIL_CONTENT_ASSETS_URL = "external.email.content.assetsUrl";
	private static final String NOT_ALE_NOTIFICATION_FTL = "notALeNotification.ftl";

	private EmailServiceHelper() {
	}

	public static void setConfiguration(Configuration configuration) {
		EmailServiceHelper.configuration = configuration;
	}

	public static List<Recipient> prepareRecipients(Set<String> emails) {
		List<Recipient> recipients = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(emails)) {
			for (String email : emails) {
				Recipient recipient = new Recipient();
				recipient.setId(email);
				recipients.add(recipient);
			}
		}
		return recipients;
	}

	public static NotificationRequestParam createClientNotificationRequest(Company company,
			SubmissionInfo submissionInfo) {
		
		String subject = String.format(BSSMessageConfig.getProperty("clientConfirmationSubject"),
				company.getName());
		
		String emailBody = generateClientConfirmationEmail(submissionInfo);
		return createNotificationRequest(submissionInfo, subject, emailBody,
				submissionInfo.getEmailInfo().getClientRecipients());
	}

	public static NotificationRequestParam createNonClientNotificationRequest(Company company,
			SubmissionInfo submissionInfo) {
		String submittedBy = submissionInfo.getEmailInfo().getRecipientName();
		if (submissionInfo.isDefaultSubmit() || submissionInfo.isResubmit()) {
			submittedBy = BSSApplicationConstants.INTERNAL_SUBMIT_USER;
		}

		String subject = String.format(BSSMessageConfig.getProperty("nonClientConfirmationSubject"),
				(Object[]) new String[] { company.getCode(), company.getName(), submittedBy });

		String emailBody = submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody();
		return createNotificationRequest(submissionInfo, subject, emailBody,
				submissionInfo.getEmailInfo().getNonClientRecipients());
	}

	public static NotificationRequestParam createAleNotificationRequest(Company company, SubmissionInfo submissionInfo) {
		String subject = null;
		if (company.isEligAle()) {
			subject = String.format(BSSMessageConfig.getProperty("aleNotificationSubject"),company.getName());
		} else {
			subject = String.format(BSSMessageConfig.getProperty("notAleNotificationSubject"), company.getName());
		}
        String emailBody = generateAleNotificationEmail(company);
		return createNotificationRequest(submissionInfo, subject, emailBody,
				submissionInfo.getEmailInfo().getClientRecipients());
	}

	private static NotificationRequestParam createNotificationRequest(SubmissionInfo submissionInfo, String subject,
			String emailBody, List<Recipient> recipients) {

		NotificationRequestParam request = new NotificationRequestParam();
		List<NotificationMessage> messages = new ArrayList<>();
		NotificationMessage message = new NotificationMessage();
		message.setMessageType("html");
		message.setSubject(subject);
		message.setCreateAttachment(false);
		message.setCompanyId(submissionInfo.getCompanyCode());
		message.setEmployeeId(submissionInfo.getSubmitStatusInfo().getUserId());
		message.setRecipients(recipients);
		message.setHtmlMsg(emailBody);
		message.setTransformRequired(false);
		messages.add(message);
		DeliveryChannel channel = new DeliveryChannel();
		channel.setChannel("email");
		request.setDeliveryChannel(channel);
		request.setNotificationMessages(messages);
		return request;

	}

	private static String generateClientConfirmationEmail(SubmissionInfo submissionInfo) {
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("debug", false);
		parameters.put("isDefaultSubmit", submissionInfo.isDefaultSubmit());
		parameters.put("isOtherInternalSubmit",
				submissionInfo.isResubmit() || BSSApplicationConstants.INTERNAL_SUBMIT_USER
						.equals(submissionInfo.getEmailInfo().getRecipientName()));
		parameters.put("submitter", submissionInfo.getEmailInfo().getRecipientName());
		parameters.put("emailContentBaseUrl", BSSMessageConfig.getProperty(EMAIL_CONTENT_BASE_URL));
		String adminBenefitPortalUrl = BSSMessageConfig.getProperty("admin.benefit.portal.url");
		parameters.put("adminBenefitPortalUrl", adminBenefitPortalUrl);

		return transform(parameters, EMAIL_CONFIRMATION_BDM_FTL);
	}
	
	private static String generateAleNotificationEmail(Company company) {
		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("emailContentAssetsUrl", BSSMessageConfig.getProperty(EMAIL_CONTENT_ASSETS_URL));
        parameters.put("benefitStartDt",company.getPlanStartDate());
        if(company.isEligAle()) {
            parameters.put("fplAmount",company.getRealmPlanYear().getAleAmount());
        }
		return company.isEligAle() ? transform(parameters, ALE_NOTIFICATION_FTL)
				: transform(parameters, NOT_ALE_NOTIFICATION_FTL);
	}
	private static String transform(Map<String, Object> parameters, String templateLocation) {
		StringWriter dataModel = new StringWriter();

		try {
			Template template = configuration.getTemplate(templateLocation, StandardCharsets.UTF_8.name());
			template.process(parameters, dataModel);
		} catch (TemplateException | IOException e) {
			throw new RuntimeException("Exception occured while processing the email template", e);
		}

		return dataModel.toString();
	}
}
