package com.trinet.ambis.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;

@Service
public interface DocManagementService {

	/**
	 * This method is for Uploading confirmation statement.
	 * 
	 * @param nonClientEmailHtml
	 * @param company
	 * @param confirmationId
	 * @return
	 */
	boolean uploadConfirmationStatement(String nonClientEmailHtml, Company company, String confirmationId);

	/**
	 * This method returns the map of confirmation statement document url mapped to
	 * document file name for given company.
	 * 
	 * @param companyCode
	 * @return
	 */
	Map<String, String> retrieveConfirmationStatementUrls(String companyCode);
}
