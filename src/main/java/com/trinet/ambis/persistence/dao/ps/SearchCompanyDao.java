package com.trinet.ambis.persistence.dao.ps;

import java.util.List;
import java.util.Map;

import com.trinet.ambis.service.model.SearchCompanyResultData;

public interface SearchCompanyDao {

	/*
	 * Method that returns the quarters and associated types of clients that the
	 * user can search for
	 */
	Map<String, String> getQuarterAndClientType(String emplid);

	/*
	 * Method that returns the SearchCompanyResultData objects comprising the search
	 * results
	 */
	List<SearchCompanyResultData> getCompanyIdAndName(String inputText, List<String> peoIDList, String queryName);
}
