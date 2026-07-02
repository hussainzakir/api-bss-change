package com.trinet.ambis.service.unit;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeEmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.service.HrisPlanAttributeService;
import com.trinet.ambis.service.impl.outputs.EmployeeCostSummaryServiceImpl;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.prospect.dto.EmployeeCostRes;
import com.trinet.ambis.service.prospect.service.ProspectEmployeeCostService;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeCostSummaryServiceImplTest {
 
	@InjectMocks
	EmployeeCostSummaryServiceImpl employeeCostSummaryService;

	@Mock
	StrategyDataDao strategyDataDao;
	
	@Mock
	ProspectEmployeeCostService prospectEmployeeCostService;

	@Mock
	HrisPlanAttributeService hrisPlanAttributeService;
    private MockedStatic<CompanyServiceHelper> companyServiceHelperMock;
    private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMock;

    @Before
    public void setUp() {
        companyServiceHelperMock = mockStatic(CompanyServiceHelper.class);
        appRulesAndConfigsUtilsMock = mockStatic(AppRulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        companyServiceHelperMock.close();
        appRulesAndConfigsUtilsMock.close();
    }
	
	@Test
	public void getCostSummaryData() {
 
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);

		OutputRequest request = new OutputRequest();
		request.setTnStrategyId("45673");

		request.setBenefitTypes(BSSApplicationConstants.PRIMARY_PLAN_TYPES_ER);

		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOptional = prepareSummaryList();
		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOmsOptional = prepareSummaryListOms();

		List<EmployeeCostRes> employeeCostResList = prepareEmployeeCostResList();

		ArgumentCaptor<List<Long>> strategyListOmsCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<String>> planTypesOmsCaptor = ArgumentCaptor.forClass(List.class);
		when(CompanyServiceHelper.isTibProspect(company)).thenCallRealMethod();
		when(strategyDataDao.getOmsStrategyGroupPlanCostByPlanType(strategyListOmsCaptor.capture(),planTypesOmsCaptor.capture()))
				.thenReturn(summaryListOmsOptional);
		
		ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<List<Long>> strategyListCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<String>> planTypesCaptor = ArgumentCaptor.forClass(List.class);
		when(strategyDataDao.getStrategyGroupPlanCostByPlanType(companyCaptor.capture(),
		strategyListCaptor.capture(),planTypesCaptor.capture()))
											.thenReturn(summaryListOptional);


		Optional<List<EmployeeCostRes>> employeeCostResListOptional = Optional.ofNullable(employeeCostResList);

		ArgumentCaptor<String> strategyId = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List<String>> benefitTypesCaptor = ArgumentCaptor.forClass(List.class);
		when(prospectEmployeeCostService.getProspectEmployeeCostByType(strategyId.capture(),
				benefitTypesCaptor.capture())).thenReturn(employeeCostResListOptional);
		when(hrisPlanAttributeService.getPlanAttributesByBenefitType(Set.of("000SR7"), BSSApplicationConstants.MEDICAL)).thenReturn(CompletableFuture.completedFuture(
				List.of(
						BenefitPlanCompare.builder()
								.planId("000SR7")
								.carrierId(1)
								.carrier("Carrier 1")
								.name("MetLife Enhanced OMS")
								.build())));

		when(CompanyServiceHelper.isTibProspect(Mockito.any(Company.class))).thenReturn(true);
		when(AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()).thenReturn(true);
		when(AppRulesAndConfigsUtils.isEmployeeComparePageBreakEnabled()).thenReturn(true);

		OutputData outputData = new OutputData();
		outputData.setTrinetStrategyIsBenTypeOffered(getOutputData().get());
		outputData.setCurrStrategyIsBenTypeOffered(getOutputData().get());

		CompletableFuture<BenefitTypeEmployeeCostSummary> costSummaryMapFuture = employeeCostSummaryService.getCostSummaryData(outputData, company,request);
		BenefitTypeEmployeeCostSummary costSummaryMap = costSummaryMapFuture.join();

		verify(strategyDataDao, times(1)).getStrategyGroupPlanCostByPlanType(any(Company.class),any(List.class),any(List.class));
		assertResults(costSummaryMap);
		assertPageBreakLogic(costSummaryMap);
	}

	private void assertPageBreakLogic(BenefitTypeEmployeeCostSummary costSummaryMap) {
		assertNotNull(costSummaryMap.getEmplCostSummaryByBenGroup().get("10"));
		// Verify that groups are present
		assertTrue(costSummaryMap.getEmplCostSummaryByBenGroup().get("10").containsKey("BENPROG1"));
		assertTrue(costSummaryMap.getEmplCostSummaryByBenGroup().get("10").containsKey("BENPROG2"));
		assertTrue(costSummaryMap.getEmplCostSummaryByBenGroup().get("10").containsKey("BENPROG3"));

		boolean foundPageBreak = false;
		for (Map.Entry<String, List<com.trinet.ambis.rest.controllers.dto.outputs.EmployeeCostSummary>> entry : costSummaryMap.getEmplCostSummaryByBenGroup().get("10").entrySet()) {
			for (com.trinet.ambis.rest.controllers.dto.outputs.EmployeeCostSummary summary : entry.getValue()) {
				if (summary.isPageBreak()) {
					foundPageBreak = true;
				}
			}
		}
		assertTrue(foundPageBreak);
	}

	@Test(expected = Exception.class)
	public void getCostSummaryDataException() {

		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);

		OutputRequest request = new OutputRequest();
		request.setTnStrategyId("45673");

		List<String> benTypes = Arrays.asList(new String("10"),new String("11"),new String("14"));
		request.setBenefitTypes(benTypes);

		OutputData outputData = new OutputData();
		outputData.setTrinetStrategyIsBenTypeOffered(getOutputData().get());
		Optional<List<StrategyGroupEmployeePlanRateData>> summaryListOptional = prepareSummaryList();

		ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
		ArgumentCaptor<List<Long>> strategyListCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<List<String>> planTypesCaptor = ArgumentCaptor.forClass(List.class);

		// Mock
		when(CompanyServiceHelper.isTibProspect(Mockito.any(Company.class))).thenReturn(true);
		when(strategyDataDao.getOmsStrategyGroupPlanCostByPlanType(strategyListCaptor.capture(), planTypesCaptor.capture()))
				.thenReturn(prepareSummaryListOms());
		when(hrisPlanAttributeService.getPlanAttributesByBenefitType(any(), anyString())).thenAnswer(invocation -> {
			throw new InterruptedException();
		});

		try {
			employeeCostSummaryService.getCostSummaryData(outputData, company, request);
		} finally {
			Thread.interrupted();
		}
	}

	@Test
	public void getOmsStrategyCostResponsePrependTrueTest() {
		// given
		long strategyId = 123456;

		//when
		when(CompanyServiceHelper.isTibProspect(Mockito.any(Company.class))).thenReturn(true);
		when(strategyDataDao.getOmsStrategyGroupPlanCostByPlanType(Arrays.asList(strategyId), Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE)))
				.thenReturn(prepareSummaryListOms());
		when(hrisPlanAttributeService.getPlanAttributesByBenefitType(Set.of("000SR7"), BSSApplicationConstants.MEDICAL)).thenReturn(CompletableFuture.completedFuture(
				List.of(
						BenefitPlanCompare.builder()
								.planId("000SR7")
								.carrierId(1)
								.carrier("Carrier 1")
								.name("MetLife Enhanced OMS")
								.build())));


		// then
		Optional<List<StrategyGroupEmployeePlanRateData>> actualResult = employeeCostSummaryService.getOmsStrategyCostResponse(Arrays.asList(strategyId),
				Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE), true);

		verify(strategyDataDao, times(1)).getOmsStrategyGroupPlanCostByPlanType(Arrays.asList(strategyId), Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		verify(hrisPlanAttributeService, times(1)).getPlanAttributesByBenefitType(Set.of("000SR7"), BSSApplicationConstants.MEDICAL);
		assertTrue(actualResult.isPresent());
		assertEquals("Carrier 1 MetLife Enhanced OMS", actualResult.get().get(0).getPlanName());
	}

	@Test
	public void getOmsStrategyCostResponsePrependFalseTest() {
		// given
		long strategyId = 123456;

		//when
		when(CompanyServiceHelper.isTibProspect(Mockito.any(Company.class))).thenReturn(true);
		when(strategyDataDao.getOmsStrategyGroupPlanCostByPlanType(Arrays.asList(strategyId), Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE)))
				.thenReturn(prepareSummaryListOms());
		when(hrisPlanAttributeService.getPlanAttributesByBenefitType(Set.of("000SR7"), BSSApplicationConstants.MEDICAL)).thenReturn(CompletableFuture.completedFuture(
				List.of(
						BenefitPlanCompare.builder()
								.planId("000SR7")
								.carrierId(1)
								.carrier("Carrier 1")
								.name("MetLife Enhanced OMS")
								.build())));


		// then
		Optional<List<StrategyGroupEmployeePlanRateData>> actualResult = employeeCostSummaryService.getOmsStrategyCostResponse(Arrays.asList(strategyId),
				Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE), false);

		verify(strategyDataDao, times(1)).getOmsStrategyGroupPlanCostByPlanType(Arrays.asList(strategyId), Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
		verify(hrisPlanAttributeService, times(1)).getPlanAttributesByBenefitType(Set.of("000SR7"), BSSApplicationConstants.MEDICAL);
		assertTrue(actualResult.isPresent());
		assertEquals("MetLife Enhanced OMS", actualResult.get().get(0).getPlanName());
	}

	private Supplier<Map<String, Boolean>> getOutputData(){
		return () ->  Map.of(ProspectConstants.MEDICAL_PLAN_TYPE_DESC,Boolean.TRUE,
				ProspectConstants.DENTAL_PLAN_TYPE_DESC,Boolean.TRUE,
				ProspectConstants.VISION_PLAN_TYPE_DESC,Boolean.TRUE,
				ProspectConstants.LIFE_ADD_PLAN_TYPE_DESC,Boolean.TRUE,
				ProspectConstants.DISABILITY_PLAN_TYPE_DESC,Boolean.TRUE);
	}

	private void assertResults(BenefitTypeEmployeeCostSummary costSummaryMap) {
		assertEquals("Adam", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getEmployee().getFirstName());
		assertEquals("C", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getEmployee().getLastName());
		assertEquals("SC", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getEmployee().getState());
		assertEquals("2", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getEmployee().getCoverageCode());
		assertEquals("BENPROG3", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getEmployee().getGroup());
		assertEquals(BigDecimal.valueOf(120.00).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getCurrentPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(10).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getCurrentPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getCurrentPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(130).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getCurrentPlan().getTotal());
		assertEquals(BigDecimal.valueOf(110.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getTriNetPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(25.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getTriNetPlan().getEeAmount());
		assertEquals("MetLife Enhanced OMS", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getTriNetPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(136.0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getTriNetPlan().getTotal());
		assertEquals(6, costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG3").get(0).getCostDiff());

		assertEquals("Jack", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getEmployee().getFirstName());
		assertEquals("H", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getEmployee().getLastName());
		assertEquals("NC", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getEmployee().getState());
		assertEquals("1", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getEmployee().getCoverageCode());
		assertEquals("BENPROG2", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getEmployee().getGroup());
		assertEquals(BigDecimal.valueOf(100.25), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getCurrentPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(13.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getCurrentPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getCurrentPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(113.75).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getCurrentPlan().getTotal());
		assertEquals(BigDecimal.valueOf(120.25).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getTriNetPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(20.25).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getTriNetPlan().getEeAmount());
		assertEquals("MetLife Enhanced OMS", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getTriNetPlan().getPlanName());
		assertEquals(new BigDecimal(140.5).setScale(2).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getTriNetPlan().getTotal());
		assertEquals(26, costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG2").get(0).getCostDiff());

		assertEquals("Pen", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG0").get(0).getEmployee().getFirstName());
		assertEquals("C", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG0").get(0).getEmployee().getLastName());
		assertEquals("CA", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG0").get(0).getEmployee().getState());
		assertEquals("W", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG0").get(0).getEmployee().getCoverageCode());

		assertEquals("Pen", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getEmployee().getFirstName());
		assertEquals("C", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getEmployee().getLastName());
		assertEquals("CA", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getEmployee().getState());
		assertEquals("C", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getEmployee().getCoverageCode());
		assertEquals("BENPROG1", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getEmployee().getGroup());
		assertEquals(BigDecimal.valueOf(0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getCurrentPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getCurrentPlan().getEeAmount());
		assertEquals("--", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getCurrentPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getCurrentPlan().getTotal());
		assertEquals(BigDecimal.valueOf(120.25).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getTriNetPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(18.25).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getTriNetPlan().getEeAmount());
		assertEquals("MetLife Enhanced OMS", costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getTriNetPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(138.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getTriNetPlan().getTotal());
		assertEquals(138, costSummaryMap.getEmplCostSummaryByBenGroup().get("10").get("BENPROG1").get(0).getCostDiff());

		assertEquals("Ben", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getEmployee().getFirstName());
		assertEquals("J", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getEmployee().getLastName());
		assertEquals("VA", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getEmployee().getState());
		assertEquals("1", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getEmployee().getCoverageCode());
		assertEquals("BENPROG1", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getEmployee().getGroup());
		assertEquals(BigDecimal.valueOf(100.25), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getCurrentPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(13.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getCurrentPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getCurrentPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(113.75).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getCurrentPlan().getTotal());
		assertEquals(BigDecimal.valueOf(0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getTriNetPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(25.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getTriNetPlan().getEeAmount());
		assertEquals("MetLife Enhanced Voluntary", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getTriNetPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(25.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getTriNetPlan().getTotal());
		assertEquals(-88, costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(0).getCostDiff());

		assertEquals("Zen", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getEmployee().getFirstName());
		assertEquals("S", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getEmployee().getLastName());
		assertEquals("DN", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getEmployee().getState());
		assertEquals("C", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getEmployee().getCoverageCode());
		assertEquals("BENPROG1", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getEmployee().getGroup());
		assertEquals(BigDecimal.valueOf(120.25), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getCurrentPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(18.25), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getCurrentPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getCurrentPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(138.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getCurrentPlan().getTotal());
		assertEquals(BigDecimal.valueOf(130.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getTriNetPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(20.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getTriNetPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getTriNetPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(151.0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getTriNetPlan().getTotal());
		assertEquals(12, costSummaryMap.getEmplCostSummaryByBenGroup().get("11").get("BENPROG1").get(1).getCostDiff());

		assertEquals("Calvin", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getEmployee().getFirstName());
		assertEquals("K", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getEmployee().getLastName());
		assertEquals("NC", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getEmployee().getState());
		assertEquals("1", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getEmployee().getCoverageCode());
		assertEquals("BENPROG2", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getEmployee().getGroup());
		assertEquals(BigDecimal.valueOf(100.25), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getCurrentPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(18.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getCurrentPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getCurrentPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(118.75), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getCurrentPlan().getTotal());
		assertEquals(BigDecimal.valueOf(0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getTriNetPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(27.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getTriNetPlan().getEeAmount());
		assertEquals("MetLife Enhanced Voluntary", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getTriNetPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(27.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getTriNetPlan().getTotal());
		assertEquals(-91, costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(0).getCostDiff());

		assertEquals("Sam", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getEmployee().getFirstName());
		assertEquals("B", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getEmployee().getLastName());
		assertEquals("NC", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getEmployee().getState());
		assertEquals("C", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getEmployee().getCoverageCode());
		assertEquals("BENPROG2", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getEmployee().getGroup());
		assertEquals(BigDecimal.valueOf(120.25), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getCurrentPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(10.25), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getCurrentPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getCurrentPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(130.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getCurrentPlan().getTotal());
		assertEquals(BigDecimal.valueOf(120.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getTriNetPlan().getErAmount());
		assertEquals(BigDecimal.valueOf(21.50).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getTriNetPlan().getEeAmount());
		assertEquals("MetLife Enhanced", costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getTriNetPlan().getPlanName());
		assertEquals(BigDecimal.valueOf(142.0).setScale(2), costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getTriNetPlan().getTotal());
		assertEquals(11, costSummaryMap.getEmplCostSummaryByBenGroup().get("14").get("BENPROG2").get(1).getCostDiff());

	}

	private Optional<List<StrategyGroupEmployeePlanRateData>> prepareSummaryList(){
		List<StrategyGroupEmployeePlanRateData> summaryList = new ArrayList<>();
		StrategyGroupEmployeePlanRateData planRateData = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567431").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4376).longValue()).groupName("BENPROG1")
				.planType("10").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("C").eeRate(new BigDecimal(18.25))
				.erRate(new BigDecimal(120.25))
				.build();
		summaryList.add(planRateData);

		StrategyGroupEmployeePlanRateData planRateData1 = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567432").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4377).longValue()).groupName("BENPROG2")
				.planType("10").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("1").eeRate(new BigDecimal(20.25))
				.erRate(new BigDecimal(120.25))
				.build();
		summaryList.add(planRateData1);

		StrategyGroupEmployeePlanRateData planRateData2 = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567433").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4378).longValue()).groupName("BENPROG3")
				.planType("10").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(25.50))
				.erRate(new BigDecimal(110.50))
				.build();
		summaryList.add(planRateData2);

		StrategyGroupEmployeePlanRateData planRateDataLong = StrategyGroupEmployeePlanRateData.builder()
				.emplId("99999999").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4376).longValue()).groupName("BENPROG1")
				.planType("10").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("C").eeRate(new BigDecimal(18.25))
				.erRate(new BigDecimal(120.25))
				.build();
		summaryList.add(planRateDataLong);

		StrategyGroupEmployeePlanRateData planRateData3 = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567500").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4378).longValue()).groupName("BENPROG1")
				.planType("11").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(20.50))
				.erRate(new BigDecimal(130.50))
				.build();
		summaryList.add(planRateData3);

		StrategyGroupEmployeePlanRateData planRateData4 = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567501").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4378).longValue()).groupName("BENPROG1")
				.planType("1D").benefitPlan("000SR8").planName("MetLife Enhanced Voluntary")
				.coverageCode("2").eeRate(new BigDecimal(25.50))
				.erRate(new BigDecimal(0))
				.build();
		summaryList.add(planRateData4);

		StrategyGroupEmployeePlanRateData planRateData5 = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567502").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4378).longValue()).groupName("BENPROG2")
				.planType("14").benefitPlan("000SR7").planName("MetLife Enhanced")
				.coverageCode("2").eeRate(new BigDecimal(21.50))
				.erRate(new BigDecimal(120.50))
				.build();
		summaryList.add(planRateData5);

		StrategyGroupEmployeePlanRateData planRateData6 = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567503").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4378).longValue()).groupName("BENPROG2")
				.planType("1V").benefitPlan("000SR8").planName("MetLife Enhanced Voluntary")
				.coverageCode("2").eeRate(new BigDecimal(27.50))
				.erRate(new BigDecimal(0))
				.build();
		summaryList.add(planRateData6);

		return Optional.ofNullable(summaryList);
	}


	private Optional<List<StrategyGroupEmployeePlanRateData>> prepareSummaryListOms() {
		List<StrategyGroupEmployeePlanRateData> summaryListOms = new ArrayList<>();
		StrategyGroupEmployeePlanRateData planRateDataOms = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567431").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4376).longValue()).groupName("BENPROG1")
				.planType("10").benefitPlan("000SR7").planName(null)
				.coverageCode("C").eeRate(new BigDecimal(18.25))
				.erRate(new BigDecimal(120.25))
				.build();
		summaryListOms.add(planRateDataOms);

		planRateDataOms = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567432").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4377).longValue()).groupName("BENPROG2")
				.planType("10").benefitPlan("000SR7").planName(null)
				.coverageCode("1").eeRate(new BigDecimal(20.25))
				.erRate(new BigDecimal(120.25))
				.build();
		summaryListOms.add(planRateDataOms);

		planRateDataOms = StrategyGroupEmployeePlanRateData.builder()
				.emplId("23567433").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4378).longValue()).groupName("BENPROG3")
				.planType("10").benefitPlan("000SR7").planName(null)
				.coverageCode("2").eeRate(new BigDecimal(25.50))
				.erRate(new BigDecimal(110.50))
				.build();
		summaryListOms.add(planRateDataOms);

		planRateDataOms = StrategyGroupEmployeePlanRateData.builder()
				.emplId("99999999").strategyId(new BigDecimal(197547).longValue())
				.groupId(new BigDecimal(4376).longValue()).groupName("BENPROG1")
				.planType("10").benefitPlan("000SR7").planName(null)
				.coverageCode("C").eeRate(new BigDecimal(18.25))
				.erRate(new BigDecimal(120.25))
				.build();
		summaryListOms.add(planRateDataOms);

		return Optional.ofNullable(summaryListOms);
	}

	private List<EmployeeCostRes> prepareEmployeeCostResList() {
		List<EmployeeCostRes.EmployeePlanContribution> employeePlanContribution = new ArrayList();

		EmployeeCostRes employeeCostRes = new EmployeeCostRes();
		employeeCostRes.setBenefitTypeCode("10");
		EmployeeCostRes.EmployeePlanContribution contri0 = new EmployeeCostRes.EmployeePlanContribution();
		contri0.setEmployeeId("23567431");
		contri0.setFirstName("Pen");
		contri0.setLastName("Clock");
		contri0.setState("CA");
		contri0.setCovgLevel("W");
		contri0.setGroupId(4376);
		contri0.setGroupName("BENPROG0");

		EmployeeCostRes.PlanContribution planContribution0 = new EmployeeCostRes.PlanContribution();
		planContribution0.setBenefitPlanId(0);
		planContribution0.setBenefitPlanName(null);
		planContribution0.setEeCost(new BigDecimal(0));
		planContribution0.setErCost(new BigDecimal(0));
		planContribution0.setTotalCost(new BigDecimal(0));
		contri0.setPlanContribution(planContribution0);
		employeePlanContribution.add(contri0);

		EmployeeCostRes.EmployeePlanContribution contri = new EmployeeCostRes.EmployeePlanContribution();
		contri.setEmployeeId("23567431");
		contri.setFirstName("Pen");
		contri.setLastName("Clock");
		contri.setState("CA");
		contri.setCovgLevel("C");
		contri.setGroupId(4376);
		contri.setGroupName("BENPROG1");

		EmployeeCostRes.PlanContribution planContribution = new EmployeeCostRes.PlanContribution();
		planContribution.setBenefitPlanId(0);
		planContribution.setBenefitPlanName(null);
		planContribution.setEeCost(new BigDecimal(16.25));
		planContribution.setErCost(new BigDecimal(120.25));
		planContribution.setTotalCost(new BigDecimal(136.50));
		contri.setPlanContribution(planContribution);
		employeePlanContribution.add(contri);

		EmployeeCostRes.EmployeePlanContribution contri1 = new EmployeeCostRes.EmployeePlanContribution();
		contri1.setEmployeeId("23567432");
		contri1.setFirstName("Jack");
		contri1.setLastName("Harrow");
		contri1.setState("NC");
		contri1.setCovgLevel("1");
		contri1.setGroupId(4377);
		contri1.setGroupName("BENPROG2");

		EmployeeCostRes.PlanContribution planContribution1 = new EmployeeCostRes.PlanContribution();
		planContribution1.setBenefitPlanId(64578);
		planContribution1.setBenefitPlanName("MetLife Enhanced");
		planContribution1.setEeCost(new BigDecimal(13.50));
		planContribution1.setErCost(new BigDecimal(100.25));
		planContribution1.setTotalCost(new BigDecimal(113.75));
		contri1.setPlanContribution(planContribution1);

		employeePlanContribution.add(contri1);

		EmployeeCostRes.EmployeePlanContribution contri2 = new EmployeeCostRes.EmployeePlanContribution();
		contri2.setEmployeeId("23567433");
		contri2.setFirstName("Adam");
		contri2.setLastName("Cool");
		contri2.setState("SC");
		contri2.setCovgLevel("2");
		contri2.setGroupId(4378);
		contri2.setGroupName("BENPROG3");

		EmployeeCostRes.PlanContribution planContribution2 = new EmployeeCostRes.PlanContribution();
		planContribution2.setBenefitPlanId(64578);
		planContribution2.setBenefitPlanName("MetLife Enhanced");
		planContribution2.setEeCost(new BigDecimal(10.00));
		planContribution2.setErCost(new BigDecimal(120.00));
		planContribution2.setTotalCost(new BigDecimal(130.00));
		contri2.setPlanContribution(planContribution2);

		employeePlanContribution.add(contri2);

		EmployeeCostRes.EmployeePlanContribution contriLong = new EmployeeCostRes.EmployeePlanContribution();
		contriLong.setEmployeeId("99999999");
		contriLong.setFirstName("VeryLongFirstNameThatExceedsTwentyCharacters");
		contriLong.setLastName("Long");
		contriLong.setState("CA");
		contriLong.setCovgLevel("C");
		contriLong.setGroupId(4376);
		contriLong.setGroupName("BENPROG1");

		EmployeeCostRes.PlanContribution planContributionLong = new EmployeeCostRes.PlanContribution();
		planContributionLong.setBenefitPlanId(0);
		planContributionLong.setBenefitPlanName(null);
		planContributionLong.setEeCost(new BigDecimal(16.25));
		planContributionLong.setErCost(new BigDecimal(120.25));
		planContributionLong.setTotalCost(new BigDecimal(136.50));
		contriLong.setPlanContribution(planContributionLong);
		employeePlanContribution.add(contriLong);

		for(int i = 0; i < 50; i++) {
			EmployeeCostRes.EmployeePlanContribution p = new EmployeeCostRes.EmployeePlanContribution();
			p.setEmployeeId("1000" + i);
			p.setFirstName("First" + i);
			p.setLastName("Last" + i);
			p.setState("CA");
			p.setCovgLevel("C");
			p.setGroupId(4399);
			p.setGroupName("BENPROG9");
			EmployeeCostRes.PlanContribution pc = new EmployeeCostRes.PlanContribution();
			pc.setBenefitPlanId(0);
			pc.setBenefitPlanName(null);
			pc.setEeCost(new BigDecimal(10));
			pc.setErCost(new BigDecimal(10));
			pc.setTotalCost(new BigDecimal(20));
			p.setPlanContribution(pc);
			employeePlanContribution.add(p);
		}

		employeeCostRes.setEmployeePlanContribution(employeePlanContribution);

		EmployeeCostRes employeeCostRes1 = new EmployeeCostRes();
		List<EmployeeCostRes.EmployeePlanContribution> employeePlanContribution1 = new ArrayList();
		employeeCostRes1.setBenefitTypeCode("11");
		EmployeeCostRes.EmployeePlanContribution contri3 = new EmployeeCostRes.EmployeePlanContribution();
		contri3.setEmployeeId("23567500");
		contri3.setFirstName("Zen");
		contri3.setLastName("Souf");
		contri3.setState("DN");
		contri3.setCovgLevel("C");
		contri3.setGroupId(4376);
		contri3.setGroupName("BENPROG1");

		EmployeeCostRes.PlanContribution planContribution3 = new EmployeeCostRes.PlanContribution();
		planContribution3.setBenefitPlanId(64578);
		planContribution3.setBenefitPlanName("MetLife Enhanced");
		planContribution3.setEeCost(new BigDecimal(18.25));
		planContribution3.setErCost(new BigDecimal(120.25));
		planContribution3.setTotalCost(new BigDecimal(138.50));
		contri3.setPlanContribution(planContribution3);
		employeePlanContribution1.add(contri3);

		EmployeeCostRes.EmployeePlanContribution contri4 = new EmployeeCostRes.EmployeePlanContribution();
		contri4.setEmployeeId("23567501");
		contri4.setFirstName("Ben");
		contri4.setLastName("Jarrow");
		contri4.setState("VA");
		contri4.setCovgLevel("1");
		contri4.setGroupId(4377);
		contri4.setGroupName("BENPROG2");

		EmployeeCostRes.PlanContribution planContribution4 = new EmployeeCostRes.PlanContribution();
		planContribution4.setBenefitPlanId(64578);
		planContribution4.setBenefitPlanName("MetLife Enhanced");
		planContribution4.setEeCost(new BigDecimal(13.50));
		planContribution4.setErCost(new BigDecimal(100.25));
		planContribution4.setTotalCost(new BigDecimal(113.75));
		contri4.setPlanContribution(planContribution4);

		employeePlanContribution1.add(contri4);
		employeeCostRes1.setEmployeePlanContribution(employeePlanContribution1);

		EmployeeCostRes employeeCostRes2 = new EmployeeCostRes();
		List<EmployeeCostRes.EmployeePlanContribution> employeePlanContribution2 = new ArrayList();
		employeeCostRes2.setBenefitTypeCode("14");
		EmployeeCostRes.EmployeePlanContribution contri5 = new EmployeeCostRes.EmployeePlanContribution();
		contri5.setEmployeeId("23567502");
		contri5.setFirstName("Sam");
		contri5.setLastName("Ben");
		contri5.setState("NC");
		contri5.setCovgLevel("C");
		contri5.setGroupId(4376);
		contri5.setGroupName("BENPROG1");

		EmployeeCostRes.PlanContribution planContribution5 = new EmployeeCostRes.PlanContribution();
		planContribution5.setBenefitPlanId(64578);
		planContribution5.setBenefitPlanName("MetLife Enhanced");
		planContribution5.setEeCost(new BigDecimal(10.25));
		planContribution5.setErCost(new BigDecimal(120.25));
		planContribution5.setTotalCost(new BigDecimal(130.50));
		contri5.setPlanContribution(planContribution5);
		employeePlanContribution2.add(contri5);

		EmployeeCostRes.EmployeePlanContribution contri6 = new EmployeeCostRes.EmployeePlanContribution();
		contri6.setEmployeeId("23567503");
		contri6.setFirstName("Calvin");
		contri6.setLastName("Klein");
		contri6.setState("NC");
		contri6.setCovgLevel("1");
		contri6.setGroupId(4377);
		contri6.setGroupName("BENPROG2");

		EmployeeCostRes.PlanContribution planContribution6 = new EmployeeCostRes.PlanContribution();
		planContribution6.setBenefitPlanId(64578);
		planContribution6.setBenefitPlanName("MetLife Enhanced");
		planContribution6.setEeCost(new BigDecimal(18.50));
		planContribution6.setErCost(new BigDecimal(100.25));
		planContribution6.setTotalCost(new BigDecimal(118.75));
		contri6.setPlanContribution(planContribution6);

		employeePlanContribution2.add(contri6);
		employeeCostRes2.setEmployeePlanContribution(employeePlanContribution2);

		List<EmployeeCostRes> employeeCostResList = new ArrayList();
		employeeCostResList.add(employeeCostRes);
		employeeCostResList.add(employeeCostRes1);
		employeeCostResList.add(employeeCostRes2);

		return employeeCostResList;
	}

}
