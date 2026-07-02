package com.trinet.ambis.service;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;

@Service
public interface PersonService {
	/**
	 * This method return first and last name of the passed employee
	 */
	String getPersonFirstAndLastName(String userPersonId);

	String prepareNameForConfirmationEmail(String userId, Company company);

	/**
	 * Method that returns the Email Address of the designated employee who is responsible for CustomerSetup at new client.
	 *
	 * @param loggedInUserId
	 * @return
	 */
	String getCSAuthEmail(String loggedInUserId);

	/**
	 * Method that returns Email Address of HR Manager assigned to the new client.
	 *
	 * @param companyId
	 * @return
	 */
	String getCompanyHrmEmail(String companyId);
}
