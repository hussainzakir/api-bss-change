package com.trinet.ambis.rest.controllers.dto.exchange;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeCarrierDto {

	private String exchangeId;

	private String exchangeName;

	@JsonProperty("isCarrierSelectionRequired")
	private boolean carrierSelectionRequired;

	@JsonProperty("isBenefitsStartDateValid")
	private boolean benefitsStartDateValid;

	@JsonProperty("isStrategyCreated")
	private boolean strategyCreated;

	private String customBundleCreated;

	private List<CarrierDto> carriers;

}
