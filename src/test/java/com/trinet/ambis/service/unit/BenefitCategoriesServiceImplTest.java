/**
 * 
 */
package com.trinet.ambis.service.unit;

import static com.trinet.ambis.enums.OmsOfferingEnum.OMB_TLD;
import static com.trinet.ambis.enums.OmsOfferingEnum.OM_OD_OV_TLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.MandatoryRegionDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.TemplateFundingDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.dao.ps.BenefitPlanDataDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PortfolioService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.impl.BenefitCategoriesServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.BenefitsCategories;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;

/**
 * @author hliddle
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class BenefitCategoriesServiceImplTest extends ServiceUnitTest {

	/*
	 * @Rule public PowerMockRule rule = new PowerMockRule();
	 */

	@Autowired
	@InjectMocks
	BenefitCategoriesServiceImpl benefitCategoriesService;
	
	@Mock
	PlanRatesService planRatesService;

	@Mock
	PortfolioService portfolioService;
	
	@Mock
	RealmPlyrPlanService realmPlyrPlanService;
	
	@Mock
	BenefitOfferExceptionService benOfferExceptionService;

	@Mock
	ProspectPlanAvailabilityService prospectPlanAvailabilityService;
	
	@Mock
	BenefitGroupDao benefitGroupsDao;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	PsCompanyDao psCompanyDao;

	@Mock
	TemplateFundingDao templateFundingDao;

	@Mock
	PortfolioRuleDao portfolioRuleDao;

	@Mock
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;

	@Mock
	BenefitPlanDataDao benefitPlanDataDao;

	@Mock
	MandatoryRegionDao mandatoryRegionDao;

	@Mock
	XbssRealmPlyrPlanDao realmPlyrPlanDao;
	
	@Mock
	BenefitPlanDao benefitPlanDao;
	
	@Mock
	CommonDataDao commonDataDao;

	@Mock
	BenefitPlanService benefitPlanService;
	
	Set<String> locations = new HashSet<>();
	Set<String> primaryPlanCarrierIds = new HashSet<>();
	Map<String, Set<StateBenefitPlan>> primaryPlanMap = new HashMap<>();
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMock;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMock;
    private MockedStatic<StrategyServiceHelper> strategyServiceHelperMock;
    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMock;
    private MockedStatic<StrategyUtils> strategyUtilsMock;
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMock;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        appRulesAndConfigsUtilsMock = Mockito.mockStatic(AppRulesAndConfigsUtils.class);

        rulesAndConfigsUtilsMock = Mockito.mockStatic(RulesAndConfigsUtils.class);
        commonServiceHelperMock = Mockito.mockStatic(CommonServiceHelper.class);
        strategyServiceHelperMock = Mockito.mockStatic(StrategyServiceHelper.class);
        benefitCategoriesHelperMock = Mockito.mockStatic(BenefitCategoriesHelper.class);
        strategyUtilsMock = Mockito.mockStatic(StrategyUtils.class);

        primaryPlanCarrierIds.add("1");
        primaryPlanCarrierIds.add("2");
        primaryPlanCarrierIds.add("3");
        primaryPlanMap = preparePrimaryPlanMap();
    }

    @After
    public void tearDown() {
        if (appRulesAndConfigsUtilsMock != null) {
            appRulesAndConfigsUtilsMock.close();
        }

        rulesAndConfigsUtilsMock.close();
        commonServiceHelperMock.close();
        strategyServiceHelperMock.close();
        benefitCategoriesHelperMock.close();
        strategyUtilsMock.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void constructBenefitsCategories() {

        Company company = prepareCompany();

        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Set<PlanCarrier> planCarriers = new HashSet<>();
        planCarrierMap.put(BSSApplicationConstants.MEDICAL, planCarriers);
        planCarrierMap.put(BSSApplicationConstants.DENTAL, planCarriers);
        planCarrierMap.put(BSSApplicationConstants.VISION, planCarriers);

        Map<String, Set<String>> planCarriersByPlanType = preparePlanCarriersByPlanType();
        Map<String, List<CoverageLevel>> coverageLevelsMap = prepareCoverageLevelsMap();
        Map<String, String> erEEPlansMapping = null;
        List<String> mandatoryPlansToExclude = new ArrayList<>();
        Map<String, Boolean> selectedBenefits = new HashMap<>();
        selectedBenefits.put(Constants.CMTR, true);
        Set<String> medicalPlans = new HashSet<>();
        Set<String> widelyAvailablePlanSet = new HashSet<>();
        Map<String, List<BenefitPlanRate>> planRates = new HashMap<>();
        Map<String, List<PlanPackage>> planPackagesMap = preparePlanPackagesMap();
        Map<String, Set<BenefitPlan>> planPackagePlans = new HashMap<>();
        List<String> medicalPlanIds = new ArrayList<>();
        Map<String, List<String>> benefitPlansStatesMap = null;
        Map<String, BigDecimal> planCostMap = new HashMap<>();
        List<FundingType> fundingTypes = prepareFundingTypes();

        when(portfolioService.findPrimaryPlanCarriers(company)).thenReturn(planCarrierMap);
        when(BenefitCategoriesHelper.getPlanCarriers(planCarrierMap)).thenReturn(primaryPlanCarrierIds);
        when(BenefitCategoriesHelper.getPlanCarriersByPlanType(planCarrierMap)).thenReturn(planCarriersByPlanType);
        when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes,
                company.getRealmPlanYearId())).thenReturn(coverageLevelsMap);
        when(StrategyServiceHelper.getLocations(company)).thenReturn(locations);
        when(employerEmployeePlansMappingDao.getEeAndErPlanMapping(company.getRealmPlanYearId()))
                .thenReturn(erEEPlansMapping);
        when(realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(company.getHeadQuatersState(),
                BigDecimal.valueOf(company.getRealmPlanYearId()))).thenReturn(mandatoryPlansToExclude);
        when(CommonServiceHelper.getOutOfRegionPlansToExclude(any(Company.class), anySet(), any(RealmDataDao.class))).thenReturn(Collections.emptySet());
        when(benefitPlanDao.getAllPrimaryBenefitPlans(primaryPlanCarrierIds, company, Collections.emptySet())).thenReturn(primaryPlanMap);
        when(BenefitCategoriesHelper.getAllBenefitPlans(primaryPlanMap)).thenReturn(medicalPlans);
        when(benefitPlanDao.getWidelyAvailablePlans(Mockito.anySet(), Mockito.anyLong()))
                .thenReturn(widelyAvailablePlanSet);
        when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(planRates);
        when(BenefitCategoriesHelper.getDefaultPlanPackage(company, coverageLevelsMap)).thenReturn(planPackagesMap);
        when(StrategyUtils.getPlanCost(Mockito.any()))
                .thenReturn(planCostMap);
        when(realmDataDao.getRealmFundingTypes(company.getRealmPlanYearId())).thenReturn(fundingTypes);

        BenefitsCategories actualResults;

        /*
         * GIVEN Test TriNetIV Exchange - Not renewal company Commuter benefits
         * NULL medicalPlanIds is empty benefitPlansStatesMap is null
         *
         */
        selectedBenefits.put(Constants.CMTR, null);

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(0, actualResults.getMedical().getPlanCarriers().size());
        assertEquals(0, actualResults.getDental().getPlanCarriers().size());
        assertNull(actualResults.getVision());

        /*
         * GIVEN Test TriNetIV Exchange - Not renewal company No commuter
         * benefits - count of 22 medicalPlanIds is empty;
         *
         */
        selectedBenefits.put(Constants.CMTR, false);
        widelyAvailablePlanSet = new HashSet<>();
        when(benefitPlanDao.getWidelyAvailablePlans(Mockito.anySet(),  Mockito.anyLong()))
                .thenReturn(widelyAvailablePlanSet);
        erEEPlansMapping = new HashMap<>();
        when(employerEmployeePlansMappingDao.getEeAndErPlanMapping(company.getRealmPlanYearId()))
                .thenReturn(erEEPlansMapping);
        planCostMap = preparePlanCostMap();
        when(StrategyUtils.getPlanCost(Mockito.any()))
                .thenReturn(planCostMap);
        when(RulesAndConfigsUtils.getMinFundingType(10)).thenReturn("HQ");
        when(benefitPlanService.getLowestCostPlanPerCarrier(company)).thenReturn(prepareCarrierMinFunding());

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(0, actualResults.getMedical().getPlanCarriers().size());
        assertEquals(0, actualResults.getDental().getPlanCarriers().size());
        assertNull(actualResults.getVision());

        /*
         * GIVEN Test TriNetIV Exchange - Not renewal company Commuter benefits
         * - count of 22 medicalPlanIds has two plans benefitPlansStatesMap is
         * empty;
         *
         */
        selectedBenefits.put(Constants.CMTR, true);
        medicalPlanIds = prepareMedicalIds();
        benefitPlansStatesMap = new HashMap<>();
        widelyAvailablePlanSet = prepareWidelyAvailablePlanSet();
        when(benefitPlanDao.getWidelyAvailablePlans(Mockito.anySet(),  Mockito.anyLong()))
                .thenReturn(widelyAvailablePlanSet);
        erEEPlansMapping = prepareErEePlansMapping();
        when(employerEmployeePlansMappingDao.getEeAndErPlanMapping(company.getRealmPlanYearId()))
                .thenReturn(erEEPlansMapping);

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(0, actualResults.getMedical().getPlanCarriers().size());
        assertEquals(0, actualResults.getDental().getPlanCarriers().size());
        assertNull(actualResults.getVision());

        /*
         * GIVEN Test TriNetIV Exchange - Not renewal company No commuter
         * benefits - count of 2 benefitPlansStatesMap has two plans;
         *
         */
        selectedBenefits.put(Constants.CMTR, false);
        benefitPlansStatesMap = prepareBenefitPlansStatesMap();

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(0, actualResults.getMedical().getPlanCarriers().size());
        assertEquals(0, actualResults.getDental().getPlanCarriers().size());
        assertNull(actualResults.getVision());

        /*
         * GIVEN Test TriNetIV Exchange - Not renewal company Commuter benefits
         * - count of 2
         *
         */
        selectedBenefits.put(Constants.CMTR, true);

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(0, actualResults.getMedical().getPlanCarriers().size());
        assertEquals(0, actualResults.getDental().getPlanCarriers().size());
        assertNull(actualResults.getVision());

        /*
         * GIVEN Test TriNetIV Exchange - Renewal company No commuter benefits -
         * count of 22
         *
         */
        company.setRenewalCompany(true);
        selectedBenefits.put(Constants.CMTR, false);

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(0, actualResults.getMedical().getPlanCarriers().size());
        assertEquals(0, actualResults.getDental().getPlanCarriers().size());
        assertNull(actualResults.getVision());
        assertEquals("CFPCT", company.getDefaultFundingType());

        /*
         * GIVEN Test Not TriNetIV Exchange - Renewal company
         *
         */
        company.getRealm().setBenExchange(BenExchngEnums.TRINET_II.getBenExchng());
        company.setRenewalCompany(true);
        addVisionToPrimaryPlanMap(primaryPlanMap);
        Map<String, Map<String, Set<String>>> autoSelectPlans = prepareAutoSelectPlans();
        when(realmDataDao.getAutoSelectPlansByRealmIdAndPlanTypes(company.getRealmPlanYearId(),
                company, Collections.emptySet())).thenReturn(autoSelectPlans);

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(0, actualResults.getMedical().getPlanCarriers().size());
        assertEquals(0, actualResults.getDental().getPlanCarriers().size());
        assertEquals(0, actualResults.getVision().getPlanCarriers().size());
        assertEquals("CFPCT", company.getDefaultFundingType());
        assertEquals(2, actualResults.getMedical().getBenefitPlans().size());
        assertEquals(2, actualResults.getDental().getBenefitPlans().size());
        assertEquals(1, actualResults.getVision().getBenefitPlans().size());
        assertEquals(0, actualResults.getMedical().getBenefitPlans().iterator().next().getHeadCount());
        assertEquals(BigDecimal.valueOf(1000),
                actualResults.getMedical().getBenefitPlans().iterator().next().getContributions().get(0).getPlanCost());
        for (BenefitPlan bp : actualResults.getMedical().getBenefitPlans()) {
            assertEquals(Arrays.asList("All"), bp.getOfferedStates());
            if (("002AHE").equals(bp.getId())) {
                assertTrue(bp.isPpoPlan());
                assertEquals("003TWZ", bp.getOptionalPlans());
                assertEquals(new HashSet<String>(Arrays.asList("0038QP", "0038QM", "002AHE")), bp.getCrossRefPlans());
            } else if (("002AHF").equals(bp.getId())) {
                assertTrue(!bp.isPpoPlan());
                assertNull(bp.getOptionalPlans());
                assertNull(bp.getCrossRefPlans());
            }
        }

        /*
         * GIVEN TriNet OMS Exchange - Renewal company
         *
         */
        company.getRealm().setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
        company.setOmsOffering(OMB_TLD.name());
        company.setRenewalCompany(false);
        company.setProspectCompany(true);
        addVisionToPrimaryPlanMap(primaryPlanMap);


        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(2, actualResults.getDental().getBenefitPlans().size());
        assertEquals(1, actualResults.getVision().getBenefitPlans().size());
        assertEquals(0, actualResults.getMedical().getBenefitPlans().iterator().next().getHeadCount());


        /*
         * GIVEN TriNet OMS Exchange TIB - Prospect
         * all plans should come from HRIS
         *
         */
        company.getRealm().setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
        company.setRenewalCompany(false);
        company.setProspectCompany(true);
        company.setOmsOffering(OM_OD_OV_TLD.name());
        addVisionToPrimaryPlanMap(primaryPlanMap);

        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.MEDICAL)).thenReturn(populateHrisPlanResponseList());
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.DENTAL)).thenReturn(populateHrisPlanResponseList());
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.VISION)).thenReturn(populateHrisPlanResponseList());

        // when
        actualResults = benefitCategoriesService.constructBenefitsCategories(company);

        // then
        assertEquals(1, actualResults.getMedical().getBenefitPlans().size());
        assertEquals(1, actualResults.getDental().getBenefitPlans().size());
        assertEquals(1, actualResults.getVision().getBenefitPlans().size());

        // verify
        strategyUtilsMock.verify(() -> StrategyUtils.getPlanCost(Mockito.any()), times(39));

    }

	/// ********************************SETUP********************//

	private List<CarrierMinimumFunding> prepareCarrierMinFunding() {
		CarrierMinimumFunding cmf = new CarrierMinimumFunding(1111L, "10", BigDecimal.valueOf(550));
		return new ArrayList<>(Arrays.asList(cmf));
	}

	private Company prepareCompany() {

		Company company = new Company();
		company.setRealmPlanYearId(10L);
		company.setHeadQuatersState("FL");
		company.setHeadQuatersCity("TAMPA");
		company.setCode("HAL");
		company.setPlanStartDate("01-JAN-2019");
		company.setTexasSitus(false);

		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_IV.getBenExchng());
		company.setRealm(realm);

		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(2);
		company.setRealmPlanYear(realmPlanYear);

		Industry industry = new Industry(1);
		industry.setIndustryType(IndustryType.AG);
		company.setIndustry(industry);

		return company;
	}

	private Map<String, Set<String>> preparePlanCarriersByPlanType() {
		Map<String, Set<String>> planCarriersByPlanType = new HashMap<>();
		Set<String> carriers = new HashSet<>();
		carriers.add("1");
		carriers.add("12");
		planCarriersByPlanType.put(BSSApplicationConstants.MEDICAL, carriers);

		carriers = new HashSet<>();
		carriers.add("1");
		carriers.add("14");
		planCarriersByPlanType.put(BSSApplicationConstants.DENTAL, carriers);

		carriers = new HashSet<>();
		carriers.add("1");
		planCarriersByPlanType.put(BSSApplicationConstants.VISION, carriers);

		return planCarriersByPlanType;
	}

	private Map<String, Set<StateBenefitPlan>> preparePrimaryPlanMap() {

		// Medical
		Set<StateBenefitPlan> stateBenefitPlans = new HashSet<>();
		StateBenefitPlan stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan("002AHE");
		stateBenefitPlan.setDescription("Aetna HNO 25 1 GA");
		stateBenefitPlan.setPlanType("10");
		stateBenefitPlan.setVendorId("AETNASOI");
		stateBenefitPlan.setPortfolioId(1);
		stateBenefitPlan.setRealmYearId(21);
		stateBenefitPlan.setMandatory(false);
		stateBenefitPlans.add(stateBenefitPlan);
		stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan("002AHF");
		stateBenefitPlan.setDescription("Aetna HNO 25 1 IL");
		stateBenefitPlan.setPlanType("10");
		stateBenefitPlan.setVendorId("AETNASOI");
		stateBenefitPlan.setPortfolioId(1);
		stateBenefitPlan.setRealmYearId(21);
		stateBenefitPlan.setMandatory(false);
		stateBenefitPlans.add(stateBenefitPlan);
		primaryPlanMap.put(BSSApplicationConstants.MEDICAL, stateBenefitPlans);

		// Dental
		stateBenefitPlans = new HashSet<>();
		stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan("0038Q4");
		stateBenefitPlan.setDescription("Aetna Dental 100 Group");
		stateBenefitPlan.setPlanType("11");
		stateBenefitPlan.setVendorId("AETNASOI");
		stateBenefitPlan.setPortfolioId(1);
		stateBenefitPlan.setRealmYearId(21);
		stateBenefitPlan.setMandatory(false);
		stateBenefitPlans.add(stateBenefitPlan);
		stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan("0038Q5");
		stateBenefitPlan.setDescription("Aetna Dental 100 Group NV");
		stateBenefitPlan.setPlanType("11");
		stateBenefitPlan.setVendorId("AETNASOI");
		stateBenefitPlan.setPortfolioId(1);
		stateBenefitPlan.setRealmYearId(21);
		stateBenefitPlan.setMandatory(false);
		stateBenefitPlans.add(stateBenefitPlan);
		stateBenefitPlans.add(stateBenefitPlan);
		primaryPlanMap.put(BSSApplicationConstants.DENTAL, stateBenefitPlans);

		return primaryPlanMap;
	}

	private List<HrisPlanResponse> populateHrisPlanResponseList() {
		return List.of(
				HrisPlanResponse.builder()
						.planId(12343)
						.planName("Some medical plan 1")
						.carrierId(1)
						.carrierName("Carrier A")
						.rateDetails(HrisPlanResponse.RateDetails.builder()
								.rateType("4tier")
								.ratesByZip(Arrays.asList(
										HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(Arrays.asList("12434"))
												.rates(Arrays.asList(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("1")
																.rate(234.43)
																.build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("2")
																.rate(234.43)
																.build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("C")
																.rate(234.43)
																.build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("4")
																.rate(234.43)
																.build()
												))
												.build()
								))
								.build())
						.build()
		);
	}

	private void addVisionToPrimaryPlanMap(Map<String, Set<StateBenefitPlan>> primaryPlanMap) {

		List<String> offeredStates = Arrays.asList("FL", "GA");

		// Vision
		Set<StateBenefitPlan> stateBenefitPlans = new HashSet<>();
		StateBenefitPlan stateBenefitPlan;
		stateBenefitPlan = new StateBenefitPlan();
		stateBenefitPlan.setBenefitPlan("0038VV");
		stateBenefitPlan.setDescription("Aetna Vision 100 Group");
		stateBenefitPlan.setPlanType("14");
		stateBenefitPlan.setOfferedStates(offeredStates);
		stateBenefitPlan.setVendorId("AETNASOI");
		stateBenefitPlan.setPortfolioId(1);
		stateBenefitPlan.setRealmYearId(21);
		stateBenefitPlan.setMandatory(false);
		stateBenefitPlans.add(stateBenefitPlan);
		primaryPlanMap.put(BSSApplicationConstants.VISION, stateBenefitPlans);

	}

	private Map<String, Map<String, Set<String>>> prepareAutoSelectPlans() {
		Map<String, Map<String, Set<String>>> autoSelectPlans = new HashMap<>();
		Map<String, Set<String>> planSetMap = new HashMap<>();
		Set<String> planSet = new HashSet<>();
		planSet.add("002AHE");
		planSet.add("0038QM");
		planSet.add("0038QP");
		planSetMap.put("002AHE", planSet);
		planSetMap.put("0038QM", planSet);
		planSetMap.put("0038QP", planSet);
		autoSelectPlans.put(BSSApplicationConstants.MEDICAL, planSetMap);
		return autoSelectPlans;

	}

	private Set<String> prepareWidelyAvailablePlanSet() {
		Set<String> widelyAvailablePlanSet = new HashSet<>();
		widelyAvailablePlanSet.add("002AHE");
		return widelyAvailablePlanSet;
	}

	private Map<String, String> prepareErEePlansMapping() {
		Map<String, String> erEEPlansMapping = new HashMap<>();
		erEEPlansMapping.put("003TWZ", "002AHE");
		erEEPlansMapping.put("002AHE", "003TWZ");
		return erEEPlansMapping;
	}

	private List<String> prepareMedicalIds() {
		List<String> medicalPlanIds = new ArrayList<>();
		medicalPlanIds.add("002AHE");
		medicalPlanIds.add("002AHF");
		return medicalPlanIds;
	}

	private Map<String, List<String>> prepareBenefitPlansStatesMap() {
		Map<String, List<String>> benefitPlansStatesMap = new HashMap<>();
		List<String> states = new ArrayList<>();
		states.add("FL");
		states.add("GA");
		benefitPlansStatesMap.put("002AHE", states);
		benefitPlansStatesMap.put("002AHF", states);

		return benefitPlansStatesMap;
	}

	private Map<String, List<PlanPackage>> preparePlanPackagesMap() {

		Map<String, List<PlanPackage>> planPackagesMap = new HashMap<>();

		// Medical
		PlanPackage planPackage = new PlanPackage();
		planPackage.setId(0);
		planPackage.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planPackagesMap.put(BSSApplicationConstants.MEDICAL, Arrays.asList(planPackage));

		// Dental
		planPackage = new PlanPackage();
		planPackage.setId(0);
		planPackage.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		planPackagesMap.put(BSSApplicationConstants.DENTAL, Arrays.asList(planPackage));

		// Vision
		planPackage = new PlanPackage();
		planPackage.setId(0);
		planPackage.setPlanType(BSSApplicationConstants.VISION_PLAN_TYPE);
		planPackagesMap.put(BSSApplicationConstants.VISION, Arrays.asList(planPackage));

		return planPackagesMap;
	}

	private Map<String, BigDecimal> preparePlanCostMap() {

		Map<String, BigDecimal> planCostMap = new HashMap<>();
		planCostMap.put("employee", new BigDecimal(1000));
		planCostMap.put("employeePlusSpouse", new BigDecimal(2000));
		planCostMap.put("employeePlusChild", new BigDecimal(3000));
		planCostMap.put("employeePlusFamily", new BigDecimal(4000));
		return planCostMap;

	}

	private List<FundingType> prepareFundingTypes() {
		List<FundingType> fundingTypes = new ArrayList<>();
		FundingType fundingType = new FundingType();
		fundingType.setId("BFPCT");
		fundingType.setDescription("Base Plan Percent");
		fundingType.setDefaultFunding(false);
		fundingTypes.add(fundingType);
		fundingType = new FundingType();
		fundingType.setId("CFPCT");
		fundingType.setDescription("Covered Person Percent");
		fundingType.setDefaultFunding(true);
		fundingTypes.add(fundingType);
		fundingType = new FundingType();
		fundingType.setId("FLT");
		fundingType.setDescription("Flat");
		fundingType.setDefaultFunding(false);
		fundingTypes.add(fundingType);
		return fundingTypes;
	}

	private Map<String, List<CoverageLevel>> prepareCoverageLevelsMap() {
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
		List<CoverageLevel> coverageLevels = new ArrayList<>();
		CoverageLevel coverageLevel1 = new CoverageLevel(CoverageCodesEnums.COV_ALL);
		CoverageLevel coverageLevel2 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE);
		CoverageLevel coverageLevel3 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD);
		CoverageLevel coverageLevel4 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY);
		coverageLevels.addAll(Arrays.asList(coverageLevel1, coverageLevel2, coverageLevel3, coverageLevel4));

		mapOfCoverageLevels.put(Constants.MEDICAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.DENTAL, coverageLevels);
		mapOfCoverageLevels.put(Constants.VISION, coverageLevels);
		return mapOfCoverageLevels;
	}
}