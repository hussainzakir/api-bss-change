package com.trinet.ambis.enums;

/**
 * @author schaudhari
 */
public enum CacheObjectTypeEnum {

	ALL("ALL"), PLAN_RATES_OBJECT_TYPE("PLAN-RATES"), BEN_PLANS_OBJECT_TYPE("BENEFIT-PLANS"),
	RATES_BEN_PLANS_OBJECT_TYPE("RATES-BENEFIT-PLANS"), STRATEGY_DATA_OBJECT_TYPE("STRATEGY_DATA"),
	BASIC_COMPANY_DETAILS("BASIC_COMPANY_DETAILS"), COMPANY_DETAILS_EFFDT("COMPANY_DETAILS_EFFDT"),
	OMS_BENEFIT_PLAN_RATES("OMS-BENEFIT-PLAN-RATES");

	private final String value;

	public String getObjectType() {
		return value;
	}

	private CacheObjectTypeEnum(String value) {
		this.value = value;
	}
}
