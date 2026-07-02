package com.trinet.ambis.service.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.service.CacheTemplateService;
import com.trinet.security.domain.TypedCacheEntry;


import java.lang.reflect.Type;
import lombok.extern.log4j.Log4j2;
import org.xerial.snappy.Snappy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Log4j2
public class CacheTemplateServiceImpl implements CacheTemplateService {

	@Autowired
	@Qualifier("jedisPool")
	private JedisPool jedisPool;

	private static ObjectMapper objectMapper = new ObjectMapper();
	
	private Jedis getJedisConnection() {
		return this.jedisPool.getResource();
	}

	@Override
	public boolean storeInCache(String key, Object value, String ttl) {
		log.info("Cacheable Key (storeInCache): {}", key);
		boolean storedStatus = false;
		try (Jedis jedisConnection = this.getJedisConnection()) {
			String valueAsString = wrapAndSerializeForCache(value);
			byte[] compressed = Snappy.compress(valueAsString);
			jedisConnection.set(key.getBytes(), compressed);
			if (StringUtils.isNotEmpty(ttl)) {
				jedisConnection.expire(key.getBytes(), Math.multiplyExact(Long.valueOf(ttl), 60));
			}
			storedStatus = true;
		} catch (Exception e) {
			log.error("Error while storing the object in cache, Key = {}, Exception = {} ", key, e);
		}
		return storedStatus;
	}

	@Override
	public Object retrieveFromCache(String key, Type returnType) {
		Object response = null;
		try (Jedis jedisConnection = this.getJedisConnection()) {
			log.info("Cacheable Key (getFromCache): Key = {}", key);
			byte[] compressed = jedisConnection.get(key.getBytes());
			if (compressed != null && compressed.length > 0) {
				String decompressed = Snappy.uncompressString(compressed);
				response = deserializeAndUnwrapTypedCacheEntry(decompressed, returnType);
			}
		} catch (Exception e) {
			log.error("Error while getting from cache, Key = {}, Exception = {} ", key, e);
		}
		return response;
	}

	@Override
	public boolean deleteFromCache(Set<String> keys) {
		boolean deleteStatus = false;
		try (Jedis jedisConnection = this.getJedisConnection()) {
			log.info("Cacheable Key (deleteFromCache): Key = {}", keys);
			byte[][] byteKeys = keys.stream().map(String::getBytes).toArray(byte[][]::new);
			jedisConnection.unlink(byteKeys);
			deleteStatus = true;
		} catch (Exception e) {
			log.error("Error while deleting from cache, Key = {}, Exception = {} ", keys, e);
		}
		return deleteStatus;
	}

	/**
	 * Serializes an object into a JSON string suitable for caching.
	 * <p>
	 * This method wraps the given object in a {@code TypedCacheEntry}, which
	 * includes the object's runtime class and its serialized JSON representation.
	 * The resulting {@code TypedCacheEntry} is then serialized into a JSON string.
	 * </p>
	 *
	 * @param object the object to be cached
	 * @return a JSON string representing the typed cache entry
	 * @throws JsonProcessingException if the object or the cache entry cannot be
	 *                                 serialized
	 */
	private String wrapAndSerializeForCache(Object object) throws JsonProcessingException {
		TypedCacheEntry typedCacheEntry = TypedCacheEntry.builder().type(object.getClass())
				.value(objectMapper.writeValueAsString(object)).build();
		return objectMapper.writeValueAsString(typedCacheEntry);
	}

	/**
	 * Deserializes a JSON string retrieved from the cache back into its original
	 * object.
	 * <p>
	 * This method expects the input JSON to represent a {@code TypedCacheEntry},
	 * which contains both the serialized object and its original type. It first
	 * deserializes the cache entry, then uses the stored type to reconstruct the
	 * original object.
	 * </p>
	 *
	 * @param response   the JSON string retrieved from the cache
	 * @param returnType the return type of method
	 * @return the original object reconstructed from the cache entry
	 * @throws JsonProcessingException if deserialization fails
	 */
	private Object deserializeAndUnwrapTypedCacheEntry(String response, Type returnType)
			throws JsonProcessingException {
		TypedCacheEntry typedCacheEntry = objectMapper.readValue(response, TypedCacheEntry.class);
		JavaType javaType = objectMapper.getTypeFactory().constructType(returnType);
		return objectMapper.readValue(typedCacheEntry.getValue(), javaType);
	}
	
}
