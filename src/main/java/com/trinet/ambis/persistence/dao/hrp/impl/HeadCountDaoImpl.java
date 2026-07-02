package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.HeadCountDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.MappedHeadCount;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

public class HeadCountDaoImpl implements HeadCountDao {

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Override
	public Map<String, Integer> getEmployeeCountByBenefitGroup(String companyCode, Long realmPlanYearId) {
		Map<String, Integer> employeeCountByBG = new HashMap<>();

		Query query = em.createNamedQuery("EMPLOYEE_COUNT_BY_BENEFIT_GROUP");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);

		List<Object[]> results = DaoUtils.getResultList(query, "EMPLOYEE_COUNT_BY_BENEFIT_GROUP");
		for (Object[] result : results) {
			String benefitGroup = (String) result[0];
			int distinctEmployees = ((BigDecimal) result[1]).intValue();
			employeeCountByBG.put(benefitGroup, distinctEmployees);
		}
		return employeeCountByBG;
	}

	@Override
	public Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getPlanCoverageLevelHeadCountByGroup(
			String companyCode, Long realmPlanYearId, boolean isHeadCountMapped) {
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> headCountMap = new HashMap<>();
		String queryString = null;
		if (isHeadCountMapped) {
			queryString = "EMPLOYEE_COUNT_BY_BENEFIT_GROUP_MAPPED";
		} else {
			queryString = "EMPLOYEE_COUNT_BY_BENEFIT_GROUP_PLAN_LEVEL";
		}
		Query query = em.createNamedQuery(queryString);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);

		List<Object[]> results = DaoUtils.getResultList(query, queryString);

		for (Object[] r : results) {
			String groupName = (String) r[0];
			String planType = (String) r[1];
			String plan = (String) r[2];
			String covrgCode = (String) r[3];
			int headCount = ((BigDecimal) r[4]).intValue();
			int hsaHeadCount = ((BigDecimal) r[5]).intValue();

			if (Constants.primaryPlanTypeList.contains(planType)) {
				PlanCoverageLevelHeadCount covrgHeadCount = new PlanCoverageLevelHeadCount();

				covrgHeadCount.setGroupName(groupName);
				covrgHeadCount.setPlanType(planType);
				covrgHeadCount.setBenefitPlan(plan);
				covrgHeadCount.setCovrgCode(covrgCode);
				covrgHeadCount.setHeadCount(headCount);
				covrgHeadCount.setHsaHeadCount(hsaHeadCount);

				if (headCountMap.get(groupName) != null) {
					Map<String, List<PlanCoverageLevelHeadCount>> planHeadCountsMap = headCountMap.get(groupName);
					if (planHeadCountsMap.get(plan) != null) {
						List<PlanCoverageLevelHeadCount> list = planHeadCountsMap.get(plan);
						list.add(covrgHeadCount);
						planHeadCountsMap.put(plan, list);
					} else {
						List<PlanCoverageLevelHeadCount> list = new ArrayList<>();
						list.add(covrgHeadCount);
						planHeadCountsMap.put(plan, list);
					}
					headCountMap.put(groupName, planHeadCountsMap);
				} else {
					Map<String, List<PlanCoverageLevelHeadCount>> planHeadCountsMap = new HashMap<>();
					List<PlanCoverageLevelHeadCount> list = new ArrayList<>();
					list.add(covrgHeadCount);
					planHeadCountsMap.put(plan, list);
					headCountMap.put(groupName, planHeadCountsMap);
				}
			}
		}
		return headCountMap;
	}

	@Override
	public Map<String, Long> getWaiverHeadCountByBenefitProgram(Company company, long strategyId, boolean history) {
		Map<String, Long> waiverHeadCountByBenefitGroup = new HashMap<>();
		String sqlQueryName;
		Query query;

		if (history) {
			sqlQueryName = "MEDICAL_WAIVER_COUNT_BY_BENEFIT_PROGRAM_CURRENT";
			query = em.createNamedQuery(sqlQueryName);
		} else {
			sqlQueryName = "MEDICAL_WAIVER_COUNT_BY_BENEFIT_PROGRAM_FUTURE";
			query = em.createNamedQuery(sqlQueryName);
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		}
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
		List<Object[]> results = DaoUtils.getResultList(query, sqlQueryName);

		for (Object[] result : results) {
			String benefitProgram = (String) result[0];
			BigDecimal headCount = (BigDecimal) result[1];
			waiverHeadCountByBenefitGroup.put(benefitProgram, headCount.longValue());
		}
		return waiverHeadCountByBenefitGroup;
	}

	@Override
	public Map<String, Integer> geEnrolledHeadCountByBenefitProgram(Company company, long strategyId, boolean history) {
		Map<String, Integer> enrolledHeadCountByBenefitGroup = new HashMap<>();
		String sqlQueryName;
		Query query;

		if (history) {
			sqlQueryName = "ENROLLED_COUNT_BY_BENEFIT_PROGRAM_CURRENT";
			query = em.createNamedQuery(sqlQueryName);
		} else {
			sqlQueryName = "ENROLLED_COUNT_BY_BENEFIT_PROGRAM_FUTURE";
			query = em.createNamedQuery(sqlQueryName);
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		}
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
		query.setParameter(BSSQueryConstants.PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		List<Object[]> results = DaoUtils.getResultList(query, sqlQueryName);

		for (Object[] result : results) {
			String benefitProgram = (String) result[0];
			BigDecimal headCount = (BigDecimal) result[1];
			enrolledHeadCountByBenefitGroup.put(benefitProgram, headCount.intValue());
		}
		return enrolledHeadCountByBenefitGroup;
	}

	@Override
	public Map<String, ActiveEligibleEECount> getEligibleEmployeeCount(Company company, long strategyId,
			boolean history) {
		Set<String> sdiStates = RulesAndConfigsUtils.getSDIStates(company.getRealmPlanYearId());
		boolean isDisabledBundledOn = RulesAndConfigsUtils.isDisabledBundledOn(company.getRealmPlanYear().getId());

		if (sdiStates.isEmpty()) {
			sdiStates.add(BSSApplicationConstants.EMPTY_SPACE);
		}
		String sqlName = history ? "ELIG_EE_COUNT_BY_WORK_STATE_CURRENT" : "ELIG_EE_COUNT_BY_WORK_STATE_FUTURE";
		Query query = buildQuery(company, sdiStates, sqlName, history, strategyId);
		Map<String, ActiveEligibleEECount> eligHeadCountMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(query, sqlName);

		if (results != null && !results.isEmpty()) {
			for (Object[] result : results) {
				String benProg = (String) result[0];
				int headCount = ((BigDecimal) result[2]).intValue();
				boolean inState = "IN".equals((String) result[1]);

				ActiveEligibleEECount eligibleEECount = eligHeadCountMap.computeIfAbsent(benProg,
						k -> new ActiveEligibleEECount());
				eligibleEECount.setBenProg(benProg);

				updateHeadCount(eligibleEECount, company, headCount, inState, isDisabledBundledOn, sdiStates);
			}
		}
		return eligHeadCountMap;
	}

	private Query buildQuery(Company company, Set<String> sdiStates, String sqlName, boolean history, long strategyId) {
		Query query = em.createNamedQuery(sqlName);
		query.setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearEnd());
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
		query.setParameter("sdiStates", sdiStates);
		if (!history) {
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		}
		return query;
	}

	private void updateHeadCount(ActiveEligibleEECount eligibleEECount, Company company, int headCount, boolean inState,
			boolean isDisabledBundledOn, Set<String> sdiStates) {
		if (isDisabledBundledOn) {
			if (sdiStates.contains(company.getHeadQuatersState())) {
				if (inState) {
					eligibleEECount.setPrimaryHeadCount(headCount);
				} else {
					eligibleEECount.setSecondaryHeadCount(headCount);
				}
			} else {
				if (!inState) {
					eligibleEECount.setPrimaryHeadCount(headCount);
				} else {
					eligibleEECount.setSecondaryHeadCount(headCount);
				}
			}
		} else {
			eligibleEECount.setTotalHeadCount(eligibleEECount.getTotalHeadCount() + headCount);
		}
	}

	@Override
	public List<MappedHeadCount> getMappedHeadCount(String companyCode, long realmPlanYearId) {
		List<MappedHeadCount> headCountMap = new ArrayList<>();
		String queryString = "MAPPED_PLAN_HEADCOUNT";
		Query query = em.createNamedQuery(queryString);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		query.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);

		List<Object[]> results = DaoUtils.getResultList(query, queryString);

		for (Object[] r : results) {
			String currentBenefitPlan = (String) r[0];
			String futureBenefitPlan = (String) r[1];
			String covrgCode = (String) r[2];
			int headCount = ((BigDecimal) r[3]).intValue();

			MappedHeadCount mappedHeadCount = new MappedHeadCount();
			mappedHeadCount.setCurrentBenefitPlanId(currentBenefitPlan);
			mappedHeadCount.setFutureBenefitPlanId(futureBenefitPlan);
			mappedHeadCount.setCoverageCode(covrgCode);
			mappedHeadCount.setHeadCount(headCount);
			headCountMap.add(mappedHeadCount);
		}
		return headCountMap;
	}

}
