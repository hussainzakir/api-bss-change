package com.trinet.ambis.util;

import java.util.List;

import com.trinet.ambis.service.FeatureFlagService;
import com.trinet.ambis.service.model.FeatureFlag;

public final class FeatureFlagUtils {
	private static final String BSS_YEAR_ROUND = "BSS_YEAR_ROUND";

	private static FeatureFlagService featureFlagService;

	private FeatureFlagUtils() {
	}

	public static void setFeatureFlagService(FeatureFlagService service) {
		FeatureFlagUtils.featureFlagService = service;
	}

	public static boolean isBssYearAround(final String companyCode, long realmYrId) {
		return retrieveFlagValueBy(companyCode, BSS_YEAR_ROUND, realmYrId);
	}

	private static boolean retrieveFlagValueBy(String companyCode, String flagKey, long realmYrId) {
		boolean result = false;
		List<FeatureFlag> featureFlags = featureFlagService.retrieveFeatureFlags(companyCode, realmYrId);
		for (FeatureFlag featureFlag : featureFlags) {
			if (flagKey.equals(featureFlag.getKey())) {
				result = featureFlag.isValue();
			}
		}
		return result;
	}

}
