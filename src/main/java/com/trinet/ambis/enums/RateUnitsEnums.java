package com.trinet.ambis.enums;

public enum RateUnitsEnums {

	PER_HUNDRED("PHUN", "$100 of covered payroll"),
	PER_THOUSAND("PTHO", "$1,000 of covered payroll"), 
	NONE("NONE", "Amount per benefits eligible worksite employee");

	private String code;
	private String description;

	private RateUnitsEnums(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public static String valueOfDescription(String code) {
		for (RateUnitsEnums value : values()) {
			if (value.code.equals(code)) {
				return value.description;
			}
		}
		throw new IllegalArgumentException("No enum const " + RateUnitsEnums.class + "@code." + code);
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {
		return this.description;
	}

}
