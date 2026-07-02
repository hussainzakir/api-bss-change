package com.trinet.ambis.rest.controllers.dto.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarrierDto {

	private long portfolioId;

	private String portfolioName;

}
