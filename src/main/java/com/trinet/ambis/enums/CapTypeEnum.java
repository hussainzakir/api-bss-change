package com.trinet.ambis.enums;

import lombok.Getter;

@Getter
public enum CapTypeEnum {

	LIMIT_PLAN("limitplan"), DOLLAR("dollar");

	private final String capType;

	CapTypeEnum(String capType) {
		this.capType = capType;
	}

}