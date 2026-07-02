package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.service.model.FeatureFlag;

public interface FeatureFlagService {

	/**
	 * This method returns all the configured feature flags which are effective for
	 * given company.
	 * 
	 * @param companyCode
	 * @param realmYrId
	 * @return
	 */
	List<FeatureFlag> retrieveFeatureFlags(String companyCode, long realmYrId);
}
