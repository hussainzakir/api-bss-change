package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.impl.PlanHeadCountServiceImpl;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;
import com.trinet.ambis.service.model.PlanHeadCount;
import com.trinet.ambis.service.prospect.impl.ProspectPlanHeadCountServiceImpl;
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

@RunWith(MockitoJUnitRunner.class)
public class HeadCountControllerTest extends ServiceUnitTest {

	@InjectMocks
	HeadCountController headCountController;

	@Mock
	CompanyService companyService;

	@Mock
	PlanHeadCountServiceImpl planHeadCountService;
	
	@Mock
	ProspectPlanHeadCountServiceImpl prospectPlanHeadCountService;

	private static final String EMPLID = "0000000123456";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
	}

	@After
	public void tearDown() {
		mockStaticBSSSecurityUtils.close();
	}

	@Test
	public void getPlan() {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		List<PlanHeadCount> expectedResult = new ArrayList<PlanHeadCount>();
		List<PlanHeadCount> actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(planHeadCountService.getPlanHeadCount(company, strategyId)).thenReturn(expectedResult);

		actualResult = headCountController.getPlan(request, strategyId, companyCode, null);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getPlanheadCount() {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = null;
		List<BenefitProgramHeadCountPlans> expectedResult = new ArrayList<BenefitProgramHeadCountPlans>();
		List<BenefitProgramHeadCountPlans> actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(planHeadCountService.getBenefitProgramHeadCountPlans(company, strategyId)).thenReturn(expectedResult);

		actualResult = headCountController.getPlanheadCount(request, strategyId, companyCode, exchangeId);
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void getPlanheadCount_forProspect() {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = "TNIII";
		List<BenefitProgramHeadCountPlans> expectedResult = new ArrayList<BenefitProgramHeadCountPlans>();
		List<BenefitProgramHeadCountPlans> actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectCompany(true);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.TRINET_III)).thenReturn(company);
		when(prospectPlanHeadCountService.getBenefitProgramHeadCountPlans(company, strategyId)).thenReturn(expectedResult);

		actualResult = headCountController.getPlanheadCount(request, strategyId, companyCode, exchangeId);
		
		assertEquals(expectedResult, actualResult);
		verify(prospectPlanHeadCountService, times(1)).getBenefitProgramHeadCountPlans(company, strategyId);
	}
	
	@Test
	public void getPlanheadCount_forProspectConvertedOnboarding() {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = "TNIII";
		List<BenefitProgramHeadCountPlans> expectedResult = new ArrayList<BenefitProgramHeadCountPlans>();
		List<BenefitProgramHeadCountPlans> actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectConvertedOnboardingClient(true);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.TRINET_III))
				.thenReturn(company);
		when(prospectPlanHeadCountService.getBenefitProgramHeadCountPlans(company, strategyId))
				.thenReturn(expectedResult);

		actualResult = headCountController.getPlanheadCount(request, strategyId, companyCode, exchangeId);

		assertEquals(expectedResult, actualResult);
		verify(prospectPlanHeadCountService, times(1)).getBenefitProgramHeadCountPlans(company, strategyId);
	}

	@Test
	public void getPlanheadCount_forProspectConvertedRenewal() {

		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = "TNIII";
		List<BenefitProgramHeadCountPlans> expectedResult = new ArrayList<BenefitProgramHeadCountPlans>();
		List<BenefitProgramHeadCountPlans> actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectConvertedClient(true);
		company.setRenewalCompany(true);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.TRINET_III))
				.thenReturn(company);

		actualResult = headCountController.getPlanheadCount(request, strategyId, companyCode, exchangeId);

		assertEquals(expectedResult, actualResult);
		verify(prospectPlanHeadCountService, times(0)).getBenefitProgramHeadCountPlans(company, strategyId);
	}
	
	@Test
	public void getPlanheadCount_forRenewal() {
		HttpServletRequest request;
		long strategyId = 1000L;
		String companyCode = "TEST";
		String exchangeId = "TNIII";
		List<BenefitProgramHeadCountPlans> expectedResult = new ArrayList<BenefitProgramHeadCountPlans>();
		List<BenefitProgramHeadCountPlans> actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);

		company = new Company();
		company.setProspectConvertedClient(false);
		company.setRenewalCompany(true);
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, BenExchngEnums.TRINET_III))
				.thenReturn(company);

		actualResult = headCountController.getPlanheadCount(request, strategyId, companyCode, exchangeId);

		assertEquals(expectedResult, actualResult);
		verify(planHeadCountService, times(1)).getBenefitProgramHeadCountPlans(company, strategyId);
	}

}
