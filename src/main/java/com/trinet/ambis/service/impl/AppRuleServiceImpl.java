package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.AppRulesDao;
import com.trinet.ambis.persistence.model.AppRules;
import com.trinet.ambis.service.AppRuleService;

@Service
public class AppRuleServiceImpl implements AppRuleService {

	@Autowired
	AppRulesDao appRulesDao;

	@Override
	public List<AppRules> findAll() {
		return appRulesDao.findAll();
	}

}