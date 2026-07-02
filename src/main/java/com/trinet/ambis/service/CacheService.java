package com.trinet.ambis.service;

import com.trinet.ambis.persistence.model.Company;

public interface CacheService {

	/**
	 * @param company
	 */
	void invalidateOutofDateCache(Company company);

	/**
	 * @param objectType
	 * @param level
	 * @param value
	 * @return
	 */
	boolean invalidateCache(String objectType, String level, String value);
	
	/**
	 * Invalidates strategies related to company id
	 * @param company
	 */
    void invalidateStrategyDataCache(Company company);

}