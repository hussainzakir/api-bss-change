package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AdditionalPlanRate {
	private String rateTblId;
	private int age;
	private String unit;
	private BigDecimal rate;
}
