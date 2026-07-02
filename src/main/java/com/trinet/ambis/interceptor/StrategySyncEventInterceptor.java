package com.trinet.ambis.interceptor;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.service.ProcessStatusService;

/**
 * Intercepts requests after API security checks and prevents forwarding to the controller
 * if a strategy sync events are currently in process for the company.
 */
@Component
public class StrategySyncEventInterceptor implements HandlerInterceptor {

	@Autowired
	private ProcessStatusService processStatusService;

	final List<String> excludedPaths = Arrays.asList("/exchange-bands", "/exchange-carriers", "/prospect/census",
			"/prospect/tib/dependent", "/prospect-data", "/rate-update", "/bundles/plans");

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String requestUri = urlPathHelper().getRequestUri(request);
		if (excludedPaths.stream().anyMatch(requestUri::contains)) {
			return true;
		}
		int num = requestUri.lastIndexOf('/');
		String urlCompanyCode = requestUri.substring(num + 1, requestUri.length());
		String processStatusFlagForBands = processStatusService.findStrategySyncProcessStatus(urlCompanyCode);
		if (!StringUtils.isEmpty(processStatusFlagForBands)) {
			throw new BSSApplicationException(new BSSApplicationError(
					BSSErrorResponseCodes.BSS_STRATEGY_SYNC_EVENT_IN_PROCESS,
					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategySyncEventInterceptor.class.getName(),
					"Prices and strategy estimates are being updated. The BSS should be available momentarily, refresh this page to try again.",
					null, null));
		}

//		Commenting out this check for bundles as part of phase 1 since bundle sync events are not being triggered at this time.
//		This will be re-enabled when bundle sync events are implemented and triggered.
//        boolean processStatusFlagForBundles = processStatusService.findBssCoreProcessStatus(urlCompanyCode);
//        if (!processStatusFlagForBundles) {
//			throw new BSSApplicationException(new BSSApplicationError(
//					BSSErrorResponseCodes.BSS_BUNDLE_SYNC_EVENT_IN_PROCESS,
//					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategySyncEventInterceptor.class.getName(),
//					"Bundle selections are being processed. The BSS should be available momentarily, refresh this page to try again.",
//					null, null));
//		}

		return true;
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