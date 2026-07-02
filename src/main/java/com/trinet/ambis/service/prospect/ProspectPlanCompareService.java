package com.trinet.ambis.service.prospect;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;

public interface ProspectPlanCompareService {

	public List<PlanCompareDetailDto> getPlanCompareDetails(Company company, List<Long> trinetStrategyIds,
			HttpServletRequest httpRequest);
}
