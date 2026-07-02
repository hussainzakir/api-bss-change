package com.trinet.ambis.rest.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.URIConstants;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.AppRulesConfigService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.FeatureFlagService;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RefreshService;
import com.trinet.ambis.service.model.FeatureFlag;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class CommonControllerTest extends ServiceUnitTest {

	@InjectMocks
	CommonController commonController;

	@Mock
	CompanyService companyService;

	@Mock
	PersonService personService;

	@Mock
	RefreshService refreshService;

	@Mock
	RealmPlanYearRuleConfigService ruleConfigService;
	
	@Mock
	AppRulesConfigService appRulesConfigService;
	
	@Mock
	private FeatureFlagService featureFlagService;
	
	private MockMvc mockMvc;

	private static final String EMPLID = "0000000123456";
	private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
	private MockedStatic<RulesAndConfigsUtils> mockStaticRulesAndConfigsUtils;

	@Before
	public void setUp() {
//		MockitoAnnotations.initMocks(this);
		mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
		mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn(EMPLID);
		mockStaticRulesAndConfigsUtils = Mockito.mockStatic(RulesAndConfigsUtils.class);
		mockStaticRulesAndConfigsUtils.when(() -> RulesAndConfigsUtils.findPickChooseWithExceptions(any(Company.class))).thenReturn(true);
		mockMvc = MockMvcBuilders.standaloneSetup(commonController).build();
	}

	@After
	public void tearDown() {
		if (mockStaticBSSSecurityUtils != null) {
			mockStaticBSSSecurityUtils.close();
			mockStaticBSSSecurityUtils = null;
		}
		if (mockStaticRulesAndConfigsUtils != null) {
			mockStaticRulesAndConfigsUtils.close();
			mockStaticRulesAndConfigsUtils = null;
		}
	}

	@Test
	public void getPersonName() {

		HttpServletRequest request;
		String actualResult;
		String employeeName = "TEST, TEST";

		/*
		 * Test with null session
		 */
		request = Mockito.mock(HttpServletRequest.class);

		actualResult = commonController.getPersonName(request);
		assertNull(actualResult);

		/*
		 * Test with not null session and null trinetAuthEmplId
		 */
		request = new MockHttpServletRequest();

		actualResult = commonController.getPersonName(request);
		assertNull(actualResult);

		/*
		 * Test with success
		 */
		when(personService.getPersonFirstAndLastName(EMPLID)).thenReturn(employeeName);

		actualResult = commonController.getPersonName(request);
		assertEquals(employeeName, actualResult);


	}
	
	@Test(expected = BSSApplicationException.class)
	public void getPersonNameThrowsBSSApplicationException1() {
		
		HttpServletRequest request = new MockHttpServletRequest();

		when(personService.getPersonFirstAndLastName(EMPLID)).thenThrow(new BSSApplicationException());

		commonController.getPersonName(request);
	}
	
	@Test
	public void refreshPlans() {

		doNothing().when(refreshService).refreshPlanView();

		commonController.refreshPlans();
	}
	


	@Test(expected = BSSApplicationException.class)
	public void refreshPlansThrowsBSSApplicationException1() {

		doThrow(new BSSApplicationException()).when(refreshService).refreshPlanView();

		commonController.refreshPlans();
	}
	
	@Test
	public void refreshSession() {

		boolean actualResult;

		actualResult = commonController.refreshSession();
		assertTrue(actualResult);
	}

	@Test
	public void getRulesAndConfigs() {

		HttpServletRequest request = new MockHttpServletRequest();
		String companyCode = "TEST";
		Company company = prepareCompany();
		Map<String, String> realmRules = prepareRealmRules();
		Map<String, String> psRules = preparePsRules();
		Map<String, String> appRules = new HashMap<>();
		appRules.put("SUBMIT_QUE_ENABLED", "true");
		Map<String, String> actualResult;

		when(companyService.getCompanyDetails(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), any() )).thenReturn(company);
		when(ruleConfigService.getRulesAndConfigsByRealmPlanYearId( company )).thenReturn(realmRules);
		when(appRulesConfigService.getAllRulesAndConfigs()).thenReturn(appRules);
		
		actualResult = commonController.getRulesAndConfigs(request, companyCode, null);
		assertEquals(3, actualResult.size());
		assertEquals("REALM_VALUE", actualResult.get("REALM_KEY"));
		assertEquals("true", actualResult.get("SUBMIT_QUE_ENABLED"));
		assertEquals("false", actualResult.get("PICK_CHOOSE_FLAG"));
	}
	
	@Test(expected = BSSApplicationException.class)
	public void getRulesAndConfigsThrowsBSSApplicationException1() {
		
		HttpServletRequest request = new MockHttpServletRequest();
		String companyCode = "TEST";
		when(companyService.getCompanyDetails(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), any() )).thenThrow(new BSSApplicationException());

		commonController.getRulesAndConfigs(request, companyCode, null);
	}
	
	@Test(expected = IllegalStateException.class)
	public void getRulesAndConfigsThrowsBSSApplicationException2() {
		
		HttpServletRequest request = new MockHttpServletRequest();
		String companyCode = "TEST";
		when(companyService.getCompanyDetails(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), any() )).thenThrow(new IllegalStateException());

		commonController.getRulesAndConfigs(request, companyCode, null);
	}
	
	@Test
	public void getFeatureFlags_flagAvailable() throws Exception {
		String FEATURE_FLAG_URI = URIConstants.VERSION_AND_ROOT + URIConstants.FEATURE_FLAGS;

		List<FeatureFlag> flags = new ArrayList<>();
		FeatureFlag flag = new FeatureFlag("BSS_YEAR_ROUND", true);
		flags.add(flag);

		Company company = prepareCompany();
		when(companyService.getCompanyDetails(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), any() )).thenReturn(company);
		when(featureFlagService.retrieveFeatureFlags("G48", 68)).thenReturn(flags);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(FEATURE_FLAG_URI, "G48", "00001401813", "G48")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("[{\"key\":\"BSS_YEAR_ROUND\",\"value\":true}]", response.getContentAsString());
	}

	@Test
	public void getFeatureFlags_noFlagAvailable() throws Exception {
		String FEATURE_FLAG_URI = URIConstants.VERSION_AND_ROOT + URIConstants.FEATURE_FLAGS;
		Company company = prepareCompany();
		
		when(companyService.getCompanyDetails(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), any() )).thenReturn(company);
		when(featureFlagService.retrieveFeatureFlags("G48", 68)).thenReturn(new ArrayList<>());

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get(FEATURE_FLAG_URI, "G48", "00001401813", "G48")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(requestBuilder).andReturn();
		MockHttpServletResponse response = result.getResponse();
		assertEquals(HttpStatus.OK.value(), response.getStatus());
		assertEquals("[]", response.getContentAsString());
	}
	
	/*
	 * 
	 * Setup methods
	 * 
	 */
	private Company prepareCompany() {
		Company company = new Company();
		company.setCode( "3AW" );
		company.setRealmPlanYearId(10);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setPlanYearStart(new Date());
		rpy.setId(68);
		company.setRealmPlanYear(rpy);
		return company;
	}
	
	private Map<String, String> prepareRealmRules() {
		Map<String, String> realmRules = new HashMap<>();
		realmRules.put("REALM_KEY", "REALM_VALUE");
		realmRules.put("PICK_CHOOSE_FLAG", "false");
		return realmRules;
	}
	
	private Map<String, String> preparePsRules() {
		Map<String, String> psRules = new HashMap<>();
		psRules.put("PS_KEY", "PS_VALUE");
		return psRules;
	}

}
