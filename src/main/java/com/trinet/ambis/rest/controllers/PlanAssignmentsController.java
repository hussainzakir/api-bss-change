package com.trinet.ambis.rest.controllers;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.aop.BSSEvictCache;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.service.model.planAvailability.EligibleEmployeePlanResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.prospect.CreatePlanAssignmentsRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.PlanAssignmentsResponse;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanAssignmentsService;
import com.trinet.ambis.service.dto.BasePlansResDto;
import com.trinet.ambis.service.model.Response;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.validator.GroupIdValidator;
import com.trinet.ambis.validator.StrategyIdValidator;
import com.trinet.domain.common.ReturnResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "trinet API-BSS Plan Assignments Controller")
public class PlanAssignmentsController {

	@Autowired
	PlanAssignmentsService planAssignmentsService;
	@Autowired
	CompanyService companyService;

    @GetMapping(value = URIConstants.PLAN_ASSIGNMENTS_BY_STRATEGY_GROUP)
    @ApiOperation(value = "Get employee plan assignments by Strategy ID and Group ID", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Plan assignments found and returned") })
    @ResponseBody
    public ReturnResponse<List<PlanAssignmentsResponse>> getPlanAssignments(HttpServletRequest request,
	    @StrategyIdValidator @PathVariable("strategyId") final long strategyId,
	    @GroupIdValidator @PathVariable("groupId") final long groupId,
	    @PathVariable("companyCode") final String companyCode,
	    @RequestParam(required = false, defaultValue = "") String exchangeId) {
	Company company = companyService.getCompanyDetails(companyCode, false,
		BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
	List<PlanAssignmentsResponse> responseData = planAssignmentsService.getPlanAssignments(strategyId, groupId,
		company);
	ReturnResponse<List<PlanAssignmentsResponse>> response = new ReturnResponse<>();
	response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
	response.setData(responseData);
	return response;
    }

	@GetMapping(value = URIConstants.ELIG_REGIONAL_PLANS_BY_STRATEGY)
	@ApiOperation(value = "Get eligible plans for the employee for StrategyID and BenefitType", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan assignments found and returned") })
	@ResponseBody
	public ReturnResponse<List<String>> getEligibleRegionalPlans(HttpServletRequest request,
			@PathVariable("strategyId") final long strategyId, @PathVariable("groupId") final long groupId,
			@PathVariable("benefitType") final String benefitType, @PathVariable("state") final String state,
			@PathVariable("zipCode") final String zipCode, @PathVariable("companyCode") final String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId,
			@RequestParam(required = false, defaultValue = "") String cvgTierCode) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		List<String> responseData = planAssignmentsService.getEligiblePlans(strategyId, groupId, state, zipCode,
				benefitType, company, cvgTierCode);
		ReturnResponse<List<String>> response = new ReturnResponse<>();
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setData(responseData);
		return response;
	}

	@GetMapping(value = URIConstants.PLAN_ASSIGNMENT_BASE_PLANS)
	@ApiOperation(value = "Get base plans for strategy and group", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Base plans found and returned") })
	@ResponseBody
	public ReturnResponse<List<BasePlansResDto>> getBasePlans(HttpServletRequest request,
			@PathVariable("companyCode") final String companyCode,
			@RequestParam(required = true) long strategyId,
			@RequestParam(required = true) long groupId,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		List<BasePlansResDto> responseData = planAssignmentsService.getBasePlans(strategyId, groupId, company);
		ReturnResponse<List<BasePlansResDto>> response = new ReturnResponse<>();
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setData(responseData);
		return response;
	}

	@PutMapping(value = URIConstants.PLAN_ASSIGNMENTS_BY_STRATEGY)
	@ApiOperation(value = "Create employee plan assignments")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan assignments created") })
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public void createPlanAssignments(HttpServletRequest request,
			@RequestBody final List<CreatePlanAssignmentsRequest> assignments,
			@StrategyIdValidator @PathVariable("strategyId") @CacheKey final long strategyId,
			@PathVariable("companyCode") final String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		planAssignmentsService.createPlanAssignments(strategyId, company, assignments);
	}

	@GetMapping(value = URIConstants.PLAN_ASSIGNMENT_BASE_PLAN_ELIGIBLE_EMPL)
	@ApiOperation(value = "Get employeeId/planId combinations for employees in the group eligible for a" +
			" regional plan associated with the passed in" +
			" base plan for the strategyId", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan assignments found and returned") })
	@ResponseBody
	public ReturnResponse<List<EligibleEmployeePlanResponse>> getEmployeeRegionalPlanForBasePlan(HttpServletRequest request,
			@PathVariable("strategyId") final long strategyId,
			@PathVariable("groupId") final long groupId,
			@PathVariable("companyCode") final String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId,
			@RequestParam(required = true, defaultValue = "") String basePlanId,
			@RequestParam(required = false, defaultValue = "") String benefitType) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));

        List<EligibleEmployeePlanResponse> responseData = planAssignmentsService
                .getProspectEmployeesEligibleForPlan(strategyId, groupId, company, basePlanId, benefitType);

		ReturnResponse<List<EligibleEmployeePlanResponse>> response = new ReturnResponse<>();
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setData(responseData);
		return response;
	}

	@GetMapping(value = URIConstants.PLAN_ASSIGNMENT_PLAN_RATE_BY_EMPLOYEE)
	@ApiOperation(value = "Get Plan Rates By Employee", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan costs by employee found and returned") })
	@ResponseBody
	public ReturnResponse<BigDecimal> getPlanRatesByEmployee(HttpServletRequest request,
			@PathVariable("prospectEmployeeID") final String prospectEmployeeID,
			@PathVariable("planId") final String planId,
			@PathVariable("coverageLevelCode") final String coverageLevelCode,
			@PathVariable("benefitType") String benefitType,
			@PathVariable("companyCode") final String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));

		BigDecimal responseData = planAssignmentsService.getOmsPlanRateByEmployee(company, prospectEmployeeID, planId, coverageLevelCode, benefitType);

		ReturnResponse<BigDecimal> response = new ReturnResponse<>();
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setData(responseData);
		return response;
	}

}
