package com.trinet.ambis.rest.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeBandsDto;
import com.trinet.ambis.rest.controllers.dto.exchange.ExchangeCarrierDto;
import com.trinet.ambis.rest.controllers.views.ExchangeBandsViews;
import com.trinet.ambis.service.ExchangeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Exchange Controller")
public class ExchangeController {

	@Autowired
	private ExchangeService exchangeService;

	@GetMapping(value = URIConstants.GET_EXCHANGE_CARRIERS)
	@ApiOperation(value = "Gets the exchange carriers information for the given company ids", responseContainer = "Set")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retrieved exchange carriers data successfully.") })
	public List<ExchangeCarrierDto> getExchangeCarriers(@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		return exchangeService.getExchangeCarriers(companyCode, BenExchngEnums.getByExchangeId( exchangeId ));
	}

	@GetMapping(value = URIConstants.EXCHANGE_BANDS)
	@ApiOperation(value = "Gets the exchanges carriers and bands information for the given company ids", responseContainer = "List")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retrieved exchange carriers band data successfully.") })
	@JsonView(ExchangeBandsViews.GetResView.class)
	public List<ExchangeBandsDto> getExchangeCarriersBands(@PathVariable("companyCode") String companyCode,
			@RequestParam(required = false, defaultValue = "") String exchangeId) {
		return exchangeService.getExchangeBands(companyCode, BenExchngEnums.getByExchangeId( exchangeId ));
	}

	@PutMapping(value = URIConstants.EXCHANGE_BANDS)
	@ApiOperation(value = "Creates bss company and saves band details", responseContainer = "List")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Creates bss company and saved band details successfully.") })
	@JsonView(ExchangeBandsViews.PutResView.class)
	public List<ExchangeBandsDto> saveExchangeBands(@RequestBody List<ExchangeBandsDto> exchangeBands,
			@PathVariable("companyCode") String companyCode) {
		return exchangeService.saveExchangeBands(exchangeBands, companyCode);
	}

}
