package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.util.StrategyUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.GroupRatePK;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.StrategyFundingModel;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.MinimumFunding;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.RegionalMinimumFunding;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class RenewalServiceHelperTest {
	
	@Mock
	RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;

	@Mock
	RealmDataDao realmDataDao;

    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        if (mockStaticAppRulesAndConfigsUtils == null) {
            mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
        }
        commonServiceHelperMockedStatic = Mockito.mockStatic(CommonServiceHelper.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
        }
        commonServiceHelperMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
    }
	
	@Test
	public void getHeadCount() {
		List<PlanCoverageLevelHeadCount> list = new ArrayList<>();
		PlanCoverageLevelHeadCount planHeadCount = new PlanCoverageLevelHeadCount();
		planHeadCount.setBenefitPlan("AA1111");
		planHeadCount.setHeadCount(2);
		list.add(planHeadCount);
		planHeadCount = new PlanCoverageLevelHeadCount();
		planHeadCount.setBenefitPlan("BB1111");
		planHeadCount.setHeadCount(4);
		list.add(planHeadCount);

		long actualResult = RenewalServiceHelper.getHeadCount(list, "BB1111");

		assertEquals(4, actualResult);

		actualResult = RenewalServiceHelper.getHeadCount(Collections.<PlanCoverageLevelHeadCount>emptyList(), "BB1111");

		assertEquals(0, actualResult);
	}
	
	@Test
	public void getCovrgHeadCount() {
		List<PlanCoverageLevelHeadCount> headCountList = prepareCoverageLevelHeadCount();

		int actualResult = RenewalServiceHelper.getCovrgHeadCount(headCountList, CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		assertEquals(4, actualResult);

		actualResult = RenewalServiceHelper.getCovrgHeadCount(Collections.<PlanCoverageLevelHeadCount> emptyList(),
				CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		assertEquals(0, actualResult);
	}

	@Test
	public void getCovrgHsaHeadCount() {
		List<PlanCoverageLevelHeadCount> headCountList = prepareCoverageLevelHeadCount();

		int actualResult = RenewalServiceHelper.getCovrgHsaHeadCount(headCountList, CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		assertEquals(2, actualResult);

		actualResult = RenewalServiceHelper.getCovrgHsaHeadCount(Collections.<PlanCoverageLevelHeadCount> emptyList(),
				CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		assertEquals(0, actualResult);
	}

	@Test
	public void constructBenefitGroupForRenewalCompany() {
		BenefitGroup bg = new BenefitGroup();
		bg.setBenefitProgram("AA1111");
		Company company = new Company();
		company.setId(11111111);
		Map<String, Integer> groupHeadCountMap = new HashMap<>();
		groupHeadCountMap.put("AA1111", 2);
		Map<String, String> rateTableIds = new HashMap<>();
		String eligRuleId = "eligRule";
		Map<String, String> waitPeriodMap = new HashMap<>();
		waitPeriodMap.put("AA1111", "firstDayOfMonth");

		RenewalServiceHelper.constructBenefitGroupForRenewalCompany(bg, company, groupHeadCountMap, rateTableIds,
				eligRuleId, waitPeriodMap);

		assertEquals(eligRuleId, bg.getEligRuleId());
		assertEquals("firstDayOfMonth", bg.getWaitingPeriod());
		assertEquals(11111111, bg.getCompanyId());
		assertEquals(2, bg.getHeadcount());
		assertEquals(false, bg.isDefaultGroup());
		assertEquals(BSSApplicationConstants.STD_GROUP_TYPE, bg.getType());
		assertEquals(Collections.emptySet(), bg.getGroupRate());

		// When headcount map is null
		RenewalServiceHelper.constructBenefitGroupForRenewalCompany(bg, company, null, rateTableIds, eligRuleId,
				waitPeriodMap);

		assertEquals(0, bg.getHeadcount());

		// When headcount map does not contain ben program
		groupHeadCountMap = new HashMap<>();
		groupHeadCountMap.put("BB1111", 2);

		RenewalServiceHelper.constructBenefitGroupForRenewalCompany(bg, company, null, rateTableIds, eligRuleId,
				waitPeriodMap);

		assertEquals(0, bg.getHeadcount());

		// When group's ben program is same as company's ben program.
		company.setBenefitProgram("AA1111");

		RenewalServiceHelper.constructBenefitGroupForRenewalCompany(bg, company, null, rateTableIds, eligRuleId,
				waitPeriodMap);

		assertEquals(true, bg.isDefaultGroup());
	}
	
	@Test
	public void updateContributionsForMinimumFundingDefault() {

		Map<String, BigDecimal> minimumFundingMap = prepareMinimumFundingMap();
		List<Contribution> contributions = prepareContributions();
		Company company = prepareCompany();
		Map<String, List<String>> planRegions = preparePlanRegions();
		Map<String, List<Contribution>> benefitPlanContributions = prepareBenefitPlanContributions();
		String fundingType = BSSApplicationConstants.CFPCT;
		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put("FUNDING_TYPE", BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE);
		Map<String, Map<String, Object>> groupFundingDetails = new HashMap<>();

		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigService);
		
		RenewalServiceHelper.updateContributionsForMinimumFunding(minimumFundingMap, contributions, company,
				planRegions, benefitPlanContributions, fundingType, groupFundingDetails);
	}
	
	@Test
	public void updateContributionsForMinimumFundingHq() {

		Map<String, BigDecimal> minimumFundingMap = prepareMinimumFundingMap();
		List<Contribution> contributions = prepareContributions();
		Company company = prepareCompany();
		Map<String, List<String>> planRegions = preparePlanRegions();
		Map<String, List<Contribution>> benefitPlanContributions = prepareBenefitPlanContributions();
		String fundingType = BSSApplicationConstants.CFPCT;
		Map<String, String> rulesMap = new HashMap<>();
		rulesMap.put("FUNDING_TYPE", BSSApplicationConstants.HQ_MIN_FUNDING_TYPE);
		Map<String, Map<String, Object>> groupFundingDetails = new HashMap<>();

		RulesAndConfigsUtils.setRealmPlanYearRuleConfigService(realmPlanYearRuleConfigService);
		
		RenewalServiceHelper.updateContributionsForMinimumFunding(minimumFundingMap, contributions, company,
				planRegions, benefitPlanContributions, fundingType, groupFundingDetails);
	}
	
	@Test
	public void getCompanyPreviousPlanYearHealthPlans() {
		Map<String, Map<String, BenefitPlan>> offerPlanMap = prepareHealthPlansMap();
		Map<String, PlanMapping> primaryPlanMap = preparePrimaryPlanMap();
		Set<String> actualResults = RenewalServiceHelper.getCompanyPreviousPlanYearHealthPlans(offerPlanMap, null);
		assertEquals(5, actualResults.size());
		assertTrue(actualResults.contains("VISIONPLAN1"));
		
		actualResults = RenewalServiceHelper.getCompanyPreviousPlanYearHealthPlans(offerPlanMap, primaryPlanMap);
		assertEquals(6, actualResults.size());
		assertTrue(actualResults.contains("VISIONPLAN1"));
		assertTrue(actualResults.contains("MAPPEDMEDICAL"));
	}

	@Test
	public void updateContributionsForBandCodeChangesBSSFunding() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, (float) 560.7);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
		PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
		pclhc.setBenefitPlan("001302");
		pclhc.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE.getId());
		pclhc.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		headCountList.add(pclhc);

		Map<String, Object> cvgFunding = new HashMap<>();
		cvgFunding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BSUPP);
		cvgFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, "12345");
		cvgFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, new BigDecimal(100));
		cvgFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, new BigDecimal(1));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(200));

        List<Contribution> contributions =RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates, null,
				cvgFunding, null, true);

        assertEquals(4, contributions.size());
        assertTrue(contributions.stream().allMatch(c -> BigDecimal.valueOf(560.70).compareTo(c.getEmployerContribution()) == 0));
	}
	
	@Test
	public void updateContributionsForBandCodeChangesBSSFunding1() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, (float) 560.7);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
		PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
		pclhc.setBenefitPlan("001302");
		pclhc.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE.getId());
		pclhc.setPlanType(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		headCountList.add(pclhc);

		Map<String, Object> cvgFunding = new HashMap<>();
		cvgFunding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BSUPP);
		cvgFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, "12345");
		cvgFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, new BigDecimal(100));
		cvgFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, new BigDecimal(1));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(200));

		RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates, null,
				cvgFunding, null, true);
	}
	
	@Test
	public void updateContributionsForBandCodeChangesBSSFunding2() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 400);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
		PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
		pclhc.setBenefitPlan("001302");
		pclhc.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE.getId());
		pclhc.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		headCountList.add(pclhc);

		Map<String, Object> cvgFunding = new HashMap<>();
		cvgFunding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BFPCT);
		cvgFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, "12345");
		cvgFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, new BigDecimal(100));
		cvgFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, new BigDecimal(1));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(200));

		RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates, null,
				cvgFunding, null, true);
	}

	@Test
	public void constructContributionsByBSSFunding() {
		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, (float) 560.7);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
		PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
		pclhc.setBenefitPlan("001302");
		pclhc.setCovrgCode("1");
		pclhc.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		headCountList.add(pclhc);

		Map<String, Object> coverageLevelFunding = populateCvgFunding(BigDecimal.valueOf(560.7), BSSApplicationConstants.BFPCT, BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		Map<String, String> covergeOverride = new HashMap<>();

		covergeOverride.put("1", BSSApplicationConstants.PLAN_OVERRIDE_BASE);

		planOverrides.put("001302", covergeOverride);

		RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates,
				headCountList, coverageLevelFunding, planOverrides, false);

	}

	@Test
	public void constructContributionsByBSSFunding2() {
		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, (float) 560.7);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Object> coverageLevelFunding = populateCvgFunding(BigDecimal.valueOf(560.7), BSSApplicationConstants.BFPCT, BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		Map<String, String> covergeOverride = new HashMap<>();

		covergeOverride.put("1", BSSApplicationConstants.PLAN_OVERRIDE_BASE);

		planOverrides.put("001302", covergeOverride);

		RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates,
				headCountList, coverageLevelFunding, planOverrides, false);

	}

	@Test
	public void constructContributionsByBSSFunding3() {
		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 400);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Object> coverageLevelFunding = populateCvgFunding(BigDecimal.valueOf(560.7), BSSApplicationConstants.BFPCT, BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		Map<String, String> covergeOverride = new HashMap<>();

		covergeOverride.put("1", BSSApplicationConstants.PLAN_OVERRIDE_BASE);

		planOverrides.put("001302", covergeOverride);

		RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates,
				headCountList, coverageLevelFunding, planOverrides, false);

	}

	@Test
	public void constructContributionsByBSSFunding4() {
		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 400);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Object> coverageLevelFunding = populateCvgFunding(BigDecimal.valueOf(560.7), BSSApplicationConstants.CFPCT, BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		Map<String, String> covergeOverride = new HashMap<>();

		covergeOverride.put("1", BSSApplicationConstants.PLAN_OVERRIDE_BASE);

		planOverrides.put("001302", covergeOverride);

		RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates,
				headCountList, coverageLevelFunding, planOverrides, false);
	}

    @Test
    public void createUpdateContributionsByBaseFundingPlanRateV2Enabled() {
        try (MockedStatic<StrategyUtils> strategyUtilsMock = mockStatic(StrategyUtils.class)) {
            Company company = new Company();
            company.setId(11111111);

            BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

            PlanSelection planSelection = new PlanSelection();
            Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, (float) 560.7);

            List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
            PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
            pclhc.setBenefitPlan("001302");
            pclhc.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE.getId());
            pclhc.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
            headCountList.add(pclhc);

            Map<String, Object> cvgFunding = new HashMap<>();
            cvgFunding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BSUPP);
            cvgFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, "12345");
            cvgFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, new BigDecimal(100));
            cvgFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, new BigDecimal(1));
            cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(200));
            cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(200));
            cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(200));
            cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(200));

            List<Contribution> contributions = RenewalServiceHelper.createUpdateContributionsByBaseFunding(benefitPlan, planSelection, rates, null,
                    cvgFunding, null, true);

            assertEquals(4, contributions.size());
            assertTrue(contributions.stream().allMatch(c -> BigDecimal.valueOf(560.70).compareTo(c.getEmployerContribution()) == 0));
        }
    }

	@Test
	public void constructContributionsByPercentIncrease() {
		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 590);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
		PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
		pclhc.setBenefitPlan("001302");
		pclhc.setCovrgCode("1");
		pclhc.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		headCountList.add(pclhc);

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		List<Contribution> contribList = new ArrayList<>();
		RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection, null, rates,
				headCountList, contribList, planOverrides);

        assertEquals(4, contribList.size());
        assertTrue(contribList.stream().allMatch(c -> BigDecimal.valueOf(590).compareTo(c.getEmployerContribution()) == 0));

	}

	@Test
	public void constructContributionsByPercentIncrease50() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 590);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		List<Contribution> contribList = new ArrayList<>();
		RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection,
				new BigDecimal(50), rates, headCountList, contribList, planOverrides);

	}

	@Test
	public void constructContributionsByPercentIncrease100() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 590);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		List<Contribution> contribList = new ArrayList<>();
		RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection,
				new BigDecimal(100), rates, headCountList, contribList, planOverrides);

	}

	@Test
	public void constructContributionsByPercentIncrease0() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 590);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		List<Contribution> contribList = new ArrayList<>();
		RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection,
				new BigDecimal(0), rates, headCountList, contribList, planOverrides);

	}

	@Test
	public void constructContributionsByPercentIncrease4() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 400);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		List<Contribution> contribList = new ArrayList<>();
		RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection, null, rates,
				headCountList, contribList, planOverrides);

	}

	@Test
	public void constructContributionsByPercentIncrease505() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 400);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		List<Contribution> contribList = new ArrayList<>();
		RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection,
				new BigDecimal(50), rates, headCountList, contribList, planOverrides);

	}

	@Test
	public void constructContributionsByPercentIncrease1006() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 400);

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		List<Contribution> contribList = new ArrayList<>();
		RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection,
				new BigDecimal(100), rates, headCountList, contribList, planOverrides);

	}

    @Test
    public void constructContributionsByPercentIncreasePlanRateV2Enabled() {
        try (MockedStatic<StrategyUtils> strategyUtilsMock = mockStatic(StrategyUtils.class)) {
            Company company = new Company();
            company.setId(11111111);

            BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

            PlanSelection planSelection = new PlanSelection();

            Map<String, List<BenefitPlanRate>> rates = populateRates("001302", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 590);

            List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
            PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
            pclhc.setBenefitPlan("001302");
            pclhc.setCovrgCode("1");
            pclhc.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
            headCountList.add(pclhc);

            Map<String, Map<String, String>> planOverrides = new HashMap<>();

            List<Contribution> contribList = new ArrayList<>();
            RenewalServiceHelper.constructContributionsByPercentIncrease(benefitPlan, planSelection, null, rates,
                    headCountList, contribList, planOverrides);

            assertEquals(4, contribList.size());
            assertTrue(contribList.stream().allMatch(c -> BigDecimal.valueOf(590).compareTo(c.getEmployerContribution()) == 0));
        }
    }

	@Test
	public void getMinimumFundingTestContributionDefaultFunding() {
		List<Contribution> contributions = prepareContributions();
		List<String> mandatoryPlansToExclude = Arrays.asList("PLAN_TWO");
		Company company = new Company();
		Map<String, Set<Long>> selectedPlanCarriers = new HashMap<>();
		List<CarrierMinimumFunding> minFundings = new ArrayList<>();
		MinimumFunding minimumFunding = new MinimumFunding(PlanTypesEnum.MEDICAL.getCode(), "VALUE_TYPE",
				BigDecimal.valueOf(75), false);
		when(CommonServiceHelper.extractMinFundingDetails(Mockito.anyString(), Mockito.any(Company.class)))
				.thenReturn(minimumFunding);
		when(RulesAndConfigsUtils.getMinFundingType(Mockito.anyLong()))
				.thenReturn(BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE);

		Map<String, BigDecimal> actualResult = RenewalServiceHelper.getMinimumFunding(contributions,
				mandatoryPlansToExclude, company, selectedPlanCarriers, minFundings);

		assertEquals(0, actualResult.get(PlanTypesEnum.MEDICAL.getCode()).compareTo(BigDecimal.valueOf(225.00)));
		assertEquals(0, actualResult.get(PlanTypesEnum.DENTAL.getCode()).compareTo(BigDecimal.valueOf(22.50)));
		assertEquals(0, actualResult.get(PlanTypesEnum.VISION.getCode()).compareTo(BigDecimal.valueOf(11.25)));
	}

	@Test
	public void getMinimumFundingTestContributionHqFunding() {
		List<Contribution> contributions = prepareContributions();
		List<String> mandatoryPlansToExclude = Arrays.asList("PLAN_TWO");
		Company company = new Company();
		Map<String, Set<Long>> selectedPlanCarriers = prepareSelectedPlanCarriers();
		List<CarrierMinimumFunding> minFundings = prepareCarrierMinimumFundings();
		MinimumFunding minimumFunding = new MinimumFunding(PlanTypesEnum.MEDICAL.getCode(), "VALUE_TYPE",
				BigDecimal.valueOf(75), false);
		when(CommonServiceHelper.extractMinFundingDetails(Mockito.anyString(), Mockito.any(Company.class)))
				.thenReturn(minimumFunding);
		when(RulesAndConfigsUtils.getMinFundingType(Mockito.anyLong()))
				.thenReturn(BSSApplicationConstants.HQ_MIN_FUNDING_TYPE);

		Map<String, BigDecimal> actualResult = RenewalServiceHelper.getMinimumFunding(contributions,
				mandatoryPlansToExclude, company, selectedPlanCarriers, minFundings);

		assertEquals(0, actualResult.get(PlanTypesEnum.MEDICAL.getCode()).compareTo(BigDecimal.valueOf(56.25)));
		assertEquals(0, actualResult.get(PlanTypesEnum.DENTAL.getCode()).compareTo(BigDecimal.valueOf(56.25)));
		assertEquals(0, actualResult.get(PlanTypesEnum.VISION.getCode()).compareTo(BigDecimal.valueOf(56.25)));
	}

	@Test
	public void getMinimumFundingTestContributionOtherFunding() {
		Company company = new Company();
		when(RulesAndConfigsUtils.getMinFundingType(Mockito.anyLong())).thenReturn("OTHER");

		Map<String, BigDecimal> actualResult = RenewalServiceHelper.getMinimumFunding(null, null, company, null, null);

		assertEquals(true, actualResult.isEmpty());
	}

	@Test
	public void getMinimumFundingTestPlanSelectionDefaultFunding() {

		List<PlanSelection> planSelections = preparePlanSelections();
		Map<String, List<BenefitPlanRate>> rates = populateRates("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE,
				(float) 400);
		rates.putAll(populateRates("DENTALPLAN1", BSSApplicationConstants.DENTAL_PLAN_TYPE, (float) 40));
		rates.putAll(populateRates("VISIONPLAN1", BSSApplicationConstants.VISION_PLAN_TYPE, (float) 20));

		List<String> mandatoryPlansToExclude = Arrays.asList("PLAN_TWO");
		Company company = new Company();
		Map<String, Set<Long>> selectedPlanCarriers = new HashMap<>();
		List<CarrierMinimumFunding> minFundings = new ArrayList<>();
		MinimumFunding minimumFunding = new MinimumFunding(PlanTypesEnum.MEDICAL.getCode(), "VALUE_TYPE",
				BigDecimal.valueOf(75), false);
		
		
		when(CommonServiceHelper.extractMinFundingDetails(Mockito.anyString(), Mockito.any(Company.class)))
				.thenReturn(minimumFunding);
		when(RulesAndConfigsUtils.getMinFundingType(Mockito.anyLong()))
				.thenReturn(BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE);

		Map<String, BigDecimal> actualResult = RenewalServiceHelper.getMinimumFunding(planSelections, rates,
				mandatoryPlansToExclude, company, selectedPlanCarriers, minFundings);
		
		assertEquals(0, actualResult.get(PlanTypesEnum.MEDICAL.getCode()).compareTo(BigDecimal.valueOf(300.00)));
		assertEquals(0, actualResult.get(PlanTypesEnum.DENTAL.getCode()).compareTo(BigDecimal.valueOf(30.00)));
		assertEquals(0, actualResult.get(PlanTypesEnum.VISION.getCode()).compareTo(BigDecimal.valueOf(15.00)));
	}

	@Test
	public void getMinimumFundingTestPlanSelectionHqFunding() {

		List<PlanSelection> planSelections = preparePlanSelections();
		Map<String, List<BenefitPlanRate>> rates = preparePlanRates();
		List<String> mandatoryPlansToExclude = Arrays.asList("PLAN_TWO");
		Company company = new Company();
		Map<String, Set<Long>> selectedPlanCarriers = prepareSelectedPlanCarriers();
		List<CarrierMinimumFunding> minFundings = prepareCarrierMinimumFundings();
		MinimumFunding minimumFunding = new MinimumFunding(PlanTypesEnum.MEDICAL.getCode(), MinFundExceptionService.FLAT,
				BigDecimal.valueOf(400), false);
		when(CommonServiceHelper.extractMinFundingDetails(Mockito.anyString(), Mockito.any(Company.class)))
				.thenReturn(minimumFunding);
		when(RulesAndConfigsUtils.getMinFundingType(Mockito.anyLong()))
				.thenReturn(BSSApplicationConstants.HQ_MIN_FUNDING_TYPE);

		Map<String, BigDecimal> actualResult = RenewalServiceHelper.getMinimumFunding(planSelections, rates,
				mandatoryPlansToExclude, company, selectedPlanCarriers, minFundings);
		
		assertEquals(0, actualResult.get(PlanTypesEnum.MEDICAL.getCode()).compareTo(BigDecimal.valueOf(400.00)));
		assertEquals(0, actualResult.get(PlanTypesEnum.DENTAL.getCode()).compareTo(BigDecimal.valueOf(400.00)));
		assertEquals(0, actualResult.get(PlanTypesEnum.VISION.getCode()).compareTo(BigDecimal.valueOf(400.00)));
	}

	@Test
	public void getMinimumFundingTestPlanSelectionOtherFunding() {
		Company company = new Company();
		when(RulesAndConfigsUtils.getMinFundingType(Mockito.anyLong())).thenReturn("OTHER");

		Map<String, BigDecimal> actualResult = RenewalServiceHelper.getMinimumFunding(null, null, null, company, null,
				null);

		assertEquals(true, actualResult.isEmpty());
	}

    @Test
    public void getMinimumFundingTestPlanSelectionDefaultFundingPlanRateV2Enabled() {
        try (MockedStatic<StrategyUtils> strategyUtilsMock = mockStatic(StrategyUtils.class)) {
            List<PlanSelection> planSelections = preparePlanSelections();
            Map<String, List<BenefitPlanRate>> rates = populateRates("MEDPLAN1", BSSApplicationConstants.MEDICAL_PLAN_TYPE,
                    (float) 400);
            rates.putAll(populateRates("DENTALPLAN1", BSSApplicationConstants.DENTAL_PLAN_TYPE, (float) 40));
            rates.putAll(populateRates("VISIONPLAN1", BSSApplicationConstants.VISION_PLAN_TYPE, (float) 20));

            List<String> mandatoryPlansToExclude = Arrays.asList("PLAN_TWO");
            Company company = new Company();
            Map<String, Set<Long>> selectedPlanCarriers = new HashMap<>();
            List<CarrierMinimumFunding> minFundings = new ArrayList<>();
            MinimumFunding minimumFunding = new MinimumFunding(PlanTypesEnum.MEDICAL.getCode(), "VALUE_TYPE",
                    BigDecimal.valueOf(75), false);

            when(CommonServiceHelper.extractMinFundingDetails(Mockito.anyString(), Mockito.any(Company.class)))
                    .thenReturn(minimumFunding);
            when(RulesAndConfigsUtils.getMinFundingType(Mockito.anyLong()))
                    .thenReturn(BSSApplicationConstants.DEFAULT_MIN_FUNDING_TYPE);

            Map<String, BigDecimal> actualResult = RenewalServiceHelper.getMinimumFunding(planSelections, rates,
                    mandatoryPlansToExclude, company, selectedPlanCarriers, minFundings);

            assertEquals(0, actualResult.get(PlanTypesEnum.MEDICAL.getCode()).compareTo(BigDecimal.valueOf(300.00)));
            assertEquals(0, actualResult.get(PlanTypesEnum.DENTAL.getCode()).compareTo(BigDecimal.valueOf(30.00)));
            assertEquals(0, actualResult.get(PlanTypesEnum.VISION.getCode()).compareTo(BigDecimal.valueOf(15.00)));
        }
    }

	@Test
	public void constructHistoryContributions() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();
		Map<String, Map<String, Object>> offerTypeFunding = new HashMap<>();

		offerTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, new HashMap<String, Object>());
		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		PlanCoverageLevelHeadCount pclhc = new PlanCoverageLevelHeadCount();
		pclhc.setBenefitPlan("001302");
		pclhc.setCovrgCode("1");
		pclhc.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		headCountList.add(pclhc);
		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		RenewalServiceHelper.constructHistoryContributions(benefitPlan, planSelection, headCountList,
				planOverrides, BSSApplicationConstants.STD_GROUP_TYPE, offerTypeFunding);

	}

	@Test
	public void constructHistoryContributions1() {

		Company company = new Company();
		company.setId(11111111);

		BenefitPlan benefitPlan = populateBenefitPlan(BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		PlanSelection planSelection = new PlanSelection();

		Map<String, Map<String, Object>> offerTypeFunding = new HashMap<>();
		offerTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, new HashMap<String, Object>());
		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();

		Map<String, Map<String, String>> planOverrides = new HashMap<>();

		RenewalServiceHelper.constructHistoryContributions(benefitPlan, planSelection, headCountList,
				planOverrides, BSSApplicationConstants.K1_GROUP_TYPE, offerTypeFunding);

	}

	@Test
	public void addBlankContributions() {
		BenefitPlan bp = new BenefitPlan();
		List<CoverageLevel> coverageCodes = new ArrayList<>();
		CoverageLevel covgLevel = new CoverageLevel();
		covgLevel.setId("all");
		coverageCodes.add(covgLevel);
		covgLevel = new CoverageLevel();
		covgLevel.setId(CoverageCodesEnums.COV_EMPLOYEE.getId());
		coverageCodes.add(covgLevel);
		covgLevel = new CoverageLevel();
		covgLevel.setId(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
		coverageCodes.add(covgLevel);

		RenewalServiceHelper.addBlankContributions(bp, coverageCodes);

		assertEquals(2, bp.getContributions().size());
	}

	@Test
	public void addPlanSelectionsForAutoSelectPlans() {
		List<PlanSelection> planSelections = new ArrayList<>();
		Map<String, Map<String, Set<String>>> autoSelectPlans = prepareAutoSelectPlans();
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = prepareMapOfCovgCodes();
		Map<String, BenefitPlan> benefitPlanMap = prepareBenPlanMap();
		Long strategyId = 1111L;
		Long benefitGroupId = 2222L;

		RenewalServiceHelper.addPlanSelectionsForAutoSelectPlans(planSelections, autoSelectPlans, mapOfCoverageLevels,
				benefitPlanMap, strategyId, benefitGroupId);

		assertEquals(2, planSelections.size());
		for (PlanSelection planSelection : planSelections) {
			assertTrue(Arrays.asList("DEN2222", "MED2222").contains(planSelection.getBenefitPlan()));
			if ("DEN2222".equals(planSelection.getBenefitPlan())) {
				assertEquals(0, planSelection.getHeadCount());
				assertEquals(1111L, planSelection.getStrategyId());
				assertEquals(2222L, planSelection.getGroupId());
				assertEquals("DEN2222", planSelection.getBenefitPlan());
				assertEquals(BSSApplicationConstants.DENTAL_PLAN_TYPE, planSelection.getPlanType());
			}
		}
	}

	// TODO
	@Test
	public void constructRenewalStrategyFundingDetails() {
		long strategyId = 1234L;
		long benefitGroupId = 1234L;
		BenefitGroup bg = new BenefitGroup();
		bg.setId(benefitGroupId);
		bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		List<StrategyFundingModel> strategyFunding = new ArrayList<>();
		Map<String, Map<String, Object>> psFundingDetails = new HashMap<>();
		Map<String, PlanMapping> realmPlanMapping = new HashMap<>();
		Map<String, Object> cvgFunding = new HashMap<>();
		cvgFunding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BSUPP);
		cvgFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, "12345");
		cvgFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, new BigDecimal(100));
		cvgFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, new BigDecimal(1));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(200));
		cvgFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(200));
		psFundingDetails.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, cvgFunding);
		boolean isHistory = false;
		List<String> bsuppPlanTypes = new ArrayList<>();
		bsuppPlanTypes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		List<String> medPlans=  new  ArrayList<>();
		RenewalServiceHelper.constructRenewalStrategyFundingDetails(strategyId, bg, strategyFunding,
				psFundingDetails, realmPlanMapping, isHistory, bsuppPlanTypes, medPlans, null, null, new HashMap<>());

	}

	@Test
	public void constructRenewalStrategyFundingDetails1() {
		long strategyId = 1234L;
		long benefitGroupId = 1234L;
		BenefitGroup bg = new BenefitGroup();
		bg.setId(benefitGroupId);
		bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		List<StrategyFundingModel> strategyFunding = new ArrayList<>();
		Map<String, Map<String, Object>> psFundingDetails = new HashMap<>();
		Map<String, PlanMapping> realmPlanMapping = new HashMap<>();
		Map<String, Object> cvgFunding = new HashMap<>();
		cvgFunding.put(BSSApplicationConstants.FUNDING_TYPE, "FLTMAX");
		cvgFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, "12345");
		cvgFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, new BigDecimal(100));
		cvgFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, new BigDecimal(1));

		cvgFunding.put("employeeLIMIT", new BigDecimal(200));
		cvgFunding.put("employeePlusSpouseLIMIT", new BigDecimal(200));
		cvgFunding.put("employeePlusChildLIMIT", new BigDecimal(200));
		cvgFunding.put("employeePlusFamilyLIMIT", new BigDecimal(200));

		psFundingDetails.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, cvgFunding);

		boolean isHistory = false;
		List<String> bsuppPlanTypes = new ArrayList<>();
		bsuppPlanTypes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		List<String> medPlans=  new  ArrayList<>();
		RenewalServiceHelper.constructRenewalStrategyFundingDetails(strategyId, bg, strategyFunding,
				psFundingDetails, realmPlanMapping, isHistory, bsuppPlanTypes, medPlans, null, null, new HashMap<>());

	}

	@Test
	public void isFplApplicable() {
		Company company = new Company();
		Realm realm = new Realm();

		realm.setBenExchange(BenExchngEnums.TRINET_II.getBenExchng());
		company.setRealm(realm);
		int acaFplOpted = 0;

		boolean actualResult = RenewalServiceHelper.isFplApplicable(company, acaFplOpted);

		assertFalse(actualResult);

		acaFplOpted = 1;

		actualResult = RenewalServiceHelper.isFplApplicable(company, acaFplOpted);

		assertFalse(actualResult);

		company.setEligAle(false);
		acaFplOpted = 0;

		actualResult = RenewalServiceHelper.isFplApplicable(company, acaFplOpted);

		assertFalse(actualResult);

		company.setEligAle(true);
		acaFplOpted = 1;

		actualResult = RenewalServiceHelper.isFplApplicable(company, acaFplOpted);

		assertTrue(actualResult);
	}

	// TODO
	@Test
	public void updateContributionByEmployeeContributions() {

	}
/*
	@Test
	public void getSelectedPlanCarriers() {

		Map<String, Map<String, BenefitPlan>> bgAllHealthPlansMap = prepareBgAllHealthPlansMap();
		Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap = prepareAllBenefitStatePlansMap();
		Map<String, Set<PlanCarrier>> planCarrierMap = preparePlanCarriers();
		Map<String, PlanMapping> realmPlanMapping = new HashMap<>();
		PlanMapping planMapping = new PlanMapping();
		planMapping.setNewBenefitPlan("MEDPLAN8");
		realmPlanMapping.put("MEDPLAN2", planMapping);
		Map<String, Set<Long>> mandatoryPortfoliosMap = new HashMap<>();
		Set<String> medicalPlanCarriers = new HashSet<>();

//		when(BenefitCategoriesHelper.getMandatoryPlanCarriers(planCarrierMap)).thenReturn(mandatoryPortfoliosMap);
//		when(BenefitCategoriesHelper.getMedicalPlanCarriers(planCarrierMap)).thenReturn(medicalPlanCarriers);
		
		
		Map<String, Set<Long>> result = RenewalServiceHelper.getSelectedPlanCarriers(bgAllHealthPlansMap,
				allBenefitStatePlansMap, planCarrierMap, realmPlanMapping);

		assertEquals(1, result.size());
		assertEquals(3, result.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).size());
		assertEquals(result.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE), Set.of(5L, 18L, 19L));
	}
*/	

	@Test
	public void updateContributionByLimitContributions() {
		List<Contribution> contributions = new ArrayList<>();
		Contribution cont = new Contribution();
		cont.setEmployeeContribution(BigDecimal.valueOf(100));
		cont.setEmployerContribution(BigDecimal.valueOf(50));
		cont.setEmployerPercent(new BigDecimal("66.67"));
		contributions.add(cont);
		cont = new Contribution();
		cont.setEmployeeContribution(BigDecimal.valueOf(20));
		cont.setEmployerContribution(BigDecimal.valueOf(40));
		cont.setEmployerPercent(new BigDecimal("100.00"));
		contributions.add(cont);
		BigDecimal limitCvgPlanCost = new BigDecimal(100);

		RenewalServiceHelper.updateContributionByLimitContributions(contributions, limitCvgPlanCost);

		assertEquals(new BigDecimal(50), contributions.get(0).getEmployerContribution());
		assertEquals(new BigDecimal(100), contributions.get(0).getEmployeeContribution());
		assertEquals(new BigDecimal("66.67"), contributions.get(0).getEmployerPercent().setScale(2, RoundingMode.UP));

		assertEquals(new BigDecimal(40), contributions.get(1).getEmployerContribution());
		assertEquals(new BigDecimal(20), contributions.get(1).getEmployeeContribution());
		assertEquals(new BigDecimal("100.00"), contributions.get(1).getEmployerPercent().setScale(2, RoundingMode.UP));
	}

	// TODO
	@Test
	public void createK1Funding() {

	}

	@Test
	public void validateK1RateTableId() {
		String k1RateTableId = "AA1111";
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		BenefitGroup bg = new BenefitGroup();
		Set<GroupRate> grs = new HashSet<>();
		GroupRate gr = new GroupRate();
		GroupRatePK id = new GroupRatePK();
		id.setRateTblId(k1RateTableId);
		gr.setId(id);
		bg.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		grs.add(gr);
		bg.setGroupRate(grs);
		benefitGroups.add(bg);

		boolean actual = RenewalServiceHelper.validateK1RateTableId(benefitGroups, k1RateTableId);

		assertFalse(actual);

		benefitGroups.clear();
		bg = new BenefitGroup();
		grs.clear();
		gr = new GroupRate();
		id = new GroupRatePK();
		id.setRateTblId(k1RateTableId);
		gr.setId(id);
		gr.setRateIdType("15");
		bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		grs.add(gr);
		bg.setGroupRate(grs);
		benefitGroups.add(bg);

		actual = RenewalServiceHelper.validateK1RateTableId(benefitGroups, k1RateTableId);

		assertFalse(actual);

		benefitGroups.clear();
		bg = new BenefitGroup();
		grs.clear();
		gr = new GroupRate();
		id = new GroupRatePK();
		id.setRateTblId("AA2222");
		gr.setId(id);
		gr.setRateIdType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		grs.add(gr);
		bg.setGroupRate(grs);
		benefitGroups.add(bg);

		actual = RenewalServiceHelper.validateK1RateTableId(benefitGroups, k1RateTableId);

		assertFalse(actual);

		benefitGroups.clear();
		bg = new BenefitGroup();
		grs.clear();
		gr = new GroupRate();
		id = new GroupRatePK();
		id.setRateTblId(k1RateTableId);
		gr.setId(id);
		gr.setRateIdType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		bg.setType(BSSApplicationConstants.STD_GROUP_TYPE);
		grs.add(gr);
		bg.setGroupRate(grs);
		benefitGroups.add(bg);

		actual = RenewalServiceHelper.validateK1RateTableId(benefitGroups, k1RateTableId);

		assertTrue(actual);
	}

	@Test
	public void updateK1RateTableId() {
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		BenefitGroup bg = new BenefitGroup();
		bg.setType(BSSApplicationConstants.K1_GROUP_TYPE);
		benefitGroups.add(bg);
		Map<String, String> updatedRateTableIds = null;

		boolean actual = RenewalServiceHelper.updateK1RateTableId(benefitGroups, updatedRateTableIds);

		assertTrue(actual);
		assertTrue(benefitGroups.get(0).getGroupRate().isEmpty());

		benefitGroups.clear();
		bg = new BenefitGroup();
		bg.setType("K1");
		benefitGroups.add(bg);
		updatedRateTableIds = new HashMap<>();
		updatedRateTableIds.put("ID1111", "VAL1111");

		actual = RenewalServiceHelper.updateK1RateTableId(benefitGroups, updatedRateTableIds);

		assertTrue(actual);
		assertEquals(1, benefitGroups.get(0).getGroupRate().size());
		for (GroupRate gr : benefitGroups.get(0).getGroupRate()) {
			assertEquals("VAL1111", gr.getId().getRateTblId());
			assertEquals("ID1111", gr.getRateIdType());
		}
	}

	@Test
	public void updateGroupDVPlansBsupp() {
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = new HashMap<>();
		Map<String, Map<String, Object>> planTypeFunding = new HashMap<>();
		Map<String, Object> funding = new HashMap<>();
		funding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BSUPP);
		planTypeFunding.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, funding);
		groupFundingDetails.put("131232", planTypeFunding);
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = new HashMap<>();
		Map<String, Map<String, BenefitPlan>> planTypePlans = new HashMap<>();
		Map<String, BenefitPlan> benefitPlans = new HashMap<>();
		BenefitPlan bp = populateBenefitPlan(BSSApplicationConstants.VISION_PLAN_TYPE);
		benefitPlans.put("12345", bp);
		planTypePlans.put(BSSApplicationConstants.VISION_PLAN_TYPE, benefitPlans);

		BenefitPlan bp1 = populateBenefitPlan(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		benefitPlans.put("123453", bp1);

		planTypePlans.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, benefitPlans);
		bgsHealthPlansMap.put("131232", planTypePlans);
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();

		List<CoverageLevel> coverageCodes = new ArrayList<>();
		CoverageLevel covgLevel = new CoverageLevel();
		covgLevel.setId("all");
		coverageCodes.add(covgLevel);
		covgLevel = new CoverageLevel();
		covgLevel.setId(CoverageCodesEnums.COV_EMPLOYEE.getId());
		coverageCodes.add(covgLevel);
		covgLevel = new CoverageLevel();
		covgLevel.setId(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
		coverageCodes.add(covgLevel);

		mapOfCoverageLevels.put(BSSApplicationConstants.DENTAL, coverageCodes);
		mapOfCoverageLevels.put(BSSApplicationConstants.MEDICAL, coverageCodes);
		mapOfCoverageLevels.put(BSSApplicationConstants.VISION, coverageCodes);

		Map<String, String> erEeMapping = new HashMap<>();
		RenewalServiceHelper.updateGroupDVPlansBsupp(groupFundingDetails, bgsHealthPlansMap, mapOfCoverageLevels,
				erEeMapping);
	}
	
	@Test
	public void getFplPLans() {
		Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap = prepareAllBenStatePlans();
		Map<String, StateBenefitPlan> mandatoryPlans = prepareMandatoryPlans();
		Set<Long> selectedMedicalCarriers = new HashSet<>();
		selectedMedicalCarriers.addAll(Arrays.asList(1111L, 3333L));

		List<String> actualResult = RenewalServiceHelper.getFplPlans(allBenefitStatePlansMap, mandatoryPlans,
				selectedMedicalCarriers);

		assertEquals(4, mandatoryPlans.size());
		assertTrue(mandatoryPlans.containsKey("FPL_PLAN1"));
		assertTrue(mandatoryPlans.containsKey("FPL_PLAN2"));
		assertTrue(mandatoryPlans.containsKey("MND_PLAN1"));
		assertTrue(mandatoryPlans.containsKey("MND_PLAN2"));

		assertEquals(2, actualResult.size());
		assertTrue(actualResult.containsAll(Arrays.asList("FPL_PLAN1", "FPL_PLAN2")));
	}
	
	@Test
	public void calculateStrategyGroupEstimates() {
		long strategyId = 1L;
		long benefiGroupId = 1L;
		List<Contribution> contributions = prepareContributionsForEstimates();
		Map<String, Map<String, Object>> groupFundingDetails = prepareGroupFundingDetails();
		RenewalServiceHelper.calculateStrategyGroupEstimates(strategyId, benefiGroupId, contributions,
				groupFundingDetails);
	}

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<RenewalServiceHelper> constructor = RenewalServiceHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            org.junit.Assert.fail("Expected IllegalStateException");
        } catch (InvocationTargetException e) {
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

    @Test
    public void updateFundingDetailsForBasePlan() {
        // Prepare test data
        String benefitPlanId = "001302";
        String planType = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        String bandCode = "10";
        // company with Aetna band code
        Company company = prepareCompany();
        BandCodes bandCodes = new BandCodes();
        bandCodes.setAetnaBandCode(bandCode);
        company.setBandCodes(bandCodes);
        // Adding multiple rates to test selection based on band code
        Map<String, List<BenefitPlanRate>> rates = Map.of(
                benefitPlanId,
                new ArrayList<>(Arrays.asList(
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE, benefitPlanId, planType, "N", BigDecimal.valueOf(432)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE, benefitPlanId, planType, "N", BigDecimal.valueOf(432)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD, benefitPlanId, planType, "N", BigDecimal.valueOf(432)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_FAMILY, benefitPlanId, planType, "N", BigDecimal.valueOf(432)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE, benefitPlanId, planType, bandCode, BigDecimal.valueOf(567)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE, benefitPlanId, planType, bandCode, BigDecimal.valueOf(567)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD, benefitPlanId, planType, bandCode, BigDecimal.valueOf(567)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_FAMILY, benefitPlanId, planType, bandCode, BigDecimal.valueOf(567))))
        );

        // Costs for the funding details
        BigDecimal cost = BigDecimal.valueOf(409.37);
        // 409.37 * (567 / 100) = 2321.13
        BigDecimal expectedEmployeeLIMIT = new BigDecimal("2321.13");

        updateFundingDetailsForBasePlan(company, rates, benefitPlanId, planType, cost, expectedEmployeeLIMIT);
    }

    @Test
    public void updateFundingDetailsForBasePlanPlanRateV2Enabled() {
        // Prepare test data
        String benefitPlanId = "001302";
        String planType = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        String bandCode = "10";
        // company with Aetna band code
        Company company = prepareCompany();
        BandCodes bandCodes = new BandCodes();
        bandCodes.setAetnaBandCode(bandCode);
        company.setBandCodes(bandCodes);
        // Adding multiple rates to test selection based on band code
        Map<String, List<BenefitPlanRate>> rates = Map.of(
                benefitPlanId,
                new ArrayList<>(Arrays.asList(
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE, benefitPlanId, planType, bandCode, BigDecimal.valueOf(111)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE, benefitPlanId, planType, bandCode, BigDecimal.valueOf(111)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD, benefitPlanId, planType, bandCode, BigDecimal.valueOf(111)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_FAMILY, benefitPlanId, planType, bandCode, BigDecimal.valueOf(111))))
        );
        // Costs for the funding details
        BigDecimal cost = BigDecimal.valueOf(409.37);
        // 409.37 * (111 / 100) = 454.40
        BigDecimal expectedEmployeeLIMIT = new BigDecimal("454.40");

        try (MockedStatic<StrategyUtils> strategyUtilsMock = mockStatic(StrategyUtils.class)) {
            updateFundingDetailsForBasePlan(company, rates, benefitPlanId, planType, cost, expectedEmployeeLIMIT);
        }
    }

    @Test
    public void updateFundingDetailsForBasePlanDefaultBandCode() {
        // Prepare test data
        String benefitPlanId = "001302";
        String planType = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        String bandCode = "10";
        // company without band codes
        Company company = prepareCompany();
        BandCodes bandCodes = new BandCodes();
        bandCodes.setAetnaBandCode(null);
        company.setBandCodes(bandCodes);
        // Adding multiple rates to test selection based on band code
        Map<String, List<BenefitPlanRate>> rates = Map.of(
                benefitPlanId,
                new ArrayList<>(Arrays.asList(
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE, benefitPlanId, planType, "N", BigDecimal.valueOf(432)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE, benefitPlanId, planType, "N", BigDecimal.valueOf(432)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD, benefitPlanId, planType, "N", BigDecimal.valueOf(432)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_FAMILY, benefitPlanId, planType, "N", BigDecimal.valueOf(432))))
        );
        // Costs for the funding details
        BigDecimal cost = BigDecimal.valueOf(409.37);
        // 409.37 * (432 / 100) = 1768.48
        BigDecimal expectedEmployeeLIMIT = new BigDecimal("1768.48");

        updateFundingDetailsForBasePlan(company, rates, benefitPlanId, planType, cost, expectedEmployeeLIMIT);
    }

    @Test
    public void updateFundingDetailsForBasePlanDefaultBandCodePlanRateV2Enabled() {
        // Prepare test data
        String benefitPlanId = "001302";
        String planType = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
        // company without band codes
        Company company = prepareCompany();
        BandCodes bandCodes = new BandCodes();
        bandCodes.setAetnaBandCode(null);
        company.setBandCodes(bandCodes);
        // Adding multiple rates to test selection based on band code
        Map<String, List<BenefitPlanRate>> rates = Map.of(
                benefitPlanId,
                new ArrayList<>(Arrays.asList(
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE, benefitPlanId, planType, "N", BigDecimal.valueOf(222)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE, benefitPlanId, planType, "N", BigDecimal.valueOf(222)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD, benefitPlanId, planType, "N", BigDecimal.valueOf(222)),
                        createPlanRate(CoverageCodesEnums.COV_EMPLOYEE_FAMILY, benefitPlanId, planType, "N", BigDecimal.valueOf(222))))
        );
        // Costs for the funding details
        BigDecimal cost = BigDecimal.valueOf(409.37);
        // 409.37 * (222 / 100) = 908.80
        BigDecimal expectedEmployeeLIMIT = new BigDecimal("908.80");

        try (MockedStatic<StrategyUtils> strategyUtilsMock = mockStatic(StrategyUtils.class)) {
            updateFundingDetailsForBasePlan(company, rates, benefitPlanId, planType, cost, expectedEmployeeLIMIT);
        }
    }
    
	@Test
	public void addMissingChildCarriers_withListParentIds() {
        // Prepare planCarrierMap with medical carriers where children reference multiple parent IDs
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Set<PlanCarrier> medicalCarriers = new HashSet<>();
        PlanCarrier planCarrier = new PlanCarrier();
        planCarrier.setId(10);
        planCarrier.setMandatory(true);
        medicalCarriers.add(planCarrier);
        PlanCarrier planCarrier2 = new PlanCarrier();
        planCarrier2.setId(20);
        planCarrier2.setMandatory(true);
        medicalCarriers.add(planCarrier2);
        PlanCarrier planCarrier3 = new PlanCarrier();
        planCarrier3.setId(101);
        planCarrier3.setMandatory(false);
        planCarrier3.setParentId(Arrays.asList("10", "20"));
        medicalCarriers.add(planCarrier3);
        PlanCarrier planCarrier4 = new PlanCarrier();
        planCarrier4.setId(102);
        planCarrier4.setMandatory(false);
        planCarrier4.setParentId(Arrays.asList("20"));
        medicalCarriers.add(planCarrier4);
        PlanCarrier planCarrier5 = new PlanCarrier();
        planCarrier5.setId(103);
        planCarrier5.setMandatory(false);
        planCarrier5.setParentId(Arrays.asList(null, "10"));
        medicalCarriers.add(planCarrier5);
        planCarrierMap.put(BSSApplicationConstants.MEDICAL, medicalCarriers);

        // Selected plan carriers initially include only parent 10
        Map<String, Set<Long>> selectedPlanCarriers = new HashMap<>();
        selectedPlanCarriers.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, new HashSet<>(Arrays.asList(10L)));

        RenewalServiceHelper.addMissingChildCarriers(planCarrierMap, selectedPlanCarriers);

        // Expect children of parent 10 to be added: 101 and 103
        Set<Long> updated = selectedPlanCarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
        assertTrue(updated.contains(10L));
        assertTrue(updated.contains(101L));
        assertTrue(updated.contains(103L));
        assertFalse(updated.contains(102L));
    }

    @Test
    public void addMissingChildCarriers_handlesNullParentIdsGracefully() {
        Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
        Set<PlanCarrier> medicalCarriers = new HashSet<>();
        PlanCarrier planCarrier = new PlanCarrier();
        planCarrier.setId(30);
        planCarrier.setMandatory(true);
        medicalCarriers.add(planCarrier);
        PlanCarrier planCarrier2 = new PlanCarrier();
        planCarrier2.setId(201);
        planCarrier2.setMandatory(false);
        planCarrier2.setParentId(null);
        medicalCarriers.add(planCarrier2);
        PlanCarrier planCarrier3 = new PlanCarrier();
        planCarrier3.setId(202);
        planCarrier3.setMandatory(false);
        planCarrier3.setParentId(null);
        medicalCarriers.add(planCarrier3);
        planCarrierMap.put(BSSApplicationConstants.MEDICAL, medicalCarriers);

        Map<String, Set<Long>> selectedPlanCarriers = new HashMap<>();
        selectedPlanCarriers.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, new HashSet<>(Arrays.asList(30L)));

        RenewalServiceHelper.addMissingChildCarriers(planCarrierMap, selectedPlanCarriers);

        // No children should be added due to null parent references
        Set<Long> updated = selectedPlanCarriers.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
        assertEquals(new HashSet<>(Arrays.asList(30L)), updated);
    }

    private void updateFundingDetailsForBasePlan(
            Company company, Map<String, List<BenefitPlanRate>> rates, String benefitPlanId, String planType,
            BigDecimal cost, BigDecimal expectedEmployeeLIMIT) {
        Map<String, PlanMapping> realmPlanMapping = new HashMap<>();
        Map<String, Boolean> benOfferExceptions = new HashMap<>();
        // Initial groupFundingDetails map
        Map<String, Map<String, Object>> groupFundingDetails = new HashMap<>();
        Map<String, Object> coverageLevelFunding = new HashMap<>();
        groupFundingDetails.put(planType, coverageLevelFunding);
        coverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.BFPCT);
        coverageLevelFunding.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, planType);
        coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, benefitPlanId);
        coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), cost);
        coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), cost);
        coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), cost);
        coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), cost);
        coverageLevelFunding.put("employeeLIMIT", cost);
        coverageLevelFunding.put("employeePlusSpouseLIMIT", cost);
        coverageLevelFunding.put("employeePlusFamilyLIMIT", cost);
        coverageLevelFunding.put("employeePlusChildLIMIT", cost);
        // Mock realmDataDao to return a vendor ID for the benefit plan
        Map<String, String> limitPlanVendorIdMap = new HashMap<>();
        limitPlanVendorIdMap.put(benefitPlanId, "VENDOR123");
        when(realmDataDao.getPlanVendors(Mockito.anySet(), Mockito.anyLong())).thenReturn(limitPlanVendorIdMap);

        // Call the method under test
        RenewalServiceHelper.updateFundingDetailsForBasePlan(
                groupFundingDetails, rates, company, realmPlanMapping, realmDataDao, null, benOfferExceptions);

        // Verify that the funding details have been updated correctly
        assertEquals(cost, groupFundingDetails.get(planType).get("employee"));
        assertEquals(cost, groupFundingDetails.get(planType).get("employeePlusSpouse"));
        assertEquals(cost, groupFundingDetails.get(planType).get("employeePlusChild"));
        assertEquals(cost, groupFundingDetails.get(planType).get("employeePlusFamily"));
        assertEquals(expectedEmployeeLIMIT, groupFundingDetails.get(planType).get("employeeLIMIT"));
        assertEquals(expectedEmployeeLIMIT, groupFundingDetails.get(planType).get("employeePlusSpouseLIMIT"));
        assertEquals(expectedEmployeeLIMIT, groupFundingDetails.get(planType).get("employeePlusChildLIMIT"));
        assertEquals(expectedEmployeeLIMIT, groupFundingDetails.get(planType).get("employeePlusFamilyLIMIT"));
    }

    private BenefitPlanRate createPlanRate(
            CoverageCodesEnums coverageCode, String benefitPlanId, String planType, String bandCode, BigDecimal cost) {
        BenefitPlanRate bpr = new BenefitPlanRate();
        bpr.setBenefitPlan(benefitPlanId);
        bpr.setBandCode(bandCode);
        bpr.setCoverageCode(coverageCode.getCode());
        bpr.setEmployerCost(cost);
        bpr.setPlanType(planType);
        return bpr;
    }

	private BenefitPlan populateBenefitPlan(String planType) {
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setId("001302");
		benefitPlan.setPlanType(planType);
		List<PlanContribution> contributions = new ArrayList<>();
		PlanContribution pc = new PlanContribution();
		pc.setId(111L);
		pc.setBenefitPlanId("001302");
		pc.setType(CoverageCodesEnums.COV_EMPLOYEE.getId());
		pc.setEmployerContribution(BigDecimal.valueOf(560.7));
		pc.setEmployeeContribution(new BigDecimal(0));
		pc.setEmployerPercent(new BigDecimal(100));
		pc.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		benefitPlan.setContributions(contributions);
		contributions.add(pc);
		pc = new PlanContribution();
		pc.setId(111L);
		pc.setBenefitPlanId("001302");
		pc.setType(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
		pc.setEmployerContribution(BigDecimal.valueOf(560.7));
		pc.setEmployeeContribution(new BigDecimal(0));
		pc.setEmployerPercent(new BigDecimal(100));
		pc.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		benefitPlan.setContributions(contributions);
		contributions.add(pc);
		pc = new PlanContribution();
		pc.setId(111L);
		pc.setBenefitPlanId("001302");
		pc.setType(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId());
		pc.setEmployerContribution(BigDecimal.valueOf(560.7));
		pc.setEmployeeContribution(new BigDecimal(0));
		pc.setEmployerPercent(new BigDecimal(100));
		pc.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		benefitPlan.setContributions(contributions);
		contributions.add(pc);
		pc = new PlanContribution();
		pc.setId(111L);
		pc.setBenefitPlanId("001302");
		pc.setType(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId());
		pc.setEmployerContribution(BigDecimal.valueOf(560.7));
		pc.setEmployeeContribution(new BigDecimal(0));
		pc.setEmployerPercent(new BigDecimal(100));
		pc.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		benefitPlan.setContributions(contributions);
		contributions.add(pc);
		return benefitPlan;
	}

	private Map<String, List<BenefitPlanRate>> populateRates(String benefitPlan, String planType, float cost) {
		Map<String, List<BenefitPlanRate>> rates = new HashMap<>();

		List<BenefitPlanRate> bprList = new ArrayList<>();

		BenefitPlanRate bpr = new BenefitPlanRate();
		bpr.setBenefitPlan(benefitPlan);
		bpr.setBandCode("N");
		bpr.setCoverageCode(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		bpr.setEmployerCost(BigDecimal.valueOf(cost));
		bpr.setPlanType(planType);
		bprList.add(bpr);

		bpr = new BenefitPlanRate();
		bpr.setBenefitPlan(benefitPlan);
		bpr.setBandCode("N");
		bpr.setCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		bpr.setEmployerCost(BigDecimal.valueOf(cost));
		bpr.setPlanType(planType);
		bprList.add(bpr);

		bpr = new BenefitPlanRate();
		bpr.setBenefitPlan(benefitPlan);
		bpr.setBandCode("N");
		bpr.setCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode());
		bpr.setEmployerCost(BigDecimal.valueOf(cost));
		bpr.setPlanType(planType);
		bprList.add(bpr);

		bpr = new BenefitPlanRate();
		bpr.setBenefitPlan(benefitPlan);
		bpr.setBandCode("N");
		bpr.setCoverageCode(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode());
		bpr.setEmployerCost(BigDecimal.valueOf(cost));
		bpr.setPlanType(planType);

		bprList.add(bpr);
		rates.put(benefitPlan, bprList);
		return rates;
	}

	private Map<String, Object> populateCvgFunding(BigDecimal cost, String fundingType, String planType) {
		Map<String, Object> coverageLevelFunding = new HashMap<>();
		coverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, fundingType);
		coverageLevelFunding.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, planType);
		coverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, "001302");
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), cost);
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), cost);
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), cost);
		coverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), cost);

		coverageLevelFunding.put("employeeLIMIT", cost);
		coverageLevelFunding.put("employeePlusSpouseLIMIT", cost);
		coverageLevelFunding.put("employeePlusFamilyLIMIT", cost);
		coverageLevelFunding.put("employeePlusChildLIMIT", cost);

		return coverageLevelFunding;
	}	
	private Map<String, Map<String, BenefitPlan>> prepareBgAllHealthPlansMap() {
		Map<String, Map<String, BenefitPlan>> bgAllHealthPlansMap = new HashMap<>();
		Map<String, BenefitPlan> benefitPlans = new HashMap<>();
		benefitPlans.put("MEDPLAN1", prepareBenPlan("MEDPLAN1", 5L));
		benefitPlans.put("MEDPLAN2", prepareBenPlan("MEDPLAN2", 18L));
		benefitPlans.put("MEDPLAN4", prepareBenPlan("MEDPLAN4", 2L));
		benefitPlans.put("MEDPLAN5", prepareBenPlan("MEDPLAN5", 13L));
		// Plan for carrier which is not in future year plan year carrier list.
		benefitPlans.put("MEDPLAN6", prepareBenPlan("MEDPLAN5", 29L));
		bgAllHealthPlansMap.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, benefitPlans);
		return bgAllHealthPlansMap;
	}

	private Map<String, Set<StateBenefitPlan>> prepareAllBenefitStatePlansMap() {
		Map<String, Set<StateBenefitPlan>> allBenefitStatePlansMap = new HashMap<String, Set<StateBenefitPlan>>();
		Set<StateBenefitPlan> stateBenefitPlans = new HashSet<>();
		stateBenefitPlans.add(prepareStateBenefitPlan("PPO", "MEDPLAN1", 5L));
		//New mapping plan for MEDPLAN2
		stateBenefitPlans.add(prepareStateBenefitPlan("PPO", "MEDPLAN8", 18L));
		stateBenefitPlans.add(prepareStateBenefitPlan("PPO", "MEDPLAN3", 1L));
		stateBenefitPlans.add(prepareStateBenefitPlan("HMO", "MEDPLAN4", 2L));
		stateBenefitPlans.add(prepareStateBenefitPlan("PPO", "MEDPLAN5", 13L));
		stateBenefitPlans.add(prepareStateBenefitPlan("PPO", "MEDPLAN5", 29L));
		
		allBenefitStatePlansMap.put(BSSApplicationConstants.MEDICAL, stateBenefitPlans);
		return allBenefitStatePlansMap;
	}

	private Map<String, Set<PlanCarrier>> preparePlanCarriers() {
		Map<String, Set<PlanCarrier>> benOfferplanCarriers = new HashMap<>();
		Set<PlanCarrier> planCarriers = new HashSet<>();
		planCarriers.add(preparePlanCarrier(18, null, true));
		planCarriers.add(preparePlanCarrier(19, null, true));
		planCarriers.add(preparePlanCarrier(5, null, false));
		planCarriers.add(preparePlanCarrier(1, null, false));
		planCarriers.add(preparePlanCarrier(28, "1", false));
		planCarriers.add(preparePlanCarrier(27, "1", false));
		planCarriers.add(preparePlanCarrier(2, "1", false));
		planCarriers.add(preparePlanCarrier(13, "1", false));
		planCarriers.add(preparePlanCarrier(11, "1", false));
		planCarriers.add(preparePlanCarrier(24, "1", false));
		planCarriers.add(preparePlanCarrier(25, "1", false));

		benOfferplanCarriers.put(BSSApplicationConstants.MEDICAL, planCarriers);
		return benOfferplanCarriers;
	}

	private BenefitPlan prepareBenPlan(String id, Long planCarrierId) {
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setId(id);
		benefitPlan.setPlanCarrierId(planCarrierId);
		return benefitPlan;
	}
	
	private PlanCarrier preparePlanCarrier(int id, String parentId, boolean mandatory) {
		PlanCarrier planCarrier = new PlanCarrier();
		planCarrier.setId(id);
		planCarrier.setMandatory(mandatory);
		planCarrier.setParentId(Arrays.asList(parentId));
		return planCarrier;
	}

	private Map<String, Set<StateBenefitPlan>> prepareAllBenStatePlans() {
		Map<String, Set<StateBenefitPlan>> map = new HashMap<>();
		Set<StateBenefitPlan> plans = new HashSet<>();
		plans.add(prepareStateBenefitPlan(BSSApplicationConstants.FPL, "FPL_PLAN1", 1111));
		plans.add(prepareStateBenefitPlan(BSSApplicationConstants.FPL, "FPL_PLAN2", 1111));
		plans.add(prepareStateBenefitPlan(BSSApplicationConstants.FPL, "FPL_PLAN3", 2222));
		plans.add(prepareStateBenefitPlan(BSSApplicationConstants.MND, "MND_PLAN1", 3333));
		plans.add(prepareStateBenefitPlan(BSSApplicationConstants.MND, "MND_PLAN2", 3333));
		plans.add(prepareStateBenefitPlan(BSSApplicationConstants.MND, "MND_PLAN3", 4444));
		map.put(BSSApplicationConstants.MEDICAL, plans);
		return map;
	}

	private Map<String, StateBenefitPlan> prepareMandatoryPlans() {
		Map<String, StateBenefitPlan> map = new HashMap<>();
		map.put("MND_PLAN1", prepareStateBenefitPlan(BSSApplicationConstants.MND, "BEN_PLAN1", 3333));
		return map;
	}

	private StateBenefitPlan prepareStateBenefitPlan(String category, String benPlan, long portfolioId) {
		StateBenefitPlan sbp = new StateBenefitPlan();
		sbp.setPlanCategory(category);
		sbp.setBenefitPlan(benPlan);
		sbp.setPortfolioId(portfolioId);
		sbp.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		return sbp;
	}

	private Map<String, BenefitPlan> prepareBenPlanMap() {
		Map<String, BenefitPlan> map = new HashMap<>();
		BenefitPlan bp = new BenefitPlan();
		bp.setId("MED1111");
		bp.setVendorId("VM1111");
		bp.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		map.put("MED1111", bp);
		bp = new BenefitPlan();
		bp.setId("MED3333");
		bp.setVendorId("VM3333");
		bp.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		map.put("MED3333", bp);
		bp = new BenefitPlan();
		bp.setId("DEN1111");
		bp.setVendorId("VD1111");
		bp.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		map.put("DEN1111", bp);
		bp = new BenefitPlan();
		bp.setId("VIS1111");
		bp.setVendorId("VV1111");
		bp.setPlanType(BSSApplicationConstants.VISION_PLAN_TYPE);
		map.put("VIS1111", bp);
		return map;
	}

	private Map<String, Map<String, Set<String>>> prepareAutoSelectPlans() {
		Map<String, Map<String, Set<String>>> autoSelectPlans = new HashMap<>();
		Map<String, Set<String>> bpCrossRefPlans = new HashMap<>();
		Set<String> crossRefPlans = new HashSet<>();
		crossRefPlans.add("MED2222");
		crossRefPlans.add("MED3333");
		bpCrossRefPlans.put("MED1111", crossRefPlans);
		autoSelectPlans.put(BSSApplicationConstants.MEDICAL, bpCrossRefPlans);
		bpCrossRefPlans = new HashMap<>();
		crossRefPlans = new HashSet<>();
		crossRefPlans.add("DEN2222");
		bpCrossRefPlans.put("DEN1111", crossRefPlans);
		crossRefPlans.add("DEN2222");
		autoSelectPlans.put(BSSApplicationConstants.DENTAL, bpCrossRefPlans);
		autoSelectPlans.put(BSSApplicationConstants.VISION, null);
		return autoSelectPlans;
	}

	private Map<String, List<CoverageLevel>> prepareMapOfCovgCodes() {
		Map<String, List<CoverageLevel>> planCovgCode = new HashMap<>();
		List<CoverageLevel> covgCodes = new ArrayList<>();
		covgCodes.add(new CoverageLevel("1", CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(100)));
		covgCodes.add(new CoverageLevel("2", CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(200)));
		covgCodes.add(new CoverageLevel("3", CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(300)));
		planCovgCode.put(BSSApplicationConstants.MEDICAL, covgCodes);
		covgCodes = new ArrayList<>();
		covgCodes.add(new CoverageLevel("1", CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(80)));
		planCovgCode.put(BSSApplicationConstants.DENTAL, covgCodes);
		return planCovgCode;
	}
	
	private void addPlyrToMap(Map<String,XbssRealmPlyrPlan> plyrMap, long id, long rpyId, String planType, String benefitPlan,
			long portId, String situs, String locator) {

		plyrMap.put(benefitPlan, this.makePlyrObj(id, rpyId, planType, benefitPlan, portId, situs, locator));
		
	}

	private XbssRealmPlyrPlan makePlyrObj(long id, long rpyId, String planType, String benefitPlan,
			long portId, String situs, String locator ) {
		XbssRealmPlyrPlan plyr = new XbssRealmPlyrPlan();
		plyr.setId( id );
		plyr.setRealmYearId( BigDecimal.valueOf( rpyId ) );
		plyr.setPlanType( planType );
		plyr.setBenefitPlan( benefitPlan );
		plyr.setPortfolioId( BigDecimal.valueOf( portId ) );
		plyr.setBandLocator( locator );
		plyr.setSitus(situs);
		return plyr;
	}
	
	private List<PlanCoverageLevelHeadCount> prepareCoverageLevelHeadCount() {

		List<PlanCoverageLevelHeadCount> headCountList = new ArrayList<>();
		PlanCoverageLevelHeadCount covgLvlHeadCnt = new PlanCoverageLevelHeadCount();
		covgLvlHeadCnt.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		covgLvlHeadCnt.setHeadCount(2);
		covgLvlHeadCnt.setHsaHeadCount(1);
		headCountList.add(covgLvlHeadCnt);
		covgLvlHeadCnt = new PlanCoverageLevelHeadCount();
		covgLvlHeadCnt.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		covgLvlHeadCnt.setHeadCount(4);
		covgLvlHeadCnt.setHsaHeadCount(2);
		headCountList.add(covgLvlHeadCnt);

		return headCountList;
	}
	
	private Map<String, BigDecimal> prepareMinimumFundingMap() {
		Map<String, BigDecimal> returnMap = new HashMap<>();
		returnMap.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, BigDecimal.valueOf(100));
		returnMap.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, BigDecimal.valueOf(100));
		returnMap.put(BSSApplicationConstants.VISION_PLAN_TYPE, BigDecimal.valueOf(100));
		return returnMap;
	}

	private List<Contribution> prepareContributions() {
		List<Contribution> contributions = new ArrayList<>();
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setId("PLAN_ONE");
		benefitPlan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		Contribution contribution = new Contribution();
		contribution.setCoverageLevel(BSSApplicationConstants.CVG_CODE_EMPLOYEE);
		contribution.setEmployeeContribution(BigDecimal.valueOf(100));
		contribution.setEmployerContribution(BigDecimal.valueOf(200));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(200));
		contribution.setEmployerContribution(BigDecimal.valueOf(4));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);

		benefitPlan = new BenefitPlan();
		benefitPlan.setId("PLAN_TWO");
		benefitPlan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		contribution = new Contribution();
		contribution.setCoverageLevel(BSSApplicationConstants.CVG_CODE_EMPLOYEE);
		contribution.setEmployeeContribution(BigDecimal.valueOf(100));
		contribution.setEmployerContribution(BigDecimal.valueOf(200));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(200));
		contribution.setEmployerContribution(BigDecimal.valueOf(400));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);

		benefitPlan = new BenefitPlan();
		benefitPlan.setId("DENTAL_ONE");
		benefitPlan.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		contribution = new Contribution();
		contribution.setCoverageLevel(BSSApplicationConstants.CVG_CODE_EMPLOYEE);
		contribution.setEmployeeContribution(BigDecimal.valueOf(10));
		contribution.setEmployerContribution(BigDecimal.valueOf(20));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(20));
		contribution.setEmployerContribution(BigDecimal.valueOf(40));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);
		
		benefitPlan = new BenefitPlan();
		benefitPlan.setId("VISION");
		benefitPlan.setPlanType(BSSApplicationConstants.VISION_PLAN_TYPE);
		contribution = new Contribution();
		contribution.setCoverageLevel(BSSApplicationConstants.CVG_CODE_EMPLOYEE);
		contribution.setEmployeeContribution(BigDecimal.valueOf(5));
		contribution.setEmployerContribution(BigDecimal.valueOf(10));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setCoverageLevel(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		contribution.setEmployeeContribution(BigDecimal.valueOf(10));
		contribution.setEmployerContribution(BigDecimal.valueOf(15));
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);

		return contributions;
	}
	
	private List<PlanSelection> preparePlanSelections() {
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection planSelection = new PlanSelection();
		planSelection.setPlanType(PlanTypesEnum.MEDICAL.getCode());
		planSelection.setBenefitPlan("MEDPLAN1");
		planSelections.add(planSelection);
		
		planSelection = new PlanSelection();
		planSelection.setPlanType(PlanTypesEnum.DENTAL.getCode());
		planSelection.setBenefitPlan("DENTALPLAN1");
		planSelections.add(planSelection);
		
		planSelection = new PlanSelection();
		planSelection.setPlanType(PlanTypesEnum.VISION.getCode());
		planSelection.setBenefitPlan("VISIONPLAN1");
		planSelections.add(planSelection);
		
		return planSelections;
	}
	
	private Map<String, List<BenefitPlanRate>> preparePlanRates() {
		Map<String, List<BenefitPlanRate>> rates = new HashMap<>();
		return rates;
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setRealmPlanYearId(10);
		company.setRegionalMinimumFundings(prepareRegionalMinimumFunding());
		company.setMinFundings(prepareMinimumFundings());
		return company;
	}
	
	private Set<MinimumFunding> prepareMinimumFundings() {
		Set<MinimumFunding> returnSet = new HashSet<>();

		MinimumFunding minimumFunding = new MinimumFunding(BSSApplicationConstants.MEDICAL,
				BSSApplicationConstants.FLAT, BigDecimal.valueOf(100), false);
		minimumFunding.setPlanType(BSSApplicationConstants.MEDICAL);
		returnSet.add(minimumFunding);

		minimumFunding = new MinimumFunding(BSSApplicationConstants.DENTAL,
				BSSApplicationConstants.FLAT, BigDecimal.valueOf(100), false);
		minimumFunding.setPlanType(BSSApplicationConstants.DENTAL);
		returnSet.add(minimumFunding);
		return returnSet;
	}
	
	private List<RegionalMinimumFunding> prepareRegionalMinimumFunding() {
		List<RegionalMinimumFunding> returnList = new ArrayList<>();
		
		RegionalMinimumFunding regionalMinimumFunding = new RegionalMinimumFunding();
		regionalMinimumFunding.setRegion("STATE_1");
		regionalMinimumFunding.setFundingPct(BigDecimal.valueOf(75));
		returnList.add(regionalMinimumFunding);
		
		regionalMinimumFunding = new RegionalMinimumFunding();
		regionalMinimumFunding.setRegion("STATE_2");
		regionalMinimumFunding.setFundingPct(BigDecimal.valueOf(85));
		returnList.add(regionalMinimumFunding);		
		return returnList;
	}

	private Map<String, List<String>> preparePlanRegions() {
		Map<String, List<String>> planRegions = new HashMap<>();
		planRegions.put("PLAN_ONE", Arrays.asList("STATE_1", "STATE_2"));
		planRegions.put("PLAN_TWO", Arrays.asList("STATE_3", "STATE_4"));
		return planRegions;
	}
	
	private Map<String, List<Contribution>> prepareBenefitPlanContributions() {
		Map<String, List<Contribution>> benefitPlanContributions = new HashMap<>();
		benefitPlanContributions.put("PLAN_ONE", prepareContributions());
		benefitPlanContributions.put("PLAN_TWO", prepareContributions());
		return benefitPlanContributions;
	};
	
	private List<Contribution> prepareContributionsForEstimates() {
		List<Contribution> contributions = new ArrayList<>();
		BenefitPlan benefitPlan = new BenefitPlan();
		benefitPlan.setId("PLAN_ONE");
		benefitPlan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		Contribution contribution = new Contribution();
		contribution.setCoverageLevel(BSSApplicationConstants.CVG_CODE_EMPLOYEE);
		contribution.setEmployerPercent(BigDecimal.valueOf(50));		
		contribution.setEmployeeContribution(BigDecimal.valueOf(100));
		contribution.setEmployerContribution(BigDecimal.valueOf(200));
		contribution.setHsaHeadCount(1);
		contribution.setBenefitPlanAssociation(benefitPlan);
		contributions.add(contribution);
		
		
		BenefitPlan dental = new BenefitPlan();
		dental.setId("PLAN_ONE");
		dental.setPlanType(BSSApplicationConstants.DENTAL_PLAN_TYPE);
		Contribution dentalContribution = new Contribution();
		dentalContribution.setCoverageLevel(BSSApplicationConstants.CVG_CODE_EMPLOYEE);
		dentalContribution.setEmployerPercent(BigDecimal.valueOf(50));
		dentalContribution.setEmployeeContribution(BigDecimal.valueOf(100));
		dentalContribution.setEmployerContribution(BigDecimal.valueOf(200));
		dentalContribution.setHsaHeadCount(1);
		dentalContribution.setBenefitPlanAssociation(dental);
		contributions.add(dentalContribution);
		
		
		BenefitPlan vision = new BenefitPlan();
		vision.setId("PLAN_ONE");
		vision.setPlanType(BSSApplicationConstants.VISION_PLAN_TYPE);
		Contribution visionContribution = new Contribution();
		visionContribution.setCoverageLevel(BSSApplicationConstants.CVG_CODE_EMPLOYEE);
		//visionContribution.setEmployerPercent(BigDecimal.valueOf(50));
		visionContribution.setEmployeeContribution(BigDecimal.valueOf(100));
		visionContribution.setEmployerContribution(BigDecimal.valueOf(200));
		visionContribution.setHsaHeadCount(1);
		visionContribution.setBenefitPlanAssociation(vision);
		contributions.add(visionContribution);
		
		return contributions;
	}
	
	private Map<String, Map<String, Object>> prepareGroupFundingDetails() {
		Map<String, Map<String, Object>> groupFundingDetails = new HashMap<>();
		
		Map<String, Object> medicalCoverageLevelFunding = new HashMap<>();
		medicalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, "BSUPP");
		medicalCoverageLevelFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, BigDecimal.ONE);
		medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(100));
		medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(100));
		medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(100));
		medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(100));
		groupFundingDetails.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, medicalCoverageLevelFunding);

		Map<String, Object> dentalCoverageLevelFunding = new HashMap<>();
		dentalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, "CFPCT");
		dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(100));
		dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(100));
		dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(100));
		dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(100));
		groupFundingDetails.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, dentalCoverageLevelFunding);

		Map<String, Object> visionCoverageLevelFunding = new HashMap<>();
		visionCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, "CFPCT");
		visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal(100));
		visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal(100));
		visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal(100));
		visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), new BigDecimal(100));
		groupFundingDetails.put(BSSApplicationConstants.VISION_PLAN_TYPE, visionCoverageLevelFunding);
		return groupFundingDetails;
	}

	private Map<String, Map<String, BenefitPlan>> prepareHealthPlansMap() {
		Map<String, Map<String, BenefitPlan>> offerMap = new HashMap<>();
		Map<String, BenefitPlan> planMap = new HashMap<>();
		planMap.put("MEDPLAN1", new BenefitPlan());
		planMap.put("MEDPLAN2", new BenefitPlan());
		offerMap.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, planMap);
		planMap = new HashMap<>();
		planMap.put("DENTALPLAN1", new BenefitPlan());
		planMap.put("DENTALPLAN2", new BenefitPlan());
		offerMap.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, planMap);
		planMap = new HashMap<>();
		planMap.put("VISIONPLAN1", new BenefitPlan());
		offerMap.put(BSSApplicationConstants.VISION_PLAN_TYPE, planMap);
		return offerMap;
	}
	
	private Map<String, PlanMapping> preparePrimaryPlanMap() {
		Map<String, PlanMapping> primaryPlanMap = new HashMap<>();
		PlanMapping planMapping = new PlanMapping();
		planMapping.setNewBenefitPlans(Arrays.asList("MAPPEDMEDICAL"));
		primaryPlanMap.put("MEDPLAN1", planMapping);
		return primaryPlanMap;
	}
	
	private Map<String, Set<Long>> prepareSelectedPlanCarriers() {
		Map<String, Set<Long>> selectedPlanCarriers = new HashMap<>();
		selectedPlanCarriers.put(PlanTypesEnum.MEDICAL.getCode(), new HashSet<Long>(Arrays.asList(1L)));
		selectedPlanCarriers.put(PlanTypesEnum.DENTAL.getCode(), new HashSet<Long>(Arrays.asList(1L)));
		selectedPlanCarriers.put(PlanTypesEnum.VISION.getCode(), new HashSet<Long>(Arrays.asList(1L)));	
		return selectedPlanCarriers;
	}
	
	private List<CarrierMinimumFunding> prepareCarrierMinimumFundings() {

		List<CarrierMinimumFunding> minFundings = new ArrayList<>();
		minFundings.add(new CarrierMinimumFunding(1, PlanTypesEnum.MEDICAL.getCode(), BigDecimal.valueOf(75)));
		minFundings.add(new CarrierMinimumFunding(1, PlanTypesEnum.DENTAL.getCode(), BigDecimal.valueOf(75)));
		minFundings.add(new CarrierMinimumFunding(1, PlanTypesEnum.VISION.getCode(), BigDecimal.valueOf(75)));
		return minFundings;

	}

}
