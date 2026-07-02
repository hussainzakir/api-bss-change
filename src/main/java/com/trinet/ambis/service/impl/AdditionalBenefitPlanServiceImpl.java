/**
 * 
 */
package com.trinet.ambis.service.impl;

import static com.trinet.ambis.helper.PlanCompareHelper.formatPlanCost;
import static com.trinet.ambis.helper.PlanCompareHelper.formatUnitRate;
import static com.trinet.ambis.util.Constants.LIFE_CMTR_PLANS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.trinet.ambis.enums.RateUnitsEnums;
import com.trinet.ambis.persistence.dao.hrp.EmployeeBenefitGroupDao;
import com.trinet.ambis.rest.controllers.dto.outputs.AdditionalBenefitGroupPlans;
import com.trinet.ambis.service.model.*;
import com.trinet.ambis.service.model.output.AdditionalBenefitPlanDto;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.response.CensusRes;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.AdditionalBenefitServiceHelper;
import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.ps.LifeAndDisabilityCalcData;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.outputs.AdditionalBenefitGroup;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparisonAdditonalBenefits;
import com.trinet.ambis.service.AdditionalBenefitPlanService;
import com.trinet.ambis.service.BenefitGroupService;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.DisabilityOptionService;
import com.trinet.ambis.service.HeadCountService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.prospect.ProspectPlanHeadCountService;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.Utils;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

/**
 * @author rvutukuri
 *
 */
@Service
public class AdditionalBenefitPlanServiceImpl implements AdditionalBenefitPlanService {
	private static final Logger logger = LoggerFactory.getLogger(AdditionalBenefitPlanServiceImpl.class);
	private static final int ROWS_PER_PAGE = 18;
	private static final int ROWS_PER_PAGE_LONG_SDI_LIST = 12;
	private static final int MAX_COUNT_FOR_SDI_STATE_ROW = 12;

	@Autowired
	private RealmDataDao realmDataDao;

	@Autowired
	private LifeAndDisabilityCalcData lifeAndDisabilityCalcData;

	@Autowired
	private DisabilityOptionService disabilityOptionService;

	@Autowired
	private StrategyDataDao strategyDataDao;

	@Autowired
	private BenefitGroupService benefitGroupService;

	@Autowired
	BenefitOfferExceptionService benOfferExceptionService;

	@Autowired
	HeadCountService headCountService;
	
	@Autowired
	RealmPlanYearService realmPlanYearService;

	@Autowired
	StrategyGroupDataDao strategyGroupDataDao;
	
	@Autowired
	ProspectPlanHeadCountService prospectPlanHeadCountService;

	@Autowired
	ProspectEmployeeService prospectEmployeeService;

	@Autowired
	EmployeeBenefitGroupDao employeeBenefitGroupDao;

	 //A global counter to track the number of rows processed for page breaks.
	 //This counter is shared between Disability and Life plan processing to ensure
     //consistent page break handling across both sections.
	AtomicInteger pageBreakCounter = new AtomicInteger(0);

	@Override
	public List<AdditionalBenefitPlanRates> getADBPlanCostByGroup(Company company, long strategyId) {
		List<AdditionalBenefitPlanRates> planRatesList = new ArrayList<>();
		Map<String, AdditionalBenefitsCategoryOffer> additionalBenefitOffersMap = null;
		List<AdditionalBenefitsCategoryOffer> additionalBenefitOffers = null;

		// getting all the AD plans by region and realm.
		Map<String, Set<StateBenefitPlan>> additionalBenefitsAllStatePlansMap = realmDataDao
				.getAdditionalBenefitsAllStatePlans(company.getRealmPlanYear().getId(),
						StrategyServiceHelper.getLocations(company), company);

		benOfferExceptionService.applyException(company, additionalBenefitsAllStatePlansMap);

		Map<String, Set<String>> adbPlanMap = AdditionalBenefitServiceHelper
				.getADBPlanListMapByType(additionalBenefitsAllStatePlansMap);

		// Get all PlanRates for realmYearId
		Map<String, BigDecimal> planEstCostMap = strategyDataDao
				.getAdditionalBenefitPlanEstCost(company.getRealmPlanYear().getId());

		// getting plan type description
		Map<String, PlanTypeDescription> planTypeDescMap = strategyDataDao
				.getPlanTypeDescriptions(company.getRealmPlanYear().getId());

		Set<AdditionalBenefitPlan> disabilityPlans = disabilityOptionService
				.getDisabilityOptionsByRealmPlanYear(company);

		// getting benefit offers valid for the company.
		Map<String, Boolean> selectedBenefits = realmDataDao.getSelectedBenefits(company.getRealmPlanYear().getId(),
				StrategyServiceHelper.getHqStateCity(company));
		
		if (company.isRenewalCompany() || company.isProspectConvertedClient() || company.isProspectCompany()) {
			// Get Company benefit groups
			List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(strategyId,
					BSSApplicationConstants.STATUS_ACTIVE);

			Map<String, Map<String, BigDecimal>> groupPlanCostMap = prepareGroupPlanCostMap(company, strategyId,
					adbPlanMap, benefitGroups);

			RealmPlanYear prevRealmYear = realmPlanYearService.getPreviousRealmPlanYear(company.getCode(),
					company.getRealmPlanYear().getId());

			// Get active employees count for additional benefits.
			Map<String, ActiveEligibleEECount> activeEligibleEmplCount = getActiveEligibleEmplCount(company, strategyId,
					prevRealmYear);
			for (var entry : groupPlanCostMap.entrySet()) {
				additionalBenefitOffersMap = new HashMap<>();
				additionalBenefitOffers = new ArrayList<>();
				BenefitGroup bg = this.getBenefitGroup(entry.getKey(), benefitGroups);
				AdditionalBenefitPlanRates apr = new AdditionalBenefitPlanRates();
				if (bg != null) {
					apr.setBenefitProgram(bg.getBenefitProgram());
					apr.setGroupId(bg.getId());
					apr.setGroupName(bg.getName());
					getLifeAndCommuterOffer(additionalBenefitsAllStatePlansMap, entry.getValue(), planEstCostMap,
							additionalBenefitOffersMap, planTypeDescMap, selectedBenefits, bg.getId());
					getDisabilityPlanCost(additionalBenefitsAllStatePlansMap, company, entry.getValue(),
							selectedBenefits, additionalBenefitOffersMap, bg.getId(), bg.getType(), true,
							activeEligibleEmplCount.get(entry.getKey()), prepareClone(disabilityPlans));
					additionalBenefitOffers.addAll(additionalBenefitOffersMap.values());
					apr.setAdditionalBenefitOffers(additionalBenefitOffers);
					planRatesList.add(apr);
				}
			}

		} else {
			Map<String, BigDecimal> planCostMap = calculateAdditionalPlansCost(company, false, adbPlanMap);
			int i = 0;
			while (i < BSSApplicationConstants.GROUP_TYPES.size()) {
				if (company.isK1Company() || !(BSSApplicationConstants.K1_GROUP_TYPE
						.equals(BSSApplicationConstants.GROUP_TYPES.get(i)))) {
					additionalBenefitOffersMap = new HashMap<>();
					additionalBenefitOffers = new ArrayList<>();
					AdditionalBenefitPlanRates apr = new AdditionalBenefitPlanRates();
					apr.setGroupId(i);
					apr.setGroupType(BSSApplicationConstants.GROUP_TYPES.get(i));
					getLifeAndCommuterOffer(additionalBenefitsAllStatePlansMap, planCostMap, planEstCostMap,
							additionalBenefitOffersMap, planTypeDescMap, selectedBenefits, apr.getGroupId());
					getDisabilityPlanCost(additionalBenefitsAllStatePlansMap, company, planCostMap, selectedBenefits,
							additionalBenefitOffersMap, apr.getGroupId(), apr.getGroupType(), false, null,
							prepareClone(disabilityPlans));
					additionalBenefitOffers.addAll(additionalBenefitOffersMap.values());
					apr.setAdditionalBenefitOffers(additionalBenefitOffers);
					planRatesList.add(apr);
				}
				i++;
			}
		}
		return planRatesList;
	}
	
	private Map<String, Map<String, BigDecimal>> prepareGroupPlanCostMap(Company company, long strategyId,
			Map<String, Set<String>> adbPlanMap, List<BenefitGroup> benefitGroups) {
		boolean isVendorMappingOn = RulesAndConfigsUtils.isVendorMappingOn(company.getRealmPlanYear().getId());

		Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = lifeAndDisabilityCalcData
				.getGroupEmployeeSelections(company, false, strategyId, isVendorMappingOn);

		Map<String, Map<String, BigDecimal>> groupPlanCostMap = calculateAdditionalPlansCostByGroup(company, false,
				adbPlanMap, employeeSelection);

		if (MapUtils.isEmpty(groupPlanCostMap)) {
			groupPlanCostMap = new HashMap<>();
			Map<String, BigDecimal> planCostMap = calculateAdditionalPlansCost(company, false, adbPlanMap);
			for (BenefitGroup bg : benefitGroups) {
				if (null == groupPlanCostMap.get(bg.getBenefitProgram())) {
					groupPlanCostMap.put(bg.getBenefitProgram(), planCostMap);
				}
			}
		}
		return groupPlanCostMap;
	}
	
	private Map<String, ActiveEligibleEECount> getActiveEligibleEmplCount(Company company, long strategyId,
			RealmPlanYear prevRealmYear) {
		Map<String, ActiveEligibleEECount> activeEligibleEmplCount;
		if (!company.isRenewalCompany()) {
			activeEligibleEmplCount = prospectPlanHeadCountService.getProspectEligibleEmployeeCount(company,
					strategyId);
		} else {
			activeEligibleEmplCount = headCountService.getEligibleEmployeeCount(company, strategyId, prevRealmYear,
					false);
		}
		return activeEligibleEmplCount;
	}
	
	@Override
	public Map<String, AdditionalPlanRate> getAdditionalPlansRate(Company company, boolean history,
			Map<String, Set<String>> adbPlanMap) {
		Map<String, AdditionalPlanRate> planCostMap = new HashMap<>();
		Date effDt = Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT);
		if (company.isRenewalCompany()) {
			if (history) {
				effDt = company.getRealmPlanYear().getPlanYearEnd();
			} else {
				effDt = company.getRealmPlanYear().getPlanYearStart();
			}
		}
		if (null != adbPlanMap.get(Constants.LTD_CODE)) {
			planCostMap.putAll(calculateAdditionalPlanRates(company, history, adbPlanMap, Constants.LTD_CODE, effDt,
					BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES, company.getBandCodes().getDisBandCode()));
		}
		if (null != adbPlanMap.get(Constants.STD_CODE)) {
			planCostMap.putAll(calculateAdditionalPlanRates(company, history, adbPlanMap, Constants.STD_CODE, effDt,
					BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES, company.getBandCodes().getDisBandCode()));
		}
		if (null != adbPlanMap.get(Constants.LIFE_CODE)) {
			planCostMap.putAll(calculateAdditionalPlanRates(company, history, adbPlanMap, Constants.LIFE_CODE, effDt,
					BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES, company.getBandCodes().getLifeBandCode()));
		}
		return planCostMap;
	}
	
	
	@Override
	public List<PlanComparisonAdditonalBenefits> getAdditionalBenefitsCompareInformation(Company company,
			long strategyId,List<String> templateNames) {

		List<BenefitGroup> benefitGroups = benefitGroupService.getBenefitGroupByStrategy(strategyId,
				BSSApplicationConstants.STATUS_ACTIVE);

		Map<String, String> benefitGroupNames = new HashMap<>();
		benefitGroups.stream().forEach(benefitGroup -> {
			benefitGroupNames.put(benefitGroup.getBenefitProgram(), benefitGroup.getName());
		});

		// Get the benefit programs that have employees associated with it.  We do not return data for empty groups
		List<String> benefitProgramsForStrategy = benefitGroupService.getBenefitProgramsForStrategy(company.getCode(), strategyId);

		//Get the additional benefit plans for the strategy by group
		Map<String, List<AdditionalBenefitPlanDto>> plansByGroup = strategyGroupDataDao
				.getAdditionalBenPlanSelections(strategyId, company.getRealmPlanYearId());

		// Remove any benefit programs that do not have any employees associated with it
		Iterator<Map.Entry<String, List<AdditionalBenefitPlanDto>>> planMapIterator = plansByGroup.entrySet().iterator();
		while (planMapIterator.hasNext()) {
			Map.Entry<String, List<AdditionalBenefitPlanDto>> entry = planMapIterator.next();
			if (!benefitProgramsForStrategy.contains(entry.getKey())) {
				planMapIterator.remove();
			}
		}

		if(!plansByGroup.isEmpty()) {
			List<AdditionalBenefitPlanDto> allGroupsAddPlanSelections = new ArrayList<>();
			for (List<AdditionalBenefitPlanDto> planSelections : plansByGroup.values()) {
				allGroupsAddPlanSelections.addAll(planSelections);
			}

			Map<String, Map<String, BigDecimal>> groupPlanCostMap = calculateLifeAndDisabilityPlansCost(company,
					allGroupsAddPlanSelections, strategyId);

			List<PlanComparisonAdditonalBenefits> compareDetails = new ArrayList<>();
			compareDetails.add(constructDisabilityComparisionDetails(company, strategyId, plansByGroup, groupPlanCostMap,
					benefitGroupNames,templateNames));
			compareDetails.add(constructLifeComparisionDetails(plansByGroup, groupPlanCostMap, benefitGroupNames,templateNames));

			logger.info("CompareDetails {}" , compareDetails);
			return compareDetails;
		}
		return Collections.emptyList();
	}

	/**
	 * This method returns a map of benefit program, plan and compare attributes for the disability plans.  The employee's state is used to determine if they
	 * should be enrolled in the SDI or non-SDI version of the STD plan.
	 * @param company
	 * @param strategyId
	 * @param plansByGroup
	 * @param groupPlanCostMap
	 * @return
	 */
	private Map<String, Map<String, List<String>>> getDisabilityPlanCostAttributesByGroupAndPlan(Company company, Long strategyId,
																								 Map<String, List<AdditionalBenefitPlanDto>> plansByGroup,
																								 Map<String, Map<String, BigDecimal>> groupPlanCostMap,
																								 MutableBoolean isEnrollmentInSdiPlan) {
		List<CensusRes> prospectEmployees = prospectEmployeeService.getEmployees(company.getCode());

		Map<String, EmployeeStrategyGroupDetails> trinetEmployeesByEmplId = employeeBenefitGroupDao
				.getEmployeeDetailsByStrategy(strategyId);

		// Get a map of triNetEmployee.getFutureGroupId() and List<employeeId>
		Map<Long, List<String>> employeeIdsByGroupId = trinetEmployeesByEmplId.values().stream()
				.collect(Collectors.groupingBy(EmployeeStrategyGroupDetails::getFutureGroupId,
						Collectors.mapping(EmployeeStrategyGroupDetails::getEmplId, Collectors.toList())));


		Map<String, Map<String, List<String>>> disabilityPlanCostAttributesByGroupAndPlan = new HashMap<>();
		List<String> processedPlans;
		for (CensusRes prospectEmployee : prospectEmployees) {
			processedPlans = new ArrayList<>();
			String emplId = prospectEmployee.getEmployeeId();
			String homeState = prospectEmployee.getHomeState();
			EmployeeStrategyGroupDetails triNetEmployee = trinetEmployeesByEmplId.get(emplId);
			if (triNetEmployee != null) {
				Long groupId = triNetEmployee.getFutureGroupId();
				String benefitProgram = triNetEmployee.getFutureBenefitProgram();
				boolean sdiEmpoyee = company.getSdiStates().contains(homeState);

				Map<String, BigDecimal> groupPlanCosts = groupPlanCostMap.get(benefitProgram);
				List<AdditionalBenefitPlanDto> groupPlans = plansByGroup.get(benefitProgram);
				boolean sdiPlanExists = groupPlans.stream().anyMatch(AdditionalBenefitPlanDto::isSdiPlan);

				for (AdditionalBenefitPlanDto groupPlan : groupPlans) {
					if (processedPlans.contains(groupPlan.getBenefitPlan())) {
						continue;
					}
					BigDecimal estimatedPayrollCoverage = calculateEstimatedPayrollCoverage(employeeIdsByGroupId,
							groupId, groupPlanCosts, groupPlan);
					if (BSSApplicationConstants.STD_CODE.equals(groupPlan.getPlanType())) {
						// If this is TNIV,
						// If the plan is an SDI plan and the employee is an SDI employee, add the plan to the list
						// If the plan is not an SDI plan, add the plan to the list
						if (BenExchngEnums.TRINET_IV.getBenExchng().equals(company.getRealm().getBenExchange())
								&& company.getRealmPlanYearId() < 86) {
							if ((groupPlan.isSdiPlan() && sdiEmpoyee) || !groupPlan.isSdiPlan()) {
								if (groupPlan.isSdiPlan()) {
									isEnrollmentInSdiPlan.setValue(true);
								}
								addDisabilityPlanCostAttributesByGroupAndPlan(benefitProgram, groupPlan.getBenefitPlan(),
										estimatedPayrollCoverage, groupPlanCosts.get(groupPlan.getBenefitPlan()), disabilityPlanCostAttributesByGroupAndPlan);
							}
						}
						else if (!sdiPlanExists || groupPlan.isSdiPlan() == sdiEmpoyee) {
							if (groupPlan.isSdiPlan()) {
								isEnrollmentInSdiPlan.setValue(true);
							}
							addDisabilityPlanCostAttributesByGroupAndPlan(benefitProgram, groupPlan.getBenefitPlan(), estimatedPayrollCoverage, groupPlanCosts.get(groupPlan.getBenefitPlan()), disabilityPlanCostAttributesByGroupAndPlan);
						}
					} else {
						addDisabilityPlanCostAttributesByGroupAndPlan(benefitProgram, groupPlan.getBenefitPlan(), estimatedPayrollCoverage, groupPlanCosts.get(groupPlan.getBenefitPlan()), disabilityPlanCostAttributesByGroupAndPlan);
					}
					processedPlans.add(groupPlan.getBenefitPlan());
				}
			} else {
				logger.error("ProspectStrategyIntegrationService: Employee {} not found in TriNet", emplId);
			}
		}
		return disabilityPlanCostAttributesByGroupAndPlan;
	}

	private BigDecimal calculateEstimatedPayrollCoverage(Map<Long, List<String>> employeeIdsByGroupId, Long groupId,
			Map<String, BigDecimal> groupPlanCosts, AdditionalBenefitPlanDto groupPlan) {
		BigDecimal estimatedPayrollCoverage = BigDecimal.ZERO;
		if (groupPlanCosts.get(groupPlan.getBenefitPlan() + ProspectConstants.EST_PYRL_CVG) != null) {
			estimatedPayrollCoverage = groupPlanCosts.get(groupPlan.getBenefitPlan() + ProspectConstants.EST_PYRL_CVG)
					.divide(BigDecimal.valueOf(employeeIdsByGroupId.get(groupId).size()), 10, RoundingMode.HALF_UP);
		}
		return estimatedPayrollCoverage;
	}
	
	private void addDisabilityPlanCostAttributesByGroupAndPlan(String benefitProgram, String planId, BigDecimal payrollCoverage,
						BigDecimal planCost, Map<String, Map<String, List<String>>> disabilityPlanCostAttributesByGroupAndPlan ) {
		if (disabilityPlanCostAttributesByGroupAndPlan.containsKey(benefitProgram)) {
			Map<String, List<String>> planCostAttributesByPlan = disabilityPlanCostAttributesByGroupAndPlan.get(benefitProgram);
			if (planCostAttributesByPlan.containsKey(planId)) {
				List<String> planCostAttributes = planCostAttributesByPlan.get(planId);
				Integer numberOfEes = Integer.valueOf(planCostAttributes.get(0)) + 1;
				BigDecimal totalPayrollCoverage = new BigDecimal(planCostAttributes.get(1)).add(payrollCoverage);
				BigDecimal totalPlanCost = new BigDecimal(planCostAttributes.get(2)).add(planCost);
				planCostAttributes.set(0, numberOfEes.toString());
				planCostAttributes.set(1, totalPayrollCoverage.toString());
				planCostAttributes.set(2, totalPlanCost.toString());
			} else {
				List<String> planCostAttributes = new ArrayList<>();
				planCostAttributes.add("1");
				planCostAttributes.add(payrollCoverage.toString());
				planCostAttributes.add(planCost.toString());
				planCostAttributesByPlan.put(planId, planCostAttributes);
			}
		} else {
			Map<String, List<String>> planCostAttributesByPlan = new HashMap<>();
			List<String> planCostAttributes = new ArrayList<>();
			planCostAttributes.add("1");
			planCostAttributes.add(payrollCoverage.toString());
			planCostAttributes.add(planCost.toString());
			planCostAttributesByPlan.put(planId, planCostAttributes);
			disabilityPlanCostAttributesByGroupAndPlan.put(benefitProgram, planCostAttributesByPlan);
		}
	}
	
	private Map<String, Map<String, BigDecimal>> calculateLifeAndDisabilityPlansCost(Company company,
																					 List<AdditionalBenefitPlanDto> adPlanSelections, long strategyId) {
		Map<String, Set<String>> adbPlanMap = AdditionalBenefitServiceHelper.getAdditionalBenefitPlanListMapByType(adPlanSelections);
		Map<String, AdditionalBenefitEmployeeDetails> employeeSelection = lifeAndDisabilityCalcData
				.getGroupEmployeeSelections(company, false, strategyId, true);
		Map<String, Map<String, BigDecimal>> groupPlanCostMap = new HashMap<>();
		if (null != employeeSelection && !employeeSelection.isEmpty()) {
			groupPlanCostMap = calculateAdditionalPlansCostByGroup(company, false, adbPlanMap, employeeSelection);
		}
		if (MapUtils.isEmpty(groupPlanCostMap)) {
			groupPlanCostMap = new HashMap<>();
			Map<String, BigDecimal> planCostMap = calculateAdditionalPlansCost(company, false, adbPlanMap);
			for (var entry : groupPlanCostMap.entrySet()) {
				groupPlanCostMap.put(entry.getKey(), planCostMap);
			}
		}
		return groupPlanCostMap;
	}

	private PlanComparisonAdditonalBenefits constructLifeComparisionDetails(
			Map<String, List<AdditionalBenefitPlanDto>> plansByGroup,
			Map<String, Map<String, BigDecimal>> groupPlanCostMap, Map<String, String> benefitGroupNames,List<String> templateNames) {

		List<String> attributeNames = new ArrayList<>();
		attributeNames.add(ProspectConstants.PLAN_NAME);
		attributeNames.add(ProspectConstants.UNIT_RATE);
		attributeNames.add(ProspectConstants.UNIT);
        attributeNames.add(ProspectConstants.EMPLOYEE_COUNT);
		attributeNames.add(ProspectConstants.EST_PYRL_CVG);
		attributeNames.add(ProspectConstants.MONTHLY_COST);
		PlanComparisonAdditonalBenefits lifeCompare = new PlanComparisonAdditonalBenefits();

		// Filter to only Life plans
		Map<String, List<AdditionalBenefitPlanDto>> lifePlansByGroup =
				plansByGroup.entrySet().stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								entry -> entry.getValue().stream()
										.filter(plan -> BSSApplicationConstants.LIFE_CODE.equals(plan.getPlanType()))
										.collect(Collectors.toList())
						));

		// Selected plans
		Map<String, List<AdditionalBenefitPlanDto>> selectedPlansByGroup =
				lifePlansByGroup.entrySet().stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								entry -> entry.getValue().stream()
										.filter(plan -> plan.isSelected())
										.collect(Collectors.toList())
						));

		if(templateNames.contains(ProspectConstants.PLAN_COMPARISON)) {
			List<AdditionalBenefitGroup> selectedGroupDetails = constructLifeGroupDetails(selectedPlansByGroup, groupPlanCostMap, benefitGroupNames);
			lifeCompare.setSelectedGroupDetails(selectedGroupDetails);
		}
		if(templateNames.contains(ProspectConstants.PLAN_APPENDIX)) {
			List<AdditionalBenefitGroup> availableGroupDetails = constructLifeGroupDetails(lifePlansByGroup, groupPlanCostMap, benefitGroupNames);
			lifeCompare.setAvailableGroupDetails(availableGroupDetails);
		}
		lifeCompare.setAttributeNames(attributeNames);
		lifeCompare.setBenefitType("LIFE");
		return lifeCompare;
	}

	private List<AdditionalBenefitGroup> constructLifeGroupDetails(Map<String, List<AdditionalBenefitPlanDto>> plansByGroup,
																		 Map<String, Map<String, BigDecimal>> groupPlanCostMap,
																		 Map<String, String> benefitGroupNames) {

		List<AdditionalBenefitGroup> groupDetails = new ArrayList<>();
		int reportRowCount = pageBreakCounter.get();
		for (var entry : plansByGroup.entrySet()) {
			List<List<String>> attributeValues = new ArrayList<>();
			AdditionalBenefitGroup bg = new AdditionalBenefitGroup();
			bg.setGroupName(benefitGroupNames.get(entry.getKey()));
			bg.setPlans(new ArrayList<>());
			reportRowCount++;
			Map<String, BigDecimal> planCostMap = groupPlanCostMap.get(entry.getKey());
			AdditionalBenefitGroupPlans plans = new AdditionalBenefitGroupPlans();
			for (AdditionalBenefitPlanDto ps : entry.getValue()) {
				reportRowCount++;
				List<String> attributes = buildLifeGroupAttributes(planCostMap, ps);
				attributeValues.add(attributes);
			}
			if (reportRowCount > ROWS_PER_PAGE) {
				plans.setPageBreak(true);
				reportRowCount = entry.getValue().size() + 1;
			}
			plans.setAttributeValues(attributeValues);
			bg.getPlans().add(plans);
			groupDetails.add(bg);
		}
		//Reset the global page break counter after processing all groups of PLAN-COMPARISON.
		pageBreakCounter.set(0);
		return groupDetails;
	}

	private List<String> buildLifeGroupAttributes(Map<String, BigDecimal> planCostMap, AdditionalBenefitPlanDto ps) {
		List<String> attributes = new ArrayList<>();
		attributes.add(ps.getName());
		attributes.add(formatUnitRate().apply(planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT_RATE).toString()));
		if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT) == null) {
			attributes.add("Covered employee");
		}
		else if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT).equals(new BigDecimal(100))) {
			attributes.add(RateUnitsEnums.PER_HUNDRED.getDescription());
		} else {
			attributes.add(RateUnitsEnums.PER_THOUSAND.getDescription());
		}
		if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.GROUP_HEADCOUNT) == null) {
		    attributes.add(ProspectConstants.DOUBLE_DASH);
		} else {
		    attributes.add(planCostMap.get(ps.getBenefitPlan() + ProspectConstants.GROUP_HEADCOUNT) + "");
		}
		if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.EST_PYRL_CVG) == null) {
			attributes.add(ProspectConstants.DOUBLE_DASH);
		} else {
			attributes.add(formatPlanCost().apply(planCostMap.get(ps.getBenefitPlan() + ProspectConstants.EST_PYRL_CVG) + ""));
		}
		BigDecimal monthlyCost;
		if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.GROUP_HEADCOUNT) != null && planCostMap.get(ps.getBenefitPlan()) != null) {
			monthlyCost = planCostMap.get(ps.getBenefitPlan())
					.multiply(planCostMap.get(ps.getBenefitPlan() + ProspectConstants.GROUP_HEADCOUNT));
		} else {
			monthlyCost = planCostMap.get(ps.getBenefitPlan() + ProspectConstants.MONTHLY_COST);
		}
		attributes.add(formatPlanCost().apply(monthlyCost + ""));
		return attributes;
	}

	private PlanComparisonAdditonalBenefits constructDisabilityComparisionDetails(
			Company company, Long strategyId,
			Map<String, List<AdditionalBenefitPlanDto>> plansByGroup,
			Map<String, Map<String, BigDecimal>> groupPlanCostMap,
			Map<String, String> benefitGroupNames, List<String> templateNames) {

		List<String> attributeNames = new ArrayList<>();
		attributeNames.add(ProspectConstants.DISABILITY_TYPE);
		attributeNames.add(ProspectConstants.DIS_PLAN_NAME);
		attributeNames.add(ProspectConstants.STATES);
		attributeNames.add(ProspectConstants.UNIT_RATE);
		attributeNames.add(ProspectConstants.UNIT);
		attributeNames.add(ProspectConstants.EMPLOYEE_COUNT);
		attributeNames.add(ProspectConstants.DIS_EST_PYRL_CVG);
		attributeNames.add(ProspectConstants.MONTHLY_COST);
		PlanComparisonAdditonalBenefits disabilityCompare = new PlanComparisonAdditonalBenefits();

		// Filter to only Disability plans
		Map<String, List<AdditionalBenefitPlanDto>> disabilityPlansByGroup =
				plansByGroup.entrySet().stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								entry -> entry.getValue().stream()
										.filter(plan -> BSSApplicationConstants.DISABILITY_PLAN_TYPES.contains(plan.getPlanType()))
										.collect(Collectors.toList())
						));

		// Selected plans
		Map<String, List<AdditionalBenefitPlanDto>> selectedPlansByGroup =
				disabilityPlansByGroup.entrySet().stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								entry -> entry.getValue().stream()
										.filter(plan -> plan.isSelected())
										.collect(Collectors.toList())
						));

		MutableBoolean isEnrollmentInSdiPlan = new MutableBoolean(false);
		Map<String, Map<String, List<String>>> availableDisabilityPlanCostAttributesByGroupAndPlan = getDisabilityPlanCostAttributesByGroupAndPlan(company, strategyId, disabilityPlansByGroup, groupPlanCostMap, isEnrollmentInSdiPlan);

		if(templateNames.contains(ProspectConstants.PLAN_COMPARISON)) {
			List<AdditionalBenefitGroup> selectedGroupDetails = constructDisabilityGroupDetails(selectedPlansByGroup, groupPlanCostMap, company.getRealm().getBenExchange(), company.getRealmPlanYearId(), benefitGroupNames, availableDisabilityPlanCostAttributesByGroupAndPlan, isEnrollmentInSdiPlan,ProspectConstants.PLAN_COMPARISON);
			disabilityCompare.setSelectedGroupDetails(selectedGroupDetails);
		}
		if(templateNames.contains(ProspectConstants.PLAN_APPENDIX)) {
			List<AdditionalBenefitGroup> availableGroupDetails = constructDisabilityGroupDetails(disabilityPlansByGroup, groupPlanCostMap, company.getRealm().getBenExchange(), company.getRealmPlanYearId(), benefitGroupNames, availableDisabilityPlanCostAttributesByGroupAndPlan, isEnrollmentInSdiPlan,ProspectConstants.PLAN_APPENDIX);
			disabilityCompare.setAvailableGroupDetails(availableGroupDetails);
		}
		disabilityCompare.setAttributeNames(attributeNames);
		disabilityCompare.setBenefitType("DISABILITY");
		return disabilityCompare;
	}

	private List<AdditionalBenefitGroup> constructDisabilityGroupDetails(Map<String, List<AdditionalBenefitPlanDto>> plansByGroup,
																		 Map<String, Map<String, BigDecimal>> groupPlanCostMap, String exchange, long realmPlanYrId,
																		 Map<String, String> benefitGroupNames, Map<String, Map<String, List<String>>> disabilityPlanCostAttributesByGroupAndPlan,
																		 MutableBoolean isEnrollmentInSdiPlan,String templateType) {

		List<String> sdiStates = new ArrayList<>();
		sdiStates.addAll(RulesAndConfigsUtils.getSDIStates(realmPlanYrId));
		Collections.sort(sdiStates);
		int rowsPerPage = ROWS_PER_PAGE;
		if (sdiStates.size() > MAX_COUNT_FOR_SDI_STATE_ROW && isEnrollmentInSdiPlan.isTrue()) {
			rowsPerPage = ROWS_PER_PAGE_LONG_SDI_LIST;
		}

		List<AdditionalBenefitGroup> groupDetails = new ArrayList<>();
		int reportRowCount = 0;
		int bundleRowCount = 0;
		for (var entry : plansByGroup.entrySet()) {
			BigDecimal totalCost = BigDecimal.ZERO;
			List<List<String>> attributeValues = new ArrayList<>();
			AdditionalBenefitGroup bg = new AdditionalBenefitGroup();
			bg.setGroupName(benefitGroupNames.get(entry.getKey()));
			bg.setPlans(new ArrayList<>());
			Map<String, BigDecimal> planCostMap = groupPlanCostMap.get(entry.getKey());
			AdditionalBenefitGroupPlans plans = new AdditionalBenefitGroupPlans();
			String previousBundleId = "";
			String previousBundleName = "";
			for (AdditionalBenefitPlanDto ps : entry.getValue()) {
				String bundleId = ps.getBundleId();
				// If the bundleId changes, add the previous bundle to the group details
				if (!bundleId.equals(previousBundleId) && !previousBundleId.isEmpty()) {
					// Increase both by one to account for the bundle header row
					reportRowCount++;
					bundleRowCount++;
					if (reportRowCount > rowsPerPage) {
						plans.setPageBreak(true);
						reportRowCount = bundleRowCount;
					}
					plans.setBundleName(previousBundleName);
					plans.setTotalCost(formatPlanCost().apply(totalCost.toString()));
					plans.setAttributeValues(attributeValues);
					bg.getPlans().add(plans);
					bundleRowCount = 0;
					totalCost = BigDecimal.ZERO;
					attributeValues = new ArrayList<>();
					plans = new AdditionalBenefitGroupPlans();
				}

				previousBundleId = bundleId;
				previousBundleName = ps.getBundleName();

				// If this is an employee paid plan, skip it
				// If this plan does not have cost attributes, skip it
				// If this plan does have cost attributes and the employee count is 0, skip it
				if (ps.isEmployeePaid()
						|| !disabilityPlanCostAttributesByGroupAndPlan.containsKey(entry.getKey())
						|| !disabilityPlanCostAttributesByGroupAndPlan.get(entry.getKey()).containsKey(ps.getBenefitPlan())
						|| disabilityPlanCostAttributesByGroupAndPlan.get(entry.getKey()).get(ps.getBenefitPlan()).get(0).equals("0")) {
					continue;
				}

				reportRowCount++;
				bundleRowCount++;
				List<String> attributes = new ArrayList<>();
				String disabilityCode = ps.getPlanType().equals(BSSApplicationConstants.STD_CODE)
						? BSSApplicationConstants.STD
						: BSSApplicationConstants.LTD;
				attributes.add(disabilityCode);
				attributes.add(ps.getName());
				attributes.add(getStatesforDisabilityPlan(exchange, ps.isSdiPlan(), ps.getPlanType(), sdiStates,
						realmPlanYrId));
				attributes.add(formatUnitRate().apply(planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT_RATE).toString()));
				if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT) == null) {
					attributes.add("Covered employee");
				}
				else if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT).equals(new BigDecimal(100))) {
					attributes.add(RateUnitsEnums.PER_HUNDRED.getDescription());
				} else {
					attributes.add(RateUnitsEnums.PER_THOUSAND.getDescription());
				}

				if (disabilityPlanCostAttributesByGroupAndPlan.containsKey(entry.getKey())
						&& disabilityPlanCostAttributesByGroupAndPlan.get(entry.getKey()).containsKey(ps.getBenefitPlan())
				) {
					List<String> planCostAttributes = disabilityPlanCostAttributesByGroupAndPlan.get(entry.getKey()).get(ps.getBenefitPlan());
					attributes.add(planCostAttributes.get(0));
					if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT) == null) {
						attributes.add(ProspectConstants.DOUBLE_DASH);
					} else {
						BigDecimal estimatedPayrollCoverage = new BigDecimal(planCostAttributes.get(1)).setScale(2, RoundingMode.HALF_UP);
						attributes.add(formatPlanCost().apply(estimatedPayrollCoverage.toString()));
					}
					totalCost = totalCost.add(new BigDecimal(planCostAttributes.get(2)));
					attributes.add(formatPlanCost().apply(planCostAttributes.get(2)));
				} else {
					attributes.add("0");
					if (planCostMap.get(ps.getBenefitPlan() + ProspectConstants.UNIT) == null) {
						attributes.add(ProspectConstants.DOUBLE_DASH);
					} else {
						attributes.add(formatPlanCost().apply("0"));
					}
					attributes.add(formatPlanCost().apply("0"));
				}

				attributeValues.add(attributes);
			}
			// Add the last bundle to the group details
			// Increase both by one to account for the bundle header row
			reportRowCount++;
			bundleRowCount++;
			if (reportRowCount > rowsPerPage) {
				plans.setPageBreak(true);
				reportRowCount = bundleRowCount;
			}
			bundleRowCount = 0;
			plans.setBundleName(previousBundleName);
			plans.setTotalCost(formatPlanCost().apply(totalCost.toString()));
			plans.setAttributeValues(attributeValues);
			bg.getPlans().add(plans);
			groupDetails.add(bg);
		}
	    // If both conditions are true, set the global pageBreakCounter to the current reportRowCount.
	    // This ensures that the page break logic is applied consistently across Life and Disability plans.
		if(AppRulesAndConfigsUtils.isLifeAndDiPageBreakEnabled()
				&& templateType.equalsIgnoreCase(ProspectConstants.PLAN_COMPARISON)) {
			pageBreakCounter.set(reportRowCount);
		}

		return groupDetails;
	}

	private String getStatesforDisabilityPlan(String exchange, boolean isSdiPlan, String planType,
			List<String> sdiStates, long realmYearId) {
		String stdSdiStateText = String.join(", ", sdiStates);
		String stdNonSdiStateText = "Other States not in " + stdSdiStateText;

		String disabilityStates = "All States";
		if ((BSSApplicationConstants.STD_CODE).equals(planType)) {
			if (BenExchngEnums.TRINET_IV.getBenExchng().equals(exchange) && realmYearId < 86) {
				if (isSdiPlan) {
					disabilityStates = stdSdiStateText;
				}
			} else {
				if (isSdiPlan) {
					disabilityStates = stdSdiStateText;
				} else {
					disabilityStates = stdNonSdiStateText;
				}
			}
		}
		return disabilityStates;
	}
	
	private Map<String, AdditionalPlanRate> calculateAdditionalPlanRates(Company company, boolean history,
			Map<String, Set<String>> adbPlanMap, String planType, Date effDt, String propertiesType, String bandCode) {

		Map<String, AdditionalPlanRate> planCostMap;
		Map<String, FormulaProperties> formulaProps = null;

		Set<String> plans = adbPlanMap.get(planType);
		formulaProps = lifeAndDisabilityCalcData.getFormulaProperties(plans, effDt, propertiesType);
		Map<String, RateProperties> map = lifeAndDisabilityCalcData.getRateProperties(
				company.getRealmPlanYear().getCloneProgram(), effDt, plans, bandCode,
				company.getQuater());
		Map<String, List<AdditionalPlanRate>> rates = getPlanRates(company, map, history);
		planCostMap = getBasePlanRates(formulaProps, map, rates);

		return planCostMap;
	}
	
	
	private Map<String, List<AdditionalPlanRate>> getPlanRates(Company company, Map<String, RateProperties> map,
			boolean history) {
		Set<String> rateIds = new TreeSet<>();
		for (Map.Entry<String, RateProperties> entry : map.entrySet()) {
			if (entry.getValue() != null && entry.getValue().getRateTblID() != null) {
				rateIds.add(entry.getValue().getRateTblID());
				logger.debug("PLAN TYPE : {}\t PLAN : {}\t RATE TABLE ID : {}"  , entry.getValue().getPlanType(), entry.getValue().getBenefitPlan(),entry.getValue().getRateTblID());
			} else {
				logger.error("RateTbl ID is not set properly for company : {}\t FOR PLAN : {}\t BEN PROG {}\t PLAN TYPE : {}\t RATE TYPE : {}"
						, company.getCode(),entry.getValue().getBenefitPlan(),entry.getValue().getBenProg(),entry.getValue().getPlanType(),entry.getValue().getRateType());
			}
		}
		Map<String, List<AdditionalPlanRate>> planRates = null;
		if (!rateIds.isEmpty()) {
			if (company.isRenewalCompany() || company.isProspectCompany() || company.isProspectConvertedClient()) {
				if (history) {
					planRates = lifeAndDisabilityCalcData.getPlanRates(rateIds,
							company.getRealmPlanYear().getPlanYearEnd());
				} else {
					planRates = lifeAndDisabilityCalcData.getPlanRates(rateIds,
							company.getRealmPlanYear().getPlanYearStart());
				}

			} else {
				planRates = lifeAndDisabilityCalcData.getPlanRates(rateIds,
						Utils.convertStringToDate(company.getPlanStartDate(), Constants.DATE_FORMAT));
			}
		}
		return planRates;
	}

	private Map<String, BigDecimal> calculatePlanCostForNewClient(Company company,
			Map<String, FormulaProperties> formulaProps, Map<String, RateProperties> map,
			Map<String, BigDecimal> planCostMap, String planType,
			Map<String, List<AdditionalPlanRate>> rates) {

		// loop through each entry of a Map of BENEFIT_PLAN to formula properties
		for (Map.Entry<String, FormulaProperties> entry : formulaProps.entrySet()) {
			BigDecimal finalCost = BigDecimal.ZERO;
			RateProperties rp = map.get(entry.getKey());
			if (rp.getRateTblID() != null && rp.getRatePerUnit().equals("NONE")) {
				if (rp.getRateType() == 2) {
					finalCost = rates.get(rp.getRateTblID()).get(0).getRate().setScale(2, RoundingMode.HALF_UP);
				}
				planCostMap.put(entry.getKey(), finalCost);

			} else {
				// get the formula definition entries and attach them to the formula properties object
				List<FormulaDefinition> formDefs = lifeAndDisabilityCalcData.getFormulaDefinition(entry.getValue().getFormulaID(),
						entry.getValue().getFormulaEffDt());
				entry.getValue().setFormulaDefs( formDefs );

				// calculate the coverage amount
				BigDecimal salaryBase = company.getRealmPlanYear().getAvgSalary();
				BigDecimal coverageAmt = calculateCoverageAmount(planType, entry, salaryBase);
				BigDecimal planRate = calculatePlanRate(rates, rp);
				finalCost = calculateFinalCost(finalCost, rp, coverageAmt, planRate);

				planCostMap.put(entry.getKey(), finalCost);
			}
		}
		return planCostMap;
	}
	
	private BigDecimal calculateCoverageAmount(String planType, Map.Entry<String, FormulaProperties> entry,
			BigDecimal salaryBase) {
		BigDecimal coverageAmt;

		// calculate the coverage amount
		if( BSSApplicationConstants.LIFE_CODE.equals( planType ) ) {
			coverageAmt = entry.getValue().calculateLifeADDCoverage( salaryBase );
		} else if( BSSApplicationConstants.DISABILITY_PLAN_TYPES.contains( planType )) {
			coverageAmt = entry.getValue().calculateDisabilityCoverage( salaryBase );
		} else {
			// this should never happen; behaviour is undefined
			coverageAmt = BigDecimal.ZERO;
		}
		return coverageAmt;
	}
	
	private BigDecimal calculatePlanRate(Map<String, List<AdditionalPlanRate>> rates, RateProperties rp) {
		BigDecimal planRate = BigDecimal.ZERO;
		if (rp.getRateTblID() != null && rp.getRateType() == 1) {
			List<AdditionalPlanRate> list = rates.get(rp.getRateTblID());
			for (AdditionalPlanRate rate : list) {
				if (rate.getAge() == 40) {
					planRate = rate.getRate();
					break;
				}
			}
		} else if (rp.getRateTblID() != null && rp.getRateType() == 2) {
			planRate = rates.get(rp.getRateTblID()).get(0).getRate();
		}
		return planRate;
	}
	
	private BigDecimal calculateFinalCost(BigDecimal finalCost, RateProperties rp, BigDecimal coverageAmt,
			BigDecimal planCost) {
		BigDecimal roundTo = null;
		if (planCost.compareTo(BigDecimal.ZERO) > 0 && null != coverageAmt) {
			if (rp.getRatePerUnit().equals("PHUN")) {
				roundTo = coverageAmt.divide(new BigDecimal(100));
				finalCost = planCost.multiply(roundTo).setScale(2, RoundingMode.HALF_UP);
			} else if (rp.getRatePerUnit().equals("PTHO")) {
				roundTo = coverageAmt.divide(new BigDecimal(1000));
				finalCost = planCost.multiply(roundTo).setScale(2, RoundingMode.HALF_UP);
			}
		}
		return finalCost;
	}
	
	private Map<String, AdditionalPlanRate> getBasePlanRates(Map<String, FormulaProperties> formulaProps,
			Map<String, RateProperties> map, Map<String, List<AdditionalPlanRate>> rates) {

		Map<String, AdditionalPlanRate> planCostMap = new HashMap<>();

		for (Map.Entry<String, FormulaProperties> entry : formulaProps.entrySet()) {
			AdditionalPlanRate finalPlanRate = new AdditionalPlanRate();
			BigDecimal planRate = BigDecimal.ZERO;
			RateProperties rp = map.get(entry.getKey());
			if (rp != null) {
				finalPlanRate.setRateTblId(rp.getRateTblID());
				finalPlanRate.setUnit(rp.getRatePerUnit());

				if (rp.getRateTblID() != null && rp.getRatePerUnit().equals("NONE")) {
					if (rp.getRateType() == 2) {
						planRate = rates.get(rp.getRateTblID()).get(0).getRate().setScale(2, RoundingMode.HALF_UP);
					}
					finalPlanRate.setRate(planRate);
					planCostMap.put(entry.getKey(), finalPlanRate);

				} else {
					if (rp.getRateTblID() != null && rp.getRateType() == 1) {
						List<AdditionalPlanRate> list = rates.get(rp.getRateTblID());
						for (AdditionalPlanRate rate : list) {
							if (rate.getAge() == 40) {
								planRate = rate.getRate();
								break;
							}
						}
					} else if (rp.getRateTblID() != null && rp.getRateType() == 2) {
						planRate = rates.get(rp.getRateTblID()).get(0).getRate();
					}
				}
				finalPlanRate.setRate(planRate);
			} else {
				finalPlanRate = null;
			}
			planCostMap.put(entry.getKey(), finalPlanRate);
		}
		return planCostMap;
	}

	private void getLifeAndCommuterOffer(Map<String, Set<StateBenefitPlan>> additionalBenefitsAllStatePlansMap,
			Map<String, BigDecimal> planCostMap, Map<String, BigDecimal> planEstCostMap,
			Map<String, AdditionalBenefitsCategoryOffer> additionalBenefitOffersMap,
			Map<String, PlanTypeDescription> planTypeDescMap, Map<String, Boolean> selectedBenefits, long groupId) {
		for (Map.Entry<String, Set<StateBenefitPlan>> entry : additionalBenefitsAllStatePlansMap.entrySet()) {
			if (LIFE_CMTR_PLANS.contains(entry.getKey())) {
				AdditionalBenefitPlan adBenefitPlan = null;
				for (StateBenefitPlan statePlan : entry.getValue()) {
					adBenefitPlan = createAdditionalBenefitPlan(planCostMap, planEstCostMap, statePlan);
					updateAdditionalBenefitOffersMap(additionalBenefitOffersMap, planTypeDescMap, selectedBenefits,
							groupId, entry, adBenefitPlan);
				}
			}
		}
	}
	
	private AdditionalBenefitPlan createAdditionalBenefitPlan(Map<String, BigDecimal> planCostMap,
			Map<String, BigDecimal> planEstCostMap, StateBenefitPlan statePlan) {
		AdditionalBenefitPlan adBenefitPlan;
		adBenefitPlan = new AdditionalBenefitPlan();
		adBenefitPlan.setId(statePlan.getBenefitPlan());
		adBenefitPlan.setDescription(statePlan.getDescription());
		adBenefitPlan.setDisplaySeq( statePlan.getDisplaySeq() );
		adBenefitPlan.setPlanType(statePlan.getPlanType());
		if (Constants.LIFE_CODE.equals(adBenefitPlan.getPlanType())) {
			adBenefitPlan.setPlanCost(planCostMap.get(adBenefitPlan.getId()));
			adBenefitPlan.setAnnualCap(planEstCostMap.get(adBenefitPlan.getId()));
		} else if (Constants.COMMUTER_CODE.equals(adBenefitPlan.getPlanType())) {
			adBenefitPlan.setPlanCost(planEstCostMap.get(adBenefitPlan.getId()));
		}
		return adBenefitPlan;
	}
	
	private void updateAdditionalBenefitOffersMap(
			Map<String, AdditionalBenefitsCategoryOffer> additionalBenefitOffersMap,
			Map<String, PlanTypeDescription> planTypeDescMap, Map<String, Boolean> selectedBenefits, long groupId,
			Map.Entry<String, Set<StateBenefitPlan>> entry, AdditionalBenefitPlan adBenefitPlan) {
		AdditionalBenefitsCategoryOffer addBCOffer;
		if (null != additionalBenefitOffersMap.get(adBenefitPlan.getPlanType())) {
			addBCOffer = additionalBenefitOffersMap.get(entry.getKey());
			addBCOffer.getAdditionalBenefitPlans().add(adBenefitPlan);
		} else {
			addBCOffer = new AdditionalBenefitsCategoryOffer();
			BenefitOfferSummary summary = addBCOffer.getSummary();
			summary.setGroupId(groupId);
			PlanTypeDescription planTypeDesc = planTypeDescMap.get(adBenefitPlan.getPlanType());
			if (planTypeDesc != null) {
				summary.setType(planTypeDesc.getType());
				summary.setDescription(planTypeDesc.getDescription());
			}
			addBCOffer.setSummary(summary);
			if (planTypeDesc != null) {
				addBCOffer.setMandatory(selectedBenefits.get(planTypeDesc.getType()));
			}
			addBCOffer.getAdditionalBenefitPlans().add(adBenefitPlan);
			additionalBenefitOffersMap.put(adBenefitPlan.getPlanType(), addBCOffer);
		}
	}

	private void getDisabilityPlanCost(Map<String, Set<StateBenefitPlan>> additionalBenefitsAllStatePlansMap, Company company, Map<String, BigDecimal> planCostMap,
			Map<String, Boolean> selectedBenefits, Map<String, AdditionalBenefitsCategoryOffer> additionalBenefitOffersMap,
			long groupId, String groupType, boolean isRenewalCompany, ActiveEligibleEECount activeEligibleEECount,
			Set<AdditionalBenefitPlan> disabilityPlans) {
		if (additionalBenefitsAllStatePlansMap.containsKey(PlanTypesEnum.STD.getCode())
				|| additionalBenefitsAllStatePlansMap.containsKey(PlanTypesEnum.LTD.getCode())) {
			AdditionalBenefitsCategoryOffer abc = new AdditionalBenefitsCategoryOffer();
			BenefitOfferSummary summary = abc.getSummary();
			summary.setType(BSSApplicationConstants.DISABILITY);
			summary.setDescription("Short & Long Term Disability Plan Options");
			summary.setGroupId(groupId);
			abc.setMandatory(selectedBenefits.get(PlanTypesEnum.LTD.getName()));
			for (AdditionalBenefitPlan abp : disabilityPlans) {
				AdditionalBenefitServiceHelper.populatePlanOptionsCost(planCostMap, activeEligibleEECount, abp,
						company);
			}
			Set<AdditionalBenefitPlan> filteredDisabilityPlans = new HashSet<>();
			for (AdditionalBenefitPlan abp : disabilityPlans) {
				if ((BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType)
						&& (BSSApplicationConstants.K1_GROUP_TYPE.equals(abp.getOfferedGroupType())
								|| BSSApplicationConstants.DISABILITY_OFFERED_GROUP_TYPE_ALL
										.equals(abp.getOfferedGroupType()))) 
						|| (BSSApplicationConstants.STD_GROUP_TYPE.equals(groupType)
												&& (BSSApplicationConstants.STD_GROUP_TYPE.equals(abp.getOfferedGroupType())
														|| BSSApplicationConstants.DISABILITY_OFFERED_GROUP_TYPE_ALL
																.equals(abp.getOfferedGroupType()))) ) {
					filteredDisabilityPlans.add(abp);
				} 
			}
			abc.setAdditionalBenefitPlans(filteredDisabilityPlans);
			additionalBenefitOffersMap.put(PlanTypesEnum.DISABILITY.getCode(), abc);
		}
	}

	@Override
	public Map<String, Map<String, BigDecimal>> calculateAdditionalPlansCostByGroup(Company company, boolean history,
			Map<String, Set<String>> adbPlanMap, Map<String, AdditionalBenefitEmployeeDetails> employeeSelection) {
		Map<String, Map<String, BigDecimal>> groupPlanCostMap = new HashMap<>();
		Date effDt = company.isRenewalCompany() && history ? company.getRealmPlanYear().getPlanYearEnd() : company.getRealmPlanYear().getPlanYearStart();
		for (String group : employeeSelection.keySet()) {
			for (String offerType : BSSApplicationConstants.ADDITIONAL_PLAN_TYPES) {
				Set<String> plans = adbPlanMap.get(offerType);
				if (CollectionUtils.isNotEmpty(plans)) {
					Map<String, BigDecimal> planCostMap = new HashMap<>();
					String coverageProperties = null;
					String bandCodes = null;
					if (BSSApplicationConstants.DISABILITY_PLAN_TYPES.contains(offerType)) {
						coverageProperties = BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES;
						bandCodes = company.getBandCodes().getDisBandCode();
					} else {
						coverageProperties = BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES;
						bandCodes = company.getBandCodes().getLifeBandCode();
					}
					Map<String, FormulaProperties> formulaProps = lifeAndDisabilityCalcData.getFormulaProperties(plans,
							effDt, coverageProperties);
					Map<String, RateProperties> rateProperties = lifeAndDisabilityCalcData.getRateProperties(
							company.getRealmPlanYear().getCloneProgram(), effDt, plans, bandCodes, company.getQuater());
					Map<String, List<AdditionalPlanRate>> rates = getPlanRates(company, rateProperties, history);
					calculatePlanCostForRenewal(company, formulaProps, rateProperties, planCostMap, offerType,
							employeeSelection, rates, group);
					groupPlanCostMap.computeIfAbsent(group, k -> new HashMap<>()).putAll(planCostMap);
				}
			}
		}
		return groupPlanCostMap;
	}
	
	@Override
	public Map<String, BigDecimal> calculateAdditionalPlansCost(Company company, boolean history,
			Map<String, Set<String>> adbPlanMap) {
		Map<String, BigDecimal> planCostMap = new HashMap<>();
		Date effDt = null;
		if (company.isRenewalCompany() && history) {
			effDt = company.getRealmPlanYear().getPlanYearEnd();
		} else {
			effDt = company.getRealmPlanYear().getPlanYearStart();
		}
		for (String offerType : BSSApplicationConstants.ADDITIONAL_PLAN_TYPES) {
			Set<String> plans = adbPlanMap.get(offerType);
			if (CollectionUtils.isNotEmpty(plans)) {
				String coverageProperties = null;
				String bandCodes = null;
				if (BSSApplicationConstants.DISABILITY_PLAN_TYPES.contains(offerType)) {
					coverageProperties = BSSQueryConstants.DISABILITY_CVG_FORMULA_PROPERTIES;
					bandCodes = company.getBandCodes().getDisBandCode();
				} else {
					coverageProperties = BSSQueryConstants.LIFE_CVG_FORMULA_PROPERTIES;
					bandCodes = company.getBandCodes().getLifeBandCode();
				}
				Map<String, FormulaProperties> formulaProps = lifeAndDisabilityCalcData.getFormulaProperties(plans,
						effDt, coverageProperties);
				Map<String, RateProperties> rateProperties = lifeAndDisabilityCalcData.getRateProperties(
						company.getRealmPlanYear().getCloneProgram(), effDt, plans, bandCodes, company.getQuater());
				Map<String, List<AdditionalPlanRate>> rates = getPlanRates(company, rateProperties, history);
				calculatePlanCostForNewClient(company, formulaProps, rateProperties, planCostMap, offerType, rates);
			}
		}
		return planCostMap;
	}

	private Map<String, BigDecimal> calculatePlanCostForRenewal(Company company,
			Map<String, FormulaProperties> formulaProps, Map<String, RateProperties> map,
			Map<String, BigDecimal> planCostMap, String planType,
			Map<String, AdditionalBenefitEmployeeDetails> empSelections, Map<String, List<AdditionalPlanRate>> rates,
			String group) {
		BigDecimal finalCost = BigDecimal.ZERO.setScale( 2 );
		BigDecimal totalCost = BigDecimal.ZERO.setScale( 2 );
		try {
			if (formulaProps != null) {
				for (Map.Entry<String, FormulaProperties> entry : formulaProps.entrySet()) {

					RateProperties rp = map.get(entry.getKey());
					if (rp == null) {

						BSSApplicationError error = new BSSApplicationError(
								"Rate properties are not found for Benefit Plan : " + entry.getKey());
						throw new BSSApplicationException(error);
					}
					if ("NONE".equals(rp.getRatePerUnit())) {
						updatePlanCostMapForNoneRate(planCostMap, empSelections, rates, group, entry, rp);
					} else {
						// Calculate cost based on rate formula

						// get formula definition details and attach to formula properties
						List<FormulaDefinition> fd = lifeAndDisabilityCalcData.getFormulaDefinition(
								entry.getValue().getFormulaID(), entry.getValue().getFormulaEffDt());
						entry.getValue().setFormulaDefs( fd );

						logger.info("Formula ID : {} entry.getValue().getFormulaEffDt() {}: ", entry.getValue().getFormulaID(), entry.getValue().getFormulaEffDt());

						totalCost = BigDecimal.ZERO.setScale(2);
						BigDecimal totalCoverage = BigDecimal.ZERO.setScale( 2 );
						BigDecimal planRate = BigDecimal.ZERO;
						BigDecimal preUnitRate = BigDecimal.ZERO;
						long employeeCount = 0;

						AdditionalBenefitEmployeeDetails benDetails = empSelections.get( group );
						// loop through the salary amounts that have been saved for this plan
						for( BigDecimal salaryBase : benDetails.retrieveEmplSalaryMapForPlan( entry.getKey() ).values() ) {

							BigDecimal coverageAmt = calculateCoverageAmount(planType, entry, salaryBase);
							planRate = calculatePlanRate(rates, rp);
							preUnitRate = calculatePerUnitRate(rp, planRate, coverageAmt);
							finalCost = calculateFinalCost(finalCost, rp, coverageAmt, planRate);

							totalCost = totalCost.add( finalCost );
							totalCoverage = totalCoverage.add(coverageAmt);
							employeeCount++;

						}


						BigDecimal avgCostPerEmployee = calculateAvgCostPerEmployee(totalCost, employeeCount);
						
						planCostMap.put( entry.getKey(), avgCostPerEmployee );
						planCostMap.put(entry.getKey() + ProspectConstants.EST_PYRL_CVG,totalCoverage);
						planCostMap.put(entry.getKey() + ProspectConstants.UNIT_RATE, planRate);
						planCostMap.put(entry.getKey() + ProspectConstants.UNIT, preUnitRate);
						planCostMap.put(entry.getKey() + ProspectConstants.MONTHLY_COST, totalCost);
					
					}
					planCostMap.put(entry.getKey() + ProspectConstants.GROUP_HEADCOUNT,
							new BigDecimal(empSelections.get( group ).getEmplCountForPlan( entry.getKey() )));
				}
			}
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, company.getCode(), "");
		}
		return planCostMap;
	}

	private BigDecimal calculateAvgCostPerEmployee(BigDecimal totalCost, long employeeCount) {
		BigDecimal avgCostPerEmployee;
		if( employeeCount == 0 ) {
			avgCostPerEmployee = BigDecimal.ZERO;
		} else {
			avgCostPerEmployee = totalCost.divide( BigDecimal.valueOf( employeeCount ), RoundingMode.HALF_UP );
		}
		return avgCostPerEmployee;
	}

	private void updatePlanCostMapForNoneRate(Map<String, BigDecimal> planCostMap,
			Map<String, AdditionalBenefitEmployeeDetails> empSelections, Map<String, List<AdditionalPlanRate>> rates,
			String group, Map.Entry<String, FormulaProperties> entry, RateProperties rp) {
		BigDecimal finalCost = BigDecimal.ZERO.setScale(2);
		BigDecimal totalCost = BigDecimal.ZERO.setScale(2);
		if (rp.getRateType() == 2) {
			finalCost = rates.get(rp.getRateTblID()).get(0).getRate().setScale(2,
					RoundingMode.HALF_UP);
			totalCost = finalCost.multiply(BigDecimal.valueOf(empSelections.get( group ).retrieveEmplSalaryMapForPlan(entry.getKey()).size()))
					.setScale(2, RoundingMode.HALF_UP);
		}
		planCostMap.put(entry.getKey(), finalCost);
		planCostMap.put(entry.getKey() + ProspectConstants.EST_PYRL_CVG, null);
		planCostMap.put(entry.getKey() + ProspectConstants.UNIT_RATE, finalCost);
		planCostMap.put(entry.getKey() + ProspectConstants.UNIT, null);
		planCostMap.put(entry.getKey() + ProspectConstants.MONTHLY_COST, totalCost);
	}
		
	private BigDecimal calculatePerUnitRate(RateProperties rp, BigDecimal planRate, BigDecimal coverageAmt) {
		BigDecimal preUnitRate = BigDecimal.ZERO;
		if (planRate.compareTo(BigDecimal.ZERO) > 0 && null != coverageAmt) {
			if (rp.getRatePerUnit().equals("PHUN")) {
				preUnitRate = new BigDecimal(100);
			} else if (rp.getRatePerUnit().equals("PTHO")) {
				preUnitRate = new BigDecimal(1000);
			}
		}
		return preUnitRate;
	}


	/**
	 * 
	 * @param benefitProgram
	 * @param groups
	 * @return
	 */
	private BenefitGroup getBenefitGroup(String benefitProgram, List<BenefitGroup> groups) {
		for (BenefitGroup group : groups) {
			if (group.getBenefitProgram().equals(benefitProgram)) {
				return group;
			}
		}
		return null;
	}
	
	private Set<AdditionalBenefitPlan> prepareClone(Set<AdditionalBenefitPlan> disabilityPlans) {
		if (null == disabilityPlans) {
			return disabilityPlans;
		}
		Set<AdditionalBenefitPlan> clonedData = new HashSet<>();
		for (AdditionalBenefitPlan additionalBenefitPlan : disabilityPlans) {
			clonedData.add(new AdditionalBenefitPlan(additionalBenefitPlan));
		}
		return clonedData;
	}
}