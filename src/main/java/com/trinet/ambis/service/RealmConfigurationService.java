package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.RealmConfiguration;

/**
 * @author schaudhari
 *
 */
public interface RealmConfigurationService {

	/**
	 * Returns a List of all {@code RealmConfiguration} for given realmId
	 * 
	 * @param realmId
	 * @return {@code List<RealmConfiguration>}
	 */
	List<RealmConfiguration> findByRealmId(long realmId);

}