package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.EmployeeSourceData;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.prospect.impl.ProspectGroupAssignmentServiceImpl;
import com.trinet.ambis.service.prospect.response.BenefitGroupAssignmentRes;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ProspectServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class ProspectGroupAssignmentServiceTest extends ServiceUnitTest {

	@InjectMocks
	private ProspectGroupAssignmentServiceImpl prospectGroupAssignmentService;

	@Mock
	private ProspectServiceRestClient prospectServiceRestClient;

	@Captor
	private ArgumentCaptor<ProspectApiRequest> apiGetRequestCaptor;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * given prospect id </br>
	 * when getBenefitGroupAssignments is called </br>
	 * then return employee group assignment details</br>
	 **/
	@Test
	public void getBenefitGroupAssignmentsTest1() {
		// given
		// data
		String prospectId = "P1PC1";
		List<BenefitGroupAssignmentRes> benefitGroupAssignments = buildBenefitGroupAssignmentDetails();
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiGetRequestCaptor.capture()))
				.thenReturn(benefitGroupAssignments);
		// when
		Set<EmployeeData> actualResult = prospectGroupAssignmentService.getEmployeeGroupAssignments(prospectId);
		// then
		// assertions
		assertEquals(2, actualResult.size());
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(apiGetRequestCaptor.getValue());
	}

	/**
	 * given prospect id and prospect api call is not successful </br>
	 * when getBenefitGroupAssignments is called </br>
	 * then return empty response</br>
	 **/
	@Test
	public void getBenefitGroupAssignmentsTest2() {
		// given
		// data
		String prospectId = "P1PC1";
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiGetRequestCaptor.capture())).thenReturn(null);
		// when
		Set<EmployeeData> actualResult = prospectGroupAssignmentService.getEmployeeGroupAssignments(prospectId);
		// then
		// assertions
		assertEquals(0, actualResult.size());
		assertTrue(CollectionUtils.isEmpty(actualResult));
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(apiGetRequestCaptor.getValue());
	}

	/**
	 * given strategy Id is 0 </br>
	 * when updateEmployeeGroupAssignment method is called </br>
	 * then update employee group assignment on prospect service </br>
	 **/
	@Test
	public void updateEmployeeGroupAssignmentTest() {
		// given
		// data
		long strategyId = 0;
		EmployeeAssignmentData employeeAssignmentData = prepareEmployeeAssignmentData();
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(true);
		// when
		prospectGroupAssignmentService.updateEmployeeGroupAssignment(strategyId, employeeAssignmentData);
		// then
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any(ProspectApiRequest.class));
	}

	private List<BenefitGroupAssignmentRes> buildBenefitGroupAssignmentDetails() {
		return List.of(
				BenefitGroupAssignmentRes.builder().employeeId("E1").firstName("John").lastName("Doe").benefitGroupId(1)
						.build(),
				BenefitGroupAssignmentRes.builder().employeeId("E2").firstName("Smith").lastName("Doe")
						.benefitGroupId(1).build());
	}

	private EmployeeAssignmentData prepareEmployeeAssignmentData() {
		List<String> employees = List.of("E1", "E2");

		List<EmployeeSourceData> employeeSourceDataList = new ArrayList<>();
		EmployeeSourceData employeeSourceData = new EmployeeSourceData();
		employeeSourceData.setSourceStrategyGroupId(1111);
		employeeSourceData.setEmployees(employees);

		EmployeeAssignmentData employeeAssignmentData = new EmployeeAssignmentData();
		employeeAssignmentData.setDestinationStrategyGroupId(1112);
		employeeAssignmentData.setEmployeesList(employeeSourceDataList);

		return employeeAssignmentData;
	}

}