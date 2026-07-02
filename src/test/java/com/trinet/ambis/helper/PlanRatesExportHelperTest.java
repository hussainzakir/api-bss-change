package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.AdditionalPlanOptionPlanExport;
import com.trinet.ambis.service.model.AdditionalPlanOptionsExport;
import com.trinet.ambis.service.model.HealthPlanRatesExportPlan;
import com.trinet.ambis.service.model.PlanRatesExportData;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;


@RunWith(MockitoJUnitRunner.class)
public class PlanRatesExportHelperTest extends ServiceUnitTest {

	private static final String DISABILITY_BUNDLE_50 = "50% Disability Co Paid";
	private static final String DISABILITY_BUNDLE_60 = "60% STD 1500 Co Pd (Enhanced) & 60% LTD 10000 Co Pd";

    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        rulesAndConfigsUtilsMockedStatic = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        if (rulesAndConfigsUtilsMockedStatic != null) {
            rulesAndConfigsUtilsMockedStatic.close();
            rulesAndConfigsUtilsMockedStatic = null;
        }
    }

	@Test
	public void constructPlanRatesWorkbookMappingDisabled() {

		Workbook workbook = new XSSFWorkbook();
		Company currentCompany = prepareCurrentCompany();
		Company futureCompany = prepareFutureCompany();
		
		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(false);

		PlanRatesExportHelper.constructPlanRatesWorkbook(futureCompany,
				getPlanRatesExportData(futureCompany, currentCompany), workbook, "");

		assertEquals(5, workbook.getNumberOfSheets());
		assertEquals(4, workbook.getSheet("Medical").getLastRowNum());
		assertEquals(4, workbook.getSheet("Dental").getLastRowNum());
		assertEquals(4, workbook.getSheet("Vision").getLastRowNum());
		assertEquals(5, workbook.getSheet("Life").getLastRowNum());
		assertEquals(10, workbook.getSheet("Disability").getLastRowNum());
		assertEquals( DISABILITY_BUNDLE_50, workbook.getSheet("Disability").getRow( 3 ).getCell( 0 ).toString() );
		assertEquals( DISABILITY_BUNDLE_60, workbook.getSheet("Disability").getRow( 7 ).getCell( 0 ).toString() );
		

		// For test only
		// PlanRatesExportHelper.saveExcel(workbook);
	}

	@Test
	public void constructPlanRatesWorkbookMappingEnabled() {

		Workbook workbook = new XSSFWorkbook();
		Company currentCompany = prepareCurrentCompany();
		Company futureCompany = prepareFutureCompany();
		
		when(RulesAndConfigsUtils.isPlanRateMappingEnabled(futureCompany.getRealmPlanYearId())).thenReturn(true);

		PlanRatesExportHelper.constructPlanRatesWorkbook(futureCompany,
				getPlanRatesExportData(futureCompany, currentCompany), workbook, "");

		assertEquals(5, workbook.getNumberOfSheets());
		assertEquals(4, workbook.getSheet("Medical").getLastRowNum());
		assertEquals(4, workbook.getSheet("Dental").getLastRowNum());
		assertEquals(4, workbook.getSheet("Vision").getLastRowNum());
		assertEquals(5, workbook.getSheet("Life").getLastRowNum());
		assertEquals(10, workbook.getSheet("Disability").getLastRowNum());
		assertEquals( DISABILITY_BUNDLE_50, workbook.getSheet("Disability").getRow( 3 ).getCell( 0 ).toString() );
		assertEquals( DISABILITY_BUNDLE_60, workbook.getSheet("Disability").getRow( 7 ).getCell( 0 ).toString() );
		

		// For test only
		// PlanRatesExportHelper.saveExcel(workbook);
	}

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<?> constructor = PlanRatesExportHelper.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            org.junit.Assert.fail("Expected IllegalStateException");
        } catch (InvocationTargetException e) {
            org.junit.Assert.assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }
	
	private PlanRatesExportData getPlanRatesExportData(Company futureCompany, Company currentCompany) {

		Map<String, List<HealthPlanRatesExportPlan>> healthPlanData = Maps.newHashMap();
		List<HealthPlanRatesExportPlan> healthPlanRatesExportPlans = contructHealthPlanRates();
		healthPlanData.put("medical", healthPlanRatesExportPlans);
		healthPlanData.put("dental", healthPlanRatesExportPlans);
		healthPlanData.put("vision", healthPlanRatesExportPlans);

		Map<String, List<AdditionalPlanOptionsExport>> additionalPlanData = Maps.newHashMap();

		additionalPlanData.put("LIFE", contructLifeForExport() );
		additionalPlanData.put("DISABILITY", contructDisabilityForExport() );

		currentCompany.setRenewalCompany(true);
		PlanRatesExportData planRatesExportData = new PlanRatesExportData();
		planRatesExportData.setCurrentStartDate(Utils.convertDateToString(currentCompany.getRealmPlanYear().getPlanYearStart()));
		planRatesExportData.setCurrentEndDate(Utils.convertDateToString(currentCompany.getRealmPlanYear().getPlanYearEnd()));
		planRatesExportData.setFutureStartDate(Utils.convertDateToString(futureCompany.getRealmPlanYear().getPlanYearStart()));
		planRatesExportData.setFutureEndDate(Utils.convertDateToString(futureCompany.getRealmPlanYear().getPlanYearEnd()));

		planRatesExportData.setHealthPlanData(healthPlanData);
		planRatesExportData.setAdditionalPlanData(additionalPlanData);

		return planRatesExportData;
	}

	private List<AdditionalPlanOptionsExport> contructLifeForExport() {
		List<AdditionalPlanOptionsExport> additionalPlanDataLife = Lists.newArrayList();

		String jsonString = "{\"id\":\"LIFE01\",\"name\":\"Good Life\",\"planType\":\"23\",\"offeredYearsFlag\":\"B\",\"offeredStates\":null,\"currentUnit\":\"$1,000 of covered payroll\",\"futureUnit\":\"$1,000 of covered payroll\",\"currentCost\":0.13,\"futureCost\":0.13,\"optionPlans\":null}";
		AdditionalPlanOptionsExport lifePlanForExport = CommonServiceHelper.jsonToObject( jsonString, AdditionalPlanOptionsExport.class );
		additionalPlanDataLife.add( lifePlanForExport );

		jsonString = "{\"id\":\"LIFE02\",\"name\":\"Extra Life\",\"planType\":\"23\",\"offeredYearsFlag\":\"B\",\"offeredStates\":null,\"currentUnit\":\"$1,000 of covered payroll\",\"futureUnit\":\"$1,000 of covered payroll\",\"currentCost\":0.13,\"futureCost\":0.13,\"optionPlans\":null}";
		lifePlanForExport = CommonServiceHelper.jsonToObject( jsonString, AdditionalPlanOptionsExport.class );
		additionalPlanDataLife.add( lifePlanForExport );

		jsonString = "{\"id\":\"LIFE03\",\"name\":\"Super Life\",\"planType\":\"23\",\"offeredYearsFlag\":\"B\",\"offeredStates\":null,\"currentUnit\":\"$1,000 of covered payroll\",\"futureUnit\":\"$1,000 of covered payroll\",\"currentCost\":0.13,\"futureCost\":0.13,\"optionPlans\":null}";
		lifePlanForExport = CommonServiceHelper.jsonToObject( jsonString, AdditionalPlanOptionsExport.class );
		additionalPlanDataLife.add( lifePlanForExport );

		return additionalPlanDataLife;
	}

	private List<AdditionalPlanOptionsExport> contructDisabilityForExport() {
		List<AdditionalPlanOptionsExport> additionalPlanDataDisability = Lists.newArrayList();

		AdditionalPlanOptionsExport additionalPlanOptionsExport = new AdditionalPlanOptionsExport();
		additionalPlanOptionsExport.setName( DISABILITY_BUNDLE_50 );
		additionalPlanOptionsExport.setOptionPlans( Lists.newArrayList() );

		String jsonString = "{\"name\":\"50% LTD 7500 Co Pd\",\"planType\":\"LTD\",\"offeredStatesString\":\"All States\",\"currentUnit\":null,\"futureUnit\":\"$100 of covered payroll\",\"currentCost\":null,\"futureCost\":0.1723,\"sdiPlan\":false}";
		AdditionalPlanOptionPlanExport disabilityPlanForExport = CommonServiceHelper.jsonToObject( jsonString, AdditionalPlanOptionPlanExport.class );
		additionalPlanOptionsExport.getOptionPlans().add( disabilityPlanForExport );

		jsonString = "{\"name\":\"50% STD 1750 Co Pd\",\"planType\":\"STD\",\"offeredStatesString\":\"Other States not in CA, HI, MA, NJ, PR, RI, WA\",\"currentUnit\":null,\"futureUnit\":\"$100 of covered payroll\",\"currentCost\":null,\"futureCost\":0.0405,\"sdiPlan\":false}";
		disabilityPlanForExport = CommonServiceHelper.jsonToObject( jsonString, AdditionalPlanOptionPlanExport.class );
		additionalPlanOptionsExport.getOptionPlans().add( disabilityPlanForExport );

		additionalPlanDataDisability.add( additionalPlanOptionsExport );


		additionalPlanOptionsExport = new AdditionalPlanOptionsExport();
		additionalPlanOptionsExport.setName( DISABILITY_BUNDLE_60 );
		additionalPlanOptionsExport.setOptionPlans( Lists.newArrayList() );

		jsonString = "{\"name\":\"60% LTD 10000 Co Pd\",\"planType\":\"LTD\",\"offeredStatesString\":\"All States\",\"currentUnit\":null,\"futureUnit\":\"$100 of covered payroll\",\"currentCost\":null,\"futureCost\":0.1723,\"sdiPlan\":false}";
		disabilityPlanForExport = CommonServiceHelper.jsonToObject( jsonString, AdditionalPlanOptionPlanExport.class );
		additionalPlanOptionsExport.getOptionPlans().add( disabilityPlanForExport );

		jsonString = "{\"name\":\"60% STD 1500 Co Pd (Enhanced)\",\"planType\":\"STD\",\"offeredStatesString\":\"Other States not in CA, HI, MA, NJ, PR, RI, WA\",\"currentUnit\":\"Amount per benefits eligible worksite employee\",\"futureUnit\":\"Amount per benefits eligible worksite employee\",\"currentCost\":10.67,\"futureCost\":10.67,\"sdiPlan\":false}";
		disabilityPlanForExport = CommonServiceHelper.jsonToObject( jsonString, AdditionalPlanOptionPlanExport.class );
		additionalPlanOptionsExport.getOptionPlans().add( disabilityPlanForExport );

		additionalPlanDataDisability.add( additionalPlanOptionsExport );


		return additionalPlanDataDisability;
	}

	private List<HealthPlanRatesExportPlan> contructHealthPlanRates() {

		List<HealthPlanRatesExportPlan> healthPlanRatesExportPlans = Lists.newArrayList();

		HealthPlanRatesExportPlan healthPlanRatesExportPlan = new HealthPlanRatesExportPlan();
		healthPlanRatesExportPlan.setCurrentId("0");
		healthPlanRatesExportPlan.setCurrentName("PLAN_1");
		healthPlanRatesExportPlan.setPlanType("HEALTH_PLAN");
		healthPlanRatesExportPlan.setHasHeadcount(true);
		healthPlanRatesExportPlan.setOfferedYearsFlag("");
		healthPlanRatesExportPlan.setOfferedStates(Arrays.asList("NJ", "NY", "CT"));
		healthPlanRatesExportPlan.setEmployeeOnlyCurrentCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeSpouseCurrentCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeChildCurrentCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeFamilyCurrentCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeOnlyFutureCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeSpouseFutureCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeChildFutureCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeFamilyFutureCost(new BigDecimal(100));
		healthPlanRatesExportPlan.setEmployeeOnlyCurrentHeadcount(10L);
		healthPlanRatesExportPlan.setEmployeeSpouseCurrentHeadcount(10L);
		healthPlanRatesExportPlan.setEmployeeChildCurrentHeadcount(10L);
		healthPlanRatesExportPlan.setEmployeeFamilyCurrentHeadcount(10L);
		healthPlanRatesExportPlans.add(healthPlanRatesExportPlan);

		healthPlanRatesExportPlan = new HealthPlanRatesExportPlan();
		healthPlanRatesExportPlan.setCurrentId("1");
		healthPlanRatesExportPlan.setCurrentName("PLAN_2");
		healthPlanRatesExportPlan.setPlanType("HEALTH_PLAN");
		healthPlanRatesExportPlan.setHasHeadcount(true);
		healthPlanRatesExportPlan.setOfferedYearsFlag("");
		healthPlanRatesExportPlan.setOfferedStates(Arrays.asList("NJ", "NY", "CT"));
		healthPlanRatesExportPlan.setEmployeeOnlyCurrentCost(null);
		healthPlanRatesExportPlan.setEmployeeSpouseCurrentCost(null);
		healthPlanRatesExportPlan.setEmployeeChildCurrentCost(null);
		healthPlanRatesExportPlan.setEmployeeFamilyCurrentCost(null);
		healthPlanRatesExportPlan.setEmployeeOnlyFutureCost(null);
		healthPlanRatesExportPlan.setEmployeeSpouseFutureCost(null);
		healthPlanRatesExportPlan.setEmployeeChildFutureCost(null);
		healthPlanRatesExportPlan.setEmployeeFamilyFutureCost(null);
		healthPlanRatesExportPlan.setEmployeeOnlyCurrentHeadcount(null);
		healthPlanRatesExportPlan.setEmployeeSpouseCurrentHeadcount(null);
		healthPlanRatesExportPlan.setEmployeeChildCurrentHeadcount(null);
		healthPlanRatesExportPlan.setEmployeeFamilyCurrentHeadcount(null);
		healthPlanRatesExportPlans.add(healthPlanRatesExportPlan);

		return healthPlanRatesExportPlans;
	}

	private Company prepareCurrentCompany() {
		Company company = new Company();
		company.setDescription("Plan Rates Export Test Company");
		RealmPlanYear realmPlanYear = new RealmPlanYear(0, 20, "AL", 50, true, true,
				new GregorianCalendar(2019, Calendar.JANUARY, 1).getTime(),
				new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(), "", BigDecimal.TEN, BigDecimal.ONE, 0, "",
				true);
		company.setRealmPlanYear(realmPlanYear);
		return company;
	}

	private Company prepareFutureCompany() {
		Company company = new Company();
		company.setDescription("Plan Rates Export Test Company");
		RealmPlanYear realmPlanYear = new RealmPlanYear(0, 20, "AL", 50, true, true,
				new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime(),
				new GregorianCalendar(2021, Calendar.JANUARY, 1).getTime(), "", BigDecimal.TEN, BigDecimal.ONE, 0, "",
				true);
		company.setRealmPlanYear(realmPlanYear);
		company.setRenewalCompany(true);
		return company;
	}
}
