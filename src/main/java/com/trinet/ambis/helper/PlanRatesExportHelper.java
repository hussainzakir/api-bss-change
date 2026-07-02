package com.trinet.ambis.helper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTimeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.model.AdditionalPlanOptionPlanExport;
import com.trinet.ambis.service.model.AdditionalPlanOptionsExport;
import com.trinet.ambis.service.model.HealthPlanRatesExportPlan;
import com.trinet.ambis.service.model.PlanRatesExportData;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;

/**
 * @author jshuali
 * 
 * @see https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/BuiltinFormats.html
 */
public class PlanRatesExportHelper {

	private static final String DASH = " - ";
	private static final String STATES = "States";
	private static final String PER_UNIT = "Unit";
	private static final String FAMILY = "Family";
	private static final String EMPLOYEE_CHILDREN = "Employee + Child(ren)";
	private static final String EMPLOYEE_SPOUSE = "Employee + Spouse";
	private static final String CURRENT = "Current";
	private static final String FUTURE = "Future";
	private static final String HEAD_COUNT = "Head Count";
	private static final String HEAD_COUNT_CURRENT = CURRENT + " " + HEAD_COUNT;
	private static final String HEAD_COUNT_FUTURE = FUTURE + " " + HEAD_COUNT;
	private static final String PERCENT_DIFFERENCE = "Percent Difference";
	private static final String RATE_DIFFERENCE = "Rate Difference";
	private static final String EMPLOYEE_ONLY = "Employee Only";
	private static final String PLAN_NAME = "Plan Name";
	private static final String DISABILITY = "DISABILITY";
	private static final String LIFE = "LIFE";
	private static final String VISION = "vision";
	private static final String DENTAL = "dental";
	private static final String MEDICAL = "medical";

	private static final String MM_DD_YYYY = "MM/dd/yyyy";
	private static final String RETIRED = "Retired";
	private static final String NOT_OFFERED = "Not Offered";
	private static final String INVALID = "Pending enrollment update";

	private static final Logger logger = LoggerFactory.getLogger(PlanRatesExportHelper.class);

	private static CellStyle styleHeaderFormat;
	private static CellStyle styleGenericRightFormat;
	private static CellStyle styleGenericLeftFormat;
	private static CellStyle styleGenericLeftBoldNoBorder;
	private static CellStyle styleCompanyHeaderFormat;
	private static CellStyle styleHeaderRightFormat;
	private static CellStyle styleCurrencyFormat;
	private static CellStyle styleCurrencyPlusFormat;
	private static CellStyle styleAdditionalCurrencyFormat;
	private static CellStyle stylePercentageFormat;

	@Autowired
	CompanyService companyService;

	@Autowired
	PlanRatesService planRatesService;

	private PlanRatesExportHelper() {
		throw new IllegalStateException(
				"Utility class " + PlanRatesExportHelper.class.getName() + " can not be instantiated.");
	}

	public static void constructPlanRatesWorkbook(Company company, PlanRatesExportData planRatesExportData,
			Workbook workbook, String hiddenColumns) {

		boolean planRateMappingFlag = RulesAndConfigsUtils.isPlanRateMappingEnabled(company.getRealmPlanYearId());
		
		createStyles(workbook);

		boolean showHistoryData = company.isRenewalCompany();
		if (!planRateMappingFlag) {
			hiddenColumns = hiddenColumns + HEAD_COUNT_CURRENT + CURRENT + " " + PLAN_NAME;
		}
		
		if (DateTimeComparator.getDateOnlyInstance()
				.compare(company.getRealmPlanYear().getPlanYearStart(), null) < 0) {
			showHistoryData = false;
			hiddenColumns = hiddenColumns + HEAD_COUNT_CURRENT + CURRENT + " " + PLAN_NAME;
		}
		
		if (!company.isRenewalCompany()) {
			hiddenColumns = hiddenColumns + HEAD_COUNT_CURRENT + HEAD_COUNT_FUTURE + CURRENT + " " + PLAN_NAME;
		}

		constructHealthPlan(company, planRatesExportData, workbook, hiddenColumns, showHistoryData, planRateMappingFlag);
		constructAdditionalPlan(company, planRatesExportData, workbook, showHistoryData);

		// For testing only
		// saveExcel(workbook);
	}

	private static void constructHealthPlan(Company company, PlanRatesExportData planRatesExportData, Workbook workbook,
			String hiddenColumns, boolean showHistoryData, boolean planRateMappingFlag) {

		if (planRatesExportData.getHealthPlanData() != null) {
			if (planRatesExportData.getHealthPlanData().get(MEDICAL) != null) {
				createPrimaryTab(company, planRatesExportData, workbook, MEDICAL, hiddenColumns, showHistoryData,
						planRateMappingFlag);
			}
			if (planRatesExportData.getHealthPlanData().get(DENTAL) != null) {
				createPrimaryTab(company, planRatesExportData, workbook, DENTAL, hiddenColumns, showHistoryData,
						planRateMappingFlag);
			}
			if (planRatesExportData.getHealthPlanData().get(VISION) != null) {
				createPrimaryTab(company, planRatesExportData, workbook, VISION, hiddenColumns, showHistoryData,
						planRateMappingFlag);
			}
		}
	}

	private static void constructAdditionalPlan(Company company, PlanRatesExportData planRatesExportData,
			Workbook workbook, boolean showHistoryData) {

		if (planRatesExportData.getAdditionalPlanData().get(DISABILITY) != null) {
			createDisabilityTab(company, planRatesExportData, workbook, showHistoryData);
		}

		if (planRatesExportData.getAdditionalPlanData().get(LIFE) != null) {
			createLifeTab(company, planRatesExportData, workbook, showHistoryData);
		}
	}

	static void createPrimaryTab(Company company, PlanRatesExportData planRatesExportData, Workbook workbook,
			String planType, String hiddenColumns, boolean showHistoryData, boolean planRateMappingFlag) {

		String futureStartToEndDate = getDateRangeString(planRatesExportData.getFutureStartDate(),
				planRatesExportData.getFutureEndDate());
		String currentStartToEndDate = getDateRangeString(planRatesExportData.getCurrentStartDate(),
				planRatesExportData.getCurrentEndDate());

		Sheet sheet = workbook.createSheet(StringUtils.capitalize(planType));
		sheet.setDefaultColumnWidth(15);

		createCompanyHeader(sheet, company);

		createPrimaryHeaderRow(sheet, futureStartToEndDate, currentStartToEndDate, hiddenColumns, showHistoryData, planRateMappingFlag);

		int rowNum = 3;
		int cellNum;

		for (HealthPlanRatesExportPlan healthPlanRatesExportPlan : planRatesExportData.getHealthPlanData()
				.get(planType)) {

			cellNum = 0;
			Row row = sheet.createRow(rowNum++);

			if (planRateMappingFlag) {
				if (hiddenColumns.indexOf(CURRENT + " " + PLAN_NAME) < 0) {
					createPlanNames(healthPlanRatesExportPlan.getCurrentName(), row, cellNum++);
				}
				createPlanNames(BSSApplicationConstants.INVALID_PLAN_ENROLLMENT.equals(healthPlanRatesExportPlan.getOfferedYearsFlag()) ? INVALID : healthPlanRatesExportPlan.getFutureName(), row, cellNum++);
			} else {
				createPlanNames(
						healthPlanRatesExportPlan.getFutureName() != null ? healthPlanRatesExportPlan.getFutureName()
								: healthPlanRatesExportPlan.getCurrentName(),
						row, cellNum++);
			}

			cellNum = createPrimaryDataForCoverageLevel(row, cellNum,
					healthPlanRatesExportPlan.getEmployeeOnlyFutureCost(),
					healthPlanRatesExportPlan.getEmployeeOnlyCurrentCost(),
					healthPlanRatesExportPlan.getEmployeeOnlyCurrentHeadcount(),
					healthPlanRatesExportPlan.getEmployeeOnlyFutureHeadcount(), showHistoryData, hiddenColumns,
					healthPlanRatesExportPlan.getOfferedYearsFlag());

			cellNum = createPrimaryDataForCoverageLevel(row, cellNum,
					healthPlanRatesExportPlan.getEmployeeSpouseFutureCost(),
					healthPlanRatesExportPlan.getEmployeeSpouseCurrentCost(),
					healthPlanRatesExportPlan.getEmployeeSpouseCurrentHeadcount(),
					healthPlanRatesExportPlan.getEmployeeSpouseFutureHeadcount(), showHistoryData, hiddenColumns,
					healthPlanRatesExportPlan.getOfferedYearsFlag());

			cellNum = createPrimaryDataForCoverageLevel(row, cellNum,
					healthPlanRatesExportPlan.getEmployeeChildFutureCost(),
					healthPlanRatesExportPlan.getEmployeeChildCurrentCost(),
					healthPlanRatesExportPlan.getEmployeeChildCurrentHeadcount(),
					healthPlanRatesExportPlan.getEmployeeChildFutureHeadcount(), showHistoryData, hiddenColumns,
					healthPlanRatesExportPlan.getOfferedYearsFlag());

			createPrimaryDataForCoverageLevel(row, cellNum, healthPlanRatesExportPlan.getEmployeeFamilyFutureCost(),
					healthPlanRatesExportPlan.getEmployeeFamilyCurrentCost(),
					healthPlanRatesExportPlan.getEmployeeFamilyCurrentHeadcount(),
					healthPlanRatesExportPlan.getEmployeeFamilyFutureHeadcount(), showHistoryData, hiddenColumns,
					healthPlanRatesExportPlan.getOfferedYearsFlag());
		}

		sheet.autoSizeColumn(0);
		if (hiddenColumns.indexOf(CURRENT + " " + PLAN_NAME) < 0) {
			sheet.autoSizeColumn(1);
		}
	}

	static int createPrimaryDataForCoverageLevel(Row row, int cellNum, BigDecimal futureCost, BigDecimal currentCost,
			Long currentHeadCount, Long futureHeadCount, boolean showHistoryData, String hiddenColumns, String offeredYearsFlag) {

		if (showHistoryData) {
			createCurrentCostCell(currentCost, row, cellNum++);
		}
		createFutureCostCell(futureCost, row, cellNum++, offeredYearsFlag);

		if (hiddenColumns.indexOf(RATE_DIFFERENCE) < 0 && showHistoryData) {
			createRateDifferenceCell(currentCost, futureCost, row, cellNum++);
		}

		if (hiddenColumns.indexOf(PERCENT_DIFFERENCE) < 0 && showHistoryData) {
			createPercentDifferenceCell(currentCost, futureCost, row, cellNum++);
		}
		
		if (hiddenColumns.indexOf(HEAD_COUNT_CURRENT) < 0) {
			createHeadcountCell(currentHeadCount, row, cellNum++);
		}

		if (hiddenColumns.indexOf(HEAD_COUNT_FUTURE) < 0) {
			createHeadcountCell(futureHeadCount, row, cellNum++);
		}

		return cellNum;

	}

	static void createLifeTab(Company company, PlanRatesExportData planRatesExportData, Workbook workbook,
			boolean showHistoryData) {

		String futureStartToEndDate = getDateRangeString(planRatesExportData.getFutureStartDate(),
				planRatesExportData.getFutureEndDate());
		String currentStartToEndDate = getDateRangeString(planRatesExportData.getCurrentStartDate(),
				planRatesExportData.getCurrentEndDate());

		Sheet sheet = workbook.createSheet("Life");

		createCompanyHeader(sheet, company);

		createLifeHeaderRow(sheet, futureStartToEndDate, currentStartToEndDate, showHistoryData);

		int rowNum = 3;
		for (AdditionalPlanOptionsExport additionalPlanOptions : planRatesExportData.getAdditionalPlanData()
				.get(LIFE)) {
			Row row = sheet.createRow(rowNum++);
			createLifeRow(additionalPlanOptions, row, showHistoryData);
		}
		setColumnWidths(sheet);
	}

	private static void createPlanNames(String planName, Row row, int columnCount) {

		Cell cell = row.createCell(columnCount);
		cell.setCellValue(Strings.nullToEmpty(planName));
		cell.setCellStyle(styleGenericLeftFormat);
	}

	private static void createLifeRow(AdditionalPlanOptionsExport additionalPlanOptions, Row row,
			boolean showHistoryData) {

		int cellNum = 0;
		Cell nameCell = row.createCell(cellNum++);
		nameCell.setCellValue(additionalPlanOptions.getName());
		nameCell.setCellStyle(styleGenericLeftFormat);

		if (showHistoryData) {
			Cell currentCostCell = row.createCell(cellNum++);
			Cell currentUnitCell = row.createCell(cellNum++);
			currentUnitCell.setCellStyle(styleGenericLeftFormat);

			if (additionalPlanOptions.getCurrentCost() == null) {
				currentCostCell.setCellStyle(styleGenericRightFormat);
				currentCostCell.setCellValue(NOT_OFFERED);
				currentUnitCell.setCellValue(DASH);
			} else {
				currentCostCell.setCellStyle(styleAdditionalCurrencyFormat);
				currentCostCell.setCellValue(additionalPlanOptions.getCurrentCost().doubleValue());
				currentUnitCell.setCellValue(additionalPlanOptions.getCurrentUnit());
			}
		}

		Cell futureCostCell = row.createCell(cellNum++);
		Cell futureUnitCell = row.createCell(cellNum);
		futureUnitCell.setCellStyle(styleGenericLeftFormat);

		if (additionalPlanOptions.getFutureCost() == null) {
			futureCostCell.setCellStyle(styleGenericRightFormat);
			futureCostCell.setCellValue(RETIRED);
			futureUnitCell.setCellValue(DASH);
		} else {
			futureCostCell.setCellStyle(styleAdditionalCurrencyFormat);
			futureCostCell.setCellValue(additionalPlanOptions.getFutureCost().doubleValue());
			futureUnitCell.setCellValue(additionalPlanOptions.getFutureUnit());
		}
	}

	private static void createCurrentCostCell(BigDecimal cost, Row row, int columnCount) {

		Cell cell = row.createCell(columnCount);
		cell.setCellStyle(styleCurrencyFormat);

		if (cost != null) {
			cell.setCellValue(cost.doubleValue());
		} else {
			cell.setCellValue(NOT_OFFERED);
			cell.setCellStyle(styleGenericRightFormat);
		}
	}

	private static void createFutureCostCell(BigDecimal cost, Row row, int columnCount, String offeredYearsFlag) {

		Cell cell = row.createCell(columnCount);
		cell.setCellStyle(styleCurrencyFormat);
		if (cost != null) {
			cell.setCellValue(cost.doubleValue());
		} else if (BSSApplicationConstants.INVALID_PLAN_ENROLLMENT.equals(offeredYearsFlag)) {
			cell.setCellValue(DASH);
			cell.setCellStyle(styleGenericRightFormat);
		} else {
			cell.setCellValue(RETIRED);
			cell.setCellStyle(styleGenericRightFormat);
		}
	}

	private static void createRateDifferenceCell(BigDecimal currentCost, BigDecimal futureCost, Row row,
			int columnCount) {

		Cell cell = row.createCell(columnCount);
		cell.setCellStyle(styleCurrencyPlusFormat);

		if (futureCost != null && currentCost != null) {
			BigDecimal rateDiff = futureCost.subtract(currentCost);
			cell.setCellValue(rateDiff.doubleValue());
		} else {
			cell.setCellValue(DASH);
			cell.setCellStyle(styleGenericRightFormat);
		}
	}

	private static void createPercentDifferenceCell(BigDecimal currentCost, BigDecimal futureCost, Row row,
			int columnCount) {

		Cell cell = row.createCell(columnCount);
		cell.setCellStyle(stylePercentageFormat);

		MathContext precision = new MathContext(9);
		if (futureCost != null && currentCost != null) {
			BigDecimal diff = futureCost.subtract(currentCost, precision);
			BigDecimal percent = diff.divide(currentCost, precision);
			cell.setCellValue(percent.doubleValue());
		} else {
			cell.setCellValue(DASH);
			cell.setCellStyle(styleGenericRightFormat);
		}
	}

	private static void createHeadcountCell(Long headCount, Row row, int columnCount) {

		Cell cell = row.createCell(columnCount);
		cell.setCellStyle(styleGenericRightFormat);
		if (headCount != null) {
			cell.setCellValue(headCount);
		} else {
			cell.setCellValue(0);
		}
	}

	private static void createCompanyHeader(Sheet sheet, Company company) {

		int cellNum = 0;
		Row row = sheet.createRow(0);

		Cell cell0 = row.createCell(cellNum++);
		cell0.setCellValue("Company:");
		cell0.setCellStyle(styleHeaderRightFormat);

		Cell cell1 = row.createCell(cellNum);
		cell1.setCellValue(company.getName());
		cell1.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
		cell1.setCellStyle(styleCompanyHeaderFormat);

		sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));
	}

	private static void createLifeHeaderRow(Sheet sheet, String futureStartToEndDate, String currentStartToEndDate,
			boolean showHistoryData) {

		int cellNum = 0;
		Row row = sheet.createRow(2);

		createHeaderCell(row, cellNum++, "Plan - Group Term Life Insurance and AD&D\n(Employee Coverage Only)",
				styleHeaderFormat);

		if (showHistoryData) {
			createHeaderCell(row, cellNum++, currentStartToEndDate, styleHeaderFormat);
			createHeaderCell(row, cellNum++, PER_UNIT, styleHeaderFormat);
		}

		createHeaderCell(row, cellNum++, futureStartToEndDate, styleHeaderFormat);
		createHeaderCell(row, cellNum, PER_UNIT, styleHeaderFormat);
	}

	private static String getDateRangeString(String startDate, String endDate) {

		SimpleDateFormat formatter = new SimpleDateFormat(MM_DD_YYYY);
		String returnString = "";

		if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			returnString = formatter
					.format(Utils.convertStringToDate(startDate, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY)) + DASH
					+ formatter
							.format(Utils.convertStringToDate(endDate, BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		}
		return returnString;
	}

	private static void createStyles(Workbook workbook) {

		Font fontBold = workbook.createFont();
		fontBold.setBold(true);
		fontBold.setFontHeightInPoints((short) 12);

		Font font = workbook.createFont();
		font.setBold(false);
		font.setFontHeightInPoints((short) 12);

		styleCompanyHeaderFormat = workbook.createCellStyle();
		styleCompanyHeaderFormat.setFont(fontBold);
		addBorder(styleCompanyHeaderFormat);

		styleHeaderFormat = workbook.createCellStyle();
		styleHeaderFormat.setWrapText(true);
		styleHeaderFormat.setFont(fontBold);
		styleHeaderFormat.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		styleHeaderFormat.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleHeaderFormat.setAlignment(HorizontalAlignment.CENTER);
		styleHeaderFormat.setVerticalAlignment(VerticalAlignment.CENTER);
		addBorder(styleHeaderFormat);

		styleHeaderRightFormat = workbook.createCellStyle();
		styleHeaderRightFormat.setFont(fontBold);
		styleHeaderRightFormat.setAlignment(HorizontalAlignment.RIGHT);
		addBorder(styleHeaderRightFormat);

		styleGenericRightFormat = workbook.createCellStyle();
		styleGenericRightFormat.setAlignment(HorizontalAlignment.RIGHT);
		styleGenericRightFormat.setFont(font);
		addBorder(styleGenericRightFormat);

		styleGenericLeftFormat = workbook.createCellStyle();
		styleGenericLeftFormat.setAlignment(HorizontalAlignment.LEFT);
		styleGenericLeftFormat.setFont(font);
		addBorder(styleGenericLeftFormat);

		styleGenericLeftBoldNoBorder = workbook.createCellStyle();
		styleGenericLeftBoldNoBorder.setAlignment(HorizontalAlignment.LEFT);
		styleGenericLeftBoldNoBorder.setFont(fontBold);

		styleCurrencyFormat = workbook.createCellStyle();
		DataFormat dataFormat = workbook.createDataFormat();
		styleCurrencyFormat.setDataFormat(dataFormat.getFormat("$#,##0.00"));
		addBorder(styleCurrencyFormat);

		styleCurrencyPlusFormat = workbook.createCellStyle();
		DataFormat dataPlusFormat = workbook.createDataFormat();
		styleCurrencyPlusFormat.setDataFormat(dataPlusFormat.getFormat("+$#,##0.00;-$#,##0.00;$#,##0.00"));
		addBorder(styleCurrencyPlusFormat);

		styleAdditionalCurrencyFormat = workbook.createCellStyle();
		DataFormat dataLifeFormat = workbook.createDataFormat();
		styleAdditionalCurrencyFormat.setDataFormat(dataLifeFormat.getFormat("$#,##0.0000"));
		addBorder(styleAdditionalCurrencyFormat);

		stylePercentageFormat = workbook.createCellStyle();
		stylePercentageFormat.setDataFormat(dataFormat.getFormat("+0.00%;-0.00%;0.00%"));
		addBorder(stylePercentageFormat);
	}

	private static void addBorder(CellStyle style) {

		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
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

	static void saveExcel(Workbook workbook) {
		try {
			FileOutputStream outputStream = new FileOutputStream("/Users/jshuali/OneDrive/PlanRates.xlsx");
			workbook.write(outputStream);
			workbook.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static void createPrimaryHeaderRow(Sheet sheet, String futureStartToEndDate, String currentStartToEndDate,
			String hiddenColumns, boolean showHistoryData, boolean planRateMappingFlag) {

		int cellNum = 0;
		Row row = sheet.createRow(2);

		if (planRateMappingFlag && showHistoryData) {
			if (hiddenColumns.indexOf(CURRENT + " " + PLAN_NAME) < 0) {
				createHeaderCell(row, cellNum++, CURRENT + " " + PLAN_NAME, styleHeaderFormat);
			}
			createHeaderCell(row, cellNum++, FUTURE + " " + PLAN_NAME, styleHeaderFormat);
		} else {
			createHeaderCell(row, cellNum++, PLAN_NAME, styleHeaderFormat);
		}
		
		cellNum = createPrimayHeadersForCoverageLevel(row, cellNum, EMPLOYEE_ONLY, showHistoryData,
				futureStartToEndDate, currentStartToEndDate, hiddenColumns);

		cellNum = createPrimayHeadersForCoverageLevel(row, cellNum, EMPLOYEE_SPOUSE, showHistoryData,
				futureStartToEndDate, currentStartToEndDate, hiddenColumns);

		cellNum = createPrimayHeadersForCoverageLevel(row, cellNum, EMPLOYEE_CHILDREN, showHistoryData,
				futureStartToEndDate, currentStartToEndDate, hiddenColumns);

		createPrimayHeadersForCoverageLevel(row, cellNum, FAMILY, showHistoryData, futureStartToEndDate,
				currentStartToEndDate, hiddenColumns);

	}

	static int createPrimayHeadersForCoverageLevel(Row row, int cellNum, String coverageLevel, boolean showHistoryData,
			String futureStartToEndDate, String currentStartToEndDate, String hiddenColumns) {

		if (showHistoryData) {
			createHeaderCell(row, cellNum++, coverageLevel + DASH + currentStartToEndDate, styleHeaderFormat);
		}
		
		createHeaderCell(row, cellNum++, coverageLevel + DASH + futureStartToEndDate, styleHeaderFormat);

		if (hiddenColumns.indexOf(RATE_DIFFERENCE) < 0 && showHistoryData) {
			createHeaderCell(row, cellNum++, RATE_DIFFERENCE, styleHeaderFormat);
		}

		if (hiddenColumns.indexOf(PERCENT_DIFFERENCE) < 0 && showHistoryData) {
			createHeaderCell(row, cellNum++, PERCENT_DIFFERENCE, styleHeaderFormat);
		}

		if (hiddenColumns.indexOf(HEAD_COUNT_CURRENT) < 0) {
			createHeaderCell(row, cellNum++, HEAD_COUNT + DASH + currentStartToEndDate, styleHeaderFormat);
		}

		if (hiddenColumns.indexOf(HEAD_COUNT_FUTURE) < 0) {
			createHeaderCell(row, cellNum++, HEAD_COUNT + DASH + futureStartToEndDate, styleHeaderFormat);
		}

		return cellNum;

	}

	static void createHeaderCell(Row row, int cellNum, String text, CellStyle style) {
		Cell cell = row.createCell(cellNum);
		cell.setCellValue(text);
		cell.setCellStyle(style);
	}

	static void createDisabilityTab(Company company, PlanRatesExportData planRatesExportData, Workbook workbook,
			boolean showHistoryData) {

		String futureStartToEndDate = getDateRangeString(planRatesExportData.getFutureStartDate(),
				planRatesExportData.getFutureEndDate());
		String currentStartToEndDate = getDateRangeString(planRatesExportData.getCurrentStartDate(),
				planRatesExportData.getCurrentEndDate());

		Sheet sheet = workbook.createSheet("Disability");

		createCompanyHeader(sheet, company);

		createDisabilityHeaderRow(sheet, futureStartToEndDate, currentStartToEndDate, showHistoryData);

		AtomicInteger rowNum = new AtomicInteger(3);
		for (AdditionalPlanOptionsExport additionalPlanOptions : planRatesExportData.getAdditionalPlanData()
				.get(DISABILITY)) {

			createDisabilityBundleTitleRow(additionalPlanOptions, sheet, rowNum);
			createDisabilityRows(additionalPlanOptions, sheet, rowNum, showHistoryData);
			sheet.createRow(rowNum.getAndIncrement());
		}
		setColumnWidths(sheet);
	}

	private static void createDisabilityBundleTitleRow(AdditionalPlanOptionsExport additionalPlanOptions, Sheet sheet,
			AtomicInteger rowNum) {

		int cellNum = 0;
		Row row = sheet.createRow(rowNum.getAndIncrement());

		Cell cell0 = row.createCell(cellNum);
		cell0.setCellValue(additionalPlanOptions.getName());
		cell0.setCellStyle(styleGenericLeftBoldNoBorder);
		sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 6));
	}

	private static void createDisabilityRows(AdditionalPlanOptionsExport additionalPlanOptions, Sheet sheet,
			AtomicInteger rowNum, boolean showHistoryData) {

		for (AdditionalPlanOptionPlanExport additionalPlan : additionalPlanOptions.getOptionPlans()) {

			int cellNum = 0;
			Row row = sheet.createRow(rowNum.getAndIncrement());

			Cell cell0 = row.createCell(cellNum++);
			cell0.setCellValue(additionalPlan.getPlanType());
			cell0.setCellStyle(styleGenericLeftFormat);

			Cell cell1 = row.createCell(cellNum++);
			cell1.setCellValue(additionalPlan.getName());
			cell1.setCellStyle(styleGenericLeftFormat);

			Cell cell2 = row.createCell(cellNum++);
			cell2.setCellValue(additionalPlan.getOfferedStatesString());
			cell2.setCellStyle(styleGenericLeftFormat);

			if (showHistoryData) {

				Cell cell5 = row.createCell(cellNum++);
				Cell cell6 = row.createCell(cellNum++);
				cell6.setCellStyle(styleGenericLeftFormat);

				if (additionalPlan.getCurrentCost() == null) {
					cell5.setCellStyle(styleGenericRightFormat);
					cell5.setCellValue(NOT_OFFERED);
					cell6.setCellValue(DASH);
				} else {
					cell5.setCellStyle(styleAdditionalCurrencyFormat);
					cell5.setCellValue(additionalPlan.getCurrentCost().doubleValue());
					cell6.setCellValue(additionalPlan.getCurrentUnit());
				}
			}

			Cell cell3 = row.createCell(cellNum++);
			Cell cell4 = row.createCell(cellNum);
			cell4.setCellStyle(styleGenericLeftFormat);

			if (additionalPlan.getFutureCost() == null) {
				cell3.setCellStyle(styleGenericRightFormat);
				cell3.setCellValue(RETIRED);
				cell4.setCellValue(DASH);
			} else {
				cell3.setCellStyle(styleAdditionalCurrencyFormat);
				cell3.setCellValue(additionalPlan.getFutureCost().doubleValue());
				cell4.setCellValue(additionalPlan.getFutureUnit());
			}
		}
	}

	private static void createDisabilityHeaderRow(Sheet sheet, String futureStartToEndDate,
			String currentStartToEndDate, boolean showHistoryData) {

		int cellNum = 0;
		Row row = sheet.createRow(2);

		createHeaderCell(row, cellNum++, "Company Paid Benefit", styleHeaderFormat);
		createHeaderCell(row, cellNum++, PLAN_NAME, styleHeaderFormat);
		createHeaderCell(row, cellNum++, STATES, styleHeaderFormat);

		if (showHistoryData) {
			createHeaderCell(row, cellNum++, currentStartToEndDate, styleHeaderFormat);
			createHeaderCell(row, cellNum++, PER_UNIT, styleHeaderFormat);
		}
		
		createHeaderCell(row, cellNum++, futureStartToEndDate, styleHeaderFormat);
		createHeaderCell(row, cellNum, PER_UNIT, styleHeaderFormat);
	}
}
