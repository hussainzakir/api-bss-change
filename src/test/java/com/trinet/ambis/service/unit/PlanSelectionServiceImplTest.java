/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.PlanSelectionServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.PlanSelectionDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixBenefitPlanData;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.impl.PlanSelectionServiceImpl;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.CoverageLevel;

/**
 * @author vshukla
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PlanSelectionServiceImplTest extends ServiceUnitTest {
	
	@InjectMocks
	PlanSelectionServiceImpl planSelectionService;

	@Mock
	PlanSelectionDao planSelectionDao;

	@Mock
	StrategyDataDao strategyDataDao;

	@Mock
	StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

	@Mock
	RealmPlyrPlanService realmPlyrPlanService;

	@Mock
	BenefitPlanDao benefitPlanDao;

	@Mock
	EmployeePlanAssignmentService employeePlanAssignmentService;

	@Mock
	StrategyGroupService strategyGroupService;
	
	private static final String BENEFIT_PLAN = "000SR9";
	private static final String TIB_BENEFIT_PLAN_1 = "771234";
	private static final String TIB_BENEFIT_PLAN_2 = "884567";
	private static final String TIB_BENEFIT_PLAN_3 = "997890";
	private static final String TIB_BENEFIT_PLAN_4 = "1123456";
	private static final long STRATEGY_ID = 282357;
    private MockedStatic<PlanSelectionServiceHelper> planSelectionServiceHelperMockedStatic;

    @Before
    public void setUp() {
        planSelectionServiceHelperMockedStatic = Mockito.mockStatic(PlanSelectionServiceHelper.class);
    }

    @After
    public void tearDown() {
        planSelectionServiceHelperMockedStatic.close();
    }

	@Test
	public void createUpdatePlanSelection() {
		PlanSelection planSelection = new PlanSelection();

		long strategyId = 24182;
		long groupId = 24154;
		String planType = BSSApplicationConstants.VISION_PLAN_TYPE;
		long headCount = 4;
		boolean ppoPlan = false;

		planSelection.setStrategyId(strategyId);
		planSelection.setGroupId(groupId);
		planSelection.setPlanType(planType);
		planSelection.setBenefitPlan(BENEFIT_PLAN);
		planSelection.setHeadCount(headCount);
		planSelection.setPpoPlan(ppoPlan);

		when(planSelectionDao.saveAndFlush(planSelection)).thenReturn(planSelection);
		PlanSelection planSelectionResult = planSelectionService.createUpdatePlanSelection(planSelection);
		assertEquals(planSelectionResult, planSelection);
		assertEquals(planSelectionResult.getStrategyId(), planSelection.getStrategyId());
	}

	@Test
	public void getPlanSelection() {
		PlanSelection planSelection = new PlanSelection();

		long strategyId = 24182;
		long groupId = 24154;
		String planType = BSSApplicationConstants.VISION_PLAN_TYPE;
		long headCount = 4;
		boolean ppoPlan = false;

		planSelection.setStrategyId(strategyId);
		planSelection.setGroupId(groupId);
		planSelection.setPlanType(planType);
		planSelection.setBenefitPlan(BENEFIT_PLAN);
		planSelection.setHeadCount(headCount);
		planSelection.setPpoPlan(ppoPlan);

		when(planSelectionDao.getByStrategyIdAndGroupIdAndBenefitPlan(strategyId, groupId, BENEFIT_PLAN))
				.thenReturn(planSelection);
		PlanSelection planSelectionReturn = planSelectionService.getPlanSelection(strategyId, groupId, BENEFIT_PLAN);
		assertEquals(planSelectionReturn, planSelection);
	}

	@Test
	public void getPlansByStrategyIdGroupId() {
		PlanSelection planSelection = new PlanSelection();

		long strategyId = 24182;
		long groupId = 24154;
		String planType = BSSApplicationConstants.VISION_PLAN_TYPE;
		long headCount = 4;
		boolean ppoPlan = false;

		planSelection.setStrategyId(strategyId);
		planSelection.setGroupId(groupId);
		planSelection.setPlanType(planType);
		planSelection.setBenefitPlan(BENEFIT_PLAN);
		planSelection.setHeadCount(headCount);
		planSelection.setPpoPlan(ppoPlan);

		List<PlanSelection> listOfPlanSelections = new ArrayList<>();
		listOfPlanSelections.add(planSelection);

		when(strategyGroupPlanSelectDao.findByStrategyIdAndGroupId(strategyId, groupId))
				.thenReturn(listOfPlanSelections);
		List<PlanSelection> listOfPlanSelectionReturn = planSelectionService.getPlansByStrategyIdGroupId(strategyId,
				groupId);
		assertEquals(listOfPlanSelectionReturn.size(), 1);
		assertEquals(listOfPlanSelections.get(0), planSelection);
	}

	@Test
	public void getPlansByGroupId() {
		PlanSelection planSelection = new PlanSelection();

		long strategyId = 24182;
		long groupId = 24154;
		String planType = BSSApplicationConstants.VISION_PLAN_TYPE;
		long headCount = 4;
		boolean ppoPlan = false;

		planSelection.setStrategyId(strategyId);
		planSelection.setGroupId(groupId);
		planSelection.setPlanType(planType);
		planSelection.setBenefitPlan(BENEFIT_PLAN);
		planSelection.setHeadCount(headCount);
		planSelection.setPpoPlan(ppoPlan);

		List<PlanSelection> listOfPlanSelections = new ArrayList<>();
		listOfPlanSelections.add(planSelection);

		when(strategyGroupPlanSelectDao.findByGroupId(groupId)).thenReturn(listOfPlanSelections);

		List<PlanSelection> listOfPlanSelectionReturn = planSelectionService.getPlansByGroupId(groupId);
		assertEquals(listOfPlanSelectionReturn.size(), 1);
		assertEquals(listOfPlanSelections.get(0), planSelection);
	}

	@Test
	public void deleteAll() {
		PlanSelection planSelection = new PlanSelection();
		planSelection.setId(1111);
		List<PlanSelection> plans = Arrays.asList(planSelection);

		doNothing().when(strategyGroupPlanSelectDao).deleteAllInBatch(plans);

		planSelectionService.deleteAll(plans);

		verify(strategyGroupPlanSelectDao, times(1)).deleteAllInBatch(plans);
	}

	@Test
	public void saveAll() {
		List<PlanSelection> planSelectionList = new ArrayList<>();
		PlanSelection planSelection = new PlanSelection();

		long strategyId = 24182;
		long groupId = 24154;
		String planType = BSSApplicationConstants.VISION_PLAN_TYPE;
		long headCount = 4;
		boolean ppoPlan = false;

		planSelection.setStrategyId(strategyId);
		planSelection.setGroupId(groupId);
		planSelection.setPlanType(planType);
		planSelection.setBenefitPlan(BENEFIT_PLAN);
		planSelection.setHeadCount(headCount);
		planSelection.setPpoPlan(ppoPlan);
		planSelectionList.add(planSelection);

		when(planSelectionDao.saveAllAndFlush(planSelectionList)).thenReturn(planSelectionList);

		List<PlanSelection> result = planSelectionService.saveAll(planSelectionList);

		verify(planSelectionDao, times(1)).saveAllAndFlush(planSelectionList);
		assertEquals(planSelectionList, result);
	}
	
	@Test
	public void addRequiredDentalVisionPlansTest() throws Exception {
		
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_IV.getBenExchng());
		company.setRealm(realm);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1);
		company.setRealmPlanYear(realmPlanYear);
		
		List<BenefitGroup> benefitGroupList = new ArrayList<>();
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsHealthPlansMap = new HashMap<>();
		Map<String, List<CoverageLevel>> mapOfCoverageLevels = new HashMap<>();
		
		when(realmPlyrPlanService.getPlanTypePlanMapForRealmPlanYear(Mockito.anyLong(), Mockito.anyList())).thenReturn(null);
		
		planSelectionService.addRequiredDentalVisionPlans(company, benefitGroupList, bgsHealthPlansMap, mapOfCoverageLevels);
	}

	@Test
	public void findAppendixReportBenefitPlansByTest() {
		List<PlanAppendixBenefitPlanData> plansWithoutEnrollments = createPlanAppendixBenefitPlanData();
		when(benefitPlanDao.getPlansForAppendix(Mockito.any(), Mockito.any(), Mockito.anyList(),Mockito.anyList(),Mockito.anyList(), anyBoolean())).thenReturn(plansWithoutEnrollments);

		Map<String, List<PlanAppendixBenefitPlanData>> planAppendixDataByPlanType = planSelectionService.findAppendixReportBenefitPlansBy(new Company(), "282357", Arrays.asList(new String[]{"NY"}), Arrays.asList(new String[]{"10", "11"}), false);

		assertEquals(3, planAppendixDataByPlanType.size());
		assertEquals(2, planAppendixDataByPlanType.get("10").size());
		assertEquals(2, planAppendixDataByPlanType.get("11").size());
		assertEquals(1, planAppendixDataByPlanType.get("14").size());

	}

	@Test
	public void syncOmsMedicalPlanSelectionsTest() {
		// given
		// then
		when(employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(STRATEGY_ID)))
				.thenReturn(prepareEmployeePlanAssignments());
		when(strategyGroupService.findByStrategyIdAndStatus(STRATEGY_ID, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(prepareBenefitGroupStrategies());
		when(strategyGroupPlanSelectDao.findByStrategyIdAndPlanTypeIn(STRATEGY_ID, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER))
				.thenReturn(prepareCurrentPlanSelections());
		when(planSelectionDao.saveAllAndFlush(Mockito.anyList())).thenReturn(new ArrayList<>());
		doNothing().when(strategyGroupPlanSelectDao).deleteAllInBatch(Mockito.anyList());

		// when
		planSelectionService.syncOmsMedicalPlanSelections(STRATEGY_ID);

		verify(employeePlanAssignmentService, times(1)).getEmployeePlanAssigmentBy(List.of(STRATEGY_ID));
		verify(strategyGroupService, times(1)).findByStrategyIdAndStatus(STRATEGY_ID, BSSApplicationConstants.STATUS_ACTIVE);
		verify(strategyGroupPlanSelectDao, times(1)).findByStrategyIdAndPlanTypeIn(STRATEGY_ID, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
		verify(planSelectionDao, times(1)).saveAllAndFlush(Mockito.anyList());
		verify(strategyGroupPlanSelectDao, times(1)).deleteAllInBatch(Mockito.anyList());



	}

	private List<PlanAppendixBenefitPlanData> createPlanAppendixBenefitPlanData() {
		List<PlanAppendixBenefitPlanData> planAppendixBenefitPlanDataList = new ArrayList<>();
		PlanAppendixBenefitPlanData planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("MEDPLAN1");
		planAppendixBenefitPlanData.setDescription("Aetna Medical Plan 1");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("MEDPLAN2");
		planAppendixBenefitPlanData.setDescription("Aetna Medical Plan 2");
		planAppendixBenefitPlanData.setPlanType("10");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("DENPLAN1");
		planAppendixBenefitPlanData.setDescription("Aetna Dental Plan 1");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("DENPLAN2");
		planAppendixBenefitPlanData.setDescription("Aetna Dental Plan 2");
		planAppendixBenefitPlanData.setPlanType("11");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		planAppendixBenefitPlanData = new PlanAppendixBenefitPlanData();
		planAppendixBenefitPlanData.setBenefitPlan("VISPLAN1");
		planAppendixBenefitPlanData.setDescription("Aetna Vision Plan 1");
		planAppendixBenefitPlanData.setPlanType("14");
		planAppendixBenefitPlanDataList.add(planAppendixBenefitPlanData);

		return planAppendixBenefitPlanDataList;
	}

	private List<EePlanAssignment> prepareEmployeePlanAssignments() {
		List<EePlanAssignment> employeePlanAssignments = new ArrayList<>();
		EePlanAssignment eePlanAssignment = new EePlanAssignment();
		EePlanAssignmentPK eePlanAssignmentPK = new EePlanAssignmentPK();
		eePlanAssignmentPK.setBenefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		eePlanAssignment.setEePlanAssignmentPK(eePlanAssignmentPK);
		eePlanAssignment.setBenefitPlan(TIB_BENEFIT_PLAN_1);
		employeePlanAssignments.add(eePlanAssignment);

		eePlanAssignment = new EePlanAssignment();
		eePlanAssignmentPK = new EePlanAssignmentPK();
		eePlanAssignmentPK.setBenefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		eePlanAssignment.setEePlanAssignmentPK(eePlanAssignmentPK);
		eePlanAssignment.setBenefitPlan(TIB_BENEFIT_PLAN_4);
		employeePlanAssignments.add(eePlanAssignment);
		return employeePlanAssignments;
	}

	private List<BenefitGroupStrategy> prepareBenefitGroupStrategies() {
		List<BenefitGroupStrategy> benefitGroupStrategies = new ArrayList<>();
		BenefitGroupStrategy benefitGroupStrategy = new BenefitGroupStrategy();
		benefitGroupStrategy.setId(1L);
		benefitGroupStrategy.setGroupId(1L);
		benefitGroupStrategies.add(benefitGroupStrategy);

		benefitGroupStrategy = new BenefitGroupStrategy();
		benefitGroupStrategy.setId(2L);
		benefitGroupStrategy.setGroupId(2L);
		benefitGroupStrategies.add(benefitGroupStrategy);

		return benefitGroupStrategies;
	}

	private List<PlanSelection> prepareCurrentPlanSelections() {
		List<PlanSelection> planSelections = new ArrayList<>();
		PlanSelection planSelection = new PlanSelection();
		planSelection.setId(1L);
		planSelection.setStrategyId(STRATEGY_ID);
		planSelection.setGroupId(1L);
		planSelection.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planSelection.setBenefitPlan(TIB_BENEFIT_PLAN_1);
		planSelections.add(planSelection);

		planSelection = new PlanSelection();
		planSelection.setId(2L);
		planSelection.setStrategyId(STRATEGY_ID);
		planSelection.setGroupId(2L);
		planSelection.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planSelection.setBenefitPlan(TIB_BENEFIT_PLAN_1);
		planSelections.add(planSelection);

		planSelection = new PlanSelection();
		planSelection.setId(3L);
		planSelection.setStrategyId(STRATEGY_ID);
		planSelection.setGroupId(1L);
		planSelection.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planSelection.setBenefitPlan(TIB_BENEFIT_PLAN_2);
		planSelections.add(planSelection);

		planSelection = new PlanSelection();
		planSelection.setId(4L);
		planSelection.setStrategyId(STRATEGY_ID);
		planSelection.setGroupId(2L);
		planSelection.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planSelection.setBenefitPlan(TIB_BENEFIT_PLAN_2);
		planSelections.add(planSelection);

		planSelection = new PlanSelection();
		planSelection.setId(5L);
		planSelection.setStrategyId(STRATEGY_ID);
		planSelection.setGroupId(1L);
		planSelection.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planSelection.setBenefitPlan(TIB_BENEFIT_PLAN_3);
		planSelections.add(planSelection);

		planSelection = new PlanSelection();
		planSelection.setId(6L);
		planSelection.setStrategyId(STRATEGY_ID);
		planSelection.setGroupId(2L);
		planSelection.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		planSelection.setBenefitPlan(TIB_BENEFIT_PLAN_3);
		planSelections.add(planSelection);

		return planSelections;
	}
}