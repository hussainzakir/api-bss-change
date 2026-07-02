package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSExportConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlanRateData;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.EmployeeStrategyPlanData;
import com.trinet.ambis.service.model.ModelComparePlanTypeCost;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class ModelCompareExportHelperTest extends ServiceUnitTest {

	private static final Long STRATEGY_ID_1 = 1L;
	private static final Long STRATEGY_ID_2 = 2L;

	@Test
	public void constructCompanyStrategyWorkbook() {

		Workbook workbook = new XSSFWorkbook();
		Company company = prepareCompany();

		ModelCompareExportHelper.constructCompanyStrategyWorkbook(company, prepareStrategyMap(),
				prepareModelCompareStrategyCostList(), workbook);
		
		Sheet sheet = workbook.getSheet(BSSExportConstants.COMPANY_SHEET_NAME);
		
		assertEquals(1, workbook.getNumberOfSheets());
		assertEquals(12, sheet.getLastRowNum());
		assertEquals("Company Confidential", sheet.getRow(12).getCell(0).getStringCellValue());

	}

	@Test
	public void constructEmployeeStrategiesPlanCostWorkbook() {

		Workbook workbook = new XSSFWorkbook();
		
		ModelCompareExportHelper.constructEmployeeStrategiesPlanCostWorkbook(prepareStrategyMap(),
				prepareEmployeeStrategyDataList(), workbook);
		
		Sheet sheet = workbook.getSheet(BSSExportConstants.EMPLOYEE_SHEET_NAME);
		
		assertEquals(1, workbook.getNumberOfSheets());
		assertEquals(7, sheet.getLastRowNum());
		assertEquals("Company Confidential", sheet.getRow(0).getCell(0).getStringCellValue());
		
		Row row = sheet.getRow(1);
		assertEquals("Last Name", row.getCell(0).getStringCellValue());
		assertEquals("First Name", row.getCell(1).getStringCellValue());
		assertEquals("Full Name", row.getCell(2).getStringCellValue());
		assertEquals("Employee ID", row.getCell(3).getStringCellValue());
		assertEquals("Department", row.getCell(4).getStringCellValue());
		assertEquals("Plan Type", row.getCell(5).getStringCellValue());
		assertEquals("Level of Coverage", row.getCell(6).getStringCellValue());
		
		assertEquals("Benefit Group\nStrategy 1", row.getCell(7).getStringCellValue());
		assertEquals("Benefit Group\nStrategy 2", row.getCell(8).getStringCellValue());
		assertEquals("Plan Name\nStrategy 1", row.getCell(9).getStringCellValue());
		assertEquals("Plan Name\nStrategy 2", row.getCell(10).getStringCellValue());
		assertEquals("Monthly WSE Cost\nStrategy 1", row.getCell(11).getStringCellValue());
		assertEquals("Monthly WSE Cost\nStrategy 2", row.getCell(12).getStringCellValue());
		assertEquals("Monthly WSE Dollar Difference\nStrategy 2 vs\nStrategy 1", row.getCell(13).getStringCellValue());
		assertEquals("Monthly WSE Percent Difference\nStrategy 2 vs\nStrategy 1", row.getCell(14).getStringCellValue());
		assertEquals("Monthly Company Cost\nStrategy 1", row.getCell(15).getStringCellValue());
		assertEquals("Monthly Company Cost\nStrategy 2", row.getCell(16).getStringCellValue());
		assertEquals("Monthly Company Dollar Difference\nStrategy 2 vs\nStrategy 1", row.getCell(17).getStringCellValue());
		assertEquals("Monthly Company Percent Difference\nStrategy 2 vs\nStrategy 1", row.getCell(18).getStringCellValue());
		assertEquals("Monthly Total Plan Cost\nStrategy 1", row.getCell(19).getStringCellValue());
		assertEquals("Monthly Total Plan Cost\nStrategy 2", row.getCell(20).getStringCellValue());
		assertEquals("Monthly Total Dollar Difference\nStrategy 2 vs\nStrategy 1", row.getCell(21).getStringCellValue());
		assertEquals("Monthly Total Percent Difference\nStrategy 2 vs\nStrategy 1", row.getCell(22).getStringCellValue());

	}

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<ModelCompareExportHelper> constructor =
                ModelCompareExportHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            org.junit.Assert.fail("Expected IllegalStateException");
        } catch (InvocationTargetException e) {
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }
	
	/*
	 * 
	 * Setup methods
	 * 
	 */
	private Map<Long, String> prepareStrategyMap() {
		Map<Long, String> strategyMap = new HashMap<>();
		strategyMap.put(STRATEGY_ID_1, "Strategy 1");
		strategyMap.put(STRATEGY_ID_2, "Strategy 2");
		return strategyMap;
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setDescription("MC Export Test Company");
		return company;
	}

	private List<ModelCompareStrategyCost> prepareModelCompareStrategyCostList() {

		List<ModelCompareStrategyCost> mcStrategyCostList = new ArrayList<>();

		// Strategy 1
		ModelCompareStrategyCost mcStrategyCost = new ModelCompareStrategyCost();
		ModelComparePlanTypeCost mcPlanTypeCost = new ModelComparePlanTypeCost();
		mcPlanTypeCost.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		mcPlanTypeCost.setCost(BigDecimal.valueOf(10000));

		mcStrategyCost.setStrategyId(STRATEGY_ID_1);
		mcStrategyCost.setPlanTypeCosts(Arrays.asList(mcPlanTypeCost));
		mcStrategyCostList.add(mcStrategyCost);

		// Strategy 2
		mcStrategyCost = new ModelCompareStrategyCost();
		mcPlanTypeCost = new ModelComparePlanTypeCost();
		mcPlanTypeCost.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		mcPlanTypeCost.setCost(BigDecimal.valueOf(20000));

		mcStrategyCost.setStrategyId(STRATEGY_ID_2);
		mcStrategyCost.setPlanTypeCosts(Arrays.asList(mcPlanTypeCost));
		mcStrategyCostList.add(mcStrategyCost);

		return mcStrategyCostList;
	}

	private List<EmployeeStrategyData> prepareEmployeeStrategyDataList() {
		List<EmployeeStrategyData> employeeStrategyDataList = new ArrayList<>();

		// EMPLOYEE 1
		// Strategy 1
		EmployeeStrategyData employeeStrategyData = new EmployeeStrategyData();
		List<EmployeeStrategyPlanData> employeeStrategyPlanDataList = new ArrayList<>();
		EmployeeStrategyPlanData employeeStrategyPlanData = new EmployeeStrategyPlanData();
		employeeStrategyPlanData.setStrategyId(STRATEGY_ID_1);
		List<BenefitPlanRateData> benefitPlanRateDataList = new ArrayList<>();
		BenefitPlanRateData benefitPlanRateData = new BenefitPlanRateData("MEDICALPLAN",
				BSSApplicationConstants.MEDICAL, "MEDICAL PLAN NAME", CoverageCodesEnums.COV_EMPLOYEE.getCode(),
				CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(100), BigDecimal.valueOf(900), null, "E",
				null, false, true);
		benefitPlanRateDataList.add(benefitPlanRateData);
		employeeStrategyPlanData.setBenefitPlans(benefitPlanRateDataList);
		employeeStrategyPlanDataList.add(employeeStrategyPlanData);

		// Strategy 2
		employeeStrategyPlanData = new EmployeeStrategyPlanData();
		employeeStrategyPlanData.setStrategyId(STRATEGY_ID_2);
		benefitPlanRateDataList = new ArrayList<>();
		benefitPlanRateData = new BenefitPlanRateData("MEDICALPLAN", BSSApplicationConstants.MEDICAL,
				"MEDICAL PLAN NAME", CoverageCodesEnums.COV_EMPLOYEE.getCode(),
				CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(100), BigDecimal.valueOf(900), null, "E",
				null, false, true);
		benefitPlanRateDataList.add(benefitPlanRateData);
		employeeStrategyPlanData.setBenefitPlans(benefitPlanRateDataList);
		employeeStrategyPlanDataList.add(employeeStrategyPlanData);

		employeeStrategyData.setEmplFirstName("FIRSTNAME");
		employeeStrategyData.setEmplLastName("LASTNAME");
		employeeStrategyData.setStrategyDetails(employeeStrategyPlanDataList);
		employeeStrategyDataList.add(employeeStrategyData);

		// EMPLOYEE 2
		// Strategy 1
		employeeStrategyData = new EmployeeStrategyData();
		employeeStrategyPlanDataList = new ArrayList<>();
		employeeStrategyPlanData = new EmployeeStrategyPlanData();
		employeeStrategyPlanData.setStrategyId(STRATEGY_ID_1);
		benefitPlanRateDataList = new ArrayList<>();
		benefitPlanRateData = new BenefitPlanRateData(null,
				BSSApplicationConstants.MEDICAL, null, null,
				null, null, null, null, "W",
				null, false, true);
		benefitPlanRateDataList.add(benefitPlanRateData);
		employeeStrategyPlanData.setBenefitPlans(benefitPlanRateDataList);
		employeeStrategyPlanDataList.add(employeeStrategyPlanData);

		// Strategy 2
		employeeStrategyPlanData = new EmployeeStrategyPlanData();
		employeeStrategyPlanData.setStrategyId(STRATEGY_ID_2);
		benefitPlanRateDataList = new ArrayList<>();
		benefitPlanRateData = new BenefitPlanRateData("MEDICALPLAN", BSSApplicationConstants.MEDICAL,
				"MEDICAL PLAN NAME", CoverageCodesEnums.COV_EMPLOYEE.getCode(),
				CoverageCodesEnums.COV_EMPLOYEE.getName(), BigDecimal.valueOf(100), BigDecimal.valueOf(900), null, "N",
				null, false, true);
		benefitPlanRateDataList.add(benefitPlanRateData);
		employeeStrategyPlanData.setBenefitPlans(benefitPlanRateDataList);
		employeeStrategyPlanDataList.add(employeeStrategyPlanData);

		employeeStrategyData.setEmplFirstName("FIRSTNAME2");
		employeeStrategyData.setEmplLastName("LASTNAME2");
		employeeStrategyData.setStrategyDetails(employeeStrategyPlanDataList);		
		employeeStrategyDataList.add(employeeStrategyData);
		return employeeStrategyDataList;
	}

}
