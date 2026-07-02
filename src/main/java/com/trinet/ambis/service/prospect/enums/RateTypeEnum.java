package com.trinet.ambis.service.prospect.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum RateTypeEnum {

	AGE_BANDED("ageBanded","Age Banded"), FOUR_TIER("4Tier","4Tier"),
    TIERED("tiered","tiered");

	@Getter
	private String code;
	@Getter
	private String type;

}
