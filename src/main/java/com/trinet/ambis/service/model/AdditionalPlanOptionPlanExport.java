package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author hliddle
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AdditionalPlanOptionPlanExport implements Comparable<AdditionalPlanOptionPlanExport> {

	private String name;
	private String planType;
	private boolean isSdiPlan;
	private String offeredStatesString;
	private String currentUnit;
	private String futureUnit;
	private BigDecimal currentCost;
	private BigDecimal futureCost;
	
	@Override
	public int compareTo(AdditionalPlanOptionPlanExport o) {

		int value1 = this.getPlanType().compareTo(o.getPlanType());
		if (value1 == 0) {
			return this.getName().compareTo(o.getName());
		} else {
			return value1;
		}
	}

}
