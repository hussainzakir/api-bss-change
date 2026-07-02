package com.trinet.ambis.enums;

import lombok.Getter;

@Getter
public enum RelationEnum {

	CHILD("CH", "Child"), SPOUSE("SP", "Spouse"), DOMESTIC_PARTNER("DP", "Domestic Partner");

	private final String code;

	private final String sfdcCode;

	RelationEnum(String code, String sfdcCode) {
		this.code = code;
		this.sfdcCode = sfdcCode;
	}

}