package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trinet.ambis.service.CacheService;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;


@RunWith(MockitoJUnitRunner.class)
public class CacheControllerTest extends ServiceUnitTest {

	@InjectMocks
	CacheController cacheController;

	@Mock
	CacheService cacheService;

	@Mock
	HttpServletRequest request;

	@Test
	public void invalidateCache() throws JsonProcessingException {
		String objectType = "ALL";
		String level = "COMPANY";
		String value = "G48";

		when(cacheService.invalidateCache(objectType, level, value)).thenReturn(true);

		ResponseEntity<String> actualResult = cacheController.invalidateCache(request, objectType, level, value);

		assertEquals("Cache invalidated successfully!", actualResult.getBody());
	}

	@Test
	public void invalidateCache_result_false() throws JsonProcessingException {
		String objectType = "ALL";
		String level = "COMPANY";
		String value = "G48";

		when(cacheService.invalidateCache(objectType, level, value)).thenReturn(false);

		ResponseEntity<String> actualResult = cacheController.invalidateCache(request, objectType, level, value);

		assertEquals(
				"No keys found to invalidate the cache or some error occurred, please refer logs. ObjectType : ALL level : COMPANY value : G48",
				actualResult.getBody());
	}

	@Test(expected = Exception.class)
	public void invalidateCache_exception1() throws JsonProcessingException {
		String objectType = "ALL";
		String level = "COMPANY";
		String value = "G48";

		when(cacheService.invalidateCache(objectType, level, value)).thenThrow(new RuntimeException());

		cacheController.invalidateCache(request, objectType, level, value);
	}

}
