/**
 * 
 */
package com.trinet.ambis.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparisonAdditonalBenefits;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.AdditionalBenefitPlanRates;
import com.trinet.ambis.service.model.AdditionalPlanRate;

/**
 * @author rvutukuri
 *
 */
@Service
public interface AdditionalBenefitPlanService {

	/**
	 * 
	 * @param company
	 * @return
	 */
	List<AdditionalBenefitPlanRates> getADBPlanCostByGroup(Company company, long strategyId);

	/**
	 * 
	 * @param company
	 * @param history
	 * @param adbPlanMap
	 * @return
	 */
	Map<String, Map<String, BigDecimal>> calculateAdditionalPlansCostByGroup(Company company, boolean history,
			Map<String, Set<String>> adbPlanMap, Map<String, AdditionalBenefitEmployeeDetails> employeeSelection);

	/**
	 * 
	 * @param company
	 * @param history
	 * @param adbPlanMap
	 * @return
	 */
	Map<String, BigDecimal> calculateAdditionalPlansCost(Company company, boolean history,
			Map<String, Set<String>> adbPlanMap);

	/**
	 * Returns of map of the company's additional benefitPlan and the plan's
	 * AdditionalPlanRate object - with rate and rate unit
	 * 
	 * @param company
	 * @param history
	 * @param adbPlanMap
	 * @return
	 */
	Map<String, AdditionalPlanRate> getAdditionalPlansRate(Company company, boolean history,
			Map<String, Set<String>> adbPlanMap);

	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @param planTypes
	 * @param templateNames
	 * @return
	 */
	List<PlanComparisonAdditonalBenefits> getAdditionalBenefitsCompareInformation(Company company, long strategyId, List<String> templateNames);


}