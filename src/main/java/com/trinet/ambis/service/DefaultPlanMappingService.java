package com.trinet.ambis.service;

import java.util.List;
import java.util.Set;

import com.trinet.ambis.client.DefaultPlanMappingServiceClient;
import com.trinet.ambis.persistence.model.Company;

public interface DefaultPlanMappingService {

    /**
     * Calls the plan mapping service for the given company and triggers default plan assignment flow.
     *
     * @param company the company for which plan mappings are requested
     */
    void callPlanMappingService(Company company);

    /**
     * Calls the plan mapping service for specific employees in the given company.
     *
     * @param company the company for which plan mappings are requested
     * @param employeeIds the set of employee IDs to map plans for
     */
    void callPlanMappingService(Company company, Set<String> employeeIds);

    /**
     * Persists default plan mappings for a company using the plan mapping response payload.
     *
     * @param company the company for which default mappings are being saved
     * @param planMappingResponse the plan mapping response entries returned by the plan mapping service
     */
    void saveDefaultPlanMappings(Company company, List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse);

    /**
     * Creates and persists OMS employee plan assignments for a strategy using the plan mapping response payload.
     *
     * @param company the company for which OMS assignments are being saved
     * @param strategyId strategy id
     * @param planMappingResponse the plan mapping response entries returned by the plan mapping service
     */
    void createOmsEePlanAssignments(Company company, long strategyId,
            List<DefaultPlanMappingServiceClient.PlanMappingResponse> planMappingResponse);

    /**
     * Calls the plan mapping service for OMS companies and returns the plan mapping response.
     *
     * @param company the company for which plan mappings are requested
     * @param employeeIds the set of employee IDs to map plans for (can be empty)
     * @return the plan mapping response from the service
     */
    List<DefaultPlanMappingServiceClient.PlanMappingResponse> callPlanMappingServiceForOms(Company company, Set<String> employeeIds);
}
