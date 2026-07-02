/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.enums.BenExchngEnums;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.aop.BSSCacheable;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.persistence.dao.hrp.PlanRatesDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.StateBenefitPlan;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author rvutukuri
 *
 */
public class PlanRatesDataDaoImpl implements PlanRatesDataDao {

	private static final Logger logger = LoggerFactory.getLogger(PlanRatesDataDaoImpl.class);

	private static final String BENEFIT_PLANS_FOR_PLAN_RATES = "BENEFIT_PLANS_FOR_PLAN_RATES";
	private static final String BENEFIT_PLANS_FOR_PLAN_RATES_V2 = "BENEFIT_PLANS_FOR_PLAN_RATES_V2";

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager hpdbEm;
	
	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager hrdbEm;
	
	@Autowired
	StrategyDataDao stategyDataDao;

	@Override
	public Map<String, StateBenefitPlan> getBenefitPlans(long realmPlanYearId,
			Set<String> portfolios, Company company, Set<String> outOfRegionPlans, Set<String> planTypes) {

		Map<String, StateBenefitPlan> benefitPlanMap = new HashMap<>();
		if (CollectionUtils.isEmpty(outOfRegionPlans)) {
			outOfRegionPlans = new HashSet<>();
			outOfRegionPlans.add(BSSQueryConstants.PLAN_TO_EXCLUDE);
		}
		if (CollectionUtils.isEmpty(portfolios)) {
			portfolios = new HashSet<>();
			portfolios.add("");
		}
		
		String queryName = AppRulesAndConfigsUtils.isBundleV2Enabled()
				? BENEFIT_PLANS_FOR_PLAN_RATES_V2
				: BENEFIT_PLANS_FOR_PLAN_RATES;
		Query q = hpdbEm.createNamedQuery(queryName);
		if (company.isTexasSitus()) {
			q.setParameter("SITUS", "TX");
		} else {
			q.setParameter("SITUS", "FL");
		}
		q.setParameter("REALM_PLAN_YEAR", realmPlanYearId);
		q.setParameter("PORTFOLIOS", portfolios);
		q.setParameter("PLAN_TYPE_LIST", planTypes);
		q.setParameter("LIFE_PLAN_TYPE_LIST", Arrays.asList(BSSApplicationConstants.LIFE_CODE));
		q.setParameter("OUT_OF_REGION_PLANS", outOfRegionPlans);
        q.setParameter("OE_QUARTER", company.getRealmPlanYear().getOeQuarter());
		q.setParameter(BSSQueryConstants.BUNDLE_ID,
				Objects.nonNull(company.getBundleId()) ? company.getBundleId() : BSSQueryConstants.ORACLE_NULL);
		q.setParameter(BSSQueryConstants.EFF_DT, company.getRealmPlanYear().getPlanYearStart());

		List<Object[]> results = DaoUtils.getResultList(q, queryName);
		for (Object[] r : results) {

			StateBenefitPlan plan = new StateBenefitPlan();
			String benefitPlan = (String) r[0];
			plan.setBenefitPlan(benefitPlan);
			plan.setDescription((String) r[1]);
			plan.setPlanType((String) r[2]);
			plan.setVendorId((String) r[3]);
			plan.setPortfolioId(((BigDecimal) r[4]).longValue());
			benefitPlanMap.put(benefitPlan, plan);
		}
		return benefitPlanMap;
	}

	@Override
	public Map<String, Set<String>> getBenefitPlanStates(long realmPlanYearId, long prevRealmPlanYearId) {

		Map<String, Set<String>> benefitPlanStateMap = new HashMap<>();

		Query q;
		q = hpdbEm.createNamedQuery("BENEFIT_PLAN_STATES_FOR_PLAN_RATES");
		q.setParameter("REALM_PLAN_YEARS", Arrays.asList(realmPlanYearId, prevRealmPlanYearId));

		List<Object[]> results = DaoUtils.getResultList(q, "BENEFIT_PLAN_STATES_FOR_PLAN_RATES");
		for (Object[] r : results) {

			String benefitPlan = (String) r[0];
			String state = (String) r[1];

			if (benefitPlanStateMap.containsKey(benefitPlan)) {
				benefitPlanStateMap.get(benefitPlan).add(state);
			} else {

				benefitPlanStateMap.put(benefitPlan, new HashSet<>(Arrays.asList(state)));
			}
		}
		return benefitPlanStateMap;
	}	
	
	@Override
	@BSSCacheable(objectType = CacheObjectTypeEnum.PLAN_RATES_OBJECT_TYPE)
	public Map<String, List<BenefitPlanRate>> getBenefitPlanRatesBy(@CacheKey(value = "id") Company company) {
		logger.info("$$$$$$$$$$$$$$$$$$$ getting benefit plan rates from DB $$$$$$$$$$$$$$");
		long startTime = System.currentTimeMillis();

		Set<String> planTypes = stategyDataDao.getRealmPlanTypes(company.getRealmPlanYear().getId());
		Set<String> bandCodesSet = extractBandCodes(company);
		List<Object[]> results;

		Query q = hrdbEm.createNamedQuery("REALM_ALL_RATES");
		q.setParameter(BSSQueryConstants.CLONE_PROGRAM, company.getRealmPlanYear().getCloneProgram());
		q.setParameter("quarter", (BenExchngEnums.TRINET_OMS.getQuarters().contains(company.getQuater())
				? BenExchngEnums.TRINET_XI.getQuarters().iterator().next()
				: company.getQuater()));
		q.setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearEnd());
		q.setParameter("planTypeList", planTypes);
		q.setParameter("bandCodesSet", bandCodesSet);
		q.setHint(QueryHints.FETCH_SIZE, BSSQueryConstants.HIBERNATE_FETCH_SIZE_1000);
		results = DaoUtils.getResultList(q, "REALM_ALL_RATES");

		Map<String, List<BenefitPlanRate>> map = new HashMap<>();
		for (Object[] r : results) {
			BenefitPlanRate planRate = new BenefitPlanRate();
			planRate.setBandCode((String) r[0]);
			String plan = (String) r[1];
			planRate.setBenefitPlan(plan);
			planRate.setCoverageCode((String) r[2]);
			planRate.setPlanType((String) r[3]);
			planRate.setEmployerCost((BigDecimal) r[4]);
			planRate.setEffDt((Date) r[5]);
			if (map.get(plan) != null) {
				List<BenefitPlanRate> rates = map.get(plan);
				rates.add(planRate);
				Collections.sort(rates);
				map.put(plan, rates);
			} else {
				List<BenefitPlanRate> rates = new ArrayList<>();
				rates.add(planRate);
				Collections.sort(rates);
				map.put(plan, rates);
			}
		}

		long endTime = System.currentTimeMillis();
		logger.info("REALM_ALL_RATES took {} ms", (endTime - startTime));
		return map;
	}
	
	private Set<String> extractBandCodes(Company company) {
		BandCodes bandCodes = company.getBandCodes();
		Set<String> bandCodesSet = new HashSet<>();
		bandCodesSet.add(bandCodes.getAetnaBandCode());
		bandCodesSet.add(bandCodes.getAetnaHmoBandCode());
		bandCodesSet.add(bandCodes.getAetnaPpoBandCode());
		bandCodesSet.add(bandCodes.getBcbsBandCode());
		bandCodesSet.add(bandCodes.getBcbsNcBandCode());
		bandCodesSet.add(bandCodes.getBsOfCaBandCode());
		bandCodesSet.add(bandCodes.getDisBandCode());
		bandCodesSet.add(bandCodes.getKaisCoBandCode());
		bandCodesSet.add(bandCodes.getKaisNwBandCode());
		bandCodesSet.add(bandCodes.getKaiserBandCode());
		bandCodesSet.add(bandCodes.getLifeBandCode());
		bandCodesSet.add(bandCodes.getTuftsBandCode());
		bandCodesSet.add(bandCodes.getUhcBandCode());
		bandCodesSet.add(bandCodes.getBcOfIdBandCode());
		bandCodesSet.add(bandCodes.getBcbsMNBandCode());
		bandCodesSet.add(bandCodes.getKaiMidAtlBandCode());
		bandCodesSet.add(bandCodes.getKaiHawaiiBandCode());
		bandCodesSet.add(bandCodes.getEmpireNYBand());
		bandCodesSet.add(bandCodes.getHarvardBandCode());
		bandCodesSet.add(bandCodes.getHighmarkBandCode());
		bandCodesSet.add("N");
		return bandCodesSet;
	}
	
	public void setHpdbEm(EntityManager em) {
		this.hpdbEm = em;
	}
	
	public void setHrdbEm(EntityManager em) {
		this.hrdbEm = em;
	}
}
