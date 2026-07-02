package com.trinet.ambis.rest.controllers;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.model.RateUpdateDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "TriNet API-BSS FlexRate Controller")
@RequiredArgsConstructor
public class FlexRateController {

	private final FlexRateService flexRateService;

	@PutMapping(value = URIConstants.RATE_UPDATE)
	@ApiOperation(value = "Update rate group for a company if rateGroupId changed")
	@ApiResponses({
			@ApiResponse(code = 202, message = "Rate group updated successfully."),
			@ApiResponse(code = 200, message = "No update performed. The rate group was already set to the requested value."),
			@ApiResponse(code = 400, message = "Bad request.")
	})
	public ResponseEntity<?> updateRateGroup(
			@PathVariable("companyCode") String companyCode,
			@Valid @RequestBody RateUpdateDto dto) {
		boolean changed = flexRateService.processRateUpdateEvent(dto);
		return ResponseEntity.status(changed ? HttpStatus.ACCEPTED : HttpStatus.OK).build();
	}
}