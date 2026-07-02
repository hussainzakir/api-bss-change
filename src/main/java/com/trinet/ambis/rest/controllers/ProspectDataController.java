package com.trinet.ambis.rest.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.rest.controllers.dto.ProspectDataUpdateRequest;
import com.trinet.ambis.service.ProspectDataService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST controller for updating prospect data on location or NAICS code changes
 *
 * @author echavarria
 */
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Prospect Data Controller")
public class ProspectDataController {

    private static final Logger logger = LoggerFactory.getLogger(ProspectDataController.class);

    @Autowired
    private ProspectDataService prospectDataService;

    /**
     * Updates prospect strategies based on location or NAICS code changes.
     * Only accessible by api-benefit-batch system account.
     *
     * @param companyCode the company code
     * @param request the update request containing location/NAICS update flags and data
     * @param httpRequest the HTTP servlet request
     */
    @PutMapping(value = URIConstants.UPDATE_PROSPECT_DATA)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Update prospect data on location or NAICS code change", response = Void.class)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Prospect data updated successfully"),
        @ApiResponse(code = 400, message = "Invalid request data"),
        @ApiResponse(code = 403, message = "Forbidden - Only api-benefit-batch system account can access this endpoint")
    })
    public void updateProspectData(
            HttpServletRequest httpRequest,
            @PathVariable("companyCode") String companyCode,
            @Valid @RequestBody ProspectDataUpdateRequest request) {

        logger.info("Received prospect data update request for company: {}, locationUpdate: {}, naicsCodeUpdate: {}",
            companyCode, request.isLocationUpdate(), request.isNaicsCodeUpdate());

        prospectDataService.updateProspectData(companyCode, request);

        logger.info("Completed prospect data update for company: {}", companyCode);
    }
}

