package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.trinet.ambis.service.impl.BenefitGroupFundingServiceImpl;
import com.trinet.ambis.service.prospect.ProspectGroupService;
import com.trinet.ambis.service.prospect.dto.GroupFundingRes;
import com.trinet.ambis.service.prospect.dto.request.GroupFundingReq;

@RunWith(MockitoJUnitRunner.class)
public class BenefitGroupFundingServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	BenefitGroupFundingServiceImpl benefitGroupFundingService;

	@Mock
	ProspectGroupService prospectGroupService;

	/**
	 * given group id </br>
	 * when updateGroupFunding is called </br>
	 * then update the funding details</br>
	 **/
	@Test
	public void updateBenefitGroupFundingTest1() {
		// given
		// data
		List<GroupFundingReq> groupFundingReqs = prepareGroupFundingReq();
		long groupId = 1111;
		long strategyId = 0;
		String companyCode = "a1b2c3";
		// method mocks
		doNothing().when(prospectGroupService).updateGroupFunding(groupId, groupFundingReqs);
		// when
		benefitGroupFundingService.updateBenefitGroupFunding(companyCode, strategyId, groupId, groupFundingReqs);
		// then
		// verify
		verify(prospectGroupService, times(1)).updateGroupFunding(groupId, groupFundingReqs);
	}

	/**
	 * given the company is not a prospect company </br>
	 * when updateBenefitGroupFunding is called </br>
	 * then funding details are not updated</br>
	 **/
	@Test(expected = RuntimeException.class)
	public void updateBenefitGroupFundingTest2() {
		// given
		// data
		List<GroupFundingReq> groupFundingReqs = prepareGroupFundingReq();
		long groupId = 1111;
		long strategyId = 0;
		String companyCode = "D2S";
		// when
		benefitGroupFundingService.updateBenefitGroupFunding(companyCode, strategyId, groupId, groupFundingReqs);
		// then
		// verify
		verify(prospectGroupService, times(0)).updateGroupFunding(groupId, groupFundingReqs);
	}

	/**
	 * given the strategy id is not for a prospect company </br>
	 * when updateBenefitGroupFunding is called </br>
	 * then funding details are not updated</br>
	 **/
	@Test(expected = RuntimeException.class)
	public void updateBenefitGroupFundingTest3() {
		// given
		// data
		List<GroupFundingReq> groupFundingReqs = prepareGroupFundingReq();
		long groupId = 1111;
		long strategyId = 78979;
		String companyCode = "a1b2c3";
		// when
		benefitGroupFundingService.updateBenefitGroupFunding(companyCode, strategyId, groupId, groupFundingReqs);
		// then
		// verify
		verify(prospectGroupService, times(0)).updateGroupFunding(groupId, groupFundingReqs);
	}

	/**
	 * given the company and strategy id is not for a prospect company </br>
	 * when updateBenefitGroupFunding is called </br>
	 * then funding details are not updated and an exception is thrown</br>
	 **/
	@Test(expected = RuntimeException.class)
	public void updateBenefitGroupFundingTest4() {
		// given
		// data
		List<GroupFundingReq> groupFundingReqs = prepareGroupFundingReq();
		long groupId = 1111;
		long strategyId = 78979;
		String companyCode = "D2S";
		// when
		benefitGroupFundingService.updateBenefitGroupFunding(companyCode, strategyId, groupId, groupFundingReqs);
		// then
		// verify
		verify(prospectGroupService, times(0)).updateGroupFunding(groupId, groupFundingReqs);
	}

	/**
	 * given group id </br>
	 * when getBenefitGroupFunding is called </br>
	 * then return the funding details</br>
	 **/
	@Test
	public void getBenefitGroupFundingTest1() {
		// given
		// data
		List<GroupFundingRes> groupFundingRes = prepareGroupFundingRes();
		long groupId = 1111;
		long strategyId = 0;
		String companyCode = "a1b2c3";
		// method mocks
		when(prospectGroupService.getGroupFundings(groupId)).thenReturn(groupFundingRes);
		// when
		List<GroupFundingRes> actualResult = benefitGroupFundingService.getBenefitGroupFunding(companyCode, strategyId,
				groupId);
		// then
		// assertions
		assertTrue(CollectionUtils.isEqualCollection(actualResult, groupFundingRes));
		// verify
		verify(prospectGroupService, times(1)).getGroupFundings(groupId);
	}

	/**
	 * given the company is not a prospect company </br>
	 * when getBenefitGroupFunding is called </br>
	 * then throw exception</br>
	 **/
	@Test(expected = RuntimeException.class)
	public void getBenefitGroupFundingTest2() {
		// given
		// data
		long groupId = 1111;
		long strategyId = 0;
		String companyCode = "D2S";
		// when
		benefitGroupFundingService.getBenefitGroupFunding(companyCode, strategyId, groupId);
		// then
		// verify
		verify(prospectGroupService, times(0)).getGroupFundings(groupId);
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