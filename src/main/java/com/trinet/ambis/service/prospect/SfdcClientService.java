package com.trinet.ambis.service.prospect;

import org.springframework.util.MultiValueMap;

public interface SfdcClientService {

	/**
	 * Sends employee cost, plan comparison, appendix report and quote summary to
	 * sfdc
	 * 
	 * @param bodyMap
	 */
	void sendProposal(MultiValueMap<String, Object> bodyMap);

}
