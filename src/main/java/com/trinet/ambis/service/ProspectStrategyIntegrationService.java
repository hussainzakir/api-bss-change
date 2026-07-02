package com.trinet.ambis.service;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse;

public interface ProspectStrategyIntegrationService {

	/**
	 * This method returns the submitted strategy's data for the prospect to be sent
	 * to SFDC.
	 *
	 * @param strategyId
	 * @param company
	 * @param benExchngEnums
	 * @return
	 */
	ProspectBenefitsSummaryTotalsResponse getBenefitsSummaryTotalsForStrategy(Long strategyId, Company company,
			BenExchngEnums benExchngEnums);

}
