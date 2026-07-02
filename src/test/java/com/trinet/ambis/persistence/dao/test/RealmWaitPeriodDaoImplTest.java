package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import com.trinet.ambis.persistence.dao.hrp.impl.RealmWaitPeriodDaoImpl;
import com.trinet.ambis.service.model.WaitPeriod;
import com.trinet.ambis.service.unit.RealmWaitPeriodServiceImplTest;

/**
 * @author rvutukuri
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class RealmWaitPeriodDaoImplTest {

	RealmWaitPeriodDaoImpl realmWaitPeriodDao = new RealmWaitPeriodDaoImpl();
	EntityManager entityManager = null;
	Query mockedQuery = null;

	@Before
	public void setup() {

		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		realmWaitPeriodDao.setEntityManager(entityManager);

		when(mockedQuery.setParameter(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(mockedQuery);
		when(realmWaitPeriodDao.getEntityManager().createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getWaitPeriodsByRealmPlanYearTest1() {
		List<Object[]> wpl = createWaitPeriods();
		when(mockedQuery.getResultList()).thenReturn(wpl);
		List<WaitPeriod> wpl1 = realmWaitPeriodDao.getWaitPeriodsByRealmPlanYear(2);
		WaitPeriod wp = wpl1.get(0);
		assertEquals(wp.getId(), "FDOH");
		assertEquals(wp.getDescription(), "Testing");
	}

	@Test
	public void getWaitPeriodsByRealmPlanYearTest2() {
		when( mockedQuery.getResultList()).thenReturn( RealmWaitPeriodServiceImplTest.generateWaitPeriodData() );
		Map<String,String> actualResult = realmWaitPeriodDao.getWaitPeriodDescriptions();
		assertEquals( 7, actualResult.size() );
		assertEquals( "Other" ,actualResult.get( "OTHR" ));
	}


	public List<Object[]> createWaitPeriods() {
		List<Object[]> wpl = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "FDOH";
		r[1] = "Testing";
		wpl.add(r);
		return wpl;
	}
}