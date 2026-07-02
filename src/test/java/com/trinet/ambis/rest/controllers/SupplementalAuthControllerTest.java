package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.SupplementalAuthService;
import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class SupplementalAuthControllerTest extends ServiceUnitTest {

	@InjectMocks
	SupplementalAuthController supplementalAuthController;

	@Mock
	SupplementalAuthService supplementalAuthService;

	@Mock
	private HttpServletRequest request;

	private static final String API_URL = URIConstants.VERSION_AND_ROOT + URIConstants.EXEC_SUPP_LTD_AUTH_RESPONSE;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(supplementalAuthController).build();
	}

	@Test
	public void getExecSuppLtdAuthResponse() throws Exception {
		SupplementalLtdAuthReponse obj = SupplementalLtdAuthReponse.builder().userId("00002222263")
				.authFirstName("Fname").authLastName("Lname").build();
		when(supplementalAuthService.getExecSuppLtdAuthResponse("G48")).thenReturn(obj);
		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(API_URL, "G48", "00003233378", "G48")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		SupplementalLtdAuthReponse data = new Gson().fromJson(response.getContentAsString(),
				SupplementalLtdAuthReponse.class);
		assertEquals("00002222263", data.getUserId());
		assertEquals("Fname", data.getAuthFirstName());
		assertEquals("Lname", data.getAuthLastName());
	}

	@Test
	public void saveExecSuppLtdAuthResponse_test1() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("Y");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void saveExecSuppLtdAuthResponse_test2() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("N");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Ignore
	@Test
	public void saveExecSuppLtdAuthResponse_test3() throws Exception {
		MvcResult result = performStrategyBudgetAPIMockRequest("true");
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
	}

	private MvcResult performStrategyBudgetAPIMockRequest(String answer) throws Exception {
		RequestBuilder requestBuilder = MockMvcRequestBuilders.post(API_URL, "G48", "00003233378", "G48")
				.accept(MediaType.APPLICATION_JSON).content("{\"answer\":\""+answer+"\"}")
				.contentType(MediaType.APPLICATION_JSON);
		return mockMvc.perform(requestBuilder).andReturn();
	}
}
