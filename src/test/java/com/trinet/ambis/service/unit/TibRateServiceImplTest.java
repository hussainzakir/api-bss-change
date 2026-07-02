package com.trinet.ambis.service.unit;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.RateServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.impl.TibRateServiceImpl;
import com.trinet.ambis.service.model.PlanRateDto;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.DateUtils;
import com.trinet.ambis.util.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static com.trinet.ambis.enums.CoverageCodesEnums.COV_EMPLOYEE_FAMILY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TibRateServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	TibRateServiceImpl tibRateService;

	@Mock
	ProspectCensusService prospectCensusService;

	@Mock
	ProspectPlanAvailabilityService prospectPlanAvailabilityService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	EmployeePlanAssignmentService employeePlanAssignmentService;

	@Mock
	EmployeeDataDao employeeDataDao;

	@Mock
	EePlanAssignmentDao eePlanAssignmentDao;

	@Mock
	StrategyService strategyService;

	private static final long STRATEGY_ID = 123;
	private final Company COMPANY = prepareCompany();
	private static final int DEPENDENT_AGE_LIMIT = 25;
	private final LocalDate BENEFITS_START_DATE = Utils.convertStringToLocalDate(COMPANY.getBenefitStartDate(),
			BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
	private static MockedStatic<DateUtils> dateUtilsMockedStatic;
	private static MockedStatic<RateServiceHelper> rateServiceHelperMockedStatic;

	@Before
	public void setUp() {
		dateUtilsMockedStatic = Mockito.mockStatic(DateUtils.class);
		rateServiceHelperMockedStatic = Mockito.mockStatic(RateServiceHelper.class);
		when(DateUtils.calculateAgeUntilDate(LocalDate.of(2000, 1, 1), BENEFITS_START_DATE)).thenReturn(25);
		when(DateUtils.calculateAgeUntilDate(LocalDate.of(2001, 9, 28), BENEFITS_START_DATE)).thenReturn(24);
		when(DateUtils.calculateAgeUntilDate(LocalDate.of(2022, 3, 31), BENEFITS_START_DATE)).thenReturn(3);
		when(DateUtils.calculateAgeUntilDate(LocalDate.of(1975, 9, 28), BENEFITS_START_DATE)).thenReturn(50);
	}

	@After
	public void tearDown() {
		if (dateUtilsMockedStatic != null) {
			dateUtilsMockedStatic.close();
			dateUtilsMockedStatic = null;
		}
		if (rateServiceHelperMockedStatic != null) {
			rateServiceHelperMockedStatic.close();
			rateServiceHelperMockedStatic = null;
		}
	}

	@Test
	public void getRateForEmployee4TierTest() {

		// given
		// data
		String employeeId = "0000000123456";
		String planId = "123456";
		String covgLevelCode = "4";

		when(prospectCensusService.getProspectCensus(COMPANY.getCode())).thenReturn(prepareProspectCensus());
		when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL)).thenReturn(prepareHrisPlanResponse());

		// when
		BigDecimal actualResult = tibRateService.getRateForEmployee(COMPANY, employeeId, planId, covgLevelCode,
				BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		// then
		// verify
		verify(prospectCensusService, times(1)).getProspectCensus(COMPANY.getCode());
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL);

	}

	@Test
	public void getRateForEmployeeAgeBandedTest() {

		// given
		// data
		String employeeId = "0000000123456";
		String planId = "123457";
		String covgLevelCode = COV_EMPLOYEE_FAMILY.getCode();

		rateServiceHelperMockedStatic.when(() -> RateServiceHelper.getPlanCost(any(), any(), any(), any()))
				.thenReturn(BigDecimal.valueOf(150));
		when(prospectCensusService.getProspectCensus(COMPANY.getCode())).thenReturn(prepareProspectCensus());
		when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL)).thenReturn(prepareHrisPlanResponse());

		// when
		BigDecimal actualResult = tibRateService.getRateForEmployee(COMPANY, employeeId, planId, covgLevelCode,
				BSSApplicationConstants.MEDICAL_PLAN_TYPE);

		// then
		// verify

		assertEquals(BigDecimal.valueOf(150), actualResult);
		verify(RateServiceHelper.class);
		ArgumentCaptor<PlanRateDto> planRateDtoArgumentCaptor = ArgumentCaptor.forClass(PlanRateDto.class);

		LocalDate expectedBenefitStartDate = Utils.convertStringToLocalDate(COMPANY.getBenefitStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		RateServiceHelper.getPlanCost(planRateDtoArgumentCaptor.capture(), eq(covgLevelCode), any(),
				eq(expectedBenefitStartDate));
		PlanRateDto actualPlanRateDto = planRateDtoArgumentCaptor.getValue();
		assertEquals(DEPENDENT_AGE_LIMIT, actualPlanRateDto.getCarrierAgeLimit().intValue());
		verify(prospectCensusService).getProspectCensus(COMPANY.getCode());
		verify(prospectPlanAvailabilityService).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL);

	}

	@Test
	public void saveRatesPerStrategyCfpctTest() {

		// given
		// data
		List<ProspectCensusResponse> censusData = prepareProspectCensus();
		Set<String> emplIds = censusData.stream().map(ProspectCensusResponse::getEmployeeId)
				.collect(Collectors.toSet());

		when(prospectCensusService.getProspectCensus(COMPANY.getCode())).thenReturn(censusData);
		when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(eq(COMPANY), anyString()))
				.thenReturn(prepareHrisPlanResponse());
		when(realmDataDao.getStrategyFundingDetails(STRATEGY_ID)).thenReturn(prepareCfpctFundingDetails());
		when(employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(STRATEGY_ID)))
				.thenReturn(prepareEmployeePlanAssignments());
		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareEmployeeGroupDetails());
		doNothing().when(strategyService).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
		doNothing().when(employeePlanAssignmentService).saveEePlanAssignments(anyList());

		// when
		tibRateService.saveRatesPerStrategy(COMPANY, STRATEGY_ID);

		// then
		// verify
		verify(prospectCensusService, times(1)).getProspectCensus(COMPANY.getCode());
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.DENTAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.VISION);
		verify(realmDataDao, times(3)).getStrategyFundingDetails(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(3)).getEmployeePlanAssigmentBy(List.of(STRATEGY_ID));
		verify(employeeDataDao, times(3)).getEmployeeGroupDetailsByStrategy(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		verify(employeePlanAssignmentService, times(1)).saveEePlanAssignments(anyList());
		verify(strategyService, times(1)).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
	}

	@Test
	public void saveRatesPerStrategyFlatTest() {

		// given
		// data
		List<ProspectCensusResponse> censusData = prepareProspectCensus();
		Set<String> emplIds = censusData.stream().map(ProspectCensusResponse::getEmployeeId)
				.collect(Collectors.toSet());

		when(prospectCensusService.getProspectCensus(COMPANY.getCode())).thenReturn(censusData);
		when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(eq(COMPANY), anyString()))
				.thenReturn(prepareHrisPlanResponse());
		when(realmDataDao.getStrategyFundingDetails(STRATEGY_ID)).thenReturn(prepareFlatFundingDetails());
		when(employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(STRATEGY_ID)))
				.thenReturn(prepareEmployeePlanAssignments());
		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareEmployeeGroupDetails());
		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		doNothing().when(strategyService).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
		doNothing().when(employeePlanAssignmentService).saveEePlanAssignments(anyList());

		// when
		tibRateService.saveRatesPerStrategy(COMPANY, STRATEGY_ID);

		// then
		// verify
		verify(prospectCensusService, times(1)).getProspectCensus(COMPANY.getCode());
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.DENTAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.VISION);
		verify(realmDataDao, times(3)).getStrategyFundingDetails(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(3)).getEmployeePlanAssigmentBy(List.of(STRATEGY_ID));
		verify(employeeDataDao, times(3)).getEmployeeGroupDetailsByStrategy(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		verify(employeePlanAssignmentService, times(1)).saveEePlanAssignments(anyList());
		verify(strategyService, times(1)).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
	}

	@Test
	public void saveRatesPerEmployeeTest() {

		// given
		// data
		List<ProspectCensusResponse> censusData = prepareProspectCensus();
		Set<String> emplIds = censusData.stream().map(ProspectCensusResponse::getEmployeeId)
				.collect(Collectors.toSet());

		when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(eq(COMPANY), anyString()))
				.thenReturn(prepareHrisPlanResponse());
		when(realmDataDao.getStrategyFundingDetails(STRATEGY_ID)).thenReturn(prepareCfpctFundingDetails());
		when(employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(STRATEGY_ID)))
				.thenReturn(prepareEmployeePlanAssignments());
		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareEmployeeGroupDetails());
		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		doNothing().when(strategyService).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
		doNothing().when(employeePlanAssignmentService).saveEePlanAssignments(anyList());

		// when
		tibRateService.saveRatesPerEmployee(COMPANY, STRATEGY_ID, censusData);

		// then
		// verify
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.DENTAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.VISION);
		verify(realmDataDao, times(3)).getStrategyFundingDetails(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(3)).getEmployeePlanAssigmentBy(List.of(STRATEGY_ID));
		verify(employeeDataDao, times(3)).getEmployeeGroupDetailsByStrategy(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		verify(employeePlanAssignmentService, times(1)).saveEePlanAssignments(anyList());
		verify(strategyService, times(1)).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));

	}

	@Test
	public void saveRatesPerEmployeeIdsTest1() {

		// given
		// data
		List<ProspectCensusResponse> censusData = prepareProspectCensus();
		Set<String> emplIds = censusData.stream().map(ProspectCensusResponse::getEmployeeId)
				.collect(Collectors.toSet());

		when(prospectCensusService.getProspectCensus(COMPANY.getCode())).thenReturn(censusData);
		when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(eq(COMPANY), anyString()))
				.thenReturn(prepareHrisPlanResponse());
		when(realmDataDao.getStrategyFundingDetails(STRATEGY_ID)).thenReturn(prepareCfpctFundingDetails());
		when(employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(STRATEGY_ID)))
				.thenReturn(prepareEmployeePlanAssignments());
		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareEmployeeGroupDetails());
		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		doNothing().when(strategyService).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
		doNothing().when(employeePlanAssignmentService).saveEePlanAssignments(anyList());

		// when
		tibRateService.saveRatesPerEmployeeIds(COMPANY, STRATEGY_ID, emplIds);

		// then
		// verify
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.DENTAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.VISION);
		verify(realmDataDao, times(3)).getStrategyFundingDetails(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(3)).getEmployeePlanAssigmentBy(List.of(STRATEGY_ID));
		verify(employeeDataDao, times(3)).getEmployeeGroupDetailsByStrategy(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		verify(employeePlanAssignmentService, times(1)).saveEePlanAssignments(anyList());
		verify(strategyService, times(1)).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
	}

	@Test
	public void saveRatesPerEmployeeIdsTest2() {

		// given
		// data
		List<ProspectCensusResponse> censusData = prepareProspectCensus();
		Set<String> emplIds = censusData.stream().map(ProspectCensusResponse::getEmployeeId)
				.collect(Collectors.toSet());

		when(prospectCensusService.getProspectCensus(COMPANY.getCode())).thenReturn(censusData);
		when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(eq(COMPANY), anyString()))
				.thenReturn(prepareHrisPlanResponse());
		when(realmDataDao.getStrategyFundingDetails(STRATEGY_ID)).thenReturn(prepareCfpctFundingDetails());
		when(employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(STRATEGY_ID)))
				.thenReturn(prepareEmployeePlanAssignments());
		when(employeeDataDao.getEmployeeGroupDetailsByStrategy(STRATEGY_ID)).thenReturn(prepareEmployeeGroupDetails());
		doNothing().when(employeePlanAssignmentService).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		doNothing().when(strategyService).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
		doNothing().when(employeePlanAssignmentService).saveEePlanAssignments(anyList());

		// when
		tibRateService.saveRatesPerEmployeeIds(COMPANY, STRATEGY_ID, emplIds,
				BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);

		// then
		// verify
		verify(prospectCensusService, times(1)).getProspectCensus(COMPANY.getCode());
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.MEDICAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.DENTAL);
		verify(prospectPlanAvailabilityService, times(1)).getProspectEmployeeHrisPlanAvailability(COMPANY,
				BSSApplicationConstants.VISION);
		verify(realmDataDao, times(3)).getStrategyFundingDetails(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(3)).getEmployeePlanAssigmentBy(List.of(STRATEGY_ID));
		verify(employeeDataDao, times(3)).getEmployeeGroupDetailsByStrategy(STRATEGY_ID);
		verify(employeePlanAssignmentService, times(1)).deleteEmployeePlanAssignmentForBenTypes(emplIds, STRATEGY_ID,
				new HashSet<>(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));
		verify(employeePlanAssignmentService, times(1)).saveEePlanAssignments(anyList());
		verify(strategyService, times(1)).createOmsStrategyEstimate(COMPANY, Set.of(STRATEGY_ID));
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode("a1b2c3");
		company.setBenefitStartDate("01-JAN-2025");
		return company;
	}

	private List<ProspectCensusResponse> prepareProspectCensus() {
		return List.of(
				ProspectCensusResponse.builder().employeeId("0000000123456").employeeName("John").state("CA")
						.gender("M").k1(true).salary(BigDecimal.valueOf(6000)).zip("90210").dob("2000-01-01")
						.dependents(List.of(
								ProspectCensusResponse.Dependents.builder().dob("2001-09-28").relation("SP")
										.covgElection(true).includeInCost(true).build(),
								ProspectCensusResponse.Dependents.builder().dob("2022-03-31").relation("CH")
										.covgElection(true).includeInCost(true).build()))
						.build(),
				ProspectCensusResponse.builder().employeeId("0000000123457").employeeName("katty Scott").state("TX")
						.gender("F").k1(false).salary(BigDecimal.valueOf(4500)).zip("77001").dob("1975-09-28")
						.dependents(List.of(
								ProspectCensusResponse.Dependents.builder().dob("2001-09-28").relation("SP")
										.covgElection(false).includeInCost(true).build(),
								ProspectCensusResponse.Dependents.builder().dob("2022-03-31").relation("CH")
										.covgElection(true).includeInCost(false).build()))
						.build());

	}

	private List<HrisPlanResponse> prepareHrisPlanResponse() {
		return List.of(
				HrisPlanResponse.builder().planId(123456).planName("HRIS Medical Plan 1").carrierId(1)
						.carrierName("HRIS Carrier 1").dependentAgeLimit(DEPENDENT_AGE_LIMIT)
						.rateDetails(
								HrisPlanResponse.RateDetails.builder().rateType("4Tier")
										.ratesByZip(Arrays.asList(HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(Arrays.asList("90210"))
												.rates(Arrays.asList(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("1").rate(123.45).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("2").rate(234.56).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("C").rate(345.67).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("4").rate(456.78).build()))
												.build()))
										.build())
						.build(),
				HrisPlanResponse.builder().planId(123457).planName("HRIS Medical Plan 2").carrierId(1)
						.carrierName("HRIS Carrier 2").dependentAgeLimit(DEPENDENT_AGE_LIMIT)
						.rateDetails(
								HrisPlanResponse.RateDetails.builder().rateType("ageBanded")
										.ratesByZip(Arrays.asList(HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(Arrays.asList("90210"))
												.rates(Arrays.asList(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("0").rate(50.00).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("15").rate(65.00).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("24").rate(74.00).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
																.tierCode("25").rate(75.00).build()))
												.build()))
										.build())
						.build());
	}

	private Map<String, Map<String, Map<String, Object>>> prepareCfpctFundingDetails() {
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = new HashMap<>();
		Map<String, Map<String, Object>> fundingDetails = new HashMap<>();
		Map<String, Object> fundingDetail = new HashMap<>();
		fundingDetail.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.CFPCT);
		fundingDetail.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		fundingDetail.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(100));
		fundingDetail.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(100));
		fundingDetail.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(100));
		fundingDetail.put(COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(100));
		fundingDetails.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, fundingDetail);
		groupFundingDetails.put("BENPRG1", fundingDetails);
		return groupFundingDetails;
	}

	private Map<String, Map<String, Map<String, Object>>> prepareFlatFundingDetails() {
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = new HashMap<>();
		Map<String, Map<String, Object>> fundingDetails = new HashMap<>();
		Map<String, Object> fundingDetail = new HashMap<>();
		fundingDetail.put(BSSApplicationConstants.FUNDING_TYPE, BSSApplicationConstants.FLAT);
		fundingDetail.put(BSSApplicationConstants.PRIMARY_PLAN_TYPE, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
		fundingDetail.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), BigDecimal.valueOf(200));
		fundingDetail.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(), BigDecimal.valueOf(300));
		fundingDetail.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(), BigDecimal.valueOf(400));
		fundingDetail.put(COV_EMPLOYEE_FAMILY.getId(), BigDecimal.valueOf(500));
		fundingDetails.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, fundingDetail);
		groupFundingDetails.put("BENPRG1", fundingDetails);
		return groupFundingDetails;
	}

	private List<EePlanAssignment> prepareEmployeePlanAssignments() {
		return List.of(
				EePlanAssignment.builder()
						.eePlanAssignmentPK(EePlanAssignmentPK.builder().strategyId(STRATEGY_ID).emplId("0000000123456")
								.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).build())
						.benefitPlan("123456").covrgCD("4").build(),
				EePlanAssignment.builder()
						.eePlanAssignmentPK(EePlanAssignmentPK.builder().strategyId(STRATEGY_ID).emplId("0000000123457")
								.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).build())
						.benefitPlan("123456").covrgCD("1").build());
	}

	private Set<Employee> prepareEmployeeGroupDetails() {
		Set<Employee> employeeData = new HashSet<>();
		Employee employee = new Employee();
		employee.setEmplId("0000000123456");
		employee.setBenefitProgram("BENPRG1");
		employeeData.add(employee);
		employee = new Employee();
		employee.setEmplId("0000000123457");
		employee.setBenefitProgram("BENPRG1");
		employeeData.add(employee);
		return employeeData;
	}

}
