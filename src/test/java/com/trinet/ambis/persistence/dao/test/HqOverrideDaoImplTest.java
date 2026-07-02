package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
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

import com.trinet.ambis.persistence.dao.hrp.impl.HqOverrideDaoImpl;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class HqOverrideDaoImplTest {
	
	HqOverrideDaoImpl hrpDaoImpl;
	PsCompanyDao psCompanyDao;
	EntityManager em = null;
	Query mockedQuery = null;

	@Before
	public void setup() {
		em = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		psCompanyDao = mock(PsCompanyDao.class);
		when(em.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		when(em.createNativeQuery(Mockito.anyString())).thenReturn(mockedQuery);
		hrpDaoImpl = new HqOverrideDaoImpl();
		hrpDaoImpl.setPsCompanyDao(psCompanyDao);
		hrpDaoImpl.setEm(em);
	}
	
	@Test
	public void getHqPlanYearData() {
		String companyCode = "BSS-APP-1";

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[8];
		r[0] = "1001";
		r[1] = 1L;
		r[2] = "Q1";
		r[3] = new Date();
		r[4] =null;
		r[5] = "NI";
		r[6] = "12345-12";
		r[7] = "1";
		
		results.add(r);
		r = new Object[8];
		r[0] = "1001";
		r[1] = 1L;
		r[2] = "Q1";
		r[3] = new Date();
		r[4] = new Date();
		r[5] = "NI";
		r[6] = "12345-12";
		r[7] = "0";
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);

		List<CompanyHQData> actual = hrpDaoImpl.getHqPlanYearData(companyCode);

		assertEquals(2, actual.size());
		assertEquals(false, actual.get(1).isHasStrategies());
		assertEquals(true, actual.get(0).isHasStrategies());

 
	}
	
	@Test
	public void getOverridesPlanYears() {
		String companyCode = "BSS-APP-1";

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[8];
		r[0] = "1001";
		r[1] = "test";
		r[2] = "Q1";
		 
		results.add(r);
		r = new Object[8];
		r[0] = "1001";
		r[1] = "test";
		r[2] = "Q1";
		 
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);

		List<Map<String, Object>> actual = hrpDaoImpl.getOverridesPlanYears(companyCode);

		assertEquals(2, actual.size());
		assertEquals(results.get(0)[1], actual.get(0).get("planYearStart"));
		assertEquals(results.get(0)[2], actual.get(0).get("planYearEnd"));
		assertEquals(results.get(0)[0], actual.get(0).get("realmYearId"));
 
	}
	
	@Test
	public void getHqOverridesDetails() {
		String companyCode = "BSS-APP-1";

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[16];
		r[0] = "1001";
		r[1] = 1L;
		r[2] =  new Date();
		r[3] = "1";
		r[4] ="1";
		r[5] = new Date();
		r[6] =  new Date();
		r[7] = "1";
		r[8] = null;
		r[9] = null;
		r[10] = null;
		r[11] = null;
		r[12] = null;
		r[13] = null;
		r[14] = null;
		r[15] = null;
		
		results.add(r);
		r = new Object[16];
		r[0] = "1001";
		r[1] = 1L;
		r[2] =  new Date();
		r[3] = "1";
		r[4] ="1";
		r[5] = new Date();
		r[6] =  new Date();
		r[7] = "0";
		r[8] = null;
		r[9] = null;
		r[10] = null;
		r[11] = null;
		r[12] = null;
		r[13] = null;
		r[14] = null;
		r[15] = null;
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);

		List<HqOverridesDto> actual = hrpDaoImpl.getHqOverridesDetails(companyCode, "Q2");

		assertEquals(2, actual.size());
		assertEquals(false, actual.get(1).isHasStrategies());
 
	}

}
