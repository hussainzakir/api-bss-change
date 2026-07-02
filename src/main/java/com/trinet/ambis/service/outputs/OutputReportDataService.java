package com.trinet.ambis.service.outputs;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;


public interface OutputReportDataService {

    /**
     * This method prepares the data required to generate the output report
     *
	 * @param prospectRequest
	 * @param company
	 * @param httpRequest
	 * @return
	 */
	OutputData getData(OutputRequest prospectRequest, Company company, HttpServletRequest httpRequest);

}
