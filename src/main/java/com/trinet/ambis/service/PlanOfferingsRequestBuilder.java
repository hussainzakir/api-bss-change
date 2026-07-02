package com.trinet.ambis.service;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsReportDetails;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;

public interface PlanOfferingsRequestBuilder {
	
	public PlanOfferingsReportDetails buildPlanOfferingsReportRequest(PlanOfferingsRequest planOfferingsRequest,
			HttpServletRequest httpRequest);

}
