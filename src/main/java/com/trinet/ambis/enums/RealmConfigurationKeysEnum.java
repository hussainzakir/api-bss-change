package com.trinet.ambis.enums;

/**
 * This enum contains keys for RealmConfiguration.
 * 
 * @author schaudhari
 *
 */
public enum RealmConfigurationKeysEnum {

	NEW_TO_RENEWAL_NUM_DAYS("NEW_TO_RENEWAL_NUM_DAYS"),
	CACHE_TTL("CACHE_TTL");

	private final String value;

	private RealmConfigurationKeysEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}