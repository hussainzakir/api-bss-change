package com.trinet.ambis.service.prospect.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

import com.trinet.ambis.service.prospect.exception.NotFoundException;

@Getter
public enum BenefitTypeEnum {

	MEDICAL("medical", "10", 1, "Medical"), DENTAL("dental", "11", 2, "Dental"), VISION("vision", "14", 3, "Vision");

	private final String benTypeDesc;
	private final String benTypeCode;
	// Refer Table BenefitsCommon.Benefit_Type
	private final Integer bcrBenTypeCode;
	private final String bcrBenTypeDesc;

	private static final String BCR_BENEFIT_TYPE_DESC_NOT_FOUND = "Bcr benefit type desc = %s not found.";

	BenefitTypeEnum(String benTypeDesc, String benTypeCode, Integer bcrBenTypeCode, String bcrBenTypeDesc) {
		this.benTypeDesc = benTypeDesc;
		this.benTypeCode = benTypeCode;
		this.bcrBenTypeCode = bcrBenTypeCode;
		this.bcrBenTypeDesc = bcrBenTypeDesc;
	}

	public static String getBenTypeCodeFromBcrBenTypeDesc(String bcrBenTypeDesc) {
		Optional<BenefitTypeEnum> benefitTypeEnum = Arrays.stream(BenefitTypeEnum.values())
				.filter(value -> value.bcrBenTypeDesc.equals(bcrBenTypeDesc)).findFirst();
		if (benefitTypeEnum.isPresent()) {
			return benefitTypeEnum.get().getBenTypeCode();
		} else {
			throw new NotFoundException(String.format(BCR_BENEFIT_TYPE_DESC_NOT_FOUND, bcrBenTypeDesc));
		}
	}

	public static String getBcrBenTypeDescFromBenTypeCode(String benTypeCode) {
		Optional<BenefitTypeEnum> benefitTypeEnum = Arrays.stream(BenefitTypeEnum.values())
				.filter(value -> value.benTypeCode.equals(benTypeCode)).findFirst();
		if (benefitTypeEnum.isPresent()) {
			return benefitTypeEnum.get().getBcrBenTypeDesc();
		} else {
			throw new NotFoundException(String.format(BCR_BENEFIT_TYPE_DESC_NOT_FOUND, benTypeCode));
		}
	}
}