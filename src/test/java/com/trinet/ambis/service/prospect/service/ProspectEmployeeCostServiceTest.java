package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.configuration.BSSMessageConfig;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.exception.ProspectApiCallException;
import com.trinet.ambis.service.prospect.service.impl.ProspectEmployeeCostServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ProspectServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class ProspectEmployeeCostServiceTest extends ServiceUnitTest {

	@InjectMocks
	private ProspectEmployeeCostServiceImpl prospectEmployeeCostService;

	@Mock
	private ProspectServiceRestClient prospectServiceRestClient;

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
	 * given benefit types
	 * when getProspectEmployeeCostByType is called
	 * then return EmployeeCostRes for the benefit types
	 **/
	@Test
	public void getProspectEmployeeCostByTypeTest1() {
		// given
		// data
		String prospectId = "P1PC1";
		List<String> benefitTypes = List.of("10", "11");

		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any())).thenReturn(buildEmployeeCostRes());

		// when
		Optional<List<EmployeeCostRes>> actualResultOptional = prospectEmployeeCostService.getProspectEmployeeCostByType(prospectId, benefitTypes);
		List<EmployeeCostRes> actualResult = actualResultOptional.orElseThrow(() -> new ProspectApiCallException("No data found for the given prospect id and benefit types"));
		// then
		// assertions
		assertEquals(1, actualResult.size());
		assertEquals("10", actualResult.get(0).getBenefitTypeCode());

		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any());
	}

	/**
	 * given empty list of benefit types
	 * when getProspectEmployeeCostByType is called
	 * then return empty EmployeeCostRes for the benefit types
	 **/
	@Test
	public void getProspectEmployeeCostByTypeTest2() {
		// given
		// data
		String prospectId = "P1PC1";
		List<String> benefitTypes = new ArrayList<>();

		// when
		Optional<List<EmployeeCostRes>> actualResultOptional = prospectEmployeeCostService.getProspectEmployeeCostByType(prospectId, benefitTypes);
		List<EmployeeCostRes> actualResult = actualResultOptional.orElseThrow(() -> new ProspectApiCallException("No data found for the given prospect id and benefit types"));
		// then
		// assertions
		assertTrue(actualResult.isEmpty());

		// verify
		verify(prospectServiceRestClient, times(0)).prepareRequestAndCallEndPoint(any());
	}

	/**
	 * given benefit types
	 * when getProspectEmployeeCostByType is called and response is null
	 * then return empty EmployeeCostRes for the benefit types
	 **/
	@Test
	public void getProspectEmployeeCostByTypeTest3() {
		// given
		// data
		String prospectId = "P1PC1";
		List<String> benefitTypes = List.of("10", "11");

		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any())).thenReturn(null);

		// when
		Optional<List<EmployeeCostRes>> actualResultOptional = prospectEmployeeCostService.getProspectEmployeeCostByType(prospectId, benefitTypes);
		List<EmployeeCostRes> actualResult = actualResultOptional.orElseThrow(() -> new ProspectApiCallException("No data found for the given prospect id and benefit types"));
		// then
		// assertions
		assertTrue(actualResult.isEmpty());

		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any());
	}

	private List<EmployeeCostRes> buildEmployeeCostRes() {
		return List.of(
				EmployeeCostRes.builder().benefitTypeCode("10")
						.employeePlanContribution(List.of(EmployeeCostRes.EmployeePlanContribution.builder()
										.employeeId("EMPLOYEE_1")
										.firstName("John")
										.lastName("Doe")
										.state("CA")
										.covgLevel("1")
										.groupId(1001)
										.groupName("GROUP_1")
										.planContribution(EmployeeCostRes.PlanContribution.builder()
												.benefitPlanId(1)
												.benefitPlanName("Health Plan A")
												.eeCost(BigDecimal.valueOf(100.11))
												.erCost(BigDecimal.valueOf(200.22))
												.totalCost(BigDecimal.valueOf(300.33))
												.build()).build())).build());

	}
}
