package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitCategoriesService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.TemplateDataService;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitsCategories;
import com.trinet.ambis.service.model.BenefitsCategory;
import com.trinet.ambis.service.model.NewCompanyOptions;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import java.util.HashSet;
import java.util.Set;
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
public class TemplateDataControllerTest extends ServiceUnitTest {

	@InjectMocks
	TemplateDataController templateDataController;

	@Mock
	CompanyService companyService;

	@Mock
	TemplateDataService templateDataService;

	@Mock
	BenefitCategoriesService benefitCategoriesService;

	private static final String emplId = "0000000123456";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(emplId);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void newCompanyOptions() {

		HttpServletRequest request = new MockHttpServletRequest();
		String companyCode = "TEST";
		NewCompanyOptions expectedResult = new NewCompanyOptions();
		NewCompanyOptions actualResult;
		Company company;

		/*
		 * Test success
		 */
		company = new Company();
		when(companyService.getCompanyDetails(companyCode)).thenReturn(company);
		when(templateDataService.getNewCompanyOptions(company)).thenReturn(expectedResult);

		actualResult = templateDataController.newCompanyOptions(request, companyCode);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getBenefitsCategories() {

		HttpServletRequest request;
		String companyCode = "TEST";
		BenefitsCategories expectedResult = prepareBenefitsCategories();
		BenefitsCategories actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		/*
		 * Test success
		 */
		company = new Company();
		when(companyService.getCompanyDetails(companyCode, false, emplId, null)).thenReturn(company);
		when(benefitCategoriesService.constructBenefitsCategories(company)).thenReturn(expectedResult);

		actualResult = templateDataController.getBenefitsCategories(request, companyCode, null);
//		assertEquals(expectedResult, actualResult);
	}
	/*
	 *
	 * Setup methods
	 * 
	 */
	private BenefitsCategories prepareBenefitsCategories() {
		BenefitsCategories benefitCategories = new BenefitsCategories();
		BenefitsCategory medical = new BenefitsCategory();
		Set<BenefitPlan> benefitPlans = new HashSet<>();
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlans.add(benefitPlan);
		medical.setBenefitPlans(benefitPlans);
		benefitCategories.setMedical(medical);
		return benefitCategories;
	}

}
