package com.trinet.ambis.service.prospect.service;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;

public interface ProspectBenefitsPlansRatesService {

	Map<String, RateDetail> getBenefitsPlansRateDetails(List<String> benefitsPlanIds, String prospectId);

}
