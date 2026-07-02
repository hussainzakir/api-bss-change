/**
 * 
 */
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.persistence.dao.hrp.impl.CommonDataDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.SelectItem;

/**
 * @author rvutukuri
 *
 */
@RunWith(JUnit4.class)
@WebAppConfiguration
public class CommonDataDaoImplTest {

	@Autowired
	@InjectMocks
	CommonDataDaoImpl commonDataDaoImpl;

	@Mock
	EntityManager em;

	@Mock
	EntityManager entityManager;

	private Company company = new Company();

	private Query mockedQuery = null;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		when(entityManager.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
		company.setId(1L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearEnd(new Date());
		company.setCode("XYZ");
		company.setRealmPlanYear(realmPlanYear);
	}

	@Test
	public void getBsuppExcessOptions() {
		when(mockedQuery.getResultList()).thenReturn(prepareExcessOptionsData());
		List<SelectItem> actualResult = commonDataDaoImpl.getBsuppExcessOptions();
		assertEquals(2, actualResult.size());
	}

	@Test
	public void getBsuppVolPlanTypes() {
		when(mockedQuery.getResultList()).thenReturn(prepareBsuppVolPlanTypesData());
		List<SelectItem> actualResult = commonDataDaoImpl.getBsuppVolPlanTypes(2L);
		assertEquals(2, actualResult.size());
	}

	private List<Object[]> prepareExcessOptionsData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = new BigDecimal(1);
		r[1] = "C";
		results.add(r);
		r = new Object[10];
		r[0] = new BigDecimal(2);
		r[1] = "F";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareBsuppVolPlanTypesData() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[10];
		r[0] = "1D";
		r[1] = "Dental";
		results.add(r);
		r = new Object[10];
		r[0] = "1V";
		r[1] = "Vision";
		results.add(r);
		return results;
	}

}
