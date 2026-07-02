package com.trinet.ambis.service.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BenDefnPlan {

	private String benefitProgram;
	private String effdt;
	private String planType;
	private String displayPlnSeq;
	private BigDecimal minAnnualContrib;
	private BigDecimal maxAnnualContrib;
	private String waiveCoverage;
	private BigDecimal restrictEntryMm;
	private String eventRulesId;
	private String cobraPlan;
	private String hipaaPlan;
	private String collectDepben;
	private String collectFunds;
	private String showPlanType;
	private String handbookUrlId;
	private String depRuleId;

}
