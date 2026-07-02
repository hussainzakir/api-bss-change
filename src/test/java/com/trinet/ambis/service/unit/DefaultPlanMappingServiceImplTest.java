package com.trinet.ambis.service.unit;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.OmsOfferingEnum;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;
import com.trinet.ambis.service.impl.DefaultPlanMappingServiceImpl;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.client.DefaultPlanMappingServiceClient;
import com.trinet.ambis.util.BssCoreServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPlanMappingServiceImplTest {

    private static final long STRATEGY_ID = 987L;

    @InjectMocks
    private DefaultPlanMappingServiceImpl service;

    @Mock
    private RealmPlyrPlanService realmPlyrPlanService;
    @Mock
    private DefaultPlanMappingServiceClient planMappingServiceClient;
    @Mock
    private EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;
    @Mock
    private EmployeePlanAssignmentService employeePlanAssignmentService;
    @Mock
    private ProspectCensusService prospectCensusService;
    @Mock
    private BssCoreServiceClient bssCoreServiceClient;

    @Captor
    private ArgumentCaptor<List<EmplDefaultPlanAssignmentDto>> assignmentsCaptor;
    @Captor
    private ArgumentCaptor<DefaultPlanMappingServiceClient.PlanMappingRequest> requestCaptor;
    @Captor
    private ArgumentCaptor<List<EePlanAssignment>> eeAssignmentsCaptor;

    @Test
    public void callPlanMappingService_nonTib_usesDefaultAssignmentFlowAndDefaultTemplate() {
        Company company = buildCompany(true, false);
        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(Map.of(
                "MED1", realmPlan("MED1", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 10)
        ));

        DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse =
                new DefaultPlanMappingServiceClient.EmployeeResponse("E1", Map.of(
                        "medical", new DefaultPlanMappingServiceClient.PlanResponse("MED1", "EE", null)
                ));
        when(planMappingServiceClient.getPlanMapping(eq(company.getCode()), any()))
                .thenReturn(List.of(new DefaultPlanMappingServiceClient.PlanMappingResponse(List.of(employeeResponse))));

        service.callPlanMappingService(company, Collections.emptySet());

        verify(planMappingServiceClient).getPlanMapping(eq(company.getCode()), requestCaptor.capture());
        verify(emplDefaultPlanAssignmentService).saveAll(assignmentsCaptor.capture());
        verify(employeePlanAssignmentService, never()).saveEePlanAssignments(any());
        verify(prospectCensusService, never()).getProspectCensus(any());
        verify(bssCoreServiceClient, never()).getCensusByCompanyCode(any());

        DefaultPlanMappingServiceClient.PlanMappingRequest request = requestCaptor.getValue();
        assertEquals("PROSPECT_DEFAULT", request.getMappingRuleTemplateName());
        assertEquals(BenExchngEnums.getByQuarter(company.getRealmPlanYear().getOeQuarter()).getExchangeId(), request.getExchangeId());
        assertEquals("CA", request.getHqState());
        assertEquals("94043", request.getHqZip());
        assertEquals("541512", request.getNaicsCode());

        List<EmplDefaultPlanAssignmentDto> assignments = assignmentsCaptor.getValue();
        assertEquals(1, assignments.size());
        assertEquals("MED1", assignments.get(0).getBenefitPlanId());
        assertEquals("EE", assignments.get(0).getCoverageCode());
    }

    @Test
    public void callPlanMappingService_tib_usesEeAssignmentFlowWithStrategyAndCost() {
        Company company = buildCompany(true, true);
        List<ProspectCensusResponse> census = List.of(
                ProspectCensusResponse.builder()
                        .employeeId("E1")
                        .medicalTier("EE")
                        .build());

        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(census);
        DefaultPlanMappingServiceClient.PlanCostResponse planCostResponse =
                new DefaultPlanMappingServiceClient.PlanCostResponse(
                        new BigDecimal("12.34"), new BigDecimal("45.67"), new BigDecimal("58.01"));

        DefaultPlanMappingServiceClient.PlanResponse planResponse =
                new DefaultPlanMappingServiceClient.PlanResponse("MED1", "EE", Map.of("EE", planCostResponse));

        DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse =
                new DefaultPlanMappingServiceClient.EmployeeResponse("E1", Map.of(
                        "medical", planResponse
                ));

        when(planMappingServiceClient.getPlanMapping(eq(company.getCode()), any()))
                .thenReturn(List.of(new DefaultPlanMappingServiceClient.PlanMappingResponse(List.of(employeeResponse))));

         List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse =
                 service.callPlanMappingServiceForOms(company, Collections.emptySet());
         service.createOmsEePlanAssignments(company, STRATEGY_ID, planMappingResponse);

         verify(planMappingServiceClient).getPlanMapping(eq(company.getCode()), requestCaptor.capture());
         verify(employeePlanAssignmentService).saveEePlanAssignments(eeAssignmentsCaptor.capture());
         verify(emplDefaultPlanAssignmentService, never()).saveAll(any());

        DefaultPlanMappingServiceClient.PlanMappingRequest request = requestCaptor.getValue();
        assertEquals("PROSPECT_OMS", request.getMappingRuleTemplateName());
        assertEquals("CA", request.getHqState());
        assertEquals("94043", request.getHqZip());
        assertEquals("541512", request.getNaicsCode());

        List<EePlanAssignment> assignments = eeAssignmentsCaptor.getValue();
        assertEquals(1, assignments.size());
        EePlanAssignment assignment = assignments.get(0);
        assertEquals(STRATEGY_ID, assignment.getEePlanAssignmentPK().getStrategyId());
        assertEquals("E1", assignment.getEePlanAssignmentPK().getEmplId());
        assertEquals(BSSApplicationConstants.MEDICAL_PLAN_TYPE, assignment.getEePlanAssignmentPK().getBenefitType());
        assertEquals("MED1", assignment.getBenefitPlan());
        assertEquals("EE", assignment.getCovrgCD());
        assertEquals(new BigDecimal("12.34"), assignment.getEeRate());
        assertEquals(new BigDecimal("58.01"), assignment.getErRate());
    }

    @Test
    public void callPlanMappingService_tib_skipsPersistenceWhenNoValidAssignments() {
        Company company = buildCompany(true, true);
        List<ProspectCensusResponse> census = List.of(ProspectCensusResponse.builder().employeeId("E1").medicalTier("EE").build());

        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(census);

        DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse =
                new DefaultPlanMappingServiceClient.EmployeeResponse("E1", Map.of(
                        "medical", new DefaultPlanMappingServiceClient.PlanResponse(null, null, null)
                ));
        when(planMappingServiceClient.getPlanMapping(eq(company.getCode()), any()))
                .thenReturn(List.of(new DefaultPlanMappingServiceClient.PlanMappingResponse(List.of(employeeResponse))));

         List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse =
                 service.callPlanMappingServiceForOms(company, Collections.emptySet());
         service.createOmsEePlanAssignments(company, STRATEGY_ID, planMappingResponse);

         verify(employeePlanAssignmentService, never()).saveEePlanAssignments(any());
         verify(emplDefaultPlanAssignmentService, never()).saveAll(any());
     }

    @Test
    public void callPlanMappingService_nonProspectCompany_doesNotFetchCensusForDefaultAssignments() {
        Company company = buildCompany(false, false);
        company.setProspectCompany(false);

        when(realmPlyrPlanService.getMapForRealmPlanYear(company.getRealmPlanYear().getId())).thenReturn(Map.of(
                "MED1", realmPlan("MED1", BSSApplicationConstants.MEDICAL_PLAN_TYPE, 10)
        ));

        DefaultPlanMappingServiceClient.EmployeeResponse employeeResponse =
                new DefaultPlanMappingServiceClient.EmployeeResponse("E1", Map.of(
                        "medical", new DefaultPlanMappingServiceClient.PlanResponse("MED1", "EE", null)
                ));

        when(planMappingServiceClient.getPlanMapping(eq(company.getCode()), any()))
                .thenReturn(List.of(new DefaultPlanMappingServiceClient.PlanMappingResponse(List.of(employeeResponse))));

        service.callPlanMappingService(company, Collections.emptySet());

        verify(bssCoreServiceClient, never()).getCensusByCompanyCode(any());
        verify(prospectCensusService, never()).getProspectCensus(any());
    }

    private Company buildCompany(boolean prospectCompany, boolean tibProspect) {
        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setId(101L);
        realmPlanYear.setOeQuarter("Q1");
        realmPlanYear.setPlanYearStart(new Date(1704067200000L));
        realmPlanYear.setCloneProgram("102");

        Realm realm = new Realm();
        realm.setBenExchange(tibProspect ? BenExchngEnums.TRINET_OMS.getBenExchng() : BenExchngEnums.TRINET_III.getBenExchng());

        Company company = new Company();
        company.setId(999L);
        company.setCode("COMP1");
        company.setProspectCompany(prospectCompany);
        company.setRealm(realm);
        company.setRealmPlanYear(realmPlanYear);
        company.setHeadQuatersState("CA");
        company.setZipCode("94043");
        company.setNaicsCode(541512);
        if (tibProspect) {
            company.setOmsOffering(OmsOfferingEnum.OM_TD_TV_TLD.name());
        }
        return company;
    }


    private XbssRealmPlyrPlan realmPlan(String benefitPlan, String planType, int portfolioId) {
        XbssRealmPlyrPlan plan = new XbssRealmPlyrPlan();
        plan.setBenefitPlan(benefitPlan);
        plan.setPlanType(planType);
        plan.setPortfolioId(BigDecimal.valueOf(portfolioId));
        return plan;
    }
}
