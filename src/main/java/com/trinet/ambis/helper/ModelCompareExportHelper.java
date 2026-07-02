/**
 * 
 */
package com.trinet.ambis.helper;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.trinet.ambis.common.BSSExportConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlanRateData;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.EmployeeStrategyPlanData;
import com.trinet.ambis.service.model.ModelComparePlanTypeCost;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;

/**
 * @author hliddle
 *
 */
public class ModelCompareExportHelper {
	
	private static final String ERROR_CHECK_PREFIX = "=0,0,IF(ISERROR(";

	private static int firstBenefitGroupColumn;
	private static int firstPlanNameColumn;
	private static int firstMonthlyWseCostColumn;
	private static int firstMonthlyWseDollarDifferenceColumn;
	private static int firstMonthlyWsePercentDifferenceColumn;
	private static int firstMonthlyCompanyCostColumn;
	private static int firstMonthlyCompanyDollarDifferenceColumn;
	private static int firstMonthlyCompanyPercentDifferenceColumn;
	private static int firstMonthlyTotalCostColumn;
	private static int firstMonthlyTotalDollarDifferenceColumn;
	private static int firstMonthlyTotalPercentDifferenceColumn;

	private static CellStyle styleHeaderFormat;
	private static CellStyle styleHeaderRightFormat;
	private static CellStyle styleCurrencyFormat;
	private static CellStyle stylePercentageFormat;
	private static CellStyle styleDisclaimerHeaderFormat;
	private static CellStyle styleDisclaimerFormat;
	private static CellStyle styleBorderFormat;
	private static CellStyle styleBorderBoldFormat;
	private static CellStyle styleCurrencyBorderFormat;
	private static CellStyle styleBorderRightFormat;
	private static CellStyle confDisclaimerCellStyle;

	private ModelCompareExportHelper() {
		throw new IllegalStateException(
				"Utility class " + ModelCompareExportHelper.class.getName() + " can not be instantiated.");
	}

	/**
	 * Adds a sheet to the passed in Excel workbook that contains the company
	 * cost comparison data for the passed in strategies using the passed in
	 * client strategy cost data.
	 * 
	 * @param company
	 * @param strategyMap
	 * @param mcStrategyCostList
	 * @param workbook
	 */
	public static void constructCompanyStrategyWorkbook(Company company, Map<Long, String> strategyMap,
			List<ModelCompareStrategyCost> mcStrategyCostList, Workbook workbook) {

		createStyles(workbook);
		workbook.createSheet(BSSExportConstants.COMPANY_SHEET_NAME);
		constructCompanyHeader(company, strategyMap, workbook);
		constuctCompanyRows(strategyMap, mcStrategyCostList, workbook);
		populateCompanyFormulas(workbook, strategyMap.size());
		constuctCompanyDisclaimer(workbook);
		setColumnWidths(workbook.getSheet(BSSExportConstants.COMPANY_SHEET_NAME));
		evaluateFormulas(workbook);
	}

	/**
	 * Adds a sheet to the passed in Excel workbook that contains the employee
	 * cost comparison data for the passed in strategies using the passed in
	 * employee strategy data.
	 * 
	 * @param strategyMap
	 * @param employeeStrategyDataList
	 * @param workbook
	 */
	public static void constructEmployeeStrategiesPlanCostWorkbook(Map<Long, String> strategyMap,
			List<EmployeeStrategyData> employeeStrategyDataList, Workbook workbook) {

		calcuateEmployeeColumnValues(strategyMap.size());
		createStyles(workbook);

		Sheet sheet = workbook.createSheet(BSSExportConstants.EMPLOYEE_SHEET_NAME);
		constructConfDisclaimer(sheet, BSSExportConstants.EMPLOYEE_DISCLAIMER_START_COLUMN, BSSExportConstants.EMPLOYEE_DISCLAIMER_END_COLUMN, BSSExportConstants.EMPLOYEE_DISCLAIMER_ROW);
		constructEmployeeHeader(strategyMap, workbook);
		setColumnWidths(workbook.getSheet(BSSExportConstants.EMPLOYEE_SHEET_NAME));
		int rowNumber = BSSExportConstants.EMPLOYEE_DATA_FIRST_ROW;

		for (EmployeeStrategyData employeeStrategyData : employeeStrategyDataList) {

			int i = 0;

			// Medical Row
			Row rowMedical = sheet.createRow(rowNumber++);
			constuctEmployeeRows(employeeStrategyData, i++, rowMedical);

			// Dental Row
			Row rowDental = sheet.createRow(rowNumber++);
			constuctEmployeeRows(employeeStrategyData, i++, rowDental);

			// Vision Row
			Row rowVision = sheet.createRow(rowNumber++);
			constuctEmployeeRows(employeeStrategyData, i, rowVision);

		}

		populateEmployeeFormulas(workbook, strategyMap.size() - 1);
		evaluateFormulas(workbook);
	}

	/**
	 * Creates the common fonts and styles used in the spreadsheets.
	 * 
	 * @param workbook
	 */
	private static void createStyles(Workbook workbook) {

		Font font = workbook.createFont();
		font.setBold(true);

		styleHeaderFormat = workbook.createCellStyle();
		styleHeaderFormat.setWrapText(true);
		styleHeaderFormat.setFont(font);

		styleHeaderRightFormat = workbook.createCellStyle();
		styleHeaderRightFormat.setWrapText(true);
		styleHeaderRightFormat.setFont(font);
		styleHeaderRightFormat.setAlignment(HorizontalAlignment.RIGHT);

		styleCurrencyFormat = workbook.createCellStyle();
		styleCurrencyFormat.setDataFormat((short) 7);

		stylePercentageFormat = workbook.createCellStyle();
		stylePercentageFormat.setDataFormat((short) 0xa);

		styleDisclaimerHeaderFormat = workbook.createCellStyle();
		styleDisclaimerHeaderFormat.setFont(font);
		styleDisclaimerHeaderFormat.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		styleDisclaimerHeaderFormat.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		styleDisclaimerFormat = workbook.createCellStyle();
		styleDisclaimerFormat.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		styleDisclaimerFormat.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleDisclaimerFormat.setWrapText(true);

		styleBorderFormat = workbook.createCellStyle();
		styleBorderFormat.setBorderBottom(BorderStyle.THIN);
		styleBorderFormat.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderFormat.setBorderLeft(BorderStyle.THIN);
		styleBorderFormat.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderFormat.setBorderRight(BorderStyle.THIN);
		styleBorderFormat.setRightBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderFormat.setBorderTop(BorderStyle.THIN);
		styleBorderFormat.setTopBorderColor(IndexedColors.BLACK.getIndex());

		styleCurrencyBorderFormat = workbook.createCellStyle();
		styleCurrencyBorderFormat.setDataFormat((short) 7);
		styleCurrencyBorderFormat.setBorderBottom(BorderStyle.THIN);
		styleCurrencyBorderFormat.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styleCurrencyBorderFormat.setBorderLeft(BorderStyle.THIN);
		styleCurrencyBorderFormat.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		styleCurrencyBorderFormat.setBorderRight(BorderStyle.THIN);
		styleCurrencyBorderFormat.setRightBorderColor(IndexedColors.BLACK.getIndex());
		styleCurrencyBorderFormat.setBorderTop(BorderStyle.THIN);
		styleCurrencyBorderFormat.setTopBorderColor(IndexedColors.BLACK.getIndex());

		styleBorderRightFormat = workbook.createCellStyle();
		styleBorderRightFormat.setBorderBottom(BorderStyle.THIN);
		styleBorderRightFormat.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderRightFormat.setBorderLeft(BorderStyle.THIN);
		styleBorderRightFormat.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderRightFormat.setBorderRight(BorderStyle.THIN);
		styleBorderRightFormat.setRightBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderRightFormat.setBorderTop(BorderStyle.THIN);
		styleBorderRightFormat.setTopBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderRightFormat.setAlignment(HorizontalAlignment.RIGHT);

		styleBorderBoldFormat = workbook.createCellStyle();
		styleBorderBoldFormat.setBorderBottom(BorderStyle.THIN);
		styleBorderBoldFormat.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderBoldFormat.setBorderLeft(BorderStyle.THIN);
		styleBorderBoldFormat.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderBoldFormat.setBorderRight(BorderStyle.THIN);
		styleBorderBoldFormat.setRightBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderBoldFormat.setBorderTop(BorderStyle.THIN);
		styleBorderBoldFormat.setTopBorderColor(IndexedColors.BLACK.getIndex());
		styleBorderBoldFormat.setFont(font);
		
		confDisclaimerCellStyle = workbook.createCellStyle();
		confDisclaimerCellStyle.setBorderBottom(BorderStyle.THIN);
		confDisclaimerCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		confDisclaimerCellStyle.setBorderLeft(BorderStyle.THIN);
		confDisclaimerCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		confDisclaimerCellStyle.setBorderRight(BorderStyle.THIN);
		confDisclaimerCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
		confDisclaimerCellStyle.setBorderTop(BorderStyle.THIN);
		confDisclaimerCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		confDisclaimerCellStyle.setAlignment(HorizontalAlignment.CENTER);
		confDisclaimerCellStyle.setFont(font);

	}

	/**
	 * Updates the company sheet to add the header row.
	 * 
	 * @param company
	 * @param strategyMap
	 * @param workbook
	 */
	private static void constructCompanyHeader(Company company, Map<Long, String> strategyMap, Workbook workbook) {

		int rowNum = BSSExportConstants.COMPANY_SHEET_HEADER_ROW;
		Row row;
		Sheet sheet = workbook.getSheet(BSSExportConstants.COMPANY_SHEET_NAME);
		row = sheet.createRow(rowNum++);
		row.createCell(0).setCellValue(company.getDescription() + BSSExportConstants.COMPANY_SHEET_HEADER_SUFFIX);
		row.getCell(0).setCellStyle(styleHeaderFormat);

		sheet.createRow(rowNum++).createCell(0, CellType.BLANK);

		row = sheet.createRow(rowNum);
		row.createCell(0, CellType.BLANK);

		int s = 1;
		for (String strategyName : strategyMap.values()) {
			row.createCell(s).setCellValue(strategyName);
			row.getCell(s).setCellStyle(styleBorderRightFormat);
			s++;
		}
	}

	/**
	 * Updates the company sheet to add the company cost rows.
	 * 
	 * @param strategyMap
	 * @param mcStrategyCostList
	 * @param workbook
	 */
	private static void constuctCompanyRows(Map<Long, String> strategyMap,
			List<ModelCompareStrategyCost> mcStrategyCostList, Workbook workbook) {

		int rowNumber;
		int strategyCount = 0;
		Sheet sheet = workbook.getSheet(BSSExportConstants.COMPANY_SHEET_NAME);

		for (Long strategyId : strategyMap.keySet()) {
			for (ModelCompareStrategyCost mcStrategyCost : mcStrategyCostList) {

				if (strategyId == mcStrategyCost.getStrategyId()) {
					rowNumber = 3;
					for (ModelComparePlanTypeCost planTypeCost : mcStrategyCost.getPlanTypeCosts()) {
						createOrUpdateRow(rowNumber, strategyCount, sheet, planTypeCost);
						rowNumber++;
					}
					strategyCount++;
				}
			}
		}
	}

	private static void createOrUpdateRow(int rowNumber, int strategyCount, Sheet sheet,
			ModelComparePlanTypeCost planTypeCost) {
		Row row;
		if (strategyCount == 0) {
			row = sheet.createRow(rowNumber);
			row.createCell(0).setCellValue(planTypeCost.getPlanTypeDisplayName());
			row.getCell(0).setCellStyle(styleBorderFormat);
		} else {
			row = sheet.getRow(rowNumber);
		}
		row.createCell(strategyCount + 1).setCellValue(planTypeCost.getCost().doubleValue());
		row.getCell(strategyCount + 1).setCellStyle(styleCurrencyBorderFormat);
	}

	/**
	 * Updates the company sheet to add the total row with Excel formulas.
	 * 
	 * @param workbook
	 * @param strategyCount
	 */
	private static void populateCompanyFormulas(Workbook workbook, int strategyCount) {

		Sheet sheet = workbook.getSheet(BSSExportConstants.COMPANY_SHEET_NAME);
		int rowNumber = sheet.getLastRowNum() + 1;
		CellReference firstRowCellReference;
		CellReference lastRowCellReference;

		Row totalRow = sheet.createRow(rowNumber);
		totalRow.createCell(0).setCellValue("Total Monthly Cost (All Worksite Employees)");
		totalRow.getCell(0).setCellStyle(styleBorderBoldFormat);

		// Total Costs
		for (int i = 1; i < strategyCount + 1; i++) {
			firstRowCellReference = new CellReference(3, i);
			lastRowCellReference = new CellReference(rowNumber - 1, i);

			totalRow.createCell(i).setCellFormula("SUM(" + firstRowCellReference.formatAsString() + ":"
					+ lastRowCellReference.formatAsString() + ")");
			totalRow.getCell(i).setCellStyle(styleCurrencyBorderFormat);
		}
	}
	
	/**
	 * Updates the company and employee sheet for the passed in workbook to add the Confidential Header
	 * row.
	 * 
	 * @param workbook
	 * @param cellRangeStart
	 * @param cellRangeEnd
	 * @param sheetName
	 * @param rowNumber
	 */
	private static void constructConfDisclaimer(Sheet sheet, int cellRangeStart, int cellRangeEnd, int rowNumber) {
		Row confDisclaimerRow = sheet.createRow(rowNumber);
		Cell confDislaimerCell = null;
		
		for (int i = 0; i <= cellRangeEnd; ++i) {
			confDislaimerCell = confDisclaimerRow.createCell(i);
			confDislaimerCell.setCellStyle(confDisclaimerCellStyle);
		}
		confDisclaimerRow.getCell(0).setCellValue(BSSExportConstants.COMPANY_DISCLAIMER_TEXT_5);
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, cellRangeStart, cellRangeEnd));
	}

	/**
	 * Updates the company sheet to add the disclaimer rows.
	 * 
	 * @param workbook
	 */
	private static void constuctCompanyDisclaimer(Workbook workbook) {


		Sheet sheet = workbook.getSheet(BSSExportConstants.COMPANY_SHEET_NAME);
		int rowNumber = sheet.getLastRowNum() + 2;
		Row disclaimerRow = null;
		Cell dislaimerCell = null;

		dislaimerCell = sheet.createRow(rowNumber).createCell(0);
		dislaimerCell.setCellValue(BSSExportConstants.COMPANY_DISCLAIMER_TEXT_1);
		dislaimerCell.setCellStyle(styleDisclaimerHeaderFormat);
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, BSSExportConstants.COMPANY_DISCLAIMER_START_COLUMN, BSSExportConstants.COMPANY_DISCLAIMER_END_COLUMN));

		rowNumber++;

		disclaimerRow = sheet.createRow(rowNumber);
		dislaimerCell = disclaimerRow.createCell(0, CellType.BLANK);
		dislaimerCell.setCellStyle(styleDisclaimerFormat);
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, BSSExportConstants.COMPANY_DISCLAIMER_START_COLUMN, BSSExportConstants.COMPANY_DISCLAIMER_END_COLUMN));

		rowNumber++;

		disclaimerRow = sheet.createRow(rowNumber);
		dislaimerCell = disclaimerRow.createCell(0);
		dislaimerCell.setCellValue(BSSExportConstants.COMPANY_DISCLAIMER_TEXT_2);
		dislaimerCell.setCellStyle(styleDisclaimerFormat);
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, BSSExportConstants.COMPANY_DISCLAIMER_START_COLUMN, BSSExportConstants.COMPANY_DISCLAIMER_END_COLUMN));

		rowNumber++;

		disclaimerRow = sheet.createRow(rowNumber);
		dislaimerCell = disclaimerRow.createCell(0, CellType.BLANK);
		dislaimerCell.setCellStyle(styleDisclaimerFormat);
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, BSSExportConstants.COMPANY_DISCLAIMER_START_COLUMN, BSSExportConstants.COMPANY_DISCLAIMER_END_COLUMN));

		rowNumber++;

		disclaimerRow = sheet.createRow(rowNumber);
		dislaimerCell = disclaimerRow.createCell(0);
		dislaimerCell.setCellValue(BSSExportConstants.COMPANY_DISCLAIMER_TEXT_3);
		dislaimerCell.setCellStyle(styleDisclaimerFormat);
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, BSSExportConstants.COMPANY_DISCLAIMER_START_COLUMN, BSSExportConstants.COMPANY_DISCLAIMER_END_COLUMN));

		rowNumber++;

		disclaimerRow = sheet.createRow(rowNumber);
		dislaimerCell = disclaimerRow.createCell(0);
		dislaimerCell.setCellValue(BSSExportConstants.COMPANY_DISCLAIMER_TEXT_4);
		dislaimerCell.setCellStyle(styleDisclaimerFormat);
		disclaimerRow.setHeightInPoints((2*sheet.getDefaultRowHeightInPoints()));  
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, BSSExportConstants.COMPANY_DISCLAIMER_START_COLUMN, BSSExportConstants.COMPANY_DISCLAIMER_END_COLUMN));

		rowNumber++;

		constructConfDisclaimer(sheet, BSSExportConstants.COMPANY_DISCLAIMER_START_COLUMN, BSSExportConstants.COMPANY_DISCLAIMER_END_COLUMN, rowNumber);

	}

	/**
	 * Sets the column number for the columns in the employee spreadsheet based
	 * on the number of strategies that are being compared.
	 * 
	 * @param strategyCount
	 */
	private static void calcuateEmployeeColumnValues(int strategyCount) {

		firstBenefitGroupColumn = BSSExportConstants.COVERAGE_LEVEL_COLUMN + 1;
		firstPlanNameColumn = firstBenefitGroupColumn + strategyCount;
		firstMonthlyWseCostColumn = firstPlanNameColumn + strategyCount;
		firstMonthlyWseDollarDifferenceColumn = firstMonthlyWseCostColumn + strategyCount;
		firstMonthlyWsePercentDifferenceColumn = firstMonthlyWseDollarDifferenceColumn + strategyCount - 1;
		firstMonthlyCompanyCostColumn = firstMonthlyWsePercentDifferenceColumn + strategyCount - 1;
		firstMonthlyCompanyDollarDifferenceColumn = firstMonthlyCompanyCostColumn + strategyCount;
		firstMonthlyCompanyPercentDifferenceColumn = firstMonthlyCompanyDollarDifferenceColumn + strategyCount - 1;
		firstMonthlyTotalCostColumn = firstMonthlyCompanyPercentDifferenceColumn + strategyCount - 1;
		firstMonthlyTotalDollarDifferenceColumn = firstMonthlyTotalCostColumn + strategyCount;
		firstMonthlyTotalPercentDifferenceColumn = firstMonthlyTotalDollarDifferenceColumn + strategyCount - 1;
	}

	/**
	 * Updates the employee sheet for the passed in workbook to add the header
	 * row.
	 * 
	 * @param strategyMap
	 * @param workbook
	 */
	private static void constructEmployeeHeader(Map<Long, String> strategyMap, Workbook workbook) {

		String baseStrategyName = "";
		Sheet sheet = workbook.getSheet(BSSExportConstants.EMPLOYEE_SHEET_NAME);
		Row rowHeader = sheet.createRow(BSSExportConstants.EMPLOYEE_HEADER_ROW);

		rowHeader.createCell(BSSExportConstants.LAST_NAME_COLUMN)
				.setCellValue(BSSExportConstants.LAST_NAME_COLUMN_HEADER);
		rowHeader.createCell(BSSExportConstants.FIRST_NAME_COLUMN)
				.setCellValue(BSSExportConstants.FIRST_NAME_COLUMN_HEADER);
		rowHeader.createCell(BSSExportConstants.FULL_NAME_COLUMN)
				.setCellValue(BSSExportConstants.FULL_NAME_COLUMN_HEADER);
		rowHeader.createCell(BSSExportConstants.EMPLOYEE_ID_COLUMN)
				.setCellValue(BSSExportConstants.EMPLOYEE_ID_COLUMN_HEADER);
		rowHeader.createCell(BSSExportConstants.DEPARTMENT_COLUMN)
				.setCellValue(BSSExportConstants.DEPARTMENT_COLUMN_HEADER);
		rowHeader.createCell(BSSExportConstants.PLAN_TYPE_COLUMN)
				.setCellValue(BSSExportConstants.PLAN_TYPE_COLUMN_HEADER);
		rowHeader.createCell(BSSExportConstants.COVERAGE_LEVEL_COLUMN)
				.setCellValue(BSSExportConstants.COVERAGE_LEVEL_COLUMN_HEADER);

		int s = 0;
		for (String strategyName : strategyMap.values()) {
			rowHeader.createCell(firstBenefitGroupColumn + s)
					.setCellValue(BSSExportConstants.BENEFIT_GROUP_COLUMN_HEADER + strategyName);
			rowHeader.createCell(firstPlanNameColumn + s)
					.setCellValue(BSSExportConstants.PLAN_NAME_COLUMN_HEADER + strategyName);
			rowHeader.createCell(firstMonthlyWseCostColumn + s)
					.setCellValue(BSSExportConstants.MONTHLY_WSE_COST_COLUMN_HEADER + strategyName);
			rowHeader.createCell(firstMonthlyCompanyCostColumn + s)
					.setCellValue(BSSExportConstants.MONTHLY_COMPANY_COST_COLUMN_HEADER + strategyName);
			rowHeader.createCell(firstMonthlyTotalCostColumn + s)
					.setCellValue(BSSExportConstants.MONTHLY_TOTAL_COST_COLUMN_HEADER + strategyName);

			if (s == 0) {
				baseStrategyName = strategyName;
			} else {
				rowHeader.createCell(firstMonthlyWseDollarDifferenceColumn + s - 1)
						.setCellValue(BSSExportConstants.MONTHLY_WSE_DOLLAR_DIFF_COLUMN_HEADER + strategyName
								+ BSSExportConstants.VS_NEW_LINE + baseStrategyName);
				rowHeader.createCell(firstMonthlyWsePercentDifferenceColumn + s - 1)
						.setCellValue(BSSExportConstants.MONTHLY_WSE_PERCENT_DIFF_COLUMN_HEADER + strategyName
								+ BSSExportConstants.VS_NEW_LINE + baseStrategyName);
				rowHeader.createCell(firstMonthlyCompanyDollarDifferenceColumn + s - 1)
						.setCellValue(BSSExportConstants.MONTHLY_COMPANY_DOLLAR_DIFF_COLUMN_HEADER + strategyName
								+ BSSExportConstants.VS_NEW_LINE + baseStrategyName);
				rowHeader.createCell(firstMonthlyCompanyPercentDifferenceColumn + s - 1)
						.setCellValue(BSSExportConstants.MONTHLY_COMPANY_PERCENT_DIFF_COLUMN_HEADER + strategyName
								+ BSSExportConstants.VS_NEW_LINE + baseStrategyName);
				rowHeader.createCell(firstMonthlyTotalDollarDifferenceColumn + s - 1)
						.setCellValue(BSSExportConstants.MONTHLY_TOTAL_DOLLAR_DIFF_COLUMN_HEADER + strategyName
								+ BSSExportConstants.VS_NEW_LINE + baseStrategyName);
				rowHeader.createCell(firstMonthlyTotalPercentDifferenceColumn + s - 1)
						.setCellValue(BSSExportConstants.MONTHLY_TOTAL_PERCENT_DIFF_COLUMN_HEADER + strategyName
								+ BSSExportConstants.VS_NEW_LINE + baseStrategyName);
			}
			s++;
		}

		Iterator<Cell> cellIterator = rowHeader.cellIterator();
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if (cell.getStringCellValue().contains("Monthly")) {
				cell.setCellStyle(styleHeaderRightFormat);
			} else {
				cell.setCellStyle(styleHeaderFormat);

			}
		}
	}

	/**
	 * Updates the employees rows to add the employee base data.
	 * 
	 * @param employeeStrategyData
	 * @param typeInt
	 * @param row
	 */
	private static void constuctEmployeeRows(EmployeeStrategyData employeeStrategyData, int typeInt, Row row) {

		String planType = "";
		if (typeInt == 0) {
			planType = "Medical";
		} else if (typeInt == 1) {
			planType = "Dental";
		} else if (typeInt == 2) {
			planType = "Vision";
		}

		row.createCell(BSSExportConstants.LAST_NAME_COLUMN).setCellValue(employeeStrategyData.getEmplLastName());
		row.createCell(BSSExportConstants.FIRST_NAME_COLUMN).setCellValue(employeeStrategyData.getEmplFirstName());
		row.createCell(BSSExportConstants.FULL_NAME_COLUMN).setCellValue(employeeStrategyData.getEmplFullName());
		row.createCell(BSSExportConstants.EMPLOYEE_ID_COLUMN).setCellValue(employeeStrategyData.getEmplId());
		row.createCell(BSSExportConstants.DEPARTMENT_COLUMN).setCellValue(employeeStrategyData.getDeptName());
		row.createCell(BSSExportConstants.PLAN_TYPE_COLUMN).setCellValue(planType);

		constuctEmployeePlanRows(employeeStrategyData, typeInt, row);

	}

	/**
	 * Updates the employees rows to add the cost data.
	 * 
	 * @param employeeStrategyData
	 * @param typeInt
	 * @param row
	 */
	private static void constuctEmployeePlanRows(EmployeeStrategyData employeeStrategyData, int typeInt, Row row) {

		int i = 0;

		for (EmployeeStrategyPlanData employeeStrategyDetails : employeeStrategyData.getStrategyDetails()) {

			int cellNumber = firstBenefitGroupColumn + i;

			if (typeInt < employeeStrategyDetails.getBenefitPlans().size()) {
				BenefitPlanRateData employeePlan = employeeStrategyDetails.getBenefitPlans().get(typeInt);

				if (i == 0) {
					setCoverageLevelCellValue(row, employeePlan);
				}

				setCellValue(row, cellNumber, employeeStrategyDetails.getGroupName());
				setCellValue(row, firstPlanNameColumn + i, employeePlan.getPlanName());

				setCurrencyCellValue(row, firstMonthlyWseCostColumn + i, employeePlan.getCoverageElect(), employeePlan.getEmployeeContribution()) ;
				setCurrencyCellValue(row, firstMonthlyCompanyCostColumn + i, employeePlan.getCoverageElect(), employeePlan.getEmployerContribution()) ;

				i++;
			}
		}

	}

	private static void setCoverageLevelCellValue(Row row, BenefitPlanRateData employeePlan) {
		String coverageLevel = "-";
		if (!"N".equals(employeePlan.getCoverageElect())) {
			coverageLevel = employeePlan.getCoverageLevelName() == null ? "Waived"
					: employeePlan.getCoverageLevelName();
		}
		row.createCell(BSSExportConstants.COVERAGE_LEVEL_COLUMN)
				.setCellValue(coverageLevel);
	}

	private static void setCellValue(Row row, int cellNumber, String value ) {
		row.createCell(cellNumber).setCellValue(value);
	}
	
	private static void setCurrencyCellValue(Row row, int cellNumber, String coverageElect, BigDecimal contribution) {
	    if ("N".equals(coverageElect) || null == contribution) {
	        row.createCell(cellNumber, CellType.BLANK);
	    } else {
	        row.createCell(cellNumber).setCellValue(contribution.doubleValue());
	    }
	    row.getCell(cellNumber).setCellStyle(styleCurrencyFormat);
	}

	/**
	 * Updates the employee sheet for the passed in workbook to add all cells
	 * that are based on Excel formulas.
	 * 
	 * @param workbook
	 * @param futureStrategyCount
	 */
	private static void populateEmployeeFormulas(Workbook workbook, int futureStrategyCount) {

		String differenceString;
		CellReference currentCostCell;
		CellReference futureCostCell;
		Sheet sheet = workbook.getSheet(BSSExportConstants.EMPLOYEE_SHEET_NAME);

		for (Row row : sheet) {
			if (row.getRowNum() >= BSSExportConstants.EMPLOYEE_DATA_FIRST_ROW) {

				// Total Costs
				for (int i = 0; i < futureStrategyCount + 1; i++) {
					currentCostCell = new CellReference(row.getRowNum(), firstMonthlyWseCostColumn + i);
					futureCostCell = new CellReference(row.getRowNum(), firstMonthlyCompanyCostColumn + i);
					row.createCell(firstMonthlyTotalCostColumn + i)
							.setCellFormula(currentCostCell.formatAsString() + "+" + futureCostCell.formatAsString());
					row.getCell(firstMonthlyTotalCostColumn + i).setCellStyle(styleCurrencyFormat);

				}

				for (int i = 0; i < futureStrategyCount; i++) {

					// WSE Cost Differences
					currentCostCell = new CellReference(row.getRowNum(), firstMonthlyWseCostColumn);
					futureCostCell = new CellReference(row.getRowNum(), firstMonthlyWseCostColumn + i + 1);

					differenceString = "(" + futureCostCell.formatAsString() + "-" + currentCostCell.formatAsString()
							+ ")";

					row.createCell(firstMonthlyWseDollarDifferenceColumn + i)
							.setCellFormula(differenceString);
					row.getCell(firstMonthlyWseDollarDifferenceColumn + i).setCellStyle(styleCurrencyFormat);
			
					row.createCell(firstMonthlyWsePercentDifferenceColumn + i)
							.setCellFormula("IF(" + differenceString + ERROR_CHECK_PREFIX + differenceString + "/"
									+ currentCostCell.formatAsString() + "),1," + differenceString + "/"
									+ currentCostCell.formatAsString() + "))");
					row.getCell(firstMonthlyWsePercentDifferenceColumn + i).setCellStyle(stylePercentageFormat);

					// Company Cost Differences
					currentCostCell = new CellReference(row.getRowNum(), firstMonthlyCompanyCostColumn);
					futureCostCell = new CellReference(row.getRowNum(), firstMonthlyCompanyCostColumn + i + 1);

					differenceString = "(" + futureCostCell.formatAsString() + "-" + currentCostCell.formatAsString()
							+ ")";

					row.createCell(firstMonthlyCompanyDollarDifferenceColumn + i).setCellFormula(differenceString);
					row.getCell(firstMonthlyCompanyDollarDifferenceColumn + i).setCellStyle(styleCurrencyFormat);

					row.createCell(firstMonthlyCompanyPercentDifferenceColumn + i)
							.setCellFormula("IF(" + differenceString + ERROR_CHECK_PREFIX + differenceString + "/"
									+ currentCostCell.formatAsString() + "),1," + differenceString + "/"
									+ currentCostCell.formatAsString() + "))");
					row.getCell(firstMonthlyCompanyPercentDifferenceColumn + i).setCellStyle(stylePercentageFormat);

					// Total Cost Differences
					currentCostCell = new CellReference(row.getRowNum(), firstMonthlyTotalCostColumn);
					futureCostCell = new CellReference(row.getRowNum(), firstMonthlyTotalCostColumn + i + 1);

					differenceString = "(" + futureCostCell.formatAsString() + "-" + currentCostCell.formatAsString()
							+ ")";

					row.createCell(firstMonthlyTotalDollarDifferenceColumn + i).setCellFormula(differenceString);
					row.getCell(firstMonthlyTotalDollarDifferenceColumn + i).setCellStyle(styleCurrencyFormat);

					row.createCell(firstMonthlyTotalPercentDifferenceColumn + i)
							.setCellFormula("IF(" + differenceString + ERROR_CHECK_PREFIX + differenceString + "/"
									+ currentCostCell.formatAsString() + "),1," + differenceString + "/"
									+ currentCostCell.formatAsString() + "))");
					row.getCell(firstMonthlyTotalPercentDifferenceColumn + i).setCellStyle(stylePercentageFormat);
				}
			}
		}
	}

	/**
	 * Loops through all columns in the passed in worksheet and sets the column
	 * width based on the data in the cells.
	 * 
	 * @param sheet
	 */
	private static void setColumnWidths(Sheet sheet) {

		for (Row row : sheet) {
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				int columnIndex = cell.getColumnIndex();
				sheet.autoSizeColumn(columnIndex);
			}
		}
	}

	private static void evaluateFormulas(Workbook workbook) {
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		evaluator.evaluateAll();
	}
	
}
