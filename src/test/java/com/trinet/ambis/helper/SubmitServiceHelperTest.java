package com.trinet.ambis.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategySummary;


@RunWith(JUnit4.class)
public class SubmitServiceHelperTest {

	@Mock
	BenefitGroupService benefitGroupService;
	
	@Mock
	StrategyGroupService benefitGroupStrategyService;
	
	private static final String PLAN_A = "AA1234";
	private static final String PLAN_B = "BB1234";
	private static final String PLAN_C = "CC1234";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getSelectedMedicalBenefitPlans() {
		BenefitGroup group = prepareBenGroup();
		Map<String, Set<String>> actualResult = SubmitServiceHelper.getSelectedBenefitPlans(group);
		assertEquals(2, actualResult.get("10").size());
		assertTrue(actualResult.get("10").contains("1111"));
		assertTrue(actualResult.get("10").contains("2222"));
	}
	
	@Test
	public void getAllBenOffersContributions() {
		BenefitGroup group = prepareBenGroup();
		List<Contribution> actualResult = SubmitServiceHelper.getAllBenOffersContributions(group);
		assertEquals(6, actualResult.size());
	}

	// When ALE amount is greater
	@Test
	public void setFPLForLowCostPpoPlan1() {
		List<Contribution> contributions = new ArrayList<>();
		Contribution contribution = new Contribution();
		BenefitPlan benefitPlanAssociation = new BenefitPlan();
		benefitPlanAssociation.setId(PLAN_A);
		contribution.setCoverageLevel("1");
		contribution.setBenefitPlanAssociation(benefitPlanAssociation);
		contribution.setEmployeeContribution(BigDecimal.valueOf(150));
		contribution.setEmployerContribution(BigDecimal.valueOf(250));
		contribution.setEmployerPercent(BigDecimal.valueOf(75));
		contribution.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		contributions.add(contribution);

		Map<String, String> fplPLansByRegion = new HashMap<>();
		fplPLansByRegion.put("MA", PLAN_A);

		Company company = new Company();
		company.setAleAmount(BigDecimal.valueOf(300));
		
		List<String> fplMedicalPlans = new ArrayList<>();
		fplMedicalPlans.add(PLAN_A);

		SubmitServiceHelper.setFPLForLowCostPpoPlan(contributions, fplPLansByRegion, company, fplMedicalPlans);

		assertEquals(BigDecimal.valueOf(150), contribution.getEmployeeContribution());
		assertEquals(BigDecimal.valueOf(250), contribution.getEmployerContribution());
		assertEquals(BigDecimal.valueOf(75), contribution.getEmployerPercent());
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_BASE, contribution.getOverrideType());
	}

//	When there is no matching low cost ppo plan present in region
	@Test
	public void setFPLForLowCostPpoPlan2() {
		List<Contribution> contributions = new ArrayList<>();
		Contribution contribution = new Contribution();
		BenefitPlan benefitPlanAssociation = new BenefitPlan();
		benefitPlanAssociation.setId(PLAN_A);
		contribution.setCoverageLevel("1");
		contribution.setBenefitPlanAssociation(benefitPlanAssociation);
		contribution.setEmployeeContribution(BigDecimal.valueOf(150));
		contribution.setEmployerContribution(BigDecimal.valueOf(250));
		contribution.setEmployerPercent(BigDecimal.valueOf(75));
		contribution.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		contributions.add(contribution);

		Map<String, String> fplPLansByRegion = new HashMap<>();
		fplPLansByRegion.put("MA", "BB4567");

		Company company = new Company();
		company.setAleAmount(BigDecimal.valueOf(100));

		SubmitServiceHelper.setFPLForLowCostPpoPlan(contributions, fplPLansByRegion, company, null);

		assertEquals(BigDecimal.valueOf(150), contribution.getEmployeeContribution());
		assertEquals(BigDecimal.valueOf(250), contribution.getEmployerContribution());
		assertEquals(BigDecimal.valueOf(75), contribution.getEmployerPercent());
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_BASE, contribution.getOverrideType());
	}

	// When contribution coverage level is other than employee
	@Test
	public void setFPLForLowCostPpoPlan3() {
		List<Contribution> contributions = new ArrayList<>();
		Contribution contribution = new Contribution();
		BenefitPlan benefitPlanAssociation = new BenefitPlan();
		benefitPlanAssociation.setId(PLAN_A);
		contribution.setCoverageLevel("2");
		contribution.setBenefitPlanAssociation(benefitPlanAssociation);
		contribution.setEmployeeContribution(BigDecimal.valueOf(150));
		contribution.setEmployerContribution(BigDecimal.valueOf(250));
		contribution.setEmployerPercent(BigDecimal.valueOf(75));
		contribution.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		contributions.add(contribution);

		Map<String, String> fplPLansByRegion = new HashMap<>();
		fplPLansByRegion.put("MA", "BB4567");

		Company company = new Company();
		company.setAleAmount(BigDecimal.valueOf(100));
		
		List<String> fplMedicalPlans = new ArrayList<>();
		fplMedicalPlans.add(PLAN_A);

		SubmitServiceHelper.setFPLForLowCostPpoPlan(contributions, fplPLansByRegion, company, fplMedicalPlans);

		assertEquals(BigDecimal.valueOf(150), contribution.getEmployeeContribution());
		assertEquals(BigDecimal.valueOf(250), contribution.getEmployerContribution());
		assertEquals(BigDecimal.valueOf(75), contribution.getEmployerPercent());
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_BASE, contribution.getOverrideType());
	}

//	When contribution coverage level is other than employee
	@Test
	public void setFPLForLowCostPpoPlan4() {
		List<Contribution> contributions = new ArrayList<>();
		Contribution contribution = new Contribution();
		BenefitPlan benefitPlanAssociation = new BenefitPlan();
		benefitPlanAssociation.setId(PLAN_A);
		contribution.setCoverageLevel("1");
		contribution.setBenefitPlanAssociation(benefitPlanAssociation);
		contribution.setEmployeeContribution(BigDecimal.valueOf(150));
		contribution.setEmployerContribution(BigDecimal.valueOf(250));
		contribution.setEmployerPercent(BigDecimal.valueOf(75));
		contribution.setOverrideType(BSSApplicationConstants.PLAN_OVERRIDE_BASE);
		contributions.add(contribution);

		Map<String, String> fplPLansByRegion = new HashMap<>();
		fplPLansByRegion.put("MA", PLAN_A);

		Company company = new Company();
		company.setAleAmount(BigDecimal.valueOf(100));
		
		List<String> fplMedicalPlans = new ArrayList<>();
		fplMedicalPlans.add(PLAN_A);

		SubmitServiceHelper.setFPLForLowCostPpoPlan(contributions, fplPLansByRegion, company, fplMedicalPlans);

		assertEquals(BigDecimal.valueOf(100), contribution.getEmployeeContribution());
		assertEquals(BigDecimal.valueOf(300), contribution.getEmployerContribution());
		assertEquals(new BigDecimal("75.0000000000"), contribution.getEmployerPercent());
		assertEquals(BSSApplicationConstants.PLAN_OVERRIDE_FPL, contribution.getOverrideType());
	}

	@Test
	public void findLowCostPpoPlanByRegion() {
		Map<String, List<String>> benefitPlansByRegion = prepareBenPlansByRegion();
		Map<String, List<Contribution>> benefitPlanContributions = prepareBenPlanContributions();

		Map<String, String> actualResult = SubmitServiceHelper.findLowCostPpoPlanByRegion(benefitPlansByRegion,
				benefitPlanContributions);

		assertEquals(2, actualResult.size());
		assertEquals(PLAN_C, actualResult.get("MA"));
		assertEquals(PLAN_B, actualResult.get("CA"));
	}

	@Test
	public void createMapOfContributions() {
		List<Contribution> contributions = new ArrayList<>();
		Contribution contribution = new Contribution();
		contribution.setId(1111);
		BenefitPlan benefitPlanAssociation = new BenefitPlan();
		benefitPlanAssociation.setId(PLAN_A);
		contribution.setBenefitPlanAssociation(benefitPlanAssociation);
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setId(2222);
		benefitPlanAssociation = new BenefitPlan();
		benefitPlanAssociation.setId(PLAN_A);
		contribution.setBenefitPlanAssociation(benefitPlanAssociation);
		contributions.add(contribution);

		contribution = new Contribution();
		contribution.setId(3333);
		benefitPlanAssociation = new BenefitPlan();
		benefitPlanAssociation.setId(PLAN_B);
		contribution.setBenefitPlanAssociation(benefitPlanAssociation);
		contributions.add(contribution);

		Map<String, List<Contribution>> actualResult = SubmitServiceHelper.createMapOfContributions(contributions);

		assertEquals(2, actualResult.size());
	}

	@Test
	public void updateBenefitGroupData() {
		StrategyData strategy = new StrategyData();
		List<StrategyBenefitGroup> benefitGroups = new ArrayList<>();

		StrategyBenefitGroup sbg = new StrategyBenefitGroup();
		String benefitProgram = PLAN_A;
		sbg.setBenefitProgram(benefitProgram);
		List<BenefitOffer> benefitOffers = new ArrayList<>();
		BenefitOffer benOffer = new BenefitOffer();
		benefitOffers.add(benOffer);
		sbg.setBenefitOffers(benefitOffers);
		benefitGroups.add(sbg);
		strategy.setBenefitGroups(benefitGroups);
		StrategySummary strategySummary = new StrategySummary();
		strategySummary.setId(1111L);
		strategy.setStrategySummary(strategySummary);

		List<BenefitGroupStrategy> benefitGroupStrategies = new ArrayList<>();
		BenefitGroupStrategy bgs = new BenefitGroupStrategy();
		bgs.setWaitingPeriod("waitPeriod");
		bgs.setHeadcount(2);
		bgs.setStatus("A");
		bgs.setDefaultGroup(true);
		BenefitGroup bg = new BenefitGroup();
		bg.setBenefitProgram(benefitProgram);
		bgs.setBenefitGroup(bg);
		benefitGroupStrategies.add(bgs);

		when(benefitGroupStrategyService.getBenefitGroupStrategy(1111L, "A")).thenReturn(benefitGroupStrategies);

		List<BenefitGroup> actualResult = SubmitServiceHelper.updateBenefitGroupData(benefitGroupStrategyService, strategy);

		assertEquals(1, actualResult.size());
		assertEquals("waitPeriod", actualResult.get(0).getWaitingPeriod());
		assertEquals(2, actualResult.get(0).getHeadcount());
		assertEquals("A", actualResult.get(0).getStatus());
		assertEquals(true, actualResult.get(0).isDefaultGroup());
		assertEquals(1, actualResult.get(0).getBenefitOffers().size());
	}

	@Test
	public void updateCompaniesBenefitProgram() {
		Company company = new Company();
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		BenefitGroup bg = new BenefitGroup();
		bg.setDefaultGroup(false);
		bg.setBenefitProgram(PLAN_A);
		benefitGroups.add(bg);

		bg = new BenefitGroup();
		bg.setDefaultGroup(true);
		bg.setBenefitProgram(PLAN_B);
		benefitGroups.add(bg);

		SubmitServiceHelper.updateCompaniesBenefitProgram(company, benefitGroups);

		assertEquals(PLAN_B, company.getBenefitProgram());

		company = new Company();
		benefitGroups = new ArrayList<>();

		SubmitServiceHelper.updateCompaniesBenefitProgram(company, benefitGroups);

		assertEquals(null, company.getBenefitProgram());
	}

	@Test(expected = InvocationTargetException.class)
	public void privateConstructorTest()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> constructor = SubmitServiceHelper.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		constructor.newInstance();
	}
	
	private BenefitGroup prepareBenGroup() {
		BenefitGroup group = new BenefitGroup();
		List<BenefitOffer> benOffers = new ArrayList<>();
		prepareMedBenOffers(benOffers);
		prepareDentalBenOffers(benOffers);
		prepareVisionBenOffers(benOffers);
		prepareAdditionalBenOffers(benOffers);

		group.setBenefitOffers(benOffers);
		return group;
	}

	private List<BenefitOffer> prepareMedBenOffers(List<BenefitOffer> benOffers) {
		List<BenefitPlan> benPlans = new ArrayList<>();
		BenefitOffer bo = new BenefitOffer();
		BenefitOfferSummary bos = new BenefitOfferSummary();
		bos.setType(BSSApplicationConstants.MEDICAL);
		bos.setDescription("10");
		bo.setSummary(bos);
		BenefitPlan bp = new BenefitPlan();
		bp.setId("1111");
		List<PlanContribution> contributions = new ArrayList<>();
		PlanContribution contribution = new PlanContribution();
		contribution.setType(CoverageCodesEnums.COV_EMPLOYEE.getId());
		contribution.setPlanCost(BigDecimal.valueOf(200));
		contribution.setEmployerPercent(BigDecimal.TEN);
		contributions.add(contribution);

		contribution = new PlanContribution();
		contribution.setType(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
		contribution.setPlanCost(BigDecimal.valueOf(175));
		contribution.setEmployerPercent(BigDecimal.TEN);
		contributions.add(contribution);

		bp.setContributions(contributions);

		benPlans.add(bp);

		bp = new BenefitPlan();
		bp.setId("2222");
		bp.setPlanType("10");
		contributions = new ArrayList<>();
		contribution = new PlanContribution();
		contribution.setType(CoverageCodesEnums.COV_EMPLOYEE.getId());
		contribution.setPlanCost(BigDecimal.valueOf(150));
		contribution.setEmployerPercent(BigDecimal.TEN);
		contributions.add(contribution);

		contribution = new PlanContribution();
		contribution.setType(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
		contribution.setPlanCost(BigDecimal.valueOf(100));
		contribution.setEmployerPercent(BigDecimal.TEN);
		contributions.add(contribution);

		bp.setContributions(contributions);

		benPlans.add(bp);

		bo.setBenefitPlans(benPlans);
		benOffers.add(bo);
		return benOffers;
	}

	private void prepareDentalBenOffers(List<BenefitOffer> benOffers) {
		List<BenefitPlan> benPlans = new ArrayList<>();
		BenefitOffer bo = new BenefitOffer();
		BenefitOfferSummary bos = new BenefitOfferSummary();
		bos.setType(BSSApplicationConstants.DENTAL);
		bos.setDescription("11");
		bo.setSummary(bos);
		BenefitPlan bp = new BenefitPlan();
		bp.setId("3333");
		bp.setPlanType("11");
		List<PlanContribution> contributions = new ArrayList<>();
		PlanContribution contribution = new PlanContribution();
		contribution.setType("employee");
		contribution.setPlanCost(BigDecimal.valueOf(140));
		contribution.setEmployerPercent(BigDecimal.TEN);
		contributions.add(contribution);
		bp.setContributions(contributions);
		benPlans.add(bp);
		bo.setBenefitPlans(benPlans);
		benOffers.add(bo);
	}
	
	private void prepareVisionBenOffers(List<BenefitOffer> benOffers) {
		List<BenefitPlan> benPlans = new ArrayList<>();
		BenefitOffer bo = new BenefitOffer();
		BenefitOfferSummary bos = new BenefitOfferSummary();
		bos.setType("vision");
		bos.setDescription("14");
		bo.setSummary(bos);
		BenefitPlan bp = new BenefitPlan();
		bp.setId("3333");
		bp.setPlanType("14");
		List<PlanContribution> contributions = new ArrayList<>();
		PlanContribution contribution = new PlanContribution();
		contribution.setType(CoverageCodesEnums.COV_EMPLOYEE.getId());
		contribution.setPlanCost(BigDecimal.valueOf(140));
		contribution.setEmployerPercent(BigDecimal.TEN);
		contributions.add(contribution);
		bp.setContributions(contributions);
		benPlans.add(bp);
		bo.setBenefitPlans(benPlans);
		benOffers.add(bo);
	}

	private void prepareAdditionalBenOffers(List<BenefitOffer> benOffers) {
		List<BenefitPlan> benPlans = new ArrayList<>();
		BenefitOffer bo = new BenefitOffer();
		BenefitOfferSummary bos = new BenefitOfferSummary();
		bos.setType(BSSApplicationConstants.ADDITIONAL);
		bo.setSummary(bos);
		BenefitPlan bp = new BenefitPlan();
		bp.setId("4444");
		List<PlanContribution> contributions = new ArrayList<>();
		PlanContribution contribution = new PlanContribution();
		contribution.setType(CoverageCodesEnums.COV_EMPLOYEE.getId());
		contribution.setPlanCost(BigDecimal.valueOf(225));
		contributions.add(contribution);
		bp.setContributions(contributions);
		benPlans.add(bp);
		bo.setBenefitPlans(benPlans);
		benOffers.add(bo);
	}

	private Map<String, List<String>> prepareBenPlansByRegion() {
		Map<String, List<String>> map = new HashMap<>();
		List<String> plans = new ArrayList<>();
		plans.add(PLAN_A);
		plans.add(PLAN_C);
		map.put("MA", plans);
		plans = new ArrayList<>();
		plans.add(PLAN_B);
		map.put("CA", plans);
		return map;
	}

	private Map<String, List<Contribution>> prepareBenPlanContributions() {
		Map<String, List<Contribution>> map = new HashMap<>();
		List<Contribution> contributions = new ArrayList<>();
		Contribution cont = new Contribution();
		BenefitPlan benPlanAssociation = new BenefitPlan();
		benPlanAssociation.setId(PLAN_A);
		benPlanAssociation.setPpoPlan(true);
		benPlanAssociation.setWidelyAvailablePlan(true);
		cont.setBenefitPlanAssociation(benPlanAssociation);
		cont.setCoverageLevel("1");
		cont.setEmployeeContribution(BigDecimal.valueOf(100));
		cont.setEmployerContribution(BigDecimal.valueOf(150));
		contributions.add(cont);

		cont = new Contribution();
		benPlanAssociation = new BenefitPlan();
		benPlanAssociation.setId(PLAN_A);
		benPlanAssociation.setPpoPlan(false);
		cont.setBenefitPlanAssociation(benPlanAssociation);
		cont.setCoverageLevel("1");
		cont.setEmployeeContribution(BigDecimal.valueOf(50));
		cont.setEmployerContribution(BigDecimal.valueOf(100));
		contributions.add(cont);

		cont = new Contribution();
		benPlanAssociation = new BenefitPlan();
		benPlanAssociation.setId(PLAN_A);
		benPlanAssociation.setPpoPlan(true);
		cont.setBenefitPlanAssociation(benPlanAssociation);
		cont.setCoverageLevel("2");
		cont.setEmployeeContribution(BigDecimal.valueOf(50));
		cont.setEmployerContribution(BigDecimal.valueOf(100));
		contributions.add(cont);

		map.put(PLAN_A, contributions);

		cont = new Contribution();
		benPlanAssociation = new BenefitPlan();
		benPlanAssociation.setId(PLAN_C);
		benPlanAssociation.setPpoPlan(true);
		cont.setBenefitPlanAssociation(benPlanAssociation);
		cont.setCoverageLevel("1");
		cont.setEmployeeContribution(BigDecimal.valueOf(80));
		cont.setEmployerContribution(BigDecimal.valueOf(100));
		contributions.add(cont);

		map.put(PLAN_C, contributions);

		cont = new Contribution();
		benPlanAssociation = new BenefitPlan();
		benPlanAssociation.setId(PLAN_B);
		benPlanAssociation.setPpoPlan(true);
		cont.setBenefitPlanAssociation(benPlanAssociation);
		cont.setCoverageLevel("1");
		cont.setEmployeeContribution(BigDecimal.valueOf(50));
		cont.setEmployerContribution(BigDecimal.valueOf(100));
		contributions.add(cont);

		map.put(PLAN_B, contributions);
		return map;
	}
}
