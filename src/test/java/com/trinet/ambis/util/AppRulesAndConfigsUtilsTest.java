package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.trinet.ambis.common.BSSApplicationConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.AppRulesConfigService;

@RunWith(MockitoJUnitRunner.class)
public class AppRulesAndConfigsUtilsTest {

    @Mock
    AppRulesConfigService service;

    @Before
    public void setUp() {
        AppRulesAndConfigsUtils.setAppRuleConfigService(service);
    }

    @Test
    public void isSubmitQueuingEnabled() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareSubmitQueRulesAndConfigs("true"));

        boolean actualResult = AppRulesAndConfigsUtils.isSubmitQueuingEnabled();

        assertTrue(actualResult);
    }

    @Test
    public void isSubmitQueuingEnabled_test1() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareSubmitQueRulesAndConfigs("false"));

        boolean actualResult = AppRulesAndConfigsUtils.isSubmitQueuingEnabled();

        assertFalse(actualResult);
    }

    @Test
    public void isSubmitQueuingEnabled_test2() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());

        boolean actualResult = AppRulesAndConfigsUtils.isSubmitQueuingEnabled();

        assertFalse(actualResult);
    }

    @Test
    public void getStrategyNameMaxLength() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareMaxLengthRulesAndConfigs("50"));

        int actualResult = AppRulesAndConfigsUtils.getStrategyNameMaxLength();

        assertEquals(50, actualResult);
    }

    @Test
    public void getStrategyNameMaxLengthEmpty() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());

        int actualResult = AppRulesAndConfigsUtils.getStrategyNameMaxLength();

        assertEquals(255, actualResult);
    }

    @Test
    public void getGroupNameMaxLength() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareMaxLengthRulesAndConfigs("50"));

        int actualResult = AppRulesAndConfigsUtils.getGroupNameMaxLength();

        assertEquals(50, actualResult);
    }

    @Test
    public void getGroupNameMaxLengthEmpty() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());

        int actualResult = AppRulesAndConfigsUtils.getGroupNameMaxLength();

        assertEquals(255, actualResult);
    }

    @Test
    public void getMaxBundleSeq() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareMaxBundleSeqRulesAndConfigs("5"));

        int actualResult = AppRulesAndConfigsUtils.getMaxBundleSeq();

        assertEquals(5, actualResult);
    }

    @Test
    public void getMaxBundleSeqEmpty() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareMaxBundleSeqRulesAndConfigs(""));

        int actualResult = AppRulesAndConfigsUtils.getMaxBundleSeq();

        assertEquals(3, actualResult);
    }

    @Test
    public void getMaxBundleSeqNull() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());

        int actualResult = AppRulesAndConfigsUtils.getMaxBundleSeq();

        assertEquals(3, actualResult);
    }

    @Test
    public void getMaxBundleSeqInvalidValue() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareMaxBundleSeqRulesAndConfigs("invalid"));

        int actualResult = AppRulesAndConfigsUtils.getMaxBundleSeq();

        assertEquals(3, actualResult);
    }

    @Test
    public void getMaxBundleSeqNonPositiveValue() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareMaxBundleSeqRulesAndConfigs("0"));

        int actualResult = AppRulesAndConfigsUtils.getMaxBundleSeq();

        assertEquals(3, actualResult);
    }

    @Test
    public void getMaxBundleSeqWhenServiceThrowsException() {
        when(service.getAllRulesAndConfigs()).thenThrow(new RuntimeException("Unable to load configs"));

        int actualResult = AppRulesAndConfigsUtils.getMaxBundleSeq();

        assertEquals(3, actualResult);
    }

    @Test
    public void getAllowedCharacterRegExp() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareAllowedCharacterRulesAndConfigs("REGEX_VALUE"));

        String actualResult = AppRulesAndConfigsUtils.getAllowedCharacterRegExp();

        assertEquals("REGEX_VALUE", actualResult);
    }

    @Test
    public void getAllowedCharacterRegExpEmpty() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());

        String actualResult = AppRulesAndConfigsUtils.getAllowedCharacterRegExp();

        assertEquals(null, actualResult);
    }

    @Test
    public void isCacheDisabled() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareCacheDisabledRulesAndConfigs("true"));

        boolean actualResult = AppRulesAndConfigsUtils.isCacheDisabled();

        assertTrue(actualResult);
    }

    @Test
    public void isCacheDisabled_test1() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareCacheDisabledRulesAndConfigs("false"));

        boolean actualResult = AppRulesAndConfigsUtils.isCacheDisabled();

        assertFalse(actualResult);
    }

    @Test
    public void isCacheDisabled_test2() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());

        boolean actualResult = AppRulesAndConfigsUtils.isCacheDisabled();

        assertFalse(actualResult);
    }

    @Test
    public void isSnowEmailsEnabledDefaultTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isSnowEmailsEnabled();
        // then
        assertTrue(actual);
    }

    @Test
    public void isSnowEmailsEnabledTrueTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        rules.put("ENABLE_SNOW_EMAILS", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isSnowEmailsEnabled();
        // then
        assertTrue(actual);
    }

    @Test
    public void isSnowEmailsEnabledFalseTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        rules.put("ENABLE_SNOW_EMAILS", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isSnowEmailsEnabled();
        // then
        assertFalse(actual);
    }

    @Test
    public void isEventDrivenSyncEnabledDefaultTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isEventDrivenSyncEnabled();
        // then
        assertFalse(actual);
    }

    @Test
    public void isEventDrivenSyncEnabledTrueTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        rules.put("EVENT_DRIVEN_SYNC", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isEventDrivenSyncEnabled();
        // then
        assertTrue(actual);
    }

    @Test
    public void isEventDrivenSyncEnabledFalseTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        rules.put("EVENT_DRIVEN_SYNC", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isEventDrivenSyncEnabled();
        // then
        assertFalse(actual);
    }

    @Test
    public void isStrategySyncEnabled() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareStrategySyncRulesAndConfigs("true"));
        boolean actualResult = AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled();
        assertTrue(actualResult);
    }

    @Test
    public void isStrategySyncEnabled_test1() {
        when(service.getAllRulesAndConfigs()).thenReturn(prepareStrategySyncRulesAndConfigs("false"));
        boolean actualResult = AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled();
        assertFalse(actualResult);
    }

    @Test
    public void isStrategySyncEnabled_test2() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());
        boolean actualResult = AppRulesAndConfigsUtils.isProspectStrategySyncPlYrChangeEnabled();
        assertFalse(actualResult);
    }

    @Test
    public void isStrategyThreadEnabled_test1() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());
        boolean actualResult = AppRulesAndConfigsUtils.isStrategyThreadEnabled();
        assertTrue(actualResult);
    }

    @Test
    public void isStrategyThreadEnabled_test2() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("IS_STRATEGY_THREAD_ENABLED", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);
        boolean actualResult = AppRulesAndConfigsUtils.isStrategyThreadEnabled();
        assertTrue(actualResult);
    }

    @Test
    public void isStrategyThreadEnabled_test3() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("IS_STRATEGY_THREAD_ENABLED", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);
        boolean actualResult = AppRulesAndConfigsUtils.isStrategyThreadEnabled();
        assertFalse(actualResult);
    }

    @Test
    public void isStrategyCacheEnabled_test1() {
        when(service.getAllRulesAndConfigs()).thenReturn(Collections.emptyMap());
        boolean actualResult = AppRulesAndConfigsUtils.isStrategyCacheEnabled();
        assertTrue(actualResult);
    }

    @Test
    public void isStrategyCacheEnabled_test2() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("IS_STRATEGY_CACHE_ENABLED", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);
        boolean actualResult = AppRulesAndConfigsUtils.isStrategyCacheEnabled();
        assertTrue(actualResult);
    }

    @Test
    public void isStrategyCacheEnabled_test3() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("IS_STRATEGY_CACHE_ENABLED", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);
        boolean actualResult = AppRulesAndConfigsUtils.isStrategyCacheEnabled();
        assertFalse(actualResult);
    }

    @Test
    public void getProspectConversionCutOfffDate() {
        when(service.getAllRulesAndConfigs()).thenReturn(ProspectConversionCutOfffDate("1-JAN-2018"));
        String actualResult = AppRulesAndConfigsUtils.getProspectConversionCutOffDate();
        assertEquals("1-JAN-2018", actualResult);
    }

    @Test
    public void getNaicsLifeBandCode() {
        when(service.getAllRulesAndConfigs()).thenReturn(getTNIVNaicsLifeBandCode());
        Map<String, String> actualResult = AppRulesAndConfigsUtils.getTNIVLifeAndDisabilityBandCode();
        assertEquals("4", actualResult.get(BSSApplicationConstants.LIFE));
        assertEquals("1", actualResult.get(BSSApplicationConstants.DISABILITY));
    }

    private Map<String, String> prepareSubmitQueRulesAndConfigs(String submitQue) {
        Map<String, String> data = new HashMap<>();
        data.put("SUBMIT_QUE_ENABLED", submitQue);
        return data;
    }

    private Map<String, String> prepareMaxLengthRulesAndConfigs(String maxLength) {
        Map<String, String> data = new HashMap<>();
        data.put("STRATEGY_NAME_MAX_LENGTH", maxLength);
        data.put("GROUP_NAME_MAX_LENGTH", maxLength);
        return data;
    }

    private Map<String, String> prepareMaxBundleSeqRulesAndConfigs(String maxBundleSeq) {
        Map<String, String> data = new HashMap<>();
        data.put("MAX_BUNDLE_SEQ", maxBundleSeq);
        return data;
    }

    private Map<String, String> prepareAllowedCharacterRulesAndConfigs(String allowedCharacters) {
        Map<String, String> data = new HashMap<>();
        data.put("ALLOWED_CHARACTERS", allowedCharacters);
        return data;
    }

    private Map<String, String> prepareCacheDisabledRulesAndConfigs(String cacheDisabled) {
        Map<String, String> data = new HashMap<>();
        data.put("DISABLE_CACHE", cacheDisabled);
        return data;
    }

    private Map<String, String> prepareStrategySyncRulesAndConfigs(String plYrChange) {
        Map<String, String> data = new HashMap<>();
        data.put("PROSPECT_STRATEGY_SYNC_PLYR_CHANGE_ENABLED", plYrChange);
        return data;
    }

    private Map<String, String> ProspectConversionCutOfffDate(String cutOffDate) {
        Map<String, String> data = new HashMap<>();
        data.put("PROSPECT_CONVERSION_CUTOFFDATE", cutOffDate);
        return data;
    }

    @Test
    public void isLowestCostPlanPerCarrierV2EnabledTrueTest(){
        //given
        Map<String, String > rules = new HashMap<>();
        rules.put("LOWEST_COST_PLAN_V2_ENABLED","true");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        //when
        boolean actual = AppRulesAndConfigsUtils.isLowestCostPlanPerCarrierV2Enabled();
        //then
        assertTrue(actual);
    }

    @Test
    public void isLowestCostPlanPerCarrierV2EnabledFalseTest(){
        //given
        Map<String, String> rules=new HashMap<>();
        rules.put("LOWEST_COST_PLAN_V2_ENABLED","false");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        //when
        boolean actual=AppRulesAndConfigsUtils.isLowestCostPlanPerCarrierV2Enabled();
        //then
        assertFalse(actual);
    }

    private Map<String, String> getTNIVNaicsLifeBandCode() {
        Map<String, String> data = new HashMap<>();
        data.put("TNIV_NAICS_LIFE_BAND_CODE", "4");
        data.put("TNIV_NAICS_DISABILITY_BAND_CODE", "1");
        return data;
    }

    @Test
    public void isLifeAndDiBandCalcEnabledTrue() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("LIFE_DI_BAND_CALC_ENABLED", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isLifeAndDiBandCalcEnabled();

        assertTrue(actualResult);
    }

    @Test
    public void isLifeAndDiBandCalcEnabledFalse() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("LIFE_DI_BAND_CALC_ENABLED", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isLifeAndDiBandCalcEnabled();

        assertFalse(actualResult);
    }

    @Test
    public void isLifeAndDiPageBreakEnabledTrue() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("LIFE_DI_SELECTED_PLANS_PAGE_BREAK_ENABLED", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isLifeAndDiPageBreakEnabled();

        assertTrue(actualResult);
    }

    @Test
    public void isLifeAndDiPageBreakEnabledFalse() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("LIFE_DI_SELECTED_PLANS_PAGE_BREAK_ENABLED", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isLifeAndDiPageBreakEnabled();

        assertFalse(actualResult);
    }

    @Test
    public void isEmployeeComparePageBreakEnabledTrue() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("EMPLOYEE_COMPARE_PAGE_BREAK_ENABLED", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isEmployeeComparePageBreakEnabled();

        assertTrue(actualResult);
    }

    @Test
    public void isEmployeeComparePageBreakEnabledFalse() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("EMPLOYEE_COMPARE_PAGE_BREAK_ENABLED", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isEmployeeComparePageBreakEnabled();

        assertFalse(actualResult);
    }

    @Test
    public void isBssOutputPhase2EnabledTrueTest() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("BSS_OUTPUT_PHASE2", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isBssOutputPhase2Enabled();

        assertTrue(actualResult);
    }

    @Test
    public void isBssOutputPhase2EnabledFalseTest() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("BSS_OUTPUT_PHASE2", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isBssOutputPhase2Enabled();

        assertFalse(actualResult);
    }

    @Test
    public void isMdvPageBreakEnabledTrueTest() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("MDV_PAGE_BREAK_ENABLED", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isMdvPageBreakEnabled();

        assertTrue(actualResult);
    }

    @Test
    public void isMdvPageBreakEnabledFalseTest() {
        Map<String, String> appRulesAndConfig = new HashMap<>();
        appRulesAndConfig.put("MDV_PAGE_BREAK_ENABLED", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(appRulesAndConfig);

        boolean actualResult = AppRulesAndConfigsUtils.isMdvPageBreakEnabled();

        assertFalse(actualResult);
    }

    @Test
    public void isBundleV2EnabledDefaultTest() {
        // given - key not present (disabled by default)
        when(service.getAllRulesAndConfigs()).thenReturn(new HashMap<>());
        // when
        boolean actual = AppRulesAndConfigsUtils.isBundleV2Enabled();
        // then
        assertFalse(actual);
    }

    @Test
    public void isBundleV2EnabledTrueTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        rules.put("BUNDLE_V2_ENABLED", "true");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isBundleV2Enabled();
        // then
        assertTrue(actual);
    }

    @Test
    public void isBundleV2EnabledFalseTest() {
        // given
        Map<String, String> rules = new HashMap<>();
        rules.put("BUNDLE_V2_ENABLED", "false");
        when(service.getAllRulesAndConfigs()).thenReturn(rules);
        // when
        boolean actual = AppRulesAndConfigsUtils.isBundleV2Enabled();
        // then
        assertFalse(actual);
    }

}
