package com.trinet.ambis.service.email.impl;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.EmailServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.CompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DocManagementService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.EmailAddressService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.EmailNotificationService;
import com.trinet.ambis.service.email.EmailService;
import com.trinet.ambis.service.email.dto.ClientConversionFailureEmailDto;
import com.trinet.ambis.service.email.dto.ClientConversionRequestDto;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.model.notification.Recipient;
import com.trinet.ambis.util.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class EmailServiceImpl implements EmailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

	@Autowired
	private EmailNotificationService emailNotificationService;

	@Autowired
	private EmailAddressService emailAddressService;

	@Autowired
	private EmailGenService emailGenService;

	@Autowired
	private DocManagementService docManagementService;

	@Autowired
	private PersonService personService;

	@Autowired
	private SubmitStatusService submitStatusService;

	@Autowired
	private CompanyDao companyDao;

	private static final String NO_USERID = "00000000000";

	@Override
	public void uploadStatementAndSendConfirmation(Company company, SubmissionInfo submissionInfo) {
		setRecipientName(company, submissionInfo);

		uploadConfirmationStatement(company, submissionInfo);

		if (StringUtils.isNotBlank(submissionInfo.getEmailInfo().getConfirmationStmtHtmlBody())) {
			try {
				if (isBDMEmailAvailable(company, submissionInfo)) {
					boolean sendClientEmail = BSSApplicationConstants.SUCCESS
							.equals(submissionInfo.getSubmitStatusInfo().getStatementUploadStatus())
							&& submissionInfo.getEmailInfo().isSendClientEmail();

					if (sendClientEmail) {
						sendClientConfirmationEmail(company, submissionInfo);
					}
				}
			} catch (Exception e) {
				CommonUtils.logExceptions(e, LOGGER, company.getCode(),
						submissionInfo.getSubmitStatusInfo().getUserId());
			}
			boolean sendNonClientEmail = false;
			if (submissionInfo.isResubmit() && !CommonServiceHelper.isTriNetCompany(company.getCode())) {
				sendNonClientEmail = submissionInfo.getEmailInfo().isSendClientEmail();
			} else if (!CommonServiceHelper.isTriNetCompany(company.getCode())) {
				sendNonClientEmail = true;
			}
			if (sendNonClientEmail) {
				sendNonClientConfirmationEmail(company, submissionInfo);
			}
		}
	}

	@Override
	public void resendConfirmationEmail(Company company, String confirmationNumber) {
		SubmitStatus submitStatus = submitStatusService.findByConfirmationNumber(company.getCode(), confirmationNumber);

		if (submitStatus == null) {
			throw new RuntimeException(String.format("No submit record found for company %s and Confirmation number %s",
					company.getCode(), confirmationNumber));
		}

		SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder().withEmailInfo().resendEmail(true)
				.buildEmailInfo().withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
				.buildPreSubmit();

		try {
			uploadStatementAndSendConfirmation(company, submissionInfo);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, LOGGER, company.getCode(), submissionInfo.getSubmitStatusInfo().getUserId());
		}
		submitStatus.setStatementUploadStatus(submissionInfo.getSubmitStatusInfo().getStatementUploadStatus());
		submitStatus.setEmailSentStatus(submissionInfo.getEmailInfo().isClientEmailSent());
		submitStatusService.createUpdateSubmitStatus(submitStatus);
	}

	private void uploadConfirmationStatement(Company company, SubmissionInfo submissionInfo) {
		try {
			StrategyData strategy = CommonServiceHelper.jsonToObject(submissionInfo.getSubmitStatusInfo().getPayload(),
					StrategyData.class);

			String confirmationStmtHtml = emailGenService.generateBssConfirmationStatementHtml(strategy,
					submissionInfo.getSubmitStatusInfo().getConfirmationNumber(), company,
					submissionInfo.getEmailInfo().getRecipientName(),
					submissionInfo.getSubmitStatusInfo().getServiceOrderNumber());
			submissionInfo.getEmailInfo().setConfirmationStmtHtmlBody(confirmationStmtHtml);

			boolean generateConfirmationStatement = false;

			if (submissionInfo.getEmailInfo().isResendEmail() && !BSSApplicationConstants.SUCCESS
					.equals(submissionInfo.getSubmitStatusInfo().getStatementUploadStatus())) {
				generateConfirmationStatement = true;
			}
			if (submissionInfo.isResubmit()
					&& BSSApplicationConstants.SUCCESS.equals(submissionInfo.getSubmitStatusInfo().getSubmitStatus())) {
				generateConfirmationStatement = true;
			}
			if (!submissionInfo.getEmailInfo().isResendEmail() && !submissionInfo.isResubmit()) {
				generateConfirmationStatement = true;
			}

			if (generateConfirmationStatement) {
				boolean statementUploadStatus = docManagementService.uploadConfirmationStatement(confirmationStmtHtml,
						company, submissionInfo.getSubmitStatusInfo().getConfirmationNumber());
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(
						statementUploadStatus ? BSSApplicationConstants.SUCCESS : BSSApplicationConstants.ERROR);
			}
		} catch (Exception e) {
			submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.ERROR);
			CommonUtils.logExceptions(e, LOGGER, company.getCode(), submissionInfo.getSubmitStatusInfo().getUserId());
		} finally {
			if (submissionInfo.getSubmitStatusInfo().getStatementUploadStatus() == null) {
				submissionInfo.getSubmitStatusInfo().setStatementUploadStatus(BSSApplicationConstants.ERROR);
			}
		}
	}

	private void setRecipientName(Company company, SubmissionInfo submissionInfo) {
		String recipientName = personService
				.prepareNameForConfirmationEmail(submissionInfo.getSubmitStatusInfo().getUserId(), company);
		submissionInfo.getEmailInfo().setRecipientName(recipientName);
	}

	private void sendClientConfirmationEmail(Company company, SubmissionInfo submissionInfo) {
		Set<String> emails = emailAddressService.getConfirmationStatementClientRecipients(company,
				submissionInfo.getSubmitStatusInfo().getUserId());
		List<Recipient> recipients = EmailServiceHelper.prepareRecipients(emails);
		submissionInfo.getEmailInfo().setClientRecipients(recipients);

		if (CollectionUtils.isNotEmpty(submissionInfo.getEmailInfo().getClientRecipients())) {
			NotificationRequestParam clientRequest = EmailServiceHelper.createClientNotificationRequest(company,
					submissionInfo);
			emailNotificationService.sendConfirmationEmail(clientRequest);
			submissionInfo.getEmailInfo().setClientEmailSent(true);
		}
	}

	private void sendNonClientConfirmationEmail(Company company, SubmissionInfo submissionInfo) {
		Set<String> emails = emailAddressService.getConfirmationStatementNonClientRecipients(company);
		List<Recipient> recipients = EmailServiceHelper.prepareRecipients(emails);
		submissionInfo.getEmailInfo().setNonClientRecipients(recipients);
		
		if (CollectionUtils.isNotEmpty(submissionInfo.getEmailInfo().getNonClientRecipients())) {
			NotificationRequestParam nonClientRequest = EmailServiceHelper.createNonClientNotificationRequest(company,
					submissionInfo);
			emailNotificationService.sendConfirmationEmail(nonClientRequest);
		}

	}

	private boolean isBDMEmailAvailable(Company company, SubmissionInfo submissionInfo) {
		boolean result = false;
		Map<String, Integer> adminCounts = null;
		if (submissionInfo.isDefaultSubmit()) {
			adminCounts = submissionInfo.getEmailInfo().getBdmCounts();
		} else {
			adminCounts = emailGenService.getAdminEmailCount(company.getCode());
		}
		if (MapUtils.isNotEmpty(adminCounts) && adminCounts.containsKey(company.getCode())) {
			result = true;
		}
		return result;
	}

}
