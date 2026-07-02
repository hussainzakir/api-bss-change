package com.trinet.ambis.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class Constants {
	
	private Constants() {
		throw new IllegalStateException(
				"Utility class " + Constants.class.getName() + " can not be instantiated.");
	}
	
    public static final List<String> primaryPlanTypeList = Arrays.asList("10", "11", "14", "1D", "1V");
    public static final List<String> medicalPlanTypeList = Arrays.asList("10");
    public static final List<String> dentalPlanTypeList = Arrays.asList("11", "1D");
    public static final List<String> visionPlanTypeList = Arrays.asList("14", "1V");
    public static final List<String> voluntaryPlanTypeList = Arrays.asList("1D", "1V");
    public static final List<String> additionalPlanTypeList = Arrays.asList("20", "23", "30", "31", "A3", "67");
    public static final List<String> planTypes = Arrays.asList("10", "11", "14", "1D", "1V", "23", "30", "31", "A3");
    
    

    public static final String DEFAULT_COMPANY_CODE = "DEFAULT";
    public static final String K1VALUE = "Y";

    public static final String TEXAS_SITUS_VALUE = "DFTX";
    public static final String TEXAS_SITUS_STATE = "TX";


    public static final String MEDICAL_CODE = "10";
    public static final String DP_MEDICAL_CODE = "15";
    public static final String DENTAL_CODE = "11";
    public static final String VISION_CODE = "14";
    
    public static final String COVERAGE_ALL = "all";
    public static final String EMPLOYEE = "employee";
   
    public static final String VOLUNTARY_VISION_CODE = "1V";
    public static final String VOLUNTARY_DENTAL_CODE = "1D";

    public static final String COMMUTER_CODE = "A3";
    public static final String LIFE_CODE = "23";
    public static final String STD_CODE = "30";
    public static final String LTD_CODE = "31";

    public static final BigDecimal ALEAMOUNTHIGH = new BigDecimal("95.63");
    public static final BigDecimal ALEAMOUNTLOW = BigDecimal.ZERO;
    
    public static final List<String> premiumPlanTypeList = Arrays.asList("000SR3", "000SRE", "001EKX", "001ELD");

    public static final String MA_CODE = "MA";
    
    //Passport medical vendor
    public static final String AETNA = "AETNA";
    public static final String BCBSFL = "BCBSFL";
    public static final String BCBSNC = "BCBSNC";
    public static final String BCBSMN = "BCBSMN";
    
    public static final String BLUE = "BLUE";
    public static final String GHWHMO = "GHWHMO";
    public static final String KAISER = "KAISER";
    public static final String KAISHI = "KAISHI";
    public static final String KAISNW = "KAISNW";
    public static final String KAIVMD = "KAIVMD";
    public static final String TFHP = "TFHP";
    public static final String TUFTS = "TUFTS";
    public static final String UHC = "UHC";
    public static final String BCID = "BCID";
    
    public static final String AETNAAM = "AETNAAM";
	public static final String AETNAACD = "AETNAACD";
    public static final String AETNASOI = "AETNASOI";
    public static final String KAISERSOI = "KAISERSOI";
    public static final String KAISCOSOI = "KAISCOSOI";
    public static final String KAISHISOI = "KAISHISOI";
    public static final String KAISGASOI = "KAISGASOI";
    public static final String FLBLUESOI = "FLBLUESOI";
    public static final String TUFTSACD = "TUFTSACD";
	public static final String TUFTSSOI = "TUFTSSOI";
	public static final String EMPIRENY = "EMPIRENY";

	public static final String PPO = "PPO";
	public static final String HMO = "HMO";
	
	public static final List<String> KAISER_NW_EXCLUSIONS = Arrays.asList(
			/* PAS Q1 */
			"000NIN","000NIO","000NIR","000NIS","000NIT","000NIU","000NIX","000NIY","0052YI","0052YJ",
			"0052YK","0052YM","0052YN","0052YO","0052YP","0052YR",
			/* Q3..Q4 */
			"000PZG","000PZF","002C4K","000PZK","000PZL","000PZH","000PZA","000PZB","000PZE","002C4J",
			"000EGZ","001RN3","004S42","000EH9","004S44","000M57","004S43","001RN2","000EHE","000EH5",
			"004S40","004S45","004S3X","004S3Z","004S46","004S3Y","000EHA","004S41","000EH4","000M56");
	
    public static final String BSCASOI = "BSCASOI";
    public static final String GUARDSOI = "GUARDSOI";
    public static final String UHCAM = "UHCAM";
    public static final String UHCAMPA = "UHCAMPA";
    public static final String UHCAMPB = "UHCAMPB";
    public static final String KAISERAM = "KAISERAM";
    
    public static final String TOP_QUALITY_NAME = "Premier";
    public static final String TOP_QUALITY_ID = "PRM";

    public static final String BALANCED_PACKAGE_NAME = "Intermediate";
    public static final String BALANCED_ID = "INT";

    public static final String CONSERVATIVE_PACKAGE_NAME = "Conservative";
    public static final String CONSERVATIVE_ID = "CON";

    public static final String DATE_FORMAT = "dd-MMM-yyyy";
    public static final String DATE_FORMAT_EMAIL = "MMMM dd, yyyy";
    
    public static final String MEDICAL = "medical";
    public static final String VISION = "vision";
    public static final String DENTAL = "dental";
    public static final String ADDITIONAL = "additionalBenefit";
    public static final String LTD = "ltd";
    public static final String STD = "std";
    public static final String LIFE = "life";
    public static final String DISABILITY = "DISABILITY";
    public static final String CMTR = "CMTR";
    
    public static final int COMMUTER_ELIGIBLE_EMPLOYEES = 20;

    public static final List<String> primaryPlanTypesCodes = Arrays.asList("10", "11", "14");
    
	public static final String TRUE = "T";
	
	public static final String YES = "Y";
	
	public static final String NO = "N";

    public static final String LIFE_000SRO = "000SRO";
    public static final String LIFE_000TM9 = "000TM9";
    public static final String LIFE_000TMA = "000TMA";
    public static final String LIFE_000TMB = "000TMB";

    public static final List<String> PKG_TYPES = Arrays.asList("CON", "INT", "PRM");
    
    public static final String FIRST_NAME = "FIRST_NAME";
    public static final String EMAIL_ADDRESS = "EMAIL_ADDRESS";
    
    public static final BigDecimal BigDecimal_100 = new BigDecimal(100L);
    public static final BigDecimal BigDecimal_50 = new BigDecimal(50L);

	public static final String AMBROSE_PEO_ID = "AMB";
	public static final String X11_PEO_ID = "ALP";
	public static final String SOI_PEO_ID = "SOI";
	public static final String PASSPORT_PEO_ID = "PAS";	
	public static final String PASSPORT_BEN_EXCHANGE = "TriNet III";

	public static final String MAX_BENEFIIT_BASE = "9999999999";
	
	public static final String ALL_STATES = "All";
	public static final String CVG_CODE_EMPLOYEE = "1";
	public static final String STANDARD_BG = "STD";
	public static final int NUMBER_ONE = 1;
	public static final List<String> LIFE_CMTR_PLANS = Arrays.asList("23", "A3");
	public static final List<String> LIFE_CMTR_PLANS_DESC = Arrays.asList("LIFE", "CMTR");
	
	public static final String SEARCH_CLIENT_TYPE_ALL = "All";
	public static final String SEARCH_CLIENT_TYPE_NEW = "New";
	public static final String SEARCH_CLIENT_TYPE_RENEWAL = "Renewal";
	
	public static final String QUERY_FOR_ALL_CLIENTS = "getResultsForAllCT";
	public static final String QUERY_FOR_NEW_CLIENTS = "getResultsForNewCT";
	public static final String QUERY_FOR_RENEWAL_CLIENTS = "getResultsForRenewalCT";
	
	public static final List<String> COVERAGE_CODES = Arrays.asList("1","2","C", "4");
	public static final String ACTIVE_STATUS = "A";
	
	public static final List<String> DISABILITY_BUNDLED_EXCHANGES = Arrays.asList("TriNet XI", "TriNet II","TriNet III","TriNet IV");
	public static final String STD_AND_LTD = "Short & Long Term Disability Plan Options";
	
}
