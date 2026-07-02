package com.trinet.ambis.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trinet.ambis.helper.EmailServiceHelper;
import com.trinet.ambis.helper.PlanCompareHelper;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.FeatureFlagService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.FeatureFlagUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

import freemarker.template.Configuration;

@Component
public class StaticContextInitializer {

	@Autowired
	private RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	@Autowired
	private AppRulesConfigService appRuleConfigService;

	@Autowired
	private Configuration freemarkerConfiguration;

	@Autowired
	private FeatureFlagService featureFlagService;

	@Autowired
	private PlanCompareService planCompareService;
	
	@PostConstruct
	public void init() {
		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigService);
		FeatureFlagUtils.setFeatureFlagService(featureFlagService);
		AppRulesAndConfigsUtils.setAppRuleConfigService(appRuleConfigService);
		EmailServiceHelper.setConfiguration(freemarkerConfiguration);
		PlanCompareHelper.setPlanCompareService(planCompareService);
	}
}