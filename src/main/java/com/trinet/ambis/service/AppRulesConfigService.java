package com.trinet.ambis.service;

import java.util.Map;

/**
 * @author Saket Chaudhari
 * 
 */
public interface AppRulesConfigService {

	/**
	 * Returns a map of all Rules and Configurations for the Application.
	 * 
	 * @return {@code Map<String, String>}
	 */
	Map<String, String> getAllRulesAndConfigs();

}