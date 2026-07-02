package com.trinet.ambis.service.unit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.BenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.BenefitStrategyGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.GroupCovrgHeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.GroupHeadCount;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.sp.GetNextEligRulesId;
import com.trinet.ambis.persistence.sp.NextBenProgram;
import com.trinet.ambis.persistence.sp.NextRateTblID;
import com.trinet.ambis.service.BenefitClassService;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.GroupRuleService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.RealmWaitPeriodService;
import com.trinet.ambis.service.StrategyFundingModelService;
import com.trinet.ambis.service.StrategyGroupHeadCountService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.BenefitGroupServiceImpl;
import com.trinet.ambis.service.model.EmployeeCensusStrategyGroupDetails;
import com.trinet.ambis.service.model.GroupData;
import com.trinet.ambis.service.model.GroupRuleDto;
import com.trinet.ambis.service.prospect.ProspectGroupService;

/**
 * @author rvutukuri
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class BenefitGroupServiceImplTest extends ServiceUnitTest {
	@InjectMocks
	BenefitGroupServiceImpl benefitGroupService;

	@Mock
	BenefitGroupDao benefitGroupDao;

	@Mock
	private NextBenProgram nextBenProgram;

	@Mock
	private NextRateTblID nextRateTblID;

	@Mock
	GroupCovrgHeadCountDao groupCovrgHeadCountDao;

	@Mock
	private GetNextEligRulesId spGetNextEligRulesId;

	@Mock
	private BenefitStrategyGroupDao benefitStrategyGroupDao;

	@Mock
	PlanSelectionService planSelectionService;

	@Mock
	ContributionService contributionService;

	@Mock
	StrategyGroupService strategyGroupService;

	@Mock
	GroupRuleService groupRuleService;

	@Mock
	RealmWaitPeriodService realmWaitPeriodService;

	@Mock
	ProspectGroupService prospectGroupService;

	@Mock
	StrategyService strategyService;

	@Mock
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Mock
	CacheService cacheService;

	@Mock
	BenefitClassService benefitClassService;

	@Mock
	StrategyGroupHeadCountService strategyGroupHeadCountService;

	@Captor
	ArgumentCaptor<String> prospectIdCaptor;

	@Captor
	ArgumentCaptor<Long> sourceGroupIdCaptor;

	@Captor
	ArgumentCaptor<String> groupNameCaptor;

	@Mock
	StrategyFundingModelService strategyFundingModelService;

	@Mock
	StrategyDataDao strategyDataDao;
	private static final String BEN_PROGRAM_1 = "BENPROG1";
	private static final String BEN_PROGRAM_2 = "BENPROG2";

	@Test
	public void saveBenefitGroup() {
		BenefitGroup benefitGroup = new BenefitGroup();
		when(benefitGroupDao.saveAndFlush(benefitGroup)).thenReturn(benefitGroup);
		BenefitGroup actualResult = benefitGroupService.saveBenefitGroup(benefitGroup);
		verify(benefitGroupDao, times(1)).saveAndFlush(benefitGroup);
		assertEquals(benefitGroup, actualResult);
	}

	@Test
	public void deleteBenefitGroup() {
		long id = 1111;
		doNothing().when(benefitGroupDao).deleteById(id);
		benefitGroupService.deleteBenefitGroup(id);
		verify(benefitGroupDao, times(1)).deleteById(id);
	}

	@Test
	public void getBenefitGroupByCompanyIdAndId() {
		long companyId = 1111;
		long groupId = 2222;
		BenefitGroup benGrp = new BenefitGroup();
		when(benefitGroupDao.findByCompanyIdAndId(companyId, groupId)).thenReturn(benGrp);
		BenefitGroup actualResult = benefitGroupService.getBenefitGroupByCompanyIdAndId(companyId, groupId);
		verify(benefitGroupDao, times(1)).findByCompanyIdAndId(companyId, groupId);
		assertEquals(benGrp, actualResult);
	}

	@Test
	public void getAllBenefitGroups() {
		long companyId = 1111;
		String status = "A";
		List<BenefitGroup> benGrps = new ArrayList<>();
		when(benefitGroupDao.findByCompanyIdAndStatus(companyId, status)).thenReturn(benGrps);
		List<BenefitGroup> actualResult = benefitGroupService.getAllBenefitGroups(companyId, status);
		verify(benefitGroupDao, times(1)).findByCompanyIdAndStatus(companyId, status);
		assertEquals(benGrps, actualResult);
	}

	@Test
	public void getBenefitGroupByStrategy() {
		long strategyId = 1111;
		String status = "A";
		List<BenefitGroup> benGrps = new ArrayList<>();
		when(benefitGroupDao.getBenefitGroupsByStrategyId(strategyId, status)).thenReturn(benGrps);
		List<BenefitGroup> actualResult = benefitGroupService.getBenefitGroupByStrategy(strategyId, status);
		verify(benefitGroupDao, times(1)).getBenefitGroupsByStrategyId(strategyId, status);
		assertEquals(benGrps, actualResult);
	}

	@Test
	public void getBenefitGroupsByStrategyIdAndGroupId() {
		long strategyId = 1111;
		long groupId = 2222;
		String status = "A";
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setId(groupId);
		benGrp.setStatus(status);
		when(benefitGroupDao.getBenefitGroupsByStrategyIdAndGroupId(strategyId, groupId, status)).thenReturn(benGrp);
		BenefitGroup actualResult = benefitGroupService.getBenefitGroupsByStrategyIdAndGroupId(strategyId, groupId,
				status);
		verify(benefitGroupDao, times(1)).getBenefitGroupsByStrategyIdAndGroupId(strategyId, groupId, status);
		assertEquals(benGrp, actualResult);
		assertTrue(actualResult.isActive());
	}

	@Test
	public void saveAll() {
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		when(benefitGroupDao.saveAll(benefitGroups)).thenReturn(benefitGroups);
		List<BenefitGroup> actualResult = benefitGroupService.saveAll(benefitGroups);
		verify(benefitGroupDao, times(1)).saveAll(benefitGroups);
		assertEquals(benefitGroups, actualResult);
	}

	@Test
	public void getGroupHeadCount() {
		long id = 1111;
		GroupHeadCount grpHeadCnt = new GroupHeadCount();
		when(groupCovrgHeadCountDao.findByGroupId(id)).thenReturn(grpHeadCnt);
		GroupHeadCount actualResult = benefitGroupService.getGroupHeadCount(id);
		verify(groupCovrgHeadCountDao, times(1)).findByGroupId(id);
		assertEquals(grpHeadCnt, actualResult);
	}

	@Test
	public void updateBenefitGroupNameTestForProspectCurrentStrategy() {
		doNothing().when(prospectGroupService).updateGroupName(1111, "Group 1");
		benefitGroupService.updateBenefitGroupName(0, 1111, "Group 1");
		verify(prospectGroupService, times(1)).updateGroupName(1111, "Group 1");
		verify(cacheService, times(0)).invalidateCache(anyString(), anyString(), anyString());
	}

	@Test
	public void updateBenefitGroupNameTestForProspectTriNetStrategy() {
		long groupId = 1111;
		String grpName = "Group 1";
		BenefitGroup mockSTDGroup = prepareSTDGroup();
		Strategy strategy = new Strategy();
		strategy.setId(2222L);

		when(benefitGroupDao.findById(groupId)).thenReturn(mockSTDGroup);
		when(strategyService.getAllStrategies(1111L)).thenReturn(List.of(strategy));
		benefitGroupService.updateBenefitGroupName(strategy.getId(), groupId, grpName);
		verify(prospectGroupService, times(0)).updateGroupName(anyLong(), anyString());
		verify(cacheService, times(1)).invalidateCache("STRATEGY_DATA", "STRATEGY", "2222");
	}

	@Test
	public void updateBenefitGroupMetaDataTest() {
		String companyCode = "";
		Long groupId = 1111L;
		Long strategyId = 2222L;
		List<String> waitPeriods = Arrays.asList("NONE", "DOH");
		String waitPeriod = "NONE";
		boolean defaultFlag = true;
		long realmPlanYearId = 60;

		List<BenefitGroupStrategy> benGrpStrategies = null;

		/*
		 * Given defaultFlag is true
		 */
		Mockito.reset(benefitStrategyGroupDao);
		defaultFlag = true;
		Long groupId2 = 3333L;
		benGrpStrategies = new ArrayList<>();
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		bgs.setGroupId(groupId);
		BenefitGroupStrategy bgs1 = new BenefitGroupStrategy();
		bgs1.setGroupId(groupId2);
		benGrpStrategies.add(bgs);
		benGrpStrategies.add(bgs1);

		when(realmWaitPeriodService.getWaitPeriodCodesForRelamPlanYear(realmPlanYearId)).thenReturn(waitPeriods);
		Mockito.when(strategyGroupService.findByStrategyIdAndStatus(strategyId, "A")).thenReturn(benGrpStrategies);
		Mockito.when(benefitStrategyGroupDao.saveAll(Mockito.anyList())).thenReturn(null);
		Mockito.when(benefitStrategyGroupDao.saveAndFlush(any(BenefitGroupStrategy.class))).thenReturn(null);
		benefitGroupService.updateBenefitGroupMetaData(companyCode, groupId, strategyId, waitPeriod, defaultFlag,
				realmPlanYearId);
		verify(strategyGroupService, times(1)).findByStrategyIdAndStatus(strategyId, "A");
		verify(benefitStrategyGroupDao, times(1)).saveAll(benGrpStrategies);
		verify(benefitStrategyGroupDao, times(0)).saveAndFlush(any(BenefitGroupStrategy.class));

		assertEquals(waitPeriod, benGrpStrategies.get(0).getWaitingPeriod());
		assertEquals(groupId, (Long) benGrpStrategies.get(0).getGroupId());
		assertEquals(true, benGrpStrategies.get(0).isDefaultGroup());
		assertEquals(groupId2, (Long) benGrpStrategies.get(1).getGroupId());
		assertEquals(null, benGrpStrategies.get(1).getWaitingPeriod());
		assertEquals(false, benGrpStrategies.get(1).isDefaultGroup());

		/*
		 * Given defaultFlag is false
		 */
		Mockito.reset(benefitStrategyGroupDao);
		defaultFlag = false;
		benGrpStrategies = new ArrayList<>();
		bgs = new BenefitGroupStrategy();
		bgs.setGroupId(groupId);
		bgs1 = new BenefitGroupStrategy();
		bgs1.setGroupId(groupId2);
		benGrpStrategies.add(bgs);
		benGrpStrategies.add(bgs1);

		when(realmWaitPeriodService.getWaitPeriodCodesForRelamPlanYear(realmPlanYearId)).thenReturn(waitPeriods);
		Mockito.when(strategyGroupService.findByStrategyIdAndStatus(strategyId, "A")).thenReturn(benGrpStrategies);
		Mockito.when(benefitStrategyGroupDao.saveAndFlush(any(BenefitGroupStrategy.class))).thenReturn(null);
		benefitGroupService.updateBenefitGroupMetaData(companyCode, groupId, strategyId, waitPeriod, defaultFlag,
				realmPlanYearId);
		verify(strategyGroupService, times(2)).findByStrategyIdAndStatus(strategyId, "A");
		verify(benefitStrategyGroupDao, times(0)).save(any(BenefitGroupStrategy.class));
		verify(benefitStrategyGroupDao, times(1)).saveAndFlush(any(BenefitGroupStrategy.class));

		assertEquals(waitPeriod, benGrpStrategies.get(0).getWaitingPeriod());
		assertEquals(groupId, (Long) benGrpStrategies.get(0).getGroupId());
		assertEquals(false, benGrpStrategies.get(0).isDefaultGroup());
		assertEquals(groupId2, (Long) benGrpStrategies.get(1).getGroupId());
		assertEquals(null, benGrpStrategies.get(1).getWaitingPeriod());
		assertEquals(false, benGrpStrategies.get(1).isDefaultGroup());
	}

	@Test(expected = BSSApplicationException.class)
	public void updateBenefitGroupMetaDataTest_exceptionInvalidStrategyGroup() {

		String companyCode = "";
		Long groupId = 1111L;
		Long strategyId = 2222L;
		List<String> waitPeriods = Arrays.asList("NONE", "DOH");
		String waitPeriod = "NONE";
		boolean defaultFlag = true;
		long realmPlanYearId = 60;

		/*
		 * Given benGrpStrategies is null
		 */
		List<BenefitGroupStrategy> benGrpStrategies = null;

		when(realmWaitPeriodService.getWaitPeriodCodesForRelamPlanYear(realmPlanYearId)).thenReturn(waitPeriods);
		Mockito.when(strategyGroupService.findByStrategyIdAndStatus(strategyId, "A")).thenReturn(benGrpStrategies);
		benefitGroupService.updateBenefitGroupMetaData(companyCode, groupId, strategyId, waitPeriod, defaultFlag,
				realmPlanYearId);
		verify(strategyGroupService, times(1)).findByStrategyIdAndStatus(strategyId, "A");
		verify(benefitStrategyGroupDao, times(0)).saveAll(Mockito.anyList());
		verify(benefitStrategyGroupDao, times(0)).saveAndFlush(any(BenefitGroupStrategy.class));
	}

	@Test(expected = BSSApplicationException.class)
	public void updateBenefitGroupMetaDataTest_exceptionInvalidWaitPeriod() {

		String companyCode = "";
		Long groupId = 1111L;
		Long strategyId = 2222L;
		List<String> waitPeriods = Arrays.asList("NONE", "DOH");
		String waitPeriod = "Wait Period";
		boolean defaultFlag = true;
		long realmPlanYearId = 60;

		/*
		 * Given invalid wait period
		 */
		Mockito.reset(benefitStrategyGroupDao);
		defaultFlag = false;
		List<BenefitGroupStrategy> benGrpStrategies = new ArrayList<>();
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		bgs.setGroupId(groupId);
		BenefitGroupStrategy bgs1 = new BenefitGroupStrategy();
		Long groupId2 = 3333L;
		bgs1.setGroupId(groupId2);
		benGrpStrategies.add(bgs);
		benGrpStrategies.add(bgs1);
		when(realmWaitPeriodService.getWaitPeriodCodesForRelamPlanYear(realmPlanYearId)).thenReturn(waitPeriods);
		benefitGroupService.updateBenefitGroupMetaData(companyCode, groupId, strategyId, waitPeriod, defaultFlag,
				realmPlanYearId);
		verify(strategyGroupService, times(1)).findByStrategyIdAndStatus(strategyId, "A");
		verify(benefitStrategyGroupDao, times(0)).saveAll(Mockito.anyList());
		verify(benefitStrategyGroupDao, times(0)).saveAndFlush(any(BenefitGroupStrategy.class));
	}

	@Test
	public void updateBenefitGroupNameSTDTest() {
		long id = 1111L;
		String newName = "NewGroupName";
		BenefitGroup mockSTDGroup = prepareSTDGroup();
		when(benefitGroupDao.findById(id)).thenReturn(mockSTDGroup);
		benefitGroupService.updateBenefitGroupName(id, newName);
		verify(benefitGroupDao, times(1)).save(mockSTDGroup);
		assertEquals(newName, mockSTDGroup.getName());
	}

	@Test
	public void k1_group_name_must_start_with_k1() {
		long id = 1111L;
		String newName = "NewGroupName";
		BenefitGroup mockK1Group = prepareK1Group();
		when(benefitGroupDao.findById(id)).thenReturn(mockK1Group);
		Exception e = assertThrows(BSSApplicationException.class, () -> benefitGroupService.updateBenefitGroupName(id, newName));
        assertEquals(e.getMessage(), "Name must start with K1");
	}

	/**
	 * When isDefaultGroup = true;
	 */
	@Test
	public void getBenefitGroupMetaData1() {
		boolean isDefaultGroup = true;
		String rateTblID = "11111";
		String nextEligRulesId = "2222";
		String benefitProgram = "Benefit Program";
		Company company = new Company();
		company.setId(1111L);
		company.setBenefitProgram(benefitProgram);
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setDefaultGroup(isDefaultGroup);
		Mockito.when(nextRateTblID.execute()).thenReturn(rateTblID);
		Mockito.when(spGetNextEligRulesId.execute()).thenReturn(nextEligRulesId);
		benefitGroupService.getBenefitGroupMetaData(company, benefitGroup);
		verify(nextBenProgram, times(0)).execute();
		assertEquals(benefitProgram, benefitGroup.getBenefitProgram());
		assertEquals(nextEligRulesId, benefitGroup.getEligRuleId());
	}

	/**
	 * When isDefaultGroup = false;
	 */
	@Test
	public void getBenefitGroupMetaData2() {
		boolean isDefaultGroup = false;
		String rateTblID = "11111";
		String nextEligRulesId = "2222";
		String benefitProgram = "Benefit Program";
		Company company = new Company();
		company.setId(1111L);
		BenefitGroup benefitGroup = new BenefitGroup();
		benefitGroup.setDefaultGroup(isDefaultGroup);
		Mockito.when(nextBenProgram.execute()).thenReturn(benefitProgram);
		Mockito.when(nextRateTblID.execute()).thenReturn(rateTblID);
		Mockito.when(spGetNextEligRulesId.execute()).thenReturn(nextEligRulesId);
		benefitGroupService.getBenefitGroupMetaData(company, benefitGroup);
		verify(nextBenProgram, times(1)).execute();
		assertEquals(benefitProgram, benefitGroup.getBenefitProgram());
		assertEquals(nextEligRulesId, benefitGroup.getEligRuleId());
	}

	@Test
	public void generateRateTableIdsNonMedical() {
		String rateTblID = "rateTblId";
		Map<String, String> rateTableIds = new HashMap<>();
		Mockito.when(nextRateTblID.execute()).thenReturn(rateTblID);
		benefitGroupService.generateRateTableIdsNonMedical(rateTableIds);
		verify(nextRateTblID, times(2)).execute();
		assertEquals(rateTblID, rateTableIds.get("15"));
		assertEquals(rateTblID, rateTableIds.get("OTHER"));
	}

	@Test
	public void getBenefitProgramHeadCount() {
		long companyId = 1111;
		String status = "A";
		List<BenefitGroup> bgs = new ArrayList<>();
		BenefitGroup bg = new BenefitGroup();
		bg.setBenefitProgram("benefitProgram");
		bg.setHeadcount(2);
		bgs.add(bg);
		when(benefitGroupDao.findByCompanyIdAndStatus(companyId, status)).thenReturn(bgs);
		Map<String, Integer> actualResult = benefitGroupService.getBenefitProgramHeadCount(companyId, status);
		verify(benefitGroupDao, times(1)).findByCompanyIdAndStatus(companyId, status);
		assertEquals(1, actualResult.size());
		assertEquals(2, actualResult.get("benefitProgram").intValue());
	}

	/*
	 * When no source ben strategy group found throw exception.
	 */
	@Test(expected = BSSApplicationException.class)
	public void addGroup1() {
		long strategyGrpId = 2222;
		Company company = new Company();
		GroupData groupData = new GroupData();
		groupData.setSourceStrategyGroupId(strategyGrpId);
		long strategyId = 1111;
		benefitGroupService.addGroup(company, groupData, strategyId);
	}

	@Test
	public void addGroup2() {
		long strategyGrpId = 2222;
		Company company = new Company();
		company.setCode("AAA");
		GroupData groupData = new GroupData();
		groupData.setSourceStrategyGroupId(strategyGrpId);
		long strategyId = 1111;
		long groupId = 3333;
		long psId = 4444;
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setId(groupId);
		bgs.setBenefitGroup(benGrp);
		List<PlanSelection> planList = new ArrayList<>();
		PlanSelection ps = new PlanSelection();
		ps.setBenefitPlan("benPlan");
		ps.setId(psId);
		planList.add(ps);
		List<PlanSelection> newPlanList = new ArrayList<>();
		PlanSelection ps1 = new PlanSelection();
		ps1.setBenefitPlan("benPlan");
		newPlanList.add(ps1);
		List<Contribution> contributions = new ArrayList<>();
		Contribution contribution = new Contribution();
		contributions.add(contribution);
		when(strategyGroupService.getBenefitGroupStrategyBy(strategyGrpId, strategyId)).thenReturn(bgs);
		when(benefitGroupDao.saveAndFlush(any(BenefitGroup.class))).thenReturn(benGrp);
		when(strategyGroupService.saveBenefitGroupStrategy(any(BenefitGroupStrategy.class))).thenReturn(bgs);
		when(planSelectionService.getPlansByStrategyIdGroupId(strategyId, groupId)).thenReturn(planList);
		when(contributionService.getContributions(psId)).thenReturn(contributions);
		doNothing().when(contributionService).saveAll(any());
		benefitGroupService.addGroup(company, groupData, strategyId);
		verify(planSelectionService, times(1)).saveAll(Mockito.anyList());
		verify(contributionService, times(1)).saveAll(Mockito.anyList());
	}

    @Test
    public void addGroup3() {
        //given
        long strategyGrpId = 2222;
        Company company = new Company();
        company.setCode("AAA");
        company.setId(123);
        GroupData groupData = new GroupData();
        groupData.setDestGroupName("W2 MA");
        groupData.setSourceStrategyGroupId(strategyGrpId);
        long strategyId = 1111;
        long groupId = 12345L;
        long psId = 4444;
        BenefitGroupStrategy bgs = new BenefitGroupStrategy();
        BenefitGroup benGrp = new BenefitGroup();
        benGrp.setId(groupId);
        bgs.setBenefitGroup(benGrp);
        List<PlanSelection> planList = new ArrayList<>();
        PlanSelection ps = new PlanSelection();
        ps.setBenefitPlan("benPlan");
        ps.setId(psId);
        planList.add(ps);
        List<PlanSelection> newPlanList = new ArrayList<>();
        PlanSelection ps1 = new PlanSelection();
        ps1.setBenefitPlan("benPlan");
        newPlanList.add(ps1);
        List<Contribution> contributions = new ArrayList<>();
        Contribution contribution = new Contribution();
        contributions.add(contribution);
        List<BenefitGroup> group = prepareBenGroups();
        group.add(prepareW2MAGroup());
        //method mocks
        when(strategyGroupService.getBenefitGroupStrategyBy(strategyGrpId, strategyId)).thenReturn(bgs);
        when(strategyGroupService.saveBenefitGroupStrategy(any(BenefitGroupStrategy.class))).thenReturn(bgs);
        when(planSelectionService.getPlansByStrategyIdGroupId(strategyId, groupId)).thenReturn(planList);
        when(contributionService.getContributions(psId)).thenReturn(contributions);
        when(benefitGroupDao.findByCompanyIdAndStatus(123, BSSApplicationConstants.STATUS_ACTIVE)).thenReturn(group);
        doNothing().when(contributionService).saveAll(any());
        long id = benefitGroupService.addGroup(company, groupData, strategyId);
        //verify
        verify(planSelectionService, times(1)).saveAll(Mockito.anyList());
        verify(contributionService, times(1)).saveAll(Mockito.anyList());
        assertEquals(12345L, id);

    }

	@Test(expected = BSSApplicationException.class)
	public void deleteGroup1() {
		long strategyGrpId = 1111;
		long strategyId = 2222;

		benefitGroupService.deleteGroup(strategyGrpId, strategyId);
	}

	@Test
	public void deleteGroup2() {
		long strategyGrpId = 1111;
		long strategyId = 1111;
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		bgs.setStatus("A");
		bgs.setGroupId(strategyGrpId); // Set groupId to avoid NPE
		bgs.setStrategyId(strategyId); // Set strategyId to avoid NPE
		when(strategyGroupService.getBenefitGroupStrategyBy(strategyGrpId, strategyId)).thenReturn(bgs);
		when(strategyGroupService.saveBenefitGroupStrategy(bgs)).thenReturn(bgs);
		benefitGroupService.deleteGroup(strategyGrpId, strategyId);
		verify(strategyGroupService, times(1)).getBenefitGroupStrategyBy(strategyGrpId, strategyId);
		verify(strategyGroupService, times(1)).saveBenefitGroupStrategy(bgs);
		assertEquals("P", bgs.getStatus());
	}

	/* For Prospect Current Strategy */
	@Test
	public void deleteGroup3() {
		long strategyGrpId = 111;
		long strategyId = ProspectConstants.PROSPECT_STRATEGY_ID;
		doNothing().when(prospectGroupService).deleteBenefitGroup(strategyGrpId);
		benefitGroupService.deleteGroup(strategyGrpId, strategyId);
		verify(prospectGroupService, times(1)).deleteBenefitGroup(strategyGrpId);
		verify(strategyGroupService, times(0)).getBenefitGroupStrategyBy(anyLong(), anyLong());
		verify(strategyGroupService, times(0)).saveBenefitGroupStrategy(any());
	}

	@Test
	public void addMandatoryBenefitGroups() {
		Company company = new Company();
		company.setBenefitProgram(BEN_PROGRAM_1);
		List<GroupRuleDto> mandatoryGroupRules = prepaMandatoryGroupRules();
		List<BenefitGroup> benefitGroups = prepareBenGroups();
		Map<String, String> waitPeriodMap = new HashMap<>();
		waitPeriodMap.put(BEN_PROGRAM_1, "First Day of First Month");
		when(groupRuleService.getApplicableGroups(company, true)).thenReturn(mandatoryGroupRules);
		when(nextBenProgram.execute()).thenReturn("1");
		benefitGroupService.addMandatoryBenefitGroups(company, benefitGroups, waitPeriodMap);

	}

	@Test
	public void updateBenefitGroupStatus() {

		List<BenefitGroup> benefitGroups = prepareBenGroups();

		benefitGroupService.updateBenefitGroupStatus(benefitGroups.get(0), BSSApplicationConstants.STATUS_DELETED);

		verify(benefitGroupDao, times(1)).saveAndFlush(any());
	}

	private List<GroupRuleDto> prepaMandatoryGroupRules() {
		List<GroupRuleDto> dtos = new ArrayList<>();
		GroupRuleDto dto = new GroupRuleDto();
		dto.setGroupType("K1");
		dtos.add(dto);
		return dtos;
	}

	private List<BenefitGroup> prepareBenGroups() {
		List<BenefitGroup> groups = new ArrayList<>();
		groups.add(prepareK1Group());
		groups.add(prepareSTDGroup());
		return groups;
	}

	private BenefitGroup prepareSTDGroup() {
		BenefitGroup grp = new BenefitGroup();
		grp.setType("STD");
		grp.setName("Workers");
		grp.setState("MA");
		grp.setBenefitProgram(BEN_PROGRAM_2);
		grp.setCompanyId(1111L);
		return grp;
	}

	private BenefitGroup prepareK1Group() {
		BenefitGroup grp = new BenefitGroup();
		grp.setType("K1");
		grp.setName("Directors");
		grp.setState("MA");
		grp.setBenefitProgram(BEN_PROGRAM_1);
		return grp;
	}

	@Test
	public void getBenefitProgramsForStrategy() {
		Company company = new Company();
		company.setCode("0010z00001aloe4AAA");
		when(employeeBenefitGroupDao.getEmployeeStrategyGroupDetails(company.getCode()))
				.thenReturn(getEmployeeStrategyGroupDetails());
		List<String> groupDescByStrategy = benefitGroupService.getBenefitProgramsForStrategy(company.getCode(),
				319808L);
		assertTrue(groupDescByStrategy.containsAll(List.of(BEN_PROGRAM_1, BEN_PROGRAM_2)));
	}

	/**
	 * Given a prospect company, When constructK1Group is called, Then the returned
	 * BenefitGroup should have type "K1", be default.
	 */
	@Test
	public void testConstructK1Group_ProspectCompany() {
		Company company = new Company();
		company.setProspectCompany(true);
		BenefitGroup group = benefitGroupService.constructK1Group(company);
		assertEquals("K1", group.getType());
		assertFalse(group.isDefaultGroup());
		assertNotNull(group.getBenefitProgram());
	}

	/**
	 * Given a non-prospect company, When constructK1Group is called, Then the
	 * returned BenefitGroup should have type "K1", be default, and have expected
	 * program, eligRuleId, and eligConfig1.
	 */
	@Test
	public void testConstructK1Group_NonProspectCompany() {
		Company company = new Company();
		company.setId(1L);
		company.setProspectCompany(false);
		when(nextBenProgram.execute()).thenReturn("101");
		when(spGetNextEligRulesId.execute()).thenReturn("102");
		when(benefitClassService.generateClassCode(any(), any())).thenReturn("Test");
		BenefitGroup group = benefitGroupService.constructK1Group(company);
		assertEquals("K1", group.getType());
		assertFalse(group.isDefaultGroup());
		assertEquals("101", group.getBenefitProgram());
		assertEquals("102", group.getEligRuleId());
		assertEquals("Test", group.getEligConfig1());
	}

    /**
     * Given a prospect company, When constructMAGroup is called, Then the returned BenefitGroup
     * should have type "MA", be default.
     */
    @Test
    public void testConstructMAGroup_ProspectCompany() {
        Company company = new Company();
        company.setProspectCompany(true);
        BenefitGroup group = benefitGroupService.constructMAGroup(company);
        assertEquals("STD", group.getType());
        assertEquals("W2 MA", group.getName());
        assertFalse(group.isDefaultGroup());
        assertNotNull(group.getBenefitProgram());
    }

    /**
     * Given a non-prospect company, When constructMAGroup is called, Then the returned BenefitGroup
     * should have type "MA", be default, and have expected program, eligRuleId, and eligConfig1.
     */
    @Test
    public void testConstructMAGroup_NonProspectCompany() {
        Company company = new Company();
        company.setId(1L);
        company.setProspectCompany(false);
        when(nextBenProgram.execute()).thenReturn("101");
        when(spGetNextEligRulesId.execute()).thenReturn("102");
        when(benefitClassService.generateClassCode(any(), any())).thenReturn("Test");
        BenefitGroup group = benefitGroupService.constructMAGroup(company);
        assertEquals("STD", group.getType());
        assertEquals("W2 MA", group.getName());
        assertFalse(group.isDefaultGroup());
        assertEquals("101", group.getBenefitProgram());
        assertEquals("102", group.getEligRuleId());
        assertEquals("Test", group.getEligConfig1());
    }

	/**
	 * Given a prospect company, When constructW2Group is called, Then the returned
	 * BenefitGroup should have name "W2", be default.
	 */
	@Test
	public void testConstructW2Group_ProspectCompany() {
		Company company = new Company();
		company.setProspectCompany(true);
		BenefitGroup group = benefitGroupService.constructW2Group(company, true);
		assertEquals("W2", group.getName());
		assertTrue(group.isDefaultGroup());
		assertNotNull(group.getBenefitProgram());
	}

	/**
	 * Given a non-prospect company, When constructW2Group is called, Then the
	 * returned BenefitGroup should have name "W2", not be default, and have
	 * expected program, eligRuleId, and eligConfig1.
	 */
	@Test
	public void testConstructW2Group_NonProspectCompany() {
		Company company = new Company();
		company.setId(2L);
		company.setProspectCompany(false);
		when(nextBenProgram.execute()).thenReturn("101");
		when(spGetNextEligRulesId.execute()).thenReturn("102");
		when(benefitClassService.generateClassCode(any(), any())).thenReturn("Test");
		BenefitGroup group = benefitGroupService.constructW2Group(company, false);
		assertEquals("W2", group.getName());
		assertFalse(group.isDefaultGroup());
		assertEquals("101", group.getBenefitProgram());
		assertEquals("102", group.getEligRuleId());
		assertEquals("Test", group.getEligConfig1());
	}

	/**
	 * Given a BenefitGroup with required fields, When
	 * updateBenefitGroupWithProgramAndRates is called, Then the benefit program
	 * should be updated.
	 */
	@Test
	public void testUpdateGroupWithPSDetails_FieldsAlreadySet() {
		BenefitGroup bg = new BenefitGroup();
		bg.setEligRuleId("101");
		bg.setEligConfig1("102");
		bg.getGroupRate().add(new com.trinet.ambis.persistence.model.GroupRate());
		Company company = new Company();
		when(nextBenProgram.execute()).thenReturn("BEN_PROG4");
		BenefitGroup result = benefitGroupService.updateGroupWithPSDetails(bg, company, false);
		assertEquals("BEN_PROG4", result.getBenefitProgram());
		assertEquals("101", result.getEligRuleId());
		assertEquals("102", result.getEligConfig1());
	}

    @Test
    public void updateBenefitGroupTypeTest() {
        long groupId = 1111L;
        long strategyId = 1000L;
        String companyCode = "G28";

        String groupType = "K1";
        BenefitGroup mockK1Group = prepareK1Group();
        when( benefitGroupDao.findById( groupId )).thenReturn( mockK1Group );
        benefitGroupService.updateBenefitGroupType(strategyId, groupId, companyCode);

        verify( benefitGroupDao, times(1) ).save( mockK1Group );
        assertEquals( groupType, mockK1Group.getType() );
    }

	private Map<String, List<EmployeeCensusStrategyGroupDetails>> getEmployeeStrategyGroupDetails() {
		Map<String, List<EmployeeCensusStrategyGroupDetails>> strategyGroupDetails = new HashMap<>();
		EmployeeCensusStrategyGroupDetails employeeCensusStrategyGroupDetails = EmployeeCensusStrategyGroupDetails
				.builder().emplId("00001aloe4AAA-E1").benefitProgram(BEN_PROGRAM_1).strategyId(319808).build();
		EmployeeCensusStrategyGroupDetails employeeCensusStrategyGroupDetails2 = EmployeeCensusStrategyGroupDetails
				.builder().emplId("00001aloe4AAA-E2").benefitProgram(BEN_PROGRAM_1).strategyId(319808).build();
		EmployeeCensusStrategyGroupDetails employeeCensusStrategyGroupDetails3 = EmployeeCensusStrategyGroupDetails
				.builder().emplId("00001aloe4AAA-E3").benefitProgram(BEN_PROGRAM_2).strategyId(319808).build();
		strategyGroupDetails.put("00001aloe4AAA-E1", List.of(employeeCensusStrategyGroupDetails));
		strategyGroupDetails.put("00001aloe4AAA-E2", List.of(employeeCensusStrategyGroupDetails2));
		strategyGroupDetails.put("00001aloe4AAA-E3", List.of(employeeCensusStrategyGroupDetails3));
		return strategyGroupDetails;
	}

    private BenefitGroup prepareW2MAGroup() {
        BenefitGroup grp = new BenefitGroup();
        grp.setId(12345);
        grp.setType("STD");
        grp.setName("W2 MA");
        grp.setState("MA");
        grp.setBenefitProgram(BEN_PROGRAM_1);
        return grp;
    }

}
