package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.EmplDefaultPlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EmplDefaultPlanAssignmentDataDao;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignment;
import com.trinet.ambis.persistence.model.EmplDefaultPlanAssignmentId;
import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;
import com.trinet.ambis.service.impl.EmplDefaultPlanAssignmentServiceImpl;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class EmplDefaultPlanAssignmentServiceImplTest {

	@InjectMocks
	private EmplDefaultPlanAssignmentServiceImpl emplDefaultPlanAssignmentService;

	@Mock
	private EmplDefaultPlanAssignmentDao emplDefaultPlanAssignmentDao;

	@Mock
	private EmplDefaultPlanAssignmentDataDao emplDefaultPlanAssignmentDataDao;

	@Captor
	ArgumentCaptor<List<EmplDefaultPlanAssignment>> emplArgCaptor;

	@Test
	public void findAllByTest1() {
		// given
		// data
		long companyId = 1111L;
		int portfolioId = 1;
		List<EmplDefaultPlanAssignment> emplDefaultPlanAssignments = List.of(
				EmplDefaultPlanAssignment.builder()
						.emplDefaultPlanAssignmentId(EmplDefaultPlanAssignmentId.builder().companyId(1).emplId("Emp1")
								.planType("10")
						.benefitPlanId("MED1").build()).build(),
				EmplDefaultPlanAssignment.builder().emplDefaultPlanAssignmentId(
						EmplDefaultPlanAssignmentId.builder().companyId(1).emplId("Emp1").planType("11")
						.benefitPlanId("DEN1").build()).build());
		// method mocks
		when(emplDefaultPlanAssignmentDao.findBy(companyId, portfolioId)).thenReturn(emplDefaultPlanAssignments);
		// when
		Map<String, Map<String, String>> actualResult = emplDefaultPlanAssignmentService.findAllBy(companyId,
				portfolioId);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		assertEquals(2, actualResult.get("Emp1").size());
		// verify
		verify(emplDefaultPlanAssignmentDao, times(1)).findBy(companyId, portfolioId);
	}

	@Test
	public void saveAllTest1() {
		// given
		// data
		List<EmplDefaultPlanAssignmentDto> emplDefaultPlanAssignments = List.of(
				EmplDefaultPlanAssignmentDto.builder().companyId(1).emplId("Emp1").planType("10").build(),
				EmplDefaultPlanAssignmentDto.builder().companyId(1).emplId("Emp1").planType("11").build());
		List<EmplDefaultPlanAssignment> emplDefaultAssignments = List.of(
				EmplDefaultPlanAssignment.builder()
						.emplDefaultPlanAssignmentId(
								EmplDefaultPlanAssignmentId.builder().emplId("Emp1").planType("10").build())
						.build(),
				EmplDefaultPlanAssignment.builder().emplDefaultPlanAssignmentId(
						EmplDefaultPlanAssignmentId.builder().emplId("Emp1").planType("11").build()).build());
		// method mocks
		// when
		emplDefaultPlanAssignmentService.saveAll(emplDefaultPlanAssignments);
		// then
		// verify
		verify(emplDefaultPlanAssignmentDataDao, times(1)).saveEmplDefaultPlanAssignmentData(emplArgCaptor.capture());
	}

	@Test
	public void deleteEmplDefaultPlanAssignmentTest() {
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
			emplDefaultPlanAssignmentService.deleteEmplDefaultPlanAssignment(retainedEmployeesData, companyCode);

			// then
			verify(emplDefaultPlanAssignmentDao, times(1)).deleteEmplDefaultPlanAssignment(bucket, companyCode);
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void deleteEmplDefaultPlanAssignmentTestEmpty() {
		String companyCode = "a1b2c3";
		List<String> retainedEmployeesData = Collections.emptyList();
		// Create a bucket that holds all employees (adjust or split as you want)

		Exception ex = assertThrows(IllegalArgumentException.class, () -> {
			emplDefaultPlanAssignmentService.deleteEmplDefaultPlanAssignment(retainedEmployeesData, companyCode);
		});
		assertEquals("Employee IDs list cannot be null or contain only blank/empty values", ex.getMessage());
		emplDefaultPlanAssignmentService.deleteEmplDefaultPlanAssignment(retainedEmployeesData, companyCode);
	}

	private List<String> getDummyEmployees(int count) {
		return IntStream.rangeClosed(1, count).mapToObj(i -> "Emp" + i).collect(Collectors.toList());
	}
}