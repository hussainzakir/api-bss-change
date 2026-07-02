package com.trinet.ambis.rest.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.model.BenOfferExceptionDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@PreAuthorize("@hrpService.hasAccessToGatewayApp(T(com.trinet.ambis.common.BSSApplicationConstants).BEN_OFFER_EXCEPTION_APP_KEY)")
@Api(value = "Trinet API-BSS  Benefit Offer Exception Controller")
public class BenefitOfferExceptionController {

	@Autowired
	private BenefitOfferExceptionService benOfferExceptionService;
	
	@GetMapping(value = URIConstants.BENOFFER_EXCEPTIONS)
	@ApiOperation(value = "Gets all benefit offer exceptions", response = BenOfferExceptionDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "All benefit offer exceptions retrived successfully") })
	@ResponseBody
	public List<BenOfferExceptionDto> getAllBenefitOfferExceptions(HttpServletRequest request) {
		return benOfferExceptionService.findAllActive();
	}

	@GetMapping(value = URIConstants.GET_BENOFFER_EXCEPTION)
	@ApiOperation(value = "Gets minimum fund exceptions for benefit offerings", response = BenOfferExceptionDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Minimum fund exceptions retrived successfully") })
	@ResponseBody
	public BenOfferExceptionDto getMinFundException(HttpServletRequest request, @PathVariable("benOfferExceptionId") long id) {
		return benOfferExceptionService.findBy(id);

	}

	@PutMapping(value = URIConstants.BENOFFER_EXCEPTIONS)
	@ApiOperation(value = "Updates benefit offer exception", response = BenOfferExceptionDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Updated benefit offer exception successfully") })
	@ResponseBody
	public BenOfferExceptionDto updateBenOfferException(HttpServletRequest request,
			@RequestBody final BenOfferExceptionDto dto) {

		return benOfferExceptionService.update(dto);

	}

	@PostMapping(value = URIConstants.BENOFFER_EXCEPTIONS)
	@ApiOperation(value = "Saves benefit offer exceptions", response = BenOfferExceptionDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Saved benefit offer exceptions successfully") })
	@ResponseBody
	public BenOfferExceptionDto saveBenOfferException(HttpServletRequest request,
			@RequestBody final BenOfferExceptionDto dto) {
		return benOfferExceptionService.save(dto);
	}

}
