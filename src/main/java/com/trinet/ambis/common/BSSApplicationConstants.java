 package com.trinet.ambis.common;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author rvutukuri
 *
 */
public class BSSApplicationConstants {

	private BSSApplicationConstants() {
	}

	public static final String API_NAME = "api-bss";
	public static final String GET_VERSION = "/v1.0";
	public static final String BASE_RESOURCE = GET_VERSION + API_NAME;

	public static final List<String> PRIMARY_PLAN_TYPES = Arrays.asList("10", "11", "14", "1D", "1V");
	public static final List<String> MEDICAL_PLAN_TYPES = Arrays.asList("10");
	public static final List<String> DENTAL_PLAN_TYPES = Arrays.asList("11", "1D");
	public static final List<String> VISION_PLAN_TYPES = Arrays.asList("14", "1V");
	public static final List<String> VOLUNTARY_PLAN_TYPES = Arrays.asList("1D", "1V");
	public static final List<String> ADDITIONAL_PLAN_TYPES=Arrays.asList("23", "30", "31");
	public static final List<String> ADDITIONAL_PLAN_TYPES_INCLUD_CMTR = Arrays.asList("20", "23", "30", "31", "A3",
			"67");
	public static final List<String> PRIMARY_PLAN_TYPES_ER = Arrays.asList("10", "11", "14");
	public static final List<String> DISABILITY_PLAN_TYPES = Arrays.asList("30", "31");
	public static final List<String> ALL_PLAN_TYPES_INCLUD_ADDITIONAL = Arrays.asList("10", "11", "14", "1D", "1V", "23", "30", "31");
	public static final List<String> EMPTY_PLAN_TYPES = Arrays.asList("");
	public static List<String> getDisabilityPlanTypes() {
		List<String> list = new ArrayList<>();
		list.addAll( DISABILITY_PLAN_TYPES );
		return list;
	}	
	public static List<String> getCheckPlanTypes() {
		List<String> list = new ArrayList<>();
		list.addAll( ADDITIONAL_PLAN_TYPES );
		return list;
	}
	public static final List<String> PLAN_TYPES = Arrays.asList("Life / AD&D", "HSA", "Waiver Allowance");
	public static List<String> getPlanTypeNames() {
		List<String> list = new ArrayList<>();
		list.addAll( PLAN_TYPES );
		return list;
	}
	private static final List<String> LIFE_PLAN_TYPES = Arrays.asList("20","21","22","23","24","25","26","27","28","2W","2X","2Y");
	public static List<String> getLifePlanTypes() {
		List<String> list = new ArrayList<>();
		list.addAll( LIFE_PLAN_TYPES );
		return list;
	}

	// time/date constants
	public static final long ONE_DAY = 86400000L;

	// plan type constants
	public static final String MEDICAL_PLAN_TYPE = "10";
	public static final String MEDICAL_PLAN_TYPE_DP = "15";
	public static final String DENTAL_PLAN_TYPE_DP = "16";
	public static final String DENTAL_PLAN_TYPE = "11";
	public static final String VISION_PLAN_TYPE = "14";
	public static final String VISION_PLAN_TYPE_DP = "17";
	public static final String VOLUNTARY_DENTAL_PLAN_TYPE = "1D";
	public static final String VOLUNTARY_DENTAL_PLAN_TYPE_DP = "1E";
	public static final String VOLUNTARY_VISION_PLAN_TYPE = "1V";
	public static final String VOLUNTARY_VISION_PLAN_TYPE_DP = "1U";
	public static final String ACCIDENT_PLAN_TYPE = "1A";
	public static final String ACCIDENT_PLAN_TYPE_DP = "18";
	public static final String CRIT_ILLNESS_PLAN_TYPE = "1G";
	public static final String CRIT_ILLNESS_PLAN_TYPE_DP = "1H";
	public static final String INDEMNITY_PLAN_TYPE = "1X";
	public static final String INDEMNITY_PLAN_TYPE_DP = "1K";
	public static final String LIFE_CODE = "23";
	public static final String STD_CODE = "30";
	public static final String LTD_CODE = "31";
	public static final String CMTR_CODE = "A3";
	public static final String ADDITIONAL_CODE = "99";
	public static final String WAIVER_ALLOWANCE_PLAN_SUB_TYPE = "WA";
	public static final String BSUPP_ALL_VOL_PLAN_ID = "XX";
	public static final String DISABILITY_CODE = "DI";

	public static final String MEDICAL = "medical";
	public static final String VISION = "vision";
	public static final String DENTAL = "dental";
	public static final String DISABILITY = "DISABILITY";
	public static final String STD = "STD";
	public static final String LTD = "LTD";
	public static final String LIFE = "LIFE";
	public static final String HSA = "HSA";
	public static final String PRIMARY = "primaryBenefit";
	public static final String ADDITIONAL = "additionalBenefit";
	public static final String CMTR = "CMTR";

	// plan appendix constants
	public static final int PLAN_APPENDIX_MAX_LINES_FIRST_PAGE = 23;
	public static final int PLAN_APPENDIX_MAX_LINES_SUBSEQUENT_PAGES = 25;
	public static final String PLAN_APPENDIX_FIRST_BEN_TYPE = "PlanappendixfirstBentype";
	public static final int PLAN_APPENDIX_HEADER_LINES = 4;
	// plan emoloyee cost summary constants
	public static final int EMPLOYEE_COST_SUMMARY_MAX_LINES_FIRST_PAGE = 22;
	public static final int EMPLOYEE_COST_SUMMARY_MAX_LINES_SUBSEQUENT_PAGES = 25;
	public static final int EMPLOYEE_COST_SUMMARY_HEADER_LINES = 4;
	// plan compare constants
	public static final int PLAN_COMPARE_MAX_LINES_FIRST_PAGE = 20;
	public static final int PLAN_COMPARE_MAX_LINES_SUBSEQUENT_PAGES = 25;
	public static final int PLAN_COMPARE_HEADER_LINES = 4;
	public static final List<String> PRIMARY_PLAN_TYPE_NAMES = Arrays.asList(MEDICAL, DENTAL, VISION);
    public static final String TNIV_NAICS_LIFE_BAND_CODE = "TNIV_NAICS_LIFE_BAND_CODE";
    public static final String TNIV_NAICS_DISABILITY_BAND_CODE = "TNIV_NAICS_DISABILITY_BAND_CODE";
    // eligibility constants

	// eligibility constants
	public static final List<String> ELIG_INACTIVE = Arrays.asList( "2009", "23GC", "236Q" );


	// Group Types:
	public static final String K1_GROUP_TYPE = "K1";
	public static final String STD_GROUP_TYPE = "STD";
	public static final String CLIENT_K1_GROUP_NAME = "K1";
	public static final String WAIT_PERIOD_NONE = "NONE";
    public static final String CLIENT_MA_GROUP_NAME = "W2 MA";

	public static final List<String> GROUP_TYPES = Arrays.asList(STD_GROUP_TYPE, K1_GROUP_TYPE);
	
	// Strategy Types:
	public static final String STRATEGY_TYPE_REFERENCE = "reference";
	public static final String STRATEGY_TYPE_CUSTOM = "custom";
	public static final String STRATEGY_TYPE_FUTURE = "future";
	public static final String STRATEGY_TYPE_RECOMMENDED = "recommended";
	public static final String STRATEGY_TYPE_CUSTOM_RECOMMENDED = "customrecommended";
	public static final String STRATEGY_TYPE_SELECTED = "selected";
	public static final String STRATEGY_RATE_SUFFIX = "(Updated Rates)";

	// funding type constants
	public static final String BFPCT = "BFPCT";
	public static final String CFPCT = "CFPCT";
	public static final String FLAT = "FLT";
	public static final String CFPCT_DESC = "Covered Person Percent";
	public static final String FLAT_DESC = "Flat";
	public static final String FLAT_MAX = "FLTMAX";
	public static final String BSUPP = "BSUPP";
	public static final String EEC = "EEC";
	public static final List<String> PROSPECT_FUND_TYPES = Arrays.asList(CFPCT, FLAT);

	// Plan override values
	public static final String PLAN_OVERRIDE_BASE = "BASE";
	public static final String PLAN_OVERRIDE_PCT = "PCT";
	public static final String PLAN_OVERRIDE_FLT = "FLT";
	public static final String PLAN_OVERRIDE_FPL = "FPL";
	public static final String PLAN_OVERRIDE_FPL_PCT = "FPL-PCT";
	public static final String PLAN_OVERRIDE_FPL_FLT = "FPL-FLT";
	public static final String PLAN_OVERRIDE_MNF = "MNF";
	public static final String PLAN_OVERRIDE_MNF_PCT = "MNF-PCT";
	public static final String PLAN_OVERRIDE_MNF_FLT = "MNF-FLT";
	public static final String PLAN_OVERRIDE_FLTEE = "FLTEE";

	// funding attributes
	public static final String FUNDING_TYPE = "funding_Type";
	public static final String FUNDING_BASE_PLAN = "funding_Base_plan";
	public static final String FUNDING_BASE_CVG = "funding_Base_coverage";
	public static final String FUNDING_BASE_PCT = "funding_Base_pct";
	public static final String PRIMARY_PLAN_TYPE = "primary_Plan_Type";
	public static final String WAIVER_ALLOWANCE = "waiver_allowance";
	public static final String BSUPP_EXCESS_OPTION = "bsupp_excess_option";
	public static final String CUSTOMIZED = "customized";
	public static final String FUNDING_MODEL_ID = "fundingModelId";
	public static final String FUNDING_PKG_TYPE = "funding_pkg_type";
	public static final String LIMIT = "LIMIT";
	public static final List<String> FUNDING_COVERAGE_LEVELS = Arrays.asList("all", "employee", "employeePlusSpouse",
			"employeePlusChild", "employeePlusFamily");

	// Passport constants
	public static final List<String> TEXAS_SITUS_EXCLUSION_STATES = Arrays.asList("ID", "MN");
	public static final String TRUE = "true";

	// Coverage codes
	public static final String CVG_CODE_ALL = "all";
	public static final String CVG_CODE_EMPLOYEE = "1";

	// EXCLUDED PRODUCT LINES FOR EXCHANGES OTHER THAN ALP
	public static final List<String> EXCULDED_PRODUCT_LINES = Arrays.asList("NOMD", "NOHW");
	public static final List<String> EXCULDED_PRODUCT_LINES_EXCH_XI = Arrays.asList("NOHW");

	// DATE FORMATS
	public static final String DATE_FORMAT_DD_MMM_YYYY = "dd-MMM-yyyy";
	public static final String DATE_PATTERN_MM_DD_YYYY 	= "MM/dd/yyyy";
	public static final String DATE_PATTERN_YYYY_MM_DD 	= "yyyy-MM-dd";
	public static final String DATE_FORMAT_DD_MMM_YY = "dd-MMM-yy";

	// SubmitStatus constants
	public static final String ERROR = "ERROR";
	public static final String SUCCESS = "SUCCESS";
	public static final String PROCESSING = "PROCESSING";
	public static final String UNPROCESSED = "UNPROCESSED";
	public static final String NA = "NA";

	// Model Compare sort order for employee plans
	public static final int MEDICAL_SORT = 0;
	public static final int DENTAL_SORT = 1;
	public static final int VISION_SORT = 2;

	// The plan types that should be always be returned for MC
	public static final List<String> MODEL_COMPARE_CONTRIBUTION_TYPES = Arrays.asList(MEDICAL, BSUPP, DENTAL, VISION, DISABILITY,
			LIFE, CMTR, WAIVER_ALLOWANCE, HSA);
	// Status
	public static final String PENDING_STATUS = "P";
	public static final String STATUS_ACTIVE = "A";
	public static final String STATUS_IN_ACTIVE = "I";
	public static final String STATUS_DELETED = "D";
	public static final List<String> ACTIVE_PENDING_STATUS = Arrays.asList("A", "P");

	// quarters
	public static final String SM_QUARTER = "SM";
	public static final String Q3_QUARTER = "Q3";

	public static final String T2_EXCL_MED_PLAN_BCBSFL = "BCBS";
	
	public static final String AETNA_PORTFOLIO = "1";
	
	public static final int AETNA_PORTFOLIO_INT = 1;
	
	public static final int FLORIDABLUE_PORTFOLIO_INT = 12;
	
	//SITUS
	public static final String SITUS_TX = "TX";
	
	public static final String SITUS_FL = "FL";
	

	public static final List<Long> NEW_COMPANY_DEFAULT_STRATEGIES = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);

	//MINIMUM funding 
	public static final Set<String> MIN_FUNDING_OVERRIDE_STATES_PAS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("FL", "TX")));
	
	public static final BigDecimal MIN_FUNDING_OVERRIDE_PCT_PAS = BigDecimal.valueOf(50);
	
	public static final String Y = "Y";
	public static final String N = "N";
	public static final String NA_STRING = "NA";
	public static final String YES = "YES";
	public static final String NO = "NO";
	public static final	String CUSTOM = "CUSTOM";
	public static final String EMPTY_SPACE = " ";
	
	public static final String ZERO_PERCENT = "0%";
    public static final String ZERO_DOLLAR = "$0";
    public static final String ZERO = "0";
	
	public static final String ADMIN_COMPANY_CODE = "001";
	
	public static final int REDIRECT_NOTIFICATION_URL_ERROR_CODE = 302;
	public static final int SERVER_ERROR_NOTIFICATION_URL_ERROR_CODE = 500;
	
	public static final String HQ_MIN_FUNDING_TYPE = "HQ"; 
	
	public static final String DEFAULT_MIN_FUNDING_TYPE = "DEFAULT";
	
	public static final String TOP_QUALITY_NAME = "Premier";
    public static final String TOP_QUALITY_ID = "PRM";

    public static final String BALANCED_PACKAGE_NAME = "Intermediate";
    public static final String BALANCED_ID = "INT";

    public static final String CONSERVATIVE_PACKAGE_NAME = "Conservative";
    public static final String CONSERVATIVE_ID = "CON";
    
    public static final String PROCESS_STATUS_NEW = "N";
    public static final String PROCESS_STATUS_INPROGRESS = "I";
    public static final String PROCESS_STATUS_PROCESSED = "P";
    public static final String PROCESS_STATUS_FAILED = "F";
    
    public static final String DISABILITY_OFFERED_GROUP_TYPE_ALL = "ALL";
    
    public static final String PLANTOEXCLUDE = "PLANTOEXCLUDE";
    
    public static final String EMPIRE_MED_PLAN_GRP = "EM01";
    public static final String BCBSCA_MED_PLAN_GRP = "BSCA";

    public static final String MAP_REASON_TRINET = "TriNet";
    public static final String MAP_REASON_CLIENT = "Client";
    
    public static final int QUERY_IN_CLAUSE_PARTITION_SIZE = 999;
    
    public static final String DEFAULT_SUBMIT = "DEFAULT_SUBMIT";
	public static final String CREATED_BY_PRELOAD = "PRELOAD";
	public static final String BANDCHANGE_USER_ID = "BANDCHANGE";
	public static final String SYNC_FAILURE = "STRATEGY_SYNC_FAILED";
	
	public static final String TTL_FOR_CACHE = "64800";
	public static final String TTL_FOR_STRATEGY_CACHE = "240";
	
	public static final int COMMUTER_ELIGIBLE_EMPLOYEES = 20;
		
	public static final String FPL = "FPL";
	public static final String DFLT = "DFLT";
	public static final String MND = "MND";
	
	//HSA Frequencies
	public static final String HSA_ANNUAL = "A";
	public static final String HSA_QUARTERLY = "Q";
	public static final String HSA_MONTHLY = "M";

	public static final String PLANTYPE = "PLAN TYPE";
	public static final String APPROVERS = "APPROVERS";
	public static final String ORIGINATION = "ORIGINATION DEPT";
	public static final String EXCEPTIONVALUETYPE = "EXCEPTION VALUE TYPE";

	public static final String MIN_FUND_EXCEPTION_APP_KEY = "BSS-MFE";
	public static final String BEN_OFFER_EXCEPTION_APP_KEY = "BSS-BOE";
	public static final String PLAN_DESELECTION_EXCEPTION_APP_KEY = "BSS-PDE";

	public static final List<String> TRINET_COMPANIES = Arrays.asList("000", "001", "002", "003", "004", "005", "006", "007",
			"008", "009", "00A", "00B", "00C", "020", "DVP", "E42", "NWH", "PAZ", "PEJ", "SQU");
	
	public static final String TRINET_COMPANY_ID = "000";
	public static final String TRINET_EMPL_ID = "00000000000";
	
	public static final String CONFIRMATION_STMT_DOC_TYPE = "BCS";
	
	//Process status name
	public static final String TERMED_CLIENT_DEFAULT_SUBMIT_PROCESS = "TERM_DEFAULT";
	public static final Set<String> SUBMIT_PROCESS_NAMES = Set.of("SUBMIT", "RESUBMIT",
			TERMED_CLIENT_DEFAULT_SUBMIT_PROCESS, "BANDCODE_RESUBMIT");
	
	public static final List<String> STD_EMPLOYEE_PAID_PLANS = Collections
			.unmodifiableList(Arrays.asList("006J2G", "006J2J"));
	public static final List<String> LTD_EMPLOYEE_PAID_PLANS = Collections
			.unmodifiableList(Arrays.asList("006J4G", "006J4H"));
	
	//Email Strings
	public static final String FLATMAX_LABEL = "By Amount";
	public static final String NOT_AVAILABLE = "Not Available";

	public static final Set<String> ALL_LOCATIONS = Collections.unmodifiableSet(Stream.of("AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE",
			"FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS", "MT",
			"NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA", "PR", "RI", "SC", "SD", "TN", "TX",
			"UT", "VA", "VT", "WA", "WI", "WV", "WY", "XX","AS","GU","MP","VI").collect(Collectors.toSet()));

	public static final String COMPANY_HEAD_QUARTERS_FL = "FL"; 

	public static final int PRELOAD_STATUS_LAST_30_DAYS= 30; 

	public static final String INTERNAL_SUBMIT_USER = "Internal";
	
	public static final String FORBIDDEN_EXCEPTION_TEXT = "Access denied";

	public static final int ACA_FPL_OPTED_IN = 1;
	public static final int ACA_FPL_OPTED_OUT = 0;
	
	public static final String INVALID_PLAN_ENROLLMENT = "I";
	
	public static final String BSS_EXPORT_TEMPLATE = "bss_export_template";

	public static final String PROSPECT_PLAN_EXPORT_TEMPLATE = "prospect_incumbent_plan_export_template";
	
	public static final String PROSPECT_INCUMBENT_PLAN_MEDICAL = "prospect_incumbent_plan_medical";

	public static final String PROSPECT_INCUMBENT_PLAN_DENTAL = "prospect_incumbent_plan_dental";

	public static final String PROSPECT_INCUMBENT_PLAN_VISION = "prospect_incumbent_plan_vision";

	public static final String BSS_EXPORT_MEDICAL = "bss_export_medical";

	public static final String BSS_EXPORT_DENTAL = "bss_export_dental";

	public static final String BSS_EXPORT_VISION = "bss_export_vision";

	public static final String MEDICAL_BSS_OUTPUT = "medical_bss_output";
	
	public static final String DENTAL_BSS_OUTPUT = "dental_bss_output";
	
	public static final String VISION_BSS_OUTPUT = "vision_bss_output";

	public static final String TRINET_STRATEGY = "TriNet Strategy";

	public static final String PRIMARY_HEADCOUNT_KEY = "primaryHeadcount";

	
	public static final String SECONDARY_HEADCOUNT_KEY = "secondaryHeadcount";
    public static final String TOTAL_HEADCOUNT_KEY = "totalHeadcount";

	public static final String AETNA_BAND_CARRIER = "AETNA";
	public static final String AETNA_HMO_BAND_CARRIER = "AETNAHMO";
	public static final String AETNA_PPO_BAND_CARRIER = "AETNAPPO";
	public static final String BCBS_BAND_CARRIER = "BCBS";
	public static final String BCBSNC_BAND_CARRIER = "BCBSNC";
	public static final String BCBSID_BAND_CARRIER = "BCBSID";
	public static final String BCBSCA_BAND_CARRIER = "BCBSCA";
	public static final String DISABILITY_BAND_CARRIER = "DIABILITY"; // STOP ####### Don't fix the spelling
	public static final String KAISERCO_BAND_CARRIER = "KAISERCO";
	public static final String KAISER_BAND_CARRIER = "KAISER";
	public static final String LIFE_BAND_CARRIER = "LIFE";
	public static final String TUFFS_BAND_CARRIER = "TUFFS";          // STOP ####### Don't fix the spelling
	public static final String UHC_BAND_CARRIER = "UHC";
	public static final String BCBSMN_BAND_CARRIER = "BCBSMN";
	public static final String KAISERHI_BAND_CARRIER = "KAISERHI";
	public static final String KAISERMD_BAND_CARRIER = "KAISERMD";
	public static final String KAISERNW_BAND_CARRIER = "KAISERNW";
	public static final String EMPIRENY_BAND_CARRIER = "EMPIRENY";
	public static final String HARVARD_BAND_CARRIER = "HARVARD";
	public static final String HIGHMARK_BAND_CARRIER = "HIGHMARK";

	public static final Map<String, String> databaseTemplatesMap = Map.of(MEDICAL_PLAN_TYPE, MEDICAL_BSS_OUTPUT,
			DENTAL_PLAN_TYPE, DENTAL_BSS_OUTPUT,
			VOLUNTARY_DENTAL_PLAN_TYPE, DENTAL_BSS_OUTPUT,
			VISION_PLAN_TYPE, VISION_BSS_OUTPUT,
			VOLUNTARY_VISION_PLAN_TYPE, VISION_BSS_OUTPUT);
	
	public static final String DUMMY = "DUMMY";
	// Redis object cache TTL
	public static final String TTL_FOR_OMS_BENEFIT_PLAN_RATES = "120";
    public static final String TTL_FOR_FLEX_RATES_PLAN_RATES = "120";

	//SYSTEM_ACCOUNT
	public static final String SYSTEM_ACCOUNT = "systemAccount";
	public static final String EMPLID_SYSTEM_ACCOUNT = "00000000000";
	public static final String PROPOSED_STRATEGY_NAME = "Proposed Strategy";
	public static final String RECIPIENT_ERROR = "Recipient must not be null";
	
	public static final Map<String, String> exportTemplatesMap = Map.of(MEDICAL_PLAN_TYPE, BSS_EXPORT_MEDICAL,
			DENTAL_PLAN_TYPE, BSS_EXPORT_DENTAL, VOLUNTARY_DENTAL_PLAN_TYPE, BSS_EXPORT_DENTAL, VISION_PLAN_TYPE,
			BSS_EXPORT_VISION, VOLUNTARY_VISION_PLAN_TYPE, BSS_EXPORT_VISION);

	public static final Map<String, String> prospectTemplatesMap = Map.of(MEDICAL_PLAN_TYPE,
			PROSPECT_INCUMBENT_PLAN_MEDICAL, DENTAL_PLAN_TYPE, PROSPECT_INCUMBENT_PLAN_DENTAL,
			VOLUNTARY_DENTAL_PLAN_TYPE, PROSPECT_INCUMBENT_PLAN_DENTAL, VISION_PLAN_TYPE,
			PROSPECT_INCUMBENT_PLAN_VISION, VOLUNTARY_VISION_PLAN_TYPE, PROSPECT_INCUMBENT_PLAN_VISION);
	
	public static final String VERSION_V2 = "v2";

	public static final String DEFAULT_RISK_TYPE = "BANDS";

	public static final int PROSPECT_EXPIRY_DAYS = 90;

	public static final long CUSTOM_BUNDLE_ID = -1L;
	public static final String CUSTOM_BUNDLE_NAME = "CUSTOM";

	// Preferences link status constants
	public static final String PREFERENCES_LINK_STATUS_SHOW = "show";
	public static final String PREFERENCES_LINK_STATUS_HIDE = "hide";
	public static final String PREFERENCES_LINK_STATUS_DISABLED = "disabled";
	public static final String PREFERENCES_LINK_STATUS_READ_ONLY = "readOnly";

	public static final String REGIONAL = "REGIONAL";
	public static final String NATIONAL = "NATIONAL";
}
