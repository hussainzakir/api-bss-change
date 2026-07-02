package com.trinet.ambis.util;

import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.trinet.ambis.common.ApiBssPropertiesConstants;
import com.trinet.ambis.configuration.AuthenticationProperties;
import com.trinet.common.auth.TriNetAuthUtil;
import com.trinet.security.util.SecurityUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class TokenUtils {
	
	@Autowired
	private AuthenticationProperties authProperties;
	
	/**
	 * Header for the service which has token.
	 * 
	 * @param token
	 * @return HttpHeaders
	 */
	public HttpHeaders getHttpHeaders(String key , String token, String mediaType) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, mediaType);
		headers.add(key, token);
		return headers;
	}
	
	public Function<HttpServletRequest,HttpHeaders> getHeaders(String mediaType){
    	return request -> {
    		HttpHeaders headers = new HttpHeaders();
    		headers.add(HttpHeaders.CONTENT_TYPE, mediaType);
    		headers.add(ApiBssPropertiesConstants.TOKEN, SecurityUtils.parseAuthenticationToken(request));
    		return headers;
    	};
    }
	
	public String getSystemAccountToken() {	
		log.info("ClientId {} Secret {} Scope {}",authProperties.getClientId(), authProperties.getSecret(), authProperties.getScope());
    	return TriNetAuthUtil.getAdminUserSignOnToken(authProperties.getClientId(), authProperties.getSecret(), authProperties.getScope());
	}

	public HttpHeaders getCMSHeader(){
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", authProperties.getCmsAuth());
		headers.set("api_key", authProperties.getApiKey());
		return headers;
	}

}
