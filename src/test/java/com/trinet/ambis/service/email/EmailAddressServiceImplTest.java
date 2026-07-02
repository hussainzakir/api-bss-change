package com.trinet.ambis.service.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.email.impl.EmailAddressServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class EmailAddressServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	EmailAddressServiceImpl emailAddressService;

	@Mock
	private HrpDao hrpDao;

	@Mock
	PsDao psDao;

    private MockedStatic<BSSMessageConfig> mockStaticBSSMessageConfig;

	@Before
	public void setUp() throws Exception {
        mockStaticBSSMessageConfig = Mockito.mockStatic(BSSMessageConfig.class);
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("ImplementationTeamEmail"))
                .thenReturn("implementationteam@trinet.com");
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("CustomerSetupSiteEmail"))
                .thenReturn("CustomerSetupSite@trinet.com");
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("CssEmail"))
                .thenReturn("css@trinet.com");
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("TmtEmail"))
                .thenReturn("TMT@trinet.com");
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("AmbroseImplementationEmail"))
                .thenReturn("ambroseimplementation@trinet.com");
        mockStaticBSSMessageConfig.when(() -> BSSMessageConfig.getProperty("FundingConfirmationEmail"))
                .thenReturn("fundingconfirm@trinet.com");
    }

    @After
    public void tearDown() {
        if (mockStaticBSSMessageConfig != null) {
            mockStaticBSSMessageConfig.close();
            mockStaticBSSMessageConfig = null;
        }
    }

	@Test
	public void getConfirmationStatementClientRecipients_testTriNetCompany() {
		String companyCode = "001";
		String employeeId = "000033337874";
		Set<String> roleEmails = new HashSet<>();
		roleEmails.add("roleEmail@somedomain.com");
		String employeeEmail = "emplEmail@somedomain.com";
		Company company = new Company();
		company.setCode(companyCode);
		when(hrpDao.getRoleEmails(companyCode, "BEN_CORP_AD")).thenReturn(roleEmails);
		when(hrpDao.getEmplEmail(companyCode, employeeId)).thenReturn(employeeEmail);

		Set<String> emails = emailAddressService.getConfirmationStatementClientRecipients(company, employeeId);

		assertEquals(2, emails.size());
		assertTrue(emails.containsAll(Arrays.asList("roleEmail@somedomain.com", "emplEmail@somedomain.com")));
		verify(hrpDao, times(0)).getBDMEmails(anyString());
	}

	@Test
	public void getConfirmationStatementClientRecipients_testNonTriNetCompany() {
		String companyCode = "G48";
		String employeeId = "000033337874";
		Set<String> bdmEmails = new HashSet<>();
		bdmEmails.add("bdmEmail@somedomain.com");
		String employeeEmail = "emplEmail@somedomain.com";
		Company company = new Company();
		company.setCode(companyCode);
		when(hrpDao.getBDMEmails(companyCode)).thenReturn(bdmEmails);
		when(hrpDao.getEmplEmail(companyCode, employeeId)).thenReturn(employeeEmail);

		Set<String> emails = emailAddressService.getConfirmationStatementClientRecipients(company, employeeId);

		assertEquals(2, emails.size());
		assertEquals(emails, new HashSet<String>(Arrays.asList("bdmEmail@somedomain.com", "emplEmail@somedomain.com")));
		verify(hrpDao, times(0)).getRoleEmails(anyString(), anyString());
	}

	@Test
	public void getConfirmationStatementNonClientRecipients_testRenewalCompany() {
		String companyCode = "G48";
		Company company = new Company();
		company.setCode(companyCode);
		company.setRenewalCompany(true);
		List<String> assignmentAddresses = new ArrayList<>();
		assignmentAddresses.add("assignmentEmail@somedomain.com");

		when(psDao.getAssignmentAddresses(company)).thenReturn(assignmentAddresses);

		Set<String> emails = emailAddressService.getConfirmationStatementNonClientRecipients(company);

		assertEquals(5, emails.size());
		assertEquals(emails, new HashSet<String>(Arrays.asList("assignmentEmail@somedomain.com",
				"ambroseimplementation@trinet.com", "css@trinet.com", "TMT@trinet.com", "fundingconfirm@trinet.com")));
		verify(psDao, times(0)).getNewClientAddresses(any(Company.class));
	}

	@Test
	public void getConfirmationStatementNonClientRecipients_testNewCompany() {
		String companyCode = "G48";
		Company company = new Company();
		company.setCode(companyCode);
		List<String> newAssignmentAddresses = new ArrayList<>();
		newAssignmentAddresses.add("newAssignmentEmail@somedomain.com");

		when(psDao.getNewClientAddresses(company)).thenReturn(newAssignmentAddresses);

		Set<String> emails = emailAddressService.getConfirmationStatementNonClientRecipients(company);

		assertEquals(5, emails.size());
		assertEquals(emails,
				new HashSet<String>(Arrays.asList("newAssignmentEmail@somedomain.com", "implementationteam@trinet.com",
						"CustomerSetupSite@trinet.com", "TMT@trinet.com", "fundingconfirm@trinet.com")));
		verify(psDao, times(0)).getAssignmentAddresses(any(Company.class));
	}

}
