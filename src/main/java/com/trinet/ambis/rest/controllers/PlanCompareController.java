package com.trinet.ambis.rest.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.RequestResponseConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.PlanCompareFacade;
import com.trinet.ambis.service.PlanCompareService;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.outputs.PlanComparisonService;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.FileUtils;
import com.trinet.ambis.validator.StrategyIdValidator;
import com.trinet.domain.common.ReturnResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "PlanCompareController")
public class PlanCompareController {
	
	private static final Logger logger = LoggerFactory.getLogger(PlanCompareController.class);

	@Autowired
	private PlanCompareFacade planCompareFacade;

	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private PlanCompareService planCompareService;

	@Autowired
	private PlanComparisonService planComparisonService;
	
	@GetMapping(value = URIConstants.COMPANY_PLAN_COMPARE_EXPORT)
	@ApiOperation(value = "Generate the company level plan compare report for enrollered plans in the given strategies")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Company level plan compare report for enrollered plans in given strategy generated.") })
	@ResponseBody
	public void generateEnrolledPlanCompareReport(HttpServletResponse response,
			HttpServletRequest httpRequest,
			@PathVariable("companyCode") final String companyCode,
			@StrategyIdValidator @PathVariable("futureStrategyIds") final String futureStrategyIds,@RequestParam(required = false, defaultValue = "") String exchangeId ) throws IOException {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));

		List<Long> strategyIdsLong = Stream.of(futureStrategyIds.split(",")).map(Long::valueOf)
				.collect(Collectors.toList());
		Workbook workbook = planCompareFacade.generateEnrolledPlanCompareReport(company, strategyIdsLong, httpRequest);

		String companyName = FileUtils.removeSpecialCharacters(company.getDescription());
		String fileName = StringUtils.join(Arrays.asList(companyName, "ComparePlanAttributes.xlsx"), "_");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		workbook.write(response.getOutputStream());
	}
	
	/**
	 * Gets list of current year plan plans for given quarter and code
	 * 
	 * @param request
	 * @param quarterName
	 * @param code
	 * @return
	 */
	@GetMapping(value = URIConstants.CURRENT_YEAR_PLANS)
	@ApiOperation(value = "Gets list of current year plan plans for given quarter and code", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Gets company list of current plan year plans") })
	@ResponseBody
	public ReturnResponse<List<BenefitPlanDetailDto>> companyCurrentYearPlans(HttpServletRequest request,@PathVariable("companyCode") String code) {
		logger.info("getting current year plans for the company code {} ", code);
		
		List<BenefitPlanDetailDto> companyCurrentYearPlans = planCompareService.findSubmittedStrategyPlansBy(code);
		
		ReturnResponse<List<BenefitPlanDetailDto>> response = new ReturnResponse<>();
		if(CollectionUtils.isEmpty(companyCurrentYearPlans)) {
			response.setStatusCode(String.valueOf(BSSHttpStatusConstants.NOT_FOUND));
			response.setStatusMessage(RequestResponseConstants.SUCCESS);
			response.setStatusText(RequestResponseConstants.NO_PLANS_MESSAGE);
			return response;
		}
		response.setData(companyCurrentYearPlans);
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setStatusMessage(RequestResponseConstants.SUCCESS);
		response.setStatusText(HttpStatus.OK.name());
		return response;
	}
	
	/**
	 * Get list of mapped plans for given company code and quarter
	 * 
	 * @param quarterName
	 * @param code
	 * 
	 * @return List<MappedPlanDetailDto>
	 */
	@GetMapping(value = URIConstants.MAPPING_PLANS)
	@ApiOperation(value = "Get list of mapped plans for given company code and quarter", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Get list of mapped plans for given company code and quarter") })
	@ResponseBody
	public ReturnResponse<List<MappedPlanDetailDto>> mappingPlans(HttpServletRequest request, @PathVariable("companyCode") String code) {
		logger.info("getting mapping plans for the company code {} ", code);
		List<MappedPlanDetailDto> companyCurrentYearPlans = planCompareService.findMappingBenefitPlansBy(code);
		
		ReturnResponse<List<MappedPlanDetailDto>> response = new ReturnResponse<>();
		if(CollectionUtils.isEmpty(companyCurrentYearPlans)) {
			response.setStatusCode(String.valueOf(BSSHttpStatusConstants.NOT_FOUND));
			response.setStatusMessage(RequestResponseConstants.SUCCESS);
			response.setStatusText(RequestResponseConstants.NO_PLANS_MESSAGE);
			return response;
		}
		response.setData(companyCurrentYearPlans);
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setStatusMessage(RequestResponseConstants.SUCCESS);
		response.setStatusText(HttpStatus.OK.name());
		return response;
	}
	
	/**
	 * Get list of all plans for given company code and quarter
	 * 
	 * @param quarterName
	 * @param code
	 * 
	 * @return List<MappedPlanDetailDto>
	 */
	@GetMapping(value = URIConstants.FUTURE_PLANS)
	@ApiOperation(value = "Get list of all plans for given company code", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Get list of all plans for given company code") })
	@ResponseBody
	public ReturnResponse<List<BenefitPlanDetailDto>> getFutureYearPlans(HttpServletRequest request, @PathVariable("companyCode") String code) {
		logger.info("getting all plans for the company code {} ", code);
		List<BenefitPlanDetailDto> allPlans = planCompareService.findAllFutureYearPlansBy(code);
		
		ReturnResponse<List<BenefitPlanDetailDto>> response = new ReturnResponse<>();
		if(CollectionUtils.isEmpty(allPlans)) {
			response.setStatusCode(String.valueOf(BSSHttpStatusConstants.NOT_FOUND));
			response.setStatusMessage(RequestResponseConstants.SUCCESS);
			response.setStatusText(RequestResponseConstants.NO_PLANS_MESSAGE);
			return response;
		}
		response.setData(allPlans);
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setStatusMessage(RequestResponseConstants.SUCCESS);
		response.setStatusText(BSSHttpStatusConstants.OK_HTTP_STATUS_NAME);
		return response;
	}
	
	/**
	 * Export list of plans 
	 * 
	 * @param comparePlans
	 * @param code
	 * 
	 */
	@PostMapping(value = URIConstants.PLAN_COMPARE_EXPORT)
	@ApiOperation(value = "Generate the excel report for current vs future plan attributes comparison", response = Boolean.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Generate the excel report for current vs future plan attributes comparison") })
	@ResponseBody
	public void generatePlanCompareReport(HttpServletResponse response, HttpServletRequest httpRequest,
			@PathVariable("companyCode") final String companyCode,
			@RequestBody Map<String, Map<String, LinkedHashSet<String>>> curFutPlansToCompare) throws IOException {
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), null);
		Map<String, Map<String, Set<String>>> plansToCompareByBenefitType = new LinkedHashMap<>();
		curFutPlansToCompare.forEach((benefitType, planMap) -> {
			Map<String, Set<String>> plansToCompare = new LinkedHashMap<>();
			planMap.forEach((planId, planSet) -> plansToCompare.put(planId, new LinkedHashSet<>(planSet)));
			plansToCompareByBenefitType.put(benefitType, plansToCompare);
		});
		Workbook workbook = planCompareFacade.generatePlanCompareReport(company, plansToCompareByBenefitType,
				httpRequest);
		String companyName = FileUtils.removeSpecialCharacters(company.getDescription());
		String fileName = StringUtils.join(Arrays.asList(companyName, "ComparePlanAttributes.xlsx"), "_");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		workbook.write(response.getOutputStream());
	}

	@GetMapping(value = URIConstants.PLAN_COMPARE)
	@ApiOperation(value = "Get list of current and trinet plans with attributes")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get list of current and trinet plans with attributes") })
	@ResponseBody
	public ReturnResponse<List<PlanCompareDetailDto>> getPlanCompareDetails(HttpServletRequest httpReqest,
			@PathVariable("trinetStrategyIds") List<Long> trinetStrategyIds,
			@PathVariable("companyCode") final String companyCode, @RequestParam() String exchangeId) {
		List<PlanCompareDetailDto> planCompareDetailDtos = planCompareFacade.getPlanCompareDetails(companyCode,
				exchangeId, trinetStrategyIds, httpReqest);
		ReturnResponse<List<PlanCompareDetailDto>> response = new ReturnResponse<>();
		if (CollectionUtils.isEmpty(planCompareDetailDtos)) {
			response.setStatusCode(String.valueOf(BSSHttpStatusConstants.NOT_FOUND));
			response.setStatusMessage(RequestResponseConstants.SUCCESS);
			response.setStatusText(RequestResponseConstants.NO_PLANS_MESSAGE);
			return response;
		}
		response.setData(planCompareDetailDtos);
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setStatusMessage(RequestResponseConstants.SUCCESS);
		response.setStatusText(BSSHttpStatusConstants.OK_HTTP_STATUS_NAME);
		return response;
	}
	
	@GetMapping(value = URIConstants.PLAN_COMPARE_ASSIGNMENT)
	@ApiOperation(value = "Get list of current and trinet plan with attributes for edit plan assignement")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Get list of current and trinet plan with attributes for edit plan assignement") })
	@ResponseBody
	public ReturnResponse<BasePlanComparison> getPlanCompareAssignmentDetails(HttpServletRequest httpReqest,
			@PathVariable("prospectPlanId") String prospectPlanId,@PathVariable("trinetPlanId") String trinetPlanId,
			@PathVariable("companyCode") String companyCode, @RequestParam String benefitType,
			@RequestParam (required = false) String exchangeId) {

		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));

		BasePlanComparison planCompareAssignementDtos = planComparisonService
				.getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpReqest, company);

		ReturnResponse<BasePlanComparison> response = new ReturnResponse<>();
		if (ObjectUtils.isEmpty(planCompareAssignementDtos)) {
			response.setStatusCode(String.valueOf(BSSHttpStatusConstants.NOT_FOUND));
			response.setStatusMessage(RequestResponseConstants.SUCCESS);
			response.setStatusText(RequestResponseConstants.NO_PLANS_MESSAGE);
			return response;
		}
		response.setData(planCompareAssignementDtos);
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setStatusMessage(RequestResponseConstants.SUCCESS);
		response.setStatusText(BSSHttpStatusConstants.OK_HTTP_STATUS_NAME);
		return response;
	}

}
