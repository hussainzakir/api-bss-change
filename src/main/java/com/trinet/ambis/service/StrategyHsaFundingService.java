/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

/**
 * @author hliddle
 *
 */
public interface StrategyHsaFundingService {
	
	/**
	 * @param id
	 * @return
	 */
	StrategyHsaFundingDto findById(long id);

	/**
	 * @param StrategyHsaFundingDto
	 * @return
	 */
	StrategyHsaFundingDto save(StrategyHsaFundingDto strategyHsaFundingDto);	
	
	/**
	 * @param strategyHsaFundingDtoList
	 * @return
	 */
	List<StrategyHsaFundingDto> saveAll(List<StrategyHsaFundingDto> strategyHsaFundingDtoList);
	
	/**
	 * @param strategyList
	 * @param company
	 * @param realmRuleConfigurations
	 */
	void createFutureStrategyHsaFunding(List<Strategy> strategyList, Company company,
			Map<String, String> realmRuleConfigurations);

}
