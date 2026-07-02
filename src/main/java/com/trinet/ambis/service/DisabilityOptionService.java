/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;

/**
 * @author rvutukuri
 *
 */
@Service
public interface DisabilityOptionService {

	/**
	 * This method is for getting all the DisabilityBenefitOptions.
	 * 
	 * @param company
	 * @return
	 */
	Set<AdditionalBenefitPlan> getDisabilityOptionsByRealmPlanYear(Company company);

	/**
	 * This method is for getting the DisabilityBenefitOption by list of
	 * benefitPlans.
	 * 
	 * @param bp
	 * @return
	 */
	AdditionalBenefitPlan getDisabilityOptionByPlans(List<String> benefitPlans, Company company, boolean isStandAlone);

	/**
	 * This method is for getting the benefitPlans by Option.
	 * 
	 * @param optionId
	 * @return
	 */
	List<DisabilityBenefitOptionPlans> getDisabilityPlansByOption(String optionId, Company company);

	/**
	 * 
	 * @param ltdPlan
	 * @param realmPlanYearId
	 * @param region
	 * @param benExchange
	 * @return
	 */
	String getDisabilitySTDPlanByLTDPlan(String ltdPlan, Long realmPlanYearId, String region, String benExchange);
}
