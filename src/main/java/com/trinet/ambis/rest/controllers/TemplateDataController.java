package com.trinet.ambis.rest.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.PSoftCompanyNotFound;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitCategoriesService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.TemplateDataService;
import com.trinet.ambis.service.model.BenefitsCategories;
import com.trinet.ambis.service.model.NewCompanyOptions;
import com.trinet.ambis.util.BSSSecurityUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Template Data Controller")
public class TemplateDataController {
	private static final Logger logger = LoggerFactory.getLogger(TemplateDataController.class);

	@Autowired
	private CompanyService companyService;

	@Autowired
	private TemplateDataService templateDataService;
	@Autowired
	private BenefitCategoriesService benefitCategoriesService;

	@GetMapping(value = URIConstants.NEW_COMPANY_STRATEGY_OPTIONS)
	@ApiOperation(value = "Creates new company strategy", response = NewCompanyOptions.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Created new company strategy options successfully") })
	@ResponseBody
	public NewCompanyOptions newCompanyOptions(HttpServletRequest request, @PathVariable("companyCode") String code) {
		long startTime = System.currentTimeMillis();
		Company company = null;
		NewCompanyOptions newCompanyStrategyOptions;
		company = companyService.getCompanyDetails(code);
		if (company == null) {
			throw new PSoftCompanyNotFound();
		}
		newCompanyStrategyOptions = templateDataService.getNewCompanyOptions(company);
		long endTime = System.currentTimeMillis();
		logger.info("newCompanyOptions() took {} ms", (endTime - startTime));
		return newCompanyStrategyOptions;
	}

	@GetMapping(value = URIConstants.GET_BENEFITS_CATEGORIES)
	@ApiOperation(value = "Gets benefit group categories", response = BenefitsCategories.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Benefit group categories retrived successfully") })
	@ResponseBody
	public BenefitsCategories getBenefitsCategories(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		long startTime = System.currentTimeMillis();
		logger.info("In getBenefitsCategories()");
		boolean isHistory = false;
		BenefitsCategories benefitsCategories;
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
		benefitsCategories = benefitCategoriesService.constructBenefitsCategories(company);
		long endTime = System.currentTimeMillis();
		logger.info("Benefits-categories API TOOK : {} ms", (endTime - startTime));
		return benefitsCategories;
	}
}
