package com.trinet.ambis.service.unit;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.BSSApplicationConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.DefaultPlanDataDao;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.ProspectDefaultPlanAssignmentServiceImpl;

/**
 * @author schaudhari
 *
 */
@RunWith(JUnit4.class)
public class ProspectDefaultPlanAssignmentServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	ProspectDefaultPlanAssignmentServiceImpl service;

	@Mock
	ProspectCensusService prospectCensusService;
	@Mock
	DefaultPlanDataDao defaultPlanDataDao;
	@Mock
	EePlanAssignmentDao eePlanAssignmentDao;
	@Mock
	StrategyService strategyService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void insertStrategyDefaultAssignments_test() {
		Company company = new Company();

		service.insertStrategyDefaultAssignments(company, Set.of(1L),Set.of(1L), 11111);

		verify(defaultPlanDataDao, times(1)).insertStrategyDefaultAssignmentsBy(company, Set.of(1L),Set.of(1L), 11111);
		verify(eePlanAssignmentDao, times(1)).deleteEePlanAssignmentBy(Set.of(11111L));
	}

	@Test
	public void assignDefaultPlanBy_test() {
		Company company = new Company();
		company.setCode("G48");

		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(1L, true);
		existingMedStrategyPortfolioMap.put(2L, false);
		existingMedStrategyPortfolioMap.put(4L, false);
		existingMedStrategyPortfolioMap.put(6L, false);

		service.assignDefaultPlanBy(Set.of("EMPL1", "EMPL2"), 1111L,
				existingMedStrategyPortfolioMap,
				BSSApplicationConstants.PRIMARY_PLAN_TYPES.stream().collect(Collectors.toSet()));

		verify(eePlanAssignmentDao, times(1)).deleteEePlanAssignmentBy(Set.of("EMPL1", "EMPL2"), 1111L,
				BSSApplicationConstants.PRIMARY_PLAN_TYPES.stream().collect(Collectors.toSet()));
		verify(defaultPlanDataDao, times(1)).insertStrategyDefaultAssignmentsBy(Set.of("EMPL1", "EMPL2"), 1111L,
				List.of(1L), List.of(2L, 4L, 6L), Set.of("10", "11", "14", "1D", "1V"));
	}

	@Test
	public void assignDefaultPlanBy_noEmployeesTest() {
		Company company = new Company();
		company.setCode("G48");
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();

		when(strategyService.findBy("G48")).thenReturn(Arrays.asList());

		service.assignDefaultPlanBy(Set.of(), 1111L, existingMedStrategyPortfolioMap,
				BSSApplicationConstants.PRIMARY_PLAN_TYPES.stream().collect(Collectors.toSet()));

		verify(eePlanAssignmentDao, times(0)).deleteEePlanAssignmentBy(anyString());
		verify(defaultPlanDataDao, times(0)).insertStrategyDefaultAssignmentsBy(anySet(), anyLong(), anyList(),
				anyList(), anySet());
	}
	
	@Test
	public void assignDefaultPlanBy() {
		Set<Long> strategyIds = Set.of(1111L);
		Set<Long> groupIds = Set.of(2222L);
		Map<Long, Boolean> existingMedStrategyPortfolioMap = new HashMap<>();
		existingMedStrategyPortfolioMap.put(1L, true);
		existingMedStrategyPortfolioMap.put(2L, false);
		existingMedStrategyPortfolioMap.put(4L, false);
		existingMedStrategyPortfolioMap.put(6L, false);
		Set<String> benTypes = Set.of("10");

		service.assignDefaultPlanBy(strategyIds, groupIds, existingMedStrategyPortfolioMap, benTypes);

		verify(eePlanAssignmentDao, times(1)).deleteEePlanAssignmentBy(strategyIds, groupIds, benTypes);
		verify(defaultPlanDataDao, times(1)).insertStrategyDefaultAssignmentsBy(strategyIds, groupIds, List.of(1L),
				List.of(2L, 4L, 6L), benTypes);
	}

}
