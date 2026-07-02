/**
 * 
 */
package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;

@Service
public interface BenefitGroupFundingService {

	/**
	 * Updates the benefit group funding as per the request object
	 * 
	 * @param companyCode
	 * @param strategyId
	 * @param groupId
	 * @param groupFundingReqs
	 */
	void updateBenefitGroupFunding(String companyCode, long strategyId, long groupId,
			List<GroupFundingReq> groupFundingReqs);

	/**
	 * Returns the benefit group funding based on the group id
	 * 
	 * @param companyCode
	 * @param strategyId
	 * @param groupId
	 * @return List<GroupFundingRes>
	 */
	List<GroupFundingRes> getBenefitGroupFunding(String companyCode, long strategyId, long groupId);

}