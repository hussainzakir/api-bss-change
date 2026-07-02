package com.trinet.ambis.rest.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.rest.controllers.dto.ChangeQuarterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto;
import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;
import com.trinet.ambis.service.BSSStatusDetailsService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.model.CommonData;
import com.trinet.ambis.service.model.CompanyRealmData;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.domain.common.ReturnResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Company Controller")
public class CompanyController {

	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private BSSStatusDetailsService bssStatusDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

	/**
	 * This method is used for getting Company Common Data.
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param code
	 *            Company Code
	 * @return CommonData
	 * @throws JsonProcessingException
	 */
	@GetMapping(value = URIConstants.GET_COMMON_DATA)
	@ApiOperation(value = "Gets company common data", response = CommonData.class)
	@ApiResponses(value = {@ApiResponse(code = 200, message = "Company common data retrived successfully")})
	@ResponseBody
	public CommonData getCommonData(HttpServletRequest request, @PathVariable("companyCode") String code,
									@RequestParam(required = false, defaultValue = "") String exchangeId,
									@RequestParam(required = false) boolean strategyAccessed) {
		CommonData commonData;
		logger.info("getCommonData() for code: {}, exchangeId: {}", code, exchangeId);
		long startTime = System.currentTimeMillis();
		String emplid = BSSApplicationConstants.EMPLID_SYSTEM_ACCOUNT;
		if (!BSSSecurityUtils.checkSystemAccount())
			emplid = BSSSecurityUtils.getAuthenticatedPersonId();
		commonData = companyService.getCompanyCommonData(code, emplid, BenExchngEnums.getByExchangeId(exchangeId),
				strategyAccessed);
		long endTime = System.currentTimeMillis();
		logger.info("getCommonData() took {} ms", (endTime - startTime));
		return commonData;
	}

	/**
	 * This method is for passing company data to ADMIN screen for submit
	 * functionality.
	 * 
	 * @param request
	 * @param code
	 * @return
	 * @throws JsonProcessingException
	 */
	@GetMapping(value = URIConstants.GET_COMPANY_DATA)
	@ApiOperation(value = "Gets company data details", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Company data retrived successfully") })
	@ResponseBody
	public List<CompanyRealmData> getCompanyData(HttpServletRequest request, @PathVariable("companyCode") String code) {
		String emplid = BSSSecurityUtils.getAuthenticatedPersonId();
		return companyService.getCompanyPlanYearData(code, emplid);
	}
	
	@GetMapping(value = URIConstants.GET_COMPANY_NAME)
	@ApiOperation(value = "Gets company name information", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Company name retrived successfully") })
	@ResponseBody
	public Map<String, String> getCompanyName(HttpServletRequest request, @PathVariable("companyCode") String code) {
		Set<String> companyCodes = new HashSet<>(1);
		companyCodes.add(code);
		return companyService.findCompaniesNames(companyCodes);
	}
	
	@GetMapping(value = URIConstants.IS_RENEWAL_COMPANY)
	@ApiOperation(value = "Gets company's renewal status", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Company's renewal status retrived successfully") })
	@ResponseBody
	public ReturnResponse<Boolean> isRenewalCompany(HttpServletRequest request, @PathVariable("companyCode") String code) {
		boolean result = companyService.isRenewalCompany(code);
		ReturnResponse<Boolean> response = new ReturnResponse<>();
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setData(result);
		return response;
	}
	
	@GetMapping(value = URIConstants.BSS_STATUS)
	@ApiOperation(value = "Gets bss status by company code", response = Boolean.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "bss status retrived successfully") })
	@ResponseBody
	public ReturnResponse<BSSStatusDetailsDto> bssStatus(HttpServletRequest request, @PathVariable("companyCode") String code) {
		BSSStatusDetailsDto bssStatusDto = bssStatusDetailsService.getBssStatusDetail(code);
		ReturnResponse<BSSStatusDetailsDto> response = new ReturnResponse<>();
		response.setData(bssStatusDto);
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setStatusMessage("Success");
		response.setStatusText(BSSHttpStatusConstants.OK_HTTP_STATUS.getReasonPhrase());
		return response;
	}

	@PutMapping(value = URIConstants.PLYR_CHANGE_SYNC_EXCUTED)
	@ApiOperation(value = "Update flag value for PLYR_CHANGE_SYNC_EXCUTED executed successfully", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "PLYR_CHANGE_SYNC_EXCUTED flag updated successfully") })
	@ResponseBody
	public void updatePlYrChangeSyncExecutedFlag(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode, @RequestParam("exchangeId") String exchangeId) {
		logger.info("updatePlYrChangeSyncExecutedFlag() for companyCode : {}", companyCode);
		companyService.updatePlYrChangeSyncExecutedFlag(companyCode, BenExchngEnums.getByExchangeId(exchangeId));
		logger.info("updatePlYrChangeSyncExecutedFlag() for companyCode : {} executed successfully.", companyCode);
	}


	@PostMapping(value = URIConstants.QUARTER_CHANGE)
	@ApiOperation(value = "Change the quarter for the company", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Quarter change initiated successfully") })
	@ResponseBody
	public void changeQuarter(
			HttpServletRequest request,
			@PathVariable("companyCode") String companyCode,
			@RequestBody ChangeQuarterRequest changeQuarterRequest) {
		String quarter = changeQuarterRequest.getQuarter();
		String messageSeq = changeQuarterRequest.getMessageSeq();
		if (quarter == null || messageSeq == null) {
			throw createValidationException(
					"Request body must include both 'quarter' and 'messageSeq' and both must be valid.");
		}
		if (!BenExchngEnums.isValidQuarter(quarter)) {
			throw createValidationException("Invalid quarter value: " + quarter);
		}

		logger.info("changeQuarter() for companyCode : {}, quarter: {}, messageSeq: {}", companyCode, quarter, messageSeq);
		boolean initiated = companyService.initiateQuarterChange(companyCode, quarter, messageSeq);
		logger.info("changeQuarter() for companyCode : {}, quarter: {} initiated: {}.", companyCode, quarter, initiated);
	}

	private BSSApplicationException createValidationException(String message) {
		return new BSSApplicationException(
				new BSSApplicationError(
						BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR,
						BSSHttpStatusConstants.BAD_REQUEST,
						"",
						message,
						null,
						null
				)
		);
	}

	/**
	 * Gets company details by company ID including plan year start, OMS offering,
	 * clone program, bundle sequence, and OE quarter.
	 *
	 * @param request HttpServletRequest
	 * @param companyId the company ID
	 * @return CompanyDetailsDto containing the company details
	 */
	@GetMapping(value = URIConstants.GET_COMPANY_DETAILS_BY_ID)
	@ApiOperation(value = "Gets company details by company ID", response = CompanyDetailsDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Company details retrieved successfully") })
	@ResponseBody
	public ReturnResponse<CompanyDetailsDto> getCompanyDetailsById(HttpServletRequest request,
			@PathVariable("companyCode") String companyCode,
	        @RequestParam("companyId") long companyId) {
		logger.info("getCompanyDetailsById() for companyId: {}", companyId);
		long startTime = System.currentTimeMillis();
		CompanyDetailsDto companyDetails = companyService.getCompanyDetailsById(companyId);
		ReturnResponse<CompanyDetailsDto> response = new ReturnResponse<>();
		response.setData(companyDetails);
		response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		response.setStatusMessage("Success");
		response.setStatusText(BSSHttpStatusConstants.OK_HTTP_STATUS.getReasonPhrase());
		long endTime = System.currentTimeMillis();
		logger.info("getCompanyDetailsById() took {} ms", (endTime - startTime));
		return response;
	}

   /**
    * Resets all strategies and company-level data for the given company in a single transaction.
    * Requires role APP_SUPPORT, BSS_ENG_SUP, or api-benefits-batch (via BSS-CR gateway app key).
    */
   @DeleteMapping(value = URIConstants.RESET_COMPANY)
   @ApiOperation(value = "Resets all strategies and company data for a company")
   @ApiResponses(value = {
       @ApiResponse(code = 200, message = "Company reset successfully"),
       @ApiResponse(code = 403, message = "Access denied - insufficient roles")
   })
   @ResponseBody
   public ReturnResponse<Void> resetCompany(
           HttpServletRequest request,
           @PathVariable("companyCode") String companyCode,
           @RequestParam("legacyCompanyId") long legacyCompanyId) {
       logger.info("resetCompany() for companyCode: {}, legacyCompanyId: {}", companyCode, legacyCompanyId);
       companyService.resetCompany(legacyCompanyId);
       ReturnResponse<Void> response = new ReturnResponse<>();
       response.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
       response.setStatusMessage("Success");
       response.setStatusText(BSSHttpStatusConstants.OK_HTTP_STATUS.getReasonPhrase());
       logger.info("resetCompany() completed for companyCode: {}", companyCode);
       return response;
   }

}
