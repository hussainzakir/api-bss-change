package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.hrp.TemplateFundingDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCountId;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.ExchangeService;
import com.trinet.ambis.service.HeadCountDistributionService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyGroupHeadCountService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.impl.BenefitGroupHeadCountServiceImpl;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.HeadCountData;
import com.trinet.ambis.service.model.Industry;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;

/**
 * @author schaudhari
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class BenefitGroupHeadCountServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	BenefitGroupHeadCountServiceImpl benGroupHeadCountService;

	@Mock
	CompanyService companyService;

	@Mock
	PlanRatesService planRatesService;

	@Mock
	BenefitGroupService benefitGroupService;

	@Mock
	StrategyGroupService strategyGroupService;

	@Mock
	StrategyGroupHeadCountService strategyGroupHeadCountService;

	@Mock
	PlanSelectionService planSelectionService;

	@Mock
	ContributionService contributionService;

	@Mock
	HeadCountDistributionService headCountDistributionService;

	@Mock
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;

	@Mock
	TemplateFundingDao templateFundingDao;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	StrategyDao strategyDao;

	@Mock
	BenefitPlanService benefitPlanService;

	@Mock
	RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	StrategyGroupDataDao strategyGroupDataDao;

	@Mock
	XbssRealmPlyrPlanDao realmPlyrPlanDao;

	@Mock
	ExchangeService exchangeService;

	private static final String MEDICAL_BEN_PLAN = "medicalBenPlan";
	private static final String DENTAL_BEN_PLAN = "dentalBenPlan";
	private static final String VISION_BEN_PLAN = "visionBenPlan";

	Company company;
	long strategyGroupId;
	long groupId;
	long strategyId;
	long companyId;
	String strategyPkgType;
	long realmPlanYearId;
	String headQtrState;
	IndustryType industryType;
	long planSelectionId1;
	long planSelectionId2;
	long planSelectionId3;

	@Captor
	ArgumentCaptor<Map<String, Integer>> headCountCaptor;

	@Captor
	ArgumentCaptor<Long> sgIdCaptor;

	@Captor
	ArgumentCaptor<StrategyGroupHeadCount> sgHeadCountCaptor;

	@Captor
	ArgumentCaptor<BenefitGroupStrategy> bgStrategyCaptor;

	@Captor
	ArgumentCaptor<Company> companyCaptor;

    private MockedStatic<StrategyUtils> mockStaticStrategyUtils;

    private MockedStatic<BenefitGroupServiceHelper> mockStaticBenefitGroupServiceHelper;

    private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;

    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;

    @After
    public void tearDown() {
        if (mockStaticStrategyUtils != null) {
            mockStaticStrategyUtils.close();
        }
        if (mockStaticBenefitGroupServiceHelper != null) {
            mockStaticBenefitGroupServiceHelper.close();
        }
        if (mockStaticRulesAndConfigsUtils != null) {
            mockStaticRulesAndConfigsUtils.close();
        }
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
        }
    }

    @Before
	public void setUp() {
        mockStaticStrategyUtils = Mockito.mockStatic(StrategyUtils.class);
        mockStaticBenefitGroupServiceHelper = Mockito.mockStatic(BenefitGroupServiceHelper.class);
        mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);
        mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);

		company = new Company();
		companyId = 4444;
		company.setId(companyId);
		realmPlanYearId = 21;
		company.setRealmPlanYearId(realmPlanYearId);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(realmPlanYearId);
		company.setRealmPlanYear(rpy);
		headQtrState = "FL";
		company.setHeadQuatersState(headQtrState);
		industryType = IndustryType.FS;
		Industry industry = new Industry(2);
		industry.setIndustryType(industryType);
		company.setIndustry(industry);
		Realm realm = new Realm();
		realm.setBenExchange(Constants.PASSPORT_BEN_EXCHANGE);
		company.setRealm(realm);
		strategyGroupId = 1111;
		groupId = 2222;
		strategyId = 3333;
		strategyPkgType = null;
		planSelectionId1 = 11111;
		planSelectionId2 = 22222;
		planSelectionId3 = 33333;

	}

	@Test
	public void updateGroupHeadCount() {
		List<HeadCountData> headCountList = prepareHeadCountList();
		Map<String, List<BenefitPlanRate>> planRates = Collections.<String, List<BenefitPlanRate>>emptyMap();

		when(planRatesService.getBenefitPlanRatesBy(company)).thenReturn(planRates);
		when(employerEmployeePlansMappingDao.getEeAndErPlanMapping(company.getRealmPlanYear().getId()))
				.thenReturn(prepareEeErPlanMapping());
		when(strategyGroupService.getBenefitGroupStrategyBy(strategyGroupId)).thenReturn(prepareBenGrpStrategy());
		when(strategyDao.findByIdAndCompanyIdAndStatus(strategyId, companyId, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(prepareStrategy());

		when(templateFundingDao.getTemplateHeadCountPlans(company,strategyPkgType)).thenReturn(prepareHeadCountPlans());

		Mockito.when(StrategyUtils.getPlanCost(planRates.get(MEDICAL_BEN_PLAN)))
				.thenReturn(prepareMedicalPlanCost());
		Mockito.when(StrategyUtils.getPlanCost(planRates.get(DENTAL_BEN_PLAN)))
				.thenReturn(prepareDentalPlanCost());
		Mockito.when(StrategyUtils.getPlanCost(planRates.get(VISION_BEN_PLAN)))
				.thenReturn(prepareVisionPlanCost());

		when(benefitGroupService.getAllBenefitGroups(company.getId(), Constants.ACTIVE_STATUS))
				.thenReturn(prepareBenGroups());

		Mockito.when(BenefitGroupServiceHelper.prepareStrategyGroupHeadCountObj(headCountCaptor.capture(),
				sgIdCaptor.capture())).thenReturn(prepareStrategyGrpHeadCount());

		when(strategyGroupHeadCountService.saveStrategyGroupHeadCount(sgHeadCountCaptor.capture())).thenReturn(null);
		when(strategyGroupService.saveBenefitGroupStrategy(bgStrategyCaptor.capture())).thenReturn(null);
		when(companyService.createUpdateCompany(companyCaptor.capture())).thenReturn(null);
		when( RulesAndConfigsUtils.findPickChooseWithExceptions( any( Company.class )) ).thenReturn( false );
		when(exchangeService.isMedicalOffered(anyLong())).thenReturn(true);

		benGroupHeadCountService.updateGroupHeadCount(company, headCountList);

		verify(strategyGroupHeadCountService, times(4)).saveStrategyGroupHeadCount(any(StrategyGroupHeadCount.class));
		verify(strategyGroupService, times(1)).saveBenefitGroupStrategy(any(BenefitGroupStrategy.class));
		verify(companyService, times(1)).createUpdateCompany(any(Company.class));

		assertEquals(4, sgHeadCountCaptor.getAllValues().size());
		for (StrategyGroupHeadCount sgHeadCount : sgHeadCountCaptor.getAllValues()) {
			if ("1".equals(sgHeadCount.getId().getCovrgCd())) {
				assertEquals(strategyGroupId, sgHeadCount.getId().getStrategyGroupId());
				assertEquals(4, sgHeadCount.getHeadcount());
			} else if ("2".equals(sgHeadCount.getId().getCovrgCd())) {
				assertEquals(strategyGroupId, sgHeadCount.getId().getStrategyGroupId());
				assertEquals(2, sgHeadCount.getHeadcount());
			} else if ("C".equals(sgHeadCount.getId().getCovrgCd())) {
				assertEquals(strategyGroupId, sgHeadCount.getId().getStrategyGroupId());
				assertEquals(4, sgHeadCount.getHeadcount());
			} else if ("4".equals(sgHeadCount.getId().getCovrgCd())) {
				assertEquals(strategyGroupId, sgHeadCount.getId().getStrategyGroupId());
				assertEquals(0, sgHeadCount.getHeadcount());
			}
		}

		assertEquals(4444, companyCaptor.getValue().getId());
		assertEquals(13, companyCaptor.getValue().getHeadcount());
	}

	@Test
	public void testSetPlanRatesOnContributions() throws Exception {
		// 1. Setup
		String benefitPlan = "testPlan";
		PlanSelection ps = new PlanSelection();
		ps.setId(1L);
		ps.setBenefitPlan(benefitPlan);
		ps.setPlanType("MEDICAL"); // Not an additional plan type

		List<Contribution> contributions = new ArrayList<>();
		Contribution c1 = new Contribution();
		c1.setCoverageLevel("1");
		contributions.add(c1);
		Contribution c2 = new Contribution();
		c2.setCoverageLevel("2");
		contributions.add(c2);

		when(contributionService.getContributions(1L)).thenReturn(contributions);

		Map<String, List<BenefitPlanRate>> planRates = new HashMap<>();
		planRates.put(benefitPlan, Collections.emptyList());

		Map<String, BigDecimal> costMapV2 = new HashMap<>();
		costMapV2.put("employee", new BigDecimal("100.00"));
		costMapV2.put("employeePlusSpouse", new BigDecimal("200.00"));

		Map<String, XbssRealmPlyrPlan> plyrPlanMap = new HashMap<>();

		Method method = BenefitGroupHeadCountServiceImpl.class.getDeclaredMethod("setPlanRatesOnContributions",
				Company.class, List.class, Map.class, Map.class);
		method.setAccessible(true);

		// 2. Test V2 enabled branch
		mockStaticStrategyUtils.when(() -> StrategyUtils.getPlanCost(planRates.get(benefitPlan))).thenReturn(costMapV2);

		method.invoke(benGroupHeadCountService, company, Collections.singletonList(ps), planRates, plyrPlanMap);

		assertEquals(new BigDecimal("100.00"), contributions.get(0).getPlanCost());
		assertEquals(new BigDecimal("200.00"), contributions.get(1).getPlanCost());

		// Reset plan costs before re-running
		contributions.get(0).setPlanCost(null);
		contributions.get(1).setPlanCost(null);

		method.invoke(benGroupHeadCountService, company, Collections.singletonList(ps), planRates, plyrPlanMap);

		assertEquals(new BigDecimal("100.00"), contributions.get(0).getPlanCost());
		assertEquals(new BigDecimal("200.00"), contributions.get(1).getPlanCost());

		// 3. Verify interactions
		verify(contributionService, times(2)).getContributions(1L);
	}

	private List<HeadCountData> prepareHeadCountList() {
		List<HeadCountData> headCountDataList = new ArrayList<>();
		Map<String, Integer> covrgHeadCountMap = new HashMap<>();
		covrgHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), 2);
		covrgHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), 1);
		covrgHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), 0);
		covrgHeadCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), 4);

		HeadCountData headCountData = new HeadCountData();
		headCountData.setStrategyGroupId(strategyGroupId);
		headCountData.setCovrgHeadCountMap(covrgHeadCountMap);
		headCountDataList.add(headCountData);
		return headCountDataList;
	}

	private Map<String, String> prepareEeErPlanMapping() {
		Map<String, String> map = new HashMap<>();
		map.put("0038Q4", "0038Q9");
		map.put("0038Q5", "0038Q7");
		return map;
	}

	private BenefitGroupStrategy prepareBenGrpStrategy() {
		BenefitGroupStrategy benGrpStrategy = new BenefitGroupStrategy();
		benGrpStrategy.setId(strategyGroupId);
		benGrpStrategy.setGroupId(groupId);
		benGrpStrategy.setStrategyId(strategyId);
		return benGrpStrategy;
	}

	private Strategy prepareStrategy() {
		Strategy strategy = new Strategy();
		strategy.setId(strategyId);
		strategy.setPkgType(strategyPkgType);
		return strategy;
	}

	private List<PlanSelection> preparePlanList() {
		List<PlanSelection> planList = new ArrayList<>();
		PlanSelection ps = new PlanSelection();
		ps.setId(planSelectionId1);
		ps.setPlanType(Constants.MEDICAL_CODE);
		ps.setBenefitPlan(MEDICAL_BEN_PLAN);
		planList.add(ps);
		ps = new PlanSelection();
		ps.setId(planSelectionId2);
		ps.setPlanType(Constants.DENTAL_CODE);
		ps.setBenefitPlan(DENTAL_BEN_PLAN);
		planList.add(ps);
		ps = new PlanSelection();
		ps.setId(planSelectionId3);
		ps.setPlanType(Constants.VISION_CODE);
		ps.setBenefitPlan(VISION_BEN_PLAN);
		planList.add(ps);
		ps = new PlanSelection();
		ps.setPlanType("20");
		planList.add(ps);
		return planList;
	}

	private Map<String, List<String>> prepareHeadCountPlans() {
		Map<String, List<String>> headCountPlans = new HashMap<>();
		headCountPlans.put("11", Arrays.asList(MEDICAL_BEN_PLAN));
		headCountPlans.put("14", Arrays.asList(DENTAL_BEN_PLAN));
		headCountPlans.put("10", Arrays.asList(VISION_BEN_PLAN));
		return headCountPlans;
	}

	private List<Contribution> prepareContributionList() {
		List<Contribution> list = new ArrayList<>();
		Contribution cont = new Contribution();
		cont.setCoverageLevel("1");
		cont.setPlanCost(BigDecimal.valueOf(100.00));
		list.add(cont);
		cont = new Contribution();
		cont.setCoverageLevel("2");
		cont.setPlanCost(BigDecimal.valueOf(200.00));
		list.add(cont);
		cont = new Contribution();
		cont.setCoverageLevel("C");
		cont.setPlanCost(BigDecimal.valueOf(150.00));
		list.add(cont);
		cont = new Contribution();
		cont.setCoverageLevel("4");
		cont.setPlanCost(BigDecimal.valueOf(120.00));
		list.add(cont);
		return list;
	}

	private Map<String, String> prepareVendorsMap() {
		Map<String, String> map = new HashMap<>();
		map.put(MEDICAL_BEN_PLAN, "vendor1");
		map.put(DENTAL_BEN_PLAN, "vendor2");
		map.put(VISION_BEN_PLAN, "vendor3");
		return map;
	}

	private Map<String, BigDecimal> prepareVisionPlanCost() {
		Map<String, BigDecimal> planCost = new HashMap<>();
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(120.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(150.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(220.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(320.00));
		return planCost;
	}

	private Map<String, BigDecimal> prepareDentalPlanCost() {
		Map<String, BigDecimal> planCost = new HashMap<>();
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(220.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(350.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(420.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(520.00));
		return planCost;
	}

	private Map<String, BigDecimal> prepareMedicalPlanCost() {
		Map<String, BigDecimal> planCost = new HashMap<>();
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(320.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(350.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(420.00));
		planCost.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(520.00));
		return planCost;
	}

	private Map<String, Map<String, Integer>> preparePlanHeadCountMap() {
		Map<String, Map<String, Integer>> planHeadCountMap = new HashMap<>();
		Map<String, Integer> headCountMap = new HashMap<>();
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), 2);
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), 1);
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), 4);
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), 0);
		planHeadCountMap.put(MEDICAL_BEN_PLAN, headCountMap);
		headCountMap = new HashMap<>();
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), 1);
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), 4);
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), 0);
		headCountMap.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), 2);
		planHeadCountMap.put(VISION_BEN_PLAN, headCountMap);
		return planHeadCountMap;
	}

	private List<BenefitGroup> prepareBenGroups() {
		List<BenefitGroup> list = new ArrayList<>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setHeadcount(5);
		list.add(benGrp);
		benGrp = new BenefitGroup();
		benGrp.setHeadcount(8);
		list.add(benGrp);
		return list;
	}

	private List<StrategyGroupHeadCount> prepareStrategyGrpHeadCount() {
		List<StrategyGroupHeadCount> strategyGroupHeadCounts = new ArrayList<>();
		StrategyGroupHeadCount sghc = new StrategyGroupHeadCount();
		StrategyGroupHeadCountId sghcId = new StrategyGroupHeadCountId();
		sghcId.setCovrgCd("1");
		sghcId.setStrategyGroupId(strategyGroupId);
		sghc.setId(sghcId);
		sghc.setHeadcount(4);
		strategyGroupHeadCounts.add(sghc);
		sghc = new StrategyGroupHeadCount();
		sghcId = new StrategyGroupHeadCountId();
		sghcId.setCovrgCd("2");
		sghcId.setStrategyGroupId(strategyGroupId);
		sghc.setId(sghcId);
		sghc.setHeadcount(2);
		strategyGroupHeadCounts.add(sghc);
		sghc = new StrategyGroupHeadCount();
		sghcId = new StrategyGroupHeadCountId();
		sghcId.setCovrgCd("C");
		sghcId.setStrategyGroupId(strategyGroupId);
		sghc.setId(sghcId);
		sghc.setHeadcount(4);
		strategyGroupHeadCounts.add(sghc);
		sghc = new StrategyGroupHeadCount();
		sghcId = new StrategyGroupHeadCountId();
		sghcId.setCovrgCd("4");
		sghcId.setStrategyGroupId(strategyGroupId);
		sghc.setId(sghcId);
		sghc.setHeadcount(0);
		strategyGroupHeadCounts.add(sghc);
		return strategyGroupHeadCounts;
	}
}

