package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.trinet.ambis.persistence.dao.hrp.impl.PersonDaoImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class PersonDaoImplTest {
	PersonDaoImpl personDaoImpl;
	EntityManager em = null;
	Query mockedQuery = null;

	private final static String USER_ID = "000222278";
	private final static String COMP_CODE = "C2E";
	private final static String CS_AUTH_EMAIL = "csauthmail@client.com";
	private final static String COMPANY_HRM_EMAIL = "comphrmmail@client.com";

	@Before
	public void setup() {
		em = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		personDaoImpl = new PersonDaoImpl();
		personDaoImpl.setEntityManager(em);
		when(personDaoImpl.getEntityManager().createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getFirstandLastName() {
		when(mockedQuery.getResultList()).thenReturn(prepareNameData());
		String actualResult = personDaoImpl.getFirstandLastName(USER_ID);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals("firstname lastname", actualResult);
		when(mockedQuery.getResultList()).thenReturn(Collections.EMPTY_LIST);
		actualResult = personDaoImpl.getFirstandLastName(USER_ID);
		assertEquals(null, actualResult);
	}

	@Test
	public void getFirstandLastName_multipleEntry() {
		when(mockedQuery.getResultList()).thenReturn(prepareNameDataWithMultipleEntry());
		String actualResult = personDaoImpl.getFirstandLastName(USER_ID);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(null, actualResult);
	}

	@Test
	public void getFirstandLastName_lengthLessThan2() {
		List<Object[]> result = new ArrayList<Object[]>();
		Object[] r = new Object[1];
		r[0] = "firstname";
		result.add(r);
		when(mockedQuery.getResultList()).thenReturn(result);
		String actualResult = personDaoImpl.getFirstandLastName(USER_ID);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(null, actualResult);
	}

	@Test
	public void getFirstandLastName_exception() {
		when(mockedQuery.getResultList()).thenThrow(new NoResultException());
		String actualResult = personDaoImpl.getFirstandLastName(USER_ID);
		assertEquals(null, actualResult);
	}

	@Test
	public void testGetCsAuthEmail() {
		when(mockedQuery.getResultList()).thenReturn(List.of(CS_AUTH_EMAIL));
		String actualResult = personDaoImpl.getCSAuthEmail(USER_ID);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(CS_AUTH_EMAIL, actualResult);
	}

	@Test
	public void testGetCsAuthEmail_empty() {
		when(mockedQuery.getResultList()).thenReturn(Collections.EMPTY_LIST);
		String actualResult = personDaoImpl.getCSAuthEmail(USER_ID);
		assertNull(actualResult);
	}

	@Test
	public void testGetCsAuthEmail_multipleMail() {
		when(mockedQuery.getResultList()).thenReturn(List.of(CS_AUTH_EMAIL, "secondary" + CS_AUTH_EMAIL));
		String actualResult = personDaoImpl.getCSAuthEmail(USER_ID);
		assertNull(actualResult);
	}

	@Test
	public void testGetCsAuthEmail_exception() {
		when(mockedQuery.getResultList()).thenThrow(new NoResultException());
		String actualResult = personDaoImpl.getCSAuthEmail(USER_ID);
		assertNull(actualResult);
	}

	@Test
	public void testGetCompanyHrmEmail() {
		when(mockedQuery.getResultList()).thenReturn(List.of(COMPANY_HRM_EMAIL));
		String actualResult = personDaoImpl.getCompanyHrmEmail(COMP_CODE);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(COMPANY_HRM_EMAIL, actualResult);
	}

	@Test
	public void testGetCompanyHrmEmail_empty() {
		when(mockedQuery.getResultList()).thenReturn(Collections.EMPTY_LIST);
		String actualResult = personDaoImpl.getCompanyHrmEmail(COMP_CODE);
		assertNull(actualResult);
	}

	@Test
	public void testGetCompanyHrmEmail_multipleMail() {
		when(mockedQuery.getResultList()).thenReturn(List.of(COMPANY_HRM_EMAIL, "secondary" + COMPANY_HRM_EMAIL));
		String actualResult = personDaoImpl.getCompanyHrmEmail(COMP_CODE);
		assertNull(actualResult);
	}

	@Test
	public void testGetCompanyHrmEmail_Exception() {
		when(mockedQuery.getResultList()).thenThrow(new NoResultException());
		String actualResult = personDaoImpl.getCompanyHrmEmail(COMP_CODE);
		assertNull(actualResult);
	}

	private List<Object[]> prepareNameData() {
		List<Object[]> result = new ArrayList<Object[]>();

		Object[] r = new Object[2];
		r[0] = "firstname";
		r[1] = "lastname";
		result.add(r);
		return result;
	}

	private List<Object[]> prepareNameDataWithMultipleEntry() {
		List<Object[]> result = new ArrayList<Object[]>();

		Object[] r = new Object[2];
		r[0] = "firstname1";
		r[1] = "lastname1";
		result.add(r);
		r = new Object[2];
		r[0] = "firstname2";
		r[1] = "lastname2";
		result.add(r);
		return result;
	}
}