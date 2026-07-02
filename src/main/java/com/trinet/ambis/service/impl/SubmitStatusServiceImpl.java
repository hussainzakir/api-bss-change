package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.dao.hrp.SubmitStatusDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.SubmitStatusService;

@Service
public class SubmitStatusServiceImpl implements SubmitStatusService {
	
	@Autowired
	SubmitStatusDao submitStatusDao;
	
	@Override
	public SubmitStatus createUpdateSubmitStatus(SubmitStatus s) {
		return submitStatusDao.saveAndFlush(s);
	}

	@Override
	public SubmitStatus findByConfirmationNumber(String companyCode, String confirmationNumber) {
		return submitStatusDao.findByCompanyAndConfirmationNumber(companyCode, confirmationNumber);
	}

	@Override
	public SubmitStatus findLatestSubmitStatusBy(String companyCode) {
		return submitStatusDao.findLatestSubmitStatusBy(companyCode);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public SubmitStatus updateAndCommit(SubmitStatus submitStatus) {
		return submitStatusDao.saveAndFlush(submitStatus);
	}

	@Override
	public SubmitStatus findByConfirmationNumberAndStatus(String confirmationNumber, Set<String> statuses) {
		return submitStatusDao.findByConfirmationNumberAndStatusIn(confirmationNumber, statuses);
	}

	@Override
	public List<SubmitStatus> findByCompanyAndPlanYearIdAndStatuses(Company company, Set<String> statuses) {
		return submitStatusDao.findByCompanyAndRealmYrIdAndStatusIn(company.getCode(), company.getRealmPlanYearId(),
				statuses);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int update(String status, String confirmationId, String companyCode) {
		return submitStatusDao.update(status, confirmationId, companyCode);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int update(String statementUploadStatus, boolean emailSent, String confirmationId, String companyCode) {
		return submitStatusDao.update(statementUploadStatus, emailSent, confirmationId, companyCode);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int update(String status, Date updateTime, String confirmationId, String companyCode) {
		return submitStatusDao.update(status, updateTime, confirmationId, companyCode);
	}
}