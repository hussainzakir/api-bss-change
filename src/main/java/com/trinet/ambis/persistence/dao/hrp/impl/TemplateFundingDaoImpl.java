package com.trinet.ambis.persistence.dao.hrp.impl;

import static com.trinet.ambis.util.Constants.voluntaryPlanTypeList;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.persistence.dao.hrp.TemplateFundingDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.Rules;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;

public class TemplateFundingDaoImpl implements TemplateFundingDao {
	
	private static final String PKG_TYPE_LIST = "pkg_type_list";
	private static final String IND_TYPE = "ind_type";
	
	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;


	/**
	 * This method is for getting all the Benefit plans for the Template.
	 * 
	 * @param indType
	 * @param state
	 * @param pkgTypes
	 * @return set of benefit plans
	 */
	@Override
	public Map<String, Set<BenefitPlan>> getAllTemplateBenefitPlans(Company company) {
		Map<String, Set<BenefitPlan>> benefitPlansMap = new HashMap<>();
		Query query = null;
		String sqlName;
		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		if (BenExchngEnums.TRINET_III.getBenExchng().contentEquals(company.getRealm().getBenExchange())
				&& ! isPickChoose ) {
			sqlName = "TEMPLT_PORTFOLIO_PLANS_BY_IND_REGION_EXCIII";
			query = em.createNamedQuery(sqlName);
		} else {
			sqlName = "TEMPLT_PORTFOLIO_PLANS_BY_IND_REGION";
			query = em.createNamedQuery(sqlName);
		}
		query.setParameter(IND_TYPE, company.getIndustry().getIndustryType().toString());
		query.setParameter(BSSQueryConstants.STATE, company.getHeadQuatersState());
		query.setParameter(PKG_TYPE_LIST, Constants.PKG_TYPES);
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYear().getId());
		if (company.isTexasSitus()) {
			query.setParameter(BSSQueryConstants.SITUS, "TX");
		} else {
			query.setParameter(BSSQueryConstants.SITUS, "FL");
		}
		List<Object[]> results = DaoUtils.getResultList(query, sqlName);
		int id = 0;
		for (Object[] result : results) {
			BenefitPlan benefitPlan = new BenefitPlan();
			benefitPlan.setId("" + id++);
			benefitPlan.setPlanCarrierId(((BigDecimal) result[0]).longValue());
			benefitPlan.setName((String) result[1]);
			benefitPlan.setEstimatedTotalCost(BigDecimal.ZERO);
			benefitPlan.setPlanType((String) result[2]);
			benefitPlan.setStrategyId(0L);
			benefitPlan.setPremium(Utils.isPremium(benefitPlan.getId()));
			benefitPlan.setEmployeePaid(voluntaryPlanTypeList.contains(benefitPlan.getPlanType()));
			benefitPlan.setAnnualCap(Rules.getAnnualCap(benefitPlan.getId()));
			String pkgType = (String) result[3];
			String pkg = null;
			if (pkgType.equalsIgnoreCase("PRM")) {
				pkg = Constants.TOP_QUALITY_NAME;
			} else if (pkgType.equalsIgnoreCase("CON")) {
				pkg = Constants.CONSERVATIVE_PACKAGE_NAME;
			} else if (pkgType.equalsIgnoreCase("INT")) {
				pkg = Constants.BALANCED_PACKAGE_NAME;
			}
			if (benefitPlansMap.get(pkg) != null) {
				Set<BenefitPlan> plans = benefitPlansMap.get(pkg);
				plans.add(benefitPlan);
				benefitPlansMap.put(pkg, plans);
			} else {
				Set<BenefitPlan> plans = new TreeSet<>();
				plans.add(benefitPlan);
				benefitPlansMap.put(pkg, plans);
			}
		}
		return benefitPlansMap;
	}

	/**
	 * 
	 */
	@Override
	public Map<String, List<String>> getTemplateHeadCountPlans(Company company, String pkgType) {
		Query query = null;
		String sqlName;
		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		if (BenExchngEnums.TRINET_III.getBenExchng().contentEquals(company.getRealm().getBenExchange())
				&& ! isPickChoose ) {
			sqlName = "TEMPLT_PORTFOLIO_PLANS_BY_IND_REGION_EXCIII";
		} else {
			sqlName = "TEMPLT_PORTFOLIO_PLANS_BY_IND_REGION";
		}
		query = em.createNamedQuery(sqlName);
		query.setParameter(IND_TYPE, company.getIndustry().getIndustryType().toString());
		query.setParameter(BSSQueryConstants.STATE, company.getHeadQuatersState());
		query.setParameter(PKG_TYPE_LIST, pkgType);
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYear().getId());
		if (company.isTexasSitus()) {
			query.setParameter(BSSQueryConstants.SITUS, "TX");
		} else {
			query.setParameter(BSSQueryConstants.SITUS, "FL");
		}
		List<Object[]> results = DaoUtils.getResultList(query, sqlName);
		Map<String, List<String>> headCoutPlanMap = new HashMap<>();
		for (Object[] result : results) {
			String planType = (String) result[2];
			String plan = (String) result[1];
			List<String> list = null;
			if (headCoutPlanMap.containsKey(planType)) {
				list = headCoutPlanMap.get(planType);
			} else {
				list = new ArrayList<>();
			}
			list.add(plan);
			headCoutPlanMap.put(planType, list);
		}
		return headCoutPlanMap;
	}
	
	
	@Override
	public Map<String, List<PlanPackage>> getAllTemplateFundingDetails(String indType, String state,
			List<String> pkgTypes, long realmYearId, boolean isTexasSitus) {
		Map<String, List<PlanPackage>> planPackagesMap = new HashMap<>();
		Query query = em.createNamedQuery("getFundingByIndtypePkgtypeState");
		query.setParameter(IND_TYPE, indType);
		query.setParameter(BSSQueryConstants.STATE, state);
		query.setParameter(PKG_TYPE_LIST, pkgTypes);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmYearId);
		if (isTexasSitus) {
			query.setParameter(BSSQueryConstants.SITUS, "TX");
		} else {
			query.setParameter(BSSQueryConstants.SITUS, "FL");
		}
		List<Object[]> results = DaoUtils.getResultList(query, "getFundingByIndtypePkgtypeState");

		for (Object[] result : results) {
			Long templateId = null;
			String planType = (String) result[2];
			String planTypeDesc = null;
			String pkgType = (String) result[3];
			String pkgTypeDesc = null;

			if ("CON".equals(pkgType)) {
				pkgTypeDesc = Constants.CONSERVATIVE_PACKAGE_NAME;
				templateId = 1L;
			} else if ("INT".equals(pkgType)) {
				pkgTypeDesc = Constants.BALANCED_PACKAGE_NAME;
				templateId = 2L;
			} else {
				pkgTypeDesc = Constants.TOP_QUALITY_NAME;
				templateId = 3L;
			}

			if (Constants.medicalPlanTypeList.contains(planType)) {
				planTypeDesc = Constants.MEDICAL;
			} else if (Constants.dentalPlanTypeList.contains(planType)) {
				planTypeDesc = Constants.DENTAL;
			} else {
				planTypeDesc = Constants.VISION;
			}

			if (null != planPackagesMap.get(planTypeDesc)) {
				List<PlanPackage> pks = planPackagesMap.get(planTypeDesc);
				boolean packageExists = false;
				for (PlanPackage pk : pks) {
					if (pk.getName().equals(pkgTypeDesc)) {
						pk.getCoverageLevelFunding().put((String) result[7], ((BigDecimal) result[6]));
						packageExists = true;
					}
				}
				if (!packageExists) {
					PlanPackage pk = new PlanPackage();
					pk.setId(0);
					pk.setStrategyId(0L);
					pk.setFundingType((String) result[5]);
					pk.setPlanType(planType);
					pk.setTemplateId(templateId);
					pk.setId(templateId);
					pk.setName(pkgTypeDesc);
					pk.setPlanType(planType);
					pk.setCompanyId(0L);
					pk.setEmployeePaid(Constants.voluntaryPlanTypeList.contains(planType));
					
					List<String> plans = new ArrayList<>();
					plans.add((String) result[1]);
					pk.setFundingBasePlanList(plans);
					pk.getCoverageLevelFunding().put((String) result[7], ((BigDecimal) result[6]));
					pks.add(pk);
				}
			} else {
				PlanPackage pk = new PlanPackage();
				pk.setId(0);
				pk.setStrategyId(0L);
				pk.setFundingType((String) result[5]);
				pk.setPlanType(planType);
				pk.setTemplateId(templateId);
				pk.setId(templateId);
				pk.setName(pkgTypeDesc);
				pk.setPlanType(planType);
				pk.setCompanyId(0L);
				pk.setEmployeePaid(Constants.voluntaryPlanTypeList.contains(planType));

				List<String> plans = new ArrayList<>();
				plans.add((String) result[1]);
				pk.setFundingBasePlanList(plans);
				pk.getCoverageLevelFunding().put((String) result[7], ((BigDecimal) result[6]));
				List<PlanPackage> pks = new ArrayList<>();
				pks.add(pk);
				planPackagesMap.put(planTypeDesc, pks);
			}
		}
		return planPackagesMap;
	}
	
	@Override
	public List<String> getAlternateHeadCountPlans(Company company, List<String> headCountPlans) {
		Query query = null;
		String sqlName;
		if (CollectionUtils.isEmpty(headCountPlans)) {
			headCountPlans = new ArrayList<>();
			headCountPlans.add(BSSApplicationConstants.EMPTY_SPACE);
		}
		sqlName = "TEMPLT_ALTR_HC_PLANS_EXCIII";
		query = em.createNamedQuery(sqlName);
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYear().getId());
		query.setParameter("benefit_plans", headCountPlans);
		@SuppressWarnings("unchecked")
		List<String> results = query.getResultList();
		return results;
	}

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	public EntityManager getEntityManager() {
		return this.em;
	}

}
