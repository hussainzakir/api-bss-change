package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BenefitPlanRatesData {
	private BigDecimal eeRate;
	private BigDecimal erRate;
	private String planType;
	private String bandCode;

	public BenefitPlanRatesData(BigDecimal eeRate, BigDecimal erRate, String planType, String bandCode) {
		this.eeRate = eeRate;
		this.erRate = erRate;
		this.planType = planType;
		this.bandCode = bandCode;
	}
}