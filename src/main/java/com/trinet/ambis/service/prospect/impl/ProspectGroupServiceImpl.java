package com.trinet.ambis.service.prospect.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.service.prospect.ProspectGroupService;
import com.trinet.ambis.service.prospect.dto.BenefitGroupRes;
import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

@Service
public class ProspectGroupServiceImpl implements ProspectGroupService {

	@Autowired
	ProspectServiceRestClient prospectServiceRestClient;

	@Override
	public void deleteBenefitGroup(long groupId) {
		deleteProspectBenefitGroup(groupId);
	}

	private void deleteProspectBenefitGroup(long groupId) {
		Map<String, Long> map = new HashMap<>();
		map.put("groupId", groupId);
		ParameterizedTypeReference<ReturnResponse<Object>> deleteBenefitGroupBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<Object> prospectApiDeleteRequest = ProspectApiRequest.<Object>builder().method(HttpMethod.DELETE)
				.uri(ProspectURIConstants.BENEFIT_GROUP_ID_PARAM).pathParams(map)
				.parameterizedTypeReference(deleteBenefitGroupBean).build();
		prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiDeleteRequest);
	}

	@Override
	public void updateGroupName(long groupId, String name) {
		Map<String, Long> map = new HashMap<>();
		map.put("groupId", groupId);
		ParameterizedTypeReference<ReturnResponse<Object>> updateBenefitGroupNameBean = new ParameterizedTypeReference<>() {
		};
		String groupNameReqJson = "{\"name\":" + name + "}";
		ProspectApiRequest<Object> prospectApiPutRequest = ProspectApiRequest.<Object>builder().method(HttpMethod.PUT)
				.uri(ProspectURIConstants.BENEFIT_GROUP_NAME).pathParams(map).requestBody(groupNameReqJson)
				.parameterizedTypeReference(updateBenefitGroupNameBean).build();
		prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiPutRequest);
	}

	@Override
	public long addGroup(String prospectId, long sourceGroupId, String groupName) {
		MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		requestParams.add(ProspectConstants.PROSPECT_ID_REQ_PARAM, prospectId);
		ParameterizedTypeReference<ReturnResponse<BenefitGroupRes>> addBenefitGroupBean = new ParameterizedTypeReference<>() {
		};
		String addGroupReqJson = "{\"id\" : " + sourceGroupId + " , \"name\": \"" + groupName + "\"}";
		ProspectApiRequest<BenefitGroupRes> prospectApiPostRequest = ProspectApiRequest.<BenefitGroupRes>builder()
				.method(HttpMethod.POST)
				.uri(ProspectURIConstants.BENEFIT_GROUP).queryParams(requestParams).requestBody(addGroupReqJson)
				.parameterizedTypeReference(addBenefitGroupBean).build();
		BenefitGroupRes benefitGroupRes = prospectServiceRestClient
				.prepareRequestAndCallEndPoint(prospectApiPostRequest);
		return benefitGroupRes.getId();
	}

	@Override
	public void updateGroupFunding(long groupId, List<GroupFundingReq> groupFundingReqs) {
		Map<String, Long> map = new HashMap<>();
		map.put("groupId", groupId);
		ParameterizedTypeReference<ReturnResponse<Object>> updateBenefitGroupFundingBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<Object> prospectApiPutRequest = ProspectApiRequest.<Object>builder().method(HttpMethod.PUT)
				.uri(ProspectURIConstants.GROUP_FUNDING).pathParams(map).requestBody(groupFundingReqs)
				.parameterizedTypeReference(updateBenefitGroupFundingBean).build();
		prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiPutRequest);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GroupFundingRes> getGroupFundings(long groupId) {
		Map<String, Long> map = new HashMap<>();
		map.put("groupId", groupId);
		ParameterizedTypeReference<ReturnResponse<Object>> getBenefitGroupFundingBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<Object> prospectApiGetRequest = ProspectApiRequest.<Object>builder().method(HttpMethod.GET)
				.uri(ProspectURIConstants.GROUP_FUNDING).pathParams(map)
				.parameterizedTypeReference(getBenefitGroupFundingBean).build();
		Object response = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		if (response != null) {
			return (List<GroupFundingRes>) response;
		} else {
			return Collections.emptyList();
		}
	}

}