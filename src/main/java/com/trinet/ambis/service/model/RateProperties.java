package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class RateProperties {
	private String planType;
	private String benefitPlan;
	private String rateTblID;
	private int rateType;
	private String ratePerUnit;
	private String benProg;

}
