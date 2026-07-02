package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.xerial.snappy.Snappy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.service.impl.CacheTemplateServiceImpl;
import com.trinet.security.domain.TypedCacheEntry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class CacheTemplateServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	CacheTemplateServiceImpl cacheTemplateService;
	
	ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private Jedis jedisConnection;
	
	@Mock
	private JedisPool jedisPool;

	private static final String CACHE_TTL = "1200";
	@Before
	public void setUp() {
		when(jedisPool.getResource()).thenReturn(jedisConnection);
	}

	@Test
	public void storeInCache() {
		preTest();
		assertTrue(cacheTemplateService.storeInCache("key", new Long(1), CACHE_TTL));
	}

	@Test
	public void retrieveFromCache() throws Exception {
		preTest();
		// Prepare mock Redis value using utility method
		byte[] compressedValue = wrapAndSerializeForCache("value");
		// Mock Redis call
		when(jedisConnection.get("key".getBytes())).thenReturn(compressedValue);
		// Actual call
		Object result = cacheTemplateService.retrieveFromCache("key", String.class);
		// Assert
		assertEquals("value", result);
	}

	@Test
	public void deleteFromCache() {
		preTest();
		Set<String> keys = new HashSet<>();
		assertTrue(cacheTemplateService.deleteFromCache(keys));
	}
	
	private void preTest() {
		//when(jedisConnection.set(anyString(), anyString())).thenReturn("OK");
		//when(jedisConnection.expire(anyString(), anyLong())).thenReturn(1L);
	}
	
	private byte[] wrapAndSerializeForCache(Object object) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper(); // Use your actual configured one
		TypedCacheEntry typedCacheEntry = TypedCacheEntry.builder().type(object.getClass())
				.value(objectMapper.writeValueAsString(object)).build();
		String wrappedJson = objectMapper.writeValueAsString(typedCacheEntry);
		return Snappy.compress(wrappedJson);
	}
}