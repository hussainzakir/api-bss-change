package com.trinet.ambis.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.service.SupplementalAuthService;
import com.trinet.ambis.service.model.SupplementalLtdAuthReponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "SupplementalAuthController")
@Validated
public class SupplementalAuthController {

	private static final String ANSWER = "answer";

	private final SupplementalAuthService supplementalAuthService;

	@Autowired
	public SupplementalAuthController(SupplementalAuthService supplementalAuthService) {
		this.supplementalAuthService = supplementalAuthService;
	}

	@GetMapping(value = URIConstants.EXEC_SUPP_LTD_AUTH_RESPONSE)
	@ApiOperation(value = "Get Executive Supplemental LTD Authorization response.", response = SupplementalLtdAuthReponse.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Executive Supplemental LTD Authorization response fetched!") })
	public SupplementalLtdAuthReponse getExecSuppLtdAuthResponse(
			@PathVariable("companyCode") final String companyCode) {
		return supplementalAuthService.getExecSuppLtdAuthResponse(companyCode);
	}

	@PostMapping(value = URIConstants.EXEC_SUPP_LTD_AUTH_RESPONSE)
	@ApiOperation(value = "Save Executive Supplemental LTD Authorization response.", response = SupplementalLtdAuthReponse.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Executive Supplemental LTD response Authorization response saved!") })
	public SupplementalLtdAuthReponse saveExecSuppLtdAuthResponse(@RequestBody(required = true) final JsonNode answer,
			@PathVariable("companyCode") final String companyCode) {
		if (!("Y".equals(answer.get(ANSWER).asText()) || "N".equals(answer.get(ANSWER).asText()))) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR,
					BSSHttpStatusConstants.BAD_REQUEST, "", "Answer value should be Y or N.", null, null));
		}
		return supplementalAuthService.saveExecSuppLtdAuthResponse(companyCode, answer.get(ANSWER).asText().charAt(0));
	}
}
