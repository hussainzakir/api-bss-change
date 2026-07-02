package com.trinet.ambis.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class FileUtils {

	public void deleteFiles(Path... tempFilePaths) {
		Arrays.asList(tempFilePaths).forEach(this::deleteFile);
	}

	public Path writeToTempFile(byte[] fileContent, String fileExtension, String nameSplitsSeparator,
			String... nameSplits) throws IOException {
		return Files.write(
				Files.createTempFile(buildFileNameWithoutExtension(nameSplitsSeparator, nameSplits), fileExtension),
				fileContent);
	}

	private String buildFileNameWithoutExtension(String nameSplitsSeparator, String... nameSplits) {
		return StringUtils.join(nameSplits, nameSplitsSeparator);
	}

	private void deleteFile(Path tempFilePath) {
		try {
			if (tempFilePath != null) {
				Files.deleteIfExists(tempFilePath);
			}
		} catch (Exception e) {
			log.error(e, e);
			log.error(String.format("Error occured while deleting the file %s", tempFilePath.getFileName()));
		}
	}

	public  <T> Supplier<T> readJsonData(String filePath, TypeReference<T> valueType) {
		return () -> {
			InputStream inputStream = TypeReference.class.getResourceAsStream(filePath);
			try {
				String dto = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
				mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
				return mapper.readValue(dto, valueType);
			} catch (IOException e) {
				log.error("Error while created plan comparison Object...{} ", filePath);
			}
			return null;
		};
	}

	public static String removeSpecialCharacters(String input) {
		return input.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9-_]", "");
	}

}
