package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PlanHeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.impl.PlanHeadCountServiceImpl;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitPlanHeadCount;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.service.model.HeadCountBenefitPlan;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.PlanHeadCount;
import com.trinet.ambis.service.prospect.ProspectPlanHeadCountService;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author schaudhari
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PlanHeadCountServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	PlanHeadCountServiceImpl planHeadCountService;

	@Mock
	PlanHeadCountDao planHeadCountDao;
	@Mock
	CompanyService companyService;
	@Mock
	RenewalDataDao renewalDataDao;
	@Mock
	RealmPlanYearRuleService realmPlanYearRuleService;
	@Mock
	RealmPlanYearService realmPlanYearService;
	@Mock
	RealmDataDao realmDataDao;
	@Mock
	StrategyDataDao strategyDataDao;
	@Mock
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;
	@Mock
	BenefitGroupService benefitGroupService;
	@Mock
	PortfolioRuleDao portfolioRuleDao;
	@Mock
	ProspectPlanHeadCountService prospectPlanHeadCountService;

	@Mock
	PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	
	@Mock
	PlanMappingDao planMappingDao;
	
	@Mock
	HeadCountService headCountService;
	
	RealmPlanYear realmPlanYear;
	Company company;
	long strategyId;
	long realmYrId;
	
	private static final String BENEFIT_PROGRAM_1 = "001JTW";
	private static final String BENEFIT_PROGRAM_2 = "001JVD";
	private static final String BENEFIT_PROGRAM_3 = "001MC5";


    private MockedStatic<BenefitCategoriesHelper> benefitCategoriesHelperMockedStatic;
    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @After
    public void tearDown() {
        benefitCategoriesHelperMockedStatic.close();
        commonServiceHelperMockedStatic.close();
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Before
	public void setup() {
        benefitCategoriesHelperMockedStatic = Mockito.mockStatic(BenefitCategoriesHelper.class);
        commonServiceHelperMockedStatic = Mockito.mockStatic(CommonServiceHelper.class);
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
		realmYrId = 21;

		realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(realmYrId);
		realmPlanYear.setId(realmYrId);

		company = new Company();
		company.setRealmPlanYear(realmPlanYear);
		company.setPlanStartDate("01-JAN-2020");
		company.setExclusiveMedPlan( "DFLT" );
		strategyId = 0L;
	}

	@Test
	public void getPlanHeadCount() {
		RealmPlanYear prevRealmYear = new RealmPlanYear();
		prevRealmYear.setPlanYearEnd(new Date());

		when(realmDataDao.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, realmYrId))
				.thenReturn(prepareMapOfCovgLevel());
		
		Map<String, Map<String, String>> defaultPlanMap = new HashMap<>();
		Mockito.when(realmDataDao
		.getPortfilioDefaultPlans(company.getRealmPlanYearId())).thenReturn(defaultPlanMap);

		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Mockito.when(portfolioRuleDao.getPortfoliosByHqRegion(
				company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getZipCode(),
				company.getExclusiveMedPlan(), company.getPlanStartDate(), false )).thenReturn(planCarrierMap);
		
		Set<String> primaryPlanCarriers = new HashSet<>();
		
		when(BenefitCategoriesHelper.getPlanCarriers(planCarrierMap)).thenReturn(primaryPlanCarriers);
		
		Set<String> outOfRegionPlans = new HashSet<>();
				
		when(CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao)).thenReturn(outOfRegionPlans);
		when(RulesAndConfigsUtils.isVendorMappingOn(Mockito.anyLong())).thenReturn(true);
		
		Mockito.when(planMappingDao.getPrimaryPlanMappings(company, outOfRegionPlans))
				.thenReturn(new HashMap<>());
		
		when(employerEmployeePlansMappingDao.getEeAndErPlanMapping(company.getRealmPlanYearId()))
				.thenReturn(Collections.<String, String>emptyMap());
		when(strategyDataDao.getFutureStrategies(company.getCode(), company.getRealmPlanYear().getId()))
				.thenReturn(prepareStrategies());
		when(strategyDataDao.getOfferedPlanTypesByStrategy("1111"))
				.thenReturn(Collections.<String, List<String>>emptyMap());
		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.any(RealmPlanYear.class))).thenReturn(prevRealmYear);
		when(renewalDataDao.getPlanHeadCountByGroups(Mockito.any(Company.class), Mockito.anyLong(), Mockito.any(Date.class),
				Mockito.anyMap(), Mockito.anyMap(), Mockito.anyMap(), Mockito.anyBoolean())).thenReturn(prepareGrpCovgHeadCountMap());
		when(benefitGroupService.getAllBenefitGroups(company.getId(), BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenGrps());
		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean())).thenReturn(prepareActiveEligEmplCount());
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( eq( company )) ).thenReturn( false );
		
		List<PlanHeadCount> actualResult = planHeadCountService.getPlanHeadCount(company, strategyId);

		assertEquals(3, actualResult.size());
		for (PlanHeadCount planHeadCount : actualResult) {
			assertTrue(Arrays.asList(BENEFIT_PROGRAM_1, BENEFIT_PROGRAM_2, BENEFIT_PROGRAM_3).contains(planHeadCount.getBenefitProgram()));

			if (BENEFIT_PROGRAM_1.equals(planHeadCount.getBenefitProgram())) {
				assertEquals(1, planHeadCount.getBenefitPlans().size());
				assertEquals("benPlan1", planHeadCount.getBenefitPlans().get(0).getBenefitPlan());
				assertEquals(4, planHeadCount.getBenefitPlans().get(0).getCoverageLevelHeadCount().size());
				for (CoverageLevelHeadCount covgLevelHeadCount : planHeadCount.getBenefitPlans().get(0)
						.getCoverageLevelHeadCount()) {
					if ("1".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_1, covgLevelHeadCount.getBenefitProgram());
						assertEquals(4, covgLevelHeadCount.getHeadCount());
					} else if ("2".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_1, covgLevelHeadCount.getBenefitProgram());
						assertEquals(1, covgLevelHeadCount.getHeadCount());
					} else if ("C".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_1, covgLevelHeadCount.getBenefitProgram());
						assertEquals(2, covgLevelHeadCount.getHeadCount());
					} else if ("4".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_1, covgLevelHeadCount.getBenefitProgram());
						assertEquals(0, covgLevelHeadCount.getHeadCount());
					}
				}
			} else if (BENEFIT_PROGRAM_2.equals(planHeadCount.getBenefitProgram())) {
				assertEquals("benPlan2", planHeadCount.getBenefitPlans().get(0).getBenefitPlan());
				assertEquals(4, planHeadCount.getBenefitPlans().get(0).getCoverageLevelHeadCount().size());
				for (CoverageLevelHeadCount covgLevelHeadCount : planHeadCount.getBenefitPlans().get(0)
						.getCoverageLevelHeadCount()) {
					if ("1".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_2, covgLevelHeadCount.getBenefitProgram());
						assertEquals(1, covgLevelHeadCount.getHeadCount());
					} else if ("2".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_2, covgLevelHeadCount.getBenefitProgram());
						assertEquals(0, covgLevelHeadCount.getHeadCount());
					} else if ("C".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_2, covgLevelHeadCount.getBenefitProgram());
						assertEquals(2, covgLevelHeadCount.getHeadCount());
					} else if ("4".equals(covgLevelHeadCount.getCoverageLevel())) {
						assertEquals(BENEFIT_PROGRAM_2, covgLevelHeadCount.getBenefitProgram());
						assertEquals(6, covgLevelHeadCount.getHeadCount());
					}
				}
			} else {
				assertNull(planHeadCount.getBenefitPlans());
			}
		}
	}
	
	@Test
	public void getBenefitProgramHeadCountPlans() {
		RealmPlanYear prevRealmYear = new RealmPlanYear();
		prevRealmYear.setPlanYearEnd(new Date());
		prevRealmYear.setId(0);
		long strategyId = 123L;
		List<BenefitGroup> benGrps = prepareBenGroups();

		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = prepareBenefitProgramHeadCountMap();

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupAdditionalHeadCountMap = new HashMap<>();

		when(portfolioHeadCountDataDao.getHeadCountPlans(strategyId)).thenReturn(benefitProgramHeadCounts);

		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(benGrps);
		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.any(RealmPlanYear.class))).thenReturn(prevRealmYear);

		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean())).thenReturn(prepareActiveEligEmplCount());
		
		when(renewalDataDao.getAdditionalPlansHeadCountByGroup(company.getCode(), prevRealmYear.getPlanYearEnd()))
				.thenReturn(groupAdditionalHeadCountMap);

		List<BenefitProgramHeadCountPlans> actualResult = planHeadCountService.getBenefitProgramHeadCountPlans(company,
				strategyId);
		assertEquals(4, actualResult.size());

	}
	
	@Test
	public void getBenefitProgramHeadCountPlans1() {
		RealmPlanYear prevRealmYear = new RealmPlanYear();
		prevRealmYear.setPlanYearEnd(new Date());
		prevRealmYear.setId(0);
		long strategyId = 123L;
		List<BenefitGroup> benGrps = prepareBenGroups();

		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = new HashMap<>();

		List<HeadCountBenefitPlan> hcb = new ArrayList<>();
		benefitProgramHeadCounts.put(BENEFIT_PROGRAM_1, hcb);
		
		when(portfolioHeadCountDataDao.getHeadCountPlans(strategyId)).thenReturn(benefitProgramHeadCounts);

		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(benGrps);
		when(realmPlanYearService.getPreviousRealmPlanYear(Mockito.any(RealmPlanYear.class))).thenReturn(prevRealmYear);

		when(headCountService.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.any(RealmPlanYear.class), Mockito.anyBoolean())).thenReturn(prepareActiveEligEmplCount());

		List<BenefitProgramHeadCountPlans> actualResult = planHeadCountService.getBenefitProgramHeadCountPlans(company,
				strategyId);
		assertEquals(2, actualResult.size());

	}
	
	private List<BenefitGroup> prepareBenGroups() {
		List<BenefitGroup> benGrps = new ArrayList<>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setBenefitProgram(BENEFIT_PROGRAM_1);
		benGrps.add(benGrp);
		
		benGrp = new BenefitGroup();
		benGrp.setBenefitProgram("001JTX");
		benGrps.add(benGrp);
		return benGrps;
	}

	private Map<String, List<CoverageLevel>> prepareMapOfCovgLevel() {
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
		CoverageLevel allCovgLevel = new CoverageLevel();
		allCovgLevel.setId("all");
		allCovgLevel.setName("All Levels");
		CoverageLevel empOnlyCovgLevel = new CoverageLevel();
		empOnlyCovgLevel.setId("employee");
		empOnlyCovgLevel.setName("Employee Only");
		CoverageLevel empSpouseCovgLevel = new CoverageLevel();
		empSpouseCovgLevel.setId("employeePlusSpouse");
		empSpouseCovgLevel.setName("Employee + Spouse");
		CoverageLevel empChildCovgLevel = new CoverageLevel();
		empChildCovgLevel.setId("employeePlusChild");
		empChildCovgLevel.setName("Employee + Child(ren)");
		List<CoverageLevel> medicalCoverageLevels = new ArrayList<>();
		medicalCoverageLevels.add(allCovgLevel);
		medicalCoverageLevels.add(empOnlyCovgLevel);
		medicalCoverageLevels.add(empSpouseCovgLevel);
		medicalCoverageLevels.add(allCovgLevel);
		List<CoverageLevel> dentalCoverageLevels = new ArrayList<>();
		dentalCoverageLevels.add(allCovgLevel);
		dentalCoverageLevels.add(empOnlyCovgLevel);
		dentalCoverageLevels.add(empSpouseCovgLevel);
		dentalCoverageLevels.add(allCovgLevel);
		List<CoverageLevel> visionCoverageLevels = new ArrayList<>();
		mapOfCoverageLevels.put(Constants.MEDICAL, medicalCoverageLevels);
		mapOfCoverageLevels.put(Constants.DENTAL, dentalCoverageLevels);
		mapOfCoverageLevels.put(Constants.VISION, visionCoverageLevels);
		return mapOfCoverageLevels;
	}

	private List<Strategy> prepareStrategies() {
		List<Strategy> strategies = new ArrayList<>();
		Strategy s = new Strategy();
		s.setType(BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED);
		s.setId(1111L);
		strategies.add(s);
		s = new Strategy();
		s.setType(BSSApplicationConstants.STRATEGY_TYPE_CUSTOM);
		s.setId(2222L);
		strategies.add(s);
		return strategies;
	}

	private Map<String, List<BenefitPlanHeadCount>> prepareGrpCovgHeadCountMap() {
		Map<String, List<BenefitPlanHeadCount>> groupCovrgHeadCountMap = new HashMap<>();
		List<BenefitPlanHeadCount> bpHeadCounts = new ArrayList<>();
		BenefitPlanHeadCount bpHeadCount = new BenefitPlanHeadCount();
		bpHeadCount.setBenefitPlan("benPlan1");
		bpHeadCount.setPlanType("10");
		List<CoverageLevelHeadCount> coverageLevelHeadCounts = new ArrayList<>();
		CoverageLevelHeadCount coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_1);
		coverageLevelHeadCount.setCoverageLevel("1");
		coverageLevelHeadCount.setHeadCount(4);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_1);
		coverageLevelHeadCount.setCoverageLevel("2");
		coverageLevelHeadCount.setHeadCount(1);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_1);
		coverageLevelHeadCount.setCoverageLevel("C");
		coverageLevelHeadCount.setHeadCount(2);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_1);
		coverageLevelHeadCount.setCoverageLevel("4");
		coverageLevelHeadCount.setHeadCount(0);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		bpHeadCount.setCoverageLevelHeadCount(coverageLevelHeadCounts);
		bpHeadCounts.add(bpHeadCount);
		groupCovrgHeadCountMap.put(BENEFIT_PROGRAM_1, bpHeadCounts);

		bpHeadCounts = new ArrayList<>();
		bpHeadCount = new BenefitPlanHeadCount();
		bpHeadCount.setBenefitPlan("benPlan2");
		bpHeadCount.setPlanType("11");
		coverageLevelHeadCounts = new ArrayList<>();
		coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_2);
		coverageLevelHeadCount.setCoverageLevel("1");
		coverageLevelHeadCount.setHeadCount(1);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_2);
		coverageLevelHeadCount.setCoverageLevel("2");
		coverageLevelHeadCount.setHeadCount(0);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_2);
		coverageLevelHeadCount.setCoverageLevel("C");
		coverageLevelHeadCount.setHeadCount(2);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setBenefitProgram(BENEFIT_PROGRAM_2);
		coverageLevelHeadCount.setCoverageLevel("4");
		coverageLevelHeadCount.setHeadCount(6);
		coverageLevelHeadCounts.add(coverageLevelHeadCount);
		bpHeadCount.setCoverageLevelHeadCount(coverageLevelHeadCounts);
		bpHeadCounts.add(bpHeadCount);
		groupCovrgHeadCountMap.put(BENEFIT_PROGRAM_2, bpHeadCounts);
		return groupCovrgHeadCountMap;
	}

	private List<BenefitGroup> prepareBenGrps() {
		List<BenefitGroup> benGrps = new ArrayList<>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setBenefitProgram(BENEFIT_PROGRAM_1);
		benGrps.add(benGrp);
		benGrp = new BenefitGroup();
		benGrp.setBenefitProgram(BENEFIT_PROGRAM_2);
		benGrps.add(benGrp);
		benGrp = new BenefitGroup();
		benGrp.setBenefitProgram(BENEFIT_PROGRAM_3);
		benGrps.add(benGrp);
		return benGrps;
	}

	private Map<String, ActiveEligibleEECount> prepareActiveEligEmplCount() {
		Map<String, ActiveEligibleEECount> activeEligibleEmplCounts = new HashMap<>();
		ActiveEligibleEECount activeElegEECount = new ActiveEligibleEECount();
		activeElegEECount.setPrimaryHeadCount(2);
		activeElegEECount.setSecondaryHeadCount(3);
		activeElegEECount.setTotalHeadCount(0);
		activeEligibleEmplCounts.put(BENEFIT_PROGRAM_1, activeElegEECount);
		activeElegEECount = new ActiveEligibleEECount();
		activeElegEECount.setPrimaryHeadCount(1);
		activeElegEECount.setSecondaryHeadCount(0);
		activeElegEECount.setTotalHeadCount(4);
		activeEligibleEmplCounts.put(BENEFIT_PROGRAM_2, activeElegEECount);
		activeEligibleEmplCounts.put(BENEFIT_PROGRAM_3, null);
		return activeEligibleEmplCounts;
	}
	
	private Map<String, List<HeadCountBenefitPlan>> prepareBenefitProgramHeadCountMap() {

		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCountMap = new HashMap<>();
		List<HeadCountBenefitPlan> hcb = new ArrayList<>();
		HeadCountBenefitPlan bp = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder()
				.benefitPlanId("MED_PLAN_1")
				.planType(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.planCarrierId(1).populateZeroCvgLvlHeadCounts(true)
				.build();
		hcb.add(bp);
		benefitProgramHeadCountMap.put(BENEFIT_PROGRAM_1, hcb);

		hcb = new ArrayList<>();
		bp = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder()
				.benefitPlanId("MED_PLAN_2")
				.planType(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.planCarrierId(1).populateZeroCvgLvlHeadCounts(true)
				.build();
		hcb.add(bp);
		benefitProgramHeadCountMap.put(BENEFIT_PROGRAM_2, hcb);

		hcb = new ArrayList<>();
		bp = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder()
				.benefitPlanId("MED_PLAN_1")
				.planType(CoverageCodesEnums.COV_EMPLOYEE.getCode())
				.planCarrierId(1).populateZeroCvgLvlHeadCounts(true)
				.build();
		hcb.add(bp);
		benefitProgramHeadCountMap.put(BENEFIT_PROGRAM_3, hcb);

		return benefitProgramHeadCountMap;
	}

}
