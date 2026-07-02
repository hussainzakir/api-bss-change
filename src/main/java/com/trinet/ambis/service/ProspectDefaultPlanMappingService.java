/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;

/**
 * 
 */
public interface ProspectDefaultPlanMappingService {

	/**
	 * This method creates employee default regional plan mapping assignment for a given company
	 * 
	 * @param company
	 */
	void createCensusDefaultRegionalPlanMapping(Company company);
	
	/**
	 * This method updates employee default regional plan mapping assignment for all
	 * the realm companies for given company and list of employees
	 * 
	 * @param company
	 * @param census
	 */
	void createCensusDefaultRegionalPlanMapping(Company company, List<ProspectCensusResponse> census);

}
