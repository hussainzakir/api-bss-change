package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDataDao;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.service.impl.EmployeePlanAssignmentServiceImpl;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class EmployeePlanAssignmentServiceImplTest {

	@InjectMocks
	private EmployeePlanAssignmentServiceImpl employeePlanAssignmentService;

	@Mock
	private EePlanAssignmentDao employeePlanAssignmentDao;

	@Mock
	private EePlanAssignmentDataDao employeePlanAssignmentDataDao;
	
	@Test
	public void getEmployeePlanAssigmentBy() {
		List<Long> strategyIds = List.of(1111L);
		EePlanAssignment eePlanAssignment = EePlanAssignment.builder().build();
		List<EePlanAssignment> data = Arrays.asList(eePlanAssignment);
		
		when(employeePlanAssignmentDao.findByEePlanAssignmentPKStrategyIdIn(strategyIds)).thenReturn(data );
		
		List<EePlanAssignment> actualResult = employeePlanAssignmentService.getEmployeePlanAssigmentBy(strategyIds);
		
		verify(employeePlanAssignmentDao, times(1)).findByEePlanAssignmentPKStrategyIdIn(strategyIds);
		assertEquals(data, actualResult);
	}

	@Test
	public void deleteEmployeePlanAssignmentTest() {
		String companyCode = "a1b2c3";
		int partitionSize = BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE;

		List<String> retainedEmployeesData = getDummyEmployees(2000);
		// Create a bucket that holds all employees (adjust or split as you want)
		List<String> bucket = new ArrayList<>(retainedEmployeesData);
		Collection<List<String>> plans = Arrays.asList(bucket);

		try (MockedStatic<CommonUtils> mockedStatic = mockStatic(CommonUtils.class)) {
			// Use anyList(), eq() to match what the service calls
			mockedStatic.when(() -> CommonUtils.getBucketedList(new ArrayList<>(retainedEmployeesData), partitionSize))
					.thenReturn(plans);

			// when
			employeePlanAssignmentService.deleteEmployeePlanAssignment(retainedEmployeesData, companyCode);

			// then
			verify(employeePlanAssignmentDao, times(1)).deleteEePlanAssignment(bucket, companyCode);
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void deleteEmplDefaultPlanAssignmentTestEmpty() {
		String companyCode = "a1b2c3";
		List<String> retainedEmployeesData = Collections.emptyList();
		// Create a bucket that holds all employees (adjust or split as you want)

		Exception ex = assertThrows(IllegalArgumentException.class, () -> {
			employeePlanAssignmentService.deleteEmployeePlanAssignment(retainedEmployeesData, companyCode);
		});
		assertEquals("Employee IDs list cannot be null or contain only blank/empty values", ex.getMessage());
		employeePlanAssignmentService.deleteEmployeePlanAssignment(retainedEmployeesData, companyCode);
	}
	@Test
	public void deleteEmployeePlanAssignmentForBenTypesTest() {
		// given
		// data
		Set<String> retainedEmployeesData = Set.of("Emp1", "Emp2");
		Long strategyId = 1234L;
		Set<String> benTypes = Set.of("10");
		doNothing().when(employeePlanAssignmentDao).deleteEePlanAssignmentBy(retainedEmployeesData, strategyId, benTypes);
		// when
		employeePlanAssignmentService.deleteEmployeePlanAssignmentForBenTypes(retainedEmployeesData, strategyId, benTypes);
		// then
		// verify
		verify(employeePlanAssignmentDao, times(1)).deleteEePlanAssignmentBy(retainedEmployeesData, strategyId, benTypes);
	}

	@Test
	public void copyEePlanAssignmentsFor() {
		doNothing().when(employeePlanAssignmentDao).copyEePlanAssignmentsFor(1111L, 2222L);
		
		employeePlanAssignmentService.copyEePlanAssignmentsFor(1111L, 2222L);
		
		verify(employeePlanAssignmentDao, times(1)).copyEePlanAssignmentsFor(1111L, 2222L);
	}

	@Test
	public void saveEePlanAssignmentsTest() {
		List<EePlanAssignment> planAssignments = prepareEePlanAssignments();
		doNothing().when(employeePlanAssignmentDataDao).saveEmployeePlanAssignments(planAssignments);

		employeePlanAssignmentService.saveEePlanAssignments(planAssignments);

		verify(employeePlanAssignmentDataDao, times(1)).saveEmployeePlanAssignments(planAssignments);
	}

	private List<EePlanAssignment> prepareEePlanAssignments() {
		List<EePlanAssignment> eePlanAssignments = List.of(
				EePlanAssignment.builder()
						.eePlanAssignmentPK(EePlanAssignmentPK.builder()
								.strategyId(123456)
								.emplId("EMPLOYEE1")
								.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).build())
						.benefitPlan("MEDPLAN1")
						.covrgCD("2")
						.portfolioId(5)
						.eeRate(BigDecimal.ZERO)
						.erRate(BigDecimal.valueOf(500.00)).build(),
				EePlanAssignment.builder()
						.eePlanAssignmentPK(EePlanAssignmentPK.builder()
								.strategyId(123456)
								.emplId("EMPLOYEE2")
								.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).build())
						.benefitPlan("MEDPLAN1")
						.covrgCD("4")
						.portfolioId(5)
						.eeRate(BigDecimal.ZERO)
						.erRate(BigDecimal.valueOf(2000.00)).build()
		);
		return eePlanAssignments;
	}

	private List<String> getDummyEmployees(int count) {
		return IntStream.rangeClosed(1, count).mapToObj(i -> "Emp" + i).collect(Collectors.toList());
	}
}
