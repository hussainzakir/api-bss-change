/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.service.model.HeadCountBenefitPlan;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author rvutukuri
 *
 */
public class PortfolioHeadCountDataDaoImpl implements PortfolioHeadCountDataDao {

	@Autowired
	RealmPlanYearService realmPlanYearService;

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public static final String PORTFOLIO_HEADCOUNTS_MDV = "PORTFOLIO_HEADCOUNTS_MDV";
	public static final String PROSPECT_PLAN_HEADCOUNTS_MDV = "PROSPECT_PLAN_HEADCOUNTS_MDV";
	public static final String PROSPECT_PORTFOLIO_HC_MAPPING_MDV = "PROSPECT_PORTFOLIO_HC_MAPPING_MDV";
	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Override
	public Map<String, List<HeadCountBenefitPlan>> getHeadCountPlans(long strategyId) {
		List<Object[]> results = this.getHeadCountData(strategyId);
		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = new HashMap<>();
		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String planType = (String) r[1];
			String benefitPlan = (String) r[2];
			long portfolioId = ((BigDecimal) r[3]).longValue();
			String coverageLevelId = (String) r[5];
			long headcount = ((BigDecimal) r[6]).longValue();
			long hsaHeadcount = ((BigDecimal) r[7]).longValue();

			String coverageCode = CoverageCodesEnums.valueOfId(coverageLevelId);
			if (coverageCode == null)
				continue;

			addOrUpdateBenefitPlan(benefitProgramHeadCounts, benefitProgram, benefitPlan, planType, portfolioId,
					coverageCode, headcount, hsaHeadcount);
		}

		return benefitProgramHeadCounts;
	}

	@Override
	public Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> getBenefitProgramHeadCounts(long strategyId,
			Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio) {

		List<Object[]> results = this.getHeadCountData(strategyId);
		Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> benefitProgramHeadCountMap = new HashMap<>();
		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String benefitPlan = (String) r[2];
			String coverageLevelId = (String) r[5];
			long headcount = ((BigDecimal) r[6]).longValue();
			long hsaHeadcount = ((BigDecimal) r[7]).longValue();
			String planType = (String) r[1];
			long portfolioId = ((BigDecimal) r[3]).longValue();

			boolean validPortfolio = isValidPortfolio(benefitGroupPlanTypePortfolio, benefitProgram, planType,
					portfolioId);

			if (validPortfolio) {
				CoverageLevelHeadCount coverageLevelHeadCount = createCoverageLevelHeadCount((int) headcount,
						(int) hsaHeadcount);
				updateBenefitProgramHeadCountMap(benefitProgramHeadCountMap, benefitProgram, benefitPlan,
						coverageLevelId, coverageLevelHeadCount);
			}
		}
		return benefitProgramHeadCountMap;
	}

	@Override
	public Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> getProspectBenefitProgramHeadCounts(
			long strategyId, Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio) {
		Query query = em.createNamedQuery(PROSPECT_PLAN_HEADCOUNTS_MDV);
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		List<Object[]> results = DaoUtils.getResultList(query, PROSPECT_PLAN_HEADCOUNTS_MDV);
		Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> benefitProgramHeadCountMap = new HashMap<>();

		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String planType = (String) r[1];
			String benefitPlan = (String) r[2];
			long portfolioId = ((BigDecimal) r[3]).longValue();
			String coverageLevelId = (String) r[4];
			long headcount = ((BigDecimal) r[5]).longValue();
			long hsaHeadcount = ((BigDecimal) r[6]).longValue();

			boolean validPortfolio = isValidPortfolio(benefitGroupPlanTypePortfolio, benefitProgram, planType,
					portfolioId);

			if (validPortfolio) {
				CoverageLevelHeadCount coverageLevelHeadCount = createCoverageLevelHeadCount((int) headcount,
						(int) hsaHeadcount);
				updateBenefitProgramHeadCountMap(benefitProgramHeadCountMap, benefitProgram, benefitPlan,
						coverageLevelId, coverageLevelHeadCount);
			}
		}
		return benefitProgramHeadCountMap;
	}

	@Override
	public Map<String, Map<String, Map<String, Long>>> getMirrorPlanHeadCounts(String company, long realmYearId) {
		Query query = em.createNamedQuery("MIRROR_PLAN_HC");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmYearId);
		List<Object[]> results = DaoUtils.getResultList(query, "MIRROR_PLAN_HC");
		Map<String, Map<String, Map<String, Long>>> mirrorPlanHeadCounts = new HashMap<>();
		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String benefitPlan = (String) r[1];
			String cvgCode = (String) r[2];
			long hc = ((BigDecimal) r[3]).longValue();

			if (null != mirrorPlanHeadCounts.get(benefitProgram)) {
				Map<String, Map<String, Long>> benefitPlanHc = mirrorPlanHeadCounts.get(benefitProgram);
				if (null != benefitPlanHc.get(benefitPlan)) {
					Map<String, Long> coverageLevelHc = benefitPlanHc.get(benefitPlan);
					coverageLevelHc.put(cvgCode, hc);
				} else {
					Map<String, Long> coverageLevelHc = new HashMap<>();
					coverageLevelHc.put(cvgCode, hc);
					benefitPlanHc.put(benefitPlan, coverageLevelHc);
				}
			} else {
				Map<String, Map<String, Long>> benefitPlanHc = new HashMap<>();
				Map<String, Long> coverageLevelHc = new HashMap<>();
				coverageLevelHc.put(cvgCode, hc);
				benefitPlanHc.put(benefitPlan, coverageLevelHc);
				mirrorPlanHeadCounts.put(benefitProgram, benefitPlanHc);
			}

		}
		return mirrorPlanHeadCounts;
	}

	@Override
	public Map<String, List<HeadCountBenefitPlan>> getProspectHeadCountPlans(long strategyId) {
		Query query = em.createNamedQuery(PROSPECT_PORTFOLIO_HC_MAPPING_MDV);
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		List<Object[]> results = DaoUtils.getResultList(query, PROSPECT_PORTFOLIO_HC_MAPPING_MDV);

		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = new HashMap<>();

		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String planType = (String) r[1];
			String benefitPlan = (String) r[2];
			long portfolioId = ((BigDecimal) r[3]).longValue();
			String coverageLevelId = (String) r[4];
			long headcount = ((BigDecimal) r[5]).longValue();
			long hsaHeadcount = ((BigDecimal) r[6]).longValue();

			String coverageCode = CoverageCodesEnums.valueOfId(coverageLevelId);
			if (coverageCode == null)
				continue;
			
			addOrUpdateBenefitPlan(benefitProgramHeadCounts, benefitProgram, benefitPlan, planType, portfolioId,
					coverageCode, headcount, hsaHeadcount);
		}
		return benefitProgramHeadCounts;
	}
	
	private void addOrUpdateBenefitPlan(
	        Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts,
	        String benefitProgram,
	        String benefitPlan,
	        String planType,
	        long portfolioId,
	        String coverageCode,
	        long headcount,
	        long hsaHeadcount) {

	    List<HeadCountBenefitPlan> plans = benefitProgramHeadCounts
	            .computeIfAbsent(benefitProgram, k -> new ArrayList<>());

	    Optional<HeadCountBenefitPlan> existingPlanOpt = plans.stream()
	            .filter(p -> p.getBenefitPlanId().equals(benefitPlan))
	            .findFirst();

	    if (existingPlanOpt.isPresent()) {
	        HeadCountBenefitPlan existingPlan = existingPlanOpt.get();
	        existingPlan.getCoverageLevelHeadCounts().merge(coverageCode, headcount, Long::sum);
	        existingPlan.getHsaCoverageLevelHeadCounts().merge(coverageCode, hsaHeadcount, Long::sum);
	    } else {
	        HeadCountBenefitPlan newPlan = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder()
	                .benefitPlanId(benefitPlan)
	                .planType(planType)
	                .planCarrierId(portfolioId)
	                .populateZeroCvgLvlHeadCounts(true)
	                .build();

	        newPlan.getCoverageLevelHeadCounts().put(coverageCode, headcount);
	        newPlan.getHsaCoverageLevelHeadCounts().put(coverageCode, hsaHeadcount);
	        plans.add(newPlan);
	    }
	}


	private boolean isValidPortfolio(Map<String, Map<String, Set<Long>>> benefitGroupPlanTypePortfolio,
			String benefitProgram, String planType, long portfolioId) {
		Map<String, Set<Long>> programMap = benefitGroupPlanTypePortfolio.get(benefitProgram);
		return programMap != null && programMap.containsKey(planType) && programMap.get(planType).contains(portfolioId);
	}

	/**
	 * Creates a new CoverageLevelHeadCount object with the passed in headCount and
	 * hsaHeadCount values
	 * 
	 * @param headCount
	 * @param hsaHeadCount
	 * @return
	 */
	private CoverageLevelHeadCount createCoverageLevelHeadCount(int headCount, int hsaHeadCount) {
		CoverageLevelHeadCount coverageLevelHeadCount = new CoverageLevelHeadCount();
		coverageLevelHeadCount.setHeadCount(headCount);
		coverageLevelHeadCount.setHsaHeadCount(hsaHeadCount);
		return coverageLevelHeadCount;
	}

	private void updateBenefitProgramHeadCountMap(
			Map<String, Map<String, Map<String, CoverageLevelHeadCount>>> benefitProgramHeadCountMap,
			String benefitProgram, String benefitPLan, String coverageLevelId,
			CoverageLevelHeadCount coverageLevelHeadCount) {
		Map<String, Map<String, CoverageLevelHeadCount>> benefitPlanHeadCountMap = benefitProgramHeadCountMap
				.getOrDefault(benefitProgram, new HashMap<>());
		Map<String, CoverageLevelHeadCount> coverageHeadCountMap = benefitPlanHeadCountMap.getOrDefault(benefitPLan,
				new HashMap<>());

		coverageHeadCountMap.put(coverageLevelId, coverageLevelHeadCount);
		benefitPlanHeadCountMap.put(benefitPLan, coverageHeadCountMap);
		benefitProgramHeadCountMap.put(benefitProgram, benefitPlanHeadCountMap);
	}

	/**
	 * Returns the raw data from the PORTFOLIO_HEADCOUNTS_MDV query
	 * 
	 * @param strategyId
	 * 
	 * @return
	 */
	private List<Object[]> getHeadCountData(long strategyId) {
		Query query = em.createNamedQuery(PORTFOLIO_HEADCOUNTS_MDV);
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		return DaoUtils.getResultList(query, PORTFOLIO_HEADCOUNTS_MDV);
	}

}
