package com.trinet.ambis.service;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;

public interface BenefitOptionsService {

	/**
	 * When a client is granted an exception and is not offering medical plans, HSA plans are also not available.
	 * Clear all HSA option parameters so that conflicting information is not displayed in PeopleSoft.
	 * @param company
	 * @param group
	 */
	public void clearHSAOptions( Company company, BenefitGroup group );

	/**
	 * Generate new rows for the client benefit option records in PeopleSoft
	 * @param company
	 * @param group
	 * @param hsaOptions the HSA funding options chosen for this strategy or null if 
	 * no HSA options have been created
	 */
	public void createClientBenefitOptions( Company company, BenefitGroup group, StrategyHsaFundingDto hsaOptions );

	/**
	 * After client selections have been applied to a benefit program, this method
	 * could be implemented to rebuild the OPTN3 records from the new benefit program.
	 * @param company
	 * @param group
	 */
	public void regenerateOptn3( Company company, BenefitGroup group );

	/**
	 * During new client setup, the application may have needed to create future-dated
	 * benefit option rows.  Once these are no longer needed, they should be deleted.
	 * @param company
	 * @param group
	 */
	public void deleteFutureOptions( Company company, BenefitGroup group );

}
