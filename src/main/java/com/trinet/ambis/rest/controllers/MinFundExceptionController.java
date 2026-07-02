/**
 * 
 */
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
import com.trinet.ambis.service.MinFundExceptionService;
import com.trinet.ambis.service.model.MinFundExceptionDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author schaudhari
 *
 */
@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@PreAuthorize("@hrpService.hasAccessToGatewayApp(T(com.trinet.ambis.common.BSSApplicationConstants).MIN_FUND_EXCEPTION_APP_KEY)")
@Api(value = "Trinet API-BSS MinFund Exception Controller")
public class MinFundExceptionController {

	@Autowired
	private MinFundExceptionService minFundExceptionService;
	
	@GetMapping(value = URIConstants.MIN_FUND_EXCEPTIONS)
	@ApiOperation(value = "Gets All Min Fund Exceptions", response = MinFundExceptionDto.class, responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "All Min Fund Exceptions retrived successfully") })
	@ResponseBody
	public List<MinFundExceptionDto> getAllMinFundExceptions(HttpServletRequest request) {
			return minFundExceptionService.findAllActive();
	}

	@GetMapping(value = URIConstants.GET_MIN_FUND_EXCEPTION)
	@ApiOperation(value = "Gets Min Fund Exception details", response = MinFundExceptionDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Min Fund Exception details retrived successfully") })
	@ResponseBody
	public MinFundExceptionDto getMinFundException(HttpServletRequest request, @PathVariable("minFundExceptionId") long id) {
			return minFundExceptionService.findBy(id);
	}

	@PutMapping(value = URIConstants.MIN_FUND_EXCEPTIONS)
	@ApiOperation(value = "Updates Min Fund Exception details", response = MinFundExceptionDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Min Fund Exception updated successfully") })
	@ResponseBody
	public MinFundExceptionDto updateMinFundException(HttpServletRequest request,
			@RequestBody final MinFundExceptionDto dto) {
		return minFundExceptionService.update(dto);
	}

	@PostMapping(value = URIConstants.MIN_FUND_EXCEPTIONS)
	@ApiOperation(value = "Saves Min Fund Exception details", response = MinFundExceptionDto.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Min Fund Exception saved successfully") })
	@ResponseBody
	public MinFundExceptionDto saveMinFundException(HttpServletRequest request,
			@RequestBody final MinFundExceptionDto dto) {
		return minFundExceptionService.save(dto);
	}

}
