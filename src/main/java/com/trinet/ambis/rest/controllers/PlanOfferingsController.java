package com.trinet.ambis.rest.controllers;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.planofferings.PlanOfferingsReportDataService;
import com.trinet.ambis.service.planofferings.PlanOfferingsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author smaguluri
 *
 */

@Log4j2
@CrossOrigin
@Api(value = "PlanOfferingsController")
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
public class PlanOfferingsController {
	@Autowired
	private PlanOfferingsService planOfferingsService;
	@Autowired
	private PlanOfferingsReportDataService planOfferingsReportDataService;
	
	@PostMapping(value = URIConstants.PALN_OFFERINGS)
	@ResponseBody
	@ApiOperation(value = "Generating Plan Offerings Report")
	@ApiResponse(code = 200, message = "Generate Report")
	public ResponseEntity<byte[]> generateReport(HttpServletRequest httpRequest,
			@RequestBody PlanOfferingsRequest planOfferingsRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=plan_offerings.pdf");
		byte[] docGenResponse = planOfferingsService.generatePlanOfferingsReport(planOfferingsRequest, httpRequest);
		log.info("Generated the plan offerings successfully...");
		return new ResponseEntity<>(docGenResponse, headers, HttpStatus.OK);
	}

	@GetMapping(value = URIConstants.GET_PLAN_OFFERING_CARRIERS)
	public List<CarrierData> getCarriersBy(
			@PathVariable("companyId") @ApiParam(name = "Company-ID (e.g. 10PK)") String companyId,
			@PathVariable("employeeId") @ApiParam(name = "Employee-ID (e.g. 00010396875)") String employeeId,
			@PathVariable("companyCode") @ApiParam(name = "Company-Code (e.g. 10PK)") String companyCode,
			@RequestParam(required = true, defaultValue = "") String reportCode,
			@RequestParam(required = true, defaultValue = "") String quarter,
			@RequestParam(required = true, defaultValue = "") Date effDt,
			@RequestParam(required = true, defaultValue = "") String hqState,
			@RequestParam(required = false) Optional<String> hqZipCode,
			@RequestParam(name = "benefitTypes", required = false) String benefitType) {
		return planOfferingsReportDataService.getCarriersBy(reportCode,quarter,effDt,hqState,hqZipCode,benefitType);
	}


}
