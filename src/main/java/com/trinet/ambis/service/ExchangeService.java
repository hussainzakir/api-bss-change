package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeCarrierDto;

public interface ExchangeService {

	/**
	 * Return list of exchange and carrier details for the given company ids
	 * 
	 * @param companyCode
	 * @param benExchange 
	 * @return
	 */
	List<ExchangeCarrierDto> getExchangeCarriers(String companyCode, BenExchngEnums benExchange);

	/**
	 * Returns exchange bands for the given company code
	 *
	 * @param companyCode
	 * @param benExchange 
	 * @return List<ExchangeBandsDto>
	 */
	List<ExchangeBandsDto> getExchangeBands(String companyCode, BenExchngEnums benExchange);

	/**
	 * Creates a company for the given company code and save exchange bands for the
	 * created company
	 * 
	 * @param exchangeBands
	 * @param companyCode
	 * @return
	 */
	List<ExchangeBandsDto> saveExchangeBands(List<ExchangeBandsDto> exchangeBands, String companyCode);
	
	
	/**
	 * Checks whether exchange offers medical or not
	 * 
	 * @param realmYearId
	 * @return boolean
	 */
	boolean isMedicalOffered(long realmYearId); 

}
