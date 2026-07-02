package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.AppConfigurationsDao;
import com.trinet.ambis.persistence.model.AppConfigurations;
import com.trinet.ambis.service.AppConfigurationService;

@Service
public class AppConfigurationServiceImpl implements AppConfigurationService {

	@Autowired
	AppConfigurationsDao appConfigurationsDao;

	@Override
	public List<AppConfigurations> findAll() {
		return appConfigurationsDao.findAll();
	}

}