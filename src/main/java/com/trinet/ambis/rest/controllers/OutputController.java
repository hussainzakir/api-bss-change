package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.service.OutputRequestBuilder;
import com.trinet.ambis.service.outputs.OutputReportDataService;
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
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.outputs.OutputService;
import com.trinet.ambis.service.prospect.dto.BenTypeOfferRes;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author pallu
 *
 */
@Log4j2
@CrossOrigin
@Api(value = "ProspectOutputsController")
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
public class OutputController {

	@Autowired
	private OutputService prospectoutputsService;

    @Autowired
    private OutputReportDataService outputReportDataService;

	@Autowired
	private CompanyService companyService;

	@PostMapping(value = URIConstants.PROSPECT_OUTPUTS)
	@ResponseBody
	@ApiOperation(value = "Generating BSS Report")
	@ApiResponse(code = 200, message = "Generate Report")
	public ResponseEntity<byte[]> generateReport(
			HttpServletRequest httpRequest,
			@PathVariable("employeeId") @ApiParam(name = "Employee-ID (e.g. 00010396875)") String employeeId,
			@PathVariable("companyCode") @ApiParam(name = "Company-Code (e.g. 10PK)") String companyCode,
			@RequestParam(required = true, defaultValue = "") String exchangeId,
			@RequestBody OutputRequest prospectRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=prospective_bss.pdf");
		Company company = companyService.getCompanyDetails(companyCode, false, employeeId,
				BenExchngEnums.getByExchangeId(exchangeId));
		byte[] docGenResponse = prospectoutputsService.generateReport(prospectRequest, company, httpRequest);
		log.info("Generated the outputs successfully...");
		return new ResponseEntity<>(docGenResponse, headers, HttpStatus.OK);
	}

    @PostMapping(value = URIConstants.PROSPECT_OUTPUTS_JSON)
    @ResponseBody
    @ApiOperation(value = "Generating BSS Report")
    @ApiResponse(code = 200, message = "Generate Report")
    public OutputData generateReportJSON(
            HttpServletRequest httpRequest,
            @PathVariable("employeeId") @ApiParam(name = "Employee-ID (e.g. 00010396875)") String employeeId,
            @PathVariable("companyCode") @ApiParam(name = "Company-Code (e.g. 10PK)") String companyCode,
            @RequestParam(required = true, defaultValue = "") String exchangeId,
            @RequestBody OutputRequest prospectRequest) {
        Company company = companyService.getCompanyDetails(companyCode, false, employeeId,
                BenExchngEnums.getByExchangeId(exchangeId));
        return outputReportDataService.getData(prospectRequest, company, httpRequest);
    }
	
	@GetMapping(value = URIConstants.OUTPUT_FILTER_BENEFIT_OFFERS)
	@ApiOperation(value = "Return offered benefit types for given strategies", response = BenTypeOfferRes.class, responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Details of plan types offered in a strategy retrived successfully") })
	@ResponseBody
	public List<BenTypeOfferRes> getNotOfferedPlanTypes(HttpServletRequest request,
			@PathVariable("strategyIds") List<Long> strategyIds) {
		return prospectoutputsService.getPlanTypeOfferedDetails(strategyIds, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
	}

}