package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CarrierMinimumFunding {

	long carrierId;
	String planType;
	BigDecimal minimumFundingAmt;

	public CarrierMinimumFunding(long carrierId, String planType, BigDecimal minimumFundingAmt) {
		super();
		this.carrierId = carrierId;
		this.planType = planType;
		this.minimumFundingAmt = minimumFundingAmt;
	}

}
