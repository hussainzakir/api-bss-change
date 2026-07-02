package com.trinet.ambis.service.outputs;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.trinet.ambis.rest.controllers.dto.outputs.BenefitCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeEmployeeCostSummary;

/**
 * 
 * @author rterle
 *
 */

public interface BenefitCostSummaryService {

	/**
	 * This method returns the data to generate the cost summary output report
	 *
	 * @param benefitTypeEmployeeCostSummary
	 * @param trinetStrategyId
	 * @param prospectId
	 * @param benfitPlanList
	 * @return
	 */
	CompletableFuture<BenefitCostSummary> getBenefitCostSummaryData(
			BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary, String trinetStrategyId, String prospectId, List<String> benfitPlanList);

}
