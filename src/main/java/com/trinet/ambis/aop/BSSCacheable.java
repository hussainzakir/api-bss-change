package com.trinet.ambis.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.trinet.ambis.enums.CacheObjectTypeEnum;

/**
 * Annotation indicating that the result of invoking a method can be cached.
 *
 * <p>
 * Each time an advised method is invoked, caching behavior will be applied,
 * checking whether the method has been already invoked for the given key. A
 * parameter annotated with {@link #CacheKey} attribute will be used to generate
 * unique cache key.
 *
 * <p>
 * If no value is found in the cache for the computed key, the target method
 * will be invoked and the returned value stored in the associated cache.
 *
 * @author schaudhari
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BSSCacheable {
	/**
	 * Text to prefix the cache key.
	 */
	CacheObjectTypeEnum objectType();

	/**
	 * time to live value.
	 */
	String ttl() default "";
}