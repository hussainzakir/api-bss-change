package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.BenefitGroupFundingService;
import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Benefit Group Funding Controller")
public class BenefitGroupFundingController {

	@Autowired
	BenefitGroupFundingService benefitGroupFundingService;

	@PutMapping(value = URIConstants.GROUP_FUNDING)
	@ApiOperation(value = "Updates the benefit group funding for prospect's current strategy", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Benefit group funding updated successfully") })
	@ResponseBody
	public void updateBenefitGroupFunding(HttpServletRequest request, @PathVariable("strategyId") final long strategyId,
			@PathVariable("groupId") final long groupId, @PathVariable("companyCode") final String companyCode,
			@RequestBody final List<GroupFundingReq> groupFundingReqs) {
		benefitGroupFundingService.updateBenefitGroupFunding(companyCode, strategyId, groupId, groupFundingReqs);
	}

	@GetMapping(value = URIConstants.GROUP_FUNDING)
	@ApiOperation(value = "Returns the benefit group funding for prospect's current strategy", response = GroupFundingRes.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Benefit group funding fetched successfully") })
	@ResponseBody
	public List<GroupFundingRes> getBenefitGroupFunding(HttpServletRequest request,
			@PathVariable("strategyId") final long strategyId, @PathVariable("groupId") final long groupId,
			@PathVariable("companyCode") final String companyCode) {
		return benefitGroupFundingService.getBenefitGroupFunding(companyCode, strategyId, groupId);
	}

}