package com.trinet.ambis.rest.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.service.CacheService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(URIConstants.VERSION_AND_ROOT)
@Api(value = "Trinet API-BSS Cache Controller")
public class CacheController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

	@Autowired
	CacheService cacheService;

	@DeleteMapping(value = URIConstants.INVALIDATE_CACHE)
	@ApiOperation(value = "Cache will be invalidated", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Cache invalidated successfully") })
	@ResponseBody
	public ResponseEntity<String> invalidateCache(HttpServletRequest request, @RequestParam String objectType,
			@RequestParam String level, @RequestParam String value) {
		LOGGER.info(String.format("In CacheController#invalidateCache objectType : %s level : %s value : %s",
				objectType, level, value));
		boolean result = false;
		try {
			result = cacheService.invalidateCache(objectType, level, value);
		} catch (BSSApplicationException e) {
			throw e;
		} catch (Exception e) {
			String logMsg = "Error occurred while invalidating the cache. ObjectType : " + objectType + " level : "
					+ level + " value : " + value;
			throw new BSSApplicationException(e,
					new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
							BenefitGroupController.class.getName(), logMsg, null, null));
		}
		String returnMsg = null;
		if (result) {
			returnMsg = "Cache invalidated successfully!";
		} else {
			returnMsg = "No keys found to invalidate the cache or some error occurred, please refer logs. ObjectType : "
					+ objectType + " level : " + level + " value : " + value;
		}
		return new ResponseEntity<>(returnMsg, HttpStatus.OK);
	}

}
