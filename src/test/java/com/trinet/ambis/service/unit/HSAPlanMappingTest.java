package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.service.impl.HSAPlanMapping;

/**
 * @author schaudhari
 *
 */
@RunWith(JUnit4.class)
public class HSAPlanMappingTest {

	HSAPlanMapping hsaPlanMapping;

	@Mock
	private static EntityManager bssEm;

	@Spy
	Query mockedQuery;

	@Mock
	EntityTransaction entityTrx;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(bssEm.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
	}

	public void HSAPlanMappingWhenEmIsNull() {
		bssEm = null;

		hsaPlanMapping = new HSAPlanMapping("G48");
	}

	@Test
	public void HSAPlanMapping() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "CLONEPLAN1";
		r[1] = "PLAN1";
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);

		hsaPlanMapping = new HSAPlanMapping("G48");
		hsaPlanMapping.setEntityManager(bssEm);

		String actual = hsaPlanMapping.get("CLONEPLAN1");

		assertEquals("PLAN1", actual);
	}

	@Test
	public void saveAll() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "CLONEPLAN1";
		r[1] = "PLAN1";
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);
		when(bssEm.getTransaction()).thenReturn(entityTrx);
		when(mockedQuery.executeUpdate()).thenReturn(1);

		hsaPlanMapping = new HSAPlanMapping("G48");
		hsaPlanMapping.setEntityManager(bssEm);

		int actual = hsaPlanMapping.saveAll();

		assertEquals(1, actual);
	}

	@Test(expected = BSSApplicationException.class)
	public void saveAllException() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "CLONEPLAN1";
		r[1] = "PLAN1";
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);
		when(bssEm.getTransaction()).thenReturn(entityTrx);
		when(mockedQuery.executeUpdate()).thenThrow(RuntimeException.class);

		hsaPlanMapping = new HSAPlanMapping("G48");
		hsaPlanMapping.setEntityManager(bssEm);

		int actual = hsaPlanMapping.saveAll();

		assertEquals(1, actual);
	}

}