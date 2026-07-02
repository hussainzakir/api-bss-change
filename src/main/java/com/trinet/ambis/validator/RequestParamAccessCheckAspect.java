package com.trinet.ambis.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.helper.CompanyServiceHelper;
import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.exception.BssSecurityException;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;

@Aspect
@Component
public class RequestParamAccessCheckAspect {

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private StrategyService strategyService;

	@Autowired
	private StrategyGroupService strategyGroupService;
	
	@Autowired
	private UrlPathHelper urlPathHelper;

	@Before("execution(* com.trinet.ambis.rest.controllers.*.*(.., @StrategyIdValidator (*), ..))")
	public void beforeStrategyIdValidator(JoinPoint joinPoint) {
		List<Long> pathParamStrategyIds = getPathParamValue(joinPoint, StrategyIdValidator.class);
		String companyCode = getRequestedCompany();
		List<Strategy> strategies = strategyService.findBy(companyCode);

		// If the client is a prospect, add 0 to their list of allowed strategy ids
		if (!CompanyServiceHelper.isClientCompanyPattern( companyCode )){
			Strategy strategy = new Strategy();
			strategy.setId(ProspectConstants.PROSPECT_STRATEGY_ID);
			strategies.add(strategy);
		}
		if (!strategies.stream().map(Strategy::getId).collect(Collectors.toList()).containsAll(pathParamStrategyIds)) {
			throw new BssSecurityException(BSSApplicationConstants.FORBIDDEN_EXCEPTION_TEXT);
		}
	}

	@Before("execution(* com.trinet.ambis.rest.controllers.*.*(.., @GroupIdValidator (*), ..))")
	public void beforeGroupIdValidator(JoinPoint joinPoint) {
		List<Long> pathParamBenGroupIds = getPathParamValue(joinPoint, GroupIdValidator.class);
		String requestedCompany = getRequestedCompany();
		if (CompanyServiceHelper.isClientCompanyPattern(requestedCompany)) {
			List<BenefitGroupStrategy> benGroups = strategyGroupService.findBy(getRequestedCompany());
			if (!benGroups.stream().map(BenefitGroupStrategy::getGroupId).collect(Collectors.toList())
					.containsAll(pathParamBenGroupIds)) {
				throw new BssSecurityException(BSSApplicationConstants.FORBIDDEN_EXCEPTION_TEXT);
			}
		}
	}

	private <T extends Annotation> List<Long> getPathParamValue(JoinPoint joinPoint, Class<T> annotationClass) {
	    List<Long> pathParamIds = new ArrayList<>();
	    int paramIndex = 0;
	    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
	    Method method = signature.getMethod();

	    Parameter[] params = method.getParameters();
	    if (ArrayUtils.isNotEmpty(params)) {
	        for (Parameter param : params) {
	            T validationAnnotation = param.getAnnotation(annotationClass);
	            if (validationAnnotation != null) {
	                Object pathParamIdObj = joinPoint.getArgs()[paramIndex];
	                if (pathParamIdObj instanceof String) {
	                    String pathParamIdsStr = (String) pathParamIdObj;
	                    if (pathParamIdsStr.contains(",")) {
	                        pathParamIds.addAll(Arrays.stream(pathParamIdsStr.split(","))
	                                .map(String::trim)
	                                .map(Long::valueOf)
	                                .collect(Collectors.toList()));
	                    } else {
	                        pathParamIds.add(Long.valueOf(pathParamIdsStr.trim()));
	                    }
	                } else if (pathParamIdObj instanceof Long) {
	                    pathParamIds.add((Long) pathParamIdObj);
	                } else if (pathParamIdObj instanceof List) {
	                    List<?> rawList = (List<?>) pathParamIdObj;
	                    pathParamIds = rawList.stream()
	                            .filter(Objects::nonNull)
	                            .map(Object::toString)
	                            .map(Long::valueOf)
	                            .collect(Collectors.toList());
	                } else {
	                    throwException("Data type of the path param to be validated is not supported.", joinPoint);
	                }
	                break;
	            }
	            paramIndex++;
	        }
	    }
	    return pathParamIds;
	}

	private String getRequestedCompany() {
		String requestUri = urlPathHelper.getRequestUri(request);
		int num = requestUri.lastIndexOf('/');
		return requestUri.substring(num + 1, requestUri.length());
	}

	private void throwException(String message, JoinPoint joinPoint) {
		throw new BSSApplicationException(new RuntimeException(message),
				new BSSApplicationError(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, this.getClass().getName(), String.format("", joinPoint),
						null, null));
	}

}
