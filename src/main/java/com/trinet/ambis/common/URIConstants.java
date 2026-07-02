package com.trinet.ambis.common;

import com.trinet.security.common.SecurityConstants;

/**
 * @author schaudhari
 *
 */
public class URIConstants {

	private URIConstants() {
	}

	public static final String VERSION = "/v1.0";
	public static final String ROOT = "/benefits";
	public static final String VERSION_AND_ROOT = VERSION + ROOT;
	public static final String VERSION_AND_PLATFORM = VERSION + "/platform/";
	public static final String COMP_AND_EMP_REGEX_PLACEHOLDER = SecurityConstants.COMPANY_AND_EMPLOYEE_PATH_REGEX_PLACEHOLDER;

	/* AdminController */
	public static final String GET_SCHEDULE_DATES = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "schedule-dates/{oeQuarter}/{companyCode}";
	public static final String SCHEDULE_DATES = COMP_AND_EMP_REGEX_PLACEHOLDER + "schedule-dates/{companyCode}";
	public static final String GET_PRODUCTS_QUARTERS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "product-quarters/{companyCode}";
	public static final String SEARCH_COMPANY = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "search-company/{searchParam}/{companyCode}";
	public static final String MID_YEAR_FUNDING_DETAILS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "midyear-funding-details/{companyCode}";
	public static final String REFRESH_COMPANY_MID_YEAR = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "refresh-company-mid-year/{realmYearId}/{companyCode}";
	public static final String PRE_LOAD_STRATEGIES = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "pre-load-strategies/{peoId}/{quarter}/{companyCode}";
	public static final String PRE_LOAD_COMPANIES_STRATEGIES = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "pre-load-strategies-companies";
	public static final String GET_EXCEPTION_ATTRIBUTES = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "exceptionAttributes/{companyCode}";
	public static final String HQ_OVERRIDES = COMP_AND_EMP_REGEX_PLACEHOLDER + "company-hq-overrides";
	public static final String HQ_OVERRIDES_COMPANY = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "company-hq-overrides/{companyCode}";
	public static final String HQ_OVERRIDES_COMPANY_REALMYEARID = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "company-hq-overrides/{companyCode}/{realmYearId}";
	public static final String GET_OEQUARTERS_AND_PLANYEARS = COMP_AND_EMP_REGEX_PLACEHOLDER + "quarters-info";
	public static final String PRE_LOAD_STRATEGIES_STATUS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "pre-load-strategies-status";

	/* BenConfirmationStatementsController */
	public static final String BEN_CONFIRMATION_STATEMENT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "ben-confirmation-statements/{companyCode}";

	/* AdditionalBenefitsController */
	public static final String GET_ADDITIONAL_PLAN_RATES = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "additional-plans-rates/{strategyId}/{companyCode}";
	public static final String GET_ADDITIONAL_PLAN_RATES_NEW_COMPANY = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "additional-plans-rates/{companyCode}";

	/* BenefitGroupController */
	public static final String ADD_GROUP = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "add-benefit-group/{strategyId}/{companyCode}";
	public static final String UPDATE_GROUP = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "update-benefit-group/{groupId}/{strategyId}/{defaultFlag}/{waitPeriod}/{companyCode}";
	public static final String DELETE_GROUP = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "delete-group/{strategyId}/{strategyGroupId}/{companyCode}";
	public static final String UPDATE_GROUP_HEAD_COUNT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "update-headcount/{strategyId}/{companyCode}";
	public static final String BENEFIT_GROUP_NAME = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "benefit-group-name/{groupId}/{companyCode}";
	public static final String BENEFIT_GROUP_NAME_V1 = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "benefit-group-name/{strategyId}/{groupId}/{companyCode}";
    public static final String BENEFIT_GROUP_NAME_TYPE_K1 = COMP_AND_EMP_REGEX_PLACEHOLDER
            + "convert-k1-group/{strategyId}/{groupId}/{companyCode}";

	/* BenefitOfferExceptionController */
	public static final String BENOFFER_EXCEPTIONS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "benoffer-exceptions/{companyCode}";
	public static final String GET_BENOFFER_EXCEPTION = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "benoffer-exceptions/{benOfferExceptionId}/{companyCode}";

	/* CacheController */
	public static final String INVALIDATE_CACHE = COMP_AND_EMP_REGEX_PLACEHOLDER + "cache/{companyCode}";

	/* CommonController */
	public static final String HEALTH_CHECK = "/healthcheck";
	public static final String GET_PERSON_NAME = COMP_AND_EMP_REGEX_PLACEHOLDER + "get-person-name/{companyCode}";
	public static final String REFRESH_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "plans-refresh/{companyCode}";
	public static final String REFRESH_SESSION = COMP_AND_EMP_REGEX_PLACEHOLDER + "session-refresh/{companyCode}";
	public static final String GET_RULES_AND_CONFIGS = COMP_AND_EMP_REGEX_PLACEHOLDER + "rules-configs/{companyCode}";
	public static final String FEATURE_FLAGS = COMP_AND_EMP_REGEX_PLACEHOLDER + "feature-flags/{companyCode}";

	/* CompanyController */
	public static final String GET_COMMON_DATA = COMP_AND_EMP_REGEX_PLACEHOLDER + "common-data/{companyCode}";
	public static final String GET_COMPANY_DATA = COMP_AND_EMP_REGEX_PLACEHOLDER + "company-data/{companyCode}";
	public static final String GET_COMPANY_NAME = COMP_AND_EMP_REGEX_PLACEHOLDER + "company-name/{companyCode}";
	public static final String IS_RENEWAL_COMPANY = COMP_AND_EMP_REGEX_PLACEHOLDER + "is-renewal-company/{companyCode}";
	public static final String BSS_STATUS = COMP_AND_EMP_REGEX_PLACEHOLDER + "bss-status/{companyCode}";
	public static final String PLYR_CHANGE_SYNC_EXCUTED = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategySyncStatus/planYearChange/{companyCode}";
	public static final String BUNDLE_SELECTION_DETAILS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "bundle-selection-details/{companyCode}";
	public static final String RATE_UPDATE = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "rate-update/{companyCode}";
	public static final String QUARTER_CHANGE = COMP_AND_EMP_REGEX_PLACEHOLDER + "quarter-change/{companyCode}";
	public static final String GET_COMPANY_DETAILS_BY_ID = COMP_AND_EMP_REGEX_PLACEHOLDER + "company-details/{companyCode}";
    public static final String RESET_COMPANY = COMP_AND_EMP_REGEX_PLACEHOLDER + "company/reset/{companyCode}";

	/* EmailController */
	public static final String SEND_EMAIL = COMP_AND_EMP_REGEX_PLACEHOLDER + "email/{confirmationId}/{companyCode}";
	public static final String SEND_EMAIL_CLIENT_CONVERSION_FAILED = COMP_AND_EMP_REGEX_PLACEHOLDER + "email/client-conversion-failed";

	/* EmployeeController */
	public static final String GET_EMPLOYEE_DATA = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "employee-data/{strategyId}/{companyCode}";
	public static final String UPDATE_EMPLOYEE_ASSIGNMENT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "update-employee-assignment/{strategyId}/{companyCode}";

	/* HeadCountController */
	public static final String GET_PLANS_HEADCOUNT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "plans-headcount/{strategyId}/{companyCode}";
	public static final String GET_PLAN_HEADCOUNT_MAPPING = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "plan-headcount-mapping/{strategyId}/{companyCode}";

	/* MinFundExceptionController */
	public static final String MIN_FUND_EXCEPTIONS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "minfund-exceptions/{companyCode}";
	public static final String GET_MIN_FUND_EXCEPTION = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "minfund-exceptions/{minFundExceptionId}/{companyCode}";
	
	/* ProspectOutputsController */
	public static final String PROSPECT_OUTPUTS  = COMP_AND_EMP_REGEX_PLACEHOLDER + "generate-output/{companyCode}";
	public static final String PROSPECT_OUTPUTS_JSON  = COMP_AND_EMP_REGEX_PLACEHOLDER + "generate-output-data/{companyCode}";

	/* PlanOfferingsController */
	public static final String PALN_OFFERINGS  = COMP_AND_EMP_REGEX_PLACEHOLDER + "generate-planofferings";
	public static final String GET_PLAN_OFFERING_CARRIERS = COMP_AND_EMP_REGEX_PLACEHOLDER + "plan-offering/exchange-carriers/{companyCode}";


	/* ModelCompareController */
	public static final String GET_MC_STRATEGIES = COMP_AND_EMP_REGEX_PLACEHOLDER + "mc-strategies/{companyCode}";
	public static final String GET_MC_STRATEGY_PLAN_COSTS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "mc-strategy-offer-costs/{strategyIds}/{companyCode}";
	public static final String GET_MC_STRATEGY_GROUP_FUNDING = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "mc-strategy-group-funding-detail/{strategyId}/{companyCode}";
	public static final String GET_MC_STRATEGY_EMPLOYEE_COST = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "employee-strategy-cost/{strategyIds}/{companyCode}";
	public static final String GET_MC_STRATEGY_BENPLAN_HEADCOUNTS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-benefitplan-headcounts/{strategyIds}/{companyCode}";
	public static final String GET_MC_DATA_EXPORT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "model-compare-data-export/{strategyIds}/{companyCode}";
	public static final String GET_MC_STRATEGY_GROUP_HEADCOUNT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "mc-strategy-group-headcount/{strategyIds}/{companyCode}";

	public static final String OUTPUT_FILTER_BENEFIT_OFFERS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "offered-benefits/{strategyIds}/{companyCode}";

	/* PlanRateController */
	public static final String GET_PLAN_RATES = COMP_AND_EMP_REGEX_PLACEHOLDER + "plan-rates/{companyCode}";
	public static final String PLAN_RATES_EXPORT_TO_EXCEL = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "export-to-excel/{companyCode}";
	public static final String GET_PLAN_RATES_FOR_EXPORT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "plan-rates-for-export/{companyCode}";
	public static final String GET_SELECTED_PLAN_RATES_EXPORT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "/selected-plan-rates-export/{company-code}";

	/* ResubmitController */
	public static final String RESUBMIT_STRATEGY = COMP_AND_EMP_REGEX_PLACEHOLDER + "strategy-data/{companyCode}";
	public static final String SUBMIT_DEFAULT_STRATEGY = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "submit-default-strategy/{quarter}/{companyCode}";
	public static final String PROCESS_PENDING_SUBMISSIONS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "process-pending-submissions/{companyCode}";

	/* Plan Assignments Controller */
	public static final String PLAN_ASSIGNMENTS = COMP_AND_EMP_REGEX_PLACEHOLDER + "plan-assignment/";
	public static final String PLAN_RATE = "plan-rate/";
	public static final String PLAN_ASSIGNMENTS_BY_STRATEGY = PLAN_ASSIGNMENTS + "{strategyId}/{companyCode}";
	public static final String PLAN_ASSIGNMENTS_BY_STRATEGY_GROUP = PLAN_ASSIGNMENTS + "{strategyId}/{groupId}/{companyCode}";
	public static final String PLAN_ASSIGNMENT_BASE_PLANS = PLAN_ASSIGNMENTS + "base-plans/{companyCode}";
	public static final String PLAN_ASSIGNMENT_PLAN_RATE_BY_EMPLOYEE = PLAN_ASSIGNMENTS + PLAN_RATE + "{prospectEmployeeID}/{planId}/{coverageLevelCode}/{benefitType}/{companyCode}";
	public static final String PLAN_ASSIGNMENT_BASE_PLAN_ELIGIBLE_EMPL = PLAN_ASSIGNMENTS + "base-plan-eligible-empl/{strategyId}/{groupId}/{companyCode}";

	public static final String ELIG_REGIONAL_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "eligible-plans/";
	public static final String ELIG_REGIONAL_PLANS_BY_STRATEGY = ELIG_REGIONAL_PLANS + "{strategyId}/{groupId}/{benefitType}/{state}/{zipCode}/{companyCode}";
	
	/* PlatformRequestController */
	public static final String SUBMIT_DEFAULT_STRATEGY_PLATFORM = "submit-default-strategy/{peoId}/{quarter}/{companyCode}";
	public static final String RESUBMIT_BAND_CODE_PLATFORM = "band-code-change-resubmit/{companyCode}";

	/* StrategyController */
	public static final String CREATE_STRATEGY = COMP_AND_EMP_REGEX_PLACEHOLDER + "strategy-summary/{companyCode}";
	public static final String STRATEGY_SUMMARY = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-summary/{strategyId}/{companyCode}";
	public static final String GET_STRATEGY_SUMMARIES = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-summaries/{companyCode}";
	public static final String CREATE_STRATEGY_SUMMARIES_ONBOARDING = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-summaries-onboarding/{companyCode}";
	public static final String GET_STRATEGY_SUMMARY_HISTORY = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-summaries/history/{companyCode}";
	public static final String UPDATE_STRATEGY_SUMMARY_HISTORY = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-summaries/history-update/{companyCode}";
	public static final String STRATEGY_NAME = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-name/{strategyId}/{companyCode}";
	public static final String GET_STRATEGY_SUBMIT_STATUS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-submit-status/{companyCode}";
	public static final String STRATEGY_BUDGET_FACTOR = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-summary/{strategyId}/budget-factor/{companyCode}";
	public static final String PROSPECT_STRATEGY_SUBMIT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "strategy-summaries/{strategyId}/submit/{companyCode}";
	public static final String CREATE_STRATEGIES = COMP_AND_EMP_REGEX_PLACEHOLDER + "strategies/{companyCode}";

	/* TemplateDataController */
	public static final String NEW_COMPANY_STRATEGY_OPTIONS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "new-company-strategy-options/{companyCode}";
	public static final String GET_BENEFITS_CATEGORIES = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "benefits-categories/{companyCode}";

	/* SupplementalAuthController */
	public static final String EXEC_SUPP_LTD_AUTH_RESPONSE = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "exec-supp-ltd-auth-response/{companyCode}";

	/* PlanDeselectionExceptionController */
	public static final String PLAN_DESELECTION_EXCEPTIONS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "plan-deselection-exceptions/{companyCode}";
	public static final String PLAN_DESELECTION_EXCEPTIONS_GET_BY_EXCEPTION_ID = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "plan-deselection-exceptions/{planDeselectionExceptionId}/{companyCode}";
	
	/* PlanCompareController */
	public static final String PLAN_COMPARE = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "plan-compare/{trinetStrategyIds}/{companyCode}";
	public static final String COMPANY_PLAN_COMPARE_EXPORT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "company-plan-compare-export/{futureStrategyIds}/{companyCode}";
	public static final String PLAN_COMPARE_ASSIGNMENT = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "plan-compare/assignment/{prospectPlanId}/{trinetPlanId}/{companyCode}";
	
	/* PlanCompareController : current year plans API */
	public static final String CURRENT_YEAR_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "current-year-plans/{companyCode}";
	public static final String MAPPING_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "future-mapping-plans/{companyCode}";
	public static final String PLAN_COMPARE_EXPORT = COMP_AND_EMP_REGEX_PLACEHOLDER + "plan-compare-export/{companyCode}";
	public static final String FUTURE_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "future-year-plans/{companyCode}";
	
	/* ExchangeController */
	public static final String GET_EXCHANGE_CARRIERS = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "exchange-carriers/{companyCode}";
	public static final String EXCHANGE_BANDS = COMP_AND_EMP_REGEX_PLACEHOLDER + "exchange-bands/{companyCode}";
	
	public static final String BSS_REPORT = "/doc-gen/{companyId}/{employeeId}/fetch";

	public static final String PROSPECT_CENSUS_SYNC = COMP_AND_EMP_REGEX_PLACEHOLDER + "prospect/census/{companyCode}";

	public static final String PROSPECT_TIB_RATE_SYNC_ON_DEPENDENT_CHANGE = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "prospect/tib/dependent/{companyCode}";

    public static final String PROSPECT_TIB_RATE_SYNC_ON_CENSUS_DEPENDENT_CHANGE = COMP_AND_EMP_REGEX_PLACEHOLDER
            +"prospect/tib/dependent/census/{companyCode}";

	/* BenefitGroupFundingController */
	public static final String GROUP_FUNDING = COMP_AND_EMP_REGEX_PLACEHOLDER
			+ "benefit-group-funding/{strategyId}/{groupId}/{companyCode}";

	/* BenefitPlansController */
	public static final String BENEFIT_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "/prospect/benefit-plans/{prospectId}";

	public static final String COST_SUMMARY = COMP_AND_EMP_REGEX_PLACEHOLDER + "new-company/cost-summary/{strategyId}/{companyCode}";

	public static final String PROSPECT_TO_CLIENT_CONVERSION = COMP_AND_EMP_REGEX_PLACEHOLDER + "prospect-to-client/{prospectId}";

	/* BenefitsBundleController */
	public static final String BUNDLE_BY_QUARTER = COMP_AND_EMP_REGEX_PLACEHOLDER + "bundle-by-quarters";
	public static final String BUNDLES_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "bundles/plans";

	/* BundleController */
	public static final String BUNDLE_PLANS = COMP_AND_EMP_REGEX_PLACEHOLDER + "bundles/plans/{companyCode}";

	/* AleController */
	public static final String UPDATE_ALE = COMP_AND_EMP_REGEX_PLACEHOLDER + "ale/{bssCompanyId}/{companyCode}";

	/* CensusController */
	public static final String BSS_CORE_CENSUS_SYNC = COMP_AND_EMP_REGEX_PLACEHOLDER + "bss-core/census/{companyCode}";
	
	/* PlanYearSyncProcessController */
	public static final String UPDATE_PLAN_YEAR = COMP_AND_EMP_REGEX_PLACEHOLDER+"update-plan-year";

	/* ProspectDataController */
	public static final String UPDATE_PROSPECT_DATA = COMP_AND_EMP_REGEX_PLACEHOLDER + "prospect-data/{companyCode}";
}
