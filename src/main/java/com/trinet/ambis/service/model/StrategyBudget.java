package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class StrategyBudget implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8510789946103532240L;

	@DecimalMin(value = "0", message = "The budget should be greater than 0.00")
	private BigDecimal budget;

	@Pattern(regexp = "^([1]|1[2])$", message = "Invalid budgetFactor. Accepted values are 1 (annual) and 12 (monthly)")
	private String budgetFactor;

}
