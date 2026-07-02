/**
 * 
 */
package com.trinet.ambis.persistence.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.util.StrategyUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.collections.map.MultiKeyMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.impl.StrategyDataDaoImpl;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.EmployeeBenefitGroup;
import com.trinet.ambis.service.model.ModelCompareGroupHeadcount;
import com.trinet.ambis.service.model.ModelComparePlanTypeCost;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.service.model.StrategyBenefitPlanHeadCount;
import com.trinet.ambis.service.model.StrategyEstimate;
import com.trinet.ambis.service.model.StrategyGroupPlanRateData;
import com.trinet.ambis.service.model.StrategySubmitIssueReport;
import com.trinet.ambis.service.unit.ServiceUnitTest;


/**
 * @author rvutukuri
 *
 */
@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class StrategyDataDaoImplTest extends ServiceUnitTest {

	StrategyDataDaoImpl strategyDataDao = new StrategyDataDaoImpl();
	EntityManager entityManager = null;
	EmployeeBenefitGroupDao employeeBenefitGroupDao = null;
	Query mockedQuery = null;
	Query mockedQuery1 = null;
	Query mockedQuery2 = null;
	Query mockedQuery3 = null;

	Long strategy1 = 37318L;
	Long strategy2 = 37319L;

	Long group1 = 1000L;

	List<Long> strategyIds = Arrays.asList(strategy1, strategy2);
	Set<Long> strategyIds1 = new HashSet(List.of(11111));
	Set<Long> groupIds1 = new HashSet<>(List.of(2000L, 2001L));


    private MockedStatic<StrategyUtils> strategyUtilsMockedStatic;
    private MockedStatic<StrategyServiceHelper> strategyServiceHelperMockedStatic;

    @After
    public void tearDown() {
        if(strategyUtilsMockedStatic != null)
        strategyUtilsMockedStatic.close();
        if(strategyServiceHelperMockedStatic != null)
        strategyServiceHelperMockedStatic.close();
    }

	@Before
	public void setup() {
        strategyUtilsMockedStatic = org.mockito.Mockito.mockStatic(StrategyUtils.class);
        strategyServiceHelperMockedStatic = org.mockito.Mockito.mockStatic(StrategyServiceHelper.class);
		
		entityManager = mock(EntityManager.class);
		mockedQuery = mock(Query.class);
		mockedQuery1 = mock(Query.class);
		mockedQuery2 = mock(Query.class);
		mockedQuery3 = mock(Query.class);
		employeeBenefitGroupDao = mock(EmployeeBenefitGroupDao.class);
		strategyDataDao.setEntityManager(entityManager);
		strategyDataDao.setEm(entityManager);
		strategyDataDao.setEmployeeBenefitGroupDao(employeeBenefitGroupDao);
		when(mockedQuery.setParameter(anyString(), Mockito.any())).thenReturn(mockedQuery);
		when(strategyDataDao.getEntityManager().createNamedQuery(anyString())).thenReturn(mockedQuery);
		when(strategyDataDao.getEm().createNamedQuery(anyString())).thenReturn(mockedQuery);
	}

	@Test
	public void getPlansSelectionsByCompany() {
		when(mockedQuery.getResultList()).thenReturn(preparePlanSelectionMockData());

		String companyCode = "G48";
		Long realmPlanYearId = 10L;
		String effDate = "01-JAN-2021";

		Map<Long, Map<String, Map<Long, List<PlanSelection>>>> actualResult = strategyDataDao
				.getPlansSelectionsByCompany(companyCode, realmPlanYearId, effDate);

		assertEquals(1, actualResult.size());
		assertEquals(3, actualResult.get(42610L).size());
	}
	
	@Test
	public void deleteStrategyEstimateList() {
		strategyDataDao.deleteStrategyEstimateList(new HashSet<>(Arrays.asList(1111L, 2222L)));

		verify(mockedQuery, times(1)).executeUpdate();
	}
	
	@Test
	public void deleteStrategyEstimateByStrategyGroup() {
		strategyDataDao.deleteStrategyEstimateByStrategyGroup(1111L, 2222L);

		verify(mockedQuery, times(1)).executeUpdate();
	}
	
	@Test
	public void getByPlanSelectionId() {
		List<Long> ids = new ArrayList<>();
		Map<String, List<BenefitPlanRate>> planRates = new HashMap<>();
		boolean contributionRequired = true;

		when(mockedQuery.getResultList()).thenReturn(preparePlanSelectionIdMockData());
		Map<Long, List<PlanContribution>> actualResult = strategyDataDao.getByPlanSelectionId(ids, planRates,
				contributionRequired);
		assertEquals(5, actualResult.get(293903L).get(0).getHeadcount());
		assertEquals(BigDecimal.valueOf(1200), actualResult.get(293903L).get(0).getEmployeeContribution());
		assertEquals(BigDecimal.valueOf(2411.81),
				actualResult.get(293903L).get(0).getEmployerContribution().setScale(2, RoundingMode.CEILING));

		contributionRequired = false;

		when(mockedQuery.getResultList()).thenReturn(preparePlanSelectionIdMockData());
		actualResult = strategyDataDao.getByPlanSelectionId(ids, planRates, contributionRequired);
		assertEquals(5, actualResult.get(293903L).get(0).getHeadcount());
		assertEquals(4, actualResult.get(293903L).get(0).getHsaHeadcount());
		assertNull(actualResult.get(293903L).get(0).getEmployeeContribution());
		assertNull(actualResult.get(293903L).get(0).getEmployerContribution());
	}

	@Test
	public void deleteAllPlanContributionsByBenefitgroupAndStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteAllPlanContributionsByBenefitgroupAndStrategy(11111, 22222);
		assertEquals(1, actualResult);
	}

	@Test
	public void deleteAllPlanSelectionsByBenefitgroupAndStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteAllPlanSelectionsByBenefitgroupAndStrategy(11111, 2222);
		assertEquals(1, actualResult);
	}

	@Test
	public void deleteStrategyFundingsByBenefitgroupAndStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteStrategyFundingsByBenefitgroupAndStrategy(11111, 2222);
		assertEquals(1, actualResult);
	}

	@Test
	public void deleteAllPlanContributionsByStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteAllPlanContributionsByStrategy(strategyIds1);
		assertEquals(1, actualResult);
	}

	@Test
	public void deleteAllPlanSelectionsByStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteAllPlanSelectionsByStrategy(strategyIds1);
		assertEquals(1, actualResult);
	}

	@Test
	public void deleteStrategyFundDetailByStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteStrategyFundDetailByStrategy(strategyIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteStrategyFlatMaxByStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteStrategyFlatMaxByStrategy(strategyIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteStrategyFundModelByStrategy() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteStrategyFundModelByStrategy(strategyIds1);
		assertEquals(1, actualResult);
	}
	
	@Test
	public void deleteStrategyById() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteStrategyById(strategyIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteEmployees() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteEmployees("G48", 10);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteGroupByCompanyId() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteGroupByCompanyId(11111);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteGroupCovHeadCount() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteGroupCovHeadCount(11111);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteStrategyGroup() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteStrategyGroup(strategyIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteEmployeeStrategyGroup() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteEmployeeStrategyGroup(strategyIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteStrategyGroupCovHeadCount() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteStrategyGroupCovHeadCount(strategyIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteGroupRate() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteGroupRate(1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteGroupCovHeadCountByGroupIds() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteGroupCovHeadCountByGroupIds(groupIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteGroupRateByGroupIds() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteGroupRateByGroupIds(groupIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void deleteGroupByIds() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.deleteGroupByIds(groupIds1);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getPlanPackagesByStrategyIdAndBenefitGroupIdTest() {
		List<Object[]> results = createPlanPackages();
		when(mockedQuery.getResultList()).thenReturn(results);
		Map<String, PlanPackage> pk = strategyDataDao.getPlanPackagesByStrategyIdAndBenefitGroupId(26761, 26206, 2,
				false);
		assertEquals("001EKV", pk.get("medical").getFundingBasePlan());
	}

	@Test
	public void getAdditionalBenefitPlanEstCost() {
		when(strategyDataDao.getEm().createNamedQuery("getRealmClonePgmEffdt")).thenReturn(mockedQuery1);
		when(strategyDataDao.getEm().createNamedQuery("LIFE_DISABILITY_COVERAGE")).thenReturn(mockedQuery2);
		when(strategyDataDao.getEm().createNamedQuery("getAdditionalPlansEstCost")).thenReturn(mockedQuery3);

		when(mockedQuery1.getResultList()).thenReturn(prepareRealmClonePgmMockData());
		when(mockedQuery2.getResultList()).thenReturn(prepareLifeDisabilityCvrgMockData());
		when(mockedQuery3.getResultList()).thenReturn(prepareAddtnlPlansEstCostMockData());

		Map<String, BigDecimal> actualResult = strategyDataDao.getAdditionalBenefitPlanEstCost(10);

		assertEquals(2, actualResult.size());
		assertEquals(BigDecimal.valueOf(450000), actualResult.get("000SRP"));
		assertEquals(BigDecimal.valueOf(5.85), actualResult.get("000W46"));
	}

	@Test
	public void getPlanTypeDescriptions() {
		when(mockedQuery.getResultList()).thenReturn(preparePlanTypeDescriptionsMockData());
		Map<String, PlanTypeDescription> actualResult = strategyDataDao.getPlanTypeDescriptions(10);
		assertEquals(7, actualResult.size());

	}

	@Test
	public void getStrategiesHistoryCount() {
		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));
		int actualResult = strategyDataDao.getStrategiesHistoryCount("G48", 10);
		assertEquals(1, actualResult);
	}

	@Test
	public void updateStrategySubmitFlag() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.updateStrategySubmitFlag(1111);
		assertEquals(1, actualResult);
	}

	@Test
	public void getHistoryStrategies() {
		when(mockedQuery.getResultList()).thenReturn(prepareHistoryStrategiesMockData());
		List<Strategy> actualResult = strategyDataDao.getHistoryStrategies("G48", 10);
		assertEquals(1, actualResult.size());

	}

	@Test
	public void getCurrentStrategy() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategiesMockData());
		Strategy actualResult = strategyDataDao.getCurrentStrategy("G48", 9609L);
		assertEquals("Plan with STD ~ 100% employee coverage", actualResult.getName());
		assertEquals(9609, actualResult.getCompanyId());
	}

	@Test
	public void getFutureStrategies() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategiesMockData());
		List<Strategy> actualResult = strategyDataDao.getFutureStrategies("G48", 9609L);
		assertEquals(1, actualResult.size());
		assertEquals("Plan with STD ~ 100% employee coverage", actualResult.get(0).getName());
		assertEquals(9609, actualResult.get(0).getCompanyId());
	}

	@Test
	public void getRealmPlanTypes() {
		List<String> mockData = new ArrayList<>();
		mockData.add("10");
		mockData.add("11");
		when(mockedQuery.getResultList()).thenReturn(mockData);
		Set<String> actualResult = strategyDataDao.getRealmPlanTypes(10);
		assertEquals(2, actualResult.size());
	}


	// TODO fix this test
	@Test
    @Ignore
	public void getPlanRatesForRateCharts() {
		Company company = new Company();
		company.setRealmPlanYearId(10L);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setPlanYearEnd(new java.util.Date());
		company.setRealmPlanYear(rpy);
		Realm realm = new Realm();
		company.setRealm(realm);
		BandCodes bandCodes = new BandCodes();
		company.setBandCodes(bandCodes);

		Map<String, Set<PlanCarrier>> planCarrierMap = new HashMap<>();
		Set<PlanCarrier> planCarriers = new HashSet<>();
		planCarrierMap.put("medical", planCarriers);
		planCarrierMap.put("dental", planCarriers);
		planCarrierMap.put("vision", planCarriers);

		when(entityManager.createNamedQuery("REALM_ALL_RATES_PASSPORT")).thenReturn(mockedQuery1);

		when(StrategyServiceHelper.getLocations(Mockito.any(Company.class))).thenReturn(new HashSet<>());

	}

	@Test
	public void getOverridesByBenefitGroup() {
		when(mockedQuery.getResultList()).thenReturn(preparePlanOverridesMockData());

		Map<String, Map<String, Map<String, String>>> actualResult = strategyDataDao.getOverridesByBenefitGroup("G48",
				10L, false);

		assertEquals(2, actualResult.size());
		assertEquals(2, actualResult.get("001RS3").size());
		assertEquals(1, actualResult.get("EF1").size());
	}

	@Test
	public void insertStrategyEstimate() {
		Map<Long, List<StrategyEstimate>> strategyEstimateMap = new HashMap<>();
		List<StrategyEstimate> strategyEstimates = new ArrayList<>();
		StrategyEstimate se = new StrategyEstimate();
		strategyEstimates.add(se);
		strategyEstimateMap.put(1111L, strategyEstimates);
		when(mockedQuery.executeUpdate()).thenReturn(1);
		strategyDataDao.insertStrategyEstimate(strategyEstimateMap);
		verify(mockedQuery, times(1)).executeUpdate();
	}

	@Test
	public void getModelCompareStrategies() {
		when(mockedQuery.getResultList()).thenReturn(prepareMCStrategiesMockData());

		List<ModelCompareStrategy> actualResult = strategyDataDao.getModelCompareStrategies(1111L);

		assertEquals(2, actualResult.size());
	}

	@Test
	public void getStrategiesCost() {
		List<Long> strategyIdList = new ArrayList<>();
		strategyIdList.add(29346L);
		strategyIdList.add(49480L);
		strategyIdList.add(49481L);
		when(mockedQuery.getResultList()).thenReturn(prepareMCStrategiesCostMockData());

		Map<Long, List<ModelComparePlanTypeCost>> actualResult = strategyDataDao.getStrategiesCost(strategyIdList);

		assertEquals(3, actualResult.size());
	}

	@Test
	public void getGroupsByStrategyTest() {
		List<Object[]> results = createStrategyBenGroups(); // mocked query data
		when(mockedQuery.getResultList()).thenReturn(results);

		Map<Long, List<BenefitGroup>> strategyBenGrpList = strategyDataDao.getGroupsByStrategy(strategyIds);

		assertEquals(2, strategyBenGrpList.keySet().size()); // there are 2
																// strategies in
																// mock data

		List<BenefitGroup> bg = strategyBenGrpList.get(strategy1);
		assertEquals(2, bg.size()); // 1st strategy in mock query data has 2
									// benefit groups
		assertEquals("Group1", bg.get(0).getName());
		assertEquals("Group2", bg.get(1).getName());

		bg = strategyBenGrpList.get(strategy2);
		assertEquals(1, bg.size()); // 2nd strategy in mock query data has 1
									// benefit group
		assertEquals("Group3", bg.get(0).getName());
	}

	@Test
	public void getOfferedPlanTypesByStrategy() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyGrpPlanTypeMockData());

		Map<String, List<String>> actualResult = strategyDataDao.getOfferedPlanTypesByStrategy("18332");

		assertEquals(1, actualResult.size());
	}
	
	@Test
	public void getPlanContributionsByStrategyId() {
		Company company = new Company();
		long strategyId = 47401;
		when(mockedQuery.getResultList()).thenReturn(preparePlanContributionsByStrategyId());
		
		List<Object[]> actualResult = strategyDataDao
				.getPlanContributionsByStrategyId(company, strategyId,false);
		
		assertEquals(4, actualResult.size());
	}
	
	@Test
	public void getStrategyDefaultPlans() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyDefaultPlans());
				
		Map<String, Map<String, String>> actualResult = strategyDataDao.getStrategyDefaultPlans(26, 47401);
		
		assertEquals(1, actualResult.size());
		assertEquals("002ACW", actualResult.get("14").get("14"));
		assertEquals("0038QI", actualResult.get("14").get("15"));
	}

	@Test
	public void getStrategyBenefitGroupHeadCountsFromCensus() {

		when(mockedQuery.getResultList()).thenReturn(prepareStrategyBenefitGroupHeadCounts());
		
		Map<Long, Long> actualResult = strategyDataDao
				.getStrategyBenefitGroupHeadCountsFromCensus(1L);
		
		assertEquals(2, actualResult.size());
		assertEquals(3, actualResult.get(11111L).longValue());
		assertEquals(4, actualResult.get(22222L).longValue());
	}
	
	
	@Test
	public void getStrategyBenPlans() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyBenPlans());
		
		Map<Long, Map<String, List<String>>> result = strategyDataDao
				.getStrategyBenPlans(Arrays.asList(11111L, 22222L));
		
		assertEquals(2, result.size());
		assertEquals(2, result.get(11111L).size());
		assertEquals(2, result.get(11111L).get("BENPROG1").size());
		assertEquals("BENPLAN1", result.get(11111L).get("BENPROG1").get(0));
		assertEquals("BENPLAN2", result.get(11111L).get("BENPROG1").get(1));
		assertEquals(1, result.get(11111L).get("BENPROG2").size());
		assertEquals("BENPLAN1", result.get(11111L).get("BENPROG2").get(0));
		
		assertEquals(1, result.get(22222L).size());
		assertEquals(2, result.get(22222L).get("BENPROG2").size());
		assertEquals("BENPLAN1", result.get(22222L).get("BENPROG2").get(0));
		assertEquals("BENPLAN2", result.get(22222L).get("BENPROG2").get(1));
	}
	
	/**
	 * given list of strategy ids</br>
	 * when deleteEePlanAssignmentsByStrategyIds method is called</br>
	 * then return number of records deleted
	 **/
	@Test
	public void deleteEePlanAssignmentsByStrategyIdsTest() {
		// given
		// data
		Set<Long> strategyIds = Set.of(1L, 2L);
		// method mocks
		when(mockedQuery.executeUpdate()).thenReturn(3);
		// when
		int actualResult = strategyDataDao.deleteEePlanAssignmentsByStrategyIds(strategyIds);
		// then
		// assertions
		assertEquals(3, actualResult);
	}
	
	/**
	 * given company id </br>
	 * when deleteEeDefaultPlanAssignmentsByCompanyId method is called</br>
	 * then return number of records deleted
	 **/
	@Test
	public void deleteEeDefaultPlanAssignmentsByCompanyIdTest() {
		// given
		// data
		long companyId = 129059L;
		// method mocks
		when(mockedQuery.executeUpdate()).thenReturn(7);
		// when
		int actualResult = strategyDataDao.deleteEeDefaultPlanAssignmentsByCompanyId(companyId);
		// then
		// assertions
		assertEquals(7, actualResult);
	}

	@Test
	public void deleteStrategyEstimateForPlanTypesTest() {
		// method mocks
		when(mockedQuery.executeUpdate()).thenReturn(1);
		//when
		int actualResult = strategyDataDao.deleteStrategyEstimateForPlanTypes(new HashSet<>(Arrays.asList(1111L, 2222L)), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		//then
		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResult);
	}

	@Test
	public void insertStrategyEstimateForOmsMedicalTest(){
		// method mocks
		when(mockedQuery.executeUpdate()).thenReturn(1);
		//when
		int actualResult = strategyDataDao.insertStrategyEstimateForOmsPlanTypes(new HashSet<>(Arrays.asList(1111L, 2222L)), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		//then
		verify(mockedQuery, times(1)).executeUpdate();
		assertEquals(1, actualResult);
	}

	@Test
	public void getStrategyGroupEstimateByPlanTypeTest(){
		// method mocks
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyGroupEstimateByPlanType());
		//when
		Map<String, BigDecimal> actualResult = strategyDataDao.getStrategyGroupEstimateByPlanType(strategy1, group1);
		//then
		assertEquals(2, actualResult.size());
		assertEquals(BigDecimal.valueOf(11000.75).setScale(2), actualResult.get("10").setScale(2));
		assertEquals(BigDecimal.valueOf(675.50).setScale(2), actualResult.get("11").setScale(2));
	}

	private List<Object[]> prepareStrategyBenPlans() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = new BigDecimal(11111);
		r[1] = "BENPROG1";
		r[2] = "BENPLAN1";
		results.add(r);
		r = new Object[3];
		r[0] = new BigDecimal(11111);
		r[1] = "BENPROG1";
		r[2] = "BENPLAN2";
		results.add(r);
		r = new Object[3];
		r[0] = new BigDecimal(11111);
		r[1] = "BENPROG2";
		r[2] = "BENPLAN1";
		results.add(r);
		r = new Object[3];
		r[0] = new BigDecimal(22222);
		r[1] = "BENPROG2";
		r[2] = "BENPLAN1";
		results.add(r);
		r = new Object[3];
		r[0] = new BigDecimal(22222);
		r[1] = "BENPROG2";
		r[2] = "BENPLAN2";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareStrategyBenefitGroupHeadCounts() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = new BigDecimal(11111);
		r[1] = new BigDecimal(3);
		results.add(r);
		r = new Object[2];
		r[0] = new BigDecimal(22222);
		r[1] = new BigDecimal(4);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareStrategyDefaultPlans() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = "14";
		r[1] = "002ACW";
		r[2] = "14";
		results.add(r);
		r = new Object[3];
		r[0] = "15";
		r[1] = "0038QI";
		r[2] = "14";
		results.add(r);
		return results;
	}

	private List<Object[]> preparePlanContributionsByStrategyId() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[14];
		r[0] = new BigDecimal(4298992);
		r[1] = new BigDecimal(1370898);
		r[2] = "1";
		r[3] = new BigDecimal(49.99910495318905);
		r[4] = new BigDecimal(1);
		r[5] = new BigDecimal(279.32);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "002B08";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "001QV2";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		results.add(r);
		r = new Object[14];
		r[0] = new BigDecimal(4298993);
		r[1] = new BigDecimal(1370898);
		r[2] = "2";
		r[3] = new BigDecimal(24.99932872090005);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(837.96);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "002B08";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "001QV2";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		results.add(r);
		r = new Object[14];
		r[0] = new BigDecimal(4298994);
		r[1] = new BigDecimal(1370898);
		r[2] = "C";
		r[3] = new BigDecimal(27.77683854606931);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(726.24);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "002B08";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "001QV2";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		results.add(r);
		r = new Object[14];
		r[0] = new BigDecimal(4298995);
		r[1] = new BigDecimal(1370898);
		r[2] = "4";
		r[3] = new BigDecimal(16.66626887045766);
		r[4] = new BigDecimal(0);
		r[5] = new BigDecimal(1396.59);
		r[6] = new BigDecimal(279.31);
		r[7] = new BigDecimal(35895);
		r[8] = "002B08";
		r[9] = "10";
		r[10] = 47401;
		r[11] = "001QV2";
		r[12] = new BigDecimal(18);
		r[13] = "BASE";
		results.add(r);
		return results;
	}

	@Test(expected = BSSApplicationException.class)
	public void getGroupsByStrategyExceptionTest() {
		// create an empty list to simulate no returned data
		List<Object[]> results = new ArrayList<>();
		when(mockedQuery.getResultList()).thenReturn(results);
		// should get an exception because the mock query returns an empty list
		Map<Long, List<BenefitGroup>> bgList = strategyDataDao.getGroupsByStrategy(strategyIds);
		if (null != bgList) {
			bgList.size();
		}
	}

	@Test
	public void getSubmittedStrategiesCount() {
		when(mockedQuery.getSingleResult()).thenReturn(BigDecimal.valueOf(1));
		int actualResult = strategyDataDao.getSubmittedStrategiesCount(1);
		assertEquals(1, actualResult);
	}
	
	@Test
	public void	getEmplStrategyBenGroup() {
		long companyId = 1111;
		when(mockedQuery.getResultList()).thenReturn(prepareEmplStrategyBenGroup());
		
		MultiKeyMap actualResult = strategyDataDao.getEmplStrategyBenGroup(companyId);
		
		assertEquals(2, actualResult.size());
		assertEquals("BENPROG1", ((BenefitGroup) actualResult.get("1111", 2222L)).getBenefitProgram());
		assertEquals("BENPROG2", ((BenefitGroup) actualResult.get("2222", 2222L)).getBenefitProgram());
	}

	@Test
	public void	getGroupStrategyPlanCost() {
		Company company = new Company();
		List<Long> strategyList = Arrays.asList(20017L, 20018L);
		
		when(employeeBenefitGroupDao.getBenefitProgramDetails(company)).thenReturn(prepareBenGroupMap());
		when(mockedQuery.getResultList()).thenReturn(prepareGroupStrategyPlanCost());
		
		MultiKeyMap actualResult = strategyDataDao.getGroupStrategyPlanCost(company, strategyList);
		
		assertEquals(new BigDecimal(96.25),
				((StrategyGroupPlanRateData) actualResult.get(20017L, 1111L, "11", "000SR7", "2")).getErRate());
		assertEquals(new BigDecimal(163.17),
				((StrategyGroupPlanRateData) actualResult.get(20018L, 2222L, "11", "000SR7", "C")).getErRate());
	}
	
	@Test
	public void	getStrategyGroupPlanCostByPlanType() {
		List<Long> strategyList = Arrays.asList(20017L, 20018L);
		List<String> planTypes = Arrays.asList("10", "11");
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyGroupPlanCost());
		
		Optional<List<StrategyGroupEmployeePlanRateData>> actualResult = strategyDataDao.getOmsStrategyGroupPlanCostByPlanType(strategyList,planTypes);
		
		assertEquals(new BigDecimal(96.25),
				( actualResult.get().get(0)).getEeRate());
		assertEquals(new BigDecimal(116.25),
				(actualResult.get().get(1)).getEeRate());
	}

	@Test
	public void getOmsStrategyGroupPlanCostByPlanType() {
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70);
		realmPlanYear.setPlanYearEnd(new java.util.Date());
		company.setRealmPlanYear(realmPlanYear);
		List<Long> strategyList = Arrays.asList(20017L, 20018L);
		List<String> planTypes = Arrays.asList("10", "11");
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyGroupPlanCost());

		Optional<List<StrategyGroupEmployeePlanRateData>> actualResult = strategyDataDao.getStrategyGroupPlanCostByPlanType(company, strategyList,planTypes);

		assertEquals(new BigDecimal(96.25),
				( actualResult.get().get(0)).getEeRate());
		assertEquals(new BigDecimal(116.25),
				(actualResult.get().get(1)).getEeRate());
	}
	
	@Test
	public void getStrategyProgramPlantypeOfferings() {
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyProgramPlantypeOfferings());
		
		MultiKeyMap actualResult = strategyDataDao.getStrategyProgramPlantypeOfferings(Arrays.asList(1111L, 2222L), Arrays.asList("10", "11"));
		
		assertEquals("1D", actualResult.get(1111L, "BENPROG1", "11"));
		assertEquals("10", actualResult.get(1111L, "BENPROG1", "10"));
	}
	
	@Test
	public void getHeadcountByPlanStrategyCoverage() {
		List<Long> strategyIdList = Arrays.asList(1111L, 2222L);
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1111);
		company.setRealmPlanYear(realmPlanYear );
		
		when(mockedQuery.getResultList()).thenReturn(prepareHeadcountByPlanStrategyCoverage());
		
		List<StrategyBenefitPlanHeadCount> actualResult = strategyDataDao
				.getHeadcountByPlanStrategyCoverage(strategyIdList, company.getPlanStartDate());
		
		assertEquals(3, actualResult.size());
	}
	
	@Test
	public void getStrategyGroupHeadcountCost() {
		List<Long> strategyIdList = Arrays.asList(1111L, 2222L);
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1111);
		company.setRealmPlanYear(realmPlanYear );
		
		when(mockedQuery.getResultList()).thenReturn(prepareStrategyGroupHeadcountCost());
		
		List<ModelCompareGroupHeadcount> actualResult = strategyDataDao
				.getStrategyGroupHeadcountCost(strategyIdList);
		
		assertEquals(1, actualResult.size());
	}
	
	@Test
	public void getAdditionalBenefitPlansForStrategy() {
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1111);
		company.setRealmPlanYear(realmPlanYear );
		
		when(mockedQuery.getResultList()).thenReturn(prepareAdditionalBenefitPlansForStrategy());
		
		Map<Long, List<AdditionalBenefitPlan>> actualResult = strategyDataDao
				.getAdditionalBenefitPlansForStrategy(strategy1, "01-JAN-2021");
		
		assertEquals(2, actualResult.size());
	}

	@Test
	public void getPrimaryCarrierNameTest() {
		when(mockedQuery.getSingleResult()).thenReturn("Aetna");
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70);
		realmPlanYear.setPlanYearStart(new java.util.Date());
		company.setRealmPlanYear(realmPlanYear);
		String actualResult = strategyDataDao.getPrimaryCarrierName(company, "12321");
		assertEquals("Aetna", actualResult);
	}

	@Test
	public void getAdditionalBenefitPlansForStrategyWithSdiInfo() {
		when(mockedQuery.getResultList()).thenReturn(prepareAdditionalBenefitPlansForStrategyWithSdiInfoMockData());

		Map<Long, List<DisabilityBenefitOptionPlans>> actualResult = strategyDataDao.getAdditionalBenefitPlansForStrategyWithSdiInfo(12345L, "01-JAN-2024", 78);
		assertEquals(1, actualResult.size());
		assertEquals(3, actualResult.get(1L).size());
	}

	@Test
	public void getHealthCostsByPlanType() {
		when(mockedQuery.getResultList()).thenReturn(prepareHealthCostsByPlanType());

		List<Object[]> actualResult = strategyDataDao.getHealthCostsByPlanType(1111L);

		assertEquals(2, actualResult.size());
	}

	@Test
	public void getAdditionalBenefitCostsByPlanType() {
		when(mockedQuery.getResultList()).thenReturn(prepareAdditionalBenefitCostsByPlanType());

		List<Object[]> actualResult = strategyDataDao.getHealthCostsByPlanType(1111L);

		assertEquals(2, actualResult.size());
	}
	
	@Test
	public void deleteAllPlanContributions() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteAllPlanContributionsBy(strategyIds1, "10");
		assertEquals(1, actualResult);
	}

	@Test
	public void deleteAllPlanSelections() {
		when(mockedQuery.executeUpdate()).thenReturn(1);
		int actualResult = strategyDataDao.deleteAllPlanSelectionsBy(strategyIds1, "10");
		assertEquals(1, actualResult);
	}
	
	private List<Object[]> prepareHeadcountByPlanStrategyCoverage() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[6];
		r[0] = "10";
		r[1] = "BENPLAN1";
		r[2] = "BEN PLAN 1";
		r[3] = new BigDecimal(1111);
		r[4] = "1";
		r[5] = new BigDecimal(4);
		results.add(r);
		r = new Object[6];
		r[0] = "10";
		r[1] = "BENPLAN1";
		r[2] = "BEN PLAN 1";
		r[3] = new BigDecimal(1111);
		r[4] = "C";
		r[5] = new BigDecimal(3);
		results.add(r);
		r = new Object[6];
		r[0] = "1D";
		r[1] = "DENBENPLAN1";
		r[2] = "DENTAL BEN PLAN 1";
		r[3] = new BigDecimal(1111);
		r[4] = "1";
		r[5] = new BigDecimal(3);
		results.add(r);
		r = new Object[6];
		r[0] = "1V";
		r[1] = "VISBENPLAN1";
		r[2] = "VISION BEN PLAN 1";
		r[3] = new BigDecimal(1111);
		r[4] = "1";
		r[5] = new BigDecimal(2);
		results.add(r);
		r = new Object[6];
		r[0] = "10";
		r[1] = "BENPLAN1";
		r[2] = "BEN PLAN 1";
		r[3] = new BigDecimal(2222);
		r[4] = "1";
		r[5] = new BigDecimal(2);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareStrategyProgramPlantypeOfferings() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = new BigDecimal(1111);
		r[1] = "BENPROG1";
		r[2] = "1D";
		results.add(r);
		r = new Object[3];
		r[0] = new BigDecimal(1111);
		r[1] = "BENPROG1";
		r[2] = "11";
		results.add(r);
		r = new Object[3];
		r[0] = new BigDecimal(1111);
		r[1] = "BENPROG1";
		r[2] = "10";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareGroupStrategyPlanCost() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[10];
		r[0] = new BigDecimal(19990);
		r[1] = "BENPROG1";
		r[2] = new BigDecimal(20017);
		r[3] = "11";
		r[4] = "000SR7";
		r[5] = "MetLife Enhanced";
		r[6] = "2";
		r[7] = new BigDecimal(100);
		r[8] = new BigDecimal(96.25);
		r[9] = new BigDecimal(0);
		results.add(r);
		r = new Object[10];
		r[0] = new BigDecimal(19990);
		r[1] = "BENPROG2";;
		r[2] = new BigDecimal(20018);
		r[3] = "11";
		r[4] = "000SR7";
		r[5] = "MetLife Enhanced";
		r[6] = "C";
		r[7] = new BigDecimal(100);
		r[8] = new BigDecimal(163.17);
		r[9] = new BigDecimal(0);
		results.add(r);
		return results;
	}
	
	private List<Object[]> prepareStrategyGroupPlanCost() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[11];
		r[0] = "123456789";
		r[1] = new BigDecimal(19990);
		r[2] = new BigDecimal(20018);
		r[3] = "BENPROG1";
		r[4] = "11";
		r[5] = "000SR7";
		r[6] = "MetLife Enhanced";
		r[7] = "C";
		r[8] = new BigDecimal(96.25);
		r[9] = new BigDecimal(0);
		r[10]= "Aetna";
		results.add(r);
		r = new Object[11];
		r[0] = "2864783762";
		r[1] = new BigDecimal(19990);
		r[2] = new BigDecimal(20018);
		r[3] = "BENPROG2";
		r[4] = "12";
		r[5] = "000SR7";
		r[6] = "MetLife Enhanced";
		r[7] = "C";
		r[8] = new BigDecimal(116.25);
		r[9] = new BigDecimal(0);
		r[10]= "Aetna";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareEmplStrategyBenGroup() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[5];
		r[0] = new BigDecimal(1111);
		r[1] = new BigDecimal(2222);
		r[2] = new BigDecimal(3333);
		r[3] = "Some Group 1";
		r[4] = "BENPROG1";
		results.add(r);
		r = new Object[5];
		r[0] = new BigDecimal(2222);
		r[1] = new BigDecimal(2222);
		r[2] = new BigDecimal(3333);
		r[3] = "Some Group 2";
		r[4] = "BENPROG2";
		results.add(r);
		return results;
	}

	@Test
	public void getSubmittedStrategyIssueReportData() {
		when(mockedQuery.getResultList()).thenReturn(prepareSubmittedStrategyIssueMockData());
		List<StrategySubmitIssueReport> actualResult = strategyDataDao.getSubmittedStrategyIssueReportData();
		assertEquals(3, actualResult.size());
		assertEquals(1, actualResult.get(0).getBdms().size());
		assertEquals(1, actualResult.get(1).getBdms().size());
		assertEquals(1, actualResult.get(2).getBdms().size());
	}

	/**
	 * This method is for creating Plan Carriers.
	 * 
	 * @return
	 */
	public List<Object[]> createPlanPackages() {
		List<Object[]> results = new ArrayList<>();
		Object[] planpackage = new Object[13];
		planpackage[0] = "001EKV";
		planpackage[1] = "10";
		planpackage[2] = "Conservative";
		planpackage[3] = new BigDecimal(0);
		planpackage[4] = new BigDecimal(75);
		planpackage[5] = "employee";
		planpackage[6] = "PCT";
		planpackage[7] = new BigDecimal(1);
		planpackage[8] = new BigDecimal(9);
		planpackage[9] = new BigDecimal(75);
		planpackage[10] = "employee";
		planpackage[11] = new BigDecimal(9);
		planpackage[12] = new BigDecimal(1);
		results.add(planpackage);
		Object[] planpackage1 = new Object[13];
		planpackage1[0] = "001EKV";
		planpackage1[1] = "10";
		planpackage1[2] = "Conservative";
		planpackage1[3] = new BigDecimal(0);
		planpackage1[4] = new BigDecimal(65);
		planpackage1[5] = "employeePlusChild";
		planpackage1[6] = "PCT";
		planpackage1[7] = new BigDecimal(1);
		planpackage1[8] = new BigDecimal(10);
		planpackage[9] = new BigDecimal(75);
		planpackage[10] = "employee";
		planpackage[11] = new BigDecimal(9);
		planpackage[12] = new BigDecimal(1);
		results.add(planpackage1);
		return results;
	}

	public List<Object[]> createStrategyBenGroups() {
		List<Object[]> results = new ArrayList<>();

		Object[] strategyBenGrpData = new Object[3];
		strategyBenGrpData[0] = new BigDecimal(37318);
		strategyBenGrpData[1] = new BigDecimal(30754);
		strategyBenGrpData[2] = "Group1";
		results.add(strategyBenGrpData);

		Object[] strategyBenGrpData2 = new Object[3];
		strategyBenGrpData2[0] = new BigDecimal(37318);
		strategyBenGrpData2[1] = new BigDecimal(30753);
		strategyBenGrpData2[2] = "Group2";
		results.add(strategyBenGrpData2);

		Object[] strategyBenGrpData3 = new Object[3];
		strategyBenGrpData3[0] = new BigDecimal(37319);
		strategyBenGrpData3[1] = new BigDecimal(30754);
		strategyBenGrpData3[2] = "Group3";
		results.add(strategyBenGrpData3);

		return results;

	}

	public List<Object[]> createStrategyPlanCost() {
		List<Object[]> results = new ArrayList<>();

		Object[] strategyPlanCostData = new Object[9];
		strategyPlanCostData[0] = new BigDecimal(30754);
		strategyPlanCostData[1] = new BigDecimal(37319);
		strategyPlanCostData[2] = "11";
		strategyPlanCostData[3] = "000SR7";
		strategyPlanCostData[4] = "MetLife Enhanced";
		strategyPlanCostData[5] = "1";
		strategyPlanCostData[6] = new BigDecimal(100);
		strategyPlanCostData[7] = new BigDecimal(43.15);
		strategyPlanCostData[8] = new BigDecimal(0);
		results.add(strategyPlanCostData);

		Object[] strategyPlanCostData2 = new Object[9];
		strategyPlanCostData2[0] = new BigDecimal(30754);
		strategyPlanCostData2[1] = new BigDecimal(37319);
		strategyPlanCostData2[2] = "10";
		strategyPlanCostData2[3] = "001998";
		strategyPlanCostData2[4] = "UHC Puerto Rico Plan";
		strategyPlanCostData2[5] = "C";
		strategyPlanCostData2[6] = new BigDecimal(50);
		strategyPlanCostData2[7] = new BigDecimal(728.08);
		strategyPlanCostData2[8] = new BigDecimal(728.08);
		results.add(strategyPlanCostData2);

		return results;
	}

	private List<Object[]> preparePlanSelectionIdMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[11];
		r[0] = new BigDecimal(738930);
		r[1] = new BigDecimal(293903);
		r[2] = "2";
		r[3] = new BigDecimal(100);
		r[4] = new BigDecimal(5);
		r[5] = "001EKV";
		r[6] = "employeePlusSpouse";
		r[7] = new BigDecimal(2411.8);
		r[8] = new BigDecimal(1200);
		r[9] = "PCT";
		r[10] = new BigDecimal(4);
		results.add(r);
		return results;
	}

	private List<Object[]> preparePlanTypeDescriptionsMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = "10";
		r[1] = "Medical";
		r[2] = "Medical";
		results.add(r);
		r = new Object[3];
		r[0] = "11";
		r[1] = "Dental";
		r[2] = "Dental";
		results.add(r);
		r = new Object[3];
		r[0] = "1d";
		r[1] = "Optional Dental Plan";
		r[2] = "Opt Dental";
		results.add(r);
		r = new Object[3];
		r[0] = "A3";
		r[1] = "Commuter Benefits";
		r[2] = "ComBen";
		results.add(r);
		r = new Object[3];
		r[0] = "21";
		r[1] = "Supplemental Life";
		r[2] = "Supp Life";
		results.add(r);
		r = new Object[3];
		r[0] = "14";
		r[1] = "Vision";
		r[2] = "Vision";
		results.add(r);
		r = new Object[3];
		r[0] = "23";
		r[1] = "Life and AD and D";
		r[2] = "Life,AD/D";
		results.add(r);
		return results;
	}

	private List<Object[]> prepareHistoryStrategiesMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[13];
		r[0] = BigDecimal.valueOf(42610);
		r[1] = BigDecimal.valueOf(9609);
		r[2] = "Plan with STD ~ 100% employee coverage";
		r[3] = null;
		r[4] = BigDecimal.valueOf(126956.58);
		r[5] = BigDecimal.valueOf(1575314.76);
		r[6] = null;
		r[7] = new Date(9999999999L);
		r[8] = 1;
		r[9] = BigDecimal.valueOf(0);
		r[10] = "current";
		r[11] = BigDecimal.valueOf(10);
		r[12] = BigDecimal.valueOf(12);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareStrategiesMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[12];
		r[0] = BigDecimal.valueOf(9609);
		r[1] = BigDecimal.valueOf(9609);
		r[2] = "Plan with STD ~ 100% employee coverage";
		r[3] = null;
		r[4] = BigDecimal.valueOf(126956.58);
		r[5] = BigDecimal.valueOf(1575314.76);
		r[6] = null;
		r[7] = new Date(9999999999L);
		r[8] = 1;
		r[9] = BigDecimal.valueOf(0);
		r[10] = "current";
		r[11] = BigDecimal.valueOf(10);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareRealmClonePgmMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "108";
		r[1] = new Date(999999999L);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareLifeDisabilityCvrgMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = "21";
		r[1] = "000SRP";
		r[2] = BigDecimal.valueOf(450000);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareAddtnlPlansEstCostMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[4];
		r[0] = "A3";
		r[1] = "000W46";
		r[2] = null;
		r[3] = BigDecimal.valueOf(5.85);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareMCStrategiesMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[5];
		r[0] = BigDecimal.valueOf(18332);
		r[1] = "Future Benefits Solution";
		r[2] = BigDecimal.valueOf(1);
		r[3] = BigDecimal.valueOf(0);
		r[4] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[5];
		r[0] = BigDecimal.valueOf(18333);
		r[1] = "Future Benefits Solution 1";
		r[2] = BigDecimal.valueOf(0);
		r[3] = BigDecimal.valueOf(0);
		r[4] = BigDecimal.valueOf(0);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareMCStrategiesCostMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[4];
		r[0] = BigDecimal.valueOf(29346);
		r[1] = "10";
		r[2] = null;
		r[3] = BigDecimal.valueOf(16042.64);
		results.add(r);
		r = new Object[4];
		r[0] = BigDecimal.valueOf(29346);
		r[1] = "1V";
		r[2] = null;
		r[3] = BigDecimal.valueOf(0);
		results.add(r);
		r = new Object[4];
		r[0] = BigDecimal.valueOf(49480);
		r[1] = "1D";
		r[2] = null;
		r[3] = BigDecimal.valueOf(140.4);
		results.add(r);
		r = new Object[4];
		r[0] = BigDecimal.valueOf(29346);
		r[1] = "14";
		r[2] = null;
		r[3] = BigDecimal.valueOf(1834.11);
		results.add(r);
		r = new Object[4];
		r[0] = BigDecimal.valueOf(29346);
		r[1] = "10";
		r[2] = BSSApplicationConstants.WAIVER_ALLOWANCE_PLAN_SUB_TYPE;
		r[3] = BigDecimal.valueOf(1000);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareStrategyGrpPlanTypeMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = BigDecimal.valueOf(18332);
		r[1] = "VCB";
		r[2] = "10";
		results.add(r);
		r = new Object[3];
		r[0] = BigDecimal.valueOf(18332);
		r[1] = "VCB";
		r[2] = "11";
		results.add(r);
		r = new Object[3];
		r[0] = BigDecimal.valueOf(18332);
		r[1] = "VCB";
		r[2] = "14";
		results.add(r);
		return results;
	}

	private List<Object[]> preparePlanOverridesMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[7];
		r[0] = "001RS3";
		r[1] = "001998";
		r[2] = "1";
		r[3] = null;
		r[4] = BigDecimal.ZERO;
		r[5] = BigDecimal.ZERO;
		r[6] = "NEWPLANID";
		results.add(r);
		r = new Object[7];
		r[0] = "001RS3";
		r[1] = "001998";
		r[2] = "2";
		r[3] = null;
		r[4] = BigDecimal.ZERO;
		r[5] = BigDecimal.ZERO;
		r[6] = "NEWPLANID";
		results.add(r);
		r = new Object[7];
		r[0] = "001RS3";
		r[1] = "001EKY";
		r[2] = "1";
		r[3] = null;
		r[4] = BigDecimal.ONE;
		r[5] = BigDecimal.ZERO;
		r[6] = "NEWPLANID";
		results.add(r);
		r = new Object[7];
		r[0] = "EF1";
		r[1] = "001998";
		r[2] = "1";
		r[3] = null;
		r[4] = BigDecimal.ZERO;
		r[5] = BigDecimal.ONE;
		r[6] = "NEWPLANID";
		results.add(r);
		return results;
	}

	private List<Object[]> preparePlanSelectionMockData() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[13];
		r[0] = BigDecimal.valueOf(42610);
		r[1] = BigDecimal.valueOf(36814);
		r[2] = "10";
		r[3] = BigDecimal.valueOf(1575592);
		r[4] = "001EKY";
		r[5] = BigDecimal.valueOf(9);
		r[6] = null;
		r[7] = "UHCAM";
		r[8] = "UHC Standard";
		r[9] = BigDecimal.valueOf(0);
		r[10] = BigDecimal.valueOf(1);
		r[11] = "DFLT";
		r[12] = BigDecimal.valueOf(1);
		results.add(r);
		r = new Object[13];
		r[0] = BigDecimal.valueOf(42610);
		r[1] = BigDecimal.valueOf(36814);
		r[2] = "11";
		r[3] = BigDecimal.valueOf(1575595);
		r[4] = "000SR7";
		r[5] = BigDecimal.valueOf(3);
		r[6] = null;
		r[7] = "METAM";
		r[8] = "MetLife Enhanced";
		r[9] = BigDecimal.valueOf(0);
		r[10] = BigDecimal.valueOf(0);
		r[11] = "DFLT";
		r[12] = BigDecimal.valueOf(0);
		results.add(r);
		r = new Object[13];
		r[0] = BigDecimal.valueOf(42610);
		r[1] = BigDecimal.valueOf(36814);
		r[2] = "14";
		r[3] = BigDecimal.valueOf(1575596);
		r[4] = "002J24";
		r[5] = BigDecimal.valueOf(6);
		r[6] = null;
		r[7] = "VSPAM";
		r[8] = "VSP Vision";
		r[9] = BigDecimal.valueOf(0);
		r[10] = BigDecimal.valueOf(0);
		r[11] = "DFLT";
		r[12] = BigDecimal.valueOf(0);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareSubmittedStrategyIssueMockData() {
		List<Object[]> results = new ArrayList<>();
		
		final int STATEMENT_UPLOAD_STATUS_INDEX = 0;
		final int EMAIL_SENT_INDEX = 1;
		final int STRATEGY_ID_INDEX = 2;
		final int COMPANY_CODE_INDEX = 3;
		final int EXCHANGE_INDEX = 4;
		final int QE_QUARTER_INDEX = 5;
		final int COMPANY_LEGAL_NAME_INDEX = 6;
		final int COMPANY_DBA_NAME_INDEX = 7;
		final int EMPLOYEE_ID_INDEX = 8;
		final int EMPLOYEE_FIRST_NAME_INDEX = 9;
		final int EMPLOYEE_LAST_NAME_INDEX = 10;
		final int SUBMIT_DATE_INDEX = 11;
		final int SUBMITTER_INDEX = 12;		
		
		Object[] r = new Object[13];
		r[STATEMENT_UPLOAD_STATUS_INDEX] = "NA";
		r[EMAIL_SENT_INDEX] = "N";
		r[STRATEGY_ID_INDEX] = BigDecimal.ONE;
		r[COMPANY_CODE_INDEX] = "COMPANY1";
		r[EXCHANGE_INDEX] = "EXCHANGE 1";
		r[QE_QUARTER_INDEX] = "QUARTER";
		r[COMPANY_LEGAL_NAME_INDEX] = "COMPANY 1 LEGAL NAME";
		r[COMPANY_DBA_NAME_INDEX] = "COMPANY 1 DBA NAME";
		r[EMPLOYEE_ID_INDEX] = "EMPLOYEE 1 ID";
		r[EMPLOYEE_FIRST_NAME_INDEX] = "EMPLOYEE 1 FIRSTNAME";
		r[EMPLOYEE_LAST_NAME_INDEX] = "EMPLOYEE 1 LASTNAME";
		r[SUBMIT_DATE_INDEX] = new Date(9999999L);
		r[SUBMITTER_INDEX] = "N";
		results.add(r);

		r = new Object[13];
		r[STATEMENT_UPLOAD_STATUS_INDEX] = "NA";
		r[EMAIL_SENT_INDEX] = "N";
		r[STRATEGY_ID_INDEX] = BigDecimal.valueOf(2);
		r[COMPANY_CODE_INDEX] = "COMPANY1";
		r[EXCHANGE_INDEX] = "EXCHANGE 1";
		r[QE_QUARTER_INDEX] = "QUARTER";
		r[COMPANY_LEGAL_NAME_INDEX] = "COMPANY 1 LEGAL NAME";
		r[COMPANY_DBA_NAME_INDEX] = "COMPANY 1 DBA NAME";
		r[EMPLOYEE_ID_INDEX] = "EMPLOYEE 2 ID";
		r[EMPLOYEE_FIRST_NAME_INDEX] = "EMPLOYEE 2 FIRSTNAME";
		r[EMPLOYEE_LAST_NAME_INDEX] = "EMPLOYEE 2 LASTNAME";
		r[SUBMIT_DATE_INDEX] = new Date(9999999L);
		r[SUBMITTER_INDEX] = "Y";
		results.add(r);

		r = new Object[13];
		r[STATEMENT_UPLOAD_STATUS_INDEX] = BSSApplicationConstants.ERROR;
		r[EMAIL_SENT_INDEX] = "Y";
		r[STRATEGY_ID_INDEX] = BigDecimal.valueOf(3);
		r[COMPANY_CODE_INDEX] = "COMPANY2";
		r[EXCHANGE_INDEX] = "EXCHANGE 2";
		r[QE_QUARTER_INDEX] = "QUARTER";
		r[COMPANY_LEGAL_NAME_INDEX] = "COMPANY 2 LEGAL NAME";
		r[COMPANY_DBA_NAME_INDEX] = "COMPANY 2 DBA NAME";
		r[EMPLOYEE_ID_INDEX] = "EMPLOYEE 3 ID";
		r[EMPLOYEE_FIRST_NAME_INDEX] = "EMPLOYEE 3 FIRSTNAME";
		r[EMPLOYEE_LAST_NAME_INDEX] = "EMPLOYEE 3 LASTNAME";
		r[SUBMIT_DATE_INDEX] = new Date(9999999L);
		r[SUBMITTER_INDEX] = "N";
		results.add(r);

		return results;
	}	
	
	private Map<String, EmployeeBenefitGroup> prepareBenGroupMap() {
		Map<String, EmployeeBenefitGroup> benefitGroupMap = new HashMap<>();
		EmployeeBenefitGroup benGrp = new EmployeeBenefitGroup();
		benGrp.setBenefitGroupId(1111L);
		benefitGroupMap.put("BENPROG1", benGrp);
		benGrp = new EmployeeBenefitGroup();
		benGrp.setBenefitGroupId(2222L);
		benefitGroupMap.put("BENPROG2", benGrp);
		return benefitGroupMap;
	}

	private List<Object[]> prepareStrategyGroupHeadcountCost() {
		List<Object[]> results = new ArrayList<>();

		Object[] r = new Object[7];
		r[0] = "medical";
		r[1] = BigDecimal.ONE;
		r[2] = BigDecimal.valueOf(strategy1);
		r[3] = "BENPROG1";
		r[4] = "BENPROG1_DESCR";
		r[5] = BigDecimal.TEN;
		r[6] = BigDecimal.valueOf(1000);
		results.add(r);
		
		r = new Object[7];
		r[0] = "dental";
		r[1] = BigDecimal.ONE;
		r[2] = BigDecimal.valueOf(strategy1);
		r[3] = "BENPROG1";
		r[4] = "BENPROG1_DESCR";
		r[5] = BigDecimal.TEN;
		r[6] = BigDecimal.valueOf(500);
		results.add(r);

		return results;
	}

	private List<Object[]> prepareAdditionalBenefitPlansForStrategy() {
		List<Object[]> results = new ArrayList<>();

		Object[] r = new Object[4];
		r[0] = BigDecimal.ONE;
		r[1] = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
		r[2] = "MEDICAL_PLAN_1";
		r[3] = "MEDICAL_PLAN_1_DESCR";
		results.add(r);
		
		r = new Object[4];
		r[0] = BigDecimal.TEN;
		r[1] = BSSApplicationConstants.DENTAL_PLAN_TYPE;
		r[2] = "DENTAL_PLAN_1";
		r[3] = "DENTAL_PLAN_1_DESCR";
		results.add(r);

		return results;
	}

	private List<Object[]> prepareAdditionalBenefitPlansForStrategyWithSdiInfoMockData() {
		List<Object[]> results = new ArrayList<>();

		Object[] r = new Object[7];
		r[0] = BigDecimal.ONE;
		r[1] = BSSApplicationConstants.STD_CODE;
		r[2] = "STD_PLAN_1";
		r[3] = "STD_PLAN_1_DESCR";
		r[4] = "STD_CARRIER_NAME";
		r[5] = "1";
		r[6] = "0";
		results.add(r);

		r = new Object[7];
		r[0] = BigDecimal.ONE;
		r[1] = BSSApplicationConstants.STD_CODE;
		r[2] = "STD_PLAN_2";
		r[3] = "STD_PLAN_2_DESCR";
		r[4] = "STD_CARRIER_NAME";
		r[5] = "0";
		r[6] = "0";
		results.add(r);

		r = new Object[7];
		r[0] = BigDecimal.ONE;
		r[1] = BSSApplicationConstants.LTD_CODE;
		r[2] = "LTD_PLAN_1";
		r[3] = "LTD_PLAN_1_DESCR";
		r[4] = "LTD_CARRIER_NAME";
		r[5] = "0";
		r[6] = "1";
		results.add(r);

		return results;
	}

	private List<Object[]> prepareHealthCostsByPlanType() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[3];
		r[0] = "medical";
		r[1] = new BigDecimal(100);
		r[2] = new BigDecimal(200);
		results.add(r);
		r = new Object[3];
		r[0] = "dental";
		r[1] = new BigDecimal(20);
		r[2] = new BigDecimal(40);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareAdditionalBenefitCostsByPlanType() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "LIFE";
		r[1] = new BigDecimal(100);
		results.add(r);
		r = new Object[2];
		r[0] = "DISABILITY";
		r[1] = new BigDecimal(20);
		results.add(r);
		return results;
	}

	private List<Object[]> prepareStrategyGroupEstimateByPlanType() {
		List<Object[]> results = new ArrayList<>();
		Object[] r = new Object[2];
		r[0] = "10";
		r[1] = new BigDecimal(11000.75);
		results.add(r);

		r = new Object[2];
		r[0] = "11";
		r[1] = new BigDecimal(675.50);
		results.add(r);

		return results;
	}

}