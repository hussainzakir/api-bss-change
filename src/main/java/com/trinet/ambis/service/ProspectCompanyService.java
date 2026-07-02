package com.trinet.ambis.service;

import java.time.LocalDate;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.prospect.ProspectInfoResponse;

public interface ProspectCompanyService {

	/**
	 * This method is for getting the prospect company details.
	 * 
	 * @param code
	 * @param benExchange
	 * @return Company
	 */
	Company getProspectCompanyDetails(String code, BenExchngEnums benExchange);

	Company getProspectCompanyDetails(String code, long planYrId);

	/**
	 * This method returns prospect details from the prospect API
	 * 
	 * @param companyCode
	 * @param benExchange
	 * @return ProspectInfoResponse
	 */
	ProspectInfoResponse getProspectBasicDetails(String companyCode, BenExchngEnums benExchange);

	/**
	 * This method updates the expiry date of prospect company
	 *
	 * @param companyCode
	 * @param exchangeId
	 * @param expiryDate
	 */
	void updateProspectExpiryDate(String companyCode, String exchangeId, LocalDate expiryDate);
}