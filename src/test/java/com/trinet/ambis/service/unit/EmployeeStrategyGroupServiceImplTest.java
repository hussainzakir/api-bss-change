package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployeeStrategyGroupDao;
import com.trinet.ambis.service.impl.EmployeeStrategyGroupServiceImpl;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeStrategyGroupServiceImplTest {

	@InjectMocks
	private EmployeeStrategyGroupServiceImpl employeeStrategyGroupService;

	@Mock
	private EmployeeStrategyGroupDao employeeStrategyGroupDao;

	@Test
	public void deleteEmployeeStrategyGroupsTest() {
		String companyCode = "a1b2c3";
		int partitionSize = BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE;

		List<String> retainedEmployeesData = getDummyEmployees(1500);
		// Create a bucket that holds all employees (adjust or split as you want)
		List<String> bucket = new ArrayList<>(retainedEmployeesData);
		Collection<List<String>> plans = Arrays.asList(bucket);

		try (MockedStatic<CommonUtils> mockedStatic = mockStatic(CommonUtils.class)) {
			// Use anyList(), eq() to match what the service calls
			mockedStatic.when(() -> CommonUtils.getBucketedList(new ArrayList<>(retainedEmployeesData), partitionSize))
					.thenReturn(plans);

			// when
			employeeStrategyGroupService.deleteEmployeeStrategyGroups(retainedEmployeesData, companyCode);

			// then
			verify(employeeStrategyGroupDao, times(1)).deleteEmployeeStrategyGroups(bucket, companyCode);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void deleteEmplDefaultPlanAssignmentTestEmpty() {
		String companyCode = "a1b2c3";
		List<String> retainedEmployeesData = Collections.emptyList();
		// Create a bucket that holds all employees (adjust or split as you want)

		Exception ex = assertThrows(IllegalArgumentException.class, () -> {
			employeeStrategyGroupService.deleteEmployeeStrategyGroups(retainedEmployeesData, companyCode);
		});
		assertEquals("Employee IDs list cannot be null or contain only blank/empty values", ex.getMessage());
		employeeStrategyGroupService.deleteEmployeeStrategyGroups(retainedEmployeesData, companyCode);
	}

	@Test
	public void updateEmployeesToDefaultStrategyGroupTest() {
		List<Long> k1StrategyGroupIds = List.of(101L, 102L, 103L);
		Long defaultStrategyGroupId = 999L;
		doNothing().when(employeeStrategyGroupDao).updateEmployeesToDefaultStrategyGroup(k1StrategyGroupIds, defaultStrategyGroupId);

		employeeStrategyGroupService.updateEmployeesToDefaultStrategyGroup(k1StrategyGroupIds, defaultStrategyGroupId);

		verify(employeeStrategyGroupDao, times(1)).updateEmployeesToDefaultStrategyGroup(k1StrategyGroupIds, defaultStrategyGroupId);
	}

	private List<String> getDummyEmployees(int count) {
		return IntStream.rangeClosed(1, count).mapToObj(i -> "Emp" + i).collect(Collectors.toList());
	}
}
