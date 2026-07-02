package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.outputs.OutputReportDataService;
import com.trinet.ambis.service.outputs.OutputService;
import com.trinet.ambis.service.prospect.dto.BenTypeOfferRes;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class OutputControllerTest extends ServiceUnitTest {

	@InjectMocks
	OutputController outputController;

	@Mock
	CompanyService companyService;

    @Mock
    OutputReportDataService outputReportDataService;

	@Mock
	OutputService outputService;
	
	@Mock
	HttpServletRequest httpRequest;

	private MockMvc mockMvc;
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	private static final String EMPLID = "0000000123456";

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockMvc = MockMvcBuilders.standaloneSetup(outputController).build();
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

    @Ignore
	@Test
	public void getOutputReport() throws Exception {
		OutputRequest outputRequest = new OutputRequest();
		Company company = new Company();

		when(outputService.generateReport(outputRequest, company, httpRequest))
				.thenReturn(new byte[10]);

		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_OUTPUTS, "001", "00002222256",
						"PROS-COMP-CODE")
				.param("exchangeId", "TNIII").contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(outputRequest)));

		// Assert
		assertNotNull(actualResult);
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
	}

    @Ignore
	@Test
	public void getOutputReportData() throws Exception {
		OutputRequest outputRequest = new OutputRequest();
		Company company = new Company();

		when(outputReportDataService.getData(outputRequest, company, httpRequest))
				.thenReturn(new OutputData());

		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PROSPECT_OUTPUTS_JSON, "001", "00002222256",
						"PROS-COMP-CODE")
				.param("exchangeId", "TNIII").contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(outputRequest)));

		// Assert
		assertNotNull(actualResult);
		actualResult.andExpect(status().isOk());
	}

    @Ignore
	@Test
	public void getNotOfferedPlanTypes() throws Exception {
		List<BenTypeOfferRes> mockData = new ArrayList<>();
		mockData.add(BenTypeOfferRes.builder().strategyId(1111).offerTypes(Set.of("10")).build());
		mockData.add(BenTypeOfferRes.builder().strategyId(2222).offerTypes(Set.of("10", "11")).build());
		mockData.add(BenTypeOfferRes.builder().strategyId(3333).offerTypes(Set.of("10", "14", "11")).build());

		when(outputService.getPlanTypeOfferedDetails(Arrays.asList(1111L, 2222L, 3333L), Arrays.asList("10","11","14","1D","1V"))).thenReturn(mockData);

		ResultActions actualResult = mockMvc.perform(
				MockMvcRequestBuilders.get(URIConstants.VERSION_AND_ROOT + URIConstants.OUTPUT_FILTER_BENEFIT_OFFERS,
						"001", "00002222256", "1111,2222,3333", "PROSP-COMP-CODE"));

		assertNotNull(actualResult);
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/json"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$.size()").value("3"));

		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].strategyId").value("1111"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].offerTypes.size()").value(1));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[0].offerTypes[0]").value("10"));

		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].strategyId").value("2222"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[1].offerTypes.size()").value(2));
		actualResult.andExpect(
				MockMvcResultMatchers.jsonPath("$[1].offerTypes").value(Matchers.containsInAnyOrder("10", "11")));

		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[2].strategyId").value("3333"));
		actualResult.andExpect(MockMvcResultMatchers.jsonPath("$[2].offerTypes.size()").value(3));
		actualResult.andExpect(
				MockMvcResultMatchers.jsonPath("$[2].offerTypes").value(Matchers.containsInAnyOrder("10", "11", "14")));
	}
	
	private static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
