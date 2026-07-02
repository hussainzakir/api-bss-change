package com.trinet.ambis.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYearConfiguration;
import com.trinet.ambis.persistence.model.RealmPlanYearRule;
import com.trinet.ambis.service.PsConfigurationService;
import com.trinet.ambis.service.RealmPlanYearConfigurationService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@Service
public class RealmPlanYearRuleConfigServiceImpl implements RealmPlanYearRuleConfigService {

	@Autowired
	RealmPlanYearConfigurationService configService;

	@Autowired
	RealmPlanYearRuleService ruleService;
	
	@Autowired
	PsConfigurationService psConfigurationService;

	@Override
	public Map<String,String> getRulesAndConfigsByRealmPlanYearId( Company company ) {
		Map<String,String> map = getRulesAndConfigsByRealmPlanYearId( company.getRealmPlanYear().getId() );
		boolean pickChooseFlag = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		map.put( "PICK_CHOOSE_FLAG", pickChooseFlag ? "true" : "false" );
		if (company.isProspectCompany()) {
			map.put("HSA_ENABLED", "false");
			map.put("WAIVER_ALLOWANCE", "false");
		}
		return map;
	}

	@Override
	public Map<String, String> getRulesAndConfigsByRealmPlanYearId(long realmPlanYearId) {
		Map<String, String> rulesAndConfigs = new HashMap<>();

		List<RealmPlanYearRule> rules = ruleService.findByRealmPlanYearId(realmPlanYearId);
		List<RealmPlanYearConfiguration> configs = configService.findByRealmPlanYearId(realmPlanYearId);

		for (RealmPlanYearRule rule : rules) {
			String ruleKey = rule.getId().getRuleKey();
			rulesAndConfigs.put(ruleKey.toUpperCase(), String.valueOf( rule.isRuleValue() ));
		}

		for (RealmPlanYearConfiguration config : configs) {
			String configKey = config.getId().getConfigKey();
			if (rulesAndConfigs.containsKey(configKey.toUpperCase())) {
				throw new IllegalStateException(
						"Same key can not be used for Configuration and Rule. Key :" + config.getId().getConfigKey());
			}
			rulesAndConfigs.put(configKey.toUpperCase(),
					config.getConfigValue() == null ? StringUtils.EMPTY : config.getConfigValue());
		}

		return rulesAndConfigs;
	}
	
	public Map<String, String> getPsConfigsByDate(Date effDate) {
		Map<String, String> psConfigs = new HashMap<>();
		
		psConfigs.putAll(psConfigurationService.findByEffDate(effDate));
		
		return psConfigs;
	}

	@Override
	public boolean findPickChooseWithExceptions( long realmYearId, String companyCode, Date effdt ) {
		return ruleService.findPickChooseWithExceptions(realmYearId, companyCode, effdt);
	}

}