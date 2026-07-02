package com.trinet.ambis.enums;

import com.trinet.ambis.common.BSSApplicationConstants;

public enum BenefitsCostEnum {
	
	LIFE(BSSApplicationConstants.LIFE_CODE, "Life / AD&D"),
	DISABILITY30(BSSApplicationConstants.STD_CODE, "Disability"),
	DISABILITY31(BSSApplicationConstants.LTD_CODE, "Disability"),
	HSA(BSSApplicationConstants.HSA, "HSA"),
	WA(BSSApplicationConstants.WAIVER_ALLOWANCE_PLAN_SUB_TYPE, "Waiver Allowance");

	private String code;
	private String name;

	BenefitsCostEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public static String getCode(String name) {
		for (BenefitsCostEnum value : values()) {
			if (value.name.equals(name)) {
				return value.code;
			}
		}
		throw new IllegalArgumentException("No enum const " + BenefitsCostEnum.class + "@name." + name);
	}

	public static String getName(String code) {
		for (BenefitsCostEnum value : values()) {
			if (value.code.equals(code)) {
				return value.name;
			}
		}
		throw new IllegalArgumentException("No enum const " + BenefitsCostEnum.class + "@code." + code);
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}
}
