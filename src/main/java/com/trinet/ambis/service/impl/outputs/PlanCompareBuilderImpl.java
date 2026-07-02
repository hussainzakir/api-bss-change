package com.trinet.ambis.service.impl.outputs;

import static com.trinet.ambis.helper.PlanCompareHelper.getAgeBandCvgLvlInfo;
import static com.trinet.ambis.helper.PlanCompareHelper.getCvgLvlPlanHeadCount;
import static com.trinet.ambis.helper.PlanCompareHelper.getCvgLvlPlanInfo;
import static com.trinet.ambis.helper.PlanCompareHelper.getPlanAttribute;
import static com.trinet.ambis.helper.PlanCompareHelper.populateMDVAttributeLabels;
import static com.trinet.ambis.helper.PlanCompareHelper.prepareDentalBenType;
import static com.trinet.ambis.helper.PlanCompareHelper.prepareVisionBenType;
import static com.trinet.ambis.util.PlanWordWrapUtil.calculateMaxLinesForPlan;
import static com.trinet.ambis.util.PlanWordWrapUtil.getBenTypeName;
import static com.trinet.ambis.common.PlanAttributeConstants.LIMITS_MAP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.trinet.ambis.common.ProspectConstants;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.helper.AttributeBuilderHelper;
import com.trinet.ambis.helper.CompanyServiceHelper;
import com.trinet.ambis.rest.controllers.dto.outputs.AttributeDesc;
import com.trinet.ambis.rest.controllers.dto.outputs.BasePlanComparison;
import com.trinet.ambis.rest.controllers.dto.outputs.CompareCurrentTrinetPlans;
import com.trinet.ambis.rest.controllers.dto.outputs.CurrentTrinetPlans;
import com.trinet.ambis.rest.controllers.dto.outputs.CvgLvlPlanInfo;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanAttribute;
import com.trinet.ambis.rest.controllers.dto.outputs.PlanComparison;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.outputs.PlanCompareBuilder;
import com.trinet.ambis.service.outputs.Populator;
import com.trinet.ambis.service.prospect.dto.EmployeeHeadCountRes;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlanCompareBuilderImpl implements PlanCompareBuilder {
	
	@Override
	public Populator buildPlanAttributeLabels() {
		return plans -> {
			Map<String, BasePlanComparison> planComparisonMap = plans.getPlanComparison();
			PlanComparison planComparison = null;

			Map<String, List<BenefitPlanCompare>> benefitTypePlansAttributes = plans.getBenefitTypeTrinetPlansAttributes();
			boolean useTrinetPlanAttributes = MapUtils.isNotEmpty(benefitTypePlansAttributes)
					&& benefitTypePlansAttributes.values().iterator().next().stream()
					.findFirst()
					.map(plan -> StringUtils.isNotBlank(plan.getPlanId()) && !"null".equalsIgnoreCase(plan.getPlanId()))
					.orElse(false);
			benefitTypePlansAttributes = useTrinetPlanAttributes
					? benefitTypePlansAttributes
					: plans.getBenefitTypeCurrentPlansAttributes();

			for(Map.Entry<String, List<BenefitPlanCompare>> entry : benefitTypePlansAttributes.entrySet()) {
				String benefitType = entry.getKey();
				if(plans.getRequestBenefitTypes().contains(benefitType) && (benefitTypePlansAttributes.containsKey(entry.getKey()) &&
						!entry.getKey().equalsIgnoreCase(AttributeBuilderHelper.NO_TYPE))){
					List<AttributeDesc> attributeDescs = populateMDVAttributeLabels(entry.getKey(), benefitTypePlansAttributes);
					if (Objects.nonNull(attributeDescs)) {
						List<CompareCurrentTrinetPlans> comparisons = new ArrayList<>();
						planComparison = PlanComparison.builder()
								.attributeNames(attributeDescs)
								.comparisons(comparisons)
								.build();
						String mdvBenType = prepareDentalBenType().andThen(prepareVisionBenType()).apply(entry.getKey());
						planComparisonMap.put(mdvBenType, planComparison);
					}
				}
			}
			plans.setPlanComparison(planComparisonMap);
		};
	}
	
	@Override
	public Populator buildCurrentAndTrinetPlanAttributeValues() {
		return plans -> {
			//BeneftType, CurrentPlanId, List of trinetPlanIds
			for(Map.Entry<String, Map<String, List<String>>> planMapEntrySet : plans.getCurrentTrinetPlansMapping().entrySet())
			{
				String benefitType = planMapEntrySet.getKey();
				if(plans.getRequestBenefitTypes().contains(benefitType)){
					List<CompareCurrentTrinetPlans> comparisons = new LinkedList<>();
					PlanComparison basePlanComparison = (PlanComparison)plans.getPlanComparison().get(benefitType);
					Map<String, List<String>> currentTrinetPlansByBnftType = planMapEntrySet.getValue();

					for(Map.Entry<String, List<String>> mappedPlanEntry : currentTrinetPlansByBnftType.entrySet()) {
						String currentPlanId = mappedPlanEntry.getKey();
						List<String> trinetPlanIds = mappedPlanEntry.getValue();
						CompareCurrentTrinetPlans mappedCurrentPlan = prapareMappedCurrentPlans(benefitType, currentPlanId, plans, trinetPlanIds);
						comparisons.add(mappedCurrentPlan);
						if(Objects.nonNull(basePlanComparison)) {
							basePlanComparison.setComparisons(comparisons);
						}
					}
				}
			}
		};
	}

	@Override
	public Populator buildCurrentPlanRate() {
		return plans -> {
			for(String id : plans.getRequestBenefitTypes()) {
				PlanComparison basePlanComparison = (PlanComparison) plans.getPlanComparison().get(id);
				Map<String, RateDetail> currentPlanRates = plans.getCurrentPlanRates();
				currentPlanRates.forEach((planId, currentRateDetail) -> {
					if(Objects.nonNull(basePlanComparison) && !CollectionUtils.isEmpty(basePlanComparison.getComparisons())) {
						preparePlanComparisonCurrentRate(planId, basePlanComparison, currentRateDetail);
					}
				});
			}
		};
	}

	@Override
	public Populator buildCurrentPlanHeadCount() {
		return plans -> plans.getRequestBenefitTypes().stream().forEach(key -> {
			PlanComparison basePlanComparison = (PlanComparison) plans.getPlanComparison().get(key);
			List<EmployeeHeadCountRes> currentPlanHeadCounts = plans.getCurrentPlanHeadCounts();
			if(Objects.nonNull(basePlanComparison)&& !CollectionUtils.isEmpty(basePlanComparison.getComparisons())) {
				Map<String, Map<String, String>> mapOfCurrentPlanHeadCounts = currentPlanHeadCounts.stream()
						.distinct()
						.collect(Collectors.groupingBy(EmployeeHeadCountRes::getBenefitPlan, Collectors.toMap(EmployeeHeadCountRes::getCovrgCD, emp -> String.valueOf(emp.getCount()))));
				
				basePlanComparison.getComparisons().stream()
				.forEach(compPlan -> {
					if(mapOfCurrentPlanHeadCounts.containsKey(compPlan.getCurrentPlan().getPlanId())) {
						CvgLvlPlanInfo cvgLvlPlan = getCvgLvlPlanHeadCount(mapOfCurrentPlanHeadCounts.get(compPlan.getCurrentPlan().getPlanId()));
						compPlan.getCurrentPlan().setHeadCount(cvgLvlPlan);
					}
				});
			}
		});

	}
	
	@Override
	public Populator buildTrinetPlanRate() {
		return plans -> {
			for(String id : plans.getRequestBenefitTypes()) {
				PlanComparison basePlanComparison = (PlanComparison) plans.getPlanComparison().get(id);
				Map<String, RateDetail> trinetPlanRates = plans.getTrinetPlanRates();
				// If TIB plan rates are present, merge them with trinetPlanRates
				Map<String, RateDetail> tibPlanRates = plans.getTibPlanRates();
				if(Objects.nonNull(tibPlanRates) && !CollectionUtils.isEmpty(tibPlanRates)) {
					trinetPlanRates.putAll(tibPlanRates);
				}
				if(Objects.nonNull(basePlanComparison)&& !CollectionUtils.isEmpty(basePlanComparison.getComparisons())) {
					preparePlanComparisonTrinetRate(basePlanComparison, trinetPlanRates);
				}
			}
		};
	}

	@Override
	public Populator buildTrinetPlanHeadCount() {
		return plans -> plans.getRequestBenefitTypes().stream().forEach(bnType -> {
				PlanComparison basePlanComparison = (PlanComparison) plans.getPlanComparison().get(bnType);
				Map<String,Map<String,CvgLvlPlanInfo>> cvgLvlPlanInfo = prepareHeadCount(plans, bnType);
				if(Objects.nonNull(basePlanComparison) && !CollectionUtils.isEmpty(basePlanComparison.getComparisons())) {
					updatePlanHeadCount(bnType,basePlanComparison, cvgLvlPlanInfo);
				}
			});
	}

	@Override
	public Populator buildPlanComparisonPagination() {
		return plans -> {
			if (!AppRulesAndConfigsUtils.isBssOutputPhase2Enabled()) {
				return;
			}
			for (Map.Entry<String, Map<String, List<String>>> planMapEntrySet : plans.getCurrentTrinetPlansMapping().entrySet()) {
				String benefitType = planMapEntrySet.getKey();
				if (plans.getRequestBenefitTypes().contains(benefitType)) {
					PlanComparison basePlanComparison = (PlanComparison) plans.getPlanComparison().get(benefitType);
					if (Objects.nonNull(basePlanComparison) && !CollectionUtils.isEmpty(basePlanComparison.getComparisons())) {
						List<AttributeDesc> attributeLabels = basePlanComparison.getAttributeNames();

						int rowCount = 0;
						int pageCount = 1;
						int FIRST_PAGE_LIMIT = BSSApplicationConstants.PLAN_COMPARE_MAX_LINES_FIRST_PAGE;
						int SUBSEQUENT_PAGE_LIMIT = BSSApplicationConstants.PLAN_COMPARE_MAX_LINES_SUBSEQUENT_PAGES;

						if (CompanyServiceHelper.isTibProspect(plans.getCompany())) {
							FIRST_PAGE_LIMIT -= BSSApplicationConstants.PLAN_COMPARE_HEADER_LINES;
						}

						String benTypeName = getBenTypeName(benefitType);
						String key = ProspectConstants.PLAN_COMPARISON+"_"+ benTypeName;
						Map<String, Double> limits = LIMITS_MAP.getOrDefault(key.toUpperCase(), java.util.Collections.emptyMap());
						for (CompareCurrentTrinetPlans mappedCurrentPlan : basePlanComparison.getComparisons()) {
							if (mappedCurrentPlan != null && mappedCurrentPlan.getCurrentPlan() != null) {
								int currentPlanLines = calculateMaxLinesForPlan(mappedCurrentPlan.getCurrentPlan(), attributeLabels, limits, benTypeName);
								int firstTrinetPlanLines = 0;
								if (!CollectionUtils.isEmpty(mappedCurrentPlan.getTriNetPlans())) {
									firstTrinetPlanLines = calculateMaxLinesForPlan(mappedCurrentPlan.getTriNetPlans().get(0), attributeLabels, limits, benTypeName);
								}

								int pageLimit = (pageCount == 1) ? FIRST_PAGE_LIMIT : SUBSEQUENT_PAGE_LIMIT;

								if (rowCount + currentPlanLines + firstTrinetPlanLines > pageLimit) {
									mappedCurrentPlan.getCurrentPlan().setPageBreak(true);
									rowCount = currentPlanLines;
									pageCount++;
								} else {
									rowCount += currentPlanLines;
								}

								if (!CollectionUtils.isEmpty(mappedCurrentPlan.getTriNetPlans())) {
									for (PlanAttribute trinetPlan : mappedCurrentPlan.getTriNetPlans()) {
										int trinetLines = calculateMaxLinesForPlan(trinetPlan, attributeLabels, limits, benTypeName);
										pageLimit = (pageCount == 1) ? FIRST_PAGE_LIMIT : SUBSEQUENT_PAGE_LIMIT;

										if (rowCount + trinetLines > pageLimit) {
											trinetPlan.setPageBreak(true);
											rowCount = trinetLines;
											pageCount++;
										} else {
											rowCount += trinetLines;
										}
									}
								}

								rowCount += 1;
							}
						}
					}
				}
			}
		};
	}

	private void preparePlanComparisonCurrentRate(String planId, PlanComparison basePlanComparison,
			RateDetail currentMedicalRateDetail) {
		if(Objects.nonNull(currentMedicalRateDetail)) {
			if (Objects.nonNull(currentMedicalRateDetail.getRateType()) && currentMedicalRateDetail.getRateType().equals(RateTypeEnum.FOUR_TIER.getType())) {
				updatePlanRatesForFourTier(planId, basePlanComparison, currentMedicalRateDetail);
			} else {
				basePlanComparison.getComparisons().forEach(compareCurrentTrinetPlans -> {
					PlanAttribute currentPlan = compareCurrentTrinetPlans.getCurrentPlan();
					if (currentPlan.getPlanId().equalsIgnoreCase(planId)) {
						currentPlan.setPlanRates(getAgeBandCvgLvlInfo());
					}
				});
 
			}
		}
	}

	private void updatePlanRatesForFourTier(String planId, PlanComparison basePlanComparison, RateDetail currentMedicalRateDetail) {
		Map<String, String> mapOfTierRateToCost = currentMedicalRateDetail.getTierRates().stream()
				.filter(tierRate -> Objects.nonNull(tierRate.getCvgTierCode()))
				.filter(tierRate -> Objects.nonNull(tierRate.getCost()))
				.collect(Collectors.toMap(tierRate -> CoverageCodesEnums.valueOfCode(tierRate.getCvgTierCode()),
						tierRate -> String.valueOf(tierRate.getCost())));
		basePlanComparison.getComparisons().forEach(compareCurrentTrinetPlans -> {
			PlanAttribute currentPlan = compareCurrentTrinetPlans.getCurrentPlan();
			if (currentPlan.getPlanId().equalsIgnoreCase(planId)) {
				if(!CollectionUtils.isEmpty(mapOfTierRateToCost)){
					currentPlan.setPlanRates(getCvgLvlPlanInfo(mapOfTierRateToCost));
				}else{
					currentPlan.setPlanRates(AttributeBuilderHelper.dummyCvgLvlPlan());
				}
			}
		});
	}

	private void preparePlanComparisonTrinetRate(PlanComparison basePlanComparison, Map<String, RateDetail> trinetPlanRates) {
		basePlanComparison.getComparisons().forEach(compareCurrentTrinetPlans ->
			compareCurrentTrinetPlans.getTriNetPlans().forEach(planAttribute -> {
				RateDetail trinetMedicalRateDetail = trinetPlanRates.get(planAttribute.getPlanId());
				if (Objects.nonNull(trinetMedicalRateDetail)) {
					if (Objects.nonNull(trinetMedicalRateDetail.getRateType()) && trinetMedicalRateDetail.getRateType().equals(RateTypeEnum.FOUR_TIER.getType())) {
						Map<String, String> mapOfTierRateToCost = trinetMedicalRateDetail.getTierRates().stream()
								.filter(tierRate -> Objects.nonNull(tierRate.getCvgTierCode()))
								.filter(tierRate -> Objects.nonNull(tierRate.getCost()))
								.collect(Collectors.toMap(tierRate -> CoverageCodesEnums.valueOfCode(tierRate.getCvgTierCode()),
										tierRate -> String.valueOf(tierRate.getCost())));
						planAttribute.setPlanRates(getCvgLvlPlanInfo(mapOfTierRateToCost));
					}
					else {
						planAttribute.setPlanRates(getAgeBandCvgLvlInfo());
					}
				}
                else {
                    log.error("Trinet plan rate detail not found for planId: {}", planAttribute.getPlanId());
                }
			}));
	}
	
	private CompareCurrentTrinetPlans prapareMappedCurrentPlans(String benefitType, String planId, CurrentTrinetPlans plans, List<String> trinetPlanIds) {
		CompareCurrentTrinetPlans comparePlans = null;
		if(!planId.equalsIgnoreCase(AttributeBuilderHelper.NO_PLAN_ID)) {
			Optional<BenefitPlanCompare> currentPlan = plans.getBenefitTypeCurrentPlansAttributes().get(benefitType).stream().filter(plan->plan.getPlanId().equalsIgnoreCase(planId)).findAny();
			if(currentPlan.isPresent()) {
				PlanAttribute planAttribute = getPlanAttribute(currentPlan.get(), false);
				comparePlans = AttributeBuilderHelper.getCompareCurrentTrinetPlans(planAttribute, new LinkedList<>());
				if(plans.getCurrentTrinetPlansMapping().containsKey(benefitType)) {
					List<PlanAttribute> planAttributes = getTrinetPlanAttributes(plans, trinetPlanIds, benefitType);
					comparePlans.getTriNetPlans().addAll(planAttributes);
				}
			}
		}else {
			PlanAttribute planAttribute = AttributeBuilderHelper.planAttributeMapByBenefitType().get(benefitType);
			comparePlans = AttributeBuilderHelper.getCompareCurrentTrinetPlans(planAttribute, new LinkedList<>());
			if(plans.getCurrentTrinetPlansMapping().containsKey(benefitType) && !CollectionUtils.isEmpty(trinetPlanIds)) {
				List<PlanAttribute> planAttributes = getTrinetPlanAttributes(plans, trinetPlanIds, benefitType);
				comparePlans.getTriNetPlans().addAll(planAttributes);
			}
		}
		return comparePlans;
	}

	private List<PlanAttribute> getTrinetPlanAttributes(CurrentTrinetPlans plans, List<String> trinetPlanIds, String benefitType) {
		List<PlanAttribute> planAttributes = new LinkedList<>();
		trinetPlanIds.stream().forEach(id -> {
			List<BenefitPlanCompare> trinetPlans = plans.getBenefitTypeTrinetPlansAttributes().get(benefitType);
			if(isDentalType().test(benefitType)) {
				trinetPlans = voluntaryBenType(plans, benefitType, BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
			}
			if(isVisionType().test(benefitType)) {
				trinetPlans = voluntaryBenType(plans, benefitType, BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
			}
			if (CollectionUtils.isEmpty(trinetPlans)) {
				return;
			}
			Optional<BenefitPlanCompare> tPlanCompare = trinetPlans.stream().filter(tPlan -> tPlan.getPlanId().equals(id)).findFirst();
			if(tPlanCompare.isPresent()) {
				Optional<Map.Entry<List<String>, String>> carrierLogo  = plans.getCmsLogoDetailMap().entrySet().stream().filter(entry -> entry.getKey().contains(tPlanCompare.get().getCarrier())).findAny();
				tPlanCompare.get().setCarrierLogoUrl(carrierLogo.map(Map.Entry::getValue).orElse(null));
				PlanAttribute triNetPlanAttribute = getPlanAttribute(tPlanCompare.get(), false);
				planAttributes.add(triNetPlanAttribute);
			}
			
		});
		return planAttributes;
	}
	
	private List<BenefitPlanCompare> voluntaryBenType(CurrentTrinetPlans plans, String benefitType, String volBenefitType) {
		List<BenefitPlanCompare> trinetPlans;
		trinetPlans = plans.getBenefitTypeTrinetPlansAttributes().get(benefitType);
		List<BenefitPlanCompare> trinetVolPlans = plans.getBenefitTypeTrinetPlansAttributes().get(volBenefitType);
		if(!CollectionUtils.isEmpty(trinetVolPlans)) {
			trinetPlans = trinetPlans == null ? new LinkedList<>() : trinetPlans;
			trinetPlans.addAll(trinetVolPlans);
		}
		return trinetPlans;
	}
	
	private Predicate<String> isDentalType(){
		return BSSApplicationConstants.DENTAL_PLAN_TYPE::equalsIgnoreCase;
	}
	
	private Predicate<String> isVisionType(){
		return BSSApplicationConstants.VISION_PLAN_TYPE::equalsIgnoreCase;
	}
	
	private Map<String,Map<String,CvgLvlPlanInfo>> prepareHeadCount(CurrentTrinetPlans plans, String bnType){
		//Current:  PlanId, CoverageCode, List Of employees
		Map<String, Map<String, List<String>>> currentPlanCoverageEmployeeMapping = plans.getCurrentPlanCoverageEmployeeMapping();
		//Trinet:  PlanId, CoverageCode, List Of employees
		Map<String, Map<String, List<String>>> trinetPlanCoverageEmployeeMapping = plans.getTrinetPlanCoverageEmployeeMapping();
		//PlanType,currentPlanId, List of TrinetPlanIds
		Map<String, Map<String, List<String>>> currentTrinetPlansMapping = plans.getCurrentTrinetPlansMapping();
		
		Map<String,Map<String,CvgLvlPlanInfo>> cvgLvlPlanInfo = new HashMap<>();
		Map<String, Map<String, Map<String, Integer>>> totalTriNetHCExceptNoCurrentPlan = new HashMap<>();
		
		for(Map.Entry<String, Map<String, List<String>>> entry : currentTrinetPlansMapping.entrySet()) {
			if(bnType.equalsIgnoreCase(entry.getKey())){
				for (Map.Entry<String, List<String>> valueEntry : entry.getValue().entrySet()) {
					Map<String, CvgLvlPlanInfo> cvgLvlPlan = new HashMap<>();
					String currentPlanId = valueEntry.getKey();
					List<String> trinetPlans = valueEntry.getValue();
					if (Objects.nonNull(currentPlanCoverageEmployeeMapping)) {
						for (String plan : trinetPlans) {
							Map<String, List<String>> trinetPlanDetails = trinetPlanCoverageEmployeeMapping.get(plan);
							Map<String, String> coverageCount = new HashMap<>();
							totalTriNetHCExceptNoCurrentPlan.putIfAbsent(bnType, new HashMap<>());
							Map<String, Integer> cvgLvlhcTotalWithZeros = initializeCvgLvlTotalHCWithZeros();
							totalTriNetHCExceptNoCurrentPlan.get(bnType).putIfAbsent(plan, cvgLvlhcTotalWithZeros);
							for (Map.Entry<String, List<String>> trinetPlanEntry : trinetPlanDetails.entrySet()) {
								String cvgCode = trinetPlanEntry.getKey();
								List<String> trinetEmpl = trinetPlanEntry.getValue();
								List<String> currentEmpl = currentPlanEmployees(plans, currentPlanId,
										trinetPlanEntry.getKey());
								if (currentPlanId.equalsIgnoreCase(AttributeBuilderHelper.NO_PLAN_ID)) {
									// calculate later once we identify all the HCs
									coverageCount.put(trinetPlanEntry.getKey(), String.valueOf(trinetEmpl.size()));
								} else {
									List<String> commonEmpl = trinetEmpl.stream().filter(currentEmpl::contains)
											.collect(Collectors.toList());
									coverageCount.put(trinetPlanEntry.getKey(), String.valueOf(commonEmpl.size()));
									int currentHcCnt = totalTriNetHCExceptNoCurrentPlan.get(bnType).get(plan)
											.get(cvgCode);
									int newTotalCnt = currentHcCnt + commonEmpl.size();
									totalTriNetHCExceptNoCurrentPlan.get(bnType).get(plan).put(cvgCode, newTotalCnt);
								}
							}
							cvgLvlPlan.put(plan, getCvgLvlPlanHeadCount(coverageCount));
						}
						StringJoiner planIdWithPlanType = new StringJoiner("_").add(currentPlanId).add(entry.getKey());
						cvgLvlPlanInfo.put(planIdWithPlanType.toString(), cvgLvlPlan);
					}
				}
			}
		}
		updateTriNetPlanHcForNoCurrentPlanMapping(bnType, cvgLvlPlanInfo, totalTriNetHCExceptNoCurrentPlan);
		return cvgLvlPlanInfo;
	}

	private Map<String, Integer> initializeCvgLvlTotalHCWithZeros() {
		Map<String, Integer> cvgLvlhcTotalWithZeros = new HashMap<>();
		cvgLvlhcTotalWithZeros.put(CoverageCodesEnums.COV_EMPLOYEE.getCode(), 0);
		cvgLvlhcTotalWithZeros.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode(), 0);
		cvgLvlhcTotalWithZeros.put(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode(), 0);
		cvgLvlhcTotalWithZeros.put(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode(), 0);
		return cvgLvlhcTotalWithZeros;
	}
	
	private List<String> currentPlanEmployees(CurrentTrinetPlans plans, String currentPlanId, String cvrgCode){
		List<String> currentEmpl = null;
		//Current:  PlanId, CoverageCode, List Of employees
		Map<String, Map<String, List<String>>> currentPlanCoverageEmployeeMapping = plans.getCurrentPlanCoverageEmployeeMapping();
		if (Objects.nonNull(currentPlanCoverageEmployeeMapping)) {
			Map<String, List<String>> currentPlanDetails = currentPlanCoverageEmployeeMapping.get(currentPlanId);
			if (Objects.nonNull(currentPlanDetails)) {
				currentEmpl = currentPlanDetails.get(cvrgCode);
			}else {
				currentEmpl = plans.getNoCurrentPlanEmployees().stream().map(EmployeePlansRes::getEmployeeId).collect(Collectors.toList());
			}
		}
		return CollectionUtils.isEmpty(currentEmpl) ? new ArrayList<>() : currentEmpl;
	}

	private void updatePlanHeadCount(String bnType, PlanComparison basePlanComparison, Map<String, Map<String, CvgLvlPlanInfo>> cvgLvlPlanInfo) {
		basePlanComparison.getComparisons().stream().forEach(compPlan ->
			compPlan.getTriNetPlans().forEach(compTPlan -> {
				StringJoiner planIdWithPlanType = new StringJoiner("_").add(compPlan.getCurrentPlan().getPlanId()).add(bnType);
				if (cvgLvlPlanInfo.containsKey(planIdWithPlanType.toString()) && Objects.nonNull(cvgLvlPlanInfo.get(planIdWithPlanType.toString()))) {
					CvgLvlPlanInfo cvgLvlInfo = cvgLvlPlanInfo
							.get(planIdWithPlanType.toString())
							.get(compTPlan.getPlanId());
					if (Objects.nonNull(cvgLvlInfo)) {
						compTPlan.setHeadCount(cvgLvlInfo);
					}
				}
			}));
	}

	private void updateTriNetPlanHcForNoCurrentPlanMapping(String bnType,
			Map<String, Map<String, CvgLvlPlanInfo>> cvgLvlPlanInfo,
			Map<String, Map<String, Map<String, Integer>>> totalTriNetHCExceptNoCurrentPlan) {
		try {
			Map<String, CvgLvlPlanInfo> cvgLvlInfoByPlan = cvgLvlPlanInfo
					.get(new StringJoiner("_").add(AttributeBuilderHelper.NO_PLAN_ID).add(bnType).toString());
			if (MapUtils.isNotEmpty(cvgLvlInfoByPlan)) {
				cvgLvlInfoByPlan.entrySet().forEach(entrySet -> {
					String planId = entrySet.getKey();
					CvgLvlPlanInfo cvgLvlInfo = cvgLvlInfoByPlan.get(planId);
					int eeHc = Integer.valueOf(cvgLvlInfo.getEmployeeOnly()) - totalTriNetHCExceptNoCurrentPlan.get(bnType)
							.get(planId).get(CoverageCodesEnums.COV_EMPLOYEE.getCode());
					cvgLvlInfo.setEmployeeOnly(String.valueOf(eeHc));
	
					int spouseHc = Integer.valueOf(cvgLvlInfo.getEmployeeSpouse()) - totalTriNetHCExceptNoCurrentPlan
							.get(bnType).get(planId).get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
					cvgLvlInfo.setEmployeeSpouse(String.valueOf(spouseHc));
	
					int childrenHc = Integer.valueOf(cvgLvlInfo.getEmployeeChildren()) - totalTriNetHCExceptNoCurrentPlan
							.get(bnType).get(planId).get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode());
					cvgLvlInfo.setEmployeeChildren(String.valueOf(childrenHc));
	
					int familyHc = Integer.valueOf(cvgLvlInfo.getFamily()) - totalTriNetHCExceptNoCurrentPlan.get(bnType)
							.get(planId).get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode());
					cvgLvlInfo.setFamily(String.valueOf(familyHc));
				});
			}
		} catch (Exception e) {
			log.error("Error occured while setting the HC for plan compare No Current Plan scenario :: {}", e);
		}
	}
}
