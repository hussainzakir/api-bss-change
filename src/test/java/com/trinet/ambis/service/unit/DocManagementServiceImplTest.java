package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.openhtmltopdf.pdfboxout.PdfContentStreamAdapter.PdfException;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.dao.hrp.DocTypeDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.DocumentType;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.DocManagementServiceImpl;
import com.trinet.ambis.service.model.UploadFileResponse;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.common.AppConfig;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.util.SecurityUtils;


@RunWith(MockitoJUnitRunner.class)
public class DocManagementServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	DocManagementServiceImpl docManagementService;

	@Mock
	DocTypeDao docTypeDao;

	@Mock
	private HttpServletRequest request;

	@Mock
	private RestTemplate restTemplate;

	private static final String COMPANY_CODE = "G48";
	private static final Long REALM_ID = 2L;
	private static final String CONFIRMATIONSTMT_HTML = "confirmationStmtHtml";
	private static final String CONFORMATION_ID = "NPKPUL6JWHJU";
	private Company company = null;
	public static final String token = "AQIC5wM2LY4SfcyaCHj61cbE-i1pQwGjCjDszTcW7LHGX_w.*AAJTSQACMDMAAlNLABIzMTUzNjM2MDg3MDI4MDM4OTYAAlMxAAIwMQ..*";
    // Fields in your test class
    private MockedStatic<SecurityUtils> securityUtilsMockedStatic;
    private MockedStatic<AppConfig> appConfigMockedStatic;
    private MockedStatic<BSSMessageConfig> bssMessageConfigMockedStatic;
    private MockedStatic<CommonUtils> commonUtilsMockedStatic;

    @Before
    public void setup() {
        securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class);
        appConfigMockedStatic = Mockito.mockStatic(AppConfig.class);
        bssMessageConfigMockedStatic = Mockito.mockStatic(BSSMessageConfig.class);
        commonUtilsMockedStatic = Mockito.mockStatic(CommonUtils.class);
    }

    @After
    public void tearDown() {
        if(securityUtilsMockedStatic != null)
        securityUtilsMockedStatic.close();
        if(appConfigMockedStatic != null)
        appConfigMockedStatic.close();
        if(bssMessageConfigMockedStatic != null)
        bssMessageConfigMockedStatic.close();
        if(commonUtilsMockedStatic != null)
        commonUtilsMockedStatic.close();
    }

	@Test
	public void retrieveConfirmationStatementUrls_emptyTest() {

		ResponseEntity<ReturnResponse> resp1 = populateEmptyResponseEntity();

		when(SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
		// when(CommonUtils.prepareFinalRequestUri(Mockito.anyString(),Mockito.anyMap())).thenReturn("microbib.url");
		when(AppConfig.getMicroServiceURL()).thenReturn("microbib.url");
		reset(restTemplate);

		Map<String, String> expectedConfirmationUrls = docManagementService
				.retrieveConfirmationStatementUrls(COMPANY_CODE);

		assertEquals(0, expectedConfirmationUrls.size());
	}

	@Test
	public void retrieveConfirmationStatementUrls_test() {

		ResponseEntity<ReturnResponse> resp2 = populateResponseEntity();
		when(SecurityUtils.parseAuthenticationToken(request)).thenReturn(token);
		when(BSSMessageConfig.getProperty(BSSURIConstants.CONFIRMATION_STATEMENT_DOC_URI)).thenReturn("/bss");
	    when(CommonUtils.prepareFinalRequestUri(Mockito.anyString(),Mockito.anyMap())).thenReturn("microbib.url");
		when(AppConfig.getMicroServiceURL()).thenReturn("microbib.url");
		reset(restTemplate);
		Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class),
				Mockito.eq(ReturnResponse.class))).thenReturn(resp2);

		Map<String, String> expectedConfirmationUrls = docManagementService
				.retrieveConfirmationStatementUrls(COMPANY_CODE);

		assertEquals(1, expectedConfirmationUrls.size());

	}

	@Test
	public void uploadConfirmationStatement_Test() throws PdfException, IOException {

		ResponseEntity<ReturnResponse> resp1 = populateUploadResponseEntity();
        File mockFile = mock(File.class);
        when(mockFile.getPath()).thenReturn("/path/to/your/file.txt");
		company = prepareCompany();
		DocumentType docType = new DocumentType();
		docType.setExchangeId(REALM_ID.intValue());
		docType.setDocumentTypeId(111);

		DocumentType docType1 = new DocumentType();
		docType1.setExchangeId(3);
		docType1.setDocumentTypeId(222);
		List<DocumentType> docTypes = Arrays.asList(docType);

		when(AppConfig.getPlatformURL()).thenReturn("https://trinetplatform.hrpassport.com");
		when(docTypeDao.findByDocTypeName(BSSApplicationConstants.CONFIRMATION_STMT_DOC_TYPE)).thenReturn(docTypes);
		when(CommonUtils.convertHtmlToPdf(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(mockFile);

		reset(restTemplate);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class),
				Mockito.any(HttpEntity.class), (ParameterizedTypeReference<ReturnResponse>) Mockito.any(Object.class)))
				.thenReturn(resp1);

		boolean uploadResponse = docManagementService.uploadConfirmationStatement("<html><body><h1>Hello, World!</h1></body></html>", company,
				CONFORMATION_ID);

		assertEquals(true, uploadResponse);
	}

	@Test
	public void uploadConfirmationStatement_Test1() throws PdfException, IOException {

		ResponseEntity<ReturnResponse> resp1 = populateUploadResponseEntity();

		company = prepareCompany();
		when(AppConfig.getPlatformURL()).thenReturn("https://trinetplatform.hrpassport.com");
		when(BSSMessageConfig.getProperty(BSSURIConstants.UPLOAD_CONFIRMATION_STATEMENT)).thenReturn("/bss");
		
		reset(restTemplate);
		when(CommonUtils.convertHtmlToPdf(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(null);
		boolean uploadResponse = docManagementService.uploadConfirmationStatement(CONFIRMATIONSTMT_HTML, company,
				CONFORMATION_ID);

		assertEquals(false, uploadResponse);
	}

	private List<DocumentType> setDocumentTypes() {
		List<DocumentType> documentTypes = new ArrayList<>();
		DocumentType documentType = new DocumentType();
		documentType.setDateType("PLAN_YEAR_START_DATE");
		documentType.setDescription("BSS Confirmation Statement");
		documentType.setDocTypeName("BCS");
		documentType.setDocumentTypeId(123);
		documentType.setExchangeId(3);
		documentTypes.add(documentType);

		return documentTypes;
	}

	private ResponseEntity<ReturnResponse> populateEmptyResponseEntity() {
		ReturnResponse rr = new ReturnResponse();
		List<Map<String, String>> confirmationStatementUrls = Collections.emptyList();
		rr.setData(confirmationStatementUrls);
		ResponseEntity<ReturnResponse> resp = new ResponseEntity<ReturnResponse>(rr, HttpStatus.OK);
		return resp;
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

	private ResponseEntity<ReturnResponse> populateUploadResponseEntity() {

		ReturnResponse rr = new ReturnResponse();
		UploadFileResponse uploadFileResponse = new UploadFileResponse();
		rr.setData(uploadFileResponse);
		rr.setStatusCode(String.valueOf(BSSHttpStatusConstants.OK));
		ResponseEntity<ReturnResponse> resp = new ResponseEntity<ReturnResponse>(rr, HttpStatus.OK);
		return resp;
	}

	private Company prepareCompany() {
		Company company = new Company();
		company.setCode(COMPANY_CODE);
		company.setPlanStartDate("01-JAN-2020");
		company.setPlanEndDate("01-DEC-2020");
		Realm realm = new Realm();
		realm.setId(REALM_ID);
		company.setRealm(realm);
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(REALM_ID);
		realmPlanYear.setPlanYearStart(new Date());
		realmPlanYear.setPlanYearEnd(new Date());
		company.setRealmPlanYear(realmPlanYear);
		company.setRenewalCompany(false);
		return company;
	}

}
