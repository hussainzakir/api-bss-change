package com.trinet.ambis.rest.controllers;

import java.util.Date;

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

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.QueuedSubmitService;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.submit.ResubmitService;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author schaudhari
 *
 */
@RestController
@RequestMapping(URIConstants.VERSION_AND_PLATFORM)
@Api(value = "Controller to handle all platform api request.")
public class PlatformRequestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformRequestController.class);

	@Autowired
	private SubmitService submitService;

	@Autowired
	private ResubmitService resubmitService;

	@Autowired
	private QueuedSubmitService queuedSubmitService;

	/*
	 * Rest API to submit default strategy for termed clients form platform batch
	 * job. This API only be accessible by system account
	 */
	@PostMapping(value = URIConstants.SUBMIT_DEFAULT_STRATEGY_PLATFORM)
	@ApiOperation(value = "Submit default strategy, if no stratagy is submitted. Accessible by system accounts only.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Default strategy submitted successfully") })
	@ResponseBody
	public void termDefaultSubmit(HttpServletRequest request, @PathVariable("companyCode") String companyCode,
			@PathVariable("peoId") String peoId, @PathVariable("quarter") String quarter) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("In Term defaultSubmit method");
		String userId = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName();

		if (AppRulesAndConfigsUtils.isSubmitQueuingEnabled()) {
			queuedSubmitService.createAsyncDefaultSubmitProcess(companyCode, userId);
		} else {
			submitService.defaultSubmit(companyCode, quarter, userId);
		}
		long endTime = System.currentTimeMillis();
		LOGGER.info("Term defaultSubmit method took {} ms", (endTime - startTime));
	}

	@PostMapping(value = URIConstants.RESUBMIT_BAND_CODE_PLATFORM)
	@ApiOperation(value = "Resubmit the last submitted strategy for band code change for given effective date.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Strategy resubmitted successfully") })
	@ResponseBody
	public void bandCodeResubmitSubmit(HttpServletRequest request, @PathVariable("companyCode") String companyCode,
			@RequestParam("effdt") Date effdt) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("In bandCodeResubmitSubmit method");
		resubmitService.bandcodeResubmit(companyCode, effdt);
		long endTime = System.currentTimeMillis();
		LOGGER.info("bandCodeResubmitSubmit method took {} ms", (endTime - startTime));
	}

}
