package com.trinet.ambis.service.outputs;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;

public interface PlanComparisonService {

	/**
     * This method returns the data required to generate the plan comparison report
     *
	 * @param company
	 * @param prospectRequest
	 * @param httpRequest
	 * @return
	 */
	CompletableFuture<Map<String, BasePlanComparison>> getPlanComparisonData(Company company,
			OutputRequest prospectRequest, HttpServletRequest httpRequest);
	
	BasePlanComparison getPlanCompareAssignmentDetails(String prospectPlanId, String trinetPlanId,
			String template, HttpServletRequest httpRequest, Company company);

	/**
	 * This method returns the plan comparison data for additional benefits
	 * 
	 * @param company
	 * @param prospectRequest
	 * @return
	 */
	CompletableFuture<Map<String, BasePlanComparison>> getAdditionalBenfitsCompareData(Company company, OutputRequest prospectRequest);

	/**
	 * This method returns the Tibco plan rates by plan
	 *
	 * @param company
	 * @param planIds
	 * @return Map of planId to RateDetail
	 */
	Map<String, RateDetail> getOMSPlanRatesByPlan(Company company, Map<String, Set<String>> omsPlanIdsByBenType);

}
