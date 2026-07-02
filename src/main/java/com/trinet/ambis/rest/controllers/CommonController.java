package com.trinet.ambis.rest.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.FeatureFlagService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RefreshService;
import com.trinet.ambis.service.model.FeatureFlag;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;

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
@Api(value = "Trinet API-BSS Common Controller")
public class CommonController {

	@Autowired
	private PersonService personService;
	@Autowired
	private RefreshService refreshService;
	@Autowired
	private RealmPlanYearRuleConfigService ruleConfigService;
	@Autowired
	private CompanyService companyService;
	@Autowired
	private AppRulesConfigService appRulesConfigService;
	@Autowired
	private	FeatureFlagService featureFlagService;

	private static final Logger logger = LoggerFactory.getLogger(CommonController.class);

	/**
	 * @param userPersonId
	 * @return String
	 * @throws JsonProcessingException
	 */
	@GetMapping(value = URIConstants.GET_PERSON_NAME)
	@ApiOperation(value = "Gets the Prerson full name", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Prerson full name retrived successfully") })
	@ResponseBody
	public String getPersonName(HttpServletRequest request) {
		logger.info("Inside getPersonName() of PersoNameController");
		String employeeName = null;
		employeeName = personService.getPersonFirstAndLastName(BSSSecurityUtils.getAuthenticatedPersonId());
		return employeeName;
	}

	@GetMapping(value = URIConstants.REFRESH_PLANS)
	@ApiOperation(value = "Refreshes plan in database for XBSS_BENEF_PLAN_MV")
	@ResponseBody
	public void refreshPlans() {
		logger.info("In refreshPlans():: refreshing XBSS_BENEF_PLAN_MV");
		refreshService.refreshPlanView();
		logger.info("In refreshPlans():: refreshed XBSS_BENEF_PLAN_MV");
	}

	@GetMapping(value = URIConstants.REFRESH_SESSION)
	@ApiOperation(value = "Session is refreshed", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Session refreshed successfully") })
	@ResponseBody
	public boolean refreshSession() {
		logger.info("SESSION HAS BEEN REFRESHED");
		return Boolean.TRUE;
	}

	/**
	 * Returns a key, value pair of all RealmPlanYear Rules and Configurations for
	 * given companyCode
	 * 
	 * @param request
	 * @param companyCode
	 * @return Map<String, String>
	 * @throws JsonProcessingException
	 */
	@GetMapping(value = URIConstants.GET_RULES_AND_CONFIGS)
	@ApiOperation(value = "Returns rules and configerations of API-BSS", response = String.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Rules and configerations retrived successfully") })
	@ResponseBody
	public Map<String, String> getRulesAndConfigs(HttpServletRequest request,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		Map<String, String> ruleConfigs = new HashMap<>();
		boolean isHistory = false;
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		if (null != company) {
			ruleConfigs = ruleConfigService.getRulesAndConfigsByRealmPlanYearId( company );
			ruleConfigs.putAll(ruleConfigService.getPsConfigsByDate(CommonUtils.formatStringToDate(company.getPlanStartDate(),
					BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY)));
			ruleConfigs.putAll(appRulesConfigService.getAllRulesAndConfigs());
		}
		return ruleConfigs;
	}
	
	@GetMapping(value = URIConstants.FEATURE_FLAGS)
	@ApiOperation(value = "Returns feature flags for A/B Testing for API-BSS", response = String.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Feature Flags retrived successfully") })
	@ResponseBody
	public List<FeatureFlag> getFeatureFlags(HttpServletRequest request,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		return featureFlagService.retrieveFeatureFlags(companyCode, company.getRealmPlanYear().getId());
	}
}
