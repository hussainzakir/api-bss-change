package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.impl.EligConfigDaoImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class EligConfigDaoImplTest {

	EligConfigDaoImpl eligConfigDaoImpl;
	EntityManager entityManager = null;
	Query mockedQuery1 = null;
	Query mockedQuery2 = null;
	Query mockedQuery3 = null;

	@Before
	public void setup() {
		entityManager = mock(EntityManager.class);
		eligConfigDaoImpl = new EligConfigDaoImpl();
		mockedQuery1 = mock(Query.class);
		mockedQuery2 = mock(Query.class);
		mockedQuery3 = mock(Query.class);
	}

	@Test
	public void putEligConfigRow() {
		String pfClient = "G48";
		String effdtStr = "01-JAN-2018";
		String eligConfig1 = "eligConfig";
		String effStatus = "I";
		String descr = "desc";

		when(entityManager.createNamedQuery("GET_ELIGCNFG_COMPONENT")).thenReturn(mockedQuery1);
		when(entityManager.createNamedQuery("INSERT_ELIGCFG_EFDT")).thenReturn(mockedQuery2);
		when(entityManager.createNamedQuery("INSERT_ELIGCNFG_TBL")).thenReturn(mockedQuery3);

		when(mockedQuery1.getResultList()).thenReturn(prepareEligConfigData());
		when(mockedQuery2.executeUpdate()).thenReturn(1);
		when(mockedQuery3.executeUpdate()).thenReturn(1);

		int actualResult = eligConfigDaoImpl.putEligConfigRow(pfClient, effdtStr, eligConfig1, effStatus, descr,
				entityManager);

		assertEquals(2, actualResult);
		verify(mockedQuery1, times(1)).getResultList();
		verify(mockedQuery2, times(1)).executeUpdate();
		verify(mockedQuery3, times(1)).executeUpdate();
	}

	private List<Object[]> prepareEligConfigData() {
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] temp1 = new Object[5];
		temp1[0] = "G48";
		temp1[1] = new Date();
		temp1[2] = "eligConfig1";
		temp1[3] = "I";
		temp1[4] = "desc";
		list.add(temp1);
		return list;
	}

}