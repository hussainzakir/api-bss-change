/**
 * 
 */
package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.model.AdditionalBenefitPlanRates;
import com.trinet.ambis.util.BSSSecurityUtils;
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
@Api(value = "Trinet API-BSS additional benefits plans controller")
public class AdditionalBenefitsController {

	@Autowired
	CompanyService companyService;
	@Autowired
	AdditionalBenefitPlanService additionalBenefitPlanService;

	@GetMapping(value = URIConstants.GET_ADDITIONAL_PLAN_RATES)
	@ApiOperation(value = "Get the additional plan rates details", response = AdditionalBenefitPlanRates.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Additional plan rates retrived successfully") })
	@ResponseBody
	public List<AdditionalBenefitPlanRates> getAdditionalPlanRates(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") final long strategyId,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		List<AdditionalBenefitPlanRates> additionalBenefitPlanRates = null;
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		additionalBenefitPlanRates = additionalBenefitPlanService.getADBPlanCostByGroup(company, strategyId);
		return additionalBenefitPlanRates;
	}

	@GetMapping(value = URIConstants.GET_ADDITIONAL_PLAN_RATES_NEW_COMPANY)
	@ApiOperation(value = "Get the additional plan rates details for new company", response = AdditionalBenefitPlanRates.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Additional plan rates for new company retrived successfully") })
	@ResponseBody
	public List<AdditionalBenefitPlanRates> getAdditionalPlanRatesNewCompany(HttpServletRequest request,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		List<AdditionalBenefitPlanRates> additionalBenefitPlanRates = null;
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		additionalBenefitPlanRates = additionalBenefitPlanService.getADBPlanCostByGroup(company, 0L);
		return additionalBenefitPlanRates;
	}
}
