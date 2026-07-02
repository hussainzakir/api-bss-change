package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.BenefitGroupFundingService;
import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;
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

@RunWith(MockitoJUnitRunner.class)
public class BenefitGroupFundingControllerTest extends ServiceUnitTest {

	@InjectMocks
	BenefitGroupFundingController benefitGroupFundingController;

	@Mock
	BenefitGroupFundingService benefitGroupFundingService;

	MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(benefitGroupFundingController).build();
	}

	/**
	 * given group id and group funding request </br>
	 * when updateBenefitGroupFunding method is called </br>
	 * then update the funding for the group on prospect service </br>
	 * 
	 * @throws Exception
	 * @throws JsonProcessingException
	 **/
	@Test
	public void updateBenefitGroupFundingTest1() throws JsonProcessingException, Exception {
		// given
		// data
		long groupId = 1111;
		long strategyId = 0;
		String companyCode = "a1b2c3";
		List<GroupFundingReq> groupFundingReqs = prepareGroupFundingReq();
		// method mocks
		doNothing().when(benefitGroupFundingService).updateBenefitGroupFunding(companyCode, strategyId, groupId,
				groupFundingReqs);
		// when
		mockMvc.perform(MockMvcRequestBuilders
				.put(URIConstants.VERSION_AND_ROOT + URIConstants.GROUP_FUNDING, "001", "00002222256", strategyId,
						groupId, companyCode)
				.content(new ObjectMapper().writeValueAsString(groupFundingReqs))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));
		// then
		// verify
		verify(benefitGroupFundingService, times(1)).updateBenefitGroupFunding(companyCode, strategyId, groupId,
				groupFundingReqs);
	}

	/**
	 * given group id </br>
	 * when getBenefitGroupFunding method is called </br>
	 * then return the funding for the group from prospect service </br>
	 * 
	 * @throws Exception
	 * @throws JsonProcessingException
	 **/
	@Test
	public void getBenefitGroupFundingTest1() throws JsonProcessingException, Exception {
		// given
		// data
		long groupId = 1111;
		long strategyId = 0;
		String companyCode = "a1b2c3";
		List<GroupFundingRes> groupFundingRes = prepareGroupFundingRes();
		// method mocks
		when(benefitGroupFundingService.getBenefitGroupFunding(companyCode, strategyId, groupId))
				.thenReturn(groupFundingRes);
		// when
		ResultActions actualResult = mockMvc
				.perform(MockMvcRequestBuilders.get(URIConstants.VERSION_AND_ROOT + URIConstants.GROUP_FUNDING, "001",
						"00002222256", strategyId, groupId, companyCode).contentType(MediaType.APPLICATION_JSON));
		// then
		// assertions
		actualResult.andExpect(status().isOk());
		String response = actualResult.andReturn().getResponse().getContentAsString();
		assertEquals(new ObjectMapper().writeValueAsString(groupFundingRes), response);
		// verify
		verify(benefitGroupFundingService, times(1)).getBenefitGroupFunding(companyCode, strategyId, groupId);
	}

	private List<GroupFundingReq> prepareGroupFundingReq() {
		return List.of(
				GroupFundingReq.builder().benefitType("10").fundingType("FLT").cvgCodeValues(List.of(
								GroupFundingReq.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(1200.00)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(100.00)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(129.50)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(1106.99)).build()))
						.build(),
				GroupFundingReq.builder().benefitType("11").fundingType("PCT").cvgCodeValues(List.of(
								GroupFundingReq.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(100.78)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(200.45)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(386.19)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("4").build()))
						.fundingCapDetails(GroupFundingReq.FundingCapDetails.builder().capType("dollar")
								.capPlanId(null).capPlanName(null)
								.cvgCodeValues(List.of(
										GroupFundingReq.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(200)).build(),
										GroupFundingReq.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(250)).build(),
										GroupFundingReq.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(300)).build(),
										GroupFundingReq.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(350)).build())).build())
						.build(),
				GroupFundingReq.builder().benefitType("14").fundingType("PCT").cvgCodeValues(List.of(
								GroupFundingReq.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(100.78)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(200.45)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(386.19)).build(),
								GroupFundingReq.CvgCodeValue.builder().cvgCode("4").build()))
						.fundingCapDetails(GroupFundingReq.FundingCapDetails.builder().capType("limitplan")
								.capPlanId(9876).capPlanName("Aetna PPO 1 NTL")
								.cvgCodeValues(List.of(
										GroupFundingReq.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(300)).build(),
										GroupFundingReq.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(350)).build(),
										GroupFundingReq.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(400)).build(),
										GroupFundingReq.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(450)).build())).build())
						.build());
	}

	private List<GroupFundingRes> prepareGroupFundingRes() {
		return List.of(
				GroupFundingRes.builder().benefitType("10").fundingType("FLT").cvgCodeValues(List.of(
								GroupFundingRes.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(1200.00)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(100.00)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(129.50)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(1106.99)).build()))
						.build(),
				GroupFundingRes.builder().benefitType("11").fundingType("PCT").cvgCodeValues(List.of(
								GroupFundingRes.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(100.78)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(200.45)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(386.19)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("4").build()))
						.fundingCapDetails(GroupFundingRes.FundingCapDetails.builder().capType("dollar")
								.capPlanId(null).capPlanName(null)
								.cvgCodeValues(List.of(
										GroupFundingRes.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(200)).build(),
										GroupFundingRes.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(250)).build(),
										GroupFundingRes.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(300)).build(),
										GroupFundingRes.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(350)).build())).build())
						.build(),
				GroupFundingRes.builder().benefitType("14").fundingType("PCT").cvgCodeValues(List.of(
								GroupFundingRes.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(100.78)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(200.45)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(386.19)).build(),
								GroupFundingRes.CvgCodeValue.builder().cvgCode("4").build()))
						.fundingCapDetails(GroupFundingRes.FundingCapDetails.builder().capType("limitplan")
								.capPlanId(9876).capPlanName("Aetna PPO 1 NTL")
								.cvgCodeValues(List.of(
										GroupFundingRes.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(300)).build(),
										GroupFundingRes.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(350)).build(),
										GroupFundingRes.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(400)).build(),
										GroupFundingRes.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(450)).build())).build())
						.build());
	}

}