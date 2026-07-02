package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.ps.LifeAndDisabilityCalcData;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse.PlanDetails;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse.Quotes;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse.TotalCostDetails;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProspectStrategyIntegrationService;
import com.trinet.ambis.service.model.AdditionalBenefitEmployeeDetails;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.EmployeeStrategyGroupDetails;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.response.CensusRes;
import com.trinet.ambis.util.RulesAndConfigsUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProspectStrategyIntegrationServiceImpl implements ProspectStrategyIntegrationService {
	
	@Autowired
	AdditionalBenefitPlanService additionalBenefitPlanService;

	@Autowired
	LifeAndDisabilityCalcData lifeAndDisabilityCalcData;

	@Autowired
	ProspectEmployeeService prospectEmployeeService;

	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;
	
	@Autowired
	CompanyService companyService;

	@Autowired
	StrategyDataDao strategyDataDao;

	@Autowired
	StrategyDao strategyDao;

	@Override
	public ProspectBenefitsSummaryTotalsResponse getBenefitsSummaryTotalsForStrategy(Long strategyId, Company company,
			BenExchngEnums benExchngEnums) {

		Optional<Strategy> strategyOptional = strategyDao.findById(strategyId);

		if (!strategyOptional.isPresent()) {
			throw new BSSApplicationException(new BSSApplicationError(null, BSSHttpStatusConstants.NOT_FOUND,
					ProspectStrategyIntegrationServiceImpl.class.getName(), "Strategy not found", null, null));
		}

		Strategy strategy = strategyOptional.get();
		String exchangeName = benExchngEnums.getExchangeName();

		ProspectBenefitsSummaryTotalsResponse benefitTotalsResponse = ProspectBenefitsSummaryTotalsResponse.builder()
				.exchange(exchangeName).strategyName(strategy.getName()).type(strategy.getType())
				.proposalId(company.getProposalId())
				.bundleId(company.getBundleId() != null ? String.valueOf(company.getBundleId()) : null)
				   .bundleName(company.getBundleName() != null ? company.getBundleName() : "").build();

		List<ProspectBenefitsSummaryTotalsResponse.Quotes> primaryPlans = getPrimaryPlans(company, strategyId);
		List<ProspectBenefitsSummaryTotalsResponse.Quotes> additionalBenefitsQuoteData = getAdditionalBenefitsData(company, strategyId); //Additional
		
		benefitTotalsResponse.setQuotes(primaryPlans);
		benefitTotalsResponse.getQuotes().addAll(additionalBenefitsQuoteData);

		return benefitTotalsResponse;
	}

	private List<ProspectBenefitsSummaryTotalsResponse.Quotes> getPrimaryPlans(Company company, Long strategyId) {

		Optional<List<StrategyGroupEmployeePlanRateData>> strategyGroupEmployeePlanRateDataOptional = strategyDataDao
				.getStrategyGroupPlanCostByPlanType(company, List.of(strategyId),
						BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);

		if (!strategyGroupEmployeePlanRateDataOptional.isPresent()) {
			return new ArrayList<>();
		}

		Map<String, List<StrategyGroupEmployeePlanRateData>> benefitTypeToEmployeesWithPlansMap = strategyGroupEmployeePlanRateDataOptional
				.get().stream()
				.filter(key -> key.getPlanType() != null)
				.collect(Collectors.groupingBy(StrategyGroupEmployeePlanRateData::getPlanType));

		List<ProspectBenefitsSummaryTotalsResponse.Quotes> quotesList = new ArrayList<>();

		benefitTypeToEmployeesWithPlansMap.entrySet().forEach(benefitTypeEmployeePlans -> {

			List<MonthlyCostByEmployee> monthlyCostList = new ArrayList<>();
			List<PlanDetails> planDetailsList = new ArrayList<>();
			BigDecimal totalAnnualCost = BigDecimal.ZERO;
			BigDecimal employeeTotalCost = BigDecimal.ZERO;
			BigDecimal employerTotalCost = BigDecimal.ZERO;
			Long employeeCount = 0L;

			String benefitType = benefitTypeEmployeePlans.getKey();
			List<StrategyGroupEmployeePlanRateData> employeePlans = benefitTypeEmployeePlans.getValue();

			for (StrategyGroupEmployeePlanRateData employeePlan : employeePlans) {
				// Calculates the total cost
				employeeTotalCost = employeeTotalCost.add(employeePlan.getEeRate());
				employerTotalCost = employerTotalCost.add(employeePlan.getErRate());
				totalAnnualCost = employerTotalCost.multiply(BigDecimal.valueOf(12));
				
				monthlyCostList.add(MonthlyCostByEmployee.builder().eeIdentifier(employeePlan.getEmplId())
						.planId(employeePlan.getBenefitPlan()).employee(employeePlan.getEeRate())
						.employer(employeePlan.getErRate())
						.total(employeePlan.getEeRate().add(employeePlan.getErRate())).build());

				PlanDetails planDetail = PlanDetails.builder().planId(employeePlan.getBenefitPlan())
						.name(employeePlan.getPlanName()).carrierName(employeePlan.getCarrier()).build();
				if (!planDetailsList.contains(planDetail))
					planDetailsList.add(planDetail);
			}
			employeeCount = Long.valueOf(monthlyCostList.size());
			quotesList.add(Quotes.builder().benefitType(PlanTypesEnum.getSfdcCode(benefitType))
					.planDetails(planDetailsList)
					.totalCostDetails(TotalCostDetails.builder().totalAnnualCost(totalAnnualCost)
							.employeeTotalCost(employeeTotalCost).employerTotalCost(employerTotalCost)
							.employeeCount(employeeCount).monthlyCostByEmployee(monthlyCostList).build())
					.build());
		});

		return quotesList;

	}


	private List<ProspectBenefitsSummaryTotalsResponse.Quotes> getAdditionalBenefitsData(Company company, Long strategyId) {

		Map<Long, List<DisabilityBenefitOptionPlans>> additionalBenefitPlansByGroup = strategyDataDao.getAdditionalBenefitPlansForStrategyWithSdiInfo(strategyId, company.getPlanStartDate(), company.getRealmPlanYearId());
		//Remove any employee paid plans as we do not want to send them in the quote
		additionalBenefitPlansByGroup.forEach((groupId, disabilityPlans) -> {
			disabilityPlans.removeIf(DisabilityBenefitOptionPlans::isEmployeePaid);
		});

		if( MapUtils.isEmpty(additionalBenefitPlansByGroup))
			return Collections.emptyList();
			
		Map<String, ProspectBenefitsSummaryTotalsResponse.Quotes> quotesPlanDetailsByPlanType = getAdditionalQuotesPlanDetailsByPlanType(additionalBenefitPlansByGroup);

		Map<String, ProspectBenefitsSummaryTotalsResponse.TotalCostDetails> totalCostDetailsByPlanType = getAdditionalTotalCostDetailsByPlanType(company, strategyId, additionalBenefitPlansByGroup);

		for (Map.Entry<String, ProspectBenefitsSummaryTotalsResponse.Quotes> entry : quotesPlanDetailsByPlanType.entrySet()) {
			String planType = entry.getKey();
			ProspectBenefitsSummaryTotalsResponse.Quotes quotes = entry.getValue();
			ProspectBenefitsSummaryTotalsResponse.TotalCostDetails totalCostDetails = totalCostDetailsByPlanType.get(planType);
			quotes.setTotalCostDetails(totalCostDetails); //
		}

		return new ArrayList<>(quotesPlanDetailsByPlanType.values());
	}

	private Map<String, ProspectBenefitsSummaryTotalsResponse.Quotes> getAdditionalQuotesPlanDetailsByPlanType(Map<Long, List<DisabilityBenefitOptionPlans>> additionalBenefitPlansByGroup) {

		Map<String, List<ProspectBenefitsSummaryTotalsResponse.PlanDetails>> planDetailsByPlanType = getAdditionalPlanDetails(additionalBenefitPlansByGroup);

		Map<String, ProspectBenefitsSummaryTotalsResponse.Quotes> quotesByPlanType = new HashMap<>();
		for (Map.Entry<String, List<ProspectBenefitsSummaryTotalsResponse.PlanDetails>> entry : planDetailsByPlanType.entrySet()) {
			String planType = entry.getKey();
			List<ProspectBenefitsSummaryTotalsResponse.PlanDetails> planDetails = entry.getValue();
			ProspectBenefitsSummaryTotalsResponse.Quotes quotes = new ProspectBenefitsSummaryTotalsResponse.Quotes();
			quotes.setBenefitType(PlanTypesEnum.getSfdcCode(planType));
			quotes.setPlanDetails(planDetails);
			quotesByPlanType.put(planType, quotes);
		}
		return quotesByPlanType;
	}

	private Map<String, List<ProspectBenefitsSummaryTotalsResponse.PlanDetails>> getAdditionalPlanDetails(Map<Long, List<DisabilityBenefitOptionPlans>> additionalBenefitPlansByGroup) {
		Map<String, List<ProspectBenefitsSummaryTotalsResponse.PlanDetails>> planDetailsByPlanType = new HashMap<>();
		Set<String> planIds = new HashSet<>();
		additionalBenefitPlansByGroup.forEach((groupId, disabilityPlans) -> {
			disabilityPlans.forEach(disabilityBenefitOptionPlans -> {
				if (!planIds.contains(disabilityBenefitOptionPlans.getId())) {
					ProspectBenefitsSummaryTotalsResponse.PlanDetails planDetails = new ProspectBenefitsSummaryTotalsResponse.PlanDetails();
					planDetails.setPlanId(disabilityBenefitOptionPlans.getId());
					planDetails.setName(disabilityBenefitOptionPlans.getPlanDesc());
					planDetails.setCarrierName(disabilityBenefitOptionPlans.getCarrierName());
					planDetailsByPlanType.computeIfAbsent(disabilityBenefitOptionPlans.getPlanType(), key -> new ArrayList<>()).add(planDetails);
					planIds.add(disabilityBenefitOptionPlans.getId());
				}
			});
		});
		return planDetailsByPlanType;
	}

	private Map<String, ProspectBenefitsSummaryTotalsResponse.TotalCostDetails> getAdditionalTotalCostDetailsByPlanType(Company company, Long strategyId, Map<Long, List<DisabilityBenefitOptionPlans>> additionalBenefitPlansByGroup) {

		List<CensusRes> prospectEmployees = prospectEmployeeService.getEmployees(company.getCode());

		Map<String, EmployeeStrategyGroupDetails> trinetEmployeesByEmplId = employeeBenefitGroupDao
				.getEmployeeDetailsByStrategy(strategyId);

		Map<String, List<ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee>> monthlyCostByEmployeesAndPlanType = getAdditionalMonthlyCostByEmployeesAndPlanType(prospectEmployees, trinetEmployeesByEmplId, additionalBenefitPlansByGroup, company, strategyId);

		Map<String, ProspectBenefitsSummaryTotalsResponse.TotalCostDetails> totalCostDetailsByPlanType = calcAdditionalTotalCostDetailsByPlanType(monthlyCostByEmployeesAndPlanType);

		return totalCostDetailsByPlanType;

	}

	private Map<String, Map<String, BigDecimal>> getAdditionalGroupPlanCostMap(Company company, Long strategyId, Map<String, Set<String>> additionalBenefitPlansByPlanType) {
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());

		Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = lifeAndDisabilityCalcData
				.getGroupEmployeeSelections(company, false, strategyId, isVendorMappingOn);

		return additionalBenefitPlanService.calculateAdditionalPlansCostByGroup(company, false,
				additionalBenefitPlansByPlanType, employeeSelection);

	}

	private Map<String, List<ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee>> getAdditionalMonthlyCostByEmployeesAndPlanType(
			List<CensusRes> prospectEmployees, Map<String, EmployeeStrategyGroupDetails> trinetEmployeesByEmplId,
			Map<Long, List<DisabilityBenefitOptionPlans>> additionalBenefitPlansByGroup, Company company, Long strategyId) {

		Map<String, List<ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee>> monthlyCostByEmployeesAndPlanType = new HashMap<>();

		Map<String, Set<String>> additionalBenefitPlansByPlanType = new HashMap<>();

		additionalBenefitPlansByGroup.forEach((groupId, disabilityPlans) -> {
			disabilityPlans.forEach(disabilityBenefitOptionPlans -> {
				additionalBenefitPlansByPlanType.computeIfAbsent(disabilityBenefitOptionPlans.getPlanType(), key -> new HashSet<>()).add(disabilityBenefitOptionPlans.getId());
			});
		});

		Map<String, Map<String, BigDecimal>> groupPlanCostMap = getAdditionalGroupPlanCostMap(company, strategyId, additionalBenefitPlansByPlanType);
		List<String> processedPlans = new ArrayList<>();
		for (CensusRes prospectEmployee : prospectEmployees) {
			processedPlans = new ArrayList<>();
			String emplId = prospectEmployee.getEmployeeId();
			String homeState = prospectEmployee.getHomeState();
			EmployeeStrategyGroupDetails triNetEmployee = trinetEmployeesByEmplId.get(emplId);
			if (triNetEmployee != null) {
				Long groupId = triNetEmployee.getFutureGroupId();
				String benefitProgram = triNetEmployee.getFutureBenefitProgram();
				boolean sdiEmpoyee = company.getSdiStates().contains(homeState);

				Map<String, BigDecimal> groupPlanCosts = groupPlanCostMap.get(benefitProgram);
				List<DisabilityBenefitOptionPlans> groupPlans = additionalBenefitPlansByGroup.get(groupId);
				for (DisabilityBenefitOptionPlans groupPlan : groupPlans) {
					if (processedPlans.contains(groupPlan.getId())) {
						continue;
					}
					if (BSSApplicationConstants.STD_CODE.equals(groupPlan.getPlanType())) {
						if (BenExchngEnums.TRINET_IV.getBenExchng().equals(company.getRealm().getBenExchange())
								&& company.getRealmPlanYearId() < 86) {
							if ((groupPlan.isSdiPlan() && sdiEmpoyee) || !groupPlan.isSdiPlan()) {
								setAdditionalMonthlyCostByEmployeesAndPlanType(monthlyCostByEmployeesAndPlanType, groupPlan, emplId, groupPlanCosts);
							}
						}
						else if (groupPlan.isSdiPlan() == sdiEmpoyee) {
							setAdditionalMonthlyCostByEmployeesAndPlanType(monthlyCostByEmployeesAndPlanType, groupPlan, emplId, groupPlanCosts);
						}
					} else {
						setAdditionalMonthlyCostByEmployeesAndPlanType(monthlyCostByEmployeesAndPlanType, groupPlan, emplId, groupPlanCosts);
					}
					processedPlans.add(groupPlan.getId());
				}
			} else {
				log.error(String.format("ProspectStrategyIntegrationService: Employee %s not found in TriNet", emplId));
			}
		}

		return monthlyCostByEmployeesAndPlanType;
	}


	private void setAdditionalMonthlyCostByEmployeesAndPlanType(Map<String, List<ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee>> monthlyCostByEmployeesAndPlanType,
																DisabilityBenefitOptionPlans groupPlan, String emplId, Map<String, BigDecimal> groupPlanCosts) {
		BigDecimal planCost = groupPlanCosts.get(groupPlan.getId());

		ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee monthlyCostByEmployee = new ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee();
		monthlyCostByEmployee.setEeIdentifier(emplId);
		monthlyCostByEmployee.setPlanId(groupPlan.getId());
		monthlyCostByEmployee.setEmployer(planCost);
		monthlyCostByEmployee.setEmployee(BigDecimal.ZERO);
		monthlyCostByEmployee.setTotal(planCost);

		if (monthlyCostByEmployeesAndPlanType.containsKey(groupPlan.getPlanType())) {
			monthlyCostByEmployeesAndPlanType.get(groupPlan.getPlanType()).add(monthlyCostByEmployee);
		} else {
			List<ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee> monthlyCostByEmployees = new ArrayList<>();
			monthlyCostByEmployees.add(monthlyCostByEmployee);
			monthlyCostByEmployeesAndPlanType.put(groupPlan.getPlanType(), monthlyCostByEmployees);
		}
	}

	private Map<String, ProspectBenefitsSummaryTotalsResponse.TotalCostDetails> calcAdditionalTotalCostDetailsByPlanType(Map<String, List<ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee>> monthlyCostByEmployeesAndPlanType) {
		Map<String, ProspectBenefitsSummaryTotalsResponse.TotalCostDetails> totalCostDetailsByPlanType = new HashMap<>();
		monthlyCostByEmployeesAndPlanType.entrySet().forEach(entry -> {
			String planType = entry.getKey();
			List<ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee> monthlyCostByEmployees = entry.getValue();
			BigDecimal totalCost = monthlyCostByEmployees.stream()
					.map(ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee::getTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal eeCost = monthlyCostByEmployees.stream()
					.map(ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee::getEmployee)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal erCost = monthlyCostByEmployees.stream()
					.map(ProspectBenefitsSummaryTotalsResponse.MonthlyCostByEmployee::getEmployer)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			Long employeeCount = Long.valueOf(monthlyCostByEmployees.size());

			ProspectBenefitsSummaryTotalsResponse.TotalCostDetails totalCostDetails = new ProspectBenefitsSummaryTotalsResponse.TotalCostDetails();
			totalCostDetails.setTotalAnnualCost(totalCost.multiply(BigDecimal.valueOf(12)));
			totalCostDetails.setEmployeeTotalCost(eeCost);
			totalCostDetails.setEmployerTotalCost(erCost);
			totalCostDetails.setEmployeeCount(employeeCount);
			totalCostDetails.setMonthlyCostByEmployee(monthlyCostByEmployees);
			totalCostDetailsByPlanType.put(planType, totalCostDetails);

		});
		return totalCostDetailsByPlanType;
	}
}
