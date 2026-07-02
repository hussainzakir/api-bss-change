/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;

/**
 * @author rvutukuri
 *
 */
public interface PlanMappingDao {

	Map<String, PlanMapping> getPlanMappings(Company company, Set<String> outOfRegionPlans);

	Map<String, PlanMapping> getPrimaryPlanMappings(Company company, Set<String> outOfRegionPlans);

	Map<String, List<String>> getPlanMappingsAsSimpleMap(Company company, Set<String> outOfRegionPlans);

}
