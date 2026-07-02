package com.trinet.ambis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trinet.ambis.service.model.SearchCompanyResultData;

@Service
public interface SearchCompanyService {
	/**
	 * This method retrieves the SearchCompanyResultData objects that contain the search results for the
	 * BSS-HCC/BMG page
     *
	 * @param inputText
	 * @param loggedInCompCode
	 * @param emplid
	 * @return
	 */
	List<SearchCompanyResultData> getSearchResults(String inputText, String loggedInCompCode, String emplid);
}
