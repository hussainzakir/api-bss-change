package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployeeSelectionDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;

public class EmployeeSelectionDaoImpl implements EmployeeSelectionDao {
	
	private static final Logger logger = LoggerFactory.getLogger(EmployeeSelectionDaoImpl.class);

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager hrpEntityManager;

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public Map<String, Integer> getEmployeesByBG(Company company, Date effDate) {
		Map<String, Integer> employeeCountByBG = new HashMap<>();
		try {

			Query query = entityManager.createNamedQuery("DISTINCT_EMPLOYEES_BY_BENEFITGROUP");
			query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
			query.setParameter(BSSQueryConstants.EFF_DATE, effDate);
			query.setParameter(BSSQueryConstants.PLAN_TYPES, Constants.planTypes);

			List<Object[]> results = DaoUtils.getResultList(query, "DISTINCT_EMPLOYEES_BY_BENEFITGROUP");
			for (Object[] result : results) {
				String benefitGroup = (String) result[0];
				int distinctEmployees = ((BigDecimal) result[1]).intValue();
				employeeCountByBG.put(benefitGroup, distinctEmployees);
			}
		} catch (NoResultException ex) {
			CommonUtils.logExceptions(ex, logger, company.getCode(), "");
		}
		return employeeCountByBG;
	}

	@Override
	public Map<String, BenefitPlan> getRealmPlanYearBenefitPlans(List<Long> planYears) {
		Map<String, BenefitPlan> mapOfBenefitPlans = new HashMap<>();
		try {
			Query query = hrpEntityManager.createNamedQuery("realmPlanYearPlans");
			query.setParameter("planYears", planYears);
			List<Object[]> results = DaoUtils.getResultList(query, "realmPlanYearPlans");
			for (Object[] result : results) {
				String benefitPlanResult = (String) result[0];
				String planType = (String) result[1];
				long portfolioId = ((BigDecimal) result[2]).longValue();
				boolean highDeductiblePlan = BigDecimal.ONE.equals((BigDecimal) result[3]);
				if (!mapOfBenefitPlans.containsKey(benefitPlanResult)) {
					BenefitPlan benefitPlan = new BenefitPlan();
					//benefitPlan.setAmbisId(benefitPlanResult);
					benefitPlan.setPlanType(planType);
					benefitPlan.setPlanCarrierId(portfolioId);
					benefitPlan.setHighDeductible(highDeductiblePlan);
					mapOfBenefitPlans.put(benefitPlanResult, benefitPlan);
				}
			}
		} catch (NoResultException ex) {
			CommonUtils.logExceptions(ex, logger,"", "");
		}
		return mapOfBenefitPlans;
	}

	/**
	 * @param hrpEntityManager the hrpEntityManager to set
	 */
	public void setHrpEntityManager(EntityManager hrpEntityManager) {
		this.hrpEntityManager = hrpEntityManager;
	}
}
