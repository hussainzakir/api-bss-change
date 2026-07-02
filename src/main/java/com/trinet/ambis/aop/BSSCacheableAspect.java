package com.trinet.ambis.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.enums.RealmConfigurationKeysEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CacheKeyGenerator;
import com.trinet.ambis.persistence.dao.hrp.RealmConfigurationDao;
import com.trinet.ambis.persistence.model.RealmConfiguration;
import com.trinet.ambis.service.CacheTemplateService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

import lombok.extern.log4j.Log4j2;

/**
 * This class is an aop aspect class which contains the business logic to cache
 * the result of method implementation.
 * 
 * If result is present in the cache then return it from cache otherwise execute
 * the method and store it in cache for future use as well as return it. It also
 * contains the logic to generate the cache key.
 * 
 * @author schaudhari
 *
 */
@Aspect
@Component
@Log4j2
public class BSSCacheableAspect {

	private static final String ERROR_MSG = "Exception occurred while executing %s";

	@Autowired
	private CacheTemplateService cacheTemplateService;

	@Autowired
	RealmConfigurationDao realmConfigurationDao;

	@Around("@annotation(com.trinet.ambis.aop.BSSCacheable)")
	public Object around(ProceedingJoinPoint joinPoint) {
		long startTime = System.currentTimeMillis();
		
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Type returnType = signature.getMethod().getGenericReturnType();
		Method method = signature.getMethod();

		CacheObjectTypeEnum objectType = retrieveCacheObjectType(method);
		String keyUniqueVal = retrieveCacheUniqueId(joinPoint, method);
		String cacheKey = CacheKeyGenerator.generateCacheKey(objectType, keyUniqueVal);

		Object result = null;
		
		if (AppRulesAndConfigsUtils.isCacheDisabled()) {
			result = retrieveFromDB(joinPoint);
			log.info("############## Retrieving from DB. Method :: {} Key :: {}", joinPoint, cacheKey);
		} else {
			result = cacheTemplateService.retrieveFromCache(cacheKey, returnType);
			if (null == result) {
				result = retrieveFromDB(joinPoint);
				cacheTemplateService.storeInCache(cacheKey, result, getTtl(joinPoint, method));
				log.info("############## Retrieving from DB. Method :: {} Key :: {}", joinPoint, cacheKey);
			} else {
				log.info("############## Retrieving from Cache. Method :: {} Key :: {}", joinPoint, cacheKey);
			}
		}

		long timeTaken = System.currentTimeMillis() - startTime;
		log.info("############## Time Taken by method :: {} Key :: {} is :: {}", joinPoint, cacheKey, timeTaken);
		return result;
	}

	private Object retrieveFromDB(ProceedingJoinPoint joinPoint) {
		try {
			return joinPoint.proceed();
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Throwable e) {
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_CACHE_IMPL_ERROR,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, this.getClass().getName(),
							String.format(ERROR_MSG, joinPoint), null, null));
		}
	}

	private String getTtl(ProceedingJoinPoint joinPoint, Method method) {
		String ttl = null;
		BSSCacheable annotation = method.getAnnotation(BSSCacheable.class);
		if (null != annotation && !StringUtils.isEmpty(annotation.ttl())) {
			ttl = annotation.ttl();
		} else {
			String[] cacheKeyArgPosAndVal = getCacheKeyAnnotationPosAndValue(joinPoint, method);
			Object[] args = joinPoint.getArgs();
			int argPosition = Integer.parseInt(cacheKeyArgPosAndVal[0]);
			Object arg = args[argPosition];
			ExpressionParser parser = new SpelExpressionParser();
			Expression exp = parser.parseExpression("realm.id");
			EvaluationContext context = new StandardEvaluationContext(arg);
			Object name = exp.getValue(context);
			if(ObjectUtils.isNotEmpty(name)) {
				List<RealmConfiguration> realmConfigurations = realmConfigurationDao
						.findByIdRealmId(Long.valueOf(name.toString()));
				for (RealmConfiguration realmConfiguration : realmConfigurations) {
					if (RealmConfigurationKeysEnum.CACHE_TTL.getValue().equals(realmConfiguration.getId().getConfigKey())) {
						ttl = realmConfiguration.getConfigValue();
						break;
					}
				}
			}
		}
		if (StringUtils.isEmpty(ttl)) {
			ttl = BSSApplicationConstants.TTL_FOR_CACHE;
		}
		return ttl;
	}

	private CacheObjectTypeEnum retrieveCacheObjectType(Method method) {
		BSSCacheable annotation = method.getAnnotation(BSSCacheable.class);
		return annotation.objectType();
	}

	private String retrieveCacheUniqueId(ProceedingJoinPoint joinPoint, Method method) {
		String result = "";
		String[] cacheKeyArgPosAndVal = getCacheKeyAnnotationPosAndValue(joinPoint, method);
		Object[] args = joinPoint.getArgs();
		int argPosition = Integer.parseInt(cacheKeyArgPosAndVal[0]);
		String expressionString = cacheKeyArgPosAndVal[1];
		Object arg = args[argPosition];
		if (!StringUtils.isEmpty(expressionString)) {
			ExpressionParser parser = new SpelExpressionParser();
			Expression exp = parser.parseExpression(expressionString);
			EvaluationContext context = new StandardEvaluationContext(arg);
			Object id = exp.getValue(context);
			result = id.toString();
		} else {
			result = arg.toString();
		}
		return result;
	}

	private String[] getCacheKeyAnnotationPosAndValue(ProceedingJoinPoint joinPoint, Method method) {
		int count = 0;
		String value = "";
		boolean cachKeyAnnotationFound = false;
		Parameter[] params = method.getParameters();
		if (ArrayUtils.isNotEmpty(params)) {
			for (Parameter param : params) {
				CacheKey cacheKey = param.getAnnotation(CacheKey.class);
				if (null != cacheKey) {
					cachKeyAnnotationFound = true;
					value = cacheKey.value();
					break;
				}
				count++;
			}
		}
		if (!cachKeyAnnotationFound) {
			throw new BSSApplicationException(new RuntimeException(),
					new BSSApplicationError(BSSErrorResponseCodes.BSS_CACHE_IMPL_ERROR,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, this.getClass().getName(),
							String.format(ERROR_MSG, joinPoint), null, null));
		}
		return new String[] { String.valueOf(count), value == null ? "" : value };
	}

}