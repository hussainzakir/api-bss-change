package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.RealmPlanYearConfiguration;
import com.trinet.ambis.persistence.model.RealmPlanYearConfigurationId;
import com.trinet.ambis.persistence.model.RealmPlanYearRule;
import com.trinet.ambis.persistence.model.RealmPlanYearRuleId;
import com.trinet.ambis.service.PsConfigurationService;
import com.trinet.ambis.service.RealmPlanYearConfigurationService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.impl.RealmPlanYearRuleConfigServiceImpl;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class RealmPlanYearRuleConfigServiceImplTest {

	@InjectMocks
	RealmPlanYearRuleConfigServiceImpl serviceImpl;

	@Mock
	RealmPlanYearRuleService ruleService;

	@Mock
	RealmPlanYearConfigurationService configService;

	@Mock
	PsConfigurationService psConfigurationService;

	private static final String OE_QUARTER = "Q1";
	private static final long REALM_PLYR_ID = 11;
	private static final Date EFF_DT = new java.sql.Date( new Date().getTime() );
	private static final String K1_COMPANY = "K1_COMPANY";
	private static final String AVG_SALARY = "AVG_SALARY";
	private static final String PICK_CHOOSE = "PICK_CHOOSE_FLAG";
	private static final String AVG_SALARY_AMOUNT_STRING = "150000";
	private static final String HSA_ENABLED = "HSA_ENABLED";
	private static final String WAIVER_ALLOWANCE = "WAIVER_ALLOWANCE";

    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setup() {
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getRulesAndConfigsByRealmPlanYearIdTest1() {
		List<RealmPlanYearRule> rules = Collections.emptyList();
		List<RealmPlanYearConfiguration> configs = Collections.emptyList();

		when(ruleService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(rules);
		when(configService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(configs);

		Map<String, String> actualResult = serviceImpl.getRulesAndConfigsByRealmPlanYearId(REALM_PLYR_ID);

		assertEquals(Collections.emptyMap(), actualResult);
	}

	@Test
	public void getRulesAndConfigsByRealmPlanYearIdTest2() {
		List<RealmPlanYearRule> rules = new ArrayList<>();
		RealmPlanYearRule rule1 = prepareRealmPlanYearRule(K1_COMPANY, true);
		RealmPlanYearRule rule2 = prepareRealmPlanYearRule("MODEL_COMPARE_FLAG", false);
		rules.add(rule1);
		rules.add(rule2);
		List<RealmPlanYearConfiguration> configs = Collections.emptyList();

		when(ruleService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(rules);
		when(configService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(configs);

		Map<String, String> actualResult = serviceImpl.getRulesAndConfigsByRealmPlanYearId(REALM_PLYR_ID);

		assertEquals(2, actualResult.size());
		assertEquals("true", actualResult.get(K1_COMPANY));
		assertEquals("false", actualResult.get("MODEL_COMPARE_FLAG"));
	}

	@Test
	public void getRulesAndConfigsByRealmPlanYearIdTest3() {
		List<RealmPlanYearRule> rules = Collections.emptyList();
		List<RealmPlanYearConfiguration> configs = new ArrayList<>();
		RealmPlanYearConfiguration config1 = prepareRealmPlanYearConfiguration(AVG_SALARY, AVG_SALARY_AMOUNT_STRING);
		RealmPlanYearConfiguration config2 = prepareRealmPlanYearConfiguration("MIN_FUNDING", "75");
		RealmPlanYearConfiguration config3 = prepareRealmPlanYearConfiguration("MICROSITE_URL", null);
		configs.add(config1);
		configs.add(config2);
		configs.add(config3);

		when(ruleService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(rules);
		when(configService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(configs);

		Map<String, String> actualResult = serviceImpl.getRulesAndConfigsByRealmPlanYearId(REALM_PLYR_ID);

		assertEquals(3, actualResult.size());
		assertEquals(AVG_SALARY_AMOUNT_STRING, actualResult.get(AVG_SALARY));
		assertEquals("75", actualResult.get("MIN_FUNDING"));
		assertEquals("", actualResult.get("MICROSITE_URL"));
	}

	@Test
	public void getRulesAndConfigsByRealmPlanYearIdTest4() {
		List<RealmPlanYearRule> rules = new ArrayList<>();
		RealmPlanYearRule rule1 = prepareRealmPlanYearRule(K1_COMPANY, true);
		rules.add(rule1);
		List<RealmPlanYearConfiguration> configs = new ArrayList<>();
		RealmPlanYearConfiguration config1 = prepareRealmPlanYearConfiguration(AVG_SALARY, AVG_SALARY_AMOUNT_STRING);
		configs.add(config1);

		when(ruleService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(rules);
		when(configService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(configs);

		Map<String, String> actualResult = serviceImpl.getRulesAndConfigsByRealmPlanYearId(REALM_PLYR_ID);

		assertEquals(2, actualResult.size());
		assertEquals("true", actualResult.get(K1_COMPANY));
		assertEquals(AVG_SALARY_AMOUNT_STRING, actualResult.get(AVG_SALARY));
	}

	@Test(expected = IllegalStateException.class)
	public void getRulesAndConfigsByRealmPlanYearIdTest5() {
		List<RealmPlanYearRule> rules = new ArrayList<>();
		RealmPlanYearRule rule1 = prepareRealmPlanYearRule(K1_COMPANY, true);
		rules.add(rule1);
		List<RealmPlanYearConfiguration> configs = new ArrayList<>();
		RealmPlanYearConfiguration config1 = prepareRealmPlanYearConfiguration(K1_COMPANY, AVG_SALARY_AMOUNT_STRING);
		configs.add(config1);

		when(ruleService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(rules);
		when(configService.findByRealmPlanYearId(REALM_PLYR_ID)).thenReturn(configs);

		serviceImpl.getRulesAndConfigsByRealmPlanYearId(REALM_PLYR_ID);
	}

	@Test
	public void getRulesAndConfigsByRealmPlanYearIdTest6() {
		Company company = new Company();
		company.setCode( "3MP" );
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 100L );
		rpy.setPlanYearStart( new Date() );
		company.setRealmPlanYear( rpy );

		List<RealmPlanYearRule> rules = new ArrayList<>();
		rules.add( prepareRealmPlanYearRule( PICK_CHOOSE, false ) );
		when(ruleService.findByRealmPlanYearId( company.getRealmPlanYear().getId() )).thenReturn(rules);

		List<RealmPlanYearConfiguration> configs = new ArrayList<>();
		configs.add( prepareRealmPlanYearConfiguration( K1_COMPANY, AVG_SALARY_AMOUNT_STRING ) );
		when(configService.findByRealmPlanYearId( company.getRealmPlanYear().getId() )).thenReturn(configs);

		when( RulesAndConfigsUtils.findPickChooseWithExceptions( company ) ).thenReturn( true );

		Map<String,String> result = serviceImpl.getRulesAndConfigsByRealmPlanYearId( company );
		assertEquals( 2, result.size() );
		assertEquals( "true", result.get( PICK_CHOOSE ) );
	}

	@Test
	public void getRulesAndConfigsByRealmPlanYearIdProspectTest() {
		Company company = new Company();
		company.setCode( "PROSPECTCLIENT" );
		company.setProspectCompany( true );
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId( 100L );
		rpy.setPlanYearStart( new Date() );
		company.setRealmPlanYear( rpy );

		List<RealmPlanYearRule> rules = new ArrayList<>();
		rules.add( prepareRealmPlanYearRule( PICK_CHOOSE, false ) );
		rules.add( prepareRealmPlanYearRule( HSA_ENABLED, true ) );
		rules.add( prepareRealmPlanYearRule( WAIVER_ALLOWANCE, true ) );
		when(ruleService.findByRealmPlanYearId( company.getRealmPlanYear().getId() )).thenReturn(rules);

		List<RealmPlanYearConfiguration> configs = new ArrayList<>();
		configs.add( prepareRealmPlanYearConfiguration( K1_COMPANY, AVG_SALARY_AMOUNT_STRING ) );
		when(configService.findByRealmPlanYearId( company.getRealmPlanYear().getId() )).thenReturn(configs);

		when( RulesAndConfigsUtils.findPickChooseWithExceptions( company ) ).thenReturn( true );

		Map<String,String> result = serviceImpl.getRulesAndConfigsByRealmPlanYearId( company );
		assertEquals( 4, result.size() );
		assertEquals( "true", result.get( PICK_CHOOSE ) );
		assertEquals( "false", result.get( HSA_ENABLED ) );
		assertEquals( "false", result.get( WAIVER_ALLOWANCE ) );
	}

	@Test
	public void getPsConfigsByDateTest1() {
		Map<String, String> psConfigs = new HashMap<>();

		when(psConfigurationService.findByEffDate(EFF_DT)).thenReturn(psConfigs);

		Map<String, String> actualResult = serviceImpl.getPsConfigsByDate(EFF_DT);

		assertEquals(Collections.emptyMap(), actualResult);

	}

	@Test
	public void getPsConfigsByDateTest2() {
		Map<String, String> psConfigs = new HashMap<>();
		psConfigs.put("HSA_ANNUAL_EMPLOYEE_MAXIMUM", "3000");
		psConfigs.put("HSA_ANNUAL_FAMILY_MAXIMUM", "6000");

		when(psConfigurationService.findByEffDate(EFF_DT)).thenReturn(psConfigs);

		Map<String, String> actualResult = serviceImpl.getPsConfigsByDate(EFF_DT);

		assertEquals(2, actualResult.size());
		assertEquals(psConfigs, actualResult);
	}

	private RealmPlanYearConfiguration prepareRealmPlanYearConfiguration(String key, String value) {
		RealmPlanYearConfiguration config = new RealmPlanYearConfiguration();
		RealmPlanYearConfigurationId id = new RealmPlanYearConfigurationId();
		id.setOeQuarter( OE_QUARTER );
		id.setConfigKey(key);
		id.setEffdt( (java.sql.Date) EFF_DT );
		config.setId(id);
		config.setConfigValue(value);
		config.setConfigDesc("test");
		config.setEnddt( java.sql.Date.valueOf( "2099-12-31" ) );
		return config;
	}

	private RealmPlanYearRule prepareRealmPlanYearRule(String key, boolean value) {
		RealmPlanYearRule rule = new RealmPlanYearRule();
		RealmPlanYearRuleId id = new RealmPlanYearRuleId( OE_QUARTER, (java.sql.Date) EFF_DT, key );
		rule.setId(id);
		rule.setRuleDesc("This rule description");
		rule.setRuleValue(value);
		rule.setEnddt( java.sql.Date.valueOf( "2099-12-31" ) );
		return rule;
	}
}