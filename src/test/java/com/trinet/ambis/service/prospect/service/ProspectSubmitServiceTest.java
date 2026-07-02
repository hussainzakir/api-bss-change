package com.trinet.ambis.service.prospect.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.rest.controllers.dto.prospect.ProspectBenefitsSummaryTotalsResponse;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProspectStrategyIntegrationService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.outputs.OutputService;
import com.trinet.ambis.service.prospect.SfdcClientService;
import com.trinet.ambis.service.prospect.exception.ProspectApiCallException;
import com.trinet.ambis.service.prospect.impl.ProspectSubmitServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.test.config.TestHelper;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.DateUtils;
import com.trinet.ambis.util.FileUtils;

@RunWith(MockitoJUnitRunner.class)
public class ProspectSubmitServiceTest extends ServiceUnitTest {

	@InjectMocks
	private ProspectSubmitServiceImpl prospectSubmitService;

	@Mock
	private OutputService outputService;

	@Mock
	private CompanyService companyService;

	@Mock
	StrategyService strategyService;

	@Mock
	SubmitStatusService submitStatusService;

	@Captor
	ArgumentCaptor<SubmitStatus> submitStatusCaptor;

	@Mock
	FileUtils fileUtils;

	@Captor
	ArgumentCaptor<MultiValueMap<String, Object>> bodyMapCaptor;

	@Mock
	ProspectStrategyIntegrationService prospectStrategyIntegrationService;

	@Mock
	SfdcClientService sfdcClientService;

	@Mock
	HttpServletRequest httpRequest;

	private static final String EMPLID = "00002222287";
	private static final String COMPANY_CODE = "0010z00001aloe4AAA";
	private static final String COMPANY_NAME = "ABC Ltd";
	private static final long STRATEGY_ID = 306733;
	private static final BenExchngEnums benExchngEnums = BenExchngEnums.TRINET_III;
	private static final String date = "2024_05_02";
    private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
    private MockedStatic<BenExchngEnums> mockStaticBenExchngEnums;
    private MockedStatic<DateUtils> mockStaticDateUtils;

    @Before
    public void setUp() {
        mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
        mockStaticBenExchngEnums = Mockito.mockStatic(BenExchngEnums.class);
        mockStaticDateUtils = Mockito.mockStatic(DateUtils.class);

        mockStaticBSSSecurityUtils.when(() -> BSSSecurityUtils.getAuthenticatedEmplId(any()))
                .thenReturn(EMPLID);
        mockStaticBenExchngEnums.when(() -> BenExchngEnums.getByExchangeId("TNIII"))
                .thenReturn(benExchngEnums);
        mockStaticDateUtils.when(DateUtils::getCurrentDate)
                .thenReturn(date);
    }

    @After
    public void tearDown() {
        if (mockStaticBSSSecurityUtils != null) mockStaticBSSSecurityUtils.close();
        if (mockStaticBenExchngEnums != null) mockStaticBenExchngEnums.close();
        if (mockStaticDateUtils != null) mockStaticDateUtils.close();
    }

	/**
	 * given company code, strategy id and exchange id </br>
	 * when submit method called </br>
	 * then verify all method calls as expected and strategy submitted is true with
	 * submitted date present
	 * 
	 * @throws IOException
	 **/
	@Test
	public void submitTest1() throws IOException {
		// given
		// data
		Company company = prepareCompany();
		String companyName = FileUtils.removeSpecialCharacters(company.getName());
		List<Strategy> strategies = getExistingStrategies();
		ProspectBenefitsSummaryTotalsResponse prospectBenefitsSummaryTotalsResponse = prepareProspectBenefitsSummaryTotalsResponse();
		byte[] employeeCostAndPlanComparisonReport = "EmployeeCostAndPlanComparisonReport".getBytes();
		byte[] planAppendixReport = "PlanAppendixReport".getBytes();
		Path employeeCostAndPlanComparisonReportPath = Files.write(
				Files.createTempFile(COMPANY_CODE + "_Benefits Proposal" + "_" + date, ".pdf"),
				employeeCostAndPlanComparisonReport);
		Path planAppendixReportPath = Files
				.write(Files.createTempFile(COMPANY_CODE + "_Plan Appendix" + "_" + date, ".pdf"), planAppendixReport);
		// method mocks
		when(companyService.getCompanyDetails(company.getCode(), false, EMPLID, benExchngEnums)).thenReturn(company);
		when(prospectStrategyIntegrationService.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company,
				benExchngEnums)).thenReturn(prospectBenefitsSummaryTotalsResponse);
		when(outputService.generateEmployeeCostAndPlanComparisonReport(company, STRATEGY_ID, httpRequest))
				.thenReturn(employeeCostAndPlanComparisonReport);
		when(outputService.generatePlanAppendixReport(company, STRATEGY_ID, httpRequest))
				.thenReturn(planAppendixReport);
		when(fileUtils.writeToTempFile(employeeCostAndPlanComparisonReport, ".pdf", "_", companyName,
				"Benefits Proposal", date)).thenReturn(employeeCostAndPlanComparisonReportPath);
		when(fileUtils.writeToTempFile(planAppendixReport, ".pdf", "_", companyName, "Plan Appendix", date))
				.thenReturn(planAppendixReportPath);
		when(strategyService.findBy(COMPANY_CODE)).thenReturn(strategies);
		doNothing().when(sfdcClientService).sendProposal(bodyMapCaptor.capture());
		when(submitStatusService.createUpdateSubmitStatus(submitStatusCaptor.capture())).thenReturn(new SubmitStatus());
		// when
		prospectSubmitService.submit(COMPANY_CODE, STRATEGY_ID, benExchngEnums.getExchangeId(), httpRequest);
		// then
		// assertions
		List<Strategy> submittedStrategies = strategies.stream().filter(Strategy::isSubmitted).collect(Collectors.toList());
		assertEquals(1, submittedStrategies.size());
		assertNotNull(submittedStrategies.get(0).getSubmitDate());
		// verify
		verify(companyService).getCompanyDetails(COMPANY_CODE, false, EMPLID, benExchngEnums);
		verify(prospectStrategyIntegrationService).getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company,
				benExchngEnums);
		verify(outputService).generateEmployeeCostAndPlanComparisonReport(company, STRATEGY_ID,
				httpRequest);
		verify(outputService).generatePlanAppendixReport(company, STRATEGY_ID, httpRequest);
		verify(fileUtils).writeToTempFile(employeeCostAndPlanComparisonReport, ".pdf", "_", companyName,
				"Benefits Proposal", date);
		verify(fileUtils).writeToTempFile(planAppendixReport, ".pdf", "_", companyName, "Plan Appendix", date);
		verify(strategyService).findBy(COMPANY_CODE);
		verify(submitStatusService).createUpdateSubmitStatus(submitStatusCaptor.getValue());
	}

	/**
	 * given company code, strategy id and exchange id and exception occurs</br>
	 * when submit method called </br>
	 * then throw BSSApplicationException
	 **/
	@Test(expected = BSSApplicationException.class)
	public void submitTest2() {
		// given
		// method mocks
		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, benExchngEnums)).thenThrow( new ProspectApiCallException("Error occurred while fetching company details"));
		// when
		prospectSubmitService.submit(COMPANY_CODE, STRATEGY_ID, benExchngEnums.getExchangeId(), httpRequest);
	}

	/**
	 * given company code, strategy id and exchange id and strategy id not found
	 * whle updating status</br>
	 * when submit method called </br>
	 * then throw BSSApplicationException
	 **/
	@Test(expected = BSSApplicationException.class)
	public void submitTest3() {
		// given
		Company company = prepareCompany();
		Optional<Strategy> strategyOpt = Optional.empty();
		ProspectBenefitsSummaryTotalsResponse prospectBenefitsSummaryTotalsResponse = prepareProspectBenefitsSummaryTotalsResponse();
		// method mocks
		when(companyService.getCompanyDetails(COMPANY_CODE, false, EMPLID, benExchngEnums)).thenReturn(company);
		when(prospectStrategyIntegrationService.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company,
				benExchngEnums)).thenReturn(prospectBenefitsSummaryTotalsResponse);
		// when
		prospectSubmitService.submit(COMPANY_CODE, STRATEGY_ID, benExchngEnums.getExchangeId(), httpRequest);
	}

	/**
	 * given company code, strategy id and exchange id </br>
	 * when submit method called </br>
	 * then verify all method calls as expected and strategy submitted is true with
	 * submitted date present and set submitted status to false if any submitted strategy present.
	 * 
	 * @throws IOException
	 **/
	@Test
	public void submitTest4() throws IOException {
		// given
		// data
		Company company = prepareCompany();
		String companyName = FileUtils.removeSpecialCharacters(company.getName());
		List<Strategy> strategies = getExistingStrategiesWithOneSubmittedStrategy();
		ProspectBenefitsSummaryTotalsResponse prospectBenefitsSummaryTotalsResponse = prepareProspectBenefitsSummaryTotalsResponse();
		byte[] employeeCostAndPlanComparisonReport = "EmployeeCostAndPlanComparisonReport".getBytes();
		byte[] planAppendixReport = "PlanAppendixReport".getBytes();
		Path employeeCostAndPlanComparisonReportPath = Files.write(
				Files.createTempFile(COMPANY_CODE + "_Benefits Proposal" + "_" + date, ".pdf"),
				employeeCostAndPlanComparisonReport);
		Path planAppendixReportPath = Files
				.write(Files.createTempFile(COMPANY_CODE + "_Plan Appendix" + "_" + date, ".pdf"), planAppendixReport);
		// method mocks
		when(companyService.getCompanyDetails(company.getCode(), false, EMPLID, benExchngEnums)).thenReturn(company);
		when(prospectStrategyIntegrationService.getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company,
				benExchngEnums)).thenReturn(prospectBenefitsSummaryTotalsResponse);
		when(outputService.generateEmployeeCostAndPlanComparisonReport(company, STRATEGY_ID, httpRequest))
				.thenReturn(employeeCostAndPlanComparisonReport);
		when(outputService.generatePlanAppendixReport(company, STRATEGY_ID, httpRequest))
				.thenReturn(planAppendixReport);
		when(fileUtils.writeToTempFile(employeeCostAndPlanComparisonReport, ".pdf", "_", companyName,
				"Benefits Proposal", date)).thenReturn(employeeCostAndPlanComparisonReportPath);
		when(fileUtils.writeToTempFile(planAppendixReport, ".pdf", "_", companyName, "Plan Appendix", date))
				.thenReturn(planAppendixReportPath);
		when(strategyService.findBy(COMPANY_CODE)).thenReturn(strategies);
		doNothing().when(sfdcClientService).sendProposal(bodyMapCaptor.capture());
		when(submitStatusService.createUpdateSubmitStatus(submitStatusCaptor.capture())).thenReturn(new SubmitStatus());
		// when
		prospectSubmitService.submit(COMPANY_CODE, STRATEGY_ID, benExchngEnums.getExchangeId(), httpRequest);
		// then
		// assertions
		List<Strategy> submittedStrategies = strategies.stream().filter(Strategy::isSubmitted).collect(Collectors.toList());
		assertEquals(1, submittedStrategies.size());
		assertNotNull(submittedStrategies.get(0).getSubmitDate());
		
		List<Strategy> unSubmittedStrategies = strategies.stream().filter(strategy -> !strategy.isSubmitted()).collect(Collectors.toList());
		assertEquals(2, unSubmittedStrategies.size());
		
		// verify
		verify(companyService).getCompanyDetails(COMPANY_CODE, false, EMPLID, benExchngEnums);
		verify(prospectStrategyIntegrationService).getBenefitsSummaryTotalsForStrategy(STRATEGY_ID, company,
				benExchngEnums);
		verify(outputService).generateEmployeeCostAndPlanComparisonReport(company, STRATEGY_ID,
				httpRequest);
		verify(outputService).generatePlanAppendixReport(company, STRATEGY_ID, httpRequest);
		verify(fileUtils).writeToTempFile(employeeCostAndPlanComparisonReport, ".pdf", "_", companyName,
				"Benefits Proposal", date);
		verify(fileUtils).writeToTempFile(planAppendixReport, ".pdf", "_", companyName, "Plan Appendix", date);
		verify(strategyService).findBy(COMPANY_CODE);
		verify(submitStatusService).createUpdateSubmitStatus(submitStatusCaptor.getValue());
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setName(COMPANY_NAME);
		company.setRealmPlanYearId(70L);
		return company;
	}

	private ProspectBenefitsSummaryTotalsResponse prepareProspectBenefitsSummaryTotalsResponse() {
		return TestHelper
				.readPlanComparisonRequest("/prospectSubmitServiceTestData/ProspectBenefitsSummaryTotalsResponse.json",
						new TypeReference<ProspectBenefitsSummaryTotalsResponse>() {
						})
				.get();
	}
	
	private List<Strategy> getExistingStrategies(){
		List<Strategy> strategies = new ArrayList<>();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setSubmitted(false);
		strategies.add(strategy);
		strategy = new Strategy();
		strategy.setId(123456L);
		strategy.setSubmitted(false);
		strategies.add(strategy);
		strategy = new Strategy();
		strategy.setId(234567L);
		strategy.setSubmitted(false);
		strategies.add(strategy);
		
		return strategies;
	}
	
	private List<Strategy> getExistingStrategiesWithOneSubmittedStrategy(){
		List<Strategy> strategies = new ArrayList<>();
		Strategy strategy = new Strategy();
		strategy.setId(STRATEGY_ID);
		strategy.setSubmitted(false);
		strategies.add(strategy);
		strategy = new Strategy();
		strategy.setId(123456L);
		strategy.setSubmitted(true);
		strategy.setSubmitDate(new Date());
		strategies.add(strategy);
		strategy = new Strategy();
		strategy.setId(234567L);
		strategy.setSubmitted(false);
		strategies.add(strategy);
		
		return strategies;
	}
}