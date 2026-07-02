/**
 * 
 */
package com.trinet.ambis.rest.controllers;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.aop.BSSEvictCache;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.Response;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.validator.StrategyIdValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author rvutukuri
 */

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Employee Controller")
public class EmployeeController {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

	@Autowired
	EmployeeDataService employeeDataService;
	@Autowired
	CompanyService companyService;
	@Autowired
	StrategySyncService strategySyncService;

	private static final boolean IS_HISTORY = false;

	@GetMapping(value = URIConstants.GET_EMPLOYEE_DATA)
	@ApiOperation(value = "Gets Employee data details", response = EmployeeData.class, responseContainer = "Set")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Employee details retrieved successfully") })
	@ResponseBody
	public Set<EmployeeData> getEmployeeData(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") long strategyId,
			@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		long startTime = System.currentTimeMillis();
		Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		Set<EmployeeData> employeesData = employeeDataService.getEmployeesData(company, strategyId);
		long endTime = System.currentTimeMillis();
		logger.info("getEmployeeData() took {} ms", (endTime - startTime));
		return employeesData;
	}

	@PutMapping(value = URIConstants.UPDATE_EMPLOYEE_ASSIGNMENT)
	@ApiOperation(value = "Updates Employee Assignment", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Updated employee assignment successfully") })
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public Response updateEmployeeAssignment(HttpServletRequest request,
			@RequestBody final EmployeeAssignmentData employeeAssignmentData,
			@PathVariable("strategyId") @CacheKey final long strategyId,
			@PathVariable("companyCode") final String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {

		boolean returnValue = false;
		Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		if (company != null) {
			employeeDataService.updateEmployeeAssignment(company, employeeAssignmentData, strategyId);
			if (!StrategyServiceHelper.isProspectStrategy(strategyId)) {
				strategySyncService.syncStrategyData(company, strategyId);
			}
			returnValue = true;
		}

		return new Response(returnValue);
	}

}