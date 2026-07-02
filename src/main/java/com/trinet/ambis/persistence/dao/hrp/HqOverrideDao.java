package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;

public interface HqOverrideDao {
	
	List<HqOverridesDto> getHqOverridesDetails(String companyCode, String quarter);
	
	List<Map<String, Object>> getOverridesPlanYears(String companyCode);
	
	List<CompanyHQData> getHqPlanYearData(String code);
}
