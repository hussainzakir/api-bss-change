package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.persistence.dao.hrp.dto.CompanyDetailsDto;
import com.trinet.ambis.persistence.model.BSSStatusDetailsDto;
import com.trinet.ambis.service.BSSStatusDetailsService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.model.CommonData;
import com.trinet.ambis.service.model.CompanyData;
import com.trinet.ambis.service.model.CompanyRealmData;
import com.trinet.ambis.service.model.PlanYearCommonData;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.rest.controllers.dto.ChangeQuarterRequest;
import com.trinet.domain.common.ReturnResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class CompanyControllerTest extends ServiceUnitTest {

	@InjectMocks
	CompanyController companyController;

	@Mock
	CompanyService companyService;
	
	@Mock
	BSSStatusDetailsService bssStatusDetailsService;
	
	MockMvc mockMvc;

	private static final String COMPANY_CODE = "G48";
	private static final String EMPLID = "0000000123456";
	private static final String EMPLID_SYS_ACCT = "00000000000";
	private static final String PROSPECT_ID = "P12344";
	private static final String PROSPECT_COMPANY_CODE="PROSPECT_NY";
	private static final String EXCHANGE = "TNIII";

	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::checkSystemAccount).thenReturn(false);
		mockMvc = MockMvcBuilders.standaloneSetup(companyController).build();
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void getCommonData() {

		HttpServletRequest request;
		CommonData expectedResult = new CommonData();
		PlanYearCommonData planYearCommonData = new PlanYearCommonData();
		planYearCommonData.setEffectiveDate(new Date());
		planYearCommonData.setEndDate(new Date());
		expectedResult.setPlanYearCommonData(planYearCommonData);
		CommonData actualResult;

		/*
		 * Test with null session
		 */
		request = Mockito.mock(HttpServletRequest.class);

		actualResult = companyController.getCommonData(request, COMPANY_CODE, null,false);
		assertNull(actualResult);

		/*
		 * Test with not null session and null trinetAuthEmplId
		 */
		request = new MockHttpServletRequest();

		actualResult = companyController.getCommonData(request, COMPANY_CODE, null,false);
		assertNull(actualResult);

		/*
		 * Test with success
		 */
		when(companyService.getCompanyCommonData(COMPANY_CODE, EMPLID, null,false)).thenReturn(expectedResult);

		actualResult = companyController.getCommonData(request, COMPANY_CODE, null,false);
		assertEquals(expectedResult, actualResult);
		
		
		/**
		 * Test with success for system account
		 */
		
		when(BSSSecurityUtils.checkSystemAccount()).thenReturn(true);
		when(companyService.getCompanyCommonData(COMPANY_CODE, EMPLID_SYS_ACCT, null,false)).thenReturn(expectedResult);

		actualResult = companyController.getCommonData(request, COMPANY_CODE, null,false);
		assertEquals(expectedResult, actualResult);
	
	}
	

	@Test
	public void getCompanyData() {

		HttpServletRequest request;
		List<CompanyRealmData> expectedList = prepareCompanyPlanYearData();
		List<CompanyRealmData> actualResult;

		/*
		 * Test with success
		 */
		request = new MockHttpServletRequest();
		when(companyService.getCompanyPlanYearData(COMPANY_CODE, EMPLID)).thenReturn(expectedList);
		actualResult = companyController.getCompanyData(request, COMPANY_CODE);

		assertEquals(2, actualResult.size());
		assertEquals(expectedList, actualResult);
	}

	@Test
	public void getCompanyName() {
		HttpServletRequest request = new MockHttpServletRequest();
		Set<String> companyCodes = new HashSet<>();
		companyCodes.add(COMPANY_CODE);

		Map<String, String> companies = new HashMap<>();
		companies.put(COMPANY_CODE, "TriNet Group");

		when(companyService.findCompaniesNames(companyCodes)).thenReturn(companies);

		Map<String, String> actual = companyController.getCompanyName(request, COMPANY_CODE);

		assertEquals(companies, actual);
	}

	@Test
	public void isRenewalCompany_True() throws Exception {
		String IS_RENEWAL_COMPANY_URI = URIConstants.VERSION_AND_ROOT + URIConstants.IS_RENEWAL_COMPANY;

		when(companyService.isRenewalCompany(COMPANY_CODE)).thenReturn(true);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(IS_RENEWAL_COMPANY_URI, COMPANY_CODE, "00001401813", COMPANY_CODE)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{\"data\":true,\"_statusCode\":\"200\"}", response.getContentAsString());
	}
	
	@Test
	public void isRenewalCompany_False() throws Exception {
		String IS_RENEWAL_COMPANY_URI = URIConstants.VERSION_AND_ROOT + URIConstants.IS_RENEWAL_COMPANY;

		when(companyService.isRenewalCompany(COMPANY_CODE)).thenReturn(false);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(IS_RENEWAL_COMPANY_URI, COMPANY_CODE, "00001401813", COMPANY_CODE)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{\"data\":false,\"_statusCode\":\"200\"}", response.getContentAsString());
	}
	
	@Test
	public void bssStatus() throws Exception {
		String bssStatusUrl = URIConstants.VERSION_AND_ROOT + URIConstants.BSS_STATUS;
		BSSStatusDetailsDto bssStatus=new BSSStatusDetailsDto();
		bssStatus.setBssStarted(true);
		when(bssStatusDetailsService.getBssStatusDetail(COMPANY_CODE)).thenReturn(bssStatus);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(bssStatusUrl, COMPANY_CODE, "00001401813", COMPANY_CODE)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{\"data\":{\"bssStarted\":true,\"bssSubmitted\":false},\"_statusCode\":\"200\",\"_statusText\":\"OK\",\"_statusMessage\":\"Success\"}", response.getContentAsString());
	}

	@Test
	public void getCompanyDataWithProspectId() throws Exception
	{
		HttpServletRequest request;
		CommonData expectedResult = new CommonData();
		CompanyData companyData = new CompanyData();
		companyData.setProspectId(PROSPECT_ID);
		expectedResult.setCompanyCommonData(companyData);
		CommonData actualResult;
		request = Mockito.mock(HttpServletRequest.class);

		when(companyService.getCompanyCommonData(PROSPECT_COMPANY_CODE, EMPLID, null,false)).thenReturn(expectedResult);
		actualResult = companyController.getCommonData(request, PROSPECT_COMPANY_CODE, null,false);
		assertEquals(expectedResult, actualResult);
	}

	@Test
	public void getCompanyDataWithoutProspectId() throws Exception
	{
		HttpServletRequest request;
		CommonData expectedResult = new CommonData();
		CommonData actualResult;
		request = Mockito.mock(HttpServletRequest.class);

		when(companyService.getCompanyCommonData(COMPANY_CODE, EMPLID, null,false)).thenReturn(expectedResult);

		actualResult = companyController.getCommonData(request, COMPANY_CODE, null,false);
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void updatePlYrChangeSyncExecutedFlagTest() throws Exception {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		doNothing().when(companyService).updatePlYrChangeSyncExecutedFlag(PROSPECT_ID,
				BenExchngEnums.getByExchangeId(EXCHANGE));
		companyController.updatePlYrChangeSyncExecutedFlag(request, PROSPECT_ID, EXCHANGE);
		verify(companyService, times(1)).updatePlYrChangeSyncExecutedFlag(PROSPECT_ID,
				BenExchngEnums.getByExchangeId(EXCHANGE));
	}

	/**
	 * GIVEN a valid request and strategyAccessed is true,
	 * WHEN the getCommonData endpoint is called,
	 * THEN the controller should return the expected CommonData
	 */
	@Test
	public void getCompanyDataWithStrategyAccessed() throws Exception {
		HttpServletRequest request;
		CommonData expectedResult = new CommonData();
		request = Mockito.mock(HttpServletRequest.class);

		when(companyService.getCompanyCommonData(COMPANY_CODE, EMPLID, null, true)).thenReturn(expectedResult);

		CommonData actualResult = companyController.getCommonData(request, COMPANY_CODE, null, true);
		assertEquals(expectedResult, actualResult);
		verify(companyService, times(1)).getCompanyCommonData(COMPANY_CODE, EMPLID, null, true);
	}


	@Test
	public void changeQuarter_Success() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		ChangeQuarterRequest changeQuarterRequest = ChangeQuarterRequest.builder()
				.quarter("Q1")
				.messageSeq("12345")
				.build();

		when(companyService.initiateQuarterChange(COMPANY_CODE, "Q1", "12345")).thenReturn(true);

		companyController.changeQuarter(request, COMPANY_CODE, changeQuarterRequest);

		verify(companyService, times(1)).initiateQuarterChange(COMPANY_CODE, "Q1", "12345");
	}

	@Test
	public void changeQuarter_NullQuarter_ThrowsValidationException() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		ChangeQuarterRequest changeQuarterRequest = ChangeQuarterRequest.builder()
				.quarter(null)
				.messageSeq("12345")
				.build();

		try {
			companyController.changeQuarter(request, COMPANY_CODE, changeQuarterRequest);
			fail("Expected BSSApplicationException to be thrown");
		} catch (BSSApplicationException ex) {
			assertNotNull(ex.getBssError());
			assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
			assertEquals(BSSHttpStatusConstants.BAD_REQUEST, ex.getBssError().getStatus());
			assertEquals("Request body must include both 'quarter' and 'messageSeq' and both must be valid.",
					ex.getBssError().getCustomMessage());
			verify(companyService, times(0)).initiateQuarterChange(COMPANY_CODE, null, "12345");
		}
	}

	@Test
	public void changeQuarter_NullMessageSeq_ThrowsValidationException() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		ChangeQuarterRequest changeQuarterRequest = ChangeQuarterRequest.builder()
				.quarter("Q1")
				.messageSeq(null)
				.build();

		try {
			companyController.changeQuarter(request, COMPANY_CODE, changeQuarterRequest);
			fail("Expected BSSApplicationException to be thrown");
		} catch (BSSApplicationException ex) {
			assertNotNull(ex.getBssError());
			assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
			assertEquals(BSSHttpStatusConstants.BAD_REQUEST, ex.getBssError().getStatus());
			assertEquals("Request body must include both 'quarter' and 'messageSeq' and both must be valid.",
					ex.getBssError().getCustomMessage());
			verify(companyService, times(0)).initiateQuarterChange(COMPANY_CODE, "Q1", null);
		}
	}

	@Test
	public void changeQuarter_InvalidQuarter_ThrowsValidationException() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		ChangeQuarterRequest changeQuarterRequest = ChangeQuarterRequest.builder()
				.quarter("INVALID")
				.messageSeq("12345")
				.build();

		try {
			companyController.changeQuarter(request, COMPANY_CODE, changeQuarterRequest);
			fail("Expected BSSApplicationException to be thrown");
		} catch (BSSApplicationException ex) {
			assertNotNull(ex.getBssError());
			assertEquals(BSSErrorResponseCodes.REQUEST_VALIDATION_ERROR, ex.getBssError().getCode());
			assertEquals(BSSHttpStatusConstants.BAD_REQUEST, ex.getBssError().getStatus());
			assertEquals("Invalid quarter value: INVALID", ex.getBssError().getCustomMessage());
			verify(companyService, times(0)).initiateQuarterChange(COMPANY_CODE, "INVALID", "12345");
		}
	}

	/*
	 * 
	 * Setup methods
	 * 
	 */

	private List<CompanyRealmData> prepareCompanyPlanYearData() {
		List<CompanyRealmData> returnList = new ArrayList<>();
		CompanyRealmData companyRealmData = new CompanyRealmData();
		companyRealmData.setRecordType("future");
		returnList.add(companyRealmData);
		companyRealmData = new CompanyRealmData();
		companyRealmData.setRecordType("current");
		returnList.add(companyRealmData);
		return returnList;
	}

	@Test
	public void getCompanyDetailsById_Success() throws Exception {
		String GET_COMPANY_DETAILS_URI = URIConstants.VERSION_AND_ROOT + URIConstants.GET_COMPANY_DETAILS_BY_ID;
		Long companyId = 12345L;
		Date planYearStart = new Date();

		CompanyDetailsDto companyDetailsDto = CompanyDetailsDto.builder()
				.code(COMPANY_CODE)
				.planYearStart(planYearStart)
				.cloneBenpgm("CLONE_PGM")
				.bundleSeq(100L)
				.oeQuarter("Q1")
				.naicsCode(541511)
				.largeDealProspect(1)
				.naicsBundleId(2L)
				.exchangeId("MKT1")
				.build();

		when(companyService.getCompanyDetailsById(companyId)).thenReturn(companyDetailsDto);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(GET_COMPANY_DETAILS_URI,
						BSSApplicationConstants.TRINET_COMPANY_ID, BSSApplicationConstants.TRINET_EMPL_ID, COMPANY_CODE)
				.param("companyId", companyId.toString())
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		verify(companyService, times(1)).getCompanyDetailsById(companyId);
	}

	@Test
	public void getCompanyDetailsById_ReturnsNull() throws Exception {
		String GET_COMPANY_DETAILS_URI = URIConstants.VERSION_AND_ROOT + URIConstants.GET_COMPANY_DETAILS_BY_ID;
		Long companyId = 99999L;

		when(companyService.getCompanyDetailsById(companyId)).thenReturn(null);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(GET_COMPANY_DETAILS_URI,
						BSSApplicationConstants.TRINET_COMPANY_ID, BSSApplicationConstants.TRINET_EMPL_ID, COMPANY_CODE)
				.param("companyId", companyId.toString())
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("{\"_statusCode\":\"200\",\"_statusText\":\"OK\",\"_statusMessage\":\"Success\"}", response.getContentAsString());
		verify(companyService, times(1)).getCompanyDetailsById(companyId);
	}

	@Test
	public void getCompanyDetailsById_DirectCall() {
		HttpServletRequest request = new MockHttpServletRequest();
		Long companyId = 12345L;
		Date planYearStart = new Date();

		CompanyDetailsDto companyDetailsDto = CompanyDetailsDto.builder()
				.code(COMPANY_CODE)
				.planYearStart(planYearStart)
				.cloneBenpgm("CLONE_PGM")
				.bundleSeq(100L)
				.oeQuarter("Q1")
				.naicsCode(541511)
				.largeDealProspect(1)
				.naicsBundleId(2L)
				.exchangeId("MKT1")
				.build();

		when(companyService.getCompanyDetailsById(companyId)).thenReturn(companyDetailsDto);

		ReturnResponse<CompanyDetailsDto> response = companyController.getCompanyDetailsById(request, COMPANY_CODE, companyId);

		assertNotNull(response);
		assertEquals("200", response.getStatusCode());
		assertEquals("Success", response.getStatusMessage());
		assertNotNull(response.getData());
		assertEquals(COMPANY_CODE, response.getData().getCode());
		assertEquals("Q1", response.getData().getOeQuarter());
		assertEquals("CLONE_PGM", response.getData().getCloneBenpgm());
		assertEquals(Long.valueOf(100L), response.getData().getBundleSeq());
		assertEquals(Integer.valueOf(541511), response.getData().getNaicsCode());
		assertEquals(Integer.valueOf(1), response.getData().getLargeDealProspect());
		assertEquals(Long.valueOf(2L), response.getData().getNaicsBundleId());
		assertEquals("MKT1", response.getData().getExchangeId());
		verify(companyService, times(1)).getCompanyDetailsById(companyId);
	}

	// -----------------------------------------------------------------------
	// resetCompany endpoint tests
	// -----------------------------------------------------------------------

	/**
	 * DELETE endpoint returns 200 and
	 * delegates to companyService.resetCompany with the legacyCompanyId.
	 */
	@Test
	public void resetCompany_Success_ViaMockMvc() throws Exception {
		String resetUri = URIConstants.VERSION_AND_ROOT + URIConstants.RESET_COMPANY;
		long legacyCompanyId = 12345L;

		doNothing().when(companyService).resetCompany(legacyCompanyId);

		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.delete(resetUri, COMPANY_CODE, "00001401813", COMPANY_CODE)
				.param("legacyCompanyId", String.valueOf(legacyCompanyId))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();

		assertEquals(HttpStatus.OK.value(), response.getStatus());
		verify(companyService, times(1)).resetCompany(legacyCompanyId);
	}

	/**
	 * Controller calls companyService.resetCompany
	 * with the legacyCompanyId and returns a 200 ReturnResponse.
	 */
	@Test
	public void resetCompany_Success_DirectCall() {
		HttpServletRequest request = new MockHttpServletRequest();
		long legacyCompanyId = 12345L;

		doNothing().when(companyService).resetCompany(legacyCompanyId);

		ReturnResponse<Void> response = companyController.resetCompany(request, COMPANY_CODE, legacyCompanyId);

		assertNotNull(response);
		assertEquals("200", response.getStatusCode());
		assertEquals("Success", response.getStatusMessage());
		verify(companyService, times(1)).resetCompany(legacyCompanyId);
	}

	/**
	 * (No data): resetCompany completes without error when the
	 * company has no data to delete.
	 */
	@Test
	public void resetCompany_NoData_ReturnsSuccess() {
		HttpServletRequest request = new MockHttpServletRequest();
		long legacyCompanyId = 99999L;

		doNothing().when(companyService).resetCompany(legacyCompanyId);

		ReturnResponse<Void> response = companyController.resetCompany(request, COMPANY_CODE, legacyCompanyId);

		assertNotNull(response);
		assertEquals("200", response.getStatusCode());
		verify(companyService, times(1)).resetCompany(legacyCompanyId);
	}

	/**
	 * When companyService.resetCompany throws a RuntimeException the controller
	 * must propagate it (the global exception handler maps it to an error response).
	 */
	@Test
	public void resetCompany_ServiceThrows_ExceptionPropagated() {
		HttpServletRequest request = new MockHttpServletRequest();
		long legacyCompanyId = 12345L;

		doThrow(new RuntimeException("DB rollback")).when(companyService).resetCompany(legacyCompanyId);

		try {
			companyController.resetCompany(request, COMPANY_CODE, legacyCompanyId);
			fail("Expected RuntimeException to propagate");
		} catch (RuntimeException ex) {
			assertEquals("DB rollback", ex.getMessage());
		}

		verify(companyService, times(1)).resetCompany(legacyCompanyId);
	}

}