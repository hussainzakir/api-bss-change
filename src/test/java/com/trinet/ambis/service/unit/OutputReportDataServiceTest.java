package com.trinet.ambis.service.unit;

import static com.trinet.ambis.enums.OmsOfferingEnum.OMB_TLD;
import static com.trinet.ambis.enums.OmsOfferingEnum.OM_OD_OV_TLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.trinet.ambis.util.AppRulesAndConfigsUtils;
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

import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.impl.PersonDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.BenefitTypeEmployeeCostSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.FundingSummary;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputData;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAppendixFilters;
import com.trinet.ambis.rest.controllers.dto.outputs.TitlePageData;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.service.impl.outputs.OutputReportDataServiceImpl;
import com.trinet.ambis.service.impl.outputs.OutputRequestBuilderImpl;
import com.trinet.ambis.service.outputs.BenefitCostSummaryService;
import com.trinet.ambis.service.outputs.EmployeeCostSummaryService;
import com.trinet.ambis.service.outputs.FundingSummaryService;
import com.trinet.ambis.service.outputs.OutputService;
import com.trinet.ambis.service.outputs.PlanAppendixService;
import com.trinet.ambis.service.outputs.PlanComparisonService;
import com.trinet.ambis.service.outputs.TitlePageService;
import com.trinet.ambis.service.prospect.ProspectPlanService;
import com.trinet.ambis.service.prospect.dto.BenTypeOfferRes;
import com.trinet.ambis.service.prospect.dto.BenefitPlansRes;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RestApiClient;


/**
 * @author rterle
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class OutputReportDataServiceTest extends ServiceUnitTest {

	@InjectMocks
	OutputReportDataServiceImpl outputReportDataService;

	@Mock
	CompanyService companyService;

	@Mock
	TitlePageService titlePageService;

	@Mock
	PlanAppendixService planAppendixService;

	@Mock
	PlanComparisonService planComparisonService;

	@Mock
	EmployeeCostSummaryService emplCostSummaryService;

	@Mock
	BenefitCostSummaryService benefitCostSummaryService;

	@Mock
	FundingSummaryService fundingSummaryService;

	@Mock
	OutputRequestBuilderImpl outputRequestBuilderImpl;

	@Mock
	private StrategyService strategyService;

	@Mock
	RestApiClient restApiClient;
	
	@Mock
	OutputRequestBuilderImpl outputRequestBuiler;

	@Mock
	PersonDaoImpl personDao;
	
	@Mock
	ProspectPlanService prospectPlanService;

	@Mock
	OutputService prospectoutputsService;
	
	@Mock
	HttpServletRequest httpRequest;

	private static final String EMPLID = "0000000123456";
	private static final String COMPANYCODE = "10PZ";
	private static final String EXCHANGE = "TRINET_III";
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;
	private MockedStatic<AppRulesAndConfigsUtils> appRulesAndConfigsUtilsMockedStatic;



	@Before
    public void setUp() {

		bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);
		appRulesAndConfigsUtilsMockedStatic = Mockito.mockStatic(AppRulesAndConfigsUtils.class);
    }

    @After
    public void tearDown() {
        bssSecurityUtilsMockedStatic.close();
		appRulesAndConfigsUtilsMockedStatic.close();
    }

	@Test
	public void getDataTest() {
		//Given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList("med","den","vis"));
		List<String> reports = new ArrayList<>();
		reports.add("ECC");
		reports.add("PCC");
		reports.add("APX");
		prospectRequest.setTemplateNames(reports);
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);
		company.setName("Test Company");
		company.setCode("1234");
		company.setOmsOffering(OMB_TLD.name());
		BenefitTypeEmployeeCostSummary employeeLvlCostSummary = new BenefitTypeEmployeeCostSummary(new HashMap<>());
		String strategyId = "123456";
		String prospectId = "7890";
		prospectRequest.setTnStrategyId(strategyId);
		prospectRequest.setPlanAppendixFilters(new PlanAppendixFilters());

		BenTypeOfferRes benTypeOfferRes = new BenTypeOfferRes();
		benTypeOfferRes.setStrategyId(Long.parseLong("123456"));
		Set<String> offerTypes = new HashSet<>();
		offerTypes.add("10");
		offerTypes.add("11");
		benTypeOfferRes.setOfferTypes(offerTypes);

		//When
		when(prospectPlanService.getBenefitPlansBy(company.getCode())).thenReturn(createBenefitPlansResList());
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(EMPLID);
		when(titlePageService.getTitlePageData(Mockito.any(OutputRequest.class), Mockito.any(Company.class))).thenReturn(new TitlePageData());
		when(planComparisonService.getPlanComparisonData(company, prospectRequest, httpRequest))
				.thenReturn(CompletableFuture.completedFuture(new HashMap<>()));
		when(emplCostSummaryService.getCostSummaryData(any(), eq(company), eq(prospectRequest))).thenReturn(CompletableFuture.completedFuture(employeeLvlCostSummary));
		when(fundingSummaryService.getFundingSummaryData(company, prospectRequest)).thenReturn(CompletableFuture.completedFuture(new FundingSummary()));
		when(strategyService.getAllStrategies(company.getId())).thenReturn(new ArrayList<>());
		when(prospectoutputsService.getPlanTypeOfferedDetails(List.of(Long.valueOf(prospectRequest.getTnStrategyId())), Arrays.asList("10","11","14","1D","1V","23","30","31"))).thenReturn(List.of(benTypeOfferRes));
		when(strategyService.getPrimaryCarrierName(company, strategyId)).thenReturn("Aetna");
		when(planComparisonService.getAdditionalBenfitsCompareData(company, prospectRequest))
		.thenReturn(CompletableFuture.completedFuture(new HashMap<>()));
       when(AppRulesAndConfigsUtils.isEmployeeComparePageBreakEnabled()).thenReturn(false);
	   when(AppRulesAndConfigsUtils.isLifeAndDiPageBreakEnabled()).thenReturn(false);

		OutputData actualResults = outputReportDataService.getData(prospectRequest, company, httpRequest);
	
		//Assert
		assertNotNull(actualResults);
		assertEquals("Test Company", actualResults.getCompanyName());
		assertEquals("Aetna", actualResults.getPrimaryCarrierName());
		assertFalse(actualResults.isTibCompany());
		assertEquals(true, actualResults.getCurrStrategyIsBenTypeOffered().get("10"));
		assertEquals(true, actualResults.getCurrStrategyIsBenTypeOffered().get("11"));
		assertEquals(false, actualResults.getCurrStrategyIsBenTypeOffered().get("14"));
		assertEquals(true, actualResults.getTrinetStrategyIsBenTypeOffered().get("Medical"));
		assertEquals(true, actualResults.getTrinetStrategyIsBenTypeOffered().get("Dental"));
		assertEquals(false, actualResults.getTrinetStrategyIsBenTypeOffered().get("Vision"));
		assertEquals(false, actualResults.getTrinetStrategyIsBenTypeOffered().get("Life / AD&D"));
		assertEquals(false, actualResults.getTrinetStrategyIsBenTypeOffered().get("Disability"));
        assertFalse(actualResults.isLifeAndDiPageBreakEnabled());
        assertFalse(actualResults.isEmployeeComparePageBreakEnabled());

		assertEquals(Arrays.asList("med","den","vis"), actualResults.getIncludedPlanTypes());


	}

	@Test
	public void getDataTest1() {
		//Given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(Arrays.asList("med","den","vis"));
		List<String> reports = new ArrayList<>();
		reports.add("ECC");
		reports.add("PCC");
		reports.add("APX");
		prospectRequest.setTemplateNames(reports);
		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);
		company.setName("Test Company");
		company.setCode("1234");
		company.setOmsOffering(OM_OD_OV_TLD.name());
		BenefitTypeEmployeeCostSummary employeeLvlCostSummary = new BenefitTypeEmployeeCostSummary(new HashMap<>());
		String strategyId = "123456";
		String prospectId = "7890";
		prospectRequest.setTnStrategyId(strategyId);
		prospectRequest.setPlanAppendixFilters(new PlanAppendixFilters());

		BenTypeOfferRes benTypeOfferRes = new BenTypeOfferRes();
		benTypeOfferRes.setStrategyId(Long.parseLong("123456"));
		Set<String> offerTypes = new HashSet<>();
		offerTypes.add("10");
		offerTypes.add("11");
		benTypeOfferRes.setOfferTypes(offerTypes);

		//When
		when(prospectPlanService.getBenefitPlansBy(company.getCode())).thenReturn(createBenefitPlansResList());
		when(BSSSecurityUtils.getAuthenticatedPersonId()).thenReturn(EMPLID);
		when(titlePageService.getTitlePageData(Mockito.any(OutputRequest.class), Mockito.any(Company.class))).thenReturn(new TitlePageData());
		when(planComparisonService.getPlanComparisonData(company, prospectRequest, httpRequest))
				.thenReturn(CompletableFuture.completedFuture(new HashMap<>()));
		when(emplCostSummaryService.getCostSummaryData(any(), eq(company), eq(prospectRequest))).thenReturn(CompletableFuture.completedFuture(employeeLvlCostSummary));
		when(fundingSummaryService.getFundingSummaryData(company, prospectRequest)).thenReturn(CompletableFuture.completedFuture(new FundingSummary()));
		when(strategyService.getAllStrategies(company.getId())).thenReturn(new ArrayList<>());
		when(prospectoutputsService.getPlanTypeOfferedDetails(List.of(Long.valueOf(prospectRequest.getTnStrategyId())), Arrays.asList("10","11","14","1D","1V","23","30","31"))).thenReturn(List.of(benTypeOfferRes));
		when(planComparisonService.getAdditionalBenfitsCompareData(company, prospectRequest))
				.thenReturn(CompletableFuture.completedFuture(new HashMap<>()));
		when(AppRulesAndConfigsUtils.isEmployeeComparePageBreakEnabled()).thenReturn(false);
		when(AppRulesAndConfigsUtils.isLifeAndDiPageBreakEnabled()).thenReturn(false);

		OutputData actualResults = outputReportDataService.getData(prospectRequest, company, httpRequest);

		//Assert
		assertNotNull(actualResults);
		assertTrue(actualResults.isTibCompany());
        assertEquals(Arrays.asList("med","den","vis"), actualResults.getIncludedPlanTypes());


	}
	
	@Test
	public void getDataTest_OnlyApxWithBssPhase2Enabled() {
		//Given
		OutputRequest prospectRequest = new OutputRequest();
		prospectRequest.setBenefitTypes(new ArrayList<>(Arrays.asList("den","med")));
		List<String> reports = new ArrayList<>();
		reports.add("APX");
		prospectRequest.setTemplateNames(reports);

		Company company = new Company();
		Realm realm = new Realm();
		realm.setBenExchange(BenExchngEnums.TRINET_OMS.getBenExchng());
		company.setRealm(realm);
		company.setName("Test Company");
		company.setCode("1234");
		company.setOmsOffering(OMB_TLD.name());

		String strategyId = "123456";
		prospectRequest.setTnStrategyId(strategyId);
		prospectRequest.setPlanAppendixFilters(new PlanAppendixFilters());

		BenTypeOfferRes benTypeOfferRes = new BenTypeOfferRes();
		benTypeOfferRes.setStrategyId(Long.parseLong("123456"));
		Set<String> offerTypes = new HashSet<>();
		offerTypes.add("10");
		offerTypes.add("11");
		benTypeOfferRes.setOfferTypes(offerTypes);

		//When
		when(prospectPlanService.getBenefitPlansBy(company.getCode())).thenReturn(createBenefitPlansResList());
		when(titlePageService.getTitlePageData(Mockito.any(OutputRequest.class), Mockito.any(Company.class))).thenReturn(new TitlePageData());
		when(strategyService.getAllStrategies(company.getId())).thenReturn(new ArrayList<>());
		when(prospectoutputsService.getPlanTypeOfferedDetails(Mockito.anyList(), Mockito.anyList())).thenReturn(List.of(benTypeOfferRes));

		when(AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()).thenReturn(true);

		when(planAppendixService.getPlanAppendixData(eq(company), eq(prospectRequest), eq(httpRequest), any()))
				.thenReturn(new HashMap<>());
		when(strategyService.getPrimaryCarrierName(eq(company), eq(strategyId))).thenReturn("Aetna");

		OutputData actualResults = outputReportDataService.getData(prospectRequest, company, httpRequest);

		//Assert
		assertNotNull(actualResults);
		assertTrue(actualResults.isGeneratePlanAppendixOnly());
		Mockito.verify(httpRequest).setAttribute(eq(BSSApplicationConstants.PLAN_APPENDIX_FIRST_BEN_TYPE), eq("10"));
		assertEquals("10", actualResults.getPlanappendixfirstBentype());
	}

	private List<BenefitPlansRes> createBenefitPlansResList() {
		BenefitPlansRes medBenefitPlansRes = new BenefitPlansRes();
		medBenefitPlansRes.setBenefitType("Medical");
		medBenefitPlansRes.setBenefitTypeCode("10");
		medBenefitPlansRes.setBenefitPlans(null);
		BenefitPlansRes denBenefitPlansRes = new BenefitPlansRes();
		denBenefitPlansRes.setBenefitType("Dental");
		denBenefitPlansRes.setBenefitTypeCode("11");
		denBenefitPlansRes.setBenefitPlans(null);
		return List.of(medBenefitPlansRes, denBenefitPlansRes);
	}
		
}