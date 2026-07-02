/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;

/**
 * @author rvutukuri
 *
 */
public class PlanMappingDaoImpl implements PlanMappingDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	@Override
	public Map<String, PlanMapping> getPlanMappings(Company company, Set<String> outOfRegionPlans) {
		if(CollectionUtils.isEmpty(outOfRegionPlans)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws exception for empty list.
			outOfRegionPlans = new HashSet<>(1);
			outOfRegionPlans.add(BSSQueryConstants.PLAN_TO_EXCLUDE);
		}
		Query q = em.createNamedQuery("PLAN_MAPPING");
		q.setParameter(BSSQueryConstants.OE_QUARTER, company.getRealmPlanYear().getOeQuarter());
		q.setParameter(BSSQueryConstants.EFF_DT, company.getRealmPlanYear().getPlanYearStart());
		q.setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, outOfRegionPlans);
		Map<String, PlanMapping> planMappings = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();
		for (Object[] r : results) {
			String oldPlan = (String) r[0];
			long oldPortfolioId = ((BigDecimal) r[1]).longValue();
			String newPlan = (String) r[2];
			long newPortfolioId = ((BigDecimal) r[3]).longValue();
			String planType = (String) r[4];
			if (null != planMappings.get(oldPlan)) {
				PlanMapping vpm = planMappings.get(oldPlan);
				vpm.getNewBenefitPlans().add(newPlan);
			} else {
				PlanMapping vpm = new PlanMapping();
				vpm.setOldBenefitPlan(oldPlan);
				vpm.setOldPortfolioId(oldPortfolioId);
				vpm.setNewPortfolioId(newPortfolioId);
				vpm.setPlanType(planType);
				List<String> newBenefitPlans = new ArrayList<>();
				newBenefitPlans.add(newPlan);
				vpm.setNewBenefitPlans(newBenefitPlans);
				planMappings.put(oldPlan, vpm);
			}

		}
		return planMappings;
	}
	
	@Override
	public Map<String, PlanMapping> getPrimaryPlanMappings(Company company, Set<String> outOfRegionPlans) {
		if(CollectionUtils.isEmpty(outOfRegionPlans)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws exception for empty list.
			outOfRegionPlans = new HashSet<>(1);
			outOfRegionPlans.add(BSSQueryConstants.PLAN_TO_EXCLUDE);
		}
		Query q = em.createNamedQuery("PRIMARY_PLAN_MAPPING");
		q.setParameter(BSSQueryConstants.OE_QUARTER, company.getRealmPlanYear().getOeQuarter());
		q.setParameter(BSSQueryConstants.EFF_DT, company.getRealmPlanYear().getPlanYearStart());
		q.setParameter(BSSQueryConstants.OUT_OF_REGION_PLANS, outOfRegionPlans);
		Map<String, PlanMapping> planMappings = new HashMap<>();
		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();
		for (Object[] r : results) {
			String oldBenefitPlan = (String) r[0];
			long oldPortfolioId = ((BigDecimal) r[1]).longValue();
			String newBenefitPlan = (String) r[2];
			long newPortfolioId = ((BigDecimal) r[3]).longValue();
			String planType = (String) r[4];
			if (null != planMappings.get(oldBenefitPlan)) {
				PlanMapping vpm = planMappings.get(oldBenefitPlan);
				vpm.getNewBenefitPlans().add(newBenefitPlan);
			} else {
				PlanMapping vpm = new PlanMapping();
				vpm.setOldBenefitPlan(oldBenefitPlan);
				vpm.setOldPortfolioId(oldPortfolioId);
				vpm.setNewPortfolioId(newPortfolioId);
				vpm.setPlanType(planType);
				vpm.setNewBenefitPlan(newBenefitPlan);
				List<String> newBenefitPlans = new ArrayList<>();
				newBenefitPlans.add(newBenefitPlan);
				vpm.setNewBenefitPlans(newBenefitPlans);
				planMappings.put(oldBenefitPlan, vpm);
			}

		}
		return planMappings;
	}

	@Override
	public Map<String, List<String>> getPlanMappingsAsSimpleMap(Company company, Set<String> outOfRegionPlans) {
		Map<String, List<String>> simpleMap = new HashMap<>();
		Map<String, PlanMapping> planMappings = getPlanMappings(company, outOfRegionPlans);
		for (Entry<String, PlanMapping> planMapping : planMappings.entrySet()) {
				simpleMap.put(planMapping.getKey(), planMapping.getValue().getNewBenefitPlans());
		}
		return simpleMap;
	}

}
