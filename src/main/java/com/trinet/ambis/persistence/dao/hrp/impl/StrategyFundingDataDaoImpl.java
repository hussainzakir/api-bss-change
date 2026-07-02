/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.collections.map.MultiKeyMap;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.exception.BSSErrorResponseMessages;
import com.trinet.ambis.persistence.dao.hrp.StrategyFundingDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenefitOfferFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.GroupFunding;
import com.trinet.ambis.service.model.ModelCompareBenSuppExcessOption;
import com.trinet.ambis.service.model.ModelCompareStrategy;
import com.trinet.ambis.service.model.PlanTypeDescription;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.Utils;

/**
 * @author rvutukuri
 *
 */
public class StrategyFundingDataDaoImpl implements StrategyFundingDataDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;
	
	private static final String PLAN_LEVEL_FUNDING_OVERRIDES_BY_STRATEGY_GROUP = "PLAN_LEVEL_FUNDING_OVERRIDES_BY_STRATEGY_GROUP"; 

	@Override
	public List<String> getBsuppStrategyFundVolPlanTypes(long fundingModel) {
		Query query = em.createNamedQuery("BSUPP_STRATEGY_VOL_PLAN_TYPES");
		query.setParameter("fundingModel", fundingModel);
		return DaoUtils.getResultStringList(query, "BSUPP_STRATEGY_VOL_PLAN_TYPES");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, ModelCompareStrategy> getFundingDetailsByStrategyId(List<Long> strategyIds, Company company, boolean includeGroupExcessOptions, Date effDate) {
		
		final int COMPANY_ID = 0;
		final int STRATEGY_ID = 1;
		final int GROUP_ID = 2;
		final int BENEFIT_PROGRAM = 3;
		final int GROUP_DESC = 4;
		final int WP_DESC = 5;
		final int FUNDING_MODEL_ID = 6;
		final int PLAN_TYPE = 7;
		final int FUNDING_TYPE = 8;
		final int BASE_BENEFIT_PLAN = 9;
		final int WAIVER_ALLOWANCE = 10;
		final int BSUPP_EXCESS_OPTION_ID = 11;
		final int BSUPP_EXCESS_OPTION_CODE = 12;
		final int BSUPP_EXCESS_OPTION_DESC = 13;
		final int COVERAGE_ID = 14;
		final int CONTRIBUTION = 15;
		final int STRATEGY_NAME = 16;
		final int BENEFIT_PLAN_DESC = 17;
		final int COVERAGE_DESC = 18;

		String queryName = "getMCFundingDetailsByStrategyId";
		Query q = em.createNamedQuery(queryName);
		q.setParameter("strategyList", strategyIds);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		q.setParameter("voluntaryPlanTypes", BSSApplicationConstants.VOLUNTARY_PLAN_TYPES);
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(q);
		List<Object[]> results = DaoUtils.getResultList(q, queryName);
		
		Map<Long, ModelCompareStrategy> returnList = new HashMap<>();
		List<GroupFunding> groupFundingList = null;
		Map<Long, GroupFunding> groupFundingMap = null;
		Long previousStrategyId = null;
		ModelCompareStrategy modelCompareStrategy = null;
		MultiKeyMap strategyFlaxMaxContributions = null;
		MultiKeyMap strategyBpLimits = null;

		if (CollectionUtils.isEmpty(results)) {
			throw new BSSApplicationException(new BSSApplicationError(
					BSSErrorResponseCodes.BSS_MC_STRATEGY_FUNDING_DETAILS_NOT_FOUND,
					BSSHttpStatusConstants.FORBIDDEN, StrategyDataDaoImpl.class.getName(),
					BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, queryName, queryMap));

		} else {
			for (Object[] r : results) {
				Long companyId = ((BigDecimal) r[COMPANY_ID]).longValue();
				Long strategyId = ((BigDecimal) r[STRATEGY_ID]).longValue();
				Long groupId = ((BigDecimal) r[GROUP_ID]).longValue();
				String benefitProgram = (String) r[BENEFIT_PROGRAM];
				String groupDesc = (String) r[GROUP_DESC];
				String waitTime = (String) r[WP_DESC];
				Long fundingModelId = ((BigDecimal) r[FUNDING_MODEL_ID]) != null
						? ((BigDecimal) r[FUNDING_MODEL_ID]).longValue()
						: 0L;
				String planType = (String) r[PLAN_TYPE];
				String fundingType = (String) r[FUNDING_TYPE];
				String baseFundPlan = (String) r[BASE_BENEFIT_PLAN];
				BigDecimal waiverAllowance = (BigDecimal) r[WAIVER_ALLOWANCE];
				Long bsuppExcessOptionId = r[BSUPP_EXCESS_OPTION_ID] == null ? null : ((BigDecimal) r[BSUPP_EXCESS_OPTION_ID]).longValue();
				String bsuppExcessOptionCode = (String) r[BSUPP_EXCESS_OPTION_CODE];
				String bsuppExcessOptionDesc =(String) r[BSUPP_EXCESS_OPTION_DESC];
				String coverageId = (String) r[COVERAGE_ID];
				BigDecimal contribution = (BigDecimal) r[CONTRIBUTION];
				String strategyName = (String) r[STRATEGY_NAME];
				String benefitPlanDesc = (String) r[BENEFIT_PLAN_DESC];
				String coverageLevelDesc = (String) r[COVERAGE_DESC];
				
				if (previousStrategyId == null || !strategyId.equals(previousStrategyId)) {
					if (modelCompareStrategy != null) {
						groupFundingList.addAll(groupFundingMap.values());
						modelCompareStrategy.setGroupFundingList(groupFundingList);
						returnList.put(previousStrategyId, modelCompareStrategy);					
					}					
					strategyFlaxMaxContributions = getStrategyFlatMaxContributions(strategyId);
					strategyBpLimits = getStrategyBpLimits(strategyId);
					modelCompareStrategy = new ModelCompareStrategy();
					modelCompareStrategy.setCompanyId(companyId);
					modelCompareStrategy.setId(strategyId);
					modelCompareStrategy.setName(strategyName);
					groupFundingList = new ArrayList<>();
					groupFundingMap = new LinkedHashMap<>();
				}

				CoverageLevel coverageLevel = new CoverageLevel(coverageId, coverageLevelDesc, contribution);
				
				planType = Utils.getGenericPlanType(planType);

				GroupFunding gf = null;
				if (null != groupFundingMap.get(groupId)) {
					gf = groupFundingMap.get(groupId);
				} else {
					gf = new GroupFunding();
					gf.setId(groupId);
					gf.setBenefitProgram(benefitProgram);
					gf.setName(groupDesc);
					gf.setWaitTime(waitTime);
				}
				if (null != gf.getOfferTypeFunding().get(planType)) {
					gf.getOfferTypeFunding().get(planType).getCoverageLevels().add(coverageLevel);
				} else if (planType != null && !planType.isEmpty()) {
					BenefitOfferFunding bof = new BenefitOfferFunding();
					bof.setBaseFundPlan(baseFundPlan);
					bof.setFundingType(fundingType);
					bof.setBenefitPlanDesc(benefitPlanDesc);
					bof.setType(planType);
					bof.setOffered(true);
					bof.setWaiverAllowance(waiverAllowance);
					bof.setCoverageLevels(new ArrayList<>());
					if (coverageId != null) {
						bof.getCoverageLevels().add(coverageLevel);
					}
					bof.setCoverageLevelFundingFlatMax((List<CoverageLevel>) strategyFlaxMaxContributions.get(groupId, planType));
					bof.setFundingBasePlanLimits((List<CoverageLevel>) strategyBpLimits.get(groupId, planType));
					gf.getOfferTypeFunding().put(bof.getType(), bof);

				}
				
				if (BSSApplicationConstants.BSUPP.equals(fundingType)
						&& gf.getOfferTypeFunding().get(planType).getExcessOption() == null) {
					ModelCompareBenSuppExcessOption benSuppExcessOption = createModelCompareBenSuppExcessOption(
							company.getRealm().getId(), fundingModelId, bsuppExcessOptionId, bsuppExcessOptionCode,
							bsuppExcessOptionDesc, includeGroupExcessOptions);
					gf.getOfferTypeFunding().get(planType).setExcessOption(benSuppExcessOption);
				}

				if (null == groupFundingMap.get(groupId)) {
					groupFundingMap.put(gf.getId(), gf);
				}
				

				previousStrategyId = strategyId;
			}
		}
		groupFundingList.addAll(groupFundingMap.values());
		modelCompareStrategy.setGroupFundingList(groupFundingList);
		returnList.put(previousStrategyId, modelCompareStrategy);
		return returnList;
	}
	
	@Override
	public Map<String, Set<String>> getEecFunding(String companyCode, long realmPlanYearId) {
		Map<String, Set<String>> eecFunding = new HashMap<>();
		Query query = em.createNamedQuery("GET_EEC_FUNDING");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);

		List<Object[]> results = DaoUtils.getResultList(query, "GET_EEC_FUNDING");
		
		for (Object[] r : results) {
			String benefitProgram = (String) r[0];		
			String planType = (String) r[1];
			if (eecFunding.containsKey(benefitProgram)) {
				eecFunding.get(benefitProgram).add(planType);
			}
			else {
				eecFunding.put(benefitProgram, new HashSet<>(Arrays.asList(planType)));
			}
		}
		return eecFunding;
	}
	
	@Override
	public Map<Long, Set<String>> getPlanLevelOverrides(long strategyId) {
		Map<Long, Set<String>> planLevelOverrideMap = new HashMap<>();
		Query query = em.createNamedQuery(PLAN_LEVEL_FUNDING_OVERRIDES_BY_STRATEGY_GROUP);
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		List<Object[]> results = DaoUtils.getResultList(query, PLAN_LEVEL_FUNDING_OVERRIDES_BY_STRATEGY_GROUP);
		if (results != null) {
			for (Object[] r : results) {
				Long groupId = ((BigDecimal) r[0]).longValue();
				String planType = PlanTypesEnum.planType((String) r[1]).getName();
				planLevelOverrideMap.computeIfAbsent(groupId, k -> new HashSet<>()).add(planType);
			}
		}
		return planLevelOverrideMap;
	}
	
	@SuppressWarnings("unchecked")
	private MultiKeyMap getStrategyFlatMaxContributions(long strategyId) {

		Query query = em.createNamedQuery("MC_STRATEGY_FLAT_MAX_CONTRIBUTIONS");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);

		List<Object[]> results = DaoUtils.getResultList(query, "MC_STRATEGY_FLAT_MAX_CONTRIBUTIONS");
		MultiKeyMap strategyFlaxMaxContributionsMap = new MultiKeyMap();

		for (Object[] r : results) {
			Long groupId = ((BigDecimal) r[0]).longValue();
			String planType = Utils.getGenericPlanType((String) r[1]);
			String coverageId = (String) r[2];
			BigDecimal contribution = (BigDecimal) r[3];
			CoverageLevel coverageLevel = new CoverageLevel(coverageId, CoverageCodesEnums.nameFromId(coverageId), contribution);
			if (strategyFlaxMaxContributionsMap.containsKey(groupId, planType)) {
				((ArrayList<CoverageLevel>) strategyFlaxMaxContributionsMap.get(groupId, planType)).add(coverageLevel);
			} else {
				strategyFlaxMaxContributionsMap.put(groupId, planType, new ArrayList<CoverageLevel>(Arrays.asList(coverageLevel)));
			}
		}

		return strategyFlaxMaxContributionsMap;
	}
	
	
	@SuppressWarnings("unchecked")
	private MultiKeyMap getStrategyBpLimits(long strategyId) {
		Query query = em.createNamedQuery("MC_STRATEGY_BP_LIMITS");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);

		List<Object[]> results = DaoUtils.getResultList(query, "MC_STRATEGY_BP_LIMITS");
		MultiKeyMap strategyBpLimitsMap = new MultiKeyMap();

		for (Object[] r : results) {
			Long groupId = ((BigDecimal) r[0]).longValue();
			String planType = Utils.getGenericPlanType((String) r[1]);
			String coverageId = (String) r[2];
			BigDecimal contribution = (BigDecimal) r[3];
			CoverageLevel coverageLevel = new CoverageLevel(coverageId, CoverageCodesEnums.nameFromId(coverageId), contribution);
			if (strategyBpLimitsMap.containsKey(groupId, planType)) {
				((ArrayList<CoverageLevel>) strategyBpLimitsMap.get(groupId, planType)).add(coverageLevel);
			} else {
				strategyBpLimitsMap.put(groupId, planType, new ArrayList<CoverageLevel>(Arrays.asList(coverageLevel)));
			}
		}

		return strategyBpLimitsMap;
	}

	private ModelCompareBenSuppExcessOption createModelCompareBenSuppExcessOption(Long realmId, Long fundingModelId,
			Long bsuppExcessOptionId, String bsuppExcellOptionCode, String bsuppExcellOptionDesc, boolean includeGroupExcessOptions) {

		final int PLAN_TYPE = 0;
		final int DESCRIPTION = 1;
		ModelCompareBenSuppExcessOption bsuppExcessOption = null;

		if (bsuppExcessOptionId != null) {

			bsuppExcessOption = new ModelCompareBenSuppExcessOption();
			bsuppExcessOption.setOptionId(bsuppExcessOptionId);
			bsuppExcessOption.setOptionCode(bsuppExcellOptionCode);
			bsuppExcessOption.setOptionName(bsuppExcellOptionDesc);

			List<PlanTypeDescription> excessPlanTypeList = new ArrayList<>();

			Query q = em.createNamedQuery("STRATEGY_FUNDING_BEN_SUPP_EXCESS_PLAN_TYPES");
			q.setParameter("realmId", realmId);
			q.setParameter("strategyFundingId", fundingModelId);
			q.setParameter("includeGroupExcessOptions", includeGroupExcessOptions);

			List<Object[]> results = DaoUtils.getResultList(q, "STRATEGY_FUNDING_BEN_SUPP_EXCESS_PLAN_TYPES");

			for (Object[] r : results) {
				PlanTypeDescription excessPlanType = new PlanTypeDescription();
				excessPlanType.setPlanType((String) r[PLAN_TYPE]);
				excessPlanType.setDescription((String) r[DESCRIPTION]);
				excessPlanTypeList.add(excessPlanType);
			}
			bsuppExcessOption.setExcessVoluntaryPlanTypes(excessPlanTypeList);
		}

		return bsuppExcessOption;
	}
}
