package com.trinet.ambis.persistence.dao.hrp;

public interface PersonDao {
	/*
	 * Method that returns the first and last name of the employee whose Employee ID has been passed as the input
	 */
	String getFirstandLastName(String userId);

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
