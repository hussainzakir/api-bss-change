/**
 * 
 */
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.CompanyOptionsDaoImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class CompanyOptionsDaoImplTest {

	CompanyOptionsDaoImpl companyOptionsDaoImpl = new CompanyOptionsDaoImpl();
	EntityManager entityManager = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		companyOptionsDaoImpl.setEntityManager(entityManager);
		when(mockedQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockedQuery);
		when(companyOptionsDaoImpl.getEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);

	}

	@Test
	public void getPackageTypesTest() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] obj = new Object[2];
		obj[0] = "CON";
		obj[1] = "0";
		results.add(obj);
		when(mockedQuery.getResultList()).thenReturn(results);
		Map<String, Boolean> packageTypes = companyOptionsDaoImpl.getPackageTypes(4, "AG", "CA");
		assertEquals(packageTypes.get("CON"), false);
	}

	@Test
	public void getDefaultPortfoliosTest() {
		when(mockedQuery.getResultList()).thenReturn(prepareDefaultPortfoliosMockData());
		Map<String, Map<String, List<Long>>> portfolios = companyOptionsDaoImpl.getDefaultPortfolios(4, "AG", "CA", true);
		assertEquals(new Long(1), ((portfolios.get("CON")).get("1V")).get(0));
		assertEquals(new Long(7), ((portfolios.get("PRM")).get("A3")).get(0));
		assertEquals(new Long(7), ((portfolios.get("PRM")).get("A3")).get(1));
	}

	@Test
	public void getTemplateAdditionalPlansTest() {
		when(mockedQuery.getResultList()).thenReturn(prepareTemplateAddtnalPlansMockData());
		Map<String, Map<String, List<String>>> addtionalPlans = companyOptionsDaoImpl.getTemplateAdditionalPlans(4,
				"AG", "CA");
		assertEquals(3, addtionalPlans.size());
		assertEquals(2, addtionalPlans.get("CON").size());
		assertEquals(1, addtionalPlans.get("PRM").size());
		assertEquals(1, addtionalPlans.get("INT").size());
	}

	private List<Object[]> prepareDefaultPortfoliosMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] obj = new Object[3];
		obj[0] = "PRM";
		obj[1] = "A3";
		obj[2] = new BigDecimal(7);
		results.add(obj);
		obj = new Object[3];
		obj[0] = "PRM";
		obj[1] = "A3";
		obj[2] = new BigDecimal(7);
		results.add(obj);
		obj = new Object[3];
		obj[0] = "PRM";
		obj[1] = "10";
		obj[2] = new BigDecimal(9);
		results.add(obj);
		obj = new Object[3];
		obj[0] = "CON";
		obj[1] = "1V";
		obj[2] = new BigDecimal(1);
		results.add(obj);
		return results;
	}

	private List<Object[]> prepareTemplateAddtnalPlansMockData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] obj = new Object[3];
		obj[0] = "CON";
		obj[1] = "23";
		obj[2] = "000TM9";
		results.add(obj);
		obj = new Object[3];
		obj[0] = "CON";
		obj[1] = "30";
		obj[2] = "000SRS";
		results.add(obj);
		obj = new Object[3];
		obj[0] = "INT";
		obj[1] = "23";
		obj[2] = "000TM9";
		results.add(obj);
		results.add(obj);
		obj = new Object[3];
		obj[0] = "PRM";
		obj[1] = "23";
		obj[2] = "000SRO";
		results.add(obj);
		return results;
	}
}
