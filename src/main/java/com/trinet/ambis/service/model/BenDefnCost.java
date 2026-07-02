package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BenDefnCost {

	private String benefitProgram;
	private String effdt;
	private String planType;
	private BigDecimal optionId;
	private BigDecimal costId;
	private String costType;
	private String erncd;
	private String rateType;
	private String rateTblId;
	private String calcRulesId;

}
