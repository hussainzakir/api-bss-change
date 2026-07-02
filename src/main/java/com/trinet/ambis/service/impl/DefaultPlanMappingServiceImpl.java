package com.trinet.ambis.service.impl;

import com.trinet.ambis.client.DefaultPlanMappingServiceClient;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.DefaultPlanMappingService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.BssCoreServiceClient;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultPlanMappingServiceImpl implements DefaultPlanMappingService {

    private static final String HPP_RULE_TEMPLATE_NAME = "PROSPECT_DEFAULT";
    private static final String OMS_RULE_TEMPLATE_NAME = "PROSPECT_OMS";

    private final RealmPlyrPlanService realmPlyrPlanService;
    private final DefaultPlanMappingServiceClient planMappingServiceClient;
    private final EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;
    private final EmployeePlanAssignmentService employeePlanAssignmentService;
    private final ProspectCensusService prospectCensusService;
    private final BssCoreServiceClient bssCoreServiceClient;

    public DefaultPlanMappingServiceImpl(RealmPlyrPlanService realmPlyrPlanService,
            DefaultPlanMappingServiceClient planMappingServiceClient,
            EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService,
            EmployeePlanAssignmentService employeePlanAssignmentService,
            ProspectCensusService prospectCensusService,
            BssCoreServiceClient bssCoreServiceClient) {
        this.realmPlyrPlanService = realmPlyrPlanService;
        this.planMappingServiceClient = planMappingServiceClient;
        this.emplDefaultPlanAssignmentService = emplDefaultPlanAssignmentService;
        this.employeePlanAssignmentService = employeePlanAssignmentService;
        this.prospectCensusService = prospectCensusService;
        this.bssCoreServiceClient = bssCoreServiceClient;
    }

    @Override
    public void callPlanMappingService(Company company) {
        callPlanMappingService(company, Collections.emptySet());
    }

    @Override
    public void callPlanMappingService(Company company, Set<String> employeeIds) {

        List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse =
                planMappingServiceClient.getPlanMapping(company.getCode(), buildPlanMappingRequest(company, employeeIds));

        saveDefaultPlanMappings(company, planMappingResponse);
    }

    @Override
    public List<DefaultPlanMappingServiceClient.PlanMappingResponse> callPlanMappingServiceForOms(Company company, Set<String> employeeIds) {
        return planMappingServiceClient.getPlanMapping(company.getCode(), buildPlanMappingRequest(company, employeeIds));
    }

    @Override
    public void saveDefaultPlanMappings(Company company, List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse) {
        Map<String, XbssRealmPlyrPlan> plyrPlanMap =
                realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId());
        List<EmplDefaultPlanAssignmentDto> assignments = buildAssignmentsFromPlanMapping(
                company, planMappingResponse, plyrPlanMap);
        emplDefaultPlanAssignmentService.saveAll(assignments);
    }

    @Override
    public void createOmsEePlanAssignments(Company company, long strategyId, List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse) {
        List<ProspectCensusResponse> census = fetchCensus(company);

        Map<String, ProspectCensusResponse> censusByEmplId = census.stream()
                .filter(c -> c != null && c.getEmployeeId() != null)
                .collect(Collectors.toMap(ProspectCensusResponse::getEmployeeId, c -> c));

        List<EePlanAssignment> eePlanAssignments = buildOmsEeAssignments(strategyId, planMappingResponse, censusByEmplId);

        if (CollectionUtils.isNotEmpty(eePlanAssignments)) {
            employeePlanAssignmentService.saveEePlanAssignments(eePlanAssignments);
        } else {
            log.info(
                    "No OMS EE plan assignments to save for company={} strategyId={} - plan mapping response/census produced no assignable rows", company.getCode(), strategyId);
        }
    }

    private List<EePlanAssignment> buildOmsEeAssignments(long strategyId, List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse, Map<String, ProspectCensusResponse> censusByEmplId) {
        List<EePlanAssignment> assignments = new ArrayList<>();
        for (DefaultPlanMappingServiceClient.PlanMappingResponse mappingResponse : safePlanMappingResponses(planMappingResponse)) {
            List<DefaultPlanMappingServiceClient.EmployeeResponse> employeeResponses = mappingResponse.getEmployeeResponseList();
            if (CollectionUtils.isEmpty(employeeResponses)) {
                continue;
            }

            for (DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse : employeeResponses) {
                addOmsAssignmentsForEmployee(strategyId, employeeResponse, censusByEmplId, assignments);
            }
        }
        return assignments;
    }

    private void addOmsAssignmentsForEmployee(long strategyId,
            DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse,
            Map<String, ProspectCensusResponse> censusByEmplId,
            List<EePlanAssignment> assignments) {

        if (employeeResponse == null || employeeResponse.getEmployeeId() == null) {
            return;
        }

        String emplId = employeeResponse.getEmployeeId();
        ProspectCensusResponse censusResponse = censusByEmplId.get(emplId);
        Map<String, DefaultPlanMappingServiceClient.PlanResponse> mappedPlans = employeeResponse.getMappedPlans();
        for (Map.Entry<String, DefaultPlanMappingServiceClient.PlanResponse> entry : mappedPlans.entrySet()) {
            DefaultPlanMappingServiceClient.PlanResponse mappedPlan = entry.getValue();
            if (mappedPlan == null || mappedPlan.getPlanId() == null) {
                continue;
            }

            String benefitType = PlanTypesEnum.getCode(entry.getKey().toLowerCase(Locale.ROOT));
            String coverageCode = resolveCoverageCode(censusResponse, benefitType);
            if (coverageCode == null || ProspectConstants.WAVED_COVERAGE.equalsIgnoreCase(coverageCode)) {
                continue;
            }

            DefaultPlanMappingServiceClient.PlanCostResponse planCost = resolvePlanCostForCoverage(mappedPlan, coverageCode);
            BigDecimal eeRate = planCost != null ? planCost.getEeRate() : null;
            BigDecimal erRate = planCost != null ? planCost.getTotalCost() : null;

            EePlanAssignmentPK assignmentPk = EePlanAssignmentPK.builder()
                    .strategyId(strategyId)
                    .emplId(emplId)
                    .benefitType(benefitType)
                    .build();

            EePlanAssignment assignment = EePlanAssignment.builder()
                    .eePlanAssignmentPK(assignmentPk)
                    .benefitPlan(mappedPlan.getPlanId())
                    .portfolioId(0L)
                    .covrgCD(coverageCode)
                    .eeRate(eeRate)
                    .erRate(erRate)
                    .build();

            assignments.add(assignment);
        }
    }

    private DefaultPlanMappingServiceClient.PlanCostResponse resolvePlanCostForCoverage(DefaultPlanMappingServiceClient.PlanResponse mappedPlan, String coverageCode) {
        if (mappedPlan.getCvgLevelCost() == null || mappedPlan.getCvgLevelCost().isEmpty()) {
            return null;
        }
        DefaultPlanMappingServiceClient.PlanCostResponse directMatch = mappedPlan.getCvgLevelCost()
                .get(coverageCode);
        if (directMatch != null) {
            return directMatch;
        }
        return mappedPlan.getCvgLevelCost().get(coverageCode.toUpperCase(Locale.ROOT));
    }

    private List<ProspectCensusResponse> fetchCensus(Company company) {
        List<ProspectCensusResponse> census = company.isProspectCompany()
                ? prospectCensusService.getProspectCensus(company.getCode())
                : bssCoreServiceClient.getCensusByCompanyCode(company.getCode());
        return census == null ? Collections.emptyList() : census;
    }

    private DefaultPlanMappingServiceClient.PlanMappingRequest buildPlanMappingRequest(Company company, Set<String> employeeIds) {
        String mappingRuleTemplateName = CompanyServiceHelper.isTibProspect(company)
                ? OMS_RULE_TEMPLATE_NAME
                : HPP_RULE_TEMPLATE_NAME;
        return new DefaultPlanMappingServiceClient.PlanMappingRequest(
                company.getRealmPlanYear().getCloneProgram(),
                mappingRuleTemplateName,
                BenExchngEnums.getByQuarter(company.getRealmPlanYear().getOeQuarter())
                        .getExchangeId(),
                company.getRealmPlanYear().getPlanYearStart().toString(),
                employeeIds, company.getHeadQuatersState(),
                company.getZipCode(),
                company.getNaicsCode() == null ? null : String.valueOf(company.getNaicsCode()));
    }

    private List<EmplDefaultPlanAssignmentDto> buildAssignmentsFromPlanMapping(
            Company company,
            List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse,
            Map<String, XbssRealmPlyrPlan> plyrPlanMap) {

        List<DefaultPlanMappingServiceClient.PlanMappingResponse> safeResponses =
                safePlanMappingResponses(planMappingResponse);

        Set<EmplDefaultPlanAssignmentDto> assignments = new HashSet<>();
        for (DefaultPlanMappingServiceClient.PlanMappingResponse mappingResponse : safeResponses) {
            if (mappingResponse == null || CollectionUtils.isEmpty(mappingResponse.getEmployeeResponseList())) {
                continue;
            }
            for (DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse : mappingResponse.getEmployeeResponseList()) {
                processEmployeeMappings(company, employeeResponse, plyrPlanMap, assignments);
            }
        }

        return new ArrayList<>(assignments);
    }

    private List<DefaultPlanMappingServiceClient.PlanMappingResponse> safePlanMappingResponses(
            List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse) {
        return planMappingResponse == null ? Collections.emptyList() : planMappingResponse;
    }

    private void processEmployeeMappings(Company company,
            DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse,
            Map<String, XbssRealmPlyrPlan> plyrPlanMap,
            Set<EmplDefaultPlanAssignmentDto> assignments) {
        if (employeeResponse == null) {
            return;
        }

        String emplId = employeeResponse.getEmployeeId();
        if (emplId == null) {
            log.warn("Skipping mappedPlans entry because employee id is missing for company {}", company.getCode());
            return;
        }

        Map<String, DefaultPlanMappingServiceClient.PlanResponse> mappedPlans = employeeResponse.getMappedPlans();
        addAssignmentsFromMappedPlans(company, emplId, mappedPlans, plyrPlanMap, assignments);
    }

    private void addAssignmentsFromMappedPlans(Company company,
            String emplId,
            Map<String, DefaultPlanMappingServiceClient.PlanResponse> mappedPlans,
            Map<String, XbssRealmPlyrPlan> plyrPlanMap,
            Set<EmplDefaultPlanAssignmentDto> assignments) {
        if (mappedPlans == null || mappedPlans.isEmpty()) {
            log.warn("No mapped plans found for employee {} in company {}", emplId, company.getCode());
            return;
        }

        for (DefaultPlanMappingServiceClient.PlanResponse mappedPlan : mappedPlans.values()) {
            toAssignment(company, emplId, mappedPlan, plyrPlanMap).ifPresent(assignments::add);
        }
    }

    private Optional<EmplDefaultPlanAssignmentDto> toAssignment(Company company,
            String emplId,
            DefaultPlanMappingServiceClient.PlanResponse mappedPlan,
            Map<String, XbssRealmPlyrPlan> plyrPlanMap) {
        if (mappedPlan == null || mappedPlan.getPlanId() == null) {
            return Optional.empty();
        }

        String benefitPlanId = mappedPlan.getPlanId();
        XbssRealmPlyrPlan realmPlan = plyrPlanMap.get(benefitPlanId);
        if (realmPlan == null) {
            log.warn("Plan {} from mappedPlans not found in realm plan map for company {}", benefitPlanId,
                    company.getCode());
            return Optional.empty();
        }

        String coverageCode = mappedPlan.getEnrolledCvgCode();
        if (coverageCode == null || ProspectConstants.WAVED_COVERAGE.equalsIgnoreCase(coverageCode)) {
            return Optional.empty();
        }

        int portfolioId = realmPlan.getPortfolioId() == null ? 0 : realmPlan.getPortfolioId().intValue();
        return Optional.of(EmplDefaultPlanAssignmentDto.builder()
                .companyId(company.getId())
                .emplId(emplId)
                .planType(realmPlan.getPlanType())
                .portfolioId(portfolioId)
                .benefitPlanId(benefitPlanId)
                .coverageCode(coverageCode)
                .build());
    }

    private String resolveCoverageCode(ProspectCensusResponse censusResponse, String planType) {
        if (BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains(planType)) {
            return censusResponse.getMedicalTier();
        }
        if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
            return censusResponse.getDentalTier();
        }
        if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planType)) {
            return censusResponse.getVisionTier();
        }
        log.warn("Unsupported plan type [{}] while resolving coverage for employee [{}]", planType,
                censusResponse.getEmployeeId());
        return null;
    }
}