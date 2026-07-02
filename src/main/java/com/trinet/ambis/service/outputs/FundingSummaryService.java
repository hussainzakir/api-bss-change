package com.trinet.ambis.service.outputs;

import java.util.concurrent.CompletableFuture;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.FundingSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;

/**
 * 
 * @author rterle
 *
 */

public interface FundingSummaryService {

	/**
	 * This method helps to fetch the data related to Funding summary of TriNet
	 * Strategy for a prospect company to pass to template for PDF generation
	 * 
	 * @param company
	 * @param outputRequest
	 * @return FundingSummary
	 */
	CompletableFuture<FundingSummary> getFundingSummaryData(Company company, OutputRequest outputRequest);

}
