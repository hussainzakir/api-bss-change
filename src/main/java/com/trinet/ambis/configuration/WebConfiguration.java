/**
 * 
 */
package com.trinet.ambis.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.interceptor.BssSecurityInterceptor;
import com.trinet.ambis.interceptor.StrategySyncEventInterceptor;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

	@Bean
	public BssSecurityInterceptor bssSecurityInterceptor() {
		return new BssSecurityInterceptor();
	}
	
	@Bean
	public StrategySyncEventInterceptor strategySyncEventInterceptor() {
		return new StrategySyncEventInterceptor();
	}

	private List<String> excludePaths = Arrays.asList(URIConstants.VERSION_AND_PLATFORM + "**", "/docs",
			"/docs/swagger-ui/index.html", "/docs/swagger-ui/**", "/swagger-resources/**", "/docs/swagger-resources/**",
			"/api-docs", "/docs/v2/api-docs", "/v2/api-docs", URIConstants.VERSION_AND_ROOT + URIConstants.BSS_STATUS);

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(bssSecurityInterceptor()).addPathPatterns("/**").excludePathPatterns(excludePaths)
				.order(Ordered.HIGHEST_PRECEDENCE);
		registry.addInterceptor(strategySyncEventInterceptor()).addPathPatterns("/**")
				.excludePathPatterns(excludePaths);
	}
}
