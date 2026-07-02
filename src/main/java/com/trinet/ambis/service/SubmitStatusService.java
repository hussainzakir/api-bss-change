package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SubmitStatus;

@Service
public interface SubmitStatusService {
	
	/**
	 * Create or updated the SubmitStatus entry for given submitStatus
	 * @param submitStatus
	 * @return SubmitStatus
	 */
	public SubmitStatus createUpdateSubmitStatus(SubmitStatus submitStatus);

	/**
	 * Find SubmitStatus entity for given companyCode and confirmationNumber
	 * @param companyCode
	 * @param confirmationNumber
	 * @return SubmitStatus
	 */
	public SubmitStatus findByConfirmationNumber(String companyCode, String confirmationNumber);

	/**
	 * Find status for latest submitted SubmitStatus entry for given companyCode
	 * @param companyCode
	 * @return SubmitStatus
	 */
	public SubmitStatus findLatestSubmitStatusBy(String companyCode);
	
	/**
	 * Update and commit the changes.
	 * @param submitStatus
	 * @return SubmitStatus
	 */
	public SubmitStatus updateAndCommit(SubmitStatus submitStatus);
	
	/**
	 * Find SubmitStatus entity for given confirmationNumber
	 * 
	 * @param confirmationNumber
	 * @param statuses
	 * 
	 * @return SubmitStatus
	 */
	public SubmitStatus findByConfirmationNumberAndStatus(String confirmationNumber, Set<String> statuses);

	
	/**
	 * Returns the SubmitStatus list for given company, it's realm plan year id and
	 * statuses.
	 * 
	 * @param company
	 * @param statuses
	 * @return
	 */
	public List<SubmitStatus> findByCompanyAndPlanYearIdAndStatuses(Company company, Set<String> statuses);
	
	/**
	 * This method updates submit status for given confirmationNumber and
	 * companyCode.
	 * 
	 * @param status
	 * @param confirmationId
	 * @param companyCode
	 * @return
	 */
	int update(String status, String confirmationId, String companyCode);

	/**
	 * This method updates statementUploadStatus and emailSent for given
	 * confirmationNumber and companyCode.
	 * 
	 * @param statementUploadStatus
	 * @param emailSentStatus
	 * @param confirmationNumber
	 * @param companyCode
	 * @return
	 */
	int update(String statementUploadStatus, boolean emailSentStatus, String confirmationId, String companyCode);

	/**
	 * This method updates status and updateTime for given
	 * confirmationNumber and companyCode.
	 * 
	 * @param status
	 * @param updateTime
	 * @param confirmationNumber
	 * @param companyCode
	 * @return
	 */
	int update(String status, Date updateTime, String confirmationId, String companyCode);
}
