package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author mpulipaka
 *
 */
public class EmployerEmployeePlansMappingDaoImpl implements EmployerEmployeePlansMappingDao {

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;
	
    private static final String GET_EE_ER_PLANS_MAPPING = "getEeErPlansMapping";

	@Override
	public int getEmployerEmployeePlansMappingByRealmYearIdOfferedCount(long realmPlanYear) {
		Query q = em.createNamedQuery("getEeErPlansMappingCount");
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmPlanYear);
		int employerEmployeePlansMappingByRealmYearIdOfferedCount = 0;
		Object result = DaoUtils.getSingleResult(q, "getEeErPlansMappingCount");
		if (result != null) {
			employerEmployeePlansMappingByRealmYearIdOfferedCount = ((BigDecimal) result).intValue();
		}
		return employerEmployeePlansMappingByRealmYearIdOfferedCount;
	}

	@Override
	public Map<BenefitPlan, BenefitPlan> getEmployerEmployeePlansMappingByRealmYearId(long realmPlanYear) {
		Query q = em.createNamedQuery(GET_EE_ER_PLANS_MAPPING);
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmPlanYear);
		List<Object[]> results = DaoUtils.getResultList(q, GET_EE_ER_PLANS_MAPPING);
		Map<BenefitPlan, BenefitPlan> eeErPaidMap = new HashMap<>(results.size());
		if (!results.isEmpty()) {
			for (Object[] result : results) {
				String employerPaidPlanType = String.valueOf(result[0]);
				String employerPaidBenefitPlan = (String) result[1];
				String employeePaidPlanType = (String) result[2];
				String employeePaidBenefitPlan = (String) result[3];
				BenefitPlan employerPaidPlan = new BenefitPlan();
				BenefitPlan employeePaidPlan = new BenefitPlan();
				employerPaidPlan.setId(employerPaidBenefitPlan);
				employerPaidPlan.setPlanType(employerPaidPlanType);
				employeePaidPlan.setId(employeePaidBenefitPlan);
				employeePaidPlan.setPlanType(employeePaidPlanType);

				if (!eeErPaidMap.containsKey(employerPaidPlan)) {
					eeErPaidMap.put(employerPaidPlan, employeePaidPlan);
				}
				if (!eeErPaidMap.containsKey(employeePaidPlan)) {
					eeErPaidMap.put(employeePaidPlan, employerPaidPlan);
				}

			}
		}
		return eeErPaidMap;
	}

	@Override
	public Map<String, String> getEeAndErPlanMapping(long realmPlanYear) {
		Query q = em.createNamedQuery(GET_EE_ER_PLANS_MAPPING);
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmPlanYear);
		List<Object[]> results = DaoUtils.getResultList(q, GET_EE_ER_PLANS_MAPPING);

		Map<String, String> eeErPaidMap = new HashMap<>(results.size());
		for (Object[] result : results) {
			eeErPaidMap.put((String) result[1], (String) result[3]);
			eeErPaidMap.put((String) result[3], (String) result[1]);
		}
		return eeErPaidMap;
	}

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	public EntityManager getEntityManager() {
		return this.em;
	}

}
