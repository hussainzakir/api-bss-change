package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.model.Employee;
import com.trinet.ambis.service.model.*;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.outputs.EmployeeCostSummaryService;
import com.trinet.ambis.util.BssCoreServiceClient;
import org.apache.commons.collections.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.service.ModelCompareEmployeeDataService;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;

@Service
public class ModelCompareEmployeeDataServiceImpl extends EmployeeDataServiceImpl implements ModelCompareEmployeeDataService {

    private static final Logger logger = LoggerFactory.getLogger(ModelCompareEmployeeDataServiceImpl.class);

    @Autowired
    StrategyDataDao strategyDataDao;

    @Autowired
    ProspectEmployeeCostService prospectEmployeeCostService;

    @Autowired
    EmployeeDataDao employeeDataDao;

    @Autowired
    EmployeeCostSummaryService employeeCostSummaryService;

    @Autowired
    StrategyFundingDataDao strategyFundingDataDao;

    @Autowired
    BssCoreServiceClient bssCoreServiceClient; // Service to get demographics from bss-core

    @Autowired
    EmployeeBenefitGroupDao employeeBenefitGroupDao;

    @Override
	public List<EmployeeStrategyData> getEmployeeStrategiesPlanCostData(Company company, Long currentStrategyId,
			List<Long> strategiesToCompare) {
		List<EmployeeStrategyData> finalData = new ArrayList<>();
		Set<Long> allStrategyIds = new LinkedHashSet<>();
		allStrategyIds.add(currentStrategyId);
		allStrategyIds.addAll(strategiesToCompare);

		if (!company.isProspectCompany()) {
			strategiesToCompare.add(currentStrategyId);
		}
        CompletableFuture<Optional<List<StrategyGroupEmployeePlanRateData>>> trinetResponse = CompletableFuture
                .completedFuture(strategyDataDao.getStrategyGroupPlanCostByPlanType(company, strategiesToCompare, BSSApplicationConstants.PRIMARY_PLAN_TYPES));
        if (company.isProspectCompany()) {
            CompletableFuture<Optional<List<EmployeeCostRes>>> prospectResponse = CompletableFuture
                    .completedFuture(prospectEmployeeCostService.getProspectEmployeeCostByType(company.getCode(), BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER));

            CompletableFuture<Optional<List<StrategyGroupEmployeePlanRateData>>> combinedTriNetData;
            if (CompanyServiceHelper.isTibProspect(company)) {
                CompletableFuture<Optional<List<StrategyGroupEmployeePlanRateData>>> trinetOmsResponse = CompletableFuture
                        .completedFuture(employeeCostSummaryService
                                .getOmsStrategyCostResponse(strategiesToCompare, BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER, true));
                combinedTriNetData = trinetResponse
                        .thenCombine(trinetOmsResponse, combineTrinetEmployeeStrategiesPlanCostData());
            } else {
                combinedTriNetData = trinetResponse;
            }

            CompletableFuture<List<EmployeeStrategyData>> combinedData = prospectResponse
                    .thenCombine(combinedTriNetData, buildEmployeeStrategiesPlanCostData(strategiesToCompare));
            finalData = combinedData.join();
        } else {
            Optional<List<StrategyGroupEmployeePlanRateData>> trinetData = trinetResponse.join();
            if (trinetData.isPresent()) {
                Map<String, EmployeeStrategyData> employeeStrategyDataMap = new HashMap<>();
                for (StrategyGroupEmployeePlanRateData planData : trinetData.get()) {
                    String emplId = planData.getEmplId();
                    EmployeeStrategyData employeeStrategyData = employeeStrategyDataMap.computeIfAbsent(emplId, id -> {
                        EmployeeStrategyData esd = new EmployeeStrategyData();
                        esd.setEmplId(id);
                        esd.setStrategyDetails(new ArrayList<>());
                        return esd;
                    });
                    EmployeeStrategyPlanData strategyPlanData = employeeStrategyData.getStrategyDetails().stream()
                            .filter(s -> Objects.equals(s.getStrategyId(), planData.getStrategyId()))
                            .findFirst()
                            .orElseGet(() -> {
                                EmployeeStrategyPlanData spd = new EmployeeStrategyPlanData();
                                spd.setStrategyId(planData.getStrategyId());
                                spd.setGroupId(planData.getGroupId());
                                spd.setGroupName(planData.getGroupName());
                                spd.setBenefitPlans(new ArrayList<>());
                                employeeStrategyData.getStrategyDetails().add(spd);
                                return spd;
                            });
                    strategyPlanData.getBenefitPlans().add(createTriNetBenefitPlanRateData(planData));
                }
                finalData = employeeStrategyDataMap.values().stream().collect(Collectors.toList());
            }

            // Fetch demographics from bss-core
            List<ProspectCensusResponse> emplDemographicsData = bssCoreServiceClient.getCensusByCompanyCode(company.getCode());

            // Create map of employeeId to ProspectCensusResponse
            Map<String, ProspectCensusResponse> emplIdToCensusMap = emplDemographicsData.stream()
                    .collect(Collectors.toMap(ProspectCensusResponse::getEmployeeId, e -> e));

            // Add missing employees from demographics data to finalData
            addMissingEmployeesFromDemographics(finalData, emplDemographicsData, strategiesToCompare);
            
            // Set first name and last name from demographics for existing employees
			setEmployeeNamesFromDemographics(finalData, emplIdToCensusMap);

            Map<Long, ModelCompareStrategy> strategyGroupFundingDetailsMap = new HashMap<>();

            strategyGroupFundingDetailsMap.putAll(strategyFundingDataDao.getFundingDetailsByStrategyId(strategiesToCompare,
                    company, true, company.getRealmPlanYear().getPlanYearEnd()));

            MultiKeyMap benefitSupplementData = getBenefitSupplementData(strategyGroupFundingDetailsMap);
            // Update employee/client costs based on the client's benefit supplement setup
            updateBsuppEmplRates(finalData, benefitSupplementData);
        }

        expandEmployeeStrategyData(finalData);
		finalData.forEach(employeeStrategyData -> {
                employeeStrategyData.getStrategyDetails().forEach(employeeStrategyPlanData ->
                        employeeStrategyPlanData.setBenefitPlans(orderBenefitPlanRateData(employeeStrategyPlanData.getBenefitPlans()))
                );
			sortEmployeeStrategyData(allStrategyIds, employeeStrategyData);
		});

        Collections.sort(finalData);
        
        return finalData;
    }

	private void setEmployeeNamesFromDemographics(List<EmployeeStrategyData> finalData,
			Map<String, ProspectCensusResponse> emplIdToCensusMap) {
		for (EmployeeStrategyData esd : finalData) {
			ProspectCensusResponse census = emplIdToCensusMap.get(esd.getEmplId());
			if (census != null) {
				esd.setEmplFirstName(census.getFirstName());
				esd.setEmplLastName(census.getLastName());
			}
		}
	}

	private void sortEmployeeStrategyData(Set<Long> allStrategyIds, EmployeeStrategyData employeeStrategyData) {
		List<EmployeeStrategyPlanData> orderedEmployeStrategyData = new ArrayList<>();
		allStrategyIds.forEach(strategyId -> employeeStrategyData.getStrategyDetails().stream()
				.filter(s -> Objects.equals(s.getStrategyId(), strategyId)).findFirst()
				.ifPresent(orderedEmployeStrategyData::add));
		employeeStrategyData.setStrategyDetails(orderedEmployeStrategyData);
	}
    
    private static BiFunction<Optional<List<StrategyGroupEmployeePlanRateData>>, Optional<List<StrategyGroupEmployeePlanRateData>>,
            Optional<List<StrategyGroupEmployeePlanRateData>>> combineTrinetEmployeeStrategiesPlanCostData() {
                return(trinetResponseOptional, trinetOmsResponseOptional) -> {
                    if (trinetOmsResponseOptional.isPresent()) {
                        if (trinetResponseOptional.isPresent()) {
                            trinetResponseOptional.get().addAll(trinetOmsResponseOptional.get());
                        }
                        else {
                            trinetResponseOptional = trinetOmsResponseOptional;
                        }
                    }
                    return trinetResponseOptional;
                };
    }

    private BiFunction<Optional<List<EmployeeCostRes>>, Optional<List<StrategyGroupEmployeePlanRateData>>,
            List<EmployeeStrategyData>> buildEmployeeStrategiesPlanCostData(List<Long> strategyList) {
        return (prospectResponseOptional, trinetResponseOptional) -> {
            Map<String, EmployeeStrategyData> finalMap = convertProspectToEmployeeStrategyData(prospectResponseOptional);
            return processEmployeeStrategyData(finalMap, trinetResponseOptional, strategyList);
        };
    }

    private static Map<String, EmployeeStrategyData> convertProspectToEmployeeStrategyData(Optional<List<EmployeeCostRes>> prospectData) {
        Map<String, EmployeeStrategyData> workingMap = new HashMap<>();
        if (prospectData.isPresent()) {
            List<EmployeeCostRes> employeeCostResList = prospectData.get();
            for (EmployeeCostRes employeeCostRes : employeeCostResList) {
                String benefitTypeCode = employeeCostRes.getBenefitTypeCode();
                for (EmployeeCostRes.EmployeePlanContribution employeePlanContribution : employeeCostRes.getEmployeePlanContribution()) {
                    EmployeeStrategyData employeeStrategyData = workingMap.get(employeePlanContribution.getEmployeeId());
                    if (employeeStrategyData == null) {
                        employeeStrategyData = createProspectEmployeeStrategyData(employeePlanContribution);
                        workingMap.put(employeePlanContribution.getEmployeeId(), employeeStrategyData);
                    }
                    employeeStrategyData.getStrategyDetails().get(0).getBenefitPlans()
                            .add(createProspectBenefitPlanRateData(employeePlanContribution, benefitTypeCode));
                }
            }
        }

        return workingMap;
    }

    private List<EmployeeStrategyData> processEmployeeStrategyData(Map<String, EmployeeStrategyData> finalMap,
                            Optional<List<StrategyGroupEmployeePlanRateData>> trinetData, List<Long> strategyList) {

        if (trinetData.isPresent()) {

            // Any employees that only exist on the TriNet side should be added to the prospect map
            addTriNetOnlyEmployeeRecords(finalMap, trinetData);

            Map<Long, Set<Employee>> employeeStrategyGroupsMap = new HashMap<>();
            for (Long strategyId : strategyList) {
                Set<Employee> employeeStrategyGroups = employeeDataDao.getEmployeeGroupDetailsByStrategy(strategyId);
                employeeStrategyGroupsMap.put(strategyId, employeeStrategyGroups);
            }

            for (EmployeeStrategyData employeeStrategyData : finalMap.values()) {
                List<Long> expectedStrategyIds = new ArrayList<>(strategyList);
                List<Long> strategyListForEmployee = new ArrayList<>();
                List<StrategyGroupEmployeePlanRateData> strategyGroupEmployeePlanRateDataList = trinetData.get().stream()
                        .filter(entry -> entry.getEmplId().equals(employeeStrategyData.getEmplId())).collect(Collectors.toList());

                for (StrategyGroupEmployeePlanRateData strategyGroupEmployeePlanRateData : strategyGroupEmployeePlanRateDataList) {
                    Optional<EmployeeStrategyPlanData> strategyDetailsDataOptional = employeeStrategyData.getStrategyDetails().stream()
                            .filter(entry -> entry.getStrategyId().equals(strategyGroupEmployeePlanRateData.getStrategyId()))
                            .findFirst();
                    strategyListForEmployee.add(strategyGroupEmployeePlanRateData.getStrategyId());
                    EmployeeStrategyPlanData employeeStrategyPlanData;
                    if (strategyDetailsDataOptional.isEmpty()) {
                        employeeStrategyPlanData = createEmployeeStrategyPlanData(strategyGroupEmployeePlanRateData.getStrategyId(),
                                strategyGroupEmployeePlanRateData.getGroupId(), strategyGroupEmployeePlanRateData.getGroupName());
                        employeeStrategyData.getStrategyDetails().add(employeeStrategyPlanData);
                    } else {
                        employeeStrategyPlanData = strategyDetailsDataOptional.get();
                    }
                    employeeStrategyPlanData.getBenefitPlans().add(createTriNetBenefitPlanRateData(strategyGroupEmployeePlanRateData));
                }

                // Get the missing strategy ids for the employee and add records for them
                expectedStrategyIds.removeAll(strategyListForEmployee);
                addMissingStrategyIds(expectedStrategyIds, employeeStrategyData, employeeStrategyGroupsMap);

            }
        }

        return new ArrayList<>(finalMap.values());
    }

    private static EmployeeStrategyData createProspectEmployeeStrategyData(EmployeeCostRes.EmployeePlanContribution employeePlanContribution) {
        EmployeeStrategyData employeeStrategyData = new EmployeeStrategyData();
        employeeStrategyData.setEmplId(employeePlanContribution.getEmployeeId());
        employeeStrategyData.setEmplFirstName(employeePlanContribution.getFirstName());
        employeeStrategyData.setEmplLastName(employeePlanContribution.getLastName());
        employeeStrategyData.setEmplMiddleName("");
        employeeStrategyData.setCurrentGroupId(BigDecimal.valueOf(employeePlanContribution.getGroupId()));
        employeeStrategyData.setCurrentGroupName(employeePlanContribution.getGroupName());
        List<EmployeeStrategyPlanData> strategyDetails = new ArrayList<>();
        EmployeeStrategyPlanData employeeStrategyPlanData = createEmployeeStrategyPlanData(ProspectConstants.PROSPECT_STRATEGY_ID, (long) employeePlanContribution.getGroupId(), employeePlanContribution.getGroupName());
        strategyDetails.add(employeeStrategyPlanData);
        employeeStrategyData.setStrategyDetails(strategyDetails);
        return employeeStrategyData;
    }

    private static EmployeeStrategyData createTrinetEmployeeStrategyData(String emplId) {
        EmployeeStrategyData employeeStrategyData = new EmployeeStrategyData();
        employeeStrategyData.setEmplId(emplId);
        employeeStrategyData.setEmplFirstName("");
        employeeStrategyData.setEmplLastName("");
        employeeStrategyData.setEmplMiddleName("");
        employeeStrategyData.setCurrentGroupId(null);
        employeeStrategyData.setCurrentGroupName(null);
        List<EmployeeStrategyPlanData> strategyDetails = new ArrayList<>();
        // Create prospect strategy plan data
        EmployeeStrategyPlanData employeeStrategyPlanData = createEmployeeStrategyPlanData(ProspectConstants.PROSPECT_STRATEGY_ID, 0L, null);
        strategyDetails.add(employeeStrategyPlanData);
        // Create TriNet strategy plan data
        strategyDetails.add(employeeStrategyPlanData);

        employeeStrategyData.setStrategyDetails(strategyDetails);
        return employeeStrategyData;
    }

    private static BenefitPlanRateData createProspectBenefitPlanRateData(EmployeeCostRes.EmployeePlanContribution employeePlanContribution,
                                                                         String benefitTypeCode) {

        BenefitPlanRateData benefitPlanRateData = new BenefitPlanRateData();
        benefitPlanRateData.setPlanId(String.valueOf(employeePlanContribution.getPlanContribution().getBenefitPlanId()));
        benefitPlanRateData.setPlanType(benefitTypeCode);
        benefitPlanRateData.setPlanName(employeePlanContribution.getPlanContribution().getBenefitPlanName());
        benefitPlanRateData.setCoverageLevel(employeePlanContribution.getCovgLevel());
        benefitPlanRateData.setCoverageElect(employeePlanContribution.getCovgLevel().equals("W") ? "W" : "E");
        benefitPlanRateData.setEmployeeContribution(employeePlanContribution.getPlanContribution().getEeCost());
        benefitPlanRateData.setEmployerContribution(employeePlanContribution.getPlanContribution().getErCost());
        benefitPlanRateData.setOffered(true);
        
        return benefitPlanRateData;

    }

    private static BenefitPlanRateData createTriNetBenefitPlanRateData(StrategyGroupEmployeePlanRateData strategyGroupEmployeePlanRateData) {

        BenefitPlanRateData benefitPlanRateData = new BenefitPlanRateData();
        String cvgCode = strategyGroupEmployeePlanRateData.getCoverageCode() == null || strategyGroupEmployeePlanRateData.getCoverageCode().equals("W") ? "W" : "E";
        benefitPlanRateData.setPlanId(String.valueOf(strategyGroupEmployeePlanRateData.getBenefitPlan()));
        benefitPlanRateData.setPlanType(strategyGroupEmployeePlanRateData.getPlanType());
        benefitPlanRateData.setPlanName(strategyGroupEmployeePlanRateData.getPlanName());
        benefitPlanRateData.setCoverageLevel(strategyGroupEmployeePlanRateData.getCoverageCode());
        benefitPlanRateData.setCoverageElect(cvgCode);
        benefitPlanRateData.setEmployeeContribution(strategyGroupEmployeePlanRateData.getEeRate());
        benefitPlanRateData.setEmployerContribution(strategyGroupEmployeePlanRateData.getErRate());
        benefitPlanRateData.setOffered(true);
        
        return benefitPlanRateData;

    }

    private static EmployeeStrategyPlanData createEmployeeStrategyPlanData(Long strategyId, Long groupId, String groupName) {
        EmployeeStrategyPlanData employeeStrategyPlanData = new EmployeeStrategyPlanData();
        employeeStrategyPlanData.setStrategyId(strategyId);
        employeeStrategyPlanData.setGroupId(groupId);
        employeeStrategyPlanData.setGroupName(groupName);

        return employeeStrategyPlanData;
    }

    private static List<BenefitPlanRateData> orderBenefitPlanRateData(List<BenefitPlanRateData> benefitPlanRateDataList) {
        List<BenefitPlanRateData> orderedBenefitPlanRateData = new ArrayList<>();

        Map<String, BenefitPlanRateData> dataByBenType = benefitPlanRateDataList.stream().collect(Collectors.toMap(
                BenefitPlanRateData::getPlanType,
                benefitPlanRateData -> benefitPlanRateData
        ));

        BSSApplicationConstants.PRIMARY_PLAN_TYPES.forEach(planType -> {
            BenefitPlanRateData planRateData = dataByBenType.get(planType);
            if (planRateData != null) {
                orderedBenefitPlanRateData.add(planRateData);
            }
        });
        return orderedBenefitPlanRateData;
    }

    /**
     *
     * @param employeeStrategyDataList
     *
     * This method is used to populate blank M/D/V plans for employees who do not have any plans
     * and M/D/V plans that are missing from any employee
     */
    private static void expandEmployeeStrategyData(List<EmployeeStrategyData> employeeStrategyDataList) {
        for (EmployeeStrategyData employeeStrategyData : employeeStrategyDataList) {
            for (EmployeeStrategyPlanData employeeStrategyPlanData : employeeStrategyData.getStrategyDetails()) {
                if (employeeStrategyPlanData.getBenefitPlans().isEmpty()) {
                    for (String planType : BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER) {
                        BenefitPlanRateData benefitPlanRateData = createBenefitPlanRateData(planType);
                        employeeStrategyPlanData.getBenefitPlans().add(benefitPlanRateData);
                    }
                }
                else {
                        addEmptyBenefitPlanRateDataForPlanType(employeeStrategyPlanData.getBenefitPlans(), BSSApplicationConstants.MEDICAL_PLAN_TYPES, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
                        addEmptyBenefitPlanRateDataForPlanType(employeeStrategyPlanData.getBenefitPlans(), BSSApplicationConstants.DENTAL_PLAN_TYPES, BSSApplicationConstants.DENTAL_PLAN_TYPE);
                        addEmptyBenefitPlanRateDataForPlanType(employeeStrategyPlanData.getBenefitPlans(), BSSApplicationConstants.VISION_PLAN_TYPES, BSSApplicationConstants.VISION_PLAN_TYPE);
                }
            }
        }
    }

    private static void addEmptyBenefitPlanRateDataForPlanType(List<BenefitPlanRateData> benefitPlanRateDataList,
                                                               List<String> planTypes, String planType) {
        if (benefitPlanRateDataList.stream().filter(benefitPlanRateData -> planTypes.contains(benefitPlanRateData.getPlanType()))
                .collect(Collectors.toList()).isEmpty()) {
            BenefitPlanRateData benefitPlanRateData = createBenefitPlanRateData(planType);
            benefitPlanRateDataList.add(benefitPlanRateData);
        }
    }

    private static BenefitPlanRateData createBenefitPlanRateData(String planType) {
        BenefitPlanRateData benefitPlanRateData = new BenefitPlanRateData();
        benefitPlanRateData.setPlanType(planType);
        benefitPlanRateData.setOffered(true);
        benefitPlanRateData.setCoverageElect("W");
        return benefitPlanRateData;
    }

    private static void addTriNetOnlyEmployeeRecords(Map<String, EmployeeStrategyData> finalMap,
                                                     Optional<List<StrategyGroupEmployeePlanRateData>> trinetData) {
        if (trinetData.isEmpty()) {
            return;
        }
        List<String> triNetOnlyEmployeeIds = trinetData.get().stream().map(StrategyGroupEmployeePlanRateData::getEmplId).collect(Collectors.toList());
        triNetOnlyEmployeeIds.removeAll(new ArrayList<>(finalMap.keySet()));

        for (String emplId : triNetOnlyEmployeeIds) {
            EmployeeStrategyData employeeStrategyData = createTrinetEmployeeStrategyData(emplId);
            finalMap.put(emplId, employeeStrategyData);
            logger.error("Employee : {} is not found in prospect data.", emplId);
        }

    }

    private static void addMissingStrategyIds(List<Long> expectedStrategyIds, EmployeeStrategyData employeeStrategyData,
                                              Map<Long, Set<Employee>> employeeStrategyGroupsMap) {
        for (Long strategyId : expectedStrategyIds) {
            Set<Employee> employeeStrategyGroups = employeeStrategyGroupsMap.get(strategyId);
            Employee employee = employeeStrategyGroups.stream()
                    .filter(entry -> entry.getEmplId().equals(employeeStrategyData.getEmplId()))
                    .findFirst().orElse(null);
            EmployeeStrategyPlanData employeeStrategyPlanData = createEmployeeStrategyPlanData(strategyId, employee == null ? null : employee.getBenefitGroupId(),
                    employee == null ? null : employee.getBenefitGroupName());
            employeeStrategyData.getStrategyDetails().add(employeeStrategyPlanData);
        }
    }

    /**
     * Add missing employees from demographics data to finalData. If WSE has waived all the benefit types
     * then there won't be any plan assigned to WSE and the entry will be missing in finalData.
     *
     * @param finalData              The final employee strategy data list
     * @param emplDemographicsData   The list of employee demographics data from bss-core
     * @param strategiesToCompare    The list of strategies to compare
     */
    void addMissingEmployeesFromDemographics(List<EmployeeStrategyData> finalData,
                                                           List<ProspectCensusResponse> emplDemographicsData,
                                                           List<Long> strategiesToCompare) {

        Set<String> existingEmployeeIds = finalData.stream()
                .map(EmployeeStrategyData::getEmplId)
                .collect(Collectors.toSet());

        for (ProspectCensusResponse census : emplDemographicsData) {
            if (!existingEmployeeIds.contains(census.getEmployeeId())) {
                // Create new EmployeeStrategyData for missing employee
                EmployeeStrategyData newEmployeeData = new EmployeeStrategyData();
                newEmployeeData.setEmplId(census.getEmployeeId());
                newEmployeeData.setEmplFirstName(census.getFirstName());
                newEmployeeData.setEmplLastName(census.getLastName());
                newEmployeeData.setStrategyDetails(new ArrayList<>());

                // Add empty strategy details for all strategies
                for (Long strategyId : strategiesToCompare) {
                    // Get employee strategy group details for setting group info
                    Map<String, EmployeeStrategyGroupDetails> trinetEmployeesByEmplId = employeeBenefitGroupDao
                            .getEmployeeDetailsByStrategy(strategyId);
                    EmployeeStrategyGroupDetails employeeDetails = trinetEmployeesByEmplId.get(census.getEmployeeId());
                    if (employeeDetails != null) {
                        EmployeeStrategyPlanData strategyPlanData = new EmployeeStrategyPlanData();
                        strategyPlanData.setStrategyId(strategyId);
                        strategyPlanData.setGroupId(employeeDetails.getFutureGroupId());
                        strategyPlanData.setGroupName(employeeDetails.getFutureGroupName());
                        strategyPlanData.setBenefitProgram(employeeDetails.getFutureBenefitProgram());
                        strategyPlanData.setBenefitPlans(new ArrayList<>());
                        newEmployeeData.getStrategyDetails().add(strategyPlanData);
                    }
                }
                
                finalData.add(newEmployeeData);
            }
        }
    }
}

