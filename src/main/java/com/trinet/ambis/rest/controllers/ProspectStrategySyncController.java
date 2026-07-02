package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.ProspectStrategySyncService;
import com.trinet.ambis.service.model.ProspectStrategySyncData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Strategy Sync Controller")
@Validated
public class ProspectStrategySyncController {

	@Autowired
	private ProspectStrategySyncService prospectStrategySyncService;

	@PutMapping(value = URIConstants.PROSPECT_CENSUS_SYNC)
	@ApiOperation(value = "Sync Prospect Strategy")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy Synced successfully") })
	@ResponseBody
	public void syncProspectStrategyData(HttpServletRequest request,
			@RequestBody List<ProspectStrategySyncData> prospectStrategySyncData,
			@PathVariable("companyCode") final String companyCode) {
		prospectStrategySyncService.handleCensusChangeEvent(prospectStrategySyncData, companyCode);
	}

	@DeleteMapping(value = URIConstants.PROSPECT_CENSUS_SYNC)
	@ApiOperation(value = "Delete census data to sync Prosect Trinet Strategy")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy Synced successfully") })
	public void syncProspectStrategyData(@RequestBody List<String> employeeIds,
			@PathVariable("companyCode") final String companyCode) {
		prospectStrategySyncService.handleCensusDeleteEvent(employeeIds, companyCode);
	}

	@PostMapping(value = URIConstants.PROSPECT_CENSUS_SYNC)
	@ApiOperation(value = "Sync Add Event Prospect Strategy")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy Synced successfully") })
	@ResponseBody
	public void syncProspectStrategyDataForAddEvent(HttpServletRequest request,
			@RequestBody List<ProspectStrategySyncData> prospectStrategySyncData,
			@PathVariable("companyCode") final String companyCode) {
		prospectStrategySyncService.handleCensusAddEvent(prospectStrategySyncData, companyCode);
	}

	@PostMapping(value = URIConstants.PROSPECT_TIB_RATE_SYNC_ON_DEPENDENT_CHANGE)
	@ApiOperation(value = "Sync tib plan rates on dependent change")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "TIB plan rates synced successfully") })
	public void rateSyncOnDependentChange(
			@RequestBody List<String> employeeIds,
			@PathVariable("companyCode") String companyCode) {
		prospectStrategySyncService.rateSyncOnDependentChange(employeeIds, companyCode);
	}

    @PostMapping(value = URIConstants.PROSPECT_TIB_RATE_SYNC_ON_CENSUS_DEPENDENT_CHANGE)
    @ApiOperation(value = "Sync tib plan rates on census dependent change")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "TIB plan rates synced successfully") })
    public void rateSyncOnCensusDependentChange(
            @RequestBody List<ProspectCensusResponse> prospectCensusResponseList,
            @PathVariable("companyCode") String companyCode) {
        prospectStrategySyncService.rateSyncOnCensusDependentChange(prospectCensusResponseList, companyCode);
    }
}