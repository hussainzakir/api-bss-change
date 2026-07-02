package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.impl.BenefitPlanDataDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class BenefitPlanDataDaoImplTest {
	BenefitPlanDataDaoImpl benPlanDataDaoImpl;
	EntityManager entityManager = null;
	Query mockedQuery = null;
	Company comp = null;
	BenefitGroup group = null;

	@Before
	public void setup() {
		comp = new Company();
		comp.setCode("G48");
		comp.setPlanStartDate("2018/10/02");
		group = new BenefitGroup();
		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		benPlanDataDaoImpl = new BenefitPlanDataDaoImpl();
		benPlanDataDaoImpl.setHrpEntityManager(entityManager);
		when(benPlanDataDaoImpl.getHrpEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
	}


	@Test
	public void getMedicalAutoSelectedPlansByRegion() {
		Set<String> plans = new HashSet<>();
		plans.add("00062S");
		long relamYearId = 10;

		List<Object[]> mockedResult = prepareMedicalAutoSelectedPlansByRegion();
		when(mockedQuery.getResultList()).thenReturn(mockedResult);

		Map<String, List<String>> actualResults = benPlanDataDaoImpl.getMedicalAutoSelectedPlansByRegion(plans,
				relamYearId);

		assertEquals(2, actualResults.size());
		assertEquals("002R4Q", actualResults.get("HI").get(0));
		assertEquals("002J1N", actualResults.get("HI").get(1));
		assertEquals("002R4R", actualResults.get("PR").get(0));
	}

	private List<Object[]> prepareMedicalAutoSelectedPlansByRegion() {
		List<Object[]> data = new ArrayList<>();

		Object[] r = new Object[2];
		r[0] = "002R4Q";
		r[1] = "HI";
		data.add(r);
		r = new Object[2];
		r[0] = "002J1N";
		r[1] = "HI";
		data.add(r);
		r = new Object[2];
		r[0] = "002R4R";
		r[1] = "PR";
		data.add(r);
		return data;
	}

}