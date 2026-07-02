package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearConfigurationDao;
import com.trinet.ambis.persistence.model.RealmPlanYearConfiguration;
import com.trinet.ambis.service.RealmPlanYearConfigurationService;

@Service
public class RealmPlanYearConfigurationServiceImpl implements RealmPlanYearConfigurationService {

	@Autowired
	RealmPlanYearConfigurationDao realmPlanYearConfigurationDao;

	@Override
	public List<RealmPlanYearConfiguration> findByRealmPlanYearId(long realmPlanYearId) {
		return realmPlanYearConfigurationDao.findByIdRealmPlanYearId(realmPlanYearId);
	}
}