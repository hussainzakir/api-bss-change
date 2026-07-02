package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.DefaultPlanDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.EmplDefaultPlanAssignmentService;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.dto.EmplDefaultPlanAssignmentDto;
import com.trinet.ambis.service.impl.ProspectDefaultPlanMappingServiceImpl;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.BssCoreServiceClient;

@RunWith(MockitoJUnitRunner.class)
public class ProspectDefaultPlanMappingServiceTest extends ServiceUnitTest {

	@InjectMocks
	private ProspectDefaultPlanMappingServiceImpl prospectDefaultPlanMappingService;

	@Mock
	private ProspectCensusService prospectCensusService;

	@Mock
	private DefaultPlanDataDao defaultPlanDataDao;

	@Mock
	private EmplDefaultPlanAssignmentService emplDefaultPlanAssignmentService;

	@Mock
	private CompanyService companyService;

	@Mock
	private RealmPlanYearService realmPlanYearService;

	@Mock
	PlanAvailabilityService planAvailabilityService;

	@Mock
	BssCoreServiceClient bssCoreServiceClient;

	@Captor
	ArgumentCaptor<List<EmplDefaultPlanAssignmentDto>> empDefaultPlanArgCaptor;

	@Captor
	ArgumentCaptor<Company> companyArgCaptor;

	@Test
	public void censusDefaultRegionalPlanMapping_test1() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setOeQuarter("Q1");
		realmPlanYear.setPlanYearEnd(new Date());
		realmPlanYear.setCloneProgram("102");

		Company company = new Company();
		company.setCode("ABC");
		company.setId(123);
		company.setHeadQuatersState("FL");
		company.setRealmPlanYearId(1L);
		company.setRealmPlanYear(realmPlanYear);
		company.setProspectCompany(true);

		List<ProspectCensusResponse> census = prepareProspectEmployee();

		Map<String, Map<String, Long>> defaultPlansByPlan = prepareDefaultPlansByPlanType();

		when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(census);
		when(defaultPlanDataDao.getRegionalDefaultPlansByPlanType(company)).thenReturn(defaultPlansByPlan);
		when(planAvailabilityService.getBenefitPlanAvailability(any())).thenReturn(CompletableFuture.completedFuture(preparePlanAvailableResponse()));
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(company.getId());

		prospectDefaultPlanMappingService.createCensusDefaultRegionalPlanMapping(company);

		verify(emplDefaultPlanAssignmentService, times(1)).saveAll(empDefaultPlanArgCaptor.capture());
		assertEquals(13, empDefaultPlanArgCaptor.getAllValues().get(0).size());
		assertEquals("MED_PLAN_2", empDefaultPlanArgCaptor.getAllValues().get(0).get(0).getBenefitPlanId());
		assertEquals(1, empDefaultPlanArgCaptor.getAllValues().get(0).get(0).getPortfolioId());
		assertEquals(123, empDefaultPlanArgCaptor.getAllValues().get(0).get(0).getCompanyId());
		assertEquals("MED_PLAN_3", empDefaultPlanArgCaptor.getAllValues().get(0).get(1).getBenefitPlanId());
		assertEquals(2, empDefaultPlanArgCaptor.getAllValues().get(0).get(1).getPortfolioId());
		assertEquals(123, empDefaultPlanArgCaptor.getAllValues().get(0).get(1).getCompanyId());
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(company.getId());
	}

	@Test
	public void censusDefaultRegionalPlanMapping_test2() {
		// given
		// data
		List<ProspectCensusResponse> census = prepareProspectEmployee();
		String companyCode = "PROSPECT-COMP";
		Company comp = new Company();
		RealmPlanYear rpy70 = prepareRealmPlanYear(70);
		comp.setCode(companyCode);
		comp.setRealmPlanYear(rpy70);

		List<EmplDefaultPlanAssignmentDto> emplDefaultPlanAssignments = List.of(EmplDefaultPlanAssignmentDto.builder()
				.companyId(1111).planType("10").portfolioId(1).benefitPlanId("XYZ").build());

		Map<String, Map<String, Long>> defaultPlansByPlan = prepareDefaultPlansByPlanType();

		when(defaultPlanDataDao.getRegionalDefaultPlansByPlanType(comp)).thenReturn(defaultPlansByPlan);
		when(planAvailabilityService.getBenefitPlanAvailability(any())).thenReturn(CompletableFuture.completedFuture(preparePlanAvailableResponse()));
//		doNothing().when(emplDefaultPlanAssignmentService).saveAll(emplDefaultPlanAssignments);

		// when
		prospectDefaultPlanMappingService.createCensusDefaultRegionalPlanMapping(comp, census);
		// then
		// verify
		verify(defaultPlanDataDao, times(1)).getRegionalDefaultPlansByPlanType(any(Company.class));
		verify(emplDefaultPlanAssignmentService, times(1)).saveAll(empDefaultPlanArgCaptor.capture());

	}

	@Test
	public void censusDefaultRegionalPlanMappingForClient() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setOeQuarter("Q1");
		realmPlanYear.setPlanYearEnd(new Date());
		realmPlanYear.setCloneProgram("102");

		Company company = new Company();
		company.setCode("ABC");
		company.setId(123);
		company.setHeadQuatersState("FL");
		company.setRealmPlanYearId(1L);
		company.setRealmPlanYear(realmPlanYear);
		company.setProspectConvertedOnboardingClient(true);

		List<ProspectCensusResponse> census = prepareProspectEmployee();

		Map<String, Map<String, Long>> defaultPlansByPlan = prepareDefaultPlansByPlanType();

		when(bssCoreServiceClient.getCensusByCompanyCode(company.getCode())).thenReturn(census);
		when(defaultPlanDataDao.getRegionalDefaultPlansByPlanType(company)).thenReturn(defaultPlansByPlan);
		when(planAvailabilityService.getBenefitPlanAvailability(any())).thenReturn(CompletableFuture.completedFuture(preparePlanAvailableResponse()));
		doNothing().when(emplDefaultPlanAssignmentService).deleteEmplDefaultPlanAssignments(company.getId());

		prospectDefaultPlanMappingService.createCensusDefaultRegionalPlanMapping(company);

		verify(emplDefaultPlanAssignmentService, times(1)).saveAll(empDefaultPlanArgCaptor.capture());
		assertEquals(13, empDefaultPlanArgCaptor.getAllValues().get(0).size());
		assertEquals("MED_PLAN_2", empDefaultPlanArgCaptor.getAllValues().get(0).get(0).getBenefitPlanId());
		assertEquals(1, empDefaultPlanArgCaptor.getAllValues().get(0).get(0).getPortfolioId());
		assertEquals(123, empDefaultPlanArgCaptor.getAllValues().get(0).get(0).getCompanyId());
		assertEquals("MED_PLAN_3", empDefaultPlanArgCaptor.getAllValues().get(0).get(1).getBenefitPlanId());
		assertEquals(2, empDefaultPlanArgCaptor.getAllValues().get(0).get(1).getPortfolioId());
		assertEquals(123, empDefaultPlanArgCaptor.getAllValues().get(0).get(1).getCompanyId());
		verify(emplDefaultPlanAssignmentService, times(1)).deleteEmplDefaultPlanAssignments(company.getId());
	}

	private RealmPlanYear prepareRealmPlanYear(long id) {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		if (id == 70) {
			realmPlanYear.setId(70);
			realmPlanYear.setRealmId(3);
			realmPlanYear.setOeQuarter("Q1");
			realmPlanYear.setPlanYearEnd(new Date());
			realmPlanYear.setCloneProgram("102");
		}
		else if (id == 73) {
			realmPlanYear.setId(73);
			realmPlanYear.setRealmId(2);
			realmPlanYear.setOeQuarter("Q2");
			realmPlanYear.setPlanYearEnd(new Date());
			realmPlanYear.setCloneProgram("102");
		}

		return realmPlanYear;
	}

	private List<ProspectCensusResponse> prepareProspectEmployee() {
		return List.of(
				ProspectCensusResponse.builder().employeeName("John").state("CA").gender("M").k1(true)
						.salary(BigDecimal.valueOf(6000)).zip("28262").dob("1989-01-01").build(),
				ProspectCensusResponse.builder().employeeName("katty Scott").state("TX").gender("F").k1(false)
						.salary(BigDecimal.valueOf(4500)).zip("07305").dob("1973-09-28").build(),
				ProspectCensusResponse.builder().employeeName("Invalid Zip").state("TX").gender("F").k1(false)
						.salary(BigDecimal.valueOf(5000)).zip("NOZIP").dob("2000-03-31").build());
	}

	private List<PlanAvailableResponse> preparePlanAvailableResponse() {
		List<PlanAvailableResponse> planAvailableResponses;

		planAvailableResponses =  List.of(
				PlanAvailableResponse.builder().postal("07305").plansByBenType(List.of(
								PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(List.of("MED_PLAN_1", "MED_PLAN_2", "MED_PLAN_3")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("11").planIds(List.of("DEN_PLAN_1", "DEN_PLAN_2")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1D").planIds(List.of("VOL_DEN_PLAN_1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("14").planIds(List.of("VIS_PLAN_1")).build(),
								PlanAvailableResponse.BenTypePlan.builder().benType("1V").planIds(List.of("VOL_VIS_PLAN_1")).build()))
						.build(),
				PlanAvailableResponse.builder().postal("28262").plansByBenType(List.of(
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

		return planAvailableResponses;
	}

	private Map<String, Map<String, Long>> prepareDefaultPlansByPlanType() {
		Map<String, Map<String, Long>> defaultPlansByPlanType = new HashMap<>();
		Map<String, Long> defaultPlansByPlan = new HashMap<>();
		defaultPlansByPlan.put("MED_PLAN_1", 1L);
		defaultPlansByPlan.put("MED_PLAN_2", 1L);
		defaultPlansByPlan.put("MED_PLAN_3", 2L);
		defaultPlansByPlanType.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, defaultPlansByPlan);

		defaultPlansByPlan = new HashMap<>();
		defaultPlansByPlan.put("DEN_PLAN_1", 1L);
		defaultPlansByPlan.put("DEN_PLAN_2", 2L);
		defaultPlansByPlan.put("DEN_PLAN_3", 2L);
		defaultPlansByPlanType.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, defaultPlansByPlan);

		defaultPlansByPlan = new HashMap<>();
		defaultPlansByPlan.put("VOL_DEN_PLAN_1", 1L);
		defaultPlansByPlan.put("VOL_DEN_PLAN_3", 2L);
		defaultPlansByPlanType.put(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE, defaultPlansByPlan);

		defaultPlansByPlan = new HashMap<>();
		defaultPlansByPlan.put("VIS_PLAN_1", 1L);
		defaultPlansByPlan.put("VIS_PLAN_2", 1L);
		defaultPlansByPlan.put("VIS_PLAN_3", 2L);
		defaultPlansByPlanType.put(BSSApplicationConstants.VISION_PLAN_TYPE, defaultPlansByPlan);

		defaultPlansByPlan = new HashMap<>();
		defaultPlansByPlan.put("VOL_VIS_PLAN_1", 1L);
		defaultPlansByPlan.put("VOL_VIS_PLAN_2", 1L);
		defaultPlansByPlan.put("VOL_VIS_PLAN_3", 2L);
		defaultPlansByPlanType.put(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE, defaultPlansByPlan);

		return defaultPlansByPlanType;
	}

}