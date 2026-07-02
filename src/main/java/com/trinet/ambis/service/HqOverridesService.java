package com.trinet.ambis.service;
import java.util.List;

import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;

public interface HqOverridesService {

	List<HqOverridesDto> getHqOverridesDetails(String companyCode, String quarter);

	HqOverridesDto createHqOverridesDetails(HqOverridesDto hqOverridesDto);

	List<CompanyHQData> getCompanyPlanYearData(String code);

	void deleteHqOverride(String companyCode, Integer realmYearId);
}