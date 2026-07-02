package com.trinet.ambis.service.impl;

import com.trinet.ambis.persistence.dao.hrp.PersonDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.PersonService;

@Service
public class PersonServiceImpl implements PersonService {

	@Autowired
	private PersonDao personDao;

	@Autowired
	PsDao psDao;

	private static final String DEFAULT_ADDRESS = "Valued Client";

	@Override
	public String getPersonFirstAndLastName(String userId) {
		return personDao.getFirstandLastName(userId);
	}

	@Override
	public String prepareNameForConfirmationEmail(String userId, Company company) {
		String userName = psDao.getEmployeeFullName(userId);
		if (company.isTMTUser()) {
			userName = BSSApplicationConstants.INTERNAL_SUBMIT_USER;
		}
		else if (!StringUtils.isNotBlank(userName)) {
			userName = DEFAULT_ADDRESS;
		}
		return userName;
	}

	/**
	 * Method that returns the Email Address of the designated employee who is responsible for CustomerSetup at new client.
	 *
	 * @param loggedInUserId
	 * @return
	 */
	@Override
	public String getCSAuthEmail(String loggedInUserId) {
		return personDao.getCSAuthEmail(loggedInUserId);
	}

	/**
	 * Method that returns Email Address of HR Manager assigned to the new client.
	 *
	 * @param companyId
	 * @return
	 */
	@Override
	public String getCompanyHrmEmail(String companyId) {
		return personDao.getCompanyHrmEmail(companyId);
	}

}
