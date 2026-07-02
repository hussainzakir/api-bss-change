package com.trinet.ambis.service.prospect;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;

public interface ProspectPlanHeadCountService {
	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @return
	 */
	List<BenefitProgramHeadCountPlans> getBenefitProgramHeadCountPlans(Company company, Long strategyId);

	/**
	 * 
	 * @param company
	 * @param strategyId
	 * @return
	 */
	Map<String, ActiveEligibleEECount> getProspectEligibleEmployeeCount(Company company, long strategyId);
}