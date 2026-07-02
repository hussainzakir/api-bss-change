package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.trinet.ambis.persistence.dao.hrp.impl.FeatureFlagDaoImpl;
import com.trinet.ambis.service.model.FeatureFlag;

@RunWith(JUnit4.class)
public class FeatureFlagDaoImplTest {

	private FeatureFlagDaoImpl featureFlagDao = new FeatureFlagDaoImpl();
	private EntityManager mockedEm = null;
	private Query mockedQuery = null;

	@Before
	public void setup() {
		mockedEm = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		featureFlagDao.setEntityManager(mockedEm);
	}

	@Test
	public void retrieveFeatureFlags_noFlag() {
		when(mockedEm.createNamedQuery("FEATURE_ON_OFF_FLAG")).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(Arrays.asList());

		List<FeatureFlag> actualResult = featureFlagDao.retrieveFeatureFlags("G48", 68);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(0, actualResult.size());

	}

	@Test
	public void retrieveFeatureFlags_flagsAvailable() {
		when(mockedEm.createNamedQuery("FEATURE_ON_OFF_FLAG")).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(prepareData());

		List<FeatureFlag> actualResult = featureFlagDao.retrieveFeatureFlags("G48", 68);

		verify(mockedQuery, times(1)).getResultList();

		assertEquals(2, actualResult.size());

		assertEquals("FEATURE_1", actualResult.get(0).getKey());
		assertTrue(actualResult.get(0).isValue());

		assertEquals("FEATURE_2", actualResult.get(1).getKey());
		assertFalse(actualResult.get(1).isValue());

	}

	private List<Object[]> prepareData() {
		List<Object[]> results = new ArrayList<Object[]>();

		Object[] r = new Object[2];
		r[0] = "FEATURE_1";
		r[1] = BigDecimal.ONE;
		results.add(r);

		r = new Object[2];
		r[0] = "FEATURE_2";
		r[1] = BigDecimal.ZERO;
		results.add(r);

		return results;
	}
}