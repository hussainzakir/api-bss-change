package com.trinet.ambis.rest.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.prospect.ProspectPlanService;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Benefit Plan Controller")
public class ProspectBenefitPlanController {

	@Autowired
	ProspectPlanService prospectPlanService;

	@GetMapping(value = URIConstants.BENEFIT_PLANS)
	@ApiOperation(value = "Returns the benefit plans for prospect", response = BenefitPlansRes.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Benefit plans fetched successfully") })
	@ResponseBody
	public List<BenefitPlansRes> getBenefitPlansBy(@PathVariable("prospectId") final String prospectId,
			@RequestParam(required = false, defaultValue = "") String rateType,
			@RequestParam(required = false, defaultValue = "false") boolean includeWithRates) {
		return prospectPlanService.getBenefitPlansBy(prospectId, rateType, includeWithRates);
	}
}
