package com.trinet.ambis.persistence.plancompare.dao.hrp.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.plancompare.dao.hrp.PlanCompareDao;
import com.trinet.ambis.persistence.plancompare.model.BenefitPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.MappedPlanDetailDto;
import com.trinet.ambis.persistence.plancompare.model.PlanYearDetailDto;
import com.trinet.ambis.util.DaoUtils;

/**
 * @author rpittala
 * 
 *         PlanCompareDaoImpl will provide the plan and plan year related
 *         information from database. Company current plans
 */

public class PlanCompareDaoImpl implements PlanCompareDao {

	public static final String CURREN_FUTURE_PLAN_YEAR_DETAILS = "CURREN_FUTURE_PLAN_YEAR_DETAILS";
	public static final String CURRENT_YEAR_PLANS = "CURRENT_YEAR_PLANS";
	public static final String FUTURE_YEAR_PLANS = "FUTURE_YEAR_PLANS";
	public static final String MAPPING_PLANS = "MAPPING_PLANS";

	private static final Logger logger = LoggerFactory.getLogger(PlanCompareDaoImpl.class);

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	/**
	 * Fetching the plan years for given quarter
	 * 
	 * @param quarterName
	 * @param planYearDate
	 * @param lastXYears
	 * @param nextXYears
	 * 
	 * @return List<PlanCompareDto>
	 */
	@Override
	public List<PlanYearDetailDto> findPlanYearDetailsBy(String quarterName, String planYearDate, int lastXYears,
			int nextXYears) {
		Query query = getQuery(quarterName, planYearDate).apply(CURREN_FUTURE_PLAN_YEAR_DETAILS);
		logger.info("Plan years query : {} ", query);
		query.setParameter(BSSQueryConstants.LASTXY_YEARS, lastXYears);
		query.setParameter(BSSQueryConstants.NEXTXY_YEARS, nextXYears);
		List<Object[]> planYearsStatuses = DaoUtils.getResultList(query, CURREN_FUTURE_PLAN_YEAR_DETAILS);
		List<PlanYearDetailDto> plans = null;
		if (!CollectionUtils.isEmpty(planYearsStatuses)) {
			plans = planYearsStatuses.stream().map(planCompareMapper()).collect(Collectors.toList());
		}

		return plans;
	}

	/**
	 * Find the current year plans for the given company code and realm
	 * 
	 * @param companyCode
	 * @param realmYearId
	 * 
	 * @return List<BenefitPlanDetailDto>
	 */
	@Override
	public List<BenefitPlanDetailDto> findSubmittedStrategyPlansBy(String companyCode, String realmYearId) {
		Query query = getQuery(companyCode, realmYearId).apply(CURRENT_YEAR_PLANS);
		logger.info("Current year plans query : {} ", query);
		return getPlans(query, CURRENT_YEAR_PLANS, planMapper());
	}

	/**
	 * Find all plans for the given company code and realm
	 * 
	 * @param realmYearId
	 * 
	 * @return List<BenefitPlanDetailDto>
	 */
	@Override
	public List<BenefitPlanDetailDto> findAllFutureYearPlansBy(String realmYearId) {
		Query query = getQuery(realmYearId).apply(FUTURE_YEAR_PLANS);
		logger.info("Future year plans query : {} ", query);
		return getPlans(query, FUTURE_YEAR_PLANS, planMapper());
	}

	@Override
	public List<MappedPlanDetailDto> findMappingBenefitPlansBy(String futureRealmYearId, String currentRealmYearId) {
		Query query = getQuery(futureRealmYearId, currentRealmYearId).apply(MAPPING_PLANS);
		logger.info("Mapped plans for the curent year query : {} ", query);
		return getMappingPlans(query, MAPPING_PLANS, mappedPlanMapper());
	}

	/**
	 * Getting the plans for the given query and it will prepare the response.
	 * 
	 * @param query
	 * @param queryName
	 * @param mapper
	 * 
	 * @return List<BenefitPlanDetailDto>
	 */
	private List<BenefitPlanDetailDto> getPlans(Query query, String queryName,
			Function<Object[], BenefitPlanDetailDto> mapper) {
		List<Object[]> planYearsStatuses = DaoUtils.getResultList(query, queryName);
		List<BenefitPlanDetailDto> plans = null;
		if (!CollectionUtils.isEmpty(planYearsStatuses)) {
			plans = planYearsStatuses.stream().map(mapper).collect(Collectors.toList());
		}
		return plans;
	}

	/**
	 * Getting the plans for the given query and it will prepare the response.
	 * 
	 * @param query
	 * @param queryName
	 * @param mapper
	 * 
	 * @return List<MappedPlanDetailDto>
	 */
	private List<MappedPlanDetailDto> getMappingPlans(Query query, String queryName,
			Function<Object[], MappedPlanDetailDto> mapper) {
		List<Object[]> planYearsStatuses = DaoUtils.getResultList(query, queryName);
		List<MappedPlanDetailDto> plans = null;
		if (!CollectionUtils.isEmpty(planYearsStatuses)) {
			plans = planYearsStatuses.stream().map(mapper).collect(Collectors.toList());
		}
		return plans;
	}

	/**
	 * It will map the response to the current year plan response
	 * 
	 * @return BenefitPlanDetailDto
	 */
	private Function<Object[], BenefitPlanDetailDto> planMapper() {
		return plan -> {
			BenefitPlanDetailDto currentYearPlans = new BenefitPlanDetailDto();
			currentYearPlans.setPlanId(String.valueOf(plan[0]));
			currentYearPlans.setOfferType(String.valueOf(plan[1]));
			currentYearPlans.setPlanName(String.valueOf(plan[2]));
			return currentYearPlans;
		};
	}

	/**
	 * It will map the response to the plancompare response
	 * 
	 * @return PlanYearDetailDto
	 */
	private Function<Object[], PlanYearDetailDto> planCompareMapper() {
		return plan -> {
			PlanYearDetailDto planCompareDto = new PlanYearDetailDto();
			planCompareDto.setPlanYear(String.valueOf(plan[0]));
			planCompareDto.setRealmYearId(String.valueOf(plan[1]));
			planCompareDto.setPlanYearStartDate(String.valueOf(plan[2]));
			planCompareDto.setPlanYearEndDate(String.valueOf(plan[3]));
			return planCompareDto;
		};
	}

	/**
	 * It will map the response to the current year plan response
	 * 
	 * @return MappedPlanDetailDto
	 */
	private Function<Object[], MappedPlanDetailDto> mappedPlanMapper() {
		return plan -> {
			MappedPlanDetailDto mappedPlan = new MappedPlanDetailDto();
			mappedPlan.setParentId(String.valueOf(plan[0]));
			mappedPlan.setPlanId(String.valueOf(plan[1]));
			mappedPlan.setPlanName(String.valueOf(plan[2]));
			mappedPlan.setOfferType(String.valueOf(plan[3]));
			return mappedPlan;
		};
	}

	/**
	 * Based on the key this will return the query object and it will prepare the
	 * named parameters for the query.
	 * 
	 * @param params
	 * 
	 * @return Query
	 */
	private Function<String, Query> getQuery(String... params) {
		return key -> {
			switch (key) {
			case CURREN_FUTURE_PLAN_YEAR_DETAILS: {
				Query query = em.createNamedQuery(CURREN_FUTURE_PLAN_YEAR_DETAILS);
				query.setParameter(BSSQueryConstants.QUARTER_NAME, params[0]);
				query.setParameter(BSSQueryConstants.PLAN_YEAR_DATE, params[1]);
				return query;
			}
			case CURRENT_YEAR_PLANS: {
				Query query = em.createNamedQuery(CURRENT_YEAR_PLANS);
				query.setParameter(BSSQueryConstants.COMPANY_CODE, params[0]);
				query.setParameter(BSSQueryConstants.REALM_YEAR_ID, params[1]);
				return query;
			}
			case FUTURE_YEAR_PLANS: {
				Query query = em.createNamedQuery(FUTURE_YEAR_PLANS);
				query.setParameter(BSSQueryConstants.REALM_YEAR_ID, params[0]);
				return query;
			}
			case MAPPING_PLANS: {
				Query query = em.createNamedQuery(MAPPING_PLANS);
				query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, params[0]);
				query.setParameter(BSSQueryConstants.PREVIOUS_REALM_PLAN_YEAR_ID, params[1]);
				return query;
			}
			default:
				return null;
			}
		};
	}

}
