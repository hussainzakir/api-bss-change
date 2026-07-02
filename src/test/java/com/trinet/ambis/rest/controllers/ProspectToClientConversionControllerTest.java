package com.trinet.ambis.rest.controllers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionResponse;
import com.trinet.ambis.service.prospect.ProspectToClientConversionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


@RunWith(MockitoJUnitRunner.class)
public class ProspectToClientConversionControllerTest {

	@InjectMocks
	ProspectToClientConversionController prospectToClientConversionController;

	@Mock
	ProspectToClientConversionService prospectToClientConversionService;

	MockMvc mockMvc;

	private static final String COMPANY_CODE = "2R23";
	private static final String PROSPECT_ID = "a1b2c3";
	private static final String STREAM_EVENT_ID = "a6xEa000000TPaaIAG";
	private static final String COMPANY_ID = "001";
	private static final String EMPLOYEE_ID = "00002222256";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(prospectToClientConversionController).build();
	}

	// ---- New endpoint tests ----

	@Test
	public void processProspectToClientConversionTest1() throws Exception {
		ProspectToClientConversionResponse prospectToClientConversionResponse = ProspectToClientConversionResponse
				.builder().bssCompanyId(2).build();

		ProspectToClientConversionRequest request = ProspectToClientConversionRequest.builder()
				.streamEventId(STREAM_EVENT_ID).companyCode(COMPANY_CODE)
				.bundleId("1")
				.riskType(RiskTypeEnum.BANDS.name()).build();

		when(prospectToClientConversionService
				.processProspectToClientConversion(any(ProspectToClientConversionRequest.class)))
				.thenReturn(prospectToClientConversionResponse);

		mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_TO_CLIENT_CONVERSION, COMPANY_ID, EMPLOYEE_ID,
						PROSPECT_ID)
				.content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON));

		verify(prospectToClientConversionService, times(1))
				.processProspectToClientConversion(any(ProspectToClientConversionRequest.class));
	}

	@Test
	public void newEndpoint_returnsHttpOkAndResponseBody() throws Exception {
		ProspectToClientConversionResponse response = ProspectToClientConversionResponse
				.builder().bssCompanyId(42).k1(true).build();

		ProspectToClientConversionRequest request = ProspectToClientConversionRequest.builder()
				.streamEventId(STREAM_EVENT_ID).companyCode(COMPANY_CODE)
				.bundleId("100").riskType(RiskTypeEnum.BANDS.name()).build();

		when(prospectToClientConversionService.processProspectToClientConversion(any()))
				.thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_TO_CLIENT_CONVERSION,
						COMPANY_ID, EMPLOYEE_ID, PROSPECT_ID)
				.content(objectMapper.writeValueAsString(request))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bssCompanyId").value(42))
				.andExpect(jsonPath("$.k1").value(true));
	}

	@Test
	public void newEndpoint_setsProspectIdFromPathVariable() throws Exception {
		ProspectToClientConversionRequest requestBody = ProspectToClientConversionRequest.builder()
				.companyCode(COMPANY_CODE).streamEventId(STREAM_EVENT_ID)
				.bundleId("100").riskType(RiskTypeEnum.BANDS.name()).build();

		when(prospectToClientConversionService.processProspectToClientConversion(any()))
				.thenReturn(ProspectToClientConversionResponse.builder().bssCompanyId(1).build());

		ArgumentCaptor<ProspectToClientConversionRequest> captor =
				ArgumentCaptor.forClass(ProspectToClientConversionRequest.class);

		mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_TO_CLIENT_CONVERSION,
						COMPANY_ID, EMPLOYEE_ID, PROSPECT_ID)
				.content(objectMapper.writeValueAsString(requestBody))
				.contentType(MediaType.APPLICATION_JSON));

		verify(prospectToClientConversionService).processProspectToClientConversion(captor.capture());
		assertEquals(PROSPECT_ID, captor.getValue().getProspectId());
	}

}

