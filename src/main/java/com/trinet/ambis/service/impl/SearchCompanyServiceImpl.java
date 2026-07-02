package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSURIConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.persistence.dao.ps.SearchCompanyDao;
import com.trinet.ambis.service.SearchCompanyService;
import com.trinet.ambis.service.model.SearchCompanyResultData;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.common.AppConfig;
import com.trinet.domain.common.ReturnResponse;
import com.trinet.security.util.SecurityUtils;

@Service
public class SearchCompanyServiceImpl implements SearchCompanyService {

	private static final Logger logger = LoggerFactory.getLogger(SearchCompanyServiceImpl.class);

	@Autowired
	SearchCompanyDao searchCompanyDao;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public List<SearchCompanyResultData> getSearchResults(String inputText, String loggedInCompCode, String emplid) {
		List<SearchCompanyResultData> searchResults = new ArrayList<>();

		String fundingService = getFundingService(inputText, loggedInCompCode);
		long startTime = System.currentTimeMillis();
		Map<String, String> quarterAndClientTypes = searchCompanyDao.getQuarterAndClientType(emplid);
		long endTime = System.currentTimeMillis();
		logger.info("getQuarterAndClientType QUERY TOOK {} ms", (endTime - startTime));

		if (!quarterAndClientTypes.isEmpty() && "BSS".equals(fundingService)) {

			List<String> allClientQuarterList = new ArrayList<>();
			List<String> newClientQuaterList = new ArrayList<>();
			List<String> renewalClientQuarterList = new ArrayList<>();

			Set<Entry<String, String>> entrySet = quarterAndClientTypes.entrySet();

			for (Map.Entry<String, String> entry : entrySet) {
				logger.info("Key: {} Value: {}", entry.getKey(), entry.getValue());

				/*
				 * If the user can search for both New as well as Renewal clients, make a note
				 * of that quarter
				 */
				if (entry.getValue().equalsIgnoreCase(Constants.SEARCH_CLIENT_TYPE_ALL)) {
					allClientQuarterList.add(entry.getKey());
				}

				/*
				 * If the user can search for only New clients, make a note of that quarter
				 */
				else if (entry.getValue().equalsIgnoreCase(Constants.SEARCH_CLIENT_TYPE_NEW)) {
					newClientQuaterList.add(entry.getKey());
				}

				/*
				 * If the user can search for only Renewal clients, make a note of that quarter
				 */
				else if (entry.getValue().equalsIgnoreCase(Constants.SEARCH_CLIENT_TYPE_RENEWAL)) {
					renewalClientQuarterList.add(entry.getKey());
				}
			}

			searchResults.addAll(
					getSearchResultsForClientType(inputText, allClientQuarterList, Constants.QUERY_FOR_ALL_CLIENTS));

			searchResults.addAll(
					getSearchResultsForClientType(inputText, newClientQuaterList, Constants.QUERY_FOR_NEW_CLIENTS));

			searchResults.addAll(getSearchResultsForClientType(inputText, renewalClientQuarterList,
					Constants.QUERY_FOR_RENEWAL_CLIENTS));

		}

		return searchResults;
	}
	
	private List<SearchCompanyResultData> getSearchResultsForClientType(String inputText, List<String> quarterList,
			String queryName) {

		List<SearchCompanyResultData> results = new ArrayList<>();

		if (!quarterList.isEmpty()) {
			results = searchCompanyDao.getCompanyIdAndName(inputText, quarterList, queryName);
		}
		return results;
	}


	@SuppressWarnings("unchecked")
	public String getFundingService(String searchCompanyCode, String loggedInCompCode) {
		String fundingService = "";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", "application/json");
		headers.add("token", SecurityUtils.parseAuthenticationToken(request));
		HttpEntity<?> entity = new HttpEntity<>(headers);
		ResponseEntity<ReturnResponse<Map<String, Object>>> resp = null;
		String benefitsAlertsServiceUrl = BSSMessageConfig.getProperty(BSSURIConstants.GET_BENEFIT_PROFILE);
		Map<String, Object> benefitsAlertsMap = null;
		Map<String, String> urlParams = new HashMap<>();
		urlParams.put("companyId", loggedInCompCode);
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(benefitsAlertsServiceUrl).queryParam("searchParam",
				searchCompanyCode);
		String finalBenefitsAlertsServiceUrl = AppConfig.getBenefitsServiceURL()
				+ builder.buildAndExpand(urlParams).toUriString();
		try {
			long startTime = System.currentTimeMillis();
			logger.info("ApiServiceBroker::invokeRestCall Start call to url: {} and timestamp: {}",
					finalBenefitsAlertsServiceUrl, startTime);
			resp = restTemplate.exchange(finalBenefitsAlertsServiceUrl, HttpMethod.GET, entity, 
					new ParameterizedTypeReference<ReturnResponse<Map<String, Object>>>() {});
			logger.info("ApiServiceBroker::invokeRestCall End call to url: {} and time taken in ms: {}",
					finalBenefitsAlertsServiceUrl, System.currentTimeMillis() - startTime);
			Object benefitsObj = resp.getBody().getData();
			benefitsAlertsMap = (Map<String, Object>) benefitsObj;
			if (Objects.nonNull(benefitsAlertsMap)) {
				Map<String, String> fundingBenefitProviderDetails = (Map<String, String>) (benefitsAlertsMap
						.get("fundingServiceProvider"));
				fundingService = fundingBenefitProviderDetails.get("service") != null
						? fundingBenefitProviderDetails.get("service")
						: BSSApplicationConstants.EMPTY_SPACE;
			}
			logger.info("fundingService : {}" , fundingService);
		} catch (Exception ex) {
			fundingService = "";
			logger.warn("Error from urlL {} with error message: {}", benefitsAlertsServiceUrl, ex.getMessage());
			CommonUtils.logExceptions(ex, logger, loggedInCompCode, "");
		}
		return fundingService;
	}

}
