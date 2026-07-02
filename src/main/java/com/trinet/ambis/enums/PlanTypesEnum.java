package com.trinet.ambis.enums;

import com.trinet.ambis.common.BSSApplicationConstants;

/**
 * @author kpamulapati
 *
 */
public enum PlanTypesEnum {
	MEDICAL(BSSApplicationConstants.MEDICAL_PLAN_TYPE, "medical","13"),
	DENTAL(BSSApplicationConstants.DENTAL_PLAN_TYPE, "dental","1"),
	VISION(BSSApplicationConstants.VISION_PLAN_TYPE, "vision","2"), ADDITIONAL(BSSApplicationConstants.ADDITIONAL_CODE, "additionalBenefit",""),
	DENTAL_VOLUNTARY(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, "DentalVoluntary",""),
	VISION_VOLUNTARY(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, "VisionVoluntary",""), CMTR(BSSApplicationConstants.CMTR_CODE, "CMTR",""),
	LIFE(BSSApplicationConstants.LIFE_CODE, "LIFE","3"), STD(BSSApplicationConstants.STD_CODE, BSSApplicationConstants.DISABILITY,"7"),
	LTD(BSSApplicationConstants.LTD_CODE, BSSApplicationConstants.DISABILITY,"8"), DISABILITY(BSSApplicationConstants.DISABILITY_CODE, BSSApplicationConstants.DISABILITY,"");

	private String code;
	private String name;
	private String sfdcCode;
	
	private static final String NO_ENUM_CONST = "No enum const ";

	PlanTypesEnum(String code, String name, String sfdcCode) {
		this.code = code;
		this.name = name;
		this.sfdcCode = sfdcCode;
	}

	public static String getCode(String name) {
		for (PlanTypesEnum value : values()) {
			if (value.name.equals(name)) {
				return value.code;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_CONST + PlanTypesEnum.class + "@name." + name);
	}

	public static String getName(String code) {
		for (PlanTypesEnum value : values()) {
			if (value.code.equals(code)) {
				return value.name;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_CONST + PlanTypesEnum.class + "@code." + code);
	}
	
	public static String getSfdcCode(String code) {
		for (PlanTypesEnum value : values()) {
			if (value.code.equals(code)) {
				return value.sfdcCode;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_CONST + PlanTypesEnum.class + "@code." + code);
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}
	
	public String getSfdcCode() {
		return sfdcCode;
	}

	public static synchronized PlanTypesEnum planType(String code) {
		switch (code) {
		case BSSApplicationConstants.MEDICAL_PLAN_TYPE:
			return PlanTypesEnum.MEDICAL;
		case BSSApplicationConstants.DENTAL_PLAN_TYPE:
			return PlanTypesEnum.DENTAL;
		case BSSApplicationConstants.VISION_PLAN_TYPE:
			return PlanTypesEnum.VISION;
		case BSSApplicationConstants.ADDITIONAL_CODE:
			return PlanTypesEnum.ADDITIONAL;
		case BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE:
			return PlanTypesEnum.DENTAL_VOLUNTARY;
		case BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE:
			return PlanTypesEnum.VISION_VOLUNTARY;
		case BSSApplicationConstants.CMTR_CODE:
			return PlanTypesEnum.CMTR;
		case BSSApplicationConstants.LIFE_CODE:
			return PlanTypesEnum.LIFE;
		case BSSApplicationConstants.STD_CODE:
			return PlanTypesEnum.STD;
		case BSSApplicationConstants.LTD_CODE:
			return PlanTypesEnum.LTD;
		case BSSApplicationConstants.DISABILITY:
			return PlanTypesEnum.DISABILITY;
		default:
			throw new IllegalArgumentException(code);
		}
	}

}
