/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.model.CarrierMinimumFunding;

/**
 * @author schaudhari
 *
 */
@Service
public interface BenefitPlanService {
	/**
	 * This method returns the list of all MDV benefit plan ids for given company.
	 * 
	 * @param company
	 * @return
	 */
	Set<String> getAllPrimaryBenefitPlansForPlanRates(Company company);

	Map<String, BenefitPlan> getRegionalBasePlanMapping(RealmPlanYear rpy);

    List<CarrierMinimumFunding> getLowestCostPlanPerCarrier(Company company);
}
