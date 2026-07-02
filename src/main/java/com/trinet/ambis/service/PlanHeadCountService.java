/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;
import com.trinet.ambis.service.model.PlanHeadCount;

public interface PlanHeadCountService {
	/**
	 * 
	 * @param company
	 * @return
	 */
	List<PlanHeadCount> getPlanHeadCount(Company company, Long strategyId);

	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @return
	 */
	List<BenefitProgramHeadCountPlans> getBenefitProgramHeadCountPlans(Company company, Long strategyId);

}
