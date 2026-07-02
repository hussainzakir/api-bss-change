package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.ps.impl.PsCompanyDaoImpl;
import com.trinet.ambis.persistence.dao.ps.impl.PsDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.util.Constants;

@RunWith(JUnit4.class)
@WebAppConfiguration
public class PsDaoImplTest {

	/*
	 * @Rule public PowerMockRule rule = new PowerMockRule();
	 */

	@InjectMocks
	PsDaoImpl psDaoImpl;

	@Mock
	EntityManager em;

	@Mock
	PsCompanyDaoImpl psCompanyDao;

	private Query mockedQuery = null;

	private static final String COMPANY_CODE = "TEST";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockedQuery = mock(Query.class);
		when(em.createNamedQuery(ArgumentMatchers.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getEmployeeFirstName() {
		String userId = "0002222238";
		when(mockedQuery.getResultList()).thenReturn(prepareUserName("Joshua "));

		String results = psDaoImpl.getEmployeeFirstName(userId);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals("Joshua", results);
	}

	@Test
	public void getEmployeeLastName() {
		String userId = "0002222238";
		when(mockedQuery.getResultList()).thenReturn(prepareUserName("Clark "));

		String results = psDaoImpl.getEmployeeLastName(userId);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals("Clark", results);
	}
	
	@Test
	public void getEmployeeLastName_noEmplIdPresent() {
		String userId = "0002222238";
		when(mockedQuery.getResultList()).thenReturn(prepareUserName(null));

		String results = psDaoImpl.getEmployeeLastName(userId);

		verify(mockedQuery, times(1)).getResultList();
		assertEquals("", results);
	}

	@Test
	public void getAssignmentAddresses() {

		Company company = prepareCompany();
		when(mockedQuery.getResultList()).thenReturn(prepareClientAssignmentEmails());
		when(psCompanyDao.isCSAUser("00001447262", company.getRealm().getId())).thenReturn(false);
		when(psCompanyDao.isCSAUser("00001447261", company.getRealm().getId())).thenReturn(true);

		// Results
		List<String> actualResult = psDaoImpl.getAssignmentAddresses(company);
		assertEquals(1, actualResult.size());
		assertEquals("Testing1@trinet.com", actualResult.get(0));
		
		// Empty Results
		when(mockedQuery.getResultList()).thenReturn(new ArrayList<>());
		actualResult = psDaoImpl.getAssignmentAddresses(company);
		assertEquals(true, actualResult.isEmpty());

		verify(mockedQuery, times(2)).getResultList();
	}

	@Test
	public void getUnsubmittedClients() {
		String quarter = BSSApplicationConstants.SM_QUARTER;
		Long realmYearId = 10L;
		Date payrollCutOffDate = new Date();
		List<String> mockData = new ArrayList<String>();
		mockData.add("G48");
		mockData.add("S7B");

		when(mockedQuery.getResultList()).thenReturn(mockData);

		List<String> actualResults = psDaoImpl.getUnsubmittedClients(quarter, realmYearId, payrollCutOffDate, "ACTIVE");

		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());
		assertEquals("G48", actualResults.get(0));
		assertEquals("S7B", actualResults.get(1));

		quarter = BSSApplicationConstants.Q3_QUARTER;

		actualResults = psDaoImpl.getUnsubmittedClients(quarter, realmYearId, payrollCutOffDate, "ACTIVE");

		verify(mockedQuery, times(2)).getResultList();
		assertEquals(2, actualResults.size());
		assertEquals("G48", actualResults.get(0));
		assertEquals("S7B", actualResults.get(1));
	}

	@Test
	public void getPreLoadClients() {
		List<String> mockData = new ArrayList<String>();
		mockData.add("CLIENT 1");
		mockData.add("CLIENT 2");

		when(mockedQuery.getResultList()).thenReturn(mockData);

		String peoId = Constants.X11_PEO_ID;
		String quarter = "Q1";
		Long realmYearId = 1L;
		Date payrollCutOffDate = new Date();

		List<String> actualResults = psDaoImpl.getPreLoadClients(peoId, quarter, realmYearId, payrollCutOffDate);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());

		peoId = Constants.PASSPORT_PEO_ID;
		actualResults = psDaoImpl.getPreLoadClients(peoId, quarter, realmYearId, payrollCutOffDate);

		verify(mockedQuery, times(2)).getResultList();
		assertEquals(2, actualResults.size());

	}

	@Test
	public void getDatabase() {
		List<String> mockData = new ArrayList<String>();
		mockData.add("hrdb");

		when(mockedQuery.getResultList()).thenReturn(mockData);

		String actualResults = psDaoImpl.getDatabase();

		assertEquals("hrdb", actualResults);
	}

	@Test
	public void getNewClientAddresses() {
		List<String> mockData = new ArrayList<String>();
		mockData.add("hrdb");
		Company company = prepareCompany();
		when(mockedQuery.getResultList()).thenReturn(prepareClientAssignmentEmails());
		when(psCompanyDao.isCSAUser("00001447261", company.getRealm().getId())).thenReturn(true);
		when(psCompanyDao.isCSAUser("00001447262", company.getRealm().getId())).thenReturn(false);

		// Results
		List<String> actualResults = psDaoImpl.getNewClientAddresses(company);
		assertEquals(1, actualResults.size());
		assertEquals("Testing1@trinet.com", actualResults.get(0));
		
		// Empty Results
		when(mockedQuery.getResultList()).thenReturn(new ArrayList<>());
		actualResults = psDaoImpl.getNewClientAddresses(company);
		assertEquals(true, actualResults.isEmpty());

		verify(mockedQuery, times(2)).getResultList();
}

	@Test
	public void getHsaMaximumsByEffDate() {
		Date effDate = new Date();
		// Test with empty return
		when(mockedQuery.getResultList()).thenReturn(prepareHsaMaximums());
		Map<String, BigDecimal> actualResults = psDaoImpl.getHsaMaximumsByEffDate(effDate);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());

		// Test with actual return
		when(mockedQuery.getResultList()).thenReturn(new ArrayList<Object[]>());
		actualResults = psDaoImpl.getHsaMaximumsByEffDate(effDate);
		verify(mockedQuery, times(2)).getResultList();
		assertEquals(0, actualResults.size());
	}
	
	@Test
	public void getEmployeesFullName() {

		Set<String> emplIdList = new HashSet<>();
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeNames());
		Map<String, String> actualResults = psDaoImpl.getEmployeesFullName(emplIdList);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals(2, actualResults.size());		
	}

	@Test
	public void getEmployeeFullName() {

		String emplId = "EMPLOYEE_1";
		when(mockedQuery.getResultList()).thenReturn(prepareEmployeeNames());
		String actualResults = psDaoImpl.getEmployeeFullName(emplId);
		verify(mockedQuery, times(1)).getResultList();
		assertEquals("EMPLOYEE 1 NAME", actualResults);		
	}
	
	
	private List<String> prepareUserName(String name) {
		List<String> data = new ArrayList<>();
		if(name != null)
			data.add(name);
		return data;
	}

	private List<Object[]> prepareClientAssignmentEmails() {
		List<Object[]> data = new ArrayList<Object[]>();
		Object[] r = new Object[2];
		r[0] = "00001447262";
		r[1] = "Testing2@trinet.com";
		data.add(r);
		r = new Object[2];
		r[0] = "00001447261";
		r[1] = "Testing1@trinet.com";
		data.add(r);
		r = new Object[2];
		r[0] = "00001447263";
		r[1] = null;
		data.add(r);
		return data;
	}

	private List<Object[]> prepareHsaMaximums() {
		List<Object[]> data = new ArrayList<Object[]>(2);
		Object[] r = new Object[2];
		r[0] = "HSA_ANNUAL_EMPLOYEE_MAXIMUM";
		r[1] = BigDecimal.valueOf(3000);
		data.add(r);
		r = new Object[2];
		r[0] = "HSA_ANNUAL_FAMILY_MAXIMUM";
		r[1] = BigDecimal.valueOf(6000);
		data.add(r);
		return data;

	}

	private List<Object[]> prepareEmployeeNames() {
		List<Object[]> data = new ArrayList<Object[]>(2);
		Object[] r = new Object[2];
		r[0] = "EMPLOYEE_1";
		r[1] = "EMPLOYEE 1 NAME";
		data.add(r);
		r = new Object[2];
		r[0] = "EMPLOYEE_2";
		r[1] = "EMPLOYEE 2 NAME";
		data.add(r);
		return data;

	}

	private Company prepareCompany() {
		Company company = new Company();
		Realm realm = new Realm();
		realm.setPeoid("PAS");
		realm.setId(1L);
		realm.setBenExchange("TriNet III");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(31);
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());

		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setCloseDate(new Date());
		schedTbl.setExtensionEndDate(new Date());

		company.setId(0);
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(10);
		company.setRealm(realm);
		company.setRealmPlanYear(realmPlanYear);
		company.setQuater("Q2");
		company.setRenewalCompany(true);
		company.setPlanStartDate("01-JAN-2019");
		company.setPlanEndDate("31-DEC-2019");
		company.setSchedTbl(schedTbl);
		return company;
	}

}