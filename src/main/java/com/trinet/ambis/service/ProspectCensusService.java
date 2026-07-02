/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;

/**
 * 
 */
public interface ProspectCensusService {

	/**
	 * 
	 * @param prospectId
	 * @return
	 */
	List<ProspectCensusResponse> getProspectCensus(String prospectId);

}
