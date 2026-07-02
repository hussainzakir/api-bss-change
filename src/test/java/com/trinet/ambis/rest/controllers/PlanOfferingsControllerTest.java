package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.planofferings.PlanOfferingsReportDataService;
import com.trinet.ambis.service.planofferings.PlanOfferingsService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class PlanOfferingsControllerTest extends ServiceUnitTest {

	@InjectMocks
	PlanOfferingsController planOfferingsController;

	@Mock
	PlanOfferingsService planOfferingsService;

	@Mock
	PlanOfferingsReportDataService planOfferingsReportDataService;
	
	@Mock
	HttpServletRequest httpRequest;

	private MockMvc mockMvc;
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	private static final String EMPLID = "0000000123456";

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockMvc = MockMvcBuilders.standaloneSetup(planOfferingsController).build();
	}

	@After
	public void tearDown() {
        if (mockStaticBSSSecurityUtils != null) {
            mockStaticBSSSecurityUtils.close();
            mockStaticBSSSecurityUtils = null;
        }
    }

	@Test
	public void getPlanOfferingsReport() throws Exception {
		PlanOfferingsRequest planOfferingsRequest = new PlanOfferingsRequest();

		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.post(URIConstants.VERSION_AND_ROOT + URIConstants.PALN_OFFERINGS, "001", "00002222256")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(planOfferingsRequest)));

		assertNotNull(actualResult);
		actualResult.andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
	}
	
	private static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void getCarriersData() {
		String reportCode = "POEX";
		String quarter = "Q1";
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.JANUARY, 01);
		Date effDt = calendar.getTime();
		String benefitType = "med";
		Optional<String> hqZipCode = Optional.ofNullable("12345");
		List<CarrierData> actualResult;

		
		String companyId = "10PZ";
		String employeeId = "0000176543";
		String companyCode = "10PZ";

		List<CarrierData> carriers = new ArrayList<>();
		CarrierData carrier1 = new CarrierData(1,"Aetna");
		CarrierData carrier2 = new CarrierData(2, "AIG");
		carriers.add(carrier1);
		carriers.add(carrier2);

		when(planOfferingsReportDataService.getCarriersBy(reportCode,quarter,effDt,"CA",hqZipCode,benefitType)).thenReturn(carriers);

		actualResult = planOfferingsController.getCarriersBy(companyId, employeeId, companyCode, reportCode,quarter,effDt,"CA",hqZipCode,benefitType);

		assertEquals(1, actualResult.get(0).getCarrierId());
		assertEquals("Aetna", actualResult.get(0).getCarrierName());
		assertEquals(2, actualResult.get(1).getCarrierId());
		assertEquals("AIG", actualResult.get(1).getCarrierName());

	}
	
	/*
	 * Should return carriers when zipcode is empty for state other than CA or NY
	 */
	@Test
	public void getCarriersData1() {
		String companyId = "10PZ";
		String employeeId = "0000176543";
		String companyCode = "10PZ";
		String reportCode = null;
		String quarter = "Q1";
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.JANUARY, 01);
		Date effDt = calendar.getTime();
		String benefitType = "med";
		Optional<String> hqZipCode = Optional.ofNullable("12345");
		List<CarrierData> actualResult;
		
	

		List<CarrierData> carriers = new ArrayList<>();
		CarrierData carrier1 = new CarrierData(1,"Aetna");
		CarrierData carrier2 = new CarrierData(2, "AIG");
		carriers.add(carrier1);
		carriers.add(carrier2);

		when(planOfferingsReportDataService.getCarriersBy(reportCode,quarter,effDt,"CA",hqZipCode,benefitType)).thenReturn(carriers);

		actualResult = planOfferingsController.getCarriersBy(companyId, employeeId, companyCode, reportCode,quarter,effDt,"CA",hqZipCode,benefitType);

		assertEquals(1, actualResult.get(0).getCarrierId());
		assertEquals("Aetna", actualResult.get(0).getCarrierName());
		assertEquals(2, actualResult.get(1).getCarrierId());
		assertEquals("AIG", actualResult.get(1).getCarrierName());		
	}


}
