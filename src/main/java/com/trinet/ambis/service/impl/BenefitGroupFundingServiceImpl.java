
package com.trinet.ambis.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.service.BenefitGroupFundingService;
import com.trinet.ambis.service.prospect.ProspectGroupService;
import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;

@Service
public class BenefitGroupFundingServiceImpl implements BenefitGroupFundingService {

	@Autowired
	ProspectGroupService prospectGroupService;

	@Override
	public void updateBenefitGroupFunding(String companyCode, long strategyId, long groupId,
			List<GroupFundingReq> groupFundingReqs) {
		validateRequest(companyCode, strategyId);
		prospectGroupService.updateGroupFunding(groupId, groupFundingReqs);
	}

	@Override
	public List<GroupFundingRes> getBenefitGroupFunding(String companyCode, long strategyId, long groupId) {
		validateRequest(companyCode, strategyId);
		return prospectGroupService.getGroupFundings(groupId);
	}

	void validateRequest(String companyCode, long strategyId) {
		if (CompanyServiceHelper.isClientCompanyPattern(companyCode)
				|| Long.compare(ProspectConstants.PROSPECT_STRATEGY_ID, strategyId) != 0) {
			throw new BSSApplicationException(
					new BSSApplicationError("This API only supports prospect's current strategy funding."));
		}
	}

}