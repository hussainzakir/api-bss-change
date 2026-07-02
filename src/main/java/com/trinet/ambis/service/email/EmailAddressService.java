package com.trinet.ambis.service.email;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;

@Service
public interface EmailAddressService {
	/**
	 * @param company
	 * @param userId
	 * @return
	 */
	Set<String> getConfirmationStatementClientRecipients(Company company, String userId);

	/**
	 * Please refer the below confluence page to get list of non client recipients
	 * https://confluence.trinet-devops.com/display/QE/Confirmation+Emails
	 * 
	 * @param company
	 * @return
	 */
	Set<String> getConfirmationStatementNonClientRecipients(Company company);
	
	String getEmployeeEmail(String companyCode, String employeeId);
}
