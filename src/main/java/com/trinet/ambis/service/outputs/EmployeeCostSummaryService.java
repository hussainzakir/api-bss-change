package com.trinet.ambis.service.outputs;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeEmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;

public interface EmployeeCostSummaryService {

	/**
	 * This method returns the data required to generate the Employee Cost Summary
	 * output report
	 *
	 * @param company
	 * @param outputRequest
	 * @return BenefitTypeEmployeeCostSummary -- Cost Summary Data
	 */
	CompletableFuture<BenefitTypeEmployeeCostSummary> getCostSummaryData(OutputData data, Company company, OutputRequest outputRequest);

	/**
	 * This method returns the data required to generate the OMS Employee Cost Summary
	 * output report and model compare data
	 *
	 * @param strategyIds
	 * @param benTypeCodes
	 * @param prependCarrierName
	 */
	Optional<List<StrategyGroupEmployeePlanRateData>> getOmsStrategyCostResponse(List<Long> strategyIds, List<String> benTypeCodes, boolean prependCarrierName);
}
