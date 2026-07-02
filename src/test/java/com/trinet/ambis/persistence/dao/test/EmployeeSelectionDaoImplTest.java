package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.EmployeeSelectionDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlan;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class EmployeeSelectionDaoImplTest {

	EmployeeSelectionDaoImpl empSelectionDaoImpl;
	EntityManager em = null;
	EntityManager hrpEm = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		em = mock(EntityManager.class);
		hrpEm = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		empSelectionDaoImpl = new EmployeeSelectionDaoImpl();
		empSelectionDaoImpl.setEntityManager(em);
		empSelectionDaoImpl.setHrpEntityManager(hrpEm);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(hrpEm.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	

	@Test
	public void getEmployeesByBG() {
		Company company = new Company();
		Date effDate = new Date();

		when(mockedQuery.getResultList()).thenReturn(prepareEmpByBenGrp());

		Map<String, Integer> actualResult = empSelectionDaoImpl.getEmployeesByBG(company, effDate);

		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get("EF1").intValue());
		assertEquals(56, actualResult.get("UPP").intValue());
	}

	

	@Test
	public void getRealmPlanYearBenefitPlans() {
		List<Long> planYears = new ArrayList<Long>();
		when(mockedQuery.getResultList()).thenReturn(prepareRealmPlanYearBenPlansMockData());

		Map<String, BenefitPlan> actualResult = empSelectionDaoImpl.getRealmPlanYearBenefitPlans(planYears);

		assertEquals(2, actualResult.size());
		assertEquals("30", actualResult.get("002A6C").getPlanType());
		assertEquals("31", actualResult.get("000TMF").getPlanType());
	}

	

	

	private List<Object[]> prepareEmpSelectionByComp() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[8];
		r[0] = "G48";
		r[1] = "UPP";
		r[2] = "00001415472";
		r[3] = 1;
		r[4] = 0;
		r[5] = "10";
		r[6] = "001EKY";
		r[7] = "4";
		results.add(r);
		r = new Object[8];
		r[0] = "G48";
		r[1] = "UPP";
		r[2] = "00001415472";
		r[3] = 1;
		r[4] = 0;
		r[5] = "11";
		r[6] = "000SR7";
		r[7] = "3";
		results.add(r);
		r = new Object[8];
		r[0] = "G48";
		r[1] = "UPP";
		r[2] = "00001415472";
		r[3] = 1;
		r[4] = 0;
		r[5] = "50";
		r[6] = "002J24";
		r[7] = "1";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareEmpByBenGrp() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "EF1";
		r[1] = BigDecimal.valueOf(2);
		results.add(r);
		r = new Object[2];
		r[0] = "UPP";
		r[1] = BigDecimal.valueOf(56);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareRealmPlanYearBenPlansMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[4];
		r[0] = "000TMF";
		r[1] = "31";
		r[2] = BigDecimal.valueOf(3);
		r[3] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[4];
		r[0] = "000TMF";
		r[1] = "31";
		r[2] = BigDecimal.valueOf(3);
		r[3] = BigDecimal.valueOf(0);
		results.add(r);
		r = new Object[4];
		r[0] = "002A6C";
		r[1] = "30";
		r[2] = BigDecimal.valueOf(1);
		r[3] = BigDecimal.valueOf(0);
		results.add(r);
		return results;
	}

}