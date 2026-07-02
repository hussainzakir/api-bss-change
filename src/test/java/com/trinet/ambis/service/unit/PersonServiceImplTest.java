package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;

import com.trinet.ambis.persistence.dao.hrp.PersonDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.service.impl.PersonServiceImpl;

@RunWith(JUnit4.class)
public class PersonServiceImplTest {

	@InjectMocks
	PersonServiceImpl personService;

	@Mock
	PersonDao personDao;

	@Mock
	PsDao psDao;

	@Mock
	Company company;

	private final static String USER_ID = "000222278";
	private final static String COMP_CODE = "C2E";
	private final static String USER_FULL_NAME = "Linda Veldhuizen";
	private final static String VALUED_CLIENT = "Valued Client";
	private final static String CS_AUTH_EMAIL = "csauthmail@client.com";
	private final static String COMPANY_HRM_EMAIL = "comphrmmail@client.com";

	@Before
	public void setup() throws ParseException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getPersonFirstAndLastName() {
		when(personDao.getFirstandLastName(USER_ID)).thenReturn(USER_FULL_NAME);
		String employeeName = personService.getPersonFirstAndLastName(USER_ID);
		verify(personDao, times(1)).getFirstandLastName(USER_ID);
		assertEquals(USER_FULL_NAME, employeeName);
	}

	@Test
	public void testPrepareNameForConfirmationEmail_TMTUser() {
		when(company.isTMTUser()).thenReturn(true);
		String result = personService.prepareNameForConfirmationEmail(USER_ID, company);
		assertEquals("Internal", result);
		verify(psDao).getEmployeeFullName(USER_ID);
	}

	@Test
	public void testPrepareNameForConfirmationEmail_Success() {
		when(company.isTMTUser()).thenReturn(false);
		when(psDao.getEmployeeFullName(USER_ID)).thenReturn(USER_FULL_NAME);
		String result = personService.prepareNameForConfirmationEmail(USER_ID, company);
		assertEquals(USER_FULL_NAME, result);
	}

	@Test
	public void testPrepareNameForConfirmationEmail_BlankName() {
		when(company.isTMTUser()).thenReturn(false);
		when(psDao.getEmployeeFullName(USER_ID)).thenReturn("   "); // Blank name
		String result = personService.prepareNameForConfirmationEmail(USER_ID, company);
		assertEquals(VALUED_CLIENT, result);
	}

	@Test
	public void testPrepareNameForConfirmationEmail_UserNameNull() {
		when(company.isTMTUser()).thenReturn(false);
		when(psDao.getEmployeeFullName(USER_ID)).thenReturn(null);
		String result = personService.prepareNameForConfirmationEmail(USER_ID, company);
		assertEquals(VALUED_CLIENT, result);
	}

	@Test
	public void testGetCsAuthEmail() {
		when(personDao.getCSAuthEmail(USER_ID)).thenReturn(CS_AUTH_EMAIL);
		String csAuthMail = personService.getCSAuthEmail(USER_ID);
		verify(personDao, times(1)).getCSAuthEmail(USER_ID);
		assertEquals(CS_AUTH_EMAIL, csAuthMail);
	}

	@Test
	public void testGetCsAuthEmail_empty() {
		when(personDao.getCSAuthEmail(USER_ID)).thenReturn(null);
		String csAuthMail = personService.getCSAuthEmail(USER_ID);
		verify(personDao, times(1)).getCSAuthEmail(USER_ID);
		assertNull(csAuthMail);
	}

	@Test
	public void testGetCompanyHrmEmail() {
		when(personDao.getCompanyHrmEmail(COMP_CODE)).thenReturn(COMPANY_HRM_EMAIL);
		String companyHrmEmail = personService.getCompanyHrmEmail(COMP_CODE);
		verify(personDao, times(1)).getCompanyHrmEmail(COMP_CODE);
		assertEquals(COMPANY_HRM_EMAIL, companyHrmEmail);
	}

	@Test
	public void testGetCompanyHrmEmail_empty() {
		when(personDao.getCompanyHrmEmail(COMP_CODE)).thenReturn(null);
		String companyHrmEmail = personService.getCompanyHrmEmail(COMP_CODE);
		verify(personDao, times(1)).getCompanyHrmEmail(COMP_CODE);
		assertNull(companyHrmEmail);
	}
}