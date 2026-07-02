package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.HqOverridesService;
import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS HqOverridesAdminController Controller")
public class HqOverridesAdminController {

	@Autowired
	HqOverridesService hqOverridesService;

	@GetMapping(value = URIConstants.HQ_OVERRIDES)
	@ApiOperation(value = "Gets COMPANY_HQ_OVERRIDES_DETAILS", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Hq Override retrived successfully") })
	@ResponseBody
	public List<HqOverridesDto> getCompanyHqOverridesData(HttpServletRequest request,
			@RequestParam(value = "companyCode", required = false) String companyCode,
			@RequestParam(value = "quarter", required = false) String quarter) {
	 
		return hqOverridesService.getHqOverridesDetails(companyCode, quarter);
	}

	@PostMapping(value = URIConstants.HQ_OVERRIDES)
	@ApiOperation(value = "Create HQ override", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Hq Override created successfully") })
	@ResponseBody
	public HqOverridesDto createCompanyHqOverridesData(HttpServletRequest request, @RequestBody HqOverridesDto hqOverridesDto) {
		return  hqOverridesService.createHqOverridesDetails(hqOverridesDto);
	}
	@GetMapping(value = URIConstants.HQ_OVERRIDES_COMPANY)
	@ApiOperation(value = "Returns the HQ overrides for given company", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "HQ overrides for given company retrived successfully") })
	@ResponseBody
	public List<CompanyHQData> getCompanyData(HttpServletRequest request, @PathVariable("companyCode") String code) {
		return hqOverridesService.getCompanyPlanYearData(code);
	}
 
	@DeleteMapping(value = URIConstants.HQ_OVERRIDES_COMPANY_REALMYEARID)
	@ApiOperation(value = "Delete HQ Override ", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "HQ Override deleted successfully") })
	@ResponseBody
	public void deleteHq(HttpServletRequest request, @PathVariable(value = "companyCode") String companyCode,
			@PathVariable(value = "realmYearId") Integer realmYearId) {
		hqOverridesService.deleteHqOverride(companyCode,realmYearId);
	}

}
