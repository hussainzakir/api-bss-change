package com.trinet.ambis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RealTimeSyncServiceStatusEnum {

	// @formatter:off
    STATUS_PROCESSED("P"), 
    STATUS_FAILED("F"), 
    STATUS_NORECORDS("NR");
	// @formatter:on

	private final String code;

}
