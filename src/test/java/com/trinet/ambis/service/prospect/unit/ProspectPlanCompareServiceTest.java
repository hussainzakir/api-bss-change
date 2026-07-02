package com.trinet.ambis.service.prospect.unit;

import static com.trinet.ambis.common.BSSApplicationConstants.DENTAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.MEDICAL_PLAN_TYPE;
import static com.trinet.ambis.common.BSSApplicationConstants.VISION_PLAN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.PlanCompareHelper;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.projections.PlanSelectionView;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.TierRate;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.BenefitsPlanViewService;
import com.trinet.ambis.service.BplService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.RlRegionPlan1Service;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes.BenefitPlan;
import com.trinet.ambis.service.prospect.enums.BenefitTypeEnum;
import com.trinet.ambis.service.prospect.impl.ProspectPlanCompareServiceImpl;
import com.trinet.ambis.service.prospect.service.ProspectBenefitsPlansRatesService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

@RunWith(JUnit4.class)
public class ProspectPlanCompareServiceTest {

	ProspectPlanCompareServiceImpl prospectPlanCompareService;
	@Mock
	private ProspectEmployeeService prospectEmployeeService;
	@Mock
	private EmployeePlanAssignmentService emplPlanAssignmentService;
	@Mock
	private BenefitPlanService benPlanService;
	@Mock
	private RealmPlyrPlanService realmPlyrPlanService;
	@Mock
	private BenefitsPlanViewService benefitsPlanViewService;
	@Mock
	private BplService bplService;
	@Mock
	private PlanRatesService planRatesService;
	@Mock
	private StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;
	
	@Mock
	private PlanCompareService planCompareService;

	@Mock
	private ProspectBenefitsPlansRatesService prospectBenefitsPlansRatesService;
	
	@Mock
	private RlRegionPlan1Service rlRegionPlan1Service;
	
	@Mock
	private HttpServletRequest httpRequest;
	
	@Captor
	private ArgumentCaptor<List<String>> planIdsCaptor;

	@Captor
	private ArgumentCaptor<String> companyCodeCaptor;
	
	private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
	@Mock
	private AppRulesConfigService appRulesConfigService;



	@Before
	public void setUp() {
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
        MockitoAnnotations.openMocks(this);
        mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		// Ensure util has a config service to avoid NPEs even across threads
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(new HashMap<>());
		AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
        prospectPlanCompareService = new ProspectPlanCompareServiceImpl(prospectEmployeeService,
				emplPlanAssignmentService, benPlanService, realmPlyrPlanService, planRatesService,
				strategyGroupPlanSelectDao, prospectBenefitsPlansRatesService, rlRegionPlan1Service);

		if (mockStaticAppRulesAndConfigsUtils == null) {
			mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		}
	}

	@After
	public void tearDown() {
		if (mockStaticAppRulesAndConfigsUtils != null) {
			mockStaticAppRulesAndConfigsUtils.close();
		}

	}

	// @formatter:off
	// Given 1. there are 3 employees enrolled in following current and TriNet plans
	// ------------------------------------------------------------
	// |      Empl ID        |   Current Plan  |  TriNet Plan     |
	// ------------------------------------------------------------
	// | EMPL1-KSJLKASJDLJAD |    1  (med)     |    MEDPLAN1      |
	// | EMPL1-KSJLKASJDLJAD |    3  (den)     |    DENPLAN1      |
	// | EMPL1-KSJLKASJDLJAD |    5  (vis)     |    VISPLAN1      |
	// ------------------------------------------------------------
	// | EMPL2-JKFHJSHFDJKHF |    2  (med)     |    MEDPLAN2      |
 	// | EMPL2-JKFHJSHFDJKHF |    4  (den)     |    DENPLAN2	  |
	// | EMPL2-JKFHJSHFDJKHF |	  6  (vis)     |    VISPLAN2	  |
	// ------------------------------------------------------------
	// | EMPL3-KJKLDFJKJJDKK |    6  (vis)     |    VISPLAN3	  |
    // ------------------------------------------------------------
	// Given 2. there is a base plan(REGMEDPLAN1) for regional plan MEDPLAN1
	//
	// Then the output of plan comparison should be
	//
	// There should be 3 DTOs returned each for Medical, Dental and Vision ben type.
	// Each DTO should have following current to TriNet plan mapping
	// and base REGMEDPLAN1 should be pulled for MEDPLAN1
	// ------------------------------------------------------------
	// |   Plan Type  |   Current Plan  |  TriNet Plan     |
	// ------------------------------------------------------------
	// |     MED      |    1  (med)     |    REGMEDPLAN1   |
	// |     MED      |    2  (med)     |    MEDPLAN2      |
	// |     DEN      |    3  (den)     |    DENPLAN1      |
	// |     DEN      |    4  (den)     |    DENPLAN2      |
	// |     VIS      |    5  (vis)     |    VISPLAN1      |
	// |     VIS      |    6  (vis)     | VISPLAN2,VISPLAN3|
	// @formatter:on
	@Test
	public void getPlanCompareDetailsTest1() {
		// Given
		List<Long> trinetStrategyIds = Arrays.asList(1111L, 2222L);
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		ArgumentCaptor<Set<String>> setArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<HttpServletRequest> httpReqCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);

		Company company = prepareCompany();
		when(prospectEmployeeService.getEmployeePlans(company.getCode())).thenReturn(prepareCurrentEmployeePlans());
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(trinetStrategyIds))
				.thenReturn(prepareTriNetEmplPlanAssignment());
		when(realmPlyrPlanService.getMapForRealmPlanYear(72)).thenReturn(prepareRealmPlanMap());
		when(benPlanService.getRegionalBasePlanMapping(company.getRealmPlanYear()))
				.thenReturn(prepareRegionalBasePlanMapping());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_MEDICAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentMedicalPlanResponse()).thenReturn(prepareFutureMedicalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_DENTAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentDentalPlanResponse()).thenReturn(prepareFutureDentalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_VISION), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentVisionPlanResponse()).thenReturn(prepareFutureVisionPlanResponse());
		when(prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture())).thenReturn(Collections.emptyMap());
		when(rlRegionPlan1Service.findByRealmPlanYearId(72)).thenReturn(Collections.emptyMap());
		when(strategyGroupPlanSelectDao.findDistinctBenefitPlanPlanTypeByStrategyIdIn(trinetStrategyIds))
				.thenReturn(preparePlanSelections());
		PlanCompareHelper.setPlanCompareService(planCompareService);

		// When
		List<PlanCompareDetailDto> actualResult = prospectPlanCompareService.getPlanCompareDetails(company,
				trinetStrategyIds, httpRequest);

		// Then
		boolean isMedicalAsserted = false, isDentalAsserted = false, isVisionAsserted = false;
		assertEquals(3, actualResult.size());

		for (PlanCompareDetailDto resultDto : actualResult) {
			if (MEDICAL_PLAN_TYPE.equals(resultDto.getBenefitType())) {
				isMedicalAsserted = true;
				assertEquals(3, resultDto.getPlanCompareData().size());

				PlanCompareDetailDto.PlanCompareData planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001002"))
						.collect(Collectors.toList()).get(0);
				assertEquals("Current Med Plan 1", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(1, planCompareData.getFuturePlans().size());
				assertEquals("MEDPLAN1", planCompareData.getFuturePlans().get(0).getPlanId());
				assertEquals("Regional Base Med Plan 1", planCompareData.getFuturePlans().get(0).getPlanName());

				planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001003"))
						.collect(Collectors.toList()).get(0);
				assertEquals("Current Med Plan 2", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(1, planCompareData.getFuturePlans().size());
				assertEquals("MEDPLAN2", planCompareData.getFuturePlans().get(0).getPlanId());
				assertEquals("MED PLAN 2", planCompareData.getFuturePlans().get(0).getPlanName());

				planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "10_NO_PLAN_ID"))
						.collect(Collectors.toList()).get(0);
				assertEquals("No Plan", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(0, planCompareData.getCurrentPlan().getAttributes().size());
				assertNull(planCompareData.getCurrentPlan().getRates());
				assertEquals(0, planCompareData.getFuturePlans().size());
			} else if (DENTAL_PLAN_TYPE.equals(resultDto.getBenefitType())) {
				isDentalAsserted = true;
				assertEquals(3, resultDto.getPlanCompareData().size());

				PlanCompareDetailDto.PlanCompareData planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001004"))
						.collect(Collectors.toList()).get(0);
				assertEquals("Current Den Plan 1", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(1, planCompareData.getFuturePlans().size());
				assertEquals("DENPLAN1", planCompareData.getFuturePlans().get(0).getPlanId());
				assertEquals("DEN PLAN 1", planCompareData.getFuturePlans().get(0).getPlanName());

				planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001005"))
						.collect(Collectors.toList()).get(0);
				assertEquals("Current Den Plan 2", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(1, planCompareData.getFuturePlans().size());
				assertEquals("DENPLAN2", planCompareData.getFuturePlans().get(0).getPlanId());
				assertEquals("DEN PLAN 2", planCompareData.getFuturePlans().get(0).getPlanName());

				planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "11_NO_PLAN_ID"))
						.collect(Collectors.toList()).get(0);
				assertEquals("No Plan", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(0, planCompareData.getCurrentPlan().getAttributes().size());
				assertNull(planCompareData.getCurrentPlan().getRates());
				assertEquals(0, planCompareData.getFuturePlans().size());
			} else if (VISION_PLAN_TYPE.equals(resultDto.getBenefitType())) {
				isVisionAsserted = true;
				assertEquals(2, resultDto.getPlanCompareData().size());

				PlanCompareDetailDto.PlanCompareData planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001006"))
						.collect(Collectors.toList()).get(0);
				assertEquals("Current Vis Plan 1", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(1, planCompareData.getFuturePlans().size());
				assertEquals("VISPLAN1", planCompareData.getFuturePlans().get(0).getPlanId());
				assertEquals("VIS PLAN 1", planCompareData.getFuturePlans().get(0).getPlanName());

				planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001007"))
						.collect(Collectors.toList()).get(0);
				assertEquals("Current Vis Plan 2", planCompareData.getCurrentPlan().getPlanName());
				assertEquals(2, planCompareData.getFuturePlans().size());
				assertEquals("VISPLAN2", planCompareData.getFuturePlans().get(0).getPlanId());
				assertEquals("VIS PLAN 2", planCompareData.getFuturePlans().get(0).getPlanName());
				assertEquals("VISPLAN3", planCompareData.getFuturePlans().get(1).getPlanId());
				assertEquals("VIS PLAN 3", planCompareData.getFuturePlans().get(1).getPlanName());
			}
		}
		assertTrue(isMedicalAsserted);
		assertTrue(isDentalAsserted);
		assertTrue(isVisionAsserted);
		// verify
		verify(prospectEmployeeService, times(1)).getEmployeePlans(company.getCode());
		verify(emplPlanAssignmentService, times(1)).getEmployeePlanAssigmentBy(Arrays.asList(1111L, 2222L));
		verify(realmPlyrPlanService, times(1)).getMapForRealmPlanYear(72);
		verify(benPlanService, times(1)).getRegionalBasePlanMapping(company.getRealmPlanYear());
		verify(planCompareService, times(2)).getPlanAttributes(org.mockito.ArgumentMatchers.anySet(),
				org.mockito.ArgumentMatchers.any(java.util.Date.class),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_VISION), eq(httpRequest));
		verify(prospectBenefitsPlansRatesService, times(1)).getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture());
		verify(rlRegionPlan1Service, times(1)).findByRealmPlanYearId(72);
	}

	/**
	 * given rate details present of rthe current plans </br>
	 * when getPlanCompareDetails is called </br>
	 * then return rates for the current plan
	 **/
	@Test
	public void getPlanCompareDetailsTest2() {
		// given
		// data
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		ArgumentCaptor<Set<String>> setArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<HttpServletRequest> httpReqCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
		Company company = prepareCompany();
		// method mocks
		when(prospectEmployeeService.getEmployeePlans(company.getCode())).thenReturn(prepareCurrentEmployeePlans());
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(Arrays.asList(1111L, 2222L)))
				.thenReturn(prepareTriNetEmplPlanAssignment());
		when(realmPlyrPlanService.getMapForRealmPlanYear(72)).thenReturn(prepareRealmPlanMap());
		when(benPlanService.getRegionalBasePlanMapping(company.getRealmPlanYear()))
				.thenReturn(prepareRegionalBasePlanMapping());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_MEDICAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentMedicalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_DENTAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentDentalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_VISION), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentVisionPlanResponse());
		when(planCompareService.getPlanAttributes(
				Set.of("REGMEDPLAN1", "MEDPLAN1", "MEDPLAN2", "DENPLAN1", "DENPLAN2", "VISPLAN1", "VISPLAN2", "VISPLAN3"),
				company.getRealmPlanYear().getPlanYearEnd(), BSSApplicationConstants.PROSPECT_PLAN_EXPORT_TEMPLATE,
				httpRequest)).thenReturn(prepareFuturePlanResponse());
		when(prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture())).thenReturn(prepareBPLRateDetails());
		when(rlRegionPlan1Service.findByRealmPlanYearId(72)).thenReturn(Collections.emptyMap());
		PlanCompareHelper.setPlanCompareService(planCompareService);
		// when
		List<PlanCompareDetailDto> actualResult = prospectPlanCompareService.getPlanCompareDetails(company,
				List.of(1111L, 2222L), httpRequest);
		// then
		// assertions
		assertEquals(3, actualResult.size());
		for (PlanCompareDetailDto resultDto : actualResult) {
			if (MEDICAL_PLAN_TYPE.equals(resultDto.getBenefitType())) {
				PlanCompareDetailDto.PlanCompareData planCompareData = resultDto.getPlanCompareData().stream()
						.filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001002"))
						.collect(Collectors.toList()).get(0);
				RateDetail rateDetail = planCompareData.getCurrentPlan().getRates().get(0);
				assertEquals(1, rateDetail.getRegionCode().size());
				assertEquals("ALL", rateDetail.getRegionCode().get(0));
				assertEquals("ageBanded", rateDetail.getRateType());
				List<TierRate> tierRates = rateDetail.getTierRates();
				assertEquals(2, tierRates.size());
				assertEquals("15", tierRates.get(0).getCvgTierCode());
				assertEquals(new BigDecimal(500.55), tierRates.get(0).getCost());
				assertEquals("60", tierRates.get(1).getCvgTierCode());
				assertEquals(new BigDecimal(600.66), tierRates.get(1).getCost());
			} else if (DENTAL_PLAN_TYPE.equals(resultDto.getBenefitType())) {
				RateDetail rateDetail = resultDto.getPlanCompareData().get(0).getCurrentPlan().getRates().get(0);
				assertEquals(1, rateDetail.getRegionCode().size());
				assertEquals("ALL", rateDetail.getRegionCode().get(0));
				assertEquals("4Tier", rateDetail.getRateType());
				List<TierRate> tierRates = rateDetail.getTierRates();
				assertEquals(4, tierRates.size());
				assertEquals("1", tierRates.get(0).getCvgTierCode());
				assertEquals(new BigDecimal(100.11), tierRates.get(0).getCost());
				assertEquals("2", tierRates.get(1).getCvgTierCode());
				assertEquals(new BigDecimal(200.22), tierRates.get(1).getCost());
				assertEquals("C", tierRates.get(2).getCvgTierCode());
				assertEquals(new BigDecimal(300.33), tierRates.get(2).getCost());
				assertEquals("4", tierRates.get(3).getCvgTierCode());
				assertEquals(new BigDecimal(400.44), tierRates.get(3).getCost());
			} else if (VISION_PLAN_TYPE.equals(resultDto.getBenefitType())) {
				RateDetail rateDetail = resultDto.getPlanCompareData().get(0).getCurrentPlan().getRates().get(0);
				assertEquals(1, rateDetail.getRegionCode().size());
				assertEquals("ALL", rateDetail.getRegionCode().get(0));
				assertEquals("ageBanded", rateDetail.getRateType());
				List<TierRate> tierRates = rateDetail.getTierRates();
				assertEquals(2, tierRates.size());
				assertEquals("20", tierRates.get(0).getCvgTierCode());
				assertEquals(new BigDecimal(700.55), tierRates.get(0).getCost());
				assertEquals("55", tierRates.get(1).getCvgTierCode());
				assertEquals(new BigDecimal(800.66), tierRates.get(1).getCost());
			}
		}
		// verify
		verify(prospectEmployeeService, times(1)).getEmployeePlans(company.getCode());
		verify(emplPlanAssignmentService, times(1)).getEmployeePlanAssigmentBy(Arrays.asList(1111L, 2222L));
		verify(realmPlyrPlanService, times(1)).getMapForRealmPlanYear(72);
		verify(benPlanService, times(1)).getRegionalBasePlanMapping(company.getRealmPlanYear());
		verify(prospectBenefitsPlansRatesService, times(1)).getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture());
		verify(rlRegionPlan1Service, times(1)).findByRealmPlanYearId(72);
	}
	
	@Test
	public void getBplPlanCompareDetailsTest() {
		// Given
		List<Long> trinetStrategyIds = Arrays.asList(1111L, 2222L);
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		ArgumentCaptor<Set<String>> setArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<HttpServletRequest> httpReqCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);

		Company company = prepareCompany();
		when(prospectEmployeeService.getEmployeePlans(company.getCode())).thenReturn(prepareCurrentEmployeePlans());
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(trinetStrategyIds))
				.thenReturn(prepareTriNetEmplPlanAssignment());
		when(realmPlyrPlanService.getMapForRealmPlanYear(72)).thenReturn(prepareRealmPlanMap());
		when(benPlanService.getRegionalBasePlanMapping(company.getRealmPlanYear()))
				.thenReturn(prepareRegionalBasePlanMapping());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_MEDICAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentMedicalPlanResponse()).thenReturn(prepareFutureMedicalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_DENTAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentDentalPlanResponse()).thenReturn(prepareFutureDentalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_VISION), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentVisionPlanResponse()).thenReturn(prepareFutureVisionPlanResponse());
		when(prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture())).thenReturn(Collections.emptyMap());
		when(rlRegionPlan1Service.findByRealmPlanYearId(72)).thenReturn(Collections.emptyMap());
		when(strategyGroupPlanSelectDao.findDistinctBenefitPlanPlanTypeByStrategyIdIn(trinetStrategyIds))
				.thenReturn(preparePlanSelections());
		PlanCompareHelper.setPlanCompareService(planCompareService);

		// When
		prospectPlanCompareService.getPlanCompareDetails(company, trinetStrategyIds, httpRequest);

		// verify
		verify(prospectEmployeeService, times(1)).getEmployeePlans(company.getCode());
		verify(emplPlanAssignmentService, times(1)).getEmployeePlanAssigmentBy(Arrays.asList(1111L, 2222L));
		verify(realmPlyrPlanService, times(1)).getMapForRealmPlanYear(72);
		verify(benPlanService, times(1)).getRegionalBasePlanMapping(company.getRealmPlanYear());
		verify(prospectBenefitsPlansRatesService, times(1)).getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture());
		verify(rlRegionPlan1Service, times(1)).findByRealmPlanYearId(72);
	}
	
	private CompletableFuture<List<BenefitPlanCompare>> prepareBPLCurrentPlanResponse() {
		BenefitPlanCompare benefitPlanCompare = preparePlanCompare("ae00001002", "Current Med Plan 1",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare1 = preparePlanCompare("ae00001003", "Current Med Plan 2",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare2 = preparePlanCompare("ae00001004", "Current Den Plan 1",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare3 = preparePlanCompare("ae00001005", "Current Den Plan 2",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare4 = preparePlanCompare("ae00001006", "Current Vis Plan 1",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare5 = preparePlanCompare("ae00001007", "Current Vis Plan 2",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		return CompletableFuture.completedFuture(Arrays.asList(benefitPlanCompare, benefitPlanCompare1,
				benefitPlanCompare2, benefitPlanCompare3, benefitPlanCompare4, benefitPlanCompare5));
	}

    @Test
    public void getPlanCompareDetailsV2EnabledTest() {
        // Given
        List<Long> trinetStrategyIds = Arrays.asList(1111L, 2222L);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Set<String>> setArgCaptor = ArgumentCaptor.forClass(Set.class);
        ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpServletRequest> httpReqCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);

        Company company = prepareCompany();
        when(prospectEmployeeService.getEmployeePlans(company.getCode())).thenReturn(prepareCurrentEmployeePlans());
        when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(trinetStrategyIds))
                .thenReturn(prepareTriNetEmplPlanAssignment());
        when(realmPlyrPlanService.getMapForRealmPlanYear(72)).thenReturn(prepareRealmPlanMap());
        when(benPlanService.getRegionalBasePlanMapping(company.getRealmPlanYear()))
                .thenReturn(prepareRegionalBasePlanMapping());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_MEDICAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentMedicalPlanResponse()).thenReturn(prepareFutureMedicalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_DENTAL), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentDentalPlanResponse()).thenReturn(prepareFutureDentalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_VISION), httpReqCaptor.capture()))
				.thenReturn(prepareBPLCurrentVisionPlanResponse()).thenReturn(prepareFutureVisionPlanResponse());
        when(prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIdsCaptor.capture(),
                companyCodeCaptor.capture())).thenReturn(Collections.emptyMap());
        when(rlRegionPlan1Service.findByRealmPlanYearId(72)).thenReturn(Collections.emptyMap());
        when(strategyGroupPlanSelectDao.findDistinctBenefitPlanPlanTypeByStrategyIdIn(trinetStrategyIds))
                .thenReturn(preparePlanSelections());
        PlanCompareHelper.setPlanCompareService(planCompareService);
        // When
        List<PlanCompareDetailDto> actualResult = prospectPlanCompareService.getPlanCompareDetails(company,
                trinetStrategyIds, httpRequest);

        // Then
        boolean isMedicalAsserted = false, isDentalAsserted = false, isVisionAsserted = false;
        assertEquals(3, actualResult.size());

        for (PlanCompareDetailDto resultDto : actualResult) {
            if (MEDICAL_PLAN_TYPE.equals(resultDto.getBenefitType())) {
                isMedicalAsserted = true;
                assertEquals(3, resultDto.getPlanCompareData().size());

                PlanCompareDetailDto.PlanCompareData planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001002"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("Current Med Plan 1", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(1, planCompareData.getFuturePlans().size());
                assertEquals("MEDPLAN1", planCompareData.getFuturePlans().get(0).getPlanId());
                assertEquals("Regional Base Med Plan 1", planCompareData.getFuturePlans().get(0).getPlanName());

                planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001003"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("Current Med Plan 2", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(1, planCompareData.getFuturePlans().size());
                assertEquals("MEDPLAN2", planCompareData.getFuturePlans().get(0).getPlanId());
                assertEquals("MED PLAN 2", planCompareData.getFuturePlans().get(0).getPlanName());

                planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "10_NO_PLAN_ID"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("No Plan", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(0, planCompareData.getCurrentPlan().getAttributes().size());
                assertNull(planCompareData.getCurrentPlan().getRates());
                assertEquals(0, planCompareData.getFuturePlans().size());
            } else if (DENTAL_PLAN_TYPE.equals(resultDto.getBenefitType())) {
                isDentalAsserted = true;
                assertEquals(3, resultDto.getPlanCompareData().size());

                PlanCompareDetailDto.PlanCompareData planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001004"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("Current Den Plan 1", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(1, planCompareData.getFuturePlans().size());
                assertEquals("DENPLAN1", planCompareData.getFuturePlans().get(0).getPlanId());
                assertEquals("DEN PLAN 1", planCompareData.getFuturePlans().get(0).getPlanName());

                planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001005"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("Current Den Plan 2", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(1, planCompareData.getFuturePlans().size());
                assertEquals("DENPLAN2", planCompareData.getFuturePlans().get(0).getPlanId());
                assertEquals("DEN PLAN 2", planCompareData.getFuturePlans().get(0).getPlanName());

                planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "11_NO_PLAN_ID"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("No Plan", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(0, planCompareData.getCurrentPlan().getAttributes().size());
                assertNull(planCompareData.getCurrentPlan().getRates());
                assertEquals(0, planCompareData.getFuturePlans().size());
            } else if (VISION_PLAN_TYPE.equals(resultDto.getBenefitType())) {
                isVisionAsserted = true;
                assertEquals(2, resultDto.getPlanCompareData().size());

                PlanCompareDetailDto.PlanCompareData planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001006"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("Current Vis Plan 1", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(1, planCompareData.getFuturePlans().size());
                assertEquals("VISPLAN1", planCompareData.getFuturePlans().get(0).getPlanId());
                assertEquals("VIS PLAN 1", planCompareData.getFuturePlans().get(0).getPlanName());

                planCompareData = resultDto.getPlanCompareData().stream()
                        .filter(a -> Objects.equals(a.getCurrentPlan().getPlanId(), "ae00001007"))
                        .collect(Collectors.toList()).get(0);
                assertEquals("Current Vis Plan 2", planCompareData.getCurrentPlan().getPlanName());
                assertEquals(2, planCompareData.getFuturePlans().size());
                assertEquals("VISPLAN2", planCompareData.getFuturePlans().get(0).getPlanId());
                assertEquals("VIS PLAN 2", planCompareData.getFuturePlans().get(0).getPlanName());
                assertEquals("VISPLAN3", planCompareData.getFuturePlans().get(1).getPlanId());
                assertEquals("VIS PLAN 3", planCompareData.getFuturePlans().get(1).getPlanName());
            }
        }
        assertTrue(isMedicalAsserted);
        assertTrue(isDentalAsserted);
        assertTrue(isVisionAsserted);
        // verify
        verify(prospectEmployeeService, times(1)).getEmployeePlans(company.getCode());
        verify(emplPlanAssignmentService, times(1)).getEmployeePlanAssigmentBy(Arrays.asList(1111L, 2222L));
        verify(realmPlyrPlanService, times(1)).getMapForRealmPlanYear(72);
        verify(benPlanService, times(1)).getRegionalBasePlanMapping(company.getRealmPlanYear());
		verify(planCompareService, times(2)).getPlanAttributes(org.mockito.ArgumentMatchers.anySet(),
		org.mockito.ArgumentMatchers.any(java.util.Date.class),
		eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_VISION), eq(httpRequest));
        verify(prospectBenefitsPlansRatesService, times(1)).getBenefitsPlansRateDetails(planIdsCaptor.capture(),
                companyCodeCaptor.capture());
        verify(rlRegionPlan1Service, times(1)).findByRealmPlanYearId(72);
    }
    
    /**
	 * given rate details present of for current plans </br>
	 * when getPlanCompareDetails is called </br>
	 * then return rates for the current plan and fetch attributes from BPL
	 **/
	@Test
	public void getPlanCompareDetailsForBPLAttributes() {
		// given
		// data
		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		ArgumentCaptor<Set<String>> setArgCaptor = ArgumentCaptor.forClass(Set.class);
		ArgumentCaptor<String> templateCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<HttpServletRequest> httpReqCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
		Company company = prepareCompany();
		// method mocks
		when(prospectEmployeeService.getEmployeePlans(company.getCode())).thenReturn(prepareCurrentEmployeePlans());
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(Arrays.asList(1111L, 2222L)))
				.thenReturn(prepareTriNetEmplPlanAssignment());
		when(realmPlyrPlanService.getMapForRealmPlanYear(72)).thenReturn(prepareRealmPlanMap());
		when(benPlanService.getRegionalBasePlanMapping(company.getRealmPlanYear()))
				.thenReturn(prepareRegionalBasePlanMapping());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_MEDICAL), httpReqCaptor.capture())).thenReturn(prepareBPLCurrentMedicalPlanResponse())
				.thenReturn(prepareFutureMedicalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_DENTAL), httpReqCaptor.capture())).thenReturn(prepareBPLCurrentDentalPlanResponse())
				.thenReturn(prepareFutureDentalPlanResponse());
		when(planCompareService.getPlanAttributes(setArgCaptor.capture(), dateCaptor.capture(),
				eq(BSSApplicationConstants.PROSPECT_INCUMBENT_PLAN_VISION), httpReqCaptor.capture())).thenReturn(prepareBPLCurrentVisionPlanResponse())
				.thenReturn(prepareFutureVisionPlanResponse());
		when(prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture())).thenReturn(prepareRateDetails());
		when(rlRegionPlan1Service.findByRealmPlanYearId(72)).thenReturn(Collections.emptyMap());
		PlanCompareHelper.setPlanCompareService(planCompareService);
		// when	
		List<PlanCompareDetailDto> actualResult = prospectPlanCompareService.getPlanCompareDetails(company,
				List.of(1111L, 2222L), httpRequest);
		// then
		// assertions
		assertEquals(3, actualResult.size());
		
		// verify
		verify(prospectEmployeeService, times(1)).getEmployeePlans(company.getCode());
		verify(emplPlanAssignmentService, times(1)).getEmployeePlanAssigmentBy(Arrays.asList(1111L, 2222L));
		verify(realmPlyrPlanService, times(1)).getMapForRealmPlanYear(72);
		verify(benPlanService, times(1)).getRegionalBasePlanMapping(company.getRealmPlanYear());
		verify(prospectBenefitsPlansRatesService, times(1)).getBenefitsPlansRateDetails(planIdsCaptor.capture(),
				companyCodeCaptor.capture());
		verify(rlRegionPlan1Service, times(1)).findByRealmPlanYearId(72);
	}

	private Optional<List<EmployeePlansRes>> prepareCurrentEmployeePlans() {
		BenefitPlan medPlan1 = BenefitPlan.builder().benefitPlanId("1").benefitTypeCode(MEDICAL_PLAN_TYPE)
				.bplPlanId("ae00001002").build();
		BenefitPlan medPlan2 = BenefitPlan.builder().benefitPlanId("2").benefitTypeCode(MEDICAL_PLAN_TYPE)
				.bplPlanId("ae00001003").build();

		BenefitPlan denPlan1 = BenefitPlan.builder().benefitPlanId("3").benefitTypeCode(DENTAL_PLAN_TYPE)
				.bplPlanId("ae00001004").build();
		BenefitPlan denPlan2 = BenefitPlan.builder().benefitPlanId("4").benefitTypeCode(DENTAL_PLAN_TYPE)
				.bplPlanId("ae00001005").build();

		BenefitPlan visPlan1 = BenefitPlan.builder().benefitPlanId("5").benefitTypeCode(VISION_PLAN_TYPE)
				.bplPlanId("ae00001006").build();
		BenefitPlan visPlan2 = BenefitPlan.builder().benefitPlanId("6").benefitTypeCode(VISION_PLAN_TYPE)
				.bplPlanId("ae00001007").build();

		EmployeePlansRes empl1 = EmployeePlansRes.builder().employeeId("EMPL1-KSJLKASJDLJAD")
				.benefitPlans(Arrays.asList(medPlan1, denPlan1, visPlan1)).build();

		EmployeePlansRes empl2 = EmployeePlansRes.builder().employeeId("EMPL2-JKFHJSHFDJKHF")
				.benefitPlans(Arrays.asList(medPlan2, denPlan2, visPlan2)).build();

		EmployeePlansRes empl3 = EmployeePlansRes.builder().employeeId("EMPL3-KJKLDFJKJJDKK")
				.benefitPlans(Arrays.asList(visPlan2)).build();

		return Optional.of(Arrays.asList(empl1, empl2, empl3));
	}

	private List<EePlanAssignment> prepareTriNetEmplPlanAssignment() {
		EePlanAssignment empl1Med1 = prepareTriNetEmplPlan(1111, "MEDPLAN1", "EMPL1-KSJLKASJDLJAD", "" ,"","10");
		EePlanAssignment empl1Den1 = prepareTriNetEmplPlan(1111, "DENPLAN1", "EMPL1-KSJLKASJDLJAD", "" ,"","11");
		EePlanAssignment empl1Vis1 = prepareTriNetEmplPlan(1111, "VISPLAN1", "EMPL1-KSJLKASJDLJAD", "" ,"","14");

		EePlanAssignment empl2Med2 = prepareTriNetEmplPlan(2222, "MEDPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,"","10");
		EePlanAssignment empl2Dev2 = prepareTriNetEmplPlan(2222, "DENPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,"","11");
		EePlanAssignment empl2Vis2 = prepareTriNetEmplPlan(2222, "VISPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,"","14");

		EePlanAssignment empl3Vis3 = prepareTriNetEmplPlan(2222, "VISPLAN3", "EMPL3-KJKLDFJKJJDKK", "" ,"","14");
		return Arrays.asList(empl1Med1, empl1Den1, empl1Vis1, empl2Med2, empl2Dev2, empl2Vis2, empl3Vis3);
	}

	private EePlanAssignment prepareTriNetEmplPlan(int strategyId, String benPlanId, String emplId, String covrgCD, String portfolioID, String benefitType) {
		EePlanAssignment empl = EePlanAssignment.builder().build();
		EePlanAssignmentPK pk = new EePlanAssignmentPK(strategyId, emplId, benefitType);
		empl.setCovrgCD(covrgCD);
		empl.setBenefitPlan(benPlanId);
		empl.setEePlanAssignmentPK(pk);
		return empl;

	}

	private Map<String, com.trinet.ambis.service.model.plancompare.BenefitPlan> prepareRegionalBasePlanMapping() {
		Map<String, com.trinet.ambis.service.model.plancompare.BenefitPlan> basePlanMappings = new HashMap<>();
		com.trinet.ambis.service.model.plancompare.BenefitPlan rbp = new com.trinet.ambis.service.model.plancompare.BenefitPlan(
				"MEDPLAN1", "Regional Base Med Plan 1");
		basePlanMappings.put("MEDPLAN1", rbp);
		rbp = new com.trinet.ambis.service.model.plancompare.BenefitPlan("REGMEDPLAN1", "Regional Base Med Plan 1");
		basePlanMappings.put("REGMEDPLAN1", rbp);
		rbp = new com.trinet.ambis.service.model.plancompare.BenefitPlan("VISPLAN1", "VIS PLAN 1");
		basePlanMappings.put("VISPLAN1", rbp);
		return basePlanMappings;
	}

	private Map<String, XbssRealmPlyrPlan> prepareRealmPlanMap() {
		Map<String, XbssRealmPlyrPlan> rpps = new HashMap<>();

		XbssRealmPlyrPlan rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("MEDPLAN1");
		rpp.setPlanType(MEDICAL_PLAN_TYPE);
		rpps.put("MEDPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("MEDPLAN2");
		rpp.setPlanType(MEDICAL_PLAN_TYPE);
		rpps.put("MEDPLAN2", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("REGMEDPLAN1");
		rpp.setPlanType(MEDICAL_PLAN_TYPE);
		rpps.put("REGMEDPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("DENPLAN1");
		rpp.setPlanType(DENTAL_PLAN_TYPE);
		rpps.put("DENPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("DENPLAN2");
		rpp.setPlanType(DENTAL_PLAN_TYPE);
		rpps.put("DENPLAN2", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("VISPLAN1");
		rpp.setPlanType(VISION_PLAN_TYPE);
		rpps.put("VISPLAN1", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("VISPLAN2");
		rpp.setPlanType(VISION_PLAN_TYPE);
		rpps.put("VISPLAN2", rpp);

		rpp = new XbssRealmPlyrPlan();
		rpp.setBenefitPlan("VISPLAN3");
		rpp.setPlanType(VISION_PLAN_TYPE);
		rpps.put("VISPLAN3", rpp);

		return rpps;
	}

	private CompletableFuture<List<BenefitPlanCompare>> prepareCurrentPlanResponse() {
		BenefitPlanCompare benefitPlanCompare = preparePlanCompare("1", "Current Med Plan 1",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare1 = preparePlanCompare("2", "Current Med Plan 2",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare2 = preparePlanCompare("3", "Current Den Plan 1",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare3 = preparePlanCompare("4", "Current Den Plan 2",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare4 = preparePlanCompare("5", "Current Vis Plan 1",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare5 = preparePlanCompare("6", "Current Vis Plan 2",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		return CompletableFuture.completedFuture(Arrays.asList(benefitPlanCompare, benefitPlanCompare1,
				benefitPlanCompare2, benefitPlanCompare3, benefitPlanCompare4, benefitPlanCompare5));
	}

	private CompletableFuture<List<BenefitPlanCompare>> prepareFuturePlanResponse() {
		BenefitPlanCompare benefitPlanCompare = preparePlanCompare("REGMEDPLAN1", "REG MEDPLAN 1",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare1 = preparePlanCompare("MEDPLAN1", "MED PLAN 1",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare2 = preparePlanCompare("MEDPLAN2", "MED PLAN 2",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare3 = preparePlanCompare("DENPLAN1", "DEN PLAN 1",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare4 = preparePlanCompare("DENPLAN2", "DEN PLAN 2",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare5 = preparePlanCompare("VISPLAN1", "VIS PLAN 1",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare6 = preparePlanCompare("VISPLAN2", "VIS PLAN 2",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare7 = preparePlanCompare("VISPLAN3", "VIS PLAN 3",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		return CompletableFuture
				.completedFuture(Arrays.asList(benefitPlanCompare, benefitPlanCompare1, benefitPlanCompare2,
						benefitPlanCompare3, benefitPlanCompare4, benefitPlanCompare5, benefitPlanCompare6, benefitPlanCompare7));
	}

	private BenefitPlanCompare preparePlanCompare(String planId, String planName, String planType) {
		List<PlanCompareTemplate> templates = prepareAttrPlanTemplate();
		BenefitPlanCompare planCompare = new BenefitPlanCompare();
		planCompare.setPlanId(planId);
		planCompare.setName(planName);
		planCompare.setCarrier("Aetna");
		planCompare.setBenefitType(planType);
		planCompare.setTemplate(templates);
		return planCompare;
	}

	private List<PlanCompareTemplate> prepareAttrPlanTemplate() {
		List<PlanCompareTemplate> templates = new ArrayList<>();
		PlanCompareTemplate template = new PlanCompareTemplate();
		template.setName("attr1");
		Attribute attr = new Attribute();
		attr.setDisplayName("Attribute 1");
		attr.setName("Attribute 1");
		attr.setValue("Value");
		template.setChildren(Arrays.asList(attr));
		templates.add(template);
		return templates;
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode("FJDKSJFKL12313JHDDFD");
		company.setId(12345);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(72);
		rpy.setPlanYearEnd(new java.util.Date());
		company.setRealmPlanYear(rpy);
		return company;
	}

	private Map<String, RateDetail> prepareRateDetails() {
		return Map.of("1",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(TierRate.builder().cvgTierCode("15").cost(new BigDecimal(500.55)).build(),
								TierRate.builder().cvgTierCode("60").cost(new BigDecimal(600.66)).build()))
						.build(),
				"3",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("4Tier")
						.tierRates(List.of(TierRate.builder().cvgTierCode("1").cost(new BigDecimal(100.11)).build(),
								TierRate.builder().cvgTierCode("2").cost(new BigDecimal(200.22)).build(),
								TierRate.builder().cvgTierCode("C").cost(new BigDecimal(300.33)).build(),
								TierRate.builder().cvgTierCode("4").cost(new BigDecimal(400.44)).build()))
						.build(),
				"5",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(TierRate.builder().cvgTierCode("20").cost(new BigDecimal(700.55)).build(),
								TierRate.builder().cvgTierCode("55").cost(new BigDecimal(800.66)).build()))
						.build());
	}

	private List<PlanSelectionView> preparePlanSelections() {
		PlanSelectionView planSelectionView = new PlanSelectionView();
		planSelectionView.setBenefitPlan("VISPLAN1");
		planSelectionView.setPlanType(VISION_PLAN_TYPE);
		return Arrays.asList(planSelectionView);
	}
	
	private CompletableFuture<List<BenefitPlanCompare>> prepareFutureMedicalPlanResponse() {
		BenefitPlanCompare benefitPlanCompare = preparePlanCompare("REGMEDPLAN1", "REG MEDPLAN 1",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare1 = preparePlanCompare("MEDPLAN1", "MED PLAN 1",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare2 = preparePlanCompare("MEDPLAN2", "MED PLAN 2",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		return CompletableFuture.completedFuture(Arrays.asList(benefitPlanCompare, benefitPlanCompare1, benefitPlanCompare2));
	}

	private CompletableFuture<List<BenefitPlanCompare>> prepareFutureDentalPlanResponse() {
		BenefitPlanCompare benefitPlanCompare2 = preparePlanCompare("DENPLAN1", "DEN PLAN 1",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare3 = preparePlanCompare("DENPLAN2", "DEN PLAN 2",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		return CompletableFuture.completedFuture(Arrays.asList(benefitPlanCompare2, benefitPlanCompare3));
	}

	private CompletableFuture<List<BenefitPlanCompare>> prepareFutureVisionPlanResponse() {
		BenefitPlanCompare benefitPlanCompare4 = preparePlanCompare("VISPLAN1", "VIS PLAN 1",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare5 = preparePlanCompare("VISPLAN2", "VIS PLAN 2",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare6 = preparePlanCompare("VISPLAN3", "VIS PLAN 3",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		return CompletableFuture
				.completedFuture(Arrays.asList(benefitPlanCompare4, benefitPlanCompare5, benefitPlanCompare6));
	}

	private CompletableFuture<List<BenefitPlanCompare>> prepareBPLCurrentMedicalPlanResponse() {
		BenefitPlanCompare benefitPlanCompare = preparePlanCompare("ae00001002", "Current Med Plan 1",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare1 = preparePlanCompare("ae00001003", "Current Med Plan 2",
				BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc());
		return CompletableFuture.completedFuture(Arrays.asList(benefitPlanCompare, benefitPlanCompare1));
	}

	private CompletableFuture<List<BenefitPlanCompare>> prepareBPLCurrentDentalPlanResponse() {
		BenefitPlanCompare benefitPlanCompare2 = preparePlanCompare("ae00001004", "Current Den Plan 1",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare3 = preparePlanCompare("ae00001005", "Current Den Plan 2",
				BenefitTypeEnum.DENTAL.getBcrBenTypeDesc());
		return CompletableFuture.completedFuture(Arrays.asList(benefitPlanCompare2, benefitPlanCompare3));
	}

	private CompletableFuture<List<BenefitPlanCompare>> prepareBPLCurrentVisionPlanResponse() {
		BenefitPlanCompare benefitPlanCompare4 = preparePlanCompare("ae00001006", "Current Vis Plan 1",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		BenefitPlanCompare benefitPlanCompare5 = preparePlanCompare("ae00001007", "Current Vis Plan 2",
				BenefitTypeEnum.VISION.getBcrBenTypeDesc());
		return CompletableFuture.completedFuture(Arrays.asList(benefitPlanCompare4, benefitPlanCompare5));
	}

	private Map<String, RateDetail> prepareBPLRateDetails() {
		return Map.of("1",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(TierRate.builder().cvgTierCode("15").cost(new BigDecimal(500.55)).build(),
								TierRate.builder().cvgTierCode("60").cost(new BigDecimal(600.66)).build()))
						.build(),
				"2",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(TierRate.builder().cvgTierCode("15").cost(new BigDecimal(500.55)).build(),
								TierRate.builder().cvgTierCode("60").cost(new BigDecimal(600.66)).build()))
						.build(),
				"3",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("4Tier")
						.tierRates(List.of(TierRate.builder().cvgTierCode("1").cost(new BigDecimal(100.11)).build(),
								TierRate.builder().cvgTierCode("2").cost(new BigDecimal(200.22)).build(),
								TierRate.builder().cvgTierCode("C").cost(new BigDecimal(300.33)).build(),
								TierRate.builder().cvgTierCode("4").cost(new BigDecimal(400.44)).build()))
						.build(),
				"4",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("4Tier")
						.tierRates(List.of(TierRate.builder().cvgTierCode("1").cost(new BigDecimal(100.11)).build(),
								TierRate.builder().cvgTierCode("2").cost(new BigDecimal(200.22)).build(),
								TierRate.builder().cvgTierCode("C").cost(new BigDecimal(300.33)).build(),
								TierRate.builder().cvgTierCode("4").cost(new BigDecimal(400.44)).build()))
						.build(),
				"5",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(TierRate.builder().cvgTierCode("20").cost(new BigDecimal(700.55)).build(),
								TierRate.builder().cvgTierCode("55").cost(new BigDecimal(800.66)).build()))
						.build(),
				"6",
				RateDetail.builder().regionCode(List.of("ALL")).rateType("ageBanded")
						.tierRates(List.of(TierRate.builder().cvgTierCode("20").cost(new BigDecimal(700.55)).build(),
								TierRate.builder().cvgTierCode("55").cost(new BigDecimal(800.66)).build()))
						.build());
	}
}
