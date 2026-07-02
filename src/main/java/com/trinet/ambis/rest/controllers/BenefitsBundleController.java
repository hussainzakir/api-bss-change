package com.trinet.ambis.rest.controllers;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.rest.controllers.dto.BundlePlanResponse;
import com.trinet.ambis.rest.controllers.dto.BundlePlansDto;
import com.trinet.ambis.rest.controllers.dto.BundleSelectionDetailsRequest;
import com.trinet.ambis.service.BenefitsBundleService;
import com.trinet.ambis.service.model.BenefitsBundleDto;
import com.trinet.ambis.service.model.BundleSelectionDetailsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS BundleBenefitPlans Controller ")
public class BenefitsBundleController {

    @Autowired
    private BenefitsBundleService benefitsBundleService;

    @GetMapping(value = URIConstants.BUNDLE_BY_QUARTER)
    @ApiOperation(value = "Gets the Bundle information By Quarter", response = BenefitsBundleDto.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved Bundle information successfully"),
            @ApiResponse(code = 400, message = "Invalid request parameters")
    })
    public List<BenefitsBundleDto> getBundlesDetails(
            @RequestParam(name = "oeQuarter", required = true) @NotBlank String oeQuarter,
            @RequestParam(name = "effectiveDate", required = true) @NotBlank String effectiveDate) {
        return benefitsBundleService.getBundleDetails(oeQuarter, effectiveDate);
    }

    @GetMapping(value = URIConstants.BUNDLES_PLANS)
    @ApiOperation(value = "Gets Bundle and associated Plans by effective date and quarter",
            response = BundlePlanResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Retrieved bundle plans successfully"),
            @ApiResponse(code = 400, message = "Invalid request parameters")
    })
    public BundlePlanResponse getBundlesPlans(
            @RequestParam(name = "effectiveDate", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate effectiveDate,
            @RequestParam(name = "quarter", required = true) @NotBlank String quarter) {
        return benefitsBundleService.getBundlesByEffectiveDateAndQuarter(effectiveDate, quarter);
    }

    @GetMapping(value = URIConstants.BUNDLE_PLANS)
    @ApiOperation(value = "Get bundle and exchange plans", response = BundlePlansDto.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Bundle and exchange plans retrieved successfully"),
            @ApiResponse(code = 400, message = "Invalid request parameters")
    })
    public List<BundlePlansDto> getBundlePlanExchangePlans(
            @PathVariable("companyCode") String companyCode,
            @RequestParam(name = "exchangeId", required = true) String exchangeId,
            @RequestParam(name = "bundleIds", required = false) Set<Long> bundleIds) {
        return benefitsBundleService.getBundleAndExchangePlans(companyCode, exchangeId,
                bundleIds == null || bundleIds.isEmpty() ? null : bundleIds);
    }

    @PostMapping(value = URIConstants.BUNDLE_SELECTION_DETAILS)
    @ApiOperation(value = "Gets bundle selection details for company", response = BundleSelectionDetailsDto.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Bundle selection details retrieved successfully") })
    public List<BundleSelectionDetailsDto> getBundleSelectionDetails(
            @RequestBody @javax.validation.Valid BundleSelectionDetailsRequest request) {
        return benefitsBundleService.getBundleSelectionDetails(request);
    }
}