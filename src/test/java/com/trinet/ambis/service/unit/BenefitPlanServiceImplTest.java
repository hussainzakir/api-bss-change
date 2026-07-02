package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.CommonServiceHelper;

import java.math.BigDecimal;
import java.util.*;

import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.impl.BenefitPlanServiceImpl;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.StateBenefitPlan;

@RunWith(MockitoJUnitRunner.class)
public class BenefitPlanServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	BenefitPlanServiceImpl benefitPlanService;

	@Mock
	BenefitPlanDao benefitPlanDao;

	@Mock
	PortfolioService portfolioService;

    @Mock
    PsCompanyDao psCompanyDao;

    @Mock
    PlanRatesService planRatesService;

    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMock;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMock;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMock;
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMock;

    @Before
    public void setUp() {
        benefitCategoriesHelperMock = Mockito.mockStatic(BenefitCategoriesHelper.class);
        commonServiceHelperMock = Mockito.mockStatic(CommonServiceHelper.class);
        rulesAndConfigsUtilsMock = org.mockito.Mockito.mockStatic(RulesAndConfigsUtils.class);
        appRulesAndConfigsUtilsMock = org.mockito.Mockito.mockStatic(AppRulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        benefitCategoriesHelperMock.close();
        commonServiceHelperMock.close();
        rulesAndConfigsUtilsMock.close();
        appRulesAndConfigsUtilsMock.close();
    }


    @Test
	public void getAllPrimaryBenefitPlansForPlanRates() {
		Company company = new Company();
		Map<String, Set<PlanCarrier>> planCarriers = new HashMap<>();
		Set<String> plansPortfolios = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		Map<String, Set<StateBenefitPlan>> benefitPlansByPlanTypes = new HashMap<>();
		Set<String> benPlans = new HashSet<>(Arrays.asList("PLAN1", "PLAN2"));

		when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarriers);
		when(BenefitCategoriesHelper.getPlanCarriers(planCarriers)).thenReturn(plansPortfolios);
		when(benefitPlanDao.getAllPrimaryBenefitPlans(plansPortfolios, company, outOfRegionPlans))
				.thenReturn(benefitPlansByPlanTypes);
		when(BenefitCategoriesHelper.getAllBenefitPlans(benefitPlansByPlanTypes)).thenReturn(benPlans);

		Set<String> actualResult = benefitPlanService.getAllPrimaryBenefitPlansForPlanRates(company);

		verify(benefitPlanDao, times(1)).getAllPrimaryBenefitPlans(plansPortfolios, company, outOfRegionPlans);
		assertEquals(benPlans, actualResult);
	}

    @Test
    public void getLowestCostPlanPerCarrier_NotHQFundingType() {
        Company company = prepareCompany();
        when(RulesAndConfigsUtils.getMinFundingType(company.getRealmPlanYearId()))
                .thenReturn(null);
        List<CarrierMinimumFunding> result1 = benefitPlanService.getLowestCostPlanPerCarrier(
                company);
        verify(psCompanyDao, times(0)).getLowestCostPlanPerPlanCarrier(company);
        assertNull(result1);
    }

    @Test
    public void getLowestCostPlanPerCarrier_FeatureEnabled() {
        Company company = prepareCompany();

        when(RulesAndConfigsUtils.getMinFundingType(company.getRealmPlanYearId()))
                .thenReturn("HQ");
        when(AppRulesAndConfigsUtils.isLowestCostPlanPerCarrierV2Enabled())
                .thenReturn(true);

        when(benefitPlanDao.getPortfolioPlansByPlanTypeForState(anyString(), anyLong(), anySet()))
                .thenReturn(preparePortfolioPlansByPlanType());
        Mockito.when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(preparePlanCost());
        List<CarrierMinimumFunding> expectedList2 = new ArrayList<>();
        expectedList2.add(new CarrierMinimumFunding(1002L, BSSApplicationConstants.MEDICAL_PLAN_TYPE, BigDecimal.valueOf(800.00)));
        expectedList2.add(new CarrierMinimumFunding(1001L, BSSApplicationConstants.MEDICAL_PLAN_TYPE, BigDecimal.valueOf(1000.00)));
        expectedList2.add(new CarrierMinimumFunding(2001L, BSSApplicationConstants.DENTAL_PLAN_TYPE, BigDecimal.valueOf(140.00)));
        expectedList2.add(new CarrierMinimumFunding(3002L, BSSApplicationConstants.VISION_PLAN_TYPE, BigDecimal.valueOf(10.00)));

        List<CarrierMinimumFunding> result = benefitPlanService.getLowestCostPlanPerCarrier(
                company);
        verify(psCompanyDao, times(0)).getLowestCostPlanPerPlanCarrier(company);
        verify(benefitPlanDao, times(1)).getPortfolioPlansByPlanTypeForState(anyString(), anyLong(), anySet());
        verify(planRatesService, times(1)).getBenefitPlanRatesBy(company);
        assertEquals(4, result.size());
        assertTrue(result.containsAll(expectedList2));
    }

    @Test
    public void getLowestCostPlanPerCarrier_FeatureNotEnabled() {
        Company company = prepareCompany();
        List<CarrierMinimumFunding> expectedList3 = new ArrayList<>();
        expectedList3.add(new CarrierMinimumFunding(1L, "PLAN1", null));
        when(RulesAndConfigsUtils.getMinFundingType(company.getRealmPlanYearId()))
                .thenReturn("HQ");
        when(AppRulesAndConfigsUtils.isLowestCostPlanPerCarrierV2Enabled())
                .thenReturn(false);
        when(psCompanyDao.getLowestCostPlanPerPlanCarrier(company))
                .thenReturn(expectedList3);
        List<CarrierMinimumFunding> result3 = benefitPlanService.getLowestCostPlanPerCarrier(
                company);
        verify(psCompanyDao, times(1)).getLowestCostPlanPerPlanCarrier(company);
        assertEquals(1, result3.size());
        assertEquals(expectedList3, result3);

    }

    private BenefitPlanRate prepareBenPlanRate(String bandCode, String benPlan, String planType, BigDecimal employerCost) {
        BenefitPlanRate planRate = new BenefitPlanRate();
        planRate.setBandCode(bandCode);
        planRate.setBenefitPlan(benPlan);
        planRate.setCostId(BigDecimal.valueOf(1));
        planRate.setCoverageCode("1");
        planRate.setEmployerCost(employerCost);
        planRate.setOptionId(BigDecimal.valueOf(1));
        java.util.Date effDt = new Date();
        planRate.setEffDt(effDt);
        planRate.setPlanType(planType);
        return planRate;
    }

    private Map<String, List<BenefitPlanRate>> preparePlanCost() {
        Map<String, List<BenefitPlanRate>> planCosts = new HashMap<>();

        List<BenefitPlanRate> planRates = new ArrayList<>();
        BenefitPlanRate planRate = prepareBenPlanRate("BANDCODE1", "MEDPLAN1", "medical", BigDecimal.valueOf(1400.00));
        planRates.add(planRate);
        planCosts.put("MEDPLAN1", planRates);

        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE1", "MEDPLAN2", "medical", BigDecimal.valueOf(1000.00));
        planRates.add(planRate);
        planCosts.put("MEDPLAN2", planRates);

        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE1", "MEDPLAN3", "medical", BigDecimal.valueOf(800.00));
        planRates.add(planRate);
        planCosts.put("MEDPLAN3", planRates);

        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE1", "DENPLAN1", "dental", BigDecimal.valueOf(140.00));
        planRates.add(planRate);
        planCosts.put("DENPLAN1", planRates);

        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE1", "DENPLAN2", "dental", BigDecimal.valueOf(160.00));
        planRates.add(planRate);
        planCosts.put("DENPLAN2", planRates);

        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE1", "DENPLAN3", "dental", BigDecimal.valueOf(180.00));
        planRates.add(planRate);
        planCosts.put("DENPLAN3", planRates);

        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE1", "VISPLAN1", "vision", BigDecimal.valueOf(14.00));
        planRates.add(planRate);
        planCosts.put("VISPLAN1", planRates);

        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE1", "VISPLAN2", "vision", BigDecimal.valueOf(10.00));
        planRates.add(planRate);
        planCosts.put("VISPLAN2", planRates);

        // This plan should be filter out as it is not in PortfolioPlansByPlanType
        planRates = new ArrayList<>();
        planRate = prepareBenPlanRate("BANDCODE100", "VISPLAN100", "vision", BigDecimal.valueOf(20.00));
        planRates.add(planRate);
        planCosts.put("VISPLAN100", planRates);


        return planCosts;
    }

    private Map<String, Map<Long, Set<String>>> preparePortfolioPlansByPlanType() {
        Map<String, Map<Long, Set<String>>> portfolioPlans = new HashMap<>();

        // HMO plan type
        Map<Long, Set<String>> medicalPortfolios = new HashMap<>();
        medicalPortfolios.put(1001L, new HashSet<>(Arrays.asList("MEDPLAN1", "MEDPLAN2")));
        medicalPortfolios.put(1002L, new HashSet<>(Collections.singleton("MEDPLAN3")));
        portfolioPlans.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, medicalPortfolios);

        // PPO plan type
        Map<Long, Set<String>> dentalPortfolios = new HashMap<>();
        dentalPortfolios.put(2001L, new HashSet<>(Arrays.asList("DENPLAN1", "DENPLAN2")));
        dentalPortfolios.put(2002L, new HashSet<>(Collections.singleton("DENPLAN3")));
        portfolioPlans.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, dentalPortfolios);


        // Vision plan type
        Map<Long, Set<String>> visionPortfolios = new HashMap<>();
        visionPortfolios.put(3001L, new HashSet<>(Collections.singleton("VISPLAN1")));
        visionPortfolios.put(3002L, new HashSet<>(Collections.singleton("VISPLAN2")));
        portfolioPlans.put(BSSApplicationConstants.VISION_PLAN_TYPE, visionPortfolios);

        return portfolioPlans;
    }

    private Company prepareCompany() {
        Company company = new Company();
        company.setId(1L);
        company.setRealmPlanYearId(1L);
        company.setHeadQuatersState("CA");
        return company;
    }

}