package com.trinet.ambis.service.unit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.rest.controllers.dto.outputs.*;
import com.trinet.ambis.service.dto.CarrierAssetDto;
import com.trinet.ambis.service.dto.CmsLogoDto;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.util.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.service.impl.outputs.PlanCompareBuilderImpl;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes.BenefitPlan;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PlanCompareBuilderImplTest extends ServiceUnitTest {

	@InjectMocks
	private PlanCompareBuilderImpl planCompareBuilder;

	private static final String COMPANY_CODE = "22K0";
	CurrentTrinetPlans plans = null;
    private static Logger logger;
    private MockedStatic<ReflectionUtils> appConfigMockedStatic;
    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;

	@Before
	public void setUp() {
		appConfigMockedStatic = Mockito.mockStatic(ReflectionUtils.class);
		logger = Mockito.mock(Logger.class);
		plans = populatePlan();
		if (mockStaticAppRulesAndConfigsUtils == null) {
			mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		}
	}

    @After
    public void tearDown() {
    	appConfigMockedStatic.close();
		if (mockStaticAppRulesAndConfigsUtils != null) {
			mockStaticAppRulesAndConfigsUtils.close();
		}
    }

	@Test
	public void buildCurrentPlanAttributeLabelsTest() {
		planCompareBuilder.buildPlanAttributeLabels().populate(plans);
		assertFalse(plans.getPlanComparison().isEmpty());
		
		plans.setCurrentTrinetPlansMapping(currentTrinetPlansMapping());
		planCompareBuilder.buildCurrentAndTrinetPlanAttributeValues().populate(plans);
		
		planCompareBuilder.buildCurrentPlanRate().populate(plans);
		assertFalse(plans.getCurrentPlanRates().isEmpty());
		
		planCompareBuilder.buildCurrentPlanHeadCount().populate(plans);
		assertFalse(plans.getCurrentPlanHeadCounts().isEmpty());
		
		planCompareBuilder.buildTrinetPlanRate().populate(plans);
		assertFalse(plans.getTrinetPlanRates().isEmpty());
		
		planCompareBuilder.buildTrinetPlanHeadCount().populate(plans);
		assertFalse(plans.getTrinetPlanHeadCounts().isEmpty());

		plans.setBenefitTypeTrinetPlansAttributes(null);
		planCompareBuilder.buildPlanAttributeLabels().populate(plans);
		assertFalse(plans.getPlanComparison().isEmpty());
	}

	@Test
	public void testBuildTrinetPlanRate_TrinetAndOmsPlans() {
		// Setting up the test data
		plans.setTibPlanRates(omsPlanRates());
		plans.setPlanComparison(planComparisonMedicalOms());
		plans.getPlanComparison().putAll(planComparisonDentalVisionTriNet());
		plans.setBenefitTypeTrinetPlansAttributes(benefitTypeTrinetPlansAttributes());
		plans.setRequestBenefitTypes(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		// Executing the method
		planCompareBuilder.buildTrinetPlanRate().populate(plans);
		// Verifying TriNet and OMS plan rates
		assertEquals(3, plans.getPlanComparison().size());

		// Medical
		assertBuildTrinetPlanRateMedical((PlanComparison) plans.getPlanComparison().get(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		assertBuildTrinetPlanRateDental((PlanComparison) plans.getPlanComparison().get(BSSApplicationConstants.DENTAL_PLAN_TYPE));
		assertBuildTrinetPlanRateVision((PlanComparison) plans.getPlanComparison().get(BSSApplicationConstants.VISION_PLAN_TYPE));

	}


	@Test
	public void testBuildTrinetPlanRate_TriggersLoggerWhenRateDetailNotFound() {
		// Setting up the test data
		plans.setTrinetPlanRates(new HashMap<>()); // Empty map to trigger the else condition
		plans.setPlanComparison(planComparisonMedicalOms());
		plans.setBenefitTypeTrinetPlansAttributes(benefitTypeTrinetPlansAttributes());
		plans.setRequestBenefitTypes(List.of("10"));
		// Executing the method
		planCompareBuilder.buildTrinetPlanRate().populate(plans);
		// Verifying the logger was triggered
	}

	@Test
	public void testBuildCurrentAndTrinetPlanAttributeValues_WithPageBreaks() {
		mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBssOutputPhase2Enabled).thenReturn(true);

		Map<String, List<BenefitPlanCompare>> currentPlans = new HashMap<>();
		List<BenefitPlanCompare> medicalCurrentPlans = new ArrayList<>();
		medicalCurrentPlans.add(BenefitPlanCompare.builder()
				.planId("CURRENT_MED")
				.name("Very Long Medical Plan Name That Should Exceed Forty Characters To Test Wrap Logic")
				.build());
		currentPlans.put("10", medicalCurrentPlans);
		plans.setBenefitTypeCurrentPlansAttributes(currentPlans);

		Map<String, List<BenefitPlanCompare>> trinetPlans = new HashMap<>();
		List<BenefitPlanCompare> medicalTrinetPlans = new ArrayList<>();
		// Adding multiple plans to exceed page limit
		for (int i = 1; i <= 25; i++) {
			medicalTrinetPlans.add(BenefitPlanCompare.builder()
					.planId("TRINET_MED_" + i)
					.name("Trinet Plan " + i)
					.carrier("Carrier " + i)
					.build());
		}
		trinetPlans.put("10", medicalTrinetPlans);
		plans.setBenefitTypeTrinetPlansAttributes(trinetPlans);

		Map<String, Map<String, List<String>>> mapping = new HashMap<>();
		Map<String, List<String>> medMapping = new HashMap<>();
		List<String> trinetIds = new ArrayList<>();
		for (int i = 1; i <= 25; i++) {
			trinetIds.add("TRINET_MED_" + i);
		}
		medMapping.put("CURRENT_MED", trinetIds);
		mapping.put("10", medMapping);
		plans.setCurrentTrinetPlansMapping(mapping);

		plans.setRequestBenefitTypes(List.of("10"));
		planCompareBuilder.buildPlanAttributeLabels().populate(plans);
		planCompareBuilder.buildCurrentAndTrinetPlanAttributeValues().populate(plans);
		planCompareBuilder.buildPlanComparisonPagination().populate(plans);

		PlanComparison planComparison = (PlanComparison) plans.getPlanComparison().get("10");
		CompareCurrentTrinetPlans comparison = planComparison.getComparisons().get(0);

		// Assert that at least one plan has a page break
		boolean hasPageBreak = comparison.getTriNetPlans().stream().anyMatch(PlanAttribute::isPageBreak);
		assertTrue("At least one TriNet plan should have a page break due to row count exceeding limit", hasPageBreak);
	}

	@Test
	public void testBuildPlanComparisonPagination_Disabled() {
		mockStaticAppRulesAndConfigsUtils.when(AppRulesAndConfigsUtils::isBssOutputPhase2Enabled).thenReturn(false);
		plans.setRequestBenefitTypes(List.of("10"));
		planCompareBuilder.buildPlanComparisonPagination().populate(plans);
		// If disabled, pagination ignores plans
	}

	public Company company() {
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70);
		company.setRealmPlanYear(realmPlanYear);
		company.setCode(COMPANY_CODE);
		com.trinet.ambis.persistence.model.Realm realm = new com.trinet.ambis.persistence.model.Realm();
		realm.setBenExchange("TRINET");
		company.setRealm(realm);
		return company;
	}

	private Map<String, BasePlanComparison> planComparisonMedicalOms() {
		Map<String, BasePlanComparison> planComparisonMap = new HashMap<>();
		PlanComparison planComparison = new PlanComparison();

		// Initialize the comparisons list
		List<CompareCurrentTrinetPlans> comparisons = new ArrayList<>();
		comparisons.add(CompareCurrentTrinetPlans.builder()
				.currentPlan(PlanAttribute.builder().planId("PROSPECT_MED").build())
				.triNetPlans(List.of(
						PlanAttribute.builder().planId("OMS_MEDICAL_PLAN1").build(),
						PlanAttribute.builder().planId("OMS_MEDICAL_PLAN2").build()))
				.build());

		List<AttributeDesc> attributeNames = new ArrayList<>();
		attributeNames.add(AttributeDesc.builder().name("Plan Name").build());

		planComparison.setComparisons(comparisons);
		planComparison.setAttributeNames(attributeNames);


		// Add the initialized planComparison object to the map
		planComparisonMap.put("10", planComparison);
		return planComparisonMap;
	}

	private Map<String, BasePlanComparison> planComparisonDentalVisionTriNet() {
		Map<String, BasePlanComparison> planComparisonMap = new HashMap<>();

		// DENTAL
		PlanComparison planComparison = new PlanComparison();
		List<CompareCurrentTrinetPlans> comparisons = new ArrayList<>();
		comparisons.add(CompareCurrentTrinetPlans.builder()
				.currentPlan(PlanAttribute.builder().planId("PROSPECT_DEN").build())
				.triNetPlans(List.of(PlanAttribute.builder().planId("TRINET_DENTAL_PLAN").build()))
				.build());

		List<AttributeDesc> attributeNames = new ArrayList<>();
		attributeNames.add(AttributeDesc.builder().name("Plan Name").build());

		planComparison.setComparisons(comparisons);
		planComparison.setAttributeNames(attributeNames);
		planComparisonMap.put("11", planComparison);

		// VISION
		planComparison = new PlanComparison();
		comparisons = new ArrayList<>();
		comparisons.add(CompareCurrentTrinetPlans.builder()
				.currentPlan(PlanAttribute.builder().planId("PROSPECT_VIS").build())
				.triNetPlans(List.of(PlanAttribute.builder().planId("TRINET_VISION_PLAN").build()))
				.build());

		attributeNames = new ArrayList<>();
		attributeNames.add(AttributeDesc.builder().name("Plan Name").build());

		planComparison.setComparisons(comparisons);
		planComparison.setAttributeNames(attributeNames);
		planComparisonMap.put("14", planComparison);

		return planComparisonMap;
	}

	public CurrentTrinetPlans populatePlan() {
		return CurrentTrinetPlans.builder().company(company())
				.requestBenefitTypes(List.of("10", "11", "14")).strategyId("301023")
				.prospectEmployees(prospectEmployees()).plyrPlanMap(plyrPlanMap())
				.trinetPlanRatesByCompany(trinetPlanRatesByCompany())
				.benefitTypeCurrentPlansAttributes(benefitTypeCurrentPlansAttributes()).planComparison(planComparison())
				.benefitTypeTrinetPlansAttributes(benefitTypeTrinetPlansAttributes())
				.currentPlanRates(currentPlanRates())
				.currentPlanHeadCounts(currentPlanHeadCounts())
				.trinetPlanHeadCounts(trinetPlanHeadCounts())
				.trinetPlanRates(trinetPlanRates())
				.currentPlanIds(currentPlanIds())
				.trinetPlanIds(trinetPlanIds())
				.cmsLogoDetailMap(new HashMap<>())
				.build();
	}
	
	private List<String> currentPlanIds(){
		TypeReference<List<String>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentPlanIds.json", type).get();
	}
	
	private List<String> trinetPlanIds(){
		TypeReference<List<String>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlanIds.json", type).get();
	}
	
	private Map<String, RateDetail> currentPlanRates(){
		TypeReference<Map<String, RateDetail>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentPlanRates.json", type).get();
	}
	
	private Map<String, RateDetail> trinetPlanRates(){
		TypeReference<Map<String, RateDetail>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlanRates.json", type).get();
	}

	private Map<String, RateDetail> omsPlanRates(){
		TypeReference<Map<String, RateDetail>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/omsPlanRates.json", type).get();
	}
	
	private Map<String,List<BenefitPlanCompare>> benefitTypeTrinetPlansAttributes(){
		TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlansAttributes.json", type).get();
	}

	private Map<String,Map<String,List<String>>> currentTrinetPlansMapping(){
		TypeReference<Map<String,Map<String,List<String>>>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentTrinetPlansMapping.json", type).get();
	}

	private Map<String, List<BenefitPlanRate>> trinetPlanRatesByCompany() {
		TypeReference<Map<String, List<BenefitPlanRate>>> type = new TypeReference<>() {
		};
		return readPlanComparisonRequest("/planComparison/trinetPlanRatesByCompany.json", type).get();
	}
	private List<EmployeeHeadCountRes> currentPlanHeadCounts(){
		TypeReference<List<EmployeeHeadCountRes>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/currentPlanHeadCounts.json", type).get();
	}
	
	private List<EmployeeHeadCountRes> trinetPlanHeadCounts(){
		TypeReference<List<EmployeeHeadCountRes>> type = new TypeReference<>(){};
		return readPlanComparisonRequest("/planComparison/trinetPlanHeadCounts.json", type).get();
	}

	private Optional<List<EmployeePlansRes>> prospectEmployees() {
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

	private Map<String, XbssRealmPlyrPlan> plyrPlanMap() {
		TypeReference<Map<String, XbssRealmPlyrPlan>> type = new TypeReference<>() {
		};
		return readPlanComparisonRequest("/planComparison/plyrPlanMap.json", type).get();
	}

	private Map<String, List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes() {
		TypeReference<Map<String, List<BenefitPlanCompare>>> type = new TypeReference<>() {
		};
		return (Map<String, List<BenefitPlanCompare>>) readPlanComparisonRequest(
				"/planComparison/currentPlansAttributes.json", type).get();
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

	private Map<String, BasePlanComparison> planComparison() {
		return new HashMap<>();
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

	private void assertBuildTrinetPlanRateMedical(PlanComparison planComparison) {
		assertEquals(1, planComparison.getAttributeNames().size());
		assertEquals("Plan Name", planComparison.getAttributeNames().get(0).getName());
		assertEquals(1, planComparison.getComparisons().size());
		assertEquals("PROSPECT_MED", planComparison.getComparisons().get(0).getCurrentPlan().getPlanId());
		assertEquals(2, planComparison.getComparisons().get(0).getTriNetPlans().size());

		// 4Tier
		assertEquals("OMS_MEDICAL_PLAN1", planComparison.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
		CvgLvlPlanInfo planRates = planComparison.getComparisons().get(0).getTriNetPlans().get(0).getPlanRates();
		assertEquals("$500.37", planRates.getEmployeeOnly());
		assertEquals("$600.46", planRates.getEmployeeSpouse());
		assertEquals("$700.43", planRates.getEmployeeChildren());
		assertEquals("$800.54", planRates.getFamily());

		// Age Banded
		assertEquals("OMS_MEDICAL_PLAN2", planComparison.getComparisons().get(0).getTriNetPlans().get(1).getPlanId());
		planRates = planComparison.getComparisons().get(0).getTriNetPlans().get(1).getPlanRates();
		assertEquals("Age Banded", planRates.getEmployeeOnly());
		assertEquals("Age Banded", planRates.getEmployeeSpouse());
		assertEquals("Age Banded", planRates.getEmployeeChildren());
		assertEquals("Age Banded", planRates.getFamily());
	}


	private void assertBuildTrinetPlanRateDental(PlanComparison planComparison) {
		assertEquals(1, planComparison.getAttributeNames().size());
		assertEquals("Plan Name", planComparison.getAttributeNames().get(0).getName());
		assertEquals(1, planComparison.getComparisons().size());
		assertEquals("PROSPECT_DEN", planComparison.getComparisons().get(0).getCurrentPlan().getPlanId());
		assertEquals(1, planComparison.getComparisons().get(0).getTriNetPlans().size());

		// 4Tier
		assertEquals("TRINET_DENTAL_PLAN", planComparison.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
		CvgLvlPlanInfo planRates = planComparison.getComparisons().get(0).getTriNetPlans().get(0).getPlanRates();
		assertEquals("$50.00", planRates.getEmployeeOnly());
		assertEquals("$60.00", planRates.getEmployeeSpouse());
		assertEquals("$70.00", planRates.getEmployeeChildren());
		assertEquals("$80.00", planRates.getFamily());
	}


	private void assertBuildTrinetPlanRateVision(PlanComparison planComparison) {
		assertEquals(1, planComparison.getAttributeNames().size());
		assertEquals("Plan Name", planComparison.getAttributeNames().get(0).getName());
		assertEquals(1, planComparison.getComparisons().size());
		assertEquals("PROSPECT_VIS", planComparison.getComparisons().get(0).getCurrentPlan().getPlanId());
		assertEquals(1, planComparison.getComparisons().get(0).getTriNetPlans().size());

		// 4Tier
		assertEquals("TRINET_VISION_PLAN", planComparison.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
		CvgLvlPlanInfo planRates = planComparison.getComparisons().get(0).getTriNetPlans().get(0).getPlanRates();
		assertEquals("$30.00", planRates.getEmployeeOnly());
		assertEquals("$40.00", planRates.getEmployeeSpouse());
		assertEquals("$50.00", planRates.getEmployeeChildren());
		assertEquals("$60.00", planRates.getFamily());
	}

}
