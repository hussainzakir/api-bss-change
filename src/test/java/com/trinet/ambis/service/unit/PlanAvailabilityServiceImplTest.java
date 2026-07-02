package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.trinet.ambis.authorization.BenefitsBatchAuthorization;
import com.trinet.common.AppConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.common.PlanAvailabilityURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.PlanAvailabilityServiceImpl;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.domain.common.ReturnResponse;

@RunWith(MockitoJUnitRunner.class)
public class PlanAvailabilityServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	PlanAvailabilityServiceImpl planAvailabilityServiceImpl;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	UriComponentsBuilder uriComponentsBuilder;

	@Mock
	UriComponents uriComponents;

	private Company company;

	private final String COMPANY_CODE = "PROSPECT_ID";
	private final long REALM_ID = 58;

    private MockedStatic<CommonServiceHelper> commonServiceHelperMockedStatic;
    private MockedStatic<BenefitsBatchAuthorization> benefitsBatchAuthorizationMockedStatic;
    private MockedStatic<BSSMessageConfig> bssMessageConfigMockedStatic;
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;
    private MockedStatic<UriComponentsBuilder> uriComponentsBuilderMockedStatic;
    private MockedStatic<AppConfig> appConfigMockedStatic;

    @Before
    public void setUp() {
        commonServiceHelperMockedStatic = Mockito.mockStatic(CommonServiceHelper.class);
        benefitsBatchAuthorizationMockedStatic = Mockito.mockStatic(BenefitsBatchAuthorization.class);
        bssMessageConfigMockedStatic = Mockito.mockStatic(BSSMessageConfig.class);
        bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);
        uriComponentsBuilderMockedStatic = Mockito.mockStatic(UriComponentsBuilder.class);
        appConfigMockedStatic = Mockito.mockStatic(AppConfig.class);
		company = new Company();
		company.setId(12345L);
		company.setCode(COMPANY_CODE);
		RealmPlanYear rpy = new RealmPlanYear();
		rpy.setId(REALM_ID);
		company.setRealmPlanYear(rpy);
        appConfigMockedStatic.when(AppConfig::getPlatformURL).thenReturn("http://hrpbib.hrpassport.com/");

	}

    @After
    public void tearDown() {
        commonServiceHelperMockedStatic.close();
        benefitsBatchAuthorizationMockedStatic.close();
        bssMessageConfigMockedStatic.close();
        bssSecurityUtilsMockedStatic.close();
        uriComponentsBuilderMockedStatic.close();
        appConfigMockedStatic.close();
    }

	@Test
    @Ignore//revisit
	public void getBenefitPlanAvailabilityTest1() throws Exception {

        benefitsBatchAuthorizationMockedStatic
                .when(() -> BenefitsBatchAuthorization.addAuthHeaders(any(HttpHeaders.class)));
        bssMessageConfigMockedStatic
                .when(() -> BSSMessageConfig.getProperty(PlanAvailabilityURIConstants.PLAN_AVAILABILITY_API_URI))
                .thenReturn("URI_STRING");

        bssSecurityUtilsMockedStatic
                .when(() -> BSSSecurityUtils.getAuthenticatedCompanyCode(any(HttpServletRequest.class)))
                .thenReturn(COMPANY_CODE);

        bssSecurityUtilsMockedStatic
                .when(BSSSecurityUtils::getAuthenticatedPersonId)
                .thenReturn("12345");
		when(UriComponentsBuilder.fromHttpUrl(anyString())).thenReturn(uriComponentsBuilder);
		when(uriComponentsBuilder.buildAndExpand(any(Map.class))).thenReturn(uriComponents);
		when(uriComponents.toString()).thenReturn("URI_STRING");

		when(CommonServiceHelper.objectToJsonString(any(PlanAvailableRequest.class))).thenReturn("REQUEST_BODY");
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
				any(ParameterizedTypeReference.class))).thenReturn(preparePlanAvailableResponse());

		CompletableFuture<List<PlanAvailableResponse>> actualResult = planAvailabilityServiceImpl.getBenefitPlanAvailability(preparePlanAvailableRequest());
		List<PlanAvailableResponse> planAvailableResponses = actualResult.get();
		assertEquals(3, planAvailableResponses.size());
		assertEquals("94501", planAvailableResponses.get(0).getPostal());
		assertEquals(5, planAvailableResponses.get(0).getPlansByBenType().size());
		assertTrue(planAvailableResponses.get(0).getPlansByBenType().get(0).getPlanIds().contains("MED_PLAN_1"));
		assertTrue(planAvailableResponses.get(0).getPlansByBenType().get(1).getPlanIds().contains("DEN_PLAN_2"));
		assertTrue(planAvailableResponses.get(0).getPlansByBenType().get(2).getPlanIds().contains("VOL_DEN_PLAN_1"));
		assertTrue(planAvailableResponses.get(0).getPlansByBenType().get(3).getPlanIds().contains("VIS_PLAN_1"));
		assertTrue(planAvailableResponses.get(0).getPlansByBenType().get(4).getPlanIds().contains("VOL_VIS_PLAN_1"));

		assertEquals("07060", planAvailableResponses.get(1).getPostal());
		assertEquals(5, planAvailableResponses.get(1).getPlansByBenType().size());
		assertTrue(planAvailableResponses.get(1).getPlansByBenType().get(0).getPlanIds().contains("MED_PLAN_2"));
		assertTrue(planAvailableResponses.get(1).getPlansByBenType().get(1).getPlanIds().contains("DEN_PLAN_1"));
		assertTrue(planAvailableResponses.get(1).getPlansByBenType().get(2).getPlanIds().contains("VOL_DEN_PLAN_1"));
		assertTrue(planAvailableResponses.get(1).getPlansByBenType().get(3).getPlanIds().contains("VIS_PLAN_2"));
		assertTrue(planAvailableResponses.get(1).getPlansByBenType().get(4).getPlanIds().contains("VOL_VIS_PLAN_2"));

		assertEquals("10001", planAvailableResponses.get(2).getPostal());
		assertEquals(5, planAvailableResponses.get(2).getPlansByBenType().size());
		assertTrue(planAvailableResponses.get(2).getPlansByBenType().get(0).getPlanIds().contains("MED_PLAN_3"));
		assertTrue(planAvailableResponses.get(2).getPlansByBenType().get(1).getPlanIds().contains("DEN_PLAN_3"));
		assertTrue(planAvailableResponses.get(2).getPlansByBenType().get(2).getPlanIds().contains("VOL_DEN_PLAN_3"));
		assertTrue(planAvailableResponses.get(2).getPlansByBenType().get(3).getPlanIds().contains("VIS_PLAN_3"));
		assertTrue(planAvailableResponses.get(2).getPlansByBenType().get(4).getPlanIds().contains("VOL_VIS_PLAN_3"));

	}

	private PlanAvailableRequest preparePlanAvailableRequest() {
		PlanAvailableRequest planAvailableRequest = new PlanAvailableRequest();
		List<PlanAvailableRequest.Location> locationList = new ArrayList<>();

		locationList.add(PlanAvailableRequest.Location.builder().state("CA").postalCode("94044").build());
		locationList.add(PlanAvailableRequest.Location.builder().state("NJ").postalCode("07060").build());
		locationList.add(PlanAvailableRequest.Location.builder().state("NY").postalCode("10001").build());

		planAvailableRequest.setCloneBenefitProgram("102");
		planAvailableRequest.setEffectiveDate(new Date());
		planAvailableRequest.setLocations(locationList);
		planAvailableRequest.setPlans(new ArrayList<>(Arrays.asList("000OCR", "0024E3", "0024E4", "0024E5", "0024E7", "0024E9", "0024EA", "0024EZ", "0024F2", "0024F5", "0024F8", "0024GF", "002M3V", "0033QU", "0033QV", "0033QW", "0033QX", "003V06", "003V07", "005AJE", "005AKH", "005ARH", "005ARI", "005ARJ", "005ARK", "0065LC", "0065LG", "0065LK", "0065LO", "0065LS", "0065PH", "0065PL", "0065PP", "0065PT", "0065PX")));

		return planAvailableRequest;
	}

	private ResponseEntity<ReturnResponse<List<PlanAvailableResponse>>> preparePlanAvailableResponse() {
		ReturnResponse<List<PlanAvailableResponse>> response = new ReturnResponse<>();
		List<PlanAvailableResponse> planAvailableResponses;

		planAvailableResponses =  List.of(
				PlanAvailableResponse.builder().postal("94501").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MED_PLAN_1", "MED_PLAN_2", "MED_PLAN_3")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DEN_PLAN_1", "DEN_PLAN_2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1D").planIds(List.of("VOL_DEN_PLAN_1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("VIS_PLAN_1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1V").planIds(List.of("VOL_VIS_PLAN_1")).build()))
						.build(),
				PlanAvailableResponse.builder().postal("07060").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MED_PLAN_2", "MED_PLAN_3")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DEN_PLAN_1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1D").planIds(List.of("VOL_DEN_PLAN_1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("VIS_PLAN_1", "VIS_PLAN_2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1V").planIds(List.of("VOL_VIS_PLAN_1", "VOL_VIS_PLAN_2")).build()))
						.build(),
				PlanAvailableResponse.builder().postal("10001").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MED_PLAN_3")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DEN_PLAN_3")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1D").planIds(List.of("VOL_DEN_PLAN_3")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("VIS_PLAN_3")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1V").planIds(List.of("VOL_VIS_PLAN_3")).build()))
						.build());

		response.setData(planAvailableResponses);
		response.setStatusCode(String.valueOf(HttpStatus.OK.value()));
		return new ResponseEntity(response, HttpStatus.OK);
	}

}
