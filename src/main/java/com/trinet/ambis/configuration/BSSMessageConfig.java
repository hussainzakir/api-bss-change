package com.trinet.ambis.configuration;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * This class loads and provides convenient assess to properties defined in
 * api-bss.properties file. First it will load the property file under the
 * classpath then it will load the property file under
 * MICROSERVICES_CONFIG path. The property file which will be loaded at last
 * will override the properties of previously loaded file.
 * 
 * @author schaudhari
 */
@Service(value = "bssMessageConfig")
@Scope(value = "singleton")
@PropertySources({ @PropertySource("classpath:api-bss.properties"), @PropertySource("classpath:api-bss-outputs.properties"),
		@PropertySource(value = "file:${MICROSERVICES_CONFIG}/api-bss.properties", ignoreResourceNotFound = true) })
public class BSSMessageConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(BSSMessageConfig.class);

	@Autowired
	private Environment environment;

	private static Environment env;

	@PostConstruct
	public void init() {
		env = environment;
	}

	/**
	 * This method returns single property.
	 * 
	 * @param key
	 * @return
	 */
	public static String getProperty(String key) {
		LOGGER.debug("Getting property  {}", key);
		return env.getProperty(key);
	}

	/**
	 * This method returns list of properties.
	 * 
	 * @param key
	 * @return
	 */
	public static List<String> getPropertyAsList(String key) {
		LOGGER.debug("Getting property  {}", key);
		String prop = env.getProperty(key);
		return Arrays.asList(prop != null ? prop.split(",") : null);
	}
}