package com.trinet.ambis.service.unit;

import static com.trinet.ambis.enums.OmsOfferingEnum.OM_OD_OV_TLD;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
//import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.model.*;
import com.trinet.ambis.service.model.planAvailability.HrisPlanResponse;
import com.trinet.ambis.service.*;
import com.trinet.ambis.service.dto.CarrierAssetDto;
import com.trinet.ambis.service.dto.CmsLogoDto;
import com.trinet.ambis.service.outputs.CarrierLogoService;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;
import com.trinet.ambis.util.FileUtils;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.outputs.AdditionalBenefitGroup;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparisonAdditonalBenefits;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.service.impl.outputs.PlanComparisonServiceImpl;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.outputs.PlanComparisionAsyncService;
import com.trinet.ambis.service.outputs.PlanPopulator;
import com.trinet.ambis.service.outputs.Populator;
import com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes.BenefitPlan;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import lombok.extern.log4j.Log4j2;

@RunWith(MockitoJUnitRunner.class)
@Log4j2
public class PlanComparisonServiceImplTest extends ServiceUnitTest {

    @InjectMocks
    PlanComparisonServiceImpl planComparisonServiceImpl;

    @Mock
    PlanComparisionAsyncService planComparisionAsyncService;

    @Mock
    EmployeePlanAssignmentService eePlanAssignmentService;

    @Mock
    ProspectPlanAvailabilityService prospectPlanAvailabilityService;

    @Mock
    PlanPopulator planPopulator;

    @Mock
    AdditionalBenefitPlanService additionalBenefitPlanService;

    @Mock
    BenefitGroupService benefitGroupService;

    @Mock
    HttpServletRequest httpRequest;

    @Mock
    private CarrierLogoService carrierLogoService;

    @Mock
    private FileUtils fileUtils;

    @Mock
    private BenefitsPlanViewService benefitsPlanViewService;

    @Mock
    private HrisPlanAttributeService hrisPlanAttributeService;
    
    @Mock
   	AppRulesConfigService appRulesConfigService;
       
    @Mock
   	PlanCompareService planCompareService;

    private static final String COMPANY_CODE = "22K0";
    private MockedStatic<CompanyServiceHelper> mockStaticCompanyServiceHelper;

    @Before
    public void setUp() {
        mockStaticCompanyServiceHelper = Mockito.mockStatic(CompanyServiceHelper.class);
        AppRulesAndConfigsUtils.setAppRuleConfigService(appRulesConfigService);
    }

    @After
    public void tearDown() {
        if (mockStaticCompanyServiceHelper != null) {
            mockStaticCompanyServiceHelper.close();
            mockStaticCompanyServiceHelper = null;
        }
    }

    @Test
    public void getPlanComparisonDataTest() throws InterruptedException, ExecutionException {
        //When
        when(planComparisionAsyncService.realmPlanYearAsync(company().getRealmPlanYear().getId())).thenReturn(realmPlanYear());
        when(planComparisionAsyncService.prospectEmployeePlansAsync(company().getCode())).thenReturn(prospectEmployeePlans());
        when(planComparisionAsyncService.getBenefitPlanRatesByAsync(any())).thenReturn(getBenefitPlanRatesBy());

        when(planPopulator.populateCurrentPlan()).thenReturn(populatePlan());
        when(planPopulator.populateCurrentPlanRate()).thenReturn(populatePlan());
        when(planPopulator.populateCurrentPlanHeadCount()).thenReturn(populatePlan());

        when(planPopulator.populateTrinetPlan()).thenReturn(populatePlan());
        when(planPopulator.populateTrinetPlanRate()).thenReturn(populatePlan());

        CompletableFuture<Map<String, BasePlanComparison>> comparisonDataFuture = planComparisonServiceImpl.getPlanComparisonData(company(), request(), httpRequest);

        //Assert
        Map<String, BasePlanComparison> comparisonData = comparisonDataFuture.get();

        assertNotNull(comparisonData);
        assertEquals(3, comparisonData.size());
        assertNotNull((PlanComparison)comparisonData.get("10"));
        assertNotNull((PlanComparison)comparisonData.get("11"));
        assertNotNull((PlanComparison)comparisonData.get("14"));
        assertNull((PlanComparisonAdditonalBenefits)comparisonData.get("23"));
        assertNull((PlanComparisonAdditonalBenefits)comparisonData.get("30"));

        verify(planComparisionAsyncService, times(1)).realmPlanYearAsync(company().getRealmPlanYear().getId());
        verify(planComparisionAsyncService, times(1)).prospectEmployeePlansAsync(any());
        verify(planComparisionAsyncService, times(1)).getBenefitPlanRatesByAsync(any());
        verify(additionalBenefitPlanService, times(0)).getAdditionalBenefitsCompareInformation(any(), anyLong(), anyList());
    }

    @Test
    public void getPlanComparisonDataTIBTest() throws InterruptedException, ExecutionException {
        //When
        when(planComparisionAsyncService.realmPlanYearAsync(companyTIB().getRealmPlanYear().getId())).thenReturn(realmPlanYear());
        when(planComparisionAsyncService.prospectEmployeePlansAsync(companyTIB().getCode())).thenReturn(prospectEmployeePlans());
        when(planComparisionAsyncService.getBenefitPlanRatesByAsync(any())).thenReturn(getBenefitPlanRatesBy());

        when(planPopulator.populateCurrentPlan()).thenReturn(populatePlan());
        when(planPopulator.populateCurrentPlanRate()).thenReturn(populatePlan());
        when(planPopulator.populateCurrentPlanHeadCount()).thenReturn(populatePlan());

        when(planPopulator.populateTrinetPlan()).thenReturn(populatePlan());
        when(planPopulator.populateTrinetPlanRate()).thenReturn(populatePlan());

        CompletableFuture<Map<String, BasePlanComparison>> comparisonDataFuture = planComparisonServiceImpl.getPlanComparisonData(companyTIB(), request(), httpRequest);

        //Assert
        Map<String, BasePlanComparison> comparisonData = comparisonDataFuture.get();

        assertNotNull(comparisonData);
        assertEquals(3, comparisonData.size());
        assertNotNull((PlanComparison)comparisonData.get("10"));
        assertNotNull((PlanComparison)comparisonData.get("11"));
        assertNotNull((PlanComparison)comparisonData.get("14"));
        assertNull((PlanComparisonAdditonalBenefits)comparisonData.get("23"));
        assertNull((PlanComparisonAdditonalBenefits)comparisonData.get("30"));

        verify(planComparisionAsyncService, times(1)).realmPlanYearAsync(companyTIB().getRealmPlanYear().getId());
        verify(planComparisionAsyncService, times(1)).prospectEmployeePlansAsync(any());
        verify(planComparisionAsyncService, times(1)).getBenefitPlanRatesByAsync(any());
        verify(additionalBenefitPlanService, times(0)).getAdditionalBenefitsCompareInformation(any(), anyLong(), anyList());
    }

    @Test
    public void getOMSPlanRatesByPlanTest() {
        // Mock data
        Company company = company();
        Map<String, Set<String>> planIdsByBenType =
                Map.of(
                        BSSApplicationConstants.MEDICAL_PLAN_TYPE, Set.of("101"),
                        BSSApplicationConstants.DENTAL_PLAN_TYPE, Set.of("102")
                );
        // Mock behavior
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(
                company, BSSApplicationConstants.MEDICAL
        )).thenReturn(
                getOMSPlanRates(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
        );
        when(prospectPlanAvailabilityService.getProspectEmployeeHrisPlanAvailability(
                company, BSSApplicationConstants.DENTAL
        )).thenReturn(
                getOMSPlanRates(BSSApplicationConstants.DENTAL_PLAN_TYPE)
        );

        // Call the method
        Map<String, RateDetail> result = planComparisonServiceImpl.getOMSPlanRatesByPlan(company, planIdsByBenType);

        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("101"));
        assertTrue(result.containsKey("102"));
        assertEquals(RateTypeEnum.AGE_BANDED.getCode(), result.get("101").getRateType());
        assertEquals(1, result.get("101").getTierRates().size());
        assertEquals("1", result.get("101").getTierRates().get(0).getCvgTierCode());
        assertEquals(BigDecimal.valueOf(120.0), result.get("101").getTierRates().get(0).getCost());
        assertEquals(RateTypeEnum.FOUR_TIER.getCode(), result.get("102").getRateType());
        assertEquals(1, result.get("102").getTierRates().size());
        assertEquals(CoverageCodesEnums.COV_EMPLOYEE.getId(), result.get("102").getTierRates().get(0).getCvgTierCode());
        assertEquals(BigDecimal.valueOf(220.0), result.get("102").getTierRates().get(0).getCost());
    }

    public List<HrisPlanResponse> getOMSPlanRates(String benefitType) {
        List<HrisPlanResponse> omsPlanRates;


        if (Objects.equals(benefitType, BSSApplicationConstants.MEDICAL_PLAN_TYPE)){
            omsPlanRates = List.of(
                    HrisPlanResponse.builder()
                            .planId(101)
                            .rateDetails(HrisPlanResponse.RateDetails.builder()
                                    .rateType(RateTypeEnum.AGE_BANDED.getCode())
                                    .ratesByZip(List.of(
                                            HrisPlanResponse.RateDetails.RatesByZip.builder()
                                                    .zips(List.of("11111", "22222"))
                                                    .rates(List.of(
                                                            HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                                    .tierCode("1")
                                                                    .rate(120.0)
                                                                    .build()
                                                    ))
                                                    .build()
                                    ))
                                    .build()
                            )
                            .build()
            );
        } else if (Objects.equals(benefitType, BSSApplicationConstants.DENTAL_PLAN_TYPE)){
            omsPlanRates = List.of(
                    HrisPlanResponse.builder()
                            .planId(102)
                            .rateDetails(HrisPlanResponse.RateDetails.builder()
                                    .rateType("4Tier")
                                    .ratesByZip(List.of(
                                            HrisPlanResponse.RateDetails.RatesByZip.builder()
                                                    .zips(List.of("33333", "44444"))
                                                    .rates(List.of(
                                                            HrisPlanResponse.RateDetails.RatesByZip.Rate.builder()
                                                                    .tierCode("1")
                                                                    .rate(220.0)
                                                                    .build()
                                                    ))
                                                    .build()
                                    ))
                                    .build()
                            )
                            .build()
            );
        } else {
            omsPlanRates = new ArrayList<>();
        }
        return omsPlanRates;
    }

    public List<EePlanAssignment> getEEPlanAssignment() {
        return List.of(
                EePlanAssignment.builder()
                        .eePlanAssignmentPK(EePlanAssignmentPK.builder()
                                .strategyId(101L)
                                .emplId("EMPL001")
                                .benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE)
                                .build())
                        .benefitPlan("101")
                        .portfolioId(2001L)
                        .covrgCD("COVRG_A")
                        .eeRate(BigDecimal.valueOf(120.0))
                        .erRate(BigDecimal.valueOf(240.0))
                        .build(),
                EePlanAssignment.builder()
                        .eePlanAssignmentPK(EePlanAssignmentPK.builder()
                                .strategyId(101L)
                                .emplId("EMPL002")
                                .benefitType(BSSApplicationConstants.DENTAL_PLAN_TYPE)
                                .build())
                        .benefitPlan("102")
                        .portfolioId(2002L)
                        .covrgCD("COVRG_B")
                        .eeRate(BigDecimal.valueOf(180.0))
                        .erRate(BigDecimal.valueOf(360.0))
                        .build()
        );
    }

    public OutputRequest request() {
        OutputRequest request = new OutputRequest();
        request.setBenefitTypes(List.of("10","11","14", "23", "30"));
        request.setPlanAppendixFilters(new PlanAppendixFilters());
        request.getPlanAppendixFilters().setRegions(List.of("SA","CA"));
        request.setTemplateNames(List.of("PCC","ECC","APX"));
        request.setTnStrategyId("301023");
        return request;
    }

    public Company company() {
        Company company = new Company();
        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setId(70);
        Realm realm = new Realm();
        realm.setBenExchange(BenExchngEnums.TRINET_III.getBenExchng());
        company.setAuthBroker(BSSApplicationConstants.DUMMY);
        company.setRealm(realm);
        company.setRealmPlanYear(realmPlanYear);
        company.setCode(COMPANY_CODE);
        return company;
    }

    public Company companyTIB() {
        Company company = new Company();
        RealmPlanYear realmPlanYear = new RealmPlanYear();
        realmPlanYear.setId(70);
        company.setRealmPlanYear(realmPlanYear);
        company.setCode(COMPANY_CODE);
        company.setOmsOffering(OM_OD_OV_TLD.name());
        Realm realm = new Realm();
        realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
        company.setRealm(realm);
        return company;
    }

    public CompletableFuture<Map<String, XbssRealmPlyrPlan>> realmPlanYear(){
        Map<String, XbssRealmPlyrPlan> realmPlanyearPlans = new HashMap<>();
        return CompletableFuture.completedFuture(realmPlanyearPlans);
    }

    public CompletableFuture<Optional<List<EmployeePlansRes>>> prospectEmployeePlans(){
        List<EmployeePlansRes> employeePlansRes = new ArrayList<>();
        return CompletableFuture.completedFuture(Optional.of(employeePlansRes));
    }

    public CompletableFuture<Map<String, List<BenefitPlanRate>>> getBenefitPlanRatesBy(){
        Map<String, List<BenefitPlanRate>> employeePlansRes = new HashMap<>();
        return CompletableFuture.completedFuture(employeePlansRes);
    }

    public Populator populatePlan() {
        return plan ->{
            plan.setProspectEmployees(prospectEmployees());
            plan.setRequestBenefitTypes(List.of("10","11","14","23","30"));
            plan.setStrategyId("301023");
            plan.setPlyrPlanMap(plyrPlanMap());
            plan.setRealmPlyrPlans(realmPlyrPlans());
            plan.setCurrentPlanIds(currentPlanIds());
            plan.setTrinetPlanIds(trinetPlanIds());
            plan.setBenefitTypeCurrentPlansAttributes(benefitTypeCurrentPlansAttributes());
            plan.setBenefitTypeTrinetPlansAttributes(benefitTypeTrinetPlansAttributes());
            plan.setCurrentPlanHeadCounts(currentPlanHeadCounts());
            plan.setTrinetPlanHeadCounts(trinetPlanHeadCounts());
            plan.setCurrentPlanRates(currentPlanRates());
            plan.setTrinetPlanRates(trinetPlanRates());
            plan.setTrinetPlanRatesByCompany(trinetPlanRatesByCompany());
            plan.setCurrentTrinetPlansMapping(currentTrinetPlansMapping());
            plan.setPlanComparison(planComparison());
        };
    }

    @Test
    public void getAdditionalPlanComparisonDataTest() throws InterruptedException, ExecutionException {
        List<PlanComparisonAdditonalBenefits> adCompareDetails = adCompareDetails();
        //When
        when(additionalBenefitPlanService.getAdditionalBenefitsCompareInformation(any(), anyLong(), anyList())).thenReturn(adCompareDetails);

        String groupName = adCompareDetails.get(1).getSelectedGroupDetails().stream().map(AdditionalBenefitGroup::getGroupName).collect(Collectors.toList()).get(2);

        CompletableFuture<Map<String, BasePlanComparison>> comparisonDataFuture = planComparisonServiceImpl.getAdditionalBenfitsCompareData(company(), addditionalPlansRequest());

        //Assert
        Map<String, BasePlanComparison> comparisonData = comparisonDataFuture.get();
        assertNotNull(comparisonData);
        assertEquals("TEST-SA", groupName);
        assertNotEquals("TEST-SA", ((PlanComparisonAdditonalBenefits)comparisonData.get("23")).getSelectedGroupDetails().get(0).getGroupName());
        assertNotEquals("TEST-SA", ((PlanComparisonAdditonalBenefits)comparisonData.get("23")).getSelectedGroupDetails().get(1).getGroupName());

        verify(additionalBenefitPlanService, times(1)).getAdditionalBenefitsCompareInformation(any(), anyLong(), anyList());
        verify(planComparisionAsyncService, times(0)).realmPlanYearAsync(company().getRealmPlanYear().getId());
        verify(planComparisionAsyncService, times(0)).prospectEmployeePlansAsync(any());
        verify(planComparisionAsyncService, times(0)).getBenefitPlanRatesByAsync(any());
    }

    private OutputRequest addditionalPlansRequest() {
        OutputRequest request = new OutputRequest();
        request.setBenefitTypes(List.of("23", "30"));
        request.setPlanAppendixFilters(new PlanAppendixFilters());
        request.getPlanAppendixFilters().setRegions(List.of("SA","CA"));
        request.setTemplateNames(List.of("PCC","ECC","APX"));
        request.setTnStrategyId("301023");
        return request;
    }

    @Test
    public void getPlanComparisonDataForMedicalTest() throws InterruptedException, ExecutionException {
        //When
        when(planComparisionAsyncService.realmPlanYearAsync(company().getRealmPlanYear().getId())).thenReturn(realmPlanYear());
        when(planComparisionAsyncService.prospectEmployeePlansAsync(company().getCode())).thenReturn(prospectEmployeePlans());
        when(planComparisionAsyncService.getBenefitPlanRatesByAsync(any())).thenReturn(getBenefitPlanRatesBy());

        when(planPopulator.populateCurrentPlan()).thenReturn(populatePlan());
        when(planPopulator.populateCurrentPlanRate()).thenReturn(populatePlan());
        when(planPopulator.populateCurrentPlanHeadCount()).thenReturn(populatePlan());

        when(planPopulator.populateTrinetPlan()).thenReturn(populatePlan());
        when(planPopulator.populateTrinetPlanRate()).thenReturn(populatePlan());

        CompletableFuture<Map<String, BasePlanComparison>> comparisonDataFuture = planComparisonServiceImpl.getPlanComparisonData(company(), medicalAndAdditionalPlansRequest(), httpRequest);

        //Assert
        Map<String, BasePlanComparison> comparisonData = comparisonDataFuture.get();
        assertNotNull(comparisonData);
        assertEquals(3, comparisonData.size());
        assertNotNull((PlanComparison)comparisonData.get("10"));

        verify(planComparisionAsyncService, times(1)).realmPlanYearAsync(company().getRealmPlanYear().getId());
        verify(planComparisionAsyncService, times(1)).prospectEmployeePlansAsync(any());
        verify(planComparisionAsyncService, times(1)).getBenefitPlanRatesByAsync(any());
    }

    private OutputRequest medicalAndAdditionalPlansRequest() {
        OutputRequest request = new OutputRequest();
        request.setBenefitTypes(List.of("10", "23","30"));
        request.setPlanAppendixFilters(new PlanAppendixFilters());
        request.getPlanAppendixFilters().setRegions(List.of("SA","CA"));
        request.setTemplateNames(List.of("PCC","ECC","APX"));
        request.setTnStrategyId("301023");
        return request;
    }

    @Test
    public void getPlanComparisonAssignementDataTest() throws InterruptedException, ExecutionException {

        String prospectPlanId = "1";
        String trinetPlanId = "2";
        String benefitType = "10";
        CompletableFuture<List<BenefitPlanCompare>> benefitPlanCompares =  CompletableFuture.completedFuture(benefitPlanCompareDetails());
        Company company = company();
        //When
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);
        when(planCompareService.getPlanAttributes(any(), any(),any(), any())).thenReturn(benefitPlanCompares);

        PlanComparison comparisonData = (PlanComparison)planComparisonServiceImpl.getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpRequest,company);

        //Assert
        assertNotNull(comparisonData);
        assertNotNull(comparisonData.getComparisons().get(0).getCurrentPlan());
        assertEquals("1", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanId());
        assertEquals("FL Blue PPO 2000 NTL", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanName());
        assertEquals("Florida Blue", comparisonData.getComparisons().get(0).getCurrentPlan().getCarrierName());
        assertEquals("PPO", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanType());
        assertEquals("$2,000", comparisonData.getComparisons().get(0).getCurrentPlan().getAttributeValues().get(0).getChildren().get(0).getValue());

        assertNotNull(comparisonData.getComparisons().get(0).getTriNetPlans());
        assertEquals("2", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
        assertEquals("UHC Primary 1000 Dallas", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanName());
        assertEquals("UnitedHealthcare", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getCarrierName());
        assertEquals("UHC", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanType());
        assertEquals("$1,000 / $4,000", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getAttributeValues().get(0).getChildren().get(0).getValue());


    }

    @Test
    public void getPlanComparisonAssignementDataNoPlanIdsTest() throws InterruptedException, ExecutionException {
        String prospectPlanId = "0";
        String trinetPlanId = "0";
        String benefitType = "10";
        Company company = company();
        //When
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(false);

        PlanComparison comparisonData = (PlanComparison)planComparisonServiceImpl.getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpRequest,company);

        //Assert
        assertNotNull(comparisonData);
        assertEquals(1, comparisonData.getComparisons().size());
        assertNotNull(comparisonData.getComparisons().get(0).getCurrentPlan());
        assertEquals("0", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanId());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getPlanName());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getCarrierName());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getPlanType());
        assertEquals(0, comparisonData.getComparisons().get(0).getCurrentPlan().getAttributeValues().size());

    }

    @Test
    public void getPlanComparisonAssignementDataForTIBTest() throws InterruptedException, ExecutionException {

        String prospectPlanId = "1";
        String trinetPlanId = "2";
        String benefitType = "10";
        List<BenefitPlanCompare> benefitPlanCompares = CompletableFuture.completedFuture(benefitPlanCompareDetails())
                .get();
        CompletableFuture<List<BenefitPlanCompare>> prospectPlanBenefitPlanCompare = CompletableFuture
                .completedFuture(List.of(benefitPlanCompares.get(0)));
        CompletableFuture<List<BenefitPlanCompare>> trinetPlanBenefitPlanCompare = CompletableFuture
                .completedFuture(List.of(benefitPlanCompares.get(1)));
        Company company = companyTIB();
        // When
        when(planCompareService.getPlanAttributes(anySet(), any(), any(), any())).thenReturn(prospectPlanBenefitPlanCompare);
        when(hrisPlanAttributeService.getPlanAttributesByBenefitType(anySet(), any())).thenReturn(trinetPlanBenefitPlanCompare);
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);

        PlanComparison comparisonData = (PlanComparison) planComparisonServiceImpl
                .getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpRequest, company);

        // Assert
        assertNotNull(comparisonData);
        assertNotNull(comparisonData.getComparisons().get(0).getCurrentPlan());
        assertEquals("1", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanId());
        assertEquals("FL Blue PPO 2000 NTL", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanName());
        assertEquals("Florida Blue", comparisonData.getComparisons().get(0).getCurrentPlan().getCarrierName());
        assertEquals("PPO", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanType());
        assertEquals("$2,000", comparisonData.getComparisons().get(0).getCurrentPlan().getAttributeValues().get(0)
                .getChildren().get(0).getValue());

        assertNotNull(comparisonData.getComparisons().get(0).getTriNetPlans());
        assertEquals("2", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
        assertEquals("UHC Primary 1000 Dallas",
                comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanName());
        assertEquals("UnitedHealthcare",
                comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getCarrierName());
        assertEquals("UHC", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanType());
        assertEquals("$1,000 / $4,000", comparisonData.getComparisons().get(0).getTriNetPlans().get(0)
                .getAttributeValues().get(0).getChildren().get(0).getValue());

    }

    @Test
    public void getPlanComparisonAssignementDataForTIBNoProspectPlanIdTest() throws InterruptedException, ExecutionException {
        String prospectPlanId = "0";
        String trinetPlanId = "2";
        String benefitType = "10";
        List<BenefitPlanCompare> benefitPlanCompares = CompletableFuture.completedFuture(benefitPlanCompareDetails()).get();
        CompletableFuture<List<BenefitPlanCompare>> trinetPlanBenefitPlanCompare = CompletableFuture.completedFuture(List.of(benefitPlanCompares.get(1)));
        Company company = companyTIB();
        // When
        when(hrisPlanAttributeService.getPlanAttributesByBenefitType(anySet(), any())).thenReturn(trinetPlanBenefitPlanCompare);
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);


        PlanComparison comparisonData = (PlanComparison)planComparisonServiceImpl.getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpRequest,company);

        //Assert
        assertNotNull(comparisonData);
        assertNotNull(comparisonData.getComparisons().get(0).getCurrentPlan());
        assertEquals("0", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanId());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getPlanName());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getCarrierName());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getPlanType());
        assertEquals(0, comparisonData.getComparisons().get(0).getCurrentPlan().getAttributeValues().size());

        assertNotNull(comparisonData.getComparisons().get(0).getTriNetPlans());
        assertEquals("2", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
        assertEquals("UHC Primary 1000 Dallas", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanName());
        assertEquals("UnitedHealthcare", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getCarrierName());
        assertEquals("UHC", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanType());
        assertEquals("$1,000 / $4,000", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getAttributeValues().get(0).getChildren().get(0).getValue());

        // Verify
        verify(benefitsPlanViewService, times(0)).getBenefitPlanAttributes(any(), any(),any(), any());

    }

    @Test
    public void getPlanComparisonAssignementDataForTIBNoProspectPlanIdOrTriNetPlanTest() throws InterruptedException, ExecutionException {
        String prospectPlanId = "0";
        String trinetPlanId = "null";
        String benefitType = "10";
        List<BenefitPlanCompare> benefitPlanCompares = CompletableFuture.completedFuture(benefitPlanCompareDetails()).get();
        CompletableFuture<List<BenefitPlanCompare>> trinetPlanBenefitPlanCompare = CompletableFuture.completedFuture(List.of(benefitPlanCompares.get(1)));
        Company company = companyTIB();
        // When

        PlanComparison comparisonData = (PlanComparison)planComparisonServiceImpl.getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpRequest,company);

        //Assert
        assertNotNull(comparisonData);
        assertNotNull(comparisonData.getComparisons().get(0).getCurrentPlan());
        assertEquals("0", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanId());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getPlanName());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getCarrierName());
        assertNull(comparisonData.getComparisons().get(0).getCurrentPlan().getPlanType());
        assertEquals(0, comparisonData.getComparisons().get(0).getCurrentPlan().getAttributeValues().size());

        assertNotNull(comparisonData.getComparisons().get(0).getTriNetPlans());
        assertEquals("null", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
        assertNull(comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanName());
        assertNull(comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getCarrierName());
        assertNull(comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanType());
        assertEquals(0, comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getAttributeValues().size());

        // Verify
        verify(benefitsPlanViewService, times(0)).getBenefitPlanAttributes(any(), any(),any(), any());

    }

    @Test
    public void getPlanComparisonAssignementDataForDentalTIBTest() throws InterruptedException, ExecutionException {

        String prospectPlanId = "1";
        String trinetPlanId = "2";
        String benefitType = "11";
        List<BenefitPlanCompare> benefitPlanCompares = CompletableFuture.completedFuture(benefitPlanCompareDetails()).get();
        CompletableFuture<List<BenefitPlanCompare>> prospectPlanBenefitPlanCompare = CompletableFuture.completedFuture(List.of(benefitPlanCompares.get(0)));
        CompletableFuture<List<BenefitPlanCompare>> trinetPlanBenefitPlanCompare = CompletableFuture.completedFuture(List.of(benefitPlanCompares.get(1)));
        Company company = companyTIB();

        // When
        when(planCompareService.getPlanAttributes(anySet(), any(), any(), any())).thenReturn(prospectPlanBenefitPlanCompare);
        when(hrisPlanAttributeService.getPlanAttributesByBenefitType(anySet(), any())).thenReturn(trinetPlanBenefitPlanCompare);
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);

        PlanComparison comparisonData = (PlanComparison)planComparisonServiceImpl.getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpRequest,company);

        //Assert
        assertNotNull(comparisonData);
        assertNotNull(comparisonData.getComparisons().get(0).getCurrentPlan());
        assertEquals("1", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanId());
        assertEquals("FL Blue PPO 2000 NTL", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanName());
        assertEquals("Florida Blue", comparisonData.getComparisons().get(0).getCurrentPlan().getCarrierName());
        assertEquals("PPO", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanType());
        assertEquals("$2,000", comparisonData.getComparisons().get(0).getCurrentPlan().getAttributeValues().get(0).getChildren().get(0).getValue());

        assertNotNull(comparisonData.getComparisons().get(0).getTriNetPlans());
        assertEquals("2", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
        assertEquals("UHC Primary 1000 Dallas", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanName());
        assertEquals("UnitedHealthcare", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getCarrierName());
        assertEquals("UHC", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanType());
        assertEquals("$1,000 / $4,000", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getAttributeValues().get(0).getChildren().get(0).getValue());

    }

    @Test
    public void getPlanComparisonAssignementDataForVisionTIBTest() throws InterruptedException, ExecutionException {

        String prospectPlanId = "1";
        String trinetPlanId = "2";
        String benefitType = "14";
        List<BenefitPlanCompare> benefitPlanCompares = CompletableFuture.completedFuture(benefitPlanCompareDetails()).get();
        CompletableFuture<List<BenefitPlanCompare>> prospectPlanBenefitPlanCompare = CompletableFuture.completedFuture(List.of(benefitPlanCompares.get(0)));
        CompletableFuture<List<BenefitPlanCompare>> trinetPlanBenefitPlanCompare = CompletableFuture.completedFuture(List.of(benefitPlanCompares.get(1)));
        Company company = companyTIB();
        // When
        when(planCompareService.getPlanAttributes	(anySet(), any(), any(), any())).thenReturn(prospectPlanBenefitPlanCompare);
        when(hrisPlanAttributeService.getPlanAttributesByBenefitType(anySet(), any())).thenReturn(trinetPlanBenefitPlanCompare);
        when(CompanyServiceHelper.isTibProspect(company)).thenReturn(true);

        PlanComparison comparisonData = (PlanComparison)planComparisonServiceImpl.getPlanCompareAssignmentDetails(prospectPlanId, trinetPlanId, benefitType, httpRequest,company);

        //Assert
        assertNotNull(comparisonData);
        assertNotNull(comparisonData.getComparisons().get(0).getCurrentPlan());
        assertEquals("1", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanId());
        assertEquals("FL Blue PPO 2000 NTL", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanName());
        assertEquals("Florida Blue", comparisonData.getComparisons().get(0).getCurrentPlan().getCarrierName());
        assertEquals("PPO", comparisonData.getComparisons().get(0).getCurrentPlan().getPlanType());
        assertEquals("$2,000", comparisonData.getComparisons().get(0).getCurrentPlan().getAttributeValues().get(0).getChildren().get(0).getValue());

        assertNotNull(comparisonData.getComparisons().get(0).getTriNetPlans());
        assertEquals("2", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanId());
        assertEquals("UHC Primary 1000 Dallas", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanName());
        assertEquals("UnitedHealthcare", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getCarrierName());
        assertEquals("UHC", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getPlanType());
        assertEquals("$1,000 / $4,000", comparisonData.getComparisons().get(0).getTriNetPlans().get(0).getAttributeValues().get(0).getChildren().get(0).getValue());

    }

    private Optional<List<EmployeePlansRes>> prospectEmployees(){
        List<EmployeePlansRes> employees = new ArrayList<>();
        EmployeePlansRes empl = new EmployeePlansRes();
        empl.setEmployeeId("00010590832");
        List<BenefitPlan> benefitPlans = new ArrayList<>();
        BenefitPlan benefitPlan = new BenefitPlan();
        benefitPlan.setBenefitPlanId("0024GI");
        benefitPlan.setBenefitTypeCode("10");
        benefitPlan.setCoverageCode("1");
        empl.setBenefitPlans(benefitPlans);
        employees.add(empl);
        return Optional.of(employees);
    }

    private Map<String, XbssRealmPlyrPlan> plyrPlanMap(){
        TypeReference<Map<String, XbssRealmPlyrPlan>> type = new TypeReference<Map<String, XbssRealmPlyrPlan>>(){};
        return readPlanComparisonRequest("/planComparison/plyrPlanMap.json", type).get();
    }

    private Map<String,Set<XbssRealmPlyrPlan>> realmPlyrPlans(){
        TypeReference<Map<String,Set<XbssRealmPlyrPlan>>> type = new TypeReference<Map<String,Set<XbssRealmPlyrPlan>>>(){};
        return readPlanComparisonRequest("/planComparison/realmPlyrPlans.json", type).get();
    }

    private Map<String,List<BenefitPlanCompare>> benefitTypeCurrentPlansAttributes(){
        TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<Map<String,List<BenefitPlanCompare>>>(){};
        Map<String,List<BenefitPlanCompare>> map = (Map<String,List<BenefitPlanCompare>>)readPlanComparisonRequest("/planComparison/currentPlansAttributes.json", type).get();
        return map;
    }

    private Map<String,List<BenefitPlanCompare>> benefitTypeTrinetPlansAttributes(){
        TypeReference<Map<String,List<BenefitPlanCompare>>> type = new TypeReference<Map<String,List<BenefitPlanCompare>>>(){};
        return readPlanComparisonRequest("/planComparison/trinetPlansAttributes.json", type).get();
    }

    private List<EmployeeHeadCountRes> currentPlanHeadCounts(){
        TypeReference<List<EmployeeHeadCountRes>> type = new TypeReference<List<EmployeeHeadCountRes>>(){};
        return readPlanComparisonRequest("/planComparison/currentPlanHeadCounts.json", type).get();
    }

    private List<EmployeeHeadCountRes> trinetPlanHeadCounts(){
        TypeReference<List<EmployeeHeadCountRes>> type = new TypeReference<List<EmployeeHeadCountRes>>(){};
        return readPlanComparisonRequest("/planComparison/trinetPlanHeadCounts.json", type).get();
    }

    private Map<String, RateDetail> currentPlanRates(){
        TypeReference<Map<String, RateDetail>> type = new TypeReference<Map<String, RateDetail>>(){};
        return readPlanComparisonRequest("/planComparison/currentPlanRates.json", type).get();
    }

    private Map<String, RateDetail> trinetPlanRates(){
        TypeReference<Map<String, RateDetail>> type = new TypeReference<Map<String, RateDetail>>(){};
        return readPlanComparisonRequest("/planComparison/trinetPlanRates.json", type).get();
    }

    private Map<String, List<BenefitPlanRate>> trinetPlanRatesByCompany(){
        TypeReference<Map<String, List<BenefitPlanRate>>> type = new TypeReference<Map<String, List<BenefitPlanRate>>>(){};
        return readPlanComparisonRequest("/planComparison/trinetPlanRatesByCompany.json", type).get();
    }

    private Map<String,Map<String,List<String>>> currentTrinetPlansMapping(){
        TypeReference<Map<String,Map<String,List<String>>>> type = new TypeReference<Map<String,Map<String,List<String>>>>(){};
        return readPlanComparisonRequest("/planComparison/currentTrinetPlansMapping.json", type).get();
    }

    private List<String> currentPlanIds(){
        TypeReference<List<String>> type = new TypeReference<List<String>>(){};
        return readPlanComparisonRequest("/planComparison/currentPlanIds.json", type).get();
    }

    private List<String> trinetPlanIds(){
        TypeReference<List<String>> type = new TypeReference<List<String>>(){};
        return readPlanComparisonRequest("/planComparison/trinetPlanIds.json", type).get();
    }

    private Map<String,BasePlanComparison> planComparison(){
        return new HashMap<>();
    }

    private List<PlanComparisonAdditonalBenefits> adCompareDetails(){
        TypeReference<List<PlanComparisonAdditonalBenefits>> type = new TypeReference<List<PlanComparisonAdditonalBenefits>>(){};
        return readPlanComparisonRequest("/planComparison/additionalBenefits.json", type).get();
    }

    private List<BenefitPlanCompare> benefitPlanCompareDetails(){
        TypeReference<List<BenefitPlanCompare>> type = new TypeReference<List<BenefitPlanCompare>>(){};
        return readPlanComparisonRequest("/planComparison/benefitPlanCompare.json", type).get();
    }

    private static <T> Supplier<T> readPlanComparisonRequest(String filePath, TypeReference<T> valueType) {
        return () -> {
            ClassPathResource staticDataResource = new ClassPathResource(filePath);
            try {
                String dto = IOUtils.toString(staticDataResource.getInputStream(), StandardCharsets.UTF_8);
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                mapper.setVisibility(
                        VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                );
                return mapper.readValue(dto, valueType);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read or parse file: " + filePath, e);
            }
        };
    }

    private CmsLogoDto getCmsLogDto(){
        CmsLogoDto logDto = new CmsLogoDto();
        CmsLogoDto.LogoDto logo = new CmsLogoDto.LogoDto();
        List<String> url = new ArrayList<>();
        url.add("aethena");
        List< CmsLogoDto.LogoDto > assets = new ArrayList<>();
        assets.add(logo);
        logDto.setAssets(assets);
        CmsLogoDto.MetaData metaData = new CmsLogoDto.MetaData();
        CmsLogoDto.Extensions extensions = new CmsLogoDto.Extensions();
        CmsLogoDto.CarrierDetails carrierDetails = new CmsLogoDto.CarrierDetails();
        List<String> carrierNames = new ArrayList<>();
        carrierNames.add("aethena");
        carrierNames.add("Aetna");
        carrierNames.add("Florida Blue");
        carrierNames.add("UnitedHealthcare");
        carrierDetails.setCarrierNames(carrierNames);
        List<CmsLogoDto.CarrierDetails> carrierDetailsList = new ArrayList<>();
        extensions.setCarrierDetailsList(carrierDetailsList);
        metaData.setExtensions(extensions);
        logo.setMetadata(metaData);
        logo.setUid("123345");
        //logo.setUrl("https://images.contentstack.io/v3/assets/bltab010fdefd6ceb60/blt3dead4dab1e331e6/672108363a7f64d7693ebbc3/Aetna.png");
        logo.setUrl("123345");
        return logDto;
    }

    public Supplier<CarrierAssetDto> getCarrierLogoAsset(){
        return () -> {
            CarrierAssetDto carrierAssetDto = new CarrierAssetDto();
            CarrierAssetDto.AssestDetails details = new CarrierAssetDto.AssestDetails();
            details.setUid("123345");
            details.setCarrierNames(List.of("Aetna", "MetLife" ,"Florida Blue", "UnitedHealthcare"));
            List<CarrierAssetDto.AssestDetails> assestDetails = new ArrayList<>();
            assestDetails.add(details);
            carrierAssetDto.setAssestDetails(assestDetails);
            return carrierAssetDto;
        };
    }
}

