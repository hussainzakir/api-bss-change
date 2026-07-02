package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.persistence.model.HQExceptionsId;
import com.trinet.ambis.service.HqOverridesService;
import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
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

@RunWith(MockitoJUnitRunner.class)
public class HqOverridesAdminControllerTest extends ServiceUnitTest {

	@InjectMocks
	HqOverridesAdminController hqOverrideController;

	@Mock
	HqOverridesService hqOverridesService;

	private static final String COMPANY_CODE = "G48";
	private static final String EMPLID = "0000000123456";
	private static final String QUARTER = "Q!";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void createHqOverride() {
		HttpServletRequest request;
		HqOverridesDto hqOverrideDto = prepareCompanyHqData();
		HqOverridesDto actualResult;
		request = Mockito.mock(HttpServletRequest.class);
		when(hqOverridesService.createHqOverridesDetails(hqOverrideDto)).thenReturn(hqOverrideDto);
		actualResult = hqOverrideController.createCompanyHqOverridesData(request, hqOverrideDto);
		assertEquals(hqOverrideDto, actualResult);
	}

	@Test
	public void getHqOverridesDetails() {
		HttpServletRequest request;
		List<HqOverridesDto> hqOverrideDtos = prepareCompanyHqDataList();
		List<HqOverridesDto> actualResult;
		request = Mockito.mock(HttpServletRequest.class);
		when(hqOverridesService.getHqOverridesDetails(COMPANY_CODE,QUARTER)).thenReturn(hqOverrideDtos);
		actualResult = hqOverrideController.getCompanyHqOverridesData(request, COMPANY_CODE, QUARTER);
		assertEquals(hqOverrideDtos, actualResult);
		actualResult = hqOverrideController.getCompanyHqOverridesData(request, "0", "0");
		assertEquals(0, actualResult.size());

	}
	@Test
	public void getCompanyPlanYearData() {
		HttpServletRequest request;
		List<CompanyHQData> hqOverrideDtos = prepareCompanyHq();
		List<CompanyHQData> actualResult;
		request = Mockito.mock(HttpServletRequest.class);
		when(hqOverridesService.getCompanyPlanYearData(COMPANY_CODE)).thenReturn(hqOverrideDtos);
		actualResult = hqOverrideController.getCompanyData(request, COMPANY_CODE);
		assertEquals(hqOverrideDtos, actualResult);
	}
	
	@Test
	public void deleteHqOverride() {
		HttpServletRequest request;
		HQExceptionsId id=new HQExceptionsId();
		request = Mockito.mock(HttpServletRequest.class);
		hqOverridesService.deleteHqOverride("011",1);
		doNothing().when(hqOverridesService).deleteHqOverride(COMPANY_CODE, 43);
		hqOverrideController.deleteHq(request, COMPANY_CODE, 43);
		verify(hqOverridesService, times(1)).deleteHqOverride(COMPANY_CODE, 43);

	}

	private HqOverridesDto prepareCompanyHqData() {
		HqOverridesDto hqOverridesDto = new HqOverridesDto();
		hqOverridesDto.setCanCopy(true);
		hqOverridesDto.setCompanyCode(COMPANY_CODE);
		hqOverridesDto.setCompanyName("Test");
		hqOverridesDto.setRealmYearId(43L);
		hqOverridesDto.setNextYearPlanYearStart("01-01-2021");
		return hqOverridesDto;
	}
	
	private List<HqOverridesDto> prepareCompanyHqDataList() {
		HqOverridesDto hqOverridesDto = new HqOverridesDto();
		List<HqOverridesDto> listOfHq= new ArrayList<>(); 
		hqOverridesDto.setCanCopy(true);
		hqOverridesDto.setCompanyCode(COMPANY_CODE);
		hqOverridesDto.setCompanyName("Test");
		hqOverridesDto.setRealmYearId(43L);
		hqOverridesDto.setNextYearPlanYearStart("01-01-2021");
		listOfHq.add(hqOverridesDto);
		return listOfHq;
	}

	private List<CompanyHQData> prepareCompanyHq() {
		CompanyHQData companyRealmData = new CompanyHQData();
		companyRealmData.setCode("001");
		companyRealmData.setRealmYearId(41L);
		companyRealmData.setCompanyHq("NA");
		List<CompanyHQData> listOfCompany = new ArrayList<>();
		listOfCompany.add(companyRealmData);
		return listOfCompany;
	}

}
