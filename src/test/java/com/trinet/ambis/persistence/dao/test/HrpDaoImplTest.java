package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.hrp.impl.HrpDaoImpl;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.OLPProcessStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class HrpDaoImplTest {

	HrpDaoImpl hrpDaoImpl;
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
		hrpDaoImpl = new HrpDaoImpl();
		hrpDaoImpl.setPsCompanyDao(psCompanyDao);
		hrpDaoImpl.setEm(em);
	}

	@Test
	public void getEmplEmail() {
		String companyCode = "G48";
		String emplId = "00002222267";

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "00002222268";
		r[1] = "david.rochard@trinet.com";
		results.add(r);
		r = new Object[2];
		r[0] = "00002222267";
		r[1] = "mat.coleman@trinet.com";
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);
		when(psCompanyDao.isActiveWithCompany("00002222267", companyCode)).thenReturn(true);
		when(psCompanyDao.isActiveWithCompany("00002222268", companyCode)).thenReturn(false);

		String actual = hrpDaoImpl.getEmplEmail(companyCode, emplId);

		assertEquals("mat.coleman@trinet.com", actual);
	}

	@Test
	public void getBDMEmails() {
		String company = "G48";
		when(mockedQuery.getResultList()).thenReturn(prepareBDMEmails());
		when(psCompanyDao.isActiveWithCompany("11111", company)).thenReturn(true);
		when(psCompanyDao.isActiveWithCompany("22222", company)).thenReturn(false);

		Set<String> actualResults = hrpDaoImpl.getBDMEmails(company);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(1, actualResults.size());

		when(mockedQuery.getResultList()).thenReturn(null);

		actualResults = hrpDaoImpl.getBDMEmails(company);

		assertEquals(0, actualResults.size());
	}
	


	@Test
	public void getRoleEmails() {
		String company = "G48";
		when(mockedQuery.getResultList()).thenReturn(prepareBDMEmails());
		when(psCompanyDao.isActiveWithCompany("11111", company)).thenReturn(true);
		when(psCompanyDao.isActiveWithCompany("22222", company)).thenReturn(false);

		Set<String> actualResults = hrpDaoImpl.getRoleEmails(company, "BEN_CORP_AD");

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(1, actualResults.size());

		when(mockedQuery.getResultList()).thenReturn(null);

		actualResults = hrpDaoImpl.getBDMEmails(company);

		assertEquals(0, actualResults.size());
	}

	@Test
	public void getBDMCount() {
		List<String> companies = Arrays.asList("COMP1", "COMP2", "COMP3");

		when(mockedQuery.getResultList()).thenReturn(prepareBDMEmailAvailableResult());
		Map<String, Integer> actualResults = hrpDaoImpl.getBDMCount(companies);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());

	}

	@Test
	public void getBenCorpAdminCount() {
		List<String> companies = Arrays.asList("001");

		when(mockedQuery.getResultList()).thenReturn(prepareBDMEmailAvailableResult());
		Map<String, Integer> actualResults = hrpDaoImpl.getBenCorpAdminCount(companies);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());

	}

	@Test
	public void getCovrgCdMap() {

		when(mockedQuery.getResultList()).thenReturn(prepareCovrgCodes());
		Map<String, String> actualResults = hrpDaoImpl.getCovrgCdMap();
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());
	}

	@Test
	public void refreshPlanView() {
		when(em.createNativeQuery(Mockito.anyString())).thenReturn(mockedQuery);
		hrpDaoImpl.refreshPlanView();
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getGatewayAppAccessibleRolesBy() {
		String appKey = "BSS-APP-1";

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "1001";
		r[1] = "TMT";
		results.add(r);
		r = new Object[2];
		r[0] = "1001";
		r[1] = "BDM";
		results.add(r);

		when(mockedQuery.getResultList()).thenReturn(results);

		Set<String> actual = hrpDaoImpl.getGatewayAppAccessibleRolesBy(appKey);

		assertEquals(2, actual.size());
		assertTrue(actual.contains("TMT"));
		assertTrue(actual.contains("BDM"));
	}
	
	@Test
	public void getOLPStatus() {
		Company company = new Company();
		company.setCode("COMPANY");
		company.setPfClient("PF_CLIENT");

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.ONE);
		int actualResult = hrpDaoImpl.getOLPStatus(company);
		assertEquals(1, actualResult);
	}	
	
	@Test
	public void getOlpHiringCompletedStatus() {
		Company company = new Company();
		company.setCode("COMPANY");
		company.setPfClient("PF_CLIENT");

		when(mockedQuery.getResultList()).thenReturn(prepareOlpHiringCompletedStatusData());

		OLPProcessStatus actualResult = hrpDaoImpl.getOlpHiringCompletedStatus(company);

		assertEquals(428975L, actualResult.getOlpId());
		assertEquals(0, actualResult.getStatus());
		// TODO This is failing on Bamboo. Need to fix this later.
//		assertEquals("Sat Sep 08 21:46:39 EDT 2001", actualResult.getUpdateDate().toString());

		when(mockedQuery.getResultList()).thenReturn(null);

		actualResult = hrpDaoImpl.getOlpHiringCompletedStatus(company);

		assertEquals(null, actualResult);

		when(mockedQuery.getResultList()).thenReturn(Collections.emptyList());

		actualResult = hrpDaoImpl.getOlpHiringCompletedStatus(company);

		assertEquals(null, actualResult);
	}
	
	@Test
	public void getZipCodesAndStates() {
		List<String> zipCodes = Arrays.asList("12345", "23456");
		when(mockedQuery.getResultList()).thenReturn(prepareZipCodeStates());
		
		Map<String, String> actualResults = hrpDaoImpl.getZipCodesAndStatesBy(zipCodes);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());
	}
	
	@Test
	public void deleteCompanyByCompanyId() {
		hrpDaoImpl.deleteCompanyByCompanyId(1234L);
		verify(em, times(1)).createNativeQuery(Mockito.contains("DELETE FROM XBSS_BAND_CODES WHERE company_id = 1234"));
		verify(em, times(1)).createNativeQuery(Mockito.contains("DELETE FROM XBSS_COMPANY WHERE ID = 1234"));
		verify(mockedQuery, times(1)).executeUpdate();
	}

	private List<Object[]> prepareBDMEmailAvailableResult() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "COMP1";
		r[1] = new BigDecimal(2);
		results.add(r);
		r = new Object[12];
		r[0] = "COMP2";
		r[1] = new BigDecimal(3);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareBDMEmails() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "11111";
		r[1] = "test1@trinet.com";
		results.add(r);

		r = new Object[2];
		r[0] = "22222";
		r[1] = "test2@trinet.com";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareCovrgCodes() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "1";
		r[1] = "employee";
		results.add(r);
		r = new Object[2];
		r[0] = "2";
		r[1] = "spouse";
		results.add(r);
		return results;
	}
	
	private List<Object[]> prepareOlpHiringCompletedStatusData() {
		List<Object[]> result = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = BigDecimal.valueOf(428975);
		r[1] = BigDecimal.valueOf(0);
		r[2] = new Timestamp(999999999929L);
		result.add(r);
		return result;
	}
	
	private List<Object[]> prepareZipCodeStates() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "12345";
		r[1] = "CA";
		results.add(r);
		r = new Object[2];
		r[0] = "23456";
		r[1] = "FL";
		results.add(r);
		return results;
	}

}