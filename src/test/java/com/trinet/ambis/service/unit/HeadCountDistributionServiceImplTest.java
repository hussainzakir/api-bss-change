package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.enums.IndustryType;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.service.impl.HeadCountDistributionServiceImpl;
import com.trinet.ambis.service.model.HeadCountData;
import com.trinet.ambis.service.model.Industry;

/**
 * @author schaudhari
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HeadCountDistributionServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	HeadCountDistributionServiceImpl headCountDistributionService;

	private static final long STRATEGY_GROUP_ID = 1111;

	/*
	 * When no plans present
	 */
	@Test
	public void planHeadCountDistribution1() {
		Company company = new Company();
		Industry industry = new Industry(1);
		industry.setIndustryType(IndustryType.FS);
		company.setIndustry(industry);
		List<PlanSelection> plans = Collections.emptyList();
		HeadCountData headCountData = prepareHeadCountMap();

		Map<String, Map<String, Integer>> actualResult = headCountDistributionService.planHeadCountDistribution(company,
				plans, headCountData);

		assertEquals(0, actualResult.size());
	}

	/*
	 * When only one plan present
	 */
	@Test
	public void planHeadCountDistribution2() {
		Company company = new Company();
		Industry industry = new Industry(1);
		industry.setIndustryType(IndustryType.FS);
		company.setIndustry(industry);
		List<PlanSelection> plans = preparePlanSelections();
		HeadCountData headCountData = prepareHeadCountMap();

		Map<String, Map<String, Integer>> actualResult = headCountDistributionService.planHeadCountDistribution(company,
				plans, headCountData);

		assertEquals(1, actualResult.size());
		assertEquals(2, actualResult.get("BenPlan1").get("1").intValue());
		assertEquals(1, actualResult.get("BenPlan1").get("2").intValue());
		assertEquals(4, actualResult.get("BenPlan1").get("C").intValue());
		assertEquals(0, actualResult.get("BenPlan1").get("4").intValue());
	}
	
	/*
	 * When there are multiple plans present
	 */
	@Test
	public void planHeadCountDistribution3() {
		Company company = new Company();
		Industry industry = new Industry(1);
		industry.setIndustryType(IndustryType.FS);
		company.setIndustry(industry);
		List<PlanSelection> plans = prepareMultiplePlanSelections();
		HeadCountData headCountData = prepareHeadCountMap();

		Map<String, Map<String, Integer>> actualResult = headCountDistributionService.planHeadCountDistribution(company,
				plans, headCountData);

		assertEquals(3, actualResult.size());
		assertEquals(1, actualResult.get("BenPlan1").get("1").intValue());
		assertEquals(0, actualResult.get("BenPlan1").get("2").intValue());
		assertEquals(1, actualResult.get("BenPlan1").get("C").intValue());
		assertEquals(0, actualResult.get("BenPlan1").get("4").intValue());
		
		assertEquals(1, actualResult.get("BenPlan2").get("1").intValue());
		assertEquals(1, actualResult.get("BenPlan2").get("2").intValue());
		assertEquals(2, actualResult.get("BenPlan2").get("C").intValue());
		assertEquals(0, actualResult.get("BenPlan2").get("4").intValue());
		
		assertEquals(0, actualResult.get("BenPlan3").get("1").intValue());
		assertEquals(0, actualResult.get("BenPlan3").get("2").intValue());
		assertEquals(1, actualResult.get("BenPlan3").get("C").intValue());
		assertEquals(0, actualResult.get("BenPlan3").get("4").intValue());
	}

	private List<PlanSelection> preparePlanSelections() {
		List<PlanSelection> plans = new ArrayList<PlanSelection>();
		PlanSelection ps1 = new PlanSelection();
		ps1.setBenefitPlan("BenPlan1");
		List<Contribution> contributions = new ArrayList<Contribution>();
		Contribution contribution1 = new Contribution();
		contribution1.setCoverageLevel("1");
		contribution1.setPlanCost(BigDecimal.valueOf(380));
		contributions.add(contribution1);
		Contribution contribution2 = new Contribution();
		contribution2.setCoverageLevel("C");
		contribution2.setPlanCost(BigDecimal.valueOf(500));
		contributions.add(contribution2);
		ps1.setContributions(contributions);
		plans.add(ps1);

		return plans;
	}
	
	private List<PlanSelection> prepareMultiplePlanSelections() {
		List<PlanSelection> plans = new ArrayList<PlanSelection>();
		PlanSelection ps = new PlanSelection();
		ps.setBenefitPlan("BenPlan1");
		List<Contribution> contributions = new ArrayList<Contribution>();
		Contribution contribution = new Contribution();
		contribution.setCoverageLevel("1");
		contribution.setPlanCost(BigDecimal.valueOf(380));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("2");
		contribution.setPlanCost(BigDecimal.valueOf(410));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("4");
		contribution.setPlanCost(BigDecimal.valueOf(425));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("C");
		contribution.setPlanCost(BigDecimal.valueOf(520));
		contributions.add(contribution);
		ps.setContributions(contributions);
		plans.add(ps);
		
		ps = new PlanSelection();
		ps.setBenefitPlan("BenPlan2");
		contributions = new ArrayList<Contribution>();
		contribution = new Contribution();
		contribution.setCoverageLevel("1");
		contribution.setPlanCost(BigDecimal.valueOf(230));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("2");
		contribution.setPlanCost(BigDecimal.valueOf(235));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("4");
		contribution.setPlanCost(BigDecimal.valueOf(260));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("C");
		contribution.setPlanCost(BigDecimal.valueOf(295));
		contributions.add(contribution);
		ps.setContributions(contributions);
		plans.add(ps);
		
		ps = new PlanSelection();
		ps.setBenefitPlan("BenPlan3");
		contributions = new ArrayList<Contribution>();
		contribution = new Contribution();
		contribution.setCoverageLevel("2");
		contribution.setPlanCost(BigDecimal.valueOf(187));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("2");
		contribution.setPlanCost(BigDecimal.valueOf(192));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("4");
		contribution.setPlanCost(BigDecimal.valueOf(198));
		contributions.add(contribution);
		contribution = new Contribution();
		contribution.setCoverageLevel("C");
		contribution.setPlanCost(BigDecimal.valueOf(202));
		contributions.add(contribution);
		ps.setContributions(contributions);
		plans.add(ps);

		return plans;
	}

	private HeadCountData prepareHeadCountMap() {
		HeadCountData hc = new HeadCountData();
		hc.setStrategyGroupId(STRATEGY_GROUP_ID);
		Map<String, Integer> covrgHeadCountMap = new HashMap<String, Integer>();
		covrgHeadCountMap.put("1", 2);
		covrgHeadCountMap.put("2", 1);
		covrgHeadCountMap.put("C", 4);
		covrgHeadCountMap.put("4", 0);
		hc.setCovrgHeadCountMap(covrgHeadCountMap);
		return hc;
	}
	
	public static void main(String[] args) {
		List<Integer>  a = new ArrayList<Integer>();
		Integer b = new Integer(2);
		a.add(b);
		b = new Integer(3);
		a.add(b);
		System.out.println(a);
	}
}