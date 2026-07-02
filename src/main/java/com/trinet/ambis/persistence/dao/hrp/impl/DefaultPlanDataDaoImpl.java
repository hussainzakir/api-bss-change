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
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.DefaultPlanDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.prospect.enums.BenefitTypeEnum;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * 
 */
public class DefaultPlanDataDaoImpl implements DefaultPlanDataDao {
	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	private static final String DUMMY_STRING = "DUMMY_STRING";
	private static final String INSERT_PROSP_STRATEGY_DEFAULT_PLAN_ASSIGNMENTS = "INSERT_PROSP_STRATEGY_DEFAULT_PLAN_ASSIGNMENTS";

	@Override
	public Map<String, Map<String, Long>> getRegionalDefaultPlansByPlanType(Company company) {

		Map<String, Map<String, Long>> regionalDefaultPlansByPlanType = new HashMap<>();

		Query q = em.createNamedQuery("REGIONAL_DEFAULT_PLANS_BY_PLAN");
		q.setParameter(BSSQueryConstants.STATE, company.getHeadQuatersState());
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYearId());
		q.setParameter(BSSQueryConstants.OE_QUARTER, company.getRealmPlanYear().getOeQuarter());
		q.setParameter(BSSQueryConstants.PICK_CHOOSE_FLAG, RulesAndConfigsUtils.findPickChooseWithExceptions(company));
		q.setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearEnd());
		q.setParameter(BSSQueryConstants.BUNDLE_ID, Objects.nonNull(company.getBundleId()) ?
				company.getBundleId() : BSSQueryConstants.ORACLE_NULL);
		if (company.isTexasSitus()) {
			q.setParameter(BSSQueryConstants.SITUS, "TX");
		} else {
			q.setParameter(BSSQueryConstants.SITUS, "FL");
		}
		List<Object[]> results = DaoUtils.getResultList(q, "REGIONAL_DEFAULT_PLANS_BY_PLAN");
		if (results != null) {
			for (Object[] r : results) {
				String planType = (String) r[0];
				String benefitPlan = (String) r[1];
				Long portfolioId = ((BigDecimal) r[2]).longValue();
				regionalDefaultPlansByPlanType.putIfAbsent(planType, new HashMap<>());
				regionalDefaultPlansByPlanType.get(planType).put(benefitPlan, portfolioId);
			}

		}
		return regionalDefaultPlansByPlanType;
	}

	@Override
	public void insertStrategyDefaultAssignmentsBy(Company company, Set<Long> primaryPortfolioIds,
			Set<Long> altPortfolioIds, long strategyId) {
		if (CollectionUtils.isEmpty(primaryPortfolioIds)) {
			primaryPortfolioIds = Set.of(999999999L); // Setting invalid value so that IN clause don't fail
		}
		if (CollectionUtils.isEmpty(altPortfolioIds)) {
			altPortfolioIds = Set.of(999999999L); // Setting invalid value so that IN clause don't fail
		}
		Query q = em.createNamedQuery("INSERT_PROSP_STRATEGT_DEFAULT_PLANS");
		q.setParameter("STRATEGY_ID", strategyId);
		q.setParameter("PRM_PORTFOLIO_IDS", primaryPortfolioIds);
		q.setParameter("ALT_PORTFOLIO_IDS", altPortfolioIds);
		q.setParameter("COMPANY_ID", company.getId());
		DaoUtils.executeUpdate(q, "INSERT_PROSP_STRATEGT_DEFAULT_PLANS");
	}

	@Override
	public List<String> getEligibleRegionalPlans(String region, long strategyId, long realmPlanYearId,
			String benefitType, String employeeId) {
		Query q = em.createNamedQuery("ELIGIBLE_REGIONAL_PLANS_FOR_EE");
		q.setParameter("REGION", region);
		q.setParameter("REALM_PLAN_YEAR_ID", realmPlanYearId);
		q.setParameter("STRATEGY_ID", strategyId);
		q.setParameter("BENEFIT_TYPE", benefitType);
		q.setParameter("EMPLID", employeeId);
		return DaoUtils.getResultStringList(q, "ELIGIBLE_REGIONAL_PLANS_FOR_EE");
	}

	@Override
	public void copyStrategyAssignments(long fromStrategyId, long toStrategyId) {
		Query q = em.createNamedQuery("COPY_PROSP_STRATEGT_DEFAULT_PLANS");
		q.setParameter("FROM_STRATEGY_ID", fromStrategyId);
		q.setParameter("TO_STRATEGY_ID", toStrategyId);
		DaoUtils.executeUpdate(q, "COPY_PROSP_STRATEGT_DEFAULT_PLANS");
	}

	@Override
	@Transactional
	public void insertStrategyDefaultAssignmentsBy(Set<String> emplIds, long strategyId, List<Long> primaryPortfolioIds,
			List<Long> alternatePortfolioIds, Set<String> benTypes) {
		Query q = em.createNamedQuery(INSERT_PROSP_STRATEGY_DEFAULT_PLAN_ASSIGNMENTS);
		List<String> dentalAndVisionBenTypes = new ArrayList<>();
		String medBenType = DUMMY_STRING;
		if (benTypes.contains(BenefitTypeEnum.DENTAL.getBenTypeCode()))
			dentalAndVisionBenTypes.addAll(BSSApplicationConstants.DENTAL_PLAN_TYPES);
		if (benTypes.contains(BenefitTypeEnum.VISION.getBenTypeCode()))
			dentalAndVisionBenTypes.addAll(BSSApplicationConstants.VISION_PLAN_TYPES);
		if (CollectionUtils.isEmpty(dentalAndVisionBenTypes))
			dentalAndVisionBenTypes.add(DUMMY_STRING);
		if (benTypes.contains(BSSApplicationConstants.MEDICAL_PLAN_TYPE))
			medBenType = BSSApplicationConstants.MEDICAL_PLAN_TYPE;

		if (CollectionUtils.isEmpty(primaryPortfolioIds)) {
			primaryPortfolioIds.add(Long.valueOf(99999999));
		}
		if (CollectionUtils.isEmpty(alternatePortfolioIds)) {
			alternatePortfolioIds.add(Long.valueOf(99999999));
		}
		q.setParameter("DEN_VIS_BEN_TYPE_CODES", dentalAndVisionBenTypes);
		q.setParameter("MED_BEN_TYPE_CODE", medBenType);
		q.setParameter("STRATEGY_IDS", Set.of(strategyId));
		q.setParameter("IGNORE_STRATEGY_ID", 0);
		q.setParameter("GROUP_IDS", Set.of(DUMMY_STRING));
		q.setParameter("IGNORE_GROUP_ID", 1);
		q.setParameter("EMPLIDS", emplIds);
		q.setParameter("IGNORE_EMPL_ID", 0);
		q.setParameter("PRIMARY_MED_PORTFOLIO", primaryPortfolioIds);
		q.setParameter("ALTERNATE_MED_PORTFOLIO", alternatePortfolioIds);
		DaoUtils.executeUpdate(q, INSERT_PROSP_STRATEGY_DEFAULT_PLAN_ASSIGNMENTS);
	}

	@Override
	public void insertStrategyDefaultAssignmentsBy(Set<Long> strategyIds, Set<Long> groupIds,
			List<Long> primaryPortfolioIds, List<Long> alternatePortfolioIds, Set<String> benTypes) {
		Query q = em.createNamedQuery(INSERT_PROSP_STRATEGY_DEFAULT_PLAN_ASSIGNMENTS);
		List<String> dentalAndVisionBenTypes = new ArrayList<>();
		if (benTypes.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)
				|| benTypes.contains(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE)) {
			dentalAndVisionBenTypes.addAll(BSSApplicationConstants.DENTAL_PLAN_TYPES);
		}
		if (benTypes.contains(BSSApplicationConstants.VISION_PLAN_TYPE)
				|| benTypes.contains(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE)) {
			dentalAndVisionBenTypes.addAll(BSSApplicationConstants.VISION_PLAN_TYPES);
		}
		if (CollectionUtils.isEmpty(dentalAndVisionBenTypes)) {
			dentalAndVisionBenTypes.add(DUMMY_STRING);
		}
		if (CollectionUtils.isEmpty(primaryPortfolioIds)) {
			primaryPortfolioIds.add(Long.valueOf(99999999));
		}
		if (CollectionUtils.isEmpty(alternatePortfolioIds)) {
			alternatePortfolioIds.add(Long.valueOf(99999999));
		}

		q.setParameter("MED_BEN_TYPE_CODE",
				benTypes.contains(BSSApplicationConstants.MEDICAL_PLAN_TYPE) ? BSSApplicationConstants.MEDICAL_PLAN_TYPE
						: DUMMY_STRING);
		q.setParameter("DEN_VIS_BEN_TYPE_CODES", dentalAndVisionBenTypes);
		q.setParameter("STRATEGY_IDS", CollectionUtils.isEmpty(strategyIds) ? DUMMY_STRING : strategyIds);
		q.setParameter("IGNORE_STRATEGY_ID", CollectionUtils.isEmpty(strategyIds) ? 1 : 0);
		q.setParameter("GROUP_IDS", CollectionUtils.isEmpty(groupIds) ? DUMMY_STRING : groupIds);
		q.setParameter("IGNORE_GROUP_ID", CollectionUtils.isEmpty(groupIds) ? 1 : 0);
		q.setParameter("EMPLIDS", DUMMY_STRING);
		q.setParameter("IGNORE_EMPL_ID", 1);
		q.setParameter("PRIMARY_MED_PORTFOLIO", primaryPortfolioIds);
		q.setParameter("ALTERNATE_MED_PORTFOLIO", alternatePortfolioIds);
		DaoUtils.executeUpdate(q, INSERT_PROSP_STRATEGY_DEFAULT_PLAN_ASSIGNMENTS);
	}

	@Override
	public Map<String, Set<String>> getMissingEmplPlanAssignmentsBy(Set<String> emplIds, long strategyId,
			long companyId) {
		Map<String, Set<String>> benTypeToEmplIds = new HashMap<>();

		Query q = em.createNamedQuery("FIND_MISSING_EMPL_PLAN_ASSIGNMENTS");
		q.setParameter("emplIds", emplIds);
		q.setParameter("strategyId", strategyId);
		q.setParameter("companyId", companyId);
		List<Object[]> results = DaoUtils.getResultList(q, "FIND_MISSING_EMPL_PLAN_ASSIGNMENTS");
		if (results != null) {
			for (Object[] r : results) {
				String emplId = (String) r[0];
				String benType = (String) r[1];
				benTypeToEmplIds.putIfAbsent(benType, new HashSet<>());
				benTypeToEmplIds.get(benType).add(emplId);
			}

		}
		return benTypeToEmplIds;
	}

}
