package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import com.trinet.ambis.enums.OmsOfferingEnum;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.CurrentTrinetPlans;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.HrisPlanAttributeService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.impl.outputs.PlanPopulatorImpl;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes.BenefitPlan;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(BSSSecurityUtils.class)
public class PlanPopulatorImplTest extends ServiceUnitTest {
	
	@InjectMocks
	private PlanPopulatorImpl planPopulator;

	@Mock
	private EmployeePlanAssignmentService emplPlanAssignmentService;

	@Mock
	private HrisPlanAttributeService hrisPlanAttributeService;
	
	@Mock
	PlanCompareService planCompareService;
	
	@Mock
	BenefitPlanService benefitPlanService;

	private static final String COMPANY_CODE = "22K0";
	
	private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
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
	
	@Test
	public void populateCurrentPlanTest() {
		CurrentTrinetPlans plans = populatePlan();
		plans.setProspectEmployees(prospectCurrentEmployees());
		Set<String> bcrPlanIds = plans.getProspectEmployees().get().stream()
				.flatMap(emp -> emp.getBenefitPlans().stream()).map(BenefitPlan::getBenefitPlanId)
				.collect(Collectors.toSet());
		
		CompletableFuture<List<BenefitPlanCompare>> benefitPlanCompares = CompletableFuture
				.completedFuture(benefitPlanCompareDetails());
		
		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(Mockito.anyList()))
		.thenReturn(prepareTriNetEmplPlanAssignment());
		
		when(planCompareService.getPlanAttributes(any(), any(), any(), any())).thenReturn(benefitPlanCompares);
		
		planPopulator.populateCurrentPlan().populate(plans);
		assertFalse(plans.getTrinetPlanRates().isEmpty());
//		verify(planCompareService).getPlanAttributes(eq(bcrPlanIds), any(), any(), any());
	}
	
	@Test
	public void populateCurrentPlanTestForBPLPlans() {
		CurrentTrinetPlans plans = populatePlan();
		plans.setProspectEmployees(prospectCurrentEmployees());
		Set<String> bplPlanIds = plans.getProspectEmployees().get().stream()
				.flatMap(emp -> emp.getBenefitPlans().stream()).map(BenefitPlan::getBplPlanId)
				.collect(Collectors.toSet());
		CompletableFuture<List<BenefitPlanCompare>> benefitPlanCompares = CompletableFuture
				.completedFuture(benefitPlanCompareDetails());

		when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(Mockito.anyList()))
				.thenReturn(prepareTriNetEmplPlanAssignment());

		when(planCompareService.getPlanAttributes(any(), any(), any(), any())).thenReturn(benefitPlanCompares);

		planPopulator.populateCurrentPlan().populate(plans);
		assertFalse(plans.getTrinetPlanRates().isEmpty());
		verify(planCompareService).getPlanAttributes(eq(bplPlanIds), any(), any(), any());
	}
	
	@Test
	public void populateTest() {
		CurrentTrinetPlans plans = populatePlan();
		planPopulator.populateTrinetPlan().populate(plans);
		
		planPopulator.populateTrinetPlanRate().populate(plans);
		assertTrue(plans.getTrinetPlanRates().isEmpty());
		
	}
	
    @Test
    public void populateTrinetPlanTest() {
        CurrentTrinetPlans plans = populatePlan();
        when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(Mockito.anyList()))
                .thenReturn(prepareTriNetEmplPlanAssignmentForMedical());
		when(hrisPlanAttributeService.getPlanAttributesByBenefitType(Mockito.anySet(), anyString()))
				.thenReturn(CompletableFuture.completedFuture(benefitPlanCompare()));
        planPopulator.populateTrinetPlan().populate(plans);
        assertEquals(4, plans.getTrinetPlanIds().size());
    }
	
	@Test
    public void populateTrinetPlanTest_withNationalRateType() {
        CurrentTrinetPlans plans = populatePlanWithNationalRateType();
        
        // Setup base plan mapping
        Map<String, com.trinet.ambis.service.model.plancompare.BenefitPlan> regionalToBasePlanMap = new HashMap<>();
        com.trinet.ambis.service.model.plancompare.BenefitPlan basePlan = 
        		new com.trinet.ambis.service.model.plancompare.BenefitPlan("BASE_MEDPLAN1", "Base Medical Plan 1");
        regionalToBasePlanMap.put("MEDPLAN1", basePlan);
        
        when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(Mockito.anyList()))
                .thenReturn(prepareTriNetEmplPlanAssignmentForMedical());
        when(benefitPlanService.getRegionalBasePlanMapping(any()))
                .thenReturn(regionalToBasePlanMap);
        when(hrisPlanAttributeService.getPlanAttributesByBenefitType(Mockito.anySet(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(benefitPlanCompare()));
        
        planPopulator.populateTrinetPlan().populate(plans);
        
        // Verify benefitPlanService was called since company has NATIONAL rate type
        verify(benefitPlanService).getRegionalBasePlanMapping(any());
    }

    @Test
    public void populateTrinetPlanTest_withRegionalRateType() {
        CurrentTrinetPlans plans = populatePlan(); // Default company has REGIONAL rate type

        when(emplPlanAssignmentService.getEmployeePlanAssigmentBy(Mockito.anyList()))
                .thenReturn(prepareTriNetEmplPlanAssignmentForMedical());
        when(hrisPlanAttributeService.getPlanAttributesByBenefitType(Mockito.anySet(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(benefitPlanCompare()));

        planPopulator.populateTrinetPlan().populate(plans);

        // Verify benefitPlanService was NOT called since company has REGIONAL rate type
        verify(benefitPlanService, Mockito.never()).getRegionalBasePlanMapping(any());
    }


	@Test
	public void populateCurrentPlanHeadCountTest() {
		CurrentTrinetPlans plans = populatePlan();
		planPopulator.populateCurrentPlanHeadCount().populate(plans);
		assertTrue(plans.getCurrentPlanHeadCounts().isEmpty());
	}

	@Test
	public void populateTrinetPlanRate_PlanRateV2EnabledTest1() {
		try (MockedStatic<StrategyUtils> strategyUtilsMock = mockStatic(StrategyUtils.class)) {

			Map<String, List<BenefitPlanRate>> planRates = trinetPlanRatesByCompany();
			Map<String, BigDecimal> planCostV2 = Map.of("TIER1", BigDecimal.TEN);
			strategyUtilsMock.when(() -> StrategyUtils.getPlanCost(planRates.get("MEDPLAN1"))).thenReturn(planCostV2);

			CurrentTrinetPlans plans = populatePlan();
			plans.setTrinetPlanIds(List.of("MEDPLAN1"));
			plans.setTrinetPlanRatesByCompany(planRates);

			planPopulator.populateTrinetPlanRate().populate(plans);

			assertEquals(BigDecimal.TEN, plans.getTrinetPlanRates().get("MEDPLAN1").getTierRates().get(0).getCost());
		}
	}

    @Test
	public void populateTrinetPlanRate_PlanRateV2EnabledTest2() {
		try (MockedStatic<StrategyUtils> strategyUtilsMock = mockStatic(StrategyUtils.class)) {

			Map<String, List<BenefitPlanRate>> planRates = trinetPlanRatesByCompany();
			Map<String, XbssRealmPlyrPlan> plyrPlanMap = plyrPlanMap();
			Map<String, BigDecimal> planCost = Map.of("TIER2", BigDecimal.ONE);

			strategyUtilsMock.when(() -> StrategyUtils.getPlanCost(planRates.get("MEDPLAN1")))
					.thenReturn(planCost);

			CurrentTrinetPlans plans = populatePlan();
			plans.setTrinetPlanIds(List.of("MEDPLAN1"));
			plans.setTrinetPlanRatesByCompany(planRates);
			plans.setPlyrPlanMap(plyrPlanMap);

			planPopulator.populateTrinetPlanRate().populate(plans);

			assertEquals(BigDecimal.ONE, plans.getTrinetPlanRates().get("MEDPLAN1").getTierRates().get(0).getCost());
		}
	}
	
	private List<EePlanAssignment> prepareTriNetEmplPlanAssignment() {
		EePlanAssignment empl1Med1 = prepareTriNetEmplPlan(1111, "MEDPLAN1", "EMPL1-KSJLKASJDLJAD", "" ,1,"10");
		EePlanAssignment empl1Den1 = prepareTriNetEmplPlan(1111, "DENPLAN1", "EMPL1-KSJLKASJDLJAD", "" ,2,"11");
		EePlanAssignment empl1Vis1 = prepareTriNetEmplPlan(1111, "VISPLAN1", "EMPL1-KSJLKASJDLJAD", "" ,3,"14");

		EePlanAssignment empl2Med2 = prepareTriNetEmplPlan(2222, "MEDPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,1,"10");
		EePlanAssignment empl2Dev2 = prepareTriNetEmplPlan(2222, "DENPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,2,"11");
		EePlanAssignment empl2Vis2 = prepareTriNetEmplPlan(2222, "VISPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,3,"14");

		EePlanAssignment empl3Vis3 = prepareTriNetEmplPlan(2222, "VISPLAN3", "EMPL3-KJKLDFJKJJDKK", "" ,3,"14");
		return Arrays.asList(empl1Med1, empl1Den1, empl1Vis1, empl2Med2, empl2Dev2, empl2Vis2, empl3Vis3);
	}
	
	private List<EePlanAssignment> prepareTriNetEmplPlanAssignmentForMedical() {
		EePlanAssignment empl1Med1 = prepareTriNetEmplPlan(1111, "006XAE", "EMPL1-KSJLKASJDLJAD", "" ,2,"11");
		EePlanAssignment empl1Med2 = prepareTriNetEmplPlan(1111, "MEDPLAN1", "EMPL1-KSJLKASJDLJAD", "" ,1,"10");
		EePlanAssignment empl2Med2 = prepareTriNetEmplPlan(2222, "MEDPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,1,"10");
		EePlanAssignment empl2Vis2 = prepareTriNetEmplPlan(2222, "VISPLAN2", "EMPL2-JKFHJSHFDJKHF", "" ,3,"14");
		return Arrays.asList(empl1Med1,empl1Med2,empl2Med2,empl2Vis2);
	}
	
	private EePlanAssignment prepareTriNetEmplPlan(int strategyId, String benPlanId, String emplId, String covrgCD, long portfolioID, String benefitType) {
		EePlanAssignment empl = EePlanAssignment.builder().build();
		EePlanAssignmentPK pk = new EePlanAssignmentPK(strategyId, emplId, benefitType);
		empl.setCovrgCD(covrgCD);
		empl.setBenefitPlan(benPlanId);
		empl.setPortfolioId(portfolioID);
		empl.setEePlanAssignmentPK(pk);
		return empl;

	}
	
	public CurrentTrinetPlans populatePlan() {
		return CurrentTrinetPlans.builder().company(company())
				.requestBenefitTypes(List.of("10", "11", "14")).strategyId("301023")
				.realmPlyrPlans(realmPlyrPlans())
				.prospectEmployees(prospectEmployees()).plyrPlanMap(plyrPlanMap())
				.trinetPlanRatesByCompany(trinetPlanRatesByCompany())
				.benefitTypeCurrentPlansAttributes(benefitTypeCurrentPlansAttributes())
				.planComparison(planComparison())
				.benefitTypeTrinetPlansAttributes(benefitTypeTrinetPlansAttributes())
				.currentPlanRates(currentPlanRates())
				.currentPlanHeadCounts(currentPlanHeadCounts())
				.trinetPlanHeadCounts(trinetPlanHeadCounts())
				.trinetPlanRates(trinetPlanRates())
				.trinetPlanIds(trinetPlanIds())
				.currentPlanIds(currentPlanIds())
				.currentTrinetPlansMapping(currentTrinetPlansMapping())
				.trinetPlanHeadCounts(trinetPlanHeadCounts())
				.build();
	}

	public CurrentTrinetPlans populatePlanWithNationalRateType() {
		return CurrentTrinetPlans.builder().company(companyWithDifferentials())
				.requestBenefitTypes(List.of("10", "11", "14")).strategyId("301023")
				.realmPlyrPlans(realmPlyrPlans())
				.prospectEmployees(prospectEmployees()).plyrPlanMap(plyrPlanMap())
				.trinetPlanRatesByCompany(trinetPlanRatesByCompany())
				.benefitTypeCurrentPlansAttributes(benefitTypeCurrentPlansAttributes())
				.planComparison(planComparison())
				.benefitTypeTrinetPlansAttributes(benefitTypeTrinetPlansAttributes())
				.currentPlanRates(currentPlanRates())
				.currentPlanHeadCounts(currentPlanHeadCounts())
				.trinetPlanHeadCounts(trinetPlanHeadCounts())
				.trinetPlanRates(trinetPlanRates())
				.trinetPlanIds(trinetPlanIds())
				.currentPlanIds(currentPlanIds())
				.currentTrinetPlansMapping(currentTrinetPlansMapping())
				.trinetPlanHeadCounts(trinetPlanHeadCounts())
				.build();
	}

	public Company company() {
		BandCodes bandCodes = new BandCodes();
		bandCodes.setAetnaBandCode("12345");
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70);
		company.setRealmPlanYear(realmPlanYear);
		company.setCode(COMPANY_CODE);
		company.setBandCodes(bandCodes);
		company.setRealm(realm());
		company.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
		// Default is BANDS risk type (returns REGIONAL)
		company.setRiskType(RiskTypeEnum.BANDS);
		return company;
	}

	public Company companyWithDifferentials() {
		BandCodes bandCodes = new BandCodes();
		bandCodes.setAetnaBandCode("12345");
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70);
		company.setRealmPlanYear(realmPlanYear);
		company.setCode(COMPANY_CODE);
		company.setBandCodes(bandCodes);
		company.setRealm(realm());
		company.setOmsOffering(OmsOfferingEnum.OM_OD_OV_TLD.name());
		// DIFFERENTIALS risk type returns NATIONAL
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		company.setRateType("NATIONAL");
		return company;
	}
	
	private Realm realm() {
		Realm realm = new Realm();
		realm.setBenExchange("TriNet OMS");
		return realm;
	}
	
	private List<BenefitPlanCompare> benefitPlanCompare() {
		List<BenefitPlanCompare> emptyPlanList = new ArrayList<>();
		BenefitPlanCompare benefitPlanCompare = BenefitPlanCompare.builder().planId("MEDPLAN1").build();
		emptyPlanList.add(benefitPlanCompare);
		return emptyPlanList;

	}
	
	private Optional<List<EmployeePlansRes>> prospectEmployees(){
		List<EmployeePlansRes> employees = new ArrayList<>();
		EmployeePlansRes empl = new EmployeePlansRes();
		empl.setEmployeeId("00010590832");
		List<BenefitPlan> benefitPlans = new ArrayList<>(); 
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setBenefitPlanId("0024GI");
		benefitPlan.setBenefitTypeCode("10");
		benefitPlan.setCoverageCode("1");
		empl.setBenefitPlans(benefitPlans);
		employees.add(empl);
		return Optional.of(employees);
	}
	
	private Optional<List<EmployeePlansRes>> prospectCurrentEmployees(){
		List<EmployeePlansRes> employees = new ArrayList<>();
		EmployeePlansRes empl = new EmployeePlansRes();
		empl.setEmployeeId("00010590832");
		List<BenefitPlan> benefitPlans = new ArrayList<>(); 
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setBenefitPlanId("0024GI");
		benefitPlan.setBenefitTypeCode("10");
		benefitPlan.setBplPlanId("ae0001d002");
		benefitPlan.setCoverageCode("1");
		benefitPlans.add(benefitPlan);
		empl.setBenefitPlans(benefitPlans);
		employees.add(empl);
		return Optional.of(employees);
	}
	
	private Map<String, XbssRealmPlyrPlan> plyrPlanMap(){
		TypeReference<Map<String, XbssRealmPlyrPlan>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/plyrPlanMap.json", type).get();
	}
	
	private Map<String,Set<XbssRealmPlyrPlan>> realmPlyrPlans(){
		TypeReference<Map<String,Set<XbssRealmPlyrPlan>>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/realmPlyrPlans.json", type).get();
	}

	private Map<String,List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes(){
		TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentPlansAttributes.json", type).get();
	}
	
	private Map<String,List<BenefitPlanCompare>> benefitTypeTrinetPlansAttributes(){
		TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlansAttributes.json", type).get();
	}
	
	private List<EmployeeHeadCountRes> currentPlanHeadCounts(){
		TypeReference<List<EmployeeHeadCountRes>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentPlanHeadCounts.json", type).get();
	}
	
	private List<EmployeeHeadCountRes> trinetPlanHeadCounts(){
		TypeReference<List<EmployeeHeadCountRes>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlanHeadCounts.json", type).get();
	}
	
	private Map<String, RateDetail> currentPlanRates(){
		TypeReference<Map<String, RateDetail>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentPlanRates.json", type).get();
	}
	
	private Map<String, RateDetail> trinetPlanRates(){
		TypeReference<Map<String, RateDetail>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlanRates.json", type).get();
	}
	
	private Map<String, List<BenefitPlanRate>> trinetPlanRatesByCompany(){
		TypeReference<Map<String, List<BenefitPlanRate>>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlanRatesByCompany.json", type).get();
	}
	
	private Map<String,Map<String,List<String>>> currentTrinetPlansMapping(){
		TypeReference<Map<String,Map<String,List<String>>>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentTrinetPlansMapping.json", type).get();
	}
	
	private List<String> currentPlanIds(){
		TypeReference<List<String>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentPlanIds.json", type).get();
	}
	
	private List<String> trinetPlanIds(){
		TypeReference<List<String>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlanIds.json", type).get();
	}
	
	private Map<String,BasePlanComparison> planComparison(){
		return new HashMap<>();
	}
	
	
	private static <T> Supplier<T> readPlanComparisonRequest(String filePath, TypeReference<T> valueType) {
		return () -> {
			ClassPathResource staticDataResource = new ClassPathResource(filePath);
			try {
				String dto = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				mapper.setVisibility(
						VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
				return mapper.readValue(dto, valueType);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		};
	}
	
	 private List<BenefitPlanCompare> benefitPlanCompareDetails(){
	        TypeReference<List<BenefitPlanCompare>> type = new TypeReference<List<BenefitPlanCompare>>(){};
	        return readPlanComparisonRequest("/planComparison/benefitPlanCompare.json", type).get();
	    }

}
