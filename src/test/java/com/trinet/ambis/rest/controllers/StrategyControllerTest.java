package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trinet.ambis.client.DefaultPlanMappingServiceClient;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.rest.controllers.dto.CreateStrategiesRequest;
import com.trinet.ambis.rest.controllers.dto.StrategyCostRes;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.model.StrategyBudget;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.prospect.ProspectSubmitService;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.validator.RequestValidator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class StrategyControllerTest extends ServiceUnitTest {

	@InjectMocks
	StrategyController strategyController;

	@Mock
	CompanyService companyService;

	@Mock
	StrategyService strategyService;

	@Mock
	SubmitStatusService submitStatusService;

	@Mock
	ProcessStatusService processStatusService;

	@Mock
	QueuedSubmitService queuedSubmitService;

	@Mock
	AppRulesConfigService appRulesConfigService;

	@Mock
	SubmitService submitService;

	@Mock
	ProspectSubmitService prospectSubmitService;

	@Mock
	HttpServletRequest httpRequest;

	private static final String EMPLID = "0000000123456";

	private static final String EMPLID_SYS_ACCT = "00000000000";

	private static final String API_URL_STRATEGY_BUDGET_API = URIConstants.VERSION_AND_ROOT
			+ URIConstants.STRATEGY_BUDGET_FACTOR;

	private MockMvc mockMvc;
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<RequestValidator> mockStaticRequestValidator;

	@Before
	public void setUp() {

		if (mockStaticBSSSecurityUtils == null) {
			mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
			mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
			mockStaticBSSSecurityUtils.when(BSSSecurityUtils::checkSystemAccount).thenReturn(false);
		}
		if (mockStaticRequestValidator == null) {
			mockStaticRequestValidator = Mockito.mockStatic(RequestValidator.class);
		}

		AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
		mockMvc = MockMvcBuilders.standaloneSetup(strategyController).build();
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
		if (mockStaticRequestValidator != null) {
			mockStaticRequestValidator.close();
			mockStaticRequestValidator = null;
		}
	}

	@Test
	public void createStrategy() {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		StrategyData dto = prepareStrategyData();
		StrategyData expectedResult = new StrategyData();
		StrategyData actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test success
		 */
		company = new Company();
		company.setProspectCompany(false);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.createUpdateStrategy(dto, company, false)).thenReturn(strategyId);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(expectedResult);
		when(RequestValidator.getValidatedStrategyName(dto.getStrategySummary().getName()))
				.thenReturn(dto.getStrategySummary().getName());

		actualResult = strategyController.createStrategy(request, dto, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	// Submit false
	@Test
	public void createCustomStrategy() throws JsonProcessingException {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = "";
		StrategyData dto = prepareStrategyData();
		long id = 0;
		StrategyData createStrategy;
		StrategyData actualResult;
		Company company;
		Map<String, String> appConfigRules = new HashMap<>();
		appConfigRules.put("SUBMIT_QUE_ENABLED", "true");

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectCompany(false);
		createStrategy = new StrategyData();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.getByExchangeId(exchangeId)))
				.thenReturn(company);
		when(strategyService.createUpdateStrategy(dto, company, false)).thenReturn(strategyId);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(createStrategy);

		actualResult = strategyController.createCustomStrategy(request, dto, id, companyCode, exchangeId);

		verify(queuedSubmitService, times(0)).createSubmitProcess(company, createStrategy,
				ProcessStatusEnum.SUBMIT_PROCESS.getProcessName(), true);
		verify(submitService, times(0)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());
		assertEquals(createStrategy, actualResult);
	}

	/*
	 * Submit true and queue disabled
	 */
	@Test
	public void createCustomStrategy_1() throws JsonProcessingException {
		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = "TNIII";
		StrategyData dto = prepareStrategyData();
		long id = 0;
		StrategyData createStrategy;
		StrategyData actualResult;
		Company company;
		Map<String, String> appConfigRules = new HashMap<>();
		appConfigRules.put("SUBMIT_QUE_ENABLED", "false");

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectCompany(false);
		dto.getStrategySummary().setSubmitted(true);
		createStrategy = new StrategyData();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.getByExchangeId(exchangeId)))
				.thenReturn(company);
		when(strategyService.createUpdateStrategy(dto, company, false)).thenReturn(strategyId);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(createStrategy);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(appConfigRules);

		actualResult = strategyController.createCustomStrategy(request, dto, id, companyCode, exchangeId);

		verify(queuedSubmitService, times(0)).createSubmitProcess(company, createStrategy,
				ProcessStatusEnum.SUBMIT_PROCESS.getProcessName(), true);
		verify(submitService, times(1)).submit(company, createStrategy, EMPLID, true, false);
		assertEquals(createStrategy, actualResult);
	}

	/*
	 * Submit true and queue enabled
	 */
	@Test
	public void createCustomStrategy_2() throws JsonProcessingException {
		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = "TNIII";
		StrategyData dto = prepareStrategyData();
		long id = 0;
		StrategyData createStrategy;
		StrategyData actualResult;
		Company company;
		Map<String, String> appConfigRules = new HashMap<>();
		appConfigRules.put("SUBMIT_QUE_ENABLED", "true");

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectCompany(false);
		dto.getStrategySummary().setSubmitted(true);
		createStrategy = new StrategyData();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.getByExchangeId(exchangeId)))
				.thenReturn(company);
		when(strategyService.createUpdateStrategy(dto, company, false)).thenReturn(strategyId);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(createStrategy);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(appConfigRules);

		actualResult = strategyController.createCustomStrategy(request, dto, id, companyCode, exchangeId);

		verify(queuedSubmitService, times(1)).createSubmitProcess(company, createStrategy,
				ProcessStatusEnum.SUBMIT_PROCESS.getProcessName(), true);
		verify(submitService, times(0)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());
		assertEquals(createStrategy, actualResult);
	}

	@Test
	public void updateStrategy() throws JsonProcessingException {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		StrategyData dto = prepareStrategyData();
		long id = 0;
		StrategyData updatedStrategy;
		StrategyData actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectCompany(false);
		updatedStrategy = new StrategyData();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.createUpdateStrategy(dto, company, true)).thenReturn(strategyId);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(updatedStrategy);
		when(RequestValidator.getValidatedStrategyName(dto.getStrategySummary().getName()))
				.thenReturn(dto.getStrategySummary().getName());

		actualResult = strategyController.updateStrategy(request, dto, id, companyCode, null);

		verify(queuedSubmitService, times(0)).createSubmitProcess(company, updatedStrategy,
				ProcessStatusEnum.SUBMIT_PROCESS.getProcessName(), true);
		verify(submitService, times(0)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());
		assertEquals(updatedStrategy, actualResult);
	}

	/*
	 * Submit true and queue disabled
	 */
	@Test
	public void updateStrategy_1() throws JsonProcessingException {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		StrategyData dto = prepareStrategyData();
		long id = 0;
		StrategyData updatedStrategy;
		StrategyData actualResult;
		Company company;
		Map<String, String> appConfigRules = new HashMap<>();
		appConfigRules.put("SUBMIT_QUE_ENABLED", "false");

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test with success strategy created - submitted true
		 */
		company = new Company();
		company.setProspectCompany(false);
		dto.getStrategySummary().setSubmitted(true);
		updatedStrategy = new StrategyData();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.createUpdateStrategy(dto, company, true)).thenReturn(strategyId);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(updatedStrategy);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(appConfigRules);

		actualResult = strategyController.updateStrategy(request, dto, id, companyCode, null);

		verify(queuedSubmitService, times(0)).createSubmitProcess(company, updatedStrategy,
				ProcessStatusEnum.SUBMIT_PROCESS.getProcessName(), true);
		verify(submitService, times(1)).submit(company, updatedStrategy, EMPLID, true, false);
		assertEquals(updatedStrategy, actualResult);
	}

	/*
	 * Submit true and queue enabled
	 */
	@Test
	public void updateStrategy_2() throws JsonProcessingException {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		StrategyData dto = prepareStrategyData();
		long id = 0;
		StrategyData updatedStrategy;
		StrategyData actualResult;
		Company company;
		Map<String, String> appConfigRules = new HashMap<>();
		appConfigRules.put("SUBMIT_QUE_ENABLED", "true");

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test with success strategy created - submitted true
		 */
		company = new Company();
		company.setProspectCompany(false);
		dto.getStrategySummary().setSubmitted(true);
		updatedStrategy = new StrategyData();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.createUpdateStrategy(dto, company, true)).thenReturn(strategyId);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(updatedStrategy);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(appConfigRules);

		actualResult = strategyController.updateStrategy(request, dto, id, companyCode, null);

		verify(queuedSubmitService, times(1)).createSubmitProcess(company, updatedStrategy,
				ProcessStatusEnum.SUBMIT_PROCESS.getProcessName(), true);
		verify(submitService, times(0)).submit(any(Company.class), any(StrategyData.class), anyString(), anyBoolean(),
				anyBoolean());
		assertEquals(updatedStrategy, actualResult);
	}

	@Test
	public void updateStrategyName() {
		HttpServletRequest request;
		long strategyId = 1111L;
		String strategyName = "UPDATED STRATEGY NAME";

		request = Mockito.mock(HttpServletRequest.class);
		when(RequestValidator.getValidatedStrategyName(strategyName)).thenReturn(strategyName);
		doNothing().when(strategyService).updateStrategyName(strategyId, strategyName);

		strategyController.updateStrategyName(request, strategyId, strategyName);

		verify(strategyService, times(1)).updateStrategyName(strategyId, strategyName);
	}

	@Test
	public void getStrategyData() throws JsonProcessingException, ParseException {

		HttpServletRequest request;
		String companyCode = "TEST";
		List<StrategyData> expectedResult = new ArrayList<StrategyData>();
		List<StrategyData> actualResult;
		Company company;
		boolean isDefaultSubmit = false;
		boolean isPreload = false;

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test with success - not renewal company
		 */
		company = new Company();
		company.setProspectCompany(false);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(expectedResult);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		actualResult = strategyController.getStrategyData(request, companyCode, null, null);
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with success - renewal company
		 */
		company = new Company();
		company.setRenewalCompany(true);
		company.setProspectCompany(false);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		doNothing().when(strategyService).createFutureStrategies(company, isDefaultSubmit, isPreload);
		when(strategyService.getStrategies(company, false, null)).thenReturn(expectedResult);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		actualResult = strategyController.getStrategyData(request, companyCode, null, null);
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with success - renewal company; renewal open
		 */
		company = new Company();
		company.setRenewalCompany(true);
		company.setRenewalOpen(true);
		company.setProspectCompany(false);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		doNothing().when(strategyService).createFutureStrategies(company, isDefaultSubmit, isPreload);
		when(strategyService.getStrategies(company, false, null)).thenReturn(expectedResult);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		actualResult = strategyController.getStrategyData(request, companyCode, null, null);
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with success - renewal company; transition period
		 */
		company = new Company();
		company.setRenewalCompany(true);
		company.setTransitionPeriod(true);
		company.setProspectCompany(false);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		doNothing().when(strategyService).createFutureStrategies(company, isDefaultSubmit, isPreload);
		when(strategyService.getStrategies(company, false, null)).thenReturn(expectedResult);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		actualResult = strategyController.getStrategyData(request, companyCode, null, null);
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with success - renewal company; defaultSubmit
		 */
		company = new Company();
		company.setRenewalCompany(true);
		isDefaultSubmit = true;
		company.setProspectCompany(false);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(expectedResult);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		actualResult = strategyController.getStrategyData(request, companyCode, null, null);
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with success - prospect
		 */
		company = new Company();
		company.setRenewalCompany(false);
		isDefaultSubmit = true;
		company.setProspectCompany(true);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(expectedResult);
		when(processStatusService.isStrategySummariesProcessed(company.getCode())).thenReturn(true);

		actualResult = strategyController.getStrategyData(request, companyCode, null, "1");
		assertEquals(expectedResult, actualResult);

		/*
		 * Test with success - Onboardingclient
		 */
		company = new Company();
		company.setRenewalCompany(false);
		company.setProspectCompany(false);
		company.setProspectConvertedOnboardingClient(true);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.getStrategies(company, false, null)).thenReturn(expectedResult);

		actualResult = strategyController.getStrategyData(request, companyCode, null, "1");
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategyDataHistory() throws JsonProcessingException, ParseException {

		HttpServletRequest request;
		String companyCode = "TEST";
		List<StrategyData> expectedResult = new ArrayList<StrategyData>();
		List<StrategyData> actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test with success
		 */
		company = new Company();
		company.setProspectCompany(false);
		actualResult = strategyController.getStrategyDataHistory(request, companyCode);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategyDetail() throws JsonProcessingException {

		HttpServletRequest request;
		String companyCode = "TEST";
		long strategyId = 1000;
		StrategyData expectedResult = new StrategyData();
		StrategyData actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test with success
		 */
		company = new Company();
		company.setProspectCompany(false);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.getStrategyById(company, strategyId, false)).thenReturn(expectedResult);

		actualResult = strategyController.getStrategyDetail(request, strategyId, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getStrategySubmitStatus() throws JsonProcessingException {

		HttpServletRequest request;
		String companyCode = "TEST";
		SubmitStatus submitStatus;
		String expectedResult = "PENDING";
		String actualResult;

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test with success - null
		 */
		submitStatus = null;
		when(submitStatusService.findLatestSubmitStatusBy(companyCode)).thenReturn(submitStatus);

		actualResult = strategyController.getStrategySubmitStatus(request, companyCode);
		assertNull(actualResult);

		/*
		 * Test with success - not null
		 */
		submitStatus = SubmitStatus.builder().status(expectedResult).build();
		when(submitStatusService.findLatestSubmitStatusBy(companyCode)).thenReturn(submitStatus);

		actualResult = strategyController.getStrategySubmitStatus(request, companyCode);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void deleteStrategy() {
		HttpServletRequest request;
		long strategyId = 1111L;
		String compCode = "G48";
		Company comp = new Company();

		request = Mockito.mock(HttpServletRequest.class);
		doNothing().when(strategyService).deleteStrategy(comp, strategyId);
		when(companyService.getCompanyDetails(compCode, false, EMPLID, null)).thenReturn(comp);

		strategyController.deleteStrategy(request, strategyId, compCode, null);

		verify(strategyService, times(1)).deleteStrategy(comp, strategyId);
	}

	private StrategyData prepareStrategyData() {
		StrategyData strategyDataJson = new StrategyData();
		StrategySummary summary = new StrategySummary();
		summary.setId(100L);
		summary.setSubmitted(false);
		summary.setName("STRATEGY_NAME");
		strategyDataJson.setStrategySummary(summary);
		return strategyDataJson;
	}

	@Test
	public void updateStrategyBudgetTest() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		StrategyBudget budget = Mockito.mock(StrategyBudget.class);
		long strategyId = 1111L;
		doNothing().when(strategyService).updateStrategyBudget(strategyId, budget);
		strategyController.updateStrategyBudget(request, strategyId, budget);
		verify(strategyService, times(1)).updateStrategyBudget(strategyId, budget);
	}

	@Test
	public void updateStrategyBudgetNullBudgetFactorTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":100,\"budgetFactor\":\"\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetInvalidBudgetTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":,\"budgetFactor\":\"1\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetInvalidBudgetAndBudgetFactorTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":,\"budgetFactor\":\"\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetValidBudgetAndBudgetFactorTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":100,\"budgetFactor\":\"12\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetNegativeBudgetTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":-100,\"budgetFactor\":\"1\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetInvalidBudgetFactorTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":100,\"budgetFactor\":\"6\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetValidMonthlyBudgetFactorTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":100,\"budgetFactor\":\"1\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetValidYearlyBudgetFactorTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":100,\"budgetFactor\":\"12\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void updateStrategyBudgetNegativeBudgetFactorTest() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("{\"budget\":100,\"budgetFactor\":\"-12\"}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void submitProspectsTriNetStrategyTest() throws Exception {
		String prospectCompCode = "PROSPECT-COMPANY-TEST";
		long strategyId = 18317;

		ArgumentCaptor<String> prospectCompanyCodeCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Long> strategyIdCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<String> exchangeIdCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);

		String URI = URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_STRATEGY_SUBMIT;

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(URI, "001", "00001401813", strategyId, prospectCompCode).param("exchangeId", "TNIII")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		verify(prospectSubmitService, times(1)).submit(prospectCompanyCodeCaptor.capture(), strategyIdCaptor.capture(),
				exchangeIdCaptor.capture(), requestCaptor.capture());
		assertEquals(prospectCompCode, prospectCompanyCodeCaptor.getValue());
		assertEquals(strategyId, strategyIdCaptor.getValue().longValue());
		assertEquals("TNIII", exchangeIdCaptor.getValue());
	}

	@Test
	public void getCostSummaryData() {

		String companyCode = "TEST";
		long strategyId = 12345;
		StrategyCostRes expectedResult = new StrategyCostRes();


		Company company = new Company();
		company.setProspectCompany(false);

		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(strategyService.getStrategyCostByPlanType(company, strategyId)).thenReturn(expectedResult);

		StrategyCostRes actualResult = strategyController.getCostSummaryData(companyCode, strategyId, null);
		assertEquals(expectedResult, actualResult);

		//test with system account
		when(BSSSecurityUtils.checkSystemAccount()).thenReturn(true);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID_SYS_ACCT, null)).thenReturn(company);
		when(strategyService.getStrategyCostByPlanType(company, strategyId)).thenReturn(expectedResult);

		actualResult = strategyController.getCostSummaryData(companyCode, strategyId, null);
		assertEquals(expectedResult, actualResult);
	}

	private MvcResult performStrategyBudgetAPIMockRequest(String requestBody) throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.put(API_URL_STRATEGY_BUDGET_API, "D11", "00001401813", "18317", "D11")
				.accept(MediaType.APPLICATION_JSON).content(requestBody).contentType(MediaType.APPLICATION_JSON);
		return mockMvc.perform(requestBuilder).andReturn();
	}

	@Test
	public void createDefaultStrategyOnBoardingClients() throws JsonProcessingException, ParseException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		String companyCode = "TEST";
		Company onboardingCompany = new Company();
		onboardingCompany.setProspectConvertedOnboardingClient(true);
		List<StrategyData> expectedStrategies = new ArrayList<>();

		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(onboardingCompany);
		when(strategyService.getStrategies(onboardingCompany, false, null)).thenReturn(expectedStrategies);
		when(processStatusService.isStrategySummariesProcessed(onboardingCompany.getCode())).thenReturn(true);

		List<StrategyData> actualStrategies = strategyController.createDefaultStrategyOnBoardingClients(request, companyCode, null, "1");
		assertEquals(expectedStrategies, actualStrategies);
	}

	@Test
	public void createStrategies_success() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		String companyCode = "TEST";
		Company company = new Company();

		CreateStrategiesRequest requestBody = new CreateStrategiesRequest();
		requestBody.setBundleId(55L);
		requestBody.setSelectedCarrierId(101L);
		requestBody.setPlanMappingResponse(List.of(new DefaultPlanMappingServiceClient.PlanMappingResponse()));
		when(companyService.getCompanyDetails(companyCode, false, "SYSTEM", null)).thenReturn(company);

		strategyController.createStrategies(request, requestBody, companyCode);

		assertEquals(Long.valueOf(55L), company.getBundleId());
		verify(strategyService).createProspectsTrinetStrategy(company, 101L, requestBody.getPlanMappingResponse());
	}

	@Test
	public void createStrategies_bundleIdMinusOne_convertsToNull() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		String companyCode = "TEST";
		Company company = new Company();

		CreateStrategiesRequest requestBody = new CreateStrategiesRequest();
		requestBody.setBundleId(-1L);
		requestBody.setSelectedCarrierId(101L);
		requestBody.setPlanMappingResponse(List.of(new DefaultPlanMappingServiceClient.PlanMappingResponse()));
		when(companyService.getCompanyDetails(companyCode, false, "SYSTEM", null)).thenReturn(company);

		strategyController.createStrategies(request, requestBody, companyCode);

		assertNull(requestBody.getBundleId());
		assertNull(company.getBundleId());
		verify(strategyService).createProspectsTrinetStrategy(company, 101L, requestBody.getPlanMappingResponse());
	}

	@Test
	public void createStrategies_nullSelectedCarrierId_returnsBadRequest() throws Exception {
		MvcResult result = performCreateStrategiesAPIMockRequest("{\"planMappingResponse\":[{}]}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void createStrategies_emptyPlanMappingResponse_returnsBadRequest() throws Exception {
		MvcResult result = performCreateStrategiesAPIMockRequest("{\"selectedCarrierId\":101,\"planMappingResponse\":[]}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	@Test
	public void createStrategies_nullPlanMappingResponse_returnsBadRequest() throws Exception {
		MvcResult result = performCreateStrategiesAPIMockRequest("{\"selectedCarrierId\":101,\"planMappingResponse\":null}");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	private MvcResult performCreateStrategiesAPIMockRequest(String requestBody) throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.CREATE_STRATEGIES, "001", "00001401813", "D11")
				.accept(MediaType.APPLICATION_JSON).content(requestBody).contentType(MediaType.APPLICATION_JSON);
		return mockMvc.perform(requestBuilder).andReturn();
	}

}