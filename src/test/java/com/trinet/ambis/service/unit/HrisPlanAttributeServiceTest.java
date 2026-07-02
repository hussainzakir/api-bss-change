package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.trinet.ambis.service.impl.HrisPlanAttributeServiceImpl;
import com.trinet.ambis.service.model.plancompare.Attribute;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.HrisPlanServiceRestClient;


@RunWith(MockitoJUnitRunner.class)
public class HrisPlanAttributeServiceTest extends ServiceUnitTest {

    @InjectMocks
    private HrisPlanAttributeServiceImpl hrisPlanAttributeService;

    @Mock
    private HrisPlanServiceRestClient hrisPlanServiceRestClient;
    
    private MockedStatic<AppRulesAndConfigsUtils> mockStaticAppRulesAndConfigsUtils;
    
    @Before
	public void setUp() {
		if (mockStaticAppRulesAndConfigsUtils == null) {
			mockStaticAppRulesAndConfigsUtils = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
		}
	}

    @After
    public void tearDown() {
        if (mockStaticAppRulesAndConfigsUtils != null) {
            mockStaticAppRulesAndConfigsUtils.close();
        }
    }

    /**
     * given planId's
     * when getPlanAttributes method is called
     * then return the CompletableFuture of list of Benefit Plan Compare details
     * @throws ExecutionException
     * @throws InterruptedException
     **/
    @Test
    public void getPlanAttributesTest() throws InterruptedException, ExecutionException {
        // Given
        Set<String> planIds = Set.of("784527", "778533");

        // Mock
		Mockito.when(hrisPlanServiceRestClient.getPlanAttributes(any(), any()))
				.thenReturn(populateBenefitPlanCompareList());

        // When
        CompletableFuture<List<BenefitPlanCompare>> planAttributesFuture = hrisPlanAttributeService.getPlanAttributes(planIds);

        // Then
        List<BenefitPlanCompare> plans = planAttributesFuture.get();
        assertNotNull(plans);
        assertEquals(2, plans.size());
        BenefitPlanCompare plan1 = plans.get(0);
        assertEquals("10", plan1.getBenefitType());
        assertEquals("784527", plan1.getPlanId());
        assertEquals("Carrier A", plan1.getCarrier());
        assertEquals("[2025] Gold Plan $1250/$35/20%/$8600", plan1.getName());
        BenefitPlanCompare plan2 = plans.get(1);
        assertEquals("10", plan2.getBenefitType());
        assertEquals("778533", plan2.getPlanId());
        assertEquals("Carrier A", plan2.getCarrier());
        assertEquals("[2025] Prem Plan", plan2.getName());
        List<PlanCompareTemplate> templates = plan1.getTemplate();
        assertEquals(1, templates.size());
        assertEquals("medical_ouput", templates.get(0).getName());
        assertEquals("Category", templates.get(0).getType());
        assertEquals("Deductible", templates.get(0).getChildren().get(0).getName());
        assertEquals("Single Deductible", templates.get(0).getChildren().get(0).getChildren().get(0).getName());
    }

    /**
     * given empty planId's
     * when getPlanAttributes method is called
     * then return the CompletableFuture of list of Benefit Plan Compare details
     * @throws ExecutionException
     * @throws InterruptedException
     **/
    @Test
    public void getPlanAttributesInvalidEmptyPlanIds() throws ExecutionException, InterruptedException {
        // Given
        Set<String> planIds = Collections.emptySet();

        // When
        CompletableFuture<List<BenefitPlanCompare>> planAttributesFuture = hrisPlanAttributeService.getPlanAttributes(planIds);

        // Then
        List<BenefitPlanCompare> plans = planAttributesFuture.get();
        assertEquals(0, plans.size());
    }

    /**
     * given empty planId's
     * when getPlanAttributes method is called
     * then return the CompletableFuture of list of Benefit Plan Compare details
     * @throws ExecutionException
     * @throws InterruptedException
     **/
    @Test
    public void getPlanAttributesInvalidResponse() throws ExecutionException, InterruptedException {
        // Given
        Set<String> planIds = Set.of("784527", "778533");

        // Mock
		Mockito.when(hrisPlanServiceRestClient.getPlanAttributes(any(), any())).thenReturn(null);

        // When
        CompletableFuture<List<BenefitPlanCompare>> planAttributesFuture = hrisPlanAttributeService.getPlanAttributes(planIds);

        // Then
        List<BenefitPlanCompare> plans = planAttributesFuture.get();
        assertEquals(0, plans.size());

        // Verify
		verify(hrisPlanServiceRestClient, times(1)).getPlanAttributes(any(), any());
    }

    /**
     * given empty planId's
     * when getPlanAttributes method is called
     * then return the CompletableFuture of list of Benefit Plan Compare details
     * @throws ExecutionException
     * @throws InterruptedException
     **/
    @Test
    public void getPlanAttributesExceptionTest() throws ExecutionException, InterruptedException {
        // Given
        Set<String> planIds = Set.of("784527", "778533");

        // Mock
		Mockito.when(hrisPlanServiceRestClient.getPlanAttributes(any(), any()))
				.thenReturn(new RuntimeException("API call failed"));

        // When
        CompletableFuture<List<BenefitPlanCompare>> planAttributesFuture = hrisPlanAttributeService.getPlanAttributes(planIds);

        // Then
        List<BenefitPlanCompare> plans = planAttributesFuture.get();
        assertEquals(0, plans.size());

        // Verify
		verify(hrisPlanServiceRestClient, times(1)).getPlanAttributes(any(), any());
    }
    
    /**
     * given planId's
     * when getPlanAttributes method is called
     * then return the CompletableFuture of list of Benefit Plan Compare details
     * @throws ExecutionException
     * @throws InterruptedException
     **/
    @Test
	public void getV2PlanAttributesTest() throws InterruptedException, ExecutionException {
		// Given
		Set<String> planIds = Set.of("784527", "778533");

		// Mock
		Mockito.when(hrisPlanServiceRestClient.getPlanAttributes(any(), any()))
				.thenReturn(populateV2BenefitPlanCompareList());

		// When
		CompletableFuture<List<BenefitPlanCompare>> planAttributesFuture = hrisPlanAttributeService
				.getPlanAttributes(planIds);

		// Then
		List<BenefitPlanCompare> plans = planAttributesFuture.get();
		assertNotNull(plans);
		assertEquals(2, plans.size());
		BenefitPlanCompare plan1 = plans.get(0);
		assertEquals("11", plan1.getBenefitType());
		assertEquals("784527", plan1.getPlanId());
		assertEquals("Carrier A", plan1.getCarrier());
		assertEquals("2026 Metlife Dental PPO", plan1.getName());
		BenefitPlanCompare plan2 = plans.get(1);
		assertEquals("11", plan2.getBenefitType());
		assertEquals("778533", plan2.getPlanId());
		assertEquals("Carrier A", plan2.getCarrier());
		assertEquals("2026 Metlife Dental PEO", plan2.getName());
		List<PlanCompareTemplate> templates = plan1.getTemplate();
		assertEquals(1, templates.size());
		assertEquals("dental_ouput", templates.get(0).getName());
		assertEquals("Category", templates.get(0).getType());
		assertEquals("Deductible", templates.get(0).getChildren().get(0).getName());
		assertEquals("Single (In-Network/OON)", templates.get(0).getChildren().get(0).getChildren().get(0).getName());
	}

    private List<BenefitPlanCompare> populateBenefitPlanCompareList() {

        Attribute attributeSingleDeductible = Attribute.builder()
                .type("Attribute")
                .name("Single Deductible")
                .displayName("Single")
                .value("$1,250.00")
                .id(6)
                .build();

        Attribute attributeFamilyDeductible = Attribute.builder()
                .type("Attribute")
                .name("Family Deductible")
                .displayName("Family")
                .value( "$2,500.00")
                .id(7)
                .build();

        Attribute attributeDeductible = Attribute.builder()
                .type("Category")
                .name("Deductible")
                .displayOrder(2)
                .children(List.of(attributeSingleDeductible, attributeFamilyDeductible))
                .build();

        PlanCompareTemplate template = PlanCompareTemplate.builder()
                .type("Category")
                .name("medical_ouput")
                .displayOrder(1)
                .children(List.of(attributeDeductible))
                .build();

        BenefitPlanCompare planCompare1 = BenefitPlanCompare.builder()
                .planId("784527")
                .name("[2025] Gold Plan $1250/$35/20%/$8600")
                .benefitType("10")
                .carrier("Carrier A")
                .carrierLogoUrl("http://test/logoA.png")
                .template(List.of(template))
                .build();

        BenefitPlanCompare planCompare2 = BenefitPlanCompare.builder()
                .planId("778533")
                .name("[2025] Prem Plan")
                .benefitType("10")
                .carrier("Carrier A")
                .carrierLogoUrl("http://test/logoA.png")
                .template(List.of(template))
                .build();

        return List.of(planCompare1, planCompare2);
    }
    
    private List<BenefitPlanCompare> populateV2BenefitPlanCompareList() {

        Attribute attributeSingleDeductible = Attribute.builder()
                .type("Attribute")
                .name("Single (In-Network/OON)")
                .displayName("Single (In-Network/OON)")
                .value("$50.00 / $50.00")
                .id(100)
                .build();

        Attribute attributeFamilyDeductible = Attribute.builder()
                .type("Attribute")
                .name("Family (In-Network/OON)")
                .displayName("Family")
                .value( "$2,500.00")
                .id(101)
                .build();

        Attribute attributeDeductible = Attribute.builder()
                .type("Category")
                .name("Deductible")
                .displayOrder(1)
                .children(List.of(attributeSingleDeductible, attributeFamilyDeductible))
                .build();

        PlanCompareTemplate template = PlanCompareTemplate.builder()
                .type("Category")
                .name("dental_ouput")
                .displayOrder(1)
                .children(List.of(attributeDeductible))
                .build();

        BenefitPlanCompare planCompare1 = BenefitPlanCompare.builder()
                .planId("784527")
                .name("2026 Metlife Dental PPO")
                .benefitType("11")
                .carrier("Carrier A")
                .carrierLogoUrl("http://test/logoA.png")
                .template(List.of(template))
                .build();

        BenefitPlanCompare planCompare2 = BenefitPlanCompare.builder()
                .planId("778533")
                .name("2026 Metlife Dental PEO")
                .benefitType("11")
                .carrier("Carrier A")
                .carrierLogoUrl("http://test/logoA.png")
                .template(List.of(template))
                .build();

        return List.of(planCompare1, planCompare2);
    }
}
