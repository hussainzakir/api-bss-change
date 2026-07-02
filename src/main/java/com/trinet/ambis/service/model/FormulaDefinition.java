package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FormulaDefinition {

	private String benOperand;
	private String bnEntryTyp;
	private BigDecimal bnValue;
	private BigDecimal roundUpAmt;
	private BigDecimal roundTo;

}
