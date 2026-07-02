/**
 * 
 */
package com.trinet.ambis.rest.controllers;

import java.util.ArrayList;
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
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanHeadCountService;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;
import com.trinet.ambis.service.model.PlanHeadCount;
import com.trinet.ambis.service.prospect.ProspectPlanHeadCountService;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.validator.StrategyIdValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author kpamulapati
 */
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Head Count Controller")
public class HeadCountController {

	@Autowired
	PlanHeadCountService planHeadCountService;
	@Autowired
	ProspectPlanHeadCountService prospectPlanHeadCountService;
	@Autowired
	CompanyService companyService;
	@Autowired
	PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	@Autowired
	StrategyGroupDataDao strategyGroupDataDao;

	@GetMapping(value = URIConstants.GET_PLANS_HEADCOUNT)
	@ApiOperation(value = "Gets Plan details", response = PlanHeadCount.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan deatils retrived successfully") })
	@ResponseBody
	public List<PlanHeadCount> getPlan(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") Long strategyId,
			@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		boolean isHistory = false;
		List<PlanHeadCount> planHeadcountList = new ArrayList<>();
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		planHeadcountList = planHeadCountService.getPlanHeadCount(company, strategyId);
		return planHeadcountList;

	}

	@GetMapping(value = URIConstants.GET_PLAN_HEADCOUNT_MAPPING)
	@ApiOperation(value = "Gets Plan head count details", response = BenefitProgramHeadCountPlans.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Plan head count deatils retrived successfully") })
	@ResponseBody
	public List<BenefitProgramHeadCountPlans> getPlanheadCount(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") Long strategyId,
			@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		boolean isHistory = false;
		List<BenefitProgramHeadCountPlans> planHeadcountList = null;
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		if (company.isProspectCompany() || company.isProspectConvertedOnboardingClient()) {
			planHeadcountList = prospectPlanHeadCountService.getBenefitProgramHeadCountPlans(company, strategyId);
		} else {
			planHeadcountList = planHeadCountService.getBenefitProgramHeadCountPlans(company, strategyId);
		}
		return planHeadcountList;
	}
}
