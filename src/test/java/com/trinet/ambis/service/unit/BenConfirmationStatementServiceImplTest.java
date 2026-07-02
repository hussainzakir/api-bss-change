package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.BenConfirmationStmntDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.DocManagementService;
import com.trinet.ambis.service.impl.BenConfirmationStatementServiceImpl;
import com.trinet.ambis.service.model.BenConfirmationStatement;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.common.AppConfig;
import com.trinet.common.CommonUtils;
import com.trinet.common.DateUtils;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.exception.TriNetParseException;
import com.trinet.security.util.SecurityUtils;


@RunWith(MockitoJUnitRunner.class)
public class BenConfirmationStatementServiceImplTest extends ServiceUnitTest {

    @Autowired
    @InjectMocks
    BenConfirmationStatementServiceImpl benConfirmationStatementService;

    @Mock
    BenConfirmationStmntDao benConfirmationStmntDao;

    @Mock
    DocManagementService docManagementService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    CompanyService companyService;

    private static final String COMPANY_CODE = "G48";

    public static final String token = "AQIC5wM2LY4SfcyaCHj61cbE-i1pQwGjCjDszTcW7LHGX_w.*AAJTSQACMDMAAlNLABIzMTUzNjM2MDg3MDI4MDM4OTYAAlMxAAIwMQ..*";

    private MockedStatic<BSSSecurityUtils> mockStaticBSSSecurityUtils;
    private MockedStatic<SecurityUtils> mockStaticSecurityUtils;
    private MockedStatic<AppConfig> mockStaticAppConfig;
    private MockedStatic<CommonUtils> mockStaticCommonUtils;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Static mocks
        mockStaticBSSSecurityUtils = Mockito.mockStatic(BSSSecurityUtils.class);
        mockStaticSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
        mockStaticAppConfig = Mockito.mockStatic(AppConfig.class);
        mockStaticCommonUtils = Mockito.mockStatic(CommonUtils.class);

        mockStaticBSSSecurityUtils.when(BSSSecurityUtils::getAuthenticatedPersonId).thenReturn("00000000000");
    }

    @After
    public void tearDown() {
        mockStaticBSSSecurityUtils.close();
        mockStaticSecurityUtils.close();
        mockStaticAppConfig.close();
        mockStaticCommonUtils.close();
    }

    @Test
    public void getConfirmationStatements_NoResults() {
        Company company = new Company();
        company.setBenefitStartDate("01-JAN-2020");

        mockStaticSecurityUtils.when(() -> SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);

        ResponseEntity<ReturnResponse> resp = populateResponseEntity();
        List<BenConfirmationStatement> benConfirmationStatementResults = Collections.emptyList();

        when(companyService.getCompanyDetails(COMPANY_CODE, false, "00000000000", null)).thenReturn(company);
        when(benConfirmationStmntDao.getBenefitConfirmationStatementsBy(COMPANY_CODE)).thenReturn(benConfirmationStatementResults);
        reset(restTemplate);

        List<BenConfirmationStatement> expectedBenConfirmationStatementResults = benConfirmationStatementService.getBenConfirmationStatementsBy(COMPANY_CODE);
        verify(benConfirmationStmntDao, times(1)).getBenefitConfirmationStatementsBy(COMPANY_CODE);

        assertEquals(0, expectedBenConfirmationStatementResults.size());
    }

    @Test
    public void getConfirmationStatements_renewalComp() throws TriNetParseException {
        Company company = new Company();
        company.setBenefitStartDate("01-JAN-2020");

        mockStaticSecurityUtils.when(() -> SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
        mockStaticAppConfig.when(AppConfig::getPlatformURL).thenReturn("http://hrpbib.hrpassport.com/");

        ResponseEntity<ReturnResponse> resp = populateResponseEntity();
        List<BenConfirmationStatement> benConfirmationStatementResults = prepareConfirmationStatements();

        Map<String, String> confirmationStmtUrls = new HashMap<>();
        confirmationStmtUrls.put("BCS_ETLADVXV1U56", "api-benefits-docmgmt/v1/downloadDocument/G48/64825/bss");
        confirmationStmtUrls.put("BCS_ETLADVXV1U57", "api-benefits-docmgmt/v1/downloadDocument/G48/64826/bss");
        confirmationStmtUrls.put("BCS_ETLADVXV1U58", "api-benefits-docmgmt/v1/downloadDocument/G48/64827/bss");

        when(companyService.getCompanyDetails(COMPANY_CODE, false, "00000000000", null)).thenReturn(company);
        when(docManagementService.retrieveConfirmationStatementUrls(COMPANY_CODE)).thenReturn(confirmationStmtUrls);
        when(benConfirmationStmntDao.getBenefitConfirmationStatementsBy(COMPANY_CODE)).thenReturn(benConfirmationStatementResults);

        List<BenConfirmationStatement> expectedBenConfirmationStatementResults = benConfirmationStatementService.getBenConfirmationStatementsBy(COMPANY_CODE);
        verify(benConfirmationStmntDao, times(1)).getBenefitConfirmationStatementsBy(COMPANY_CODE);

        assertEquals(2, expectedBenConfirmationStatementResults.size());
        assertEquals("http://hrpbib.hrpassport.com/api-benefits-docmgmt/v1/downloadDocument/G48/64825/bss", expectedBenConfirmationStatementResults.get(0).getUrl());
        assertEquals("http://hrpbib.hrpassport.com/api-benefits-docmgmt/v1/downloadDocument/G48/64826/bss", expectedBenConfirmationStatementResults.get(1).getUrl());

        assertEquals(DateUtils.convertStringToDate("01-MAR-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(0).getStatementStartDate());
        assertEquals(DateUtils.convertStringToDate("31-DEC-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(0).getStatementEndDate());
        assertEquals("Mid-Year Funding", expectedBenConfirmationStatementResults.get(0).getSubmitType());

        assertEquals(DateUtils.convertStringToDate("01-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(1).getStatementStartDate());
        assertEquals(DateUtils.convertStringToDate("28-FEB-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(1).getStatementEndDate());
        assertEquals("Annual Renewal", expectedBenConfirmationStatementResults.get(1).getSubmitType());
    }

    @Test
    public void getConfirmationStatements_newComp() throws TriNetParseException {
        Company company = new Company();
        company.setBenefitStartDate("01-FEB-2021");

        mockStaticSecurityUtils.when(() -> SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
        mockStaticAppConfig.when(AppConfig::getPlatformURL).thenReturn("http://hrpbib.hrpassport.com/");

        ResponseEntity<ReturnResponse> resp = populateResponseEntity();
        List<BenConfirmationStatement> benConfirmationStatementResults = prepareConfirmationStatements();

        Map<String, String> confirmationStmtUrls = new HashMap<>();
        confirmationStmtUrls.put("BCS_ETLADVXV1U56", "api-benefits-docmgmt/v1/downloadDocument/G48/64825/bss");
        confirmationStmtUrls.put("BCS_ETLADVXV1U57", "api-benefits-docmgmt/v1/downloadDocument/G48/64826/bss");
        confirmationStmtUrls.put("BCS_ETLADVXV1U58", "api-benefits-docmgmt/v1/downloadDocument/G48/64827/bss");

        when(companyService.getCompanyDetails(COMPANY_CODE, false, "00000000000", null)).thenReturn(company);
        when(docManagementService.retrieveConfirmationStatementUrls(COMPANY_CODE)).thenReturn(confirmationStmtUrls);
        when(benConfirmationStmntDao.getBenefitConfirmationStatementsBy(COMPANY_CODE)).thenReturn(benConfirmationStatementResults);

        List<BenConfirmationStatement> expectedBenConfirmationStatementResults = benConfirmationStatementService.getBenConfirmationStatementsBy(COMPANY_CODE);
        verify(benConfirmationStmntDao, times(1)).getBenefitConfirmationStatementsBy(COMPANY_CODE);

        assertEquals(2, expectedBenConfirmationStatementResults.size());
        assertEquals("http://hrpbib.hrpassport.com/api-benefits-docmgmt/v1/downloadDocument/G48/64825/bss", expectedBenConfirmationStatementResults.get(0).getUrl());
        assertEquals("http://hrpbib.hrpassport.com/api-benefits-docmgmt/v1/downloadDocument/G48/64826/bss", expectedBenConfirmationStatementResults.get(1).getUrl());

        assertEquals(DateUtils.convertStringToDate("01-MAR-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(0).getStatementStartDate());
        assertEquals(DateUtils.convertStringToDate("31-DEC-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(0).getStatementEndDate());
        assertEquals("Mid-Year Funding", expectedBenConfirmationStatementResults.get(0).getSubmitType());

        assertEquals(DateUtils.convertStringToDate("01-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(1).getStatementStartDate());
        assertEquals(DateUtils.convertStringToDate("28-FEB-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(1).getStatementEndDate());
        assertEquals("First Year", expectedBenConfirmationStatementResults.get(1).getSubmitType());
    }

    @Test
    public void getConfirmationStatements_newComp1() throws TriNetParseException {
        Company company = new Company();
        company.setBenefitStartDate("01-JAN-2021");

        mockStaticSecurityUtils.when(() -> SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
        mockStaticAppConfig.when(AppConfig::getPlatformURL).thenReturn("http://hrpbib.hrpassport.com/");

        ResponseEntity<ReturnResponse> resp = populateResponseEntity();
        List<BenConfirmationStatement> benConfirmationStatementResults = prepareConfirmationStatements();

        Map<String, String> confirmationStmtUrls = new HashMap<>();
        confirmationStmtUrls.put("BCS_ETLADVXV1U56", "api-benefits-docmgmt/v1/downloadDocument/G48/64825/bss");
        confirmationStmtUrls.put("BCS_ETLADVXV1U57", "api-benefits-docmgmt/v1/downloadDocument/G48/64826/bss");
        confirmationStmtUrls.put("BCS_ETLADVXV1U58", "api-benefits-docmgmt/v1/downloadDocument/G48/64827/bss");

        when(companyService.getCompanyDetails(COMPANY_CODE, false, "00000000000", null)).thenReturn(company);
        when(docManagementService.retrieveConfirmationStatementUrls(COMPANY_CODE)).thenReturn(confirmationStmtUrls);
        when(benConfirmationStmntDao.getBenefitConfirmationStatementsBy(COMPANY_CODE)).thenReturn(benConfirmationStatementResults);
//        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.eq(ReturnResponse.class))).thenReturn(resp);

        List<BenConfirmationStatement> expectedBenConfirmationStatementResults = benConfirmationStatementService.getBenConfirmationStatementsBy(COMPANY_CODE);
        verify(benConfirmationStmntDao, times(1)).getBenefitConfirmationStatementsBy(COMPANY_CODE);

        assertEquals(2, expectedBenConfirmationStatementResults.size());
        assertEquals("http://hrpbib.hrpassport.com/api-benefits-docmgmt/v1/downloadDocument/G48/64825/bss", expectedBenConfirmationStatementResults.get(0).getUrl());
        assertEquals("http://hrpbib.hrpassport.com/api-benefits-docmgmt/v1/downloadDocument/G48/64826/bss", expectedBenConfirmationStatementResults.get(1).getUrl());

        assertEquals(DateUtils.convertStringToDate("01-MAR-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(0).getStatementStartDate());
        assertEquals(DateUtils.convertStringToDate("31-DEC-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(0).getStatementEndDate());
        assertEquals("Mid-Year Funding", expectedBenConfirmationStatementResults.get(0).getSubmitType());

        assertEquals(DateUtils.convertStringToDate("01-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(1).getStatementStartDate());
        assertEquals(DateUtils.convertStringToDate("28-FEB-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY), expectedBenConfirmationStatementResults.get(1).getStatementEndDate());
        assertEquals("First Year", expectedBenConfirmationStatementResults.get(1).getSubmitType());
    }

    private List<BenConfirmationStatement> prepareConfirmationStatements() throws TriNetParseException {
        List<BenConfirmationStatement> benConfirmationStatements = new ArrayList<>();

        BenConfirmationStatement benConfirmationStatement = new BenConfirmationStatement();
        benConfirmationStatement.setStrategyId(153367);
        benConfirmationStatement.setRealmYrId(35);
        benConfirmationStatement.setPlanYrStartDate(DateUtils.convertStringToDate("01-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatement.setPlanYrEndDate(DateUtils.convertStringToDate("31-DEC-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatement.setEffectiveDate(DateUtils.convertStringToDate("01-MAR-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatement.setSubmitUser("Mid-Year Submit");
        benConfirmationStatement.setConfirmationNumber("ETLADVXV1U56");
        benConfirmationStatement.setSubmittedDate(DateUtils.convertStringToDate("01-NOV-2020", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatements.add(benConfirmationStatement);

        benConfirmationStatement = new BenConfirmationStatement();
        benConfirmationStatement.setStrategyId(153368);
        benConfirmationStatement.setRealmYrId(35);
        benConfirmationStatement.setPlanYrStartDate(DateUtils.convertStringToDate("01-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatement.setPlanYrEndDate(DateUtils.convertStringToDate("31-DEC-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatement.setEffectiveDate(DateUtils.convertStringToDate("01-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatement.setSubmitUser("Woualson Highfield");
        benConfirmationStatement.setConfirmationNumber("ETLADVXV1U57");
        benConfirmationStatement.setSubmittedDate(DateUtils.convertStringToDate("01-JAN-2021", BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
        benConfirmationStatements.add(benConfirmationStatement);

        return benConfirmationStatements;
    }

    private ResponseEntity<ReturnResponse> populateResponseEntity() {
        ReturnResponse rr = new ReturnResponse();
        List<Map<String, String>> confirmationStatementUrls = new ArrayList<>();
        Map<String, String> titleUrls = new HashMap<>();
        titleUrls.put("url", "/api-benefits-docmgmt/v1/downloadDocument/L1M/64701");
        confirmationStatementUrls.add(titleUrls);
        rr.setData(confirmationStatementUrls);
        ResponseEntity<ReturnResponse> resp = new ResponseEntity<ReturnResponse>(rr, HttpStatus.OK);
        return resp;
    }

}
