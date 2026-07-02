package com.trinet.ambis.service;

import java.util.Date;
import java.util.Map;

/**
 * @author hliddle
 *
 */
public interface PsConfigurationService {

	/**
	 * Returns a Map of all key value PeopleSoft configurations for given
	 * effective date
	 * 
	 * @param effDt
	 * @return {@code Map<String, String>}
	 */
	Map<String, String> findByEffDate(Date effDate);

}