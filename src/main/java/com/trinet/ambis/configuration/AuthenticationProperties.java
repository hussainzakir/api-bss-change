package com.trinet.ambis.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@lombok.Generated
public class AuthenticationProperties {
	
	@Value("${api.client.id}")
	private String clientId;

	@Value("${api.secret}")
	private String secret;

	@Value("${api.scope}")
	private String scope;

	@Value("${api.cms.auth}")
	private String cmsAuth;

	@Value("${api.cms.apiKey}")
	private String apiKey;

}

