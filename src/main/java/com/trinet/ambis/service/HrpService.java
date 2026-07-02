package com.trinet.ambis.service;

import org.springframework.stereotype.Service;

@Service
public interface HrpService {

	/**
	 * This method checks whether user has access to given TriNet Gateway
	 * application.
	 * 
	 * @param appKey
	 * @return
	 */
	boolean hasAccessToGatewayApp(String appKey);
	
}
