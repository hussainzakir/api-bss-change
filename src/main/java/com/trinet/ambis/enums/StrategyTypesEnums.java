/**
 * 
 */
package com.trinet.ambis.enums;

import java.math.BigDecimal;

/**
 * @author rvutukuri
 *
 */
public enum StrategyTypesEnums {
	
	F_S(null, "Current Strategy", "DFLT"),
	EE_PC(new BigDecimal(0L), "Employees Pay Cost Changes", "EEPC"), 
	ER_PC(new BigDecimal(100L), "Company Pays Cost Changes", "ERPC"), 
	S_PC(new BigDecimal(50L), "Share Cost Changes", "SPC");

	private BigDecimal code;
	private String name;
	private String value;

	private static final String NO_ENUM_ERROR = "No enum const ";

	StrategyTypesEnums(BigDecimal code, String name, String value) {
		this.code = code;
		this.name = name;
		this.value = value;

	}

	public static BigDecimal getCode(String name) {
		for (StrategyTypesEnums value : values()) {
			if (value.name.equals(name)) {
				return value.code;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + StrategyTypesEnums.class + "@name." + name);
	}

	public static BigDecimal getCodeByValue(String svalue) {
		for (StrategyTypesEnums value : values()) {
			if (value.value.equals(svalue)) {
				return value.code;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + StrategyTypesEnums.class + "@value." + svalue);
	}

	public static String getName(BigDecimal code) {
		for (StrategyTypesEnums value : values()) {
			if (value.code.compareTo(code) == 0) {
				return value.name;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + StrategyTypesEnums.class + "@code." + code);
	}

	public static String getValue(String name) {
		for (StrategyTypesEnums value : values()) {
			if (value.name.equals(name)) {
				return value.value;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + StrategyTypesEnums.class + "@name." + name);
	}

	public BigDecimal getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}

	public String getValue() {
		return value;
	}

}
