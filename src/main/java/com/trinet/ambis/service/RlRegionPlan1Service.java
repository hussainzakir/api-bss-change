package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

public interface RlRegionPlan1Service {

	/**
	 * This method returns a map with benefit plan as key and list of its associated
	 * regions as value
	 * 
	 * @param realmPlanYearId
	 * @return Map<String, List<String>>
	 */
	Map<String, List<String>> findByRealmPlanYearId(long realmPlanYearId);

}
