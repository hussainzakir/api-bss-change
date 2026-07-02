package com.trinet.ambis.rest.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.persistence.dao.hrp.QuarterAndPlanYearDto;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PreLoadStrategiesStatusDto;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ExceptionAttributeService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.SchedTblService;
import com.trinet.ambis.service.SearchCompanyService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.model.ExceptionAttributeDto;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.service.model.SchedMidYearFundingDto;
import com.trinet.ambis.service.model.SchedTblAdminDto;
import com.trinet.ambis.service.model.SchedTblDto;
import com.trinet.ambis.service.model.SearchCompanyResultData;
import com.trinet.ambis.util.BSSSecurityUtils;

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
@Api(value = "Trinet API-BSS Admin Controller")
public class AdminController {

	@Autowired
	private SchedTblService schedTblService;
	@Autowired
	private RealmDataDao realmDataDao;
	@Autowired
	private CompanyService companyService;
	@Autowired
	private SearchCompanyService searchCompanyService;
	@Autowired
	RealmPlanYearDao realmPlanYearDao;
	@Autowired
	private StrategyService strategyService;
	@Autowired
	ProcessStatusService processStatusService;
	@Autowired
	ExceptionAttributeService exceptionAttributeService;
	
	@Autowired
	RealmPlanYearService realmPlanYearService;

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	/**
	 * @param request
	 * @param companyCode
	 * @param peoId
	 * @param quarter
	 * @return
	 */
	@GetMapping(value = URIConstants.GET_SCHEDULE_DATES)
	@ApiOperation(value = "Gets schedule dates details", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "ScheduleDates details retrieved successfully") })
	@ResponseBody
	public List<SchedTblAdminDto> getScheduleDates(HttpServletRequest request, @PathVariable("companyCode") String companyCode,
			 @PathVariable("oeQuarter") String quarter) {
		
		return schedTblService.getSchedTblAdminDates(companyCode, quarter);
	}

	/**
	 * @param request
	 * @param schedTblDto
	 * @param companyCode
	 * @return
	 */
	@PostMapping(value = URIConstants.SCHEDULE_DATES)
	@ApiOperation(value = "Create schedule dates information", response = SchedTbl.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "ScheduleDates details retrieved successfully") })
	@ResponseBody

	public SchedTbl createScheduleDates(HttpServletRequest request, @RequestBody final SchedTblDto schedTblDto,
			@PathVariable("companyCode") String companyCode) {
		schedTblService.validateRequest(schedTblDto);
		return schedTblService.createUpdateScheduleDates(request, schedTblDto, BSSSecurityUtils.getAuthenticatedPersonId());
	}

	/**
	 * @param request
	 * @param schedTblDto
	 * @param companyCode
	 * @return
	 */
	@PutMapping(value = URIConstants.SCHEDULE_DATES)
	@ApiOperation(value = "Update schedule dates information", response = SchedTbl.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Update ScheduleDates details") })
	@ResponseBody
	public SchedTbl updateScheduleDates(HttpServletRequest request, @RequestBody final SchedTblDto schedTblDto,
			@PathVariable("companyCode") String companyCode) {
		return schedTblService.createUpdateScheduleDates(request, schedTblDto, BSSSecurityUtils.getAuthenticatedPersonId());

	}

	/**
	 * This method is for getting products and corresponding quarters.
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping(value = URIConstants.GET_PRODUCTS_QUARTERS)
	@ApiOperation(value = "Gets product information", response = ProductQuarters.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Product details retrieved successfully") })
	@ResponseBody
	public List<ProductQuarters> getproducts(HttpServletRequest request) {
		List<ProductQuarters> pqs = new ArrayList<>();
		Map<String, ProductQuarters> productQuartersMap = realmDataDao.getAllProductQuarters();
		pqs.addAll(productQuartersMap.values());
		return pqs;
	}

	/**
	 * @param request
	 * @param search_param
	 * @return
	 * @throws JsonProcessingException
	 */
	@GetMapping(value = URIConstants.SEARCH_COMPANY)
	@ApiOperation(value = "Gets Search results", response = SearchCompanyResultData.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Search results retrieved successfull") })
	@ResponseBody
	public List<SearchCompanyResultData> getSearchResults(HttpServletRequest request,
			@PathVariable("searchParam") String searchParam, @PathVariable("companyCode") String companyCode) {
		List<SearchCompanyResultData> searchResults = null;
		searchResults = searchCompanyService.getSearchResults(searchParam, companyCode, BSSSecurityUtils.getAuthenticatedPersonId());
		logger.info("Employee id of the seraching user {}", BSSSecurityUtils.getAuthenticatedPersonId());
		return searchResults;
	}

	/**
	 * @param request
	 * @param companyCode
	 * @param schedMidYearFundingDto
	 * @return
	 */
	@PostMapping(value = URIConstants.MID_YEAR_FUNDING_DETAILS)
	@ApiOperation(value = "Create MidYear Funding details", response = SchedMidYearFundingDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "MidYear Funding details created successfull") })
	@ResponseBody
	public List<SchedMidYearFundingDto> createMidYearFundingDetails(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode,
			@RequestBody final SchedMidYearFundingDto schedMidYearFundingDto) {
		List<SchedMidYearFundingDto> scheduleList = null;
		schedMidYearFundingDto.setLastUpdatedBy(BSSSecurityUtils.getAuthenticatedPersonId());
		Company company = companyService.getCompanyDetails(companyCode);
		scheduleList = schedTblService.createUpdateMidYearDetails(schedMidYearFundingDto, company, false);

		return scheduleList;
	}

	/**
	 * @param request
	 * @param companyCode
	 * @param schedMidYearFundingDto
	 * 
	 * @return
	 */
	@PutMapping(value = URIConstants.MID_YEAR_FUNDING_DETAILS)
	@ApiOperation(value = "update MidYear Funding details", response = SchedMidYearFundingDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "MidYear Funding details updated successfully") })
	@ResponseBody
	public List<SchedMidYearFundingDto> updateMidYearFundingDetails(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode,
			@RequestBody final SchedMidYearFundingDto schedMidYearFundingDto) {
		List<SchedMidYearFundingDto> scheduleList = null;
		schedMidYearFundingDto.setLastUpdatedBy(BSSSecurityUtils.getAuthenticatedPersonId());
		Company company = companyService.getCompanyDetails(companyCode);
		scheduleList = schedTblService.createUpdateMidYearDetails(schedMidYearFundingDto, company, true);
		return scheduleList;
	}

	/**
	 * @param request
	 * @param companyCode
	 * @return
	 */
	@GetMapping(value = URIConstants.MID_YEAR_FUNDING_DETAILS)
	@ApiOperation(value = "Gets MidYear Funding details", response = SchedMidYearFundingDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "MidYear Funding details retrieved successfully") })
	@ResponseBody
	public List<SchedMidYearFundingDto> getMidYearFundingDetails(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode) {
		List<SchedMidYearFundingDto> scheduleList = null;
		scheduleList = schedTblService.getMidYearDetails(companyCode);
		return scheduleList;
	}

	/**
	 * Trigger an ad-hoc instance of the daily census refresh for one company for a given year.
	 * @param request
	 * @param realmYearId
	 * @param companyCode
	 */
	@PutMapping(value = URIConstants.REFRESH_COMPANY_MID_YEAR)
	@ApiOperation(value = "Trigger mid-year census refresh")
	@ResponseBody
	public String refreshCompanyCensus(HttpServletRequest request,
			@PathVariable("realmYearId") Long realmYearId,
			@PathVariable("companyCode") String companyCode) {
		companyService.refreshCompanyCensus( companyCode, realmYearId );
		return "Done: " + (new java.util.Date()).toString();
	}

	/**
	 * @param request
	 * @param peoId
	 * @param quarter
	 */
	@PostMapping(value = URIConstants.PRE_LOAD_STRATEGIES)
	@ApiOperation(value = "Copies pre loaded strategies with respect to quater(default strategy)")
	@ResponseBody
	public void preLoadStrategies(HttpServletRequest request, @PathVariable("peoId") String peoId,
			@PathVariable("quarter") String quarter) {
		boolean processStatusFlag = processStatusService.isPreLoadProcessed(quarter);
		if (processStatusFlag) {
			strategyService.preLoadBssStrategies(peoId, quarter, BSSSecurityUtils.getAuthenticatedPersonId());
		}
	}


	/**
	 * 
	 * @param request
	 */
	@PostMapping(value = URIConstants.PRE_LOAD_COMPANIES_STRATEGIES)
	@ApiOperation(value = "Copies pre loaded company strategies for all companies")
	@ResponseBody
	public void preLoadCompaniesStrategies(HttpServletRequest request, @RequestBody List<String> companies) {
		for (String companyCode : companies) {
			boolean isHistory = false;
			boolean processStatusFlag = processStatusService.isStrategySummariesProcessed(companyCode);
			if (processStatusFlag) {
				Company company = companyService.getCompanyDetails(companyCode, isHistory,
						BSSSecurityUtils.getAuthenticatedPersonId(), null);
				strategyService.preLoadBssStrategies(company);
			}
		}
	}
	
	@GetMapping(value = URIConstants.GET_EXCEPTION_ATTRIBUTES)
	@ApiOperation(value = "Gets Exception Attributes details", response = ExceptionAttributeDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Exception Attributes details retrieved successfully") })
	@ResponseBody
	public List<ExceptionAttributeDto> getExceptionAttributes(HttpServletRequest request) {
		return exceptionAttributeService.findAllExceptionAttributes();
	}
	
	@GetMapping(value = URIConstants.GET_OEQUARTERS_AND_PLANYEARS)
	@ApiOperation(value = "Gets OeQuarters And Plan Years details", response = QuarterAndPlanYearDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OeQuarters And Plan Years details retrieved successfully") })
	@ResponseBody
	public List<QuarterAndPlanYearDto> getListOfOeQuartersAndPlanYears(HttpServletRequest request) {
		return realmPlanYearService.getOeQuartersAndPlanYearsInfo();
	}
	
	@GetMapping(value = URIConstants.PRE_LOAD_STRATEGIES_STATUS)
	@ApiOperation(value = "Gets PRE_LOAD_STRATEGIES_STATUS Years details", response = QuarterAndPlanYearDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "PRE_LOAD_STRATEGIES_STATUS And Plan Years details retrieved successfully") })
	@ResponseBody
	public List<PreLoadStrategiesStatusDto> getPreLoadStrategiesStatus(HttpServletRequest request) {
		return processStatusService.getPreLoadStrategiesStatuses();
	}
	
}
