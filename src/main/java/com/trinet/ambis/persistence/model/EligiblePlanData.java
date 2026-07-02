package com.trinet.ambis.persistence.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EligiblePlanData implements Serializable {
	private String planId;
	private String carrier;
	private BigDecimal planCost;
}
