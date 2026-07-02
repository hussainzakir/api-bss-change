package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.helper.BenefitCategoriesHelper;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.HrisPlanAttributeService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.impl.PortfolioServiceImpl;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author schaudhari
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PortfolioServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	PortfolioServiceImpl portfolioService;

	@Mock
	private RealmDataDao realmDataDao;

	@Mock
	RealmPlanYearRuleService realmPlanYearRuleService;

	@Mock
	private PortfolioRuleDao portfolioRuleDao;

	@Mock
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	@Mock
	HrisPlanAttributeService hrisPlanAttributeService;

	@Mock
	StrategyDao strategyDao;

    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        benefitCategoriesHelperMockedStatic = Mockito.mockStatic(BenefitCategoriesHelper.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        benefitCategoriesHelperMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void findPrimaryPlanCarriers() throws Exception {
		Company company = new Company();
		company.setCode( "6PR" );
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(1234L);
		company.setRealmPlanYear( rpy );
		company.setHeadQuatersState("CA");
		company.setZipCode("22787");
		company.setExclusiveMedPlan( "DFLT");
		company.setPlanStartDate("01/01/2020");
		company.setRenewalCompany(true);
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( eq( company ) ) ).thenReturn( false );
		
		Map<String, Set<PlanCarrier>> actualResult = portfolioService.findPrimaryPlanCarriers(company);

		assertEquals(planCarrierMap, actualResult);
	}

	@Test
	public void getTibPlanCarriersForCompanyTest() throws Exception {

		Company company = new Company();
		Strategy strategy1 = new Strategy();
		strategy1.setId(1234L);

		Strategy strategy2 = new Strategy();
		strategy2.setId(5678L);

		List<PlanSelection> planSelections = preparePlanSelections();
		Set<String> planIds = planSelections.stream()
				.map(PlanSelection::getBenefitPlan)
				.collect(Collectors.toSet());

		when(strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(List.of(strategy1, strategy2));
		when(strategyGroupPlanSelectDao.findByStrategyIdAndPlanType(anyLong(), eq(BSSApplicationConstants.MEDICAL_PLAN_TYPE))).thenReturn(planSelections);
		when(hrisPlanAttributeService.getPlanAttributesByBenefitType(planIds, BSSApplicationConstants.MEDICAL)).thenReturn(preparePlanAttributes());

		Set<PlanCarrier> actualResult = portfolioService.getOmsPlanCarriersForCompanyAndPlanType(company, BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		verify(strategyDao, times(1)).findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
		verify(strategyGroupPlanSelectDao, times(2)).findByStrategyIdAndPlanType(anyLong(), eq(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		verify(hrisPlanAttributeService, times(2)).getPlanAttributesByBenefitType((Set<String>) planIds, BSSApplicationConstants.MEDICAL);

		assertEquals(2, actualResult.size());
		assertEquals(1, actualResult.stream().filter(planCarrier -> planCarrier.getId() == 1).count());
		assertEquals(1, actualResult.stream().filter(planCarrier -> planCarrier.getId() == 2).count());
	}

	@Test
	public void getTibPlanCarriersForStrategyIdTest() throws Exception {

		long strategyId = 1234L;
		List<PlanSelection> planSelections = preparePlanSelections();
		Set<String> planIds = planSelections.stream()
				.map(PlanSelection::getBenefitPlan)
				.collect(Collectors.toSet());

		when(strategyGroupPlanSelectDao.findByStrategyIdAndPlanType(strategyId, BSSApplicationConstants.MEDICAL_PLAN_TYPE)).thenReturn(planSelections);
		when(hrisPlanAttributeService.getPlanAttributesByBenefitType(planIds, BSSApplicationConstants.MEDICAL)).thenReturn(preparePlanAttributes());

		Set<PlanCarrier> actualResult = portfolioService.getOmsPlanCarriersForStrategyIdAndPlanType(strategyId, BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		verify(strategyGroupPlanSelectDao, times(1)).findByStrategyIdAndPlanType(anyLong(), eq(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		verify(hrisPlanAttributeService, times(1)).getPlanAttributesByBenefitType(planIds, BSSApplicationConstants.MEDICAL);
		assertEquals(2, actualResult.size());
		assertEquals(1, actualResult.stream().filter(planCarrier -> planCarrier.getId() == 1).count());
		assertEquals(1, actualResult.stream().filter(planCarrier -> planCarrier.getId() == 2).count());
	}

	private List<PlanSelection> preparePlanSelections() {
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection planSelection = new PlanSelection();
		planSelection.setBenefitPlan("1234");
		planSelections.add(planSelection);

		planSelection = new PlanSelection();
		planSelection.setBenefitPlan("5678");
		planSelections.add(planSelection);

		planSelection = new PlanSelection();
		planSelection.setBenefitPlan("9012");
		planSelections.add(planSelection);

		return planSelections;
	}
	private CompletableFuture<List<BenefitPlanCompare>> preparePlanAttributes() {
		List<BenefitPlanCompare> planCompares = List.of(
				BenefitPlanCompare.builder().carrierId(1).carrier("Carrier 1").build(),
				BenefitPlanCompare.builder().carrierId(2).carrier("Carrier 2").build(),
				BenefitPlanCompare.builder().carrierId(1).carrier("Carrier 1").build()
		);
		return CompletableFuture.completedFuture(planCompares);
	}
}
