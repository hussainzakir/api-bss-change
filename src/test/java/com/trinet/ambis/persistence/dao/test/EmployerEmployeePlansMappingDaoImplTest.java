package com.trinet.ambis.persistence.dao.test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.EmployerEmployeePlansMappingDaoImpl;
import com.trinet.ambis.service.model.BenefitPlan;
/**
 * @author mpulipaka
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class EmployerEmployeePlansMappingDaoImplTest {
	
	EmployerEmployeePlansMappingDaoImpl employerEmployeePlansMappingDao = new EmployerEmployeePlansMappingDaoImpl();
	EntityManager entityManager = null;
	Query mockedQuery = null;
	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		employerEmployeePlansMappingDao.setEntityManager(entityManager);
//		when(mockedQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.anyObject())).thenReturn(mockedQuery);
		when(employerEmployeePlansMappingDao.getEntityManager().createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}
	
	@Test
	public void getEmployerEmployeePlansMappingByRealmYearIdOfferedCountTest() {
	    Object result = getEmployerEmployeePlansMappingByRealmYearIdOfferedCount();
		when(mockedQuery.getSingleResult()).thenReturn(result);
		long realmYearId = 4;
		int employerEmployeePlansMappingByRealmYearIdOfferedCount = 0;
		employerEmployeePlansMappingByRealmYearIdOfferedCount = employerEmployeePlansMappingDao.getEmployerEmployeePlansMappingByRealmYearIdOfferedCount(realmYearId);
		System.out.println(employerEmployeePlansMappingByRealmYearIdOfferedCount);
		Assert.assertTrue(employerEmployeePlansMappingByRealmYearIdOfferedCount>0);
   }
	
	@Test
	public void getEmployerEmployeePlansMappingByRealmYearIdTest() {
		List<Object[]> results = getEmployerEmployeePlansMappingByRealmYearIdMock();
		when(mockedQuery.getResultList()).thenReturn(results);
		long realmPlanYearId = 4;
		Map<BenefitPlan, BenefitPlan> employerPaidMap  = new HashMap<BenefitPlan, BenefitPlan>();
		employerPaidMap = employerEmployeePlansMappingDao.getEmployerEmployeePlansMappingByRealmYearId(realmPlanYearId);
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setId("002ACM");
		assertEquals(true, employerPaidMap.containsKey(benefitPlan));
		assertEquals("002ACQ", employerPaidMap.get(benefitPlan).getId());
	}
	
	@Test
	public void getEeAndErPlanMapping() {
		when(mockedQuery.getResultList()).thenReturn(prepareEeErPlanMappingMockData());
		
		Map<String, String> actualResult = employerEmployeePlansMappingDao.getEeAndErPlanMapping(10);
		
		assertEquals(3, actualResult.size());
		assertEquals("000TFY", actualResult.get("000SR6"));
		assertEquals("000SR7", actualResult.get("000TFY"));
		assertEquals("000TFY", actualResult.get("000SR7"));
	}

	private List<Object[]> getEmployerEmployeePlansMappingByRealmYearIdMock() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] result = new Object[4];
		result[0] = BigDecimal.valueOf(14);
		result[1] = "002ACM";
		result[2] = "1V";
		result[3] = "002ACQ";
	    results.add(result);
	    Object[] result1 = new Object[4];
	    result1[0] =BigDecimal.valueOf(14);
		result1[1] = "002ACN";
		result1[2] = "1V";
		result1[3] = "002ACR";
	    results.add(result1);
		return results;
	}

	private Object getEmployerEmployeePlansMappingByRealmYearIdOfferedCount() {
		Object result = new Object();
		result = BigDecimal.valueOf(9);
		return result;
	}

	private List<Object[]> prepareEeErPlanMappingMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] result = new Object[4];
		result[0] = "11";
		result[1] = "000SR6";
		result[2] = "1D";
		result[3] = "000TFY";
	    results.add(result);
	    result = new Object[4];
		result[0] = "11";
		result[1] = "000SR7";
		result[2] = "1D";
		result[3] = "000TFY";
	    results.add(result);
	    return results;
	}
	
}
