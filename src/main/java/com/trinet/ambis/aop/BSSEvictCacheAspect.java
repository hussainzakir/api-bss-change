package com.trinet.ambis.aop;

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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;

/**
 * This class is an aop aspect class which contains the logic to remove
 * object from redis cache.
 * 
 * @author schaudhari
 *
 */
@Aspect
@Component
@Log4j2
public class BSSEvictCacheAspect {

	private static final String ERROR_MSG = "Exception occurred while executing %s";

	@Autowired
	private CacheTemplateService cacheTemplateService;

	@Autowired
	RealmConfigurationDao realmConfigurationDao;

	@Before("@annotation(com.trinet.ambis.aop.BSSEvictCache)")
	public void before(JoinPoint joinPoint) {
		long startTime = System.currentTimeMillis();
		
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		CacheObjectTypeEnum objectType = retrieveCacheObjectType(method);
		String cacheKey1 = retrieveCacheUniqueId(joinPoint, method);
		String cacheKey = CacheKeyGenerator.generateCacheKey(objectType, cacheKey1);

		if(cacheTemplateService.deleteFromCache(Set.of(cacheKey))) {
			long timeTaken = System.currentTimeMillis() - startTime;
			log.info("############## Time Taken to delete key from cache :: {} Key :: {} is :: {}", joinPoint, cacheKey, timeTaken);
		}
	}

	private String retrieveCacheUniqueId(JoinPoint joinPoint, Method method) {
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

	private String[] getCacheKeyAnnotationPosAndValue(JoinPoint joinPoint, Method method) {
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

	private CacheObjectTypeEnum retrieveCacheObjectType(Method method) {
		BSSEvictCache annotation = method.getAnnotation(BSSEvictCache.class);
		return annotation.objectType();
	}

}