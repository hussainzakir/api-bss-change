package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.BSSStatusDetailsDaoImpl;
import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class BSSStatusDetailsDaoImplTest {

	BSSStatusDetailsDaoImpl bssStatusDetailsDaoImpl;
	EntityManager em = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		em = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		when(em.createNativeQuery(Mockito.anyString())).thenReturn(mockedQuery);
		bssStatusDetailsDaoImpl = new BSSStatusDetailsDaoImpl();
		bssStatusDetailsDaoImpl.setEm(em);
	}

	@Test
	public void getSubmitedStatus() {

		when(mockedQuery.getResultList()).thenReturn(prepareBSSStatusResult());
		BSSStatusDetailsDto actualResult = bssStatusDetailsDaoImpl.getSubmitedStatus("G48");

		assertEquals(true, actualResult.isBssStarted());
		assertEquals(false, actualResult.isBssSubmitted());
		
		when(mockedQuery.getResultList()).thenReturn(new ArrayList<Object[]>());
		actualResult = bssStatusDetailsDaoImpl.getSubmitedStatus("G48");

		assertEquals(false, actualResult.isBssStarted());
		assertEquals(false, actualResult.isBssSubmitted());

	}

	private List<Object[]> prepareBSSStatusResult() {
		List<Object[]> results = new ArrayList<>();

		Object[] r = new Object[4];
		r[0] = "true";
		r[1] = null;
		results.add(r);
		return results;
	}

}
