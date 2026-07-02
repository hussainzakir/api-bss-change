package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.HeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.HeadCountServiceImpl;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.util.RulesAndConfigsUtils;

@RunWith(MockitoJUnitRunner.class)
public class HeadCountServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	HeadCountServiceImpl headCountService;

	@Mock
	HeadCountDao headCountDao;

	@Mock
	EmployeeSelectionDao employeeSelectionDao;

	@Mock
	StrategyGroupDataDao strategyGroupDataDao;

	@Mock
	RenewalDataDao renewalDataDao;

	@Mock
	PortfolioHeadCountDataDao portfolioHeadCountDataDao;

	@Mock
	StrategyDataDao strategyDataDao;

	RealmPlanYear realmPlanYear;
	Company company;
	long strategyId;
	long realmYrId;

    private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;

	@Before
	public void setup() {
        mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);

		realmYrId = 21;

		realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(realmYrId);
		realmPlanYear.setId(realmYrId);

		company = new Company();
		company.setCode("CODE");
		company.setRealmPlanYear(realmPlanYear);
		company.setPlanStartDate("01-JAN-2020");
		company.setExclusiveMedPlan("DFLT");
		strategyId = 0L;
	}

    @After
    public void tearDown() {
        if (mockStaticRulesAndConfigsUtils != null) {
            mockStaticRulesAndConfigsUtils.close();
        }
    }

	@Test
	public void getEmployeeHeadcountByBenefitGroup() {
		when(headCountDao.getEmployeeCountByBenefitGroup(Mockito.anyString(), Mockito.anyLong()))
				.thenReturn(prepareEmployeeCountMockMap());
		when(employeeSelectionDao.getEmployeesByBG(Mockito.any(Company.class), Mockito.any(Date.class)))
				.thenReturn(prepareEmployeeCountMockMap());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(true);
		Map<String, Integer> actualResult = headCountService.getEmployeeHeadcountByBenefitGroup(company, realmYrId,
				new Date());
		assertEquals(3, actualResult.size());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(false);
		actualResult = headCountService.getEmployeeHeadcountByBenefitGroup(company, realmYrId, new Date());
		assertEquals(3, actualResult.size());
	}

	@Test
	public void getHeadCountByGroupAndPlan() {
		when(headCountDao.getPlanCoverageLevelHeadCountByGroup(Mockito.anyString(), Mockito.anyLong(), Mockito.anyBoolean()))
				.thenReturn(prepareCoverageLevelMockMap());
		when(renewalDataDao.getHeadCountByGroupAndPlan(Mockito.anyString(), Mockito.any(Date.class)))
				.thenReturn(prepareCoverageLevelMockMap());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(true);
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> actualResult = headCountService
				.getHeadCountByGroupAndPlan(company, realmYrId, new Date(), false);
		assertEquals(1, actualResult.size());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(false);
		actualResult = headCountService.getHeadCountByGroupAndPlan(company, realmYrId, new Date(), false);
		assertEquals(1, actualResult.size());
	}

	@Test
	public void getMirrorPlanHeadCounts() {
		when(portfolioHeadCountDataDao.getMirrorPlanHeadCounts(Mockito.anyString(), Mockito.anyLong()))
				.thenReturn(prepareMirrorCountMockMap());

		Map<String, Map<String, Map<String, Long>>> actualResult = headCountService.getMirrorPlanHeadCounts(company,
				realmYrId);
		assertEquals(1, actualResult.size());

	}

	@Test
	public void getWaiverHeadCountByBenefitProgram() {

		boolean history = true;
		when(headCountDao.getWaiverHeadCountByBenefitProgram( Mockito.any(Company.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(prepareWaiverCountMockMap());
		when(strategyGroupDataDao.getStrategyWaiverHeadCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.anyBoolean())).thenReturn(prepareWaiverCountMockMap());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(true);
		Map<String, Long> actualResult = headCountService.getWaiverHeadCountByBenefitProgram(company, strategyId,
				realmYrId, history);
		assertEquals(3, actualResult.size());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(false);
		actualResult = headCountService.getWaiverHeadCountByBenefitProgram(company, strategyId, realmYrId, history);
		assertEquals(3, actualResult.size());
	}

	@Test
	public void getPrimaryHeadCountByBenefitProgram() {

		boolean history = true;
		when(headCountDao.geEnrolledHeadCountByBenefitProgram( Mockito.any(Company.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(prepareEmployeeCountMockMap());
		when(renewalDataDao.getPrimaryEnrolledEECount(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.anyLong())).thenReturn(prepareEmployeeCountMockMap());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(true);
		Map<String, Integer> actualResult = headCountService.getPrimaryHeadCountByBenefitProgram(company, strategyId,
				history);
		assertEquals(3, actualResult.size());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(false);
		actualResult = headCountService.getPrimaryHeadCountByBenefitProgram(company, strategyId, history);
		assertEquals(3, actualResult.size());
	}

	@Test
	public void getStrategyBenefitGroupHeadCount() {

		when(strategyDataDao.getStrategyBenefitGroupHeadCountsFromCensus(Mockito.anyLong())).thenReturn(prepareStrategyBenefitGroupHeadCountMockMap());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(true);
		Map<Long, Long> actualResult = headCountService.getStrategyBenefitGroupHeadCount(company, strategyId);
		assertEquals(3, actualResult.size());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(false);
		actualResult = headCountService.getStrategyBenefitGroupHeadCount(company, strategyId);
		assertEquals(3, actualResult.size());
	}

	@Test
	public void getEligibleEmployeeCount() {
		boolean history = true;
		when(headCountDao.getEligibleEmployeeCount(Mockito.any(Company.class), Mockito.anyLong(),
				Mockito.anyBoolean())).thenReturn(prepareEligibleHeadCount());
		when(renewalDataDao.getActiveEligibleEECount(Mockito.any(Company.class), Mockito.anyBoolean(),
				Mockito.anyLong(), Mockito.any(RealmPlanYear.class))).thenReturn(prepareEligibleHeadCount());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(true);
		Map<String, ActiveEligibleEECount> actualResult = headCountService.getEligibleEmployeeCount(company, strategyId,
				realmPlanYear, history);
		assertEquals(1, actualResult.size());

		when(RulesAndConfigsUtils.isVendorMappingOn(realmYrId)).thenReturn(false);
		actualResult = headCountService.getEligibleEmployeeCount(company, strategyId, realmPlanYear, history);
		assertEquals(1, actualResult.size());
	}

	private Map<String, Integer> prepareEmployeeCountMockMap() {
		Map<String, Integer> groupHeadCountMap = new HashMap<>();
		groupHeadCountMap.put("PROGRAM_1", 21);
		groupHeadCountMap.put("PROGRAM_2", 5);
		groupHeadCountMap.put("PROGRAM_3", 7);
		return groupHeadCountMap;
	}

	private Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> prepareCoverageLevelMockMap() {
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupCovrgHeadCountMap = new HashMap<>();
		Map<String, List<PlanCoverageLevelHeadCount>> planMap = new HashMap<>();

		String benefitProgram = "PROGRAM_1";
		String benefitPlan = "MEDICAL_PLAN_1";

		List<PlanCoverageLevelHeadCount> covrgHeadCountList = new ArrayList<>();

		PlanCoverageLevelHeadCount covrgHeadCount = new PlanCoverageLevelHeadCount();
		covrgHeadCount.setGroupName(benefitProgram);
		covrgHeadCount.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		covrgHeadCount.setBenefitPlan(benefitPlan);
		covrgHeadCount.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE.getCode());
		covrgHeadCount.setHeadCount(10);
		covrgHeadCount.setHsaHeadCount(1);

		covrgHeadCountList.add(covrgHeadCount);
		covrgHeadCount = new PlanCoverageLevelHeadCount();
		covrgHeadCount.setGroupName(benefitProgram);
		covrgHeadCount.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		covrgHeadCount.setBenefitPlan(benefitPlan);
		covrgHeadCount.setCovrgCode(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
		covrgHeadCount.setHeadCount(10);
		covrgHeadCount.setHsaHeadCount(1);
		covrgHeadCountList.add(covrgHeadCount);

		planMap.put(benefitPlan, covrgHeadCountList);
		groupCovrgHeadCountMap.put(benefitProgram, planMap);

		return groupCovrgHeadCountMap;
	}

	private Map<String, Map<String, Map<String, Long>>> prepareMirrorCountMockMap() {
		String benefitProgram = "PROGRAM_1";
		String benefitPlan = "MEDICAL_PLAN_1";
		Map<String, Map<String, Map<String, Long>>> returnMap = new HashMap<>();
		Map<String, Map<String, Long>> planMap = new HashMap<>();
		Map<String, Long> coverageLevelMap = new HashMap<>();
		coverageLevelMap.put(CoverageCodesEnums.COV_EMPLOYEE.getCode(), 2L);
		planMap.put(benefitPlan, coverageLevelMap);
		returnMap.put(benefitProgram, planMap);
		return returnMap;
	}

	private Map<String, Long> prepareWaiverCountMockMap() {
		Map<String, Long> groupHeadCountMap = new HashMap<>();
		groupHeadCountMap.put("PROGRAM_1", 21L);
		groupHeadCountMap.put("PROGRAM_2", 5L);
		groupHeadCountMap.put("PROGRAM_3", 7L);
		return groupHeadCountMap;
	}

	private Map<Long, Long> prepareStrategyBenefitGroupHeadCountMockMap() {
		Map<Long, Long> headCountMap = new HashMap<>();
		headCountMap.put(1L, 21L);
		headCountMap.put(2L, 5L);
		headCountMap.put(3L, 7L);
		return headCountMap;
	}

	private Map<String, ActiveEligibleEECount> prepareEligibleHeadCount() {
		Map<String, ActiveEligibleEECount> returnMap = new HashMap<>();
		ActiveEligibleEECount eeCount = new ActiveEligibleEECount();
		eeCount.setBenProg("PROGRAM_1");
		eeCount.setPrimaryHeadCount(2);
		eeCount.setSecondaryHeadCount(4);
		eeCount.setTotalHeadCount(6);
		returnMap.put("PROGRAM_1", eeCount);
		return returnMap;
	}

}
