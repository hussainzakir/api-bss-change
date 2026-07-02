/**
 * 
 */
package com.trinet.ambis.common;

/**
 * @author hliddle
 *
 */
public class BSSExportConstants {

	private BSSExportConstants() {
		throw new IllegalStateException(
				"Constants class " + BSSExportConstants.class.getName() + " can not be instantiated.");
	}

	public static final String COMPANY_SHEET_NAME = "CompanyCompare";
	public static final String EMPLOYEE_SHEET_NAME = "EmployeeCompare";

	public static final int COMPANY_SHEET_HEADER_ROW = 0;
	public static final String COMPANY_SHEET_HEADER_SUFFIX = " - Benefit Strategy Comparison";
	public static final String COMPANY_DISCLAIMER_TEXT_1 = "Company Cost Calculation Assumptions:";
	public static final String COMPANY_DISCLAIMER_TEXT_2 = "Please note that we’re basing these cost estimates on the following assumptions:";
	public static final String COMPANY_DISCLAIMER_TEXT_3 = "1.  The amounts shown are estimates based on your worksite employees' actual enrollment elections and may change.";
	public static final String COMPANY_DISCLAIMER_TEXT_4 = "2. If you do not currently have group dental or group vision and add these plans, we use your current dental and vision enrollments as a baseline for cost calculations.  Costs will vary depending on your worksite employees' actual elections.";
	public static final String COMPANY_DISCLAIMER_TEXT_5 = "Company Confidential";
	public static final int COMPANY_DISCLAIMER_START_COLUMN = 0;
	public static final int COMPANY_DISCLAIMER_END_COLUMN = 2;

	
	public static final int EMPLOYEE_DISCLAIMER_ROW = 0;
	public static final int EMPLOYEE_HEADER_ROW = EMPLOYEE_DISCLAIMER_ROW + 1;
	public static final int EMPLOYEE_DATA_FIRST_ROW = EMPLOYEE_HEADER_ROW + 1;
	public static final int EMPLOYEE_DISCLAIMER_START_COLUMN = 0;
	public static final int EMPLOYEE_DISCLAIMER_END_COLUMN = 8;

	public static final int LAST_NAME_COLUMN = 0;
	public static final int FIRST_NAME_COLUMN = 1;
	public static final int FULL_NAME_COLUMN = 2;
	public static final int EMPLOYEE_ID_COLUMN = 3;
	public static final int DEPARTMENT_COLUMN = 4;
	public static final int PLAN_TYPE_COLUMN = 5;
	public static final int COVERAGE_LEVEL_COLUMN = 6;

	public static final String LAST_NAME_COLUMN_HEADER = "Last Name";
	public static final String FIRST_NAME_COLUMN_HEADER = "First Name";
	public static final String FULL_NAME_COLUMN_HEADER = "Full Name";
	public static final String EMPLOYEE_ID_COLUMN_HEADER = "Employee ID";
	public static final String DEPARTMENT_COLUMN_HEADER = "Department";
	public static final String PLAN_TYPE_COLUMN_HEADER = "Plan Type";
	public static final String COVERAGE_LEVEL_COLUMN_HEADER = "Level of Coverage";

	public static final String BENEFIT_GROUP_COLUMN_HEADER = "Benefit Group\n";
	public static final String PLAN_NAME_COLUMN_HEADER = "Plan Name\n";
	public static final String MONTHLY_WSE_COST_COLUMN_HEADER = "Monthly WSE Cost\n";
	public static final String MONTHLY_COMPANY_COST_COLUMN_HEADER = "Monthly Company Cost\n";
	public static final String MONTHLY_TOTAL_COST_COLUMN_HEADER = "Monthly Total Plan Cost\n";
	public static final String MONTHLY_WSE_DOLLAR_DIFF_COLUMN_HEADER = "Monthly WSE Dollar Difference\n";
	public static final String MONTHLY_WSE_PERCENT_DIFF_COLUMN_HEADER = "Monthly WSE Percent Difference\n";
	public static final String MONTHLY_COMPANY_DOLLAR_DIFF_COLUMN_HEADER = "Monthly Company Dollar Difference\n";
	public static final String MONTHLY_COMPANY_PERCENT_DIFF_COLUMN_HEADER = "Monthly Company Percent Difference\n";
	public static final String MONTHLY_TOTAL_DOLLAR_DIFF_COLUMN_HEADER = "Monthly Total Dollar Difference\n";
	public static final String MONTHLY_TOTAL_PERCENT_DIFF_COLUMN_HEADER = "Monthly Total Percent Difference\n";
	public static final String VS_NEW_LINE = " vs\n";

	public static final String COMMUTER_DISPLAY_NAME = "Commuter";
	public static final String DISABILITY_DISPLAY_NAME = "Group Disability";
	public static final String LIFE_DISPLAY_NAME = "Life Insurance/AD&D";
	public static final String WAVIER_ALLOWANCE_DISPLAY_NAME = "Medical Waiver Allowance";
	public static final String BSUPP_DISPLAY_NAME = "Surplus Benefits Supplement";

}
