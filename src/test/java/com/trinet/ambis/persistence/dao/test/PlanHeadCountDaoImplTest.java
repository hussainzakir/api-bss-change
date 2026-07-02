package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.PlanHeadCountDaoImpl;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class PlanHeadCountDaoImplTest {

	PlanHeadCountDaoImpl planHeadCountDaoImpl;
	EntityManager em = null;
	EntityManager hrpEm = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		em = mock(EntityManager.class);
		hrpEm = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		planHeadCountDaoImpl = new PlanHeadCountDaoImpl();
		planHeadCountDaoImpl.setEm(em);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(hrpEm.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getLastYearPlansAndHeadCount() {
		String companyCode = "G48";
		long previousRealmPlanYearId = 10;
		Set<Long> submitStatus = new HashSet<Long>();
		submitStatus.add(1111L);

		when(mockedQuery.getResultList()).thenReturn(prepareAddlPlanSelectionsData());

		Map<String, Map<String, List<CoverageLevelHeadCount>>> actualResults = planHeadCountDaoImpl
				.getLastYearPlansAndHeadCount(companyCode, previousRealmPlanYearId, submitStatus);

		assertEquals(1, actualResults.size());
		assertEquals(2, actualResults.get("001RS3").size());
		assertEquals(2, actualResults.get("001RS3").get("002J2F").size());
		assertEquals(2, actualResults.get("001RS3").get("002R72").size());
		
		when(mockedQuery.getResultList()).thenReturn(null);

		actualResults = planHeadCountDaoImpl
				.getLastYearPlansAndHeadCount(companyCode, previousRealmPlanYearId, submitStatus);

		assertEquals(0, actualResults.size());
	}

	private List<Object[]> prepareAddlPlanSelectionsData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[6];
		r[0] = "36814";
		r[1] = "002J2F";
		r[2] = "C";
		r[3] = BigDecimal.valueOf(0);
		r[4] = "14";
		r[5] = "001RS3";
		results.add(r);
		r = new Object[6];
		r[0] = "36814";
		r[1] = "002J2F";
		r[2] = "4";
		r[3] = BigDecimal.valueOf(0);
		r[4] = "14";
		r[5] = "001RS3";
		results.add(r);
		r = new Object[6];
		r[0] = "36814";
		r[1] = "002R72";
		r[2] = "1";
		r[3] = BigDecimal.valueOf(0);
		r[4] = "14";
		r[5] = "001RS3";
		results.add(r);
		r = new Object[6];
		r[0] = "36814";
		r[1] = "002R72";
		r[2] = "2";
		r[3] = BigDecimal.valueOf(0);
		r[4] = "14";
		r[5] = "001RS3";
		results.add(r);
		return results;
	}

}