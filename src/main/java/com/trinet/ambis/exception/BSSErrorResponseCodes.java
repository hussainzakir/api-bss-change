package com.trinet.ambis.exception;

/**
 * @author rvutukuri
 *
 */
public class BSSErrorResponseCodes {

	/**
	 * Allow no instances of this class
	 */
	private BSSErrorResponseCodes() {}
	
	public static final String BSS_UNHANDLED_EXCEPTION = "ERR-BSS-00000";
	public static final String BSS_COMPANY_NOT_FOUND = "ERR-BSS-10000";
	public static final String BSS_PS_COMPANY_NOT_FOUND = "ERR-BSS-10001";
	public static final String BSS_COMPANY_NOT_VAL_NAICS = "ERR-BSS-10002";
	public static final String BSS_COMPANY_SAVE_FAIL = "ERR-BSS-10003";
	public static final String BSS_COMPANY_NOT_VAL_PEO = "ERR-BSS-10004";
	public static final String BSS_NO_CLONE_COMPANY = "ERR-BSS-10005";
	public static final String BSS_NO_DISABILITY_BAND_CODES = "ERR-BSS-10006";
	public static final String BSS_MISSING_DIFFERENTIALS_BAND_CODES = "ERR-BSS-10007";
	public static final String BSS_STRATEGY_SAVE_FAILED = "ERR-BSS-10019";
	public static final String BSS_GROUP_SAVE_FAILED = "ERR-BSS-10020";
	public static final String BSS_INVALID_EMPLOYEE_ID = "ERR-BSS-BAD-EMPLID";
	public static final String BSS_INVALID_BENEFIT_PLAN = "ERR-BSS-BAD-PLAN";
	public static final String BSS_CENSUS_REFRESH_ERROR = "ERR-BSS-REFRESH";
	public static final String BSS_PROSPECT_TO_CLIENT_CONVERSION_ERROR = "ERR-BSS-10030";

	// MODEL-COMPARE-CODES
	public static final String BSS_MC_GENERIC = "ERR-BSS-30000";
	public static final String BSS_MC_STRATEGIES_NOT_FOUND = "ERR-BSS-30002";
	public static final String BSS_MC_STRATEGY_FUNDING_DETAILS_NOT_FOUND = "ERR-BSS-30003";
	public static final String BSS_STRATEGY_COST_NOT_FOUND = "ERR-BSS-30005";
	public static final String BSS_MC_STRATEGY_GROUP_NOT_FOUND = "ERR-BSS-30006";

	// Strategy
	public static final String BSS_STRATEGY_PRE_LOAD_FAIL = "ERR-BSS-40001";
	public static final String BSS_STRATEGY_CREATE_FAIL = "ERR-BSS-40002";
	public static final String BSS_LIFE_DISABILITY_SYNC = "ERR-BSS-LIFE-DISABILITY-SYNC"; 

	// Benefit group
	public static final String BSS_GROUP_NAME_CHANGE_FAIL = "ERR-BSS-GROUP-NAME-CHANGE";
	public static final String BSS_STRATEGY_GROUP_INVALID = "ERR-BSS-STRATEGY-GROUP-INVALID";
	public static final String BSS_GROUP_WAIT_PERIOD_INVALID = "ERR-BSS-GROUP-WAIT-PERIOD-INVALID";

	// Prospect common data
	public static final String ERR_BSS_PROSPECT_REQUEST = "ERR-BSS-PROSPECT-REQUEST";

	// Submit
	public static final String BSS_STRATEGY_DEFAULT_SUBMIT_FAIL = "ERR-BSS-50001";
	public static final String BSS_STRATEGY_COMPANY_SUBMIT_FAIL = "ERR-BSS-50002";

	// EMAIL ERROR
	public static final String BSS_EMAIL_SERVICE_ERROR = "ERR-BSS-90001";
	
	// CACHE IMPLEMENTATION ERROR
	public static final String BSS_CACHE_IMPL_ERROR = "ERR-BSS-60000";

	// Minimum funding exception error
	public static final String BSS_MIN_FUNDING_EXCEPTION_ERROR = "ERR-BSS-80001";
	
	//Submit failure error
	public static final String BSS_SUBMIT_FAILURE_EXCEPTION = "ERR-BSS-70000";
	
	// Request validation exception
	public static final String REQUEST_VALIDATION_ERROR = "ERR-BSS-90000";
	
	// Plan Deselection exception error
	public static final String BSS_PLAN_DESELECTION_EXCEPTION_ERROR = "ERR-BSS-80011";
	
	// Plan Compare exception error
	public static final String BSS_PLAN_COMPARE_EXCEPTION_ERROR = "ERR-BSS-90011";
	
	// Plan attribute value errors
	public static final String BSS_ATTRIBUTE_VALUE_ERROR = "ERR-BSS_ATTRIBUTE_VALUE_ERROR"; 
	
	// Plan year not found
	public static final String BSS_REALM_PLAN_YEAR_NOT_FOUND = "ERR-BSS-REALM-PLAN-YEAR-NOT-FOUND";
	
	// Strategy sync event Event in process
	public static final String BSS_STRATEGY_SYNC_EVENT_IN_PROCESS = "ERR-BSS-STRATEGY-SYNC-EVENT-IN-PROCESS";

	// Bundle sync event in process (BSS Core bundle selection processing)
	public static final String BSS_BUNDLE_SYNC_EVENT_IN_PROCESS = "ERR-BSS-BUNDLE-SYNC-EVENT-IN-PROCESS";

	// BSS Core API authentication/authorization failure
	public static final String BSS_CORE_AUTH_ERROR = "ERR-BSS-CORE-AUTH-ERROR";

	// Error code for Coverage Tier
	public static final String BSS_INVALID_COVERGAE_TIER_CODE = "BSS_INVALID_COVERGAE_TIER_CODE";
	
	// Plans Not Found
	public static final String BSS_PLANS_NOT_FOUND = "ERR-BSS-PLANS-NOT-FOUND";

}
