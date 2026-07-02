package com.trinet.ambis.util;

import java.util.HashMap;
import java.util.Map;

import com.trinet.ambis.common.BSSApplicationConstants;
import org.apache.commons.lang3.StringUtils;

import com.trinet.ambis.service.AppRulesConfigService;

import static com.trinet.ambis.common.BSSApplicationConstants.TNIV_NAICS_DISABILITY_BAND_CODE;
import static com.trinet.ambis.common.BSSApplicationConstants.TNIV_NAICS_LIFE_BAND_CODE;

public final class AppRulesAndConfigsUtils {
    private static final String DISABLE_CACHE = "DISABLE_CACHE";
    private static final String SUBMIT_QUE_ENABLED = "SUBMIT_QUE_ENABLED";
    private static final String PROSPECT_STRATEGY_QUEUE_ENABLED = "PROSPECT_STRATEGY_QUEUE_ENABLED";
    private static final String STRATEGY_NAME_MAX_LENGTH = "STRATEGY_NAME_MAX_LENGTH";
    private static final String GROUP_NAME_MAX_LENGTH = "GROUP_NAME_MAX_LENGTH";
    private static final String MAX_BUNDLE_SEQ = "MAX_BUNDLE_SEQ";
    private static final String ALLOWED_CHARACTERS = "ALLOWED_CHARACTERS";
    private static final int DEFAULT_MAX_LENGTH = 255;
    private static final int DEFAULT_MAX_BUNDLE_SEQ = 3;
    private static final String ENABLE_SNOW_EMAILS = "ENABLE_SNOW_EMAILS";
    private static final String EVENT_DRIVEN_SYNC = "EVENT_DRIVEN_SYNC";
    private static final String ON_DEMAND_SYNC = "ON_DEMAND_SYNC";
    private static final String IS_STRATEGY_THREAD_ENABLED = "IS_STRATEGY_THREAD_ENABLED";
    private static final String PROSPECT_STRATEGY_SYNC_PLYR_CHANGE_ENABLED = "PROSPECT_STRATEGY_SYNC_PLYR_CHANGE_ENABLED";
    private static final String IS_STRATEGY_CACHE_ENABLED = "IS_STRATEGY_CACHE_ENABLED";
    private static final String PROSPECT_CONVERSION_CUTOFFDATE = "PROSPECT_CONVERSION_CUTOFFDATE";
    private static final String PROSPECT_MA_DEFAULT_GROUP_CREATION = "PROSPECT_MA_DEFAULT_GROUP_CREATION";
    private static final String LIFE_DI_BAND_CALC_ENABLED = "LIFE_DI_BAND_CALC_ENABLED";
    private static final String LOWEST_COST_PLAN_V2_ENABLED="LOWEST_COST_PLAN_V2_ENABLED";
    private static final String BUNDLE_V2_ENABLED = "BUNDLE_V2_ENABLED";
    private static final String LIFE_DI_SELECTED_PLANS_PAGE_BREAK_ENABLED="LIFE_DI_SELECTED_PLANS_PAGE_BREAK_ENABLED";
    private static final String EMPLOYEE_COMPARE_PAGE_BREAK_ENABLED="EMPLOYEE_COMPARE_PAGE_BREAK_ENABLED";
    private static final String BROKER_NOTIFICATION_ENABLED="BROKER_NOTIFICATION_ENABLED";
    private static final String MDV_PAGE_BREAK_ENABLED="MDV_PAGE_BREAK_ENABLED";
    private static final String BSS_OUTPUT_PHASE2="BSS_OUTPUT_PHASE2";

    private static AppRulesConfigService appRulesConfigService;

    private AppRulesAndConfigsUtils() {
    }

    public static void setAppRuleConfigService(AppRulesConfigService service) {
        AppRulesAndConfigsUtils.appRulesConfigService = service;
    }

    public static boolean isSubmitQueuingEnabled() {
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
        String value = result.get(SUBMIT_QUE_ENABLED);
        return Boolean.parseBoolean(value);
    }

    public static boolean isProspectStrategyQueuingEnabled() {
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
        String value = result.get(PROSPECT_STRATEGY_QUEUE_ENABLED);
        return Boolean.parseBoolean(value);
    }


    public static boolean isProspectStrategySyncPlYrChangeEnabled() {
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
        String value = result.get(PROSPECT_STRATEGY_SYNC_PLYR_CHANGE_ENABLED);
        return Boolean.parseBoolean(value);
    }

    public static int getStrategyNameMaxLength() {
        int returnValue;
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
        if (result.get(STRATEGY_NAME_MAX_LENGTH) == null) {
            returnValue = DEFAULT_MAX_LENGTH;
        } else {
            returnValue = Integer.valueOf(result.get(STRATEGY_NAME_MAX_LENGTH));
        }
        return returnValue;
    }

    public static int getGroupNameMaxLength() {
        int returnValue;
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
        if (result.get(GROUP_NAME_MAX_LENGTH) == null) {
            returnValue = DEFAULT_MAX_LENGTH;
        } else {
            returnValue = Integer.valueOf(result.get(GROUP_NAME_MAX_LENGTH));
        }
        return returnValue;
    }

    public static int getMaxBundleSeq() {
        try {
            Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
            if (result == null) {
                return DEFAULT_MAX_BUNDLE_SEQ;
            }

            String value = result.get(MAX_BUNDLE_SEQ);
            if (StringUtils.isBlank(value)) {
                return DEFAULT_MAX_BUNDLE_SEQ;
            }

            int parsedValue = Integer.parseInt(value);
            return parsedValue > 0 ? parsedValue : DEFAULT_MAX_BUNDLE_SEQ;
        } catch (Exception ex) {
            return DEFAULT_MAX_BUNDLE_SEQ;
        }
    }

    public static String getAllowedCharacterRegExp() {
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
        return result.get(ALLOWED_CHARACTERS);
    }

    public static boolean isCacheDisabled() {
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();

        if (result.get(DISABLE_CACHE) == null) {
            return false;
        }
        String value = result.get(DISABLE_CACHE);

        return Boolean.parseBoolean(value);
    }

    /**
     * Reads ENABLE_SNOW_EMAILS key from XBSS_APP_RULES table
     *
     * @return false when value is 0<br>
     * true when values is 1 <br>
     * true if key is not present in the table
     */
    public static boolean isSnowEmailsEnabled() {
        String enableSnowEmailsFlag = appRulesConfigService.getAllRulesAndConfigs().get(ENABLE_SNOW_EMAILS);
        return StringUtils.isEmpty(enableSnowEmailsFlag) || Boolean.parseBoolean(enableSnowEmailsFlag);
    }

    /**
     * Reads EVENT_DRIVEN_SYNC key from XBSS_APP_RULES table
     *
     * @return false when value is 0 <br>
     * false if key is not present in the table <br>
     * true when values is 1 <br>
     */
    public static boolean isEventDrivenSyncEnabled() {
        String eventDrivenSyncFlag = appRulesConfigService.getAllRulesAndConfigs().get(EVENT_DRIVEN_SYNC);
        return StringUtils.isNotEmpty(eventDrivenSyncFlag) && Boolean.parseBoolean(eventDrivenSyncFlag);
    }

    /**
     * Reads ON_DEMAND_SYNC key from XBSS_APP_RULES table
     *
     * @return false when value is 0 <br>
     * false if key is not present in the table <br>
     * true when values is 1 <br>
     */
    public static boolean isOnDemandSyncEnabled() {
        String eventDrivenSyncFlag = appRulesConfigService.getAllRulesAndConfigs().get(ON_DEMAND_SYNC);
        return StringUtils.isNotEmpty(eventDrivenSyncFlag) && Boolean.parseBoolean(eventDrivenSyncFlag);
    }

    public static boolean isStrategyThreadEnabled() {
        String isStrategyThreadEnabled = appRulesConfigService.getAllRulesAndConfigs().get(IS_STRATEGY_THREAD_ENABLED);
        return StringUtils.isEmpty(isStrategyThreadEnabled) || Boolean.parseBoolean(isStrategyThreadEnabled);
    }

    public static boolean isStrategyCacheEnabled() {
        String isStrategyThreadEnabled = appRulesConfigService.getAllRulesAndConfigs().get(IS_STRATEGY_CACHE_ENABLED);
        return StringUtils.isEmpty(isStrategyThreadEnabled) || Boolean.parseBoolean(isStrategyThreadEnabled);
    }

    public static String getProspectConversionCutOffDate() {
        return appRulesConfigService.getAllRulesAndConfigs().get(PROSPECT_CONVERSION_CUTOFFDATE);
    }

    public static boolean isLowestCostPlanPerCarrierV2Enabled() {
        String lowestCostPlanPerCarrier=appRulesConfigService.getAllRulesAndConfigs()
                .get(LOWEST_COST_PLAN_V2_ENABLED);
        return StringUtils.isNotEmpty(lowestCostPlanPerCarrier) && Boolean.parseBoolean(lowestCostPlanPerCarrier);
    }

    public static Map<String, String> getTNIVLifeAndDisabilityBandCode() {
        Map<String, String> allRulesAndConfigs = appRulesConfigService.getAllRulesAndConfigs();

        Map<String, String> bandCodeMap = new HashMap<>();

        bandCodeMap.put(BSSApplicationConstants.LIFE, allRulesAndConfigs.get(TNIV_NAICS_LIFE_BAND_CODE));
        bandCodeMap.put(BSSApplicationConstants.DISABILITY, allRulesAndConfigs.get(TNIV_NAICS_DISABILITY_BAND_CODE));

        return bandCodeMap;

    }

    public static boolean isProspectDefaultMAGroupCreationEnabled() {
        String enableDefaultMAGroupCreation = appRulesConfigService.getAllRulesAndConfigs()
                .get(PROSPECT_MA_DEFAULT_GROUP_CREATION);
        return StringUtils.isNotEmpty(enableDefaultMAGroupCreation)
                && Boolean.parseBoolean(enableDefaultMAGroupCreation);
    }

    public static boolean isLifeAndDiBandCalcEnabled() {
        String isLifeDiBandCalcEnabled = appRulesConfigService.getAllRulesAndConfigs()
                .get(LIFE_DI_BAND_CALC_ENABLED);
        return StringUtils.isNotEmpty(isLifeDiBandCalcEnabled)
                && Boolean.parseBoolean(isLifeDiBandCalcEnabled);
    }

    public static boolean isLifeAndDiPageBreakEnabled() {
        String isLifeAndDiPageBreakEnabled = appRulesConfigService.getAllRulesAndConfigs()
                .get(LIFE_DI_SELECTED_PLANS_PAGE_BREAK_ENABLED);
        return StringUtils.isNotEmpty(isLifeAndDiPageBreakEnabled)
                && Boolean.parseBoolean(isLifeAndDiPageBreakEnabled);
    }

    public static boolean isEmployeeComparePageBreakEnabled() {
        String isEmployeeComparePageBreakEnabled = appRulesConfigService.getAllRulesAndConfigs()
                .get(EMPLOYEE_COMPARE_PAGE_BREAK_ENABLED);
        return StringUtils.isNotEmpty(isEmployeeComparePageBreakEnabled)
                && Boolean.parseBoolean(isEmployeeComparePageBreakEnabled);
    }

    public static boolean isMdvPageBreakEnabled(){
       Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
       String value=result.get(MDV_PAGE_BREAK_ENABLED);
       return Boolean.parseBoolean(value);
    }

    public static boolean isBssOutputPhase2Enabled(){
       Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
       String value = result.get(BSS_OUTPUT_PHASE2);
       return Boolean.parseBoolean(value);
    }

    public static boolean isBrokerNotificationEnabled() {
        Map<String, String> result = appRulesConfigService.getAllRulesAndConfigs();
        String isBrokerNotificationEnabled = result.get(BROKER_NOTIFICATION_ENABLED);
        return StringUtils.isNotEmpty(isBrokerNotificationEnabled)
                && Boolean.parseBoolean(isBrokerNotificationEnabled);
    }

    public static boolean isBundleV2Enabled() {
        String isBundleV2Enabled = appRulesConfigService.getAllRulesAndConfigs()
                .get(BUNDLE_V2_ENABLED);
        return StringUtils.isNotEmpty(isBundleV2Enabled)
                && Boolean.parseBoolean(isBundleV2Enabled);
    }

}
