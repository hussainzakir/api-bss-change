package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.ModelCompareService;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.ModelCompareGroupHeadcount;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.model.StrategyBenefitPlanHeadCount;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.FileUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class ModelCompareControllerTest extends ServiceUnitTest {

	@InjectMocks
	ModelCompareController modelCompareController;

	@Mock
	CompanyService companyService;

	@Mock
	ModelCompareService modelCompareService;

	@Mock
	EmployeeDataService employeeDataService;

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<FileUtils> mockStaticFileUtils;
	private static final String EMPLID = "0000000123456";
	private static final String EMPLID_SYS_ACCT = "00000000000";

	boolean isHistory = false;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::checkSystemAccount).thenReturn(false);
		mockStaticFileUtils = Mockito.mockStatic(FileUtils.class);
	}

	@After
	public void tearDown() {
		mockStaticBSSSecurityUtils.close();
		mockStaticFileUtils.close();
	}

	@Test
	public void getStrategies() {
		HttpServletRequest request;
		Company company = prepareCompany();
		String companyCode = "TEST";
		List<ModelCompareStrategy> expectedResult = new ArrayList<>();
		List<ModelCompareStrategy> actualResult;

		request = new MockHttpServletRequest();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);

		when(modelCompareService.getMCStrategies(company,false)).thenReturn(expectedResult);

		actualResult = modelCompareController.getStrategies(request, companyCode, null);
		assertEquals(expectedResult, actualResult);
		
		
		when(BSSSecurityUtils.checkSystemAccount()).thenReturn(true);
		when(companyService.getCompanyDetails(companyCode, false,EMPLID_SYS_ACCT, null)).thenReturn(company);

		when(modelCompareService.getMCStrategies(company,false)).thenReturn(expectedResult);

		actualResult = modelCompareController.getStrategies(request, companyCode, null);
		assertEquals(expectedResult, actualResult);
			
	}

	@Test
	public void getStrategiesForProspect() {
		HttpServletRequest request;
		Company company = prepareCompany();
		String companyCode = "PROSPECT";
		List<ModelCompareStrategy> expectedResult = new ArrayList<>();
		List<ModelCompareStrategy> actualResult;

		request = new MockHttpServletRequest();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.TRINET_III)).thenReturn(company);

		when(modelCompareService.getMCStrategies(company, false)).thenReturn(expectedResult);

		actualResult = modelCompareController.getStrategies(request, companyCode, BenExchngEnums.TRINET_III.getExchangeId());
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategyPlanCosts() {

		List<Long> strategyIdList = Arrays.asList(100L, 200L, 300L);
		Company company = new Company();
		String companyCode = "ABC";
		List<ModelCompareStrategyCost> expectedResult = new ArrayList<>();
		List<ModelCompareStrategyCost> actualResult;
		HttpServletRequest request = new MockHttpServletRequest();


		actualResult = modelCompareController.getStrategyPlanCosts(request, strategyIdList, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategyPlanCostsForProspect() {

		List<Long> strategyIdList = Arrays.asList(0L, 200L, 300L);
		Company company = new Company();
		String companyCode = "PROSPECT";
		List<ModelCompareStrategyCost> expectedResult = new ArrayList<>();
		List<ModelCompareStrategyCost> actualResult;
		HttpServletRequest request = new MockHttpServletRequest();


		actualResult = modelCompareController.getStrategyPlanCosts(request, strategyIdList, companyCode, BenExchngEnums.TRINET_III.getExchangeId());
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategyGroupFunding() {
		Company company = new Company();
		long strategyId = 100;
		String companyCode = "TEST";
		ModelCompareStrategy expectedResult = new ModelCompareStrategy();
		ModelCompareStrategy actualResult;
		HttpServletRequest request = new MockHttpServletRequest();
		when(companyService.getCompanyDetails(companyCode, isHistory, EMPLID, null)).thenReturn(company);
		when(modelCompareService.getMCStrategyGroupFunding(strategyId, company)).thenReturn(expectedResult);

		actualResult = modelCompareController.getStrategyGroupFunding(request, strategyId, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategiesEmployeeCostData() {
		HttpServletRequest request = new MockHttpServletRequest();
		Long currentStrategyId = 1L;
		List<Long> strategyList = Arrays.asList(2L, 3L);
		String strategyIdList = "1,2,3";
		String companyCode = "TEST";
		List<EmployeeStrategyData> expectedResult = new ArrayList<>();
		List<EmployeeStrategyData> actualResult;


		actualResult = modelCompareController.getStrategiesEmployeeCostData(request, strategyIdList, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategiesEmployeeCostDataForProspect() {
		HttpServletRequest request = new MockHttpServletRequest();
		Long currentStrategyId = 0L;
		List<Long> strategyList = Arrays.asList(2L, 3L);
		String strategyIdList = "1,2,3";
		String companyCode = "PROSPECT";
		String exchangeId = "TNIII";
		List<EmployeeStrategyData> expectedResult = new ArrayList<>();
		List<EmployeeStrategyData> actualResult;
		Company company = new Company();
		company.setProspectCompany(true);

		when(companyService.getCompanyDetails(companyCode, isHistory, EMPLID, BenExchngEnums.getByExchangeId( exchangeId ))).thenReturn(company);

		actualResult = modelCompareController.getStrategiesEmployeeCostData(request, strategyIdList, companyCode, exchangeId);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getPlanStrategyHeadcount() {
		List<Long> strategyList = Arrays.asList(1L, 2L, 3L);
		String companyCode = "TEST";
		List<StrategyBenefitPlanHeadCount> expectedResult = new ArrayList<>();
		List<StrategyBenefitPlanHeadCount> actualResult;
		Company company = new Company();

		HttpServletRequest request = new MockHttpServletRequest();

		when(companyService.getCompanyDetails(companyCode, isHistory, EMPLID, null)).thenReturn(company);
		when(modelCompareService.getMCPlanStrategyCoverageHeadcount(strategyList, company)).thenReturn(expectedResult);

		actualResult = modelCompareController.getPlanStrategyHeadcount(request, strategyList, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategiesGroupHeadcountCostData() {
		HttpServletRequest request = new MockHttpServletRequest();
		List<Long> strategyList = Arrays.asList(1L, 2L, 3L);
		String companyCode = "TEST";
		List<ModelCompareGroupHeadcount> expectedResult = new ArrayList<>();
		List<ModelCompareGroupHeadcount> actualResult;

		Company company = new Company();
		when(companyService.getCompanyDetails(companyCode, isHistory, EMPLID, null)).thenReturn(company);
		when(modelCompareService.getMCStrategyHeadcountCostByGroup(strategyList, company)).thenReturn(expectedResult);

		actualResult = modelCompareController.getStrategiesGroupHeadcountCostData(request, strategyList, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	//@Test
	public void getModelCompareExcelFile() throws IOException {
		HttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = mock(HttpServletResponse.class);
		ServletOutputStream outputStream = mock(ServletOutputStream.class);
		String strategyIdListString = "1,2,3";
		String companyCode = "TEST";
		Company company = new Company();
		company.setDescription("TEST COMPANY");
		Workbook workbook = new XSSFWorkbook();

		when(companyService.getCompanyDetails(companyCode, isHistory, EMPLID, null)).thenReturn(company);
		when(modelCompareService.getModelCompareExcelWorkbook(company, 1L, Arrays.asList(2L, 3L))).thenReturn(workbook);
		when(response.getOutputStream()).thenReturn(outputStream);
		when(FileUtils.removeSpecialCharacters("TEST COMPANY")).thenReturn("TEST_COMPANY");
		
		modelCompareController.getModelCompareExcelFile(request, response, strategyIdListString, companyCode, null);
	}

	/*
	 * 
	 * Setup methods
	 * 
	 */
	private Company prepareCompany() {

		return new Company();
	}

}
