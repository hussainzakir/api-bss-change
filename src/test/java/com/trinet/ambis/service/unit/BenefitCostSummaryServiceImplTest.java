package com.trinet.ambis.service.unit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeEmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeTotal;
import com.trinet.ambis.rest.controllers.dto.outputs.EmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.EmployeeDetails;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse.BenefitType;
import com.trinet.ambis.rest.controllers.dto.prospect.BenefitsDetailsResponse.FundingDetails;
import com.trinet.ambis.service.impl.outputs.BenefitCostSummaryServiceImpl;
import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.prospect.ProspectPlanService;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;
import com.trinet.ambis.util.ProspectServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class BenefitCostSummaryServiceImplTest  extends ServiceUnitTest{

    @InjectMocks
    BenefitCostSummaryServiceImpl benefitCostSummaryService;

    @Mock
    StrategyDataDao strategyDataDao;

    @Mock
    ProspectEmployeeCostService prospectEmployeeCostService;

    @Mock
    ProspectPlanService prospectPlanService;

    @Mock
	ProspectServiceRestClient prospectServiceRestClient;
    private MockedStatic<BSSMessageConfig> bssMessageConfigMock;

    @Before
    public void setUp() throws Exception {
        bssMessageConfigMock = org.mockito.Mockito.mockStatic(BSSMessageConfig.class);
        bssMessageConfigMock.when(() -> BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_API_URI))
                .thenReturn("http://localhost:8087/api-wf-hw-bss-prospect/v1");
        bssMessageConfigMock.when(() -> BSSMessageConfig.getProperty(ProspectURIConstants.PROSPECT_STRATEGY_URI))
                .thenReturn("/benefits-details/{benefitTypes}");
    }

    @After
    public void tearDown() {
        bssMessageConfigMock.close();
    }
    @Test
    public void getBenefitGroupCostSummaryData() throws InterruptedException, ExecutionException {
        OutputRequest request = new OutputRequest();
        request.setTnStrategyId("45673");
        List<String> benTypes =Arrays.asList(new String("med"),new String("den"),new String("vis"),new String("lif"),new String("dis"));
        request.setBenefitTypes(benTypes);
        List<String> requestedBenfitPlanCodes = Arrays.asList("10","11","14","23","30"); 
		List<BenefitsDetailsResponse> response = prepareBenefitsDetailsRes();
		ArgumentCaptor<ProspectApiRequest> apiRequestArgCaptor = ArgumentCaptor
				.forClass(ProspectApiRequest.class);
		when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiRequestArgCaptor.capture()))
				.thenReturn(response);
		
		
        BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary = createBenefitTypeEmployeeCostSummary();
        when(strategyDataDao.getBenefitCostSummary(anyString())).thenReturn(getAddData());
        when(strategyDataDao.getStrategyHsaFunding(anyList())).thenReturn(getHsaData());
        when(strategyDataDao.getStrategyFundingByStrategy(anyList())).thenReturn(getWaiverData());
        when(prospectPlanService.getBenefitPlansBy("1234")).thenReturn(getBenefitPlanData());

        CompletableFuture<BenefitCostSummary> benefitCostSummaryFuture = benefitCostSummaryService.getBenefitCostSummaryData(benefitTypeEmployeeCostSummary, "45673","1234", requestedBenfitPlanCodes);

        verify(strategyDataDao, times(1)).getBenefitCostSummary(anyString());

        // Prospect Strategy
        BenefitCostSummary benefitCostSummary = benefitCostSummaryFuture.get();
        assertEquals(5, benefitCostSummary.getCurrentStrategy().getBenCosts().size());
        assertEquals("Medical", benefitCostSummary.getCurrentStrategy().getBenCosts().get(0).getBenType());
        assertEquals(BigDecimal.valueOf(338).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(0).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(273).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(0).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(611).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(0).getBenTypeTotal().getTotal().setScale(2));
        assertEquals("Dental", benefitCostSummary.getCurrentStrategy().getBenCosts().get(1).getBenType());
        assertEquals(BigDecimal.valueOf(115).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(1).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(195).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(1).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(310).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(1).getBenTypeTotal().getTotal().setScale(2));
        assertEquals("Vision", benefitCostSummary.getCurrentStrategy().getBenCosts().get(2).getBenType());
        assertEquals(BigDecimal.valueOf(80).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(2).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(40).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(2).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(120).setScale(2), benefitCostSummary.getCurrentStrategy().getBenCosts().get(2).getBenTypeTotal().getTotal().setScale(2));
        assertEquals("Life / AD&D", benefitCostSummary.getCurrentStrategy().getBenCosts().get(3).getBenType());
        assertEquals(BigDecimal.valueOf(10110.13), benefitCostSummary.getCurrentStrategy().getBenCosts().get(3).getBenTypeTotal().getErAmount());
        assertNull(benefitCostSummary.getCurrentStrategy().getBenCosts().get(3).getBenTypeTotal().getEeAmount());
        assertEquals(BigDecimal.valueOf(10110.13), benefitCostSummary.getCurrentStrategy().getBenCosts().get(3).getBenTypeTotal().getTotal());
        assertEquals("Disability", benefitCostSummary.getCurrentStrategy().getBenCosts().get(4).getBenType());
        assertEquals(BigDecimal.valueOf(1338.56), benefitCostSummary.getCurrentStrategy().getBenCosts().get(4).getBenTypeTotal().getErAmount());
        assertNull(benefitCostSummary.getCurrentStrategy().getBenCosts().get(4).getBenTypeTotal().getEeAmount());
        assertEquals(BigDecimal.valueOf(1338.56), benefitCostSummary.getCurrentStrategy().getBenCosts().get(4).getBenTypeTotal().getTotal());
        assertEquals(BigDecimal.valueOf(143780.28).setScale(2), benefitCostSummary.getCurrentStrategy().getAnnualTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(6096).setScale(2), benefitCostSummary.getCurrentStrategy().getAnnualTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(149876.28).setScale(2), benefitCostSummary.getCurrentStrategy().getAnnualTotal().getTotal().setScale(2));
        assertEquals(BigDecimal.valueOf(11981.69).setScale(2), benefitCostSummary.getCurrentStrategy().getMonthlyTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(508.00).setScale(2), benefitCostSummary.getCurrentStrategy().getMonthlyTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(12489.69).setScale(2), benefitCostSummary.getCurrentStrategy().getMonthlyTotal().getTotal().setScale(2));
        // Trinet Strategy
        assertEquals(7, benefitCostSummary.getTrinetStrategy().getBenCosts().size());
        assertEquals("Medical", benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenType());
        assertEquals(BigDecimal.valueOf(488).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(312).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(800).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenTypeTotal().getTotal().setScale(2));
        assertEquals("Dental", benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenType());
        assertEquals(BigDecimal.valueOf(187).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(183).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(370).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenTypeTotal().getTotal().setScale(2));
        assertEquals("Vision", benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenType());
        assertEquals(BigDecimal.valueOf(100).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(60).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(160).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenTypeTotal().getTotal().setScale(2));
        assertEquals("Life / AD&D", benefitCostSummary.getTrinetStrategy().getBenCosts().get(3).getBenType());
        assertEquals(BigDecimal.valueOf(623).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(3).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(0).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(3).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(623).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(3).getBenTypeTotal().getTotal().setScale(2));
        assertEquals("Disability", benefitCostSummary.getTrinetStrategy().getBenCosts().get(4).getBenType());
        assertEquals(BigDecimal.valueOf(846).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(4).getBenTypeTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(0).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(4).getBenTypeTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(846).setScale(2), benefitCostSummary.getTrinetStrategy().getBenCosts().get(4).getBenTypeTotal().getTotal().setScale(2));
        assertEquals(BigDecimal.valueOf(26928).setScale(2), benefitCostSummary.getTrinetStrategy().getAnnualTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(6660).setScale(2), benefitCostSummary.getTrinetStrategy().getAnnualTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(33588).setScale(2), benefitCostSummary.getTrinetStrategy().getAnnualTotal().getTotal().setScale(2));
        assertEquals(BigDecimal.valueOf(2244).setScale(2), benefitCostSummary.getTrinetStrategy().getMonthlyTotal().getErAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(555).setScale(2), benefitCostSummary.getTrinetStrategy().getMonthlyTotal().getEeAmount().setScale(2));
        assertEquals(BigDecimal.valueOf(2799).setScale(2), benefitCostSummary.getTrinetStrategy().getMonthlyTotal().getTotal().setScale(2));
        assertEquals(false, benefitCostSummary.getTrinetStrategy().getBenCosts().get(5).isDisplayOnReport());
        assertEquals(false, benefitCostSummary.getTrinetStrategy().getBenCosts().get(6).isDisplayOnReport());

        assertEquals("5", benefitCostSummary.getEnrollmentByType().get(0).getTotal());
        assertEquals("Medical", benefitCostSummary.getEnrollmentByType().get(0).getBenType());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeSpouseTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeChildTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getWaivedTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeFamilyTier());
        assertTrue(benefitCostSummary.getEnrollmentByType().get(0).isDisplayOnReport());

        assertEquals("4", benefitCostSummary.getEnrollmentByType().get(1).getTotal());
        assertEquals("Dental", benefitCostSummary.getEnrollmentByType().get(1).getBenType());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getEeTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getEeSpouseTier());
        assertEquals("0", benefitCostSummary.getEnrollmentByType().get(1).getEeChildTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getWaivedTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getEeFamilyTier());
        assertTrue(benefitCostSummary.getEnrollmentByType().get(1).isDisplayOnReport());

        assertEquals("Vision", benefitCostSummary.getEnrollmentByType().get(2).getBenType());
        assertTrue(benefitCostSummary.getEnrollmentByType().get(2).isDisplayOnReport());
    }
    
	/**
	 * given OutputRequest with benefitType as medical
	 * when getBenefitCostSummaryData called
	 * then verify EnrollmentByType::isDisplayOnReport value should be true rest should be false 
	 */
    @Test
    public void getBenefitGroupCostSummaryData2() throws InterruptedException, ExecutionException {
        OutputRequest request = new OutputRequest();
        request.setTnStrategyId("45673");
        List<String> benTypes =Arrays.asList(new String("10"));
        request.setBenefitTypes(benTypes);

        BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary = createBenefitTypeEmployeeCostSummary();
        when(strategyDataDao.getBenefitCostSummary(anyString())).thenReturn(getAddData());
        when(strategyDataDao.getStrategyHsaFunding(anyList())).thenReturn(getHsaData());
        when(strategyDataDao.getStrategyFundingByStrategy(anyList())).thenReturn(getWaiverData());
        when(prospectPlanService.getBenefitPlansBy("1234")).thenReturn(getBenefitPlanData());


        CompletableFuture<BenefitCostSummary> benefitCostSummaryFuture = benefitCostSummaryService.getBenefitCostSummaryData(benefitTypeEmployeeCostSummary, "45673","1234", request.getBenefitTypes());

        verify(strategyDataDao, times(1)).getBenefitCostSummary(anyString());

        BenefitCostSummary benefitCostSummary = benefitCostSummaryFuture.get();
        assertEquals(5, benefitCostSummary.getCurrentStrategy().getBenCosts().size());

        assertEquals("5", benefitCostSummary.getEnrollmentByType().get(0).getTotal());
        assertEquals("Medical", benefitCostSummary.getEnrollmentByType().get(0).getBenType());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeSpouseTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeChildTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getWaivedTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(0).getEeFamilyTier());
        assertTrue(benefitCostSummary.getEnrollmentByType().get(0).isDisplayOnReport());

        assertEquals("4", benefitCostSummary.getEnrollmentByType().get(1).getTotal());
        assertEquals("Dental", benefitCostSummary.getEnrollmentByType().get(1).getBenType());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getEeTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getEeSpouseTier());
        assertEquals("0", benefitCostSummary.getEnrollmentByType().get(1).getEeChildTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getWaivedTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(1).getEeFamilyTier());
        assertFalse(benefitCostSummary.getEnrollmentByType().get(1).isDisplayOnReport());
        
        assertEquals("Vision", benefitCostSummary.getEnrollmentByType().get(2).getBenType());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(2).getEeTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(2).getEeSpouseTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(2).getEeChildTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(2).getWaivedTier());
        assertEquals("1", benefitCostSummary.getEnrollmentByType().get(2).getEeFamilyTier());
        assertFalse(benefitCostSummary.getEnrollmentByType().get(2).isDisplayOnReport());
    }

    /**
     * given benefitTypeEmployeeCostSummary is null
     * when getBenefitCostSummaryData called
     * then verify medical, dental and vision benefit types are not present in the trinet strategy
     */
    @Test
    public void getBenefitGroupCostSummaryData3() throws InterruptedException, ExecutionException {
        OutputRequest request = new OutputRequest();
        request.setTnStrategyId("45673");
        List<String> benTypes =Arrays.asList(new String("10"));
        request.setBenefitTypes(benTypes);

        BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary = null;
        when(prospectPlanService.getBenefitPlansBy("1234")).thenReturn(getBenefitPlanData());
        when(strategyDataDao.getBenefitCostSummary(anyString())).thenReturn(getAddData());
        when(strategyDataDao.getStrategyHsaFunding(anyList())).thenReturn(getHsaData());
        when(strategyDataDao.getStrategyFundingByStrategy(anyList())).thenReturn(getWaiverData());

        CompletableFuture<BenefitCostSummary> benefitCostSummaryFuture = benefitCostSummaryService.getBenefitCostSummaryData(benefitTypeEmployeeCostSummary, "45673","1234", request.getBenefitTypes());

        verify(strategyDataDao, times(1)).getBenefitCostSummary(anyString());
        verify(strategyDataDao, times(1)).getStrategyHsaFunding(anyList());
        verify(strategyDataDao, times(1)).getStrategyFundingByStrategy(anyList());

        BenefitCostSummary benefitCostSummary = benefitCostSummaryFuture.get();
        assertEquals(5, benefitCostSummary.getCurrentStrategy().getBenCosts().size());

        assertTrue(benefitCostSummary.getEnrollmentByType().isEmpty());
        assertEquals(7, benefitCostSummary.getTrinetStrategy().getBenCosts().size());
        assertEquals("Medical", benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenTypeTotal().getTotal());

        assertEquals("Dental", benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenTypeTotal().getTotal());

        assertEquals("Vision", benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenTypeTotal().getTotal());

        assertEquals("Life / AD&D", benefitCostSummary.getTrinetStrategy().getBenCosts().get(3).getBenType());
        assertTrue(BigDecimal.valueOf(623.00).compareTo(benefitCostSummary.getTrinetStrategy().getBenCosts().get(3).getBenTypeTotal().getTotal()) == 0);

        assertEquals("Disability", benefitCostSummary.getTrinetStrategy().getBenCosts().get(4).getBenType());
        assertTrue(BigDecimal.valueOf(846.00).compareTo(benefitCostSummary.getTrinetStrategy().getBenCosts().get(4).getBenTypeTotal().getTotal()) == 0);

        assertEquals("HSA", benefitCostSummary.getTrinetStrategy().getBenCosts().get(5).getBenType());
        assertTrue(BigDecimal.ZERO.compareTo(benefitCostSummary.getTrinetStrategy().getBenCosts().get(5).getBenTypeTotal().getTotal()) == 0);

        assertEquals("Waiver Allowance", benefitCostSummary.getTrinetStrategy().getBenCosts().get(6).getBenType());
        assertTrue(BigDecimal.ZERO.compareTo(benefitCostSummary.getTrinetStrategy().getBenCosts().get(6).getBenTypeTotal().getTotal()) == 0);

    }

    /**
     * given benefitTypeEmployeeCostSummary.getEmplCostSummaryByBenGroup() is null
     * when getBenefitCostSummaryData called
     * then verify medical, dental and vision benefit types are not present in the trinet strategy
     */
    @Test
    public void getBenefitGroupCostSummaryData4() throws InterruptedException, ExecutionException {
        OutputRequest request = new OutputRequest();
        request.setTnStrategyId("45673");
        List<String> benTypes =Arrays.asList(new String("10"));
        request.setBenefitTypes(benTypes);

        BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary = new BenefitTypeEmployeeCostSummary(null);
        when(prospectPlanService.getBenefitPlansBy("1234")).thenReturn(getBenefitPlanData());
        when(strategyDataDao.getBenefitCostSummary(anyString())).thenReturn(new ArrayList<>());
        when(strategyDataDao.getStrategyHsaFunding(anyList())).thenReturn(new ArrayList<>());
        when(strategyDataDao.getStrategyFundingByStrategy(anyList())).thenReturn(new ArrayList<>());

        CompletableFuture<BenefitCostSummary> benefitCostSummaryFuture = benefitCostSummaryService.getBenefitCostSummaryData(benefitTypeEmployeeCostSummary, "45673","1234", request.getBenefitTypes());

        verify(strategyDataDao, times(1)).getBenefitCostSummary(anyString());
        verify(strategyDataDao, times(1)).getStrategyHsaFunding(anyList());
        verify(strategyDataDao, times(1)).getStrategyFundingByStrategy(anyList());

        BenefitCostSummary benefitCostSummary = benefitCostSummaryFuture.get();
        assertEquals(5, benefitCostSummary.getCurrentStrategy().getBenCosts().size());

        assertTrue(benefitCostSummary.getEnrollmentByType().isEmpty());
        assertEquals(3, benefitCostSummary.getTrinetStrategy().getBenCosts().size());
        assertEquals("Medical", benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenTypeTotal().getTotal());

        assertEquals("Dental", benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenTypeTotal().getTotal());

        assertEquals("Vision", benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenTypeTotal().getTotal());

    }

    /**
     * given benefitTypeEmployeeCostSummary.getEmplCostSummaryByBenGroup().isEmpty()
     * when getBenefitCostSummaryData called
     * then verify medical, dental and vision benefit types are not present in the trinet strategy
     */
    @Test
    public void getBenefitGroupCostSummaryData5() throws InterruptedException, ExecutionException {
        OutputRequest request = new OutputRequest();
        request.setTnStrategyId("45673");
        List<String> benTypes =Arrays.asList(new String("10"));
        request.setBenefitTypes(benTypes);

        BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary = new BenefitTypeEmployeeCostSummary(new HashMap<>());
        when(prospectPlanService.getBenefitPlansBy("1234")).thenReturn(getBenefitPlanData());
        when(strategyDataDao.getBenefitCostSummary(anyString())).thenReturn(new ArrayList<>());
        when(strategyDataDao.getStrategyHsaFunding(anyList())).thenReturn(new ArrayList<>());
        when(strategyDataDao.getStrategyFundingByStrategy(anyList())).thenReturn(new ArrayList<>());

        CompletableFuture<BenefitCostSummary> benefitCostSummaryFuture = benefitCostSummaryService.getBenefitCostSummaryData(benefitTypeEmployeeCostSummary, "45673","1234", request.getBenefitTypes());

        verify(strategyDataDao, times(1)).getBenefitCostSummary(anyString());
        verify(strategyDataDao, times(1)).getStrategyHsaFunding(anyList());
        verify(strategyDataDao, times(1)).getStrategyFundingByStrategy(anyList());

        BenefitCostSummary benefitCostSummary = benefitCostSummaryFuture.get();
        assertEquals(5, benefitCostSummary.getCurrentStrategy().getBenCosts().size());

        assertTrue(benefitCostSummary.getEnrollmentByType().isEmpty());
        assertEquals(3, benefitCostSummary.getTrinetStrategy().getBenCosts().size());
        assertEquals("Medical", benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(0).getBenTypeTotal().getTotal());

        assertEquals("Dental", benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(1).getBenTypeTotal().getTotal());

        assertEquals("Vision", benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenType());
        assertEquals(null, benefitCostSummary.getTrinetStrategy().getBenCosts().get(2).getBenTypeTotal().getTotal());

    }


    /**
     * given prospectBenefitdetails request 
     * when getProspectBenefitDetails method is called
     * then verify BenefitsDetailsResponse for 23, 30, 31 
     */
    @Test
    public void getProspectBenefitDetailsTest() {
    	// given
    	List<BenefitsDetailsResponse> response = prepareBenefitsDetailsRes();
    	ArgumentCaptor<ProspectApiRequest> apiRequestArgCaptor = ArgumentCaptor
    			.forClass(ProspectApiRequest.class);
    	// when
    	when(prospectServiceRestClient.prepareRequestAndCallEndPoint(apiRequestArgCaptor.capture()))
    	.thenReturn((Object) response);
    	// then
    	List<BenefitsDetailsResponse> actualResult = benefitCostSummaryService.getProspectBenefitDetails("P1");
    	// assertions
    	assertNotNull(actualResult);
    	BenefitsDetailsResponse benefitsDetailsResponse = actualResult.get(0);
    	assertEquals(1, benefitsDetailsResponse.getGroupId());
    	assertEquals(3, benefitsDetailsResponse.getBenefitTypes().size());
    	assertEquals("group1", benefitsDetailsResponse.getGroupName());
    	List<BenefitType> benefitTypes = benefitsDetailsResponse.getBenefitTypes();

    	assertEquals("23", benefitTypes.get(0).getBenefitTypeCode());
    	assertEquals(BigDecimal.valueOf(10.01), benefitTypes.get(0).getMonthlyTotal());

    	assertEquals("30", benefitTypes.get(1).getBenefitTypeCode());
    	assertEquals(BigDecimal.valueOf(20.55), benefitTypes.get(1).getMonthlyTotal());

    	assertEquals("31", benefitTypes.get(2).getBenefitTypeCode());
    	assertEquals(BigDecimal.valueOf(30.03), benefitTypes.get(2).getMonthlyTotal());

    	benefitsDetailsResponse = actualResult.get(1);
    	assertEquals(2, benefitsDetailsResponse.getGroupId());
    	assertEquals(2, benefitsDetailsResponse.getBenefitTypes().size());
    	assertEquals("group2", benefitsDetailsResponse.getGroupName());
    	benefitTypes = benefitsDetailsResponse.getBenefitTypes();

    	assertEquals("23", benefitTypes.get(0).getBenefitTypeCode());
    	assertEquals(BigDecimal.valueOf(10100.12), benefitTypes.get(0).getMonthlyTotal());

    	assertEquals("30", benefitTypes.get(1).getBenefitTypeCode());
    	assertEquals(BigDecimal.valueOf(1287.98), benefitTypes.get(1).getMonthlyTotal());

    }

    private List<BenefitsDetailsResponse> prepareBenefitsDetailsRes() {
    	return List.of(
    			BenefitsDetailsResponse.builder().groupId(1).groupName("group1").groupType("STD")
    			.monthlyTotal(BigDecimal.valueOf(1437.86)).headCount(4)
    			.benefitTypes(List.of(
    					BenefitType.builder().benefitTypeCode("23").monthlyTotal(BigDecimal.valueOf(10.01))
    					.fundingDetails(FundingDetails.builder().fundingType(null)
    							.cvgCodeValues(null)
    							.build())
    					.build(),

    					BenefitType.builder().benefitTypeCode("30").monthlyTotal(BigDecimal.valueOf(20.55))
    					.fundingDetails(FundingDetails.builder().fundingType(null)
    							.cvgCodeValues(null)
    							.build())
    					.build(),

    					BenefitType.builder().benefitTypeCode("31").monthlyTotal(BigDecimal.valueOf(30.03))
    					.fundingDetails(FundingDetails.builder().fundingType(null)
    							.cvgCodeValues(null)
    							.build())
    					.build()))
    			.build(),
    			BenefitsDetailsResponse.builder().groupId(2).groupName("group2").groupType("K1")
    			.monthlyTotal(BigDecimal.valueOf(14633.97)).headCount(5)
    			.benefitTypes(List.of(
    					BenefitType.builder().benefitTypeCode("23").monthlyTotal(BigDecimal.valueOf(10100.12))
    					.planCarriers(null)
    					.fundingDetails(null)
    					.build(),
    					BenefitType.builder().benefitTypeCode("30").monthlyTotal(BigDecimal.valueOf(1287.98))
    					.planCarriers(null)
    					.fundingDetails(null)
    					.build()))
    			.build());
    }

    private BenefitTypeEmployeeCostSummary createBenefitTypeEmployeeCostSummary() {
        BenefitTypeEmployeeCostSummary benefitTypeEmployeeCostSummary = new BenefitTypeEmployeeCostSummary(new HashMap<>());
        Map<String, Map<String,List<EmployeeCostSummary>>> employeeCostSummary = new HashMap<>();
        employeeCostSummary.put(PlanTypesEnum.MEDICAL.getCode(), createEmployeeCostSummaryMedicalData());
        employeeCostSummary.put(PlanTypesEnum.DENTAL.getCode(), createEmployeeCostSummaryDemtalData());
        employeeCostSummary.put(PlanTypesEnum.VISION.getCode(), createEmployeeCostSummaryVisionData());
        benefitTypeEmployeeCostSummary.setEmplCostSummaryByBenGroup(employeeCostSummary);
        return benefitTypeEmployeeCostSummary;
    }

    private Map<String,List<EmployeeCostSummary>> createEmployeeCostSummaryMedicalData() {
        Map<String, List<EmployeeCostSummary>> employeeCostSummaryMedicalData = new HashMap<>();
        List<EmployeeCostSummary> employeeCostSummaryList = new ArrayList<>();
        EmployeeDetails e1 = new EmployeeDetails();
        e1.setCoverageCode("W");
        e1.setGroup("A1");
        e1.setState("CA");
        e1.setFirstName("John");
        e1.setLastName("Doe");
        EmployeeDetails e2 = new EmployeeDetails();
        e2.setCoverageCode("1");
        e2.setGroup("A2");
        e2.setState("CA");
        e2.setFirstName("Hin");
        e2.setLastName("Chui");
        EmployeeDetails e3 = new EmployeeDetails();
        e3.setCoverageCode("2");
        e3.setGroup("A3");
        e3.setState("CA");
        e3.setFirstName("Kris");
        e3.setLastName("Patt");
        EmployeeDetails e4 = new EmployeeDetails();
        e4.setCoverageCode("C");
        e4.setGroup("A3");
        e4.setState("CA");
        e4.setFirstName("Prem");
        e4.setLastName("Kumar");
        EmployeeDetails e5 = new EmployeeDetails();
        e5.setCoverageCode("4");
        e5.setGroup("A4");
        e5.setState("CA");
        e5.setFirstName("Rina");
        e5.setLastName("Jain");
        EmployeeCostSummary employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e1)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Medical Plan Name")
                        .erAmount(BigDecimal.valueOf(100))
                        .eeAmount(BigDecimal.valueOf(50))
                        .total(BigDecimal.valueOf(150))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Medical Plan Name")
                        .erAmount(BigDecimal.valueOf(200))
                        .eeAmount(BigDecimal.valueOf(100))
                        .total(BigDecimal.valueOf(300))
                        .build())
                .costDiff(150)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e2)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Medical Plan Name")
                        .erAmount(BigDecimal.valueOf(200))
                        .eeAmount(BigDecimal.valueOf(100))
                        .total(BigDecimal.valueOf(300))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Medical Plan Name")
                        .erAmount(BigDecimal.valueOf(300))
                        .eeAmount(BigDecimal.valueOf(150))
                        .total(BigDecimal.valueOf(450))
                        .build())
                .costDiff(150)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e3)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(60))
                        .eeAmount(BigDecimal.valueOf(40))
                        .total(BigDecimal.valueOf(100))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(80))
                        .eeAmount(BigDecimal.valueOf(50))
                        .total(BigDecimal.valueOf(130))
                        .build())
                .costDiff(30)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e4)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(43))
                        .eeAmount(BigDecimal.valueOf(58))
                        .total(BigDecimal.valueOf(101))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(86))
                        .eeAmount(BigDecimal.valueOf(24))
                        .total(BigDecimal.valueOf(100))
                        .build())
                .costDiff(30)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e5)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(35))
                        .eeAmount(BigDecimal.valueOf(75))
                        .total(BigDecimal.valueOf(110))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(22))
                        .eeAmount(BigDecimal.valueOf(88))
                        .total(BigDecimal.valueOf(110))
                        .build())
                .costDiff(30)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);
        employeeCostSummaryMedicalData.put("w2",employeeCostSummaryList);
        return employeeCostSummaryMedicalData;
    }

    private Map<String, List<EmployeeCostSummary>> createEmployeeCostSummaryDemtalData() {
        Map<String, List<EmployeeCostSummary>> employeeCostSummaryDemtalData = new HashMap<>();
        List<EmployeeCostSummary> employeeCostSummaryList = new ArrayList<>();
        EmployeeDetails e1 = new EmployeeDetails();
        e1.setCoverageCode("W");
        e1.setGroup("A1");
        e1.setState("CA");
        e1.setFirstName("John");
        e1.setLastName("Doe");
        EmployeeDetails e2 = new EmployeeDetails();
        e2.setCoverageCode("1");
        e2.setGroup("A2");
        e2.setState("CA");
        e2.setFirstName("Hin");
        e2.setLastName("Chui");
        EmployeeDetails e3 = new EmployeeDetails();
        e3.setCoverageCode("2");
        e3.setGroup("A3");
        e3.setState("CA");
        e3.setFirstName("Kris");
        e3.setLastName("Patt");
        EmployeeDetails e4 = new EmployeeDetails();
        e4.setCoverageCode("C");
        e4.setGroup("A3");
        e4.setState("CA");
        e4.setFirstName("Prem");
        e4.setLastName("Kumar");
        EmployeeDetails e5 = new EmployeeDetails();
        e5.setCoverageCode("4");
        e5.setGroup("A4");
        e5.setState("CA");
        e5.setFirstName("Rina");
        e5.setLastName("Jain");
        EmployeeCostSummary employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e1)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(50))
                        .eeAmount(BigDecimal.valueOf(25))
                        .total(BigDecimal.valueOf(75))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(75))
                        .eeAmount(BigDecimal.valueOf(50))
                        .total(BigDecimal.valueOf(125))
                        .build())
                .costDiff(50)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e2)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(20))
                        .eeAmount(BigDecimal.valueOf(80))
                        .total(BigDecimal.valueOf(100))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(85))
                        .eeAmount(BigDecimal.valueOf(45))
                        .total(BigDecimal.valueOf(130))
                        .build())
                .costDiff(30)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e3)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(60))
                        .eeAmount(BigDecimal.valueOf(40))
                        .total(BigDecimal.valueOf(100))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(80))
                        .eeAmount(BigDecimal.valueOf(50))
                        .total(BigDecimal.valueOf(130))
                        .build())
                .costDiff(30)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e4)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(43))
                        .eeAmount(BigDecimal.valueOf(58))
                        .total(BigDecimal.valueOf(101))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(86))
                        .eeAmount(BigDecimal.valueOf(24))
                        .total(BigDecimal.valueOf(100))
                        .build())
                .costDiff(30)
                .build();

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e5)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(35))
                        .eeAmount(BigDecimal.valueOf(75))
                        .total(BigDecimal.valueOf(110))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Dental Plan Name")
                        .erAmount(BigDecimal.valueOf(22))
                        .eeAmount(BigDecimal.valueOf(88))
                        .total(BigDecimal.valueOf(110))
                        .build())
                .costDiff(30)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummaryDemtalData.put("w2",employeeCostSummaryList);
        return employeeCostSummaryDemtalData;
    }

    private Map<String, List<EmployeeCostSummary>> createEmployeeCostSummaryVisionData() {
        Map<String, List<EmployeeCostSummary>> employeeCostSummaryVisionData = new HashMap<>();
        List<EmployeeCostSummary> employeeCostSummaryList = new ArrayList<>();
        EmployeeDetails e1 = new EmployeeDetails();
        e1.setCoverageCode("W");
        e1.setGroup("A1");
        e1.setState("CA");
        e1.setFirstName("John");
        e1.setLastName("Doe");
        EmployeeDetails e2 = new EmployeeDetails();
        e2.setCoverageCode("1");
        e2.setGroup("A2");
        e2.setState("CA");
        e2.setFirstName("Hin");
        e2.setLastName("Chui");
        EmployeeDetails e3 = new EmployeeDetails();
        e3.setCoverageCode("2");
        e3.setGroup("A3");
        e3.setState("CA");
        e3.setFirstName("Kris");
        e3.setLastName("Patt");
        EmployeeDetails e4 = new EmployeeDetails();
        e4.setCoverageCode("C");
        e4.setGroup("A3");
        e4.setState("CA");
        e4.setFirstName("Prem");
        e4.setLastName("Kumar");
        EmployeeDetails e5 = new EmployeeDetails();
        e5.setCoverageCode("4");
        e5.setGroup("A4");
        e5.setState("CA");
        e5.setFirstName("Rina");
        e5.setLastName("Jain");
        EmployeeCostSummary employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e1)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(30))
                        .eeAmount(BigDecimal.valueOf(20))
                        .total(BigDecimal.valueOf(50))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(40))
                        .eeAmount(BigDecimal.valueOf(25))
                        .total(BigDecimal.valueOf(65))
                        .build())
                .costDiff(15)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e2)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(20))
                        .eeAmount(BigDecimal.valueOf(10))
                        .total(BigDecimal.valueOf(30))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(25))
                        .eeAmount(BigDecimal.valueOf(15))
                        .total(BigDecimal.valueOf(40))
                        .build())
                .costDiff(10)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e3)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(20))
                        .eeAmount(BigDecimal.valueOf(10))
                        .total(BigDecimal.valueOf(30))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(25))
                        .eeAmount(BigDecimal.valueOf(15))
                        .total(BigDecimal.valueOf(40))
                        .build())
                .costDiff(10)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e4)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(20))
                        .eeAmount(BigDecimal.valueOf(10))
                        .total(BigDecimal.valueOf(30))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(25))
                        .eeAmount(BigDecimal.valueOf(15))
                        .total(BigDecimal.valueOf(40))
                        .build())
                .costDiff(10)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummary = EmployeeCostSummary.builder()
                .employee(e5)
                .currentPlan(BenefitTypeTotal.builder()
                        .planName("Current Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(20))
                        .eeAmount(BigDecimal.valueOf(10))
                        .total(BigDecimal.valueOf(30))
                        .build())
                .triNetPlan(BenefitTypeTotal.builder()
                        .planName("TriNet Vision Plan Name")
                        .erAmount(BigDecimal.valueOf(25))
                        .eeAmount(BigDecimal.valueOf(15))
                        .total(BigDecimal.valueOf(40))
                        .build())
                .costDiff(10)
                .build();
        employeeCostSummaryList.add(employeeCostSummary);

        employeeCostSummaryVisionData.put("w2",employeeCostSummaryList);
        return employeeCostSummaryVisionData;
    }

    private List<Object[]> getAddData() {
        List<Object[]> results= new ArrayList<>();
        Object[] r1 = {"23", null, BigDecimal.valueOf(623)};
        results.add(r1);
        Object[] r2 = {"30", null, BigDecimal.valueOf(323)};
        results.add(r2);
        Object[] r3 = {"31", null, BigDecimal.valueOf(523)};
        results.add(r3);
        Object[] r4 = {"10", "HSA", BigDecimal.valueOf(0)};
        results.add(r4);
        Object[] r5 = {"10", "WA", BigDecimal.valueOf(0)};
        results.add(r5);

        return results;
    }

    private List<BenefitPlansRes> getBenefitPlanData() {
        List<BenefitPlansRes> results= new ArrayList<>();
        BenefitPlansRes r1 = new BenefitPlansRes();
        results.add(r1);
        return results;
    }

    private List<Object[]> getHsaData() {
        List<Object[]> results= new ArrayList<>();
        Object[] hsaResult = {"306620", null, null};
        results.add(hsaResult);
        return results;
    }

    private List<Object[]> getWaiverData() {
        List<Object[]> results= new ArrayList<>();
        Object[] waiverResult1 = {null, "CFPCT", null, null, null, null, null, null, null};
        results.add(waiverResult1);
        Object[] waiverResult2 = {null, "CFPCT", null, null, null, null, null, null, null};
        results.add(waiverResult2);
        return results;
    }

}