package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;

public interface  CompanyOptionsDao {
	
	Map<String, Boolean> getPackageTypes(long realmYearId, String industryType, String state);

	Map<String, Map<String, List<Long>>> getDefaultPortfolios(long realmYearId,String industryType,
			String state, boolean isPickChoose );

	Map<String, Map<String, List<String>>> getTemplateAdditionalPlans(long realmYearId,
			String industryType,String state);	  

}
