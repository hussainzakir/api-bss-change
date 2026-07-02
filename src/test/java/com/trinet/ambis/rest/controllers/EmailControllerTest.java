package com.trinet.ambis.rest.controllers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.email.EmailService;
import com.trinet.ambis.service.email.dto.ClientConversionRequestDto;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class EmailControllerTest extends ServiceUnitTest {

	@InjectMocks
	EmailController emailController;

	@Mock
	CompanyService companyService;

	@Mock
	EmailService emailService;

	@Mock
	private HttpServletRequest request;

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	private static final String EMPL_ID = "00001234596";
	private static final String COMPANY_CODE = "TEST";
	private static final String CONFIRMATION_NUMBER = "CONFIRMATION";
	public static final String AUTH_TOKEN = "AQIC5wM2LY4SfcyaCHj61cbE-i1pQwGjCjDszTcW7LHGX_w.*AAJTSQACMDMAAlNLABIzMTUzNjM2MDg3MDI4MDM4OTYAAlMxAAIwMQ..*";

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPL_ID);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
		}
	}

	@Test
	public void sendEmail() {
		Company company = null;

		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPL_ID, null)).thenReturn(company);

		emailController.sendEmail(request, CONFIRMATION_NUMBER, COMPANY_CODE);

		verify(emailService, times(1)).resendConfirmationEmail(company, CONFIRMATION_NUMBER);
	}
}
