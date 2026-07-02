package com.trinet.ambis.persistence.dao.ps.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.persistence.dao.ps.SearchCompanyDao;
import com.trinet.ambis.service.model.SearchCompanyResultData;
import com.trinet.ambis.util.DaoUtils;

public class SearchCompanyDaoImpl implements SearchCompanyDao {
	
	 @PersistenceContext(unitName = "bis-sysadm")
	 private EntityManager entityManager;
	 
	 private static final Logger logger = LoggerFactory.getLogger(SearchCompanyDaoImpl.class);
	 
	 public void setEntityManager(EntityManager em) {
	        this.entityManager = em;
	    }

	    public EntityManager getEntityManager() {
	        return this.entityManager;
	    }
	
	@Override
	public Map<String, String> getQuarterAndClientType(String emplid) {

		Map<String, String> quarterAndClientTypes = new HashMap<>();

		try {
			Query query = entityManager.createNamedQuery("getQuarterAndClientType");
			query.setParameter("psEmplid", emplid);
			List<Object[]> results = DaoUtils.getResultList(query, "getQuarterAndClientType");
			for (Object[] record : results) {
				String oeQuarter = (String) record[0];
				String clientType = (String) record[1];

				quarterAndClientTypes.putIfAbsent(oeQuarter, clientType);

			}

		} catch (Exception exc) {
			logger.error("Exception encountered in SearchCompanyDaoImpl.getQuarterAndClientType",
					exc);
		}
		return quarterAndClientTypes;
	}	
	
	@Override
	public List<SearchCompanyResultData> getCompanyIdAndName(String inputText, List<String> quarterList,
			String queryName) {

		logger.info("Inside SearchCompanyDaoImpl.getCompanyCodeAndName");
		List<SearchCompanyResultData> searchCompanyResults = new ArrayList<>();

		try {
			Query query = entityManager.createNamedQuery(queryName);

			query.setParameter("inputText", inputText + "%");
			query.setParameter("quarterList", quarterList);

			List<Object[]> list = DaoUtils.getResultList(query, queryName);

			for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
				Object[] tempValue = iterator.next();
				if (tempValue != null && tempValue.length > 0) {
					logger.info("Company Code: {} , Company Name: {}", tempValue[0], tempValue[1]);
					SearchCompanyResultData record = new SearchCompanyResultData((String) tempValue[0],
							(String) tempValue[1]);
					searchCompanyResults.add(record);
				}
			}

		} catch (Exception exc) {
			logger.error(
					"Exception encountered in getCompanyIdAndName(String inputText, String quarterList, String queryName) of SearchCompanyDaoImpl",
					exc);
		}
		return searchCompanyResults;
	}
}
