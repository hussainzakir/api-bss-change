package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.dao.ps.SupplementalAuthDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.email.EmailAddressService;
import com.trinet.ambis.service.impl.SupplementalAuthServiceImpl;
import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class SupplementalAuthServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	private SupplementalAuthServiceImpl supplementalAuthService;

	@Mock
	private PsDao psDao;

	@Mock
	private EmailAddressService emailAddressService;

	@Mock
	private SupplementalAuthDao supplementalAuthDao;

	@Mock
	private CompanyService companyService;

	private static final String COMPANY_CODE = "G48";
	private static final String LOGGED_IN_PERSON_ID = "00002222278";
	private static final String AUTH_PERSON_ID = "00003333378";
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMock;

    @Before
    public void setUp() {
        bssSecurityUtilsMock = mockStatic(BSSSecurityUtils.class);
        prepareCommonMock();
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMock.close();
    }

	@Test
	public void getExecSuppLtdAuthResponse_whenRenewalPeriodClosed() {
		Company comp = new Company();
		comp.setRenewalOpen(false);
		when(companyService.getCompanyDetails(COMPANY_CODE, false, LOGGED_IN_PERSON_ID, null)).thenReturn(comp);

		SupplementalLtdAuthReponse result = supplementalAuthService.getExecSuppLtdAuthResponse(COMPANY_CODE);

		assertEquals(null, result.getAnswer());
		assertEquals(null, result.getAuthUserId());
		assertEquals(null, result.getAuthEmail());
		assertEquals(null, result.getAuthFirstName());
		assertEquals(null, result.getAuthLastName());
		assertEquals(null, result.getUserId());
		assertEquals(null, result.getFirstName());
		assertEquals(null, result.getLastName());
		assertEquals(false, result.isDisplayPopup());
		assertEquals(false, result.isDisplayBanners());
	}

	@Test
	public void getExecSuppLtdAuthResponse_whenRenewalPeriodOpen() {
		Company comp = new Company();
		comp.setRenewalOpen(true);
		when(companyService.getCompanyDetails(COMPANY_CODE, false, LOGGED_IN_PERSON_ID, null)).thenReturn(comp);

		SupplementalLtdAuthReponse result = supplementalAuthService.getExecSuppLtdAuthResponse(COMPANY_CODE);

		assertEquals(null, result.getAnswer());
		assertEquals(null, result.getAuthUserId());
		assertEquals(null, result.getAuthEmail());
		assertEquals(null, result.getAuthFirstName());
		assertEquals(null, result.getAuthLastName());
		assertEquals(LOGGED_IN_PERSON_ID, result.getUserId());
		assertEquals("Fname", result.getFirstName());
		assertEquals("Lname", result.getLastName());
		assertEquals("Fname.Lname@trinet.com", result.getEmail());
		assertEquals(true, result.isDisplayPopup());
		assertEquals(true, result.isDisplayBanners());
	}

	@Test
	public void getExecSuppLtdAuthResponse_whenRenewalPeriodOpenAndResponseRecordedInSameOpenPeriod()
			throws ParseException {
		Date authDate = new SimpleDateFormat("MM/dd/yyyy").parse("10/15/2020");
		Company comp = new Company();
		comp.setRenewalOpen(true);
		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setOpenDate(new SimpleDateFormat("MM/dd/yyyy").parse("10/01/2020"));
		schedTbl.setCloseDate(new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2020"));
		schedTbl.setExtensionEndDate(new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2020"));
		comp.setSchedTbl(schedTbl);
		SupplementalLtdAuthReponse response = SupplementalLtdAuthReponse.builder().authUserId(AUTH_PERSON_ID)
				.answer('Y').authDate(authDate).build();

		when(companyService.getCompanyDetails(COMPANY_CODE, false, LOGGED_IN_PERSON_ID, null)).thenReturn(comp);
		when(supplementalAuthDao.getExecSuppLtdAuthResponse(COMPANY_CODE)).thenReturn(response);

		SupplementalLtdAuthReponse result = supplementalAuthService.getExecSuppLtdAuthResponse(COMPANY_CODE);
		assertEquals("Y", String.valueOf(result.getAnswer()));
		assertEquals(AUTH_PERSON_ID, result.getAuthUserId());
		assertEquals("AFname.ALname@trinet.com", result.getAuthEmail());
		assertEquals("AFname", result.getAuthFirstName());
		assertEquals("ALname", result.getAuthLastName());
		assertEquals(LOGGED_IN_PERSON_ID, result.getUserId());
		assertEquals("Fname", result.getFirstName());
		assertEquals("Lname", result.getLastName());
		assertEquals("Fname.Lname@trinet.com", result.getEmail());
		assertEquals(false, result.isDisplayPopup());
		assertEquals(true, result.isDisplayBanners());
	}
	
	@Test
	public void getExecSuppLtdAuthResponse_whenRenewalPeriodOpenAndResponseRecordedInSameOpenPeriodExtDtIsGreater()
			throws ParseException {
		Date authDate = new SimpleDateFormat("MM/dd/yyyy").parse("10/15/2020");
		Company comp = new Company();
		comp.setRenewalOpen(true);
		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setOpenDate(new SimpleDateFormat("MM/dd/yyyy").parse("10/01/2020"));
		schedTbl.setCloseDate(new SimpleDateFormat("MM/dd/yyyy").parse("11/29/2020"));
		schedTbl.setExtensionEndDate(new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2020"));
		comp.setSchedTbl(schedTbl);
		SupplementalLtdAuthReponse response = SupplementalLtdAuthReponse.builder().authUserId(AUTH_PERSON_ID)
				.answer('Y').authDate(authDate).build();

		when(companyService.getCompanyDetails(COMPANY_CODE, false, LOGGED_IN_PERSON_ID, null)).thenReturn(comp);
		when(supplementalAuthDao.getExecSuppLtdAuthResponse(COMPANY_CODE)).thenReturn(response);

		SupplementalLtdAuthReponse result = supplementalAuthService.getExecSuppLtdAuthResponse(COMPANY_CODE);
		assertEquals("Y", String.valueOf(result.getAnswer()));
		assertEquals(AUTH_PERSON_ID, result.getAuthUserId());
		assertEquals("AFname.ALname@trinet.com", result.getAuthEmail());
		assertEquals("AFname", result.getAuthFirstName());
		assertEquals("ALname", result.getAuthLastName());
		assertEquals(LOGGED_IN_PERSON_ID, result.getUserId());
		assertEquals("Fname", result.getFirstName());
		assertEquals("Lname", result.getLastName());
		assertEquals("Fname.Lname@trinet.com", result.getEmail());
		assertEquals(false, result.isDisplayPopup());
		assertEquals(true, result.isDisplayBanners());
	}

	@Test
	public void getExecSuppLtdAuthResponse_whenRenewalPeriodOpenAndResponseRecordedInPriorOpenPeriod()
			throws ParseException {
		Date authDate = new SimpleDateFormat("MM/dd/yyyy").parse("10/15/2019");
		Company comp = new Company();
		comp.setRenewalOpen(true);
		SchedTbl schedTbl = new SchedTbl();
		schedTbl.setOpenDate(new SimpleDateFormat("MM/dd/yyyy").parse("10/01/2020"));
		schedTbl.setCloseDate(new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2020"));
		schedTbl.setExtensionEndDate(new SimpleDateFormat("MM/dd/yyyy").parse("12/31/2020"));
		comp.setSchedTbl(schedTbl);
		SupplementalLtdAuthReponse response = SupplementalLtdAuthReponse.builder().authUserId(AUTH_PERSON_ID)
				.answer('Y').authDate(authDate).build();

		when(companyService.getCompanyDetails(COMPANY_CODE, false, LOGGED_IN_PERSON_ID, null)).thenReturn(comp);
		when(supplementalAuthDao.getExecSuppLtdAuthResponse(COMPANY_CODE)).thenReturn(response);

		SupplementalLtdAuthReponse result = supplementalAuthService.getExecSuppLtdAuthResponse(COMPANY_CODE);

		assertEquals("Y", String.valueOf(result.getAnswer()));
		assertEquals(AUTH_PERSON_ID, result.getAuthUserId());
		assertEquals("AFname.ALname@trinet.com", result.getAuthEmail());
		assertEquals("AFname", result.getAuthFirstName());
		assertEquals("ALname", result.getAuthLastName());
		assertEquals(LOGGED_IN_PERSON_ID, result.getUserId());
		assertEquals("Fname", result.getFirstName());
		assertEquals("Lname", result.getLastName());
		assertEquals("Fname.Lname@trinet.com", result.getEmail());
		assertEquals(false, result.isDisplayPopup());
		assertEquals(false, result.isDisplayBanners());
	}

	@Test
	public void saveExecSuppLtdAuthResponse() {
		Company comp = new Company();
		comp.setRenewalOpen(true);

		ArgumentCaptor<SupplementalLtdAuthReponse> argCaptor = ArgumentCaptor
				.forClass(SupplementalLtdAuthReponse.class);
		ArgumentCaptor<String> compArgCaptor = ArgumentCaptor.forClass(String.class);

		when(companyService.getCompanyDetails(COMPANY_CODE, false, LOGGED_IN_PERSON_ID, null)).thenReturn(comp);

		supplementalAuthService.saveExecSuppLtdAuthResponse(COMPANY_CODE, 'Y');

		verify(supplementalAuthDao, times(1)).saveExecSuppLtdAuthResponse(compArgCaptor.capture(), argCaptor.capture());
		assertEquals(LOGGED_IN_PERSON_ID, argCaptor.getValue().getAuthUserId());
		assertEquals(String.valueOf('Y'), String.valueOf(argCaptor.getValue().getAnswer()));
		assertEquals(COMPANY_CODE, String.valueOf(compArgCaptor.getValue()));
	}

	private void prepareCommonMock() {
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(LOGGED_IN_PERSON_ID);

		when(psDao.getEmployeeFirstName(LOGGED_IN_PERSON_ID)).thenReturn("Fname");
		when(psDao.getEmployeeLastName(LOGGED_IN_PERSON_ID)).thenReturn("Lname");
		when(emailAddressService.getEmployeeEmail(COMPANY_CODE, LOGGED_IN_PERSON_ID))
				.thenReturn("Fname.Lname@trinet.com");

		when(psDao.getEmployeeFirstName(AUTH_PERSON_ID)).thenReturn("AFname");
		when(psDao.getEmployeeLastName(AUTH_PERSON_ID)).thenReturn("ALname");
		when(emailAddressService.getEmployeeEmail(COMPANY_CODE, AUTH_PERSON_ID)).thenReturn("AFname.ALname@trinet.com");
	}

}
