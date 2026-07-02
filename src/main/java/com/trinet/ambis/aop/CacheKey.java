package com.trinet.ambis.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that the annotated parameter will be used to generate
 * the unique cache key.
 * 
 * If no value parameter provided then the toString() form of annotated
 * parameter will be used as key.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheKey {
	/**
	 * Spring Expression Language (SpEL) expression for computing the key
	 * dynamically.
	 * <p>
	 * Default is {@code ""}, meaning the toString() form of annotated parameter
	 * will be used as key.
	 * <p>
	 * 
	 */
	String value() default "";
}