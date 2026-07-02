package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.impl.PlanMappingDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanYear;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class PlanMappingDaoImplTest {

	@InjectMocks
	PlanMappingDaoImpl planMappingDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	EntityManager em;

	@Mock
	EntityManager entityManager;

	private Company company = new Company();

	private Query mockedQuery = null;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(entityManager.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		company.setId(1L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearEnd(new Date());
		company.setCode("XYZ");
		company.setRealmPlanYear(realmPlanYear);
	}

	@Test
	public void getPlanMappingsTest() {

		Set<String> emptySet = new HashSet<>();
		when(mockedQuery.getResultList()).thenReturn(preparePlanMappingMockData());

		Map<String, PlanMapping> actualResult = planMappingDao.getPlanMappings(company, emptySet);

		assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get("OLDMED1").getNewBenefitPlans().size());
		assertEquals(true, actualResult.get("OLDMED1").getNewBenefitPlans().contains("NEWMED11"));
		assertEquals(true, actualResult.get("OLDMED1").getNewBenefitPlans().contains("NEWMED12"));
		assertEquals(true, actualResult.get("OLDMED1").getNewBenefitPlans().contains("NEWMED2"));
	}

	@Test
	public void getPrimaryPlanMappingsTest() {

		Set<String> emptySet = new HashSet<>();
		when(mockedQuery.getResultList()).thenReturn(preparePlanMappingMockData());

		Map<String, PlanMapping> actualResult = planMappingDao.getPrimaryPlanMappings(company, emptySet);

		assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get("OLDMED1").getNewBenefitPlans().size());
		assertEquals(true, actualResult.get("OLDMED1").getNewBenefitPlans().contains("NEWMED11"));
		assertEquals(true, actualResult.get("OLDMED1").getNewBenefitPlans().contains("NEWMED12"));
		assertEquals(true, actualResult.get("OLDMED1").getNewBenefitPlans().contains("NEWMED2"));
	}

	@Test
	public void getPlanMappingsAsSimpleMapTest() {

		Set<String> emptySet = new HashSet<>();
		when(mockedQuery.getResultList()).thenReturn(preparePlanMappingMockData());

		Map<String, List<String>> actualResult = planMappingDao.getPlanMappingsAsSimpleMap(company, emptySet);

		assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get("OLDMED1").size());
		assertEquals(true, actualResult.get("OLDMED1").contains("NEWMED11"));
		assertEquals(true, actualResult.get("OLDMED1").contains("NEWMED12"));
		assertEquals(true, actualResult.get("OLDMED1").contains("NEWMED2"));
	}

	private List<Object[]> preparePlanMappingMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[5];
		r[0] = "OLDMED1";
		r[1] = BigDecimal.ONE;
		r[2] = "NEWMED11";
		r[3] = BigDecimal.ONE;
		r[4] = "10";
		results.add(r);

		r = new Object[5];
		r[0] = "OLDMED1";
		r[1] = BigDecimal.ONE;
		r[2] = "NEWMED12";
		r[3] = BigDecimal.ONE;
		r[4] = "10";
		results.add(r);

		r = new Object[5];
		r[0] = "OLDMED1";
		r[1] = BigDecimal.ONE;
		r[2] = "NEWMED2";
		r[3] = BigDecimal.valueOf(2);
		r[4] = "10";
		results.add(r);

		r = new Object[5];
		r[0] = "OLDMED3";
		r[1] = BigDecimal.valueOf(3);
		r[2] = "NEWMED3";
		r[3] = BigDecimal.valueOf(3);
		r[4] = "10";
		results.add(r);

		return results;
	}

}