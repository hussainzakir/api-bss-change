package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.OLPProcessStatus;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

public class HrpDaoImpl implements HrpDao {
	
	private static final Logger logger = LoggerFactory.getLogger(HrpDaoImpl.class);

	public static final String GET_CURRENT_FUTURE_BENEFIT_PLANS = "GET_CURRENT_FUTURE_BENEFIT_PLANS";

	/**
	 * Builds the PL/SQL DELETE block for strategy data, inlining the numeric companyId
	 * directly to avoid Hibernate's named-parameter parser choking on Oracle's := operator.
	 */
	private static String buildDeleteStrategiesSql(long companyId) {
		return "DECLARE " +
			"  CURSOR strategies IS SELECT id FROM Xbss_Strategy WHERE company_id = " + companyId + "; " +
			"BEGIN " +
			"  FOR id IN strategies LOOP " +
			"    DELETE FROM Xbss_Contribution WHERE plan_selection_id IN " +
			"      (SELECT Id FROM xbss_strategy_group_planselect WHERE Strategy_Id = id.Id); " +
			"    DELETE FROM xbss_strategy_group_planselect WHERE strategy_id = id.id; " +
			"    DELETE FROM xbss_strategy_funding_detail WHERE strategy_funding_id IN " +
			"      (SELECT id FROM xbss_strategy_funding_model WHERE strategy_id = id.id); " +
			"    DELETE FROM XBSS_STRATG_FUND_BSUPP_PLN_TP WHERE strategy_funding_id IN " +
			"      (SELECT id FROM xbss_strategy_funding_model WHERE strategy_id = id.id); " +
			"    DELETE FROM XBSS_STRATEGY_FUNDING_FLTMAX WHERE strategy_funding_id IN " +
			"      (SELECT id FROM xbss_strategy_funding_model WHERE strategy_id = id.id); " +
			"    DELETE FROM xbss_strategy_funding_model WHERE strategy_id = id.id; " +
			"    DELETE FROM XBSS_STRATEGY_ESTIMATE WHERE strategy_id = id.id; " +
			"    DELETE FROM XBSS_EMPLOYEE_STRATEGY_GROUP WHERE STRATEGY_GROUP_ID IN " +
			"      (SELECT id FROM XBSS_STRATEGY_GROUP WHERE STRATEGY_ID = id.id); " +
			"    DELETE FROM XBSS_EMPLOYEE_STRATEGY_GROUP_TRNX WHERE STRATEGY_GROUP_ID IN " +
			"      (SELECT id FROM XBSS_STRATEGY_GROUP WHERE STRATEGY_ID = id.id); " +
			"    DELETE FROM XBSS_STRATEGY_GROUP_COV_HC WHERE STRATEGY_GROUP_ID IN " +
			"      (SELECT id FROM XBSS_STRATEGY_GROUP WHERE STRATEGY_ID = id.id); " +
			"    DELETE FROM XBSS_STRATEGY_HSA_FUNDING WHERE STRATEGY_ID = id.id; " +
			"    DELETE FROM XBSS_STRATEGY_GROUP WHERE STRATEGY_ID = id.id; " +
			"    DELETE FROM XBSS_EE_PLAN_ASSIGNMENT WHERE STRATEGY_ID = id.id; " +
			"    DELETE FROM Xbss_Strategy WHERE Id = id.Id; " +
			"  END LOOP; " +
			"END;";
	}

	/**
	 * Builds the PL/SQL DELETE block for company-level data, inlining the numeric companyId
	 * directly to avoid Hibernate's named-parameter parser choking on Oracle's := operator.
	 */
	private static String buildDeleteCompanyDataSql(long companyId) {
		return "BEGIN " +
			"  DELETE FROM XBSS_SCHED_MID_YEAR_FUNDING WHERE company_id = " + companyId + "; " +
			"  DELETE FROM XBSS_COMPANY_REGIONS WHERE company_id = " + companyId + "; " +
			"  DELETE FROM XBSS_GROUP_RATE WHERE group_id IN " +
			"    (SELECT id FROM xbss_group WHERE company_id = " + companyId + "); " +
			"  DELETE FROM XBSS_EE_DEFAULT_PLAN_ASSIGNMENT WHERE company_id = " + companyId + "; " +
			"  DELETE FROM xbss_group_cov_headcount WHERE group_id IN " +
			"    (SELECT id FROM xbss_group WHERE company_id = " + companyId + "); " +
			"  DELETE FROM xbss_group WHERE company_id = " + companyId + "; " +
			"  DELETE FROM XBSS_EMPLOYEE WHERE COMPANY = " +
			"    (SELECT code FROM xbss_company WHERE id = " + companyId + "); " +
			"END;";
	}

	private static String buildDeleteCompanySql(long companyId) {
		return "BEGIN " +
			"  DELETE FROM XBSS_BAND_CODES WHERE company_id = " + companyId + "; " +
			"  DELETE FROM XBSS_COMPANY WHERE ID = " + companyId + "; " +
			"END;";
	}

	@PersistenceContext(unitName="bis-hrp")
	EntityManager em;

	@Autowired
	PsCompanyDao psCompanyDao;

	@Override
	public String getEmplEmail(String companyCode, String emplId) {
		String emailAddress = null;
		Set<String> emailSet = getEmails(companyCode, emplId, "EE");
		if (!emailSet.isEmpty()) {
			emailAddress = emailSet.iterator().next();
		}
		return emailAddress;
	}
	
	@Override
	public Set<String> getBDMEmails(String companyCode){
		return getEmails(companyCode, "", "CDM");
	}
	
	@Override
	public Set<String> getRoleEmails(String companyCode, String role){
		return getEmails(companyCode, "", role);
	}
	
	/**
	 * Returns a set of email addresses for employees assigned the role for the
	 * passed in company
	 * 
	 * @param companyCode
	 * @param emplId
	 * @param role
	 * @return
	 */
	private Set<String> getEmails(String companyCode, String emplId, String role) {
		Set<String> emailList = new HashSet<>();
		Query q = em.createNamedQuery("GET_EMPLOYEE_OR_ROLE_EMAIL");
		q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		q.setParameter("role", role);
		q.setParameter(BSSQueryConstants.EMPL_ID, (emplId == null ? "" : emplId));
		List<Object[]> results = DaoUtils.getResultList(q, "GET_EMPLOYEE_OR_ROLE_EMAIL");
		if (results != null) {
			for (Object[] result : results) {
				String emplIdResult = (String) result[0];
				String email = (String) result[1];
				if (psCompanyDao.isActiveWithCompany(emplIdResult, companyCode)) {
					emailList.add(email);
				}
			}
		}
		return emailList;
	}

	@Override
	public Map<String, Integer> getBDMCount(List<String> companies) {
		return getRoleEmailCount(companies, "CDM");
	}

	@Override
	public Map<String, Integer> getBenCorpAdminCount(List<String> companies) {
		return getRoleEmailCount(companies, "BEN_CORP_AD");
	}
	
	@Override
	public Map<String, Integer> getRoleEmailCount(List<String> companies, String role) {
		final int COMPANY_INDEX = 0;
		final int COUNT_INDEX = 1;
		Map<String, Integer> bdmCounts = new HashMap<>();
		final Collection<List<String>> bucketedList = CommonUtils.getBucketedList(companies,
				BSSApplicationConstants.QUERY_IN_CLAUSE_PARTITION_SIZE);
		
		for (List<String> bucket : bucketedList) {
			Query q = em.createNamedQuery("GET_ROLE_EMAIL_COUNT");
			q.setParameter("companies", bucket);
			q.setParameter("role", role);

			List<Object[]> results = DaoUtils.getResultList(q, "GET_ROLE_EMAIL_COUNT");
			if (results != null) {
				for (Object[] result : results) {
					String company = (String) result[COMPANY_INDEX];
					Integer bdmCount = ((BigDecimal) result[COUNT_INDEX]).intValue();
					bdmCounts.put(company, bdmCount);
				}
			}
		}
		return bdmCounts;
	}

	@Override
	public Map<String,String> getCovrgCdMap() {
		// using LinkedHashMap to preserve order of entries in the map
		Map<String,String> map = new LinkedHashMap<>();
		Query q = em.createNativeQuery("SELECT COVRG_ID, DESCR FROM XBSS_CVG_CODES ORDER BY SORT_ORDER");
		List<Object[]> results = DaoUtils.getResultList(q, "SELECT COVRG_ID, DESCR FROM XBSS_CVG_CODES ORDER BY SORT_ORDER");

		if (results != null) {
			for (Object[] result : results) {
				String covrgId = (String) result[0];
				String descr = (String) result[1];
				map.put( covrgId, descr );
			}
		}
		return map;
	}

	@Override
	public void refreshPlanView() {
		Query q = em.createNativeQuery("BEGIN DBMS_MVIEW.REFRESH('XBSS_BENEF_PLAN_MV', 'C', '', TRUE, FALSE, 0,0,0, FALSE, FALSE); END; ");
		DaoUtils.executeUpdate(q, "DBMS_MVIEW.REFRESH SP");
	}

	@Override
	public Set<String> getGatewayAppAccessibleRolesBy(String appKey) {
		Set<String> roles = null;
		Query q = em.createNamedQuery("FIND_GATEWAY_APP_ACCESSIBLE_ROLES");
		q.setParameter("appKey", appKey);
		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();
		if (CollectionUtils.isNotEmpty(results)) {
			roles = new HashSet<>(results.size());
			for (Object[] result : results) {
				String role = (String) result[1];
				roles.add(role);
			}
		}
		return roles;
	}

	@Override
	public int getOLPStatus(Company company) {

		Query query = em.createNamedQuery("GET_OLP_STATUS");
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());

		BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "GET_OLP_STATUS");

		if (count == null)
			return 0;
		else
			return count.intValue();
	}	
	
	@Override
	public OLPProcessStatus getOlpHiringCompletedStatus(Company company) {
		Query query = em.createNamedQuery("GET_OLP_HIRING_COMPLETED_STATUS");
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		List<Object[]> result = DaoUtils.getResultList(query, "GET_OLP_HIRING_COMPLETED_STATUS");
		OLPProcessStatus olpProcessStatus = null;
		if (result != null && !result.isEmpty()) {
			Object[] tempValue = result.get(0);
			olpProcessStatus = new OLPProcessStatus();
			olpProcessStatus.setOlpId(((BigDecimal) tempValue[0]).longValue());
			olpProcessStatus.setStatus(((BigDecimal) tempValue[1]).intValue());
			Timestamp ts = (Timestamp) tempValue[2];
			java.util.Date date = new java.util.Date(ts.getTime());
			olpProcessStatus.setUpdateDate(date);
		}
		return olpProcessStatus;
	}
	
	@Override
	public Map<String,String> getZipCodesAndStatesBy(List<String> zipCodes){
		Map<String,String> zipSatesMap = new HashMap<>();
		Query q = em.createNamedQuery("GET_ZIPCODES_STATES_LIST");
		q.setParameter("zipcodes", zipCodes);
		
		List<Object[]> results = DaoUtils.getResultList(q, "GET_ZIPCODES_STATES_LIST");
		if (CollectionUtils.isNotEmpty(results)) {
			for (Object[] result : results) {
				String zipCode = (String) result[0];
				String state = (String) result[1];
				zipSatesMap.put(zipCode, state);
			}
		}
		return zipSatesMap;
	}
	
	@Override
	public Map<String, String> getCurrentFutureBenefitPlansMap(Long oldPlanYear, Date oldPlanYearEffDt,
			Long newPlanYear, Date newPlanYearEffDt, boolean applySitus) {
		Map<String, String> currentFutureBenefitPlansMap = new HashMap<>();
		Query q = em.createNamedQuery(GET_CURRENT_FUTURE_BENEFIT_PLANS);
		q.setParameter("oldPlanYear", oldPlanYear);
		q.setParameter("oldPlanYearEffDt", oldPlanYearEffDt);
		q.setParameter("newPlanYear", newPlanYear);
		q.setParameter("newPlanYearEffDt", newPlanYearEffDt);
		q.setParameter("applySitus", applySitus ? "1" : "0");

		List<Object[]> results = DaoUtils.getResultList(q, GET_CURRENT_FUTURE_BENEFIT_PLANS);
		if (CollectionUtils.isNotEmpty(results)) {
			for (Object[] result : results) {
				String currentBenefitPlan = (String) result[0];
				String futureBenefitPlan = (String) result[1];
				currentFutureBenefitPlansMap.put(currentBenefitPlan, futureBenefitPlan);
			}
		}
		return currentFutureBenefitPlansMap;
	}

	@Override
	public void deleteStrategiesByCompanyId(long companyId) {
		logger.info("deleteStrategiesByCompanyId() - start for companyId: {}", companyId);
		Query q = em.createNativeQuery(buildDeleteStrategiesSql(companyId));
		DaoUtils.executeUpdate(q, "deleteStrategiesByCompanyId");
		logger.info("deleteStrategiesByCompanyId() - completed for companyId: {}", companyId);
	}

	@Override
	public void deleteCompanyDataByCompanyId(long companyId) {
		logger.info("deleteCompanyDataByCompanyId() - start for companyId: {}", companyId);
		Query q = em.createNativeQuery(buildDeleteCompanyDataSql(companyId));
		DaoUtils.executeUpdate(q, "deleteCompanyDataByCompanyId");
		logger.info("deleteCompanyDataByCompanyId() - completed for companyId: {}", companyId);
	}

	@Override
	public void deleteCompanyByCompanyId(long companyId) {
		logger.info("deleteCompanyByCompanyId() - start for companyId: {}", companyId);
		Query q = em.createNativeQuery(buildDeleteCompanySql(companyId));
		DaoUtils.executeUpdate(q, "deleteCompanyByCompanyId");
		logger.info("deleteCompanyByCompanyId() - completed for companyId: {}", companyId);
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