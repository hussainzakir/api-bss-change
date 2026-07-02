package com.trinet.ambis.enums;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sshanbhag
 */
@AllArgsConstructor
@Getter
public enum BSSProcessTypeToSNFeatureMapping {

	// @formatter:off
	SUBMIT("SUBMIT", "Submit"), 
	DEFAULT_SUBMIT("DEFAULT_SUBMIT", "Default Submit"), 
	RESUBMIT("RESUBMIT", "Resubmit"),
	BANDCODE_RESUBMIT("BANDCODE_RESUBMIT", "Band Change Submit"), 
	TERM_DEFAULT("TERM_DEFAULT", "Termed Default Submit"),
	// not used, only to return as a value in get method below
	UNKNOWN("UNKNOWN", "");
	// @formatter:on

	private final String bssProcessType;

	private final String serviceNowFeatureName;

	public static BSSProcessTypeToSNFeatureMapping get(String bssProcessType) {
		return Arrays.asList(BSSProcessTypeToSNFeatureMapping.values()).stream()
				.filter(mapping -> mapping.getBssProcessType().equalsIgnoreCase(bssProcessType)).findFirst()
				.orElse(UNKNOWN);
	}

}
