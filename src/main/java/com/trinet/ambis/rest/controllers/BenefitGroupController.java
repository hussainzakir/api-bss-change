/**
 * 
 */
package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.aop.BSSEvictCache;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitGroupHeadCountService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.model.GroupData;
import com.trinet.ambis.service.model.HeadCountData;
import com.trinet.ambis.service.model.Response;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.validator.GroupIdValidator;
import com.trinet.ambis.validator.RequestValidator;
import com.trinet.ambis.validator.StrategyIdValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author rvutukuri
 *
 */
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Benefit Group Controller")
public class BenefitGroupController {

	@Autowired
	BenefitGroupService benefitGroupService;

	@Autowired
	CompanyService companyService;

	@Autowired
	BenefitGroupHeadCountService benefitGroupHeadCountService;

	private static final boolean IS_HISTORY = false;

	@PostMapping(value = URIConstants.ADD_GROUP)
	@ApiOperation(value = "Add a new benefit group", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "New benefit group added successfully") })
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public Response addGroup(HttpServletRequest request, @RequestBody final GroupData groupData,
			@StrategyIdValidator @PathVariable("strategyId") @CacheKey final long strategyId,
			@PathVariable("companyCode") final String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		boolean returnValue = false;
		groupData.setDestGroupName(RequestValidator.getValidatedGroupName(groupData.getDestGroupName()));
		Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		if (company != null) {
			benefitGroupService.addGroup(company, groupData, strategyId);
			returnValue = true;
		}
		return new Response(returnValue);
	}

	@PutMapping(value = URIConstants.UPDATE_GROUP)
	@ApiOperation(value = "Update existing benefit group", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Benefit group updated successfully") })
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public void updateGroup(HttpServletRequest request, @PathVariable("companyCode") final String companyCode,
			@GroupIdValidator @PathVariable("groupId") final long groupId, 
			@StrategyIdValidator @PathVariable("strategyId") @CacheKey final long strategyId,
			@PathVariable("waitPeriod") final String waitPeriod,
			@PathVariable("defaultFlag") final boolean defaultFlag,
			@RequestParam(required = false, defaultValue = "") String exchangeId
			) {
		Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		if (company != null) {
			benefitGroupService.updateBenefitGroupMetaData(companyCode, groupId, strategyId, waitPeriod,
					defaultFlag, company.getRealmPlanYearId());
		}
	}


	@PutMapping(value = URIConstants.BENEFIT_GROUP_NAME)
	@ApiOperation(value = "Update benefit group name", response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "updated benefit group name successfully") })
	@ResponseBody
	public void updateBenefitGroupName( HttpServletRequest request, @GroupIdValidator @PathVariable("groupId") final long groupId,
			@RequestBody final String newBenefitGroupName ) {
		benefitGroupService.updateBenefitGroupName( groupId, RequestValidator.getValidatedGroupName( newBenefitGroupName ) );
	}

	@PutMapping(value = URIConstants.BENEFIT_GROUP_NAME_V1)
	@ApiOperation(value = "Update benefit group name", response = Void.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "updated benefit group name successfully")})
	@ResponseBody
	public void updateBenefitGroupNameV1(HttpServletRequest request, @PathVariable("strategyId") final long strategyId,
										 @GroupIdValidator @PathVariable("groupId") final long groupId,
										 @RequestBody final String newBenefitGroupName) {
		benefitGroupService.updateBenefitGroupName(strategyId, groupId,
				RequestValidator.getValidatedGroupName(newBenefitGroupName));
	}


	@DeleteMapping(value = URIConstants.DELETE_GROUP)
	@ApiOperation(value = "Deletes benefit group", response = Response.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Benefit group deleted successfully")})
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public Response deleteGroup(HttpServletRequest request,
								@StrategyIdValidator @PathVariable("strategyId") @CacheKey final long strategyId,
								@PathVariable("strategyGroupId") final long strategyGroupId,
								@PathVariable("companyCode") final String companyCode,
								@RequestParam(required = false, defaultValue = "") String exchangeId) {
		boolean returnValue = false;
		Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		if (company != null) {
			benefitGroupService.deleteGroup(strategyGroupId, strategyId);
			returnValue = true;
		}
		return new Response(returnValue);
	}

	@RequestMapping(value = URIConstants.UPDATE_GROUP_HEAD_COUNT, method = RequestMethod.PUT)
	@ApiOperation(value = "Updates benefit group head count", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Benefit group head count updated successfully") })
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public Response updateGroupHeadCount(HttpServletRequest request,
			@RequestBody final List<HeadCountData> headCountList,
			@StrategyIdValidator @PathVariable("strategyId") @CacheKey final long strategyId,
			@PathVariable("companyCode") final String companyCode) {
		boolean returnValue = false;
		Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
				BSSSecurityUtils.getAuthenticatedPersonId(), null);
		if (company != null) {
			benefitGroupHeadCountService.updateGroupHeadCount(company, headCountList);
			returnValue = true;
		}
		return new Response(returnValue);
	}

    @PutMapping(value = URIConstants.BENEFIT_GROUP_NAME_TYPE_K1)
    @ApiOperation(value = "Convert benefit group to K1 group", response = Void.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "updated benefit group to K1 group successfully")})
    @ResponseBody
    public void updateBenefitGroupTypeToK1(HttpServletRequest request,
                                           @PathVariable("strategyId") final long strategyId,
                                           @GroupIdValidator @PathVariable("groupId") final long groupId,
                                           @PathVariable("companyCode") final String companyCode) {
        benefitGroupService.updateBenefitGroupType(strategyId, groupId, companyCode);
    }
}
