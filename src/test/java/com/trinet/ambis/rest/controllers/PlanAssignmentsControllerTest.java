package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.prospect.PlanAssignmentsResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.PlanAssignmentsResponse.PlanAssignmentItem;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanAssignmentsService;
import com.trinet.ambis.service.dto.BasePlansResDto;
import com.trinet.ambis.service.model.planAvailability.EligibleEmployeePlanResponse;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class PlanAssignmentsControllerTest extends ServiceUnitTest {

	@InjectMocks
	private PlanAssignmentsController planAssignmentsController;

	@Mock
	private PlanAssignmentsService planAssignmentsService;

	@Mock
	CompanyService companyService;

	private MockMvc mockMvc;
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<CompanyServiceHelper> mockStaticCompanyServiceHelper;

	private static final String AUTHENTICATED_PERSON_ID = "0000000123456";
	private static final String API_URL = URIConstants.VERSION_AND_ROOT + URIConstants.PLAN_ASSIGNMENTS_BY_STRATEGY;
	private static final String API_GET_URL = URIConstants.VERSION_AND_ROOT
			+ URIConstants.PLAN_ASSIGNMENTS_BY_STRATEGY_GROUP;
	private static final String API_GET_BASE_PLAN_ELIGIBLE_EMPL_URL = URIConstants.VERSION_AND_ROOT
			+ URIConstants.PLAN_ASSIGNMENT_BASE_PLAN_ELIGIBLE_EMPL;
	private static final String EMPLOYEE_ID = "00001234567";
	private static final String EMPLOYEE_NAME = "Action Johnny";
	private static final String BENEFIT_PLAN = "001ABC";
	private static final long STRATEGY_ID = 1234L;
	private static final long GROUP_ID = 1234L;
	private static final String EXCHANGE_ID = "OMS";
	private static final String COMPANY_CODE = "ABC123";

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(AUTHENTICATED_PERSON_ID);
		mockStaticCompanyServiceHelper = Mockito.mockStatic(CompanyServiceHelper.class);
		mockMvc = MockMvcBuilders.standaloneSetup(planAssignmentsController).build();
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
		if (mockStaticCompanyServiceHelper != null) {
			mockStaticCompanyServiceHelper.close();
			mockStaticCompanyServiceHelper = null;
		}
	}

	@Test
	public void getPlanAssignmentsTest() throws Exception {
		// given
		List<PlanAssignmentsResponse> returnedObject = preparePlanAssignmentsResponse();
		Company company = new Company();
		when(planAssignmentsService.getPlanAssignments(STRATEGY_ID, GROUP_ID, company)).thenReturn(returnedObject);
	    	when(companyService.getCompanyDetails(anyString(), anyBoolean(),
		    anyString(), any(BenExchngEnums.class))).thenReturn(company);

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get(API_GET_URL, "001", EMPLOYEE_ID, STRATEGY_ID, GROUP_ID, COMPANY_CODE)
				.param("exchangeId", BenExchngEnums.TRINET_III.getExchangeId())
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();

		// then
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals(
				"{\"data\":[{\"employeeId\":\"00001234567\",\"employeeName\":\"Action Johnny\",\"homeState\":\"CO\",\"homeZipCode\":\"12345\",\"medicalCvgCd\":\"1\",\"dentalCvgCd\":\"2\",\"visionCvgCd\":\"C\",\"planAssignment\":[{\"benefitPlanId\":\"001ABC\",\"bplPlanId\":null,\"benefitType\":\"medical\",\"benefitPlanName\":\"\",\"totalCost\":null}],\"prospectPlanAssignment\":[{\"benefitPlanId\":\"001ABC\",\"bplPlanId\":\"BPL_PLAN_ID\",\"benefitType\":\"medical\",\"benefitPlanName\":\"Cigna PPO 1000\",\"totalCost\":100}]}],\"_statusCode\":\"200\"}",
				response.getContentAsString());
	}

	@Test
	public void getPlanRatesByEmployeeTest() throws Exception {
		// given
		String exchangeId = "TNIII";
		Company company = new Company();
		when(companyService.getCompanyDetails(anyString(), anyBoolean(),
				anyString(), any(BenExchngEnums.class))).thenReturn(company);
		BigDecimal expectedRate = new BigDecimal("123.45");
		when(planAssignmentsService.getOmsPlanRateByEmployee(company, EMPLOYEE_ID, BENEFIT_PLAN, "COVERAGE_LEVEL", "10"))
				.thenReturn(expectedRate);

		// when
		String apiUrl = URIConstants.VERSION_AND_ROOT + URIConstants.PLAN_ASSIGNMENT_PLAN_RATE_BY_EMPLOYEE;
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get(apiUrl, "001", EMPLOYEE_ID, EMPLOYEE_ID, BENEFIT_PLAN, "COVERAGE_LEVEL", "10",COMPANY_CODE)
				.param("exchangeId", exchangeId)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();

		// then
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{\"data\":123.45,\"_statusCode\":\"200\"}", response.getContentAsString());
	}

	@Test
	public void createPlanAssignmentsTest() throws Exception {
		// given
		String reqJson = preparePlanAssignmentsRequest();

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.put(API_URL, "001", EMPLOYEE_ID, STRATEGY_ID, COMPANY_CODE).accept(MediaType.APPLICATION_JSON)
				.content(reqJson).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();

		// then
		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

    @Test
    public void getEmployeeRegionalPlanForBasePlanTest() throws Exception {
        // given
        String exchangeId = "TNIII";
        Company company = new Company();
        String basePlanId = "BASE_PLAN_ID";
        String benefitType = "10";
        when(companyService.getCompanyDetails(anyString(), anyBoolean(), anyString(), any(BenExchngEnums.class))).thenReturn(company);
        List<EligibleEmployeePlanResponse> returnedObject = prepareEligibleEmployeePlanResponse();
        when(planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, basePlanId, benefitType)).thenReturn(returnedObject);

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(API_GET_BASE_PLAN_ELIGIBLE_EMPL_URL, "001", EMPLOYEE_ID, STRATEGY_ID, GROUP_ID, COMPANY_CODE).param("exchangeId", exchangeId)
                .param("basePlanId", basePlanId).param("benefitType", benefitType).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        MockHttpServletResponse response = result.getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("{\"data\":[{\"employeeId\":\"00001234567\",\"planId\":\"001ABC\"}],\"_statusCode\":\"200\"}", response.getContentAsString());
    }

	@Test
	public void getBasePlansTest() throws Exception {
		// given
		String getBasePlansURL = URIConstants.VERSION_AND_ROOT + URIConstants.PLAN_ASSIGNMENT_BASE_PLANS;
		Company company = new Company();
		mockStaticCompanyServiceHelper.when(() -> CompanyServiceHelper.isTibProspect(company)).thenReturn(false);
		when(companyService.getCompanyDetails(anyString(), anyBoolean(),
				anyString(), any(BenExchngEnums.class))).thenReturn(company);
		when(planAssignmentsService.getBasePlans(STRATEGY_ID, GROUP_ID, company)).thenReturn(prepareGetBasePlansMock());

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get(getBasePlansURL, "001", EMPLOYEE_ID, COMPANY_CODE)
				.queryParam("strategyId", String.valueOf( STRATEGY_ID ) )
				.queryParam("groupId", String.valueOf( GROUP_ID ) )
				.queryParam("exchangeId", EXCHANGE_ID);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		String responseJSON = result.getResponse().getContentAsString();

		// then
		assertEquals("{\"data\":[{\"benType\":\"vision\",\"plans\":[{\"planId\":\"002M7Z\",\"planName\":\"VSP Vision\"}]}],\"_statusCode\":\"200\"}"
				, responseJSON);
	}

	@Test
	public void getBasePlansNoExchangeIdTest() throws Exception {
		// given
		String getBasePlansURL = URIConstants.VERSION_AND_ROOT + URIConstants.PLAN_ASSIGNMENT_BASE_PLANS;
		Company company = new Company();
		mockStaticCompanyServiceHelper.when(() -> CompanyServiceHelper.isTibProspect(company)).thenReturn(false);

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get(getBasePlansURL, "001", EMPLOYEE_ID, COMPANY_CODE)
				.queryParam("strategyId", String.valueOf( STRATEGY_ID ) )
				.queryParam("groupId", String.valueOf( GROUP_ID ) );

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		String responseJSON = result.getResponse().getContentAsString();

		assertEquals("{\"data\":[],\"_statusCode\":\"200\"}", responseJSON);
	}


	private static List<PlanAssignmentsResponse> preparePlanAssignmentsResponse() {
		List<PlanAssignmentsResponse> list = new ArrayList<>();
		PlanAssignmentsResponse response = PlanAssignmentsResponse.builder().employeeId(EMPLOYEE_ID)
				.employeeName(EMPLOYEE_NAME).homeState("CO").homeZipCode("12345").medicalCvgCd("1").dentalCvgCd("2")
				.visionCvgCd("C").build();

		List<PlanAssignmentItem> plans = new ArrayList<>();
		plans.add(PlanAssignmentItem.builder().benefitPlanId(BENEFIT_PLAN).benefitType("medical").benefitPlanName("").totalCost(null).build());

		List<PlanAssignmentItem> prospectPlans = new ArrayList<>();
		prospectPlans.add(PlanAssignmentItem.builder().benefitPlanId(BENEFIT_PLAN).bplPlanId("BPL_PLAN_ID")
				.benefitType("medical").benefitPlanName("Cigna PPO 1000").totalCost(new BigDecimal(100)).build());

		response.setProspectPlanAssignment(prospectPlans);
		response.setPlanAssignment(plans);
		list.add(response);
		return list;
	}

	private static String preparePlanAssignmentsRequest() {

		String list = "[{\"employeeId\":\"" + EMPLOYEE_ID + "\",\"benefitPlanId\":\"" + BENEFIT_PLAN
				+ "\",\"benefitType\":\"" + "10" + "\",\"coverageCode\":\"" + "C" + "\",\"portfolioId\":\"" + 1
				+ "\" }]";
		return list;
	}

	private static List<BasePlansResDto> prepareGetBasePlansMock() {
		List<BasePlansResDto> list = new ArrayList<>();
		list.add( BasePlansResDto.builder()
				.benType("vision")
				.plans( new ArrayList<>( Arrays.asList(
						BasePlansResDto.Plan.builder()
								.planId( "002M7Z" )
								.planName( "VSP Vision" )
								.build()
							)
						)
					)
				.build()
			);

		return list;
	}

	private static List<EligibleEmployeePlanResponse> prepareEligibleEmployeePlanResponse() {
		List<EligibleEmployeePlanResponse> list = new ArrayList<>();
		EligibleEmployeePlanResponse response = EligibleEmployeePlanResponse.builder().employeeId(EMPLOYEE_ID)
				.planId(BENEFIT_PLAN).build();
		list.add(response);
		return list;
	}

}
