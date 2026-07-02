package com.trinet.ambis.service;

import java.lang.reflect.Type;
import java.util.Set;

public interface CacheTemplateService {

	/**
	 * This method stores the given object in the cache.
	 * 
	 * @param key
	 * @param value
	 * @param ttl - in minutes
	 * @return
	 */
	boolean storeInCache(String key, Object value, String ttl);

	/**
	 * This method retrieves the object from the cache for given key.
	 * 
	 * @param key
	 * @param returnType
	 * @return
	 */
	Object retrieveFromCache(String key, Type returnType);

	/**
	 * This method deletes the objects from the cache for given keys.
	 * 
	 * @param keys
	 * @return
	 */
	boolean deleteFromCache(Set<String> keys);
}
