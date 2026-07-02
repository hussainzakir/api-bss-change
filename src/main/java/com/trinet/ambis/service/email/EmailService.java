package com.trinet.ambis.service.email;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.email.dto.ClientConversionRequestDto;
import com.trinet.ambis.service.model.SubmissionInfo;

public interface EmailService {

	/**
	 * This method will upload the confirmation statement and then send the
	 * confirmation email to client and internal colleagues.
	 * For all submission except resubmit, the upload will happen regardless of the submit status. 
	 * If upload fails then no confirmation email will be sent out to client.
	 * In case of Re-submission, the statement will be uploaded only if Submit Status is SUCCESS.
	 * In case of Re-submission, the upload will happen regardless of send email flag value.
	 * In case of Re-submission, the email will be sent out only if send email flag is true.
	 * 
	 * @param company
	 * @param submissionInfo
	 */
	void uploadStatementAndSendConfirmation(Company company, SubmissionInfo submissionInfo);

	/**
	 * This method will upload the confirmation statement if existing upload status
	 * is ERROR and then send the confirmation email to client and internal
	 * colleagues.
	 * 
	 * @param company
	 * @param confirmationNumber
	 */
	void resendConfirmationEmail(Company company, String confirmationNumber);

}
