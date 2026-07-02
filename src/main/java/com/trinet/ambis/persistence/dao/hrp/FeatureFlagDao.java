package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import com.trinet.ambis.service.model.FeatureFlag;

public interface FeatureFlagDao {

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
