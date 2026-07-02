package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.HeadCountBenefitPlan;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.prospect.impl.ProspectPlanHeadCountServiceImpl;
import com.trinet.ambis.util.BssCoreServiceClient;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author schaudhari
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProspectPlanHeadCountServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	ProspectPlanHeadCountServiceImpl prospectPlanHeadCountService;

	@Mock
	EmployeeDataService employeeDataService;
	@Mock
	ProspectCensusService prospectCensusService;
	@Mock
	PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	@Mock
	BenefitGroupService benefitGroupService;
	@Mock
	BssCoreServiceClient bssCoreServiceClient;

	RealmPlanYear realmPlanYear;
	Company company;
	long strategyId;
	long realmYrId;

	private static final String BENEFIT_PROGRAM_1 = "001JTW";
	private static final String BENEFIT_PROGRAM_2 = "001JVD";
	private static final String BENEFIT_PROGRAM_3 = "001MC5";
	private static final String BENEFIT_PROGRAM_4 = "001JTX";
    private MockedStatic<RulesAndConfigsUtils> rulesAndConfigsUtilsMockedStatic;

    @After
    public void tearDown() {
        rulesAndConfigsUtilsMockedStatic.close();
    }

	@Before
	public void setup() {
        rulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(RulesAndConfigsUtils.class);
		realmYrId = 21;

		realmPlanYear = new RealmPlanYear();
		realmPlanYear.setRealmId(realmYrId);
		realmPlanYear.setId(realmYrId);

		company = new Company();
		company.setCode("PROSPECT-TEST-COMPANY-1");
		company.setRealmPlanYear(realmPlanYear);
		company.setPlanStartDate("01-JAN-2020");
	}

	@Test
	public void getBenefitProgramHeadCountPlans() {
		long strategyId = 123L;
		company.setHeadQuatersState("NJ");
		List<BenefitGroup> benGrps = prepareBenGroups();
		List<ProspectCensusResponse> prospectCensusResponse = prepareCensusResponseObjs();

		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = prepareBenefitProgramHeadCountMap();

		when(RulesAndConfigsUtils.getSDIStates(Mockito.anyLong())).thenReturn(Set.of("NJ", "NY"));
		when(portfolioHeadCountDataDao.getProspectHeadCountPlans(strategyId)).thenReturn(benefitProgramHeadCounts);
		when(prospectCensusService.getProspectCensus("PROSPECT-TEST-COMPANY-1")).thenReturn(prospectCensusResponse);
		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(benGrps);
		when(employeeDataService.getEmployeesData(company, strategyId)).thenReturn(prepareEmployeeData());

		List<BenefitProgramHeadCountPlans> actualResult = prospectPlanHeadCountService
				.getBenefitProgramHeadCountPlans(company, strategyId);
		assertEquals(4, actualResult.size());
		for (BenefitProgramHeadCountPlans benefitProgramHeadCountPlans : actualResult) {
			if(BENEFIT_PROGRAM_1.equals(benefitProgramHeadCountPlans.getBenefitProgram())) {
				assertEquals(1, benefitProgramHeadCountPlans.getAdBenefitPlans().get("primaryHeadcount").intValue());
				assertEquals(0, benefitProgramHeadCountPlans.getAdBenefitPlans().get("secondaryHeadcount").intValue());
				assertEquals(1, benefitProgramHeadCountPlans.getAdBenefitPlans().get("totalHeadcount").intValue());
			}
			if(BENEFIT_PROGRAM_2.equals(benefitProgramHeadCountPlans.getBenefitProgram())) {
				assertEquals(0, benefitProgramHeadCountPlans.getAdBenefitPlans().get("primaryHeadcount").intValue());
				assertEquals(1, benefitProgramHeadCountPlans.getAdBenefitPlans().get("secondaryHeadcount").intValue());
				assertEquals(1, benefitProgramHeadCountPlans.getAdBenefitPlans().get("totalHeadcount").intValue());
			}
			if(BENEFIT_PROGRAM_3.equals(benefitProgramHeadCountPlans.getBenefitProgram())) {
				assertEquals(1, benefitProgramHeadCountPlans.getAdBenefitPlans().get("primaryHeadcount").intValue());
				assertEquals(1, benefitProgramHeadCountPlans.getAdBenefitPlans().get("secondaryHeadcount").intValue());
				assertEquals(2, benefitProgramHeadCountPlans.getAdBenefitPlans().get("totalHeadcount").intValue());
			}
			if(BENEFIT_PROGRAM_4.equals(benefitProgramHeadCountPlans.getBenefitProgram())) {
				assertEquals(0, benefitProgramHeadCountPlans.getAdBenefitPlans().get("primaryHeadcount").intValue());
				assertEquals(0, benefitProgramHeadCountPlans.getAdBenefitPlans().get("secondaryHeadcount").intValue());
				assertEquals(0, benefitProgramHeadCountPlans.getAdBenefitPlans().get("totalHeadcount").intValue());
			}
		}

	}
	
	
	
	@Test
	public void getProspectEligibleEmployeeCount_whenDisabledBundledOn() {
		long strategyId = 123L;
		company.setHeadQuatersState("NJ");

		List<BenefitGroup> benGrps = prepareBenGroups();
		List<ProspectCensusResponse> prospectCensusResponse = prepareCensusResponseObjs();
		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = prepareBenefitProgramHeadCountMap();

		when(RulesAndConfigsUtils.getSDIStates(Mockito.anyLong())).thenReturn(Set.of("NJ", "NY"));
		when(RulesAndConfigsUtils.isDisabledBundledOn(realmYrId)).thenReturn(true);
		when(portfolioHeadCountDataDao.getProspectHeadCountPlans(strategyId)).thenReturn(benefitProgramHeadCounts);
		when(prospectCensusService.getProspectCensus("PROSPECT-TEST-COMPANY-1")).thenReturn(prospectCensusResponse);
		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(benGrps);
		when(employeeDataService.getEmployeesData(company, strategyId)).thenReturn(prepareEmployeeData());

		Map<String, ActiveEligibleEECount> actualResult = prospectPlanHeadCountService
				.getProspectEligibleEmployeeCount(company, strategyId);

		assertEquals(4, actualResult.size());
		ActiveEligibleEECount benProg1ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_1);
		assertEquals(1, benProg1ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg1ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg1ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg2ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_2);
		assertEquals(0, benProg2ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(1, benProg2ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg2ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg3ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_3);
		assertEquals(1, benProg3ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(1, benProg3ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg3ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg4ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_4);
		assertEquals(0, benProg4ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg4ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg4ActiveEligibleEECount.getTotalHeadCount());
	}
	
	
	@Test
	public void getOnboardingClientEligibleEmployeeCount_whenDisabledBundledOn() {
		long strategyId = 123L;
		company.setHeadQuatersState("NJ");
		company.setProspectConvertedClient(true);

		List<BenefitGroup> benGrps = prepareBenGroups();
		List<ProspectCensusResponse> prospectCensusResponse = prepareCensusResponseObjs();
		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = prepareBenefitProgramHeadCountMap();

		when(RulesAndConfigsUtils.getSDIStates(Mockito.anyLong())).thenReturn(Set.of("NJ", "NY"));
		when(RulesAndConfigsUtils.isDisabledBundledOn(realmYrId)).thenReturn(true);
		when(portfolioHeadCountDataDao.getProspectHeadCountPlans(strategyId)).thenReturn(benefitProgramHeadCounts);
		when(bssCoreServiceClient.getCensusByCompanyCode("PROSPECT-TEST-COMPANY-1")).thenReturn(prospectCensusResponse);
		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(benGrps);
		when(employeeDataService.getEmployeesData(company, strategyId)).thenReturn(prepareEmployeeData());

		Map<String, ActiveEligibleEECount> actualResult = prospectPlanHeadCountService
				.getProspectEligibleEmployeeCount(company, strategyId);

		assertEquals(4, actualResult.size());
		ActiveEligibleEECount benProg1ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_1);
		assertEquals(1, benProg1ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg1ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg1ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg2ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_2);
		assertEquals(0, benProg2ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(1, benProg2ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg2ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg3ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_3);
		assertEquals(1, benProg3ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(1, benProg3ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg3ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg4ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_4);
		assertEquals(0, benProg4ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg4ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg4ActiveEligibleEECount.getTotalHeadCount());
	}
	
	@Test
	public void getProspectEligibleEmployeeCount_whenDisabledBundledOff() {
		long strategyId = 123L;
		company.setHeadQuatersState("NJ");

		List<BenefitGroup> benGrps = prepareBenGroups();
		List<ProspectCensusResponse> prospectCensusResponse = prepareCensusResponseObjs();
		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = prepareBenefitProgramHeadCountMap();

		when(RulesAndConfigsUtils.getSDIStates(Mockito.anyLong())).thenReturn(Set.of("NJ", "NY"));
		when(RulesAndConfigsUtils.isDisabledBundledOn(realmYrId)).thenReturn(false);
		when(portfolioHeadCountDataDao.getProspectHeadCountPlans(strategyId)).thenReturn(benefitProgramHeadCounts);
		when(prospectCensusService.getProspectCensus("PROSPECT-TEST-COMPANY-1")).thenReturn(prospectCensusResponse);
		when(benefitGroupService.getBenefitGroupByStrategy(strategyId, BSSApplicationConstants.STATUS_ACTIVE))
				.thenReturn(benGrps);
		when(employeeDataService.getEmployeesData(company, strategyId)).thenReturn(prepareEmployeeData());

		Map<String, ActiveEligibleEECount> actualResult = prospectPlanHeadCountService
				.getProspectEligibleEmployeeCount(company, strategyId);

		assertEquals(4, actualResult.size());
		ActiveEligibleEECount benProg1ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_1);
		assertEquals(0, benProg1ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg1ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(1, benProg1ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg2ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_2);
		assertEquals(0, benProg2ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg2ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(1, benProg2ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg3ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_3);
		assertEquals(0, benProg3ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg3ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(2, benProg3ActiveEligibleEECount.getTotalHeadCount());

		ActiveEligibleEECount benProg4ActiveEligibleEECount = actualResult.get(BENEFIT_PROGRAM_4);
		assertEquals(0, benProg4ActiveEligibleEECount.getPrimaryHeadCount());
		assertEquals(0, benProg4ActiveEligibleEECount.getSecondaryHeadCount());
		assertEquals(0, benProg4ActiveEligibleEECount.getTotalHeadCount());
	}

	private Set<EmployeeData> prepareEmployeeData() {
		EmployeeData ed1 = new EmployeeData();
		ed1.setEmplId("EMPL1");
		ed1.setBenefitProgram(BENEFIT_PROGRAM_1);
		EmployeeData ed2 = new EmployeeData();
		ed2.setEmplId("EMPL2");
		ed2.setBenefitProgram(BENEFIT_PROGRAM_2);
		EmployeeData ed3 = new EmployeeData();
		ed3.setEmplId("EMPL3");
		ed3.setBenefitProgram(BENEFIT_PROGRAM_3);
		EmployeeData ed4 = new EmployeeData();
		ed4.setEmplId("EMPL4");
		ed4.setBenefitProgram(BENEFIT_PROGRAM_3);
		return Set.of(ed1, ed2, ed3, ed4);
	}

	private List<ProspectCensusResponse> prepareCensusResponseObjs() {
		ProspectCensusResponse psr1 = ProspectCensusResponse.builder().employeeId("EMPL1").state("NY").build();
		ProspectCensusResponse psr2 = ProspectCensusResponse.builder().employeeId("EMPL2").state("CA").build();
		ProspectCensusResponse psr3 = ProspectCensusResponse.builder().employeeId("EMPL3").state("NJ").build();
		ProspectCensusResponse psr4 = ProspectCensusResponse.builder().employeeId("EMPL4").state("TX").build();
		return Arrays.asList(psr1, psr2, psr3, psr4);
	}

	private List<BenefitGroup> prepareBenGroups() {
		List<BenefitGroup> benGrps = new ArrayList<>();
		BenefitGroup benGrp = new BenefitGroup();
		benGrp.setBenefitProgram(BENEFIT_PROGRAM_1);
		benGrps.add(benGrp);

		benGrp = new BenefitGroup();
		benGrp.setBenefitProgram(BENEFIT_PROGRAM_4);
		benGrps.add(benGrp);
		return benGrps;
	}

	private Map<String, List<HeadCountBenefitPlan>> prepareBenefitProgramHeadCountMap() {

		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCountMap = new HashMap<>();
		List<HeadCountBenefitPlan> hcb = new ArrayList<>();
		HeadCountBenefitPlan bp = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder().benefitPlanId("MED_PLAN_1")
				.planType(CoverageCodesEnums.COV_EMPLOYEE.getCode()).planCarrierId(1).populateZeroCvgLvlHeadCounts(true)
				.build();
		hcb.add(bp);
		benefitProgramHeadCountMap.put(BENEFIT_PROGRAM_1, hcb);

		hcb = new ArrayList<>();
		bp = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder().benefitPlanId("MED_PLAN_2")
				.planType(CoverageCodesEnums.COV_EMPLOYEE.getCode()).planCarrierId(1).populateZeroCvgLvlHeadCounts(true)
				.build();
		hcb.add(bp);
		benefitProgramHeadCountMap.put(BENEFIT_PROGRAM_2, hcb);

		hcb = new ArrayList<>();
		bp = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder().benefitPlanId("MED_PLAN_1")
				.planType(CoverageCodesEnums.COV_EMPLOYEE.getCode()).planCarrierId(1).populateZeroCvgLvlHeadCounts(true)
				.build();
		hcb.add(bp);
		benefitProgramHeadCountMap.put(BENEFIT_PROGRAM_3, hcb);

		return benefitProgramHeadCountMap;
	}

}
