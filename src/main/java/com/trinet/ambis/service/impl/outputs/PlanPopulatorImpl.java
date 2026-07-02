package com.trinet.ambis.service.impl.outputs;

import static com.trinet.ambis.helper.PlanCompareHelper.prepareDentalBenType;
import static com.trinet.ambis.helper.PlanCompareHelper.prepareVisionBenType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.trinet.ambis.rest.controllers.dto.outputs.CurrentTrinetPlans;
import com.trinet.ambis.service.*;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.AttributeBuilderHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.TierRate;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.outputs.PlanPopulator;
import com.trinet.ambis.service.outputs.Populator;
import com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes.BenefitPlan;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;
import com.trinet.ambis.service.prospect.service.ProspectBenefitsPlansRatesService;
import com.trinet.ambis.util.StrategyUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class PlanPopulatorImpl implements PlanPopulator {
	
	@Autowired
	private BenefitsPlanViewService benefitsPlanViewService;
	
	@Autowired
	private EmployeePlanAssignmentService emplPlanAssignmentService;
	
	@Autowired
	private ProspectBenefitsPlansRatesService prospectBenefitsPlansRatesService;
	
	@Autowired
	private HrisPlanAttributeService hrisPlanAttributeService;
	
	@Autowired
	private PlanCompareService planCompareService;

	@Autowired
	private BenefitPlanService benefitPlanService;
	
	@Override
	public Populator populateCurrentPlan() {
		return currentTrinetPlans -> {
			List<EePlanAssignment> emplPlanAssignment = emplPlanAssignmentService
					.getEmployeePlanAssigmentBy(List.of(Long.valueOf(currentTrinetPlans.getStrategyId())));
			
			// Map Key PlanId, Key coverageCode Value EmployeeId
			Map<String, Map<String, List<String>>> trinetPlanCoverageEmployeeMapping = emplPlanAssignment.stream()
							.map(benefit -> Map.entry(benefit.getBenefitPlan(),
									Map.entry(benefit.getCovrgCD(), benefit.getEePlanAssignmentPK().getEmplId())))
					.collect(Collectors.groupingBy(Map.Entry::getKey,
							Collectors.groupingBy(entry -> entry.getValue().getKey(),
									Collectors.mapping(entry -> entry.getValue().getValue(), Collectors.toList()))));
		
			List<EmployeePlansRes> prospectEmployee = currentTrinetPlans.getProspectEmployees().get();
			
			boolean noCurrentPlan = prospectEmployee.stream().anyMatch(empl -> CollectionUtils.isEmpty(empl.getBenefitPlans()));
			currentTrinetPlans.setNoCurrentPlanEmployees(prospectEmployee.stream()
					.filter(employee -> CollectionUtils.isEmpty(employee.getBenefitPlans()))
					.collect(Collectors.toList()));			
			Map<String, List<EePlanAssignment>> trinetBenTypeToEEPlanAssignments = emplPlanAssignment.stream().collect(
					Collectors.groupingBy(eeAssingnment -> eeAssingnment.getEePlanAssignmentPK().getBenefitType()));
			
			// setting for Trinet plan rates and head count usage
			currentTrinetPlans.setCurrentTrinetPlansMapping(currentTrinetPlanMapping(prospectEmployee,
					trinetBenTypeToEEPlanAssignments));
			
			Map<String, Set<BenefitPlan>> ids = currentTrinetPlans.getProspectEmployees().isPresent()
					? getPlanIds(currentTrinetPlans.getProspectEmployees().get())
					: null;
			Map<String, List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes = new HashMap<>();
			List<String> allPlanIds = new LinkedList<>();
			
			Map<String, String> bplToBcrIdMap = ids.values().stream().flatMap(Set::stream)
					.filter(plan -> plan.getBplPlanId() != null).collect(Collectors.toMap(BenefitPlan::getBplPlanId,
							BenefitPlan::getBenefitPlanId, (existing, replacement) -> existing, LinkedHashMap::new));
			// Getting benefitType plan attributes form planView service and setting it to
			ids.keySet().forEach(key -> {
				Set<String> planIds = prepareCurrentTypePlan(ids).apply(key);
				planIds.forEach(planId -> allPlanIds.add(bplToBcrIdMap.getOrDefault(planId, planId)));
				try {
					if (currentTrinetPlans.getRequestBenefitTypes().contains(key)) {
						List<BenefitPlanCompare> benefitPlanCompares = planCompareService
								.getPlanAttributes(planIds,
										currentTrinetPlans.getCompany().getRealmPlanYear().getPlanYearEnd(),
										BSSApplicationConstants.databaseTemplatesMap.get(key),
										currentTrinetPlans.getHttpRequest())
								.get();
						
						benefitPlanCompares.forEach(planCompare -> {
							String bcrId = bplToBcrIdMap.getOrDefault(planCompare.getPlanId(), planCompare.getPlanId());
							planCompare.setPlanId(bcrId);
						});
						 
						
						benefitTypeCurrentPlansAttributes.put(key, benefitPlanCompares);
					}
				} catch (ExecutionException | InterruptedException ex) {
					log.error("Error while getting plan details form BenefitsPlanViewService ...");
					Thread.currentThread().interrupt();
				}
			});
			if(noCurrentPlan) {
				benefitTypeCurrentPlansAttributes.put(AttributeBuilderHelper.NO_TYPE, null);
			}
			// setting for current plans rate and heads count usage
			currentTrinetPlans.setCurrentPlanIds(allPlanIds);
			currentTrinetPlans.setBenefitTypeCurrentPlansAttributes(benefitTypeCurrentPlansAttributes);
			currentTrinetPlans.setTrinetPlanCoverageEmployeeMapping(trinetPlanCoverageEmployeeMapping);
		};
	}

	@Override
	public Populator populateCurrentPlanRate() {
		return currentTrinetPlans -> {
			Map<String, RateDetail> benefitsPlansRateDetails = prospectBenefitsPlansRatesService
					.getBenefitsPlansRateDetails(currentTrinetPlans.getCurrentPlanIds(),
							currentTrinetPlans.getCompany().getCode());
			currentTrinetPlans.setCurrentPlanRates(benefitsPlansRateDetails);
		};
	}
	
	@Override
	public Populator populateCurrentPlanHeadCount() {
		return currentTrinetPlans -> {
			List<EmployeePlansRes> prospectEmployess = currentTrinetPlans.getProspectEmployees().get();
			// Map Key PlanId, Key coverageCode Value EmployeeId
			Map<String, Map<String, List<String>>> planCoverageEmployeeMapping = prospectEmployess.stream()
					.filter(employee -> !CollectionUtils.isEmpty(employee.getBenefitPlans()))
					.flatMap(employee -> employee.getBenefitPlans().stream()
							.map(benefit -> Map.entry(benefit.getBenefitPlanId(),
									Map.entry(benefit.getCoverageCode(), employee.getEmployeeId()))))
					.collect(Collectors.groupingBy(Map.Entry::getKey,
							Collectors.groupingBy(entry -> entry.getValue().getKey(),
									Collectors.mapping(entry -> entry.getValue().getValue(), Collectors.toList()))));
			
			List<EmployeeHeadCountRes> currentPlanCvrgHeadCount = prospectEmployess.stream()
					.filter(employee -> !CollectionUtils.isEmpty(employee.getBenefitPlans()))
					.flatMap(employee -> employee.getBenefitPlans().stream()).map(currentHeadCount())
					.collect(Collectors.toList());
			currentPlanCvrgHeadCount.stream().forEach(resp -> resp.setCount(Long
					.valueOf(planCoverageEmployeeMapping.get(resp.getBenefitPlan()).get(resp.getCovrgCD()).size())));
			currentTrinetPlans.setCurrentPlanHeadCounts(currentPlanCvrgHeadCount);
			currentTrinetPlans.setCurrentPlanCoverageEmployeeMapping(planCoverageEmployeeMapping);
		};
	}

	@Override
	public Populator populateTrinetPlan() {
		return currentTrinetPlans -> {
			Map<String, List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes = new HashMap<>();
			Map<String, Set<XbssRealmPlyrPlan>> benefitTypeTrinetPlans = new ConcurrentHashMap<>();
			Map<String, String> basePlanDescriptions = new ConcurrentHashMap<>();
			// Getting the Trinet plans
			List<EePlanAssignment> emplPlanAssignment = emplPlanAssignmentService
					.getEmployeePlanAssigmentBy(List.of(Long.valueOf(currentTrinetPlans.getStrategyId())));

			updateToBasePlanAssignments(currentTrinetPlans, emplPlanAssignment, basePlanDescriptions);

			emplPlanAssignment.parallelStream().forEach(planAssignment -> {
				XbssRealmPlyrPlan realmPlyrPlan = currentTrinetPlans.getPlyrPlanMap()
						.get(planAssignment.getBenefitPlan());
				if (Objects.nonNull(realmPlyrPlan)) {
					benefitTypeTrinetPlans
							.computeIfAbsent(realmPlyrPlan.getPlanType(), k -> ConcurrentHashMap.newKeySet())
							.add(realmPlyrPlan);

				} else if (CompanyServiceHelper.isTibProspect(currentTrinetPlans.getCompany())) {
					XbssRealmPlyrPlan dummyRealmPlyrPlan = new XbssRealmPlyrPlan();
					dummyRealmPlyrPlan.setBenefitPlan(planAssignment.getBenefitPlan());
					dummyRealmPlyrPlan.setPlanType(planAssignment.getEePlanAssignmentPK().getBenefitType());

					benefitTypeTrinetPlans
							.computeIfAbsent(dummyRealmPlyrPlan.getPlanType(), k -> ConcurrentHashMap.newKeySet())
							.add(dummyRealmPlyrPlan);

				}

			});

			// Getting benefitType plan attributes form planView service and setting it to
			List<String> allPlanIds = new LinkedList<>();
			benefitTypeTrinetPlans.keySet().stream().forEach(key -> {
				Set<String> planIds = prepareFutureTypePlan(benefitTypeTrinetPlans).apply(key);
				allPlanIds.addAll(planIds);
				try {
					if (currentTrinetPlans.getRequestBenefitTypes().contains(key)) {
						List<BenefitPlanCompare> benefitPlanCompares = new ArrayList<>();
						if (!planIds.isEmpty()) {
							if (CompanyServiceHelper.isTibProspect(currentTrinetPlans.getCompany())) {
								benefitPlanCompares = hrisPlanAttributeService
										.getPlanAttributesByBenefitType(planIds,PlanTypesEnum.getName(key)).get();
							} else {
								benefitPlanCompares = planCompareService.getPlanAttributes(planIds,
										currentTrinetPlans.getCompany().getRealmPlanYear().getPlanYearEnd(),
										BSSApplicationConstants.databaseTemplatesMap.get(key),
										currentTrinetPlans.getHttpRequest()).get();
								// Set description from base plan mapping if available
								if (!CollectionUtils.isEmpty(basePlanDescriptions)) {
									benefitPlanCompares.forEach(planCompare -> {
										String description = basePlanDescriptions.get(planCompare.getPlanId());
										if (Objects.nonNull(description)) {
											planCompare.setPlanName(description);
										}
									});
								}
							}
						}

						benefitTypeCurrentPlansAttributes.put(key, benefitPlanCompares);
					}
				} catch (ExecutionException | InterruptedException ex) {
					log.error("Error while getting plan details form BenefitsPlanViewService ...");
					Thread.currentThread().interrupt();
				}
			});

			currentTrinetPlans.setRealmPlyrPlans(benefitTypeTrinetPlans);
			currentTrinetPlans.setTrinetPlanIds(allPlanIds);
			currentTrinetPlans.setBenefitTypeTrinetPlansAttributes(benefitTypeCurrentPlansAttributes);
		};
	}
	
	@Override
	public Populator populateTrinetPlanRate() {
		return currentTrinetPlans -> {
			Map<String, List<BenefitPlanRate>> planRates = currentTrinetPlans.getTrinetPlanRatesByCompany();
			Map<String, RateDetail> trinetPlanRates = new HashMap<>();
			currentTrinetPlans.getTrinetPlanIds().forEach(planId -> {
                Map<String, BigDecimal> planCostMap = StrategyUtils.getPlanCost(planRates.get(planId));
                List<TierRate> tierRates = new ArrayList<>();
                planCostMap.entrySet().forEach(planCost -> tierRates
                        .add(TierRate.builder().cvgTierCode(planCost.getKey()).cost(planCost.getValue()).build()));
                trinetPlanRates.put(planId, RateDetail.builder().tierRates(tierRates).rateType(RateTypeEnum.FOUR_TIER.getType()).build());
            });
			currentTrinetPlans.setTrinetPlanRates(trinetPlanRates);
		};
	}
	
	/* Utility methods for above method */
	
	private Map<String,Map<String,List<String>>> currentTrinetPlanMapping(List<EmployeePlansRes> prospectEmployees, 
			Map<String, List<EePlanAssignment>> trinetBenTypeToEEPlanAssignments){
		//current : planType, planId -> list of employees
		Map<String, Map<String, List<String>>> prospectBenTypeToPlanToEmplMapping = new HashMap<>();
		for (EmployeePlansRes prospectEmployeePlan : prospectEmployees) {
			List<BenefitPlan> prospectBenPlans = prospectEmployeePlan.getBenefitPlans();
			Set<String> planTypes = (CollectionUtils.isEmpty(prospectBenPlans)) ? Set.of()
					: prospectBenPlans.stream().map(BenefitPlan::getBenefitTypeCode).collect(Collectors.toSet());
			trinetBenTypeToEEPlanAssignments.keySet().stream().forEach(bType -> {
				String benType = prepareDentalBenType().andThen(prepareVisionBenType()).apply(bType);
				prospectBenTypeToPlanToEmplMapping.putIfAbsent(benType, new HashMap<>());
				if (CollectionUtils.isEmpty(prospectBenPlans)) {
					prospectBenTypeToPlanToEmplMapping.get(benType).putIfAbsent(AttributeBuilderHelper.NO_PLAN_ID,
							new ArrayList<>());
					prospectBenTypeToPlanToEmplMapping.get(benType).get(AttributeBuilderHelper.NO_PLAN_ID)
							.add(prospectEmployeePlan.getEmployeeId());
				}
				for (BenefitPlan prospectBenPlan : prospectBenPlans) {
					if (planTypes.contains(benType)) {
						if (prospectBenPlan.getBenefitTypeCode().equalsIgnoreCase(benType)) {
							prospectBenTypeToPlanToEmplMapping.get(benType)
									.putIfAbsent(prospectBenPlan.getBenefitPlanId(), new ArrayList<>());
							prospectBenTypeToPlanToEmplMapping.get(benType).get(prospectBenPlan.getBenefitPlanId())
									.add(prospectEmployeePlan.getEmployeeId());
						}
					} else {
						prospectBenTypeToPlanToEmplMapping.get(benType).putIfAbsent(AttributeBuilderHelper.NO_PLAN_ID,
								new ArrayList<>());
						prospectBenTypeToPlanToEmplMapping.get(benType).get(AttributeBuilderHelper.NO_PLAN_ID)
								.add(prospectEmployeePlan.getEmployeeId());
					}
				}
			});
		}
		return mapTrinetPlanIds(prospectBenTypeToPlanToEmplMapping, trinetBenTypeToEEPlanAssignments);
	}
	
	private Map<String, Map<String, List<String>>> mapTrinetPlanIds(Map<String, Map<String, List<String>>> prospectBenTypeToPlanToEmplMapping, 
			Map<String, List<EePlanAssignment>> trinetBenTypeToEEPlanAssignments){
		Map<String, Map<String, List<String>>> currentToTrintPlanMapping = new HashMap<>();
		trinetBenTypeToEEPlanAssignments.entrySet().stream().forEach(entry -> {
			String benType = prepareDentalBenType().andThen(prepareVisionBenType()).apply(entry.getKey());
			if (prospectBenTypeToPlanToEmplMapping.get(benType) != null) {
				if (prospectBenTypeToPlanToEmplMapping.containsKey(benType)) {
					Set<Entry<String, List<String>>> entrySet = prospectBenTypeToPlanToEmplMapping.get(benType)
							.entrySet();
					for (Map.Entry<String, List<String>> prospectPlanToEmplMapping : entrySet) {
						mapPlans(currentToTrintPlanMapping, entry, prospectPlanToEmplMapping);
					}
				}
			}
		});
		
		return currentToTrintPlanMapping;
	}

	private void mapPlans(Map<String, Map<String, List<String>>> currentToTrinetPlanMapping,
			Entry<String, List<EePlanAssignment>> entry, Entry<String, List<String>> prospectPlanToEmplMapping) {
		for (EePlanAssignment emp : entry.getValue()) {
			String benType = prepareDentalBenType()
					.andThen(prepareVisionBenType())
					.apply(entry.getKey());
			if (prospectPlanToEmplMapping.getValue().contains(emp.getEePlanAssignmentPK().getEmplId())) {
				if (currentToTrinetPlanMapping.containsKey(benType)) {
					if (currentToTrinetPlanMapping.get(benType).containsKey(prospectPlanToEmplMapping.getKey())) {
						if (!currentToTrinetPlanMapping.get(benType).get(prospectPlanToEmplMapping.getKey())
								.contains(emp.getBenefitPlan())) {
							currentToTrinetPlanMapping.get(benType).get(prospectPlanToEmplMapping.getKey())
									.add(emp.getBenefitPlan());
						}
					} else {
						List<String> trinetPlans = new LinkedList<>();
						trinetPlans.add(emp.getBenefitPlan());
						currentToTrinetPlanMapping.get(benType).put(prospectPlanToEmplMapping.getKey(), trinetPlans);
					}
				} else {
					currentToTrinetPlanMapping.put(benType, preparePlanEmployeeMap().apply(emp.getBenefitPlan(), prospectPlanToEmplMapping.getKey()));
				}
			}
		}
	}

	private BiFunction<String, String, Map<String, List<String>>> preparePlanEmployeeMap(){
		return (benefitPlan, benType) -> {
			List<String> trinetPlans = new LinkedList<>();
			Map<String, List<String>> planEmplMap = new HashMap<>();
			trinetPlans.add(benefitPlan);
			planEmplMap.put(benType, trinetPlans);
			return planEmplMap;
		};
	}
	
	private Function<BenefitPlan, EmployeeHeadCountRes> currentHeadCount() {
		return benefitPlan -> {
			EmployeeHeadCountRes countResp = new EmployeeHeadCountRes();
			countResp.setBenefitPlan(benefitPlan.getBenefitPlanId());
			countResp.setCount(null);
			countResp.setCovrgCD(benefitPlan.getCoverageCode());
			return countResp;
		};
		
	}
	
	private Function<String, Set<String>> prepareCurrentTypePlan(Map<String, Set<BenefitPlan>> plans) {
		return key -> plans.get(key).stream().map(BenefitPlan::getBplPlanId).collect(Collectors.toSet());
	}
	
	private Function<String,Set<String>> prepareFutureTypePlan(Map<String,Set<XbssRealmPlyrPlan>> plans) {
		return key -> plans.get(key).stream().map(XbssRealmPlyrPlan::getBenefitPlan).collect(Collectors.toSet());
	}

	private Map<String, Set<BenefitPlan>> getPlanIds(List<EmployeePlansRes> employess) {
		return employess.stream()
		.filter(employeePlan -> Objects.nonNull(employeePlan.getBenefitPlans()))		
		.flatMap(employeePlan -> employeePlan.getBenefitPlans().stream())
				.collect(Collectors.groupingBy(EmployeePlansRes.BenefitPlan::getBenefitTypeCode, Collectors.toSet()));
	}

	private List<EePlanAssignment> updateToBasePlanAssignments(CurrentTrinetPlans currentTrinetPlans,
										  List<EePlanAssignment> eePlanAssignments,
										  Map<String, String> basePlanDescriptions) {

		if (CollectionUtils.isEmpty(eePlanAssignments)
				|| Objects.isNull(currentTrinetPlans)
				|| Objects.isNull(currentTrinetPlans.getCompany())
				|| Objects.isNull(currentTrinetPlans.getCompany().getRealmPlanYear())
				|| !BSSApplicationConstants.NATIONAL.equals(currentTrinetPlans.getCompany().getRateType())) {
			return eePlanAssignments;
		}
		Map<String, com.trinet.ambis.service.model.plancompare.BenefitPlan> regionalToBasePlanMap = benefitPlanService
				.getRegionalBasePlanMapping(currentTrinetPlans.getCompany().getRealmPlanYear());
		if (CollectionUtils.isEmpty(regionalToBasePlanMap)) {
			return eePlanAssignments;
		}
		eePlanAssignments.forEach(assignment -> {
			com.trinet.ambis.service.model.plancompare.BenefitPlan basePlan = regionalToBasePlanMap
					.get(assignment.getBenefitPlan());
			if (Objects.nonNull(basePlan)) {
				basePlanDescriptions.put(assignment.getBenefitPlan(), basePlan.getDescr());
			}
		});
		return eePlanAssignments;
	}
	
}