package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;
import com.trinet.ambis.service.prospect.impl.ProspectPlanServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.ProspectServiceRestClient;

@RunWith(MockitoJUnitRunner.class)
public class ProspectPlanServiceTest extends ServiceUnitTest {

    @InjectMocks
    ProspectPlanServiceImpl prospectPlanService;

    @Mock
    ProspectServiceRestClient prospectServiceRestClient;

    @Test
    public void getBenefitPlansByTest() {
        String prospectId = "P1";
        String rateType = "";
        boolean includeWithRates = true;
        List<BenefitPlansRes> benefitPlansRes = prepareBenefitPlansRes();

        ArgumentCaptor<ProspectApiRequest> argCaptor = ArgumentCaptor.forClass(ProspectApiRequest.class);
        // method mocks
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(argCaptor.capture())).thenReturn(benefitPlansRes);

        //when
        List<BenefitPlansRes> actualResult = prospectPlanService.getBenefitPlansBy(prospectId,rateType,includeWithRates);
        // assertions
        assertEquals(3, actualResult.size());
        assertEquals(benefitPlansRes, actualResult);
        // verify
        verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(argCaptor.getValue());

    }

    @Test
    public void getBenefitPlansByTest2() {
        String prospectId = "P1";
        String rateType = "";
        boolean includeWithRates = true;
        List<BenefitPlansRes> benefitPlansRes = prepareBenefitPlansRes();

        ArgumentCaptor<ProspectApiRequest> argCaptor = ArgumentCaptor.forClass(ProspectApiRequest.class);
        // method mocks
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(argCaptor.capture())).thenReturn(null);

        //when
        List<BenefitPlansRes> actualResult = prospectPlanService.getBenefitPlansBy(prospectId,rateType,includeWithRates);
        // assertions
        assertEquals(0, actualResult.size());
        assertTrue(CollectionUtils.isEmpty(actualResult));
        // verify
        verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(argCaptor.getValue());

    }

    @Test
    public void getBenefitPlansByTest3() {
        String prospectId = "P1";
        List<BenefitPlansRes> benefitPlansRes = prepareBenefitPlansRes();
        ArgumentCaptor<ProspectApiRequest> argCaptor = ArgumentCaptor.forClass(ProspectApiRequest.class);
        // method mocks
        when(prospectServiceRestClient.prepareRequestAndCallEndPoint(argCaptor.capture())).thenReturn(benefitPlansRes);

        //when
        List<BenefitPlansRes> actualResult = prospectPlanService.getBenefitPlansBy(prospectId);
        // assertions
        assertEquals(3, actualResult.size());
        assertEquals(benefitPlansRes, actualResult);
        // verify
        verify(prospectServiceRestClient, times(1)).prepareRequestAndCallEndPoint(argCaptor.getValue());

    }

    private List<BenefitPlansRes> prepareBenefitPlansRes() {
        return List.of(
                BenefitPlansRes
                        .builder().benefitTypeCode("10").benefitType(
                                "Medical")
                        .benefitPlans(
                                List.of(BenefitPlansRes.BenefitPlan.builder().benefitPlanId(1)
                                                .benefitPlanName("Blue Shield of California PPO 1")
                                                .carrier("Blue Shield of California")
                                                .attributes(
                                                        List.of(BenefitPlansRes.Attribute.builder().id(1)
                                                                        .displayName("Plan Type").value("ACO").displayOrder(1).build(),
                                                                BenefitPlansRes.Attribute.builder().id(2)
                                                                        .displayName("Plan Type").value("$40").displayOrder(2)
                                                                        .build()))
                                                .tierRates(List.of(BenefitPlansRes.TierRates.builder().cvgCode("1").cost(new BigDecimal(
                                                                "100.50")).build(),
                                                        BenefitPlansRes.TierRates.builder().cvgCode("2").cost(new BigDecimal(
                                                                "250.50")).build(),
                                                        BenefitPlansRes.TierRates.builder().cvgCode("C").cost(new BigDecimal(
                                                                "450.50")).build(),
                                                        BenefitPlansRes.TierRates.builder().cvgCode("4").cost(new BigDecimal(
                                                                "600.00")).build()))
                                                .build(),
                                        BenefitPlansRes.BenefitPlan.builder().benefitPlanId(2)
                                                .benefitPlanName("Aetna PPO 2000").carrier("Aetna")
                                                .attributes(List.of(
                                                        BenefitPlansRes.Attribute.builder().id(1)
                                                                .displayName("Primary Care Visit").value("$35")
                                                                .displayOrder(1).build(),
                                                        BenefitPlansRes.Attribute.builder().id(2)
                                                                .displayName("Plan Type").value("PPO").displayOrder(2)
                                                                .build()))
                                                .ageBandedRates(List.of(BenefitPlansRes.AgeBandedRates.builder().ageBandCode("14").cost(new BigDecimal(
                                                                "250.50")).build(),
                                                        BenefitPlansRes.AgeBandedRates.builder().ageBandCode("25").cost(new BigDecimal(
                                                                "650.50")).build(),
                                                        BenefitPlansRes.AgeBandedRates.builder().ageBandCode("37").cost(new BigDecimal(
                                                                "700.00")).build(),
                                                        BenefitPlansRes.AgeBandedRates.builder().ageBandCode("55").cost(new BigDecimal(
                                                                "680.80")).build()))
                                                .build()))
                        .build(),
                BenefitPlansRes
                        .builder().benefitTypeCode(
                                "11")
                        .benefitType("Dental")
                        .benefitPlans(List.of(
                                BenefitPlansRes.BenefitPlan.builder().benefitPlanId(3)
                                        .benefitPlanName("MetLife Voluntary VA").carrier("MetLife")
                                        .attributes(List.of(BenefitPlansRes.Attribute.builder().id(1)
                                                        .displayName("Plan Type").value("PPO").displayOrder(1).build(),
                                                BenefitPlansRes.Attribute.builder().id(2).displayName("Out-of-Network")
                                                        .value("100/80/50").displayOrder(2).build()))
                                        .build()))
                        .build(),
                BenefitPlansRes.builder().benefitTypeCode("14").benefitType("Vision").benefitPlans(List.of(
                                BenefitPlansRes.BenefitPlan.builder().benefitPlanId(5).benefitPlanName("MetLife Voluntary NV")
                                        .carrier("MetLife")
                                        .attributes(List.of(BenefitPlansRes.Attribute
                                                        .builder().id(1).displayName("In-Network").value("$10").displayOrder(1).build(),
                                                BenefitPlansRes.Attribute.builder().id(1).displayName("Family")
                                                        .value("$150/Family").displayOrder(2).build()))
                                        .build()))
                        .build());

    }

}
