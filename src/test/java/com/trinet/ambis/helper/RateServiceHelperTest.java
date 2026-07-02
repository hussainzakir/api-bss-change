package com.trinet.ambis.helper;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.service.model.PlanRateDto;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mockito.junit.MockitoJUnitRunner;

import static com.trinet.ambis.common.BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD;
import static com.trinet.ambis.enums.CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD;
import static com.trinet.ambis.enums.CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE;
import static com.trinet.ambis.enums.CoverageCodesEnums.COV_EMPLOYEE_FAMILY;
import static com.trinet.ambis.enums.RelationEnum.CHILD;
import static com.trinet.ambis.enums.RelationEnum.DOMESTIC_PARTNER;
import static com.trinet.ambis.enums.RelationEnum.SPOUSE;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RateServiceHelperTest {

	private static final LocalDate BENEFITS_START_DATE = LocalDate.now().withDayOfYear(1);
	private static final int CARRIER_AGE_LIMIT = 20;
	private static final int EMPLOYEE_AGE = 40;
	private static final int SPOUSE_AGE = 35;

	@Test
	public void getPlanCost4TierTest() {
		PlanRateDto planRate = prepare4TierPlanRateDto();
		ProspectCensusResponse employee = new ProspectCensusResponse();

		BigDecimal actualResult = RateServiceHelper.getPlanCost(planRate, COV_EMPLOYEE_PLUS_CHILD.getCode(), employee, BENEFITS_START_DATE);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("300.00"), actualResult);
	}

	@Test
	public void getPlanCostAgeBandedTest() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();
		ProspectCensusResponse employee = getEmployeeCensus().toBuilder()
				.dependents(List.of(getDpDependent(true, true)))
				.build();

		BigDecimal actualResult = RateServiceHelper.getPlanCost(planRate, COV_EMPLOYEE_PLUS_SPOUSE.getCode(), employee, BENEFITS_START_DATE);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("7500.00"), actualResult);
	}

	@Test
	public void givenEmployeeWithDependentsShouldIncludeOnlyCovgElectionAndIncludeInCostTrueAndGetAgeBandedPlanCost() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();

		ProspectCensusResponse employee = getEmployeeCensus().toBuilder().dependents(List.of(
					getSpouseDependent(false, true),
					getChildDependent("2005-01-01", true, false)))
				.build();

		BigDecimal actualResult = RateServiceHelper.getPlanCost(planRate, COV_EMPLOYEE_FAMILY.getCode(), employee, BENEFITS_START_DATE);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("4000.00"), actualResult);
	}

	@Test
	public void givenNotNullCarrierAgeLimitAndChildAgeLessThanCarrierAgeLimitShouldIncludeTop3AndGetAgeBandedPlanCost() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();
		planRate.setCarrierAgeLimit(CARRIER_AGE_LIMIT);
		ProspectCensusResponse employeeCensusWithDependents = prepareProspectCensusWithChildrenAgeLessThanCarrierAgeLimit();

		BigDecimal actualResult = RateServiceHelper.getPlanCost(planRate, COV_EMPLOYEE_FAMILY.getCode(), employeeCensusWithDependents, BENEFITS_START_DATE);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("12600.00"), actualResult);
	}

	@Test
	public void givenNotNullCarrierAgeLimitAndChildAgeGreaterThanCarrierAgeLimitShouldIncludeAllAndGetAgeBandedPlanCost() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();
		planRate.setCarrierAgeLimit(CARRIER_AGE_LIMIT);

		ProspectCensusResponse employeeCensusWithDependents = prepareProspectCensusWithChildrenAgeGreaterThanCarrierAgeLimit();

		BigDecimal actualResult = RateServiceHelper.getPlanCost(planRate, COV_EMPLOYEE_PLUS_CHILD.getCode(), employeeCensusWithDependents, BENEFITS_START_DATE);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("13000.00"), actualResult);
	}

	@Test
	public void givenNullCarrierAgeLimitShouldIncludeTop3AndGetAgeBandedPlanCost() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();
		ProspectCensusResponse employeeCensusWithDependents = prepareProspectCensusWithChildrenAgeGreaterThanCarrierAgeLimit();

		BigDecimal actualResult = RateServiceHelper.getPlanCost(planRate, COV_EMPLOYEE_FAMILY.getCode(), employeeCensusWithDependents, BENEFITS_START_DATE);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("14400.00"), actualResult);
	}

	@Test
	public void is4TiersTrueTest() {
		PlanRateDto planRate = prepare4TierPlanRateDto();

		boolean actualResult = RateServiceHelper.is4Tiers(planRate);

		assertTrue(actualResult);
	}

	@Test
	public void is4TiersFalseTest() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();

		boolean actualResult = RateServiceHelper.is4Tiers(planRate);

		assertFalse(actualResult);
	}

	@Test
	public void getEmployerContribution4TierCfpctTest() {
		PlanRateDto planRate = prepare4TierPlanRateDto();
		String benefitTypeCode = "medical";
		String covgLevelCode = CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode();
		boolean is4Tiers = true;
		int eeAge = 25;

		Map<String, Map<String, Object>> groupFunding = prepareGroupFundingCfpct();

		BigDecimal cvgLvlPlanCost = new BigDecimal("400.00");

		BigDecimal actualResult = RateServiceHelper.getEmployerContribution(cvgLvlPlanCost, groupFunding, planRate,
				benefitTypeCode, covgLevelCode, is4Tiers, eeAge);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("325.00"), actualResult);
	}

	@Test
	public void getEmployerContributionAgeBandedCfpctTest() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();
		String benefitTypeCode = "medical";
		String covgLevelCode = CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode();
		boolean is4Tiers = false;
		int eeAge = 25;

		Map<String, Map<String, Object>> groupFunding = prepareGroupFundingCfpct();

		BigDecimal cvgLvlPlanCost = new BigDecimal("10300.00");

		BigDecimal actualResult = RateServiceHelper.getEmployerContribution(cvgLvlPlanCost, groupFunding, planRate,
				benefitTypeCode, covgLevelCode, is4Tiers, eeAge);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("8350.00"), actualResult);
	}

	@Test
	public void getEmployerContribution4TierFlatTest() {
		PlanRateDto planRate = prepare4TierPlanRateDto();
		String benefitTypeCode = "medical";
		String covgLevelCode = CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode();
		boolean is4Tiers = true;
		int eeAge = 25;

		Map<String, Map<String, Object>> groupFunding = prepareGroupFundingFlat();

		BigDecimal cvgLvlPlanCost = new BigDecimal("400.00");

		BigDecimal actualResult = RateServiceHelper.getEmployerContribution(cvgLvlPlanCost, groupFunding, planRate,
				benefitTypeCode, covgLevelCode, is4Tiers, eeAge);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("130.00"), actualResult.setScale(2));
	}

	@Test
	public void getEmployerContributionAgeBandedFlatTest() {
		PlanRateDto planRate = prepareAgeBandedPlanRateDto();
		String benefitTypeCode = "medical";
		String covgLevelCode = CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode();
		boolean is4Tiers = false;
		int eeAge = 25;

		Map<String, Map<String, Object>> groupFunding = prepareGroupFundingFlat();

		BigDecimal cvgLvlPlanCost = new BigDecimal("10300.00");

		BigDecimal actualResult = RateServiceHelper.getEmployerContribution(cvgLvlPlanCost, groupFunding, planRate,
				benefitTypeCode, covgLevelCode, is4Tiers, eeAge);

		assertNotNull(actualResult);
		assertEquals(new BigDecimal("130.00"), actualResult.setScale(2));
	}

	@Test
	public void isValueNotNullAndNotZeroTest() {
		// Test with value not null and not zero
		BigDecimal planCost = new BigDecimal("100.00");
		boolean actualResult = RateServiceHelper.isValueNotNullAndNotZero(planCost);
		assertTrue(actualResult);


		// Test with value null
		planCost = null;
		actualResult = RateServiceHelper.isValueNotNullAndNotZero(planCost);
		assertFalse(actualResult);


		// Test with value zero
		planCost = BigDecimal.ZERO;
		actualResult = RateServiceHelper.isValueNotNullAndNotZero(planCost);
		assertFalse(actualResult);
	}

	private PlanRateDto prepare4TierPlanRateDto() {
		PlanRateDto planRate = new PlanRateDto();
		planRate.setPlanId("12345");
		planRate.setRateTypeCode("4Tier");
		Map<String, BigDecimal> tieredCost = new HashMap<>();
		tieredCost.put("1", new BigDecimal("100.00"));
		tieredCost.put("2", new BigDecimal("200.00"));
		tieredCost.put("C", new BigDecimal("300.00"));
		tieredCost.put("4", new BigDecimal("400.00"));
		planRate.setTieredCost(tieredCost);
		return planRate;
	}

	private PlanRateDto prepareAgeBandedPlanRateDto() {
		PlanRateDto planRate = new PlanRateDto();
		planRate.setPlanId("12345");
		planRate.setRateTypeCode("ageBanded");
		Map<String, BigDecimal> tieredCost = new HashMap<>();
		tieredCost.put("0", new BigDecimal("1400.00"));
		tieredCost.put("15", new BigDecimal("1500.00"));
		tieredCost.put("16", new BigDecimal("1600.00"));
		tieredCost.put("17", new BigDecimal("1700.00"));
		tieredCost.put("18", new BigDecimal("1800.00"));
		tieredCost.put("19", new BigDecimal("1900.00"));
		tieredCost.put("20", new BigDecimal("2000.00"));
		tieredCost.put("21", new BigDecimal("2100.00"));
		tieredCost.put("22", new BigDecimal("2200.00"));
		tieredCost.put("23", new BigDecimal("2300.00"));
		tieredCost.put("24", new BigDecimal("2400.00"));
		tieredCost.put("25", new BigDecimal("2500.00"));
		tieredCost.put("26", new BigDecimal("2600.00"));
		tieredCost.put("27", new BigDecimal("2700.00"));
		tieredCost.put("28", new BigDecimal("2800.00"));
		tieredCost.put("29", new BigDecimal("2900.00"));
		tieredCost.put("30", new BigDecimal("3000.00"));
		tieredCost.put("31", new BigDecimal("3100.00"));
		tieredCost.put("32", new BigDecimal("3200.00"));
		tieredCost.put("33", new BigDecimal("3300.00"));
		tieredCost.put("34", new BigDecimal("3400.00"));
		tieredCost.put("35", new BigDecimal("3500.00"));
		tieredCost.put("36", new BigDecimal("3600.00"));
		tieredCost.put("37", new BigDecimal("3700.00"));
		tieredCost.put("38", new BigDecimal("3800.00"));
		tieredCost.put("39", new BigDecimal("3900.00"));
		tieredCost.put("40", new BigDecimal("4000.00"));
		tieredCost.put("41", new BigDecimal("4100.00"));
		tieredCost.put("42", new BigDecimal("4200.00"));
		tieredCost.put("43", new BigDecimal("4300.00"));
		tieredCost.put("44", new BigDecimal("4400.00"));
		tieredCost.put("45", new BigDecimal("4500.00"));
		tieredCost.put("46", new BigDecimal("4600.00"));
		tieredCost.put("47", new BigDecimal("4700.00"));
		tieredCost.put("48", new BigDecimal("4800.00"));
		tieredCost.put("49", new BigDecimal("4900.00"));
		tieredCost.put("50", new BigDecimal("5000.00"));
		tieredCost.put("51", new BigDecimal("5100.00"));
		tieredCost.put("52", new BigDecimal("5200.00"));
		tieredCost.put("53", new BigDecimal("5300.00"));
		tieredCost.put("54", new BigDecimal("5400.00"));
		tieredCost.put("55", new BigDecimal("5500.00"));
		tieredCost.put("56", new BigDecimal("5600.00"));
		tieredCost.put("57", new BigDecimal("5700.00"));
		tieredCost.put("58", new BigDecimal("5800.00"));
		tieredCost.put("59", new BigDecimal("5900.00"));
		tieredCost.put("60", new BigDecimal("6000.00"));
		tieredCost.put("61", new BigDecimal("6100.00"));
		tieredCost.put("62", new BigDecimal("6200.00"));
		tieredCost.put("63", new BigDecimal("6300.00"));
		tieredCost.put("64", new BigDecimal("6400.00"));
		planRate.setTieredCost(tieredCost);
		return planRate;
	}

	private ProspectCensusResponse prepareProspectCensusWithChildrenAgeGreaterThanCarrierAgeLimit() {
		ProspectCensusResponse employeeCensus = getEmployeeCensus();
		LocalDate today = LocalDate.now();
		return employeeCensus.toBuilder()
				.dependents(List.of(
						getSpouseDependent(true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT + 2)), true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT + 3)), true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT + 4)), true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT + 5)), true, true)))
				.build();
	}

	private ProspectCensusResponse prepareProspectCensusWithChildrenAgeLessThanCarrierAgeLimit() {
		ProspectCensusResponse employeeCensus = getEmployeeCensus();
		LocalDate today = LocalDate.now();
		return employeeCensus.toBuilder()
				.dependents(List.of(
						getSpouseDependent(true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT - 1)), true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT - 2)), true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT - 3)), true, true),
						getChildDependent(getDobString(today.minusYears(CARRIER_AGE_LIMIT - 4)), true, true)))
				.build();
	}

	private ProspectCensusResponse.Dependents getChildDependent(String dob, boolean covgElection, boolean includeInCost) {
		return ProspectCensusResponse.Dependents.builder()
				.dob(dob).relation(CHILD.getCode())
				.covgElection(covgElection).includeInCost(includeInCost)
				.build();
	}

	private ProspectCensusResponse.Dependents getDpDependent(boolean covgElection, boolean includeInCost) {
		return ProspectCensusResponse.Dependents.builder()
				.dob(getDobString(LocalDate.now().minusYears(SPOUSE_AGE).withDayOfYear(1)))
				.relation(DOMESTIC_PARTNER.getCode()).covgElection(covgElection).includeInCost(includeInCost)
				.build();
	}

	private ProspectCensusResponse.Dependents getSpouseDependent(boolean covgElection, boolean includeInCost) {
		return ProspectCensusResponse.Dependents.builder()
				.dob(getDobString(LocalDate.now().minusYears(SPOUSE_AGE).withDayOfYear(1)))
				.relation(SPOUSE.getCode()).covgElection(covgElection).includeInCost(includeInCost)
				.build();
	}

	private ProspectCensusResponse getEmployeeCensus() {

		return ProspectCensusResponse.builder().employeeId("0000000123456").employeeName("John").state("CA").gender("M").k1(false)
				.salary(BigDecimal.valueOf(6000)).zip("90210").dob(getDobString(LocalDate.now().minusYears(EMPLOYEE_AGE).withDayOfYear(1))).build();
	}

	private String getDobString(LocalDate dob) {
		return dob.format(DateTimeFormatter.ofPattern(DATE_PATTERN_YYYY_MM_DD));
	}

	private Map<String, Map<String, Object>> prepareGroupFundingCfpct() {
		Map<String, Map<String, Object>> groupFunding = new HashMap<>();
		Map<String, Object> fundingDetails = new HashMap<>();
		fundingDetails.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.CFPCT);
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal("100.00"));
		fundingDetails.put(COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal("90.00"));
		fundingDetails.put(COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal("80.00"));
		fundingDetails.put(COV_EMPLOYEE_FAMILY.getId(), new BigDecimal("75.00"));
		groupFunding.put("medical", fundingDetails);
		return groupFunding;
	}

	private Map<String, Map<String, Object>> prepareGroupFundingFlat() {
		Map<String, Map<String, Object>> groupFunding = new HashMap<>();
		Map<String, Object> fundingDetails = new HashMap<>();
		fundingDetails.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.FLAT);
		fundingDetails.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), new BigDecimal("100.00"));
		fundingDetails.put(COV_EMPLOYEE_PLUS_SPOUSE.getId(), new BigDecimal("110.00"));
		fundingDetails.put(COV_EMPLOYEE_PLUS_CHILD.getId(), new BigDecimal("120.00"));
		fundingDetails.put(COV_EMPLOYEE_FAMILY.getId(), new BigDecimal("130"));
		groupFunding.put("medical", fundingDetails);
		return groupFunding;
	}


}
