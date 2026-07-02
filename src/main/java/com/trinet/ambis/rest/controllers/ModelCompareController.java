package com.trinet.ambis.rest.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseMessages;
import com.trinet.ambis.helper.ModelCompareServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmployeeDataService;
import com.trinet.ambis.service.ModelCompareService;
import com.trinet.ambis.service.model.EmployeeStrategyData;
import com.trinet.ambis.service.model.ModelCompareGroupHeadcount;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.ModelCompareStrategyCost;
import com.trinet.ambis.service.model.StrategyBenefitPlanHeadCount;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.FileUtils;
import com.trinet.ambis.validator.StrategyIdValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author tallam
 */

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Model Compare Controller")
public class ModelCompareController {

	private static final Logger logger = LoggerFactory.getLogger(ModelCompareController.class);

	@Autowired
	CompanyService companyService;

	@Autowired
	ModelCompareService mcService;

	@Autowired
	EmployeeDataService employeeDataService;

	private static final boolean IS_HISTORY = false;

	@GetMapping(value = URIConstants.GET_MC_STRATEGIES)
	@ApiOperation(value = "Gets all strategies details", response = ModelCompareStrategy.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy data deatils retrived successfully") })
	@ResponseBody
	public List<ModelCompareStrategy> getStrategies(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In modelcompare Rest End point: getStrategies for companyCode : {} ", companyCode);
		long startTime = System.currentTimeMillis();
		Company company = null;
		List<ModelCompareStrategy> strategies = null;
		String emplid = BSSApplicationConstants.EMPLID_SYSTEM_ACCOUNT;
		if(!BSSSecurityUtils.checkSystemAccount())	
		  emplid = BSSSecurityUtils.getAuthenticatedPersonId();
		
		try {
			company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
					emplid, BenExchngEnums.getByExchangeId( exchangeId ));
			strategies = mcService.getMCStrategies(company, false);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX + BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, e);
		}
		long endTime = System.currentTimeMillis();
		logger.info("getStrategies() took {} ms", (endTime - startTime));
		return strategies;
	}

	@GetMapping(value = URIConstants.GET_MC_STRATEGY_PLAN_COSTS)
	@ApiOperation(value = "Gets Strategy plan costs details", response = ModelCompareStrategyCost.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy plan cost details retrived successfully") })
	@ResponseBody
	public List<ModelCompareStrategyCost> getStrategyPlanCosts(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyIds") List<Long> strategyIdList,
			@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In modelcompare Rest End point: getStrategyPlanCosts for strategyIdList : {}", strategyIdList);
		long startTime = System.currentTimeMillis();
		List<ModelCompareStrategyCost> returnList = null;
		Company company = null;
		try {
			company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
					BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
			returnList = mcService.getMCSelectedStrategyCosts(strategyIdList, company);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX + BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, e);
		}
		long endTime = System.currentTimeMillis();
		logger.info("getStrategyPlanCosts() took {} ms", (endTime - startTime));
		return returnList;
	}

	@GetMapping(value = URIConstants.GET_MC_STRATEGY_GROUP_FUNDING)
	@ApiOperation(value = "Gets strategy group funding detail", response = ModelCompareStrategy.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy group funding details retrived successfull") })
	@ResponseBody
	public ModelCompareStrategy getStrategyGroupFunding(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") final long strategyId,
			@PathVariable("companyCode") String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In modelcompare Rest End point: getStrategyGroupFunding for strategyId : {}", strategyId);
		long startTime = System.currentTimeMillis();
		ModelCompareStrategy mcs = null;
		Company company = null;
		try {
			company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
					BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
			mcs = mcService.getMCStrategyGroupFunding(strategyId, company);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX + BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, e);
		}
		long endTime = System.currentTimeMillis();
		logger.info("getStrategyGroupFunding() took {} ms", (endTime - startTime));
		return mcs;
	}

	@GetMapping(value = URIConstants.GET_MC_STRATEGY_EMPLOYEE_COST)
	@ApiOperation(value = "Gets strategies for employee cost data detail", response = EmployeeStrategyData.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Strategies for employee cost data retrived successfully") })
	@ResponseBody
	public List<EmployeeStrategyData> getStrategiesEmployeeCostData(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyIds") final String strategyListStr,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In modelcompare Rest End point: getStrategiesEmployeeCostData");
		long startTime = System.currentTimeMillis();
		List<EmployeeStrategyData> employeeStrategyPlanData = null;
		Long currentStrategyId = null;
		List<Long> strategyList = null;
		Company company = null;
		String customErrorMessage = null;
		try {
			customErrorMessage = BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR;
			Map<Long, List<Long>> strategyListMap = ModelCompareServiceHelper.splitStrategyList(strategyListStr);
			if (!strategyListMap.isEmpty()) {
				currentStrategyId = strategyListMap.keySet().iterator().next();
				strategyList = strategyListMap.get(currentStrategyId);
			}
			company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
					BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
			customErrorMessage = BSSErrorResponseMessages.MSG_MC_EMPLOYEE_ERROR;
			employeeStrategyPlanData = employeeDataService.getEmployeeStrategiesPlanCostData(company, currentStrategyId,
					strategyList);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX + customErrorMessage, e);
		}
		long endTime = System.currentTimeMillis();
		logger.info("getStrategiesEmployeeCostData() took {} ms", (endTime - startTime));
		return employeeStrategyPlanData;
	}

	@GetMapping(value = URIConstants.GET_MC_STRATEGY_BENPLAN_HEADCOUNTS)
	@ApiOperation(value = "Gets Plan strategy headcount deatils", response = StrategyBenefitPlanHeadCount.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Plan strategy headcount details retrived successfully") })
	@ResponseBody
	public List<StrategyBenefitPlanHeadCount> getPlanStrategyHeadcount(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyIds") List<Long> strategyIds,
			@PathVariable("companyCode") String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In modelcompare Rest End point: getPlanStrategyHeadcount for strategyIdList : {}", strategyIds);
		long startTime = System.currentTimeMillis();
		List<StrategyBenefitPlanHeadCount> returnList = null;
		try {
			Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
					BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
			returnList = mcService.getMCPlanStrategyCoverageHeadcount(strategyIds, company);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX + BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, e);
		}
		long endTime = System.currentTimeMillis();
		logger.info("getPlanStrategyHeadcount() took {} ms", (endTime - startTime));
		return returnList;
	}

	@GetMapping(value = URIConstants.GET_MC_STRATEGY_GROUP_HEADCOUNT)
	@ApiOperation(value = "Gets strategies group headcount cost data", response = ModelCompareGroupHeadcount.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Strategies group headcount cost data retrived successfully") })
	@ResponseBody
	public List<ModelCompareGroupHeadcount> getStrategiesGroupHeadcountCostData(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyIds") List<Long> strategyIds,
			@PathVariable("companyCode") String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In modelcompare Rest End point: getStrategiesGroupHeadcountCostData");
		long startTime = System.currentTimeMillis();
		List<ModelCompareGroupHeadcount> mcGroupHeadcountList = null;
		try {
			Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
					BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
			mcGroupHeadcountList = mcService.getMCStrategyHeadcountCostByGroup(strategyIds, company);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX + BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, e);
		}
		long endTime = System.currentTimeMillis();
		logger.info("getStrategiesGroupHeadcountCostData() took {} ms", (endTime - startTime));
		return mcGroupHeadcountList;
	}

	@GetMapping(value = URIConstants.GET_MC_DATA_EXPORT)
	@ApiOperation(value = "Gets strategies model compare in excel file")
	@ResponseBody
	public void getModelCompareExcelFile(HttpServletRequest request, HttpServletResponse response,
			@StrategyIdValidator @PathVariable("strategyIds") final String strategyIds,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) throws IOException {
		logger.info("In modelcompare Rest End point: getStrategiesEmployeeCostDataExport");
		long startTime = System.currentTimeMillis();
		Long currentStrategyId = null;
		List<Long> strategyList = null;
		try {
			Map<Long, List<Long>> strategyListMap = ModelCompareServiceHelper.splitStrategyList(strategyIds);
			if (!strategyListMap.isEmpty()) {
				currentStrategyId = strategyListMap.keySet().iterator().next();
				strategyList = strategyListMap.get(currentStrategyId);
			}

			Company company = companyService.getCompanyDetails(companyCode, IS_HISTORY,
					BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));

			Workbook workbook = mcService.getModelCompareExcelWorkbook(company, currentStrategyId, strategyList);
			
			String companyDescription = FileUtils.removeSpecialCharacters(company.getDescription());
			response.setHeader("Content-disposition",
					"attachment; filename=" + companyDescription + "_Strategy_Compare.xlsx");
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			workbook.write(response.getOutputStream());
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(
					BSSErrorResponseMessages.EXCEPTION_MSG_PREFIX + BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, e);
		}
		long endTime = System.currentTimeMillis();
		logger.info("getStrategiesEmployeeCostDataExport() took {} ms", (endTime - startTime));
	}

}
