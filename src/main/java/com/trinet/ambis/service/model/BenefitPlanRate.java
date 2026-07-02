package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class BenefitPlanRate implements Comparable<BenefitPlanRate>, Serializable {
	private static final long serialVersionUID = 1L;

	String benefitPlan;
	String planType;
	String benefitProgram;
	Date effDt;
	String coverageCode;
	BigDecimal employerCost;
	String bandCode;
	BigDecimal optionId;
	BigDecimal costId;
	String rateGroupId;

	@Override
	public int compareTo(BenefitPlanRate o) {
		return this.getCoverageCode().compareTo(o.getCoverageCode());
	}
}
