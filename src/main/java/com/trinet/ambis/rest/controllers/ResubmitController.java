package com.trinet.ambis.rest.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.submit.ResubmitService;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.BSSSecurityUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author kpamulapati
 *
 */
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Resubmit Controller")
public class ResubmitController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResubmitController.class);

	@Autowired
	private QueuedSubmitService queuedSubmitService;
	
	@Autowired
	private SubmitService submitService;
	
	@Autowired
	private ResubmitService resubmitService;



	/**
	 * This Rest API to resubmit the failed Json by Support Team.
	 */
	@PostMapping(value = URIConstants.RESUBMIT_STRATEGY)
	@ApiOperation(value = "Resubmit most recent strategy data", response = StrategyData.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy data deatils resubmited successfully") })
	@ResponseBody
	public StrategyData resubmitToPS(HttpServletRequest request, @PathVariable("companyCode") final String companyCode,
			@RequestParam(name = "sendClientEmail", required = false, defaultValue = "true") boolean sendClientEmail) {
		try {
			return resubmitService.resubmit(companyCode, sendClientEmail);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, ResubmitController.class.getName(),
							"Resubmit Exception", null, null));
		}
	}

	/*
	 * Rest API to submit default strategy if client did not submit one.
	 */
	@PostMapping(value = URIConstants.SUBMIT_DEFAULT_STRATEGY)
	@ApiOperation(value = "Submit default strategy, if no stratagy is available")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Default strategy retrived successfully") })
	@ResponseBody
	public void defaultStrategy(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode, @PathVariable("quarter") String quarter) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("In defaultStrategy()");
		submitService.defaultSubmit(companyCode, quarter, BSSSecurityUtils.getAuthenticatedPersonId());
		submitService.defaultSubmitTermedClients(companyCode, quarter);
		long endTime = System.currentTimeMillis();
		LOGGER.info("defaultStrategy() took {} ms", (endTime - startTime));
	}



	@PostMapping(value = URIConstants.PROCESS_PENDING_SUBMISSIONS)
	@ResponseBody
	public void processPendingSubmissions(HttpServletRequest request) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("In processPendingSubmissions()");
		if (!AppRulesAndConfigsUtils.isSubmitQueuingEnabled()) {
			queuedSubmitService.startAsyncManualSubmitProcess();
		} else {
			BSSApplicationError error = new BSSApplicationError("Can't process request when submit queue is enabled.");
			throw new BSSApplicationException(error);
		}
		long endTime = System.currentTimeMillis();
		LOGGER.info("processPendingSubmits() took {} ms", (endTime - startTime));
	}

}
