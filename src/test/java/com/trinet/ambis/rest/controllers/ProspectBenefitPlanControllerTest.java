package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.prospect.ProspectPlanService;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(MockitoJUnitRunner.class)
public class ProspectBenefitPlanControllerTest extends ServiceUnitTest {

	@InjectMocks
	ProspectBenefitPlanController prospectBenefitPlanController;
	@Mock
	ProspectPlanService prospectPlanService;

	MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(prospectBenefitPlanController).build();
	}

	/**
	 * given prospectId when getBenefitPlansBy method is called then return the
	 * benefit plans for the prospect from prospect service
	 *
	 * @throws Exception
	 * @throws JsonProcessingException
	 **/
	@Test
	public void getBenefitPlansByTest1() throws JsonProcessingException, Exception {
		// given
		// data
		String prospectId = "P1";
		String rateType = "";
		boolean includeWithRates = true;
		List<BenefitPlansRes> benefitPlansRes = prepareBenefitPlansRes();
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put("rateType", List.of(rateType));
		map.put("includeWithRates", List.of(String.valueOf(includeWithRates)));
		// method mocks
		when(prospectPlanService.getBenefitPlansBy(prospectId, rateType, includeWithRates)).thenReturn(benefitPlansRes);
		// when
		ResultActions actualResult = mockMvc.perform(MockMvcRequestBuilders
				.get(URIConstants.VERSION_AND_ROOT + URIConstants.BENEFIT_PLANS, "001", "00002222256", prospectId)
				.queryParams(map).contentType(MediaType.APPLICATION_JSON));
		// then
		// assertions
		actualResult.andExpect(status().isOk());
		String response = actualResult.andReturn().getResponse().getContentAsString();
		assertEquals(new ObjectMapper().writeValueAsString(benefitPlansRes), response);
		// verify
		verify(prospectPlanService, times(1)).getBenefitPlansBy(prospectId, rateType, includeWithRates);
	}

	private List<BenefitPlansRes> prepareBenefitPlansRes() {
		return List.of(
				BenefitPlansRes.builder().benefitTypeCode("10").benefitType("Medical")
						.benefitPlans(List.of(
								BenefitPlansRes.BenefitPlan.builder().benefitPlanId(1)
										.benefitPlanName("Blue Shield of California PPO 1")
										.carrier("Blue Shield of California")
										.attributes(List.of(
												BenefitPlansRes.Attribute.builder().id(1).displayName("Plan Type")
														.value("ACO").displayOrder(1).build(),
												BenefitPlansRes.Attribute.builder().id(2).displayName("Plan Type")
														.value("$40").displayOrder(2).build()))
										.tierRates(
												List.of(BenefitPlansRes.TierRates.builder().cvgCode("1")
														.cost(new BigDecimal("100.50")).build(),
														BenefitPlansRes.TierRates.builder().cvgCode("2")
																.cost(new BigDecimal("250.50")).build(),
														BenefitPlansRes.TierRates.builder().cvgCode("C")
																.cost(new BigDecimal("450.50")).build(),
														BenefitPlansRes.TierRates
																.builder().cvgCode("4").cost(new BigDecimal("600.00"))
																.build()))
										.build(),
								BenefitPlansRes.BenefitPlan.builder().benefitPlanId(2).benefitPlanName("Aetna PPO 2000")
										.carrier("Aetna")
										.attributes(List.of(BenefitPlansRes.Attribute.builder().id(1)
												.displayName("Primary Care Visit").value("$35").displayOrder(1).build(),
												BenefitPlansRes.Attribute.builder().id(2).displayName("Plan Type")
														.value("PPO").displayOrder(2).build()))
										.ageBandedRates(List.of(
												BenefitPlansRes.AgeBandedRates.builder().ageBandCode("14")
														.cost(new BigDecimal("250.50")).build(),
												BenefitPlansRes.AgeBandedRates.builder().ageBandCode("25")
														.cost(new BigDecimal("650.50")).build(),
												BenefitPlansRes.AgeBandedRates.builder().ageBandCode("37")
														.cost(new BigDecimal("700.00")).build(),
												BenefitPlansRes.AgeBandedRates.builder().ageBandCode("55")
														.cost(new BigDecimal("680.80")).build()))
										.build()))
						.build(),
				BenefitPlansRes.builder().benefitTypeCode("11").benefitType("Dental")
						.benefitPlans(List.of(BenefitPlansRes.BenefitPlan.builder().benefitPlanId(3)
								.benefitPlanName("MetLife Voluntary VA").carrier("MetLife")
								.attributes(List.of(
										BenefitPlansRes.Attribute.builder().id(1).displayName("Plan Type").value("PPO")
												.displayOrder(1).build(),
										BenefitPlansRes.Attribute
												.builder().id(2).displayName("Out-of-Network").value("100/80/50")
												.displayOrder(2).build()))
								.build()))
						.build(),
				BenefitPlansRes.builder().benefitTypeCode("14").benefitType("Vision")
						.benefitPlans(List.of(BenefitPlansRes.BenefitPlan.builder().benefitPlanId(5)
								.benefitPlanName("MetLife Voluntary NV").carrier("MetLife")
								.attributes(List.of(
										BenefitPlansRes.Attribute.builder().id(1).displayName("In-Network").value("$10")
												.displayOrder(1).build(),
										BenefitPlansRes.Attribute.builder().id(1).displayName("Family")
												.value("$150/Family").displayOrder(2).build()))
								.build()))
						.build());

	}

}
