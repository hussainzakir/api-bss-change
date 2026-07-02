package com.trinet.ambis.service.impl;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.RateServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.TibRateService;
import com.trinet.ambis.service.model.PlanRateDto;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Log4j2
@Service
@RequiredArgsConstructor
public class TibRateServiceImpl implements TibRateService {

	private final ProspectCensusService prospectCensusService;
    private final ProspectPlanAvailabilityService prospectPlanAvailabilityService;
    private final RealmDataDao realmDataDao;
	private final EmployeePlanAssignmentService employeePlanAssignmentService;
	private final EmployeeDataDao employeeDataDao;
	private final EePlanAssignmentDao eePlanAssignmentDao;
	private final StrategyService strategyService;
	private static final String PLAN_RATE_NOT_FOUND = "Plan rate not found for planId: {} and zip: {}";

	@Override
	public void saveRatesPerStrategy(Company company, long strategyId) {
		List<ProspectCensusResponse> prospectCensus = prospectCensusService.getProspectCensus(company.getCode());
		calculateAndSaveRates(company, strategyId, prospectCensus, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
	}

	@Override
	public void saveRatesPerEmployee(Company company, long strategyId, List<ProspectCensusResponse> censusData) {
		calculateAndSaveRates(company, strategyId, censusData, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
	}

	@Override
	public void saveRatesPerEmployeeIds(Company company, long strategyId, Set<String> employeeIds) {
		saveRatesPerEmployeeIds(company, strategyId, employeeIds, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);
	}

	@Override
	public void saveRatesPerEmployeeIds(Company company, long strategyId, Set<String> employeeIds, List<String> benefitTypes) {
		List<ProspectCensusResponse> prospectCensus = prospectCensusService.getProspectCensus(company.getCode());
		List<ProspectCensusResponse> censusData = prospectCensus.stream()
				.filter(census -> employeeIds.contains(census.getEmployeeId()))
				.collect(Collectors.toList());
		calculateAndSaveRates(company, strategyId, censusData, benefitTypes);
	}

	@Override
	public BigDecimal getRateForEmployee(Company company, String employeeId, String planId, String covgLevelCode, String benefitType) {
		List<ProspectCensusResponse> prospectCensus = prospectCensusService.getProspectCensus(company.getCode());
		ProspectCensusResponse censusData = prospectCensus.stream()
				.filter(census -> census.getEmployeeId().equals(employeeId))
				.findFirst().orElse(null);
		if (censusData == null) {
			log.error("Employee not found in census data for employeeId: {}", employeeId);
			return BigDecimal.ZERO;
		}
		return calculateBaseRateForEmployee(company, censusData, planId, covgLevelCode, benefitType);
	}

	private BigDecimal calculateBaseRateForEmployee(Company company, ProspectCensusResponse censusData, String planId, String covgLevelCode, String benefitType) {
		List<HrisPlanResponse> planRates = prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, PlanTypesEnum.getName(benefitType));
		PlanRateDto planRate = getPlanRateForEmployee(planRates, planId, censusData.getZip());
		return RateServiceHelper.getPlanCost(planRate, covgLevelCode, censusData, Utils.convertStringToLocalDate(company.getBenefitStartDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
	}

	private void calculateAndSaveRates(Company company, long strategyId, List<ProspectCensusResponse> censusData, List<String> benefitTypes) {
		List<EePlanAssignment> employeePlans = new ArrayList<>();
		benefitTypes.stream().forEach(benType -> {
			employeePlans.addAll(calculateRatesPerEmployee(company, strategyId, censusData, benType));
		});
		if (CollectionUtils.isEmpty(employeePlans)) {
			log.info("Unable to calculate EE and ER rates for Prospect id : {} ", company.getCode());
			return;
		}
		deleteEmployeePlanAssignments(strategyId, censusData, benefitTypes);
		saveEmployeePlanAssignments(employeePlans);
		strategyService.createOmsStrategyEstimate(company, Set.of(strategyId));
	}

	private List<EePlanAssignment> calculateRatesPerEmployee(Company company, long strategyId, List<ProspectCensusResponse> censusData, String benefitType) {
		List<HrisPlanResponse> planRates = prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, PlanTypesEnum.getName(benefitType));
		if (CollectionUtils.isEmpty(planRates)) {
			log.info("No Rates found for Prospect id : {} ", company.getCode());
			return List.of();
		}
		Map<String, Map<String, Map<String, Object>>> groupFundingDetails = realmDataDao
				.getStrategyFundingDetails(strategyId);

		List<EePlanAssignment> employeePlanAssignments = employeePlanAssignmentService.getEmployeePlanAssigmentBy(List.of(strategyId));
		Set<String> employeeIds = censusData.stream().map(ProspectCensusResponse::getEmployeeId).collect(Collectors.toSet());
		employeePlanAssignments = employeePlanAssignments.stream()
				.filter(employee -> employee.getEePlanAssignmentPK().getBenefitType().equals(benefitType))
				.filter(employee -> employeeIds.contains(employee.getEePlanAssignmentPK().getEmplId()))
				.collect(Collectors.toList());

		return preparePlansWithCost(censusData, groupFundingDetails, planRates, employeePlanAssignments, strategyId,
				Utils.convertStringToLocalDate(company.getBenefitStartDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
	}

	private List<EePlanAssignment> preparePlansWithCost(List<ProspectCensusResponse> censusData,
														Map<String, Map<String, Map<String, Object>>> groupFundingDetails,
														List<HrisPlanResponse> planRates,
														List<EePlanAssignment> employeePlanAssignments,
														long strategyId, LocalDate benefitStartDate) {

		Set<Employee> strategyEmployeeData = employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId);
		Map<String, ProspectCensusResponse> censusDataMap = censusData.stream()
				.collect(Collectors.toMap(ProspectCensusResponse::getEmployeeId, census -> census));

		return employeePlanAssignments.stream().map(planAssignment -> {
			ProspectCensusResponse employee =  censusDataMap.get(planAssignment.getEePlanAssignmentPK().getEmplId());
			String employeeBenefitProgram = getEmployeeBenefitProgram(employee, strategyEmployeeData);

			Map<String, Map<String, Object>> groupFunding = groupFundingDetails.get(employeeBenefitProgram);
			PlanRateDto planRate = getPlanRateForEmployee(planRates, planAssignment.getBenefitPlan(), employee.getZip());
			if (planRate == null) {
				log.error(PLAN_RATE_NOT_FOUND, planAssignment.getBenefitPlan(), employee.getZip());
				return null;
			}
			boolean is4Tiers = RateServiceHelper.is4Tiers(planRate);
			String covgLevelCode = planAssignment.getCovrgCD();
			BigDecimal planCost = RateServiceHelper.getPlanCost(planRate, covgLevelCode, employee, benefitStartDate);
			BigDecimal employerContribution = RateServiceHelper.getEmployerContribution(planCost, groupFunding, planRate,
					planAssignment.getEePlanAssignmentPK().getBenefitType(), covgLevelCode, is4Tiers,
					RateServiceHelper.getAge(employee.getDob(), benefitStartDate));
			BigDecimal employeeContribution = RateServiceHelper.isValueNotNullAndNotZero(planCost)
					? planCost.subtract(employerContribution)
					: BigDecimal.ZERO;
			log.info("TotalPlanCost:{} ; EmployerContribution:{} ; EmployeeContribution:{}", planCost,
					employerContribution, employeeContribution);
			return buildEePlanAssignment(planAssignment, employeeContribution, employerContribution);
		}).collect(Collectors.toList());
	}

	private PlanRateDto getPlanRateForEmployee(List<HrisPlanResponse> planRates, String benefitPlan, String zip) {

		HrisPlanResponse hrisPlanRate = planRates.stream()
				.filter(obj -> Integer.toString(obj.getPlanId()).equals(benefitPlan))
				.filter(obj -> obj.getRateDetails().getRatesByZip().stream()
						.anyMatch(rateByZip -> rateByZip.getZips().contains(zip)))
				.findFirst()
				.orElse(null);

		if (hrisPlanRate == null) {
			log.error(PLAN_RATE_NOT_FOUND, benefitPlan, zip);
			return null;
		} else {
			List<HrisPlanResponse.RateDetails.RatesByZip.Rate> hrisRate = hrisPlanRate.getRateDetails().getRatesByZip().get(0).getRates();
			// map hrisRate to a Map<String, BigDecimal>
			Map<String, BigDecimal> rateMap = hrisRate.stream()
					.collect(Collectors.toMap(HrisPlanResponse.RateDetails.RatesByZip.Rate::getTierCode,
							rate -> BigDecimal.valueOf(rate.getRate()).setScale(2, RoundingMode.HALF_UP)));

			return PlanRateDto.builder()
					.planId(Integer.toString(hrisPlanRate.getPlanId()))
					.rateTypeCode(hrisPlanRate.getRateDetails().getRateType())
					.tieredCost(rateMap)
					.carrierAgeLimit(hrisPlanRate.getDependentAgeLimit())
					.build();
		}
	}

	private String getEmployeeBenefitProgram(ProspectCensusResponse employee, Set<Employee> strategyEmployeeData) {
		return strategyEmployeeData.stream()
				.filter(employeeGroup -> employeeGroup.getEmplId().equals(employee.getEmployeeId()))
				.findFirst()
				.map(Employee::getBenefitProgram)
				.orElse(null);
	}

	private void deleteEmployeePlanAssignments(Long strategyId, List<ProspectCensusResponse> censusData, List<String> benefitTypes) {
		Set<String> emplIds = censusData.stream().map(ProspectCensusResponse::getEmployeeId).collect(Collectors.toSet());
		employeePlanAssignmentService.deleteEmployeePlanAssignmentForBenTypes(emplIds, strategyId, new HashSet<>(benefitTypes));
	}

	private void saveEmployeePlanAssignments(List<EePlanAssignment> employeePlanAssignments) {
		employeePlanAssignmentService.saveEePlanAssignments(employeePlanAssignments);
	}

	private EePlanAssignment buildEePlanAssignment(EePlanAssignment planAssignment, BigDecimal employeeContribution,
			BigDecimal employerContribution) {
        planAssignment.setEeRate(employeeContribution);
		planAssignment.setErRate(employerContribution);
		return planAssignment;
	}

}