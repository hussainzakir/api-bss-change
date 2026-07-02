package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.trinet.ambis.aop.BSSEvictCache;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.rest.controllers.dto.StrategyCostRes;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.rest.controllers.dto.CreateStrategiesRequest;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.model.StrategyBudget;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.prospect.ProspectSubmitService;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.validator.RequestValidator;
import com.trinet.ambis.validator.StrategyIdValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Strategy Controller ")
@Validated
public class StrategyController {

	private static final Logger logger = LoggerFactory.getLogger(StrategyController.class);

	@Autowired
	private StrategyService strategyService;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private SubmitStatusService submitStatusService;

	@Autowired
	private ProcessStatusService processStatusService;

	@Autowired
	private QueuedSubmitService queuedSubmitService;

	@Autowired
	private SubmitService submitService;

	@Autowired
	private ProspectSubmitService prospectSubmitService;

	private static final boolean RESUBMIT = false;
	private static final boolean SEND_CLIENT_EMAIL = true;

	public StrategyController() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@PostMapping(value = URIConstants.CREATE_STRATEGY)
	@ApiOperation(value = "Create new strategy ", response = StrategyData.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Created new strategy successfully") })
	@ResponseBody
	public StrategyData createStrategy(HttpServletRequest request, @RequestBody final StrategyData dto,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		long startTime = System.currentTimeMillis();
		logger.info("In createStrategy() {}", companyCode);
		boolean isHistory = false;
		dto.getStrategySummary().setName(RequestValidator.getValidatedStrategyName(dto.getStrategySummary().getName()));
		StrategyData strategy;
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
		long strategyId = strategyService.createUpdateStrategy(dto, company, false);
		logger.info("COMPANY HEAD COUNT : {}", company.getHeadcount());
		strategy = strategyService.getStrategyById(company, strategyId, false);
		long endTime = System.currentTimeMillis();
		logger.info("createStrategy() took {} ms", (endTime - startTime));
		return strategy;
	}

	@PostMapping(value = URIConstants.STRATEGY_SUMMARY)
	@ApiOperation(value = "Create new custom strategy ", response = StrategyData.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Created new custom strategy successfully") })
	@ResponseBody
	public StrategyData createCustomStrategy(HttpServletRequest request, @RequestBody final StrategyData strategyData,
			@StrategyIdValidator @PathVariable("strategyId") final long id,
			@PathVariable("companyCode") final String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		long startTime = System.currentTimeMillis();
		logger.info("In createCustomStrategy() {}", companyCode);
		boolean isHistory = false;
		strategyData.getStrategySummary()
				.setName(RequestValidator.getValidatedStrategyName(strategyData.getStrategySummary().getName()));
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));

		long strategyId = strategyService.createUpdateStrategy(strategyData, company, false);
		StrategyData createdStrategy = strategyService.getStrategyById(company, strategyId, false);
		if (createdStrategy == null) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED,
					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyController.class.getName(), "Unable to save Strategy",
					null, null));
		} else if (strategyData.getStrategySummary().isSubmitted()) {
			performSubmit(request, exchangeId, company, strategyId, createdStrategy);
		}
		long endTime = System.currentTimeMillis();
		logger.info("createCustomStrategy() took {} ms", (endTime - startTime));
		return createdStrategy;
	}

	@PutMapping(value = URIConstants.STRATEGY_SUMMARY)
	@ApiOperation(value = "Update strategy details", response = StrategyData.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "updated strategy successfully") })
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public StrategyData updateStrategy(HttpServletRequest request, @RequestBody final StrategyData dto,
			@StrategyIdValidator @PathVariable("strategyId") @CacheKey final long id,
			@PathVariable("companyCode") final String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In updateStrategy() {}", companyCode);
		boolean isHistory = false;
		dto.getStrategySummary().setName(RequestValidator.getValidatedStrategyName(dto.getStrategySummary().getName()));
		StrategyData updatedStrategy;
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));

		logger.info("############ START UPDATING STARTEGY ##################");
		long t1 = System.currentTimeMillis();
		long strategyId = strategyService.createUpdateStrategy(dto, company, true);
		long t2 = System.currentTimeMillis();
		logger.info("******* UPDATED STARTEGY ********** {}", (t2 - t1));

		updatedStrategy = strategyService.getStrategyById(company, strategyId, false);
		long t3 = System.currentTimeMillis();
		logger.info("******* GOT STRATEGY ********** {}", (t3 - t2));
		if (updatedStrategy == null) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_STRATEGY_SAVE_FAILED,
					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyController.class.getName(),
					"Unable to update Strategy", null, null));
		} else if (dto.getStrategySummary().isSubmitted()) {
			performSubmit(request, exchangeId, company, strategyId, updatedStrategy);
		}
		return updatedStrategy;
	}

	@PutMapping(value = URIConstants.STRATEGY_NAME)
	@ApiOperation(value = "Update strategy name", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "updated strategy name successfully") })
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public void updateStrategyName(HttpServletRequest request, @StrategyIdValidator @PathVariable("strategyId")
			@CacheKey final long strategyId, @RequestBody final String strategyName) {
		logger.info("Entering update strategy name Rest API, strategyId = {}", strategyId);
		long startTime = System.currentTimeMillis();
		strategyService.updateStrategyName(strategyId, RequestValidator.getValidatedStrategyName(strategyName));
		long endTime = System.currentTimeMillis();
		logger.info("update strategy name Rest API took {} ms", (endTime - startTime));
	}

	@GetMapping(value = URIConstants.GET_STRATEGY_SUMMARIES)
	@ApiOperation(value = "Gets the strategy data information ", response = StrategyData.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retrived strategy data successfully") })
	@ResponseBody
	public List<StrategyData> getStrategyData(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId,
			@RequestParam(required = false, defaultValue = "0") String carrierId) {
		long startTime = System.currentTimeMillis();
		logger.info("In getStrategyData()");
		boolean isHistory = false;
		boolean isDefaultSubmit = false;
		boolean isPreload = false;
		List<StrategyData> strategies;
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
		if (company.isRenewalCompany() || company.isProspectCompany()) {
			boolean processStatusFlag = processStatusService.isStrategySummariesProcessed(company.getCode());
			if (processStatusFlag) {
				if (!company.isProspectCompany()) {
					strategyService.createFutureStrategies(company, isDefaultSubmit, isPreload);
				} else {
					strategyService.createProspectsTrinetStrategy(company, Long.parseLong(carrierId), null);
				}
			} else {
				int i = 0;
				while (!processStatusFlag && i < 30) {
					try {
						i++;
						Thread.sleep(5000);
						processStatusFlag = processStatusService.isStrategySummariesProcessed(company.getCode());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
		strategies = strategyService.getStrategies(company, isHistory, null);
		long endTime = System.currentTimeMillis();
		logger.info("getStrategyDetail() took {} ms", (endTime - startTime));

		return strategies;
	}
	
	@GetMapping(value = URIConstants.CREATE_STRATEGY_SUMMARIES_ONBOARDING)
	@ApiOperation(value = "Gets the strategy data information ", response = StrategyData.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retrived strategy data successfully") })
	@ResponseBody
	public List<StrategyData> createDefaultStrategyOnBoardingClients(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId,
			@RequestParam(required = false, defaultValue = "0") String carrierId) {
			final int MAX_RETRIES = 30;
			final int SLEEP_MILLIS = 5000;
			long startTime = System.currentTimeMillis();
			logger.info("In createDefaultStrategyOnBoardingClients() for companyCode: {}", companyCode);
			boolean isHistory = false;
			Company company = companyService.getCompanyDetails(companyCode, isHistory,
					BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId(exchangeId));
			if (company.isProspectConvertedOnboardingClient()) {
				boolean processStatusFlag = processStatusService.isStrategySummariesProcessed(company.getCode());
				int retries = 0;
				while (!processStatusFlag && retries < MAX_RETRIES) {
					try {
						Thread.sleep(SLEEP_MILLIS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					processStatusFlag = processStatusService.isStrategySummariesProcessed(company.getCode());
					retries++;
				}
				if (processStatusFlag) {
					strategyService.createProspectsTrinetStrategy(company, Long.parseLong(carrierId), null);
				}
			}
			List<StrategyData> strategies = strategyService.getStrategies(company, isHistory, null);
			long endTime = System.currentTimeMillis();
			logger.info("createDefaultStrategyOnBoardingClients() took {} ms", (endTime - startTime));
			return strategies;
		}

	@GetMapping(value = URIConstants.GET_STRATEGY_SUMMARY_HISTORY)
	@ApiOperation(value = "Gets the historical strategy data details", response = StrategyData.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Historical strategy data retrived successfully") })
	@ResponseBody
	public List<StrategyData> getStrategyDataHistory(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode) {
		long startTime = System.currentTimeMillis();
		logger.info("In getStrategyDataHistory()");
		boolean history = true;
		List<StrategyData> strategies;
		Company company = companyService.getCompanyDetails(companyCode, true,
				BSSSecurityUtils.getAuthenticatedPersonId(), null);
		strategies = strategyService.getStrategies(company, history, null);
		long endTime = System.currentTimeMillis();
		logger.info("getStrategyDataHistory() took {} ms", (endTime - startTime));
		return strategies;
	}

	@GetMapping(value = URIConstants.UPDATE_STRATEGY_SUMMARY_HISTORY)
	@ApiOperation(value = "Updates the strategy data histrory", response = StrategyData.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "updated strategy data history successfully") })
	@ResponseBody
	public List<StrategyData> updateStrategyDataHistory(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode) {
		long startTime = System.currentTimeMillis();
		logger.info("In getStrategyDataHistory()");
		boolean history = true;
		List<StrategyData> strategies;
		try {
			Company company = companyService.getCompanyDetails(companyCode, true,
					BSSSecurityUtils.getAuthenticatedPersonId(), null);
			strategies = strategyService.updateStrategieHistory(company, history);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyController.class.getName(),
							"Strategy Exception", null, null));
		}
		long endTime = System.currentTimeMillis();
		logger.info("getStrategyDataHistory() took {} ms", (endTime - startTime));
		return strategies;
	}

	@GetMapping(value = URIConstants.STRATEGY_SUMMARY)
	@ApiOperation(value = "Gets strategy data details by id and company code", response = StrategyData.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retrived strategy data details successfully") })
	@ResponseBody
	public StrategyData getStrategyDetail(HttpServletRequest request, @StrategyIdValidator @PathVariable("strategyId") long id,
			@PathVariable("companyCode") String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("In strategy-summary Rest End point, id = {}", id);
		long startTime = System.currentTimeMillis();
		boolean isHistory = false;
		StrategyData strategyDetail;
		Company company = companyService.getCompanyDetails(companyCode, isHistory,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
		strategyDetail = strategyService.getStrategyById(company, id, false);
		long endTime = System.currentTimeMillis();
		logger.info("getStrategyDetail() took {} ms", (endTime - startTime));
		return strategyDetail;
	}

	@GetMapping(value = URIConstants.GET_STRATEGY_SUBMIT_STATUS)
	@ApiOperation(value = "Gets the submit status", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy submit status retrived successfully") })
	@ResponseBody
	public String getStrategySubmitStatus(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode) {
		logger.info("In strategy-submit-status Rest End point, company-code = {}", companyCode);
		long startTime = System.currentTimeMillis();
		SubmitStatus submitStatus = submitStatusService.findLatestSubmitStatusBy(companyCode);
		List<ProcessStatus> processStatus = processStatusService.findPendingSubmitProcessBy(companyCode);
		long endTime = System.currentTimeMillis();
		logger.info("strategy-submit-status Rest End point took {} ms", (endTime - startTime));

		String status = submitStatus != null ? submitStatus.getStatus() : null;
		if (!CollectionUtils.isEmpty(processStatus)) {
			status = BSSApplicationConstants.PROCESSING;
		}
		return status;
	}

	@DeleteMapping(value = URIConstants.STRATEGY_SUMMARY)
	@ApiOperation(value = "Deletes the existing strategy", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Deleted strategy successfully") })
	@ResponseBody
	@BSSEvictCache(objectType = CacheObjectTypeEnum.STRATEGY_DATA_OBJECT_TYPE)
	public void deleteStrategy(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") @CacheKey long strategyId,
			@PathVariable("companyCode") String companyCode, @RequestParam(required = false, defaultValue = "") String exchangeId) {
		logger.info("Entering delete strategy Rest API, strategyId = {}", strategyId);
		long startTime = System.currentTimeMillis();
		Company company = companyService.getCompanyDetails(companyCode, false,
				BSSSecurityUtils.getAuthenticatedPersonId(), BenExchngEnums.getByExchangeId( exchangeId ));
		strategyService.deleteStrategy(company, strategyId);
		long endTime = System.currentTimeMillis();
		logger.info("delete strategy Rest API took {} ms", (endTime - startTime));
	}

	@PutMapping(value = URIConstants.STRATEGY_BUDGET_FACTOR)
	@ApiOperation(value = "Update total budget and budget factor", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Updated total budget and budget factor successfully.") })
	@ResponseBody
	public void updateStrategyBudget(HttpServletRequest request,
			@StrategyIdValidator @PathVariable("strategyId") final long strategyId,
			@RequestBody @Valid final StrategyBudget strategyBudget) {
		logger.info("Entering Update Strategy Budget Rest API, strategyId = {}", strategyId);
		long startTime = System.currentTimeMillis();
		strategyService.updateStrategyBudget(strategyId, strategyBudget);
		long endTime = System.currentTimeMillis();
		logger.info("Update Strategy Budget Rest API took {} ms.", (endTime - startTime));
	}

	@PostMapping(value = URIConstants.PROSPECT_STRATEGY_SUBMIT)
	@ApiOperation(value = "Submit TriNet strategy to SFDC for prospect company", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Submitted the TriNet strategy for prospect company successfully.") })
	@ResponseBody
	public void submitProspectsTriNetStrategy(HttpServletRequest request,
			@PathVariable("companyCode") @ApiParam(name = "Company-Code (e.g. 10PK)") String companyCode,
			@RequestParam(required = true) String exchangeId,
			@StrategyIdValidator @PathVariable("strategyId") final long strategyId) {
		prospectSubmitService.submit(companyCode, strategyId, exchangeId, request);
	}

	@GetMapping(value = URIConstants.COST_SUMMARY)
	@ApiOperation(value = "Returns the cost summary data from the prospect's submitted strategy", response = BenefitPlansRes.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Cost summary data fetched successfully") })
	@ResponseBody
	public StrategyCostRes getCostSummaryData(@PathVariable("companyCode") String companyCode,
			@StrategyIdValidator @PathVariable("strategyId") final long strategyId,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		boolean isHistory = false;

		String emplid = BSSApplicationConstants.EMPLID_SYSTEM_ACCOUNT;

		if (!BSSSecurityUtils.checkSystemAccount())
			emplid = BSSSecurityUtils.getAuthenticatedPersonId();

		Company company = companyService.getCompanyDetails(companyCode, isHistory, emplid,
				BenExchngEnums.getByExchangeId(exchangeId));
		return strategyService.getStrategyCostByPlanType(company, strategyId);

	}

	@PostMapping(value = URIConstants.CREATE_STRATEGIES)
	@ApiOperation(value = "Create strategies for a prospect company")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategies created successfully") })
	public void createStrategies(HttpServletRequest request,
	                             @Valid @RequestBody final CreateStrategiesRequest strategyRequest,
	                             @PathVariable("companyCode") final String companyCode) {
		long startTime = System.currentTimeMillis();
		logger.info("In createStrategies() for companyCode: {}", companyCode);
		Company company = companyService.getCompanyDetails(companyCode, false,
				"SYSTEM", BenExchngEnums.getByExchangeId(strategyRequest.getExchangeId()));

		// Convert bundleId of -1 to null (no bundle scenario)
		if (strategyRequest.getBundleId() != null && strategyRequest.getBundleId() == -1L) {
			strategyRequest.setBundleId(null);
		}
		company.setBundleId(strategyRequest.getBundleId());
		logger.info("createStrategies() - request accepted for companyCode: {}", companyCode);
		strategyService.createProspectsTrinetStrategy(company, strategyRequest.getSelectedCarrierId().longValue(), strategyRequest.getPlanMappingResponse());

		long endTime = System.currentTimeMillis();
		logger.info("createStrategies() took {} ms", (endTime - startTime));
	}

	private void performSubmit(HttpServletRequest request, String exchangeId, Company company,
			long strategyId, StrategyData strategyData) {
		if (!company.isProspectCompany()) {
			if (AppRulesAndConfigsUtils.isSubmitQueuingEnabled()) {
				queuedSubmitService.createSubmitProcess(company, strategyData, ProcessStatusEnum.SUBMIT_PROCESS.getProcessName(),
						SEND_CLIENT_EMAIL);
			} else {
				submitService.submit(company, strategyData, BSSSecurityUtils.getAuthenticatedPersonId(),
						SEND_CLIENT_EMAIL, RESUBMIT);
			}
		} else {
			prospectSubmitService.submit(company.getCode(), strategyId, exchangeId, request);
		}
	}
}
