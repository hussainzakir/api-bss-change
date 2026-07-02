package com.trinet.ambis.authorization;

import org.springframework.http.HttpHeaders;

import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.common.auth.TriNetAuthUtil;

public class BenefitsBatchAuthorization {

	private BenefitsBatchAuthorization() {
	}

	private static final String PROPERTY_BENEFITS_BATCH_CLIENT_ID = "benefits.batch.client.id";
	private static final String PROPERTY_BENEFITS_BATCH_CLIENT_SECRET = "benefits.batch.client.secret";
	private static final String PROPERTY_BENEFITS_BATCH_SCOPE = "api.security.benefits.batch.scope";

	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String HEADER_SCOPE = "scope";

	public static void addAuthHeaders(HttpHeaders headers) {
		headers.add(HEADER_AUTHORIZATION,
				TriNetAuthUtil.getAuthorization(BSSMessageConfig.getProperty(PROPERTY_BENEFITS_BATCH_CLIENT_ID),
						BSSMessageConfig.getProperty(PROPERTY_BENEFITS_BATCH_CLIENT_SECRET)));
		headers.add(HEADER_SCOPE, BSSMessageConfig.getProperty(PROPERTY_BENEFITS_BATCH_SCOPE));
	}

}
