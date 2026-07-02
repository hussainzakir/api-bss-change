package com.trinet.ambis.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BssSecurityException;
import com.trinet.ambis.util.BSSSecurityUtils;

/**
 * This interceptor intercepts the request after the api-security check is done
 * and forward the request to controller only if the use has access to requested
 * company.
 * 
 * @author schaudhari
 *
 */
@Component
public class BssSecurityInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		boolean authorize = false;
		String authenticatedCompanyCode = BSSSecurityUtils.getAuthenticatedCompanyCode(request);
		String requestUri = urlPathHelper().getRequestUri(request);
		int num = requestUri.lastIndexOf('/');
		String urlCompanyCode = requestUri.substring(num + 1, requestUri.length());
		if (BSSApplicationConstants.TRINET_COMPANIES.contains(authenticatedCompanyCode)) {
			authorize = true;
		} else if (StringUtils.isEmpty(urlCompanyCode)) {
			throw new BssSecurityException("company code is missing in URI");
		} else if (authenticatedCompanyCode.equals(urlCompanyCode)) {
			authorize = true;
		} else {
			throw new BssSecurityException(BSSApplicationConstants.FORBIDDEN_EXCEPTION_TEXT);
		}
		return authorize;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Bean
	public UrlPathHelper urlPathHelper() {
		return new UrlPathHelper();
	}

}