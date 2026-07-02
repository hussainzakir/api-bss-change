/**
 * 
 */
package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.helper.PlanSelectionServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.HrisPlanAvailabilityService;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.impl.ProspectPlanAvailabilityServiceImpl;
import com.trinet.ambis.service.model.planAvailability.HrisPlanRequest;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.util.Utils;

@RunWith(MockitoJUnitRunner.class)
public class ProspectPlanAvailabilityServiceImplTest extends ServiceUnitTest {
	
	@InjectMocks
	ProspectPlanAvailabilityServiceImpl prospectPlanAvailabilityService;

	@Mock
	ProspectCensusService prospectCensusService;

	@Mock
	PlanAvailabilityService planAvailabilityService;

	@Mock
	HrisPlanAvailabilityService hrisPlanAvailabilityService;

	private Company company;
	private List<String> plans;
	private MockedStatic<PlanSelectionServiceHelper> mockStaticPlanSelectionServiceHelper;

	@Before
	public void setUp() {
		company = prepareCompany();
		plans = preparePlans();
		if (mockStaticPlanSelectionServiceHelper == null) {
			mockStaticPlanSelectionServiceHelper = Mockito.mockStatic(PlanSelectionServiceHelper.class);
		}
	}

    @After
    public void tearDown() {
        mockStaticPlanSelectionServiceHelper.close();
    }

	@Test
	public void getProspectEmployeePlanAvailability() {

		when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareCensusData());
		when(planAvailabilityService.getBenefitPlanAvailability(Mockito.any(PlanAvailableRequest.class))).thenReturn(CompletableFuture.completedFuture(preparePlanAvailableResponse()));

		List<PlanAvailableResponse> actualResult = prospectPlanAvailabilityService.getProspectEmployeePlanAvailability(company, plans);
		assertEquals(3, actualResult.size());
		assertEquals(3, actualResult.get(0).getPlansByBenType().size());
		assertEquals(3, actualResult.get(1).getPlansByBenType().size());
		assertEquals(3, actualResult.get(2).getPlansByBenType().size());
	}

	@Test
	public void getProspectEmployeePlansByPlanType() {
		when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareCensusData());
		when(planAvailabilityService.getBenefitPlanAvailability(Mockito.any(PlanAvailableRequest.class))).thenReturn(CompletableFuture.completedFuture(preparePlanAvailableResponse()));

		Map<String, List<String>> actualResult = prospectPlanAvailabilityService.getProspectEmployeePlansByPlanType(company, plans);
		assertEquals(3, actualResult.size());
		assertEquals(2, actualResult.get("10").size());
		assertEquals(2, actualResult.get("11").size());
		assertEquals(1, actualResult.get("14").size());
	}
	@Test
	public void getProspectEmployeeHrisPlanAvailability() {

		when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareCensusData());
		when(hrisPlanAvailabilityService.getBenefitPlanAvailability(Mockito.any(HrisPlanRequest.class), Mockito.anyString())).thenReturn(prepareHrisPlanResponse());
		List<HrisPlanResponse> actualResult = prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.MEDICAL);
		assertEquals(2, actualResult.size());

	}

	@Test
	public void getProspectEmployeeHrisPlanAvailabilityException() {

		try {
			prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.DENTAL);
			fail("Expected IllegalArgumentException to be thrown");
		} catch (IllegalArgumentException e) {
			assertEquals("Company naicsCode is required", e.getMessage());
		}

	}

	@Test
	public void getProspectEmployeeHrisPlanAvailabilityInvalidPlanNameException() {

		try {
			prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			fail("Expected IllegalArgumentException to be thrown");
		} catch (IllegalArgumentException e) {
			assertEquals("The benefitType must be a primary plan type name", e.getMessage());
		}

	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode("282357");
		company.setRealm(new Realm());
		company.getRealm().setBenExchange("TRINET_IV");
		company.setBenefitStartDate("01-JAN-2025");
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(1);
		realmPlanYear.setPlanYearEnd(Utils.convertStringToDate("2021-12-31", "yyyy-MM-dd"));
		realmPlanYear.setCloneProgram("109");
		company.setRealmPlanYear(realmPlanYear);
		return company;
	}

	private List<String> preparePlans() {
		List<String> plans = new ArrayList<>();
		plans.add("MEDPLAN1");
		plans.add("MEDPLAN2");
		plans.add("DENPLAN1");
		plans.add("DENPLAN2");
		plans.add("VISPLAN1");
		return plans;
	}

	private static List<ProspectCensusResponse> prepareCensusData() {
		List<ProspectCensusResponse> list = new ArrayList<>();
		list.add(ProspectCensusResponse.builder().employeeId("EE001").employeeName("Jules Verne").state("NJ").zip("07060").medicalTier("1").dentalTier("2").visionTier("").build());
		list.add(ProspectCensusResponse.builder().employeeId("EE002").employeeName("Charles Dickens").state("CA").zip("90210").medicalTier("").dentalTier("4").visionTier("C")
				.build());
		list.add(ProspectCensusResponse.builder().employeeId("EE003").employeeName("Vincent Stark").state("SC").zip("29708").medicalTier("1").dentalTier("2").visionTier("").build());
		return list;
	}

	private List<PlanAvailableResponse> preparePlanAvailableResponse() {
		List<PlanAvailableResponse> planAvailableResponses;

		planAvailableResponses =  List.of(
				PlanAvailableResponse.builder().postal("07060").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MEDPLAN1", "MEDPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DENPLAN1", "DENPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("VISPLAN1")).build()))
						.build(),
				PlanAvailableResponse.builder().postal("90210").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MEDPLAN1", "MEDPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DENPLAN1", "DENPLAN2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("VISPLAN1")).build()))
						.build(),
				PlanAvailableResponse.builder().postal("29708").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MEDPLAN1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DENPLAN1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("VISPLAN1")).build()))
						.build());

		return planAvailableResponses;
	}

	private List<HrisPlanResponse> prepareHrisPlanResponse() {
		List<HrisPlanResponse> hrisPlanResponses;

		hrisPlanResponses = List.of(
				HrisPlanResponse.builder().planId(123456).planName("HRIS Medical Plan")
						.carrierId(1).carrierName("HRIS Carrier")
						.rateDetails(HrisPlanResponse.RateDetails.builder()
								.rateType("4Tier")
								.ratesByZip(List.of(
										HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(List.of("07060", "90210"))
												.rates(List.of(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("1").rate(100.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("2").rate(200.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("C").rate(300.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("4").rate(400.0).build()))
												.build(),
										HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(List.of("29708"))
												.rates(List.of(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("1").rate(150.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("2").rate(250.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("C").rate(350.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("4").rate(450.0).build()))
												.build()))
								.build())
						.build(),
				HrisPlanResponse.builder().planId(234567).planName("HRIS Medical Plan 2")
						.carrierId(1).carrierName("HRIS Carrier")
						.rateDetails(HrisPlanResponse.RateDetails.builder()
								.rateType("ageBanded")
								.ratesByZip(List.of(
										HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(List.of("07060", "90210"))
												.rates(List.of(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("0").rate(100.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("15").rate(200.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("16").rate(300.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("17").rate(400.0).build()))
												.build(),
										HrisPlanResponse.RateDetails.RatesByZip.builder()
												.zips(List.of("29708"))
												.rates(List.of(
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("0").rate(150.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("15").rate(250.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("16").rate(350.0).build(),
														HrisPlanResponse.RateDetails.RatesByZip.Rate.builder().tierCode("17").rate(450.0).build()))
												.build()))
								.build())
						.build());

		return hrisPlanResponses;
	}
}

