package com.trinet.ambis.persistence.dao.ps.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

public class PsDaoImpl implements PsDao {

	@PersistenceContext(unitName = "bis-sysadm")
	EntityManager em;

	@Autowired
	PsCompanyDao psCompanyDao;

	private static final Logger logger = LoggerFactory.getLogger(PsDaoImpl.class);

	@Override
	public String getEmployeeFirstName(String emplId) {
		Query q = em.createNamedQuery("GET_EMPLOYEE_FIRST_NAME");
		q.setParameter("emplId", emplId);
		String firstName = StringUtils.EMPTY;
		List<String> results = DaoUtils.getResultStringList(q, "GET_EMPLOYEE_FIRST_NAME");
		for (String s : results) {
			firstName = s;
		}
		return firstName.trim();
	}
	
	@Override
	public String getEmployeeLastName(String emplId) {
		String lastName = StringUtils.EMPTY;
		Query q = em.createNamedQuery("GET_EMPLOYEE_LAST_NAME");
		q.setParameter("emplId", emplId);
		List<String> lastNames = DaoUtils.getResultStringList(q, "GET_EMPLOYEE_LAST_NAME");
		if (CollectionUtils.isNotEmpty(lastNames)) {
			lastName = lastNames.get(0);
		}
		return lastName.trim();
	}

	@Override
	public String getEmployeeFullName(String emplId) {
		Map<String, String> fullNameMap = this.getEmployeesFullName(new HashSet<>(Arrays.asList(emplId)));
		return fullNameMap.get(emplId);
	}

	@Override
	public Map<String, String> getEmployeesFullName(Set<String> emplIds) {
		Query q = em.createNamedQuery("GET_EMPLOYEES_FULL_NAME");
		q.setParameter("emplIds", emplIds);
		
		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();

		Map<String, String> emplNames = new HashMap<>();
		for (Object[] result : results) {
			String emplid = (String) result[0];
			String emplFullName = (String) result[1];
			emplNames.put(emplid, emplFullName);
		}
		return emplNames;
	}

	public List<String> getAssignmentAddresses(Company company) {
		Query q = em.createNamedQuery("CLIENT_ASSIGNMENT_EMAILS");
		q.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		List<String> assignments = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, "CLIENT_ASSIGNMENT_EMAILS");

		if (!results.isEmpty()) {
			for (Object[] result : results) {
				String csaEmplid = (String) result[0];
				String csaEmail = (String) result[1];
				if( csaEmail != null && psCompanyDao.isCSAUser( csaEmplid, company.getRealm().getId()) ) {
					assignments.add( csaEmail );
				}
			}
		}

		return assignments;
	}

	@Override
	public List<String> getUnsubmittedClients(String quarter, Long realmYearId, Date payrollCutOffDate, String termStatus) {
		List<String> clientList = new ArrayList<>();
		try {
			Query q = null;
			String sqlName = "UNSUBMITTED_BSS_CLIENTS";
			List<String> productLines = BSSApplicationConstants.EXCULDED_PRODUCT_LINES_EXCH_XI;
			q = em.createNamedQuery(sqlName);
			q.setParameter("quarter", quarter);
			q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);
			q.setParameter("payrollCutOffDate", payrollCutOffDate);
			q.setParameter("termStatus", termStatus);
			q.setParameter("productLine", productLines);
			clientList = DaoUtils.getResultStringList(q, sqlName);
		} catch (NoResultException nre) {
			CommonUtils.logExceptions(nre, logger, "001", "");
		}
		return clientList;
	}
	
	@Override
	public List<String> getPreLoadClients(String peoId, String quarter, Long realmYearId, Date payrollCutOffDate) {
		List<String> clientList = new ArrayList<>();
		try {
			Query q = null;
			String sqlName = "PRE_LOAD_BSS_CLIENTS";
			q = em.createNamedQuery(sqlName);
			q.setParameter("peoId", peoId);
			q.setParameter("quarter", quarter);
			q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);
			q.setParameter("payrollCutOffDate", payrollCutOffDate);
			q.setParameter("productLine", BSSApplicationConstants.EXCULDED_PRODUCT_LINES_EXCH_XI);
			clientList = DaoUtils.getResultStringList(q, sqlName);
			
		} catch (NoResultException nre) {
			CommonUtils.logExceptions(nre, logger, "001", "");
		}
		return clientList;
	}

	@Override
	public String getDatabase() {
		Query q = em.createNamedQuery("GET_DATABASE");
		List<String> dbName = DaoUtils.getResultStringList(q, "GET_DATABASE");
		return dbName.get(0);
	}
	
	@Override
	public List<String> getNewClientAddresses(Company company) {
		Query q = em.createNamedQuery("NEW_CLIENT_ASSIGNMENT_EMAILS");
		q.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		List<String> assignments = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, "NEW_CLIENT_ASSIGNMENT_EMAILS");

		if (!results.isEmpty()) {
			for (Object[] result : results) {
				String svcEmplid = (String) result[0];
				String svcEmail = (String) result[1];
				if( svcEmail != null && psCompanyDao.isCSAUser( svcEmplid, company.getRealm().getId() )) {
					assignments.add( svcEmail );
				}
			}
		}
		return assignments;
	}
	

	
	@Override
	public Map<String, BigDecimal> getHsaMaximumsByEffDate(Date effDate) {
		Query q = em.createNamedQuery("GET_HSA_MAXIMUMS");
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		List<Object[]> results = DaoUtils.getResultList(q, "GET_HSA_MAXIMUMS");		
		
		Map<String, BigDecimal> maximumMap = new HashMap<>(results.size());

		if (!results.isEmpty()) {
			for (Object[] result : results) {
				String key = (String) result[0];
				BigDecimal maximum = (BigDecimal) result[1]; 
				maximumMap.put(key, maximum);
			}
		}
		return maximumMap;
	}

	/**
	 * @param em the em to set
	 */
	public void setEm(EntityManager em) {
		this.em = em;
	}

	/**
	 * @param psCompanyDao the psCompanyDao to set
	 */
	public void setPsCompanyDao(PsCompanyDao psCompanyDao) {
		this.psCompanyDao = psCompanyDao;
	}
}
