package com.trinet.ambis.service.planofferings;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.rest.controllers.dto.planofferings.CarrierData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsData;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;

public interface PlanOfferingsReportDataService {

	PlanOfferingsData preparePlanOfferingsData(PlanOfferingsRequest prospectRequest, HttpServletRequest httpRequest);

	List<CarrierData> getCarriersBy(String reportCode, String quarter, Date effDt, String hqState, Optional<String> hqZipCode, String benefitType);

}
