package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.model.AdditionalBenefitPlanRates;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.mock.web.MockHttpServletRequest;


@RunWith(MockitoJUnitRunner.class)
public class AdditionalBenefitsControllerTest extends ServiceUnitTest {

	@InjectMocks
	AdditionalBenefitsController additionalBenefitsController;

	@Mock
	CompanyService companyService;

	@Mock
	AdditionalBenefitPlanService additionalBenefitPlanService;

	private static final String EMPLID = "0000000123456";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void getAdditionalPlanRates() {

		HttpServletRequest request;
		List<AdditionalBenefitPlanRates> mockResult = new ArrayList<AdditionalBenefitPlanRates>();
		List<AdditionalBenefitPlanRates> actualResult;
		String companyCode = "TEST";
		Company company = new Company();

		request = new MockHttpServletRequest();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(additionalBenefitPlanService.getADBPlanCostByGroup(company, 0L)).thenReturn(mockResult);

		actualResult = additionalBenefitsController.getAdditionalPlanRates(request, 0L, companyCode, null);
		assertEquals(mockResult, actualResult);

	}

	@Test
	public void getAdditionalPlanRatesNewCompany() {

		HttpServletRequest request;
		List<AdditionalBenefitPlanRates> mockResult = new ArrayList<AdditionalBenefitPlanRates>();
		List<AdditionalBenefitPlanRates> actualResult;
		String companyCode = "TEST";
		Company company = new Company();

		request = new MockHttpServletRequest();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(additionalBenefitPlanService.getADBPlanCostByGroup(company, 0L)).thenReturn(mockResult);

		actualResult = additionalBenefitsController.getAdditionalPlanRatesNewCompany(request, companyCode, null);
		assertEquals(mockResult, actualResult);

	}
}
