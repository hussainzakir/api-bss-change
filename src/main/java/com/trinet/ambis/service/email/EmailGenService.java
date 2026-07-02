package com.trinet.ambis.service.email;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.email.dto.SubmissionEmailDto;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.service.model.StrategyData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author rvutukuri
 *
 */
@Service
public interface EmailGenService {

	/**
	 * @param supportEmailDto This DTO contains the following fields: userId, companyCode, sendToBSS
	 */
	void createSupportEmail(SupportEmailDto supportEmailDto);

	/**
	 * Generate and send an email notification to BSS team that a real-time sync process has failed
	 * @param company
	 * @param logEyeCatcher A UID to provide an eye-catcher string in the email that can 
	 * then be searched for in the log
	 */
	void createSyncFailureEmail( String company, UUID logEyeCatcher );

	/**
	 * 
	 * @param count
	 */
	void createDefaultEmail(int count, String userId);

	/**
	 * 
	 * @param count
	 * @param failedClients
	 */
	void createPreLoadEmail(int count, String failedClients, String userId);

	/**
	 * Sends an email to members of SOX listing all clients with successfully
	 * submitted strategies where the client was not sent an email or their
	 * confirmation was not uploaded successfully.
	 * In case of re-submission if send email flag is false then that record
	 * will not be included in the email sent report but if the statement upload
	 * fails then it will be included in the upload failure report. 
	 * 
	 * Additionally, an email is sent to Tier 2 regarding the unsuccessful 
	 * confirmation uploads
	 */
	void generateSubmissionIssueReport(String companyCode, String userId);

	/**
	 * Returns a set of email addresses for the passed in company's BDM (non-001
	 * clients) or BEN_CORP_AD (001 client)
	 * 
	 * @param companyCode
	 * @return
	 */
	Set<String> getAdminEmails(String companyCode);

	/**
	 * Returns true if passed in code is included in the adminCounts map. Otherwise,
	 * returns false.
	 * 
	 * @param adminCounts
	 * @param companyCode
	 * @return
	 */
	boolean isEmailAvailableForCompany(Map<String, Integer> adminCounts, String companyCode);

	/**
	 * Returns a map of company codes and the count of their BDM (non-001 clients)
	 * or BEN_CORP_AD (001 client) email addresses
	 * 
	 * @param companyList
	 * @return
	 */
	Map<String, Integer> getAdminEmailCount(String companyCode);

	/**
	 * Returns a map of company codes and the count of their BDM email addresses
	 * 
	 * @param companyList
	 * @return
	 */
	Map<String, Integer> getBDMCount(List<String> companyList);

	/**
	 * Returns a map of company codes and the count of their BEN_CORP_AD email
	 * addresses
	 * 
	 * @param companyList
	 * @return
	 */
	Map<String, Integer> getBenCorpAdminCount(List<String> companyList);

	/**
	 * 
	 * @param dto
	 * @param uniqueId
	 * @param company
	 * @param name
	 * @param serviceOrderNum
	 * @return
	 */
	String generateBssConfirmationStatementHtml(StrategyData dto, String uniqueId, Company company, String name,
			String serviceOrderNum);

	/**
	 * Send BSS submission failure mail for single and multiple clients(quarter)
	 * 
	 * @param submissionEmailDto
	 */
	void sendBssSubmissionFailureEmail(SubmissionEmailDto submissionEmailDto);

}
