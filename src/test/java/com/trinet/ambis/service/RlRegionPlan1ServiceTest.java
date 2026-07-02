package com.trinet.ambis.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.RlRegionPlan1Repository;
import com.trinet.ambis.persistence.projections.RlRegionPlan1View;
import com.trinet.ambis.service.impl.RlRegionPlan1ServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class RlRegionPlan1ServiceTest extends ServiceUnitTest {

	@InjectMocks
	private RlRegionPlan1ServiceImpl rlRegionPlan1Service;

	@Mock
	private RlRegionPlan1Repository rlRegionPlan1Repository;

	@Captor
	ArgumentCaptor<BigDecimal> realmPlanYearIdCaptor;

	/**
	 * given realm plan year id</br>
	 * when findByRealPlanYearId called </br>
	 * then return a map with plan as key and list of regions as value</br>
	 **/
	@Test
	public void findByRealPlanYearIdTest1() {
		// given
		// data
		long realPlanYearId = 64;
		// method mocks
		when(rlRegionPlan1Repository.findByRealPlanYearId(realmPlanYearIdCaptor.capture()))
				.thenReturn(buildRlRegionPlan1View());
		// when
		Map<String, List<String>> actualResult = rlRegionPlan1Service.findByRealmPlanYearId(realPlanYearId);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(2, actualResult.size());
		List<String> regions1 = actualResult.get("003GS2");
		assertNotNull(regions1);
		assertEquals(2, regions1.size());
		assertEquals("NY", regions1.get(0));
		assertEquals("IN", regions1.get(1));
		List<String> regions2 = actualResult.get("000EF7");
		assertNotNull(regions2);
		assertEquals(1, regions2.size());
		assertEquals("TX", regions2.get(0));
		// verify
		verify(rlRegionPlan1Repository, times(1)).findByRealPlanYearId(realmPlanYearIdCaptor.getValue());
	}

	/**
	 * given realm plan year id and no data present </br>
	 * when findByRealPlanYearId called </br>
	 * then return a empty map</br>
	 **/
	@Test
	public void findByRealPlanYearIdTest2() {
		// given
		// data
		long realPlanYearId = 64;
		// method mocks
		when(rlRegionPlan1Repository.findByRealPlanYearId(realmPlanYearIdCaptor.capture()))
				.thenReturn(Collections.emptyList());
		// when
		Map<String, List<String>> actualResult = rlRegionPlan1Service.findByRealmPlanYearId(realPlanYearId);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(0, actualResult.size());
		// verify
		verify(rlRegionPlan1Repository, times(1)).findByRealPlanYearId(realmPlanYearIdCaptor.getValue());
	}

	private List<RlRegionPlan1View> buildRlRegionPlan1View() {
		return List.of(RlRegionPlan1View.builder().benefitPlan("003GS2").region("NY").build(),
				RlRegionPlan1View.builder().benefitPlan("003GS2").region("IN").build(),
				RlRegionPlan1View.builder().benefitPlan("000EF7").region("TX").build());
	}

}
