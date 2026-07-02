package com.trinet.ambis.enums;

import lombok.Getter;

@Getter
public enum MedicalPlanAttributes {

	MEDICAL_SINGLE_DEDUCTIBLE("Single Deductible"), DEDUCTIBLE("Deductible");

	private final String name;

	private MedicalPlanAttributes(String name) {
		this.name = name;
	}

}
