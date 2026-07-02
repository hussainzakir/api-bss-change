package com.trinet.ambis.service.impl.outputs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.trinet.ambis.common.PlanAttributeConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.rest.controllers.dto.outputs.*;
import com.trinet.ambis.service.HrisPlanAttributeService;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.PlanWordWrapUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.service.outputs.EmployeeCostSummaryService;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes.EmployeePlanContribution;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes.PlanContribution;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pallu
 * 
 *         This class is responsible to generate the employee cost comparison
 *         report data
 */
@Service
@Slf4j
public class EmployeeCostSummaryServiceImpl implements EmployeeCostSummaryService {

	@Autowired
	private HrisPlanAttributeService hrisPlanAttributeService;
	
	@Autowired
	private ProspectEmployeeCostService prospectEmployeeCostService;

	@Autowired
	private StrategyDataDao strategyDataDao;

	@Override
	@Async
	public CompletableFuture<BenefitTypeEmployeeCostSummary> getCostSummaryData(OutputData data, Company company,
																				OutputRequest outputRequest) {
		log.info("******* Generating Cost Summary Report Data *******");
		StopWatch taskWatch = new StopWatch(" Cost Summary Report Data *******");
		taskWatch.start();
		// Get all the benefit types codes including the voluntary ones
		List<String> allBenTypeCodes = getAllBenefitTypeCodes();
		Optional<List<StrategyGroupEmployeePlanRateData>> omsStrategyCostResponse = Optional.empty();

		//Filtering not offered benefits type
		allBenTypeCodes = allBenTypeCodes.stream().filter(benType -> filterNotOfferedBenType(data.getTrinetStrategyIsBenTypeOffered()).test(benType)).collect(Collectors.toList());

		// Get an empty cost summary map before setting the data
		BenefitTypeEmployeeCostSummary costSummaryMap = prepareEmptyCostSummaryObj(allBenTypeCodes);

		// API call to prospect-api to get employee details & employee level
		// contribution by given current(prospect) strategy id and benefit type
		Optional<List<EmployeeCostRes>> currentStrategyCostResponse = getCurrentEmployeeCostSummary(company.getCode(),
				allBenTypeCodes);

		// Get employee level contribution for Medical from OMS for given trinet strategy id if prospect has TIB Auth Broker
		if(CompanyServiceHelper.isTibProspect(company)){
			omsStrategyCostResponse = getOmsStrategyCostResponse(List.of(Long.parseLong(outputRequest.getTnStrategyId())), allBenTypeCodes, false);
		}

		// Get employee level contribution by given trinet strategy id and benefit type
		Optional<List<StrategyGroupEmployeePlanRateData>> trinetStrategyCostResponse = getTrinetEmployeeCostSummary(
				company, outputRequest, allBenTypeCodes);

		CompletableFuture<BenefitTypeEmployeeCostSummary> result = CompletableFuture.completedFuture(
				buildCostSummaryMap(currentStrategyCostResponse, trinetStrategyCostResponse, omsStrategyCostResponse, costSummaryMap, data.getCurrStrategyIsBenTypeOffered(), company));
		taskWatch.stop();
		log.info(String.format("****** %s finished in :: %s *******", taskWatch.getId(),
				taskWatch.getTotalTimeMillis()));
		return result;
	}

	@Override
	public Optional<List<StrategyGroupEmployeePlanRateData>> getOmsStrategyCostResponse(List<Long> strategyIds, List<String> benTypeCodes, boolean prependCarrierName) {
		Optional<List<StrategyGroupEmployeePlanRateData>> omsStrategyCostResponse = getOmsEmployeeCostSummary(strategyIds, benTypeCodes);
		updateOmsStrategyCostResponseWithHrisPlans(omsStrategyCostResponse, prependCarrierName);
		return omsStrategyCostResponse;
	}

	private static List<String> getAllBenefitTypeCodes() {
		List<String> benefitTypeCodes = Arrays.stream(OutputBenefitsTypeEnums.values())
				.map(OutputBenefitsTypeEnums::getCode)
				.collect(Collectors.toList());
		if (benefitTypeCodes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {
			benefitTypeCodes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
		}
		if (benefitTypeCodes.contains(BSSApplicationConstants.VISION_PLAN_TYPE)) {
			benefitTypeCodes.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
		}
		return benefitTypeCodes;
	}

	private static BenefitTypeEmployeeCostSummary prepareEmptyCostSummaryObj(List<String> allBenTypeCodes) {
		Map<String, Map<String, List<EmployeeCostSummary>>> costSummaryMapData = new LinkedHashMap<>();
		BenefitTypeEmployeeCostSummary costSummary = new BenefitTypeEmployeeCostSummary(costSummaryMapData);
		// Exclude voluntary plan types while creating while building the cost summary result
		List<String> benTypes = excludeVoluntaryLifeAndDisabilityPlanTypes(allBenTypeCodes);
		benTypes.forEach(benType -> costSummary.getEmplCostSummaryByBenGroup()
				.put(benType, new HashMap<>()));
		return costSummary;
	}

	private static List<String> excludeVoluntaryLifeAndDisabilityPlanTypes(List<String> planTypes) {
		return planTypes.stream()
				.filter(planType -> !planType.equals(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE)
						&& !planType.equals(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE)
						&& !planType.equals(BSSApplicationConstants.LIFE_CODE)
						&& !planType.equals(BSSApplicationConstants.STD_CODE))
				.collect(Collectors.toList());
	}

	private Optional<List<StrategyGroupEmployeePlanRateData>> getOmsEmployeeCostSummary(List<Long> strategyIds, List<String> benTypeCodes) {
		return strategyDataDao.getOmsStrategyGroupPlanCostByPlanType(strategyIds, benTypeCodes);
	}

	private Optional<List<StrategyGroupEmployeePlanRateData>> getTrinetEmployeeCostSummary(Company company,
			OutputRequest outputRequest, List<String> allBenTypeCodes) {
		List<Long> strategyList = List.of(Long.parseLong(outputRequest.getTnStrategyId()));
		return strategyDataDao.getStrategyGroupPlanCostByPlanType(company, strategyList, allBenTypeCodes);
	}

	private Optional<List<EmployeeCostRes>> getCurrentEmployeeCostSummary(String prospectId,
			List<String> allBenTypeCodes) {
		return prospectEmployeeCostService.getProspectEmployeeCostByType(prospectId, excludeVoluntaryLifeAndDisabilityPlanTypes(allBenTypeCodes));
	}

	private static BenefitTypeEmployeeCostSummary buildCostSummaryMap(
			Optional<List<EmployeeCostRes>> currentStrategyCostResponse,
			Optional<List<StrategyGroupEmployeePlanRateData>> trinetStrategyCostResponse,
			Optional<List<StrategyGroupEmployeePlanRateData>> omsStrategyCostResponse,
			BenefitTypeEmployeeCostSummary costSummaryMap,
			Map<String, Boolean> currStrategyIsBenTypeOffered,
			Company company) {
		if (currentStrategyCostResponse.isPresent() && (trinetStrategyCostResponse.isPresent() || omsStrategyCostResponse.isPresent())) {

			Map<String, List<EmployeeCostRes.EmployeePlanContribution>> currentEmplCostSummaryByBenType = new LinkedHashMap<>();
			Map<String, List<StrategyGroupEmployeePlanRateData>> trinetEmplCostSummaryByBenType = new LinkedHashMap<>();
			currentStrategyCostResponse.ifPresent(convertCurrentStrategyResToMap(currentEmplCostSummaryByBenType));

			// Get employee level contribution map for given trinet strategy id
			if(trinetStrategyCostResponse.isPresent()) {
				trinetEmplCostSummaryByBenType = trinetStrategyCostResponse
						.get().stream().filter(type -> Objects.nonNull(type.getPlanType())).collect(
								Collectors.groupingBy(StrategyGroupEmployeePlanRateData::getPlanType, Collectors.toList()));
			}

			if(omsStrategyCostResponse.isPresent() ) {
				// Get employee level contribution map for Medical from OMS for given trinet strategy id if prospect has TIB Auth Broker
				Map<String, List<StrategyGroupEmployeePlanRateData>> omsEmplCostSummaryByBenType = omsStrategyCostResponse
						.get().stream().filter(type -> Objects.nonNull(type.getPlanType())).collect(
								Collectors.groupingBy(StrategyGroupEmployeePlanRateData::getPlanType, Collectors.toList()));

				// Check if Oms strategy data is available, replace it with OMS strategy data for each plan type
				populateOmsStrategyData(trinetEmplCostSummaryByBenType, omsEmplCostSummaryByBenType, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
				populateOmsStrategyData(trinetEmplCostSummaryByBenType, omsEmplCostSummaryByBenType, BSSApplicationConstants.DENTAL_PLAN_TYPE);
				populateOmsStrategyData(trinetEmplCostSummaryByBenType, omsEmplCostSummaryByBenType, BSSApplicationConstants.VISION_PLAN_TYPE);
			}

			currentEmplCostSummaryByBenType.forEach((key, value) -> value
					.sort(Comparator.comparing(EmployeeCostRes.EmployeePlanContribution::getFirstName)));

			// Build cost summary map of employees by plan types
			currentEmplCostSummaryByBenType
					.forEach(buildEmployeeCostSummary(trinetEmplCostSummaryByBenType, costSummaryMap, currStrategyIsBenTypeOffered, company));
		}
		return costSummaryMap;
	}

	private static Consumer<List<EmployeeCostRes>> convertCurrentStrategyResToMap(
			Map<String, List<EmployeeCostRes.EmployeePlanContribution>> currentStrategyResponseMap) {

		return prospectResponseList -> {

			prospectResponseList.forEach(employeeCostRes -> currentStrategyResponseMap
					.put(employeeCostRes.getBenefitTypeCode(), employeeCostRes.getEmployeePlanContribution()));
		};
	}

	private static BiConsumer<String, List<EmployeeCostRes.EmployeePlanContribution>> buildEmployeeCostSummary(
			Map<String, List<StrategyGroupEmployeePlanRateData>> trinetEmplCostSummaryByBenType,
			BenefitTypeEmployeeCostSummary costSummaryMap,
			Map<String, Boolean> currStrategyIsBenTypeOffered,
			Company company) {

		return (benType, currentEmplCostSummaries) -> {
			List<EmployeeCostSummary> employeeTotalCostSummaryList = new ArrayList<>();
			currentEmplCostSummaries.forEach(currentEmplCostSummary -> {

			List<StrategyGroupEmployeePlanRateData> trinetEmplCostSummaries;

			// Check for voluntary plan assignments for dental and vision plan types
			if(benType.equals(BSSApplicationConstants.DENTAL_PLAN_TYPE) || benType.equals(BSSApplicationConstants.VISION_PLAN_TYPE)) {
				trinetEmplCostSummaries = checkForVoluntaryAssignments(benType, trinetEmplCostSummaryByBenType);
			}
			else {
				trinetEmplCostSummaries = trinetEmplCostSummaryByBenType
						.get(benType);
			}
			if (trinetEmplCostSummaries != null) {
				Map<String, StrategyGroupEmployeePlanRateData> trinetEmplCostSummariesByEmplId = trinetEmplCostSummaries
						.stream()
						.collect(Collectors.toMap(StrategyGroupEmployeePlanRateData::getEmplId, Function.identity()));
				String emplId = currentEmplCostSummary.getEmployeeId();

				// if current employee benefits are waived, add it to employee cost summary list with all null values assigned
				if(currentEmplCostSummary.getCovgLevel().equalsIgnoreCase(ProspectConstants.WAVED_COVERAGE)) {
					EmployeeDetails employeeDetails = EmployeeDetails.builder().firstName(currentEmplCostSummary.getFirstName())
							.lastName(currentEmplCostSummary.getLastName().substring(0, 1).toUpperCase()).state(currentEmplCostSummary.getState())
							.coverageCode(currentEmplCostSummary.getCovgLevel()).group(currentEmplCostSummary.getGroupName())
							.build();

					EmployeeCostSummary employeeCostSummary = EmployeeCostSummary.builder().employee(employeeDetails)
							.currentPlan(null).triNetPlan(null)
							.costDiff(0).build();
					employeeTotalCostSummaryList.add(employeeCostSummary);
				}

				// if TriNet strategy data is available then build employee cost summary, else throw an error
				else {
                    StrategyGroupEmployeePlanRateData trinetPlanData = trinetEmplCostSummariesByEmplId.get(emplId);

                    PlanContribution currPlanContribution = currentEmplCostSummary.getPlanContribution();
                    EmployeeDetails employeeDetails = buildEmployeeDetails(currentEmplCostSummary, trinetPlanData);

                    BenefitTypeTotal currentPlan = buildCurrentBenTypeTotalObj(currPlanContribution);
                    BenefitTypeTotal triNetPlan = buildTrinetBenTypeTotalObj(trinetPlanData);

                    EmployeeCostSummary employeeCostSummary = EmployeeCostSummary.builder().employee(employeeDetails)
                            .currentPlan(currentPlan).triNetPlan(triNetPlan)
                            .costDiff(triNetPlan.getTotal().subtract(currentPlan.getTotal()).intValue()).build();

                    employeeTotalCostSummaryList.add(employeeCostSummary);
				}
			}
		});
			if (AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()) {
				costSummaryMap.getEmplCostSummaryByBenGroup().put(benType, employeeTotalCostSummaryList.stream()
						.collect(Collectors.groupingBy(costSummary -> costSummary.getEmployee().getGroup())));

				if (AppRulesAndConfigsUtils.isEmployeeComparePageBreakEnabled()) {
					Map<String, List<EmployeeCostSummary>> groupedByGroup = costSummaryMap.getEmplCostSummaryByBenGroup().get(benType);
					if (groupedByGroup != null) {
						int maxLinesFirstPage = BSSApplicationConstants.EMPLOYEE_COST_SUMMARY_MAX_LINES_FIRST_PAGE;
						int maxLinesSubsequentPages = BSSApplicationConstants.EMPLOYEE_COST_SUMMARY_MAX_LINES_SUBSEQUENT_PAGES;

						if (CompanyServiceHelper.isTibProspect(company)) {
							maxLinesFirstPage -= BSSApplicationConstants.EMPLOYEE_COST_SUMMARY_HEADER_LINES;
						}

						int currentLines = 0;
						boolean isFirstPage = true;
						EmployeeCostSummary lastEmployee = null;

						for (Map.Entry<String, List<EmployeeCostSummary>> entry : groupedByGroup.entrySet()) {
							int groupLines = 3;

							int currentMaxLines = isFirstPage ? maxLinesFirstPage : maxLinesSubsequentPages;
							if (currentLines > 0 && currentLines + groupLines > currentMaxLines) {
								if (lastEmployee != null) {
									lastEmployee.setPageBreak(true);
								}
								currentLines = 0;
								isFirstPage = false;
							}

							currentLines += groupLines;

							List<EmployeeCostSummary> employees = entry.getValue();
							boolean isCurrBenTypeOffered = currStrategyIsBenTypeOffered != null && Boolean.TRUE.equals(currStrategyIsBenTypeOffered.get(benType));
							Map<String, Double> limits = isCurrBenTypeOffered ? PlanAttributeConstants.LIMITS_MAP.get(PlanAttributeConstants.EE_COST_STD) : PlanAttributeConstants.LIMITS_MAP.get(PlanAttributeConstants.EE_COST_ALT);
							for (int i = 0; i < employees.size(); i++) {
								EmployeeCostSummary emp = employees.get(i);

								int empLines = PlanWordWrapUtil.calculateMaxLinesForEmployeeCostSummary(emp, limits);

								currentMaxLines = isFirstPage ? maxLinesFirstPage : maxLinesSubsequentPages;

								if (currentLines > 0 && currentLines + empLines > currentMaxLines) {
									if (lastEmployee != null) {
										lastEmployee.setPageBreak(true);
									}
									currentLines = 0;
									isFirstPage = false;
								}

								currentLines += empLines;
								emp.setPageBreak(false); // Default
								lastEmployee = emp;
							}
						}
					}
				}
			} else {
				costSummaryMap.getEmplCostSummaryByBenGroup().put(benType, employeeTotalCostSummaryList.stream()
						.collect(Collectors.groupingBy(costSummary -> costSummary.getEmployee().getGroup())));
			}
		};
	}

	private static List<StrategyGroupEmployeePlanRateData> checkForVoluntaryAssignments(String benType,
			Map<String, List<StrategyGroupEmployeePlanRateData>> trinetEmplCostSummaryByBenType) {

		List<StrategyGroupEmployeePlanRateData> trinetEmplCostSummaries = new ArrayList<>();

		if (benType.equals(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {

			if (trinetEmplCostSummaryByBenType.containsKey(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE)) {
				trinetEmplCostSummaries
						.addAll(trinetEmplCostSummaryByBenType.get(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE));
			}
			if (trinetEmplCostSummaryByBenType.containsKey(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {
				trinetEmplCostSummaries.addAll(trinetEmplCostSummaryByBenType.get(benType));
			}
		}

		if (benType.equals(BSSApplicationConstants.VISION_PLAN_TYPE)) {
			if (trinetEmplCostSummaryByBenType.containsKey(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE)) {
				trinetEmplCostSummaries
						.addAll(trinetEmplCostSummaryByBenType.get(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE));
			}
			if (trinetEmplCostSummaryByBenType.containsKey(BSSApplicationConstants.VISION_PLAN_TYPE)) {
				trinetEmplCostSummaries.addAll(trinetEmplCostSummaryByBenType.get(benType));
			}

		}

		return trinetEmplCostSummaries;
	}

	private static EmployeeDetails buildEmployeeDetails(EmployeePlanContribution currentEmplCostSummary, StrategyGroupEmployeePlanRateData trinetPlanData) {
		return EmployeeDetails.builder().firstName(currentEmplCostSummary.getFirstName())
				.lastName(currentEmplCostSummary.getLastName().substring(0, 1).toUpperCase()).state(currentEmplCostSummary.getState())
				.coverageCode(currentEmplCostSummary.getCovgLevel())
                .group(trinetPlanData != null ? trinetPlanData.getGroupName() : currentEmplCostSummary.getGroupName())
				.build();
	}

	private static BenefitTypeTotal buildCurrentBenTypeTotalObj(PlanContribution currPlanContribution) {

		if(currPlanContribution.getBenefitPlanId() == 0 && currPlanContribution.getBenefitPlanName() == null) {
			return BenefitTypeTotal.builder().planName("--")
					.erAmount(BigDecimal.ZERO.setScale(2))
					.eeAmount(BigDecimal.ZERO.setScale(2))
					.total(BigDecimal.ZERO.setScale(2)).build();
		}

		return BenefitTypeTotal.builder().planName(currPlanContribution.getBenefitPlanName())
				.erAmount(currPlanContribution.getErCost().setScale(2))
				.eeAmount(currPlanContribution.getEeCost().setScale(2))
				.total(currPlanContribution.getTotalCost().setScale(2)).build();
	}

	private static BenefitTypeTotal buildTrinetBenTypeTotalObj(StrategyGroupEmployeePlanRateData trinetPlanData) {

        if(trinetPlanData == null) {
            return BenefitTypeTotal.builder().planName("--")
                    .erAmount(BigDecimal.ZERO.setScale(2))
                    .eeAmount(BigDecimal.ZERO.setScale(2))
                    .total(BigDecimal.ZERO.setScale(2)).build();
        }

		return BenefitTypeTotal.builder().planName(trinetPlanData.getPlanName())
				.erAmount(trinetPlanData.getErRate().setScale(2))
				.eeAmount(trinetPlanData.getEeRate().setScale(2))
				.total(trinetPlanData.getErRate().add(trinetPlanData.getEeRate()).setScale(2)).build();
	}

	private Predicate<String> filterNotOfferedBenType(Map<String, Boolean> trinetStrategyIsBenTypeOffered) {
		return benType -> {
			Optional<Boolean> benTypePresent = Optional.of(benType)
					.filter(type -> type.equals(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE))
					.map(type -> trinetStrategyIsBenTypeOffered.containsKey(OutputBenefitsTypeEnums.DENTAL.getDisplayName()));
			benTypePresent =  benTypePresent.or(() -> Optional.of(benType)
					.filter(type -> type.equals(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE))
					.map(type -> trinetStrategyIsBenTypeOffered.containsKey(OutputBenefitsTypeEnums.VISION.getDisplayName())));
			benTypePresent = benTypePresent.or(() -> Optional.of(benType)
					.filter(type -> !ObjectUtils.isEmpty(trinetStrategyIsBenTypeOffered) && trinetStrategyIsBenTypeOffered.containsKey(OutputBenefitsTypeEnums.getDisplayNameByCode(type)))
					.map(type -> trinetStrategyIsBenTypeOffered.get(OutputBenefitsTypeEnums.getDisplayNameByCode(benType))));
			return benTypePresent.orElse(false);
		};
	}

	private void updateOmsStrategyCostResponseWithHrisPlans(Optional<List<StrategyGroupEmployeePlanRateData>> omsStrategyCostResponse, boolean prependCarrierName) {
		if (omsStrategyCostResponse.isEmpty()) {
			return;
		}

		Map<String, Set<String>> benefitPlanIdsByPlanType = omsStrategyCostResponse.get().stream()
				.collect(Collectors.groupingBy(
						StrategyGroupEmployeePlanRateData::getPlanType,
						Collectors.mapping(StrategyGroupEmployeePlanRateData::getBenefitPlan, Collectors.toSet())
				));

		for (Map.Entry<String, Set<String>> entry : benefitPlanIdsByPlanType.entrySet()) {
			Map<String, BenefitPlanCompare> benefitPlanCompareMap;
			String planType = entry.getKey();
			Set<String> benefitPlanIds = entry.getValue();
			if (benefitPlanIds.isEmpty()) {
				continue;
			}

			benefitPlanCompareMap = getBenefitPlanCompareMap(planType, benefitPlanIds);

			omsStrategyCostResponse.ifPresent(strategyGroupEmployeePlanRateDataList -> strategyGroupEmployeePlanRateDataList
					.forEach(employeePlanRateData -> {
						if (planType.equals(employeePlanRateData.getPlanType())) {
							BenefitPlanCompare benefitPlanCompare = benefitPlanCompareMap.get(employeePlanRateData.getBenefitPlan());
							if (benefitPlanCompare != null) {
								employeePlanRateData.setPlanName(
										(prependCarrierName ? benefitPlanCompare.getCarrier() + " " : "")
												+ benefitPlanCompare.getName());
								employeePlanRateData.setCarrier(benefitPlanCompare.getCarrier());
							} else {
								log.error("Missing plan attributes data for benefitPlan {}", employeePlanRateData.getBenefitPlan());
							}
						}
					})
			);
			}
	}

	private static void populateOmsStrategyData(Map<String, List<StrategyGroupEmployeePlanRateData>> trinetEmplCostSummaryByBenType,
										 Map<String, List<StrategyGroupEmployeePlanRateData>> omsEmplCostSummaryByBenType,
										 String planType) {
		if(!ObjectUtils.isEmpty(omsEmplCostSummaryByBenType) && omsEmplCostSummaryByBenType.containsKey(planType)) {
			trinetEmplCostSummaryByBenType.put(planType, omsEmplCostSummaryByBenType.get(planType));
		}
	}

	private Map<String, BenefitPlanCompare> getBenefitPlanCompareMap(String planType, Set<String> benefitPlanIds) {
		Map<String, BenefitPlanCompare> benefitPlanCompareMap;
		try {
			List<BenefitPlanCompare> benefitPlanCompareList = hrisPlanAttributeService
					.getPlanAttributesByBenefitType(benefitPlanIds, PlanTypesEnum.getName(planType)).get();
			benefitPlanCompareMap = benefitPlanCompareList.stream()
					.collect(Collectors.toMap(BenefitPlanCompare::getPlanId, benefitPlanCompare -> benefitPlanCompare));
		} catch (Exception ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			log.error("Error while getting plan attributes form HrisPlanAttributeService for benefitPlanIds {}", benefitPlanIds);
			throw new BSSApplicationException(ex,
					new BSSApplicationError("Error occurred while getting plan data from HrisPlanAttribute service."));
		}
		return benefitPlanCompareMap;
	}


}
