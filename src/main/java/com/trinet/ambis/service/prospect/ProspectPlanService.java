package com.trinet.ambis.service.prospect;

import java.util.List;

import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;

public interface ProspectPlanService {

	/**
	 * Fetches the list of benefit plans for passed in prospect id depending on
	 * passed in rateType and includeWithRates
	 *
	 * @param prospectId
	 * @param rateType
	 * @param includeWithRates
	 * @return List<BenefitPlansRes>
	 */
	List<BenefitPlansRes> getBenefitPlansBy(String prospectId, String rateType, boolean includeWithRates);

	/**
	 * Fetches the list of benefit plans for passed in prospect id depending on
	 *
	 * @param prospectId
	 * @return List<BenefitPlansRes>
	 */
	List<BenefitPlansRes> getBenefitPlansBy(String prospectId);
}
