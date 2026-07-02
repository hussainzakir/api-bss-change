package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.RequestResponseConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.PlanCompareConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanCompareFacade;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.domain.common.ReturnResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class PlanCompareControllerTest extends ServiceUnitTest {

	@InjectMocks
	PlanCompareController planCompareController;

	@Mock
	CompanyService companyService;

	@Mock
	PlanCompareFacade planCompareFacade;
	
	@Mock
	PlanCompareService planCompareService;
	
	@Mock
	HttpServletRequest httpRequest;

	private MockMvc mockMvc;
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	private static final String EMPLID = "00000123456";
	private static final String COMPANY_CODE = "D11";
	private static final String QUARTERNAME = "Q1";
	private static final String URI_COMPANY_PLAN_COMPARE_EXPORT = URIConstants.VERSION_AND_ROOT
			+ URIConstants.COMPANY_PLAN_COMPARE_EXPORT;
	private static final String URI_PLAN_COMPARE_EXPORT = URIConstants.VERSION_AND_ROOT
			+ URIConstants.PLAN_COMPARE_EXPORT;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockMvc = MockMvcBuilders.standaloneSetup(planCompareController).build();
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void generateEnrolledPlanCompareReport() throws Exception {
		Company company = new Company();
		company.setDescription("Test Company Inc");
		Workbook workbook = mock(Workbook.class);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, null)).thenReturn(company);
		when(planCompareFacade.generateEnrolledPlanCompareReport(any(Company.class), anyList(),
				any(HttpServletRequest.class))).thenReturn(workbook);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get(URI_COMPANY_PLAN_COMPARE_EXPORT, COMPANY_CODE, EMPLID, "18317,18318", COMPANY_CODE)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
		assertEquals("attachment; filename=Test_Company_Inc_ComparePlanAttributes.xlsx", response.getHeader("Content-disposition"));
	}
	
	@Test
	public void companyCurrentYearPlans() {
		HttpServletRequest request = new MockHttpServletRequest();
		List<BenefitPlanDetailDto> currentYearPlans =  new ArrayList<>();
		assertNotNull(getCurrentYearPlans().get());
		assertEquals("Medical", getCurrentYearPlans().get().getOfferType());
		assertEquals("PPO", getCurrentYearPlans().get().getPlanName());
		currentYearPlans.add(getCurrentYearPlans().get());
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		Map<String, String> planYearDetailMap = new HashMap<>();
		planYearDetailMap.put(PlanCompareConstants.CURRENT.getAction(), "41");
		planYearDetailMap.put(PlanCompareConstants.FUTURE.getAction(), "51");
		
		when(planCompareService.findSubmittedStrategyPlansBy(company.getCode())).thenReturn(currentYearPlans);
		ReturnResponse<List<BenefitPlanDetailDto>> companyCurrentYearPlans = planCompareController.companyCurrentYearPlans(request, company.getCode());
		
		assertNotNull(companyCurrentYearPlans);
		assertEquals(String.valueOf(BSSHttpStatusConstants.OK),companyCurrentYearPlans.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS,companyCurrentYearPlans.getStatusMessage());
		assertEquals("Medical",companyCurrentYearPlans.getData().get(0).getOfferType());
		assertEquals("1001",companyCurrentYearPlans.getData().get(0).getPlanId());
		assertEquals("PPO",companyCurrentYearPlans.getData().get(0).getPlanName());
	}
	
	@Test
	public void companyCurrentYearPlansNotFound() {
		HttpServletRequest request = new MockHttpServletRequest();
		List<BenefitPlanDetailDto> currentYearPlans =  new ArrayList<>();
		
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		Map<String, String> planYearDetailMap = new HashMap<>();
		planYearDetailMap.put(PlanCompareConstants.CURRENT.getAction(), "41");
		planYearDetailMap.put(PlanCompareConstants.FUTURE.getAction(), "51");
		
		when(planCompareService.findSubmittedStrategyPlansBy(company.getCode())).thenReturn(currentYearPlans);
		ReturnResponse<List<BenefitPlanDetailDto>> companyCurrentYearPlans = planCompareController.companyCurrentYearPlans(request, company.getCode());
		
		
		assertNotNull(companyCurrentYearPlans);
		assertEquals(String.valueOf(BSSHttpStatusConstants.NOT_FOUND),companyCurrentYearPlans.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS,companyCurrentYearPlans.getStatusMessage());
		assertEquals("There are no plans for the requested data",companyCurrentYearPlans.getStatusText());
	}
	
	private Supplier<BenefitPlanDetailDto> getCurrentYearPlans(){
		return () -> {
			BenefitPlanDetailDto currentYearPlan = new BenefitPlanDetailDto();
			currentYearPlan.setOfferType("Medical");
			currentYearPlan.setPlanId("1001");
			currentYearPlan.setPlanName("PPO");
			return currentYearPlan;
		};
	}
	
	@Test
	public void mappedPlans() {
		HttpServletRequest request = new MockHttpServletRequest();
		List<MappedPlanDetailDto> mappedPlanDetails =  new ArrayList<>();
		assertNotNull(getCurrentYearPlans().get());
		assertEquals("Medical", getCurrentYearPlans().get().getOfferType());
		assertEquals("PPO", getCurrentYearPlans().get().getPlanName());
		mappedPlanDetails.add(getMappedPlans().get());
		
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		Map<String, String> planYearDetailMap = new HashMap<>();
		planYearDetailMap.put(PlanCompareConstants.CURRENT.getAction(), "41");
		planYearDetailMap.put(PlanCompareConstants.FUTURE.getAction(), "51");
		
		when(planCompareService.findMappingBenefitPlansBy(company.getCode())).thenReturn(mappedPlanDetails);
		ReturnResponse<List<MappedPlanDetailDto>> companyCurrentYearPlans = planCompareController.mappingPlans(request, company.getCode());
		
		assertNotNull(companyCurrentYearPlans);
		assertEquals(String.valueOf(BSSHttpStatusConstants.OK),companyCurrentYearPlans.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS,companyCurrentYearPlans.getStatusMessage());
		assertEquals("Medical",companyCurrentYearPlans.getData().get(0).getOfferType());
		assertEquals("1001",companyCurrentYearPlans.getData().get(0).getPlanId());
		assertEquals("PPO",companyCurrentYearPlans.getData().get(0).getPlanName());
		assertEquals("1000",companyCurrentYearPlans.getData().get(0).getParentId());
	}
	
	@Test
	public void mappedPlansNotFound() {
		HttpServletRequest request = new MockHttpServletRequest();
		List<MappedPlanDetailDto> mappedPlanDetails =  new ArrayList<>();
		
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		Map<String, String> planYearDetailMap = new HashMap<>();
		planYearDetailMap.put(PlanCompareConstants.CURRENT.getAction(), "41");
		planYearDetailMap.put(PlanCompareConstants.FUTURE.getAction(), "51");
		
		when(planCompareService.findMappingBenefitPlansBy(company.getCode())).thenReturn(mappedPlanDetails);
		ReturnResponse<List<MappedPlanDetailDto>> companyCurrentYearPlans = planCompareController.mappingPlans(request, company.getCode());
		
		assertNotNull(companyCurrentYearPlans);
		assertEquals(String.valueOf(BSSHttpStatusConstants.NOT_FOUND),companyCurrentYearPlans.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS,companyCurrentYearPlans.getStatusMessage());
		assertEquals("There are no plans for the requested data",companyCurrentYearPlans.getStatusText());
	}
	
	@Test
	public void generatePlanCompareReport() throws Exception {
		Company company = new Company();
		company.setDescription("Test Company Inc");
		Map<String, LinkedHashSet<String>> comparePlans = new HashMap<>();
		LinkedHashSet<String> plans = new LinkedHashSet<>();
		plans.add("002EN3");
		plans.add("002EG8");
		comparePlans.put("002F60", plans);
		Map<String, Map<String, Set<String>>> plansToCompare = new LinkedHashMap<>();
		Workbook workbook = mock(Workbook.class);
		//comparePlans.entrySet().stream().forEach(comapre -> plansToCompare.put(comapre.getKey(), comapre.getValue()));
		//assertEquals(comparePlans.keySet(), plansToCompare.keySet());
		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, null)).thenReturn(company);
		when(planCompareFacade.generatePlanCompareReport(any(Company.class), anyMap(), any(HttpServletRequest.class))).thenReturn(workbook);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(URI_PLAN_COMPARE_EXPORT, COMPANY_CODE, EMPLID, COMPANY_CODE)
				.content(new ObjectMapper().writeValueAsString(plansToCompare))
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
		assertEquals("attachment; filename=Test_Company_Inc_ComparePlanAttributes.xlsx", response.getHeader("Content-disposition"));
	}
	
	private Supplier<MappedPlanDetailDto> getMappedPlans(){
		return () -> {
			MappedPlanDetailDto currentYearPlan = new MappedPlanDetailDto();
			currentYearPlan.setOfferType("Medical");
			currentYearPlan.setPlanId("1001");
			currentYearPlan.setPlanName("PPO");
			currentYearPlan.setParentId("1000");
			return currentYearPlan;
		};
	}
	
	@Test
	public void allPlans() {
		HttpServletRequest request = new MockHttpServletRequest();
		List<BenefitPlanDetailDto> currentYearPlans =  new ArrayList<>();
		assertNotNull(getCurrentYearPlans().get());
		assertEquals("Medical", getCurrentYearPlans().get().getOfferType());
		assertEquals("PPO", getCurrentYearPlans().get().getPlanName());
		currentYearPlans.add(getCurrentYearPlans().get());
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		Map<String, String> planYearDetailMap = new HashMap<>();
		planYearDetailMap.put(PlanCompareConstants.CURRENT.getAction(), "41");
		planYearDetailMap.put(PlanCompareConstants.FUTURE.getAction(), "51");
		
		when(planCompareService.findAllFutureYearPlansBy(company.getCode())).thenReturn(currentYearPlans);
		ReturnResponse<List<BenefitPlanDetailDto>> allPlans = planCompareController.getFutureYearPlans(request, company.getCode());
		
		assertNotNull(allPlans);
		assertEquals(String.valueOf(BSSHttpStatusConstants.OK),allPlans.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS,allPlans.getStatusMessage());
		assertEquals("Medical",allPlans.getData().get(0).getOfferType());
		assertEquals("1001",allPlans.getData().get(0).getPlanId());
		assertEquals("PPO",allPlans.getData().get(0).getPlanName());
	}
	
	@Test
	public void allPlansNotFound() {
		HttpServletRequest request = new MockHttpServletRequest();
		List<BenefitPlanDetailDto> currentYearPlans =  new ArrayList<>();
		
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		Map<String, String> planYearDetailMap = new HashMap<>();
		planYearDetailMap.put(PlanCompareConstants.CURRENT.getAction(), "41");
		planYearDetailMap.put(PlanCompareConstants.FUTURE.getAction(), "51");
		
		when(planCompareService.findAllFutureYearPlansBy(company.getCode())).thenReturn(currentYearPlans);
		ReturnResponse<List<BenefitPlanDetailDto>> companyCurrentYearPlans = planCompareController.getFutureYearPlans(request, company.getCode());
		
		
		assertNotNull(companyCurrentYearPlans);
		assertEquals(String.valueOf(BSSHttpStatusConstants.NOT_FOUND),companyCurrentYearPlans.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS,companyCurrentYearPlans.getStatusMessage());
		assertEquals("There are no plans for the requested data",companyCurrentYearPlans.getStatusText());
	}

	@Test
	public void getPlanCompareDetailsWithRecordsTest() {
		// given
		// data
		String companyCode = "SFSFJHDHJH787897SFSFFDFS";
		String exchange = "TNIII";
		List<Long> trinetStrategyIds = List.of(111L, 222L);
		List<PlanCompareDetailDto> planCompareDetailDtos = preparePlanCompareDetailDto();
		// method mocks
		when(planCompareFacade.getPlanCompareDetails(companyCode, exchange, trinetStrategyIds, httpRequest))
				.thenReturn(planCompareDetailDtos);
		// when
		ReturnResponse<List<PlanCompareDetailDto>> response = planCompareController
				.getPlanCompareDetails(httpRequest, trinetStrategyIds, companyCode, exchange);
		// then
		// assertions
		assertEquals(String.valueOf(BSSHttpStatusConstants.OK), response.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS, response.getStatusMessage());
		List<PlanCompareDetailDto> actualResult = response.getData();
		assertNotNull(actualResult);
		assertEquals(3, actualResult.size());
		// Medical
		assertMedical(actualResult);
		// Dental & Vision
		assertDentalAndVision(actualResult);
		// verify
		verify(planCompareFacade, times(1)).getPlanCompareDetails(companyCode, exchange, trinetStrategyIds, httpRequest);
	}

	@Test
	public void getPlanCompareDetailsWithNoRecordsTest() {
		// given
		// data
		String companyCode = "SFSFJHDHJH787897SFSFFDFS";
		String exchange = "TNIII";
		List<Long> trinetStrategyIds = List.of(111L, 222L);
		// method mocks
		when(planCompareFacade.getPlanCompareDetails(companyCode, exchange, trinetStrategyIds, httpRequest))
				.thenReturn(Collections.emptyList());
		// when
		ReturnResponse<List<PlanCompareDetailDto>> response = planCompareController
				.getPlanCompareDetails(httpRequest, trinetStrategyIds, companyCode, exchange);
		// then
		// assertions
		assertNotNull(response);
		assertEquals(String.valueOf(BSSHttpStatusConstants.NOT_FOUND), response.getStatusCode());
		assertEquals(RequestResponseConstants.SUCCESS, response.getStatusMessage());
		assertEquals("There are no plans for the requested data", response.getStatusText());
		// verify
		verify(planCompareFacade, times(1)).getPlanCompareDetails(companyCode, exchange, trinetStrategyIds, httpRequest);
	}

	private List<PlanCompareDetailDto> preparePlanCompareDetailDto() {
		return List.of(
				PlanCompareDetailDto.builder().benefitType("10")
						.planCompareData(List.of(
								PlanCompareDetailDto.PlanCompareData.builder()
										.currentPlan(PlanCompareDetailDto.BenPlanDetail.builder().planId("101")
												.planName("Aetna PPO 2000")
												.attributes(List.of(PlanCompareDetailDto.Attribute.builder().id(1)
														.name("Plan Type").value("ACO").build()))
												.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
														.regionCode(List.of("NY")).rateType("ageBaded")
														.tierRates(Collections.emptyList()).build()))

												.build())
										.futurePlans(
												List.of(PlanCompareDetailDto.BenPlanDetail.builder().planId("1")
														.planName("Blue Shield of California PPO")
														.attributes(List.of(PlanCompareDetailDto.Attribute.builder()
																.id(1).name("Primary Care Visit").value("$35").build()))
														.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
																.regionCode(List.of("CT"))
																.rateType("4Tier")
																.tierRates(List.of(
																		PlanCompareDetailDto.TierRate.builder()
																				.cvgTierCode("1").cost(BigDecimal
																						.valueOf(1234.12))
																				.build(),
																		PlanCompareDetailDto.TierRate.builder()
																				.cvgTierCode("2")
																				.cost(BigDecimal.valueOf(1234.13))
																				.build(),
																		PlanCompareDetailDto.TierRate.builder()
																				.cvgTierCode("C")
																				.cost(BigDecimal.valueOf(1234.12))
																				.build(),
																		PlanCompareDetailDto.TierRate.builder()
																				.cvgTierCode("4")
																				.cost(BigDecimal.valueOf(1234.12))
																				.build()))
																.build()))

														.build()))
										.build(),
								PlanCompareDetailDto.PlanCompareData.builder()
										.currentPlan(PlanCompareDetailDto.BenPlanDetail.builder().planId("102")
												.planName("Blue Shield of California")
												.attributes(List.of(PlanCompareDetailDto.Attribute.builder().id(1)
														.name("Plan Type").value("$45").build()))
												.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
														.regionCode(List.of("PA"))
														.rateType("ageBaded").tierRates(Collections.emptyList())
														.build()))
												.build())
										.futurePlans(List.of(PlanCompareDetailDto.BenPlanDetail.builder().planId("1")
												.planName("Blue Shield of California PPO")
												.attributes(List.of(PlanCompareDetailDto.Attribute.builder().id(1)
														.name("Primary Care Visit").value("$55").build()))
												.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
														.regionCode(List.of("CT")).rateType("4Tier")
														.tierRates(List.of(
																PlanCompareDetailDto.TierRate.builder().cvgTierCode("1")
																		.cost(BigDecimal.valueOf(1234.12)).build(),
																PlanCompareDetailDto.TierRate.builder().cvgTierCode("2")
																		.cost(null).build(),
																PlanCompareDetailDto.TierRate.builder().cvgTierCode("C")
																		.cost(BigDecimal.valueOf(1234.12)).build(),
																PlanCompareDetailDto.TierRate.builder().cvgTierCode("4")
																		.cost(null).build()))
														.build()))
												.build()))
										.build()))
						.build(),
				PlanCompareDetailDto.builder().benefitType("11")
						.planCompareData(List.of(PlanCompareDetailDto.PlanCompareData.builder()
								.currentPlan(PlanCompareDetailDto.BenPlanDetail.builder().planId("201")
										.planName("MetLife Voluntary VA")
										.attributes(List.of(PlanCompareDetailDto.Attribute.builder().id(1)
												.name("Plan Type").value("PPO").build()))
										.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
												.regionCode(List.of("NY")).rateType("ageBaded")
												.tierRates(Collections.emptyList()).build()))

										.build())
								.futurePlans(List.of(PlanCompareDetailDto.BenPlanDetail.builder().planId("1")
										.planName("Aetna Dental 50")
										.attributes(List.of(PlanCompareDetailDto.Attribute.builder().id(1)
												.name("Out-of-Network").value("100/80/50").build()))
										.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
												.regionCode(List.of("PR")).rateType("4Tier")
												.tierRates(List.of(
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("1")
																.cost(BigDecimal.valueOf(2234.11)).build(),
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("2")
																.cost(BigDecimal.valueOf(1234.12)).build(),
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("C")
																.cost(BigDecimal.valueOf(7777.99)).build(),
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("4")
																.cost(BigDecimal.valueOf(1234.12)).build()))
												.build()))

										.build()))
								.build()))
						.build(),
				PlanCompareDetailDto.builder().benefitType("14")
						.planCompareData(List.of(PlanCompareDetailDto.PlanCompareData.builder()
								.currentPlan(PlanCompareDetailDto.BenPlanDetail.builder().planId("301")
										.planName("MetLife Voluntary NV")
										.attributes(List.of(PlanCompareDetailDto.Attribute.builder().id(1)
												.name("In-Network").value("$10").build()))
										.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
												.regionCode(List.of("NY")).rateType("ageBaded")
												.tierRates(Collections.emptyList()).build()))

										.build())
								.futurePlans(List.of(PlanCompareDetailDto.BenPlanDetail.builder().planId("1")
										.planName("EyeMed Vision Plus")
										.attributes(List.of(PlanCompareDetailDto.Attribute.builder().id(1)
												.name("Out-of-Network").value("Reimbursed up to $50").build()))
										.rates(List.of(PlanCompareDetailDto.RateDetail.builder()
												.regionCode(List.of("CT")).rateType("4Tier")
												.tierRates(List.of(
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("1")
																.cost(BigDecimal.valueOf(2234.11)).build(),
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("2")
																.cost(null).build(),
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("C")
																.cost(BigDecimal.valueOf(7777.99)).build(),
														PlanCompareDetailDto.TierRate.builder().cvgTierCode("4")
																.cost(null).build()))
												.build()))

										.build()))
								.build()))
						.build()

		);
	}

	private void assertMedical(List<PlanCompareDetailDto> actualResult) {
		assertEquals("10", actualResult.get(0).getBenefitType());
		assertEquals("101", actualResult.get(0).getPlanCompareData().get(0).getCurrentPlan().getPlanId());
		assertEquals("Aetna PPO 2000", actualResult.get(0).getPlanCompareData().get(0).getCurrentPlan().getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(0).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getId());
		assertEquals("Plan Type",
				actualResult.get(0).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getName());
		assertEquals("ACO",
				actualResult.get(0).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getValue());
		assertEquals("NY",
				actualResult.get(0).getPlanCompareData().get(0).getCurrentPlan().getRates().get(0).getRegionCode().get(0));
		assertEquals("ageBaded",
				actualResult.get(0).getPlanCompareData().get(0).getCurrentPlan().getRates().get(0).getRateType());

		assertEquals("1", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getPlanId());
		assertEquals("Blue Shield of California PPO",
				actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getAttributes().get(0).getId());
		assertEquals("Primary Care Visit", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getAttributes().get(0).getName());
		assertEquals("$35", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getAttributes()
				.get(0).getValue());
		assertEquals("CT", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getRegionCode().get(0));
		assertEquals("4Tier", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getRateType());
		assertEquals("1", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(0).getCvgTierCode());
		assertEquals(new BigDecimal("1234.12"), actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(0).getCost());
		assertEquals("2", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(1).getCvgTierCode());
		assertEquals(new BigDecimal("1234.13"), actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(1).getCost());
		assertEquals("C", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(2).getCvgTierCode());
		assertEquals(new BigDecimal("1234.12"), actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(2).getCost());
		assertEquals("4", actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(3).getCvgTierCode());
		assertEquals(new BigDecimal("1234.12"), actualResult.get(0).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(3).getCost());

		assertEquals("102", actualResult.get(0).getPlanCompareData().get(1).getCurrentPlan().getPlanId());
		assertEquals("Blue Shield of California",
				actualResult.get(0).getPlanCompareData().get(1).getCurrentPlan().getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(0).getPlanCompareData().get(1).getCurrentPlan().getAttributes().get(0).getId());
		assertEquals("Plan Type",
				actualResult.get(0).getPlanCompareData().get(1).getCurrentPlan().getAttributes().get(0).getName());
		assertEquals("$45",
				actualResult.get(0).getPlanCompareData().get(1).getCurrentPlan().getAttributes().get(0).getValue());
		assertEquals("PA",
				actualResult.get(0).getPlanCompareData().get(1).getCurrentPlan().getRates().get(0).getRegionCode().get(0));
		assertEquals("ageBaded",
				actualResult.get(0).getPlanCompareData().get(1).getCurrentPlan().getRates().get(0).getRateType());

		assertEquals("1", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getPlanId());
		assertEquals("Blue Shield of California PPO",
				actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getAttributes().get(0).getId());
		assertEquals("Primary Care Visit", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0)
				.getAttributes().get(0).getName());
		assertEquals("$55", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getAttributes()
				.get(0).getValue());
		assertEquals("CT", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getRegionCode().get(0));
		assertEquals("4Tier", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getRateType());
		assertEquals("1", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(0).getCvgTierCode());
		assertEquals(new BigDecimal("1234.12"), actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(0).getCost());
		assertEquals("2", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(1).getCvgTierCode());
		assertNull(actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(1).getCost());
		assertEquals("C", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(2).getCvgTierCode());
		assertEquals(new BigDecimal("1234.12"), actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(2).getCost());
		assertEquals("4", actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(3).getCvgTierCode());
		assertNull(actualResult.get(0).getPlanCompareData().get(1).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(3).getCost());

	}

	private void assertDentalAndVision(List<PlanCompareDetailDto> actualResult) {
		// Dental
		assertEquals("11", actualResult.get(1).getBenefitType());
		assertEquals("201", actualResult.get(1).getPlanCompareData().get(0).getCurrentPlan().getPlanId());
		assertEquals("MetLife Voluntary VA",
				actualResult.get(1).getPlanCompareData().get(0).getCurrentPlan().getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(1).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getId());
		assertEquals("Plan Type",
				actualResult.get(1).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getName());
		assertEquals("PPO",
				actualResult.get(1).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getValue());
		assertEquals("NY",
				actualResult.get(1).getPlanCompareData().get(0).getCurrentPlan().getRates().get(0).getRegionCode().get(0));
		assertEquals("ageBaded",
				actualResult.get(1).getPlanCompareData().get(0).getCurrentPlan().getRates().get(0).getRateType());

		assertEquals("1", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getPlanId());
		assertEquals("Aetna Dental 50",
				actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getAttributes().get(0).getId());
		assertEquals("Out-of-Network", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getAttributes().get(0).getName());
		assertEquals("100/80/50", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getAttributes().get(0).getValue());
		assertEquals("PR", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getRegionCode().get(0));
		assertEquals("4Tier", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getRateType());
		assertEquals("1", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(0).getCvgTierCode());
		assertEquals(new BigDecimal("2234.11"), actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(0).getCost());
		assertEquals("2", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(1).getCvgTierCode());
		assertEquals(new BigDecimal("1234.12"), actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(1).getCost());
		assertEquals("C", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(2).getCvgTierCode());
		assertEquals(new BigDecimal("7777.99"), actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(2).getCost());
		assertEquals("4", actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(3).getCvgTierCode());
		assertEquals(new BigDecimal("1234.12"), actualResult.get(1).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(3).getCost());

		// Vision
		assertEquals("14", actualResult.get(2).getBenefitType());
		assertEquals("301", actualResult.get(2).getPlanCompareData().get(0).getCurrentPlan().getPlanId());
		assertEquals("MetLife Voluntary NV",
				actualResult.get(2).getPlanCompareData().get(0).getCurrentPlan().getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(2).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getId());
		assertEquals("In-Network",
				actualResult.get(2).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getName());
		assertEquals("$10",
				actualResult.get(2).getPlanCompareData().get(0).getCurrentPlan().getAttributes().get(0).getValue());
		assertEquals("NY",
				actualResult.get(2).getPlanCompareData().get(0).getCurrentPlan().getRates().get(0).getRegionCode().get(0));
		assertEquals("ageBaded",
				actualResult.get(2).getPlanCompareData().get(0).getCurrentPlan().getRates().get(0).getRateType());

		assertEquals("1", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getPlanId());
		assertEquals("EyeMed Vision Plus",
				actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getPlanName());
		assertEquals(Integer.valueOf(1),
				actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getAttributes().get(0).getId());
		assertEquals("Out-of-Network", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getAttributes().get(0).getName());
		assertEquals("Reimbursed up to $50", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getAttributes().get(0).getValue());
		assertEquals("CT", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getRegionCode().get(0));
		assertEquals("4Tier", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getRateType());
		assertEquals("1", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(0).getCvgTierCode());
		assertEquals(new BigDecimal("2234.11"), actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(0).getCost());
		assertEquals("2", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(1).getCvgTierCode());
		assertNull(actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(1).getCost());
		assertEquals("C", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(2).getCvgTierCode());
		assertEquals(new BigDecimal("7777.99"), actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0)
				.getRates().get(0).getTierRates().get(2).getCost());
		assertEquals("4", actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(3).getCvgTierCode());
		assertNull(actualResult.get(2).getPlanCompareData().get(0).getFuturePlans().get(0).getRates().get(0)
				.getTierRates().get(3).getCost());

	}

}