package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.MultiKeyMap;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.OutputBenefitsTypeEnums;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.impl.PersonDaoImpl;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.rest.controllers.dto.outputs.BSSReportDetails;
import com.trinet.ambis.rest.controllers.dto.outputs.OutputRequest;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.impl.outputs.OutputRequestBuilderImpl;
import com.trinet.ambis.service.impl.outputs.OutputServiceImpl;
import com.trinet.ambis.service.prospect.dto.BenTypeOfferRes;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.RestApiClient;

/**
 * @author rterle
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class OutputServiceTest {

	@InjectMocks
	OutputServiceImpl outputService;

	@Mock
	CompanyService companyService;

	@Mock
	RestApiClient restApiClient;

	@Mock
	OutputRequestBuilderImpl outputRequestBuiler;

	@Mock
	PersonDaoImpl personDao;

	@Mock
	HttpServletRequest httpRequest;

	@Mock
	StrategyDataDao strategyDataDao;
	
	@Captor
	ArgumentCaptor<HttpServletRequest> httpServletRequestCaptor;

	@Captor
	ArgumentCaptor<BSSReportDetails> bssReportDetailsCaptor;

	@Captor
	ArgumentCaptor<String> urlCaptor;

	@Captor
	ArgumentCaptor<HttpMethod> httpMethodCaptor;

	@Captor
	ArgumentCaptor<OutputRequest> outputRequestCaptor;

	@Captor
	ArgumentCaptor<Company> companyCaptor;
	
	private static final String EMPLID = "00002222287";
	private static final String ADMIN_COMPANY_CODE = "001";
	private static final String COMPANY_CODE = "0010z00001aloe4AAA";
	private static final String COMPANY_NAME = "ABC Ltd";
	private static final long STRATEGY_ID = 306733;
    private MockedStatic<BSSSecurityUtils> bssSecurityUtilsMockedStatic;

    @After
    public void tearDown() {
        bssSecurityUtilsMockedStatic.close();
    }

	@Before
	public void setUp() {
        bssSecurityUtilsMockedStatic = Mockito.mockStatic(BSSSecurityUtils.class);

        ReflectionTestUtils.setField(outputService, "docGenUrl",
				"https://trinetqen1.hrpassport.com/api-docgen/v1/doc-gen/");
		when(BSSSecurityUtils.getAuthenticatedCompanyCode(httpRequest)).thenReturn(ADMIN_COMPANY_CODE);
		when(BSSSecurityUtils.getAuthenticatedEmplId(httpRequest)).thenReturn(EMPLID);
	}

	@Test
	public void generateOutputTest() {
		// Given
		OutputRequest outputRequest = new OutputRequest();
		BSSReportDetails bssReportDetails = new BSSReportDetails();
		Company company = new Company();

		// When
		when(outputRequestBuiler.prepareBssReportRequest(outputRequest, company, httpRequest)).thenReturn(bssReportDetails);
		when(restApiClient.getReturnResponse(any(), any(), any(), any())).thenReturn(new byte[10]);

		byte[] template = outputService.generateReport(outputRequest, company, httpRequest);

		// Assert
		assertNotNull(template);
		assertTrue(template.length > 0);
	}

	@Test(expected = BSSApplicationException.class)
	public void generateOutputTestWhenCatchingException() {
		// Given
		OutputRequest outputRequest = new OutputRequest();
		BSSReportDetails bssReportDetails = new BSSReportDetails();
		Company company = new Company();

		// When
		when(outputRequestBuiler.prepareBssReportRequest(outputRequest, company, httpRequest)).thenReturn(bssReportDetails);
		when(restApiClient.getReturnResponse(any(), any(), any(), any())).thenThrow(BSSApplicationException.class);

		outputService.generateReport(outputRequest, company, httpRequest);
	}

	@Test
	public void getPlanTypeOfferedDetails() {
		List<Long> strategyIds = List.of(1111L, 2222L, 3333L);
		MultiKeyMap strategyGroupPlantypeMap = new MultiKeyMap();
		strategyGroupPlantypeMap.put(1111L, "BENPRG2", "14", "1V");
		strategyGroupPlantypeMap.put(2222L, "BENPRG1", "11", "1D");
		strategyGroupPlantypeMap.put(2222L, "BENPRG1", "14", "1V");
		strategyGroupPlantypeMap.put(2222L, "BENPRG2", "14", "1V");
		strategyGroupPlantypeMap.put(3333L, "BENPRG1", "10", "10");
		strategyGroupPlantypeMap.put(3333L, "BENPRG2", "10", "10");
		strategyGroupPlantypeMap.put(3333L, "BENPRG1", "11", "11");
		strategyGroupPlantypeMap.put(3333L, "BENPRG2", "14", "14");
		when(strategyDataDao.getStrategyProgramPlantypeOfferings(strategyIds, Arrays.asList("10","11","14","1D","1V"))).thenReturn(strategyGroupPlantypeMap);

		List<BenTypeOfferRes> actualRes = outputService.getPlanTypeOfferedDetails(strategyIds, Arrays.asList("10","11","14","1D","1V"));

		assertEquals(3, actualRes.size());
		assertTrue(Arrays.asList(actualRes.get(0).getStrategyId(), actualRes.get(1).getStrategyId(),
				actualRes.get(2).getStrategyId()).containsAll(strategyIds));
		actualRes.forEach(obj -> {
			if (Long.compare(1111L, obj.getStrategyId()) == 0) {
				assertEquals(1, obj.getOfferTypes().size());
				assertTrue(obj.getOfferTypes().contains("14"));
			}
			if (Long.compare(2222L, obj.getStrategyId()) == 0) {
				assertEquals(2, obj.getOfferTypes().size());
				assertTrue(obj.getOfferTypes().containsAll(Arrays.asList("11", "14")));
			}
			if (Long.compare(3333L, obj.getStrategyId()) == 0) {
				assertEquals(3, obj.getOfferTypes().size());
				assertTrue(obj.getOfferTypes().containsAll(Arrays.asList("10", "11", "14")));
			}
		});

	}

	/**
	 * given company and strategyId </br>
	 * when generatePlanAppendixReport method called </br>
	 * then verify all method calls as expected
	 **/
	@Test
	public void generatePlanAppendixReportTest() {
		// given
		// data
		Company company = prepareCompany();
		BSSReportDetails bssReportDetails = new BSSReportDetails();
		byte[] reportData = new byte[10];
		// method mocks
		when(outputRequestBuiler.prepareBssReportRequest(outputRequestCaptor.capture(), companyCaptor.capture(),
				httpServletRequestCaptor.capture())).thenReturn(bssReportDetails);
		when(restApiClient.getReturnResponse(httpServletRequestCaptor.capture(), bssReportDetailsCaptor.capture(),
				urlCaptor.capture(), httpMethodCaptor.capture())).thenReturn(reportData);
		// when
		byte[] actualResult = outputService.generatePlanAppendixReport(company, STRATEGY_ID, httpRequest);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(reportData, actualResult);
		OutputRequest outputRequest = outputRequestCaptor.getValue();
		assertEquals(String.valueOf(STRATEGY_ID), outputRequest.getTnStrategyId());
		List<String> templateNames = outputRequest.getTemplateNames();
		assertNotNull(templateNames);
		assertEquals(1, templateNames.size());
		assertEquals(ProspectConstants.PLAN_APPENDIX, templateNames.get(0));
		List<String> benefitTypes = outputRequest.getBenefitTypes();
		assertEquals(5, benefitTypes.size());
		assertEquals(OutputBenefitsTypeEnums.MEDICAL.getName(), benefitTypes.get(0));
		assertEquals(OutputBenefitsTypeEnums.DENTAL.getName(), benefitTypes.get(1));
		assertEquals(OutputBenefitsTypeEnums.VISION.getName(), benefitTypes.get(2));
		assertEquals(OutputBenefitsTypeEnums.LIFE.getName(), benefitTypes.get(3));
		assertEquals(OutputBenefitsTypeEnums.DISABILITY.getName(), benefitTypes.get(4));
		assertEquals(HttpMethod.POST, httpMethodCaptor.getValue());
		assertTrue(urlCaptor.getValue().endsWith(ADMIN_COMPANY_CODE + "/" + EMPLID + "/generate-download"));
		assertEquals(bssReportDetails, bssReportDetailsCaptor.getValue());
		// verify
		verify(outputRequestBuiler).prepareBssReportRequest(outputRequestCaptor.getValue(), companyCaptor.getValue(),
				httpServletRequestCaptor.getValue());
		verify(restApiClient).getReturnResponse(httpServletRequestCaptor.getValue(), bssReportDetailsCaptor.getValue(),
				urlCaptor.getValue(), httpMethodCaptor.getValue());
	}

	/**
	 * given company and strategyId </br>
	 * when generateEmployeeCostAndPlanComparisonReport method called </br>
	 * then verify all method calls as expected
	 **/
	@Test
	public void generateEmployeeCostAndPlanComparisonReportTest() {
		// given
		// data
		Company company = prepareCompany();
		BSSReportDetails bssReportDetails = new BSSReportDetails();
		byte[] reportData = new byte[10];
		// method mocks
		when(outputRequestBuiler.prepareBssReportRequest(outputRequestCaptor.capture(), companyCaptor.capture(),
				httpServletRequestCaptor.capture())).thenReturn(bssReportDetails);
		when(restApiClient.getReturnResponse(httpServletRequestCaptor.capture(), bssReportDetailsCaptor.capture(),
				urlCaptor.capture(), httpMethodCaptor.capture())).thenReturn(reportData);
		// when
		byte[] actualResult = outputService.generateEmployeeCostAndPlanComparisonReport(company, STRATEGY_ID,
				httpRequest);
		// then
		// assertions
		assertNotNull(actualResult);
		assertEquals(reportData, actualResult);
		OutputRequest outputRequest = outputRequestCaptor.getValue();
		assertEquals(String.valueOf(STRATEGY_ID), outputRequest.getTnStrategyId());
		List<String> templateNames = outputRequest.getTemplateNames();
		assertNotNull(templateNames);
		assertEquals(2, templateNames.size());
		assertEquals(ProspectConstants.EMPLOYEE_COST_COMPARISON, templateNames.get(0));
		assertEquals(ProspectConstants.PLAN_COMPARISON, templateNames.get(1));
		List<String> benefitTypes = outputRequest.getBenefitTypes();
		assertEquals(5, benefitTypes.size());
		assertEquals(OutputBenefitsTypeEnums.MEDICAL.getName(), benefitTypes.get(0));
		assertEquals(OutputBenefitsTypeEnums.DENTAL.getName(), benefitTypes.get(1));
		assertEquals(OutputBenefitsTypeEnums.VISION.getName(), benefitTypes.get(2));
		assertEquals(OutputBenefitsTypeEnums.LIFE.getName(), benefitTypes.get(3));
		assertEquals(OutputBenefitsTypeEnums.DISABILITY.getName(), benefitTypes.get(4));
		assertEquals(HttpMethod.POST, httpMethodCaptor.getValue());
		assertTrue(urlCaptor.getValue().endsWith(ADMIN_COMPANY_CODE + "/" + EMPLID + "/generate-download"));
		assertEquals(bssReportDetails, bssReportDetailsCaptor.getValue());
		// verify
		verify(outputRequestBuiler).prepareBssReportRequest(outputRequestCaptor.getValue(), companyCaptor.getValue(),
				httpServletRequestCaptor.getValue());
		verify(restApiClient).getReturnResponse(httpServletRequestCaptor.getValue(), bssReportDetailsCaptor.getValue(),
				urlCaptor.getValue(), httpMethodCaptor.getValue());
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setName(COMPANY_NAME);
		company.setRealmPlanYearId(70L);
		return company;
	}

}