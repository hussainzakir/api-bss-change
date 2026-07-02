package com.trinet.ambis.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.AppConfigurations;
import com.trinet.ambis.persistence.model.AppRules;
import com.trinet.ambis.service.AppConfigurationService;
import com.trinet.ambis.service.AppRuleService;
import com.trinet.ambis.service.AppRulesConfigService;

@Service
public class AppRuleConfigServiceImpl implements AppRulesConfigService {

	@Autowired
	AppRuleService appRuleService;
	
	@Autowired
	AppConfigurationService appConfigurationService;

	@Override
	public Map<String, String> getAllRulesAndConfigs() {
		Map<String, String> result = new HashMap<>();
		List<AppRules> appRules = appRuleService.findAll();
		appRules.stream().forEach(appRule -> result.put(appRule.getKey(), String.valueOf(appRule.isValue())));
		List<AppConfigurations> appConfigurations = appConfigurationService.findAll();
		appConfigurations.stream().forEach(appConfiguration -> result.put(appConfiguration.getKey(), appConfiguration.getValue()));
		
		return result;
	}

}