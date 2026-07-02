package com.trinet.ambis.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.helper.SubmitServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.DocTypeDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.DocumentType;
import com.trinet.ambis.service.DocManagementService;
import com.trinet.ambis.service.model.DMRequest;
import com.trinet.ambis.service.model.QuarterPlanYearDate;
import com.trinet.ambis.service.model.UploadFileResponse;
import com.trinet.ambis.util.BSSSecurityUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.common.AppConfig;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.util.SecurityUtils;

@Service
public class DocManagementServiceImpl implements DocManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocManagementServiceImpl.class);

	// common request constants
	private static final String REQ_AUTH_TOKEN_KEY = "token";
	private static final String REQ_ATTR_COMPANY_CODE = "COMPANY_CODE";
	private static final String PATH_PARAM_COMPANY_ID = "companyId";

	// Upload confirmation statement request constants.
	private static final String REQ_ATTR_FILE_NAMEN_ID = "FILE_NAME";
	private static final String REQ_BODY_FILES_PARAM = "files";
	private static final String REQ_BODY_DATA_PARAM = "data";

	// Retrieve confirmation statement urls request constants.
	private static final String REQ_DATA_QTR_PLYR_DT_ID_PARAM = "qtrPlanYearDateIds";
	private static final String REQ_DATA_DOC_TYPE_PARAM = "documentType";
	private static final String REQ_DATA_ATTR_PARAM = "attributes";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private DocTypeDao docTypeDao;

	@Override
	public boolean uploadConfirmationStatement(String confirmationStmtHtml, Company company, String confirmationId) {
		boolean uploadSuccessStatus = false;
		File tmpPdfFile = null;
		try {
			ParameterizedTypeReference<ReturnResponse<UploadFileResponse>> myBean = new ParameterizedTypeReference<ReturnResponse<UploadFileResponse>>() {
			};
			tmpPdfFile = CommonUtils.convertHtmlToPdf(confirmationId, ".pdf", confirmationStmtHtml);
			HttpEntity<MultiValueMap<String, Object>> entity = prepareRequestEntity(tmpPdfFile, company,
					confirmationId);
			String finalUploadServiceUrl = prepareUploadServiceUri();
			long startTime = System.currentTimeMillis();
			LOGGER.info("ApiServiceBroker::invokeRestCall Start call to url: {} and timestamp: {}",
					finalUploadServiceUrl, startTime);
			ResponseEntity<ReturnResponse<UploadFileResponse>> resp = restTemplate.exchange(finalUploadServiceUrl,
					HttpMethod.POST, entity, myBean);
			LOGGER.info("ApiServiceBroker::invokeRestCall End call to url: {} and time taken in ms: {}",
					finalUploadServiceUrl, System.currentTimeMillis() - startTime);
			ReturnResponse<UploadFileResponse> uploadFileResponse = resp.getBody();
			if (isUploadSuccessful(uploadFileResponse)) {
				uploadSuccessStatus = true;
			}
		} catch (Exception e) {
			LOGGER.error(String.format(
					"Error occured while uploading BSS confirmation statement confirmation id %s company code : %s :",
					confirmationId, company.getCode()), e);
		} finally {
			try {
				if (tmpPdfFile != null && tmpPdfFile.exists()) {
					tmpPdfFile.delete();
				}
			} catch (Exception e) {
				LOGGER.error(String.format(
						"Error occured while deleting temp confirmation statement file. Confirmation #:%s Company Code:%s :",
						confirmationId, company.getCode()), e);
			}
		}

		return uploadSuccessStatus;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<String, String> retrieveConfirmationStatementUrls(String companyCode) {
		List<Map<String, String>> benDocuments = null;
		try {
			HttpEntity<String> entity = prepareRequestEntity(companyCode);
			Map<String, String> urlParams = new HashMap<>();
			urlParams.put(PATH_PARAM_COMPANY_ID, companyCode);

			final String finalDocMgmtUrl = CommonUtils.prepareFinalRequestUri(
					BSSMessageConfig.getProperty(BSSURIConstants.CONFIRMATION_STATEMENT_DOC_URI), urlParams);

			long startTime = System.currentTimeMillis();
			LOGGER.info("ApiServiceBroker::invokeRestCall Start call to url: {} and timestamp: {}", finalDocMgmtUrl,
					startTime);
			ResponseEntity<ReturnResponse> resp = restTemplate.postForEntity(finalDocMgmtUrl, entity,
					ReturnResponse.class);
			LOGGER.info("ApiServiceBroker::invokeRestCall End call to url: {} and time taken in ms: {}",
					finalDocMgmtUrl, System.currentTimeMillis() - startTime);
			benDocuments = (List) resp.getBody().getData();
		} catch (Exception ex) {
			CommonUtils.logExceptions(ex, LOGGER, companyCode, "");
		}
		Map<String, String> titleUrls = new HashMap<>();
		if (CollectionUtils.isNotEmpty(benDocuments)) {
			for (Map<String, String> benefitDocument : benDocuments) {
				// Document management returns the generic document download url which doesn't
				// check for CDM role. Appending "/bss" at the end to call BSS specific
				// download url so that CDM url can be authorized only for CDM.
				String docUrl = benefitDocument.get("url").concat("/bss");
				titleUrls.put(benefitDocument.get("title"), docUrl);
			}
		}
		return titleUrls;
	}

	private HttpEntity<String> prepareRequestEntity(String companyCode) throws JSONException {
		HttpHeaders headers = prepareHeaders();
		JSONObject requestData = prepareRequestData(companyCode);
		return new HttpEntity<>(requestData.toString(), headers);
	}

	private JSONObject prepareRequestData(String companyCode) throws JSONException {
		JSONObject requestData = null;
		Map<String, String> attributes = new HashMap<>();
		attributes.put(REQ_ATTR_COMPANY_CODE, companyCode);
		requestData = new JSONObject();
		requestData.put(REQ_DATA_QTR_PLYR_DT_ID_PARAM, Arrays.asList(-1));
		requestData.put(REQ_DATA_DOC_TYPE_PARAM, BSSApplicationConstants.CONFIRMATION_STMT_DOC_TYPE);
		requestData.put(REQ_DATA_ATTR_PARAM, attributes);
		return requestData;
	}

	private HttpHeaders prepareHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add(REQ_AUTH_TOKEN_KEY, SecurityUtils.parseAuthenticationToken(request));
		BSSSecurityUtils.addImpersonationHeaders(request, headers);
		return headers;
	}

	private HttpEntity<MultiValueMap<String, Object>> prepareRequestEntity(File tmpPdfFile, Company company,
			String confirmationId) {
		DMRequest param = prepareDMRequest(company, confirmationId);

		MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
		bodyMap.add(REQ_BODY_FILES_PARAM, new FileSystemResource(tmpPdfFile.getPath()));
		bodyMap.add(REQ_BODY_DATA_PARAM, param);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		setBasicAuthorization(headers);
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(bodyMap, headers);
		return entity;
	}

	private String prepareUploadServiceUri() {
		String uploadDocServiceUrl = AppConfig.getPlatformURL() + BSSMessageConfig.getProperty(BSSURIConstants.UPLOAD_CONFIRMATION_STATEMENT);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(uploadDocServiceUrl);
		Map<String, String> pathParams = new HashMap<>();
		uploadDocServiceUrl = uriBuilder.buildAndExpand(pathParams).toString();
		return uploadDocServiceUrl;
	}

	private DMRequest prepareDMRequest(Company company, String confirmationId) {
		Map<String, String> attributes = new HashMap<>();
		DMRequest request = new DMRequest();
		SimpleDateFormat formatter = new SimpleDateFormat(BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY);
		QuarterPlanYearDate quarterPlanYearDate = new QuarterPlanYearDate();
		List<QuarterPlanYearDate> quarterPlanYearDates = new ArrayList<>();
		attributes.put(REQ_ATTR_COMPANY_CODE, company.getCode());
		attributes.put(REQ_ATTR_FILE_NAMEN_ID, SubmitServiceHelper.generateConfirmationStmtPdfName(confirmationId));
		quarterPlanYearDate.setId(-1);
		quarterPlanYearDate.setPlanYearStartDate(formatter.format(company.getRealmPlanYear().getPlanYearStart()));
		quarterPlanYearDate.setPlanYearEndDate(formatter.format(company.getRealmPlanYear().getPlanYearEnd()));
		quarterPlanYearDates.add(quarterPlanYearDate);
		request.setExchangeId((int) company.getRealm().getId());
		request.setPlanYearEndDate(formatter.format(company.getRealmPlanYear().getPlanYearEnd()));
		request.setPlanYearStartDate(formatter.format(company.getRealmPlanYear().getPlanYearStart()));
		request.setDocTypeId(getDocumentTypeId((int) company.getRealm().getId()));
		request.setAttributes(attributes);
		request.setQuarterPlanYearDates(quarterPlanYearDates);
		return request;
	}

	private int getDocumentTypeId(int exchangeId) {
		int docTypeId = 0;
		String docTypeCode = BSSApplicationConstants.CONFIRMATION_STMT_DOC_TYPE;
		List<DocumentType> documentTypes = docTypeDao.findByDocTypeName(docTypeCode);
		for (DocumentType documentType : documentTypes) {
			if (documentType.getExchangeId() == exchangeId) {
				docTypeId = documentType.getDocumentTypeId();
			}
		}
		return docTypeId;
	}

	private boolean isUploadSuccessful(ReturnResponse<UploadFileResponse> uploadFileResponse) {
		return uploadFileResponse != null
				&& String.valueOf(BSSHttpStatusConstants.OK).equals(uploadFileResponse.getStatusCode())
				&& null != uploadFileResponse.getData()
				&& CollectionUtils.isEmpty(uploadFileResponse.getData().getErrorFiles());
	}
	
	private void setBasicAuthorization(HttpHeaders headers) {
		String clientId = BSSMessageConfig.getProperty("benefits.batch.client.id");
		String clientSecret = BSSMessageConfig.getProperty("benefits.batch.client.secret");
		String scope = BSSMessageConfig.getProperty("api.security.benefits.batch.scope");

		String auth = clientId + ":" + clientSecret;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
		String authorization = new String(encodedAuth, StandardCharsets.UTF_8);
		authorization = "Basic " + authorization;

		headers.add("Authorization", authorization);
		headers.add("scope", scope);
	}

}