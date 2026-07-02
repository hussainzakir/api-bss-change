package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.submit.ResubmitService;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class ResubmitControllerTest extends ServiceUnitTest {

	@InjectMocks
	ResubmitController resubmitController;

	@Mock
	CompanyService companyService;

	@Mock
	SubmitStatusService submitStatusService;

	@Mock
	StrategyService strategyService;

	@Mock
	QueuedSubmitService queuedSubmitService;

	@Mock
	AppRulesConfigService appRulesConfigService;

	@Mock
	SubmitService submitService;

	@Mock
	ResubmitService resubmitService;


	private MockMvc mockMvc;
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
	private static final String COMP_CODE = "D11";
	private static final String EMPLID = "00002345612";
	private static final boolean IS_HISTORY = false;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockMvc = MockMvcBuilders.standaloneSetup(resubmitController).build();
		mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
		if (mockStaticAppRulesAndConfigsUtils != null) {
			mockStaticAppRulesAndConfigsUtils.close();
			mockStaticAppRulesAndConfigsUtils = null;
		}
	}

	@Test
	public void resubmitToPS_noSendEmailParam() throws Exception {
		StrategyData sd = new StrategyData();
		boolean sendClientEmail = true;

		String RESUBMIT_URI = URIConstants.VERSION_AND_ROOT + URIConstants.RESUBMIT_STRATEGY;
		when(resubmitService.resubmit(COMP_CODE, sendClientEmail)).thenReturn(sd);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post(RESUBMIT_URI, COMP_CODE, EMPLID, COMP_CODE)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{\"benefitGroups\":[],\"strategyHsaFunding\":null,\"cached\":false}", response.getContentAsString());
	}

	@Test
	public void resubmitToPS_sendEmailFalse() throws Exception {
		Company company = new Company();
		StrategyData sd = new StrategyData();
		boolean sendClientEmail = false;

		String RESUBMIT_URI = URIConstants.VERSION_AND_ROOT + URIConstants.RESUBMIT_STRATEGY;
		when(resubmitService.resubmit(COMP_CODE, sendClientEmail)).thenReturn(sd);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post(RESUBMIT_URI, COMP_CODE, EMPLID, COMP_CODE)
				.param("sendClientEmail", "false").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{\"benefitGroups\":[],\"strategyHsaFunding\":null,\"cached\":false}", response.getContentAsString());
	}
	

	@Test
	public void defaultStrategy() throws Exception {
		String DEFAULT_STRATEGY_URI = URIConstants.VERSION_AND_ROOT + URIConstants.SUBMIT_DEFAULT_STRATEGY;

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(DEFAULT_STRATEGY_URI, COMP_CODE, EMPLID, "Q1", COMP_CODE)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());

		verify(submitService, times(1)).defaultSubmit(COMP_CODE, "Q1", EMPLID);
		verify(submitService, times(1)).defaultSubmitTermedClients(COMP_CODE, "Q1");
	}

	@Test
	public void processPendingSubmissions_queueDisabled() throws Exception {

		String PENDING_SUBMISSION_URI = URIConstants.VERSION_AND_ROOT + URIConstants.PROCESS_PENDING_SUBMISSIONS;
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(PENDING_SUBMISSION_URI, COMP_CODE, EMPLID, COMP_CODE).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		Mockito.verify(queuedSubmitService, Mockito.times(1)).startAsyncManualSubmitProcess();
	}

	@Test
	public void processPendingSubmissions_queueEnabled() throws Exception {
		mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isSubmitQueuingEnabled).thenReturn(true);

		String PENDING_SUBMISSION_URI = URIConstants.VERSION_AND_ROOT + URIConstants.PROCESS_PENDING_SUBMISSIONS;
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(PENDING_SUBMISSION_URI, COMP_CODE, EMPLID, COMP_CODE).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		Exception expectedEx = null;
		try {
			mockMvc.perform(requestBuilder).andReturn();
		} catch (Exception e) {
			expectedEx = e;
		}
		assertEquals(
				"Request processing failed; nested exception is com.trinet.ambis.exception.BSSApplicationException: Can't process request when submit queue is enabled.",
			expectedEx.getLocalizedMessage());
	}
}
