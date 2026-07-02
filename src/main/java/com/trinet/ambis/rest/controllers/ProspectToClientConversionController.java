package com.trinet.ambis.rest.controllers;

import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectToClientConversionResponse;
import com.trinet.ambis.service.prospect.ProspectToClientConversionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Prospect to client conversion Controller")
@Validated
public class ProspectToClientConversionController {

	@Autowired
	ProspectToClientConversionService prospectToClientConversionService;

    @PostMapping(value = URIConstants.PROSPECT_TO_CLIENT_CONVERSION)
    @ApiOperation(value = "Convert Prospect to Client")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Prospect converted to client successfully")})
	public ProspectToClientConversionResponse processProspectToClientConversion(
			@PathVariable("prospectId") String prospectId, @RequestBody ProspectToClientConversionRequest request) {
        request.setProspectId(prospectId);
		return prospectToClientConversionService.processProspectToClientConversion(request);
	}


}
