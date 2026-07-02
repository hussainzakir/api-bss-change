package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.Response;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
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
public class EmployeeControllerTest extends ServiceUnitTest {

	@InjectMocks
	EmployeeController employeeController;

	@Mock
	CompanyService companyService;

	@Mock
	EmployeeDataService employeeDataService;

	@Mock
	StrategyService strategyService;
	
	@Mock
	StrategySyncService strategySyncService;

	private static final String EMPLID = "0000000123456";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
	}

	@org.junit.After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
		}
	}

	@Test
	public void getEmployeeData() throws JsonProcessingException {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		Set<EmployeeData> expectedResult = new HashSet<EmployeeData>();
		Set<EmployeeData> actualResult;
		Company company;

		/*
		 * Test with null session
		 */
		request = Mockito.mock(HttpServletRequest.class);

		actualResult = employeeController.getEmployeeData(request, strategyId, companyCode, null);
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with not null session and null trinetAuthEmplId
		 */
		request = new MockHttpServletRequest();

		actualResult = employeeController.getEmployeeData(request, strategyId, companyCode, null);
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with not null company
		 */
		company = new Company();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(employeeDataService.getEmployeesData(company, strategyId)).thenReturn(expectedResult);

		actualResult = employeeController.getEmployeeData(request, strategyId, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void assignEmployeesToGroup() throws JsonProcessingException {

		HttpServletRequest request;
		EmployeeAssignmentData employeeAssignmentData = new EmployeeAssignmentData();
		long strategyId = 1000L;
		String companyCode = "D2S";
		Set<EmployeeData> employeeData = new HashSet<EmployeeData>();
		Response actualResult;
		Company company;

		/*
		 * Test with null company
		 */
		request = new MockHttpServletRequest();
		company = null;
		String exchangeId ="3";

		actualResult = employeeController.updateEmployeeAssignment(request, employeeAssignmentData, strategyId,
				companyCode, exchangeId);
		assertEquals(Boolean.FALSE, actualResult.getResult());

		/*
		 * Test with not null company
		 */
		company = new Company();
		company.setCode(companyCode);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);

		doNothing().when(employeeDataService).updateEmployeeAssignment(company, employeeAssignmentData, strategyId);
		doNothing().when(strategySyncService).syncStrategyData(company, strategyId);

		actualResult = employeeController.updateEmployeeAssignment(request, employeeAssignmentData, strategyId,
				companyCode, exchangeId);
		assertEquals(Boolean.TRUE, actualResult.getResult());
		verify(employeeDataService, times(1)).updateEmployeeAssignment(company, employeeAssignmentData, strategyId);
		verify(strategySyncService, times(1)).syncStrategyData(company, strategyId);
	}

	@Test
	public void updateProspectEmployeeAssignment() {
		// given
		long strategyId = 0;
		String companyCode = "a1b2c3";
		String exchangeId = "3";
		Company company = new Company();
		company.setCode(companyCode);
		company.setProspectCompany(true);
		HttpServletRequest request = new MockHttpServletRequest();
		EmployeeAssignmentData employeeAssignmentData = new EmployeeAssignmentData();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		doNothing().when(employeeDataService).updateEmployeeAssignment(company, employeeAssignmentData, strategyId);
		// when
		Response actualResult = employeeController.updateEmployeeAssignment(request, employeeAssignmentData, strategyId,
				companyCode, exchangeId);
		// then
		assertEquals(Boolean.TRUE, actualResult.getResult());
		verify(employeeDataService, times(1)).updateEmployeeAssignment(company, employeeAssignmentData, strategyId);
		verify(strategySyncService, times(0)).syncStrategyData(any(), anyLong());
	}
	
}
