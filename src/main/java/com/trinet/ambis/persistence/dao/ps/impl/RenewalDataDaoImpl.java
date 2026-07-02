package com.trinet.ambis.persistence.dao.ps.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.sp.NextRateTblID;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanHeadCount;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;

public class RenewalDataDaoImpl implements RenewalDataDao {

	@PersistenceContext(unitName = "bis-sysadm")
	private EntityManager entityManager;
	
    @PersistenceContext(unitName = "bis-hrp")
    private EntityManager hrpEntityManager;	
	
	@Autowired
	NextRateTblID nextRateTblID;

	private static final Logger logger = LoggerFactory.getLogger(RenewalDataDaoImpl.class);
	private static final String ADDL_GROUP_HEADCOUNTS = "ADDL_GROUP_HEADCOUNTS";

	public void setEntityManager(EntityManager em) {
		this.entityManager = em;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	public void setHrpEntityManager(EntityManager em) {
		this.hrpEntityManager = em;
	}

	public EntityManager getHrpEntityManager() {
		return this.hrpEntityManager;
	}
	
	@Override
	public Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getHeadCountByGroupAndPlan(String company,
			Date effDate) {
		Query q = entityManager.createNamedQuery("MDV_GROUP_HEADCOUNTS");
		q.setParameter(BSSQueryConstants.COMPANY, company);
		q.setParameter("effdt", effDate);
		List<Object[]> results = DaoUtils.getResultList(q, "MDV_GROUP_HEADCOUNTS");
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> map = new HashMap<>();
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

				if (map.get(groupName) != null) {
					Map<String, List<PlanCoverageLevelHeadCount>> planHeadCountsMap = map
							.get(groupName);
					if (planHeadCountsMap.get(plan) != null) {
						List<PlanCoverageLevelHeadCount> list = planHeadCountsMap
								.get(plan);
						list.add(covrgHeadCount);
						planHeadCountsMap.put(plan, list);
					} else {
						List<PlanCoverageLevelHeadCount> list = new ArrayList<>();
						list.add(covrgHeadCount);
						planHeadCountsMap.put(plan, list);
					}
					map.put(groupName, planHeadCountsMap);
				} else {
					Map<String, List<PlanCoverageLevelHeadCount>> planHeadCountsMap = new HashMap<>();
					List<PlanCoverageLevelHeadCount> list = new ArrayList<>();
					list.add(covrgHeadCount);
					planHeadCountsMap.put(plan, list);
					map.put(groupName, planHeadCountsMap);
				}
			}
		}
		return map;
	}

	@Override
	public Map<String, List<BenefitPlanHeadCount>> getPlanHeadCountByGroups(Company company, Long strategyId,
			Date effDate, Map<String, PlanMapping> realmPlanMapping, Map<String, String> eeErPlanMapping,
			Map<String, List<String>> benefitProgramPlanTypes, boolean isVendorMappingOn) {

		// create map to get the updated benefit program for an employee/empl_rcd
		Map<String, Map<Long,String>> emplBenProgMap = new HashMap<>(); 

		Query eeQuery = hrpEntityManager.createNamedQuery("MDV_GROUP_HEADCOUNTS_UPD_BSSEE");
		eeQuery.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
		eeQuery.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYearId());
		eeQuery.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		List<Object[]> employeeResult = DaoUtils.getResultList(eeQuery, "MDV_GROUP_HEADCOUNTS_UPD_BSSEE");
		for( Object[] r : employeeResult ) {
			String emplid = (String) r[0];
			Long emplRcd = ( (BigDecimal) r[1] ).longValue();
			String benefitProgram = (String) r[2];

			Map<Long,String> innerMap = emplBenProgMap.get( emplid );
			if( innerMap == null ) {
				innerMap = new HashMap<>();
				emplBenProgMap.put( emplid, innerMap );
			}
			innerMap.put( emplRcd, benefitProgram );
		}


		/* superMap provides a path from benefitProgram -> benefitPlan -> coverageCd -> headcount */
		Map<String,Map<String,Map<String,CoverageLevelHeadCount>>> superMap = new HashMap<>();
		/* benPlanMap provides the plan type for each benefit plan */
		Map<String,String> benPlanMap = new HashMap<>();
		
		List<Object[]> results = this.getEnrolledHeadcountData(company, effDate, isVendorMappingOn);

		for (Object[] r : results) {
			String emplid = (String) r[0];
			Long emplRcd = ( (BigDecimal) r[1] ).longValue();
			String benefitProgram = (String) r[2];
			String planType = (String) r[3];
			String benefitPlan = (String) r[4];
			String covrgCd = (String) r[5];
			boolean hasHsa = ((BigDecimal) r[6] != BigDecimal.ZERO);

			// if employee was reassigned, get new benefit program value
			Map<Long,String> item = emplBenProgMap.get( emplid );
			if( item != null ) {
				benefitProgram = item.get( emplRcd );
			}
			
			if (null != realmPlanMapping.get(benefitPlan)) {
				benefitPlan = realmPlanMapping.get(benefitPlan).getNewBenefitPlan();
			}

			// transform company-paid to employee-paid or vice versa (for cases where plans are company-paid in one
			// group but employee-paid in another group)
			List<String> planTypes = benefitProgramPlanTypes.get(benefitProgram);
			if (null != planTypes && !BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planType) && !planTypes.contains(planType)) {
				benefitPlan = eeErPlanMapping.get(benefitPlan);
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(planType)) {
					if (BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(planType)) {
						planType = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
					} else {
						planType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
					}
				}
				if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(planType)) {
					if (BSSApplicationConstants.VISION_PLAN_TYPE.equals(planType)) {
						planType = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;
					} else {
						planType = BSSApplicationConstants.VISION_PLAN_TYPE;
					}
				}
			}
			
			// Save the benefit plan and plan type relationship (this will be needed when I build the final result object
			benPlanMap.put( benefitPlan, planType );

			// Update superMap with the count represented by this result row
			Map<String, Map<String, CoverageLevelHeadCount>> benPlanCountMap = superMap.get(benefitProgram);
			if (benPlanCountMap == null) {
				benPlanCountMap = new HashMap<>();
				superMap.put(benefitProgram, benPlanCountMap);
			}

			Map<String, CoverageLevelHeadCount> covrgCdMap = benPlanCountMap.get(benefitPlan);
			if (covrgCdMap == null) {
				covrgCdMap = new HashMap<>();
				benPlanCountMap.put(benefitPlan, covrgCdMap);
			}

			CoverageLevelHeadCount coverageLevelHeadCount = covrgCdMap.get(covrgCd);
			if (coverageLevelHeadCount == null) {
				coverageLevelHeadCount = new CoverageLevelHeadCount();
				coverageLevelHeadCount.setHeadCount(0);
				coverageLevelHeadCount.setHsaHeadCount(0);
			}
			coverageLevelHeadCount.setHeadCount(coverageLevelHeadCount.getHeadCount() + 1);

			if (hasHsa) {
				coverageLevelHeadCount.setHsaHeadCount(coverageLevelHeadCount.getHsaHeadCount() + 1);
			}

			covrgCdMap.put(covrgCd, coverageLevelHeadCount);
		}

		// move mapped data to output object
		Map<String, List<BenefitPlanHeadCount>> bmap = new HashMap<>();
		for (Map.Entry<String, Map<String, Map<String, CoverageLevelHeadCount>>> superEntry : superMap.entrySet()) {
			bmap.put(superEntry.getKey(), new ArrayList<>());
			for (Map.Entry<String, Map<String, CoverageLevelHeadCount>> planEntry : superEntry.getValue().entrySet()) {
				BenefitPlanHeadCount bphc = new BenefitPlanHeadCount();
				bphc.setBenefitPlan(planEntry.getKey());
				bphc.setPlanType(benPlanMap.get(planEntry.getKey()));
				bphc.setCoverageLevelHeadCount(new ArrayList<>());
				bmap.get(superEntry.getKey()).add(bphc);
				for (Map.Entry<String, CoverageLevelHeadCount> covrgEntry : planEntry.getValue().entrySet()) {
					CoverageLevelHeadCount clhc = new CoverageLevelHeadCount();
					clhc.setBenefitProgram(superEntry.getKey());
					clhc.setCoverageLevel(covrgEntry.getKey());
					clhc.setHeadCount(covrgEntry.getValue().getHeadCount());
					clhc.setHsaHeadCount(covrgEntry.getValue().getHsaHeadCount());
					bphc.getCoverageLevelHeadCount().add(clhc);
				}
			}
		}

		return bmap;
	}

	@Override
	public Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> getAdditionalPlansHeadCountByGroup(String company,
			Date effDate) {
		Query query = entityManager.createNamedQuery(ADDL_GROUP_HEADCOUNTS);
		query.setParameter(BSSQueryConstants.COMPANY, company);
		query.setParameter("effdt", effDate);
		List<Object[]> results = DaoUtils.getResultList(query, ADDL_GROUP_HEADCOUNTS);
		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> map = new HashMap<>();

		for (Object[] r : results) {
			PlanCoverageLevelHeadCount covrgHeadCount = new PlanCoverageLevelHeadCount();
			String groupName = (String) r[0];
			String planType = (String) r[1];
			String plan = (String) r[2];
			int headCount = ((BigDecimal) r[3]).intValue();
			logger.debug("ADDITIONAL PLAN FROM PS : {}\t HEAD COUNT : {}\t GROUP NAME  : {}", plan, headCount, groupName);

			covrgHeadCount.setGroupName(groupName);
			covrgHeadCount.setPlanType(planType);
			covrgHeadCount.setBenefitPlan(plan);
			covrgHeadCount.setHeadCount(headCount);

			if (map.get(groupName) != null) {
				Map<String, List<PlanCoverageLevelHeadCount>> planHeadCountsMap = map.get(groupName);
				if (planHeadCountsMap.get(plan) != null) {
					List<PlanCoverageLevelHeadCount> list = planHeadCountsMap.get(plan);
					list.add(covrgHeadCount);
					planHeadCountsMap.put(plan, list);
				} else {
					List<PlanCoverageLevelHeadCount> list = new ArrayList<>();
					list.add(covrgHeadCount);
					planHeadCountsMap.put(plan, list);
				}
				map.put(groupName, planHeadCountsMap);
			} else {
				Map<String, List<PlanCoverageLevelHeadCount>> planHeadCountsMap = new HashMap<>();
				List<PlanCoverageLevelHeadCount> list = new ArrayList<>();
				list.add(covrgHeadCount);
				planHeadCountsMap.put(plan, list);
				map.put(groupName, planHeadCountsMap);
			}
		}
		return map;
	}

	@Override
	public List<BenefitGroup> getBenefitPrograms(String pfClient, Date currentPlanYearStartDate) {
		Query q = entityManager.createNamedQuery("getBenefitPrograms");
		q.setParameter(BSSQueryConstants.PF_CLIENT, pfClient);
		q.setParameter("currentPlanYearStartDate", currentPlanYearStartDate);
		List<BenefitGroup> benefitGroups = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getBenefitPrograms");
		for (Object[] r : results) {
			BenefitGroup bg = new BenefitGroup();
			bg.setBenefitProgram((String) r[0]);
			bg.setName((String) r[1]);
			bg.setType((String) r[2]);
			bg.setState((String) r[3]);
			benefitGroups.add(bg);
		}

		return benefitGroups;
	}

	@Override
	public Map<String, Map<String, Map<String, BenefitPlan>>> getHealthPlansForRenewalCompany(
			Date currentPlanYearStartDate, String pfClient, String companyCode) {
		Map<String, Map<String, Map<String, BenefitPlan>>> healthPlansByCompany = new HashMap<>();
		Query q = entityManager.createNamedQuery("getHealthProgramsAndPlans");
		q.setParameter("currentPlanYearStartDate", currentPlanYearStartDate);
		q.setParameter(BSSQueryConstants.PF_CLIENT, pfClient);
		q.setParameter("CompanyCode", companyCode);
		List<Object[]> results = DaoUtils.getResultList(q, "getHealthProgramsAndPlans");
		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String benefitPlanId = (String) r[1];
			String planType = (String) r[3];
			String offerTypeCode = (String) r[3];
			if (Constants.DENTAL_CODE.equals(planType) || Constants.VOLUNTARY_DENTAL_CODE.equals(planType)) {
				offerTypeCode = Constants.DENTAL_CODE;
			}
			if (Constants.VISION_CODE.equals(planType) || Constants.VOLUNTARY_VISION_CODE.equals(planType)) {
				offerTypeCode = Constants.VISION_CODE;
			}

			// creating BenefitPlan
			BenefitPlan bp = new BenefitPlan();
			bp.setId(benefitPlanId);
			bp.setPlanType(planType);
			bp.setVendorId((String) r[4]);
			// creating contributions
			PlanContribution cb = new PlanContribution();
			BigDecimal employeeContribution = (BigDecimal) r[6];
			BigDecimal employerContribution = (BigDecimal) r[7];
			cb.setBenefitPlanId(benefitPlanId);
			cb.setEmployeeContribution(employeeContribution);
			cb.setEmployerContribution(employerContribution);
			cb.setType(CoverageCodesEnums.valueOfId((String) r[5]));
			logger.info("benefitProgram : {}", benefitProgram);
			logger.info("BenefitPlan : {}", cb.getBenefitPlanId());
			logger.info("employeeContribution : {}", employeeContribution);
			logger.info("employerContribution : {}", employerContribution);
			BigDecimal employerPercent = employerContribution
					.divide(employeeContribution.add(employerContribution), 10, RoundingMode.CEILING)
					.multiply(Constants.BigDecimal_100);
			cb.setEmployerPercent(employerPercent);
			bp.getContributions().add(cb);
			if (null != healthPlansByCompany.get(benefitProgram)) {
				Map<String, Map<String, BenefitPlan>> benefitPlanTypeMap = healthPlansByCompany.get(benefitProgram);
				if (null != benefitPlanTypeMap) {
					Map<String, BenefitPlan> benefitPlanMap = benefitPlanTypeMap.get(offerTypeCode);
					if (null != benefitPlanMap) {
						BenefitPlan tempBp = benefitPlanMap.get(bp.getId());
						if (null != tempBp) {
							tempBp.getContributions().add(cb);
							benefitPlanMap.put(bp.getId(), tempBp);
						} else {
							benefitPlanMap.put(bp.getId(), bp);
						}
					} else {
						benefitPlanMap = new HashMap<>();
						benefitPlanMap.put(bp.getId(), bp);
						benefitPlanTypeMap.put(offerTypeCode, benefitPlanMap);
					}
				} else {
					benefitPlanTypeMap = new HashMap<>();
					Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
					benefitPlanMap.put(bp.getId(), bp);
					benefitPlanTypeMap.put(offerTypeCode, benefitPlanMap);
					healthPlansByCompany.put(benefitProgram, benefitPlanTypeMap);
				}
			} else {
				Map<String, Map<String, BenefitPlan>> benefitPlanTypeMap = new HashMap<>();
				Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
				benefitPlanMap.put(bp.getId(), bp);
				benefitPlanTypeMap.put(offerTypeCode, benefitPlanMap);
				healthPlansByCompany.put(benefitProgram, benefitPlanTypeMap);
			}
		}

		return healthPlansByCompany;
	}

	@Override
	public Map<String, Map<String, Map<String, BenefitPlan>>> getAdditionalBenefitPlansForRenewalCompany(
			Date currentPlanYearStartDate, String pfClient, String companyCode) {
		Map<String, Map<String, Map<String, BenefitPlan>>> bgsADPlansMap = new HashMap<>();
		Query q = entityManager.createNamedQuery("getAdditionalBenefitSelections");
		q.setParameter("currentPlanYearStartDate", currentPlanYearStartDate);
		q.setParameter(BSSQueryConstants.PF_CLIENT, pfClient);
		q.setParameter("CompanyCode", companyCode);
		List<Object[]> results = DaoUtils.getResultList(q, "getAdditionalBenefitSelections");
		for (Object[] r : results) {
			String benefitProgram = (String) r[0];
			String planType = (String) r[3];
			// creating BenefitPlan
			BenefitPlan bp = new BenefitPlan();
			bp.setId((String) r[1]);
			bp.setPlanType(planType);
			bp.setEstimatedTotalCost((BigDecimal) r[5]);
			boolean employeePaid = (BigDecimal) r[4] == new BigDecimal(0) ? Boolean.TRUE : Boolean.FALSE;
			bp.setEmployeePaid(employeePaid);

			if (null != bgsADPlansMap.get(benefitProgram)) {
				Map<String, Map<String, BenefitPlan>> benefitPlanTypeMap = bgsADPlansMap.get(benefitProgram);
				if (null != benefitPlanTypeMap) {
					Map<String, BenefitPlan> benefitPlanMap = benefitPlanTypeMap.get(planType);
					if (null != benefitPlanMap) {
						benefitPlanMap.put(bp.getId(), bp);
						benefitPlanTypeMap.put(planType, benefitPlanMap);
					} else {
						benefitPlanMap = new HashMap<>();
						benefitPlanMap.put(bp.getId(), bp);
						benefitPlanTypeMap.put(planType, benefitPlanMap);
					}
				} else {
					benefitPlanTypeMap = new HashMap<>();
					Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
					benefitPlanMap.put(bp.getId(), bp);
					benefitPlanTypeMap.put(planType, benefitPlanMap);
					bgsADPlansMap.put(benefitProgram, benefitPlanTypeMap);
				}
			} else {
				Map<String, Map<String, BenefitPlan>> benefitPlanTypeMap = new HashMap<>();
				Map<String, BenefitPlan> benefitPlanMap = new HashMap<>();
				benefitPlanMap.put(bp.getId(), bp);
				benefitPlanTypeMap.put(planType, benefitPlanMap);
				bgsADPlansMap.put(benefitProgram, benefitPlanTypeMap);
			}
		}
		return bgsADPlansMap;
	}

	@Override
	public Map<String, String> getEligRuleIdsByClient(String pfClient, Date effDate) {
		Query q = entityManager.createNamedQuery("getEligRuleIdsByClient");
		q.setParameter(BSSQueryConstants.PF_CLIENT, pfClient);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		Map<String, String> eligRuleIdMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getEligRuleIdsByClient");
		for (Object[] r : results) {
			eligRuleIdMap.put((String) r[1], (String) r[0]);
		}
		return eligRuleIdMap;
	}

	@Override
	public Map<String, String> getWaitPeriodByClient(String pfClient, String company, Date effDate) {
		Query q = entityManager.createNamedQuery("getWaitPeriodByClient");
		q.setParameter(BSSQueryConstants.COMPANY, company);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		Map<String, String> waitPeriodMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getWaitPeriodByClient");
		for (Object[] r : results) {
			waitPeriodMap.put((String) r[1], (String) r[0]);
		}
		return waitPeriodMap;
	}

	@Override
	public Map<String, Map<String, Map<String, Object>>> getRenewalFundingDetails(String company, Date effDate) {
		Query q = entityManager.createNamedQuery("getRenewalFundingDetails");
		q.setParameter(BSSQueryConstants.COMPANY, company);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		Map<String, Map<String, Map<String, Object>>> groupFundingMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, "getRenewalFundingDetails");
		for (Object[] r : results) {
			Map<String, Map<String, Object>> planTypeFunding = new HashMap<>();
			// MEDICAL funding data
			Map<String, Object> medicalCoverageLevelFunding = new HashMap<>();
			String medicalFundingType = (String) r[9];
			BigDecimal waverAllowance = (BigDecimal) r[10];
			BigDecimal bsuppExcessOption = new BigDecimal(ExcessOptionEnum.getType((String) r[46]));
			if (StringUtils.isNotBlank(medicalFundingType)) {
				medicalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, medicalFundingType);
				medicalCoverageLevelFunding.put(BSSApplicationConstants.WAIVER_ALLOWANCE, waverAllowance);
				medicalCoverageLevelFunding.put(BSSApplicationConstants.BSUPP_EXCESS_OPTION, bsuppExcessOption);
				if (BSSApplicationConstants.BFPCT.equals(medicalFundingType)
						|| BSSApplicationConstants.CFPCT.equals(medicalFundingType)) {
					String fundingBasePlan = null;
					if (StringUtils.isNotBlank((String) r[11])) {
						fundingBasePlan = (String) r[11];
					}
					medicalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, fundingBasePlan);
					if (BSSApplicationConstants.BFPCT.equals(medicalFundingType)) {
						medicalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PCT, (BigDecimal) r[12]);
						if ("Z".equals((String) r[13])) {
							medicalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG, "all");
						} else {
							medicalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG,
									CoverageCodesEnums.valueOfId((String) r[13]));
						}
					} else {
						medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), (BigDecimal) r[14]);
						medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
								(BigDecimal) r[15]);
						medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
								(BigDecimal) r[16]);
						medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(),
								(BigDecimal) r[17]);
					}
					if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
						medicalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[18]);
						medicalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[19]);
						medicalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[20]);
						medicalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[21]);
					}
				} else if (BSSApplicationConstants.BSUPP.equals(medicalFundingType)) {
					medicalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, null);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), (BigDecimal) r[5]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
							(BigDecimal) r[6]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
							(BigDecimal) r[7]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), (BigDecimal) r[8]);
				}else {
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), (BigDecimal) r[14]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
							(BigDecimal) r[15]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
							(BigDecimal) r[16]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), (BigDecimal) r[17]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[18]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[19]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[20]);
					medicalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[21]);
				}
				planTypeFunding.put(Constants.MEDICAL_CODE, medicalCoverageLevelFunding);
			}
			// DENTAL funding data
			Map<String, Object> dentalCoverageLevelFunding = new HashMap<>();
			String dentalFundingType = (String) r[22];
			if (StringUtils.isNotBlank(dentalFundingType)) {
				dentalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, dentalFundingType);
				if (BSSApplicationConstants.BFPCT.equals(dentalFundingType)
						|| BSSApplicationConstants.CFPCT.equals(dentalFundingType)) {
					String fundingBasePlan = null;
					if (StringUtils.isNotBlank((String) r[23])) {
						fundingBasePlan = (String) r[23];
					}
					dentalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, fundingBasePlan);
					if (BSSApplicationConstants.BFPCT.equals(dentalFundingType)) {
						dentalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PCT, (BigDecimal) r[24]);
						if ("Z".equals((String) r[25])) {
							dentalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG, "all");
						} else {
							dentalCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG,
									CoverageCodesEnums.valueOfId((String) r[25]));
						}
					} else {
						dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), (BigDecimal) r[26]);
						dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
								(BigDecimal) r[27]);
						dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
								(BigDecimal) r[28]);
						dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(),
								(BigDecimal) r[29]);
					}
					if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
						dentalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[30]);
						dentalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[31]);
						dentalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[32]);
						dentalCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[33]);
					}
				} else {
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), (BigDecimal) r[26]);
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
							(BigDecimal) r[27]);
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
							(BigDecimal) r[28]);
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), (BigDecimal) r[29]);
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[30]);
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[31]);
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[32]);
					dentalCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[33]);
				}
				planTypeFunding.put(Constants.DENTAL_CODE, dentalCoverageLevelFunding);
			}
			// VISION funding data
			Map<String, Object> visionCoverageLevelFunding = new HashMap<>();
			String visionFundingType = (String) r[34];
			if (StringUtils.isNotBlank(visionFundingType)) {
				visionCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_TYPE, visionFundingType);
				if (BSSApplicationConstants.BFPCT.equals(visionFundingType)
						|| BSSApplicationConstants.CFPCT.equals(visionFundingType)) {
					String fundingBasePlan = null;
					if (StringUtils.isNotBlank((String) r[35])) {
						fundingBasePlan = (String) r[35];
					}
					visionCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PLAN, fundingBasePlan);
					if (BSSApplicationConstants.BFPCT.equals(visionFundingType)) {
						visionCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_PCT, (BigDecimal) r[36]);
						if ("Z".equals((String) r[37])) {
							visionCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG, "all");
						} else {
							visionCoverageLevelFunding.put(BSSApplicationConstants.FUNDING_BASE_CVG,
									CoverageCodesEnums.valueOfId((String) r[37]));
						}
					} else {
						visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), (BigDecimal) r[38]);
						visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
								(BigDecimal) r[39]);
						visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
								(BigDecimal) r[40]);
						visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(),
								(BigDecimal) r[41]);
					}
					if (BSSApplicationConstants.FLAT_MAX.equals(fundingBasePlan)) {
						visionCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[42]);
						visionCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[43]);
						visionCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[44]);
						visionCoverageLevelFunding.put(
								CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() + BSSApplicationConstants.LIMIT,
								(BigDecimal) r[45]);
					}
				} else {
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId(), (BigDecimal) r[38]);
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId(),
							(BigDecimal) r[39]);
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId(),
							(BigDecimal) r[40]);
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId(), (BigDecimal) r[41]);
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[42]);
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[43]);
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[44]);
					visionCoverageLevelFunding.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() + BSSApplicationConstants.LIMIT,
							(BigDecimal) r[45]);
				}
				planTypeFunding.put(Constants.VISION_CODE, visionCoverageLevelFunding);
			}
			groupFundingMap.put((String) r[2], planTypeFunding);
		}
		return groupFundingMap;
	}

	@Override
	public Map<String, ActiveEligibleEECount> getActiveEligibleEECount(Company company, boolean history,
			long strategyId, RealmPlanYear realmPlanYear) {
		Query q = null;
		String sqlName;
		Set<String> sdiStates = RulesAndConfigsUtils.getSDIStates(company.getRealmPlanYearId());
		boolean isDisabledBundledOn = RulesAndConfigsUtils.isDisabledBundledOn(realmPlanYear.getId());
		if(sdiStates.isEmpty()) {
			sdiStates.add(BSSApplicationConstants.EMPTY_SPACE);
		}
		if (history) {
			sqlName = "DISAB_ELIG_EE_COUNT_BY_WORK_STATE_HISTORY";
			q = entityManager.createNamedQuery(sqlName);
			q.setParameter("effdt", realmPlanYear.getPlanYearEnd());
		} else {
			sqlName = "DISAB_ELIG_EE_COUNT_BY_WORK_STATE";
			q = entityManager.createNamedQuery("DISAB_ELIG_EE_COUNT_BY_WORK_STATE");
			q.setParameter("realmYrId", realmPlanYear.getId());
			q.setParameter("effdt", company.getRealmPlanYear().getPlanYearStart());
			q.setParameter(BSSQueryConstants.STRATEGY_ID, strategyId);
		}
		q.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		q.setParameter("sdiStates", sdiStates);
		Map<String, ActiveEligibleEECount> eligHeadCountMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, sqlName);
		if (results != null && !results.isEmpty()) {
			for (Object[] result : results) {
				String benProg = (String) result[0];
				int headCount = ((BigDecimal) result[2]).intValue();
				boolean inState = "IN".equals((String) result[1]);
				ActiveEligibleEECount eligibleEECount = null;
				if (null != eligHeadCountMap.get(benProg)) {
					eligibleEECount = eligHeadCountMap.get(benProg);
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
				} else {
					eligibleEECount = new ActiveEligibleEECount();
					eligibleEECount.setBenProg(benProg);
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
					eligHeadCountMap.put(benProg, eligibleEECount);
				}
			}
		}
		return eligHeadCountMap;
	}
	
	@Override
	public Map<String, Integer> getPrimaryEnrolledEECount(Company company, boolean history, long strategyId) {
		Query q = null;
		String sqlName;
		if (history) {
			sqlName = "PRIMARY_ENROLLED_HEADCOUNT_BY_GROUP_CURRENT";
			q = entityManager.createNamedQuery(sqlName);
			q.setParameter("COMPANY_CODE", company.getCode());
			q.setParameter("EFFDT", company.getPlanEndDate());
			q.setParameter("primaryPlanTypes", BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		} else {
			sqlName = "PRIMARY_ENROLLED_HEADCOUNT_BY_GROUP_FUTURE";
			q = entityManager.createNamedQuery(sqlName);
			q.setParameter("COMPANY_CODE", company.getCode());
			q.setParameter("EFFDT", company.getPlanEndDate());
			q.setParameter("realmYearId", company.getRealmPlanYearId());
			q.setParameter("strategyId", strategyId);
			q.setParameter("primaryPlanTypes", BSSApplicationConstants.PRIMARY_PLAN_TYPES);
		}
		Map<String, Integer> eligHeadCountMap = new HashMap<>();
		List<Object[]> results = DaoUtils.getResultList(q, sqlName);
		if (results != null && !results.isEmpty()) {
			for (Object[] result : results) {
				String benProg = (String) result[0];
				int headCount = ((BigDecimal) result[1]).intValue();
				eligHeadCountMap.put(benProg, headCount);
			}
		}
		return eligHeadCountMap;
	}	
	

	@Override
	public Map<String, String> getRateTableIds(String benefitProgram, Date effDate) {
		Query q = entityManager.createNamedQuery("NEW_GET_RATE_TBL_IDS");
		q.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		List<Object[]> results = DaoUtils.getResultList(q, "NEW_GET_RATE_TBL_IDS");
		Map<String, String> rateTableMap = new HashMap<>();
		for (Object[] r : results) {
			String planType = (String) r[0];
			String rateTableId = (String) r[1];
			rateTableMap.put(planType, rateTableId);
		}
		return rateTableMap;
	}
	
	@Override
	public List<String> getBsuppVoluntaryPlanTypes(String benefitProgram, Date effDate, boolean includeGroupPlanTypes,
			List<String> supportedVoluntaryPlanTypes) {
		Query q = entityManager.createNamedQuery("BSUPP_VOL_SELECTED_PLN_TYPES");
		q.setParameter("benefitProgram", benefitProgram);
		q.setParameter("effDate", effDate);
		List<Object[]> results = DaoUtils.getResultList(q, "BSUPP_VOL_SELECTED_PLN_TYPES");
		List<String> planTypes = new ArrayList<>();
		for (Object[] r : results) {
			String planType = (String) r[1];
			if (BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(planType)) {
				if (includeGroupPlanTypes && !planTypes.contains(planType)) {
					planTypes.add(planType);
				}
				planType = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
			}
			if (BSSApplicationConstants.VISION_PLAN_TYPE.equals(planType)) {
				if (includeGroupPlanTypes && !planTypes.contains(planType)) {
					planTypes.add(planType);
				}
				planType = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;
			}
			if (supportedVoluntaryPlanTypes.contains(planType) && !planTypes.contains(planType)) {
				planTypes.add(planType);
			}
		}
		return planTypes;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public StrategyHsaFundingDto getPsHsaFundingDetails(String companyCode, Date effDate) {

		final int OPTION_INDEX = 1;
		final int LUMP_SUM_FREQ_INDEX = 2;
		final int LUMP_SUM_EE_INDEX = 3;
		final int LUMP_SUM_FAM_INDEX = 4;
		final int ANNUAL_LUMP_SUM_MONTH_INDEX = 5;
		final int MONTHLY_LUMP_SUM_Q1_MONTH_INDEX = 6;
		final int MONTHLY_LUMP_SUM_Q2_MONTH_INDEX = 7;
		final int MONTHLY_LUMP_SUM_Q3_MONTH_INDEX = 8;
		final int MONTHLY_LUMP_SUM_Q4_MONTH_INDEX = 9;
		final int CONTRIBUTION_EE_INDEX = 10;
		final int CONTRIBUTION_FAM_INDEX = 11;
		final int CONTRIBUTION_FREQ = 12;
		
		StrategyHsaFundingDto strategyHsaFundingDto = null;
		
		Query q = entityManager.createNamedQuery("GET_HSA_FUNDING_DETAILS");
		q.setParameter(BSSQueryConstants.COMPANY, companyCode);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		List<Object[]> results = q.getResultList();
		if (!results.isEmpty()) {
			Object[] r = results.get(0);
			strategyHsaFundingDto = new StrategyHsaFundingDto();
			int optionId = Integer.parseInt((String) r[OPTION_INDEX]);
			strategyHsaFundingDto.setOptionId(optionId);
			if (optionId > 0) {
				String lumpSumFrequency = (String) r[LUMP_SUM_FREQ_INDEX];
				strategyHsaFundingDto.setLumpSumFrequency(lumpSumFrequency);
				if (lumpSumFrequency != null) {
					if (BSSApplicationConstants.HSA_ANNUAL.equals(lumpSumFrequency)) {
						strategyHsaFundingDto.setAnnualEeAmount(((BigDecimal) r[LUMP_SUM_EE_INDEX]));
						strategyHsaFundingDto.setAnnualFamilyAmount(((BigDecimal) r[LUMP_SUM_FAM_INDEX]));
						strategyHsaFundingDto
								.setAnnualMonth((" ").equals((String) r[ANNUAL_LUMP_SUM_MONTH_INDEX]) ? null
										: Integer.parseInt((String) r[ANNUAL_LUMP_SUM_MONTH_INDEX]));
					} else if (BSSApplicationConstants.HSA_QUARTERLY.equals(lumpSumFrequency)) {
						strategyHsaFundingDto.setQuarterlyEeAmount(((BigDecimal) r[LUMP_SUM_EE_INDEX]));
						strategyHsaFundingDto.setQuarterlyFamilyAmount(((BigDecimal) r[LUMP_SUM_FAM_INDEX]));
						strategyHsaFundingDto
								.setQ1Month((" ").equals((String) r[MONTHLY_LUMP_SUM_Q1_MONTH_INDEX]) ? null
										: Integer.parseInt((String) r[MONTHLY_LUMP_SUM_Q1_MONTH_INDEX]));
						strategyHsaFundingDto
								.setQ2Month((" ").equals((String) r[MONTHLY_LUMP_SUM_Q2_MONTH_INDEX]) ? null
										: Integer.parseInt((String) r[MONTHLY_LUMP_SUM_Q2_MONTH_INDEX]));
						strategyHsaFundingDto
								.setQ3Month((" ").equals((String) r[MONTHLY_LUMP_SUM_Q3_MONTH_INDEX]) ? null
										: Integer.parseInt((String) r[MONTHLY_LUMP_SUM_Q3_MONTH_INDEX]));
						strategyHsaFundingDto
								.setQ4Month((" ").equals((String) r[MONTHLY_LUMP_SUM_Q4_MONTH_INDEX]) ? null
										: Integer.parseInt((String) r[MONTHLY_LUMP_SUM_Q4_MONTH_INDEX]));
					}
				}
				String contributionFrequency = (String) r[CONTRIBUTION_FREQ];
				strategyHsaFundingDto.setContributionFrequency(contributionFrequency);
				strategyHsaFundingDto.setMonthlyEeAmount(((BigDecimal) r[CONTRIBUTION_EE_INDEX]));
				strategyHsaFundingDto.setMonthlyFamilyAmount(((BigDecimal) r[CONTRIBUTION_FAM_INDEX]));
			}
		}
			
		return strategyHsaFundingDto;
	}
	
	private List<Object[]> getEnrolledHeadcountData(Company company, Date effDate, boolean isVendorMappingOn) {

		List<Object[]> results = new ArrayList<>();

		if (isVendorMappingOn) {
			Query q = hrpEntityManager.createNamedQuery("MDV_ENROLLED_CENSUS_HEADCOUNTS");
			q.setParameter(BSSQueryConstants.COMPANY_CODE, company.getCode());
			q.setParameter(BSSQueryConstants.REALM_PLAN_YEAR_ID, company.getRealmPlanYear().getId());
			q.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
			results = DaoUtils.getResultList(q, "MDV_ENROLLED_CENSUS_HEADCOUNTS");
		} else {
			Query q = entityManager.createNamedQuery("MDV_GROUP_HEADCOUNTS_UPDATED");
			q.setParameter("COMPANY_CODE", company.getCode());
			q.setParameter("EFFDT", effDate);
			q.setParameter(BSSQueryConstants.PRIMARY_PLAN_TYPES, BSSApplicationConstants.PRIMARY_PLAN_TYPES);
			results = DaoUtils.getResultList(q, "MDV_GROUP_HEADCOUNTS_UPDATED");
		}

		return results;
	}
}
