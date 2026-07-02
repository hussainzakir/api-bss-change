package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class PlanContribution implements Serializable {
	private static final long serialVersionUID = 1L;

	Long id;
	long planSelectionId;
	String type;
	String benefitPlanId;
	@JsonIgnore
	private String bssBenefitPlanId;
	private int headcount;
	private int hsaHeadcount;
	private int mirrorHeadCount;
	private BigDecimal planCost;
	private BigDecimal employerPercent;
	private BigDecimal employeePercent;
	private BigDecimal employerContribution;
	private BigDecimal employeeContribution;
	private String overrideType;
}
