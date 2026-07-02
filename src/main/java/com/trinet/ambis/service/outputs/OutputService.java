package com.trinet.ambis.service.outputs;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.service.prospect.dto.BenTypeOfferRes;

public interface OutputService {
	
	/**
	 * This method calls DocGen API to generate and download the output report pdf file
	 * 
	 * @param request
	 * @param company
	 * @param httpRequest
	 * @return
	 */
	byte[] generateReport(OutputRequest request, Company company, HttpServletRequest httpRequest);
	
	/**
	 * @param strategyIds
	 * @return
	 */
	List<BenTypeOfferRes> getPlanTypeOfferedDetails(List<Long> strategyIds, List<String> plantypes);

	/**
	 * This method generates employee cost and plan comparison report
	 * 
	 * @param company
	 * @param strategyId
	 * @param httpRequest
	 * @return
	 */
	byte[] generateEmployeeCostAndPlanComparisonReport(Company company, long strategyId,
			HttpServletRequest httpRequest);

	/**
	 * This method generates appendix report
	 * 
	 * @param company
	 * @param strategyId
	 * @param httpRequest
	 * @return
	 */
	byte[] generatePlanAppendixReport(Company company, long strategyId, HttpServletRequest httpRequest);

}

