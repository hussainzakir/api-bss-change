package com.trinet.ambis.service;

import java.util.Date;
import java.util.Map;

import com.trinet.ambis.persistence.model.Company;

/**
 * @author Saket Chaudhari
 * 
 */
public interface RealmPlanYearRuleConfigService {

	/**
	 * Returns a map of all {@code RealPlanYearRule} and
	 * {@code RealPlanYearConfiguration} for given Company
	 * 
	 * @param company
	 * @return {@code Map<String, String>}
	 */
	Map<String, String> getRulesAndConfigsByRealmPlanYearId( Company company );
	

	/**
	 * Returns a map of all {@code RealPlanYearRule} and
	 * {@code RealPlanYearConfiguration} for given RealmPlanYearId
	 * 
	 * @param realmPlanYearId
	 * @return {@code Map<String, String>}
	 */
	Map<String, String> getRulesAndConfigsByRealmPlanYearId(long realmPlanYearId);
	

	/**
	 * Returns a key,value map of all PS configurations for the passed in date
	 * 
	 * @param effDate
	 * @return {@code Map<String, String>}
	 */
	Map<String, String> getPsConfigsByDate(Date effDate);


	boolean findPickChooseWithExceptions(long realmYearId, String companyCode, Date effdt);

}