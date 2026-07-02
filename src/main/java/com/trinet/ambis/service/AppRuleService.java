package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.AppRules;

/**
 * @author schaudhari
 *
 */
public interface AppRuleService {

	/**
	 * This method returns all the Rules configured for Application.
	 * 
	 * @return List<AppRules> 
	 */
	List<AppRules> findAll();

}