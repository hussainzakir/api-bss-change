package com.trinet.ambis.service.prospect;

import javax.servlet.http.HttpServletRequest;

public interface ProspectSubmitService {
	/**
	 * This method generates the BSS output report and submit the output report and
	 * benefit estimates details to SFDC for given strategyId
	 * 
	 * @param companyCode
	 * @param strategyId
	 * @param exchangeId
	 * @param request
	 */
	void submit(String companyCode, long strategyId, String exchangeId, HttpServletRequest request);
}
