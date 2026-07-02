package com.trinet.ambis.service;

import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;

public interface BSSStatusDetailsService {

	/**
	 * @param companyCode
	 * @return
	 */
	BSSStatusDetailsDto getBssStatusDetail(String code);
}
