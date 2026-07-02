package com.trinet.ambis.service;

import java.util.Date;
import java.util.List;

import com.trinet.ambis.persistence.model.RealmPlanYearRule;

/**
 * @author schaudhari
 *
 */
public interface RealmPlanYearRuleService {

	/**
	 * Returns a List of all RealmPlanYearRule for given RealmPlanYearId
	 * 
	 * @param realmPlanYearId
	 * @return {@code List<RealmPlanYearRule>}
	 */
	List<RealmPlanYearRule> findByRealmPlanYearId(long realmPlanYearId);

	/**
	 * Returns a boolean indicating whether the company belongs to pick & choose
	 * exchange and also considers whether the company has been granted an exception.
	 * @param 
	 * @param 
	 * @param 
	 * @return true if client is pick & choose, otherwise false
	 */
	boolean findPickChooseWithExceptions( long realmYearId, String companyCode, Date effdt );
}
