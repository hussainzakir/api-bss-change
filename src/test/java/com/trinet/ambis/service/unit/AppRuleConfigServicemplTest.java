package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.trinet.ambis.service.impl.AppRuleConfigServiceImpl;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.model.AppConfigurations;
import com.trinet.ambis.persistence.model.AppRules;
import com.trinet.ambis.service.AppConfigurationService;
import com.trinet.ambis.service.AppRuleService;
import com.trinet.ambis.service.AppRulesConfigService;

@RunWith(MockitoJUnitRunner.class)
public class AppRuleConfigServicemplTest extends ServiceUnitTest {

	@InjectMocks
    AppRuleConfigServiceImpl appRulesConfigService;

	@Mock
	AppRuleService appRuleService;

	@Mock
	AppConfigurationService appConfigurationService;

	@Test
	public void findAll() {
		AppRules appRules = new AppRules();
		appRules.setKey("RULE_KEY");
		appRules.setValue(true);
		AppConfigurations appConfigurations = new AppConfigurations();
		appConfigurations.setKey("CONFIG_KEY");
		appConfigurations.setValue("CONFIG_VALUE");

		when(appRuleService.findAll()).thenReturn(Arrays.asList(appRules));
		when(appConfigurationService.findAll()).thenReturn(Arrays.asList(appConfigurations));

		Map<String, String> actualResult = appRulesConfigService.getAllRulesAndConfigs();

		assertEquals(2, actualResult.size());
	}

}