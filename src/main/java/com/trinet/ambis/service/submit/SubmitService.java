package com.trinet.ambis.service.submit;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.SubmissionInfo;

@Service
public interface SubmitService {
	/**
	 * This method performs the below task prior to people-soft submission.
	 * Upload confirmation Statement.
	 * Send client and internal colleague confirmation email
	 * Update statement upload status and email sent status
	 * Send SOX email.
	 * 
	 * @param company
	 * @param submissionInfo
	 */
	public CompletableFuture<Void> preSubmit(Company company, SubmissionInfo submissionInfo);

	/**
	 * This method performs the below task post to people-soft submission.
	 * 
	 * If Re-submission - Upload confirmation Statement.
	 * If Re-submission - Send client and internal colleague confirmation email.
	 * If Re-submission - Update statement upload status and email sent status.
	 * If Re-submission - Send SOX email.
	 * Update submit status.
	 * Send support email in case of submit failure.
	 * 
	 * 
	 * @param company
	 * @param submissionInfo
	 */
	public void postSubmit(Company company, SubmissionInfo submissionInfo);

	/**
	 * Only for non queued submission.
	 * This method kicks off the new thread for people-soft submission. Pre-submit and
	 * post-submit tasks will be performed.
	 * 
	 * @param company
	 * @param strategyData
	 * @param userId
	 * @param sendClientEmail
	 * @param resubmit
	 */
	void submit(Company company, StrategyData strategyData, String userId, boolean sendClientEmail, boolean resubmit);

	/**
	 * This method kicks off the thread for people-soft default submission.
	 * Pre-submit and post-submit tasks will be performed.
	 * 
	 * @param companyCode
	 * @param quarter
	 * @param userId
	 */
	void defaultSubmit(String companyCode, String quarter, String userId);

	/**
	 * Processes qualified termed clients for the given quarter.
	 * Discovers termed companies that have flex rates and submits default strategies for them.
	 * Only processes if companyCode is the default company code.
	 * 
	 * @param companyCode the company code to check
	 * @param quarter the quarter to process
	 */
	void defaultSubmitTermedClients(String companyCode, String quarter);
	
}
