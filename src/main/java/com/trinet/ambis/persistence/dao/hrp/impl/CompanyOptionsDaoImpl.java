/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.CompanyOptionsDao;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author kpamulapati
 */

public class CompanyOptionsDaoImpl implements CompanyOptionsDao {

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public EntityManager getEntityManager() {
        return this.em;
    }
    
	@Override
	public Map<String, Boolean> getPackageTypes(long realmYearId, String industryType, String state) {
		Query q = em.createNamedQuery("getTemplatePackageTypesByReamlStateIndustry");
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);
		q.setParameter(BSSQueryConstants.STATE, state);
		q.setParameter(BSSQueryConstants.INDUSTRY_TYPE, industryType);
		List<Object[]> results = DaoUtils.getResultList(q, "getTemplatePackageTypesByReamlStateIndustry");

		Map<String, Boolean> packageTypes = new HashMap<>();
		for (Object[] r : results) {
			boolean defaultTemplate = !((String) r[1]).equals(BSSApplicationConstants.ZERO);
			packageTypes.put(((String) r[0]), defaultTemplate);
		}
		return packageTypes;
	}

	@Override
	public Map<String, Map<String, List<Long>>> getDefaultPortfolios(long realmYearId, String industryType,
			String state, boolean isPickChoose ) {
		Query q = em.createNamedQuery("TEMPLATE_PORTFOLIOS_BY_IND_REGION");
		q.setParameter("REALM_YEAR_ID", realmYearId);
		q.setParameter("STATE", state);
		q.setParameter("INDUSTRYTYPE", industryType);
		q.setParameter( "pickChooseFlag", isPickChoose ? "1" : "0" );
		List<Object[]> results = DaoUtils.getResultList(q, "TEMPLATE_PORTFOLIOS_BY_IND_REGION");
		// Map<pkgType, Map<planType,List<Portfolios>>>
		Map<String, Map<String, List<Long>>> topMap = new HashMap<>();
		for (Object[] r : results) {
			String pkgType = (String) r[0];
			String planType = (String) r[1];
			Long portfolioId = ((BigDecimal) r[2]).longValue();
			if (topMap.containsKey(pkgType)) {
				Map<String, List<Long>> plansByPlanType = topMap.get(pkgType);
				if (plansByPlanType.containsKey(planType)) {
					List<Long> plans = plansByPlanType.get(planType);
					plans.add(portfolioId);
					plansByPlanType.put(planType, plans);
				} else {
					List<Long> plans = new ArrayList<>();
					plans.add(portfolioId);
					plansByPlanType.put(planType, plans);
				}
				topMap.put(pkgType, plansByPlanType);
			} else {
				List<Long> list = new ArrayList<>();
				list.add(portfolioId);
				Map<String, List<Long>> planTypePlans = new HashMap<>();
				planTypePlans.put(planType, list);
				topMap.put(pkgType, planTypePlans);
			}
		}
		return topMap;
	}

	@Override
	public Map<String, Map<String, List<String>>> getTemplateAdditionalPlans(long realmYearId,
			String industryType,String state) {
		
		Query q = em.createNamedQuery("getTemplateAdditionalPlansByPkgType");
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);
		q.setParameter(BSSQueryConstants.STATE, state);
		q.setParameter(BSSQueryConstants.INDUSTRY_TYPE, industryType);

		List<Object[]> results = DaoUtils.getResultList(q, "getTemplateAdditionalPlansByPkgType");

		Map<String, Map<String, List<String>>> topMap = new HashMap<>();

		for (Object[] r : results) {
			String pkgType = (String) r[0];
			String planType = (String) r[1];
			String benefitPlan = ((String) r[2]);

			if (topMap.containsKey(pkgType)) {
				Map<String, List<String>> plansByPlanType = topMap.get(pkgType);
				if (plansByPlanType.containsKey(planType)) {
					List<String> plans = plansByPlanType.get(planType);
					plans.add(benefitPlan);
					plansByPlanType.put(planType, plans);
				}
				else {
					List<String> plans = new ArrayList<>();
					plans.add(benefitPlan);
					plansByPlanType.put(planType, plans);
				}
				topMap.put(pkgType, plansByPlanType);
			}
			else {
				List<String> list = new ArrayList<>();
				list.add(benefitPlan);
				Map<String, List<String>> planTypePlans = new HashMap<>();
				planTypePlans.put(planType, list);
				topMap.put(pkgType, planTypePlans);
			}
		}
		return topMap;
	}

}
