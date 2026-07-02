/**
 * 
 */
package com.trinet.ambis.helper;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author schaudhari
 *
 */
@Component
@Slf4j
public class PlanCompareExportHelper {

	private static final short PALE_BLUE_FG_COLOR = IndexedColors.LIGHT_BLUE.getIndex();
	private static final short ORANGE_FG_COLOR = IndexedColors.LIGHT_ORANGE.getIndex();
	private static final short GREY_25_FG_COLOR = IndexedColors.GREY_25_PERCENT.getIndex();
	private static final short GREY_50_FG_COLOR = IndexedColors.GREY_50_PERCENT.getIndex();
	private static final String ATTRIBUTE_HEADER_TITLE = "Benefit";
	private static final String NO_PLAN_MESSAGE = "No plans available for comparison";
	private static final String MEDICAL_SHEET_NAME = "Medical";
	private static final String DENTAL_SHEET_NAME = "Dental";
	private static final String VISION_SHEET_NAME = "Vision";
	private static final int HEADER_ROW_INDEX = 0;
	private static final int ATTRIBUTE_COLUMN_INDEX = 0;
	private static final int COLUMN_WIDTH = 10000;
	private static final int SPACER_COLUMN_WIDTH = 600;
	private static final String WORKBOOK_PSWRD = "Tg$2#eruni0";

	public Workbook constructWorkbook(Map<String, Set<String>> plansIdsToCompare,
			Map<String, BenefitPlanCompare> currentPlansAttributes,
			Map<String, BenefitPlanCompare> futurePlansAttributes,
			Map<String, BenefitPlan> currentYrRegionalBasePlanMappings,
			Map<String, BenefitPlan> futureYrRegionalBasePlanMappings) {
		StopWatch taskWatch = new StopWatch("PreparePlanCompareExcel");
		taskWatch.start();

		Map<String, PlanCompareExcelModel> excelHelperModels = prepareExcelHelperModels();

		Workbook workbook = new XSSFWorkbook();

		EnumMap<CellStyleEnum, CellStyle> allCellStyles = prepareAllCellStyle(workbook);

		for (Entry<String, Set<String>> plansToCompare : plansIdsToCompare.entrySet()) {
			BenefitPlanCompare currentBenPlan = currentPlansAttributes.get(plansToCompare.getKey());

			Optional<PlanCompareExcelModel> planCompareExcelModelOpt = generateSheetPlansAndAttributes(workbook,
					excelHelperModels, plansToCompare.getKey(), currentBenPlan, currentYrRegionalBasePlanMappings,
					allCellStyles, true);

			Set<String> futureBenPlanIds = plansToCompare.getValue();
			for (String futureBenPlanId : futureBenPlanIds) {
				BenefitPlanCompare futureBenPlan = futurePlansAttributes.get(futureBenPlanId);
				planCompareExcelModelOpt = generateSheetPlansAndAttributes(workbook, excelHelperModels, futureBenPlanId,
						futureBenPlan, futureYrRegionalBasePlanMappings, allCellStyles, false);
			}

			storeSpacerColumnIndexes(planCompareExcelModelOpt);
		}

		createSpacerColumns(workbook, excelHelperModels, allCellStyles);

		freezeAttributeColumnAndHeaderRow(workbook);

		orderSheetsAndMarkSelected(workbook);

		lockWorkbook(workbook);

		createEmptySheetIfNoPlanForComparison(workbook);

		addDisclaimerText(workbook, allCellStyles);

		taskWatch.stop();
		log.info(String.format("%s took :: %s", taskWatch.getId(), taskWatch.getTotalTimeMillis()));

		return workbook;
	}

	private Optional<PlanCompareExcelModel> generateSheetPlansAndAttributes(Workbook workbook,
			Map<String, PlanCompareExcelModel> excelHelperModels, String planId, BenefitPlanCompare benefitPlan,
			Map<String, BenefitPlan> regionalBasePlanMappings, EnumMap<CellStyleEnum, CellStyle> allCellStyles,
			boolean isCurrentPlan) {
		if (benefitPlan == null || benefitPlan.getBenefitType() == null
				|| CollectionUtils.isEmpty(benefitPlan.getTemplate())) {
			log.error(String.format("No plan attributes data found for benefit plan : %s", planId));
			return Optional.empty();
		}
		if (regionalBasePlanMappings.containsKey(planId)) {
			benefitPlan.setName(regionalBasePlanMappings.get(planId).getDescr());
		}

		PlanCompareExcelModel excelHelperModel = excelHelperModels.get(benefitPlan.getBenefitType());
		if (excelHelperModel.getSheet() == null) {
			Sheet sheet = createSheetWithAttributeHeader(workbook, benefitPlan.getBenefitType(), allCellStyles);
			excelHelperModel.setSheet(sheet);
		}

		createPlanNameHeaderCell(benefitPlan, excelHelperModel, allCellStyles, isCurrentPlan);

		for (PlanCompareTemplate template : benefitPlan.getTemplate()) {
			for (Attribute attribute : template.getChildren()) {
				if (attribute == null || StringUtils.isEmpty(attribute.getName())) {
					log.error(String.format("No plan attributes data found for benefit plan : %s", planId));
					return Optional.empty();
				}
				createAttributeNameCell(attribute, excelHelperModel, allCellStyles);
				createAttributeValueCell(attribute, excelHelperModel, allCellStyles);
			}
		}

		excelHelperModel.setAddSpacerColumn(true);
		return Optional.of(excelHelperModel);
	}

	private void createPlanNameHeaderCell(BenefitPlanCompare benefitPlan, PlanCompareExcelModel excelHelperModel,
			EnumMap<CellStyleEnum, CellStyle> allCellStyles, boolean isCurrentPlan) {
		Row row = excelHelperModel.getSheet().getRow(HEADER_ROW_INDEX);
		Cell planNameHeaderCell = row.createCell(excelHelperModel.getColumnIndexCount().incrementAndGet());
		planNameHeaderCell.setCellValue(benefitPlan.getName());
		if (isCurrentPlan) {
			planNameHeaderCell.setCellStyle(allCellStyles.get(CellStyleEnum.BLUE_HEADER_CELL_STYLE));
		} else {
			planNameHeaderCell.setCellStyle(allCellStyles.get(CellStyleEnum.ORANGE_HEADER_CELL_STYLE));
		}
		excelHelperModel.getSheet().setColumnWidth(excelHelperModel.getColumnIndexCount().get(), COLUMN_WIDTH);
	}

	private Sheet createSheetWithAttributeHeader(Workbook workbook, String benefitType,
			EnumMap<CellStyleEnum, CellStyle> allCellStyles) {
		Sheet sheet = workbook.createSheet(benefitType);
		Row row = sheet.createRow(HEADER_ROW_INDEX);
		createAttributeHeaderCell(row, allCellStyles);
		sheet.setColumnWidth(ATTRIBUTE_COLUMN_INDEX, COLUMN_WIDTH);
		return sheet;
	}

	private void createAttributeHeaderCell(Row row, EnumMap<CellStyleEnum, CellStyle> allCellStyles) {
		Cell attributeHeaderCell = row.createCell(ATTRIBUTE_COLUMN_INDEX);
		attributeHeaderCell.setCellValue(ATTRIBUTE_HEADER_TITLE);
		attributeHeaderCell.setCellStyle(allCellStyles.get(CellStyleEnum.GRAY_HEADER_CELL_STYLE));
	}

	private void createAttributeNameCell(Attribute attribute, PlanCompareExcelModel excelHelperModel,
			EnumMap<CellStyleEnum, CellStyle> allCellStyles) {
		Row row;
		// Create attribute if does't exists otherwise get attribute row by attribute
		// index
		if (excelHelperModel.getAttributeRowIndexes().containsKey(attribute.getName())) {
			row = excelHelperModel.getSheet()
					.getRow(excelHelperModel.getAttributeRowIndexes().get(attribute.getName()));
		} else {
			excelHelperModel.getAttributeRowIndexes().put(attribute.getName(),
					excelHelperModel.getAttributeCount().incrementAndGet());
			row = excelHelperModel.getSheet().createRow(excelHelperModel.getAttributeCount().get());
		}
		createAttributeNameCell(allCellStyles, attribute, row);
	}

	private void createAttributeValueCell(Attribute attribute, PlanCompareExcelModel excelHelperModel,
			EnumMap<CellStyleEnum, CellStyle> allCellStyles) {
		// Set attribute value for plan
		Row row = excelHelperModel.getSheet()
				.getRow(excelHelperModel.getAttributeRowIndexes().get(attribute.getName()));
		Cell attributeNameCell = row.createCell(excelHelperModel.getColumnIndexCount().get());
		attributeNameCell.setCellValue(Optional.ofNullable(attribute.getValue()).orElse(""));
		attributeNameCell.setCellStyle(allCellStyles.get(CellStyleEnum.ATTR_NAME_VALUE_CELL_STYLE));
	}

	private void createAttributeNameCell(EnumMap<CellStyleEnum, CellStyle> allCellStyles, Attribute attribute,
			Row row) {
		Cell attributeNameCell = row.createCell(ATTRIBUTE_COLUMN_INDEX);
		attributeNameCell.setCellValue(attribute.getName());
		attributeNameCell.setCellStyle(allCellStyles.get(CellStyleEnum.ATTR_NAME_VALUE_CELL_STYLE));
	}

	private void storeSpacerColumnIndexes(Optional<PlanCompareExcelModel> planCompareExcelModelOpt) {
		if (planCompareExcelModelOpt.isPresent() && planCompareExcelModelOpt.get().isAddSpacerColumn()) {
			PlanCompareExcelModel planCompareExcelModel = planCompareExcelModelOpt.get();
			Sheet sheet = planCompareExcelModel.getSheet();
			short lastCellIndex = sheet.getRow(0).getLastCellNum();

			planCompareExcelModel.getSpacerColumnIndexes().add(Integer.valueOf(lastCellIndex));
			planCompareExcelModel.setAddSpacerColumn(false);
			planCompareExcelModel.getColumnIndexCount().incrementAndGet();
		}
	}

	private void createEmptySheetIfNoPlanForComparison(Workbook workbook) {
		if (workbook.getNumberOfSheets() == 0) {
			Sheet sheet = workbook.createSheet();
			Row row = sheet.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue(NO_PLAN_MESSAGE);
		}
	}

	private void addDisclaimerText(Workbook workbook, EnumMap<CellStyleEnum, CellStyle> cellStyles) {
		int disclaimerCellStartIdx = 1;
		int disclaimerCellEndIdx = 13;
		for (Sheet sheet : workbook) {
			int rowNumber = sheet.getLastRowNum() + 2;
			Row disclaimerRow = sheet.createRow(rowNumber);
			Cell dislaimerCell = disclaimerRow.createCell(disclaimerCellStartIdx);
			dislaimerCell.setCellStyle(cellStyles.get(CellStyleEnum.DISCLAIMER_CELL_STYLE));
			dislaimerCell.setCellValue(BSSMessageConfig.getProperty("planCompareDisclaimerText"));
			for (int i = disclaimerCellStartIdx + 1; i <= disclaimerCellEndIdx; ++i) {
				dislaimerCell = disclaimerRow.createCell(i);
				dislaimerCell.setCellStyle(cellStyles.get(CellStyleEnum.DISCLAIMER_CELL_STYLE));
			}
			disclaimerRow.setHeight((short) 1500);
			sheet.addMergedRegion(
					new CellRangeAddress(rowNumber, rowNumber, disclaimerCellStartIdx, disclaimerCellEndIdx));
		}
	}

	private void createSpacerColumns(Workbook workbook, Map<String, PlanCompareExcelModel> excelHelperModels,
			EnumMap<CellStyleEnum, CellStyle> allCellStyles) {
		for (Sheet sheet : workbook) {
			PlanCompareExcelModel model = excelHelperModels.get(sheet.getSheetName());
			Set<Integer> spacerColumnIndexes = model.getSpacerColumnIndexes();
			for (int spacerColumnIndex : spacerColumnIndexes) {
				if (model.getColumnIndexCount().get() != spacerColumnIndex) {
					CellStyle spacerColumnStyle = workbook.createCellStyle();
					spacerColumnStyle.setShrinkToFit(false);
					((XSSFSheet) sheet).getColumnHelper().setColDefaultStyle(spacerColumnIndex, spacerColumnStyle);
					for (int rowCnt = 0; rowCnt <= model.getAttributeCount().get(); rowCnt++) {
						Cell cell = sheet.getRow(rowCnt).createCell(spacerColumnIndex);
						cell.setCellStyle(allCellStyles.get(CellStyleEnum.GRAY_SPACER_CELL_STYLE));
					}
					sheet.setColumnWidth(spacerColumnIndex, SPACER_COLUMN_WIDTH);
				}
			}
		}
	}

	private void freezeAttributeColumnAndHeaderRow(Workbook workbook) {
		for (Sheet sheet : workbook) {
			sheet.createFreezePane(1, 1);
		}
	}

	private void orderSheetsAndMarkSelected(Workbook workbook) {
		if (workbook.getNumberOfSheets() > 0) {
			AtomicInteger sheetOrder = new AtomicInteger();
			if (workbook.getSheet(MEDICAL_SHEET_NAME) != null) {
				workbook.setSheetOrder(MEDICAL_SHEET_NAME, sheetOrder.getAndIncrement());
			}
			if (workbook.getSheet(DENTAL_SHEET_NAME) != null) {
				workbook.setSheetOrder(DENTAL_SHEET_NAME, sheetOrder.getAndIncrement());
				workbook.getSheet(DENTAL_SHEET_NAME).setSelected(false);
			}
			if (workbook.getSheet(VISION_SHEET_NAME) != null) {
				workbook.setSheetOrder(VISION_SHEET_NAME, sheetOrder.getAndIncrement());
				workbook.getSheet(VISION_SHEET_NAME).setSelected(false);
			}
			workbook.setActiveSheet(0);
			workbook.setSelectedTab(0);
		}
	}

	private void lockWorkbook(Workbook workbook) {
		XSSFWorkbook xssfWorkbook = (XSSFWorkbook) workbook;
		xssfWorkbook.lockStructure();
		xssfWorkbook.lockRevision();
		xssfWorkbook.lockWindows();
		for (Sheet sheet : xssfWorkbook) {
			((XSSFSheet) sheet).enableLocking();
			((XSSFSheet) sheet).setSheetPassword(WORKBOOK_PSWRD, HashAlgorithm.sha512);
		}
		xssfWorkbook.setWorkbookPassword(WORKBOOK_PSWRD, HashAlgorithm.sha512);
	}

	private Map<String, PlanCompareExcelModel> prepareExcelHelperModels() {
		PlanCompareExcelModel medicalExcelHelperModel = new PlanCompareExcelModel();
		PlanCompareExcelModel dentalExcelHelperModel = new PlanCompareExcelModel();
		PlanCompareExcelModel visionExcelHelperModel = new PlanCompareExcelModel();

		Map<String, PlanCompareExcelModel> excelHelperModels = new HashMap<>();
		excelHelperModels.put(MEDICAL_SHEET_NAME, medicalExcelHelperModel);
		excelHelperModels.put(DENTAL_SHEET_NAME, dentalExcelHelperModel);
		excelHelperModels.put(VISION_SHEET_NAME, visionExcelHelperModel);
		return excelHelperModels;
	}

	@Data
	class PlanCompareExcelModel {
		private Map<String, Integer> attributeRowIndexes = new HashMap<>();
		private AtomicInteger attributeCount = new AtomicInteger();
		private AtomicInteger columnIndexCount = new AtomicInteger();
		private Sheet sheet;
		private boolean addSpacerColumn;
		private Set<Integer> spacerColumnIndexes = new HashSet<>();
	}

	private EnumMap<CellStyleEnum, CellStyle> prepareAllCellStyle(Workbook workbook) {
		CellStyle borderCellStyle = createBorderCellStyle(workbook);
		CellStyle attNameValueCellStyle = createAttributeNameAndValueCellStyle(workbook, borderCellStyle);
		CellStyle orangeHeaderCellStyle = createOrangeHeaderCellStyle(workbook, borderCellStyle);
		CellStyle blueHeaderCellStyle = createBlueHeaderCellStyle(workbook, borderCellStyle);
		CellStyle grayHeaderCellStyle = createGrayHeaderCellStyle(workbook, borderCellStyle);
		CellStyle graySpacerCellStyle = createGraySpacerCellStyle(workbook);
		CellStyle disclaimerCellStyle = createDisclaimerCellStyle(workbook, borderCellStyle);
		EnumMap<CellStyleEnum, CellStyle> cellStyleMap = new EnumMap<>(CellStyleEnum.class);
		cellStyleMap.put(CellStyleEnum.ATTR_NAME_VALUE_CELL_STYLE, attNameValueCellStyle);
		cellStyleMap.put(CellStyleEnum.BLUE_HEADER_CELL_STYLE, blueHeaderCellStyle);
		cellStyleMap.put(CellStyleEnum.GRAY_HEADER_CELL_STYLE, grayHeaderCellStyle);
		cellStyleMap.put(CellStyleEnum.ORANGE_HEADER_CELL_STYLE, orangeHeaderCellStyle);
		cellStyleMap.put(CellStyleEnum.GRAY_SPACER_CELL_STYLE, graySpacerCellStyle);
		cellStyleMap.put(CellStyleEnum.DISCLAIMER_CELL_STYLE, disclaimerCellStyle);
		return cellStyleMap;
	}

	private static CellStyle createBorderCellStyle(Workbook workbook) {
		CellStyle borderCellStyle = workbook.createCellStyle();
		borderCellStyle.setBorderTop(BorderStyle.THIN);
		borderCellStyle.setBorderRight(BorderStyle.THIN);
		borderCellStyle.setBorderBottom(BorderStyle.THIN);
		borderCellStyle.setBorderLeft(BorderStyle.THIN);
		return borderCellStyle;
	}

	private static CellStyle createOrangeHeaderCellStyle(Workbook workbook, CellStyle borderCellStyle) {
		CellStyle headerCellStyle = createHeaderCellStyle(workbook, borderCellStyle);
		headerCellStyle.setFillForegroundColor(ORANGE_FG_COLOR);
		return headerCellStyle;
	}

	private static CellStyle createBlueHeaderCellStyle(Workbook workbook, CellStyle borderCellStyle) {
		CellStyle headerCellStyle = createHeaderCellStyle(workbook, borderCellStyle);
		headerCellStyle.setFillForegroundColor(PALE_BLUE_FG_COLOR);
		return headerCellStyle;
	}

	private static CellStyle createGrayHeaderCellStyle(Workbook workbook, CellStyle borderCellStyle) {
		CellStyle headerCellStyle = createHeaderCellStyle(workbook, borderCellStyle);
		headerCellStyle.setFillForegroundColor(GREY_50_FG_COLOR);
		return headerCellStyle;
	}

	private static CellStyle createGraySpacerCellStyle(Workbook workbook) {
		CellStyle spacerCellStyle = workbook.createCellStyle();
		spacerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		spacerCellStyle.setFillForegroundColor(GREY_25_FG_COLOR);
		return spacerCellStyle;
	}

	private CellStyle createDisclaimerCellStyle(Workbook workbook, CellStyle borderCellStyle) {
		CellStyle disclaimerCellStyle = workbook.createCellStyle();
		disclaimerCellStyle.cloneStyleFrom(borderCellStyle);
		disclaimerCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		disclaimerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		disclaimerCellStyle.setWrapText(true);
		disclaimerCellStyle.setAlignment(HorizontalAlignment.LEFT);
		disclaimerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		return disclaimerCellStyle;
	}

	private static CellStyle createHeaderCellStyle(Workbook workbook, CellStyle borderCellStyle) {
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.cloneStyleFrom(borderCellStyle);
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		headerCellStyle.setFont(headerFont);

		return headerCellStyle;
	}

	private static CellStyle createAttributeNameAndValueCellStyle(Workbook workbook, CellStyle borderCellStyle) {
		CellStyle attributeNameAndValueCellStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setFontHeightInPoints((short) 12);
		attributeNameAndValueCellStyle.cloneStyleFrom(borderCellStyle);
		attributeNameAndValueCellStyle.setFont(headerFont);
		attributeNameAndValueCellStyle.setWrapText(true);
		return attributeNameAndValueCellStyle;
	}

	private enum CellStyleEnum {
		ATTR_NAME_VALUE_CELL_STYLE, ORANGE_HEADER_CELL_STYLE, BLUE_HEADER_CELL_STYLE, GRAY_HEADER_CELL_STYLE,
		GRAY_SPACER_CELL_STYLE, DISCLAIMER_CELL_STYLE
	}

}
