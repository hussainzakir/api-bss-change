package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.AppConfigurations;

/**
 * @author hliddle
 *
 */
public interface AppConfigurationService {

	/**
	 * This method returns all the Rules configured for Application.
	 * 
	 * @return List<AppConfigurations> 
	 */
	List<AppConfigurations> findAll();

}