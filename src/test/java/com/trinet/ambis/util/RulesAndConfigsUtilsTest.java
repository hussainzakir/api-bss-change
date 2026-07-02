package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class RulesAndConfigsUtilsTest {

	@Mock
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	private static final long REALM_YR_ID = 23L;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigService);

		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(prepareRulesAndConfigs());
	}

	@Test
	public void getMinFundingType() {
		String minFunding = RulesAndConfigsUtils.getMinFundingType(REALM_YR_ID);

		assertEquals("23.90", minFunding);
		
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.remove("FUNDING_TYPE");
		
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
		.thenReturn(rulesConfigMap);
		
		minFunding = RulesAndConfigsUtils.getMinFundingType(REALM_YR_ID);
		
		assertEquals("DEFAULT", minFunding);
	}

	@Test
	public void getSDIStates() {
		Set<String> sdiStates = RulesAndConfigsUtils.getSDIStates(REALM_YR_ID);

		assertEquals(4, sdiStates.size());
		assertTrue(sdiStates.contains("CA"));
		assertTrue(sdiStates.contains("NJ"));
		assertTrue(sdiStates.contains("NY"));
		assertTrue(sdiStates.contains("PR"));
	}
	
	@Test
	public void isVendorMappingOn() {
		boolean result = RulesAndConfigsUtils.isVendorMappingOn(REALM_YR_ID);

		assertEquals(true, result);
		
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.put("VENDOR_MAPPING", "false");
		
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
		.thenReturn(rulesConfigMap);
		
		result = RulesAndConfigsUtils.isVendorMappingOn(REALM_YR_ID);
		
		assertEquals(false, result);
	}

	@Test
	public void getBundleExchanges() {
		boolean result = RulesAndConfigsUtils.isDisabledBundledOn(REALM_YR_ID);

		assertEquals(true, result);
		
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.put("DISABILITY_BUNDLED", "false");
		
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
		.thenReturn(rulesConfigMap);
		
		result = RulesAndConfigsUtils.isDisabledBundledOn(REALM_YR_ID);
		
		assertEquals(false, result);
	}

	@Test
	public void isAutoRefreshCensusOn() {
		boolean result = RulesAndConfigsUtils.isAutoRefreshCensusOn( REALM_YR_ID );
		assertEquals(true, result);

		Map<String, String> falseTestCase = new HashMap<>();
		falseTestCase.put("AUTO_REFRESH_CENSUS", "false");
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong())).thenReturn( falseTestCase );

		result = RulesAndConfigsUtils.isAutoRefreshCensusOn( REALM_YR_ID );
		assertEquals(false, result);
	}

	@Test
	public void getRenewalRiskType_configuredValue() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.put("RENEWAL_RISK_TYPE", "DIFFERENTIALS");

		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);

		RiskTypeEnum riskType = RulesAndConfigsUtils.getRenewalRiskType(REALM_YR_ID);

		assertEquals(RiskTypeEnum.DIFFERENTIALS, riskType);
	}

	@Test
	public void getRenewalRiskType_defaultWhenMissing() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.remove("RENEWAL_RISK_TYPE");

		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);

		RiskTypeEnum riskType = RulesAndConfigsUtils.getRenewalRiskType(REALM_YR_ID);

		assertEquals(RiskTypeEnum.BANDS, riskType);
	}

	@Test
	public void isPlanMappingServiceEnabled_trueWhenConfiguredTrue() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.put("PLAN_MAPPING_SERVICE_ENABLED", "true");

		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);

		boolean result = RulesAndConfigsUtils.isPlanMappingServiceEnabled(REALM_YR_ID);

		assertEquals(true, result);
	}

	@Test
	public void isPlanMappingServiceEnabled_falseWhenConfiguredFalse() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.put("PLAN_MAPPING_SERVICE_ENABLED", "false");

		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);

		boolean result = RulesAndConfigsUtils.isPlanMappingServiceEnabled(REALM_YR_ID);

		assertEquals(false, result);
	}

	@Test
	public void isPlanMappingServiceEnabled_falseWhenMissing() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.remove("PLAN_MAPPING_SERVICE_ENABLED");

		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);

		boolean result = RulesAndConfigsUtils.isPlanMappingServiceEnabled(REALM_YR_ID);

		assertEquals(false, result);
	}

	@Test
	public void isDisplayPreferenceProspectEnabled_trueFalseStrings() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.put("DISPLAY_PREFERENCE_PROSPECT", "true");
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);
		assertTrue(RulesAndConfigsUtils.isDisplayPreferenceProspectEnabled(REALM_YR_ID));

		rulesConfigMap.put("DISPLAY_PREFERENCE_PROSPECT", "false");
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);
		assertFalse(RulesAndConfigsUtils.isDisplayPreferenceProspectEnabled(REALM_YR_ID));
	}

	@Test
	public void isDisplayPreferenceProspectEnabled_numericStrings() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.put("DISPLAY_PREFERENCE_PROSPECT", "1");
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);
		assertTrue(RulesAndConfigsUtils.isDisplayPreferenceProspectEnabled(REALM_YR_ID));

		rulesConfigMap.put("DISPLAY_PREFERENCE_PROSPECT", "0");
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);
		assertFalse(RulesAndConfigsUtils.isDisplayPreferenceProspectEnabled(REALM_YR_ID));
	}

	@Test
	public void isDisplayPreferenceProspectEnabled_missingValueDefaultsFalse() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.remove("DISPLAY_PREFERENCE_PROSPECT");
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);
		assertFalse(RulesAndConfigsUtils.isDisplayPreferenceProspectEnabled(REALM_YR_ID));
	}

	@Test
	public void isBenBundleEnabled_missingValueDefaultsFalse() {
		Map<String, String> rulesConfigMap = prepareRulesAndConfigs();
		rulesConfigMap.remove("BEN_BUNDLE_ENABLED");
		when(realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(anyLong()))
				.thenReturn(rulesConfigMap);
		assertFalse(RulesAndConfigsUtils.isGaBundleEnabled(REALM_YR_ID));
	}

	private Map<String, String> prepareRulesAndConfigs() {
		Map<String, String> data = new HashMap<>();
		data.put("FUNDING_TYPE", "23.90");
		data.put("SDI_STATES", "CA, NJ,NY ,PR");
		data.put("VENDOR_MAPPING", "true");
		data.put("DISABILITY_BUNDLED", "true");
		data.put("AUTO_REFRESH_CENSUS", "true");
		// no RENEWAL_RISK_TYPE here so tests can explicitly control presence/absence
		return data;
	}
}
