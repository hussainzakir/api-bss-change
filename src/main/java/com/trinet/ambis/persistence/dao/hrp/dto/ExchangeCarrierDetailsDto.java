package com.trinet.ambis.persistence.dao.hrp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeCarrierDetailsDto {

	private long realmId;

	private long portfolioId;

	private String portfolioName;
	
	private boolean strategyCreated;

}
