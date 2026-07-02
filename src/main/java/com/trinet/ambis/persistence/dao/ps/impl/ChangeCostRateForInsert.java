package com.trinet.ambis.persistence.dao.ps.impl;

import lombok.Data;

@Data
public class ChangeCostRateForInsert {
	private String rateType;
	private String rateTblId;
	private String erncd;
	private String benefitProgram;
	private String effdtStr;
	private String planType;
	private String benefitPlan;
	private String covrgCd;

}
