package com.trinet.ambis.aop;

import com.trinet.ambis.enums.CacheObjectTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that the data from cache will be evicted for given CacheKey.
 *
 * @author schaudhari
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BSSEvictCache {
	/**
	 * Text to prefix the cache key.
	 */
	CacheObjectTypeEnum objectType();

}