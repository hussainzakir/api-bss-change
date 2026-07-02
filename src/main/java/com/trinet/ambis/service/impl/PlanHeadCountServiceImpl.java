package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.BenefitCategoriesHelper;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PlanHeadCountDao;
import com.trinet.ambis.persistence.dao.hrp.PlanMappingDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioHeadCountDataDao;
import com.trinet.ambis.persistence.dao.hrp.PortfolioRuleDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.PlanMapping;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.PlanHeadCountService;
import com.trinet.ambis.service.RealmPlanYearRuleService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.model.ActiveEligibleEECount;
import com.trinet.ambis.service.model.BenefitPlanHeadCount;
import com.trinet.ambis.service.model.BenefitProgramHeadCountPlans;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.CoverageLevelHeadCount;
import com.trinet.ambis.service.model.HeadCountBenefitPlan;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanCoverageLevelHeadCount;
import com.trinet.ambis.service.model.PlanHeadCount;
import com.trinet.ambis.service.prospect.ProspectPlanHeadCountService;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;

/**
 * @author rvutukuri
 */

@Service
public class PlanHeadCountServiceImpl implements PlanHeadCountService {

	@Autowired
	PlanHeadCountDao planHeadCountDao;
	@Autowired
	CompanyService companyService;
	@Autowired
	RenewalDataDao renewalDataDao;
	@Autowired
	RealmPlanYearService realmPlanYearService;
	@Autowired
	RealmPlanYearRuleService realmPlanYearRuleService;
	@Autowired
	RealmDataDao realmDataDao;
	@Autowired
	StrategyDataDao strategyDataDao;
	@Autowired
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;
	@Autowired
	BenefitGroupService benefitGroupService;
	@Autowired
	protected PortfolioHeadCountDataDao portfolioHeadCountDataDao;
	@Autowired
	PortfolioRuleDao portfolioRuleDao;
	@Autowired
	PlanMappingDao planMappingDao;
	@Autowired
	HeadCountService headCountService;
	@Autowired
	ProspectPlanHeadCountService prospectPlanHeadCountService;

	private static final String PRIMARY_HEADCOUNT_KEY = BSSApplicationConstants.PRIMARY_HEADCOUNT_KEY;
	private static final String SECONDARY_HEADCOUNT_KEY = BSSApplicationConstants.SECONDARY_HEADCOUNT_KEY;
	private static final String TOTAL_HEADCOUNT_KEY = BSSApplicationConstants.TOTAL_HEADCOUNT_KEY;

	@Override
	public List<PlanHeadCount> getPlanHeadCount(Company company, Long strategyId) {
		List<PlanHeadCount> planHeadCounts = new ArrayList<>();

		Map<String, List<CoverageLevel>> mapOfCoverageLevels = realmDataDao
				.getCoverageCodesDescByPlanTypes(Constants.primaryPlanTypesCodes, company.getRealmPlanYear().getId());
		
		Map<String, Map<String, String>> defaultPlanMap = realmDataDao
				.getPortfilioDefaultPlans(company.getRealmPlanYearId());

		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		Map<String, Set<PlanCarrier>> planCarrierMap = portfolioRuleDao.getPortfoliosByHqRegion(
				company.getRealmPlanYearId(), company.getHeadQuatersState(), company.getZipCode(),
				company.getExclusiveMedPlan(), company.getPlanStartDate(), isPickChoose );

		// applying exclusivity rules for portfolios
		BenefitCategoriesHelper.updatePlanCarrierExclusivity(company, planCarrierMap, company.isRenewalCompany());

		Set<String> primaryPlanCarriers = BenefitCategoriesHelper.getPlanCarriers(planCarrierMap);
		Set<String> outOfRegionPlans = CommonServiceHelper.getOutOfRegionPlansToExclude(company, primaryPlanCarriers,
				realmDataDao);
		
		Map<String, PlanMapping> realmPlanMapping = planMappingDao.getPrimaryPlanMappings(company, outOfRegionPlans);
		Map<String, String> eeErPlanMapping = employerEmployeePlansMappingDao
				.getEeAndErPlanMapping(company.getRealmPlanYearId());
		List<Strategy> strategies = strategyDataDao.getFutureStrategies(company.getCode(),
				company.getRealmPlanYear().getId());
		String submittedStrategyId = null;
		String recommendedStrategyId = null;
		
		for (Strategy s : strategies) {
			if (s.isSubmitted()) {
				submittedStrategyId = s.getId().toString();
			}
			if (BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED.equals(s.getType())) {
				recommendedStrategyId = s.getId().toString();
			}
		}
		Map<String, List<String>> benefitProgramPlanTypes = null;
		if (null != submittedStrategyId) {
			benefitProgramPlanTypes = strategyDataDao.getOfferedPlanTypesByStrategy(submittedStrategyId);
		} else {
			benefitProgramPlanTypes = strategyDataDao.getOfferedPlanTypesByStrategy(recommendedStrategyId);
		}

		Date effDate = CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupAdditionalHeadCountMap = renewalDataDao
				.getAdditionalPlansHeadCountByGroup(company.getCode(), effDate);

		RealmPlanYear prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear());
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());

		Map<String, List<BenefitPlanHeadCount>> groupCovrgHeadCountMap = renewalDataDao.getPlanHeadCountByGroups(
				company, strategyId, effDate, realmPlanMapping, eeErPlanMapping,
				benefitProgramPlanTypes, isVendorMappingOn);

		List<BenefitGroup> benGrps = benefitGroupService.getAllBenefitGroups(company.getId(),
				BSSApplicationConstants.STATUS_ACTIVE);
		addMissingBGToPrimaryBGHeadCountMap(benGrps, groupCovrgHeadCountMap);

		// Get active employees count for additional benefits.
		Map<String, ActiveEligibleEECount> activeEligibleEmplCount = null;
		if (company.isProspectCompany() || company.isProspectConvertedOnboardingClient()) {
			activeEligibleEmplCount = prospectPlanHeadCountService.getProspectEligibleEmployeeCount(company,
					strategyId);
		} else {
			activeEligibleEmplCount = headCountService.getEligibleEmployeeCount(company, strategyId, prevRealmYear,
					false);
		}

		for (Entry<String, List<BenefitPlanHeadCount>> benefitPlanEntry : groupCovrgHeadCountMap.entrySet()) {
			String bnp = benefitPlanEntry.getKey();
			PlanHeadCount ph = new PlanHeadCount();
			ph.setBenefitProgram(bnp);
			// Set additional benefit head count details
			ActiveEligibleEECount activeEligibleEECount = activeEligibleEmplCount.get(bnp);
			if (null != activeEligibleEECount) {
				Map<String, Integer> adBenefitPlans = new HashMap<>();
				adBenefitPlans.put(PRIMARY_HEADCOUNT_KEY, activeEligibleEECount.getPrimaryHeadCount());
				adBenefitPlans.put(SECONDARY_HEADCOUNT_KEY, activeEligibleEECount.getSecondaryHeadCount());
				adBenefitPlans.put(TOTAL_HEADCOUNT_KEY, activeEligibleEECount.getTotalHeadCount());
				ph.setAdBenefitPlans(adBenefitPlans);
			} else {
				Map<String, Integer> adBenefitPlans = new HashMap<>();
				adBenefitPlans.put(PRIMARY_HEADCOUNT_KEY, 0);
				adBenefitPlans.put(SECONDARY_HEADCOUNT_KEY, 0);
				adBenefitPlans.put(TOTAL_HEADCOUNT_KEY, 0);
				ph.setAdBenefitPlans(adBenefitPlans);
			}

			Map<String, List<PlanCoverageLevelHeadCount>> adPlanHeadCounts = groupAdditionalHeadCountMap.get(bnp);
			if (null != adPlanHeadCounts && !adPlanHeadCounts.isEmpty()) {
				for (List<PlanCoverageLevelHeadCount> pclhList : adPlanHeadCounts.values()) {
					if (CollectionUtils.isNotEmpty(pclhList)) {
						for (PlanCoverageLevelHeadCount phc : pclhList) {
							if (phc.getPlanType().equals(BSSApplicationConstants.STD_CODE)
									|| phc.getPlanType().equals(BSSApplicationConstants.LTD_CODE)) {
								ph.setHasAdditionalHeadCount(true);
								break;
							}
						}
					}
				}
			} else {
				ph.setHasAdditionalHeadCount(false);
			}

			List<BenefitPlanHeadCount> benefitPlans = benefitPlanEntry.getValue();
			if (benefitPlans != null) {
				for (BenefitPlanHeadCount bps : benefitPlans) {
					if (bps.getPlanType().equals(Constants.MEDICAL_CODE)) {
						updateCoveragelevelsList(mapOfCoverageLevels.get(Constants.MEDICAL),
								bps.getCoverageLevelHeadCount());
					} else if (Constants.dentalPlanTypeList.contains(bps.getPlanType())) {
						updateCoveragelevelsList(mapOfCoverageLevels.get(Constants.DENTAL),
								bps.getCoverageLevelHeadCount());
					} else if (Constants.visionPlanTypeList.contains(bps.getPlanType())) {
						updateCoveragelevelsList(mapOfCoverageLevels.get(Constants.VISION),
								bps.getCoverageLevelHeadCount());
					}
				}
				ph.setBenefitPlans(benefitPlans);
			}
			planHeadCounts.add(ph);
		}

		return planHeadCounts;
	}

	@Override
	public List<BenefitProgramHeadCountPlans> getBenefitProgramHeadCountPlans(Company company, Long strategyId) {
		List<BenefitProgramHeadCountPlans> bpHeadCountPlans = new ArrayList<>();
		
		RealmPlanYear prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear());
		Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts = portfolioHeadCountDataDao
				.getHeadCountPlans(strategyId);
		
		addMissingHeadCountPlansToPrograms(strategyId, benefitProgramHeadCounts);

		Map<String, Map<String, List<PlanCoverageLevelHeadCount>>> groupAdditionalHeadCountMap = renewalDataDao
				.getAdditionalPlansHeadCountByGroup(company.getCode(), prevRealmYear.getPlanYearEnd());

		// Get active employees count for additional benefits.
		Map<String, ActiveEligibleEECount> activeEligibleEmplCount = headCountService.getEligibleEmployeeCount(company,
				strategyId, prevRealmYear, false);

		for (Entry<String, List<HeadCountBenefitPlan>> bpEntry : benefitProgramHeadCounts.entrySet()) {
			String bp = bpEntry.getKey();
			BenefitProgramHeadCountPlans bphcp = new BenefitProgramHeadCountPlans();
			bphcp.setBenefitProgram(bp);
			bphcp.setBenefitPlans(bpEntry.getValue());

			ActiveEligibleEECount activeEligibleEECount = activeEligibleEmplCount.get(bp);
			if (null != activeEligibleEECount) {
				Map<String, Integer> adBenefitPlans = new HashMap<>();
				adBenefitPlans.put(PRIMARY_HEADCOUNT_KEY, activeEligibleEECount.getPrimaryHeadCount());
				adBenefitPlans.put(SECONDARY_HEADCOUNT_KEY, activeEligibleEECount.getSecondaryHeadCount());
				adBenefitPlans.put(TOTAL_HEADCOUNT_KEY, activeEligibleEECount.getTotalHeadCount());
				bphcp.setAdBenefitPlans(adBenefitPlans);
			} else {
				Map<String, Integer> adBenefitPlans = new HashMap<>();
				adBenefitPlans.put(PRIMARY_HEADCOUNT_KEY, 0);
				adBenefitPlans.put(SECONDARY_HEADCOUNT_KEY, 0);
				adBenefitPlans.put(TOTAL_HEADCOUNT_KEY, 0);
				bphcp.setAdBenefitPlans(adBenefitPlans);
			}

			Map<String, List<PlanCoverageLevelHeadCount>> adPlanHeadCounts = groupAdditionalHeadCountMap.get(bp);
			if (null != adPlanHeadCounts && !adPlanHeadCounts.isEmpty()) {
				for (List<PlanCoverageLevelHeadCount> pclhList : adPlanHeadCounts.values()) {
					if (CollectionUtils.isNotEmpty(pclhList)) {
						for (PlanCoverageLevelHeadCount phc : pclhList) {
							if (phc.getPlanType().equals(BSSApplicationConstants.STD_CODE)
									|| phc.getPlanType().equals(BSSApplicationConstants.LTD_CODE)) {
								bphcp.setHasAdditionalHeadCount(true);
								break;
							}
						}
					}
				}
			} else {
				bphcp.setHasAdditionalHeadCount(false);
			}
			bpHeadCountPlans.add(bphcp);
		}
		return bpHeadCountPlans;
	}

	/**
	 * This method is for adding the missing coverage codes.
	 * 
	 * @param coverageLevels
	 * @param coverageLevelHeadCount
	 */
	private void updateCoveragelevelsList(List<CoverageLevel> coverageLevels,
			List<CoverageLevelHeadCount> coverageLevelHeadCount) {
		List<CoverageLevelHeadCount> missing = new ArrayList<>();
		for (CoverageLevel cls : coverageLevels) {
			if (!Constants.COVERAGE_ALL.equals(cls.getId())) {
				boolean iscvgexists = false;
				for (CoverageLevelHeadCount chd : coverageLevelHeadCount) {
					if (chd.getCoverageLevel().equals(CoverageCodesEnums.valueOfCode(cls.getId()))) {
						chd.setCoverageLevel(cls.getId());
						iscvgexists = true;
					}
				}
				if (!iscvgexists) {
					CoverageLevelHeadCount cvf = new CoverageLevelHeadCount();
					cvf.setCoverageLevel(cls.getId());
					cvf.setHeadCount(0);
					missing.add(cvf);
				}
			}
		}
		coverageLevelHeadCount.addAll(missing);
	}

	private void addMissingBGToPrimaryBGHeadCountMap(List<BenefitGroup> benGrps,
			Map<String, List<BenefitPlanHeadCount>> groupCovrgHeadCountMap) {
		for (BenefitGroup benefitGroup : benGrps) {
			if (!groupCovrgHeadCountMap.containsKey(benefitGroup.getBenefitProgram())) {
				groupCovrgHeadCountMap.put(benefitGroup.getBenefitProgram(), null);
			}
		}
	}
	
	private Map<String, HeadCountBenefitPlan> getDistinctHeadCountPlans(
			Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts) {
		Map<String, HeadCountBenefitPlan> distinctHeadCountMap = new HashMap<>();
		for (List<HeadCountBenefitPlan> headCountBenefitPlanList : benefitProgramHeadCounts.values()) {
			for (HeadCountBenefitPlan headCountBenefitPlan : headCountBenefitPlanList) {

				HeadCountBenefitPlan emptyHeadCountBenefitPlan = new HeadCountBenefitPlan.HeadCountBenefitPlanBuilder()
						.benefitPlanId(headCountBenefitPlan.getBenefitPlanId())
						.planType(headCountBenefitPlan.getPlanType())
						.planCarrierId(headCountBenefitPlan.getPlanCarrierId()).populateZeroCvgLvlHeadCounts(true)
						.build();

				distinctHeadCountMap.put(emptyHeadCountBenefitPlan.getBenefitPlanId(), emptyHeadCountBenefitPlan);
			}
		}
		return distinctHeadCountMap;
	}
	
	protected void addMissingHeadCountPlansToPrograms(Long strategyId, Map<String, List<HeadCountBenefitPlan>> benefitProgramHeadCounts) {
		
		List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(strategyId,
				BSSApplicationConstants.STATUS_ACTIVE);
		
		Map<String, HeadCountBenefitPlan> distinctHeadCountMap = getDistinctHeadCountPlans(benefitProgramHeadCounts);
		
		for (BenefitGroup benefitGroup : benefitGroups) {
			String benefitProgram = benefitGroup.getBenefitProgram();
			
			if (!benefitProgramHeadCounts.containsKey(benefitProgram)) {
				benefitProgramHeadCounts.put(benefitProgram, new ArrayList<>());
			}
			List<HeadCountBenefitPlan> headCountBenefitPlans = benefitProgramHeadCounts.get(benefitProgram);

			for (Entry<String, HeadCountBenefitPlan> headCountEntry : distinctHeadCountMap.entrySet()) {
				String benefitPlan = headCountEntry.getKey();
				boolean bpExists = false;
				for (HeadCountBenefitPlan hb : headCountBenefitPlans) {
					if (hb.getBenefitPlanId().equals(benefitPlan)) {
						bpExists = true;
					}
				}
				if (!bpExists) {
					headCountBenefitPlans.add(headCountEntry.getValue());
				}
			}
		}		
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setPlanHeadCountDao(PlanHeadCountDao planHeadCountDao) {
		this.planHeadCountDao = planHeadCountDao;
	}

	public void setRenewalDataDao(RenewalDataDao renewalDataDao) {
		this.renewalDataDao = renewalDataDao;
	}

	public void setRealmPlanYearService(RealmPlanYearService realmPlanYearService) {
		this.realmPlanYearService = realmPlanYearService;
	}

	public void setRealmDataDao(RealmDataDao realmDataDao) {
		this.realmDataDao = realmDataDao;
	}
}