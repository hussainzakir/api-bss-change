package com.trinet.ambis.service;

import java.math.BigDecimal;
import java.util.List;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.prospect.CreatePlanAssignmentsRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.PlanAssignmentsResponse;
import com.trinet.ambis.service.dto.BasePlansResDto;
import com.trinet.ambis.service.model.planAvailability.EligibleEmployeePlanResponse;

public interface PlanAssignmentsService {

	/**
	 * Intended for use with new prospects, this returns a List of
	 * <code>PlanAssignmentsResponse</code> objects associated with the given
	 * strategy. All employees known for the prospect companyCode will be returned.
	 * Known current assignments will be returned and employees without a plan
	 * assignment will have no plan assignment.
	 * 
	 * @param strategyId
	 * @param company
	 * @return a List of plan assignment response objects
	 */
	List<PlanAssignmentsResponse> getPlanAssignments(long strategyId, long groupIs, Company company);

	/**
	 * Intended for use with new prospects, this creates new
	 * <code>EePlanAssignment</code> entities from the request data. Employee IDs in
	 * the request data will be validated against the companyCode. Benefit plans in
	 * the request data will be validated against the strategyId corresponding
	 * plan/year.
	 *
	 * If the company is a OMS TIB company, the ee and er costs will be calculated and
	 * saved for the employees.
	 * 
	 * @param strategyId
	 * @param company
	 * @param assignments
	 */
	void createPlanAssignments(long strategyId, Company company, List<CreatePlanAssignmentsRequest> assignments);

	/**
	 *
	 * @param strategyId
	 * @param groupId
	 * @param company
	 * @return the base plans for the given strategy/group
	 */
	List<BasePlansResDto> getBasePlans(long strategyId, long groupId, Company company);


	/**
	 * This returns list of hris medical plans if TIB prospect and benefit type is
	 * medical. or returns eligible regional plans.
	 * 
	 * @param strategyId
	 * @param groupId
	 * @param state
	 * @param zipCode
	 * @param benefitType
	 * @param company
	 * @param cvgTierCode
	 * @return a list of plan id.
	 */
	List<String> getEligiblePlans(long strategyId, long groupId, String state, String zipCode, String benefitType,
			Company company, String cvgTierCode);

	/**
	 * Returns cost of the plan for TIB employee for the given planId and coverage level
	 *
	 * @param company
	 * @param employeeId
	 * @param planId
	 * @param covgLevelCode
	 * @return
	 */
	BigDecimal getOmsPlanRateByEmployee(Company company, String employeeId, String planId, String covgLevelCode, String benefitType);

	/**
	 * Returns a list of eligible employees and their plans for the given base plan.
	 * 
	 * @param strategyId
	 * @param groupId
	 * @param company
	 * @param basePlanId
	 * @param benefitType
	 * @return a List of eligible employee plans
	 */
	List<EligibleEmployeePlanResponse> getProspectEmployeesEligibleForPlan(long strategyId, long groupId,
			Company company, String basePlanId, String benefitType);
}
