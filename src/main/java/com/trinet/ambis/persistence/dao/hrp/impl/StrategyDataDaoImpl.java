package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.service.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiKeyMap;
import org.hibernate.annotations.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.exception.BSSErrorResponseMessages;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupEmployeePlanRateData;
import com.trinet.ambis.service.model.StrategySubmitIssueReport.Bdm;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.Utils;

public class StrategyDataDaoImpl implements StrategyDataDao {
	private static final Logger logger = LoggerFactory.getLogger(StrategyDataDaoImpl.class);
	private static final String STRATEGY_GROUP_PLANTYPES = "STRATEGY_GROUP_PLANTYPES";
	private static final String OMS_STRATEGY_GROUP_PLANTYPE_PLAN_COST = "OMS_STRATEGY_GROUP_PLANTYPE_PLAN_COST";
	private static final String STRATEGY_GROUP_PLANTYPE_PLAN_COST = "STRATEGY_GROUP_PLANTYPE_PLAN_COST";
	private static final String DELETE_STRATEGY_ESTIMATE = "deleteStrategyEstimate";
	private static final String  STRATEGY_ID= "STRATEGY_ID";

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;

	
	@Autowired
	private EmployeeBenefitGroupDao employeeBenefitGroupDao;

	@Override
	public Map<Long, List<PlanContribution>> getByPlanSelectionId(List<Long> ids,
			Map<String, List<BenefitPlanRate>> planRates, boolean contributionRequired) {
		Query q = em.createNamedQuery("getContributionsByPlanIds");
		logger.info("LIST OF IDS : {}", ids);
		q.setParameter("ids", ids);
		Map<Long, List<PlanContribution>> contributionsMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getContributionsByPlanIds");
		if (results != null) {
			for (Object[] r : results) {
				PlanContribution planContribution = new PlanContribution();
				planContribution.setId(((BigDecimal) r[0]).longValue());
				long planId = ((BigDecimal) r[1]).longValue();
				planContribution.setType((String) r[6]);
				planContribution.setEmployerPercent((BigDecimal) r[3]);
				planContribution.setHeadcount(((BigDecimal) r[4]).intValue());
				planContribution.setHsaHeadcount(((BigDecimal) r[10]).intValue());
				planContribution.setBssBenefitPlanId((String) r[5]);
				if (contributionRequired) {
					planContribution.setEmployerContribution((BigDecimal) r[7]);
					planContribution.setEmployeeContribution((BigDecimal) r[8]);
				}
				planContribution.setOverrideType((String) r[9]);
				List<PlanContribution> list = contributionsMap.get(planId);

				if (list == null) {
					list = new ArrayList<>();
				}
				list.add(planContribution);
				contributionsMap.put(planId, list);
			}
		}
		return contributionsMap;
	}

	@Override
	public Map<String, PlanPackage> getPlanPackagesByStrategyIdAndBenefitGroupId(long strategyId, long groupId,
			long planYearId, boolean renewals) {
		Query q = em.createNamedQuery("PLAN_PACKAGE_BY_STRATEGY_GROUP");
		q.setParameter(STRATEGY_ID, strategyId);
		q.setParameter("GROUP_ID", groupId);
		q.setParameter("PLANYEAR_ID", planYearId);
		Map<String, PlanPackage> planPackageMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "PLAN_PACKAGE_BY_STRATEGY_GROUP");
		for (Object[] r : results) {
			String planType = (String) r[1];
			String planTypeDesc = null;
			
			if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType)) {
				planTypeDesc = BSSApplicationConstants.MEDICAL;
			} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
				planTypeDesc = BSSApplicationConstants.DENTAL;
			} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planType)) {
				planTypeDesc = BSSApplicationConstants.VISION;
			}
			
			String pkgType = null;
			long pkgTypeId = 0L;
			if (!renewals) {
				pkgType = (String) r[2];
				if (BSSApplicationConstants.TOP_QUALITY_NAME.equalsIgnoreCase(pkgType)) {
					pkgTypeId = 3L;
				} else if (BSSApplicationConstants.CONSERVATIVE_PACKAGE_NAME.equalsIgnoreCase(pkgType)) {
					pkgTypeId = 1L;
				} else if (BSSApplicationConstants.BALANCED_PACKAGE_NAME.equalsIgnoreCase(pkgType)) {
					pkgTypeId = 2L;
				}
			}
			if (null != planPackageMap.get(planTypeDesc)) {
				PlanPackage pkg = planPackageMap.get(planTypeDesc);
				if (r[4] != null) {
					if (null == pkg.getCoverageLevelFunding().get((String) r[5])) {
						pkg.getCoverageLevelFunding().put((String) r[5], ((BigDecimal) r[4]));
					}
				} else {
					if (null == pkg.getCoverageLevelFunding().get((String) r[5])) {
						pkg.getCoverageLevelFunding().put((String) r[5], BigDecimal.ZERO);
					}
				}
				if (r[9] != null) {
					pkg.getCoverageLevelFundingFlatMax().put((String) r[10], ((BigDecimal) r[9]));
				}
			} else {
				PlanPackage pkg = new PlanPackage();
				pkg.setFundingBasePlan((String) r[0]);
				pkg.setPlanType(planType);
				pkg.setId(pkgTypeId);
				pkg.setTemplateId(pkgTypeId);
				pkg.setName(pkgType);
				pkg.setStrategyId(strategyId);
				pkg.setFundingType((String) r[6]);
				pkg.setFundingModelId(((BigDecimal) r[7]).longValue());
				pkg.setWaiverAllowance((BigDecimal) r[11]);
				pkg.setBsuppExcessOption((BigDecimal) r[12]);
				BigDecimal customizedValue = ((BigDecimal) r[3]);
				pkg.setCustomized(customizedValue.compareTo(BigDecimal.ZERO) > 0);
				if (Constants.voluntaryPlanTypeList.contains(planType)) {
					pkg.setEmployeePaid(true);
				}
				if (r[4] != null) {
					if (null == pkg.getCoverageLevelFunding().get((String) r[5])) {
						pkg.getCoverageLevelFunding().put((String) r[5], ((BigDecimal) r[4]));
					}
				} else {
					if (null == pkg.getCoverageLevelFunding().get((String) r[5])) {
						pkg.getCoverageLevelFunding().put((String) r[5], BigDecimal.ZERO);
					}
				}
				if (r[9] != null) {
					pkg.getCoverageLevelFundingFlatMax().put((String) r[10], ((BigDecimal) r[9]));
				}
				if (null != pkg.getFundingBasePlan()) {
					FundingBasePlan fBasePlan = new FundingBasePlan();
					fBasePlan.setFundingBasePlan(pkg.getFundingBasePlan());
					BigDecimal planCarrierId = (BigDecimal) r[8];
					if (null != planCarrierId) {
						fBasePlan.setPlanCarrierId(planCarrierId.longValue());
					}
					pkg.getFundingBasePlans().add(fBasePlan);
					pkg.getBenefitPlans().add(pkg.getFundingBasePlan());
					pkg.getPlanCarrierIds().add(fBasePlan.getPlanCarrierId());
				}
				planPackageMap.put(planTypeDesc, pkg);
			}
		}
		return planPackageMap;

	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteAllPlanContributionsByBenefitgroupAndStrategy(long groupId, long strategyId) {
		Query q = em.createNamedQuery("deletePlanContributions");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		q.setParameter(BSSQueryConstants.GROUP_ID, groupId);

		int num = DaoUtils.executeUpdate(q, "deletePlanContributions");
		logger.info("DELETED NUMBER OF PLANS : {}", num);
		return num;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteAllPlanSelectionsByBenefitgroupAndStrategy(long groupId, long strategyId) {
		Query q = em.createNamedQuery("deletePlanSelections");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		q.setParameter(BSSQueryConstants.GROUP_ID, groupId);
		int num = DaoUtils.executeUpdate(q, "deletePlanSelections");
		logger.info("DELETED NUMBER OF CONTRIBUTIONS : {}", num);
		return num;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteStrategyFundingsByBenefitgroupAndStrategy(long groupId, long strategyId) {
		Query q = em.createNamedQuery("deleteStrategyFundings");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		q.setParameter(BSSQueryConstants.GROUP_ID, groupId);
		int num = DaoUtils.executeUpdate(q, "deleteStrategyFundings");
		logger.info("DELETED NUMBER OF STRATEGY FUNDINGS : {}", num);
		return num;
	}
	
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteAllPlanContributionsByStrategy(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("deletePlanContributionsByStrategy");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		
		int num = DaoUtils.executeUpdate(q, "deletePlanContributionsByStrategy");
		logger.info("DELETED NUMBER OF PLANS : {}", num);
		return num;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteAllPlanSelectionsByStrategy(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("deletePlanSelectionsByStrategy");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);

		int num = DaoUtils.executeUpdate(q, "deletePlanSelectionsByStrategy");
		logger.info("DELETED NUMBER OF CONTRIBUTIONS : {}", num);
		return num;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteAllPlanContributionsBy(Set<Long> strategyIds, String planType) {
		Query q = em.createNamedQuery("deletePlanContributionsByStrategyAndPlanType");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		q.setParameter(BSSQueryConstants.PLAN_TYPE, planType);

		int num = DaoUtils.executeUpdate(q, "deletePlanContributionsByStrategyAndPlanType");
		logger.info("DELETED NUMBER OF PLANS : {}", num);
		return num;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteAllPlanSelectionsBy(Set<Long> strategyIds, String planType) {
		Query q = em.createNamedQuery("deletePlanSelectionsByStrategyAndPlanType");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		q.setParameter(BSSQueryConstants.PLAN_TYPE, planType);

		int num = DaoUtils.executeUpdate(q, "deletePlanSelectionsByStrategyAndPlanType");
		logger.info("DELETED NUMBER OF CONTRIBUTIONS : {}", num);
		return num;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyFundDetailByStrategy(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("deleteStrategyFundDetailByStrategy");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		
		int num = DaoUtils.executeUpdate(q, "deleteStrategyFundDetailByStrategy");
		logger.info("DELETED NUMBER OF STRATEGY FUNDING DETAILS : {}", num);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyFlatMaxByStrategy(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("deleteStrategyFundFlatMaXByStrategy");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		
		int num = DaoUtils.executeUpdate(q, "deleteStrategyFundFlatMaXByStrategy");
		logger.info("DELETED NUMBER OF STRATEGY FUNDING DETAILS : {}", num);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteStrategyFundModelByStrategy(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("deleteStrategyFundModelByStrategy");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);

		int num = DaoUtils.executeUpdate(q, "deleteStrategyFundModelByStrategy");
		logger.info("DELETED NUMBER OF STRATEGY FUNDING MODEL : {}", num);
		return num;
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyById(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("deleteStrategyById");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		
		int num = DaoUtils.executeUpdate(q, "deleteStrategyById");
		logger.info("DELETED NUMBER OF STRATEGY : {}", num);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteEmployees(String companyCode, long realmYearId) {
		Query q = em.createNamedQuery("deleteEmployeeByCompanyRealmYearId");
		q.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmYearId);
		q.setParameter(BSSQueryConstants.COMPANY, companyCode);

		int num = DaoUtils.executeUpdate(q, "deleteEmployeeByCompanyRealmYearId");
		logger.info("DELETED NUMBER OF STRATEGY FUNDINGS : {}", num);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteGroupByCompanyId(long companyId) {
		Query q = em.createNamedQuery("deleteGroupByCompanyId");
		q.setParameter(BSSQueryConstants.COMPANY_ID, companyId);

		int num = DaoUtils.executeUpdate(q, "deleteGroupByCompanyId");
		logger.info("DELETED NUMBER OF GROUPS : {}", num);
	}

    @Override
    public void deleteGroupByIds(Set<Long> groupIds) {
        Query q = em.createNamedQuery("deleteGroupByIds");
        q.setParameter(BSSQueryConstants.GROUP_IDS, groupIds);

        DaoUtils.executeUpdate(q, "deleteGroupByIds");
    }
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteGroupCovHeadCount(long companyId) {
		Query q = em.createNamedQuery("deleteGroupCovHeadCount");
		q.setParameter(BSSQueryConstants.COMPANY_ID, companyId);

		int num = DaoUtils.executeUpdate(q, "deleteGroupCovHeadCount");
		logger.info("DELETED NUMBER OF GROUP_COV_HEADCOUNT ROWS : {}", num);
	}

    @Override
    public void deleteGroupCovHeadCountByGroupIds(Set<Long> groupIds) {
        Query q = em.createNamedQuery("deleteGroupCovHeadCountByGroupIds");
        q.setParameter(BSSQueryConstants.GROUP_IDS, groupIds);

        DaoUtils.executeUpdate(q, "deleteGroupCovHeadCountByGroupIds");
    }
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteGroupRate(long companyId) {
		Query q = em.createNamedQuery("deleteGroupRate");
		q.setParameter(BSSQueryConstants.COMPANY_ID, companyId);

		int num = DaoUtils.executeUpdate(q, "deleteGroupRate");
		logger.info("DELETED NUMBER OF GROUP_RATE ROWS : {}", num);
	}

    @Override
    public void deleteGroupRateByGroupIds(Set<Long> groupIds) {
        Query q = em.createNamedQuery("deleteGroupRateByGroupIds");
        q.setParameter(BSSQueryConstants.GROUP_IDS, groupIds);

        DaoUtils.executeUpdate(q, "deleteGroupRateByGroupIds");
    }
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyGroup(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("DELETE_STRATEGY_GROUP");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);

		int num = DaoUtils.executeUpdate(q, "DELETE_STRATEGY_GROUP");
		logger.info("DELETED NUMBER OF XBSS_STRATEGY_GROUP ROWS : {}", num);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteEmployeeStrategyGroup(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("DELETE_EMPLOYEE_STRATEGY_GROUP_FOR_COMPANY");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);

		int num = DaoUtils.executeUpdate(q, "DELETE_EMPLOYEE_STRATEGY_GROUP_FOR_COMPANY");
		logger.info("DELETED NUMBER OF XBSS_EMPLOYEE_STRATEGY_GROUP ROWS : {}", num);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyGroupCovHeadCount(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("DELETE_STRATEGY_GROUP_COV_HC");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);

		int num = DaoUtils.executeUpdate(q, "DELETE_STRATEGY_GROUP_COV_HC");
		logger.info("DELETED NUMBER OF XBSS_STRATEGY_GROUP_COV_HC ROWS : {}", num);
	}
	
	@Override
	public Map<String, BigDecimal> getAdditionalBenefitPlanEstCost(long planYearId) {
		Map<String, BigDecimal> map = new HashMap<>();

		/* get benefit program and effective date from realm tables */
		Query q = em.createNamedQuery("getRealmClonePgmEffdt");
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYearId);
		
		List<Object[]> results = DaoUtils.getResultList(q, "getRealmClonePgmEffdt");
		if (!CollectionUtils.isEmpty(results)) {
			Object[] r = results.get(0) ;
			String benefitProgram = (String) r[0];
			Date effdt = (Date) r[1];
			
			/* get disability and life insurance maximums from PeopleSoft */
			q = entityManager.createNamedQuery("LIFE_DISABILITY_COVERAGE");
			q.setParameter("benProg", benefitProgram);
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			String newDateStr = formatter.format(effdt);
			q.setParameter("effdt", newDateStr);
		
			List<Object[]> results2 = DaoUtils.getResultList(q, "LIFE_DISABILITY_COVERAGE");
		
			for ( Object[] r2 : results2 ) {
				String plan = (String) r2[1];
				BigDecimal maxCovrg = (BigDecimal) r2[2];
				map.put(plan, maxCovrg);
			}
		}

		/* original query provides cost of commuter benefit */
		q = em.createNamedQuery("getAdditionalPlansEstCost");
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYearId);

		List<Object[]> results3 = DaoUtils.getResultList(q, "getAdditionalPlansEstCost");

		for ( Object[] r : results3 ) {
			String planType = (String) r[0];
			String plan = (String) r[1];
			BigDecimal estCost = (BigDecimal) r[3];
			if (Constants.COMMUTER_CODE.equals(planType)) {
				map.put(plan, estCost);
			}
		}
		return map;
	}

	@Override
	public Map<String, PlanTypeDescription> getPlanTypeDescriptions(long planYearId) {
		Query q = em.createNamedQuery("getPlanTypeDescriptions");
		q.setParameter(BSSQueryConstants.PLAN_YEAR_ID, planYearId);
		Map<String, PlanTypeDescription> map = new HashMap<>();

		List<Object[]> results = DaoUtils.getResultList(q, "getPlanTypeDescriptions");

		for (Object[] r : results) {
			PlanTypeDescription planTypeDesc = new PlanTypeDescription();
			planTypeDesc.setPlanType((String) r[0]);
			if (planTypeDesc.getPlanType().equals("A3")) {
				planTypeDesc.setType((PlanTypesEnum.CMTR.getName()));
			} else if (planTypeDesc.getPlanType().equals("23")) {
				planTypeDesc.setType((PlanTypesEnum.LIFE.getName()));
			} else if (planTypeDesc.getPlanType().equals("1D") || planTypeDesc.getPlanType().equals("11")) {
				planTypeDesc.setType(PlanTypesEnum.DENTAL.getName());
			} else if (planTypeDesc.getPlanType().equals("10")) {
				planTypeDesc.setType((PlanTypesEnum.MEDICAL.getName()));
			} else if (planTypeDesc.getPlanType().equals("1V") || planTypeDesc.getPlanType().equals("14")) {
				planTypeDesc.setType(PlanTypesEnum.VISION.getName());
			} else {
				planTypeDesc.setType((String) r[2]);
			}
			planTypeDesc.setDescription((String) r[1]);
			map.put((String) r[0], planTypeDesc);
		}
		return map;
	}

	@Override
	public int getStrategiesHistoryCount(String companyCode, long realmYrId) {
		Query q = em.createNamedQuery("getStrategiesHistoryCount");
		q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		q.setParameter("prevRealmYrId", realmYrId);
		return ((BigDecimal) DaoUtils.getSingleResult(q, "getStrategiesHistoryCount")).intValue();
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public EntityManager getEm() {
		return this.em;
	}
	
	public void setEmployeeBenefitGroupDao(EmployeeBenefitGroupDao employeeBenefitGroupDao) {
		this.employeeBenefitGroupDao = employeeBenefitGroupDao;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int updateStrategySubmitFlag(long companyId) {
		Query q = em.createNamedQuery("updateStrategySubmitFlag");
		q.setParameter(BSSQueryConstants.COMPANY_ID, companyId);

		return DaoUtils.executeUpdate(q, "updateStrategySubmitFlag");
	}
	
	@Override
	public List<Strategy> getHistoryStrategies(String companyCode, long realmPlanYearId) {
		Query q = em.createNamedQuery("getHistoryStrategies");
		q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		List<Object[]> results = DaoUtils.getResultList(q, "getHistoryStrategies");
		List<Strategy> strategyHistoryList = new ArrayList<>();
		for (Object[] r : results) {
			Strategy strategy = new Strategy();
			strategy.setId(((BigDecimal) r[0]).longValue());
			strategy.setCompanyId(((BigDecimal) r[1]).longValue());
			strategy.setName((String) r[2]);
			strategy.setComments((String) r[3]);
			strategy.setCurrentYearTotalCost(((BigDecimal) r[4]));
			strategy.setEstimatedTotalCost(((BigDecimal) r[5]));
			strategy.setPercentChange(((BigDecimal) r[6]));
			strategy.setSubmitDate((Date) r[7]);
			strategy.setSubmitted(true);
			strategy.setTotalBudget(((BigDecimal) r[9]));
			strategy.setType((String) r[10]);
			strategy.setRealmPlanYearId(((BigDecimal) r[11]).longValue());
			strategy.setBudgetFactor(((BigDecimal) r[12]).intValue());
			strategyHistoryList.add(strategy);
		}
		return strategyHistoryList;
	}
	
	@Override
	public Strategy getCurrentStrategy(String companyCode, long id) {
        Query q = em.createNamedQuery("getCurrentStrategy");
        q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
        q.setParameter( BSSQueryConstants.REALM_PLAN_YEAR_ID, id);
		List<Object[]> results = DaoUtils.getResultList(q, "getCurrentStrategy");
        Strategy strategy = null;
        
        for (Object[] r : results) {
           strategy = new Strategy();
           strategy.setId(((BigDecimal) r[0]).longValue());
           strategy.setCompanyId(((BigDecimal) r[1]).longValue());
           strategy.setName((String) r[2]);
           strategy.setComments((String) r[3]);
           strategy.setCurrentYearTotalCost(((BigDecimal) r[4]));
           strategy.setEstimatedTotalCost(((BigDecimal) r[5]));
           strategy.setPercentChange(((BigDecimal) r[6]));
           strategy.setSubmitDate((Date) r[7]);
           strategy.setSubmitted(true);
           strategy.setTotalBudget(((BigDecimal) r[9]));
           strategy.setType((String) r[10]);
           strategy.setRealmPlanYearId(((BigDecimal) r[11]).longValue());
        }        
        return strategy;
	}
	
	@Override
	public List<Strategy> getFutureStrategies(String companyCode, long id) {
        Query q = em.createNamedQuery("getFutureStrategies");
        q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
        q.setParameter( BSSQueryConstants.REALM_PLAN_YEAR_ID, id);
		List<Object[]> results = DaoUtils.getResultList(q, "getFutureStrategies");
        List<Strategy> list = new ArrayList<>();
        
        for (Object[] r : results) {
           Strategy strategy = new Strategy();
           strategy.setId(((BigDecimal) r[0]).longValue());
           strategy.setCompanyId(((BigDecimal) r[1]).longValue());
           strategy.setName((String) r[2]);
           strategy.setComments((String) r[3]);
           strategy.setCurrentYearTotalCost(((BigDecimal) r[4]));
           strategy.setEstimatedTotalCost(((BigDecimal) r[5]));
           strategy.setPercentChange(((BigDecimal) r[6]));
           strategy.setSubmitDate((Date) r[7]);
           strategy.setSubmitted(true);
           strategy.setTotalBudget(((BigDecimal) r[9]));
           strategy.setType((String) r[10]);
           strategy.setRealmPlanYearId(((BigDecimal) r[11]).longValue());
           list.add(strategy);
        }        
        return list;
	}
	
	@Override
	public Set<String> getRealmPlanTypes(long realmYearId) {
		 Query q = em.createNamedQuery("getRealmPlanTypes");
	     q.setParameter( BSSQueryConstants.REALM_YEAR_ID, realmYearId);
	     Set<String> planTypeSet = new TreeSet<>();
	     List<String> results = DaoUtils.getResultStringList(q, "getRealmPlanTypes");
	     
	     for (String r : results) {
	    	 planTypeSet.add(r);
	     }
	     return planTypeSet;
	     
	}
	
	@Override
	public Map<String, Map<String, Map<String, String>>> getOverridesByBenefitGroup(String companyCode,
			Long realmPlanYearId, boolean history) {
		Query q = em.createNamedQuery("PLAN_OVERRIDES_BY_COMPANY_ID");
		q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		List<Object[]> results = DaoUtils.getResultList(q, "PLAN_OVERRIDES_BY_COMPANY_ID");
		Map<String, Map<String, Map<String, String>>> groupOverrideMap = new HashMap<>();
		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String benefitPlan = (String) r[1];
			String coverageLevel = (String) r[2];
			String overrideValue = (String) r[3];
			if (!history && BSSApplicationConstants.PLAN_OVERRIDE_FLTEE.equals(overrideValue)) {
				overrideValue = BSSApplicationConstants.PLAN_OVERRIDE_FLT;
			}
			boolean isNewPlan = false;
			if (BigDecimal.ONE.equals((BigDecimal) r[4])) {
				isNewPlan = true;
			}		
			boolean isMultiMapped = false;
			if (BigDecimal.ONE.equals((BigDecimal) r[5])) {
				isMultiMapped = true;
			}		
			String newPlanId = (String) r[6];
			
			if (!history && isNewPlan && !isMultiMapped) {
				benefitPlan = newPlanId;
			}
			
			updateGroupOverrideMap(groupOverrideMap, benefitProgram, benefitPlan, coverageLevel, overrideValue);
		}
		return groupOverrideMap;
	}

	private void updateGroupOverrideMap(Map<String, Map<String, Map<String, String>>> groupOverrideMap,
			String benefitProgram, String benefitPlan, String coverageLevel, String overrideValue) {
		
		Map<String, Map<String, String>> planOverrideMap = groupOverrideMap.computeIfAbsent(benefitProgram,
				k -> new HashMap<>());

		Map<String, String> coverageLevelOverride = planOverrideMap.computeIfAbsent(benefitPlan, k -> new HashMap<>());

		coverageLevelOverride.put(coverageLevel, overrideValue);
	}
	
	@Override
	public Map<Long, Map<String, Map<Long, List<PlanSelection>>>> getPlansSelectionsByCompany(String companyCode,
			Long realmPlanYearId, String effDate) {
		Query q = em.createNamedQuery("PLAN_SELECTION_BY_COMPANY_REALM_YEAR");
		q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		List<Object[]> results = DaoUtils.getResultList(q, "PLAN_SELECTION_BY_COMPANY_REALM_YEAR");
		Map<Long, Map<String, Map<Long, List<PlanSelection>>>> planSelectionMap = new HashMap<>();
		for (Object[] r : results) {
			Long strategyId = ((BigDecimal) r[0]).longValue();
			Long groupId = ((BigDecimal) r[1]).longValue();
			String planType = (String) r[2];
			String benefitPlan = (String) r[4];
			Long portfolioId = ((BigDecimal) r[5]).longValue();
			Long headCount = 0L;
			if (null != r[9]) {
				headCount = ((BigDecimal) r[9]).longValue();
			}
			boolean isPPO = false;
			if (BigDecimal.ONE.equals((BigDecimal) r[10])) {
				isPPO = true;
			}
			boolean isHighDeductible = false;
			if (BigDecimal.ONE.equals((BigDecimal) r[12])) {
				isHighDeductible = true;
			}
			PlanSelection planSelection = new PlanSelection();
			planSelection.setId(((BigDecimal) r[3]).longValue());
			planSelection.setStrategyId(strategyId);
			planSelection.setGroupId(groupId);
			planSelection.setPlanType(planType);
			planSelection.setBenefitPlan(benefitPlan);
			planSelection.setPlanCarrierId(portfolioId);
			planSelection.setVendor((String) r[7]);
			planSelection.setName((String) r[8]);
			planSelection.setHeadCount(headCount);
			planSelection.setPpoPlan(isPPO);
			planSelection.setPlanCategory((String) r[11]);
			planSelection.setHighDeductiblePlan(isHighDeductible);
			planSelection.setCrossPlan("");
			
			String planTypeDesc = getPlanTypeDescription(planType);
			if (null != planSelectionMap.get(strategyId)) {
				if (null != planSelectionMap.get(strategyId).get(planTypeDesc)) {
					Map<Long, List<PlanSelection>> groupPlanSelection = planSelectionMap.get(strategyId)
							.get(planTypeDesc);
					if (null != groupPlanSelection.get(groupId)) {
						List<PlanSelection> planSelectionList = groupPlanSelection.get(groupId);
						planSelectionList.add(planSelection);
					} else {
						List<PlanSelection> planSelectionList = new ArrayList<>();
						planSelectionList.add(planSelection);
						groupPlanSelection.put(groupId, planSelectionList);
					}
				} else {
					Map<Long, List<PlanSelection>> groupPlanSelection = new HashMap<>();
					List<PlanSelection> planSelectionList = new ArrayList<>();
					planSelectionList.add(planSelection);
					groupPlanSelection.put(groupId, planSelectionList);
					planSelectionMap.get(strategyId).put(planTypeDesc, groupPlanSelection);
				}
			} else {

				Map<String, Map<Long, List<PlanSelection>>> offerTypeMap = new HashMap<>();
				Map<Long, List<PlanSelection>> groupPlanSelection = new HashMap<>();
				List<PlanSelection> planSelectionList = new ArrayList<>();
				planSelectionList.add(planSelection);
				groupPlanSelection.put(groupId, planSelectionList);
				offerTypeMap.put(planTypeDesc, groupPlanSelection);
				planSelectionMap.put(strategyId, offerTypeMap);
			}
		}
		return planSelectionMap;
	}

	private String getPlanTypeDescription(String planType) {
		String planTypeDesc = null;
		if (Constants.medicalPlanTypeList.contains(planType)) {
			planTypeDesc = Constants.MEDICAL;
		} else if (Constants.dentalPlanTypeList.contains(planType)) {
			planTypeDesc = Constants.DENTAL;
		} else if (Constants.visionPlanTypeList.contains(planType)) {
			planTypeDesc = Constants.VISION;
		} else if (Constants.additionalPlanTypeList.contains(planType)) {
			planTypeDesc = Constants.ADDITIONAL;
		}
		return planTypeDesc;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyEstimateList(Set<Long> strategyIds) {
		Query query = em.createNamedQuery( DELETE_STRATEGY_ESTIMATE );
		query.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		DaoUtils.executeUpdate(query, DELETE_STRATEGY_ESTIMATE );
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void deleteStrategyEstimateByStrategyGroup(long strategyId, long groupId) {
		Query query = em.createNamedQuery("deleteStrategyEstimateByStrategyGroup");
		query.setParameter("strategyId", strategyId);
		query.setParameter("groupId", groupId);
		DaoUtils.executeUpdate(query, "deleteStrategyEstimateByStrategyGroup" );
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class})
	public int deleteStrategyEstimateForPlanTypes(Set<Long> strategyIds, List<String> planTypes) {
		Query query = em.createNamedQuery("deleteStrategyEstimateForPlanTypes");
		query.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		query.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
		return DaoUtils.executeUpdate(query, "deleteStrategyEstimateForPlanTypes" );
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void insertStrategyEstimate(Map<Long, List<StrategyEstimate>> strategyEstimateMap) {
		Query query = em.createNamedQuery("insertStrategyEstimate");
		Iterator<List<StrategyEstimate>> it = strategyEstimateMap.values().iterator();
		while (it.hasNext()) {
			List<StrategyEstimate> strategyEstimateList = it.next();
			for (StrategyEstimate strategyEstimate : strategyEstimateList) {
				query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyEstimate.getStrategyId());
				query.setParameter(BSSQueryConstants.GROUP_ID, strategyEstimate.getGroupId());
				query.setParameter(BSSQueryConstants.PLAN_TYPE, strategyEstimate.getPlanType());
				query.setParameter("planSubType", strategyEstimate.getPlanSubType());
				query.setParameter("estimate", strategyEstimate.getEstimate());
				DaoUtils.executeUpdate(query, "insertStrategyEstimate");
			}
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int insertStrategyEstimateForOmsPlanTypes(Set<Long> strategyIds, List<String> planTypes) {
		Query q = em.createNamedQuery("INSERT_STRATEGY_ESTIMATE_FOR_OMS_PLAN_TYPES");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		q.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
		return DaoUtils.executeUpdate(q, "INSERT_STRATEGY_ESTIMATE_FOR_OMS_PLAN_TYPES");
	}

	@Override
	public Map<String, BigDecimal> getStrategyGroupEstimateByPlanType(long strategyId, long groupId) {
		Query q = em.createNamedQuery("STRATEGY_ESTIMATE_BY_STRATEGY_GROUP");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		q.setParameter(BSSQueryConstants.GROUP_ID, groupId);
		List<Object[]> results = DaoUtils.getResultList(q, "STRATEGY_ESTIMATE_BY_STRATEGY_GROUP");
		Map<String, BigDecimal> strategyEstimateMap = new HashMap<>();
		for (Object[] r : results) {
			String planType = (String) r[0];
			BigDecimal estimate = (BigDecimal) r[1];
			strategyEstimateMap.put(planType, estimate);
		}
		return strategyEstimateMap;
	}
	
	@Override
	public List<ModelCompareStrategy> getModelCompareStrategies(long companyId) {
		String sqlName = "GET_MODEL_COMPARE_STRATEGIES";
		Query q = em.createNamedQuery(sqlName);
		q.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(q);

		List<Object[]> results = DaoUtils.getResultList(q, sqlName);
		List<ModelCompareStrategy> list = new ArrayList<>();

		if (CollectionUtils.isEmpty(results)) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_MC_STRATEGIES_NOT_FOUND,
					BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyDataDaoImpl.class.getName(),
					BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, sqlName, queryMap));

		} else {
			for (Object[] r : results) {
				ModelCompareStrategy strategy = new ModelCompareStrategy();
				strategy.setId(((BigDecimal) r[0]).longValue());
				strategy.setName((String) r[1]);
				strategy.setSubmitted(((BigDecimal) r[2]).longValue() == 1);
				strategy.setHistory(((BigDecimal) r[3]).longValue() == 1);
				strategy.setActiveStrategy(((BigDecimal) r[4]).longValue() == 1);
				list.add(strategy);
			}
		}
		return list;
	}

	@Override
	public Map<Long, List<ModelComparePlanTypeCost>> getStrategiesCost(List<Long> strategyIdList) {
		Query q = em.createNamedQuery("MC_STRATEGIES_COST");
		q.setParameter("StrategyList", strategyIdList);

		HashMap<String, Object> queryMap = new HashMap<>();
		queryMap.put("StrategyList", strategyIdList);
		
		List<Object[]> results = DaoUtils.getResultList(q, "MC_STRATEGIES_COST");
		Map<Long, List<ModelComparePlanTypeCost>> strategyPlanTypeCosts = new HashMap<>();

		for (Object[] r : results) {
			Long strategyId = ((BigDecimal) r[0]).longValue();
			String planType = (String) r[1];
			String planSubType = (String) r[2];
			BigDecimal cost = (BigDecimal) r[3];
			
			planType = ("").equals(Utils.getGenericPlanTypeCode(planType)) ? planType : Utils.getGenericPlanTypeCode(planType);

			String contributionType = getContributionType(planType, planSubType);

			updateStrategyCosts(strategyPlanTypeCosts, strategyId, cost, contributionType);
		}

		// Add the strategy with empty ModelComparePlanTypeCost list, which has
		// no estimates available.
		for (Long strategyId : strategyIdList) {
			strategyPlanTypeCosts.computeIfAbsent(strategyId, k -> new ArrayList<>());
		}

		return strategyPlanTypeCosts;
	}

	private void updateStrategyCosts(Map<Long, List<ModelComparePlanTypeCost>> strategyPlanTypeCosts, Long strategyId,
			BigDecimal cost, String contributionType) {
		List<ModelComparePlanTypeCost> mcPlanTypeCostList;
		boolean foundPlanType;
		foundPlanType = false;
		mcPlanTypeCostList = new ArrayList<>();

		// check if strategy already exists
		if (null != strategyPlanTypeCosts.get(strategyId)) {
			mcPlanTypeCostList = strategyPlanTypeCosts.get(strategyId);

			// If the planType is already in the list, add this cost to
			// the current cost
			for (ModelComparePlanTypeCost mcPlanTypeCost : mcPlanTypeCostList) {
				if (contributionType.equals(mcPlanTypeCost.getPlanType())) {
					BigDecimal totalCost = mcPlanTypeCost.getCost().add(cost);
					mcPlanTypeCost.setCost(totalCost);
					foundPlanType = true;
				}
			}

		}

		// If the strategy or planType is not in the list, create a new
		// ModelComparePlanTypeCost
		if (null == strategyPlanTypeCosts.get(strategyId) || !foundPlanType) {
			ModelComparePlanTypeCost newMcPlanTypeCost = new ModelComparePlanTypeCost();
			newMcPlanTypeCost.setPlanType(contributionType);
			newMcPlanTypeCost.setCost(cost);
			newMcPlanTypeCost.setOffered(true);
			mcPlanTypeCostList.add(newMcPlanTypeCost);
			strategyPlanTypeCosts.put(strategyId, mcPlanTypeCostList);
		}
	}

	private String getContributionType(String planType, String planSubType) {
		String contributionType = null;
		if (planSubType == null) {
			contributionType = PlanTypesEnum.getName(planType);
		}
		else if (BSSApplicationConstants.WAIVER_ALLOWANCE_PLAN_SUB_TYPE.equals(planSubType)) {
			contributionType = BSSApplicationConstants.WAIVER_ALLOWANCE;
		}
		else {
			contributionType = planSubType;
		}
		return contributionType;
	}
	
	@Override
	public List<Object[]> getBenefitCostSummary(String trinetStrategyId) {
		Query q = em.createNamedQuery("BENEFIT_COST_SUMMARY");
		q.setParameter("trinetStrategyId", trinetStrategyId);		
		return DaoUtils.getResultList(q, "BENEFIT_COST_SUMMARY");
	}
	
 
	@Override
	public List<Object[]> getStrategyHsaFunding(List<Long> strategyIdList) {
		Query q = em.createNamedQuery("STRATEGY_HSA_FUNDING");
		q.setParameter("strategyList", strategyIdList);
		return DaoUtils.getResultList(q, "STRATEGY_HSA_FUNDING");
	}

   @Override
	public List<Object[]> getStrategyFundingByStrategy(List<Long> strategyIdList) {
		Query q = em.createNamedQuery("STRATEGY_FUNDING_BY_STRATEGY");
		q.setParameter("strategyIds", strategyIdList);
		return DaoUtils.getResultList(q, "STRATEGY_FUNDING_BY_STRATEGY");
	}
	
	@Override
	public Map<Long, List<BenefitGroup>> getGroupsByStrategy(List<Long> strategyIdList) {
		String sqlName = "MC_STRATEGY_GROUPS";
		Query q = em.createNamedQuery(sqlName);
		q.setParameter("StrategyList", strategyIdList);
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(q);
		
		List<Object[]> results = DaoUtils.getResultList(q, sqlName);

		Map<Long, List<BenefitGroup>> strategyGroups = new HashMap<>();

		Long strategyId = null;
		Long groupId = null;
		String groupDescr = null;

		if (CollectionUtils.isEmpty(results)) {
			throw new BSSApplicationException(
					new BSSApplicationError(BSSErrorResponseCodes.BSS_MC_STRATEGY_GROUP_NOT_FOUND,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, StrategyDataDaoImpl.class.getName(),
							BSSErrorResponseMessages.MSG_MC_GENERAL_ERROR, sqlName, queryMap));

		} else {

			for (Object[] r : results) {
				strategyId = ((BigDecimal) r[0]).longValue();
				groupId = ((BigDecimal) r[1]).longValue();
				groupDescr = (String) r[2];

				BenefitGroup bg = new BenefitGroup();
				bg.setId(groupId);
				bg.setName(groupDescr);

				// check if strategy already exists
				if (strategyGroups.containsKey(strategyId)) {
					strategyGroups.get(strategyId).add(bg);
				} else {
					List<BenefitGroup> groupList = new ArrayList<>();
					groupList.add(bg);
					strategyGroups.put(strategyId, groupList);
				}
			}
		}

		return strategyGroups;
	}

	@Override
	public Map<String, List<String>> getOfferedPlanTypesByStrategy(String strategyId) {
		List<String> strategyList = new ArrayList<>();
		strategyList.add(strategyId);
		Query q = em.createNamedQuery(STRATEGY_GROUP_PLANTYPES);
		q.setParameter(BSSQueryConstants.STRATEGY_LIST, strategyList);
		q.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		
		List<Object[]> results = DaoUtils.getResultList(q, STRATEGY_GROUP_PLANTYPES);
		Map<String, List<String>> benefitProgramPlanType = new HashMap<>();
		for (Object[] r : results) {
			String benefitProgram = (String) r[1];
			String planType = (String) r[2];
			if (null != benefitProgramPlanType.get(benefitProgram)) {
				List<String> planTypes = benefitProgramPlanType.get(benefitProgram);
				planTypes.add(planType);
			} else {
				List<String> planTypes = new ArrayList<>();
				planTypes.add(planType);
				benefitProgramPlanType.put(benefitProgram, planTypes);
			}
		}
		  for (Entry<String, List<String>> entry : benefitProgramPlanType.entrySet()) {
		    List<String> planType = entry.getValue();		
			if (null != planType && !planType.isEmpty()) {
				if (!planType.contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)
						&& !planType.contains(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE)) {
					planType.add(BSSApplicationConstants.DENTAL_PLAN_TYPE);
				}

				if (!planType.contains(BSSApplicationConstants.VISION_PLAN_TYPE)
						&& !planType.contains(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE)) {
					planType.add(BSSApplicationConstants.VISION_PLAN_TYPE);
				}
			} else {
				if (planType == null) {
					planType = new ArrayList<>();
				}
				planType.add(BSSApplicationConstants.DENTAL_PLAN_TYPE);
				planType.add(BSSApplicationConstants.VISION_PLAN_TYPE);
			}
		}
		return benefitProgramPlanType;
	}
	
	@Override
	public List<Object[]> getPlanContributionsByStrategyId(Company company, long strategyId, boolean fetchAllRecords) {
		Query query = em.createNamedQuery("CONTRIBUTIONS_BY_STRATEGY_ID");
		query.setParameter(STRATEGY_ID, strategyId);
		query.setParameter("REALM_PLAN_YEAR", company.getRealmPlanYearId());
		if(!fetchAllRecords) {
			query.setHint(QueryHints.FETCH_SIZE, BSSQueryConstants.HIBERNATE_FETCH_SIZE_1000);
		}
		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();
		return results;
	}
	
	@Override
	public Map<String, Map<String, String>> getStrategyDefaultPlans(long realmPlanYearId, long strategyId) {
		Query q = em.createNamedQuery("STRATEGY_DEFAULT_PLANS");
		q.setParameter("REALM_PLAN_YEAR", realmPlanYearId);
		q.setParameter(STRATEGY_ID, strategyId);
		Map<String, Map<String, String>> planTypeportfolioMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "STRATEGY_DEFAULT_PLANS");
		for (Object[] r : results) {
			Map<String, String> portfolioMap = planTypeportfolioMap.get((String) r[2]);
			if (null == portfolioMap) {
				portfolioMap = new HashMap<>();
			}
			portfolioMap.put(r[0].toString(), (String) r[1]);
			planTypeportfolioMap.put((String) r[2], portfolioMap);
		}
		return planTypeportfolioMap;
	}
	
	@Override
	public Map<Long, Long> getStrategyBenefitGroupHeadCountsFromCensus(Long strategyId) {
		Query q = em.createNamedQuery("STRATEGY_GROUP_CENSUS_HC");
		q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);

		Map<Long, Long> strategyGroupHeadCountMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "STRATEGY_GROUP_CENSUS_HC");
		for (Object[] r : results) {
			long srategyGroupId = ((BigDecimal) r[0]).longValue();
			long headCount = ((BigDecimal) r[1]).longValue();
			strategyGroupHeadCountMap.put(srategyGroupId, headCount);
		}
		return strategyGroupHeadCountMap;
	}
	
	@Override
	public int getSubmittedStrategiesCount(long companyId) {
		Query q = em.createNamedQuery("SUBMITTED_STRATEGIES_COUNT_FOR_COMPANY");
		q.setParameter("COMPANY_ID", companyId);
		return ((BigDecimal) DaoUtils.getSingleResult(q, "SUBMITTED_STRATEGIES_COUNT_FOR_COMPANY")).intValue();
	}
	
	@Override
	public MultiKeyMap getEmplStrategyBenGroup(Long companyId) {
		Query query = em.createNamedQuery("EMPL_STRATEGY_BEN_GROUP");
		query.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		List<Object[]> results = DaoUtils.getResultList(query, "EMPL_STRATEGY_BEN_GROUP");
		MultiKeyMap emplStrategyBenGroupMap = new MultiKeyMap();

		for (Object[] r : results) {
			// EMPLID, STRATEGY_ID, FUTURE_GROUP_ID, FUTURE_GROUP_DESCR, FUTURE_BENEFIT_PROGRAM
			String emplId = r[0].toString();
			Long strategyId = ((BigDecimal) r[1]).longValue();
			BenefitGroup benefitGroup = new BenefitGroup();
			benefitGroup.setId(((BigDecimal) r[2]).longValue());
			benefitGroup.setName(r[3].toString());
			benefitGroup.setBenefitProgram((String) r[4]);

			emplStrategyBenGroupMap.put(emplId, strategyId, benefitGroup);
		}
		return emplStrategyBenGroupMap;
	}

	@Override
	public Optional<List<StrategyGroupEmployeePlanRateData>> getOmsStrategyGroupPlanCostByPlanType(List<Long> strategyIds, List<String> planTypes) {
		Query query = em.createNamedQuery(OMS_STRATEGY_GROUP_PLANTYPE_PLAN_COST);
		query.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
		query.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		List<Object[]> results = DaoUtils.getResultList(query, OMS_STRATEGY_GROUP_PLANTYPE_PLAN_COST);
		return getStrategyGroupPlanCostData(results);

	}
	
	@Override
	public Optional<List<StrategyGroupEmployeePlanRateData>> getStrategyGroupPlanCostByPlanType(Company company,
			List<Long> strategyIds, List<String> planTypes) {
		Query query = em.createNamedQuery(STRATEGY_GROUP_PLANTYPE_PLAN_COST);
		query.setParameter(BSSQueryConstants.EFF_DATE, company.getRealmPlanYear().getPlanYearEnd());
		query.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
		query.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		List<Object[]> results = DaoUtils.getResultList(query, STRATEGY_GROUP_PLANTYPE_PLAN_COST);
		return getStrategyGroupPlanCostData(results);
			
	}
	
	private Optional<List<StrategyGroupEmployeePlanRateData>> getStrategyGroupPlanCostData(List<Object[]> results) {
		if (CollectionUtils.isEmpty(results)) {
			return Optional.empty();
		} else {
			List<StrategyGroupEmployeePlanRateData> planRateDataList = new ArrayList<>();
			results.forEach(result -> {
				planRateDataList.add(buildStrategyGroupEmployeePlanRateData(result));
			});
			return Optional.of(planRateDataList);
		}
	}

	private StrategyGroupEmployeePlanRateData buildStrategyGroupEmployeePlanRateData(Object[] result) {
		return StrategyGroupEmployeePlanRateData.builder()
				.emplId(result[0].toString())
				.strategyId(((BigDecimal) result[1]).longValue())
				.groupId(((BigDecimal) result[2]).longValue())
				.groupName(result[3].toString())
				.planType(result[4] != null ? result[4].toString() : null)
				.benefitPlan(result[5] != null ? result[5].toString() : null)
				.planName(result[6] != null ? result[6].toString() : null)
				.coverageCode(result[7] != null ? result[7].toString() : null)
				.eeRate((result[8] != null) ? (BigDecimal) result[8] : BigDecimal.ZERO)
				.erRate((result[9] != null) ? (BigDecimal) result[9] : BigDecimal.ZERO)
				.carrier((result[10] != null) ? result[10].toString() : null)
				.build();
	}

	@Override
	public MultiKeyMap getGroupStrategyPlanCost(Company company, List<Long> strategyList) {
		Query query = em.createNamedQuery("STRATEGY_GROUP_PLAN_COST");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		query.setParameter(BSSQueryConstants.STRATEGY_LIST, strategyList);

		HashMap<String, Object> queryMap = new HashMap<>();
		queryMap.put(BSSQueryConstants.COMPANY_CODE, company.getCode());
		queryMap.put("StrategyList", strategyList);

		List<Object[]> results = DaoUtils.getResultList(query, "STRATEGY_GROUP_PLAN_COST");

		Map<String, EmployeeBenefitGroup> benefitGroupMap = employeeBenefitGroupDao.getBenefitProgramDetails(company);
		
		MultiKeyMap strategyGroupPlanCostMap = null;

		if (CollectionUtils.isEmpty(results)) {
			strategyGroupPlanCostMap = new MultiKeyMap();
		} else {
			// multi-key map of strategy, group, coverage level, cost
			strategyGroupPlanCostMap = new MultiKeyMap();

			for (Object[] r : results) {

				String benefitProgram = r[1].toString();
				Long groupId = benefitGroupMap.get(benefitProgram).getBenefitGroupId();
				Long strategyId = ((BigDecimal) r[2]).longValue();
				String planType = r[3].toString();
				String benefitPlan = r[4].toString();
				String coverageLevel = r[6].toString();

				StrategyGroupPlanRateData strategyGroupPlanRateData = new StrategyGroupPlanRateData();
				strategyGroupPlanRateData.setStrategyId(strategyId);
				strategyGroupPlanRateData.setGroupId(groupId);
				strategyGroupPlanRateData.setPlanType(planType);
				strategyGroupPlanRateData.setBenefitPlan(benefitPlan);
				strategyGroupPlanRateData.setCoverageLevel(coverageLevel);
				strategyGroupPlanRateData.setErContribPercent(((BigDecimal) r[7]));
				strategyGroupPlanRateData.setErRate(((BigDecimal) r[8]));
				strategyGroupPlanRateData.setEeRate(((BigDecimal) r[9]));
				strategyGroupPlanRateData.setDescription(r[5].toString());

				strategyGroupPlanCostMap.put(strategyId, groupId, planType, benefitPlan, coverageLevel,
						strategyGroupPlanRateData);
			}
		}

		return strategyGroupPlanCostMap;
	}
	
	@Override
	public MultiKeyMap getStrategyProgramPlantypeOfferings(List<Long> strategyList, List<String> planTypes) {

		Query query = em.createNamedQuery(STRATEGY_GROUP_PLANTYPES);
		query.setParameter(BSSQueryConstants.STRATEGY_LIST, strategyList);
		query.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, planTypes);
		String genericPlanType = "";
		List<Object[]> results = DaoUtils.getResultList(query, STRATEGY_GROUP_PLANTYPES);
		// multi-key map of strategy, group, planType
		MultiKeyMap strategyGroupPlantypeMap = new MultiKeyMap();
		for (Object[] r : results) {
			Long strategyId = ((BigDecimal) r[0]).longValue();
			String benefitProgram = r[1].toString();
			String planType = r[2].toString();
			genericPlanType = Utils.getGenericPlanTypeCode(planType);
			if (!(strategyGroupPlantypeMap.containsKey(strategyId, benefitProgram, genericPlanType))) {
				strategyGroupPlantypeMap.put(strategyId, benefitProgram, genericPlanType, planType);
			}
		}
		return strategyGroupPlantypeMap;
	}
	
	@Override
	public List<StrategyBenefitPlanHeadCount> getHeadcountByPlanStrategyCoverage(List<Long> strategyIdList,
			String effDate) {
		Query q = em.createNamedQuery("STRATEGY_PLAN_COVERAGE_HEADCOUNT");
		q.setParameter(BSSQueryConstants.STRATEGY_LIST, strategyIdList);
		q.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		List<Object[]> results = DaoUtils.getResultList(q, "STRATEGY_PLAN_COVERAGE_HEADCOUNT");
		Map<String, StrategyBenefitPlanHeadCount> localPlanMap = new LinkedHashMap<>();
		for (Object[] r : results) {
			boolean foundStrategy = false;
			String planType = (String) r[0];
			String benefitPlan = (String) r[1];
			String benefitPlanDescr = (String) r[2];
			long strategyId = ((BigDecimal) r[3]).longValue();
			String coverageLevelDescr = (String) r[4];
			Long headcount = ((BigDecimal) r[5]).longValue();
			List<StrategyCoverageLevelHeadcount> strategyCoverageHeadcountList;
			if (localPlanMap.containsKey(benefitPlan)) {
				strategyCoverageHeadcountList = localPlanMap.get(benefitPlan).getStrategyCoverageLevelHeadcount();
				for (StrategyCoverageLevelHeadcount strategyCoverageHeadcount : strategyCoverageHeadcountList) {
					if (strategyCoverageHeadcount.getStrategyId() == strategyId) {
						foundStrategy = true;
						strategyCoverageHeadcount.getCoverageHeadcount().put(coverageLevelDescr, headcount);
						break;
					}
				}
				if (!foundStrategy) {
					StrategyCoverageLevelHeadcount strategyCoverageHeadcount = new StrategyCoverageLevelHeadcount();
					strategyCoverageHeadcount.setStrategyId(strategyId);
					strategyCoverageHeadcount.setOffered(headcount != null);
					Map<String, Long> headcountMap = new LinkedHashMap<>();
					headcountMap.put(coverageLevelDescr, headcount);
					strategyCoverageHeadcount.setCoverageHeadcount(headcountMap);
					strategyCoverageHeadcountList.add(strategyCoverageHeadcount);
				}
			} else {
				String basePlanType = null;
				if (BSSApplicationConstants.MEDICAL_PLAN_TYPES.contains(planType)) {
					basePlanType = BSSApplicationConstants.MEDICAL;
				} else if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
					basePlanType = BSSApplicationConstants.DENTAL;
				} else if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planType)) {
					basePlanType = BSSApplicationConstants.VISION;
				}
				StrategyBenefitPlanHeadCount modelComparePlanHeadcount = new StrategyBenefitPlanHeadCount();
				modelComparePlanHeadcount.setPlanType(planType);
				modelComparePlanHeadcount.setBasePlanType(basePlanType);
				modelComparePlanHeadcount.setBenefitPlan(benefitPlan);
				modelComparePlanHeadcount.setBenefitPlanDescr(benefitPlanDescr);
				strategyCoverageHeadcountList = new LinkedList<>();
				StrategyCoverageLevelHeadcount strategyCoverageHeadcount = new StrategyCoverageLevelHeadcount();
				strategyCoverageHeadcount.setStrategyId(strategyId);
				strategyCoverageHeadcount.setOffered(headcount != null);
				Map<String, Long> headcountMap = new LinkedHashMap<>();
				headcountMap.put(coverageLevelDescr, headcount);
				strategyCoverageHeadcount.setCoverageHeadcount(headcountMap);
				strategyCoverageHeadcountList.add(strategyCoverageHeadcount);
				modelComparePlanHeadcount.setStrategyCoverageLevelHeadcount(strategyCoverageHeadcountList);
				localPlanMap.put(benefitPlan, modelComparePlanHeadcount);
			}
		}
		return new LinkedList<>(localPlanMap.values());
	}
	
	@Override
	public List<ModelCompareGroupHeadcount> getStrategyGroupHeadcountCost(List<Long> strategyList) {
		Query query = em.createNamedQuery("STRATEGY_GROUP_HEADCOUNT_COST");
		query.setParameter(BSSQueryConstants.STRATEGY_LIST, strategyList);
		query.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		query.setParameter("additionalPlanTypesIncludeCmtr", BSSApplicationConstants.ADDITIONAL_PLAN_TYPES_INCLUD_CMTR);
		List<Object[]> results = DaoUtils.getResultList(query, "STRATEGY_GROUP_HEADCOUNT_COST");
		Map<String, ModelCompareGroupHeadcount> localHeadcountMap = new LinkedHashMap<>();
		for (Object[] r : results) {
			String basePlanType = r[0].toString();
			Long strategyId = ((BigDecimal) r[2]).longValue();
			String benefitProgram = r[3].toString();
			String groupDescr = r[4].toString();
			Long headcount = ((BigDecimal) r[5]).longValue();
			BigDecimal cost = (BigDecimal) r[6];

			ModelComparePlanTypeCost planTypeCost = new ModelComparePlanTypeCost();
			planTypeCost.setOffered(true);
			planTypeCost.setPlanType(basePlanType);
			planTypeCost.setHeadcount(headcount);
			planTypeCost.setCost(cost);
			if (localHeadcountMap.containsKey(benefitProgram)) {
				ModelCompareGroupHeadcount mcGroupHeadcount = localHeadcountMap.get(benefitProgram);
				if (mcGroupHeadcount.getStrategyHeadcountMap().containsKey(strategyId)) {
					mcGroupHeadcount.getStrategyHeadcountMap().get(strategyId).add(planTypeCost);
				} else {
					LinkedList<ModelComparePlanTypeCost> mcPlanTypeCostList = new LinkedList<>();
					mcPlanTypeCostList.add(planTypeCost);
					mcGroupHeadcount.getStrategyHeadcountMap().put(strategyId, mcPlanTypeCostList);
				}
			} else {
				ModelCompareGroupHeadcount mcGroupHeadcount = new ModelCompareGroupHeadcount();
				mcGroupHeadcount.setBenefitProgram(benefitProgram);
				mcGroupHeadcount.setGroupDescr(groupDescr);
				Map<Long, LinkedList<ModelComparePlanTypeCost>> mcStrategyPlanTypeCost = new LinkedHashMap<>();
				LinkedList<ModelComparePlanTypeCost> mcPlanTypeCostList = new LinkedList<>();
				mcPlanTypeCostList.add(planTypeCost);
				mcStrategyPlanTypeCost.put(strategyId, mcPlanTypeCostList);
				mcGroupHeadcount.setStrategyHeadcountMap(mcStrategyPlanTypeCost);
				localHeadcountMap.put(benefitProgram, mcGroupHeadcount);
			}
		}
		return new LinkedList<>(localHeadcountMap.values());
	}
	
	@Override
	public Map<Long, List<AdditionalBenefitPlan>> getAdditionalBenefitPlansForStrategy(Long strategyId, String effDate) {

		Query query = em.createNamedQuery("ADDITIONAL_PLAN_OFFERINGS_FOR_STRATEGY");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		query.setParameter(BSSQueryConstants.ADDITIONAL_PLAN_TYPES,
				BSSApplicationConstants.ADDITIONAL_PLAN_TYPES_INCLUD_CMTR);
		query.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		

		List<Object[]> results = DaoUtils.getResultList(query, "ADDITIONAL_PLAN_OFFERINGS_FOR_STRATEGY");
		Map<Long, List<AdditionalBenefitPlan>> strategyGroupAdditonalOfferingMap = new HashMap<>();

		for (Object[] r : results) {
			Long groupId = ((BigDecimal) r[0]).longValue();
			String planType = r[1].toString();
			String planId = r[2].toString();
			String planName = r[3].toString();

			AdditionalBenefitPlan additionalBenefitPlan = new AdditionalBenefitPlan();
			additionalBenefitPlan.setPlanType(planType);
			additionalBenefitPlan.setId(planId);
			additionalBenefitPlan.setDescription(planName);

			if (strategyGroupAdditonalOfferingMap.containsKey(groupId)) {
				strategyGroupAdditonalOfferingMap.get(groupId).add(additionalBenefitPlan);
			} else {
				strategyGroupAdditonalOfferingMap.put(groupId,
						new ArrayList<>(Arrays.asList(additionalBenefitPlan)));
			}
		}

		return strategyGroupAdditonalOfferingMap;

	}

	@Override
	public Map<Long, List<DisabilityBenefitOptionPlans>> getAdditionalBenefitPlansForStrategyWithSdiInfo(Long strategyId, String effDate, long realmPlanYearId) {

		Query query = em.createNamedQuery("ADDITIONAL_PLAN_OFFERINGS_FOR_STRATEGY_WITH_SDI_INFO");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		query.setParameter(BSSQueryConstants.ADDITIONAL_PLAN_TYPES,
				BSSApplicationConstants.ADDITIONAL_PLAN_TYPES_INCLUD_CMTR);
		query.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYearId);


		List<Object[]> results = DaoUtils.getResultList(query, "ADDITIONAL_PLAN_OFFERINGS_FOR_STRATEGY_WITH_SDI_INFO");
		Map<Long, List<DisabilityBenefitOptionPlans>> strategyAdditionalPlanOfferingsByGroup = new HashMap<>();

		for (Object[] r : results) {
			Long groupId = ((BigDecimal) r[0]).longValue();
			String planType = r[1].toString();
			String planId = r[2].toString();
			String planName = r[3].toString();
			String carrierName = r[4].toString();
			boolean sdiFlag = r[5].toString().equals("1");
			boolean employeePaid = r[6].toString().equals("1");

			DisabilityBenefitOptionPlans disabilityBenefitOptionPlans = new DisabilityBenefitOptionPlans();
			disabilityBenefitOptionPlans.setPlanType(planType);
			disabilityBenefitOptionPlans.setId(planId);
			disabilityBenefitOptionPlans.setPlanDesc(planName);
			disabilityBenefitOptionPlans.setCarrierName(carrierName);
			disabilityBenefitOptionPlans.setSdiPlan(sdiFlag);
			disabilityBenefitOptionPlans.setEmployeePaid(employeePaid);

			if (strategyAdditionalPlanOfferingsByGroup.containsKey(groupId)) {
				strategyAdditionalPlanOfferingsByGroup.get(groupId).add(disabilityBenefitOptionPlans);
			} else {
				strategyAdditionalPlanOfferingsByGroup.put(groupId,
						new ArrayList<>(Arrays.asList(disabilityBenefitOptionPlans)));
			}
		}

		return strategyAdditionalPlanOfferingsByGroup;
	}
	
	@Override
	public List<StrategySubmitIssueReport> getSubmittedStrategyIssueReportData() {
		List<StrategySubmitIssueReport> returnList = new ArrayList<>();
		Query query = em.createNamedQuery("STRATEGY_SUBMITTED_ISSUE_REPORT");
		List<Object[]> results = DaoUtils.getResultList(query, "STRATEGY_SUBMITTED_ISSUE_REPORT");
		long previousStrategyId = 0;
		boolean foundSubmittedBdm = false;
		SimpleDateFormat formatter = new SimpleDateFormat(BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);

		final int STATEMENT_UPLOAD_STATUS_INDEX = 0;
		final int EMAIL_SENT_INDEX = 1;
		final int STRATEGY_ID_INDEX = 2;
		final int COMPANY_CODE_INDEX = 3;
		final int EXCHANGE_INDEX = 4;
		final int QE_QUARTER_INDEX = 5;
		final int COMPANY_LEGAL_NAME_INDEX = 6;
		final int COMPANY_DBA_NAME_INDEX = 7;
		final int EMPLOYEE_ID_INDEX = 8;
		final int EMPLOYEE_FIRST_NAME_INDEX = 9;
		final int EMPLOYEE_LAST_NAME_INDEX = 10;
		final int SUBMIT_DATE_INDEX = 11;
		final int SUBMITTER_INDEX = 12;

		StrategySubmitIssueReport strategySubmitIssueReport = new StrategySubmitIssueReport();

		for (Object[] resultRow : results) {
			String statementUploadStatus = resultRow[STATEMENT_UPLOAD_STATUS_INDEX] == null
					? BSSApplicationConstants.ERROR
					: resultRow[STATEMENT_UPLOAD_STATUS_INDEX].toString();
			boolean emailSent = ("Y").equals(resultRow[EMAIL_SENT_INDEX].toString());
			long strategyId = ((BigDecimal) resultRow[STRATEGY_ID_INDEX]).longValue();
			String companyCode = resultRow[COMPANY_CODE_INDEX].toString();
			String exchange = resultRow[EXCHANGE_INDEX].toString();
			String oeQuarter = resultRow[QE_QUARTER_INDEX].toString();
			String companyLegalName = resultRow[COMPANY_LEGAL_NAME_INDEX] == null ? ""
					: resultRow[COMPANY_LEGAL_NAME_INDEX].toString();
			String companyName = resultRow[COMPANY_DBA_NAME_INDEX].toString();
			String employeeId = resultRow[EMPLOYEE_ID_INDEX] == null ? "" : resultRow[EMPLOYEE_ID_INDEX].toString();
			String employeeFirstName = resultRow[EMPLOYEE_FIRST_NAME_INDEX] == null ? ""
					: resultRow[EMPLOYEE_FIRST_NAME_INDEX].toString();
			String employeeLastName = resultRow[EMPLOYEE_LAST_NAME_INDEX] == null ? ""
					: resultRow[EMPLOYEE_LAST_NAME_INDEX].toString();
			String submitDateStr = resultRow[SUBMIT_DATE_INDEX] == null ? ""
					: formatter.format((Date) resultRow[SUBMIT_DATE_INDEX]);
			String submitter = resultRow[SUBMITTER_INDEX] == null ? "" : resultRow[SUBMITTER_INDEX].toString();

			if (strategyId != previousStrategyId) {
				if (0 != previousStrategyId) {
					strategySubmitIssueReport.setSubmittedByBdm(foundSubmittedBdm);
					returnList.add(strategySubmitIssueReport);
					foundSubmittedBdm = false;
				}
				strategySubmitIssueReport = new StrategySubmitIssueReport();
				strategySubmitIssueReport.setStatementUploadStatus(statementUploadStatus);
				strategySubmitIssueReport.setEmailSent(emailSent);
				strategySubmitIssueReport.setCompanyCode(companyCode);
				strategySubmitIssueReport.setExchange(exchange);
				strategySubmitIssueReport.setOeQuarter(oeQuarter);
				strategySubmitIssueReport.setCompanyLegalName(companyLegalName);
				strategySubmitIssueReport.setCompanyName(companyName);
				strategySubmitIssueReport.setSubmitDateStr(submitDateStr);
			}

			Bdm bdm = strategySubmitIssueReport.new Bdm();
			bdm.setEmployeeId(employeeId);
			bdm.setEmployeeFirstName(employeeFirstName);
			bdm.setEmployeeLastName(employeeLastName);
			bdm.setSubmitter(submitter);
			foundSubmittedBdm = foundSubmittedBdm || (("Y").equals(submitter));
			if (null == strategySubmitIssueReport.getBdms()) {
				List<Bdm> bdms = new ArrayList<>();
				bdms.add(bdm);
				strategySubmitIssueReport.setBdms(bdms);
			} else {
				strategySubmitIssueReport.getBdms().add(bdm);
			}

			previousStrategyId = strategyId;
		}
		if (!results.isEmpty()) {
			strategySubmitIssueReport.setSubmittedByBdm(foundSubmittedBdm);
			returnList.add(strategySubmitIssueReport);
		}

		return returnList;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void inactivateUnSubmittedStrategiesByPlanYear(long realmPlanYear) {
		Query q = em.createNamedQuery("INACTIVATE_UNSUBMITTED_STRATEGIES_PLAN_YEAR");
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, realmPlanYear);
		int num = DaoUtils.executeUpdate(q, "INACTIVATE_UNSUBMITTED_STRATEGIES_PLAN_YEAR");
		logger.info("DELETED NUMBER OF STRATEGY FUNDING DETAILS : {}", num);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void inactivateUnSubmittedStrategiesByCompany(Company company) {
		Query q = em.createNamedQuery("INACTIVATE_UNSUBMITTED_STRATEGIES_COMPANY");
		q.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYearId());
		int num = DaoUtils.executeUpdate(q, "INACTIVATE_UNSUBMITTED_STRATEGIES_COMPANY");
		logger.info("DELETED NUMBER OF STRATEGY FUNDING DETAILS : {}", num);
	}
	
	@Override
	public Map<Long, Map<String, List<String>>> getStrategyBenPlans(List<Long> strategyIds) {
		Query query = em.createNamedQuery("FIND_STRATEGY_PLANS_ALL");
		query.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		List<Object[]> results = DaoUtils.getResultList(query, "FIND_STRATEGY_PLANS_ALL");

		Map<Long, Map<String, List<String>>> result = new HashMap<>();

		for (Object[] r : results) {
			// STRATEGY_ID, BENEFIT_PROGRAM, BENEFIT_PLAN
			Long strategyId = ((BigDecimal) r[0]).longValue();
			String benProgram = r[1].toString();
			String benefitPlan = ((String) r[2]);

			if (result.get(strategyId) == null) {
				HashMap<String, List<String>> benProgBenefitPlans = new HashMap<>();
				List<String> benefitPlans = new ArrayList<>();
				benefitPlans.add(benefitPlan);
				benProgBenefitPlans.put(benProgram, benefitPlans);
				result.put(strategyId, benProgBenefitPlans);
			} else if (result.get(strategyId).get(benProgram) == null) {
				List<String> benefitPlans = new ArrayList<>();
				benefitPlans.add(benefitPlan);
				result.get(strategyId).put(benProgram, benefitPlans);
			} else {
				result.get(strategyId).get(benProgram).add(benefitPlan);
			}
		}
		return result;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteEePlanAssignmentsByStrategyIds(Set<Long> strategyIds) {
		Query q = em.createNamedQuery("deleteEePlanAssignmentsByStrategyIds");
		q.setParameter(BSSQueryConstants.STRATEGY_IDS, strategyIds);
		int num = DaoUtils.executeUpdate(q, "deleteEePlanAssignmentsByStrategyIds");
		logger.info("DELETED {} NUMBER OF EE PLAN ASSIGNMENTS FOR STRATEGY IDS {}", num, strategyIds);
		return num;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int deleteEeDefaultPlanAssignmentsByCompanyId(long companyId) {
		Query q = em.createNamedQuery("deleteEeDefaultPlanAssignmentsByCompanyId");
		q.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		int num = DaoUtils.executeUpdate(q, "deleteEeDefaultPlanAssignmentsByCompanyId");
		logger.info("DELETED {} NUMBER OF EE DEFUALT ASSIGNMENTS FOR COMPANY ID {}", num, companyId);
		return num;
	}

	@Override
	public String getPrimaryCarrierName(Company company, String tnStrategyId) {
		Query query = em.createNamedQuery("GET_PRIMARY_CARRIER_NAME");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, tnStrategyId);
		query.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
		query.setParameter(BSSQueryConstants.PLAN_START_DATE, company.getRealmPlanYear().getPlanYearStart());
		return ((String) DaoUtils.getSingleResult(query, "GET_PRIMARY_CARRIER_NAME"));
	}

	@Override
	public List<Object[]> getHealthCostsByPlanType(Long strategyId) {
		Query query = em.createNamedQuery("HEALTH_COSTS_BY_PLAN_TYPE");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		return DaoUtils.getResultList(query, "HEALTH_COSTS_BY_PLAN_TYPE");
	}

	@Override
	public List<Object[]> getAdditionalBenefitCostsByPlanType(Long strategyId) {
		Query query = em.createNamedQuery("ADDITIONAL_BENEFITS_COSTS_BY_PLAN_TYPE");
		query.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		return DaoUtils.getResultList(query, "ADDITIONAL_BENEFITS_COSTS_BY_PLAN_TYPE");
	}

}