package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitGroupHeadCountService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.model.GroupData;
import com.trinet.ambis.service.model.HeadCountData;
import com.trinet.ambis.service.model.Response;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.validator.RequestValidator;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;


@RunWith(MockitoJUnitRunner.class)
public class BenefitGroupControllerTest extends ServiceUnitTest {

	@InjectMocks
	BenefitGroupController benefitGroupController;

	@Mock
	CompanyService companyService;

	@Mock
	BenefitGroupService benefitGroupService;

	@Mock
	StrategyService strategyService;

	@Mock
	BenefitGroupHeadCountService benefitGroupHeadCountService;

	private static final String EMPLID = "0000000123456";
    private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
    private MockedStatic<RequestValidator> mockStaticRequestValidator;

	@Before
	public void setUp() {
        mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
        mockStaticRequestValidator = Mockito.mockStatic(RequestValidator.class);

        mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
	}

    @After
    public void tearDown() {
        mockStaticBSSSecurityUtils.close();
        mockStaticRequestValidator.close();
    }

	@Test
	public void addGroup() {

		HttpServletRequest request;
		GroupData groupData = new GroupData();
		groupData.setDestGroupName("GROUP_NAME");
		long strategyId = 1000L;
		String companyCode = "TEST";
		long groupId = 1;
		Response actualResult;
		Company company;

		request = Mockito.mock(HttpServletRequest.class);
		company = null;
		when(RequestValidator.getValidatedGroupName(groupData.getDestGroupName())).thenReturn(groupData.getDestGroupName());		

		actualResult = benefitGroupController.addGroup(request, groupData, strategyId, companyCode, null);
		assertEquals(Boolean.FALSE, actualResult.getResult());

		/*
		 * Test with not null company
		 */
		company = prepareCompany();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		when(benefitGroupService.addGroup(company, groupData, strategyId)).thenReturn(groupId);

		actualResult = benefitGroupController.addGroup(request, groupData, strategyId, companyCode, null);
		assertEquals(Boolean.TRUE, actualResult.getResult());
	}

	@Test
	public void updateGroup() {

		HttpServletRequest request = new MockHttpServletRequest();
		String companyCode = "TEST";
		long groupId = 1;
		long strategyId = 1000L;
		String waitPeriod = "DOH";
		boolean defaultFlag = true;
		Company company;

		/*
		 * Test with null company
		 */
		company = null;

		benefitGroupController.updateGroup(request, companyCode, groupId, strategyId, waitPeriod,
				defaultFlag, null);

		/*
		 * Test with not null company
		 */
		company = prepareCompany();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);

		doNothing().when(benefitGroupService).updateBenefitGroupMetaData(companyCode, groupId, strategyId, waitPeriod, defaultFlag, company.getRealmPlanYearId());		
		
		benefitGroupController.updateGroup(request, companyCode, groupId, strategyId, waitPeriod,
				defaultFlag, null);
	}

	@Test
	public void deleteGroup() {

		HttpServletRequest request = new MockHttpServletRequest();
		String companyCode = "TEST";
		long strategyId = 1000L;
		long strategyGroupId = 2000L;
		String exchangeId = "";

		Response actualResult;
		Company company;

		/*
		 * Test with null company
		 */
		company = null;

		actualResult = benefitGroupController.deleteGroup(request, strategyId, strategyGroupId, companyCode,
				exchangeId);
		assertEquals(Boolean.FALSE, actualResult.getResult());

		/*
		 * Test with not null company
		 */
		company = prepareCompany();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		doNothing().when(benefitGroupService).deleteGroup(strategyGroupId, strategyId);

		actualResult = benefitGroupController.deleteGroup(request, strategyId, strategyGroupId, companyCode,
				exchangeId);
		assertEquals(Boolean.TRUE, actualResult.getResult());
	}

	@Test
	public void updateGroupHeadCount() {

		HttpServletRequest request = new MockHttpServletRequest();
		List<HeadCountData> headCountList = new ArrayList<HeadCountData>();
		String companyCode = "TEST";
		long strategyId = 1000L;

		Response actualResult;
		Company company;

		/*
		 * Test with null company
		 */
		company = null;

		actualResult = benefitGroupController.updateGroupHeadCount(request, headCountList, strategyId, companyCode);
		assertEquals(Boolean.FALSE, actualResult.getResult());

		/*
		 * Test with not null company
		 */
		company = prepareCompany();
		when(companyService.getCompanyDetails(companyCode, false, EMPLID, null)).thenReturn(company);
		doNothing().when(benefitGroupHeadCountService).updateGroupHeadCount(company, headCountList);

		actualResult = benefitGroupController.updateGroupHeadCount(request, headCountList, strategyId, companyCode);
		assertEquals(Boolean.TRUE, actualResult.getResult());
	}

	@Test
	public void updateBenefitGroupNameTest() {
		HttpServletRequest request;
		long groupId = 1111L;
		String groupName = "UPDATED GROUP NAME";

		request = Mockito.mock(HttpServletRequest.class);
		when( RequestValidator.getValidatedGroupName( groupName ) ).thenReturn( groupName );

		benefitGroupController.updateBenefitGroupName(request, groupId, groupName );
		verify( benefitGroupService, times(1) ).updateBenefitGroupName( groupId, groupName );
	}
	
	@Test
	public void updateBenefitGroupNameV1() {
		HttpServletRequest request;
		long groupId = 1111;
		long strategyId = 2222;
		String groupName = "UPDATED GROUP NAME";

		request = Mockito.mock(HttpServletRequest.class);
		when(RequestValidator.getValidatedGroupName(groupName)).thenReturn(groupName);

		benefitGroupController.updateBenefitGroupNameV1(request, strategyId, groupId, groupName);
		verify(benefitGroupService, times(1)).updateBenefitGroupName(strategyId, groupId, groupName);
	}

    @Test
    public void updateBenefitGroupTypeToK1Test() {
        HttpServletRequest request;
        long groupId = 1111L;
        String groupName = "K1";
        String groupType = "K1";
        long strategyId = 1000L;
        String companyCode = "G28";

        request = Mockito.mock(HttpServletRequest.class);
        when( RequestValidator.getValidatedGroupName( groupName ) ).thenReturn( groupName );

        benefitGroupController.updateBenefitGroupTypeToK1(request, strategyId, groupId, companyCode );
        verify( benefitGroupService, times(1) ).updateBenefitGroupType( strategyId, groupId, companyCode );
    }
	
	private Company prepareCompany() {
		
		Company company = new Company();
		company.setRealmPlanYearId(60);
		return company;
	}

}
