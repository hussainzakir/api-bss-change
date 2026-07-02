package com.trinet.ambis.service.outputs;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendix;

public interface PlanAppendixService {

    /**
     * This method returns the data required to generate the plan appendix report
     *
	 * @param company
	 * @param prospectRequest
	 * @param httpRequest
	 * @param additionalBenefitPlanCompareDataFuture
	 * @return
	 */
	Map<String, PlanAppendix> getPlanAppendixData(Company company, OutputRequest prospectRequest,
			HttpServletRequest httpRequest,CompletableFuture<Map<String, BasePlanComparison>> additionalBenefitPlanCompareDataFuture);

}






