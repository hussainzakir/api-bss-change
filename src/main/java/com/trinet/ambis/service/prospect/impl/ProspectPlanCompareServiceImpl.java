package com.trinet.ambis.service.prospect.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.helper.PlanCompareHelper;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupPlanSelectDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.EePlanAssignment;
import com.trinet.ambis.persistence.model.EePlanAssignmentPK;
import com.trinet.ambis.persistence.projections.PlanSelectionView;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.Attribute;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.BenPlanDetail;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.PlanCompareData;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.RateDetail;
import com.trinet.ambis.rest.controllers.dto.plancompare.PlanCompareDetailDto.TierRate;
import com.trinet.ambis.service.BenefitPlanService;
import com.trinet.ambis.service.EmployeePlanAssignmentService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.RlRegionPlan1Service;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.plancompare.BenefitPlan;
import com.trinet.ambis.service.model.plancompare.BenefitPlanCompare;
import com.trinet.ambis.service.model.plancompare.PlanCompareTemplate;
import com.trinet.ambis.service.prospect.ProspectEmployeeService;
import com.trinet.ambis.service.prospect.ProspectPlanCompareService;
import com.trinet.ambis.service.prospect.dto.EmployeePlansRes;
import com.trinet.ambis.service.prospect.enums.BenefitTypeEnum;
import com.trinet.ambis.service.prospect.enums.RateTypeEnum;
import com.trinet.ambis.service.prospect.service.ProspectBenefitsPlansRatesService;
import com.trinet.ambis.util.StrategyUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProspectPlanCompareServiceImpl implements ProspectPlanCompareService {

	private final ProspectEmployeeService prospectEmployeeService;
	private final EmployeePlanAssignmentService emplPlanAssignmentService;
	private final BenefitPlanService benPlanService;
	private final RealmPlyrPlanService realmPlyrPlanService;
	private final PlanRatesService planRatesService;
	private final StrategyGroupPlanSelectDao strategyGroupPlanSelectDao;
	private final ProspectBenefitsPlansRatesService prospectBenefitsPlansRatesService;
	private final RlRegionPlan1Service rlRegionPlan1Service;

	public ProspectPlanCompareServiceImpl(
			ProspectEmployeeService prospectEmployeeService,
			EmployeePlanAssignmentService emplPlanAssignmentService,
			@Qualifier("benefitPlanService") BenefitPlanService benPlanService,
			RealmPlyrPlanService realmPlyrPlanService,
			PlanRatesService planRatesService,
			StrategyGroupPlanSelectDao strategyGroupPlanSelectDao,
			ProspectBenefitsPlansRatesService prospectBenefitsPlansRatesService,
			RlRegionPlan1Service rlRegionPlan1Service
	) {
		this.prospectEmployeeService = prospectEmployeeService;
		this.emplPlanAssignmentService = emplPlanAssignmentService;
		this.benPlanService = benPlanService;
		this.realmPlyrPlanService = realmPlyrPlanService;
		this.planRatesService = planRatesService;
		this.strategyGroupPlanSelectDao = strategyGroupPlanSelectDao;
		this.prospectBenefitsPlansRatesService = prospectBenefitsPlansRatesService;
		this.rlRegionPlan1Service = rlRegionPlan1Service;
	}
	private static final String REGION_CODE_ALL = "ALL";
	private static final int PARALLEL_MAPPING_MIN_EMPLOYEES = 128;

	@Override
	public List<PlanCompareDetailDto> getPlanCompareDetails(Company company, List<Long> trinetStrategyIds,
			HttpServletRequest httpRequest) {
		Optional<List<EmployeePlansRes>> currentEmployeePlans = prospectEmployeeService
				.getEmployeePlans(company.getCode());
		Map<String,String> bplToBcrIdMap = new LinkedHashMap<>();
		
		Map<String, Map<String, String>> currentEmplPlansAssignmentByBenType = converToMap(currentEmployeePlans,bplToBcrIdMap);
		Map<String, XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService
				.getMapForRealmPlanYear(company.getRealmPlanYear().getId());
		@SuppressWarnings({ "rawtypes" })
		Map<MultiKey, String> futureEmplPlansAssignmentByBenType = findTriNetEmplPlanAssignments(trinetStrategyIds,
				plyrPlanMap);

		Map<String, BenefitPlan> regionalBasePlanMappings = benPlanService
				.getRegionalBasePlanMapping(company.getRealmPlanYear());

		Map<String,Map<String, Set<String>>> plansIdsToCompareByBenefitType = getCurrentToFuturePlanMapping(trinetStrategyIds,
				currentEmplPlansAssignmentByBenType, futureEmplPlansAssignmentByBenType, regionalBasePlanMappings);
		
		Map<String, Set<String>> plansIdsToCompare = plansIdsToCompareByBenefitType.values().stream()
				.flatMap(innerMap -> innerMap.entrySet().stream())
				.collect(Collectors.toMap(entry -> bplToBcrIdMap.getOrDefault(entry.getKey(), entry.getKey()),
						Map.Entry::getValue, (existing, replacement) -> existing, LinkedHashMap::new));
 
		StopWatch stopWatch = new StopWatch("BenefitPlanViewApiCall Started");
		stopWatch.start();
		
		CompletableFuture<List<BenefitPlanCompare>> currentPlansResponse = PlanCompareHelper.getCurrentPlansAttributes(
				plansIdsToCompareByBenefitType, new Date(), BSSApplicationConstants.PROSPECT_PLAN_EXPORT_TEMPLATE,
				httpRequest);

		CompletableFuture<List<BenefitPlanCompare>> futurePlansResponse = PlanCompareHelper
				.getFuturePlansAttributes(plansIdsToCompareByBenefitType, company.getRealmPlanYear()
						.getPlanYearEnd(), BSSApplicationConstants.PROSPECT_PLAN_EXPORT_TEMPLATE, httpRequest);

		CompletableFuture<Map<String, RateDetail>> planIdToRateDetailsMap = CompletableFuture
				.completedFuture(prospectBenefitsPlansRatesService.getBenefitsPlansRateDetails(
						plansIdsToCompare.keySet().stream().collect(Collectors.toList()), company.getCode()));

		CompletableFuture.allOf(currentPlansResponse, futurePlansResponse, planIdToRateDetailsMap).join();

		stopWatch.stop();
		log.debug("%s BenefitPlanViewApiCall Started finished in %s:", stopWatch.getId(),
				stopWatch.getTotalTimeMillis());

		Map<String, BenefitPlanCompare> currentPlanAttributes;
		Map<String, BenefitPlanCompare> futurePlanAttributes;
		Map<String, RateDetail> planIdToRateDetails;
		try {
			currentPlanAttributes = PlanCompareHelper.mapPlanIdToObject(currentPlansResponse.get()).entrySet().stream()
					.collect(Collectors.toMap(entry -> bplToBcrIdMap.getOrDefault(entry.getKey(), entry.getKey()),
							Map.Entry::getValue, (existing, replacement) -> existing, LinkedHashMap::new));
			
			futurePlanAttributes = PlanCompareHelper.mapPlanIdToObject(futurePlansResponse.get());
			planIdToRateDetails = planIdToRateDetailsMap.get();
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			throw new BSSApplicationException(e,
					new BSSApplicationError("Exception occured while calling plan compare service."));
		}
		Map<String, List<String>> benefitPlanToRegions = rlRegionPlan1Service
				.findByRealmPlanYearId(company.getRealmPlanYear().getId());
		return constructPlanCompareDetailDtos(company, trinetStrategyIds, plansIdsToCompare, currentPlanAttributes,
				futurePlanAttributes, regionalBasePlanMappings, planIdToRateDetails, benefitPlanToRegions);
	}

	private @SuppressWarnings({ "unchecked", "rawtypes" }) Map<MultiKey, String> findTriNetEmplPlanAssignments(
			List<Long> strategyIds, Map<String, XbssRealmPlyrPlan> plyrPlanMap) {
		Map<MultiKey, String> benTypeEmplPlanAssignment = new LinkedHashMap<>();

		List<EePlanAssignment> emplPlanAssignment = emplPlanAssignmentService.getEmployeePlanAssigmentBy(strategyIds);
		emplPlanAssignment.forEach(emplPlanAssignmet -> {
			EePlanAssignmentPK eePlanAssignmentPK = emplPlanAssignmet.getEePlanAssignmentPK();
			XbssRealmPlyrPlan realmPlyrPlan = plyrPlanMap.get(emplPlanAssignmet.getBenefitPlan());
			if (realmPlyrPlan == null) {
				log.error("No plan configured for realm plan year in bss plan id: ",
						emplPlanAssignmet.getBenefitPlan());
			} else {
				String benefitPlanType = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
				if (BSSApplicationConstants.DENTAL_PLAN_TYPES.contains(realmPlyrPlan.getPlanType())) {
					benefitPlanType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
				}
				if (BSSApplicationConstants.VISION_PLAN_TYPES.contains(realmPlyrPlan.getPlanType())) {
					benefitPlanType = BSSApplicationConstants.VISION_PLAN_TYPE;
				}
				MultiKey multiKey = new MultiKey(eePlanAssignmentPK.getStrategyId(), benefitPlanType,
						eePlanAssignmentPK.getEmplId());
				benTypeEmplPlanAssignment.put(multiKey, emplPlanAssignmet.getBenefitPlan());
			}
		});
		return benTypeEmplPlanAssignment;
	}

	private Map<String,Map<String, Set<String>>> getCurrentToFuturePlanMapping(List<Long> trinetStratregyIds,
			Map<String, Map<String, String>> currentEmplPlansAssignmentByBenType,
			@SuppressWarnings({ "rawtypes" }) Map<MultiKey, String> futureEmplPlansAssignmentByBenType,
			Map<String, BenefitPlan> regionalBasePlanMappings) {

		Map<String, Map<String, Set<String>>> benefitTypeToPlanMapping = new LinkedHashMap<>();

		currentEmplPlansAssignmentByBenType.forEach((benefitType, currentPlans) -> {
			Map<String, Set<String>> currentFuturePlanMapping = buildCurrentFuturePlanMapping(trinetStratregyIds,
					benefitType, currentPlans, futureEmplPlansAssignmentByBenType, regionalBasePlanMappings);
			benefitTypeToPlanMapping.put(benefitType, currentFuturePlanMapping);
		});

		return benefitTypeToPlanMapping;
	}

	private Map<String, Set<String>> buildCurrentFuturePlanMapping(List<Long> trinetStratregyIds, String benefitType,
			Map<String, String> currentPlans,
			@SuppressWarnings("rawtypes") Map<MultiKey, String> futureEmplPlansAssignmentByBenType,
			Map<String, BenefitPlan> regionalBasePlanMappings) {
		boolean useParallel = currentPlans.size() >= PARALLEL_MAPPING_MIN_EMPLOYEES;
		Map<String, Set<String>> currentFuturePlanMapping = (useParallel ? currentPlans.entrySet().parallelStream()
				: currentPlans.entrySet().stream())
				.collect(Collectors.toMap(entry -> String.valueOf(entry.getValue()),
						entry -> collectFuturePlanIdsForEmployee(trinetStratregyIds, benefitType, entry.getKey(),
								futureEmplPlansAssignmentByBenType, regionalBasePlanMappings),
						this::mergeFuturePlanIds,
						LinkedHashMap::new));

		// Keep output deterministic for API consumers/tests even when upstream collection runs in parallel.
		currentFuturePlanMapping.replaceAll((currentPlanId, futurePlanIds) -> futurePlanIds.stream()
				.sorted()
				.collect(Collectors.toCollection(LinkedHashSet::new)));

		return currentFuturePlanMapping;
	}

	private Set<String> collectFuturePlanIdsForEmployee(List<Long> trinetStratregyIds, String benefitType,
			String employeeId,
			@SuppressWarnings("rawtypes") Map<MultiKey, String> futureEmplPlansAssignmentByBenType,
			Map<String, BenefitPlan> regionalBasePlanMappings) {
		Set<String> futurePlanIds = new LinkedHashSet<>();
		for (Long strategyId : trinetStratregyIds) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			MultiKey key = new MultiKey(strategyId, benefitType, employeeId);
			String futurePlanId = normalizeFuturePlanId(futureEmplPlansAssignmentByBenType.get(key),
					regionalBasePlanMappings);
			if (futurePlanId != null) {
				futurePlanIds.add(futurePlanId);
			}
		}
		return futurePlanIds;
	}

	private Set<String> mergeFuturePlanIds(Set<String> existingFuturePlanIds, Set<String> additionalFuturePlanIds) {
		Set<String> mergedFuturePlanIds = new LinkedHashSet<>(existingFuturePlanIds);
		mergedFuturePlanIds.addAll(additionalFuturePlanIds);
		return mergedFuturePlanIds;
	}

	private String normalizeFuturePlanId(String futurePlanId, Map<String, BenefitPlan> regionalBasePlanMappings) {
		if (futurePlanId == null) {
			return null;
		}
		return regionalBasePlanMappings.containsKey(futurePlanId)
				? regionalBasePlanMappings.get(futurePlanId).getPlanId()
				: futurePlanId;
	}

	private List<PlanCompareDetailDto> constructPlanCompareDetailDtos(Company company, List<Long> trinetStrategyIds,
			Map<String, Set<String>> plansIdsToCompare, Map<String, BenefitPlanCompare> currentPlanAttributes,
			Map<String, BenefitPlanCompare> futurePlanAttributes, Map<String, BenefitPlan> regionalToBasePlanMappings,
			Map<String, RateDetail> planIdToRateDetailsMap,
			Map<String, List<String>> benefitPlanToRegions) {

		Map<String, PlanCompareDetailDto> planCompareDetailDtosByBenType = initializePlanCompareDtos();
		// This map will be used to check whether the regional plan is in the strategy
		List<PlanSelectionView> planSelections = strategyGroupPlanSelectDao
				.findDistinctBenefitPlanPlanTypeByStrategyIdIn(trinetStrategyIds);
		Map<String, PlanSelectionView> planIdToStrategyPlanSelection = planSelections.stream()
				.collect(Collectors.toMap(PlanSelectionView::getBenefitPlan, Function.identity()));

		Map<String, List<String>> baseToRegionalPlanMappings = new HashMap<>();
		for (Entry<String, BenefitPlan> regionalToBasePlanMapping : regionalToBasePlanMappings.entrySet()) {
			String basePlanId = regionalToBasePlanMapping.getValue().getPlanId();
			String regionalPlanId = regionalToBasePlanMapping.getKey();
			if (baseToRegionalPlanMappings.containsKey(basePlanId)) {
				baseToRegionalPlanMappings.get(basePlanId).add(regionalPlanId);
			} else {
				List<String> regionalPlanIds = new ArrayList<>();
				regionalPlanIds.add(regionalPlanId);
				baseToRegionalPlanMappings.put(basePlanId, regionalPlanIds);
			}
		}

		for (Entry<String, Set<String>> plansToCompare : plansIdsToCompare.entrySet()) {
			BenefitPlanCompare currentBenPlan = getCurrentBenPlan(currentPlanAttributes, plansToCompare);
			BenPlanDetail currentPlanDetail = constructCurrentPlanDetailObject(currentBenPlan);
			if (planIdToRateDetailsMap.containsKey(plansToCompare.getKey())) {
				currentPlanDetail.setRates(List.of(planIdToRateDetailsMap.get(plansToCompare.getKey())));
			}
			List<BenPlanDetail> futurePlansDetails = constructFuturePlanDetailObjects(company, futurePlanAttributes,
					regionalToBasePlanMappings, baseToRegionalPlanMappings, plansToCompare.getValue(),
					planIdToStrategyPlanSelection, benefitPlanToRegions);
			PlanCompareData planCompareData = PlanCompareData.builder().currentPlan(currentPlanDetail)
					.futurePlans(futurePlansDetails).build();
			planCompareDetailDtosByBenType
					.get(BenefitTypeEnum.getBenTypeCodeFromBcrBenTypeDesc(currentBenPlan.getBenefitType()))
					.getPlanCompareData().add(planCompareData);
		}

		return planCompareDetailDtosByBenType.values().stream().collect(Collectors.toList());
	}

	private BenefitPlanCompare getCurrentBenPlan(Map<String, BenefitPlanCompare> currentPlanAttributes,
			Entry<String, Set<String>> plansToCompare) {
		Set<String> mdvNoPlanIds = Set.of(PlanCompareHelper.MED_NO_PLAN_ID, PlanCompareHelper.DEN_NO_PLAN_ID,
				PlanCompareHelper.VIS_NO_PLAN_ID);
		if (mdvNoPlanIds.contains(plansToCompare.getKey())) {
			String benType = "";
			if (plansToCompare.getKey().contains(BSSApplicationConstants.MEDICAL_PLAN_TYPE)) {
				benType = BenefitTypeEnum.MEDICAL.getBcrBenTypeDesc();
			} else if (plansToCompare.getKey().contains(BSSApplicationConstants.DENTAL_PLAN_TYPE)) {
				benType = BenefitTypeEnum.DENTAL.getBcrBenTypeDesc();
			} else if (plansToCompare.getKey().contains(BSSApplicationConstants.VISION_PLAN_TYPE)) {
				benType = BenefitTypeEnum.VISION.getBcrBenTypeDesc();
			}
			return BenefitPlanCompare.builder().planId(plansToCompare.getKey()).name("No Plan").benefitType(benType)
					.template(List.of()).build();
		}
		return currentPlanAttributes.get(plansToCompare.getKey());
	}

	private Map<String, PlanCompareDetailDto> initializePlanCompareDtos() {
		Map<String, PlanCompareDetailDto> planCompareDetailDtosByBenType = new HashMap<>();
		planCompareDetailDtosByBenType.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, PlanCompareDetailDto.builder()
				.benefitType(BSSApplicationConstants.MEDICAL_PLAN_TYPE).planCompareData(new ArrayList<>()).build());
		planCompareDetailDtosByBenType.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, PlanCompareDetailDto.builder()
				.benefitType(BSSApplicationConstants.DENTAL_PLAN_TYPE).planCompareData(new ArrayList<>()).build());
		planCompareDetailDtosByBenType.put(BSSApplicationConstants.VISION_PLAN_TYPE, PlanCompareDetailDto.builder()
				.benefitType(BSSApplicationConstants.VISION_PLAN_TYPE).planCompareData(new ArrayList<>()).build());
		return planCompareDetailDtosByBenType;
	}

	private List<BenPlanDetail> constructFuturePlanDetailObjects(Company company,
			Map<String, BenefitPlanCompare> futurePlanAttributes, Map<String, BenefitPlan> regionalBasePlanMappings,
			Map<String, List<String>> baseToRegionalPlanMappings, Set<String> futureBenPlanIds,
			Map<String, PlanSelectionView> planIdToStrategyPlanSelection,
			Map<String, List<String>> benefitPlanToRegions) {
		List<BenPlanDetail> futurePlansDetails = new ArrayList<>();
		Map<String, List<BenefitPlanRate>> planRate = planRatesService.getBenefitPlanRatesBy(company);
		futureBenPlanIds.forEach(benPlanId -> {
			BenefitPlanCompare futureBenPlan = futurePlanAttributes.get(benPlanId);
			if (futureBenPlan == null) {
				log.error("No future plan attributes returned by BCR for plan id: ", benPlanId);
			} else {
				BenefitPlan basePlan = regionalBasePlanMappings.get(futureBenPlan.getPlanId());
				if (ObjectUtils.isNotEmpty(basePlan)) {
					futureBenPlan.setName(basePlan.getDescr());
				}
				BenPlanDetail futurePlanDetail = constructFuturePlanDetailObject(futureBenPlan, planRate,
						planIdToStrategyPlanSelection, baseToRegionalPlanMappings, benefitPlanToRegions);
				futurePlansDetails.add(futurePlanDetail);
			}
		});
		return futurePlansDetails;
	}

	private BenPlanDetail constructCurrentPlanDetailObject(BenefitPlanCompare currentBenPlan) {
		List<Attribute> currentAttributesTmp = contructAttributeObjects(currentBenPlan);
		return BenPlanDetail.builder().planId(currentBenPlan.getPlanId())
				.planName(currentBenPlan.getName() != null ? currentBenPlan.getName() : currentBenPlan.getPlanName())
				.attributes(currentAttributesTmp).build();
	}

	private BenPlanDetail constructFuturePlanDetailObject(BenefitPlanCompare benPlan,
                                                          Map<String, List<BenefitPlanRate>> planRate,
                                                          Map<String, PlanSelectionView> planIdToStrategyPlanSelection,
                                                          Map<String, List<String>> baseToRegionalPlanMappings, Map<String, List<String>> benefitPlanToRegions) {
		List<Attribute> currentAttributesTmp = contructAttributeObjects(benPlan);
		List<RateDetail> rates = constructFutureRateDetailObjects(benPlan, planRate,
				planIdToStrategyPlanSelection, baseToRegionalPlanMappings, benefitPlanToRegions);

		return BenPlanDetail.builder().planId(benPlan.getPlanId()).planName(benPlan.getName())
				.attributes(currentAttributesTmp).rates(rates).build();
	}

	private List<RateDetail> constructFutureRateDetailObjects(BenefitPlanCompare futureBenPlan,
			Map<String, List<BenefitPlanRate>> planRate,
			Map<String, PlanSelectionView> planIdToStrategyPlanSelection,
			Map<String, List<String>> baseToRegionalPlanMappings, Map<String, List<String>> benefitPlanToRegions) {
		List<RateDetail> rateDetails = new ArrayList<>();
		if (baseToRegionalPlanMappings.containsKey(futureBenPlan.getPlanId())) {
			List<String> regionalPlanIds = baseToRegionalPlanMappings.get(futureBenPlan.getPlanId());
			for (String regionalPlanId : regionalPlanIds) {
				if (planIdToStrategyPlanSelection.containsKey(regionalPlanId)) {
					List<TierRate> tierRates = prepareTierRateObjects(regionalPlanId, planRate);
					List<String> regions = benefitPlanToRegions.containsKey(regionalPlanId)
							? benefitPlanToRegions.get(regionalPlanId)
							: Arrays.asList(REGION_CODE_ALL);
					RateDetail rd = RateDetail.builder().rateType(RateTypeEnum.FOUR_TIER.getType()).regionCode(regions)
							.tierRates(tierRates).build();
					rateDetails.add(rd);
				}
			}
		} else {
			List<TierRate> tierRates = prepareTierRateObjects(futureBenPlan.getPlanId(), planRate);
			RateDetail rd = RateDetail.builder().rateType(RateTypeEnum.FOUR_TIER.getType())
					.regionCode(Arrays.asList(REGION_CODE_ALL)).tierRates(tierRates).build();
			rateDetails.add(rd);
		}
		return rateDetails;
	}

	private List<TierRate> prepareTierRateObjects(String planId,
			Map<String, List<BenefitPlanRate>> planRate) {
		List<TierRate> tierRates = new ArrayList<>();
        Map<String, BigDecimal> planCostMap = StrategyUtils.getPlanCost(planRate.get(planId));
		planCostMap.entrySet().forEach(entrySet -> tierRates
				.add(TierRate.builder().cvgTierCode(entrySet.getKey()).cost(entrySet.getValue()).build()));
		return tierRates;
	}

	private List<Attribute> contructAttributeObjects(BenefitPlanCompare benPlan) {
		List<Attribute> currentAttributesTmp = new ArrayList<>();

		for (PlanCompareTemplate template : benPlan.getTemplate()) {
			for (com.trinet.ambis.service.model.plancompare.Attribute attribute : template.getChildren()) {
				if (attribute != null && StringUtils.isNotEmpty(attribute.getName())) {
					currentAttributesTmp.add(Attribute.builder().id(attribute.getId()).name(attribute.getName())
							.value(attribute.getValue()).build());
				}
			}
		}
		return currentAttributesTmp;
	}

	private Map<String, Map<String, String>> converToMap(Optional<List<EmployeePlansRes>> employeePlans, Map<String,String> bplToBcrIdMap) {
		Map<String, Map<String, String>> currentPlans = new HashMap<>();
		if (employeePlans.isPresent()) {
			currentPlans.put(BSSApplicationConstants.MEDICAL_PLAN_TYPE, new HashMap<>());
			currentPlans.put(BSSApplicationConstants.DENTAL_PLAN_TYPE, new HashMap<>());
			currentPlans.put(BSSApplicationConstants.VISION_PLAN_TYPE, new HashMap<>());
			for (EmployeePlansRes employeePlan : employeePlans.get()) {
				currentPlans.get(BSSApplicationConstants.MEDICAL_PLAN_TYPE).put(employeePlan.getEmployeeId(),
						PlanCompareHelper.MED_NO_PLAN_ID);
				currentPlans.get(BSSApplicationConstants.DENTAL_PLAN_TYPE).put(employeePlan.getEmployeeId(),
						PlanCompareHelper.DEN_NO_PLAN_ID);
				currentPlans.get(BSSApplicationConstants.VISION_PLAN_TYPE).put(employeePlan.getEmployeeId(),
						PlanCompareHelper.VIS_NO_PLAN_ID);
				employeePlan.getBenefitPlans().stream().forEach(benPlan -> {
					currentPlans.get(benPlan.getBenefitTypeCode()).put(employeePlan.getEmployeeId(),
							benPlan.getBplPlanId());
					
					if(StringUtils.isNotEmpty(benPlan.getBplPlanId())) {
						bplToBcrIdMap.put( benPlan.getBplPlanId(),benPlan.getBenefitPlanId());
					}

				});
			}
		}
		return currentPlans;
	}

}