/**
 * 
 */
package com.trinet.ambis.service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;

import java.util.List;
import java.util.Map;

/**
 * 
 */
public interface ProspectPlanAvailabilityService {

	/**
	 * 
	 * @param company
	 */
	List<PlanAvailableResponse> getProspectEmployeePlanAvailability(Company company, List<String> plans);


	Map<String, List<String>> getProspectEmployeePlansByPlanType(Company company, List<String> plans);


	List<HrisPlanResponse> getProspectEmployeeHrisPlanAvailability(Company company);

	List<HrisPlanResponse> getProspectEmployeeHrisPlanAvailability(Company company, String benefitType);
}
