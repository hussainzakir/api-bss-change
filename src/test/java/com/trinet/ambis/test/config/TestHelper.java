package com.trinet.ambis.test.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestHelper {

	public static <T> Supplier<T> readPlanComparisonRequest(String filePath, TypeReference<T> valueType) {
		return () -> {
			ClassPathResource staticDataResource = new ClassPathResource(filePath);
			try {
				String dto = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				mapper.setVisibility(
						VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
				return mapper.readValue(dto, valueType);
			} catch (IOException e) {
				log.error("Error while created plan comparison Object...{} ", filePath);
			}
			return null;
		};
	}
	
}
