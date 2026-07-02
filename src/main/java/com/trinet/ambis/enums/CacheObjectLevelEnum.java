package com.trinet.ambis.enums;

/**
 * @author schaudhari
 */
public enum CacheObjectLevelEnum {

	REALM_PLAN_YEAR("REALM-PLAN-YEAR"), COMPANY("COMPANY"), STRATEGY("STRATEGY");

	private final String value;

	public String getObjectLevel() {
		return value;
	}

	private CacheObjectLevelEnum(String value) {
		this.value = value;
	}
}
