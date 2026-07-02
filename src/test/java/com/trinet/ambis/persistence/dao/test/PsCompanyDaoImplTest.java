package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trinet.ambis.persistence.dao.ps.impl.PsCompanyDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.RealmTypeService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:*/service-unit-test-context.xml" })
public class PsCompanyDaoImplTest {
	PsCompanyDaoImpl psCompanyDao = new PsCompanyDaoImpl();
	Company uc = mock(Company.class);
	EntityManager em = null;
	EntityManager hrpEm = null;

	RealmTypeService realmTypeService = null;
	Query mockedQuery = null;
	private static final String COMPANY_CODE = "TEST";
	@Before
	public void setup() {
		uc.setCode("FZU");
		em = mock(EntityManager.class);
		hrpEm = mock(EntityManager.class);
		psCompanyDao.setEntityManager(em);
		psCompanyDao.setHrpEntityManager(hrpEm);
		realmTypeService = mock(RealmTypeService.class);
		psCompanyDao.setRealmTypeService(realmTypeService);
		mockedQuery = mock(Query.class);

		when(psCompanyDao.getHrpEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		when(psCompanyDao.getEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void refreshCompanyCensusTest() {
		when( em.createNativeQuery( Mockito.anyString() )).thenReturn( mockedQuery );
		psCompanyDao.refreshCompanyCensus( COMPANY_CODE, 0 );
	}

	@Test
	public void getCompanyCensusCreateDtTest() {
		Date date = new Date();
		when( hrpEm.createNamedQuery( PsCompanyDaoImpl.COMPANY_CENSUS_CREATED_DT )).thenReturn( mockedQuery );
		when( mockedQuery.getSingleResult() ).thenReturn( new Timestamp( date.getTime() ));
		Object response = psCompanyDao.getCompanyCensusCreateDt( COMPANY_CODE, 0 );
		assertTrue( response instanceof Timestamp );
		assertTrue( ((Timestamp) response ).getTime() == date.getTime() );
	}

	@Test
	public void getCompanyDetailsTest() {
		when(mockedQuery.setParameter(Mockito.anyString(), Mockito.any())).thenReturn(mockedQuery);
		when(psCompanyDao.getEntityManager().createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(getCompanyDetailsResult());
		when(realmTypeService.findByQuarter("Q1")).thenReturn(new Realm());

		// test method that uses COMPANY_DETAILS query
		Company comp = psCompanyDao.getBasicCompanyDetails("FZU");

		assertEquals("9A81", comp.getPfClient());
	}

	@Test
	public void getCompanyDetailsEffdt() {
		Company company = new Company();
		Date effdt = new Date();
		when(mockedQuery.getResultList()).thenReturn(getCompanyDetailsEffdtMockData());

		Company actualResult = psCompanyDao.getCompanyDetailsByEffdt(company, effdt);

		assertEquals( "Summit Rock Advisors, LP", actualResult.getDescription());
		assertEquals( "G", actualResult.getBandCodes().getKaiHawaiiBandCode());
		assertEquals( "J", actualResult.getBandCodes().getEmpireNYBand() );
		assertEquals( "K", actualResult.getBandCodes().getHarvardBandCode() );
		assertEquals( "EXCMED", actualResult.getExclusiveMedPlan() );
		assertEquals( true, actualResult.isTexasSitus() );
	}

	@Test
	public void isBDMUser() {
		String personid = "0000222245";
		String company = "G48";

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));

		boolean actualResult = psCompanyDao.isBDMUser(personid, company);

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(0));

		actualResult = psCompanyDao.isBDMUser(personid, company);

		assertEquals(false, actualResult);
		
		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.isBDMUser(personid, company);

		assertEquals(false, actualResult);
	}

	@Test
	public void isCSAUser() {
		String empId = "0000222245";
		Company company = prepareCompany();
		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));

		boolean actualResult = psCompanyDao.isCSAUser(empId, company.getRealm().getId());

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(0));

		actualResult = psCompanyDao.isCSAUser(empId, company.getRealm().getId());

		assertEquals(false, actualResult);
		
		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.isCSAUser(empId, company.getRealm().getId());

		assertEquals(false, actualResult);
	}

	@Test
	public void isActiveWithCompany() {
		String personid = "0000222245";
		String company = "G48";

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));

		boolean actualResult = psCompanyDao.isActiveWithCompany(personid, company);

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(0));

		actualResult = psCompanyDao.isActiveWithCompany(personid, company);

		assertEquals(false, actualResult);
		
		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.isActiveWithCompany(personid, company);

		assertEquals(false, actualResult);
	}

	@Test
	public void isBMGUser() {
		String personId = "0000222245";

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));

		boolean actualResult = psCompanyDao.isBMGUser(personId);

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(0));

		actualResult = psCompanyDao.isBMGUser(personId);

		assertEquals(false, actualResult);

		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.isBMGUser(personId);

		assertEquals(false, actualResult);
	}

	@Test
	public void isTMTUser() {
		String personId = "0000222245";

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));

		boolean actualResult = psCompanyDao.isTMTUser(personId);

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(0));

		actualResult = psCompanyDao.isTMTUser(personId);

		assertEquals(false, actualResult);
		
		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.isTMTUser(personId);

		assertEquals(false, actualResult);
	}
	
	@Test
	public void isBenCorpAdUser() {
		String personId = "0000222245";

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));

		boolean actualResult = psCompanyDao.isBenCorpAdUser(personId);

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(0));

		actualResult = psCompanyDao.isBenCorpAdUser(personId);

		assertEquals(false, actualResult);
		
		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.isBenCorpAdUser(personId);

		assertEquals(false, actualResult);
	}

	@Test
	public void getCompanyActualHeadCount() {
		String company = "G48";

		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(5));

		int actualResult = psCompanyDao.getCompanyActualHeadCount(company);

		assertEquals(5, actualResult);
		
		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.getCompanyActualHeadCount(company);

		assertEquals(0, actualResult);
	}

	@Test
	public void isPayConfirm() {
		String company = "G48";

		when(mockedQuery.getSingleResult()).thenReturn("YES");

		boolean actualResult = psCompanyDao.isPayConfirm(company);

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn("NO");

		actualResult = psCompanyDao.isPayConfirm(company);

		assertEquals(false, actualResult);

		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		actualResult = psCompanyDao.isPayConfirm(company);

		assertFalse(actualResult);
	}



	@Test
	public void isNewBandsAvailable() {
		Company company = new Company();
		Date effDate = new Date();

		when(mockedQuery.getSingleResult()).thenReturn("TRUE");

		boolean actualResult = psCompanyDao.isNewBandsAvailable(company, effDate);

		assertEquals(true, actualResult);

		when(mockedQuery.getSingleResult()).thenReturn("FALSE");

		actualResult = psCompanyDao.isNewBandsAvailable(company, effDate);

		assertEquals(false, actualResult);
	}

	@Test
	public void isNewBandsAvailable_exception() {
		Company company = new Company();
		Date effDate = new Date();

		when(mockedQuery.getSingleResult()).thenThrow(NoResultException.class);

		boolean actualResult = psCompanyDao.isNewBandsAvailable(company, effDate);

		assertEquals(false, actualResult);
	}
	
	@Test
	public void getLowestCostPlanPerPlanCarrier() {
		Company company = new Company();
		RealmPlanYear rp = new RealmPlanYear();
		company.setRealmPlanYear(rp);
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] temp = new Object[4];
		temp[0] = "10";
		temp[1] = new BigDecimal(10);
		temp[2] = new BigDecimal(10);
		temp[3] = new BigDecimal(132131);
		list.add(temp);
		when(mockedQuery.getResultList()).thenReturn(list);
		List<CarrierMinimumFunding> actualResult = psCompanyDao.getLowestCostPlanPerPlanCarrier(company);
		assertEquals(1, actualResult.size());
	}
	
	@Test
	public void isTermedCompany_true() {
		when(mockedQuery.getSingleResult()).thenReturn("true");

		boolean actualResult = psCompanyDao.isTermedCompany("G48");

		verify(em, times(1)).createNamedQuery("IS_TERMED_COMPANY");
		assertTrue(actualResult);
	}
	
	@Test
	public void isTermedCompany_false() {
		when(mockedQuery.getSingleResult()).thenReturn("false");

		boolean actualResult = psCompanyDao.isTermedCompany("G48");

		verify(em, times(1)).createNamedQuery("IS_TERMED_COMPANY");
		assertFalse(actualResult);
	}
	
	@Test
	public void isTexasSitus_true() {
		Date effDate = new Date();
		when(mockedQuery.getSingleResult()).thenReturn("true");

		boolean actualResult = psCompanyDao.isTexasSitus("G48", effDate);

		verify(em, times(1)).createNamedQuery("IS_TEXAS_SITUS");
		verify(mockedQuery, times(1)).setParameter("companyCode", "G48");
		verify(mockedQuery, times(1)).setParameter("effDate", effDate);
		assertTrue(actualResult);
	}
	
	@Test
	public void isTexasSitus_false() {
		Date effDate = new Date();
		when(mockedQuery.getSingleResult()).thenReturn("false");

		boolean actualResult = psCompanyDao.isTexasSitus("G48", effDate);

		verify(em, times(1)).createNamedQuery("IS_TEXAS_SITUS");
		verify(mockedQuery, times(1)).setParameter("companyCode", "G48");
		verify(mockedQuery, times(1)).setParameter("effDate", effDate);
		assertFalse(actualResult);
	}

	private List<Object[]> getCompanyDetailsResult() {
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] temp1 = new Object[37];
		temp1[0] = "9A81";
		temp1[1] = "01-JAN-2010";
		temp1[2] = "02-JAN-2010";
		temp1[3] = "4Q";
		temp1[4] = "AMB";
		temp1[5] = "52";
		temp1[6] = "BENPGM";
		temp1[7] = "CA";
		temp1[8] = "A1";
		temp1[9] = "B1";
		temp1[10] = "A";
		temp1[11] = "Z";
		temp1[12] = "Y";
		temp1[13] = "X";
		temp1[14] = "W";
		temp1[15] = "V";
		temp1[16] = "2";
		temp1[17] = "3";
		temp1[18] = "The Little Company";
		temp1[19] = "The Little Company";
		temp1[20] = "San Francisco";
		temp1[21] = "Y";
		temp1[27] = "D";
		temp1[28] = "T";
		temp1[29] = "E";
		temp1[30] = "F";
		temp1[31] = "G";
		temp1[32] = "H";
		temp1[33] = "I";
		temp1[34] = "J";
		temp1[35] = "EXCMED";
		temp1[36] = "29715";
		list.add(temp1);
		return list;
	}

	private List<Object[]> getCompanyDetailsEffdtMockData() {
		List<Object[]> list = new ArrayList<Object[]>();
		Object[] temp1 = new Object[35];
		temp1[0] = "9ABK";
		temp1[1] = "01-SEP-2008";
		temp1[2] = "01-SEP-2008";
		temp1[3] = "8Y";
		temp1[4] = "521234";
		temp1[5] = "UPP";
		temp1[6] = "NY";
		temp1[7] = "4NA";
		temp1[8] = "4NA";
		temp1[9] = "4NA";
		temp1[10] = "4NA";
		temp1[11] = "4NA";
		temp1[12] = "4NA";
		temp1[13] = "4NA";
		temp1[14] = "4NA";
		temp1[15] = "2";
		temp1[16] = "3";
		temp1[17] = "Summit Rock Advisors, LP";
		temp1[18] = "Summit Rock Advisors, LP";
		temp1[19] = "New York";
		temp1[20] = "Y";
		temp1[21] = "01-SEP-2008";
		temp1[22] = "D";
		temp1[23] = "4NA";
		temp1[24] = "E";
		temp1[25] = "F";
		temp1[26] = "G";
		temp1[27] = "H";
		temp1[28] = "I";
		temp1[29] = "J";
		temp1[30] = "EXCMED";
		temp1[31] = "94044";
		temp1[32] = "DFTX";
		temp1[33] = "Y";
		temp1[34] = "K";
		list.add(temp1);
		return list;
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