package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.FeatureFlagDao;
import com.trinet.ambis.service.FeatureFlagService;
import com.trinet.ambis.service.model.FeatureFlag;

@Service
public class FeatureFlagServiceImpl implements FeatureFlagService {
	
	@Autowired
	private FeatureFlagDao dao;

	@Override
	public List<FeatureFlag> retrieveFeatureFlags(String companyCode, long realmYrId) {
		return dao.retrieveFeatureFlags(companyCode, realmYrId);
	}

}
