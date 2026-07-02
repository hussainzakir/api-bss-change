/**
 * 
 */
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.common.PlanOfferingReportConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.impl.BenefitPlanDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EligiblePlanData;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.Carrier;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.unit.ServiceUnitTest;

/**
 * @author schaudhari
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BenefitPlanDaoImplTest extends ServiceUnitTest {

	BenefitPlanDaoImpl benefitPlanDao;

	EntityManager hrpEm = null;

	private Query mockedQuery = null;

	private Company company;
	private Set<String> regions;
    private MockedStatic<StrategyServiceHelper> mockStaticStrategyServiceHelper;
    private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;

	@Before
	public void setup() {
        mockStaticStrategyServiceHelper = Mockito.mockStatic(StrategyServiceHelper.class);
        mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);
        mockStaticRulesAndConfigsUtils.when(() -> RulesAndConfigsUtils.findPickChooseWithExceptions(company))
                .thenReturn(Boolean.TRUE);

        hrpEm = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		Query mockedQueryAutoSelect = mock(Query.class);
		benefitPlanDao = new BenefitPlanDaoImpl();
		benefitPlanDao.setHrpEm(hrpEm);
		when(hrpEm.createNamedQuery(Mockito.anyString())).thenReturn(mockedQuery);

		when(hrpEm.createNamedQuery("autoSelectPlansByRealm")).thenReturn(mockedQueryAutoSelect);
		when(mockedQueryAutoSelect.getResultList()).thenReturn(prepareAutoSelectPlansByRealm());

		regions = new HashSet<>(Arrays.asList("CA", "MA"));

		company = new Company();
		company.setId(1L);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setPlanYearEnd(new Date());
		company.setCode("XYZ");
		company.setRealmPlanYear(realmPlanYear);
		Realm realm = new Realm();
		realm.setId(48);
		company.setRealm(realm);
		
	}

    @After
    public void tearDown() {
        if (mockStaticStrategyServiceHelper != null) mockStaticStrategyServiceHelper.close();
        if (mockStaticRulesAndConfigsUtils != null) mockStaticRulesAndConfigsUtils.close();
    }

//	 Texas situs - true
//	 outOfRegionPlans is empty
	@Test
	public void getPrimaryBenefitPlansByRegions1() {
		mock(regions.getClass());
		when(StrategyServiceHelper.getLocations(company)).thenReturn(regions);
		
		Set<String> plansPortfoliosList = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		company.setTexasSitus(true);
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			Map<String, Set<StateBenefitPlan>> actualResult = benefitPlanDao
					.getAllPrimaryBenefitPlans(plansPortfoliosList, company, outOfRegionPlans);

			assertEquals(3, actualResult.size());
			assertEquals(2, actualResult.get(BSSApplicationConstants.MEDICAL).size());
			assertEquals(2, actualResult.get(BSSApplicationConstants.DENTAL).size());
			assertEquals(2, actualResult.get(BSSApplicationConstants.VISION).size());
		}
	}

//	 Texas situs - false
//	 outOfRegionPlans is not empty
	@Test
	public void getPrimaryBenefitPlansByRegions2() {
		when(StrategyServiceHelper.getLocations(company)).thenReturn(regions);
		Set<String> plansPortfoliosList = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		outOfRegionPlans.add("002MNQ");
		company.setTexasSitus(false);
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
		company.setRealm(realm);

		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			Map<String, Set<StateBenefitPlan>> actualResult = benefitPlanDao
					.getAllPrimaryBenefitPlans(plansPortfoliosList, company, outOfRegionPlans);

			assertEquals(3, actualResult.size());
			assertEquals(2, actualResult.get(BSSApplicationConstants.MEDICAL).size());
			assertEquals(2, actualResult.get(BSSApplicationConstants.DENTAL).size());
			assertEquals(2, actualResult.get(BSSApplicationConstants.VISION).size());

			for (StateBenefitPlan medPlan : actualResult.get(BSSApplicationConstants.MEDICAL)) {
				assertTrue(Arrays.asList("0021D8", "002ALJ").contains(medPlan.getBenefitPlan()));
				assertFalse(medPlan.isTexasSitus());
			}
			for (StateBenefitPlan medPlan : actualResult.get(BSSApplicationConstants.DENTAL)) {
				assertTrue(Arrays.asList("005B5I", "005B4R").contains(medPlan.getBenefitPlan()));
				if ("005B4R".equals(medPlan.getBenefitPlan())) {
					assertTrue(medPlan.isTexasSitus());
				} else {
					assertFalse(medPlan.isTexasSitus());
				}
			}
			for (StateBenefitPlan medPlan : actualResult.get(BSSApplicationConstants.VISION)) {
				assertTrue(Arrays.asList("002LXT", "002LY6").contains(medPlan.getBenefitPlan()));
				assertFalse(medPlan.isTexasSitus());
			}
		}
	}

//	 Texas situs - true
//	 outOfRegionPlans is empty
	@Test
	public void getAllPrimaryBenefitPlans1() {
		Set<String> plansPortfolios = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		company.setTexasSitus(true);
		company.setBundleId(null);

		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			Map<String, Set<StateBenefitPlan>> actualResult = benefitPlanDao.getAllPrimaryBenefitPlans(plansPortfolios,
					company, outOfRegionPlans);

			assertEquals(3, actualResult.size());
			verify(mockedQuery, times(1)).setParameter("situs", "TX");
			verify(mockedQuery, times(1)).setParameter("planYearId", Long.valueOf(0));
			verify(mockedQuery, times(1)).setParameter("portfolios", plansPortfolios);
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, 0);
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.BUNDLE_IDS, Set.of());
			verify(mockedQuery, times(1)).setParameter("outOfRegionPlans",
					new HashSet<String>(Arrays.asList("PLANTOEXCLUDE")));
		}
	}

//	 Texas situs - false
//	 outOfRegionPlans is not empty
	@Test
	public void getAllPrimaryBenefitPlans2() {
		Set<String> plansPortfolios = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		outOfRegionPlans.add("002MNQ");
		company.setTexasSitus(false);
		company.setBundleId(null);

		when(hrpEm.createNamedQuery("ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS")).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			Map<String, Set<StateBenefitPlan>> actualResult = benefitPlanDao.getAllPrimaryBenefitPlans(plansPortfolios,
					company, outOfRegionPlans);

			assertEquals(3, actualResult.size());
			verify(mockedQuery, times(1)).setParameter("situs", "FL");
			verify(mockedQuery, times(1)).setParameter("planYearId", Long.valueOf(0));
			verify(mockedQuery, times(1)).setParameter("portfolios", plansPortfolios);
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, 0);
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.BUNDLE_IDS, Set.of());
			verify(mockedQuery, times(1)).setParameter("outOfRegionPlans", new HashSet<String>(Arrays.asList("002MNQ")));
		}
	}

	@Test
	public void getWidelyAvailablePlansTest() {
		Set<String> plans = new HashSet<>();

		when(mockedQuery.getResultList()).thenReturn(prepareWidelyAvaiablePlanMockData());

		Set<String> actualResults = benefitPlanDao.getWidelyAvailablePlans(plans, 1L);
		assertEquals(2, actualResults.size());
		assertEquals(true, actualResults.contains("WIDE_PLAN_1"));
		assertEquals(true, actualResults.contains("WIDE_PLAN_2"));

		plans.add("WIDE_PLAN_1");
		actualResults = benefitPlanDao.getWidelyAvailablePlans(plans, 1L);
		assertEquals(2, actualResults.size());
		assertEquals(true, actualResults.contains("WIDE_PLAN_1"));
		assertEquals(true, actualResults.contains("WIDE_PLAN_2"));
		verify(mockedQuery, times(1)).setParameter(eq("benefitPlanList"), anyList());

		plans = null;
		actualResults = benefitPlanDao.getWidelyAvailablePlans(plans, 1L);
		assertEquals(2, actualResults.size());
		assertEquals(true, actualResults.contains("WIDE_PLAN_1"));
		assertEquals(true, actualResults.contains("WIDE_PLAN_2"));

		verify(mockedQuery, times(3)).setParameter(BSSQueryConstants.PLAN_TYPE,
				BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		verify(mockedQuery, times(3)).setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, 1L);
	}

	@Test
	public void getRegionalBasePlanMapping() {
		RealmPlanYear rpy = new RealmPlanYear();
		when(mockedQuery.getResultList()).thenReturn(prepareRegionalBasePlanMapping());

		Map<String, BenefitPlan> result = benefitPlanDao.getRegionalBasePlanMapping(rpy);

		assertEquals(3, result.size());
		assertEquals("BASEPLAN1", result.get("REGPLAN1").getPlanId());
		assertEquals("Aetna PPO 2000", result.get("REGPLAN1").getDescr());
		assertEquals("BASEPLAN1", result.get("REGPLAN2").getPlanId());
		assertEquals("Aetna PPO 2000", result.get("REGPLAN2").getDescr());
		assertEquals("BASEPLAN2", result.get("REGPLAN3").getPlanId());
		assertEquals("Aetna PPO 1500", result.get("REGPLAN3").getDescr());
	}

	@Test
	public void getSelectedRegionalPlansForBasePlanTest() {

		Long strategyId = 111L;
		Long groupId = 222L;
		Long realmPlanYearId = 70L;
		String basePlanId = "BASE_PLAN_ID";

		when(mockedQuery.getResultList()).thenReturn(prepareSelectedRegionalPlansForBasePlan());

		List<String> actualResults = benefitPlanDao.getSelectedRegionalPlansForBasePlan(strategyId, groupId,
				realmPlanYearId, basePlanId);
		assertEquals(2, actualResults.size());
		assertEquals(true, actualResults.contains("REGIONAL_PLAN_1"));
		assertEquals(true, actualResults.contains("REGIONAL_PLAN_2"));

		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.GROUP_ID, groupId);
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		verify(mockedQuery, times(1)).setParameter("basePlanId", basePlanId);
	}

	@Test
	public void getPlansForAppendix() {
		String strategyId = "STRATEGY_ID";
		List<String> regions = Arrays.asList("CA", "NY", "CA-S");
		List<String> mdPlanTypes = Arrays.asList("10", "11", "1D");
		List<String> visionPlanTypes = BSSApplicationConstants.VISION_PLAN_TYPES;
		boolean filterSubregions = true;

		when(mockedQuery.getResultList()).thenReturn(preparePlansForAppendix());

		List<PlanAppendixBenefitPlanData> actualResults = benefitPlanDao.getPlansForAppendix(company, strategyId, regions, mdPlanTypes, visionPlanTypes, filterSubregions);
		assertEquals(3, actualResults.size());
		assertEquals("MED_PLAN_1", actualResults.get(0).getBenefitPlan());
		assertEquals("MED_PLAN_2", actualResults.get(1).getBenefitPlan());
		assertEquals("DEN_PLAN_1", actualResults.get(2).getBenefitPlan());

		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.MD_PLAN_TYPES, mdPlanTypes);
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.VISION_PLAN_TYPES, visionPlanTypes);
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearEnd());
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYearId());
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.REGIONS, regions);
		verify(mockedQuery, times(1)).setParameter("filterSubregions", filterSubregions ? 1 : 0);

	}

	@Test
	public void getBenefitsPlanOfferingsBy() {
		PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();

		when(mockedQuery.getResultList()).thenReturn(preparePlanOfferings());

		List<PlanOfferingsBenefitPlanData> actualResults = benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, 11L, null, false);
		assertEquals(3, actualResults.size());
		assertEquals("MED_PLAN_1", actualResults.get(0).getBenefitPlan());
		assertEquals("MED_PLAN_2", actualResults.get(1).getBenefitPlan());
		assertEquals("DEN_PLAN_1", actualResults.get(2).getBenefitPlan());

		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.PLAN_OFFER_REALM_YEAR_ID, 11L);
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.PICK_CHOOSE_FLAG, 0);
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.HQ_REGION, planOfferingsRequest.getHqState());
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.PRIM_PORTFOLIO, planOfferingsRequest.getCarriers().stream().map(Carrier::getId).collect(Collectors.toList()));
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.REGIONS, planOfferingsRequest.getRegions());
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, Set.of("DUMMY"));
		verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.PLAN_TYPES, planOfferingsRequest.getBenefitTypes());
	}
	
	@Test
	public void getBenefitsPlanOfferingsBy_Exchange() {

		PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
		planOfferingsRequest.setReportCode("POEX");
		Long realmYearId = 73L;

		when(mockedQuery.getResultList()).thenReturn(preparePlanOfferings());

		List<PlanOfferingsBenefitPlanData> actualResults = benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, realmYearId, null, false);
		assertEquals(3, actualResults.size());
		assertEquals("MED_PLAN_1", actualResults.get(0).getBenefitPlan());
		assertEquals("MED_PLAN_2", actualResults.get(1).getBenefitPlan());

	}

	/**
	 * Given: isBundleV2Enabled returns true and report code is POEX
	 * When: getBenefitsPlanOfferingsBy is called
	 * Then: The V2 named query is used for exchange plans
	 */
	@Test
	public void getBenefitsPlanOfferingsBy_Exchange_V2Enabled_usesV2Query() {
		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

			PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
			planOfferingsRequest.setReportCode("POEX");
			Long realmYearId = 73L;

			Query v2Query = mock(Query.class);
			when(hrpEm.createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS_V2))
					.thenReturn(v2Query);
			when(v2Query.getResultList()).thenReturn(preparePlanOfferings());

			List<PlanOfferingsBenefitPlanData> actualResults = benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, realmYearId, null, false);

			assertEquals(3, actualResults.size());
			assertEquals("MED_PLAN_1", actualResults.get(0).getBenefitPlan());
			assertEquals("MED_PLAN_2", actualResults.get(1).getBenefitPlan());
			assertEquals("DEN_PLAN_1", actualResults.get(2).getBenefitPlan());
			verify(hrpEm, times(1)).createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS_V2);
		}
	}

	/**
	 * Given: isBundleV2Enabled returns false and report code is POEX
	 * When: getBenefitsPlanOfferingsBy is called
	 * Then: The non-V2 named query is used for exchange plans
	 */
	@Test
	public void getBenefitsPlanOfferingsBy_Exchange_V2Disabled_usesNonV2Query() {
		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
			planOfferingsRequest.setReportCode("POEX");
			Long realmYearId = 73L;

			when(mockedQuery.getResultList()).thenReturn(preparePlanOfferings());

			List<PlanOfferingsBenefitPlanData> actualResults = benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, realmYearId, null, false);

			assertEquals(3, actualResults.size());
			verify(hrpEm, times(1)).createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS);
		}
	}

	/**
	 * Given: isBundleV2Enabled returns true, report code is POEX, and outOfRegionPlans is null
	 * When: getBenefitsPlanOfferingsBy is called
	 * Then: DUMMY is used for outOfRegionPlans and V2 query is used
	 */
	@Test
	public void getBenefitsPlanOfferingsBy_Exchange_V2Enabled_nullOutOfRegionPlans() {
		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

			PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
			planOfferingsRequest.setReportCode("POEX");
			Long realmYearId = 73L;

			Query v2Query = mock(Query.class);
			when(hrpEm.createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS_V2))
					.thenReturn(v2Query);
			when(v2Query.getResultList()).thenReturn(preparePlanOfferings());

			List<PlanOfferingsBenefitPlanData> actualResults = benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, realmYearId, null, false);

			assertEquals(3, actualResults.size());
			verify(v2Query, times(1)).setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, Set.of(BSSApplicationConstants.DUMMY));
			verify(hrpEm, times(1)).createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS_V2);
		}
	}

	/**
	 * Given: isBundleV2Enabled returns true, report code is POEX, and DAO returns empty list
	 * When: getBenefitsPlanOfferingsBy is called
	 * Then: Empty list is returned using V2 query
	 */
	@Test
	public void getBenefitsPlanOfferingsBy_Exchange_V2Enabled_emptyResults() {
		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

			PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
			planOfferingsRequest.setReportCode("POEX");
			Long realmYearId = 73L;

			Query v2Query = mock(Query.class);
			when(hrpEm.createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS_V2))
					.thenReturn(v2Query);
			when(v2Query.getResultList()).thenReturn(new ArrayList<>());

			List<PlanOfferingsBenefitPlanData> actualResults = benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, realmYearId, null, false);

			assertTrue(actualResults.isEmpty());
			verify(hrpEm, times(1)).createNamedQuery(PlanOfferingReportConstants.PLAN_OFFERINGS_REPORT_EXCHANGE_GET_APPLICABLE_PLANS_V2);
		}
	}
	
	@Test
	public void getBenefitsPlanOfferingsByTest1() {
		// given
		// data
		PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
		planOfferingsRequest.setReportCode(PlanOfferingReportConstants.REPORT_CODE_WSE);
		Long realmYearId = 73L;
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(preparePlanOfferings1());
		// when
		List<PlanOfferingsBenefitPlanData> actualResults = benefitPlanDao
				.getBenefitsPlanOfferingsBy(planOfferingsRequest, realmYearId, null, false);
		// then
		// assertion
		assertEquals(3, actualResults.size());
		assertEquals("MED_PLAN_1", actualResults.get(0).getBenefitPlan());
		assertEquals("10", actualResults.get(0).getPlanType());
		assertEquals("Medical Plan 1", actualResults.get(0).getDescription());
		assertEquals("MED_PLAN_2", actualResults.get(1).getBenefitPlan());
		assertEquals("10", actualResults.get(1).getPlanType());
		assertEquals("Medical Plan 2", actualResults.get(1).getDescription());
		assertEquals("DEN_PLAN_1", actualResults.get(2).getBenefitPlan());
		assertEquals("11", actualResults.get(2).getPlanType());
		assertEquals("Dental Plan 1", actualResults.get(2).getDescription());
	}

	@Test
	public void getCompanyPlanSelectionsForPlanOfferingReport() {
		// given
		// data
		PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
		planOfferingsRequest.setReportCode(PlanOfferingReportConstants.REPORT_CODE_WSE);
		Long realmYearId = 73L;
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(prepareCompanyPlanSelectionResults());
		// when
		Map<String, List<String>> actualResults = benefitPlanDao
				.getCompanyPlanSelectionsForPlanOfferingReport(planOfferingsRequest, realmYearId);
		// then
		// assertion
		assertEquals(2, actualResults.size());
		assertEquals(2, actualResults.get("10").size());
		assertEquals(1, actualResults.get("11").size());
	}
	
	@Test
	public void getStrategyGroupEstimateByPlanTypeTest() {
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(prepareBenefitPlanWithCarrierByPlanType());
		// when
		Map<String, EligiblePlanData> actualResult = benefitPlanDao.getBenefitPlansAndCarriersBy(1234L, 02345L, "10",
				new Date(), "1");
		// then
		assertEquals(2, actualResult.size());
	}

    @Test
    public void getPortfolioPlansByPlanTypeForStateTest() {
        // method mocks
        when(mockedQuery.getResultList()).thenReturn(preparePortfolioPlansByPlanType());
        // when
        Map<String, Map<Long, Set<String>>> actualResult = benefitPlanDao.getPortfolioPlansByPlanTypeForState("CA", 88, new HashSet<>(Arrays.asList("01HIJK")));
        // then
        assertEquals(3, actualResult.size());
        assertEquals(2, actualResult.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).size());
        assertEquals(2, actualResult.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).get(1L).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).get(2L).size());
        assertEquals(2, actualResult.get(BSSApplicationConstants.DENTAL_PLAN_TYPE).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.DENTAL_PLAN_TYPE).get(1L).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.DENTAL_PLAN_TYPE).get(2L).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.VISION_PLAN_TYPE).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.VISION_PLAN_TYPE).get(15L).size());
    }

	@Test
	public void getAllExchangeAndBundlesPlans_gaBundleEnabled_returnsBothBundleAndExchangePlans() {
		Company company = buildCompanyWithBundle(true, null);
		Set<String> plansPortfolios = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		Set<Long> bundleIds = Set.of(99L);

		mockStaticRulesAndConfigsUtils.when(() -> RulesAndConfigsUtils.isGaBundleEnabled(company.getRealmPlanYear().getId()))
				.thenReturn(true);
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			List<Object[]> actualResult = benefitPlanDao.getAllExchangeAndBundlesPlans(company, plansPortfolios, outOfRegionPlans, bundleIds);

			assertEquals(12, actualResult.size());
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, 1);
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, 0);
		}
	}

	@Test
	public void getAllExchangeAndBundlesPlans_companyHasBundleId_returnsBundlePlans() {
		Company company = buildCompanyWithBundle(false, 77L);
		Set<String> plansPortfolios = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		Set<Long> bundleIds = new HashSet<>();

		mockStaticRulesAndConfigsUtils.when(() -> RulesAndConfigsUtils.isGaBundleEnabled(company.getRealmPlanYear().getId()))
				.thenReturn(false);
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			List<Object[]> actualResult = benefitPlanDao.getAllExchangeAndBundlesPlans(company, plansPortfolios, outOfRegionPlans, bundleIds);

			assertEquals(6, actualResult.size());
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, 1);
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.BUNDLE_IDS, Set.of(77L));
			verify(mockedQuery, never()).setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, 0);
		}
	}

	@Test
	public void getAllExchangeAndBundlesPlans_noBundleIdAndGaDisabled_returnsExchangePlans() {
		Company company = buildCompanyWithBundle(false, null);
		Set<String> plansPortfolios = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		Set<Long> bundleIds = new HashSet<>();

		mockStaticRulesAndConfigsUtils.when(() -> RulesAndConfigsUtils.isGaBundleEnabled(company.getRealmPlanYear().getId()))
				.thenReturn(false);
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(false);

			List<Object[]> actualResult = benefitPlanDao.getAllExchangeAndBundlesPlans(company, plansPortfolios, outOfRegionPlans, bundleIds);

			assertEquals(6, actualResult.size());
			verify(mockedQuery, times(1)).setParameter(BSSQueryConstants.RETURN_ONLY_BUNDLE_PLAN, 0);
		}
	}

	@Test
	public void getAllExchangeAndBundlesPlans_v2Enabled_usesV2Query() {
		Company company = buildCompanyWithBundle(false, null);
		Set<String> plansPortfolios = new HashSet<>();
		Set<String> outOfRegionPlans = new HashSet<>();
		Set<Long> bundleIds = new HashSet<>();

		mockStaticRulesAndConfigsUtils.when(() -> RulesAndConfigsUtils.isGaBundleEnabled(company.getRealmPlanYear().getId()))
				.thenReturn(false);
		when(hrpEm.createNamedQuery("ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS_V2")).thenReturn(mockedQuery);
		when(mockedQuery.getResultList()).thenReturn(prepareBenPlansQueryResult());

		try (MockedStatic<AppRulesAndConfigsUtils> mockAppUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class)) {
			mockAppUtils.when(AppRulesAndConfigsUtils::isBundleV2Enabled).thenReturn(true);

			List<Object[]> actualResult = benefitPlanDao.getAllExchangeAndBundlesPlans(company, plansPortfolios, outOfRegionPlans, bundleIds);

			assertEquals(6, actualResult.size());
			verify(hrpEm, times(1)).createNamedQuery("ALL_PRIMARY_EXCHG_OR_BUNDLE_BENEFIT_PLANS_V2");
		}
	}

	private List<Object[]> prepareRegionalBasePlanMapping() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = "REGPLAN1";
		r[1] = "BASEPLAN1";
		r[2] = "Aetna PPO 2000";
		results.add(r);
		r = new Object[3];
		r[0] = "REGPLAN2";
		r[1] = "BASEPLAN1";
		r[2] = "Aetna PPO 2000";
		results.add(r);
		r = new Object[3];
		r[0] = "REGPLAN3";
		r[1] = "BASEPLAN2";
		r[2] = "Aetna PPO 1500";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareAutoSelectPlansByRealm() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[9];
		r[0] = "0021D8";
		r[1] = "BS-CA HMO 25";
		results.add(r);
		r = new Object[9];
		r[0] = "0021D9";
		r[1] = "BS-CA HMO 25";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareBenPlansQueryResult() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[10];
		r[0] = "0021D8";
		r[1] = "BS-CA PPO 5000 CA South";
		r[2] = "10";
		r[3] = "BLUE";
		r[4] = null;
		r[5] = new BigDecimal(11);
		r[6] = new BigDecimal(18);
		r[7] = null;
		r[8] = null;
		r[9] = "DFLT";
		results.add(r);

		r = new Object[10];
		r[0] = "002ALJ";
		r[1] = "Aetna PPO 3000 South CA";
		r[2] = "10";
		r[3] = "AETNASOI";
		r[4] = null;
		r[5] = new BigDecimal(1);
		r[6] = new BigDecimal(18);
		r[7] = new BigDecimal(0);
		r[8] = null;
		r[9] = "DFLT";
		results.add(r);

		r = new Object[10];
		r[0] = "005B4R";
		r[1] = "Aetna Dental EPP Group NY";
		r[2] = "11";
		r[3] = "AETNASOI";
		r[4] = null;
		r[5] = new BigDecimal(1);
		r[6] = new BigDecimal(18);
		r[7] = new BigDecimal(0);
		r[8] = "TX";
		r[9] = "DFLT";
		results.add(r);

		r = new Object[10];
		r[0] = "005B5I";
		r[1] = "Aetna Dental 100 Opt NV";
		r[2] = "1D";
		r[3] = "AETNASOI";
		r[4] = null;
		r[5] = new BigDecimal(1);
		r[6] = new BigDecimal(18);
		r[7] = new BigDecimal(0);
		r[8] = null;
		r[9] = "DFLT";
		results.add(r);

		r = new Object[10];
		r[0] = "002LXT";
		r[1] = "Aetna EyeMed Plus Group NV/VA";
		r[2] = "14";
		r[3] = "AETNA";
		r[4] = null;
		r[5] = new BigDecimal(15);
		r[6] = new BigDecimal(18);
		r[7] = new BigDecimal(1);
		r[8] = null;
		r[9] = "DFLT";
		results.add(r);

		r = new Object[10];
		r[0] = "002LY6";
		r[1] = "Aetna EyeMed Opt NV/VA";
		r[2] = "1V";
		r[3] = "AETNA";
		r[4] = null;
		r[5] = new BigDecimal(15);
		r[6] = new BigDecimal(18);
		r[7] = new BigDecimal(0);
		r[8] = "FL";
		r[9] = "DFLT";
		results.add(r);
		return results;
	}

	private List<String> prepareWidelyAvaiablePlanMockData() {
		List<String> data = new ArrayList<>();
		data.add("WIDE_PLAN_1");
		data.add("WIDE_PLAN_2");
		return data;
	}

	private List<String> prepareSelectedRegionalPlansForBasePlan() {
		List<String> data = new ArrayList<>();
		data.add("REGIONAL_PLAN_1");
		data.add("REGIONAL_PLAN_2");
		return data;
	}

	private List<Object[]> preparePlansForAppendix() {

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[4];
		r[0] = "10";
		r[1] = "MED_PLAN_1";
		r[2] = "Medical Plan 1";
		r[3] = "CA";
		results.add(r);
		r = new Object[4];
		r[0] = "10";
		r[1] = "MED_PLAN_2";
		r[2] = "Medical Plan 2";
		r[3] = "CA-S";
		results.add(r);
		r = new Object[4];
		r[0] = "11";
		r[1] = "DEN_PLAN_1";
		r[2] = "Dental Plan 1";
		r[3] = "NY";
		results.add(r);
		return results;
	}

	private PlanOfferingsRequest preparePlanOfferingsRequest() {
		PlanOfferingsRequest planOfferingsRequest = new PlanOfferingsRequest();
		planOfferingsRequest.setHqState("CA");
		planOfferingsRequest.setHqZipCode("12345");
		planOfferingsRequest.setPlanYearStartDate("01/01/2025");
		planOfferingsRequest.setQuarter("Q1");
		List<Carrier> carriers = new ArrayList<Carrier>();
		Carrier carrier = new Carrier();
		carrier.setId(1);
		carriers.add(carrier);
		carrier = new Carrier();
		carrier.setId(2);
		carriers.add(carrier);
		planOfferingsRequest.setCarriers(carriers);
		planOfferingsRequest.setRegions(Arrays.asList("CA", "NY", "CA-S"));
		planOfferingsRequest.setBenefitTypes(Arrays.asList("10", "11", "1D"));
		return planOfferingsRequest;
	}

	private List<Object[]> preparePlanOfferings() {

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[4];
		r[0] = "10";
		r[1] = "Q1";
		r[2] = "MED_PLAN_1";
		r[3] = "Medical Plan 1";
		results.add(r);
		r = new Object[4];
		r[0] = "10";
		r[1] = "Q1";
		r[2] = "MED_PLAN_2";
		r[3] = "Medical Plan 2";
		results.add(r);
		r = new Object[4];
		r[0] = "11";
		r[1] = "Q1";
		r[2] = "DEN_PLAN_1";
		r[3] = "Dental Plan 1";
		results.add(r);
		return results;
	}
	
	private List<Object[]> preparePlanOfferings1() {

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[4];
		r[0] = "10";
		r[1] = "MED_PLAN_1";
		r[2] = "Medical Plan 1";
		results.add(r);
		r = new Object[4];
		r[0] = "10";
		r[1] = "MED_PLAN_2";
		r[2] = "Medical Plan 2";
		results.add(r);
		r = new Object[4];
		r[0] = "11";
		r[1] = "DEN_PLAN_1";
		r[2] = "Dental Plan 1";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareCompanyPlanSelectionResults() {

		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "10";
		r[1] = "MED_PLAN_1";
		results.add(r);
		r = new Object[2];
		r[0] = "10";
		r[1] = "MED_PLAN_2";
		results.add(r);
		r = new Object[2];
		r[0] = "11";
		r[1] = "DEN_PLAN_1";
		results.add(r);
		return results;
	}
	
	private List<Object[]> prepareBenefitPlanWithCarrierByPlanType() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[4];
		r[0] = "01HIJK";
		r[1] = "Aetna";
		r[2] = new BigDecimal(100.00);
		r[3] = new BigDecimal(120.00);
		results.add(r);

		r = new Object[4];
		r[0] = "01HKJK";
		r[1] = "UHC";
		r[2] = new BigDecimal(130.00);
		r[3] = new BigDecimal(150.00);
		results.add(r);

		return results;
	}

    private List<Object[]> preparePortfolioPlansByPlanType() {
        List<Object[]> results = new ArrayList<>();
        Object[] r = new Object[3];
        r[0] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        r[1] = new BigDecimal(1);
        r[2] = "1_MED_PLAN_1";
        results.add(r);

        r = new Object[3];
        r[0] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        r[1] = new BigDecimal(1);
        r[2] = "1_MED_PLAN_2";
        results.add(r);

        r = new Object[3];
        r[0] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        r[1] = new BigDecimal(2);
        r[2] = "2_MED_PLAN_1";
        results.add(r);

        r = new Object[3];
        r[0] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
        r[1] = new BigDecimal(1);
        r[2] = "1_DEN_PLAN_1";
        results.add(r);

        r = new Object[3];
        r[0] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
        r[1] = new BigDecimal(2);
        r[2] = "2_DEN_PLAN_1";
        results.add(r);

        r = new Object[3];
        r[0] = BSSApplicationConstants.VISION_PLAN_TYPE;
        r[1] = new BigDecimal(15);
        r[2] = "15_VIS_PLAN_1";
        results.add(r);

        return results;
    }

	private Company buildCompanyWithBundle(boolean gaBundleFlag, Long bundleId) {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(101L);
		realmPlanYear.setOeQuarter("Q1");
		realmPlanYear.setPlanYearStart(new Date());
		
		Company company = new Company();
		company.setId(999L);
		company.setCode("TESTCO");
		company.setTexasSitus(false);
		company.setRealmPlanYear(realmPlanYear);
		company.setBundleId(bundleId);
		return company;
	}
	
}
