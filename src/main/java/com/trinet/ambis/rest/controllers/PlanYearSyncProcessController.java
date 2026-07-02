package com.trinet.ambis.rest.controllers;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.model.PlanYearRequest;
import com.trinet.ambis.service.PlanYearSyncProcessService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * @author mcshaik
 *
 */
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS StrategySyncProcess Controller")
public class PlanYearSyncProcessController {
	private static final Logger logger = LoggerFactory.getLogger(PlanYearSyncProcessController.class);

	@Autowired
	private PlanYearSyncProcessService planYearSyncProcessService;
	
	@PutMapping(value = URIConstants.UPDATE_PLAN_YEAR)
	@ApiOperation(value = "update STRATEGY_SYNC_PLYR_CHANGE for a strategy ", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "STRATEGY_SYNC_PLYR_CHANGE updated successfully") })
	@ResponseBody
	public void updatePlanYearSyncProcessStatus(@RequestBody final PlanYearRequest planYearRequest) {
		logger.info("updatePlanYearSyncProcessStatus() STRATEGY_SYNC_PLYR_CHANGE started : ", planYearRequest.getCompanyCode());
		planYearSyncProcessService.updatePlanYearSyncProcessStatus(planYearRequest);
	}
}
