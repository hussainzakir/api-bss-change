package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;

import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierBandDto;
import com.trinet.ambis.persistence.dao.hrp.dto.ExchangeCarrierDetailsDto;

public interface ExchangeDao {

	/**
	 * @param companyCode
	 * @param hqState
	 * @param zipCode
	 * @param benStartDt
	 * @return
	 */
	List<ExchangeCarrierDetailsDto> getExchangeCarriers(String companyCode, String hqState, String zipCode,
			Date benStartDt);

	/**
	 * @param companyCode
	 * @param benStartDt
	 * @return
	 */
	List<ExchangeCarrierBandDto> getExchangeCarriersBands(String companyCode, Date benStartDt);
}