package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.prospect.dto.BenefitGroupRes;
import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;
import com.trinet.ambis.service.prospect.impl.ProspectGroupServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ProspectServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class ProspectGroupServiceTest extends ServiceUnitTest{

	@InjectMocks
	private ProspectGroupServiceImpl prospectGroupService;

	@Mock
	ProspectServiceRestClient prospectServiceRestClient;
	
	@Captor
	ArgumentCaptor<ProspectApiRequest> prospectApiPostRequestCaptor;

	@Test
	public void deleteBenefitGroupTest() {
		// given
		// data
		long groupId = 111;
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(true);
		// when
		prospectGroupService.deleteBenefitGroup(groupId);
		// then
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(any(ProspectApiRequest.class));
	}

	@Test
	public void updateGroupNameTest() {
		// given
		// data
		long groupId = 111;
		String groupName = "Test Name";
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(true);
		ArgumentCaptor<ProspectApiRequest> argCaptor = ArgumentCaptor.forClass(ProspectApiRequest.class);
		// when
		prospectGroupService.updateGroupName(groupId, groupName);
		// then
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(argCaptor.capture());
		assertEquals(HttpMethod.PUT, argCaptor.getValue().getMethod());
		assertEquals(111L, argCaptor.getValue().getPathParams().get("groupId"));
		assertNull(argCaptor.getValue().getQueryParams());
		assertEquals("/benefit-group/{groupId}/name", argCaptor.getValue().getUri());
		assertEquals("{\"name\":Test Name}", argCaptor.getValue().getRequestBody());
	}

	/**
	 * given group id and group funding request </br>
	 * when updateGroupFunding method is called </br>
	 * then update the funding for the group on prospect service </br>
	 **/
	@Test
	public void updateGroupFundingTest1() {
		// given
		// data
		long groupId = 111;
		List<GroupFundingReq> groupFundingReqs = prepareGroupFundingReq();
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(any(ProspectApiRequest.class)))
				.thenReturn(true);
		ArgumentCaptor<ProspectApiRequest> argCaptor = ArgumentCaptor.forClass(ProspectApiRequest.class);
		// when
		prospectGroupService.updateGroupFunding(groupId, groupFundingReqs);
		// then
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(argCaptor.capture());
		assertEquals(HttpMethod.PUT, argCaptor.getValue().getMethod());
		assertEquals(111L, argCaptor.getValue().getPathParams().get("groupId"));
		assertNull(argCaptor.getValue().getQueryParams());
		assertEquals("/benefit-group/{groupId}/funding", argCaptor.getValue().getUri());
		assertEquals(groupFundingReqs, argCaptor.getValue().getRequestBody());
	}
	
	/**
	 * given group id </br>
	 * when getGroupFundings method is called </br>
	 * then return the group funding details</br>
	 **/
	@Test
	public void getGroupFundingsTest1() {
		// given
		// data
		long groupId = 111;
		List<GroupFundingRes> groupFundingRes = prepareGroupFundingRes();
		ArgumentCaptor<ProspectApiRequest> argCaptor = ArgumentCaptor.forClass(ProspectApiRequest.class);
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(argCaptor.capture())).thenReturn(groupFundingRes);
		// when
		List<GroupFundingRes> actualResult = prospectGroupService.getGroupFundings(groupId);
		// then
		// assertions
		assertEquals(2, actualResult.size());
		assertEquals(groupFundingRes, actualResult);
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(argCaptor.getValue());
	}

	/**
	 * given group id and prospect api call is not successful </br>
	 * when getGroupFundings method is called </br>
	 * then return empty response</br>
	 **/
	@Test
	public void getGroupFundingsTest2() {
		// given
		// data
		long groupId = 111;
		ArgumentCaptor<ProspectApiRequest> argCaptor = ArgumentCaptor.forClass(ProspectApiRequest.class);
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(argCaptor.capture())).thenReturn(null);
		// when
		List<GroupFundingRes> actualResult = prospectGroupService.getGroupFundings(groupId);
		// then
		// assertions
		assertEquals(0, actualResult.size());
		assertTrue(CollectionUtils.isEmpty(actualResult));
		// verify
		verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(argCaptor.getValue());
	}
	
	/**
	 * given prospect id, source group id and group name</br>
	 * when addGroup Method is called</br>
	 * then verify prepareRequestAndCallEndPoint method call is successfull
	 **/
	@Test
	public void addGroupTest1() {
		// given
		// data
		BenefitGroupRes benefitGroupRes = BenefitGroupRes.builder().id(1L).build();
		String prospectId = "0014Z00001IDLTPAAC";
		long sourceGroupId = 2;
		String groupName = "Group1";
		// method mocks
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiPostRequestCaptor.capture()))
				.thenReturn(benefitGroupRes);
		// when
		long actualResult = prospectGroupService.addGroup(prospectId, sourceGroupId, groupName);
		// then
		// assertions
		assertEquals(1L, actualResult);
		ProspectApiRequest prospectApiPostRequest = prospectApiPostRequestCaptor.getValue();
		List<String> prospectIdValues = (List<String>) prospectApiPostRequest.getQueryParams().get("prospectId");
		assertNotNull(prospectIdValues);
		assertEquals(1, prospectIdValues.size());
		assertEquals(prospectId, prospectIdValues.get(0));
		assertEquals("{\"id\" : 2 , \"name\": \"Group1\"}", prospectApiPostRequest.getRequestBody());
		assertEquals(HttpMethod.POST, prospectApiPostRequest.getMethod());
		assertEquals(ProspectURIConstants.BENEFIT_GROUP, prospectApiPostRequest.getUri());
		// verify
		verify(prospectServiceRestClient).prepareRequestAndCallEndPoint(prospectApiPostRequestCaptor.capture());
	}

	private List<GroupFundingReq> prepareGroupFundingReq() {
		return List.of(
				GroupFundingReq.builder().benefitType("10").fundingType("FLT").cvgCodeValues(List.of(
						GroupFundingReq.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(1200.00)).build(),

						GroupFundingReq.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(100.00)).build(),

						GroupFundingReq.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(129.50)).build(),
						GroupFundingReq.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(1106.99)).build()))
						.build(),
				GroupFundingReq.builder().benefitType("11").fundingType("PCT").cvgCodeValues(List.of(
						GroupFundingReq.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(100.78)).build(),

						GroupFundingReq.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(200.45)).build(),

						GroupFundingReq.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(386.19)).build(),
						GroupFundingReq.CvgCodeValue.builder().cvgCode("4").build())).build());
	}
	
	private List<GroupFundingRes> prepareGroupFundingRes() {
		return List.of(
				GroupFundingRes.builder().benefitType("10").fundingType("FLT").cvgCodeValues(List.of(
						GroupFundingRes.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(1200.00)).build(),

						GroupFundingRes.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(100.00)).build(),

						GroupFundingRes.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(129.50)).build(),
						GroupFundingRes.CvgCodeValue.builder().cvgCode("4").value(BigDecimal.valueOf(1106.99)).build()))
						.build(),
				GroupFundingRes.builder().benefitType("11").fundingType("PCT").cvgCodeValues(List.of(
						GroupFundingRes.CvgCodeValue.builder().cvgCode("1").value(BigDecimal.valueOf(100.78)).build(),

						GroupFundingRes.CvgCodeValue.builder().cvgCode("2").value(BigDecimal.valueOf(200.45)).build(),

						GroupFundingRes.CvgCodeValue.builder().cvgCode("C").value(BigDecimal.valueOf(386.19)).build(),
						GroupFundingRes.CvgCodeValue.builder().cvgCode("4").build())).build());
	}
	
}