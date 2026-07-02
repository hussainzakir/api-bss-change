package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto.CarrierBand;

public interface BandCodesService {

	/**
	 * Save the bands codes for the given company id
	 * 
	 * @param carrierBand
	 * @param companyId
	 */
	public void save(CarrierBand carrierBand, long companyId);

	public void deleteCompanyBands(List<Long> companyIds);

	public String getBandCodeByType(String naicsCode, Date effDate, String bandCodeType, BenExchngEnums exchange);
}
