package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.rest.controllers.dto.outputs.TitlePageData;
import com.trinet.ambis.service.PersonService;
import com.trinet.ambis.service.impl.outputs.TitlePageServiceImpl;
import com.trinet.ambis.util.BSSSecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class TitlePageServiceImplTest extends ServiceUnitTest {
	
	@InjectMocks
	TitlePageServiceImpl titlePageServiceImpl;
	
	@Mock
	PersonService personService;

    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;

    @Before
    public void setUp() {
        bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMockedStatic.close();
    }
	@Test
	public void getTitlePageDataTest() {
		Company company = prepareCompany();
		OutputRequest prospectRequest = prepareOutputRequest();
		
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn("00002241950");
		//when(personService.getPersonFirstAndLastName(anyString())).thenReturn("User");
		
		TitlePageData titlePageData = titlePageServiceImpl.getTitlePageData(prospectRequest, company);

		assertEquals("TriNet III", titlePageData.getExchange());
		//assertEquals("User", titlePageData.getLoggedInUser());
		assertEquals("123-4567-789", titlePageData.getZipcode());
		assertEquals("SA", titlePageData.getRegions().get(0));
		assertEquals("01/31/2024", titlePageData.getExpirationDate());
		assertEquals("2024-01-01", titlePageData.getPlanYearStartDate());
		assertEquals("2024-12-31", titlePageData.getPlanYearEndDate());
		assertEquals("03/31/2024", titlePageData.getEffectiveDate());
		assertEquals("32003", titlePageData.getAdditionalZipCodes().get(0));
		assertEquals("W2", titlePageData.getGroupName());
		

	}
	
	@Test
	public void getTitlePageDataTestWhenPlanApendixNotExist() {
		Company company = prepareCompany();
		OutputRequest prospectRequest = prepareOutputRequest();
		prospectRequest.setPlanAppendixFilters(null);
		
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn("00002241950");
		//when(personService.getPersonFirstAndLastName(anyString())).thenReturn("User");
		
		TitlePageData titlePageData = titlePageServiceImpl.getTitlePageData(prospectRequest, company);

		assertEquals("TriNet III", titlePageData.getExchange());
		//assertEquals("User", titlePageData.getLoggedInUser());
		assertEquals("123-4567-789", titlePageData.getZipcode());
		assertEquals("01/31/2024", titlePageData.getExpirationDate());
		assertEquals("2024-01-01", titlePageData.getPlanYearStartDate());
		assertEquals("2024-12-31", titlePageData.getPlanYearEndDate());
		assertEquals("03/31/2024", titlePageData.getEffectiveDate());
		assertEquals( null, titlePageData.getRegions());
		assertEquals(null, prospectRequest.getPlanAppendixFilters());

	}

	private Company prepareCompany() {
		Company company = new Company();
		Realm realm = new Realm();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realm.setBenExchange("TriNet III");
		company.setRealm(realm );
		company.setActualHeadCount(160);
		realmPlanYear.setPlanYearStart(java.sql.Date.valueOf("2024-01-01"));
		realmPlanYear.setPlanYearEnd(java.sql.Date.valueOf("2024-12-31"));
		company.setRealmPlanYear(realmPlanYear);
		company.setZipCode("123-4567-789");
		company.setExpiryDate("2024-01-31");
		company.setBenefitStartDate("31-MAR-2024");
		return company;
	}

	private OutputRequest prepareOutputRequest() {
		OutputRequest prospectRequest = new OutputRequest();
		
		List<String> additionalZipCodes = new ArrayList<>();
		additionalZipCodes.add("32003");
		additionalZipCodes.add("28277");
		
		PlanAppendixFilters planAppendixFilters = new PlanAppendixFilters();
		planAppendixFilters.setMedicalPlanCategories(List.of("PPO","HSO"));
		planAppendixFilters.setRegions(List.of("SA","NC", "FL"));
		planAppendixFilters.setZipCodes(additionalZipCodes);
		planAppendixFilters.setGroupName("W2");
		
		prospectRequest.setBenefitTypes(Arrays.asList(new String[]{"med", "den", "vis", "lad", "dis"}));
		prospectRequest.setTnStrategyId("282357");
		prospectRequest.setPlanAppendixFilters(planAppendixFilters);
		prospectRequest.setTemplateNames(List.of("APX"));
		
		return prospectRequest;
	}
}
