/**
 * 
 */
package com.trinet.ambis.common;

/**
 * @author rvutukuri
 *
 */
public class BSSQueryConstants {

	private BSSQueryConstants() {
		throw new IllegalStateException(
				"Constants class " + BSSQueryConstants.class.getName() + " can not be instantiated.");
	}
	
	public static final String ORACLE_NULL = "";

	// PS Query constants
	public static final String BEN_PROGRAM = "benProgram";
	public static final String CLONE_BEN_PROGRAM = "cloneBenProgram";
	public static final String CLONE_COMPANY = "cloneCompany";
	public static final String CLONE_ELIG_RULE = "cloneEligRule";
	public static final String CLONE_PROGRAM = "cloneProgram";
	public static final String COMPANY = "company";
	public static final String EFF_DT = "effDt";
	public static final String EFF_STATUS = "effStatus";
	public static final String ELIG_CONFIG_1 = "eligConfig1";
	public static final String ELIG_RULES_ID = "eligRulesId";
	public static final String EXCESS_CREDIT_OPTION = "excessCreditOption";
	public static final String PERSON_ID = "personid";
	public static final String PF_CLIENT = "pfClient";
	public static final String PLAN_START_DATE = "planStartDate";
	public static final String RATE_TBL_ID = "rateTblId";
	public static final String SHORT_DESCR = "shortDescr";
	public static final String GROUP_TYPE = "groupType";
	public static final String GROUP_STATE = "groupState";
	public static final String T2_BENSUPP_NB = "waiverAllowance";
	public static final String T2_BENSUPP_DEP = "benSuppC";
	public static final String T2_BENSUPP_EE = "benSupp1";
	public static final String T2_BENSUPP_FAM = "benSupp4";
	public static final String T2_BENSUPP_SP = "benSupp2";
	public static final String T2_FORFEIT_EXC_CR = "excessCreditOpt";
	public static final String T2_FUNDING_OPTN = "legacyFundingOpt";
	public static final String ER_HSA_LVL = "ErHsaLvl";
	public static final String ER_HSA_FRT_EE = "ErHsaFrtEe";
	public static final String ER_HSA_FRT_FAM = "ErHsaFrtFam";
	public static final String HSA_FRT_PAYOUT = "HsaFrtPayout";
	public static final String ER_HSA_MON_EE = "ErHsaMonEe";
	public static final String ER_HSA_MON_FAM = "ErHsaMonFam";
	public static final String HSA_CNTB_FRQ = "HsaCntbFrq";
	public static final String HSA_FRNT_FRQ = "HsaFrntFrq";
	public static final String HSA_FRTMNTH_Q1 = "HsaFrtmnthQ1";
	public static final String HSA_FRTMNTH_Q2 = "HsaFrtmnthQ2";
	public static final String HSA_FRTMNTH_Q3 = "HsaFrtmnthQ3";
	public static final String HSA_FRTMNTH_Q4 = "HsaFrtmnthQ4";
	public static final String GROUP_DENTAL = "groupDental";
	public static final String OPTIONAL_DENTAL = "optionalDental";
	public static final String GROUP_VISION = "groupVision";
	public static final String OPTIONAL_VISION = "optionalVision";


	// HRDB Query constants
	public static final String ADDITIONAL_PLAN_TYPES = "additionalPlanTypes";
	public static final String BENEFIT_PROGRAM = "benefitProgram";
	public static final String RISK_TYPE = "riskType";
	public static final String COMPANY_CODE = "companyCode";
	public static final String COMPANY_ID = "companyId";
	public static final String EFF_DATE = "effDate";
	public static final String EFF_DATE_STR = "effdtStr";
	public static final String EMPL_ID = "emplId";
	public static final String GROUP_ID = "groupId";
    public static final String GROUP_IDS = "groupIds";
	public static final String INDUSTRY_TYPE = "industryType";
	public static final String OPTION_ID = "optionId";
	public static final String PEO_ID = "peoId";
	public static final String PICK_CHOOSE_FLAG = "pickChooseFlag";
	public static final String PLAN_TO_EXCLUDE = "PLANTOEXCLUDE";
	public static final String PORTFOLIOS_DUMMY = "0";
	public static final String PLAN_TYPES = "planTypes";
	public static final String MD_PLAN_TYPES = "mdPlanTypes";
	public static final String VISION_PLAN_TYPES = "visionPlanTypes";
	public static final String PLAN_TYPE = "planType";
	public static final String PLAN_TYPE_PATTERN = "planTypePattern";
	public static final String PLAN_YEAR_ID = "planYearId";
	public static final String PREVIOUS_REALM_PLAN_YEAR_ID = "prevRealmPlanYearId";
	public static final String PRIMARY_PLAN_TYPES = "primaryPlanTypes";
	public static final String PRODUCT_LINE = "productLine";
	public static final String ALE_STATUS = "ale_status";
	public static final String ALE_TAX_YEAR = "ale_tax_year";
	public static final String CMN_OWNER_COMP = "cmn_owner_company";
	public static final String OE_QTR_EXCE = "oe_quarter_exce";
	public static final String OE_QTR = "oe_quarter";
	public static final String REALM_PLAN_YEAR_ID = "realmPlanYearId";
	public static final String REALM_YEAR_ID = "realmYearId";
	public static final String REALM_ID = "realmId";
	public static final String REGIONS = "regions";
	public static final String SITUS = "situs";
	public static final String STATE = "state";
	public static final String STRATEGY_ID = "strategyId";
	public static final String HIBERNATE_FETCH_SIZE_250 = "250";
	public static final String HIBERNATE_FETCH_SIZE_500 = "500";
	public static final String HIBERNATE_FETCH_SIZE_1000 = "1000";
	
	public static final String PORTFOLIOS = "portfolios";
	public static final String ZIP_CODE = "zipCode";
	public static final String MED_PLAN_GRPS = "medPlanGroups";
	public static final String OE_QUARTER = "oeQuarter";
	public static final String OUT_OF_REGION_PLANS = "outOfRegionPlans";
	public static final String STRATEGY_LIST = "strategyList";
	public static final String STRATEGY_IDS = "strategyIds";
	
	public static final String GET_COVERAGE_CODES_BY_PLAN_TYPES = "getCoverageCodesByAllPlanTypes";
	public static final String LIFE_CVG_FORMULA_PROPERTIES = "LIFE_CVG_FORMULA_PROPERTIES";
	public static final String DISABILITY_CVG_FORMULA_PROPERTIES = "DISABILITY_CVG_FORMULA_PROPERTIES";
	public static final String STRATEGY_PORTFOLIO_MISSING_PLANS = "STRATEGY_PORTFOLIO_MISSING_PLANS";
	public static final String STRATEGY_PORTFOLIO_MISSING_PLANS_V2 = "STRATEGY_PORTFOLIO_MISSING_PLANS_V2";
	public static final String GET_BUNDLE_SELECTION_DETAILS_BY_EXCHANGES = "GET_BUNDLE_SELECTION_DETAILS_BY_EXCHANGES";
	public static final String EE_RATE = "eeRat";
	public static final String ER_RATE = "erRate";
	public static final String BAND_CODE = "bandCode";
	public static final String LIFE_BAND_CODE = "lifeBandCode";
	public static final String DIS_BAND_CODE = "disBandCode";
	
	public static final String ACA_LARGE_EMPLR = "acaLargeEmplr";
	public static final String ACA_FPL_OPTED = "acaFplOpted";
	
	/** Added changes for the plan compare implementation **/
	public static final String PLAN_YEAR_DATE = "planYearDate";
	public static final String QUARTER_NAME = "quarterName";
	public static final String LASTXY_YEARS = "lastXYears";
	public static final String NEXTXY_YEARS = "nextXYears";
	
	public static final String HQ_REGION = "HQ_REGION";
	public static final String PRIM_PORTFOLIO = "PRIM_PORTFOLIO";
	public static final String PLAN_OFFER_REALM_YEAR_ID = "REALM_YEAR_ID";
	public static final String PORTFOLIO_IDS = "portfolioIds";
	public static final String PLYR_CHANGE_SYNC_EXCUTED = "plYrChangeSyncExcuted";

	public static final String BUNDLE_ID = "bundleId";
	public static final String BUNDLE_IDS = "bundleIds";
	public static final String RETURN_ONLY_BUNDLE_PLAN = "returnOnlyBundlePlans";
	public static final String CVG_TIER_CODE = "cvgTierCode";

	public static final String EMPLOYEE_CLASS_TYPE = "t2EmployeeClassType";

	public static final String EMPLOYEE_CLASS_CODE = "t2EmployeeClassCode";

	public static final String EMPLOYEE_CLASS_NAME = "t2EmployeeClassName";

	public static final String MEASUREMENT = "t2Measurement";

	public static final String STABILITY = "t2Stability";

	public static final String ADMIN_PERIOD = "t2AdminPeriod";

	// Term Status Constants
	public static final String TERM_STATUS_ACTIVE = "ACTIVE";
	public static final String TERM_STATUS_TERMED = "TERMED";

	public static final String RATES_FLAG = "ratesFlag";
	public static final String FILTER_SUB_REGIONS = "filterSubregions";
	
}
