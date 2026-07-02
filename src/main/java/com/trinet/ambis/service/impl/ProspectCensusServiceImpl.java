/**
 * 
 */
package com.trinet.ambis.service.impl;

import java.util.Collections;
import java.util.List;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

/**
 * 
 */
@Service
public class ProspectCensusServiceImpl implements ProspectCensusService {
	@Autowired
	ProspectServiceRestClient prospectServiceRestClient;

	@Override
	public List<ProspectCensusResponse> getProspectCensus(String prospectId) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put("prospectId", List.of(prospectId));
		ParameterizedTypeReference<ReturnResponse<List<ProspectCensusResponse>>> prospectDetailsBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<List<ProspectCensusResponse>> prospectApiGetRequest = ProspectApiRequest.<List<ProspectCensusResponse>>builder()
				.method(HttpMethod.GET)
				.uri(ProspectURIConstants.PROSPECT_CENSUS_URI)
				.queryParams(map)
				.parameterizedTypeReference(prospectDetailsBean).build();

		List<ProspectCensusResponse> data = prospectServiceRestClient
				.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		return CollectionUtils.isNotEmpty(data) ? data : Collections.emptyList();
	}

}
