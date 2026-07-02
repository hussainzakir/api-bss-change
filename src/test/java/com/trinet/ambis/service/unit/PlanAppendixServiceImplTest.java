package com.trinet.ambis.service.unit;

import static com.trinet.ambis.enums.OmsOfferingEnum.OM_OD_OV_TLD;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAttribute;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparisonAdditonalBenefits;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.dto.CarrierAssetDto;
import com.trinet.ambis.service.dto.CmsLogoDto;
import com.trinet.ambis.service.impl.HrisPlanAttributeServiceImpl;
import com.trinet.ambis.service.impl.outputs.PlanAppendixServiceImpl;
import com.trinet.ambis.service.impl.outputs.util.PlanAppendixServiceUtil;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.outputs.CarrierLogoService;
import com.trinet.ambis.service.outputs.PlanComparisonService;
import com.trinet.ambis.test.config.TestHelper;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.FileUtils;
import com.trinet.ambis.util.StrategyUtils;

@RunWith(MockitoJUnitRunner.class)
public class PlanAppendixServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	private PlanAppendixServiceImpl planAppendixService;

	@Mock
	PlanSelectionService planSelectionService;

	@Mock
	private RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	private PlanRatesService planRatesService;

	@Mock
	private HttpServletRequest httpRequest;

	@Mock
	private CarrierLogoService carrierLogoService;

	@Mock
	private FileUtils fileUtils;
	
	@Mock
	private HrpDao hrpDao;
	
	@Mock
	private ProspectPlanAvailabilityService prospectPlanAvailabilityService;
	
	@Mock
	PlanAvailabilityService planAvailabilityService;
	
	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	HrisPlanAttributeServiceImpl hrisPlanAttributeService;

	@Mock
	PlanComparisonService planComparisonService;
	
	@Mock
	PlanCompareService planCompareService;


	BiFunction<CmsLogoDto, String, String> logoUrlByName = null;

	private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
	private MockedStatic<StrategyUtils> mockStaticStrategyUtils;
	private MockedStatic<PlanAppendixServiceUtil> mockStaticPlanAppendixServiceUtil;

	@Before
	public void setUp() {
		if (mockStaticAppRulesAndConfigsUtils == null) {
			mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		}
		if (mockStaticStrategyUtils == null) {
			mockStaticStrategyUtils = Mockito.mockStatic(StrategyUtils.class);
		}
		if (mockStaticPlanAppendixServiceUtil == null) {
			mockStaticPlanAppendixServiceUtil = Mockito.mockStatic(PlanAppendixServiceUtil.class);
		}
		ReflectionTestUtils.setField(planAppendixService, "logoFile", "test");
		logoUrlByName = (cmsDto, name) -> "https://images.contentstack.io/v3/assets/bltab010fdefd6ceb60/blt3dead4dab1e331e6/672108363a7f64d7693ebbc3/Aetna.png";
	}

    @After
    public void tearDown() {
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
        }
        if (mockStaticStrategyUtils != null) {
            mockStaticStrategyUtils.close();
        }
        if (mockStaticPlanAppendixServiceUtil != null) {
            mockStaticPlanAppendixServiceUtil.close();
        }
    }

	@Test
	public void getPlanAppendixData() {
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "23", "30"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setRegions(List.of("CA","NC", "FL"));
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);
		
		CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture = getAdditionalBeneiftData();

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");


		when(StrategyUtils.getPlanCost(Mockito.any()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataByPlanType());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
				any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("11")));

		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, additionalBenefitPlanCompareDataFuture);

		assertEquals(3, planAppendixMap.get("11").getPlanAttributes().size());
		assertEquals("CA", planAppendixMap.get("11").getPlanAttributes().get(0).getRegion());
		assertEquals("125224", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanId());
		assertEquals("MetLife Dental 100 LA Vol", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanName());
		assertEquals("FL", planAppendixMap.get("11").getPlanAttributes().get(1).getRegion());
		assertEquals("125221", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanId());
		assertEquals("Aetna Dental", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanName());
		assertEquals("FL", planAppendixMap.get("11").getPlanAttributes().get(2).getRegion());
		assertEquals("125220", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanId());
		assertEquals("Aetna Dental 100 Group", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanName());
		
		assertNotNull(planAppendixMap.get("23"));

        // Assert Rates
        assertEquals("$100.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getEmployeeOnly());
        assertEquals("$200.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getEmployeeSpouse());
        assertEquals("$300.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getEmployeeChildren());
        assertEquals("$400.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getFamily());


		assertNotNull(planAppendixMap);
	}

	@Test
	public void getMedFilterPlanAppendixData() {
		//given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "lad", "dis"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setHsa("yes");
		planAppendixFilters.setRegions(List.of("CA","NC", "FL"));
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");

		//when

		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataByPlanType());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
				any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);


		//assert
		assertEquals(10, getDentalBenefitPlanComapre("10").size());
		assertEquals(8, planAppendixMap.get("10").getPlanAttributes().size());
		assertEquals("PPO", planAppendixMap.get("10").getPlanAttributes().get(0).getPlanType());
		assertEquals(true, planAppendixMap.get("10").getPlanAttributes().get(0).isHsa());
		assertNotNull(planAppendixMap);
	}

	@Test
	public void getPlanAppendixDataForOMS() {
		//given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{
			BSSApplicationConstants.MEDICAL_PLAN_TYPE,
			BSSApplicationConstants.DENTAL_PLAN_TYPE,
			BSSApplicationConstants.VISION_PLAN_TYPE}
		));
		long strategyId = 282357;
		prospectRequest.setTnStrategyId(String.valueOf(strategyId));
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setHsa("yes");
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_OMS, OM_OD_OV_TLD.name());

		//when
		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		String medPlanId = "11111";
		PlanSelection medPlanSelection = new PlanSelection();
		medPlanSelection.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		medPlanSelection.setBenefitPlan(medPlanId);
	    	when(planSelectionService.findByStrategyIdAndPlanType(strategyId, BSSApplicationConstants.MEDICAL_PLAN_TYPE)).thenReturn(List.of(medPlanSelection));

		String denPlanId = "22222";
		PlanSelection denPlanSelection = new PlanSelection();
		denPlanSelection.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
	    	denPlanSelection.setBenefitPlan(denPlanId);
	    	when(planSelectionService.findByStrategyIdAndPlanType(strategyId, BSSApplicationConstants.DENTAL_PLAN_TYPE)).thenReturn(List.of(denPlanSelection));

		String visPlanId = "33333";
		PlanSelection vesPlanSelection = new PlanSelection();
		vesPlanSelection.setPlanType(BSSApplicationConstants.VISION_PLAN_TYPE);
		vesPlanSelection.setBenefitPlan(visPlanId);
	    	when(planSelectionService.findByStrategyIdAndPlanType(strategyId, BSSApplicationConstants.VISION_PLAN_TYPE)).thenReturn(List.of(vesPlanSelection));

		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(
				hrisPlanAttributeService.getPlanAttributesByBenefitType(any(), any())
		).thenReturn(
				CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10"))
		);
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());
		Mockito.when(planComparisonService.getOMSPlanRatesByPlan(any(Company.class), ArgumentMatchers.<Map<String, Set<String>>>any())).thenReturn(prepareRateDetails());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		//assert
		assertNotNull(planAppendixMap);

		verify(hrisPlanAttributeService).getPlanAttributesByBenefitType(Set.of(medPlanId), BSSApplicationConstants.MEDICAL);
		verify(hrisPlanAttributeService).getPlanAttributesByBenefitType(Set.of(denPlanId), BSSApplicationConstants.DENTAL);
		verify(hrisPlanAttributeService).getPlanAttributesByBenefitType(Set.of(visPlanId), BSSApplicationConstants.VISION);

		assertTrue(planAppendixMap.containsKey(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		assertTrue(planAppendixMap.containsKey(BSSApplicationConstants.DENTAL_PLAN_TYPE));
		assertTrue(planAppendixMap.containsKey(BSSApplicationConstants.VISION_PLAN_TYPE));

	}

	@Test
	public void getMedFilterPlanAppendixData_plan_with_no_hsa() {
		//given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "lad", "dis"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setHsa("No");
		planAppendixFilters.setRegions(List.of("CA","NC", "FL"));
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");

		//when

		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataByPlanType());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
						any()))
				.thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		//assert
		assertEquals(10, getDentalBenefitPlanComapre("10").size());
		assertEquals(1, planAppendixMap.get("10").getPlanAttributes().size());
		assertEquals("PPO", planAppendixMap.get("10").getPlanAttributes().get(0).getPlanType());
		assertEquals(false, planAppendixMap.get("10").getPlanAttributes().get(0).isHsa());
		assertNotNull(planAppendixMap);
	}

	@Test
	public void getMedFilterPlanAppendixData_all_plan() {
		//given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "lad", "dis"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setHsa("Na");
		planAppendixFilters.setRegions(List.of("CA","NC", "FL"));
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");

		//when

		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataByPlanType());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
						any()))
				.thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);


		//assert
		assertEquals(10, getDentalBenefitPlanComapre("10").size());
		assertEquals(9, planAppendixMap.get("10").getPlanAttributes().size());
		assertEquals("PPO", planAppendixMap.get("10").getPlanAttributes().get(0).getPlanType());
		assertEquals("AETNA PPO 7150 Central FL", planAppendixMap.get("10").getPlanAttributes().get(0).getPlanName());
		assertEquals(true, planAppendixMap.get("10").getPlanAttributes().get(0).isHsa());

		assertEquals("PPO", planAppendixMap.get("10").getPlanAttributes().get(8).getPlanType());
		assertEquals("FL Blue PPO 2000 NTL", planAppendixMap.get("10").getPlanAttributes().get(8).getPlanName());
		assertEquals(true, planAppendixMap.get("10").getPlanAttributes().get(8).isHsa());

		assertNotNull(planAppendixMap);
	}
	
	@Test
	public void getPlanAppendixDataIncludeWSEOnly() {
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "lad", "dis"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setIncludeOnlyEeLocationPlans(true);
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");
		company.setEmployeeRegions(List.of("FL", "CA"));


		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataForWSEPlans());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
				any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("11")));

		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());
		Mockito.when(prospectPlanAvailabilityService.getProspectEmployeePlansByPlanType(any(), any())).thenReturn(new HashedMap<>());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		assertEquals(3, planAppendixMap.get("11").getPlanAttributes().size());
		assertEquals("CA", planAppendixMap.get("11").getPlanAttributes().get(0).getRegion());
		assertEquals("125221", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanId());
		assertEquals("Aetna Dental", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanName());
		assertEquals("CA", planAppendixMap.get("11").getPlanAttributes().get(1).getRegion());
		assertEquals("125220", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanId());
		assertEquals("Aetna Dental 100 Group", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanName());
		assertEquals("CA", planAppendixMap.get("11").getPlanAttributes().get(2).getRegion());
		assertEquals("125224", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanId());
		assertEquals("MetLife Dental 100 LA Vol", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanName());

		assertNotNull(planAppendixMap);
	}
	
	@Test
	public void getPlanAppendixDataForZipCodes() {
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "lad", "dis"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setZipCodes(List.of("78701","32003"));
		planAppendixFilters.setGroupId("12345");
		planAppendixFilters.setGroupName("W2");
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);
		List<Object[]> planContributions = preparePlanContributionsByStrategyId();

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");
		company.setEmployeeRegions(List.of("FL"));


		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataForWSEPlans());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
				any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanCompare("11")));

		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());
		Mockito.when(hrpDao.getZipCodesAndStatesBy(any())).thenReturn(prepareZipCodeStatesMap());
		Mockito.when(planAvailabilityService.getBenefitPlanAvailability(any(PlanAvailableRequest.class))).thenReturn(preparePlanAvailableResponse());
		Mockito.when(strategyDataDao.getPlanContributionsByStrategyId(company, 282357L, true)).thenReturn((planContributions));

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		assertEquals(1, planAppendixMap.get("11").getPlanAttributes().size());
		assertEquals("CA", planAppendixMap.get("11").getPlanAttributes().get(0).getRegion());
		assertEquals("125224", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanId());
		assertEquals("MetLife Dental 100 LA Vol", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanName());
		assertNull(planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates());
		assertNotNull(planAppendixMap.get("11").getPlanAttributes().get(0).getEeErPlanRates());
		assertEquals(BigDecimal.valueOf(279.38), planAppendixMap.get("11").getPlanAttributes().get(0).getEeErPlanRates().getEmployeeOnly().getEeAmount());
		assertEquals(BigDecimal.valueOf(279.31), planAppendixMap.get("11").getPlanAttributes().get(0).getEeErPlanRates().getEmployeeOnly().getErAmount());
		assertEquals(BigDecimal.valueOf(558.69), planAppendixMap.get("11").getPlanAttributes().get(0).getEeErPlanRates().getEmployeeOnly().getTotal());
		Mockito.verify(strategyDataDao, times(1)).getPlanContributionsByStrategyId(company, 282357L, true);
		assertNotNull(planAppendixMap);
	}

	@Test
	public void getPlanAppendixDataForZipCodesWithIncludeWSE() {
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "lad", "dis"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setZipCodes(List.of("78701","32003"));
		planAppendixFilters.setIncludeOnlyEeLocationPlans(true);
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");
		company.setEmployeeRegions(List.of("FL"));


		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataForWSEPlans());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
				any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("11")));

		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());
		Mockito.when(prospectPlanAvailabilityService.getProspectEmployeePlansByPlanType(any(), any())).thenReturn(new HashedMap<>());
		Mockito.when(hrpDao.getZipCodesAndStatesBy(any())).thenReturn(prepareZipCodeStatesMap());
		Mockito.when(planAvailabilityService.getBenefitPlanAvailability(any(PlanAvailableRequest.class))).thenReturn(preparePlanAvailableResponse());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		assertEquals(3, planAppendixMap.get("11").getPlanAttributes().size());
		assertEquals("CA", planAppendixMap.get("11").getPlanAttributes().get(0).getRegion());
		assertEquals("125224", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanId());
		assertEquals("MetLife Dental 100 LA Vol", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanName());
		assertEquals("125221", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanId());
		assertEquals("Aetna Dental", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanName());
		assertEquals("125220", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanId());
		assertEquals("Aetna Dental 100 Group", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanName());
		
		assertNotNull(planAppendixMap);
		assertNotNull(planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates());
		assertNull(planAppendixMap.get("11").getPlanAttributes().get(0).getEeErPlanRates());
		Mockito.verify(strategyDataDao, times(0)).getPlanContributionsByStrategyId(company, 282357L, true);
	}	
	
	@Test
	public void getPlanAppendixDataFilterWith_NTL_NonNTLPlans() {
		// given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setTemplateNames(Arrays.asList("APX"));
		prospectRequest.setBenefitTypes(Arrays.asList(new String[] { "med", "den", "vis", "lad", "dis" }));
		prospectRequest.setTnStrategyId("326542");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO", "HSO", "EPO", "HMO", "Indemnity"));
		planAppendixFilters.setHsa("NA");
		planAppendixFilters.setRegions(List.of("FL", "FL-C", "FL-N", "FL-S", "MA"));
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");

		// when //getPlanAppendixDataByBenType(company, prospectRequest)

		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);
		
		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(),
				Mockito.anyBoolean())).thenReturn(createPlanAppendixDataForRegionPlans());
		

		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(), any()))
				.thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));
		
		
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);
		// assert
		List<PlanAttribute> planAttributes = planAppendixMap.get("10").getPlanAttributes();
		assertNotNull(planAttributes);
		assertEquals(10, getDentalBenefitPlanComapre("10").size());
		
		// Regional Plans assertions
		assertEquals("AETNA PPO 7150 Central FL", planAttributes.get(0).getPlanName());
		assertEquals("Aetna", planAttributes.get(0).getCarrierName());
		
		// National Plans assertions
		assertEquals("BS-CA PPO 300 S NTL MA", planAttributes.get(planAttributes.size()-1).getPlanName());
		assertEquals("Blue Shield of California", planAttributes.get(planAttributes.size()-1).getCarrierName());
		
		assertEquals("FL Blue PPO 2000 NTL", planAttributes.get(planAttributes.size()-2).getPlanName());
		assertEquals("Florida Blue", planAttributes.get(planAttributes.size()-2).getCarrierName());
		
	}
	
	@Test
	public void getEeErPlanRateTest() {
		// Given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList(new String[] { "med", "den", "vis", "lad", "dis" }));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO", "HSO"));
		planAppendixFilters.setIncludeOnlyEeLocationPlans(true);
		planAppendixFilters.setGroupId("12345");
		planAppendixFilters.setGroupName("W2");
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);
		List<Object[]> planContributions = preparePlanContributionsByStrategyId();
		List<String> planIds = Arrays.asList("125220", "125224");

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");
		company.setEmployeeRegions(List.of("FL"));

		// when

		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(),
				Mockito.anyBoolean())).thenReturn(createPlanAppendixDataForWSEPlans());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(), any()))
				.thenReturn(CompletableFuture.completedFuture(getBenefitPlanCompare("11", planIds)));
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());
		Mockito.when(prospectPlanAvailabilityService.getProspectEmployeePlansByPlanType(any(), any()))
				.thenReturn(new HashedMap<>());
		Mockito.when(strategyDataDao.getPlanContributionsByStrategyId(company, 282357L, true))
				.thenReturn((planContributions));

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		// then
		assertEquals("MetLife Dental 100 LA Vol", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanName());
		assertEquals(BigDecimal.valueOf(558.69),
				planAppendixMap.get("11").getPlanAttributes().get(0).getEeErPlanRates().getEmployeeOnly().getTotal());
		assertEquals("Aetna Dental 100 Group", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanName());
		assertEquals(BigDecimal.valueOf(578.69),
				planAppendixMap.get("11").getPlanAttributes().get(1).getEeErPlanRates().getEmployeeOnly().getTotal());
		BigDecimal costOfFirstIndexedPlan = planAppendixMap.get("11").getPlanAttributes().get(0).getEeErPlanRates().getEmployeeOnly().getTotal();
		BigDecimal costOfSecondIndexedPlan =  planAppendixMap.get("11").getPlanAttributes().get(1).getEeErPlanRates().getEmployeeOnly().getTotal();
		assertTrue(costOfSecondIndexedPlan.compareTo(costOfFirstIndexedPlan) > 0);
	}
	
	@Test
	public void getPlanAppendixDataFilterWith_Carrier_Sorting_Test() {
		// given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setTemplateNames(Arrays.asList("APX"));
		prospectRequest.setBenefitTypes(Arrays.asList(new String[] { "med", "den", "vis", "lad", "dis" }));
		prospectRequest.setTnStrategyId("326542");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO", "HSO", "EPO", "HMO", "Indemnity"));
		planAppendixFilters.setHsa("NA");
		planAppendixFilters.setRegions(List.of("FL", "FL-C", "FL-N", "FL-S", "MA"));
		planAppendixFilters.setIncludeCarrierSorting(true);
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");

		// when

		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);
		
		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(),
				Mockito.anyBoolean())).thenReturn(createPlanAppendixDataForRegionPlans());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(), any()))
				.thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		// assert for Medical Plans
		List<PlanAttribute> medicalPlanAttributes = planAppendixMap.get("10").getPlanAttributes();
		assertNotNull(medicalPlanAttributes);
		assertEquals(10, getDentalBenefitPlanComapre("10").size());
		// Regional Plans assertions
		assertEquals("Aetna", medicalPlanAttributes.get(0).getCarrierName());
		assertEquals("UnitedHealthcare", medicalPlanAttributes.get(3).getCarrierName());
		// National Plans assertions
		assertEquals("BS-CA PPO 300 S NTL MA", medicalPlanAttributes.get(medicalPlanAttributes.size()-1).getPlanName());
		assertEquals("Blue Shield of California", medicalPlanAttributes.get(medicalPlanAttributes.size()-1).getCarrierName());
		assertEquals("FL Blue PPO 2000 NTL", medicalPlanAttributes.get(medicalPlanAttributes.size()-2).getPlanName());
		assertEquals("Florida Blue", medicalPlanAttributes.get(medicalPlanAttributes.size()-2).getCarrierName());

		// asserts for Dental Plans
		List<PlanAttribute> dentalPlanAttributes = planAppendixMap.get("11").getPlanAttributes();
		assertNotNull(dentalPlanAttributes);
		assertEquals(3, getDentalBenefitPlanComapre("11").size());
		// Regional Plans assertions
		assertEquals("Aetna", dentalPlanAttributes.get(0).getCarrierName());
		assertEquals("UnitedHealthcare", dentalPlanAttributes.get(3).getCarrierName());
		// National Plans assertions
		assertEquals("FL Blue PPO 2000 NTL", dentalPlanAttributes.get(dentalPlanAttributes.size()-1).getPlanName());
		assertEquals("Florida Blue", dentalPlanAttributes.get(dentalPlanAttributes.size()-1).getCarrierName());
		assertEquals("BS-CA PPO 300 S NTL MA", dentalPlanAttributes.get(dentalPlanAttributes.size()-2).getPlanName());
		assertEquals("Blue Shield of California", dentalPlanAttributes.get(dentalPlanAttributes.size()-2).getCarrierName());	
		
		// asserts for Vision Plans
		List<PlanAttribute> visionPlanAttributes = planAppendixMap.get("14").getPlanAttributes();
		assertNotNull(visionPlanAttributes);
		assertEquals(2, getDentalBenefitPlanComapre("14").size());
		// Regional Plans assertions
		assertEquals("Aetna", visionPlanAttributes.get(0).getCarrierName());
		assertEquals("UnitedHealthcare", visionPlanAttributes.get(3).getCarrierName());
		// National Plans assertions
		assertEquals("FL Blue PPO 2000 NTL", visionPlanAttributes.get(visionPlanAttributes.size()-1).getPlanName());
		assertEquals("Florida Blue", visionPlanAttributes.get(visionPlanAttributes.size()-1).getCarrierName());
		assertEquals("BS-CA PPO 300 S NTL MA", visionPlanAttributes.get(visionPlanAttributes.size()-2).getPlanName());
		assertEquals("Blue Shield of California", visionPlanAttributes.get(visionPlanAttributes.size()-2).getCarrierName());
	}
	
	@Test
	public void getPlanAppendixDataFilterWithout_Carrier_Sorting_Test() {
		// given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setTemplateNames(Arrays.asList("APX"));
		prospectRequest.setBenefitTypes(Arrays.asList(new String[] { "med", "den", "vis", "lad", "dis" }));
		prospectRequest.setTnStrategyId("326542");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO", "HSO", "EPO", "HMO", "Indemnity"));
		planAppendixFilters.setHsa("NA");
		planAppendixFilters.setRegions(List.of("AK", "AS", "HI", "MA", "AL", "FL", "LA", "MS"));
		planAppendixFilters.setIncludeCarrierSorting(false);
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");

		// when
		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);
		
		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(),
				Mockito.anyBoolean())).thenReturn(createPlanAppendixDataForRegionPlans());
		Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(), any()))
				.thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		// asserts for Medical Plans
		List<PlanAttribute> medicalPlanAttributes = planAppendixMap.get("10").getPlanAttributes();
		assertNotNull(medicalPlanAttributes);
		assertEquals(10, getDentalBenefitPlanComapre("10").size());
		// Regional Plans assertions
		assertEquals("Aetna", medicalPlanAttributes.get(0).getCarrierName());
		assertEquals("UnitedHealthcare", medicalPlanAttributes.get(3).getCarrierName());
		// National Plans assertions
		assertEquals("BS-CA PPO 300 S NTL MA", medicalPlanAttributes.get(medicalPlanAttributes.size()-1).getPlanName());
		assertEquals("Blue Shield of California", medicalPlanAttributes.get(medicalPlanAttributes.size()-1).getCarrierName());
		assertEquals("FL Blue PPO 2000 NTL", medicalPlanAttributes.get(medicalPlanAttributes.size()-2).getPlanName());
		assertEquals("Florida Blue", medicalPlanAttributes.get(medicalPlanAttributes.size()-2).getCarrierName());
		
		// asserts for Dental Plans
		List<PlanAttribute> dentalPlanAttributes = planAppendixMap.get("11").getPlanAttributes();
		assertNotNull(dentalPlanAttributes);
		assertEquals(3, getDentalBenefitPlanComapre("11").size());
		// Regional Plans assertions
		assertEquals("Aetna", dentalPlanAttributes.get(0).getCarrierName());
		assertEquals("UnitedHealthcare", dentalPlanAttributes.get(3).getCarrierName());
		// National Plans assertions
		assertEquals("FL Blue PPO 2000 NTL", dentalPlanAttributes.get(dentalPlanAttributes.size()-1).getPlanName());
		assertEquals("Florida Blue", dentalPlanAttributes.get(dentalPlanAttributes.size()-1).getCarrierName());
		assertEquals("BS-CA PPO 300 S NTL MA", dentalPlanAttributes.get(dentalPlanAttributes.size()-2).getPlanName());
		assertEquals("Blue Shield of California", dentalPlanAttributes.get(dentalPlanAttributes.size()-2).getCarrierName());	
		
		// asserts for Vision Plans
		List<PlanAttribute> visionPlanAttributes = planAppendixMap.get("14").getPlanAttributes();
		assertNotNull(visionPlanAttributes);
		assertEquals(2, getDentalBenefitPlanComapre("14").size());
		// Regional Plans assertions
		assertEquals("Aetna", visionPlanAttributes.get(0).getCarrierName());
		assertEquals("UnitedHealthcare", visionPlanAttributes.get(3).getCarrierName());
		// National Plans assertions
		assertEquals("FL Blue PPO 2000 NTL", visionPlanAttributes.get(visionPlanAttributes.size()-1).getPlanName());
		assertEquals("Florida Blue", visionPlanAttributes.get(visionPlanAttributes.size()-1).getCarrierName());
		assertEquals("BS-CA PPO 300 S NTL MA", visionPlanAttributes.get(visionPlanAttributes.size()-2).getPlanName());
		assertEquals("Blue Shield of California", visionPlanAttributes.get(visionPlanAttributes.size()-2).getCarrierName());
	}

	@Test
	public void getPlanAppendixData_TIB_Medical() {
		// given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setTemplateNames(Arrays.asList("APX"));
		prospectRequest.setBenefitTypes(Arrays.asList(new String[] { "10"}));
		prospectRequest.setTnStrategyId("326542");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(new ArrayList<>());
		planAppendixFilters.setHsa("NA");
		planAppendixFilters.setRegions(List.of("AK", "AS", "HI", "MA", "AL", "FL", "LA", "MS"));
		planAppendixFilters.setIncludeCarrierSorting(false);
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		Company company = setupCompany(BenExchngEnums.TRINET_OMS, OM_OD_OV_TLD.name());

		// when

		when(StrategyUtils.getPlanCost(Mockito.anyList()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(),
				Mockito.anyBoolean())).thenReturn(createPlanAppendixDataByPlanType());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());
		Mockito.when(planComparisonService.getOMSPlanRatesByPlan(any(Company.class), ArgumentMatchers.<Map<String, Set<String>>>any())).thenReturn(prepareRateDetails());
		Mockito.when(hrisPlanAttributeService.getPlanAttributesByBenefitType(any(), any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, null);

		// asserts for Medical Plans
		List<PlanAttribute> medicalPlanAttributes = planAppendixMap.get("10").getPlanAttributes();
		assertNotNull(medicalPlanAttributes);
		assertEquals(10, getDentalBenefitPlanComapre("10").size());
		// Regional Plans assertions
		assertEquals("Aetna", medicalPlanAttributes.get(0).getCarrierName());
		assertEquals("Age Banded", medicalPlanAttributes.get(4).getPlanRates().getEmployeeOnly());
		assertEquals("$100.11", medicalPlanAttributes.get(0).getPlanRates().getEmployeeOnly());
		assertEquals("Age Banded", medicalPlanAttributes.get(5).getPlanRates().getEmployeeOnly());
	}
	
	private List<Object[]> preparePlanContributionsByStrategyId() {
		List<Object[]> results = new ArrayList<Object[]>();
		Object[] r = new Object[16];
		r[0] = new BigDecimal(4298992);
		r[1] = new BigDecimal(1370898);
		r[2] = "1";
		r[3] = new BigDecimal(49.99910495318905);
		r[4] = new BigDecimal(1);
		r[5] = new BigDecimal(279.38);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(12345);
		r[8] = "125224";
		r[9] = "11";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "DFLT";
		results.add(r);
		r = new Object[16];
		r[0] = new BigDecimal(4298993);
		r[1] = new BigDecimal(1370898);
		r[2] = "2";
		r[3] = new BigDecimal(24.99932872090005);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(837.96);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(12345);
		r[8] = "125224";
		r[9] = "11";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "FPL";
		results.add(r);
		r = new Object[16];
		r[0] = new BigDecimal(4298994);
		r[1] = new BigDecimal(1370898);
		r[2] = "C";
		r[3] = new BigDecimal(27.77683854606931);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(726.24);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(12345);
		r[8] = "125224";
		r[9] = "11";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "DFLT";
		results.add(r);
		r = new Object[16];
		r[0] = new BigDecimal(4298995);
		r[1] = new BigDecimal(1370898);
		r[2] = "4";
		r[3] = new BigDecimal(16.66626887045766);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(1396.59);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(12346);
		r[8] = "125224";
		r[9] = "11";
		r[10] = 47401;
		r[11] = "BENEFIT_PROGRAM";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		r[14] = new BigDecimal(1);
		r[15] = "DFLT";
		results.add(r);
		
		Object[] r1 = new Object[16];
		r1[0] = new BigDecimal(4298993);
		r1[1] = new BigDecimal(1370899);
		r1[2] = "1";
		r1[3] = new BigDecimal(89.99910495318905);
		r1[4] = new BigDecimal(1);
		r1[5] = new BigDecimal(289.38);
		r1[6] = new BigDecimal(289.31);
		r1[7] = new BigDecimal(12345);
		r1[8] = "125220";
		r1[9] = "11";
		r1[10] = 47402;
		r1[11] = "BENEFIT_PrOGrAM";
		r1[12] = new BigDecimal(18);
		r1[13] = "BASE";
		r1[14] = new BigDecimal(1);
		r1[15] = "DFLT";
		results.add(r1);
		r1 = new Object[16];
		r1[0] = new BigDecimal(4298994);
		r1[1] = new BigDecimal(1370900);
		r1[2] = "2";
		r1[3] = new BigDecimal(34.99932872090005);
		r1[4] = new BigDecimal(0);
		r1[5] = new BigDecimal(867.96);
		r1[6] = new BigDecimal(299.31);
		r1[7] = new BigDecimal(12345);
		r1[8] = "125220";
		r1[9] = "11";
		r1[10] = 47402;
		r1[11] = "BENEFIT_PROGRAM";
		r1[12] = new BigDecimal(18);
		r1[13] = "BASE";
		r1[14] = new BigDecimal(1);
		r1[15] = "FPL";
		results.add(r1);
		r1 = new Object[16];
		r1[0] = new BigDecimal(4298995);
		r1[1] = new BigDecimal(1370901);
		r1[2] = "C";
		r1[3] = new BigDecimal(49.77683854606931);
		r1[4] = new BigDecimal(0);
		r1[5] = new BigDecimal(721.24);
		r1[6] = new BigDecimal(274.31);
		r1[7] = new BigDecimal(12345);
		r1[8] = "125220";
		r1[9] = "11";
		r1[10] = 47402;
		r1[11] = "BENEFIT_PROGRAM";
		r1[12] = new BigDecimal(18);
		r1[13] = "BASE";
		r1[14] = new BigDecimal(1);
		r1[15] = "DFLT";
		results.add(r1);
		r1 = new Object[16];
		r1[0] = new BigDecimal(4298996);
		r1[1] = new BigDecimal(13709902);
		r1[2] = "4";
	 r1[3] = new BigDecimal(36.66626887045766);
	 r1[4] = new BigDecimal(0);
	 r1[5] = new BigDecimal(1496.59);
	 r1[6] = new BigDecimal(579.31);
	 r1[7] = new BigDecimal(12345);
	 r1[8] = "125220";
	 r1[9] = "11";
	 r1[10] = 47402;
	 r1[11] = "BENEFIT_PROGRAM";
	 r1[12] = new BigDecimal(18);
	 r1[13] = "BASE";
	 r1[14] = new BigDecimal(1);
	 r1[15] = "DFLT";
	 results.add(r1);

		return results;
	}

	private Map<String, List<PlanAppendixBenefitPlanData>> createPlanAppendixDataByPlanType() {
		Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataByPlanType = new HashMap<>();

		//Medical
		List<PlanAppendixBenefitPlanData> planAppendixBenefitPlanDataList = new ArrayList<>();
		PlanAppendixBenefitPlanData planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("MEDPLAN1");
		planAppendixBenefitPlanData.setDescription("Aetna Medical Plan 1");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("MEDPLAN2");
		planAppendixBenefitPlanData.setDescription("Aetna Medical Plan 2");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		//Dental
		planAppendixBenefitPlanDataList = new ArrayList<>();
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125224");
		planAppendixBenefitPlanData.setDescription("MetLife Dental 100 LA Vol");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("CA");

		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125224");
		planAppendixBenefitPlanData.setDescription("MetLife Dental 100 LA Vol");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("NC");

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125220");
		planAppendixBenefitPlanData.setDescription("Aetna Dental 100 Group");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125221");
		planAppendixBenefitPlanData.setDescription("Aetna Dental");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		//Vision
		planAppendixBenefitPlanDataList = new ArrayList<>();
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("VISPLAN1");
		planAppendixBenefitPlanData.setDescription("Aetna Vision Plan 1");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("VISPLAN2");
		planAppendixBenefitPlanData.setDescription("Aetna Vision Plan 2");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		return planAppendixDataByPlanType;
	}

	private Map<String, BigDecimal> prepareCoverageCostMap() {
		Map<String, BigDecimal> planCostMap = new HashMap<>();
		planCostMap.put(CoverageCodesEnums.valueOfId(ProspectConstants.EMPLOYEE), BigDecimal.valueOf(100.00));
		planCostMap.put(CoverageCodesEnums.valueOfId(ProspectConstants.EMPLOYEE_PLUS_SPOUSE), BigDecimal.valueOf(200.00));
		planCostMap.put(CoverageCodesEnums.valueOfId(ProspectConstants.EMPLOYEE_PLUS_CHILD), BigDecimal.valueOf(300.00));
		planCostMap.put(CoverageCodesEnums.valueOfId(ProspectConstants.EMPLOYEE_PLUS_FAMILY), BigDecimal.valueOf(400.00));
		return planCostMap;

	}


	private List<BenefitPlanCompare> getDentalBenefitPlanComapre(String benType){
		TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<Map<String,List<BenefitPlanCompare>>>(){};
		Map<String,List<BenefitPlanCompare>> planCompare = (Map<String,List<BenefitPlanCompare>>)TestHelper.readPlanComparisonRequest("/planComparison/currentPlansAttributes.json", type).get();
		return planCompare.get(benType);
	}

	private CmsLogoDto getCmsLogDto(){
		CmsLogoDto logDto = new CmsLogoDto();
		CmsLogoDto.LogoDto logo = new CmsLogoDto.LogoDto();
		List<String> url = new ArrayList<>();
		url.add("aethena");
		List< CmsLogoDto.LogoDto > assets = new ArrayList<>();
		assets.add(logo);
		logDto.setAssets(assets);
		CmsLogoDto.MetaData metaData = new CmsLogoDto.MetaData();
		CmsLogoDto.Extensions extensions = new CmsLogoDto.Extensions();
		CmsLogoDto.CarrierDetails carrierDetails = new CmsLogoDto.CarrierDetails();
		List<String> carrierNames = new ArrayList<>();
		carrierNames.add("aethena");
		carrierNames.add("Aetna");
		carrierNames.add("Florida Blue");
		carrierNames.add("UnitedHealthcare");
		carrierDetails.setCarrierNames(carrierNames);
		List<CmsLogoDto.CarrierDetails> carrierDetailsList = new ArrayList<>();
		extensions.setCarrierDetailsList(carrierDetailsList);
		metaData.setExtensions(extensions);
		logo.setMetadata(metaData);
		logo.setUid("123345");
		//logo.setUrl("https://images.contentstack.io/v3/assets/bltab010fdefd6ceb60/blt3dead4dab1e331e6/672108363a7f64d7693ebbc3/Aetna.png");
		logo.setUrl("123345");
		return logDto;
	}

	public Supplier<CarrierAssetDto> getCarrierLogoAsset(){
		return () -> {
			CarrierAssetDto carrierAssetDto = new CarrierAssetDto();
			CarrierAssetDto.AssestDetails details = new CarrierAssetDto.AssestDetails();
			details.setUid("123345");
			details.setCarrierNames(List.of("Aetna", "MetLife" ,"Florida Blue", "UnitedHealthcare"));
			List<CarrierAssetDto.AssestDetails> assestDetails = new ArrayList<>();
			assestDetails.add(details);
			carrierAssetDto.setAssestDetails(assestDetails);
			return carrierAssetDto;
		};
	}

	private Function<String, String> getLogos(){
		return carrierName -> {
			return String.valueOf(new HashMap<>());
		};
	}
	
	private Map<String, List<PlanAppendixBenefitPlanData>> createPlanAppendixDataForWSEPlans() {
		Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataByPlanType = new HashMap<>();

		//Medical
		List<PlanAppendixBenefitPlanData> planAppendixBenefitPlanDataList = new ArrayList<>();
		PlanAppendixBenefitPlanData planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125246");
		planAppendixBenefitPlanData.setDescription("FL Blue PPO 2000 NTL");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("CA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125266");
		planAppendixBenefitPlanData.setDescription("UHC Primary 1000 Dallas");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("CA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		//Dental
		planAppendixBenefitPlanDataList = new ArrayList<>();
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125224");
		planAppendixBenefitPlanData.setDescription("MetLife Dental 100 LA Vol");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("CA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125220");
		planAppendixBenefitPlanData.setDescription("Aetna Dental 100 Group");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("CA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125221");
		planAppendixBenefitPlanData.setDescription("Aetna Dental");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("CA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		//Vision
		planAppendixBenefitPlanDataList = new ArrayList<>();
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("124842");
		planAppendixBenefitPlanData.setDescription("VSP Vision");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("122179");
		planAppendixBenefitPlanData.setDescription("Aetna EyeMed Vol");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		return planAppendixDataByPlanType;
	}
	
	private Map<String, List<PlanAppendixBenefitPlanData>> createPlanAppendixDataForRegionPlans() {
		Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataByPlanType = new HashMap<>();

		//Medical
		List<PlanAppendixBenefitPlanData> planAppendixBenefitPlanDataList = new ArrayList<>();
		PlanAppendixBenefitPlanData planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125246");
		planAppendixBenefitPlanData.setDescription("AETNA PPO 7150 Central FL");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("FL-C");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125266");
		planAppendixBenefitPlanData.setDescription("Aetna EPO 1000 Southeast/N FL");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("FL-N");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125270");
		planAppendixBenefitPlanData.setDescription("Aetna EPO 4000 Tri-State");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("MA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125273");
		planAppendixBenefitPlanData.setDescription("BS-CA HDHP 5500 S NTL");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125274");
		planAppendixBenefitPlanData.setDescription("AETNA Indemnity 1000 NTL");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125275");
		planAppendixBenefitPlanData.setDescription("BS-CA PPO 300 S NTL MA");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("MA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125276");
		planAppendixBenefitPlanData.setDescription("UHC 500 US Territories");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("AS");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125277");
		planAppendixBenefitPlanData.setDescription("Kaiser HMO HI");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("HI");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125278");
		planAppendixBenefitPlanData.setDescription("UHC PPO 100 HI");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("HI");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125279");
		planAppendixBenefitPlanData.setDescription("Kaiser POS HI");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("HI");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125280");
		planAppendixBenefitPlanData.setDescription("HPHC PPO/HDHP 3500 MA");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("MA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125281");
		planAppendixBenefitPlanData.setDescription("HPHC PPO 2000 MA");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("MA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125282");
		planAppendixBenefitPlanData.setDescription("FL Blue HDHP 3500 NTL MA");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("MA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125283");
		planAppendixBenefitPlanData.setDescription("FL Blue HDHP 6350 NTL");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("AK");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
	    planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125284");
		planAppendixBenefitPlanData.setDescription("FL Blue PPO 5500 Copay");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanData.setRegion("AK");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		//Dental
		planAppendixBenefitPlanDataList = new ArrayList<>();
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125276");
		planAppendixBenefitPlanData.setDescription("MetLife Dental 100 LA Vol");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125277");
		planAppendixBenefitPlanData.setDescription("Aetna Dental 100 Group");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("MA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125278");
		planAppendixBenefitPlanData.setDescription("Aetna Dental");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("NJ");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125279");
		planAppendixBenefitPlanData.setDescription("Guardian Dental Value");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("AK");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125280");
		planAppendixBenefitPlanData.setDescription("Aetna Dental Value");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("AL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125281");
		planAppendixBenefitPlanData.setDescription("Delta Dental 100 FL");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125282");
		planAppendixBenefitPlanData.setDescription("Aetna Dental Value LA");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("LA");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("125283");
		planAppendixBenefitPlanData.setDescription("Aetna Dental Value MS");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanData.setRegion("MS");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		//Vision
		planAppendixBenefitPlanDataList = new ArrayList<>();
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("124879");
		planAppendixBenefitPlanData.setDescription("VSP Vision");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("122180");
		planAppendixBenefitPlanData.setDescription("Aetna EyeMed Vol");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanData.setRegion("FL");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);
		
		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("124881");
		planAppendixBenefitPlanData.setDescription("Aetna EyeMed");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("122182");
		planAppendixBenefitPlanData.setDescription("VSP Vision");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("122182");
		planAppendixBenefitPlanData.setDescription("Aetna EyeMed Plus");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("122184");
		planAppendixBenefitPlanData.setDescription("VSP Vision Plus");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixDataByPlanType.put(planAppendixBenefitPlanData.getPlanType(), planAppendixBenefitPlanDataList);

		return planAppendixDataByPlanType;
	}

	private Map<String, String> prepareZipCodeStatesMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("78701", "CA");
		map.put("32003", "FL");
		return map;
	}
	
	private CompletableFuture<List<PlanAvailableResponse>> preparePlanAvailableResponse() {
		List<PlanAvailableResponse> list = new ArrayList<>();
		list.add(PlanAvailableResponse.builder().postal("78701").plansByBenType(
				Arrays.asList(PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(Arrays.asList("125246")).build())).build());
		list.add(PlanAvailableResponse.builder().postal("32003").plansByBenType(
				Arrays.asList(PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(Arrays.asList("125224")).build())).build());
		return CompletableFuture.completedFuture(list);
	}	
	
	private List<BenefitPlanCompare> getDentalBenefitPlanCompare(String benType){
		TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<Map<String,List<BenefitPlanCompare>>>(){};
		Map<String,List<BenefitPlanCompare>> planCompare = (Map<String,List<BenefitPlanCompare>>)TestHelper.readPlanComparisonRequest("/planComparison/currentPlansAttributes.json", type).get();
		
		return planCompare.get(benType).stream().filter(plan -> plan.getPlanId().equals("125224")).collect(Collectors.toList());
	}
	
	private CompletableFuture<Map<String, BasePlanComparison>> getAdditionalBeneiftData() {
		Map<String, BasePlanComparison> additionalBenefitPlanCompareData = new HashMap<String, BasePlanComparison>();
		PlanComparisonAdditonalBenefits additionalBenefits = new PlanComparisonAdditonalBenefits();
		additionalBenefits.setAvailableGroupDetails(new ArrayList<>());
		additionalBenefitPlanCompareData.put("23", additionalBenefits);
		additionalBenefitPlanCompareData.put("30", additionalBenefits);
		
		return CompletableFuture.completedFuture(additionalBenefitPlanCompareData);
	}
	
	private List<BenefitPlanCompare> getBenefitPlanCompare(String benType, List<String> planIds) {
		TypeReference<Map<String, List<BenefitPlanCompare>>> type = new TypeReference<Map<String, List<BenefitPlanCompare>>>() {
		};
		Map<String, List<BenefitPlanCompare>> planCompare = (Map<String, List<BenefitPlanCompare>>) TestHelper
				.readPlanComparisonRequest("/planComparison/currentPlansAttributes.json", type).get();
		return planCompare.get(benType).stream().filter(plan -> planIds.contains(plan.getPlanId()))
				.collect(Collectors.toList());
	}

	private Company setupCompany(BenExchngEnums benExchange, String omsOffering) {
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(benExchange.getBenExchng());
		company.setRealmPlanYear(new RealmPlanYear());
		company.setRealm(realm);
		company.setOmsOffering(omsOffering);
		return company;
	}

	private Map<String, PlanCompareDetailDto.RateDetail> prepareRateDetails() {
		return Map.of("125246",
				PlanCompareDetailDto.RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(
								PlanCompareDetailDto.TierRate.builder().cvgTierCode("15").cost(new BigDecimal(500.55)).build(),
								PlanCompareDetailDto.TierRate.builder().cvgTierCode("60").cost(new BigDecimal(600.66)).build()))
						.build(),
				"125266",
				PlanCompareDetailDto.RateDetail.builder().regionCode(List.of("ALL")).rateType("4Tier")
						.tierRates(List.of(
								PlanCompareDetailDto.TierRate.builder().cvgTierCode(CoverageCodesEnums.COV_EMPLOYEE.getId()).cost(new BigDecimal(100.11)).build(),
								PlanCompareDetailDto.TierRate.builder().cvgTierCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId()).cost(new BigDecimal(200.22)).build(),
								PlanCompareDetailDto.TierRate.builder().cvgTierCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId()).cost(new BigDecimal(300.33)).build(),
								PlanCompareDetailDto.TierRate.builder().cvgTierCode(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId()).cost(new BigDecimal(400.44)).build()))
						.build(),
				"125270",
				PlanCompareDetailDto.RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(
								PlanCompareDetailDto.TierRate.builder().cvgTierCode("20").cost(new BigDecimal(700.55)).build(),
								PlanCompareDetailDto.TierRate.builder().cvgTierCode("55").cost(new BigDecimal(800.66)).build()))
						.build());
	}

    @Test
    public void getPlanAppendixDataViaPlanRateV2() {
        OutputRequest prospectRequest = new OutputRequest();
        prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "23", "30"}));
        prospectRequest.setTnStrategyId("282357");
        PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
        planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
        planAppendixFilters.setRegions(List.of("CA","NC", "FL"));
        prospectRequest.setPlanAppendixFilters(planAppendixFilters);

        CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture = getAdditionalBeneiftData();

        Company company = setupCompany(BenExchngEnums.TRINET_III, "");


        when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any())).thenReturn(Boolean.TRUE);
        Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataByPlanType());
        Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
        Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
        Mockito.when(planCompareService.getPlanAttributes(any(), any(), any(),
                any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("11")));

        Mockito.when(carrierLogoService.fetchLogoUrl()).thenReturn(getLogos());
		when(StrategyUtils.getPlanCost(Mockito.any())).thenReturn(prepareCoverageCostMap());

        Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
                httpRequest, additionalBenefitPlanCompareDataFuture);

        assertEquals(3, planAppendixMap.get("11").getPlanAttributes().size());
        assertEquals("CA", planAppendixMap.get("11").getPlanAttributes().get(0).getRegion());
        assertEquals("125224", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanId());
        assertEquals("MetLife Dental 100 LA Vol", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanName());
        assertEquals("FL", planAppendixMap.get("11").getPlanAttributes().get(1).getRegion());
        assertEquals("125221", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanId());
        assertEquals("Aetna Dental", planAppendixMap.get("11").getPlanAttributes().get(1).getPlanName());
        assertEquals("FL", planAppendixMap.get("11").getPlanAttributes().get(2).getRegion());
        assertEquals("125220", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanId());
        assertEquals("Aetna Dental 100 Group", planAppendixMap.get("11").getPlanAttributes().get(2).getPlanName());

        // Assert Rates
        assertEquals("$100.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getEmployeeOnly());
        assertEquals("$200.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getEmployeeSpouse());
        assertEquals("$300.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getEmployeeChildren());
        assertEquals("$400.00", planAppendixMap.get("11").getPlanAttributes().get(0).getPlanRates().getFamily());

        assertNotNull(planAppendixMap.get("23"));
        assertNotNull(planAppendixMap);
    }

	@Test
	public void getPlanAppendixDataOutputPhase2AndMdvPageBreakEnabled() {
		mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBssOutputPhase2Enabled).thenReturn(true);
		mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isMdvPageBreakEnabled).thenReturn(true);

		Mockito.when(carrierLogoService.fetchCarrierLogos()).thenReturn(getCmsLogDto());
		Mockito.when(carrierLogoService.fetchCarrierLogos(any(), any(), any())).thenReturn(getCarrierLogoAsset().get());

		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setTemplateNames(List.of(ProspectConstants.PLAN_APPENDIX));
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"10", "med", "den", "vis", "23", "30"}));
		prospectRequest.setTnStrategyId("282357");
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setRegions(List.of("CA","NC", "FL"));
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);

		CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture = getAdditionalBeneiftData();

		Company company = setupCompany(BenExchngEnums.TRINET_III, "");


		when(StrategyUtils.getPlanCost(Mockito.any()))
				.thenReturn(prepareCoverageCostMap());
		when(PlanAppendixServiceUtil.isSingleINDeductibleFilterApplicable(any(), any()))
				.thenReturn(Boolean.TRUE);

		Mockito.when(planSelectionService.findAppendixReportBenefitPlansBy(any(), Mockito.anyString(), any(), any(), Mockito.anyBoolean())).thenReturn(createPlanAppendixDataByPlanType());
		Mockito.when(realmPlyrPlanService.getMapForRealmPlanYear(Mockito.anyLong())).thenReturn(new HashedMap<>());
		Mockito.when(planRatesService.getBenefitPlanRatesBy(any())).thenReturn(new HashMap<>());
		Mockito.lenient().when(planCompareService.getPlanAttributes(any(), any(), any(),
				any())).thenReturn(CompletableFuture.completedFuture(getDentalBenefitPlanComapre("10")));

		Mockito.when(httpRequest.getAttribute(BSSApplicationConstants.PLAN_APPENDIX_FIRST_BEN_TYPE)).thenReturn("10");

		Map<String, PlanAppendix> planAppendixMap = planAppendixService.getPlanAppendixData(company, prospectRequest,
				httpRequest, additionalBenefitPlanCompareDataFuture);

		assertNotNull(planAppendixMap);

		PlanAppendix planAppendix = planAppendixMap.get("10");
        if (planAppendix != null) {
		    assertEquals((Integer) (BSSApplicationConstants.PLAN_APPENDIX_MAX_LINES_FIRST_PAGE - 4), planAppendix.getMaxLinesFirstPage());
		    assertEquals((Integer) BSSApplicationConstants.PLAN_APPENDIX_MAX_LINES_SUBSEQUENT_PAGES, planAppendix.getMaxLinesSubsequentPages());

		    boolean hasPageBreak = planAppendix.getPlanAttributes().stream().anyMatch(PlanAttribute::isPageBreak);
		    assertFalse(hasPageBreak);
        }
	}

	@Test
	public void sortPlanAttributeByCarrierRegionAndCost() {
		PlanAttribute p1 = new PlanAttribute(); p1.setCarrierName("Aetna"); p1.setRegion("CA");p1.setPlanName("Aetna Dental");
		PlanAttribute p2 = new PlanAttribute(); p2.setCarrierName("Aetna"); p2.setRegion("CA"); p2.setPlanName("Aetna Dental");
		p2.setPlanRates(new com.trinet.ambis.rest.controllers.dto.outputs.CvgLvlPlanInfo()); p2.getPlanRates().setEmployeeOnly("$50");
		PlanAttribute p3 = new PlanAttribute(); p3.setCarrierName("Blue"); p3.setRegion("FL"); p3.setPlanName("Aetna Dental");

		List<PlanAttribute> list = Arrays.asList(p3, p1, p2);
		List<PlanAttribute> sorted = ReflectionTestUtils.invokeMethod(planAppendixService, "sortPlanAttributeByCarrierRegionAndCost", list);

		assertEquals("Aetna", sorted.get(0).getCarrierName());
		assertEquals("CA", sorted.get(0).getRegion());
		// p1 has cost 0, p2 has cost 50, so p1 should come before p2
		assertEquals(null, sorted.get(0).getPlanRates());

		assertEquals("Aetna", sorted.get(1).getCarrierName());
		assertEquals("50", sorted.get(1).getPlanRates().getEmployeeOnly().replaceAll("[^\\d.]", ""));

		assertEquals("Blue", sorted.get(2).getCarrierName());
	}
}
