package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.impl.SearchCompanyDaoImpl;
import com.trinet.ambis.service.model.SearchCompanyResultData;

/**
 * @author schaudhari
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class SearchCompanyDaoImplTest {

	SearchCompanyDaoImpl searchCompanyDao = new SearchCompanyDaoImpl();
	EntityManager entityManager;
	Query mockedQuery = null;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		searchCompanyDao.setEntityManager(entityManager);
		when(mockedQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(mockedQuery);
		when(searchCompanyDao.getEntityManager().createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getQuarterAndClientTypeTest() {
		String empId = "123456";
		List<Object[]> queryResults = prepareQuarterAndClientType();
		when(mockedQuery.getResultList()).thenReturn(queryResults);
		Map<String, String> actualResult = searchCompanyDao.getQuarterAndClientType(empId);
		assertEquals(2, actualResult.size());
		assertEquals("Client Type 1", actualResult.get("QUARTER1"));
		assertEquals("Client Type 2", actualResult.get("QUARTER2"));
		
		when(mockedQuery.getResultList()).thenThrow(NoResultException.class);
		actualResult = searchCompanyDao.getQuarterAndClientType(empId);
		assertEquals(0, actualResult.size());
	}

	@Test
	public void getCompanyIdAndNameTest() {
		String inputText = "inputText";
		List<String> quarterList = new ArrayList<String>();
		quarterList.add("QUARTER1");
		String queryName = "query";
		List<Object[]> queryResults = prepareCompIdAndName();

		when(searchCompanyDao.getEntityManager().createNamedQuery(queryName)).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(queryResults);

		List<SearchCompanyResultData> actualResult = searchCompanyDao.getCompanyIdAndName(inputText, quarterList,
				queryName);

		assertEquals(2, actualResult.size());
		assertEquals("code1", actualResult.get(0).getCompanyCode());
		assertEquals("Company 1", actualResult.get(0).getCompanyName());
		assertEquals("code2", actualResult.get(1).getCompanyCode());
		assertEquals("Company 2", actualResult.get(1).getCompanyName());

		when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

		actualResult = searchCompanyDao.getCompanyIdAndName(inputText, quarterList, queryName);

		assertEquals(Collections.EMPTY_LIST, actualResult);

		when(mockedQuery.getResultList()).thenThrow(NoResultException.class);

		actualResult = searchCompanyDao.getCompanyIdAndName(inputText, quarterList, queryName);

		assertEquals(0, actualResult.size());
	}

	private List<Object[]> prepareCompIdAndName() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] obj1 = new Object[10];
		obj1[0] = "code1";
		obj1[1] = "Company 1";
		results.add(obj1);
		Object[] obj2 = new Object[10];
		obj2[0] = "code2";
		obj2[1] = "Company 2";
		results.add(obj2);
		return results;
	}

	private List<Object[]> prepareQuarterAndClientType() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] obj1 = new Object[10];
		obj1[0] = "QUARTER1";
		obj1[1] = "Client Type 1";
		results.add(obj1);
		Object[] obj2 = new Object[10];
		obj2[0] = "QUARTER2";
		obj2[1] = "Client Type 2";
		results.add(obj2);
		Object[] obj3 = new Object[10];
		obj3[0] = "QUARTER3";
		obj3[1] = "Client Type 3";
		results.add(obj2);
		return results;
	}

}
