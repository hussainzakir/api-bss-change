package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MinimumFunding {

	public MinimumFunding(String planType, String minFundValueType, BigDecimal minFundValue, boolean isException) {
		this.planType = planType;
		this.minFundType = minFundValueType;
		this.minFundValue = minFundValue;
		this.isException = isException;
	}

	private String planType;
	private String minFundType;
	private BigDecimal minFundValue;
	private boolean isException;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinimumFunding other = (MinimumFunding) obj;
		if (planType == null) {
			if (other.planType != null)
				return false;
		} else if (!planType.equals(other.planType))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((planType == null) ? 0 : planType.hashCode());
		return result;
	}
	
}
