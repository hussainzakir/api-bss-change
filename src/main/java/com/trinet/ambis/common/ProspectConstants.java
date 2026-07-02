package com.trinet.ambis.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProspectConstants {

	private ProspectConstants() {

	}

	public static final String PROSPECT_K1_GROUP_NAME = "K1";
	public static final String PROSPECT_ID_REQ_PARAM = "prospectId";
	public static final String RATE_TYPE_PARAM = "rateType";
	public static final String INCLUDE_RATES_PARAM = "includeWithRates";
	public static final String PROSPECT_STRATEGY_NAME = "Prospect Current Strategy";
	public static final String PROSPECT_TN_STRATEGY_NAME = "Baseline Strategy";
	public static final String PROSPECT_ID = "prospectId";
	public static final long PROSPECT_STRATEGY_ID = 0;
	public static final String PROSPECT = "prospect";
	public static final String BENEFITS_TYPES_PATH_PARAM = "benefitTypes";
	public static final String EMPLOYEE = "1";
	public static final String EMPLOYEE_PLUS_SPOUSE = "2";
	public static final String EMPLOYEE_PLUS_CHILD = "C";
	public static final String EMPLOYEE_PLUS_FAMILY = "4";
	public static final String EMPLOYEE_FUNDING_DESC = "Employee";
	public static final String EMPLOYEE_SPOUSE_FUNDING_DESC = "Spouse/Domestic Partner";
	public static final String EMPLOYEE_CHILDREN_FUNDING_DESC = "Child(ren)";
	public static final String FAMILY_FUNDING_DESC = "Family";
	public static final String NOT_AVAILABLE = "N/A";
	public static final String NOT_OFFERED = "Not Offered";
	public static final String EMPLOYEE_COST_COMPARISON = "ECC";
	public static final String PLAN_COMPARISON = "PCC";
	public static final String PLAN_APPENDIX = "APX";
	public static final String WAVED_COVERAGE = "W";
	public static final String MEDICAL_PLAN_TYPE = "10";
	public static final String BENEFITS_TYPES_VALUES = "10,11,14,23,30,31";
	public static final List<String> OUTPUTS_PLAN_TYPE_ORDER = Arrays.asList("med", "den", "vis", "lad", "dis");
	public static final String DENTAL_PLAN_TYPE = "11";
	public static final String DENTAL_VOL_PLAN_TYPE = "1D";
	public static final String VISION_PLAN_TYPE = "14";
	public static final String VISION_VOL_PLAN_TYPE = "1V";
	public static final String LIFE_ADD_PLAN_TYPE = "23";
	public static final String STD_PLAN_TYPE = "30";
	public static final String LTD_PLAN_TYPE = "31";

	public static final String MEDICAL_PLAN_TYPE_DESC = "Medical";
	public static final String DENTAL_PLAN_TYPE_DESC = "Dental";
	public static final String VISION_PLAN_TYPE_DESC = "Vision";
	public static final String LIFE_ADD_PLAN_TYPE_DESC = "Life / AD&D";
	public static final String DISABILITY_PLAN_TYPE_DESC = "Disability";
	public static final Map<String, String> orderedBenTypes = Map.of(MEDICAL_PLAN_TYPE_DESC, "1",
			DENTAL_PLAN_TYPE_DESC, "2",
			VISION_PLAN_TYPE_DESC, "3");

	//AD constants
	public static final String PLAN_NAME = "Plan";
	public static final String DIS_PLAN_NAME = "Plans";
	public static final String UNIT_RATE = "Unit Rate";
	public static final String UNIT = "Unit";
	public static final String EST_PYRL_CVG = "Estimated Payroll Coverage";
	public static final String DIS_EST_PYRL_CVG = "Est. Payroll Coverage";
	public static final String MONTHLY_COST = "Monthly Cost";
	public static final String DISABILITY_TYPE = "Type";
	public static final String STATES = "States";
	public static final String EMPLOYEE_COUNT = "EE Count";
	public static final String GROUP_HEADCOUNT = "Group Headcount";
	public static final String DOUBLE_DASH = "--";
	public static final String EE_PAID = "EE Paid";
}