package com.trinet.ambis.rest.controllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.rest.controllers.dto.PlanDeselectionExceptionResDto;
import com.trinet.ambis.service.PlanDeselectionExceptionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@PreAuthorize("@hrpService.hasAccessToGatewayApp(T(com.trinet.ambis.common.BSSApplicationConstants).PLAN_DESELECTION_EXCEPTION_APP_KEY)")
@Api(value = "Trinet API-BSS Plan Deselection Exception Controller")
public class PlanDeselectionExceptionController {

	@Autowired
	private PlanDeselectionExceptionService planDeselectionExceptionService;

	@GetMapping(value = URIConstants.PLAN_DESELECTION_EXCEPTIONS)
	@ApiOperation(value = "Gets all active plan deselection exceptions.", response = PlanDeselectionExceptionResDto.class, responseContainer = "Set")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "All active plan deselection exceptions retrieved successfully.") })
	@ResponseBody
	public Set<PlanDeselectionExceptionResDto> findAllActive() {
		return planDeselectionExceptionService.findAllActive();
	}

	@GetMapping(value = URIConstants.PLAN_DESELECTION_EXCEPTIONS_GET_BY_EXCEPTION_ID)
	@ApiOperation(value = "Gets the plan deselection exception for the give exception id.", response = PlanDeselectionExceptionResDto.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Plan deselection exception for the given id retrieved successfully.") })
	@ResponseBody
	public PlanDeselectionExceptionResDto findById(@PathVariable("planDeselectionExceptionId") long id) {
		return planDeselectionExceptionService.findById(id);
	}

	@PostMapping(value = URIConstants.PLAN_DESELECTION_EXCEPTIONS)
	@ApiOperation(value = "Creates plan deselection exception.", response = PlanDeselectionExceptionResDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan deselection exception created successfully.") })
	@ResponseBody
	public PlanDeselectionExceptionResDto create(@RequestBody PlanDeselectionExceptionResDto dto) {
		return planDeselectionExceptionService.create(dto);
	}

	@PutMapping(value = URIConstants.PLAN_DESELECTION_EXCEPTIONS)
	@ApiOperation(value = "Updates plan deselection exception.", response = PlanDeselectionExceptionResDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan deselection exception updated successfully.") })
	@ResponseBody
	public PlanDeselectionExceptionResDto update(@RequestBody PlanDeselectionExceptionResDto dto) {
		return planDeselectionExceptionService.update(dto);
	}

}
