package com.trinet.ambis.service;

import com.trinet.ambis.persistence.model.Company;

public interface RateSystemService {
	/**
	 * Returns the rate_type from the cached FlexRateResponse for the given company and effective date.
	 *
	 * @param company       the company whose plan rates are being queried
	 * @return rate_type string from FlexRateResponse
	 */
	String getRateSystemRateType(Company company);
}
