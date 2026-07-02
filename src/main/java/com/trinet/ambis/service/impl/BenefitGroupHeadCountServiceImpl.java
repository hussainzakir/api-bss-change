package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.BenefitGroupServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.EmployerEmployeePlansMappingDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.hrp.TemplateFundingDao;
import com.trinet.ambis.persistence.dao.hrp.XbssRealmPlyrPlanDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.BenefitGroupStrategy;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyGroupHeadCount;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitGroupHeadCountService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ContributionService;
import com.trinet.ambis.service.ExchangeService;
import com.trinet.ambis.service.HeadCountDistributionService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.PlanSelectionService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyGroupHeadCountService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.HeadCountData;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;


/**
 * @author rvutukuri
 *
 */
@Service
public class BenefitGroupHeadCountServiceImpl implements BenefitGroupHeadCountService {

	private static final Logger logger = LoggerFactory.getLogger(BenefitGroupHeadCountServiceImpl.class);

	@Autowired
	CompanyService companyService;


	@Autowired
	BenefitPlanService benefitPlanService;
	
	@Autowired
	PlanRatesService planRatesService;

	@Autowired
	BenefitGroupService benefitGroupService;

	@Autowired
	StrategyGroupService strategyGroupService;
	
	@Autowired
	StrategyGroupDataDao strategyGroupDataDao;

	@Autowired
	StrategyGroupHeadCountService strategyGroupHeadCountService;

	@Autowired
	PlanSelectionService planSelectionService;

	@Autowired
	RealmPlyrPlanService realmPlyrPlanService;

	@Autowired
	ContributionService contributionService;

	@Autowired
	HeadCountDistributionService headCountDistributionService;

	@Autowired
	EmployerEmployeePlansMappingDao employerEmployeePlansMappingDao;

	@Autowired
	TemplateFundingDao templateFundingDao;

	@Autowired
	StrategyDao strategyDao;
	
	@Autowired
	XbssRealmPlyrPlanDao realmPlyrPlanDao;
	
	@Autowired
	ExchangeService exchangeService;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void updateGroupHeadCount(Company company, List<HeadCountData> headCountList) {
		Map<String, List<BenefitPlanRate>> planRates = planRatesService.getBenefitPlanRatesBy(company);

		Map<String,XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService.getMapForRealmPlanYear( company.getRealmPlanYear().getId() );
		Map<String, String> map = employerEmployeePlansMappingDao.getEeAndErPlanMapping(company.getRealmPlanYear().getId());
 
		for (HeadCountData headCountData : headCountList) {
			BenefitGroupStrategy bgs = strategyGroupService
					.getBenefitGroupStrategyBy(headCountData.getStrategyGroupId());
			// Get All Plan Types for the given strategyId and groupId
			logger.info("UPDATING HEAD COUNT FOR STRATEGY : {}", bgs.getStrategyId());
			Strategy strategy = strategyDao.findByIdAndCompanyIdAndStatus(bgs.getStrategyId(), company.getId(),
					BSSApplicationConstants.STATUS_ACTIVE);

			Map<String, List<PlanSelection>> planMap = null;
			List<PlanSelection> planList = strategyGroupDataDao.getPlanSelections(bgs.getStrategyId(), bgs.getGroupId(),
					company.getRealmPlanYear().getId());

			List<String> mandatoryPlansToExclude = realmPlyrPlanDao.getAllMandatoryPlansExcludingGivenRegion(
					company.getHeadQuatersState(), BigDecimal.valueOf(company.getRealmPlanYearId()));

			// getting Head count plans from the template, for Passport
			// we can not distribute head count to
			// all the plans in the strategy

			Map<String, List<String>> headCountPlans = templateFundingDao.getTemplateHeadCountPlans(company,
					strategy.getPkgType());
			logger.info("NUMBER OF PLANS : {} \t PLANS : {}", headCountPlans.size(), headCountPlans);

			// Get EE and ER plan mapping
			List<PlanSelection> headCountPlanSelections = getHeadCountPlanSelections(planList, headCountPlans, map,
					company);
			setPlanRatesOnContributions(company, headCountPlanSelections, planRates, plyrPlanMap);
			planMap = getPlansByMDV(headCountPlanSelections, mandatoryPlansToExclude);

			for (Entry<String, List<PlanSelection>> entry : planMap.entrySet()) {
				List<PlanSelection> plans = planMap.get(entry.getKey());
				logger.debug("KEY : {}", entry.getKey());
				Map<String, Map<String, Integer>> planHeadCountMap = headCountDistributionService
						.planHeadCountDistribution(company, plans, headCountData);
				updateContributionHeadCount(planHeadCountMap, plans);
			}
			// updating head count on Additional benefit plans
			int headCount = getGroupHeadCount(headCountData);
			updateAdditionalBenefitPlansHeadCount(planList, headCount);

			// update Benefit Group HeadCount
			bgs.setHeadcount(headCount);

			if (!company.isRenewalCompany()) {
				List<StrategyGroupHeadCount> strategyGroupHcs = BenefitGroupServiceHelper
						.prepareStrategyGroupHeadCountObj(headCountData.getCovrgHeadCountMap(), bgs.getId());
				for (StrategyGroupHeadCount sghc : strategyGroupHcs) {
					strategyGroupHeadCountService.saveStrategyGroupHeadCount(sghc);
				}
			}
			strategyGroupService.saveBenefitGroupStrategy(bgs);
			logger.info("UPDATING HEAD COUNT DONE FOR STRATEGY ID : {}", strategy.getId());
		}
		// this needs to me removed for new company
		// flow.********************
		int companyCount = getCompanyBenefitGroupsHeadCount(company);
		company.setHeadcount(companyCount);
		companyService.createUpdateCompany(company);

	}

	private List<PlanSelection> getHeadCountPlanSelections(List<PlanSelection> planList,
			Map<String, List<String>> planMap, Map<String, String> map, Company company) {
		List<PlanSelection> list = new ArrayList<>();
		boolean dentalMatch = false;
		boolean visionMatch = false;
		String dentalPlanType = null;
		String visionPlanType = null;

		for (Entry<String, List<String>> entry : planMap.entrySet()) {
			if (entry.getKey().equals("11") || entry.getKey().equals("1D")) {
				dentalPlanType = entry.getKey();
				for (PlanSelection ps : planList) {
					if (ps.getPlanType().equals(entry.getKey())) {
						dentalMatch = true;
						break;
					}
				}
			} else if (entry.getKey().equals("14") || entry.getKey().equals("1V")) {
				visionPlanType = entry.getKey();
				for (PlanSelection ps : planList) {
					if (ps.getPlanType().equals(entry.getKey())) {
						visionMatch = true;
						break;
					}
				}
			}
		}

		for (String plan : planMap.get(dentalPlanType)) {
			String mappedPlan = plan;
			if (!dentalMatch) {
				// get Mapped plan
				mappedPlan = map.get(plan);
			}
			for (PlanSelection ps : planList) {
				if (mappedPlan.equals(ps.getBenefitPlan())) {
					list.add(ps);
					break;
				}
			}
		}

		for (String plan : planMap.get(visionPlanType)) {
			String mappedPlan = plan;
			if (!visionMatch) {
				// get Mapped plan
				mappedPlan = map.get(plan);
			}
			for (PlanSelection ps : planList) {
				if (mappedPlan.equals(ps.getBenefitPlan())) {
					list.add(ps);
					break;
				}
			}
		}

		if (exchangeService.isMedicalOffered(company.getRealmPlanYearId())) {
			boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
			if ( ! isPickChoose ) {
				List<String> alternateHcPlans = templateFundingDao.getAlternateHeadCountPlans(company,
						planMap.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE));
				planMap.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).addAll(alternateHcPlans);

			}
			for (String plan : planMap.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE)) {
				for (PlanSelection ps : planList) {
					if (plan.equals(ps.getBenefitPlan())) {
						list.add(ps);
						break;
					}
				}
			}
		}
		return list;
	}

	private void setPlanRatesOnContributions(Company company, List<PlanSelection> planList,
			Map<String, List<BenefitPlanRate>> planRates, Map<String,XbssRealmPlyrPlan> plyrPlanMap ) {

		for (PlanSelection ps : planList) {
			if (Constants.additionalPlanTypeList.contains(ps.getPlanType())) {
				continue;
			}

            Map<String, BigDecimal> costMap = null;
            costMap = StrategyUtils.getPlanCost(planRates.get(ps.getBenefitPlan()));

			List<Contribution> list = contributionService.getContributions(ps.getId());
			List<Contribution> newList = new ArrayList<>();
			for (Contribution contrib : list) {
				contrib.setPlanCost(costMap.get(CoverageCodesEnums.valueOfId(contrib.getCoverageLevel())));
				logger.debug("**** PLAN : " + ps.getBenefitPlan() + "\t Contribution Coverage Code : "
						+ contrib.getCoverageLevel() + "\t PLAN COST : " + contrib.getPlanCost());
				newList.add(contrib);
			}
			ps.setContributions(newList);
		}
	}


	private Map<String, List<PlanSelection>> getPlansByMDV(List<PlanSelection> planList, List<String> mandatoryPlansToExclude) {
		Map<String, List<PlanSelection>> psMap = new HashMap<>();

		for (PlanSelection ps : planList) {
			if (Constants.additionalPlanTypeList.contains(ps.getPlanType())) {
				continue;
			}
			List<PlanSelection> psList = null;
			if (ps.getPlanType().equals(Constants.MEDICAL_CODE)) {
				if (psMap.get(Constants.MEDICAL) != null) {
					psList = psMap.get(Constants.MEDICAL);
				} else {
					psList = new ArrayList<>();
				}

				if (!mandatoryPlansToExclude.contains(ps.getBenefitPlan())) {
					psList.add(ps);
				}
				psMap.put(Constants.MEDICAL, psList);
				logger.info("{} Plan List : {}", Constants.MEDICAL, ps.getBenefitPlan());

			}
			if (Constants.dentalPlanTypeList.contains(ps.getPlanType())) {
				if (psMap.get(Constants.DENTAL) != null) {
					psList = psMap.get(Constants.DENTAL);
				} else {
					psList = new ArrayList<>();
				}
				psList.add(ps);
				psMap.put(Constants.DENTAL, psList);
				logger.info("{} Plan List : {}", Constants.DENTAL, ps.getBenefitPlan());
			}
			if (Constants.visionPlanTypeList.contains(ps.getPlanType())) {
				if (psMap.get(Constants.VISION) != null) {
					psList = psMap.get(Constants.VISION);
				} else {
					psList = new ArrayList<>();
				}
				psList.add(ps);
				psMap.put(Constants.VISION, psList);
				logger.info("{} Plan List : {}", Constants.VISION, ps.getBenefitPlan());
			}
		}
		return psMap;

	}

	private void updateContributionHeadCount(Map<String, Map<String, Integer>> planHeadCountMap,
			List<PlanSelection> plans) {
		for (PlanSelection ps : plans) {

			List<Contribution> contributions = ps.getContributions();
			Map<String, Integer> headCountMap = planHeadCountMap.get(ps.getBenefitPlan());
			logger.debug("**** PLAN  : {}", ps.getBenefitPlan());

			for (Contribution contrib : contributions) {
				int headCount = headCountMap.get(CoverageCodesEnums.valueOfId(contrib.getCoverageLevel()));
				logger.info("BENEFIT PLAN : {}\t HEAD COUNT : {}\t COVERAGE CODE : {}", ps.getBenefitPlan(), headCount,
						contrib.getCoverageLevel());
				contrib.setHeadCount(headCount);
				if (ps.isHighDeductiblePlan()) {
					contrib.setHsaHeadCount(headCount);
				}

				contributionService.createUpdate(contrib);
			}
		}
	}

	private int getGroupHeadCount(HeadCountData headCountData) {
		Map<String, Integer> covrgMap = headCountData.getCovrgHeadCountMap();
		int total = 0;
		for (Entry<String, Integer> entry : covrgMap.entrySet()) {
			total += entry.getValue();
		}
		return total;
	}

	private void updateAdditionalBenefitPlansHeadCount(List<PlanSelection> planList, int headCount) {
		List<PlanSelection> additionalPlans = new ArrayList<>();

		for (PlanSelection ps : planList) {
			if (Constants.additionalPlanTypeList.contains(ps.getPlanType())) {
				ps.setHeadCount(headCount);
				additionalPlans.add(ps);
			}
		}
		planSelectionService.saveAll(additionalPlans);
	}

	private int getCompanyBenefitGroupsHeadCount(Company company) {
		List<BenefitGroup> list = benefitGroupService.getAllBenefitGroups(company.getId(), Constants.ACTIVE_STATUS);
		int totalHeadCount = 0;
		for (BenefitGroup group : list) {
			totalHeadCount += group.getHeadcount();
		}
		return totalHeadCount;
	}
}
