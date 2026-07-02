package com.trinet.ambis.service;

import java.util.List;

import com.trinet.ambis.persistence.model.RealmPlanYearConfiguration;

/**
 * @author schaudhari
 *
 */
public interface RealmPlanYearConfigurationService {

	/**
	 * Returns a List of all {@code RealmPlanYearConfiguration} for given
	 * RealmPlanYearId
	 * 
	 * @param realmPlanYearId
	 * @return {@code List<RealmPlanYearConfiguration>}
	 */
	List<RealmPlanYearConfiguration> findByRealmPlanYearId(long realmPlanYearId);

}