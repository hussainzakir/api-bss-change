package com.trinet.ambis.service;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BSSReportDetails;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;

/**
 * @author rterle
 *
 */
public interface OutputRequestBuilder {
	
	/**
	 * @param prospectRequest
	 * @param company
	 * @param httpRequest
	 * @return
	 */
	public BSSReportDetails prepareBssReportRequest(OutputRequest prospectRequest, Company company,
			HttpServletRequest httpRequest);

}
