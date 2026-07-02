package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.EePlanAssignmentDao;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.model.EligiblePlanData;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.prospect.CreatePlanAssignmentsRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.PlanAssignmentsResponse;
import com.trinet.ambis.service.PlanAvailabilityService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.ProspectCensusService;
import com.trinet.ambis.service.ProspectPlanAvailabilityService;
import com.trinet.ambis.service.TibRateService;
import com.trinet.ambis.service.dto.BasePlansResDto;
import com.trinet.ambis.service.impl.PlanAssignmentsServiceImpl;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse.RateDetails;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse.RateDetails.RatesByZip;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableRequest;
import com.trinet.ambis.service.model.planAvailability.PlanAvailableResponse;
import com.trinet.ambis.service.model.prospect.ProspectCensusResponse;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;
import com.trinet.ambis.util.BssCoreServiceClient;

@RunWith(MockitoJUnitRunner.class)
public class PlanAssignmentsServiceImplTest extends ServiceUnitTest {

    @InjectMocks
    PlanAssignmentsServiceImpl planAssignmentsService;

    @Mock
    EePlanAssignmentDao eePlanAssignmentDao;

    @Mock
    EmployeeBenefitGroupDao employeeBenefitGroupDao;

    @Mock
    ProspectCensusService prospectCensusService;

    @Mock
    ProspectEmployeeCostService prospectEmployeeCostService;

    @Mock
    StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;

    @Mock
    XbssRealmPlyrPlanDao realmPlyrPlanDao;

    @Mock
    BenefitPlanDao benefitPlanDao;

    @Mock
    TibRateService tibRateService;

    @Mock
    PlanAvailabilityService planAvailabilityService;

    @Mock
    ProspectPlanAvailabilityService prospectPlanAvailabilityService;

    @Mock
    PlanSelectionService planSelectionService;
    
    @Mock
    BssCoreServiceClient bssCoreServiceClient;

    private MockedStatic<CompanyServiceHelper> mockStaticCompanyServiceHelper;

    @Before
    public void setUp() {
        mockStaticCompanyServiceHelper = Mockito.mockStatic(CompanyServiceHelper.class);
    }

    @After
    public void tearDown() {
        if (mockStaticCompanyServiceHelper != null) {
            mockStaticCompanyServiceHelper.close();
        }
    }

    private static final long STRATEGY_ID = 1234L;
    private static final long GROUP_ID = 1234L;
    private static final String COMPANY_CODE = "ABC123";
    private static final String EMPLOYEE_ID = "EE001";
    private static final String EMPLOYEE_ID1 = "EE002";
    private static final String EMPLOYEE_ID2 = "EE003";
    private static final String BENEFIT_PLAN = "001ABC";
    private static final String BENEFIT_PLAN1 = "002XYZ";
    private static final String BENEFIT_PLAN2 = "001ABD";
    private static final int PROSPECT_BENEFIT_PLAN_ID = 11111;

	@Test
	public void getPlanAssignmentsTest() {
	    // Given
	    // test data

	    Company company = new Company();
	    company.setCode(COMPANY_CODE);

	    List<String> employeesByStrategyGroup = new ArrayList<>();
	    employeesByStrategyGroup.add("EE001");
	    employeesByStrategyGroup.add("EE002");

	    List<EmployeeCostRes> prospectReturnData = populateProspectReturnData();
	    Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);
	   when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);
	    when(prospectCensusService.getProspectCensus(COMPANY_CODE)).thenReturn(prepareCensusData());
	    when(eePlanAssignmentDao.findByStrategyIdGroupId(STRATEGY_ID, GROUP_ID)).thenReturn(prepareAssignments());

	    when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(
		    employeesByStrategyGroup);
	    when(prospectEmployeeCostService.getProspectEmployeeCostByType(COMPANY_CODE,
		    BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
	    // When
	    List<PlanAssignmentsResponse> actual = planAssignmentsService.getPlanAssignments(STRATEGY_ID, GROUP_ID,
		    company);
	    // Then
	    // assertions
	    assertEquals(2, actual.size());
        // Employee EE001
	    assertEquals(2, actual.get(0).getPlanAssignment().size());
	    assertEquals("001ABC", actual.get(0).getPlanAssignment().get(0).getBenefitPlanId());
	    assertEquals("medical", actual.get(0).getPlanAssignment().get(0).getBenefitType());
	    assertNull(actual.get(0).getPlanAssignment().get(0).getTotalCost());
	    assertEquals("002XYZ", actual.get(0).getPlanAssignment().get(1).getBenefitPlanId());
	    assertEquals("dental", actual.get(0).getPlanAssignment().get(1).getBenefitType());
	    assertNull(actual.get(0).getPlanAssignment().get(1).getTotalCost());
	    assertEquals("1", actual.get(0).getMedicalCvgCd());
	    assertEquals("2", actual.get(0).getDentalCvgCd());
	    assertEquals("", actual.get(0).getVisionCvgCd());

        // Employee EE002
	    assertEquals(3, actual.get(1).getPlanAssignment().size());
        assertEquals("001ABC", actual.get(1).getPlanAssignment().get(0).getBenefitPlanId());
        assertEquals("medical", actual.get(1).getPlanAssignment().get(0).getBenefitType());
        assertNull(actual.get(1).getPlanAssignment().get(0).getTotalCost());
        assertEquals("001ABD", actual.get(1).getPlanAssignment().get(1).getBenefitPlanId());
        assertEquals("vision", actual.get(1).getPlanAssignment().get(1).getBenefitType());
        assertNull(actual.get(1).getPlanAssignment().get(0).getTotalCost());
	    assertEquals("002XYZ", actual.get(1).getPlanAssignment().get(2).getBenefitPlanId());
	    assertEquals("dental", actual.get(1).getPlanAssignment().get(2).getBenefitType());
	    assertNull(actual.get(1).getPlanAssignment().get(1).getTotalCost());
	    assertEquals("C", actual.get(1).getVisionCvgCd());
	    assertEquals("", actual.get(1).getMedicalCvgCd());
	    assertEquals("4", actual.get(1).getDentalCvgCd());
	    // verify
	    verify(prospectCensusService).getProspectCensus(COMPANY_CODE);
	    verify(eePlanAssignmentDao).findByStrategyIdGroupId(STRATEGY_ID, GROUP_ID);
	}

    @Test
    public void getPlanAssignmentsForTIBTest() {
        // Given
        // test data

        Company company = new Company();
        company.setCode(COMPANY_CODE);

        List<String> employeesByStrategyGroup = new ArrayList<String>();
        employeesByStrategyGroup.add("EE001");
        employeesByStrategyGroup.add("EE002");
        employeesByStrategyGroup.add("EE003");
        employeesByStrategyGroup.add("EE004");

        List<EmployeeCostRes> prospectReturnData = populateProspectReturnData();
        Optional<List<EmployeeCostRes>> prospectData = Optional.ofNullable(prospectReturnData);

        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectCensusService.getProspectCensus(COMPANY_CODE)).thenReturn(prepareCensusData());
        when(eePlanAssignmentDao.findByStrategyIdGroupId(STRATEGY_ID, GROUP_ID)).thenReturn(prepareAssignments());

        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(
                employeesByStrategyGroup);
        when(prospectEmployeeCostService.getProspectEmployeeCostByType(COMPANY_CODE,
                BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER)).thenReturn(prospectData);
        // When
        List<PlanAssignmentsResponse> actual = planAssignmentsService.getPlanAssignments(STRATEGY_ID, GROUP_ID,
                company);
        // Then
        // assertions
        assertEquals(4, actual.size());
        // Employee EE001
        assertEquals(2, actual.get(0).getPlanAssignment().size());
        assertEquals("001ABC", actual.get(0).getPlanAssignment().get(0).getBenefitPlanId());
        assertEquals("medical", actual.get(0).getPlanAssignment().get(0).getBenefitType());
        assertEquals(BigDecimal.valueOf(1500), actual.get(0).getPlanAssignment().get(0).getTotalCost());
        assertEquals("002XYZ", actual.get(0).getPlanAssignment().get(1).getBenefitPlanId());
        assertEquals("dental", actual.get(0).getPlanAssignment().get(1).getBenefitType());
        assertEquals(BigDecimal.valueOf(1600), actual.get(0).getPlanAssignment().get(1).getTotalCost());
        assertEquals("1", actual.get(0).getMedicalCvgCd());
        assertEquals("2", actual.get(0).getDentalCvgCd());
        assertEquals("", actual.get(0).getVisionCvgCd());
        // Employee EE002
        assertEquals("001ABC", actual.get(1).getPlanAssignment().get(0).getBenefitPlanId());
        assertEquals("medical", actual.get(1).getPlanAssignment().get(0).getBenefitType());
        assertNull(actual.get(1).getPlanAssignment().get(0).getTotalCost());
        assertEquals("001ABD", actual.get(1).getPlanAssignment().get(1).getBenefitPlanId());
        assertEquals("vision", actual.get(1).getPlanAssignment().get(1).getBenefitType());
        assertEquals(BigDecimal.valueOf(900), actual.get(1).getPlanAssignment().get(1).getTotalCost());
        assertEquals("002XYZ", actual.get(1).getPlanAssignment().get(2).getBenefitPlanId());
        assertEquals("dental", actual.get(1).getPlanAssignment().get(2).getBenefitType());
        assertEquals(BigDecimal.valueOf(1700), actual.get(1).getPlanAssignment().get(2).getTotalCost());
        assertEquals("C", actual.get(1).getVisionCvgCd());
        assertEquals("", actual.get(1).getMedicalCvgCd());
        assertEquals("4", actual.get(1).getDentalCvgCd());
        // Employee EE003
        assertEquals("001ABC", actual.get(2).getPlanAssignment().get(0).getBenefitPlanId());
        assertEquals("medical", actual.get(2).getPlanAssignment().get(0).getBenefitType());
        assertEquals(BigDecimal.valueOf(1000), actual.get(2).getPlanAssignment().get(0).getTotalCost());
        // Employee EE004
        assertEquals("001ABC", actual.get(3).getPlanAssignment().get(0).getBenefitPlanId());
        assertEquals("medical", actual.get(3).getPlanAssignment().get(0).getBenefitType());
        assertEquals(BigDecimal.valueOf(1000), actual.get(3).getPlanAssignment().get(0).getTotalCost());
        // verify
        verify(prospectCensusService).getProspectCensus(COMPANY_CODE);
        verify(eePlanAssignmentDao).findByStrategyIdGroupId(STRATEGY_ID, GROUP_ID);
    }
    
    @Test
    public void getPlanAssignmentsBssCoreTest1() {
        // Given
        Company company = new Company();
        company.setCode(COMPANY_CODE);
        company.setProspectConvertedClient(true);

        List<String> employeesByStrategyGroup = Arrays.asList("EE001", "EE002");

        List<ProspectCensusResponse> bssCoreCensus = new ArrayList<>();
        bssCoreCensus.add(ProspectCensusResponse.builder()
                .employeeId("EE001")
                .employeeName("John Doe")
                .state("CA")
                .zip("12345")
                .build());
        bssCoreCensus.add(ProspectCensusResponse.builder()
                .employeeId("EE002")
                .employeeName("Jane Smith")
                .state("NY")
                .zip("54321")
                .build());

        when(bssCoreServiceClient.getCensusByCompanyCode(COMPANY_CODE)).thenReturn(bssCoreCensus);
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID))
                .thenReturn(employeesByStrategyGroup);
        when(eePlanAssignmentDao.findByStrategyIdGroupId(STRATEGY_ID, GROUP_ID))
                .thenReturn(prepareAssignments());

        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);

        // When
        List<PlanAssignmentsResponse> result =
                planAssignmentsService.getPlanAssignments(STRATEGY_ID, GROUP_ID, company);

        // Then
        assertEquals(2, result.size());
        assertEquals("EE001", result.get(0).getEmployeeId());
        assertEquals("EE002", result.get(1).getEmployeeId());

        verify(bssCoreServiceClient).getCensusByCompanyCode(COMPANY_CODE);
        verify(prospectCensusService, times(0)).getProspectCensus(anyString());
    }
    
    
	@Test
	public void createPlanAssignmentsTest1() {
		// simple success test
		// Given
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(70);
		// test data
		when(prospectCensusService.getProspectCensus(COMPANY_CODE)).thenReturn(prepareCensusData());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EePlanAssignment>> planAssignCaptor = ArgumentCaptor.forClass(List.class);
        when(eePlanAssignmentDao.saveAllAndFlush(planAssignCaptor.capture())).thenAnswer(args -> {
            return args.getArgument(0);
        });

        when(realmPlyrPlanDao.validatePlanForStrategyYear(any(), anyLong())).thenReturn(Arrays.asList("PLAN1", BENEFIT_PLAN, "PLAN3"));

        // When
        planAssignmentsService.createPlanAssignments(STRATEGY_ID, company, preparePlanAssignmentsRequest());

        List<EePlanAssignment> entities = planAssignCaptor.getValue();
        EePlanAssignment actual = entities.get(0);
        EePlanAssignmentPK eePlanAssignmentPk = actual.getEePlanAssignmentPK();

        // Then
        assertEquals(1, entities.size());
        assertEquals(STRATEGY_ID, eePlanAssignmentPk.getStrategyId());
        assertEquals(EMPLOYEE_ID, eePlanAssignmentPk.getEmplId());
        assertEquals(BENEFIT_PLAN, actual.getBenefitPlan());
    }

    @Test
    public void createPlanAssignmentsTest2() {
        // invalid employee ID test
        // Given
        Company company = new Company();
        company.setCode(COMPANY_CODE);
        company.setRealmPlanYearId(70);
        // test data
        when(prospectCensusService.getProspectCensus(COMPANY_CODE)).thenReturn(prepareCensusData());
        List<CreatePlanAssignmentsRequest> request = preparePlanAssignmentsRequest();
        request.get(0).setEmployeeId("INV001");

        // When
        BSSApplicationException outerException = null;
        try {
            planAssignmentsService.createPlanAssignments(STRATEGY_ID, company, request);
        } catch (BSSApplicationException ex) {
            outerException = ex;
        }

        // Then
        assertEquals(400, outerException.getBssError().getStatus());
        assertEquals("Invalid EmployeeId 'INV001'", outerException.getMessage());
    }

    @Test
    public void createPlanAssignmentsTest3() {
        // invalid benefit plan test
        // Given
        Company company = new Company();
        company.setCode(COMPANY_CODE);
        company.setRealmPlanYearId(70);
        // test data
        when(prospectCensusService.getProspectCensus(COMPANY_CODE)).thenReturn(prepareCensusData());
        when(realmPlyrPlanDao.validatePlanForStrategyYear(any(), anyLong())).thenReturn(Arrays.asList("INV001", "INV002"));

        // When
        BSSApplicationException outerException = null;
        try {
            planAssignmentsService.createPlanAssignments(STRATEGY_ID, company, preparePlanAssignmentsRequest());
        } catch (BSSApplicationException ex) {
            outerException = ex;
        }

        // Then
        assertEquals(400, outerException.getBssError().getStatus());
        assertEquals("Benefit plan '001ABC' not valid for indicated strategy 1234", outerException.getMessage());
    }

    @Test
    public void createPlanAssignmentsTest4() {
        // OMS TIB company test
        // Given
        Company company = new Company();
        company.setCode(COMPANY_CODE);
        company.setRealmPlanYearId(70);
        // test data
        when(prospectCensusService.getProspectCensus(COMPANY_CODE)).thenReturn(prepareCensusData());
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        doNothing().when(tibRateService).saveRatesPerEmployeeIds(company, STRATEGY_ID, Set.of(EMPLOYEE_ID), List.of("10"));
        doNothing().when(planSelectionService).syncOmsMedicalPlanSelections(STRATEGY_ID);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EePlanAssignment>> planAssignCaptor = ArgumentCaptor.forClass(List.class);
        when(eePlanAssignmentDao.saveAllAndFlush(planAssignCaptor.capture())).thenAnswer(args -> {
            return args.getArgument(0);
        });

        when(realmPlyrPlanDao.validatePlanForStrategyYear(any(), anyLong())).thenReturn(Arrays.asList("PLAN1", BENEFIT_PLAN, "PLAN3"));

        // When
        planAssignmentsService.createPlanAssignments(STRATEGY_ID, company, preparePlanAssignmentsRequest());

        List<EePlanAssignment> entities = planAssignCaptor.getValue();
        EePlanAssignment actual = entities.get(0);
        EePlanAssignmentPK eePlanAssignmentPk = actual.getEePlanAssignmentPK();

        // Then
        assertEquals(1, entities.size());
        assertEquals(STRATEGY_ID, eePlanAssignmentPk.getStrategyId());
        assertEquals(EMPLOYEE_ID, eePlanAssignmentPk.getEmplId());
        assertEquals(BENEFIT_PLAN, actual.getBenefitPlan());
        verify(tibRateService).saveRatesPerEmployeeIds(company, STRATEGY_ID, Set.of(EMPLOYEE_ID), List.of("10"));
        verify(planSelectionService).syncOmsMedicalPlanSelections(STRATEGY_ID);
    }
    
    @Test
    public void createPlanAssignmentsTest5() {
        Company company = new Company();
        company.setCode(COMPANY_CODE);
        company.setProspectConvertedClient(true); // converted!

        List<ProspectCensusResponse> bssCoreCensus = new ArrayList<>();
        bssCoreCensus.add(ProspectCensusResponse.builder()
                .employeeId(EMPLOYEE_ID)
                .build());
        when(bssCoreServiceClient.getCensusByCompanyCode(COMPANY_CODE)).thenReturn(bssCoreCensus);

        when(realmPlyrPlanDao.validatePlanForStrategyYear(any(), anyLong())).thenReturn(
                Arrays.asList(BENEFIT_PLAN));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EePlanAssignment>> planAssignCaptor = ArgumentCaptor.forClass(
                List.class);
        when(eePlanAssignmentDao.saveAllAndFlush(planAssignCaptor.capture()))
                .thenAnswer(args -> args.getArgument(0));

        planAssignmentsService.createPlanAssignments(STRATEGY_ID, company,
                preparePlanAssignmentsRequest());

        verify(bssCoreServiceClient).getCensusByCompanyCode(COMPANY_CODE);
        verify(prospectCensusService, times(0)).getProspectCensus(anyString());
    }

    @Test
    public void createPlanAssignmentsTest6() {
        // OMS TIB company test
        // Given
        Company company = new Company();
        company.setCode(COMPANY_CODE);
        company.setRealmPlanYearId(70);
        // test data
        when(prospectCensusService.getProspectCensus(COMPANY_CODE)).thenReturn(prepareCensusData());
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        doNothing().when(tibRateService).saveRatesPerEmployeeIds(any(), anyLong(), anySet(), anyList());
        doNothing().when(planSelectionService).syncOmsMedicalPlanSelections(STRATEGY_ID);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EePlanAssignment>> planAssignCaptor = ArgumentCaptor.forClass(List.class);
        when(eePlanAssignmentDao.saveAllAndFlush(planAssignCaptor.capture())).thenAnswer(args -> {
            return args.getArgument(0);
        });

        when(realmPlyrPlanDao.validatePlanForStrategyYear(any(), anyLong())).thenReturn(Arrays.asList("PLAN1", BENEFIT_PLAN, "PLAN3"));

        // When
        planAssignmentsService.createPlanAssignments(STRATEGY_ID, company, preparePlanAssignmentsRequestWithAllBenefitTypes());

        List<EePlanAssignment> entities = planAssignCaptor.getValue();
        EePlanAssignment actual = entities.get(0);
        EePlanAssignmentPK eePlanAssignmentPk = actual.getEePlanAssignmentPK();

        EePlanAssignment actual1 = entities.get(1);
        EePlanAssignmentPK eePlanAssignmentPk1 = actual1.getEePlanAssignmentPK();

        EePlanAssignment actual2 = entities.get(2);
        EePlanAssignmentPK eePlanAssignmentPk2 = actual2.getEePlanAssignmentPK();

        // Then
        assertEquals(3, entities.size());
        assertEquals(STRATEGY_ID, eePlanAssignmentPk.getStrategyId());
        assertEquals(EMPLOYEE_ID, eePlanAssignmentPk.getEmplId());
        assertEquals(BENEFIT_PLAN, actual.getBenefitPlan());
        assertEquals(EMPLOYEE_ID1, eePlanAssignmentPk1.getEmplId());
        assertEquals(BENEFIT_PLAN1, actual1.getBenefitPlan());
        assertEquals(EMPLOYEE_ID2, eePlanAssignmentPk2.getEmplId());
        assertEquals(BENEFIT_PLAN2, actual2.getBenefitPlan());
        verify(tibRateService).saveRatesPerEmployeeIds(company, STRATEGY_ID, Set.of(EMPLOYEE_ID), List.of("10"));
        verify(tibRateService).saveRatesPerEmployeeIds(company, STRATEGY_ID, Set.of(EMPLOYEE_ID1), List.of("11"));
        verify(tibRateService).saveRatesPerEmployeeIds(company, STRATEGY_ID, Set.of(EMPLOYEE_ID2), List.of("14"));
        verify(planSelectionService).syncOmsMedicalPlanSelections(STRATEGY_ID);
    }

    @Test
    public void getBasPlansEmptyTest() {
        // Given
        // no test data

        // When
        List<BasePlansResDto> l = planAssignmentsService.getBasePlans(STRATEGY_ID, GROUP_ID, new Company());

        // Then an empty but valid List is returned
        assertEquals(0, l.size());
    }

    @Test
    public void getBasPlansValidTest() {
        // Given test data:
        Company company = new Company();
        when(strategyGroupPlanSelectDao.getBasePlans(STRATEGY_ID, GROUP_ID)).thenReturn(prepareBasePlanResult());

        // When
        List<BasePlansResDto> result = planAssignmentsService.getBasePlans(STRATEGY_ID, GROUP_ID, company);

        // Then a List of base plan objects is returned, with expected contents
        assertEquals(3, result.size());
        BasePlansResDto med = result.stream().filter(p -> "medical".equals(p.getBenType())).findFirst().get();
        BasePlansResDto den = result.stream().filter(p -> "dental".equals(p.getBenType())).findFirst().get();
        BasePlansResDto vis = result.stream().filter(p -> "vision".equals(p.getBenType())).findFirst().get();
        assertEquals(2, med.getPlans().size());
        assertEquals(1, den.getPlans().size());
        assertEquals(3, vis.getPlans().size());

        assertEquals("UHC Core 500 CA", med.getPlans().get(0).getPlanName());
        assertEquals("UHC Core 2500 CA", med.getPlans().get(1).getPlanName());

        // Verify
        verify(prospectPlanAvailabilityService, times(0)).getProspectEmployeeHrisPlanAvailability(company);
    }

    @Test
    public void getBasPlansForTIBTest() {
        // Given test data:
        Company company = new Company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(strategyGroupPlanSelectDao.getBasePlans(STRATEGY_ID, GROUP_ID)).thenReturn(prepareBasePlanResult());
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.MEDICAL))
                .thenReturn(prepareHrisPlanResponse(BSSApplicationConstants.MEDICAL));
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.DENTAL))
                .thenReturn(prepareHrisPlanResponse(BSSApplicationConstants.DENTAL));
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.VISION))
                .thenReturn(prepareHrisPlanResponse(BSSApplicationConstants.VISION));

        // When
        List<BasePlansResDto> result = planAssignmentsService.getBasePlans(STRATEGY_ID, GROUP_ID, company);

        // Then a List of base plan objects is returned, with expected contents
        assertEquals(3, result.size());
        BasePlansResDto med = result.stream().filter(p -> "medical".equals(p.getBenType())).findFirst().get();
        BasePlansResDto den = result.stream().filter(p -> "dental".equals(p.getBenType())).findFirst().get();
        BasePlansResDto vis = result.stream().filter(p -> "vision".equals(p.getBenType())).findFirst().get();
        assertEquals(2, med.getPlans().size());
        assertEquals(3, den.getPlans().size());
        assertEquals(1, vis.getPlans().size());

        assertEquals("HRIS Carrier 1 HRIS Medical Plan 1", med.getPlans().get(0).getPlanName());
        assertEquals("HRIS Carrier 2 HRIS Medical Plan 2", med.getPlans().get(1).getPlanName());
        assertEquals("HRIS Carrier 4 HRIS Dental Plan 1", den.getPlans().get(0).getPlanName());
        assertEquals("HRIS Carrier 5 HRIS Dental Plan 2", den.getPlans().get(1).getPlanName());
        assertEquals("HRIS Carrier 6 HRIS Dental Plan 3", den.getPlans().get(2).getPlanName());
        assertEquals("HRIS Carrier 3 HRIS Vision Plan 1", vis.getPlans().get(0).getPlanName());
    }
   
    @Test
    public void getProspectEmployeesEligibleForHrisPlanTest() {
        Company company = new Company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareProspectCensusData());
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(prepareEmployeesByStrategyGroup());
        var result = planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, "a", "10");
        assertTrue(result.isEmpty());
    }

    @Test
    public void getProspectEmployeesEligibleForHrisPlanTest2() {
        Company company = new Company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareProspectCensusData());
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(List.of());
//        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company)).thenReturn(hrisPlanResponse());
        var result = planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, "a", "10");
        assertTrue(result.isEmpty());
    }

    @Test
    public void getProspectEmployeesEligibleForHrisPlanTest3() {
        Company company = new Company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareProspectCensusData());
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(List.of());
        var result = planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, "a", "10");
        assertTrue(result.isEmpty());
    }

    @Test
    public void getProspectEmployeesEligibleForHrisPlanTest4() {
        Company company = new Company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareProspectCensusData());
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(prepareEmployeesByStrategyGroup());
        var result = planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, "a", "10");
        assertTrue(result.isEmpty());
    }

    @Test
    public void getProspectEmployeesEligibleForHrisPlanTest5() {
        Company company = new Company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareProspectCensusData());
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(prepareEmployeesByStrategyGroup());
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.MEDICAL)).thenReturn(hrisPlanResponse1());
        var result = planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, "12", "10");
        assertFalse(result.isEmpty());
    }

    @Test
    public void getProspectEmployeesEligibleForHrisPlanTest6() {
        Company company = new Company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareProspectCensusData());
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(prepareEmployeesByStrategyGroup());
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, BSSApplicationConstants.DENTAL)).thenReturn(hrisPlanResponse1());
        var result = planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, "12", "11");
        assertFalse(result.isEmpty());
    }

    @Test
    public void getProspectEmployeesEligibleForBasePlanTest2() {
        Company company = company();
        List<String> regionalPlans = new ArrayList<String>();
        regionalPlans.add("REGIONAL_PLAN_1");
        regionalPlans.add("REGIONAL_PLAN_2");
        when(benefitPlanDao.getSelectedRegionalPlansForBasePlan(anyLong(), anyLong(), anyLong(), anyString())).thenReturn(regionalPlans);
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);
        when(prospectCensusService.getProspectCensus(company.getCode())).thenReturn(prepareProspectCensusData());
        when(employeeBenefitGroupDao.getEmployeeDetailsByStrategyAndGroup(STRATEGY_ID, GROUP_ID)).thenReturn(prepareEmployeesByStrategyGroup());
        when(planAvailabilityService.getBenefitPlanAvailability(any(PlanAvailableRequest.class))).thenReturn(preparePlanAvailableResponse());
        var result = planAssignmentsService.getProspectEmployeesEligibleForPlan(STRATEGY_ID, GROUP_ID, company, "a", "10");
        assertFalse(result.isEmpty());
    }

    @Test
    public void getEligiblePlansTest() {
        var company = company();
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, PlanTypesEnum.getName("10")))
                .thenReturn(hrisPlanResponseWithBenefitType(BSSApplicationConstants.MEDICAL, Arrays.asList("77001","94536")));
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, PlanTypesEnum.getName("11")))
                .thenReturn(hrisPlanResponseWithBenefitType(BSSApplicationConstants.DENTAL, Arrays.asList("77001","94536", "10001")));
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(company, PlanTypesEnum.getName("14")))
                .thenReturn(hrisPlanResponseWithBenefitType(BSSApplicationConstants.VISION, Arrays.asList("94536", "10001")));
        var valueM = planAssignmentsService.getEligiblePlans(1, 2, "TX", "77001", "10", company, null);

        var expM = List.of("11", "14", "12", "13");
        assertEquals(4, valueM.size());
        assertEquals(expM, valueM);
        var valueD = planAssignmentsService.getEligiblePlans(1, 2, "CA", "94536", "11", company, null);
        var expD = List.of("21", "22");
        assertEquals(2, valueD.size());
        assertEquals(expD, valueD);
        assertTrue(expD.containsAll(valueD) && valueD.containsAll(expD));
        var expV = List.of("32", "33", "31");
        var valueV = planAssignmentsService.getEligiblePlans(1, 2, "CA", "94536", "14", company, null);
        assertEquals(3, valueV.size());
        assertEquals(expV, valueV);

        // 77001 is not in the vision plans, so it should not be returned
        var valueVTX = planAssignmentsService.getEligiblePlans(1, 2, "TX", "77001", "14", company, null);
        assertEquals(0, valueVTX.size());
    }

    @Test
    public void getEligibleRegionalPlansTest() {
        var company = company();
		when(planAvailabilityService.getBenefitPlanAvailability(any(PlanAvailableRequest.class)))
				.thenReturn(preparePlanAvailableResponseForEligibilityPlans());
		when(benefitPlanDao.getBenefitPlansAndCarriersBy(1, 2, "11", company.getRealmPlanYear().getPlanYearStart(),
				"1")).thenReturn(getBenefitPlansWithCarrier());
		var value = planAssignmentsService.getEligiblePlans(1, 2, "a", "a", "11", company, "1");
		assertEquals(4, value.size());
    }

    @Test
    public void testGetPlanRatesByEmployeeTib() {
        // Given
        Company company = new Company();
        company.setCode("COMP123");
        String employeeId = "EMP001";
        String planId = "PLAN123";
        String covgLevelCode = "COVG1";
        BigDecimal expectedRate = BigDecimal.valueOf(500.00);

        when(tibRateService.getRateForEmployee(company, employeeId, planId, covgLevelCode, BSSApplicationConstants.MEDICAL_PLAN_TYPE)).thenReturn(expectedRate);

        // When
        BigDecimal actualRate = planAssignmentsService.getOmsPlanRateByEmployee(company, employeeId, planId, covgLevelCode, BSSApplicationConstants.MEDICAL_PLAN_TYPE);

        // Then
        assertEquals(expectedRate, actualRate);
        verify(tibRateService).getRateForEmployee(company, employeeId, planId, covgLevelCode, BSSApplicationConstants.MEDICAL_PLAN_TYPE);
    }
    
	@Test
	public void getEligibleRegionalPlansWithSortingTest() {
		// Given
		// test data
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setCloneProgram("CLONE_PROGRAM");
		realmPlanYear.setPlanYearEnd(new Date());
		realmPlanYear.setPlanYearStart(new Date());

		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(70);
		company.setRealmPlanYear(realmPlanYear);
		company.setBenefitStartDate("07-JUL-2020");

		when(benefitPlanDao.getBenefitPlansAndCarriersBy(STRATEGY_ID, GROUP_ID, "10",
				company.getRealmPlanYear().getPlanYearStart(), "1")).thenReturn(getBenefitPlansWithCarrier());
		when(planAvailabilityService.getBenefitPlanAvailability(any(PlanAvailableRequest.class)))
				.thenReturn(preparePlanAvailableResponseForEligibilityPlans());

		// When
		List<String> actual = planAssignmentsService.getEligiblePlans(STRATEGY_ID, GROUP_ID, "CA", "12345", "10",
				company, "1");

		// Then
		// assertions
		assertEquals(4, actual.size());
		assertEquals("Aetna Plan 1", actual.get(0));
		assertEquals("BS Plan 1", actual.get(1));
		assertEquals("BS Plan 2", actual.get(2));
		assertEquals("UHC Plan 1", actual.get(3));
		verify(benefitPlanDao).getBenefitPlansAndCarriersBy(STRATEGY_ID, GROUP_ID, "10",
				company.getRealmPlanYear().getPlanYearStart(), "1");
		verify(planAvailabilityService).getBenefitPlanAvailability(any(PlanAvailableRequest.class));
	}

	@Test
	public void getEligibleRegionalPlansWithSortingWithInvalidCvgCode() {
		// Given
		// test data
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setCloneProgram("CLONE_PROGRAM");
		realmPlanYear.setPlanYearEnd(new Date());
		realmPlanYear.setPlanYearStart(new Date());

		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setRealmPlanYearId(70);
		company.setRealmPlanYear(realmPlanYear);
		company.setBenefitStartDate("07-JUL-2020");

		BSSApplicationException e = null;
		try {
			planAssignmentsService.getEligiblePlans(STRATEGY_ID, GROUP_ID, "CA", "12345", "10", company, null);
		} catch (BSSApplicationException ex) {
			e = ex;
		}
		assertEquals(BSSErrorResponseCodes.BSS_INVALID_COVERGAE_TIER_CODE, e.getBssError().getCode());
	}
	
    private static List<ProspectCensusResponse> prepareCensusData() {
        List<ProspectCensusResponse> list = new ArrayList<>();
        list.add(ProspectCensusResponse.builder().employeeId("EE001").employeeName("Jules Verne").state("IL").zip("12345").medicalTier("1").dentalTier("2").visionTier("").build());
        list.add(ProspectCensusResponse.builder().employeeId("EE002").employeeName("Charles Dickens").state("KS").zip("67891").medicalTier("").dentalTier("4").visionTier("C")
                .build());
        list.add(ProspectCensusResponse.builder().employeeId("EE003").employeeName("Vincent Stark").state("IL").zip("12345").medicalTier("1").dentalTier("2").visionTier("").build());
        list.add(ProspectCensusResponse.builder().employeeId("EE004").employeeName("Jane Doe").state("IL").zip("12345").medicalTier("1").dentalTier("2").visionTier("").build());
        return list;
    }

	private static List<EePlanAssignment> prepareAssignments() {
		List<EePlanAssignment> list = new ArrayList<>();
		list.add(constructAssignment(STRATEGY_ID, "EE001", "001ABC", "10", "1", BigDecimal.valueOf(1500), BigDecimal.valueOf(0)));
		list.add(constructAssignment(STRATEGY_ID, "EE001", "002XYZ", "11", "2", BigDecimal.valueOf(1600), BigDecimal.valueOf(0)));
        list.add(constructAssignment(STRATEGY_ID, "EE002", "001ABC", "10", "1", null, null));
		list.add(constructAssignment(STRATEGY_ID, "EE002", "001ABD", "14", "C", BigDecimal.valueOf(900), BigDecimal.valueOf(0)));
		list.add(constructAssignment(STRATEGY_ID, "EE002", "002XYZ", "11", "4", BigDecimal.valueOf(1700), BigDecimal.valueOf(0)));
        list.add(constructAssignment(STRATEGY_ID, "EE003", "001ABC", "10", "1", BigDecimal.valueOf(1000), null));
        list.add(constructAssignment(STRATEGY_ID, "EE004", "001ABC", "10", "1", null, BigDecimal.valueOf(1000)));
		return list;
	}

	private static EePlanAssignment constructAssignment(long strategyId, String emplid, String benefitPlan,
			String benefitType, String covrgCode, BigDecimal erRate, BigDecimal eeRate ) {
		EePlanAssignment asgn = new EePlanAssignment();
		EePlanAssignmentPK pk = new EePlanAssignmentPK();
		pk.setStrategyId(strategyId);
		pk.setEmplId(emplid);
		asgn.setBenefitPlan(benefitPlan);
		asgn.setEePlanAssignmentPK(pk);
		pk.setBenefitType(benefitType);
		asgn.setCovrgCD(covrgCode);
		asgn.setErRate(erRate);
		asgn.setEeRate(eeRate);

		return asgn;
	}

    private static List<CreatePlanAssignmentsRequest> preparePlanAssignmentsRequest() {
        List<CreatePlanAssignmentsRequest> list = new ArrayList<>();
        list.add(CreatePlanAssignmentsRequest.builder().employeeId(EMPLOYEE_ID).benefitPlanId(BENEFIT_PLAN).benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).build());
        return list;
    }

    private static List<CreatePlanAssignmentsRequest> preparePlanAssignmentsRequestWithAllBenefitTypes() {
        List<CreatePlanAssignmentsRequest> list = new ArrayList<>();
        list.add(CreatePlanAssignmentsRequest.builder().employeeId(EMPLOYEE_ID)
                .benefitPlanId(BENEFIT_PLAN).benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
                .build());
        list.add(CreatePlanAssignmentsRequest.builder().employeeId(EMPLOYEE_ID1)
                .benefitPlanId(BENEFIT_PLAN1).benefitType(BSSApplicationConstants.DENTAL_PLAN_TYPE)
                .build());
        list.add(CreatePlanAssignmentsRequest.builder().employeeId(EMPLOYEE_ID2)
                .benefitPlanId(BENEFIT_PLAN2).benefitType(BSSApplicationConstants.VISION_PLAN_TYPE)
                .build());
        return list;
    }

    private List<EmployeeCostRes> populateProspectReturnData() {
        List<EmployeeCostRes> prospectReturnData = new ArrayList<>();

        // Medical
        EmployeeCostRes employeeCostRes = new EmployeeCostRes();
        employeeCostRes.setBenefitTypeCode(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
        employeeCostRes.setEmployeePlanContribution(new ArrayList<>());

        EmployeeCostRes.EmployeePlanContribution employeePlanContribution = populateEmployeePlanContribution("1", "1");
        EmployeeCostRes.PlanContribution planContributionData = populatePlanContributionData(BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("2", "2");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(200), BigDecimal.valueOf(400));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("3ProspectOnly", "C");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(300), BigDecimal.valueOf(600));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        prospectReturnData.add(employeeCostRes);

        // Dental
        employeeCostRes = new EmployeeCostRes();
        employeeCostRes.setBenefitTypeCode(BSSApplicationConstants.DENTAL_PLAN_TYPE);
        employeeCostRes.setEmployeePlanContribution(new ArrayList<>());

        employeePlanContribution = populateEmployeePlanContribution("1", "C");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(25), BigDecimal.valueOf(50));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("2", "4");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(75), BigDecimal.valueOf(100));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        employeePlanContribution = populateEmployeePlanContribution("3ProspectOnly", "1");
        planContributionData = populatePlanContributionData(BigDecimal.valueOf(10), BigDecimal.valueOf(15));
        employeePlanContribution.setPlanContribution(planContributionData);
        employeeCostRes.getEmployeePlanContribution().add(employeePlanContribution);

        prospectReturnData.add(employeeCostRes);

        return prospectReturnData;
    }

    private EmployeeCostRes.EmployeePlanContribution populateEmployeePlanContribution(String employeeIndex, String coverageLevel) {
        EmployeeCostRes.EmployeePlanContribution employeePlanContribution = new EmployeeCostRes.EmployeePlanContribution();
        employeePlanContribution.setEmployeeId("Employee" + employeeIndex);
        employeePlanContribution.setFirstName("First" + employeeIndex);
        employeePlanContribution.setLastName("Last" + employeeIndex);
        employeePlanContribution.setState("CA");
        employeePlanContribution.setCovgLevel(coverageLevel);
        employeePlanContribution.setGroupId(1111);
        employeePlanContribution.setGroupName("Grp1");
        return employeePlanContribution;
    }

    private EmployeeCostRes.PlanContribution populatePlanContributionData(BigDecimal eeCost, BigDecimal erCost) {
        EmployeeCostRes.PlanContribution planContributionData = new EmployeeCostRes.PlanContribution();
        planContributionData.setBenefitPlanId(PROSPECT_BENEFIT_PLAN_ID);
        planContributionData.setBenefitPlanName("BenPlan1");
        planContributionData.setEeCost(eeCost);
        planContributionData.setErCost(erCost);
        planContributionData.setTotalCost(eeCost.add(erCost));
        return planContributionData;
    }

    private static List<Object[]> prepareBasePlanResult() {
        List<Object[]> list = Arrays.asList(
                new Object[]{"dental", "006CSV", "MetLife Dental 100"},
                new Object[]{"medical", "001O8X", "UHC Core 500 CA"},
                new Object[]{"medical", "001O90", "UHC Core 2500 CA"},
                new Object[]{"vision", "002M7Z", "VSP Vision"},
                new Object[]{"vision", "002M81", "VSP Vision Plus"},
                new Object[]{"vision", "005KW5", "Aetna EyeMed"});
        return list;
    }

    private List<ProspectCensusResponse> prepareProspectCensusData() {
        List<ProspectCensusResponse> list = new ArrayList<>();
        list.add(ProspectCensusResponse.builder().employeeId("EE001").state("SC").zip("29708").build());
        list.add(ProspectCensusResponse.builder().employeeId("EE002").state("NJ").zip("07060").build());
        list.add(ProspectCensusResponse.builder().employeeId("EE003").state("FL").zip("34677").build());
        list.add(ProspectCensusResponse.builder().employeeId("EE004").state("NY").zip("10001").build());
        return list;
    }

    private List<String> prepareEmployeesByStrategyGroup() {
        List<String> list = new ArrayList<>();
        list.add("EE001");
        list.add("EE002");
        list.add("EE003");
        return list;
    }

    private CompletableFuture<List<PlanAvailableResponse>> preparePlanAvailableResponse() {
        List<PlanAvailableResponse> list = new ArrayList<>();
        list.add(PlanAvailableResponse.builder().postal("29708").plansByBenType(
                Arrays.asList(PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(Arrays.asList("REGIONAL_SC_PLAN")).build())).build());
        list.add(PlanAvailableResponse.builder().postal("07060").plansByBenType(
                Arrays.asList(PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(Arrays.asList("REGIONAL_NJ_PLAN")).build())).build());
        return CompletableFuture.completedFuture(list);
    }
    
	private List<HrisPlanResponse> hrisPlanResponse() {
		List<HrisPlanResponse> list = new ArrayList<>();

		list.add(HrisPlanResponse.builder().planId(11).carrierName("a").planName("y").build());
		list.add(HrisPlanResponse.builder().planId(12).carrierName("b").planName("x").build());
		list.add(HrisPlanResponse.builder().planId(13).carrierName("b").planName("y").build());
		list.add(HrisPlanResponse.builder().planId(14).carrierName("a").planName("x").build());
		return list;
	}

    private List<HrisPlanResponse> hrisPlanResponseWithBenefitType(String benefitType, List<String> zips) {
        List<HrisPlanResponse> list = new ArrayList<>();

        switch (benefitType) {
            case BSSApplicationConstants.MEDICAL:
                list.add(HrisPlanResponse.builder().planId(11).carrierName("a").planName("medical1")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                list.add(HrisPlanResponse.builder().planId(12).carrierName("b").planName("medical2")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                list.add(HrisPlanResponse.builder().planId(13).carrierName("b").planName("medical3")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                list.add(HrisPlanResponse.builder().planId(14).carrierName("a").planName("medical4")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                break;
            case BSSApplicationConstants.DENTAL:
                list.add(HrisPlanResponse.builder().planId(22).carrierName("c").planName("dental2")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                list.add(HrisPlanResponse.builder().planId(21).carrierName("c").planName("dental1")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                break;
            case BSSApplicationConstants.VISION:
                list.add(HrisPlanResponse.builder().planId(31).carrierName("e").planName("vision1")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                list.add(HrisPlanResponse.builder().planId(32).carrierName("d").planName("vision2")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                list.add(HrisPlanResponse.builder().planId(33).carrierName("d").planName("vision3")
                        .rateDetails(hrisPlanResponseRateDetails(zips)).build());
                break;
        }
        return list;
    }

    private HrisPlanResponse.RateDetails hrisPlanResponseRateDetails(List<String> zips) {
        return HrisPlanResponse.RateDetails.builder()
                .rateType("4tier")
                .ratesByZip(Arrays.asList(
                        HrisPlanResponse.RateDetails.RatesByZip.builder()
                                .zips(zips)
                                .rates(Arrays.asList(
                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                .tierCode("1")
                                                .rate(234.43)
                                                .build(),
                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                .tierCode("2")
                                                .rate(234.43)
                                                .build(),
                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                .tierCode("C")
                                                .rate(234.43)
                                                .build(),
                                        HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                .tierCode("4")
                                                .rate(234.43)
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }
	
    private List<HrisPlanResponse> hrisPlanResponse1() {
        List<HrisPlanResponse> list = new ArrayList<>();

        list.add(HrisPlanResponse.builder().planId(12).rateDetails(RateDetails.builder().ratesByZip(ratesByZip()).build()).build());
        list.add(HrisPlanResponse.builder().planId(11).rateDetails(RateDetails.builder().ratesByZip(ratesByZip()).build()).build());
        return list;
    }

    private List<RatesByZip> ratesByZip() {
        List<RatesByZip> list = new ArrayList<>();
        list.add(RatesByZip.builder().zips(List.of("29708", "07060")).build());

        return list;
    }

	private Company company() {
		BandCodes bandCodes = new BandCodes();
		bandCodes.setAetnaBandCode("12345");
		Company company = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(70);
		realmPlanYear.setPlanYearStart(new Date());
		company.setRealmPlanYear(realmPlanYear);
		company.setCode(COMPANY_CODE);
		company.setBandCodes(bandCodes);
		company.setRealm(realm());
		company.setBenefitStartDate("04-Mar-2025");
		return company;
	}

	private Realm realm() {
		Realm realm = new Realm();
		realm.setBenExchange("TriNet OMS");
		return realm;
	}

	private Map<String, EligiblePlanData> getBenefitPlansWithCarrier() {
		Map<String, EligiblePlanData> eligiblePlans = new HashMap<>();

		EligiblePlanData plan = new EligiblePlanData();

		plan.setPlanId("Aetna Plan 1");
		plan.setCarrier("Aetna");
		plan.setPlanCost(new BigDecimal(100.00));
		eligiblePlans.put("Aetna Plan 1", plan);

		plan = new EligiblePlanData();

		plan.setPlanId("Aetna Plan 2");
		plan.setCarrier("Aetna");
		plan.setPlanCost(new BigDecimal(100.00));
		eligiblePlans.put("Aetna Plan 2", plan);

		plan = new EligiblePlanData();

		plan.setPlanId("KA Plan 1");
		plan.setCarrier("Kaiser");
		plan.setPlanCost(new BigDecimal(100.00));
		eligiblePlans.put("KA Plan 1", plan);

		plan = new EligiblePlanData();

		plan.setPlanId("UHC Plan 1");
		plan.setCarrier("UHC HI");
		plan.setPlanCost(new BigDecimal(100.00));
		eligiblePlans.put("UHC Plan 1", plan);

		plan = new EligiblePlanData();

		plan.setPlanId("KA Plan 2");
		plan.setCarrier("Kaiser");
		plan.setPlanCost(new BigDecimal(100.00));
		eligiblePlans.put("KA Plan 2", plan);

		plan = new EligiblePlanData();

		plan.setPlanId("BS Plan 1");
		plan.setCarrier("Blue Shield of CA");
		plan.setPlanCost(new BigDecimal(100.00));
		eligiblePlans.put("BS Plan 1", plan);

		plan = new EligiblePlanData();

		plan.setPlanId("BS Plan 2");
		plan.setCarrier("Blue Shield of CA");
		plan.setPlanCost(new BigDecimal(100.00));
		eligiblePlans.put("BS Plan 2", plan);

		return eligiblePlans;
	}

	private CompletableFuture<List<PlanAvailableResponse>> preparePlanAvailableResponseForEligibilityPlans() {
		List<PlanAvailableResponse> list = new ArrayList<>();
		list.add(PlanAvailableResponse.builder().postal("29708").plansByBenType(Arrays.asList(
				PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(Arrays.asList("BS Plan 1")).build()))
				.build());
		list.add(PlanAvailableResponse.builder().postal("07060").plansByBenType(Arrays.asList(
				PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(Arrays.asList("UHC Plan 1")).build()))
				.build());
		list.add(PlanAvailableResponse.builder().postal("29308").plansByBenType(Arrays.asList(
				PlanAvailableResponse.BenTypePlan.builder().benType("10").planIds(Arrays.asList("BS Plan 2")).build()))
				.build());
		list.add(PlanAvailableResponse.builder().postal("03060")
				.plansByBenType(Arrays.asList(PlanAvailableResponse.BenTypePlan.builder().benType("10")
						.planIds(Arrays.asList("Aetna Plan 1")).build()))
				.build());
		return CompletableFuture.completedFuture(list);
	}

    private List<HrisPlanResponse> prepareHrisPlanResponse(String benefitType) {
        switch (benefitType) {
            case BSSApplicationConstants.MEDICAL:
                return List.of(
                        HrisPlanResponse.builder()
                                .planId(11)
                                .planName("HRIS Medical Plan 1")
                                .carrierId(1)
                                .carrierName("HRIS Carrier 1")
                                .build(),
                        HrisPlanResponse.builder()
                                .planId(12)
                                .planName("HRIS Medical Plan 2")
                                .carrierId(2)
                                .carrierName("HRIS Carrier 2")
                                .build()
                );
            case BSSApplicationConstants.VISION:
                return List.of(
                        HrisPlanResponse.builder()
                                .planId(13)
                                .planName("HRIS Vision Plan 1")
                                .carrierId(3)
                                .carrierName("HRIS Carrier 3")
                                .build()
                );
            case BSSApplicationConstants.DENTAL:
                return List.of(
                        HrisPlanResponse.builder()
                                .planId(14)
                                .planName("HRIS Dental Plan 1")
                                .carrierId(4)
                                .carrierName("HRIS Carrier 4")
                                .build(),
                        HrisPlanResponse.builder()
                                .planId(15)
                                .planName("HRIS Dental Plan 2")
                                .carrierId(5)
                                .carrierName("HRIS Carrier 5")
                                .build(),
                        HrisPlanResponse.builder()
                                .planId(16)
                                .planName("HRIS Dental Plan 3")
                                .carrierId(6)
                                .carrierName("HRIS Carrier 6")
                                .build()
                );
            default:
                return Collections.emptyList();
        }
    }

}
