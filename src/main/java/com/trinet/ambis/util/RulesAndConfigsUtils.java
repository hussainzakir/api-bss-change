package com.trinet.ambis.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.trinet.ambis.enums.RiskTypeEnum;
import org.apache.commons.lang.StringUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;

public class RulesAndConfigsUtils {
	private static final String FUNDING_TYPE_KEY = "FUNDING_TYPE";
	private static final String SDI_STATES_KEY = "SDI_STATES";
	private static final String VENDOR_MAPPING_KEY = "VENDOR_MAPPING";
	private static final String DISABILITY_BUNDLED_KEY = "DISABILITY_BUNDLED";
	private static final String AUTO_REFRESH_CENSUS_KEY = "AUTO_REFRESH_CENSUS";
	private static final String PLAN_RATE_MAPPING = "PLAN_RATE_MAPPING";
    private static final String RENEWAL_RISK_TYPE_KEY = "RENEWAL_RISK_TYPE";
	private static final String DISPLAY_PREFERENCE_PROSPECT_KEY = "DISPLAY_PREFERENCE_PROSPECT";
	private static final String BEN_BUNDLE_ENABLED_KEY = "BEN_BUNDLE_ENABLED";
	private static final String PLAN_MAPPING_SERVICE_KEY = "PLAN_MAPPING_SERVICE_ENABLED";
	private static final String GA_BUNDLE_ENABLED_KEY = "GA_BUNDLE_ENABLED_KEY";

	private static RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	private RulesAndConfigsUtils() {
	}

	public static void setRealmPlanYearRuleConfigService(RealmPlanYearRuleConfigService service) {
		RulesAndConfigsUtils.realmPlanYearRuleConfigService = service;
	}

	public static String getMinFundingType(long realmYrId) {
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		return result.get(FUNDING_TYPE_KEY) != null ? result.get(FUNDING_TYPE_KEY)
				: BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE;
	}

	public static boolean isVendorMappingOn(long realmYrId) {
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		return (result.get(VENDOR_MAPPING_KEY) != null && "true".equals(result.get(VENDOR_MAPPING_KEY))) ? Boolean.TRUE : Boolean.FALSE;
	}

	public static boolean isAutoRefreshCensusOn(long realmYrId) {
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		return ( result.get(AUTO_REFRESH_CENSUS_KEY) != null && "true".equals(result.get(AUTO_REFRESH_CENSUS_KEY)) );
	}

	public static Set<String> getSDIStates(long realmYrId) {
		Set<String> states = new HashSet<>();
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		String statesStr = result.get(SDI_STATES_KEY) != null ? result.get(SDI_STATES_KEY) : StringUtils.EMPTY;
		StringTokenizer st = new StringTokenizer(statesStr, ",");
		while (st.hasMoreTokens()) {
			states.add(st.nextToken().trim());
		}
		return states;
	}
	
	public static boolean isDisabledBundledOn(long realmYrId) {
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		return (result.get(DISABILITY_BUNDLED_KEY) != null && "true".equals(result.get(DISABILITY_BUNDLED_KEY))) ? Boolean.TRUE : Boolean.FALSE;
	}

	public static boolean findPickChooseWithExceptions( Company company ) {
		return realmPlanYearRuleConfigService.findPickChooseWithExceptions( company.getRealmPlanYear().getId(), company.getCode(), company.getRealmPlanYear().getPlanYearStart() );
	}

	public static boolean isPlanRateMappingEnabled(long realmYrId) {
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		String value = result.get(PLAN_RATE_MAPPING);
		return Boolean.parseBoolean(value);
	}

    public static RiskTypeEnum getRenewalRiskType(long realmYrId) {
        Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
        String value = result.get(RENEWAL_RISK_TYPE_KEY);
        return value != null ? RiskTypeEnum.valueOf(value) : RiskTypeEnum.valueOf(BSSApplicationConstants.DEFAULT_RISK_TYPE);
    }

	public static boolean isDisplayPreferenceProspectEnabled(long realmYrId) {
		if (realmPlanYearRuleConfigService == null) {
			return false;
		}
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		if (result == null) {
			return false;
		}
		String value = result.get(DISPLAY_PREFERENCE_PROSPECT_KEY);
		return "true".equalsIgnoreCase(value) || "1".equals(value);
	}

	public static boolean isPlanMappingServiceEnabled(long realmYrId) {
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		String value = result.get(PLAN_MAPPING_SERVICE_KEY);
		return (value != null && "true".equals(value)) ? Boolean.TRUE : Boolean.FALSE;
	}

	public static boolean isGaBundleEnabled(long realmYrId) {
		Map<String, String> result = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(realmYrId);
		String value = result.get(GA_BUNDLE_ENABLED_KEY);
		return (value != null && "true".equalsIgnoreCase(value)) ? Boolean.TRUE : Boolean.FALSE;
	}

}
