package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes.BenefitPlan;
import com.trinet.ambis.service.prospect.exception.ProspectApiCallException;
import com.trinet.ambis.service.prospect.impl.ProspectEmployeeServiceImpl;
import com.trinet.ambis.service.prospect.response.ApiRes;
import com.trinet.ambis.service.prospect.response.CensusRes;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ProspectServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class ProspectEmployeeServiceTest extends ServiceUnitTest {

	@InjectMocks
	private ProspectEmployeeServiceImpl prospectEmployeeService;

	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private ProspectServiceRestClient prospectServiceRestClient;

	@Captor
	private ArgumentCaptor<String> uriCaptor;

	@Captor
	private ArgumentCaptor<HttpMethod> httpMethodCaptor;

	@Captor
	private ArgumentCaptor<HttpEntity<?>> httpEntityCaptor;

	@Captor
	private ArgumentCaptor<ParameterizedTypeReference<ApiRes<List<EmployeePlansRes>>>> responseTypeCaptor;
	
	@Captor
	private ArgumentCaptor<ProspectApiRequest> apiGetRequestCaptor;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;

    @Before
    public void setUp() {
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
    }

    @After
    public void tearDown() {
        if (mockStaticBSSMessageConfig != null) mockStaticBSSMessageConfig.close();
    }
	/**
	 * given prospect id </br>
	 * when getEmployeePlans method is called </br>
	 * then return EmployeePlansRes</br>
	 **/
	@Test
	public void getEmployeePlansTest1() {
		// given
		// data
		String prospectId = "P1PC1";
		ResponseEntity<ApiRes<List<EmployeePlansRes>>> responseEntity = buildResponseEntity();
		// method mocks
		when(restTemplate.exchange(uriCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture())).thenReturn(responseEntity);
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
				.thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_EMPLOYEE_PLAN_ASSIGNMENT_API_URI))
				.thenReturn("/employee-plans");
		// when
		Optional<List<EmployeePlansRes>> actualResult = prospectEmployeeService.getEmployeePlans(prospectId);
		// then
		// assertions
		assertTrue(actualResult.isPresent());
		List<EmployeePlansRes> employeePlansRes = actualResult.get();
		assertEquals(3, employeePlansRes.size());
		EmployeePlansRes employeePlansRes1 = employeePlansRes.get(0);
		assertEquals("E1", employeePlansRes1.getEmployeeId());
		List<BenefitPlan> benefitPlans1 = employeePlansRes1.getBenefitPlans();
		assertEquals(1, benefitPlans1.size());
		assertEquals("1", benefitPlans1.get(0).getBenefitPlanId());
		assertEquals("10", benefitPlans1.get(0).getBenefitTypeCode());
		EmployeePlansRes employeePlansRes2 = employeePlansRes.get(1);
		assertEquals("E2", employeePlansRes2.getEmployeeId());
		List<BenefitPlan> benefitPlans2 = employeePlansRes2.getBenefitPlans();
		assertEquals(1, benefitPlans2.size());
		assertEquals("2", benefitPlans2.get(0).getBenefitPlanId());
		assertEquals("11", benefitPlans2.get(0).getBenefitTypeCode());
		EmployeePlansRes employeePlansRes3 = employeePlansRes.get(2);
		assertEquals("E3", employeePlansRes3.getEmployeeId());
		List<BenefitPlan> benefitPlans3 = employeePlansRes3.getBenefitPlans();
		assertEquals(1, benefitPlans3.size());
		assertEquals("3", benefitPlans3.get(0).getBenefitPlanId());
		assertEquals("14", benefitPlans3.get(0).getBenefitTypeCode());
		// verify
		verify(restTemplate, times(1)).exchange(uriCaptor.getValue(), httpMethodCaptor.getValue(),
				httpEntityCaptor.getValue(), responseTypeCaptor.getValue());
	}

	/**
	 * given prospect id prospect api call is not successful due to bad request</br>
	 * when getEmployeePlans is called </br>
	 * then throw ProspectApiCallException</br>
	 **/
	@Test
	public void getEmployeePlansTest2() {
		// given
		// data
		String prospectId = "P1PC1";
		ResponseEntity<ApiRes<List<EmployeePlansRes>>> responseEntity = buildErrorResponseEntity();
		// method mocks
		when(restTemplate.exchange(uriCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture())).thenReturn(responseEntity);
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
				.thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_EMPLOYEE_PLAN_ASSIGNMENT_API_URI))
				.thenReturn("/employee-plans");
		exception.expect(ProspectApiCallException.class);
		exception.expectMessage("Error occured while getting prospect's employee plan assignment.");
		// when
		prospectEmployeeService.getEmployeePlans(prospectId);
		// then
		// verify
		verify(restTemplate, times(1)).exchange(uriCaptor.getValue(), httpMethodCaptor.getValue(),
				httpEntityCaptor.getValue(), responseTypeCaptor.getValue());
	}

	/**
	 * given plan ids and prospect api call is not successful due null response
	 * entity data</br>
	 * when getBenefitsPlansRateDetails is called </br>
	 * then throw ProspectApiCallException</br>
	 **/
	@Test
	public void getEmployeePlansTest3() {
		// given
		// data
		String prospectId = "P1PC1";
		ResponseEntity<ApiRes<List<EmployeePlansRes>>> responseEntity = buildErrorResponseEntity1();
		// method mocks
		when(restTemplate.exchange(uriCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(),
				responseTypeCaptor.capture())).thenReturn(responseEntity);
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
				.thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
		when(BSSMessageConfig.getProperty(ProspectURIConstants.PROPERTY_EMPLOYEE_PLAN_ASSIGNMENT_API_URI))
				.thenReturn("/employee-plans");
		exception.expect(ProspectApiCallException.class);
		exception.expectMessage("Error occured while getting prospect's employee plan assignment.");
		// when
		prospectEmployeeService.getEmployeePlans(prospectId);
		// then
		// verify
		verify(restTemplate, times(1)).exchange(uriCaptor.getValue(), httpMethodCaptor.getValue(),
				httpEntityCaptor.getValue(), responseTypeCaptor.getValue());
	}
	
	/**
	 * given prospect id </br>
	 * when getEmployees is called </br>
	 * then return employee details</br>
	 **/
	@Test
	public void getEmployeesTest1() {
		// given
		// data
		String prospectId = "P1PC1";
		List<CensusRes> censusResponse = buildCensusResponse();
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiGetRequestCaptor.capture()))
				.thenReturn(censusResponse);
		// when
		List<CensusRes> actualResult = prospectEmployeeService.getEmployees(prospectId);
		// then
		// assertions
		assertEquals(2, actualResult.size());
		assertEquals(censusResponse, actualResult);
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(apiGetRequestCaptor.getValue());
	}
	
	/**
	 * given prospect id and prospect api call is not successful </br>
	 * when getEmployees is called </br>
	 * then return empty response</br>
	 **/
	@Test
	public void getEmployeesTest2() {
		// given
		// data
		String prospectId = "P1PC1";
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiGetRequestCaptor.capture())).thenReturn(null);
		// when
		List<CensusRes> actualResult = prospectEmployeeService.getEmployees(prospectId);
		// then
		// assertions
		assertEquals(0, actualResult.size());
		assertTrue(CollectionUtils.isEmpty(actualResult));
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(apiGetRequestCaptor.getValue());
	}

	private ResponseEntity<ApiRes<List<EmployeePlansRes>>> buildResponseEntity() {
		return new ResponseEntity<ApiRes<List<EmployeePlansRes>>>(buildApiRes(), HttpStatus.OK);
	}

	private ApiRes<List<EmployeePlansRes>> buildApiRes() {
		ApiRes<List<EmployeePlansRes>> apiRes = new ApiRes<List<EmployeePlansRes>>();
		apiRes.setData(buildEmployeePlansRes());
		return apiRes;
	}

	private ResponseEntity<ApiRes<List<EmployeePlansRes>>> buildErrorResponseEntity() {
		return new ResponseEntity<ApiRes<List<EmployeePlansRes>>>(buildErrorApiRes(), HttpStatus.OK);
	}

	private ApiRes<List<EmployeePlansRes>> buildErrorApiRes() {
		ApiRes<List<EmployeePlansRes>> apiRes = new ApiRes<List<EmployeePlansRes>>();
		apiRes.setData(null);
		apiRes.setError(new ApiRes.Error());
		return apiRes;
	}

	private ResponseEntity<ApiRes<List<EmployeePlansRes>>> buildErrorResponseEntity1() {
		return new ResponseEntity<ApiRes<List<EmployeePlansRes>>>(buildNullResponseEntity(), HttpStatus.OK);
	}

	private ApiRes<List<EmployeePlansRes>> buildNullResponseEntity() {
		return null;
	}

	private List<EmployeePlansRes> buildEmployeePlansRes() {
		return List.of(EmployeePlansRes.builder().employeeId("E1")
				.benefitPlans(List.of(BenefitPlan.builder().benefitPlanId("1").benefitTypeCode("10").build())).build(),
				EmployeePlansRes.builder().employeeId("E2")
						.benefitPlans(List.of(BenefitPlan.builder().benefitPlanId("2").benefitTypeCode("11").build()))
						.build(),
				EmployeePlansRes.builder().employeeId("E3")
						.benefitPlans(List.of(BenefitPlan.builder().benefitPlanId("3").benefitTypeCode("14").build()))
						.build());
	}
	
	private List<CensusRes> buildCensusResponse() {
		return List.of(CensusRes.builder().employeeId("E1").employeeName("John Doe").age(22).k1(true).build(),
				CensusRes.builder().employeeId("E2").employeeName("Katty Scott").age(31).k1(false).build());
	}

}