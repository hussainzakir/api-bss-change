package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.common.AppConfig;
import com.trinet.security.util.SecurityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.enums.PlanCompareConstants;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.plancompare.dao.hrp.impl.PlanCompareDaoImpl;
import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.PlanCompareServiceImpl;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlanCompareServiceTest extends ServiceUnitTest {
	
	@InjectMocks
	PlanCompareServiceImpl planCompareService;
	
	@Mock
	PlanCompareDaoImpl planCompareDao;
	
	@Mock
	RealmPlanYearService realmPlanYearService;
	
	@Mock
	CompanyService companyService;
	
	private static final String EMPLID = "00000123456";
	private static final String COMPANY_CODE = "D11";
	private static final String QUARTERNAME = "8Y";
    private MockedStatic<SecurityUtils> securityUtilsMockedStatic;
    private MockedStatic<AppConfig> appConfigMockedStatic;
    private MockedStatic<BSSMessageConfig> bssMessageConfigMockedStatic;
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;

    @Before
    public void setUp() {
        securityUtilsMockedStatic = mockStatic(SecurityUtils.class);
        appConfigMockedStatic = mockStatic(AppConfig.class);
        bssMessageConfigMockedStatic = mockStatic(BSSMessageConfig.class);
        bssSecurityUtilsMockedStatic = mockStatic(BSSSecurityUtils.class);
        bssSecurityUtilsMockedStatic.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
    }

    @After
    public void tearDown() {
        securityUtilsMockedStatic.close();
        appConfigMockedStatic.close();
        bssMessageConfigMockedStatic.close();
        bssSecurityUtilsMockedStatic.close();
    }
	
	@Test
	public void getYearPlans() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		company.setTransitionPeriod(Boolean.TRUE);
		company.setRenewalOpen(Boolean.TRUE);
		
		List<BenefitPlanDetailDto> benefitPlanDetail = new ArrayList<>();
		benefitPlanDetail.add(getBenefitPlanDetails().get());
		List<PlanYearDetailDto> planYearDetails = new ArrayList<>();
		planYearDetails.add(getCurrentPlanYearDetail().get());
		planYearDetails.add(getFuturePlanYearDetail().get());
		
		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, null)).thenReturn(company);
		when(realmPlanYearService.findCurrentAndFuturePlanYearsBy(company.getCode(),company.getQuater())).thenReturn(planYearDetails);
		when(planCompareDao.findSubmittedStrategyPlansBy(company.getCode(), getCurrentPlanYearDetail().get().getRealmYearId())).thenReturn(benefitPlanDetail);
		List<BenefitPlanDetailDto> companyYearPlans = planCompareService.findSubmittedStrategyPlansBy(company.getCode());
			
		assertNotNull(companyYearPlans);
		assertEquals(1, companyYearPlans.size());
		assertEquals(getBenefitPlanDetails().get().getPlanId(), companyYearPlans.get(0).getPlanId());
		assertEquals(getBenefitPlanDetails().get().getOfferType(), companyYearPlans.get(0).getOfferType());
	}
	
	@Test
	public void getYearPlans_No_Fund() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		company.setTransitionPeriod(Boolean.FALSE);
		company.setRenewalOpen(Boolean.FALSE);
		
		List<BenefitPlanDetailDto> benefitPlanDetail = new ArrayList<>();
		benefitPlanDetail.add(getBenefitPlanDetails().get());
		List<PlanYearDetailDto> planYearDetails = new ArrayList<>();
		planYearDetails.add(getCurrentPlanYearDetail().get());
		planYearDetails.add(getFuturePlanYearDetail().get());
		
		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, null)).thenReturn(company);
		when(realmPlanYearService.findCurrentAndFuturePlanYearsBy(company.getCode(),company.getQuater())).thenReturn(planYearDetails);
		when(planCompareDao.findSubmittedStrategyPlansBy(company.getCode(), getCurrentPlanYearDetail().get().getRealmYearId())).thenReturn(benefitPlanDetail);
		List<BenefitPlanDetailDto> companyYearPlans = planCompareService.findSubmittedStrategyPlansBy(company.getCode());
			
		assertNotNull(companyYearPlans);
		assertEquals(1, companyYearPlans.size());
		assertEquals(getBenefitPlanDetails().get().getPlanId(), companyYearPlans.get(0).getPlanId());
		assertEquals(getBenefitPlanDetails().get().getOfferType(), companyYearPlans.get(0).getOfferType());
	}
	
	@Test
	public void findMappingBenefitPlansByTest() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		company.setTransitionPeriod(Boolean.TRUE);
		company.setRenewalOpen(Boolean.TRUE);
		
		List<MappedPlanDetailDto> mappedPlanDetails = new ArrayList<>();
		mappedPlanDetails.add(getMappedPlanDetails().get());
		
		List<BenefitPlanDetailDto> benefitPlanDetail = new ArrayList<>();
		benefitPlanDetail.add(getBenefitPlanDetails().get());
		benefitPlanDetail.add(getBenefitPlanDetails().get());
		List<PlanYearDetailDto> planYearDetails = new ArrayList<>();
		planYearDetails.add(getCurrentPlanYearDetail().get());
		planYearDetails.add(getFuturePlanYearDetail().get());
		
		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, null)).thenReturn(company);
		when(realmPlanYearService.findCurrentAndFuturePlanYearsBy(company.getCode(),company.getQuater())).thenReturn(planYearDetails);
		when(planCompareDao.findMappingBenefitPlansBy(getFuturePlanYearDetail().get().getRealmYearId(),
				getCurrentPlanYearDetail().get().getRealmYearId())).thenReturn(mappedPlanDetails);
		List<MappedPlanDetailDto> companyYearPlans = planCompareService.findMappingBenefitPlansBy(company.getCode());

		assertNotNull(companyYearPlans);
		assertEquals(1, companyYearPlans.size());
		assertEquals("1001",getBenefitPlanDetails().get().getPlanId());
		assertEquals("Medical",getBenefitPlanDetails().get().getOfferType());
	}
	
	@Test
	public void findAllFutureYearPlansByTest() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setDescription("Test Company Inc");
		company.setQuater(QUARTERNAME);
		company.setTransitionPeriod(Boolean.TRUE);
		company.setRenewalOpen(Boolean.TRUE);
		
		List<BenefitPlanDetailDto> benefitPlanDetail = new ArrayList<>();
		benefitPlanDetail.add(getBenefitPlanDetails().get());
		benefitPlanDetail.add(getBenefitPlanDetails().get());
		List<PlanYearDetailDto> planYearDetails = new ArrayList<>();
		planYearDetails.add(getCurrentPlanYearDetail().get());
		planYearDetails.add(getFuturePlanYearDetail().get());
		
		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, null)).thenReturn(company);
		when(realmPlanYearService.findCurrentAndFuturePlanYearsBy(company.getCode(),company.getQuater())).thenReturn(planYearDetails);
		when(planCompareDao.findAllFutureYearPlansBy(getFuturePlanYearDetail().get().getRealmYearId())).thenReturn(benefitPlanDetail);
		List<BenefitPlanDetailDto> futureYearPlans = planCompareService.findAllFutureYearPlansBy(company.getCode());

		assertNotNull(futureYearPlans);
		assertEquals(2, futureYearPlans.size());
		assertEquals("1001",getBenefitPlanDetails().get().getPlanId());
		assertEquals("Medical",getBenefitPlanDetails().get().getOfferType());
	}

	private Supplier<BenefitPlanDetailDto> getBenefitPlanDetails(){
		return () -> {
			BenefitPlanDetailDto currentYearPlan = new BenefitPlanDetailDto();
			currentYearPlan.setOfferType("Medical");
			currentYearPlan.setPlanId("1001");
			currentYearPlan.setPlanName("PPO");
			return currentYearPlan;
		};
	}
	
	private Supplier<MappedPlanDetailDto> getMappedPlanDetails(){
		return () -> {
			MappedPlanDetailDto mappedPlans = new MappedPlanDetailDto();
			mappedPlans.setOfferType("Medical");
			mappedPlans.setPlanId("1001");
			mappedPlans.setPlanName("PPO");
			mappedPlans.setParentId("1000");
			return mappedPlans;
		};
	}

	private Supplier<PlanYearDetailDto> getCurrentPlanYearDetail(){
		return () -> {
			PlanYearDetailDto plan = new PlanYearDetailDto();
			plan.setPlanYear(PlanCompareConstants.CURRENT.getAction());
			plan.setRealmYearId("56");
			return plan;
		};
	}
	
	private Supplier<PlanYearDetailDto> getFuturePlanYearDetail(){
		return () -> {
			PlanYearDetailDto plan = new PlanYearDetailDto();
			plan.setPlanYear(PlanCompareConstants.FUTURE.getAction());
			plan.setRealmYearId("66");
			return plan;
		};
	}

}
