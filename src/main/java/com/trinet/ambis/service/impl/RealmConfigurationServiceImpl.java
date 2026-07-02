package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.RealmConfigurationDao;
import com.trinet.ambis.persistence.model.RealmConfiguration;
import com.trinet.ambis.service.RealmConfigurationService;

@Service
public class RealmConfigurationServiceImpl implements RealmConfigurationService {

	@Autowired
	RealmConfigurationDao realmConfigurationDao;

	@Override
	public List<RealmConfiguration> findByRealmId(long realmId) {
		return realmConfigurationDao.findByIdRealmId(realmId);
	}
}