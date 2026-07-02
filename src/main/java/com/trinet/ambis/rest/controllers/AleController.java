package com.trinet.ambis.rest.controllers;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.email.AleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS AleController Controller ")
public class AleController {

    @Autowired
    private AleService aleService;

    @PutMapping(value = URIConstants.UPDATE_ALE)
    @ApiOperation(value = "Update the ALE Status By Company")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "ALE information updated successfully"),
            @ApiResponse(code = 400, message = "Invalid request parameters")
    })
    public void updateAleStatus(@PathVariable("bssCompanyId") long bssCompanyId,
                                @PathVariable("companyCode") String companyCode) {
        aleService.updateAleChangeStatus(bssCompanyId, companyCode);
    }
}
