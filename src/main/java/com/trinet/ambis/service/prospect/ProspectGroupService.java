package com.trinet.ambis.service.prospect;

import java.util.List;

import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;

public interface ProspectGroupService {

	/**
	 * Delete Benefit group
	 *
	 * @param groupId
	 */
	void deleteBenefitGroup(long groupId);

	/**
	 * Update prospect current group by group id
	 * 
	 * @param groupId
	 * @param newName
	 */
	void updateGroupName(long groupId, String newName);

	/**
	 * Update prospect current group's funding by group id
	 *
	 * @param groupId
	 * @param groupFundingReqs
	 */
	void updateGroupFunding(long groupId, List<GroupFundingReq> groupFundingReqs);

	/**
	 * Fetches the current group's fundings by group id
	 * 
	 * @param groupId
	 * @return List<GroupFundingRes>
	 */
	List<GroupFundingRes> getGroupFundings(long groupId);

	/**
	 * Calls prospect add group api to create the group for prospect
	 * 
	 * @param prospectId
	 * @param sourceGroupId
	 * @param groupName
	 * @return
	 */
	long addGroup(String prospectId, long sourceGroupId, String groupName);
	
}
