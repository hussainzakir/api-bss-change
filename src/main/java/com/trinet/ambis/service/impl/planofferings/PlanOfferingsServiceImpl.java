package com.trinet.ambis.service.impl.planofferings;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsReportDetails;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.service.PlanOfferingsRequestBuilder;
import com.trinet.ambis.service.planofferings.PlanOfferingsService;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RestApiClient;

/**
 * 
 * @author smaguluri
 *
 */


@Service
public class PlanOfferingsServiceImpl implements PlanOfferingsService{
	
	@Autowired
	private RestApiClient restApiClient;
	
	@Autowired
	private PlanOfferingsRequestBuilder planOfferingsRequestBuilder;
	
	@Value("${docGenFetchApiUri}")
	private String docGenUrl;


	@Override
	public byte[] generatePlanOfferingsReport(PlanOfferingsRequest planOfferingsRequest,
			HttpServletRequest httpRequest) {

		PlanOfferingsReportDetails reportDetails = planOfferingsRequestBuilder
				.buildPlanOfferingsReportRequest(planOfferingsRequest, httpRequest);
		
		validatePlansExist(reportDetails);

		String url = String.join("/", docGenUrl + BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest),
				BSSSecurityUtils.getAuthenticatedPersonId(), "generate-download");
		return restApiClient.getReturnResponse(httpRequest, reportDetails, url, HttpMethod.POST);

	}

	private void validatePlansExist(PlanOfferingsReportDetails reportDetails) {
		boolean hasValidPlan = reportDetails.getData().getPlanOfferings().values().stream()
				.anyMatch(appendix -> (appendix.getAttributeNames() != null && !appendix.getAttributeNames().isEmpty())
						|| (appendix.getPlanAttributes() != null && !appendix.getPlanAttributes().isEmpty())
						|| (appendix.getAdditionalGroupDetails() != null
								&& !appendix.getAdditionalGroupDetails().isEmpty())
						|| (appendix.getAdditionalGroupAttributeNames() != null
								&& !appendix.getAdditionalGroupAttributeNames().isEmpty()));

		if (!hasValidPlan) {
			throw new BSSApplicationException(new RuntimeException(),
					new BSSApplicationError(BSSErrorResponseCodes.BSS_PLANS_NOT_FOUND, BSSHttpStatusConstants.NOT_FOUND,
							this.getClass().getName(), "No Plans found for the selected criteria", null, null));
		}
	}
}
