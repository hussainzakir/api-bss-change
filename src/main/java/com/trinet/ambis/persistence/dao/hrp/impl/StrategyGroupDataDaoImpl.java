package com.trinet.ambis.persistence.dao.hrp.impl;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.service.model.output.AdditionalBenefitPlanDto;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rvutukuri
 *
 */
public class StrategyGroupDataDaoImpl implements StrategyGroupDataDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	private static final Logger logger = LoggerFactory.getLogger(EmployeeBenefitGroupDao.class);

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void resetStrategyContributionHeadcounts(Long strategyId) {
		try {
			Query query = em.createNamedQuery("RESET_STRATEGY_CONTRIBUTION_HC");
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
			DaoUtils.executeUpdate(query, "RESET_STRATEGY_CONTRIBUTION_HC");

		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void resetStrategyPlanSelectHeadcounts(Long strategyId) {
		try {
			Query query = em.createNamedQuery("RESET_STRATEGY_PLANSELECT_HC");
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
			DaoUtils.executeUpdate(query, "RESET_STRATEGY_PLANSELECT_HC");

		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		}

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void updateStrategyGroupStatus(Company company, Long strategyId) {
		try {
			Query query = em.createNamedQuery("UPDATE_STRATEGY_GROUP_STATUS");
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
			query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getId());
			DaoUtils.executeUpdate(query, "UPDATE_STRATEGY_GROUP_STATUS");

		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "", "");
		}
	}

	@Override
	public List<String> getMedStrategyPortfolios(long strategyId) {
		Query query = em.createNamedQuery("STRATEGY_MEDICAL_PORTFOLIOS");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		@SuppressWarnings("unchecked")
		List<BigDecimal> r = query.getResultList();
		List<String> results = new ArrayList<>();
		for (BigDecimal portfolio : r) {
			results.add(portfolio.toString());
		}
		return results;
	}

	@Override
	public List<String> getExclMedPlanPortfolio(long strategyId, long realmPlanYearId) {
		Query query = em.createNamedQuery("EXCL_MED_PLAN_PORTFOLIOS");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		return DaoUtils.getResultStringList(query, "EXCL_MED_PLAN_PORTFOLIOS");
	}

	@Override
	public Map<String, Long> getStrategyWaiverHeadCount(Company company, long strategyId, boolean history) {
		Query query = null;
		String sqlName;
		if (history) {
			sqlName = "GROUP_WAIVE_HEADCOUNTS";
			query = entityManager.createNamedQuery(sqlName);
			query.setParameter("effdt", company.getRealmPlanYear().getPlanYearEnd());
		} else {
			sqlName = "GROUP_WAIVE_HEADCOUNTS_BY_STRATEGY";
			query = entityManager.createNamedQuery(sqlName);
			query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
			query.setParameter("effdt", company.getPlanStartDate());
		}
		query.setParameter("COMPANY_CODE", company.getCode());
		List<Object[]> results = DaoUtils.getResultList(query, sqlName);
		Map<String, Long> strategyWaiverHeadCount = new HashMap<>();
		if (null != results && !results.isEmpty()) {
			for (Object[] r : results) {
				String benefitProgram = (String) r[0];
				BigDecimal headCount = (BigDecimal) r[1];
				strategyWaiverHeadCount.put(benefitProgram, headCount.longValue());
			}
		}
		return strategyWaiverHeadCount;
	}


	@Override
	public List<String> getPortfolioFsaPlans(Company company, List<String> strategyPortfolios) {
		Query query = em.createNamedQuery("PORTFOLIOS_FSA_PLANS");
		query.setParameter("REALM_PLAN_YEAR", company.getRealmPlanYearId());
		query.setParameter("PORTFOLIOS", strategyPortfolios);
		return DaoUtils.getResultStringList(query, "PORTFOLIOS_FSA_PLANS");
	}

	@Override
	public Map<String, Map<String, Set<Long>>> getStrategyPortfoliosByPlanType(long strategyId) {
		Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio = new HashMap<>();
		Query query = em.createNamedQuery("STRATEGY_PORTFOLIOS_BY_PLAN_TYPE");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		List<Object[]> results = DaoUtils.getResultList(query, "STRATEGY_PORTFOLIOS_BY_PLAN_TYPE");

		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String planType = (String) r[1];
			long portfolioId = ((BigDecimal) r[2]).longValue();

			if (null != benefitGroupPlanTypePortfolio.get(benefitProgram)) {
				Map<String, Set<Long>> planTypePortfolioMap = benefitGroupPlanTypePortfolio.get(benefitProgram);
				if (null != planTypePortfolioMap.get(planType)) {
					Set<Long> portfoliSet = planTypePortfolioMap.get(planType);
					portfoliSet.add(portfolioId);
				} else {
					Set<Long> portfoliSet = new HashSet<>();
					portfoliSet.add(portfolioId);
					planTypePortfolioMap.put(planType, portfoliSet);
				}
			} else {
				Set<Long> portfoliSet = new HashSet<>();
				portfoliSet.add(portfolioId);
				Map<String, Set<Long>> planTypePortfolioMap = new HashMap<>();
				planTypePortfolioMap.put(planType, portfoliSet);
				benefitGroupPlanTypePortfolio.put(benefitProgram, planTypePortfolioMap);
			}
		}
		return benefitGroupPlanTypePortfolio;
	}

	@Override
	public List<PlanSelection> getPlanSelections(long strategyId, long groupId, long realmPlanYearId) {
		final int ID_INDEX = 0;
		final int STRATEGY_ID_INDEX = 1;
		final int GROUP_ID_INDEX = 2;
		final int PLAN_TYPE_INDEX = 3;
		final int BENEFIT_PLAN_INDEX = 4;
		final int HEADCOUNT_INDEX = 5;
		final int PPO_INDEX = 6;
		final int HDHP_INDEX = 7;

		List<PlanSelection> planSelectionList = new ArrayList<>();
		Query query = em.createNamedQuery("PLAN_SELECTIONS");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		query.setParameter(BSSQueryConstants.GROUP_ID, groupId);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		for (Object[] r : results) {
			PlanSelection planSelection = new PlanSelection();
			planSelection.setId(((BigDecimal) r[ID_INDEX]).longValue());
			planSelection.setStrategyId(((BigDecimal) r[STRATEGY_ID_INDEX]).longValue());
			planSelection.setGroupId(((BigDecimal) r[GROUP_ID_INDEX]).longValue());
			planSelection.setPlanType((String) r[PLAN_TYPE_INDEX]);
			planSelection.setBenefitPlan((String) r[BENEFIT_PLAN_INDEX]);
			planSelection.setHeadCount(((BigDecimal) r[HEADCOUNT_INDEX]).longValue());
			planSelection.setPpoPlan(BigDecimal.ONE.equals((BigDecimal) r[PPO_INDEX]));
			planSelection.setHighDeductiblePlan(BigDecimal.ONE.equals((BigDecimal) r[HDHP_INDEX]));
			planSelectionList.add(planSelection);

		}
		return planSelectionList;
	}

	@Override
	public Map<String, List<AdditionalBenefitPlanDto>> getAdditionalBenPlanSelections(long strategyId, long realmYearId) {
		Map<String, List<AdditionalBenefitPlanDto>> benefitProgramPlans = new HashMap<>();

		Query query = em.createNamedQuery("ADDITIONAL_PLAN_SELECTIONS_BY_BEN_GROUP");

		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmYearId);
		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		for (Object[] row : results) {
			AdditionalBenefitPlanDto plan = AdditionalBenefitPlanDto.builder()
					.isSelected(row[0] != null)
					.planType((String) row[5])
					.benefitPlan((String) row[6])
					.name((String) row[7])
					.isSdiPlan(((String) row[11]).contains("SDI"))
					.isEmployeePaid(BigDecimal.ONE.equals(row[10]))
					.bundleId( row[3] == null ? "" : row[3].toString() )
					.bundleName((String) row[4])
					.build();

			String benefitProgram = (String) row[8];
			List<AdditionalBenefitPlanDto> list = benefitProgramPlans.get(benefitProgram);
			if( list == null ) {
				list = new ArrayList<>();
				benefitProgramPlans.put(benefitProgram, list);
			}
			list.add(plan);
		}

		// Determine selected disability bundle for each benefit program
		for(Map.Entry<String, List<AdditionalBenefitPlanDto>> entry : benefitProgramPlans.entrySet()) {

			// for this benefit program, develop a map of bundles and the list of associated plans
			Map<String,List<String>> mapBundleIdToPlans = new HashMap<>();
			for(AdditionalBenefitPlanDto dto : entry.getValue()) {
				if(!dto.getBundleId().equals("")) {
					List<String> bundlePlanList = mapBundleIdToPlans.get(dto.getBundleId());
					if(bundlePlanList == null) {
						bundlePlanList = new ArrayList<>();
						mapBundleIdToPlans.put(dto.getBundleId(), bundlePlanList);
					}
					bundlePlanList.add(dto.getBenefitPlan());
				}
			}

			// get the full set of selected plans
			Set<String> selectedPlanIds = entry.getValue().stream()
					.filter(plan -> !plan.getBundleId().equals("") && plan.isSelected())
					.map(AdditionalBenefitPlanDto::getBenefitPlan)
					.collect(Collectors.toSet());

			// discover one bundle that has all the selected plans and only the selected plans
			String selectedBundle = null;
			for(Map.Entry<String,List<String>> bundle : mapBundleIdToPlans.entrySet()) {
				if(bundle.getValue().containsAll(selectedPlanIds) && selectedPlanIds.containsAll(bundle.getValue()) ) {
					selectedBundle = bundle.getKey();
					break;
				}
			}

			// set the isSelected flags for disability plans according to the bundleId identified above
			for(AdditionalBenefitPlanDto dto : entry.getValue()) {
				if(!dto.getBundleId().equals("")) {
					dto.setSelected(dto.getBundleId().equals(selectedBundle));
				}
			}
		}

		return benefitProgramPlans;
	}

	@Override
	public List<String> getStrategyOutOfLocationPlans(long strategyId, Company company, Set<String> regions,
			Set<String> portfolios, Set<String> outOfRegionPlans) {
		Query q = em.createNamedQuery("STRATEGY_OUT_OF_LOCATION_PLANS");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		if (company.isTexasSitus()) {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_TX);
		} else {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_FL);
		}
		if (CollectionUtils.isEmpty(outOfRegionPlans)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws
			// exception for empty list.
			outOfRegionPlans = new HashSet<>(1);
			outOfRegionPlans.add(BSSQueryConstants.PLAN_TO_EXCLUDE);
		}

		if (CollectionUtils.isEmpty(portfolios)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws
			// exception for empty list.
			portfolios = new HashSet<>(1);
			portfolios.add(BSSQueryConstants.PORTFOLIOS_DUMMY);
		}
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, company.getRealmPlanYearId());
		q.setParameter("portfolios", portfolios);
		q.setParameter("regions", regions);
		q.setParameter("outOfRegionPlans", outOfRegionPlans);
		@SuppressWarnings("unchecked")
		List<String> results = q.getResultList();
		return results;
	}

	@Override
	public List<String> getStrategyPortfolioMissingPlans(long strategyId, Company company, Set<String> portfolios,
	         Set<String> outOfRegionPlans) {
		String queryName = AppRulesAndConfigsUtils.isBundleV2Enabled()
				? BSSQueryConstants.STRATEGY_PORTFOLIO_MISSING_PLANS_V2
				: BSSQueryConstants.STRATEGY_PORTFOLIO_MISSING_PLANS;
		Query q = em.createNamedQuery(queryName);
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		if (company.isTexasSitus()) {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_TX);
		} else {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_FL);
		}
		q.setParameter(BSSQueryConstants.EFF_DT, company.getRealmPlanYear().getPlanYearEnd());
		q.setParameter(BSSQueryConstants.BUNDLE_ID, Objects.nonNull(company.getBundleId()) ?
				company.getBundleId() : BSSQueryConstants.ORACLE_NULL);
		if (CollectionUtils.isEmpty(outOfRegionPlans)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws
			// exception for empty list.
			outOfRegionPlans = new HashSet<>(1);
			outOfRegionPlans.add(BSSQueryConstants.PLAN_TO_EXCLUDE);
		}
		if (CollectionUtils.isEmpty(portfolios)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws
			// exception for empty list.
			portfolios = new HashSet<>(1);
			portfolios.add(BSSQueryConstants.PORTFOLIOS_DUMMY);
		}
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, company.getRealmPlanYearId());
		q.setParameter("portfolios", portfolios);
		q.setParameter("outOfRegionPlans", outOfRegionPlans);
		q.setParameter("oeQuarter", company.getRealmPlanYear().getOeQuarter());
		@SuppressWarnings("unchecked")
		List<String> results = q.getResultList();
		return results;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyOutOfLocationPlans(long strategyId, List<String> benefitPlans) {
		Query q1 = em.createNamedQuery("DELETE_OUT_OF_LOC_CONTRBNS");
		q1.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		q1.setParameter("benefitPlans", benefitPlans);
		int num = q1.executeUpdate();
		logger.info("DELETED NUMBER OF PLANS : {}", num);

		Query q = em.createNamedQuery("DELETE_OUT_OF_LOC_PLNS");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		q.setParameter("benefitPlans", benefitPlans);
		num = q.executeUpdate();
		logger.info("DELETED NUMBER OF PLANS : {}", num);
	}
	
	@Override
	public List<String> getStrategyAutoSelectPlans(long strategyId, Company company, Set<String> portfolios,
			Set<String> outOfRegionPlans) {
		Query q = em.createNamedQuery("STRATEGY_AUTO_SELECT_PLANS");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		if (company.isTexasSitus()) {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_TX);
		} else {
			q.setParameter(BSSQueryConstants.SITUS, BSSApplicationConstants.SITUS_FL);
		}
		if (CollectionUtils.isEmpty(outOfRegionPlans)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws
			// exception for empty list.
			outOfRegionPlans = new HashSet<>(1);
			outOfRegionPlans.add(BSSQueryConstants.PLAN_TO_EXCLUDE);
		}
		if (CollectionUtils.isEmpty(portfolios)) {
			// Need to pass some random string as a benPlan since oracle IN clause throws
			// exception for empty list.
			portfolios = new HashSet<>(1);
			portfolios.add(BSSQueryConstants.PORTFOLIOS_DUMMY);
		}
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, company.getRealmPlanYearId());
		q.setParameter("portfolios", portfolios);
		q.setParameter("outOfRegionPlans", outOfRegionPlans);
		@SuppressWarnings("unchecked")
		List<String> results = q.getResultList();
		return results;
	}
	
}
