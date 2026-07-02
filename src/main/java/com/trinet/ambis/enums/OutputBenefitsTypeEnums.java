package com.trinet.ambis.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.service.prospect.exception.NotFoundException;

public enum OutputBenefitsTypeEnums {

	MEDICAL("med", BSSApplicationConstants.MEDICAL_PLAN_TYPE, "Medical"),

	DENTAL("den", BSSApplicationConstants.DENTAL_PLAN_TYPE, "Dental"),

	VISION("vis", BSSApplicationConstants.VISION_PLAN_TYPE, "Vision"),

	LIFE("lad", BSSApplicationConstants.LIFE_CODE, "Life / AD&D"),

	DISABILITY("dis", BSSApplicationConstants.STD_CODE, "Disability");

	private final String name;
	private final String code;
	private final String displayName;
	private static final Map<String, OutputBenefitsTypeEnums> ENUM_MAP;

	private static final String OUTPUTS_BENEFIT_TYPE_NOT_FOUND = "Output benefit type = %s not found.";

	private OutputBenefitsTypeEnums(String name, String code, String displayName) {
		this.name = name;
		this.code = code;
		this.displayName = displayName;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() { return displayName; }

	static {
		Map<String, OutputBenefitsTypeEnums> map = new ConcurrentHashMap<>();
		Stream.of(OutputBenefitsTypeEnums.values()).forEach(type -> map.put(type.getName().toLowerCase(), type));
		ENUM_MAP = Collections.unmodifiableMap(map);
	}

	public static OutputBenefitsTypeEnums get(String name) {
		return ENUM_MAP.get(name.toLowerCase());
	}

	public static String getDisplayNameByCode(String code) {
		Optional<OutputBenefitsTypeEnums> benefitTypeEnum = Arrays.stream(OutputBenefitsTypeEnums.values())
				.filter(value -> value.code.equals(code)).findFirst();
		if (benefitTypeEnum.isPresent()) {
			return benefitTypeEnum.get().getDisplayName();
		} else {
			throw new NotFoundException(String.format(OUTPUTS_BENEFIT_TYPE_NOT_FOUND, code));
		}
	}

	public static String getBenTypeCodeByName(String benefitTypeName) {
		Optional<OutputBenefitsTypeEnums> benefiTypeEnum = Arrays.stream(values())
				.filter(value -> value.name.equals(benefitTypeName)).findFirst();
		if (benefiTypeEnum.isPresent()) {
			return benefiTypeEnum.get().code;
		}
		throw new NotFoundException(String.format(OUTPUTS_BENEFIT_TYPE_NOT_FOUND, benefitTypeName));
	}
	
	public static boolean isMedicalBenefitType(String code) {
		return OutputBenefitsTypeEnums.MEDICAL.getCode().equalsIgnoreCase(code);
	}

	public static boolean isMDVBenefitType(String code) {
		// Returns true if given code is one of Medical, Dental or Vision
		return BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.contains(code);
	}

	public static boolean hasMDVBenefitType(List<String> codes) {
		// Returns true if given codes has at least one of Medical, Dental or Vision
		for (String code : codes) {
			if (BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER.contains(code)){
				return true;
			}
		}
		return false;
	}
	
}
