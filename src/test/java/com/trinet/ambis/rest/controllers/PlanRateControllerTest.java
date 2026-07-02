package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.model.PlanRatesExportData;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.FileUtils;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class PlanRateControllerTest extends ServiceUnitTest {

	@InjectMocks
	PlanRateController planRateController;

	@Mock
	CompanyService companyService;

	@Mock
	PlanRatesService planRatesService;

	@Mock
	HttpServletRequest mockRequest;

	@Mock
	HttpServletResponse mockResponse;

	@Mock
	private ServletOutputStream outputStream;

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<CommonServiceHelper> mockStaticCommonServiceHelper;
	private MockedStatic<FileUtils> mockStaticFileUtils;

	private static final String EMPLID = "0000000123456";

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticCommonServiceHelper = Mockito.mockStatic(CommonServiceHelper.class);
		mockStaticFileUtils = Mockito.mockStatic(FileUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) mockStaticBSSSecurityUtils.close();
		if (mockStaticCommonServiceHelper != null) mockStaticCommonServiceHelper.close();
		if (mockStaticFileUtils != null) mockStaticFileUtils.close();
	}

	@Test
	public void planRatesDownloadToExcel() throws IOException {

		final ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);

		/*
		 * Test success
		 */
		when(mockRequest.getParameter("data")).thenReturn("DATA");
		when(mockResponse.getOutputStream()).thenReturn(outputStream);

		planRateController.planRatesDownloadToExcel(mockRequest, mockResponse);
	}

	@Test
	public void getPlanRatesForExport() {

		HttpServletRequest request;
		String companyCode = "TEST";
		PlanRatesExportData expectedResult = new PlanRatesExportData();
		PlanRatesExportData actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(planRatesService.getPlanRatesExportData(company)).thenReturn(expectedResult);

		actualResult = planRateController.getPlanRatesForExport(request, companyCode, "");
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testGeneratePlanRatesExcelFile_Success() throws Exception {
		Company company = new Company();
		company.setName("Test &Co");
		Workbook workbook = Mockito.mock(Workbook.class);

		// Arrange
		String companyCode = "COMP123";
		String hiddenColumns = "col1,col2";
		String requestData = "{\"someField\":\"someValue\"}";

		PlanRatesExportData mockExportData = new PlanRatesExportData();

		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);

		when(CommonServiceHelper.jsonToObject(requestData, PlanRatesExportData.class))
				.thenReturn(mockExportData);

		when(planRatesService.getPlanRatesExcelWorkbook(company, mockExportData, hiddenColumns)).thenReturn(workbook);

		when(FileUtils.removeSpecialCharacters("Test &Co")).thenReturn("Test_Co");

		when(mockResponse.getOutputStream()).thenReturn(outputStream);

		// Act
		planRateController.generatePlanRatesExcelFile(mockRequest, mockResponse, companyCode, hiddenColumns,
				requestData, "");

		// Assert
		verify(mockResponse).setHeader(eq("Content-disposition"), eq("attachment; filename=Test_Co_Plan_Rates.xlsx"));
		verify(mockResponse).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		verify(workbook).write(outputStream);
	}
	
	@Test
	public void getProspectPlanRatesForExport() {
		HttpServletRequest request;
		String companyCode = "TEST";
		String exchangeId = "TNIII";
		PlanRatesExportData expectedResult = new PlanRatesExportData();
		PlanRatesExportData actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.getByExchangeId(exchangeId)))
				.thenReturn(company);
		when(planRatesService.getPlanRatesExportData(company)).thenReturn(expectedResult);

		actualResult = planRateController.getPlanRatesForExport(request, companyCode, exchangeId);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void testProspectGeneratePlanRatesExcelFile() throws Exception {
		Company company = new Company();
		company.setName("Prospect_test");
		Workbook workbook = Mockito.mock(Workbook.class);

		// Arrange
		String companyCode = "prospect123";
		String hiddenColumns = "col1,col2";
		String requestData = "{\"someField\":\"someValue\"}";
		String exchangeId = "TNIII";

		PlanRatesExportData mockExportData = new PlanRatesExportData();

		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.getByExchangeId(exchangeId)))
				.thenReturn(company);

		when(CommonServiceHelper.jsonToObject(requestData, PlanRatesExportData.class)).thenReturn(mockExportData);

		when(planRatesService.getPlanRatesExcelWorkbook(company, mockExportData, hiddenColumns)).thenReturn(workbook);

		when(FileUtils.removeSpecialCharacters(company.getName())).thenReturn("Prospect_test");

		when(mockResponse.getOutputStream()).thenReturn(outputStream);

		// Act
		planRateController.generatePlanRatesExcelFile(mockRequest, mockResponse, companyCode, hiddenColumns,
				requestData, exchangeId);

		// Assert
		verify(mockResponse).setHeader(eq("Content-disposition"),
				eq("attachment; filename=Prospect_test_Plan_Rates.xlsx"));
		verify(mockResponse).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		verify(workbook).write(outputStream);
	}
}
