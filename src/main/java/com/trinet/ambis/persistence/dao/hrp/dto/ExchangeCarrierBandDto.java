package com.trinet.ambis.persistence.dao.hrp.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeCarrierBandDto {

	private long realmId;

	private Date effectiveDt;

	private long companyId;

	private List<String> carrierCode;
	
	private List<String> bandCodeValue;
}
