package com.trinet.ambis.service.planofferings;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;

public interface PlanOfferingsService {

	byte[] generatePlanOfferingsReport(PlanOfferingsRequest planOfferingsRequest, HttpServletRequest httpRequest);

}
