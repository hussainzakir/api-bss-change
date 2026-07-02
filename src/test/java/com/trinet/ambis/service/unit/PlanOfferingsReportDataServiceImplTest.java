package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.trinet.ambis.util.StrategyUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.PlanOfferingReportConstants;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.Bundle;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.planofferings.Carrier;
import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsBenefitPlanData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsReportDetails;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.BenefitsPlanViewService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.impl.planofferings.PlanOfferingsReportDataServiceImpl;
import com.trinet.ambis.service.impl.planofferings.PlanOfferingsServiceUtil;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.test.config.TestHelper;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;


@RunWith(MockitoJUnitRunner.class)
public class PlanOfferingsReportDataServiceImplTest extends ServiceUnitTest {
	
	@InjectMocks
	private PlanOfferingsReportDataServiceImpl planOfferingsReportDataServiceImpl;
	
	@Mock
	private BenefitPlanDao benefitPlanDao;
	
	@Mock
	private RealmPlanYearDao realmPlanYearDao;
	
	@Mock
	private BenefitsPlanViewService benefitsPlanViewService;
	
	@Mock
	private PersonService personService;
	
	@Mock
	private RealmDataDao realmDataDao;
	 
    @Mock
    private HttpServletRequest httpRequest;
    
    @Mock
    private PlanOfferingsServiceUtil planOfferingsServiceUtil;

    @Mock
    private PortfolioService portfolioService;;
    
	@Mock
	PlanAvailabilityService planAvailabilityService;

	@Mock
	BenefitsBundleService benefitsBundleService;
	
	@Mock
	PlanCompareService planCompareService;

    private MockedStatic<StrategyUtils> strategyUtilsMockedStatic;
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;
    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;

    @Before
    public void setUp() {
        strategyUtilsMockedStatic = Mockito.mockStatic(StrategyUtils.class);
        bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);
        commonServiceHelperMockedStatic = Mockito.mockStatic(CommonServiceHelper.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
        if (mockStaticAppRulesAndConfigsUtils == null) {
			mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		}
    }

    @After
    public void tearDown() {
        strategyUtilsMockedStatic.close();
        bssSecurityUtilsMockedStatic.close();
        commonServiceHelperMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
        }
    }

	@Test
	public void getPlanOfferingsData() {
		// given
		// data
		PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
		Company company = prepareCompany(planOfferingsRequest);
		List<PlanOfferingsBenefitPlanData> offeringsData = preparePlanOfferingsBenefitPlanData();
		// method mocks
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn("00002241950");
		when(personService.getPersonFirstAndLastName(anyString())).thenReturn("User");
		when(planOfferingsServiceUtil.buildCompany(planOfferingsRequest)).thenReturn(company);
		
		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Set<PlanCarrier> planCarriers = new HashSet<>();
		planCarrierMap.put(BSSApplicationConstants.MEDICAL, planCarriers);
		planCarrierMap.put(BSSApplicationConstants.DENTAL, planCarriers);
		planCarrierMap.put(BSSApplicationConstants.VISION, planCarriers);
		
		when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
		
		when(CommonServiceHelper.getOutOfRegionPlansToExclude(Mockito.any(Company.class), Mockito.anySet(),
				Mockito.eq(realmDataDao))).thenReturn(Set.of("0001AWE"));
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(company)).thenReturn(false);

		Bundle bundle = new Bundle();
		bundle.setId(1L);
		bundle.setName("Bundle 1");
		when(benefitsBundleService.getBundleById(1)).thenReturn(bundle);

		Mockito.when(benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, company.getRealmPlanYear().getId(),
				Set.of("0001AWE"), false)).thenReturn(offeringsData);
		try {
			Mockito.when(planCompareService.getPlanAttributes(Mockito.any(), Mockito.any(), Mockito.any(),
					Mockito.any())).thenReturn(CompletableFuture.completedFuture(getMedicalBenefitPlanComapre("14")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// when
		PlanOfferingsData preparePlanOfferingsData = planOfferingsReportDataServiceImpl
				.preparePlanOfferingsData(planOfferingsRequest, httpRequest);
		// then
		// assert
		assertNotNull(preparePlanOfferingsData);
		assertEquals("Q1", preparePlanOfferingsData.getQuarter());
		assertTrue(preparePlanOfferingsData.getPlanOfferings().get("14").getPlanAttributes().stream()
				.anyMatch(pln -> "Vision Plan 1".equalsIgnoreCase(pln.getPlanName())));
	}

	@Test
	public void getPlanOfferingsDataForWseReport() {
		// given
		// data
		PlanOfferingsRequest planOfferingsRequest = preparePlanOfferingsRequest();
		planOfferingsRequest.setReportCode(PlanOfferingReportConstants.REPORT_CODE_WSE);
		planOfferingsRequest.setCompanyCode("COMPANY_CODE");
		Company company = prepareCompany(planOfferingsRequest);
		RealmCloneProgram realmCloneProgram = new RealmCloneProgram();
		realmCloneProgram.setCloneProgram("109");
		List<PlanOfferingsBenefitPlanData> offeringsData = preparePlanOfferingsBenefitPlanData();
		Map<String, List<String>> planSelectionsByPlanType = Map.of("10", List.of("MEDPLAN1"),
				"11", List.of("DENPLAN1"), "14", List.of("124842"));
		// method mocks
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn("00002241950");
		when(personService.getPersonFirstAndLastName(anyString())).thenReturn("User");
		when(planOfferingsServiceUtil.buildCompany(planOfferingsRequest)).thenReturn(company);
		when(realmDataDao.getRealmCloneProgram(company.getRealmPlanYear().getId())).thenReturn(realmCloneProgram);
		when(benefitPlanDao
				.getCompanyPlanSelectionsForPlanOfferingReport(planOfferingsRequest, company.getRealmPlanYear().getId()))
						.thenReturn(planSelectionsByPlanType);
		when(planAvailabilityService.getBenefitPlanAvailability(Mockito.any(PlanAvailableRequest.class))).thenReturn(CompletableFuture.completedFuture(preparePlanAvailableResponse()));

		when(CommonServiceHelper.getOutOfRegionPlansToExclude(Mockito.any(Company.class), Mockito.anySet(),
				Mockito.eq(realmDataDao))).thenReturn(Set.of("0001AWE"));
		when(RulesAndConfigsUtils.findPickChooseWithExceptions(company)).thenReturn(false);
		Mockito.when(benefitPlanDao.getBenefitsPlanOfferingsBy(planOfferingsRequest, company.getRealmPlanYear().getId(),
				Set.of("0001AWE"), false)).thenReturn(offeringsData);
		
		try {
			Mockito.when(planCompareService.getPlanAttributes(Mockito.any(), Mockito.any(), Mockito.any(),
					Mockito.any())).thenReturn(CompletableFuture.completedFuture(getMedicalBenefitPlanComapre("14")));
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		// when
		PlanOfferingsData preparePlanOfferingsData = planOfferingsReportDataServiceImpl
				.preparePlanOfferingsData(planOfferingsRequest, httpRequest);
		// then
		// assert
		assertNotNull(preparePlanOfferingsData);
		assertEquals("Q1", preparePlanOfferingsData.getQuarter());
		assertTrue(preparePlanOfferingsData.getPlanOfferings().get("14").getPlanAttributes().stream()
				.anyMatch(pln -> "Vision Plan 1".equalsIgnoreCase(pln.getPlanName())));
	}
	
	private List<BenefitPlanCompare> getMedicalBenefitPlanComapre(String benType){
		TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<Map<String,List<BenefitPlanCompare>>>(){};
		Map<String,List<BenefitPlanCompare>> planCompare = (Map<String,List<BenefitPlanCompare>>)TestHelper.readPlanComparisonRequest("/planComparison/currentPlansAttributes.json", type).get();
		return planCompare.get(benType);
	}

	@Test
	public void getMedicalCarriersTest() {
		String reportCode = "POEX";
		String quarter = "Q1";
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.JANUARY, 01);
		Date effDt = calendar.getTime();
		String benefitType = "med";
		Optional<String> hqZipCode = Optional.ofNullable("12345");
		List<CarrierData> actualResult;

		RealmPlanYear planYear = new RealmPlanYear();
		planYear.setPlanYearStart(effDt);
		planYear.setId(1);

		List<CarrierData> carriers = new ArrayList<>();
		CarrierData carrier = new CarrierData(2, "Aetna");
		carriers.add(carrier);
		carrier = new CarrierData(1, "Aig");
		carriers.add(carrier);

		when(realmPlanYearDao.findByOeQuarterAndPlanYearStart(quarter, effDt)).thenReturn(planYear);
		when(realmDataDao.getCarriersBy(Mockito.anyString(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(carriers);

		actualResult = planOfferingsReportDataServiceImpl.getCarriersBy(reportCode, quarter, effDt, "CA", hqZipCode,
				benefitType);

		assertEquals(2, actualResult.get(0).getCarrierId());
		assertEquals("Aetna", actualResult.get(0).getCarrierName());
		assertEquals(1, actualResult.get(1).getCarrierId());
		assertEquals("Aig", actualResult.get(1).getCarrierName());
	}
	
	/*
	 * Get medical carriers when report code is not related to  exchange
	 * 
	 * 
	 */
	
	@Test
	public void getMedicalCarriersTest1(){
		String quarter = "Q1";
		Calendar calendar = Calendar.getInstance();
		calendar.set(2024, Calendar.JANUARY, 01);
		Date effDt = calendar.getTime();
		String benefitType = null;
		Optional<String> hqZipCode = Optional.ofNullable("12345");
		String hqState = "CA";
		

		RealmPlanYear planYear = new RealmPlanYear();
		planYear.setPlanYearStart(effDt);
		planYear.setId(1);

		List<CarrierData> carriers = new ArrayList<>();
		CarrierData carrier = new CarrierData(2,"Aetna");
		carriers.add(carrier);
		carrier = new CarrierData(1, "Aig");
		carriers.add(carrier);
		List<CarrierData> actualResult;


		when(realmPlanYearDao.findByOeQuarterAndPlanYearStart(quarter, effDt)).thenReturn(planYear);
		when(realmDataDao.getCarriersBy(hqZipCode.orElse(""), 1, hqState, null, null)).thenReturn(carriers);

		actualResult = planOfferingsReportDataServiceImpl.getCarriersBy(null, quarter, effDt, hqState, hqZipCode,
				benefitType);

		assertEquals(2, actualResult.get(0).getCarrierId());
		assertEquals("Aetna", actualResult.get(0).getCarrierName());
		assertEquals(1, actualResult.get(1).getCarrierId());
		assertEquals("Aig", actualResult.get(1).getCarrierName());
	}
	
	
	private PlanOfferingsRequest preparePlanOfferingsRequest() {
		Carrier carrier = new Carrier();
		carrier.setId(1);
		carrier.setName("Atena");
		List<Carrier> carriers = new ArrayList<>();
		carriers.add(carrier);
		PlanOfferingsRequest planOfferingsRequest = new PlanOfferingsRequest();
		planOfferingsRequest.setCarriers(carriers);
		PlanOfferingsReportDetails reportDetails = new PlanOfferingsReportDetails();
		reportDetails.setCmsType("cms-content");
		planOfferingsRequest.setBenefitTypes(List.of("11", "14", "10"));
		planOfferingsRequest.setQuarter("Q1");
		planOfferingsRequest.setPlanYearStartDate("01/01/2024");
		planOfferingsRequest.setPlanYearEndDate("12/31/2024");
		planOfferingsRequest.setRegions(List.of("CA", "NC"));
		planOfferingsRequest.setBundleId(1L);

		return planOfferingsRequest;
	}

	private Company prepareCompany(PlanOfferingsRequest planOfferingsRequest) {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(68L);
		Company company = new Company();
		company.setCode(BSSApplicationConstants.DUMMY);
		company.setRealmPlanYear(realmPlanYear);
		Realm realm = new Realm();
		company.setRealm(realm);
		realm.setBenExchange(planOfferingsRequest.getExchange());
		company.setZipCode(planOfferingsRequest.getHqZipCode());
		company.setQuater(planOfferingsRequest.getQuarter());
		return company;
	}
	
	private List<PlanOfferingsBenefitPlanData>  preparePlanOfferingsBenefitPlanData(){
		List<PlanOfferingsBenefitPlanData> offeringsData = new ArrayList<>();
		PlanOfferingsBenefitPlanData planOfferingsBenefitPlanData = new PlanOfferingsBenefitPlanData();
		planOfferingsBenefitPlanData.setPlanType("11");
		planOfferingsBenefitPlanData.setBenefitPlan("DENPLAN1");
		planOfferingsBenefitPlanData.setDescription("Dental Plan One");
		offeringsData.add(planOfferingsBenefitPlanData);

		planOfferingsBenefitPlanData = new PlanOfferingsBenefitPlanData();
		planOfferingsBenefitPlanData.setPlanType("14");
		planOfferingsBenefitPlanData.setBenefitPlan("124842");
		planOfferingsBenefitPlanData.setDescription("Vision Plan 1");
		offeringsData.add(planOfferingsBenefitPlanData);

		planOfferingsBenefitPlanData = new PlanOfferingsBenefitPlanData();
		planOfferingsBenefitPlanData.setPlanType("10");
		planOfferingsBenefitPlanData.setBenefitPlan("MEDPLAN1");
		planOfferingsBenefitPlanData.setDescription("Medical Plan One");
		offeringsData.add(planOfferingsBenefitPlanData);

		planOfferingsBenefitPlanData = new PlanOfferingsBenefitPlanData();
		planOfferingsBenefitPlanData.setPlanType("10");
		planOfferingsBenefitPlanData.setBenefitPlan("MEDPLAN2");
		planOfferingsBenefitPlanData.setDescription("Medical Plan Two");
		offeringsData.add(planOfferingsBenefitPlanData);
		return offeringsData;
	}

	private List<PlanAvailableResponse> preparePlanAvailableResponse() {
		List<PlanAvailableResponse> planAvailableResponses;

		planAvailableResponses =  List.of(
				PlanAvailableResponse.builder().postal("07060").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MEDPLAN1", "MEDPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DENPLAN1", "DENPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("124842")).build()))
						.build(),
				PlanAvailableResponse.builder().postal("90210").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MEDPLAN1", "MEDPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DENPLAN1", "DENPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("124842")).build()))
						.build(),
				PlanAvailableResponse.builder().postal("29708").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MEDPLAN1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DENPLAN1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("124842")).build()))
						.build());

		return planAvailableResponses;
	}
	
}
