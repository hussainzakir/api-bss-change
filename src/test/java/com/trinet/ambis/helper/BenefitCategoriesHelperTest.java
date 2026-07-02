package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.StrategyUtils;

/**
 * @author hliddle
 *
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class BenefitCategoriesHelperTest {

    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMockedStatic;

    @Before
    public void setUp() {
        commonServiceHelperMockedStatic = org.mockito.Mockito.mockStatic(CommonServiceHelper.class);
        benefitCategoriesHelperMockedStatic = org.mockito.Mockito.mockStatic(BenefitCategoriesHelper.class);
    }

    @After
    public void tearDown() {
        commonServiceHelperMockedStatic.close();
        benefitCategoriesHelperMockedStatic.close();
    }

    @Test
    public void getPlanCarriers() {

        Map<String, Set<PlanCarrier>> planCarrierMap;
        Set<String> actualResult;

        /*
         * Test when the planCarrier set is null
         */
        planCarrierMap = preparePlanCarrierMapWithNull();
        actualResult = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
        assertEquals(0, actualResult.size());

        /*
         * Test when there are at least one each of M/D/V
         */
        planCarrierMap = preparePlanCarrierMap();
        actualResult = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
        assertEquals(7, actualResult.size());
    }

    @Test
    public void getMedicalPlanCarriers() {
        Map<String, Set<PlanCarrier>> planCarrierMap = preparePlanCarrierMap();

        Set<String> actualResult = BenefitCategoriesHelper.getMedicalPlanCarriers(planCarrierMap);

        assertEquals(3, actualResult.size());
    }

    @Test
    public void getMandatoryPlanCarriers() {

        Map<String, Set<PlanCarrier>> planCarrierMap;
        Map<String, Set<Long>> actualResult;

        /*
         * Test when the planCarrier set is null
         */
        planCarrierMap = preparePlanCarrierMapWithNull();
        actualResult = BenefitCategoriesHelper.getMandatoryPlanCarriers(planCarrierMap);
        assertEquals(0, actualResult.size());

        /*
         * Test when there are at least one each of M/D/V
         */
        planCarrierMap = preparePlanCarrierMap();
        actualResult = BenefitCategoriesHelper.getMandatoryPlanCarriers(planCarrierMap);
        assertEquals(3, actualResult.size());
        assertEquals(2, actualResult.get(BSSApplicationConstants.MEDICAL).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.DENTAL).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.VISION).size());
    }

    @Test
    public void getPlanCarriersByPlanType() {

        Map<String, Set<PlanCarrier>> planCarrierMap;
        Map<String, Set<String>> actualResult;

        /*
         * Test when there are at least one each of M/D/V
         */
        planCarrierMap = preparePlanCarrierMap();
        actualResult = BenefitCategoriesHelper.getPlanCarriersByPlanType(planCarrierMap);
        assertEquals(4, actualResult.size());
        assertEquals(3, actualResult.get(BSSApplicationConstants.MEDICAL).size());
        assertEquals(2, actualResult.get(BSSApplicationConstants.DENTAL).size());
        assertEquals(2, actualResult.get(BSSApplicationConstants.VISION).size());
    }

    @Test
    public void getAllBenefitPlans() {

        Map<String, Set<StateBenefitPlan>> primaryPlanMap = null;
        Set<String> actualResult;

        /*
         * Test when there are at least one each of M/D/V
         */
        primaryPlanMap = preparePrimaryPlanMap();
        actualResult = BenefitCategoriesHelper.getAllBenefitPlans(primaryPlanMap);
        assertEquals(5, actualResult.size());
    }

    @Test
    public void getAllMedicalPlanIds() {

        Map<String, Set<StateBenefitPlan>> primaryPlanMap = null;
        List<String> actualResult;
        /*
         * Test when there are at least one each of M/D/V
         */
        primaryPlanMap = preparePrimaryPlanMap();
        actualResult = BenefitCategoriesHelper.getAllMedicalPlanIds(primaryPlanMap);
        assertEquals(2, actualResult.size());

        /*
         * Test when MEDICAL is null
         */
        primaryPlanMap.put(BSSApplicationConstants.MEDICAL, null);
        actualResult = BenefitCategoriesHelper.getAllMedicalPlanIds(primaryPlanMap);
        assertEquals(0, actualResult.size());

        /*
         * Test when there is no MEDICAL
         */
        primaryPlanMap.remove(BSSApplicationConstants.MEDICAL);
        actualResult = BenefitCategoriesHelper.getAllMedicalPlanIds(primaryPlanMap);
        assertEquals(0, actualResult.size());
    }

    @Test
    public void getDefaultPlanPackage() {
        Company company = prepareCompany();
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = prepareCoverageLevelsMap();

        // Mock static dependencies
        commonServiceHelperMockedStatic.when(() -> CommonServiceHelper.extractMinFundingDetails("medical", company))
                .thenReturn(new MinimumFunding("10", "pct", BigDecimal.valueOf(1200), true));
        commonServiceHelperMockedStatic.when(() -> CommonServiceHelper.extractMinFundingDetails("dental", company))
                .thenReturn(new MinimumFunding("11", "pct", BigDecimal.valueOf(400), true));
        commonServiceHelperMockedStatic.when(() -> CommonServiceHelper.extractMinFundingDetails("vision", company))
                .thenReturn(new MinimumFunding("14", "flt", BigDecimal.valueOf(150), true));
        // Call the method under test
        Map<String, List<PlanPackage>> actualResult = BenefitCategoriesHelper.getDefaultPlanPackage(company, mapOfCoverageLevels);

        // Assert
        assertEquals(3, actualResult.size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.MEDICAL).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.DENTAL).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.VISION).size());
        assertEquals(null, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get(Constants.COVERAGE_ALL));
        assertEquals(BigDecimal.valueOf(1200), actualResult.get(BSSApplicationConstants.MEDICAL).get(0)
                .getCoverageLevelFunding().get(Constants.EMPLOYEE));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get("employeePlusSpouse"));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get("employeePlusChild"));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get("employeePlusFamily"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get(Constants.COVERAGE_ALL));
        assertEquals(BigDecimal.valueOf(400), actualResult.get(BSSApplicationConstants.DENTAL).get(0)
                .getCoverageLevelFunding().get(Constants.EMPLOYEE));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get("employeePlusSpouse"));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get("employeePlusChild"));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get("employeePlusFamily"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get(Constants.COVERAGE_ALL));
        assertEquals(BigDecimal.valueOf(150), actualResult.get(BSSApplicationConstants.VISION).get(0)
                .getCoverageLevelFunding().get(Constants.EMPLOYEE));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get("employeePlusSpouse"));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get("employeePlusChild"));
        assertEquals(BigDecimal.ZERO, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get("employeePlusFamily"));

        /*
         * Test when "CFPCT" is not default funding
         */
        company.setDefaultFundingType(BSSApplicationConstants.FLAT);
        actualResult = BenefitCategoriesHelper.getDefaultPlanPackage(company, mapOfCoverageLevels);
        assertEquals(3, actualResult.size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.MEDICAL).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.DENTAL).size());
        assertEquals(1, actualResult.get(BSSApplicationConstants.VISION).size());
        assertEquals(null, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get(Constants.COVERAGE_ALL));
        assertEquals(BigDecimal.valueOf(1200), actualResult.get(BSSApplicationConstants.MEDICAL).get(0)
                .getCoverageLevelFunding().get(Constants.EMPLOYEE));
        assertEquals(null, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get("employeePlusSpouse"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get("employeePlusChild"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.MEDICAL).get(0).getCoverageLevelFunding()
                .get("employeePlusFamily"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get(Constants.COVERAGE_ALL));
        assertEquals(BigDecimal.valueOf(400), actualResult.get(BSSApplicationConstants.DENTAL).get(0)
                .getCoverageLevelFunding().get(Constants.EMPLOYEE));
        assertEquals(null, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get("employeePlusSpouse"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get("employeePlusChild"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.DENTAL).get(0).getCoverageLevelFunding()
                .get("employeePlusFamily"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get(Constants.COVERAGE_ALL));
        assertEquals(BigDecimal.valueOf(150), actualResult.get(BSSApplicationConstants.VISION).get(0)
                .getCoverageLevelFunding().get(Constants.EMPLOYEE));
        assertEquals(null, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get("employeePlusSpouse"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get("employeePlusChild"));
        assertEquals(null, actualResult.get(BSSApplicationConstants.VISION).get(0).getCoverageLevelFunding()
                .get("employeePlusFamily"));
    }

    @Test
    public void updatePlanPackageNewClients() {

        Map<String, List<PlanPackage>> planPackagesMap = preparePlanPackagesMap();
        Map<String, Set<BenefitPlan>> planPackagePlans = preparePlanPackagePlans();
        Map<String, Map<String, Set<String>>> autoSelectPlans;
        Map<String, Set<StateBenefitPlan>> primaryPlanMap = preparePrimaryPlanMap();
        Map<String, String> erEEPlansMapping = prepareErEePlansMapping();

        /*
         * Test when realmPlanYear pickChooseFlag is false With no autoSelectPlans
         */
        autoSelectPlans = new HashMap<String, Map<String, Set<String>>>();
        BenefitCategoriesHelper.updatePlanPackageNewClients(planPackagesMap, planPackagePlans, autoSelectPlans,
                primaryPlanMap, erEEPlansMapping);
        assertEquals(1, planPackagesMap.get(BSSApplicationConstants.MEDICAL).size());
        assertEquals(2, planPackagesMap.get(BSSApplicationConstants.MEDICAL).get(0).getBenefitPlans().size());

        /*
         * Test when realmPlanYear pickChooseFlag is true With autoSelectPlans
         */
        autoSelectPlans = prepareAutoSelectPlans();
        BenefitCategoriesHelper.updatePlanPackageNewClients(planPackagesMap, planPackagePlans, autoSelectPlans,
                primaryPlanMap, erEEPlansMapping);
        assertEquals(1, planPackagesMap.get(BSSApplicationConstants.MEDICAL).size());
        assertEquals(3, planPackagesMap.get(BSSApplicationConstants.MEDICAL).get(0).getBenefitPlans().size());

    }

    @Test
    public void addCrossRefTemplatePlans() {

        Map<String, String> erEEPlansMapping = prepareErEePlansMapping();
        Map<String, List<PlanPackage>> planPackagesMap = preparePlanPackagesMap();
        for (Entry<String, List<PlanPackage>> entry : planPackagesMap.entrySet()) {
            if (BSSApplicationConstants.MEDICAL.equals(entry.getKey())) {
                entry.getValue().get(0).setBenefitPlans(Arrays.asList("MED1", "MED2"));
                BenefitCategoriesHelper.addCrossRefTemplatePlans(erEEPlansMapping, entry.getValue().get(0));
                assertEquals(3, entry.getValue().get(0).getBenefitPlans().size());
            } else if (BSSApplicationConstants.DENTAL.equals(entry.getKey())) {
                entry.getValue().get(0).setBenefitPlans(Arrays.asList("DEN1"));
                BenefitCategoriesHelper.addCrossRefTemplatePlans(erEEPlansMapping, entry.getValue().get(0));
                assertEquals(2, entry.getValue().get(0).getBenefitPlans().size());
            } else if (BSSApplicationConstants.VISION.equals(entry.getKey())) {
                entry.getValue().get(0).setBenefitPlans(Arrays.asList("VIS1"));
                BenefitCategoriesHelper.addCrossRefTemplatePlans(erEEPlansMapping, entry.getValue().get(0));
                assertEquals(1, entry.getValue().get(0).getBenefitPlans().size());
            }
        }

    }

    @Test
    public void updatePlanCarrierExclusivity() {
        Company company = prepareCompany();

        Map<String, Set<PlanCarrier>> planCarrierMap;

        /*
         * Test when not a renewal company, null M/D/V planCarrierMap
         */
        planCarrierMap = preparePlanCarrierMapWithNull();
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertNull(planCarrierMap.get(BSSApplicationConstants.MEDICAL));

        /*
         * Test with renewal company, null M/D/V planCarrierMap
         */
        company.setRenewalCompany(true);
        planCarrierMap = preparePlanCarrierMapWithNull();
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertNull(planCarrierMap.get(BSSApplicationConstants.MEDICAL));

        /*
         * Test with renewal company, null M/D/V planCarrierMap, BSBF Exclusive
         */
        company.setExclusiveMedPlan(BSSApplicationConstants.T2_EXCL_MED_PLAN_BCBSFL);
        planCarrierMap = preparePlanCarrierMapWithNull();
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertNull(planCarrierMap.get(BSSApplicationConstants.MEDICAL));

        /*
         * Test with renewal company, empty M/D/V planCarrierMap, BSBF Exclusive
         */
        planCarrierMap = preparePlanCarrierMapWithEmptySet();
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertEquals(0, planCarrierMap.get(BSSApplicationConstants.MEDICAL).size());

        /*
         * Test with renewal company, Aetna M/D/V planCarrierMap, BSBF Exclusive
         */
        planCarrierMap = preparePlanCarrierMap();
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertEquals(1, planCarrierMap.get(BSSApplicationConstants.MEDICAL).size());

        /*
         * Test with renewal company, Aetna and FloridaBlue Portfolio M/D/V
         * planCarrierMap, BSBF Exclusive
         */
        planCarrierMap = preparePlanCarrierMap();
        PlanCarrier planCarrier = new PlanCarrier();
        planCarrier.setId(12);
        planCarrier.setMandatory(true);
        planCarrierMap.get(BSSApplicationConstants.MEDICAL).add(planCarrier);
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertEquals(2, planCarrierMap.get(BSSApplicationConstants.MEDICAL).size());

        /*
         * Test with renewal company, FloridaBlue Portfolio M/D/V planCarrierMap, BSBF
         * Exclusive
         */
        planCarrierMap = preparePlanCarrierMap();
        planCarrier = new PlanCarrier();
        planCarrier.setId(12);
        planCarrier.setMandatory(true);
        planCarrierMap.get(BSSApplicationConstants.MEDICAL).clear();
        planCarrierMap.get(BSSApplicationConstants.MEDICAL).add(planCarrier);
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertEquals(1, planCarrierMap.get(BSSApplicationConstants.MEDICAL).size());

        /*
         * Test with renewal company, non-Aetna or FloridaBlue Portfolio M/D/V
         * planCarrierMap, BSBF Exclusive
         */
        planCarrierMap = preparePlanCarrierMap();
        planCarrier = new PlanCarrier();
        planCarrier.setId(3);
        planCarrier.setMandatory(true);
        planCarrierMap.get(BSSApplicationConstants.MEDICAL).clear();
        planCarrierMap.get(BSSApplicationConstants.MEDICAL).add(planCarrier);
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertEquals(1, planCarrierMap.get(BSSApplicationConstants.MEDICAL).size());

        // New tests to cover parentId List<String> logic and null-safe filtering
        // Case: portfolio has parentId list containing AETNA_PORTFOLIO -> should be excluded
        planCarrierMap = preparePlanCarrierMapWithEmptySet();
        PlanCarrier planCarrier1 = new PlanCarrier();
        planCarrier1.setId(99);
        planCarrier1.setMandatory(false);
        planCarrier1.setParentId(Arrays.asList(BSSApplicationConstants.AETNA_PORTFOLIO));
        planCarrierMap.get(BSSApplicationConstants.MEDICAL).add(planCarrier1);
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertEquals(0, planCarrierMap.get(BSSApplicationConstants.MEDICAL).size());

        // Case: portfolio has parentId list with mixed values including nulls and other IDs -> should be kept
        planCarrierMap = preparePlanCarrierMapWithEmptySet();
        PlanCarrier planCarrier2 = new PlanCarrier();
        planCarrier2.setId(100);
        planCarrier2.setMandatory(false);
        planCarrier2.setRestricted(true);
        planCarrier2.setParentId(Arrays.asList(null, "123", "456"));
        planCarrierMap.get(BSSApplicationConstants.MEDICAL).add(planCarrier2);
        BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());
        assertEquals(1, planCarrierMap.get(BSSApplicationConstants.MEDICAL).size());
    }

    @Test
    public void updatePlanPackagesPickAndChoose() {

        Map<String, List<PlanPackage>> planPackagesMap = preparePlanPackagesMap();
        Set<PlanPackage> planPackageList = new HashSet<PlanPackage>(
                planPackagesMap.get(BSSApplicationConstants.MEDICAL));
        List<String> benefitPlanIds = Arrays.asList("MEDADD1", "MEDADD2");

        BenefitCategoriesHelper.updatePlanPackagesPickAndChoose(planPackageList, benefitPlanIds);
        assertEquals(benefitPlanIds, planPackageList.iterator().next().getBenefitPlans());
    }

    @Test
    public void calculateCarrierMinFunding_test1() {
        Company company = new Company();
        String benefitOfferType = "medical";
        MinimumFunding mf = new MinimumFunding("medical", "FLT", BigDecimal.valueOf(1020), true);

        commonServiceHelperMockedStatic.when(() -> CommonServiceHelper.extractMinFundingDetails(benefitOfferType, company))
                .thenReturn(mf);
        List<CarrierMinimumFunding> lowestCostPlanPerCarrier = prepareCarrierMinFunding();

        Map<Long, BigDecimal> actualResult = BenefitCategoriesHelper.calculateCarrierMinFunding(company,
                benefitOfferType, lowestCostPlanPerCarrier);

        assertEquals(BigDecimal.valueOf(1020), actualResult.get(1111L));
    }

    @Test
    public void calculateCarrierMinFunding_test2() {
        Company company = new Company();
        String benefitOfferType = "medical";
        MinimumFunding mf = new MinimumFunding("medical", "PCT", BigDecimal.valueOf(80), true);

        commonServiceHelperMockedStatic.when(() -> CommonServiceHelper.extractMinFundingDetails(benefitOfferType, company))
                .thenReturn(mf);
        List<CarrierMinimumFunding> lowestCostPlanPerCarrier = prepareCarrierMinFunding();

        Map<Long, BigDecimal> actualResult = BenefitCategoriesHelper.calculateCarrierMinFunding(company,
                benefitOfferType, lowestCostPlanPerCarrier);

        assertEquals(BigDecimal.valueOf(440.00).setScale(2, RoundingMode.HALF_UP), actualResult.get(1111L));
        assertEquals(BigDecimal.valueOf(960.00).setScale(2, RoundingMode.HALF_UP), actualResult.get(2222L));
    }

    private List<CarrierMinimumFunding> prepareCarrierMinFunding() {
        List<CarrierMinimumFunding> cmfs = new ArrayList<>();
        CarrierMinimumFunding cmf = new CarrierMinimumFunding(1111L, "10", BigDecimal.valueOf(550));
        cmfs.add(cmf);
        cmf = new CarrierMinimumFunding(2222L, "10", BigDecimal.valueOf(1200));
        cmfs.add(cmf);
        cmf = new CarrierMinimumFunding(2222L, "11", BigDecimal.valueOf(300));
        cmfs.add(cmf);
        return cmfs;
    }

    /*
     *
     * Setup methods
     *
     */
    private Map<String, Set<PlanCarrier>> preparePlanCarrierMapWithNull() {

        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<String, Set<PlanCarrier>>();

        Set<PlanCarrier> planCarriers = null;
        planCarrierMap.put(BSSApplicationConstants.MEDICAL, planCarriers);
        planCarrierMap.put(BSSApplicationConstants.DENTAL, planCarriers);
        planCarrierMap.put(BSSApplicationConstants.VISION, planCarriers);

        return planCarrierMap;
    }

    private Map<String, Set<PlanCarrier>> preparePlanCarrierMapWithEmptySet() {

        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<String, Set<PlanCarrier>>();

        Set<PlanCarrier> planCarriers = new HashSet<PlanCarrier>();
        planCarrierMap.put(BSSApplicationConstants.MEDICAL, planCarriers);
        planCarrierMap.put(BSSApplicationConstants.DENTAL, planCarriers);
        planCarrierMap.put(BSSApplicationConstants.VISION, planCarriers);

        return planCarrierMap;
    }

    private Map<String, Set<PlanCarrier>> preparePlanCarrierMap() {

        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<String, Set<PlanCarrier>>();

        Set<PlanCarrier> planCarriers;

        // MEDICAL
        planCarriers = new HashSet<PlanCarrier>();
        PlanCarrier planCarrier = new PlanCarrier();
        planCarrier.setId(1);
        planCarrier.setMandatory(true);
        planCarriers.add(planCarrier);
        planCarrier = new PlanCarrier();
        planCarrier.setId(2);
        planCarrier.setParentId(Arrays.asList("1"));
        planCarrier.setMandatory(false);
        planCarriers.add(planCarrier);
        planCarrier = new PlanCarrier();
        planCarrier.setId(3);
        planCarrier.setMandatory(true);
        planCarriers.add(planCarrier);
        planCarrierMap.put(BSSApplicationConstants.MEDICAL, planCarriers);

        // DENTAL
        planCarriers = new HashSet<PlanCarrier>();
        planCarrier = new PlanCarrier();
        planCarrier.setId(11);
        planCarrier.setMandatory(true);
        planCarriers.add(planCarrier);
        planCarrier = new PlanCarrier();
        planCarrier.setId(12);
        planCarrier.setMandatory(false);
        planCarriers.add(planCarrier);
        planCarrierMap.put(BSSApplicationConstants.DENTAL, planCarriers);

        // VISION
        planCarriers = new HashSet<PlanCarrier>();
        planCarrier = new PlanCarrier();
        planCarrier.setId(21);
        planCarrier.setMandatory(false);
        planCarriers.add(planCarrier);
        planCarrier = new PlanCarrier();
        planCarrier.setId(22);
        planCarrier.setMandatory(true);
        planCarriers.add(planCarrier);
        planCarrierMap.put(BSSApplicationConstants.VISION, planCarriers);

        // ADDITIONAL
        planCarriers = new HashSet<PlanCarrier>();
        planCarrier = new PlanCarrier();
        planCarrier.setId(31);
        planCarriers.add(planCarrier);
        planCarrierMap.put(BSSApplicationConstants.ADDITIONAL, planCarriers);

        return planCarrierMap;
    }

    private Map<String, Set<StateBenefitPlan>> preparePrimaryPlanMap() {
        Map<String, Set<StateBenefitPlan>> primaryPlanMap = new HashMap<String, Set<StateBenefitPlan>>();
        List<String> offeredStates = Arrays.asList("FL", "GA");

        // Medical
        Set<StateBenefitPlan> stateBenefitPlans = new HashSet<StateBenefitPlan>();
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
        stateBenefitPlans = new HashSet<StateBenefitPlan>();
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

        // Vision
        stateBenefitPlans = new HashSet<StateBenefitPlan>();
        stateBenefitPlan = new StateBenefitPlan();
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

        return primaryPlanMap;
    }

    private Company prepareCompany() {

        Company company = new Company();
        company.setRealmPlanYearId(10L);
        company.setHeadQuatersState("FL");
        company.setHeadQuatersCity("TAMPA");
        company.setCode("HAL");
        company.setPlanStartDate("01-JAN-2019");
        company.setTexasSitus(false);
        company.setDefaultFundingType(BSSApplicationConstants.CFPCT);

        Realm realm = new Realm();
        realm.setBenExchange(BenExchngEnums.TRINET_IV.getBenExchng());
        company.setRealm(realm);

        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setId(2);
        realmPlanYear.setMinFunding(1000);
        company.setRealmPlanYear(realmPlanYear);

        Industry industry = new Industry(1);
        industry.setIndustryType(IndustryType.AG);
        company.setIndustry(industry);

        return company;
    }

    private Map<String, List<CoverageLevel>> prepareCoverageLevelsMap() {
        Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<String, List<CoverageLevel>>();
        List<CoverageLevel> coverageLevels = new ArrayList<CoverageLevel>();
        CoverageLevel coverageLevel0 = new CoverageLevel(CoverageCodesEnums.COV_ALL);
        CoverageLevel coverageLevel1 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE);
        CoverageLevel coverageLevel2 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE);
        CoverageLevel coverageLevel3 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD);
        CoverageLevel coverageLevel4 = new CoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_FAMILY);
        coverageLevels
                .addAll(Arrays.asList(coverageLevel0, coverageLevel1, coverageLevel2, coverageLevel3, coverageLevel4));

        mapOfCoverageLevels.put(Constants.MEDICAL, coverageLevels);
        mapOfCoverageLevels.put(Constants.DENTAL, coverageLevels);
        mapOfCoverageLevels.put(Constants.VISION, coverageLevels);
        return mapOfCoverageLevels;
    }

    private Map<String, Map<String, Set<String>>> prepareAutoSelectPlans() {
        Map<String, Map<String, Set<String>>> autoSelectPlans = new HashMap<String, Map<String, Set<String>>>();
        Map<String, Set<String>> planSetMap = new HashMap<String, Set<String>>();
        Set<String> planSet = new HashSet<String>();
        planSet.add("MED1");
        planSet.add("MED3");
        planSetMap.put("MED1", planSet);
        planSetMap.put("MED3", planSet);
        autoSelectPlans.put(BSSApplicationConstants.MEDICAL, planSetMap);
        autoSelectPlans.put(BSSApplicationConstants.DENTAL, null);

        return autoSelectPlans;
    }

    private Map<String, String> prepareErEePlansMapping() {
        Map<String, String> erEEPlansMapping = new HashMap<String, String>();
        erEEPlansMapping.put("MED1", "MED3");
        erEEPlansMapping.put("MED3", "MED1");
        erEEPlansMapping.put("DEN1", "DEN3");
        erEEPlansMapping.put("DEN3", "DEN1");
        return erEEPlansMapping;
    }

    private Map<String, List<PlanPackage>> preparePlanPackagesMap() {
        Map<String, List<PlanPackage>> planPackagesMap = new HashMap<String, List<PlanPackage>>();

        List<PlanPackage> planPackages;
        PlanPackage planPackage;

        // MEDICAL
        planPackages = new ArrayList<PlanPackage>();
        planPackage = new PlanPackage();

        // PREMIER
        planPackage.setId(0);
        planPackage.setStrategyId(0L);
        planPackage.setFundingType(BSSApplicationConstants.CFPCT);
        planPackage.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
        planPackage.setTemplateId(3L);
        planPackage.setId(3L);
        planPackage.setName(Constants.TOP_QUALITY_NAME);
        planPackage.setCompanyId(0L);
        planPackage.setFundingBasePlanList(Arrays.asList("MED1", "MED2"));
        planPackage.getCoverageLevelFunding().put("employee", BigDecimal.valueOf(50));
        planPackage.getCoverageLevelFunding().put("employeePlusSpouse", BigDecimal.ZERO);
        planPackage.getCoverageLevelFunding().put("employeePlusChild", BigDecimal.ZERO);
        planPackage.getCoverageLevelFunding().put("employeePlusFamily", BigDecimal.ZERO);

        planPackages.add(planPackage);
        planPackagesMap.put(BSSApplicationConstants.MEDICAL, planPackages);

        // DENTAL
        planPackages = new ArrayList<PlanPackage>();
        planPackage = new PlanPackage();

        // PREMIER
        planPackage.setId(0);
        planPackage.setStrategyId(0L);
        planPackage.setFundingType(BSSApplicationConstants.CFPCT);
        planPackage.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
        planPackage.setTemplateId(3L);
        planPackage.setId(3L);
        planPackage.setName(Constants.TOP_QUALITY_NAME);
        planPackage.setCompanyId(0L);
        planPackage.setFundingBasePlanList(null);
        planPackage.getCoverageLevelFunding().put("employee", BigDecimal.valueOf(50));
        planPackage.getCoverageLevelFunding().put("employeePlusSpouse", BigDecimal.ZERO);
        planPackage.getCoverageLevelFunding().put("employeePlusChild", BigDecimal.ZERO);
        planPackage.getCoverageLevelFunding().put("employeePlusFamily", BigDecimal.ZERO);

        planPackages.add(planPackage);
        planPackagesMap.put(BSSApplicationConstants.DENTAL, planPackages);

        // VISION
        planPackages = new ArrayList<PlanPackage>();
        planPackage = new PlanPackage();

        // PREMIER
        planPackage.setId(0);
        planPackage.setStrategyId(0L);
        planPackage.setFundingType(BSSApplicationConstants.CFPCT);
        planPackage.setPlanType(BSSApplicationConstants.VISION_PLAN_TYPE);
        planPackage.setTemplateId(3L);
        planPackage.setId(3L);
        planPackage.setName(Constants.TOP_QUALITY_NAME);
        planPackage.setCompanyId(0L);
        planPackage.setFundingBasePlanList(null);
        planPackage.getCoverageLevelFunding().put("employee", BigDecimal.valueOf(50));
        planPackage.getCoverageLevelFunding().put("employeePlusSpouse", BigDecimal.ZERO);
        planPackage.getCoverageLevelFunding().put("employeePlusChild", BigDecimal.ZERO);
        planPackage.getCoverageLevelFunding().put("employeePlusFamily", BigDecimal.ZERO);

        planPackages.add(planPackage);
        planPackagesMap.put(BSSApplicationConstants.VISION, planPackages);

        return planPackagesMap;
    }

    private Map<String, Set<BenefitPlan>> preparePlanPackagePlans() {

        Map<String, Set<BenefitPlan>> planPackagePlans = new HashMap<String, Set<BenefitPlan>>();
        Set<BenefitPlan> benefitPlans = new HashSet<BenefitPlan>();

        benefitPlans = prepareMedicalPlans();
        benefitPlans.addAll(prepareDentalPlans());
        benefitPlans.addAll(prepareVisionPlans());

        planPackagePlans.put(Constants.TOP_QUALITY_NAME, benefitPlans);
        planPackagePlans.put(Constants.CONSERVATIVE_PACKAGE_NAME, benefitPlans);
        planPackagePlans.put(Constants.BALANCED_PACKAGE_NAME, benefitPlans);

        return planPackagePlans;
    }

    private Set<BenefitPlan> prepareMedicalPlans() {
        Set<BenefitPlan> benefitPlans = new HashSet<BenefitPlan>();

        // MEDICAL
        BenefitPlan benefitPlan = new BenefitPlan();
        //benefitPlan.setAmbisId("");
        benefitPlan.setId("1");
        benefitPlan.setPlanCarrierId(1L);
        benefitPlan.setName("MED1");
        benefitPlan.setEstimatedTotalCost(BigDecimal.ZERO);
        benefitPlan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
        benefitPlan.setStrategyId(0L);
        benefitPlan.setHighDeductible(false);
        benefitPlan.setPremium(false);
        benefitPlan.setEmployeePaid(false);
        benefitPlan.setAnnualCap(BigDecimal.ZERO);
        benefitPlans.add(benefitPlan);

        benefitPlan = new BenefitPlan();
        //benefitPlan.setAmbisId("");
        benefitPlan.setId("2");
        benefitPlan.setPlanCarrierId(1L);
        benefitPlan.setName("MED2");
        benefitPlan.setEstimatedTotalCost(BigDecimal.ZERO);
        benefitPlan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
        benefitPlan.setStrategyId(0L);
        benefitPlan.setHighDeductible(false);
        benefitPlan.setPremium(false);
        benefitPlan.setEmployeePaid(false);
        benefitPlan.setAnnualCap(BigDecimal.ZERO);
        benefitPlans.add(benefitPlan);

        return benefitPlans;

    }

    private Set<BenefitPlan> prepareDentalPlans() {
        Set<BenefitPlan> benefitPlans = new HashSet<BenefitPlan>();

        // DENTAL
        BenefitPlan benefitPlan = new BenefitPlan();
        //benefitPlan.setAmbisId("");
        benefitPlan.setId("3");
        benefitPlan.setPlanCarrierId(1L);
        benefitPlan.setName("DEN1");
        benefitPlan.setEstimatedTotalCost(BigDecimal.ZERO);
        benefitPlan.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
        benefitPlan.setStrategyId(0L);
        benefitPlan.setHighDeductible(false);
        benefitPlan.setPremium(false);
        benefitPlan.setEmployeePaid(false);
        benefitPlan.setAnnualCap(BigDecimal.ZERO);
        benefitPlans.add(benefitPlan);

        return benefitPlans;

    }

    private Set<BenefitPlan> prepareVisionPlans() {
        Set<BenefitPlan> benefitPlans = new HashSet<BenefitPlan>();

        // VISION
        BenefitPlan benefitPlan = new BenefitPlan();
        //benefitPlan.setAmbisId("");
        benefitPlan.setId("4");
        benefitPlan.setPlanCarrierId(1L);
        benefitPlan.setName("VIS1");
        benefitPlan.setEstimatedTotalCost(BigDecimal.ZERO);
        benefitPlan.setPlanType(BSSApplicationConstants.VISION_PLAN_TYPE);
        benefitPlan.setStrategyId(0L);
        benefitPlan.setHighDeductible(false);
        benefitPlan.setPremium(false);
        benefitPlan.setEmployeePaid(false);
        benefitPlan.setAnnualCap(BigDecimal.ZERO);
        benefitPlans.add(benefitPlan);

        return benefitPlans;

    }

}