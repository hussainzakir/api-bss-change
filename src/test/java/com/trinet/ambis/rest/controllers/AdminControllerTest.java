package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.persistence.dao.hrp.QuarterAndPlanYearDto;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PreLoadStrategiesStatusDto;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.persistence.model.SchedTblId;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.SchedTblService;
import com.trinet.ambis.service.SearchCompanyService;
import com.trinet.ambis.service.model.ProductQuarters;
import com.trinet.ambis.service.model.SchedMidYearFundingDto;
import com.trinet.ambis.service.model.SchedTblAdminDto;
import com.trinet.ambis.service.model.SchedTblDto;
import com.trinet.ambis.service.model.SearchCompanyResultData;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest extends ServiceUnitTest {

	@InjectMocks
	AdminController adminController;

	@Mock
	CompanyService companyService;

	@Mock
	SchedTblService schedTblService;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	SearchCompanyService searchCompanyService;

	@Mock
	RealmPlanYearService realmPlanYearService;

	@Mock
	ProcessStatusService processStatusService;

	private MockMvc mockMvc;

	private static final String COMPID = "001";
	private static final String EMPLID = "0000000123456";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;

	@Before
	public void setUp() {
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
	}

	@Test
	public void getScheduleDates() throws Exception {

		HttpServletRequest request = new MockHttpServletRequest();
		List<SchedTblAdminDto> actualResult;
		String companyCode;
		String quarter = "";

		/*
		 * Test with companyCode not DEFAULT
		 */
		companyCode = "TEST";
		List<SchedTblAdminDto> schedTblList = prepareSchedTblList(companyCode);
		when(schedTblService.getSchedTblAdminDates(companyCode, quarter)).thenReturn(schedTblList);

		actualResult = adminController.getScheduleDates(request, companyCode, quarter);
		assertEquals(companyCode, actualResult.get(0).getCompany());

		/*
		 * Test with companyCode of DEFAULT
		 */
		companyCode = Constants.DEFAULT_COMPANY_CODE;
		schedTblList = prepareSchedTblList(companyCode);
		when(schedTblService.getSchedTblAdminDates(companyCode, quarter)).thenReturn(schedTblList);

		actualResult = adminController.getScheduleDates(request, companyCode, quarter);
		assertEquals(schedTblList, actualResult);

		/*
		 * Test with companyCode of DEFAULT; schedTbl from service is null
		 */
		companyCode = Constants.DEFAULT_COMPANY_CODE;
		schedTblList = prepareSchedTblList(companyCode);
		when(schedTblService.getSchedTblAdminDates(companyCode, quarter)).thenReturn(null);

		actualResult = adminController.getScheduleDates(request, companyCode, quarter);
		assertEquals(null, actualResult);
	}

	@Test
	public void createScheduleDates() {

		HttpServletRequest request = new MockHttpServletRequest();
		SchedTbl actualResult;
		String companyCode = "G48";
		SchedTblId sched = new SchedTblId();
		sched.setCompany(companyCode);
		SchedTblDto schedTbl = new SchedTblDto();
		schedTbl.setSched(sched);
		schedTbl.setOeQuarter("8Y");
		SchedTbl schedTblEntity = new SchedTbl();
		schedTblEntity.setSched(sched);

		when(schedTblService.createUpdateScheduleDates(request, schedTbl, EMPLID)).thenReturn(schedTblEntity);
		doNothing().when(schedTblService).validateRequest(schedTbl);
				
		actualResult = adminController.createScheduleDates(request, schedTbl, companyCode);
		assertEquals(schedTblEntity, actualResult);
	}

	@Test
	public void updateScheduleDatesTest() {
		HttpServletRequest request = new MockHttpServletRequest();
		SchedTbl actualResult;
		String companyCode = "G48";
		SchedTblId sched = new SchedTblId();
		sched.setCompany(companyCode);
		SchedTblDto schedTbl = new SchedTblDto();
		schedTbl.setSched(sched);
		schedTbl.setOeQuarter("8Y");
		SchedTbl schedTblEntity = new SchedTbl();
		schedTblEntity.setSched(sched);

		when(schedTblService.createUpdateScheduleDates(request, schedTbl, EMPLID)).thenReturn(schedTblEntity);

		actualResult = adminController.updateScheduleDates(request, schedTbl, companyCode);
		assertEquals(schedTblEntity, actualResult);
		assertEquals(companyCode, actualResult.getSched().getCompany());
	}

	@Test
	public void updateSchedDates_postCall() throws Exception {
		String API_URL_STRATEGY_BUDGET_API = URIConstants.VERSION_AND_ROOT + URIConstants.MID_YEAR_FUNDING_DETAILS;
		String requestBody = "{\"id\":\"\",\"companyCode\":\"001\",\"companyId\":114203,\"serviceOrderNumber\":\"test\",\"midYearFundingEffDate\":\"2022-07-05\",\"active\":true,\"lastUpdatedByName\":\"\",\"updatedTime\":null}";
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.post(API_URL_STRATEGY_BUDGET_API, "001", "00001401813", "001").accept(MediaType.APPLICATION_JSON)
				.content(requestBody).contentType(MediaType.APPLICATION_JSON);
		SchedMidYearFundingDto schedMidYearFundingDto = new SchedMidYearFundingDto();
		Date date = CommonUtils.formatStringToDate("2022-07-06", "yyyy-MM-dd");
		schedMidYearFundingDto.setMidYearFundingEffDate(date);
		Company company = new Company();
		company.setCode(COMPID);
		ArgumentCaptor<SchedMidYearFundingDto> schedMidYrFundDtoArgCaptor = ArgumentCaptor
				.forClass(SchedMidYearFundingDto.class);
		ArgumentCaptor<Company> companyArgCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<Boolean> updateFlgArgCaptor = ArgumentCaptor.forClass(Boolean.class);

		when(companyService.getCompanyDetails(COMPID)).thenReturn(company);
		when(schedTblService.createUpdateMidYearDetails(schedMidYrFundDtoArgCaptor.capture(),
				companyArgCaptor.capture(), updateFlgArgCaptor.capture()))
						.thenReturn(Arrays.asList(schedMidYearFundingDto));

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		assertEquals(COMPID, schedMidYrFundDtoArgCaptor.getValue().getCompanyCode());
		assertEquals(EMPLID, schedMidYrFundDtoArgCaptor.getValue().getLastUpdatedBy());
		assertEquals("2022-07-05", CommonUtils
				.formatDateToString(schedMidYrFundDtoArgCaptor.getValue().getMidYearFundingEffDate(), "yyyy-MM-dd"));
		assertEquals("2022-07-05", CommonUtils
				.formatDateToString(schedMidYrFundDtoArgCaptor.getValue().getMidYearFundingEffDate(), "yyyy-MM-dd"));
		assertEquals("test", schedMidYrFundDtoArgCaptor.getValue().getServiceOrderNumber());

		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
		String actual = result.getResponse().getContentAsString();
//		String expected = "[{\"id\":0,\"companyId\":0,\"serviceOrderNumber\":null,\"midYearFundingEffDate\":\"2022-07-05\",\"active\":false,\"companyCode\":null,\"lastUpdatedByName\":null,\"updatedTime\":null,\"planYearStartDate\":null,\"planYearEndDate\":null}]";
//		assertEquals(expected, actual);
	}

	@Test
	public void getproducts() {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		List<ProductQuarters> actualResult;
		Map<String, ProductQuarters> productQuartersMap = new HashMap<String, ProductQuarters>();

		/*
		 * Test with not null session and not null trinetAuthEmplId
		 */
		when(realmDataDao.getAllProductQuarters()).thenReturn(productQuartersMap);

		actualResult = adminController.getproducts(request);
		assertTrue(actualResult.isEmpty());
	}

	@Test
	public void getSearchResults() {

		HttpServletRequest request;
		String searchParam = "";
		List<SearchCompanyResultData> searchCompanyResultDataList = new ArrayList<SearchCompanyResultData>();
		List<SearchCompanyResultData> actualResult;

		request = Mockito.mock(HttpServletRequest.class);

		when(searchCompanyService.getSearchResults(searchParam, COMPID, EMPLID))
				.thenReturn(searchCompanyResultDataList);

		actualResult = adminController.getSearchResults(request, searchParam, COMPID);
		assertEquals(searchCompanyResultDataList, actualResult);
	}

	@Test
	public void createMidYearFundingDetails() {

		HttpServletRequest request;
		String companyCode = "TEST";
		SchedMidYearFundingDto schedMidYearFundingDto = new SchedMidYearFundingDto();
		List<SchedMidYearFundingDto> schedMidYearFundingList = new ArrayList<>();
		List<SchedMidYearFundingDto> actualResult;
		Company company = prepareCompany();

		request = Mockito.mock(HttpServletRequest.class);

		when(companyService.getCompanyDetails(companyCode)).thenReturn(company);
		when(schedTblService.createUpdateMidYearDetails(schedMidYearFundingDto, company, false))
				.thenReturn(schedMidYearFundingList);

		actualResult = adminController.createMidYearFundingDetails(request, companyCode, schedMidYearFundingDto);
		assertEquals(EMPLID, schedMidYearFundingDto.getLastUpdatedBy());
		assertEquals(schedMidYearFundingList, actualResult);
	}

	@Test
	public void updateMidYearFundingDetails() {

		HttpServletRequest request;
		String companyCode = "TEST";
		SchedMidYearFundingDto schedMidYearFundingDto = new SchedMidYearFundingDto();
		List<SchedMidYearFundingDto> schedMidYearFundingList = new ArrayList<>();
		List<SchedMidYearFundingDto> actualResult;

		request = Mockito.mock(HttpServletRequest.class);

		actualResult = adminController.updateMidYearFundingDetails(request, companyCode, schedMidYearFundingDto);
		assertTrue(actualResult.isEmpty());

		actualResult = adminController.updateMidYearFundingDetails(request, companyCode, schedMidYearFundingDto);
		assertEquals(EMPLID, schedMidYearFundingDto.getLastUpdatedBy());
		assertEquals(schedMidYearFundingList, actualResult);
	}

	@Test
	public void getMidYearFundingDetails() {

		HttpServletRequest request;
		String companyCode = "TEST";
		List<SchedMidYearFundingDto> schedMidYearFundingList = new ArrayList<>();
		List<SchedMidYearFundingDto> actualResult;

		request = Mockito.mock(HttpServletRequest.class);

		when(schedTblService.getMidYearDetails(companyCode)).thenReturn(schedMidYearFundingList);

		actualResult = adminController.getMidYearFundingDetails(request, companyCode);
		assertEquals(schedMidYearFundingList, actualResult);
	}

	@Test
	public void getListOfOeQuartersAndPlanYears() {

		HttpServletRequest request;
		List<QuarterAndPlanYearDto> actualResult;
		List<QuarterAndPlanYearDto> schedMidYearFundingList = new ArrayList<>();

		when(realmPlanYearService.getOeQuartersAndPlanYearsInfo()).thenReturn(schedMidYearFundingList);

		request = Mockito.mock(HttpServletRequest.class);

		actualResult = adminController.getListOfOeQuartersAndPlanYears(request);
		assertEquals(schedMidYearFundingList, actualResult);

	}

	@Test
	public void getPreLoadStrategiesStatus() {

		HttpServletRequest request;
		List<PreLoadStrategiesStatusDto> actualResult;
		List<PreLoadStrategiesStatusDto> preloadStatusList = new ArrayList<>();

		when(processStatusService.getPreLoadStrategiesStatuses()).thenReturn(preloadStatusList);

		request = Mockito.mock(HttpServletRequest.class);

		actualResult = adminController.getPreLoadStrategiesStatus(request);
		assertEquals(preloadStatusList, actualResult);

	}

	@Test
	public void refreshCompanyCensusTest() {

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Long realmYearId = 10L;
		String companyCode = "TEST";
		Object response = adminController.refreshCompanyCensus(request, realmYearId, companyCode);
		assertTrue(response instanceof String);
		assertTrue("Done".equals(((String) response).substring(0, 4)));
	}

	/*
	 * 
	 * Setup methods
	 * 
	 */
	private Company prepareCompany() {

		Company company = new Company();
		SchedTbl schedTbl = new SchedTbl();
		SchedTblId schedTblId = new SchedTblId();
		schedTbl.setSched(schedTblId);
		company.setSchedTbl(schedTbl);
		return company;
	}

	private List<SchedTblAdminDto> prepareSchedTblList(String companyCode) {
		List<SchedTblAdminDto> schedTblList = new ArrayList<>();
		SchedTblAdminDto schedTblAdminDto = new SchedTblAdminDto();
		schedTblAdminDto.setCompany(companyCode);
		schedTblList.add(schedTblAdminDto);
		return schedTblList;
	}

}
