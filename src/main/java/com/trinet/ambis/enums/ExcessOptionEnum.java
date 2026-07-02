/**
 * 
 */
package com.trinet.ambis.enums;

/**
 * @author rvutukuri
 *
 */
public enum ExcessOptionEnum {

	CASH(1, "C", "Apply to paycheck as taxable wages"),
	FORFEIT(2, "F", "Apply towards the cost of other Trinet plans (select all apply))"),
	OTHER(3, "F", "Retain surplus amounts");

	private final int type;
	private String code;
	private String name;

	ExcessOptionEnum(int type, String code, String name) {
		this.type = type;
		this.code = code;
		this.name = name;
	}

	public static String getName(String code) {
		for (ExcessOptionEnum value : values()) {
			if (value.code.equals(code)) {
				return value.name;
			}
		}
		throw new IllegalArgumentException("No enum Name " + ExcessOptionEnum.class + "@code." + code);
	}

	public static int getType(String code) {
		for (ExcessOptionEnum value : values()) {
			if (value.code.equals(code)) {
				return value.type;
			}
		}
		throw new IllegalArgumentException("No enum Type " + ExcessOptionEnum.class + "@code." + code);
	}

	public static String getCode(int type) {
		for (ExcessOptionEnum value : values()) {
			if (value.type == type) {
				return value.code;
			}
		}
		throw new IllegalArgumentException("No enum Code " + ExcessOptionEnum.class + "@type." + type);
	}

	public int getType() {
		return this.type;
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}

}
